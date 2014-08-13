package de.d2dev.fourseasons.gamestate.event;

import de.d2dev.fourseasons.gamestate.GameObject;

public class BooleanChangedEvent extends GameObjectChangedEvent {

	public BooleanChangedEvent(GameObject source, String name, boolean oldValue,
			boolean newValue) {
		super(source, name, oldValue, newValue);
	}
}
