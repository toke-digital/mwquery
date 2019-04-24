/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2019 David R. Smith All Rights Reserved 
 */
package digital.toke.tools;


import java.io.File;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;

import digital.toke.tools.CmdLineParser.OptionException;
import okhttp3.Headers;
import okhttp3.MediaType;

import digital.toke.tools.Networking.*;

public class Main {
	
	public static void main(String[] args) {
		
		 if(args.length == 0){
	 			help();
	 			System.exit(1);
	     }
		 
		CmdLineParser parser = new CmdLineParser();
		CmdLineParser.Option<Boolean> helpOption = parser.addBooleanOption('x', "help");
		
		// can be used multiple times
		CmdLineParser.Option<String> headerOption = parser.addStringOption('h', "header");
		
		CmdLineParser.Option<String> mediaTypeOption = parser.addStringOption('m', "mediaType");
		
		// default is POST
		CmdLineParser.Option<String> reqOption = parser.addStringOption('r', "request");
		
		// can be a json formatted string a file. use @file in that case
		CmdLineParser.Option<String> dataOption = parser.addStringOption('d', "data");
		
		CmdLineParser.Option<String> urlOption = parser.addStringOption('u', "url");
		
		// can be used multiple times, these are the queries to extract results from out of the response
		CmdLineParser.Option<String> queryOption = parser.addStringOption('q', "query");
		
		CmdLineParser.Option<Boolean> dumpOption = parser.addBooleanOption("dump");
		
		CmdLineParser.Option<Boolean> flattenOption = parser.addBooleanOption('f', "flatten");
		
		/**
		 * If set, attempt will be made to serialize cookies, allows for sticky sessions
		 */
		CmdLineParser.Option<String> cookiePathOption = parser.addStringOption('c', "cookiePath");
		
		try {
			parser.parse(args);
		} catch (OptionException e) {
			e.printStackTrace();
			return;
		}
		
		if(parser.getOptionValue(helpOption, Boolean.FALSE)){
			help();
			return;
		}
		
		final String req = parser.getOptionValue(reqOption, "GET");
		final String mediaTypeString = parser.getOptionValue(mediaTypeOption, "JSON");
		MediaType mediaType = null;
		switch(mediaTypeString) {
			case "JSON" : mediaType = Networking.JSON; break;
			case "URLENCODED" : mediaType = Networking.URLENCODED; break;
		}
		
		String url = parser.getOptionValue(urlOption, null);
		
		if(url == null) {
			
			if(parser.getRemainingArgs().length>0) {
				url = parser.getRemainingArgs()[0];
			}
			
			if(url == null) {
				return;
			}
			return;
		}
		
		final boolean dump = parser.getOptionValue(dumpOption, false);
		final boolean flatten = parser.getOptionValue(flattenOption, false);
		
		Collection<String> headers = parser.getOptionValues(headerOption);
		String data = parser.getOptionValue(dataOption);
		if(data != null) {
			if(data.startsWith("@")) {
				File f = new File(data.substring(1));
				if(!f.exists()) {
					try {
						throw new RuntimeException("data file does not exist: "+f.getCanonicalPath());
					}catch(IOException x) {}
				}
				
				try {
					data = new String(Files.readAllBytes(f.toPath()), "UTF-8");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		String cookiePath = parser.getOptionValue(cookiePathOption);
		
		Networking net = new Networking();
		if(cookiePath != null) net.setCookiePath(new File(cookiePath));
		Map<String,String> map = new HashMap<String,String>();
		Iterator<String> iter = headers.iterator(); 
		while(iter.hasNext()) {
			String item = iter.next();
		    String [] items = item.split("\\:");
		    map.put(items[0].trim(), items[1].trim());
		}
	   
		Headers _headers = Headers.of(map);
		
		switch(req) {
			case "POST": {
				
				try {
					Result r = net.post(mediaType, url, _headers, data);
					if(dump) {
						System.err.println(r);
						return;
					}
					
					// check for flatten option and if requested, output flattened set of data
					if(flatten) {
						r.walk();
					}
					
					// now do queries or bail if none
					Collection<String> queries = parser.getOptionValues(queryOption);
					if(queries.size() ==0) {
						return;
					}
					Object document = Configuration.defaultConfiguration().jsonProvider().parse(r.data);
					iter = queries.iterator(); 
					while(iter.hasNext()) {
						String item = iter.next();
					    String [] items = item.split("=");
					    String token = items[0].trim();
					    String query = items[1].trim();
					    String val = JsonPath.read(document, query);
					    System.out.println(String.format("%s=\"%s\"", token,val));
					    
					}
					
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				break;
			}
			
            case "PUT": {
				
				try {
					Result r = net.put(mediaType, url, _headers, data);
					if(dump) {
						System.err.println(r);
						return;
					}
					
					// check for flatten option and if requested, output flattened set of data
					if(flatten) {
						r.walk();
					}
					
					// now do queries or bail if none
					Collection<String> queries = parser.getOptionValues(queryOption);
					if(queries.size() ==0) {
						return;
					}
					Object document = Configuration.defaultConfiguration().jsonProvider().parse(r.data);
					iter = queries.iterator(); 
					while(iter.hasNext()) {
						String item = iter.next();
					    String [] items = item.split("=");
					    String token = items[0].trim();
					    String query = items[1].trim();
					    String val = JsonPath.read(document, query);
					    System.out.println(String.format("%s=\"%s\"", token,val));
					    
					}
					
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				break;
			}
			
			case "GET": {
				
				try {
					Result r = net.get(url, _headers);
					if(dump) {
						System.err.println(r);
						return;
					}
					
					// check for flatten option and if requested, output flattened set of data
					if(flatten) {
						r.walk();
					}
					
					
					Collection<String> queries = parser.getOptionValues(queryOption);
					if(queries.size() ==0) {
						return;
					}
					Object document = Configuration.defaultConfiguration().jsonProvider().parse(r.data);
					iter = queries.iterator(); 
					while(iter.hasNext()) {
						String item = iter.next();
					    String [] items = item.split("=");
					    String token = items[0].trim();
					    String query = items[1].trim();
					    String val = JsonPath.read(document, query);
					    System.out.println(String.format("%s=%s", token,val));
					    
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			}
			
            case "HEAD": {
				
				try {
					Result r = net.head(url, _headers);
					// dumps the headers
					System.out.println(r.data);
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			}

		}
	}

	
	private static void help() {
 		
 		System.out.println("mwquery, version 1.0.0");
 		System.out.println("Author: David R. Smith <dave.smith10@det.nsw.edu.au>");
 		System.out.println("");
 		System.out.println("Options:");
 		System.out.println("-r --request <val>         | GET|POST|PUT default is GET");
 		System.out.println("-h --header <val>          | header, can be used multiple times, but see --mediaType");
 		System.out.println("-m --mediaType <val>       | Add appropriate header for post and put - values are JSON or URLENCODED, default is JSON");
 		System.out.println("-d --data <json> or @file  | data for the rest call");
 		System.out.println("-u --url <url>             | required, the url for the REST call");
 		System.out.println("-q --query <token=query>   | query is a jsonpath expression like 'token=$.token'");
 		System.out.println("--dump                     | dump the response to stderr");
 		System.out.println("-f --flatten               | flatten the json response and output it");
 		System.out.println("-c --cookiePath <path>     | optional path to serialize cookies. If set, client is cookie-aware (for stickyness)");
 		
	
 	
 		System.out.println("-x --help                  | Show this help");
 		System.out.println("");
 		
	}

}
