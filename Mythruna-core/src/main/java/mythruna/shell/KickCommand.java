package mythruna.shell;

import com.jme3.network.HostedConnection;
import mythruna.server.GameServer;
import org.progeeks.tool.console.AbstractShellCommand;
import org.progeeks.tool.console.ShellEnvironment;

public class KickCommand extends AbstractShellCommand {

    public static final String DESCRIPTION = "Kicks a player off of the server.";
    public static final String[] HELP = {"Usage: kick <id> [<message>]", "Where:", "  <id>  is the ID of the client to kick.", "  <message> is an optional message to include when disconnecting them."};
    private GameServer server;

    public KickCommand(GameServer server) {
        super("Kicks a player off of the server.", HELP);
        this.server = server;
    }

    public int execute(ShellEnvironment sEnv, String args) {
        if (args.length() == 0) {
            sEnv.println("No ID or message specified.");
            return -1;
        }

        String sId = args.trim();
        String message = "Server has forcefully disconnected you.";
        int split = sId.indexOf(" ");
        if (split > 0) {
            message = sId.substring(split).trim();
            sId = sId.substring(0, split);
        }

        int id = 0;
        try {
            id = Integer.parseInt(sId);
        } catch (Exception e) {
            sEnv.println(e);
            return -1;
        }

        HostedConnection conn = this.server.getServer().getConnection(id);
        if (conn == null) {
            sEnv.println("Error: client ID was not valid:" + id);
            return -1;
        }

        String name = (String) conn.getAttribute("name");
        if (name == null)
            name = "Unknown";
        String info = "id:" + conn.getId() + "  name:" + name + "  address:" + conn.getAddress();

        sEnv.println("Forcefully disconnecting " + info);
        conn.close(message);

        return 0;
    }

    public boolean isSimple() {
        return true;
    }
}