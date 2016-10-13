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
package test.globalsight.ling.docproc.extractor.html;

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

/**  
 * Insert the type's description here.
 * 
 * Creation date: (8/15/2000 6:33:52 PM)
 * @author: Jim Hargrave
 */
import com.globalsight.ling.docproc.AbstractExtractor;
import com.globalsight.ling.docproc.EFInputData;
import com.globalsight.ling.docproc.ExtractorException;
import com.globalsight.ling.docproc.Output;
import com.globalsight.ling.docproc.DiplomatWriter;

import test.FileListBuilder;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

import java.io.File;

public class ExtractorTest extends TestCase
{
/**
 * ExtractorTest constructor comment.
 * @param name java.lang.String
 */
public ExtractorTest(String name)
{
	super(name);
}
/**
 * Insert the method's description here.
 * @author: Jim Hargrave
 * Creation date: (8/15/2000 6:40:01 PM)
 * @return junit.framework.Test
 */
public static Test suite()
{
	return new TestSuite(ExtractorTest.class);
}
/**
 * Insert the method's description here.
 * @author: Jim Hargrave
 * Creation date: (8/16/2000 7:49:01 PM)
 */
public void testBatch()
{
	FileListBuilder ff = new FileListBuilder();
	ff.add("D:/work/ling/test/HtmlExtractor/testFiles", ".html");
	ff.add("D:/work/ling/test/HtmlExtractor/testFiles", ".htm");
	ff.setDescription("HTML");
	File aFile = null;
	
	ExtractorException ex = null;
	
	while ((aFile = ff.getNextFile()) != null)
	{
		// Read in HTML file and create Input object
		EFInputData input = new EFInputData();
		input.setCodeset("8859_1");
		java.util.Locale locale = new java.util.Locale("en", "US");
		input.setLocale(locale);
		input.setURL("file:///" + aFile.getPath());
		Output output = new Output();
		try
		{
			AbstractExtractor extractor = new com.globalsight.ling.docproc.extractor.html.Extractor();
			extractor.init(input, output);
			extractor.loadRules();
			extractor.extract();
			System.out.println();
			System.out.println();
			System.out.println(aFile.getPath());
			System.out.print(DiplomatWriter.WriteXML(output));
		}
		catch (ExtractorException e)
		{
			ex = e;
		}
	}
	assertNull(ex);
}
}
