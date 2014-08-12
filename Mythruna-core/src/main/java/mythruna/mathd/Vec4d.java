package mythruna.mathd;

public final class Vec4d implements Cloneable {
    
    public double x;
    public double y;
    public double z;
    public double w;

    public Vec4d() {
    }

    public Vec4d(double x, double y, double z, double w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public Vec4d(Vec3d v, double w) {
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
        this.w = w;
    }

    public final void set(double x, double y, double z, double w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public final void set(Vec3d v, double w) {
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
        this.w = w;
    }

    public final void set(Vec4d v) {
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
        this.w = v.w;
    }

    public final Vec4d clone() {
        return new Vec4d(this.x, this.y, this.z, this.w);
    }

    public int hashCode() {
        long hash = 37L;
        hash += 37L * hash + Double.doubleToLongBits(this.x);
        hash += 37L * hash + Double.doubleToLongBits(this.y);
        hash += 37L * hash + Double.doubleToLongBits(this.z);
        hash += 37L * hash + Double.doubleToLongBits(this.w);

        return (int) (hash ^ hash >>> 32);
    }

    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (o == null)
            return false;
        if (o.getClass() != getClass())
            return false;
        Vec4d other = (Vec4d) o;

        if (Double.compare(this.x, other.x) != 0)
            return false;
        if (Double.compare(this.y, other.y) != 0)
            return false;
        if (Double.compare(this.z, other.z) != 0)
            return false;
        if (Double.compare(this.w, other.w) != 0)
            return false;
        return true;
    }

    public Vec3d collapse() {
        double s = 1.0D / this.w;
        return new Vec3d(this.x * s, this.y * s, this.z * s);
    }

    public Vec3d xyz() {
        return new Vec3d(this.x, this.y, this.z);
    }

    public final Vec4d add(Vec4d v) {
        return new Vec4d(this.x + v.x, this.y + v.y, this.z + v.z, this.w + v.w);
    }

    public final Vec4d add(Vec3d v, double vw) {
        return new Vec4d(this.x + v.x, this.y + v.y, this.z + v.z, this.w + vw);
    }

    public final Vec4d add(double vx, double vy, double vz, double vw) {
        return new Vec4d(this.x + vx, this.y + vy, this.z + vz, this.w + vw);
    }

    public final Vec4d subtract(Vec4d v) {
        return new Vec4d(this.x - v.x, this.y - v.y, this.z - v.z, this.w - v.w);
    }

    public final Vec4d subtract(double vx, double vy, double vz, double vw) {
        return new Vec4d(this.x - vx, this.y - vy, this.z - vz, this.w - vw);
    }

    public final Vec4d mult(double s) {
        return new Vec4d(this.x * s, this.y * s, this.z * s, this.w * s);
    }

    public final Vec4d mult(Vec4d v) {
        return new Vec4d(this.x * v.x, this.y * v.y, this.z * v.z, this.w * v.w);
    }

    public final Vec4d addLocal(Vec4d v) {
        this.x += v.x;
        this.y += v.y;
        this.z += v.z;
        this.w += v.w;
        return this;
    }

    public final Vec4d addLocal(Vec3d v, double vw) {
        this.x += v.x;
        this.y += v.y;
        this.z += v.z;
        this.w += vw;
        return this;
    }

    public final Vec4d addLocal(double vx, double vy, double vz, double vw) {
        this.x += vx;
        this.y += vy;
        this.z += vz;
        this.w += vw;
        return this;
    }

    public final Vec4d subtractLocal(Vec4d v) {
        this.x -= v.x;
        this.y -= v.y;
        this.z -= v.z;
        this.w -= v.w;
        return this;
    }

    public final Vec4d multLocal(double s) {
        this.x *= s;
        this.y *= s;
        this.z *= s;
        this.w *= s;
        return this;
    }

    public final double lengthSq() {
        return this.x * this.x + this.y * this.y + this.z * this.z + this.w * this.w;
    }

    public final double length() {
        return Math.sqrt(lengthSq());
    }

    public final Vec4d normalize() {
        return mult(1.0D / length());
    }

    public final Vec4d normalizeLocal() {
        return multLocal(1.0D / length());
    }

    public String toString() {
        return "Vec4[" + this.x + ", " + this.y + ", " + this.z + ", " + this.w + "]";
    }
}