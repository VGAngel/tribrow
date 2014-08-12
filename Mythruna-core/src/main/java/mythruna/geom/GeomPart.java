package mythruna.geom;

public class GeomPart {

    public static final int PRIM_TRI = 0;
    public static final int PRIM_POINT = 1;
    private int materialType;
    private int dir;
    private int primitiveType;
    private byte primitiveSize;
    private float[] coords;
    private float[] norms;
    private float[] texes;
    private float[] tangents;
    private short[] indexes;
    private float[] colors;
    private float[] sizes;
    private float sun = 1.0F;
    private float localLight = 0.0F;

    public GeomPart(int materialType, int dir) {
        this.materialType = materialType;
        this.dir = dir;
        this.primitiveSize = 3;
        this.primitiveType = 0;
    }

    public GeomPart(int materialType, int dir, int primitiveType) {
        this.materialType = materialType;
        this.dir = dir;
        this.primitiveType = primitiveType;

        switch (primitiveType) {
            case 1:
                this.primitiveSize = 1;
                break;
            default:
                this.primitiveSize = 3;
        }
    }

    public int getMaterialType() {
        return this.materialType;
    }

    public int getDirection() {
        return this.dir;
    }

    public int getPrimitiveType() {
        return this.primitiveType;
    }

    public int getPrimitiveSize() {
        return this.primitiveSize;
    }

    public void setSun(float sun) {
        this.sun = sun;
    }

    public float getSun() {
        return this.sun;
    }

    public void setLight(float l) {
        this.localLight = l;
    }

    public float getLight() {
        return this.localLight;
    }

    public void setCoords(float[] f) {
        this.coords = f;
    }

    public float[] getCoords() {
        return this.coords;
    }

    public int getVertexCount() {
        return this.coords.length / 3;
    }

    public void setNormals(float[] f) {
        this.norms = f;
    }

    public float[] getNormals() {
        return this.norms;
    }

    public void setTangents(float[] f) {
        this.tangents = f;
    }

    public float[] getTangents() {
        return this.tangents;
    }

    public void setTexCoords(float[] f) {
        this.texes = f;
    }

    public float[] getTexCoords() {
        return this.texes;
    }

    public void setIndexes(short[] s) {
        this.indexes = s;
    }

    public void setIndexes(int[] ints) {
        this.indexes = new short[ints.length];
        for (int i = 0; i < this.indexes.length; i++)
            this.indexes[i] = (short) ints[i];
    }

    public short[] getIndexes() {
        return this.indexes;
    }

    public void setColors(float[] f) {
        this.colors = f;
    }

    public float[] getColors() {
        return this.colors;
    }

    public void setSizes(float[] f) {
        this.sizes = f;
    }

    public float[] getSizes() {
        return this.sizes;
    }

    public int getTriangleCount() {
        if (this.indexes != null)
            return this.indexes.length / this.primitiveSize;
        return this.coords.length / 3;
    }
}