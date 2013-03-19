package examples.tcpserver.services;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import examples.tcpserver.IService;



/**
 * This service demonstrates how to maintain state across connections by saving
 * it in instance variables and using synchronized access to those variables. It
 * maintains a count of how many clients have connected and tells each client
 * what number it is
 **/
public class UniqueIDService implements IService {


	public int id = 0;

	public synchronized int nextId() {

		return id++;
	}

	public void serve(InputStream i, OutputStream o) throws IOException {

		PrintWriter out = new PrintWriter(new OutputStreamWriter(o));
		out.println("You are client #: " + nextId());
		out.close();
		i.close();
	}
}
