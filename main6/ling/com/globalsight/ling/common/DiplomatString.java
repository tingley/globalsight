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
package com.globalsight.ling.common;

import com.globalsight.ling.common.DiplomatBasicParser;
import com.globalsight.ling.common.DiplomatBasicParserException;
import com.globalsight.ling.common.DiplomatBasicHandler;

import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

/**
 * A diplomat string class which provides basic operations such as
 * equals() and caseInsensitiveEquals().
 *
 * <p> Note: All "i" attributes are normalized to zero for comparison.
 */
public class DiplomatString
    implements DiplomatBasicHandler
{
    private boolean m_bParsingFirstString = true;
    private Vector m_vSrc = null;
    private Vector m_vTrg = null;
    private String m_strSrc = "";
    private int m_nMethodCall = -1;

    // METHOD CALL IDS
    private static final int PARSE_SRC = 1;
    private static final int PARSE_SRC_IGNORE_CASE = 2;
    private static final int EQUALS = 3;
    private static final int EQUALS_IGNORE_CASE = 4;

    /**
     * DiplomatStringCompare constructor comment.
     */
    public DiplomatString(String p_DiplomatIn)
    {
        super();
        m_strSrc = p_DiplomatIn;
    }

    /**
     * Event handler called by the diplomat basic parser.
     */
    public void handleEndTag(String p_name, String p_originalTag)
        throws DiplomatBasicParserException
    {
        switch (m_nMethodCall)
        {
        case PARSE_SRC_IGNORE_CASE:
            m_vSrc.addElement(p_name.toLowerCase());
            break;
        case PARSE_SRC:
            m_vSrc.addElement(p_name);
            break;
        case EQUALS:
            m_vTrg.addElement(p_name);
            break;
        case EQUALS_IGNORE_CASE:
            m_vTrg.addElement(p_name.toLowerCase());
            break;
        default:
            break;
        }
    }

    /**
     * Event handler called by the diplomat basic parser.
     * @exception DiplomatBasicParserException The exception description.
     */
    public void handleStart()
        throws DiplomatBasicParserException
    {
    }

    /**
     * Event handler called by the diplomat basic parser.
     * @param p_strName java.lang.String
     * @param p_hAtributes Hashtable
     * @param p_strOriginalString java.lang.String
     */
    public void handleStartTag(String p_name, Properties p_attributes,
        String p_originalString)
        throws DiplomatBasicParserException
    {
        // remove all x attributes becuase they are optional
        // addables do not inject them
        if (p_attributes.get("x") != null)
        {
            p_attributes.remove("x");
        }

        switch(m_nMethodCall)
        {
        case PARSE_SRC_IGNORE_CASE:
        {
            Enumeration keys = p_attributes.keys();
            while(keys.hasMoreElements())
            {
                String nextKey = (String)keys.nextElement();
                String val = (String)p_attributes.get(nextKey);
                p_attributes.put(nextKey, val.toLowerCase() );
            }
        }
        // intentional fall through to PARSE_SRC
        case PARSE_SRC:
            if(p_attributes.get("i") != null)
            {
                // normalize i attribute
                p_attributes.put("i","0");
            }

            m_vSrc.addElement(p_name);
            m_vSrc.addElement(p_attributes);
            break;
        case EQUALS_IGNORE_CASE:
        {
            Enumeration keys = p_attributes.keys();
            while(keys.hasMoreElements())
            {
                String nextKey = (String)keys.nextElement();
                String val = (String)p_attributes.get(nextKey);
                p_attributes.put(nextKey, val.toLowerCase() );
            }
        }
        // intentional fall through to EQUALS
        case EQUALS :
            if(p_attributes.get("i") != null)
            {
                // normalize i attribute
                p_attributes.put("i","0");
            }
            m_vTrg.addElement(p_name);
            m_vTrg.addElement(p_attributes);
            break;
        default:
            break;
        }
    }

    /**
     * Event handler called by the diplomat basic parser.
     * @exception DiplomatBasicParserException The exception description.
     */
    public void handleStop()
        throws DiplomatBasicParserException
    {

    }

    /**
     * Event handler called by the diplomat basic parser.
     */
    public void handleText(String p_text)
        throws DiplomatBasicParserException
    {
        switch (m_nMethodCall)
        {
        case PARSE_SRC_IGNORE_CASE:
            m_vSrc.addElement(p_text.toLowerCase());
            break;
        case PARSE_SRC:
            m_vSrc.addElement(p_text);
            break;
        case EQUALS :
            m_vTrg.addElement(p_text);
            break;
        case EQUALS_IGNORE_CASE :
            m_vTrg.addElement(p_text.toLowerCase());
            break;
        default:
            break;
        }
    }

    /**
     * Does a case sensitive comparison of text, tag and attributes.<p>
     * <p>
     * NOTE: the "i" attribute is normalized to zero for comparison.
     */
    public boolean equals(String p_strTrg)
        throws DiplomatBasicParserException
    {
        m_vSrc = new Vector();
        m_vTrg = new Vector();

        m_nMethodCall = EQUALS;

        if(m_vSrc == null || m_vSrc.size() <= 0)
            parseSource();

        DiplomatBasicParser parser = new DiplomatBasicParser(this);
        parser.parse(p_strTrg);

        return (m_vSrc.equals(m_vTrg));
    }

    /**
     * Does a case insensitive comparison of text, tag and attributes.<p>
     * <p>
     * NOTE: the "i" attribute is normalized to zero for comparison.
     */
    public boolean equalsIgnoreCase(String p_strTrg)
        throws DiplomatBasicParserException
    {
        m_vSrc = new Vector();
        m_vTrg = new Vector();

        m_nMethodCall = EQUALS_IGNORE_CASE;

        if(m_vSrc == null || m_vSrc.size() <= 0)
            parseSource();

        DiplomatBasicParser parser = new DiplomatBasicParser(this);
        parser.parse(p_strTrg);

        return ( m_vSrc.equals(m_vTrg) );
    }

    /**
     * DiplomatStringCompare constructor comment.
     */
    private void parseSource()
        throws DiplomatBasicParserException
    {
        int tmp = m_nMethodCall;

        if (tmp == EQUALS_IGNORE_CASE)
        {
            m_nMethodCall = PARSE_SRC_IGNORE_CASE;
        }
        else
        {
            m_nMethodCall = PARSE_SRC;
        }

        DiplomatBasicParser parser = new DiplomatBasicParser(this);
        parser.parse(m_strSrc);

        m_nMethodCall = tmp;
    }
}
