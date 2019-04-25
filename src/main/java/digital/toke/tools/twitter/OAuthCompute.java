/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2019 David R. Smith All Rights Reserved 
 */
package digital.toke.tools.twitter;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import digital.toke.tools.URLUtil;

/**
 * Simple class to assist generating tweets (handles OAuth functions - assumes
 * you have a developer account, consumer api key, token, etc)
 * 
 * @author daves
 *
 */
public class OAuthCompute {

	private static final SecureRandom rand = new SecureRandom();

	String header;
	String body;

	// set if debug was true in the builder
	String parameterString;
	String signatureBaseString;
	String signingKey;
	String signature;

	public OAuthCompute(String header,String body) {
		this.header = header;
		this.body = body;
	}
	
	// used with debug
	OAuthCompute(String header, String body, String parameterString, String signatureBaseString, String signingKey, String signature) {
		this.header = header;
		this.body = body;
		this.parameterString = parameterString;
		this.signatureBaseString = signatureBaseString;
		this.signingKey = signingKey;
		this.signature = signature;
	}

	public String getHeader() {
		return header;
	}

	public String getParameterString() {
		return parameterString;
	}

	public String getSignatureBaseString() {
		return signatureBaseString;
	}

	public String getSigningKey() {
		return signingKey;
	}
	public String getSignature() {
		return signature;
	}

	public String getBody() {
		return body;
	}

	public static Builder builder(String consumerKey, String token) {
		return new Builder(consumerKey, token);
	}

	public static class Builder {

		String oauthConsumerKey, oauthNonce, oauthSignature, oauthSignatureMethod, oauthTimestamp, oauthToken,
				oauthVersion;

		String method;
		String url;

		String consumerSecret;
		String oauthTokenSecret;

		Map<String, String> collector;

		boolean debug = false;
		String parameterString;
		String signatureBaseString;
		String signingKey;
		String header;
		String body;

		public Builder(String consumerKey, String token) {
			oauthConsumerKey = consumerKey;
			oauthToken = token;
			collector = new TreeMap<String, String>();
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

		public Builder debug() {
			this.debug = true;
			return this;
		}

		public Builder version(String version) {
			this.oauthVersion = version;
			return this;
		}

		public Builder addParameter(String paramName, String paramValue) {
			collect(paramName, paramValue);
			return this;
		}
		
		public Builder addParameters(Collection<String> params) {
			params.forEach(item-> {
			  int index = item.indexOf("=");
			  if(index == -1) throw new RuntimeException("malformed parameter, must be formatted as name=value");
			   String paramName = item.substring(0,index);
			   String paramValue = item.substring(index+1,item.length());
			   collect(paramName,paramValue);
			});
			
			return this;
		}

		private void collect(String name, String value) {
			collector.put(percentEncode(name), percentEncode(value));
			if(name.equals("status")) {
				body = "status="+collector.get("status");
			}
		}

		public OAuthCompute build() {

			// 1.0 addParameter will have added and percent encoded stuff like the status

			// 1.1 add and percent encode the main items
			collect("oauth_consumer_key", this.oauthConsumerKey);
			collect("oauth_nonce", this.oauthNonce);
			collect("oauth_consumer_key", this.oauthConsumerKey);
			collect("oauth_signature_method", this.oauthSignatureMethod);
			collect("oauth_timestamp", this.oauthTimestamp);
			collect("oauth_token", this.oauthToken);
			collect("oauth_version", this.oauthVersion);

			StringBuffer buf = new StringBuffer();

			// iterator need to be be sorted lexigraphically
			int size = collector.size();
			int i = 0;

			// 1.2 append items
			Iterator<String> iter = collector.keySet().iterator();
			while (iter.hasNext()) {
				String key = iter.next();
				String value = collector.get(key);
				buf.append(key);
				buf.append("=");
				buf.append(value);
				if (i < size - 1)
					buf.append("&");
				i++;
			}

			parameterString = buf.toString();

			buf = new StringBuffer();

			// 1.3 append method
			buf.append(method.toUpperCase());
			buf.append("&");

			// 1.4 append url
			buf.append(percentEncode(url));
			buf.append("&");

			buf.append(percentEncode(parameterString));

			// 1.5 our base string
			signatureBaseString = buf.toString();

			buf = new StringBuffer();
			buf.append(percentEncode(this.consumerSecret));
			buf.append("&");
			buf.append(percentEncode(this.oauthTokenSecret));

			// 1.6 our signing key
			signingKey = buf.toString();

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

			header = buf.toString();
			
			
			if(debug) return new OAuthCompute(header, body, parameterString, signatureBaseString, signingKey, oauthSignature);
			return new OAuthCompute(header, body);

		}

	    private static String percentEncode(String value) {
		        return URLUtil.percentEncode(value);
	    }

		private String calculateNonce() {
			byte[] bytes = new byte[32];
			rand.nextBytes(bytes);
			return Base64.getEncoder().encodeToString(bytes);
		}

		private String now() {
			long ut1 = Instant.now().getEpochSecond();
			return String.valueOf(ut1);
		}
	}

}
