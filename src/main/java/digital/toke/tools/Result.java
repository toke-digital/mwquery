/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2019 David R. Smith All Rights Reserved 
 */
package digital.toke.tools;

import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;

public class Result {

	int code;
	boolean success;
	String data;
	StringBuffer buf;
	
	public Result(int code, boolean success, String data) {
		super();
		this.code = code;
		this.success = success;
		if(data != null) this.data = data.trim();
		else data = null;
	}

	public String toString() {
		if (data == null)
			return "";
		JSONObject obj = new JSONObject(data);
		return obj.toString(4);
	}
	
	public void walk() {
		
		buf = new StringBuffer();
		
		if(data == null || data.isEmpty()) return;
		if(data.startsWith("[")) {
			JSONArray array = new JSONArray(data);
			walk(array);
		}else if(data.startsWith("{")) {
			JSONObject obj = new JSONObject(data);
			walk(obj);
		}
	}

	public void walk(Object obj) {

		switch (obj.getClass().getName()) {
			case "org.json.JSONObject": {
				
				JSONObject item = (JSONObject) obj;
				Iterator<String> keys = item.keySet().iterator();
				while(keys.hasNext()) {
					String key = keys.next();
					String dotKey = "."+key;
					buf.append(dotKey);
					walk(item.get(key));
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
					walk(o);
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
				
				
			}
		}
	}

}
