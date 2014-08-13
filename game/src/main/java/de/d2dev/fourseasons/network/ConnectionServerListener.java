package de.d2dev.fourseasons.network;

/**
 * Listen to {@link HttpConnectionServer}.
 * @author Sebastian Bordt
 *
 */
public interface ConnectionServerListener {
	
	/**
	 * This method is being called AFTER the server has been added.
	 * @param server
	 */
	public void onAddServer(ServerDescription server);
	
	/**
	 * This method is being called right BEFORE the server is being removed.
	 * @param server
	 */
	public void onRemoveServer(ServerDescription server);
}
