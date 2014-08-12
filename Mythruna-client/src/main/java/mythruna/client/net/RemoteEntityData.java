package mythruna.client.net;

import com.jme3.network.Client;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import mythruna.es.*;
import mythruna.msg.*;
import org.progeeks.util.log.Log;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class RemoteEntityData implements ObservableEntityData {

    static Log log = Log.getLog();

    private static AtomicInteger nextSetId = new AtomicInteger();
    private static AtomicInteger nextRequestId = new AtomicInteger();
    private static AtomicInteger nextQueueId = new AtomicInteger();
    private Client client;
    private Map<Integer, RemoteEntitySet> activeSets = new ConcurrentHashMap<>();

    private Map<Integer, PendingRequest> pendingRequests = new ConcurrentHashMap<>();

    private Map<Integer, RemoteChangeQueue> activeQueues = new ConcurrentHashMap<>();

    private Map<EntityId, CacheEntry> entityCache = new ConcurrentHashMap<>();
    private ExecutorService executor;

    public RemoteEntityData(Client client) {
        this.client = client;

        this.executor = Executors.newFixedThreadPool(4);

        client.addMessageListener(new MessageObserver(), new Class[]{EntityDataMessage.class, EntityIdsMessage.class, EntityComponentsMessage.class, ComponentChangeMessage.class});
    }

    public StringIndex getStrings() {
        throw new UnsupportedOperationException("String look-ups are server-side only for now.");
    }

    public EntityId createEntity() {
        throw new UnsupportedOperationException("RemoteEntityData is read-only.");
    }

    public void removeEntity(EntityId entityId) {
        throw new UnsupportedOperationException("RemoteEntityData is read-only.");
    }

    public void setComponent(EntityId entityId, EntityComponent component) {
        throw new UnsupportedOperationException("RemoteEntityData is read-only.");
    }

    public void setComponents(EntityId entityId, EntityComponent[] components) {
        throw new UnsupportedOperationException("RemoteEntityData is read-only.");
    }

    public boolean removeComponent(EntityId entityId, Class type) {
        throw new UnsupportedOperationException("RemoteEntityData is read-only.");
    }

    public void addEntityComponentListener(EntityComponentListener l) {
        throw new UnsupportedOperationException("Unfiltered change listening not supported.");
    }

    public void removeEntityComponentListener(EntityComponentListener l) {
        throw new UnsupportedOperationException("Unfiltered change listening not supported.");
    }

    public <T extends EntityComponent> T getComponent(EntityId entityId, Class<T> type) {
        Entity e = getEntity(entityId, new Class[]{type});
        return e.get(type);
    }

    public Entity getEntity(EntityId entityId, Class[] types) {
        if (log.isDebugEnabled()) {
            log.debug("getEntity(" + entityId + ", " + Arrays.asList(types) + ")");
        }

        CacheEntry entry = (CacheEntry) this.entityCache.get(entityId);
        Entity result = null;
        if (entry != null) {
            result = entry.getEntity(types);
        }

        if ((result == null) || (!result.isComplete())) {
            int id = nextRequestId.getAndIncrement();
            GetComponentsMessage msg = new GetComponentsMessage(id, entityId, types);
            msg.setReliable(true);

            PendingEntityRequest request = new PendingEntityRequest(msg);
            this.pendingRequests.put(id, request);
            this.client.send(2, msg);
            try {
                result = (Entity) request.getResult();
            } catch (InterruptedException e) {
                throw new RuntimeException("Interrupted waiting for entity data.", e);
            }

            if (entry != null) {
                entry.addComponents(types, result.getComponents());
            } else {
                entry = new CacheEntry(types, result);
                this.entityCache.put(entityId, entry);
                resizeCache();
            }
            entry.markUsed();
        } else {
            log.debug("Using cached entity instead of requesting a new one.");
        }

        return result;
    }

    protected void resizeCache() {
        log.info("Cache size:" + this.entityCache.size());
        while (this.entityCache.size() > 20) {
            long oldest = 9223372036854775807L;
            EntityId oldestEntity = null;
            for (Map.Entry<EntityId, CacheEntry> e : this.entityCache.entrySet()) {
                if ((e.getValue()).getLastUsed() < oldest) {
                    oldestEntity = e.getKey();
                    oldest = (e.getValue()).getLastUsed();
                }
            }

            if (oldestEntity != null) {
                log.info("Removing:" + oldestEntity + " from cache.");
                this.entityCache.remove(oldestEntity);
            } else {
                log.warn("Unable to find an oldest cache entry to remove.");
            }
        }
    }

    public EntitySet getEntities(Class[] types) {
        return getEntities(null, types);
    }

    public EntityId findEntityOriginal(ComponentFilter filter) {
        int id = nextRequestId.getAndIncrement();
        FindEntityMessage msg = new FindEntityMessage(id, filter);
        msg.setReliable(true);

        PendingEntityIdRequest request = new PendingEntityIdRequest(msg);
        this.pendingRequests.put(id, request);
        this.client.send(2, msg);
        try {
            return request.getResult();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted waiting for entity data.", e);
        }
    }

    public EntityId findEntity(ComponentFilter filter, Class[] types) {
        if ((types == null) || (types.length == 0))
            return findEntityOriginal(filter);
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public Set<EntityId> findEntities(ComponentFilter filter, Class[] types) {
        int id = nextRequestId.getAndIncrement();
        FindEntitiesMessage msg = new FindEntitiesMessage(id, filter, types);
        msg.setReliable(true);

        PendingEntityIdsRequest request = new PendingEntityIdsRequest(msg);
        this.pendingRequests.put(id, request);
        this.client.send(2, msg);
        try {
            return request.getResult();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted waiting for entity data.", e);
        }
    }

    public EntitySet getEntities(ComponentFilter filter, Class[] types) {
        if (log.isTraceEnabled())
            log.trace("RemoteEntityData.getEntities( " + filter + ", " + Arrays.asList(types) + " )");
        int id = nextSetId.getAndIncrement();
        RemoteEntitySet result = new RemoteEntitySet(id, this, filter, types, this.client);

        this.activeSets.put(id, result);

        Message m = new GetEntitySetMessage(id, filter, types);
        m.setReliable(true);
        this.client.send(2, m);

        return result;
    }

    public void releaseEntitySet(EntitySet entities) {
        Message m = new ReleaseEntitySetMessage(((RemoteEntitySet) entities).setId);
        m.setReliable(true);
        this.client.send(2, m);
    }

    public ChangeQueue getChangeQueue(Class[] componentTypes) {
        int id = nextQueueId.getAndIncrement();
        RemoteChangeQueue result = new RemoteChangeQueue(id, this, componentTypes);

        this.activeQueues.put(id, result);

        Message m = new ObserveChangesMessage(id, componentTypes);
        m.setReliable(true);
        this.client.send(2, m);

        return result;
    }

    public void releaseChangeQueue(ChangeQueue queue) {
        Message m = new ReleaseObserveChangesMessage(((RemoteChangeQueue) queue).queueId);
        m.setReliable(true);
        this.client.send(2, m);
    }

    protected void componentChange(ComponentChangeMessage m) {
        if (log.isTraceEnabled()) {
            log.trace("Received component change:" + m);
        }
        EntityChange change = new EntityChange(m.getEntityId(), m.getType(), m.getComponent());

        CacheEntry entry = this.entityCache.get(m.getEntityId());
        if (entry != null) {
            EntityComponent value = m.getComponent();

            if (log.isDebugEnabled()) {
                log.debug("Updating cache entry:" + entry.id + " with:" + value);
            }
            if (value == null)
                entry.removeComponent(m.getType());
            else {
                entry.setComponent(value);
            }

        }

        for (RemoteEntitySet set : this.activeSets.values()) {
            set.entityChange(change);
        }
        for (RemoteChangeQueue queue : this.activeQueues.values()) {
            queue.getListener().componentChange(change);
        }
    }

    public void close() {
        for (PendingRequest req : this.pendingRequests.values()) {
            req.close();
        }

        this.executor.shutdownNow();
    }

    public void execute(EntityProcessor proc) {
        this.executor.submit(new EntityProcessorRunnable(proc, this));
    }

    protected class CacheEntry {
        private long lastUsed;
        private EntityId id;
        private Map<Class, EntityComponent> components = new ConcurrentHashMap<>();

        public CacheEntry(Class[] types, Entity e) {
            this.id = e.getId();
            addComponents(types, e.getComponents());
            markUsed();
        }

        public void markUsed() {
            this.lastUsed = System.currentTimeMillis();
        }

        public long getLastUsed() {
            return this.lastUsed;
        }

        public void addComponents(Class[] types, EntityComponent[] values) {
            for (int i = 0; i < types.length; i++) {
                if (values[i] == null)
                    this.components.remove(types[i]);
                else
                    this.components.put(types[i], values[i]);
            }
        }

        public Entity getEntity(Class[] types) {
            EntityComponent[] values = new EntityComponent[types.length];
            for (int i = 0; i < values.length; i++) {
                values[i] = this.components.get(types[i]);
            }

            return new DefaultEntity(this.id, values, types);
        }

        public void setComponent(EntityComponent c) {
            this.components.put(c.getType(), c);
        }

        public void removeComponent(Class type) {
            this.components.remove(type);
        }
    }

    protected class PendingEntityIdsRequest extends RemoteEntityData.PendingRequest<EntityIdsMessage, Set<EntityId>> {
        private Set<EntityId> results = new HashSet<>();

        public PendingEntityIdsRequest(FindEntitiesMessage request) {
            super(request);
        }

        public void dataReceived(EntityIdsMessage m) {
            if (RemoteEntityData.log.isDebugEnabled()) {
                RemoteEntityData.log.debug("find entities Received:" + m);
            }
            if (m.getEntityIds() != null) {
                for (long l : m.getEntityIds()) {
                    this.results.add(new EntityId(l));
                }

            }

            if (m.isLast())
                setResult(this.results);
        }
    }

    protected class PendingEntityIdRequest extends RemoteEntityData.PendingRequest<EntityComponentsMessage, EntityId> {
        public PendingEntityIdRequest(FindEntityMessage request) {
            super(request);
        }

        public void dataReceived(EntityComponentsMessage m) {
            setResult(m.getEntityId());
        }
    }

    protected class PendingEntityRequest extends RemoteEntityData.PendingRequest<EntityComponentsMessage, Entity> {
        public PendingEntityRequest(GetComponentsMessage request) {
            super(request);
        }

        public void dataReceived(EntityComponentsMessage m) {
            Entity e = new DefaultEntity(m.getEntityId(), m.getComponents(), ((GetComponentsMessage) this.request).getComponentTypes());

            setResult(e);
        }
    }

    protected abstract class PendingRequest<M, T> {
        protected Message request;
        private AtomicReference<T> result = new AtomicReference<>();
        private CountDownLatch received = new CountDownLatch(1);

        protected PendingRequest(Message request) {
            this.request = request;
        }

        public boolean isDone() {
            return this.result.get() != null;
        }

        public void close() {
            this.received.countDown();
        }

        protected void setResult(T val) {
            this.result.set(val);
            this.received.countDown();
        }

        public abstract void dataReceived(M paramM);

        public T getResult() throws InterruptedException {
            this.received.await();
            return this.result.get();
        }

        public String toString() {
            return "PendingRequest[" + this.request + "]";
        }
    }

    private class MessageObserver
            implements MessageListener<Client> {
        private MessageObserver() {
        }

        public void messageReceived(Client client, Message m) {
            if (RemoteEntityData.log.isTraceEnabled()) {
                RemoteEntityData.log.trace("Received message:" + m);
            }
            if ((m instanceof EntityDataMessage))
                entityData((EntityDataMessage) m);
            else if ((m instanceof EntityComponentsMessage))
                entityComponents((EntityComponentsMessage) m);
            else if ((m instanceof ComponentChangeMessage))
                RemoteEntityData.this.componentChange((ComponentChangeMessage) m);
            else if ((m instanceof EntityIdsMessage))
                entityIds((EntityIdsMessage) m);
        }

        protected void entityComponents(EntityComponentsMessage m) {
            RemoteEntityData.PendingRequest request = RemoteEntityData.this.pendingRequests.remove(m.getRequestId());
            if (request == null) {
                RemoteEntityData.log.error("Received component data but no request is pending, id:" + m.getRequestId());
                return;
            }

            request.dataReceived(m);
        }

        protected void entityIds(EntityIdsMessage m) {
            RemoteEntityData.PendingRequest request = RemoteEntityData.this.pendingRequests.get(m.getRequestId());
            if (request == null) {
                RemoteEntityData.log.error("Received data but no request is pending, id:" + m.getRequestId());
                return;
            }
            request.dataReceived(m);
            if (request.isDone())
                RemoteEntityData.this.pendingRequests.remove(m.getRequestId());
        }

        protected void entityData(EntityDataMessage m) {
            RemoteEntityData.RemoteEntitySet set = (RemoteEntityData.RemoteEntitySet) RemoteEntityData.this.activeSets.get(m.getSetId());

            for (EntityDataMessage.ComponentData d : m.getData()) {
                if (d.getComponents() != null) {
                    Entity e = new DefaultEntity(d.getEntityId(), d.getComponents(), set.getTypes());
                    set.directAdd(e);
                } else {
                    set.directRemove(d.getEntityId());
                }
            }
        }
    }

    protected static class RemoteEntitySet extends EntitySet {
        private Client client;
        private int setId;
        private ConcurrentLinkedQueue<Entity> directAdds = new ConcurrentLinkedQueue<>();

        private ConcurrentLinkedQueue<EntityId> directRemoves = new ConcurrentLinkedQueue<>();

        public RemoteEntitySet(int setId, EntityData ed, ComponentFilter filter, Class[] types, Client client) {
            super(ed, filter, types);
            this.setId = setId;
            this.client = client;
        }

        protected Class[] getTypes() {
            return super.getTypes();
        }

        protected void loadEntities(boolean reload) {
        }

        public boolean resetFilter(ComponentFilter filter) {
            boolean result = super.resetFilter(filter);

            Message m = new ResetEntitySetFilterMessage(this.setId, filter);
            m.setReliable(true);

            if (RemoteEntityData.log.isDebugEnabled()) {
                RemoteEntityData.log.debug("Sending filter reset:" + m);
            }
            this.client.send(2, m);

            return result;
        }

        protected boolean buildTransactionChanges(Set<EntityChange> updates) {
            boolean directMods = false;

            if (!this.directRemoves.isEmpty()) {
                while (!this.directRemoves.isEmpty()) {
                    EntityId id = (EntityId) this.directRemoves.poll();
                    debug(id, "RemoteEntityData[" + this.setId + "] direct remove:" + id);

                    this.transaction.directRemove(id);
                    directMods = true;
                }

            }

            if (!this.directAdds.isEmpty()) {
                while (!this.directAdds.isEmpty()) {
                    Entity d = (Entity) this.directAdds.poll();

                    debug(d.getId(), "RemoteEntityData[" + this.setId + "] direct add:" + d.getId());

                    this.transaction.directAdd(d);
                    directMods = true;
                }

            }

            if (super.buildTransactionChanges(updates)) {
                return true;
            }
            return directMods;
        }

        protected void entityChange(EntityChange change) {
            EntityId id = change.getEntityId();
            if (!containsId(id)) {
                boolean found = false;
                for (Entity e : this.directAdds) {
                    if (id.equals(e.getId())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    return;
                }
            } else if (this.directRemoves.contains(id)) {
                return;
            }

            super.entityChange(change);
        }

        protected void directAdd(Entity e) {
            this.directAdds.add(e);
        }

        protected void directRemove(EntityId id) {
            this.directRemoves.add(id);
        }
    }

    protected static class RemoteChangeQueue extends ChangeQueue {
        private int queueId;

        public RemoteChangeQueue(int queueId, ObservableEntityData ed, Class[] types) {
            super(ed, types);
            this.queueId = queueId;
        }

        protected EntityComponentListener getListener() {
            return super.getListener();
        }
    }
}