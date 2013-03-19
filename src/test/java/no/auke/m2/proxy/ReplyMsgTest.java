package no.auke.m2.proxy;

import java.util.Arrays;
import java.util.Random;

import no.auke.m2.proxy.dataelements.ReplyMsg;

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
		assertEquals(msg.getErrcode(),msg2.getErrcode());
		assertTrue(Arrays.equals(msg.getData(), msg2.getData()));
		
	}
	
	public void test_error_message(){
		
		byte[] data = new byte[100];
		
		rnd.nextBytes(data);
		
		ReplyMsg msg = new ReplyMsg(ReplyMsg.ErrCode.LOCAL_SEND_REMOTE,"error sending remote");
		ReplyMsg msg2 = new ReplyMsg(msg.getBytes());
		
		assertEquals(msg.getErrcode(),msg2.getErrcode());
		assertEquals(new String(msg.getData()),new String(msg2.getData()));
		
		
	}	

}
