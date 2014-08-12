package mythruna.geom;

import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import mythruna.BlockType;
import mythruna.BoundaryShape;
import mythruna.ShapeIndex;

public class PineTopFactory implements GeomFactory {

    private static Vector3f min = new Vector3f(0.25F, 0.25F, 0.0F);
    private static Vector3f max = new Vector3f(0.75F, 0.75F, 1.0F);

    private static float TIP = -0.3F;
    private static float T_OFFSET = 0.3F;

    private static float BRANCH_OFFSET = 0.04F;

    private static float TOP_SCALE = 1.3F;
    private static float BRANCH_LENGTH = (1.7F + -TIP + 0.5F + 0.5F + 0.25F + 0.2F) * TOP_SCALE;
    private static float BRANCH_WIDTH = (1.0F + T_OFFSET) * TOP_SCALE;
    private static float NEEDLE_OFFSET = BRANCH_WIDTH * TOP_SCALE;

    private static Vector3f[] baseVerts = {new Vector3f(0.0F, 0.0F, BRANCH_OFFSET), new Vector3f(BRANCH_WIDTH, 0.0F, -NEEDLE_OFFSET), new Vector3f(0.0F, BRANCH_LENGTH, 0.0F), new Vector3f(-BRANCH_WIDTH, 0.0F, -NEEDLE_OFFSET), new Vector3f(0.0F, 0.0F, BRANCH_OFFSET), new Vector3f(0.0F, BRANCH_LENGTH, 0.0F), new Vector3f(0.0F, 0.0F, -BRANCH_OFFSET), new Vector3f(BRANCH_WIDTH, 0.0F, NEEDLE_OFFSET), new Vector3f(0.0F, BRANCH_LENGTH, 0.0F), new Vector3f(-BRANCH_WIDTH, 0.0F, NEEDLE_OFFSET), new Vector3f(0.0F, 0.0F, -BRANCH_OFFSET), new Vector3f(0.0F, BRANCH_LENGTH, 0.0F)};

    private static Vector3f[] baseNorms = {new Vector3f(0.0F, 0.0F, 1.0F).normalizeLocal(), new Vector3f(1.0F, 0.0F, -1.0F).normalizeLocal(), new Vector3f(0.0F, 1.0F, 0.0F).normalizeLocal(), new Vector3f(-1.0F, 0.0F, -1.0F).normalizeLocal(), new Vector3f(0.0F, 0.0F, 1.0F).normalizeLocal(), new Vector3f(0.0F, 1.0F, 0.0F).normalizeLocal(), new Vector3f(0.0F, 0.0F, -1.0F).normalizeLocal(), new Vector3f(1.0F, 0.0F, 1.0F).normalizeLocal(), new Vector3f(0.0F, 1.0F, 0.0F).normalizeLocal(), new Vector3f(-1.0F, 0.0F, 1.0F).normalizeLocal(), new Vector3f(0.0F, 0.0F, -1.0F).normalizeLocal(), new Vector3f(0.0F, 1.0F, 0.0F).normalizeLocal()};

    private static float[] texCoords = {0.5F, 1.0F, 1.0F + T_OFFSET, 1.0F, 0.5F, TIP, 0.0F - T_OFFSET, 1.0F, 0.5F, 1.0F, 0.5F, TIP, 0.5F, 1.0F, 1.0F + T_OFFSET, 1.0F, 0.5F, TIP, 0.0F - T_OFFSET, 1.0F, 0.5F, 1.0F, 0.5F, TIP};

    private static short[] indexes = {0, 2, 1, 3, 5, 4, 6, 7, 8, 9, 10, 11, 0, 1, 2, 3, 4, 5, 6, 8, 7, 9, 11, 10};

    private static float radius = 0.25F;
    private static float coneHeight = 2.25F;
    private static float cos45 = FastMath.cos(0.7853982F);
    private static float sin45 = FastMath.sin(0.7853982F);

    private static Vector3f[] cylNorms = {new Vector3f(1.0F, 0.0F, 0.0F), new Vector3f(cos45, 0.0F, -sin45), new Vector3f(0.0F, 0.0F, -1.0F), new Vector3f(-cos45, 0.0F, -sin45), new Vector3f(-1.0F, 0.0F, 0.0F), new Vector3f(-cos45, 0.0F, sin45), new Vector3f(0.0F, 0.0F, 1.0F), new Vector3f(cos45, 0.0F, sin45), new Vector3f(1.0F, 0.0F, 0.0F), new Vector3f(1.0F, 0.0F, 0.0F), new Vector3f(cos45, 0.0F, -sin45), new Vector3f(0.0F, 0.0F, -1.0F), new Vector3f(-cos45, 0.0F, -sin45), new Vector3f(-1.0F, 0.0F, 0.0F), new Vector3f(-cos45, 0.0F, sin45), new Vector3f(0.0F, 0.0F, 1.0F), new Vector3f(cos45, 0.0F, sin45), new Vector3f(1.0F, 0.0F, 0.0F)};

    private static Vector3f[] cylTans = {new Vector3f(0.0F, 0.0F, -1.0F), new Vector3f(-sin45, 0.0F, -cos45), new Vector3f(-1.0F, 0.0F, 0.0F), new Vector3f(-sin45, 0.0F, cos45), new Vector3f(0.0F, 0.0F, 1.0F), new Vector3f(sin45, 0.0F, cos45), new Vector3f(1.0F, 0.0F, 0.0F), new Vector3f(sin45, 0.0F, -cos45), new Vector3f(0.0F, 0.0F, -1.0F), new Vector3f(0.0F, 0.0F, -1.0F), new Vector3f(-sin45, 0.0F, -cos45), new Vector3f(-1.0F, 0.0F, 0.0F), new Vector3f(-sin45, 0.0F, cos45), new Vector3f(0.0F, 0.0F, 1.0F), new Vector3f(sin45, 0.0F, cos45), new Vector3f(1.0F, 0.0F, 0.0F), new Vector3f(sin45, 0.0F, -cos45), new Vector3f(0.0F, 0.0F, -1.0F)};

    private static Vector3f[] cylVerts = {cylNorms[0].mult(radius), cylNorms[1].mult(radius), cylNorms[2].mult(radius), cylNorms[3].mult(radius), cylNorms[4].mult(radius), cylNorms[5].mult(radius), cylNorms[6].mult(radius), cylNorms[7].mult(radius), cylNorms[8].mult(radius), new Vector3f(0.0F, coneHeight, 0.0F), new Vector3f(0.0F, coneHeight, 0.0F), new Vector3f(0.0F, coneHeight, 0.0F), new Vector3f(0.0F, coneHeight, 0.0F), new Vector3f(0.0F, coneHeight, 0.0F), new Vector3f(0.0F, coneHeight, 0.0F), new Vector3f(0.0F, coneHeight, 0.0F), new Vector3f(0.0F, coneHeight, 0.0F)};

    private static float[] cylTex = {0.0F, 0.0F, 0.5F, 0.0F, 1.0F, 0.0F, 1.5F, 0.0F, 2.0F, 0.0F, 2.5F, 0.0F, 3.0F, 0.0F, 3.5F, 0.0F, 4.0F, 0.0F, 0.25F, 3.0F, 0.75F, 3.0F, 1.25F, 3.0F, 1.75F, 3.0F, 2.25F, 3.0F, 2.75F, 3.0F, 3.25F, 3.0F, 3.75F, 3.0F};

    private static short[] cylIndex = {0, 1, 9, 1, 2, 10, 2, 3, 11, 3, 4, 12, 4, 5, 13, 5, 6, 14, 6, 7, 15, 7, 8, 16};
    private int trunk;
    private int branch;
    private Quaternion rotate;

    public PineTopFactory(int trunkMaterial, int branchMaterial) {
        this.trunk = trunkMaterial;
        this.branch = branchMaterial;
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

    public int createInternalGeometry(GeomPartBuffer buffer, int x, int y, int z, int xWorld, int yWorld, int zWorld, float sun, float light, BlockType block) {
        float xCenter = x + 0.5F;
        float zCenter = y + 0.5F;
        float yBase = z - 0.25F;

        GeomPart part = new GeomPart(this.trunk, -1);
        part.setSun(sun);
        part.setLight(light);

        part.setCoords(new float[]{xCenter + cylVerts[0].x, z + cylVerts[0].y, zCenter + cylVerts[0].z, xCenter + cylVerts[1].x, z + cylVerts[1].y, zCenter + cylVerts[1].z, xCenter + cylVerts[2].x, z + cylVerts[2].y, zCenter + cylVerts[2].z, xCenter + cylVerts[3].x, z + cylVerts[3].y, zCenter + cylVerts[3].z, xCenter + cylVerts[4].x, z + cylVerts[4].y, zCenter + cylVerts[4].z, xCenter + cylVerts[5].x, z + cylVerts[5].y, zCenter + cylVerts[5].z, xCenter + cylVerts[6].x, z + cylVerts[6].y, zCenter + cylVerts[6].z, xCenter + cylVerts[7].x, z + cylVerts[7].y, zCenter + cylVerts[7].z, xCenter + cylVerts[8].x, z + cylVerts[8].y, zCenter + cylVerts[8].z, xCenter + cylVerts[9].x, z + cylVerts[9].y, zCenter + cylVerts[9].z, xCenter + cylVerts[10].x, z + cylVerts[10].y, zCenter + cylVerts[10].z, xCenter + cylVerts[11].x, z + cylVerts[11].y, zCenter + cylVerts[11].z, xCenter + cylVerts[12].x, z + cylVerts[12].y, zCenter + cylVerts[12].z, xCenter + cylVerts[13].x, z + cylVerts[13].y, zCenter + cylVerts[13].z, xCenter + cylVerts[14].x, z + cylVerts[14].y, zCenter + cylVerts[14].z, xCenter + cylVerts[15].x, z + cylVerts[15].y, zCenter + cylVerts[15].z, xCenter + cylVerts[16].x, z + cylVerts[16].y, zCenter + cylVerts[16].z});

        part.setNormals(new float[]{cylNorms[0].x, cylNorms[0].y, cylNorms[0].z, cylNorms[1].x, cylNorms[1].y, cylNorms[1].z, cylNorms[2].x, cylNorms[2].y, cylNorms[2].z, cylNorms[3].x, cylNorms[3].y, cylNorms[3].z, cylNorms[4].x, cylNorms[4].y, cylNorms[4].z, cylNorms[5].x, cylNorms[5].y, cylNorms[5].z, cylNorms[6].x, cylNorms[6].y, cylNorms[6].z, cylNorms[7].x, cylNorms[7].y, cylNorms[7].z, cylNorms[8].x, cylNorms[8].y, cylNorms[8].z, cylNorms[9].x, cylNorms[9].y, cylNorms[9].z, cylNorms[10].x, cylNorms[10].y, cylNorms[10].z, cylNorms[11].x, cylNorms[11].y, cylNorms[11].z, cylNorms[12].x, cylNorms[12].y, cylNorms[12].z, cylNorms[13].x, cylNorms[13].y, cylNorms[13].z, cylNorms[14].x, cylNorms[14].y, cylNorms[14].z, cylNorms[15].x, cylNorms[15].y, cylNorms[15].z, cylNorms[16].x, cylNorms[16].y, cylNorms[16].z});

        part.setTangents(new float[]{cylTans[0].x, cylTans[0].y, cylTans[0].z, cylTans[1].x, cylTans[1].y, cylTans[1].z, cylTans[2].x, cylTans[2].y, cylTans[2].z, cylTans[3].x, cylTans[3].y, cylTans[3].z, cylTans[4].x, cylTans[4].y, cylTans[4].z, cylTans[5].x, cylTans[5].y, cylTans[5].z, cylTans[6].x, cylTans[6].y, cylTans[6].z, cylTans[7].x, cylTans[7].y, cylTans[7].z, cylTans[8].x, cylTans[8].y, cylTans[8].z, cylTans[9].x, cylTans[9].y, cylTans[9].z, cylTans[10].x, cylTans[10].y, cylTans[10].z, cylTans[11].x, cylTans[11].y, cylTans[11].z, cylTans[12].x, cylTans[12].y, cylTans[12].z, cylTans[13].x, cylTans[13].y, cylTans[13].z, cylTans[14].x, cylTans[14].y, cylTans[14].z, cylTans[15].x, cylTans[15].y, cylTans[15].z, cylTans[16].x, cylTans[16].y, cylTans[16].z});

        part.setTexCoords(cylTex);
        part.setIndexes(cylIndex);
        buffer.add(part);

        float baseYaw = 0.0F;
        float a = baseYaw;

        Quaternion q = new Quaternion().fromAngles(0.0F, a, 0.0F);

        Vector3f[] verts = new Vector3f[baseVerts.length];
        Vector3f[] norms = new Vector3f[baseNorms.length];

        for (int i = 0; i < verts.length; i++) {
            verts[i] = q.mult(baseVerts[i]);
            norms[i] = q.mult(baseNorms[i]);
        }

        part = new GeomPart(this.branch, -1);
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