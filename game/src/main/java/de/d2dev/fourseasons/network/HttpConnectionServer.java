package de.d2dev.fourseasons.network;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Future;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;

import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.RequestBuilder;
import com.ning.http.client.Response;

/**
 * This class performs the java-side communication with a HTTP connection server.
 * The class provides three methods to send the three types of requests: {@link #sendGetServersRequest()},
 * {@link #sendAddServerRequest(ServerDescription)} and {@link #sendRemoveServerRequest(ServerDescription)}.
 * The requests execute asynchronously, i.e. non-blocking. There is an internal list caching
 * the servers obtained by the last request, it can be retrieved calling {@link #getServers()}. Please
 * note that no implicit getServers requests will be send, so the list will initially be empty.
 * If an addServer or removeServer requests succeeds, the server will manually be added to/removed
 * from the internal list as necessary. 
 * @author Sebastian Bordt
 *
 */
public class HttpConnectionServer {
	
	private static final String CONNECTION_SERVER_REQUEST = "connectionServerRequest";
	private static final String CONNECTION_SERVER_RESPONSE = "connectionServerResponse";
	private static final String SUCCEEDED = "succeeded";
	// not needed: private static final String FAILED = "failed";
	private static final String GET_SERVERS = "getServers";
	private static final String SERVERS = "servers";
	private static final String SERVER = "server";
	private static final String IP = "ip";
	private static final String PORT = "port";
	private static final String DESCRIPTION = "description";
	private static final String ADD_SERVER = "addServer";
	private static final String REMOVE_SERVER = "removeServer";
	
	/**
	 * URL of the connection server we are talking to (e.g. http://home.in.tum.de/~bordt/d2dev/connectionServer/connectionServer.php).
	 */
	private String url = new String();
	
	/**
	 * Synchronized {@code List} caching the servers obtained by the last request.
	 */
	private List<ServerDescription> servers = Collections.synchronizedList( new Vector<ServerDescription>() );
	
	/**
	 * Unmodifiable view on the servers.
	 */
	private List<ServerDescription> unmfServers;
	
	/**
	 * Listeners.
	 */
	private Vector<ConnectionServerListener> listeners = new Vector<ConnectionServerListener>();
	
	/**
	 * Our http client.
	 */
	private AsyncHttpClient http = new AsyncHttpClient();
	
	/**
	 * Pending response from a getServers request. {@code null} in case of no
	 * pending getServers response. Used to keep a maximum of one pending
	 * getServers request.
	 */
	private Future<Response> getServersResponse = null;
	
	/**
	 * Add a server to our internal list and notify listeners.
	 * Server must not be in the list.
	 * @param server
	 */
	private void addServerToList(ServerDescription server) {
		this.servers.add( server );
		this.fireAddServer( server );
	}
	
	/**
	 * Remove a server from our internal list and notify listeners.
	 * Server must be in the list.
	 * @param server
	 */
	private void removeServerFromList(ServerDescription server) {
		this.fireRemoveServer( server );
		this.servers.remove( server );		
	}
	
	/**
	 * Determine if a request succeeded (As according to the http connection server specification).
	 * @param response
	 * @return
	 */
	public static boolean succeeded(Response response) {
		try {
			Builder builder = new Builder();
        	Document doc = builder.build( response.getResponseBodyAsStream() );
        	
        	if ( doc.getRootElement().getLocalName() != CONNECTION_SERVER_RESPONSE ) return false;
        	if ( doc.getRootElement().getChildElements().get(0).getLocalName() != SUCCEEDED ) return false;
		} catch (Exception e) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Determine if a request failed (As according to the http connection server specification).
	 * @param response
	 * @return
	 */
	public static boolean failed(Response response) {
		return !HttpConnectionServer.succeeded(response);
	}
	
	/**
	 * 
	 * @param url URL of the connection server we want to talk to. Set on construction
	 * and never changes.
	 */
	public HttpConnectionServer(String url) {
		unmfServers = Collections.unmodifiableList( this.servers );
		
		this.url = url;
	}
	
	public void addListener(ConnectionServerListener l) {
		if ( !this.listeners.contains( l ) ) {
			this.listeners.add( l );
		}
	}
	
	public void removeListener(ConnectionServerListener l) {
		this.listeners.remove( l );
	}
	
	private void fireAddServer(ServerDescription server) {
		for(ConnectionServerListener l : this.listeners) {
			l.onAddServer(server);
		}
	}
	
	private void fireRemoveServer(ServerDescription server) {
		for(ConnectionServerListener l : this.listeners) {
			l.onRemoveServer(server);
		}
	}
	
	/**
	 * Getter.
	 * @return An unmodifiable and synchronized {@code List} containing the servers as obtained
	 * by the last request ({@code Collections.unmodifiableList} and {@code Collections.synchronizedList}).
	 * Make sure to manually synchronize when iterating. List will be empty if no request to obtain
	 * the servers has been send yet.
	 */
	public List<ServerDescription> getServers() {
		return this.unmfServers;
	}
	
	/**
	 * Determines if there is a pending getServers request.
	 * @return {@code true} if there is one.
	 */
	public boolean pendingGetServers() {
		synchronized ( this ) {
			return this.getServersResponse != null;
		}
	}
	
	/**
	 * Send a getServers request to the connection server. If there is a pending
	 * getServers request, nothing will happen.
	 * @return The future response to the request. The future response to a 
	 * pending request in case of a pending request.
	 * @throws IOException
	 */
	public Future<Response> sendGetServersRequest() throws IOException {
		// Is there a pending request? We synchronize manually as the response might just arrive!
		synchronized ( this ) {
			if ( this.getServersResponse != null )
				return this.getServersResponse;
		}
		
		// Build a request
		RequestBuilder builder = new RequestBuilder( "POST" );
		builder.setUrl(this.url);
		
		// Build the XML to be send
		Element root = new Element( CONNECTION_SERVER_REQUEST );
		Element getServers = new Element( GET_SERVERS );
		root.appendChild(getServers);
		Document doc = new Document(root);
		builder.setBody( doc.toXML() );
		
		// Execute the request
		this.getServersResponse = this.http.executeRequest( builder.build(), new AsyncCompletionHandler<Response>() {
			
	        @Override
	        public Response onCompleted(Response response) {
	        	HashSet<ServerDescription> servers_received = new HashSet<ServerDescription>();
	        	
	        	// Parse the returned XML data
	        	try {  	
		        	Builder builder = new Builder();
		        	Document doc = builder.build( response.getResponseBodyAsStream() );
		        	
		        	Elements servers = doc.getRootElement().getChildElements( SERVERS ).get(0).getChildElements( SERVER );
		        	for (int i=0; i < servers.size(); i++) {
		        		Element server = servers.get(i);
		        		
		        		String ip = server.getChildElements( IP ).get(0).getValue();
		        		int port = Integer.decode( server.getChildElements( PORT ).get(0).getValue() );
		        		
		        		Document description = new Document( (Element) server.getChildElements( DESCRIPTION ).get(0).copy() );
		        		
		        		servers_received.add( new ServerDescription( InetAddress.getByName(ip), port, description) );
		        	}

	        	} catch (Exception e) {
	        		// TODO error msg
	        		return response;
	        	}
	        	
	        	// Remove the servers that are in the current list but are no longer being listed by the connection server.
	        	// Add the servers that are not in the current list but listed by the connection server.
	        	// We determine what to remove/add and then remove/add server by server to avoid deadlocks due to the fact
	        	// that we are calling our listeners here. They might perform some (read) operations on the servers list.
	        	HashSet<ServerDescription> remove;
	        	
	        	synchronized (HttpConnectionServer.this.servers) {        		
	        		remove = new HashSet<ServerDescription>( HttpConnectionServer.this.servers );
	        		remove.removeAll( servers_received );
	        		
	        		servers_received.removeAll( HttpConnectionServer.this.servers );
	        	}
	        	
	        	// Remove
	        	for(ServerDescription server : remove) {
	        		HttpConnectionServer.this.removeServerFromList(server);
	        	}
	        	
	        	// Add
	        	for(ServerDescription server : servers_received) {
	        		HttpConnectionServer.this.addServerToList(server);
	        	}       
	        	
	        	// The response has been handled, there is no longer a pending request!
	        	synchronized( HttpConnectionServer.this ) {
	        		HttpConnectionServer.this.getServersResponse = null;
	        	}
	   
	        	return response;
	        }
	    });
		
		return this.getServersResponse;
	}
	
	/**
	 * Add a server.
	 * @param server
	 * @return
	 * @throws IOException
	 */
	public Future<Response> sendAddServerRequest(final ServerDescription server) throws IOException {
		// Build a request
		RequestBuilder builder = new RequestBuilder( "POST" );
		builder.setUrl(this.url);
		
		// Build the XML to be send (ip, port, description)
		Element root = new Element( CONNECTION_SERVER_REQUEST );
		Element addServer = new Element( ADD_SERVER );
		
		Element ip = new Element( IP );
		ip.appendChild( server.getIp().getHostAddress() );
		addServer.appendChild(ip);
		
		Element port = new Element( PORT );
		port.appendChild( server.getPortAsString() );
		addServer.appendChild(port);
		
		Element desc = new Element( DESCRIPTION );
		desc.appendChild( server.getXMLDesc().getRootElement().copy() );
		addServer.appendChild(desc);
		
		root.appendChild(addServer);
		
		Document doc = new Document(root);
		builder.setBody( doc.toXML() );
		
		// Execute the request
		return this.http.executeRequest( builder.build(), new AsyncCompletionHandler<Response>() {
			
				private ServerDescription toAdd;
				{toAdd = server;}
			
		        @Override
		        public Response onCompleted(Response response) {
		        	
		        	// If the server we just added is not contained in our internal list, add it!
		        	if ( HttpConnectionServer.succeeded(response) ) {
		        		synchronized( HttpConnectionServer.this.servers ) {
		        			if ( !HttpConnectionServer.this.servers.contains( toAdd ) ) {
		        				HttpConnectionServer.this.addServerToList(server);
		        			}
		        		}
		        	}
		   
		        	return response;	        	
		        }
		});
	}
	
	/**
	 * Remove a server from the connection server.
	 * @param server
	 * @return
	 * @throws IOException
	 */
	public Future<Response> sendRemoveServerRequest(final ServerDescription server) throws IOException {
		// Build a request
		RequestBuilder builder = new RequestBuilder( "POST" );
		builder.setUrl(this.url);
		
		// Build the XML to be send (ip, port)
		Element root = new Element( CONNECTION_SERVER_REQUEST );
		Element removeServer = new Element( REMOVE_SERVER );
		
		Element ip = new Element( IP );
		ip.appendChild( server.getIp().getHostAddress() );
		removeServer.appendChild(ip);
		
		Element port = new Element( PORT );
		port.appendChild( server.getPortAsString() );
		removeServer.appendChild(port);
		
		root.appendChild(removeServer);
		
		Document doc = new Document(root);
		builder.setBody( doc.toXML() );
		
		// Execute the request
		return this.http.executeRequest( builder.build(), new AsyncCompletionHandler<Response>() {
			
			private ServerDescription toRemove;
			{toRemove = server;}
		
	        @Override
	        public Response onCompleted(Response response) {
	        	
	        	// If the server we just removed is contained in our internal list, add it!
	        	if ( HttpConnectionServer.succeeded(response) ) {
	        		synchronized( HttpConnectionServer.this.servers ) {
	        			if ( HttpConnectionServer.this.servers.contains( toRemove ) ) {
	        				HttpConnectionServer.this.removeServerFromList(server);
	        			}
	        		}
	        	}
	   
	        	return response;	        	
	        }
        });
	}
	
	/**
	 * Remove all known servers (i.e. the servers retrieved by {@link #getServers}).
	 * This method only sends request, so the effect is not immediately.
	 * @throws IOException
	 */
	public void removeAllServers() throws IOException {
		synchronized( this.servers ) {
			for (ServerDescription s : this.servers ) {
				this.sendRemoveServerRequest(s);
			}
		}
	}
}
