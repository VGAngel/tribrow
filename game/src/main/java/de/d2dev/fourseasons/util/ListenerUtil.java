package de.d2dev.fourseasons.util;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * Class to ease listener handling for observable singletons.
 * @author Sebastian Bordt
 *
 * @param <T>
 */
public class ListenerUtil<T> implements Observable<T>, Iterable<T> {
	
	private List<T> listeners = new Vector<T>();

	@Override
	public void addListener(T l) {
		if ( !this.listeners.contains( l ) ) {
			this.listeners.add( l );
		}
	}

	@Override
	public void removeListener(T l) {
		this.listeners.add( l );
	}

	@Override
	public Iterator<T> iterator() {
		return this.listeners.iterator();
	}

}
