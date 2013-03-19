package no.auke.m2.proxy.dataelements;

import no.auke.m2.proxy.dataelements.Neighbor;
import junit.framework.TestCase;

public class NeighborTest extends TestCase {

	public void test() {
		
		Neighbor n1 = new Neighbor("testid");
		
		n1.setAlive(true);
		n1.setSpeedDownLoad(100);
		n1.setSpeedUpload(200);
		n1.setNumNeighbors(20);
		n1.setLastAlive(System.currentTimeMillis());
		
		Neighbor n2 = new Neighbor(n1.getBytes());

		assertEquals(n1.getClientid(),n2.getClientid());
		assertEquals(n1.isAlive(),n2.isAlive());
		assertEquals(n1.getSpeedDownLoad(),n2.getSpeedDownLoad());
		assertEquals(n1.getSpeedUpload(),n2.getSpeedUpload());
		assertEquals(n1.getLastAlive(),n2.getLastAlive());
		assertEquals(n1.getNumNeighbors(),n2.getNumNeighbors());
		
		
	}
	
	
}
