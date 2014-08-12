package mythruna.client.view;

import com.jme3.collision.Collidable;
import com.jme3.collision.CollisionResults;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.debug.Arrow;
import com.jme3.scene.debug.WireBox;
import mythruna.Coordinates;
import mythruna.MaterialIndex;
import mythruna.Vector3i;
import mythruna.World;
import mythruna.client.Avatar;
import mythruna.client.ClientOptions;
import mythruna.client.GameClient;
import mythruna.client.MainStart;
import mythruna.db.*;
import mythruna.es.*;
import mythruna.phys.proto.ContactDebug;
import mythruna.phys.proto.PhysicsDebug;
import mythruna.sim.FrameTransition;
import mythruna.sim.Mob;
import mythruna.sim.MobChangeListener;
import mythruna.sim.MobClass;
import mythruna.util.ReportSystem;
import mythruna.util.Reporter;
import org.progeeks.util.log.Log;

import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LocalArea extends Node {

    static Log log = Log.getLog();
    public static final int LOCAL_AREA_SIZE_XY = 6;
    public static final int LOCAL_AREA_SIZE_Z = 5;
    private int sizeXY = 13;
    private int sizeZ = 5;

    private int xCenter = 6;
    private int yCenter = 6;

    public static int activeColumns = 0;

    private Column noColumn = new Column();

    private int clipDistance = 4;

    private int objectClip = 1;

    private Vector3i areaLocation = new Vector3i();
    private Vector3i newLocation;
    private Column[][] area = new Column[this.sizeXY][this.sizeXY];
    private boolean invalid = false;
    private World world;
    private WorldDatabase worldDb;
    private Vector3f worldLocation = null;
    private Vector3f lastWorldLocation = null;

    private Node root = new Node("LocalArea");
    private LeafPriority priority;
    private Set<Column> toRelease = new HashSet<>();
    private GeometryBuilder builder;
    private GeometryBuilder objectBuilder;
    private GameClient client;
    private Map<Mob, Column> mobColumns = new ConcurrentHashMap<>();
    private Map<Mob, Node> mobNodes = new HashMap<>();
    private ConcurrentLinkedQueue<Column> relight = new ConcurrentLinkedQueue<>();
    private int totalTileCount;
    private ClaimFieldControl worldClaims;
    private RopeControl ropes;
    private boolean physicsDebug = false;

    public LocalArea(GameClient client, World world) {
        super("LocalArea");
        this.client = client;
        this.world = world;
        this.worldDb = world.getWorldDatabase();
        this.builder = new GeometryBuilder(ClientOptions.getInstance().getGeometryThreadCount());
        this.objectBuilder = new GeometryBuilder(ClientOptions.getInstance().getGeometryThreadCount());

        world.getWorldDatabase().addLeafChangeListener(new LeafObserver());

        client.getMobs().addMobChangeListener(new MobObserver());

        for (Mob e : client.getMobs().mobs(MobClass.PLAYER)) {
            if (e.getId() != client.getPlayer().getId()) {
                this.mobColumns.put(e, this.noColumn);
            }
        }
        this.worldClaims = new ClaimFieldControl(client);
        addControl(this.worldClaims);

        this.ropes = new RopeControl((ObservableEntityData) world.getEntityData());
        addControl(this.ropes);

        ReportSystem.registerCacheReporter(new MemReporter());
    }

    public void setPhysicsDebug(boolean b) {
        if (this.physicsDebug == b) {
            return;
        }
        this.physicsDebug = b;

        for (int x = 0; x < this.sizeXY; x++) {
            for (int y = 0; y < this.sizeXY; y++) {
                if (this.area[x][y] != null)
                    this.area[x][y].setPhysicsDebug(b);
            }
        }
    }

    public boolean getPhysicsDebug() {
        return this.physicsDebug;
    }

    public GeometryBuilder getLeafBuilder() {
        return this.builder;
    }

    public int getPendingSize() {
        return this.builder.getPendingSize();
    }

    public int getTilesLoaded() {
        return this.totalTileCount;
    }

    public void setClipDistance(int distance) {
        if (this.clipDistance == distance) {
            return;
        }
        this.clipDistance = distance;
        this.invalid = true;
    }

    public int getClipDistance() {
        return this.clipDistance;
    }

    public int getMaxClipDistance() {
        return 6;
    }

    public void setLeafPriority(LeafPriority p) {
        this.priority = p;
    }

    public void updateGeometry() {
        if (this.newLocation != null) {
            move(this.newLocation);
            this.newLocation = null;
        }

        if (this.invalid) {
            this.invalid = false;
            refreshColumns();
            this.worldClaims.setLocation(this.areaLocation, this.clipDistance);
            this.ropes.setLocation(this.areaLocation, this.clipDistance);
        }

        while (this.relight.size() > 0) {
            Column col = (Column) this.relight.poll();
            if (col != null) {
                col.relightObjects();
            }
        }
        if ((this.lastWorldLocation == null) || (this.lastWorldLocation.x != this.worldLocation.x) || (this.lastWorldLocation.y != this.worldLocation.y) || (this.lastWorldLocation.z != this.worldLocation.z)) {
            resetLocation();
        }

        updateMobs();

        int maxUpdates = ClientOptions.getInstance().getMaxUpdatesPerFrame();
        int updateCount = 0;
        long start = System.nanoTime();
        updateCount += this.builder.applyUpdates(this, Math.max(1, maxUpdates / 2));
        this.objectBuilder.applyUpdates(this, Math.max(1, maxUpdates / 2));
        this.totalTileCount += updateCount;
        maxUpdates -= updateCount;
        long adds = System.nanoTime();

        if ((this.toRelease.size() > 0) && (maxUpdates > 0)) {
            for (Iterator i = this.toRelease.iterator(); (i.hasNext()) && (maxUpdates > 0); maxUpdates--) {
                Column col = (Column) i.next();
                i.remove();
                col.releaseColumn();
                updateCount++;
            }
        }

        long removes = System.nanoTime();
        long end = System.nanoTime();
        if (updateCount > 0) {
            long updateTime = end - start;
            long addTime = adds - start;
            long removeTime = removes - adds;

            if (updateTime > 1000000L) {
                System.out.println(updateCount + "  Updates performed in > 1 ms:" + (end - start / 1000000.0D) + " ms" + "  add time:" + (adds - start / 1000000.0D) + " ms  remove time:" + (removes - adds / 1000000.0D) + " ms");
            }
        }
    }

    public Vector3f getLocation() {
        return this.worldLocation;
    }

    public void setLocation(float x, float y, float z) {
        if (this.worldLocation == null) {
            this.worldLocation = new Vector3f(x, y, z);
        } else {
            if ((this.worldLocation.x == x) && (this.worldLocation.y == y) && (this.worldLocation.z == z)) {
                return;
            }

            this.worldLocation.set(x, y, z);
        }

        this.newLocation = calculateBase(x, y, z);

        int zCenterNew = Math.max(0, Coordinates.worldToLeaf(z));

        int i = this.newLocation.x - this.areaLocation.x;
        int j = this.newLocation.y - this.areaLocation.y;
        int k = this.newLocation.z - this.areaLocation.z;

        if ((i == 0) && (j == 0) && (k == 0)) {
            this.newLocation = null;
        }
    }

    public float setLocation(float x, float y) {
        int xCell = (int) Math.floor(x);
        int yCell = (int) Math.floor(y);

        for (int l = this.sizeZ; l >= 0; l--) {
            LeafData leaf = this.worldDb.getLeaf(xCell, yCell, l * 32);

            System.out.println(l + ": Found leaf:" + leaf);
            if (leaf != null) {
                int i = xCell % 32;
                int j = yCell % 32;

                int h = leaf.elevation(i, j);
                System.out.println(" h:" + h + "  level:" + l);
                if ((h >= 0) && (h <= 32)) {
                    setLocation(x, y, l * 32 + h);
                    return l * 32 + h;
                }
            }
        }
        return 0.0F;
    }

    public boolean contains(float x, float y, float z) {
        Vector3f loc = getLocation();
        if (Math.abs(x - loc.x) > 192.0F)
            return false;
        if (Math.abs(y - loc.y) > 192.0F)
            return false;
        return (z >= 0.0F) && (z <= 160.0F);
    }

    public Vector3f getSceneLocation(float x, float y, float z) {
        x -= this.areaLocation.x * 32;
        y -= this.areaLocation.y * 32;

        return new Vector3f(x, z, y);
    }

    protected Vector3i calculateBase(float x, float y, float z) {
        int i = Coordinates.worldToLeaf(x);
        int j = Coordinates.worldToLeaf(y);
        int k = Math.min(this.sizeZ - 1, Math.max(0, Coordinates.worldToLeaf(z)));

        return new Vector3i(i - 6, j - 6, k);
    }

    protected void resetLocation() {
        float x = this.worldLocation.x - Coordinates.leafToWorld(this.areaLocation.x + this.xCenter);
        float y = this.worldLocation.y - Coordinates.leafToWorld(this.areaLocation.y + this.yCenter);
        float z = this.worldLocation.z - Coordinates.leafToWorld(this.areaLocation.z);

        x = this.xCenter * -32 - x;
        y = this.yCenter * -32 - y;
        z = this.areaLocation.z * -32 - z;

        setLocalTranslation(x, z, y);
        if (this.lastWorldLocation == null)
            this.lastWorldLocation = new Vector3f(this.worldLocation);
        else
            this.lastWorldLocation.set(this.worldLocation);
    }

    protected Column getWorldColumn(float x, float y) {
        int i = Coordinates.worldToLeaf(x) - this.areaLocation.x;
        int j = Coordinates.worldToLeaf(y) - this.areaLocation.y;

        return getColumn(i, j);
    }

    protected Column getColumn(int x, int y) {
        if ((x < 0) || (y < 0))
            return null;
        if ((x >= this.sizeXY) || (y >= this.sizeXY))
            return null;
        return this.area[x][y];
    }

    protected boolean isClipped(int x, int y) {
        if (Math.abs(x - this.xCenter) > this.clipDistance)
            return true;
        if (Math.abs(y - this.yCenter) > this.clipDistance)
            return true;
        return false;
    }

    protected boolean isObjectsClipped(int x, int y, int z) {
        if (Math.abs(x - this.xCenter) > this.objectClip)
            return true;
        if (Math.abs(y - this.yCenter) > this.objectClip) {
            return true;
        }

        return false;
    }

    protected void move(Vector3i location) {
        Column[][] newArea = new Column[this.sizeXY][this.sizeXY];

        int xDelta = location.x - this.areaLocation.x;
        int yDelta = location.y - this.areaLocation.y;
        int zNew = location.z;

        for (int x = 0; x < this.sizeXY; x++) {
            for (int y = 0; y < this.sizeXY; y++) {
                if ((x < xDelta) || (y < yDelta) || (x >= this.sizeXY + xDelta) || (y >= this.sizeXY + yDelta)) {
                    if (this.area[x][y] != null) {
                        this.area[x][y].markForRelease();
                        this.area[x][y] = null;
                        activeColumns -= 1;
                    }
                }

                Column col = getColumn(x + xDelta, y + yDelta);
                if (col == null) {
                    if (!isClipped(x, y)) {
                        col = new Column(location.x + x, location.y + y);
                        activeColumns += 1;
                        attachChild(col);
                    }
                }
                newArea[x][y] = col;
            }
        }

        this.area = newArea;
        this.areaLocation.x += xDelta;
        this.areaLocation.y += yDelta;
        this.areaLocation.z = zNew;
        this.invalid = true;
    }

    protected void refreshColumns() {
        for (int i = 0; i < this.sizeXY; i++) {
            for (int j = 0; j < this.sizeXY; j++) {
                Column col = this.area[i][j];
                boolean clipped = isClipped(i, j);

                if ((col != null) || (!clipped)) {
                    if (col == null) {
                        col = new Column(this.areaLocation.x + i, this.areaLocation.y + j);
                        activeColumns += 1;

                        attachChild(col);
                        this.area[i][j] = col;
                    } else if (clipped) {
                        col.markForRelease();
                        this.area[i][j] = null;
                        activeColumns -= 1;
                        continue;
                    }

                    col.setLocalTranslation(i * 32, 0.0F, j * 32);

                    col.refreshLeaves(i, j);
                }
            }
        }
    }

    public void rebuildGeometry() {
        for (int i = 0; i < this.sizeXY; i++) {
            for (int j = 0; j < this.sizeXY; j++) {
                Column col = this.area[i][j];
                if (col != null) {
                    col.refreshExisting(i, j);
                }
            }
        }
    }

    protected int priority(int i, int j, int k, LeafReference ref) {
        if (this.priority == null)
            return 1;
        i -= this.xCenter;
        j -= this.yCenter;
        k -= this.areaLocation.z;

        return this.priority.getPriority(i, j, k, getLocalTranslation(), ref);
    }

    public int getBlockType(float x, float y, float z) {
        return getBlockType((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
    }

    public int getBlockType(int x, int y, int z) {
        return this.worldDb.getCellType(x, y, z);
    }

    public int getSunlight(int x, int y, int z) {
        return this.worldDb.getLight(0, x, y, z);
    }

    public int getLocalLight(int x, int y, int z) {
        return this.worldDb.getLight(1, x, y, z);
    }

    public int getCenterSunlightValue() {
        return getSunlight((int) Math.floor(this.worldLocation.x), (int) Math.floor(this.worldLocation.y), (int) Math.floor(this.worldLocation.z));
    }

    public int getCenterLocalLightValue() {
        return getLocalLight((int) Math.floor(this.worldLocation.x), (int) Math.floor(this.worldLocation.y), (int) Math.floor(this.worldLocation.z));
    }

    public int getCenterType() {
        return getBlockType((int) Math.floor(this.worldLocation.x), (int) Math.floor(this.worldLocation.y), (int) Math.floor(this.worldLocation.z));
    }

    public int getCenterType(float xOffset, float yOffset, float zOffset) {
        return getBlockType((int) Math.floor(this.worldLocation.x + xOffset), (int) Math.floor(this.worldLocation.y + yOffset), (int) Math.floor(this.worldLocation.z + zOffset));
    }

    public LeafData getCenter() {
        Column col = this.area[this.xCenter][this.yCenter];
        if (col == null) {
            return null;
        }
        LeafReference ref = col.leaves[this.areaLocation.z];
        LeafData leaf = ref != null ? ref.getLeaf() : null;
        return leaf;
    }

    protected LeafReference getCenterReference() {
        Column col = this.area[this.xCenter][this.yCenter];
        if (col == null) {
            return null;
        }
        LeafReference ref = col.leaves[this.areaLocation.z];
        return ref;
    }

    protected LeafReference getReference(int x, int y, int z) {
        x = Coordinates.worldToLeaf(x) - this.areaLocation.x;
        y = Coordinates.worldToLeaf(y) - this.areaLocation.y;
        z = Coordinates.worldToLeaf(z);

        if ((x < 0) || (y < 0) || (z < 0))
            return null;
        if ((x >= this.sizeXY) || (y >= this.sizeXY) || (z >= this.sizeZ)) {
            return null;
        }
        Column col = this.area[x][y];
        return col.leaves[z];
    }

    public int setBlockType(float x, float y, float z, int type) {
        return setBlockType((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z), type);
    }

    public int setBlockType(int x, int y, int z, int type) {
        LeafReference ref = getReference(x, y, z);
        if (ref == null) {
            System.out.println("**** LeafReference not found for:" + x + ", " + y + ", " + z);
            return -1;
        }

        LeafData leaf = ref.getLeaf();
        if (leaf == null) {
            System.out.println("**** LeafReference doesn't have a leaf:" + leaf);
            return -1;
        }

        int old = this.worldDb.setCellType(x, y, z, type, leaf);
        return old;
    }

    protected void reloadLeaf(int x, int y, int z, int priority) {
        LeafReference ref = getReference(x, y, z);
        if (ref == null) {
            return;
        }
        this.builder.build(priority, ref);
    }

    protected Quaternion worldToCamera(Quaternion q) {
        return Coordinates.flipAxes(q);
    }

    protected void cleanupMob(Mob e) {
        this.mobColumns.remove(e);
        Node n = (Node) this.mobNodes.remove(e);
        if (n != null) {
            n.removeFromParent();
        }
    }

    protected Node getMobNode(Mob e) {
        return this.mobNodes.get(e);
    }

    protected void updateMobs() {
        long time = this.client.getTime(GameClient.TimeType.RENDER);

        for (Map.Entry entry : this.mobColumns.entrySet()) {
            Mob e = (Mob) entry.getKey();

            if (!e.isAlive()) {
                this.mobColumns.remove(e);
                Node n = (Node) this.mobNodes.remove(e);
                if (n != null) {
                    n.removeFromParent();
                }
            }
            Column col = (Column) entry.getValue();

            FrameTransition ft = e.getFrame(time);
            if (ft == null) {
                System.out.println("**** Mob:" + e + "  says it has no transitions, timebuffer:" + e.getTimeBuffer());
            } else {
                if (time < ft.getStartTime()) {
                    System.out.println("Underrun.");
                } else if (time <= ft.getEndTime()) ;
                Vector3f pos = ft.getPosition(time, true);
                Quaternion rot = ft.getRotation(time, true);

                Column colNew = getWorldColumn(pos.x, pos.y);
                Node n = this.mobNodes.get(e);

                if ((col == this.noColumn) && (colNew == null)) {
                    this.mobColumns.remove(e);
                    this.mobNodes.remove(e);
                } else {
                    if (col != colNew) {
                        if (col == this.noColumn) {
                            if (n == null) {
                                n = new Avatar(MainStart.globalAssetManager, e.getName());
                                ((Avatar) n).setEntityId(e.getEntityId());
                                this.mobNodes.put(e, n);
                            }

                        } else {
                            col.mobRemoved(e);
                            this.mobColumns.remove(e);
                        }

                        if (colNew == null) {
                            cleanupMob(e);
                        } else {
                            colNew.mobAdded(e);

                            this.mobColumns.put(e, colNew);
                        }
                    }

                    if (colNew != null) {
                        colNew.adjustToLocal(e, pos, rot);

                        if ((n instanceof Avatar)) {
                            if (((Avatar) n).getName() == null)
                                ((Avatar) n).setName(e.getName());
                        }
                    }
                }
            }
        }
    }

    protected void addChangedMob(Mob e) {
        if (!this.mobColumns.containsKey(e)) {
            this.mobColumns.put(e, this.noColumn);
        }
    }

    protected void activateObject(Entity e) {
        this.ropes.activate(e);
    }

    protected void deactivateObject(Entity e) {
        this.ropes.deactivate(e);
    }

    private class MemReporter implements Reporter {
        private MemReporter() {
        }

        public void printReport(String type, PrintWriter out) {
            out.println("LocalArea->Mob Column cache:" + LocalArea.this.mobColumns.size());
            out.println("LocalArea->Mob Node cache:" + LocalArea.this.mobNodes.size());

            int mobs = 0;
            int eNodes = 0;

            for (int i = 0; i < LocalArea.this.sizeXY; i++) {
                for (int j = 0; j < LocalArea.this.sizeXY; j++) {
                    LocalArea.Column col = LocalArea.this.area[i][j];
                    if (col != null) {
                        mobs += col.getMobChildrenSize();
                        eNodes += col.getEntityNodesSize();
                    }
                }
            }
            out.println("LocalArea->Mob children:" + mobs);
            out.println("LocalArea->Entity node children:" + eNodes);
        }
    }

    public class Column extends Node {
        LeafReference[] leaves = new LeafReference[LocalArea.this.sizeZ];
        int x;
        int y;
        boolean clipped = false;

        Map<Mob, Node> mobChildren = new HashMap<>();
        Node containedEntities;

        public Column() {
            super();
            this.x = -1;
            this.y = -1;
        }

        public Column(int x, int y) {
            super();
            this.x = x;
            this.y = y;

            this.containedEntities = new Node("entities:" + x + "x" + y);
            attachChild(this.containedEntities);
            //TODO: fix
            this.containedEntities.addControl(new EntityContainer(null, x, y));
            this.containedEntities.addControl(new DebugEntityContainer(null, x, y));
            this.containedEntities.addControl(new DebugContactEntityContainer(null, x, y));
        }

        protected int getMobChildrenSize() {
            return this.mobChildren.size();
        }

        protected int getEntityNodesSize() {
            return ((LocalArea.EntityContainer) this.containedEntities.getControl(LocalArea.EntityContainer.class)).getChildCount();
        }

        public int collideWith(Collidable other, CollisionResults results) {
            int total = 0;
            total += this.containedEntities.collideWith(other, results);
            for (Node mob : this.mobChildren.values())
                total += mob.collideWith(other, results);
            return total;
        }

        public void relightObjects() {
            ((LocalArea.EntityContainer) this.containedEntities.getControl(LocalArea.EntityContainer.class)).relight();
        }

        public void mobAdded(Mob e) {
            Node n = LocalArea.this.getMobNode(e);
            if (n == null) {
                return;
            }
            this.mobChildren.put(e, n);

            attachChild(n);
        }

        public void mobRemoved(Mob e) {
            Node n = (Node) this.mobChildren.remove(e);
            n.removeFromParent();
        }

        public void adjustToLocal(Mob e, Vector3f pos, Quaternion rot) {
            Node n = LocalArea.this.getMobNode(e);
            if (n == null) {
                System.out.println("Error: mob is missing a node:" + e);
                return;
            }

            int xBase = Coordinates.leafToWorld(this.x);
            int yBase = Coordinates.leafToWorld(this.y);
            int zBase = 0;

            float x = pos.x - xBase;
            float y = pos.z - zBase;
            float z = pos.y - yBase;

            Vector3f current = n.getLocalTranslation();
            if ((current.x != x) || (current.y != y) || (current.z != z)) {
                n.setLocalTranslation(x, y, z);

                if ((n instanceof Avatar)) {
                    int i = (int) Math.floor(pos.x);
                    int j = (int) Math.floor(pos.y);
                    int k = (int) Math.floor(pos.z);
                    int sunLevel = LocalArea.this.getSunlight(i, j, k);
                    int lightLevel = LocalArea.this.getLocalLight(i, j, k);
                    ((Avatar) n).setLighting(sunLevel / 15.0F, lightLevel / 15.0F);
                }
            }

            if ((n instanceof Avatar)) {
                ((Avatar) n).setFacing(rot);
            }
        }

        public void cancelLoads() {
            for (int k = 0; k < LocalArea.this.sizeZ; k++) {
                LeafReference ref = this.leaves[k];
                if (ref != null) {
                    LocalArea.this.builder.cancel(ref);
                }
            }
        }

        public void markForRelease() {
            LocalArea.this.toRelease.add(this);
            setCullHint(Spatial.CullHint.Always);
            cancelLoads();

            LocalArea.this.mobColumns.keySet().removeAll(this.mobChildren.keySet());
            LocalArea.this.mobNodes.keySet().removeAll(this.mobChildren.keySet());
        }

        public void releaseColumn() {
            LocalArea.this.detachChild(this);

            for (LeafReference ref : this.leaves) {
                if (ref != null) {
                    int priority = 10000000;
                    LocalArea.this.builder.build(priority, new ReleaseReference(ref));
                }
            }
        }

        protected boolean setClipped(boolean clip) {
            if (this.clipped == clip) {
                return false;
            }
            this.clipped = clip;

            if (this.clipped) {
                cancelLoads();

                setCullHint(Spatial.CullHint.Always);
            } else {
                setCullHint(Spatial.CullHint.Dynamic);
            }

            return this.clipped;
        }

        protected void setShowObjects(boolean b) {
            ((LocalArea.EntityContainer) this.containedEntities.getControl(LocalArea.EntityContainer.class)).setEnabled(b);
            if (LocalArea.this.physicsDebug) {
                ((LocalArea.DebugEntityContainer) this.containedEntities.getControl(LocalArea.DebugEntityContainer.class)).setEnabled(b);
                ((LocalArea.DebugContactEntityContainer) this.containedEntities.getControl(LocalArea.DebugContactEntityContainer.class)).setEnabled(b);
            }
        }

        protected void setPhysicsDebug(boolean b) {
            b = (b) && (((LocalArea.EntityContainer) this.containedEntities.getControl(LocalArea.EntityContainer.class)).isEnabled());
            ((LocalArea.DebugEntityContainer) this.containedEntities.getControl(LocalArea.DebugEntityContainer.class)).setEnabled(b);
            ((LocalArea.DebugContactEntityContainer) this.containedEntities.getControl(LocalArea.DebugContactEntityContainer.class)).setEnabled(b);
        }

        public void refreshExisting(int i, int j) {
            for (int k = 0; k < LocalArea.this.sizeZ; k++) {
                LeafReference ref = this.leaves[k];
                if (ref != null) {
                    int priority = LocalArea.this.priority(i, j, k, ref);
                    LocalArea.this.builder.build(priority, ref);
                }
            }
        }

        public void refreshLeaves(int i, int j) {
            setClipped(LocalArea.this.isClipped(i, j));

            setShowObjects(!LocalArea.this.isObjectsClipped(i, j, 0));

            if (this.clipped) {
                return;
            }
            for (int k = 0; k < LocalArea.this.sizeZ; k++) {
                LeafReference ref = this.leaves[k];
                boolean changed = false;

                if (ref == null) {
                    leaves[k] = ref = new LeafReference(this, world, areaLocation.x + i, areaLocation.y + j, k);
                    ref.setNodePosition(0.0F, k * 32, 0.0F);
                    changed = true;
                }

                changed |= ref.needsUpdate();
                changed |= ref.adjustVisibility(i, j, k);

                if (changed) {
                    int priority = LocalArea.this.priority(i, j, k, ref);
                    LocalArea.this.builder.build(priority, ref);
                }
            }
        }
    }

    public class DebugContactEntityContainer extends AbstractEntityContainer<Node> {
        public DebugContactEntityContainer(EntityData ed, int x, int y) {
            super(ed, x, y, new Class[]{ContactDebug.class});
        }

        protected Node createSpatial(Entity e) {
            ContactDebug c = (ContactDebug) e.get(ContactDebug.class);

            Vector3f pos = c.getContactPoint();
            Vector3f dir = c.getContactNormal();
            float pen = (float) c.getPenetration();

            Arrow mesh = new Arrow(dir);
            Geometry geom = new Geometry("debug arrow:" + e.getId(), mesh);

            Node result = new Node("debug contact:" + e.getId());
            result.attachChild(geom);
            result.setLocalTranslation(pos);
            result.setUserData("id", e.getId().getId());

            Material mat = MaterialIndex.DEBUG_MATERIAL.clone();
            mat.getAdditionalRenderState().setDepthTest(false);
            result.setMaterial(mat);
            result.setQueueBucket(RenderQueue.Bucket.Translucent);

            if (c.isActivated())
                result.setCullHint(Spatial.CullHint.Dynamic);
            else
                result.setCullHint(Spatial.CullHint.Always);
            return result;
        }

        protected Node adjustToLocal(Entity e) {
            Node n = getChild(e, true);

            ContactDebug c = e.get(ContactDebug.class);

            if (c.isActivated())
                n.setCullHint(Spatial.CullHint.Dynamic);
            else {
                n.setCullHint(Spatial.CullHint.Always);
            }
            Vector3f loc = c.getContactPoint();

            Geometry geom = (Geometry) n.getChild(0);
            Arrow mesh = (Arrow) geom.getMesh();
            Vector3f dir = c.getContactNormal();
            mesh.setArrowExtent(dir);

            float x = loc.x - this.xBase;
            float y = loc.y - this.zBase;
            float z = loc.z - this.yBase;

            n.setLocalTranslation(x, y, z);
            return n;
        }

        protected void removeChild(Entity e, Node node) {
            if (node == null)
                return;
            if (node.getParent() == this.spatial) {
                ((Node) this.spatial).detachChild(node);
            }
        }
    }

    public class DebugEntityContainer extends AbstractEntityContainer<Node> {
        public DebugEntityContainer(EntityData ed, int x, int y) {
            super(ed, x, y, new Class[]{Position.class, ModelInfo.class, PhysicsDebug.class});
        }

        protected Node createSpatial(Entity e) {
            ModelInfo mi = (ModelInfo) e.get(ModelInfo.class);
            System.out.println("Creating debug box for:" + mi);

            BlueprintData bpData = LocalArea.this.world.getBlueprint(mi.getBlueprintId());

            float x = bpData.xSize * 0.5F * bpData.scale;
            float y = bpData.ySize * 0.5F * bpData.scale;
            float z = bpData.zSize * 0.5F * bpData.scale;
            WireBox box = new WireBox(x, z, y);
            Geometry geom = new Geometry("debug box:" + e.getId(), box);

            geom.setLocalTranslation(0.0F, z, 0.0F);

            Node result = new Node("debug:" + e.getId());
            result.attachChild(geom);
            result.setUserData("id", e.getId().getId());

            Material mat = MaterialIndex.DEBUG_MATERIAL.clone();
            mat.getAdditionalRenderState().setDepthTest(false);
            result.setMaterial(mat);
            result.setQueueBucket(RenderQueue.Bucket.Translucent);

            return result;
        }

        protected ColorRGBA getColor(PhysicsDebug debug) {
            switch (debug.getState().ordinal())
            {
                case 1:
                    return ColorRGBA.Gray;
                case 2:
                    return ColorRGBA.Blue;
                case 3:
                    return ColorRGBA.Red;
                case 4:
                    break;
                case 5:
                default:
                    return ColorRGBA.White;
            }

            double t = debug.getTemperature();
            if (t < 1.0D)
                System.out.println("t:" + t);
            float r = (float) t;
            float g = (float) Math.abs(0.5D - t) * 2.0F;

            return new ColorRGBA(r, g, 0.0F, 1.0F);
        }

        protected Node adjustToLocal(Entity e) {
            Node n = (Node) getChild(e, true);

            Position pos = (Position) e.get(Position.class);
            Vector3f loc = pos.getLocation();

            float x = loc.x - this.xBase;
            float y = loc.z - this.zBase;
            float z = loc.y - this.yBase;

            n.setLocalTranslation(x, y, z);
            n.setLocalRotation(pos.getRotation());

            Material mat = ((Geometry) n.getChild(0)).getMaterial();

            PhysicsDebug debug = (PhysicsDebug) e.get(PhysicsDebug.class);
            if (debug != null) {
                mat.setColor("Color", getColor(debug));
            }

            return n;
        }

        protected void removeChild(Entity e, Node child) {
            if (child == null) {
                return;
            }
            if (child.getParent() == this.spatial) {
                ((Node) this.spatial).detachChild(child);
            }
        }
    }

    public class EntityContainer extends AbstractEntityContainer<BlockObject> {
        public EntityContainer(EntityData ed, int x, int y) {
            super(ed, x, y, new Class[]{Position.class, ModelInfo.class});
        }

        protected void initialize() {
            super.initialize();
            System.out.println("EntityContainer:" + this.entities.debugId());
            this.entities.debugOn = true;
        }

        protected BlockObject createSpatial(Entity e) {
            ModelInfo mi = e.get(ModelInfo.class);

            BlockObject bo = new BlockObject(LocalArea.this.world, mi.getBlueprintId());
            Node n = bo.getNode();
            n.setUserData("id", e.getId().getId());

            if ((LocalArea.log.isDebugEnabled()) && (bo.isBuilt())) {
                LocalArea.log.debug("Node:" + n + " was already built");
            }

            LocalArea.this.objectBuilder.build(0, bo);

            LocalArea.this.activateObject(e);

            return bo;
        }

        protected Node adjustToLocal(Entity e) {
            BlockObject bo = getChild(e, true);

            ModelInfo mi = e.get(ModelInfo.class);

            if (mi.getBlueprintId() != bo.getBlueprintId()) {
                bo.setBlueprintId(mi.getBlueprintId());
                Node n = bo.getNode();
                n.setUserData("id", e.getId().getId());
                if (!bo.isBuilt()) {
                    ((Node) this.spatial).attachChild(n);
                    LocalArea.this.objectBuilder.build(0, bo);
                }
            }

            Node n = bo.getNode();

            Position pos = (Position) e.get(Position.class);
            Vector3f loc = pos.getLocation();

            float x = loc.x - this.xBase;
            float y = loc.z - this.zBase;
            float z = loc.y - this.yBase;

            n.setLocalTranslation(x, y, z);
            n.setLocalRotation(pos.getRotation());

            float zCenter = bo.getSizeZ() * 0.5F * bo.getScale();
            int i = Coordinates.worldToCell(loc.x);
            int j = Coordinates.worldToCell(loc.y);
            int k = Coordinates.worldToCell(loc.z + zCenter);
            int localLight = LocalArea.this.getLocalLight(i, j, k);
            int sunlight = LocalArea.this.getSunlight(i, j, k);

            bo.setLighting(sunlight / 15.0F, localLight / 15.0F);

            return n;
        }

        protected void removeChild(Entity e, BlockObject child) {
            Node n = child.getNode();
            if (n == null) {
                LocalArea.this.deactivateObject(e);
                return;
            }

            if (n.getParent() == this.spatial) {
                ((Node) this.spatial).detachChild(n);
                LocalArea.this.deactivateObject(e);
            }
        }

        public void relight() {
            updateAll();
        }
    }

    protected class MobObserver
            implements MobChangeListener {
        protected MobObserver() {
        }

        public void mobChanged(Mob e, Vector3f newPos, Quaternion rot) {
            if ((MobClass.PLAYER.equals(e.getType())) && (e.getId() == LocalArea.this.client.getPlayer().getId())) {
                return;
            }
            LocalArea.this.addChangedMob(e);
        }
    }

    protected class LeafObserver
            implements LeafChangeListener {
        protected LeafObserver() {
        }

        public void leafChanged(LeafChangeEvent event) {
            LeafData leaf = event.getLeaf();

            if (event.getType() == LeafChangeEvent.ChangeType.CREATED) {
                return;
            }

            LocalArea.Column col = LocalArea.this.getWorldColumn(leaf.getX(), leaf.getY());
            if (col == null) {
                return;
            }
            LeafReference ref = LocalArea.this.getReference(leaf.getX(), leaf.getY(), leaf.getZ());

            if (ref == null) {
                return;
            }

            LocalArea.this.relight.add(col);

            if (ref == LocalArea.this.getCenterReference()) {
                LocalArea.this.builder.build(-2, ref);
            } else {
                LocalArea.this.builder.build(-1, ref);
            }
        }
    }
}