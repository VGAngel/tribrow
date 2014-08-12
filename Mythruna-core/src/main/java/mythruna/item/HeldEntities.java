package mythruna.item;

import com.jme3.network.serializing.Serializable;
import mythruna.es.EntityComponent;
import mythruna.es.EntityId;

@Serializable
public class HeldEntities implements EntityComponent {

    private EntityId id;
    private EntityId left;
    private EntityId right;

    public HeldEntities() {
    }

    public HeldEntities(EntityId id, EntityId left, EntityId right) {
        this.id = id;
        this.left = left;
        this.right = right;
    }

    public Class<HeldEntities> getType() {
        return HeldEntities.class;
    }

    public HeldEntities addLeft(EntityId add) {
        return new HeldEntities(this.id, add, this.right);
    }

    public HeldEntities addRight(EntityId add) {
        return new HeldEntities(this.id, this.left, add);
    }

    public EntityId getId() {
        return this.id;
    }

    public EntityId getLeft() {
        return this.left;
    }

    public EntityId getRight() {
        return this.right;
    }

    public String toString() {
        return "HeldEntities[" + this.left + ", " + this.right + "]";
    }
}