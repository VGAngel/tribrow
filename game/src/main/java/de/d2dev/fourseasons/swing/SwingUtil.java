package de.d2dev.fourseasons.swing;

import java.awt.Color;
import java.awt.Component;
import java.io.File;

import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class SwingUtil {
	
	public static boolean verifyIntegerTextField(JTextField field) {
		try {
			Integer.valueOf( field.getText() ); 
			field.setBackground( Color.WHITE );
		} catch (NumberFormatException e) {
			field.setBackground( Color.LIGHT_GRAY );
			return false;
		}
		
		return true;
	}
	
	/**
	 * Show a dialog that asks the user if a certain file should be overwritten. Nothing
	 * will happen if the given file does not exist.
	 * @param parent
	 * @param file File to be overwritten.
	 * @return {@code true} if the user confirms or if the file does not exist.
 	 */
	public static boolean confirmFileWriting(Component parent, File file) {
		if ( !file.exists() )
			return true;
			
		return JOptionPane.showConfirmDialog( parent, 
				"The file '" + file.getName() + "' does already exist. Overwrite?", "Confirm", JOptionPane.YES_NO_OPTION ) == JOptionPane.YES_OPTION;
	}
}
