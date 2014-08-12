package mythruna.shell;

import com.jme3.network.HostedConnection;
import mythruna.PlayerData;
import mythruna.server.GameServer;
import org.progeeks.tool.console.AbstractShellCommand;
import org.progeeks.tool.console.ShellEnvironment;

public class WhoCommand extends AbstractShellCommand {

    public static final String DESCRIPTION = "Lists the players that are currently online.";
    public static final String[] HELP = {"Usage: who", "   No additional parameters."};
    private GameServer server;
    private boolean showVersion;

    public WhoCommand(GameServer server, boolean showVersion) {
        super("Lists the players that are currently online.", HELP);
        this.server = server;
        this.showVersion = showVersion;
    }

    public int execute(ShellEnvironment sEnv, String args) {
        for (HostedConnection conn : this.server.getServer().getConnections()) {
            String name = (String) conn.getAttribute("name");
            if (name == null) {
                name = "Unknown";
            }
            PlayerData p = (PlayerData) conn.getAttribute("player");

            String line = "id:" + conn.getId() + "  name:" + name;

            if (this.server != null) {
                line = line + "  address:" + conn.getAddress();
            }

            if (this.showVersion) {
                line = line + "  version:" + conn.getAttribute("version");
            }
            if (p != null) {
                Long login = p.getLong("stats.lastLogin");
                if (login != null) {
                    long time = System.currentTimeMillis() - login.longValue();
                    line = line + "  on for:" + UptimeCommand.timeString(time, false);
                }

                Long count = p.getLong("stats.blocksChanged");
                if (count != null) {
                    line = line + "  changed blocks:" + count;
                }
            }
            sEnv.println(line);
        }
        return 0;
    }

    public boolean isSimple() {
        return true;
    }
}