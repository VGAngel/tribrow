package de.d2dev.fourseasons.script.lua;

import java.util.List;
import java.util.Vector;

import org.luaj.vm2.LuaFunction;

import de.d2dev.fourseasons.script.Script;
import de.d2dev.fourseasons.script.ScriptFunction;

public class LuaScript implements Script {
	
	private LuaFunction script;
	private List<ScriptFunction> functions = new Vector<ScriptFunction>();
	
	public LuaScript(LuaFunction script, List<LuaScriptFunction> functions) {
		this.script = script;
		this.functions.addAll( functions );
	}

	@Override
	public List<ScriptFunction> getFunctions() {
		return this.functions;
	}
	
	public LuaFunction getScript() {
		return this.script;
	}

}
