package mythruna.geom;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import mythruna.BlockType;
import mythruna.BoundaryShape;
import mythruna.ShapeIndex;

public class PineBranchFactory implements GeomFactory {

    private static Vector3f min = new Vector3f(0.25F, 0.25F, 0.25F);
    private static Vector3f max = new Vector3f(0.75F, 0.75F, 0.75F);

    private static float TIP = -0.3F;
    private static float T_OFFSET = 0.3F;

    private static float BRANCH_OFFSET = 0.04F;
    private static float NEEDLE_OFFSET = 0.85F;
    private static float BRANCH_LENGTH = 1.7F + -TIP + 0.5F;
    private static float BRANCH_WIDTH = 1.0F + T_OFFSET;

    private static Vector3f[] baseVerts = {new Vector3f(0.0F, BRANCH_OFFSET, 0.0F), new Vector3f(BRANCH_WIDTH, -NEEDLE_OFFSET, 0.0F), new Vector3f(0.0F, 0.0F, BRANCH_LENGTH), new Vector3f(-BRANCH_WIDTH, -NEEDLE_OFFSET, 0.0F), new Vector3f(0.0F, BRANCH_OFFSET, 0.0F), new Vector3f(0.0F, 0.0F, BRANCH_LENGTH), new Vector3f(0.0F, -BRANCH_OFFSET, 0.0F), new Vector3f(BRANCH_WIDTH, NEEDLE_OFFSET, 0.0F), new Vector3f(0.0F, 0.0F, BRANCH_LENGTH), new Vector3f(-BRANCH_WIDTH, NEEDLE_OFFSET, 0.0F), new Vector3f(0.0F, -BRANCH_OFFSET, 0.0F), new Vector3f(0.0F, 0.0F, BRANCH_LENGTH)};

    private static Vector3f[] baseNorms = {new Vector3f(0.0F, 1.0F, 0.0F).normalizeLocal(), new Vector3f(1.0F, -1.0F, 0.0F).normalizeLocal(), new Vector3f(0.0F, 1.0F, 1.0F).normalizeLocal(), new Vector3f(-1.0F, -1.0F, 0.0F).normalizeLocal(), new Vector3f(0.0F, 1.0F, 0.0F).normalizeLocal(), new Vector3f(0.0F, 1.0F, 1.0F).normalizeLocal(), new Vector3f(0.0F, -1.0F, 0.0F).normalizeLocal(), new Vector3f(1.0F, 1.0F, 0.0F).normalizeLocal(), new Vector3f(0.0F, 1.0F, 1.0F).normalizeLocal(), new Vector3f(-1.0F, 1.0F, 0.0F).normalizeLocal(), new Vector3f(0.0F, -1.0F, 0.0F).normalizeLocal(), new Vector3f(0.0F, 1.0F, 1.0F).normalizeLocal()};

    private static float[] texCoords = {0.5F, 1.0F, 1.0F + T_OFFSET, 1.0F, 0.5F, TIP, 0.0F - T_OFFSET, 1.0F, 0.5F, 1.0F, 0.5F, TIP, 0.5F, 1.0F, 1.0F + T_OFFSET, 1.0F, 0.5F, TIP, 0.0F - T_OFFSET, 1.0F, 0.5F, 1.0F, 0.5F, TIP};

    private static short[] indexes = {0, 2, 1, 3, 5, 4, 6, 7, 8, 9, 10, 11, 0, 1, 2, 3, 4, 5, 6, 8, 7, 9, 11, 10};

    private Vector3f offset = new Vector3f(0.0F, 0.0F, -0.8F);
    private int material;
    private float yaw;
    private Quaternion rotate;

    public PineBranchFactory(int materialType, float yaw) {
        this.material = materialType;
        this.yaw = yaw;

        this.rotate = new Quaternion().fromAngles(0.0F, yaw, 0.0F);
        this.rotate.multLocal(this.offset);
    }

    public boolean isSameShape(GeomFactory f) {
        if (f == this)
            return true;
        if ((f == null) || (f.getClass() != getClass()))
            return false;
        PineBranchFactory other = (PineBranchFactory) f;
        if (this.yaw != other.yaw) {
            return false;
        }
        return true;
    }

    public float getYaw() {
        return this.yaw;
    }

    public int createGeometry(GeomPartBuffer buffer, int x, int y, int z, int xWorld, int yWorld, int zWorld, float sun, float light, BlockType block, int dir) {
        return 0;
    }

    public int createInternalGeometry(GeomPartBuffer buffer, int x, int y, int z, int xWorld, int yWorld, int zWorld, float sun, float light, BlockType block) {
        float xCenter = x + 0.5F + this.offset.x;
        float zCenter = y + 0.5F + this.offset.z;
        float yBase = z + 0.75F + this.offset.y;

        yBase += (float) (buffer.nextRandom() * 0.5D - 0.25D);

        float a = this.yaw + (float) (buffer.nextRandom() * 20.0D - 10.0D) * 0.01745329F;

        float p = (float) (buffer.nextRandom() * 20.0D - 10.0D) * 0.01745329F;
        Quaternion q = new Quaternion().fromAngles(p, a, 0.0F);

        Vector3f[] verts = new Vector3f[baseVerts.length];
        Vector3f[] norms = new Vector3f[baseNorms.length];

        for (int i = 0; i < verts.length; i++) {
            verts[i] = q.mult(baseVerts[i]);
            norms[i] = q.mult(baseNorms[i]);
        }

        GeomPart part = new GeomPart(this.material, -1);
        part.setSun(sun);
        part.setLight(light);

        part.setCoords(new float[]{verts[0].x + xCenter, verts[0].y + yBase, verts[0].z + zCenter, verts[1].x + xCenter, verts[1].y + yBase, verts[1].z + zCenter, verts[2].x + xCenter, verts[2].y + yBase, verts[2].z + zCenter, verts[3].x + xCenter, verts[3].y + yBase, verts[3].z + zCenter, verts[4].x + xCenter, verts[4].y + yBase, verts[4].z + zCenter, verts[5].x + xCenter, verts[5].y + yBase, verts[5].z + zCenter, verts[6].x + xCenter, verts[6].y + yBase, verts[6].z + zCenter, verts[7].x + xCenter, verts[7].y + yBase, verts[7].z + zCenter, verts[8].x + xCenter, verts[8].y + yBase, verts[8].z + zCenter, verts[9].x + xCenter, verts[9].y + yBase, verts[9].z + zCenter, verts[10].x + xCenter, verts[10].y + yBase, verts[10].z + zCenter, verts[11].x + xCenter, verts[11].y + yBase, verts[11].z + zCenter});

        part.setNormals(new float[]{norms[0].x, norms[0].y, norms[0].z, norms[1].x, norms[1].y, norms[1].z, norms[2].x, norms[2].y, norms[2].z, norms[3].x, norms[3].y, norms[3].z, norms[4].x, norms[4].y, norms[4].z, norms[5].x, norms[5].y, norms[5].z, norms[6].x, norms[6].y, norms[6].z, norms[7].x, norms[7].y, norms[7].z, norms[8].x, norms[8].y, norms[8].z, norms[9].x, norms[9].y, norms[9].z, norms[10].x, norms[10].y, norms[10].z, norms[11].x, norms[11].y, norms[11].z});

        part.setTexCoords(texCoords);
        part.setIndexes(indexes);
        buffer.add(part);

        return 2;
    }

    public boolean isSolid(int direction) {
        return true;
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
        return 0.75F;
    }

    public double getMassPortion() {
        double x = getMax().x - getMin().x;
        double y = getMax().y - getMin().y;
        double z = getMax().z - getMin().z;

        return x * y * z * 0.125D;
    }

    public Vector3f getMin() {
        return min;
    }

    public Vector3f getMax() {
        return max;
    }

    public boolean isClipped() {
        return true;
    }
}