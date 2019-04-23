/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2019 David R. Smith All Rights Reserved 
 */
package digital.toke.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

public class ResultTest {

	@Test
	public void test0() {
		try (
			 InputStream in = this.getClass().getResourceAsStream("/example.json");
			 InputStreamReader inReader = new InputStreamReader(in, "UTF-8");
			BufferedReader reader = new BufferedReader(inReader);
		) {
			StringBuffer buf = new StringBuffer();
			Stream<String> stream = reader.lines();
			stream.forEachOrdered(item -> buf.append(item+'\n'));
			Result r = new Result(200, true, buf.toString());
			r.walk();
			
		    
	
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
