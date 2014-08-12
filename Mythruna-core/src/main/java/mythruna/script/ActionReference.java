package mythruna.script;

import com.jme3.network.serializing.Serializable;

@Serializable
public class ActionReference {

    private int id;
    private String group;
    private String name;
    private ActionType type;
    private long iconRef;

    public ActionReference() {
    }

    public ActionReference(int id, String group, String name, ActionType type, long iconRef) {
        this.id = id;
        this.group = group;
        this.name = name;
        this.type = type;
        this.iconRef = iconRef;
    }

    public int getId() {
        return this.id;
    }

    public String getGroup() {
        return this.group;
    }

    public String getName() {
        return this.name;
    }

    public ActionType getType() {
        return this.type;
    }

    public long getIcon() {
        return this.iconRef;
    }

    public String toString() {
        return "ActionReference[" + this.id + ", " + this.group + ", " + this.name + "]";
    }
}