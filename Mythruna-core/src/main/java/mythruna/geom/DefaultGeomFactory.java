package mythruna.geom;

import com.jme3.math.Vector3f;
import mythruna.BlockType;
import mythruna.BoundaryShape;
import mythruna.ShapeIndex;
import mythruna.phys.Collider;

public class DefaultGeomFactory implements GeomFactory {

    public static final Vector3f UNIT_CUBE = new Vector3f(1.0F, 1.0F, 1.0F);
    public static final BoundaryShape UNIT_SQUARE = ShapeIndex.getRect(0.0F, 0.0F, 1.0F, 1.0F);

    private PartFactory[] dirParts = new PartFactory[6];
    private PartFactory internalParts;
    private Vector3f min;
    private Vector3f max;
    private double volume;
    private Collider collider;
    private boolean clipped;
    private boolean allSolid = false;
    private boolean[] solid = new boolean[6];
    private float[] transparency = {0.0F, 0.0F, 0.0F};

    public DefaultGeomFactory(Collider collider, float[] transparency, Double volume, boolean clipped, PartFactory[] dirParts, PartFactory internalParts) {
        this.collider = collider;
        this.dirParts = dirParts;
        this.internalParts = internalParts;
        this.clipped = clipped;

        this.min = new Vector3f(1.0F, 1.0F, 1.0F);
        this.max = new Vector3f(0.0F, 0.0F, 0.0F);

        int solidCount = 0;
        for (int i = 0; i < dirParts.length; i++) {
            if (dirParts[i] != null) {
                this.min.minLocal(dirParts[i].getMin());
                this.max.maxLocal(dirParts[i].getMax());

                BoundaryShape shape = dirParts[i].getBoundaryShape();

                if (shape.getArea() >= 1.0F) {
                    this.solid[i] = true;
                    solidCount++;
                }
            }
        }
        this.allSolid = (solidCount == 6);

        if (internalParts != null) {
            this.min.minLocal(internalParts.getMin());
            this.max.maxLocal(internalParts.getMax());
        }

        if (transparency == null) {
            float xAxis = Math.max(getBoundaryShape(2).getArea(), getBoundaryShape(3).getArea());

            float yAxis = Math.max(getBoundaryShape(0).getArea(), getBoundaryShape(1).getArea());

            float zAxis = Math.max(getBoundaryShape(4).getArea(), getBoundaryShape(5).getArea());

            this.transparency[0] = (1.0F - xAxis);
            this.transparency[1] = (1.0F - yAxis);
            this.transparency[2] = (1.0F - zAxis);
        } else {
            for (int i = 0; i < 3; i++) {
                this.transparency[i] = transparency[i];
            }
        }
        if (volume != null) {
            this.volume = volume.doubleValue();
        } else {
            double x = getMax().x - getMin().x;
            double y = getMax().y - getMin().y;
            double z = getMax().z - getMin().z;

            this.volume = (x * y * z);
        }
    }

    public boolean isSameShape(GeomFactory f) {
        if (f == this)
            return true;
        if ((f == null) || (f.getClass() != getClass())) {
            return false;
        }
        DefaultGeomFactory other = (DefaultGeomFactory) f;

        if (!other.collider.equals(this.collider)) {
            return false;
        }
        return true;
    }

    public int createGeometry(GeomPartBuffer buffer, int x, int y, int z, int xWorld, int yWorld, int zWorld, float sun, float light, BlockType block, int dir) {
        if (this.dirParts[dir] == null) {
            return 0;
        }
        return this.dirParts[dir].addParts(buffer, x, y, z, xWorld, yWorld, zWorld, sun, light, block, dir);
    }

    public int createInternalGeometry(GeomPartBuffer buffer, int x, int y, int z, int xWorld, int yWorld, int zWorld, float sun, float light, BlockType block) {
        if (this.internalParts == null) {
            return 0;
        }
        return this.internalParts.addParts(buffer, x, y, z, xWorld, yWorld, zWorld, sun, light, block, -1);
    }

    public Collider getCollider() {
        return this.collider;
    }

    public final float getTransparency(int axis) {
        return this.transparency[axis];
    }

    public final boolean isSolid(int direction) {
        return this.solid[direction];
    }

    public final boolean isSolid() {
        return this.allSolid;
    }

    public final boolean isBoundary(int direction) {
        return this.dirParts[direction] != null;
    }

    public final BoundaryShape getBoundaryShape(int direction) {
        if (this.dirParts[direction] == null) {
            return ShapeIndex.NULL_SHAPE;
        }
        return this.dirParts[direction].getBoundaryShape();
    }

    public final Vector3f getMin() {
        return this.min;
    }

    public final Vector3f getMax() {
        return this.max;
    }

    public final double getMassPortion() {
        return this.volume;
    }

    public final boolean isClipped() {
        return this.clipped;
    }
}