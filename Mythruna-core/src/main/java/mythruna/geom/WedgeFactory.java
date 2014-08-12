package mythruna.geom;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import mythruna.BlockType;
import mythruna.BoundaryShape;
import mythruna.ShapeIndex;

public class WedgeFactory implements GeomFactory {

    private static final int SIDE_EMPTY = 0;
    private static final int SIDE_SOLID = 1;
    private static final int SIDE_TRI_LEFT = 2;
    private static final int SIDE_TRI_RIGHT = 3;
    private static final BoundaryShape TRI_UP = ShapeIndex.getTriangle(0.0F, 0.0F, 1.0F, 1.0F, -FastMath.sqrt(2.0F), FastMath.sqrt(2.0F));
    private static final BoundaryShape TRI_DOWN = ShapeIndex.getTriangle(0.0F, 0.0F, 1.0F, 1.0F, FastMath.sqrt(2.0F), FastMath.sqrt(2.0F));

    private static final BoundaryShape TRI_UP_INV = ShapeIndex.getTriangle(0.0F, 0.0F, 1.0F, 1.0F, FastMath.sqrt(2.0F), -FastMath.sqrt(2.0F));
    private static final BoundaryShape TRI_DOWN_INV = ShapeIndex.getTriangle(0.0F, 0.0F, 1.0F, 1.0F, -FastMath.sqrt(2.0F), -FastMath.sqrt(2.0F));

    private int[] types = new int[6];
    private int[] sides = new int[6];
    private BoundaryShape[] shapes = new BoundaryShape[6];
    private int solidDir;
    private boolean facingUp;
    private boolean flipTexture = false;
    private CubeFactory solid;

    public WedgeFactory(int solidSideXY, int solidSideZ, int materialType) {
        this.solid = new CubeFactory(materialType);
        this.solidDir = solidSideXY;
        this.sides[solidSideXY] = 1;
        this.sides[solidSideZ] = 1;
        this.sides[mythruna.Direction.LEFT[solidSideXY]] = 2;
        this.sides[mythruna.Direction.INVERSE[mythruna.Direction.LEFT[solidSideXY]]] = 3;

        this.facingUp = (solidSideZ == 5);

        for (int i = 0; i < this.types.length; i++) {
            this.types[i] = materialType;
            this.shapes[i] = ShapeIndex.NULL_SHAPE;
        }

        this.shapes[solidSideXY] = CubeFactory.UNIT_SQUARE;
        this.shapes[solidSideZ] = CubeFactory.UNIT_SQUARE;

        switch (solidSideXY) {
            case 0:
                if (solidSideZ == 5) {
                    this.shapes[2] = TRI_DOWN;
                    this.shapes[3] = TRI_DOWN;
                } else {
                    this.shapes[2] = TRI_UP_INV;
                    this.shapes[3] = TRI_UP_INV;
                }
                break;
            case 1:
                if (solidSideZ == 5) {
                    this.shapes[2] = TRI_UP;
                    this.shapes[3] = TRI_UP;
                } else {
                    this.shapes[2] = TRI_DOWN_INV;
                    this.shapes[3] = TRI_DOWN_INV;
                }
                break;
            case 2:
                if (solidSideZ == 5) {
                    this.shapes[0] = TRI_UP;
                    this.shapes[1] = TRI_UP;
                } else {
                    this.shapes[0] = TRI_DOWN_INV;
                    this.shapes[1] = TRI_DOWN_INV;
                }
                break;
            case 3:
                if (solidSideZ == 5) {
                    this.shapes[0] = TRI_DOWN;
                    this.shapes[1] = TRI_DOWN;
                } else {
                    this.shapes[0] = TRI_UP_INV;
                    this.shapes[1] = TRI_UP_INV;
                }
        }
    }

    public WedgeFactory(int solidSideXY, int solidSideZ, int sideType, int topType, int bottomType) {
        this(solidSideXY, solidSideZ, sideType, topType, bottomType, false);
    }

    public WedgeFactory(int solidSideXY, int solidSideZ, int sideType, int topType, int bottomType, boolean yFlip) {
        this(solidSideXY, solidSideZ, sideType);
        this.types[4] = topType;
        this.types[5] = bottomType;
        this.solid = new CubeFactory(sideType, topType, bottomType);
        this.flipTexture = yFlip;
    }

    public boolean isSameShape(GeomFactory f) {
        if (f == this)
            return true;
        if ((f == null) || (f.getClass() != getClass()))
            return false;
        WedgeFactory other = (WedgeFactory) f;
        if (other.facingUp != this.facingUp)
            return false;
        if (other.solidDir != this.solidDir)
            return false;
        return true;
    }

    public int getSolidDir() {
        return this.solidDir;
    }

    public boolean isFacingUp() {
        return this.facingUp;
    }

    public int createGeometry(GeomPartBuffer buffer, int x, int y, int z, int xWorld, int yWorld, int zWorld, float sun, float light, BlockType block, int dir) {
        if (this.sides[dir] == 1) {
            return this.solid.createGeometry(buffer, x, y, z, xWorld, yWorld, zWorld, sun, light, block, dir);
        }
        if ((dir == 5) || (dir == 4)) {
            return 0;
        }
        if (this.sides[dir] == 0) {
            return 0;
        }
        boolean up = this.sides[4] != 1;

        GeomPart part = new GeomPart(this.types[dir], dir);
        part.setSun(sun);
        part.setLight(light);

        float x1 = 0.0F;
        float x2 = 1.0F;
        float yPoint = up ? 0.0F : 1.0F;
        float z1 = 0.0F;
        float z2 = 0.0F;
        float n1 = 0.0F;
        float n2 = 0.0F;

        switch (dir) {
            case 0:
                x1 = 1.0F;
                x2 = 0.0F;
                n1 = 0.0F;
                n2 = -1.0F;
                break;
            case 1:
                x1 = 0.0F;
                x2 = 1.0F;
                z1 = 1.0F;
                z2 = 1.0F;
                n1 = 0.0F;
                n2 = 1.0F;
                break;
            case 2:
                x1 = 1.0F;
                x2 = 1.0F;
                z1 = 1.0F;
                z2 = 0.0F;
                n1 = 1.0F;
                n2 = 0.0F;
                break;
            case 3:
                x1 = 0.0F;
                x2 = 0.0F;
                z1 = 0.0F;
                z2 = 1.0F;
                n1 = -1.0F;
                n2 = 0.0F;
        }

        if (this.sides[dir] == 2) {
            part.setCoords(new float[]{x + x1, z, y + z1, x + x2, z + yPoint, y + z2, x + x1, z + 1, y + z1});

            part.setTexCoords(new float[]{0.0F, 0.0F, 1.0F, 1.0F - yPoint, 0.0F, 1.0F});
        } else if (this.sides[dir] == 3) {
            part.setCoords(new float[]{x + x1, z + yPoint, y + z1, x + x2, z, y + z2, x + x2, z + 1, y + z2});

            part.setTexCoords(new float[]{0.0F, 1.0F - yPoint, 1.0F, 0.0F, 1.0F, 1.0F});
        }

        float[] tangents = mythruna.Direction.TANGENTS[dir];

        part.setIndexes(new short[]{0, 1, 2});

        buffer.add(part);

        return 1;
    }

    public int createInternalGeometry(GeomPartBuffer buffer, int x, int y, int z, int xWorld, int yWorld, int zWorld, float sun, float light, BlockType block) {
        boolean up = this.sides[4] != 1;
        int type = this.types[5];

        GeomPart part = new GeomPart(type, -1);
        part.setSun(sun);
        part.setLight(light);

        float y1 = 0.0F;
        float y2 = 0.0F;
        float y3 = 0.0F;
        float y4 = 0.0F;
        float n = FastMath.cos(0.7853982F);
        float nx = 0.0F;
        float nz = 0.0F;

        float xTangent = 0.0F;
        float yTangent = 0.0F;
        float zTangent = 0.0F;

        switch (this.solidDir) {
            case 0:
                y3 = 1.0F;
                y4 = 1.0F;
                nx = 0.0F;
                nz = n;
                xTangent = 1.0F;
                break;
            case 1:
                y1 = 1.0F;
                y2 = 1.0F;
                nx = 0.0F;
                nz = -n;
                xTangent = 1.0F;
                break;
            case 2:
                y2 = 1.0F;
                y3 = 1.0F;
                nx = -n;
                nz = 0.0F;
                xTangent = n;
                yTangent = n;
                zTangent = 0.0F;
                break;
            case 3:
                y4 = 1.0F;
                y1 = 1.0F;
                nx = n;
                nz = 0.0F;
                xTangent = n;
                yTangent = -n;
        }

        if (up) {
            part.setCoords(new float[]{x, z + y1, y + 1, x + 1, z + y2, y + 1, x + 1, z + y3, y, x, z + y4, y});

            part.setNormals(new float[]{nx, n, nz, nx, n, nz, nx, n, nz, nx, n, nz});
        } else {
            part.setCoords(new float[]{x + 1, z + 1 - y2, y + 1, x, z + 1 - y1, y + 1, x, z + 1 - y4, y, x + 1, z + 1 - y3, y});

            part.setNormals(new float[]{nx, -n, nz, nx, -n, nz, nx, -n, nz, nx, -n, nz});

            yTangent = -yTangent;
            xTangent = -xTangent;
        }

        if (!this.flipTexture) {
            part.setTangents(new float[]{xTangent, yTangent, zTangent, xTangent, yTangent, zTangent, xTangent, yTangent, zTangent, xTangent, yTangent, zTangent});

            part.setTexCoords(new float[]{0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F});

            part.setIndexes(new short[]{0, 1, 2, 0, 2, 3});
        } else {
            part.setTangents(new float[]{-xTangent, -yTangent, -zTangent, -xTangent, -yTangent, -zTangent, -xTangent, -yTangent, -zTangent, -xTangent, -yTangent, -zTangent});

            part.setTexCoords(new float[]{1.0F, 1.0F, 0.0F, 1.0F, 0.0F, 0.0F, 1.0F, 0.0F});

            part.setIndexes(new short[]{2, 3, 0, 2, 0, 1});
        }

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