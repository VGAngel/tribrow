package mythruna.geom;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import mythruna.BlockType;
import mythruna.BoundaryShape;
import mythruna.ShapeIndex;

public class TreeLeafFactory implements GeomFactory {

    private static float COS_45 = FastMath.cos(0.7853982F);
    private static float SIN_45 = FastMath.sin(0.7853982F);

    private static Vector3f min = new Vector3f(0.25F, 0.25F, 0.25F);
    private static Vector3f max = new Vector3f(0.75F, 0.75F, 0.75F);
    private int materialType;
    private static final int ATLAS_SIZE = 16;
    private float[][] yTexOffsets = {{0.0F, 0.249F}, {0.25F, 0.49F}, {0.5F, 0.749F}, {0.75F, 0.99F}, {0.249F, 0.0F}, {0.49F, 0.25F}, {0.749F, 0.5F}, {0.99F, 0.75F}, {0.0F, 0.249F}, {0.25F, 0.49F}, {0.5F, 0.749F}, {0.75F, 0.99F}, {0.249F, 0.0F}, {0.49F, 0.25F}, {0.749F, 0.5F}, {0.99F, 0.75F}};

    private float[][] xTexOffsets = {{0.0F, 0.25F}, {0.0F, 0.25F}, {0.0F, 0.25F}, {0.0F, 0.25F}, {0.0F, 0.25F}, {0.0F, 0.25F}, {0.0F, 0.25F}, {0.0F, 0.25F}, {0.25F, 0.0F}, {0.25F, 0.0F}, {0.25F, 0.0F}, {0.25F, 0.0F}, {0.25F, 0.0F}, {0.25F, 0.0F}, {0.25F, 0.0F}, {0.25F, 0.0F}};

    private float[] normals = {0.0F, 1.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 1.0F, 0.0F};

    public TreeLeafFactory(int materialType) {
        this.materialType = materialType;
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
        return 0;
    }

    protected float perturb(GeomPartBuffer buffer) {
        return (float) (buffer.nextRandom() * 0.3D) - 0.15F;
    }

    public int createInternalGeometry(GeomPartBuffer buffer, int x, int y, int z, int xWorld, int yWorld, int zWorld, float sun, float light, BlockType block) {
        return createInternalGeometryPlain(buffer, x, y, z, xWorld, yWorld, zWorld, sun, light, block);
    }

    public int createInternalGeometryPlain(GeomPartBuffer buffer, int x, int y, int z, int xWorld, int yWorld, int zWorld, float sun, float light, BlockType block) {
        float height = 1.8F;
        float width = 0.9F;

        float xCenter = x + 0.5F;
        float zCenter = y + 0.5F;

        float yBase = z - 0.4F;

        float offset = 0.05F;
        float nest = 0.1F;

        float angle = 0.5105088F;

        float cos = FastMath.cos(angle);
        float sin = FastMath.sin(angle);
        Vector3f norm = new Vector3f(cos, cos, sin);
        norm.normalize();

        Vector3f norm2 = new Vector3f(cos, 0.0F, sin);
        norm2.normalize();

        int texIndex = (int) (buffer.nextRandom() * 16.0D);

        GeomPart part = new GeomPart(this.materialType, -1);
        part.setSun(sun);
        part.setLight(light);
        part.setCoords(new float[]{xCenter + width - offset - nest, yBase, zCenter + width + offset + nest, xCenter - width - offset + nest, yBase, zCenter - width + offset - nest, xCenter - width + nest, yBase + height, zCenter - width - nest, xCenter + width - nest, yBase + height, zCenter + width + nest});

        part.setNormals(new float[]{norm2.x, -norm2.y, norm2.z, -norm2.z, -norm2.y, -norm2.x, -norm.z, norm.y, -norm.x, norm.x, norm.y, norm.z});

        part.setTexCoords(new float[]{this.xTexOffsets[texIndex][0], this.yTexOffsets[texIndex][0], this.xTexOffsets[texIndex][1], this.yTexOffsets[texIndex][0], this.xTexOffsets[texIndex][1], this.yTexOffsets[texIndex][1], this.xTexOffsets[texIndex][0], this.yTexOffsets[texIndex][1]});

        part.setIndexes(new short[]{0, 1, 2, 0, 2, 3});

        buffer.add(part);

        texIndex = (texIndex + 1) % 16;
        part = new GeomPart(this.materialType, -1);
        part.setSun(sun);
        part.setLight(light);
        part.setCoords(new float[]{xCenter - width + offset + nest, yBase, zCenter - width - offset - nest, xCenter + width + offset - nest, yBase, zCenter + width - offset + nest, xCenter + width - nest, yBase + height, zCenter + width + nest, xCenter - width + nest, yBase + height, zCenter - width - nest});

        part.setNormals(new float[]{-norm2.x, -norm2.y, -norm2.z, norm2.z, -norm2.y, norm2.x, norm.z, norm.y, norm.x, -norm.x, norm.y, -norm.z});

        part.setTexCoords(new float[]{this.xTexOffsets[texIndex][0], this.yTexOffsets[texIndex][0], this.xTexOffsets[texIndex][1], this.yTexOffsets[texIndex][0], this.xTexOffsets[texIndex][1], this.yTexOffsets[texIndex][1], this.xTexOffsets[texIndex][0], this.yTexOffsets[texIndex][1]});

        part.setIndexes(new short[]{0, 1, 2, 0, 2, 3});

        buffer.add(part);

        texIndex = (texIndex + 1) % 16;
        part = new GeomPart(this.materialType, -1);
        part.setSun(sun);
        part.setLight(light);
        part.setCoords(new float[]{xCenter + width + offset + nest, yBase, zCenter - width + offset + nest, xCenter - width + offset - nest, yBase, zCenter + width + offset - nest, xCenter - width - nest, yBase + height, zCenter + width - nest, xCenter + width + nest, yBase + height, zCenter - width + nest});

        part.setNormals(new float[]{norm2.z, -norm2.y, -norm2.x, -norm2.x, -norm2.y, norm2.z, -norm.x, norm.y, norm.z, norm.z, norm.y, -norm.x});

        part.setTexCoords(new float[]{this.xTexOffsets[texIndex][0], this.yTexOffsets[texIndex][0], this.xTexOffsets[texIndex][1], this.yTexOffsets[texIndex][0], this.xTexOffsets[texIndex][1], this.yTexOffsets[texIndex][1], this.xTexOffsets[texIndex][0], this.yTexOffsets[texIndex][1]});

        part.setIndexes(new short[]{0, 1, 2, 0, 2, 3});

        buffer.add(part);

        texIndex = (texIndex + 1) % 16;
        part = new GeomPart(this.materialType, -1);
        part.setSun(sun);
        part.setLight(light);
        part.setCoords(new float[]{xCenter - width - offset - nest, yBase, zCenter + width - offset - nest, xCenter + width - offset + nest, yBase, zCenter - width - offset + nest, xCenter + width + nest, yBase + height, zCenter - width + nest, xCenter - width - nest, yBase + height, zCenter + width - nest});

        part.setNormals(new float[]{-norm2.z, -norm2.y, norm2.x, norm2.x, -norm2.y, -norm2.z, norm.x, norm.y, -norm.z, -norm.z, norm.y, norm.x});

        part.setTexCoords(new float[]{this.xTexOffsets[texIndex][0], this.yTexOffsets[texIndex][0], this.xTexOffsets[texIndex][1], this.yTexOffsets[texIndex][0], this.xTexOffsets[texIndex][1], this.yTexOffsets[texIndex][1], this.xTexOffsets[texIndex][0], this.yTexOffsets[texIndex][1]});

        part.setIndexes(new short[]{0, 1, 2, 0, 2, 3});

        buffer.add(part);

        texIndex = (texIndex + 1) % 16;
        part = new GeomPart(this.materialType, -1);
        part.setSun(sun);
        part.setLight(light);
        float topWidth = 0.5F;
        float yTop = z + 0.5F;
        part.setCoords(new float[]{xCenter - topWidth, yTop, zCenter + topWidth, xCenter + topWidth, yTop, zCenter + topWidth, xCenter + topWidth, yTop, zCenter - topWidth, xCenter - topWidth, yTop, zCenter - topWidth});

        part.setNormals(new float[]{-norm.z, norm.y, norm.x, norm.x, norm.y, norm.z, norm.z, norm.y, -norm.x, -norm.x, norm.y, -norm.z});

        part.setTexCoords(new float[]{this.xTexOffsets[texIndex][0], this.yTexOffsets[texIndex][0], this.xTexOffsets[texIndex][1], this.yTexOffsets[texIndex][0], this.xTexOffsets[texIndex][1], this.yTexOffsets[texIndex][1], this.xTexOffsets[texIndex][0], this.yTexOffsets[texIndex][1]});

        part.setIndexes(new short[]{0, 1, 2, 0, 2, 3});

        buffer.add(part);

        return 0;
    }

    public int createInternalGeometryModifiedGrass(GeomPartBuffer buffer, int x, int y, int z, int xWorld, int yWorld, int zWorld, float sun, float light, BlockType block) {
        float height = 1.2F;
        float width = 1.2F;

        float innerOffset = width * 11.0F / 15.0F - 0.5F;
        float outerOffset = width * 0.5F;

        int texIndex = (x % 4 + y % 3) % 4;

        float xCenter = x + 0.5F;
        float zCenter = y + 0.5F;

        float yBase = z;

        Vector3f norm = new Vector3f(1.0F, 1.0F, 1.0F);
        norm.normalize();

        GeomPart part = new GeomPart(this.materialType, 4);
        part.setSun(sun);
        part.setLight(light);
        part.setCoords(new float[]{xCenter - innerOffset + perturb(buffer), yBase, zCenter - outerOffset, xCenter + outerOffset, yBase, zCenter + innerOffset + perturb(buffer), xCenter + outerOffset, z + height, zCenter + innerOffset + perturb(buffer), xCenter - innerOffset + perturb(buffer), z + height, zCenter - outerOffset});

        part.setNormals(this.normals);
        part.setTexCoords(new float[]{0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F});

        part.setIndexes(new short[]{0, 1, 2, 0, 2, 3});

        buffer.add(part);

        texIndex = (texIndex + 1) % 4;
        part = new GeomPart(this.materialType, 4);
        part.setSun(sun);
        part.setLight(light);
        part.setCoords(new float[]{xCenter + innerOffset + perturb(buffer), yBase, zCenter - outerOffset, xCenter - outerOffset, yBase, zCenter + innerOffset + perturb(buffer), xCenter - outerOffset, z + height, zCenter + innerOffset + perturb(buffer), xCenter + innerOffset + perturb(buffer), z + height, zCenter - outerOffset});

        part.setNormals(this.normals);
        part.setTexCoords(new float[]{0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F});

        part.setIndexes(new short[]{0, 1, 2, 0, 2, 3});

        buffer.add(part);

        texIndex = (texIndex + 1) % 4;
        part = new GeomPart(this.materialType, 4);
        part.setSun(sun);
        part.setLight(light);
        part.setCoords(new float[]{xCenter - outerOffset, yBase, zCenter - innerOffset + perturb(buffer), xCenter + innerOffset + perturb(buffer), yBase, zCenter + outerOffset, xCenter + innerOffset + perturb(buffer), z + height, zCenter + outerOffset, xCenter - outerOffset, z + height, zCenter - innerOffset + perturb(buffer)});

        part.setNormals(this.normals);
        part.setTexCoords(new float[]{0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F});

        part.setIndexes(new short[]{0, 1, 2, 0, 2, 3});

        buffer.add(part);

        texIndex = (texIndex + 1) % 4;
        part = new GeomPart(this.materialType, 4);
        part.setSun(sun);
        part.setLight(light);
        part.setCoords(new float[]{xCenter + outerOffset, yBase, zCenter - innerOffset + perturb(buffer), xCenter - innerOffset + perturb(buffer), yBase, zCenter + outerOffset, xCenter - innerOffset + perturb(buffer), z + height, zCenter + outerOffset, xCenter + outerOffset, z + height, zCenter - innerOffset + perturb(buffer)});

        part.setNormals(this.normals);
        part.setTexCoords(new float[]{0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F});

        part.setIndexes(new short[]{0, 1, 2, 0, 2, 3});

        buffer.add(part);

        return 4;
    }

    public int createInternalGeometryFunky(GeomPartBuffer buffer, int x, int y, int z, int xWorld, int yWorld, int zWorld, float sun, float light, BlockType block) {
        int count = 0;

        float xCenter = x + 0.5F;
        float zCenter = y + 0.5F;
        float yBase = z;
        float yTop = z + 1.0F;

        float outer = 0.5F;
        float inner = 0.1F;

        float outerOffset = 0.1F;
        float innerOffset = 0.1F;

        float lowerOffset = -0.2F;

        Vector3f norm = new Vector3f(1.0F, 1.0F, 1.0F);
        norm.normalize();

        GeomPart part = new GeomPart(this.materialType, 4);
        part.setSun(sun);
        part.setLight(light);
        part.setCoords(new float[]{xCenter - outer - outerOffset, yTop, zCenter - outer + outerOffset, xCenter + inner + innerOffset, yTop, zCenter + inner - innerOffset, xCenter - outer + outerOffset, yBase - lowerOffset, zCenter - outer - outerOffset});

        part.setNormals(new float[]{-norm.x, norm.y, -norm.z, norm.x, norm.y, -norm.z, -SIN_45, 0.0F, -COS_45});

        part.setTexCoords(new float[]{0.0F, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F});

        part.setIndexes(new short[]{0, 1, 2});

        buffer.add(part);
        count++;

        part = new GeomPart(this.materialType, 4);
        part.setSun(sun);
        part.setLight(light);
        part.setCoords(new float[]{xCenter - outer + outerOffset, yTop, zCenter - outer - outerOffset, xCenter + inner - innerOffset, yTop, zCenter + inner + innerOffset, xCenter - outer - outerOffset, yBase - lowerOffset, zCenter - outer + outerOffset});

        part.setNormals(new float[]{-norm.x, norm.y, -norm.z, -norm.x, norm.y, norm.z, -SIN_45, 0.0F, -COS_45});

        part.setTexCoords(new float[]{0.0F, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F});

        part.setIndexes(new short[]{0, 2, 1});

        buffer.add(part);
        count++;

        part = new GeomPart(this.materialType, 4);
        part.setSun(sun);
        part.setLight(light);
        part.setCoords(new float[]{xCenter + outer - outerOffset, yTop, zCenter + outer + outerOffset, xCenter - inner + innerOffset, yTop, zCenter - inner - innerOffset, xCenter + outer + outerOffset, yBase - lowerOffset, zCenter + outer - outerOffset});

        part.setNormals(new float[]{norm.x, norm.y, norm.z, norm.x, norm.y, -norm.z, SIN_45, 0.0F, COS_45});

        part.setTexCoords(new float[]{0.0F, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F});

        part.setIndexes(new short[]{0, 2, 1});

        buffer.add(part);
        count++;

        part = new GeomPart(this.materialType, 4);
        part.setSun(sun);
        part.setLight(light);
        part.setCoords(new float[]{xCenter + outer + outerOffset, yTop, zCenter + outer - outerOffset, xCenter - inner - innerOffset, yTop, zCenter - inner + innerOffset, xCenter + outer - outerOffset, yBase - lowerOffset, zCenter + outer + outerOffset});

        part.setNormals(new float[]{norm.x, norm.y, norm.z, -norm.x, norm.y, norm.z, SIN_45, 0.0F, COS_45});

        part.setTexCoords(new float[]{0.0F, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F});

        part.setIndexes(new short[]{0, 1, 2});

        buffer.add(part);
        count++;

        return count;
    }

    public boolean isSolid(int direction) {
        return true;
    }

    public boolean isSolid() {
        return true;
    }

    public boolean isBoundary(int direction) {
        return false;
    }

    public BoundaryShape getBoundaryShape(int direction) {
        return ShapeIndex.NULL_SHAPE;
    }

    public final float getTransparency(int axis) {
        return 0.75F;
    }

    public Vector3f getMin() {
        return min;
    }

    public Vector3f getMax() {
        return max;
    }

    public double getMassPortion() {
        double x = getMax().x - getMin().x;
        double y = getMax().y - getMin().y;
        double z = getMax().z - getMin().z;

        return x * y * z * 0.125D;
    }

    public boolean isClipped() {
        return true;
    }
}