package no.auke.m2.proxy;

import java.util.Arrays;
import java.util.Random;

import junit.framework.TestCase;

public class RequestMsgTest extends TestCase {

	Random rnd = new Random();
	public void test(){
		
		byte[] data = new byte[100];
		
		rnd.nextBytes(data);
		
		RequestMsg msg = new RequestMsg("leif", 100, "localhost", 2100, data);
		
		RequestMsg msg2 = new RequestMsg(msg.getBytes());
		assertEquals(msg.getReplyTo(),msg2.getReplyTo());
		assertEquals(msg.getSession(),msg2.getSession());
		assertEquals(msg.getHost(),msg2.getHost());
		assertEquals(msg.getPort(),msg2.getPort());
		assertTrue(Arrays.equals(msg.getHttpData(), msg2.getHttpData()));
		
	}
}
