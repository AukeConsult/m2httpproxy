package no.auke.m2.proxy.request;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import junit.framework.TestCase;
import no.auke.m2.proxy.Server;
import no.auke.m2.proxy.comunicate.INeighborCom;
import no.auke.m2.proxy.dataelements.ReplyMsg;
import no.auke.m2.proxy.dataelements.RequestMsg;
import no.auke.m2.proxy.services.ClientService;
import no.auke.m2.proxy.services.EndPointService;
import no.auke.m2.proxy.services.NeighBorhodService;
import no.auke.p2p.m2.PeerServer;
import no.auke.p2p.m2.Socket;

public class RequestBase extends TestCase {
	
	class NeighborCom extends INeighborCom {


		boolean running=true;
		
		public NeighborCom () {
			
		}
		
		public NeighborCom (boolean running) {
			
			this.running=running;
			
		}
		
		@Override
		public boolean isRunning() {

			return running;
		}

		@Override
		public boolean replyHttpFromEndPoint(ReplyMsg msg, String replyTo) {

			return true;
		}

		@Override
		public boolean sendHttpToEndPoint(RequestMsg msg, String sendTo) {

			return true;
		}

		@Override
		public void close() {

			// TODO Auto-generated method stub
			
		}
		
	}

	PeerServer peerserver;
	Server proxyserver;
	ClientService clientservice;
	EndPointService endpointservice;
	NeighBorhodService neigborhodservice;
	
	Socket peer_socket;	
	java.net.Socket socket;
	
	PipedOutputStream send_socket_pipe = new PipedOutputStream();
	PipedInputStream read_socket_pipe = new PipedInputStream();
	
	private ExecutorService executor = Executors.newCachedThreadPool();
	
	public void setUp() throws Exception {
		
		peerserver = mock(PeerServer.class);
		when(peerserver.isRunning()).thenReturn(true);
				
		InetAddress address=mock(InetAddress.class);
		when(address.getHostAddress()).thenReturn("test");

		socket=mock(java.net.Socket.class);
		when(socket.getInetAddress()).thenReturn(address);
		when(socket.getPort()).thenReturn(1000);
		
		//when(socket.getInputStream()).thenReturn(new PipedInputStream(send_socket_pipe));
		when(socket.getOutputStream()).thenReturn(new PipedOutputStream(read_socket_pipe));
		
		peer_socket = mock(Socket.class);
		when(peer_socket.send(anyString(), anyInt(), (byte[])any())).thenReturn(true);

		proxyserver = mock(Server.class); 
		when(proxyserver.getClientid()).thenReturn("localpoint");
		when(proxyserver.getExecutor()).thenReturn(executor);
		when(proxyserver.getPeerServer()).thenReturn(peerserver);
		
		clientservice = spy(new ClientService(proxyserver));
		endpointservice = spy(new EndPointService(proxyserver));

		neigborhodservice = mock(NeighBorhodService.class);
		when(neigborhodservice.getRemoteEndPoint(anyString())).thenReturn("endpoint");
		
		when(proxyserver.getNeighborService()).thenReturn(neigborhodservice);
		when(proxyserver.getEndpointService()).thenReturn(endpointservice);
		when(proxyserver.getClientService()).thenReturn(clientservice);
		
	}
	
	void send_tcp(byte[] sendhttp) throws IOException{
		
		send_socket_pipe = new PipedOutputStream();
		when(socket.getInputStream()).thenReturn(new PipedInputStream(send_socket_pipe));

		send_socket_pipe.write(sendhttp);
		send_socket_pipe.flush();
		send_socket_pipe.close();

	}
	
	public void testDefault() {
	}
	

}
	