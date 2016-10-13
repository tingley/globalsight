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
import java.util.Properties;

import com.globalsight.ling.common.DiplomatBasicParser;
import com.globalsight.ling.common.DiplomatBasicParserException;
import com.globalsight.ling.common.DiplomatBasicHandler;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
*/
public class DiplomatBasicParserTest 
extends TestCase
{                          private String m_withSpaceBeforeEnd = null;

    private class m_handler 
        implements DiplomatBasicHandler 
    {
        public String m_tag = null;

        public void handleEndTag(String p_name, String p_originalTag)
            throws DiplomatBasicParserException
        {
        }

        public void handleStartTag(
            String p_name, 
            Properties p_atributes, 
            String p_originalString) throws DiplomatBasicParserException
        {
            m_tag = p_originalString;
        }

        public void handleText(String p_text) throws DiplomatBasicParserException
        {
        }

        public void handleStart() throws DiplomatBasicParserException
        {
        }

        public void handleStop() throws DiplomatBasicParserException
        {
        }
    }

    /**
     * Insert the method's description here.
     * Creation date: (8/16/2000 10:40:43 AM)
     */
    public static Test suite()
    { 
        return new TestSuite(DiplomatBasicParserTest.class);
    }

    /**
     * Insert the method's description here.
     * 
     */
    public void test1()
    {
        
        Exception ex = null;
        MyHandler handler = new MyHandler();
        DiplomatBasicParser parser = new DiplomatBasicParser(handler);
        try
        {
            parser.parse(m_withSpaceBeforeEnd);
        }
        catch (DiplomatBasicParserException e)
        {
            ex = e;
        }
        assertNull(ex);
        assertEquals(handler.m_tag.compareTo(m_withSpaceBeforeEnd), 0);
    }

    /**
     * JPExtractorTest constructor comment.
     */
    public DiplomatBasicParserTest(String p_Name)
    {
        super(p_Name);
    }

    /**
     * Insert the method's description here.
     */
    public void setUp() 
    {
        m_withSpaceBeforeEnd = "<b i=\"1\" >";
    }    private class MyHandler 
        implements DiplomatBasicHandler 
    {
        public String m_tag = null;

        public void handleEndTag(String p_name, String p_originalTag)
            throws DiplomatBasicParserException
        {
        }

        public void handleStartTag(
            String p_name, 
            Properties p_atributes, 
            String p_originalString) throws DiplomatBasicParserException
        {
            m_tag = p_originalString;
        }

        public void handleText(String p_text) throws DiplomatBasicParserException
        {
        }

        public void handleStart() throws DiplomatBasicParserException
        {
        }

        public void handleStop() throws DiplomatBasicParserException
        {
        }
    }}
