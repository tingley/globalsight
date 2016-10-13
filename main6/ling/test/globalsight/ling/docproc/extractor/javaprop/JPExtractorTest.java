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


import com.globalsight.ling.docproc.extractor.javaprop.Parser;
import com.globalsight.ling.docproc.extractor.javaprop.JPToken;
import com.globalsight.ling.docproc.AbstractExtractor;
import com.globalsight.ling.docproc.ExtractorException;
import com.globalsight.ling.docproc.extractor.javaprop.Extractor;
import com.globalsight.ling.docproc.EFInputData;
import com.globalsight.ling.docproc.Output;
import java.io.File;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import test.FileListBuilder;

import com.globalsight.ling.docproc.DiplomatWriter;import java.util.Locale;/**
 * Extractor class test case
 */
public class JPExtractorTest extends TestCase
{

	private static java.lang.String m_srcDir;
	private java.lang.String m_TmpPropFileName;
/**
 * JPExtractorTest constructor comment.
 */
public JPExtractorTest(String p_Name) {
	super(p_Name);
}

/**
 * Insert the method's description here.
 * Creation date: (8/16/2000 2:47:41 PM)
 */
public void setUp() 
{
	m_srcDir = "testclasses/test/globalsight/ling/docproc/extractor/javaprop/TestFiles";
	m_TmpPropFileName = "jp_self_generating.properties";
	
}
/**
 * Insert the method's description here.
 * Creation date: (8/17/2000 5:12:16 PM)
 */
public void testBatch()
{

	java.io.FileReader r;
	JPToken token = new JPToken();
	Output output;
	
	FileListBuilder FLB = new FileListBuilder();
	FLB.addRecursive(m_srcDir, ".properties");
	FLB.setDescription("Properties Files");

	// Create an Input object - normally done by the caller
	EFInputData input = new EFInputData();
	input.setCodeset("8859_1");
	java.util.Locale locale = new java.util.Locale("en", "US");
	input.setLocale(locale);
	
	File fin; 
 	while( (fin= FLB.getNextFile()) != null )
 	{
		input.setURL("file:///" + fin.getPath());

		try
		{
			// Create an output object  - normally done by the caller
			output = new Output();

			// Create a new Extractor wrapper ( Normally done by the caller )
			AbstractExtractor extractor = new
			com.globalsight.ling.docproc.extractor.javaprop.Extractor();
			extractor.init(input, output);
			extractor.loadRules();
			extractor.extract();		
		}
		catch(Exception e)
		{
		  fail("File: " + fin.getName() + " " + e.toString());
		}
 	}

}
/**
 * Checks for exceptions from extractor
 * TODO: file compare or roundtrip compare (input agianst merged file)
 * Creation date: (8/16/2000 2:52:16 PM)
 */
public void testPCRegTestFile()
{

	Output output;

	// Create an Input object - normally done by the caller
	EFInputData input = new EFInputData();
	input.setCodeset("8859_1");
	java.util.Locale locale = new java.util.Locale("en", "US");
	input.setLocale(locale);

	input.setURL("file:///" + m_srcDir + m_TmpPropFileName);

// to do one - place in m_srcDir dir - and change name here
	//input.setURL("file:///" + m_srcDir + "messages_en.properties");


	try
	{
		// Create an output object  - normally done by the caller
		output = new Output();

		// Create a new Extractor wrapper ( Normally done by the caller )
		AbstractExtractor extractor = new
		com.globalsight.ling.docproc.extractor.javaprop.Extractor();
		extractor.init(input, output);
		extractor.loadRules();
		extractor.extract();
		
		// Print the diplomat file
		DiplomatWriter dw = new DiplomatWriter();
		String result = dw.WriteXML(output);

		// print diplomat to console
		// System.out.println( result );
		
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
 * Parsers the input file and returns a complete Diplomat file as a String.
 * @param p_strFilePath - the full input file path.
 * @exception com.globalsight.ling.docproc.ExtractorException
 */
public static String convertFile2DiplomatString(String p_inPath) throws ExtractorException
{
	// Read in HTML file and create Input object
	EFInputData input = new EFInputData();
	input.setCodeset("8859_1");
	Locale locale = new Locale("en", "US");
	input.setLocale(locale);
	input.setURL("file:///" + p_inPath);
	
	// Extraction
	Output output = new Output();
	Extractor extractor = new Extractor();
	extractor.init(input, output);
	extractor.loadRules();
	extractor.extract(); 

	return(DiplomatWriter.WriteXML(output)); 
}}
