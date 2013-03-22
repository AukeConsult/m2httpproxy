package no.auke.m2.proxy.request;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.apache.http.impl.client.DefaultHttpClient;

import no.auke.m2.proxy.dataelements.RequestMsg;
import no.auke.util.StringConv;

public class EndPointRequestTest extends RequestBase {
	
	EndPointRequest request;
	public void setUp() throws Exception {
		super.setUp();
		
		DefaultHttpClient httpclient = new DefaultHttpClient();
		when(endpointservice.getHttpClient()).thenReturn(httpclient);

	}
	
	public void test_send_socket() {

		request = spy(new EndPointRequest(endpointservice,
					  new RequestMsg("", "", 100, "GET HTTP://startsiden.no/ FORMAT \r\n\r\n".getBytes()), 
				      new RequestBase.NeighborCom())
		);
		
		request.run();
		assertNotNull(request.getLastReplyMsg());
		System.out.println(StringConv.UTF8(request.getLastReplyMsg().getData()));
		
	}
	
}
