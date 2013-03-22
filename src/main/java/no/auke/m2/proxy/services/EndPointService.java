/*
 * This file is part of m2 http proxy project 
 * 
 * Copyright (c) 2011-2013 Auke Team / Leif Auke <leif@auke.no> / Huy Do <huydo@auke.no>
 * 
 * License: Attribution-NonCommercial-ShareAlike CC BY-NC-SA 
 * 
 */

package no.auke.m2.proxy.services;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.auke.m2.proxy.Server;
import no.auke.m2.proxy.dataelements.RequestMsg;
import no.auke.m2.proxy.request.EndPointRequest;

// proxy service 
public class EndPointService extends IServiceBase {
	
	private static final Logger logger = LoggerFactory.getLogger(EndPointService.class);	
	
	private ConcurrentHashMap<Integer,EndPointRequest> requests = new ConcurrentHashMap<Integer,EndPointRequest>(); 
	
    DefaultHttpClient httpclient;	
	public DefaultHttpClient getHttpClient() {
	
		return httpclient;
	}

	public EndPointService (Server server) {
		super(server);

		httpclient = new DefaultHttpClient();

	}	

	public void gotRequest(RequestMsg msg) {

		if(logger.isDebugEnabled())
			logger.debug("http request message from " + msg.getReplyTo());
		
		getServer().getExecutor().execute(new EndPointRequest(this,msg,getNeighborSocket()));

	}
	
	@Override
	public void onInBuffer(byte[] buffer) {

		gotRequest(new RequestMsg(buffer));
		
	}
	
	@Override
	public void run() {

		// loop something to do perhaps ??
		while(true) {
			
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
			}
		}
	}

}
