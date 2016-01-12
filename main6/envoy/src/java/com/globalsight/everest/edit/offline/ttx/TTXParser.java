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
package com.globalsight.everest.edit.offline.ttx;

import java.io.File;
import java.io.Reader;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import com.globalsight.everest.edit.offline.AmbassadorDwUpConstants;
import com.globalsight.everest.webapp.pagehandler.offline.OfflineConstants;

public class TTXParser
{
    static private final Logger s_logger = Logger.getLogger(TTXParser.class);

    // toolSettings attributes
    private String creationDate = null;
    private String creationTool = null;
    private String creationToolVersion = null;

    // userSettings attributes
    private String dataType = null;
    private String o_encoding = null;
    private String sourceLanguage = null;
    private String targetLanguage = null;
    private String sourceDocumentPath = null;
    private String userId = null;
    private String dataTypeVersion = null;
    private String settingsPath = null;
    private String settingsname = null;
    private String targetDefaultFont = null;

    // GS header information
    private String gs_Encoding = null;
    private String gs_DocumentFormat = null;
    private String gs_PlaceholderFormat = null;
    private String gs_SourceLocale = null;
    private String gs_TargetLocale = null;
    private String gs_PageID = null;
    private String gs_WorkflowID = null;
    private String gs_TaskID = null;
    private String gs_ExactMatchWordCount = null;
    private String gs_FuzzyMatchWordCount = null;
    private String gs_EditAll = null;
    private String gs_Populate100TargetSegments = null;// YES or NO

    private StringBuffer results = new StringBuffer();
    // private String latestPosition = null;

    private boolean isParsingTTXForGS = true;

    /**
     * Parse offline uploading TTX file for uploading purpose.
     * 
     * @param doc
     * @param isParsingTTXForGS
     *            :True for GS uploading;False for common TTX file parsing.
     * @return
     * @throws Exception
     */
    public String parseToTxt(Document doc, boolean isParsingTTXForGS)
            throws Exception
    {
        this.isParsingTTXForGS = isParsingTTXForGS;

        Element root = doc.getRootElement();// TRADOStag element
        // toolSettings
        parseToolSettings(root);

        // userSettings
        parseUserSettings(root);

        Element bodyElement = root.element(TTXConstants.BODY);
        Element rawElement = bodyElement.element(TTXConstants.RAW);
        // parse header info such as "pageId","taskId" etc.
        parseHeaderInfo(rawElement);
        // append header info
        appendHeaderInfo();

        // main contents
        if (rawElement.nodeCount() > 0)
        {
            Iterator nodesIt = rawElement.nodeIterator();
            while (nodesIt.hasNext())
            {
                Node node = (Node) nodesIt.next();
                String nodeStr = node.asXML();
                domNodehandler(node, false);
            }
        }

        appendEndInfo();

        return results.toString();
    }

    /**
     * Initialize toolSettings attributes' values.
     * 
     * @param p_doc
     */
    private void parseToolSettings(Element p_root)
    {
        if (p_root == null)
        {
            return;
        }

        try
        {
            Element ele = p_root.element(TTXConstants.FRONTMATTER).element(
                    TTXConstants.TOOLSETTINGS);

            Attribute creationDateAtt = ele
                    .attribute(TTXConstants.TOOLSETTINGS_ATT_CREATIONDATE);
            creationDate = (creationDateAtt == null ? "" : creationDateAtt
                    .getValue());

            Attribute creationToolAtt = ele
                    .attribute(TTXConstants.TOOLSETTINGS_ATT_CREATIONTOOL);
            creationTool = (creationToolAtt == null ? "" : creationToolAtt
                    .getValue());

            Attribute creationToolVersionAtt = ele
                    .attribute(TTXConstants.TOOLSETTINGS_ATT_CREATIONTOOLVERSION);
            creationToolVersion = (creationToolVersionAtt == null ? ""
                    : creationToolVersionAtt.getValue());
        }
        catch (Exception e)
        {
            s_logger.error("Error occurs during parsing toolSettings.", e);
        }
    }

    /**
     * Initialize userSettings attributes' values.
     * 
     * @param p_doc
     */
    private void parseUserSettings(Element p_root)
    {
        if (p_root == null)
        {
            return;
        }

        try
        {
            Element ele = p_root.element(TTXConstants.FRONTMATTER).element(
                    TTXConstants.USERSETTINGS);

            Attribute srcLangAtt = ele
                    .attribute(TTXConstants.USERSETTINGS_SOURCE_lANGUAGE);
            sourceLanguage = (srcLangAtt == null ? "" : srcLangAtt.getValue());

            Attribute dataTypeAtt = ele
                    .attribute(TTXConstants.USERSETTINGS_DATA_TYPE);
            dataType = (dataTypeAtt == null ? "" : dataTypeAtt.getValue());

            Attribute o_encodingAtt = ele
                    .attribute(TTXConstants.USERSETTINGS_O_ENCODING);
            o_encoding = (o_encodingAtt == null ? "" : o_encodingAtt.getValue());

            Attribute trgLangAtt = ele
                    .attribute(TTXConstants.USERSETTINGS_TARGET_LANGUAGE);
            targetLanguage = (trgLangAtt == null ? "" : trgLangAtt.getValue());

            Attribute srcDocumentPathAtt = ele
                    .attribute(TTXConstants.USERSETTINGS_SOURCE_DOCUMENT_PATH);
            sourceDocumentPath = (srcDocumentPathAtt == null ? ""
                    : srcDocumentPathAtt.getValue());

            Attribute userIdAtt = ele
                    .attribute(TTXConstants.USERSETTINGS_USERID);
            userId = (userIdAtt == null ? "" : userIdAtt.getValue());

            Attribute dataTypeVersionAtt = ele
                    .attribute(TTXConstants.USERSETTINGS_DATA_TYPE_VERSION);
            dataTypeVersion = (dataTypeVersionAtt == null ? ""
                    : dataTypeVersionAtt.getValue());

            Attribute settingsPathAtt = ele
                    .attribute(TTXConstants.USERSETTINGS_SETTINGS_PATH);
            settingsPath = (settingsPathAtt == null ? "" : settingsPathAtt
                    .getValue());

            Attribute settingsNameAtt = ele
                    .attribute(TTXConstants.USERSETTINGS_SETTINGS_NAME);
            settingsname = (settingsNameAtt == null ? "" : settingsNameAtt
                    .getValue());

            Attribute trgDefaultFontAtt = ele
                    .attribute(TTXConstants.USERSETTINGS_TARGET_DEFAULT_FONT);
            targetDefaultFont = (trgDefaultFontAtt == null ? ""
                    : trgDefaultFontAtt.getValue());
        }
        catch (Exception e)
        {
            s_logger.error("Error occurs duing parsing userSettings.", e);
        }
    }

    /**
     * Parse extra info for uploading such as "pageId", "taskId" etc.
     * 
     * @param p_rawElement
     */
    private void parseHeaderInfo(Element p_rawElement)
    {
        if (p_rawElement == null)
        {
            return;
        }

        Iterator utIt = p_rawElement.elementIterator(TTXConstants.UT);
        while (utIt.hasNext())
        {
            Element utEle = (Element) utIt.next();
            Attribute displayTextAtt = utEle
                    .attribute(TTXConstants.UT_ATT_DISPLAYTEXT);
            if (displayTextAtt != null)
            {
                String attValue = displayTextAtt.getValue();
                // ignore locked segment - UT gs:locked segment
                if (TTXConstants.GS_LOCKED_SEGMENT.equalsIgnoreCase(attValue))
                {
                    continue;
                }
                
                String utTextNodeValue = utEle.getStringValue();
                int index = utTextNodeValue.lastIndexOf(":");
                if(index == -1)
                {
                	continue;
                }
                String utName = utTextNodeValue.substring(0, index).trim();
                String utValue = utTextNodeValue.substring(index + 1).trim();
                if (TTXConstants.GS_ENCODING.equalsIgnoreCase(attValue)
                        || "Encoding".equalsIgnoreCase(utName))
                {
                    gs_Encoding = utValue;
                }
                else if (TTXConstants.GS_DOCUMENT_FORMAT
                        .equalsIgnoreCase(attValue)
                        || "Document Format".equalsIgnoreCase(utName))
                {
                    gs_DocumentFormat = utValue;
                }
                else if (TTXConstants.GS_PLACEHOLDER_FORMAT
                        .equalsIgnoreCase(attValue)
                        || "Placeholder Format".equalsIgnoreCase(utName))
                {
                    gs_PlaceholderFormat = utValue;
                }
                else if (TTXConstants.GS_SOURCE_LOCALE
                        .equalsIgnoreCase(attValue)
                        || "Source Locale".equalsIgnoreCase(utName))
                {
                    gs_SourceLocale = utValue;
                }
                else if (TTXConstants.GS_TARGET_LOCALE
                        .equalsIgnoreCase(attValue)
                        || "Target Locale".equalsIgnoreCase(utName))
                {
                    gs_TargetLocale = utValue;
                }
                else if (TTXConstants.GS_PAGEID.equalsIgnoreCase(attValue)
                        || "Page ID".equalsIgnoreCase(utName))
                {
                    gs_PageID = utValue;
                }
                else if (TTXConstants.GS_WORKFLOW_ID.equalsIgnoreCase(attValue)
                        || "Workflow ID".equalsIgnoreCase(utName))
                {
                    gs_WorkflowID = utValue;
                }
                else if (TTXConstants.GS_TASK_ID.equalsIgnoreCase(attValue)
                        || "Task ID".equalsIgnoreCase(utName))
                {
                    gs_TaskID = utValue;
                }
                else if (TTXConstants.GS_EXACT_MATCH_WORD_COUNT
                        .equalsIgnoreCase(attValue)
                        || "Exact Match word count".equalsIgnoreCase(utName))
                {
                    gs_ExactMatchWordCount = utValue;
                }
                else if (TTXConstants.GS_FUZZY_MATCH_WORD_COUNT
                        .equalsIgnoreCase(attValue)
                        || "Fuzzy Match word count".equalsIgnoreCase(utName))
                {
                    gs_FuzzyMatchWordCount = utValue;
                }
                else if (TTXConstants.GS_EDIT_ALL.equalsIgnoreCase(attValue)
                        || "Edit all".equalsIgnoreCase(utName))
                {
                    gs_EditAll = utValue;
                }
                else if (TTXConstants.GS_POPULATE_100_TARGET_SEGMENTS.equals(attValue)
                        ||"Populate 100% Target Segments".equalsIgnoreCase(utName))
                {
                    gs_Populate100TargetSegments = utValue;
                }
            }
        }
    }

    /**
     * Append header infos.
     */
    private void appendHeaderInfo()
    {
        // Required
        results.append("# GlobalSight Download File").append(
                TTXConstants.NEW_LINE);
        results.append("# Encoding: ").append(this.gs_Encoding)
                .append(TTXConstants.NEW_LINE);
        results.append("# Document Format: ").append(this.gs_DocumentFormat)
                .append(TTXConstants.NEW_LINE);
        results.append("# Placeholder Format: ")
                .append(this.gs_PlaceholderFormat)
                .append(TTXConstants.NEW_LINE);
        results.append("# Source Locale: ").append(this.gs_SourceLocale)
                .append(TTXConstants.NEW_LINE);
        results.append("# Target Locale: ").append(this.gs_TargetLocale)
                .append(TTXConstants.NEW_LINE);
        results.append("# Page ID: ").append(this.gs_PageID)
                .append(TTXConstants.NEW_LINE);
        results.append("# Workflow ID: ").append(this.gs_WorkflowID)
                .append(TTXConstants.NEW_LINE);
        results.append("# Task ID: ").append(this.gs_TaskID)
                .append(TTXConstants.NEW_LINE);

        // Optional
        results.append("# Exact Match word count: ")
                .append(this.gs_ExactMatchWordCount)
                .append(TTXConstants.NEW_LINE);
        results.append("# Fuzzy Match word count: ")
                .append(this.gs_FuzzyMatchWordCount)
                .append(TTXConstants.NEW_LINE);
        results.append("# Populate 100% Target Segments: ")
                .append(this.gs_Populate100TargetSegments)
                .append(TTXConstants.NEW_LINE);
        results.append("# Edit all: ").append(this.gs_EditAll);
    }

    /**
     * Judge if current "ut" is a header info UT by the "displayText" value.
     * 
     * @param p_value
     * @return boolean
     */
    private boolean isHeaderInfo(String p_value)
    {
        if (p_value == null || "".equals(p_value.trim()))
        {
            return false;
        }

        p_value = p_value.trim();
        if (p_value.indexOf("...") > -1
                || TTXConstants.GS_ENCODING.equalsIgnoreCase(p_value)
                || TTXConstants.GS_DOCUMENT_FORMAT.equalsIgnoreCase(p_value)
                || TTXConstants.GS_PLACEHOLDER_FORMAT.equalsIgnoreCase(p_value)
                || TTXConstants.GS_SOURCE_LOCALE.equalsIgnoreCase(p_value)
                || TTXConstants.GS_TARGET_LOCALE.equalsIgnoreCase(p_value)
                || TTXConstants.GS_PAGEID.equalsIgnoreCase(p_value)
                || TTXConstants.GS_WORKFLOW_ID.equalsIgnoreCase(p_value)
                || TTXConstants.GS_TASK_ID.equalsIgnoreCase(p_value)
                || TTXConstants.GS_EXACT_MATCH_WORD_COUNT
                        .equalsIgnoreCase(p_value)
                || TTXConstants.GS_FUZZY_MATCH_WORD_COUNT
                        .equalsIgnoreCase(p_value)
                || TTXConstants.GS_EDIT_ALL.equalsIgnoreCase(p_value)
                || "GS:InstanceID".equalsIgnoreCase(p_value)
                || TTXConstants.GS_POPULATE_100_TARGET_SEGMENTS.equalsIgnoreCase(p_value))
        {
            return true;
        }

        return false;
    }

    private boolean isTuId(String p_value)
    {
        if (p_value == null || "".equals(p_value.trim()))
        {
            return false;
        }

        p_value = p_value.trim();
        if (p_value.startsWith("TuId:"))
        {
            return true;
        }

        return false;
    }

    /**
     * Parse main contents
     * 
     * @param p_element
     */
    private void domNodehandler(Node p_node, boolean isSource)
    {
        // public static final short ANY_NODE 0
        // public static final short ATTRIBUTE_NODE 2
        // public static final short CDATA_SECTION_NODE 4
        // public static final short COMMENT_NODE 8
        // public static final short DOCUMENT_NODE 9
        // public static final short DOCUMENT_TYPE_NODE 10
        // public static final short ELEMENT_NODE 1
        // public static final short ENTITY_REFERENCE_NODE 5
        // public static final short MAX_NODE_TYPE 14
        // public static final short NAMESPACE_NODE 13
        // public static final short PROCESSING_INSTRUCTION_NODE 7
        // public static final short TEXT_NODE 3
        // public static final short UNKNOWN_NODE 14
        if (p_node == null)
        {
            return;
        }

        switch (p_node.getNodeType())
        {
            case Node.ELEMENT_NODE:
                elementNodeProcessor(p_node, isSource);

                break;
            case Node.TEXT_NODE:
                String nodeValue = p_node.getStringValue();
                if (nodeValue.startsWith("#"))
                {
                    nodeValue = nodeValue.replaceFirst("#", OfflineConstants.PONUD_SIGN);
                }
                if (isParsingTTXForGS)
                {
                    boolean isInTargetTuv = isInTargetTuv(p_node);
                    if (nodeValue != null && isInTargetTuv)
                    {
                        results.append(nodeValue);
                    }
                    else if (nodeValue != null && isLockedSegment(p_node))
                    {
                        results.append(
                                AmbassadorDwUpConstants.SEGMENT_MATCH_TYPE_KEY)
                                .append(" ")
                                .append("DO NOT TRANSLATE OR MODIFY (Locked).")
                                .append(TTXConstants.NEW_LINE);
                        results.append(nodeValue);
                    }
                }
                else
                {
                    results.append(nodeValue);
                }
                break;
            default:

                return;
        }
    }

    private void elementNodeProcessor(Node p_node, boolean p_isSource)
    {
        Element element = (Element) p_node;
        String eleStr = element.asXML();
        String elementName = element.getName();

        boolean isHeaderInfoUT = false;
        boolean isTuId = false;
        if (TTXConstants.TU.equalsIgnoreCase(elementName)
                || (element.getParent() != null && TTXConstants.TUV
                        .equalsIgnoreCase(element.getParent().getName())))
        {
            // latestPosition = TTXConstants.IN_TU;
        }
        else if (TTXConstants.UT.equalsIgnoreCase(elementName))
        {
            Attribute att = element.attribute(TTXConstants.UT_ATT_DISPLAYTEXT);
            if (att != null)
            {
                String value = att.getValue();
                // If header info,return as header info has been handled
                // separately.
                // This check is not required.
                isHeaderInfoUT = isHeaderInfo(value);
                if (isHeaderInfoUT)
                {
                    return;
                }
                // If TuId,handle them here.
                isTuId = isTuId(value);
                if (isTuId)
                {
                    // latestPosition = TTXConstants.TU_ID;
                    String tuId = value.substring(value.indexOf(":") + 1)
                            .trim();
                    if (results != null && results.length() > 0)
                    {
                        results.append(TTXConstants.NEW_LINE).append(
                                TTXConstants.NEW_LINE);
                    }
                    results.append(TTXConstants.HASH_MARK).append(tuId)
                            .append(TTXConstants.NEW_LINE);

                    return;
                }
            }
        }

        if (element.nodeCount() > 0)
        {
            Iterator nodesIt = element.nodeIterator();
            while (nodesIt.hasNext())
            {
                Node node = (Node) nodesIt.next();
                String nodeStr = node.asXML();
                String nodeName = node.getName();
                if (TTXConstants.TUV.equalsIgnoreCase(node.getName()))
                {
                    Attribute langAtt = ((Element) node)
                            .attribute(TTXConstants.TUV_ATT_LANG);
                    String lang = null;
                    if (langAtt != null)
                    {
                        lang = langAtt.getValue();
                    }
                    if (sourceLanguage != null && sourceLanguage.equals(lang))
                    {
                        // latestPosition = TTXConstants.IN_SOURCE_TUV;
                        // Not handle source TUV for TTX off-line uploading.
                        // domNodehandler(node, true);
                    }
                    else
                    {
                        // latestPosition = TTXConstants.IN_TARGET_TUV;
                        domNodehandler(node, false);
                    }
                }
                else
                {
                    domNodehandler(node, false);
                }
            }
        }
        else
        {
            if (TTXConstants.UT.equalsIgnoreCase(elementName))
            {
                Attribute displayTextAtt = element
                        .attribute(TTXConstants.UT_ATT_DISPLAYTEXT);
                if (displayTextAtt != null)
                {
                    String attValue = displayTextAtt.getValue();
                    if (attValue != null
                            && attValue.startsWith(TTXConstants.TU_ID))
                    {
                        // latestPosition = TTXConstants.TU_ID;
                        String tuId = attValue.substring(
                                attValue.indexOf(":") + 1).trim();
                        if (results != null && results.length() > 0)
                        {
                            results.append(TTXConstants.NEW_LINE).append(
                                    TTXConstants.NEW_LINE);
                        }
                        results.append(TTXConstants.HASH_MARK).append(tuId)
                                .append(TTXConstants.NEW_LINE);
                    }
                    else if (attValue != null
                            && attValue.startsWith(TTXConstants.GS))
                    {
                        Attribute typeValueAtt = element
                                .attribute(TTXConstants.UT_ATT_TYPE);
                        String typeValue = null;
                        if (typeValueAtt != null)
                        {
                            typeValue = typeValueAtt.getValue();
                        }

                        String gsTag = attValue.substring(
                                attValue.indexOf(":") + 1).trim();
                        if (typeValue != null
                                && TTXConstants.UT_ATT_TYPE_START
                                        .equalsIgnoreCase(typeValue))
                        {
                            results.append("[").append(gsTag).append("]");
                        }
                        else if (typeValue != null
                                && TTXConstants.UT_ATT_TYPE_END
                                        .equalsIgnoreCase(typeValue))
                        {
                            results.append("[/").append(gsTag).append("]");
                        }
                        else
                        {
                            results.append("[").append(gsTag).append("]");
                            results.append("[/").append(gsTag).append("]");
                        }
                    }
                }
            }
            else if (TTXConstants.DF.equalsIgnoreCase(elementName))
            {
                // do not handle this.
            }
        }
    }

    private void appendEndInfo()
    {
        results.append(TTXConstants.NEW_LINE);
        results.append(TTXConstants.NEW_LINE);
        results.append("# END GlobalSight Download File");
    }

    /**
     * Judge if current node is in target TUV element.
     * 
     * @param p_node
     * @return
     */
    private boolean isInTargetTuv(Node p_node)
    {
        boolean result = false;
        if (p_node == null)
        {
            return false;
        }

        Element parentNode = p_node.getParent();
        while (parentNode != null)
        {
            // In Tuv.
            String nodeName = parentNode.getName();
            if ("Tuv".equalsIgnoreCase(nodeName))
            {
                Attribute langAtt = parentNode
                        .attribute(TTXConstants.TUV_ATT_LANG);
                String attName = langAtt.getValue();
                if (langAtt != null && targetLanguage.equalsIgnoreCase(attName))
                {
                    result = true;
                }

                break;
            }

            parentNode = parentNode.getParent();
        }

        return result;
    }
    
    private boolean isLockedSegment(Node p_node)
    {
        boolean result = false;
        if (p_node == null)
        {
            return false;
        }

        Element parentNode = p_node.getParent();
        if (parentNode != null)
        {
            // In Tuv.
            String nodeName = parentNode.getName();
            if ("ut".equalsIgnoreCase(nodeName))
            {
                Attribute attt = parentNode
                        .attribute(TTXConstants.UT_ATT_DISPLAYTEXT);
                if (attt != null)
                {
                    String attValue = attt.getValue();
                    result = TTXConstants.GS_LOCKED_SEGMENT
                            .equalsIgnoreCase(attValue);
                }
            }
        }

        return result;
    }

    public Document getDocument(Reader reader) throws DocumentException
    {
        SAXReader saxReader = new SAXReader();
        Document document = (Document) saxReader.read(reader);
        return document;
    }

    public Document getDocument(File file) throws Exception
    {
        SAXReader saxReader = new SAXReader();
        Document document = (Document) saxReader.read(file);
        return document;
    }

    public Element getRootNode(Document doc) throws Exception
    {
        Element root = doc.getRootElement();
        return root;
    }

    public String getEnd() throws Exception
    {
        String result = "";
        result += "# END GlobalSight Download File";

        return result;
    }

    public String getHeader(Document doc) throws Exception
    {
        StringBuilder result = new StringBuilder();

        return result.toString();
    }

    public String getInfoFromAnnotation(String annotation, String condition)
    {
        String documentFormat = "";
        int start = annotation.indexOf(condition);
        start = start + condition.length();
        documentFormat = annotation.substring(start);
        int end = documentFormat.indexOf("\n");
        return annotation.substring(start, start + end);
    }

    /*
     * private class TransUnitInner { private String id;
     * 
     * private String source;
     * 
     * private String target;
     * 
     * private String matchType;
     * 
     * public String getMatchType() { return matchType; }
     * 
     * public void setMatchType(String matchType) { this.matchType = matchType;
     * }
     * 
     * public String getId() { return id; }
     * 
     * public void setId(String string) { this.id = string; }
     * 
     * public String getSource() { return source; }
     * 
     * public void setSource(String source) { this.source = source; }
     * 
     * public String getTarget() { return target; }
     * 
     * public void setTarget(String target) { this.target = target; }
     * 
     * public String toString() { StringBuffer result = new StringBuffer();
     * result.append("id:").append(id).append("\n");
     * result.append("matchType:").append(matchType).append("\n");
     * result.append("source:").append(source).append("\n");
     * result.append("target:").append(target).append("\n"); return
     * result.toString();
     * 
     * }
     * 
     * }
     */

}
