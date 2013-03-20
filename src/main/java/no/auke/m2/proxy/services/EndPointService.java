/*
 * This file is part of m2 http proxy project 
 * 
 * Copyright (c) 2011-2013 Auke Team / Leif Auke <leif@auke.no> / Huy Do <huydo@auke.no>
 * 
 * License: Attribution-NonCommercial-ShareAlike CC BY-NC-SA 
 * 
 */

package no.auke.m2.proxy.services;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.auke.m2.proxy.Server;
import no.auke.m2.proxy.ServerParams;
import no.auke.m2.proxy.dataelements.RequestMsg;
import no.auke.m2.proxy.request.EndPointRequest;
import no.auke.p2p.m2.Socket;
import no.auke.p2p.m2.SocketListener;

// proxy service 
public class EndPointService implements Runnable {
	
	private static final Logger logger = LoggerFactory.getLogger(EndPointService.class);	

	private Server server;
	public Server getServer() {
		return server;
	}

	private no.auke.p2p.m2.Socket peer_socket;
	public no.auke.p2p.m2.Socket getPeerSocket() {
		return peer_socket;
	}

	private ConcurrentHashMap<String,EndPointRequest> requests = new ConcurrentHashMap<String,EndPointRequest>(); 
	
	public EndPointService (Server server) {
				
		this.server = server;

    	if(server.getPeerServer().isRunning()) {
    		
    		peer_socket = getServer().getPeerServer().open(ServerParams.HTTP_SERVICE_PORT, new SocketListener(){
    			
    			@Override
    			public void onIncomming(byte[] buffer) {

    				gotRequest(new RequestMsg(buffer));
    				
    			}});

    		logger.info("Started loopback from m2 network");
    		
    	} else {
    		
    		peer_socket=new Socket(0, null);
    		logger.info("No loopback from m2 network");
    		
    	}

		
	}	

	public void gotRequest(RequestMsg msg) {

    	logger.debug(" > request message " + msg.getReplyTo() + " for " + msg.getAddress());
		if(!requests.containsKey(msg.getAddress())) {
			
			getServer().getNeighborService().addNeighbor(msg);
			
			// open a request session for each different host endpoint
			
			EndPointRequest request = new EndPointRequest(this,msg.getHost(),msg.getPort());
			
			requests.put(msg.getAddress(), request);
			getServer().getPeerServer().getExecutor().execute(request);
					
		}
		requests.get(msg.getAddress()).gotRequest(msg);
		
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
