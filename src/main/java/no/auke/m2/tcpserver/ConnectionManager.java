package no.auke.m2.tcpserver;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Vector;

/**
 * This nested class manages client connections for the server. It maintains a
 * list of current connections and enforces the maximum connection limit. It
 * creates a separate thread (one per server) that sits around and wait()s to be
 * notify()'d that a connection has terminated. When this happens, it updates
 * the list of connections.
 **/

public class ConnectionManager extends Thread {


	private int maxConnections; // The maximum number of allowed connections

	public int getMaxConnections() {

		return maxConnections;
	}

	/** Change the current connection limit */
	public synchronized void setMaxConnections(int max) {

		maxConnections = max;
	}

	private Vector<Connection> connections; // The current list of connections

	Server server = null;

	/**
	 * Create a ConnectionManager in the specified thread group to enforce the
	 * specified maximum connection limit. Make it a daemon thread so the
	 * interpreter won't wait around for it to exit.
	 **/
	public ConnectionManager(Server server, ThreadGroup group, int maxConnections) {

		super(group, "ConnectionManager");
		this.setDaemon(true);
		this.maxConnections = maxConnections;
		connections = new Vector<Connection>(maxConnections);
		this.server = server;
		server.log("Starting connection manager.  Max connections: " + maxConnections);
	}

	/**
	 * This is the method that Listener objects call when they accept a
	 * connection from a client. It either creates a Connection object for the
	 * connection and adds it to the list of current connections, or, if the
	 * limit on connections has been reached, it closes the connection.
	 **/
	synchronized void addConnection(Socket s, IService service) {

		// If the connection limit has been reached
		if (connections.size() >= maxConnections) {

			try {

				PrintWriter out = new PrintWriter(s.getOutputStream());

				// Then tell the client it is being rejected.
				out.println("Connection refused; " + "server has reached maximum number of clients.");
				out.flush();

				// And close the connection to the rejected client.
				s.close();

				// And log it, of course
				server.log("Connection refused to " + s.getInetAddress().getHostAddress() + ":" + s.getPort() + ": max connections reached.");

			} catch (IOException e) {

				server.log(e);
			}

		} else { // Otherwise, if the limit has not been reached

			// Create a Connection thread to handle this connection
			Connection c = new Connection(server, s, service);

			// Add it to the list of current connections
			connections.addElement(c);

			// Log this new connection
			server.log("Connected to " + s.getInetAddress().getHostAddress() + ":" + s.getPort() + " on port " + s.getLocalPort() + " for service " + service.getClass().getName());

			// And start the Connection thread running to provide the
			// service
			c.start();

		}
	}

	/**
	 * A Connection object calls this method just before it exits. This method
	 * uses notify() to tell the ConnectionManager thread to wake up and delete
	 * the thread that has exited.
	 **/
	public synchronized void endConnection() {

		this.notify();
	}

	/**
	 * Output the current list of connections to the specified stream. This
	 * method is used by the Control service defined below.
	 **/
	public synchronized void printConnections(PrintWriter out) {

		for (int i = 0; i < connections.size(); i++) {
			Connection c = (Connection) connections.elementAt(i);
			out.println("CONNECTED TO " + c.client.getInetAddress().getHostAddress() + ":" + c.client.getPort() + " ON PORT " + c.client.getLocalPort() + " FOR SERVICE " + c.service.getClass().getName());
		}
	}

	/**
	 * The ConnectionManager is a thread, and this is the body of that thread.
	 * While the ConnectionManager methods above are called by other threads,
	 * this method is run in its own thread. The job of this thread is to keep
	 * the list of connections up to date by removing connections that are no
	 * longer alive. It uses wait() to block until notify()'d by the
	 * endConnection() method.
	 **/
	public void run() {

		while (true) { // infinite loop

			// Check through the list of connections, removing dead ones
			for (int i = 0; i < connections.size(); i++) {

				Connection c = (Connection) connections.elementAt(i);

				if (!c.isAlive()) {

					connections.removeElementAt(i);
					server.log("Connection to " + c.client.getInetAddress().getHostAddress() + ":" + c.client.getPort() + " closed.");
				}

			}

			// Now wait to be notify()'d that a connection has exited
			// When we wake up we'll check the list of connections again.
			try {
				synchronized (this) {
					this.wait();
				}
			} catch (InterruptedException e) {}
		}
	}
}
