package mythruna.shell;

import mythruna.server.GameServer;
import org.progeeks.tool.console.AbstractShellCommand;
import org.progeeks.tool.console.ShellEnvironment;

public class SendMessageCommand extends AbstractShellCommand {

    public static final String DESCRIPTION = "Sends a message to logged in players.";
    public static final String[] HELP = {"Usage: send <message>", "Where:", "   <message> is a message that will be sent to all logged in", "             players and show up on their console log."};
    private GameServer server;

    public SendMessageCommand(GameServer server) {
        super("Sends a message to logged in players.", HELP);
        this.server = server;
    }

    public int execute(ShellEnvironment sEnv, String args) {
        this.server.sendChat("admin", args);

        return 0;
    }

    public boolean isSimple() {
        return true;
    }
}