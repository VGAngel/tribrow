package mythruna.es;

import com.jme3.network.serializing.Serializable;
import org.progeeks.util.ObjectUtils;

import java.lang.reflect.Field;

@Serializable
public class FieldFilter<T extends EntityComponent> implements ComponentFilter<T> {

    private Class<T> type;
    private Field field;
    private Object value;
    private transient boolean initialized = false;

    public FieldFilter() {
    }

    public FieldFilter(Class<T> type, String field, Object value) {
        try {
            this.type = type;
            this.field = type.getDeclaredField(field);
            this.field.setAccessible(true);
            this.value = value;
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException("Field not found:" + field + " on type:" + type, e);
        }
    }

    public static <T extends EntityComponent> FieldFilter<T> create(Class<T> type, String field, Object value) {
        return new FieldFilter(type, field, value);
    }

    public String getFieldName() {
        return this.field.getName();
    }

    public Object getValue() {
        return this.value;
    }

    public Class<T> getComponentType() {
        return this.type;
    }

    public boolean evaluate(EntityComponent c) {
        if (!this.type.isInstance(c)) {
            return false;
        }
        try {
            if (!this.initialized) {
                this.field.setAccessible(true);
                this.initialized = true;
            }
            Object val = this.field.get(c);

            return ObjectUtils.areEqual(this.value, val);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Error retrieving field[" + this.field + "] of:" + c, e);
        }
    }

    public String toString() {
        return "FieldFilter[" + this.field + " == " + this.value + "]";
    }
}