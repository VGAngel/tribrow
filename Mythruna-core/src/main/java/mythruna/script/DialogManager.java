package mythruna.script;

import groovy.lang.Closure;
import mythruna.es.EntityId;
import org.codehaus.groovy.jsr223.GroovyScriptEngineImpl;
import org.progeeks.util.StringUtils;
import org.progeeks.util.log.Log;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DialogManager {

    static Log log = Log.getLog();

    private List<Object> initScripts = new ArrayList();
    private List<String> scripts = new ArrayList();
    private List<String> classpaths = new ArrayList();

    private Map<String, Object> dialogs = new ConcurrentHashMap();

    private Map<EntityId, PlayerEnvironment> envs = new ConcurrentHashMap();

    private List<Object> roots = new ArrayList();
    private Map<String, Object> defaultBindings;
    private ScriptEngineManager factory;

    public DialogManager(File root, Object[] startupScripts) {
        this.roots.add(root);
        this.initScripts.addAll(Arrays.asList(startupScripts));

        this.factory = new ScriptEngineManager();
        this.defaultBindings = new HashMap();

        this.defaultBindings.put("bindings", this.defaultBindings);
        this.defaultBindings.put("scripts", this);
    }

    public void addRoot(Object root) {
        this.roots.add(root);
    }

    public void setBinding(String name, Object value) {
        this.defaultBindings.put(name, value);
    }

    public void addStartupScript(Object o) {
        this.initScripts.add(o);
    }

    protected String nameToScript(String name) {
        return name + ".dlg.groovy";
    }

    protected String readScript(String f) {
        try {
            for (Iterator i$ = this.roots.iterator(); i$.hasNext(); ) {
                Object o = i$.next();

                if ((o instanceof String)) {
                    InputStream in = getClass().getResourceAsStream(o + "/" + f);
                    if (in != null) {
                        return StringUtils.readString(new InputStreamReader(in));
                    }
                } else if ((o instanceof File)) {
                    File file = new File((File) o, f);
                    if (file.exists()) {
                        return StringUtils.readFile(file);
                    }
                }
            }
            throw new RuntimeException("File not found for:" + f);
        } catch (IOException e) {
            throw new RuntimeException("Error reading dialog:" + f, e);
        }
    }

    protected PlayerEnvironment getEnv(EntityId player)
            throws ScriptException {
        PlayerEnvironment env = (PlayerEnvironment) this.envs.get(player);
        if (env == null) {
            ScriptEngine se = this.factory.getEngineByName("groovy");
            env = new PlayerEnvironment(player, se);
            this.envs.put(player, env);
        }

        return env;
    }

    public void startDialog(EntityId player, String name) throws ScriptException {
        String script = (String) this.dialogs.get(name);
        if (script == null) {
            script = readScript(nameToScript(name));
            this.dialogs.put(name, script);
        }

        PlayerEnvironment env = getEnv(player);
        env.run(script);
    }

    public void selectOption(EntityId player, DialogPrompt prompt, int index, DialogOption option) {
        Closure c = option.getAction();
        if (c.getMaximumNumberOfParameters() > 1)
            c.call(new Object[]{prompt, Integer.valueOf(index)});
        else if (c.getMaximumNumberOfParameters() == 1)
            c.call(Integer.valueOf(index));
        else
            c.call();
    }

    protected void loadScriptResource(String s) throws IOException, ScriptException {
        String initScript = StringUtils.readStringResource(getClass(), s);
        System.out.println("Adding:" + s);
        this.scripts.add(initScript);
    }

    protected void loadScriptFile(File f) throws IOException, ScriptException {
        if (f.isDirectory()) {
            loadDirectory(f);
            return;
        }

        if (!f.exists()) {
            log.warn("Script file or directory does not exist:" + f);
            return;
        }

        System.out.println("Adding:" + f);
        String script = StringUtils.readFile(f);
        this.scripts.add(script);
    }

    protected void loadDirectory(File d)
            throws IOException, ScriptException {
        this.classpaths.add(d.getPath());

        File[] libs = new File(d, "lib").listFiles();
        if (libs != null) {
            for (File f : libs) {
                if (f.getName().endsWith(".jar")) {
                    log.info("Adding script dependency:" + f);

                    this.classpaths.add(f.getPath());
                }
            }

        }

        File[] init = d.listFiles();
        if (init != null) {
            for (File f : init) {
                if (f.getName().endsWith(".init.groovy")) {
                    loadScriptFile(f);
                }
            }
        }
    }

    protected void initEngine(ScriptEngine engine) throws ScriptException {
        for (String s : this.classpaths) {
            ((GroovyScriptEngineImpl) engine).getClassLoader().addClasspath(s);
        }

        for (String s : this.scripts) {
            engine.eval(s);
        }
    }

    public void initialize() {
        for (int i = 0; i < this.initScripts.size(); i++) {
            Object o = this.initScripts.get(i);
            try {
                if ((o instanceof String)) {
                    loadScriptResource((String) o);
                    continue;
                }
                if ((o instanceof File)) {
                    loadScriptFile((File) o);
                    continue;
                }
            } catch (Exception e) {
                throw new RuntimeException("Error processing script resource:" + o, e);
            }

            throw new RuntimeException("Unknown resource type:" + o);
        }
    }

    private class PlayerEnvironment {
        private EntityId player;
        private ScriptEngine engine;
        private Bindings bindings;
        private Map options;

        public PlayerEnvironment(EntityId player, ScriptEngine engine) throws ScriptException {
            this.player = player;
            this.engine = engine;
            this.bindings = engine.createBindings();
            this.bindings.putAll(DialogManager.this.defaultBindings);
            this.bindings.put("player", player);
            this.options = new HashMap();
            this.bindings.put("options", this.options);
            engine.setBindings(this.bindings, 100);
            DialogManager.this.initEngine(engine);
        }

        public synchronized void run(String script) throws ScriptException {
            this.options.clear();
            this.engine.eval(script);
        }
    }
}