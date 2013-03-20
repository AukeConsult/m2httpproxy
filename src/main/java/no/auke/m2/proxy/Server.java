/*
 * This file is part of m2 http proxy project 
 * 
 * Copyright (c) 2011-2013 Auke Team / Leif Auke <leif@auke.no> / Huy Do <huydo@auke.no>
 * 
 * License: Attribution-NonCommercial-ShareAlike CC BY-NC-SA 
 * 
 */

package no.auke.m2.proxy;

import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.auke.m2.proxy.services.ClientService;
import no.auke.m2.proxy.services.EndPointService;
import no.auke.m2.proxy.services.NeighBorhodService;
import no.auke.p2p.m2.PeerServer;
import no.auke.p2p.m2.agent.AgentInterface;
import no.auke.p2p.m2.agent.NetAddress;
import no.auke.p2p.m2.general.LicenseReasons;
import no.auke.util.FileUtil;

public class Server {
	
	private static final Logger logger = LoggerFactory.getLogger(Server.class);	

	private PeerServer peerserver;
	public PeerServer getPeerServer() {
		return peerserver;
	}	
	
	public ExecutorService getExecutor() {
		
		return peerserver.getExecutor();
		
	}
	public static Server singelton;
	
	public ClientService getClientService() {
	
		return clientservice;
	}
	public EndPointService getEndpointService() {
	
		return endpointservice;
	}
	public NeighBorhodService getNeighborService() {
	
		return neighborservice;
	}

	private ClientService clientservice;
	private EndPointService endpointservice;
	private NeighBorhodService neighborservice;
	
	private void initPeerServer(){

		if(peerserver==null) {

			// TODO: get a better device id
			String deviceid = AgentInterface.convertToHex(AgentInterface.getHash(ServerParams.DEVICEID + ServerParams.NETSPACEID));
			peerserver = new PeerServer(ServerParams.NETSPACEID,
					                     ServerParams.APPID, 
					                     deviceid, 
					                     ServerParams.USERDIR,
					                     ServerParams.BOOTADDRESS,
					                     ServerParams.USEMIDDLEMAN, // using middleman, set on netspace
					                     new PeerServerListener(ServerParams.DEBUG){
											
											public void printLog(String message) {
											
												onMessage(message);	
											
											}
											
											public void onServiceConnected(
													NetAddress publicAddress,
													NetAddress kaServerAddress) {
												
												
											}
											
											public void connectionRejected(
													NetAddress kaServerAddress,
													String msg) {
												
												onMessage("user rejected from KA");
												peerserver.stopServer();
											}
							
											@Override
											public void onLicenseError(
													LicenseReasons reason,
													String licenseKey) {
											
												onMessage("license :" + licenseKey + " is invalid, reason: " + reason.name());
												peerserver.stopServer();
												
											}

										}
										);
			
			//HUYDO: read license from resource
			//if we don't have license on disk or we did not force any license, the trial will be used
			
			this.peerserver.setLicenseRegistrationHandler(ServerParams.getLicenseRegistrationHandler());		
			if(this.getClass().getResourceAsStream("/m2clientchat.license")!=null) {
			
				this.peerserver.setLicense(FileUtil.readFromFile(this.getClass().getResourceAsStream("/m2clientchat.license")));
			
			}
				
		}
		
	}
	
	public Server() {
		
		singelton=this;
		
	}
	
	public void startProxy() {

		initPeerServer();
		
		if(ServerParams.USE_REMOTE) {
			
			peerserver.start("", ServerParams.M2_PORT, getClientid());
		}

		if(ServerParams.USE_REMOTE && !peerserver.isConnected()) {

			logger.warn("can not start peerserver " + peerserver.getLastmessage());
			
		} else {

			neighborservice = new NeighBorhodService(this,"defaultendpoint");
			clientservice = new ClientService(this); 
			endpointservice = new EndPointService(this);
			
			//server.getExecutor().execute(neighbors);
			peerserver.getExecutor().execute(clientservice);
			//server.getExecutor().execute(endpointservice);		
			
		}
		
	}

	public void stopProxy() {
		
		peerserver.stopServer();
		
	}
	
	public static void main(String[] args) {
		
		ServerParams.USE_REMOTE=false; // internal testing
		ServerParams.setArgs(args);
		Server service = new Server();
		service.startProxy();		
	}
	
	public String getClientid() {

		return ServerParams.USERID;
	}
	

}
