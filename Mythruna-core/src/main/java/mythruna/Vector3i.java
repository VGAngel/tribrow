package mythruna;

import com.jme3.math.Vector3f;
import com.jme3.network.serializing.Serializable;

@Serializable
public class Vector3i implements Cloneable {

    public int x;
    public int y;
    public int z;

    public Vector3i() {
    }

    public Vector3i(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3i(Vector3f v) {
        this.x = (int) v.x;
        this.y = (int) v.y;
        this.z = (int) v.z;
    }

    public Vector3i(Vector3i v) {
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
    }

    public Vector3f toVector3f() {
        return new Vector3f(this.x, this.y, this.z);
    }

    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (o == null)
            return false;
        if (o.getClass() != getClass())
            return false;
        Vector3i other = (Vector3i) o;
        return (this.x == other.x) && (this.y == other.y) && (this.z == other.z);
    }

    public int hashCode() {
        return this.x ^ this.y ^ this.z;
    }

    public Vector3i clone() {
        try {
            return (Vector3i) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Error cloning", e);
        }
    }

    public final void set(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public final void set(Vector3i val) {
        this.x = val.x;
        this.y = val.y;
        this.z = val.z;
    }

    public final int get(int index) {
        switch (index) {
            case 0:
                return this.x;
            case 1:
                return this.y;
            case 2:
                return this.z;
        }
        throw new IndexOutOfBoundsException(String.valueOf(index));
    }

    public final void set(int index, int value) {
        switch (index) {
            case 0:
                this.x = value;
                break;
            case 1:
                this.y = value;
                break;
            case 2:
                this.z = value;
                break;
            default:
                throw new IndexOutOfBoundsException(String.valueOf(index));
        }
    }

    public final void addLocal(Vector3i v) {
        this.x += v.x;
        this.y += v.y;
        this.z += v.z;
    }

    public final Vector3i add(int i, int j, int k) {
        return new Vector3i(this.x + i, this.y + j, this.z + k);
    }

    public final Vector3i subtract(Vector3i v) {
        return new Vector3i(this.x - v.x, this.y - v.y, this.z - v.z);
    }

    public final Vector3i mult(int scale) {
        return new Vector3i(this.x * scale, this.y * scale, this.z * scale);
    }

    public void minLocal(int i, int j, int k) {
        this.x = Math.min(this.x, i);
        this.y = Math.min(this.y, j);
        this.z = Math.min(this.z, k);
    }

    public void maxLocal(int i, int j, int k) {
        this.x = Math.max(this.x, i);
        this.y = Math.max(this.y, j);
        this.z = Math.max(this.z, k);
    }

    public void minLocal(Vector3i v) {
        this.x = Math.min(v.x, this.x);
        this.y = Math.min(v.y, this.y);
        this.z = Math.min(v.z, this.z);
    }

    public void maxLocal(Vector3i v) {
        this.x = Math.max(v.x, this.x);
        this.y = Math.max(v.y, this.y);
        this.z = Math.max(v.z, this.z);
    }

    public String toString() {
        return "Vector3i[" + this.x + ", " + this.y + ", " + this.z + "]";
    }
}