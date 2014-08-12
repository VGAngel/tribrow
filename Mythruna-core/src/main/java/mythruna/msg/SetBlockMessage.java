package mythruna.msg;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import mythruna.Vector3i;

@Serializable
public class SetBlockMessage extends AbstractMessage {

    private long time;
    private int x;
    private int y;
    private int z;
    private int type;

    public SetBlockMessage() {
    }

    public SetBlockMessage(long time, int x, int y, int z, int type) {
        this.time = time;
        this.x = x;
        this.y = y;
        this.z = z;
        this.type = type;
    }

    public long getTime() {
        return this.time;
    }

    public Vector3i getLocation() {
        return new Vector3i(this.x, this.y, this.z);
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String toString() {
        return "SetBlockMessage[ time:" + this.time + ", " + getLocation() + ", " + this.type + "]";
    }
}