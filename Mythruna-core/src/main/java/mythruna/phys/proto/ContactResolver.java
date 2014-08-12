package mythruna.phys.proto;

import mythruna.mathd.Vec3d;

import java.util.List;

public class ContactResolver {

    private int maxContacts = 256;

    private int velocityIterations = this.maxContacts * 18;
    private int positionIterations = this.maxContacts * 18;

    private double velocityEpsilon = 1.E-005D;
    private double positionEpsilon = 1.0E-006D;

    public ContactResolver() {
    }

    public void resolveContacts(List<Contact> contacts, double t) {
        if (contacts.isEmpty()) {
            return;
        }
        prepareContacts(contacts, t);
        adjustPositions(contacts, t);
        adjustVelocities(contacts, t);
    }

    protected void prepareContacts(List<Contact> contacts, double t) {
        for (Contact c : contacts)
            c.calculateInternals(t);
    }

    protected void adjustVelocities(List<Contact> contacts, double t) {
        Vec3d[] vChange = {new Vec3d(), new Vec3d()};
        Vec3d[] rotChange = {new Vec3d(), new Vec3d()};
        Vec3d deltaV = new Vec3d();

        int used = 0;
        Contact contact;
        while (used < this.velocityIterations) {
            double max = this.velocityEpsilon;
            contact = null;
            for (Contact c : contacts) {
                if (c.desiredDeltaVelocity > max) {
                    max = c.desiredDeltaVelocity;
                    contact = c;
                }
            }

            if (contact == null) {
                break;
            }
            used++;

            vChange[0].set(0.0D, 0.0D, 0.0D);
            vChange[1].set(0.0D, 0.0D, 0.0D);
            rotChange[0].set(0.0D, 0.0D, 0.0D);
            rotChange[1].set(0.0D, 0.0D, 0.0D);
            deltaV.set(0.0D, 0.0D, 0.0D);

            contact.matchAwakeState();
            contact.applyVelocityChange(vChange[0], vChange[1], rotChange[0], rotChange[1]);

            for (Contact c : contacts) {
                if ((c.body1 != null) && (c.body1 == contact.body1) && (!c.body1.isStatic())) {
                    deltaV.set(vChange[0].add(rotChange[0].cross(c.relativeContactPosition1)));

                    double sign = 1.0D;

                    c.contactVelocity.addLocal(c.contactToWorld.transpose().mult(deltaV).mult(sign));

                    c.calculateDesiredDeltaVelocity(t);
                }

                if ((c.body2 != null) && (c.body2 == contact.body1)) {
                    deltaV.set(vChange[0].add(rotChange[0].cross(c.relativeContactPosition2)));

                    double sign = -1.0D;
                    c.contactVelocity.addLocal(c.contactToWorld.transpose().mult(deltaV).mult(sign));
                    c.calculateDesiredDeltaVelocity(t);
                }
                if ((c.body1 != null) && (c.body1 == contact.body2)) {
                    deltaV.set(vChange[1].add(rotChange[1].cross(c.relativeContactPosition1)));

                    double sign = 1.0D;
                    c.contactVelocity.addLocal(c.contactToWorld.transpose().mult(deltaV).mult(sign));
                    c.calculateDesiredDeltaVelocity(t);
                }
                if ((c.body2 != null) && (c.body2 == contact.body2) && (!c.body2.isStatic())) {
                    deltaV.set(vChange[1].add(rotChange[1].cross(c.relativeContactPosition2)));

                    double sign = -1.0D;
                    c.contactVelocity.addLocal(c.contactToWorld.transpose().mult(deltaV).mult(sign));
                    c.calculateDesiredDeltaVelocity(t);
                }

            }

        }

        if (used >= this.velocityIterations)
            System.out.println("Velocity bottomed out.");
    }

    protected void adjustPositions(List<Contact> contacts, double t) {
        Vec3d[] linearChange = {new Vec3d(), new Vec3d()};
        Vec3d[] angularChange = {new Vec3d(), new Vec3d()};
        Vec3d deltaPos = new Vec3d();

        int used = 0;
        Contact contact;
        while (used < this.positionIterations) {
            double max = this.positionEpsilon;
            contact = null;
            for (Contact c : contacts) {
                if (c.penetration > max) {
                    max = c.penetration;
                    contact = c;
                }
            }

            if (contact == null) {
                break;
            }
            used++;

            linearChange[0].set(0.0D, 0.0D, 0.0D);
            linearChange[1].set(0.0D, 0.0D, 0.0D);
            angularChange[0].set(0.0D, 0.0D, 0.0D);
            angularChange[1].set(0.0D, 0.0D, 0.0D);
            deltaPos.set(0.0D, 0.0D, 0.0D);

            contact.matchAwakeState();

            contact.applyPositionChange(linearChange[0], linearChange[1], angularChange[0], angularChange[1], max);

            for (Contact c : contacts) {
                if ((c.body1 != null) && (c.body1 == contact.body1)) {
                    deltaPos.set(linearChange[0].add(angularChange[0].cross(c.relativeContactPosition1)));

                    double sign = -1.0D;
                    c.penetration += deltaPos.dot(c.contactNormal) * sign;
                }

                if ((c.body2 != null) && (c.body2 == contact.body1)) {
                    deltaPos.set(linearChange[0].add(angularChange[0].cross(c.relativeContactPosition2)));

                    double sign = 1.0D;
                    c.penetration += deltaPos.dot(c.contactNormal) * sign;
                }

                if ((c.body1 != null) && (c.body1 == contact.body2)) {
                    deltaPos.set(linearChange[1].add(angularChange[1].cross(c.relativeContactPosition1)));

                    double sign = -1.0D;
                    c.penetration += deltaPos.dot(c.contactNormal) * sign;
                }

                if ((c.body2 != null) && (c.body2 == contact.body2)) {
                    deltaPos.set(linearChange[1].add(angularChange[1].cross(c.relativeContactPosition2)));

                    double sign = 1.0D;
                    c.penetration += deltaPos.dot(c.contactNormal) * sign;
                }
            }

        }

        if (used >= this.positionIterations)
            System.out.println("Position bottomed out.");
    }
}