package mythruna.msg;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import mythruna.es.EntityChange;
import mythruna.es.EntityComponent;
import mythruna.es.EntityId;

@Serializable
public class ComponentChangeMessage extends AbstractMessage {

    private EntityId entityId;
    private Class type;
    private EntityComponent component;

    public ComponentChangeMessage() {
    }

    public ComponentChangeMessage(EntityChange change) {
        this(change.getEntityId(), change.getComponentType(), change.getComponent());
    }

    public ComponentChangeMessage(EntityId entityId, Class type, EntityComponent component) {
        this.entityId = entityId;
        this.type = (component == null ? type : null);
        this.component = component;
    }

    public EntityId getEntityId() {
        return this.entityId;
    }

    public Class getType() {
        if (this.component != null)
            return this.component.getClass();
        return this.type;
    }

    public EntityComponent getComponent() {
        return this.component;
    }

    public String toString() {
        return "ComponentChangeMessage[" + this.entityId + ", " + this.type + ", " + this.component + "]";
    }
}