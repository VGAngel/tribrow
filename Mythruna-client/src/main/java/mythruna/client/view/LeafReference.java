package mythruna.client.view;

import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import mythruna.BlockType;
import mythruna.Coordinates;
import mythruna.Vector3i;
import mythruna.World;
import mythruna.db.LeafData;
import mythruna.db.LeafInfo;
import mythruna.db.WorldDatabase;
import mythruna.geom.GeomPartBuffer;
import mythruna.geom.LeafMesh;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LeafReference
        implements BuilderReference {
    private static Map<String, LeafReference> dupeCheck = new ConcurrentHashMap();

    public static boolean SIDE_OPTIMIZATION = false;
    private LocalArea.Column parent;
    private LeafData leaf;
    private Vector3i leafLocation;
    private boolean[] invisible = new boolean[6];
    private World world;
    private WorldDatabase worldDb;
    private Node node;
    private long nodeVersion;
    private Vector3f nodePos = new Vector3f();
    private Node newNode;
    private long newVersion;
    private boolean released = false;

    public LeafReference(LocalArea.Column parent, World world, int x, int y, int z) {
        if (z < 0) {
            throw new IllegalArgumentException("z cannot be negative.");
        }
        this.parent = parent;
        this.world = world;
        this.worldDb = world.getWorldDatabase();
        this.leafLocation = new Vector3i(x, y, z);
    }

    public Vector3i getLeafLocation() {
        return this.leafLocation;
    }

    public LeafData getLeaf() {
        return this.leaf;
    }

    public void setNodePosition(float x, float y, float z) {
        this.nodePos.set(x, y, z);
        Node n = this.node;
        if (n != null)
            n.setLocalTranslation(x, y, z);
    }

    public boolean needsUpdate() {
        if (this.leaf == null) {
            return true;
        }
        if (this.nodeVersion < this.leaf.getVersion()) {
            return true;
        }
        return false;
    }

    public boolean adjustVisibility(int i, int j, int k) {
        int count = 0;

        if (SIDE_OPTIMIZATION) {
            count += setInvisibility(3, i < 0);
            count += setInvisibility(2, i > 0);

            count += setInvisibility(0, j < 0);
            count += setInvisibility(1, j > 0);
        }

        return count != 0;
    }

    protected int setInvisibility(int dir, boolean b) {
        if (this.invisible[dir] == b) {
            return 0;
        }
        this.invisible[dir] = b;
        return 1;
    }

    public boolean[] getInvisibility() {
        return this.invisible;
    }

    protected void deleteBuffers(Node n) {
        for (Spatial child : n.getChildren()) {
            if (!(child instanceof Geometry)) {
                System.out.println("Encountered non-geometry child:" + child);
            } else {
                LeafMesh m = (LeafMesh) ((Geometry) child).getMesh();
                m.deleteBuffers();
            }
        }
    }

    public void release() {
        if (this.released)
            return;
        this.released = true;
        if (this.node != null) {
            deleteBuffers(this.node);
        }
    }

    public void applyUpdates(LocalArea localArea) {
        if (this.released) {
            return;
        }

        Node next = this.newNode;
        Node last = this.node;

        if ((last == null) && (next == null)) {
            this.nodeVersion = this.newVersion;
            return;
        }

        if ((this.node == null) || (next == null)) {
            this.node = next;
            this.nodeVersion = this.newVersion;
        } else if (this.newVersion >= this.nodeVersion) {
            this.node = next;
            this.nodeVersion = this.newVersion;
        }

        if (this.node == last) {
            return;
        }
        if (last != null) {
            this.parent.detachChild(last);

            deleteBuffers(last);
        }

        if (this.node != null) {
            this.node.setLocalTranslation(this.nodePos);
            this.parent.attachChild(this.node);
        }
    }

    public void build() {
        this.leaf = this.worldDb.getLeaf(Coordinates.leafToWorld(this.leafLocation.x), Coordinates.leafToWorld(this.leafLocation.y), Coordinates.leafToWorld(this.leafLocation.z));

        if (this.leaf == null) {
            System.out.println("*********** why isn't there a leaf for:" + this);
            return;
        }

        this.newVersion = this.leaf.getVersion();

        this.newNode = generateNode(this.invisible);
    }

    public Node generateNode() {
        return generateNode(new boolean[6]);
    }

    public Node generateNode(boolean[] invisible) {
        LeafInfo info = this.leaf.getInfo();

        GeomPartBuffer parts = new GeomPartBuffer();
        long leafSeed = Coordinates.worldToLeaf(info.x) << 32 | Coordinates.worldToLeaf(info.y);

        parts.setRandomSeed(leafSeed);

        for (int k = 0; k < 32; k++) {
            for (int i = 0; i < 32; i++) {
                for (int j = 0; j < 32; j++) {
                    int v = this.leaf.getRaw(i, j, k);
                    int t = LeafData.toType(v);
                    float sun = LeafData.toSunlight(v) / 16.0F;
                    float light = LeafData.toLocalLight(v) / 16.0F;

                    BlockType type = mythruna.BlockTypeIndex.types[t];

                    if ((t == 0) || (!type.isSolid()) || (type.getGroup() != 0)) {
                        for (int d = 0; d < 6; d++) {
                            if (invisible[d]) {
                                int adj = this.world.getType(info.x + i, info.y + j, info.z + k, d, this.leaf);

                                if (adj != 0) {
                                    int back = mythruna.Direction.INVERSE[d];

                                    BlockType adjType = mythruna.BlockTypeIndex.types[adj];

                                    if (type != null) {
                                        if (adjType.getGroup() == type.getGroup()) {
                                            if (type.isSolid(d)) {
                                                continue;
                                            }

                                            if (type.getBoundary(d) == null)
                                                throw new RuntimeException("Type has null boundary(" + d + ") type:" + type);
                                            if (adjType.getBoundary(back) == null) {
                                                throw new RuntimeException("Type has null boundary(" + back + ") type:" + adjType);
                                            }

                                            if (type.getBoundary(d).isMatchingFace(adjType.getBoundary(back))) {
                                                continue;
                                            }

                                        }

                                    }

                                    int x = i + mythruna.Direction.DIRS[d][0];
                                    int y = j + mythruna.Direction.DIRS[d][1];
                                    int z = k + mythruna.Direction.DIRS[d][2];

                                    adjType.getGeomFactory().createGeometry(parts, x, y, z, info.x + x, info.y + y, info.z + z, sun, light, adjType, back);
                                }
                            }
                        }

                        if ((t != 0) && (!type.isSolid())) {
                            type.getGeomFactory().createInternalGeometry(parts, i, j, k, info.x + i, info.y + j, info.z + k, sun, light, type);
                        }
                    }
                }
            }

        }

        if (parts.size() == 0) {
            return null;
        }

        for (int i = 0; i < 5; i++) {
            try {
                return parts.createNode(info.x + ", " + info.y + ", " + info.z + " v:" + this.nodeVersion + "+" + System.currentTimeMillis());
            } catch (OutOfMemoryError e) {
                System.out.println("********************** Ran out of memory:" + e);

                Runtime rt = Runtime.getRuntime();
                long free = rt.freeMemory();
                long max = rt.maxMemory();
                long total = rt.totalMemory();

                System.out.println("free:" + free + "  total:" + total + "  max:" + max);
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                System.out.println("********************** Forcing GC to run.");
                System.gc();
                if (i < 4)
                    System.out.println("********************** Trying again: " + i + "/5");
            }
        }
        return null;
    }

    public String toString() {
        return "LeafReference[" + this.leafLocation + "  leaf:" + this.leaf + "]";
    }
}
