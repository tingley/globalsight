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
package test.globalsight.ling.common;

// Imports
import com.globalsight.ling.common.RegEx;
import com.globalsight.ling.common.RegExException;
import com.globalsight.ling.common.RegExMatch;
import com.globalsight.ling.common.RegExMatchInterface;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
*/
public class RegExTest
    extends TestCase
{
    private String m_input1 = null;
    private String m_pattern1 = null;

    private String m_input2 = null;
    private String m_pattern2 = null;

    private String m_input3 = null;
    private String m_pattern3 = null;

    private String m_input4 = null;
    private String m_pattern4 = null;

    private String m_input5 = null;
    private String m_pattern5 = null;

    public static Test suite()
    {
        return new TestSuite(RegExTest.class);
    }

    public RegExTest(String p_Name)
    {
        super(p_Name);
    }

    public static void main(String[] args)
    {
        String[] myargs = {RegExTest.class.getName()};
        junit.swingui.TestRunner.main(myargs);
    }

    public void setUp()
    {
        m_input1 = "AAABBBCCC";
        m_pattern1 = "([a]+)([b]+)([c]+)";

        m_input2 = "AAABBBCCC";
        m_pattern2 = "([b]+)";

        m_input3 = "CHARSET=ISO-8859-1";
        m_pattern3 = "charset=([^ \'\"]+)";

        m_input4 = "CHARSET";
        m_pattern4 = "char.*";

        m_input5 = "charset";
        m_pattern5 = "char.*";
    }

    public void test1()
    {
        Exception ex = null;
        RegExMatch match = null;
        try
        {
            //m_input = "aaabbbccc";
            //m_pattern = "([a]+)([b]+)([c]+)";
            match = (RegExMatch)RegEx.matchSubstring(m_input1, m_pattern1,
              false);                             // case-insensitive
        }
        catch (RegExException e)
        {
            ex = e;
        }
        assertNull(ex);
        assertNotNull(match);
        assert(match.groups() == 4);
        assert("begin offset 0", match.beginOffset(0) == 0);
        assert("end offset 0", match.endOffset(0) == 9);

        assert("begin offset 1", match.beginOffset(1) == 0);
        assert("end offset 1", match.endOffset(1) == 3);

        assert("begin offset 2", match.beginOffset(2) == 3);
        assert("end offset 2", match.endOffset(2) == 6);

        assert("begin offset 3", match.beginOffset(3) == 6);
        assert("end offset 3", match.endOffset(3) == 9);

        assert(match.toString().equalsIgnoreCase("aaabbbccc"));
        assert(match.group(1).equalsIgnoreCase("aaa"));
        assert(match.group(2).equalsIgnoreCase("bbb"));
        assert(match.group(3).equalsIgnoreCase("ccc"));

        assert(match.length() == 9);

    }

    public void test2()
    {

        Exception ex = null;
        String result = null;
        String sub = "xxx";

        try
        {
            result = RegEx.substituteAll(m_input2, m_pattern2, sub, false);
        }
        catch (RegExException e)
        {
            ex = e;
        }
        assertNull(ex);
        assert(result.equalsIgnoreCase("aaaxxxccc"));
    }

    public void test3()
    {

        Exception ex = null;
        RegExMatch match = null;

        try
        {
            match = (RegExMatch)
              RegEx.matchSubstring(m_input3, m_pattern3, false);
        }
        catch (RegExException e)
        {
            ex = e;
        }
        assertNull(ex);
        assertNotNull(match);
    }

    public void test4()
    {

        Exception ex = null;
        RegExMatch match = null;

        try
        {
            match = (RegExMatch)
              RegEx.matchSubstring(m_input4, m_pattern4, false);
        }
        catch (RegExException e)
        {
            ex = e;
        }
        assertNull(ex);
        assertNotNull(match);
    }

    public void test5()
    {

        Exception ex = null;
        RegExMatch match = null;

        try
        {
            match = (RegExMatch)
              RegEx.matchSubstring(m_input5, m_pattern5, false);
        }
        catch (RegExException e)
        {
            ex = e;
        }
        assertNull(ex);
        assertNotNull(match);
    }
}
