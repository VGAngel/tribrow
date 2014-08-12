package mythruna.client.tabs.bp;

import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import mythruna.BlockType;

import java.util.Iterator;

public class BlueprintIntersector implements Iterator<BlueprintIntersector.Intersection> {
    private int[][][] cells;
    private int size;
    private Ray ray;
    private PlaneIntersector[] planeIntersects;
    private float distance;
    private Intersection next;

    public BlueprintIntersector(int[][][] cells, int size, Ray ray) {
        this.cells = cells;
        this.size = size;
        this.ray = ray;

        this.planeIntersects = new PlaneIntersector[]{new PlaneIntersector(ray, 0), new PlaneIntersector(ray, 1), new PlaneIntersector(ray, 2)};
    }

    private int minIndex(float[] vals) {
        int minIndex = 0;
        float min = (1.0F / 1.0F);
        for (int i = 0; i < vals.length; i++) {
            if (vals[i] != (0.0F / 0.0F)) {
                if (vals[i] < min) {
                    min = vals[i];
                    minIndex = i;
                }
            }
        }
        return minIndex;
    }

    protected int getBlockType(int x, int y, int z) {
        if (z == -1)
            return 1;
        if ((x < 0) || (y < 0) || (z < 0))
            return 0;
        if ((x >= this.size) || (y >= this.size) || (z >= this.size))
            return 0;
        return this.cells[x][y][z];
    }

    protected void fetch() {
        if (this.next != null)
            return;
        float[] dists;
        int minIndex;
        Vector3f blockIntersect;
        Vector3f intersect;
        int type;
        BlockType blockType;
        while (this.distance < this.ray.getLimit()) {
            dists = new float[]{this.planeIntersects[0].getNextLength(), this.planeIntersects[1].getNextLength(), this.planeIntersects[2].getNextLength()};

            minIndex = minIndex(dists);

            this.distance = this.planeIntersects[minIndex].getNextLength();
            blockIntersect = this.planeIntersects[minIndex].getNextBlockIntersect();

            intersect = this.planeIntersects[minIndex].next();

            type = getBlockType((int) blockIntersect.x, (int) blockIntersect.y, (int) blockIntersect.z);

            if (type != 0) {
                blockType = mythruna.BlockTypeIndex.types[type];
                if (blockType.intersects(blockIntersect, this.ray.getOrigin(), this.ray.getDirection())) {
                    int side = -1;
                    float epsilon = 0.001F;
                    if (Math.abs(blockIntersect.x - intersect.x) < epsilon)
                        side = 3;
                    else if (Math.abs(blockIntersect.x + 1.0F - intersect.x) < epsilon)
                        side = 2;
                    else if (Math.abs(blockIntersect.y - intersect.y) < epsilon)
                        side = 0;
                    else if (Math.abs(blockIntersect.y + 1.0F - intersect.y) < epsilon)
                        side = 1;
                    else if (Math.abs(blockIntersect.z - intersect.z) < epsilon)
                        side = 5;
                    else if (Math.abs(blockIntersect.z + 1.0F - intersect.z) < epsilon) {
                        side = 4;
                    }
                    if (side < 0) {
                        System.out.println("Got an intersect from:" + blockType + " at:" + blockIntersect + " but it doesn't match:" + intersect);
                    } else {
                        if ((!blockType.isSolid(side)) || (!blockType.isBoundary(side))) {
                            Vector3f better = blockType.getIntersection(blockIntersect, this.ray.getOrigin(), this.ray.getDirection());
                            if (better != null) {
                                intersect = better;
                            }

                        }

                        this.next = new Intersection(intersect, blockIntersect, type, side);
                    }
                }
            }
        }
    }

    public boolean hasNext() {
        fetch();
        return this.next != null;
    }

    public Intersection next() {
        Intersection result = this.next;
        this.next = null;
        return result;
    }

    public void remove() {
        throw new UnsupportedOperationException("Remove not supported on intersectors.");
    }

    private static class PlaneIntersector {
        private Vector3f start;
        private int axis = 0;
        private int step = 0;
        private float sign;
        private Vector3f dir;
        private float dirLength;
        private float nextLength;

        public PlaneIntersector(Ray ray, int axis) {
            this(ray.getOrigin(), ray.getDirection(), axis);
        }

        public PlaneIntersector(Vector3f pos, Vector3f heading, int axis) {
            this.axis = axis;

            this.sign = Math.signum(heading.get(axis));
            this.dir = heading.mult(this.sign / heading.get(axis));
            if (this.sign == 0.0F)
                this.dirLength = (1.0F / 1.0F);
            else {
                this.dirLength = this.dir.length();
            }

            float grid = (float) Math.floor(pos.get(axis));
            if (this.sign > 0.0F) {
                grid += 1.0F;
            }

            float scale = (grid - pos.get(axis)) / this.sign;

            this.start = this.dir.mult(scale);
            this.nextLength = this.start.length();

            this.start.addLocal(pos);
        }

        public float getSign() {
            return this.sign;
        }

        public float getNextLength() {
            return this.nextLength;
        }

        public Vector3f getNextBlockIntersect() {
            Vector3f pos = getNextIntersect();
            Vector3f result = new Vector3f();
            result.x = (int) Math.floor(pos.x);
            result.y = (int) Math.floor(pos.y);
            result.z = (int) Math.floor(pos.z);

            if (this.sign < 0.0F) {
                result.set(this.axis, result.get(this.axis) - 1.0F);
            }
            return result;
        }

        public Vector3f getNextIntersect() {
            return this.start.add(this.dir.mult(this.step));
        }

        public Vector3f next() {
            Vector3f last = getNextIntersect();
            this.nextLength += this.dirLength;
            this.step += 1;
            return last;
        }
    }

    public static class Intersection {
        private Vector3f point;
        private Vector3f block;
        private int type;
        private int side;

        public Intersection(Vector3f point, Vector3f block, int type, int side) {
            this.point = point;
            this.block = block;
            this.type = type;
            this.side = side;
        }

        public boolean equals(Object o) {
            if (o == this)
                return true;
            if (o == null)
                return false;
            if (o.getClass() != getClass())
                return false;
            Intersection other = (Intersection) o;
            if (other.type != this.type)
                return false;
            if (other.side != this.side)
                return false;
            if (other.block != this.block)
                return false;
            return true;
        }

        public int getType() {
            return this.type;
        }

        public Vector3f getPoint() {
            return this.point;
        }

        public int getSide() {
            return this.side;
        }

        public Vector3f getBlock() {
            return this.block;
        }

        public String toString() {
            return "Intersection[block:" + this.block + ", hit:" + this.point + ", type:" + this.type + ", side:" + this.side + "]";
        }
    }
}
