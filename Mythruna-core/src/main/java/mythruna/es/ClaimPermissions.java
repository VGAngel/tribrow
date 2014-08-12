package mythruna.es;

import com.jme3.network.serializing.Serializable;
import org.progeeks.util.ObjectUtils;

@Serializable
public class ClaimPermissions implements EntityComponent, PersistentComponent {

    public static final int LOCAL_OVERRIDE = 1;
    public static final int BLOCK_REMOVE = 2;
    public static final int BLOCK_ADD = 4;
    public static final int OBJ_ADD = 16;
    public static final int OBJ_REMOVE = 32;
    public static final int OBJ_MOVE = 64;
    public static final int OBJ_CHANGE = 128;
    public static final int PROP_ADD = 256;
    public static final int PROP_REMOVE = 512;
    public static final int PROP_MOVE = 1024;
    public static final int PROP_CHANGE = 2048;
    private int perms;
    private EntityId claimId;

    public ClaimPermissions() {
    }

    public ClaimPermissions(EntityId claimId, int[] permList) {
        setPerms(permList);
        this.claimId = claimId;
    }

    public static ClaimPermissions createNoAccess(EntityId claimId) {
        return new ClaimPermissions(claimId, new int[0]);
    }

    public static ClaimPermissions createOwnerPermissions(EntityId claimId) {
        return new ClaimPermissions(claimId, new int[]{1, 4, 2, 16, 32, 64, 128, 256, 512, 1024, 2048});
    }

    public boolean canDoAll(int[] permList) {
        int result = 0;
        for (int i : permList)
            result |= i & this.perms;
        return result != 0;
    }

    public boolean canDo(int perm) {
        return (this.perms & perm) != 0;
    }

    public boolean canAddBlock() {
        return (this.perms & 0x4) != 0;
    }

    public boolean canRemoveBlock() {
        return (this.perms & 0x2) != 0;
    }

    public boolean canAddObject() {
        return (this.perms & 0x10) != 0;
    }

    public boolean canRemoveObject() {
        return (this.perms & 0x20) != 0;
    }

    public boolean canMoveObject() {
        return (this.perms & 0x40) != 0;
    }

    public boolean canChangeObject() {
        return (this.perms & 0x80) != 0;
    }

    protected void setPerms(int[] permList) {
        this.perms = 0;
        for (int i : permList) {
            this.perms |= i;
        }
    }

    public int getPerms() {
        return this.perms;
    }

    public EntityId getClaimId() {
        return this.claimId;
    }

    public int hashCode() {
        if (this.claimId == null)
            return this.perms;
        return this.perms ^ this.claimId.hashCode();
    }

    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (o == null)
            return false;
        if (o.getClass() != getClass()) {
            return false;
        }
        ClaimPermissions other = (ClaimPermissions) o;
        if (!ObjectUtils.areEqual(this.claimId, other.claimId))
            return false;
        if (this.perms != other.perms)
            return false;
        return true;
    }

    public Class<ClaimPermissions> getType() {
        return ClaimPermissions.class;
    }

    public String toString() {
        return "ClaimPermissions[" + this.claimId + ", " + Integer.toHexString(this.perms) + "]";
    }
}