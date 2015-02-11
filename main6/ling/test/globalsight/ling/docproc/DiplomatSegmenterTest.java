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
import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import com.globalsight.ling.docproc.DiplomatSegmenterException;
import com.globalsight.ling.docproc.DiplomatSegmenter;

public class DiplomatSegmenterTest
extends TestCase
{
    private String m_diplomatWithOneSegmentIn = null;
    private String m_diplomatWithOneSegmentOut = null;
    private String m_diplomatWithoutSegmentTags1 = null;	
    private String m_diplomatWithoutSegmentTags2 = null;	
    private String m_diplomatWithoutSegmentTags3 = null;	
    private String m_diplomatWithoutSegmentTags4 = null;	
    private String m_diplomatWithSegmentTags1 = null;	
    private String m_diplomatWithSegmentTags2 = null;	
    private String m_diplomatWithSegmentTags3 = null;	
    private String m_diplomatWithSegmentTags4 = null;

    /**
     */
    public DiplomatSegmenterTest(String p_name)
    {
        super(p_name);
    }

    /**
     * Insert the method's description here.
     * Creation date: (8/15/2000 5:32:33 PM)
     * @param args java.lang.String[]
     */
    public static void main(String[] args)
    {
        //String[] myargs = {DiplomatSegmenterTest.class.getName()};
        //junit.swingui.TestRunner.main(myargs);
         TestRunner.run(suite());
    }

    /**
     * Insert the method's description here.
     * Creation date: (8/16/2000 2:47:41 PM)
     */
    public void setUp()
    {
        m_diplomatWithOneSegmentIn =
            "<diplomat version=\"1.0\" locale=\"en_US\" datatype=\"html\">"
                + "<skeleton>skeleton</skeleton>"
                + "<translatable blockId=\"1\">"
                + "A translatable1 segment"
                + "</translatable>"
                + "<localizable blockId=\"2\">localizable</localizable>"
                + "</diplomat>";

        m_diplomatWithOneSegmentOut =
            "<?xml version=\"1.0\"?>\n\n"
                + "<diplomat version=\"1.0\" datatype=\"html\" locale=\"en_US\">\n"
                + "  <skeleton>skeleton</skeleton>\n"
                + "  <translatable blockId=\"1\">\n"
                + "    <segment segmentId=\"1\">A translatable1 segment</segment>\n"
                + "  </translatable>\n"
                + "  <localizable blockId=\"2\">localizable</localizable>\n"
                + "</diplomat>";

        m_diplomatWithoutSegmentTags1 =
            "<diplomat version=\"1.0\" locale=\"en_US\" datatype=\"html\">"
                + "<skeleton>skeleton</skeleton>"
                + "<translatable blockId=\"1\">"
                + "A translatable1 segment. "
                + "A translatable2 segment."
                + "A translatable3 segment"
                + "</translatable>"
                + "<localizable blockId=\"2\">localizable</localizable>"
                + "</diplomat>";

        m_diplomatWithSegmentTags1 =
            "<?xml version=\"1.0\"?>\n\n"
                + "<diplomat version=\"1.0\" datatype=\"html\" locale=\"en_US\">\n"
                + "  <skeleton>skeleton</skeleton>\n"
                + "  <translatable blockId=\"1\">\n"
                + "    <segment segmentId=\"1\">A translatable1 segment. </segment>\n"
                + "    <segment segmentId=\"2\">A translatable2 segment. </segment>\n"
                + "    <segment segmentId=\"3\">A translatable3 segment</segment>\n"
                + "  </translatable>\n"
                + "  <localizable blockId=\"2\">localizable</localizable>\n"
                + "</diplomat>";

        m_diplomatWithoutSegmentTags2 =
            "<diplomat version=\"1.0\" locale=\"en_US\" datatype=\"html\">"
                + "<skeleton>skeleton</skeleton>"
                + "<translatable blockId=\"1\">"
                + "<bpt type=\"bold\" i=\"1\">&lt;b&gt;</bpt>A<ept i=\"1\">&lt;/b&gt;</ept> translatable1 segment. "
                + "A translatable2 segment. "
                + "A translatable3 segment"
                + "</translatable>"
                + "<localizable blockId=\"2\">localizable</localizable>"
                + "</diplomat>";

        m_diplomatWithSegmentTags2 =
            "<?xml version=\"1.0\"?>\n\n"
                + "<diplomat version=\"1.0\" datatype=\"html\" locale=\"en_US\">\n"
                + "  <skeleton>skeleton</skeleton>\n"
                + "  <translatable blockId=\"1\">\n"
                + "    <segment segmentId=\"1\"><bpt type=\"bold\" i=\"1\">&lt;b&gt;</bpt>A<ept i=\"1\">&lt;/b&gt;</ept> translatable1 segment. </segment>\n"
                + "    <segment segmentId=\"2\">A translatable2 segment. </segment>\n"
                + "    <segment segmentId=\"3\">A translatable3 segment</segment>\n"
                + "  </translatable>\n"
                + "  <localizable blockId=\"2\">localizable</localizable>\n"
                + "</diplomat>";

        m_diplomatWithoutSegmentTags3 =
            "<diplomat version=\"1.0\" locale=\"en_US\" datatype=\"html\">"
                + "<skeleton>skeleton</skeleton>"
                + "<translatable blockId=\"1\">"
                + "<bpt type=\"bold\" i=\"1\">&lt;b&gt;</bpt>A translatable1 segment. "
                + "A translatable2 segment. "
                + "A translatable3 segment<ept i=\"1\">&lt;/b&gt;</ept>"
                + "</translatable>"
                + "<localizable blockId=\"2\">localizable</localizable>"
                + "</diplomat>";

        m_diplomatWithSegmentTags3 =
            "<?xml version=\"1.0\"?>\n\n"
                + "<diplomat version=\"1.0\" datatype=\"html\" locale=\"en_US\">\n"
                + "  <skeleton>skeleton</skeleton>\n"
                + "  <translatable blockId=\"1\">\n"
                + "    <segment segmentId=\"1\"><it type=\"bold\" pos=\"begin\" x=\"1\">&lt;b&gt;</it>A translatable1 segment. </segment>\n"
                + "    <segment segmentId=\"2\">A translatable2 segment. </segment>\n"
                + "    <segment segmentId=\"3\">A translatable3 segment<it pos=\"end\" x=\"1\">&lt;/b&gt;</it></segment>\n"
                + "  </translatable>\n"
                + "  <localizable blockId=\"2\">localizable</localizable>\n"
                + "</diplomat>";

        m_diplomatWithoutSegmentTags4 =
            "<diplomat version=\"1.0\" locale=\"en_US\" datatype=\"html\">"
                + "<skeleton>skeleton</skeleton>"
                + "<translatable blockId=\"1\">"
                + "<it type=\"bold\" pos=\"begin\" x=\"1\">&lt;b&gt;</it>A translatable1 segment. "
                + "A translatable2 segment. "
                + "A translatable3 segment<it pos=\"end\" x=\"1\">&lt;/b&gt;</it>"
                + "</translatable>"
                + "<localizable blockId=\"2\">localizable</localizable>"
                + "</diplomat>";

        m_diplomatWithSegmentTags4 =
            "<?xml version=\"1.0\"?>\n\n"
                + "<diplomat version=\"1.0\" datatype=\"html\" locale=\"en_US\">\n"
                + "  <skeleton>skeleton</skeleton>\n"
                + "  <translatable blockId=\"1\">\n"
                + "    <segment segmentId=\"1\"><it pos=\"begin\" x=\"1\" type=\"bold\">&lt;b&gt;</it>A translatable1 segment. </segment>\n"
                + "    <segment segmentId=\"2\">A translatable2 segment. </segment>\n"
                + "    <segment segmentId=\"3\">A translatable3 segment<it pos=\"end\" x=\"1\">&lt;/b&gt;</it></segment>\n"
                + "  </translatable>\n"
                + "  <localizable blockId=\"2\">localizable</localizable>\n"
                + "</diplomat>";
    }

    /**
     * Insert the method's description here.
     * Creation date: (8/16/2000 10:40:43 AM)
     */
    public static Test suite()
    {
        return new TestSuite(DiplomatSegmenterTest.class);
    }

    /**
     * Simple segmenter test (no internal Diplomat tags)
     *
     */
    public void test1()
    {

        DiplomatSegmenter diplomatSegmenter = new DiplomatSegmenter();
        String withSegments = null;

        Exception ex = null;
        try
        {
            withSegments = diplomatSegmenter.segment(m_diplomatWithoutSegmentTags1);
        }
        catch (DiplomatSegmenterException e)
        {
            ex = e;
        }
        assertNull(ex);

        assertEquals(withSegments.compareTo(m_diplomatWithSegmentTags1), 0);
    }

    /**
    * Simple segmenter test with internal Diplomat tags
    *
    */
    public void test2()
    {

        DiplomatSegmenter diplomatSegmenter = new DiplomatSegmenter();
        String withSegments = null;

        Exception ex = null;
        try
        {
            withSegments = diplomatSegmenter.segment(m_diplomatWithoutSegmentTags2);
        }
        catch (DiplomatSegmenterException e)
        {
            ex = e;
        }
        assertNull(ex);

        assertEquals(withSegments.compareTo(m_diplomatWithSegmentTags2), 0);
    }

    /**
    * Simple it tag test.
    *
    */
    public void test3()
    {

        DiplomatSegmenter diplomatSegmenter = new DiplomatSegmenter();
        String withSegments = null;

        Exception ex = null;
        try
        {
            withSegments = diplomatSegmenter.segment(m_diplomatWithoutSegmentTags3);
        }
        catch (DiplomatSegmenterException e)
        {
            ex = e;
        }
        assertNull(ex);

        assertEquals(withSegments.compareTo(m_diplomatWithSegmentTags3), 0);
    }

    /**
    * Simple test with existing it tags in input.
    *
    */
    public void test4()
    {

        DiplomatSegmenter diplomatSegmenter = new DiplomatSegmenter();
        String withSegments = null;

        Exception ex = null;
        try
        {
            withSegments = diplomatSegmenter.segment(m_diplomatWithoutSegmentTags4);
        }
        catch (DiplomatSegmenterException e)
        {
            ex = e;
        }
        assertNull(ex);

        assertEquals(withSegments.compareTo(m_diplomatWithSegmentTags4), 0);
    }

    /**
    * Simple test with existing it tags in input.
    *
    */
    public void test5()
    {

        DiplomatSegmenter diplomatSegmenter = new DiplomatSegmenter();
        String withSegments = null;

        Exception ex = null;
        try
        {
            withSegments = diplomatSegmenter.segment(m_diplomatWithOneSegmentIn);
        }
        catch (DiplomatSegmenterException e)
        {
            ex = e;
        }
        assertNull(ex);

        assertEquals(withSegments.compareTo(m_diplomatWithOneSegmentOut), 0);
    }}
