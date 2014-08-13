package de.d2dev.fourseasons.script.lua;

import java.io.InputStream;

import org.luaj.vm2.lib.ResourceFinder;

import de.d2dev.fourseasons.resource.ResourceLocator;
import de.d2dev.fourseasons.resource.types.LuaResource;
import de.schlichtherle.truezip.file.TFileInputStream;

/**
 * Adapt our {@link ResourceLocator} to serve as a {@link org.luaj.vm2.lib.ResourceFinder} 
 * an find lua resources (i.e. resolve lua require 'file' statements).
 * @author Sebastian Bordt
 *
 */
public class LuaResourceFinder implements ResourceFinder {
	
	public ResourceLocator finder;
	
	public LuaResourceFinder(ResourceLocator finder) {
		this.finder = finder;
	}

	@Override
	public InputStream findResource(String arg0) {
		String location;
		
		if ( ( location = this.finder.getAbsoluteLocation( LuaResource.createLuaResource( arg0 ) ) ) == null )
			return null;
		
		InputStream in;
		
		try {
			in = new TFileInputStream( location );
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		return in;
	}
}
