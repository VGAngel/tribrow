package mythruna.mathd;

public class Matrix4d {
    
    public double m00;
    public double m01;
    public double m02;
    public double m03;
    public double m10;
    public double m11;
    public double m12;
    public double m13;
    public double m20;
    public double m21;
    public double m22;
    public double m23;
    public double m30;
    public double m31;
    public double m32;
    public double m33;

    public Matrix4d() {
        makeIdentity();
    }

    public Matrix4d(double m00, double m01, double m02, double m03, double m10, double m11, double m12, double m13, double m20, double m21, double m22, double m23, double m30, double m31, double m32, double m33) {
        this.m00 = m00;
        this.m01 = m01;
        this.m02 = m02;
        this.m03 = m03;
        this.m10 = m10;
        this.m11 = m11;
        this.m12 = m12;
        this.m13 = m13;
        this.m20 = m20;
        this.m21 = m21;
        this.m22 = m22;
        this.m23 = m23;
        this.m30 = m30;
        this.m31 = m31;
        this.m32 = m32;
        this.m33 = m33;
    }

    public void makeIdentity() {
        this.m01 = (this.m02 = this.m03 = 0.0D);
        this.m10 = (this.m12 = this.m13 = 0.0D);
        this.m20 = (this.m21 = this.m23 = 0.0D);
        this.m30 = (this.m31 = this.m32 = 0.0D);
        this.m00 = (this.m11 = this.m22 = this.m33 = 1.0D);
    }

    public void setTransform(Vec3d pos, Matrix3d rot) {
        this.m00 = rot.m00;
        this.m01 = rot.m01;
        this.m02 = rot.m02;
        this.m03 = pos.x;

        this.m10 = rot.m10;
        this.m11 = rot.m11;
        this.m12 = rot.m12;
        this.m13 = pos.y;

        this.m20 = rot.m20;
        this.m21 = rot.m21;
        this.m22 = rot.m22;
        this.m23 = pos.z;

        this.m30 = 0.0D;
        this.m31 = 0.0D;
        this.m32 = 0.0D;
        this.m33 = 1.0D;
    }

    public Matrix4d mult(Matrix4d mat) {
        double temp00 = this.m00 * mat.m00 + this.m01 * mat.m10 + this.m02 * mat.m20 + this.m03 * mat.m30;

        double temp01 = this.m00 * mat.m01 + this.m01 * mat.m11 + this.m02 * mat.m21 + this.m03 * mat.m31;

        double temp02 = this.m00 * mat.m02 + this.m01 * mat.m12 + this.m02 * mat.m22 + this.m03 * mat.m32;

        double temp03 = this.m00 * mat.m03 + this.m01 * mat.m13 + this.m02 * mat.m23 + this.m03 * mat.m33;

        double temp10 = this.m10 * mat.m00 + this.m11 * mat.m10 + this.m12 * mat.m20 + this.m13 * mat.m30;

        double temp11 = this.m10 * mat.m01 + this.m11 * mat.m11 + this.m12 * mat.m21 + this.m13 * mat.m31;

        double temp12 = this.m10 * mat.m02 + this.m11 * mat.m12 + this.m12 * mat.m22 + this.m13 * mat.m32;

        double temp13 = this.m10 * mat.m03 + this.m11 * mat.m13 + this.m12 * mat.m23 + this.m13 * mat.m33;

        double temp20 = this.m20 * mat.m00 + this.m21 * mat.m10 + this.m22 * mat.m20 + this.m23 * mat.m30;

        double temp21 = this.m20 * mat.m01 + this.m21 * mat.m11 + this.m22 * mat.m21 + this.m23 * mat.m31;

        double temp22 = this.m20 * mat.m02 + this.m21 * mat.m12 + this.m22 * mat.m22 + this.m23 * mat.m32;

        double temp23 = this.m20 * mat.m03 + this.m21 * mat.m13 + this.m22 * mat.m23 + this.m23 * mat.m33;

        double temp30 = this.m30 * mat.m00 + this.m31 * mat.m10 + this.m32 * mat.m20 + this.m33 * mat.m30;

        double temp31 = this.m30 * mat.m01 + this.m31 * mat.m11 + this.m32 * mat.m21 + this.m33 * mat.m31;

        double temp32 = this.m30 * mat.m02 + this.m31 * mat.m12 + this.m32 * mat.m22 + this.m33 * mat.m32;

        double temp33 = this.m30 * mat.m03 + this.m31 * mat.m13 + this.m32 * mat.m23 + this.m33 * mat.m33;

        return new Matrix4d(temp00, temp01, temp02, temp03, temp10, temp11, temp12, temp13, temp20, temp21, temp22, temp23, temp30, temp31, temp32, temp33);
    }

    public Vec3d mult(Vec3d v) {
        double x = v.x;
        double y = v.y;
        double z = v.z;

        double xr = this.m00 * x + this.m01 * y + this.m02 * z + this.m03;
        double yr = this.m10 * x + this.m11 * y + this.m12 * z + this.m13;
        double zr = this.m20 * x + this.m21 * y + this.m22 * z + this.m23;

        return new Vec3d(xr, yr, zr);
    }

    public double determinant() {
        double a0 = this.m00 * this.m11 - this.m01 * this.m10;
        double a1 = this.m00 * this.m12 - this.m02 * this.m10;
        double a2 = this.m00 * this.m13 - this.m03 * this.m10;
        double a3 = this.m01 * this.m12 - this.m02 * this.m11;
        double a4 = this.m01 * this.m13 - this.m03 * this.m11;
        double a5 = this.m02 * this.m13 - this.m03 * this.m12;
        double b0 = this.m20 * this.m31 - this.m21 * this.m30;
        double b1 = this.m20 * this.m32 - this.m22 * this.m30;
        double b2 = this.m20 * this.m33 - this.m23 * this.m30;
        double b3 = this.m21 * this.m32 - this.m22 * this.m31;
        double b4 = this.m21 * this.m33 - this.m23 * this.m31;
        double b5 = this.m22 * this.m33 - this.m23 * this.m32;
        return a0 * b5 - a1 * b4 + a2 * b3 + a3 * b2 - a4 * b1 + a5 * b0;
    }

    public Matrix4d invert() {
        double a0 = this.m00 * this.m11 - this.m01 * this.m10;
        double a1 = this.m00 * this.m12 - this.m02 * this.m10;
        double a2 = this.m00 * this.m13 - this.m03 * this.m10;
        double a3 = this.m01 * this.m12 - this.m02 * this.m11;
        double a4 = this.m01 * this.m13 - this.m03 * this.m11;
        double a5 = this.m02 * this.m13 - this.m03 * this.m12;
        double b0 = this.m20 * this.m31 - this.m21 * this.m30;
        double b1 = this.m20 * this.m32 - this.m22 * this.m30;
        double b2 = this.m20 * this.m33 - this.m23 * this.m30;
        double b3 = this.m21 * this.m32 - this.m22 * this.m31;
        double b4 = this.m21 * this.m33 - this.m23 * this.m31;
        double b5 = this.m22 * this.m33 - this.m23 * this.m32;
        double d = a0 * b5 - a1 * b4 + a2 * b3 + a3 * b2 - a4 * b1 + a5 * b0;
        if (d == 0.0D) {
            return new Matrix4d();
        }

        double rm00 = this.m11 * b5 - this.m12 * b4 + this.m13 * b3;
        double rm10 = -this.m10 * b5 + this.m12 * b2 - this.m13 * b1;
        double rm20 = this.m10 * b4 - this.m11 * b2 + this.m13 * b0;
        double rm30 = -this.m10 * b3 + this.m11 * b1 - this.m12 * b0;
        double rm01 = -this.m01 * b5 + this.m02 * b4 - this.m03 * b3;
        double rm11 = this.m00 * b5 - this.m02 * b2 + this.m03 * b1;
        double rm21 = -this.m00 * b4 + this.m01 * b2 - this.m03 * b0;
        double rm31 = this.m00 * b3 - this.m01 * b1 + this.m02 * b0;
        double rm02 = this.m31 * a5 - this.m32 * a4 + this.m33 * a3;
        double rm12 = -this.m30 * a5 + this.m32 * a2 - this.m33 * a1;
        double rm22 = this.m30 * a4 - this.m31 * a2 + this.m33 * a0;
        double rm32 = -this.m30 * a3 + this.m31 * a1 - this.m32 * a0;
        double rm03 = -this.m21 * a5 + this.m22 * a4 - this.m23 * a3;
        double rm13 = this.m20 * a5 - this.m22 * a2 + this.m23 * a1;
        double rm23 = -this.m20 * a4 + this.m21 * a2 - this.m23 * a0;
        double rm33 = this.m20 * a3 - this.m21 * a1 + this.m22 * a0;

        double s = 1.0D / d;

        return new Matrix4d(rm00 * s, rm01 * s, rm02 * s, rm03 * s, rm10 * s, rm11 * s, rm12 * s, rm13 * s, rm20 * s, rm21 * s, rm22 * s, rm23 * s, rm30 * s, rm31 * s, rm32 * s, rm33 * s);
    }

    public Matrix4d transpose() {
        return new Matrix4d(this.m00, this.m10, this.m20, this.m30, this.m01, this.m11, this.m21, this.m31, this.m02, this.m12, this.m22, this.m32, this.m03, this.m13, this.m23, this.m33);
    }

    public Matrix3d toRotationMatrix() {
        return new Matrix3d(this.m00, this.m01, this.m02, this.m10, this.m11, this.m12, this.m20, this.m21, this.m22);
    }

    public String toString() {
        return "Matrix4d[{" + this.m00 + ", " + this.m01 + ", " + this.m02 + ", " + this.m03 + "}, {" + this.m10 + ", " + this.m11 + ", " + this.m12 + ", " + this.m13 + "}, {" + this.m20 + ", " + this.m21 + ", " + this.m22 + ", " + this.m23 + "}, {" + this.m30 + ", " + this.m31 + ", " + this.m32 + ", " + this.m33 + "}]";
    }
}