package de.d2dev.fourseasons.resource.types;

import de.d2dev.fourseasons.resource.Resource;
import de.d2dev.fourseasons.resource.ResourceType;

public class AudioResource  extends ResourceType {

	public static final String TYPENAME = "Audio";
	
	private static final AudioResource instance = new AudioResource();
	
	public static AudioResource instance() {
		return instance;
	}
	
	public static Resource createAudioResource(String name) {
		return new Resource( name, instance );
	}
	
	protected AudioResource() {
		super( TYPENAME );
	}
	
}
