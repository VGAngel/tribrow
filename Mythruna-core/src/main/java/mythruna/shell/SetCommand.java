package mythruna.shell;

import org.progeeks.tool.console.AbstractShellCommand;
import org.progeeks.tool.console.ShellEnvironment;
import org.progeeks.util.Inspector;
import org.progeeks.util.SimpleExpressionLanguage;
import org.progeeks.util.TemplateExpressionProcessor;

public class SetCommand extends AbstractShellCommand {

    public static final String DESCRIPTION = "Sets the value of a variable or option.";
    public static final String[] HELP = {"Usage: set <expression> <value>", "Where:", "  <expression>  is dot-notation variable to set, such as", "                opts.rotationSpeed", "  <value> is the value to set."};

    public SetCommand() {
        super("Sets the value of a variable or option.", HELP);
    }

    public int execute(ShellEnvironment sEnv, String args) {
        if (args.length() == 0) {
            sEnv.println("No expression or value specified.");
            return -1;
        }

        int split = args.indexOf(32);
        if (split < 0) {
            sEnv.println("No value specified.");
            return -2;
        }

        String name = args.substring(0, split);
        String value = args.substring(split + 1);

        SimpleExpressionLanguage el = TemplateExpressionProcessor.getContextProcessor().getExpressionLanguage();
        Class c = el.getPropertyClass(sEnv.getVariables(), name);
        if (c == null) {
            sEnv.println("Variable does not exist for:" + name);
            return -3;
        }

        try {
            Object o = Inspector.constructFromString(value, c);
            sEnv.println("Setting: " + name + "=" + o);
            el.setProperty(sEnv.getVariables(), name, o);
        } catch (Exception e) {
            sEnv.println("Error setting value. " + e.getMessage());
        }

        return 0;
    }

    public boolean isSimple() {
        return true;
    }
}