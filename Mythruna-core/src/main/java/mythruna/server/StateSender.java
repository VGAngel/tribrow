package mythruna.server;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.network.HostedConnection;
import com.jme3.network.Message;
import com.jme3.network.Server;
import mythruna.GameTime;
import mythruna.db.*;
import mythruna.msg.EntityStateMessage;
import mythruna.msg.ResetLeafMessage;
import mythruna.msg.SetBlockMessage;
import mythruna.msg.TimeMessage;
import mythruna.sim.GameSimulation;
import mythruna.sim.Mob;
import mythruna.sim.MobChangeListener;

import java.util.ArrayList;
import java.util.List;

public class StateSender implements Runnable {

    private Server server;
    private GameTime gameTime;
    private WorldDatabase worldDb;
    private int frequency = 20;
    private int skip = 60 / this.frequency;
    private int count = 0;

    private List<Message> outboundBuffer = new ArrayList();
    private List<Message> sending = new ArrayList();
    private GameSimulation sim;

    public StateSender(Server server, GameTime gameTime, WorldDatabase worldDb, GameSimulation sim) {
        this.server = server;
        this.gameTime = gameTime;
        this.worldDb = worldDb;

        WorldObserver l = new WorldObserver();
        worldDb.addCellChangeListener(l);
        worldDb.addLeafChangeListener(l);

        this.sim = sim;
        sim.getEntityManager().addMobChangeListener(new EntityObserver());
    }

    protected final synchronized void addMessage(Message m) {
        this.outboundBuffer.add(m);
    }

    protected final synchronized List<Message> swap() {
        List temp = this.outboundBuffer;
        this.outboundBuffer = this.sending;
        this.sending = temp;

        this.outboundBuffer.clear();

        return this.sending;
    }

    public void run() {
        this.count += 1;
        if (this.count < this.skip)
            return;
        this.count = 0;

        if (!this.server.hasConnections()) {
            return;
        }
        TimeMessage t = new TimeMessage(GameSimulation.getTime(), this.gameTime.getTime());
        t.setReliable(false);

        this.server.broadcast(t);

        List<Message> toSend = swap();

        for (Message m : toSend) {
            this.server.broadcast(m);
        }
        toSend.clear();

        for (HostedConnection conn : this.server.getConnections()) {
            HostedEntityData hed = (HostedEntityData) conn.getAttribute("hostedEntityData");
            if (hed != null) {
                hed.sendChanges();
            }
        }
    }

    protected class EntityObserver
            implements MobChangeListener {
        protected EntityObserver() {
        }

        public void mobChanged(Mob entity, Vector3f newPos, Quaternion rot) {
            if ((newPos == null) || (rot == null)) {
                return;
            }

            Message m = new EntityStateMessage(GameSimulation.getTime(), entity.getType().getRawId(), entity.getId(), newPos, rot);

            StateSender.this.addMessage(m);
        }
    }

    protected class WorldObserver
            implements CellChangeListener, LeafChangeListener {
        protected WorldObserver() {
        }

        public void cellChanged(CellChangeEvent event) {
            Message m = new SetBlockMessage(GameSimulation.getTime(), event.getX(), event.getY(), event.getZ(), event.getCellType());

            StateSender.this.addMessage(m);
        }

        public void leafChanged(LeafChangeEvent event) {
            if (event.getType() != LeafChangeEvent.ChangeType.RESET) {
                return;
            }
            LeafData leaf = event.getLeaf();
            LeafInfo info = leaf.getInfo();
            Message m = new ResetLeafMessage(GameSimulation.getTime(), info.x, info.y, info.z);

            StateSender.this.addMessage(m);
        }
    }
}