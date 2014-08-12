package mythruna;

import com.jme3.math.Vector3f;
import mythruna.es.*;
import org.progeeks.util.ObjectUtils;
import org.progeeks.util.log.Log;

public class DefaultPlayerPermissions implements PlayerPermissions {

    static Log log = Log.getLog();
    private EntityId player;
    private EntityData ed;
    private Entity lastWorldZone = null;
    private EntitySet worldZones;
    private Entity lastZone = null;
    private ClaimPermissionChain lastChain = null;

    private EntityId lastParent = null;
    private EntitySet lastChildren = null;

    private EntitySet badges = null;

    public DefaultPlayerPermissions(EntityId player, EntityData ed) {
        this.player = player;
        this.ed = ed;
        this.worldZones = ed.getEntities(new FieldFilter(ClaimType.class, "parent", null), new Class[]{ClaimType.class, ClaimArea.class, OwnedBy.class});
        this.badges = ed.getEntities(new FieldFilter(InContainer.class, "parentId", player), new Class[]{InContainer.class, ClaimPermissions.class});
    }

    public void release() {
        this.worldZones.release();
        this.badges.release();
        if (this.lastChildren != null)
            this.lastChildren.release();
    }

    protected void setLastWorldZone(Entity z) {
        if (this.lastWorldZone == z)
            return;
        this.lastWorldZone = z;
    }

    public ClaimPermissionChain getPermissions(Vector3i location) {
        Entity e = getContainingZone(location);
        if (e == null) {
            return new ClaimPermissionChain();
        }
        if ((this.lastChain != null) && (this.lastChain.isClaim(e.getId()))) {
            return this.lastChain;
        }
        this.badges.applyChanges();
        if (this.lastWorldZone == e)
            this.lastChain = new ClaimPermissionChain(this.player, this.badges, new Entity[]{e});
        else {
            this.lastChain = new ClaimPermissionChain(this.player, this.badges, new Entity[]{e, this.lastWorldZone});
        }
        return this.lastChain;
    }

    public ClaimPermissionChain getPermissions(Entity claim) {
        if ((this.lastChain != null) && (this.lastChain.isClaim(claim.getId()))) {
            return this.lastChain;
        }
        this.badges.applyChanges();

        ClaimType type = (ClaimType) claim.get(ClaimType.class);
        if (type.getParent() == null) {
            return new ClaimPermissionChain(this.player, this.badges, new Entity[]{claim});
        }

        Entity parent = getWorldZone(type.getParent());
        if (parent == null) {
            return new ClaimPermissionChain();
        }
        return new ClaimPermissionChain(this.player, this.badges, new Entity[]{claim, parent});
    }

    public Entity getContainingProperty(Vector3i location) {
        return getContainingZone(location);
    }

    protected Entity findInArea(EntitySet zones, Vector3i location) {
        for (Entity e : zones) {
            ClaimArea area = (ClaimArea) e.get(ClaimArea.class);

            if (area.contains(location))
                return e;
        }
        return null;
    }

    protected Entity getWorldZone(EntityId id) {
        this.worldZones.applyChanges();
        for (Entity e : this.worldZones) {
            if (ObjectUtils.areEqual(e.getId(), id))
                return e;
        }
        return null;
    }

    protected Entity getWorldZone(Vector3i location) {
        this.worldZones.applyChanges();

        ClaimArea area = null;
        if (this.lastWorldZone != null) {
            area = (ClaimArea) this.lastWorldZone.get(ClaimArea.class);
        }

        if (area != null) {
            if (area.contains(location)) {
                return this.lastWorldZone;
            }
        }
        for (Entity e : this.worldZones) {
            area = (ClaimArea) e.get(ClaimArea.class);

            if (area.contains(location)) {
                setLastWorldZone(e);
                return e;
            }
        }

        return null;
    }

    protected Entity getContainingZone(Vector3i location) {
        this.worldZones.applyChanges();

        ClaimArea area = null;
        if (this.lastZone != null) {
            area = (ClaimArea) this.lastZone.get(ClaimArea.class);
        }

        if (area != null) {
            if (area.contains(location)) {
                return this.lastZone;
            }
        }
        Entity w = getWorldZone(location);

        if (w != null) {
            ClaimType type = (ClaimType) w.get(ClaimType.class);
            if (type.canBeParent()) {
                EntitySet children = getChildren(w.getId());

                Entity sub = findInArea(children, location);
                if (sub != null) {
                    w = sub;
                }
            }

        }

        return w;
    }

    protected EntitySet getChildren(EntityId parent) {
        if ((parent.equals(this.lastParent)) && (this.lastChildren != null)) {
            this.lastChildren.applyChanges();
            return this.lastChildren;
        }

        this.lastParent = parent;

        if (this.lastChildren != null) {
            this.lastChildren.release();
        }

        this.lastChildren = this.ed.getEntities(new FieldFilter(ClaimType.class, "parent", parent), new Class[]{ClaimType.class, ClaimArea.class, Position.class, OwnedBy.class});

        return this.lastChildren;
    }

    protected boolean checkBlockEdit(Entity zone, Vector3i block) {
        OwnedBy owner = (OwnedBy) this.ed.getComponent(zone.getId(), OwnedBy.class);
        ClaimType type = (ClaimType) zone.get(ClaimType.class);

        if (type.getParent() != null) {
            OwnedBy parentOwner = (OwnedBy) this.ed.getComponent(type.getParent(), OwnedBy.class);

            if (this.player.equals(parentOwner.getOwnerId())) {
                return true;
            }

            if (!this.player.equals(owner.getOwnerId())) {
                return false;
            }

            ClaimArea area = (ClaimArea) zone.get(ClaimArea.class);

            return area.containsVertically(block.z);
        }

        if (this.player.equals(owner.getOwnerId())) {
            return true;
        }
        return false;
    }

    public boolean canChangeBlock(Vector3i location) {
        ClaimPermissionChain chain = getPermissions(location);

        return chain.canDo(4);
    }

    public boolean canChangeClaim(EntityId claim) {
        if (!isOwner(claim))
            return false;
        return true;
    }

    public boolean canRemoveClaim(EntityId claim) {
        if (!isOwner(claim))
            return false;
        return true;
    }

    public boolean canPlaceClaim(Vector3i location, ClaimArea area, EntityId claim) {
        Entity zone = getContainingZone(location);

        if (zone == null) {
            return true;
        }

        ClaimType type = (ClaimType) this.ed.getComponent(claim, ClaimType.class);

        if (zone.getId().equals(type.getParent())) {
            return true;
        }

        return false;
    }

    public boolean hasSubPlots(EntityId claim) {
        this.worldZones.applyChanges();

        if (!this.worldZones.containsId(claim)) {
            return false;
        }
        return !getChildren(claim).isEmpty();
    }

    public ClaimArea trim(EntityId parent, Vector3i block, ClaimArea area, EntityId child) {
        if (parent != null) {
            log.warn("trim() not supported on non-world level claims.");
            return null;
        }

        this.worldZones.applyChanges();
        EntitySet children = this.worldZones;
        for (Entity e : children) {
            if (!e.getId().equals(child)) {
                ClaimArea a = (ClaimArea) e.get(ClaimArea.class);
                if (a.contains(block)) {
                    return null;
                }

                if (a.intersects(area)) {
                    area.trimTo(a);
                }
            }
        }

        return area;
    }

    public boolean intersectsSiblings(EntityId parent, ClaimArea area, EntityId child) {
        if (parent == null) {
            return intersects(this.worldZones, area, child);
        }
        EntitySet children = getChildren(parent);
        boolean intersects = intersects(children, area, child);

        return intersects;
    }

    protected boolean intersects(EntitySet siblings, ClaimArea area, EntityId self) {
        siblings.applyChanges();

        for (Entity e : siblings) {
            if (!e.getId().equals(self)) {
                ClaimArea a = (ClaimArea) e.get(ClaimArea.class);
                if (area.intersects(a)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean checkWorld(ClaimArea area, ClaimType type, OwnedBy owner, EntityId claim) {
        if (!owner.getOwnerId().equals(this.player)) {
            return false;
        }

        if (intersects(this.worldZones, area, claim))
            return false;
        return true;
    }

    protected boolean checkSub(ClaimArea area, ClaimType type, OwnedBy owner, EntityId claim) {
        OwnedBy parentOwner = (OwnedBy) this.ed.getComponent(type.getParent(), OwnedBy.class);
        if ((!parentOwner.getOwnerId().equals(this.player)) && (!owner.getOwnerId().equals(this.player))) {
            return false;
        }

        ClaimArea parentArea = (ClaimArea) this.ed.getComponent(type.getParent(), ClaimArea.class);
        if (!parentArea.contains(area)) {
            return false;
        }

        return !intersectsSiblings(type.getParent(), area, claim);
    }

    public boolean canMoveClaim(ClaimArea area, EntityId claim) {
        ClaimType type = (ClaimType) this.ed.getComponent(claim, ClaimType.class);
        OwnedBy owner = (OwnedBy) this.ed.getComponent(claim, OwnedBy.class);

        if (type.getParent() == null) {
            return checkWorld(area, type, owner, claim);
        }

        return checkSub(area, type, owner, claim);
    }

    public boolean isOwner(EntityId claim) {
        ClaimType type = (ClaimType) this.ed.getComponent(claim, ClaimType.class);
        OwnedBy owner = (OwnedBy) this.ed.getComponent(claim, OwnedBy.class);

        if (owner.getOwnerId().equals(this.player)) {
            return true;
        }
        if (type.getParent() == null) {
            return false;
        }
        OwnedBy parentOwner = (OwnedBy) this.ed.getComponent(type.getParent(), OwnedBy.class);
        if (parentOwner.getOwnerId().equals(this.player)) {
            return true;
        }
        return false;
    }

    public boolean isCreator(EntityId obj) {
        CreatedBy creator = (CreatedBy) this.ed.getComponent(obj, CreatedBy.class);
        if ((creator == null) || (creator.getCreatorId().equals(this.player)))
            return true;
        return false;
    }

    public boolean canMoveClaim(Position marker, EntityId claim) {
        if (!isOwner(claim)) {
            return false;
        }

        return true;
    }

    public boolean canAddObject(Vector3f location) {
        ClaimPermissionChain chain = getPermissions(Coordinates.worldToCell(location));
        return chain.canDo(16);
    }

    public boolean canMoveObject(Vector3f location, EntityId obj) {
        ClaimPermissionChain chain = getPermissions(Coordinates.worldToCell(location));

        boolean creator = isCreator(obj);
        if ((creator) && (chain.canDo(64))) {
            return true;
        }

        if (creator) {
            return false;
        }

        Position pos = (Position) this.ed.getComponent(obj, Position.class);
        chain = getPermissions(Coordinates.worldToCell(pos.getLocation()));

        return chain.canDo(64);
    }

    public boolean canRemoveObject(Vector3f location, EntityId obj) {
        if (isCreator(obj)) {
            return true;
        }

        ClaimPermissionChain chain = getPermissions(Coordinates.worldToCell(location));
        return chain.canDo(32);
    }

    public boolean canChangeObject(Vector3f location, EntityId obj) {
        if (isCreator(obj)) {
            return true;
        }

        ClaimPermissionChain chain = getPermissions(Coordinates.worldToCell(location));
        return chain.canDo(32);
    }
}