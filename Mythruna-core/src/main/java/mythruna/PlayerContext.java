package mythruna;

import mythruna.es.EntityId;
import mythruna.script.ActionParameter;
import mythruna.shell.Console;
import org.progeeks.tool.console.Shell;

import java.util.Map;

public abstract interface PlayerContext {

    public abstract GameSystems getSystems();

    public abstract EntityId getPlayer();

    public abstract PlayerData getPlayerData();

    public abstract Console getConsole();

    public abstract Shell getShell();

    public abstract PlayerPermissions getPerms();

    public abstract Map<String, Object> getSessionData();

    public abstract void echo(String paramString);

    public abstract void runEntityAction(String paramString);

    public abstract void runEntityAction(String paramString, ActionParameter paramActionParameter);
}