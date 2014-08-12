package mythruna.phys;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import mythruna.World;
import mythruna.es.*;
import mythruna.mathd.Quatd;
import mythruna.mathd.Vec3d;
import mythruna.phys.proto.ContactDebug;
import mythruna.phys.proto.PhysicsDebug;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class PhysicsSystem {
    private World world;
    private EntityData ed;
    private CollisionSystem collisions;
    private ForceSystem forces;
    private long lastUpdate = 0L;

    private double baseGravity = -20.0D;

    private Runner runner = new Runner();
    private long fps = 60L;

    private long physTime = 1000000000L / this.fps;

    private double step = 1.0D / this.fps;
    private long updateDelta = 50000000L;
    private EntitySet entities;
    private Set<Entity> pending = new HashSet();
    private EntitySet impulses;
    private Map<EntityId, RigidBody> bodies = new HashMap();

    private List<Contact> contacts = new ArrayList();

    private ContactResolver resolver = new TandemContactResolver();
    private static final boolean debugOn = true;
    private List<EntityId> debugContacts = new ArrayList();
    private int debugContactsInUse = 0;

    public PhysicsSystem(World world, EntityData ed) {
        this.collisions = this.collisions;
        this.world = world;
        this.ed = ed;

        this.collisions = new CollisionSystem(world, ed);
        this.forces = new ForceSystem(this, world, ed);
    }

    protected void initialize() {
        System.out.println("Initializing physics system...");

        this.collisions.start();
        this.forces.start();

        this.collisions.resetActive(-2, -2, 2, 2);

        EntitySet mobiles = this.ed.getEntities(new Class[]{Position.class, Mass.class, MassProperties.class});
        try {
            mobiles.applyChanges();
            for (Entity e : mobiles) {
                Mass m = (Mass) e.get(Mass.class);
                if (m.getInverseMass() != 0.0D) {
                    e.set(new BodyState());
                }
            }
        } finally {
            mobiles.release();
        }

        this.entities = this.ed.getEntities(new Class[]{Mass.class, MassProperties.class, BodyState.class});
        added(this.entities);

        this.impulses = this.ed.getEntities(new Class[]{MassProperties.class, Impulse.class});
        applyImpulses(this.impulses);
    }

    protected void terminate() {
        this.forces.shutdown();
        this.collisions.shutdown();
    }

    public void start() {
        System.out.println("Starting physics engine.");
        this.runner.start();
    }

    public void shutdown() {
        System.out.println("Shuttong down physics engine.");
        this.runner.close();
    }

    protected void applyImpulses(Set<Entity> set) {
        if (set.isEmpty()) {
            return;
        }
        System.out.println("Current impulses:" + this.impulses);

        for (Entity e : set) {
            RigidBody body = (RigidBody) this.bodies.get(e.getId());
            if (body != null) {
                Impulse imp = (Impulse) e.get(Impulse.class);
                Vector3f v = imp.getVelocity();
                body.addVelocity(v.x, v.y, v.z);

                this.ed.removeComponent(e.getId(), Impulse.class);
            }
        }
    }

    public RigidBody activate(EntityId e) {
        if (e == null) {
            return null;
        }

        RigidBody result = (RigidBody) this.bodies.get(e);
        if (result != null) {
            return result;
        }

        this.ed.setComponent(e, new BodyState(true));

        return null;
    }

    protected void createRigidBody(Entity e, CollisionMesh cm) {
        RigidBody body = (RigidBody) this.bodies.get(e.getId());
        if (body == null) {
            body = new RigidBody(cm, (Mass) e.get(Mass.class), (MassProperties) e.get(MassProperties.class));
            body.setAcceleration(0.0D, this.baseGravity, 0.0D);

            this.bodies.put(e.getId(), body);

            this.forces.activateBody(body);
        }

        this.ed.setComponent(body.getId(), new PhysicsDebug(PhysicsDebug.PhysicsState.AWAKE, body.getTemperature()));
    }

    protected void updateRigidBodyDebug(RigidBody body) {
        this.ed.setComponent(body.getId(), new PhysicsDebug(PhysicsDebug.PhysicsState.AWAKE, body.getTemperature()));
    }

    protected void removeRigidBody(Entity e) {
        RigidBody body = (RigidBody) this.bodies.get(e.getId());

        this.forces.deactivateBody(body);

        this.ed.setComponent(body.getId(), new PhysicsDebug(PhysicsDebug.PhysicsState.SLEEPING, body.getTemperature()));
    }

    protected void added(Set<Entity> set) {
        if (set.isEmpty()) {
            return;
        }
        for (Entity e : set) {
            CollisionMesh cm = this.collisions.getCollisionMesh(e.getId());

            if (cm == null) {
                this.pending.add(e);
            } else {
                this.pending.remove(e);
                createRigidBody(e, cm);
            }
        }
    }

    protected void removed(Set<Entity> set) {
        if (set.isEmpty()) {
            return;
        }
        for (Entity e : set) {
            this.pending.remove(e);
            removeRigidBody(e);
        }
    }

    protected void updated(Set<Entity> set) {
        if (set.isEmpty()) ;
    }

    protected void generateContacts() {
        this.contacts.clear();

        for (RigidBody b : this.bodies.values()) {
            if (!b.isSleepy()) {
                int start = this.contacts.size();
                this.collisions.generateWorldCollisions(b.getCollisionMesh(), this.contacts);
                int end = this.contacts.size();
            }
        }

        this.collisions.generateObjectCollisions(this.contacts);
    }

    protected void test2(RigidBody b, List<Contact> list) {
        Vec3d vDelta1 = new Vec3d();
        Vec3d rotDelta1 = new Vec3d();

        Vec3d linear1 = new Vec3d();
        Vec3d angular1 = new Vec3d();

        Vec3d vMin = new Vec3d();
        Vec3d vMax = new Vec3d();
        Vec3d rotMin = new Vec3d();
        Vec3d rotMax = new Vec3d();

        Vec3d linMin = new Vec3d();
        Vec3d linMax = new Vec3d();
        Vec3d angMin = new Vec3d();
        Vec3d angMax = new Vec3d();

        double maxPen = 0.0D;
        for (Contact c : list) {
            RigidBody b1 = null;
            RigidBody b2 = null;
            double localTemperature = 1.0D;
            if (c.mesh1 != null) {
                b1 = (RigidBody) this.bodies.get(c.mesh1.getId());
                if (b1.getTemperature() < localTemperature)
                    localTemperature = b1.getTemperature();
            }
            if (c.mesh2 != null) {
                b2 = (RigidBody) this.bodies.get(c.mesh2.getId());
                if (b2.getTemperature() < localTemperature)
                    localTemperature = b2.getTemperature();
            }
            c.setBodyData(b1, b2);
            c.calculateInternals(this.step * localTemperature);

            c.calculatePositionChange(linear1, null, angular1, null, c.penetration);
            linMin.minLocal(linear1);
            linMax.maxLocal(linear1);
            angMin.minLocal(angular1);
            angMax.maxLocal(angular1);

            maxPen = Math.max(c.penetration, maxPen);
        }

        Vec3d l = linMin.addLocal(linMax);
        Vec3d a = angMin.addLocal(angMax);

        b.getCollisionMesh().position.addLocal(l);
        b.getCollisionMesh().orientation.addScaledVectorLocal(a, 1.0D);
        b.calculateDerivedData();

        Vec3d deltaPos = new Vec3d();

        for (Contact c : list) {
            c.calculateVelocityChange(vDelta1, null, rotDelta1, null);

            vMin.minLocal(vDelta1);
            vMax.maxLocal(vDelta1);
            rotMin.minLocal(rotDelta1);
            rotMax.maxLocal(rotDelta1);
        }

        Vec3d v = vMin.addLocal(vMax);
        Vec3d r = rotMin.addLocal(rotMax);

        b.addVelocity(v);
        b.addRotation(r);

        list.clear();
    }

    protected void resolveContacts() {
        for (Contact c : this.contacts) {
            RigidBody b1 = null;
            RigidBody b2 = null;
            if (c.mesh1 != null)
                b1 = (RigidBody) this.bodies.get(c.mesh1.getId());
            if (c.mesh2 != null) {
                b2 = (RigidBody) this.bodies.get(c.mesh2.getId());
            }
            c.setBodyData(b1, b2);
        }

        this.resolver.resolveContacts(this.contacts, this.step);
    }

    protected final void publishContacts() {
        int i;
        for (i = 0; i < this.contacts.size(); i++) {
            Contact c = (Contact) this.contacts.get(i);
            EntityId e = null;

            if (i < this.debugContacts.size()) {
                e = (EntityId) this.debugContacts.get(i);
            } else {
                e = this.ed.createEntity();
                this.debugContacts.add(e);
            }

            ContactDebug cb = new ContactDebug(c);

            this.ed.setComponent(e, cb);
        }

        int inUse = i;

        for (; i < this.debugContacts.size(); i++) {
            EntityId e = (EntityId) this.debugContacts.get(i);

            this.ed.setComponent(e, new ContactDebug(false));
        }

        this.debugContactsInUse = inUse;
    }

    protected void step() {
        if (this.collisions.applyChanges()) {
            added(this.pending);
        }

        if (this.entities.applyChanges()) {
            removed(this.entities.getRemovedEntities());
            added(this.entities.getAddedEntities());
            updated(this.entities.getChangedEntities());
        }
        this.impulses.applyChanges();
        applyImpulses(this.impulses);

        this.forces.updateForces(this.step);

        if (this.bodies.isEmpty()) {
            return;
        }

        long time = System.nanoTime();
        long delta = time - this.lastUpdate;
        boolean publish = false;
        if (delta > this.updateDelta) {
            this.lastUpdate = time;
            publish = true;
        }

        for (RigidBody body : this.bodies.values()) {
            if (body.getCollisionMesh().position.y <= 50.0D) {
                this.ed.removeComponent(body.getId(), BodyState.class);
            } else {
                if (!body.isSleepy()) {
                    body.integrate(this.step);
                }
                if (body.isSleepy()) {
                    this.ed.removeComponent(body.getId(), BodyState.class);
                }
            }

        }

        generateContacts();

        if (publish) {
            publishContacts();
        }
        resolveContacts();

        if (publish) {
            for (RigidBody body : this.bodies.values()) {
                Vec3d v = body.getCollisionMesh().getBasePosition();
                Quatd q = body.getCollisionMesh().orientation;
                Position pos = new Position(new Vector3f((float) v.x, (float) v.z, (float) v.y), new Quaternion((float) q.x, (float) q.y, (float) q.z, (float) q.w));

                this.ed.setComponent(body.getId(), pos);

                updateRigidBodyDebug(body);
            }
        }
    }

    private class Runner extends Thread {
        private AtomicBoolean go = new AtomicBoolean(true);

        public Runner() {
            setName("PhysicsThread");
        }

        public void close() {
            this.go.set(false);
            try {
                join();
            } catch (InterruptedException e) {
                throw new RuntimeException("Interrupted while waiting for physic thread to complete.", e);
            }
        }

        public void run() {
            PhysicsSystem.this.initialize();
            long lastTime = System.nanoTime();
            while (this.go.get()) {
                long time = System.nanoTime();
                long delta = time - lastTime;
                if (delta >= PhysicsSystem.this.physTime) {
                    lastTime = time;
                    try {
                        PhysicsSystem.this.step();
                        long end = System.nanoTime();
                        delta = end - time;
                        if (delta > PhysicsSystem.this.physTime)
                            System.out.println("Physics processing time exceeded time step size:" + delta / 1000000.0D + " ms");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        Thread.sleep(1L);
                    } catch (InterruptedException e) {
                        throw new RuntimeException("Interrupted sleeping", e);
                    }
                }
            }
            PhysicsSystem.this.terminate();
        }
    }
}