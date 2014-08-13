package de.d2dev.fourseasons.resource.types;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import com.google.common.base.Preconditions;

import de.d2dev.fourseasons.resource.Resource;
import de.d2dev.fourseasons.resource.ResourceLocator;

/**
 * Utility class to store textures as {@code BufferedImage}s.
 * @author Sebastian Bordt
 *
 */
public class BufferedImageStorage {
	
	private ResourceLocator resourceFinder;
	
	private HashMap< String, BufferedImage > images = new HashMap< String, BufferedImage >();
	
	public BufferedImageStorage(ResourceLocator resourceFinder) {
		super();
		this.resourceFinder = resourceFinder;
	}

	/**
	 * Loads the given texture resource as a {@code BufferedImage}.
	 * @param texture
	 * @return The given texture or a pink error image if the texture couldn't be found.
	 */
	public BufferedImage provideTexture(Resource texture) {
		// only texture resources
		Preconditions.checkArgument( texture.getType().isType( TextureResource.TYPENAME ), "Attempt to provide a texture from a non-texture resource." );
		
		BufferedImage img;
		
		// have we already stored this image?
		if ( (img = this.images.get( texture.getName() )) == null ) {
			// load the image
			try {
				img = TextureResource.load( this.resourceFinder.getAbsoluteLocation( texture ) );
				this.images.put( texture.getName(), img );
			} 
			// loading failed - provide the error image
			catch (Exception e) {
				e.printStackTrace();
				
				img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB );
				Graphics2D graphics = img.createGraphics();

				graphics.setColor( Color.PINK );
				graphics.fillRect( 0, 0, img.getWidth(), img.getHeight() );
			}
		}
		
		return img;
	}
	
}
