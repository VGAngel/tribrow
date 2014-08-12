package mythruna.item;

import mythruna.es.EntityComponent;
import mythruna.es.EntityId;
import mythruna.es.PersistentComponent;

public class IntVariable implements Variable, EntityComponent, PersistentComponent {

    private EntityId holder;
    private int nameId;
    private int value;

    public IntVariable() {
    }

    public IntVariable(EntityId holder, int nameId, int value) {
        this.holder = holder;
        this.nameId = nameId;
        this.value = value;
    }

    public Class<IntVariable> getType() {
        return IntVariable.class;
    }

    public EntityId getHolder() {
        return this.holder;
    }

    public int getNameId() {
        return this.nameId;
    }

    public int getValue() {
        return this.value;
    }

    public String toString() {
        return "IntVariable[" + this.holder + ", " + this.nameId + ", " + this.value + "]";
    }
}