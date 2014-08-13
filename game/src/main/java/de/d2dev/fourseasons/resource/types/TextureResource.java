package de.d2dev.fourseasons.resource.types;

import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import de.d2dev.fourseasons.resource.*;
import de.schlichtherle.truezip.file.TFileInputStream;

public class TextureResource extends ResourceType {
	
	public static final String TYPENAME = "Texture";
	
	private static final TextureResource instance = new TextureResource();

	public static TextureResource instance() {
		return instance;
	}
	
	public static Resource createTextureResource(String name) {
		return new Resource( name, instance );
	}
	
	public static BufferedImage load(String location) throws Exception {
		TFileInputStream in = new TFileInputStream( location );
		
		try {
			return ImageIO.read( in );
		} finally {
			in.close();	// allway close
		}
	}

	protected TextureResource() {
		super( TYPENAME );
	}
	
}
