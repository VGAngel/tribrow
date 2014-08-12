package mythruna.msg;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import mythruna.es.ComponentFilter;

import java.util.Arrays;

@Serializable
public class GetEntitySetMessage extends AbstractMessage {

    private int setId;
    private ComponentFilter filter;
    private Class[] componentTypes;

    public GetEntitySetMessage() {
    }

    public GetEntitySetMessage(int setId, ComponentFilter filter, Class[] components) {
        this.setId = setId;
        this.filter = filter;
        this.componentTypes = components;
    }

    public int getSetId() {
        return this.setId;
    }

    public ComponentFilter getFilter() {
        return this.filter;
    }

    public Class[] getComponentTypes() {
        return this.componentTypes;
    }

    public String toString() {
        return "GetEntitySetMessage[" + this.setId + ", " + this.filter + ", " + Arrays.asList(this.componentTypes) + "]";
    }
}