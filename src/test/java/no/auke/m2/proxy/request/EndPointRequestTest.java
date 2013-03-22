package no.auke.m2.proxy.request;

//import static org.mockito.Mockito.when;
//import static org.mockito.Matchers.any;
//import static org.mockito.Matchers.anyInt;
//import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.spy;

import no.auke.m2.proxy.dataelements.RequestMsg;
import no.auke.util.StringConv;

public class EndPointRequestTest extends RequestBase {
	
	EndPointRequest request;
	public void setUp() throws Exception {
		super.setUp();
	}
	
	public void test_send_socket() {

		request = spy(new EndPointRequest(endpointservice,
					  new RequestMsg("", "", 100, "GET HTTP://wiki.auke.no/ FORMAT \r\n\r\n".getBytes()), 
				      new RequestBase.NeighborCom())
		);
		
		request.run();
		assertNotNull(request.getLastReplyMsg());
		System.out.println(StringConv.UTF8(request.getLastReplyMsg().getData()));
		
	}
	
}
