package mythruna;

import com.jme3.math.Quaternion;

public class MovementState {

    public static final byte FORWARD = 1;
    public static final byte BACK = 2;
    public static final byte STRAFE_LEFT = 4;
    public static final byte STRAFE_RIGHT = 8;
    public static final byte RAISE = 16;
    public static final byte LOWER = 32;
    public static final byte JUMP = 64;
    public static final byte RUN = -128;
    private byte movement = 0;
    private Quaternion facing = new Quaternion();

    public MovementState() {
    }

    public MovementState(MovementState state) {
        set(state);
    }

    public boolean isMoving() {
        return this.movement != 0;
    }

    public void set(MovementState state) {
        setMoveState(state.getFacing(), state.getMovementFlags());
    }

    public void setMoveState(Quaternion facing, byte states) {
        this.facing.set(facing);
        this.movement = states;
    }

    public void setFacing(Quaternion q) {
        this.facing.set(q);
    }

    public Quaternion getFacing() {
        return this.facing;
    }

    public void setMovementFlags(byte states) {
        this.movement = states;
    }

    public byte getMovementFlags() {
        return this.movement;
    }

    public void set(byte type, boolean on) {
        if (on)
            this.movement = (byte) (this.movement | type);
        else
            this.movement = (byte) (this.movement & (type ^ 0xFFFFFFFF));
    }

    public boolean isOn(byte type) {
        return (this.movement & type) != 0;
    }

    public String toString() {
        return "MovementState[" + Integer.toHexString(this.movement) + ", " + this.facing + "]";
    }
}