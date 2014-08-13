package de.d2dev.fourseasons.network.tests;

import static org.junit.Assert.*;

import java.io.StringReader;
import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.Future;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;

import org.junit.Before;
import org.junit.Test;

import com.ning.http.client.Response;

import de.d2dev.fourseasons.network.ConnectionServerListener;
import de.d2dev.fourseasons.network.HttpConnectionServer;
import de.d2dev.fourseasons.network.ServerDescription;


public class HttpConnectionServerTest {
	
	private int addServerCalls;
	private int removeServerCalls;
	public HttpConnectionServer server;
	
	@Before
	public void init() {
		this.addServerCalls = 0;
		this.removeServerCalls = 0;
		server = null;
	}
	
	@Test
	public void getServers() throws Exception {
		// Check the data we send
		server = new HttpConnectionServer("http://home.in.tum.de/~bordt/d2dev/tests/postReflection.php");
		Response response = server.sendGetServersRequest().get();
		
		Builder parser = new Builder();
		Document doc = parser.build( new StringReader( response.getResponseBody() ) );
		
		assertEquals( "connectionServerRequest", doc.getRootElement().getLocalName() );
		assertEquals( 1, doc.getRootElement().getChildElements("getServers").size() );
		
		// Check if we receive the servers correctly
		server = new HttpConnectionServer("https://home.in.tum.de/~bordt/d2dev/tests/connectionServer/dummyConnectionServer.php");
		response = server.sendGetServersRequest().get();
		
		List<ServerDescription> servers = server.getServers();
		assertEquals( 2, servers.size() );
	
		System.out.println( servers.get(0).getIp() );
		System.out.println( servers.get(0).getPort() );
		System.out.println( servers.get(0).getXMLDesc().toXML() );
		
		System.out.println( servers.get(1).getIp() );
		System.out.println( servers.get(1).getPort() );
		System.out.println( servers.get(1).getXMLDesc().toXML() );
		
		// Check if nothing changes on a second request
		server.sendGetServersRequest().get();
		
		servers = server.getServers();
		assertEquals( 2, servers.size() );
	}
	
	@Test
	public void addServer() throws Exception {
		// Check the data we send
		server = new HttpConnectionServer("http://home.in.tum.de/~bordt/d2dev/tests/postReflection.php");
		
		Element xml = new Element("customData");
		Element data = new Element("data");
		data.appendChild("Irgendwelche Daten!");
		xml.appendChild(data);
		
		Response response = server.sendAddServerRequest( new ServerDescription(InetAddress.getByName( "193.99.144.71" ), 1000, new Document(xml) ) ).get();
		
		System.out.println(response.getResponseBody());
		
		Builder parser = new Builder();
		Document doc = parser.build( new StringReader( response.getResponseBody() ) );
		
		assertEquals( "connectionServerRequest", doc.getRootElement().getLocalName() );
		
		Element addServer = doc.getRootElement().getChildElements("addServer").get(0);
		
		Element ip = addServer.getChildElements("ip").get(0);
		assertEquals( InetAddress.getByName( "193.99.144.71" ), InetAddress.getByName(ip.getValue()) );
		
		Element port = addServer.getChildElements("port").get(0);
		assertEquals( "1000", port.getValue() );
		
		assertEquals( 1, addServer.getChildElements("description").size() );
		assertEquals( 1, addServer.getChildElements("description").get(0).getChildElements("customData").size() );
	}
	
	@Test
	public void removeServer() throws Exception {
		// Check the data we send
		server = new HttpConnectionServer("http://home.in.tum.de/~bordt/d2dev/tests/postReflection.php");
		Response response = server.sendRemoveServerRequest( new ServerDescription(InetAddress.getByName( "193.99.144.71" ), 1000) ).get();
		
		Builder parser = new Builder();
		Document doc = parser.build( new StringReader( response.getResponseBody() ) );
		
		assertEquals( "connectionServerRequest", doc.getRootElement().getLocalName() );
		
		Element addServer = doc.getRootElement().getChildElements("removeServer").get(0);
		
		Element ip = addServer.getChildElements("ip").get(0);
		assertEquals( InetAddress.getByName( "193.99.144.71" ), InetAddress.getByName(ip.getValue()) );
		
		Element port = addServer.getChildElements("port").get(0);
		assertEquals( "1000", port.getValue() );
	}	
	
	@Test
	public void listener() throws Exception {
		server = new HttpConnectionServer("https://home.in.tum.de/~bordt/d2dev/tests/connectionServer/dummyConnectionServer.php");
		server.addListener( new ConnectionServerListener() {
			
			@Override
			public void onAddServer(ServerDescription server) {
				HttpConnectionServerTest.this.addServerCalls++;
				
				// Test accessing the servers list
				System.out.println( HttpConnectionServerTest.this.server.getServers().size() );
			}

			@Override
			public void onRemoveServer(ServerDescription server) {
				HttpConnectionServerTest.this.removeServerCalls++;
			}
		});
		
		server.sendGetServersRequest().get();
		
		Thread.sleep(1000);	// w8 for listener
		
		assertEquals( 2, this.addServerCalls );
		assertEquals( 0, this.removeServerCalls );
		
		// Check that we don't get notifications on a second request
		server.sendGetServersRequest().get();
		
		assertEquals( 2, this.addServerCalls );
		assertEquals( 0, this.removeServerCalls );		
	}
	
	@Test
	public void multipleGetServers() throws Exception {
		server = new HttpConnectionServer("https://home.in.tum.de/~bordt/d2dev/tests/connectionServer/dummyConnectionServer.php");
		Future<Response> r = server.sendGetServersRequest();
		
		assertEquals( r, server.sendGetServersRequest() );		// no new request!
		r.get();
		
		assertFalse( r == server.sendGetServersRequest() );	// new request!
	}
}
