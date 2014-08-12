package mythruna.client.view;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import mythruna.BlockType;
import mythruna.World;
import mythruna.db.BlueprintData;
import mythruna.es.EntityId;
import mythruna.geom.GeomPartBuffer;
import mythruna.geom.LeafMesh;
import mythruna.util.LruCache;
import org.progeeks.util.log.Log;

import java.util.ArrayList;
import java.util.List;

public class BlockObject implements Cloneable, BuilderReference {
    static Log log = Log.getLog();

    private static LruCache<Long, Node> meshCache = new LruCache<>("ObjectMeshes", 200);

    private static long instanceCount = 0L;
    private long id = instanceCount++;

    private Vector3f position = new Vector3f();

    private Quaternion rotation = new Quaternion();
    private int xSize;
    private int ySize;
    private int zSize;
    private int[][][] cells;
    private float sunlight = 1.0F;
    private float localLight = 0.0F;
    private float scale = 0.25F;
    private World world;
    private long blueprintId;
    private EntityId entity;
    private Node node;
    private Node builder;
    private boolean built = false;
    private boolean cache = true;

    public BlockObject(int xSize, int ySize, int zSize) {
        this.xSize = xSize;
        this.ySize = ySize;
        this.zSize = zSize;

        this.cells = new int[xSize][ySize][zSize];
    }

    public BlockObject(BlueprintData data) {
        this(data, true);
    }

    public BlockObject(BlueprintData data, boolean cache) {
        this.xSize = data.xSize;
        this.ySize = data.ySize;
        this.zSize = data.zSize;
        this.scale = data.scale;

        this.cells = data.cells;

        this.blueprintId = data.id;

        this.cache = false;
    }

    public BlockObject(World world, long blueprintId) {
        this.world = world;
        this.blueprintId = blueprintId;

        Node cached = (Node) meshCache.get(Long.valueOf(blueprintId));
        if (cached != null) {
            this.node = ((Node) cached.deepClone());
            this.node.setName(String.valueOf(this));

            this.scale = this.node.getLocalScale().x;

            uncloneMaterials(cached, this.node);
            this.built = true;

            this.cache = false;

            relight(this.node);
        }
    }

    public long getBlueprintId() {
        return this.blueprintId;
    }

    public void setBlueprintId(long blueprintId) {
        if (this.blueprintId == blueprintId) {
            return;
        }
        this.blueprintId = blueprintId;

        Node cached = (Node) meshCache.get(Long.valueOf(blueprintId));
        if (cached != null) {
            Node newNode = (Node) cached.deepClone();
            newNode.setName(String.valueOf(this));

            this.scale = newNode.getLocalScale().x;

            uncloneMaterials(cached, newNode);

            this.cache = false;

            this.builder = newNode;
            relight(this.builder);
            this.built = false;
            applyUpdates(null);
            this.built = true;
        } else {
            this.cells = ((int[][][]) null);
            this.built = false;
        }
    }

    public void setEntity(EntityId e) {
        this.entity = e;
    }

    public EntityId getEntity() {
        return this.entity;
    }

    protected void uncloneMaterials(Node original, Node clone) {
        int count = original.getQuantity();
        for (int i = 0; i < count; i++) {
            Spatial s1 = original.getChild(i);
            Spatial s2 = clone.getChild(i);
            if ((s1 instanceof Geometry)) {
                s2.setMaterial(((Geometry) s1).getMaterial());
            }
        }
    }

    protected void dumpControls(Spatial s) {
        System.out.println("Spatial:" + s + "  controls:");
        for (int i = 0; i < s.getNumControls(); i++) {
            System.out.println(" [" + i + "] = " + s.getControl(i));
        }

        if ((s instanceof Node)) {
            for (Spatial child : ((Node) s).getChildren()) {
                dumpControls(child);
            }
        }
    }

    public long getId() {
        return this.id;
    }

    public int[][][] getCells() {
        return this.cells;
    }

    public void setPosition(float x, float y, float z) {
        this.position.set(x, y, z);
    }

    public void setPosition(Vector3f v) {
        this.position.set(v);
    }

    public Vector3f getPosition() {
        return this.position;
    }

    public void setRotation(Quaternion rot) {
        this.rotation.set(rot);
    }

    public Quaternion getRotation() {
        return this.rotation;
    }

    public int getSizeX() {
        return this.xSize;
    }

    public int getSizeY() {
        return this.ySize;
    }

    public int getSizeZ() {
        return this.zSize;
    }

    public void setScale(float scale) {
        this.scale = scale;
        if (this.node != null)
            this.node.setLocalScale(scale);
    }

    public float getScale() {
        return this.scale;
    }

    public void setType(int i, int j, int k, int type) {
        this.cells[i][j][k] = type;
    }

    public BlockObject cloneFully() {
        try {
            BlockObject clone = (BlockObject) super.clone();
            clone.position = this.position.clone();
            clone.rotation = this.rotation.clone();
            clone.id = (instanceCount++);

            if (this.node == null) {
                throw new RuntimeException("Cannot clone an unbuilt block object.");
            }

            clone.node = this.node.clone(false);

            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("This should never happen", e);
        }
    }

    protected int getAdjacent(int x, int y, int z, int d) {
        x += mythruna.Direction.DIRS[d][0];
        y += mythruna.Direction.DIRS[d][1];
        z += mythruna.Direction.DIRS[d][2];

        if ((x < 0) || (y < 0) || (z < 0))
            return 0;
        if ((x >= this.xSize) || (y >= this.ySize) || (z >= this.zSize))
            return 0;
        return this.cells[x][y][z];
    }

    public Node getNode() {
        if (this.node == null) {
            this.node = new Node(String.valueOf(this));
            this.node.setLocalScale(this.scale);
        }
        return this.node;
    }

    public boolean isBuilt() {
        return this.built;
    }

    public void build() {
        if ((this.builder != null) || (isBuilt())) {
            log.info("Canceling build for already built object.");
            return;
        }

        Node cached = (Node) meshCache.get(Long.valueOf(this.blueprintId));
        if (cached != null) {
            this.builder = ((Node) cached.deepClone());
            this.builder.setName(String.valueOf(this) + ":builderClone");

            uncloneMaterials(cached, this.builder);

            if (log.isDebugEnabled()) {
                log.debug("Reused existing geometry for:" + this);
            }
            relight(this.builder);

            return;
        }

        if (this.cells == null) {
            BlueprintData data = this.world.getBlueprint(this.blueprintId);

            this.xSize = data.xSize;
            this.ySize = data.ySize;
            this.zSize = data.zSize;
            this.scale = data.scale;

            this.cells = data.cells;

            this.blueprintId = data.id;
        }

        if (log.isDebugEnabled())
            log.debug("Creating builder node for:" + this);
        this.builder = new Node(String.valueOf(this) + ":builder");
        synchronized (this) {
            generateNode(this.builder, this.sunlight, this.localLight);
        }
        if (log.isDebugEnabled())
            log.debug("Geometry generated.");
    }

    public void applyUpdates(LocalArea parent) {
        if (isBuilt()) {
            return;
        }

        if ((this.node != null) && (this.node.getParent() == null)) {
            log.info("Canceling applyUpdates for detached node.");
            return;
        }
        getNode();

        if (log.isDebugEnabled()) {
            log.debug("Applying updates for:" + this);
        }

        this.node.detachAllChildren();

        List<Spatial> children = new ArrayList<Spatial>(this.builder.getChildren());

        this.builder.detachAllChildren();

        for (Spatial s : children)
            this.node.attachChild(s);
        this.node.setLocalScale(this.scale);

        if (log.isDebugEnabled()) {
            log.debug("Builder done for:" + this);
        }
        this.builder = null;

        if (this.cache) {
            if (!meshCache.containsKey(Long.valueOf(this.blueprintId))) {
                Node cacheNode = (Node) this.node.deepClone();
                uncloneMaterials(this.node, cacheNode);
                meshCache.put(Long.valueOf(this.blueprintId), cacheNode);
            }
        }
    }

    public void setLighting(float sunlight, float localLight) {
        if ((this.sunlight == sunlight) && (this.localLight == localLight))
            return;
        synchronized (this) {
            this.sunlight = sunlight;
            this.localLight = localLight;
        }

        if (this.node != null) {
            relight(this.node, sunlight, localLight);
        }
    }

    protected void relight(Node n) {
        relight(n, this.sunlight, this.localLight);
    }

    protected void relight(Node n, float sun, float local) {
        for (Spatial child : n.getChildren()) {
            if ((child instanceof Geometry)) {
                Mesh m = ((Geometry) child).getMesh();
                if ((m instanceof LeafMesh)) {
                    ((LeafMesh) m).relight(sun, local);
                }
            }
        }
    }

    public GeomPartBuffer generateParts(float sun, float localLight) {
        GeomPartBuffer parts = new GeomPartBuffer();

        for (int k = 0; k < this.zSize; k++) {
            for (int i = 0; i < this.xSize; i++) {
                for (int j = 0; j < this.ySize; j++) {
                    int t = this.cells[i][j][k];
                    BlockType type = mythruna.BlockTypeIndex.types[t];

                    if (t != 0) {
                        int d = -1;

                        if (k == 0) {
                            d = 5;
                            type.getGeomFactory().createGeometry(parts, i, j, k, i, j, k, sun, localLight, type, d);
                        }

                        if (i == 0) {
                            d = 3;
                            type.getGeomFactory().createGeometry(parts, i, j, k, i, j, k, sun, localLight, type, d);
                        }

                        if (j == 0) {
                            d = 0;
                            type.getGeomFactory().createGeometry(parts, i, j, k, i, j, k, sun, localLight, type, d);
                        }

                        if (k == this.zSize - 1) {
                            d = 4;
                            type.getGeomFactory().createGeometry(parts, i, j, k, i, j, k, sun, localLight, type, d);
                        }

                        if (i == this.xSize - 1) {
                            d = 2;
                            type.getGeomFactory().createGeometry(parts, i, j, k, i, j, k, sun, localLight, type, d);
                        }

                        if (j == this.ySize - 1) {
                            d = 1;
                            type.getGeomFactory().createGeometry(parts, i, j, k, i, j, k, sun, localLight, type, d);
                        }

                        if ((type.isSolid()) && (type.getGroup() == 0)) ;
                    } else {
                        for (int d = 0; d < 6; d++) {
                            int adj = getAdjacent(i, j, k, d);
                            if (adj != 0) {
                                BlockType adjType = mythruna.BlockTypeIndex.types[adj];

                                if ((type == null) ||
                                        (adjType.getGroup() != type.getGroup()) || (
                                        (adjType.getGroup() == 0) && (!type.isSolid(d)))) {
                                    int x = i + mythruna.Direction.DIRS[d][0];
                                    int y = j + mythruna.Direction.DIRS[d][1];
                                    int z = k + mythruna.Direction.DIRS[d][2];
                                    int back = mythruna.Direction.INVERSE[d];

                                    adjType.getGeomFactory().createGeometry(parts, x, y, z, x, y, z, sun, localLight, adjType, back);
                                }
                            }
                        }

                        if ((t != 0) && (!type.isSolid())) {
                            type.getGeomFactory().createInternalGeometry(parts, i, j, k, i, j, k, sun, localLight, type);
                        }
                    }
                }
            }

        }

        return parts;
    }

    protected Node generateNode(Node node, float sun, float localLight) {
        GeomPartBuffer parts = generateParts(sun, localLight);

        parts.createGeometry(node, -this.xSize / 2.0F, -this.ySize / 2.0F, 0.0F);
        return node;
    }
}