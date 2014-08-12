package mythruna.client;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import mythruna.BlockTypeIndex;
import mythruna.GameTime;
import mythruna.World;
import mythruna.client.shell.DefaultConsole;
import mythruna.es.EntityData;
import mythruna.phys.MaskStrategy;
import mythruna.shell.Console;
import mythruna.sim.MobManager;

public abstract class AbstractGameClient implements GameClient {

    protected static final long FRAME_DELAY = 150L;
    protected World world;
    protected CameraTask cameraTask;
    protected GameTime gameTime;
    protected DefaultConsole console;
    protected MobManager entities;
    protected PlayerState playerState;

    protected AbstractGameClient() {
    }

    public void initialize() {
        TimeLog.log(getClass().getSimpleName(), "Initializing block types.");
        Progress.get("GameClient").setMessage("Initializing Block Packs...");

        BlockTypeIndex.initialize();

        TimeLog.log(getClass().getSimpleName(), "Creating player state");
        this.playerState = new PlayerState(this);
    }

    protected void setWorld(World world) {
        this.world = world;

        this.cameraTask = new CameraTask.WalkCameraTask(world, this);

        PhysicsThread.instance.addTask(this.cameraTask);

        this.entities = new MobManager(world);
    }

    protected void setGameTime(GameTime gameTime) {
        this.gameTime = gameTime;
    }

    public double getGameTime() {
        return this.gameTime.getTime();
    }

    public GameTime getTimeProvider() {
        return this.gameTime;
    }

    public Console getConsole() {
        return this.console;
    }

    public void echo(String s) {
        this.console.echo(s);
    }

    public MobManager getMobs() {
        return this.entities;
    }

    public long getRawTime() {
        return getTime(GameClient.TimeType.RAW);
    }

    protected abstract long toSimTime(long paramLong);

    public long getTime(GameClient.TimeType type) {
        long time = System.currentTimeMillis();
        long simTime = toSimTime(time);

        switch (type.ordinal())
        {
            case 1:
                return simTime - 150L;
            case 2:
                return simTime;
            case 3:
                return time;
        }
        return -1L;
    }

    public boolean isHeadInWater() {
        return this.cameraTask.isHeadInWater();
    }

    public PlayerState getPlayerState() {
        return this.playerState;
    }

    public void setLocation(float x, float y, float z) {
        this.cameraTask.setWorldPosition(x, y, z);
    }

    public Vector3f getLocation() {
        return this.cameraTask.getWorldPosition(getTime(GameClient.TimeType.RENDER));
    }

    protected Vector3f getLocation(long time) {
        return this.cameraTask.getWorldPosition(time);
    }

    public Quaternion getFacing() {
        return this.cameraTask.getFacing(getTime(GameClient.TimeType.RENDER));
    }

    public Vector3f getVelocity() {
        return this.cameraTask.getVelocity(getTime(GameClient.TimeType.RENDER));
    }

    public void setFacing(Quaternion facing) {
        this.cameraTask.setDirection(facing);
    }

    public void setMoveState(byte flags) {
        this.cameraTask.setMoveState(flags);
    }

    public void setCollisionMaskStrategy(MaskStrategy strat) {
        this.cameraTask.setCollisionMaskStrategy(strat);
    }

    public World getWorld() {
        return this.world;
    }

    public EntityData getEntityData() {
        return this.world.getEntityData();
    }
}