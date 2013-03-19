package examples.tcpserver.services;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import examples.tcpserver.IService;



/**
 * This is another example service. It reads lines of input from the client, and
 * sends them back, reversed. It also displays a welcome message and
 * instructions, and closes the connection when the user enters a '.' on a line
 * by itself.
 **/
public class ReverseService implements IService {


	public void serve(InputStream i, OutputStream o) throws IOException {

		BufferedReader in = new BufferedReader(new InputStreamReader(i));
		PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(o)));
		out.println("Welcome to the line reversal server.");
		out.println("Enter lines.  End with a '.' on a line by itself");
		for (;;) {
			out.print("> ");
			out.flush();
			String line = in.readLine();
			if ((line == null) || line.equals("."))
				break;
			for (int j = line.length() - 1; j >= 0; j--)
				out.print(line.charAt(j));
			out.println();
		}
		out.close();
		in.close();
	}
}
