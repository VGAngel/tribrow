package mythruna.phys;

import com.jme3.math.Vector3f;
import mythruna.World;
import mythruna.es.*;
import mythruna.mathd.Vec3d;

import java.util.HashMap;
import java.util.Map;

public class ForceSystem {

    private World world;
    private ObservableEntityData ed;
    private PhysicsSystem physics;
    private Map<EntityId, ForceGenerator> generators = new HashMap();
    private ChangeQueue changes;

    public ForceSystem(PhysicsSystem physics, World world, EntityData ed) {
        this.physics = physics;
        this.world = world;
        this.ed = ((ObservableEntityData) ed);
    }

    public void start() {
        this.changes = this.ed.getChangeQueue(new Class[]{Spring.class});
    }

    public void shutdown() {
        this.changes.release();
    }

    protected void updateGenerator(EntityId id, EntityComponent val) {
        System.out.println("************* updateGenerator(" + id + ", " + val + ")");
    }

    protected void addGenerator(EntityId id, EntityComponent val) {
        System.out.println("************* addGenerator(" + id + ", " + val + ")");

        PhysicalLink link = (PhysicalLink) this.ed.getComponent(id, PhysicalLink.class);
        System.out.println(" link:" + link);

        RigidBody source = this.physics.activate(link.getSource());
        boolean hasSource = (source != null) || (link.getSource() == null);
        RigidBody target = this.physics.activate(link.getTarget());
        boolean hasTarget = (target != null) || (link.getTarget() == null);

        if ((hasSource) && (hasTarget))
            createSpring(id, source, target, link, (Spring) val);
    }

    protected void addGenerator(RigidBody b, EntityId id, PhysicalLink link, Spring spring) {
        RigidBody source = this.physics.activate(link.getSource());
        boolean hasSource = (source != null) || (link.getSource() == null);
        RigidBody target = this.physics.activate(link.getTarget());
        boolean hasTarget = (target != null) || (link.getTarget() == null);

        if ((hasSource) && (hasTarget))
            createSpring(id, source, target, link, spring);
    }

    protected void createSpring(EntityId id, RigidBody source, RigidBody target, PhysicalLink link, Spring info) {
        System.out.println("createSpring(" + id + ", " + source + ", " + target + ", " + link + ", " + info + ")");
        SpringGenerator s = new SpringGenerator(source, target, link, info);
        this.generators.put(id, s);
    }

    protected void applyChanges() {
        EntityChange c = null;
        while ((c = (EntityChange) this.changes.poll()) != null) {
            EntityId id = c.getEntityId();
            EntityComponent val = c.getComponent();

            if (this.generators.containsKey(id)) {
                if (val == null) {
                    this.generators.remove(id);
                } else {
                    updateGenerator(id, val);
                }
            } else if (val != null) {
                addGenerator(id, val);
            }
        }
    }

    public void updateForces(double t) {
        applyChanges();

        for (ForceGenerator g : this.generators.values())
            g.updateForce(t);
    }

    public void activateBody(RigidBody b) {
        System.out.println("******* activateBody:" + b);

        FieldFilter filter = new FieldFilter(PhysicalLink.class, "source", b.getId());
        EntitySet set = this.ed.getEntities(filter, new Class[]{PhysicalLink.class, Spring.class});
        try {
            for (Entity e : set) {
                addGenerator(b, e.getId(), (PhysicalLink) e.get(PhysicalLink.class), (Spring) e.get(Spring.class));
            }
        } finally {
            set.release();
        }

        filter = new FieldFilter(PhysicalLink.class, "target", b.getId());
        set = this.ed.getEntities(filter, new Class[]{PhysicalLink.class, Spring.class});
        try {
            for (Entity e : set) {
                addGenerator(b, e.getId(), (PhysicalLink) e.get(PhysicalLink.class), (Spring) e.get(Spring.class));
            }
        } finally {
            set.release();
        }
    }

    public void deactivateBody(RigidBody b) {
        System.out.println("********* deactivateBody:" + b);
    }

    private class SpringGenerator implements ForceGenerator {
        private RigidBody source;
        private RigidBody target;
        private Vec3d sourcePoint;
        private Vec3d targetPoint;
        private double length;
        private double strength;

        public SpringGenerator(RigidBody source, RigidBody target, PhysicalLink link, Spring info) {
            System.out.println("New Spring(" + source + ", " + target + ", " + link + ")");
            this.source = source;
            this.target = target;
            this.length = info.getLength();
            this.strength = info.getStrength();

            Vector3f v = link.getSourceOffset();
            this.sourcePoint = new Vec3d(v.x, v.z, v.y);
            v = link.getTargetOffset();
            this.targetPoint = new Vec3d(v.x, v.z, v.y);
        }

        private Vec3d getSourcePoint() {
            if (this.source == null)
                return this.sourcePoint;
            return this.source.localToWorld(this.sourcePoint);
        }

        private Vec3d getTargetPoint() {
            if (this.target == null)
                return this.targetPoint;
            return this.target.localToWorld(this.targetPoint);
        }

        public void updateForce(double t) {
            Vec3d p1 = getSourcePoint();
            Vec3d p2 = getTargetPoint();

            Vec3d force = p1.subtract(p2);

            double len = force.length();

            if (len - this.length < 0.0D) {
                return;
            }
            double m = (len - this.length) * this.strength;
            if (m == 0.0D) {
                return;
            }

            force.multLocal(m / len);

            if (this.source != null) {
                this.source.addForceAtPoint(force.mult(-1.0D), p1);
                if (this.source.isSleepy())
                    this.source.setTemperature(0.01D);
            }
            if (this.target != null) {
                this.target.addForceAtPoint(force, p2);
                if (this.target.isSleepy())
                    this.target.setTemperature(0.01D);
            }
        }
    }
}