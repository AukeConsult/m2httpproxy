package no.auke.m2.proxy;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import no.auke.p2p.m2.PeerServer;
import no.auke.p2p.m2.SocketListener;

// proxy service 
public class EndPointService implements Runnable {

	private static final int PEER_PORT = 10;
	
	private PeerServer server;
	
	public PeerServer getPeerServer() {
		return server;
	}

	private no.auke.p2p.m2.Socket peer_socket;
	public no.auke.p2p.m2.Socket getPeerSocket() {
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
				
				if(!requests.containsKey(msg.getAddress())) {
					
					// open a request session for each different host endpoint
					
					EndPointRequest request = new EndPointRequest(me,msg.getHost(),msg.getPort());
					
					requests.put(msg.getAddress(), request);
					getPeerServer().getExecutor().execute(request);
				
					
				}
				
				requests.get(msg.getAddress()).gotRequest(msg);
				
			}});

    	System.out.println("Started endpoint");
            
		
	}	
	
	// get a remote party
	public String getRemote(SocketAddress localSocketAddress) {

		return defaultEndPoint;
	}

	@Override
	public void run() {

		// loop and clean up
		
		while(true) {
			
			// remove completed requests
        	ArrayList<EndPointRequest> openRequests = new ArrayList<EndPointRequest>(requests.values());
        	for(EndPointRequest check:openRequests){
        	
        		if(check.isComplete()) {
        			
        			requests.remove(check.getAddress());
        			
        		
        		}
        		
        	}
			
			
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}

}
