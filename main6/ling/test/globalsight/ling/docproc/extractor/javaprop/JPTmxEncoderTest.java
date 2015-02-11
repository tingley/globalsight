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
import com.globalsight.ling.common.NativeEnDecoderException;
import com.globalsight.ling.docproc.extractor.javaprop.JPTmxEncoder;
import com.globalsight.ling.docproc.extractor.javaprop.*;
import junit.framework.TestCase;

/**
 * Tmx Encoder class test case
 */
public class JPTmxEncoderTest extends TestCase {
	public java.lang.String src;
	public JPTmxEncoder Tmx;
/**
 * JPTmxEncoderTest constructor comment.
 */
public JPTmxEncoderTest(String p_name) {
	super(p_name);
}

/**
 * Insert the method's description here.
 * Creation date: (8/15/2000 6:46:42 PM)
 */
public void setUp()
{
	src = new String(" tab\t linefeed\r cariage return\n formfeed\f");
	Tmx = new JPTmxEncoder();
}
/**
 * Test - ALL_TMX_NON_ERASABEL with no leading spaces
 */
public void testNumber1()
{

	Tmx.setErasable(JPConstants.ALL_TMX_NON_ERASABLE); 
	Tmx.enableTmxOnLeadingSpaces(false);
	String trg = new String(" tab<ph type=\"x-tab\">\\t</ph> linefeed<ph type=\"lb\">\\r</ph> cariage return<ph type=\"lb\">\\n</ph> formfeed<ph type=\"x-formfeed\">\\f</ph>");
	String result = Tmx.encode(src);

	assertEquals(trg, result);
	
/*	if(!trg.equals(result))
	{
		System.out.println("Encode test 1 failed!");
		System.out.println("Input  : " + JPEsc.encode(src));
		System.out.println("Desired: " + trg);
		System.out.println("Result : " + result);
		System.exit(1);
	}
	else
		System.out.println("\tTest #1 - OK.");
*/
}
/**
 * Test - ALL_TMX_ERASABLE, enableTmxOnLeadingSpaces(false);
 */
public void testNumber2()
{
	
	Tmx.setErasable(JPConstants.ALL_TMX_ERASABLE); 
	Tmx.enableTmxOnLeadingSpaces(false);

	String trg = new String(" tab<ph type=\"x-tab\" erasable=\"1\">\\t</ph> linefeed<ph type=\"lb\" erasable=\"1\">\\r</ph> cariage return<ph type=\"lb\" erasable=\"1\">\\n</ph> formfeed<ph type=\"x-formfeed\" erasable=\"1\">\\f</ph>");
	String result = Tmx.encode(src);

	assertEquals("Compare failed", trg, result);
/*	
	if(!trg.equals(result))
	{
		System.out.println("Encode test 2 failed!");
		System.out.println("Input  : " + JPEsc.encode(src));
		System.out.println("Desired: " + trg);
		System.out.println("Result : " + result);
		System.exit(2);
	}
	else
		System.out.println("\tTest #2 - OK.");
*/
}
/**
 * Test - LEADING_TMX_NON_ERASABLE), enableTmxOnLeadingSpaces(false);
 */
public void testNumber3()
{

	Tmx.setErasable(JPConstants.LEADING_TMX_NON_ERASABLE); 
	Tmx.enableTmxOnLeadingSpaces(false);

	String trg = new String(" tab<ph type=\"x-tab\" erasable=\"1\">\\t</ph> linefeed<ph type=\"lb\" erasable=\"1\">\\r</ph> cariage return<ph type=\"lb\" erasable=\"1\">\\n</ph> formfeed<ph type=\"x-formfeed\" erasable=\"1\">\\f</ph>");
	String result = Tmx.encode(src);

	assertEquals("Compare failed", trg, result);
	
/*	if(!trg.equals(result))
	{
		System.out.println("Encode test 3 failed!");
		System.out.println("Input  : " + JPEsc.encode(src));
		System.out.println("Desired: " + trg);
		System.out.println("Result : " + result);
		System.exit(2);
	}
	else
		System.out.println("\tTest #3 - OK.");
*/

}
/**
* Test - EADING_TMX_NON_ERASABLE, enableTmxOnLeadingSpaces(true)
*/
public void testNumber4()
{
	
	Tmx.setErasable(JPConstants.LEADING_TMX_NON_ERASABLE); 
	Tmx.enableTmxOnLeadingSpaces(true);

	String trg = new String("<ph type=\"x-space\"> </ph>tab<ph type=\"x-tab\" erasable=\"1\">\\t</ph> linefeed<ph type=\"lb\" erasable=\"1\">\\r</ph> cariage return<ph type=\"lb\" erasable=\"1\">\\n</ph> formfeed<ph type=\"x-formfeed\" erasable=\"1\">\\f</ph>");
	String result = Tmx.encode(src);
	
	assertEquals("Compare failed",  trg, result);

/*
	if(!trg.equals(result))
	{
		System.out.println("Encode test 4 failed!");
		System.out.println("Input  : " + JPEsc.encode(src));
		System.out.println("Desired: " + trg);
		System.out.println("Result : " + result);
		System.exit(2);
	}
	else
		System.out.println("\tTest #4 - OK.");
*/
}
/**
* Test - LEADING_TMX_NON_ERASABLE, enableTmxOnLeadingSpaces(true),
* Source text is a single space
*/
public void testNumber5() 
{
	Tmx.setErasable(JPConstants.LEADING_TMX_NON_ERASABLE); 
	Tmx.enableTmxOnLeadingSpaces(true);

	String trg = new String(" ");
	String result = Tmx.encode(" "); //single space string
	
	assertEquals("Compare failed", trg, result);
	
}
/**
* Test - LEADING_TMX_NON_ERASABLE, enableTmxOnLeadingSpaces(true),
* Source text is an empty string
*/
public void testNumber6()
{
	Tmx.setErasable(JPConstants.LEADING_TMX_NON_ERASABLE); 
	Tmx.enableTmxOnLeadingSpaces(true);

	String trg = new String("");
	String result = Tmx.encode(""); //single space string
	
	assertEquals("Compare failed", trg, result);
		
}
/**
* Test - LEADING_TMX_NON_ERASABLE, enableTmxOnLeadingSpaces(true),
* Source text is a null reference
*/
public void testNumber7() 
{
	Tmx.setErasable(JPConstants.LEADING_TMX_NON_ERASABLE); 
	Tmx.enableTmxOnLeadingSpaces(true);

	String trg = null;
	String result = Tmx.encode(null); 
	
	assertNotNull("Compare failed", result);
		
}
}
