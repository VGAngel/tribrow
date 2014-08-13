package de.d2dev.fourseasons.util;

/**
 * Interface that can be implemented by observable singletons.
 * @author Sebastian Bordt
 *
 * @param <T>
 */
public interface Observable<T> {
	
	public void addListener(T l);
	public void removeListener(T l);
}
