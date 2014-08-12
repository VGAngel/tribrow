package mythruna.phys.proto;

import mythruna.mathd.Vec3d;

import java.util.List;

public class ContactGenerators {

    public ContactGenerators() {
    }

    public static ContactGenerator hardLink(RigidBody b1, Vec3d pos1, RigidBody b2, Vec3d pos2, double error) {
        return new HardLink(b1, pos1, b2, pos2, error);
    }

    private static class HardLink implements ContactGenerator {
        public RigidBody b1;
        public RigidBody b2;
        public Vec3d pos1;
        public Vec3d pos2;
        public double error;

        public HardLink(RigidBody b1, Vec3d pos1, RigidBody b2, Vec3d pos2, double error) {
            this.b1 = b1;
            this.b2 = b2;
            this.pos1 = pos1;
            this.pos2 = pos2;
            this.error = error;
        }

        public int addContacts(List<Contact> contacts) {
            if ((!this.b1.isAwake()) && (!this.b2.isAwake())) {
                return 0;
            }

            Vec3d p1World = this.b1.localToWorld(this.pos1);

            Vec3d p2World = this.b2.localToWorld(this.pos2);

            Vec3d dir = p2World.subtract(p1World);
            Vec3d normal = dir.clone();
            double length = dir.length();
            normal.multLocal(1.0D / length);

            if (Math.abs(length) <= this.error) {
                return 0;
            }

            Contact c = new Contact();
            c.contactNormal = normal;
            if (this.b2.isStatic()) {
                c.contactPoint = p2World;
                c.alwaysAwake = this.b2.isAwake();
            } else {
                c.contactPoint = p1World.add(p2World).mult(0.5D);
            }
            c.penetration = (length - this.error);

            if (this.b2.isStatic())
                c.setBodyData(this.b1, null, 1.0D, 0.0D);
            else {
                c.setBodyData(this.b1, this.b2, 1.0D, 0.0D);
            }
            contacts.add(c);

            return 1;
        }
    }
}