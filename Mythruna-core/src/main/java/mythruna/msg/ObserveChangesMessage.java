package mythruna.msg;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;

import java.util.Arrays;

@Serializable
public class ObserveChangesMessage extends AbstractMessage {

    private int queueId;
    private Class[] componentTypes;

    public ObserveChangesMessage() {
    }

    public ObserveChangesMessage(int queueId, Class[] components) {
        this.queueId = queueId;
        this.componentTypes = components;
    }

    public int getQueueId() {
        return this.queueId;
    }

    public Class[] getComponentTypes() {
        return this.componentTypes;
    }

    public String toString() {
        return "ObserveChangesMessage[" + this.queueId + ", " + Arrays.asList(this.componentTypes) + "]";
    }
}