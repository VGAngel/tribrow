package mythruna.es;

import com.jme3.network.serializing.Serializable;

@Serializable
public class InContainer implements EntityComponent, PersistentComponent {

    private EntityId parentId;
    private byte slot;

    public InContainer() {
        this(EntityId.NULL_ID, (byte) -1);
    }

    public InContainer(EntityId parentId, byte slot) {
        this.parentId = parentId;
        this.slot = slot;
    }

    public InContainer(EntityId parentId, int slot) {
        this(parentId, (byte) slot);
    }

    public Class<InContainer> getType() {
        return InContainer.class;
    }

    public EntityId getParentId() {
        return this.parentId;
    }

    public byte getSlot() {
        return this.slot;
    }

    public String toString() {
        return "InContainer[" + this.parentId + ":" + this.slot + "]";
    }
}