package mythruna.es;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.network.serializing.Serializable;
import mythruna.Coordinates;
import mythruna.Vector3i;

@Serializable
public class Position implements EntityComponent, PersistentComponent {

    private long columnId;
    private Vector3f pos;
    private Quaternion rotation;

    public Position() {
        this(new Vector3f(), Quaternion.DIRECTION_Z.clone());
    }

    public Position(Vector3f pos) {
        this(pos, Quaternion.DIRECTION_Z.clone());
    }

    public Position(Vector3i pos) {
        this(new Vector3f(pos.x, pos.y, pos.z), Quaternion.DIRECTION_Z.clone());
    }

    public Position(Vector3i pos, double heading) {
        this(new Vector3f(pos.x, pos.y, pos.z), new Quaternion().fromAngles(0.0F, (float) heading, 0.0F));
    }

    public Position(Vector3f pos, double heading) {
        this(pos, new Quaternion().fromAngles(0.0F, (float) heading, 0.0F));
    }

    public Position(double x, double y, double z) {
        this(new Vector3f((float) x, (float) y, (float) z), Quaternion.DIRECTION_Z.clone());
    }

    public Position(Vector3f pos, Quaternion rot) {
        if (pos == null)
            throw new IllegalArgumentException("Position parameter cannot be null.");
        this.pos = pos;
        this.rotation = rot;
        this.columnId = Coordinates.worldToColumnId(pos);
    }

    public int hashCode() {
        return Long.valueOf(this.columnId).hashCode() + this.pos.hashCode() + this.rotation.hashCode();
    }

    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (o == null)
            return false;
        if (o.getClass() != getClass()) {
            return false;
        }
        Position other = (Position) o;
        if (other.columnId != this.columnId)
            return false;
        if (!other.pos.equals(this.pos))
            return false;
        if (!other.rotation.equals(this.rotation)) {
            return false;
        }
        return true;
    }

    public Class<Position> getType() {
        return Position.class;
    }

    public long getColumnId() {
        return this.columnId;
    }

    public Vector3f getLocation() {
        return this.pos;
    }

    public Quaternion getRotation() {
        return this.rotation;
    }

    public String toString() {
        return "Position[" + this.pos + ", " + this.rotation + ", " + Long.toHexString(this.columnId) + "]";
    }
}