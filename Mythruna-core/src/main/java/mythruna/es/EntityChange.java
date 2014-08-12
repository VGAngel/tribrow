package mythruna.es;

public class EntityChange {

    private EntityId entityId;
    private EntityComponent component;
    private Class type;

    public EntityChange(EntityId entityId, Class type, EntityComponent component) {
        this.entityId = entityId;
        this.type = (component == null ? type : component.getType());
        this.component = component;
    }

    public EntityChange(EntityId entityId, EntityComponent component) {
        this(entityId, null, component);
    }

    public EntityChange(EntityId entityId, Class type) {
        this(entityId, type, null);
    }

    public EntityId getEntityId() {
        return this.entityId;
    }

    public Class getComponentType() {
        return this.type;
    }

    public EntityComponent getComponent() {
        return this.component;
    }

    public String toString() {
        return "EntityChange[" + this.entityId + ", " + this.component + ", " + this.type + "]";
    }
}