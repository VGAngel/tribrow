package de.d2dev.fourseasons.resource;

public interface ResourceLocator {
	
	/**
	 * Get a resources absolute location (from where it can be loaded).
	 * @param r
	 * @return {@code null} in case the resource could not be found.
	 */
	public String getAbsoluteLocation(Resource r);
}
