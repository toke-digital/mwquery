/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2019 David R. Smith All Rights Reserved 
 */
package digital.toke.tools;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Our networking functions. Cookies will be serialized and re-applied if cookiePath is non-null. 
 * 
 * @author David R. Smith 
 *
 */
public class Networking {

	public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
	protected final Lock lock = new ReentrantLock();
	protected final OkHttpClient client;
	protected MWQCookieJar cookieJar;
	protected File cookiePath;

	public Networking() {
		cookieJar = new MWQCookieJar();
		client = new OkHttpClient().newBuilder().cookieJar(cookieJar).build();
	}
	
	public boolean pingHost(String host, int port, int timeout) {
	    try (Socket socket = new Socket()) {
	        socket.connect(new InetSocketAddress(host, port), timeout);
	        return true;
	    } catch (IOException e) {
	        return false; // Either timeout or unreachable or failed DNS lookup.
	    }
	}
	
	public boolean checkIsReachable(String hostname) {
		try {
			return InetAddress.getByName(hostname).isReachable(200);
		} catch (Exception x) {
			
		}
		return false;
	}
	
	public Result get(String url, Headers headers) throws IOException {
		lock.lock();
		
		if(cookiePath != null) {
			// attempt to load cookies
			if(cookiePath.exists()) {
				cookieJar.readCookies(cookiePath);
			}
		}
		
		try {
			
			Request request = null;
			
			if(headers == null || headers.size() == 0) {
				request = new Request.Builder()
						.url(url)
						.build();
			}else {
				request = new Request.Builder()
						.url(url)
						.headers(headers)
						.build();
			}
			
			int code; boolean success; String result;
			try (Response response = client.newCall(request).execute()){
				 result = response.body().string();
				 code = response.code();
				 success = response.isSuccessful();
			}
			
			if(cookiePath != null) {
				cookieJar.writeCookies(cookiePath);
			}
			
			return new Result(code, success, result);
			
		} finally {
			lock.unlock();
		}
	}
	
	public Result head(String url, Headers headers) throws IOException {
		lock.lock();
		
		if(cookiePath != null) {
			// attempt to load cookies
			if(cookiePath.exists()) {
				cookieJar.readCookies(cookiePath);
			}
		}
		
		try {
			
			Request request = null;
			
			if(headers == null || headers.size() == 0) {
				request = new Request.Builder()
						.head()
						.url(url)
						.build();
			}else {
				request = new Request.Builder()
						.head()
						.url(url)
						.headers(headers)
						.build();
			}
			
			int code; boolean success;
			StringBuffer buf = new StringBuffer();
			try (Response response = client.newCall(request).execute()){
				
				 code = response.code();
				 success = response.isSuccessful();
				
				Headers responseHeaders = response.headers();
				Iterator<String> iter = responseHeaders.names().iterator();
				while(iter.hasNext()) {
					String name = iter.next();
					String value = response.header(name);
					buf.append(name);
					buf.append("=");
					buf.append("\"");
					buf.append(value);
					buf.append("\"");
					buf.append("\n");
				}
			
			}
			
			if(cookiePath != null) {
				cookieJar.writeCookies(cookiePath);
			}
			
			return new Result(code, success, buf.toString());
			
		} finally {
			lock.unlock();
		}
	}
	
	
	public Result post(String url, Headers headers, String json) throws IOException {
		lock.lock();
		
		if(cookiePath != null) {
			// attempt to load cookies
			if(cookiePath.exists()) {
				cookieJar.readCookies(cookiePath);
			}
		}
		
		try {
			RequestBody body = RequestBody.create(JSON, json);
			Request request = new Request.Builder()
					.url(url)
					.post(body)
					.headers(headers)
					.build();
			try (Response response = client.newCall(request).execute()) {
				if(cookiePath != null) {
					cookieJar.writeCookies(cookiePath);
				}
				return new Result(response.code(), response.isSuccessful(), response.body().string());
			}
		} finally {
			lock.unlock();
		}
	}
	
	public Result put(String url, Headers headers, String json) throws IOException {
		lock.lock();
		
		if(cookiePath != null) {
			// attempt to load cookies
			if(cookiePath.exists()) {
				cookieJar.readCookies(cookiePath);
			}
		}
		
		try {
			RequestBody body = RequestBody.create(JSON, json);
			Request request = new Request.Builder()
					.url(url)
					.put(body)
					.headers(headers)
					.build();
			try (Response response = client.newCall(request).execute()) {
				if(cookiePath != null) {
					cookieJar.writeCookies(cookiePath);
				}
				return new Result(response.code(), response.isSuccessful(), response.body().string());
			}
		} finally {
			lock.unlock();
		}
	}
	

	public File getCookiePath() {
		return cookiePath;
	}

	public void setCookiePath(File cookiePath) {
		this.cookiePath = cookiePath;
	}

}

