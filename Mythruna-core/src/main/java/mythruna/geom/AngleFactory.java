package mythruna.geom;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import mythruna.BlockType;
import mythruna.BoundaryShape;
import mythruna.ShapeIndex;

public class AngleFactory implements GeomFactory {

    private static final int SIDE_EMPTY = 0;
    private static final int SIDE_SOLID = 1;
    private static final int SIDE_TRI_TOP = 2;
    private static final int SIDE_TRI_BOTTOM = 3;
    private static final float[][] triangles = {{0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F}, {1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F}, {1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 1.0F}, {0.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F}};

    private static final float[][] texCoords = {{0.0F, 0.0F, 1.0F, 1.0F, 1.0F, 0.0F}, {1.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F}, {0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F}, {1.0F, 1.0F, 0.0F, 1.0F, 0.0F, 0.0F}};

    private static final float[][] unstretchedTexCoords = {{0.0F, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F}, {0.0F, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F}, {1.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F}, {0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 1.0F}};

    private static final float COS45 = FastMath.cos(0.7853982F);
    private static final float[][] normals = {{COS45, 0.0F, COS45}, {-COS45, 0.0F, -COS45}, {-COS45, 0.0F, COS45}, {COS45, 0.0F, -COS45}};

    private static final float[][] tangents = {{COS45, 0.0F, -COS45}, {-COS45, 0.0F, COS45}, {COS45, 0.0F, COS45}, {-COS45, 0.0F, -COS45}};

    private int[] types = new int[6];
    private int interiorType;
    private int[] sides = new int[6];
    private int solidDir1;
    private int solidDir2;
    private BoundaryShape[] shapes = new BoundaryShape[6];
    private CubeFactory solid;
    private boolean unstretched = true;

    public AngleFactory(int solidSide1, int solidSide2, int materialType) {
        this.solid = new CubeFactory(materialType);
        this.solidDir1 = solidSide1;
        this.solidDir2 = solidSide2;
        this.sides[solidSide1] = 1;
        this.sides[solidSide2] = 1;
        this.sides[4] = 2;
        this.sides[5] = 3;

        for (int i = 0; i < this.types.length; i++) {
            this.types[i] = materialType;
        }
        this.interiorType = materialType;
        setupShapes(solidSide1, solidSide2);
    }

    public AngleFactory(int solidSide1, int solidSide2, int materialType, int interiorMaterial) {
        this.solid = new CubeFactory(materialType);
        this.solidDir1 = solidSide1;
        this.solidDir2 = solidSide2;
        this.sides[solidSide1] = 1;
        this.sides[solidSide2] = 1;
        this.sides[4] = 2;
        this.sides[5] = 3;

        for (int i = 0; i < this.types.length; i++) {
            this.types[i] = materialType;
        }
        this.interiorType = interiorMaterial;
        setupShapes(solidSide1, solidSide2);
    }

    public AngleFactory(int solidSide1, int solidSide2, int materialType, int topMaterial, int bottomMaterial, int interiorType) {
        this.solid = new CubeFactory(materialType);
        this.solidDir1 = solidSide1;
        this.solidDir2 = solidSide2;
        this.sides[solidSide1] = 1;
        this.sides[solidSide2] = 1;
        this.sides[4] = 2;
        this.sides[5] = 3;

        for (int i = 0; i < this.types.length; i++) {
            this.types[i] = materialType;
        }
        this.types[4] = topMaterial;
        this.types[5] = bottomMaterial;
        this.interiorType = interiorType;
        setupShapes(solidSide1, solidSide2);
    }

    public int getDirection() {
        return this.solidDir1;
    }

    public boolean isSameShape(GeomFactory f) {
        if (f == this)
            return true;
        if ((f == null) || (f.getClass() != getClass()))
            return false;
        AngleFactory other = (AngleFactory) f;
        if (other.solidDir1 != this.solidDir1)
            return false;
        if (other.solidDir2 != this.solidDir2)
            return false;
        return true;
    }

    protected void setupShapes(int solidSide1, int solidSide2) {
        for (int d = 0; d < this.shapes.length; d++)
            this.shapes[d] = ShapeIndex.NULL_SHAPE;
        this.shapes[solidSide1] = CubeFactory.UNIT_SQUARE;
        this.shapes[solidSide2] = CubeFactory.UNIT_SQUARE;

        float[] normal = normals[solidSide1];

        this.shapes[4] = ShapeIndex.getTriangle(0.0F, 0.0F, 1.0F, 1.0F, normal[0], normal[2]);
        this.shapes[5] = this.shapes[4];
    }

    public int createGeometry(GeomPartBuffer buffer, int x, int y, int z, int xWorld, int yWorld, int zWorld, float sun, float light, BlockType block, int dir) {
        if (this.sides[dir] == 1) {
            return this.solid.createGeometry(buffer, x, y, z, xWorld, yWorld, zWorld, sun, light, block, dir);
        }

        if (this.sides[dir] == 0) {
            return 0;
        }

        GeomPart part = new GeomPart(this.types[dir], dir);
        part.setSun(sun);
        part.setLight(light);

        float yFace = 0.0F;
        if (dir == 4) {
            yFace = 1.0F;
        }
        float[] tri = triangles[this.solidDir1];
        float[] tex;
        if (this.unstretched)
            tex = unstretchedTexCoords[this.solidDir1];
        else
            tex = texCoords[this.solidDir1];
        float[] norms = mythruna.Direction.NORMALS[dir];
        float[] tangents = mythruna.Direction.TANGENTS[dir];
        part.setCoords(new float[]{x + tri[0], z + yFace, y + tri[2], x + tri[3], z + yFace, y + tri[5], x + tri[6], z + yFace, y + tri[8]});

        part.setTexCoords(tex);

        if (dir == 4)
            part.setIndexes(new short[]{0, 1, 2});
        else {
            part.setIndexes(new short[]{0, 2, 1});
        }
        buffer.add(part);
        return 1;
    }

    public int createInternalGeometry(GeomPartBuffer buffer, int x, int y, int z, int xWorld, int yWorld, int zWorld, float sun, float light, BlockType block) {
        float[] tri = triangles[this.solidDir1];

        GeomPart part = new GeomPart(this.interiorType, -1);
        part.setSun(sun);
        part.setLight(light);

        float n = FastMath.cos(0.7853982F);

        part.setCoords(new float[]{x + tri[3], z, y + tri[5], x + tri[6], z, y + tri[8], x + tri[6], z + 1, y + tri[8], x + tri[3], z + 1, y + tri[5]});

        part.setTexCoords(new float[]{0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F});

        float[] normal = normals[this.solidDir1];
        float[] tangent = tangents[this.solidDir1];

        part.setNormals(new float[]{normal[0], normal[1], normal[2], normal[0], normal[1], normal[2], normal[0], normal[1], normal[2], normal[0], normal[1], normal[2]});

        part.setTangents(new float[]{tangent[0], tangent[1], tangent[2], tangent[0], tangent[1], tangent[2], tangent[0], tangent[1], tangent[2], tangent[0], tangent[1], tangent[2]});

        part.setIndexes(new short[]{0, 1, 2, 0, 2, 3});

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

    private float dot(Vector3f origin, float[] triOrigin, float[] dir, float x, float y) {
        x -= origin.x;
        y -= origin.y;

        x -= triOrigin[0];
        y -= triOrigin[2];

        return dir[0] * x + dir[2] * y;
    }

    public boolean isClipped() {
        return true;
    }
}