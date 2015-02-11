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
package test.globalsight.ling.docproc.extractor.plaintext;

import java.util.Vector;
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
import com.globalsight.ling.docproc.extractor.plaintext.PTTmxController;
import com.globalsight.ling.docproc.extractor.plaintext.PTTmxControllerConstants;
import com.globalsight.ling.docproc.extractor.plaintext.PTToken;
import java.io.StringReader;
import java.io.File;
import junit.extensions.*;
import junit.framework.*;

import test.FileListBuilder;

/**
 * Test class for the Pain Text Tmx controller.
 */
public class PTTmxControllerTest extends TestCase {
	private java.lang.String m_srcDir;
/**
 * Constructor required by Junit.
 */
public PTTmxControllerTest(String p_sName) {
	super(p_sName);
}

/**
 * Initializes the test case.
 */
public void setUp() 
{
	m_srcDir = "C:\\work\\ling\\test\\globalsight\\ling\\docproc\\extractor\\plaintext\\TestFiles\\";

}
/**
 * Test the default controller rules as applied to a fixed
 * vector of token objects. The test simulates the okens that 
 * the parser would have returned for the given string. Then
 * the rules are applied to the vector. After that, we walk the
 * vector to confirm all tokens have been configured according 
 * to the default controller rules for plain text.
 */
public void testPublicMethods()
{
	PTTmxController Ctrl = new PTTmxController();
	Vector v = new Vector();
	PTToken token;

	// Simulates the string: "\t \t \n text\ftext\ntext\rtext\u00a0"
	v.add(new PTToken(PTToken.TAB,"\t"));
	v.add(new PTToken(PTToken.TEXT," "));
	v.add(new PTToken(PTToken.TAB,"\t"));
	v.add(new PTToken(PTToken.TEXT," "));
	v.add(new PTToken(PTToken.LINEBREAK,"\n"));;
	v.add(new PTToken(PTToken.TEXT," text"));
	v.add(new PTToken(PTToken.FF,"\f"));
	v.add(new PTToken(PTToken.TEXT,"text"));
	v.add(new PTToken(PTToken.LINEBREAK,"\n"));
	v.add(new PTToken(PTToken.TEXT,"text"));
	v.add(new PTToken(PTToken.LINEBREAK,"\r"));
	v.add(new PTToken(PTToken.TEXT,"text"));
	v.add(new PTToken(PTToken.NBSPACE,"\u00a0"));
	
	Ctrl.applyRules(v);

	token = (PTToken)v.get(0); // \t
	assertEquals("assert1",token.m_nPos, PTTmxControllerConstants.LEADING);
	assertTrue("assert2",Ctrl.makeTmx(token.m_strContent.charAt(0), token.m_nPos));
	assertTrue("assert3",Ctrl.getStart().indexOf("erasable=\"1\"") != -1);

	token = (PTToken)v.get(1); // space
	assertEquals("assert4",token.m_nPos, PTTmxControllerConstants.LEADING);

	token = (PTToken)v.get(2); // \t
	assertEquals("assert5",token.m_nPos, PTTmxControllerConstants.LEADING);
	assertTrue("assert6",Ctrl.makeTmx(token.m_strContent.charAt(0), token.m_nPos));
	assertTrue("assert7",Ctrl.getStart().indexOf("erasable=\"1\"") != -1);

	token = (PTToken)v.get(3); // space
	assertEquals("assert8",token.m_nPos, PTTmxControllerConstants.LEADING);

	token = (PTToken)v.get(4); // \n
	assertEquals("assert9",token.m_nPos, PTTmxControllerConstants.LEADING);
	assertTrue("assert10",Ctrl.makeTmx(token.m_strContent.charAt(0), token.m_nPos));
	assertEquals("assert11",Ctrl.getStart().indexOf("erasable=\"1\""), -1);

	token = (PTToken)v.get(5); // text
	assertEquals("assert12",token.m_nPos, PTTmxControllerConstants.EMBEDDED);

	token = (PTToken)v.get(6); // \f
	assertEquals("assert13",token.m_nPos, PTTmxControllerConstants.EMBEDDED);
	assertTrue("assert14",Ctrl.makeTmx(token.m_strContent.charAt(0), token.m_nPos));
	assertTrue("assert15",Ctrl.getStart().indexOf("erasable=\"1\"") != -1);

	token = (PTToken)v.get(7); // text
	assertEquals("assert16",token.m_nPos, PTTmxControllerConstants.EMBEDDED);

	token = (PTToken)v.get(8); // \n
	assertEquals("assert17",token.m_nPos, PTTmxControllerConstants.EMBEDDED);
	assertTrue("assert18",Ctrl.makeTmx(token.m_strContent.charAt(0), token.m_nPos));
	assertEquals("assert19",Ctrl.getStart().indexOf("erasable=\"1\""), -1);

	token = (PTToken)v.get(9); // text
	assertEquals("assert20",token.m_nPos, PTTmxControllerConstants.EMBEDDED);

	token = (PTToken)v.get(10); // \n
	assertEquals("assert21",token.m_nPos, PTTmxControllerConstants.EMBEDDED);
	assertTrue("assert22",Ctrl.makeTmx(token.m_strContent.charAt(0), token.m_nPos));
	assertEquals("assert23",Ctrl.getStart().indexOf("erasable=\"1\""), -1);

	token = (PTToken)v.get(11); // text
	assertEquals("assert24",token.m_nPos, PTTmxControllerConstants.EMBEDDED);

	token = (PTToken)v.get(12); // non breaking space - \u00a0
	assertEquals("assert25",token.m_nPos, PTTmxControllerConstants.TRAILING);
	assertTrue("assert26",Ctrl.makeTmx(token.m_strContent.charAt(0), token.m_nPos));
	assertTrue("assert27",Ctrl.getStart().indexOf("erasable=\"1\"") != -1);

}
}
