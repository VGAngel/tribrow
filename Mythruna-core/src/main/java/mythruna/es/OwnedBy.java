package mythruna.es;

import com.jme3.network.serializing.Serializable;

@Serializable
public class OwnedBy implements EntityComponent, PersistentComponent {

    private EntityId ownerId;

    public OwnedBy() {
        this(EntityId.NULL_ID);
    }

    public OwnedBy(EntityId ownerId) {
        this.ownerId = ownerId;
    }

    public Class<OwnedBy> getType() {
        return OwnedBy.class;
    }

    public EntityId getOwnerId() {
        return this.ownerId;
    }

    public String toString() {
        return "OwnedBy[" + this.ownerId + "]";
    }
}