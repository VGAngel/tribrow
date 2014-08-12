package mythruna.item;

import mythruna.es.EntityComponent;
import mythruna.es.PersistentComponent;

public class ObjectClass implements EntityComponent, PersistentComponent {

    private String name;

    public ObjectClass() {
    }

    public ObjectClass(String name) {
        this.name = name;
    }

    public Class<ObjectClass> getType() {
        return ObjectClass.class;
    }

    public String getName() {
        return this.name;
    }

    public String toString() {
        return "ObjectClass[" + this.name + "]";
    }
}