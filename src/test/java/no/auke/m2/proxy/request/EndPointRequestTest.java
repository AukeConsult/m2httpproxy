package no.auke.m2.proxy.request;

import static org.mockito.Mockito.spy;

public class EndPointRequestTest extends RequestBase {
	
	final String end = "\r\n\r\n"; 
	private String getHttpGet() {
		
		return "GET HTTP://wiki.auke.no /HTTP.1.1\r\n" + end; 
		
	} 

	EndPointRequest request;

	public void setUp() throws Exception {
		
		super.setUp();
		
	}
	public void test_connect_socket() {

		request = spy(new EndPointRequest(endpointservice, "wiki.auke.no",80, new RequestBase.NeighborCom()));
		assertTrue(request.isConnected());
				
	}
	
	public void test_send_socket() {

		request = spy(new EndPointRequest(endpointservice, "wiki.auke.no",80,new RequestBase.NeighborCom()));
		assertTrue(request.isConnected());
		
		java.net.Socket tcpsocket = request.getTcpSocket();
		
		
		
	
		
		
		
	}
	

}
