package de.d2dev.fourseasons.gamestate.event;

import de.d2dev.fourseasons.gamestate.GameObject;

/**
 * Superclass for {@code GameObject} events.
 * @author Sebastian Bordt
 *
 */
public class GameObjectChangedEvent {
	
	protected final GameObject source;
	protected final String name;
	protected final Object oldValue;
	protected final Object newValue;
	
	public GameObjectChangedEvent(GameObject source, String name, Object oldValue, Object newValue) {
		this.source = source;
		this.name = name;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}
	
	public GameObject getSource() {
		return this.source;
	}
	
	public String getName() {
		return this.name;
	}
	
	public Object getOldValue() {
		return this.oldValue;
	}
	
	public Object getNewValue() {
		return this.newValue;
	}
}
