package examples.tcpserver.services;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.StringTokenizer;

import examples.tcpserver.IService;
import examples.tcpserver.Listener;
import examples.tcpserver.Server;



/**
 * This is a non-trivial service. It implements a command-based protocol that
 * gives password-protected runtime control over the operation of the server.
 * See the main() method of the Server class to see how this service is started.
 * 
 * The recognized commands are: password: give password; authorization is
 * required for most commands add: dynamically add a named service on a
 * specified port remove: dynamically remove the service running on a specified
 * port max: change the current maximum connection limit. status: display
 * current services, connections, and connection limit help: display a help
 * message quit: disconnect
 * 
 * This service displays a prompt, and sends all of its output to the user in
 * capital letters. Only one client is allowed to connect to this service at a
 * time.
 **/

public class ControlService implements IService {

	private Server server; // The server we control
	private String password; // The password we require
	
	public static boolean connected = false; // Whether a client is already connected to us

	/**
	 * Create a new Control service. It will control the specified Server
	 * object, and will require the specified password for authorization Note
	 * that this Service does not have a no argument constructor, which means
	 * that it cannot be dynamically instantiated and added as the other,
	 * generic services above can be.
	 **/
	
	public ControlService(Server server, String password) {

		this.server = server;
		this.password = password;
	
	}

	/**
	 * This is the serve method that provides the service. It reads a line the
	 * client, and uses java.util.StringTokenizer to parse it into commands and
	 * arguments. It does various things depending on the command.
	 **/

	public void serve(InputStream i, OutputStream o) throws IOException {

		// Setup the streams
		BufferedReader in = new BufferedReader(new InputStreamReader(i));
		PrintWriter out = new PrintWriter(new OutputStreamWriter(o));
		String line;
		boolean authorized = false; // Has the user has given the password
									// yet?
		// If there is already a client connected to this service, display a
		// message to this client and close the connection. We use a
		// synchronized block to prevent a race condition.
		synchronized (this) {

			if (connected) {

				out.println("ONLY ONE CONTROL CONNECTION ALLOWED AT A TIME.");
				out.close();
				return;

			} else {

				connected = true;
			}
		}

		for (;;) { // infinite loop

			out.print("> "); // Display a prompt
			out.flush(); // Make it appear right away
			
			line = in.readLine(); // Get the user's input

			if (line == null) {

				break; // Quit if we get EOF.

			}

			try {
				
				// Use a StringTokenizer to parse the user's command
				StringTokenizer t = new StringTokenizer(line);

				if (!t.hasMoreTokens()) {

					continue; // if input was blank line

				}

				// Get the first word of the input and convert to lower case
				String command = t.nextToken().toLowerCase();

				// Now compare it to each of the possible commands, doing
				// the
				// appropriate thing for each command

				if (command.equals("password")) { // Password command

					String p = t.nextToken(); // Get the next word of input

					if (p.equals(this.password)) { // Does it equal the password
													
						out.println("OK"); // Say so
						authorized = true; // Grant authorization

					} else {

						out.println("INVALID PASSWORD"); // Otherwise
					}
					// fail
				} else if (command.equals("add")) { // Add Service command

					// Check whether password has been given
					if (!authorized) {

						out.println("PASSWORD REQUIRED");

					} else {

						// Get the name of the service and try to
						// dynamically load
						// and instantiate it. Exceptions will be handled
						// below
						String serviceName = t.nextToken();
						Class<?> serviceClass = Class.forName(serviceName);
						IService service;
						try {
							
							service = (IService) serviceClass.newInstance();

						} catch (NoSuchMethodError e) {

							throw new IllegalArgumentException("Service must have a no-argument constructor");
						}

						int port = Integer.parseInt(t.nextToken());

						// If no exceptions occurred, add the service
						server.addService(service, port);

						out.println("SERVICE ADDED"); // acknowledge

					}

				} else if (command.equals("remove")) { // Remove service
														// command
					if (!authorized) {

						out.println("PASSWORD REQUIRED");

					} else {

						int port = Integer.parseInt(t.nextToken()); // get port
						server.removeService(port); // remove the service on it
						
						out.println("SERVICE REMOVED"); // acknowledge
					}
				} else if (command.equals("max")) { // Set max connection limit
													 
					if (!authorized) {

						out.println("PASSWORD REQUIRED");

					} else {

						int max = Integer.parseInt(t.nextToken()); // get limit
																	 
						server.getConnectionManager().setMaxConnections(max); // set limit
						out.println("MAX CONNECTIONS CHANGED"); // acknowledge
					}

				} else if (command.equals("status")) { // Status Display command
														
					if (!authorized) {
					
						out.println("PASSWORD REQUIRED");
					
					} else {

						// Display a list of all services currently running
						Enumeration<Integer> keys = server.getServices().keys();
						while (keys.hasMoreElements()) {
						
							Integer port = (Integer) keys.nextElement();
							
							Listener listener = (Listener) server.getServices().get(port);
							out.println("SERVICE " + listener.getClass().getName() + " ON PORT " + port);
						
						}

						// Display a list of all current connections
						server.getConnectionManager().printConnections(out);

						// Display the current connection limit
						out.println("MAX CONNECTIONS: " + server.getConnectionManager().getMaxConnections());
					}

				} else if (command.equals("help")) { // Help command

					// Display command syntax. Password not required
					out.println("COMMANDS:\n" + "\tpassword <password>\n" + "\tadd <service> <port>\n" + "\tremove <port>\n" + "\tmax <max-connections>\n" + "\tstatus\n" + "\thelp\n" + "\tquit");

				} else if (command.equals("quit"))

					break; // Quit command. Exit.

				else {

					out.println("UNRECOGNIZED COMMAND"); // Unknown command
				}
				
			} catch (Exception e) {

				// If an exception occurred during the command, print an
				// error
				// message, then output details of the exception.
				out.println("EXCEPTION WHILE PARSING OR EXECUTING COMMAND:");
				out.println(e);
			}
			
		}
		
		// Finally, when the loop command loop ends, close the streams
		// and set our connected flag to false so that other clients can
		// now connect.
		
		out.close();
		in.close();
		connected = false;
		
	}
}
