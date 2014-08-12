package mythruna.db.cave;

import com.jme3.math.FastMath;
import com.jme3.math.LineSegment;
import com.jme3.math.Vector3f;

import java.io.Serializable;

public class WallInfluencer implements Influencer, Serializable {

    static final long serialVersionUID = 42L;
    private Vector3f min;
    private Vector3f max;
    private float height;
    private LineSegment line;
    private Vector3f direction;
    private Vector3f origin;
    private Vector3f dir2D;
    private float extent;
    private float extent2D;
    private float radius;
    private float radiusSq;
    private float strength;
    private boolean wet = false;

    public WallInfluencer(Vector3f start, Vector3f end, float height, float radius, float strength) {
        this.min = new Vector3f(Math.min(start.x, end.x), Math.min(start.y, end.y), Math.min(start.z, end.z));
        this.max = new Vector3f(Math.max(start.x, end.x), Math.max(start.y, end.y), Math.max(start.z, end.z));
        this.height = height;
        this.line = new LineSegment(start, end);
        this.origin = this.line.getOrigin();
        this.direction = this.line.getDirection();
        this.radius = radius;
        this.radiusSq = (radius * radius);
        this.strength = strength;

        float x = end.x - this.origin.x;
        float y = end.y - this.origin.y;
        this.dir2D = new Vector3f(end.x - start.x, end.y - start.y, 0.0F);
        this.dir2D.normalizeLocal();
        this.extent2D = (this.dir2D.x * x + this.dir2D.y * y);
        this.extent = this.line.getExtent();
    }

    public void setWet(boolean wet) {
        this.wet = wet;
    }

    public boolean isWet() {
        return this.wet;
    }

    public Vector3f getCenter() {
        return this.origin;
    }

    public Vector3f getMin() {
        return this.min.subtract(this.radius, this.radius, this.radius);
    }

    public Vector3f getMax() {
        return this.max.add(this.radius, this.radius, this.radius + this.height);
    }

    public Vector3f getStart() {
        return this.line.getNegativeEnd(new Vector3f());
    }

    public Vector3f getEnd() {
        return this.line.getPositiveEnd(new Vector3f());
    }

    public LineSegment getLine() {
        return this.line;
    }

    public LineSegment getLine2D() {
        Vector3f start = this.line.getNegativeEnd(new Vector3f());
        Vector3f end = this.line.getPositiveEnd(new Vector3f());

        start.z = 0.0F;
        end.z = 0.0F;

        return new LineSegment(start, end);
    }

    public WallInfluencer setHeight(float height) {
        this.height = height;
        return this;
    }

    public float getHeight() {
        return this.height;
    }

    public WallInfluencer setRadius(float radius) {
        this.radius = radius;
        this.radiusSq = (radius * radius);
        return this;
    }

    public float getRadius() {
        return this.radius;
    }

    public float getRadiusSq() {
        return this.radiusSq;
    }

    public float getDistanceSq(Vector3f pos) {
        Vector3f p = nearestPoint(pos);
        if (pos.z > p.z) {
            pos = pos.clone();

            if (pos.z > p.z + this.height)
                pos.z -= this.height;
            else {
                pos.z = p.z;
            }
        }
        return p.distanceSquared(pos);
    }

    protected Vector3f nearestPoint(Vector3f point) {
        Vector3f result = new Vector3f();
        point.subtract(this.origin, result);

        float dot = this.dir2D.x * result.x + this.dir2D.y * result.y;

        dot = dot * this.extent / this.extent2D;

        if (dot > -this.extent) {
            if (dot < this.extent) {
                this.origin.add(this.direction.mult(dot, result), result);
            } else {
                this.origin.add(this.direction.mult(this.extent, result), result);
            }
        } else {
            this.origin.subtract(this.direction.mult(this.extent, result), result);
        }

        return result;
    }

    public WallInfluencer setStrength(float s) {
        this.strength = s;
        return this;
    }

    public float getStrength() {
        return this.strength;
    }

    public float getStrength(Vector3f pos) {
        Vector3f p = nearestPoint(pos);
        if (pos.z > p.z) {
            pos = pos.clone();

            if (pos.z > p.z + this.height)
                pos.z -= this.height;
            else {
                pos.z = p.z;
            }
        }
        float distSq = p.distanceSquared(pos);
        if (distSq >= this.radiusSq) {
            return 0.0F;
        }
        float dist = FastMath.sqrt(distSq);

        float invRatio = 1.0F - dist / this.radius;
        return this.strength * invRatio * invRatio;
    }

    public boolean canInfluence(float x1, float y1, float x2, float y2) {
        if ((x2 < this.min.x - this.radius) || (y2 < this.min.y - this.radius))
            return false;
        if ((x1 > this.max.x + this.radius) || (y1 > this.max.y + this.radius)) {
            return false;
        }

        return true;
    }

    public String toString() {
        return "Line[" + getStart() + ", " + getEnd() + ", " + this.height + ", " + this.radius + ", " + this.strength + "]";
    }
}