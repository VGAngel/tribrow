package mythruna.msg;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import mythruna.sim.Mob;
import mythruna.sim.MobClass;

@Serializable
public class EntityListUpdateMessage extends AbstractMessage {

    public static int ADDED = 0;
    public static int REMOVED = 1;
    public static int CHANGED = 2;
    private int type;
    private long id;
    private String name;
    private int changeType;

    public EntityListUpdateMessage() {
    }

    public EntityListUpdateMessage(Mob entity, int changeType) {
        this(entity.getType().getRawId(), entity.getId(), entity.getName(), changeType);
    }

    public EntityListUpdateMessage(int type, long id, String name, int changeType) {
        this.type = type;
        this.id = id;
        this.name = name;
        this.changeType = changeType;
        setReliable(true);
    }

    public long getId() {
        return this.id;
    }

    public MobClass getType() {
        return new MobClass(this.type);
    }

    public String getName() {
        return this.name;
    }

    public int getChangeType() {
        return this.changeType;
    }

    public String toString() {
        return "EntityListUpdateMessage[ id:" + this.id + ", type:" + this.type + ", " + this.name + ", " + this.changeType + "]";
    }
}