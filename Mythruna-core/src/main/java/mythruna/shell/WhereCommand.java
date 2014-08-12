package mythruna.shell;

import com.jme3.math.Vector3f;
import com.jme3.network.HostedConnection;
import mythruna.server.GameServer;
import mythruna.sim.FrameTransition;
import mythruna.sim.Mob;
import org.progeeks.tool.console.AbstractShellCommand;
import org.progeeks.tool.console.ShellEnvironment;

import java.util.StringTokenizer;

public class WhereCommand extends AbstractShellCommand {

    public static final String DESCRIPTION = "Shows where players are located in the game world.";
    public static final String[] HELP = {"Usage: where [<id list>]", "Where:", "  <id list>  is an optional list of client IDs to display specific information for.", "             The list can be space and/or comma delimited.", "", "  Leaving the id off displays location information for all users."};
    private GameServer server;

    public WhereCommand(GameServer server) {
        super("Shows where players are located in the game world.", HELP);
        this.server = server;
    }

    protected String connInfo(HostedConnection conn) {
        if (conn == null) {
            return "Invalid connection.";
        }
        String name = (String) conn.getAttribute("name");
        if (name == null) {
            name = "Unknown";
        }
        String loc = "Unknown";
        Mob e = (Mob) conn.getAttribute("entity");
        if (e != null) {
            long time = System.currentTimeMillis();
            FrameTransition ft = e.getFrame(time);
            if (ft != null) {
                Vector3f pos = ft.getPosition(time);
                loc = String.format("%.1f, %.1f, %.1f", new Object[]{Float.valueOf(pos.x), Float.valueOf(pos.y), Float.valueOf(pos.z)});
            }

        }

        return "id:" + conn.getId() + "  name:" + name + "  location:" + loc;
    }

    public int execute(ShellEnvironment sEnv, String args) {
        if (args.length() > 0) {
            StringTokenizer st = new StringTokenizer(args, ", ");
            while (st.hasMoreTokens()) {
                try {
                    String t = st.nextToken();
                    int id = Integer.parseInt(t);
                    HostedConnection conn = this.server.getServer().getConnection(id);
                    sEnv.println(connInfo(conn));
                } catch (Exception e) {
                    sEnv.println(e);
                }
            }
        } else {
            for (HostedConnection conn : this.server.getServer().getConnections()) {
                sEnv.println(connInfo(conn));
            }
        }
        return 0;
    }

    public boolean isSimple() {
        return true;
    }
}