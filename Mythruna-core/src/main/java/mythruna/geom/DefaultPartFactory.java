package mythruna.geom;

import com.jme3.math.Vector3f;
import mythruna.BlockType;
import mythruna.BoundaryShape;

public class DefaultPartFactory implements PartFactory {

    private GeomPart[] templates;
    private BoundaryShape shape;
    private Vector3f min;
    private Vector3f max;

    public DefaultPartFactory(GeomPart[] templates) {
        this(null, templates);
    }

    public DefaultPartFactory(BoundaryShape shape, GeomPart[] templates) {
        this.templates = templates;
        this.shape = shape;

        this.min = new Vector3f(1.0F, 1.0F, 1.0F);
        this.max = new Vector3f(0.0F, 0.0F, 0.0F);

        for (GeomPart p : templates) {
            float[] coords = p.getCoords();
            for (int i = 0; i < coords.length; i += 3) {
                float x = coords[i] + 0.5F;
                float z = coords[(i + 1)] + 0.5F;
                float y = coords[(i + 2)] + 0.5F;

                Vector3f pos = new Vector3f(x, y, z);

                this.min.minLocal(pos);
                this.max.maxLocal(pos);
            }
        }
    }

    public BoundaryShape getBoundaryShape() {
        return this.shape;
    }

    public Vector3f getMin() {
        return this.min;
    }

    public Vector3f getMax() {
        return this.max;
    }

    public int addParts(GeomPartBuffer buffer, int x, int y, int z, int xWorld, int yWorld, int zWorld, float sun, float light, BlockType block, int dir) {
        float xCenter = x + 0.5F;
        float yCenter = y + 0.5F;
        float zCenter = z + 0.5F;

        int count = 0;
        for (GeomPart t : this.templates) {
            GeomPart part = new GeomPart(t.getMaterialType(), t.getDirection());
            part.setSun(sun);
            part.setLight(light);

            float[] pos = (float[]) t.getCoords().clone();
            int xIndex = 0;
            int yIndex = 1;
            int zIndex = 2;
            while (zIndex < pos.length) {
                pos[xIndex] += xCenter;
                pos[yIndex] += zCenter;
                pos[zIndex] += yCenter;

                xIndex += 3;
                yIndex += 3;
                zIndex += 3;
            }

            part.setCoords(pos);

            if (t.getNormals() != null)
                part.setNormals(t.getNormals());
            if (t.getNormals() != null)
                part.setNormals(t.getNormals());
            if (t.getTangents() != null)
                part.setTangents(t.getTangents());
            if (t.getTexCoords() != null)
                part.setTexCoords(t.getTexCoords());
            if (t.getIndexes() != null)
                part.setIndexes(t.getIndexes());
            if (t.getColors() != null)
                part.setColors(t.getColors());
            if (t.getSizes() != null) {
                part.setSizes(t.getSizes());
            }
            buffer.add(part);
            count++;
        }

        return count;
    }
}