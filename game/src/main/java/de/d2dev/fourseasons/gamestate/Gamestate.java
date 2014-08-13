package de.d2dev.fourseasons.gamestate;

public class Gamestate {
	
	/**
	 * Like {@link com.google.common.base.Preconditions}, only for our game state. 
	 * @param expression
	 * @param errorMessage
	 * @throws GameStateException
	 */
	public static void checkState(boolean expression, String errorMessage) throws GameStateException {
		if ( !expression )
			throw new GameStateException( errorMessage );
	}
}
