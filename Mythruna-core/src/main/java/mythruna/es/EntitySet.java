package mythruna.es;

import org.progeeks.util.log.Log;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class EntitySet extends AbstractSet<Entity> {

    static Log log = Log.getLog();

    private HashMap<EntityId, Entity> entities = new HashMap();

    private ConcurrentLinkedQueue<EntityChange> changes = new ConcurrentLinkedQueue();
    private EntityData ed;
    private ComponentFilter mainFilter;
    private ComponentFilter[] filters;
    private Class[] types;
    private boolean filtersChanged = false;

    protected Transaction transaction = new Transaction();
    private Set<Entity> addedEntities = new HashSet();
    private Set<Entity> changedEntities = new HashSet();
    private Set<Entity> removedEntities = new HashSet();

    private boolean released = false;

    public boolean debugOn = false;

    public EntitySet(EntityData ed, ComponentFilter filter, Class[] types) {
        this.ed = ed;
        this.types = types;
        setMainFilter(filter);
    }

    protected Class[] getTypes() {
        return this.types;
    }

    public String debugId() {
        return "EntitySet@" + System.identityHashCode(this);
    }

    protected void debug(String debug) {
    }

    protected void debug(EntityId id, String debug) {
    }

    protected void debug(Set<Entity> context, String debug) {
    }

    protected void setMainFilter(ComponentFilter filter) {
        this.mainFilter = filter;

        if (filter != null) {
            this.filters = new ComponentFilter[this.types.length];
            for (int i = 0; i < this.types.length; i++) {
                if (filter.getComponentType() == this.types[i])
                    this.filters[i] = filter;
            }
        } else {
            this.filters = null;
        }
    }

    protected ComponentFilter getMainFilter() {
        return this.mainFilter;
    }

    protected void loadEntities(boolean reload) {
        Set<EntityId> idSet = this.ed.findEntities(this.mainFilter, this.types);
        if (idSet.isEmpty()) {
            return;
        }

        EntityComponent[] buffer = new EntityComponent[this.types.length];
        for (EntityId id : idSet) {
            if ((!reload) || (!containsId(id))) {
                for (int i = 0; i < buffer.length; i++) {
                    buffer[i] = this.ed.getComponent(id, this.types[i]);
                }

                DefaultEntity e = new DefaultEntity(id, (EntityComponent[]) buffer.clone(), this.types);
                if ((add(e)) && (reload))
                    this.addedEntities.add(e);
            }
        }
    }

    protected void purgeEntities() {
        for (Iterator it = iterator(); it.hasNext(); ) {
            Entity e = (Entity) it.next();

            if (!entityMatches(e)) {
                it.remove();
                this.removedEntities.add(e);
            }
        }
    }

    public boolean resetFilter(ComponentFilter filter) {
        setMainFilter(filter);

        this.filtersChanged = true;

        return false;
    }

    public boolean containsId(EntityId id) {
        return this.entities.containsKey(id);
    }

    public Entity getEntity(EntityId id) {
        return (Entity) this.entities.get(id);
    }

    public boolean equals(Object o) {
        return o == this;
    }

    public int size() {
        return this.entities.size();
    }

    public Iterator<Entity> iterator() {
        return new EntityIterator();
    }

    public void clear() {
        this.entities.clear();
    }

    public boolean add(Entity e) {
        return this.entities.put(e.getId(), e) == null;
    }

    protected Entity remove(EntityId id) {
        return (Entity) this.entities.remove(id);
    }

    public boolean remove(Object e) {
        if (!(e instanceof Entity))
            return false;
        return this.entities.remove(((Entity) e).getId()) != null;
    }

    public boolean contains(Object e) {
        if (!(e instanceof Entity))
            return false;
        return this.entities.containsKey(((Entity) e).getId());
    }

    public Set<Entity> getAddedEntities() {
        return this.addedEntities;
    }

    public Set<Entity> getChangedEntities() {
        return this.changedEntities;
    }

    public Set<Entity> getRemovedEntities() {
        return this.removedEntities;
    }

    public void clearChangeSets() {
        this.addedEntities.clear();
        this.changedEntities.clear();
        this.removedEntities.clear();
    }

    public boolean hasChanges() {
        return (!this.addedEntities.isEmpty()) || (!this.changedEntities.isEmpty()) || (!this.removedEntities.isEmpty());
    }

    public boolean applyChanges() {
        return applyChanges(null);
    }

    public boolean applyChanges(Set<EntityChange> updates) {
        return applyChanges(updates, true);
    }

    protected boolean buildTransactionChanges(Set<EntityChange> updates) {
        if (this.changes.isEmpty()) {
            return false;
        }
        EntityChange change = null;
        while ((change = (EntityChange) this.changes.poll()) != null) {
            this.transaction.addChange(change, updates);
        }
        return true;
    }

    protected void filterUpdates(Set<EntityChange> updates) {
        if (updates == null) ;
    }

    public boolean hasFilterChanged() {
        return this.filtersChanged;
    }

    protected boolean applyChanges(Set<EntityChange> updates, boolean clearChangeSets) {
        if (clearChangeSets) {
            clearChangeSets();
        }
        if (this.released) {
            this.changes.clear();

            this.removedEntities.addAll(this);
            clear();
            return hasChanges();
        }

        debug(" changes:" + this.changes);

        if (buildTransactionChanges(updates)) {
            this.transaction.resolveChanges();
            debug(this, " .end----------------------");
            debug(this.addedEntities, " .addedEntities:" + this.addedEntities);
            debug(this.changedEntities, " .changedEntities:" + this.changedEntities);
            debug(this.removedEntities, " .removedEntities:" + this.removedEntities);
        }

        if (this.filtersChanged) {
            this.filtersChanged = false;

            purgeEntities();

            loadEntities(true);
        }

        filterUpdates(updates);

        return (!this.addedEntities.isEmpty()) || (!this.changedEntities.isEmpty()) || (!this.removedEntities.isEmpty());
    }

    public void release() {
        this.ed.releaseEntitySet(this);

        this.released = true;
    }

    protected boolean entityMatches(Entity e) {
        EntityComponent[] array = e.getComponents();
        for (int i = 0; i < this.types.length; i++) {
            if (array[i] == null) {
                return false;
            }
            if ((this.filters != null) && (this.filters[i] != null)) {
                if (!this.filters[i].evaluate(array[i]))
                    return false;
            }
        }
        return true;
    }

    protected boolean isMatchingComponent(EntityComponent c) {
        for (int i = 0; i < this.types.length; i++) {
            if (c.getType() == this.types[i]) {
                if ((this.filters != null) && (this.filters[i] != null)) {
                    return this.filters[i].evaluate(c);
                }
                return true;
            }
        }
        return false;
    }

    public final boolean hasType(Class type) {
        for (Class c : this.types) {
            if (c == type)
                return true;
        }
        return false;
    }

    private int typeIndex(Class type) {
        for (int i = 0; i < this.types.length; i++) {
            if (this.types[i] == type)
                return i;
        }
        return -1;
    }

    protected boolean isRelevantChange(EntityChange change) {
        if (!hasType(change.getComponentType())) {
            if (log.isTraceEnabled()) {
                log.trace("   not our type.");
            }
            return false;
        }

        Entity e = (Entity) this.entities.get(change.getEntityId());
        if (e != null) {
            if (log.isTraceEnabled()) {
                log.trace("   We already have it, so we care.");
            }
            return true;
        }

        if (this.filters == null) {
            if (log.isTraceEnabled()) {
                log.trace("   No special filters, so we care.");
            }
            return true;
        }

        EntityComponent newValue = change.getComponent();
        if (newValue == null) {
            if (log.isTraceEnabled()) {
                log.trace("   It's a removal of a component for an entity we don't care about yet.");
            }
            return false;
        }

        if (!isMatchingComponent(newValue)) {
            return false;
        }

        if (log.isTraceEnabled()) {
            log.trace("It might be relevant.");
        }
        return true;
    }

    protected void entityChange(EntityChange change) {
        if (log.isTraceEnabled())
            log.trace("entityChange(" + change + ")");
        if (!isRelevantChange(change)) {
            return;
        }
        if (log.isTraceEnabled())
            log.trace("Adding change:" + change);
        debug(change.getEntityId(), " adding change event:" + change);

        this.changes.add(change);
    }

    protected class Transaction {
        Map<EntityId, DefaultEntity> adds = new HashMap();
        Set<EntityId> mods = new HashSet();

        protected Transaction() {
        }

        public void directRemove(EntityId id) {
            EntitySet.this.debug(id, " Transaction.directRemove(" + id + ")");

            Entity e = EntitySet.this.remove(id);
            if (e != null)
                EntitySet.this.removedEntities.add(e);
        }

        public void directAdd(Entity e) {
            EntitySet.this.debug(e.getId(), " Transaction.directAdd(" + e + ")");

            if (EntitySet.this.add(e))
                EntitySet.this.addedEntities.add(e);
        }

        public void addChange(EntityChange change, Set<EntityChange> updates) {
            EntityId id = change.getEntityId();
            EntityComponent comp = change.getComponent();
            DefaultEntity e = (DefaultEntity) EntitySet.this.entities.get(id);

            if (e == null) {
                e = (DefaultEntity) this.adds.get(id);

                if (e == null) {
                    if (comp == null) {
                        return;
                    }

                    EntitySet.this.debug(id, " Transaction.change caused add:" + change);

                    e = new DefaultEntity(id, new EntityComponent[EntitySet.this.types.length], EntitySet.this.types);
                    this.adds.put(id, e);
                }
            } else {
                EntitySet.this.debug(id, " Transaction.change caused mod:" + change);

                this.mods.add(id);
            }
            int index;
            if (comp == null)
                index = EntitySet.this.typeIndex(change.getComponentType());
            else {
                index = EntitySet.this.typeIndex(comp.getClass());
            }

            if (updates != null) {
                if ((comp == null) || (EntitySet.this.filters == null) || (EntitySet.this.filters[index] == null) || (EntitySet.this.filters[index].evaluate(comp))) {
                    updates.add(change);
                }
            }
            e.getComponents()[index] = comp;
        }

        protected boolean completeEntity(DefaultEntity e) {
            EntityComponent[] array = e.getComponents();
            for (int i = 0; i < EntitySet.this.types.length; i++) {
                boolean rechecking = false;
                if (array[i] == null) {
                    if (EntitySet.log.isDebugEnabled())
                        EntitySet.log.debug("Pulling component type:" + EntitySet.this.types[i] + " for id:" + e.getId());
                    array[i] = EntitySet.this.ed.getComponent(e.getId(), EntitySet.this.types[i]);

                    if (array[i] == null)
                        return false;
                } else {
                    rechecking = true;
                }

                if ((EntitySet.this.filters != null) && (EntitySet.this.filters[i] != null)) {
                    if (!EntitySet.this.filters[i].evaluate(array[i])) {
                        if (rechecking)
                            EntitySet.log.warn("Non-matching component:" + array[i] + " for entity:" + e);
                        return false;
                    }
                }
            }
            e.validate();
            return true;
        }

        public void resolveChanges() {
            for (DefaultEntity e : this.adds.values()) {
                if (completeEntity(e)) {
                    EntitySet.this.debug(e.getId(), " Transaction.adding:" + e);

                    if (EntitySet.this.add(e)) {
                        EntitySet.this.addedEntities.add(e);
                    }

                }

            }

            for (EntityId id : this.mods) {
                Entity e = (Entity) EntitySet.this.entities.get(id);
                ((DefaultEntity) e).validate();

                if (EntitySet.this.entityMatches(e)) {
                    EntitySet.this.debug(e.getId(), " Transaction.changed:" + e);
                    EntitySet.this.changedEntities.add(e);
                } else {
                    EntitySet.this.debug(e.getId(), " Transaction.removing:" + e);
                    if (EntitySet.this.remove(e)) {
                        EntitySet.this.removedEntities.add(e);
                    }
                }
            }

            this.adds.clear();
            this.mods.clear();
        }
    }

    private class EntityIterator
            implements Iterator<Entity> {
        private Iterator<Map.Entry<EntityId, Entity>> delegate = EntitySet.this.entities.entrySet().iterator();

        public EntityIterator() {
        }

        public boolean hasNext() {
            return this.delegate.hasNext();
        }

        public Entity next() {
            return (Entity) ((Map.Entry) this.delegate.next()).getValue();
        }

        public void remove() {
            this.delegate.remove();
        }
    }
}