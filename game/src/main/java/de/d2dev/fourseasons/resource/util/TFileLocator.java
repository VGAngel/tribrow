package de.d2dev.fourseasons.resource.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetLoadException;
import com.jme3.asset.AssetLocator;
import com.jme3.asset.AssetManager;
import com.jme3.asset.AssetNotFoundException;

import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.file.TFileInputStream;

/**
 * Porting the {@link com.jme3.asset.plugins.FileLocator} to TFiles.
 * @author Sebastian Bordt
 *
 */
public class TFileLocator implements AssetLocator {

	private TFile root;

    public void setRootPath(String rootPath) {
        if (rootPath == null)
            throw new NullPointerException();

        root = new TFile(rootPath);
        if (!root.isDirectory())
            throw new IllegalArgumentException("Given root path \"" + root + "\" not a directory");
    }

    private static class AssetInfoFile extends AssetInfo {

        private TFile file;

        @SuppressWarnings("rawtypes")
		public AssetInfoFile(AssetManager manager, AssetKey key, TFile file){
            super(manager, key);
            this.file = file;
        }

        @Override
        public InputStream openStream() {
            try{
                return new TFileInputStream(file);
            }catch (FileNotFoundException ex){
                return null;
            }
        }
    }

    @SuppressWarnings("rawtypes")
	public AssetInfo locate(AssetManager manager, AssetKey key) {
        String name = key.getName();
        TFile file = new TFile(root, name);
        if (file.exists() && file.isFile()){
            try {
                // Now, check asset name requirements
                String canonical = file.getCanonicalPath();
                String absolute = file.getAbsolutePath();
                if (!canonical.endsWith(absolute)){
                    throw new AssetNotFoundException("Asset name doesn't match requirements.\n"+
                                                     "\"" + canonical + "\" doesn't match \"" + absolute + "\"");
                }
            } catch (IOException ex) {
                throw new AssetLoadException("Failed to get file canonical path " + file, ex);
            }
            
            
            return new AssetInfoFile(manager, key, file);
        }else{
            return null;
        }
    }
}
