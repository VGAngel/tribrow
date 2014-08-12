package mythruna;

public abstract interface BoundaryShape {

    public abstract boolean isMatchingFace(BoundaryShape paramBoundaryShape);

    public abstract float getArea();

    public abstract BoundaryShape rotate(int paramInt1, int paramInt2);
}