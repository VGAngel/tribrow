package mythruna.shell;

import org.progeeks.tool.console.AbstractShellCommand;
import org.progeeks.tool.console.ShellEnvironment;

public class GcCommand extends AbstractShellCommand
{
    public static final String DESCRIPTION = "Requests that the memory garbage collector execute.";
    public static final String[] HELP = { "Usage: gc", "   No additional parameters." };

    public GcCommand()
    {
        super("Requests that the memory garbage collector execute.", HELP);
    }

    public int execute(ShellEnvironment sEnv, String args)
    {
        long start = System.nanoTime();
        System.gc();
        long end = System.nanoTime();
        sEnv.println("GC executed in " + (end - start / 1000000.0D) + " ms.");
        return 0;
    }

    public boolean isSimple()
    {
        return true;
    }
}