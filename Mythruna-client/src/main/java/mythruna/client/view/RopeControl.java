package mythruna.client.view;

import com.jme3.math.Transform;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import mythruna.Coordinates;
import mythruna.MaterialIndex;
import mythruna.Vector3i;
import mythruna.es.*;
import mythruna.phys.Mass;
import mythruna.phys.PhysicalLink;
import mythruna.phys.Rope;
import org.progeeks.util.log.Log;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RopeControl extends AbstractControl {
    static Log log = Log.getLog();
    private ObservableEntityData ed;
    private EntitySet entities;
    private RopeMesh ropeMesh;
    private Node ropeNode;
    private Map<EntityId, SimpleBody> simpleBodies = new ConcurrentHashMap<>();
    private Map<EntityId, BodyLink> links = new HashMap<>();
    private ChangeQueue changes;
    private FieldFilter<Position>[] filters;
    private ConcurrentLinkedQueue<BodyLink> newLinks = new ConcurrentLinkedQueue<>();

    private Vector3f worldBase = new Vector3f();

    public RopeControl(ObservableEntityData ed) {
        this.ed = ed;
        super.setEnabled(true);
        this.ropeMesh = new RopeMesh();

        Geometry geom = new Geometry("Ropes", this.ropeMesh);
        geom.setMaterial(MaterialIndex.ROPE_MATERIAL);

        this.ropeNode = new Node("Ropes");
        this.ropeNode.attachChild(geom);
    }

    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        initialize();
    }

    public Control cloneForSpatial(Spatial spatial) {
        throw new UnsupportedOperationException("This control cannot be cloned.");
    }

    protected void setupFilters(Vector3i areaLocation) {
        if (log.isDebugEnabled()) {
            log.debug("RopeControl.setupFilters(" + areaLocation + ")");
        }
        int width = 1;
        int xCenter = areaLocation.x + 6;
        int yCenter = areaLocation.x + 6;

        if (this.filters == null) {
            int filterCount = width * 2 + 1;
            filterCount *= filterCount;
            this.filters = new FieldFilter[filterCount];
        }

        int i = 0;
        for (int x = xCenter - width; x <= xCenter + width; x++) {
            for (int y = yCenter - width; y <= yCenter + width; y++) {
                if (log.isDebugEnabled()) {
                    log.debug("catch[" + x + ", " + y + "]");
                }
                long colId = Coordinates.leafToColumnId(x, y);

                this.filters[(i++)] = FieldFilter.create(Position.class, "columnId", colId);
            }
        }

        ComponentFilter f = OrFilter.create(Position.class, this.filters);

        if (log.isDebugEnabled()) {
            log.debug("Filter:" + f);
        }
        if (this.entities == null) {
            this.entities = this.ed.getEntities(f, new Class[]{Position.class, ModelInfo.class, Mass.class});
            addChildren(this.entities);
        } else if (this.entities.resetFilter(f)) {
            processChanges();
        }
    }

    protected void processChanges() {
        removeChildren(this.entities.getRemovedEntities());
        addChildren(this.entities.getAddedEntities());
        updateChildren(this.entities.getChangedEntities());
    }

    protected void initialize() {
        System.out.println("RopeControl.initialize()");

        this.changes = this.ed.getChangeQueue(new Class[]{Rope.class, PhysicalLink.class});
        ((Node) this.spatial).attachChild(this.ropeNode);
    }

    protected void terminate() {
        ((Node) this.spatial).detachChild(this.ropeNode);
        this.changes.release();

        if (this.entities != null) {
            removeChildren(this.entities);
            this.entities.release();
            this.entities = null;
        }
    }

    public void setLocation(Vector3i areaLocation, int clipDistance) {
        setupFilters(areaLocation);
        setWorldLocation(Coordinates.leafToWorld(areaLocation), clipDistance);
    }

    public void setWorldLocation(Vector3i worldLocation, int clipDistance) {
        System.out.println("RopeControl.setWorldLocation(" + worldLocation + ")");
        float x = 0 - worldLocation.x;
        float y = 0 - worldLocation.y;
        float z = 0.0F;

        this.ropeNode.setLocalTranslation(x, z, y);
        this.worldBase.set(x, z, y);
    }

    public void activate(Entity e) {
    }

    public void deactivate(Entity e) {
    }

    protected void addChildren(Set<Entity> set) {
        for (Entity e : set) {
            this.ed.execute(new LinkLoader(e));
        }
    }

    protected void updateChildren(Set<Entity> set) {
        for (Iterator i$ = set.iterator(); i$.hasNext(); ) {
            Entity e = (Entity) i$.next();

            for (BodyLink l : this.links.values()) {
                l.entityChanged(e);
            }
        }
    }

    protected void removeChildren(Set<Entity> set) {
        Entity e;
        for (Iterator i$ = set.iterator(); i$.hasNext(); e = (Entity) i$.next()) ;
    }

    protected void componentChange(EntityChange c) {
        EntityId id = c.getEntityId();
        EntityComponent comp = c.getComponent();

        if ((comp instanceof Rope)) {
            float x = (float) (Math.random() * 64.0D - 32.0D);
            float y = (float) (Math.random() * 64.0D + 70.0D);
            float z = (float) (Math.random() * 64.0D - 32.0D);
            Vector3f v1 = new Vector3f(x, y, z);
            x = (float) (Math.random() * 64.0D - 32.0D);
            y = (float) (Math.random() * 64.0D + 70.0D);
            z = (float) (Math.random() * 64.0D - 32.0D);
            Vector3f v2 = new Vector3f(x, y, z);

            RopeLink link = new RopeLink(v1, v2);
            System.out.println("Adding rope from:" + v1 + " to:" + v2);
            this.ropeMesh.addRope(link);
        }
    }

    public void setEnabled(boolean b) {
        if (isEnabled() == b)
            return;
        super.setEnabled(b);

        if (b)
            initialize();
        else
            terminate();
    }

    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    protected void controlUpdate(float tpf) {
        if (this.entities != null) {
            long start = System.nanoTime();
            boolean hasChanges = this.entities.applyChanges();
            long end = System.nanoTime();
            if (hasChanges) {
                if (end - start > 2000000L) {
                    System.out.println(getClass().getSimpleName() + " apply changes time:" + (end - start / 1000000.0D) + " ms");
                }

                processChanges();
            }

            while (!this.newLinks.isEmpty()) {
                BodyLink bl = (BodyLink) this.newLinks.poll();
                if (this.links.put(bl.entity.getId(), bl) == null) {
                    System.out.println("Adding new link:" + bl);
                    System.out.println("----- geometry:" + bl.getGeometry());
                    this.ropeMesh.addRope(bl.getGeometry());
                }
            }
        }

        EntityChange c = null;
        while ((c = (EntityChange) this.changes.poll()) != null) {
            componentChange(c);
        }

        for (BodyLink l : this.links.values()) {
            l.update();
        }

        this.ropeMesh.update();
    }

    protected SimpleBody getSimpleBody(EntityId id) {
        if (id == null) {
            return null;
        }

        SimpleBody body = (SimpleBody) this.simpleBodies.get(id);
        if (body == null) {
            Entity e = this.entities.getEntity(id);
            body = new SimpleBody(e);
            this.simpleBodies.put(id, body);
        }

        return body;
    }

    private class LinkLoader
            implements EntityProcessor {
        Entity entity;

        public LinkLoader(Entity e) {
            this.entity = e;
        }

        protected void addLink(EntityId id, EntityData ed) {
            Entity linkEntity = ed.getEntity(id, new Class[]{PhysicalLink.class, Rope.class});
            PhysicalLink link = (PhysicalLink) linkEntity.get(PhysicalLink.class);

            EntityId source = link.getSource();
            EntityId target = link.getTarget();

            RopeControl.SimpleBody b1 = RopeControl.this.getSimpleBody(source);
            RopeControl.SimpleBody b2 = RopeControl.this.getSimpleBody(target);

            RopeControl.BodyLink bl = new RopeControl.BodyLink(linkEntity, b1, b2);

            RopeControl.this.newLinks.add(bl);
        }

        public void run(EntityData ed) {
            ComponentFilter f = FieldFilter.create(PhysicalLink.class, "source", this.entity.getId());
            Set<EntityId> set = ed.findEntities(f, new Class[]{PhysicalLink.class, Rope.class});
            for (EntityId id : set) {
                addLink(id, ed);
            }

            f = FieldFilter.create(PhysicalLink.class, "target", this.entity.getId());
            set = ed.findEntities(f, new Class[]{PhysicalLink.class, Rope.class});
            for (EntityId id : set)
                addLink(id, ed);
        }
    }

    private class SimpleBody {
        private Entity entity;
        private Transform transform;

        public SimpleBody(Entity entity) {
            this.entity = entity;

            this.transform = new Transform();
            resetTransform();
        }

        public void resetTransform() {
            Position pos = (Position) this.entity.get(Position.class);

            Vector3f loc = pos.getLocation();
            this.transform.setTranslation(loc.x, loc.z, loc.y);
            this.transform.setRotation(pos.getRotation());
        }

        public EntityId getId() {
            return this.entity.getId();
        }

        public Vector3f localToWorld(Vector3f local) {
            System.out.println("localToWorld(" + local + ")");
            Vector3f result = new Vector3f(local.x, local.z, local.y);
            result = this.transform.transformVector(result, result);
            System.out.println("result:" + result);
            return result;
        }
    }

    private class BodyLink {
        Entity entity;
        PhysicalLink link;
        Rope ropeInfo;
        RopeControl.SimpleBody b1;
        RopeControl.SimpleBody b2;
        RopeLink ropeGeometry;
        boolean needsUpdate = true;

        public BodyLink(Entity entity, RopeControl.SimpleBody b1, RopeControl.SimpleBody b2) {
            this.entity = entity;
            this.link = ((PhysicalLink) entity.get(PhysicalLink.class));
            this.ropeInfo = ((Rope) entity.get(Rope.class));
            this.b1 = b1;
            this.b2 = b2;
        }

        public RopeLink getGeometry() {
            if (this.ropeGeometry == null)
                this.ropeGeometry = new RopeLink(end1(), end2());
            return this.ropeGeometry;
        }

        protected Vector3f flip(Vector3f v) {
            return new Vector3f(v.x, v.z, v.y);
        }

        protected Vector3f end1() {
            if (this.b1 == null)
                return flip(this.link.getSourceOffset());
            return this.b1.localToWorld(this.link.getSourceOffset());
        }

        protected Vector3f end2() {
            if (this.b2 == null)
                return flip(this.link.getTargetOffset());
            return this.b2.localToWorld(this.link.getTargetOffset());
        }

        public void entityChanged(Entity e) {
            if ((this.b1 != null) && (this.b1.entity == e)) {
                this.b1.resetTransform();
                this.needsUpdate = true;
            }
            if ((this.b2 != null) && (this.b2.entity == e)) {
                this.b2.resetTransform();
                this.needsUpdate = true;
            }
        }

        public void update() {
            if (!this.needsUpdate) {
                return;
            }
            this.needsUpdate = false;
            this.ropeGeometry.update(end1(), end2());
        }
    }
}