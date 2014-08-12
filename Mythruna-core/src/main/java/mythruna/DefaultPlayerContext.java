package mythruna;

import mythruna.es.EntityId;
import mythruna.script.ActionManager;
import mythruna.script.ActionParameter;
import mythruna.shell.Console;
import org.progeeks.tool.console.Shell;

import java.util.Map;

public class DefaultPlayerContext implements PlayerContext {

    private GameSystems systems;
    private EntityId player;
    private PlayerData playerData;
    private Console console;
    private Shell shell;
    private PlayerPermissions perms;
    private Map<String, Object> session;
    private ActionManager actions;

    public DefaultPlayerContext(GameSystems systems, EntityId player, PlayerData data, Console console, Shell shell, PlayerPermissions perms) {
        if (systems == null)
            throw new IllegalArgumentException("GameSystems cannot be null.");
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null.");
        }

        if (console == null)
            throw new IllegalArgumentException("Console cannot be null.");
        if (shell == null)
            throw new IllegalArgumentException("Shell cannot be null.");
        if (perms == null) {
            throw new IllegalArgumentException("PlayerPermissions cannot be null.");
        }
        this.systems = systems;
        this.player = player;
        this.playerData = data;
        this.console = console;
        this.shell = shell;
        this.perms = new DefaultPlayerPermissions(player, systems.getEntityData());
        this.actions = systems.getActionManager();

        console.setPlayerContext(this);
    }

    public GameSystems getSystems() {
        return this.systems;
    }

    public EntityId getPlayer() {
        return this.player;
    }

    public PlayerData getPlayerData() {
        return this.playerData;
    }

    public Console getConsole() {
        return this.console;
    }

    public Shell getShell() {
        return this.shell;
    }

    public PlayerPermissions getPerms() {
        return this.perms;
    }

    public Map<String, Object> getSessionData() {
        return this.session;
    }

    public void echo(String s) {
        this.console.echo(s);
    }

    public void runEntityAction(String action) {
        this.actions.execute(action, this, this.player, null);
    }

    public void runEntityAction(String action, ActionParameter target) {
        this.actions.execute(action, this, this.player, target);
    }
}