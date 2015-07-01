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

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

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
import org.xml.sax.SAXException;

/**
 * The helper for config xml in base filter
 * 
 * <BaseFilterConfig> <internalTexts>a</internalTexts> </BaseFilterConfig>
 * 
 */
public class PlainTextFilterParser
{
    public static final String NODE_ROOT = "PlainTextFilterConfig";
    public static final String NODE_CUSTOM = "customTextRules";
    public static final String NODE_SID = "customTextRuleSids";
    public static final String NODE_ELEMENT_POST_FILTER = "elementPostFilter";
    public static final String NODE_ELEMENT_POST_FILTER_ID = "elementPostFilterId";

    public static final String nullConfigXml = "<" + NODE_ROOT + ">" + "</"
            + NODE_ROOT + ">";

    private static final Logger CATEGORY = Logger
            .getLogger(PlainTextFilterParser.class);

    private Document m_document = null;
    private Element m_rootElement = null;
    private String m_configXml = null;
    private String m_elementPostFilter = null;
    private String m_elementPostFilterId = null;

    public PlainTextFilterParser(PlainTextFilter f)
    {
        this(f == null ? null : f.getConfigXml());
    }

    public PlainTextFilterParser(String configXml)
    {
        m_configXml = (configXml == null || "".equals(configXml.trim())) ? nullConfigXml
                : configXml;
    }

    public String getConfigXml()
    {
        return m_configXml;
    }

    // {tagName : "name1", itemid : 1, attributes : [{itemid : 0, aName :
    // "name1", aOp : "equal", aValue : "vvv1"}]}
    public static String toXml(JSONArray customTextRules, JSONArray customTextRuleSids, 
            String elementPostFilter, String elementPostFilterId)
            throws Exception
    {
        StringBuffer sb = new StringBuffer();
        sb.append("<").append(NODE_ROOT).append(">");
        sb.append("<").append(NODE_CUSTOM).append(">");
        sb.append(customTextRules == null ? ""
                : jsonArrayToXml(customTextRules));
        sb.append("</").append(NODE_CUSTOM).append(">");
        
        sb.append("<").append(NODE_SID).append(">");
        sb.append(customTextRuleSids == null ? ""
                : jsonArrayToXml(customTextRuleSids));
        sb.append("</").append(NODE_SID).append(">");
        
        sb.append("<").append(NODE_ELEMENT_POST_FILTER).append(">");
        sb.append(elementPostFilter);
        sb.append("</").append(NODE_ELEMENT_POST_FILTER).append(">");
        sb.append("<").append(NODE_ELEMENT_POST_FILTER_ID).append(">");
        sb.append(elementPostFilterId);
        sb.append("</").append(NODE_ELEMENT_POST_FILTER_ID).append(">");
        sb.append("</").append(NODE_ROOT).append(">");

        return sb.toString();
    }

    public void parserXml() throws SAXException, IOException
    {
        StringReader sr = new StringReader(m_configXml);
        InputSource is = new InputSource(sr);
        DOMParser parser = new DOMParser();
        parser.setFeature("http://xml.org/sax/features/validation", false);
        parser.parse(is);
        m_document = parser.getDocument();
        m_rootElement = m_document.getDocumentElement();
    }

    public List<CustomTextRuleBase> getCustomTextRules()
    {
        List<CustomTextRuleBase> result = new ArrayList<CustomTextRuleBase>();

        List<CustomTextRule> rrr = getBaseFilterTagsFromXml(NODE_CUSTOM, new CustomTextRule());
        
        for (CustomTextRule customTextRule : rrr)
        {
            result.add(customTextRule);
        }

        return result;
    }
    
    public List<CustomTextRuleBase> getCustomTextRuleSids()
    {
        List<CustomTextRuleBase> result = new ArrayList<CustomTextRuleBase>();
        List<CustomTextRuleSid> rrr = getBaseFilterTagsFromXml(NODE_SID, new CustomTextRuleSid());

        for (CustomTextRuleSid customTextRule : rrr)
        {
            result.add(customTextRule);
        }
        
        return result;
    }

    private <T> List<T> getBaseFilterTagsFromXml(String nodename, T t)
    {
        List<T> result = new ArrayList<T>();
        Element element = getSingleElement(nodename);
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
            if (t instanceof CustomTextRule)
            {
                t2 = (T) CustomTextRule.initFromElement(tagElement);
            }
            
            if (t instanceof CustomTextRuleSid)
            {
                t2 = (T) CustomTextRuleSid.initFromElement(tagElement);
            }

            if (t2 != null)
            {
                result.add(t2);
            }
        }

        return result;
    }

    public String getElementPostFilterTableName()
    {
        if (m_elementPostFilter == null)
        {
            String result = getSingleElementValue(NODE_ELEMENT_POST_FILTER);
            m_elementPostFilter = (result == null ? "" : result);
        }

        return m_elementPostFilter;
    }

    public Filter getElementPostFilter() throws Exception
    {
        long postFilterId = -1;
        String v = getElementPostFilterId();
        try
        {
            postFilterId = Long.parseLong(v);
        }
        catch (Exception e)
        {
        }
        String filterTableName = getElementPostFilterTableName();

        if (postFilterId >= 0 && filterTableName != null)
        {
            return FilterHelper.getFilter(filterTableName, postFilterId);
        }
        else
        {
            return null;
        }
    }

    public String getElementPostFilterId()
    {
        if (m_elementPostFilterId == null)
        {
            String result = getSingleElementValue(NODE_ELEMENT_POST_FILTER_ID);
            m_elementPostFilterId = (result == null ? "" : result);
        }

        return m_elementPostFilterId;
    }

    public String getCustomTextRulesJson()
    {
        try
        {
            Element element = getSingleElement(NODE_CUSTOM);
            String[] toArray = null;
            return tagsXmlToJsonArray(element, toArray);
        }
        catch (Exception e)
        {
            CATEGORY.error("Error occur when xml to json", e);
            return "[]";
        }
    }
    
    public String getCustomTextRuleSidsJson()
    {
        try
        {
            Element element = getSingleElement(NODE_SID);
            String[] toArray = null;
            return tagsXmlToJsonArray(element, toArray);
        }
        catch (Exception e)
        {
            CATEGORY.error("Error occur when xml to json", e);
            return "[]";
        }
    }

    public String getNewConfigXml(String elementName, String newValue)
            throws Exception
    {
        setSingleElementValue(elementName, newValue);

        return documentToStr();
    }

    public String documentToStr() throws Exception
    {
        String returnStr = null;
        StringWriter strWtr = new StringWriter();
        StreamResult strResult = new StreamResult(strWtr);
        TransformerFactory tfac = TransformerFactory.newInstance();
        Transformer t = tfac.newTransformer();
        t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        t.transform(new DOMSource(m_rootElement), strResult);
        String result = strResult.getWriter().toString().trim();

        returnStr = result.substring(result.indexOf("<" + NODE_ROOT + ">"),
                result.length());
        strWtr.close();

        return returnStr;
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

        String newXml = innerXml.replace("<finishString/>",
                "<finishString></finishString>");
        StringBuffer ret = new StringBuffer(
                (XML.toJSONObject(newXml)).toString());

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

    private static String jsonArrayToXml(JSONArray jsonArrayPreserveWsTags)
            throws Exception
    {
        // XmlUtil.escapeString(exWhiteSpaceChars)
        String ret = XML.toString(jsonArrayPreserveWsTags);
        return ret;
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
