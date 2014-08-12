package mythruna.geom;

import com.jme3.math.Vector3f;
import mythruna.BlockType;
import mythruna.BoundaryShape;

public abstract interface GeomFactory {

    public abstract int createGeometry(GeomPartBuffer paramGeomPartBuffer, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, float paramFloat1, float paramFloat2, BlockType paramBlockType, int paramInt7);

    public abstract int createInternalGeometry(GeomPartBuffer paramGeomPartBuffer, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, float paramFloat1, float paramFloat2, BlockType paramBlockType);

    public abstract boolean isClipped();

    public abstract boolean isSameShape(GeomFactory paramGeomFactory);

    public abstract boolean isSolid(int paramInt);

    public abstract boolean isSolid();

    public abstract boolean isBoundary(int paramInt);

    public abstract BoundaryShape getBoundaryShape(int paramInt);

    public abstract float getTransparency(int paramInt);

    public abstract double getMassPortion();

    public abstract Vector3f getMin();

    public abstract Vector3f getMax();
}