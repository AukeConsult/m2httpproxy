package no.auke.m2.proxy.services;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketAddress;
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
    					
    					requests.get(reply.getSession()).gotReply(reply);
    					
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
        	
            while (listening.get()) {
            	
            	ClientRequest request = new ClientRequest(this,tcp_Socket.accept());
            	requests.put(request.getSession(),request);
            	server.getExecutor().execute(request);
            	            	
            	// check finish and remove from list
            	
            	ArrayList<ClientRequest> openRequests = new ArrayList<ClientRequest>(requests.values());
            	for(ClientRequest check:openRequests){
            	
            		if(check.isComplete()){
            			
            			requests.remove(check.getSession());
            		}
            		
            	}
            	
            }
		
        	tcp_Socket.close();
        	peer_socket.close();
		
        } catch (IOException e) {
		
        	e.printStackTrace();
		
        }
		
	}

}
