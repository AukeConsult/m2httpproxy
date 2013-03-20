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

public class ReplyMsg {
	
	public enum ErrCode {
		
		OK,
		LOCAL_ERR_SEND_REMOTE,
		LOCAL_ERR_NO_ENDPOINT,
		LOCAL_ERR_REMOTE_TIMEOUT,
		LOCAL_ERR_NO_REPLY_FROM_REMOTE,
		REMOTE_ERR_SEND_REQUEST, 
		REMOTE_ERR_READ_REQUEST
		
	}
	
	public int getSession() {
	
		return session;
	}
	
	public byte[] getData() {
	
		return data;
	}

	
	public int getOrder() {
	
		return order;
	}

	public boolean isComplete() {
		
		return iscomplete;
	}

	public ErrCode getErrcode() {

		return errcode;
	
	}
	
	public String getMessage() {
		
		if(errcode!=ErrCode.OK) {
			
			return StringConv.UTF8(data);
			
		} else {
			
			return "";
		}
		
	}

	private int session=0;
	private int order=0;
	private boolean iscomplete=false;
	private ErrCode errcode=ErrCode.OK; 
	private byte[] data; 
	
	public ReplyMsg(int session, int order, boolean iscomplete, byte[] data) {
		
		this.session=session;
		this.order=order;
		this.iscomplete=iscomplete;
		this.data=data;
	
	}
	
	// error message for request
	public ReplyMsg(ErrCode errcode, int session, String message) {
		
		this.errcode=errcode;
		this.session=session;
		this.iscomplete=true;
		this.data = StringConv.getBytes(message);
		
	}

	public ReplyMsg(byte[] data) {
		
        List<byte[]> subs = ByteUtil.splitDynamicBytes(data);
        session = ByteUtil.getInt(subs.get(0));
        order = ByteUtil.getInt(subs.get(1));
        iscomplete = ByteUtil.getInt(subs.get(2)) == 1? true : false;
        errcode = ErrCode.valueOf(StringConv.UTF8(subs.get(3)));
        this.data = subs.get(4);
		
	}

	public byte[] getBytes() {

		return ByteUtil.mergeDynamicBytesWithLength(
				
				ByteUtil.getBytes(session, 4),
				ByteUtil.getBytes(order, 4),
				ByteUtil.getBytes(iscomplete ? 1: 0, 1),
				StringConv.getBytes(errcode.toString()),
				data

			);

	}

}
