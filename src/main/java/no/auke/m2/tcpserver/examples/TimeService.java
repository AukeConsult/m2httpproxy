package no.auke.m2.tcpserver.examples;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Date;

import no.auke.m2.tcpserver.IService;




/**
 * A very simple service. It displays the current time on the server to the
 * client, and closes the connection.
 **/

public class TimeService implements IService {

	public void serve(InputStream i, OutputStream o) throws IOException {

		PrintWriter out = new PrintWriter(new OutputStreamWriter(o));
		out.println(new Date());
		out.close();
		i.close();
	
	}
	
}