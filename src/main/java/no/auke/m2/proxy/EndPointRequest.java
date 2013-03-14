package no.auke.m2.proxy;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Random;
import java.util.StringTokenizer;

public class EndPointRequest implements Runnable {
	
	private int session=0; 
	
	private EndPointService service;
	
	private boolean iscomplete=false;
	
	private String replyTo;
	
	public EndPointRequest(EndPointService service, int session) {		
	
		this.service=service;
		this.session=session;
	
	}	
	
	public void gotRequest(RequestMsg msg){
		
		if(msg.getData()!=null) {

			// send to web server 
			
			
		}
		
		if(!msg.getReplyTo().isEmpty()) {
			
			replyTo = msg.getReplyTo();
			
		}
		
//		if(reply.isComplete()) {
//			
//			try {
//				
//				if (replyClientStream != null) {
//					replyClientStream.close();
//				}
//				if (requestClientStream != null) {
//					requestClientStream.close();
//				}
//				if (tcp_socket != null) {
//					tcp_socket.close();
//				}
//				
//			} catch (IOException e) {
//				
//				e.printStackTrace();
//			
//			}	
//			
//			iscomplete=true;
//			
//			
//		}
		
		
	}

	public Integer getSession() {

		return session;
	}

	@Override
	public void run() {

		// got reply from  web server
		
		// send back to client
		
		byte [] data=null;
		
		ReplyMsg msg = new ReplyMsg(session, false, data);
		
		if(service.getPeerSocket().send(replyTo, service.getPeerSocket().getPort(), msg.getBytes())) {
			
			
		} else {
			
			// error sending back 
			
		}
	
	
	}

	public boolean isComplete() {
		
		return iscomplete;
	
	}

}
