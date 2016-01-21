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

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.apache.xerces.parsers.DOMParser;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.globalsight.cxe.adapter.AdapterResult;
import com.globalsight.cxe.adapter.IConverterHelper2;
import com.globalsight.cxe.adapter.openoffice.StringIndex;
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
import com.globalsight.cxe.util.fileImport.eventFlow.Category;
import com.globalsight.cxe.util.fileImport.eventFlow.EventFlowXml;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.ExportUtil;
import com.globalsight.ling.docproc.extractor.msoffice2010.WordExtractor;
import com.globalsight.util.FileUtil;
import com.globalsight.util.StringUtil;

public class OfficeXmlHelper implements IConverterHelper2
{
    private static final String CATEGORY_NAME = "OfficeXmlAdapter";
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

    private PptxFileManager pptxFileManager = null;

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
    private boolean m_isCommentTranslate = false;

    private Properties m_properties = null;
    private String m_safeBaseFileName = null;

    private static SystemConfiguration m_sc = SystemConfiguration.getInstance();

    // private String m_eventFlowXml;
    private CxeMessage m_cxeMessage;
    private EventFlowXml m_eventFlow;

    private String m_hiddenSharedId = "";
    private String m_numStyleIds = "";
    private HashMap<String, String> m_hideCellMap = new HashMap<String, String>();
    private Set<String> m_hiddenSheetIds = new HashSet<String>();

    private List<String> m_hideCellStyleIds = new ArrayList<String>();
    private Set<String> m_unextractableExcelCellStyles = new HashSet<String>();
    private Set<String> m_excelInternalTextCellStyles = new HashSet<String>();

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
    public static final String DOCX_NUMBERING_XML = "word/numbering.xml";
    public static final String DOCX_WORD_DIR = "word";
    public static final String DOCX_DIAGRAMS_DIR = "word/diagrams";
    public static final String DOCX_CHAR_DIR = "word/charts";
    public static final String XLSX_CONTENT_SHARE = "xl/sharedStrings.xml";
    public static final String XLSX_SHEET_NAME = "xl/workbook.xml";
    public static final String XLSX_STYLE_XML = "xl/styles.xml";
    public static final String XLSX_SHEETS_DIR = "xl/worksheets";
    public static final String XLSX_RELS_DIR = "xl/worksheets/_rels";
    public static final String PPTX_SLIDES_DIR = "ppt/slides";
    public static final String PPTX_SLIDES_RELS_DIR = "ppt/slides/_rels";
    public static final String PPTX_SLIDE_MASTER_DIR = "ppt/slideMasters";
    public static final String PPTX_SLIDE_LAYOUT_DIR = "ppt/slideLayouts";
    public static final String PPTX_SLIDE_NOTES_DIR = "ppt/notesSlides";
    public static final String PPTX_SLIDE_NOTESMASTER_DIR = "ppt/notesMasters";
    public static final String PPTX_SLIDE_HANDOUTMASTER_DIR = "ppt/handoutMasters";
    public static final String PPTX_PRESENTATION_XML = "ppt/presentation.xml";
    public static final String PPTX_DIAGRAMS_DIR = "ppt/diagrams";
    public static final String PPTX_DIAGRAMS_RELS_DIR = "ppt/diagrams/_rels";
    public static final String PPTX_CHART_DIR = "ppt/charts";
    public static final String PPTX_COMMENTS_DIR = "ppt/comments";

    public static final String DNAME_PRE_DOCX_COMMENT = "(comments) ";
    public static final String DNAME_PRE_XLSX_SHEET_NAME = "(sheet name) ";
    public static final String DNAME_PRE_XLSX_SHARED = "(shared strings) ";
    public static final String DNAME_PRE_PPTX_DIAGRAM = "(diagram ";
    public static final String DNAME_PRE_PPTX_NOTE = "(note";
    public static final String DNAME_PRE_PPTX_MASTER = "(slide master";
    public static final String DNAME_PRE_PPTX_LAYOUT = "(slide layout";
    public static final String DNAME_PRE_PPTX_NOTEMASTER = "(note master";
    public static final String DNAME_PRE_PPTX_HANDOUTMASTER = "(handout master";
    public static final String DNAME_PRE_PPTX_SLIDE = "(slide";

    public static final String DNAME_PRE_HYPERLINK = "hyperlinks";

    private static final String[] prefix_slide =
    { "slide" };
    private static final String[] prefix_slideNote =
    { "notesSlide" };
    private static final String[] prefix_sheet =
    { "sheet" };
    private static final String[] prefix_sort_sheet =
    { "sortsheet" };
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

    // for GBS-2554 & GBS-3240
    private static final String W_VANISH = "<w:vanish/>";
    private static final String W_VANISH_WITH_ATTR = "<w:vanish w:val=\"0\"/>";
    private static final Pattern W_TBL = Pattern
            .compile("(<w:tbl[^>]*>)([\\d\\D]*?)(</w:tbl>)");
    private static final Pattern W_TBLPR = Pattern
            .compile("(<w:tblPr>)([\\d\\D]*?)(</w:tblPr>)");
    private static final Pattern W_TBLSTYLE = Pattern
            .compile("<w:tblStyle[^>]*w:val=\"([^\"]*)\"[^>]*/>");
    private static final Pattern WP = Pattern
            .compile("(<w:p>)([\\d\\D]*?)(</w:p>)");
    private static final Pattern WP2 = Pattern
            .compile("(<w:p [^>]*>)([\\d\\D]*?)(</w:p>)");
    private static final Pattern W_PPR = Pattern
            .compile("(<w:pPr>)([\\d\\D]*?)(</w:pPr>)");
    private static final Pattern W_PSTYLE = Pattern
            .compile("<w:pStyle[^>]*w:val=\"([^\"]*)\"[^>]*/>");
    private static final Pattern WR = Pattern
            .compile("(<w:r>)([\\d\\D]*?)(</w:r>)");
    private static final Pattern WR2 = Pattern
            .compile("(<w:r [^>]*>)([\\d\\D]*?)(</w:r>)");
    private static final Pattern W_RPR = Pattern
            .compile("(<w:rPr>)([\\d\\D]*?)(</w:rPr>)");
    private static final Pattern W_RSTYLE = Pattern
            .compile("<w:rStyle[^>]*w:val=\"([^\"]*)\"[^>]*/>");
    private static final String W_STYLE = "<w:style[^>]*w:styleId=\"{0}\"[^>]*>([\\d\\D]*?)</w:style>";
    private static final Pattern W_BASEDON = Pattern
            .compile("<w:basedOn[^>]*w:val=\"([^\"]*)\"[^>]*/>");
    public static final String HIDDEN_MARK = "<gs-hidden-mark/>";

    // for xlsx repeated strings shared in sharedStrings.xml
    private static final Pattern SI = Pattern.compile("<si>([\\d\\D]*?)</si>");
    private static final Pattern C = Pattern
            .compile("(<c[^>]*t=\"s\"[^>]*>)([\\d\\D]*?)(</c>)");
    private static final Pattern V = Pattern.compile("<v>([\\d]*?)</v>");

    // GBS-2941
    public static final String NUMBERING_TAG_ADDED_START = "<gs-numbering-added-for-translation>";
    public static final String NUMBERING_TAG_ADDED_END = "</gs-numbering-added-for-translation>";

    /**
     * Init OfficeXmlHelper for office 2010 importing
     */
    public OfficeXmlHelper(CxeMessage p_cxeMessage, Logger p_logger,
            Properties p_msOfficeProperties)
    {
        m_cxeMessage = p_cxeMessage;
        m_eventFlow = p_cxeMessage.getEventFlowObject();
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

    private void splitFiles(String dirName)
    {
        PptxFileManager m = new PptxFileManager();
        m.splitFile(dirName);
    }

    private void mergePages(String dir)
    {
        pptxFileManager = new PptxFileManager();
        MSOffice2010Filter msf = getMainFilter();
        if (msf == null)
        {
            msf = new MSOffice2010Filter();
        }
        pptxFileManager.setFilter(msf);
        pptxFileManager.mergeFile(dir);
    }

    private String[] createPage(String dir) throws IOException
    {
        StringBuffer sb = new StringBuffer(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        sb.append("<pptx><nocontent/></pptx>");

        String path = dir + "/ppt/slide.xml";
        FileUtil.writeFile(new File(path), sb.toString(), "UTF-8");

        return new String[]
        { path };
    }

    private void sortSegments(String dir, List<String> hIds, String excelOrder)
    {
        if ("n".equalsIgnoreCase(excelOrder))
            return;

        ExcelFileManager m = new ExcelFileManager();
        m.sortSegments(dir, hIds, excelOrder, m_hideCellMap,
                m_excelInternalTextCellStyles);
    }

    private void mergeSortSegments(String dir)
    {
        ExcelFileManager m = new ExcelFileManager();
        m.mergeSortSegments(dir);
    }

    private void sortComments(List<String> comments)
    {
        MSOffice2010Filter msf = getMainFilter();
        if (msf == null)
        {
            msf = new MSOffice2010Filter();
        }
        String order = msf.getExcelOrder();
        if ("n".equalsIgnoreCase(order))
            return;

        ExcelFileManager m = new ExcelFileManager();
        m.sortComments(comments, order);
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

            String fpIdstr = m_eventFlow.getSource().getDataSourceId();
            boolean useNewExtractor = WordExtractor.useNewExtractor(fpIdstr);

            if (m_type == OFFICE_PPTX && useNewExtractor)
            {
                mergePages(dir);
            }
            preHandleHiddenTextInDocx(dir);
            preHandleTextInSharedStringsXml(dir);
            handleExcelStyleIds(dir);
            handleExcelHidden(dir);

            MSOffice2010Filter msf = getMainFilter();
            if (msf == null)
            {
                msf = new MSOffice2010Filter();
            }

            String[] xmlFiles;
            if (m_type == OFFICE_XLSX && useNewExtractor
                    && !"n".equalsIgnoreCase(msf.getExcelOrder()))
            {
                List<String> hidden = new ArrayList<String>();
                hidden.addAll(m_unextractableExcelCellStyles);
                if (m_hiddenSharedId != null && m_hiddenSharedId.length() > 0)
                {
                    hidden.addAll(MSOffice2010Filter.toList(m_hiddenSharedId));
                }

                sortSegments(dir, hidden, msf.getExcelOrder());
                List<String> list = new ArrayList<String>();
                getNewLocalizedXmlFilesXLSX(dir, list);

                if (list.isEmpty())
                {
                    xmlFiles = new String[0];
                }
                else
                {
                    String[] result = new String[list.size()];
                    xmlFiles = list.toArray(result);
                }
            }
            else
            {
                xmlFiles = getLocalizeXmlFiles(dir, useNewExtractor);
            }

            // 5 merge tags

            if (!(m_type == OFFICE_DOCX && useNewExtractor))
            {
                OfficeXmlTagHelper help = new OfficeXmlTagHelper(m_type);
                help.mergeTags(xmlFiles);
            }

            if (gcCounter > 100)
            {
                // call GC to free memory for large file
                System.gc();
                gcCounter = 0;
            }

            // all hidden PPTX stop creating job
            if (m_type == OFFICE_PPTX && !useNewExtractor && xmlFiles != null
                    && xmlFiles.length == 0)
            {
                xmlFiles = createPage(dir);
            }

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

            List<CxeMessage> slides = new ArrayList<CxeMessage>();
            List<CxeMessage> notes = new ArrayList<CxeMessage>();
            List<CxeMessage> others = new ArrayList<CxeMessage>();

            m_oriDisplayName = m_eventFlow.getDisplayName();
            for (int i = 0; i < msgs.length; i++)
            {
                // 5 modify eventflowxml
                EventFlowXml newEventFlowXml = m_eventFlow.clone();
                String xmlfilename = xmlFiles[i];
                if (xmlfilename != null && xmlfilename.length() > dirLen)
                {
                    int index_dirname = xmlfilename.indexOf(dirname);
                    xmlfilename = xmlfilename.substring(index_dirname);
                }
                modifyEventFlowXmlForImport(xmlfilename, i + 1, msgs.length,
                        unParaStyles, unCharStyles, internalCharStyles,
                        m_numStyleIds, newEventFlowXml);
                // 7 return proper CxeMesseges
                CxeMessageType type = getPostConversionEvent();
                CxeMessage cxeMessage = new CxeMessage(type);
                cxeMessage.setParameters(m_cxeMessage.getParameters());
                cxeMessage.setMessageData(messageData[i]);
                cxeMessage.setEventFlowObject(newEventFlowXml);

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
            // writeDebugFile(m_conversionType + "_" + getBaseFileName()
            // + "_sa.xml", m_eventFlow.serializeToXml());

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

            String oofilename = getCategory().getValue("safeBaseFileName");
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
                outputMsg.setEventFlowObject(m_eventFlow);

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
                outputMsg.setEventFlowObject(m_eventFlow);
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

    /**
     * Handles Unextractable Excel Cell Styles and Excel Internal Text Cell
     * Styles.
     */
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
            Set<String> excelInternalTextStyles = new HashSet<String>();
            MSOffice2010Filter msf = getMainFilter();
            if (msf != null)
            {
                hiddenStyles.addAll(msf.getUnextractableExcelCellStyles());
                excelInternalTextStyles.addAll(msf
                        .getSelectedExcelInternalTextStylesAsList());
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
            // GBS-3944
            Set<String> internalXfIds = new HashSet<String>();
            Set<String> internalXfxIds = new HashSet<String>();
            for (String style : excelInternalTextStyles)
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
                        internalXfIds.add(node.getAttribute("xfId"));
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

                    // GBS-3944
                    if (internalXfIds.contains(xfId))
                    {
                        internalXfxIds.add(i + "");
                    }

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
            Set<String> unextractable = new HashSet<String>();
            Set<String> internal = new HashSet<String>();
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
                                    unextractable.add(vnid);
                                }
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        m_logger.error(e);
                    }
                }
                // GBS-3944
                for (String id : internalXfxIds)
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
                                    internal.add(vnid);
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
            m_unextractableExcelCellStyles.addAll(unextractable);
            m_excelInternalTextCellStyles.addAll(internal);
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
            styles = msf.getSelectedWordInternalTextStyles();
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
            m_isCommentTranslate = f != null ? f.isCommentTranslate() : false;
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
            if (m_type == OFFICE_PPTX)
            {
                splitFiles(dirName);
            }
            else if (m_type == OFFICE_XLSX)
            {
                mergeSortSegments(dirName);
            }

            String filename = getCategory().getValue("safeBaseFileName");
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
            if ("000".equals(fileNumber))
                fileNumber = "";

            // from file data1.xml.rels, fileNamePrefix is data1.xml
            if (fileNamePrefix.startsWith("data")
                    && fileNamePrefix.endsWith(".xml"))
            {
                newDisplayName = DNAME_PRE_PPTX_DIAGRAM
                        + FileUtils.getPrefix(fileNamePrefix) + " "
                        + DNAME_PRE_HYPERLINK + ") " + m_oriDisplayName;
            }
            else if (fileNamePrefix.startsWith("data"))
            {
                newDisplayName = DNAME_PRE_PPTX_DIAGRAM + fileNamePrefix + ") "
                        + m_oriDisplayName;
            }
            else if (fileNamePrefix.equals("diagramData"))
            {
                newDisplayName = "(diagram data) " + m_oriDisplayName;
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
            // from file slide1.xml.rels, fileNamePrefix is slide1.xml
            else if (fileNamePrefix.startsWith("slide")
                    && fileNamePrefix.endsWith(".xml"))
            {
                fileNumber = getPageNumber(FileUtils.getPrefix(fileNamePrefix));
                newDisplayName = DNAME_PRE_PPTX_SLIDE + fileNumber + " "
                        + DNAME_PRE_HYPERLINK + ") " + m_oriDisplayName;
            }
            else if (fileNamePrefix.startsWith("slide"))
            {
                newDisplayName = DNAME_PRE_PPTX_SLIDE + fileNumber + ") "
                        + m_oriDisplayName;
            }
            else
            {
                newDisplayName = "(" + fileNamePrefix + ") " + m_oriDisplayName;
            }
        }

        if (m_type == OFFICE_XLSX)
        {
            if (fileNamePrefix.startsWith("sheet")
                    && fileNamePrefix.endsWith(".xml"))
            {
                String sheet = FileUtils.getPrefix(fileNamePrefix);
                newDisplayName = "(" + sheet + " " + DNAME_PRE_HYPERLINK + ") "
                        + m_oriDisplayName;
            }
            else if (fileNamePrefix.startsWith("sheet"))
            {
                newDisplayName = "(" + fileNamePrefix + ") " + m_oriDisplayName;
            }
            if (fileNamePrefix.startsWith("sortsheet"))
            {
                newDisplayName = "(" + fileNamePrefix.substring(4) + ") "
                        + m_oriDisplayName;
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
            else if (fileNamePrefix.startsWith("comments"))
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
            else if (fileNamePrefix.startsWith("numbering"))
            {
                newDisplayName = "(" + fileNamePrefix + ") " + m_oriDisplayName;
            }
            else if (fileNamePrefix.startsWith("document.xml"))
            {
                newDisplayName = "(" + DNAME_PRE_HYPERLINK + ") "
                        + m_oriDisplayName;
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
        if (m_safeBaseFileName == null)
        {
            m_safeBaseFileName = createSafeBaseFileName(getBaseFileName());
        }
        return m_safeBaseFileName;
    }

    private synchronized static String createSafeBaseFileName(String p_filename)
    {
        try
        {
            // this is required
            Thread.sleep(1000);
        }
        catch (InterruptedException e)
        {

        }
        return System.currentTimeMillis() + p_filename;
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
            String unCharStyles, String internalCharStyles, String numStyleIds,
            EventFlowXml newEventFlowXml) throws Exception
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

        p_xmlFilename = StringUtil.replace(p_xmlFilename, "\\", File.separator);
        p_xmlFilename = StringUtil.replace(p_xmlFilename, "/", File.separator);
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

        String postMergeEvent;
        String formatType;
        String safeBaseFileName;
        String originalFileSize;

        // First get original Category
        Category oriC = newEventFlowXml.getCategory(CATEGORY_NAME);
        if (oriC != null)
        {
            postMergeEvent = oriC.getValue("postMergeEvent");
            formatType = oriC.getValue("formatType");
            safeBaseFileName = oriC.getValue("safeBaseFileName");
            originalFileSize = oriC.getValue("originalFileSize");
            newEventFlowXml.getCategory().remove(oriC);
        }
        else
        {
            postMergeEvent = newEventFlowXml.getPostMergeEvent();
            formatType = newEventFlowXml.getSource().getFormatType();
            safeBaseFileName = getSafeBaseFileName();
            originalFileSize = String.valueOf(m_cxeMessage.getMessageData()
                    .getSize());
        }

        Category newC = new Category();
        newC.setName(CATEGORY_NAME);

        newC.addValue("postMergeEvent", postMergeEvent);
        newC.addValue("formatType", formatType);
        newC.addValue("safeBaseFileName", safeBaseFileName);
        newC.addValue("originalFileSize", originalFileSize);
        newC.addValue("unParaStyles", unParaStyles);
        newC.addValue("unCharStyles", unCharStyles);
        newC.addValue("internalCharStyles", internalCharStyles);
        newC.addValue("numStyleIds", numStyleIds);
        newC.addValue("hiddenSharedSI", hiddenSharedSI);
        newC.addValue("sheetHiddenCell", sheetHiddenCell);
        newC.addValue("unextractableExcelCellStyles",
                MSOffice2010Filter.toString(new ArrayList<String>(
                        m_unextractableExcelCellStyles)));
        newC.addValue("excelInternalTextCellStyles", MSOffice2010Filter
                .toString(new ArrayList<String>(m_excelInternalTextCellStyles)));
        newC.addValue("isTableOfContentTranslate",
                String.valueOf(isTableOfContentTranslate));
        newC.addValue("isHeaderFooterTranslate",
                String.valueOf(m_isHeaderTranslate));
        newC.addValue("isToolTipsTranslate",
                String.valueOf(m_isToolTipsTranslate));
        newC.addValue("isHiddenTextTranslate",
                String.valueOf(m_isHiddenTextTranslate));
        newC.addValue("relSafeName", p_xmlFilename);
        newEventFlowXml.getCategory().add(newC);

        // Then modify eventFlow
        newEventFlowXml.setPostMergeEvent(getPostMergeEvent());
        // newEventFlowXml.setSourceFormatType("xml");

        newEventFlowXml.getBatchInfo().setDocPageCount(p_docPageCount);
        newEventFlowXml.getBatchInfo().setDocPageNumber(p_docPageNum);

        // modify display name
        String number = getPageNumber(fileNamePrefix);
        String newDisplayName = getNewDisplayName(fileNamePrefix, number);

        newEventFlowXml.getBatchInfo().setDisplayName(newDisplayName);
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
    private String[] getLocalizeXmlFiles(String dir, boolean useNewExtractor)
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
            if (useNewExtractor)
                getNewLocalizedXmlFilesPPTX(dir, list);
            else
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
        String numberingXml = FileUtils.concatPath(dir, DOCX_NUMBERING_XML);
        list.add(contentXml);

        if (hasContentToExtract(numberingXml, contentXml))
        {
            list.add(numberingXml);
        }

        if (m_isCommentTranslate)
        {
            if (isFileExists(commentXml))
            {
                list.add(commentXml);
            }
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
                if (isFileContains(docxmlRels, keyText, true))
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

    private boolean hasContentToExtract(String numberingXml, String documentXml)
    {
        File numbering = new File(numberingXml);
        if (!numbering.exists())
        {
            return false;
        }
        File document = new File(documentXml);
        try
        {
            String ds = FileUtil.readFile(document, "utf-8");
            String ns = FileUtil.readFile(numbering, "utf-8");
            Pattern p = Pattern
                    .compile("<w:pStyle w:val=\"([^\"]*)\"/><w:lvlText w:val=\"([^%\"]*?)\\s*%\\d+\"/>");
            Matcher m = p.matcher(ns);
            boolean found = false;
            while (m.find())
            {
                String pStyle = m.group(1);
                String text = m.group(2);
                // check if the text is empty and whether the pStyle exists in
                // document xml
                if (!text.isEmpty() && ds.contains(pStyle))
                {
                    found = true;
                    // add a new node with the numbering text for translation.
                    // This node will be deleted and the translation will be
                    // updated to w:lvlText w:val attribute at export stage
                    String newString = m.group() + NUMBERING_TAG_ADDED_START
                            + text + NUMBERING_TAG_ADDED_END;
                    ns = StringUtil.replace(ns, m.group(), newString);
                    FileUtil.writeFile(numbering, ns, "utf-8");
                }
            }
            return found;
        }
        catch (IOException e)
        {
            m_logger.error("Error when reading xml content", e);
            return false;
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

            Matcher m = SI.matcher(sharedStringsContent);
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
                m = C.matcher(sheetContent);
                StringBuilder output = new StringBuilder();
                int start = 0;

                while (m.find())
                {
                    Matcher m1 = V.matcher(m.group(2));
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
                                // Write out all characters before this matched
                                // region
                                output.append(sheetContent.substring(start,
                                        m.start()));
                                // Write out the replacement text. This will
                                // vary by method.
                                output.append(newC);
                                start = m.end();

                                repeatedSIds.add(v);
                            }
                            else
                            {
                                set.add(v);
                            }
                        }
                    }
                }

                output.append(sheetContent.substring(start));

                FileUtil.writeFile(sheet, output.toString(), "utf-8");
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
     * Handles the texts with hidden styles in document xml.
     * 
     * @since GBS-2554, GBS-3240
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
            documentContent = processHiddenStyles(documentContent, styleContent);
            FileUtil.writeFile(document, documentContent, "utf-8");
            if (m_isCommentTranslate)
            {
                File comment = new File(dir, DOCX_COMMENT_XML);
                if (comment.exists())
                {
                    String commentContent = FileUtil.readFile(comment, "utf-8");
                    commentContent = processHiddenStyles(commentContent,
                            styleContent);
                    FileUtil.writeFile(comment, commentContent, "utf-8");
                }
            }
        }
        catch (Exception e)
        {
            m_logger.error("Pre-handle hidden text error in docx.", e);
        }
    }

    /**
     * Processes the hidden styles on document xml.
     * 
     * @return modified document xml content.
     * @since GBS-3240
     */
    private String processHiddenStyles(String documentContent,
            String styleContent)
    {
        // process table style
        Matcher m_wtbl = W_TBL.matcher(documentContent);
        while (m_wtbl.find())
        {
            String wtbl = m_wtbl.group(2);
            wtbl = processHiddenStylesOnTable(wtbl, styleContent);
            documentContent = StringUtil.replace(documentContent,
                    m_wtbl.group(), m_wtbl.group(1) + wtbl + m_wtbl.group(3));
        }
        // process paragraph style without a table
        Matcher m_wp = WP.matcher(documentContent);
        while (m_wp.find())
        {
            String wp = m_wp.group(2);
            wp = processHiddenStylesOnParagraph(wp, styleContent, false);
            documentContent = StringUtil.replace(documentContent, m_wp.group(),
                    m_wp.group(1) + wp + m_wp.group(3));
        }
        m_wp = WP2.matcher(documentContent);
        while (m_wp.find())
        {
            String wp = m_wp.group(2);
            wp = processHiddenStylesOnParagraph(wp, styleContent, false);
            documentContent = StringUtil.replace(documentContent, m_wp.group(),
                    m_wp.group(1) + wp + m_wp.group(3));
        }
        // process character style without a paragraph
        // this case may not exist in actual document, leave it commented
        // Matcher m_wr = WR.matcher(documentContent);
        // while (m_wr.find())
        // {
        // String wr = m_wr.group(2);
        // wr = processHiddenStylesOnCharacter(wr, styleContent, false, false);
        // documentContent = StringUtil.replace(documentContent, m_wr.group(),
        // m_wr.group(1) + wr + m_wr.group(3));
        // }
        // m_wr = WR2.matcher(documentContent);
        // while (m_wr.find())
        // {
        // String wr = m_wr.group(2);
        // wr = processHiddenStylesOnCharacter(wr, styleContent, false, false);
        // documentContent = StringUtil.replace(documentContent, m_wr.group(),
        // m_wr.group(1) + wr + m_wr.group(3));
        // }

        return documentContent;
    }

    /**
     * Processes hidden styles on w:tbl string text.
     * 
     * @return modified w:tbl string.
     */
    private String processHiddenStylesOnTable(String wtbl, String styleContent)
    {
        boolean isTableStyleHidden = false;
        Matcher m_wtblpr = W_TBLPR.matcher(wtbl);
        if (m_wtblpr.find())
        {
            String wtblpr = m_wtblpr.group(2);
            Matcher m_wtblstyle = W_TBLSTYLE.matcher(wtblpr);
            if (m_wtblstyle.find())
            {
                String styleId = m_wtblstyle.group(1);
                if (isHiddenStyle(styleId, styleContent))
                {
                    isTableStyleHidden = true;
                }
            }
        }
        // process paragraph style in a table
        Matcher m_wp = WP.matcher(wtbl);
        while (m_wp.find())
        {
            String wp = m_wp.group(2);
            wp = processHiddenStylesOnParagraph(wp, styleContent,
                    isTableStyleHidden);
            wtbl = StringUtil.replace(wtbl, m_wp.group(), m_wp.group(1) + wp
                    + m_wp.group(3));
        }
        m_wp = WP2.matcher(wtbl);
        while (m_wp.find())
        {
            String wp = m_wp.group(2);
            wp = processHiddenStylesOnParagraph(wp, styleContent,
                    isTableStyleHidden);
            wtbl = StringUtil.replace(wtbl, m_wp.group(), m_wp.group(1) + wp
                    + m_wp.group(3));
        }
        // process character style without a paragraph
        // this case may not exist in actual document, leave it commented
        // Matcher m_wr = WR.matcher(wtbl);
        // while (m_wr.find())
        // {
        // String wr = m_wr.group(2);
        // wr = processHiddenStylesOnCharacter(wr, styleContent,
        // isTableStyleHidden, false);
        // wtbl = StringUtil.replace(wtbl, m_wr.group(), m_wr.group(1)
        // + wr + m_wr.group(3));
        // }
        // m_wr = WR2.matcher(wtbl);
        // while (m_wr.find())
        // {
        // String wr = m_wr.group(2);
        // wr = processHiddenStylesOnCharacter(wr, styleContent,
        // isTableStyleHidden, false);
        // wtbl = StringUtil.replace(wtbl, m_wr.group(), m_wr.group(1)
        // + wr + m_wr.group(3));
        // }
        return wtbl;
    }

    /**
     * Processes hidden styles on w:p string text.
     * 
     * @return modified w:p string.
     */
    private String processHiddenStylesOnParagraph(String wp,
            String styleContent, boolean isTableStyleHidden)
    {
        boolean isParagraphStyleHidden = false;
        Matcher m_wppr = W_PPR.matcher(wp);
        if (m_wppr.find())
        {
            String wppr = m_wppr.group(2);
            Matcher m_wpstyle = W_PSTYLE.matcher(wppr);
            if (m_wpstyle.find())
            {
                String styleId = m_wpstyle.group(1);
                if (isHiddenStyle(styleId, styleContent))
                {
                    isParagraphStyleHidden = true;
                }
            }
        }
        // process character style in a paragraph
        Matcher m_wr = WR.matcher(wp);
        while (m_wr.find())
        {
            String wr = m_wr.group(2);
            wr = processHiddenStylesOnCharacter(wr, styleContent,
                    isTableStyleHidden, isParagraphStyleHidden);
            wp = StringUtil.replace(wp, m_wr.group(),
                    m_wr.group(1) + wr + m_wr.group(3));
        }
        m_wr = WR2.matcher(wp);
        while (m_wr.find())
        {
            String wr = m_wr.group(2);
            wr = processHiddenStylesOnCharacter(wr, styleContent,
                    isTableStyleHidden, isParagraphStyleHidden);
            wp = StringUtil.replace(wp, m_wr.group(),
                    m_wr.group(1) + wr + m_wr.group(3));
        }
        return wp;
    }

    /**
     * Processes hidden styles on w:r string text.
     * 
     * @return modified w:r string.
     */
    private String processHiddenStylesOnCharacter(String wr,
            String styleContent, boolean isTableStyleHidden,
            boolean isParagraphStyleHidden)
    {
        boolean isCharacterStyleHidden = false;
        String wrpr = "";
        Matcher m_rpr = W_RPR.matcher(wr);
        if (m_rpr.find())
        {
            wrpr = m_rpr.group(2);
            Matcher m_rstyle = W_RSTYLE.matcher(wrpr);
            if (m_rstyle.find())
            {
                String styleId = m_rstyle.group(1);
                if (isHiddenStyle(styleId, styleContent))
                {
                    isCharacterStyleHidden = true;
                }
            }
        }
        if (wr.contains("<w:t") && !wr.contains(HIDDEN_MARK))
        {
            if (isTableStyleHidden ^ isParagraphStyleHidden
                    ^ isCharacterStyleHidden)
            {
                // the basic logic is the final hidden style for a text is
                // calculated by exclusive OR (XOR) of table style, paragraph
                // style and character style
                if (!wrpr.contains(W_VANISH_WITH_ATTR))
                {
                    wr = HIDDEN_MARK + wr;
                }
            }
            else if (wrpr.contains(W_VANISH))
            {
                // attribute "<w:vanish/>" indicates this is a hidden text
                wr = HIDDEN_MARK + wr;
            }
        }
        return wr;
    }

    /**
     * Checks if the given style is hidden or not.
     */
    public static boolean isHiddenStyle(String styleId, String styleContent)
    {
        boolean isHidden = false;
        Pattern p_wstyle = Pattern.compile(MessageFormat.format(W_STYLE,
                styleId));
        Matcher m_wstyle = p_wstyle.matcher(styleContent);
        if (m_wstyle.find())
        {
            String style = m_wstyle.group(1);
            Matcher m_wrpr = W_RPR.matcher(style);
            if (m_wrpr.find())
            {
                String rpr = m_wrpr.group(2);
                if (rpr.contains(W_VANISH))
                {
                    return true;
                }
                else if (rpr.contains(W_VANISH_WITH_ATTR))
                {
                    // just return false if found <w:vanish w:val=\"0\"/>
                    return false;
                }
                else
                {
                    isHidden = false;
                }
            }
            else
            {
                // no <w:rPr>, so no "<w:vanish/>"
                isHidden = false;
            }

            if (!isHidden)
            {
                // recursively looking for the "<w:vanish/>" attribute according
                // to its based-on style id <w:basedOn w:val="xxxx"/>
                Matcher m_basedon = W_BASEDON.matcher(style);
                if (m_basedon.find())
                {
                    styleId = m_basedon.group(1);
                    return isHiddenStyle(styleId, styleContent);
                }
            }
        }
        return isHidden;
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

    private void getCommentFilesForPptx(String dir, List<String> list,
            List<String> slideHiddenFiles)
    {
        File root = new File(dir, PPTX_COMMENTS_DIR);
        if (root.exists())
        {
            List<File> fs = FileUtil.getAllFiles(root, new FileFilter()
            {
                @Override
                public boolean accept(File arg0)
                {
                    String name = arg0.getName();
                    if (name.startsWith("comment") && name.endsWith(".xml"))
                    {
                        try
                        {
                            String text = FileUtils.read(arg0, "UTF-8");
                            if (text.contains("<p:text"))
                            {
                                return true;
                            }
                        }
                        catch (IOException e)
                        {
                            logException(e);
                        }
                    }
                    return false;
                }
            });

            for (File f : fs)
            {
                if (PptxFileManager.isSlideHiddenFile(slideHiddenFiles, f))
                {
                    continue;
                }

                list.add(f.getAbsolutePath());
            }
        }
    }

    private void getUrlFilesForPptx(String dir, List<String> list,
            List<String> slideHiddenFiles)
    {
        if (m_isURLTranslate)
        {
            File root = new File(dir, PPTX_SLIDES_RELS_DIR);
            if (root.exists())
            {
                List<File> fs = FileUtil.getAllFiles(root, new FileFilter()
                {
                    @Override
                    public boolean accept(File arg0)
                    {
                        String name = arg0.getName();
                        if (name.endsWith(".rels"))
                        {
                            try
                            {
                                String text = FileUtils.read(arg0, "UTF-8");
                                if (text.contains(" TargetMode=\"External\""))
                                    return true;

                                return false;
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
                    if (PptxFileManager.isSlideHiddenFile(slideHiddenFiles, f))
                    {
                        continue;
                    }

                    list.add(f.getAbsolutePath());
                }
            }

            File root2 = new File(dir, PPTX_DIAGRAMS_RELS_DIR);
            if (root2.exists())
            {
                List<File> fs = FileUtil.getAllFiles(root2, new FileFilter()
                {
                    @Override
                    public boolean accept(File arg0)
                    {
                        String name = arg0.getName();
                        if (name.startsWith("data") && name.endsWith(".rels"))
                        {
                            try
                            {
                                String text = FileUtils.read(arg0, "UTF-8");
                                if (text.contains(" TargetMode=\"External\""))
                                    return true;

                                return false;
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
                    if (PptxFileManager.isSlideHiddenFile(slideHiddenFiles, f))
                    {
                        continue;
                    }

                    list.add(f.getAbsolutePath());
                }
            }
        }
    }

    private void getUrlFilesForXlsx(String dir, List<String> list)
    {
        if (m_isURLTranslate)
        {
            File root = new File(dir, XLSX_RELS_DIR);
            if (root.exists())
            {
                List<File> fs = FileUtil.getAllFiles(root, new FileFilter()
                {
                    @Override
                    public boolean accept(File arg0)
                    {
                        String name = arg0.getName();
                        if (name.endsWith(".rels"))
                        {
                            try
                            {
                                String text = FileUtils.read(arg0, "UTF-8");
                                if (text.contains(" TargetMode=\"External\""))
                                    return true;

                                return false;
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

    private void getCommentFilesForXlsx(String dir, List<String> list)
    {
        File root = new File(dir + "/xl");
        if (root.exists())
        {
            List<File> fs = FileUtil.getAllFiles(root, new FileFilter()
            {
                @Override
                public boolean accept(File arg0)
                {
                    String name = arg0.getName();
                    if (name.startsWith("comments") && name.endsWith(".xml"))
                    {
                        try
                        {
                            String text = FileUtils.read(arg0, "UTF-8");
                            if (text.contains("</t>"))
                            {
                                return true;
                            }
                        }
                        catch (IOException e)
                        {
                            logException(e);
                        }
                    }

                    return false;
                }
            });

            for (File f : fs)
            {
                if (!ExcelHiddenHandler.isCommentFromHiddenSheet(
                        m_hiddenSheetIds, f))
                {
                    list.add(f.getAbsolutePath());
                }
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

        getDrawingFiles(dir, list);
        getDiagramsFiles(dir, list);
        getChartsFiles(dir, list);
        getUrlFilesForXlsx(dir, list);
        if (m_isCommentTranslate)
        {
            getCommentFilesForXlsx(dir, list);
        }

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

                    if (!m_hiddenSheetIds.contains(fid) && !isEmpty)
                        list.add(f.getPath());
                }

                if (list.size() == 0 && sheets.length > 0)
                {
                    list.add(sheets[0].getPath());
                }
            }
        }
    }

    private void getNewLocalizedXmlFilesXLSX(String dir, List<String> list)
    {
        // get sheet name
        String sheetnameXml = FileUtils.concatPath(dir, XLSX_SHEET_NAME);

        if (m_isExcelTabNamesTranslate)
        {
            list.add(sheetnameXml);
        }

        getDrawingFiles(dir, list);
        getDiagramsFiles(dir, list);
        getChartsFiles(dir, list);
        getUrlFilesForXlsx(dir, list);
        if (m_isCommentTranslate)
        {
            List<String> comments = new ArrayList<String>();
            getCommentFilesForXlsx(dir, comments);
            sortComments(comments);
            list.addAll(comments);
        }

        File sheetsDir = new File(dir, XLSX_SHEETS_DIR);
        // get sheets
        if (sheetsDir.isDirectory())
        {

            File[] sheets = getSortSheetFiles(sheetsDir);

            if (sheets != null && sheets.length >= 0)
            {
                // get each sheet and check if it is empty
                for (int i = 0; i < sheets.length; i++)
                {
                    File f = sheets[i];
                    String fbasename = FileUtils.getPrefix(FileUtils
                            .getBaseName(f.getPath()));
                    String fid = fbasename.substring(9);

                    if (!m_hiddenSheetIds.contains(fid))
                        list.add(f.getPath());
                }

                if (list.size() == 0)
                {
                    File[] sheet2s = getSheetFiles(sheetsDir);
                    if (sheet2s.length > 0)
                        list.add(sheet2s[0].getPath());
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

        // get slides
        List<String> hiddenSlideFiles = new ArrayList<String>();
        File slidesDir = new File(dir, PPTX_SLIDES_DIR);
        if (slidesDir.isDirectory())
        {
            File[] slides = listAcceptFiles(slidesDir, prefix_slide);

            if (slides != null && slides.length >= 0)
            {
                for (int i = 0; i < slides.length; i++)
                {
                    File f = slides[i];

                    if (m_isHiddenTextTranslate)
                    {
                        list.add(f.getPath());
                    }
                    else
                    {
                        // check if this slide is hidden : GBS-3576
                        boolean isHidden = false;
                        try
                        {
                            String text = FileUtils.read(f, "UTF-8");
                            StringIndex si = StringIndex.getValueBetween(text,
                                    0, "<p:sld ", ">");

                            // check if this slide is hidden
                            if (si != null
                                    && si.allValue.contains("show=\"0\""))
                            {
                                isHidden = true;
                                PptxFileManager.findHiddenFiles(
                                        hiddenSlideFiles, f);
                            }
                        }
                        catch (Exception e)
                        {
                            logException(e);
                        }

                        if (!isHidden)
                        {
                            list.add(f.getPath());
                        }
                    }
                }
            }
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
                    if (PptxFileManager.isSlideHiddenFile(hiddenSlideFiles, f))
                    {
                        continue;
                    }
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
                    if (PptxFileManager.isSlideHiddenFile(hiddenSlideFiles, f))
                    {
                        continue;
                    }
                    if (isFileContains(f, "</a:t>", false))
                    {
                        list.add(f.getPath());
                    }
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
                    if (PptxFileManager.isSlideHiddenFile(hiddenSlideFiles, f))
                    {
                        continue;
                    }
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

                        // ignore hidden slide's node : GBS-3576
                        if (PptxFileManager.isSlideHiddenFile(hiddenSlideFiles,
                                f))
                        {
                            continue;
                        }

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

        getUrlFilesForPptx(dir, list, hiddenSlideFiles);
        if (m_isCommentTranslate)
        {
            getCommentFilesForPptx(dir, list, hiddenSlideFiles);
        }
    }

    private void getNewLocalizedXmlFilesPPTX(String dir, List<String> list)
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
        File diagramData = new File(dir, "ppt/diagramData.xml");
        if (isFileContains(diagramData, "</a:t>", false))
        {
            list.add(diagramData.getPath());
        }

        // charts
        File chart = new File(dir, "ppt/chart.xml");
        if (isFileContains(chart, "</a:t>", false))
        {
            list.add(chart.getPath());
        }

        // get slides
        File slide = new File(dir, "ppt/slide.xml");
        if (slide.exists())
            list.add(slide.getAbsolutePath());

        File drawing = new File(dir, "ppt/drawing.xml");
        if (isFileContains(drawing, "</a:t>", false))
        {
            list.add(drawing.getPath());
        }

        if (m_isCommentTranslate)
        {
            File comment = new File(dir, "ppt/comment.xml");
            if (isFileContains(comment, "</p:text>", false))
            {
                list.add(comment.getPath());
            }
        }

        // get notes if needed
        if (m_isNotesTranslate)
        {
            File notesSlide = new File(dir, "ppt/notesSlide.xml");
            if (isFileContains(notesSlide, "</a:r>", false))
            {
                list.add(notesSlide.getPath());
            }
        }

        // get master pages if needed
        if (m_isMasterTranslate)
        {
            File notesMaster = new File(dir, "ppt/slideMaster.xml");
            if (isFileContains(notesMaster, "</a:r>", false))
            {
                list.add(notesMaster.getPath());
            }
        }

        // get slide layouts if needed
        if (m_isSlideLayoutTranslate)
        {
            File slideLayout = new File(dir, "ppt/slideLayout.xml");
            if (isFileContains(slideLayout, "</a:r>", false))
            {
                list.add(slideLayout.getPath());
            }

        }

        // get master pages if needed
        if (m_isNotesMasterTranslate)
        {
            File notesMaster = new File(dir, "ppt/notesMaster.xml");
            if (isFileContains(notesMaster, "</a:r>", false))
            {
                list.add(notesMaster.getPath());
            }

        }

        // get master pages if needed
        if (m_isHandoutMasterTranslate)
        {
            File handoutMaster = new File(dir, "ppt/handoutMaster.xml");
            if (isFileContains(handoutMaster, "</a:r>", false))
            {
                list.add(handoutMaster.getPath());
            }
        }

        getUrlFilesForPptx(dir, list,
                pptxFileManager != null ? pptxFileManager.getSlideHiddenFiles()
                        : null);
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
        if (!file.exists())
            return false;

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

    private File[] getSortSheetFiles(File sheetsDir)
    {
        File[] sheets = listAcceptFiles(sheetsDir, prefix_sort_sheet);
        return sheets;
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

        ExcelHiddenHandler handler = new ExcelHiddenHandler(dir,
                m_hideCellStyleIds);
        handler.run();

        m_hiddenSharedId = handler.getHiddenSharedString();
        m_hideCellMap = handler.getHideCellMap();
        m_hiddenSheetIds = handler.getHiddenSheetIds();
    }

    private String getExcelVText(Element ce)
    {
        Node vn = ce.getElementsByTagName("v").item(0);
        String vnid = vn.getFirstChild().getNodeValue();
        return vnid;
    }

    private NodeList getAffectedNodes(String xmlfile, String xpath)
            throws SAXException, IOException, TransformerException
    {
        DOMParser parser = new DOMParser();
        parser.parse(xmlfile);
        Document doc = parser.getDocument();
        Node node = doc.getDocumentElement();

        NodeList affectedNodes = XPathAPI.selectNodeList(node, xpath);
        return affectedNodes;
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
                .getValue("relSafeName"));
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
            path = StringUtil.replace(path, "\\", "/");
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
