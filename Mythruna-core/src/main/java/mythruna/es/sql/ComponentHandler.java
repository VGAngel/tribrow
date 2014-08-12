package mythruna.es.sql;

import mythruna.es.ComponentFilter;
import mythruna.es.EntityComponent;
import mythruna.es.EntityId;

import java.util.Set;

public abstract interface ComponentHandler<T extends EntityComponent> {

    public abstract void setComponent(EntityId paramEntityId, T paramT);

    public abstract boolean removeComponent(EntityId paramEntityId);

    public abstract T getComponent(EntityId paramEntityId);

    public abstract Set<EntityId> getEntities();

    public abstract Set<EntityId> getEntities(ComponentFilter paramComponentFilter);

    public abstract EntityId findEntity(ComponentFilter paramComponentFilter);
}