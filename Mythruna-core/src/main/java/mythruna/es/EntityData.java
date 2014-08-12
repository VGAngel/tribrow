package mythruna.es;

import java.util.Set;

public abstract interface EntityData {

    public abstract EntityId createEntity();

    public abstract void removeEntity(EntityId paramEntityId);

    public abstract void setComponent(EntityId paramEntityId, EntityComponent paramEntityComponent);

    public abstract void setComponents(EntityId paramEntityId, EntityComponent[] paramArrayOfEntityComponent);

    public abstract boolean removeComponent(EntityId paramEntityId, Class paramClass);

    public abstract <T extends EntityComponent> T getComponent(EntityId paramEntityId, Class<T> paramClass);

    public abstract Entity getEntity(EntityId paramEntityId, Class[] paramArrayOfClass);

    public abstract EntityId findEntity(ComponentFilter paramComponentFilter, Class[] paramArrayOfClass);

    public abstract Set<EntityId> findEntities(ComponentFilter paramComponentFilter, Class[] paramArrayOfClass);

    public abstract EntitySet getEntities(Class[] paramArrayOfClass);

    public abstract EntitySet getEntities(ComponentFilter paramComponentFilter, Class[] paramArrayOfClass);

    public abstract void releaseEntitySet(EntitySet paramEntitySet);

    public abstract StringIndex getStrings();

    public abstract void execute(EntityProcessor paramEntityProcessor);

    public abstract void close();
}