package de.d2dev.fourseasons.prototypes.lua;

import java.io.FileInputStream;

import org.luaj.vm2.*;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.lib.jse.JsePlatform;

public class CallFunction {
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		String script = CallFunction.class.getResource("FirstLua.lua").getPath();
		
		// create an environment to run in
		LuaValue _G = JsePlatform.standardGlobals();
		LuaValue print = _G.get("print");
		print.call( LuaValue.valueOf("Java ueber Lua an Java!") );
		
		// compile to bytecode
		LuaFunction fnc = LuaC.instance.load( new FileInputStream(script), "FirstLua.lua", _G );
		fnc.call();
		
		// call function separately
        //TODO:fix
//		LuaFunction bp = (LuaFunction) fnc.getfenv().get("boundPrint");
//		bp.call();
	}
}
