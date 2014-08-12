package mythruna.script;

import com.jme3.network.serializing.Serializable;
import mythruna.es.Entity;
import mythruna.es.EntityId;

@Serializable
public class EntityParameter implements ActionParameter {

    private EntityId entity;

    public EntityParameter() {
    }

    public EntityParameter(EntityId entity) {
        this.entity = entity;
    }

    public EntityParameter(Entity entity) {
        this.entity = entity.getId();
    }

    public EntityId getEntity() {
        return this.entity;
    }

    public String toString() {
        return "EntityParameter[" + this.entity + "]";
    }
}