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

package galign.helpers.tmx;

import galign.helpers.tmx.TmxConstants;

import galign.helpers.util.EditUtil;
import galign.helpers.util.XmlParser;
import galign.helpers.util.XmlUtil;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import java.util.*;

/**
 * This class represents a TMX translation unit variant with embedded
 * G-TMX elements.
 *
 * @see http://www.lisa.org/tmx
 */
public class Tuv
    implements TmxConstants
{
    private String m_lang;
    private String m_datatype;
    private Element m_segment;

    //
    // Constructor
    //

    public Tuv()
    {
    }

    public Tuv(String p_language)
    {
        m_lang = p_language;
    }

    public Tuv(Element p_root)
    {
        init(p_root);
    }

    //
    // Public Methods
    //

    public String getLanguage()
    {
        return m_lang;
    }

    public String getText()
    {
       return m_segment.getText();
    }

    public void setText(String p_text)
    {
        m_segment.setText(p_text);
    }

    /**
     * Sets the segment to a DOM Element whose root element must be
     * &lt;seg&gt;.
     */
    public void setSegment(Element p_segment)
    {
        m_segment = p_segment;
    }

    /**
     * Sets the content to an XML string whose root element must be
     * "<seg>".
     */
    public void setSegment(String p_xml)
    {
        m_segment = XmlParser.parseXml(p_xml).getRootElement();
    }

    public String getXml()
    {
        StringBuffer result = new StringBuffer(128);

        result.append("    <tuv xml:lang=\"");
        result.append(m_lang);
        result.append("\"");
        if (m_datatype != null && m_datatype.length() > 0)
        {
            result.append(" datatype=\"");
            result.append(m_datatype);
            result.append("\"");
        }
        result.append(">\n      ");

        result.append(m_segment.asXML());

        result.append("\n    </tuv>\n");

        return result.toString();
    }

    //
    // Private Methods
    //

    private void init(Element p_root)
    {
        m_lang = p_root.attributeValue("lang");
        m_datatype = p_root.attributeValue("datatype");

        m_segment = p_root.element("seg")/*.clone()*/;
    }
}
