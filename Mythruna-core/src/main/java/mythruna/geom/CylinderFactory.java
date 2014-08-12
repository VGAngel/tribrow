package mythruna.geom;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import mythruna.BlockType;
import mythruna.BoundaryShape;
import mythruna.ShapeIndex;

public class CylinderFactory implements GeomFactory {

    private static float[][] RADIALS = {cosAndSin(0.0F), cosAndSin(0.7853982F), cosAndSin(1.570796F), cosAndSin(2.356195F), cosAndSin(3.141593F), cosAndSin(3.926991F), cosAndSin(4.712389F), cosAndSin(5.497788F), cosAndSin(0.0F)};

    private static float[] texCoord = {0.0F, 0.5F, 1.0F, 1.5F, 2.0F, 2.5F, 3.0F, 3.5F, 4.0F};
    private int sideMaterialType;
    private int topMaterialType;
    private float radius;
    private boolean generateTangents = true;
    private float textureScale = 1.0F;
    private TextureType mapping = TextureType.RADIAL;
    private BoundaryShape circle;

    public CylinderFactory(int sideMaterialType, int topMaterialType, float radius) {
        this(sideMaterialType, topMaterialType, radius, radius / 0.5F, true, TextureType.RADIAL);
    }

    public CylinderFactory(int sideMaterialType, int topMaterialType, float radius, float textureScale, boolean generateTangents, TextureType mapping) {
        this.sideMaterialType = sideMaterialType;
        this.topMaterialType = topMaterialType;
        this.radius = radius;
        this.textureScale = textureScale;
        this.generateTangents = generateTangents;
        this.mapping = mapping;

        this.circle = ShapeIndex.getCircle(0.5F, 0.5F, radius);
    }

    public static float[] cosAndSin(float angle) {
        return new float[]{FastMath.cos(-angle), FastMath.sin(-angle)};
    }

    public boolean isSameShape(GeomFactory f) {
        if (f == this)
            return true;
        if ((f == null) || (f.getClass() != getClass()))
            return false;
        CylinderFactory other = (CylinderFactory) f;
        if (other.radius != this.radius) {
            return false;
        }
        return true;
    }

    public float getRadius() {
        return this.radius;
    }

    public int createGeometry(GeomPartBuffer buffer, int x, int y, int z, int xWorld, int yWorld, int zWorld, float sun, float light, BlockType block, int dir) {
        if ((dir != 4) && (dir != 5)) {
            return 0;
        }
        float xCenter = x + 0.5F;
        float zCenter = y + 0.5F;

        float ySurface = z;
        float normalDir = -1.0F;
        if (dir == 4) {
            ySurface = z + 1;
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

        int v = 27;
        coords[v] = xCenter;
        coords[(v + 1)] = ySurface;
        coords[(v + 2)] = zCenter;

        normals[v] = 0.0F;
        normals[(v + 1)] = normalDir;
        normals[(v + 2)] = 0.0F;

        tangents[v] = normalDir;
        tangents[(v + 1)] = 0.0F;
        tangents[(v + 2)] = 0.0F;

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
        float xCenter = x + 0.5F;
        float zCenter = y + 0.5F;
        float yBase = z;
        float yTop = z + 1.0F;

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

            coords[vIndex] = (xCenter + radial[0] * this.radius);
            coords[(vIndex + 1)] = yBase;
            coords[(vIndex + 2)] = (zCenter + radial[1] * this.radius);

            normals[vIndex] = radial[0];
            normals[(vIndex + 1)] = 0.0F;
            normals[(vIndex + 2)] = radial[1];

            tangents[vIndex] = radial[1];
            tangents[(vIndex + 1)] = 0.0F;
            tangents[(vIndex + 2)] = (-radial[0]);

            texCoords[tIndex] = (s * this.textureScale);
            texCoords[(tIndex + 1)] = 0.0F;

            vIndex += 3;
            tIndex += 2;

            coords[vIndex] = (xCenter + radial[0] * this.radius);
            coords[(vIndex + 1)] = yTop;
            coords[(vIndex + 2)] = (zCenter + radial[1] * this.radius);

            normals[vIndex] = radial[0];
            normals[(vIndex + 1)] = 0.0F;
            normals[(vIndex + 2)] = radial[1];

            tangents[vIndex] = radial[1];
            tangents[(vIndex + 1)] = 0.0F;
            tangents[(vIndex + 2)] = (-radial[0]);

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
        if ((direction != 4) && (direction != 5))
            return false;
        return true;
    }

    public BoundaryShape getBoundaryShape(int direction) {
        if ((direction != 4) && (direction != 5))
            return ShapeIndex.NULL_SHAPE;
        return this.circle;
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
        return area * getMax().z - getMin().z;
    }

    public boolean isClipped() {
        return true;
    }

    public static enum TextureType {
        DECAL, DECAL_STRETCHED, RADIAL;
    }
}