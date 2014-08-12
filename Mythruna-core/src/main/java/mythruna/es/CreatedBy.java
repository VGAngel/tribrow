package mythruna.es;

import com.jme3.network.serializing.Serializable;

@Serializable
public class CreatedBy implements EntityComponent, PersistentComponent {

    private EntityId creatorId;

    public CreatedBy() {
        this(EntityId.NULL_ID);
    }

    public CreatedBy(EntityId creatorId) {
        this.creatorId = creatorId;
    }

    public Class<CreatedBy> getType() {
        return CreatedBy.class;
    }

    public EntityId getCreatorId() {
        return this.creatorId;
    }

    public String toString() {
        return "CreatedBy[" + this.creatorId + "]";
    }
}