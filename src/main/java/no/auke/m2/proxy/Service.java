package no.auke.m2.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.auke.p2p.m2.PeerServer;
import no.auke.p2p.m2.agent.AgentInterface;
import no.auke.p2p.m2.agent.NetAddress;
import no.auke.p2p.m2.general.LicenseReasons;
import no.auke.util.FileUtil;

public class Service {
	
	private static final Logger logger = LoggerFactory.getLogger(Service.class);	

	PeerServer server;
	
	private void initPeerServer(){

		if(server==null) {

			// TODO: get a better device id
			String deviceid = AgentInterface.convertToHex(AgentInterface.getHash(ServerParams.DEVICEID + ServerParams.NETSPACEID));
			server = new PeerServer(ServerParams.NETSPACEID,
					                     ServerParams.APPID, 
					                     deviceid, 
					                     ServerParams.USERDIR,
					                     ServerParams.BOOTADDRESS,
					                     ServerParams.USEMIDDLEMAN, // using middleman, set on netspace
					                     new PeerServerListener(ServerParams.DEBUG){
											
											public void printLog(String message) {
											
												onMessage(message);	
											
											}
											
											public void onServiceConnected(
													NetAddress publicAddress,
													NetAddress kaServerAddress) {
												
												
											}
											
											public void connectionRejected(
													NetAddress kaServerAddress,
													String msg) {
												
												onMessage("user rejected from KA");
												server.stopServer();
											}
							
											@Override
											public void onLicenseError(
													LicenseReasons reason,
													String licenseKey) {
											
												onMessage("license :" + licenseKey + " is invalid, reason: " + reason.name());
												server.stopServer();
												
											}
										}
										);
			
			//HUYDO: read license from resource
			//if we don't have license on disk or we did not force any license, the trial will be used
			
			this.server.setLicenseRegistrationHandler(ServerParams.getLicenseRegistrationHandler());		
			if(this.getClass().getResourceAsStream("/m2clientchat.license")!=null) {
			
				this.server.setLicense(FileUtil.readFromFile(this.getClass().getResourceAsStream("/m2clientchat.license")));
			
			}
				
		}
		
	}
	
	public Service() {}
	
	public void startProxy() {

		initPeerServer();
		
		NeighBorhodService neighbors = new NeighBorhodService(server,"defaultendpoint");
		
		server.start("", ServerParams.M2_PORT, ServerParams.USERID);
		if(server.isConnected()) {
			
			server.getExecutor().execute(neighbors);
			server.getExecutor().execute(new ClientService(server,neighbors));
			server.getExecutor().execute(new EndPointService(server,neighbors));		
			
		} else {
			
			logger.warn("can not start peerserver " + server.getLastmessage());
		}

		
		
	}

	public void stopProxy() {
		
		server.stopServer();
		
	}
	

}
