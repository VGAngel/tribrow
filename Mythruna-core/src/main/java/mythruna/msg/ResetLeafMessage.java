package mythruna.msg;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;

@Serializable
public class ResetLeafMessage extends AbstractMessage {

    private long time;
    private int x;
    private int y;
    private int z;

    public ResetLeafMessage() {
    }

    public ResetLeafMessage(long time, int x, int y, int z) {
        this.time = time;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public long getTime() {
        return this.time;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getZ() {
        return this.z;
    }

    public String toString() {
        return "ResetLeafMessage[ " + this.x + ", " + this.y + ", " + this.z + "]";
    }
}