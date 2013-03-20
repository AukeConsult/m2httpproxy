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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.auke.m2.proxy.Server;
import no.auke.m2.proxy.ServerParams;
import no.auke.m2.proxy.dataelements.Neighbor;
import no.auke.m2.proxy.dataelements.RequestMsg;
import no.auke.p2p.m2.Socket;
import no.auke.p2p.m2.SocketListener;

public class NeighBorhodService implements Runnable {
	
	private static final Logger logger = LoggerFactory.getLogger(NeighBorhodService.class);

	private Server server;	
	public Server getServer() {
	
		return server;
	}

	private ConcurrentHashMap<String,Neighbor> neigbors = new ConcurrentHashMap<String,Neighbor>(); 
	
	private Socket peer_socket;
	public Socket getPeerSocket() {
		return peer_socket;
	}
	
	String defaultendpoint="";
	public NeighBorhodService(Server server, String defaultendpoint){
		
		this.defaultendpoint=defaultendpoint;
		this.server=server;
		
		if(server.getPeerServer().isRunning()) {

			peer_socket = server.getPeerServer().open(ServerParams.NEIGTBOR_SERVICE_PORT, new SocketListener(){

				@Override
				public void onIncomming(byte[] buffer) {
					
				
				}});
			
    		logger.info("Started loopback from m2 network");
			
		} else {
			
    		logger.info("no loopback from m2 network");
			
		}
		
	}
	
	@Override
	public void run() {

		while(true) {
			
			// loop and organize neighbors
					
			
		}
		
	}

	// get the current endpoint for the address 
	public String getRemoteEndPoint(String address) {

		return defaultendpoint;
	
	}
	
	// reset the remote end point for this address 
	public void resetRemoteEndPoint(String address) {

		
	}	

	// add an incoming for the list
	public void addNeighbor(RequestMsg msg) {
		
		if(!neigbors.containsKey(msg.getReplyTo())) {
			
			neigbors.put(msg.getReplyTo(), new Neighbor(msg.getReplyTo()));
			
		}
		
	}

	// setting neighbor not alive
	public void setNotAlive(String clientid) {

		if(neigbors.containsKey(clientid)) {
			
			neigbors.get(clientid).setAlive(false);
			
		}
		
	}

}