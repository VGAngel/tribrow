package mythruna.msg;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;

@Serializable
public class ReleaseObserveChangesMessage extends AbstractMessage {

    private int queueId;

    public ReleaseObserveChangesMessage() {
    }

    public ReleaseObserveChangesMessage(int queueId) {
        this.queueId = queueId;
    }

    public int getQueueId() {
        return this.queueId;
    }

    public String toString() {
        return "ReleaseObserveChangesMessage[" + this.queueId + "]";
    }
}