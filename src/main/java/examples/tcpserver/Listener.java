package examples.tcpserver;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;


/**
 * This nested Thread subclass is a "listener". It listens for connections on a
 * specified port (using a ServerSocket) and when it gets a connection request,
 * it calls a method of the ConnectionManager to accept (or reject) the
 * connection. There is one Listener for each Service being provided by the
 * Server. The Listener passes the Server object to the ConnectionManager, but
 * doesn't do anything with it itself.
 */

public class Listener extends Thread {


	ServerSocket listen_socket; // The socket we listen for connections on
	int port; // The port we're listening on
	IService service; // The service to provide on that port
	boolean stop = false; // Whether we've been asked to stop

	Server server;

	/**
	 * The Listener constructor creates a thread for itself in the specified
	 * threadgroup. It creates a ServerSocket to listen for connections on the
	 * specified port. It arranges for the ServerSocket to be interruptible, so
	 * that services can be removed from the server.
	 **/

	public Listener(Server server, ThreadGroup group, int port, IService service) throws IOException {

		super(group, "Listener:" + port);
		listen_socket = new ServerSocket(port);
		// give the socket a non-zero timeout so accept() can be interrupted
		listen_socket.setSoTimeout(600000);
		this.port = port;
		this.service = service;
		this.server = server;

	}

	/** This is the nice way to get a Listener to stop accepting connections */
	public void pleaseStop() {

		this.stop = true; // set the stop flag
		this.interrupt(); // and make the accept() call stop blocking
	}

	/**
	 * A Listener is a Thread, and this is its body. Wait for connection
	 * requests, accept them, and pass the socket on to the ConnectionManager
	 * object of this server
	 **/

	public void run() {

		while (!stop) { // loop until we're asked to stop.

			try {

				Socket client = listen_socket.accept();
				server.getConnectionManager().addConnection(client, service);

			} catch (InterruptedIOException e) {} catch (IOException e) {

				server.log(e);

			}
		}
	}
}


