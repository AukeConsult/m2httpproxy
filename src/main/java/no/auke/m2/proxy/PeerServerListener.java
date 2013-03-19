/*
 * This file is part of Keep Alive project, mobile soft socket 
 * 
 * Copyright (c) 2011-2012 Leif Auke <leif@auke.no> - All rights
 * reserved.
 * 
 */

package no.auke.m2.proxy;

import no.auke.p2p.m2.agent.NetAddress;
import no.auke.p2p.m2.general.IListener;

public abstract class PeerServerListener extends IListener {

	public PeerServerListener(int Loglevel) {
		super(Loglevel);

	}

	@Override
	public final void onServiceDisconnected(NetAddress kaServerAddress) {
		trace("Disconnected from KA " + kaServerAddress.getAddressPort());	
	}

	@Override
	public void connectionRejected(NetAddress kaServerAddress, String msg) {
		message("Connection rejected from KA " + kaServerAddress.getAddressPort() + " " + msg);
	}

	@Override
	public final void onPeerConnected(NetAddress peerAddress) {
		trace("Connected to peer " + peerAddress.getAddressPort());
	}

	@Override
	public final void onPeerDisconnected(NetAddress peerAddress) {
		trace("Disconnected from peer " + peerAddress.getAddressPort());
	}	
	
	public final void onServiceStarted(String message) {}
	public final void onServiceStopped(String message) {}
	public final void onMessageSend(NetAddress peerAddress, int socketPort, int messageId, int size) {}
	public final void onMessageRecieved(NetAddress peerAddress, int socketPort, int messageId, int size) {}
	public final void onMessageDisplay(String message) {}
	public final void onMessageConfirmed(NetAddress peerAddress, int messageId) {}
	public final void onTraffic(float bytes_in_sec, float bytes_out_sec,long bytes_total_in, long bytes_total_out) {}
}
