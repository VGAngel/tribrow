package mythruna.es;

import com.jme3.network.serializing.Serializable;

@Serializable(id = 69)
public final class EntityId implements Comparable<EntityId> {

    public static final EntityId NULL_ID = new EntityId(-9223372036854775808L);
    private long id;

    public EntityId() {
    }

    public EntityId(long id) {
        this.id = id;
    }

    public long getId() {
        return this.id;
    }

    public int compareTo(EntityId other) {
        if (this.id < other.id)
            return -1;
        if (this.id > other.id)
            return 1;
        return 0;
    }

    public int hashCode() {
        return (int) (this.id ^ this.id >>> 32);
    }

    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (o == null)
            return false;
        if (o.getClass() != getClass())
            return false;
        EntityId other = (EntityId) o;
        return this.id == other.id;
    }

    public String toString() {
        return "EntityId[" + this.id + "]";
    }
}