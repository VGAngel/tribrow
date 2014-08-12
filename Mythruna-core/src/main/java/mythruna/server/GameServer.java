package mythruna.server;

import com.jme3.math.Vector3f;
import com.jme3.network.*;
import mythruna.*;
import mythruna.db.*;
import mythruna.db.user.DefaultUserDatabase;
import mythruna.db.user.UserDatabase;
import mythruna.es.EntityData;
import mythruna.es.EntityId;
import mythruna.es.Name;
import mythruna.es.UserId;
import mythruna.es.sql.SqlEntityData;
import mythruna.event.*;
import mythruna.event.EventListener;
import mythruna.msg.*;
import mythruna.script.ScriptManager;
import mythruna.server.event.ChatEvent;
import mythruna.server.event.ServerEvent;
import mythruna.server.event.ServerEvents;
import mythruna.server.event.ServerPlayerEvent;
import mythruna.sim.*;
import mythruna.util.LogAdapter;
import mythruna.util.ReportSystem;
import mythruna.util.Reporter;
import org.progeeks.util.log.Log;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class GameServer {

    static Log log = Log.getLog();
    static Log statsLog = Log.getLog("stats");
    static Log chatLog = Log.getLog("chat");
    public static final String KEY_VERSION = "version";
    private Server server;
    private GameSystems gameSystems;
    private World world;
    private WorldDatabase worldDb;
    private EntityData ed;
    private ServerShell shell;
    private ServerStats stats;
    private UserDatabase userDb;
    private ScheduledThreadPoolExecutor leafWriterExec;
    private LeafWriter leafWriter;
    private Scheduler scheduler;
    private long startupTime;
    private GameTime gameTime;
    private EventDispatcher eventDispatcher;
    private ScriptManager scripts;
    private Map<EntityId, HostedConnection> players = new ConcurrentHashMap<EntityId, HostedConnection>();

    public GameServer(Server server, GameSystems gameSystems, UserDatabase userDb, ServerStats stats) {
        this.gameSystems = gameSystems;
        this.world = gameSystems.getWorld();
        this.worldDb = this.world.getWorldDatabase();
        this.eventDispatcher = gameSystems.getEventDispatcher();
        this.userDb = userDb;
        this.server = server;
        this.scripts = gameSystems.getScriptManager();
        this.stats = stats;
        this.ed = gameSystems.getEntityData();

        this.scheduler = new Scheduler(5);

        this.leafWriterExec = new ScheduledThreadPoolExecutor(1);
        this.leafWriter = new LeafWriter(this.worldDb);

        BlockTypeIndex.initialize();

        Messages.initialize();

        MessageObserver messageObserver = new MessageObserver();
        server.addConnectionListener(messageObserver);
        server.addMessageListener(messageObserver, new Class[]{LoginMessage.class, CreateAccountMessage.class});

        new LoggedInMessageHandler().register(server);
        new EntityMessageDelegator().register(server);

        this.shell = new ServerShell(this, this.worldDb);

        PlayerObserver playerObserver = new PlayerObserver();
        this.eventDispatcher.addListener(ServerEvents.playerConnected, playerObserver);
        this.eventDispatcher.addListener(ServerEvents.playerDisconnected, playerObserver);

        ReportSystem.registerCacheReporter(new MemReporter());
    }

    public WorldDatabase getWorldDatabase() {
        return this.worldDb;
    }

    public World getWorld() {
        return this.world;
    }

    public UserDatabase getUserDatabase() {
        return this.userDb;
    }

    public EventDispatcher getEventDispatcher() {
        return this.eventDispatcher;
    }

    public ServerStats getStats() {
        return this.stats;
    }

    public Scheduler getScheduler() {
        return this.scheduler;
    }

    public Server getServer() {
        return this.server;
    }

    public long getStartupTime() {
        return this.startupTime;
    }

    public ServerShell getShell() {
        return this.shell;
    }

    public EntityId getPlayerId(int clientId) {
        HostedConnection conn = this.server.getConnection(clientId);
        return (EntityId) conn.getAttribute("entityId");
    }

    public int getClientId(EntityId playerId) {
        HostedConnection conn = (HostedConnection) this.players.get(playerId);
        if (conn == null)
            return -1;
        return conn.getId();
    }

    public void start() {
        System.out.println("Scheduling simulation task.");
        this.startupTime = System.currentTimeMillis();

        this.leafWriterExec.scheduleAtFixedRate(this.leafWriter, 10L, 1000L, TimeUnit.MILLISECONDS);

        statsLog.info("Initializing game systems.");
        this.scripts.setBinding("server", this);
        this.scripts.setBinding("eventDispatcher", this.eventDispatcher);

        this.gameSystems.start();
        this.gameTime = this.gameSystems.getGameTime();

        this.gameSystems.getSymbolGroups().compile();

        this.gameSystems.getSimulation().setEndLoopProcessor(new StateSender(this.server, this.gameTime, this.worldDb, this.gameSystems.getSimulation()));

        this.server.start();

        this.eventDispatcher.publishEvent(ServerEvents.serverStarted, new ServerEvent(this));

        statsLog.info("============== Server started ===================");
    }

    public void shutdown() {
        statsLog.info("Server shutting down.");

        this.eventDispatcher.publishEvent(ServerEvents.serverStopping, new ServerEvent(this));

        for (HostedConnection conn : this.server.getConnections()) {
            conn.close("Server is shutting down.");
        }

        this.gameSystems.shutdown();

        System.out.println("Shutting down simulation thread...");
        this.scheduler.shutdown();
        this.leafWriterExec.shutdown();

        this.server.close();

        this.ed.close();

        statsLog.info("Server shut down.");
    }

    protected String getName(HostedConnection conn) {
        String name = (String) conn.getAttribute("name");
        if (name == null)
            name = conn.getAddress();
        return name;
    }

    public void sendChat(String message) {
        sendChat(null, message, true);
    }

    public void sendChat(String from, String message) {
        ConsoleMessage chat = new ConsoleMessage(GameSimulation.getTime(), -1, from, message);

        this.server.broadcast(1, null, chat);

        chatLog.info(from + ":" + message);
    }

    protected void sendChat(HostedConnection source, String message, boolean showOnConsole) {
        if (source == null) {
            ConsoleMessage chat = new ConsoleMessage(GameSimulation.getTime(), -1, null, message);
            this.server.broadcast(1, null, chat);

            chatLog.info(message);
        } else {
            String name = getName(source);
            ConsoleMessage chat = new ConsoleMessage(GameSimulation.getTime(), source.getId(), name, message);

            this.server.broadcast(1, null, chat);

            chatLog.info(name + ":" + message);
        }
    }

    protected Mob getMob(HostedConnection source, boolean create) {
        Mob e = (Mob) source.getAttribute("entity");
        if (e != null) {
            return e;
        }
        EntityId playerEntity = (EntityId) source.getAttribute("entityId");
        if (playerEntity == null) {
            return null;
        }
        return getMob(source, playerEntity, create);
    }

    protected Mob getMob(HostedConnection source, EntityId playerEntity, boolean create) {
        Mob e = (Mob) source.getAttribute("entity");
        if (e != null) {
            return e;
        }
        String name = (String) source.getAttribute("name");
        System.out.println("Name:" + name);
        if (name == null) {
            return null;
        }

        e = this.gameSystems.getSimulation().getEntityManager().getMob(MobClass.PLAYER, playerEntity.getId());
        System.out.println("Retrieved entity for connection:" + source + " playerEntity:" + playerEntity + "  entity:" + e);
        source.setAttribute("entity", e);

        e.setName(name);

        Message u = new EntityListUpdateMessage(e, EntityListUpdateMessage.ADDED);
        this.server.broadcast(2, null, u);

        return e;
    }

    public static World createServerWorld(WorldInfo info, File baseDir) throws IOException {
        int seed = info.getSeed();
        System.out.println("Loading world with seed:" + seed);
        EntityData entityData;
        try {
            entityData = new SqlEntityData(new File(baseDir, "entities"), 100L);
        } catch (SQLException e) {
            throw new RuntimeException("Error initializing entity database in:" + baseDir, e);
        }

        DefaultLeafDatabase leafDb = new DefaultLeafDatabase(baseDir, seed);
        DefaultBlueprintDatabase bpDb = new DefaultBlueprintDatabase(new File(baseDir, "blueprints"));

        DefaultLeafFileLocator locator = new DefaultLeafFileLocator(baseDir);
        ColumnFactory colFactory = WorldUtils.createDefaultColumnFactory(locator, seed);
        WorldDatabase worldDb = new ColumnWorldDatabase(new RevisionedLeafDatabase(new File(baseDir, "revs"), leafDb), colFactory);

        World world = new DefaultWorld(worldDb, bpDb, entityData);

        return world;
    }

    public static void main(String[] args) throws Exception {
        Log.initialize(GameServer.class.getResource("/server-log4j.xml"));

        LogAdapter.initialize();

        File baseDir = new File("mythruna.db");
        int port = 4234;
        int seed = 0;

        for (int i = 0; i < args.length; i++) {
            if (("-p".equals(args[i])) && (i < args.length - 1)) {
                i++;
                port = Integer.parseInt(args[i]);
            } else if (("-seed".equals(args[i])) && (i < args.length - 1)) {
                i++;
                seed = Integer.parseInt(args[i]);
            } else {
                System.out.println("Unknown option:" + args[i]);
            }
        }

        WorldInfo info = WorldInfo.load(baseDir);
        if (info == null) {
            info = WorldInfo.create(baseDir, "Mythruna:" + seed, seed);
        } else {
            seed = info.getSeed();
        }

        World world = createServerWorld(info, baseDir);
        UserDatabase userDb = new DefaultUserDatabase(new File(baseDir, "users"));

        GameSystems gameSystems = new GameSystems(world);
        gameSystems.getScriptManager().addStandardScripts();
        gameSystems.getScriptManager().addScript("/mythruna/server/ServerEnvironment.groovy");
        gameSystems.getScriptManager().addScript(new File("scripts"));

        gameSystems.getDialogManager().addRoot(new File("dialog"));
        gameSystems.getDialogManager().addRoot("/dialog");
        gameSystems.getDialogManager().addStartupScript("/mythruna/script/BaseDialogEnvironment.groovy");

        Server server = Network.createServer("Mythruna", 20120627, port, port);

        server.addChannel(port + 1);
        server.addChannel(port + 2);
        server.addChannel(port + 3);

        ServerStats stats = new ServerStats(new File(baseDir, "stats.json"));

        GameServer game = new GameServer(server, gameSystems, userDb, stats);
        game.start();
        game.shell.run();

        game.shutdown();
    }

    private class MemReporter
            implements Reporter {
        private MemReporter() {
        }

        public void printReport(String type, PrintWriter out) {
            out.println("GameServer->players:" + GameServer.this.players.size());
        }
    }

    protected class EntityMessageDelegator extends AbstractMessageDelegator<HostedConnection> {
        public EntityMessageDelegator() {
            super(HostedEntityData.class, true);
        }

        protected Object getSourceDelegate(final HostedConnection source) {
            final HostedEntityData hed = (HostedEntityData)source.getAttribute("hostedEntityData");
            if (hed == null) {
                GameServer.log.warn("Received entity-related message for a connection with no HostedEntityData wrapper.");
                return null;
            }
            return hed;
        }
    }

    protected class LoggedInMessageHandler extends AbstractMessageDelegator<HostedConnection> {
        public LoggedInMessageHandler() {
            super(LoggedInMessageHandler.class, true);
        }

        protected Object getSourceDelegate(HostedConnection source) {
            PlayerData p = (PlayerData) source.getAttribute("player");
            if (p == null) {
                GameServer.log.warn("Ignoring message for unlogged in connection:" + source);
                return null;
            }

            return this;
        }

        protected void setBlock(HostedConnection source, SetBlockMessage m) {
            PlayerData p = (PlayerData) source.getAttribute("player");

            boolean nerfed = false;
            Boolean b = (Boolean) p.get("grant.nerfed");
            if (Boolean.TRUE.equals(b)) {
                nerfed = true;
            }
            boolean rejected = false;

            if (nerfed) {
                return;
            }

            EntityId playerEntity = (EntityId) source.getAttribute("entityId");
            if (playerEntity == null) {
                rejected = true;
            }
            if (rejected) {
                Vector3i loc = m.getLocation();
                int original = GameServer.this.worldDb.getCellType(loc.x, loc.y, loc.z);
                System.out.println("old value:" + original);
                m.setType(original);

                source.send(0, m);
                return;
            }

            Vector3i loc = m.getLocation();

            int i = p.increment("stats.blocksChanged");
            System.out.println(loc + " = " + m.getType() + " blocks changed:" + i + "  player:" + GameServer.this.getName(source));

            int original = GameServer.this.worldDb.getCellType(loc.x, loc.y, loc.z);

            SetBlockCommand cmd = new SetBlockCommand(m.getTime(), loc.x, loc.y, loc.z, m.getType(), original, playerEntity, new GameServer.BlockReverter(source));

            GameServer.this.gameSystems.getSimulation().addCommand(cmd);
        }

        protected void sendLeaf(HostedConnection source, GetLeafDataMessage m) {
            LeafData leaf = GameServer.this.worldDb.getLeaf(m.getX(), m.getY(), m.getZ());
            if (leaf == null) {
                throw new RuntimeException("No such leaf for:" + m);
            }

            if (!leaf.contains(m.getX(), m.getY(), m.getZ())) {
                throw new RuntimeException("Leaf does not contain point, maybe file is misnamed... message:" + m);
            }
            ReturnLeafDataMessage[] array = ReturnLeafDataMessage.createMessages(leaf);
            for (ReturnLeafDataMessage msg : array) {
                source.send(0, msg);
            }
        }

        protected void console(HostedConnection source, ConsoleMessage m) {
            String cmd = m.getMessage();

            if (cmd.startsWith("say ")) {
                PlayerConnectionContext ctx = (PlayerConnectionContext) source.getAttribute("context");
                String s = cmd.substring("say ".length());
                ChatEvent event = new ChatEvent(ctx, s);
                GameServer.this.eventDispatcher.publishEvent(ServerEvents.playerChatted, event);

                GameServer.this.sendChat(source, event.getMessage(), true);
            } else {
                HostedConnectionShell shell = (HostedConnectionShell) source.getAttribute("shell");
                if (shell != null) {
                    GameServer.statsLog.info(source + " executing:" + cmd);
                    shell.execute(cmd);
                } else {
                    System.out.println(source + " tried to run a command with a null shell.");
                }
            }
        }

        protected void updateUserState(HostedConnection source, UserStateMessage m) {
            Mob e = GameServer.this.getMob(source, true);
            if (e == null) {
                return;
            }
            GameServer.this.gameSystems.getSimulation().addCommand(new ChangeMobCommand(m.getTime(), e, m.getLocation(), m.getFacing()));
        }

        protected void runAction(HostedConnection source, RunActionMessage m) {
            synchronized (GameServer.this.gameSystems.getActionManager()) {
                PlayerConnectionContext context = (PlayerConnectionContext) source.getAttribute("context");
                if (m.getTarget() != null)
                    GameServer.this.gameSystems.getActionManager().execute(m.getActionId(), context, m.getTarget(), m.getActionParameter());
                else
                    GameServer.this.gameSystems.getActionManager().execute(m.getActionId(), context, m.getActionParameter());
            }
        }

        protected void runNamedAction(HostedConnection source, RunNamedActionMessage m) {
            String name = m.getName();
            if (name == null) {
                name = GameServer.this.gameSystems.getSymbolGroups().getString("entityActions", m.getNameId());
            }

            synchronized (GameServer.this.gameSystems.getActionManager()) {
                PlayerConnectionContext context = (PlayerConnectionContext) source.getAttribute("context");
                GameServer.this.gameSystems.getActionManager().execute(name, context, m.getTarget(), m.getActionParameter());
            }
        }

        protected void getBlueprint(HostedConnection source, GetBlueprintMessage m) {
            BlueprintData bpData = GameServer.this.world.getBlueprint(m.getBlueprintId());
            source.send(new BlueprintDataMessage(bpData).setReliable(true));
        }
    }

    protected class BlockReverter implements EventListener<CellEvent> {
        private HostedConnection conn;

        public BlockReverter(HostedConnection conn) {
            this.conn = conn;
        }

        public void newEvent(EventType<CellEvent> type, CellEvent event) {
            GameServer.log.info("Need to revert to:" + event + "  for conn:" + this.conn);

            Vector3i pos = event.getCell();
            GameServer.this.gameSystems.getSimulation();
            SetBlockMessage m = new SetBlockMessage(GameSimulation.getTime(), pos.x, pos.y, pos.z, event.getNewType());

            m.setReliable(true);
            this.conn.send(0, m);
        }
    }

    private class MessageObserver
            implements MessageListener<HostedConnection>, ConnectionListener {
        private MessageObserver() {
        }

        public void connectionAdded(Server server, HostedConnection conn) {
            GameServer.statsLog.info("connectionAdded:" + conn);
            GameServer.statsLog.info(server.getConnections().size() + " connections.");

            GameServer.this.eventDispatcher.publishEvent(ServerEvents.newConnection, new ServerEvent(conn));
        }

        public void connectionRemoved(Server server, HostedConnection conn) {
            try {
                String name = GameServer.this.getName(conn);
                if (name != null) {
                    GameServer.this.sendChat(null, name + " has left.", true);
                }
                PlayerData p = (PlayerData) conn.getAttribute("player");

                EntityId playerEntity = (EntityId) conn.getAttribute("entityId");
                if (playerEntity != null) {
                    System.out.println("Cleaning up player entity state:" + playerEntity);
                    if (GameServer.this.players.remove(playerEntity) == null) {
                        GameServer.log.warn("Connection not found for playerEntity:" + playerEntity);
                    }
                }
                Mob e = (Mob) conn.getAttribute("entity");
                if (e != null) {
                    System.out.println("entity:" + e);
                    if (p != null) {
                        long t = GameSimulation.getTime();
                        FrameTransition ft = e.getFrame(t);

                        Vector3f pos = ft.getPosition(t);

                        System.out.println("Saving user:" + name + " location:" + pos);

                        p.setLocation("characterInfo.lastLocation", pos);
                        long stop = System.currentTimeMillis();
                        p.set("stats.lastDisconnect", Long.valueOf(stop));

                        Long start = p.getLong("stats.lastLogin");
                        if (start != null) {
                            long total = stop - start.longValue();
                            p.set("stats.lastDuration", Long.valueOf(total));
                            p.addValue("stats.totalDuration", total);
                        }
                        p.save();
                    }

                    GameServer.this.gameSystems.getSimulation().getEntityManager().remove(e);

                    Message u = new EntityListUpdateMessage(e, EntityListUpdateMessage.REMOVED);
                    server.broadcast(u);

                    PlayerConnectionContext context = (PlayerConnectionContext) conn.getAttribute("context");
                    GameServer.this.eventDispatcher.publishEvent(PlayerEvents.playerLeft, new PlayerEvent(context));

                    GameServer.this.eventDispatcher.publishEvent(ServerEvents.playerDisconnected, new ServerPlayerEvent(context));
                }

                PlayerPermissions perms = (PlayerPermissions) conn.getAttribute("perms");
                if (perms != null) {
                    perms.release();
                }
                HostedEntityData hed = (HostedEntityData) conn.getAttribute("hostedEntityData");
                if (hed != null) {
                    hed.close();
                }
                GameServer.statsLog.info(server.getConnections().size() + " connections remaining.");
            } catch (RuntimeException e) {
                GameServer.log.error("Error closing out connection:" + conn, e);
            }
        }

        protected void createAccount(HostedConnection source, CreateAccountMessage m) {
            String userId = m.getUserId();

            PlayerData p = GameServer.this.userDb.getUser(userId);
            if (p != null) {
                source.send(new AccountStatusMessage(userId, false, "Choose another user ID."));
                return;
            }

            String password = m.getPassword();
            String email = m.getEmail();
            String name = m.getName();

            p = GameServer.this.userDb.createUser(userId, password);
            p.set("characterInfo.name", name);
            p.set("userInfo.email", email);
            p.save();

            source.send(new AccountStatusMessage(userId, true, "Account created for:" + userId));
        }

        protected void login(HostedConnection source, LoginMessage m) {
            System.out.println("login(" + m + ")");

            String userId = m.getUserId();
            String password = m.getPassword();
            PlayerData p = GameServer.this.userDb.getUser(m.getUserId());
            System.out.println("player:" + p);

            if ((p == null) || (password == null) || (!password.equals(p.get("userInfo.password")))) {
                System.out.println("Login failed for:" + p);
                source.send(new LoginStatusMessage(false, "Invalid user ID or password.", null, new Vector3f()));
                return;
            }

            source.setAttribute("player", p);
            String name = (String) p.get("characterInfo.name");
            source.setAttribute("name", name);
            source.setAttribute("version", m.getBuildVersion());

            p.set("stats.lastLogin", Long.valueOf(System.currentTimeMillis()));
            p.increment("stats.timesLoggedIn");
            p.set("stats.lastAddress", source.getAddress());
            p.save();

            Vector3f lastLoc = p.getLocation("characterInfo.lastLocation");

            Long characterEntity = p.getLong("characterInfo.entityId");
            EntityId playerEntity;
            if (characterEntity == null) {
                playerEntity = GameServer.this.world.getEntityData().createEntity();
                System.out.println("Created new player character entity ID:" + playerEntity);
                p.set("characterInfo.entityId", Long.valueOf(playerEntity.getId()));
            } else {
                playerEntity = new EntityId(characterEntity.longValue());
            }

            GameServer.this.ed.setComponent(playerEntity, new UserId(userId));

            GameServer.this.ed.setComponent(playerEntity, new Name(name));

            PlayerPermissions perms = new DefaultPlayerPermissions(playerEntity, GameServer.this.ed);
            source.setAttribute("perms", perms);
            HostedEntityData hed = new HostedEntityData(playerEntity, perms, source, GameServer.this.world);

            GameServer.this.players.put(playerEntity, source);
            source.setAttribute("hostedEntityData", hed);
            source.setAttribute("entityId", playerEntity);

            Mob player = GameServer.this.getMob(source, playerEntity, true);

            if (lastLoc == null) {
                lastLoc = player.getPosition(GameSimulation.getTime());
            }

            source.send(new LoginStatusMessage(true, null, playerEntity, lastLoc));

            GameServer.this.sendChat(null, name + " has joined.", true);

            for (Mob e : GameServer.this.gameSystems.getSimulation().getEntityManager().mobs(MobClass.PLAYER)) {
                Message u = new EntityListUpdateMessage(e, EntityListUpdateMessage.ADDED);

                source.send(2, u);

                long t = GameSimulation.getTime();
                FrameTransition ft = e.getFrame(t);
                if (ft != null) {
                    Message s = new EntityStateMessage(t, e, ft.getPosition(t, true), ft.getRotation(t, true));
                    s.setReliable(true);
                    source.send(2, s);
                }
            }

            source.setAttribute("shell", new HostedConnectionShell(GameServer.this, source));
            PlayerConnectionContext context = new PlayerConnectionContext(GameServer.this, GameServer.this.gameSystems, source);
            source.setAttribute("context", context);

            GameServer.this.eventDispatcher.publishEvent(ServerEvents.playerConnected, new ServerPlayerEvent(context));

            GameServer.this.eventDispatcher.publishEvent(PlayerEvents.playerJoined, new PlayerEvent(context));
        }

        public void messageReceived(HostedConnection source, Message m) {
            if ((m instanceof LoginMessage))
                login(source, (LoginMessage) m);
            else if ((m instanceof CreateAccountMessage))
                createAccount(source, (CreateAccountMessage) m);
        }
    }

    private class PlayerObserver implements EventListener<ServerPlayerEvent> {
        private PlayerObserver() {
        }

        public void newEvent(EventType<ServerPlayerEvent> type, ServerPlayerEvent event) {
            PlayerConnectionContext context = (PlayerConnectionContext) event.getContext();

            System.out.println("player event:" + type + "  event:" + event);
            System.out.println("connection:" + context.getConnection());

            PlayerData player = (PlayerData) context.getConnection().getAttribute("player");
            if (player == null) {
                GameServer.log.warn("No player for event:" + event);
                return;
            }

            HashMap<String, Object> m = new HashMap<String, Object>();
            String name = (String) player.get("characterInfo.name");
            m.put("characterName", name);
            m.put("userId", player.get("id"));
            if (type.equals(ServerEvents.playerConnected)) {
                m.put("type", "Login");
                m.put("time", player.get("stats.lastLogin"));
            } else {
                m.put("type", "Logoff");
                m.put("time", player.get("stats.lastDisconnect"));
            }
            GameServer.this.stats.add("recentActivity", m, 20);

            if (type.equals(ServerEvents.playerDisconnected)) {
                List l = GameServer.this.stats.getList("topModders");
                int count;
                if (l.isEmpty()) {
                    System.out.println("Regenerating top modders stats list.");
                    l.clear();

                    Comparator<Integer> reverse = new Comparator<Integer>() {
                        @Override
                        public int compare(Integer i1, Integer i2) {
                            return i1.compareTo(i2) * -1;
                        }
                    };
                    TreeMap<Integer, PlayerData> modders = new TreeMap<Integer, PlayerData>(reverse);
                    for (String id : GameServer.this.userDb.getUserIds()) {
                        PlayerData p = GameServer.this.userDb.getUser(id);
                        Integer changed = (Integer) p.get("stats.blocksChanged");
                        if (changed != null) {
                            modders.put(changed, p);
                        }
                    }
                    count = 0;
                    for (PlayerData playerData : modders.values()) {
                        Map modder = new HashMap();
                        modder.put("userId", playerData.get("id"));
                        modder.put("characterName", playerData.get("characterInfo.name"));
                        modder.put("blocksChanged", playerData.get("stats.blocksChanged"));
                        l.add(modder);
                        count++;
                        if (count >= 10)
                            break;
                    }
                } else {
                    Integer changed = (Integer) player.get("stats.blocksChanged");
                    if (changed != null) {
                        for (Iterator i = l.iterator(); i.hasNext(); ) {
                            Map entry = (Map) i.next();
                            if (name.equals(entry.get("characterName"))) {
                                l.remove(entry);

                                break;
                            }
                        }

                        int index = 0;
                        for (Object changedStats : l) {
                            Map changedStatsMap = (Map) changedStats;
                            Integer c = (Integer) changedStatsMap.get("blocksChanged");
                            if (changed.compareTo(c) > 0) {
                                break;
                            }
                            index++;
                        }
                        if (index < 10) {
                            Map<String, Object> modder = new HashMap<String, Object>();
                            modder.put("userId", player.get("id"));
                            modder.put("characterName", name);
                            modder.put("blocksChanged", changed);
                            GameServer.this.stats.add("topModders", index, modder, 10, true);
                        }
                    }
                }

                l = GameServer.this.stats.getList("topLoggers");
                if (l.isEmpty()) {
                    System.out.println("Regenerating top loggers stats list.");
                    l.clear();

                    Comparator<Integer> reverse = new Comparator<Integer>() {
                        public int compare(Integer i1, Integer i2) {
                            return i1.compareTo(i2) * -1;
                        }
                    };
                    TreeMap<Integer, PlayerData> modders = new TreeMap<Integer, PlayerData>(reverse);
                    for (String id : GameServer.this.userDb.getUserIds()) {
                        PlayerData p = GameServer.this.userDb.getUser(id);
                        Integer changed = (Integer) p.get("stats.totalDuration");
                        if (changed != null) {
                            modders.put(changed, p);
                        }
                    }
                    count = 0;
                    for (PlayerData p : modders.values()) {
                        Map modder = new HashMap();
                        modder.put("userId", p.get("id"));
                        modder.put("characterName", p.get("characterInfo.name"));
                        modder.put("totalDuration", p.get("stats.totalDuration"));
                        l.add(modder);
                        count++;
                        if (count >= 10)
                            break;
                    }
                } else {
                    Long online = player.getLong("stats.totalDuration");
                    if (online != null) {
                        for (Iterator i = l.iterator(); i.hasNext(); ) {
                            Map entry = (Map) i.next();
                            if (name.equals(entry.get("characterName"))) {
                                l.remove(entry);

                                break;
                            }
                        }

                        int index = 0;
                        for (Object entry : l) {
                            Map entryMap = (Map) entry;
                            Number time = (Number) entryMap.get("totalDuration");
                            long timeVal = time.longValue();
                            if (online.compareTo(Long.valueOf(timeVal)) > 0)
                                break;
                            index++;
                        }
                        if (index < 10) {
                            Map<String, Serializable> modder = new HashMap<String, Serializable>();
                            modder.put("characterName", name);
                            modder.put("totalDuration", online);
                            GameServer.this.stats.add("topLoggers", index, modder, 10, true);
                        }
                    }
                }
            }

            GameServer.this.stats.save();
        }
    }
}