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
package test.globalsight.ling.tw.offline.parser;

import java.io.StringReader;
import java.io.Reader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

import com.globalsight.ling.tw.offline.parser.AmbassadorDwUpParser;
import com.globalsight.ling.tw.offline.parser.ParseException;

public class AmbassadorDwUpParserTest 
extends TestCase
{   
    private String m_allIncluded = null;
    private String m_optionalRemoved = null;
    private String m_noSegments = null;
    private String m_testPageName = null;
    
    /**
    */
    public AmbassadorDwUpParserTest(String p_name)
    {
        super(p_name);
    }

    /**
    */
    public void setUp()
    {      
        m_allIncluded =
        "# GlobalSight Download File - Do not modify any lines that start with a pound sign.\n" +
        "# Encoding: ASCII\n" +
        "# Document Format: XML\n" +
        "# Placeholder Format: PTAG-VERBOSE\n" +
        "# Source Locale: en_US\n" +
        "# Target Locale: en_US\n" +
        "# Page Name: en_US\\AllIncludedOptions\n" +
        "# Page ID: 1\n" + 
        "# Workflow ID: 1\n" +
        "# Task ID: 1\n" +
        "# Exact Match word count: 100\n" +
        "# Fuzzy Match word count: 100\n" +
        "# No Match word count: 100\n" +
        "# Edit all: Yes\n" +
        "# Help: Working Offline\n" +
        "\n" +
        "# 19379\n" +
        "# Segment Format: HTML\n" +
        "# Match Type: Exact - for context (Locked)\n" +
        "# Match Score: score\n" +
        "# Resources: [Term4], [Segment5], [Terminology8]\n" +
        "# Src:\n" +
        "A Target with [link1] one link[/link1] and then [link2]another[/link2]\n" +
        "\n" +
        "# Trg:\n" +
        "A Target with [link1] one link[/link1] and then [link2]another[/link2]\n" +
        "\n" +
        "# 19379:[link1]:4563\n" +
        "# Segment Format: HTML\n" +
        "# Match Type: Exact subflow - for context (Locked)\n" +
        "# Src:\n" +
        "Subflow under link1 in segment 19379\n" +
        "\n" +
        "# Trg:\n" +
        "Subflow under link1 in segment 19379\n" +
        "\n" +
        "# END GlobalSight Download File - Do not modify any lines that start with a pound sign.";   

        m_optionalRemoved =
        "# GlobalSight Download File - Do not modify any lines that start with a pound sign.\n" +
        "# Encoding: ASCII\n" +
        "# Document Format: XML\n" +
        "# Placeholder Format: PTAG-VERBOSE\n" +
        "# Source Locale: en_US\n" +
        "# Target Locale: en_US\n" +
        "# Page ID: 1\n" +
        "# Workflow ID: 1\n" +
        "# Task ID: 1\n" +
        "\n" +
        "# 19379\n" +
        "A Target with [link1] one link[/link1] and then [link2]another[/link2]\n" +        
        "\n" +
        "# 19379:[/link1]:4563\n" +
        "Subflow under link1 in segment 19379\n" +      
        "\n" +
        "# END GlobalSight Download File - Do not modify any lines that start with a pound sign.";   
        
        m_noSegments =
        "# GlobalSight Download File - Do not modify any lines that start with a pound sign.\n" +
        "# Encoding: ASCII\n" +
        "# Document Format: XML\n" +
        "# Placeholder Format: PTAG-VERBOSE\n" +
        "# Source Locale: en_US\n" +
        "# Target Locale: en_US\n" +
        "# Page Name: en_US\\noSegments\n" +
        "# Page ID: 1\n" + 
        "# Workflow ID: 1\n" +
        "# Task ID: 1\n" +
        "# Exact Match word count: 100\n" +
        "# Fuzzy Match word count: 100\n" +
        "# No Match word count: 100\n" +
        "# Edit all: Yes\n" +
        "\n" +
        "\n" +
        "# END GlobalSight Download File - Do not modify any lines that start with a pound sign.";   

         m_testPageName =
        "# GlobalSight Download File - Do not modify any lines that start with a pound sign.\n" +
        "# Encoding: ASCII\n" +
        "# Document Format: XML\n" +
        "# Placeholder Format: PTAG-VERBOSE\n" +
        "# Source Locale: en_US\n" +
        "# Target Locale: en_US\n" +
        "# Page Name: !@#$%^&*()_-+={[}]|':;?><,\u000c\u100c\\\u200c\u300c\\\u400c\u500c\n" +
        "# Page ID: 1\n" + 
        "# Workflow ID: 1\n" +
        "# Task ID: 1\n" +
        "# Exact Match word count: 100\n" +
        "# Fuzzy Match word count: 100\n" +
        "# No Match word count: 100\n" +
        "# Edit all: Yes\n" +
        "\n" +
        "# END GlobalSight Download File - Do not modify any lines that start with a pound sign.";
    }

    /**    
    */
    public static Test suite()
    {
        return new TestSuite(AmbassadorDwUpParserTest.class);
    }
    
    /**    
    */
    public void test1()
    {
        AmbassadorDwUpEventHandler handler = new AmbassadorDwUpEventHandler();
        Reader input = new StringReader(m_allIncluded);
        AmbassadorDwUpParser parser = new AmbassadorDwUpParser(input);       
    
        Exception ex = null;
        try
        {
            parser.setHandler(handler);
            parser.parse();            
        }
        catch(ParseException e)
        {
            ex = e;
            System.out.println("Test1: " + e.toString());
        }
        assertNull(ex);
    }
    
    /**    
    */
    public void test2()
    {
        AmbassadorDwUpEventHandler handler = new AmbassadorDwUpEventHandler();
        Reader input = new StringReader(m_optionalRemoved);
        AmbassadorDwUpParser parser = new AmbassadorDwUpParser(input);       
    
        Exception ex = null;
        try
        {
            parser.setHandler(handler);
            parser.parse();            
        }
        catch(ParseException e)
        {
            ex = e;
            System.out.println("Test2: " + e.toString());
        }
        assertNull(ex);
    }
        
    /**    
     CURRENTLY WE DO NOT USE PAGE NAME IN THE HEADER
     THERE IS A PROBLEM WITH THE GRAMMAR BEING ABLE TO EXCEPT ANY CHARACTER
     (ANY LANGUAGE) IN THE NAME VALUE
    public void testPageName()
    {
        AmbassadorDwUpEventHandler handler = new AmbassadorDwUpEventHandler();
        Reader input = new StringReader(m_testPageName);
        AmbassadorDwUpParser parser = new AmbassadorDwUpParser(input);       
    
        Exception ex = null;
        try
        {
            parser.setHandler(handler);
            parser.parse();            
        }
        catch(ParseException e)
        {
            ex = e;
            System.out.println("Test4: " + e.toString());
        }
        assertNull(ex);
    }
     **/
}
