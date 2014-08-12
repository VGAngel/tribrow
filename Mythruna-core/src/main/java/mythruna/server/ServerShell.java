package mythruna.server;

import mythruna.db.WorldDatabase;
import mythruna.shell.*;
import org.progeeks.tool.console.PrintCommand;
import org.progeeks.tool.console.Shell;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ServerShell extends Shell {

    private GameServer server;
    private WorldDatabase workdDb;
    private List<ConsoleListener> listeners = new ArrayList();

    public ServerShell(GameServer server, WorldDatabase workdDb) {
        super("mythruna");

        registerCommand("print", new PrintCommand());

        registerCommand("who", new WhoCommand(server, true));
        registerCommand("where", new WhereCommand(server));
        registerCommand("mem", new MemoryCommand());
        registerCommand("send", new SendMessageCommand(server));
        registerCommand("gc", new GcCommand());
        registerCommand("kick", new KickCommand(server));
        registerCommand("uptime", new UptimeCommand(server, false));
        registerCommand("threads", new ShowThreadsCommand());
        registerCommand("copy", new CopyCommand(server));
        registerCommand("report", new ReportCommand());
        registerCommand("grant", new GrantCommand(server));
    }

    public void chatEcho(String from, String message) {
        if (from != null)
            echo("chat-" + from + ":" + message);
        else
            echo("chat:" + message);
    }

    public void addConsoleListener(ConsoleListener l) {
        this.listeners.add(l);
    }

    public void removeConsoleListener(ConsoleListener l) {
        this.listeners.remove(l);
    }

    protected void notifyListeners(Object s) {
        if (!this.listeners.isEmpty()) {
            for (ConsoleListener c : this.listeners)
                c.echo(s);
        }
    }

    public void echo(Object s) {
        super.echo(s);
        notifyListeners(s);
    }

    public void error(Object s) {
        super.error(s);
        notifyListeners("ERROR: " + s);
    }

    public void commandFinished() {
        super.commandFinished();
        notifyListeners(": " + getLastStatus());
    }

    public void stop() {
        super.stop();
        try {
            System.in.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing shell", e);
        }
    }
}