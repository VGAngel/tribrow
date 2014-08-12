package mythruna.server;

import org.codehaus.groovy.jsr223.GroovyScriptEngineImpl;
import org.progeeks.util.StringUtils;
import org.progeeks.util.log.Log;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.File;

public class ScriptManagerOld {

    static Log log = Log.getLog();
    private GameServer server;
    private File scriptRoot;

    public ScriptManagerOld(File scriptRoot) {
        this.scriptRoot = scriptRoot;
    }

    public void initialize(GameServer server) {
        this.server = server;
        try {
            ScriptEngineManager factory = new ScriptEngineManager();
            ScriptEngine engine = factory.getEngineByName("groovy");

            ((GroovyScriptEngineImpl) engine).getClassLoader().addClasspath(this.scriptRoot.getPath());

            File[] libs = new File(this.scriptRoot, "lib").listFiles();

            if (libs != null) {
                for (File f : libs) {
                    if (f.getName().endsWith(".jar")) {
                        log.info("Adding script dependency:" + f);
                        ((GroovyScriptEngineImpl) engine).getClassLoader().addClasspath(f.getPath());
                    }

                }

            }

            Bindings bindings = engine.createBindings();
            bindings.put("bindings", bindings);
            bindings.put("server", server);
            bindings.put("eventDispatcher", server.getEventDispatcher());
            engine.setBindings(bindings, 100);

            String initScript = StringUtils.readStringResource(getClass(), "ScriptingEnvironment.groovy");

            engine.eval(initScript);

            File[] init = this.scriptRoot.listFiles();
            if (init != null) {
                for (File f : init) {
                    if (f.getName().endsWith(".init.groovy")) {
                        log.info("Executing initialization script:" + f);
                        String script = StringUtils.readFile(f);
                        engine.eval(script);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error initializing script support", e);
        }
    }
}