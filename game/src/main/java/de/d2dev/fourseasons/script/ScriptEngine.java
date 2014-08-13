package de.d2dev.fourseasons.script;

import java.util.List;
import java.util.Vector;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JseBaseLib;
import org.luaj.vm2.lib.jse.JsePlatform;

import de.d2dev.fourseasons.resource.ResourceLocator;
import de.d2dev.fourseasons.script.lua.LuaResourceFinder;
import de.d2dev.fourseasons.script.lua.LuaScriptDecomposer;
import de.d2dev.fourseasons.script.lua.LuaScriptLoader;
import de.schlichtherle.truezip.file.TFile;

public class ScriptEngine implements ScriptLoader {

    public static ScriptEngine createDefaultScriptEngine(ResourceLocator scriptFinder, LuaScriptDecomposer decomposer) {
        ScriptEngine engine = new ScriptEngine();

        // lua scripts
        LuaValue _G = JsePlatform.standardGlobals();

        // Help lua to resolve 'require' statements
        //TODO:fix
        //JseBaseLib.FINDER = new LuaResourceFinder(scriptFinder);

        // Create a lua script loader using the given lua script decomposer
        LuaScriptLoader luaLoader = new LuaScriptLoader(_G, decomposer);
        engine.addScriptLoader(luaLoader);

        return engine;
    }

    private List<ScriptLoader> loaders = new Vector<ScriptLoader>();

    public void addScriptLoader(ScriptLoader loader) {
        this.loaders.add(loader);
    }

    @Override
    public Script load(TFile file) {
        Script script;

        for (ScriptLoader loader : this.loaders) {
            if ((script = loader.load(file)) != null) {
                return script;
            }
        }

        return null;
    }

}
