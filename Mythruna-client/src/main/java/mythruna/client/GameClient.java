package mythruna.client;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import mythruna.GameTime;
import mythruna.PlayerPermissions;
import mythruna.World;
import mythruna.es.EntityAction;
import mythruna.es.EntityActionEnvironment;
import mythruna.es.EntityData;
import mythruna.es.EntityId;
import mythruna.phys.CollisionSystem;
import mythruna.phys.MaskStrategy;
import mythruna.script.ActionParameter;
import mythruna.script.ActionReference;
import mythruna.shell.Console;
import mythruna.sim.MobManager;

public abstract interface GameClient extends EntityActionEnvironment {
    public abstract int getId();

    public abstract EntityId getPlayer();

    public abstract void initialize();

    public abstract void start();

    public abstract void close();

    public abstract Console getConsole();

    public abstract boolean isRemote();

    public abstract boolean isLoggedIn();

    public abstract double getGameTime();

    public abstract GameTime getTimeProvider();

    public abstract long getRawTime();

    public abstract long getTime(TimeType paramTimeType);

    public abstract void setLocation(float paramFloat1, float paramFloat2, float paramFloat3);

    public abstract Vector3f getLocation();

    public abstract Vector3f getVelocity();

    public abstract Quaternion getFacing();

    public abstract boolean isHeadInWater();

    public abstract PlayerState getPlayerState();

    public abstract void setFacing(Quaternion paramQuaternion);

    public abstract void setMoveState(byte paramByte);

    public abstract void setCollisionMaskStrategy(MaskStrategy paramMaskStrategy);

    public abstract EntityData getEntityData();

    public abstract World getWorld();

    public abstract MobManager getMobs();

    public abstract PlayerPermissions getPerms();

    public abstract CollisionSystem getCollisions();

    public abstract void executeAction(EntityAction paramEntityAction, EntityId paramEntityId);

    public abstract void executeRef(ActionReference paramActionReference, ActionParameter paramActionParameter);

    public abstract void executeRef(ActionReference paramActionReference, EntityId paramEntityId, ActionParameter paramActionParameter);

    public abstract void execute(String paramString, EntityId paramEntityId, ActionParameter paramActionParameter);

    public static enum TimeType {
        RENDER, SIMULATION, RAW;
    }
}