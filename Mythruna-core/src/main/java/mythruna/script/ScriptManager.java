package mythruna.script;

import org.codehaus.groovy.jsr223.GroovyScriptEngineImpl;
import org.progeeks.util.StringUtils;
import org.progeeks.util.log.Log;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ScriptManager {

    static Log log = Log.getLog();

    private static final String[] STANDARD_SCRIPTS = {"/mythruna/script/BaseScriptEnvironment.groovy", "/mythruna/script/BaseObjectEnvironment.groovy", "/mythruna/script/standard-actions.groovy", "/mythruna/script/object-actions.groovy", "/mythruna/script/property-actions.groovy", "/scripts/tools/tools.init.groovy", "/mythruna/script/cell-hook.groovy", "/mythruna/script/player-setup.groovy"};

    private List<Object> scripts = new ArrayList();

    private List<String> classpath = new ArrayList();
    private ScriptEngine engine;
    private Bindings bindings;
    private String scriptExtension = ".init.groovy";
    private int processingIndex = -1;
    private int nextIndex = -1;

    public ScriptManager(Object[] scripts) {
        this.scripts.addAll(Arrays.asList(scripts));

        ScriptEngineManager factory = new ScriptEngineManager();
        this.engine = factory.getEngineByName("groovy");
        this.bindings = this.engine.createBindings();

        this.bindings.put("bindings", this.bindings);
        this.bindings.put("scripts", this);
    }

    public Bindings getBindings() {
        return this.bindings;
    }

    public List<String> getInitializedClasspath() {
        return this.classpath;
    }

    public void addStandardScripts() {
        for (String s : STANDARD_SCRIPTS)
            addScript(s);
    }

    public void setBinding(String name, Object value) {
        this.bindings.put(name, value);
    }

    public void setScriptExtension(String ext) {
        if (ext.startsWith("."))
            this.scriptExtension = ext;
        else
            this.scriptExtension = ("." + ext);
    }

    public void addScript(Object o) {
        this.scripts.add(o);
    }

    protected void initializeResource(String s) throws IOException, ScriptException {
        String initScript = StringUtils.readStringResource(getClass(), s);
        System.out.println("Running:" + s);
        this.engine.eval(initScript);
    }

    protected void initializeFile(File f) throws IOException, ScriptException {
        if (f.isDirectory()) {
            initializeDirectory(f);
            return;
        }

        if (!f.exists()) {
            log.warn("Script file or directory does not exist:" + f);
            return;
        }

        System.out.println("Running:" + f);
        this.bindings.put("localDirectory", f.getParent());
        String script = StringUtils.readFile(f);
        this.engine.eval(script);
    }

    protected void initializeDirectory(File d)
            throws IOException, ScriptException {
        ((GroovyScriptEngineImpl) this.engine).getClassLoader().addClasspath(d.getPath());
        this.classpath.add(d.getPath());

        File[] libs = new File(d, "lib").listFiles();
        if (libs != null) {
            for (File f : libs) {
                if (f.getName().endsWith(".jar")) {
                    log.info("Adding script dependency:" + f);
                    ((GroovyScriptEngineImpl) this.engine).getClassLoader().addClasspath(f.getPath());
                    this.classpath.add(f.getPath());
                }
            }

        }

        File[] init = d.listFiles();
        if (init != null) {
            for (File f : init) {
                if ((f.getName().endsWith(this.scriptExtension)) || (f.isDirectory())) {
                    initializeFile(f);
                }
            }
        }
    }

    public void initializeNext(String s) {
        if (this.processingIndex < 0) {
            throw new RuntimeException("ScriptManager is not processing init scripts.");
        }
        this.scripts.add(this.nextIndex++, s);
    }

    public void initializeNext(File f) {
        if (this.processingIndex < 0) {
            throw new RuntimeException("ScriptManager is not processing init scripts.");
        }
        this.scripts.add(this.nextIndex++, f);
    }

    public void initialize() {
        this.engine.setBindings(this.bindings, 100);

        for (int i = 0; i < this.scripts.size(); i++) {
            Object o = this.scripts.get(i);

            this.processingIndex = i;
            this.nextIndex = (this.processingIndex + 1);
            try {
                if ((o instanceof String)) {
                    this.bindings.put("localDirectory", new File("."));
                    initializeResource((String) o);
                    continue;
                }
                if ((o instanceof File)) {
                    File f = (File) o;
                    initializeFile((File) o);
                    continue;
                }
            } catch (Exception e) {
                throw new RuntimeException("Error processing script resource:" + o, e);
            }

            throw new RuntimeException("Unknown resource type:" + o);
        }
    }
}