package de.d2dev.fourseasons.resource.types;

import de.d2dev.fourseasons.resource.*;

public class LuaResource extends ResourceType {

	public static final String TYPENAME = "LuaScript";
	
	private static final LuaResource instance = new LuaResource();
	
	public static LuaResource instance() {
		return instance;
	}
	
	public static Resource createLuaResource(String name) {
		return new Resource( name, instance );
	}
	
	protected LuaResource() {
		super( TYPENAME );
	}
}
