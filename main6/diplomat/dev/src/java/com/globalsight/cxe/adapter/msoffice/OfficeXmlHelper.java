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

package com.globalsight.cxe.adapter.msoffice;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.globalsight.cxe.adapter.IConverterHelper;
import com.globalsight.cxe.engine.eventflow.Category;
import com.globalsight.cxe.engine.eventflow.DiplomatAttribute;
import com.globalsight.cxe.engine.eventflow.EventFlow;
import com.globalsight.cxe.engine.util.FileCopier;
import com.globalsight.cxe.engine.util.FileUtils;
import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.cxe.entity.filterconfiguration.FilterConstants;
import com.globalsight.cxe.entity.filterconfiguration.FilterHelper;
import com.globalsight.cxe.entity.filterconfiguration.MSOffice2010Filter;
import com.globalsight.cxe.message.CxeMessage;
import com.globalsight.cxe.message.CxeMessageType;
import com.globalsight.cxe.message.FileMessageData;
import com.globalsight.cxe.message.MessageData;
import com.globalsight.cxe.message.MessageDataFactory;
import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.diplomat.util.database.ConnectionPoolException;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.ling.docproc.extractor.xml.XPathAPI;
import com.globalsight.util.FileUtil;

public class OfficeXmlHelper implements IConverterHelper
{
    private static final String CATEGORY_NAME = "OfficeXmlAdapter";
    private Logger m_logger;

    // Supported extensions for Office (XML)
    static private final String DOCX = ".docx";
    static private final String XLSX = ".xlsx";
    static private final String PPTX = ".pptx";

    // "INDD", "INX", "AI" -- goes in the command file
    private String m_conversionType = null;

    // The content specific conversion directory
    private String m_convDir = null;

    // The locale specific save directory under the conversion dir.
    private String m_saveDir = null;

    private String m_oriDisplayName = null;

    private boolean m_isImport = true;

    private boolean m_isHeaderTranslate = false;

    private boolean m_isMasterTranslate = false;

    private Properties m_properties = null;

    private long m_currentTimeMillis = 0;

    private static SystemConfiguration m_sc = SystemConfiguration.getInstance();

    private String m_eventFlowXml;

    private CxeMessage m_cxeMessage;

    private EventFlow m_eventFlow;

    private String m_hiddenSharedId = "";
    private String m_numStyleIds = "";
    private HashMap<String, String> m_hideCellMap = new HashMap<String, String>();

    private List<String> m_hideCellStyleIds = new ArrayList<String>();

    private static Hashtable<String, Integer> s_exportBatches = new Hashtable<String, Integer>();
    private static Object s_exportBatchesLocker = new Object();

    private static final String STYLE_TYPE_PARAGRAPH = "paragraph";
    private static final String STYLE_TYPE_CHARACTER = "character";

    // The types of Adobe files
    private int m_type = OFFICE_DOCX;

    public static final int OFFICE_DOCX = 0;

    public static final int OFFICE_XLSX = 1;

    public static final int OFFICE_PPTX = 2;

    public static final String OFFICE_XML = "office-xml";
    public static final String DOCX_CONTENT_XML = "word/document.xml";
    public static final String DOCX_COMMENT_XML = "word/comments.xml";
    public static final String DOCX_STYLE_XML = "word/styles.xml";
    public static final String DOCX_WORD_DIR = "word";
    public static final String XLSX_CONTENT_SHARE = "xl/sharedStrings.xml";
    public static final String XLSX_SHEET_NAME = "xl/workbook.xml";
    public static final String XLSX_STYLE_XML = "xl/styles.xml";
    public static final String XLSX_SHEETS_DIR = "xl/worksheets";
    public static final String PPTX_SLIDES_DIR = "ppt/slides";
    public static final String PPTX_SLIDE_MASTER_DIR = "ppt/slideMasters";
    public static final String PPTX_SLIDE_LAYOUT_DIR = "ppt/slideLayouts";
    public static final String PPTX_SLIDE_NOTES_DIR = "ppt/notesSlides";
    public static final String PPTX_SLIDE_NOTESMASTER_DIR = "ppt/notesMasters";
    public static final String PPTX_PRESENTATION_XML = "ppt/presentation.xml";
    public static final String PPTX_DIAGRAMS_DIR = "ppt/diagrams";

    public static final String DNAME_PRE_DOCX_COMMENT = "(comments) ";
    public static final String DNAME_PRE_XLSX_SHEET_NAME = "(sheet name) ";
    public static final String DNAME_PRE_XLSX_SHARED = "(shared strings) ";
    public static final String DNAME_PRE_PPTX_DIAGRAM = "(diagram ";
    public static final String DNAME_PRE_PPTX_NOTE = "(note";
    public static final String DNAME_PRE_PPTX_MASTER = "(master";
    public static final String DNAME_PRE_PPTX_LAYOUT = "(master layout";
    public static final String DNAME_PRE_PPTX_NOTEMASTER = "(note master";

    private static final String[] prefix_slide =
    { "slide" };
    private static final String[] prefix_slideNote =
    { "notesSlide" };
    private static final String[] prefix_sheet =
    { "sheet" };
    private static final String[] prefix_header =
    { "header", "footer", "footnotes" };
    private static final String[] prefix_slideMaster =
    { "slideMaster" };

    private static final String numbers = "0123456789";

    public OfficeXmlHelper(CxeMessage p_cxeMessage,
            Logger p_logger, Properties p_msOfficeProperties)
    {
        m_cxeMessage = p_cxeMessage;
        m_eventFlowXml = p_cxeMessage.getEventFlowXml();
        m_eventFlow = new EventFlow(m_eventFlowXml);
        m_properties = p_msOfficeProperties;
        m_logger = p_logger;
    }

    /**
     * Perform conversion
     * 
     * @return conversion result
     * @throws MsOfficeAdapterException
     */
    public CxeMessage[] performConversion() throws MsOfficeAdapterException
    {
        m_isImport = true;
        String filename = null;
        try
        {
            // 1 set basic parameters such as file type and conversion dir.
            setBasicParams();
            // 2 write file to conv dir
            filename = writeContentToConvInbox();
            // 3 ask open office converter to handle
            convert(filename);
            // 4 wait for Converter to convert
            String dir = getUnzipDir(filename);
            String[] xmlFiles = getLocalizeXmlFiles(dir);
            // 5 merge tags
            OfficeXmlTagHelper help = new OfficeXmlTagHelper(m_type);
            help.mergeTags(xmlFiles);

            MessageData[] messageData = readXmlOutput(filename, xmlFiles);
            CxeMessage[] result = new CxeMessage[messageData.length];
            String basename = FileUtils.getBaseName(filename);
            String dirname = getUnzipDir(basename);
            int dirLen = dir.length();

            // styles
            String unParaStyles = getStyleIds(dir, STYLE_TYPE_PARAGRAPH);
            String unCharStyles = getStyleIds(dir, STYLE_TYPE_CHARACTER);
            handleExcelStyleIds(dir);
            handleExcelHidden(dir);

            m_oriDisplayName = m_eventFlow.getDisplayName();
            for (int i = 0; i < result.length; i++)
            {
                // 5 modify eventflowxml
                String xmlfilename = xmlFiles[i];
                if (xmlfilename != null && xmlfilename.length() > dirLen)
                {
                    int index_dirname = xmlfilename.indexOf(dirname);
                    xmlfilename = xmlfilename.substring(index_dirname);
                }
                modifyEventFlowXmlForImport(xmlfilename, i + 1, result.length,
                        unParaStyles, unCharStyles, m_numStyleIds);
                // 7 return proper CxeMesseges
                CxeMessageType type = getPostConversionEvent();
                CxeMessage cxeMessage = new CxeMessage(type);
                cxeMessage.setParameters(m_cxeMessage.getParameters());
                cxeMessage.setMessageData(messageData[i]);

                String eventFlowXml = m_eventFlow.serializeToXml();
                cxeMessage.setEventFlowXml(eventFlowXml);

                result[i] = cxeMessage;
            }
            writeDebugFile(m_conversionType + "_" + getBaseFileName()
                    + "_sa.xml", m_eventFlow.serializeToXml());

            return result;
        }
        catch (MsOfficeAdapterException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw wrapImportException(e, m_eventFlow.getDisplayName());
        }
    }

    public CxeMessage performConversionBack() throws MsOfficeAdapterException
    {
        m_isImport = false;
        try
        {
            setBasicParams();
            String saveFileName = writeContentToXmlBox();
            HashMap params = m_cxeMessage.getParameters();

            String exportBatchId = m_eventFlow.getBatchInfo().getBatchId();
            String targetLocale = m_eventFlow.getTargetLocale();
            int docPageCount = m_eventFlow.getBatchInfo().getDocPageCount();
            String key = exportBatchId + getBaseFileName() + targetLocale;

            if (isExportFileComplete(key, docPageCount))
            {
                String oofilename = getCategory().getDiplomatAttribute(
                        "safeBaseFileName").getValue();
                String oofile = FileUtils.concatPath(m_saveDir, oofilename);
                modifyEventFlowXmlForExport();
                convert(oofile);
                MessageData fmd = readConvOutput(oofile);

                CxeMessage outputMsg = new CxeMessage(
                        CxeMessageType.getCxeMessageType(m_eventFlow
                                .getPostMergeEvent()));
                outputMsg.setMessageData(fmd);
                outputMsg.setParameters(params);

                String eventFlowXml = m_eventFlow.serializeToXml();
                writeDebugFile(m_conversionType + "_" + getBaseFileName()
                        + "_ea.xml", eventFlowXml);
                outputMsg.setEventFlowXml(eventFlowXml);

                return outputMsg;
            }
            else
            {
                // Since it was not the last page in the batch, don't
                // reconstruct the file.
                if (m_logger.isDebugEnabled())
                {
                    m_logger.debug("Skipping reconstruction for file: "
                            + saveFileName);
                }
                long lastMod = new File(saveFileName).lastModified();

                CxeMessageType type = CxeMessageType
                        .getCxeMessageType(CxeMessageType.CXE_EXPORT_STATUS_EVENT);
                CxeMessage outputMsg = new CxeMessage(type);
                outputMsg.setEventFlowXml(m_eventFlow.serializeToXml());
                params.put("Exception", null);
                params.put("ExportedTime", new Long(lastMod));
                outputMsg.setParameters(params);

                return outputMsg;
            }
        }
        catch (Exception e)
        {
            throw wrapExportException(e, m_eventFlow.getDisplayName());
        }
    }

    private void handleExcelStyleIds(String dir)
    {
        if (m_type != OFFICE_XLSX)
            return;

        String styleXml = FileUtils.concatPath(dir, XLSX_STYLE_XML);
        List<String> styleidList = new ArrayList<String>();

        try
        {
            // file not exists or not a file, return empty
            File styleFile = new File(styleXml);
            if (!styleFile.exists() || !styleFile.isFile())
            {
                return;
            }

            DOMParser stylesParser = new DOMParser();
            stylesParser.parse(styleXml);
            Document stylesDoc = stylesParser.getDocument();
            Node stylesNode = stylesDoc.getDocumentElement();

            String xpath = "//*[local-name()=\"cellXfs\"]/*[local-name()=\"xf\"]";

            NodeList affectedNodes = XPathAPI.selectNodeList(stylesNode, xpath);

            if (affectedNodes != null && affectedNodes.getLength() > 0)
            {
                int len = affectedNodes.getLength();
                for (int i = 0; i < len; i++)
                {
                    Element node = (Element) affectedNodes.item(i);
                    String numFmtId = node.getAttribute("numFmtId");
                    String applyNumberFormat = node
                            .getAttribute("applyNumberFormat");
                    if (!"0".equals(numFmtId) && "1".equals(applyNumberFormat))
                    {
                        styleidList.add(i + "");
                    }

                    NodeList protections = node
                            .getElementsByTagName("protection");
                    if (protections != null && protections.getLength() > 0)
                    {
                        Element protection = (Element) protections.item(0);
                        String hidden = protection.getAttribute("hidden");
                        if ("1".equals(hidden))
                        {
                            m_hideCellStyleIds.add(i + "");
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            // ignore, just log it
            m_logger.error("Exception occurs when read " + styleXml, e);
        }

        m_numStyleIds = styleidList.isEmpty() ? "" : MSOffice2010Filter
                .toString(styleidList);
    }

    public void logException(Exception e)
    {
        m_logger.error("Exception occurs when convert Office 2010", e);
    }

    /**
     * get style names for un-extract. (need to refine it to load XML once per
     * one import)
     * 
     * @param p_filepath
     * @param p_styleFamily
     * @return
     */
    private String getStyleIds(String dir, String p_styleType)
    {
        if (m_type != OFFICE_DOCX)
            return "";

        MSOffice2010Filter msf = getMainFilter();

        if (msf == null)
            return "";

        boolean isChar = false;
        String styles = "";
        if (STYLE_TYPE_CHARACTER.equals(p_styleType))
        {
            isChar = true;
            styles = msf.getUnextractableWordCharacterStyles();
        }
        else
        {
            styles = msf.getUnextractableWordParagraphStyles();
        }

        List<String> stylesList = MSOffice2010Filter.toList(styles);

        // add one more style for char
        if (isChar)
        {
            List<String> newStylesList = new ArrayList<String>();
            for (String styleName : stylesList)
            {
                newStylesList.add(styleName);
                newStylesList.add(styleName + " Char");
            }

            stylesList = newStylesList;
        }

        String stylesXml = dir + File.separator + DOCX_STYLE_XML;
        List<String> styleidList = new ArrayList<String>();

        try
        {
            // file not exists or not a file, return empty
            File styleFile = new File(stylesXml);
            if (!styleFile.exists() || !styleFile.isFile())
            {
                return "";
            }

            DOMParser stylesParser = new DOMParser();
            stylesParser.parse(stylesXml);
            Document stylesDoc = stylesParser.getDocument();
            Node stylesNode = stylesDoc.getDocumentElement();

            for (String styleName : stylesList)
            {
                String xpath = "//w:style[@w:type=\"" + p_styleType
                        + "\"]/w:name[@w:val=\"" + styleName + "\"]/..";

                NodeList affectedNodes = XPathAPI.selectNodeList(stylesNode,
                        xpath);

                if (affectedNodes == null || affectedNodes.getLength() == 0)
                {
                    xpath = "//w:style[@w:type=\"" + p_styleType
                            + "\"]/w:name[@w:val=\"" + styleName.toLowerCase()
                            + "\"]/..";
                    affectedNodes = XPathAPI.selectNodeList(stylesNode, xpath);
                }

                if (affectedNodes != null && affectedNodes.getLength() > 0)
                {
                    Element node = (Element) affectedNodes.item(0);
                    String v1 = node.getAttribute("w:styleId");
                    if (v1 != null && !"".equals(v1))
                    {
                        styleidList = addValueIfNotExists(styleidList, v1);
                    }
                }
            }
        }
        catch (Exception e)
        {
            // ignore, just log it
            m_logger.error("Exception occurs when read " + stylesXml, e);
        }

        return styleidList.isEmpty() ? "" : MSOffice2010Filter
                .toString(styleidList);
    }

    private List<String> addValueIfNotExists(List<String> styleNames, String val)
    {
        if (!styleNames.contains(val))
            styleNames.add(val);

        return styleNames;
    }

    private void setBasicParams() throws MsOfficeAdapterException
    {
        try
        {
            setType();
            setConversionDir();
            setSaveDirectory();
            m_currentTimeMillis = System.currentTimeMillis();

            MSOffice2010Filter f = getMainFilter();
            m_isHeaderTranslate = (m_type == OFFICE_DOCX && f != null) ? f
                    .isHeaderTranslate() : false;
            m_isMasterTranslate = (m_type == OFFICE_PPTX && f != null) ? f
                    .isMasterTranslate() : false;
        }
        catch (Exception e)
        {
            m_logger.error("Unable to set basic parameters. ", e);
            throw new MsOfficeAdapterException("Unexpected", null, e);
        }
    }

    /**
     * Sets the internal type (ODT, ODS, ODP, etc.)
     */
    private void setType()
    {
        String name = m_eventFlow.getDisplayName().toLowerCase();

        if (name.endsWith(DOCX))
        {
            m_type = OFFICE_DOCX;
            m_conversionType = "docx";
        }
        else if (name.endsWith(XLSX))
        {
            m_type = OFFICE_XLSX;
            m_conversionType = "xlsx";
        }
        else if (name.endsWith(PPTX))
        {
            m_type = OFFICE_PPTX;
            m_conversionType = "pptx";
        }
    }

    /**
     * Determines and sets the content specific conversion directory, for
     * example: E:\Builds\FileStorage\OpenOffice-Conv
     */
    private void setConversionDir() throws Exception
    {
        m_convDir = getConversionDir();
    }

    /**
     * Determines and sets the locale specific save directory during
     * import/export process.
     */
    private void setSaveDirectory() throws Exception
    {
        // First save the file out to a temp location.
        StringBuffer saveDir = new StringBuffer(m_convDir);

        saveDir.append(File.separator);
        saveDir.append(m_isImport ? m_eventFlow.getSourceLocale() : m_eventFlow
                .getTargetLocale());
        File saveDirF = new File(saveDir.toString());
        saveDirF.mkdirs();

        m_saveDir = saveDir.toString();
    }

    /**
     * Actually convert od to xml
     */
    private void convert(String p_filepath) throws Exception
    {
        String dirName = getUnzipDir(p_filepath);
        OfficeXmlConverter oxc = new OfficeXmlConverter();
        if (m_isImport)
        {
            oxc.convertOfficeToXml(p_filepath, dirName);
        }
        else
        {
            String filename = getCategory().getDiplomatAttribute(
                    "safeBaseFileName").getValue();
            oxc.convertXmlToOffice(filename, dirName);
        }
    }

    private String getUnzipDir(String p_filepath)
    {
        String dirName = p_filepath + "." + m_type;
        return dirName;
    }

    private MSOffice2010Filter getMainFilter()
    {
        try
        {
            String fpIdstr = m_eventFlow.getSource().getDataSourceId();
            if ("null".equals(fpIdstr))
            {
                // this is from "create aligner package" where the fp id is
                // "null" value.
                return null;
            }
            long fpId = Long.parseLong(fpIdstr);
            FileProfile fileProfile = null;
            fileProfile = ServerProxy.getFileProfilePersistenceManager()
                    .readFileProfile(fpId);
            long filterId = fileProfile.getFilterId();
            String filterTableName = fileProfile.getFilterTableName();

            if (filterId > 0
                    && FilterConstants.OFFICE2010_TABLENAME
                            .equals(filterTableName))
            {
                MSOffice2010Filter f = (MSOffice2010Filter) FilterHelper
                        .getFilter(filterTableName, filterId);
                return f;
            }
            else
            {
                return null;
            }
        }
        catch (Exception e)
        {
            m_logger.error("Can not get Filter", e);
            return null;
        }
    }

    private String getBaseFileName()
    {
        String dName = m_eventFlow.getDisplayName();
        dName = getOriginalFilename(dName);

        return FileUtils.getBaseName(dName);
    }

    private String getNewDisplayName(String fileNamePrefix, String fileNumber)
    {
        String newDisplayName = m_oriDisplayName;

        if (m_type == OFFICE_PPTX)
        {
            if (fileNamePrefix.startsWith("data"))
            {
                newDisplayName = DNAME_PRE_PPTX_DIAGRAM + fileNamePrefix + ") "
                        + m_oriDisplayName;
            }
            else if (fileNamePrefix.startsWith("notesSlide"))
            {
                newDisplayName = DNAME_PRE_PPTX_NOTE + fileNumber + ") "
                        + m_oriDisplayName;
            }
            else if (fileNamePrefix.startsWith("slideMaster"))
            {
                newDisplayName = DNAME_PRE_PPTX_MASTER + fileNumber + ") "
                        + m_oriDisplayName;
            }
            else if (fileNamePrefix.startsWith("slideLayout"))
            {
                newDisplayName = DNAME_PRE_PPTX_LAYOUT + fileNumber + ") "
                        + m_oriDisplayName;
            }
            else if (fileNamePrefix.startsWith("notesMaster"))
            {
                newDisplayName = DNAME_PRE_PPTX_NOTEMASTER + fileNumber + ") "
                        + m_oriDisplayName;
            }
            else if (fileNamePrefix.startsWith("slide"))
            {
                newDisplayName = "(slide" + fileNumber + ") "
                        + m_oriDisplayName;
            }
            else
            {
                newDisplayName = "(" + fileNamePrefix + ") " + m_oriDisplayName;
            }
        }

        if (m_type == OFFICE_XLSX)
        {
            if (fileNamePrefix.startsWith("sheet"))
            {
                newDisplayName = "(" + fileNamePrefix + ") " + m_oriDisplayName;
            }
            else if (fileNamePrefix.startsWith("sharedStrings"))
            {
                newDisplayName = DNAME_PRE_XLSX_SHARED + m_oriDisplayName;
            }
            else if (fileNamePrefix.startsWith("workbook"))
            {
                newDisplayName = DNAME_PRE_XLSX_SHEET_NAME + m_oriDisplayName;
            }
        }

        if (m_type == OFFICE_DOCX)
        {
            if (fileNamePrefix.startsWith("comments"))
            {
                newDisplayName = DNAME_PRE_DOCX_COMMENT + m_oriDisplayName;
            }
        }

        if (m_isHeaderTranslate)
        {
            if (fileNamePrefix.startsWith("header")
                    || fileNamePrefix.startsWith("footer")
                    || fileNamePrefix.startsWith("footnotes"))
            {
                newDisplayName = "(" + fileNamePrefix + ") " + m_oriDisplayName;
            }
        }

        return newDisplayName;
    }

    private String getPageNumber(String fileNamePrefix)
    {
        StringBuffer sb = new StringBuffer();

        for (int i = fileNamePrefix.length() - 1; i > -1; i--)
        {
            String c = fileNamePrefix.charAt(i) + "";
            if (numbers.contains(c))
            {
                sb.insert(0, c);
            }
            else
            {
                break;
            }
        }

        while (sb.length() < 3)
        {
            sb.insert(0, "0");
        }

        return sb.toString();
    }

    /**
     * Get the orignal file name from display name for Office Xml
     * 
     * @param p_displayName
     * @return
     */
    public static String getOriginalFilename(String p_displayName)
    {
        if (p_displayName == null || "".equals(p_displayName.trim()))
        {
            return p_displayName;
        }

        String result = null;

        if (p_displayName.startsWith(DNAME_PRE_DOCX_COMMENT))
        {
            result = p_displayName.substring(DNAME_PRE_DOCX_COMMENT.length());
        }
        else if (p_displayName.startsWith(DNAME_PRE_XLSX_SHARED))
        {
            result = p_displayName.substring(DNAME_PRE_XLSX_SHARED.length());
        }
        else if (p_displayName.startsWith(DNAME_PRE_XLSX_SHEET_NAME))
        {
            result = p_displayName
                    .substring(DNAME_PRE_XLSX_SHEET_NAME.length());
        }
        else if (p_displayName.startsWith("(slide")
                || p_displayName.startsWith("(sheet")
                || p_displayName.startsWith("(presentation")
                || p_displayName.startsWith("(slideMaster")
                || p_displayName.startsWith("(slideLayout")
                || p_displayName.startsWith("(notesSlide")
                || p_displayName.startsWith("(notesMaster")
                || p_displayName.startsWith("(footer")
                || p_displayName.startsWith("(header")
                || p_displayName.startsWith("(footnotes")
                || p_displayName.startsWith(DNAME_PRE_PPTX_DIAGRAM)
                || p_displayName.startsWith(DNAME_PRE_PPTX_NOTE)
                || p_displayName.startsWith(DNAME_PRE_PPTX_MASTER)
                || p_displayName.startsWith(DNAME_PRE_PPTX_LAYOUT)
                || p_displayName.startsWith(DNAME_PRE_PPTX_NOTEMASTER))
        {
            int index = p_displayName.indexOf(") ");
            if (index > -1)
            {
                result = p_displayName.substring(index + 2);
            }
        }

        result = result == null ? p_displayName : result.trim();

        return result;
    }

    public CxeMessage getCxeMessage()
    {
        return m_cxeMessage;
    }

    private CxeMessageType getPostConversionEvent()
    {
        return CxeMessageType
                .getCxeMessageType(CxeMessageType.XML_IMPORTED_EVENT);
    }

    public String getPostMergeEvent()
    {
        return CxeMessageType.getCxeMessageType(
                CxeMessageType.MSOFFICE_LOCALIZED_EVENT).getName();
    }

    private String getSafeBaseFileName()
    {
        return createSafeBaseFileName(getBaseFileName());
    }

    private String createSafeBaseFileName(String p_filename)
    {
        return m_currentTimeMillis + p_filename;
    }

    private Category getCategory()
    {
        return m_eventFlow.getCategory(CATEGORY_NAME);
    }

    private void modifyEventFlowXmlForExport()
    {
        m_eventFlow.setPostMergeEvent(getCategory().getPostMergeEvent());
    }

    protected void modifyEventFlowXmlForImport(String p_xmlFilename,
            int p_docPageNum, int p_docPageCount, String unParaStyles,
            String unCharStyles, String numStyleIds) throws Exception
    {
        if (unParaStyles == null || unParaStyles.length() == 0)
        {
            unParaStyles = ",";
        }
        if (unCharStyles == null || unCharStyles.length() == 0)
        {
            unCharStyles = ",";
        }
        if (numStyleIds == null || numStyleIds.length() == 0)
        {
            numStyleIds = ",";
        }

        p_xmlFilename = p_xmlFilename.replace("\\", File.separator);
        p_xmlFilename = p_xmlFilename.replace("/", File.separator);
        String fileNamePrefix = FileUtils.getPrefix(FileUtils
                .getBaseName(p_xmlFilename));

        String sheetHiddenCell = ",";
        if (m_hideCellMap.containsKey(fileNamePrefix))
        {
            sheetHiddenCell = m_hideCellMap.get(fileNamePrefix);
        }

        String hiddenSharedSI = ",";
        if (fileNamePrefix.startsWith("sharedStrings")
                && m_hiddenSharedId != null && m_hiddenSharedId.length() > 0)
        {
            hiddenSharedSI = m_hiddenSharedId;
        }

        // First get original Category
        Category oriC = getCategory();
        if (oriC != null)
        {
            Category newC = new Category(CATEGORY_NAME, new DiplomatAttribute[]
            { oriC.getDiplomatAttribute("postMergeEvent"),
                    oriC.getDiplomatAttribute("formatType"),
                    oriC.getDiplomatAttribute("safeBaseFileName"),
                    oriC.getDiplomatAttribute("originalFileSize"),
                    new DiplomatAttribute("unParaStyles", unParaStyles),
                    new DiplomatAttribute("unCharStyles", unCharStyles),
                    new DiplomatAttribute("numStyleIds", numStyleIds),
                    new DiplomatAttribute("hiddenSharedSI", hiddenSharedSI),
                    new DiplomatAttribute("sheetHiddenCell", sheetHiddenCell),
                    new DiplomatAttribute("relSafeName", p_xmlFilename) });

            m_eventFlow.removeCategory(oriC);
            m_eventFlow.addCategory(newC);
        }
        else
        {
            Category newC = new Category(CATEGORY_NAME, new DiplomatAttribute[]
            {
                    new DiplomatAttribute("postMergeEvent",
                            m_eventFlow.getPostMergeEvent()),
                    new DiplomatAttribute("formatType",
                            m_eventFlow.getSourceFormatType()),
                    new DiplomatAttribute("safeBaseFileName",
                            getSafeBaseFileName()),
                    new DiplomatAttribute("originalFileSize",
                            String.valueOf(m_cxeMessage.getMessageData()
                                    .getSize())),
                    new DiplomatAttribute("unParaStyles", unParaStyles),
                    new DiplomatAttribute("unCharStyles", unCharStyles),
                    new DiplomatAttribute("numStyleIds", numStyleIds),
                    new DiplomatAttribute("hiddenSharedSI", hiddenSharedSI),
                    new DiplomatAttribute("sheetHiddenCell", sheetHiddenCell),
                    new DiplomatAttribute("relSafeName", p_xmlFilename) });
            m_eventFlow.addCategory(newC);
        }
        // Then modify eventFlow
        m_eventFlow.setPostMergeEvent(getPostMergeEvent());
        // m_eventFlow.setSourceFormatType("xml");

        m_eventFlow.setDocPageCount(p_docPageCount);
        m_eventFlow.setDocPageNumber(p_docPageNum);

        // modify display name
        String number = getPageNumber(fileNamePrefix);
        String newDisplayName = getNewDisplayName(fileNamePrefix, number);
        m_eventFlow.setDisplayName(newDisplayName);
    }

    protected MessageData readConvOutput(String fileName)
            throws MsOfficeAdapterException
    {
        try
        {
            String oofile = FileUtils.getPrefix(fileName) + "."
                    + m_conversionType;
            FileMessageData fmd = MessageDataFactory.createFileMessageData();
            fmd.copyFrom(new File(oofile));
            return fmd;
        }
        catch (Exception e)
        {
            m_logger.error("Read adobe file failed", e);
            throw wrapExportException(e, e.getMessage());
        }
    }

    protected MessageData[] readXmlOutput(String p_filepath,
            String[] localizeXml) throws MsOfficeAdapterException
    {
        try
        {
            String dir = getUnzipDir(p_filepath);
            MessageData[] result = new MessageData[localizeXml.length];

            for (int i = 0; i < result.length; i++)
            {
                FileMessageData fmd = (FileMessageData) MessageDataFactory
                        .createFileMessageData(OFFICE_XML);
                fmd.copyFrom(new File(localizeXml[i]));

                result[i] = fmd;
            }

            String oofile = FileUtils.getPrefix(p_filepath) + "."
                    + m_conversionType;
            File dirFile = new File(dir);
            copyToTargetLocales(FileUtil.getAllFiles(dirFile));
            copyToTargetLocales(new String[]
            { oofile });

            return result;
        }
        catch (Exception e)
        {
            m_logger.error("Failed to read xml output:", e);
            throw wrapImportException(e, e.getMessage());
        }
    }

    private String[] getLocalizeXmlFiles(String dir)
    {
        List<String> list = new ArrayList<String>();

        if (m_type == OFFICE_DOCX)
        {
            String contentXml = FileUtils.concatPath(dir, DOCX_CONTENT_XML);
            String commentXml = FileUtils.concatPath(dir, DOCX_COMMENT_XML);
            list.add(contentXml);

            // get comments xml
            if (isFileExists(commentXml))
            {
                list.add(commentXml);
            }

            // get header / footer xml
            if (m_isHeaderTranslate)
            {
                File wordDir = new File(dir, DOCX_WORD_DIR);
                if (wordDir.isDirectory())
                {
                    File[] headers = listAcceptFiles(wordDir, prefix_header);

                    if (headers != null && headers.length >= 0)
                    {
                        for (int i = 0; i < headers.length; i++)
                        {
                            File f = headers[i];
                            String keyText = "</w:t>";
                            if (isFileContains(f, keyText, true))
                                list.add(f.getPath());
                        }
                    }
                }
            }
        } // docx
        else if (m_type == OFFICE_XLSX)
        {
            String sharedXml = FileUtils.concatPath(dir, XLSX_CONTENT_SHARE);
            list.add(sharedXml);

            // get sheet name
            String sheetnameXml = FileUtils.concatPath(dir, XLSX_SHEET_NAME);
            list.add(sheetnameXml);

            List<String> hiddenSheetIds = getExcelHiddenSheetId(sheetnameXml);

            File sheetsDir = new File(dir, XLSX_SHEETS_DIR);
            // get sheets
            if (sheetsDir.isDirectory())
            {
                File[] sheets = getSheetFiles(sheetsDir);

                if (sheets != null && sheets.length >= 0)
                {
                    // get each sheet and check if it is empty
                    for (int i = 0; i < sheets.length; i++)
                    {
                        File f = sheets[i];
                        String fbasename = FileUtils.getPrefix(FileUtils
                                .getBaseName(f.getPath()));
                        String fid = fbasename.substring(5);
                        boolean isEmpty = false;

                        try
                        {
                            String text = FileUtils.read(f, "UTF-8");
                            if (text.contains("<sheetData/>"))
                            {
                                isEmpty = true;
                            }
                        }
                        catch (Exception e)
                        {
                            // ignore
                            logException(e);
                        }

                        if (!hiddenSheetIds.contains(fid) && !isEmpty)
                            list.add(f.getPath());
                    }
                }
            }
        } // xlsx
        else if (m_type == OFFICE_PPTX)
        {
            // check if section name exists
            String presentationXml = FileUtils.concatPath(dir,
                    PPTX_PRESENTATION_XML);
            try
            {
                File presentationFile = new File(presentationXml);

                if (presentationFile.exists())
                {
                    if (isFileContains(presentationFile, "p14:section name=",
                            false))
                    {
                        list.add(presentationXml);
                    }
                }
            }
            catch (Exception e)
            {
                // ignore
                logException(e);
            }

            // check if there is diagram data
            File diagramDir = new File(dir, PPTX_DIAGRAMS_DIR);
            if (diagramDir.isDirectory())
            {
                final String[] acceptNames =
                { "data" };
                File[] datafiles = listAcceptFiles(diagramDir, acceptNames);

                if (datafiles != null && datafiles.length >= 0)
                {
                    for (int i = 0; i < datafiles.length; i++)
                    {
                        File f = datafiles[i];
                        if (isFileContains(f, "</a:t>", false))
                        {
                            list.add(f.getPath());
                        }
                    }
                }
            }

            // get slides
            File slidesDir = new File(dir, PPTX_SLIDES_DIR);
            if (slidesDir.isDirectory())
            {
                File[] slides = listAcceptFiles(slidesDir, prefix_slide);

                if (slides != null && slides.length >= 0)
                {
                    for (int i = 0; i < slides.length; i++)
                    {
                        File f = slides[i];
                        list.add(f.getPath());
                    }
                }
            }

            // get notes
            File notesDir = new File(dir, PPTX_SLIDE_NOTES_DIR);
            if (notesDir.isDirectory())
            {
                File[] notes = listAcceptFiles(notesDir, prefix_slideNote);

                if (notes != null && notes.length >= 0)
                {
                    for (int i = 0; i < notes.length; i++)
                    {
                        File f = notes[i];
                        if (isFileContains(f, "</a:r>", false))
                        {
                            list.add(f.getPath());
                        }
                    }
                }
            }

            // get master pages if needed
            if (m_isMasterTranslate)
            {
                File slideMasterDir = new File(dir, PPTX_SLIDE_MASTER_DIR);
                if (slideMasterDir.isDirectory())
                {
                    File[] slideMasters = listAcceptFiles(slideMasterDir,
                            prefix_slideMaster);

                    if (slideMasters != null && slideMasters.length >= 0)
                    {
                        for (int i = 0; i < slideMasters.length; i++)
                        {
                            File f = slideMasters[i];
                            if (isFileContains(f, "</a:r>", false))
                            {
                                list.add(f.getPath());
                            }
                        }
                    }
                }
            }
        } // pptx

        if (list.isEmpty())
        {
            return new String[0];
        }
        else
        {
            String[] result = new String[list.size()];
            result = list.toArray(result);

            return result;
        }
    }

    private boolean isFileExists(String xmlFile)
    {
        try
        {
            File file = new File(xmlFile);
            if (file.exists())
            {
                return true;
            }
        }
        catch (Exception e)
        {
            logException(e);
        }

        return false;
    }

    private boolean isFileContains(File file, String keyText,
            boolean resultOfException)
    {
        boolean contains = false;
        try
        {
            String text = FileUtils.read(file, "UTF-8");
            contains = text.contains(keyText);
            if (!contains)
            {
                Pattern p = Pattern
                        .compile("<wp:docPr [^>]* descr=\"([^\"]*)\"[^>]*>");
                Matcher m = p.matcher(text);
                if (m.find())
                    contains = true;
            }
        }
        catch (Exception e)
        {
            logException(e);
            contains = resultOfException;
        }

        return contains;
    }

    private File[] listAcceptFiles(File filesDir, final String[] acceptNames)
    {
        File[] files = filesDir.listFiles(new FileFilter()
        {
            public boolean accept(File pathname)
            {
                if (!pathname.isFile())
                {
                    return false;
                }

                String basename = FileUtils.getBaseName(pathname.getPath());
                for (String name : acceptNames)
                {
                    if (basename.startsWith(name))
                    {
                        return true;
                    }
                }

                return false;
            }
        });

        return files;
    }

    private File[] getSheetFiles(File sheetsDir)
    {
        File[] sheets = listAcceptFiles(sheetsDir, prefix_sheet);
        return sheets;
    }

    // get shared string id (t="s") in hidden sheet
    private void handleExcelHidden(String dir)
    {
        if (m_type != OFFICE_XLSX)
        {
            return;
        }

        String sheetnameXml = FileUtils.concatPath(dir, XLSX_SHEET_NAME);
        String sheetsDir = FileUtils.concatPath(dir, XLSX_SHEETS_DIR);

        List<String> hiddenSharedId = new ArrayList<String>();
        List<String> visibleSharedId = new ArrayList<String>();

        // get it in hidden sheets
        List<String> hiddenSheetIds = getExcelHiddenSheetId(sheetnameXml);
        if (hiddenSheetIds != null && hiddenSheetIds.size() > 0)
        {
            for (String id : hiddenSheetIds)
            {
                String sheet = FileUtils.concatPath(sheetsDir, "sheet" + id
                        + ".xml");
                List<String> hiddenSharedIdHere = getSharedIdInSheet(sheet);

                for (String sharedId : hiddenSharedIdHere)
                {
                    if (!hiddenSharedId.contains(sharedId))
                    {
                        hiddenSharedId.add(sharedId);
                    }
                }
            }
        }

        // get it in visible sheets, hidden row, column, cell
        File[] sheets = getSheetFiles(new File(sheetsDir));

        if (sheets != null && sheets.length >= 0)
        {
            // get each sheet and check if it is empty
            for (int i = 0; i < sheets.length; i++)
            {
                File f = sheets[i];
                String fbasename = FileUtils.getBaseName(f.getPath());
                String fprefix = FileUtils.getPrefix(fbasename);
                String fid = fprefix.substring(5);
                List<String> hiddenCells = new ArrayList<String>();

                if (hiddenSheetIds.contains(fid))
                {
                    continue;
                }

                // not hide sheet
                try
                {
                    // check first to avoid unnecessary XML parse
                    String text = FileUtils.read(f, "UTF-8");
                    if (!text.contains(" hidden=\"1\" ")
                            && (m_hideCellStyleIds == null || m_hideCellStyleIds
                                    .isEmpty()))
                    {
                        continue;
                    }

                    if (!text.contains("</row>"))
                    {
                        continue;
                    }

                    // get hidden cell and its shared id
                    DOMParser parser = new DOMParser();
                    parser.parse(f.getPath());
                    Document doc = parser.getDocument();
                    Node docElem = doc.getDocumentElement();

                    // 1 hidden cell
                    if (m_hideCellStyleIds != null
                            && !m_hideCellStyleIds.isEmpty())
                    {
                        for (String id : m_hideCellStyleIds)
                        {
                            String xpath = "//*[local-name()=\"c\"][@s=\"" + id
                                    + "\"]";
                            NodeList affectedNodes = XPathAPI.selectNodeList(
                                    docElem, xpath);

                            handleHiddenCellElement(affectedNodes,
                                    hiddenSharedId, hiddenCells);
                        }
                    }

                    // 2 hidden row
                    List<String> cells = new ArrayList<String>();
                    String xpath = "//*[local-name()=\"row\"][@hidden=\"1\"]";
                    NodeList affectedNodes = XPathAPI.selectNodeList(docElem,
                            xpath);

                    if (affectedNodes != null && affectedNodes.getLength() > 0)
                    {
                        int nodesLen = affectedNodes.getLength();
                        for (int j = 0; j < nodesLen; j++)
                        {
                            Element ce = (Element) affectedNodes.item(j);
                            String rowId = ce.getAttribute("r");
                            String spans = ce.getAttribute("spans");

                            addAllCells(cells, Integer.parseInt(rowId), spans);
                        }
                    }

                    // 3 hidden column
                    xpath = "//*[local-name()=\"col\"][@hidden=\"1\"]";
                    affectedNodes = XPathAPI.selectNodeList(docElem, xpath);

                    if (affectedNodes != null && affectedNodes.getLength() > 0)
                    {
                        String lastRow = "//*[local-name()=\"row\"][last()]";
                        NodeList lastRowNodes = XPathAPI.selectNodeList(
                                docElem, lastRow);
                        String lastRowIndexStr = ((Element) lastRowNodes
                                .item(0)).getAttribute("r");
                        int lastRowIndex = Integer.parseInt(lastRowIndexStr);

                        int nodesLen = affectedNodes.getLength();
                        for (int j = 0; j < nodesLen; j++)
                        {
                            Element ce = (Element) affectedNodes.item(j);
                            String min = ce.getAttribute("min");
                            String max = ce.getAttribute("max");

                            for (int k = 1; k <= lastRowIndex; k++)
                            {
                                addAllCells(cells, k, min + ":" + max);
                            }
                        }
                    }

                    if (cells.size() != 0)
                    {
                        for (String id : cells)
                        {
                            String cellpath = "//*[local-name()=\"c\"][@r=\""
                                    + id + "\"]";
                            NodeList cellaffectedNodes = XPathAPI
                                    .selectNodeList(docElem, cellpath);

                            handleHiddenCellElement(cellaffectedNodes,
                                    hiddenSharedId, hiddenCells);
                        }
                    }

                    // 4 remove shared string which is contained in both hide
                    // and visible cell
                    String tsCellXPath = "//*[local-name()=\"c\"][@t=\"s\"]";
                    NodeList tsCells = XPathAPI.selectNodeList(docElem,
                            tsCellXPath);
                    if (tsCells != null && tsCells.getLength() > 0)
                    {
                        int tsCellLen = tsCells.getLength();
                        for (int j = 0; j < tsCellLen; j++)
                        {
                            Element ce = (Element) tsCells.item(j);
                            String s = ce.getAttribute("s");
                            String r = ce.getAttribute("r");

                            // hide cell
                            if (r != null && hiddenCells.contains(r))
                            {
                                continue;
                            }

                            // hide style
                            if (s != null && m_hideCellStyleIds.contains(s))
                            {
                                continue;
                            }

                            // not hide
                            String vv = getExcelVText(ce);
                            addValueIfNotExists(visibleSharedId, vv);
                        }
                    }
                }
                catch (Exception e)
                {
                    // ignore
                    logException(e);
                }

                // each sheet
                if (hiddenCells.size() != 0)
                {
                    m_hideCellMap.put(fprefix,
                            MSOffice2010Filter.toString(hiddenCells));
                }
            }
        }

        removeSameValueInList1(hiddenSharedId, visibleSharedId);

        m_hiddenSharedId = MSOffice2010Filter.toString(hiddenSharedId);
    }

    private void removeSameValueInList1(List<String> list1, List<String> list2)
    {
        List<String> saveValues = new ArrayList<String>();
        for (String vv : list1)
        {
            if (list2.contains(vv))
            {
                saveValues.add(vv);
            }
        }

        for (String vv : saveValues)
        {
            list1.remove(vv);
        }
    }

    private void addAllCells(List<String> cells, int row, String spans)
    {
        String[] span = spans.split(":");
        if (span != null && span.length == 2)
        {
            int s1 = Integer.parseInt(span[0]);
            int s2 = Integer.parseInt(span[1]);

            for (int i = s1; i <= s2; i++)
            {
                String column = getExcelColumnChar(i);
                addValueIfNotExists(cells, column + row);
            }
        }
    }

    private void handleHiddenCellElement(NodeList affectedNodes,
            List<String> hiddenSharedId, List<String> hiddenCells)
    {
        if (affectedNodes != null && affectedNodes.getLength() > 0)
        {
            for (int j = 0; j < affectedNodes.getLength(); j++)
            {
                Element ce = (Element) affectedNodes.item(j);
                String rr = ce.getAttribute("r");
                hiddenCells = addValueIfNotExists(hiddenCells, rr);
                String ss = ce.getAttribute("t");
                if ("s".equals(ss))
                {
                    String vnid = getExcelVText(ce);
                    hiddenSharedId = addValueIfNotExists(hiddenSharedId, vnid);
                }
            }
        }
    }

    private String getExcelVText(Element ce)
    {
        Node vn = ce.getElementsByTagName("v").item(0);
        String vnid = vn.getFirstChild().getNodeValue();
        return vnid;
    }

    private String getExcelColumnChar(int num)
    {
        int A = 65;
        String sCol = "";
        int iRemain = 0;

        if (num > 701)
        {
            return "";
        }

        if (num <= 26)
        {
            if (num == 0)
            {
                sCol = "" + (char) ((A + 26) - 1);
            }
            else
            {
                sCol = "" + (char) ((A + num) - 1);
            }
        }
        else
        {
            iRemain = (num / 26) - 1;
            if ((num % 26) == 0)
            {
                sCol = getExcelColumnChar(iRemain) + getExcelColumnChar(0);
            }
            else
            {
                sCol = (char) (A + iRemain) + getExcelColumnChar(num % 26);
            }
        }

        return sCol;
    }

    private List<String> getSharedIdInSheet(String sheet)
    {
        List<String> result = new ArrayList<String>();
        try
        {
            String xpath = "//*[local-name()=\"c\"][@t=\"s\"]/*[local-name()=\"v\"]";

            NodeList affectedNodes = getAffectedNodes(sheet, xpath);

            if (affectedNodes != null && affectedNodes.getLength() > 0)
            {
                int len = affectedNodes.getLength();
                for (int i = 0; i < len; i++)
                {
                    Element nd = (Element) affectedNodes.item(i);
                    String id = nd.getFirstChild().getNodeValue();
                    result.add(id);
                }
            }
        }
        catch (Exception e)
        {
            // ignore
            logException(e);
        }

        return result;
    }

    private NodeList getAffectedNodes(String xmlfile, String xpath)
            throws SAXException, IOException
    {
        DOMParser parser = new DOMParser();
        parser.parse(xmlfile);
        Document doc = parser.getDocument();
        Node node = doc.getDocumentElement();

        NodeList affectedNodes = XPathAPI.selectNodeList(node, xpath);
        return affectedNodes;
    }

    private List<String> getExcelHiddenSheetId(String sheetnameXml)
    {
        List<String> result = new ArrayList<String>();
        try
        {
            String xpath = "//*[local-name()=\"sheet\"][@state=\"hidden\"]";
            NodeList affectedNodes = getAffectedNodes(sheetnameXml, xpath);

            if (affectedNodes != null && affectedNodes.getLength() > 0)
            {
                int len = affectedNodes.getLength();
                for (int i = 0; i < len; i++)
                {
                    Element nd = (Element) affectedNodes.item(i);
                    String id = nd.getAttribute("sheetId");
                    result.add(id);
                }
            }
        }
        catch (Exception e)
        {
            // ignore
            logException(e);
        }

        return result;
    }

    private String writeContentToConvInbox() throws MsOfficeAdapterException
    {
        try
        {
            String fileName = FileUtils.concatPath(m_saveDir,
                    getSafeBaseFileName());
            if (m_logger.isInfoEnabled())
            {
                m_logger.info("Converting: " + m_eventFlow.getDisplayName()
                        + ", size: " + m_cxeMessage.getMessageData().getSize()
                        + ", tmp file: " + fileName);
            }

            FileMessageData fmd = (FileMessageData) m_cxeMessage
                    .getMessageData();
            fmd.copyTo(new File(fileName));

            return fileName;
        }
        catch (Exception e)
        {
            m_logger.error("Failed to write adobe to inbox. ", e);
            String[] errorArgs =
            { m_eventFlow.getDisplayName() };
            throw new MsOfficeAdapterException("Import", errorArgs, e);
        }
    }

    private String writeContentToXmlBox() throws IOException
    {
        String saveFileName = FileUtils.concatPath(m_saveDir, getCategory()
                .getDiplomatAttribute("relSafeName").getValue());
        File saveFile = new File(saveFileName);

        m_cxeMessage.getMessageData().copyTo(saveFile);

        return saveFileName;
    }

    private static MsOfficeAdapterException wrapExportException(Exception e,
            String arg)
    {
        return new MsOfficeAdapterException("Export", new String[]
        { arg }, e);
    }

    private static MsOfficeAdapterException wrapImportException(Exception e,
            String arg)
    {
        return new MsOfficeAdapterException("Import", new String[]
        { arg }, e);
    }

    private void writeDebugFile(String fileName, String content)
    {
        String debugFileDirectory = m_properties
                .getProperty("DebugFileDirectory");
        if (debugFileDirectory != null)
        {
            try
            {
                FileUtils.write(new File(debugFileDirectory, fileName),
                        content, "UTF-8");
            }
            catch (Exception e)
            {
                if (m_logger.isEnabledFor(Priority.WARN))
                {
                    m_logger.warn("Fail to write content to file: " + fileName,
                            e);
                }
            }
        }
    }

    private List<File> copyToTargetLocales(String[] files)
    {
        List<File> fileList = new ArrayList<File>();
        for (int i = 0; i < files.length; i++)
        {
            File f = new File(files[i]);
            fileList.add(f);
        }

        return copyToTargetLocales(fileList);
    }

    private List<File> copyToTargetLocales(List<File> files)
    {
        List<File> fileList = new ArrayList<File>();
        for (int i = 0; i < files.size(); i++)
        {
            File expectedFile = files.get(i);
            if (!expectedFile.exists())
            {
                throw new RuntimeException("conversion failed: " + expectedFile);
            }
            fileList.add(expectedFile);
            doCopyToTargetLocales(expectedFile);
        }
        return fileList;
    }

    private void doCopyToTargetLocales(File expectedFile)
    {
        String srcLocale = m_eventFlow.getSourceLocale();
        String l10nProfileId = m_eventFlow.getBatchInfo().getL10nProfileId();
        ArrayList<String> targetLocales = findTargetLocales(l10nProfileId);
        for (int i = 0; i < targetLocales.size(); i++)
        {
            String locale = targetLocales.get(i);
            String path = expectedFile.getParent();
            path = path.replace('\\', '/');
            StringBuffer targetDir = new StringBuffer(path);
            int srcIndex = targetDir.lastIndexOf('/' + srcLocale + '/') + 1;
            if (srcIndex == 0)
            {
                // indicate the expectedFile looks like "xxxx/en_US/xxx.docx
                srcIndex = targetDir.lastIndexOf('/' + srcLocale) + 1;
            }
            targetDir.replace(srcIndex, srcIndex + srcLocale.length(), locale);

            File targetDirF = new File(targetDir.toString());
            targetDirF.mkdirs();
            FileCopier.copy(expectedFile, targetDir.toString());
        }
    }

    private ArrayList<String> findTargetLocales(String p_l10nProfileId)
    {
        ArrayList<String> targetLocales = new ArrayList<String>();

        if (p_l10nProfileId == null || p_l10nProfileId.equals("null"))
        {
            // May be null for aligner import.
            return targetLocales;
        }

        Connection connection = null;
        PreparedStatement query = null;
        StringBuffer sql = new StringBuffer(
                "select loc.iso_lang_code, loc.iso_country_code ");
        sql.append("from l10n_profile_wftemplate_info lpwf, ");
        sql.append("  workflow_template wft, locale loc ");
        sql.append("where lpwf.l10n_profile_id=? ");
        sql.append("and lpwf.wf_template_id=wft.id ");
        sql.append("and loc.id=wft.target_locale_id");
        try
        {
            connection = ConnectionPool.getConnection();
            query = connection.prepareStatement(sql.toString());
            query.setString(1, p_l10nProfileId);
            ResultSet results = query.executeQuery();
            while (results.next())
            {
                String lang = results.getString(1);
                String country = results.getString(2);
                String locale = lang + "_" + country;
                if (!targetLocales.contains(locale))
                {
                    targetLocales.add(locale);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException("findTargetLocales error " + sql);
        }
        finally
        {
            try
            {
                query.close();
            }
            catch (Throwable e)
            {
            }
            try
            {
                ConnectionPool.returnConnection(connection);
            }
            catch (ConnectionPoolException cpe)
            {
            }
        }
        return targetLocales;
    }

    private static boolean isExportFileComplete(String p_filekey,
            int p_pageCount)
    {
        // Default is to write out the file.
        boolean result = true;
        int curPageCnt = -1;

        synchronized (s_exportBatchesLocker)
        {
            Integer oldPageCount = s_exportBatches.get(p_filekey);
            if (oldPageCount == null)
            {
                // First page of this exportBatch.
                curPageCnt = p_pageCount - 1;
                if (curPageCnt == 0)
                {
                    // The batch is complete, no need to put anything
                    // in the hashtable.
                    result = true;
                }
                else
                {
                    result = false;
                    s_exportBatches.put(p_filekey, new Integer(curPageCnt));
                }
            }
            else
            {
                curPageCnt = oldPageCount.intValue() - 1;
                if (curPageCnt == 0)
                {
                    // The batch is complete, remove the value from the
                    // hashtable.
                    result = true;
                    s_exportBatches.remove(p_filekey);
                }
                else
                {
                    result = false;
                    s_exportBatches.put(p_filekey, new Integer(curPageCnt));
                }
            }
        }

        return result;
    }

    public static String getConversionDir() throws Exception
    {
        StringBuffer convDir = new StringBuffer();
        convDir.append(m_sc.getStringParameter(
                SystemConfigParamNames.FILE_STORAGE_DIR,
                CompanyWrapper.SUPER_COMPANY_ID));
        convDir.append(File.separator);
        convDir.append("OfficeXml-Conv");

        return convDir.toString();
    }
}