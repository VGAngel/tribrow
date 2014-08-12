package mythruna.es;

import com.jme3.network.serializing.Serializable;

import java.util.Arrays;

@Serializable
public class AndFilter<T extends EntityComponent> implements ComponentFilter<T> {

    private Class<T> type;
    private ComponentFilter<? extends T>[] operands;

    public AndFilter() {
    }

    public AndFilter(Class<T> type, ComponentFilter<? extends T>[] operands) {
        this.type = type;
        this.operands = operands;
    }

    public static <T extends EntityComponent> AndFilter<T> create(Class<T> type, ComponentFilter<? extends T>[] operands) {
        return new AndFilter(type, operands);
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
            if (!f.evaluate(c)) {
                return false;
            }
        }

        return true;
    }

    public String toString() {
        return "AndFilter[" + Arrays.asList(this.operands) + "]";
    }
}