/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2019 David R. Smith All Rights Reserved 
 */
package digital.toke.tools.twitter;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Simple class to assist generating tweets (handles OAuth functions - assumes you have a developer account, consumer api key, token, etc)
 * 
 * @author daves
 *
 */
public class TwitterOAuthHeader {
	
	private static final SecureRandom rand = new SecureRandom();
	
	String header;
	 Map<String,String> collector;
	
	public TwitterOAuthHeader(String header, Map<String,String> collector) {
		this.header = header;
		this.collector = collector;
	}
	
	public static Builder builder(String consumerKey, String token) {
		return new Builder(consumerKey,token);
	}
	
	public static class Builder {
		
		String oauthConsumerKey, oauthNonce, oauthSignature, oauthSignatureMethod, oauthTimestamp, oauthToken, oauthVersion;
		
		String method;
		String url;
		
		String consumerSecret;
		String oauthTokenSecret;
		
		Map<String,String> collector;
		
		public Builder(String consumerKey,String token) {
			oauthConsumerKey = consumerKey;
			oauthToken = token;
			collector = new TreeMap<String,String>();
		}
		
		public Builder consumerSecret(String consumerSecret) {
			this.consumerSecret = consumerSecret;
			return this;
		}
		
		public Builder oauthTokenSecret(String oauthTokenSecret) {
			this.oauthTokenSecret = oauthTokenSecret;
			return this;
		}
		
		public Builder method(String method) {
			this.method = method;
			return this;
		}
		
		public Builder url(String url) {
			this.url = url;
			return this;
		}
		
		public Builder consumerKey(String consumerKey) {
			this.oauthConsumerKey = consumerKey;
			return this;
		}
		
		public Builder nonce(String nonce) {
			this.oauthNonce = nonce;
			return this;
		}
		
		public Builder nonce() {
			this.oauthNonce = calculateNonce();
			return this;
		}
		
		public Builder signatureMethod(String sigMethod) {
			this.oauthSignatureMethod = sigMethod;
			return this;
		}
		
		public Builder signatureMethod() {
			this.oauthSignatureMethod = "HMAC-SHA1";
			return this;
		}
		
		public Builder timestamp(String timestamp) {
			this.oauthTimestamp = timestamp;
			return this;
		}
		
		public Builder timestamp() {
			this.oauthTimestamp = now();
			return this;
		}
	
		public Builder token(String token) {
			this.oauthToken = token;
			return this;
		}
		
		public Builder version() {
			this.oauthVersion = "1.0";
			return this;
		}
		
		public Builder version(String version) {
			this.oauthVersion = version;
			return this;
		}
		
		public Builder addParameter(String paramName, String paramValue) {
			collect(paramName,paramValue);
			return this;
		}
		
		private void collect(String name, String value) {
			collector.put(percentEncode(name), percentEncode(name));
		}
		
		public TwitterOAuthHeader build() {
			
			// 1.0 addParameter will have added and percent encoded stuff like the status
			
			// 1.1 add and percent encode the main items
			collect("oauth_consumer_key", this.oauthConsumerKey);
			collect("oauth_nonce", this.oauthNonce);
			collect("oauth_consumer_key", this.oauthConsumerKey);
			collect("oauth_signature_method", this.oauthConsumerKey);
			collect("oauth_timestamp", this.oauthTimestamp);
			collect("oauth_token", this.oauthToken);
			collect("oauth_version", this.oauthVersion);
			
			StringBuffer buf = new StringBuffer();
			
			// iterator need to be be sorted lexigraphically
			int size = collector.size();
			int i = 0;
			
			// 1.2 append items
			Iterator<String> iter = collector.keySet().iterator();
			while(iter.hasNext()) {
				String key = iter.next();
				String value = collector.get(key);
				buf.append(key);
				buf.append("=");
				buf.append(value);
				if(i < size-1) buf.append("&");
				i++;
			}
			
			String parameterString = buf.toString();
			
			buf = new StringBuffer();
			
			// 1.3 append method
			buf.append(method.toUpperCase());
			buf.append("&");
			
			// 1.4 append url
			buf.append(percentEncode(url));
			buf.append("&");
			
			buf.append(percentEncode(parameterString));
			
			// 1.5 our base string
			String signatureBaseString = buf.toString();
			
			buf = new StringBuffer();
			buf.append(percentEncode(this.consumerSecret));
			buf.append("&");
			buf.append(percentEncode(this.oauthTokenSecret));
			
			// 1.6 our signing key
			String signingKey = buf.toString();
			
			// 1.7 our signature
			oauthSignature = HmacSHA1.calculateRFC2104HMAC(signatureBaseString, signingKey);
			
			buf = new StringBuffer();
			
			buf.append("OAuth "); 
			
			buf.append("oauth_consumer_key=");
			buf.append("\"");
			buf.append(percentEncode(this.oauthConsumerKey));
			buf.append("\"");
			buf.append(", ");
			
			buf.append("oauth_nonce=");
			buf.append("\"");
			buf.append(percentEncode(this.oauthNonce));
			buf.append("\"");
			buf.append(", ");
			
			buf.append("oauth_signature=");
			buf.append("\"");
			buf.append(percentEncode(this.oauthSignature));
			buf.append("\"");
			buf.append(", ");
			
			buf.append("oauth_signature_method=");
			buf.append("\"");
			buf.append(percentEncode(this.oauthSignatureMethod));
			buf.append("\"");
			buf.append(", ");
			
			buf.append("oauth_timestamp=");
			buf.append("\"");
			buf.append(percentEncode(this.oauthTimestamp));
			buf.append("\"");
			buf.append(", ");
			
			buf.append("oauth_token=");
			buf.append("\"");
			buf.append(percentEncode(this.oauthToken));
			buf.append("\"");
			buf.append(", ");
			
			buf.append("oauth_version=");
			buf.append("\"");
			buf.append(percentEncode(this.oauthVersion));
			buf.append("\"");
			
			
			return new TwitterOAuthHeader(buf.toString(), collector);
			
		}
		
		private String percentEncode(String s) {
		    if (s == null) {
		        return "";
		    }
		    try {
		        return URLEncoder.encode(s, "UTF-8")
		                // OAuth encodes some characters differently:
		                .replace("+", "%20").replace("*", "%2A").replace("%7E", "~");
		    } catch (UnsupportedEncodingException wow) {
		        throw new RuntimeException(wow.getMessage(), wow);
		    }
		}
		
		private String calculateNonce() {
			byte [] bytes = new byte[32];
			rand.nextBytes(bytes);
			return Base64.getEncoder().encodeToString(bytes);
		}
		
		private String now() {
			long ut1 = Instant.now().getEpochSecond();
			return String.valueOf(ut1);
		}
	}
	
	

}
