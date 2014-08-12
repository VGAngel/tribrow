package mythruna.server;

import com.jme3.network.HostedConnection;
import mythruna.PlayerContext;
import mythruna.msg.ConsoleMessage;
import mythruna.shell.*;
import mythruna.sim.GameSimulation;
import org.progeeks.tool.console.PrintCommand;
import org.progeeks.tool.console.Shell;

public class HostedConnectionShell extends Shell implements Console {

    private GameServer server;
    private HostedConnection conn;

    public HostedConnectionShell(GameServer server, HostedConnection conn) {
        super("", new PlayerShellCommandProcessor(null));

        this.conn = conn;

        getShellEnvironment().getVariables().put("connection", conn);
        getShellEnvironment().getVariables().put("console", this);
        getShellEnvironment().getVariables().put("worldDb", server.getWorldDatabase());

        registerCommand("print", new PrintCommand());

        registerCommand("who", new WhoCommand(server, false));
        registerCommand("where", new WhereCommand(server));
        registerCommand("serverMem", new MemoryCommand());
        registerCommand("uptime", new UptimeCommand(server, true));
    }

    public void setPlayerContext(PlayerContext context) {
        ((PlayerShellCommandProcessor) getShellEnvironment()).setPlayerContext(context);
    }

    public void echo(Object s) {
        System.out.println("Sending to client:" + this.conn + "  message:" + s);
        ConsoleMessage m = new ConsoleMessage(GameSimulation.getTime(), -1, null, String.valueOf(s));
        this.conn.send(1, m);
    }

    public void runCommand(String cmd) {
        execute(cmd);
    }
}