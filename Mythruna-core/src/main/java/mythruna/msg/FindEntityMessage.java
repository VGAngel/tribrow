package mythruna.msg;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import mythruna.es.ComponentFilter;

@Serializable
public class FindEntityMessage extends AbstractMessage {

    private int requestId;
    private ComponentFilter filter;

    public FindEntityMessage() {
    }

    public FindEntityMessage(int requestId, ComponentFilter filter) {
        this.requestId = requestId;
        this.filter = filter;
    }

    public int getRequestId() {
        return this.requestId;
    }

    public ComponentFilter getFilter() {
        return this.filter;
    }

    public String toString() {
        return "FindEntityMessage[" + this.requestId + ", " + this.filter + "]";
    }
}