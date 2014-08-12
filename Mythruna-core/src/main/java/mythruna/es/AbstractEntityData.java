package mythruna.es;

import mythruna.util.NamedThreadFactory;
import mythruna.util.ReportSystem;
import mythruna.util.Reporter;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class AbstractEntityData implements ObservableEntityData {

    private List<EntitySet> entitySets = new CopyOnWriteArrayList<EntitySet>();

    private List<EntityComponentListener> entityListeners = new CopyOnWriteArrayList<EntityComponentListener>();
    private ExecutorService executor;

    protected AbstractEntityData() {
        DefaultEntity.ed = this;

        this.executor = Executors.newFixedThreadPool(4, new NamedThreadFactory("EntityProc", false));

        ReportSystem.registerCacheReporter(new MemReporter());
    }

    public void addEntityComponentListener(EntityComponentListener l) {
        this.entityListeners.add(l);
    }

    public void removeEntityComponentListener(EntityComponentListener l) {
        this.entityListeners.remove(l);
    }

    public void close() {
        this.executor.shutdownNow();
    }

    public void execute(EntityProcessor proc) {
        this.executor.submit(new EntityProcessorRunnable(proc, this));
    }

    protected EntitySet createSet(ComponentFilter filter, Class[] types) {
        EntitySet set = new EntitySet(this, filter, types);
        this.entitySets.add(set);
        return set;
    }

    protected void replace(Entity e, EntityComponent oldValue, EntityComponent newValue) {
        setComponent(e.getId(), newValue);
    }

    public void setComponents(EntityId entityId, EntityComponent[] components) {
        for (EntityComponent c : components)
            setComponent(entityId, c);
    }

    public Entity getEntity(EntityId entityId, Class[] types) {
        EntityComponent[] values = new EntityComponent[types.length];
        for (int i = 0; i < values.length; i++)
            values[i] = getComponent(entityId, types[i]);
        return new DefaultEntity(entityId, values, types);
    }

    protected abstract Set<EntityId> getEntityIds(Class paramClass, ComponentFilter paramComponentFilter);

    protected abstract Set<EntityId> getEntityIds(Class paramClass);

    public EntitySet getEntities(Class[] types) {
        EntitySet results = createSet((ComponentFilter) null, types);

        Set<EntityId> first = getEntityIds(types[0]);
        if (first.isEmpty())
            return results;
        Set<EntityId> and = new HashSet<EntityId>();
        and.addAll(first);

        for (int i = 1; i < types.length; i++) {
            and.retainAll(getEntityIds(types[i]));
        }

        EntityComponent[] buffer = new EntityComponent[types.length];
        for (EntityId id : and) {
            for (int i = 0; i < buffer.length; i++) {
                buffer[i] = getComponent(id, types[i]);
            }

            DefaultEntity e = new DefaultEntity(id, (EntityComponent[]) buffer.clone(), types);
            results.add(e);
        }

        return results;
    }

    protected ComponentFilter forType(ComponentFilter filter, Class type) {
        if ((filter == null) || (filter.getComponentType() != type))
            return null;
        return filter;
    }

    protected abstract EntityId findSingleEntity(ComponentFilter paramComponentFilter);

    public EntityId findEntity(ComponentFilter filter, Class[] types) {
        if ((types == null) || (types.length == 0)) {
            return findSingleEntity(filter);
        }
        Set<EntityId> first = getEntityIds(types[0], forType(filter, types[0]));
        if (first.isEmpty())
            return null;
        Set<EntityId> and = new HashSet<EntityId>();
        and.addAll(first);

        for (int i = 1; i < types.length; i++) {
            Set sub = getEntityIds(types[i], forType(filter, types[i]));
            if (sub.isEmpty())
                return null;
            and.retainAll(sub);
        }

        if (and.isEmpty()) {
            return null;
        }
        return and.iterator().next();
    }

    public Set<EntityId> findEntities(ComponentFilter filter, Class[] types) {
        if ((types == null) || (types.length == 0)) {
            types = new Class[]{filter.getComponentType()};
        }
        Set<EntityId> first = getEntityIds(types[0], forType(filter, types[0]));
        if (first.isEmpty())
            return Collections.emptySet();
        Set<EntityId> and = new HashSet<EntityId>();
        and.addAll(first);

        for (int i = 1; i < types.length; i++) {
            Set sub = getEntityIds(types[i], forType(filter, types[i]));
            if (sub.isEmpty())
                return Collections.emptySet();
            and.retainAll(sub);
        }

        return and;
    }

    public EntitySet getEntities(ComponentFilter filter, Class[] types) {
        EntitySet results = createSet(filter, types);
        results.loadEntities(false);
        return results;
    }

    public void releaseEntitySet(EntitySet entities) {
        this.entitySets.remove(entities);
    }

    public ChangeQueue getChangeQueue(Class[] componentTypes) {
        ChangeQueue queue = new ChangeQueue(this, componentTypes);
        addEntityComponentListener(queue.getListener());
        return queue;
    }

    public void releaseChangeQueue(ChangeQueue queue) {
        removeEntityComponentListener(queue.getListener());
    }

    protected void entityChange(EntityChange change) {
        for (EntityComponentListener l : this.entityListeners) {
            l.componentChange(change);
        }

        for (EntitySet set : this.entitySets) {
            set.entityChange(change);
        }
    }

    private class MemReporter implements Reporter {
        private MemReporter() {
        }

        public void printReport(String type, PrintWriter out) {
            out.println("EntityData->EntitySets:" + AbstractEntityData.this.entitySets.size());
        }
    }
}