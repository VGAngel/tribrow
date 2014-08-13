package de.d2dev.fourseasons.gamestate.primitives;

import de.d2dev.fourseasons.gamestate.*;

public class IntegerObject extends GameObjectAdapter {
	
	/**
	 * The actual value of the integer object.
	 */
	private int value;
	
	IntegerObject(int i) {
		this.value = i;
	}

	@Override
	public boolean isInteger() {
		return true;
	}

	@Override
	public int getInteger() throws GameObjectException {
		return this.value;
	}

	@Override
	public void setInteger(int i) throws GameObjectException {
		this.value = i;
	}
}
