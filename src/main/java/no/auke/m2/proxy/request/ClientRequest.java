package no.auke.m2.proxy.request;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Random;
import java.util.StringTokenizer;

import no.auke.m2.proxy.ClientService;
import no.auke.m2.proxy.dataelements.ReplyMsg;
import no.auke.m2.proxy.dataelements.RequestMsg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientRequest implements Runnable {
	
	private static final Logger logger = LoggerFactory.getLogger(ClientRequest.class);	
	
	public static Random sessions = new Random();
	
	private Socket tcp_socket = null;
	private int session=0; 
	
	private DataOutputStream replyClientStream;
	private BufferedReader requestClientStream;
	
	private ClientService service;
	
	private boolean iscomplete=false;
	
	public ClientRequest(ClientService service, Socket tcp_socket) {		
	
		this.tcp_socket = tcp_socket;
		this.session=sessions.nextInt();
		this.service=service;
	
	}	
	
	public void gotReply(ReplyMsg reply){
		
		if(reply.getData()!=null) {
			
			try {
			
				replyClientStream.write(reply.getData(), 0, reply.getData().length);
				replyClientStream.flush();
			
			} catch (IOException e) {

				logger.warn("IO error sending to browser " + e.getMessage());
			
			}
		
		}
		
		if(reply.isComplete()) {
			
			try {
				
				if (replyClientStream != null) {
					replyClientStream.close();
				}
				if (requestClientStream != null) {
					requestClientStream.close();
				}
				if (tcp_socket != null) {
					tcp_socket.close();
				}
				
			} catch (IOException e) {
				
				logger.warn("IO error closing browser request " + e.getMessage());
			
			}	
			
			iscomplete=true;
			
			
		}
		
		
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

			requestClientStream = new BufferedReader(new InputStreamReader(tcp_socket.getInputStream()));
			replyClientStream = new DataOutputStream(tcp_socket.getOutputStream());

			String inputLine;
			
			int cnt = 0;
			String urlToCall = "";
			
			// /////////////////////////////////
			// begin get request from client
			
			while ((inputLine = requestClientStream.readLine()) != null) {

				System.out.println(String.valueOf(cnt) + " " + inputLine);

				try {
				
					StringTokenizer tok = new StringTokenizer(inputLine);
					tok.nextToken();
				
				} catch (Exception e) {
					
					break;
				
				}
				
				// parse the first line of the request to find the url
				if (cnt == 0) {
					
					String[] tokens = inputLine.split(" ");
					urlToCall = tokens[1];
					
					// can redirect this to output log
					System.out.println("Request for : " + urlToCall);
				
				}

				cnt++;
			
			}
			
			// send all
			String url="";
			int port=0;
			byte[] data=null;
			 
			
			String endpoint = service.getNeighborService().getRemoteEndPoint(browser_address);
			
			if(endpoint.isEmpty()) {

				RequestMsg request = new RequestMsg(service.getPeerServer().getClientid(), session, url, port, data);
				
				if(service.getPeerSocket().send(service.getNeighborService().getRemoteEndPoint(browser_address), service.getPeerSocket().getPort(), request.getBytes())){
					
					
				} else {
					
					service.getNeighborService().resetRemoteEndPoint(browser_address);
					service.getNeighborService().setNotAlive(endpoint);
					
					// sending direct reply with error
					
					gotReply(new ReplyMsg(ReplyMsg.ErrCode.LOCAL_SEND_REMOTE,"error sending request to remote proxy"));
					
					// error sending
					logger.warn("Error sending request to end point " + endpoint + " m2 error " + service.getPeerSocket().getLastMessage() );
					
				}
				
			} else {
				
				
				
			}
			
			

		} catch (IOException e) {
			
			logger.warn("IO error reading browser request " + e.getMessage());
		
		}		
	
	}

	public boolean isComplete() {
		
		return iscomplete;
	
	}

}
