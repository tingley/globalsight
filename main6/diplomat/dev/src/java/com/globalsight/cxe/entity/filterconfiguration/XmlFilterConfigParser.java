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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.globalsight.diplomat.util.XmlUtil;
import com.globalsight.everest.util.comparator.StringComparator;
import com.globalsight.util.SortUtil;

/**
 * The helper for config xml in xml filter
 * 
 * <xmlFilterConfig> <extendedWhitespaceChars>a</extendedWhitespaceChars>
 * </xmlFilterConfig>
 * 
 */
public class XmlFilterConfigParser implements XmlFilterConstants
{
    private static final Logger CATEGORY = Logger
            .getLogger(XmlFilterConfigParser.class);

    private Document m_document = null;
    private Element m_rootElement = null;
    private String m_configXml = null;

    private String m_extendedWhiteSpaceChars = null;
    private int m_phConsolidationMode = -1;
    private int m_phTrimMode = -1;
    private int m_nonasciiAs = -1;
    private int m_wshandleMode = -1;
    private int m_emptyTagFormat = -1;
    private String m_elementPostFilter = null;
    private String m_elementPostFilterId = null;
    private String m_cdataPostFilter = null;
    private String m_cdataPostFilterId = null;
    private String m_sidTagName = null;
    private String m_sidAttrName = null;
    private String m_isCheckWellFormed = null;
    private String m_isGerateLangInfo = null;

    public XmlFilterConfigParser(XMLRuleFilter xmlFilter)
    {
        this(xmlFilter == null ? null : xmlFilter.getConfigXml());
    }

    public XmlFilterConfigParser(String configXml)
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
    public static String toXml(String exWhiteSpaceChars, int phConsolidation,
            int phTrimMode, int nonasciiAs, int wsHandleMode,
            int emptyTagFormat, String elementPostFilter,
            String elementPostFilterId, String cdataPostFilter,
            String cdataPostFilterId, String sidTagName, String sidAttrName,
            String isCheckWellFormed, String isGerateLangInfo,
            JSONArray preserveWsTags, JSONArray embTags,
            JSONArray transAttrTags, JSONArray contentInclTags,
            JSONArray cdataPostfilterTags, JSONArray entities,
            JSONArray processIns, JSONArray internalTag,
            JSONArray srcCmtXmlComment, JSONArray srcCmtXmlTag)
            throws Exception
    {
        StringBuffer sb = new StringBuffer();
        sb.append("<").append(NODE_ROOT).append(">");
        sb.append("<").append(NODE_EXTENDED_WHITESPACE_CHARS).append(">");
        sb.append(exWhiteSpaceChars == null ? "" : XmlUtil
                .escapeString(exWhiteSpaceChars));
        sb.append("</").append(NODE_EXTENDED_WHITESPACE_CHARS).append(">");
        sb.append("<").append(NODE_PH_CONSOLIDATION).append(">");
        sb.append(phConsolidation);
        sb.append("</").append(NODE_PH_CONSOLIDATION).append(">");
        sb.append("<").append(NODE_PH_TRIM).append(">");
        sb.append(phTrimMode);
        sb.append("</").append(NODE_PH_TRIM).append(">");
        sb.append("<").append(NODE_NON_ASCII_AS).append(">");
        sb.append(nonasciiAs);
        sb.append("</").append(NODE_NON_ASCII_AS).append(">");
        sb.append("<").append(NODE_WHITESPACE_HANDLE).append(">");
        sb.append(wsHandleMode);
        sb.append("</").append(NODE_WHITESPACE_HANDLE).append(">");
        sb.append("<").append(NODE_EMPTY_TAG_FORMAT).append(">");
        sb.append(emptyTagFormat);
        sb.append("</").append(NODE_EMPTY_TAG_FORMAT).append(">");
        sb.append("<").append(NODE_ELEMENT_POST_FILTER).append(">");
        sb.append(elementPostFilter);
        sb.append("</").append(NODE_ELEMENT_POST_FILTER).append(">");
        sb.append("<").append(NODE_ELEMENT_POST_FILTER_ID).append(">");
        sb.append(elementPostFilterId);
        sb.append("</").append(NODE_ELEMENT_POST_FILTER_ID).append(">");
        sb.append("<").append(NODE_CDATA_POST_FILTER).append(">");
        sb.append(cdataPostFilter);
        sb.append("</").append(NODE_CDATA_POST_FILTER).append(">");
        sb.append("<").append(NODE_CDATA_POST_FILTER_ID).append(">");
        sb.append(cdataPostFilterId);
        sb.append("</").append(NODE_CDATA_POST_FILTER_ID).append(">");
        sb.append("<").append(NODE_SID_TAG_NAME).append(">");
        sb.append(sidTagName);
        sb.append("</").append(NODE_SID_TAG_NAME).append(">");
        sb.append("<").append(NODE_SID_ATTR_NAME).append(">");
        sb.append(sidAttrName);
        sb.append("</").append(NODE_SID_ATTR_NAME).append(">");
        sb.append("<").append(NODE_IS_CHECK_WELL_FORMED).append(">");
        sb.append(isCheckWellFormed);
        sb.append("</").append(NODE_IS_CHECK_WELL_FORMED).append(">");
        sb.append("<").append(NODE_IS_GENERATE_LANG).append(">");
        sb.append(isGerateLangInfo);
        sb.append("</").append(NODE_IS_GENERATE_LANG).append(">");
        sb.append("<").append(NODE_WHITESPACE_PRESERVE_TAGS).append(">");
        sb.append(jsonArrayToXml(preserveWsTags));
        sb.append("</").append(NODE_WHITESPACE_PRESERVE_TAGS).append(">");
        sb.append("<").append(NODE_EMBEDDED_TAGS).append(">");
        sb.append(jsonArrayToXml(embTags));
        sb.append("</").append(NODE_EMBEDDED_TAGS).append(">");
        sb.append("<").append(NODE_TRANSLATE_ATTRIBUTE_TAGS).append(">");
        sb.append(jsonArrayToXml(transAttrTags));
        sb.append("</").append(NODE_TRANSLATE_ATTRIBUTE_TAGS).append(">");
        sb.append("<").append(NODE_CONTENT_INCLUTION_TAGS).append(">");
        sb.append(jsonArrayToXml(contentInclTags));
        sb.append("</").append(NODE_CONTENT_INCLUTION_TAGS).append(">");
        sb.append("<").append(NODE_CDATA_POST_FILTER_TAGS).append(">");
        sb.append(jsonArrayToXml(cdataPostfilterTags));
        sb.append("</").append(NODE_CDATA_POST_FILTER_TAGS).append(">");
        sb.append("<").append(NODE_ENTITIES).append(">");
        sb.append(jsonArrayToXml(entities));
        sb.append("</").append(NODE_ENTITIES).append(">");
        sb.append("<").append(NODE_PROCESS_INS).append(">");
        sb.append(jsonArrayToXml(processIns));
        sb.append("</").append(NODE_PROCESS_INS).append(">");
        sb.append("<").append(NODE_INTERNAL_TAG).append(">");
        sb.append(jsonArrayToXml(internalTag));
        sb.append("</").append(NODE_INTERNAL_TAG).append(">");
        sb.append("<").append(NODE_SRCCMT_XMLCOMMENT).append(">");
        sb.append(jsonArrayToXml(srcCmtXmlComment));
        sb.append("</").append(NODE_SRCCMT_XMLCOMMENT).append(">");
        sb.append("<").append(NODE_SRCCMT_XMLTAG).append(">");
        sb.append(jsonArrayToXml(srcCmtXmlTag));
        sb.append("</").append(NODE_SRCCMT_XMLTAG).append(">");
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

    public String getNewConfigXmlStr(String p_elementName, String p_value)
            throws Exception
    {
        setSingleElementValue(p_elementName, p_value);
        String newConfigXmlStr = documentToStr();
        return newConfigXmlStr;
    }

    public String getNewConfigXmlStr(Map<Long, Long> htmlFilterIdMap)
            throws Exception
    {
        Element cdataPostfilterTagsNode = getSingleElement(NODE_CDATA_POST_FILTER_TAGS);
        NodeList arrayNodes = cdataPostfilterTagsNode.getChildNodes();
        for (int i = 0; i < arrayNodes.getLength(); i++)
        {
            Element arrayElement = (Element) arrayNodes.item(i);
            NodeList list = arrayElement.getElementsByTagName("postFilterId");
            if (list != null && list.getLength() > 0)
            {
                Element postFilterIdElement = (Element) list.item(0);
                if (postFilterIdElement != null
                        || postFilterIdElement.getFirstChild() != null)
                {
                    String postFilterId = postFilterIdElement.getFirstChild()
                            .getNodeValue();
                    if (htmlFilterIdMap.containsKey(Long
                            .parseLong(postFilterId)))
                    {
                        String newId = String.valueOf(htmlFilterIdMap.get(Long
                                .parseLong(postFilterId)));
                        postFilterIdElement.getFirstChild().setNodeValue(newId);
                    }
                }
            }
        }
        String newConfigXmlStr = documentToStr();
        if (newConfigXmlStr == null)
            return null;

        return newConfigXmlStr;
    }

    public String documentToStr() throws Exception
    {
        String returnStr = null;
        StringWriter strWtr = new StringWriter();
        StreamResult strResult = new StreamResult(strWtr);
        TransformerFactory tfac = TransformerFactory.newInstance();
        try
        {
            Transformer t = tfac.newTransformer();
            t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            t.transform(new DOMSource(m_rootElement), strResult);
            String result = strResult.getWriter().toString().trim();
            returnStr = result.substring(result.indexOf("<xmlFilterConfig>"),
                    result.length());
            strWtr.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return returnStr;
    }

    public String getExtendedWhiteSpaceChars()
    {
        if (m_extendedWhiteSpaceChars == null)
        {
            String result = getSingleElementValue(NODE_EXTENDED_WHITESPACE_CHARS);
            m_extendedWhiteSpaceChars = (result == null ? "" : result);
        }

        return m_extendedWhiteSpaceChars;
    }

    public int getPhConsolidationMode()
    {
        if (m_phConsolidationMode == -1)
        {
            int result = PH_CONSOLIDATE_DONOT;
            String v = getSingleElementValue(NODE_PH_CONSOLIDATION);
            try
            {
                result = Integer.parseInt(v);
            }
            catch (Exception e)
            {
                result = PH_CONSOLIDATE_DONOT;
            }

            m_phConsolidationMode = result;
        }

        return m_phConsolidationMode;
    }

    public int getPhTrimMode()
    {
        if (m_phTrimMode == -1)
        {
            int result = PH_TRIM_DONOT;
            String v = getSingleElementValue(NODE_PH_TRIM);
            try
            {
                result = Integer.parseInt(v);
            }
            catch (Exception e)
            {
                result = PH_TRIM_DONOT;
            }

            m_phTrimMode = result;
        }

        return m_phTrimMode;
    }

    public int getNonasciiAs()
    {
        if (m_nonasciiAs == -1)
        {
            int result = NON_ASCII_AS_CHARACTER;
            String v = getSingleElementValue(NODE_NON_ASCII_AS);
            try
            {
                result = Integer.parseInt(v);
            }
            catch (Exception e)
            {
                result = NON_ASCII_AS_CHARACTER;
            }

            m_nonasciiAs = result;
        }

        return m_nonasciiAs;
    }

    public int getWhiteSpaceHanldeMode()
    {
        if (m_wshandleMode == -1)
        {
            int result = WHITESPACE_HANDLE_PRESERVE;
            String v = getSingleElementValue(NODE_WHITESPACE_HANDLE);
            try
            {
                result = Integer.parseInt(v);
            }
            catch (Exception e)
            {
                result = WHITESPACE_HANDLE_PRESERVE;
            }

            m_wshandleMode = result;
        }

        return m_wshandleMode;
    }

    public int getEmptyTagFormat()
    {
        if (m_emptyTagFormat == -1)
        {
            int result = EMPTY_TAG_FORMAT_PRESERVE;
            String v = getSingleElementValue(NODE_EMPTY_TAG_FORMAT);
            try
            {
                result = Integer.parseInt(v);
            }
            catch (Exception e)
            {
                result = EMPTY_TAG_FORMAT_PRESERVE;
            }

            m_emptyTagFormat = result;
        }

        return m_emptyTagFormat;
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

    public String getElementPostFilterId()
    {
        if (m_elementPostFilterId == null)
        {
            String result = getSingleElementValue(NODE_ELEMENT_POST_FILTER_ID);
            m_elementPostFilterId = (result == null ? "" : result);
        }

        return m_elementPostFilterId;
    }

    public String getCdataPostFilterTableName()
    {
        if (m_cdataPostFilter == null)
        {
            String result = getSingleElementValue(NODE_CDATA_POST_FILTER);
            m_cdataPostFilter = (result == null ? "" : result);
        }

        return m_cdataPostFilter;
    }

    public String getCdataPostFilterId()
    {
        if (m_cdataPostFilterId == null)
        {
            String result = getSingleElementValue(NODE_CDATA_POST_FILTER_ID);
            m_cdataPostFilterId = (result == null ? "" : result);
        }

        return m_cdataPostFilterId;
    }

    public String getSidTagName()
    {
        if (m_sidTagName == null)
        {
            String result = getSingleElementValue(NODE_SID_TAG_NAME);
            m_sidTagName = (result == null ? "" : result);
        }

        return m_sidTagName;
    }

    public String getSidAttrName()
    {
        if (m_sidAttrName == null)
        {
            String result = getSingleElementValue(NODE_SID_ATTR_NAME);
            m_sidAttrName = (result == null ? "" : result);
        }

        return m_sidAttrName;
    }

    public boolean isCheckWellFormed()
    {
        if (m_isCheckWellFormed == null)
        {
            String result = getSingleElementValue(NODE_IS_CHECK_WELL_FORMED);
            m_isCheckWellFormed = (result == null ? "" : result);
        }

        return "true".equalsIgnoreCase(m_isCheckWellFormed);
    }

    public boolean isGerateLangInfo()
    {
        if (m_isGerateLangInfo == null)
        {
            String result = getSingleElementValue(NODE_IS_GENERATE_LANG);
            m_isGerateLangInfo = (result == null ? "" : result);
        }

        return "true".equalsIgnoreCase(m_isGerateLangInfo);
    }

    public List<String> getPostFilterIdAndName()
    {
        List<String> returnList = new ArrayList<String>();
        Element cdataPostfilterTagsNode = getSingleElement(NODE_CDATA_POST_FILTER_TAGS);
        NodeList arrayNodes = cdataPostfilterTagsNode.getChildNodes();
        for (int i = 0; i < arrayNodes.getLength(); i++)
        {
            Element arrayElement = (Element) arrayNodes.item(i);
            String postFilterId = getSingleElementValue(arrayElement,
                    "postFilterId");
            String postFilterTableName = getSingleElementValue(arrayElement,
                    "postFilterTableName");
            returnList.add(postFilterId + "," + postFilterTableName);
        }
        return returnList;
    }

    public String getWhiteSpacePreserveTagsJson()
    {
        try
        {
            Element element = getSingleElement(NODE_WHITESPACE_PRESERVE_TAGS);
            String[] toArray =
            { "\"attributes\":{", "\"transAttributes\":{" };
            return tagsXmlToJsonArray(element, toArray);
        }
        catch (Exception e)
        {
            CATEGORY.error("Error occur when xml to json", e);
            return "[]";
        }
    }

    public String getEmbeddedTagsJson()
    {
        try
        {
            Element element = getSingleElement(NODE_EMBEDDED_TAGS);
            String[] toArray =
            { "\"attributes\":{", "\"transAttributes\":{" };
            return tagsXmlToJsonArray(element, toArray);
        }
        catch (Exception e)
        {
            CATEGORY.error("Error occur when xml to json", e);
            return "[]";
        }
    }

    public String getTransAttrTagsJson()
    {
        try
        {
            Element element = getSingleElement(NODE_TRANSLATE_ATTRIBUTE_TAGS);
            String[] toArray =
            { "\"attributes\":{", "\"transAttributes\":{" };
            return tagsXmlToJsonArray(element, toArray);
        }
        catch (Exception e)
        {
            CATEGORY.error("Error occur when xml to json", e);
            return "[]";
        }
    }

    public String getContentInclTagsJson()
    {
        try
        {
            Element element = getSingleElement(NODE_CONTENT_INCLUTION_TAGS);
            String[] toArray =
            { "\"attributes\":{", "\"transAttributes\":{" };
            return tagsXmlToJsonArray(element, toArray);
        }
        catch (Exception e)
        {
            CATEGORY.error("Error occur when xml to json", e);
            return "[]";
        }
    }

    public String getCDataPostFilterTagsJson()
    {
        try
        {
            Element element = getSingleElement(NODE_CDATA_POST_FILTER_TAGS);
            String[] toArray =
            { "\"cdataConditions\":{" };
            return tagsXmlToJsonArray(element, toArray);
        }
        catch (Exception e)
        {
            CATEGORY.error("Error occur when xml to json", e);
            return "[]";
        }
    }

    public String getEntitiesJson()
    {
        try
        {
            Element element = getSingleElement(NODE_ENTITIES);
            String[] toArray = null;
            return tagsXmlToJsonArray(element, toArray);
        }
        catch (Exception e)
        {
            CATEGORY.error("Error occur when xml to json", e);
            return "[]";
        }
    }

    public String getProcessInsJson()
    {
        try
        {
            Element element = getSingleElement(NODE_PROCESS_INS);
            String[] toArray =
            { "\"piTransAttributes\":{" };
            return tagsXmlToJsonArray(element, toArray);
        }
        catch (Exception e)
        {
            CATEGORY.error("Error occur when xml to json", e);
            return "[]";
        }
    }

    public String getInternalTagJson()
    {
        try
        {
            Element element = getSingleElement(NODE_INTERNAL_TAG);
            String[] toArray =
            { "\"attributes\":{" };
            return tagsXmlToJsonArray(element, toArray);
        }
        catch (Exception e)
        {
            CATEGORY.error("Error occur when xml to json", e);
            return "[]";
        }
    }

    public String getSrcCmtXmlCommentJson()
    {
        try
        {
            Element element = getSingleElement(NODE_SRCCMT_XMLCOMMENT);
            String[] toArray =
            { "\"attributes\":{" };
            return tagsXmlToJsonArray(element, toArray);
        }
        catch (Exception e)
        {
            CATEGORY.error("Error occur when xml to json", e);
            return "[]";
        }
    }

    public String getSrcCmtXmlTagJson()
    {
        try
        {
            Element element = getSingleElement(NODE_SRCCMT_XMLTAG);
            String[] toArray =
            { "\"attributes\":{" };
            return tagsXmlToJsonArray(element, toArray);
        }
        catch (Exception e)
        {
            CATEGORY.error("Error occur when xml to json", e);
            return "[]";
        }
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
    protected static Element getSingleElement(Element p_rootElement,
            String p_elementName)
    {
        NodeList list = p_rootElement.getElementsByTagName(p_elementName);
        return (list != null && list.getLength() > 0) ? (Element) list.item(0)
                : null;
    }

    private static String tagsXmlToJsonArray(Element element, String[] toArray)
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

        String[] strs = innerXml.split("</array>");
        List<String> list = Arrays.asList(strs);
        SortUtil.sort(list, new StringComparator(Locale.getDefault()));
        Iterator<String> it = list.iterator();
        innerXml = it.next() + "</array>";
        while (it.hasNext())
        {
            innerXml = innerXml + it.next() + "</array>";
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

    private static void tagsXmlToJsonArrayFixObject(StringBuffer ret,
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
}
