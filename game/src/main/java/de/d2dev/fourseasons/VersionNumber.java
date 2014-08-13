package de.d2dev.fourseasons;

import nu.xom.*;

/**
 * Represents a version consisting of a major and minor version, e.g. 17.542.
 * @author Sebastian Bordt
 *
 */
public class VersionNumber {
	
	private static final String XML_ROOT = "versionNumber";
	private static final String MAJOR = "major";
	private static final String MINOR = "minor";
	
	private int major;
	private int minor;
	
	public VersionNumber(int major, int minor) {
		this.major = major;
		this.minor = minor;
	}
	
	/**
	 * Read from xml.
	 * @param xml
	 */
	public VersionNumber(Document xml) {
		this.major = Integer.valueOf( xml.getRootElement().getChildElements( MAJOR ).get(0).getValue() );
		this.minor = Integer.valueOf( xml.getRootElement().getChildElements( MINOR ).get(0).getValue() );
	}
	
	public int getMajorVersion() {
		return this.major;
	}
	
	public int getMinorVersion() {
		return this.minor;
	}
	
	/**
	 * Serialize to xml.
	 * @return
	 */
	public Document toXML() {
		Element xml_root = new Element( XML_ROOT );
		
		Element major = new Element( MAJOR );
		major.appendChild( Integer.toString(this.major) );
		xml_root.appendChild(major);
		
		Element minor = new Element( MINOR );
		minor.appendChild( Integer.toString(this.minor) );
		xml_root.appendChild(minor);
		
		return new Document(xml_root);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + major;
		result = prime * result + minor;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VersionNumber other = (VersionNumber) obj;
		if (major != other.major)
			return false;
		if (minor != other.minor)
			return false;
		return true;
	}
}
