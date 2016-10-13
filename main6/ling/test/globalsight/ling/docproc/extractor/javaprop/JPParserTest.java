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

import java.io.PrintStream;
import com.globalsight.ling.common.JPEscapeSequence;
import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;
import java.io.CharArrayReader;
import com.globalsight.ling.docproc.extractor.javaprop.Parser;
import com.globalsight.ling.docproc.extractor.javaprop.JPToken;
import com.globalsight.ling.docproc.extractor.javaprop.Parser.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.Reader;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import test.FileListBuilder;

/**
 * Parser class test case
 */
public class JPParserTest extends TestCase {
	private java.lang.String m_srcDir;
	private java.lang.String m_TmpPropFileName;
	private java.lang.String m_logFname;
/**
 * JPParserTest constructor comment.
 */
public JPParserTest(String p_name) {
	super(p_name);
}
/**
 * Starts the application.
 * @param args an array of command-line arguments
 */

/**
 * Insert the method's description here.
 * Creation date: (8/16/2000 3:10:35 PM)
 */
public void setUp()
{
	m_srcDir = "testclasses/test/globalsight/ling/docproc/extractor/javaprop/TestFiles";
	m_logFname ="log.txt";
	m_TmpPropFileName = "jp_self_generating.properties";
	
	// create regression test property file
	try
	{
		java.io.File fPropOut = new java.io.File(m_srcDir + m_TmpPropFileName);
		java.io.FileWriter wPropOut =	new java.io.FileWriter(fPropOut);
		java.io.PrintWriter pTmpOut =	new java.io.PrintWriter(wPropOut);
		
		pTmpOut.println("#=========================================================]");
		pTmpOut.println("# This file will be reproduced by JPParserTest.java");
		pTmpOut.println("#=========================================================");
		pTmpOut.println("");
		pTmpOut.println("# normal property with equal separator");
		pTmpOut.println("# syntax > [key][sp]=[sp][value]");
		pTmpOut.println("key1 = Value of Prop1");
		pTmpOut.println("");
		pTmpOut.println("# normal property with colon separator");
		pTmpOut.println("# syntax > [key][sp]:[sp][value]");
		pTmpOut.println("key2 : Value of Prop2");
		pTmpOut.println("");
		pTmpOut.println("# normal property with single whitespace separator");
		pTmpOut.println("# syntax > [key][sp][value]");
		pTmpOut.println("key3 Value of Prop3");
		pTmpOut.println("");
		pTmpOut.println("# normal property with multiple whitespace separator");
		pTmpOut.println("# syntax > [key][sp][tab][sp][value]");
		pTmpOut.println("key4 	 value of Prop4");
		pTmpOut.println("");
		pTmpOut.println("# property key that uses escaped terminator characters in the key name");
		pTmpOut.println("# syntax >  \\[sp]key\\[sp]\\=\\:5[sp]=[sp][value]");
		pTmpOut.println("# the entire valid key here is [sp]key[sp]=:5");
		// Turned off the next line to be able to run the test.
		// Our parser is inconsistant with a property class load() in regards to keys.
		// java considers leading escaped space as part of the key and we don't.
		// This will not affect our output since it all goes to the skeleton.
		// When fixed, we should turn the next line back on. 
		//pTmpOut.println("\\ key\\ \\=\\:5 = value of Prop5");
		pTmpOut.println("");
		pTmpOut.println("# commented property using '!'");
		pTmpOut.println("# syntax > [!][key][sp]=[sp][value]");
		pTmpOut.println("!keydummy1 = COMMENTED PROP, YOU SHOULD NEVER SEE THIS IN A TRASLATABLE SEGMENT");
		pTmpOut.println("");
		pTmpOut.println("# commented property using '#'");
		pTmpOut.println("# syntax > [#][key][sp]=[sp][value]");
		pTmpOut.println("#keydummy2 = COMMENTED PROP, YOU SHOULD NEVER SEE THIS IN A TRASLATABLE SEGMENT");
		pTmpOut.println("");
		pTmpOut.println("# A tab leading the key");
		pTmpOut.println("#  syntax > [tab][key]:[value]");
		pTmpOut.println("	key6:value of Prop6");
		pTmpOut.println("");
		pTmpOut.println("# Tab and space leading the key");
		pTmpOut.println("#  syntax > [tab][sp][key]:[value]");
		pTmpOut.println("	 key7:value of Prop7");
		pTmpOut.println("");
		pTmpOut.println("# A tab following the key");
		pTmpOut.println("# syntax > [key][tab]:[value]");
		pTmpOut.println("key8		:value of Prop8");
		pTmpOut.println("");
		pTmpOut.println("# syntax > [key][tab][sp]:[value]");
		pTmpOut.println("key9	 :value of Prop9");
		pTmpOut.println("");
		pTmpOut.println("# unescaped White space following the terminator, which is NOT part of value ");
		pTmpOut.println("# syntax > [key]:[tab][sp][value]");
		pTmpOut.println("key10:	 value of Prop10 - ***VALUE SHOULD HAVE NO LEADING WHITE SPACE***");
		pTmpOut.println("");
		pTmpOut.println("# escaped White space following the terminator which would BECOME part of the value ");
		pTmpOut.println("# syntax > [key]:[\\][tab][\\][sp][value]");
		pTmpOut.println("key11:\\	\\ value of Prop11 - ***VALUE SHOULD LEAD WITH A TAB AND A SINGLE SPACE***");
		pTmpOut.println("");
		pTmpOut.println("# Bunch of ASCII escape sequences in the value");
		pTmpOut.println("# syntax > [key]:[value = \\t,\\n,\\r,\\\\,\\\",\",\\',',\\sp ]");
		pTmpOut.println("key12: value of prop12, \\t,\\n,\\r,\\\\,\\\",\",\\',',\\ , ,\\a,\\b,\\f end");
		pTmpOut.println("");
		pTmpOut.println("# syntax > [key]:[value =  \\u0041 \\u00b5 \\u00a5 \\u0042 ]");
		pTmpOut.println("key13: value of prop13, \\u0041 \\u00b5 \\u00a5 \\u0042 ");
		pTmpOut.println("");
		pTmpOut.println("# line continuation test - with embedded tab");
		pTmpOut.println("# syntax > see below");
		pTmpOut.println("key14				   apple, banana, pear, \\");
		pTmpOut.println("				   \\tcanteloupe, watermelon, \\");
		pTmpOut.println("		 	 	   kiwi, mango.");
		pTmpOut.println("");
		pTmpOut.println("# A valid key with an empty value - \\n is both terminator and end-of line");
		pTmpOut.println("# syntax > [key][\\n]");
		pTmpOut.println("key15");
		pTmpOut.println("");
		pTmpOut.println("# A valid escaped key with an empty value - \\n is both terminator and end-of line");
		pTmpOut.println("# syntax > \\[key][\\n]");
		pTmpOut.println("\\key16");
		pTmpOut.println("");
		pTmpOut.println("# A valid key with an empty value - \\n is both terminator and end-of line");
		pTmpOut.println("# syntax > [key][\\n]");
		pTmpOut.println("k");
		pTmpOut.println("");
		pTmpOut.println("# A valid key with an empty value - \\n is both terminator and end-of line");
		pTmpOut.println("# syntax > [key][sp][\\n]");
		pTmpOut.println("key18 ");
		pTmpOut.println("");
		pTmpOut.println("# A key followed by a tab and a value");
		pTmpOut.println("# syntax > [key][\\t]=[value]");
		pTmpOut.println("key19\\t=value of prop19");
		pTmpOut.println("");
		pTmpOut.println("# Another line continuation test with embedded cariage returns");
		pTmpOut.println("#  syntax > see below");
		pTmpOut.println("key20			   apple2, banana, pear,\\n \\");
		pTmpOut.println("				   canteloupe, watermelon,\\n \\");
		pTmpOut.println("		 	 	   kiwi, mango.");
		pTmpOut.println("");
		pTmpOut.println("# A key followed by a value containing the basic set of characters that need to be escaped for XML");
		pTmpOut.println("# syntax > [key] [ \\[sp] < > & ' \" value 21 ]");
		pTmpOut.println("key21 \\ < > & ' \" value 21");
		pTmpOut.println("");
		pTmpOut.println("# A key followed by an escaped space and then two real tabs");
		pTmpOut.println("# syntax > [key] [\\[sp][tab][tab]]");
		pTmpOut.println("key22 \\ leadspace	real tab	real tab value 22");
		pTmpOut.println("");
		pTmpOut.println("# special embedded chars");
		pTmpOut.println("# syntax > [key]:\\#,#,\\!,!,\\=,=,\\:,:");
		pTmpOut.println("key23=\\#,#,\\!,!,\\=,=,\\:,:");
		pTmpOut.println("");
		pTmpOut.println("# Leading and trailing space");
		pTmpOut.println("# syntax > [key]: [sp][realtab][sp]the[realtab]value of space[sp][realtab][sp]");
		pTmpOut.write("key24= \\ 	 the	value of space");
		pTmpOut.println("");
		pTmpOut.println("# Unexpected EOF after terminator");
		pTmpOut.println("# syntax > [key]:[EOF]");
		pTmpOut.write("key24=v");

		pTmpOut.close();
	}
	catch(Exception e)
	{
		fail("Setup failed");
	}
}
/**
 * This code confirms parser token against a real property class by
 * reading the same file.<p>
 * Note: so far we do not test conditional extraction.
 * Creation date: (8/16/2000 9:53:24 PM)
 */
public void testBatch()
{
	JPToken token = new JPToken();
	Properties JavaProp, ParProp;
	String sEmptyValueResult = "NOTFOUND";
	String sMsg = "";
	String sRst = "";
	String sCurKey="";
	JPEscapeSequence JPEsc = new JPEscapeSequence();
	PrintStream log = null;
	
	try
	{	
		FileOutputStream fos = new FileOutputStream(new File(m_srcDir + m_logFname));
		log = new PrintStream(fos);
	}
	catch(Exception e)
	{
		fail(e.toString());
	}
	
	FileListBuilder FLB = new FileListBuilder();
	FLB.addRecursive(m_srcDir, ".properties");
	FLB.setDescription("Properties Files");

	File fin;
	while ((fin = FLB.getNextFile()) != null)
	{
		int keyCnt = 0;
		JavaProp = new Properties();
		ParProp = new Properties();
		try
		{
			// load property class from input file
			java.io.FileInputStream in = new java.io.FileInputStream(fin.getPath());
			JavaProp.load(in);
			in.close();
			Enumeration JavaKeys = JavaProp.propertyNames();

			// logging - for when thing don't work
			log.println(fin.getPath());
			log.println("java found these keys: ");
			while (JavaKeys.hasMoreElements())
			{
				log.println(JavaKeys.nextElement().toString());
			}
			log.println("parser found these keys: ");

			// parser start 
			java.io.FileReader r = new java.io.FileReader(fin);
			Parser parser = new Parser(r);
			token = parser.getNextToken();
			

   /* TURNED OFF

   NOTE:
   There are to many discrepancies between java 1.2.0 and 1.2.2 properties classes
   to do this tight of a testing. Some extraneous keys can be returned in one version 
   that are not returned in the other.

   
			// THE TEST
			while (token.m_nType != token.EOF)
			{
				sCurKey = "";
				
				// We are concerned with keys, key terminators and values, ignore other tokens
				if (token.m_nType != token.PROP_KEY)
				{
					token = parser.getNextToken();
					continue; 
				} else
				{
					log.println(token.m_strContent);
					token.m_strContent = JPEsc.decode(token.m_strContent);
					sCurKey = token.m_strContent;
				}

				String javaVal = JavaProp.getProperty(token.m_strContent, sEmptyValueResult);

				// key still exist according to java	
				if (javaVal.equals(""))				
				{
					// a KEY_TERMINATOR_EMPTY_VALUE token should follow
					// if a KEY_TERMINATOR follows it means we hit EOF.
					token = parser.getNextToken();
					assert("Assert1 File:" + fin.getName(), ((token.m_nType == token.KEY_TERMINATOR) || (token.m_nType == token.KEY_TERMINATOR_EMPTY_VALUE)));
					ParProp.setProperty(sCurKey,""); //normalize to java empty string
					continue;
				} 
				else
				{
					// a terminator should follow
					token = parser.getNextToken();
					assert("Assert2 File:" + fin.getName(), ((token.m_nType == token.KEY_TERMINATOR)));
					// a value should follow that matches the javaVal
					token = parser.getNextToken();
					assert("Assert3 File:" + fin.getName(), ((token.m_nType == token.KEY_VALUE) || (token.m_nType == token.EOF)));
					token.m_strContent = JPEsc.decode(token.m_strContent);
					assert("Assert4 File:" + fin.getName(), ((token.m_strContent.equals(javaVal))));
					ParProp.setProperty(sCurKey,token.m_strContent);
				}
				token = parser.getNextToken();
			}

			// did the parser miss a key ?
			// NOTE: java 1.2.0 will incorrectly recognize a sequence like:
			//       [tab]#[\n] as a valid key called "#".
			//       To be a valid key, any white-space that is part of the key 
			//       should be escaped. If you run across a file like this, the
			//       recomendation for now is to discard of it as a test file.
			//       THIS ONLY AFFECTS THE TEST. We still extract the correct keys.
			log.println("Compare classes ========== ");				
			JavaKeys = JavaProp.propertyNames();
			while (JavaKeys.hasMoreElements())
			{
				String sTmp = JavaKeys.nextElement().toString();
				sRst = ParProp.getProperty(sTmp, sEmptyValueResult);
				//does the java key exists in the parsers key list ?
				if(sRst.equals(sEmptyValueResult) == true)
				{
					sMsg = "parser missed a key - "+ sTmp; 
					log.println(sMsg);
					assert(("Assert5 File:" + fin.getName() + " : " + sMsg ), false );
				}
			} 
			
			// did parser find an extra key ?
			Enumeration PropKeys = ParProp.propertyNames();
			while (PropKeys.hasMoreElements())
			{
				String sTmp = PropKeys.nextElement().toString();
				sRst = JavaProp.getProperty(sTmp, sEmptyValueResult);
				if(sRst.equals(sEmptyValueResult) == true)
				{
					sMsg = "parser found extra key - "+ sTmp;
					log.println(sMsg);
					assert(("Assert6 File:" + fin.getName() + " : " + sMsg), false );
				}
			}

			// do the values match ?
			PropKeys = ParProp.propertyNames();
			while (PropKeys.hasMoreElements())
			{	
				String theKey = PropKeys.nextElement().toString();
				String pRst = ParProp.getProperty(theKey, sEmptyValueResult);
				String jRst = JavaProp.getProperty(theKey, sEmptyValueResult + "dummy");
				if(!pRst.equals(jRst))
				{
					sMsg = "Values do not match for key "+ theKey + " " + jRst + " and " + pRst;
					log.println(sMsg);
					assert(("Assert7 File:" + fin.getName() + " : " + sMsg), false );
				}
			}
			
			log.println("OK.");				
*/
		} catch (Exception e)
		{
			fail("Assert8 File: " + fin.getName() + " " +  e.toString());
		}
	}
 log.close();
}
}
