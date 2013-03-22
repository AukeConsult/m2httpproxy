/*
 * This file is part of m2 http proxy project 
 * 
 * Copyright (c) 2011-2013 Auke Team / Leif Auke <leif@auke.no> / Huy Do <huydo@auke.no>
 * 
 * License: Attribution-NonCommercial-ShareAlike CC BY-NC-SA 
 * 
 */

package no.auke.m2.proxy.dataelements;

import java.util.List;

import no.auke.util.ByteUtil;
import no.auke.util.StringConv;

public class RequestMsg {
	
	public String getReplyTo() {
		return replyTo;
	}

	public String getSendTo() {
		return replyTo;
	}
	
	public int getSession() {
		return session;
	}
	public byte[] getHttpData() {
		return httpdata;
	}
	
	private String replyTo="";
	private String sendTo="";	
	private int session=0;
	private byte[] httpdata;

	public RequestMsg(String replyTo, String sendTo, int session, byte[] httpdata) {
		
		this.replyTo=replyTo.isEmpty()?"":replyTo;
		this.sendTo=sendTo.isEmpty()?"":sendTo;
		this.session=session;
		this.httpdata=httpdata;
	
	}

	public RequestMsg(byte[] data) {
		
        List<byte[]> subs = ByteUtil.splitDynamicBytes(data);
        replyTo = StringConv.UTF8(subs.get(0));
        sendTo = StringConv.UTF8(subs.get(1));
        session = ByteUtil.getInt(subs.get(2));
        httpdata = subs.get(3);
		
	}
	
	public byte[] getBytes() {

		return ByteUtil.mergeDynamicBytesWithLength(
				
				StringConv.getBytes(replyTo),
				StringConv.getBytes(sendTo),
				ByteUtil.getBytes(session, 4),
				httpdata

			);
	}
	
}
