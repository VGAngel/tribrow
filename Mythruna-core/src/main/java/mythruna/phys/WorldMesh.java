package mythruna.phys;

import mythruna.BlockType;
import mythruna.Coordinates;
import mythruna.Vector3i;
import mythruna.World;
import mythruna.db.*;
import mythruna.mathd.Vec3d;
import mythruna.util.LruCache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

public class WorldMesh {

    private World world;
    private LruCache<Vector3i, WorldLeaf> cache = new LruCache<Vector3i, WorldLeaf>("CollisionTileCache", 20);
    private ConcurrentLinkedQueue<LeafData> changed = new ConcurrentLinkedQueue<LeafData>();
    private byte[] inverseMask;
    private Collider[] colliders;

    public WorldMesh(World world) {
        this.world = world;

        world.getWorldDatabase().addLeafChangeListener(new WorldListener());
        Colliders.initialize();

        this.inverseMask = Colliders.inverseMask;
        this.colliders = Colliders.colliders;
    }

    public void applyChanges() {
        Map<WorldLeaf, LeafData> pending = new HashMap<>();

        LeafData leaf = null;
        while ((leaf = (LeafData) this.changed.poll()) != null) {
            LeafInfo info = leaf.getInfo();
            WorldLeaf tile = getTile(info.x, info.y, info.z, false);
            if (tile != null) {
                pending.put(tile, leaf);
            }

        }

        for (Map.Entry<WorldLeaf, LeafData> e : pending.entrySet()) {
            (e.getKey()).update(e.getValue());
        }
    }

    private WorldLeaf getTile(int x, int y, int z, boolean create) {
        Vector3i key = new Vector3i(Coordinates.worldToLeaf(x), Coordinates.worldToLeaf(y), Coordinates.worldToLeaf(z));

        WorldLeaf tile = (WorldLeaf) this.cache.get(key);
        if ((tile == null) && (create)) {
            Vector3i baseLoc = Coordinates.leafToWorld(key);
            LeafData leaf = this.world.getWorldDatabase().getLeaf(baseLoc.x, baseLoc.y, baseLoc.z);
            tile = new WorldLeaf(this.world, leaf);

            this.cache.put(key, tile);
        }
        return tile;
    }

    private int getWorldType(int x, int y, int z) {
        if ((z < 0) || (z >= 160))
            return 0;
        WorldLeaf tile = getTile(x, y, z, true);
        return tile.getWorldType(x, y, z);
    }

    private int getWorldMask(int x, int y, int z) {
        if ((z < 0) || (z >= 160))
            return 0;
        WorldLeaf tile = getTile(x, y, z, true);
        return tile.getWorldMask(x, y, z);
    }

    public void collideWithWorld(CollisionMesh cm, MaskStrategy maskStrat, List<Contact> contacts) {
        BlueprintData bp = cm.getMesh();
        double scale = bp.scale;
        double cellRadius = scale * 0.5D;
        int[][][] cells = bp.cells;

        byte[][][] clipMasks = cm.getClipMasks();

        Vec3d xModel = new Vec3d(1.0D, 0.0D, 0.0D);
        Vec3d yModel = new Vec3d(0.0D, 1.0D, 0.0D);
        Vec3d zModel = new Vec3d(0.0D, 0.0D, 1.0D);

        Vec3d xWorld = cm.orientation.mult(xModel);
        Vec3d yWorld = cm.orientation.mult(yModel);
        Vec3d zWorld = cm.orientation.mult(zModel);

        xWorld.multLocal(scale);
        yWorld.multLocal(scale);
        zWorld.multLocal(scale);

        Vec3d base = new Vec3d();

        base.subtractLocal(cm.cog);
        base.x += cellRadius;
        base.y += cellRadius;
        base.z += cellRadius;

        base = cm.orientation.mult(base);
        base.addLocal(cm.position);

        Vec3d px = base.clone();
        Vec3d py = new Vec3d();
        Vec3d pz = new Vec3d();

        int[] masks = new int[2];

        for (int i = 0; i < bp.xSize; ) {
            py.set(px);
            for (int j = 0; j < bp.ySize; ) {
                pz.set(py);
                for (int k = 0; k < bp.zSize; ) {
                    int val = cells[i][j][k];

                    if (val != 0) {
                        int baseMask = clipMasks[i][j][k];
                        baseMask = 63;
                        int invMask = this.inverseMask[baseMask];

                        int xMin = Coordinates.worldToCell(pz.x - cellRadius);
                        int yMin = Coordinates.worldToCell(pz.z - cellRadius);
                        int zMin = Coordinates.worldToCell(pz.y - cellRadius);
                        int xMax = Coordinates.worldToCell(pz.x + cellRadius);
                        int yMax = Coordinates.worldToCell(pz.z + cellRadius);
                        int zMax = Coordinates.worldToCell(pz.y + cellRadius);

                        Vec3d cellPos = new Vec3d();

                        for (int x = xMin; x <= xMax; x++) {
                            for (int y = yMin; y <= yMax; y++) {
                                for (int z = zMin; z <= zMax; z++) {
                                    int t = getWorldType(x, y, z);
                                    if (t != 0) {
                                        BlockType type = mythruna.BlockTypeIndex.types[t];
                                        if ((type != null) && (type.getGeomFactory().isClipped())) {
                                            cellPos.x = x;
                                            cellPos.y = z;
                                            cellPos.z = y;

                                            int mask = getWorldMask(x, y, z);

                                            if (maskStrat != null) {
                                                if (maskStrat.getMasks(mask, invMask, type, masks)) {
                                                    mask = masks[0];
                                                    invMask = masks[1];

                                                    invMask = 63;
                                                }
                                            }

                                            int combinedMask = invMask & mask;
                                            if (combinedMask != 0) {
                                                Collider coll = this.colliders[t];
                                                if (coll != null) {
                                                    Contact cont = coll.getContact(cellPos, pz, cellRadius, mask, invMask);
                                                    if (cont != null) {
                                                        cont.setMeshData(cm, null, 0.95D, 0.05D);
                                                        contacts.add(cont);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    k++;
                    pz.addLocal(yWorld);
                }
                j++;
                py.addLocal(zWorld);
            }
            i++;
            px.addLocal(xWorld);
        }
    }

    private class WorldListener
            implements LeafChangeListener {
        private WorldListener() {
        }

        public void leafChanged(LeafChangeEvent event) {
            LeafData data = event.getLeaf();
            WorldMesh.this.changed.add(data);
        }
    }
}