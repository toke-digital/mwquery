package digital.toke.tools;

import org.junit.jupiter.api.Test;

import digital.toke.tools.twitter.OAuthCompute;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class TweetTest {
	
	
	@Test
	public void test1() {
		
	}

	@Test
	public void test0() {
		
		
		// these are all published test values from dev.twitter.com
		// https://developer.twitter.com/en/docs/basics/authentication/guides/creating-a-signature.html
		// note that the final header example signature value is apparently wrong... ;-)
		
		String consumerKey = "xvz1evFS4wEEPTGEFPHBog";
		String token = "370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb";
		
		OAuthCompute tah = OAuthCompute.builder(consumerKey, token)
				.addParameter("include_entities", "true")
				.addParameter("status", "Hello Ladies + Gentlemen, a signed OAuth request!")
				.consumerSecret("kAcSOqF21Fu85e7zjz7ZN2U4ZRhfV3WpwPAoE3Z7kBw")
				.oauthTokenSecret("LswwdoUaIvS8ltyTt5jkRh4J50vUPVVHtR2YPi5kE")
				.method("POST")
				.url("https://api.twitter.com/1.1/statuses/update.json")
				.nonce("kYjzVBB8Y0ZFabxSWbWovY3uYSQ2pTgmZeNu2VS4cg") // normally just use .nonce()
				.timestamp("1318622958") // normally just use .timestamp();
				.signatureMethod()
				.version()
				.debug()
				.build();
		
		String defParamStr = "include_entities=true&oauth_consumer_key=xvz1evFS4wEEPTGEFPHBog&oauth_nonce=kYjzVBB8Y0ZFabxSWbWovY3uYSQ2pTgmZeNu2VS4cg&oauth_signature_method=HMAC-SHA1&oauth_timestamp=1318622958&oauth_token=370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb&oauth_version=1.0&status=Hello%20Ladies%20%2B%20Gentlemen%2C%20a%20signed%20OAuth%20request%21"; 
		assertEquals(defParamStr,tah.getParameterString());
		
		String defSignatureBaseString = 
		"POST&https%3A%2F%2Fapi.twitter.com%2F1.1%2Fstatuses%2Fupdate.json&include_entities%3Dtrue%26oauth_consumer_key%3Dxvz1evFS4wEEPTGEFPHBog%26oauth_nonce%3DkYjzVBB8Y0ZFabxSWbWovY3uYSQ2pTgmZeNu2VS4cg%26oauth_signature_method%3DHMAC-SHA1%26oauth_timestamp%3D1318622958%26oauth_token%3D370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb%26oauth_version%3D1.0%26status%3DHello%2520Ladies%2520%252B%2520Gentlemen%252C%2520a%2520signed%2520OAuth%2520request%2521";

		assertEquals(defSignatureBaseString,tah.getSignatureBaseString());
		
		String defConsumerSigningKey = "kAcSOqF21Fu85e7zjz7ZN2U4ZRhfV3WpwPAoE3Z7kBw&LswwdoUaIvS8ltyTt5jkRh4J50vUPVVHtR2YPi5kE";
		assertEquals(defConsumerSigningKey, tah.getSigningKey());
		
		String defSignature = "hCtSmYh+iHYCEqBWrE7C7hYmtUk=";
		
		assertEquals(defSignature, tah.getSignature());
		
		String defHeader = "OAuth oauth_consumer_key=\"xvz1evFS4wEEPTGEFPHBog\", oauth_nonce=\"kYjzVBB8Y0ZFabxSWbWovY3uYSQ2pTgmZeNu2VS4cg\", oauth_signature=\"hCtSmYh%2BiHYCEqBWrE7C7hYmtUk%3D\", oauth_signature_method=\"HMAC-SHA1\", oauth_timestamp=\"1318622958\", oauth_token=\"370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb\", oauth_version=\"1.0\"";
	 
	assertEquals(defHeader.length(),tah.getHeader().length());
	assertEquals(defHeader,tah.getHeader());
		
	}
}
