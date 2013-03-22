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
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.auke.m2.proxy.comunicate.INeighborCom;
import no.auke.m2.proxy.dataelements.ReplyMsg;
import no.auke.m2.proxy.dataelements.RequestMsg;
import no.auke.m2.proxy.services.EndPointService;
import no.auke.util.ByteUtil;
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
	
	private byte[] ReadResponse(InputStream content) throws IOException {

		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		byte[] inbuffer = new byte[4096];
		int bytes=0;
		while((bytes=content.read(inbuffer,0,4096))!=-1){
			buffer.write(inbuffer, 0,bytes);
		}
		return buffer.toByteArray();			
	}	
	
	public byte[] readGet(String[] header, URL url) throws ClientProtocolException, IOException {
		
		
		
		
		DefaultHttpClient client = HttpClientFactory.getThreadSafeClient();
		client.getConnectionManager().closeExpiredConnections();
		
        HttpHost target = new HttpHost(url.getHost(), url.getPort(), url.getProtocol());
        HttpGet req = new HttpGet(url.getPath() + (url.getQuery()==null?"":url.getQuery()));
        
		for(int i=1;i<header.length;i++){
			req.setHeader(header[i].split(":")[0], header[i].split(":")[1]);
		}

		
        HttpResponse rsp = client.execute(target, req);
		logger.debug("GOT response");

        List<byte[]> bytes = new ArrayList<byte[]>();
        
        bytes.add(StringConv.getBytes(rsp.getStatusLine().toString()+"\r\n"));

        System.out.println(rsp.getStatusLine().toString());

        for(Header h:rsp.getAllHeaders()){
            System.out.println(h.toString());
            bytes.add(StringConv.getBytes(h.toString()+"\r\n"));
        }
        bytes.add(StringConv.getBytes("\r\n"));
        
        HttpEntity entity = rsp.getEntity();
        if(entity!=null){

        	bytes.add(ReadResponse(entity.getContent()));
        	
        }
		return ByteUtil.mergeBytes(bytes);
		
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
				
				byte[] page = readGet(header,url);
				sendReplyToClient(new ReplyMsg(requestMsg.getSession(), 0, true, page),requestMsg);
				
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

		logger.debug(new String(msg.getHttpData()));
		readHttpResultSendToClient(msg);

	}

}
