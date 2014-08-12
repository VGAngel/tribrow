package mythruna.sim;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import mythruna.es.EntityId;

public class Mob {

    private long id;
    private MobClass type;
    private TimeBuffer timeBuffer;
    private String name;
    private boolean alive;

    public Mob(MobClass type, long id, int timeBufferSize) {
        this.type = type;
        this.id = id;
        this.timeBuffer = new TimeBuffer(timeBufferSize);
        this.alive = true;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public long getId() {
        return this.id;
    }

    public EntityId getEntityId() {
        if (this.type != MobClass.PLAYER)
            return null;
        return new EntityId(this.id);
    }

    public MobClass getType() {
        return this.type;
    }

    public boolean isAlive() {
        return this.alive;
    }

    protected void setAlive(boolean alive) {
        this.alive = alive;
    }

    public int hashCode() {
        return (int) (this.id ^ this.id >>> 32);
    }

    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (o == this)
            return true;
        if (o.getClass() != getClass())
            return false;
        Mob e = (Mob) o;
        if (e.id != this.id)
            return false;
        if (this.type.getRawId() != e.type.getRawId())
            return false;
        return true;
    }

    public String toString() {
        return "Entity@" + System.identityHashCode(this) + "[id:" + this.id + ", type:" + this.type + ", name:" + this.name + "]";
    }

    public Vector3f getPosition(long time) {
        FrameTransition ft = this.timeBuffer.getFrame(time);
        return ft.getPosition(time);
    }

    public FrameTransition getFrame(long time) {
        return this.timeBuffer.getFrame(time);
    }

    public void initializeTransform(long time, Vector3f v, Quaternion q) {
        this.timeBuffer.addFrame(time, v, q);
    }

    public boolean updateTransform(long time, Vector3f v, Quaternion q) {
        FrameTransition ft = this.timeBuffer.getFrame(time);
        if (ft.getEndTime() >= time) {
            return false;
        }

        boolean changed = true;
        if ((v.equals(ft.getPosition(time))) && (q.equals(ft.getRotation(time)))) {
            changed = false;
        }

        this.timeBuffer.addFrame(time, v, q);
        return changed;
    }

    public TimeBuffer getTimeBuffer() {
        return this.timeBuffer;
    }
}