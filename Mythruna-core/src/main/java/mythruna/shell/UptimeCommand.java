package mythruna.shell;

import mythruna.server.GameServer;
import org.progeeks.tool.console.AbstractShellCommand;
import org.progeeks.tool.console.ShellEnvironment;

public class UptimeCommand extends AbstractShellCommand {

    public static final String DESCRIPTION = "Shows server up time.";
    public static final String[] HELP = {"Usage: uptime", " No additional options."};
    private static final long MILLIS_PER_MINUTE = 60000L;
    private static final long MILLIS_PER_HOUR = 3600000L;
    private static final long MILLIS_PER_DAY = 86400000L;
    private long startTime = System.currentTimeMillis();
    private GameServer server;
    private boolean showLocal;

    public UptimeCommand(GameServer server, boolean showLocal) {
        super("Shows server up time.", HELP);
        this.server = server;
        this.showLocal = showLocal;
    }

    public static String timeString(long time, boolean forceDays) {
        long days = time / 86400000L;
        long hours = time % 86400000L / 3600000L;
        long minutes = time % 3600000L / 60000L;

        if ((days == 0L) && (!forceDays)) {
            return String.format("%02d:%02d", new Object[]{Long.valueOf(hours), Long.valueOf(minutes)});
        }
        String s = String.format("%d days, %02d:%02d", new Object[]{Long.valueOf(days), Long.valueOf(hours), Long.valueOf(minutes)});

        return s;
    }

    public int execute(ShellEnvironment sEnv, String args) {
        long time = System.currentTimeMillis();
        time -= this.server.getStartupTime();

        String s = "Up " + timeString(time, true);

        if (this.showLocal) {
            time = System.currentTimeMillis();
            time -= this.startTime;

            s = s + "  client online " + timeString(time, true);
        }

        sEnv.println(s);

        return 0;
    }

    public boolean isSimple() {
        return true;
    }
}