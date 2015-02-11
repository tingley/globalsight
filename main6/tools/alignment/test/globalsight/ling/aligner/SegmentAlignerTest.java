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

import com.globalsight.ling.aligner.SegmentAligner;
import com.globalsight.ling.aligner.AlignerException;
import com.globalsight.ling.aligner.AlignedFiles;
import com.globalsight.ling.aligner.FileAligner;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

/** Test the tag aligner
 */
public class SegmentAlignerTest extends TestCase
{
    private String CONFIG_PATH1 = null;
    
    /**
     * @param p_name  */
    public SegmentAlignerTest(String p_name)
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
        {SegmentAlignerTest.class.getName()};
        junit.swingui.TestRunner.main(myargs);
    }

    /**
     * Insert the method's description here.
     * Creation date: (8/16/2000 2:47:41 PM)
     */
    public void setUp()
    {
        CONFIG_PATH1 = "d:/temp/align/JimConfig.properties";
    }

    /**
     * Insert the method's description here.
     * Creation date: (8/16/2000 10:40:43 AM)
     */
    public static Test suite()
    {
        return new TestSuite(SegmentAlignerTest.class);
    }

    /**
     */
    public void test1()
    {   
        SegmentAligner segmentAligner = new SegmentAligner();
        FileAligner fileAligner = new FileAligner();
        AlignedFiles alignedFiles = null;
        Exception ex = null;
        try
        {
            fileAligner.loadConfigFile(CONFIG_PATH1);
            while((alignedFiles = fileAligner.getNextFileGroup()) != null)
            {
                segmentAligner.align(alignedFiles);            
            }
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
    }
}