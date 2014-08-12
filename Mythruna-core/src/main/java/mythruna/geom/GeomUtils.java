package mythruna.geom;

import com.jme3.math.Vector3f;

public class GeomUtils {

    public GeomUtils() {
    }

    public static GeomPart createQuad(float x, float y, float z, Vector3f min, Vector3f max, boolean includeNormals, int dir, int materialType, float sun, float light) {
        GeomPart part = new GeomPart(materialType, dir);
        part.setSun(sun);
        part.setLight(light);

        float mx = x;
        float my = z;
        float mz = y;

        float x1 = mx + min.x;
        float x2 = mx + max.x;
        float y1 = my + min.z;
        float y2 = my + max.z;
        float z1 = mz + min.y;
        float z2 = mz + max.y;

        switch (dir) {
            case 0:
                part.setCoords(new float[]{x2, y1, z1, x1, y1, z1, x1, y2, z1, x2, y2, z1});

                break;
            case 1:
                part.setCoords(new float[]{x1, y1, z2, x2, y1, z2, x2, y2, z2, x1, y2, z2});

                break;
            case 2:
                part.setCoords(new float[]{x2, y1, z2, x2, y1, z1, x2, y2, z1, x2, y2, z2});

                break;
            case 3:
                part.setCoords(new float[]{x1, y1, z1, x1, y1, z2, x1, y2, z2, x1, y2, z1});

                break;
            case 4:
                part.setCoords(new float[]{x1, y2, z2, x2, y2, z2, x2, y2, z1, x1, y2, z1});

                break;
            case 5:
                part.setCoords(new float[]{x1, y1, z1, x2, y1, z1, x2, y1, z2, x1, y1, z2});
        }

        part.setTexCoords(new float[]{0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 1.0F});

        if (includeNormals) {
            float[] norms = mythruna.Direction.NORMALS[dir];

            float[] tangents = mythruna.Direction.TANGENTS[dir];
            part.setNormals(new float[]{norms[0], norms[1], norms[2], norms[0], norms[1], norms[2], norms[0], norms[1], norms[2], norms[0], norms[1], norms[2]});

            part.setTangents(new float[]{tangents[0], tangents[1], tangents[2], tangents[0], tangents[1], tangents[2], tangents[0], tangents[1], tangents[2], tangents[0], tangents[1], tangents[2]});
        }

        part.setIndexes(new short[]{0, 1, 2, 0, 2, 3});

        return part;
    }
}