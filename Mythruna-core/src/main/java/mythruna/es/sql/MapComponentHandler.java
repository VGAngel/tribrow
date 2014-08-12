package mythruna.es.sql;

import mythruna.es.ComponentFilter;
import mythruna.es.EntityComponent;
import mythruna.es.EntityId;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MapComponentHandler<T extends EntityComponent> implements ComponentHandler<T> {

    private Map<EntityId, T> components = new ConcurrentHashMap();

    public MapComponentHandler() {
    }

    public void setComponent(EntityId entityId, T component) {
        this.components.put(entityId, component);
    }

    public boolean removeComponent(EntityId entityId) {
        return this.components.remove(entityId) != null;
    }

    public T getComponent(EntityId entityId) {
        return (T) this.components.get(entityId);
    }

    public Set<EntityId> getEntities() {
        return this.components.keySet();
    }

    public Set<EntityId> getEntities(ComponentFilter filter) {
        if (filter == null) {
            return this.components.keySet();
        }
        Set results = new HashSet();
        for (Map.Entry e : this.components.entrySet()) {
            if (filter.evaluate((EntityComponent) e.getValue()))
                results.add(e.getKey());
        }
        return results;
    }

    public EntityId findEntity(ComponentFilter filter) {
        for (Map.Entry e : this.components.entrySet()) {
            if ((filter == null) || (filter.evaluate((EntityComponent) e.getValue())))
                return (EntityId) e.getKey();
        }
        return null;
    }
}