package mythruna.item;

import mythruna.es.EntityComponent;
import mythruna.es.EntityId;
import mythruna.es.PersistentComponent;

public class ClassInstance implements EntityComponent, PersistentComponent {

    private EntityId classEntity;

    public ClassInstance() {
    }

    public ClassInstance(EntityId classEntity) {
        this.classEntity = classEntity;
    }

    public Class<ClassInstance> getType() {
        return ClassInstance.class;
    }

    public EntityId getClassEntity() {
        return this.classEntity;
    }

    public String toString() {
        return "ClassInstance[" + this.classEntity + "]";
    }
}