package mythruna.phys;

import mythruna.BlockType;
import mythruna.BlockTypeIndex;
import mythruna.Coordinates;
import mythruna.World;
import mythruna.db.BlueprintData;
import mythruna.es.EntityData;
import mythruna.es.EntityId;
import mythruna.mathd.Quatd;
import mythruna.mathd.Vec3d;

import java.util.*;

public class CollisionSystem {

    private World world;
    private EntityData ed;
    private WorldMesh worldMesh;
    private ActiveObjects active;
    private byte[] inverseMask;
    private Collider[] colliders;
    private boolean started = false;

    public CollisionSystem(World world, EntityData ed) {
        this.world = world;
        this.ed = ed;
    }

    public void start() {
        if (this.started)
            return;
        this.started = true;

        if (!BlockTypeIndex.isInitialized()) {
            throw new RuntimeException("Block types are not initialized.");
        }
        this.worldMesh = new WorldMesh(this.world);
        this.active = new ActiveObjects(this.world, this.ed);

        this.inverseMask = Colliders.inverseMask;
        this.colliders = Colliders.colliders;
    }

    public void shutdown() {
        if (this.active != null) {
            this.active.close();
            this.worldMesh = null;
            this.active = null;
            this.started = false;
        }
    }

    public boolean applyChanges() {
        boolean changed = this.active.applyChanges();
        this.worldMesh.applyChanges();
        return changed;
    }

    public void resetActive(int xMin, int yMin, int xMax, int yMax) {
        this.active.resetActive(xMin, yMin, xMax, yMax);
    }

    public CollisionMesh getCollisionMesh(EntityId entity) {
        CollisionMesh mesh = this.active.getCollisionMesh(entity);
        return mesh;
    }

    public void generateWorldCollisions(CollisionMesh cm, List<Contact> contacts) {
        this.worldMesh.collideWithWorld(cm, null, contacts);

        collideWithObjects(cm, null, true, contacts);
    }

    public void generateObjectCollisions(List<Contact> contacts) {
        Set visited = new HashSet();

        for (Iterator i$ = this.active.iterator(); i$.hasNext(); ) {
            CollisionMesh m = (CollisionMesh) i$.next();

            if (!m.isStatic()) {
                visited.add(m);

                for (CollisionMesh sub : this.active) {
                    if ((!visited.contains(sub)) &&
                            (broadPhase(m, sub))) {
                        generateContacts(sub, m, null, true, contacts);
                    }
                }
            }
        }
        CollisionMesh m;
    }

    public List<Contact> getCollisions(CollisionMesh cm, MaskStrategy maskStrat, List<Contact> contacts) {
        if (this.active == null) {
            return Collections.emptyList();
        }

        this.active.resetActive(cm);
        this.active.applyChanges();

        this.worldMesh.applyChanges();
        this.worldMesh.collideWithWorld(cm, maskStrat, contacts);

        collideWithObjects(cm, maskStrat, false, contacts);

        return contacts;
    }

    protected void collideWithObjects(CollisionMesh cm, MaskStrategy maskStrat, boolean reorder, List<Contact> contacts) {
        for (CollisionMesh m : this.active) {
            if ((m != cm) && (m.isStatic()) &&
                    (broadPhase(cm, m))) {
                generateContacts(m, cm, maskStrat, reorder, contacts);
            }
        }
    }

    protected boolean broadPhase(CollisionMesh cm1, CollisionMesh cm2) {
        Vec3d p1 = cm1.position;
        Vec3d o1 = cm1.boundsCenter;
        Vec3d p2 = cm2.position;
        Vec3d o2 = cm2.boundsCenter;

        double x = p2.x + o2.x - (p1.x + o1.x);
        double y = p2.y + o2.y - (p1.y + o1.y);
        double z = p2.z + o2.z - (p1.z + o1.z);

        double d = x * x + y * y + z * z;
        double r = cm1.getMaxRadius() + cm2.getMaxRadius();
        r *= r;

        return d < r;
    }

    protected void generateContacts(CollisionMesh cm1, CollisionMesh cm2, MaskStrategy maskStrat, boolean reorder, List<Contact> contacts) {
        CollisionMesh target = cm1;
        CollisionMesh source = cm2;
        double scale1 = cm1.getMesh().scale;
        double scale2 = cm2.getMesh().scale;

        if ((reorder) && (cm1.getSize() < cm2.getSize()) && (maskStrat == null)) {
            target = cm2;
            source = cm1;
        }

        BlueprintData bp1 = target.getMesh();
        BlueprintData bp2 = source.getMesh();
        scale1 = bp1.scale;
        scale2 = bp2.scale;
        double cellRadius1 = scale1 * 0.5D;
        double cellRadius2 = scale2 * 0.5D;
        int[][][] targetCells = bp1.cells;
        byte[][][] targetMasks = target.getClipMasks();

        int[][][] sourceCells = bp2.cells;
        byte[][][] sourceMasks = source.getClipMasks();

        Vec3d xModel = new Vec3d(scale2, 0.0D, 0.0D);
        Vec3d yModel = new Vec3d(0.0D, scale2, 0.0D);
        Vec3d zModel = new Vec3d(0.0D, 0.0D, scale2);

        xModel = source.orientation.mult(xModel);
        yModel = source.orientation.mult(yModel);
        zModel = source.orientation.mult(zModel);

        Quatd inverse = target.orientation.inverse();
        xModel = inverse.mult(xModel);
        yModel = inverse.mult(yModel);
        zModel = inverse.mult(zModel);

        double invScale1 = 1.0D / scale1;
        xModel.multLocal(invScale1);
        yModel.multLocal(invScale1);
        zModel.multLocal(invScale1);

        double cellRadius2in1 = invScale1 * cellRadius2;

        Vec3d sourceOrigin = new Vec3d();
        sourceOrigin.subtractLocal(source.cog);

        sourceOrigin.x += cellRadius2;
        sourceOrigin.y += cellRadius2;
        sourceOrigin.z += cellRadius2;

        sourceOrigin = source.orientation.mult(sourceOrigin);

        sourceOrigin.addLocal(source.position);

        Vec3d base = sourceOrigin.subtract(target.position);

        base = inverse.mult(base);

        base.addLocal(target.cog);

        base.multLocal(invScale1);

        double sourceCellRadius = scale2 * 0.5D;

        double sourceCellRadiusInTarget = invScale1 * sourceCellRadius;

        int xSourceSize = source.getMesh().xSize;
        int ySourceSize = source.getMesh().ySize;
        int zSourceSize = source.getMesh().zSize;
        int xTargetSize = target.getMesh().xSize;
        int yTargetSize = target.getMesh().ySize;
        int zTargetSize = target.getMesh().zSize;

        Vec3d px = base.clone();
        Vec3d py = new Vec3d();
        Vec3d pz = new Vec3d();

        int[] masks = new int[2];

        for (int i = 0; i < xSourceSize; ) {
            py.set(px);
            for (int j = 0; j < ySourceSize; ) {
                pz.set(py);
                for (int k = 0; k < zSourceSize; ) {
                    int val = sourceCells[i][j][k];

                    if (val != 0) {
                        int baseMask = sourceMasks[i][j][k];
                        int invMask = this.inverseMask[baseMask];

                        int xMin = Coordinates.worldToCell(pz.x - sourceCellRadiusInTarget);
                        int yMin = Coordinates.worldToCell(pz.z - sourceCellRadiusInTarget);
                        int zMin = Coordinates.worldToCell(pz.y - sourceCellRadiusInTarget);
                        int xMax = Coordinates.worldToCell(pz.x + sourceCellRadiusInTarget);
                        int yMax = Coordinates.worldToCell(pz.z + sourceCellRadiusInTarget);
                        int zMax = Coordinates.worldToCell(pz.y + sourceCellRadiusInTarget);

                        Vec3d cellPos = new Vec3d();

                        for (int x = xMin; x <= xMax; x++) {
                            for (int y = yMin; y <= yMax; y++) {
                                for (int z = zMin; z <= zMax; z++) {
                                    if ((x >= 0) && (x < xTargetSize)) {
                                        if ((y >= 0) && (y < yTargetSize)) {
                                            if ((z >= 0) && (z < zTargetSize)) {
                                                int t = targetCells[x][y][z];
                                                if (t != 0) {
                                                    BlockType type = BlockTypeIndex.types[t];
                                                    if ((type != null) && (type.getGeomFactory().isClipped())) {
                                                        cellPos.x = x;
                                                        cellPos.y = z;
                                                        cellPos.z = y;

                                                        int mask = targetMasks[x][y][z];

                                                        if (maskStrat != null) ;
                                                        int combinedMask = invMask & mask;
                                                        if (combinedMask != 0) {
                                                            Collider coll = this.colliders[t];
                                                            if (coll != null) {
                                                                Contact cont = coll.getContact(cellPos, pz, sourceCellRadiusInTarget, mask, invMask);

                                                                if (cont != null) {
                                                                    cont.contactNormal = target.orientation.mult(cont.contactNormal, cont.contactNormal);

                                                                    Vec3d cp = cont.contactPoint.multLocal(scale1);

                                                                    cp = cp.subtractLocal(target.cog);

                                                                    target.orientation.mult(cp, cp);

                                                                    cp.addLocal(target.position);

                                                                    cont.penetration *= scale1;

                                                                    cont.setMeshData(source, target, 0.95D, 0.05D);
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
                            }
                        }
                    }
                    k++;
                    pz.addLocal(yModel);
                }
                j++;
                py.addLocal(zModel);
            }
            i++;
            px.addLocal(xModel);
        }
    }
}