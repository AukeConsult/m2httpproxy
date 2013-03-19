package examples.tcpserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * This class is a subclass of Thread that handles an individual connection
 * between a client and a Service provided by this server. Because each such
 * connection has a thread of its own, each Service can have multiple
 * connections pending at once. Despite all the other threads in use, this is
 * the key feature that makes this a multi-threaded server implementation.
 **/
public class Connection extends Thread {


	Socket client; // The socket to talk to the client through
	IService service; // The service being provided to that client
	Server server;

	/**
	 * This constructor just saves some state and calls the superclass
	 * constructor to create a thread to handle the connection. Connection
	 * objects are created by Listener threads. These threads are part of the
	 * server's ThreadGroup, so all Connection threads are part of that group,
	 * too.
	 **/
	public Connection(Server server, Socket client, IService service) {

		super("Server.Connection:" + client.getInetAddress().getHostAddress() + ":" + client.getPort());
		this.client = client;
		this.service = service;
		this.server = server;

	}

	/**
	 * This is the body of each and every Connection thread. All it does is pass
	 * the client input and output streams to the serve() method of the
	 * specified Service object. That method is responsible for reading from and
	 * writing to those streams to provide the actual service. Recall that the
	 * Service object has been passed from the Server.addService() method to a
	 * Listener object to the ConnectionManager.addConnection() to this
	 * Connection object, and is now finally getting used to provide the
	 * service. Note that just before this thread exits it calls the
	 * ConnectionManager.endConnection() method to wake up the ConnectionManager
	 * thread so that it can remove this Connection from its list of active
	 * connections.
	 **/
	public void run() {

		try {
			InputStream in = client.getInputStream();
			OutputStream out = client.getOutputStream();
			service.serve(in, out);
		} catch (IOException e) {
			server.log(e);
		} finally {
			server.getConnectionManager().endConnection();
		}
	}
}
