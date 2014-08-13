package de.d2dev.fourseasons.gamestate.primitives;

import de.d2dev.fourseasons.gamestate.*;

public class BooleanObject extends GameObjectAdapter {
	
	/**
	 * The actual value of the boolean object.
	 */
	private boolean value;
	
	BooleanObject(boolean b) {		
		this.value = b;
	}
	
	@Override
	public boolean isBoolean() {
		return true;
	}

	@Override
	public boolean getBoolean() throws GameObjectException {
		return this.value;
	}

	@Override
	public void setBoolean(boolean b) throws GameObjectException {
		this.value = b;
	}
}
