package mythruna.phys.collision;

import mythruna.Direction;
import mythruna.mathd.Vec3d;
import mythruna.phys.Collider;
import mythruna.phys.Contact;

public class AngleCollider implements Collider {

    private static final double HALF_SQRT2 = Math.sqrt(2.0D) * 0.5D;
    private static final double COS45 = Math.cos(0.7853981852531433D);
    private static final double[][] normals45 = {{COS45, 0.0D, COS45}, {-COS45, 0.0D, -COS45}, {-COS45, 0.0D, COS45}, {COS45, 0.0D, -COS45}};
    private int dir;
    private Vec3d normal;
    private Vec3d solidCorner;
    private double xSign;
    private double zSign;
    private int angleMask;

    public AngleCollider(int dir) {
        this.dir = dir;
        this.normal = new Vec3d(normals45[dir][0], normals45[dir][1], normals45[dir][2]);

        this.xSign = (-Math.signum(this.normal.x));
        this.zSign = (-Math.signum(this.normal.z));

        this.solidCorner = new Vec3d(0.5D + this.xSign * 0.5D, 0.0D, 0.5D + this.zSign * 0.5D);

        switch (dir) {
            case 0:
                this.angleMask = 6;
                break;
            case 1:
                this.angleMask = 9;
                break;
            case 2:
                this.angleMask = 10;
                break;
            case 3:
                this.angleMask = 5;
        }
    }

    public String getName() {
        return "angle";
    }

    public Collider rotate(int dirDelta) {
        return new AngleCollider(Direction.rotate(this.dir, dirDelta));
    }

    public Contact getContact(Vec3d cellPos, Vec3d pos, double radius, int dirMask, int srcInvMask) {
        double x = pos.x - cellPos.x;
        double y = pos.y - cellPos.y;
        double z = pos.z - cellPos.z;

        double xPen = x + radius * this.xSign;
        double zPen = z + radius * this.zSign;

        if ((y + radius <= 0.0D) || (y - radius >= 1.0D)) {
            return null;
        }
        Vec3d cp = new Vec3d();
        Vec3d cn = new Vec3d();
        double pen = 10.0D;
        int hitMask = 0;

        if ((xPen > 0.0D) && (zPen > 0.0D) && (xPen < 1.0D) && (zPen < 1.0D)) {
            double xn = xPen - this.solidCorner.x;
            double zn = zPen - this.solidCorner.z;

            double dot = xn * this.normal.x + zn * this.normal.z;
            if (dot > HALF_SQRT2) {
                return null;
            }

            if ((srcInvMask & this.angleMask) == this.angleMask) {
                double p = HALF_SQRT2 - dot;
                if ((p > 0.0D) && (p < pen)) {
                    pen = p;

                    cp.x = (xPen + this.normal.x * p);
                    cp.y = y;
                    cp.z = (zPen + this.normal.z * p);

                    cn.set(this.normal);
                    hitMask = this.angleMask;
                }
            }
        }

        int[] resultMask = new int[1];
        pen = CubeCollider.checkCubeContact(x, y, z, radius, cp, cn, pen, dirMask, srcInvMask, resultMask);
        if (resultMask[0] != 0) {
            hitMask = resultMask[0];
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