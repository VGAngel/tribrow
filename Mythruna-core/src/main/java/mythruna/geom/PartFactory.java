package mythruna.geom;

import com.jme3.math.Vector3f;
import mythruna.BlockType;
import mythruna.BoundaryShape;

public abstract interface PartFactory {

    public abstract int addParts(GeomPartBuffer paramGeomPartBuffer, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, float paramFloat1, float paramFloat2, BlockType paramBlockType, int paramInt7);

    public abstract BoundaryShape getBoundaryShape();

    public abstract Vector3f getMin();

    public abstract Vector3f getMax();
}