package mythruna.client.shell;

import mythruna.client.ClientOptions;
import org.progeeks.tool.console.AbstractShellCommand;
import org.progeeks.tool.console.ShellEnvironment;

public class ResetOptionsCommand extends AbstractShellCommand {
    public static final String DESCRIPTION = "Resets all client options to defaults.";
    public static final String[] HELP = {"Usage: resetopts", "   No additional parameters."};

    public ResetOptionsCommand() {
        super("Resets all client options to defaults.", HELP);
    }

    public int execute(ShellEnvironment sEnv, String args) {
        ClientOptions.getInstance().reset();
        sEnv.println("Client options have been restored to default.");
        return 0;
    }

    public boolean isSimple() {
        return true;
    }
}