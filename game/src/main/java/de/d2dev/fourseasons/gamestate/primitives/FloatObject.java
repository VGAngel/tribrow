package de.d2dev.fourseasons.gamestate.primitives;

import de.d2dev.fourseasons.gamestate.*;

public class FloatObject extends GameObjectAdapter {
	
	/**
	 * The actual value of the float object.
	 */
	private float value;
	
	public FloatObject(float f) {
		this.value = f;
	}

	@Override
	public boolean isFloat() {
		return true;
	}

	@Override
	public float getFloat() throws GameObjectException {
		return this.value;
	}

	@Override
	public void setFloat(float f) throws GameObjectException {
		this.value = f;
	}
}
