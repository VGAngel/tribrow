package mythruna.db.cave;

import com.jme3.math.FastMath;
import com.jme3.math.LineSegment;
import com.jme3.math.Vector3f;

import java.io.Serializable;

public class LineInfluencer implements Influencer, Serializable {

    static final long serialVersionUID = 42L;
    private Vector3f min;
    private Vector3f max;
    private LineSegment line;
    private float radius;
    private float radiusSq;
    private float strength;
    private boolean wet = false;

    public LineInfluencer(Vector3f start, Vector3f end, float radius, float strength) {
        this.min = new Vector3f(Math.min(start.x, end.x), Math.min(start.y, end.y), Math.min(start.z, end.z));
        this.max = new Vector3f(Math.max(start.x, end.x), Math.max(start.y, end.y), Math.max(start.z, end.z));
        this.line = new LineSegment(start, end);
        this.radius = radius;
        this.radiusSq = (radius * radius);
        this.strength = strength;
    }

    public void setWet(boolean wet) {
        this.wet = wet;
    }

    public boolean isWet() {
        return this.wet;
    }

    public Vector3f getCenter() {
        return this.line.getOrigin();
    }

    public Vector3f getMin() {
        return this.min.subtract(this.radius, this.radius, this.radius);
    }

    public Vector3f getMax() {
        return this.max.add(this.radius, this.radius, this.radius);
    }

    public LineSegment getLine() {
        return this.line;
    }

    public Vector3f getStart() {
        return this.line.getNegativeEnd(new Vector3f());
    }

    public Vector3f getEnd() {
        return this.line.getPositiveEnd(new Vector3f());
    }

    public LineSegment getLine2D() {
        Vector3f start = this.line.getNegativeEnd(new Vector3f());
        Vector3f end = this.line.getPositiveEnd(new Vector3f());

        start.z = 0.0F;
        end.z = 0.0F;

        return new LineSegment(start, end);
    }

    public float getDistanceSq(Vector3f pos) {
        return this.line.distanceSquared(pos);
    }

    public LineInfluencer setRadius(float radius) {
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

    public boolean intersects(LineInfluencer in) {
        float range = getRadius() + in.getRadius();
        range *= range;

        return this.line.distanceSquared(in.line) <= range;
    }

    public LineInfluencer setStrength(float s) {
        this.strength = s;
        return this;
    }

    public float getStrength() {
        return this.strength;
    }

    public float getStrength(Vector3f pos) {
        float distSq = this.line.distanceSquared(pos);
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
        return "Line[" + getStart() + ", " + getEnd() + ", " + this.radius + ", " + this.strength + "]";
    }
}