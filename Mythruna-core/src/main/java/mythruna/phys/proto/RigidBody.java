package mythruna.phys.proto;

import mythruna.db.BlueprintData;
import mythruna.es.EntityId;
import mythruna.mathd.Matrix3d;
import mythruna.mathd.Matrix4d;
import mythruna.mathd.Quatd;
import mythruna.mathd.Vec3d;

public class RigidBody {

    public static final double SLEEP_EPSILON = 0.11D;
    private EntityId entityId;
    public PhysicsDebug.PhysicsState debugState = null;
    private BlueprintData mesh;
    private double maxRadius;
    private double volume;
    public Vec3d cog = new Vec3d();
    private double inverseMass;
    private Matrix3d inverseInertiaTensor = new Matrix3d();

    private Vec3d position = new Vec3d();
    private Vec3d velocity = new Vec3d();
    private Vec3d acceleration = new Vec3d();
    private Vec3d rotation = new Vec3d();
    private Quatd orientation = new Quatd();
    private double linearDamping;
    private double angularDamping;
    private Vec3d forces = new Vec3d();
    private Vec3d torques = new Vec3d();

    private Matrix3d inverseInertiaTensorWorld = new Matrix3d();
    private double motion;
    private Vec3d lastFrameAcceleration = new Vec3d();
    private boolean isAwake;
    private boolean canSleep;
    private Matrix4d transform = new Matrix4d();

    private double lastMotion = 0.0D;
    private double motionDelta = 0.0D;

    public RigidBody(EntityId entityId) {
        this.entityId = entityId;
    }

    public EntityId getEntityId() {
        return this.entityId;
    }

    public void setMesh(BlueprintData bp) {
        this.mesh = bp;

        double x = bp.xSize * 0.5D;
        double y = bp.ySize * 0.5D;
        double z = bp.zSize * 0.5D;

        double d = x * x + y * y + z * z;
        this.maxRadius = (Math.sqrt(d) * bp.scale);

        this.volume = bp.scale * bp.xSize * (bp.scale * bp.ySize) * (bp.scale * bp.zSize);
    }

    public BlueprintData getMesh() {
        return this.mesh;
    }

    public double getMaxRadius() {
        return this.maxRadius;
    }

    public double getVolume() {
        return this.volume;
    }

    private static void transformInertiaTensor(Matrix3d iitWorld, Matrix3d iitBody, Matrix4d rotmat) {
        double t4 = rotmat.m00 * iitBody.m00 + rotmat.m01 * iitBody.m10 + rotmat.m02 * iitBody.m20;

        double t9 = rotmat.m00 * iitBody.m01 + rotmat.m01 * iitBody.m11 + rotmat.m02 * iitBody.m21;

        double t14 = rotmat.m00 * iitBody.m02 + rotmat.m01 * iitBody.m12 + rotmat.m02 * iitBody.m22;

        double t28 = rotmat.m10 * iitBody.m00 + rotmat.m11 * iitBody.m10 + rotmat.m12 * iitBody.m20;

        double t33 = rotmat.m10 * iitBody.m01 + rotmat.m11 * iitBody.m11 + rotmat.m12 * iitBody.m21;

        double t38 = rotmat.m10 * iitBody.m02 + rotmat.m11 * iitBody.m12 + rotmat.m12 * iitBody.m22;

        double t52 = rotmat.m20 * iitBody.m00 + rotmat.m21 * iitBody.m10 + rotmat.m22 * iitBody.m20;

        double t57 = rotmat.m20 * iitBody.m01 + rotmat.m21 * iitBody.m11 + rotmat.m22 * iitBody.m21;

        double t62 = rotmat.m20 * iitBody.m02 + rotmat.m21 * iitBody.m12 + rotmat.m22 * iitBody.m22;

        iitWorld.m00 = (t4 * rotmat.m00 + t9 * rotmat.m01 + t14 * rotmat.m02);

        iitWorld.m01 = (t4 * rotmat.m10 + t9 * rotmat.m11 + t14 * rotmat.m12);

        iitWorld.m02 = (t4 * rotmat.m20 + t9 * rotmat.m21 + t14 * rotmat.m22);

        iitWorld.m10 = (t28 * rotmat.m00 + t33 * rotmat.m01 + t38 * rotmat.m02);

        iitWorld.m11 = (t28 * rotmat.m10 + t33 * rotmat.m11 + t38 * rotmat.m12);

        iitWorld.m12 = (t28 * rotmat.m20 + t33 * rotmat.m21 + t38 * rotmat.m22);

        iitWorld.m20 = (t52 * rotmat.m00 + t57 * rotmat.m01 + t62 * rotmat.m02);

        iitWorld.m21 = (t52 * rotmat.m10 + t57 * rotmat.m11 + t62 * rotmat.m12);

        iitWorld.m22 = (t52 * rotmat.m20 + t57 * rotmat.m21 + t62 * rotmat.m22);
    }

    public static void addScaledVector(Vec3d target, Vec3d v, double scale) {
        target.x += v.x * scale;
        target.y += v.y * scale;
        target.z += v.z * scale;
    }

    public static void addScaledVector(Quatd quat, Vec3d v, double scale) {
        Quatd q = new Quatd(v.x * scale, v.y * scale, v.z * scale, 0.0D);
        q.multLocal(quat);

        double x = quat.x + q.x * 0.5D;
        double y = quat.y + q.y * 0.5D;
        double z = quat.z + q.z * 0.5D;
        double w = quat.w + q.w * 0.5D;
        quat.set(x, y, z, w);
    }

    public void calculateDerivedData() {
        this.orientation.normalizeLocal();
        this.transform.setTransform(this.position, this.orientation.toRotationMatrix());

        transformInertiaTensor(this.inverseInertiaTensorWorld, this.inverseInertiaTensor, this.transform);
    }

    public void integrate(double t) {
        if (!this.isAwake) {
            return;
        }

        if (!isStatic()) {
            this.lastFrameAcceleration.set(this.acceleration);
            addScaledVector(this.lastFrameAcceleration, this.forces, this.inverseMass);

            Vec3d angAcc = this.inverseInertiaTensorWorld.mult(this.torques);
            if ((angAcc.x != 0.0D) && (angAcc.y != 0.0D) && (angAcc.z != 0.0D)) {
                System.out.println("angAcc:" + angAcc);
            }

            addScaledVector(this.velocity, this.lastFrameAcceleration, t);
            addScaledVector(this.rotation, angAcc, t);

            this.velocity.multLocal(Math.pow(this.linearDamping, t));
            this.rotation.multLocal(Math.pow(this.angularDamping, t));

            double vLimit = 15.0D;
            double vLen = this.velocity.length();
            if (vLen > vLimit) {
                this.velocity.multLocal(1.0D / vLen * vLimit);
            }

            addScaledVector(this.position, this.velocity, t);
            addScaledVector(this.orientation, this.rotation, t);
        }

        calculateDerivedData();

        clearAccumulators();

        if (this.canSleep) {
            double currentMotion = this.velocity.lengthSq() + this.rotation.lengthSq();

            double bias = Math.pow(0.5D, t);
            this.motion = (bias * this.motion + (1.0D - bias) * currentMotion);

            double delta = Math.abs(currentMotion - this.lastMotion);
            this.motionDelta = (bias * this.motionDelta + (1.0D - bias) * delta);
            this.lastMotion = currentMotion;

            if ((this.motion < 0.11D) || (this.motionDelta < 0.01D)) {
                System.out.println("asleep");
                setAwake(false);
            } else if (this.motion > 1.1D) {
                this.motion = 1.1D;
            }
        }
    }

    public void setInverseMass(double invMass) {
        this.inverseMass = invMass;
    }

    public double getInverseMass() {
        return this.inverseMass;
    }

    public void setMass(double mass) {
        if (mass == 0.0D)
            this.inverseMass = 0.0D;
        else
            this.inverseMass = (1.0D / mass);
    }

    public double getMass() {
        if (this.inverseMass == 0.0D)
            return (1.0D / 0.0D);
        return 1.0D / this.inverseMass;
    }

    public boolean isStatic() {
        return this.inverseMass == 0.0D;
    }

    public void setInertiaTensor(Matrix3d it) {
        this.inverseInertiaTensor.set(it.invert());
    }

    public Matrix3d getInertiaTensor() {
        return this.inverseInertiaTensor.invert();
    }

    public Matrix3d getInertiaTensorWorld() {
        return this.inverseInertiaTensorWorld.invert();
    }

    public void setInverseInertiaTensor(Matrix3d it) {
        this.inverseInertiaTensor.set(it);
    }

    public Matrix3d getInverseInertiaTensor() {
        return this.inverseInertiaTensor;
    }

    public Matrix3d getInverseInertiaTensorWorld() {
        return this.inverseInertiaTensorWorld;
    }

    public void setDamping(double linear, double angular) {
        this.linearDamping = linear;
        this.angularDamping = angular;
    }

    public void setLinearDamping(double linear) {
        this.linearDamping = linear;
    }

    public void setAngularDamping(double angular) {
        this.angularDamping = angular;
    }

    public double getLinearDamping() {
        return this.linearDamping;
    }

    public double getAngularDamping() {
        return this.angularDamping;
    }

    public void setPosition(Vec3d pos) {
        this.position.set(pos);
    }

    public void setPosition(double x, double y, double z) {
        this.position.set(x, y, z);
    }

    public Vec3d getPosition() {
        return this.position;
    }

    public void setOrientation(Quatd q) {
        this.orientation.set(q);
        this.orientation.normalizeLocal();
    }

    public Quatd getOrientation() {
        return this.orientation;
    }

    public Matrix4d getTransform() {
        return this.transform;
    }

    public Vec3d worldToLocal(Vec3d point) {
        return this.transform.invert().mult(point);
    }

    public Vec3d localToWorld(Vec3d point) {
        return this.transform.mult(point);
    }

    public Vec3d getLocalDirection(Vec3d dir) {
        return this.transform.toRotationMatrix().invert().mult(dir);
    }

    public Vec3d getWorldDirection(Vec3d dir) {
        return this.transform.toRotationMatrix().mult(dir);
    }

    public void setVelocity(Vec3d v) {
        this.velocity.set(v);
    }

    public void setVelocity(double x, double y, double z) {
        this.velocity.set(x, y, z);
    }

    public Vec3d getVelocity() {
        return this.velocity;
    }

    public void addVelocity(Vec3d v) {
        this.velocity.addLocal(v);
    }

    public void setRotation(Vec3d rotation) {
        this.rotation.set(rotation);
    }

    public void setRotation(double x, double y, double z) {
        this.rotation.set(x, y, z);
    }

    public Vec3d getRotation() {
        return this.rotation;
    }

    public void addRotation(Vec3d delta) {
        this.rotation.addLocal(delta);
    }

    public boolean isAwake() {
        return this.isAwake;
    }

    public void setAwake(boolean f) {
        if (this.isAwake == f)
            return;
        this.isAwake = f;
        if (f) {
            this.motion = 0.22D;
            this.motionDelta = 0.1D;
        } else {
            this.velocity.set(0.0D, 0.0D, 0.0D);
            this.rotation.set(0.0D, 0.0D, 0.0D);
        }
    }

    public boolean canSleep() {
        return this.canSleep;
    }

    public void setCanSleep(boolean f) {
        this.canSleep = f;
        if ((!this.canSleep) && (!this.isAwake)) {
            setAwake(true);
        }
    }

    public Vec3d getLastFrameAcceleration() {
        return this.lastFrameAcceleration;
    }

    public void clearAccumulators() {
        this.forces.set(0.0D, 0.0D, 0.0D);
        this.torques.set(0.0D, 0.0D, 0.0D);
    }

    public void addForce(Vec3d f) {
        this.forces.addLocal(f);
        this.isAwake = true;
    }

    public void addForceAtBodyPoint(Vec3d f, Vec3d p) {
        Vec3d pt = localToWorld(p);
        addForceAtPoint(f, pt);
    }

    public void addForceAtPoint(Vec3d f, Vec3d p) {
        Vec3d pt = p.subtract(this.position);

        this.forces.addLocal(f);
        this.torques.addLocal(pt.cross(f));

        this.isAwake = true;
    }

    public void addTorque(Vec3d t) {
        this.torques.addLocal(t);
        this.isAwake = true;
    }

    public void setAcceleration(Vec3d a) {
        this.acceleration.set(a);
    }

    public void setAcceleration(double x, double y, double z) {
        this.acceleration.set(x, y, z);
    }

    public Vec3d getAcceleration() {
        return this.acceleration;
    }
}