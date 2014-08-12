package mythruna.item;

import mythruna.es.EntityComponent;
import mythruna.es.EntityId;

public class ObjectAction implements EntityComponent {

    private EntityId object;
    private String group;
    private String name;

    public ObjectAction() {
    }

    public ObjectAction(EntityId object, String group, String name) {
        this.object = object;
        this.group = group;
        this.name = name;
    }

    public Class<ObjectAction> getType() {
        return ObjectAction.class;
    }

    public EntityId getObject() {
        return this.object;
    }

    public String getGroup() {
        return this.group;
    }

    public String getName() {
        return this.name;
    }

    public String toString() {
        return "ObjectAction[" + this.object + "->" + this.group + ":" + this.name + "]";
    }
}