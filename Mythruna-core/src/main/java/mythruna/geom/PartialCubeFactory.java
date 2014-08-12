package mythruna.geom;

import com.jme3.math.Vector3f;
import mythruna.BlockType;
import mythruna.BoundaryShape;
import mythruna.ShapeIndex;

public class PartialCubeFactory implements GeomFactory {

    private Vector3f min;
    private Vector3f max;
    private int[] types = new int[6];
    private boolean[] solid = new boolean[6];
    private boolean[] boundary = new boolean[6];
    private float[] transparency = {0.0F, 0.0F, 0.0F};
    private BoundaryShape[] shapes = new BoundaryShape[6];
    private boolean clip;
    private boolean includeNormals = false;

    public PartialCubeFactory(Vector3f min, Vector3f max, boolean clip, boolean includeNormals, float[] light, int sideType, int topType, int bottomType, int[] matTypes) {
        this.min = min;
        this.max = max;
        this.clip = clip;
        this.includeNormals = includeNormals;
        for (int i = 0; i < this.types.length; i++)
            this.types[i] = sideType;
        this.types[4] = topType;
        this.types[5] = bottomType;

        for (int i = 0; i < matTypes.length; i++) {
            this.types[i] = matTypes[i];
        }
        if (light != null) {
            this.transparency = light;
        }

        boundary[4] = max.z == 1.0F;
        boundary[5] = min.z == 0.0F;
        boundary[0] = min.y == 0.0F;
        boundary[1] = max.y == 1.0F;
        boundary[2] = max.x == 1.0F;
        boundary[3] = min.x == 0.0F;

        float x = max.x - min.x;
        float y = max.y - min.y;
        float z = max.z - min.z;
        if (this.transparency[0] == 0.0F)
            this.transparency[0] = (1.0F - z * y);
        if (this.transparency[1] == 0.0F)
            this.transparency[1] = (1.0F - z * x);
        if (this.transparency[2] == 0.0F) {
            this.transparency[2] = (1.0F - x * y);
        }

        if ((max.z == 1.0F) && (min.x == 0.0F) && (max.x == 1.0F) && (min.y == 0.0F) && (max.y == 1.0F)) {
            this.solid[4] = true;
        }
        if ((min.z == 0.0F) && (min.x == 0.0F) && (max.x == 1.0F) && (min.y == 0.0F) && (max.y == 1.0F)) {
            this.solid[5] = true;
        }

        if ((min.y == 0.0F) && (min.x == 0.0F) && (max.x == 1.0F) && (min.z == 0.0F) && (max.z == 1.0F)) {
            this.solid[0] = true;
        }
        if ((max.y == 1.0F) && (min.x == 0.0F) && (max.x == 1.0F) && (min.z == 0.0F) && (max.z == 1.0F)) {
            this.solid[1] = true;
        }

        if ((max.x == 1.0F) && (min.y == 0.0F) && (max.y == 1.0F) && (min.z == 0.0F) && (max.z == 1.0F)) {
            this.solid[2] = true;
        }
        if ((min.x == 0.0F) && (min.y == 0.0F) && (max.y == 1.0F) && (min.z == 0.0F) && (max.z == 1.0F)) {
            this.solid[3] = true;
        }
        if (this.boundary[4])
            this.shapes[4] = ShapeIndex.getRect(min.x, min.y, max.x, max.y);
        else {
            this.shapes[4] = ShapeIndex.NULL_SHAPE;
        }
        if (this.boundary[5])
            this.shapes[5] = ShapeIndex.getRect(min.x, min.y, max.x, max.y);
        else {
            this.shapes[5] = ShapeIndex.NULL_SHAPE;
        }
        if (this.boundary[0])
            this.shapes[0] = ShapeIndex.getRect(min.x, min.z, max.x, max.z);
        else {
            this.shapes[0] = ShapeIndex.NULL_SHAPE;
        }
        if (this.boundary[1])
            this.shapes[1] = ShapeIndex.getRect(min.x, min.z, max.x, max.z);
        else {
            this.shapes[1] = ShapeIndex.NULL_SHAPE;
        }
        if (this.boundary[2])
            this.shapes[2] = ShapeIndex.getRect(min.y, min.z, max.y, max.z);
        else {
            this.shapes[2] = ShapeIndex.NULL_SHAPE;
        }
        if (this.boundary[3])
            this.shapes[3] = ShapeIndex.getRect(min.y, min.z, max.y, max.z);
        else
            this.shapes[3] = ShapeIndex.NULL_SHAPE;
    }

    public boolean isSameShape(GeomFactory f) {
        if (f == this)
            return true;
        if ((f == null) || (f.getClass() != getClass()))
            return false;
        PartialCubeFactory other = (PartialCubeFactory) f;
        if (!this.min.equals(other.min))
            return false;
        if (!this.max.equals(other.max)) {
            return false;
        }
        return true;
    }

    public int createGeometry(GeomPartBuffer buffer, int x, int y, int z, int xWorld, int yWorld, int zWorld, float sun, float light, BlockType block, int dir) {
        int type = this.types[dir];

        int count = 0;

        if (this.boundary[dir]) {
            buffer.add(GeomUtils.createQuad(x, y, z, this.min, this.max, this.includeNormals, dir, type, sun, light));

            count++;
        }

        return count;
    }

    public int createInternalGeometry(GeomPartBuffer buffer, int x, int y, int z, int xWorld, int yWorld, int zWorld, float sun, float light, BlockType block) {
        int count = 0;

        for (int i = 0; i < 6; i++) {
            if (!this.boundary[i]) {
                buffer.add(GeomUtils.createQuad(x, y, z, this.min, this.max, this.includeNormals, i, this.types[i], sun, light));

                count++;
            }
        }

        return count;
    }

    public boolean isClipped() {
        return this.clip;
    }

    public boolean isSolid() {
        return false;
    }

    public boolean isSolid(int direction) {
        if (direction < 0)
            return false;
        return this.solid[direction];
    }

    public boolean isBoundary(int direction) {
        if (direction < 0)
            return false;
        return this.boundary[direction];
    }

    public BoundaryShape getBoundaryShape(int direction) {
        return this.shapes[direction];
    }

    public final float getTransparency(int axis) {
        return this.transparency[axis];
    }

    public Vector3f getMin() {
        return this.min;
    }

    public Vector3f getMax() {
        return this.max;
    }

    public double getMassPortion() {
        double x = getMax().x - getMin().x;
        double y = getMax().y - getMin().y;
        double z = getMax().z - getMin().z;

        return x * y * z;
    }
}