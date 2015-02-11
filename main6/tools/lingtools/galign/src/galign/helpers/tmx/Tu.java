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
import galign.helpers.tmx.Tuv;

import galign.helpers.util.EditUtil;
import galign.helpers.util.XmlParser;

//import org.dom4j.tree.AbstractElement;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import java.util.*;

/**
 * This class represents a TMX translation unit.
 *
 * @see http://www.lisa.org/tmx
 */
public class Tu
    implements TmxConstants
{
    private String m_tuid;
    private String m_srclang;
    private String m_datatype;

    private ArrayList m_props = new ArrayList();
    private ArrayList m_tuvs = new ArrayList();

    //
    // Constructor
    //

    public Tu()
    {
    }

    public Tu(Element p_root)
    {
        init(p_root);
    }

    public Tu(String p_arg)
    {
        m_tuid = p_arg;
    }

    //
    // Public Methods
    //

    public String getId()
    {
        return m_tuid;
    }

    public ArrayList getTuvs()
    {
        return m_tuvs;
    }

    public void clearTuvs()
    {
        m_tuvs.clear();
    }

    public void addTuv(Tuv p_arg)
    {
        m_tuvs.add(p_arg);
    }

    public Tuv getTuv(String p_language)
    {
        for (int i = 0, max = m_tuvs.size(); i < max; i++)
        {
            Tuv tuv = (Tuv)m_tuvs.get(i);

            if (tuv.getLanguage().equals(p_language))
            {
                return tuv;
            }
        }

        return null;
    }

    public String getXml()
    {
        StringBuffer result = new StringBuffer(128);

        result.append("  <tu");
        if (m_tuid != null && m_tuid.length() > 0)
        {
            result.append(" tuid=\"");
            result.append(m_tuid);
            result.append("\"");
        }
        if (m_srclang != null && m_srclang.length() > 0)
        {
            result.append(" srclang=\"");
            result.append(m_srclang);
            result.append("\"");
        }
        if (m_datatype != null && m_datatype.length() > 0)
        {
            result.append(" datatype=\"");
            result.append(m_datatype);
            result.append("\"");
        }
        result.append(">\n");

        for (int i = 0, max = m_props.size(); i < max; ++i)
        {
            Element element = (Element)m_props.get(i);
            result.append("    ");
            result.append(element.asXML());
            result.append("\n");
        }

        for (int i = 0, max = m_tuvs.size(); i < max; ++i)
        {
            result.append(((Tuv)m_tuvs.get(i)).getXml());
        }

        result.append("  </tu>\n");

        return result.toString();
    }

    //
    // Private Methods
    //

    protected void init(Element p_root)
    {
        clearTuvs();

        m_tuid = p_root.attributeValue("tuid");
        m_srclang = p_root.attributeValue("srclang");
        m_datatype = p_root.attributeValue("datatype");

        for (Iterator i = p_root.elementIterator("prop"); i.hasNext();)
        {
            Element item = (Element)i.next();
            m_props.add(item);
        }

        for (Iterator i = p_root.elementIterator("tuv"); i.hasNext();)
        {
            Element item = (Element)i.next();
            Tuv tuv = new Tuv(item);

            addTuv(tuv);
        }
    }
}
