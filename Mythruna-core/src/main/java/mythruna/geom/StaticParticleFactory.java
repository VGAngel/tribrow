package mythruna.geom;

import com.jme3.math.Vector3f;
import mythruna.BlockType;
import mythruna.BoundaryShape;
import mythruna.ShapeIndex;

public class StaticParticleFactory implements GeomFactory {

    private int materialType;
    private int count = 1;
    private Vector3f min;
    private Vector3f max;
    private GeomFactory delegate;
    private int[] counts = new int[4];
    private float[] heights = new float[4];

    public StaticParticleFactory(int materialType) {
        this(materialType, new Vector3f(0.0F, 0.0F, 0.0F), new Vector3f(1.0F, 1.0F, 1.0F), null, new int[]{2, 3, 3, 3});
    }

    public StaticParticleFactory(int materialType, Vector3f min, Vector3f max, GeomFactory delegate, int[] counts) {
        this.materialType = materialType;
        this.min = min;
        this.max = max;
        this.delegate = delegate;
        this.count = 0;

        for (int i = 0; i < counts.length; i++) {
            this.counts[i] = counts[i];
            this.heights[i] = 1.0F;
            this.count += counts[i];
        }
    }

    public StaticParticleFactory setHeight(int type, float height) {
        this.heights[type] = height;
        return this;
    }

    public boolean isSameShape(GeomFactory f) {
        if (f == this)
            return true;
        if ((f == null) || (f.getClass() != getClass()))
            return false;
        StaticParticleFactory other = (StaticParticleFactory) f;
        if (!this.min.equals(other.min))
            return false;
        if (!this.max.equals(other.max)) {
            return false;
        }
        return true;
    }

    public int createGeometry(GeomPartBuffer buffer, int x, int y, int z, int xWorld, int yWorld, int zWorld, float sun, float light, BlockType block, int dir) {
        if (this.delegate != null)
            return this.delegate.createGeometry(buffer, x, y, z, xWorld, yWorld, zWorld, sun, light, block, dir);
        return 0;
    }

    private float range(double s, float min, float max) {
        return (float) (min + s * max - min);
    }

    private float range(double s, float min, float max, float scale) {
        return (float) (min + s * max - min * scale);
    }

    public int createInternalGeometry(GeomPartBuffer buffer, int x, int y, int z, int xWorld, int yWorld, int zWorld, float sun, float light, BlockType block) {
        if (this.delegate != null) {
            this.delegate.createInternalGeometry(buffer, x, y, z, xWorld, yWorld, zWorld, sun, light, block);
        }

        float[] pos = new float[this.count * 3];
        float[] sizes = new float[this.count];
        float[] colors = new float[this.count * 4];
        float[] texes = new float[this.count * 4];

        int base3 = 0;
        int base4 = 0;
        int index = 0;

        for (int type = 0; type < 4; type++) {
            int localCount = this.counts[type];
            for (int i = 0; i < localCount; i++) {
                float xOffset = range(buffer.nextRandom(), this.min.x, this.max.x);
                float yOffset = range(buffer.nextRandom(), this.min.y, this.max.y);
                float zOffset = range(buffer.nextRandom(), this.min.z, this.max.z, this.heights[type]);

                pos[base3] = (x + xOffset);
                pos[(base3 + 1)] = (z + zOffset);
                pos[(base3 + 2)] = (y + yOffset);

                sizes[index] = 5.0F;

                colors[base4] = light;
                colors[(base4 + 1)] = 1.0F;
                colors[(base4 + 2)] = 1.0F;
                colors[(base4 + 3)] = sun;

                int s = (int) (buffer.nextRandom() * 4.0D);

                int t = type;

                texes[base4] = (s * 0.25F);
                texes[(base4 + 1)] = (t * 0.25F + 0.24F);
                texes[(base4 + 2)] = (s * 0.25F + 0.24F);
                texes[(base4 + 3)] = (t * 0.25F);

                index++;
                base3 += 3;
                base4 += 4;
            }
        }

        GeomPart part = new GeomPart(this.materialType, -1, 1);
        part.setCoords(pos);
        part.setSizes(sizes);
        part.setColors(colors);
        part.setTexCoords(texes);
        buffer.add(part);
        return 1;
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