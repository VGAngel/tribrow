package mythruna.sim;

public class MobClass {

    public static final MobClass PLAYER = new MobClass(0);
    private int id;

    public MobClass(int id) {
        this.id = id;
    }

    public MobType getType() {
        if (this.id < 0)
            return MobType.MOB;
        if (this.id > 0)
            return MobType.PLACEABLE;
        return MobType.PLAYER;
    }

    public int getRawId() {
        return this.id;
    }

    public int getClassId() {
        if (this.id > 0)
            return this.id - 1;
        if (this.id < 0)
            return -this.id - 1;
        return 0;
    }

    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (o == this)
            return true;
        if (o.getClass() != getClass())
            return false;
        MobClass c = (MobClass) o;
        return c.id == this.id;
    }

    public int hashCode() {
        return this.id;
    }

    public String toString() {
        return getType() + ":" + getClassId();
    }
}