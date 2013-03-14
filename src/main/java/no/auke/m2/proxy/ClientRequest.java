package no.auke.m2.proxy;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Random;
import java.util.StringTokenizer;

public class ClientRequest implements Runnable {
	
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

				// TODO Auto-generated catch block
				e.printStackTrace();
			
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
				
				e.printStackTrace();
			
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

			replyClientStream = new DataOutputStream(tcp_socket.getOutputStream());
			requestClientStream = new BufferedReader(new InputStreamReader(tcp_socket.getInputStream()));

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
			
			RequestMsg request = new RequestMsg(service.getPeerServer().getClientid(), session, url, port, data);
			
			if(service.getPeerSocket().send(service.getRemote(tcp_socket.getLocalSocketAddress()), service.getPeerSocket().getPort(), request.getBytes())){
				
				
			} else {
				
				// error sending
				
			}
			

		} catch (IOException e) {
			
			e.printStackTrace();
		
		}		
	
	}

	public boolean isComplete() {
		
		return iscomplete;
	
	}

}
