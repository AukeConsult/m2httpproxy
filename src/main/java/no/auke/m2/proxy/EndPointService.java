package no.auke.m2.proxy;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import no.auke.p2p.m2.PeerServer;
import no.auke.p2p.m2.Socket;
import no.auke.p2p.m2.SocketListener;


// proxy service 
public class EndPointService {

	private static final int PEER_PORT = 10;
	
	private PeerServer server;
	
	public PeerServer getPeerServer() {
		return server;
	}

	private Socket peer_socket;
	public Socket getPeerSocket() {
		return peer_socket;
	}

	private ConcurrentHashMap<String,EndPointRequest> requests; 
	
	private String defaultEndPoint;
	
	public EndPointService (PeerServer server, String defaultEndPoint) {
				
		this.server = server;
		this.defaultEndPoint=defaultEndPoint;
		
		final EndPointService me = this;
		
		peer_socket = server.open(PEER_PORT, new SocketListener(){

			@Override
			public void onIncomming(byte[] buffer) {

				RequestMsg msg = new RequestMsg(buffer);
				
				if(!requests.containsKey(msg.getReplyTo())) {
					
					EndPointRequest request = new EndPointRequest(me,msg.getSession());
					requests.put(msg.getReplyTo(), request);
					
				}
				
				requests.get(msg.getReplyTo()).gotRequest(msg);
				
			}});

    	System.out.println("Started endpoint");
            
		
	}	
	
	// get a remote party
	public String getRemote(SocketAddress localSocketAddress) {

		return defaultEndPoint;
	}

}
