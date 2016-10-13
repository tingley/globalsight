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

package com.globalsight.terminology;

import com.globalsight.terminology.TermbaseException;
import com.globalsight.terminology.util.XmlParser;

import com.globalsight.util.edit.EditUtil;

import org.dom4j.Document;
import org.dom4j.Element;

/**
 * <p>A terminology entry in a termbase. Represented both as an XML
 * string for client-side XML processing and as a DOM object that can
 * be queried and modified from within server-side Java code.</p>
 */
public class Entry
{
    //
    // Private & Protected Constants
    //
    private long m_id = 0;

    private String m_xml = "";
    private Document m_dom = null;

    private boolean m_xmlIsDirty = false;
    private boolean m_domIsDirty = false;

    //
    // Constructors
    //

    public Entry() {}

    public Entry(String p_xml)
    {
        m_xml = p_xml;
        setDomIsDirty();
    }

    public Entry(Document p_dom)
    {
        m_dom = p_dom;
        setXmlIsDirty();
    }

    //
    // Public Methods
    //

    public void clear()
    {
        m_xml = "";
        m_dom = null;

        m_xmlIsDirty = false;
        m_domIsDirty = false;
    }

    public long getId()
    {
        return m_id;
    }

    public void setId(long p_id)
    {
        if (m_id != p_id)
        {
            m_id = p_id;
        }
    }

    public String getXml()
    {
        if (m_xml == null || m_xmlIsDirty == true)
        {
            m_xml = m_dom.getRootElement().asXML();
        }

        return m_xml;
    }

    public Document getDom()
        throws TermbaseException
    {
        if (m_dom == null || m_domIsDirty == true)
        {
            XmlParser parser = null;

            try
            {
                parser = XmlParser.hire();
                m_dom = parser.parseXml(m_xml);
            }
            finally
            {
                XmlParser.fire(parser);
            }
        }

        return m_dom;
    }

    public void setXml(String p_xml)
    {
        m_xml = p_xml;
        setDomIsDirty();
    }

    public void setDom(Document p_dom)
    {
        m_dom = p_dom;
        setXmlIsDirty();
    }

    //
    // Private Methods
    //
    private void setXmlIsDirty()
    {
        m_xmlIsDirty = true;
        m_domIsDirty = false;
    }

    private void setDomIsDirty()
    {
        m_domIsDirty = true;
        m_xmlIsDirty = false;
    }
}
