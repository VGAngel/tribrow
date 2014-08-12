package mythruna.shell;

import org.progeeks.tool.console.AbstractShellCommand;
import org.progeeks.tool.console.ShellEnvironment;
import org.progeeks.util.StringUtils;
import org.progeeks.util.log.Log;

import java.io.File;
import java.util.Map;
import java.util.TreeSet;

public class ShowThreadsCommand extends AbstractShellCommand {

    static Log log = Log.getLog();
    public static final String DESCRIPTION = "Displays the active threads and their stack traces.";
    public static final String[] HELP = {"Usage: threads", "", "  Displays all of the currently active threads and their", "  stack traces."};

    public ShowThreadsCommand() {
        super("Displays the active threads and their stack traces.", HELP);
    }

    public int execute(ShellEnvironment sEnv, String args) {
        TreeSet<String> dumps = new TreeSet<>();
        for (Map.Entry<Thread, StackTraceElement[]> e : Thread.getAllStackTraces().entrySet()) {
            Thread t = e.getKey();
            StackTraceElement[] trace = e.getValue();
            StringBuilder sb = new StringBuilder();
            sb.append(new StringBuilder().append(" \"").append(t.getName()).append("\"").toString());
            sb.append(new StringBuilder().append(" #").append(t.getId()).toString());
            sb.append(t.isDaemon() ? " daemon" : "");
            sb.append(new StringBuilder().append(" prio=").append(t.getPriority()).toString());
            sb.append(new StringBuilder().append(" state=").append(t.getState()).toString());

            for (int i = 0; i < trace.length; i++) {
                sb.append(new StringBuilder().append("\n        ").append(trace[i]).toString());
            }
            sb.append("\n");

            dumps.add(sb.toString());
        }

        StringBuilder sb = new StringBuilder();
        for (String s : dumps) {
            System.out.println(s);
            sb.append(new StringBuilder().append(s).append("\n").toString());
        }

        try {
            File f = new File(new StringBuilder().append("threads-").append(System.currentTimeMillis()).append(".txt").toString());
            System.out.println(new StringBuilder().append("Writing thread dump to:").append(f).toString());
            StringUtils.writeFile(sb.toString(), f);

            sEnv.println(new StringBuilder().append("Thread dump written to:").append(f.getCanonicalFile().getPath()).toString());
        } catch (Exception e) {
            log.error("Error writing thread stack dump", e);
        }

        return 0;
    }

    public boolean isSimple() {
        return true;
    }
}