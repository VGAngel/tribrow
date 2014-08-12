package mythruna.geom;

import com.jme3.math.Vector3f;
import mythruna.BlockType;
import mythruna.BoundaryShape;
import mythruna.ShapeIndex;

public class GrassFactory implements GeomFactory {

    private Vector3f max = new Vector3f(1.0F, 1.0F, 0.5F);
    private float height;
    private int materialType;
    private float[] normals = {0.0F, 1.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 1.0F, 0.0F};

    private float[][] texOffsets = {{0.0F, 0.235F}, {0.25F, 0.485F}, {0.5F, 0.735F}, {0.75F, 0.985F}};

    public GrassFactory(float height, int materialType) {
        this.height = height;
        this.max.z = (height * 0.5F);
        this.materialType = materialType;
    }

    public int createGeometry(GeomPartBuffer buffer, int x, int y, int z, int xWorld, int yWorld, int zWorld, float sun, float light, BlockType block, int dir) {
        return 0;
    }

    public boolean isSameShape(GeomFactory f) {
        if (f == this)
            return true;
        if ((f == null) || (f.getClass() != getClass())) {
            return false;
        }
        return true;
    }

    protected float perturb(GeomPartBuffer buffer) {
        return (float) (buffer.nextRandom() * 0.3D) - 0.15F;
    }

    public int createInternalGeometry(GeomPartBuffer buffer, int x, int y, int z, int xWorld, int yWorld, int zWorld, float sun, float light, BlockType block) {
        float width = 1.2F;

        float innerOffset = width * 11.0F / 15.0F - 0.5F;
        float outerOffset = width * 0.5F;

        int texIndex = (x % 4 + y % 3) % 4;

        float xCenter = x + 0.5F;
        float zCenter = y + 0.5F;

        float yBase = z;

        GeomPart part = new GeomPart(this.materialType, 4);
        part.setSun(sun);
        part.setLight(light);
        part.setCoords(new float[]{xCenter - innerOffset + perturb(buffer), yBase, zCenter - outerOffset, xCenter + outerOffset, yBase, zCenter + innerOffset + perturb(buffer), xCenter + outerOffset, z + this.height, zCenter + innerOffset + perturb(buffer), xCenter - innerOffset + perturb(buffer), z + this.height, zCenter - outerOffset});

        part.setNormals(this.normals);
        part.setTexCoords(new float[]{0.0F, this.texOffsets[texIndex][0], 1.0F, this.texOffsets[texIndex][0], 1.0F, this.texOffsets[texIndex][1], 0.0F, this.texOffsets[texIndex][1]});

        part.setIndexes(new short[]{0, 1, 2, 0, 2, 3});

        buffer.add(part);

        texIndex = (texIndex + 1) % 4;
        part = new GeomPart(this.materialType, 4);
        part.setSun(sun);
        part.setLight(light);
        part.setCoords(new float[]{xCenter + innerOffset + perturb(buffer), yBase, zCenter - outerOffset, xCenter - outerOffset, yBase, zCenter + innerOffset + perturb(buffer), xCenter - outerOffset, z + this.height, zCenter + innerOffset + perturb(buffer), xCenter + innerOffset + perturb(buffer), z + this.height, zCenter - outerOffset});

        part.setNormals(this.normals);
        part.setTexCoords(new float[]{0.0F, this.texOffsets[texIndex][0], 1.0F, this.texOffsets[texIndex][0], 1.0F, this.texOffsets[texIndex][1], 0.0F, this.texOffsets[texIndex][1]});

        part.setIndexes(new short[]{0, 1, 2, 0, 2, 3});

        buffer.add(part);

        texIndex = (texIndex + 1) % 4;
        part = new GeomPart(this.materialType, 4);
        part.setSun(sun);
        part.setLight(light);
        part.setCoords(new float[]{xCenter - outerOffset, yBase, zCenter - innerOffset + perturb(buffer), xCenter + innerOffset + perturb(buffer), yBase, zCenter + outerOffset, xCenter + innerOffset + perturb(buffer), z + this.height, zCenter + outerOffset, xCenter - outerOffset, z + this.height, zCenter - innerOffset + perturb(buffer)});

        part.setNormals(this.normals);
        part.setTexCoords(new float[]{0.0F, this.texOffsets[texIndex][0], 1.0F, this.texOffsets[texIndex][0], 1.0F, this.texOffsets[texIndex][1], 0.0F, this.texOffsets[texIndex][1]});

        part.setIndexes(new short[]{0, 1, 2, 0, 2, 3});

        buffer.add(part);

        texIndex = (texIndex + 1) % 4;
        part = new GeomPart(this.materialType, 4);
        part.setSun(sun);
        part.setLight(light);
        part.setCoords(new float[]{xCenter + outerOffset, yBase, zCenter - innerOffset + perturb(buffer), xCenter - innerOffset + perturb(buffer), yBase, zCenter + outerOffset, xCenter - innerOffset + perturb(buffer), z + this.height, zCenter + outerOffset, xCenter + outerOffset, z + this.height, zCenter - innerOffset + perturb(buffer)});

        part.setNormals(this.normals);
        part.setTexCoords(new float[]{0.0F, this.texOffsets[texIndex][0], 1.0F, this.texOffsets[texIndex][0], 1.0F, this.texOffsets[texIndex][1], 0.0F, this.texOffsets[texIndex][1]});

        part.setIndexes(new short[]{0, 1, 2, 0, 2, 3});

        buffer.add(part);

        return 4;
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

    public final float getTransparency(int axis) {
        return 1.0F;
    }

    public BoundaryShape getBoundaryShape(int direction) {
        return ShapeIndex.NULL_SHAPE;
    }

    public Vector3f getMin() {
        return Vector3f.ZERO;
    }

    public Vector3f getMax() {
        return this.max;
    }

    public double getMassPortion() {
        return 0.01D;
    }

    public boolean isClipped() {
        return false;
    }
}