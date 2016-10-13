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
package com.globalsight.ling.docproc.extractor.xml;

import java.util.ArrayList;
import java.util.List;

import com.globalsight.util.StringUtil;

/**
 * The tag object in office 2010 embedded contents.
 * 
 */
public class OfficeXmlContentTag
{
    private String m_tag = null;
    private String m_name = null;
    private int m_bptIndex = -1;
    private boolean m_isMerged = false;
    private OfficeXmlContentTag m_pairedTag = null;
    private List<Attribute> m_attributeList = new ArrayList<Attribute>();

    public OfficeXmlContentTag(String tag)
    {
        m_tag = tag;
        generateTagName();
        generateAttributes();
    }

    public OfficeXmlContentTag(String tag, String tagName)
    {
        m_tag = tag;
        m_name = tagName;
        m_isMerged = true;
    }

    public void addAttribute(Attribute a)
    {
        m_attributeList.add(a);
    }

    public List<Attribute> getAttributeList()
    {
        return m_attributeList;
    }

    public String getTag()
    {
        return m_tag;
    }

    public String getName()
    {
        return m_name;
    }

    public int getBptIndex()
    {
        return m_bptIndex;
    }

    public OfficeXmlContentTag getPairedTag()
    {
        return m_pairedTag;
    }

    public boolean hasAttributes()
    {
        return m_attributeList.size() > 0;
    }

    public boolean isClosed()
    {
        return m_tag.endsWith("/>");
    }

    public boolean isEndTag()
    {
        return m_tag.startsWith("</");
    }

    public boolean isStartTag()
    {
        return !isEndTag() && !isClosed();
    }

    public boolean isMerged()
    {
        return m_isMerged;
    }

    public boolean isPaired()
    {
        return m_pairedTag != null;
    }

    public void setBptIndex(int bptIndex)
    {
        m_bptIndex = bptIndex;
    }

    public void setPairedTag(OfficeXmlContentTag pairedTag)
    {
        m_pairedTag = pairedTag;
    }

    public String toString()
    {
        return m_tag;
    }

    public void reset()
    {
        if (m_pairedTag != null)
        {
            m_pairedTag.setPairedTag(null);
        }
        m_pairedTag = null;
        m_attributeList.clear();
    }

    /**
     * Generates the attributes for {@link OfficeXmlContentTag}.
     */
    private void generateAttributes()
    {

        String tmp = m_tag.toString().replace("/>", "").replace(">", "");

        String tagName = getName();
        // remove the tag name
        tmp = tmp.substring(tmp.indexOf(tagName) + tagName.length());
        if (tmp.trim().isEmpty())
        {
            return;
        }

        if (!tmp.contains("="))
        {
            // <TAG NAME1 NAME2>
            String[] ss = tmp.split(" ");
            for (String s : ss)
            {
                addAttribute(new Attribute(s));
            }
        }

        while (tmp.contains("="))
        {
            // <TAG NAME1="VALUE1" NAME2="VALUE2">
            int equalIndex = tmp.indexOf("=");
            int spaceIndex = tmp.indexOf(" ");
            if (spaceIndex > -1 && spaceIndex < equalIndex)
            {
                // found an attribute
                String name = tmp.substring(spaceIndex, equalIndex).trim();
                if (name.indexOf(" ") > -1)
                {
                    String[] names = name.split(" ");
                    for (int i = 0; i < names.length - 1; i++)
                    {
                        addAttribute(new Attribute(names[i]));
                    }
                    name = names[names.length - 1];
                }
                tmp = tmp.substring(equalIndex + 1).trim();
                spaceIndex = tmp.indexOf(" ");
                equalIndex = tmp.indexOf("=");
                String value = "";
                char c = 0;
                if (tmp.length() > 0)
                {
                    c = tmp.charAt(0);
                }
                if (c == '\"' || c == '\'')
                {
                    String s = tmp.substring(1);
                    char c1 = 0;
                    if (tmp.length() > 1)
                    {
                        c1 = tmp.charAt(1);
                    }
                    if (c1 == '\"' || c1 == '\'')
                    {
                        s = tmp.substring(2);
                        String cc = String.valueOf(c) + String.valueOf(c1);
                        if (s.indexOf(cc) > -1)
                        {
                            value = tmp.substring(0, s.indexOf(cc) + 4).trim();
                        }
                        else if (s.indexOf(c) > -1)
                        {
                            value = tmp.substring(0, s.indexOf(c) + 3).trim();
                        }
                        else if (spaceIndex > -1)
                        {
                            value = tmp.substring(0, spaceIndex).trim();
                        }
                        else
                        {
                            value = tmp.trim();
                        }
                    }
                    else
                    {
                        if (s.indexOf(c) > -1)
                        {
                            value = tmp.substring(0, s.indexOf(c) + 2).trim();
                        }
                        else if (spaceIndex > -1)
                        {
                            value = tmp.substring(0, spaceIndex).trim();
                        }
                        else
                        {
                            value = tmp.trim();
                        }
                    }
                }
                else if (spaceIndex == -1 && equalIndex > -1)
                {
                    value = tmp.trim();
                }
                else if (spaceIndex > -1)
                {
                    value = tmp.substring(0, spaceIndex).trim();
                }
                else if (spaceIndex == -1 && equalIndex == -1)
                {
                    value = tmp.trim();
                }

                if (!StringUtil.isEmpty(name) && !StringUtil.isEmpty(value))
                {
                    addAttribute(new Attribute(name, value));
                }
                else if (StringUtil.isEmpty(value))
                {
                    name = name + "=";
                    addAttribute(new Attribute(name));
                }

                tmp = tmp.substring(tmp.indexOf(value) + value.length());
                if (tmp.length() > 0 && !tmp.contains("="))
                {
                    String[] attrs = tmp.trim().split(" ");
                    for (String attr : attrs)
                    {
                        addAttribute(new Attribute(attr));
                    }
                }
            }
            else
            {
                // avoid dead loop issue
                break;
            }
        }
    }

    /**
     * Generates the tag name for {@link OfficeXmlContentTag}.
     */
    private void generateTagName()
    {
        String tmp = m_tag.replace("</", "").replace("<", "").replace("/>", "")
                .replace(">", "").trim();
        if (!tmp.contains(" "))
        {
            m_name = tmp;
        }
        else
        {
            m_name = tmp.substring(0, tmp.indexOf(" "));
        }
    }

    /**
     * A simple attribute with a NAME and possibly a VALUE.
     */
    public class Attribute
    {
        private String name = null;
        private String value = null;

        public Attribute(String n)
        {
            name = n;
        }

        public Attribute(String n, String v)
        {
            name = n;
            value = v;
        }

        public String toString()
        {
            return (value != null ? name + "=" + value : name);
        }

        public String getValue()
        {
            return value;
        }

        public String getName()
        {
            return name;
        }

        public boolean hasValue()
        {
            return value != null;
        }
    }
}
