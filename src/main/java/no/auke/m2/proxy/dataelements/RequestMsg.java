package no.auke.m2.proxy.dataelements;

import java.util.List;

import no.auke.util.ByteUtil;
import no.auke.util.StringConv;
import no.auke.util.StringUtil;

public class RequestMsg {
	
	public String getReplyTo() {
		return replyTo;
	}
	
	public int getSession() {
		return session;
	}
	public byte[] getHttpData() {
		return httpdata;
	}
	
	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}	

	private String replyTo="";
	private int session=0;
	private byte[] httpdata;
	private int port=0;
	private String host="";

	public RequestMsg(String replyTo, int session, String host, int port, byte[] httpdata) {
		
		this.replyTo=replyTo;
		this.session=session;
		this.host=host;
		this.port=port;
		this.httpdata=httpdata;
	
	}

	public RequestMsg(byte[] data) {
		
        List<byte[]> subs = ByteUtil.splitDynamicBytes(data);
        replyTo = StringConv.UTF8(subs.get(0));
        session = ByteUtil.getInt(subs.get(1));
        host = StringConv.UTF8(subs.get(2));
        port = ByteUtil.getInt(subs.get(3));
        httpdata = subs.get(4);
		
	}
	
	public byte[] getBytes() {

		return ByteUtil.mergeDynamicBytesWithLength(
				
				StringConv.getBytes(replyTo),
				ByteUtil.getBytes(session, 4),
				StringConv.getBytes(host),
				ByteUtil.getBytes(port, 4),
				httpdata

			);
	}

	public String getAddress() {
	
		return host + ":" + String.valueOf(port);
	
	}

	
}
