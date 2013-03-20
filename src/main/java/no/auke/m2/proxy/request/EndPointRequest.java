package no.auke.m2.proxy.request;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.auke.m2.proxy.ServerParams;
import no.auke.m2.proxy.dataelements.ReplyMsg;
import no.auke.m2.proxy.dataelements.RequestMsg;
import no.auke.m2.proxy.services.EndPointService;
import no.auke.p2p.m2.Socket;
import no.auke.p2p.m2.general.BlockingQueue;

public class EndPointRequest implements Runnable {
	
	private static final Logger logger = LoggerFactory.getLogger(EndPointRequest.class);	
	
	private static final int BUFFER_SIZE = 32768;
	private static final long MAX_INACTIVE = 60000; //(one minute) 

	private EndPointService service;
	public EndPointService getService() {
	
		return service;
	}

	private long lastused=0;
	
	private int port=0;
	private String host="";
	
	private BlockingQueue<RequestMsg> outMsg;
	
	private java.net.Socket tcpsocket;

	private AtomicBoolean isstopped = new AtomicBoolean();
	
	public EndPointRequest(EndPointService service, String host, int port) {		
	
		this.service=service;
		this.host = host;
		this.port=port;

		isstopped.set(true);
		outMsg = new BlockingQueue<RequestMsg>(1000);
		
		try {

			logger.debug("open endpoint socket for");
			
			tcpsocket = new java.net.Socket(host, port);
			isstopped.set(false);
		
		} catch (UnknownHostException e) {
		
			logger.warn("unknown host " + getAddress() +  " error " + e.getMessage());

		} catch (IOException e) {

			logger.warn("IOException host " + getAddress() +  " error " + e.getMessage());

		}
	
	}	
	
	public void gotRequest(RequestMsg msg){

		if(!isstopped.get()) {

			lastused = System.currentTimeMillis();
			outMsg.add(msg);
			
		}
	
	}
	
	@Override
	public void run() {

		RequestMsg msgOut;
		byte[] datain = new byte[BUFFER_SIZE];
		try {
		
			Socket peer_socket = getService().getPeerSocket();
			boolean peer_server_running = getService().getServer().getPeerServer().isRunning();
			
			while(!isstopped.get() && (msgOut = outMsg.take())!=null) {

				if(logger.isDebugEnabled())
					logger.debug("handling request from " + msgOut.getReplyTo() +  " to " + getAddress());

				lastused = System.currentTimeMillis();

				try {
			
					tcpsocket.getOutputStream().write(msgOut.getHttpData(), 0, msgOut.getHttpData().length);
					tcpsocket.getOutputStream().flush();

					int cnt=0;

					int index = 0;
					while ((index = tcpsocket.getInputStream().read(datain, 0, BUFFER_SIZE))!= -1) {
						
						byte[] dataout = new byte[index];
						if(index>0) {
							
							System.arraycopy(datain, 0, dataout, 0, index);
						}
						
						ReplyMsg msg = new ReplyMsg(msgOut.getSession(), cnt, index < BUFFER_SIZE, dataout);
						
						if(peer_server_running && !msgOut.getReplyTo().equals(ServerParams.USERID)) {

							if(!peer_socket.send(msgOut.getReplyTo(), ServerParams.M2_PORT, msg.getBytes())) {

								logger.warn("Error sending request back to " + msgOut.getReplyTo() +  " error " + getService().getPeerSocket().getLastMessage());

								// error sending back 
								break;
							}

							
						} else {
							
							// request is from local client
							getService().getServer().getClientService().gotReply(msg);
							
						}

						lastused = System.currentTimeMillis();
						cnt++;
							

					}
				
				
				} catch (IOException e) {
					
					logger.warn("IO error handling request to " + getAddress() +  " error " + e.getMessage());
					
				}

				
			}
		
			if(tcpsocket!=null) {

				tcpsocket.close();
				
			}

		} catch (InterruptedException e) {
		
		
		} catch (IOException e) {

			logger.warn("IO error closing socket to " + getAddress() +  " error " + e.getMessage());
		
		}
	
	}

	public boolean isComplete() {
		
		isstopped.set(System.currentTimeMillis() - lastused > MAX_INACTIVE);
		return isstopped.get();
	
	}

	public String getAddress() {

		return host + ":" + String.valueOf(port);
	}

}
