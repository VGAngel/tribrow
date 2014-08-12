package mythruna.es;

import com.jme3.network.serializing.Serializable;

import java.util.Arrays;

@Serializable
public class OrFilter<T extends EntityComponent> implements ComponentFilter<T> {

    private Class<T> type;
    private ComponentFilter<? extends T>[] operands;

    public OrFilter() {
    }

    public OrFilter(Class<T> type, ComponentFilter<? extends T>[] operands) {
        this.type = type;
        this.operands = operands;
    }

    public static <T extends EntityComponent> OrFilter<T> create(Class<T> type, ComponentFilter<? extends T>[] operands) {
        return new OrFilter(type, operands);
    }

    public ComponentFilter<? extends T>[] getOperands() {
        return this.operands;
    }

    public Class<T> getComponentType() {
        return this.type;
    }

    public boolean evaluate(EntityComponent c) {
        if (!this.type.isInstance(c)) {
            return false;
        }
        if (this.operands == null) {
            return true;
        }

        for (ComponentFilter f : this.operands) {
            if (f.evaluate(c)) {
                return true;
            }
        }

        return false;
    }

    public String toString() {
        return "OrFilter[" + Arrays.asList(this.operands) + "]";
    }
}