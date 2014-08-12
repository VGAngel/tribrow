package mythruna.phys.collision;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import mythruna.Direction;
import mythruna.mathd.Quatd;
import mythruna.mathd.Vec3d;
import mythruna.phys.Collider;
import mythruna.phys.Contact;

public class CubeCollider implements Collider {

    private Vec3d center = new Vec3d(0.5D, 0.5D, 0.5D);
    private Vec3d extents = new Vec3d(0.5D, 0.5D, 0.5D);
    private boolean symetric = false;
    private String name;

    public CubeCollider() {
    }

    public CubeCollider(String name) {
        this.name = name;
    }

    public CubeCollider(Vec3d min, Vec3d max) {
        this(min, max, null);
    }

    public CubeCollider(Vec3d min, Vec3d max, String name) {
        this.name = name;
        this.center.set(min);
        this.center.addLocal(max);
        this.center.multLocal(0.5D);
        this.extents.set(max);
        this.extents.subtractLocal(min);
        this.extents.multLocal(0.5D);
        if ((this.center.x == 0.0D) && (this.center.z == 0.0D))
            this.symetric = true;
    }

    public CubeCollider(Vector3f min, Vector3f max) {
        this(min, max, null);
    }

    public CubeCollider(Vector3f min, Vector3f max, String name) {
        this.name = name;

        this.center.set(min.x, min.z, min.y);
        this.center.addLocal(max.x, max.z, max.y);
        this.center.multLocal(0.5D);
        this.extents.set(max.x, max.z, max.y);
        this.extents.subtractLocal(min.x, min.z, min.y);
        this.extents.multLocal(0.5D);
        if ((this.center.x == 0.0D) && (this.center.y == 0.0D))
            this.symetric = true;
    }

    protected CubeCollider(CubeCollider toClone) {
        this.center = toClone.center.clone();
        this.extents = toClone.extents.clone();
        this.symetric = toClone.symetric;
        this.name = toClone.name;
    }

    protected static float epsilon(float f) {
        long i = Math.round(f * 10000.0F);
        f = (float) i / 10000.0F;
        return f;
    }

    public String getName() {
        if (this.name == null) {
            this.name = "box";
        }
        return this.name;
    }

    protected double epsilon(double d) {
        long i = Math.round(d * 10000.0D);
        d = i / 10000.0D;

        return d;
    }

    public Collider rotate(int dirDelta) {
        if (this.symetric) {
            return new CubeCollider(this);
        }

        Vec3d min = this.center.subtract(this.extents);
        Vec3d max = this.center.add(this.extents);

        min.subtractLocal(0.5D, 0.5D, 0.5D);
        max.subtractLocal(0.5D, 0.5D, 0.5D);

        Quaternion quat = new Quaternion().fromAngles(0.0F, -0.01745329F * dirDelta * 90.0F, 0.0F);
        Quatd q = new Quatd(quat.getX(), quat.getY(), quat.getZ(), quat.getW());

        Vec3d v1 = q.mult(min);
        Vec3d v2 = q.mult(max);

        min.x = epsilon(0.5D + Math.min(v1.x, v2.x));
        min.y = epsilon(0.5D + Math.min(v1.y, v2.y));
        min.z = epsilon(0.5D + Math.min(v1.z, v2.z));
        max.x = epsilon(0.5D + Math.max(v1.x, v2.x));
        max.y = epsilon(0.5D + Math.max(v1.y, v2.y));
        max.z = epsilon(0.5D + Math.max(v1.z, v2.z));

        CubeCollider result = new CubeCollider(min, max, this.name);

        return result;
    }

    private static final double clamp(double v, double min, double max) {
        return v > max ? max : v < min ? min : v;
    }

    private static final double clamp(double v, double extent) {
        return clamp(v, -extent, extent);
    }

    public Contact getContact(Vec3d cellPos, Vec3d pos, double radius, int dirMask, int srcInvMask) {
        double x = pos.x - (cellPos.x + this.center.x);
        double y = pos.y - (cellPos.y + this.center.y);
        double z = pos.z - (cellPos.z + this.center.z);

        if (x <= -(this.extents.x + radius))
            return null;
        if (y <= -(this.extents.y + radius))
            return null;
        if (z <= -(this.extents.z + radius))
            return null;
        if (x >= this.extents.x + radius)
            return null;
        if (y >= this.extents.y + radius)
            return null;
        if (z >= this.extents.z + radius) {
            return null;
        }

        Vec3d cp = new Vec3d();
        Vec3d normal = new Vec3d();
        double pen = 10.0D;

        int hitMask = 0;

        if (Direction.hasEast(srcInvMask)) {
            double p = this.extents.x + radius - x;
            if ((p > 0.0D) && (p < pen)) {
                pen = p;
                normal.set(1.0D, 0.0D, 0.0D);
                cp.set(0.5D, clamp(y, this.extents.y), clamp(z, this.extents.z));
                hitMask = 4;
            }
        }
        if (Direction.hasWest(srcInvMask)) {
            double p = x + (this.extents.x + radius);

            if ((p > 0.0D) && (p < pen)) {
                pen = p;
                normal.set(-1.0D, 0.0D, 0.0D);
                cp.set(-0.5D, clamp(y, this.extents.y), clamp(z, this.extents.z));
                hitMask = 8;
            }
        }
        if (Direction.hasSouth(srcInvMask)) {
            double p = this.extents.z + radius - z;
            if ((p > 0.0D) && (p < pen)) {
                pen = p;
                normal.set(0.0D, 0.0D, 1.0D);
                cp.set(clamp(x, this.extents.x), clamp(y, this.extents.y), 0.5D);
                hitMask = 2;
            }
        }
        if (Direction.hasNorth(srcInvMask)) {
            double p = z + (this.extents.z + radius);
            if ((p > 0.0D) && (p < pen)) {
                pen = p;
                normal.set(0.0D, 0.0D, -1.0D);
                cp.set(clamp(x, this.extents.x), clamp(y, this.extents.y), -0.5D);
                hitMask = 1;
            }
        }
        if (Direction.hasUp(srcInvMask)) {
            double p = this.extents.y + radius - y;
            if ((p > 0.0D) && (p < pen)) {
                pen = p;
                normal.set(0.0D, 1.0D, 0.0D);
                cp.set(clamp(x, this.extents.x), 0.5D, clamp(z, this.extents.z));
                hitMask = 16;
            }
        }
        if (Direction.hasDown(srcInvMask)) {
            double p = y + (this.extents.y + radius);
            if ((p > 0.0D) && (p < pen)) {
                pen = p;
                normal.set(0.0D, -1.0D, 0.0D);
                cp.set(clamp(x, this.extents.x), -0.5D, clamp(z, this.extents.z));
                hitMask = 32;
            }

        }

        if ((hitMask & dirMask) == 0) {
            return null;
        }

        Contact result = new Contact();
        result.contactPoint = cp.addLocal(cellPos).addLocal(this.center);
        result.contactNormal = normal;
        result.penetration = pen;

        return result;
    }

    public static final double checkCubeContact(double x, double y, double z, double radius, Vec3d cp, Vec3d cn, double maxPen, int dirMask, int srcInvMask, int[] resultMask) {
        double pen = maxPen;
        int hitMask = 0;

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

        resultMask[0] = hitMask;
        return pen;
    }
}