package mythruna.phys;

import mythruna.mathd.Matrix3d;
import mythruna.mathd.Quatd;
import mythruna.mathd.Vec3d;

public class Contact {

    public CollisionMesh mesh1;
    public RigidBody body1;
    public CollisionMesh mesh2;
    public RigidBody body2;
    public double friction;
    public double restitution;
    public Vec3d contactPoint;
    public Vec3d contactNormal;
    public double penetration;
    private static final double VELOCITY_LIMIT = 0.25D;
    protected Matrix3d contactToWorld = new Matrix3d();
    protected Vec3d contactVelocity;
    protected double desiredDeltaVelocity;
    protected Vec3d relativeContactPosition1;
    protected Vec3d relativeContactPosition2;
    protected double localTemperature;

    public Contact() {
    }

    public void setMeshData(CollisionMesh b1, CollisionMesh b2, double friction, double restitution) {
        this.mesh1 = b1;
        this.mesh2 = b2;
        this.friction = friction;
        this.restitution = restitution;
    }

    public void setBodyData(RigidBody b1, RigidBody b2) {
        this.body1 = b1;
        this.body2 = b2;
    }

    public String toString() {
        return "Contact[ cp:" + this.contactPoint + " normal:" + this.contactNormal + " p:" + this.penetration + " b1:" + this.body1 + ", b2:" + this.body2 + "]";
    }

    protected void calculateInternals(double t) {
        if (this.body1 == null) {
            swapBodies();
        }
        calculateContactBasis();

        this.relativeContactPosition1 = this.contactPoint.subtract(this.mesh1.position);
        if (this.body2 != null) {
            this.relativeContactPosition2 = this.contactPoint.subtract(this.mesh2.position);
        }
        this.contactVelocity = calculateLocalVelocity(0, t);
        if (this.body2 != null) {
            this.contactVelocity.subtractLocal(calculateLocalVelocity(1, t));
        }
        calculateDesiredDeltaVelocity(t);
    }

    protected void swapBodies() {
        this.contactNormal.multLocal(-1.0D);
        RigidBody temp = this.body1;
        this.body1 = this.body2;
        this.body2 = temp;

        this.mesh1 = this.body1.getCollisionMesh();
        this.mesh2 = (this.body2 == null ? null : this.body2.getCollisionMesh());
    }

    protected void matchAwakeState() {
        this.localTemperature = 0.001D;
        if (this.body1 != null) {
            this.localTemperature = Math.max(this.localTemperature, this.body1.getTemperature());
        }
        if (this.body2 != null) {
            this.localTemperature = Math.max(this.localTemperature, this.body2.getTemperature());
            this.body2.setTemperature(this.localTemperature);
        }
        if (this.body1 != null)
            this.body1.setTemperature(this.localTemperature);
    }

    protected void calculateDesiredDeltaVelocity(double t) {
        double velocityLimit = 0.25D;

        double vFromAcc = 0.0D;

        vFromAcc = this.body1.lastFrameAcceleration.mult(t).dot(this.contactNormal);

        if (this.body2 != null) {
            vFromAcc -= this.body2.lastFrameAcceleration.mult(t).dot(this.contactNormal);
        }
        double r = this.restitution;

        if (Math.abs(this.contactVelocity.x) < velocityLimit) {
            r = 0.0D;
        }

        this.desiredDeltaVelocity = (-this.contactVelocity.x - r * (this.contactVelocity.x - vFromAcc));
    }

    protected Vec3d calculateLocalVelocity(int index, double t) {
        Vec3d relContactPos;
        RigidBody b;
        if (index == 0) {
            b = this.body1;
            CollisionMesh cm = this.mesh1;
            relContactPos = this.relativeContactPosition1;
        } else {
            b = this.body2;
            CollisionMesh cm = this.mesh2;
            relContactPos = this.relativeContactPosition2;
        }

        Vec3d v = b.getRotation().cross(relContactPos);
        v.addLocal(b.getVelocity());

        Vec3d vContact = this.contactToWorld.transpose().mult(v);

        Vec3d vAcc = b.lastFrameAcceleration.mult(t);

        vAcc = this.contactToWorld.transpose().mult(vAcc);

        vAcc.x = 0.0D;

        vContact.addLocal(vAcc);

        return vContact;
    }

    protected void calculateContactBasis() {
        Vec3d contactTangent0 = new Vec3d();
        Vec3d contactTangent1 = new Vec3d();

        if (Math.abs(this.contactNormal.x) > Math.abs(this.contactNormal.y)) {
            double s = 1.0D / Math.sqrt(this.contactNormal.z * this.contactNormal.z + this.contactNormal.x * this.contactNormal.x);

            contactTangent0.x = (this.contactNormal.z * s);
            contactTangent0.y = 0.0D;
            contactTangent0.z = (-this.contactNormal.x * s);

            contactTangent1.x = (this.contactNormal.y * contactTangent0.x);
            contactTangent1.y = (this.contactNormal.z * contactTangent0.x - this.contactNormal.x * contactTangent0.z);

            contactTangent1.z = (-this.contactNormal.y * contactTangent0.x);
        } else {
            double s = 1.0D / Math.sqrt(this.contactNormal.z * this.contactNormal.z + this.contactNormal.y * this.contactNormal.y);

            contactTangent0.x = 0.0D;
            contactTangent0.y = (-this.contactNormal.z * s);
            contactTangent0.z = (this.contactNormal.y * s);

            contactTangent1.x = (this.contactNormal.y * contactTangent0.z - this.contactNormal.z * contactTangent0.y);

            contactTangent1.y = (-this.contactNormal.x * contactTangent0.z);
            contactTangent1.z = (this.contactNormal.x * contactTangent0.y);
        }

        this.contactToWorld.setColumn(0, this.contactNormal);
        this.contactToWorld.setColumn(1, contactTangent0);
        this.contactToWorld.setColumn(2, contactTangent1);
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

    protected void applyImpulse(Vec3d impulse, RigidBody b, Vec3d vDelta, Vec3d rotDelta) {
    }

    protected void applyVelocityChange(Vec3d vDelta1, Vec3d vDelta2, Vec3d rotDelta1, Vec3d rotDelta2) {
        Matrix3d iit1 = null;
        Matrix3d iit2 = null;
        iit1 = this.body1.getInverseInertiaTensorWorld();
        if (this.body2 != null)
            iit2 = this.body2.getInverseInertiaTensorWorld();
        Vec3d impContact;
        if (this.friction == 0.0D)
            impContact = calculateFrictionlessImpulse(iit1, iit2);
        else {
            impContact = calculateFrictionImpulse(iit1, iit2);
        }

        Vec3d imp = this.contactToWorld.mult(impContact);

        Vec3d impTorque = this.relativeContactPosition1.cross(imp);

        rotDelta1.set(iit1.mult(impTorque));

        vDelta1.set(0.0D, 0.0D, 0.0D);

        addScaledVector(vDelta1, imp, this.body1.getInverseMass());

        this.body1.addVelocity(vDelta1);

        this.body1.addRotation(rotDelta1);

        if (this.body2 != null) {
            impTorque = imp.cross(this.relativeContactPosition2);
            rotDelta2.set(iit2.mult(impTorque));
            vDelta2.set(0.0D, 0.0D, 0.0D);
            addScaledVector(vDelta2, imp, -this.body2.getInverseMass());

            this.body2.addVelocity(vDelta2);
            this.body2.addRotation(rotDelta2);
        }
    }

    protected void calculateVelocityChange(Vec3d vDelta1, Vec3d vDelta2, Vec3d rotDelta1, Vec3d rotDelta2) {
        Matrix3d iit1 = null;
        Matrix3d iit2 = null;
        iit1 = this.body1.getInverseInertiaTensorWorld();
        if (this.body2 != null)
            iit2 = this.body2.getInverseInertiaTensorWorld();
        Vec3d impContact;
        if (this.friction == 0.0D)
            impContact = calculateFrictionlessImpulse(iit1, iit2);
        else {
            impContact = calculateFrictionImpulse(iit1, iit2);
        }

        Vec3d imp = this.contactToWorld.mult(impContact);

        Vec3d impTorque = this.relativeContactPosition1.cross(imp);

        rotDelta1.set(iit1.mult(impTorque));

        vDelta1.set(0.0D, 0.0D, 0.0D);
        addScaledVector(vDelta1, imp, this.body1.getInverseMass());

        if (this.body2 != null) {
            impTorque = imp.cross(this.relativeContactPosition2);
            rotDelta2.set(iit2.mult(impTorque));
            vDelta2.set(0.0D, 0.0D, 0.0D);
            addScaledVector(vDelta2, imp, -this.body2.getInverseMass());
        }
    }

    protected void applyPositionChange(Vec3d linear1, Vec3d linear2, Vec3d angular1, Vec3d angular2, double penetration) {
        double angularLimit = 0.001D;

        double totalInertia = 0.0D;
        double linearInertia1 = 0.0D;
        double linearInertia2 = 0.0D;
        double angularInertia1 = 0.0D;
        double angularInertia2 = 0.0D;

        if (this.body1 != null) {
            Matrix3d iit = this.body1.getInverseInertiaTensorWorld();
            Vec3d angularInertiaWorld = this.relativeContactPosition1.cross(this.contactNormal);
            angularInertiaWorld = iit.mult(angularInertiaWorld);
            angularInertiaWorld = angularInertiaWorld.cross(this.relativeContactPosition1);
            angularInertia1 = angularInertiaWorld.dot(this.contactNormal);

            linearInertia1 = this.body1.getInverseMass();
            totalInertia += linearInertia1 + angularInertia1;
        }

        if (this.body2 != null) {
            Matrix3d iit = this.body2.getInverseInertiaTensorWorld();
            Vec3d angularInertiaWorld = this.relativeContactPosition2.cross(this.contactNormal);
            angularInertiaWorld = iit.mult(angularInertiaWorld);
            angularInertiaWorld = angularInertiaWorld.cross(this.relativeContactPosition2);
            angularInertia2 = angularInertiaWorld.dot(this.contactNormal);

            linearInertia2 = this.body2.getInverseMass();
            totalInertia += linearInertia2 + angularInertia2;
        }

        if (this.body1 != null) {
            double sign = 1.0D;
            double angularMove1 = sign * penetration * (angularInertia1 / totalInertia);
            double linearMove1 = sign * penetration * (linearInertia1 / totalInertia);

            Vec3d projection = this.relativeContactPosition1.clone();
            addScaledVector(projection, this.contactNormal, -this.relativeContactPosition1.dot(this.contactNormal));

            double maxMagnitude = angularLimit * projection.length();

            if (angularMove1 < -maxMagnitude) {
                double totalMove = angularMove1 + linearMove1;
                angularMove1 = -maxMagnitude;
                linearMove1 = totalMove - angularMove1;
            } else if (angularMove1 > maxMagnitude) {
                double totalMove = angularMove1 + linearMove1;
                angularMove1 = maxMagnitude;
                linearMove1 = totalMove - angularMove1;
            } else {
                angularMove1 = 0.0D;
            }

            if (angularMove1 == 0.0D) {
                angular1.set(0.0D, 0.0D, 0.0D);
            } else {
                Vec3d targetAngularDirection = this.relativeContactPosition1.cross(this.contactNormal);
                Matrix3d iit = this.body1.getInverseInertiaTensorWorld();
                angular1.set(iit.mult(targetAngularDirection).multLocal(angularMove1 / angularInertia1));
            }

            double vLimit = 15.0D;
            if (linearMove1 > vLimit) {
                linearMove1 = vLimit;
            }
            linear1.set(this.contactNormal.mult(linearMove1));

            this.mesh1.position.addScaledVectorLocal(this.contactNormal, linearMove1);

            if (angularMove1 != 0.0D) {
                this.mesh1.orientation.addScaledVectorLocal(angular1, 1.0D);
            }

            this.body1.calculateDerivedData();
        }

        if (this.body2 != null) {
            double sign = -1.0D;
            double angularMove2 = sign * penetration * (angularInertia2 / totalInertia);
            double linearMove2 = sign * penetration * (linearInertia2 / totalInertia);

            Vec3d projection = this.relativeContactPosition2.clone();
            projection.addScaledVectorLocal(this.contactNormal, -this.relativeContactPosition2.dot(this.contactNormal));

            double maxMagnitude = angularLimit * projection.length();

            if (angularMove2 < -maxMagnitude) {
                double totalMove = angularMove2 + linearMove2;
                angularMove2 = -maxMagnitude;
                linearMove2 = totalMove - angularMove2;
            } else if (angularMove2 > maxMagnitude) {
                double totalMove = angularMove2 + linearMove2;
                angularMove2 = maxMagnitude;
                linearMove2 = totalMove - angularMove2;
            }

            if (angularMove2 == 0.0D) {
                angular2.set(0.0D, 0.0D, 0.0D);
            } else {
                Vec3d targetAngularDirection = this.relativeContactPosition2.cross(this.contactNormal);
                Matrix3d iit = this.body2.getInverseInertiaTensorWorld();
                angular2.set(iit.mult(targetAngularDirection).multLocal(angularMove2 / angularInertia2));
            }

            double vLimit = 15.0D;

            if (linearMove2 > vLimit) {
                linearMove2 = vLimit;
            }
            linear2.set(this.contactNormal.mult(linearMove2));

            this.mesh2.position.addScaledVectorLocal(this.contactNormal, linearMove2);

            this.mesh2.orientation.addScaledVectorLocal(angular2, 1.0D);

            this.body2.calculateDerivedData();
        }
    }

    protected void calculatePositionChange(Vec3d linear1, Vec3d linear2, Vec3d angular1, Vec3d angular2, double penetration) {
        double angularLimit = 0.001D;

        double totalInertia = 0.0D;
        double linearInertia1 = 0.0D;
        double linearInertia2 = 0.0D;
        double angularInertia1 = 0.0D;
        double angularInertia2 = 0.0D;

        if (this.body1 != null) {
            Matrix3d iit = this.body1.getInverseInertiaTensorWorld();
            Vec3d angularInertiaWorld = this.relativeContactPosition1.cross(this.contactNormal);
            angularInertiaWorld = iit.mult(angularInertiaWorld);
            angularInertiaWorld = angularInertiaWorld.cross(this.relativeContactPosition1);
            angularInertia1 = angularInertiaWorld.dot(this.contactNormal);

            linearInertia1 = this.body1.getInverseMass();
            totalInertia += linearInertia1 + angularInertia1;
        }

        if (this.body2 != null) {
            Matrix3d iit = this.body2.getInverseInertiaTensorWorld();
            Vec3d angularInertiaWorld = this.relativeContactPosition2.cross(this.contactNormal);
            angularInertiaWorld = iit.mult(angularInertiaWorld);
            angularInertiaWorld = angularInertiaWorld.cross(this.relativeContactPosition2);
            angularInertia2 = angularInertiaWorld.dot(this.contactNormal);

            linearInertia2 = this.body2.getInverseMass();
            totalInertia += linearInertia2 + angularInertia2;
        }

        if (this.body1 != null) {
            double sign = 1.0D;
            double angularMove1 = sign * penetration * (angularInertia1 / totalInertia);
            double linearMove1 = sign * penetration * (linearInertia1 / totalInertia);
            Vec3d projection = this.relativeContactPosition1.clone();
            addScaledVector(projection, this.contactNormal, -this.relativeContactPosition1.dot(this.contactNormal));

            double maxMagnitude = angularLimit * projection.length();
            if (angularMove1 < -maxMagnitude) {
                double totalMove = angularMove1 + linearMove1;
                angularMove1 = -maxMagnitude;
                linearMove1 = totalMove - angularMove1;
            } else if (angularMove1 > maxMagnitude) {
                double totalMove = angularMove1 + linearMove1;
                angularMove1 = maxMagnitude;
                linearMove1 = totalMove - angularMove1;
            } else {
                angularMove1 = 0.0D;
            }

            if (angularMove1 == 0.0D) {
                angular1.set(0.0D, 0.0D, 0.0D);
            } else {
                Vec3d targetAngularDirection = this.relativeContactPosition1.cross(this.contactNormal);
                Matrix3d iit = this.body1.getInverseInertiaTensorWorld();
                angular1.set(iit.mult(targetAngularDirection).multLocal(angularMove1 / angularInertia1));
            }

            double vLimit = 15.0D;
            if (linearMove1 > vLimit) {
                linearMove1 = vLimit;
            }
            linear1.set(this.contactNormal.mult(linearMove1));
        }

        if (this.body2 != null) {
            double sign = -1.0D;
            double angularMove2 = sign * penetration * (angularInertia2 / totalInertia);
            double linearMove2 = sign * penetration * (linearInertia2 / totalInertia);

            Vec3d projection = this.relativeContactPosition2.clone();
            projection.addScaledVectorLocal(this.contactNormal, -this.relativeContactPosition2.dot(this.contactNormal));

            double maxMagnitude = angularLimit * projection.length();

            if (angularMove2 < -maxMagnitude) {
                double totalMove = angularMove2 + linearMove2;
                angularMove2 = -maxMagnitude;
                linearMove2 = totalMove - angularMove2;
            } else if (angularMove2 > maxMagnitude) {
                double totalMove = angularMove2 + linearMove2;
                angularMove2 = maxMagnitude;
                linearMove2 = totalMove - angularMove2;
            }

            if (angularMove2 == 0.0D) {
                angular2.set(0.0D, 0.0D, 0.0D);
            } else {
                Vec3d targetAngularDirection = this.relativeContactPosition2.cross(this.contactNormal);
                Matrix3d iit = this.body2.getInverseInertiaTensorWorld();
                angular2.set(iit.mult(targetAngularDirection).multLocal(angularMove2 / angularInertia2));
            }

            double vLimit = 15.0D;

            if (linearMove2 > vLimit) {
                linearMove2 = vLimit;
            }
            linear2.set(this.contactNormal.mult(linearMove2));
        }
    }

    protected Vec3d calculateFrictionlessImpulse(Matrix3d iit1, Matrix3d iit2) {
        Vec3d impContact = new Vec3d();

        Vec3d vDeltaWorld = this.relativeContactPosition1.cross(this.contactNormal);
        vDeltaWorld = iit1.mult(vDeltaWorld);
        vDeltaWorld = vDeltaWorld.cross(this.relativeContactPosition1);

        double vDelta = vDeltaWorld.dot(this.contactNormal);

        vDelta += this.body1.getInverseMass();

        if (this.body2 != null) {
            vDeltaWorld = this.relativeContactPosition2.cross(this.contactNormal);
            vDeltaWorld = iit2.mult(vDeltaWorld);
            vDeltaWorld = vDeltaWorld.cross(this.relativeContactPosition2);

            vDelta += vDeltaWorld.dot(this.contactNormal);

            vDelta += this.body2.getInverseMass();
        }

        impContact.x = (this.desiredDeltaVelocity / vDelta);
        return impContact;
    }

    public static void setSkewSymmetric(Matrix3d target, Vec3d v) {
        target.m00 = 0.0D;
        target.m11 = 0.0D;
        target.m22 = 0.0D;

        target.m01 = (-v.z);
        target.m02 = v.y;
        target.m10 = v.z;
        target.m12 = (-v.x);
        target.m20 = (-v.y);
        target.m21 = v.x;
    }

    public static void add(Matrix3d m1, Matrix3d m2) {
        m1.m00 += m2.m00;
        m1.m01 += m2.m01;
        m1.m02 += m2.m02;
        m1.m10 += m2.m10;
        m1.m11 += m2.m11;
        m1.m12 += m2.m12;
        m1.m20 += m2.m20;
        m1.m21 += m2.m21;
        m1.m22 += m2.m22;
    }

    protected Vec3d calculateFrictionImpulse(Matrix3d iit1, Matrix3d iit2) {
        double invMass = this.body1.getInverseMass();

        Matrix3d impToTorque = new Matrix3d();
        setSkewSymmetric(impToTorque, this.relativeContactPosition1);

        Matrix3d vDeltaWorld = impToTorque.clone();
        vDeltaWorld.multLocal(iit1);
        vDeltaWorld.multLocal(impToTorque);
        vDeltaWorld.multLocal(-1.0D);

        if (this.body2 != null) {
            setSkewSymmetric(impToTorque, this.relativeContactPosition2);

            Matrix3d vDeltaWorld2 = impToTorque.clone();
            vDeltaWorld2.multLocal(iit2);
            vDeltaWorld2.multLocal(impToTorque);
            vDeltaWorld2.multLocal(-1.0D);

            add(vDeltaWorld, vDeltaWorld2);

            invMass += this.body2.getInverseMass();
        }

        Matrix3d vDelta = this.contactToWorld.transpose();
        vDelta.multLocal(vDeltaWorld);
        vDelta.multLocal(this.contactToWorld);

        vDelta.m00 += invMass;
        vDelta.m11 += invMass;
        vDelta.m22 += invMass;

        Matrix3d impMatrix = vDelta.invert();

        Vec3d velKill = new Vec3d(this.desiredDeltaVelocity, -this.contactVelocity.y, -this.contactVelocity.z);
        Vec3d impContact = impMatrix.mult(velKill);

        double planarImp = Math.sqrt(impContact.y * impContact.y + impContact.z * impContact.z);

        if (planarImp > impContact.x * this.friction) {
            impContact.y /= planarImp;
            impContact.z /= planarImp;

            impContact.x = (vDelta.m00 + vDelta.m01 * this.friction * impContact.y + vDelta.m02 * this.friction * impContact.z);

            impContact.x = (this.desiredDeltaVelocity / impContact.x);
            impContact.y *= this.friction * impContact.x;
            impContact.z *= this.friction * impContact.x;
        }

        return impContact;
    }
}