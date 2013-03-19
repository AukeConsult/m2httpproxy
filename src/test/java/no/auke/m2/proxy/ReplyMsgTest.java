package no.auke.m2.proxy;

import java.util.Arrays;
import java.util.Random;

import junit.framework.TestCase;

public class ReplyMsgTest extends TestCase {
	
	Random rnd = new Random();
	
	public void test(){
		
		byte[] data = new byte[100];
		
		rnd.nextBytes(data);
		
		ReplyMsg msg = new ReplyMsg(200, 100, true, data);
		
		ReplyMsg msg2 = new ReplyMsg(msg.getBytes());
		
		assertEquals(msg.getSession(),msg2.getSession());
		assertEquals(msg.getOrder(),msg2.getOrder());
		assertEquals(msg.isComplete(),msg2.isComplete());
		assertTrue(Arrays.equals(msg.getData(), msg2.getData()));
		
	}

}
