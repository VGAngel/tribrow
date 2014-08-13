package de.d2dev.fourseasons.resource;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

import com.google.common.base.Preconditions;
import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetLocator;
import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.audio.AudioKey;

import de.d2dev.fourseasons.resource.types.AudioResource;
import de.d2dev.fourseasons.resource.types.TextureResource;
import de.schlichtherle.truezip.file.TFileInputStream;

/**
 * Adapter to use our {@link ResourceLocator} as a {@link com.jme3.asset.AssetLocator}.
 * @author Sebastian Bordt
 *
 */
@SuppressWarnings("rawtypes")
public class JmeAssetLocatorAdapter implements AssetLocator {
	
	static public HashMap<String, ResourceLocator> locators = new HashMap<String, ResourceLocator>();
	
	private class ResourceAssetInfo extends AssetInfo {

        private String location;

        public ResourceAssetInfo(AssetManager manager,  AssetKey key, String location){
            super(manager, key);
            this.location = location;
        }

        @Override
        public InputStream openStream() {
                try {
					return new TFileInputStream( this.location );
				} catch (FileNotFoundException e) {
					return null;
				}
        }
    }
	
	public ResourceLocator locator;

	@Override
	public AssetInfo locate(AssetManager manager, AssetKey key) {		
		// textures
		if ( key instanceof TextureKey ) {	
			String location = this.locator.getAbsoluteLocation( TextureResource.createTextureResource( key.getName() ) );
			
			if (location == null) 	// not found
				return null;
			
			return new ResourceAssetInfo( manager, key, location );
		}
	
		// sounds
		if ( key instanceof AudioKey ) {
			String location = this.locator.getAbsoluteLocation( AudioResource.createAudioResource( key.getName() ) );
			if (location == null) 	// not found
				return null;
			
			return new ResourceAssetInfo( manager, key, location );			
		}

		return null;	// not found
	}

	@Override
	public void setRootPath(String arg0) {
		this.locator = Preconditions.checkNotNull( JmeAssetLocatorAdapter.locators.get(arg0), "JmeAssetLocatorAdapter: Did not find the locator named " + arg0 );
	}
}
