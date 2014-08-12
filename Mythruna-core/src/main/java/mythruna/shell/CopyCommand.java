package mythruna.shell;

import mythruna.Vector3i;
import mythruna.db.LeafData;
import mythruna.db.WorldDatabase;
import mythruna.server.GameServer;
import org.progeeks.tool.console.AbstractShellCommand;
import org.progeeks.tool.console.ShellEnvironment;

import java.util.StringTokenizer;

public class CopyCommand extends AbstractShellCommand {

    public static final String DESCRIPTION = "Copies an object or data.";
    public static final String[] HELP = {"Usage: copy <type> <parameters>", "Where:", "   <type> is currently just leaf.", "", "Parameters:", " leaf: <loc1> <loc2>", "       where location is in x,y,z form and the x,y,z elements are integers."};
    private GameServer server;

    public CopyCommand(GameServer server) {
        super("Copies an object or data.", HELP);
        this.server = server;
    }

    protected Vector3i toCoordinate(ShellEnvironment sEnv, String s) {
        try {
            Vector3i result = new Vector3i();

            StringTokenizer st = new StringTokenizer(s, ", ");
            if (st.countTokens() < 3) {
                sEnv.println("Missing values in coordinate string:" + s);
                return null;
            }

            result.x = Integer.parseInt(st.nextToken());
            result.y = Integer.parseInt(st.nextToken());
            result.z = Integer.parseInt(st.nextToken());

            return result;
        } catch (Exception e) {
            sEnv.println("Error parsing coordinate[" + s + "]:" + e);
        }
        return null;
    }

    protected int copyLeaf(ShellEnvironment sEnv, StringTokenizer st) {
        if (!st.hasMoreTokens()) {
            sEnv.println("No paremeters specified.");
            return -3;
        }

        Vector3i loc1 = toCoordinate(sEnv, st.nextToken());
        if (loc1 == null) {
            return -4;
        }
        if (!st.hasMoreTokens()) {
            sEnv.println("No target location specified.");
            return -3;
        }
        Vector3i loc2 = toCoordinate(sEnv, st.nextToken());
        if (loc1 == null) {
            return -4;
        }
        sEnv.println("copy leaf at:" + loc1 + "  to leaf at:" + loc2);

        WorldDatabase worldDb = this.server.getWorldDatabase();
        LeafData leaf1 = worldDb.getLeaf(loc1.x, loc1.y, loc1.z);
        LeafData leaf2 = worldDb.getLeaf(loc2.x, loc2.y, loc2.z);

        sEnv.println("Source:" + leaf1 + "  target:" + leaf2);

        if (leaf2 == null) {
            sEnv.println("Target does not exist.");
            return -5;
        }

        int[][][] source = leaf1 != null ? leaf1.getCells() : (int[][][]) null;
        int[][][] target = leaf2.getCells();

        if ((source == null) && (target == null)) {
            sEnv.println("Nothing to copy.");
            return 0;
        }

        if (source == null) {
            sEnv.println("Source has no data.  Clearing target.");
        }

        if (target == null) {
            target = new int[32][32][32];
            leaf2.setCells(target);
        }

        for (int i = 0; i < 32; i++) {
            for (int j = 0; j < 32; j++) {
                for (int k = 0; k < 32; k++) {
                    if (source == null)
                        target[i][j][k] = 0;
                    else {
                        target[i][j][k] = source[i][j][k];
                    }
                }
            }
        }
        leaf2.getInfo().lit = false;
        leaf2.markChanged();

        worldDb.markChanged(leaf2);

        return 0;
    }

    public int execute(ShellEnvironment sEnv, String args) {
        StringTokenizer st = new StringTokenizer(args, " ");
        if (!st.hasMoreTokens()) {
            sEnv.println("No type specified.");
            return -1;
        }

        String type = st.nextToken();
        if ("leaf".equals(type)) {
            return copyLeaf(sEnv, st);
        }

        sEnv.println("Unknown type:" + type);
        return -2;
    }

    public boolean isSimple() {
        return true;
    }
}