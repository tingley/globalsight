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
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.globalsight.cxe.entity.filterconfiguration.Filter;
import com.globalsight.cxe.entity.filterconfiguration.FilterConstants;
import com.globalsight.cxe.entity.filterconfiguration.FilterHelper;
import com.globalsight.cxe.entity.filterconfiguration.XMLRuleFilter;
import com.globalsight.cxe.entity.filterconfiguration.XmlFilterConfigParser;
import com.globalsight.cxe.entity.filterconfiguration.XmlFilterConstants;
import com.globalsight.ling.common.Text;
import com.globalsight.ling.common.XmlEntities;
import com.globalsight.ling.common.srccomment.SrcCmtXmlComment;
import com.globalsight.ling.common.srccomment.SrcCmtXmlTag;

public class XmlFilterHelper
{
    private static final String NUMBER_ENTITY_START = "&#";
    private static final String NUMBER_ENTITY_END = ";";
    private XmlFilterConfigParser m_xmlFilterConfigParser = null;
    private XmlEntities m_entities = null;
    private String m_extendedWhiteSpaceChars = null;
    private XmlFilterTags tags = null;

    /**
     * Constructor
     * 
     * @param xmlFilter
     */
    public XmlFilterHelper(XMLRuleFilter xmlFilter)
    {
        m_xmlFilterConfigParser = (xmlFilter == null) ? null
                : new XmlFilterConfigParser(xmlFilter);
    }

    // ////////////////////////////////////////////////////////
    // public
    // ////////////////////////////////////////////////////////

    /**
     * Init
     */
    public void init() throws Exception
    {
        if (!isConfigParserNull())
            m_xmlFilterConfigParser.parserXml();
    }

    /**
     * Set the entity encoder / decoder
     * 
     * @param p_entities
     */
    public void setXmlEntities(XmlEntities p_entities)
    {
        m_entities = p_entities;
    }

    /**
     * Handle the extended white space characters and preserve/collapse white
     * space
     * 
     * @param p_text
     * @return
     */
    public String processText(String p_text, boolean isInline,
            boolean isPreserveWS)
    {
        if (isConfigParserNull())
        {
            return m_entities.encodeStringBasic(p_text);
        }

        if (!isInline && isBlankOrExblank(p_text))
        {
            return p_text;
        }

        final int len = p_text.length();
        StringBuffer temp = new StringBuffer();
        StringBuffer result = new StringBuffer(len);

        for (int i = 0; i < len; i++)
        {
            char c = p_text.charAt(i);

            if (isExtendedWhiteSpaceChar(p_text, c, i))
            {
                if (temp.length() > 0)
                {
                    result.append(m_entities.encodeStringBasic(temp.toString()));
                    temp.delete(0, temp.length());
                }

                result.append("<ph type=\"x-exspace\">");
                result.append(m_entities.encodeStringBasic("" + c));
                result.append("</ph>");
            }
            else
            {
                temp.append(c);
            }
        }

        result.append(m_entities.encodeStringBasic(temp.toString()));

        String retStr = null;

        // collapse white space if needed
        if (!isPreserveWS)
        {
            retStr = collapseWhiteSpace(result);
        }
        else
        {
            retStr = result.toString();
        }

        return retStr;
    }

    /**
     * Check if the content is blank or extended whitespace
     * 
     * @param toAdd
     * @return
     */
    public boolean isBlankOrExblank(String p_content)
    {
        if (isConfigParserNull())
        {
            return Text.isBlank(p_content);
        }

        String content = removeExspaces(p_content);
        return Text.isBlank(content);
    }

    /**
     * Count the nodes which can do PlaceHolder trimming. The root node is
     * mark-up (not extract) and not embedded; and its child node is extracted,
     * embedded, not empty, no extractable attribute and no dataFormat
     * 
     * @param p_node
     * @return
     */
    public int countPhTrim(Node p_node, Map p_ruleMap, boolean force)
    {
        int mode = XmlFilterConfigParser.PH_TRIM_DONOT;

        if (force)
        {
            mode = XmlFilterConfigParser.PH_TRIM_DO;
        }
        else
        {
            // do nothing if not init
            if (isConfigParserNull())
            {
                mode = XmlFilterConfigParser.PH_TRIM_DONOT;
            }
            else
            {
                mode = m_xmlFilterConfigParser.getPhTrimMode();
            }
        }

        // return 0 if trim node is do not
        if (mode == XmlFilterConfigParser.PH_TRIM_DONOT)
        {
            return 0;
        }

        // check parameters
        if (p_node == null || p_node.getNodeType() != Node.ELEMENT_NODE
                || isEmptyTag(p_node))
        {
            return 0;
        }

        boolean isEmbeddable = Rule.isInline(p_ruleMap, p_node);

        // parent node is mark-up and not embedded
        if (isEmbeddable)
        {
            return 0;
        }

        // child node is extract, embedded and no extract attributes
        int currentCount = 0;
        Node currentNode = p_node.getFirstChild();
        String dataFormat = null;
        while (currentNode != null
                && currentNode.getNodeType() == Node.ELEMENT_NODE
                && !isEmptyTag(currentNode)
                && currentNode.getNextSibling() == null
                && !Rule.isInternal(p_ruleMap, currentNode))
        {
            boolean isCurrentEmbeddable = Rule.isInline(p_ruleMap, currentNode);
            boolean isCurrentExtractable = Rule
                    .extracts(p_ruleMap, currentNode);
            NamedNodeMap attrs = currentNode.getAttributes();
            boolean isAttrExtractable = false;

            if (attrs != null && attrs.getLength() > 0)
            {
                for (int i = 0; i < attrs.getLength(); ++i)
                {
                    Node att = attrs.item(i);
                    isAttrExtractable = Rule.extracts(p_ruleMap, att);
                    if (isAttrExtractable)
                    {
                        break;
                    }
                }
            }

            dataFormat = Rule.getDataFormat(p_ruleMap, currentNode);

            if (isCurrentEmbeddable && isCurrentExtractable
                    && !isAttrExtractable && dataFormat == null)
            {
                ++currentCount;
                currentNode = currentNode.getFirstChild();
            }
            else
            {
                break;
            }
        }

        return currentCount;
    }

    /**
     * Count the PlaceHolder consolidation nodes which is valid node as
     * following: extracts must be true, isEmbeddable must be true,
     * isTranslatable must be save as parent node, containedInHtml must be
     * false, isEmptyTag must be false, dataFormat must be null
     * 
     * @param p_node
     * @param p_ruleMap
     * @param mode
     * @return
     */
    public int countPhConsolidation(Node p_node, Map p_ruleMap, int mode,
            boolean isIdml)
    {
        // return 0 if mode is not to do PlaceHolder consolidation
        boolean isIgnoreSpace = (mode == XmlFilterConfigParser.PH_CONSOLIDATE_ADJACENT_IGNORE_SPACE);
        if (mode == XmlFilterConfigParser.PH_CONSOLIDATE_DONOT)
        {
            return 0;
        }

        // check parameters
        if (p_ruleMap == null)
        {
            return 0;
        }

        if (p_node == null || p_node.getNodeType() != Node.ELEMENT_NODE)
        {
            return 0;
        }

        // return current count if this element is not valid
        int currentCount = 0;
        String dataFormat = null;
        boolean isEmbeddable = false;
        boolean isTranslatable = true;
        boolean extracts = false;
        boolean containedInHtml = Rule.isContainedInHtml(p_ruleMap, p_node);
        boolean isEmptyTag = isEmptyTag(p_node);

        if (!isIdml && nextIsElement(p_node))
        {
            return currentCount;
        }

        if (containedInHtml)
        {
            return currentCount;
        }

        if (isEmptyTag)
        {
            return currentCount;
        }

        isEmbeddable = Rule.isInline(p_ruleMap, p_node);
        if (!isEmbeddable)
        {
            return currentCount;
        }

        extracts = Rule.extracts(p_ruleMap, p_node);
        if (extracts)
        {
            isTranslatable = Rule.isTranslatable(p_ruleMap, p_node);
            dataFormat = Rule.getDataFormat(p_ruleMap, p_node);
        }
        else
        {
            return currentCount;
        }

        if (dataFormat != null)
        {
            return currentCount;
        }

        Node childNode = getPhConsolidateableChildNode(p_node, isIgnoreSpace);

        if (childNode != null)
        {
            return countPhConsolidation(childNode, p_ruleMap, isTranslatable,
                    currentCount, isIgnoreSpace);
        }
        else
        {
            return currentCount;
        }
    }

    public int countPhConsolidation(Node p_node, Map p_ruleMap)
    {
        int mode = XmlFilterConfigParser.PH_CONSOLIDATE_DONOT;

        // do nothing if not init
        if (isConfigParserNull())
        {
            mode = XmlFilterConfigParser.PH_CONSOLIDATE_DONOT;
        }
        else
        {
            mode = m_xmlFilterConfigParser.getPhConsolidationMode();
        }

        return countPhConsolidation(p_node, p_ruleMap, mode, false);
    }

    public boolean isPreserveWhiteSpaces()
    {
        if (!isConfigParserNull())
        {
            return m_xmlFilterConfigParser.getWhiteSpaceHanldeMode() == XmlFilterConstants.WHITESPACE_HANDLE_PRESERVE;
        }

        return false;
    }

    public boolean usesEmptyTag()
    {
        if (!isConfigParserNull())
        {
            return m_xmlFilterConfigParser.getEmptyTagFormat() == XmlFilterConstants.EMPTY_TAG_FORMAT_CLOSE;
        }

        return false;
    }

    public boolean preserveEmptyTag()
    {
        if (!isConfigParserNull())
        {
            return m_xmlFilterConfigParser.getEmptyTagFormat() == XmlFilterConstants.EMPTY_TAG_FORMAT_PRESERVE;
        }

        // default preserve empty tag as source if not using a filter
        return true;
    }

    public boolean isElementPostFilter()
    {
        if (!isConfigParserNull())
        {
            String str = getElementPostFilterTableName();
            return (str != null && FilterConstants.ALL_FILTER_TABLE_NAMES
                    .contains(str));
        }

        return false;
    }

    public Filter getElementPostFilter() throws Exception
    {
        long filterId = getElementPostFilterId();
        String filterTableName = getElementPostFilterTableName();

        if (filterId >= 0 && filterTableName != null)
        {
            return FilterHelper.getFilter(filterTableName, filterId);
        }
        else
        {
            return null;
        }
    }

    public String getElementPostFilterTableName()
    {
        if (!isConfigParserNull())
        {
            return m_xmlFilterConfigParser.getElementPostFilterTableName();
        }

        return "";
    }

    public long getElementPostFilterId()
    {
        if (!isConfigParserNull())
        {
            String v = m_xmlFilterConfigParser.getElementPostFilterId();
            try
            {
                return Long.parseLong(v);
            }
            catch (Exception e)
            {
                return -1l;
            }
        }

        return -1l;
    }

    public String getElementPostFormat()
    {
        String filterTableName = getElementPostFilterTableName();
        return getFormatForFilter(filterTableName);
    }

    public boolean isCdataPostFilter()
    {
        if (!isConfigParserNull())
        {
            String str = getCdataPostFilterTableName();
            return (str != null && FilterConstants.ALL_FILTER_TABLE_NAMES
                    .contains(str));
        }

        return false;
    }

    public Filter getCdataPostFilter() throws Exception
    {
        long filterId = getCdataPostFilterId();
        String filterTableName = getCdataPostFilterTableName();

        if (filterId >= 0 && filterTableName != null)
        {
            return FilterHelper.getFilter(filterTableName, filterId);
        }
        else
        {
            return null;
        }
    }

    public String getCdataPostFilterTableName()
    {
        if (!isConfigParserNull())
        {
            return m_xmlFilterConfigParser.getCdataPostFilterTableName();
        }

        return "";
    }

    public long getCdataPostFilterId()
    {
        if (!isConfigParserNull())
        {
            String v = m_xmlFilterConfigParser.getCdataPostFilterId();
            try
            {
                return Long.parseLong(v);
            }
            catch (Exception e)
            {
                return -1l;
            }
        }

        return -1l;
    }

    public String getCdataPostFormat()
    {
        String filterTableName = getCdataPostFilterTableName();
        return getFormatForFilter(filterTableName);
    }

    public XmlFilterTags getXmlFilterTags()
    {
        if (tags == null)
        {
            tags = new XmlFilterTags();
            if (!isConfigParserNull())
            {
                // preserve whitespace tags
                List<XmlFilterTag> wsPreserveTags = getXmlFilterTagsFromXml(XmlFilterConstants.NODE_WHITESPACE_PRESERVE_TAGS);
                tags.setWhiteSpacePreserveTags(wsPreserveTags);

                // embedded tags
                List<XmlFilterTag> embTags = getXmlFilterTagsFromXml(XmlFilterConstants.NODE_EMBEDDED_TAGS);
                tags.setEmbeddedTags(embTags);

                // Translatable attributes tags
                List<XmlFilterTag> transAttrTags = getXmlFilterTagsFromXml(XmlFilterConstants.NODE_TRANSLATE_ATTRIBUTE_TAGS);
                tags.setTransAttrTags(transAttrTags);

                // content inclusion tags
                List<XmlFilterTag> contentInclTags = getXmlFilterTagsFromXml(XmlFilterConstants.NODE_CONTENT_INCLUTION_TAGS);
                tags.setContentInclTags(contentInclTags);

                // cdata post filter tags
                List<XmlFilterCDataTag> cdataPostFilterTags = getCDataPostFilterTagsFromXml(XmlFilterConstants.NODE_CDATA_POST_FILTER_TAGS);
                tags.setCdataPostFilterTags(cdataPostFilterTags);

                // entities
                List<XmlFilterEntity> entities = getEntitiesFromXml(XmlFilterConstants.NODE_ENTITIES);
                tags.setEntities(entities);

                // process instruction
                List<XmlFilterProcessIns> processIns = getProcessInsFromXml(XmlFilterConstants.NODE_PROCESS_INS);
                tags.setProcessIns(processIns);

                // sid tags
                List<XmlFilterSidTag> sidTags = getSidTagsFromXml();
                tags.setSidTags(sidTags);

                // internal tag
                List<XmlFilterTag> internalTag = getXmlFilterTagsFromXml(XmlFilterConstants.NODE_INTERNAL_TAG);
                tags.setIntenalTag(internalTag);

                // src comment
                List<SrcCmtXmlComment> srcCmtXmlComment = getSrcCmtXmlCommentFromXml(XmlFilterConstants.NODE_SRCCMT_XMLCOMMENT);
                tags.setSrcCmtXmlComment(srcCmtXmlComment);

                List<SrcCmtXmlTag> srcCmtXmlTag = getSrcCmtXmlTagFromXml(XmlFilterConstants.NODE_SRCCMT_XMLTAG);
                tags.setSrcCmtXmlTag(srcCmtXmlTag);
            }
        }

        return tags;
    }

    public XmlFilterCDataTag getRuleForCData(Node p_node)
    {
        List<XmlFilterCDataTag> cdataPostFilterTags = getXmlFilterTags()
                .getCdataPostFilterTags();
        XmlFilterCDataTag ret = null;

        if (cdataPostFilterTags == null || cdataPostFilterTags.size() == 0)
        {
            return ret;
        }

        for (int i = 0; i < cdataPostFilterTags.size(); i++)
        {
            XmlFilterCDataTag tag = cdataPostFilterTags.get(i);
            if (tag.isMatched(p_node))
            {
                ret = tag;
            }
        }

        return ret;
    }

    public XmlFilterEntity getMatchedXmlFilterEntity(String entityName)
    {
        List<XmlFilterEntity> entities = getXmlFilterTags().getEntities();
        XmlFilterEntity ret = null;

        if (entities == null || entities.size() == 0)
        {
            return ret;
        }

        for (int i = 0; i < entities.size(); i++)
        {
            XmlFilterEntity tag = entities.get(i);
            if (tag.isNameMatched(entityName))
            {
                ret = tag;
            }
        }

        return ret;
    }

    public boolean isExclude(Node p_node) throws Exception
    {
        List<XmlFilterTag> contentInclTags = getXmlFilterTags()
                .getContentInclTags();

        if (contentInclTags != null && contentInclTags.size() > 0)
        {
            for (XmlFilterTag xmlFilterTag : contentInclTags)
            {
                List<Node> matchedNodes = xmlFilterTag
                        .getMatchedNodeList(p_node.getOwnerDocument());
                boolean isContentInclude = xmlFilterTag.isContentInclude();

                if (isContentInclude)
                    continue;

                for (Node node : matchedNodes)
                {
                    if (node.equals(p_node))
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public XmlFilterProcessIns getMatchedProcessIns(String aname)
    {
        List<XmlFilterProcessIns> tags = getXmlFilterTags().getProcessIns();
        XmlFilterProcessIns ret = null;

        if (tags == null || tags.size() == 0)
        {
            return ret;
        }

        for (int i = 0; i < tags.size(); i++)
        {
            XmlFilterProcessIns tag = tags.get(i);
            if (tag.isNameMatched(aname))
            {
                ret = tag;
            }
        }

        return ret;
    }

    public String getFormatForFilter(String filterTableName)
    {
        if (filterTableName == null)
        {
            return null;
        }

        if (FilterConstants.FILTER_TABLE_NAMES_FORMAT
                .containsKey(filterTableName))
        {
            return FilterConstants.FILTER_TABLE_NAMES_FORMAT
                    .get(filterTableName);
        }

        return null;
    }

    public boolean isCheckWellFormed()
    {
        if (!isConfigParserNull())
        {
            return m_xmlFilterConfigParser.isCheckWellFormed();
        }

        return false;
    }

    public boolean isGerateLangInfo()
    {
        if (!isConfigParserNull())
        {
            return m_xmlFilterConfigParser.isGerateLangInfo();
        }

        return false;
    }

    // ////////////////////////////////////////////////////////
    // private
    // ////////////////////////////////////////////////////////

    private List<XmlFilterTag> getXmlFilterTagsFromXml(String nodename)
    {
        List<XmlFilterTag> tags = new ArrayList<XmlFilterTag>();
        Element element = m_xmlFilterConfigParser.getSingleElement(nodename);

        if (element == null)
        {
            return tags;
        }

        NodeList nodes = element.getElementsByTagName("array");
        if (nodes != null && nodes.getLength() > 0)
        {
            for (int i = 0; i < nodes.getLength(); i++)
            {
                Node tagNode = nodes.item(i);
                if (tagNode.getNodeType() == Node.ELEMENT_NODE)
                {
                    Element tagElement = (Element) tagNode;
                    if (isEnabledTag(tagElement))
                    {
                        tags.add(new XmlFilterTag(tagElement));
                    }
                }
            }
        }

        return tags;
    }

    private List<XmlFilterCDataTag> getCDataPostFilterTagsFromXml(
            String nodename)
    {
        List<XmlFilterCDataTag> tags = new ArrayList<XmlFilterCDataTag>();
        Element element = m_xmlFilterConfigParser.getSingleElement(nodename);

        if (element == null)
        {
            return tags;
        }

        NodeList nodes = element.getElementsByTagName("array");
        if (nodes != null && nodes.getLength() > 0)
        {
            for (int i = 0; i < nodes.getLength(); i++)
            {
                Node tagNode = nodes.item(i);
                if (tagNode.getNodeType() == Node.ELEMENT_NODE)
                {
                    Element tagElement = (Element) tagNode;
                    if (isEnabledTag(tagElement))
                    {
                        tags.add(new XmlFilterCDataTag(tagElement));
                    }
                }
            }
        }

        return tags;
    }

    private List<XmlFilterProcessIns> getProcessInsFromXml(String nodename)
    {
        List<XmlFilterProcessIns> tags = new ArrayList<XmlFilterProcessIns>();
        Element element = m_xmlFilterConfigParser.getSingleElement(nodename);

        if (element == null)
        {
            return tags;
        }

        NodeList nodes = element.getElementsByTagName("array");
        if (nodes != null && nodes.getLength() > 0)
        {
            for (int i = 0; i < nodes.getLength(); i++)
            {
                Node tagNode = nodes.item(i);
                if (tagNode.getNodeType() == Node.ELEMENT_NODE)
                {
                    Element tagElement = (Element) tagNode;
                    if (isEnabledTag(tagElement))
                    {
                        tags.add(new XmlFilterProcessIns(tagElement));
                    }
                }
            }
        }

        return tags;
    }

    private List<SrcCmtXmlComment> getSrcCmtXmlCommentFromXml(String nodename)
    {
        List<SrcCmtXmlComment> tags = new ArrayList<SrcCmtXmlComment>();
        Element element = m_xmlFilterConfigParser.getSingleElement(nodename);

        if (element == null)
        {
            return tags;
        }

        NodeList nodes = element.getElementsByTagName("array");
        if (nodes != null && nodes.getLength() > 0)
        {
            for (int i = 0; i < nodes.getLength(); i++)
            {
                Node tagNode = nodes.item(i);
                if (tagNode.getNodeType() == Node.ELEMENT_NODE)
                {
                    Element tagElement = (Element) tagNode;
                    if (isEnabledTag(tagElement))
                    {
                        tags.add(SrcCmtXmlComment.initFromElement(tagElement));
                    }
                }
            }
        }

        return tags;
    }

    private List<SrcCmtXmlTag> getSrcCmtXmlTagFromXml(String nodename)
    {
        List<SrcCmtXmlTag> tags = new ArrayList<SrcCmtXmlTag>();
        Element element = m_xmlFilterConfigParser.getSingleElement(nodename);

        if (element == null)
        {
            return tags;
        }

        NodeList nodes = element.getElementsByTagName("array");
        if (nodes != null && nodes.getLength() > 0)
        {
            for (int i = 0; i < nodes.getLength(); i++)
            {
                Node tagNode = nodes.item(i);
                if (tagNode.getNodeType() == Node.ELEMENT_NODE)
                {
                    Element tagElement = (Element) tagNode;
                    if (isEnabledTag(tagElement))
                    {
                        tags.add(SrcCmtXmlTag.initFromElement(tagElement));
                    }
                }
            }
        }

        return tags;
    }

    private List<XmlFilterSidTag> getSidTagsFromXml()
    {
        List<XmlFilterSidTag> tags = new ArrayList<XmlFilterSidTag>();
        if (!isConfigParserNull())
        {
            String tname = m_xmlFilterConfigParser.getSidTagName();
            String aname = m_xmlFilterConfigParser.getSidAttrName();

            if (tname != null && tname.length() != 0 && aname != null
                    && aname.length() != 0)
            {
                tags.add(new XmlFilterSidTag(tname, aname));
            }
        }

        return tags;
    }

    private List<XmlFilterEntity> getEntitiesFromXml(String nodename)
    {
        List<XmlFilterEntity> tags = new ArrayList<XmlFilterEntity>();
        Element element = m_xmlFilterConfigParser.getSingleElement(nodename);

        if (element == null)
        {
            return tags;
        }

        NodeList nodes = element.getElementsByTagName("array");
        if (nodes != null && nodes.getLength() > 0)
        {
            for (int i = 0; i < nodes.getLength(); i++)
            {
                Node tagNode = nodes.item(i);
                if (tagNode.getNodeType() == Node.ELEMENT_NODE)
                {
                    Element tagElement = (Element) tagNode;
                    if (isEnabledTag(tagElement))
                    {
                        tags.add(new XmlFilterEntity(tagElement));
                    }
                }
            }
        }

        return tags;
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

    private boolean isExtendedWhiteSpaceChar(String p_text, char c, int i)
    {
        if (isConfigParserNull())
        {
            return false;
        }

        if (m_extendedWhiteSpaceChars == null)
        {
            String chars = m_xmlFilterConfigParser.getExtendedWhiteSpaceChars();
            if (chars == null || "".equals(chars))
            {
                m_extendedWhiteSpaceChars = "";
            }
            else
            {
                String[] charsArray = chars.split(" ");
                StringBuffer sb = new StringBuffer(charsArray.length);
                for (String ch : charsArray)
                {
                    sb.append(m_entities.decodeStringBasic(ch));
                }

                m_extendedWhiteSpaceChars = sb.toString();
            }
        }

        return m_extendedWhiteSpaceChars.contains("" + c);
    }

    private boolean nextIsElement(Node p_node)
    {
        Node nextNode = p_node.getNextSibling();

        while (nextNode != null)
        {
            if (nextNode.getNodeType() == Node.ELEMENT_NODE)
            {
                return true;
            }

            nextNode = nextNode.getNextSibling();
        }

        return false;
    }

    /**
     * Determine if this node can do PlaceHolde consolidation and do recur if
     * its child element is valid for PlaceHolder consolidation
     * 
     * @param p_node
     * @param p_ruleMap
     * @param isTranslateableOfParent
     * @param currentCount
     * @param isIgnoreSpace
     * @return
     */
    private int countPhConsolidation(Node p_node, Map p_ruleMap,
            boolean isTranslateableOfParent, int currentCount,
            boolean isIgnoreSpace)
    {
        String dataFormat = null;
        boolean isEmbeddable = false;
        boolean isTranslatable = true;
        boolean extracts = false;
        boolean containedInHtml = Rule.isContainedInHtml(p_ruleMap, p_node);
        boolean isEmptyTag = isEmptyTag(p_node);

        if (nextIsElement(p_node))
        {
            return currentCount;
        }

        if (containedInHtml)
        {
            return currentCount;
        }

        if (isEmptyTag)
        {
            return currentCount;
        }

        isEmbeddable = Rule.isInline(p_ruleMap, p_node);
        if (!isEmbeddable)
        {
            return currentCount;
        }

        extracts = Rule.extracts(p_ruleMap, p_node);
        if (extracts)
        {
            isTranslatable = Rule.isTranslatable(p_ruleMap, p_node);
            dataFormat = Rule.getDataFormat(p_ruleMap, p_node);
        }
        else
        {
            return currentCount;
        }

        if (dataFormat != null)
        {
            return currentCount;
        }

        if (isTranslateableOfParent != isTranslatable)
        {
            return currentCount;
        }

        // if do not return here, that means this current node is OK for
        // PlaceHolder consolidation
        int newCount = currentCount + 1;
        Node childNode = getPhConsolidateableChildNode(p_node, isIgnoreSpace);

        if (childNode != null)
        {
            return countPhConsolidation(childNode, p_ruleMap, isTranslatable,
                    newCount, isIgnoreSpace);
        }
        else
        {
            return newCount;
        }
    }

    /**
     * Get the child element which can do PlaceHolder consolidation
     * 
     * @param p_node
     * @param isIgnoreSpace
     * @return
     */
    private Node getPhConsolidateableChildNode(Node p_node,
            boolean isIgnoreSpace)
    {
        Node childNode = null;
        NodeList nodes = p_node.getChildNodes();
        int countOfElements = 0;
        if (nodes != null && nodes.getLength() > 0)
        {
            for (int i = 0; i < nodes.getLength(); i++)
            {
                Node node = nodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE)
                {
                    childNode = node;
                    countOfElements++;
                }
                else if (node.getNodeType() == Node.TEXT_NODE)
                {
                    String value = node.getNodeValue();
                    if (value == null
                            || (isIgnoreSpace && "".equals(value.trim())))
                    {
                        continue;
                    }
                    else
                    {
                        childNode = null;
                        break;
                    }
                }
                else
                {
                    childNode = null;
                    break;
                }
            }
        }

        // if has several child elements return null
        if (countOfElements > 1)
        {
            childNode = null;
        }

        return childNode;
    }

    private boolean isConfigParserNull()
    {
        return m_xmlFilterConfigParser == null;
    }

    private String removeExspaces(String p_content)
    {
        final int len = p_content.length();
        StringBuffer result = new StringBuffer(len);

        for (int i = 0; i < len; i++)
        {
            char c = p_content.charAt(i);

            if (isExtendedWhiteSpaceChar(p_content, c, i))
            {
                result.append(" ");
            }
            else
            {
                result.append(c);
            }
        }

        return result.toString();
    }

    // ////////////////////////////////////////////////////////
    // public static
    // ////////////////////////////////////////////////////////

    public static String saveNonAsciiAs(String p_src, long p_filterId,
            String p_filterTableName) throws Exception
    {
        if (p_filterId != -1
                && FilterConstants.XMLRULE_TABLENAME.equals(p_filterTableName))
        {
            XMLRuleFilter filter = FilterHelper.getXmlFilter(p_filterId);
            XmlFilterConfigParser configParser = new XmlFilterConfigParser(
                    filter);
            configParser.parserXml();

            if (configParser.getNonasciiAs() == XmlFilterConfigParser.NON_ASCII_AS_ENTITY)
            {
                // DOMParser parser = new DOMParser();
                // InputSource is = new InputSource(new StringReader(p_src));
                // parser.parse(is);
                // Document doc = parser.getDocument();
                // domNodeVisitor(doc);

                String xml = saveNonAsciiAsNumberEntity(p_src);
                return xml;
            }
        }

        return p_src;
    }

    /**
     * collapse multi-whitespace into one whitespace in the original text.
     * Support unicode white space with isWhitespace and isSpaceChar method in
     * {@link #Character}
     * 
     * @param oriText
     * @return
     */
    public static String collapseWhiteSpace(StringBuffer oriText)
    {
        if (oriText == null)
        {
            return null;
        }

        int oriLen = oriText.length();
        StringBuffer newText = new StringBuffer(oriLen);
        int wsCount = 0;
        char lastWs = ' ';

        for (int i = 0; i <= oriLen; i++)
        {
            if (i == oriLen)
            {
                if (wsCount == 1)
                {
                    newText.append(lastWs);
                }

                if (wsCount > 1)
                {
                    newText.append(' ');
                }

                wsCount = 0;
                break;
            }

            char c = oriText.charAt(i);
            if (Character.isWhitespace(c) || Character.isSpaceChar(c))
            {
                wsCount++;
                lastWs = c;
            }
            else
            {
                if (wsCount == 1)
                {
                    newText.append(lastWs);
                }

                if (wsCount > 1)
                {
                    newText.append(' ');
                }

                wsCount = 0;

                newText.append(c);
            }
        }

        String relt = newText.toString();
        return relt;
    }

    // ////////////////////////////////////////////////////////
    // private static
    // ////////////////////////////////////////////////////////

    private static String saveNonAsciiAsNumberEntity(String p_src)
    {
        if (p_src == null || p_src.length() == 0 || p_src.trim().length() == 0)
        {
            return p_src;
        }

        int length = p_src.length();
        StringBuffer src = new StringBuffer(p_src);
        StringBuffer ret = new StringBuffer(length);
        for (int i = 0; i < length; i++)
        {
            char c = src.charAt(i);
            int ci = (int) c;
            if (isAscii(ci))
            {
                ret.append(c);
            }
            else if (isEncodeable(src, i))
            {
                ret.append(NUMBER_ENTITY_START).append(ci)
                        .append(NUMBER_ENTITY_END);
            }
            else
            {
                ret.append(c);
            }
        }

        return ret.toString();
    }

    private static boolean isEncodeable(StringBuffer p_src, int index)
    {
        // white space
        boolean blank = false;
        boolean lt = false;
        String sub = null;

        for (int i = index - 1; i >= 0; i--)
        {
            char c = p_src.charAt(i);
            String cstr = "" + c;

            if ("".equals(cstr.trim()))
            {
                blank = true;
            }
            else if (c == '<')
            {
                sub = p_src.substring(i, index);
                lt = true;
                break;
            }
            // node value, encode
            else if (c == '>')
            {
                return true;
            }
        }

        // node name, do not encode
        if (lt && !blank)
        {
            return false;
        }

        // comments, encode
        if (sub.startsWith("<!--"))
        {
            return true;
        }

        // cdata, encode
        if (sub.startsWith("<![CDATA["))
        {
            return true;
        }

        // dtd or version, do not encode
        if (sub.startsWith("<!") || sub.startsWith("<?"))
        {
            return false;
        }

        // determine attribute
        int subLen = sub.length();
        boolean eq = false;
        boolean singleQuote = false;
        boolean doubleQuote = false;
        for (int i = 1; i < subLen; i++)
        {
            char c = sub.charAt(i);

            if (c == '=' && !singleQuote && !doubleQuote)
            {
                eq = true;
            }

            if (c == '\'' && eq && !doubleQuote)
            {
                singleQuote = !singleQuote;
                if (!singleQuote)
                {
                    eq = false;
                }
            }

            if (c == '"' && eq && !singleQuote)
            {
                doubleQuote = !doubleQuote;
                if (!doubleQuote)
                {
                    eq = false;
                }
            }
        }

        if (singleQuote || doubleQuote)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    private static boolean isAscii(int ci)
    {
        return (ci >= 0 && ci <= 127);
    }

    private static boolean isEmptyTag(Node p_node)
    {
        boolean isEmptyTag = (p_node == null) ? true
                : (p_node.getFirstChild() == null ? true : false);

        return isEmptyTag;
    }

    public static String encodeSpecifiedEntities(String p_text,
            char[] p_specXmlEncodeChar)
    {
        XmlEntities entities = new XmlEntities();
        entities.setUseDefaultXmlEncoderChar(false);
        return entities.encodeString(p_text, p_specXmlEncodeChar);
    }
}
