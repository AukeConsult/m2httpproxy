package no.auke.m2.proxy.request;

import org.apache.http.HttpVersion;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.conn.params.ConnPerRoute;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

public class HttpClientFactory {

    private static DefaultHttpClient client;

    public synchronized static DefaultHttpClient getThreadSafeClient() {
  
        if (client != null) {

        	return client;
        	
        }
         
        HttpParams params = new BasicHttpParams();
        params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
        params.setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, HTTP.UTF_8);

        params.setParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false);
        params.setParameter(CoreConnectionPNames.SO_TIMEOUT, 0);
        params.setParameter(CoreConnectionPNames.TCP_NODELAY, true);
        
        params.setParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, 10);
        params.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE, new ConnPerRoute() {
          public int getMaxForRoute(HttpRoute route) {
            return 10;
          }}); 
        
    
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
        
        client =   new DefaultHttpClient(new ThreadSafeClientConnManager(params, schemeRegistry), params);
        HttpRequestRetryHandler  myretryhandlers = new DefaultHttpRequestRetryHandler(3, true);
        client.setHttpRequestRetryHandler(myretryhandlers); 
        
        return client;
    }
    
    public static void ShutDown()
    {
    	  if (client != null) client.getConnectionManager().shutdown();
    }
}