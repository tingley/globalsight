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

package com.globalsight.cxe.entity.filterconfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import com.globalsight.ling.common.HtmlEntities;
import com.globalsight.ling.docproc.extractor.html.HtmlObjects;
import com.globalsight.ling.docproc.extractor.html.HtmlObjects.Attribute;
import com.globalsight.ling.docproc.extractor.html.HtmlObjects.ExtendedAttributeList;

public class HtmlInternalTag
{
    private String name;
    private Map<String, String> rules = new HashMap<String, String>();

    public static HtmlInternalTag string2tag(String tag)
            throws InternalTagException
    {
        return string2tag(tag, null);
    }

    public static HtmlInternalTag string2tag(String tag, ResourceBundle bundle)
            throws InternalTagException
    {
        HtmlInternalTag internalTag = new HtmlInternalTag();

        tag = tag.trim();
        
        if (tag.startsWith("&lt;"))
        {
            HtmlEntities entities = new HtmlEntities();
            tag = entities.decodeStringBasic(tag);
        }
        
        if (tag.startsWith("<"))
            tag = tag.substring(1);

        if (tag.endsWith(">"))
            tag = tag.substring(0, tag.length() - 1);

        tag = tag.trim();

        int state = 0;

        StringBuffer nameBuffer = new StringBuffer();
        StringBuffer attBuffer = new StringBuffer();
        StringBuffer valueBuffer = new StringBuffer();
        char split = '"';
        for (int i = 0; i < tag.length(); i++)
        {
            char c = tag.charAt(i);

            // start
            if (state == 0)
            {
                nameBuffer.append(c);

                if (c == ' ' || i == tag.length() - 1)
                {
                    String name = nameBuffer.toString().trim();
                    if (name.length() == 0)
                    {
                        String error = "Tag name can not be null";
                        if (bundle != null)
                        {
                            error = bundle
                                    .getString("msg_html_internal_tag_name_null");
                        }
                        throw new InternalTagException(error);
                    }

                    internalTag.name = name;
                    state = 1;
                }
            }
            // attribute name
            else if (state == 1)
            {
                if (i == tag.length() - 1)
                {
                    String error = "Attribute value can not be null";
                    if (bundle != null)
                    {
                        error = bundle
                                .getString("msg_html_internal_tag_value_null");
                    }
                    throw new InternalTagException(error);
                }

                if (c != '=' && i < tag.length() - 1)
                {
                    attBuffer.append(c);
                }
                else
                {
                    state = 2;
                    for (int j = i + 1; j < tag.length(); j++)
                    {
                        char c2 = tag.charAt(j);

                        if (c2 != '"' && c2 != ' ' && c2 != '\'')
                        {
                            i = j - 1;
                            break;
                        }

                        split = c2;
                    }
                }
            }
            // attribute value
            else if (state == 2)
            {
                if (c != split && i < tag.length() - 1)
                {
                    valueBuffer.append(c);
                }
                else
                {
                    if (i == tag.length() - 1 && c != split)
                    {
                        valueBuffer.append(c);
                    }

                    String att = attBuffer.toString().trim();
                    String value = valueBuffer.toString().trim();

                    if (att.length() == 0)
                    {
                        String error = "Attribute name can not be null";
                        if (bundle != null)
                        {
                            error = bundle
                                    .getString("msg_html_internal_tag_att_name_null");
                        }
                        throw new InternalTagException(error);
                    }

                    if (value.length() == 0)
                    {
                        String error = "Attribute value can not be null";
                        if (bundle != null)
                        {
                            error = bundle
                                    .getString("msg_html_internal_tag_value_null");
                        }
                        throw new InternalTagException(error);
                    }

                    internalTag.addRule(att, value);
                    state = 1;

                    attBuffer = new StringBuffer();
                    valueBuffer = new StringBuffer();
                }
            }
        }
        
        if (internalTag.name == null || internalTag.name.length() == 0)
        {
            String error = "Tag name can not be null";
            if (bundle != null)
            {
                error = bundle
                        .getString("msg_html_internal_tag_name_null");
            }
            throw new InternalTagException(error);
        }

        return internalTag;
    }

    public boolean accept(HtmlObjects.Tag tag)
    {
        if (!name.equalsIgnoreCase(tag.tag))
            return false;

        ExtendedAttributeList attributes = tag.attributes;

        Set<String> keys = rules.keySet();
        for (String key : keys)
        {
            Attribute attribute = attributes.getAttribute(key);
            if (attribute == null)
                return false;

            String value = attribute.getValue();
            value = value.trim();

            if (value.startsWith("\"") || value.startsWith("'"))
                value = value.substring(1);
            if (value.endsWith("\"") || value.endsWith("'"))
                value = value.substring(0, value.length() - 1);

            value = value.trim();

            if (attribute == null || !rules.get(key).equals(value))
            {
                return false;
            }
        }

        return true;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Map<String, String> getRules()
    {
        return rules;
    }

    public void addRule(String key, String value)
    {
        this.rules.put(key, value);
    }

    public void setRules(Map<String, String> rules)
    {
        this.rules = rules;
    }

    public String toString()
    {
        StringBuffer s = new StringBuffer();
        s.append("<").append(name);
        Set<String> keys = rules.keySet();
        for (String key : keys)
        {
            s.append(" ").append(key).append("=\"").append(rules.get(key))
                    .append("\"");
        }
        s.append(">");
        HtmlEntities entities = new HtmlEntities();
        return entities.encodeStringBasic(s.toString());
    }
}
