package mythruna.mathd;

public final class Vec3d implements Cloneable {
    
    public double x;
    public double y;
    public double z;

    public Vec3d() {
    }

    public Vec3d(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public final void set(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public final void set(Vec3d v) {
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
    }

    public final Vec3d clone() {
        return new Vec3d(this.x, this.y, this.z);
    }

    public final Vec3d add(Vec3d v) {
        return new Vec3d(this.x + v.x, this.y + v.y, this.z + v.z);
    }

    public final Vec3d add(double vx, double vy, double vz) {
        return new Vec3d(this.x + vx, this.y + vy, this.z + vz);
    }

    public final Vec3d subtract(Vec3d v) {
        return new Vec3d(this.x - v.x, this.y - v.y, this.z - v.z);
    }

    public final Vec3d subtract(double vx, double vy, double vz) {
        return new Vec3d(this.x - vx, this.y - vy, this.z - vz);
    }

    public final Vec3d mult(double s) {
        return new Vec3d(this.x * s, this.y * s, this.z * s);
    }

    public final Vec3d mult(Vec3d v) {
        return new Vec3d(this.x * v.x, this.y * v.y, this.z * v.z);
    }

    public final Vec3d addLocal(Vec3d v) {
        this.x += v.x;
        this.y += v.y;
        this.z += v.z;
        return this;
    }

    public final Vec3d addLocal(double vx, double vy, double vz) {
        this.x += vx;
        this.y += vy;
        this.z += vz;
        return this;
    }

    public final Vec3d subtractLocal(Vec3d v) {
        this.x -= v.x;
        this.y -= v.y;
        this.z -= v.z;
        return this;
    }

    public final Vec3d subtractLocal(double vx, double vy, double vz) {
        this.x -= vx;
        this.y -= vy;
        this.z -= vz;
        return this;
    }

    public final Vec3d multLocal(double s) {
        this.x *= s;
        this.y *= s;
        this.z *= s;
        return this;
    }

    public final double lengthSq() {
        return this.x * this.x + this.y * this.y + this.z * this.z;
    }

    public final double length() {
        return Math.sqrt(lengthSq());
    }

    public final Vec3d normalize() {
        return mult(1.0D / length());
    }

    public final Vec3d normalizeLocal() {
        return multLocal(1.0D / length());
    }

    public final double dot(Vec3d v) {
        return this.x * v.x + this.y * v.y + this.z * v.z;
    }

    public final double dot(double vx, double vy, double vz) {
        return this.x * vx + this.y * vy + this.z * vz;
    }

    public final Vec3d cross(Vec3d v) {
        double xNew = this.y * v.z - this.z * v.y;
        double yNew = this.z * v.x - this.x * v.z;
        double zNew = this.x * v.y - this.y * v.x;
        return new Vec3d(xNew, yNew, zNew);
    }

    public final Vec3d crossLocal(Vec3d v) {
        double xNew = this.y * v.z - this.z * v.y;
        double yNew = this.z * v.x - this.x * v.z;
        double zNew = this.x * v.y - this.y * v.x;

        this.x = xNew;
        this.y = yNew;
        this.z = zNew;
        return this;
    }

    public final Vec3d addScaledVectorLocal(Vec3d toAdd, double scale) {
        this.x += toAdd.x * scale;
        this.y += toAdd.y * scale;
        this.z += toAdd.z * scale;
        return this;
    }

    public final Vec3d minLocal(Vec3d v) {
        this.x = (this.x < v.x ? this.x : v.x);
        this.y = (this.y < v.y ? this.y : v.y);
        this.z = (this.z < v.z ? this.z : v.z);
        return this;
    }

    public final Vec3d maxLocal(Vec3d v) {
        this.x = (this.x > v.x ? this.x : v.x);
        this.y = (this.y > v.y ? this.y : v.y);
        this.z = (this.z > v.z ? this.z : v.z);
        return this;
    }

    public final void zeroEpsilon(double e) {
        if ((this.x > -e) && (this.x < e))
            this.x = 0.0D;
        if ((this.y > -e) && (this.y < e))
            this.y = 0.0D;
        if ((this.z > -e) && (this.z < e))
            this.z = 0.0D;
    }

    public String toString() {
        return "Vec3[" + this.x + ", " + this.y + ", " + this.z + "]";
    }
}