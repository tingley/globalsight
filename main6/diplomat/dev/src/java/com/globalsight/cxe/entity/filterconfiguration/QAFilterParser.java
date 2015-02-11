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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.axis.utils.XMLUtils;
import org.apache.log4j.Logger;
import org.apache.xerces.parsers.DOMParser;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.XML;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class QAFilterParser implements QAFilterConstants
{
    private static final Logger CATEGORY = Logger
            .getLogger(QAFilterParser.class);

    private Document m_document = null;
    private Element m_rootElement = null;
    private String m_configXml = null;

    public QAFilterParser(QAFilter qaFilter)
    {
        this(qaFilter == null ? null : qaFilter.getConfigXml());
    }

    public QAFilterParser(String configXml)
    {
        m_configXml = (configXml == null || "".equals(configXml.trim())) ? nullConfigXml
                : configXml;
    }

    public String getConfigXml()
    {
        return m_configXml;
    }

    public static String toXml(JSONArray rules, JSONArray defaultRules)
            throws Exception
    {
        StringBuilder sb = new StringBuilder();
        sb.append("<").append(NODE_ROOT).append(">");
        sb.append("<").append(NODE_RULES).append(">");
        sb.append(rules == null ? "" : jsonArrayToXml(rules));
        sb.append("</").append(NODE_RULES).append(">");
        sb.append("<").append(NODE_RULES_DEFAULT).append(">");
        sb.append(defaultRules == null ? "" : jsonArrayToXml(defaultRules));
        sb.append("</").append(NODE_RULES_DEFAULT).append(">");
        sb.append("</").append(NODE_ROOT).append(">");

        return sb.toString();
    }

    public void parserXml() throws Exception
    {
        StringReader sr = new StringReader(m_configXml);
        InputSource is = new InputSource(sr);
        DOMParser parser = new DOMParser();
        parser.setFeature("http://xml.org/sax/features/validation", false);
        parser.parse(is);
        m_document = parser.getDocument();
        m_rootElement = m_document.getDocumentElement();
    }

    public List<QARule> getQARules()
    {
        List<QARule> result = new ArrayList<QARule>();

        result = getQAFilterTagsFromXml(NODE_RULES, new QARule());

        return result;
    }

    public List<QARuleDefault> getQARulesDefault()
    {
        List<QARuleDefault> result = new ArrayList<QARuleDefault>();

        result = getQAFilterTagsFromXml(NODE_RULES_DEFAULT, new QARuleDefault());

        return result;
    }

    public String getRulesJson()
    {
        try
        {
            Element element = getSingleElement(NODE_RULES);
            String[] toArray = null;
            return tagsXmlToJsonArray(element, toArray);
        }
        catch (Exception e)
        {
            CATEGORY.error("Error getting rules json. ", e);
            return "[]";
        }
    }

    public String getDefaultRulesJson()
    {
        try
        {
            Element element = getSingleElement(NODE_RULES_DEFAULT);
            String[] toArray = null;
            return tagsXmlToJsonArray(element, toArray);
        }
        catch (Exception e)
        {
            CATEGORY.error("Error getting default rules json. ", e);
            return "[]";
        }
    }

    private <T> List<T> getQAFilterTagsFromXml(String nodeName, T t)
    {
        List<T> result = new ArrayList<T>();

        Element element = getSingleElement(nodeName);
        if (element == null)
        {
            return result;
        }

        NodeList nodes = element.getElementsByTagName("array");
        if (nodes == null || nodes.getLength() <= 0)
        {
            return result;
        }
        for (int i = 0; i < nodes.getLength(); i++)
        {
            Node tagNode = nodes.item(i);
            if (tagNode.getNodeType() != Node.ELEMENT_NODE)
            {
                continue;
            }

            Element tagElement = (Element) tagNode;
            if (!isEnabledTag(tagElement))
            {
                continue;
            }

            T t2 = null;
            if (t instanceof QARule)
            {
                t2 = (T) QARule.initFromElement(tagElement);
            }
            else if (t instanceof QARuleDefault)
            {
                t2 = (T) QARuleDefault.initFromElement(tagElement);
            }

            if (t2 != null)
            {
                result.add(t2);
            }
        }

        return result;
    }

    public String setSingleElementValue(String p_elementName, String p_value)
    {
        return setSingleElementValue(m_rootElement, p_elementName, p_value);
    }

    public String getSingleElementValue(String p_elementName)
    {
        return getSingleElementValue(m_rootElement, p_elementName);
    }

    public Element getSingleElement(String p_elementName)
    {
        return getSingleElement(m_rootElement, p_elementName);
    }

    /**
     * Gets the value of a single element. For example,
     * getSingleElementValue(rootElement,"postMergeEvent")
     * 
     * @param p_rootElement
     *            -- the root to use in order to find p_elementName
     * @param p_elementName
     *            -- the element whose value to get
     * @return the value of the element
     */
    public static String getSingleElementValue(Element p_rootElement,
            String p_elementName)
    {
        Element e = getSingleElement(p_rootElement, p_elementName);
        return (e == null || e.getFirstChild() == null) ? null : e
                .getFirstChild().getNodeValue();
    }

    /**
     * Sets the value of a single element. For example,
     * setSingleElementValue(rootElement, "postMergeEvent", postMergeEvent)
     * 
     * @param p_rootElement
     *            -- the root to use in order to find p_elementName
     * @param p_elementName
     *            -- the element whose value to set
     * @param p_value
     *            -- the single value for this element
     * @return the old value of the element
     */
    public static String setSingleElementValue(Element p_rootElement,
            String p_elementName, String p_value)
    {
        Element e = getSingleElement(p_rootElement, p_elementName);
        String originalValue = e.getFirstChild().getNodeValue();
        e.getFirstChild().setNodeValue(p_value);
        return originalValue;
    }

    /**
     * Gets the named element from the specified DOM structure
     * 
     * @param p_elementName
     *            -- the name of the element to get
     * @return the Element
     */
    public static Element getSingleElement(Element p_rootElement,
            String p_elementName)
    {
        NodeList list = p_rootElement.getElementsByTagName(p_elementName);
        return (list != null && list.getLength() > 0) ? (Element) list.item(0)
                : null;
    }

    public static String tagsXmlToJsonArray(Element element, String[] toArray)
            throws JSONException
    {
        if (element == null)
        {
            return "[]";
        }

        String innerXml = XMLUtils.getInnerXMLString(element);

        if (innerXml == null || !innerXml.startsWith("<array"))
        {
            return "[]";
        }

        StringBuffer ret = new StringBuffer(
                (XML.toJSONObject(innerXml)).toString());

        if (ret.indexOf("{\"array\":") == 0)
        {
            ret.delete(0, 9);
            ret.deleteCharAt(ret.length() - 1);
        }

        if (ret.charAt(0) != '[')
        {
            ret.insert(0, '[');
            ret.append(']');
        }

        if (toArray != null)
        {
            for (String taa : toArray)
            {
                tagsXmlToJsonArrayFixObject(ret, taa);
            }
        }

        return ret.toString();
    }

    public static void tagsXmlToJsonArrayFixObject(StringBuffer ret,
            String keyWord)
    {
        int keyLen = keyWord.length();
        int index = ret.lastIndexOf(keyWord);
        if (index != -1)
        {
            while (index != -1)
            {
                int preCurlyBracesCount = 0;
                int quotCount = 0;
                int i = index + keyLen;
                for (; i < ret.length(); i++)
                {
                    boolean notInQuot = (quotCount % 2 == 0);
                    char c = ret.charAt(i);
                    if (c == '}' && preCurlyBracesCount == 0 && notInQuot)
                    {
                        ret.insert(i + 1, ']');
                        break;
                    }
                    else if (c == '}' && notInQuot)
                    {
                        preCurlyBracesCount--;
                    }
                    else if (c == '{' && notInQuot)
                    {
                        preCurlyBracesCount++;
                    }
                    else if (c == '"' && ret.charAt(i - 1) != '\\')
                    {
                        quotCount++;
                    }
                }

                ret.insert(index + keyLen - 1, '[');
                index = ret.lastIndexOf(keyWord);
            }
        }
    }

    public static String jsonArrayToXml(JSONArray jsonArray) throws Exception
    {
        return XML.toString(jsonArray);
    }

    private boolean isEnabledTag(Element element)
    {
        if (element == null)
        {
            return false;
        }

        NodeList ees = element.getElementsByTagName("enable");
        if (ees == null || ees.getLength() == 0)
        {
            return false;
        }

        try
        {
            Node enableEle = ees.item(0);
            String enable = enableEle.getFirstChild().getNodeValue();
            return "true".equals(enable);
        }
        catch (Exception e)
        {
        }

        return false;
    }
}
