package mythruna.phys;

import mythruna.es.EntityId;
import mythruna.mathd.Matrix3d;
import mythruna.mathd.Matrix4d;
import mythruna.mathd.Vec3d;

public class RigidBody {

    public static final double DEFAULT_LINEAR_DAMPING = 0.9D;
    public static final double DEFAULT_ANGULAR_DAMPING = 0.8D;
    private CollisionMesh cm;
    private double inverseMass;
    private Matrix3d inverseInertiaTensor = new Matrix3d();
    private Matrix3d inverseInertiaTensorWorld = new Matrix3d();

    private Vec3d acceleration = new Vec3d();

    private Vec3d velocity = new Vec3d();
    private Vec3d rotation = new Vec3d();

    private Vec3d forces = new Vec3d();
    private Vec3d torques = new Vec3d();

    private double linearDamping = 0.9D;
    private double angularDamping = 0.8D;

    private Matrix4d transform = new Matrix4d();

    public Vec3d lastFrameAcceleration = new Vec3d();

    private double temperature = 1.0D;
    private static final double MOTION_EPSILON = 0.12D;
    private static final double ROTATION_EPSILON = 0.02D;
    private static final double MOTION_SLOW_COOL = 0.31D;
    private static final double ROTATION_SLOW_COOL = 0.1D;
    private static final double COOLING = 0.9D;
    private static final double SLOW_COOLING = 0.99D;
    private static final double HEATING = 1.5D;
    private int contactCount = 0;

    public RigidBody(CollisionMesh cm) {
        this.cm = cm;
    }

    public RigidBody(CollisionMesh cm, Mass mass, MassProperties massProps) {
        this.cm = cm;

        Vec3d pos = cm.position.subtract(cm.cog);

        pos.addLocal(massProps.getCog());

        cm.position.set(pos);
        cm.setCog(massProps.getCog());

        setInverseMass(mass.getInverseMass());
        setInertiaTensor(massProps.getInteria());
        calculateDerivedData();
    }

    public CollisionMesh getCollisionMesh() {
        return this.cm;
    }

    public EntityId getId() {
        return this.cm.getId();
    }

    public void calculateDerivedData() {
        this.cm.orientation.normalizeLocal();
        this.transform.setTransform(this.cm.position, this.cm.orientation.toRotationMatrix());

        transformInertiaTensor(this.inverseInertiaTensorWorld, this.inverseInertiaTensor, this.transform);
    }

    public void clearAccumulators() {
        this.forces.set(0.0D, 0.0D, 0.0D);
        this.torques.set(0.0D, 0.0D, 0.0D);
    }

    public void incrementContactCount() {
        this.contactCount += 1;
    }

    public void integrate(double t) {
        t *= getTemperature();

        this.lastFrameAcceleration.set(this.acceleration);
        Vec3d linearAcc = this.lastFrameAcceleration;
        linearAcc.addScaledVectorLocal(this.forces, this.inverseMass);

        Vec3d angAcc = this.inverseInertiaTensorWorld.mult(this.torques);

        this.velocity.addScaledVectorLocal(linearAcc, t);
        this.rotation.addScaledVectorLocal(angAcc, t);

        this.velocity.multLocal(Math.pow(this.linearDamping, t));
        this.rotation.multLocal(Math.pow(this.angularDamping, t));

        this.cm.position.addScaledVectorLocal(this.velocity, t);
        this.cm.orientation.addScaledVectorLocal(this.rotation, t);

        calculateDerivedData();

        clearAccumulators();

        double velSq = this.velocity.lengthSq();
        double rotSq = this.rotation.lengthSq();

        if ((velSq < 0.12D) && (rotSq < 0.02D)) {
            this.temperature *= 0.9D;
        } else if ((velSq < 0.31D) && (rotSq < 0.1D)) {
            this.temperature *= 0.99D;
        } else if ((velSq > 0.465D) && (rotSq > 0.15D)) {
            this.temperature *= 1.5D;
            if (this.temperature > 1.0D) {
                this.temperature = 1.0D;
            }
        }
        this.contactCount = 0;
    }

    public static boolean isCold(double t) {
        return t < 0.01D;
    }

    public boolean isSleepy() {
        return this.temperature < 0.01D;
    }

    public double getMotion() {
        return this.temperature;
    }

    public void setTemperature(double t) {
        this.temperature = t;
    }

    public double getTemperature() {
        if (this.contactCount > 10) {
            if (this.temperature > 0.25D) {
                return this.temperature * 0.5D;
            }
        }
        return this.temperature;
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

    public void setAcceleration(Vec3d a) {
        this.acceleration.set(a);
    }

    public void setAcceleration(double x, double y, double z) {
        this.acceleration.set(x, y, z);
    }

    public Vec3d getAcceleration() {
        return this.acceleration;
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

    public void addVelocity(double x, double y, double z) {
        this.velocity.addLocal(x, y, z);
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

    public Vec3d localToWorld(Vec3d point) {
        return this.transform.mult(point);
    }

    public void addForce(Vec3d f) {
        this.forces.addLocal(f);
    }

    public void addForceAtBodyPoint(Vec3d f, Vec3d p) {
        Vec3d pt = localToWorld(p);
        addForceAtPoint(f, pt);
    }

    public void addForceAtPoint(Vec3d f, Vec3d p) {
        Vec3d pt = p.subtract(this.cm.position);

        this.forces.addLocal(f);
        this.torques.addLocal(pt.cross(f));
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
}