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

import com.globalsight.ling.docproc.ExtractorException;
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
import com.globalsight.ling.docproc.extractor.plaintext.Parser;
import com.globalsight.ling.docproc.extractor.plaintext.PTToken;
import java.io.FileReader;
import java.io.File;
import java.io.StringReader;
import test.FileListBuilder;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * Test class for the Pain Text parser.
 */
public class ParserTest extends TestCase {
	private java.lang.String m_srcDir;
/**
 * Constructor required by Junit.
 */
public ParserTest(String p_sName) {
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
 * Feeds a formatted string to the parser and then monitors the tokens
 * that are returned by the getNextToken() method.
 */
public void test1()
{
	StringReader sr = new StringReader(" \ttext\ftext\ntext\rtext\u00a0");
	Parser p = new Parser(sr);
	PTToken token = new PTToken();
	
	try
	{
		token = p.getNextToken();
		assertEquals("wrong token", token.m_nType, PTToken.TEXT);
		assertEquals("wrong token content" , token.m_strContent, " ");
		
		token = p.getNextToken();
		assertEquals("wrong token", token.m_nType, PTToken.TAB);
		assertEquals("wrong token content", token.m_strContent, "\t");

		token = p.getNextToken();
		assertEquals("wrong token", token.m_nType, PTToken.TEXT);
		assertEquals("wrong token content", token.m_strContent, "text");

		token = p.getNextToken();
		assertEquals("wrong token", token.m_nType, PTToken.FF);
		assertEquals("wrong token content", token.m_strContent, "\f");
		
		token = p.getNextToken();
		assertEquals("wrong token", token.m_nType, PTToken.TEXT);
		assertEquals("wrong token content", token.m_strContent, "text");
		
		token = p.getNextToken();
		assertEquals("wrong token", token.m_nType, PTToken.LINEBREAK);
		assertEquals("wrong token content", token.m_strContent, "\n");
		
		token = p.getNextToken();
		assertEquals("wrong token", token.m_nType, PTToken.TEXT);
		assertEquals("wrong token content", token.m_strContent, "text");
						
		token = p.getNextToken();
		assertEquals("wrong token", token.m_nType, PTToken.LINEBREAK);
		assertEquals("wrong token content", token.m_strContent, "\n");

		token = p.getNextToken();
		assertEquals("wrong token", token.m_nType, PTToken.TEXT);
		assertEquals("wrong token content", token.m_strContent, "text");

		token = p.getNextToken();
		assertEquals("wrong token", token.m_nType, PTToken.NBSPACE);
		assertEquals("wrong token content", token.m_strContent, "\u00a0");

		token = p.getNextToken();
		assertEquals("wrong token", token.m_nType, PTToken.EOF);

	}
	catch(Exception e)
	{
		fail(e.toString());
	}
 
}
/**
 * Runs the parser on multiple files and report high level exceptions.
 */
public void testBatch()
{
	PTToken token = null; 
	
	FileListBuilder FLB = new FileListBuilder();
	FLB.addRecursive(m_srcDir, ".txt");
	FLB.setDescription("Text Files");

	File fin; 
 	while( (fin= FLB.getNextFile()) != null )
 	{
	 	//System.out.println(fin.getName());
		try
		{
			FileReader fr = new FileReader(fin.getPath()); 
			Parser p = new Parser(fr);
			token = p.getNextToken();
			while(token.m_nType != PTToken.EOF)
			{ token = p.getNextToken(); }
		}
		catch(Exception e )
		{
			fail(e.toString());
		}
 	}
	
}
}
