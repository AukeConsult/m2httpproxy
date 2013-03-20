/*
 * This file is part of m2 http proxy project 
 * 
 * Copyright (c) 2011-2013 Auke Team / Leif Auke <leif@auke.no> / Huy Do <huydo@auke.no>
 * 
 * License: Attribution-NonCommercial-ShareAlike CC BY-NC-SA 
 * 
 */

package no.auke.m2.proxy;

import java.io.IOException;
import java.util.Properties;

import no.auke.p2p.m2.license.ILicenseRegisterHandler;
import no.auke.util.FileUtil;
import no.auke.util.ListNetworks;

public class ServerParams {	

	public final static String APPID="m2proxy";
	public final static String NETSPACEID = "m2proxynetspace";
	public final static int HTTP_SERVICE_PORT = 10;
	public final static int NEIGTBOR_SERVICE_PORT = 11;
	
	public static int PROXY_PORT = 8432;
	public static int M2_PORT = 8431;

	public static int CHECK_FREQUENCY = 15000;	
	
	public static boolean USE_REMOTE = true;

	public static String ROOTDIR = "";
	
	public static String DEVICEID=ListNetworks.getMacAddress();

	public static String BOOTADDRESS=""; 
	public static String USERDIR="";

	public static int DEBUG=2;
	public static int ENCRYPTION=0;
		
	public static String USERID="";
	
	public static boolean USEMIDDLEMAN = false;
	public static boolean SILENT = false;
	
    public static void setArgs(String[] args) {
    	
    	
    	// Linux / windows
    	if(System.getProperty("os.name").toLowerCase().startsWith("win")) {
    		
    		ROOTDIR = System.getenv("LOCALAPPDATA");
    		
    	} else {
    		
    		ROOTDIR = System.getProperty("user.dir");
    		
    	}
    	
    	try {
		
    		Properties properties = FileUtil.readPropertiesFromFile(ServerParams.class, "NORMAL.properties");

    		BOOTADDRESS = properties.getProperty("bootAddress") == null ? BOOTADDRESS
                    : properties.getProperty("bootAddress").trim();

    		ENCRYPTION = properties.getProperty("encryption") == null ? ENCRYPTION
                    : Integer.parseInt(properties.getProperty("encryption"));
    		
    		M2_PORT = properties.getProperty("m2Port") == null ? M2_PORT
                    : Integer.parseInt(properties.getProperty("m2Port"));

    		PROXY_PORT = properties.getProperty("proxyPort") == null ? PROXY_PORT
                    : Integer.parseInt(properties.getProperty("proxyPort"));
    		
    		USEMIDDLEMAN = properties.getProperty("useMiddelman") == null ? USEMIDDLEMAN
                    : Boolean.parseBoolean(properties.getProperty("useMiddelman"));
    	
    	} catch (IOException e) {

		}

    	// LHA, important :-)
    	for(int i=0;i<args.length;i++)
    	{
    		args[i] = args[i].trim(); 
    	}

        int pos = 0;
        while (args != null && pos < args.length) {

            if (args[pos].startsWith("-")) {
            	
            	  if (args[pos].toLowerCase().equals("-userid") && args.length > pos + 1) {
            		
            		  USERID = args[pos + 1];
            	  
            	  } else if (args[pos].toLowerCase().equals("-deviceid") && args.length > pos + 1) {
                  		
            		  DEVICEID = args[pos + 1];

            	  } else if (args[pos].toLowerCase().equals("-root") && args.length > pos + 1) {
            		  
            		  ROOTDIR = args[pos + 1];
            		  
            	  } else if (args[pos].toLowerCase().equals("-home") && args.length > pos + 1) {
            		  
            		  USERDIR=ROOTDIR + "/" + APPID + "/" + args[pos + 1];
            		  
            	  } else if (args[pos].toLowerCase().equals("-bootaddress") && args.length > pos + 1) {
            		  
            		  BOOTADDRESS = args[pos + 1];            		  

                  } else if (args[pos].toLowerCase().equals("-m2Port") && args.length > pos + 1) {
                	  
                	  M2_PORT = Integer.valueOf(args[pos + 1]);

                  } else if (args[pos].toLowerCase().equals("-proxyPort") && args.length > pos + 1) {
                	  
                	  PROXY_PORT = Integer.valueOf(args[pos + 1]);
            		  
                  } else if (args[pos].toLowerCase().equals("-debug") && args.length > pos + 1) {
                	  
                	  DEBUG = Integer.valueOf(args[pos + 1]);
                  
                  } else if (args[pos].toLowerCase().equals("-encryption") && args.length > pos + 1) {
                	
                	  ENCRYPTION = Integer.valueOf(args[pos + 1]);

//                } else if (args[pos].toLowerCase().equals("-portscan") && args.length > pos + 1) {
//                    portscan = args[pos + 1].equals("0") || args[pos + 1].toLowerCase().equals("false") ? false : true;
//                } else if (args[pos].toLowerCase().equals("-sendtimeout") && args.length > pos + 1) {
//                    setSendTimeout(Integer.valueOf(args[pos + 1]));
            	 
                  } else if (args[pos].toLowerCase().equals("-usemiddleman") && args.length > pos) {
            		  
             		  USEMIDDLEMAN = true;            		  

                  } else if (args[pos].toLowerCase().equals("-silent") && args.length > pos) {
            		  
                	  SILENT = true;
             		  
                  }
                
            }
            pos++;
        }

        if(USERDIR.isEmpty())
        {
        	USERDIR=ROOTDIR + "/" + APPID + "/home";
        }
		
        FileUtil.createDirectory(ROOTDIR);
		FileUtil.createDirectory(USERDIR);


    }

	public static ILicenseRegisterHandler getLicenseRegistrationHandler() {

		// TODO Auto-generated method stub
		return null;
	}
	
}
