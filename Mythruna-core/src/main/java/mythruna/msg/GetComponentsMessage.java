package mythruna.msg;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import mythruna.es.EntityId;

import java.util.Arrays;

@Serializable
public class GetComponentsMessage extends AbstractMessage {

    private int requestId;
    private EntityId entityId;
    private Class[] componentTypes;

    public GetComponentsMessage() {
    }

    public GetComponentsMessage(int requestId, EntityId entityId, Class[] components) {
        this.requestId = requestId;
        this.entityId = entityId;
        this.componentTypes = components;
    }

    public int getRequestId() {
        return this.requestId;
    }

    public EntityId getEntityId() {
        return this.entityId;
    }

    public Class[] getComponentTypes() {
        return this.componentTypes;
    }

    public String toString() {
        return "GetComponentsMessage[" + this.requestId + ", " + this.entityId + ", " + Arrays.asList(this.componentTypes) + "]";
    }
}