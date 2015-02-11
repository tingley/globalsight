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

package test.globalsight.ling.aligner;

import java.lang.Exception;
import java.lang.Throwable;

import com.globalsight.ling.aligner.SegmentTagsAligner;
import com.globalsight.ling.aligner.AlignerException;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

/** Test the tag aligner
 */
public class SegmentTagsAlignerTest extends TestCase
{
    private String m_SourceDiplomat1 = null;
    private String m_TargetDiplomat1 = null;
    private String m_NewTargetDiplomat1 = null;

    private String m_SourceDiplomat2 = null;
    private String m_TargetDiplomat2 = null;
    
    private String m_SourceDiplomat3 = null;
    private String m_TargetDiplomat3 = null;
    
    private String m_NewTargetDiplomat2 = null;

    /**
     * @param p_name  */
    public SegmentTagsAlignerTest(String p_name)
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
        String[] myargs =
        {SegmentTagsAlignerTest.class.getName()};
        junit.swingui.TestRunner.main(myargs);
    }

    /**
     * Insert the method's description here.
     * Creation date: (8/16/2000 2:47:41 PM)
     */
    public void setUp()
    {
        m_SourceDiplomat1 =
        "This is a test <bpt type=\"bold\" i=\"1\" x=\"5\" erasable=\"yes\">" +
        "&lt;b&gt;" +
        "</bpt>" +
        "bold text <ept i=\"1\">&lt;/b&gt;</ept> new stuff";

        m_TargetDiplomat1 =
        "This is a test <bpt type=\"bold\" i=\"1\" x=\"1\" erasable=\"yes\">" +
        "&lt;b&gt;" +
        "</bpt>" +
        "bold text <ept i=\"1\">&lt;/b&gt;</ept> new stuff";

        m_NewTargetDiplomat1 =
        "This is a test <bpt type=\"bold\" i=\"1\" erasable=\"yes\" x=\"5\">" +
        "&lt;b&gt;" +
        "</bpt>" +
        "bold text <ept i=\"1\">&lt;/b&gt;</ept> new stuff";

        m_SourceDiplomat2 =
        "This is a test <bpt type=\"x-foo\" i=\"1\" x=\"5\">" +
        "&lt;b " +
        "q=\"<sub locType=\"translatable\" type=\"foo\">sub stuff</sub>" +
        "\"&gt;" +
        "</bpt>" +
        "bold text <ept i=\"1\">&lt;/b&gt;</ept> new stuff";

        m_TargetDiplomat2 =
        "This is a test <bpt type=\"x-foo\" i=\"1\" x=\"1\">" +
        "&lt;b " +
        "q=\"<sub locType=\"translatable\" type=\"foo\">sub stuff trg</sub>" +
        "\"&gt;" +
        "</bpt>" +
        "bold text <ept i=\"1\">&lt;/b&gt;</ept> new stuff";

        m_NewTargetDiplomat2 =
        "This is a test <bpt type=\"x-foo\" i=\"1\" x=\"5\">" +
        "&lt;b " +
        "q=&quot;<sub locType=\"translatable\" type=\"foo\">sub stuff trg</sub>" +
        "&quot;&gt;" +
        "</bpt>" +
        "bold text <ept i=\"1\">&lt;/b&gt;</ept> new stuff";
        
        m_SourceDiplomat3 =
        "<bpt erasable=\"yes\" i=\"1\" type=\"bold\" x=\"1\">&lt;b&gt;</bpt>Virgin? Megastore" +
        "<ept i=\"1\">&lt;/b&gt;</ept>";
        
        m_TargetDiplomat3 =
        "<bpt erasable=\"yes\" i=\"1\" type=\"bold\" x=\"1\">&lt;b&gt;</bpt>Virgin" +
        "<ept i=\"1\">&lt;/b&gt;</ept>™ <bpt erasable=\"yes\" i=\"2\" type=\"bold\" x=\"2\">&lt;b&gt;</bpt>Megastore<ept i=\"2\">&lt;/b&gt;</ept>";
        
        //<a href="javascript:openAttraction(&apos;../../../../II/A/2/b/illuminations.html&apos;);">Illuminations: Reflections of Earth</a>
        //<a href="javascript:openAttraction2(&apos;../../../../II/A/2/b/illuminations.html&apos;);">Illuminations: Reflections of Earth</a>

    }

    /**
     * Insert the method's description here.
     * Creation date: (8/16/2000 10:40:43 AM)
     */
    public static Test suite()
    {
        return new TestSuite(SegmentTagsAlignerTest.class);
    }

    /**
     */
    public void test1()
    {
        SegmentTagsAligner tagAligner = new SegmentTagsAligner();
        String newTarget = null;

        Exception ex = null;
        try
        {
            newTarget = tagAligner.alignTags(m_SourceDiplomat1, m_TargetDiplomat1);
        }
        catch (AlignerException e)
        {
            ex = e;
        }
        catch (Exception e)
        {
            ex = e;
        }
        assertNull(ex);
        assert(newTarget.compareTo(m_NewTargetDiplomat1) == 0);
    }


    /**
     */
    public void test2()
    {
        SegmentTagsAligner tagAligner = new SegmentTagsAligner();
        String newTarget = null;

        Exception ex = null;
        try
        {
            newTarget = tagAligner.alignTags(m_SourceDiplomat2, m_TargetDiplomat2);
        }
        catch (AlignerException e)
        {
            ex = e;
        }
        catch (Exception e)
        {
            ex = e;
        }
        assertNull(ex);
        assert(newTarget.compareTo(m_NewTargetDiplomat2) == 0);
    }
    
    /**
     */
    public void test3()
    {
        SegmentTagsAligner tagAligner = new SegmentTagsAligner();
        String newTarget = null;

        Exception ex = null;
        try
        {
            newTarget = tagAligner.alignTags(m_SourceDiplomat3, m_TargetDiplomat3);
        }
        catch (AlignerException e)
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
}