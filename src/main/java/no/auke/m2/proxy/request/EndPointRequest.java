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
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.auke.m2.proxy.ServerParams;
import no.auke.m2.proxy.comunicate.INeighborCom;
import no.auke.m2.proxy.dataelements.ReplyMsg;
import no.auke.m2.proxy.dataelements.RequestMsg;
import no.auke.m2.proxy.services.ClientService;
import no.auke.m2.proxy.services.EndPointService;
import no.auke.p2p.m2.general.BlockingQueue;

public class EndPointRequest implements Runnable {
	
	private static final Logger logger = LoggerFactory.getLogger(EndPointRequest.class);	
	
	private static final int BUFFER_SIZE = 32768;
	private static final long MAX_INACTIVE = 60000; //(one minute) 

	private EndPointService service;
	public EndPointService getService() {
	
		return service;
	}

	private long lastused=0;
	
	private int port=0;
	private String host="";
	
	private BlockingQueue<RequestMsg> outMsg = new BlockingQueue<RequestMsg>(1000);
	
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

	public EndPointRequest(EndPointService service, String host, int port, INeighborCom neighborCom) {		
	
		this.service=service;
		this.host = host;
		this.port=port;
		this.neighborCom=neighborCom;

		isconnected.set(false);
		try {

			logger.debug("open endpoint socket for");
			
			tcpsocket = new java.net.Socket(host, port);
			isconnected.set(tcpsocket.isConnected());
		
		} catch (UnknownHostException e) {
		
			logger.warn("unknown host " + getAddress() +  " error " + e.getMessage());

		} catch (IOException e) {

			logger.warn("IOException host " + getAddress() +  " error " + e.getMessage());

		}
	
	}	
	
	public boolean gotRequest(RequestMsg msg){

		if(isConnected()) {

			lastused = System.currentTimeMillis();
			outMsg.add(msg);

			return true;
			
		} else {
			
			return false;
			
		}
	
	}
	
	public boolean writeRequest(byte[] httpdata) {
		
		try {
			
			tcpsocket.getOutputStream().write(httpdata, 0, httpdata.length);
			tcpsocket.getOutputStream().flush();
			
			Thread.yield();
			return true;

		} catch (IOException e) {
			
			logger.warn("IO error handling request to " + getAddress() +  " error " + e.getMessage());
			return false;
		}
				
	}
	
	public boolean readResultSendBack(int session, String replyTo) {
		
		byte[] datain = new byte[BUFFER_SIZE];
		int cnt=0;
		int index = 0;
		
		try {
			
			while ((index = tcpsocket.getInputStream().read(datain, 0, BUFFER_SIZE))!= -1) {
				
				byte[] dataout = new byte[index];
				if(index>0) {
					
					System.arraycopy(datain, 0, dataout, 0, index);
				}
				
				sendResultBack(new ReplyMsg(session, cnt, index < BUFFER_SIZE, dataout),replyTo);
				lastused = System.currentTimeMillis();
				cnt++;
					
			}
			
			return true;
			
		} catch (IOException e) {

			logger.warn("IO error reading result from " + getAddress() +  " error " + e.getMessage());
			sendResultBack(new ReplyMsg(ReplyMsg.ErrCode.REMOTE_ERR_IO,session,e.getMessage()),replyTo);
			
			return false;

		}
		
	}
		
	public boolean sendResultBack(ReplyMsg msg, String replyTo) {
		
		if(!replyTo.equals(getService().getServer().getClientid())) {

			return getNeighborCom().replyHttpFromEndPoint(msg, replyTo);
			
		} else {
			
			// request is from local client
			getService().getServer().getClientService().gotReply(msg);
			return true;
		}
		
		
	}
	
	@Override
	public void run() {

		RequestMsg msgOut;
		try {
		
			while(!isconnected.get() && (msgOut = outMsg.take())!=null) {

				if(logger.isDebugEnabled())
					logger.debug("handling request from " + msgOut.getReplyTo() +  " to " + getAddress());
				
				logger.debug(new String(msgOut.getHttpData()));
				lastused = System.currentTimeMillis();

				if(writeRequest(msgOut.getHttpData())) {

					readResultSendBack(msgOut.getSession(), msgOut.getReplyTo());
					
				} else {
					
					logger.warn("can not send request to web server at " + getAddress());
					sendResultBack(new ReplyMsg(ReplyMsg.ErrCode.REMOTE_ERR_NO_WEB_SERVER,msgOut.getSession(),"can not send request to webserver"),msgOut.getReplyTo());
					
					// disconnect this session
					isconnected.set(false);
				
				}
				
			}

			if(tcpsocket!=null) {

				tcpsocket.close();
				
			}			


		} catch (InterruptedException e) {
		
		
		} catch (IOException e) {

			logger.warn("IO error closing socket to " + getAddress() +  " error " + e.getMessage());
		
		}
	
	}

	public boolean isComplete() {
		
		isconnected.set(System.currentTimeMillis() - lastused > MAX_INACTIVE);
		return isconnected.get();
	
	}

	public String getAddress() {

		return host + ":" + String.valueOf(port);
	}

}
