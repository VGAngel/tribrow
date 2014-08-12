package mythruna.msg;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import mythruna.es.EntityAction;
import mythruna.es.EntityId;

@Serializable
public class EntityActionMessage extends AbstractMessage {

    private EntityAction action;
    private EntityId target;

    public EntityActionMessage() {
    }

    public EntityActionMessage(EntityAction action, EntityId target) {
        this.action = action;
        this.target = target;
    }

    public EntityAction getAction() {
        return this.action;
    }

    public EntityId getTarget() {
        return this.target;
    }

    public String toString() {
        return "EntityActionMessage[" + this.action + ", " + this.target + "]";
    }
}