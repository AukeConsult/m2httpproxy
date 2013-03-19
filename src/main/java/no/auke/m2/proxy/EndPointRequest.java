package no.auke.m2.proxy;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.auke.p2p.m2.general.BlockingQueue;

public class EndPointRequest implements Runnable {
	
	private static final Logger logger = LoggerFactory.getLogger(EndPointRequest.class);	
	
	private static final int BUFFER_SIZE = 32768;
	private static final long MAX_INACTIVE = 60000; //(one minute) 

	private EndPointService service;
	private long lastused=0;
	private int port=0;
	private String host="";
	
	private BlockingQueue<RequestMsg> outMsg;
	
	private Socket tcpsocket;

	private AtomicBoolean isstopped = new AtomicBoolean();
	
	public EndPointRequest(EndPointService service, String host, int port) {		
	
		this.service=service;
		this.host=host;
		this.port=port;

		isstopped.set(false);
		outMsg = new BlockingQueue<RequestMsg>(1000);
		
		try {
		
			tcpsocket = new Socket(host, port);
		
		} catch (UnknownHostException e) {
		
			// TODO Auto-generated catch block
			e.printStackTrace();
		
		} catch (IOException e) {
			
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		
			while(!isstopped.get() && (msgOut = outMsg.take())!=null) {
				
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
						
						if(service.getPeerSocket().send(msgOut.getReplyTo(), service.getPeerSocket().getPort(), msg.getBytes())) {
							
							
						} else {
							
							// error sending back 
							break;
						}

						lastused = System.currentTimeMillis();
						cnt++;
							

					}
				
				
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				
			}
		
			tcpsocket.close();

		} catch (InterruptedException e) {
		
			// TODO Auto-generated catch block
			e.printStackTrace();
		
		} catch (IOException e) {
			
			// TODO Auto-generated catch block
			e.printStackTrace();
		
		}
	
	}

	public boolean isComplete() {
		
		isstopped.set(System.currentTimeMillis() - lastused > MAX_INACTIVE);
		return isstopped.get();
	
	}

	public String getAddress() {

		// TODO Auto-generated method stub
		return host + ":" + String.valueOf(port);
	}

}
