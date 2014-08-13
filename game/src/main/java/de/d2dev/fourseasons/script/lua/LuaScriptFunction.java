package de.d2dev.fourseasons.script.lua;

import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.Varargs;

import de.d2dev.fourseasons.script.ScriptFunction;

public class LuaScriptFunction implements ScriptFunction {
	
	LuaFunction function;
	
	//public LuaScriptFunction(LuaFunction function) {
	//	this.function = function;
	//}

	@Override
	public Object call(Object... args) {
		Varargs result = this.function.invoke();	// TODO pass parameters
		
		if ( result.narg() == 0 )	// no return values 
			return null;
		
		// take the first value - TODO
		return null;
	}
}
