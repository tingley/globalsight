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
import com.globalsight.ling.docproc.AbstractExtractor;
import com.globalsight.ling.docproc.ExtractorException;
import com.globalsight.ling.docproc.EFInputData;
import com.globalsight.ling.docproc.extractor.plaintext.Extractor;
import com.globalsight.ling.docproc.Output;

import java.io.File;
import junit.extensions.*;
import junit.framework.*;

import test.FileListBuilder;

/**
 * Tests the Plain Text extractor.
 */
public class PTExtractorTest extends TestCase {
	private java.lang.String m_srcDir;
/**
 * Constructor required by Junit.
 */
public PTExtractorTest(String p_sName) {
	super(p_sName);
}

/**
 * Initializes the test case.
 */
public void setUp() 
{
	m_srcDir = "testclasses/test/globalsight/ling/docproc/extractor/plaintext/TestFiles";

}
/**
 * Runs the extractor on a static unicode string.
 * Reports high level exceptions.
 */
public void test1() 
{

	Output output;

	// Create an Input object - normally done by the caller
	EFInputData input = new EFInputData();
	input.setCodeset("8859_1");
	java.util.Locale locale = new java.util.Locale("en", "US");
	input.setLocale(locale);
	
	//input.setUnicodeInput("\t\n\tfirst\nsecond\u00a0third\t\nfourth\t\n\nfifth\n\nsixth\n");
	input.setURL("file:///" + m_srcDir + "constitution.txt");
	
	try
	{
		// Create an output object  - normally done by the caller
		output = new Output();
		
		// Create a new Extractor wrapper ( Normally done by the caller )
		AbstractExtractor extractor = new	com.globalsight.ling.docproc.extractor.plaintext.Extractor();
		Extractor PlainText = (Extractor) extractor;
		PlainText.init(input, output);
		PlainText.loadRules();
		PlainText.s_breakOnSingleCR = false;
		PlainText.s_keepEmbeddedCR = true;
		PlainText.extract();
		
	
		// Create the diplomat file
		com.globalsight.ling.docproc.DiplomatWriter dw = new com.globalsight.ling.docproc.DiplomatWriter();
		String result = dw.WriteXML(output);
		//System.out.println(result);
		
		try
		{
			// print diplomat to file  
			java.io.OutputStreamWriter osw = new java.io.OutputStreamWriter ( new java.io.FileOutputStream(m_srcDir + "diplomat.xml"));
			osw.write(result);	
			osw.close(); 
		}
		catch(Exception e)
		{
			fail(e.toString());
		}
	}
	catch(ExtractorException e )
	{
		fail(e.toString());
	}
}
/**
 * Runs the extractor on multiple files in the test directory.
 * Reports high level exceptions.
 */
public void testBatch()
{

	Output output;

	FileListBuilder FLB = new FileListBuilder();
	FLB.addRecursive(m_srcDir, ".txt");
	FLB.setDescription("Text Files");

	// Create an Input object - normally done by the caller
	EFInputData input = new EFInputData();
	input.setCodeset("8859_1");
	java.util.Locale locale = new java.util.Locale("en", "US");
	input.setLocale(locale);
	

	File fin; 
 	while( (fin= FLB.getNextFile()) != null )
 	{
		//System.out.println(fin.getName());
		
		input.setURL("file:///" + fin.getPath());

		try
		{
			// Create an output object  - normally done by the caller
			output = new Output();
			
			// Create a new Extractor wrapper ( Normally done by the caller )
			AbstractExtractor extractor = new	com.globalsight.ling.docproc.extractor.plaintext.Extractor();
			extractor.init(input, output);
			extractor.loadRules();
			extractor.extract();
		}
		catch(ExtractorException e )
		{
			fail(e.toString());
		}
 	}
}
}
