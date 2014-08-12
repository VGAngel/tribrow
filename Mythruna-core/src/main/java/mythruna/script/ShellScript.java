package mythruna.script;

import groovy.lang.Closure;
import mythruna.PlayerContext;
import mythruna.shell.PlayerShellCommandProcessor;
import org.progeeks.tool.console.AbstractShellCommand;
import org.progeeks.tool.console.ShellEnvironment;

public class ShellScript extends AbstractShellCommand {

    private Closure script;

    public ShellScript(String description, String[] help, Closure script) {
        super(description, help);
        this.script = script;
    }

    public int execute(ShellEnvironment sEnv, String args) {
        PlayerContext context = ((PlayerShellCommandProcessor) sEnv).getPlayerContext();

        ExecutionContext.setEnvironment(context);
        try {
            this.script.setDelegate(context);
            this.script.call(args);
            return 0;
        } finally {
            ExecutionContext.setEnvironment(null);
        }
    }

    public boolean isSimple() {
        return true;
    }
}