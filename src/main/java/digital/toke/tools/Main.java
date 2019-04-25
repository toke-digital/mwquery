/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2019 David R. Smith All Rights Reserved 
 */
package digital.toke.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;

import digital.toke.tools.CmdLineParser.OptionException;
import digital.toke.tools.twitter.OAuthCompute;
import okhttp3.Headers;
import okhttp3.MediaType;

/**
 * Kind of like curl + jq
 * 
 * @author dave
 *
 */
public class Main {
	
	private static String consumerKey, token, consumerSecret, tokenSecret;
	
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
		
		// causes URL-encoding to be done in a way that OAuth likes
		CmdLineParser.Option<Boolean> strictRFC3896Option = parser.addBooleanOption("strictRFC3896");
		
		// default is POST
		CmdLineParser.Option<String> reqOption = parser.addStringOption('r', "request");
		
		// use @file in that case of a file
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
		
		CmdLineParser.Option<String> oauthOption = parser.addStringOption('o', "oauth");
		
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
		
		boolean strictRFC3896 = parser.getOptionValue(strictRFC3896Option, Boolean.FALSE);
		
		String url = parser.getOptionValue(urlOption, null);
		
		// url is required
	   if(url == null) {
			
			if(parser.getRemainingArgs().length>0) {
				url = parser.getRemainingArgs()[0];
			}
			
			if(url == null) {
				return;
			}
			return;
		}
		
		// construct a base url and collect query params if found
		URL urlObj = null;
		try {
			urlObj = new URL(url);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
			return;
		}
		StringBuffer urlbuf = new StringBuffer();
		urlbuf.append(urlObj.getProtocol());
		urlbuf.append("://");
		
		// handle explicit/implicit port value
		int port = urlObj.getPort();
		if(url.contains(String.valueOf(port))) {
			// if the port is explicit in the url string, keep it explicit
			urlbuf.append(urlObj.getAuthority());
		}else {
			urlbuf.append(urlObj.getHost());
		}
		
		urlbuf.append(urlObj.getPath());
		String baseUrl = urlbuf.toString(); // should be protocol://host[:port]/path  but have no params
		
		// parameters found in the url are collected here, as we need this analysis for OAuth
		Collection<String> parameters = new HashSet<String>();
		String queryParams = urlObj.getQuery();
		if(queryParams != null) {
			if(queryParams.contains("&")) {
			    String [] paramItems = queryParams.split("&");
			    for(String p: paramItems) parameters.add(p);
		    }else {
		    	// apparently just one
		    	parameters.add(queryParams);
		    }
		}
		
		final boolean dump = parser.getOptionValue(dumpOption, false);
		final boolean flatten = parser.getOptionValue(flattenOption, false);
		
		// our collection of headers from the command line
		Collection<String> headers = parser.getOptionValues(headerOption);
		
		// parameters from file or json, etc
		String data = parser.getOptionValue(dataOption, null);
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
			
			// so now data is loaded, if we are URLENCODED media type (for POST), assume it is name=value&name=value params. Collect and escape
			// NOTE we are not yet handling multi-part mime encoded params!!!
			
			if(mediaType == Networking.URLENCODED) {
				// collect the params for analysis (for example, OAuth)
				queryParams = data;
				if(queryParams != null) {
					if(queryParams.contains("&")) {
					    String [] paramItems = queryParams.split("&");
					    for(String p: paramItems) parameters.add(p);
				    }else {
				    	// apparently just one
				    	parameters.add(queryParams);
				    }
				}
				
				// finally, do URL encoding
				if(strictRFC3896) {
					data = URLUtil.percentEncode(data);
				}else {
				 // more typical encoding
				  try {
					data = URLEncoder.encode(data, "UTF-8");
				  } catch (UnsupportedEncodingException e) {}
				}
			}
		}
		
		String cookiePath = parser.getOptionValue(cookiePathOption);
		
		Networking net = new Networking();
		if(cookiePath != null) net.setCookiePath(new File(cookiePath));
		Map<String,String> headerMap = new HashMap<String,String>();
		
		// get the headers out of the command line
		Iterator<String> iter = headers.iterator(); 
		while(iter.hasNext()) {
			String item = iter.next();
		    String [] items = item.split("\\:");
		    headerMap.put(items[0].trim(), items[1].trim());
		}
		
		// See if we are doing oauth
		String oauthConfig = parser.getOptionValue(oauthOption, null);
		if(oauthConfig != null) {
			InputStream in = null;
			Properties props = new Properties();

			try {
				File config = new File(oauthConfig);
				if (config.exists()) {
					// external config file
					in = new FileInputStream(config);
				} else {
					throw new RuntimeException("oauth config path does not appear to exist!");
				}

				props.load(in);
			}catch(Exception x) {
				x.printStackTrace();
				return;
			}
				
			// these oauth tokens are now in scope
			consumerKey = props.getProperty("consumer_key",null);
			consumerSecret = props.getProperty("consumer_secret",null);
			token = props.getProperty("token",null);
			tokenSecret = props.getProperty("token_secret",null);
			
			if(consumerKey == null || 
				consumerSecret == null || 
				token == null || 
				tokenSecret == null) throw new RuntimeException("OAuth config failed, at least one required property is not set");
			
			OAuthCompute oac = OAuthCompute.builder(consumerKey, token)
					.addParameters(parameters)
					.consumerSecret(consumerSecret)
					.oauthTokenSecret(tokenSecret)
					.method(req)
					.url(baseUrl)
					.nonce()
					.timestamp()
					.signatureMethod()
					.version()
					.build();
			
			headerMap.put("Authorization", oac.getHeader());
			
		}
	   
		Headers _headers = Headers.of(headerMap);
		
		Result result = null;
		
		try {
			switch(req) {
				case "POST": {
					result = net.post(mediaType, url, _headers, data);
					break;
				}
				
	            case "PUT": {
					result = net.put(mediaType, url, _headers, data);
					break;
				}
				
				case "GET": {
					result = net.get(url, _headers);
					break;
				}
				
	            case "HEAD": {
					
					try {
						result = net.head(url, _headers);
					} catch (IOException e) {
						e.printStackTrace();
					}
					break;
				}
			}
		}catch(IOException x) {
			x.printStackTrace();
			return;
		}
			
		// process Result	
			
			if(dump) {
				System.err.println(result);
				return;
			}
			
			// check for flatten option for json and if requested, output flattened set of data
			if(flatten) {
				result.walk();
			}
			
			// now do json queries or bail if none required
			Collection<String> queries = parser.getOptionValues(queryOption);
			if(queries.size() == 0) {
				return;
			}
			Object document = Configuration.defaultConfiguration().jsonProvider().parse(result.data);
			iter = queries.iterator(); 
			while(iter.hasNext()) {
				String item = iter.next();
			    String [] items = item.split("=");
			    String token = items[0].trim();
			    String query = items[1].trim();
			    String val = JsonPath.read(document, query);
			    System.out.println(String.format("%s=\"%s\"", token,val));
			    
			}
			
	
			
			
	}

	
	private static void help() {
 		
 		System.out.println("mwquery, version 1.0.0");
 		System.out.println("Author: David R. Smith <dave.smith10@det.nsw.edu.au>");
 		System.out.println("");
 		System.out.println("Options:");
 		System.out.println("-r --request <val>         | GET|POST|PUT|HEAD default is GET");
 		System.out.println("-h --header <val>          | header, can be used multiple times, but see --mediaType");
 		System.out.println("-m --mediaType <val>       | Add appropriate header for POST and PUT media type - values are JSON or URLENCODED, default is JSON");
 		System.out.println("--strictRFC3896            | Use with URLENCODED mediaType if required to control the url encoding");
 		System.out.println("-d --data <json> or @file  | data for the rest call");
 		System.out.println("-u --url <url>             | required, the url for the REST call");
 		System.out.println("-q --query <token=query>   | query is a jsonpath expression like 'token=$.token'");
 		System.out.println("--dump                     | dump the response to stdout (useful for debugging)");
 		System.out.println("-f --flatten               | flatten the json response and output it as name=value pairs");
 		System.out.println("-c --cookiePath <path>     | optional path to serialize cookies. If set, client is cookie-aware (for stickyness)");
 		
 		System.out.println("-x --help                  | Show this help");
 		System.out.println("");
 		
	}

}
