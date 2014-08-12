package mythruna.db;

import mythruna.event.AbstractEvent;

public class GeneratorEvent extends AbstractEvent<Object> {

    private Object generator;

    public GeneratorEvent(Object generator) {
        super(null);
        this.generator = generator;
    }

    public Object getGenerator() {
        return this.generator;
    }

    public String toString() {
        return "GeneratorEvent[" + this.generator + "]";
    }
}