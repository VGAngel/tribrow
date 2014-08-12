package mythruna.msg;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import mythruna.es.ComponentFilter;

import java.util.Arrays;

@Serializable
public class FindEntitiesMessage extends AbstractMessage {

    private int requestId;
    private ComponentFilter filter;
    private Class[] types;

    public FindEntitiesMessage() {
    }

    public FindEntitiesMessage(int requestId, ComponentFilter filter, Class[] types) {
        this.requestId = requestId;
        this.filter = filter;
        this.types = types;
    }

    public int getRequestId() {
        return this.requestId;
    }

    public ComponentFilter getFilter() {
        return this.filter;
    }

    public Class[] getTypes() {
        return this.types;
    }

    public String toString() {
        return "FindEntitiesMessage[" + this.requestId + ", " + this.filter + ", " + Arrays.asList(this.types) + "]";
    }
}