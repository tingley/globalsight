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
package test.globalsight.ling.tm;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import com.globalsight.ling.tm.LingManagerException;
import com.globalsight.ling.tm.SegmentTagsAligner;
import com.sun.org.apache.regexp.internal.RE;
import com.sun.org.apache.regexp.internal.RESyntaxException;

/**
 * Test the tag aligner
 */
public class SegmentTagsAlignerTest extends TestCase
{
    private static final String ROOT_TAGS_REGEX = "<segment[^>]*>|</segment[:space:]*>|<localizable[^>]*>|</localizable[:space:]*>";
    private static RE c_removeRootTags = makeParser(ROOT_TAGS_REGEX);

    private static RE makeParser(String p_regex)
    {
        RE tmpParser = null;

        try
        {
            tmpParser = new RE(p_regex, RE.MATCH_NORMAL);
        }
        catch (RESyntaxException e)
        {
            int xxxx = 1;
        }

        return tmpParser;
    }

    private String m_SourceDiplomat1 = null;
    private String m_TargetDiplomat1 = null;
    private String m_NewTargetDiplomat1 = null;

    private String m_SourceDiplomat2 = null;
    private String m_TargetDiplomat2 = null;
    private String m_NewTargetDiplomat2 = null;

    private String m_SourceDiplomat3 = null;
    private String m_TargetDiplomat3 = null;

    private String m_SourceDiplomat4 = null;
    private String m_TargetDiplomat4 = null;

    private String m_SourceDiplomat5 = null;
    private String m_TargetDiplomat5 = null;
    private String m_newTargetDiplomat5 = null;

    private String m_SourceDiplomat6 = null;
    private String m_TargetDiplomat6 = null;
    private String m_newTargetDiplomat6 = null;

    private String m_SourceDiplomat7 = null;
    private String m_TargetDiplomat7 = null;
    private String m_newTargetDiplomat7 = null;

    /**
     * @param p_name
     */
    public SegmentTagsAlignerTest(String p_name)
    {
        super(p_name);
    }

    /**
     * Insert the method's description here. Creation date: (8/15/2000 5:32:33
     * PM)
     * 
     * @param args
     *            java.lang.String[]
     */
    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }

    public void setUp()
    {
        m_SourceDiplomat1 = "This is a test <bpt type=\"bold\" i=\"1\" x=\"5\" erasable=\"yes\">"
                + "&lt;b&gt;"
                + "</bpt>"
                + "bold text <ept i=\"1\">&lt;/b&gt;</ept> new stuff";

        m_TargetDiplomat1 = "This is a test <bpt type=\"bold\" i=\"1\" x=\"1\" erasable=\"yes\">"
                + "&lt;b&gt;"
                + "</bpt>"
                + "bold text <ept i=\"1\">&lt;/b&gt;</ept> new stuff";

        m_NewTargetDiplomat1 = "This is a test <bpt type=\"bold\" i=\"1\" erasable=\"yes\" x=\"5\">"
                + "&lt;b&gt;"
                + "</bpt>"
                + "bold text <ept i=\"1\">&lt;/b&gt;</ept> new stuff";

        m_SourceDiplomat2 = "This is a test <bpt type=\"x-foo\" i=\"1\" x=\"5\">"
                + "&lt;b "
                + "q=\"<sub locType=\"translatable\" type=\"foo\">sub stuff</sub>"
                + "\"&gt;"
                + "</bpt>"
                + "bold text <ept i=\"1\">&lt;/b&gt;</ept> new stuff";

        m_TargetDiplomat2 = "This is a test <bpt type=\"x-foo\" i=\"1\" x=\"1\">"
                + "&lt;b "
                + "q=\"<sub locType=\"translatable\" type=\"foo\">sub stuff trg</sub>"
                + "\"&gt;"
                + "</bpt>"
                + "bold text <ept i=\"1\">&lt;/b&gt;</ept> new stuff";

        m_NewTargetDiplomat2 = "This is a test <bpt type=\"x-foo\" i=\"1\" x=\"5\">"
                + "&lt;b "
                + "q=&quot;<sub locType=\"translatable\" type=\"foo\">sub stuff trg</sub>"
                + "&quot;&gt;"
                + "</bpt>"
                + "bold text <ept i=\"1\">&lt;/b&gt;</ept> new stuff";

        m_SourceDiplomat3 = "<bpt erasable=\"yes\" i=\"1\" type=\"bold\" x=\"1\">&lt;b&gt;</bpt>Virgin? Megastore"
                + "<ept i=\"1\">&lt;/b&gt;</ept>";

        m_TargetDiplomat3 = "<bpt erasable=\"yes\" i=\"1\" type=\"bold\" x=\"1\">&lt;b&gt;</bpt>Virgin"
                + "<ept i=\"1\">&lt;/b&gt;</ept>™ <bpt erasable=\"yes\" i=\"2\" type=\"bold\" x=\"2\">&lt;b&gt;</bpt>Megastore<ept i=\"2\">&lt;/b&gt;</ept>";

        // <a
        // href="javascript:openAttraction(&apos;../../../../II/A/2/b/illuminations.html&apos;);">Illuminations:
        // Reflections of Earth</a>
        // <a
        // href="javascript:openAttraction2(&apos;../../../../II/A/2/b/illuminations.html&apos;);">Illuminations:
        // Reflections of Earth</a>

        m_SourceDiplomat4 = "<segment segmentId=\"1\" wordcount=\"5\">A very simple <bpt i=\"1\" type=\"link\" x=\"1\">&lt;a href=&quot;<sub type=\"url-a\" locType=\"localizable\">test.html</sub>&quot;&gt;</bpt>link<ept i=\"1\">&lt;/a&gt;</ept> segment.</segment>";

        m_TargetDiplomat4 = "<segment segmentId=\"1\" wordcount=\"5\">A very simple <bpt i=\"1\" type=\"link\" x=\"2\">&lt;a href=&quot;<sub type=\"url-a\" id=\"1\" locType=\"localizable\">test.html</sub>&quot;&gt;</bpt>link<ept i=\"1\">&lt;/a&gt;</ept> segment.</segment>";

        m_SourceDiplomat5 = "<bpt i=\"1\" type=\"link\" x=\"1\">link1</bpt><ept i=\"1\">link1</ept>";

        m_TargetDiplomat5 = "aaa<bpt i=\"3\" erasable = \"yes\" type=\"addable\" x=\"3\">addable</bpt><ept i=\"3\">addable</ept>xxx<bpt i=\"1\" type=\"link\" x=\"1\">link1</bpt><ept i=\"1\">link1</ept>yyyzzz";

        m_newTargetDiplomat5 = "aaa<bpt i=\"3\" erasable=\"yes\" type=\"addable\" x=\"3\">addable</bpt><ept i=\"3\">addable</ept>xxx<bpt i=\"1\" type=\"link\" x=\"1\">link1</bpt><ept i=\"1\">link1</ept>yyyzzz";
        // "aaa<bpt i=\"3\" erasable=\"yes\" type=\"addable\" x=\"3\">addable</bpt>xxx<bpt i=\"1\" type=\"link\" x=\"1\">link1</bpt><ept i=\"1\">link1</ept>yyyzzz"
        m_SourceDiplomat6 = "<bpt i=\"1\" type=\"link\" x=\"1\">&lt;a href=&quot;<sub type=\"url-a\" locType=\"localizable\">a.html</sub>&quot;&gt;</bpt>Translation Memory.<ept i=\"1\">&lt;/a&gt;</ept>";

        m_TargetDiplomat6 = "<bpt i=\"1\" type=\"link\" x=\"1\">&lt;a href=&quot;<sub type=\"url-b\" locType=\"localizable\">a.html</sub>&quot;&gt;</bpt>Translation Memory.<ept i=\"1\">&lt;/a&gt;</ept>";

        m_newTargetDiplomat6 = "Translation Memory.";

        m_SourceDiplomat7 = "<segment segmentId=\"1\" wordcount=\"27\"><bpt i=\"1\" type=\"link\" x=\"1\">&lt;a href=&quot;<sub type=\"url-a\" locType=\"localizable\">seg_one_Link_one</sub>&quot;&gt;</bpt>Seg xxxxx link1 text<ept i=\"1\">&lt;/a&gt;</ept> seg one normal1 text <bpt i=\"2\" type=\"link\" x=\"2\">&lt;a href=&quot;<sub type=\"url-a\" locType=\"localizable\">seg_one_Link_two</sub>&quot;&gt;</bpt> seg one link2 text<ept i=\"2\">&lt;/a&gt;</ept> seg one more normal2 text<bpt i=\"3\" type=\"link\" x=\"3\">&lt;a href=&quot;<sub type=\"url-a\" locType=\"localizable\">seg_one_Link_three</sub>&quot;&gt;</bpt> seg one link3 text<ept i=\"3\">&lt;/a&gt;</ept> seg one still more normal3 text.</segment>";

        m_TargetDiplomat7 = "<segment segmentId=\"1\" wordcount=\"27\"><bpt i=\"1\" type=\"link\" x=\"1\">&lt;a href=&quot;<sub type=\"url-a\" locType=\"localizable\">seg_one_Link_one</sub>&quot;&gt;</bpt>Seg xxxxx link1 text<ept i=\"1\">&lt;/a&gt;</ept> seg one normal1 text <bpt i=\"2\" type=\"link\" x=\"2\">&lt;a href=&quot;<sub type=\"url-a\" locType=\"localizable\">seg_one_Link_two</sub>&quot;&gt;</bpt> seg one link2 text<ept i=\"2\">&lt;/a&gt;</ept> seg one more normal2 text<bpt i=\"3\" type=\"link\" x=\"3\">&lt;a href=&quot;<sub type=\"url-a\" locType=\"localizable\">seg_one_Link_three</sub>&quot;&gt;</bpt> seg one link3 text<ept i=\"3\">&lt;/a&gt;</ept> seg one still more normal3 text.</segment>";

        m_newTargetDiplomat7 = "<segment segmentId=\"1\" wordcount=\"27\">Seg xxxxx link1 text seg one normal1 text  seg one link2 text seg one more normal2 text seg one link3 text seg one still more normal3 text.</segment>";

    }

    public static Test suite()
    {
        return new TestSuite(SegmentTagsAlignerTest.class);
    }

    public void test1()
    {
        SegmentTagsAligner tagAligner = new SegmentTagsAligner();
        String newTarget = null;

        Exception ex = null;
        try
        {
            newTarget = tagAligner.alignTags(m_SourceDiplomat1,
                    m_TargetDiplomat1);
        }
        catch (LingManagerException e)
        {
            ex = e;
        }
        catch (Exception e)
        {
            ex = e;
        }
        assertNull(ex);
        assertEquals(newTarget.compareTo(m_NewTargetDiplomat1), 0);
    }

    public void test2()
    {
        SegmentTagsAligner tagAligner = new SegmentTagsAligner();
        String newTarget = null;

        Exception ex = null;
        try
        {
            newTarget = tagAligner.alignTags(m_SourceDiplomat2,
                    m_TargetDiplomat2);
        }
        catch (LingManagerException e)
        {
            ex = e;
        }
        catch (Exception e)
        {
            ex = e;
        }
        assertNull(ex);
        assertEquals(newTarget.compareTo(m_NewTargetDiplomat2), 0);
    }

    public void test3()
    {
        SegmentTagsAligner tagAligner = new SegmentTagsAligner();
        String newTarget = null;

        Exception ex = null;
        try
        {
            newTarget = tagAligner.alignTags(m_SourceDiplomat3,
                    m_TargetDiplomat3);
        }
        catch (LingManagerException e)
        {
            ex = e;
        }
        catch (Exception e)
        {
            ex = e;
        }
        assertNull(ex);
        assertNotNull(newTarget);
    }

    public void test4()
    {
        SegmentTagsAligner tagAligner = new SegmentTagsAligner();
        String newTarget = null;

        Exception ex = null;
        try
        {
            newTarget = tagAligner.alignTags(m_SourceDiplomat4,
                    m_TargetDiplomat4);
        }
        catch (LingManagerException e)
        {
            ex = e;
        }
        catch (Exception e)
        {
            ex = e;
        }

        if (tagAligner.isDifferingX())
        {
            assertTrue(true);
        }

        assertNull(ex);
        assertNotNull(newTarget);
    }

    public void test5()
    {
        SegmentTagsAligner tagAligner = new SegmentTagsAligner();
        String newTarget = null;

        Exception ex = null;
        try
        {
            newTarget = tagAligner.removeUnmatchedTargetTags(m_SourceDiplomat5,
                    m_TargetDiplomat5);
        }
        catch (LingManagerException e)
        {
            ex = e;
        }
        catch (Exception e)
        {
            ex = e;
        }
        assertNull(ex);
        assertEquals(newTarget.compareTo(m_newTargetDiplomat5), 0);
    }

    public void test6()
    {
        SegmentTagsAligner tagAligner = new SegmentTagsAligner();
        String newTarget = null;

        Exception ex = null;
        try
        {
            newTarget = tagAligner.removeUnmatchedTargetTags(m_SourceDiplomat6,
                    m_TargetDiplomat6);
        }
        catch (LingManagerException e)
        {
            ex = e;
        }
        catch (Exception e)
        {
            ex = e;
        }
        assertNull(ex);
        assertEquals(newTarget.compareTo(m_newTargetDiplomat6), 0);
    }

    public void test7()
    {
        SegmentTagsAligner tagAligner = new SegmentTagsAligner();
        String newTarget = null;

        Exception ex = null;
        try
        {
            newTarget = tagAligner.removeUnmatchedTargetTags(m_SourceDiplomat7,
                    m_TargetDiplomat7);
        }
        catch (LingManagerException e)
        {
            ex = e;
        }
        catch (Exception e)
        {
            ex = e;
        }

        // String rootBeGone = c_removeRootTags.subst(newTarget, "");
        assertNull(ex);
        assertEquals(newTarget.compareTo(m_newTargetDiplomat7), 0);
    }
}
