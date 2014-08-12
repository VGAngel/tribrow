package mythruna.es;

import org.progeeks.util.ObjectUtils;

import java.util.Arrays;

public class ClaimPermissionChain {

    private EntityId player;
    private ClaimPermissions[] perms;

    public ClaimPermissionChain(EntityId player, EntitySet badges, Entity[] claims) {
        this.player = player;
        setupPerms(badges, claims);
    }

    public ClaimPermissionChain() {
    }

    public boolean isClaim(EntityId claimId) {
        if ((this.perms == null) && (claimId == null))
            return true;
        if (this.perms == null)
            return false;
        return this.perms[0].getClaimId().equals(claimId);
    }

    public boolean canDo(int perm) {
        if (this.perms == null) {
            return true;
        }

        if (this.perms[0].canDo(perm))
            return true;
        if (this.perms.length < 2)
            return false;
        return this.perms[1].canDo(1);
    }

    protected EntityId getOwnerId(Entity claim) {
        OwnedBy ob = (OwnedBy) claim.get(OwnedBy.class);
        if (ob == null)
            return null;
        return ob.getOwnerId();
    }

    protected ClaimPermissions findPermissions(EntitySet badges, Entity claim) {
        for (Entity e : badges) {
            System.out.println("Checking badge:" + e);
            ClaimPermissions p = (ClaimPermissions) e.get(ClaimPermissions.class);
            System.out.println("Badge claim:" + p.getClaimId() + "  this claim:" + claim.getId());
            if (ObjectUtils.areEqual(p.getClaimId(), claim.getId()))
                return p;
        }
        return ClaimPermissions.createNoAccess(claim.getId());
    }

    protected void setupPerms(EntitySet badges, Entity[] claims) {
        badges.applyChanges();
        System.out.println("Badges:" + badges);
        this.perms = new ClaimPermissions[claims.length];

        for (int i = 0; i < claims.length; i++) {
            System.out.println("Claim:" + claims[i]);
            System.out.println("Claim owner:" + getOwnerId(claims[i]) + "  player:" + this.player);
            if (ObjectUtils.areEqual(getOwnerId(claims[i]), this.player)) {
                System.out.println("Creating owner permissions.");
                this.perms[i] = ClaimPermissions.createOwnerPermissions(claims[i].getId());
            } else {
                System.out.println("Building badge permissions.");

                this.perms[i] = findPermissions(badges, claims[i]);
            }
        }
    }

    public String toString() {
        if (this.perms == null)
            return "ClaimPermissionChain[all]";
        return "ClaimPermissionChain[" + Arrays.asList(this.perms) + "]";
    }
}