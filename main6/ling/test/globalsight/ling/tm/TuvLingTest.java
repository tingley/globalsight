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

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Test;
import junit.textui.TestRunner;

import com.globalsight.ling.tm.TuvLing;
import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.ling.tm.LingManagerException;

import java.util.Locale;
import java.io.CharArrayWriter;
import java.io.PrintWriter;


public class TuvLingTest extends TestCase
{
    private TuvLing m_tuvLing = new TuvImpl();
    
    protected void setUp()
    {
        m_tuvLing.setGxml("<segment>If you wish to edit your preferences please <bpt type=\"link\" i=\"1\">&lt;a href=&quot;<sub type=\"url-a\" locType=\"localizable\">login.jhtml</sub>&quot;&gt;</bpt>log in<ept i=\"1\">&lt;/a&gt;</ept> first.</segment>");
    }
    
    public TuvLingTest(String name)
    {
        super(name);
    }
    
    public static Test suite()
    {
        return new TestSuite(TuvLingTest.class);
        
    }
    
    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }

    public void testGetExactMatchFormat()
        throws LingManagerException
    {
        String expectedFormat = "If you wish to edit your preferences please <a href=\"login.jhtml\">log in</a> first.";
        String exactMatchFormat = m_tuvLing.getExactMatchFormat();
        //        System.out.println(expectedFormat);
        //        System.out.println(exactMatchFormat);
        assertEquals(exactMatchFormat, expectedFormat);
    }
    
    public void testAGetFuzzyMatchFormat()
        throws LingManagerException
    {
        String expectedFormat = "If you wish to edit your preferences please login.jhtmllog in first.";
        String exactMatchFormat = m_tuvLing.getFuzzyMatchFormat();
        //        System.out.println(expectedFormat);
        //        System.out.println(exactMatchFormat);
        assertEquals(exactMatchFormat, expectedFormat);
    }
    
    public void testGetGxmlWithoutTags()
        throws LingManagerException
    {
        String expectedFormat = "If you wish to edit your preferences please log in first.";
        String exactMatchFormat = m_tuvLing.getGxmlWithoutTags();
        //        System.out.println(expectedFormat);
        //        System.out.println(exactMatchFormat);
        assertEquals(exactMatchFormat, expectedFormat);
    }
}

            
