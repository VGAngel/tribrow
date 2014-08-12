package mythruna.es;

import com.jme3.network.serializing.Serializable;
import org.progeeks.util.ObjectUtils;

@Serializable
public class ClaimType implements EntityComponent, PersistentComponent {

    public static final byte TYPE_WORLD = 0;
    public static final byte TYPE_STRONGHOLD = 1;
    public static final byte TYPE_TOWN = 2;
    public static final byte TYPE_CITY = 3;
    public static final byte TYPE_TOWN_PLOT = 4;
    public static final byte TYPE_CITY_PLOT = 5;
    private byte type;
    private EntityId parent;
    private int maxArea;

    public ClaimType() {
    }

    public ClaimType(byte type, int maxArea, EntityId parent) {
        this.type = type;
        this.parent = parent;
        this.maxArea = maxArea;
    }

    public static ClaimType createStandard(byte type, EntityId parent) {
        switch (type) {
            case 1:
                return new ClaimType(type, 1024, parent);
            case 2:
                return new ClaimType(type, 16384, parent);
            case 3:
                return new ClaimType(type, 65536, parent);
            case 4:
                return new ClaimType(type, 160, parent);
            case 5:
                return new ClaimType(type, 80, parent);
        }
        throw new RuntimeException("Unknown standard type:" + type);
    }

    public int hashCode() {
        int result = this.type + this.maxArea;
        if (this.parent != null)
            result += this.parent.hashCode();
        return result;
    }

    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (o == null)
            return false;
        if (o.getClass() != getClass())
            return false;
        ClaimType other = (ClaimType) o;
        if (other.type != this.type)
            return false;
        if (other.maxArea != this.maxArea)
            return false;
        if (!ObjectUtils.areEqual(this.parent, other.parent))
            return false;
        return true;
    }

    public Class<ClaimType> getType() {
        return ClaimType.class;
    }

    public boolean isChild() {
        return (this.type == 4) || (this.type == 5);
    }

    public boolean canBeParent() {
        return (this.type == 2) || (this.type == 3);
    }

    public byte getChildType() {
        if (this.type == 2)
            return 4;
        if (this.type == 3)
            return 5;
        return 0;
    }

    public EntityId getParent() {
        return this.parent;
    }

    public byte getClaimType() {
        return this.type;
    }

    public int getMaxArea() {
        return this.maxArea;
    }

    public String toString() {
        return "ClaimType[" + this.type + ":" + this.maxArea + " -> " + this.parent + "]";
    }
}