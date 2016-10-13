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

// Imports
import java.util.Locale;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.io.IOException;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

import com.globalsight.ling.docproc.DiplomatMerger;
import com.globalsight.ling.docproc.DiplomatMergerException;
import com.globalsight.ling.docproc.L10nContent;

import com.globalsight.ling.docproc.DiplomatWriter;
import com.globalsight.ling.docproc.EFInputData;
import com.globalsight.ling.docproc.DiplomatAttribute;
import com.globalsight.ling.docproc.extractor.html.Extractor;
import com.globalsight.ling.docproc.Output;
import com.globalsight.ling.docproc.ExtractorException;

import test.Arguments;
import test.FileListBuilder;

public class DiplomatMergerTest
extends TestCase
{
    private String m_simpleMerge = null;
    private String m_simpleMergedContent = null;
    private String m_simpleGsaMergedContent = null;
    private String m_simpleMergeWithEncode = null;
    private String m_simpleMergedWithEncodeContent = null;
    private String m_simpleMergeWithSubflow = null;
    private String m_simpleMergedWithSubflowContent = null;
    private String m_diplomatWithSegments = null;
    private String m_diplomatWithSegmentsContent = null;
    private String m_testShigemichi = null;
    private String m_javaScript = null;

    /**
     */
    public DiplomatMergerTest(String p_name)
    {
        super(p_name);
    }

        /**
         * Insert the method's description here.
         * Creation date: (8/16/2000 2:47:41 PM)
         */
        public void setUp()
        {
            m_simpleMerge =
            "<diplomat version=\"1.0\" locale=\"en_US\" datatype=\"html\">" +
            "<skeleton>skeleton&lt;tag&gt;</skeleton>" +
            "<gsa>" +
            "<translatable blockId=\"1\">&lt;&amp;&gt;&quot;translatable</translatable>" +
            "</gsa>" +
            "<localizable blockId=\"2\">localizable</localizable>" +
            "</diplomat>";

            m_simpleMergedContent ="skeleton<tag>&lt;&amp;&gt;&quot;translatablelocalizable";

            m_simpleGsaMergedContent ="skeleton<tag>\n<gsa>\n&lt;&amp;&gt;&quot;translatable\n</gsa>\nlocalizable";

            m_simpleMergeWithEncode =
            "<diplomat version=\"1.0\" locale=\"en_US\" datatype=\"html\">" +
            "<skeleton>skeleton</skeleton>" +
            "<translatable blockId=\"1\">&gt;©\u2122®translatable</translatable>" +
            "<localizable blockId=\"2\">localizable</localizable>" +
            "</diplomat>";
            m_simpleMergedWithEncodeContent ="skeleton&gt;&copy;&trade;&reg;translatablelocalizable";

            m_simpleMergeWithSubflow =
            "<diplomat version=\"1.0\" locale=\"en_US\" datatype=\"html\">" +
            "<skeleton>skeleton</skeleton>" +
            "<translatable blockId=\"1\">" +
            "<bpt>&lt;img alt=&quot;<sub>subflow text</sub>&quot;&gt;</bpt>translatable" +
            //"<segment><bpt>&lt;img alt=&quot;<sub>subflow text</sub>&quot;&gt;</bpt>translatable</segment>" +
            "</translatable>" +
            "<localizable blockId=\"2\">localizable</localizable>" +
            "</diplomat>";

            m_simpleMergedWithSubflowContent ="skeleton<img alt=\"subflow text\">translatablelocalizable";

            m_diplomatWithSegments =
            "<diplomat version=\"1.0\" locale=\"en_US\" datatype=\"html\">" +
            "<skeleton>skeleton</skeleton>" +
            "<translatable blockId=\"1\">" +
            "<segment segmentId=\"1\">translatable1</segment>" +
            "<segment segmentId=\"2\">translatable2</segment>" +
            "<segment segmentId=\"3\">translatable3</segment>" +
            "</translatable>" +
            "<localizable blockId=\"2\">localizable</localizable>" +
            "</diplomat>";

            m_diplomatWithSegmentsContent ="skeletontranslatable1translatable2translatable3localizable";

            m_simpleRoundTripHtml =
            "<html>\n" +
            "<img src=\"translate me\" href=\"www.globalsight.com\"></img>\n" +
            "<p>\n" +
            "Some text\n" +
            "</p>\n" +
            "<p>\n" +
            "Some more text\n" +
            "</p>\n" +
            "</html>";

            m_testShigemichi =
            "<?xml version=\"1.0\"?>" +
            "<diplomat version=\"1.0\" locale=\"en-US\" datatype=\"html\">" +
            "<skeleton>&lt;HTML&gt;" +
            "&lt;HEAD&gt;&lt;/HEAD&gt;" +
            "&lt;BODY&gt;" +
            "&lt;P&gt;</skeleton>" +
            "<translatable blockId=\"1\">" +
            "<segment segmentId=\"1\">Hello, <bpt i=\"1\" type=\"href\">&lt;A HREF=&quot;<sub locType=\"localizable\">world.html</sub>&quot;&gt;</bpt>world.<ept i=\"1\">&lt;/A&gt;</ept></segment>" +
    //		"Hello, <bpt i=\"1\" type=\"href\">&lt;A HREF=&quot;<sub locType=\"localizable\">world.html</sub>&quot;&gt;</bpt>world.<ept i=\"1\">&lt;/A&gt;</ept>" +
            "</translatable>" +
            "<skeleton>&lt;/P&gt;" +
            "&lt;/BODY&gt;" +
            "&lt;/HTML&gt;</skeleton>" +
            "</diplomat>";

            m_javaScript =
            "<SCRIPT>" +
            "alert(\"I love \\\"System 3!\\\"\");" +
            "</SCRIPT>" +
            "<A href=\"javascript:alert(&quot;I love \\&quot;System3!\\&quot;&quot;);\">Click me.</A>";
        }

    /**
     * Insert the method's description here.
     * Creation date: (8/16/2000 10:40:43 AM)
     */
    public static Test suite()
    {
        return new TestSuite(DiplomatMergerTest.class);
    }

    /**
     * Insert the method's description here.
     *
     */
    public void testSimpleGsaMerge()
    {
        DiplomatMerger diplomatMerger = new DiplomatMerger();
        L10nContent l10ncontent = new L10nContent();

        Exception ex = null;
        try
        {
            diplomatMerger.setKeepGsa(true);
            diplomatMerger.init(m_simpleMerge, l10ncontent);
            diplomatMerger.merge();
        }
        catch (DiplomatMergerException e)
        {
            ex = e;
        }
        assertNull(ex);

        String merged = l10ncontent.getL10nContent();
        assertEquals(merged.compareTo(m_simpleGsaMergedContent), 0);
    }

    /**
     * Insert the method's description here.
     *
     */
    public void testSimpleMerge()
    {
        DiplomatMerger diplomatMerger = new DiplomatMerger();
        L10nContent l10ncontent = new L10nContent();

        Exception ex = null;
        try
        {
            diplomatMerger.init(m_simpleMerge, l10ncontent);
            diplomatMerger.merge();
        }
        catch (DiplomatMergerException e)
        {
            ex = e;
        }
        assertNull(ex);

        String merged = l10ncontent.getL10nContent();
        assertEquals(merged.compareTo(m_simpleMergedContent), 0);
    }

    /**
     * Insert the method's description here.
     *
     */
    public void testSimpleMergeWithEncode()
    {
        DiplomatMerger diplomatMerger = new DiplomatMerger();

        L10nContent l10ncontent = new L10nContent();
        Exception ex = null;
        try
        {
            diplomatMerger.init(m_simpleMergeWithEncode, l10ncontent);
            diplomatMerger.merge();
        }
        catch (DiplomatMergerException e)
        {
            ex = e;
        }
        assertNull(ex);
        String merged = l10ncontent.getL10nContent();
        assertEquals(merged.compareTo(m_simpleMergedWithEncodeContent), 0);
    }

    /**
     * Insert the method's description here.
     *
     */
    public void testSimpleMergeWithSegments()
    {

        DiplomatMerger diplomatMerger = new DiplomatMerger();
        L10nContent l10ncontent = new L10nContent();

        Exception ex = null;
        try
        {
            diplomatMerger.init(m_diplomatWithSegments, l10ncontent);
            diplomatMerger.merge();
        }
        catch (DiplomatMergerException e)
        {
            ex = e;
        }
        assertNull(ex);

        String merged = l10ncontent.getL10nContent();
        assertEquals(merged.compareTo(m_diplomatWithSegmentsContent), 0);
    }

    /**
     * Insert the method's description here.
     *
     */
    public void testSimpleMergeWithSubflow()
    {
        DiplomatMerger diplomatMerger = new DiplomatMerger();
        L10nContent l10ncontent = new L10nContent();

        Exception ex = null;
        try
        {
            diplomatMerger.init(m_simpleMergeWithSubflow, l10ncontent);
            diplomatMerger.merge();
        }
        catch (DiplomatMergerException e)
        {
            ex = e;
        }
        assertNull(ex);

        String merged = l10ncontent.getL10nContent();
        assertEquals(merged.compareTo(m_simpleMergedWithSubflowContent), 0);
    }
    private String m_simpleRoundTripHtml = null;	/**
     * Insert the method's description here.
     *
     */

    public void testSimpleRoundTrip()
    {
        Exception ex = null;

        Extractor extractor = new Extractor();
        EFInputData input = new EFInputData();
        Locale locale = new Locale("en", "US");
        input.setLocale(locale);
        input.setCodeset("8859_1");
        input.setUnicodeInput(m_simpleRoundTripHtml);
        Output output = new Output();

        try
        {
            extractor.init(input, output);
            extractor.extract();
        }
        catch(ExtractorException e)
        {
            ex = e;
        }

        String diplomatOut = DiplomatWriter.WriteXML(output);

        DiplomatMerger diplomatMerger = new DiplomatMerger();
        L10nContent l10ncontent = new L10nContent();
        try
        {
            diplomatMerger.init(diplomatOut, l10ncontent);
            diplomatMerger.merge();
        }
        catch (DiplomatMergerException e)
        {
            ex = e;
        }
        assertNull(ex);

        String merged = l10ncontent.getL10nContent();
        assertEquals(merged.compareTo(m_simpleRoundTripHtml), 0);
    }

    /**
     * Insert the method's description here.
     *
     */
    public void testShigemichi()
    {
        DiplomatMerger diplomatMerger = new DiplomatMerger();
        L10nContent l10ncontent = new L10nContent();

        Exception ex = null;
        try
        {
            diplomatMerger.init(m_testShigemichi, l10ncontent);
            diplomatMerger.merge();
        }
        catch (DiplomatMergerException e)
        {
            ex = e;
        }
        assertNull(ex);
    }

    public void testJavascript()
    {
        Exception ex = null;

        com.globalsight.ling.docproc.extractor.html.Extractor
            extractor = new com.globalsight.ling.docproc.extractor.html.Extractor();
        EFInputData input = new EFInputData();
        Locale locale = new Locale("en", "US");
        input.setLocale(locale);
        input.setCodeset("8859_1");
        input.setUnicodeInput(m_javaScript);
        Output output = new Output();

        try
        {
            extractor.init(input, output);
            extractor.extract();
        }
        catch(ExtractorException e)
        {
            ex = e;
        }

        String diplomatOut = DiplomatWriter.WriteXML(output);

        DiplomatMerger diplomatMerger = new DiplomatMerger();
        L10nContent l10ncontent = new L10nContent();
        try
        {
            diplomatMerger.init(diplomatOut, l10ncontent);
            diplomatMerger.merge();
        }
        catch (DiplomatMergerException e)
        {
            ex = e;
        }
        assertNull(ex);

        String merged = l10ncontent.getL10nContent();
    }

    public void testExternalFIle()
    {
        Exception ex = null;
        String diplomat = null;
        DiplomatMerger diplomatMerger = new DiplomatMerger();
        L10nContent l10ncontent = new L10nContent();

        String input = "D:/work/entities.xml";


        try
        {
            File file = new File(input);
            byte[] bytes = new byte[(int)file.length()];
            FileInputStream reader = new FileInputStream(input);
            reader.read(bytes, 0, bytes.length);
            diplomat = new String(bytes, "UTF8");
        }
        catch (UnsupportedEncodingException e)
        {
            ex = e;
        }
        catch (FileNotFoundException e)
        {
            ex = e;
        }
        catch (java.io.IOException e)
        {
            ex = e;
        }



        try
        {
            diplomatMerger.init(diplomat, l10ncontent);
            diplomatMerger.merge();
        }
        catch (DiplomatMergerException e)
        {
            ex = e;
        }

        assertNull(ex);

        String merged = l10ncontent.getL10nContent();
    }
}
