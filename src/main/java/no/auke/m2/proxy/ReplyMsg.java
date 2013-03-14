package no.auke.m2.proxy;

public class ReplyMsg {
	
	public int getSession() {
	
		return session;
	}
	
	public byte[] getData() {
	
		return data;
	}

	private int session=0;
	private int order=0;
	private byte[] data; 
	private boolean iscomplete=false;
	
	public boolean isComplete() {
	
		return iscomplete;
	}

	public ReplyMsg(int session, int order, boolean iscomplete, byte[] data) {
		
		this.session=session;
		this.order=order;
		this.data=data;
		this.iscomplete=iscomplete;
	
	}

	public ReplyMsg(byte[] data) {
		
	}

	public String getBytes() {

		// TODO Auto-generated method stub
		return null;
	}

}
