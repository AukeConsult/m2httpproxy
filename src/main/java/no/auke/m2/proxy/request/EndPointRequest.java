/*
 * This file is part of m2 http proxy project 
 * 
 * Copyright (c) 2011-2013 Auke Team / Leif Auke <leif@auke.no> / Huy Do <huydo@auke.no>
 * 
 * License: Attribution-NonCommercial-ShareAlike CC BY-NC-SA 
 * 
 */

package no.auke.m2.proxy.request;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.auke.m2.proxy.comunicate.INeighborCom;
import no.auke.m2.proxy.dataelements.ReplyMsg;
import no.auke.m2.proxy.dataelements.RequestMsg;
import no.auke.m2.proxy.services.EndPointService;
import no.auke.p2p.m2.general.BlockingQueue;

public class EndPointRequest implements Runnable {
	
	private static final Logger logger = LoggerFactory.getLogger(EndPointRequest.class);	
	
	private static final int BUFFER_SIZE = 32768;
	private static final long MAX_INACTIVE = 60000; //(one minute) 

	private EndPointService endpointservice;
	public EndPointService getEndPointService() {
	
		return endpointservice;
	}

	private long lastused=0;
	
	private int webhostport=0;
	private String webhostaddress="";
	
	private BlockingQueue<RequestMsg> outMsgQueue = new BlockingQueue<RequestMsg>(1000);
	
	private java.net.Socket tcpsocket;
	public java.net.Socket getTcpSocket() {
	
		return tcpsocket;
	}

	private AtomicBoolean isconnected = new AtomicBoolean();
	public boolean isConnected() {
	
		return isconnected.get();
	}

	private INeighborCom neighborCom;
	public INeighborCom getNeighborCom() {
	
		return neighborCom;
	}

	public EndPointRequest(EndPointService endpointservice, String webhostaddress, int webhostport, INeighborCom neighborCom) {		
	
		this.endpointservice=endpointservice;
		this.webhostaddress=webhostaddress;
		this.webhostport=webhostport;
		this.neighborCom=neighborCom;

		isconnected.set(false);
		try {

			logger.debug("open endpoint socket for");
			
			tcpsocket = new java.net.Socket(this.webhostaddress, this.webhostport);
			isconnected.set(tcpsocket.isConnected());
		
		} catch (UnknownHostException e) {
		
			logger.warn("unknown host " + getWebAddress() +  " error " + e.getMessage());

		} catch (IOException e) {

			logger.warn("IOException host " + getWebAddress() +  " error " + e.getMessage());

		}
	
	}	
	
	public boolean gotRequest(RequestMsg msg){

		if(isConnected()) {

			lastused = System.currentTimeMillis();
			outMsgQueue.add(msg);

			return true;
			
		} else {
			
			return false;
			
		}
	
	}
	
	public boolean sendHttpRequest(RequestMsg requestMsg) {
		
		try {
			
			getTcpSocket().getOutputStream().write(requestMsg.getHttpData(), 0, requestMsg.getHttpData().length);
			getTcpSocket().getOutputStream().flush();
			
			Thread.yield();
			return true;

		} catch (IOException e) {
			
			sendReplyToClient(new ReplyMsg(ReplyMsg.ErrCode.REMOTE_ERR_SEND_REQUEST,requestMsg.getSession(),e.getMessage()),requestMsg);
			logger.warn("IO error sending request to " + getWebAddress() +  " error " + e.getMessage());
			return false;
		}
				
	}
	
	public boolean readHttpResultSendToClient(RequestMsg requestMsg) {
		
		byte[] datain = new byte[BUFFER_SIZE];
		int cnt=0;
		int index = 0;
		
		try {
			
			while ((index = getTcpSocket().getInputStream().read(datain, 0, BUFFER_SIZE))!= -1) {
				
				byte[] dataout = new byte[index];
				if(index>0) {
					
					System.arraycopy(datain, 0, dataout, 0, index);
				}
				
				sendReplyToClient(new ReplyMsg(requestMsg.getSession(), cnt, index < BUFFER_SIZE, dataout),requestMsg);
				lastused = System.currentTimeMillis();
				cnt++;
					
			}
			
			return true;
			
		} catch (IOException e) {

			logger.warn("IO error reading result from " + getWebAddress() +  " error " + e.getMessage());
			sendReplyToClient(new ReplyMsg(ReplyMsg.ErrCode.REMOTE_ERR_READ_REQUEST,requestMsg.getSession(),e.getMessage()),requestMsg);
			
			return false;

		}
		
	}
		
	public boolean sendReplyToClient(ReplyMsg replyMsg, RequestMsg requestMsg) {
		
		if(!requestMsg.getReplyTo().equals(getEndPointService().getServer().getClientid())) {

			return getNeighborCom().replyHttpFromEndPoint(replyMsg, requestMsg.getReplyTo());
			
		} else {
			
			// request is from local client
			getEndPointService().getServer().getClientService().gotReply(replyMsg);
			return true;
		}
		
		
	}
	
	@Override
	public void run() {

		try {
		
			RequestMsg requestMsg=null;

			while(!isconnected.get() && (requestMsg = outMsgQueue.take())!=null) {

				if(logger.isDebugEnabled())
					logger.debug("handling request from " + requestMsg.getReplyTo() +  " to " + getWebAddress());
				
				logger.debug(new String(requestMsg.getHttpData()));
				lastused = System.currentTimeMillis();

				if(sendHttpRequest(requestMsg)) {

					if(!readHttpResultSendToClient(requestMsg)){

						// disconnect this session
						isconnected.set(false);
						
					}
					
				} else {
					
					// disconnect this session
					isconnected.set(false);
				
				}
				
			}

			if(tcpsocket!=null) {

				tcpsocket.close();
				
			}			

		} catch (InterruptedException e) {
		} catch (IOException e) {
			logger.warn("IO error closing socket to " + getWebAddress() +  " error " + e.getMessage());
		}
	
	}

	public boolean isComplete() {
		
		isconnected.set(System.currentTimeMillis() - lastused > MAX_INACTIVE);
		return isconnected.get();
	
	}

	public String getWebAddress() {

		return webhostaddress + ":" + String.valueOf(webhostport);
	}

}
