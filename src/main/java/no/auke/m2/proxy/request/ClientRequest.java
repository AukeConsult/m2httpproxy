/*
 * This file is part of m2 http proxy project 
 * 
 * Copyright (c) 2011-2013 Auke Team / Leif Auke <leif@auke.no> / Huy Do <huydo@auke.no>
 * 
 * License: Attribution-NonCommercial-ShareAlike CC BY-NC-SA 
 * 
 */

package no.auke.m2.proxy.request;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import no.auke.m2.proxy.comunicate.INeighborCom;
import no.auke.m2.proxy.dataelements.ReplyMsg;
import no.auke.m2.proxy.dataelements.RequestMsg;
import no.auke.m2.proxy.services.ClientService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientRequest implements Runnable {
	
	private static final Logger logger = LoggerFactory.getLogger(ClientRequest.class);

	private int MAX_WAIT = 15000; // 15 seconds	
	
	public static Random sessions = new Random();
	
	// for testing purpose
	private RequestMsg last_request;
	public RequestMsg getLastRequestMsg() {
		return last_request;
	}	
	private ReplyMsg last_reply;
	public ReplyMsg getLastReplyMsg() {
		return last_reply;
	}
	
	
	private Socket tcp_socket = null;
	private int session=0; 
	
	private ClientService clientservice;
	public ClientService getClientService() {
	
		return clientservice;
	}
	
	private boolean iscomplete=false;
	
	private AtomicLong last_activity=new AtomicLong();
	public long getLastActivity() {
	
		return last_activity.get();
	}
	public void setLastActivity(long last_activity_time) {
	
		this.last_activity.set(last_activity_time);
	}

	private INeighborCom neighborCom;
	public INeighborCom getNeighborCom() {
	
		return neighborCom;
	}

	public ClientRequest(ClientService clientservice, Socket tcp_socket, INeighborCom neighborCom) {		
	
		this.tcp_socket = tcp_socket;
		this.session=sessions.nextInt();
		this.clientservice=clientservice;
		this.neighborCom=neighborCom;
			
	}
	
	public boolean gotReply(ReplyMsg reply){
		
		last_reply = reply;
		last_activity.set(System.currentTimeMillis());
		
		if(logger.isDebugEnabled())
			logger.debug("got reply, length " + String.valueOf(reply.getData().length));
		
		if(reply.getErrcode()!=ReplyMsg.ErrCode.OK){
			
			iscomplete=true;
		
		} else if(reply.getErrcode()==ReplyMsg.ErrCode.REMOTE_ERR_READ_REQUEST) {
		
			// remote error reading result from web server

			logger.warn("remote REMOTE_ERR_READ_REQUEST " + reply.getMessage());

		} else if(reply.getErrcode()==ReplyMsg.ErrCode.REMOTE_ERR_SEND_REQUEST) {
			
			// remote error sending request to web server
			
			logger.warn("remote REMOTE_ERR_SEND_REQUEST " + reply.getMessage());
		}
		
		if(reply.getErrcode()==ReplyMsg.ErrCode.OK && reply.getData()!=null) {
			
			try {

				if(logger.isDebugEnabled())
					logger.debug(new String(reply.getData()));				
				
				tcp_socket.getOutputStream().write(reply.getData(), 0, reply.getData().length);
				tcp_socket.getOutputStream().flush();
			
			} catch (IOException e) {

				logger.warn("IO error sending to browser " + e.getMessage());
				return false;
			}
		
		} else {
			
			logger.warn("got empty reply");
			
		}
		
		if(iscomplete || reply.isComplete()) {
			
			try {
				
				if (tcp_socket != null) {
					tcp_socket.close();
				}
				
			} catch (IOException e) {
				
				logger.warn("IO error closing browser request " + e.getMessage());
				return false;
			
			}	
			iscomplete=true;
			
		}
		return true;
			
	}

	public Integer getSession() {

		return session;
	}
	
	@Override
	public void run() {

		// get input from user
		// send request to server
		// get response from server
		// send response to user

		try {

			String browser_address = tcp_socket.getInetAddress().getHostAddress() + ":"+ String.valueOf(tcp_socket.getPort());
			
			if(logger.isDebugEnabled())
				logger.debug("got browser request from address " + browser_address);

			
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			byte[] inbuffer = new byte[4096];
			int bytes=0;
			while((bytes=tcp_socket.getInputStream().read(inbuffer,0,4096))!=-1){

				buffer.write(inbuffer, 0,bytes);

				if(logger.isDebugEnabled())
					logger.debug("input from browser, length " + String.valueOf(buffer.size()));

				// check end of request
				if(inbuffer[bytes-1]=='\n' &&
				   inbuffer[bytes-2]=='\r' &
				   inbuffer[bytes-3]=='\n' &&
				   inbuffer[bytes-4]=='\r'    ) {
					
				   break; 
					
				}
				
			}
			
			byte[] data=buffer.toByteArray();

			if(data.length>0) {

				String endpoint = getClientService().getServer().getNeighborService().getRemoteEndPoint(browser_address);
				if(!endpoint.isEmpty()) {

					logger.debug(new String(data));

					last_request = new RequestMsg(getClientService().getServer().getClientid(), endpoint, session, data);
					
					if(getNeighborCom().isRunning()) {
						
						if(getNeighborCom().sendHttpToEndPoint(last_request,endpoint)){
							
							// ok send
							if(logger.isDebugEnabled())
								logger.debug("request sent to " + endpoint + " from " + browser_address +  " session " + String.valueOf(session));

							last_activity.set(System.currentTimeMillis());
							

						} else {
							
							last_request=null;
							
							getClientService().getServer().getNeighborService().resetRemoteEndPoint(browser_address);
							getClientService().getServer().getNeighborService().setNotAlive(endpoint);
							
							// sending direct reply with error
							
							gotReply(new ReplyMsg(ReplyMsg.ErrCode.LOCAL_ERR_SEND_REMOTE,getSession(),"error sending request to remote proxy"));
							
							
						}
						
						
					} else {
						
						// sending directly to the local end point
						getClientService().getServer().getEndpointService().gotRequest(last_request);
						
					}
					
				} else {

					gotReply(new ReplyMsg(ReplyMsg.ErrCode.LOCAL_ERR_NO_ENDPOINT,getSession(),"no endpoint found for " + browser_address));

					// error sending
					logger.warn("can not find any endpoint for address " + browser_address);
					
				}
				
				
			} else {
				
				logger.info("request from browser is empty");
				
			}

		} catch (IOException e) {
			
			logger.warn("IO error reading browser request " + e.getMessage());
		
		}		
	
	}

	public void checkReply() {

		if(last_activity.get() > 0 && System.currentTimeMillis() - last_activity.get() > MAX_WAIT) {

			gotReply(new ReplyMsg(ReplyMsg.ErrCode.LOCAL_ERR_REMOTE_TIMEOUT,getSession(),"to long waiting for reply from proxy"));
		
		}
	}
	
	public boolean isComplete() {		
		return iscomplete;
	}


}
