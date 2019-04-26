package digital.toke.tools;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashSet;

public class URLUtil {

	// from
	// https://github.com/Twitter4J/Twitter4J/blob/master/twitter4j-core/src/internal-http/java/twitter4j/HttpParameter.java
	// this is implementing a strict interpretation RFC 3896 which works with OAuth
	public static String percentEncode(String value) {
		String encoded = null;
		try {
			encoded = URLEncoder.encode(value, "UTF-8");
		} catch (UnsupportedEncodingException ignore) {
		}
		StringBuilder buf = new StringBuilder(encoded.length());
		char focus;
		for (int i = 0; i < encoded.length(); i++) {
			focus = encoded.charAt(i);
			if (focus == '*') {
				buf.append("%2A");
			} else if (focus == '+') {
				buf.append("%20");
			} else if (focus == '%' && (i + 1) < encoded.length() && encoded.charAt(i + 1) == '7'
					&& encoded.charAt(i + 2) == 'E') {
				buf.append('~');
				i += 2;
			} else {
				buf.append(focus);
			}
		}
		return buf.toString();
	}
	
	public static Collection<String> splitParams(String data){
		Collection<String> parameters = new HashSet<String>();
		String queryParams = data;
		if(queryParams != null) {
			if(queryParams.contains("&")) {
			    String [] paramItems = queryParams.split("&");
			    for(String p: paramItems) parameters.add(p);
		    }else {
		    	// apparently just one
		    	parameters.add(queryParams);
		    }
		}
		return parameters;
	}
	
	public static String urlEncodeData(String data) {
		Collection<String> params = splitParams(data);
		StringBuffer buf = new StringBuffer();
		params.forEach(item -> {
			try {
				String [] array = item.split("=");
				buf.append(URLEncoder.encode(array[0], "UTF-8"));
				buf.append("=");
				buf.append(URLEncoder.encode(array[1], "UTF-8"));
				buf.append("&");
			}catch(IOException x) {
				throw new RuntimeException("Parameter looks bad: "+item);
			}
		});
		
		if(buf.length()>0)buf.deleteCharAt(buf.length()-1); // snip off the last &
		
		return buf.toString();
	}
	
	public static String urlEncodeDataRFC3896(String data) {
		Collection<String> params = splitParams(data);
		StringBuffer buf = new StringBuffer();
		params.forEach(item -> {
				String [] array = item.split("=");
				buf.append(percentEncode(array[0]));
				buf.append("=");
				buf.append(percentEncode(array[1]));
				buf.append("&");	
		});
		
		if(buf.length()>0)buf.deleteCharAt(buf.length()-1); // snip off the last &
		
		return buf.toString();
	}
	
	
}
