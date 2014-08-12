package mythruna.shell;

import mythruna.util.ReportSystem;
import org.progeeks.tool.console.AbstractShellCommand;
import org.progeeks.tool.console.ShellEnvironment;

public class ReportCommand extends AbstractShellCommand {

    public static final String DESCRIPTION = "Displays reports of different resource usage.";
    public static final String[] HELP = {"Usage: report <type>", "   Where type is one of:", "   cache - displays information about current cache usage.", "           This is also the default if no option is specified."};

    public ReportCommand() {
        super("Displays reports of different resource usage.", HELP);
    }

    public int execute(ShellEnvironment sEnv, String args) {
        if (args.length() == 0) {
            args = "cache";
        }
        sEnv.println(ReportSystem.getReport(args));
        return 0;
    }

    public boolean isSimple() {
        return true;
    }
}