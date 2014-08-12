package mythruna.phys.collision;

import com.jme3.math.Quaternion;
import mythruna.mathd.Quatd;
import mythruna.mathd.Vec3d;
import mythruna.phys.Collider;
import mythruna.phys.Contact;

public class CylinderCollider implements Collider {

    private int d;
    private Quatd rotation;
    private Vec3d origin;
    private Vec3d dir;
    private Vec3d axis1;
    private Vec3d axis2;
    private double radius;
    private double radiusSq;
    private double length;
    private int axis1PosMask;
    private int axis1NegMask;
    private int axis2PosMask;
    private int axis2NegMask;
    private int endMask1;
    private int endMask2;

    public CylinderCollider(double radius) {
        this.d = 4;
        this.radius = radius;
        this.origin = new Vec3d(0.5D, 0.0D, 0.5D);
        this.dir = new Vec3d(0.0D, 1.0D, 0.0D);
        this.axis1 = new Vec3d(1.0D, 0.0D, 0.0D);
        this.axis2 = new Vec3d(0.0D, 0.0D, 1.0D);
        this.length = 1.0D;
        this.radiusSq = (radius * radius);

        this.axis1PosMask = 4;
        this.axis1NegMask = 8;
        this.axis2PosMask = 2;
        this.axis2NegMask = 1;
        this.endMask1 = 32;
        this.endMask2 = 16;
    }

    public CylinderCollider(int d, double radius, double length, double xOrigin, double yOrigin, double zOrigin) {
        this.d = d;
        this.radius = radius;
        this.radiusSq = (radius * radius);
        this.length = length;
        this.origin = new Vec3d(xOrigin, yOrigin, zOrigin);

        switch (d) {
            case 2:
                this.dir = new Vec3d(1.0D, 1.0D, 0.0D).normalizeLocal();
                this.axis1 = new Vec3d(-1.0D, 1.0D, 0.0D).normalizeLocal();
                this.axis2 = new Vec3d(0.0D, 0.0D, 1.0D);

                this.axis1PosMask = 36;
                this.axis1NegMask = 24;
                this.axis2PosMask = 2;
                this.axis2NegMask = 1;

                this.endMask1 = 40;
                this.endMask1 = 20;
                break;
            case 3:
                this.dir = new Vec3d(-1.0D, 1.0D, 0.0D).normalizeLocal();
                this.axis1 = new Vec3d(1.0D, 1.0D, 0.0D).normalizeLocal();
                this.axis2 = new Vec3d(0.0D, 0.0D, 1.0D);

                this.axis1PosMask = 20;
                this.axis1NegMask = 40;
                this.axis2PosMask = 2;
                this.axis2NegMask = 1;

                this.endMask1 = 36;
                this.endMask1 = 24;
                break;
            case 0:
                this.dir = new Vec3d(0.0D, 1.0D, -1.0D).normalizeLocal();
                this.axis1 = new Vec3d(0.0D, 1.0D, 1.0D).normalizeLocal();
                this.axis2 = new Vec3d(1.0D, 0.0D, 0.0D);

                this.axis1PosMask = 18;
                this.axis1NegMask = 33;
                this.axis2PosMask = 4;
                this.axis2NegMask = 8;

                this.endMask1 = 34;
                this.endMask1 = 17;
                break;
            case 1:
                this.dir = new Vec3d(0.0D, 1.0D, 1.0D).normalizeLocal();
                this.axis1 = new Vec3d(0.0D, 1.0D, -1.0D).normalizeLocal();
                this.axis2 = new Vec3d(1.0D, 0.0D, 0.0D);

                this.axis1PosMask = 34;
                this.axis1NegMask = 17;
                this.axis2PosMask = 4;
                this.axis2NegMask = 8;

                this.endMask1 = 33;
                this.endMask1 = 18;
                break;
            default:
                throw new IllegalArgumentException("Illegal direction:" + d);
        }
    }

    public CylinderCollider(Quaternion rotation, double radius, double length) {
        this.d = -1;
        this.radius = radius;
        this.radiusSq = (radius * radius);
        this.length = length;
        this.origin = new Vec3d(0.0D, 0.0D, 0.0D);
        this.rotation = new Quatd(rotation.getX(), rotation.getY(), rotation.getZ(), rotation.getW());

        this.dir = new Vec3d(0.0D, 1.0D, 0.0D);
        this.axis1 = new Vec3d(-1.0D, 0.0D, 0.0D);
        this.axis2 = new Vec3d(0.0D, 0.0D, 1.0D);

        this.dir = this.rotation.mult(this.dir);

        this.axis1 = this.rotation.mult(this.axis1);
        this.axis2 = this.rotation.mult(this.axis2);

        if (this.dir.x != 0.0D)
            this.endMask1 = (this.endMask1 | 0x8 | 0x4);
        if (this.dir.y != 0.0D)
            this.endMask1 = (this.endMask1 | 0x10 | 0x20);
        if (this.dir.z != 0.0D) {
            this.endMask1 = (this.endMask1 | 0x1 | 0x2);
        }
        if (this.axis1.x != 0.0D) {
            this.axis1PosMask |= 4;
            this.axis1NegMask = (this.axis1PosMask | 0x8);
        }
        if (this.axis1.y != 0.0D) {
            this.axis1PosMask |= 16;
            this.axis1NegMask = (this.axis1PosMask | 0x20);
        }
        if (this.axis1.z != 0.0D) {
            this.axis1PosMask |= 2;
            this.axis1NegMask = (this.axis1PosMask | 0x1);
        }
        if (this.axis2.x != 0.0D) {
            this.axis2PosMask |= 4;
            this.axis2NegMask = (this.axis2PosMask | 0x8);
        }
        if (this.axis2.y != 0.0D) {
            this.axis2PosMask |= 16;
            this.axis2NegMask = (this.axis2PosMask | 0x20);
        }
        if (this.axis2.z != 0.0D) {
            this.axis2PosMask |= 2;
            this.axis2NegMask = (this.axis2PosMask | 0x1);
        }
    }

    protected static float epsilon(double d) {
        long i = Math.round(d * 10000.0D);
        float f = (float) i / 10000.0F;
        return f;
    }

    public String getName() {
        return "cylinder-" + epsilon(this.radius) + "x" + epsilon(this.length);
    }

    public Collider rotate(int dirDelta) {
        return this;
    }

    public Contact getContact(Vec3d cellPos, Vec3d pos, double cellRadius, int dirMask, int srcInvMask) {
        double x = pos.x - cellPos.x;
        double y = pos.y - cellPos.y;
        double z = pos.z - cellPos.z;

        double xr = x - this.origin.x;
        double yr = y - this.origin.y;
        double zr = z - this.origin.z;

        double yc = this.dir.dot(xr, yr, zr);
        if ((yc <= -cellRadius) || (yc >= this.length + cellRadius)) {
            return null;
        }
        double xc = this.axis1.dot(xr, yr, zr);
        if ((xc < -(this.radius + cellRadius)) || (xc > this.radius + cellRadius))
            return null;
        double zc = this.axis2.dot(xr, yr, zr);
        if ((zc < -(this.radius + cellRadius)) || (zc > this.radius + cellRadius)) {
            return null;
        }

        double xp = 0.0D;
        double x1 = xc - cellRadius;
        double x2 = xc + cellRadius;
        if (x2 < 0.0D)
            xp = x2;
        if (x1 > 0.0D) {
            xp = x1;
        }
        double zp = 0.0D;
        double z1 = zc - cellRadius;
        double z2 = zc + cellRadius;
        if (z2 < 0.0D)
            zp = z2;
        if (z1 > 0.0D) {
            zp = z1;
        }

        double distSq = xp * xp + zp * zp;
        if (distSq > this.radiusSq) {
            return null;
        }

        int sideMask = 0;
        if (xc >= 0.0D)
            sideMask |= this.axis1PosMask;
        if (xc <= 0.0D)
            sideMask |= this.axis1PosMask;
        if (zc >= 0.0D)
            sideMask |= this.axis2PosMask;
        if (zc <= 0.0D) {
            sideMask |= this.axis2PosMask;
        }

        Vec3d cp = new Vec3d();
        Vec3d cn = new Vec3d();
        double pen = 10.0D;
        int hitMask = 0;

        if ((sideMask & srcInvMask) != 0) {
            double xDelta = 0.0D;
            double zDelta = 0.0D;
            if ((xp == 0.0D) && (zp == 0.0D)) {
                double xOffset = 0.0D;
                double zOffset = 0.0D;

                if (-x1 > x2) {
                    xOffset = x2;
                } else {
                    xOffset = x1;
                }
                if (-z1 > z2) {
                    zOffset = z2;
                } else {
                    zOffset = z1;
                }

                double offset = Math.sqrt(xOffset * xOffset + zOffset * zOffset);
                pen = this.radius + offset;

                cn.set(-(xOffset / offset), 0.0D, -(zOffset / offset));
                xDelta = cn.x * offset;
                zDelta = cn.z * offset;
            } else {
                double dist = Math.sqrt(distSq);
                double p = this.radius - dist;
                pen = p;

                cn.set(xp / dist, 0.0D, zp / dist);

                xDelta = cn.x * this.radius;
                zDelta = cn.z * this.radius;
            }

            cp.set(xDelta, yc, zDelta);
            hitMask = sideMask;
        }

        if ((this.endMask1 & srcInvMask) != 0) {
            double p = yc + cellRadius;
            if ((p > 0.0D) && (p < pen)) {
                pen = p;
                cn.set(0.0D, -1.0D, 0.0D);
                cp.set(xc, 0.0D, zc);
                hitMask = this.endMask1;
            }
        }
        if ((this.endMask2 & srcInvMask) != 0) {
            double p = 1.0D - (yc - cellRadius);
            if ((p > 0.0D) && (p < pen)) {
                pen = p;
                cn.set(0.0D, 1.0D, 0.0D);
                cp.set(xc, 1.0D, zc);
                hitMask = this.endMask2;
            }
        }

        if ((hitMask & dirMask) == 0) {
            return null;
        }

        Contact result = new Contact();

        Vec3d temp = new Vec3d();
        temp.addLocal(this.axis1.x * cp.x, this.axis1.y * cp.x, this.axis1.z * cp.x);
        temp.addLocal(this.axis2.x * cp.z, this.axis2.y * cp.z, this.axis2.z * cp.z);
        temp.addLocal(this.dir.x * cp.y, this.dir.y * cp.y, this.dir.z * cp.y);
        temp.addLocal(this.origin);
        temp.addLocal(cellPos);
        result.contactPoint = temp;

        temp = new Vec3d();
        temp.addLocal(this.axis1.x * cn.x, this.axis1.y * cn.x, this.axis1.z * cn.x);
        temp.addLocal(this.axis2.x * cn.z, this.axis2.y * cn.z, this.axis2.z * cn.z);
        temp.addLocal(this.dir.x * cn.y, this.dir.y * cn.y, this.dir.z * cn.y);
        result.contactNormal = temp;

        result.penetration = pen;

        return result;
    }
}