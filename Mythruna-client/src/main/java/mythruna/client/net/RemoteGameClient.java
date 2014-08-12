package mythruna.client.net;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.network.*;
import mythruna.DefaultPlayerPermissions;
import mythruna.DefaultWorld;
import mythruna.GameTime;
import mythruna.PlayerPermissions;
import mythruna.client.*;
import mythruna.es.EntityAction;
import mythruna.es.EntityId;
import mythruna.msg.*;
import mythruna.phys.CollisionSystem;
import mythruna.script.ActionParameter;
import mythruna.script.ActionReference;
import mythruna.script.SymbolGroups;
import mythruna.server.AbstractMessageDelegator;
import mythruna.sim.Mob;
import org.progeeks.util.log.Log;

import javax.swing.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class RemoteGameClient extends AbstractGameClient {

    static Log log = Log.getLog();
    private Client client;
    private String userName;
    private StateOutputThread stateUpdate;
    private long serverTimeOffset = 0L;
    private volatile boolean loggedIn = false;
    private CommandStatusListener commandStatusListener;
    private EntityId playerEntity = new EntityId(-2147483648L);
    private PlayerPermissions perms;
    private SymbolGroups symbolGroups;
    private CollisionSystem collisions;
    private Progress progress;

    public RemoteGameClient(LoginClient loginClient) {
        this.client = loginClient.getClient();
        System.out.println("______RemoteGameClient(" + this.client + ")");

        this.progress = Progress.get("GameClient");

        this.progress.setMax(0);

        this.playerEntity = loginClient.getPlayer();

        setWorld(new DefaultWorld(new RemoteWorldDatabase(this, this.client), new RemoteBlueprintDatabase(this.client), new RemoteEntityData(this.client)));

        this.client.addMessageListener(new TimeListener(), new Class[]{TimeMessage.class});

        Vector3f loc = loginClient.getLocation();
        setLocation(loc.x, loc.y, loc.z);
        Quaternion q = loginClient.getFacing();
        if (q != null) {
            setFacing(q);
        }

        EntityUpdateHandler updateHandler = new EntityUpdateHandler();
        this.client.addMessageListener(updateHandler, new Class[]{EntityStateMessage.class, WarpPlayerMessage.class, EntityListUpdateMessage.class});

        ClientStateObserver obs = new ClientStateObserver();
        this.client.addClientStateListener(obs);
        this.client.addErrorListener(obs);

        GameTime gt = new GameTime();
        gt.setTimeScale(60.0D);
        gt.setTime(21600.0D);
        setGameTime(gt);

        loginClient.transferState(updateHandler);

        loginClient.release();

        this.loggedIn = true;
    }

    public void initialize() {
        super.initialize();

        this.progress.setMessage("Creating Game Systems...");

        this.symbolGroups = new SymbolGroups(getEntityData(), true);

        this.stateUpdate = new StateOutputThread();

        this.console = new RemoteConsole(this.client);
        this.collisions = new CollisionSystem(getWorld(), getEntityData());

        this.collisions.start();

        this.perms = new DefaultPlayerPermissions(this.playerEntity, getEntityData());
    }

    public int getId() {
        return this.client.getId();
    }

    public EntityId getPlayer() {
        if (!isLoggedIn()) {
            throw new RuntimeException("Player is not logged in.");
        }
        return this.playerEntity;
    }

    public CollisionSystem getCollisions() {
        return this.collisions;
    }

    public void start() {
        this.progress.setMessage("Starting Game Systems...");

        this.cameraTask.initialize();

        PhysicsThread.instance.start();

        this.stateUpdate.start();
    }

    public void close() {
        System.out.println("****** RemoteGameClient.close()");

        PhysicsThread.instance.removeTask(this.cameraTask);

        this.collisions.shutdown();
        this.stateUpdate.close();
        this.world.close();
        this.client.close();
    }

    public boolean isConnected() {
        return this.client.isConnected();
    }

    public boolean isRemote() {
        return true;
    }

    public boolean isLoggedIn() {
        return this.loggedIn;
    }

    protected long toSimTime(long raw) {
        return raw - this.serverTimeOffset;
    }

    public PlayerPermissions getPerms() {
        return this.perms;
    }

    public void executeAction(EntityAction action, EntityId target) {
        this.client.send(new EntityActionMessage(action, target));
    }

    public void executeRef(ActionReference ref, ActionParameter parm) {
        this.client.send(new RunActionMessage(ref.getId(), parm));
    }

    public void executeRef(ActionReference ref, EntityId target, ActionParameter parm) {
        this.client.send(new RunActionMessage(ref.getId(), target, parm));
    }

    public void execute(String action, EntityId source, ActionParameter target) {
        int nameId = this.symbolGroups.getId("entityActions", action);
        if (nameId >= 0) {
            this.client.send(new RunNamedActionMessage((short) nameId, source, target));
        } else {
            this.client.send(new RunNamedActionMessage(action, source, target));
        }
    }

    protected class StateOutputThread extends Thread {
        private long targetFrameLength = 50000000L;
        private long lastTime = -1L;

        private AtomicBoolean go = new AtomicBoolean(true);

        public StateOutputThread() {
            super();
            setDaemon(true);
        }

        public void close() {
            this.go.set(false);
            try {
                join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        protected void doStateSend(long delta) {
            long rawTime = RemoteGameClient.this.getRawTime();
            Message m = new UserStateMessage(rawTime, RemoteGameClient.this.getLocation(rawTime), RemoteGameClient.this.cameraTask.getMoveState());

            m.setReliable(false);

            long start = System.nanoTime();
            RemoteGameClient.this.client.send(m);
            long end = System.nanoTime();
            long deltaTime = end - start;
            long timeWeCareAbout = 1000000L;
            if (deltaTime > timeWeCareAbout)
                System.out.println("state sent in:" + deltaTime / 1000000.0D + " ms.");
        }

        public void run() {
            while (this.go.get()) {
                long time = System.nanoTime();
                long delta = time - this.lastTime;
                if (delta > this.targetFrameLength) {
                    try {
                        doStateSend(delta);
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                        throw e;
                    }
                    this.lastTime = time;
                } else {
                    try {
                        Thread.sleep(1L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    protected class EntityUpdateHandler extends AbstractMessageDelegator<Client> {
        public EntityUpdateHandler() {
            super(EntityUpdateHandler.class, true);
        }

        protected Object getSourceDelegate(Client source) {
            return this;
        }

        public void updateEntityList(Client client, EntityListUpdateMessage msg) {
            if (RemoteGameClient.log.isTraceEnabled())
                RemoteGameClient.log.trace("Received:" + msg);
            Mob e = RemoteGameClient.this.entities.getMob(msg.getType(), msg.getId());
            if ((msg.getChangeType() == EntityListUpdateMessage.ADDED) || (msg.getChangeType() == EntityListUpdateMessage.CHANGED)) {
                e.setName(msg.getName());
            } else if (msg.getChangeType() == EntityListUpdateMessage.REMOVED) {
                RemoteGameClient.this.entities.remove(e);
            }
        }

        public void updateEntityState(Client client, EntityStateMessage msg) {
            Mob e = RemoteGameClient.this.entities.getMob(msg.getType(), msg.getId());
            if (e == null) {
                System.out.println("Unknown entity for:" + msg);
                return;
            }

            RemoteGameClient.this.entities.changeMob(msg.getTime(), e, msg.getFacing(), msg.getLocation());
        }

        public void warpPlayer(Client client, WarpPlayerMessage msg) {
            System.out.println("WarpPlayer to:" + msg.getLocation());

            Vector3f loc = msg.getLocation();
            RemoteGameClient.this.setLocation(loc.x, loc.y, loc.z);
            RemoteGameClient.this.setFacing(msg.getFacing());
        }
    }

    protected class TimeListener
            implements MessageListener<Client> {
        private long lastRawTime = -1L;
        private long lastTime = -1L;

        protected TimeListener() {
        }

        public void messageReceived(Client client, Message m) {
            TimeMessage msg = (TimeMessage) m;

            if ((this.lastTime > 0L) && (msg.getTime() < this.lastTime)) {
                System.out.println("Discarding out of order message:" + msg);
                return;
            }

            long serverTime = msg.getTime();
            long raw = RemoteGameClient.this.getRawTime();

            if (raw == this.lastRawTime) {
                System.out.println("Ignoring time update because local time has not moved.");
                return;
            }
            this.lastRawTime = raw;

            long newDelta = raw - serverTime;

            if (Math.abs(RemoteGameClient.this.serverTimeOffset - newDelta) > 16L) {
                System.out.println("Adjusting local time by:" + (newDelta - RemoteGameClient.this.serverTimeOffset) + " ms");
                RemoteGameClient.this.serverTimeOffset = newDelta;
                RemoteGameClient.this.gameTime.setTime(msg.getGameTime());
            }

            this.lastTime = msg.getTime();
        }
    }

    protected class LagTester
            implements MessageListener<Client> {
        protected LagTester() {
        }

        public void messageReceived(Client client, Message m) {
            if (m.isReliable()) {
                long sleepTime = 500L;
                if ((m instanceof ReturnLeafDataMessage)) {
                    sleepTime = 0L;
                } else {
                    System.out.println("Got message:" + m);
                }
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected class ClientStateObserver
            implements ClientStateListener, ErrorListener<Client> {
        private boolean erroredOut = false;

        protected ClientStateObserver() {
        }

        public void clientConnected(Client c) {
        }

        public void handleError(Client source, Throwable t) {
            System.out.println("****************************************");
            System.out.println("**** handle network Error(" + t + ")");
            t.printStackTrace();
            System.out.println("****************************************");

            if (this.erroredOut) {
                System.exit(-1);
                return;
            }
            this.erroredOut = true;

            ErrorHandler.handle(t, false);

            String message = "Unknown network error:" + t.getMessage();
            JOptionPane.showMessageDialog(null, message, "Disconnected", 0);
        }

        public void clientDisconnected(Client c, ClientStateListener.DisconnectInfo info) {
            System.out.println("****************************************");
            System.out.println("**** disconnected:" + (info == null ? "null" : info.reason));
            System.out.println("****************************************");

            if (info != null) {
                JOptionPane.showMessageDialog(null, info.reason, "Disconnected", 0);
            }
        }
    }
}