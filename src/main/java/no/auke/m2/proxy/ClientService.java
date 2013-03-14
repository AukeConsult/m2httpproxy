package no.auke.m2.proxy;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import no.auke.p2p.m2.PeerServer;
import no.auke.p2p.m2.Socket;
import no.auke.p2p.m2.SocketListener;


// proxy service 
public class ClientService implements Runnable{

	private static final int PEER_PORT = 10;
	
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
	
	private String defaultEndPoint;
	
	public ClientService (PeerServer server, int listenport, String defaultEndPoint) {
				
		this.server = server;
		this.defaultEndPoint=defaultEndPoint;
		
        try {
        
        	tcp_Socket = new ServerSocket(listenport);

    		peer_socket = server.open(PEER_PORT, new SocketListener(){

    			@Override
    			public void onIncomming(byte[] buffer) {

    				ReplyMsg reply = new ReplyMsg(buffer);
    				
    				if(requests.containsKey(reply.getSession())) {
    					
    					requests.get(reply.getSession()).gotReply(reply);
    					
    				}
    				
    				
    			}});

        	System.out.println("Started on: " + listenport);
        
        } catch (IOException e) {
            
        	System.err.println("Could not listen on port: " + listenport);
        
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

	// get a remote party
	public String getRemote(SocketAddress localSocketAddress) {

		return defaultEndPoint;
	}

}
