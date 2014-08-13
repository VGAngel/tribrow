package de.d2dev.fourseasons.prototypes.lua;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

public class FirstLua {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String script = FirstLua.class.getResource("FirstLua.lua").getPath();
		
		// create an environment to run in
		LuaValue _G = JsePlatform.standardGlobals();
		_G.get("dofile").call( LuaValue.valueOf(script) );
	}
}
