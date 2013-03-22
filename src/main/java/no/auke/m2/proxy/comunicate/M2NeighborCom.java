package no.auke.m2.proxy.comunicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.auke.m2.proxy.dataelements.ReplyMsg;
import no.auke.m2.proxy.dataelements.RequestMsg;
import no.auke.p2p.m2.Socket;

public class M2NeighborCom extends INeighborCom {

	private static final Logger logger = LoggerFactory.getLogger(M2NeighborCom.class);

	private Socket peer_socket;
	public Socket getPeerSocket() {
		return peer_socket;
	}

	public M2NeighborCom(Socket peer_socket) {
		
		this.peer_socket=peer_socket;
		
	}

	@Override
	public boolean isRunning() {

		return getPeerSocket().getService().isRunning();
	}

	@Override
	public boolean replyHttpFromEndPoint(ReplyMsg msg, String replyTo) {

		if(!getPeerSocket().send(replyTo, peer_socket.getPort(), msg.getBytes())) {

			logger.warn("Error reply back to " + replyTo +  " error " + getPeerSocket().getLastMessage());
			return false;

		}
		return true;
		
	}

	@Override
	public boolean sendHttpToEndPoint(RequestMsg msg, String sendTo) {

		if(getPeerSocket().send(sendTo, getPeerSocket().getPort(), msg.getBytes())) {
			
			return true;
			
		} else {
			
			logger.warn("Error sending to end point " + sendTo + " m2 error " + getPeerSocket().getLastMessage() );
			return false;
		}

	}

	@Override
	public void close() {

		peer_socket.close();
		
	}

	@Override
	public String getClientid() {
		return getPeerSocket().getService().getClientid();
	}

}
