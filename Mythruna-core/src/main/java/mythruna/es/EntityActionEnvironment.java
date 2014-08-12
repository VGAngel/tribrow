package mythruna.es;

import mythruna.PlayerPermissions;
import mythruna.World;

public abstract interface EntityActionEnvironment {

    public abstract EntityId getPlayer();

    public abstract EntityData getEntityData();

    public abstract PlayerPermissions getPerms();

    public abstract World getWorld();

    public abstract void echo(String paramString);
}