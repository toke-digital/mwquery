package digital.toke.tools.twitter;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import digital.toke.tools.CmdLineParser;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TweetCLI {
	
	public static final MediaType URLENCODED = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");
	public static String url = "https://api.twitter.com/1.1/statuses/update.json";
	
	protected final static OkHttpClient client = new OkHttpClient();

	public static void main(String[] args) {
	
		 if(args.length == 0){
	 			help();
	 			System.exit(1);
	     }
		 
		CmdLineParser parser = new CmdLineParser();
		CmdLineParser.Option<Boolean> helpOption = parser.addBooleanOption('h', "help");
		
		CmdLineParser.Option<String> configOption = parser.addStringOption('c', "config");
		
		CmdLineParser.Option<String> statusOption = parser.addStringOption('s', "status");
		
		try {
			parser.parse(args);
		} catch (digital.toke.tools.CmdLineParser.OptionException e) {
			e.printStackTrace();
			return;
		}

		boolean needsHelp = parser.getOptionValue(helpOption, Boolean.FALSE);

		if (needsHelp) {
			help();
			return;
		}
		
		String configPath = parser.getOptionValue(configOption, "twitter.properties");
		String status = parser.getOptionValue(statusOption, null);
		
		if(status == null) {
			help();
			return;
		}
		
		if(status.length()> 280) throw new RuntimeException("status cannot exceed 280 chars"); 

		InputStream in = null;
		Properties props = new Properties();

		try {
			File config = new File(configPath);
			if (config.exists()) {
				// external config file
				in = new FileInputStream(configPath);
			} else {
				throw new RuntimeException("config path does not appear to exist!");
			}

			props.load(in);
		}catch(Exception x) {
			x.printStackTrace();
		}
			
		String consumerKey = props.getProperty("consumer_key");
		String token = props.getProperty("token");
		String consumerSecret = props.getProperty("consumer_secret");
		String tokenSecret = props.getProperty("token_secret");
		
		OAuthCompute tah = OAuthCompute.builder(consumerKey, token)
				.addParameter("include_entities", "true")
				.addParameter("status", status)
				.consumerSecret(consumerSecret)
				.oauthTokenSecret(tokenSecret)
				.method("POST")
				.url(url)
				.nonce()
				.timestamp()
				.signatureMethod()
				.version()
				.debug()
				.build();
		
			Map<String,String> headerMap = new HashMap<String,String>();
			headerMap.put("Authorization", tah.getHeader());
		
			Headers headers = Headers.of(headerMap);
		
			RequestBody body = RequestBody.create(URLENCODED, tah.getBody());
			Request request = new Request.Builder()
					.url(url+"?include_entities=true")
					.post(body)
					.headers(headers)
					.build();
			
			try (Response response = client.newCall(request).execute()) {
				
				System.out.println(response.isSuccessful()+", "+response.code()+", "+response.body().string());
				
			}catch(Exception x) {
				x.printStackTrace();
			}
		

	}
	
	private static void help() {

		System.out.println("Toke Digital - TweetCLI, version 1.0.0");
		System.out.println("Author: David R. Smith <dave.smith10@det.nsw.edu.au>");
		System.out.println("");
		System.out.println("Options:");
		System.out.println("-c --config           | Config file location, required");
		System.out.println("-s --status           | the 'tweet' text");
		System.out.println("");
		System.out.println("-h --help             | Show this help");
		System.out.println("");

	}

}
