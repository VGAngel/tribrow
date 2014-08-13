package de.d2dev.fourseasons.gamestate;

import de.d2dev.fourseasons.FourseasonsException;

/**
 * Thrown when a method call to a {@link GameObject} does not meet that objects type
 * (Composite Pattern).
 * @author Sebastian Bordt
 *
 */
public class GameObjectException extends FourseasonsException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public GameObjectException(GameObject source, String Message) {
		
	}

}
