package no.auke.m2.proxy.services;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.auke.m2.proxy.ServerParams;
import no.auke.m2.proxy.dataelements.RequestMsg;
import no.auke.m2.proxy.request.EndPointRequest;
import no.auke.p2p.m2.PeerServer;
import no.auke.p2p.m2.SocketListener;

// proxy service 
public class EndPointService implements Runnable {
	
	private static final Logger logger = LoggerFactory.getLogger(EndPointService.class);	

	private PeerServer server;
	private NeighBorhodService neighbors=null;

	public PeerServer getPeerServer() {
		return server;
	}

	private no.auke.p2p.m2.Socket peer_socket;
	public no.auke.p2p.m2.Socket getPeerSocket() {
		return peer_socket;
	}

	private ConcurrentHashMap<String,EndPointRequest> requests; 
	
	public EndPointService (PeerServer server, NeighBorhodService neighbors) {
				
		this.server = server;
		this.neighbors=neighbors;

		final EndPointService me = this;
		
		
		peer_socket = this.server.open(ServerParams.HTTP_SERVICE_PORT, new SocketListener(){
			
			@Override
			public void onIncomming(byte[] buffer) {

				RequestMsg msg = new RequestMsg(buffer);

		    	logger.debug(" > request message " + msg.getReplyTo() + " for " + msg.getAddress());

				if(!requests.containsKey(msg.getAddress())) {
					
					me.neighbors.addNeighbor(msg);
					
					// open a request session for each different host endpoint
					
					EndPointRequest request = new EndPointRequest(me,msg.getHost(),msg.getPort());
					
					requests.put(msg.getAddress(), request);
					getPeerServer().getExecutor().execute(request);
				
					
				}
				
				requests.get(msg.getAddress()).gotRequest(msg);
				
			}});

    	logger.info("Started endpoint service");
            
		
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
