package mythruna.geom;

import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import mythruna.BlockType;
import mythruna.BoundaryShape;
import mythruna.ShapeIndex;

public class ThatchFactory implements GeomFactory {

    private static float COS_45 = FastMath.cos(0.7853982F);
    private static float SIN_45 = FastMath.sin(0.7853982F);

    private static Vector3f min = new Vector3f(0.0F, 0.0F, 0.0F);
    private static Vector3f max = new Vector3f(1.0F, 1.0F, 1.0F);
    private int materialType;
    private int innerMaterialType;
    private int dir;
    private static final int ATLAS_SIZE = 1;
    private float[][] yTexOffsets = {{0.0F, 1.0F}};

    private float[][] xTexOffsets = {{0.0F, 1.0F}};

    private float[] normals = {0.0F, 1.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 1.0F, 0.0F};

    public ThatchFactory(int materialType, int innerMaterialType, int dir) {
        this.materialType = materialType;
        this.innerMaterialType = innerMaterialType;
        this.dir = dir;
    }

    public boolean isSameShape(GeomFactory f) {
        if (f == this)
            return true;
        if ((f == null) || (f.getClass() != getClass()))
            return false;
        ThatchFactory other = (ThatchFactory) f;
        if (this.dir != other.dir) {
            return false;
        }
        return true;
    }

    public int createGeometry(GeomPartBuffer buffer, int x, int y, int z, int xWorld, int yWorld, int zWorld, float sun, float light, BlockType block, int dir) {
        return 0;
    }

    public int getDirection() {
        return this.dir;
    }

    protected float perturb(GeomPartBuffer buffer) {
        return (float) (buffer.nextRandom() * 0.3D) - 0.15F;
    }

    public int createInternalGeometry(GeomPartBuffer buffer, int x, int y, int z, int xWorld, int yWorld, int zWorld, float sun, float light, BlockType block) {
        return createInternalGeometryPoly(buffer, x, y, z, xWorld, yWorld, zWorld, sun, light, block);
    }

    public int createInternalGeometryPoly(GeomPartBuffer buffer, int x, int y, int z, int xWorld, int yWorld, int zWorld, float sun, float light, BlockType block) {
        float innerSize = 0.5F;
        float outerSize = 0.5F;

        float depth = 0.9F;
        float dirSplay = 0.1F;

        float dirSplay2 = 0.9F;
        float dirSplay3 = 0.1F;
        float sideSplay = 0.2F;
        float nest = 0.1F;
        float tinyNest = 0.01F;

        float shorten = 0.7F;
        float fudge = 0.0F;

        float flapOffset = 0.4F;
        float flapOffset2 = 0.2F;
        float flapOffset3 = 0.2F;

        float lowerShorten = 0.9F;

        float xCenter = x + 0.5F;
        float yCenter = z + 0.5F;
        float zCenter = y + 0.5F;

        float angle = 0.7853982F;
        float cos = FastMath.cos(angle);
        float sin = FastMath.sin(angle);

        Vector3f innerMin = new Vector3f(-innerSize, -innerSize, -innerSize);
        Vector3f innerMax = new Vector3f(innerSize, innerSize, innerSize);

        Vector3f outerMin = new Vector3f(-outerSize - depth - dirSplay + shorten, -outerSize - depth - dirSplay2 + lowerShorten, -outerSize - sideSplay);

        Vector3f outerMax = new Vector3f(outerSize - depth + dirSplay2 + dirSplay3, outerSize - depth + dirSplay, outerSize + sideSplay);

        Vector3f[] verts = {new Vector3f(innerMin.x, innerMax.y, innerMax.z), new Vector3f(innerMax.x, innerMin.y, innerMax.z), new Vector3f(innerMax.x, innerMin.y, innerMin.z), new Vector3f(innerMin.x, innerMax.y, innerMin.z), new Vector3f(outerMin.x, outerMax.y + tinyNest, outerMax.z + nest), new Vector3f(outerMax.x + nest, outerMin.y, outerMax.z - nest), new Vector3f(outerMax.x, outerMin.y, outerMin.z + nest), new Vector3f(outerMin.x, outerMax.y - tinyNest, outerMin.z - nest), new Vector3f(outerMin.x + flapOffset2, outerMax.y + tinyNest + flapOffset, outerMax.z + nest - flapOffset3), new Vector3f(outerMin.x + flapOffset2, outerMax.y - tinyNest + flapOffset, outerMin.z - nest + flapOffset3)};

        Vector3f innerNormal = new Vector3f(0.2F, 1.0F, 0.0F);
        Vector3f innerNormal2 = new Vector3f(0.0F, 1.0F, 0.0F);
        Vector3f outerNormal = new Vector3f(0.2F, -0.2F, 2.0F);
        Vector3f outerNormal2 = new Vector3f(0.2F, 0.2F, 2.0F);
        innerNormal.normalizeLocal();
        outerNormal.normalizeLocal();
        outerNormal2.normalizeLocal();

        Vector3f[] normals = {new Vector3f(innerNormal2.x, innerNormal2.y, innerNormal2.z), new Vector3f(innerNormal.x, innerNormal.y, innerNormal.z), new Vector3f(innerNormal.x, innerNormal.y, innerNormal.z), new Vector3f(innerNormal2.x, innerNormal2.y, innerNormal2.z), new Vector3f(-outerNormal2.x, outerNormal2.y, outerNormal2.z), new Vector3f(outerNormal.x, outerNormal.y, outerNormal.z), new Vector3f(outerNormal.x, outerNormal.y, -outerNormal.z), new Vector3f(-outerNormal2.x, outerNormal2.y, -outerNormal2.z), new Vector3f(-outerNormal2.x, outerNormal2.y, outerNormal2.z), new Vector3f(-outerNormal2.x, outerNormal2.y, -outerNormal2.z), new Vector3f(-innerNormal2.x, -innerNormal2.y, -innerNormal2.z), new Vector3f(-innerNormal.x, -innerNormal.y, -innerNormal.z), new Vector3f(-innerNormal.x, -innerNormal.y, -innerNormal.z), new Vector3f(-innerNormal2.x, -innerNormal2.y, -innerNormal2.z), new Vector3f(outerNormal2.x, -outerNormal2.y, -outerNormal2.z), new Vector3f(-outerNormal.x, -outerNormal.y, -outerNormal.z), new Vector3f(-outerNormal.x, -outerNormal.y, outerNormal.z), new Vector3f(outerNormal2.x, -outerNormal2.y, outerNormal2.z), new Vector3f(outerNormal2.x, -outerNormal2.y, -outerNormal2.z), new Vector3f(outerNormal2.x, -outerNormal2.y, outerNormal2.z)};

        Quaternion rotation = null;
        switch (this.dir) {
            case 2:
            default:
                break;
            case 3:
                rotation = new Quaternion();
                rotation.fromAngles(0.0F, 3.141593F, 0.0F);
                break;
            case 0:
                rotation = new Quaternion();
                rotation.fromAngles(0.0F, 1.570796F, 0.0F);
                break;
            case 1:
                rotation = new Quaternion();
                rotation.fromAngles(0.0F, 4.712389F, 0.0F);
        }

        if (rotation != null) {
            for (Vector3f v : verts) {
                Vector3f result = rotation.mult(v);
                v.set(result);
            }

            for (Vector3f v : normals) {
                Vector3f result = rotation.mult(v);
                v.set(result);
            }

        }

        GeomPart part = new GeomPart(this.materialType, this.dir);
        part.setSun(sun);
        part.setLight(light);

        part.setCoords(new float[]{xCenter + verts[0].x, yCenter + verts[0].y, zCenter + verts[0].z, xCenter + verts[1].x, yCenter + verts[1].y, zCenter + verts[1].z, xCenter + verts[2].x, yCenter + verts[2].y, zCenter + verts[2].z, xCenter + verts[3].x, yCenter + verts[3].y, zCenter + verts[3].z, xCenter + verts[4].x, yCenter + verts[4].y, zCenter + verts[4].z, xCenter + verts[5].x, yCenter + verts[5].y, zCenter + verts[5].z, xCenter + verts[6].x, yCenter + verts[6].y, zCenter + verts[6].z, xCenter + verts[7].x, yCenter + verts[7].y, zCenter + verts[7].z, xCenter + verts[8].x, yCenter + verts[8].y, zCenter + verts[8].z, xCenter + verts[9].x, yCenter + verts[9].y, zCenter + verts[9].z});

        part.setNormals(new float[]{normals[0].x, normals[0].y, normals[0].z, normals[1].x, normals[1].y, normals[1].z, normals[2].x, normals[2].y, normals[2].z, normals[3].x, normals[3].y, normals[3].z, normals[4].x, normals[4].y, normals[4].z, normals[5].x, normals[5].y, normals[5].z, normals[6].x, normals[6].y, normals[6].z, normals[7].x, normals[7].y, normals[7].z, normals[8].x, normals[8].y, normals[8].z, normals[9].x, normals[9].y, normals[9].z});

        float textureOffset = 0.2F;
        part.setTexCoords(new float[]{0.5F - textureOffset, 0.5F - textureOffset, 0.5F + textureOffset, 0.5F - textureOffset, 0.5F + textureOffset, 0.5F + textureOffset, 0.5F - textureOffset, 0.5F + textureOffset, 0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 1.0F});

        part.setIndexes(new short[]{0, 1, 2, 0, 2, 3, 0, 5, 1, 0, 4, 5, 1, 5, 2, 2, 5, 6, 6, 7, 3, 2, 6, 3, 3, 9, 0, 0, 9, 8});

        buffer.add(part);

        part = new GeomPart(this.innerMaterialType, this.dir);
        part.setSun(sun);
        part.setLight(light);

        part.setCoords(new float[]{xCenter + verts[0].x, yCenter + verts[0].y, zCenter + verts[0].z, xCenter + verts[1].x, yCenter + verts[1].y, zCenter + verts[1].z, xCenter + verts[2].x, yCenter + verts[2].y, zCenter + verts[2].z, xCenter + verts[3].x, yCenter + verts[3].y, zCenter + verts[3].z, xCenter + verts[4].x, yCenter + verts[4].y, zCenter + verts[4].z, xCenter + verts[5].x, yCenter + verts[5].y, zCenter + verts[5].z, xCenter + verts[6].x, yCenter + verts[6].y, zCenter + verts[6].z, xCenter + verts[7].x, yCenter + verts[7].y, zCenter + verts[7].z, xCenter + verts[8].x, yCenter + verts[8].y, zCenter + verts[8].z, xCenter + verts[9].x, yCenter + verts[9].y, zCenter + verts[9].z});

        part.setNormals(new float[]{normals[10].x, normals[10].y, normals[10].z, normals[11].x, normals[11].y, normals[11].z, normals[12].x, normals[12].y, normals[12].z, normals[13].x, normals[13].y, normals[13].z, normals[14].x, normals[14].y, normals[14].z, normals[15].x, normals[15].y, normals[15].z, normals[16].x, normals[16].y, normals[16].z, normals[17].x, normals[17].y, normals[17].z, normals[18].x, normals[18].y, normals[18].z, normals[19].x, normals[19].y, normals[19].z});

        textureOffset = 0.3F;
        part.setTexCoords(new float[]{0.5F - textureOffset, 0.5F - textureOffset, 0.5F - textureOffset, 0.5F + textureOffset, 0.5F + textureOffset, 0.5F + textureOffset, 0.5F + textureOffset, 0.5F - textureOffset, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F});

        part.setIndexes(new short[]{0, 2, 1, 0, 3, 2, 0, 1, 5, 0, 5, 4, 1, 2, 5, 2, 6, 5, 6, 3, 7, 2, 3, 6, 3, 0, 9, 0, 8, 9});

        buffer.add(part);

        return 0;
    }

    public int createInternalGeometryFuzzy(GeomPartBuffer buffer, int x, int y, int z, int xWorld, int yWorld, int zWorld, float sun, float light, BlockType block) {
        float size = 0.8F;
        float nest = 0.01F;
        float tent = 0.3F;
        float separation = 0.25F;

        float xCenter = x + 0.5F;
        float yCenter = z + 0.5F;
        float zCenter = y + 0.5F;

        float angle = 0.7853982F;
        float cos = FastMath.cos(angle);
        float sin = FastMath.sin(angle);

        GeomPart part = new GeomPart(this.materialType, this.dir);
        part.setSun(sun);
        part.setLight(light);

        part.setCoords(new float[]{xCenter - size, yCenter + size + tent - nest + separation, zCenter + size, xCenter + size, yCenter - size + tent + nest + separation, zCenter + size, xCenter + size, yCenter - size - tent + nest + separation, zCenter - size, xCenter - size, yCenter + size - tent - nest + separation, zCenter - size});

        part.setNormals(new float[]{cos, sin, 0.0F, cos, sin, 0.0F, cos, sin, 0.0F, cos, sin, 0.0F});

        part.setTexCoords(new float[]{0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F});

        part.setIndexes(new short[]{0, 1, 2, 0, 2, 3});

        buffer.add(part);

        part = new GeomPart(this.materialType, this.dir);
        part.setSun(sun);
        part.setLight(light);

        part.setCoords(new float[]{xCenter - size, yCenter + size - tent - nest + separation, zCenter + size, xCenter + size, yCenter - size - tent + nest + separation, zCenter + size, xCenter + size, yCenter - size + tent + nest + separation, zCenter - size, xCenter - size, yCenter + size + tent - nest + separation, zCenter - size});

        part.setNormals(new float[]{cos, sin, 0.0F, cos, sin, 0.0F, cos, sin, 0.0F, cos, sin, 0.0F});

        part.setTexCoords(new float[]{0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F});

        part.setIndexes(new short[]{0, 1, 2, 0, 2, 3});

        buffer.add(part);

        part = new GeomPart(this.materialType, this.dir);
        part.setSun(sun);
        part.setLight(light);

        part.setCoords(new float[]{xCenter - size, yCenter + size - tent + nest + separation, zCenter + size, xCenter + size, yCenter - size + tent + nest + separation, zCenter + size, xCenter + size, yCenter - size + tent - nest + separation, zCenter - size, xCenter - size, yCenter + size - tent - nest + separation, zCenter - size});

        part.setNormals(new float[]{cos, sin, 0.0F, cos, sin, 0.0F, cos, sin, 0.0F, cos, sin, 0.0F});

        part.setTexCoords(new float[]{0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F});

        part.setIndexes(new short[]{0, 1, 2, 0, 2, 3});

        buffer.add(part);

        part = new GeomPart(this.materialType, this.dir);
        part.setSun(sun);
        part.setLight(light);

        part.setCoords(new float[]{xCenter - size, yCenter + size + tent - nest + separation, zCenter + size, xCenter + size, yCenter - size - tent - nest + separation, zCenter + size, xCenter + size, yCenter - size - tent + nest + separation, zCenter - size, xCenter - size, yCenter + size + tent + nest + separation, zCenter - size});

        part.setNormals(new float[]{cos, sin, 0.0F, cos, sin, 0.0F, cos, sin, 0.0F, cos, sin, 0.0F});

        part.setTexCoords(new float[]{0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F});

        part.setIndexes(new short[]{0, 1, 2, 0, 2, 3});

        buffer.add(part);

        part = new GeomPart(this.materialType, this.dir);
        part.setSun(sun);
        part.setLight(light);

        part.setCoords(new float[]{xCenter - size, yCenter + size + tent - nest - separation, zCenter + size, xCenter + size, yCenter - size + tent + nest - separation, zCenter + size, xCenter + size, yCenter - size - tent + nest - separation, zCenter - size, xCenter - size, yCenter + size - tent - nest - separation, zCenter - size});

        part.setNormals(new float[]{-cos, -sin, 0.0F, -cos, -sin, 0.0F, -cos, -sin, 0.0F, -cos, -sin, 0.0F});

        part.setTexCoords(new float[]{0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F});

        part.setIndexes(new short[]{0, 2, 1, 0, 3, 2});

        buffer.add(part);

        part = new GeomPart(this.materialType, this.dir);
        part.setSun(sun);
        part.setLight(light);

        part.setCoords(new float[]{xCenter - size, yCenter + size - tent - nest - separation, zCenter + size, xCenter + size, yCenter - size - tent + nest - separation, zCenter + size, xCenter + size, yCenter - size + tent + nest - separation, zCenter - size, xCenter - size, yCenter + size + tent - nest - separation, zCenter - size});

        part.setNormals(new float[]{-cos, -sin, 0.0F, -cos, -sin, 0.0F, -cos, -sin, 0.0F, -cos, -sin, 0.0F});

        part.setTexCoords(new float[]{0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F});

        part.setIndexes(new short[]{0, 2, 1, 0, 3, 2});

        buffer.add(part);

        return 0;
    }

    public boolean isSolid(int direction) {
        return false;
    }

    public boolean isSolid() {
        return false;
    }

    public boolean isBoundary(int direction) {
        return false;
    }

    public BoundaryShape getBoundaryShape(int direction) {
        return ShapeIndex.NULL_SHAPE;
    }

    public final float getTransparency(int axis) {
        return 0.0F;
    }

    public Vector3f getMin() {
        return min;
    }

    public Vector3f getMax() {
        return max;
    }

    public double getMassPortion() {
        return 0.2D;
    }

    public boolean isClipped() {
        return true;
    }
}