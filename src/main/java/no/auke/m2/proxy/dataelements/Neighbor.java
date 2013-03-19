package no.auke.m2.proxy.dataelements;

import java.util.List;

import no.auke.util.ByteUtil;
import no.auke.util.StringConv;

public class Neighbor {
	
	public String getClientid() {
	
		return clientid;
	}
	public void setClientid(String clientid) {
	
		this.clientid = clientid;
	}
	public boolean isAlive() {
	
		return isalive;
	}
	public void setAlive(boolean isalive) {
	
		this.isalive = isalive;
	}
	public Long getLastAlive() {
	
		return lastAlive;
	}
	public void setLastAlive(Long lastAlive) {
	
		this.lastAlive = lastAlive;
	}
	public int getSpeedUpload() {
	
		return speedUpload;
	}
	public void setSpeedUpload(int speedUpload) {
	
		this.speedUpload = speedUpload;
	}
	public int getSpeedDownLoad() {
	
		return speedDownLoad;
	}
	public void setSpeedDownLoad(int speedDownLoad) {
	
		this.speedDownLoad = speedDownLoad;
	}
	public int getNumNeighbors() {
	
		return numNeighbors;
	}
	public void setNumNeighbors(int numNeighbors) {
	
		this.numNeighbors = numNeighbors;
	}

	private String clientid="";
	private boolean isalive=false;
	private Long lastAlive=0L;
	private int speedUpload=0;
	private int speedDownLoad=0;
	private int numNeighbors=0;
		
	public Neighbor(String clientid) {		
		
		this.clientid=clientid;
	
	}

	public Neighbor(byte[] data) {	
		
        List<byte[]> subs = ByteUtil.splitDynamicBytes(data);
        clientid = StringConv.UTF8(subs.get(0));
        isalive = ByteUtil.getInt(subs.get(1)) == 1? true : false;
        lastAlive = ByteUtil.getLong(subs.get(2));
        speedUpload = ByteUtil.getInt(subs.get(3));
        speedDownLoad = ByteUtil.getInt(subs.get(4));
        numNeighbors = ByteUtil.getInt(subs.get(5));
		
	}
	
	public byte[] getBytes() {
		
		return ByteUtil.mergeDynamicBytesWithLength(
				
				StringConv.getBytes(clientid),
				ByteUtil.getBytes(isalive ? 1: 0, 1),
				ByteUtil.getBytes(lastAlive, 8),
				ByteUtil.getBytes(speedUpload, 4),
				ByteUtil.getBytes(speedDownLoad, 4),
				ByteUtil.getBytes(numNeighbors, 4)
				
			);
	}
	

}
