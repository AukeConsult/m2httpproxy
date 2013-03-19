package no.auke.m2.proxy.request;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import no.auke.m2.proxy.dataelements.ReplyMsg;
import no.auke.m2.proxy.dataelements.RequestMsg;
import no.auke.m2.proxy.services.ClientService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientRequest implements Runnable {
	
	private static final Logger logger = LoggerFactory.getLogger(ClientRequest.class);

	private int MAX_WAIT = 15000; // 15 seconds	
	
	public static Random sessions = new Random();
	
	// for testing purpose
	private RequestMsg last_request;
	public RequestMsg getLastRequestMsg() {
		return last_request;
	}	
	private ReplyMsg last_reply;
	public ReplyMsg getLastReplyMsg() {
		return last_reply;
	}
	
	
	private Socket tcp_socket = null;
	private int session=0; 
	
	private ClientService service;
	private boolean iscomplete=false;
	
	private AtomicLong last_activity=new AtomicLong();
	public long getLastActivity() {
	
		return last_activity.get();
	}
	public void setLastActivity(long last_activity_time) {
	
		this.last_activity.set(last_activity_time);
	}

	public ClientRequest(ClientService service, Socket tcp_socket) {		
	
		this.tcp_socket = tcp_socket;
		this.session=sessions.nextInt();
		this.service=service;
	
	}	
	
	public boolean gotReply(ReplyMsg reply){
		
		last_reply = reply;
		last_activity.set(System.currentTimeMillis());
		
		if(reply.getErrcode()!=ReplyMsg.ErrCode.OK){
			
			iscomplete=true;
		}
		
		if(reply.getData()!=null) {
			
			try {

				// private DataOutputStream replyClientStream = new DataOutputStream(tcp_socket.getOutputStream());

				tcp_socket.getOutputStream().write(reply.getData(), 0, reply.getData().length);
				tcp_socket.getOutputStream().flush();
			
			} catch (IOException e) {

				logger.warn("IO error sending to browser " + e.getMessage());
				return false;
			}
		
		}
		
		if(iscomplete || reply.isComplete()) {
			
			try {
				
				if (tcp_socket != null) {
					tcp_socket.close();
				}
				
			} catch (IOException e) {
				
				logger.warn("IO error closing browser request " + e.getMessage());
				return false;
			
			}	
			iscomplete=true;
			
		}
		return true;
			
	}

	public Integer getSession() {

		return session;
	}
	
	@Override
	public void run() {

		// get input from user
		// send request to server
		// get response from server
		// send response to user

		try {

			String browser_address = tcp_socket.getInetAddress().getHostAddress() + ":"+ String.valueOf(tcp_socket.getPort());
			
			if(logger.isDebugEnabled())
				logger.debug("got browser request from address " + browser_address);

			
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			byte[] inbuffer = new byte[4096];
			int bytes=0;
			while((bytes=tcp_socket.getInputStream().read(inbuffer,0,4096))!=-1){
				buffer.write(inbuffer, 0,bytes);
			}
			byte[] data=buffer.toByteArray();

			String host="";
			int port=0;
			
			byte[] first_line = new byte[1000];
			for(int i=0;i<1000;i++){
				
				first_line[i]=data[i];
				if(first_line[i]=='\n') {
					
					// get first line
					String http_command = new String(first_line);

					String[] commands = http_command.split(" ");
					
					String http = commands[1].split("//")[0];
					String address = commands[1].split("//")[1];
					
					host = address.split(":")[0];
					port = address.split(":").length>1?Integer.valueOf(address.split(":")[1]):http.toLowerCase().equals("https:")?443:80;
					
					break;
				}
				
			}
			
			
			String endpoint = service.getNeighborService().getRemoteEndPoint(browser_address);
			
			if(!endpoint.isEmpty()) {

				last_request = new RequestMsg(service.getPeerServer().getClientid(), endpoint, session, host, port, data);
				
				if(service.getPeerSocket().send(endpoint, service.getPeerSocket().getPort(), last_request.getBytes())){
					
					// ok send
					if(logger.isDebugEnabled())
						logger.debug("request sent to " + endpoint + " from " + browser_address +  " session " + String.valueOf(session));

					last_activity.set(System.currentTimeMillis());
					

				} else {
					
					last_request=null;
					
					service.getNeighborService().resetRemoteEndPoint(browser_address);
					service.getNeighborService().setNotAlive(endpoint);
					
					// sending direct reply with error
					
					gotReply(new ReplyMsg(ReplyMsg.ErrCode.LOCAL_ERR_SEND_REMOTE,"error sending request to remote proxy"));
					
					// error sending
					logger.warn("Error sending request to end point " + endpoint + " m2 error " + service.getPeerSocket().getLastMessage() );
					
				}
				
			} else {

				gotReply(new ReplyMsg(ReplyMsg.ErrCode.LOCAL_ERR_NO_ENDPOINT,"no endpoint found for " + browser_address));

				// error sending
				logger.warn("can not find any endpoint for address " + browser_address);
				
			}

		} catch (IOException e) {
			
			logger.warn("IO error reading browser request " + e.getMessage());
		
		}		
	
	}

	public void checkReply() {

		if(last_activity.get() > 0 && System.currentTimeMillis() - last_activity.get() > MAX_WAIT) {

			gotReply(new ReplyMsg(ReplyMsg.ErrCode.LOCAL_ERR_REMOTE_TIMEOUT,"to long waiting for reply from proxy"));
		
		}
	}
	
	public boolean isComplete() {		
		return iscomplete;
	}


}
