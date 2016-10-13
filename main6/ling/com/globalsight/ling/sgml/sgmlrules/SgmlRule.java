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

package com.globalsight.ling.sgml.sgmlrules;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import com.globalsight.everest.foundation.SearchCriteriaParameters;
import com.globalsight.ling.common.XmlEntities;
import com.globalsight.ling.sgml.GlobalSightDtd;
import com.globalsight.util.SortUtil;

public class SgmlRule implements Comparable
{
    static private XmlEntities s_codec = new XmlEntities();

    static private final String s_separator = "\t";

    static public class Element
    {
        public String m_name;
        public boolean m_extract;
        public boolean m_paired;
        public ArrayList m_attributes;

        public Element(String p_name, boolean p_extract, boolean p_paired)
        {
            m_name = p_name;
            m_extract = p_extract;
            m_paired = p_paired;
        }

        public Element()
        {
        }

        public Attribute getAttribute(String p_attrName)
        {
            for (int i = 0; i < m_attributes.size(); i++)
            {
                Attribute attr = (Attribute) m_attributes.get(i);

                if (attr.m_name.equals(p_attrName))
                {
                    return attr;
                }
            }

            return null;
        }
    }

    static public class Attribute
    {
        public String m_name;
        public String m_translatable;
        public String m_type;

        public Attribute()
        {
        }

        public Attribute(String p_name, String p_translatable, String p_type)
        {
            m_name = p_name;
            m_translatable = p_translatable;
            m_type = p_type;
        }
    }

    // DTD identifying fields
    private String m_publicId;
    private String m_systemId;

    // Actually a URL with file: protocol
    private String m_filename;

    // Transient fields only needed when parsing the DTD and creating rules.

    private HashMap m_rules = new HashMap();
    private GlobalSightDtd m_dtd;
    private Exception m_exception;

    //
    // Constructor
    //

    public SgmlRule()
    {
    }

    //
    // Public Methods
    //

    public void setPublicId(String p_arg)
    {
        m_publicId = p_arg;
    }

    public String getPublicId()
    {
        return m_publicId;
    }

    public void setSystemId(String p_arg)
    {
        m_systemId = p_arg;
    }

    public String getSystemId()
    {
        return m_systemId;
    }

    public void setFilename(String p_arg)
    {
        m_filename = p_arg;
    }

    public String getFilename()
    {
        return m_filename;
    }

    public void setDtd(GlobalSightDtd p_arg)
    {
        m_dtd = p_arg;
    }

    public GlobalSightDtd getDtd()
    {
        return m_dtd;
    }

    public void setException(Exception p_arg)
    {
        m_exception = p_arg;
    }

    public Exception getException()
    {
        return m_exception;
    }

    public ArrayList getElementNames()
    {
        ArrayList result = null;

        if (m_dtd != null)
        {
            result = m_dtd.getElementNames();
            SortUtil.sort(result);
        }

        return result;
    }

    public void updateElement(String p_origName, String p_name,
            boolean p_extract, boolean p_paired)
    {
        Element elem = (Element) m_rules.remove(p_origName);

        elem.m_name = p_name;
        elem.m_extract = p_extract;
        elem.m_paired = p_paired;

        m_rules.put(elem.m_name, elem);
    }

    public void addElement(String p_name, boolean p_extract, boolean p_paired)
    {
        Element elem = new Element(p_name, p_extract, p_paired);
        elem.m_attributes = new ArrayList();

        m_rules.put(elem.m_name, elem);
    }

    public void removeElement(String p_name)
    {
        m_rules.remove(p_name);
    }

    public void updateAttribute(String p_elem, String p_origName,
            String p_name, String p_trans, String p_type)
    {
        Element elem = (Element) m_rules.get(p_elem);
        Attribute attr = elem.getAttribute(p_origName);
        attr.m_name = p_name;
        attr.m_translatable = p_trans;
        attr.m_type = p_type;
    }

    public void addAttribute(String p_elemName, String p_name, String p_trans,
            String p_type)
    {
        Element elem = (Element) m_rules.get(p_elemName);
        Attribute attr = new Attribute(p_name, p_trans, p_type);
        elem.m_attributes.add(attr);
    }

    /*
     * Remove an attribute from an element.
     * 
     * @param p_elemName Name of Element
     * 
     * @param p_name Name of Attribute
     */
    public void removeAttribute(String p_elemName, String p_name)
    {
        Element elem = (Element) m_rules.get(p_elemName);

        for (int i = 0; i < elem.m_attributes.size(); i++)
        {
            Attribute attr = (Attribute) elem.m_attributes.get(i);

            if (attr.m_name.equals(p_name))
            {
                elem.m_attributes.remove(i);
                break;
            }
        }
    }

    /*
     * Return only the element names that match the criteria. Return all
     * elements if criteria is empty.
     * 
     * @param p_criteria (begins with, ends with, contains)
     * 
     * @param p_value the text to match
     */
    public ArrayList getData(String p_criteria, String p_value)
    {
        ArrayList allElements = new ArrayList();
        Set keys = m_rules.keySet();
        Iterator iter = keys.iterator();
        while (iter.hasNext())
        {
            allElements.add(m_rules.get((String) iter.next()));
        }

        if (p_criteria == null || p_criteria.equals(""))
        {
            return allElements;
        }

        ArrayList result = new ArrayList();

        if (p_criteria.equals(SearchCriteriaParameters.BEGINS_WITH))
        {
            for (int i = 0; i < allElements.size(); i++)
            {
                Element elem = (Element) allElements.get(i);

                if (elem.m_name.startsWith(p_value))
                {
                    result.add(elem);
                }
            }
        }
        else if (p_criteria.equals(SearchCriteriaParameters.ENDS_WITH))
        {
            for (int i = 0; i < allElements.size(); i++)
            {
                Element elem = (Element) allElements.get(i);

                if (elem.m_name.endsWith(p_value))
                {
                    result.add(elem);
                }
            }
        }
        else
        {
            for (int i = 0; i < allElements.size(); i++)
            {
                Element elem = (Element) allElements.get(i);

                if (elem.m_name.indexOf(p_value) != -1)
                {
                    result.add(elem);
                }
            }
        }

        return result;
    }

    public ArrayList getAttributeNames(String p_element)
    {
        ArrayList result = null;

        if (m_dtd != null)
        {
            result = m_dtd.getAttributeNames(p_element);
            SortUtil.sort(result);
        }

        return result;
    }

    public Attribute getAttributeRule(String p_elementName, String p_attr)
    {
        Element elem = (Element) m_rules.get(p_elementName);
        return elem.getAttribute(p_attr);
    }

    public Element getElementRule(String p_elementName)
    {
        return (Element) m_rules.get(p_elementName);
    }

    public void setRules(Properties p_props)
    {
        m_rules = new HashMap();

        for (Enumeration e = p_props.propertyNames(); e.hasMoreElements();)
        {
            String key = (String) e.nextElement();
            String value = p_props.getProperty(key);
            // elem and attr name are the key
            String[] names = key.split(s_separator);

            Element elem = (Element) m_rules.get(names[0]);
            if (elem == null)
            {
                elem = new Element();
                elem.m_name = names[0];
            }

            String[] tmp = value.split(",");
            elem.m_extract = Boolean.valueOf(tmp[0]).booleanValue();
            elem.m_paired = Boolean.valueOf(tmp[1]).booleanValue();

            ArrayList attrs = elem.m_attributes;
            if (attrs == null)
            {
                attrs = new ArrayList();
                elem.m_attributes = attrs;
            }

            if (names.length == 2)
            {
                Attribute attr = new Attribute();
                attr.m_name = names[1];
                attr.m_translatable = tmp[2];
                attr.m_type = tmp[3];
                attrs.add(attr);
            }

            m_rules.put(elem.m_name, elem);
        }
    }

    public Properties getRules()
    {
        Properties result = new Properties();

        StringBuffer value = new StringBuffer();

        for (Iterator it = m_rules.keySet().iterator(); it.hasNext();)
        {
            String elemName = (String) it.next();
            Element elem = (Element) m_rules.get(elemName);
            ArrayList attrs = elem.m_attributes;
            if (attrs.size() > 0)
            {
                for (int i = 0; i < attrs.size(); i++)
                {
                    value.setLength(0);
                    Attribute attr = (Attribute) attrs.get(i);
                    String key = elemName + "\t" + attr.m_name;
                    value.append(elem.m_extract);
                    value.append(",");
                    value.append(elem.m_paired);
                    value.append(",");
                    value.append(attr.m_translatable);
                    value.append(",");
                    value.append(attr.m_type);
                    result.put(key, value.toString());
                }
            }
            else
            {
                value.setLength(0);
                value.append(elem.m_extract);
                value.append(",");
                value.append(elem.m_paired);
                result.put(elemName, value.toString());
            }
        }

        return result;
    }

    public int compareTo(Object p_other)
    {
        return this.m_publicId.compareTo(((SgmlRule) p_other).m_publicId);
    }

    public void initData()
    {
        m_rules = new HashMap();

        ArrayList elems = getElementNames();
        if (elems == null)
        {
            return;
        }

        for (int i = 0, maxi = elems.size(); i < maxi; i++)
        {
            String elem = (String) elems.get(i);
            Element newElem = new Element(elem, false, true);

            ArrayList attrs = getAttributeNames(elem);
            if (attrs != null)
            {
                ArrayList attributes = new ArrayList();

                for (int j = 0, maxj = attrs.size(); j < maxj; j++)
                {
                    String attr = (String) attrs.get(j);
                    Attribute newAttr = new Attribute();
                    newAttr.m_name = attr;
                    newAttr.m_translatable = "no";
                    newAttr.m_type = "text";
                    attributes.add(newAttr);
                }

                newElem.m_attributes = attributes;
            }

            m_rules.put(elem, newElem);
        }
    }

    // for debugging
    public void dumpHash(String from)
    {
        System.out.println("dump " + from);

        for (Iterator it = m_rules.keySet().iterator(); it.hasNext();)
        {
            String key = (String) it.next();
            Element elem = (Element) m_rules.get(key);

            System.out.println("key=" + key);

            ArrayList attrs = elem.m_attributes;
            if (attrs != null)
            {
                for (int i = 0; i < attrs.size(); i++)
                {
                    Attribute attr = (Attribute) attrs.get(i);

                    System.out.println("\t" + attr.m_name);
                }
            }
        }
    }
}
