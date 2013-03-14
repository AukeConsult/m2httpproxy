// This example is from _Java Examples in a Nutshell_. (http://www.oreilly.com)
// Copyright (c) 1997 by David Flanagan
// This example is provided WITHOUT ANY WARRANTY either expressed or implied.
// You may study, use, modify, and distribute it for non-commercial purposes.
// For any commercial use, see http://www.davidflanagan.com/javaexamples

package no.auke.m2.tcpserver;

import java.io.*;
import java.util.*;

import no.auke.m2.tcpserver.examples.ControlService;

/**
 * This class is a generic framework for a flexible, multi-threaded server. It
 * listens on any number of specified ports, and, when it receives a connection
 * on a port, passes input and output streams to a specified Service object
 * which provides the actual service. It can limit the number of concurrent
 * connections, and logs activity to a specified stream.
 **/
public class Server {


	/**
	 * A main() method for running the server as a standalone program. The
	 * command-line arguments to the program should be pairs of servicenames and
	 * port numbers. For each pair, the program will dynamically load the named
	 * Service class, instantiate it, and tell the server to provide that
	 * Service on the specified port. The special -control argument should be
	 * followed by a password and port, and will start special server control
	 * service running on the specified port, protected by the specified
	 * password.
	 **/
	
	public static void main(String[] args) {

		try {

			
			if (args.length < 2) {
				
				// Check number of arguments
				throw new IllegalArgumentException("Must start at least one service");
			
			}

			// Create a Server object that uses standard out as its log and
			// has a limit of ten concurrent connections at once.
			Server s = new Server(System.out, 10);

			// Parse the argument list
			int i = 0;
			while (i < args.length) {
				
				if (args[i].equals("-control")) { // Handle the -control argument
													 
					i++;
					String password = args[i++];
					int port = Integer.parseInt(args[i++]);
					
					s.addService(new ControlService(s, password), port); // add control service
																	 
				} else {
					
					// Otherwise start a named service on the specified port.
					// Dynamically load and instantiate a class that implements
					// Service.
					
					String serviceName = args[i++];
					Class<?> serviceClass = Class.forName(serviceName); // dynamic load
																		 
					IService service = (IService) serviceClass.newInstance(); // instantiate
					int port = Integer.parseInt(args[i++]);
					
					s.addService(service, port);
				
				}
			}
		
		} catch (Exception e) { // Display a message if anything goes wrong
		
			System.err.println("Server: " + e);
			System.err.println("Usage: java Server [-control <password> <port>] " + "[<servicename> <port> ... ]");
			System.exit(1);
		
		}
	}

	// This is the state for the server
	
	private ConnectionManager connectionManager; // The ConnectionManager object
	
	public ConnectionManager getConnectionManager() {
	
		return connectionManager;
	}

	private Hashtable<Integer, Listener> services; // The current services and their ports
	
	public Hashtable<Integer, Listener> getServices() {
	
		return services;
	}

	ThreadGroup threadGroup; // The threadgroup for all our threads
	PrintWriter logStream; // Where we send our logging output to

	/**
	 * This is the Server() constructor. It must be passed a stream to send log
	 * output to (may be null), and the limit on the number of concurrent
	 * connections. It creates and starts a ConnectionManager thread which
	 * enforces this limit on connections.
	 **/
	public Server(OutputStream logStream, int maxConnections) {

		setLogStream(logStream);
		log("Starting server");
		
		threadGroup = new ThreadGroup("Server");
		connectionManager = new ConnectionManager(this, threadGroup, maxConnections);
		connectionManager.start();
		services = new Hashtable<Integer, Listener>();
	
	}

	/**
	 * A public method to set the current logging stream. Pass null to turn
	 * logging off
	 **/
	public void setLogStream(OutputStream out) {

		if (out != null) {
		
			logStream = new PrintWriter(new OutputStreamWriter(out));
		
		} else {
			
			logStream = null;
		}
	}

	/** Write the specified string to the log */
	protected synchronized void log(String s) {

		if (logStream != null) {
			
			logStream.println("[" + new Date() + "] " + s);
			logStream.flush();
		}
	}

	/** Write the specified object to the log */
	protected void log(Object o) {

		log(o.toString());
	}

	/**
	 * This method makes the server start providing a new service. It runs the
	 * specified Service object on the specified port.
	 **/
	
	public void addService(IService service, int port) throws IOException {

		Integer key = new Integer(port); // the hashtable key
		// Check whether a service is already on that port
		if (services.get(key) != null)
			throw new IllegalArgumentException("Port " + port + " already in use.");
		
		// Create a Listener object to listen for connections on the port
		Listener listener = new Listener(this, threadGroup, port, service);
		
		// Store it in the hashtable
		services.put(key, listener);
		
		// Log it
		log("Starting service " + service.getClass().getName() + " on port " + port);
		
		// Start the listener running.
		listener.start();
	
	}

	/**
	 * This method makes the server stop providing a service on a port. It does
	 * not terminate any pending connections to that service, merely causes the
	 * server to stop accepting new connections
	 **/
	public void removeService(int port) {

		Integer key = new Integer(port); // hashtable key
		
		// Look up the Listener object for the port in the hashtable of services
		final Listener listener = (Listener) services.get(key);
		if (listener == null) {
			return;
		}
		
		// Ask the listener to stop
		listener.pleaseStop();
		
		// Remove it from the hashtable
		services.remove(key);
		
		// And log it.
		log("Stopping service " + listener.service.getClass().getName() + " on port " + port);
	}


}
