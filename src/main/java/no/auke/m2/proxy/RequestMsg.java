package no.auke.m2.proxy;

public class RequestMsg {
	
	public int getSession() {
		return session;
	}
	public byte[] getData() {
		return httpcontent;
	}

	private int session=0;
	private byte[] httpcontent;
	private int port=0;
	private String host="";
	
	private String replyTo="";
	public String getReplyTo() {
	
		return replyTo;
	}

	public RequestMsg(String replyTo, int session, String host, int port, byte[] httpcontent) {
		
		this.replyTo=replyTo;
		this.session=session;
		this.host=host;
		this.port=port;
		this.httpcontent=httpcontent;
	
	}

	public RequestMsg(byte[] data) {
		
	}
	public String getBytes() {

		// TODO Auto-generated method stub
		return null;
	}

}
