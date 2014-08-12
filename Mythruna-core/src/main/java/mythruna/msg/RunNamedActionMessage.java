package mythruna.msg;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import mythruna.es.EntityId;
import mythruna.script.ActionParameter;

@Serializable
public class RunNamedActionMessage extends AbstractMessage {

    private short nameId;
    private String name;
    private EntityId target;
    private ActionParameter parm;

    public RunNamedActionMessage() {
    }

    public RunNamedActionMessage(short nameId, EntityId target, ActionParameter parm) {
        this.nameId = nameId;
        this.target = target;
        this.parm = parm;
    }

    public RunNamedActionMessage(String name, EntityId target, ActionParameter parm) {
        this.name = name;
        this.target = target;
        this.parm = parm;
    }

    public short getNameId() {
        return this.nameId;
    }

    public String getName() {
        return this.name;
    }

    public EntityId getTarget() {
        return this.target;
    }

    public ActionParameter getActionParameter() {
        return this.parm;
    }

    public String toString() {
        return "RunNamedActionMessage[" + this.nameId + " or " + this.name + ", target:" + this.target + ", parm:" + this.parm + "]";
    }
}