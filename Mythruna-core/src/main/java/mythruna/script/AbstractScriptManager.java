package mythruna.script;

import org.progeeks.util.StringUtils;
import org.progeeks.util.log.Log;

import javax.script.Bindings;
import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractScriptManager {

    static Log log = Log.getLog();

    private List<Object> scripts = new ArrayList();
    private String scriptExtension;
    private Bindings bindings;

    public AbstractScriptManager(String scriptExtension, Object[] scripts) {
        this.scriptExtension = scriptExtension;
        this.scripts.addAll(Arrays.asList(scripts));

        this.bindings = createBindings();
        this.bindings.put("bindings", this.bindings);
        this.bindings.put("scripts", this);
    }

    public void copySettingsTo(AbstractScriptManager to) {
        to.scripts.addAll(this.scripts);
        to.scriptExtension = this.scriptExtension;
        to.bindings.putAll(this.bindings);

        to.bindings.put("bindings", to.bindings);
        to.bindings.put("scripts", to);
    }

    protected abstract Bindings createBindings();

    protected abstract void applyEngineBindings(Bindings paramBindings);

    protected abstract void eval(String paramString);

    protected abstract void addClasspath(String paramString);

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
        eval(initScript);
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
        String script = StringUtils.readFile(f);
        eval(script);
    }

    protected void initializeDirectory(File d)
            throws IOException, ScriptException {
        addClasspath(d.getPath());

        File[] libs = new File(d, "lib").listFiles();
        if (libs != null) {
            for (File f : libs) {
                if (f.getName().endsWith(".jar")) {
                    log.info("Adding script dependency:" + f);
                    addClasspath(f.getPath());
                }
            }

        }

        File[] init = d.listFiles();
        if (init != null) {
            for (File f : init) {
                if (f.getName().endsWith(this.scriptExtension)) {
                    initializeFile(f);
                }
            }
        }
    }

    public void initialize() {
        applyEngineBindings(this.bindings);

        for (int i = 0; i < this.scripts.size(); i++) {
            Object o = this.scripts.get(i);
            try {
                if ((o instanceof String)) {
                    initializeResource((String) o);
                    continue;
                }
                if ((o instanceof File)) {
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