package mythruna.shell;

import org.progeeks.tool.console.AbstractShellCommand;
import org.progeeks.tool.console.ShellEnvironment;

public class MemoryCommand extends AbstractShellCommand {
    public static final String DESCRIPTION = "Displays current memory usage.";
    public static final String[] HELP = {"Usage: mem", "   No additional parameters."};

    public MemoryCommand() {
        super("Displays current memory usage.", HELP);
    }

    public static String getMemoryString() {
        Runtime rt = Runtime.getRuntime();
        long free = rt.freeMemory();
        long total = rt.totalMemory();
        long max = rt.maxMemory();

        long used = total - free;

        long percent1 = used * 100L / total;
        long percent2 = used * 100L / max;

        String line = "Working memory: " + percent1 + "% (" + used + "/" + total + ")" + "  VM Max: " + percent2 + "% (" + used + "/" + max + ")";

        return line;
    }

    public int execute(ShellEnvironment sEnv, String args) {
        sEnv.println(getMemoryString());
        return 0;
    }

    public boolean isSimple() {
        return true;
    }
}