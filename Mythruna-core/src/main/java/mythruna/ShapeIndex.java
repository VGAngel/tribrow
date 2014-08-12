package mythruna;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import org.progeeks.util.ObjectUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShapeIndex {

    public static final BoundaryShape NULL_SHAPE = new NullShape();

    private static Map<BoundaryShape, Integer> shapeMap = new HashMap();
    private static List<BoundaryShape> shapes = new ArrayList();

    public static final BoundaryShape UNIT_SQUARE = getRect(0.0F, 0.0F, 1.0F, 1.0F);

    public ShapeIndex() {
    }

    public static BoundaryShape getRect(float xMin, float yMin, float xMax, float yMax) {
        if ((xMin < 0.0F) || (yMin < 0.0F))
            throw new IllegalArgumentException("range out of bounds");
        if ((xMax > 1.0F) || (yMax > 1.0F))
            throw new IllegalArgumentException("range out of bounds");
        BoundaryShape key = new Rect(xMin, yMin, xMax, yMax);
        Integer result = (Integer) shapeMap.get(key);

        if (result == null) {
            result = Integer.valueOf(shapes.size());
            shapes.add(key);
            shapeMap.put(key, result);
        }

        return (BoundaryShape) shapes.get(result.intValue());
    }

    public static BoundaryShape getTriangle(float xMin, float yMin, float xMax, float yMax, float xNormal, float yNormal) {
        BoundaryShape key = new Triangle(xMin, yMin, xMax, yMax, xNormal, yNormal);
        Integer result = (Integer) shapeMap.get(key);
        if (result == null) {
            result = Integer.valueOf(shapes.size());
            shapes.add(key);
            shapeMap.put(key, result);
        }

        return (BoundaryShape) shapes.get(result.intValue());
    }

    public static BoundaryShape getCircle(float xCenter, float yCenter, float radius) {
        BoundaryShape key = new Circle(xCenter, yCenter, radius);
        Integer result = (Integer) shapeMap.get(key);
        if (result == null) {
            result = Integer.valueOf(shapes.size());
            shapes.add(key);
            shapeMap.put(key, result);
        }

        return (BoundaryShape) shapes.get(result.intValue());
    }

    protected static float epsilon(float f) {
        long i = Math.round(f * 10000.0F);
        f = (float) i / 10000.0F;

        return f;
    }

    protected static Vector3f projectPos(Vector2f v, int dir) {
        Vector3f result = new Vector3f();
        switch (dir) {
            case 0:
                result.x = (v.x - 0.5F);
                result.y = -0.5F;
                result.z = (v.y - 0.5F);
                break;
            case 1:
                result.x = (v.x - 0.5F);
                result.y = 0.5F;
                result.z = (v.y - 0.5F);
                break;
            case 2:
                result.x = 0.5F;
                result.y = (v.x - 0.5F);
                result.z = (v.y - 0.5F);
                break;
            case 3:
                result.x = -0.5F;
                result.y = (v.x - 0.5F);
                result.z = (v.y - 0.5F);
                break;
            case 4:
                result.x = (v.x - 0.5F);
                result.y = (v.y - 0.5F);
                result.z = 0.5F;
                break;
            case 5:
                result.x = (v.x - 0.5F);
                result.y = (v.y - 0.5F);
                result.z = -0.5F;
                break;
            default:
                throw new IllegalArgumentException("Unknown direction:" + dir);
        }
        return result;
    }

    protected static Vector2f unprojectPos(Vector3f v, int dir) {
        Vector2f result = new Vector2f();
        switch (dir) {
            case 0:
                result.x = epsilon(0.5F + v.x);
                result.y = epsilon(0.5F + v.z);
                break;
            case 1:
                result.x = epsilon(0.5F + v.x);
                result.y = epsilon(0.5F + v.z);
                break;
            case 2:
                result.x = epsilon(0.5F + v.y);
                result.y = epsilon(0.5F + v.z);
                break;
            case 3:
                result.x = epsilon(0.5F + v.y);
                result.y = epsilon(0.5F + v.z);
                break;
            case 4:
                result.x = epsilon(0.5F + v.x);
                result.y = epsilon(0.5F + v.y);
                break;
            case 5:
                result.x = epsilon(0.5F + v.x);
                result.y = epsilon(0.5F + v.y);
                break;
            default:
                throw new IllegalArgumentException("Unknown direction:" + dir);
        }
        return result;
    }

    protected static Vector3f projectDir(Vector2f v, int dir) {
        Vector3f result = new Vector3f();
        switch (dir) {
            case 0:
                result.x = v.x;
                result.z = v.y;
                break;
            case 1:
                result.x = v.x;
                result.z = v.y;
                break;
            case 2:
                result.y = v.x;
                result.z = v.y;
                break;
            case 3:
                result.y = v.x;
                result.z = v.y;
                break;
            case 4:
                result.x = v.x;
                result.y = v.y;
                break;
            case 5:
                result.x = v.x;
                result.y = v.y;
                break;
            default:
                throw new IllegalArgumentException("Unknown direction:" + dir);
        }
        return result;
    }

    protected static Vector2f unprojectDir(Vector3f v, int dir) {
        Vector2f result = new Vector2f();
        switch (dir) {
            case 0:
                result.x = epsilon(v.x);
                result.y = epsilon(v.z);
                break;
            case 1:
                result.x = epsilon(v.x);
                result.y = epsilon(v.z);
                break;
            case 2:
                result.x = epsilon(v.y);
                result.y = epsilon(v.z);
                break;
            case 3:
                result.x = epsilon(v.y);
                result.y = epsilon(v.z);
                break;
            case 4:
                result.x = epsilon(v.x);
                result.y = epsilon(v.y);
                break;
            case 5:
                result.x = epsilon(v.x);
                result.y = epsilon(v.y);
                break;
            default:
                throw new IllegalArgumentException("Unknown direction:" + dir);
        }

        return result;
    }

    private static class Circle
            implements BoundaryShape {
        Vector2f center;
        float radius;
        float area;

        public Circle(float xCenter, float yCenter, float radius) {
            this.center = new Vector2f(xCenter, yCenter);
            this.radius = radius;
            this.area = (float) (3.141592653589793D * radius * radius);
        }

        public BoundaryShape rotate(int dir, int dirDelta) {
            Vector3f pCenter = ShapeIndex.projectPos(this.center, dir);

            Quaternion rot = new Quaternion().fromAngles(0.0F, 0.0F, 0.01745329F * dirDelta * 90.0F);

            pCenter = rot.mult(pCenter);

            int newDir = Direction.rotate(dir, dirDelta);

            Vector2f v1 = ShapeIndex.unprojectPos(pCenter, newDir);

            BoundaryShape result = ShapeIndex.getCircle(v1.x, v1.y, this.radius);
            return result;
        }

        public boolean isMatchingFace(BoundaryShape shape) {
            return shape == this;
        }

        public float getArea() {
            return this.area;
        }

        public int hashCode() {
            return getClass().hashCode() ^ this.center.hashCode() ^ Float.floatToIntBits(this.radius);
        }

        public boolean equals(Object o) {
            if (o == this)
                return true;
            if (o == null)
                return false;
            if (o.getClass() != getClass()) {
                return false;
            }
            Circle other = (Circle) o;

            if (!ObjectUtils.areEqual(other.center, this.center))
                return false;
            if (Float.compare(other.radius, this.radius) != 0)
                return false;
            return true;
        }

        public String toString() {
            return "Circle[" + this.center + ", " + this.radius + "]";
        }
    }

    private static class Triangle
            implements BoundaryShape {
        Vector2f min;
        Vector2f max;
        Vector2f normal;
        float area;

        public Triangle(float xMin, float yMin, float xMax, float yMax, float xNormal, float yNormal) {
            this.min = new Vector2f(ShapeIndex.epsilon(xMin), ShapeIndex.epsilon(yMin));
            this.max = new Vector2f(ShapeIndex.epsilon(xMax), ShapeIndex.epsilon(yMax));
            this.normal = new Vector2f(ShapeIndex.epsilon(xNormal), ShapeIndex.epsilon(yNormal));
            this.area = ((xMax - xMin) * (yMax - yMin) * 0.5F);
        }

        public BoundaryShape rotate(int dir, int dirDelta) {
            Vector3f pMin = ShapeIndex.projectPos(this.min, dir);
            Vector3f pMax = ShapeIndex.projectPos(this.max, dir);
            Vector3f pNorm = ShapeIndex.projectDir(this.normal, dir);

            Quaternion rot = new Quaternion().fromAngles(0.0F, 0.0F, 0.01745329F * dirDelta * 90.0F);

            pMin = rot.mult(pMin);
            pMax = rot.mult(pMax);
            pNorm = rot.mult(pNorm);

            int newDir = Direction.rotate(dir, dirDelta);

            Vector2f v1 = ShapeIndex.unprojectPos(pMin, newDir);
            Vector2f v2 = ShapeIndex.unprojectPos(pMax, newDir);
            Vector2f n = ShapeIndex.unprojectDir(pNorm, newDir);

            BoundaryShape result = ShapeIndex.getTriangle(Math.min(v1.x, v2.x), Math.min(v1.y, v2.y), Math.max(v1.x, v2.x), Math.max(v1.y, v2.y), n.x, n.y);

            return result;
        }

        public boolean isMatchingFace(BoundaryShape shape) {
            return shape == this;
        }

        public float getArea() {
            return this.area;
        }

        public int hashCode() {
            return getClass().hashCode() ^ this.min.hashCode() ^ this.max.hashCode() * this.normal.hashCode();
        }

        public boolean equals(Object o) {
            if (o == this)
                return true;
            if (o == null)
                return false;
            if (o.getClass() != getClass()) {
                return false;
            }
            Triangle other = (Triangle) o;

            if (!ObjectUtils.areEqual(other.min, this.min))
                return false;
            if (!ObjectUtils.areEqual(other.max, this.max))
                return false;
            if (!ObjectUtils.areEqual(other.normal, this.normal))
                return false;
            return true;
        }

        public String toString() {
            return "Triangle[" + this.min + ", " + this.max + ", norm:" + this.normal + "]";
        }
    }

    private static class Rect
            implements BoundaryShape {
        Vector2f min;
        Vector2f max;
        float area;

        public Rect(float xMin, float yMin, float xMax, float yMax) {
            this.min = new Vector2f(xMin, yMin);
            this.max = new Vector2f(xMax, yMax);
            this.area = ((xMax - xMin) * (yMax - yMin));
        }

        public float getArea() {
            return this.area;
        }

        public BoundaryShape rotate(int dir, int dirDelta) {
            Vector3f pMin = ShapeIndex.projectPos(this.min, dir);
            Vector3f pMax = ShapeIndex.projectPos(this.max, dir);

            Quaternion rot = new Quaternion().fromAngles(0.0F, 0.0F, 0.01745329F * dirDelta * 90.0F);

            pMin = rot.mult(pMin);
            pMax = rot.mult(pMax);

            int newDir = Direction.rotate(dir, dirDelta);

            Vector2f v1 = ShapeIndex.unprojectPos(pMin, newDir);
            Vector2f v2 = ShapeIndex.unprojectPos(pMax, newDir);

            return ShapeIndex.getRect(Math.min(v1.x, v2.x), Math.min(v1.y, v2.y), Math.max(v1.x, v2.x), Math.max(v1.y, v2.y));
        }

        public boolean isMatchingFace(BoundaryShape shape) {
            return shape == this;
        }

        public int hashCode() {
            return getClass().hashCode() ^ this.min.hashCode() ^ this.max.hashCode();
        }

        public boolean equals(Object o) {
            if (o == this)
                return true;
            if (o == null)
                return false;
            if (o.getClass() != getClass()) {
                return false;
            }
            Rect other = (Rect) o;

            if (!ObjectUtils.areEqual(other.min, this.min))
                return false;
            if (!ObjectUtils.areEqual(other.max, this.max))
                return false;
            return true;
        }

        public String toString() {
            return "Rect[" + this.min + ", " + this.max + "]";
        }
    }

    private static class NullShape
            implements BoundaryShape {
        private NullShape() {
        }

        public boolean isMatchingFace(BoundaryShape shape) {
            return false;
        }

        public BoundaryShape rotate(int dir, int dirDelta) {
            return this;
        }

        public float getArea() {
            return 0.0F;
        }

        public String toString() {
            return "NULL_SHAPE";
        }
    }
}