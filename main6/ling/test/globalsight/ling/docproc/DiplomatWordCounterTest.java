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

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import com.globalsight.ling.docproc.DiplomatWordCounterException;
import com.globalsight.ling.docproc.DiplomatWordCounter;
import com.globalsight.ling.docproc.DiplomatReader;
import com.globalsight.ling.docproc.DiplomatReaderException;
import com.globalsight.ling.docproc.Output;

/**
*/
public class DiplomatWordCounterTest 
extends TestCase
{
    private String m_diplomatWithoutWordCounts1 = null;
    private final int m_test1Count = 10;
    private String m_diplomatWithoutWordCounts2 = null;
    private final int m_test2Count = 11;
    private String m_diplomatWithoutWordCounts3 = null;
    private final int m_test3Count = 5;

    /**
    */
    public DiplomatWordCounterTest(String p_name)
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
        //String[] myargs = {DiplomatWordCounterTest.class.getName()};
        //junit.swingui.TestRunner.main(myargs);
        TestRunner.run(suite());
    }

    /**
    * Insert the method's description here.
    * Creation date: (8/16/2000 2:47:41 PM)
    */
    public void setUp()
    {
        m_diplomatWithoutWordCounts1 =
            "<diplomat version=\"1.0\" locale=\"en_US\" datatype=\"html\">"
                + "<skeleton>skeleton</skeleton>"
                + "<translatable blockId=\"1\">"
                + "<segment segmentId=\"1\">A translatable segment. </segment>"
                + "<segment segmentId=\"2\">A translatable segment. </segment>"
                + "<segment segmentId=\"3\">A translatable segment</segment>"
                + "</translatable>"
                + "<localizable blockId=\"2\">localizable</localizable>"
                + "</diplomat>";
    
        m_diplomatWithoutWordCounts2 =
            "<diplomat version=\"1.0\" locale=\"en_US\" datatype=\"html\">"
                + "<skeleton>skeleton</skeleton>"
                + "<translatable blockId=\"1\">"
                + "<segment segmentId=\"1\"><bpt i=\"1\" type=\"bold\">&lt;b&gt;</bpt>A translatable segment. "
                + "A <ph type=\"link\">&gt;a href=\"<sub locType=\"translatable\">link</sub>\"&lt;</ph>translatable segment. "
                + "A translatable segment<ept i=\"1\">&lt;/b&gt;</ept>"
                + "</segment></translatable>"
                + "<localizable blockId=\"2\">localizable</localizable>"
                + "</diplomat>";
    
        m_diplomatWithoutWordCounts3 =
            "<diplomat version=\"1.0\" locale=\"ja\" datatype=\"html\">"
                + "<skeleton>skeleton</skeleton>"
                + "<translatable blockId=\"1\">"
                + "<segment segmentId=\"2\">\u4249\u4269\u42ad\u427f</segment>"
                + "</translatable>"
                + "<localizable blockId=\"2\">localizable</localizable>"
                + "</diplomat>";
    }

    /**
    * Insert the method's description here.
    * Creation date: (8/16/2000 10:40:43 AM)
    */
    public static Test suite()
    {
        return new TestSuite(DiplomatWordCounterTest.class);
    }

    /**
    * Simple word count test with NO internal Diplomat tags
    *
    */
    public void test1()
    {
        DiplomatWordCounter diplomatWordCounter = new DiplomatWordCounter();
        String withWordCounts = null;
        Output output = null;
    
        Exception ex = null;
        try
        {
            withWordCounts = diplomatWordCounter.countDiplomatDocument(m_diplomatWithoutWordCounts1);
            DiplomatReader diplomatReader = new DiplomatReader(withWordCounts);
            output = diplomatReader.getOutput();
        }
        catch (DiplomatWordCounterException e)
        {
            ex = e;
        }
        catch (DiplomatReaderException e)
        {
            ex = e;
        }
        assertNull(ex);
    
        assertEquals(output.getWordCount(), m_test1Count);
    }

    /**
    * Simple word count test with internal Diplomat tags
    *
    */
    public void test2()
    {
        DiplomatWordCounter diplomatWordCounter = new DiplomatWordCounter();
        String withWordCounts = null;
        Output output = null;
    
        Exception ex = null;
        try
        {
            withWordCounts = diplomatWordCounter.countDiplomatDocument(m_diplomatWithoutWordCounts2);
            DiplomatReader diplomatReader = new DiplomatReader(withWordCounts);
            output = diplomatReader.getOutput();
        }
        catch (DiplomatWordCounterException e)
        {
            ex = e;
        }
        catch (DiplomatReaderException e)
        {
            ex = e;
        }
        catch (Exception e)
        {
            ex = e;
        }
        assertNull(ex);
    
        assertEquals(output.getWordCount(), m_test2Count);
    }

    /**
    * Simple word count test with Japanese
    * Note: The JDK logic for wordcounting seems broken!
    */
    public void test3()
    {
        DiplomatWordCounter diplomatWordCounter = new DiplomatWordCounter();
        String withWordCounts = null;
        Output output = null;
    
        Exception ex = null;
        try
        {
            withWordCounts = diplomatWordCounter.countDiplomatDocument(m_diplomatWithoutWordCounts3);
            DiplomatReader diplomatReader = new DiplomatReader(withWordCounts);
            output = diplomatReader.getOutput();
        }
        catch (DiplomatWordCounterException e)
        {
            ex = e;
        }
        catch (DiplomatReaderException e)
        {
            ex = e;
        }
        assertNull(ex);
    
        //assertEquals(output.getWordCount(), m_test2Count);
    }
}
