package mythruna.msg;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import mythruna.sim.Mob;
import mythruna.sim.MobClass;

@Serializable
public class EntityStateMessage extends AbstractMessage {
    
    private long time;
    private int type;
    private long id;
    private float x;
    private float y;
    private float z;
    private float[] quat;

    public EntityStateMessage() {
    }

    public EntityStateMessage(long time, Mob e, Vector3f loc, Quaternion facing) {
        this(time, e.getType().getRawId(), e.getId(), loc, facing);
    }

    public EntityStateMessage(long time, int type, long id, Vector3f loc, Quaternion facing) {
        this.time = time;
        this.type = type;
        this.id = id;
        this.x = loc.x;
        this.y = loc.y;
        this.z = loc.z;
        this.quat = new float[]{facing.getX(), facing.getY(), facing.getZ(), facing.getW()};
        setReliable(false);
    }

    public long getId() {
        return this.id;
    }

    public MobClass getType() {
        return new MobClass(this.type);
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
        return "EntityStateMessage[ time:" + this.time + ", id:" + this.id + ", type:" + this.type + ", " + getFacing() + ", " + getLocation() + "]";
    }
}