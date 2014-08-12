package mythruna.client;

import mythruna.*;
import mythruna.client.shell.DefaultConsole;
import mythruna.client.shell.ResetOptionsCommand;
import mythruna.db.*;
import mythruna.db.user.DefaultUserDatabase;
import mythruna.db.user.UserDatabase;
import mythruna.es.EntityAction;
import mythruna.es.EntityData;
import mythruna.es.EntityId;
import mythruna.es.sql.SqlEntityData;
import mythruna.event.PlayerEvent;
import mythruna.event.PlayerEvents;
import mythruna.phys.CollisionSystem;
import mythruna.script.ActionParameter;
import mythruna.script.ActionReference;
import mythruna.sim.QueuedCellAccess;

import java.io.File;

public class LocalGameClient extends AbstractGameClient {
    private EntityId playerEntity = new EntityId(-2147483648L);
    private EntityData ed;
    private GameSystems gameSystems;
    private PlayerContext playerContext;
    private PlayerPermissions perms;
    private UserDatabase userDb;
    private PlayerData playerData;
    private Progress progress;

    public LocalGameClient() {
        this.progress = Progress.get("GameClient");

        this.progress.setMax(0);
    }

    public void initialize() {
        super.initialize();

        TimeLog.log(getClass().getSimpleName(), "Creating console");
        this.console = new DefaultConsole();
        this.console.registerCommand("resetopts", new ResetOptionsCommand());

        TimeLog.log(getClass().getSimpleName(), "Creating world...");

        this.progress.setMessage("Creating World...");
        setWorld(createWorld(0));

        setLocation(512.5F, 512.5F, 80.0F);

        this.console.setLocalVariable("opts", ClientOptions.getInstance());

        TimeLog.log(getClass().getSimpleName(), "Creating player permissions.");

        this.perms = new DefaultPlayerPermissions(this.playerEntity, this.ed);

        TimeLog.log(getClass().getSimpleName(), "Creating game systems.");

        this.progress.setMessage("Creating Game Systems...");
        this.gameSystems = new GameSystems(getWorld());

        TimeLog.log(getClass().getSimpleName(), "Setting up scripts");
        this.gameSystems.getScriptManager().addStandardScripts();
        this.gameSystems.getScriptManager().addScript("/mythruna/script/ClientEnvironment.groovy");
        this.gameSystems.getScriptManager().addScript(new File("mods/scripts"));
        this.gameSystems.getScriptManager().setBinding("gameClient", this);

        this.gameSystems.getDialogManager().addRoot(new File("mods/dialog"));
        this.gameSystems.getDialogManager().addRoot("/dialog");
        this.gameSystems.getDialogManager().addStartupScript("/mythruna/script/BaseDialogEnvironment.groovy");
    }

    public GameSystems getGameSystems() {
        return this.gameSystems;
    }

    private World createWorld(int seed) {
        File baseDir = new File("mythruna.db");

        this.userDb = new DefaultUserDatabase(new File(baseDir, "users"));
        this.playerData = this.userDb.getUser("SinglePlayer");
        if (this.playerData == null) {
            this.playerData = this.userDb.createUser("SinglePlayer", "SinglePlayer");
            this.playerData.set("characterInfo.name", "SinglePlayer");

            this.playerData.save();
        }

        WorldInfo info = WorldInfo.load(baseDir);
        if (info == null) {
            info = WorldInfo.create(baseDir, "Mythruna:" + seed, seed);
        } else {
            seed = info.getSeed();
        }

        System.out.println("Loading world with seed:" + seed);

        DefaultLeafDatabase leafDb = new DefaultLeafDatabase(baseDir, seed);
        DefaultBlueprintDatabase bpDb = new DefaultBlueprintDatabase(new File(baseDir, "blueprints"));
        LeafFileLocator locator = new DefaultLeafFileLocator(baseDir);
        ColumnFactory colFactory = WorldUtils.createDefaultColumnFactory(locator, seed);
        WorldDatabase worldDb = new LocalWorldDatabase(leafDb, colFactory);
        try {
            this.ed = new SqlEntityData("mythruna.db/entities", 100L);
        } catch (Exception e) {
            throw new RuntimeException("Error accessing entity database", e);
        }

        return new DefaultWorld(worldDb, bpDb, this.ed);
    }

    public int getId() {
        return -1;
    }

    public EntityId getPlayer() {
        return this.playerEntity;
    }

    public PlayerData getPlayerData() {
        return this.playerData;
    }

    public CollisionSystem getCollisions() {
        return this.gameSystems.getCollisions();
    }

    public void start() {
        System.out.println("LocalGameClient.start...........");
        TimeLog.log(getClass().getSimpleName(), "Starting game client.");

        World world = getWorld();
        world.setCellAccess(new QueuedCellAccess(this.playerEntity, this.gameSystems.getSimulation(), world.getWorldDatabase()));

        this.playerContext = new DefaultPlayerContext(this.gameSystems, this.playerEntity, this.playerData, this.console, this.console.getShell(), this.perms);

        TimeLog.log(getClass().getSimpleName(), "Starting game systems...");

        this.progress.setMessage("Starting Game Systems...");
        this.gameSystems.start();
        setGameTime(this.gameSystems.getGameTime());

        TimeLog.log(getClass().getSimpleName(), "Starting animation thread.");

        this.cameraTask.initialize();
        PhysicsThread.instance.start();

        TimeLog.log(getClass().getSimpleName(), "Publishing player joined event.");
        this.gameSystems.getEventDispatcher().publishEvent(PlayerEvents.playerJoined, new PlayerEvent(this.playerContext));

        TimeLog.log(getClass().getSimpleName(), "Game client started.");
    }

    public void close() {
        this.gameSystems.getEventDispatcher().publishEvent(PlayerEvents.playerLeft, new PlayerEvent(this.playerContext));

        PhysicsThread.instance.removeTask(this.cameraTask);

        this.gameSystems.shutdown();

        this.ed.close();
        this.world.getWorldDatabase().close();
    }

    public boolean isRemote() {
        return false;
    }

    public boolean isLoggedIn() {
        return true;
    }

    protected long toSimTime(long raw) {
        return raw;
    }

    public PlayerPermissions getPerms() {
        return this.perms;
    }

    public void executeAction(EntityAction action, EntityId target) {
        action.runAction(this, target);
    }

    public void executeRef(ActionReference ref, ActionParameter parm) {
        this.gameSystems.getActionManager().execute(ref.getId(), this.playerContext, parm);
    }

    public void executeRef(ActionReference ref, EntityId target, ActionParameter parm) {
        this.gameSystems.getActionManager().execute(ref.getId(), this.playerContext, target, parm);
    }

    public void execute(String action, EntityId source, ActionParameter target) {
        this.gameSystems.getActionManager().execute(action, this.playerContext, source, target);
    }
}