/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2019 David R. Smith All Rights Reserved 
 */
package digital.toke.tools;

import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.Headers;
import okhttp3.MediaType;

public class Result {

	Headers responseHeaders;
	int code;
	boolean success;
	String data;
	StringBuffer buf;
	public HashMap<String,String> results;
	
	MediaType contentType; // can be null
	
	public Result(Headers responseHeaders, int code, boolean success, String data) {
		super();
		this.responseHeaders = responseHeaders;
		this.code = code;
		this.success = success;
		if(data != null) this.data = data.trim();
		else data = null;
		setContentType(responseHeaders);
		results = new HashMap<String,String>();
	}
	
	private void setContentType(Headers headers) {
		// can be null
		String header = responseHeaders.get("Content-Type");
		if(header != null) contentType = MediaType.parse(header);
	}

	/**
	 * Can be null if there was no such header in the response
	 * 
	 * @return
	 */
	public MediaType getContentType() {
		return contentType;
	}
	
	public void walk() {
		
		buf = new StringBuffer();
		
		if(data == null || data.isEmpty()) return;
		if(data.startsWith("[")) {
			JSONArray array = new JSONArray(data);
			emitToSTDOut(array);
		}else if(data.startsWith("{")) {
			JSONObject obj = new JSONObject(data);
			emitToSTDOut(obj);
		}
	}

	public void emitToSTDOut(Object obj) {

		switch (obj.getClass().getName()) {
			case "org.json.JSONObject": {
				
				JSONObject item = (JSONObject) obj;
				Iterator<String> keys = item.keySet().iterator();
				while(keys.hasNext()) {
					String key = keys.next();
					String dotKey = "."+key;
					buf.append(dotKey);
					emitToSTDOut(item.get(key));
					buf.delete(buf.length()-dotKey.length(),buf.length());
				}
				break;
			}
			case "org.json.JSONArray": {
				
				JSONArray item = (JSONArray) obj;
				Iterator<Object> iter = item.iterator();
				int i = 0;
				while(iter.hasNext()) {
					Object o = iter.next();
					String dotIndex = "."+i;
					buf.append(dotIndex);
					emitToSTDOut(o);
					buf.delete(buf.length()-dotIndex.length(),buf.length());
					i++;
				}
				break;
			}
	
			default: {
				
				String bufPre = buf.toString();
				if(bufPre.startsWith(".")) bufPre = bufPre.substring(1,bufPre.length());
				
				StringBuffer end = new StringBuffer();
				end.append("=");
				end.append('"');
				end.append(String.valueOf(obj));
				end.append('"');
				
				System.out.print(bufPre);
				System.out.println(end.toString());
				results.put(bufPre, String.valueOf(obj));
			}
		}
	}

	@Override
	public String toString() {
		return "Result [responseHeaders=" + responseHeaders + ", code=" + code + ", success=" + success + ", data="
				+ data + ", buf=" + buf + ", results=" + results + ", contentType=" + contentType + "]";
	}
	
	

}
