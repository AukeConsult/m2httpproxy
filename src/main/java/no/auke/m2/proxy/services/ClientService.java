package no.auke.m2.proxy.services;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.auke.m2.proxy.Server;
import no.auke.m2.proxy.ServerParams;
import no.auke.m2.proxy.dataelements.ReplyMsg;
import no.auke.m2.proxy.request.ClientRequest;
import no.auke.p2p.m2.Socket;
import no.auke.p2p.m2.SocketListener;

// proxy service 
public class ClientService implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(ClientService.class);

	
	private Server server;
	public Server getServer() {
		return server;
	}

	private Socket peer_socket;
	public Socket getPeerSocket() {
		return peer_socket;
	}

	private ServerSocket tcp_Socket = null;

	private ConcurrentHashMap<Integer,ClientRequest> requests = new ConcurrentHashMap<Integer,ClientRequest>(); 
	private AtomicBoolean listening = new AtomicBoolean();
	
	
	public ClientService (Server server) {
				
		this.server = server;
        try {
        
        	tcp_Socket = new ServerSocket(ServerParams.PROXY_PORT);

        	if(server.getPeerServer().isRunning()) {
        		
        		peer_socket = server.getPeerServer().open(ServerParams.HTTP_SERVICE_PORT, new SocketListener(){

        			@Override
        			public void onIncomming(byte[] buffer) {

        				gotReply(new ReplyMsg(buffer));
        				
        			}});

        		logger.info("Started loopback from m2 network");
        		
        	} else {
        		
        		peer_socket=new Socket(0, null);
        		logger.info("No loopback from m2 network");
        		
        	}
        
        } catch (IOException e) {
            
        	logger.error("Could not listen on port: " + ServerParams.PROXY_PORT);
        
        }
		
	}	
	

	public void gotReply(ReplyMsg msg) {

		if(requests.containsKey(msg.getSession())) {
			
			if(!requests.get(msg.getSession()).gotReply(msg)) {
				
				logger.error("error request handling the reply for session " + msg.getSession());
				
			}
			
		}
		
	}	
	
	@Override
	public void run() {

        try {
        	
        	tcp_Socket.setSoTimeout(ServerParams.CHECK_FREQUENCY);
        	
        	listening.set(true);
        	
        	while (listening.get()) {
            	
        		logger.debug("check requests");
        		try {
            		
            		
                	ClientRequest request = new ClientRequest(this,tcp_Socket.accept());
                	requests.put(request.getSession(),request);
                	server.getPeerServer().getExecutor().execute(request);
            		
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
