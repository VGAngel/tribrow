package mythruna.mathd;

public class Matrix3d {
    
    public double m00;
    public double m01;
    public double m02;
    public double m10;
    public double m11;
    public double m12;
    public double m20;
    public double m21;
    public double m22;

    public Matrix3d() {
        makeIdentity();
    }

    public Matrix3d(double m00, double m01, double m02, double m10, double m11, double m12, double m20, double m21, double m22) {
        this.m00 = m00;
        this.m01 = m01;
        this.m02 = m02;
        this.m10 = m10;
        this.m11 = m11;
        this.m12 = m12;
        this.m20 = m20;
        this.m21 = m21;
        this.m22 = m22;
    }

    public Matrix3d clone() {
        return new Matrix3d(this.m00, this.m01, this.m02, this.m10, this.m11, this.m12, this.m20, this.m21, this.m22);
    }

    public void set(Matrix3d mat) {
        this.m00 = mat.m00;
        this.m01 = mat.m01;
        this.m02 = mat.m02;
        this.m10 = mat.m10;
        this.m11 = mat.m11;
        this.m12 = mat.m12;
        this.m20 = mat.m20;
        this.m21 = mat.m21;
        this.m22 = mat.m22;
    }

    public void makeIdentity() {
        this.m01 = (this.m02 = this.m10 = this.m12 = this.m20 = this.m21 = 0.0D);
        this.m00 = (this.m11 = this.m22 = 1.0D);
    }

    public Vec3d getColumn(int i) {
        switch (i) {
            case 0:
                return new Vec3d(this.m00, this.m10, this.m20);
            case 1:
                return new Vec3d(this.m01, this.m11, this.m21);
            case 2:
                return new Vec3d(this.m02, this.m12, this.m22);
        }
        return null;
    }

    public Matrix3d setColumn(int i, Vec3d col) {
        switch (i) {
            case 0:
                this.m00 = col.x;
                this.m10 = col.y;
                this.m20 = col.z;
                break;
            case 1:
                this.m01 = col.x;
                this.m11 = col.y;
                this.m21 = col.z;
                break;
            case 2:
                this.m02 = col.x;
                this.m12 = col.y;
                this.m22 = col.z;
                break;
            default:
                throw new IllegalArgumentException("Column does not exist:" + i);
        }
        return this;
    }

    public Matrix3d mult(Matrix3d mat) {
        double temp00 = this.m00 * mat.m00 + this.m01 * mat.m10 + this.m02 * mat.m20;
        double temp01 = this.m00 * mat.m01 + this.m01 * mat.m11 + this.m02 * mat.m21;
        double temp02 = this.m00 * mat.m02 + this.m01 * mat.m12 + this.m02 * mat.m22;
        double temp10 = this.m10 * mat.m00 + this.m11 * mat.m10 + this.m12 * mat.m20;
        double temp11 = this.m10 * mat.m01 + this.m11 * mat.m11 + this.m12 * mat.m21;
        double temp12 = this.m10 * mat.m02 + this.m11 * mat.m12 + this.m12 * mat.m22;
        double temp20 = this.m20 * mat.m00 + this.m21 * mat.m10 + this.m22 * mat.m20;
        double temp21 = this.m20 * mat.m01 + this.m21 * mat.m11 + this.m22 * mat.m21;
        double temp22 = this.m20 * mat.m02 + this.m21 * mat.m12 + this.m22 * mat.m22;

        return new Matrix3d(temp00, temp01, temp02, temp10, temp11, temp12, temp20, temp21, temp22);
    }

    public Vec3d mult(Vec3d v) {
        double x = v.x;
        double y = v.y;
        double z = v.z;

        double xr = this.m00 * x + this.m01 * y + this.m02 * z;
        double yr = this.m10 * x + this.m11 * y + this.m12 * z;
        double zr = this.m20 * x + this.m21 * y + this.m22 * z;

        return new Vec3d(xr, yr, zr);
    }

    public Matrix3d multLocal(Matrix3d mat) {
        double temp00 = this.m00 * mat.m00 + this.m01 * mat.m10 + this.m02 * mat.m20;
        double temp01 = this.m00 * mat.m01 + this.m01 * mat.m11 + this.m02 * mat.m21;
        double temp02 = this.m00 * mat.m02 + this.m01 * mat.m12 + this.m02 * mat.m22;
        double temp10 = this.m10 * mat.m00 + this.m11 * mat.m10 + this.m12 * mat.m20;
        double temp11 = this.m10 * mat.m01 + this.m11 * mat.m11 + this.m12 * mat.m21;
        double temp12 = this.m10 * mat.m02 + this.m11 * mat.m12 + this.m12 * mat.m22;
        double temp20 = this.m20 * mat.m00 + this.m21 * mat.m10 + this.m22 * mat.m20;
        double temp21 = this.m20 * mat.m01 + this.m21 * mat.m11 + this.m22 * mat.m21;
        double temp22 = this.m20 * mat.m02 + this.m21 * mat.m12 + this.m22 * mat.m22;

        this.m00 = temp00;
        this.m01 = temp01;
        this.m02 = temp02;
        this.m10 = temp10;
        this.m11 = temp11;
        this.m12 = temp12;
        this.m20 = temp20;
        this.m21 = temp21;
        this.m22 = temp22;

        return this;
    }

    public Matrix3d multLocal(double scale) {
        this.m00 *= scale;
        this.m01 *= scale;
        this.m02 *= scale;
        this.m10 *= scale;
        this.m11 *= scale;
        this.m12 *= scale;
        this.m20 *= scale;
        this.m21 *= scale;
        this.m22 *= scale;

        return this;
    }

    public double determinant() {
        double co00 = this.m11 * this.m22 - this.m12 * this.m21;
        double co10 = this.m12 * this.m20 - this.m10 * this.m22;
        double co20 = this.m10 * this.m21 - this.m11 * this.m20;
        return this.m00 * co00 + this.m01 * co10 + this.m02 * co20;
    }

    public Matrix3d invert() {
        double d = determinant();
        if (d == 0.0D) {
            return new Matrix3d();
        }
        double rm00 = this.m11 * this.m22 - this.m12 * this.m21;
        double rm01 = this.m02 * this.m21 - this.m01 * this.m22;
        double rm02 = this.m01 * this.m12 - this.m02 * this.m11;
        double rm10 = this.m12 * this.m20 - this.m10 * this.m22;
        double rm11 = this.m00 * this.m22 - this.m02 * this.m20;
        double rm12 = this.m02 * this.m10 - this.m00 * this.m12;
        double rm20 = this.m10 * this.m21 - this.m11 * this.m20;
        double rm21 = this.m01 * this.m20 - this.m00 * this.m21;
        double rm22 = this.m00 * this.m11 - this.m01 * this.m10;

        double s = 1.0D / d;

        return new Matrix3d(rm00 * s, rm01 * s, rm02 * s, rm10 * s, rm11 * s, rm12 * s, rm20 * s, rm21 * s, rm22 * s);
    }

    public Matrix3d transpose() {
        return new Matrix3d(this.m00, this.m10, this.m20, this.m01, this.m11, this.m21, this.m02, this.m12, this.m22);
    }

    public String toString() {
        return "Matrix3d[{" + this.m00 + ", " + this.m01 + ", " + this.m02 + "}, {" + this.m10 + ", " + this.m11 + ", " + this.m12 + "}, {" + this.m20 + ", " + this.m21 + ", " + this.m22 + "}]";
    }
}