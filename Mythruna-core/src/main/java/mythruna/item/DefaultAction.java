package mythruna.item;

import mythruna.es.EntityComponent;

public class DefaultAction implements EntityComponent {

    private String group;
    private String name;

    public DefaultAction() {
    }

    public DefaultAction(String group, String name) {
        this.group = group;
        this.name = name;
    }

    public Class<DefaultAction> getType() {
        return DefaultAction.class;
    }

    public String getGroup() {
        return this.group;
    }

    public String getName() {
        return this.name;
    }

    public String toString() {
        return "DefaultAction[" + this.group + ":" + this.name + "]";
    }
}