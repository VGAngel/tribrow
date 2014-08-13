package de.d2dev.fourseasons.script.lua;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.compiler.LuaC;

import de.d2dev.fourseasons.script.Script;
import de.d2dev.fourseasons.script.ScriptLoader;
import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.file.TFileInputStream;

public class LuaScriptLoader implements ScriptLoader {
	
	private LuaValue env;
	private LuaScriptDecomposer decomposer;
	
	public LuaScriptLoader(LuaValue env, LuaScriptDecomposer decomposer) {
		this.env = env;
		this.decomposer = decomposer;
	}

	@Override
	public Script load(TFile file) {
		if ( !file.getName().endsWith(".lua" ) )	// we only load .lua files
			return null;
		
		// load the script
		org.luaj.vm2.LuaFunction fnc;
		
		try {
			fnc = LuaC.instance.load( new TFileInputStream( file ), file.getName().substring( 0, file.getName().lastIndexOf( '.' ) ), this.env );
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
		
		// we need to call the script before we can decompose it
		fnc.call();
		
		return new LuaScript( fnc, decomposer.decompose(fnc) );
	}
}
