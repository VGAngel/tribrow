package mythruna.shell;

import com.jme3.network.HostedConnection;
import mythruna.PlayerData;
import mythruna.server.GameServer;
import org.progeeks.tool.console.AbstractShellCommand;
import org.progeeks.tool.console.ShellEnvironment;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GrantCommand extends AbstractShellCommand {

    public static final String DESCRIPTION = "Grants or ungrants a specific privilege to a player.";
    public static final String[] HELP = {"Usage: grant <id>:<\"user name\"> [+/-grant]", "Where:", "  <id>  is the ID of the client to grant the permission to.", "  or \"user name\" is the user name to grant the permission to.", "  [+/-grant] is the name of the privilege to grant or ungrant as the", "             + or - indicates.  + is considered the default.", "", "  If no grant option is specified then the current grants are ", "  listed for the player."};
    private GameServer server;

    public GrantCommand(GameServer server) {
        super("Grants or ungrants a specific privilege to a player.", HELP);
        this.server = server;
    }

    public int execute(ShellEnvironment sEnv, String args) {
        if (args.length() == 0) {
            sEnv.println("No ID or user specified.");
            return -1;
        }

        Pattern p = Pattern.compile("(?:(\\d+)|\"(.*)\"|([^ ]+))(?: ([+\\-]?.+))?");

        Matcher m = p.matcher(args);
        if (!m.matches()) {
            sEnv.println("Invalid args:" + args);
            return -1;
        }

        PlayerData player = null;

        if (m.group(1) != null) {
            int id = Integer.parseInt(m.group(1));
            HostedConnection conn = this.server.getServer().getConnection(id);
            if (conn == null) {
                sEnv.println("Connection not found for:" + id);
                return -1;
            }

            player = (PlayerData) conn.getAttribute("player");
            if (player == null) {
                sEnv.println("Player is not fully connected for id:" + id);
                return -1;
            }
        } else if (m.group(2) != null) {
            String uid = m.group(2);
            player = this.server.getUserDatabase().getUser(uid);
            if (player == null) {
                sEnv.println("Player not found for user id:" + uid);
                return -1;
            }
        } else if (m.group(3) != null) {
            String uid = m.group(3);
            player = this.server.getUserDatabase().getUser(uid);
            if (player == null) {
                sEnv.println("Player not found for user id:" + uid);
                return -1;
            }
        } else {
            sEnv.println("Could not determine user or ID in args:" + args);
            return -1;
        }

        if (m.group(4) != null) {
            String grant = m.group(4);
            if (grant.charAt(0) == '-') {
                grant = grant.substring(1);
                sEnv.println("Removing grant:" + grant);
                player.set("grant." + grant, null);
            } else {
                if (grant.charAt(0) == '+')
                    grant = grant.substring(1);
                sEnv.println("Adding grant:" + grant);
                player.set("grant." + grant, Boolean.valueOf(true));
            }
        }

        sEnv.println("Grants:" + player.get("grant"));

        return 0;
    }

    public boolean isSimple() {
        return true;
    }
}