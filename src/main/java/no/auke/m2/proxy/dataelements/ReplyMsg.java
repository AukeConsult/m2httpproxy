package no.auke.m2.proxy.dataelements;

import java.util.List;

import no.auke.m2.proxy.dataelements.ReplyMsg.ErrCode;
import no.auke.util.ByteUtil;
import no.auke.util.StringConv;

public class ReplyMsg {
	
	public enum ErrCode {
		
		OK,
		LOCAL_ERR_SEND_REMOTE,
		LOCAL_ERR_NO_ENDPOINT,
		LOCAL_ERR_REMOTE_TIMEOUT,
		LOCAL_ERR_NO_REPLY_FROM_REMOTE,
		REMOTE_ERR_NO_WEB_SERVER
		
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
	public ReplyMsg(ErrCode errcode, String message) {
		
		this.errcode=errcode;
		this.data = StringConv.getBytes(String.valueOf(errcode) + " -> " + message);
		
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
