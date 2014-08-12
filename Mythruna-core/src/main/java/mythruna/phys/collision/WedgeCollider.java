package mythruna.phys.collision;

import mythruna.Direction;
import mythruna.mathd.Vec3d;
import mythruna.phys.Collider;
import mythruna.phys.Contact;

public class WedgeCollider implements Collider {

    private static final double HALF_SQRT2 = Math.sqrt(2.0D) * 0.5D;
    private int solidDir;
    private boolean facingUp;
    private Vec3d rising;
    private Vec3d up;
    private Vec3d side;
    private Vec3d normal;
    private Vec3d rampNormal;
    private double slope;
    private Vec3d origin;
    private int rampMask;

    public WedgeCollider(int solidDir, boolean facingUp) {
        this.solidDir = solidDir;
        this.facingUp = facingUp;

        this.rising = new Vec3d(Direction.DIRS[solidDir][0], Direction.DIRS[solidDir][2], Direction.DIRS[solidDir][1]);
        if (facingUp)
            this.up = new Vec3d(0.0D, 1.0D, 0.0D);
        else {
            this.up = new Vec3d(0.0D, -1.0D, 0.0D);
        }

        this.normal = new Vec3d(-this.rising.x, this.up.y, -this.rising.z).normalizeLocal();

        this.slope = 1.0D;
        this.rampNormal = new Vec3d(-1.0D, this.up.y, 0.0D).normalizeLocal();

        this.rampMask = Direction.MASKS[solidDir];
        if (facingUp)
            this.rampMask |= 16;
        else {
            this.rampMask |= 32;
        }

        switch (solidDir) {
            case 0:
                this.origin = new Vec3d(0.0D, 0.0D, 1.0D);
                this.side = new Vec3d(1.0D, 0.0D, 0.0D);
                break;
            case 1:
                this.origin = new Vec3d(0.0D, 0.0D, 0.0D);
                this.side = new Vec3d(1.0D, 0.0D, 0.0D);
                break;
            case 2:
                this.origin = new Vec3d(0.0D, 0.0D, 0.0D);
                this.side = new Vec3d(0.0D, 0.0D, 1.0D);
                break;
            case 3:
                this.origin = new Vec3d(1.0D, 0.0D, 0.0D);
                this.side = new Vec3d(0.0D, 0.0D, 1.0D);
        }
    }

    public boolean equals(Object o) {
        if (o == this)
            return true;
        if ((o == null) || (o.getClass() != getClass()))
            return false;
        WedgeCollider other = (WedgeCollider) o;
        if (other.solidDir != this.solidDir)
            return false;
        if (other.facingUp != this.facingUp) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return this.solidDir;
    }

    public String getName() {
        return new StringBuilder().append("wedge").append(this.facingUp ? "" : "-up").toString();
    }

    public Collider rotate(int dirDelta) {
        return new WedgeCollider(Direction.rotate(this.solidDir, dirDelta), this.facingUp);
    }

    private static final double clamp(double v, double min, double max) {
        return v > max ? max : v < min ? min : v;
    }

    public Contact getContact(Vec3d cellPos, Vec3d pos, double radius, int dirMask, int srcInvMask) {
        Vec3d temp = pos.clone();
        temp.x -= cellPos.x + this.origin.x;
        temp.y -= cellPos.y + this.origin.y;
        temp.z -= cellPos.z + this.origin.z;

        double x = this.rising.dot(temp);

        if ((x + radius <= 0.0D) || (x - radius >= 1.0D)) {
            return null;
        }

        if ((temp.y + radius < 0.0D) || (temp.y - radius > 1.0D)) {
            return null;
        }
        double z = this.side.dot(temp);
        double y = temp.y - radius * this.up.y;

        double h = (x + radius) * this.slope;
        if (this.up.y < 0.0D) {
            h = 1.0D - h;
        }

        if ((this.up.y > 0.0D) && (temp.y - radius > h))
            return null;
        if ((this.up.y < 0.0D) && (temp.y + radius < h)) {
            return null;
        }

        Vec3d cp = new Vec3d();
        Vec3d cn = new Vec3d();
        double pen = 10.0D;
        int hitMask = 0;

        if ((x + radius < 1.0D) && ((srcInvMask & this.rampMask) == this.rampMask)) {
            double ry = y;
            if (this.up.y < 0.0D) {
                ry = y - 1.0D;
            }
            double penDot = (1.0D - (x + radius)) * -this.rampNormal.x + ry * this.rampNormal.y;
            double p = HALF_SQRT2 - penDot;

            double contact = x + radius + this.rampNormal.x * p;
            if ((contact > 0.0D) && (contact < 1.0D) && (p > 0.0D) && (p < pen)) {
                pen = p;

                double cx = x + radius - this.rampNormal.x * p;
                double cy = y + this.rampNormal.y * p;
                double cz = z;

                cp.set(this.origin);
                cp.addLocal(this.rising.x * cx, this.rising.y * cx, this.rising.z * cx);
                cp.addLocal(this.side.x * cz, this.side.y * cz, this.side.z * cz);
                cp.y = cy;

                cn.set(this.normal);

                hitMask = this.rampMask;
            }

        }

        x = pos.x - cellPos.x;
        y = pos.y - cellPos.y;
        z = pos.z - cellPos.z;

        if (Direction.hasEast(srcInvMask)) {
            double p = 1.0D + radius - x;
            if ((p > 0.0D) && (p < pen)) {
                pen = p;
                cn.set(1.0D, 0.0D, 0.0D);
                cp.set(1.0D, clamp(y, 0.0D, 1.0D), clamp(z, 0.0D, 1.0D));
                hitMask = 4;
            }
        }
        if (Direction.hasWest(srcInvMask)) {
            double p = x + radius;
            if ((p > 0.0D) && (p < pen)) {
                pen = p;
                cn.set(-1.0D, 0.0D, 0.0D);
                cp.set(0.0D, clamp(y, 0.0D, 1.0D), clamp(z, 0.0D, 1.0D));
                hitMask = 8;
            }
        }
        if (Direction.hasNorth(srcInvMask)) {
            double p = z + radius;
            if ((p > 0.0D) && (p < pen)) {
                pen = p;
                cn.set(0.0D, 0.0D, -1.0D);
                cp.set(clamp(x, 0.0D, 1.0D), clamp(y, 0.0D, 1.0D), 0.0D);
                hitMask = 1;
            }
        }
        if (Direction.hasSouth(srcInvMask)) {
            double p = 1.0D + radius - z;
            if ((p > 0.0D) && (p < pen)) {
                pen = p;
                cn.set(0.0D, 0.0D, 1.0D);
                cp.set(clamp(x, 0.0D, 1.0D), clamp(y, 0.0D, 1.0D), 1.0D);
                hitMask = 2;
            }
        }
        if (Direction.hasUp(srcInvMask)) {
            double p = 1.0D + radius - y;
            if ((p > 0.0D) && (p < pen)) {
                pen = p;
                cn.set(0.0D, 1.0D, 0.0D);
                cp.set(clamp(x, 0.0D, 1.0D), 1.0D, clamp(z, 0.0D, 1.0D));
                hitMask = 16;
            }
        }
        if (Direction.hasDown(srcInvMask)) {
            double p = y + radius;
            if ((p > 0.0D) && (p < pen)) {
                pen = p;
                cn.set(0.0D, -1.0D, 0.0D);
                cp.set(clamp(x, 0.0D, 1.0D), 0.0D, clamp(z, 0.0D, 1.0D));
                hitMask = 32;
            }
        }

        if ((hitMask & dirMask) == 0) {
            return null;
        }

        Contact result = new Contact();
        result.contactPoint = cp.addLocal(cellPos);
        result.contactNormal = cn;
        result.penetration = pen;

        return result;
    }
}