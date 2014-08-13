package de.d2dev.fourseasons.files;

import de.d2dev.fourseasons.FourseasonsException;

public class MagicStringException extends FourseasonsException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MagicStringException(String expected, String was) {
		super( "expected: '" + expected + "', was: '" + was + "'" );
	}
}
