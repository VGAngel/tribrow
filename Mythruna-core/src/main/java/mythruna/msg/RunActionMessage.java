package mythruna.msg;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import mythruna.es.EntityId;
import mythruna.script.ActionParameter;

@Serializable
public class RunActionMessage extends AbstractMessage {

    private int id;
    private EntityId target;
    private ActionParameter parm;

    public RunActionMessage() {
    }

    public RunActionMessage(int id, ActionParameter parm) {
        this.id = id;
        this.parm = parm;
    }

    public RunActionMessage(int id, EntityId target, ActionParameter parm) {
        this.id = id;
        this.target = target;
        this.parm = parm;
    }

    public int getActionId() {
        return this.id;
    }

    public EntityId getTarget() {
        return this.target;
    }

    public ActionParameter getActionParameter() {
        return this.parm;
    }

    public String toString() {
        return "RunActionMessage[" + this.id + ", " + this.target + ", " + this.parm + "]";
    }
}