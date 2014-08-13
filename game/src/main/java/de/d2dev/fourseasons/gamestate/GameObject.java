package de.d2dev.fourseasons.gamestate;

//import java.util.List;
//
//import de.d2dev.fourseasons.gamestate.event.*;

/**
 * Superinterface for all game objects. That are logical entities of the game.
 * Applies the composite Pattern.
 * @author Sebastian Bordt
 *
 */
public interface GameObject {
	
	//public void addGameEventListener(GameEventDescription e, GameEventListener l);
	
	//public List<GameEventDescription> getGameEvents();
		
	public boolean isBoolean();
	public boolean getBoolean() throws GameObjectException;
	public void setBoolean(boolean b) throws GameObjectException;
	
	public boolean isInteger();
	public int getInteger() throws GameObjectException;
	public void setInteger(int i) throws GameObjectException;	
	
	public boolean isDouble();
	public double getDouble() throws GameObjectException;
	public void setDouble(double d) throws GameObjectException;
	
	public boolean isFloat();
	public float getFloat() throws GameObjectException;
	public void setFloat(float f) throws GameObjectException;
	
	public boolean isString(); 
	public String getString() throws GameObjectException;
	public void setString(String s) throws GameObjectException;
}
