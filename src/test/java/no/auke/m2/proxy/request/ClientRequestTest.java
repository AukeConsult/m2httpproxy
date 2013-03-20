package no.auke.m2.proxy.request;

import java.io.IOException;
import java.util.Arrays;

import no.auke.m2.proxy.dataelements.ReplyMsg;
import no.auke.m2.proxy.dataelements.RequestMsg;
import no.auke.util.StringConv;

//import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
//import static org.powermock.api.mockito.PowerMockito.mock;
//import static org.powermock.api.mockito.PowerMockito.when;

public class ClientRequestTest extends RequestBase {

	ClientRequest request;

	public void setUp() throws Exception {
		
		super.setUp();
		request = spy(new ClientRequest(clientservice, socket, new RequestBase.NeighborCom()));
		
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
	
	public void test_send_http_comservice_not_running() throws IOException {
		
		request = spy(new ClientRequest(clientservice, socket, new RequestBase.NeighborCom(false)));

		byte[] sendhttp = StringConv.getBytes("GET http://test.no format \r\nasdadsasdasd\r\nasdasdasd\r\n\r\n");
		send_tcp(sendhttp);
		request.run();
		
		verify(endpointservice,times(1)).gotRequest((RequestMsg) anyObject());
		
	}	

	public void test_send_http_comservice_running() throws IOException {
		
		request = spy(new ClientRequest(clientservice, socket, new RequestBase.NeighborCom(true)));

		byte[] sendhttp = StringConv.getBytes("GET http://test.no format \r\nasdadsasdasd\r\nasdasdasd\r\n\r\n");
		send_tcp(sendhttp);
		request.run();
		
		verify(endpointservice,never()).gotRequest((RequestMsg) anyObject());
		
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
	
	public void test_send_http_strip_last_backspace() throws IOException {
		
		byte[] sendhttp = StringConv.getBytes("GET http://test.no/ format \r\nasdadsasdasd\r\nasdasdasd\r\n\r\n");
		send_tcp(sendhttp);
		request.run();	
		
		assertNotNull(request.getLastRequestMsg());
		assertEquals("test.no",request.getLastRequestMsg().getHost());
		
	}	
	
	public void test_send_http_port() throws IOException {
		
		byte[] sendhttp = StringConv.getBytes("GET https://test.no:3000 format \r\nasdadsasdasd\r\nasdasdasd\r\n\r\n");
		send_tcp(sendhttp);
		request.run();	
		
		assertNotNull(request.getLastRequestMsg());
		assertEquals("test.no",request.getLastRequestMsg().getHost());
		assertEquals(3000,request.getLastRequestMsg().getPort());
		
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
