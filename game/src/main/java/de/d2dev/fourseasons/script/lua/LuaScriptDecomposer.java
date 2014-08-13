package de.d2dev.fourseasons.script.lua;

import java.util.List;
import java.util.Vector;

import org.luaj.vm2.LuaFunction;

/**
 * This class is meant to be decorated to decompose custom lua scripts.
 * @author Sebastian Bordt
 *
 */
public class LuaScriptDecomposer {
	
	/**
	 * Does nothing.
	 * @param script
	 * @return Empty list.
	 */
	public List<LuaScriptFunction> decompose(LuaFunction script) {
		return new Vector<LuaScriptFunction>();
	}
}