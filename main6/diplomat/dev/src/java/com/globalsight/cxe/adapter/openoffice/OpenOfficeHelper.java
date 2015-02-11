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

package com.globalsight.cxe.adapter.openoffice;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.globalsight.cxe.engine.eventflow.Category;
import com.globalsight.cxe.engine.eventflow.DiplomatAttribute;
import com.globalsight.cxe.engine.eventflow.EventFlow;
import com.globalsight.cxe.engine.util.FileCopier;
import com.globalsight.cxe.engine.util.FileUtils;
import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.cxe.entity.filterconfiguration.FilterConstants;
import com.globalsight.cxe.entity.filterconfiguration.FilterHelper;
import com.globalsight.cxe.entity.filterconfiguration.OpenOfficeFilter;
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

public class OpenOfficeHelper
{
    private static final String CATEGORY_NAME = "OpenOfficeAdapter";

    private static final Logger logger = Logger.getLogger(OpenOfficeHelper.class);

    // Supported extensions for OpenOffice
    private static final String ODT = ".odt";

    private static final String ODS = ".ods";

    private static final String ODP = ".odp";

    private static final String STYLE_FAMILY_PARAGRAPH = "paragraph";
    private static final String STYLE_FAMILY_TEXT = "text";

    // The types of Adobe files
    private static final int OPENOFFICE_ODT = 0;

    private static final int OPENOFFICE_ODS = 1;

    private static final int OPENOFFICE_ODP = 2;

    private int m_type = OPENOFFICE_ODT;

    // "INDD", "INX", "AI" -- goes in the command file
    private String m_conversionType = null;

    // The content specific conversion directory
    private String m_convDir = null;

    // The locale specific save directory under the conversion dir.
    private String m_saveDir = null;

    private boolean m_isImport = true;

    private boolean m_isHeaderTranslate = false;

    private Properties m_ooProperties = null;

    private long m_currentTimeMillis = 0;

    private static SystemConfiguration m_sc = SystemConfiguration.getInstance();

    private CxeMessage m_cxeMessage;

    private EventFlow m_eventFlow;

    private static Hashtable<String, Integer> s_exportBatches = new Hashtable<String, Integer>();
    private static Object s_exportBatchesLocker = new Object();

    public static final String OPENDOC_XML = "od-xml";
    /**
     * Open Office file content
     */
    public static final String XML_CONTENT = "content.xml";
    /**
     * header footer for odt, master page for odp
     */
    public static final String XML_STYLES = "styles.xml";
    /**
     * Open Office file information
     */
    public static final String XML_META = "meta.xml";
    /**
     * Open Office file information prefix
     */
    public static final String OO_FILEINFO_DISPLAY_NAME_PREFIX = "(OpenOffice file information) ";
    /**
     * Open Office header/footer prefix
     */
    public static final String OO_HEADER_DISPLAY_NAME_PREFIX = "(OpenOffice header and footer) ";

    public OpenOfficeHelper(CxeMessage p_cxeMessage, Properties p_ooProperties)
    {
        m_cxeMessage = p_cxeMessage;
        m_eventFlow = new EventFlow(p_cxeMessage.getEventFlowXml());
        m_ooProperties = p_ooProperties;
    }

    /**
     * Perform conversion
     * 
     * @return conversion result
     * @throws OpenOfficeAdapterException
     */
    public CxeMessage[] performConversion() throws OpenOfficeAdapterException
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
            // 4 wait for Adobe Converter to convert
            String unParaStyles = getStyleNames(filename, STYLE_FAMILY_PARAGRAPH);
            String unCharStyles = getStyleNames(filename, STYLE_FAMILY_TEXT);
            MessageData[] messageData = readXmlOutput(filename);
            CxeMessage[] result = new CxeMessage[messageData.length];
            for (int i = 0; i < result.length; i++)
            {
                // 5 modify eventflowxml
                String basename = FileUtils.getBaseName(filename);
                String dirname = getUnzipDir(basename);
                String xmlfilename = dirname + File.separator + XML_CONTENT;
                if (i == 1)
                {
                    xmlfilename = dirname + File.separator + XML_STYLES;
                }
                modifyEventFlowXmlForImport(xmlfilename, i + 1, result.length, unParaStyles,
                        unCharStyles);
                // 7 return proper CxeMesseges
                CxeMessageType type = getPostConversionEvent();
                CxeMessage cxeMessage = new CxeMessage(type);
                cxeMessage.setParameters(m_cxeMessage.getParameters());
                cxeMessage.setMessageData(messageData[i]);

                String eventFlowXml = m_eventFlow.serializeToXml();
                cxeMessage.setEventFlowXml(eventFlowXml);

                result[i] = cxeMessage;
            }
            writeDebugFile(m_conversionType + "_" + getBaseFileName() + "_sa.xml", m_eventFlow
                    .serializeToXml());

            return result;
        }
        catch (OpenOfficeAdapterException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw wrapAdobeImportException(e, m_eventFlow.getDisplayName());
        }
    }

    /**
     * get style names for un-extract. (need to refine it to load XML once per
     * one import)
     * 
     * @param p_filepath
     * @param p_styleFamily
     * @return
     */
    private String getStyleNames(String p_filepath, String p_styleFamily)
    {
        OpenOfficeFilter oof = getMainFilter();

        if (oof == null)
            return "";

        String styleDisNames = null;
        String styleFamily = p_styleFamily;

        if (STYLE_FAMILY_PARAGRAPH.equals(p_styleFamily))
        {
            styleDisNames = oof.getUnextractableWordParagraphStyles();
        }
        if (STYLE_FAMILY_TEXT.equals(p_styleFamily))
        {
            styleDisNames = oof.getUnextractableWordCharacterStyles();

            if (m_type == OPENOFFICE_ODS)
            {
                styleFamily = "table-cell";
            }
        }

        List<String> styleDisplayNames = OpenOfficeFilter.toList(styleDisNames);

        if (styleDisplayNames == null || styleDisplayNames.isEmpty())
            return "";

        String dir = getUnzipDir(p_filepath);
        String stylesXml = dir + File.separator + XML_STYLES;
        String contentXml = dir + File.separator + XML_CONTENT;
        List<String> styleNames = new ArrayList<String>();

        try
        {
            DOMParser stylesParser = new DOMParser();
            stylesParser.parse(stylesXml);
            Document stylesDoc = stylesParser.getDocument();
            Node stylesNode = stylesDoc.getDocumentElement();

            DOMParser contentParser = new DOMParser();
            contentParser.parse(contentXml);
            Document contentDoc = contentParser.getDocument();
            Node contentNode = contentDoc.getDocumentElement();

            List<String> addedNames = new ArrayList<String>();

            for (String displayname : styleDisplayNames)
            {
                // display name, like customer name
                String xpath = "//*[local-name()=\"style\"][@style:family=\"" + styleFamily
                        + "\"][@style:display-name=\"" + displayname + "\"]";
                NodeList affectedNodes = XPathAPI.selectNodeList(stylesNode, xpath);

                if (affectedNodes != null && affectedNodes.getLength() > 0)
                {
                    Element node = (Element) affectedNodes.item(0);
                    String v1 = node.getAttribute("style:name");
                    if (v1 != null && !"".equals(v1))
                    {
                        styleNames = addValueIfNotExists(styleNames, v1);
                        addedNames.add(v1);
                    }
                }

                // style name, like Subtitle
                xpath = "//*[local-name()=\"style\"][@style:family=\"" + styleFamily
                        + "\"][@style:name=\"" + displayname + "\"]";

                NodeList affectedNodesName = XPathAPI.selectNodeList(stylesNode, xpath);
                if (affectedNodesName != null && affectedNodesName.getLength() > 0)
                {
                    styleNames = addValueIfNotExists(styleNames, displayname);
                    addedNames.add(displayname);
                }

                // get sub style from content.xml
                for (String addedName : addedNames)
                {
                    xpath = "//*[local-name()=\"style\"][@style:family=\"" + styleFamily
                            + "\"][@style:parent-style-name=\"" + addedName + "\"]";

                    NodeList affectedNodesInContent = XPathAPI.selectNodeList(contentNode, xpath);
                    if (affectedNodesInContent != null && affectedNodesInContent.getLength() > 0)
                    {
                        int len = affectedNodesInContent.getLength();
                        for (int i = 0; i < len; i++)
                        {
                            Element node = (Element) affectedNodesInContent.item(i);
                            String v1 = node.getAttribute("style:name");
                            if (v1 != null && !"".equals(v1))
                            {
                                styleNames = addValueIfNotExists(styleNames, v1);
                            }
                        }
                    }
                }

                // add Standard for Default
                if ("Default".equals(displayname))
                    styleNames = addValueIfNotExists(styleNames, "Standard");

                addedNames.clear();
            }
        }
        catch (Exception e)
        {
            // ignore, just log it
            logger.error("Exception occurs when reading styles.xml : " + stylesXml, e);
        }

        return styleNames.isEmpty() ? "" : OpenOfficeFilter.toString(styleNames);
    }

    private List<String> addValueIfNotExists(List<String> styleNames, String val)
    {
        if (!styleNames.contains(val))
            styleNames.add(val);

        return styleNames;
    }

    public CxeMessage[] performConversionBack() throws OpenOfficeAdapterException
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
                String oofilename = getCategory().getDiplomatAttribute("safeBaseFileName")
                        .getValue();
                String oofile = FileUtils.concatPath(m_saveDir, oofilename);
                modifyEventFlowXmlForExport();
                convert(oofile);
                MessageData fmd = readConvOutput(oofile);

                CxeMessage outputMsg = new CxeMessage(CxeMessageType.getCxeMessageType(m_eventFlow
                        .getPostMergeEvent()));
                outputMsg.setMessageData(fmd);
                outputMsg.setParameters(params);

                String eventFlowXml = m_eventFlow.serializeToXml();
                writeDebugFile(m_conversionType + "_" + getBaseFileName() + "_ea.xml", eventFlowXml);
                outputMsg.setEventFlowXml(eventFlowXml);

                return new CxeMessage[] { outputMsg };
            }
            else
            {
                // Since it was not the last page in the batch, don't
                // reconstruct the file.
                if (logger.isDebugEnabled())
                {
                    logger.debug("Skipping reconstruction for file: " + saveFileName);
                }
                long lastMod = new File(saveFileName).lastModified();

                CxeMessageType type = CxeMessageType
                        .getCxeMessageType(CxeMessageType.CXE_EXPORT_STATUS_EVENT);
                CxeMessage outputMsg = new CxeMessage(type);
                outputMsg.setEventFlowXml(m_eventFlow.serializeToXml());
                params.put("Exception", null);
                params.put("ExportedTime", new Long(lastMod));
                outputMsg.setParameters(params);

                return new CxeMessage[] { outputMsg };
            }
        }
        catch (Exception e)
        {
            throw wrapAdobeExportException(e, m_eventFlow.getDisplayName());
        }
    }

    private void setBasicParams() throws OpenOfficeAdapterException
    {
        try
        {
            setType();
            setConversionDir();
            setSaveDirectory();
            m_currentTimeMillis = System.currentTimeMillis();

            OpenOfficeFilter f = getMainFilter();
            m_isHeaderTranslate = (f == null) ? false : f.isHeaderTranslate();
        }
        catch (Exception e)
        {
            logger.error("Unable to set basic parameters. ", e);
            throw new OpenOfficeAdapterException("Unexpected", null, e);
        }
    }

    /**
     * Sets the internal type (ODT, ODS, ODP, etc.)
     */
    private void setType()
    {
        String name = m_eventFlow.getDisplayName().toLowerCase();

        if (name.endsWith(ODT))
        {
            m_type = OPENOFFICE_ODT;
            m_conversionType = "odt";
        }
        else if (name.endsWith(ODS))
        {
            m_type = OPENOFFICE_ODS;
            m_conversionType = "ods";
        }
        else if (name.endsWith(ODP))
        {
            m_type = OPENOFFICE_ODP;
            m_conversionType = "odp";
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
        saveDir.append(m_isImport ? m_eventFlow.getSourceLocale() : m_eventFlow.getTargetLocale());
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
        OpenOfficeConverter ooc = new OpenOfficeConverter();
        if (m_isImport)
        {
            ooc.convertOdToXml(p_filepath, dirName);

            replcaeSpace(dirName);
        }
        else
        {
            String filename = getCategory().getDiplomatAttribute("safeBaseFileName").getValue();
            ooc.convertXmlToOd(filename, dirName);
        }
    }

    /**
     * replace all <text:s/> with " " in content xml and styles xml
     * 
     * @param dirName
     * @throws Exception
     */
    private void replcaeSpace(String dirName) throws Exception
    {
        File conXml = new File(dirName, XML_CONTENT);
        File stylesXml = new File(dirName, XML_STYLES);

        replcaeFileSpace(conXml);
        if (m_isHeaderTranslate && m_type == OPENOFFICE_ODT)
            replcaeFileSpace(stylesXml);
    }

    private void replcaeFileSpace(File xmlfile) throws Exception
    {
        String xmlData = FileUtils.read(xmlfile, "UTF-8");
        String tag = "<text:s/>";
        String tag_0 = "<text:s text:c=";
        int tag_0_len = tag_0.length();
        String space = " ";

        if (xmlData.contains(tag) || xmlData.contains(tag_0))
        {
            String tmp = xmlData;
            if (xmlData.contains(tag))
            {
                tmp = xmlData.replace(tag, space);
            }

            if (tmp.contains(tag_0))
            {
                StringBuffer tmpBuffer = new StringBuffer(tmp);
                int in_0 = tmpBuffer.lastIndexOf(tag_0);

                while (in_0 != -1)
                {
                    int in_1 = tmpBuffer.indexOf(">", in_0);
                    char mark_0 = tmpBuffer.charAt(in_0 + tag_0_len);
                    int in_mark_1 = tmpBuffer.indexOf(mark_0 + "", in_0 + tag_0_len + 1);
                    String ccount = tmpBuffer.substring(in_0 + tag_0_len + 1, in_mark_1);
                    int spaceCount = 1;
                    try
                    {
                        spaceCount = Integer.parseInt(ccount);
                    }
                    catch (Exception e)
                    {
                        // do nothing
                    }

                    StringBuffer spaces = new StringBuffer();
                    for (int i = 0; i < spaceCount; i++)
                    {
                        spaces.append(space);
                    }

                    tmpBuffer.replace(in_0, in_1 + 1, spaces.toString());

                    in_0 = tmpBuffer.lastIndexOf(tag_0);
                }

                tmp = tmpBuffer.toString();
            }

            FileUtils.write(xmlfile, tmp, "UTF-8");
        }
    }

    private String getUnzipDir(String p_filepath)
    {
        String dirName = p_filepath + "." + m_type;
        return dirName;
    }

    private OpenOfficeFilter getMainFilter()
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
            fileProfile = ServerProxy.getFileProfilePersistenceManager().readFileProfile(fpId);
            long filterId = fileProfile.getFilterId();
            String filterTableName = fileProfile.getFilterTableName();

            if (filterId > 0 && FilterConstants.OPENOFFICE_TABLENAME.equals(filterTableName))
            {
                OpenOfficeFilter f = (OpenOfficeFilter) FilterHelper.getFilter(filterTableName,
                        filterId);
                return f;
            }
            else
            {
                return null;
            }
        }
        catch (Exception e)
        {
            logger.error("Can not get Filter", e);
            return null;
        }
    }

    private String getBaseFileName()
    {
        String dName = m_eventFlow.getDisplayName();
        if (dName.startsWith(OO_HEADER_DISPLAY_NAME_PREFIX))
        {
            dName = dName.substring(OO_HEADER_DISPLAY_NAME_PREFIX.length());
        }
        else if (dName.startsWith(OO_FILEINFO_DISPLAY_NAME_PREFIX))
        {
            dName = dName.substring(OO_FILEINFO_DISPLAY_NAME_PREFIX.length());
        }

        return FileUtils.getBaseName(dName);
    }

    public CxeMessage getCxeMessage()
    {
        return m_cxeMessage;
    }

    private CxeMessageType getPostConversionEvent()
    {
        return CxeMessageType.getCxeMessageType(CxeMessageType.XML_IMPORTED_EVENT);
    }

    public String getPostMergeEvent()
    {
        return CxeMessageType.getCxeMessageType(CxeMessageType.OPENOFFICE_LOCALIZED_EVENT)
                .getName();
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

    protected void modifyEventFlowXmlForImport(String p_xmlFilename, int p_docPageNum,
            int p_docPageCount, String unParaStyles, String unCharStyles) throws Exception
    {
        if (unParaStyles == null || unParaStyles.length() == 0)
        {
            unParaStyles = ",";
        }
        if (unCharStyles == null || unCharStyles.length() == 0)
        {
            unCharStyles = ",";
        }

        // First get original Category
        Category oriC = getCategory();
        if (oriC != null)
        {
            Category newC = new Category(CATEGORY_NAME, new DiplomatAttribute[] {
                    oriC.getDiplomatAttribute("postMergeEvent"),
                    oriC.getDiplomatAttribute("formatType"),
                    oriC.getDiplomatAttribute("safeBaseFileName"),
                    oriC.getDiplomatAttribute("originalFileSize"),
                    new DiplomatAttribute("unParaStyles", unParaStyles),
                    new DiplomatAttribute("unCharStyles", unCharStyles),
                    new DiplomatAttribute("relSafeName", p_xmlFilename) });

            m_eventFlow.removeCategory(oriC);
            m_eventFlow.addCategory(newC);
        }
        else
        {
            Category newC = new Category(CATEGORY_NAME, new DiplomatAttribute[] {
                    new DiplomatAttribute("postMergeEvent", m_eventFlow.getPostMergeEvent()),
                    new DiplomatAttribute("formatType", m_eventFlow.getSourceFormatType()),
                    new DiplomatAttribute("safeBaseFileName", getSafeBaseFileName()),
                    new DiplomatAttribute("originalFileSize", String.valueOf(m_cxeMessage
                            .getMessageData().getSize())),
                    new DiplomatAttribute("unParaStyles", unParaStyles),
                    new DiplomatAttribute("unCharStyles", unCharStyles),
                    new DiplomatAttribute("relSafeName", p_xmlFilename) });
            m_eventFlow.addCategory(newC);
        }
        // Then modify eventFlow
        m_eventFlow.setPostMergeEvent(getPostMergeEvent());
        // m_eventFlow.setSourceFormatType("xml");

        m_eventFlow.setDocPageCount(p_docPageCount);
        m_eventFlow.setDocPageNumber(p_docPageNum);

        if (m_isHeaderTranslate && p_docPageNum == 2)
        {
            m_eventFlow
                    .setDisplayName(OO_HEADER_DISPLAY_NAME_PREFIX + m_eventFlow.getDisplayName());
        }
    }

    protected MessageData readConvOutput(String fileName) throws OpenOfficeAdapterException
    {
        try
        {
            String oofile = FileUtils.getPrefix(fileName) + "." + m_conversionType;
            FileMessageData fmd = MessageDataFactory.createFileMessageData();
            fmd.copyFrom(new File(oofile));
            return fmd;
        }
        catch (Exception e)
        {
            logger.error("Read adobe file failed", e);
            throw wrapAdobeExportException(e, e.getMessage());
        }
    }

    protected MessageData[] readXmlOutput(String p_filepath) throws OpenOfficeAdapterException
    {
        try
        {
            String dir = getUnzipDir(p_filepath);
            String contentXml = dir + File.separator + XML_CONTENT;
            // String metaXml = dir + File.separator + XML_META;
            String stylesXml = dir + File.separator + XML_STYLES;

            String oofile = FileUtils.getPrefix(p_filepath) + "." + m_conversionType;
            FileMessageData fmd = (FileMessageData) MessageDataFactory
                    .createFileMessageData(OPENDOC_XML);
            fmd.copyFrom(new File(contentXml));
            FileMessageData fmd_header = null;
            if (m_isHeaderTranslate && m_type == OPENOFFICE_ODT)
            {
                // use rdf:RDF as root element to extract
                fmd_header = (FileMessageData) MessageDataFactory
                        .createFileMessageData(OPENDOC_XML);
                fmd_header.copyFrom(new File(stylesXml));
            }

            File dirFile = new File(dir);
            copyToTargetLocales(FileUtil.getAllFiles(dirFile));
            copyToTargetLocales(new String[] { oofile });

            if (fmd_header == null)
            {
                return new MessageData[] { fmd };
            }
            else
            {
                return new MessageData[] { fmd, fmd_header };
            }
        }
        catch (Exception e)
        {
            logger.error("Failed to read xml output:", e);
            throw wrapAdobeImportException(e, e.getMessage());
        }
    }

    private String writeContentToConvInbox() throws OpenOfficeAdapterException
    {
        try
        {
            String fileName = FileUtils.concatPath(m_saveDir, getSafeBaseFileName());
            if (logger.isInfoEnabled())
            {
                logger.info("Converting: " + m_eventFlow.getDisplayName() + ", size: "
                        + m_cxeMessage.getMessageData().getSize() + ", tmp file: " + fileName);
            }

            FileMessageData fmd = (FileMessageData) m_cxeMessage.getMessageData();
            fmd.copyTo(new File(fileName));

            return fileName;
        }
        catch (Exception e)
        {
            logger.error("Failed to write adobe to inbox. ", e);
            String[] errorArgs = { m_eventFlow.getDisplayName() };
            throw new OpenOfficeAdapterException("Import", errorArgs, e);
        }
    }

    private String writeContentToXmlBox() throws IOException
    {
        String saveFileName = FileUtils.concatPath(m_saveDir, getCategory().getDiplomatAttribute(
                "relSafeName").getValue());
        File saveFile = new File(saveFileName);

        m_cxeMessage.getMessageData().copyTo(saveFile);

        return saveFileName;
    }

    private static OpenOfficeAdapterException wrapAdobeExportException(Exception e, String arg)
    {
        return new OpenOfficeAdapterException("Export", new String[] { arg }, e);
    }

    private static OpenOfficeAdapterException wrapAdobeImportException(Exception e, String arg)
    {
        return new OpenOfficeAdapterException("Import", new String[] { arg }, e);
    }

    private void writeDebugFile(String fileName, String content)
    {
        String debugFileDirectory = m_ooProperties.getProperty("DebugFileDirectory");
        if (debugFileDirectory != null)
        {
            try
            {
                FileUtils.write(new File(debugFileDirectory, fileName), content, "UTF-8");
            }
            catch (Exception e)
            {
                if (logger.isEnabledFor(Priority.WARN))
                {
                    logger.warn("Fail to write content to file: " + fileName, e);
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
            StringBuffer targetDir = new StringBuffer(expectedFile.getParent());
            int srcIndex = targetDir.lastIndexOf(srcLocale);
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
        StringBuffer sql = new StringBuffer("select loc.iso_lang_code, loc.iso_country_code ");
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

    private static boolean isExportFileComplete(String p_filekey, int p_pageCount)
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
        convDir.append(m_sc.getStringParameter(SystemConfigParamNames.FILE_STORAGE_DIR,
                CompanyWrapper.SUPER_COMPANY_ID));
        convDir.append(File.separator);
        convDir.append("OpenOffice-Conv");

        return convDir.toString();
    }

    /**
     * Fix content xml for open office files
     * 
     * @param target
     * @param source
     * @param sourceLocale
     * @param targetLocale
     * @param relSafeName
     * @return
     * @throws Exception
     */
    public static String fixContentXmlForOds(String target, String source, String sourceLocale,
            String targetLocale, String relSafeName) throws Exception
    {
        String tableName = "<table:table table:name=\"";
        String attEnd = "\"";

        StringBuffer result = new StringBuffer(target);
        StringBuffer targetBuff = new StringBuffer(target);
        StringBuffer sourceBuff = new StringBuffer(source);
        StringIndex tableNameInTarget = getValueBetween(targetBuff, 0, tableName, attEnd);
        StringIndex tableNameInSource = getValueBetween(sourceBuff, 0, tableName, attEnd);
        boolean isFirstTime = true;

        // continue if find sheet name in both source content and target content
        while (tableNameInTarget != null && tableNameInSource != null)
        {
            boolean isNameSame = tableNameInTarget.value.equals(tableNameInSource.value);
            if (!isNameSame)
            {
                result = replaceSheetNameFromBack(tableNameInSource.value, tableNameInTarget.value,
                        result);
                replaceSheetNameForObjects(sourceLocale, targetLocale, relSafeName,
                        tableNameInSource.value, tableNameInTarget.value, isFirstTime);
                isFirstTime = false;
            }

            tableNameInTarget = getValueBetween(targetBuff, tableNameInTarget.end, tableName,
                    attEnd);
            tableNameInSource = getValueBetween(sourceBuff, tableNameInSource.end, tableName,
                    attEnd);
        }

        return result.toString();
    }

    /**
     * Use new sheet name in such file: Object 1\content.xml under target locale
     * 
     * @param sourceLocale
     * @param targetLocale
     * @param relSafeName
     * @param oldName
     * @param newName
     * @param isFirstTime
     *            read content.xml from source locale at the first time
     * @throws Exception
     */
    private static void replaceSheetNameForObjects(String sourceLocale, String targetLocale,
            String relSafeName, String oldName, String newName, boolean isFirstTime)
            throws Exception
    {
        String srcXmlPath = getConversionDir() + File.separator + sourceLocale + File.separator
                + relSafeName;
        File sourceDir = (new File(srcXmlPath)).getParentFile();
        String tgtXmlPath = getConversionDir() + File.separator + targetLocale + File.separator
                + relSafeName;
        File tgtDir = (new File(tgtXmlPath)).getParentFile();

        // get object directories
        File[] objectDirs = sourceDir.listFiles(new FileFilter()
        {
            public boolean accept(File pathname)
            {
                if (pathname.isDirectory() && pathname.toString().contains("Object "))
                {
                    return true;
                }

                return false;
            }
        });

        if (objectDirs != null && objectDirs.length > 0)
        {
            for (int i = 0; i < objectDirs.length; i++)
            {
                File objDir = objectDirs[i];
                File objContent = new File(objDir, "content.xml");
                File objContentTgt = new File(tgtDir, objDir.getName() + File.separator
                        + "content.xml");
                if (objContent.exists() && objContentTgt.exists())
                {
                    File dataFile = isFirstTime ? objContent : objContentTgt;
                    String contentdata = FileUtils.read(dataFile, "UTF-8");
                    StringBuffer result = replaceSheetNameFromBack(oldName, newName,
                            new StringBuffer(contentdata));
                    FileUtils.write(objContentTgt, result.toString(), "UTF-8");
                }
            }
        }
    }

    private static String getValidSheetName(String name)
    {
        return "&apos;" + name + "&apos;";
    }

    private static StringIndex getValueBetween(StringBuffer src, int s, String start, String end)
    {
        int index_s = src.indexOf(start, s);
        if (index_s != -1)
        {
            int index_e = src.indexOf(end, index_s + start.length());

            if (index_e != -1)
            {
                int st = index_s + start.length();
                return new StringIndex(src.substring(st, index_e), st, index_e);
            }
        }

        return null;
    }

    private static StringBuffer replaceSheetNameFromBack(String oldName, String newName,
            StringBuffer ori)
    {
        String s = "=\"", e = "\"";
        StringBuffer newSb = new StringBuffer(ori.toString());
        String newOri = ori.toString();
        int index_s = newOri.lastIndexOf(s);
        while (index_s != -1)
        {
            int index_e = newOri.indexOf(e, index_s + s.length());
            if (index_e != -1)
            {
                String value = newOri.substring(index_s, index_e);
                // logger.info(value);
                String sheetName = getValidSheetName(oldName) + ".";
                String sheetName2 = "'" + oldName + "'.";
                if (value.contains(oldName + ".") || value.contains(sheetName)
                        || value.contains(sheetName2))
                {
                    String newSheetName = getValidSheetName(newName) + ".";
                    String newV = value.replace(oldName + ".", newSheetName);
                    newV = newV.replace(sheetName, newSheetName);
                    newV = newV.replace(sheetName2, newSheetName);

                    newSb.replace(index_s, index_e, newV);
                }
            }

            newOri = newOri.substring(0, index_s);
            index_s = newOri.lastIndexOf(s);
        }

        return newSb;
    }

    private static class StringIndex
    {
        public String value;
        public int start;
        public int end;

        public StringIndex(String v, int s, int e)
        {
            value = v;
            end = e;
            start = s;
        }
    }
}