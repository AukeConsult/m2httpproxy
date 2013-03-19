package no.auke.m2.proxy.services;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.auke.m2.proxy.ServerParams;
import no.auke.m2.proxy.dataelements.ReplyMsg;
import no.auke.m2.proxy.request.ClientRequest;
import no.auke.p2p.m2.PeerServer;
import no.auke.p2p.m2.Socket;
import no.auke.p2p.m2.SocketListener;

// proxy service 
public class ClientService implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(ClientService.class);

	private static final int CHECK_FREQUENCY = 1000;	
	
	private NeighBorhodService neighborService=null;
	public NeighBorhodService getNeighborService() {
	
		return neighborService;
	}

	private PeerServer server;
	public PeerServer getPeerServer() {
		return server;
	}

	private Socket peer_socket;
	public Socket getPeerSocket() {
		return peer_socket;
	}

	private ServerSocket tcp_Socket = null;

	private ConcurrentHashMap<Integer,ClientRequest> requests; 
	private AtomicBoolean listening = new AtomicBoolean();
	
	
	public ClientService (PeerServer server, NeighBorhodService neighborService) {
				
		this.server = server;
		this.neighborService=neighborService;
		
        try {
        
        	tcp_Socket = new ServerSocket(ServerParams.PROXY_PORT);

    		peer_socket = server.open(ServerParams.HTTP_SERVICE_PORT, new SocketListener(){

    			@Override
    			public void onIncomming(byte[] buffer) {

    				ReplyMsg reply = new ReplyMsg(buffer);
    				
    				if(requests.containsKey(reply.getSession())) {
    					
    					if(!requests.get(reply.getSession()).gotReply(reply)) {
    						
    						logger.error("error request handling the reply for session " + reply.getSession());
    						
    					}
    					
    				}
    				
    				
    			}});

        	System.out.println("Started on: " + ServerParams.PROXY_PORT);
        
        } catch (IOException e) {
            
        	System.err.println("Could not listen on port: " + ServerParams.PROXY_PORT);
        
        }
        
		
	}	
	
	@Override
	public void run() {

        try {
        	
        	tcp_Socket.setSoTimeout(CHECK_FREQUENCY);
            
        	while (listening.get()) {
            	
            	try {
            		
                	ClientRequest request = new ClientRequest(this,tcp_Socket.accept());
                	requests.put(request.getSession(),request);
                	server.getExecutor().execute(request);
            		
            	} catch (SocketTimeoutException tm) {
            	}
            	            	
            	// check finish and remove from list
            	if(requests.size()>0) {

            		ArrayList<ClientRequest> openRequests = new ArrayList<ClientRequest>(requests.values());
                	for(ClientRequest request:openRequests){
                	
                		request.checkReply();
                		if(request.isComplete()){
                			requests.remove(request.getSession());
                		}
                		
                	}
            		
            	}
            	
            }
		
        	tcp_Socket.close();
        	peer_socket.close();
		
        } catch (IOException e) {
		
        	logger.error("error in tcp_socket " + e.getMessage());

        } catch (Exception e) {
    		
        	logger.error("error in Client Service loop " + e.getMessage());
        	e.printStackTrace();
		
        }
		
	}

}
