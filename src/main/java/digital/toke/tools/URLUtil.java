package digital.toke.tools;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class URLUtil {

	// from https://github.com/Twitter4J/Twitter4J/blob/master/twitter4j-core/src/internal-http/java/twitter4j/HttpParameter.java
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
	            } else if (focus == '%' && (i + 1) < encoded.length()
	                    && encoded.charAt(i + 1) == '7' && encoded.charAt(i + 2) == 'E') {
	                buf.append('~');
	                i += 2;
	            } else {
	                buf.append(focus);
	            }
	        }
	        return buf.toString();
	    }
}
