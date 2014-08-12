package mythruna.es;

import com.jme3.math.Vector3f;
import com.jme3.network.serializing.Serializable;
import mythruna.Vector3i;
import org.progeeks.util.ObjectUtils;

@Serializable
public class ClaimArea implements EntityComponent, PersistentComponent {

    private Vector3i min;
    private Vector3i max;
    private byte depth;
    private byte height;

    public ClaimArea() {
    }

    public ClaimArea(Vector3i min, Vector3i max, byte depth, byte height) {
        this.min = min;
        this.max = max;
        this.depth = depth;
        this.height = height;
    }

    public ClaimArea(Vector3i min, Vector3i max, ClaimArea original) {
        this.min = min;
        this.max = max;
        this.depth = original.depth;
        this.height = original.height;
    }

    public static ClaimArea createStandard(Vector3i center, ClaimType type) {
        Vector3i rMin = new Vector3i(center);
        Vector3i rMax = new Vector3i(center);
        byte rDepth = 0;
        byte rHeight = 0;

        switch (type.getClaimType()) {
            case 1:
                rMin.x -= 16;
                rMin.y -= 16;
                rMin.z = center.z;
                rMax.x += 15;
                rMax.y += 15;
                rMax.z = center.z;
                rDepth = -1;
                rHeight = -1;
                break;
            case 2:
                rMin.x -= 64;
                rMin.y -= 64;
                rMin.z = center.z;
                rMax.x += 63;
                rMax.y += 63;
                rMax.z = center.z;
                rDepth = -1;
                rHeight = -1;
                break;
            case 3:
                rMin.x -= 128;
                rMin.y -= 128;
                rMin.z = center.z;
                rMax.x += 127;
                rMax.y += 127;
                rMax.z = center.z;
                rDepth = -1;
                rHeight = -1;
                break;
            case 4:
                rMin.z = center.z;
                rMax.x += 1;
                rMax.y += 1;
                rMax.z = center.z;
                rDepth = 5;
                rHeight = 10;
                break;
            case 5:
                rMin.z = center.z;
                rMax.x += 1;
                rMax.y += 1;
                rMax.z = center.z;
                rDepth = 5;
                rHeight = 15;
                break;
            default:
                throw new RuntimeException("Unknown standard type:" + type);
        }
        return new ClaimArea(rMin, rMax, rDepth, rHeight);
    }

    public int hashCode() {
        return this.depth + this.height + this.min.hashCode() + this.max.hashCode();
    }

    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (o == null)
            return false;
        if (o.getClass() != getClass()) {
            return false;
        }
        ClaimArea other = (ClaimArea) o;
        if (!ObjectUtils.areEqual(this.min, other.min))
            return false;
        if (!ObjectUtils.areEqual(this.max, other.max))
            return false;
        if (this.depth != other.depth)
            return false;
        if (this.height != other.height)
            return false;
        return true;
    }

    public Class<ClaimArea> getType() {
        return ClaimArea.class;
    }

    public boolean contains(Vector3i loc) {
        if ((loc.x < this.min.x) || (loc.y < this.min.y))
            return false;
        if ((loc.x > this.max.x) || (loc.y > this.max.y)) {
            return false;
        }

        if (((getDepth() != -1) && (loc.z < this.min.z - getDepth())) || ((getHeight() != -1) && (loc.z > this.min.z + getHeight()))) {
            return false;
        }
        return true;
    }

    public boolean contains(Vector3i loc, int border) {
        if ((loc.x < this.min.x + border) || (loc.y < this.min.y + border))
            return false;
        if ((loc.x > this.max.x - border) || (loc.y > this.max.y - border)) {
            return false;
        }

        if (((getDepth() != -1) && (loc.z < this.min.z - getDepth())) || ((getHeight() != -1) && (loc.z > this.min.z + getHeight()))) {
            return false;
        }
        return true;
    }

    public boolean contains(Vector3f loc, float border) {
        if ((loc.x < this.min.x + border) || (loc.y < this.min.y + border))
            return false;
        if ((loc.x > this.max.x - border) || (loc.y > this.max.y - border))
            return false;
        return true;
    }

    public boolean contains(ClaimArea area) {
        if ((area.min.x < this.min.x) || (area.min.y < this.min.y))
            return false;
        if ((area.max.x > this.max.x) || (area.max.y > this.max.y))
            return false;
        return true;
    }

    public void trimTo(ClaimArea area) {
        if ((this.max.x >= area.min.x) && (this.min.x < area.min.x)) {
            this.max.x = (area.min.x - 1);
        }
        if ((this.max.y >= area.min.y) && (this.min.y < area.min.y)) {
            this.max.y = (area.min.y - 1);
        }
        if ((this.min.x <= area.max.x) && (this.max.x > area.max.x)) {
            this.min.x = (area.max.x + 1);
        }
        if ((this.min.y <= area.max.y) && (this.max.y > area.max.y)) {
            this.min.y = (area.max.y + 1);
        }
    }

    public boolean intersects(ClaimArea area) {
        if ((area.max.x < this.min.x) || (area.max.y < this.min.y))
            return false;
        if ((area.min.x > this.max.x) || (area.min.y > this.max.y)) {
            return false;
        }
        return true;
    }

    public boolean containsVertically(int z) {
        if ((getDepth() != -1) && (z < this.min.z - getDepth()))
            return false;
        if ((getHeight() != -1) && (z > this.min.z + getHeight()))
            return false;
        return true;
    }

    public Vector3i getMin() {
        return this.min;
    }

    public Vector3i getMax() {
        return this.max;
    }

    public int getDepth() {
        return this.depth & 0xFF;
    }

    public int getHeight() {
        return this.height & 0xFF;
    }

    public int getDeltaX() {
        return this.max.x - this.min.x + 1;
    }

    public int getDeltaY() {
        return this.max.y - this.min.y + 1;
    }

    public int getAreaSize() {
        int x = getDeltaX();
        int y = getDeltaY();
        return x * y;
    }

    public String toString() {
        return "ClaimArea[" + this.min + ", " + this.max + ", " + this.depth + ", " + this.height + "]";
    }
}