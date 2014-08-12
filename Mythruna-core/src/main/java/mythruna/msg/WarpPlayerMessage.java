package mythruna.msg;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;

@Serializable
public class WarpPlayerMessage extends AbstractMessage {

    private long time;
    private float x;
    private float y;
    private float z;
    private float[] quat;

    public WarpPlayerMessage() {
    }

    public WarpPlayerMessage(long time, Vector3f loc, Quaternion facing) {
        this.time = time;
        this.x = loc.x;
        this.y = loc.y;
        this.z = loc.z;
        this.quat = new float[]{facing.getX(), facing.getY(), facing.getZ(), facing.getW()};
        setReliable(true);
    }

    public Quaternion getFacing() {
        Quaternion q = new Quaternion(this.quat[0], this.quat[1], this.quat[2], this.quat[3]);
        return q;
    }

    public Vector3f getLocation() {
        return new Vector3f(this.x, this.y, this.z);
    }

    public long getTime() {
        return this.time;
    }

    public String toString() {
        return "WarpPlayerMessage[ time:" + this.time + ", " + getFacing() + ", " + getLocation() + "]";
    }
}