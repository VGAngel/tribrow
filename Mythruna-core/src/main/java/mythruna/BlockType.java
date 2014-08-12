package mythruna;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import mythruna.geom.GeomFactory;

public class BlockType {
    private static int nextIndex = 0;
    private int id;
    private int orderingIndex = nextIndex++;
    private String name;
    private MaterialType material;
    private boolean opaque;
    private int group;
    private Vector3f min = new Vector3f(0.0F, 0.0F, 0.0F);
    private Vector3f max = new Vector3f(1.0F, 1.0F, 1.0F);
    private BoundingBox bounds;
    private boolean stackable = true;
    private GeomFactory geomFactory;
    public int transformIndex = -1;

    public BlockType(int id, String name, MaterialType material, boolean opaque, int group, GeomFactory geomFactory) {
        this.id = id;
        this.name = name;
        this.material = material;
        this.opaque = opaque;
        this.group = group;
        this.geomFactory = geomFactory;
        this.min = geomFactory.getMin();
        this.max = geomFactory.getMax();
    }

    public BlockType(int id, String name, MaterialType material, boolean opaque, int group, float height, GeomFactory geomFactory) {
        this.id = id;
        this.name = name;
        this.material = material;
        this.opaque = opaque;
        this.group = group;
        this.geomFactory = geomFactory;
        this.max.z = height;
    }

    public BlockType(int id, String name, MaterialType material, boolean opaque, int group, Vector3f v1, Vector3f v2, GeomFactory geomFactory) {
        this.id = id;
        this.name = name;
        this.material = material;
        this.opaque = opaque;
        this.group = group;
        this.geomFactory = geomFactory;
        this.min = v1;
        this.max = v2;
    }

    public final int getId() {
        return this.id;
    }

    public final int getSortingIndex() {
        return this.orderingIndex;
    }

    public final String getName() {
        return this.name;
    }

    public final MaterialType getMaterial() {
        return this.material;
    }

    public final Vector3f getMin() {
        return this.min;
    }

    public final Vector3f getMax() {
        return this.max;
    }

    public final BoundingBox getBounds() {
        if (this.bounds == null)
            this.bounds = new BoundingBox(this.min, this.max);
        return this.bounds;
    }

    public final float getTransparency(int axis) {
        return this.geomFactory.getTransparency(axis);
    }

    public boolean intersects(Vector3f blockLocation, Vector3f start, Vector3f direction) {
        Ray ray = new Ray(start, direction);

        BoundingBox b = new BoundingBox(blockLocation.add(this.min), blockLocation.add(this.max));

        return b.intersects(ray);
    }

    public Vector3f getIntersection(Vector3f blockLocation, Vector3f start, Vector3f direction) {
        Vector3f localMin = blockLocation.add(this.min).subtract(start);
        Vector3f localMax = blockLocation.add(this.max).subtract(start);

        float xLen1 = localMin.x / direction.x;
        float xLen2 = localMax.x / direction.x;

        float near = Math.min(xLen1, xLen2);
        float far = Math.max(xLen1, xLen2);

        float yLen1 = localMin.y / direction.y;
        float yLen2 = localMax.y / direction.y;

        float yNear = Math.min(yLen1, yLen2);
        float yFar = Math.max(yLen1, yLen2);

        near = Math.max(near, yNear);
        far = Math.min(far, yFar);

        if (near > far) {
            return null;
        }

        float zLen1 = localMin.z / direction.z;
        float zLen2 = localMax.z / direction.z;

        float zNear = Math.min(zLen1, zLen2);
        float zFar = Math.max(zLen1, zLen2);

        near = Math.max(near, zNear);
        far = Math.min(far, zFar);
        if (near > far) {
            return null;
        }

        return start.add(direction.mult(near));
    }

    public final boolean isSolid(int direction) {
        return this.geomFactory.isSolid(direction);
    }

    public final boolean isBoundary(int direction) {
        return this.geomFactory.isBoundary(direction);
    }

    public final boolean isSolid() {
        return (this.opaque) && (this.geomFactory.isSolid());
    }

    public final BoundaryShape getBoundary(int direction) {
        return this.geomFactory.getBoundaryShape(direction);
    }

    public final boolean isTransparent() {
        return !this.opaque;
    }

    public final int getGroup() {
        return this.group;
    }

    public final GeomFactory getGeomFactory() {
        return this.geomFactory;
    }

    public String toString() {
        return "BlockType[" + this.id + ":" + this.name + "]";
    }
}