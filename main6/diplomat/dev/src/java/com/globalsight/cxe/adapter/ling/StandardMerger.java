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
package com.globalsight.cxe.adapter.ling;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.globalsight.cxe.adapter.msoffice.OfficeXmlRepairer;
import com.globalsight.cxe.adapter.openoffice.OpenOfficeHelper;
import com.globalsight.cxe.engine.util.FileUtils;
import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.cxe.entity.fileprofile.FileProfileImpl;
import com.globalsight.cxe.entity.filterconfiguration.FilterConstants;
import com.globalsight.cxe.message.CxeMessage;
import com.globalsight.cxe.message.CxeMessageType;
import com.globalsight.cxe.message.MessageData;
import com.globalsight.cxe.message.MessageDataFactory;
import com.globalsight.cxe.message.MessageDataReader;
import com.globalsight.diplomat.util.Logger;
import com.globalsight.diplomat.util.XmlUtil;
import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.diplomat.util.database.ConnectionPoolException;
import com.globalsight.everest.page.pageexport.style.MifStyleUtil;
import com.globalsight.everest.projecthandler.exporter.ExportUtil;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.ling.common.TranscoderException;
import com.globalsight.ling.docproc.DiplomatAPI;
import com.globalsight.ling.docproc.DiplomatMergerException;
import com.globalsight.ling.docproc.IFormatNames;
import com.globalsight.ling.docproc.extractor.xml.XmlFilterHelper;
import com.globalsight.ling.docproc.merger.paginated.PaginatedMerger;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.FileUtil;
import com.globalsight.util.Replacer;
import com.globalsight.util.StringUtil;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.edit.SegmentUtil;

/**
 * StandardMerger
 * <p>
 * The StandardMerger is a wrapper for calls made to the DiplomatAPI for merging
 * DiplomatXML back into whatever the original format was. (Should work even on
 * formats the DiplomatAPI cannot extract).
 */
public class StandardMerger implements IFormatNames
{
    static private final org.apache.log4j.Logger m_logger = org.apache.log4j.Logger
            .getLogger(StandardMerger.class);

    private String m_sourceLocale = null;
    private String m_targetLocale = null;
    private boolean m_keepGsTags = false;
    private String m_targetEncoding = null;
    private String m_fileName = "";
    private String m_relSafeName = "";
    private CxeMessage m_cxeMessage = null;
    private String[] m_errorArgs = new String[1];
    private String m_postMergeEvent = null;
    private String m_formatType = null;
    private SystemConfiguration m_config = null;
    private String m_fileProfile = null;
    private boolean m_isPreview = false;

    private long filterId;
    private String filterTableName;

    private static final String CONFIG_FILE = "/properties/LingAdapter.properties";
    private static Pattern SPAN_PATTERN = Pattern.compile(
            "(<span [^<>]+lastCR[^<>]+>)[\\r\\n]</span>", Pattern.DOTALL);

    /**
     * Creates a StandardMerger object
     * 
     * @param p_logger
     *            globalsight category to use
     * @param p_cxeMessage
     *            CxeMessage to work on
     * @param p_config
     *            -- system configuration for properties
     */
    public StandardMerger(CxeMessage p_cxeMessage, SystemConfiguration p_config)
    {
        m_cxeMessage = p_cxeMessage;
        m_config = p_config;
        if (m_config == null)
        {
            m_config = SystemConfiguration.getInstance(CONFIG_FILE);
        }
    }

    /**
     * Converts the GXML content in the MessageData back to the original text
     * format by removing the GXML markup.
     * 
     * @return MessageData corresponding to content in the original format
     * @exception LingAdapterException
     */
    MessageData merge() throws LingAdapterException
    {
        try
        {
            String s = getContent();

            MessageData fmd = MessageDataFactory.createFileMessageData();
            BufferedOutputStream bos = new BufferedOutputStream(
                    fmd.getOutputStream());
            OutputStreamWriter osw = new OutputStreamWriter(bos,
                    m_targetEncoding);

            osw.write(s, 0, s.length());
            osw.close();

            return fmd;
        }
        catch (DiplomatMergerException dme)
        {
            m_logger.error("Merger Exception: ", dme);
            throw new LingAdapterException("MergeException", m_errorArgs, dme);
        }
        catch (Exception e)
        {
            m_logger.error("Exception during merging", e);
            throw new LingAdapterException("CxeInternal", m_errorArgs, e);
        }
    }

    public String getPreviewContent() throws LingAdapterException
    {
        m_isPreview = true;
        return getMergeContent();
    }

    public String getContent() throws LingAdapterException
    {
        m_isPreview = false;
        return getMergeContent();
    }

    private String getMergeContent() throws LingAdapterException
    {
        parseEventFlowXml();
        try
        {
            // the DiplomatAPI should be changed at some point to take in a
            // filename of GXML and return a filename or inputstream to the
            // content because this might be too big to hold in memory.
            String gxml = readGxml();

            gxml = ExportUtil.replaceWhitespace(gxml, m_targetLocale);

            m_logger.info("Merging: " + m_fileName + ", size: "
                    + m_cxeMessage.getMessageData().getSize());
            byte[] mergeResult = null;
            if (m_cxeMessage.getMessageType().getValue() == CxeMessageType.PRSXML_LOCALIZED_EVENT)
                mergeResult = mergeWithPrsXmlMerger(gxml);
            else
                mergeResult = mergeWithDiplomat(gxml);

            m_logger.info("Done Merging: " + m_fileName + ", result size: "
                    + mergeResult.length);
            Logger.writeDebugFile("lam_merge.txt", mergeResult);

            String s = new String(mergeResult, m_targetEncoding);

            s = handleReturns(s);

            if (FORMAT_XML.equals(m_formatType)
                    && filterId != -1
                    && FilterConstants.XMLRULE_TABLENAME
                            .equals(filterTableName))
            {
                s = XmlFilterHelper
                        .saveNonAsciiAs(s, filterId, filterTableName);
            }

            if (FORMAT_MIF.equals(m_formatType))
            {
                MifStyleUtil util = new MifStyleUtil();
                s = util.updateStringBeforExport(s);
            }

            return fixGxml(s);
        }
        catch (DiplomatMergerException dme)
        {
            m_logger.error("Merger Exception: ", dme);
            throw new LingAdapterException("MergeException", m_errorArgs, dme);
        }
        catch (Exception e)
        {
            m_logger.error("Exception during merging", e);
            throw new LingAdapterException("CxeInternal", m_errorArgs, e);
        }
    }

    private String fixGxml(String p_mergeResult) throws Exception
    {
        // Do not fix GXML for HTML, this is because if we replace consecutive
        // "&nbsp;" with " ", it will cause HTML formatting error.
        // also do not fix &nbsp; for xml because of GBS-3577
        if (m_cxeMessage.getMessageType().getValue() != CxeMessageType.HTML_LOCALIZED_EVENT
                && m_cxeMessage.getMessageType().getValue() != CxeMessageType.XML_LOCALIZED_EVENT)
        {
            // this follows the original logic.
            p_mergeResult = fixGxml(p_mergeResult, "&nbsp;", " ");
            p_mergeResult = fixGxml(p_mergeResult, "&nbsp", " ");
        }

        if (isWordHtml())
        {
            p_mergeResult = fixWordHtml(p_mergeResult);
        }
        if (isPowerPointHtml())
        {
            p_mergeResult = fixPowerPointHtml(p_mergeResult);
        }
        if (isOpenOfficeXml())
        {
            p_mergeResult = fixOpenOfficeXml(p_mergeResult);
        }
        if (isOfficeXml())
        {
            p_mergeResult = fixOfficeXml(p_mergeResult);
        }

        if (isRestoreInvalidUnicodeChar())
        {
            p_mergeResult = SegmentUtil
                    .restoreInvalidUnicodeChar(p_mergeResult);
        }

        return p_mergeResult;
    }

    private String fixGxml(String p_mergeResult, String oldStr, String newStr)
    {
        StringBuffer sb = new StringBuffer(p_mergeResult);
        int pos1 = 0;
        while (pos1 >= 0)
        {
            pos1 = sb.indexOf(oldStr, pos1);

            if (pos1 > 0)
            {
                // Do not change CDATA content in XML.
                if (isXmlLocalizationEvent() && isInXmlCdata(sb, pos1))
                {
                    pos1 = pos1 + 1;
                    continue;
                }

                sb.replace(pos1, pos1 + oldStr.length(), newStr);
            }
        }
        return sb.toString();
    }

    private boolean isXmlLocalizationEvent()
    {
        return (m_cxeMessage.getMessageType().getValue() == CxeMessageType.XML_LOCALIZED_EVENT);
    }

    private String fixWordHtml(String p_mergeResult) throws Exception
    {
        if (p_mergeResult != null && !"".equals(p_mergeResult.trim()))
        {
            // Remove the tag <title>XX</title> in the gxml to resolve the
            // Fragmented markup in RTF document results in empty export
            // issue.
            int startIndex = p_mergeResult.indexOf("<title>");
            int endIndex = p_mergeResult.indexOf("</title>");

            if (startIndex != -1 && endIndex != -1)
            {
                int lengthOfEndTag = "</title>".length();
                String titleText = p_mergeResult.substring(startIndex, endIndex
                        + lengthOfEndTag);
                p_mergeResult = StringUtil
                        .replace(p_mergeResult, titleText, "");
            }

            // remove PicExportError in
            // list-style-image:url("PicExportError");
            startIndex = p_mergeResult.indexOf("<head>");
            endIndex = p_mergeResult.indexOf("</head>");

            if (startIndex != -1 && endIndex != -1)
            {
                String headString = p_mergeResult.substring(startIndex,
                        endIndex);

                if (headString
                        .contains("list-style-image:url(\"PicExportError\");"))
                {
                    String before = p_mergeResult.substring(0, startIndex);
                    String end = p_mergeResult.substring(endIndex);
                    headString = StringUtil.replace(headString,
                            "list-style-image:url(\"PicExportError\");",
                            "list-style-image:url(\"\");");
                    p_mergeResult = before + headString + end;
                }
            }
        }
        return p_mergeResult;
    }

    /**
     * Fix issues exits in PowerPoint HTML
     */
    private String fixPowerPointHtml(String p_mergeResult) throws Exception
    {
        // restore lastCR to &#13;
        if (!p_mergeResult.contains("lastCR"))
        {
            return p_mergeResult;
        }

        p_mergeResult = StringUtil.replaceWithRE(p_mergeResult, SPAN_PATTERN,
                new Replacer()
                {
                    @Override
                    public String getReplaceString(Matcher m)
                    {
                        return m.group(1) + "&#13;</span>";
                    }
                });

        return p_mergeResult;
    }

    /**
     * Fix issues in open office files
     */
    private String fixOpenOfficeXml(String p_content) throws Exception
    {
        if (m_relSafeName != null && m_relSafeName.length() > 0)
        {
            if (m_relSafeName.endsWith(OpenOfficeHelper.XML_CONTENT)
                    && m_relSafeName.toLowerCase().contains(".ods.1"))
            {
                String oriXmlPath = OpenOfficeHelper.getConversionDir()
                        + File.separator + m_sourceLocale + File.separator
                        + m_relSafeName;
                File oriXmlFile = new File(oriXmlPath);
                if (oriXmlFile.exists())
                {
                    String oriXml = FileUtils.read(oriXmlFile, "UTF-8");
                    return OpenOfficeHelper.fixContentXmlForOds(p_content,
                            oriXml, m_sourceLocale, m_targetLocale,
                            m_relSafeName);
                }
            }
        }

        return p_content;
    }

    /**
     * Fix issues in office xml, office 2010 files
     */
    private String fixOfficeXml(String p_content) throws Exception
    {
        boolean isRtlLocale = EditUtil.isRTLLocale(m_targetLocale);
        if (isRtlLocale)
        {
            return OfficeXmlRepairer.fixRtlLocale(p_content, m_targetLocale);
        }

        return p_content;
    }

    /**
     * Check if this position is in xml cdata
     * 
     * @param sb
     * @param pos1
     * @return
     */
    private boolean isInXmlCdata(StringBuffer sb, int pos1)
    {
        String cdataS = "<![CDATA[";
        String cdataE = "]]>";

        if (pos1 > 0 && pos1 < sb.length())
        {
            String pre = sb.substring(0, pos1);
            int indexS = pre.lastIndexOf(cdataS);
            int indexE = pre.lastIndexOf(cdataE);

            if (indexS > indexE)
            {
                return true;
            }
        }

        return false;
    }

    private byte[] mergeWithDiplomat(String p_gxml) throws Exception
    {
        byte[] mergeResult = null;
        DiplomatAPI diplomat = new DiplomatAPI();
        try
        {
            diplomat.setFileProfileId(m_fileProfile);
            diplomat.setFilterId(filterId);
            diplomat.setFilterTableName(filterTableName);
            diplomat.setTargetLocale(m_targetLocale);
            diplomat.setCxeMessage(m_cxeMessage);
            diplomat.setPreview(m_isPreview);
            mergeResult = diplomat
                    .merge(p_gxml, m_targetEncoding, m_keepGsTags);
        }
        catch (TranscoderException e)
        {
            m_logger.error("Merger encoding exception. Using default encoding",
                    e);
            mergeResult = diplomat.merge(p_gxml, m_keepGsTags).getBytes();
        }

        mergeResult = postProcessResult(mergeResult);

        return mergeResult;
    }

    /**
     * Does any special post processing which is hardcoded. This is separate
     * from the user of pre and post processors in CXE.
     * 
     * @param p_mergeResult
     */
    private byte[] postProcessResult(byte[] p_mergeResult)
    {
        byte[] result = p_mergeResult;
        try
        {
            if (DiplomatAPI.FORMAT_XML.equals(m_formatType))
            {
                String xml = new String(p_mergeResult, m_targetEncoding);
                // first find out what the tags to unescape are
                String tagsProperty = m_config
                        .getStringParameter("unescapedTags");
                if (tagsProperty == null || tagsProperty.length() == 0)
                {
                    return p_mergeResult;
                }

                String tags[] = tagsProperty.split(",");
                String xmlRuleText = queryRuleFile();
                if (xmlRuleText == null || xmlRuleText.length() == 0)
                {
                    if (m_logger.isDebugEnabled())
                    {
                        FileProfile fp = ServerProxy
                                .getFileProfilePersistenceManager()
                                .readFileProfile(Long.parseLong(m_fileProfile));

                        if (m_logger.isDebugEnabled())
                        {
                            m_logger.debug("No xml rule found for file profile \""
                                    + fp.getName() + "\"");
                        }
                    }
                    return p_mergeResult;
                }
                List tagsHere = new ArrayList();
                org.dom4j.Document doc = org.dom4j.DocumentHelper
                        .parseText(xmlRuleText);
                List tranList = org.dom4j.DocumentHelper.selectNodes(
                        "//translate", doc);
                StringBuffer exIndex = new StringBuffer();
                for (Iterator iter = tranList.iterator(); iter.hasNext();)
                {
                    org.dom4j.Node node = (org.dom4j.Node) iter.next();

                    if (node instanceof org.dom4j.Element
                            && ((org.dom4j.Element) node)
                                    .attributeValue("path") != null)
                    {
                        org.dom4j.Element element = (org.dom4j.Element) node;
                        for (int i = 0; i < tags.length; i++)
                        {
                            if (exIndex.toString().indexOf("" + i) == -1
                                    && element.attributeValue("path").endsWith(
                                            "/" + tags[i])
                                    && element
                                            .attributeValue("containedInHtml") != null
                                    && element
                                            .attributeValue("containedInHtml")
                                            .equals("yes"))
                            {
                                tagsHere.add(tags[i]);
                                exIndex.append(i);
                            }
                        }
                    }
                    if (exIndex.length() == tags.length)
                        break;
                }

                String newxml = xml;
                tags = new String[tagsHere.size()];
                for (int i = 0; i < tagsHere.size(); i++)
                {
                    tags[i] = (String) tagsHere.get(i);
                }

                // now go through the tags and replace them
                newxml = recoverTags(tags, newxml);
                result = newxml.getBytes(m_targetEncoding);
            }
        }
        catch (Exception e)
        {
            m_logger.error("Failed to (internal) post process merge result: ",
                    e);
            result = p_mergeResult; // just return the original merge result
        }
        // Runtime.getRuntime().gc();
        return result;
    }

    private String recoverTags(String[] p_tags, String p_xml) throws Exception
    {
        String result = p_xml;
        try
        {
            String tagsFile = "/properties/Tags.properties";
            SystemConfiguration tagsProperties = SystemConfiguration
                    .getInstance(tagsFile);
            String pairedTagHtml[] = tagsProperties.getStringParameter(
                    "PairedTag_html").split(",");
            String unpairedTagHtml[] = tagsProperties.getStringParameter(
                    "UnpairedTag_html").split(",");

            for (int i = 0; p_tags != null && p_tags.length > i; i++)
            {
                String tag = p_tags[i];
                boolean isPairedTagHtml = isInTheArray(tag, pairedTagHtml);
                boolean isUnairedTagHtml = isInTheArray(tag, unpairedTagHtml);

                // <p> </p> ... can be recoverd here
                if (isPairedTagHtml
                        && result.indexOf("&lt;" + tag + "&gt;") != -1)
                {
                    String regex1 = "\\&lt;" + tag + "\\&gt;";
                    String ex1 = "<" + tag + ">";
                    String regex2 = "\\&lt;/" + tag + "\\&gt;";
                    String ex2 = "</" + tag + ">";
                    result = StringUtil.replaceWithRE(result, regex1, ex1);
                    result = StringUtil.replaceWithRE(result, regex2, ex2);
                }

                // <br/> <hr /> ... can be recoverd here
                if (isUnairedTagHtml
                        && (result.indexOf("&lt;" + tag + "/&gt;") != -1 || result
                                .indexOf("&lt;" + tag + " /&gt;") != -1))
                {
                    String regex1 = "\\&lt;" + tag + "\\/&gt;";
                    String ex1 = "<" + tag + "/>";
                    String regex2 = "\\&lt;" + tag + "\\ /&gt;";
                    String ex2 = "<" + tag + " />";

                    result = StringUtil.replaceWithRE(result, regex1, ex1);
                    result = StringUtil.replaceWithRE(result, regex2, ex2);
                }

                // <a href="www.Welocalize.com" href="_blank">Welocalize</a>
                // <input type="button" value=">>" />
                // <input type="textarea" value="'Hello,' he said, 'b>a '" >
                // ... can be recoverd here
                // index_1 is the "&lt;tag " 's index
                int index_1 = result.indexOf("&lt;" + tag + " ");
                if (index_1 != -1
                        && ((isPairedTagHtml && result.indexOf("&lt;/" + tag
                                + "&gt;", index_1 + 4) != -1) || (isUnairedTagHtml && result
                                .indexOf("&gt;", index_1 + 4) != -1)))
                {
                    StringBuffer xmlBuffer = new StringBuffer();
                    String tempxml = result;

                    // index_2 is the matched "&gt;" 's index
                    int index_2 = tempxml.indexOf("&gt;", index_1);
                    while (index_1 != -1 && index_2 != -1)
                    {
                        xmlBuffer.append(tempxml.substring(0, index_1));
                        // replace the first &lt; to <
                        tempxml = "<" + tempxml.substring(index_1 + 4);
                        index_2 = tempxml.indexOf("&gt;");

                        // handle the special content like &amp;quot; to &quot;
                        String content = tempxml.substring(0, index_2);
                        String contentAfterDecode = XmlUtil
                                .unescapeString(content);

                        // to find the matched "&gt;" 's index (index_2) for
                        // "&lt;tag "
                        // See the example below, we need to find the second ">"
                        // what we do is to check if the ">" is in '"' or '\''.
                        // <input type="textarea" value="'Hello,' he said, 'b>a
                        // '" >
                        int index_equalSign = contentAfterDecode.indexOf('=');
                        int index_sign2 = 0;
                        while (true)
                        {
                            int aposCount = 0;
                            int quotCount = 0;

                            if (index_sign2 == 0)
                                index_sign2 = 1;
                            String temp = (index_equalSign != -1) ? contentAfterDecode
                                    .substring(index_equalSign) : "";
                            while (index_sign2 > 0
                                    && (temp.indexOf('"') != -1 || temp
                                            .indexOf('\'') != -1))
                            {
                                quotCount = aposCount = 0;
                                index_sign2 = 0;
                                boolean countQuot = false;

                                if (temp.indexOf('\'') == -1
                                        || (temp.indexOf('"') < temp
                                                .indexOf('\'') && temp
                                                .indexOf('"') != -1))
                                {
                                    countQuot = true;
                                }
                                for (int j = 0; j < temp.length(); j++)
                                {
                                    if (countQuot && temp.charAt(j) == '"')
                                    {
                                        quotCount++;
                                    }
                                    else if (!countQuot
                                            && temp.charAt(j) == '\'')
                                    {
                                        aposCount++;
                                    }
                                    if ((quotCount + aposCount) == 2
                                            && index_sign2 == 0)
                                    {
                                        index_sign2 = j;
                                    }
                                }

                                if (index_sign2 > 0
                                        && (quotCount + aposCount) >= 2)
                                {
                                    index_equalSign = contentAfterDecode
                                            .indexOf('=', index_sign2
                                                    + index_equalSign);
                                    temp = (index_equalSign != -1) ? contentAfterDecode
                                            .substring(index_equalSign) : "";
                                    continue;
                                }
                            }

                            if ((aposCount + quotCount) % 2 == 1)
                            {
                                index_2 = tempxml.indexOf("&gt;", index_2 + 4);

                                content = tempxml.substring(0, index_2);
                                contentAfterDecode = XmlUtil
                                        .unescapeString(content);
                            }
                            else
                            {
                                break;
                            }
                        }
                        xmlBuffer.append(contentAfterDecode);

                        // replace the matched &gt; to <
                        tempxml = ">" + tempxml.substring(index_2 + 4);

                        index_1 = tempxml.indexOf("&lt;" + tag + " ");
                        index_2 = tempxml.indexOf("&gt;", index_1);
                    }
                    xmlBuffer.append(tempxml);
                    result = xmlBuffer.toString();

                    if (isPairedTagHtml)
                    {
                        result = StringUtil.replaceWithRE(result, "\\&lt;/"
                                + tag + "\\&gt;", "</" + tag + ">");
                    }
                }
            }
        }
        catch (Exception e)
        {
            throw e;
        }

        return result;
    }

    private boolean isInTheArray(String p_str, String[] p_array)
    {
        for (int i = 0; i < p_array.length; i++)
        {
            if (p_str.equals(p_array[i]))
            {
                return true;
            }
        }

        return false;
    }

    private byte[] mergeWithPrsXmlMerger(String p_gxml) throws Exception
    {
        byte[] mergeResult = null;
        PaginatedMerger merger = new PaginatedMerger();
        mergeResult = merger.merge(p_gxml, m_targetEncoding);
        return mergeResult;
    }

    /**
     * Returns the CxeMessageType used as the post-merge event. That is, this is
     * the event used to send the content to a target data source adapter, or
     * other format converter after the content has been merged from GXML.
     * 
     * @return CxeMessageType
     */
    public CxeMessageType getPostMergeEvent()
    {
        return CxeMessageType.getCxeMessageType(m_postMergeEvent);
    }

    // ////////////////////////////////////
    // Private Methods //
    // ////////////////////////////////////

    /**
     * Parses the EventFlowXml and sets some internal values
     * 
     * @exception LingAdapterException
     */
    private void parseEventFlowXml() throws LingAdapterException
    {
        // first retrieve the charset from the eventFlowXml
        StringReader sr = null;
        try
        {
            sr = new StringReader(m_cxeMessage.getEventFlowXml());
            InputSource is = new InputSource(sr);
            DOMParser parser = new DOMParser();
            parser.setFeature("http://xml.org/sax/features/validation", false); // don't
                                                                                // validate
            parser.parse(is);
            Element elem = parser.getDocument().getDocumentElement();

            // Get Source EventFlow
            NodeList nl = elem.getElementsByTagName("source");
            Element sourceElement = (Element) nl.item(0);
            m_formatType = sourceElement.getAttribute("formatType");
            m_fileProfile = sourceElement.getAttribute("dataSourceId");
            long fpId = (m_fileProfile == null) ? -1 : Long
                    .parseLong(m_fileProfile);
            filterId = -1;
            if (fpId > 0)
            {
                FileProfileImpl fp = HibernateUtil.get(FileProfileImpl.class,
                        fpId, false);
                filterId = fp.getFilterId();
                filterTableName = fp.getFilterTableName();
            }
            // source locale
            Element sourceLocaleElement = (Element) sourceElement
                    .getElementsByTagName("locale").item(0);
            m_sourceLocale = sourceLocaleElement.getFirstChild().getNodeValue();

            // Get Target EventFlow
            nl = elem.getElementsByTagName("target");
            Element targetElement = (Element) nl.item(0);
            // target encoding
            Element charsetElement = (Element) targetElement
                    .getElementsByTagName("charset").item(0);
            m_targetEncoding = charsetElement.getFirstChild().getNodeValue();
            // target locale
            Element targetLocaleElement = (Element) targetElement
                    .getElementsByTagName("locale").item(0);
            m_targetLocale = targetLocaleElement.getFirstChild().getNodeValue();

            // if this is a source page export, we leave the GS tags intact
            m_keepGsTags = m_targetLocale.equals(m_sourceLocale) ? true : false;

            // get the post merge event
            nl = elem.getElementsByTagName("postMergeEvent");
            Element pmElement = (Element) nl.item(0);
            m_postMergeEvent = pmElement.getFirstChild().getNodeValue();
            // For indd export, the xml encoding needs UTF-8 to let the
            // converter accept.
            // related issue: gbs-1341
            if ("unknown".equals(m_targetEncoding)
                    || CxeMessageType
                            .getCxeMessageType(
                                    CxeMessageType.ADOBE_LOCALIZED_EVENT)
                            .getName().equals(m_postMergeEvent))
            {
                m_targetEncoding = "UTF-8";
            }

            // get the display name
            nl = elem.getElementsByTagName("displayName");
            Element displayNameElement = (Element) nl.item(0);
            m_fileName = displayNameElement.getFirstChild().getNodeValue();
            m_errorArgs[0] = m_fileName;

            // get relSafeName for open office xml
            nl = (isOpenOfficeXml() || isOfficeXml()) ? elem
                    .getElementsByTagName("da") : null;
            if (nl != null && nl.getLength() > 0)
            {
                for (int i = 0, nllen = nl.getLength(); i < nllen; i++)
                {
                    Element daElement = (Element) nl.item(i);
                    String daName = daElement.getAttribute("name");
                    if ("relSafeName".equals(daName))
                    {
                        Node dvElement = daElement.getElementsByTagName("dv")
                                .item(0);
                        if (dvElement != null)
                        {
                            m_relSafeName = dvElement.getFirstChild()
                                    .getNodeValue();
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            m_logger.error(
                    "Unable to parse EventFlowXml. Cannot determine locale, "
                            + "encoding, and format_type for merging.", e);
            throw new LingAdapterException("CxeInternal", m_errorArgs, e);
        }
        finally
        {
            if (sr != null)
            {
                sr.close();
            }
        }
    }

    private boolean isWordHtml()
    {
        return FORMAT_WORD_HTML.equals(m_formatType);
    }

    private boolean isPowerPointHtml()
    {
        return FORMAT_POWERPOINT_HTML.equals(m_formatType);
    }

    private boolean isOfficeXml()
    {
        return FORMAT_OFFICE_XML.equals(m_formatType);
    }

    private boolean isOpenOfficeXml()
    {
        return FORMAT_OPENOFFICE_XML.equals(m_formatType);
    }

    private boolean isRestoreInvalidUnicodeChar()
    {
        return FORMAT_XML.equals(m_formatType)
                || FORMAT_IDML.equals(m_formatType);
    }

    /**
     * Reads the MessageData content and creates a String of GXML.
     * 
     * NOTE: This should be removed once the DiplomatAPI.merge() is changed to
     * take in a filename and return a filename.
     * 
     * @return String
     */
    private String readGxml() throws Exception
    {
        String s = MessageDataReader.readString(m_cxeMessage.getMessageData());
        m_cxeMessage.setDeleteMessageData(true);
        return s;
    }

    /**
     * Queries the rule file associated with the file profile out of the DB.
     * 
     * @exception LingAdapterException
     */
    private String queryRuleFile() throws LingAdapterException
    {
        Connection connection = null;
        PreparedStatement query = null;
        ResultSet results = null;
        String ruleFile = null;
        try
        {
            // Retrieve the (XML) Rule File from the Database.
            String SQL_SELECT_RULE = "SELECT RULE_TEXT FROM FILE_PROFILE, XML_RULE"
                    + " WHERE FILE_PROFILE.ID=?"
                    + " and XML_RULE.ID=FILE_PROFILE.XML_RULE_ID";
            connection = ConnectionPool.getConnection();
            query = connection.prepareStatement(SQL_SELECT_RULE);
            query.setString(1, m_fileProfile);
            results = query.executeQuery();

            if (results.next())
            {
                ruleFile = results.getString(1);
            }
            else
            {
                ruleFile = null;
            }

            return ruleFile;
        }
        catch (ConnectionPoolException cpe)
        {
            m_logger.error(
                    "Unable to connect to database retrieve XML rule file"
                            + " for FileProfileID " + m_fileProfile, cpe);
            throw new LingAdapterException("DbConnection", m_errorArgs, cpe);
        }
        catch (SQLException sqle)
        {
            m_logger.error(
                    "Unable to retrieve XML rule file for FileProfileID "
                            + m_fileProfile, sqle);
            throw new LingAdapterException("SqlException", m_errorArgs, sqle);
        }
        finally
        {
            ConnectionPool.silentClose(results);
            ConnectionPool.silentClose(query);
            ConnectionPool.silentReturnConnection(connection);
        }
    }

    /**
     * Ensure the target content has same return with that in source file.
     * 
     * @since 2011/11/28 (GBS-2134; version: 8.2.2)
     * @author Vincent.Yan
     */
    private String handleReturns(String s)
    {
        File cxeBaseDir = AmbFileStoragePathUtils.getCxeDocDir();
        File sourceFile = new File(cxeBaseDir, m_fileName);
        boolean isWindowsReturnMethod = FileUtil
                .isWindowsReturnMethod(sourceFile.getAbsolutePath());
        if (s.indexOf("\r\n") != -1)
        {
            // Merge result content is in Windows return method
            if (!isWindowsReturnMethod)
                s = StringUtil.replace(s, "\r\n", "\n");
        }
        else
        {
            // Merge result content is in Unix return method
            if (isWindowsReturnMethod)
                s = StringUtil.replace(s, "\n", "\r\n");
        }

        return s;
    }
}
