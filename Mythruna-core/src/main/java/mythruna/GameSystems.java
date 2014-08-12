package mythruna;

import mythruna.es.*;
import mythruna.event.EventDispatcher;
import mythruna.phys.CollisionSystem;
import mythruna.phys.PhysicsSystem;
import mythruna.phys.proto.ProtoPhysicsSystem;
import mythruna.script.*;
import mythruna.sim.GameSimulation;
import mythruna.util.NamedThreadFactory;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class GameSystems {

    public static final boolean useProtoPhysics = false;
    private ScheduledThreadPoolExecutor physExec;
    private ScheduledThreadPoolExecutor timePersister;
    private ProtoPhysicsSystem phys;
    private long physTime = 16L;
    private CollisionSystem collisions;
    private EntityData ed;
    private World world;
    private GameTime gameTime;
    private EntityId worldEntity;
    private WorldAge lastAge;
    private SymbolGroups symbolGroups;
    private ScriptManager scriptManager;
    private ActionManager actionManager;
    private DialogManager dialogManager;
    private EventDispatcher eventDispatcher;
    private ObjectTemplates objectTemplates;
    private ScheduledThreadPoolExecutor simulationExec;
    private GameSimulation simulation;
    private long simTime = 16L;
    private PhysicsSystem physics;

    public GameSystems(World world) {
        this.world = world;
        this.ed = world.getEntityData();

        this.gameTime = new GameTime();
        this.gameTime.setTimeScale(60.0D);
        this.gameTime.setTime(21600.0D);

        this.timePersister = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("TimePersist"));

        this.symbolGroups = new SymbolGroups(this.ed, false);

        this.scriptManager = new ScriptManager(new Object[0]);
        this.actionManager = new ActionManager();
        this.objectTemplates = new ObjectTemplates();
        this.dialogManager = new DialogManager(null, new Object[0]);
        this.eventDispatcher = EventDispatcher.getInstance();

        this.scriptManager.setBinding("world", world);
        this.scriptManager.setBinding("worldDb", world.getWorldDatabase());
        this.scriptManager.setBinding("entities", this.ed);
        this.scriptManager.setBinding("actions", this.actionManager);
        this.scriptManager.setBinding("eventDispatcher", this.eventDispatcher);
        this.scriptManager.setBinding("symbolGroups", this.symbolGroups);
        this.scriptManager.setBinding("dialogs", this.dialogManager);
        this.scriptManager.setBinding("objectTemplates", this.objectTemplates);

        this.dialogManager.setBinding("world", world);
        this.dialogManager.setBinding("entities", this.ed);
        this.dialogManager.setBinding("actions", this.actionManager);
        this.dialogManager.setBinding("dialogs", this.dialogManager);
        this.dialogManager.setBinding("symbolGroups", this.symbolGroups);

        this.collisions = new CollisionSystem(world, this.ed);
        this.physics = new PhysicsSystem(world, this.ed);

        this.simulationExec = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("Simulation"));
        this.simulation = new GameSimulation(this);
    }

    public World getWorld() {
        return this.world;
    }

    public EntityData getEntityData() {
        return this.ed;
    }

    public GameTime getGameTime() {
        return this.gameTime;
    }

    public ScriptManager getScriptManager() {
        return this.scriptManager;
    }

    public DialogManager getDialogManager() {
        return this.dialogManager;
    }

    public ActionManager getActionManager() {
        return this.actionManager;
    }

    public ObjectTemplates getObjectTemplates() {
        return this.objectTemplates;
    }

    public EventDispatcher getEventDispatcher() {
        return this.eventDispatcher;
    }

    public CollisionSystem getCollisions() {
        return this.collisions;
    }

    public SymbolGroups getSymbolGroups() {
        return this.symbolGroups;
    }

    public GameSimulation getSimulation() {
        return this.simulation;
    }

    protected void loadWorldEntity() {
        EntitySet es = this.ed.getEntities(new Class[]{WorldAge.class});
        try {
            es.applyChanges();
            if (es.isEmpty()) {
                System.out.println("Creating world entity...");
                this.worldEntity = this.ed.createEntity();
                this.lastAge = new WorldAge((long)this.gameTime.getTime());
                this.ed.setComponent(this.worldEntity, this.lastAge);

                System.out.println("Created world age:" + this.lastAge);
            } else {
                Entity e = (Entity) es.iterator().next();
                this.worldEntity = e.getId();
                this.lastAge = ((WorldAge) e.get(WorldAge.class));

                System.out.println("Loaded world age:" + this.lastAge);
            }
        } finally {
            es.release();
        }
    }

    protected void checkDbVersion() {
        if (this.worldEntity == null) {
            throw new IllegalStateException("World Entity has not been initialized.");
        }
        WorldInfo info = (WorldInfo) this.ed.getComponent(this.worldEntity, WorldInfo.class);
        System.out.println("Info:" + info);

        int entityDbVersion = info == null ? 0 : info.getEntityDbVersion();
        if (entityDbVersion == 1)
            return;
        EntitySet es;
        if (entityDbVersion < 1) {
            System.out.println("Upgrading entity database from version:" + entityDbVersion);

            es = this.ed.getEntities(new Class[]{Position.class});
            try {
                es.applyChanges();

                int count = es.size();
                int index = 0;
                int lastPercent = 0;
                System.out.println("Checking " + count + " positioned entities for repair.");

                for (Entity e : es) {
                    Position p = (Position) e.get(Position.class);
                    long columnId = Coordinates.worldToColumnId(p.getLocation());
                    if (columnId != p.getColumnId()) {
                        System.out.println("Repairing entity:" + e);
                        p = new Position(p.getLocation(), p.getRotation());
                        e.set(p);
                    }

                    index++;
                    int percent = index * 100 / count;
                    if (percent != lastPercent) {
                        System.out.println("Progress:" + percent + "%");
                        lastPercent = percent;
                    }
                }

                entityDbVersion = 1;
            } finally {
                es.release();
            }

        }

        info = new WorldInfo(entityDbVersion);
        this.ed.setComponent(this.worldEntity, info);
    }

    public void start() {
        loadWorldEntity();
        if (this.lastAge != null) {
            this.gameTime.setTime(this.lastAge.getSeconds());
        }
        checkDbVersion();

        this.collisions.start();

        this.timePersister.scheduleAtFixedRate(new AgeSaver(), 100L, 500L, TimeUnit.MILLISECONDS);

        this.scriptManager.initialize();
        this.dialogManager.initialize();

        this.simulationExec.scheduleAtFixedRate(this.simulation, 10L, this.simTime, TimeUnit.MILLISECONDS);
    }

    public void shutdown() {
        System.out.println("******** Shutting down game systems **********");
        this.simulationExec.shutdown();

        this.collisions.shutdown();

        this.timePersister.shutdown();
        System.out.println("******** Game systems shut down **********");
    }

    public static class WorldInfo
            implements EntityComponent, PersistentComponent {
        public static final int CURRENT_DB_VERSION = 1;
        private int entityDbVersion;

        public WorldInfo() {
        }

        public WorldInfo(int entityDbVersion) {
            this.entityDbVersion = entityDbVersion;
        }

        public Class<WorldInfo> getType() {
            return WorldInfo.class;
        }

        public int getEntityDbVersion() {
            return this.entityDbVersion;
        }

        public String toString() {
            return "WorldInfo[ entityDbVersion=" + this.entityDbVersion + "]";
        }
    }

    public static class WorldAge
            implements EntityComponent, PersistentComponent {
        private long seconds;

        public WorldAge() {
        }

        public WorldAge(long seconds) {
            this.seconds = seconds;
        }

        public Class<WorldAge> getType() {
            return WorldAge.class;
        }

        public long getSeconds() {
            return this.seconds;
        }

        public String toString() {
            return "WorldAge[" + this.seconds + "]";
        }
    }

    private class AgeSaver implements Runnable {
        private AgeSaver() {
        }

        public void run() {
            double d = GameSystems.this.gameTime.getTime();
            long l = (long) d;

            if ((GameSystems.this.lastAge == null) || (GameSystems.this.lastAge.getSeconds() != l)) {
                GameSystems.this.lastAge = new GameSystems.WorldAge(l);
                GameSystems.this.ed.setComponent(GameSystems.this.worldEntity, GameSystems.this.lastAge);
            }
        }
    }
}