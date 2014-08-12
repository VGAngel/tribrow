package mythruna.geom;

import com.jme3.math.Vector3f;
import mythruna.BlockType;
import mythruna.BoundaryShape;
import mythruna.ShapeIndex;

public class CubeFactory implements GeomFactory {

    public static final Vector3f UNIT_CUBE = new Vector3f(1.0F, 1.0F, 1.0F);

    public static final BoundaryShape UNIT_SQUARE = ShapeIndex.getRect(0.0F, 0.0F, 1.0F, 1.0F);

    private int[] types = new int[6];
    private float[] transparency = {0.0F, 0.0F, 0.0F};
    private boolean includeNormals = false;

    public CubeFactory(int materialType) {
        for (int i = 0; i < this.types.length; i++)
            this.types[i] = materialType;
    }

    public CubeFactory(int sideType, int topType, int bottomType) {
        this(sideType);
        this.types[4] = topType;
        this.types[5] = bottomType;
    }

    public CubeFactory(int topType, int bottomType, int[] sides) {
        this(topType);
        for (int i = 0; i < sides.length; i++)
            this.types[i] = sides[i];
    }

    public CubeFactory(float[] light, int topType, int bottomType, int[] sides) {
        this(topType);
        for (int i = 0; i < sides.length; i++)
            this.types[i] = sides[i];
        if (light != null)
            this.transparency = light;
    }

    public boolean isSameShape(GeomFactory f) {
        if (f == this)
            return true;
        if ((f == null) || (f.getClass() != getClass())) {
            return false;
        }
        return true;
    }

    public int createGeometry(GeomPartBuffer buffer, int x, int y, int z, int xWorld, int yWorld, int zWorld, float sun, float light, BlockType block, int dir) {
        int type = this.types[dir];

        buffer.add(GeomUtils.createQuad(x, y, z, Vector3f.ZERO, UNIT_CUBE, this.includeNormals, dir, type, sun, light));

        return 1;
    }

    public int createInternalGeometry(GeomPartBuffer buffer, int x, int y, int z, int xWorld, int yWorld, int zWorld, float sun, float light, BlockType block) {
        return 0;
    }

    public final float getTransparency(int axis) {
        return this.transparency[axis];
    }

    public final boolean isSolid(int direction) {
        return true;
    }

    public final boolean isSolid() {
        return true;
    }

    public final boolean isBoundary(int direction) {
        return true;
    }

    public final BoundaryShape getBoundaryShape(int direction) {
        return UNIT_SQUARE;
    }

    public final Vector3f getMin() {
        return Vector3f.ZERO;
    }

    public final Vector3f getMax() {
        return UNIT_CUBE;
    }

    public final double getMassPortion() {
        double x = getMax().x - getMin().x;
        double y = getMax().y - getMin().y;
        double z = getMax().z - getMin().z;

        return x * y * z;
    }

    public final boolean isClipped() {
        return true;
    }
}