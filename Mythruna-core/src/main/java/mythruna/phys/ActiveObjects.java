package mythruna.phys;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import mythruna.Coordinates;
import mythruna.Vector3i;
import mythruna.World;
import mythruna.db.BlueprintData;
import mythruna.es.*;
import mythruna.mathd.Vec3d;
import org.progeeks.util.log.Log;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ActiveObjects implements Iterable<CollisionMesh> {

    static Log log = Log.getLog();

    public static final BlueprintData BLOCK = new BlueprintData();
    private World world;
    private EntityData ed;
    private Map<EntityId, CollisionMesh> meshes = new HashMap();

    private Map<CollisionMesh, Integer> usageCounts = new HashMap();

    private Map<Vector3i, ActiveSet> active = new HashMap();

    private ConcurrentLinkedQueue<MeshBuilder> finishedBuilders = new ConcurrentLinkedQueue();

    private Vector3i lastColumn = null;

    public ActiveObjects(World world, EntityData ed) {
        this.world = world;
        this.ed = ed;
    }

    public void close() {
        for (ActiveSet as : this.active.values())
            as.release();
        this.active.clear();
    }

    public Iterator<CollisionMesh> iterator() {
        return this.meshes.values().iterator();
    }

    protected void markUsed(CollisionMesh cm) {
        Integer i = (Integer) this.usageCounts.get(cm);
        if (i == null)
            i = Integer.valueOf(1);
        else
            i = Integer.valueOf(i.intValue() + 1);
        this.usageCounts.put(cm, i);
    }

    protected boolean unmarkUsed(CollisionMesh cm) {
        Integer i = (Integer) this.usageCounts.get(cm);
        if (i == null)
            return true;
        i = Integer.valueOf(i.intValue() - 1);
        if (i.intValue() == 0) {
            this.usageCounts.remove(cm);
            return true;
        }
        this.usageCounts.put(cm, i);
        return false;
    }

    public void resetActive(CollisionMesh cm) {
        Vec3d pos = cm.position;

        int x = Coordinates.worldToLeaf(pos.x);
        int y = Coordinates.worldToLeaf(pos.z);

        if (this.lastColumn == null) {
            this.lastColumn = new Vector3i(x, y, 0);
        } else {
            if ((this.lastColumn.x == x) && (this.lastColumn.y == y)) {
                return;
            }

            this.lastColumn.x = x;
            this.lastColumn.y = y;
        }

        int xMin = this.lastColumn.x - 1;
        int yMin = this.lastColumn.y - 1;
        int xMax = this.lastColumn.x + 1;
        int yMax = this.lastColumn.y + 1;
        resetActive(xMin, yMin, xMax, yMax);
    }

    public void resetActive(int xMin, int yMin, int xMax, int yMax) {
        Set<Vector3i> toRemove = new HashSet(this.active.keySet());

        for (int i = xMin; i <= xMax; i++) {
            for (int j = yMin; j <= yMax; j++) {
                Vector3i v = new Vector3i(i, j, 0);

                ActiveSet set = (ActiveSet) this.active.get(v);
                if (set == null) {
                    set = new ActiveSet(v);
                    this.active.put(v, set);
                    added(set.entities, set);
                } else {
                    toRemove.remove(v);
                }
            }
        }

        for (Vector3i col : toRemove) {
            ActiveSet set = (ActiveSet) this.active.remove(col);
            if (set != null) {
                set.release();
                removed(set.entities, set);
            }
        }
    }

    public boolean applyChanges() {
        boolean changed = false;

        if (!this.finishedBuilders.isEmpty()) {
            MeshBuilder mb = null;
            while ((mb = (MeshBuilder) this.finishedBuilders.poll()) != null) {
                mb.apply();
            }

        }

        for (ActiveSet as : this.active.values()) {
            EntitySet es = as.entities;

            if (es.applyChanges()) {
                changed = true;
                removed(es.getRemovedEntities(), as);
                if (!es.getAddedEntities().isEmpty()) {
                    added(es.getAddedEntities(), as);
                }

                updated(es.getChangedEntities(), as);
            }

        }

        return changed;
    }

    public CollisionMesh getCollisionMesh(EntityId entity) {
        CollisionMesh mesh = (CollisionMesh) this.meshes.get(entity);
        return mesh;
    }

    private final void added(Set<Entity> set, ActiveSet as) {
        if (set.isEmpty()) {
            return;
        }

        for (Entity e : set) {
            CollisionMesh mesh = (CollisionMesh) this.meshes.get(e.getId());
            if (mesh == null) {
                ModelInfo mi = (ModelInfo) e.get(ModelInfo.class);

                BlueprintData bp = this.world.getBlueprint(mi.getBlueprintId(), false);

                if (bp == null) {
                    if (log.isDebugEnabled())
                        log.debug("Queing up background request for blueprint:" + mi.getBlueprintId() + "  entity:" + e.getId());
                    mesh = new CollisionMesh(e.getId(), BLOCK);
                    this.ed.execute(new MeshBuilder(mi.getBlueprintId(), mesh));
                    this.meshes.put(e.getId(), mesh);
                } else {
                    mesh = new CollisionMesh(e.getId(), bp);
                    this.meshes.put(e.getId(), mesh);
                }

            }

            markUsed(mesh);

            update(e, as);
        }
    }

    private final void update(Entity e, ActiveSet as) {
        CollisionMesh mesh = (CollisionMesh) this.meshes.get(e.getId());
        if (mesh == null) {
            throw new RuntimeException("No mesh found for ID:" + e.getId());
        }

        Position pos = (Position) e.get(Position.class);
        Vector3f p = pos.getLocation();
        Quaternion q = pos.getRotation();
        if (p == null) {
            throw new RuntimeException("Position has null location:" + pos);
        }

        mesh.orientation.set(q.getX(), q.getY(), q.getZ(), q.getW());
        mesh.setBasePosition(p.x, p.z, p.y);

        Mass mass = (Mass) e.get(Mass.class);
        mesh.setIsStatic(mass.getInverseMass() == 0.0D);
    }

    private final void updated(Set<Entity> set, ActiveSet as) {
        if (set.isEmpty()) {
            return;
        }

        for (Entity e : set) {
            update(e, as);
        }
    }

    private final void removed(Set<Entity> set, ActiveSet as) {
        if (set.isEmpty()) {
            return;
        }

        for (Entity e : set) {
            CollisionMesh mesh = (CollisionMesh) this.meshes.get(e.getId());

            if (unmarkUsed(mesh)) {
                this.meshes.remove(e.getId());
            }
        }
    }

    static {
        BLOCK.cells = new int[1][1][1];
        BLOCK.cells[0][0][0] = 1;
        BLOCK.id = -42L;
        BLOCK.xSize = 1;
        BLOCK.ySize = 1;
        BLOCK.zSize = 1;
        BLOCK.scale = 2.5F;
    }

    private class MeshBuilder
            implements EntityProcessor {
        long blueprintId;
        CollisionMesh cm;
        BlueprintData data;

        public MeshBuilder(long blueprintId, CollisionMesh mesh) {
            this.blueprintId = blueprintId;
            this.cm = mesh;
        }

        public void apply() {
            if (ActiveObjects.log.isDebugEnabled()) {
                ActiveObjects.log.debug("Applying mesh blueprint for:" + this.blueprintId + "  entity:" + this.cm.getId());
            }
            if (ActiveObjects.this.meshes.containsKey(this.cm.getId()))
                this.cm.setMesh(this.data);
        }

        public void run(EntityData data) {
            if (ActiveObjects.log.isDebugEnabled())
                ActiveObjects.log.debug("Requesting mesh blueprint for:" + this.blueprintId + "  entity:" + this.cm.getId());
            this.data = ActiveObjects.this.world.getBlueprint(this.blueprintId, true);
            ActiveObjects.this.finishedBuilders.add(this);
        }
    }

    private class ActiveSet {
        long colId;
        Vector3i leafPos;
        EntitySet entities;

        public ActiveSet(Vector3i v) {
            this.leafPos = v;
            this.colId = Coordinates.leafToColumnId(v.x, v.y);

            FieldFilter filter = new FieldFilter(Position.class, "columnId", Long.valueOf(this.colId));
            this.entities = ActiveObjects.this.ed.getEntities(filter, new Class[]{Position.class, ModelInfo.class, Mass.class});
        }

        public void release() {
            this.entities.release();
        }
    }
}