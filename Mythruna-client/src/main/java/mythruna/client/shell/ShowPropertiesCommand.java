package mythruna.client.shell;

import mythruna.GameConstants;
import mythruna.client.MainStart;
import org.progeeks.tool.console.AbstractShellCommand;
import org.progeeks.tool.console.ShellEnvironment;
import org.progeeks.util.StringUtils;
import org.progeeks.util.log.Log;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ShowPropertiesCommand extends AbstractShellCommand {
    static Log log = Log.getLog();

    private static final String[] ECHO_PROPS = {"java.runtime.name", "java.vm.version", "java.vm.vendor", "java.vm.name", "sun.java.launcher", "sun.os.patch.level", "java.vm.specification.name", "java.runtime.version", "java.awt.graphicsenv", "os.arch", "java.vm.specification.vendor", "os.name", "sun.management.compiler", "os.version", "sun.arch.data.model", "java.vm.info", "java.version", "java.vendor"};

    private static final Set<String> ECHO_PROPSET = new HashSet(Arrays.asList(ECHO_PROPS));
    public static final String DESCRIPTION = "Displays and logs the system properties.";
    public static final String[] HELP = {"Usage: props", "", "  Displays some of the system properties and writes all of them", "  to a file in the current working directory."};

    public ShowPropertiesCommand() {
        super("Displays and logs the system properties.", HELP);
    }

    public int execute(ShellEnvironment sEnv, String args) {
        StringBuilder sb = new StringBuilder();

        for (Map.Entry e : System.getProperties().entrySet()) {
            String line = e.getKey() + " = " + e.getValue();
            sb.append(line + "\n");
            System.out.println(line);
            if (ECHO_PROPSET.contains(e.getKey())) {
                sEnv.println(line);
            }
        }
        StringWriter sOut = new StringWriter();
        PrintWriter out = new PrintWriter(sOut);

        MainStart app = MainStart.instance;
        if (app == null) {
            out.println("Application was not initialized.");
        } else {
            out.println("Build version:" + GameConstants.buildVersion());

            out.println();
            app.writeAppInfo(out);
        }
        out.flush();
        sb.append("\n" + sOut);
        try {
            File f = new File("system-props-" + System.currentTimeMillis() + ".txt");
            System.out.println("Writing property dump to:" + f);
            StringUtils.writeFile(sb.toString(), f);

            sEnv.println("Full property dump written to:" + f.getCanonicalFile().getPath());
        } catch (Exception e) {
            log.error("Error writing thread stack dump", e);
        }

        return 0;
    }

    public boolean isSimple() {
        return true;
    }
}
