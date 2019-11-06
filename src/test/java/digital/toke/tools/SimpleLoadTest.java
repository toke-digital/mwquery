package digital.toke.tools;

import java.security.SecureRandom;
import java.util.Base64;

import org.junit.jupiter.api.Test;

public class SimpleLoadTest {
	
	@Test
	public void loadTest() {
		
		for(int i = 0;i<1000;i++) {
			test(i);
		}
	}
	
	
	public void test(int count) {
		
		System.err.println("Starting "+count);
		
			String [] cmd = {
					"-r", "POST", 
					"-h", "Content-Type: application/json",
					"-h", "X-OpenAM-Username: dave.smith10",
					"-h", "X-OpenAM-Password: password1",
					"-h", "Accept-API-Version: resource=2.0, protocol=1.0",
					"-d", "{}",
					"-u", "https://sso.pre.det.nsw.edu.au/sso/json/realms/root/authenticate",
					"--logging",
					"--flatten",
					"--dump",
					"-c", "C:\\Users\\daves\\cookies.bin",
					"--time"
					
			};
		
			digital.toke.tools.Main.main(cmd);
			
			
			String [] cmd2 = {
					"-r", "POST", 
					"-h", "Content-Type: application/json",
					"-h", "Cache-Control: no-cache",
					"-h", "iPlanetDirectoryPro: tokenId",
					"-h", "Accept-API-Version: resource=3.1, protocol=1.0",
					"-u", "https://sso.pre.det.nsw.edu.au/sso/json/realms/root/sessions/?_action=logout",
					"-d", "",
					"--logging",
					"-c", "C:\\Users\\daves\\cookies.bin",
					"--dump",
					"--time"
					
					
			};
			
			digital.toke.tools.Main.main(cmd2);
			
				
			
		
				
		}
	
	@Test
	public void test1() {
		
			String [] cmd = {
					"-r", "POST", 
					"-h", "Content-Type: application/json",
					"-h", "X-OpenAM-Username: dave.smith10",
					"-h", "X-OpenAM-Password: password1",
					"-h", "Accept-API-Version: resource=2.0, protocol=1.0",
					"-d", "{}",
					"-u", "https://sso.pre.det.nsw.edu.au/sso/json/realms/root/authenticate",
					"-f",
					"--out", "G:/repos/mwquery/transient.txt"
					
			};
			
			digital.toke.tools.Main.main(cmd);
			
		}
	
	@Test
	public void test2() {
		
		String [] cmd2 = {
				"-r", "POST", 
				"-h", "Content-Type: application/json",
				"-h", "Cache-Control: no-cache",
				"-h", "iPlanetDirectoryPro: 4KKvrTG-y4ZkrE98xQ9lZmHq6YA.*AAJTSQACMDIAAlNLABxYSzI4akZzNy9nN090VnB3M3NNY3ZYWWZuTzQ9AAR0eXBlAANDVFMAAlMxAAIwMw..*",
				"-h", "Accept-API-Version: resource=3.1, protocol=1.0",
				"-u", "https://sso.pre.det.nsw.edu.au/sso/json/realms/root/sessions/?_action=logout",
				"-d", "",
				"--logging"
				
				
		};
		
		digital.toke.tools.Main.main(cmd2);
	}
	
	@Test
	public void testRootWebapp() throws Exception {
		
		 SecureRandom sec = SecureRandom.getInstanceStrong();
		
		for(int i = 0;i<1000;i++) {
			byte [] bytes = new byte[128];
			sec.nextBytes(bytes);
			String s = new String(Base64.getMimeEncoder().encode(bytes));
			String [] cmd2 = {
				"-r", "GET", 
				"-u", "https://sso.pre.det.nsw.edu.au/sso/isAlive.jsp?dave.smith10&count="+i+"&key="+s,
				"-c", "C:\\Users\\daves\\cookies.bin",
				"--logging"
				
				
			};
		
			digital.toke.tools.Main.main(cmd2);
		}
		
	
		
	}
	
}

