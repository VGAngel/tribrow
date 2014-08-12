package mythruna.server;

import com.jme3.network.HostedConnection;
import com.jme3.network.Message;
import mythruna.PlayerPermissions;
import mythruna.World;
import mythruna.es.*;
import mythruna.es.action.MoveObjectAction;
import mythruna.msg.*;
import org.progeeks.util.log.Log;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class HostedEntityData implements EntityActionEnvironment {

    public static final String ATTRIBUTE = "hostedEntityData";
    static Log log = Log.getLog();

    private AtomicBoolean closing = new AtomicBoolean(false);
    private EntityId playerId;
    private HostedConnection conn;
    private World world;
    private ObservableEntityData ed;
    private PlayerPermissions perms;
    private Map<Integer, EntitySet> activeSets = new ConcurrentHashMap();

    private Map<Integer, ChangeQueue> activeQueues = new ConcurrentHashMap();

    private Set<EntityChange> changeBuffer = new HashSet();
    private List<EntityDataMessage.ComponentData> dataBuffer = new ArrayList();
    private int maxPerMessage = 20;

    public HostedEntityData(EntityId playerId, PlayerPermissions perms, HostedConnection conn, World world) {
        this.playerId = playerId;
        this.conn = conn;
        this.world = world;
        this.ed = ((ObservableEntityData) world.getEntityData());
        this.perms = perms;
    }

    public void close() {
        this.closing.set(true);

        for (Map.Entry e : this.activeSets.entrySet()) {
            try {
                EntitySet set = (EntitySet) e.getValue();
                log.trace("close(): Releasing set for ID:" + e.getKey());

                set.release();
            } catch (RuntimeException ex) {
                log.error("Error releasing entity set", ex);
            }
        }
        this.activeSets.clear();

        for (ChangeQueue queue : this.activeQueues.values()) {
            try {
                queue.release();
            } catch (RuntimeException ex) {
                log.error("Error releasing change queue", ex);
            }
        }
        this.activeQueues.clear();
    }

    public EntityId getPlayer() {
        return this.playerId;
    }

    public EntityData getEntityData() {
        return this.ed;
    }

    public PlayerPermissions getPerms() {
        return this.perms;
    }

    public void echo(String s) {
        HostedConnectionShell shell = (HostedConnectionShell) this.conn.getAttribute("shell");
        shell.echo(s);
    }

    public World getWorld() {
        return this.world;
    }

    protected void getEntitySet(HostedConnection source, GetEntitySetMessage m) {
        int setId = m.getSetId();

        EntitySet set = (EntitySet) this.activeSets.get(Integer.valueOf(setId));
        if (set != null) {
            throw new RuntimeException("Set already exists for ID:" + setId);
        }
        if (log.isTraceEnabled()) {
            log.trace("Creating set for ID:" + m.getSetId());
        }
        set = this.ed.getEntities(m.getFilter(), m.getComponentTypes());
        this.activeSets.put(Integer.valueOf(setId), set);

        if (log.isTraceEnabled())
            log.trace("entity set:" + set);
        List data = new ArrayList();
        for (Entity e : set) {
            data.add(new EntityDataMessage.ComponentData(e));
            if (data.size() > 20) {
                source.send(2, new EntityDataMessage(setId, data).setReliable(true));
                data.clear();
            }
        }

        if (!data.isEmpty()) {
            source.send(2, new EntityDataMessage(setId, data).setReliable(true));
            data.clear();
        }
    }

    protected void sendData(int setId, List<EntityDataMessage.ComponentData> data) {
        if (log.isTraceEnabled()) {
            log.trace("sendData(" + setId + ", size:" + data.size() + ")");
        }
        for (int i = 0; i < data.size(); i += this.maxPerMessage) {
            int start = i;
            int end = Math.min(i + this.maxPerMessage, data.size());

            this.conn.send(2, new EntityDataMessage(setId, data.subList(start, end)).setReliable(true));
        }
    }

    protected void getComponents(HostedConnection source, GetComponentsMessage m) {
        Entity e = this.ed.getEntity(m.getEntityId(), m.getComponentTypes());

        source.send(2, new EntityComponentsMessage(m.getRequestId(), e).setReliable(true));
    }

    protected void findEntity(HostedConnection source, FindEntityMessage m) {
        EntityId id = this.ed.findEntity(m.getFilter(), new Class[0]);
        source.send(2, new EntityComponentsMessage(m.getRequestId(), id).setReliable(true));
    }

    protected void findEntities(HostedConnection source, FindEntitiesMessage m) {
        Set results = this.ed.findEntities(m.getFilter(), m.getTypes());
        if (results.isEmpty()) {
            log.trace("findEntities() sending empty message.");

            source.send(2, new EntityIdsMessage(m.getRequestId()));
            return;
        }

        int maxSize = 1000;
        int parts = (int) Math.ceil(results.size() / maxSize);
        int part = 1;

        if (log.isTraceEnabled()) {
            log.trace("findEntities()  parts:" + parts);
        }
        while (!results.isEmpty()) {
            int count = Math.min(maxSize, results.size());
            long[] ids = new long[count];
            int index = 0;
            for (Iterator it = results.iterator(); it.hasNext(); ) {
                EntityId id = (EntityId) it.next();
                ids[(index++)] = id.getId();
                it.remove();
            }

            if (log.isTraceEnabled()) {
                log.trace("findEntities()  sending part:" + part + "/" + parts);
            }
            source.send(2, new EntityIdsMessage(m.getRequestId(), ids, part, parts));

            part++;
        }
    }

    protected void entityAction(HostedConnection source, EntityActionMessage m) {
        executeAction(m.getAction(), m.getTarget());
    }

    protected void resetEntitySetFilter(HostedConnection source, ResetEntitySetFilterMessage m) {
        if (log.isTraceEnabled()) {
            log.trace("Reset entity set filter:" + m);
        }
        EntitySet set = (EntitySet) this.activeSets.get(Integer.valueOf(m.getSetId()));
        set.resetFilter(m.getFilter());
    }

    protected void releaseEntitySet(HostedConnection source, ReleaseEntitySetMessage m) {
        if (log.isTraceEnabled())
            log.trace("Release entity set:" + m);
        EntitySet set = (EntitySet) this.activeSets.remove(Integer.valueOf(m.getSetId()));
        set.release();
    }

    protected void observeChanges(HostedConnection source, ObserveChangesMessage m) {
        int queueId = m.getQueueId();

        ChangeQueue queue = (ChangeQueue) this.activeQueues.get(Integer.valueOf(queueId));
        if (queue != null) {
            throw new RuntimeException("Queue already exists for ID:" + queueId);
        }
        if (log.isTraceEnabled()) {
            log.trace("Creating change queue for ID:" + queueId);
        }
        queue = this.ed.getChangeQueue(m.getComponentTypes());
        this.activeQueues.put(Integer.valueOf(queueId), queue);
    }

    protected void releaseObserveChanges(HostedConnection source, ReleaseObserveChangesMessage m) {
        int queueId = m.getQueueId();

        ChangeQueue queue = (ChangeQueue) this.activeQueues.remove(Integer.valueOf(queueId));
        if (queue == null)
            throw new RuntimeException("No releasble queue for id:" + queueId);
        queue.release();
    }

    public void messageReceived(HostedConnection source, Message m) {
        if (log.isTraceEnabled()) {
            log.trace("Received message:" + m + "  for:" + source);
        }
        throw new RuntimeException("I don't think this is used anymore since the message delegator routes directly.");
    }

    public void executeAction(EntityAction action, EntityId target) {
        if ((log.isInfoEnabled()) && (!(action instanceof MoveObjectAction)))
            log.info("Executing action:" + action);
        else if (log.isDebugEnabled())
            log.debug("Executing action:" + action);
        action.runAction(this, target);
    }

    public void sendChanges() {
        if (this.closing.get()) {
            return;
        }

        this.changeBuffer.clear();

        for (Map.Entry e : this.activeSets.entrySet()) {
            EntitySet set = (EntitySet) e.getValue();

            if (set.applyChanges(this.changeBuffer)) {
                this.dataBuffer.clear();

                for (Entity entity : set.getRemovedEntities()) {
                    if (log.isTraceEnabled()) {
                        log.trace("Entity removed from set[" + e.getKey() + "]:" + entity);
                    }
                    this.dataBuffer.add(new EntityDataMessage.ComponentData(entity.getId(), (EntityComponent[]) null));
                }

                for (Entity entity : set.getAddedEntities()) {
                    if (log.isTraceEnabled()) {
                        log.trace("Entity added to set[" + e.getKey() + "]:" + entity);
                    }
                    this.dataBuffer.add(new EntityDataMessage.ComponentData(entity));
                }

                sendData(((Integer) e.getKey()).intValue(), this.dataBuffer);

                set.clearChangeSets();
            }
        }
        if (!this.changeBuffer.isEmpty()) {
            if (log.isTraceEnabled()) {
                log.trace("Change buffer:" + this.changeBuffer);
            }
            for (EntityChange c : this.changeBuffer) {
                this.conn.send(2, new ComponentChangeMessage(c).setReliable(true));
            }

        }

        for (ChangeQueue queue : this.activeQueues.values()) {
            EntityChange c = null;
            while ((c = (EntityChange) queue.poll()) != null) {
                if (this.changeBuffer.add(c))
                    this.conn.send(2, new ComponentChangeMessage(c).setReliable(true));
            }
        }
    }
}