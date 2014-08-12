package mythruna.msg;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;

@Serializable
public class ReleaseEntitySetMessage extends AbstractMessage {

    private int setId;

    public ReleaseEntitySetMessage() {
    }

    public ReleaseEntitySetMessage(int setId) {
        this.setId = setId;
    }

    public int getSetId() {
        return this.setId;
    }

    public String toString() {
        return "ReleaseEntitySetMessage[" + this.setId + "]";
    }
}