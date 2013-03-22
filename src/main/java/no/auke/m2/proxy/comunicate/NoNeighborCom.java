package no.auke.m2.proxy.comunicate;

import no.auke.m2.proxy.dataelements.ReplyMsg;
import no.auke.m2.proxy.dataelements.RequestMsg;

public class NoNeighborCom extends INeighborCom {

	private String clientid;
	public NoNeighborCom(String clientid) {
		this.clientid=clientid;
	}
	@Override
	public boolean isRunning() {
		return false;
	}

	@Override
	public boolean replyHttpFromEndPoint(ReplyMsg msg, String replyTo) {
		return false;
	}

	@Override
	public boolean sendHttpToEndPoint(RequestMsg msg, String sendTo) {
		return false;
	}

	@Override
	public void close() {}

	@Override
	public String getClientid() {
		return clientid;
	}

}
