package de.d2dev.fourseasons;

import java.io.*;

import javax.swing.UIManager;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.SystemUtils;

public class Application {
	
	public static String getPublicStoragePath(String appName) {
		return System.getProperty( "user.home" ) +  "/" + appName;
	}
	
	/**
	 * Tell swing to use native look and feal!
	 */
	public static void useNativeLookAndFeal() {
    	try {  
    	  //Tell the UIManager to use the platform look and feel  
    	  UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());  
    	}  catch(Exception e) {  
    	  //Do nothing  
    	} 
	}
	
	/**
	 * Get the location of the dropbox folder. 
	 * See https://www.dropbox.com/developers/desktop_apps
	 * @return {@code null} if not found.
	 */
	public static String getFileSystemDropbox() {
		// Determine the location of the host.db (currently only windows support  ~/.dropbox/host.db for linux/mac)
		String host_db_path = "";
		
		if ( SystemUtils.IS_OS_WINDOWS ) {	
			host_db_path = System.getenv( "APPDATA" )+ "/Dropbox/host.db";
		}
		
		// open the host.db and read the location
		File host_db;
		
		if ( !(host_db = new File( host_db_path ) ).exists() )
			return null;
		
		FileInputStream in = null;
		try{
			  in = new FileInputStream( host_db );
			  BufferedReader br = new BufferedReader( new InputStreamReader(in) );
			  
			  // read the first line
			  br.readLine();
			  
			  // the location is decoded in the second line
			  return new String( Base64.decodeBase64(br.readLine() ) );
		} catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			// always close the input stream
			if ( in != null ) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}	
			}
		}
		
		// not found :-(
		return null;
	}
}
