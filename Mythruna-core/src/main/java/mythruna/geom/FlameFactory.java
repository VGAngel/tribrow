package mythruna.geom;

import com.jme3.math.Vector3f;
import mythruna.BlockType;
import mythruna.BoundaryShape;
import mythruna.ShapeIndex;

public class FlameFactory implements GeomFactory {

    private int materialType;
    private int count = 1;
    private float height = 1.0F;
    private Vector3f min;
    private Vector3f max;

    public FlameFactory(int materialType) {
        this(materialType, 1.0F);
    }

    public FlameFactory(int materialType, float height) {
        this.materialType = materialType;
        this.height = height;

        float offset = height * 0.25F;
        this.min = new Vector3f(0.5F - offset, 0.5F - offset, 0.0F);
        this.max = new Vector3f(0.5F + offset, 0.5F + offset, height);
    }

    public FlameFactory(int materialType, int count) {
        this.materialType = materialType;
        this.count = count;
        this.min = new Vector3f(0.0F, 0.0F, 0.0F);
        this.max = new Vector3f(1.0F, 1.0F, 1.0F);
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

    public int createInternalGeometry(GeomPartBuffer buffer, int x, int y, int z, int xWorld, int yWorld, int zWorld, float sun, float light, BlockType block) {
        if (this.count <= 1) {
            GeomPart part = createFlame(x + 0.5F, y + 0.5F, z, this.height);
            buffer.add(part);
            return 1;
        }

        for (int i = 0; i < this.count; i++) {
            float xOffset = (float) buffer.nextRandom() - 0.5F;
            float yOffset = (float) buffer.nextRandom() - 0.5F;
            float scale = (float) buffer.nextRandom() * (0.5F * this.height) + 0.5F * this.height;
            GeomPart part = createFlame(x + 0.5F + xOffset, y + 0.5F + yOffset, z, scale);
            buffer.add(part);
        }

        return this.count;
    }

    protected GeomPart createFlame(float x, float y, float z, float scale) {
        GeomPart part = new GeomPart(this.materialType, -1, 1);

        float[] pos = new float[12];
        float[] sizes = new float[12];
        float h = 0.25F;
        float delta = 0.15F;
        float s = 20.0F;
        for (int i = 0; i < pos.length; i++) {
            pos[i] = (h * scale);
            sizes[i] = (s * scale);
            s *= 0.75F;
            h += delta;
            delta *= 0.75F;
        }

        part.setCoords(new float[]{x, z + pos[11], y, x, z + pos[10], y, x, z + pos[9], y, x, z + pos[8], y, x, z + pos[7], y, x, z + pos[6], y, x, z + pos[5], y, x, z + pos[4], y, x, z + pos[3], y, x, z + pos[2], y, x, z + pos[1], y, x, z + pos[0], y});

        part.setSizes(new float[]{sizes[11] * 1.1F, sizes[10], sizes[9], sizes[8], sizes[7], sizes[6], sizes[5], sizes[4], sizes[3], sizes[2], sizes[1], sizes[0]});

        boolean tapered = false;
        if (tapered) {
            part.setColors(new float[]{1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.1F, 0.1F, 1.0F, 1.0F, 0.1F, 0.1F, 1.0F, 1.0F, 0.2F, 0.2F, 1.0F, 1.0F, 0.2F, 0.2F, 1.0F, 1.0F, 0.3F, 0.3F, 1.0F, 1.0F, 0.3F, 0.3F, 1.0F, 1.0F, 0.4F, 0.4F, 1.0F, 1.0F, 0.5F, 0.5F, 1.0F, 1.0F, 0.7F, 0.7F, 1.0F, 1.0F, 0.9F, 0.9F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F});
        } else {
            float red = 0.45F;

            float green = 0.6F;

            float blue = 0.45F;
            float alpha = 1.0F;
            part.setColors(new float[]{red, 0.0F, 0.0F, alpha, red, green, blue, alpha, red, green, blue, alpha, red, green, blue, alpha, red, green, blue, alpha, red, green, blue, alpha, red, green, blue, alpha, red, green, blue, alpha, red, green, blue, alpha, red, green, blue, alpha, red, green, blue, alpha, red, green, blue, alpha});
        }

        part.setTexCoords(new float[]{0.0F, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F});

        return part;
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
        return this.min;
    }

    public Vector3f getMax() {
        return this.max;
    }

    public double getMassPortion() {
        return 0.0D;
    }

    public boolean isClipped() {
        return false;
    }
}