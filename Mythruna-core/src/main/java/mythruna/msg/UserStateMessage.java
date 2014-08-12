package mythruna.msg;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import mythruna.MovementState;

@Serializable
public class UserStateMessage extends AbstractMessage {

    private long time;
    private float x;
    private float y;
    private float z;
    private byte movement;
    private float[] quat;

    public UserStateMessage() {
    }

    public UserStateMessage(long time, Vector3f loc, MovementState state) {
        this.time = time;
        this.x = loc.x;
        this.y = loc.y;
        this.z = loc.z;
        this.movement = state.getMovementFlags();
        Quaternion q = state.getFacing();
        this.quat = new float[]{q.getX(), q.getY(), q.getZ(), q.getW()};
        setReliable(false);
    }

    public Quaternion getFacing() {
        Quaternion q = new Quaternion(this.quat[0], this.quat[1], this.quat[2], this.quat[3]);
        return q;
    }

    public MovementState getMovementState() {
        MovementState state = new MovementState();
        state.setMoveState(getFacing(), this.movement);
        return state;
    }

    public Vector3f getLocation() {
        return new Vector3f(this.x, this.y, this.z);
    }

    public long getTime() {
        return this.time;
    }

    public String toString() {
        return "UserStateMessage[ time:" + this.time + ", " + Integer.toHexString(this.movement) + ", " + getFacing() + ", " + getLocation() + "]";
    }
}