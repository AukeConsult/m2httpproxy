package no.auke.m2.proxy.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.auke.m2.proxy.Server;
import no.auke.m2.proxy.ServerParams;
import no.auke.m2.proxy.comunicate.INeighborCom;
import no.auke.m2.proxy.comunicate.M2NeighborCom;
import no.auke.m2.proxy.comunicate.NoNeighborCom;
import no.auke.p2p.m2.Socket;
import no.auke.p2p.m2.SocketListener;

public abstract class IServiceBase implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(IServiceBase.class);

	private Server server;
	public Server getServer() {
		return server;
	}
	
	private INeighborCom neighborCom;
	public INeighborCom getNeighborSocket() {
	
		return neighborCom;
	}

	public abstract void onInBuffer(byte[] buffer);
	
	public IServiceBase (Server server) {
		
		this.server = server;

		// communicate with m2
    	if(server.getPeerServer().isRunning()) {
    		
    		Socket peer_socket = server.getPeerServer().open(ServerParams.HTTP_SERVICE_PORT, new SocketListener(){

    			@Override
    			public void onIncomming(byte[] buffer) {

    				onInBuffer(buffer);
    				
    			}});

    		logger.info("Open softsocket m2 network on port: " +  String.valueOf(ServerParams.HTTP_SERVICE_PORT));

    		neighborCom = new M2NeighborCom(peer_socket);

    	} else {

    		neighborCom = new NoNeighborCom();
    		logger.info("No m2 network client running, http request is done locally");
    		
    	}
		
	}	
}
