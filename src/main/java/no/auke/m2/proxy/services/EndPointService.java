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
import no.auke.m2.proxy.dataelements.RequestMsg;
import no.auke.m2.proxy.request.EndPointRequest;

// proxy service 
public class EndPointService extends IServiceBase {
	
	private static final Logger logger = LoggerFactory.getLogger(EndPointService.class);	
	
	private ConcurrentHashMap<String,EndPointRequest> requests = new ConcurrentHashMap<String,EndPointRequest>(); 
	
	public EndPointService (Server server) {
		super(server);
	}	

	public void gotRequest(RequestMsg msg) {

    	logger.debug("http request message from " + msg.getReplyTo() + " for " + msg.getAddress());

    	if(!requests.containsKey(msg.getAddress()) || !requests.get(msg.getAddress()).isConnected()) {
			
			getServer().getNeighborService().addNeighbor(msg);
			
			// open a request session for each different host endpoint
			EndPointRequest request = new EndPointRequest(this,msg.getHost(),msg.getPort(),getNeighborSocket());
			
			requests.put(msg.getAddress(), request);
			getServer().getExecutor().execute(request);
					
		}		
		requests.get(msg.getAddress()).gotRequest(msg);
		
	}
	
	@Override
	public void onInBuffer(byte[] buffer) {

		gotRequest(new RequestMsg(buffer));
		
	}
	
	@Override
	public void run() {

		// loop and clean up
		
		while(true) {
			
			// remove completed requests
        	ArrayList<EndPointRequest> openRequests = new ArrayList<EndPointRequest>(requests.values());
        	for(EndPointRequest check:openRequests){
        	
        		if(check.isComplete()) {
        			
        			requests.remove(check.getWebAddress());
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
