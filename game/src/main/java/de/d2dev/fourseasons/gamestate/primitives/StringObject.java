package de.d2dev.fourseasons.gamestate.primitives;

import de.d2dev.fourseasons.gamestate.*;

public class StringObject extends GameObjectAdapter {
	
	/**
	 * The actual value of the string object.
	 */
	private String value;
	
	StringObject(String s) {		
		this.value = s;
	}

	@Override
	public boolean isString() {
		return true;
	}

	@Override
	public String getString() throws GameObjectException {
		return this.value;
	}

	@Override
	public void setString(String s) throws GameObjectException {
		this.value = s;
	}
}
