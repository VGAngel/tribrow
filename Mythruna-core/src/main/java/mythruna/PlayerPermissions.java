package mythruna;

import com.jme3.math.Vector3f;
import mythruna.es.*;

public interface PlayerPermissions {

    public abstract ClaimPermissionChain getPermissions(Vector3i vector3i);

    public abstract ClaimPermissionChain getPermissions(Entity entity);

    public abstract Entity getContainingProperty(Vector3i vector3i);

    public abstract boolean canChangeBlock(Vector3i vector3i);

    public abstract boolean canAddObject(Vector3f vector3f);

    public abstract boolean canMoveObject(Vector3f vector3f, EntityId entityid);

    public abstract boolean canRemoveObject(Vector3f vector3f, EntityId entityid);

    public abstract boolean canChangeObject(Vector3f vector3f, EntityId entityid);

    public abstract boolean canPlaceClaim(Vector3i vector3i, ClaimArea claimarea, EntityId entityid);

    public abstract boolean canMoveClaim(ClaimArea claimarea, EntityId entityid);

    public abstract boolean canMoveClaim(Position position, EntityId entityid);

    public abstract boolean canChangeClaim(EntityId entityid);

    public abstract boolean canRemoveClaim(EntityId entityid);

    public abstract boolean intersectsSiblings(EntityId entityid, ClaimArea claimarea, EntityId entityid1);

    public abstract boolean isOwner(EntityId entityid);

    public abstract boolean hasSubPlots(EntityId entityid);

    public abstract void release();
}
