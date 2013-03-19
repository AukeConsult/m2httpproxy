package no.auke.m2.proxy;

import java.util.List;
import no.auke.util.ByteUtil;
import no.auke.util.StringConv;

public class ReplyMsg {
	
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
	
	private int session=0;
	private int order=0;
	private boolean iscomplete=false;
	private byte[] data; 
	
	public ReplyMsg(int session, int order, boolean iscomplete, byte[] data) {
		
		this.session=session;
		this.order=order;
		this.iscomplete=iscomplete;
		this.data=data;
	
	}

	public ReplyMsg(byte[] data) {
		
        List<byte[]> subs = ByteUtil.splitDynamicBytes(data);
        session = ByteUtil.getInt(subs.get(0));
        order = ByteUtil.getInt(subs.get(1));
        iscomplete = ByteUtil.getInt(subs.get(2)) == 1? true : false;
        this.data = subs.get(3);
		
	}

	public byte[] getBytes() {

		return ByteUtil.mergeDynamicBytesWithLength(
				
				ByteUtil.getBytes(session, 4),
				ByteUtil.getBytes(order, 4),
				ByteUtil.getBytes(iscomplete ? 1: 0, 1),
				data

			);

	}

}
