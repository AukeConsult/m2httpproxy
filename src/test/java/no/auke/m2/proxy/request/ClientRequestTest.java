package no.auke.m2.proxy.request;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.InetAddress;
import java.util.Arrays;

import no.auke.m2.proxy.dataelements.ReplyMsg;
import no.auke.m2.proxy.services.ClientService;
import no.auke.m2.proxy.services.NeighBorhodService;
import no.auke.p2p.m2.PeerServer;
import no.auke.p2p.m2.Socket;
import no.auke.util.StringConv;
import junit.framework.TestCase;

//import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
//import static org.powermock.api.mockito.PowerMockito.mock;
//import static org.powermock.api.mockito.PowerMockito.when;

public class ClientRequestTest extends TestCase {

	ClientRequest request;
	PeerServer server;
	ClientService clientservice;
	NeighBorhodService neigborhodservice;
	
	Socket peer_socket;
	
	java.net.Socket socket;
	
	PipedOutputStream send_socket_pipe = new PipedOutputStream();
	PipedInputStream read_socket_pipe = new PipedInputStream();
	
	public void setUp() throws Exception {

		server = mock(PeerServer.class);
		when(server.getClientid()).thenReturn("localpoint");
		
		neigborhodservice = mock(NeighBorhodService.class);
		when(neigborhodservice.getRemoteEndPoint(anyString())).thenReturn("endpoint");

		InetAddress address=mock(InetAddress.class);
		when(address.getHostAddress()).thenReturn("test");

		socket=mock(java.net.Socket.class);
		when(socket.getInetAddress()).thenReturn(address);
		when(socket.getPort()).thenReturn(1000);
		
		//when(socket.getInputStream()).thenReturn(new PipedInputStream(send_socket_pipe));
		when(socket.getOutputStream()).thenReturn(new PipedOutputStream(read_socket_pipe));
		
		peer_socket = mock(Socket.class);
		when(peer_socket.send(anyString(), anyInt(), (byte[])any())).thenReturn(true);

		clientservice = spy(new ClientService(server, neigborhodservice));
		when(clientservice.getPeerSocket()).thenReturn(peer_socket);

		request = spy(new ClientRequest(clientservice, socket));
		
	}

	private void send_tcp(byte[] sendhttp) throws IOException{
		
		send_socket_pipe = new PipedOutputStream();
		when(socket.getInputStream()).thenReturn(new PipedInputStream(send_socket_pipe));

		send_socket_pipe.write(sendhttp);
		send_socket_pipe.flush();
		send_socket_pipe.close();

		
	}
	
	public void test_send_all_http_header_data() throws IOException {
		
		for(int i=0;i<10;i++) {

			String http = "GET http://test.no:3000 format \r\n";
			for(int x=0;x<i;x++){
				http+="sfdsdfsdfsdfsdfsdfsdfsdfsdf dfdf ewerwerwerwe eweewrwerwer \r\n";
			}
			
			http+="\r\n";
			http+="123123123123123saasdsvx cxcvfsdfsdfsdf DATATATA";
			
			byte[] sendhttp = StringConv.getBytes(http);
			send_tcp(sendhttp);
			
			request.run();	
			assertNotNull(request.getLastRequestMsg());
			assertNull(request.getLastReplyMsg());
			assertTrue(Arrays.equals(sendhttp, request.getLastRequestMsg().getHttpData()));
			
		}
		
	}
	
	public void test_send_http() throws IOException {
		
		byte[] sendhttp = StringConv.getBytes("GET http://test.no format \r\nasdadsasdasd\r\nasdasdasd\r\n\r\n");
		send_tcp(sendhttp);
		request.run();	
		
		assertNotNull(request.getLastRequestMsg());
		assertEquals("test.no",request.getLastRequestMsg().getHost());
		assertEquals(80,request.getLastRequestMsg().getPort());
		
		assertTrue(Arrays.equals(sendhttp, request.getLastRequestMsg().getHttpData()));
		
	}

	public void test_send_https() throws IOException {
		
		byte[] sendhttp = StringConv.getBytes("GET https://test.no format \r\nasdadsasdasd\r\nasdasdasd\r\n\r\n");
		send_tcp(sendhttp);
		request.run();	
		
		assertNotNull(request.getLastRequestMsg());
		assertEquals("test.no",request.getLastRequestMsg().getHost());
		assertEquals(443,request.getLastRequestMsg().getPort());
		
		assertTrue(Arrays.equals(sendhttp, request.getLastRequestMsg().getHttpData()));
		
	}
	
	public void test_send_http_port() throws IOException {
		
		byte[] sendhttp = StringConv.getBytes("GET https://test.no:3000 format \r\nasdadsasdasd\r\nasdasdasd\r\n\r\n");
		send_tcp(sendhttp);
		request.run();	
		
		assertNotNull(request.getLastRequestMsg());
		assertEquals("test.no",request.getLastRequestMsg().getHost());
		assertEquals(3000,request.getLastRequestMsg().getPort());
		
	}	

	public void test_fail_send() throws IOException {
		
		when(peer_socket.send(anyString(), anyInt(), (byte[])any())).thenReturn(false);

		byte[] sendhttp = StringConv.getBytes("GET http://test.no:3000 format \r\nasdadsasdasd\r\nasdasdasd\r\n\r\n");

		send_tcp(sendhttp);
		request.run();	
		
		assertNull(request.getLastRequestMsg());
		assertNotNull(request.getLastReplyMsg());

		assertEquals(ReplyMsg.ErrCode.LOCAL_ERR_SEND_REMOTE,request.getLastReplyMsg().getErrcode());
		
		
	}

	public void test_fail_no_endpoint() throws IOException {
		
		when(neigborhodservice.getRemoteEndPoint(anyString())).thenReturn("");

		byte[] sendhttp = StringConv.getBytes("GET http://test.no:3000 format \r\nasdadsasdasd\r\nasdasdasd\r\n\r\n");

		send_tcp(sendhttp);
		request.run();	
		
		assertNull(request.getLastRequestMsg());
		assertNotNull(request.getLastReplyMsg());

		assertEquals(ReplyMsg.ErrCode.LOCAL_ERR_NO_ENDPOINT,request.getLastReplyMsg().getErrcode());
		
		
	}
	
	public void test_gotReply() throws IOException {
		
		ReplyMsg msg = new ReplyMsg(0, 0, false, new byte[1]);
		request.gotReply(msg);
		assertNotNull(request.getLastReplyMsg());
		assertFalse(request.getLastReplyMsg().isComplete());
		
	}	

	public void test_gotReply_complete() throws IOException {
		
		ReplyMsg msg = new ReplyMsg(0, 0, true, new byte[1]);
		request.gotReply(msg);
		assertNotNull(request.getLastReplyMsg());
		assertTrue(request.getLastReplyMsg().isComplete());
		
	}	
	
	public void test_check_timeout() throws IOException {
		
		request.setLastActivity(System.currentTimeMillis() - 100000);
		request.checkReply();
		
		assertNotNull(request.getLastReplyMsg());
		assertTrue(request.isComplete());
		assertEquals(ReplyMsg.ErrCode.LOCAL_ERR_REMOTE_TIMEOUT,request.getLastReplyMsg().getErrcode());
		
	}	
	
}
