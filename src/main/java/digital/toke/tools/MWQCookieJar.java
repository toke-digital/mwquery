/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2019 David R. Smith All Rights Reserved 
 */
package digital.toke.tools;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

/**
 * Implement a cookie jar that can serialize its contents to a file
 * 
 * @author David R. Smith
 *
 */
public class MWQCookieJar implements CookieJar {
	
    private final HashMap<String, List<Cookie>> cookieStore = new HashMap<>();

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        cookieStore.put(url.host(), cookies);
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        List<Cookie> cookies = cookieStore.get(url.host());
        return cookies != null ? cookies : new ArrayList<Cookie>();
    }

	public HashMap<String, List<Cookie>> getCookieStore() {
		return cookieStore;
	}
    
	public void writeCookies(File file) {
		Iterator<String> iter = cookieStore.keySet().iterator();
		JSONObject root = new JSONObject();
		while(iter.hasNext()) {
			String host = iter.next();
			JSONArray cookieList = new JSONArray();
			List<Cookie> list = cookieStore.get(host);
			for(Cookie c: list) {
				JSONObject cookie = new JSONObject();
				cookie.put("name", c.name());
				cookie.put("value", c.value());
				cookie.put("expiresAt", c.expiresAt());
				cookie.put("domain", c.domain());
				cookie.put("path", c.path());
				cookie.put("secure", c.secure());
				cookie.put("httpOnly", c.httpOnly());
				cookie.put("hostOnly", c.hostOnly());
				cookie.put("persistent", c.persistent());
				cookieList.put(cookie);
			}
			root.put(host, cookieList);
		}
		
		String json = root.toString(2);
		try {
			Files.write(file.toPath(), json.getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void readCookies(File file) {
		try {
			String json = new String(Files.readAllBytes(file.toPath()),"UTF-8");
			JSONObject root = new JSONObject(json);
			Iterator<String> iter = root.keys();
			while(iter.hasNext()) {
				String host = iter.next();
				JSONArray cookieList = root.getJSONArray(host);
				Iterator<Object> cookieIter = cookieList.iterator();
				while(cookieIter.hasNext()) {
					JSONObject cookieRep = (JSONObject) cookieIter.next();
					Cookie.Builder b = new Cookie.Builder();
						b.name(cookieRep.getString("name"))
							.value(cookieRep.getString("value"))
							.expiresAt(cookieRep.getLong("expiresAt"))
							.hostOnlyDomain(cookieRep.getString("domain"))
							.path(cookieRep.getString("path"));
						if(cookieRep.getBoolean("secure")) b.secure();
						if(cookieRep.getBoolean("secure")) b.secure();
						if(cookieRep.getBoolean("secure")) b.secure();
						if(cookieRep.getBoolean("secure")) b.secure();
						Cookie c = b.build();
						if(cookieStore.containsKey(host)) {
							// assume we have a list, just add our cookie
							List<Cookie> list = cookieStore.get(host);
							if(!list.contains(c)) {
								list.add(c);
							}
						}else {
							cookieStore.put(host, new ArrayList<Cookie>());
							cookieStore.get(host).add(c);
						}
				}
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
    
};