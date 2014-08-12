package mythruna.script;

import com.jme3.math.Vector3f;
import com.jme3.network.serializing.Serializable;
import mythruna.es.EntityId;

@Serializable
public class ObjectParameter implements ActionParameter {

    private EntityId object;
    private Vector3f location;

    public ObjectParameter() {
    }

    public ObjectParameter(EntityId object, Vector3f location) {
        this.object = object;
        this.location = location;
    }

    public Vector3f getLocation() {
        return this.location;
    }

    public EntityId getObject() {
        return this.object;
    }

    public String toString() {
        return "ObjectParameter[" + this.object + ", " + this.location + "]";
    }
}