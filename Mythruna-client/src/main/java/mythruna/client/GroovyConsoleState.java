package mythruna.client;

import com.jme3.app.Application;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.GroovyShell;
import groovy.ui.Console;
import mythruna.GameSystems;
import mythruna.client.shell.DefaultConsole;
import mythruna.client.ui.ObservableState;
import mythruna.script.ScriptManager;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.progeeks.tool.console.AbstractShellCommand;
import org.progeeks.tool.console.Shell;
import org.progeeks.tool.console.ShellEnvironment;
import org.progeeks.util.StringUtils;
import org.progeeks.util.log.Log;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GroovyConsoleState extends ObservableState {
    static Log log = Log.getLog();
    private GameClient client;
    private Console console;
    private JFrame frame;
    private Component outputWindow;
    private Bindings globalBindings = null;
    private List<String> scriptList = new ArrayList<String>();
    public static final String DESCRIPTION = "Opens the script console.";
    public static final String[] HELP = {"Usage: script", "   Opens the script console."};

    public GroovyConsoleState(GameClient client) {
        super("Groovy Console", false);

        this.client = client;
    }

    protected void initialize(Application app) {
        super.initialize(app);

        Shell shell = ((DefaultConsole) this.client.getConsole()).getShell();
        shell.registerCommand("script", new EnableScriptConsoleCommand());
    }

    protected String readScript(String s) throws IOException {
        return StringUtils.readStringResource(getClass(), s);
    }

    protected void initializeResource(Console console, String s) throws IOException {
        String initScript = StringUtils.readStringResource(getClass(), s);

        console.getShell().evaluate(initScript);
    }

    protected void initializeFile(Console console, File f) throws IOException {
        if (!f.exists()) {
            log.warn("Script file or directory does not exist:" + f);
            return;
        }

        String script = StringUtils.readFile(f);
        console.getShell().evaluate(script);
    }

    public void update(float tpf) {
        if ((this.frame != null) && (!this.frame.isDisplayable()))
            setEnabled(false);
    }

    protected void enable() {
        this.console = new Console();
        this.console.setShell(new EnhancedShell(this.console.getShell(), this.scriptList));
        this.console.run();

        this.outputWindow = this.console.getOutputWindow();
        this.frame = ((JFrame) this.console.getFrame());

        if ((this.client instanceof LocalGameClient)) {
            LocalGameClient localClient = (LocalGameClient) this.client;
            GroovyShell shell = this.console.getShell();
            GameSystems systems = localClient.getGameSystems();
            ScriptManager scripts = systems.getScriptManager();

            this.globalBindings = scripts.getBindings();

            for (Map.Entry<String, Object> e : this.globalBindings.entrySet()) {
                shell.setVariable(e.getKey(), e.getValue());
            }

            shell.setVariable("systems", systems);
            shell.setVariable("player", this.client.getPlayer());
            shell.setVariable("playerData", localClient.getPlayerData());
            shell.setVariable("console", localClient.getConsole());
            shell.setVariable("shell", ((DefaultConsole) localClient.getConsole()).getShell());
            shell.setVariable("perms", localClient.getPerms());

            shell.evaluate("echo = { console.echo(it) }");
            try {
                this.scriptList.add(readScript("/mythruna/script/BaseScriptEnvironment.groovy"));
                this.scriptList.add(readScript("/mythruna/script/ClientEnvironment.groovy"));
            } catch (IOException e) {
                throw new RuntimeException("Error reading base scripts", e);
            }
        } else {
            throw new RuntimeException("Multiplayer client-side scripting console not supported.");
        }
    }

    protected void disable() {
        if ((this.frame != null) && (this.frame.isDisplayable()))
            this.console.exit(null);
    }

    public void cleanup() {
        disable();
    }

    public class EnableScriptConsoleCommand extends AbstractShellCommand {
        public EnableScriptConsoleCommand() {
            super("Opens the script console.", GroovyConsoleState.HELP);
        }

        public int execute(ShellEnvironment sEnv, String args) {
            GroovyConsoleState.this.setEnabled(true);
            return 0;
        }

        public boolean isSimple() {
            return true;
        }
    }

    public class EnhancedShell extends GroovyShell {
        private List<String> scriptList;

        public EnhancedShell(GroovyShell inherit, List<String> scriptList) {
            super(inherit.getContext(), new CompilerConfiguration());
            this.scriptList = scriptList;
        }

        public Object run(String scriptText, String fileName, List list) throws CompilationFailedException {
            ScriptEngineManager factory = new ScriptEngineManager();
            ScriptEngine engine = factory.getEngineByName("groovy");
            engine.setBindings(GroovyConsoleState.this.globalBindings, 100);
            Bindings bindings = GroovyConsoleState.this.globalBindings;

            bindings.putAll(getContext().getVariables());
            try {
                for (String s : this.scriptList) {
                    engine.eval(s);
                }
                Object result = engine.eval(scriptText);

                getContext().getVariables().putAll(bindings);

                return result;
            } catch (ScriptException e) {
                throw new GroovyRuntimeException("Error executing script", e);
            }
        }
    }
}