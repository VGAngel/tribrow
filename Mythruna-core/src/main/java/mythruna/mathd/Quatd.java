package mythruna.mathd;

public final class Quatd implements Cloneable {
    
    public double x;
    public double y;
    public double z;
    public double w;

    public Quatd() {
        this(0.0D, 0.0D, 0.0D, 1.0D);
    }

    public Quatd(double x, double y, double z, double w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public final Quatd clone() {
        return new Quatd(this.x, this.y, this.z, this.w);
    }

    public final void set(double x, double y, double z, double w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public final void set(Quatd q) {
        this.x = q.x;
        this.y = q.y;
        this.z = q.z;
        this.w = q.w;
    }

    public final Quatd add(Quatd q) {
        return new Quatd(this.x + q.x, this.y + q.y, this.z + q.z, this.w + q.w);
    }

    public final Quatd addLocal(Quatd q) {
        this.x += q.x;
        this.y += q.y;
        this.z += q.z;
        this.w += q.w;
        return this;
    }

    public final Quatd subtract(Quatd q) {
        return new Quatd(this.x - q.x, this.y - q.y, this.z - q.z, this.w - q.w);
    }

    public final Quatd subtractLocal(Quatd q) {
        this.x -= q.x;
        this.y -= q.y;
        this.z -= q.z;
        this.w -= q.w;
        return this;
    }

    public final void addScaledVectorLocal(Vec3d v, double scale) {
        Quatd q = new Quatd(v.x * scale, v.y * scale, v.z * scale, 0.0D);

        q.multLocal(this);

        this.x += q.x * 0.5D;
        this.y += q.y * 0.5D;
        this.z += q.z * 0.5D;
        this.w += q.w * 0.5D;
    }

    public final Quatd mult(Quatd q) {
        double qx = q.x;
        double qy = q.y;
        double qz = q.z;
        double qw = q.w;

        double xr = this.x * qw + this.y * qz - this.z * qy + this.w * qx;
        double yr = -this.x * qz + this.y * qw + this.z * qx + this.w * qy;
        double zr = this.x * qy - this.y * qx + this.z * qw + this.w * qz;
        double wr = -this.x * qx - this.y * qy - this.z * qz + this.w * qw;

        return new Quatd(xr, yr, zr, wr);
    }

    public final Quatd multLocal(Quatd q) {
        double qx = q.x;
        double qy = q.y;
        double qz = q.z;
        double qw = q.w;

        double xr = this.x * qw + this.y * qz - this.z * qy + this.w * qx;
        double yr = -this.x * qz + this.y * qw + this.z * qx + this.w * qy;
        double zr = this.x * qy - this.y * qx + this.z * qw + this.w * qz;
        double wr = -this.x * qx - this.y * qy - this.z * qz + this.w * qw;

        this.x = xr;
        this.y = yr;
        this.z = zr;
        this.w = wr;
        return this;
    }

    public Vec3d mult(Vec3d v) {
        if ((v.x == 0.0D) && (v.y == 0.0D) && (v.z == 0.0D)) {
            return new Vec3d();
        }
        double vx = v.x;
        double vy = v.y;
        double vz = v.z;

        double rx = this.w * this.w * vx + 2.0D * this.y * this.w * vz - 2.0D * this.z * this.w * vy + this.x * this.x * vx + 2.0D * this.y * this.x * vy + 2.0D * this.z * this.x * vz - this.z * this.z * vx - this.y * this.y * vx;

        double ry = 2.0D * this.x * this.y * vx + this.y * this.y * vy + 2.0D * this.z * this.y * vz + 2.0D * this.w * this.z * vx - this.z * this.z * vy + this.w * this.w * vy - 2.0D * this.x * this.w * vz - this.x * this.x * vy;

        double rz = 2.0D * this.x * this.z * vx + 2.0D * this.y * this.z * vy + this.z * this.z * vz - 2.0D * this.w * this.y * vx - this.y * this.y * vz + 2.0D * this.w * this.x * vy - this.x * this.x * vz + this.w * this.w * vz;

        return new Vec3d(rx, ry, rz);
    }

    public Vec3d mult(Vec3d v, Vec3d result) {
        if ((v.x == 0.0D) && (v.y == 0.0D) && (v.z == 0.0D)) {
            if (v != result)
                result.set(0.0D, 0.0D, 0.0D);
            return result;
        }

        double vx = v.x;
        double vy = v.y;
        double vz = v.z;

        double rx = this.w * this.w * vx + 2.0D * this.y * this.w * vz - 2.0D * this.z * this.w * vy + this.x * this.x * vx + 2.0D * this.y * this.x * vy + 2.0D * this.z * this.x * vz - this.z * this.z * vx - this.y * this.y * vx;

        double ry = 2.0D * this.x * this.y * vx + this.y * this.y * vy + 2.0D * this.z * this.y * vz + 2.0D * this.w * this.z * vx - this.z * this.z * vy + this.w * this.w * vy - 2.0D * this.x * this.w * vz - this.x * this.x * vy;

        double rz = 2.0D * this.x * this.z * vx + 2.0D * this.y * this.z * vy + this.z * this.z * vz - 2.0D * this.w * this.y * vx - this.y * this.y * vz + 2.0D * this.w * this.x * vy - this.x * this.x * vz + this.w * this.w * vz;

        result.set(rx, ry, rz);
        return result;
    }

    public final double lengthSq() {
        return this.x * this.x + this.y * this.y + this.z * this.z + this.w * this.w;
    }

    public final Quatd normalizeLocal() {
        double d = lengthSq();
        if (d == 0.0D) {
            this.w = 1.0D;
            return this;
        }

        double s = 1.0D / Math.sqrt(d);
        this.x *= s;
        this.y *= s;
        this.z *= s;
        this.w *= s;

        return this;
    }

    public Matrix3d toRotationMatrix() {
        double d = lengthSq();
        double s = 2.0D / d;

        double xs = this.x * s;
        double ys = this.y * s;
        double zs = this.z * s;
        double xx = this.x * xs;
        double xy = this.x * ys;
        double xz = this.x * zs;
        double xw = this.w * xs;
        double yy = this.y * ys;
        double yz = this.y * zs;
        double yw = this.w * ys;
        double zz = this.z * zs;
        double zw = this.w * zs;

        double m00 = 1.0D - (yy + zz);
        double m01 = xy - zw;
        double m02 = xz + yw;
        double m10 = xy + zw;
        double m11 = 1.0D - (xx + zz);
        double m12 = yz - xw;
        double m20 = xz - yw;
        double m21 = yz + xw;
        double m22 = 1.0D - (xx + yy);

        return new Matrix3d(m00, m01, m02, m10, m11, m12, m20, m21, m22);
    }

    public Quatd inverse() {
        double norm = lengthSq();
        if (norm <= 0.0D) {
            return null;
        }
        double inv = 1.0D / norm;
        return new Quatd(-this.x * inv, -this.y * inv, -this.z * inv, this.w * inv);
    }

    public String toString() {
        return "Quatd[" + this.x + ", " + this.y + ", " + this.z + ", " + this.w + "]";
    }
}