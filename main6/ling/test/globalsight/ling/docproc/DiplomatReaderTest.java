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
package test.globalsight.ling.docproc;

/*
Copyright (c) 2000 GlobalSight Corporation. All rights reserved.

THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF 
GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.

THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
BY LAW.
*/

// Imports
import java.util.Iterator;

import com.globalsight.ling.docproc.DocumentElement;
import com.globalsight.ling.docproc.TranslatableElement;
import com.globalsight.ling.docproc.DiplomatReader;
import com.globalsight.ling.docproc.DiplomatAttribute;
import com.globalsight.ling.docproc.DiplomatWriter;
import com.globalsight.ling.docproc.DiplomatReaderException;
import com.globalsight.ling.docproc.Output;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
*/
public class DiplomatReaderTest extends TestCase
{
	private String m_simpleDiplomat = null;
	private int SIMPLE_DIPLOMAT_COUNT;
	private int[] m_simpleDiplomatElements =
	{
		DocumentElement.SKELETON,
		DocumentElement.TRANSLATABLE,
		DocumentElement.LOCALIZABLE
	};
	
	private String m_diplomatWithGsa = null;
	private int DIPLOMAT_WITH_GSA_COUNT;
	private int[] m_diplomatWithGsaElements =
	{
		DocumentElement.SKELETON,
		DocumentElement.GSA_START,
		DocumentElement.TRANSLATABLE,
		DocumentElement.GSA_END,
		DocumentElement.LOCALIZABLE
	};
	
	private String m_diplomatWithSegments = null;
	private int SEGMENT_COUNT;

	private String m_diplomatWithSubflow = null;
/**
 * JPExtractorTest constructor comment.
 */
public DiplomatReaderTest(String p_Name)
{
	super(p_Name);
}
/**
 * Insert the method's description here.
 * 
 * @return boolean
 * @param p_output com.globalsight.ling.docproc.Output
 */
private boolean checkElementCount(Output p_output, int p_count)
{
	Iterator it = p_output.documentElementIterator();
	DocumentElement de = null;

	assertNotNull(p_output);

	int nodeCount = 0;
	while(it.hasNext())
	{
		nodeCount++;
		it.next();
	}
	
	return (nodeCount == p_count);
}
/**
 * Insert the method's description here.
 * 
 * @return boolean
 * @param p_output com.globalsight.ling.docproc.Output
 */
private boolean checkElementTypes(Output p_output, int[] p_elements)
{
	Iterator it = p_output.documentElementIterator();
	DocumentElement de = null;
	boolean success = true;

	assertNotNull(p_output);

	int nodeCount = 0;
	while(it.hasNext())
	{
		de = (DocumentElement) it.next();
		if (de.type() != p_elements[nodeCount])
		{
			success = false;
			break;
		}
		nodeCount++;
		
	}
	
	return success;
}
/**
 * Insert the method's description here.
 * 
 * @return boolean
 * @param p_output com.globalsight.ling.docproc.Output
 */
private boolean checkSegmentCount(Output p_output, int p_count)
{
	Iterator it = p_output.documentElementIterator();
	DocumentElement de = null;

	assertNotNull(p_output);

	int segmentCount = 0;
	while(it.hasNext())
	{
		de = (DocumentElement) it.next();
		if (de.type() == DocumentElement.TRANSLATABLE)
		{
			Iterator segs = ((TranslatableElement)de).getSegments().iterator();
			while (segs.hasNext())
			{
				segs.next();
				segmentCount++;
			}
			
		}
	}
	
	return (segmentCount == p_count);
}

/**
 * Insert the method's description here.
 */
public void setUp() 
{
	m_simpleDiplomat =
	"<diplomat version=\"1.0\" locale=\"en_US\" datatype=\"plaintext\">" +
	"<skeleton>skeleton</skeleton>" +
	"<translatable blockId=\"1\">translatable</translatable>" +
	"<localizable blockId=\"2\">localizable</localizable>" +
	"</diplomat>";
	SIMPLE_DIPLOMAT_COUNT = 3;
	

	m_diplomatWithGsa =
	"<diplomat version=\"1.0\" locale=\"en_US\" datatype=\"plaintext\">" +
	"<skeleton>skeleton</skeleton>" +
	"<gsa>" +
	"<translatable blockId=\"1\">translatable</translatable>" +
	"</gsa>" +
	"<localizable blockId=\"2\">localizable</localizable>" +
	"</diplomat>";
	DIPLOMAT_WITH_GSA_COUNT = 5;

	m_diplomatWithSegments =
	"<diplomat version=\"1.0\" locale=\"en_US\" datatype=\"plaintext\">" +
	"<skeleton>skeleton</skeleton>" +
	"<translatable blockId=\"1\">" +
	"<segment segmentId=\"1\">translatable1</segment>" +
	"<segment segmentId=\"2\">translatable2</segment>" +
	"<segment segmentId=\"3\">translatable3</segment>" +
	"</translatable>" +
	"<localizable blockId=\"2\">localizable</localizable>" +
	"</diplomat>";
	SEGMENT_COUNT = 3;

	m_diplomatWithSubflow =
	"<diplomat version=\"1.0\" locale=\"en_US\" datatype=\"html\">" +
	"<skeleton>skeleton" +
	"</skeleton>" +
	"<translatable blockId=\"2\">translatable</translatable>" +
	"<localizable blockId=\"1\">" +
	"<bpt>&lt;img alt=&quot;<sub>subflow text</sub>&quot;&gt;</bpt>" +
	"</localizable>" +
	"</diplomat>";
}
/**
 * Insert the method's description here.
 * Creation date: (8/16/2000 10:40:43 AM)
 */
public static Test suite()
{ 
	return new TestSuite(DiplomatReaderTest.class);
}
/**
 * Insert the method's description here.
 * 
 */
public void testDiplomatWithGsa()
{
	DiplomatReader diplomatReader = new DiplomatReader(m_diplomatWithGsa);
	
	Exception ex = null;
	Output out = null;
	
	try
	{
		out = diplomatReader.getOutput();
	}
	catch (DiplomatReaderException e)
	{
		ex = e;
	}
	assertNull(ex);

	//
	System.out.print(DiplomatWriter.WriteXML(out));

	// check node count
	assertTrue(checkElementCount(out, DIPLOMAT_WITH_GSA_COUNT));

	// check node types
	assertTrue(checkElementTypes(out, m_diplomatWithGsaElements));
}
/**
 * Insert the method's description here.
 * 
 */
public void testDiplomatWithSegments()
{
	DiplomatReader diplomatReader = new DiplomatReader(m_diplomatWithSegments);
	
	Exception ex = null;
	Output out = null;
	try
	{
		out = diplomatReader.getOutput();
	}
	catch (DiplomatReaderException e)
	{
		ex = e;
	}
	assertNull(ex);

	//
	System.out.print(DiplomatWriter.WriteXML(out));

	// check node count
	assertTrue(checkElementCount(out, SIMPLE_DIPLOMAT_COUNT));

	// check node types
	assertTrue(checkElementTypes(out, m_simpleDiplomatElements));

	// check segment count
	assertTrue(checkSegmentCount(out, SEGMENT_COUNT));
}
/**
 * Insert the method's description here.
 * 
 */
public void testDiplomatWithSubflow()
{
	DiplomatReader diplomatReader = new DiplomatReader(m_diplomatWithSubflow);
	
	Exception ex = null;
	Output out = null;
	try
	{
		out = diplomatReader.getOutput();
	}
	catch (DiplomatReaderException e)
	{
		ex = e;
	}
	assertNull(ex);

	//
	System.out.print(DiplomatWriter.WriteXML(out));
	
	// check node count
	assertTrue(checkElementCount(out, SIMPLE_DIPLOMAT_COUNT));

	// check node types
	assertTrue(checkElementTypes(out, m_simpleDiplomatElements));
}
/**
 * Insert the method's description here.
 * 
 */
public void testSimpleDiplomat()
{
	DiplomatReader diplomatReader = new DiplomatReader(m_simpleDiplomat);
	
	Exception ex = null;
	Output out = null;
	
	try
	{
		out = diplomatReader.getOutput();
	}
	catch (DiplomatReaderException e)
	{
		ex = e;
	}
	assertNull(ex);

	//
	System.out.print(DiplomatWriter.WriteXML(out));

	// check node count
	assertTrue(checkElementCount(out, SIMPLE_DIPLOMAT_COUNT));

	// check node types
	assertTrue(checkElementTypes(out, m_simpleDiplomatElements));
}
}
