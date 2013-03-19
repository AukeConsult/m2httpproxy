package no.auke.m2.proxy;

import java.util.concurrent.ConcurrentHashMap;

import no.auke.m2.proxy.dataelements.Neighbor;
import no.auke.m2.proxy.dataelements.RequestMsg;
import no.auke.p2p.m2.PeerServer;
import no.auke.p2p.m2.Socket;
import no.auke.p2p.m2.SocketListener;

public class NeighBorhodService implements Runnable {
	
	private PeerServer server;	
	public PeerServer getServer() {
	
		return server;
	}

	private ConcurrentHashMap<String,Neighbor> neigbors; 
	
	private Socket peer_socket;
	public Socket getPeerSocket() {
		return peer_socket;
	}
	
	String defaultendpoint="";
	public NeighBorhodService(PeerServer server, String defaultendpoint){
		
		this.defaultendpoint=defaultendpoint;
		this.server=server;
		
		peer_socket = server.open(ServerParams.NEIGTBOR_SERVICE_PORT, new SocketListener(){

			@Override
			public void onIncomming(byte[] buffer) {

				
			
			}});
		
	}
	
	@Override
	public void run() {

		while(true) {
			
			// loop and organize neighbors
					
			
		}
		
	}

	// get the current endpoint for the address 
	public String getRemoteEndPoint(String address) {

		return defaultendpoint;
	
	}
	
	// reset the remote end point for this address 
	public void resetRemoteEndPoint(String address) {

		
	}	

	// add an incoming for the list
	public void addNeighbor(RequestMsg msg) {
		
		if(!neigbors.containsKey(msg.getReplyTo())) {
			
			neigbors.put(msg.getReplyTo(), new Neighbor(msg.getReplyTo()));
			
		}
		
	}

	// setting neighbor not alive
	public void setNotAlive(String clientid) {

		if(neigbors.containsKey(clientid)) {
			
			neigbors.get(clientid).setAlive(false);
			
		}
		
	}

}