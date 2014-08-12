package mythruna.msg;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import mythruna.es.Entity;
import mythruna.es.EntityComponent;
import mythruna.es.EntityId;

import java.util.Arrays;

@Serializable
public class EntityComponentsMessage extends AbstractMessage {

    private int requestId;
    private EntityId entityId;
    private EntityComponent[] components;

    public EntityComponentsMessage() {
    }

    public EntityComponentsMessage(int requestId, Entity e) {
        this.requestId = requestId;
        this.entityId = e.getId();
        this.components = e.getComponents();
    }

    public EntityComponentsMessage(int requestId, EntityId id) {
        this.requestId = requestId;
        this.entityId = id;
    }

    public int getRequestId() {
        return this.requestId;
    }

    public EntityId getEntityId() {
        return this.entityId;
    }

    public EntityComponent[] getComponents() {
        return this.components;
    }

    public String toString() {
        return new StringBuilder().append("EntityComponentsMessage[").append(this.requestId).append(", ").append(this.entityId).append(", ").append(this.components != null ? Arrays.asList(this.components) : null).append("]").toString();
    }
}