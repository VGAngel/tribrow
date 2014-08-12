package mythruna.es;

import java.util.Arrays;

public class DefaultEntity implements Entity {

    protected static AbstractEntityData ed;
    private EntityId id;
    private EntityComponent[] components;
    private Class[] types;

    public DefaultEntity(EntityId id, EntityComponent[] components, Class[] types) {
        this.id = id;
        this.components = components;
        this.types = types;

        validate();
    }

    protected void validate() {
        for (int i = 0; i < this.types.length; i++) {
            if (this.components[i] != null) {
                if (this.components[i].getType() != this.types[i])
                    throw new RuntimeException("Validation error.  components[" + i + "]:" + this.components[i] + " is not of type:" + this.types[i]);
            }
        }
    }

    public EntityId getId() {
        return this.id;
    }

    public EntityComponent[] getComponents() {
        return this.components;
    }

    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (o == null)
            return false;
        if (o.getClass() != getClass()) {
            return false;
        }
        return this.id.equals(((DefaultEntity) o).id);
    }

    public int hashCode() {
        return this.id.hashCode();
    }

    public <T extends EntityComponent> T get(Class<T> type) {
        for (EntityComponent c : this.components) {
            if ((c != null) && (c.getType() == type))
                return (T) type.cast(c);
        }
        return null;
    }

    public void set(EntityComponent c) {
        for (int i = 0; i < this.components.length; i++) {
            if (this.components[i].getType().isInstance(c)) {
                ed.replace(this, this.components[i], c);
                this.components[i] = c;
                return;
            }

        }

        ed.setComponent(this.id, c);
    }

    public boolean isComplete() {
        for (int i = 0; i < this.components.length; i++) {
            if (this.components[i] == null)
                return false;
        }
        return true;
    }

    protected void update(EntityComponent c) {
        for (int i = 0; i < this.components.length; i++) {
            if (this.components[i].getType().isInstance(c)) {
                this.components[i] = c;
                return;
            }
        }
    }

    protected void clear(Class type) {
        for (int i = 0; i < this.components.length; i++) {
            if (this.components[i].getType() == type) {
                this.components[i] = null;
                return;
            }
        }
    }

    protected void clear() {
        for (int i = 0; i < this.components.length; i++)
            this.components[i] = null;
    }

    public String toString() {
        return "Entity[" + this.id + ", values=" + Arrays.asList(this.components) + "]";
    }
}