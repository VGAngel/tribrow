package de.d2dev.fourseasons.gamestate;

/**
 * Adapter class for our game object.
 * @author Sebastian Bordt
 *
 */
public class GameObjectAdapter implements GameObject {

	@Override
	public boolean isBoolean() {
		return false;
	}

	@Override
	public boolean getBoolean() throws GameObjectException {
		throw new GameObjectException( this, "This game object is not a boolean." );
	}

	@Override
	public void setBoolean(boolean b) throws GameObjectException {
		throw new GameObjectException( this, "This game object is not a boolean." );
	}

	@Override
	public boolean isInteger() {
		return false;
	}

	@Override
	public int getInteger() throws GameObjectException {
		throw new GameObjectException( this, "This game object is not an integer." );
	}

	@Override
	public void setInteger(int i) throws GameObjectException {
		throw new GameObjectException( this, "This game object is not an integer." );
	}

	@Override
	public boolean isDouble() {
		return false;
	}

	@Override
	public double getDouble() throws GameObjectException {
		throw new GameObjectException( this, "This game object is not a double." );
	}

	@Override
	public void setDouble(double d) throws GameObjectException {
		throw new GameObjectException( this, "This game object is not a double." );	
	}

	@Override
	public boolean isFloat() {
		return false;
	}

	@Override
	public float getFloat() throws GameObjectException {
		throw new GameObjectException( this, "This game object is not a float." );
	}

	@Override
	public void setFloat(float f) throws GameObjectException {
		throw new GameObjectException( this, "This game object is not a float." );
	}

	@Override
	public boolean isString() {
		return false;
	}

	@Override
	public String getString() throws GameObjectException {
		throw new GameObjectException( this, "This game object is not a string." );
	}

	@Override
	public void setString(String s) throws GameObjectException {
		throw new GameObjectException( this, "This game object is not a string." );
	}
}
