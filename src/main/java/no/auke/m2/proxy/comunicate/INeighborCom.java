package no.auke.m2.proxy.comunicate;

import no.auke.m2.proxy.dataelements.ReplyMsg;
import no.auke.m2.proxy.dataelements.RequestMsg;

public abstract class INeighborCom {

	public abstract boolean isRunning();
	public abstract boolean replyHttpFromEndPoint(ReplyMsg msg, String replyTo);
	public abstract boolean sendHttpToEndPoint(RequestMsg msg, String sendTo);
	public abstract void close();
	public abstract String getClientid();

}
