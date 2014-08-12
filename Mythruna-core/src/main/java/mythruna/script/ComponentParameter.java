package mythruna.script;

import com.jme3.network.serializing.Serializable;
import mythruna.es.EntityComponent;

@Serializable
public class ComponentParameter implements ActionParameter {

    private EntityComponent component;

    public ComponentParameter() {
    }

    public ComponentParameter(EntityComponent component) {
        this.component = component;
    }

    public EntityComponent getComponent() {
        return this.component;
    }

    public String toString() {
        return "ComponentParameter[" + this.component + "]";
    }
}