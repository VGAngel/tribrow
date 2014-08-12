package mythruna.server;

import com.jme3.network.HostedConnection;
import mythruna.DefaultPlayerContext;
import mythruna.GameSystems;
import mythruna.PlayerData;
import mythruna.PlayerPermissions;
import mythruna.es.EntityId;
import mythruna.shell.Console;
import org.progeeks.tool.console.Shell;

public class PlayerConnectionContext extends DefaultPlayerContext {

    private GameServer server;
    private HostedConnection conn;

    public PlayerConnectionContext(GameServer server, GameSystems systems, HostedConnection conn) {
        super(systems, (EntityId) conn.getAttribute("entityId"), (PlayerData) conn.getAttribute("player"), (Console) conn.getAttribute("shell"), (Shell) conn.getAttribute("shell"), (PlayerPermissions) conn.getAttribute("perms"));

        this.server = server;
        this.conn = conn;
    }

    public Console getConsole() {
        return (Console) this.conn.getAttribute("shell");
    }

    public Shell getShell() {
        return (Shell) this.conn.getAttribute("shell");
    }

    public GameServer getServer() {
        return this.server;
    }

    public HostedConnection getConnection() {
        return this.conn;
    }
}