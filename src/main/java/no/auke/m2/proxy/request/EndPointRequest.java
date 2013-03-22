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
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.auke.m2.proxy.comunicate.INeighborCom;
import no.auke.m2.proxy.dataelements.ReplyMsg;
import no.auke.m2.proxy.dataelements.RequestMsg;
import no.auke.m2.proxy.services.EndPointService;
import no.auke.p2p.m2.general.BlockingQueue;
import no.auke.util.StringConv;

public class EndPointRequest implements Runnable {
	
	private static final Logger logger = LoggerFactory.getLogger(EndPointRequest.class);	
	
	private EndPointService endpointservice;
	public EndPointService getEndPointService() {
	
		return endpointservice;
	}

	private INeighborCom neighborCom;
	public INeighborCom getNeighborCom() {
	
		return neighborCom;
	}
	
	private ReplyMsg last_replyMsg;
	public ReplyMsg getLastReplyMsg() {
	
		return last_replyMsg;
	}

	RequestMsg msg=null;
	public EndPointRequest(EndPointService endpointservice,RequestMsg msg,INeighborCom neighborCom) {		
	
		this.endpointservice=endpointservice;
		this.neighborCom=neighborCom;
		this.msg=msg;
	
	}	
	
	public StringBuilder readGet(String[] header, URL url) throws ClientProtocolException, IOException {
		
        HttpHost target = new HttpHost(url.getHost(), url.getPort(), url.getProtocol());
        HttpGet req = new HttpGet(url.getPath() + (url.getQuery()==null?"":url.getQuery()));
        
		for(int i=1;i<header.length;i++){
			req.setHeader(header[i].split(":")[0], header[i].split(":")[1]);
            System.out.println(header[i]);
		}

        HttpResponse rsp = getEndPointService().getHttpClient().execute(target, req);
        HttpEntity entity = rsp.getEntity();

        StringBuilder reponsestring = new StringBuilder();
        reponsestring.append(rsp.getStatusLine() + "\r\n");

        Header[] headers = rsp.getAllHeaders();
        for (int i = 0; i<headers.length; i++) {
            reponsestring.append(headers[i] + "\r\n");
        }
        if (entity != null) {
        	reponsestring.append(EntityUtils.toString(entity)+"\r\n");
        }
        reponsestring.append("\r\n");
        
		return reponsestring;
		
	}
	
	public void readHttpResultSendToClient(RequestMsg requestMsg) {
		
		last_replyMsg=null;
		try {

			String[] alldata = StringConv.UTF8(requestMsg.getHttpData()).split("\r\n\r\n");

			String[] header = alldata[0].split("\r\n");
			String verb = header[0].split(" ")[0];
			String urlstring = header[0].split(" ")[1];
			
			URL url = new URL(urlstring);
			
			if(verb.toUpperCase().equals("GET")) {
				
				StringBuilder reponsestring = readGet(header,url);
				sendReplyToClient(new ReplyMsg(requestMsg.getSession(), 0, true, StringConv.getBytes(reponsestring.toString())),requestMsg);
				System.out.println(reponsestring.toString());

			}
			
		} catch (MalformedURLException e) {

			logger.warn("MalformedURLException reading result " + e.getMessage());
			sendReplyToClient(new ReplyMsg(ReplyMsg.ErrCode.REMOTE_ERR_READ_REQUEST,requestMsg.getSession(),e.getMessage()),requestMsg);
			
		} catch (IOException e) {

			logger.warn("IOException reading result " + e.getMessage());
			sendReplyToClient(new ReplyMsg(ReplyMsg.ErrCode.REMOTE_ERR_READ_REQUEST,requestMsg.getSession(),e.getMessage()),requestMsg);

		}
		
	}	
	
	public boolean sendReplyToClient(ReplyMsg replyMsg, RequestMsg requestMsg) {
		
		last_replyMsg = replyMsg;
		if(!requestMsg.getReplyTo().equals(getNeighborCom().getClientid())) {

			return getNeighborCom().replyHttpFromEndPoint(replyMsg, requestMsg.getReplyTo());
			
		} else {
			
			// request is from local client
			getEndPointService().getServer().getClientService().gotReply(replyMsg);
			return true;
		}
		
	}
	
	@Override
	public void run() {

		if(logger.isDebugEnabled())
			logger.debug("start request thread");
		
		logger.debug(new String(msg.getHttpData()));
		readHttpResultSendToClient(msg);

	}

}
