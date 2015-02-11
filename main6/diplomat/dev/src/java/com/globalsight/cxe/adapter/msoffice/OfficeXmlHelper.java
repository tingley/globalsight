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
import java.io.FileInputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.globalsight.cxe.adapter.AdapterResult;
import com.globalsight.cxe.adapter.IConverterHelper2;
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
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.ExportUtil;
import com.globalsight.ling.docproc.extractor.xml.XPathAPI;
import com.globalsight.util.FileUtil;

public class OfficeXmlHelper implements IConverterHelper2
{
    private static final String CATEGORY_NAME = "OfficeXmlAdapter";
    private static Object m_locker = new Object();
    private Logger m_logger;
    private static int gcCounter = 0;

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
    private boolean m_isFootendNotesTranslate = false;
    private boolean m_isURLTranslate = false;
    private boolean m_isMasterTranslate = false;
    private boolean m_isNotesTranslate = false;
    private boolean m_isSlideLayoutTranslate = false;
    private boolean m_isNotesMasterTranslate = false;
    private boolean m_isHandoutMasterTranslate = false;
    private boolean m_isExcelTabNamesTranslate = false;
    private boolean m_isToolTipsTranslate = false;
    private boolean m_isHiddenTextTranslate = false;
    private boolean isTableOfContentTranslate = false;

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
    private Set<String> hideSharedStrings = new HashSet<String>();
    private Set<String> m_unextractableExcelCellStyles = new HashSet<String>();

    private static Hashtable<String, Integer> s_exportBatches = new Hashtable<String, Integer>();
    private static Object s_exportBatchesLocker = new Object();

    private static final String STYLE_TYPE_PARAGRAPH = "paragraph";
    private static final String STYLE_TYPE_CHARACTER = "character";
    private static final String STYLE_CATEGORY_PARAGRAPH_UN = "paragraph_un";
    private static final String STYLE_CATEGORY_CHARACTER_UN = "character_un";
    private static final String STYLE_CATEGORY_CHARACTER_INTERNAL = "character_internal";

    // The types of Adobe files
    private int m_type = OFFICE_DOCX;
    public static final int OFFICE_DOCX = 0;
    public static final int OFFICE_XLSX = 1;
    public static final int OFFICE_PPTX = 2;

    public static final String OFFICE_XML = "office-xml";
    public static final String CONVERSION_DIR_NAME = "OfficeXml-Conv";
    public static final String DOCX_CONTENT_XML = "word/document.xml";
    public static final String DOCX_RELS_XML = "word/_rels/document.xml.rels";
    public static final String DOCX_COMMENT_XML = "word/comments.xml";
    public static final String DOCX_STYLE_XML = "word/styles.xml";
    public static final String DOCX_WORD_DIR = "word";
    public static final String DOCX_DIAGRAMS_DIR = "word/diagrams";
    public static final String DOCX_CHAR_DIR = "word/charts";
    public static final String XLSX_CONTENT_SHARE = "xl/sharedStrings.xml";
    public static final String XLSX_SHEET_NAME = "xl/workbook.xml";
    public static final String XLSX_STYLE_XML = "xl/styles.xml";
    public static final String XLSX_SHEETS_DIR = "xl/worksheets";
    public static final String PPTX_SLIDES_DIR = "ppt/slides";
    public static final String PPTX_SLIDE_MASTER_DIR = "ppt/slideMasters";
    public static final String PPTX_SLIDE_LAYOUT_DIR = "ppt/slideLayouts";
    public static final String PPTX_SLIDE_NOTES_DIR = "ppt/notesSlides";
    public static final String PPTX_SLIDE_NOTESMASTER_DIR = "ppt/notesMasters";
    public static final String PPTX_SLIDE_HANDOUTMASTER_DIR = "ppt/handoutMasters";
    public static final String PPTX_PRESENTATION_XML = "ppt/presentation.xml";
    public static final String PPTX_DIAGRAMS_DIR = "ppt/diagrams";
    public static final String PPTX_CHART_DIR = "ppt/charts";

    public static final String DNAME_PRE_DOCX_COMMENT = "(comments) ";
    public static final String DNAME_PRE_XLSX_SHEET_NAME = "(sheet name) ";
    public static final String DNAME_PRE_XLSX_SHARED = "(shared strings) ";
    public static final String DNAME_PRE_PPTX_DIAGRAM = "(diagram ";
    public static final String DNAME_PRE_PPTX_NOTE = "(note";
    public static final String DNAME_PRE_PPTX_MASTER = "(slide master";
    public static final String DNAME_PRE_PPTX_LAYOUT = "(slide layout";
    public static final String DNAME_PRE_PPTX_NOTEMASTER = "(note master";
    public static final String DNAME_PRE_PPTX_HANDOUTMASTER = "(handout master";

    private static final String[] prefix_slide =
    { "slide" };
    private static final String[] prefix_slideNote =
    { "notesSlide" };
    private static final String[] prefix_sheet =
    { "sheet" };
    private static final String[] prefix_header =
    { "header", "footer" };
    private static final String[] prefix_footendnotes =
    { "endnotes", "footnotes" };
    private static final String[] prefix_slideMaster =
    { "slideMaster" };
    private static final String[] prefix_slideLayout =
    { "slideLayout" };
    private static final String[] prefix_notesMaster =
    { "notesMaster" };
    private static final String[] prefix_handoutMaster =
    { "handoutMaster" };

    private static final String numbers = "0123456789";

    // for GBS-2554
    private static final String W_VANISH = "<w:vanish/>";
    private static final String W_VANISH0 = "<w:vanish w:val=\"0\"/>";
    private static final String RE_WP = "(<w:p[^>]*>)([\\d\\D]*?)(</w:p>)";
    private static final String RE_STYLE = "<w:style[^>]*w:styleId=\"{0}\"[^>]*>([\\d\\D]*?)</w:style>";
    private static final String RE_RPR = "(<w:rPr>)([\\d\\D]*?)(</w:rPr>)";
    private static final String RE_PPR = "(<w:pPr>)([\\d\\D]*?)(</w:pPr>)";
    private static final String RE_WR = "(<w:r[^>]*>)([\\d\\D]*?)(</w:r>)";
    private static final String RE_RSTYLE = "<w:rStyle[^>]*w:val=\"([^\"]*)\"[^>]*/>";
    private static final String RE_PSTYLE = "<w:pStyle[^>]*w:val=\"([^\"]*)\"[^>]*/>";

    // for xlsx repeated strings shared in sharedStrings.xml
    private static final String RE_SI = "<si>([\\d\\D]*?)</si>";
    private static final String RE_SI_T = "<t[^>]*>([\\d\\D]*?)</t>";
    private static final String RE_C = "(<c[^>]*t=\"s\"[^>]*>)([\\d\\D]*?)(</c>)";
    private static final String RE_V = "<v>([\\d]*?)</v>";

    /**
     * Init OfficeXmlHelper for office 2010 importing
     */
    public OfficeXmlHelper(CxeMessage p_cxeMessage, Logger p_logger,
            Properties p_msOfficeProperties)
    {
        m_cxeMessage = p_cxeMessage;
        m_eventFlowXml = p_cxeMessage.getEventFlowXml();
        m_eventFlow = new EventFlow(m_eventFlowXml);
        m_properties = p_msOfficeProperties;
        m_logger = p_logger;
    }

    private void copyAllFiles(String key, String name)
    {
        String oofile = m_convDir + "/" + m_eventFlow.getSourceLocale() + "/"
                + name;
        String dir = oofile + "." + m_type;
        File dirFile = new File(dir);
        copyToTargetLocales(FileUtil.getAllFiles(dirFile));
        copyToTargetLocales(new String[]
        { oofile });
    }

    /**
     * Perform conversion
     * 
     * @return conversion result
     * @throws MsOfficeAdapterException
     */
    public AdapterResult[] performConversion() throws MsOfficeAdapterException
    {
        gcCounter++;
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
            if (gcCounter > 100)
            {
                // call GC to free memory for large file
                System.gc();
                gcCounter = 0;
            }

            preHandleHiddenTextInDocx(dir);

            preHandleTextInSharedStringsXml(dir);

            MessageData[] messageData = readXmlOutput(filename, xmlFiles);
            CxeMessage[] msgs = new CxeMessage[messageData.length];
            String basename = FileUtils.getBaseName(filename);
            String dirname = getUnzipDir(basename);
            int dirLen = dir.length();

            // styles
            String unParaStyles = getStyleIds(dir, STYLE_CATEGORY_PARAGRAPH_UN,
                    STYLE_TYPE_PARAGRAPH);
            String unCharStyles = getStyleIds(dir, STYLE_CATEGORY_CHARACTER_UN,
                    STYLE_TYPE_CHARACTER);
            String internalCharStyles = getStyleIds(dir,
                    STYLE_CATEGORY_CHARACTER_INTERNAL, STYLE_TYPE_CHARACTER);

            handleExcelStyleIds(dir);
            handleExcelHidden(dir);

            List<CxeMessage> slides = new ArrayList<CxeMessage>();
            List<CxeMessage> notes = new ArrayList<CxeMessage>();
            List<CxeMessage> others = new ArrayList<CxeMessage>();

            m_oriDisplayName = m_eventFlow.getDisplayName();
            for (int i = 0; i < msgs.length; i++)
            {
                // 5 modify eventflowxml
                String xmlfilename = xmlFiles[i];
                if (xmlfilename != null && xmlfilename.length() > dirLen)
                {
                    int index_dirname = xmlfilename.indexOf(dirname);
                    xmlfilename = xmlfilename.substring(index_dirname);
                }
                modifyEventFlowXmlForImport(xmlfilename, i + 1, msgs.length,
                        unParaStyles, unCharStyles, internalCharStyles,
                        m_numStyleIds);
                // 7 return proper CxeMesseges
                CxeMessageType type = getPostConversionEvent();
                CxeMessage cxeMessage = new CxeMessage(type);
                cxeMessage.setParameters(m_cxeMessage.getParameters());
                cxeMessage.setMessageData(messageData[i]);

                String eventFlowXml = m_eventFlow.serializeToXml();
                cxeMessage.setEventFlowXml(eventFlowXml);

                File f = new File(xmlfilename);
                String name = f.getName();
                if (m_type == OFFICE_PPTX)
                {
                    if (name.startsWith("slides"))
                    {
                        slides.add(cxeMessage);
                    }
                    else if (name.startsWith("notes"))
                    {
                        notes.add(cxeMessage);
                    }
                    else
                    {
                        others.add(cxeMessage);
                    }
                }
                else
                {
                    msgs[i] = cxeMessage;
                }
            }
            writeDebugFile(m_conversionType + "_" + getBaseFileName()
                    + "_sa.xml", m_eventFlow.serializeToXml());

            if (m_type == OFFICE_PPTX)
            {
                List<AdapterResult> as = new ArrayList<AdapterResult>();
                if (slides.size() > 0)
                {
                    AdapterResult ar = new AdapterResult();
                    ar.addAllMsg(slides);
                    as.add(ar);
                }

                if (notes.size() > 0)
                {
                    AdapterResult ar = new AdapterResult();
                    ar.addAllMsg(notes);
                    as.add(ar);
                }

                if (others.size() > 0)
                {
                    AdapterResult ar = new AdapterResult();
                    ar.addAllMsg(others);
                    as.add(ar);
                }

                AdapterResult results[] = new AdapterResult[as.size()];
                for (int i = 0; i < as.size(); i++)
                {
                    results[i] = as.get(i);
                }

                return results;
            }
            else
            {
                AdapterResult results[] = new AdapterResult[msgs.length];
                for (int i = 0; i < msgs.length; i++)
                {
                    results[i] = new AdapterResult(msgs[i]);
                }

                return results;
            }
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

    private void repairExcelTable(String name)
    {
        ExcelTableRepairer repairer = new ExcelTableRepairer();
        repairer.setSourceRoot(m_convDir + "/" + m_eventFlow.getSourceLocale()
                + "/" + name);
        repairer.setTargetRoot(m_convDir + "/" + m_eventFlow.getTargetLocale()
                + "/" + name);

        try
        {
            repairer.repair();
        }
        catch (Exception e)
        {
            m_logger.error(e);
        }
    }

    private void repairExcelLink(String name)
    {
        ExcelLinkRepairer repairer = new ExcelLinkRepairer();
        repairer.setSourceRoot(m_convDir + "/" + m_eventFlow.getSourceLocale()
                + "/" + name);
        repairer.setTargetRoot(m_convDir + "/" + m_eventFlow.getTargetLocale()
                + "/" + name);

        try
        {
            repairer.repair();
        }
        catch (Exception e)
        {
            m_logger.error(e);
        }
    }

    public CxeMessage performConversionBack() throws MsOfficeAdapterException
    {
        m_isImport = false;
        try
        {
            setBasicParams();
            HashMap params = m_cxeMessage.getParameters();

            String exportBatchId = m_eventFlow.getBatchInfo().getBatchId();
            String targetLocale = m_eventFlow.getTargetLocale();
            int docPageCount = m_eventFlow.getBatchInfo().getDocPageCount();
            String key = exportBatchId + getBaseFileName() + targetLocale;

            String oofilename = getCategory().getDiplomatAttribute(
                    "safeBaseFileName").getValue();
            String eBatchId = (String) params.get("ExportBatchId");
            String tFileName = (String) params.get("TargetFileName");

            synchronized (s_exportBatchesLocker)
            {
                if (m_type == OFFICE_XLSX
                        && ExportUtil.isFirstFileAndAllFileSelected(eBatchId,
                                tFileName, docPageCount, targetLocale))
                {
                    copyAllFiles(key, oofilename);
                }
            }

            String saveFileName = writeContentToXmlBox();
            if (ExportUtil.isLastFile(eBatchId, tFileName, targetLocale))
            {

                repairExcelLink(oofilename);
                repairExcelTable(oofilename);

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

            Set<String> hiddenStyles = new HashSet<String>();
            MSOffice2010Filter msf = getMainFilter();
            if (msf != null)
            {
                hiddenStyles.addAll(msf.getUnextractableExcelCellStyles());
            }

            Set<String> hiddenXfIds = new HashSet<String>();
            Set<String> hiddenXfxIds = new HashSet<String>();

            for (String style : hiddenStyles)
            {
                String xpath = "//*[local-name()=\"cellStyles\"]/*[local-name()=\"cellStyle\"][@name=\""
                        + style + "\"]";
                NodeList affectedNodes = XPathAPI.selectNodeList(stylesNode,
                        xpath);

                if (affectedNodes != null && affectedNodes.getLength() > 0)
                {
                    int len = affectedNodes.getLength();
                    for (int i = 0; i < len; i++)
                    {
                        Element node = (Element) affectedNodes.item(i);
                        hiddenXfIds.add(node.getAttribute("xfId"));
                    }
                }
            }

            String xpath = "//*[local-name()=\"cellXfs\"]/*[local-name()=\"xf\"]";

            NodeList affectedNodes = XPathAPI.selectNodeList(stylesNode, xpath);

            if (affectedNodes != null && affectedNodes.getLength() > 0)
            {
                int len = affectedNodes.getLength();
                for (int i = 0; i < len; i++)
                {
                    Element node = (Element) affectedNodes.item(i);
                    String numFmtId = node.getAttribute("numFmtId");
                    String xfId = node.getAttribute("xfId");
                    String applyNumberFormat = node
                            .getAttribute("applyNumberFormat");

                    if (hiddenXfIds.contains(xfId))
                    {
                        styleidList.add(i + "");
                        hiddenXfxIds.add(i + "");
                    }
                    else if (!"0".equals(numFmtId)
                            && ("1".equals(applyNumberFormat) || ""
                                    .equals(applyNumberFormat)))
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

            List<File> sheets = FileUtil.getAllFiles(new File(dir,
                    "xl/worksheets"), new FileFilter()
            {
                @Override
                public boolean accept(File pathname)
                {
                    String name = pathname.getName();

                    return name.startsWith("sheet") && name.endsWith(".xml");
                }
            });

            // Unextractable Excel Cell Styles
            Set<String> result = new HashSet<String>();
            for (File sheet : sheets)
            {
                for (String id : hiddenXfxIds)
                {
                    xpath = "//*[local-name()=\"c\"][@s=\"" + id + "\"]";

                    try
                    {
                        affectedNodes = getAffectedNodes(
                                sheet.getAbsolutePath(), xpath);
                        if (affectedNodes != null
                                && affectedNodes.getLength() > 0)
                        {
                            for (int j = 0; j < affectedNodes.getLength(); j++)
                            {
                                Element ce = (Element) affectedNodes.item(j);
                                String ss = ce.getAttribute("t");
                                if ("s".equals(ss))
                                {
                                    String vnid = getExcelVText(ce);
                                    result.add(vnid);
                                }
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        m_logger.error(e);
                    }
                }
            }

            m_unextractableExcelCellStyles.addAll(result);
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
    private String getStyleIds(String dir, String p_styleCatogery,
            String p_styleType)
    {
        if (m_type != OFFICE_DOCX)
            return "";

        MSOffice2010Filter msf = getMainFilter();

        if (msf == null)
            return "";

        boolean isChar = false;
        String styles = "";
        if (STYLE_CATEGORY_CHARACTER_UN.equals(p_styleCatogery))
        {
            isChar = true;
            styles = msf.getUnextractableWordCharacterStyles();
        }
        else if (STYLE_CATEGORY_PARAGRAPH_UN.equals(p_styleCatogery))
        {
            styles = msf.getUnextractableWordParagraphStyles();
        }
        else if (STYLE_CATEGORY_CHARACTER_INTERNAL.equals(p_styleCatogery))
        {
            isChar = true;
            styles = msf.getSelectedInternalTextStyles();
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
        Set<String> styleidList = new HashSet<String>();

        try
        {
            // file not exists or not a file, return empty
            File styleFile = new File(stylesXml);
            if (!styleFile.exists() || !styleFile.isFile())
            {
                return "";
            }

            DOMParser stylesParser = new DOMParser();
            stylesParser.parse(new InputSource(new FileInputStream(styleFile)));
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
                .toString(new ArrayList<String>(styleidList));
    }

    private Set<String> addValueIfNotExists(Set<String> styleNames, String val)
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
            m_isHeaderTranslate = f != null ? f.isHeaderTranslate() : false;
            m_isFootendNotesTranslate = f != null ? f.isFootendnoteTranslate()
                    : false;
            m_isURLTranslate = f != null ? f.isUrlTranslate() : false;
            m_isMasterTranslate = (m_type == OFFICE_PPTX && f != null) ? f
                    .isMasterTranslate() : false;
            m_isNotesTranslate = (m_type == OFFICE_PPTX && f != null) ? f
                    .isNotesTranslate() : false;
            m_isNotesMasterTranslate = (m_type == OFFICE_PPTX && f != null) ? f
                    .isNotemasterTranslate() : false;
            m_isSlideLayoutTranslate = (m_type == OFFICE_PPTX && f != null) ? f
                    .isPptlayoutTranslate() : false;
            m_isHandoutMasterTranslate = (m_type == OFFICE_PPTX && f != null) ? f
                    .isHandoutmasterTranslate() : false;
            m_isExcelTabNamesTranslate = (m_type == OFFICE_XLSX && f != null) ? f
                    .isExcelTabNamesTranslate() : false;
            m_isToolTipsTranslate = f != null ? f.isToolTipsTranslate() : false;
            m_isHiddenTextTranslate = f != null ? f.isHiddenTextTranslate()
                    : false;
            isTableOfContentTranslate = f != null ? f
                    .isTableOfContentTranslate() : false;
        }
        catch (Exception e)
        {
            m_logger.error("Unable to set basic parameters. ", e);
            throw new MsOfficeAdapterException("Unexpected", null, e);
        }
    }

    /**
     * Sets the internal type (DOCX, XLSX, PPTX, etc.)
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
            // get main filter from evetflow xml
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
            else if (fileNamePrefix.startsWith("chart"))
            {
                newDisplayName = "(" + fileNamePrefix + ") " + m_oriDisplayName;
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
            else if (fileNamePrefix.startsWith("handoutMaster"))
            {
                newDisplayName = DNAME_PRE_PPTX_HANDOUTMASTER + fileNumber
                        + ") " + m_oriDisplayName;
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
            else if (fileNamePrefix.startsWith("drawing"))
            {
                newDisplayName = "(" + fileNamePrefix + ") " + m_oriDisplayName;
            }
            else if (fileNamePrefix.startsWith("data"))
            {
                newDisplayName = "(" + fileNamePrefix + ") " + m_oriDisplayName;
            }
            else if (fileNamePrefix.startsWith("chart"))
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
            else if (fileNamePrefix.startsWith("data"))
            {
                newDisplayName = DNAME_PRE_PPTX_DIAGRAM + fileNamePrefix + ") "
                        + m_oriDisplayName;
            }
            else if (fileNamePrefix.startsWith("chart")
                    || fileNamePrefix.startsWith("drawing"))
            {
                newDisplayName = "(" + fileNamePrefix + ") " + m_oriDisplayName;
            }
        }

        if (m_isHeaderTranslate)
        {
            if (fileNamePrefix.startsWith("header")
                    || fileNamePrefix.startsWith("footer"))
            {
                newDisplayName = "(" + fileNamePrefix + ") " + m_oriDisplayName;
            }
        }

        if (m_isFootendNotesTranslate)
        {
            if (fileNamePrefix.startsWith("footnotes")
                    || fileNamePrefix.startsWith("endnotes"))
            {
                newDisplayName = "(" + fileNamePrefix + ") " + m_oriDisplayName;
            }
        }

        if (m_isURLTranslate)
        {
            if (fileNamePrefix.startsWith("document.xml"))
            {
                newDisplayName = "(Hyperlinks) " + m_oriDisplayName;
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
                || p_displayName.startsWith("(chart")
                || p_displayName.startsWith("(drawing")
                || p_displayName.startsWith("(data")
                || p_displayName.startsWith(DNAME_PRE_PPTX_DIAGRAM)
                || p_displayName.startsWith(DNAME_PRE_PPTX_NOTE)
                || p_displayName.startsWith(DNAME_PRE_PPTX_MASTER)
                || p_displayName.startsWith(DNAME_PRE_PPTX_LAYOUT)
                || p_displayName.startsWith(DNAME_PRE_PPTX_NOTEMASTER)
                || p_displayName.startsWith(DNAME_PRE_PPTX_HANDOUTMASTER))
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
            String unCharStyles, String internalCharStyles, String numStyleIds)
            throws Exception
    {
        if (unParaStyles == null || unParaStyles.length() == 0)
        {
            unParaStyles = ",";
        }
        if (unCharStyles == null || unCharStyles.length() == 0)
        {
            unCharStyles = ",";
        }
        if (internalCharStyles == null || internalCharStyles.length() == 0)
        {
            internalCharStyles = ",";
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
            {
                    oriC.getDiplomatAttribute("postMergeEvent"),
                    oriC.getDiplomatAttribute("formatType"),
                    oriC.getDiplomatAttribute("safeBaseFileName"),
                    oriC.getDiplomatAttribute("originalFileSize"),
                    new DiplomatAttribute("unParaStyles", unParaStyles),
                    new DiplomatAttribute("unCharStyles", unCharStyles),
                    new DiplomatAttribute("internalCharStyles",
                            internalCharStyles),
                    new DiplomatAttribute("numStyleIds", numStyleIds),
                    new DiplomatAttribute("hiddenSharedSI", hiddenSharedSI),
                    new DiplomatAttribute("sheetHiddenCell", sheetHiddenCell),
                    new DiplomatAttribute("unextractableExcelCellStyles",
                            MSOffice2010Filter.toString(new ArrayList<String>(
                                    m_unextractableExcelCellStyles))),
                    new DiplomatAttribute("isTableOfContentTranslate",
                            String.valueOf(isTableOfContentTranslate)),
                    new DiplomatAttribute("isHeaderFooterTranslate",
                            String.valueOf(m_isHeaderTranslate)),
                    new DiplomatAttribute("isToolTipsTranslate",
                            String.valueOf(m_isToolTipsTranslate)),
                    new DiplomatAttribute("isHiddenTextTranslate",
                            String.valueOf(m_isHiddenTextTranslate)),
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
                    new DiplomatAttribute("internalCharStyles",
                            internalCharStyles),
                    new DiplomatAttribute("numStyleIds", numStyleIds),
                    new DiplomatAttribute("hiddenSharedSI", hiddenSharedSI),
                    new DiplomatAttribute("sheetHiddenCell", sheetHiddenCell),
                    new DiplomatAttribute("unextractableExcelCellStyles",
                            MSOffice2010Filter.toString(new ArrayList<String>(
                                    m_unextractableExcelCellStyles))),
                    new DiplomatAttribute("isTableOfContentTranslate",
                            String.valueOf(isTableOfContentTranslate)),
                    new DiplomatAttribute("isHeaderFooterTranslate",
                            String.valueOf(m_isHeaderTranslate)),
                    new DiplomatAttribute("isToolTipsTranslate",
                            String.valueOf(m_isToolTipsTranslate)),
                    new DiplomatAttribute("isHiddenTextTranslate",
                            String.valueOf(m_isHiddenTextTranslate)),
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

    /**
     * Get files to be extracted base on filter configuration
     * 
     * @param dir
     *            the root path where contain office xml files
     * @return
     */
    public String[] getLocalizeXmlFiles(String dir)
    {
        List<String> list = new ArrayList<String>();

        if (m_type == OFFICE_DOCX)
        {
            getLocalizedXmlFilesDOCX(dir, list);
        } // docx
        else if (m_type == OFFICE_XLSX)
        {
            getLocalizedXmlFilesXLSX(dir, list);
        } // xlsx
        else if (m_type == OFFICE_PPTX)
        {
            getLocalizedXmlFilesPPTX(dir, list);
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

    private void getLocalizedXmlFilesDOCX(String dir, List<String> list)
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

        // get endnotes / footnotes xml
        if (m_isFootendNotesTranslate)
        {
            File wordDir = new File(dir, DOCX_WORD_DIR);
            if (wordDir.isDirectory())
            {
                File[] notes = listAcceptFiles(wordDir, prefix_footendnotes);

                if (notes != null && notes.length >= 0)
                {
                    for (int i = 0; i < notes.length; i++)
                    {
                        File f = notes[i];
                        String keyText = "</w:t>";
                        if (isFileContains(f, keyText, true))
                            list.add(f.getPath());
                    }
                }
            }
        }

        if (m_isURLTranslate)
        {
            File docxmlRels = new File(dir, DOCX_RELS_XML);
            if (docxmlRels.exists())
            {
                String keyText = " TargetMode=\"External\"";
                String keyText2 = "/hyperlink\"";
                if (isFileContains(docxmlRels, keyText, true)
                        && isFileContains(docxmlRels, keyText2, true))
                    list.add(docxmlRels.getPath());
            }
        }

        File diagramDir = new File(dir, DOCX_DIAGRAMS_DIR);
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

        File chartDir = new File(dir, DOCX_CHAR_DIR);
        if (chartDir.isDirectory())
        {
            final String[] acceptNames =
            { "chart" };
            File[] datafiles = listAcceptFiles(chartDir, acceptNames);

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

        File drawDir = new File(dir, "/word/drawings");
        if (drawDir.isDirectory())
        {
            final String[] acceptNames =
            { "drawing" };
            File[] datafiles = listAcceptFiles(drawDir, acceptNames);

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
    }

    /**
     * For some cases, there are different same texts in sheets but there is
     * only one text stored in sharedStrings.xml. We need to handle them
     * accordingly.
     * <p>
     * For xlsx only.
     */
    protected void preHandleTextInSharedStringsXml(String dir)
    {
        if (m_type != OFFICE_XLSX)
        {
            return;
        }

        File sharedStrings = new File(dir, XLSX_CONTENT_SHARE);
        if (!sharedStrings.exists())
        {
            return;
        }

        try
        {
            // read <si>xxxx</si> in sharedStrings.xml
            String sharedStringsContent = FileUtil.readFile(sharedStrings,
                    "utf-8");

            Map<Long, String> sMap = new HashMap<Long, String>();
            long siNumber = -1;
            String lastSiContent = "";
            int lastStart = -1;
            int lastEnd = -1;

            Pattern p = Pattern.compile(RE_SI);
            Matcher m = p.matcher(sharedStringsContent);
            while (m.find())
            {
                siNumber++;
                sMap.put(siNumber, m.group(1));
                lastSiContent = m.group();
                lastStart = m.start();
                lastEnd = m.end();
            }

            // find repeated si references in sheet xmls
            File[] sheets = getSheetFiles(new File(dir, XLSX_SHEETS_DIR));
            Set<Long> set = new HashSet<Long>();
            List<Long> repeatedSIds = new ArrayList<Long>();
            for (File sheet : sheets)
            {
                String sheetContent = FileUtil.readFile(sheet, "utf-8");
                p = Pattern.compile(RE_C);
                m = p.matcher(sheetContent);
                while (m.find())
                {
                    Pattern p1 = Pattern.compile(RE_V);
                    Matcher m1 = p1.matcher(m.group(2));
                    if (m1.find())
                    {
                        long v = Long.parseLong(m1.group(1));
                        String sharedString = sMap.get(v);
                        if (sharedString != null
                                && !sharedString.trim().isEmpty())
                        {
                            if (set.contains(v))
                            {
                                // found repeated v id, then replace old v id
                                // with new one to be added in sharedStrings.xml
                                String newC = m.group(1) + "<v>" + (++siNumber)
                                        + "</v>" + m.group(3);
                                sheetContent = sheetContent.replace(m.group(),
                                        newC);
                                repeatedSIds.add(v);
                            }
                            else
                            {
                                set.add(v);
                            }
                        }
                    }
                }

                FileUtil.writeFile(sheet, sheetContent, "utf-8");
            }

            StringBuilder newLast = new StringBuilder(lastSiContent);
            for (long sId : repeatedSIds)
            {
                // add repeated si content in sharedStrings.xml
                String siToAdd = "<si>" + sMap.get(sId) + "</si>";
                newLast.append(siToAdd);
            }

            if (repeatedSIds.size() > 0)
            {
                String before = sharedStringsContent.substring(0, lastStart);
                String end = sharedStringsContent.substring(lastEnd);
                sharedStringsContent = before + newLast.toString() + end;

                FileUtil.writeFile(sharedStrings, sharedStringsContent, "utf-8");
            }
        }
        catch (Exception e)
        {
            m_logger.error("Pre-handle sharedStrings.xml error.", e);
        }
    }

    /**
     * Finds hidden texts in document.xml and adds tag "<w:vanish/>" to mark
     * them specially.
     * <p>
     * For GBS-2554.
     */
    private void preHandleHiddenTextInDocx(String dir)
    {
        if (m_isHiddenTextTranslate || m_type != OFFICE_DOCX)
        {
            return;
        }

        try
        {
            File document = new File(dir, DOCX_CONTENT_XML);
            File style = new File(dir, DOCX_STYLE_XML);
            String documentContent = FileUtil.readFile(document, "utf-8");
            String styleContent = FileUtil.readFile(style, "utf-8");

            // "<w:pPr><w:pStyle"
            Pattern p = Pattern.compile(RE_WP);
            Matcher m = p.matcher(documentContent);
            while (m.find())
            {
                Pattern p1 = Pattern.compile(RE_PPR);
                Matcher m1 = p1.matcher(m.group(2));
                if (m1.find())
                {
                    String wppr = m1.group(2);
                    Pattern p2 = Pattern.compile(RE_PSTYLE);
                    Matcher m2 = p2.matcher(wppr);
                    if (m2.find())
                    {
                        Pattern p3 = Pattern.compile(MessageFormat.format(
                                RE_STYLE, m2.group(1)));
                        Matcher m3 = p3.matcher(styleContent);
                        if (m3.find())
                        {
                            Pattern p31 = Pattern.compile(RE_RPR);
                            Matcher m31 = p31.matcher(m3.group(1));
                            if (m31.find())
                            {
                                if (!m31.group(2).contains(W_VANISH))
                                {
                                    continue;
                                }
                            }
                            else
                            {
                                // no <w:rPr>, so no "<w:vanish/>"
                                continue;
                            }
                            // found "<w:vanish/>", indicating the style is
                            // hidden
                            String wp = m.group(2);
                            Pattern p4 = Pattern.compile(RE_WR);
                            Matcher m4 = p4.matcher(wp);
                            while (m4.find())
                            {
                                String wr = m4.group(2);
                                if (wr.contains("<w:t"))
                                {
                                    Pattern p41 = Pattern.compile(RE_RPR);
                                    Matcher m41 = p41.matcher(wr);
                                    if (m41.find())
                                    {
                                        String wrpr = m41.group(2);
                                        if (!wrpr.contains(W_VANISH0))
                                        {
                                            // add "<w:vanish/>" since it is in
                                            // hidden style
                                            if (!wrpr.contains(W_VANISH))
                                            {
                                                wrpr = W_VANISH + wrpr;
                                                wr = wr.replace(m41.group(),
                                                        m41.group(1) + wrpr
                                                                + m41.group(3));
                                            }
                                        }
                                        else
                                        {
                                            // remove "<w:vanish w:val=\"0\"/>"
                                            // since the text with it is not
                                            // hidden
                                            wrpr = wrpr.replace(W_VANISH0, "");
                                            if (wrpr.trim().isEmpty())
                                            {
                                                wr = wr.replace(m41.group(), "");
                                            }
                                            else
                                            {
                                                wr = wr.replace(m41.group(),
                                                        m41.group(1) + wrpr
                                                                + m41.group(3));
                                            }
                                        }
                                    }
                                    else
                                    {
                                        // add "<w:vanish/>" since it is in
                                        // hidden style
                                        wr = "<w:rPr><w:vanish/></w:rPr>" + wr;
                                    }
                                }

                                wp = wp.replace(m4.group(), m4.group(1) + wr
                                        + m4.group(3));
                            }

                            String newContent = m.group(1) + wp + m.group(3);
                            documentContent = documentContent.replace(
                                    m.group(), newContent);
                        }
                    }
                }
            }
            // "<w:rPr><w:rStyle"
            p = Pattern.compile(RE_WR);
            m = p.matcher(documentContent);
            while (m.find())
            {
                Pattern p1 = Pattern.compile(RE_RPR);
                Matcher m1 = p1.matcher(m.group(2));
                if (m1.find())
                {
                    String wrpr = m1.group(2);
                    Pattern p2 = Pattern.compile(RE_RSTYLE);
                    Matcher m2 = p2.matcher(wrpr);
                    if (m2.find())
                    {
                        Pattern p3 = Pattern.compile(MessageFormat.format(
                                RE_STYLE, m2.group(1)));
                        Matcher m3 = p3.matcher(styleContent);

                        if (m3.find())
                        {
                            Pattern p21 = Pattern.compile(RE_RPR);
                            Matcher m21 = p21.matcher(m3.group(1));
                            if (m21.find())
                            {
                                if (!m21.group(2).contains(W_VANISH))
                                {
                                    continue;
                                }
                            }
                            else
                            {
                                // no <w:rPr>, so no "<w:vanish/>"
                                continue;
                            }
                            // found "<w:vanish/>", indicating the style is
                            // hidden
                            String wr = m.group(2);
                            if (wr.contains("<w:t"))
                            {
                                if (!wrpr.contains(W_VANISH0))
                                {
                                    // add "<w:vanish/>" since it is in hidden
                                    // style
                                    if (!wrpr.contains(W_VANISH))
                                    {
                                        wrpr = W_VANISH + wrpr;
                                        wr = wr.replace(m1.group(), m1.group(1)
                                                + wrpr + m1.group(3));
                                    }
                                }
                                else
                                {
                                    // remove "<w:vanish w:val=\"0\"/>"
                                    // since the text with it is not
                                    // hidden
                                    wrpr = wrpr.replace(W_VANISH0, "");
                                    if (wrpr.trim().isEmpty())
                                    {
                                        wr = wr.replace(m1.group(), "");
                                    }
                                    else
                                    {
                                        wr = wr.replace(m1.group(), m1.group(1)
                                                + wrpr + m1.group(3));
                                    }
                                }

                                String newContent = m.group(1) + wr
                                        + m.group(3);
                                documentContent = documentContent.replace(
                                        m.group(), newContent);
                            }
                        }
                    }
                }
            }

            FileUtil.writeFile(document, documentContent, "utf-8");
        }
        catch (Exception e)
        {
            m_logger.error("Pre-handle hidden text error in docx.", e);
        }
    }

    private void getDrawingFiles(String dir, List<String> list)
    {
        File root = new File(dir + "/xl/drawings");
        if (root.exists())
        {
            List<File> fs = FileUtil.getAllFiles(root, new FileFilter()
            {
                @Override
                public boolean accept(File arg0)
                {
                    String name = arg0.getName();
                    if (name.startsWith("drawing") && name.endsWith(".xml"))
                    {
                        try
                        {
                            String text = FileUtils.read(arg0, "UTF-8");
                            if (text.contains("<a:t>"))
                            {
                                return true;
                            }
                            else if (m_isToolTipsTranslate)
                            {
                                List<String> xdrs = getXdr(text);
                                for (String xdr : xdrs)
                                {
                                    if (xdr.contains("descr="))
                                    {
                                        // found toolTips
                                        // <xdr:cNvPr title="title"
                                        // descr="this is toolTips"
                                        // name="Picture 1" id="1"/>
                                        return true;
                                    }
                                }
                            }
                        }
                        catch (IOException e)
                        {
                            // ignore
                            logException(e);
                        }
                    }

                    return false;
                }
            });

            for (File f : fs)
            {
                list.add(f.getAbsolutePath());
            }
        }

    }

    private static List<String> getXdr(String text)
    {
        List<String> xdrs = new ArrayList<String>();
        Pattern p = Pattern.compile("<xdr:cNvPr [^>]*?>");
        Matcher m = p.matcher(text);

        while (m.find())
        {
            xdrs.add(m.group());
        }

        return xdrs;
    }

    private void getDiagramsFiles(String dir, List<String> list)
    {
        File root = new File(dir + "/xl/diagrams");
        if (root.exists())
        {
            List<File> fs = FileUtil.getAllFiles(root, new FileFilter()
            {
                @Override
                public boolean accept(File arg0)
                {
                    String name = arg0.getName();
                    if (name.startsWith("data") && name.endsWith(".xml"))
                    {
                        try
                        {
                            String text = FileUtils.read(arg0, "UTF-8");
                            if (text.contains("<a:t>"))
                            {
                                return true;
                            }
                        }
                        catch (IOException e)
                        {
                            // ignore
                            logException(e);
                        }
                    }

                    return false;
                }
            });

            for (File f : fs)
            {
                list.add(f.getAbsolutePath());
            }
        }
    }

    private void getChartsFiles(String dir, List<String> list)
    {
        File root = new File(dir + "/xl/charts");
        if (root.exists())
        {
            List<File> fs = FileUtil.getAllFiles(root, new FileFilter()
            {
                @Override
                public boolean accept(File arg0)
                {
                    String name = arg0.getName();
                    if (name.startsWith("chart") && name.endsWith(".xml"))
                    {
                        try
                        {
                            String text = FileUtils.read(arg0, "UTF-8");
                            if (text.contains("<a:t>"))
                            {
                                return true;
                            }
                        }
                        catch (IOException e)
                        {
                            // ignore
                            logException(e);
                        }
                    }

                    return false;
                }
            });

            for (File f : fs)
            {
                list.add(f.getAbsolutePath());
            }
        }
    }

    private void getLocalizedXmlFilesXLSX(String dir, List<String> list)
    {
        String sharedXml = FileUtils.concatPath(dir, XLSX_CONTENT_SHARE);
        if (new File(sharedXml).exists())
        {
            list.add(sharedXml);
        }

        // get sheet name
        String sheetnameXml = FileUtils.concatPath(dir, XLSX_SHEET_NAME);

        if (m_isExcelTabNamesTranslate)
        {
            list.add(sheetnameXml);
        }

        List<String> hiddenSheetIds = getExcelHiddenSheetId(sheetnameXml);

        getDrawingFiles(dir, list);
        getDiagramsFiles(dir, list);
        getChartsFiles(dir, list);

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

                if (list.size() == 0 && sheets.length > 0)
                {
                    list.add(sheets[0].getPath());
                }
            }
        }
    }

    private void getLocalizedXmlFilesPPTX(String dir, List<String> list)
    {
        // check if section name exists
        String presentationXml = FileUtils.concatPath(dir,
                PPTX_PRESENTATION_XML);
        try
        {
            File presentationFile = new File(presentationXml);

            if (presentationFile.exists())
            {
                if (isFileContains(presentationFile, "p14:section name=", false))
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

        File chartDir = new File(dir, PPTX_CHART_DIR);
        if (chartDir.isDirectory())
        {
            final String[] acceptNames =
            { "chart" };
            File[] datafiles = listAcceptFiles(chartDir, acceptNames);

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

        File drawDir = new File(dir, "/ppt/drawings");
        if (drawDir.isDirectory())
        {
            final String[] acceptNames =
            { "drawing" };
            File[] datafiles = listAcceptFiles(drawDir, acceptNames);

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

        // get notes if needed
        if (m_isNotesTranslate)
        {
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

        // get slide layouts if needed
        if (m_isSlideLayoutTranslate)
        {
            File tmpdir = new File(dir, PPTX_SLIDE_LAYOUT_DIR);
            if (tmpdir.isDirectory())
            {
                File[] tempfiles = listAcceptFiles(tmpdir, prefix_slideLayout);

                if (tempfiles != null && tempfiles.length >= 0)
                {
                    for (int i = 0; i < tempfiles.length; i++)
                    {
                        File f = tempfiles[i];
                        if (isFileContains(f, "</a:r>", false))
                        {
                            list.add(f.getPath());
                        }
                    }
                }
            }
        }

        // get master pages if needed
        if (m_isNotesMasterTranslate)
        {
            File tmpdir = new File(dir, PPTX_SLIDE_NOTESMASTER_DIR);
            if (tmpdir.isDirectory())
            {
                File[] tempfiles = listAcceptFiles(tmpdir, prefix_notesMaster);

                if (tempfiles != null && tempfiles.length >= 0)
                {
                    for (int i = 0; i < tempfiles.length; i++)
                    {
                        File f = tempfiles[i];
                        if (isFileContains(f, "</a:r>", false))
                        {
                            list.add(f.getPath());
                        }
                    }
                }
            }
        }

        // get master pages if needed
        if (m_isHandoutMasterTranslate)
        {
            File tmpdir = new File(dir, PPTX_SLIDE_HANDOUTMASTER_DIR);
            if (tmpdir.isDirectory())
            {
                File[] tempfiles = listAcceptFiles(tmpdir, prefix_handoutMaster);

                if (tempfiles != null && tempfiles.length >= 0)
                {
                    for (int i = 0; i < tempfiles.length; i++)
                    {
                        File f = tempfiles[i];
                        if (isFileContains(f, "</a:r>", false))
                        {
                            list.add(f.getPath());
                        }
                    }
                }
            }
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

    private void handleExcelHidden(String dir)
    {
        if (m_isHiddenTextTranslate || m_type != OFFICE_XLSX)
        {
            return;
        }

        String sheetnameXml = FileUtils.concatPath(dir, XLSX_SHEET_NAME);
        String sheetsDir = FileUtils.concatPath(dir, XLSX_SHEETS_DIR);

        Set<String> hiddenSharedIds = new HashSet<String>();

        // get it in hidden sheets
        List<String> hiddenSheetIds = getExcelHiddenSheetId(sheetnameXml);
        if (hiddenSheetIds != null && hiddenSheetIds.size() > 0)
        {
            for (String id : hiddenSheetIds)
            {
                String sheet = FileUtils.concatPath(sheetsDir, "sheet" + id
                        + ".xml");
                hiddenSharedIds.addAll(getSharedIdInSheet(sheet));
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
                String sheetPath = f.getPath();
                String fbasename = FileUtils.getBaseName(sheetPath);
                String fprefix = FileUtils.getPrefix(fbasename);
                String fid = fprefix.substring(5);
                Set<String> hiddenCells = new HashSet<String>();

                if (hiddenSheetIds.contains(fid))
                {
                    continue;
                }

                // not hide sheet
                try
                {
                    // check first to avoid unnecessary XML parse
                    String text = FileUtils.read(f, "UTF-8");
                    if (!text.contains(" hidden=\"1\"")
                            && !text.contains("hidden=\"true\"")
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
                    // change xerces to JDom for null pointer exception of
                    // DeferredElementNSImpl.synchronizeData
                    Set<String> cells = new HashSet<String>();
                    org.jdom.input.SAXBuilder saxb = new org.jdom.input.SAXBuilder();
                    org.jdom.Document jdomDoc = saxb.build(sheetPath);
                    org.jdom.Element jdomElem = jdomDoc.getRootElement();

                    // 1 hidden cell
                    if (m_hideCellStyleIds != null
                            && !m_hideCellStyleIds.isEmpty())
                    {
                        for (String id : m_hideCellStyleIds)
                        {
                            String xpath = "//*[local-name()=\"c\"][@s=\"" + id
                                    + "\"]";
                            List affectedNodes = org.jdom.xpath.XPath
                                    .selectNodes(jdomElem, xpath);

                            handleHiddenCellElementJdom(affectedNodes,
                                    hiddenSharedIds, hiddenCells);
                        }
                    }

                    // 2 hidden row, split rows to 4 parts to handle exception
                    // if there are so many rows
                    // String xpath0 =
                    // "//*[position() mod 4 = 0 ][local-name()=\"row\"][@hidden=\"1\"]";
                    String xpath = "//*[local-name()=\"row\"][@hidden=\"1\"]";
                    List affectedNodes = org.jdom.xpath.XPath.selectNodes(
                            jdomElem, xpath);

                    if (affectedNodes != null && affectedNodes.size() > 0)
                    {
                        int nodesLen = affectedNodes.size();
                        for (int j = 0; j < nodesLen; j++)
                        {
                            org.jdom.Element ce = (org.jdom.Element) affectedNodes
                                    .get(j);
                            String rowId = ce.getAttributeValue("r");
                            String spans = ce.getAttributeValue("spans");

                            addAllCells(cells, Integer.parseInt(rowId), spans);
                        }
                    }

                    // 3 hidden column case 1
                    xpath = "//*[local-name()=\"col\"][@hidden=\"1\"]";
                    affectedNodes = org.jdom.xpath.XPath.selectNodes(jdomElem,
                            xpath);

                    if (affectedNodes != null && affectedNodes.size() > 0)
                    {
                        String lastRow = "//*[local-name()=\"row\"][last()]";
                        List lastRowNodes = org.jdom.xpath.XPath.selectNodes(
                                jdomElem, lastRow);
                        String lastRowIndexStr = ((org.jdom.Element) lastRowNodes
                                .get(0)).getAttributeValue("r");
                        int lastRowIndex = Integer.parseInt(lastRowIndexStr);

                        int nodesLen = affectedNodes.size();
                        for (int j = 0; j < nodesLen; j++)
                        {
                            org.jdom.Element ce = (org.jdom.Element) affectedNodes
                                    .get(j);
                            String min = ce.getAttributeValue("min");
                            String max = ce.getAttributeValue("max");

                            for (int k = 1; k <= lastRowIndex; k++)
                            {
                                addAllCells(cells, k, min + ":" + max);
                            }
                        }
                    }

                    // 4 hidden column case 2
                    // <col min="9" max="9" customWidth="true" style="1"
                    // width="5.7109375" collapsed="true" hidden="true" />
                    xpath = "//*[local-name()=\"col\"][@hidden=\"true\"]";
                    affectedNodes = org.jdom.xpath.XPath.selectNodes(jdomElem,
                            xpath);
                    int nodesLen = affectedNodes.size();

                    if (affectedNodes != null && nodesLen > 0)
                    {
                        String lastRow = "//*[local-name()=\"row\"][last()]";
                        List lastRowNodes = org.jdom.xpath.XPath.selectNodes(
                                jdomElem, lastRow);
                        String lastRowIndexStr = ((org.jdom.Element) lastRowNodes
                                .get(0)).getAttributeValue("r");
                        int lastRowIndex = Integer.parseInt(lastRowIndexStr);
                        for (int j = 0; j < nodesLen; j++)
                        {
                            org.jdom.Element e = (org.jdom.Element) affectedNodes
                                    .get(j);
                            String min = e.getAttributeValue("min");
                            String max = e.getAttributeValue("max");

                            if (min != null && max != null && min.equals(max))
                            {
                                for (int k = 1; k <= lastRowIndex; k++)
                                {
                                    addAllCells(cells, k, min + ":" + max);
                                }
                            }
                        }
                    }

                    if (cells.size() != 0)
                    {
                        for (String id : cells)
                        {
                            String cellpath = "//*[local-name()=\"c\"][@r=\""
                                    + id + "\"]";
                            List cellaffectedNodes = org.jdom.xpath.XPath
                                    .selectNodes(jdomElem, cellpath);

                            handleHiddenCellElementJdom(cellaffectedNodes,
                                    hiddenSharedIds, hiddenCells);
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
                    m_hideCellMap.put(fprefix, MSOffice2010Filter
                            .toString(new ArrayList<String>(hiddenCells)));
                }
            }
        }

        hiddenSharedIds.addAll(hideSharedStrings);
        m_hiddenSharedId = MSOffice2010Filter.toString(new ArrayList<String>(
                hiddenSharedIds));
    }

    private void addAllCells(Set<String> cells, int row, String spans)
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

    private void handleHiddenCellElementJdom(List affectedNodes,
            Set<String> hiddenSharedId, Set<String> hiddenCells)
    {
        if (affectedNodes != null && affectedNodes.size() > 0)
        {
            for (int j = 0; j < affectedNodes.size(); j++)
            {
                org.jdom.Element ce = (org.jdom.Element) affectedNodes.get(j);
                String rr = ce.getAttributeValue("r");
                hiddenCells = addValueIfNotExists(hiddenCells, rr);
                String ss = ce.getAttributeValue("t");
                if ("s".equals(ss))
                {
                    String vnid = getExcelVTextJdom(ce);
                    if (vnid != null)
                    {
                        hiddenSharedId = addValueIfNotExists(hiddenSharedId,
                                vnid);
                    }
                }
            }
        }
    }

    private String getExcelVTextJdom(org.jdom.Element ce)
    {
        List children = ce.getChildren();
        org.jdom.Element vn = null;
        for (int i = 0; i < children.size(); i++)
        {
            org.jdom.Element jdElem = (org.jdom.Element) children.get(i);
            if ("v".equals(jdElem.getName()))
            {
                vn = jdElem;
                break;
            }
        }

        if (vn != null)
        {
            String vnid = vn.getText();
            return vnid;
        }
        else
        {
            return null;
        }
    }

    private void handleHiddenCellElement(NodeList affectedNodes,
            Set<String> hiddenSharedId, Set<String> hiddenCells)
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

    private Set<String> getSharedIdInSheet(String sheet)
    {
        Set<String> result = new HashSet<String>();
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
                    String rid = nd.getAttribute("r:id");
                    if (rid != null && rid.startsWith("rId"))
                    {
                        id = rid.substring(3);
                    }

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
        String[] targetLocales = m_eventFlow.getTargetLocale().split(",");
        for (int i = 0; i < targetLocales.length; i++)
        {
            String locale = targetLocales[i];
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

    /**
     * For filter testing purpose
     */
    public void setParametersForTesting(int docType, boolean isHeaderTranslate,
            boolean isMasterTranslate, boolean isNotesTranslate,
            boolean isNotesMasterTranslate, boolean isSlideLayoutTranslate,
            boolean isHandoutMasterTranslate)
    {
        m_type = docType;
        m_isHeaderTranslate = isHeaderTranslate;
        m_isMasterTranslate = isMasterTranslate;
        m_isNotesTranslate = isNotesTranslate;
        m_isNotesMasterTranslate = isNotesMasterTranslate;
        m_isSlideLayoutTranslate = isSlideLayoutTranslate;
        m_isHandoutMasterTranslate = isHandoutMasterTranslate;
    }

    /**
     * Just for testing purpose
     */
    public OfficeXmlHelper()
    {
    }

    public static String getConversionDir() throws Exception
    {
        StringBuffer convDir = new StringBuffer();
        convDir.append(m_sc.getStringParameter(
                SystemConfigParamNames.FILE_STORAGE_DIR,
                CompanyWrapper.SUPER_COMPANY_ID));
        convDir.append(File.separator);
        convDir.append(CONVERSION_DIR_NAME);

        return convDir.toString();
    }
}
