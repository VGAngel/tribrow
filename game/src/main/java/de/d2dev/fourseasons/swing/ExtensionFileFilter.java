package de.d2dev.fourseasons.swing;

import java.io.File;
import java.util.regex.Pattern;

import javax.swing.filechooser.FileFilter;

/**
 * FileFilter applying a regex to the files extension.
 * @author Sebastian Bordt
 *
 */
public class ExtensionFileFilter extends FileFilter {
	
	Pattern pattern;
	String description;
	
	/**
	 * 
	 * @param regex regex
	 * @param description
	 */
	public ExtensionFileFilter(String regex, String description) {
		this.pattern = Pattern.compile(regex);
		this.description = description;
	}

	@Override
	public boolean accept(File file) {
		
		if ( file.isDirectory() )	// accept directories
			return true;
		
		String name = file.getName();
		int i = name.lastIndexOf('.');
		
		if ( i == -1 )	// do not accept that
			return false;
		
		return this.pattern.matcher( name.substring( i+1, name.length() ) ).matches();
	}

	@Override
	public String getDescription() {
		return description;
	}

}
