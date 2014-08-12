package mythruna.msg;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import mythruna.es.ComponentFilter;

@Serializable
public class ResetEntitySetFilterMessage extends AbstractMessage {

    private int setId;
    private ComponentFilter filter;

    public ResetEntitySetFilterMessage() {
    }

    public ResetEntitySetFilterMessage(int setId, ComponentFilter filter) {
        this.setId = setId;
        this.filter = filter;
    }

    public int getSetId() {
        return this.setId;
    }

    public ComponentFilter getFilter() {
        return this.filter;
    }

    public String toString() {
        return "ResetEntitySetFilterMessage[" + this.setId + ", " + this.filter + "]";
    }
}