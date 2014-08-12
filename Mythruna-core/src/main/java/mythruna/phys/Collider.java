package mythruna.phys;

import mythruna.mathd.Vec3d;

public abstract interface Collider {

    public abstract String getName();

    public abstract Contact getContact(Vec3d paramVec3d1, Vec3d paramVec3d2, double paramDouble, int paramInt1, int paramInt2);

    public abstract Collider rotate(int paramInt);
}