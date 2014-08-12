package mythruna.geom;

import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import mythruna.BlockType;
import mythruna.BoundaryShape;
import mythruna.ShapeIndex;

public class CylinderFactory2 implements GeomFactory {

    private static float[][] RADIALS = {cosAndSin(0.0F), cosAndSin(0.7853982F), cosAndSin(1.570796F), cosAndSin(2.356195F), cosAndSin(3.141593F), cosAndSin(3.926991F), cosAndSin(4.712389F), cosAndSin(5.497788F), cosAndSin(0.0F)};

    private static float[] texCoord = {0.0F, 0.5F, 1.0F, 1.5F, 2.0F, 2.5F, 3.0F, 3.5F, 4.0F};

    private Vector3f[] axes = {new Vector3f(1.0F, 1.0F, 0.0F).normalizeLocal(), new Vector3f(-1.0F, 1.0F, 0.0F).normalizeLocal(), new Vector3f(0.0F, 0.0F, 1.0F)};
    private int sideMaterialType;
    private int topMaterialType;
    private int dir = -1;
    private float radius;
    private boolean generateTangents = true;
    private float textureScale = 1.0F;
    private TextureType mapping = TextureType.RADIAL;
    private float height = FastMath.sqrt(2.0F) * 0.5F;
    private Vector3f offset;
    private Quaternion rotation = new Quaternion();

    public CylinderFactory2(int sideMaterialType, int topMaterialType, float radius, int dir) {
        this(sideMaterialType, topMaterialType, radius, radius / 0.5F, true, TextureType.RADIAL, dir);
    }

    public CylinderFactory2(int sideMaterialType, int topMaterialType, float radius, float height, Quaternion rotation) {
        this(sideMaterialType, topMaterialType, radius, radius / 0.5F, true, TextureType.RADIAL, height, rotation);
    }

    public CylinderFactory2(int sideMaterialType, int topMaterialType, float radius, float height, Quaternion rotation, Vector3f offset) {
        this(sideMaterialType, topMaterialType, radius, radius / 0.5F, true, TextureType.RADIAL, height, rotation, offset);
    }

    public CylinderFactory2(int sideMaterialType, int topMaterialType, float radius, float textureScale, boolean generateTangents, TextureType mapping, int dir) {
        this.sideMaterialType = sideMaterialType;
        this.topMaterialType = topMaterialType;
        this.radius = radius;
        this.textureScale = textureScale;
        this.generateTangents = generateTangents;
        this.mapping = mapping;
        this.dir = dir;

        switch (dir) {
            case 2:
                this.rotation.fromAngles(0.0F, 0.0F, -0.7853982F);
                this.offset = new Vector3f(-radius, 0.0F, 0.0F);
                break;
            case 3:
                this.rotation.fromAngles(0.0F, 0.0F, 0.7853982F);
                this.offset = new Vector3f(radius, 0.0F, 0.0F);
                break;
            case 1:
                this.rotation.fromAngles(0.7853982F, 0.0F, 0.0F);
                this.offset = new Vector3f(0.0F, 0.0F, -radius);
                break;
            case 0:
                this.rotation.fromAngles(-0.7853982F, 0.0F, 0.0F);
                this.offset = new Vector3f(0.0F, 0.0F, radius);
        }
    }

    public CylinderFactory2(int sideMaterialType, int topMaterialType, float radius, float textureScale, boolean generateTangents, TextureType mapping, float height, Quaternion rotation) {
        this(sideMaterialType, topMaterialType, radius, textureScale, generateTangents, mapping, height, rotation, new Vector3f(0.0F, 0.0F, 0.0F));
    }

    public CylinderFactory2(int sideMaterialType, int topMaterialType, float radius, float textureScale, boolean generateTangents, TextureType mapping, float height, Quaternion rotation, Vector3f offset) {
        this.sideMaterialType = sideMaterialType;
        this.topMaterialType = topMaterialType;
        this.radius = radius;
        this.textureScale = textureScale;
        this.generateTangents = generateTangents;
        this.mapping = mapping;
        this.dir = this.dir;

        this.rotation.set(rotation);
        this.height = height;

        this.offset = new Vector3f(offset.x, offset.z, offset.y);
    }

    public static float[] cosAndSin(float angle) {
        return new float[]{FastMath.cos(-angle), FastMath.sin(-angle)};
    }

    public boolean isSameShape(GeomFactory f) {
        if (f == this)
            return true;
        if ((f == null) || (f.getClass() != getClass()))
            return false;
        CylinderFactory2 other = (CylinderFactory2) f;
        if (other.radius != this.radius)
            return false;
        if (other.dir != this.dir)
            return false;
        if (other.height != this.height)
            return false;
        if (!this.offset.equals(other.offset))
            return false;
        if (!this.rotation.equals(other.rotation)) {
            return false;
        }
        return true;
    }

    public float getRadius() {
        return this.radius;
    }

    public int getDir() {
        return this.dir;
    }

    public float getHeight() {
        return this.height;
    }

    public Vector3f getOffset() {
        return this.offset;
    }

    public Quaternion getRotation() {
        return this.rotation;
    }

    public int createGeometry(GeomPartBuffer buffer, int x, int y, int z, int xWorld, int yWorld, int zWorld, float sun, float light, BlockType block, int dir) {
        return 0;
    }

    protected int createEndCaps(GeomPartBuffer buffer, int x, int y, int z, int xWorld, int yWorld, int zWorld, float sun, float light, BlockType block, int dir) {
        float xCenter = x + 0.5F + this.offset.x;
        float yCenter = z + 0.5F + this.offset.y;
        float zCenter = y + 0.5F + this.offset.z;

        float ySurface = -this.height;
        float normalDir = -1.0F;
        if (dir == 4) {
            ySurface = this.height;
            normalDir = 1.0F;
        }

        GeomPart part = new GeomPart(this.topMaterialType, dir);
        part.setSun(sun);
        part.setLight(light);

        if (this.mapping != TextureType.RADIAL) {
            float[] coords = new float[24];
            float[] normals = new float[24];
            float[] tangents = new float[24];

            for (int i = 0; i < 8; i++) {
                int v = i * 3;
                float[] radial = RADIALS[i];
                coords[v] = (xCenter + radial[0] * this.radius);
                coords[(v + 1)] = ySurface;
                coords[(v + 2)] = (zCenter + radial[1] * this.radius);

                normals[v] = 0.0F;
                normals[(v + 1)] = normalDir;
                normals[(v + 2)] = 0.0F;

                tangents[v] = normalDir;
                tangents[(v + 1)] = 0.0F;
                tangents[(v + 2)] = 0.0F;
            }

            part.setCoords(coords);
            part.setNormals(normals);
            part.setTangents(tangents);

            if (this.mapping == TextureType.DECAL_STRETCHED) {
                part.setTexCoords(new float[]{1.0F, 0.5F, 1.0F, 1.0F, 0.5F, 1.0F, 0.0F, 1.0F, 0.0F, 0.5F, 0.0F, 0.0F, 0.5F, 0.0F, 1.0F, 0.0F});
            } else {
                float[] tc = new float[16];
                int index = 0;
                for (int i = 0; i < 8; i++) {
                    tc[(index++)] = (0.5F + RADIALS[i][0] * 0.5F);
                    tc[(index++)] = (0.5F + RADIALS[i][1] * 0.5F);
                }
                part.setTexCoords(tc);
            }

            if (dir == 4) {
                part.setIndexes(new short[]{1, 2, 3, 0, 1, 3, 0, 3, 4, 0, 4, 7, 7, 4, 5, 7, 5, 6});
            } else {
                part.setIndexes(new short[]{1, 3, 2, 0, 3, 1, 0, 4, 3, 0, 7, 4, 7, 5, 4, 7, 6, 5});
            }

            buffer.add(part);
            return 1;
        }

        float[] coords = new float[30];
        float[] normals = new float[30];
        float[] tangents = new float[30];

        for (int i = 0; i < 9; i++) {
            int v = i * 3;
            float[] radial = RADIALS[i];

            Vector3f coord = new Vector3f(radial[0] * this.radius, ySurface, radial[1] * this.radius);
            coord = this.rotation.mult(coord);
            coords[v] = (xCenter + coord.x);
            coords[(v + 1)] = (yCenter + coord.y);
            coords[(v + 2)] = (zCenter + coord.z);

            Vector3f norm = new Vector3f(0.0F, normalDir, 0.0F);
            norm = this.rotation.mult(norm);
            normals[v] = norm.x;
            normals[(v + 1)] = norm.y;
            normals[(v + 2)] = norm.z;

            Vector3f tangent = new Vector3f(normalDir, 0.0F, 0.0F);
            tangent = this.rotation.mult(tangent);
            tangents[v] = tangent.x;
            tangents[(v + 1)] = tangent.y;
            tangents[(v + 2)] = tangent.z;
        }

        int v = 27;
        Vector3f coord = new Vector3f(0.0F, ySurface, 0.0F);
        coord = this.rotation.mult(coord);
        coords[v] = (xCenter + coord.x);
        coords[(v + 1)] = (yCenter + coord.y);
        coords[(v + 2)] = (zCenter + coord.z);

        Vector3f norm = new Vector3f(0.0F, normalDir, 0.0F);
        norm = this.rotation.mult(norm);
        normals[v] = norm.x;
        normals[(v + 1)] = norm.y;
        normals[(v + 2)] = norm.z;

        Vector3f tangent = new Vector3f(normalDir, 0.0F, 0.0F);
        tangent = this.rotation.mult(tangent);
        tangents[v] = tangent.x;
        tangents[(v + 1)] = tangent.y;
        tangents[(v + 2)] = tangent.z;

        part.setCoords(coords);
        part.setNormals(normals);
        part.setTangents(tangents);

        part.setTexCoords(new float[]{1.0F, 0.5F, 1.0F, 1.0F, 0.5F, 1.0F, 0.0F, 1.0F, 0.0F, 0.5F, 0.0F, 0.0F, 0.5F, 0.0F, 1.0F, 0.0F, 1.0F, 0.5F, 0.5F, 0.5F});

        if (dir == 4) {
            part.setIndexes(new short[]{0, 1, 9, 1, 2, 9, 2, 3, 9, 3, 4, 9, 4, 5, 9, 5, 6, 9, 6, 7, 9, 7, 8, 9});
        } else {
            part.setIndexes(new short[]{0, 9, 1, 1, 9, 2, 2, 9, 3, 3, 9, 4, 4, 9, 5, 5, 9, 6, 6, 9, 7, 7, 9, 8});
        }

        buffer.add(part);
        return 1;
    }

    public int createInternalGeometry(GeomPartBuffer buffer, int x, int y, int z, int xWorld, int yWorld, int zWorld, float sun, float light, BlockType block) {
        createEndCaps(buffer, x, y, z, xWorld, yWorld, zWorld, sun, light, block, 4);
        createEndCaps(buffer, x, y, z, xWorld, yWorld, zWorld, sun, light, block, 5);

        float xCenter = x + 0.5F + this.offset.x;
        float yCenter = z + 0.5F + this.offset.y;
        float zCenter = y + 0.5F + this.offset.z;
        float yBase = -this.height;
        float yTop = this.height;

        float[] coords = new float[RADIALS.length * 3 * 2];
        float[] normals = new float[RADIALS.length * 3 * 2];
        float[] tangents = new float[RADIALS.length * 3 * 2];
        float[] texCoords = new float[RADIALS.length * 2 * 2];
        short[] indexes = new short[(RADIALS.length - 1) * 3 * 2];

        int vIndex = 0;
        int tIndex = 0;
        int iIndex = 0;
        for (int i = 0; i < RADIALS.length; i++) {
            float[] radial;
            if (i < RADIALS.length)
                radial = RADIALS[i];
            else {
                radial = RADIALS[0];
            }
            float s = texCoord[i];

            Vector3f coord = new Vector3f(radial[0] * this.radius, yBase, radial[1] * this.radius);
            coord = this.rotation.mult(coord);
            coords[vIndex] = (xCenter + coord.x);
            coords[(vIndex + 1)] = (yCenter + coord.y);
            coords[(vIndex + 2)] = (zCenter + coord.z);

            Vector3f norm = new Vector3f(radial[0], 0.0F, radial[1]);
            norm = this.rotation.mult(norm);
            normals[vIndex] = norm.x;
            normals[(vIndex + 1)] = norm.y;
            normals[(vIndex + 2)] = norm.z;

            Vector3f tangent = new Vector3f(radial[1], 0.0F, -radial[0]);
            tangent = this.rotation.mult(tangent);
            tangents[vIndex] = tangent.x;
            tangents[(vIndex + 1)] = tangent.y;
            tangents[(vIndex + 2)] = tangent.z;

            texCoords[tIndex] = (s * this.textureScale);
            texCoords[(tIndex + 1)] = 0.0F;

            vIndex += 3;
            tIndex += 2;

            coord = new Vector3f(radial[0] * this.radius, yTop, radial[1] * this.radius);
            coord = this.rotation.mult(coord);
            coords[vIndex] = (xCenter + coord.x);
            coords[(vIndex + 1)] = (yCenter + coord.y);
            coords[(vIndex + 2)] = (zCenter + coord.z);

            normals[vIndex] = norm.x;
            normals[(vIndex + 1)] = norm.y;
            normals[(vIndex + 2)] = norm.z;

            tangents[vIndex] = tangent.x;
            tangents[(vIndex + 1)] = tangent.y;
            tangents[(vIndex + 2)] = tangent.z;

            texCoords[tIndex] = (s * this.textureScale);
            texCoords[(tIndex + 1)] = 1.0F;

            vIndex += 3;
            tIndex += 2;

            if (i < RADIALS.length - 1) {
                int baseVertex = i * 2;

                indexes[(iIndex++)] = (short) baseVertex;
                indexes[(iIndex++)] = (short) (baseVertex + 2);
                indexes[(iIndex++)] = (short) (baseVertex + 3);
                indexes[(iIndex++)] = (short) baseVertex;
                indexes[(iIndex++)] = (short) (baseVertex + 3);
                indexes[(iIndex++)] = (short) (baseVertex + 1);
            }
        }

        GeomPart part = new GeomPart(this.sideMaterialType, 4);
        part.setSun(sun);
        part.setLight(light);

        part.setCoords(coords);
        part.setNormals(normals);
        part.setTangents(tangents);
        part.setTexCoords(texCoords);
        part.setIndexes(indexes);
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
        return Vector3f.ZERO;
    }

    public Vector3f getMax() {
        return Vector3f.UNIT_XYZ;
    }

    public double getMassPortion() {
        double area = this.radius * this.radius * 3.141592653589793D;
        return area * this.height * 2.0F;
    }

    private Vector3f toWorld(Vector3f origin, float x, float y, float z) {
        Vector3f result = this.rotation.mult(new Vector3f(x, z, y));
        origin.x += result.x;
        result.y = (origin.z + result.y);
        result.z = (origin.y + result.z);

        float temp = result.y;
        result.y = result.z;
        result.z = temp;

        return result;
    }

    public boolean isClipped() {
        return true;
    }

    public static enum TextureType {
        DECAL, DECAL_STRETCHED, RADIAL;
    }
}