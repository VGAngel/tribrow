package mythruna.geom;

import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import mythruna.BlockType;
import mythruna.BoundaryShape;
import mythruna.ShapeIndex;

public class OuterCornerFactory implements GeomFactory {

    private static final int SIDE_EMPTY = 0;
    private static final int SIDE_SOLID = 1;
    private static final int SIDE_TRI_LEFT = 2;
    private static final int SIDE_TRI_RIGHT = 3;
    private static final BoundaryShape TRI_UP = ShapeIndex.getTriangle(0.0F, 0.0F, 1.0F, 1.0F, -FastMath.sqrt(2.0F), FastMath.sqrt(2.0F));
    private static final BoundaryShape TRI_DOWN = ShapeIndex.getTriangle(0.0F, 0.0F, 1.0F, 1.0F, FastMath.sqrt(2.0F), FastMath.sqrt(2.0F));

    private static Vector3f[] baseVerts = {new Vector3f(0.0F, 0.0F, 0.0F), new Vector3f(0.0F, 0.0F, 1.0F), new Vector3f(1.0F, 0.0F, 1.0F), new Vector3f(1.0F, 0.0F, 0.0F), new Vector3f(0.0F, 1.0F, 1.0F)};

    private static Vector3f[] baseNormals = {new Vector3f(1.0F, 1.0F, 0.0F).normalizeLocal(), new Vector3f(0.0F, 1.0F, -1.0F).normalizeLocal()};

    private static Vector3f[] baseTangents = {new Vector3f(0.0F, 0.0F, -1.0F), new Vector3f(-1.0F, 0.0F, 0.0F)};
    private Vector3f[] verts;
    private Vector3f[] normals;
    private Vector3f[] tangents;
    private int[] sides = new int[6];
    private int triLeftDir;
    private int triRightDir;
    private Quaternion rotation;
    private int type1;
    private int type2;
    private int topType1;
    private int topType2;
    private BoundaryShape[] shapes = new BoundaryShape[6];

    public OuterCornerFactory(int dir, int topType1, int topType2, int type1, int type2) {
        this.type1 = type1;
        this.type2 = type2;
        this.topType1 = topType1;
        this.topType2 = topType2;

        for (int d = 0; d < this.shapes.length; d++) {
            this.shapes[d] = ShapeIndex.NULL_SHAPE;
        }
        this.shapes[5] = CubeFactory.UNIT_SQUARE;

        switch (dir) {
            case 2:
                this.triLeftDir = 1;
                this.triRightDir = 3;

                this.shapes[this.triLeftDir] = TRI_DOWN;
                this.shapes[this.triRightDir] = TRI_DOWN;

                break;
            case 0:
                this.triLeftDir = 2;
                this.triRightDir = 1;

                this.shapes[this.triLeftDir] = TRI_UP;
                this.shapes[this.triRightDir] = TRI_UP;
                break;
            case 3:
                this.triLeftDir = 0;
                this.triRightDir = 2;

                this.shapes[this.triLeftDir] = TRI_UP;
                this.shapes[this.triRightDir] = TRI_UP;
                break;
            case 1:
                this.triLeftDir = 3;
                this.triRightDir = 0;

                this.shapes[this.triLeftDir] = TRI_DOWN;
                this.shapes[this.triRightDir] = TRI_DOWN;
        }

        this.rotation = new Quaternion();
        float xOffset = 0.0F;
        float zOffset = 0.0F;
        switch (dir) {
            case 2:
            default:
                break;
            case 3:
                this.rotation.fromAngles(0.0F, 3.141593F, 0.0F);
                xOffset = 1.0F;
                zOffset = 1.0F;
                break;
            case 0:
                this.rotation.fromAngles(0.0F, 1.570796F, 0.0F);
                zOffset = 1.0F;
                break;
            case 1:
                this.rotation.fromAngles(0.0F, 4.712389F, 0.0F);
                xOffset = 1.0F;
        }

        this.verts = new Vector3f[baseVerts.length];
        for (int i = 0; i < this.verts.length; i++) {
            this.verts[i] = this.rotation.mult(baseVerts[i]);
            this.verts[i].x += xOffset;
            this.verts[i].z += zOffset;
        }

        this.normals = new Vector3f[baseNormals.length];
        for (int i = 0; i < this.normals.length; i++) {
            this.normals[i] = this.rotation.mult(baseNormals[i]);
        }
        this.tangents = new Vector3f[baseTangents.length];
        for (int i = 0; i < this.tangents.length; i++) {
            this.tangents[i] = this.rotation.mult(baseTangents[i]);
        }

        this.sides[5] = 1;
        this.sides[this.triLeftDir] = 2;
        this.sides[this.triRightDir] = 3;
    }

    public boolean isSameShape(GeomFactory f) {
        if (f == this)
            return true;
        if ((f == null) || (f.getClass() != getClass()))
            return false;
        OuterCornerFactory other = (OuterCornerFactory) f;
        if (!this.rotation.equals(other.rotation)) {
            return false;
        }
        return true;
    }

    public int createGeometry(GeomPartBuffer buffer, int x, int y, int z, int xWorld, int yWorld, int zWorld, float sun, float light, BlockType block, int dir) {
        if (this.sides[dir] == 0) {
            return 0;
        }
        if (dir == 5) {
            GeomPart part = new GeomPart(this.type2, dir);
            part.setSun(sun);
            part.setLight(light);

            part.setCoords(new float[]{this.verts[1].x + x, this.verts[1].y + z, this.verts[1].z + y, this.verts[0].x + x, this.verts[0].y + z, this.verts[0].z + y, this.verts[3].x + x, this.verts[3].y + z, this.verts[3].z + y});

            part.setTexCoords(new float[]{1.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F});

            part.setIndexes(new short[]{0, 1, 2});

            buffer.add(part);

            part = new GeomPart(this.type2, dir);
            part.setSun(sun);
            part.setLight(light);

            part.setCoords(new float[]{this.verts[1].x + x, this.verts[1].y + z, this.verts[1].z + y, this.verts[3].x + x, this.verts[3].y + z, this.verts[3].z + y, this.verts[2].x + x, this.verts[2].y + z, this.verts[2].z + y});

            part.setTexCoords(new float[]{0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F});

            part.setIndexes(new short[]{0, 1, 2});

            buffer.add(part);
            return 2;
        }

        if (dir == this.triLeftDir) {
            GeomPart part = new GeomPart(this.type1, dir);
            part.setSun(sun);
            part.setLight(light);

            part.setCoords(new float[]{this.verts[1].x + x, this.verts[1].y + z, this.verts[1].z + y, this.verts[2].x + x, this.verts[2].y + z, this.verts[2].z + y, this.verts[4].x + x, this.verts[4].y + z, this.verts[4].z + y});

            part.setTexCoords(new float[]{0.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F});

            part.setIndexes(new short[]{0, 1, 2});

            buffer.add(part);
            return 1;
        }

        if (dir == this.triRightDir) {
            GeomPart part = new GeomPart(this.type2, dir);
            part.setSun(sun);
            part.setLight(light);

            part.setCoords(new float[]{this.verts[1].x + x, this.verts[1].y + z, this.verts[1].z + y, this.verts[0].x + x, this.verts[0].y + z, this.verts[0].z + y, this.verts[4].x + x, this.verts[4].y + z, this.verts[4].z + y});

            part.setTexCoords(new float[]{0.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F});

            part.setIndexes(new short[]{0, 2, 1});

            buffer.add(part);
            return 1;
        }

        return 0;
    }

    public int createInternalGeometry(GeomPartBuffer buffer, int x, int y, int z, int xWorld, int yWorld, int zWorld, float sun, float light, BlockType block) {
        GeomPart part = new GeomPart(this.topType2, -1);
        part.setSun(sun);
        part.setLight(light);

        part.setCoords(new float[]{this.verts[4].x + x, this.verts[4].y + z, this.verts[4].z + y, this.verts[3].x + x, this.verts[3].y + z, this.verts[3].z + y, this.verts[0].x + x, this.verts[0].y + z, this.verts[0].z + y});

        part.setNormals(new float[]{this.normals[1].x, this.normals[1].y, this.normals[1].z, this.normals[1].x, this.normals[1].y, this.normals[1].z, this.normals[1].x, this.normals[1].y, this.normals[1].z});

        part.setTangents(new float[]{this.tangents[1].x, this.tangents[1].y, this.tangents[1].z, this.tangents[1].x, this.tangents[1].y, this.tangents[1].z, this.tangents[1].x, this.tangents[1].y, this.tangents[1].z});

        part.setTexCoords(new float[]{1.0F, 1.0F, 0.0F, 0.0F, 1.0F, 0.0F});

        part.setIndexes(new short[]{0, 1, 2});

        buffer.add(part);

        part = new GeomPart(this.topType2, -1);
        part.setSun(sun);
        part.setLight(light);

        part.setCoords(new float[]{this.verts[4].x + x, this.verts[4].y + z, this.verts[4].z + y, this.verts[2].x + x, this.verts[2].y + z, this.verts[2].z + y, this.verts[3].x + x, this.verts[3].y + z, this.verts[3].z + y});

        part.setNormals(new float[]{this.normals[0].x, this.normals[0].y, this.normals[0].z, this.normals[0].x, this.normals[0].y, this.normals[0].z, this.normals[0].x, this.normals[0].y, this.normals[0].z});

        part.setTangents(new float[]{this.tangents[0].x, this.tangents[0].y, this.tangents[0].z, this.tangents[0].x, this.tangents[0].y, this.tangents[0].z, this.tangents[0].x, this.tangents[0].y, this.tangents[0].z});

        part.setTexCoords(new float[]{0.0F, 1.0F, 0.0F, 0.0F, 1.0F, 0.0F});

        part.setIndexes(new short[]{0, 1, 2});

        buffer.add(part);

        return 0;
    }

    public boolean isSolid(int direction) {
        return this.sides[direction] == 1;
    }

    public boolean isSolid() {
        return false;
    }

    public boolean isBoundary(int direction) {
        return this.sides[direction] != 0;
    }

    public BoundaryShape getBoundaryShape(int direction) {
        return this.shapes[direction];
    }

    public final float getTransparency(int axis) {
        return 0.0F;
    }

    public Vector3f getMin() {
        return Vector3f.ZERO;
    }

    public Vector3f getMax() {
        return CubeFactory.UNIT_CUBE;
    }

    public double getMassPortion() {
        double x = getMax().x - getMin().x;
        double y = getMax().y - getMin().y;
        double z = getMax().z - getMin().z;

        return x * y * z * 0.5D;
    }

    public boolean isClipped() {
        return true;
    }
}