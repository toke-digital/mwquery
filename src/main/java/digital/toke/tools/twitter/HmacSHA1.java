/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2019 David R. Smith All Rights Reserved 
 */
package digital.toke.tools.twitter;

import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
See https://gist.githubusercontent.com/ishikawa/88599/raw/3195bdeecabeb38aa62872ab61877aefa6aef89e/gistfile1.java
*/

public class HmacSHA1 {

	private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

	public static String calculateRFC2104HMAC(String data, String key) {
		SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);
		Mac mac  = null;
		try {
			mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
			mac.init(signingKey);
			return Base64.getEncoder().encodeToString(mac.doFinal(data.getBytes()));
		}catch(Exception x) {
			throw new RuntimeException(x);
		}
	}
}