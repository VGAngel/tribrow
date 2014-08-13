package de.d2dev.fourseasons.resource;

/**
 * Dummy where the absolute location is just the resources name.
 * Makes testing easy when a resource locator is required.
 * @author Sebastian Bordt
 *
 */
public class DummyResourceLocator implements ResourceLocator {

	@Override
	public String getAbsoluteLocation(Resource r) {
		return r.getName();
	}
}
