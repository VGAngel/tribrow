package de.d2dev.fourseasons.files;

import java.io.IOException;

import nu.xom.*;

import de.d2dev.fourseasons.VersionNumber;
import de.schlichtherle.truezip.file.*;

/**
 * Superclass for custom container file formats using Truezip. Validates the file format 
 * by a magic {@code String} contained in a file named magic_file.xml and keeps a {@link VersionNumber}
 * in a file named version.xml.
 * 
 * @author Sebastian Bordt
 *
 */
public abstract class AbstractContainerFile {
	
	protected static final String MAGIC_FILE_NAME = "magic_file.xml";
	protected static final String MAGIC_FILE_ROOT = "magicString";

	protected static final String VERSION_FILE_NAME = "version.xml";
	
	/**
	 * Used to create new empty container. Creates the magic and version file.
	 * @param path
	 * @param magicString
	 * @param version
	 * @return The newly created container file.
	 * @throws IOException
	 */
	protected static TFile createEmptyContainer(String path, String magicString, VersionNumber version) throws IOException {
		// remove a previous file in case it exists
		TFile prev_file = new TFile ( path );
		
		if ( prev_file.exists() )
			prev_file.rm();
		
		// write the magic file
		TFile magic_file = new TFile( path + "/" + AbstractContainerFile.MAGIC_FILE_NAME );
		
		Element xml = new Element( MAGIC_FILE_ROOT );
		xml.appendChild( magicString );
		
		FileUtil.writeXMLToFile( magic_file, new Document(xml) );
		
		// write the version file
		TFile version_file = new TFile( path + "/" + AbstractContainerFile.VERSION_FILE_NAME );
		FileUtil.writeXMLToFile( version_file, version.toXML() );
		
		return new TFile( path );
	}

	/**
	 * The container file.
	 */
	protected TFile file;
	
	/**
	 * The file format version number.
	 */
	protected VersionNumber version;

	/**
	 * Constructor. Looks for the magic file and validates the magic {@code String}.
	 * Reads the version file.
	 * @param path
	 * @throws MagicStringException
	 * @throws IOException
	 * @throws ParsingException
	 */
	protected AbstractContainerFile(String path) throws MagicStringException, IOException, ParsingException {
		this.file = new TFile( path );
		
		// read the magic file and validate the magic string
		TFile magic_file = new TFile( path + "/" + AbstractContainerFile.MAGIC_FILE_NAME );
		
		Document magic_xml = FileUtil.readXMLFromFile(magic_file);
			
		if ( !magic_xml.getRootElement().getValue().equals( this.getMagicString() ) ) {
			throw new MagicStringException( this.getMagicString(), magic_xml.getRootElement().getValue() ); 
		}
		
		// read the version number from version.xml
		TFile version_file = new TFile( path + "/" + AbstractContainerFile.VERSION_FILE_NAME );

		Document version_xml = FileUtil.readXMLFromFile(version_file);
		this.version = new VersionNumber( version_xml );
	}
	
	/**
	 * Getter.
	 * @return
	 */
	public VersionNumber getVersion() {
		return this.version;
	}
	
	/**
	 * A magic string is used to validate/identify container file formats.
	 * @return A magic string unique to the file format.
	 */
	public abstract String getMagicString();
}
