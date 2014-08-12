package mythruna.db.cave;

import com.jme3.math.Vector3f;

import java.io.Serializable;

public class PointInfluencer implements Influencer, Serializable {

    static final long serialVersionUID = 42L;
    private Vector3f point;
    private float radius;
    private float radiusSq;
    private float strength;
    private boolean wet = false;

    public PointInfluencer(float x, float y, float z, float radius, float strength) {
        this(new Vector3f(x, y, z), radius, strength);
    }

    public PointInfluencer(double x, double y, double z, double radius, double strength) {
        this((float) x, (float) y, (float) z, (float) radius, (float) strength);
    }

    public PointInfluencer(Vector3f pos, float radius, float strength) {
        this.point = pos;
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
        return this.point;
    }

    public Vector3f getMin() {
        return this.point.subtract(this.radius, this.radius, this.radius);
    }

    public Vector3f getMax() {
        return this.point.add(this.radius, this.radius, this.radius);
    }

    public PointInfluencer setRadius(float radius) {
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

    public PointInfluencer setStrength(float s) {
        this.strength = s;
        return this;
    }

    public float getStrength() {
        return this.strength;
    }

    public float getStrength(Vector3f pos) {
        float x = pos.x - this.point.x;
        float y = pos.y - this.point.y;
        float z = pos.z - this.point.z;

        float distSq = x * x + y * y + z * z;
        if (distSq >= this.radiusSq) {
            return 0.0F;
        }
        float invRatio = 1.0F - distSq / this.radiusSq;
        return this.strength * invRatio * invRatio;
    }

    public boolean canInfluence(float x1, float y1, float x2, float y2) {
        if ((x2 < this.point.x - this.radius) || (y2 < this.point.y - this.radius)) {
            return false;
        }
        if ((x1 > this.point.x + this.radius) || (y1 > this.point.y + this.radius)) {
            return false;
        }
        return true;
    }

    public String toString() {
        return "Point[" + this.point + ", " + this.radius + ", " + this.strength + "]";
    }
}