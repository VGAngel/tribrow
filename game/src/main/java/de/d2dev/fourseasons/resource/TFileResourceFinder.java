package de.d2dev.fourseasons.resource;

import java.util.List;
import java.util.Vector;

import de.d2dev.fourseasons.resource.types.AudioResource;
import de.d2dev.fourseasons.resource.types.LuaResource;
import de.d2dev.fourseasons.resource.types.TextureResource;

import de.schlichtherle.truezip.file.TFile;

/**
 * Simple {@link ResourceLocator} that locates resources in different
 * TFile folders. Easy and often we don't need more!
 * @author Sebastian Bordt
 *
 */
public class TFileResourceFinder implements ResourceLocator {
	
	public List<TFile> textureLocations = new Vector<TFile>();
	public List<TFile> audioLocations = new Vector<TFile>();
	public List<TFile> luaScriptLocations = new Vector<TFile>();
	
	@Override
	public String getAbsoluteLocation(Resource r) {
		TFile resource;
		
		// absolute path - general
		if ( ( resource = new TFile( r.getName() ) ).exists() ) {
			return resource.getAbsolutePath();
		}
		
		// textures
		if ( r.getType().isType( TextureResource.TYPENAME ) ) {
			for (TFile textureLocation : this.textureLocations) {
				if ( ( resource = new TFile( textureLocation, r.getName() ) ).exists() ) {
					return resource.getAbsolutePath();
				}
			}
		}
		
		// sounds
		if ( r.getType().isType( AudioResource.TYPENAME ) ) {
			for (TFile audioLocation : this.audioLocations) {
				if ( ( resource = new TFile( audioLocation, r.getName() ) ).exists() ) {
					return resource.getAbsolutePath();
				}
			}
		}
		
		// lua script
		if ( r.getType().isType( LuaResource.TYPENAME ) ) {
			for (TFile luaScriptLocation : this.luaScriptLocations) {
				if ( ( resource = new TFile( luaScriptLocation, r.getName() ) ).exists() ) {
					return resource.getAbsolutePath();
				}				
			}
		}
		
		// not found
		System.out.println( "Resource '" + r.getName() + "' of type '" + r.getType().getTypeName() + "' could not be found!" );
		return null;
	}

}
