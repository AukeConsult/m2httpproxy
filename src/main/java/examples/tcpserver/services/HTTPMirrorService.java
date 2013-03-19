package examples.tcpserver.services;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import examples.tcpserver.IService;



/**
 * This service is an HTTP mirror, just like the HttpMirror class implemented
 * earlier in this chapter. It echos back the client's HTTP request
 **/
public class HTTPMirrorService implements IService {


	public void serve(InputStream i, OutputStream o) throws IOException {

		BufferedReader in = new BufferedReader(new InputStreamReader(i));
		PrintWriter out = new PrintWriter(new OutputStreamWriter(o));
		out.println("HTTP/1.0 200 ");
		out.println("Content-Type: text/plain");
		out.println();
		String line;
		while ((line = in.readLine()) != null) {
			if (line.length() == 0)
				break;
			out.println(line);
		}
		out.close();
		in.close();
	}
}
