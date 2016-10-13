/**
 *  Copyright 2009 Welocalize, Inc. 
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */
package test.globalsight.ling.docproc.extractor.javaprop;

/*
 * Copyright (c) 2000 GlobalSight Corporation. All rights reserved.
 *
 * THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF 
 * GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
 * IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
 * OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
 * AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.
 *
 * THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
 * SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
 * UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
 * BY LAW.
 */


import com.globalsight.ling.common.JPEscapeSequence;
import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test case for escape sequences
 */
public class JPEscapeTest extends TestCase {
	
/**
 * JPEscapeTest constructor comment.
 */
public JPEscapeTest(String p_name) {
	
	super(p_name);
	//m_configParams = null;

}

/**
 * Insert the method's description here.
 * Creation date: (8/16/2000 10:40:43 AM)
 */
public static Test suite()
{ 
	return new TestSuite(JPEscapeTest.class);
}
/**
 * Starts the application.
 * @param args an array of command-line arguments
 */
public void testRoundTrip() {

		JPEscapeSequence JPEsc = new JPEscapeSequence();
		String result ="";
		String cvt ="";
		
		String s = new String("  \\t \\f\\ \\t\\u00c0 < \\u0041 \\A \\u0042 \\B \\u0456 \\uf902 \\\\\\'\\\"\\b\\f\\n\\r\\tefg\\h #!:= \\#\\!\\:\\=");
		String trg = new String("\\  \\t \\f \\t\\u00c0 < A A B B \\u0456 \\uf902 \\\\\\'\\\"b\\f\\n\\r\\tefgh \\#\\!\\:\\= \\#\\!\\:\\=");
		
		try
		{
			cvt = JPEsc.decode(s);
			result = JPEsc.encode( cvt );
		}
		catch(Exception e)
		{
			fail(e.toString());
		}

		assertEquals("Compare Failed", trg, result);

}
}
