package de.d2dev.fourseasons.gamestate.primitives;

import de.d2dev.fourseasons.gamestate.*;

public class DoubleObject extends GameObjectAdapter {

	/**
	 * The actual value of the double object.
	 */
	double value;
	
	DoubleObject(double d) {
		this.value = d;
	}
	
	@Override
	public boolean isDouble() {
		return true;
	}

	@Override
	public double getDouble() throws GameObjectException {
		return this.value;
	}

	@Override
	public void setDouble(double d) throws GameObjectException {
		this.value = d;
	}
}
