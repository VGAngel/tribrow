package mythruna.sim;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

public class FrameTransition {

    private long startTime;
    private Vector3f startPos;
    private Quaternion startRot;
    private long endTime;
    private Vector3f endPos;
    private Quaternion endRot;

    public FrameTransition(long startTime, Vector3f startPos, Quaternion startRot, long endTime, Vector3f endPos, Quaternion endRot) {
        if (startTime > endTime)
            throw new IllegalArgumentException("Frame transitions cannot go backwards.");
        this.startTime = startTime;
        this.startPos = startPos;
        this.startRot = startRot;
        this.endTime = endTime;
        this.endPos = endPos;
        this.endRot = endRot;
    }

    public FrameTransition(FrameTransition last, long endTime, Vector3f endPos, Quaternion endRot) {
        this(last.endTime, last.endPos, last.endRot, endTime, endPos, endRot);
    }

    public boolean contains(long time) {
        if (time < this.startTime)
            return false;
        return time <= this.endTime;
    }

    public long getStartTime() {
        return this.startTime;
    }

    public long getEndTime() {
        return this.endTime;
    }

    protected final float tween(long time) {
        long length = this.endTime - this.startTime;
        if (length == 0L) {
            return 0.0F;
        }
        float part = (float) (time - this.startTime);
        if (part > (float) length)
            return 1.0F;
        if (part < 0.0F)
            return 0.0F;
        return part / (float) length;
    }

    public Vector3f getFrameVelocity() {
        return new Vector3f(this.endPos.x - this.startPos.x, this.endPos.y - this.startPos.y, this.endPos.z - this.startPos.z);
    }

    public Vector3f getPosition(long time) {
        return getPosition(time, false);
    }

    public Vector3f getPosition(long time, boolean clamp) {
        if ((time < this.startTime) && (!clamp)) {
            return null;
        }
        float t = tween(time);

        Vector3f result = new Vector3f().interpolate(this.startPos, this.endPos, t);
        return result;
    }

    public Quaternion getRotation(long time) {
        return getRotation(time, false);
    }

    public Quaternion getRotation(long time, boolean clamp) {
        if (this.startRot == null) {
            return null;
        }
        if ((time < this.startTime) && (!clamp)) {
            return null;
        }
        Quaternion result = new Quaternion().slerp(this.startRot, this.endRot, tween(time));
        return result;
    }

    public static void main(String[] args) {
        Vector3f start = new Vector3f(0.0F, 0.0F, 0.0F);
        Vector3f end = new Vector3f(100.0F, 0.0F, 0.0F);

        Quaternion q1 = new Quaternion().fromAngles(0.0F, 0.0F, 0.0F);
        Quaternion q2 = new Quaternion().fromAngles(0.0F, 3.141593F, 0.0F);

        FrameTransition ft = new FrameTransition(10L, start, q1, 100L, end, q2);
        System.out.println(ft);

        for (int i = 0; i < 110; i++) {
            System.out.println("pos[" + i + "] = " + ft.getPosition(i) + "  rot:" + ft.getRotation(i));
        }
    }

    public String toString() {
        return "FrameTransition[ t:" + this.startTime + ", pos:" + this.startPos + ", rot:" + this.startRot + " -> t:" + this.endTime + ", pos:" + this.endPos + ", rot:" + this.endRot + " ]";
    }
}