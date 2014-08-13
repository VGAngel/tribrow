package de.d2dev.fourseasons.network;

import java.net.InetAddress;

import nu.xom.Document;
import nu.xom.Element;

/**
 * Server information consisting of IP, port and XML description.
 * Two instances of {@code ServerDescription} are considered equal if
 * their IP and port are equal.
 * @author Sebastian Bordt
 *
 */
public class ServerDescription {
	
	private InetAddress ip;
	private int port;
	private Document description;
	
	/**
	 * Description will be {@code <?xml version="1.0"?><description></description>}.
	 * @param ip
	 * @param port
	 */
	public ServerDescription(InetAddress ip, int port) {
		this.ip = ip;
		this.port = port;
		
		Element description = new Element("description");
		this.description = new Document( description );
	}
	
	/**
	 * 
	 * @param ip
	 * @param port
	 * @param description
	 */
	public ServerDescription(InetAddress ip, int port, Document description) {
		this.ip = ip;
		this.port = port;
		this.description = description;
	}
	
	/**
	 * Getter.
	 * @return
	 */
	public InetAddress getIp() {
		return this.ip;
	}
	
	/**
	 * Getter.
	 * @return
	 */
	public int getPort() {
		return this.port;
	}
	
	/**
	 * Getter.
	 * @return
	 */
	public String getPortAsString() {
		return Integer.toString( this.port );
	}
	
	/**
	 * Getter.
	 * @return
	 */
	public Document getXMLDesc() {
		return this.description;
	}
	
	@Override
	public String toString() {
		return "ServerDescription [ip=" + ip + ", port=" + port
				+ ", description=" + description.toXML().replace('\n', ' ').replaceAll("\\s\\s+\\s*", "") + "]";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ip == null) ? 0 : ip.hashCode());
		result = prime * result + port;
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
		ServerDescription other = (ServerDescription) obj;
		if (ip == null) {
			if (other.ip != null)
				return false;
		} else if (!ip.equals(other.ip))
			return false;
		if (port != other.port)
			return false;
		return true;
	}
}
