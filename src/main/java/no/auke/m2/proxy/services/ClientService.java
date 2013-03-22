/*
 * This file is part of m2 http proxy project 
 * 
 * Copyright (c) 2011-2013 Auke Team / Leif Auke <leif@auke.no> / Huy Do <huydo@auke.no>
 * 
 * License: Attribution-NonCommercial-ShareAlike CC BY-NC-SA 
 * 
 */

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

// proxy service 
public class ClientService extends IServiceBase {

	private static final Logger logger = LoggerFactory.getLogger(ClientService.class);
	
	private ServerSocket tcp_Socket = null;

	private ConcurrentHashMap<Integer,ClientRequest> requests = new ConcurrentHashMap<Integer,ClientRequest>(); 
	private AtomicBoolean listening = new AtomicBoolean();
	
	public ClientService (Server server) {
		
		super(server);
		
        try {
        
        	tcp_Socket = new ServerSocket(ServerParams.PROXY_PORT);

    		logger.info("Open socket for browser input on port: " + String.valueOf(ServerParams.PROXY_PORT));

        	listening.set(true);
        	
        
        } catch (IOException e) { 
            
        	logger.error("Could not listen on port: " + ServerParams.PROXY_PORT);

        	listening.set(false);
        
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
	public void onInBuffer(byte[] buffer) {

		gotReply(new ReplyMsg(buffer));

	}

	
	@Override
	public void run() {

        try {

        	if(listening.get()) {
        		
            	tcp_Socket.setSoTimeout(ServerParams.CHECK_FREQUENCY);
            	
            	
            	while (listening.get()) {
                	
            		logger.debug("check requests");
            		try {
                		
                		
                    	ClientRequest request = new ClientRequest(this,tcp_Socket.accept(),getNeighborSocket());
                    	requests.put(request.getSession(),request);
                    	getServer().getExecutor().execute(request);
                		
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
    		
        	}

        	tcp_Socket.close();
        	getNeighborSocket().close();
		
        } catch (IOException e) {
		
        	logger.error("error in tcp_socket " + e.getMessage());

        } catch (Exception e) {
    		
        	logger.error("error in Client Service loop " + e.getMessage());
        	e.printStackTrace();
		
        }
        
        this.getServer().stopProxy();
		
	}



}
