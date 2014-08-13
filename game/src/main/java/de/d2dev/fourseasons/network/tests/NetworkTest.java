package de.d2dev.fourseasons.network.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import de.d2dev.fourseasons.network.Network;


public class NetworkTest {
	
	@Test
	public void ipv4() {
		assertTrue( Network.isIPv4Address("127.0.0.1") );
		assertTrue( Network.isIPv4Address("66.77.5.99") );
		assertFalse( Network.isIPv4Address("66.77.5.256") );
		assertFalse( Network.isIPv4Address("66.77.5") );
		assertFalse( Network.isIPv4Address("66.77.5d") );
	}
	
	@Test
	public void ipv6() {
	}
}
