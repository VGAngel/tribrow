package de.d2dev.fourseasons.files;

import java.io.IOException;

import nu.xom.*;

import de.schlichtherle.truezip.file.*;

public class FileUtil {
	
	/**
	 * Helper to write a {@link nu.xml.Document} to a {@link de.schlichtherle.truezip.file.TFile}.
	 * @param file
	 * @param xml
	 * @throws IOException
	 */
	public static void writeXMLToFile(TFile file, Document xml) throws IOException {
		TFileOutputStream out = new TFileOutputStream( file );
		
		try {
			Serializer serializer = new Serializer( out );
			serializer.write( new Document( xml ) );
		} finally {
		    out.close(); // ALWAYS close the stream!
		}
	}
	
	/**
	 *  Helper to write a {@link nu.xml.Document} to file.
	 * @param path
	 * @param xml
	 * @throws IOException
	 */
	public static void writeXMLToFile(String path, Document xml) throws IOException {
		FileUtil.writeXMLToFile( new TFile( path ), xml );
	}
	
	/**
	 * Helper to read a {@link nu.xml.Document} from a {@link de.schlichtherle.truezip.file.TFile}.
	 * @param file
	 * @return
	 * @throws IOException
	 * @throws ParsingException
	 */
	public static Document readXMLFromFile(TFile file) throws IOException, ParsingException {
		TFileInputStream in = new TFileInputStream( file );
		
		try {
			Builder builder = new Builder();
			return builder.build( in );
		} finally {
		    in.close(); // ALWAYS close the stream!
		}
	}
	
	/**
	 * Helper to read a {@link nu.xml.Document} from a file.
	 * @param path
	 * @return
	 * @throws IOException
	 * @throws ParsingException
	 */
	public static Document readXMLFromFile(String path) throws IOException, ParsingException {
		return FileUtil.readXMLFromFile( new TFile( path) );
	}
	
	/**
	 * Helper to read an {@code int} attribute from a {@link nu.xml.Element} .
	 * @param element
	 * @param name
	 * @return
	 */
	public static int readIntAttribute(Element element, String name) {
		Attribute att = element.getAttribute( name );
		
		if ( att == null ) throw new NoSuchAttributeException( "element:'" + element.getBaseURI() + "' attribute:'" + name +"'" );
		
		return Integer.valueOf( att.getValue() );
	}
	
	
	/**
	 * Helper to read a {@code boolean} attribute from a {@link nu.xml.Element} .
	 * @param element
	 * @param name
	 * @return
	 */
	public static boolean readBoleanAttribute(Element element, String name) {
		Attribute att = element.getAttribute( name );
		
		if ( att == null ) throw new NoSuchAttributeException( "element:'" + element.getBaseURI() + "' attribute:'" + name +"'" );
		
		return Boolean.valueOf( att.getValue() );
	}
	
	/**
	 * Helper to read a {@code String} attribute from a {@link nu.xml.Element} .
	 * @param element
	 * @param name
	 * @return
	 */
	public static String readStringAttribute(Element element, String name) {
		Attribute att = element.getAttribute( name );
		
		if ( att == null ) throw new NoSuchAttributeException( "element:'" + element.getBaseURI() + "' attribute:'" + name +"'" );
		
		return att.getValue();
	}
}
