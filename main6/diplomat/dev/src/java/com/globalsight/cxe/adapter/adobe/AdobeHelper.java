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

package com.globalsight.cxe.adapter.adobe;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

import com.globalsight.cxe.adapter.idml.IdmlHelper;
import com.globalsight.cxe.engine.util.FileCopier;
import com.globalsight.cxe.engine.util.FileUtils;
import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.cxe.entity.filterconfiguration.FilterConstants;
import com.globalsight.cxe.entity.filterconfiguration.FilterHelper;
import com.globalsight.cxe.entity.filterconfiguration.InddFilter;
import com.globalsight.cxe.message.CxeMessage;
import com.globalsight.cxe.message.CxeMessageType;
import com.globalsight.cxe.message.FileMessageData;
import com.globalsight.cxe.message.MessageData;
import com.globalsight.cxe.message.MessageDataFactory;
import com.globalsight.cxe.util.fileImport.eventFlow.Category;
import com.globalsight.cxe.util.fileImport.eventFlow.EventFlowXml;
import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.diplomat.util.database.ConnectionPoolException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.FileUtil;
import com.globalsight.util.Replacer;
import com.globalsight.util.StringUtil;
import com.globalsight.util.file.FileWaiter;

public class AdobeHelper
{
    private static final String CATEGORY_NAME = "AdobeAdapter";

    private static final Logger logger = Logger.getLogger(AdobeHelper.class);

    private static final String MARK_LF_IDML = IdmlHelper.MARK_LF_IDML;
    private static final String LINE_BREAK = IdmlHelper.LINE_BREAK;

    // Supported extensions for Adobe
    private static final String INDD = ".indd";

    private static final String INX = ".inx";

    private static final String AI = ".ai";

    // The types of Adobe files
    private static final int ADOBE_INDD = 0;

    private static final int ADOBE_AI = 1;

    private static final int ADOBE_INX = 2;

    private int m_type = ADOBE_INDD;

    // "INDD", "INX", "AI" -- goes in the command file
    private String m_conversionType = null;

    // The content specific conversion directory
    private String m_convDir = null;

    // The locale specific save directory under the conversion dir.
    private String m_saveDir = null;

    private boolean m_isImport = true;

    private boolean m_isAdobeXmpTranslate = false;

    private Properties m_adobeProperties = null;

    private static final String STATUS_FILE_SUFFIX = ".status";

    private static final String XML_FILE_SUFFIX = ".xml";

    private static final String XMP_FILE_SUFFIX = ".xmp";

    private static final String PDF_FILE_SUFFIX = ".pdf";

    private long m_currentTimeMillis = 0;

    private static SystemConfiguration m_sc = SystemConfiguration.getInstance();

    private CxeMessage m_cxeMessage;

    private EventFlowXml m_eventFlow;

    static private Hashtable s_exportBatches = new Hashtable();

    public static final String XMP_DISPLAY_NAME_PREFIX = "(Adobe file information) ";
    
    // [contents-contents-contents]xxxxxx[/contents-contents-contents]LINE_BREAK
    private static Pattern LINK_PATTERN = Pattern
            .compile("\\[([^/-]+-[^-]+-[^\\]]+)\\]([^\\[]*)\\[/(\\1)\\]"
                    + LINE_BREAK);
    
    // LINE_BREAK[contents-contents-contents]xxxxxx[/contents-contents-contents]
    private static Pattern LINK_BREAK_PATTERN = Pattern.compile(LINE_BREAK
            + "\\[([^/-]+-[^-]+-[^\\]]+)\\]([^\\[]*)\\[/(\\1)\\]");
    
    // [contents-contents-contents]xxxxxx[/contents-contents-contents]
    private static Pattern P1 = Pattern
            .compile("\\[([^/-]+-[^-]+-[^\\]]+)\\]([^\\[]*)\\[/(\\1)\\]");

    private void checkIsInstalled() throws AdobeAdapterException
    {
        if ((m_type == ADOBE_INDD || m_type == ADOBE_INX)
                && !AdobeAdapter.isInddInstalled())
        {
            throw new AdobeAdapterException("InddNotInstalled", null, null);
        }

        if (m_type == ADOBE_AI && !AdobeAdapter.isIllustratorInstalled())
        {
            throw new AdobeAdapterException("IllustratorNotInstalled", null,
                    null);
        }
    }

    public AdobeHelper(CxeMessage p_cxeMessage, Properties p_adobeProperties)
    {
        m_cxeMessage = p_cxeMessage;
        m_eventFlow = p_cxeMessage.getEventFlowObject();
        m_adobeProperties = p_adobeProperties;
    }

    /*
     * Add Illustrator XMP support remove (m_type == ADOBE_INDD) in
     * setBasicParams() and remove (m_type == ADOBE_INDD) in
     * readXmlOutput(String fileName)
     */
    public CxeMessage[] performConversion() throws AdobeAdapterException
    {
        m_isImport = true;
        String filename = null;
        try
        {
            // 1 set basic parameters such as file type and conversion dir.
            setBasicParams();
            // 2 check if the converter is installed.
            checkIsInstalled();
            // 3 write to temp files
            filename = writeContentToAdobeInbox();
            // 4 write command file(newly added)
            writeCommandFile(filename);

            // 5 wait for Adobe Converter to convert
            MessageData[] messageData = readXmlOutput(filename);
            CxeMessage[] result = new CxeMessage[messageData.length];
            for (int i = 0; i < result.length; i++)
            {
                // 6 modify eventflowxml
                EventFlowXml newEventFlowXml = m_eventFlow.clone();
                String suffix = XML_FILE_SUFFIX;
                if (i == 1)
                {
                    suffix = XMP_FILE_SUFFIX;
                }
                modifyEventFlowXmlForImport(
                        FileUtils.getPrefix(FileUtils.getBaseName(filename))
                                + suffix, i + 1, result.length, newEventFlowXml);
                // 7 return proper CxeMesseges
                CxeMessageType type = getPostConversionEvent();
                CxeMessage cxeMessage = new CxeMessage(type);
                cxeMessage.setParameters(m_cxeMessage.getParameters());
                cxeMessage.setMessageData(messageData[i]);

                cxeMessage.setEventFlowObject(newEventFlowXml);

                result[i] = cxeMessage;
            }

            return result;
        }
        catch (AdobeAdapterException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw wrapAdobeImportException(e, m_eventFlow.getDisplayName());
        }
    }

    private void setBasicParams() throws AdobeAdapterException
    {
        try
        {
            setType();
            setConversionDir();
            setSaveDirectory();
            m_currentTimeMillis = System.currentTimeMillis();

            if ((m_type == ADOBE_INDD) || (m_type == ADOBE_INX))
            {
                boolean isInddHiddenTranslate = false;
                boolean isMasterTranslate = true;
                if (m_isImport)
                {
                    InddFilter f = getMainFilter();
                    m_isAdobeXmpTranslate = (f == null) ? false : f
                            .getTranslateFileInfo();
                    isInddHiddenTranslate = (f == null) ? false : f
                            .getTranslateHiddenLayer();
                    isMasterTranslate = (f == null) ? true : f
                            .getTranslateMasterLayer();
                }
                else
                {
                    String xmpTranslated = m_eventFlow.getBatchInfo().getAdobeXmpTranslated();
                    if (xmpTranslated == null
                            || xmpTranslated.trim().equals(""))
                    {
                        xmpTranslated = m_sc
                                .getStringParameter(SystemConfigParamNames.ADOBE_XMP_TRANSLATE);
                    }

                    String hiddenTranslated = m_eventFlow.getBatchInfo()
                            .getInddHiddenTranslated();
                    if (hiddenTranslated == null
                            || hiddenTranslated.trim().equals(""))
                    {
                        hiddenTranslated = "true";
                    }

                    m_isAdobeXmpTranslate = "true".equals(xmpTranslated);
                    isInddHiddenTranslate = "true".equals(hiddenTranslated);
                    isMasterTranslate = "true".equals(m_eventFlow.getBatchInfo()
                            .getMasterTranslated());
                }

                m_eventFlow.getBatchInfo().setAdobeXmpTranslated(String
                        .valueOf(m_isAdobeXmpTranslate));
                m_eventFlow.getBatchInfo().setInddHiddenTranslated(String
                        .valueOf(isInddHiddenTranslate));
                m_eventFlow.getBatchInfo().setMasterTranslated(String
                        .valueOf(isMasterTranslate));
            }
        }
        catch (Exception e)
        {
            logger.error("Unable to set basic parameters. ", e);
            throw new AdobeAdapterException("Unexpected", null, e);
        }
    }

    /**
     * Sets the internal type (ADOBE_INDD, ADOBE_AI, etc.)
     */
    private void setType()
    {
        String name = m_eventFlow.getDisplayName().toLowerCase();

        if (name.endsWith(INDD))
        {
            m_type = ADOBE_INDD;
            m_conversionType = "indd";
        }
        else if (name.endsWith(INX))
        {
            m_type = ADOBE_INX;
            m_conversionType = "inx";
        }
        else if (name.endsWith(AI))
        {
            m_type = ADOBE_AI;
            m_conversionType = "ai";
        }
    }

    /**
     * Determines and sets the content specific conversion directory, for
     * example: D:\WINFILES\Illustrator
     */
    private void setConversionDir() throws Exception
    {
        StringBuffer convDir = null;
        if (m_type == ADOBE_INDD)
        {
            String sourceFormat = m_isImport ? m_eventFlow.getSource()
                    .getFormatType() : m_eventFlow.getValue("formatType");
            String IndesignConverterDir = "";
            if ("indd_cs5.5".equals(sourceFormat))
            {
                IndesignConverterDir = m_sc
                        .getStringParameter(SystemConfigParamNames.ADOBE_CONV_DIR_CS5_5);
            }
            else if ("indd_cs5".equals(sourceFormat))
            {
                IndesignConverterDir = m_sc
                        .getStringParameter(SystemConfigParamNames.ADOBE_CONV_DIR_CS5);
            }
            else if ("indd_cs4".equals(sourceFormat))
            {
                IndesignConverterDir = m_sc
                        .getStringParameter(SystemConfigParamNames.ADOBE_CONV_DIR_CS4);
            }
            else
            {
                IndesignConverterDir = sourceFormat.equals("indd") ? m_sc
                        .getStringParameter(SystemConfigParamNames.ADOBE_CONV_DIR)
                        : m_sc.getStringParameter(SystemConfigParamNames.ADOBE_CONV_DIR_CS3);
            }

            convDir = new StringBuffer(IndesignConverterDir);
            convDir.append(File.separator);
            convDir.append("indd");

        }
        else if (m_type == ADOBE_INX)
        {
            String sourceFormat = m_isImport ? m_eventFlow.getSource()
                    .getFormatType() : m_eventFlow.getValue("formatType");
            String IndesignConverterDir = sourceFormat.equals("inx") ? m_sc
                    .getStringParameter(SystemConfigParamNames.ADOBE_CONV_DIR)
                    : m_sc.getStringParameter(SystemConfigParamNames.ADOBE_CONV_DIR_CS3);

            convDir = new StringBuffer(IndesignConverterDir);
            convDir.append(File.separator);
            convDir.append("inx");
        }
        else if (m_type == ADOBE_AI)
        {
            convDir = new StringBuffer(
                    m_sc.getStringParameter(SystemConfigParamNames.ADOBE_CONV_DIR));
            convDir.append(File.separator);
            convDir.append("Illustrator");
        }

        m_convDir = convDir.toString();
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

    public CxeMessage[] performConversionBack() throws AdobeAdapterException
    {
        m_isImport = false;
        try
        {
            setBasicParams();
            String saveFileName = writeContentToXmlBox();

            HashMap params = m_cxeMessage.getParameters();

            String exportBatchId = m_eventFlow.getBatchInfo().getBatchId();
            String fileName = getBaseFileName();
            String targetLocale = m_eventFlow.getTargetLocale();
            int docPageCount = m_eventFlow.getBatchInfo().getDocPageCount();

            String key = exportBatchId + fileName + targetLocale;

            if (isExportFileComplete(key, docPageCount))
            {
                modifyEventFlowXmlForExport();
                // GBS-2955
                if ((m_type == ADOBE_INDD) || (m_type == ADOBE_INX))
                {
                    handleXmlContent(saveFileName);
                }
                writeCommandFile(saveFileName);
                MessageData fmd = readAdobeOutput(saveFileName);

                CxeMessage outputMsg = new CxeMessage(
                        CxeMessageType.getCxeMessageType(m_eventFlow
                                .getPostMergeEvent()));
                outputMsg.setMessageData(fmd);
                outputMsg.setParameters(params);
                outputMsg.setEventFlowObject(m_eventFlow.clone());

                return new CxeMessage[]
                { outputMsg };
            }
            else
            {
                // Since it was not the last page in the batch, don't
                // reconstruct the file.
                if (logger.isDebugEnabled())
                {
                    logger.debug("Skipping reconstruction for file: "
                            + saveFileName);
                }
                long lastMod = new File(saveFileName).lastModified();

                CxeMessageType type = CxeMessageType
                        .getCxeMessageType(CxeMessageType.CXE_EXPORT_STATUS_EVENT);
                CxeMessage outputMsg = new CxeMessage(type);
                outputMsg.setEventFlowObject(m_eventFlow.clone());
                params.put("Exception", null);
                params.put("ExportedTime", new Long(lastMod));
                outputMsg.setParameters(params);

                return new CxeMessage[]
                { outputMsg };
            }
        }
        catch (Exception e)
        {
            throw wrapAdobeExportException(e, m_eventFlow.getDisplayName());
        }
    }

    /**
     * Actually writes out the command file. The format of the command file is:
     * ConvertFrom=indd | ai ConvertTo=indd | ai AcceptChanges=true
     */
    public void writeCommandFile(String p_commandFileName) throws Exception
    {
        String commandFileName = getCommandFileName(p_commandFileName);
        String convertFrom = "ConvertFrom=";
        String convertTo = "ConvertTo=";
        String acceptChanges = "AcceptChanges=true"; // always true here

        if (m_isImport)
        {
            convertFrom += m_conversionType;
            convertTo += "xml";
        }
        else
        {
            convertFrom += "xml";
            convertTo += m_conversionType;
        }

        StringBuffer text = new StringBuffer();
        text.append(convertFrom).append("\r\n");
        text.append(convertTo).append("\r\n");
        text.append(acceptChanges).append("\r\n");
        if ((m_type == ADOBE_INDD) || (m_type == ADOBE_INX))
        {
            // MasterTranslated
            String masterTranslated = "MasterTranslated="
                    + m_eventFlow.getBatchInfo().getMasterTranslated();
            text.append(masterTranslated).append("\r\n");

            // TranslateHiddenLayer
            String transHiddenLayer = "TranslateHiddenLayer="
                    + m_eventFlow.getBatchInfo().getInddHiddenTranslated();
            text.append(transHiddenLayer).append("\r\n");
        }

        FileUtil.writeFileAtomically(new File(commandFileName),
                text.toString(), "US-ASCII");
    }

    private InddFilter getMainFilter()
    {
        try
        {
            String fpIdstr = m_eventFlow.getSource().getDataSourceId();
            long fpId = Long.parseLong(fpIdstr);
            FileProfile fileProfile = null;
            fileProfile = ServerProxy.getFileProfilePersistenceManager()
                    .readFileProfile(fpId);
            long filterId = fileProfile.getFilterId();
            String filterTableName = fileProfile.getFilterTableName();

            if (filterId > 0
                    && FilterConstants.INDD_TABLENAME.equals(filterTableName))
            {
                InddFilter f = (InddFilter) FilterHelper.getFilter(
                        filterTableName, filterId);
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

    /**
     * Gets the command file name.
     * 
     * @param filename
     *            the file name
     * @return the command file name.
     */
    private String getCommandFileName(String filename)
    {
        String commandSuffixName = m_isImport ? "im_command" : "ex_command";
        return FileUtils.getPrefix(filename) + "." + commandSuffixName;
    }

    private String getBaseFileName()
    {
        String dName = m_eventFlow.getDisplayName();
        if (m_isAdobeXmpTranslate && dName.startsWith(XMP_DISPLAY_NAME_PREFIX))
        {
            dName = dName.substring(XMP_DISPLAY_NAME_PREFIX.length());
        }
        return FileUtils.getBaseName(dName);
    }

    public CxeMessage getCxeMessage()
    {
        return m_cxeMessage;
    }

    // private String getDisplayName()
    // {
    // return m_eventFlow.getDisplayName();
    // }

    protected long getMaxWaitTime()
    {
        try
        {
            String maxWaitTime = m_adobeProperties
                    .getProperty(AdobeConfiguration.MAX_TIME_TO_WAIT);
            return Long.parseLong(maxWaitTime) * AdobeConfiguration.MINUTE;
        }
        catch (Exception e)
        {
            if (logger.isEnabledFor(Priority.WARN))
            {
                logger.warn("Cannot set maxWaitTime, using default value "
                        + AdobeConfiguration.DEFAULT_MAX_WAIT_TIME
                        / AdobeConfiguration.MINUTE + " minutes", e);
            }
            return AdobeConfiguration.DEFAULT_MAX_WAIT_TIME;
        }
    }

    private CxeMessageType getPostConversionEvent()
    {
        return CxeMessageType
                .getCxeMessageType(CxeMessageType.XML_IMPORTED_EVENT);
    }

    public String getPostMergeEvent()
    {
        return CxeMessageType.getCxeMessageType(
                CxeMessageType.ADOBE_LOCALIZED_EVENT).getName();
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

    /**
     * @deprecated use modifyEventFlowXmlForImport(String, int, int)
     * @param p_xmlFilename
     * @throws Exception
     */
    protected void modifyEventFlowXmlForImport(String p_xmlFilename)
            throws Exception
    {
        // First save original value to Category.
        Category c = new Category();
        c.setName(CATEGORY_NAME);
        c.addValue("postMergeEvent", m_eventFlow
                .getPostMergeEvent());
        c.addValue("formatType", m_eventFlow.getSource().getFormatType());
        c.addValue("safeBaseFileName",
                getSafeBaseFileName());
        c.addValue("originalFileSize", String
                .valueOf(m_cxeMessage.getMessageData()
                        .getSize()));
        c.addValue("relSafeName", p_xmlFilename);
        m_eventFlow.getCategory().add(c);
        
        // Then modify eventFlow
        m_eventFlow.setPostMergeEvent(getPostMergeEvent());
        m_eventFlow.getSource().setFormatType("xml");
    }

    protected void modifyEventFlowXmlForImport(String p_xmlFilename,
            int p_docPageNum, int p_docPageCount, EventFlowXml newEventFlowXm) throws Exception
    {
        String postMergeEvent;
        String formatType;
        String safeBaseFileName;
        String originalFileSize;

        // First get original Category
        Category oriC = getCategory();
        if (oriC != null)
        {
            postMergeEvent = oriC.getValue("postMergeEvent");
            formatType = oriC.getValue("formatType");
            safeBaseFileName = oriC.getValue("safeBaseFileName");
            originalFileSize = oriC.getValue("originalFileSize");
            newEventFlowXm.getCategory().remove(oriC);
        }
        else
        {
            postMergeEvent = newEventFlowXm.getPostMergeEvent();
            formatType = newEventFlowXm.getSource().getFormatType();
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
        newC.addValue("relSafeName", p_xmlFilename);
        newEventFlowXm.getCategory().add(newC);
        
        // Then modify eventFlow
        newEventFlowXm.setPostMergeEvent(getPostMergeEvent());
        newEventFlowXm.getSource().setFormatType("xml");

        newEventFlowXm.getBatchInfo().setDocPageCount(p_docPageCount);
        newEventFlowXm.getBatchInfo().setDocPageNumber(p_docPageNum);

        if (m_isAdobeXmpTranslate && p_docPageNum == 2)
        {
            newEventFlowXm.getBatchInfo().setDisplayName(XMP_DISPLAY_NAME_PREFIX
                    + newEventFlowXm.getDisplayName());
        }
    }

    protected MessageData readAdobeOutput(String fileName)
            throws AdobeAdapterException
    {
        String statusFileName = FileUtils.getPrefix(fileName)
                + STATUS_FILE_SUFFIX;
        String status = null;
        try
        {
            FileWaiter waiter = new FileWaiter(AdobeConfiguration.SLEEP_TIME,
                    getMaxWaitTime(), statusFileName);
            waiter.waitForFile();

            status = statusInfo(new File(statusFileName));
            if (status != null)
            {
                throw new Exception(status);
            }
            else
            {
                String adobeFileName = FileUtils.getPrefix(fileName) + "."
                        + m_conversionType;
                FileMessageData fmd = MessageDataFactory
                        .createFileMessageData();
                fmd.copyFrom(new File(adobeFileName));
                return fmd;
            }
        }
        catch (Exception e)
        {
            logger.error("Read adobe file failed", e);
            throw wrapAdobeExportException(e, status);
        }
        finally
        {
            FileUtils.deleteSilently(statusFileName);
        }
    }

    protected MessageData[] readXmlOutput(String fileName)
            throws AdobeAdapterException
    {
        String statusFileName = FileUtils.getPrefix(fileName)
                + STATUS_FILE_SUFFIX;
        File statusFile = new File(statusFileName);
        String status = null;
        try
        {
            FileWaiter fileWaiter = new FileWaiter(
                    AdobeConfiguration.SLEEP_TIME, getMaxWaitTime(),
                    statusFileName);
            fileWaiter.waitForFile();

            status = statusInfo(statusFile);
            if (status != null)
            {
                throw new Exception(status);
            }
            else
            {
                String xmlFileName = FileUtils.getPrefix(fileName)
                        + XML_FILE_SUFFIX;
                // GBS-2955
                if ((m_type == ADOBE_INDD) || (m_type == ADOBE_INX))
                {
                    handleXmlContent(xmlFileName);
                }

                String xmpFileName = FileUtils.getPrefix(fileName)
                        + XMP_FILE_SUFFIX;
                String adobeFileName = FileUtils.getPrefix(fileName) + "."
                        + m_conversionType;
                FileMessageData fmd = (FileMessageData) MessageDataFactory
                        .createFileMessageData();
                fmd.copyFrom(new File(xmlFileName));
                FileMessageData fmd_xmp = null;
                if (m_isAdobeXmpTranslate)
                {
                    // remove x:xmpmeta tag
                    String xmpRdfFileName = FileUtils.getPrefix(fileName)
                            + "_RDF.xml";
                    File xmpRdfFile = new File(xmpRdfFileName);
                    xmpRdfFile.createNewFile();
                    String xmpFileData = FileUtils.read(new File(xmpFileName));
                    FileUtils.write(xmpRdfFile, readRdfData(xmpFileData));
                    // use rdf:RDF as root element to extract
                    fmd_xmp = (FileMessageData) MessageDataFactory
                            .createFileMessageData();
                    fmd_xmp.copyFrom(xmpRdfFile);
                    // delete temp rdf file
                    FileUtils.deleteSilently(xmpRdfFileName);
                }

                if ((m_type == ADOBE_INDD) || (m_type == ADOBE_INX))
                {
                    String adobePdfName = FileUtils.getPrefix(fileName)
                            + PDF_FILE_SUFFIX;
                    copyToTargetLocales(new String[]
                    { xmpFileName, adobePdfName });
                    // FileUtils.deleteSilently(xmpFileName);
                    // For Adobe INDD preview
                    FileMessageData fmd_pdf = (FileMessageData) MessageDataFactory
                            .createFileMessageData();
                    fmd_pdf.copyFrom(new File(adobePdfName));

                    String srcDisplayName = m_eventFlow.getDisplayName();
                    String srcPageName = AmbFileStoragePathUtils
                            .getPdfPreviewDir().getAbsolutePath()
                            + File.separator
                            + srcDisplayName.substring(0,
                                    srcDisplayName.lastIndexOf("."))
                            + PDF_FILE_SUFFIX;
                    File srcPage = new File(srcPageName.substring(0,
                            srcPageName.lastIndexOf(File.separator)));
                    srcPage.mkdirs();
                    fmd_pdf.copyTo(new File(srcPageName));
                    writeStatusToTargetLocales(adobePdfName);
                }
                copyToTargetLocales(new String[]
                { adobeFileName });

                if (fmd_xmp == null)
                {
                    return new MessageData[]
                    { fmd };
                }
                else
                {
                    return new MessageData[]
                    { fmd, fmd_xmp };
                }
            }
        }
        catch (Exception e)
        {
            logger.error("Failed to read xml output:", e);
            throw wrapAdobeImportException(e, status);
        }
        finally
        {
            FileUtils.deleteSilently(statusFileName);
        }
    }

    /**
     * Specially handle xml content before/after going to converter.
     */
    private void handleXmlContent(String xmlFileName) throws Exception
    {
        File xmlFile = new File(xmlFileName);
        String xmlContent = FileUtil.readFile(xmlFile, "utf-8");
        xmlContent = optimizeForOddChar(xmlContent);
        FileUtil.writeFile(xmlFile, xmlContent, "utf-8");
    }

    /**
     * Optimizes the odd characters in the converted xml.
     */
    private String optimizeForOddChar(String content)
    {
        if (m_isImport)
        {
            if (extractLineBreak())
            {
                content = convertLineBreakToTag(content);
            }
            else
            {
                content = removeLineBreak(content);
            }
        }
        else
        {
            if (extractLineBreak())
            {
                content = recoverLineBreak(content);
            }
        }

        return content;
    }

    /**
     * Recovers the line break characters to their original positions.
     */
    public static String recoverLineBreak(String content)
    {
        content = StringUtil.replaceWithRE(content, LINK_PATTERN, new Replacer() 
        {
			@Override
			public String getReplaceString(Matcher m) 
			{
	            String group2 = m.group(2);
	            StringBuilder sb = new StringBuilder();
	            sb.append("[");
	            sb.append(m.group(1));
	            sb.append("]");
	            sb.append(group2);
	            sb.append(LINE_BREAK);
	            sb.append("[/");
	            sb.append(m.group(3));
	            sb.append("]");
	            
				return sb.toString();
			}
		});

        content = StringUtil.replaceWithRE(content, LINK_BREAK_PATTERN, new Replacer() 
        {
			@Override
			public String getReplaceString(Matcher m) 
			{
		         String group2 = m.group(2);
		         StringBuilder sb = new StringBuilder();
		         sb.append("[");
		         sb.append(m.group(1));
		         sb.append("]");
		         sb.append(LINE_BREAK);
		         sb.append(group2);
		         sb.append("[/");
		         sb.append(m.group(3));
		         sb.append("]");
	            
				return sb.toString();
			}
		});

        return content;
    }

    /**
     * Converts the line break character to tag used for segment separation.
     */
    private String convertLineBreakToTag(String content)
    {
    	content = StringUtil.replaceWithRE(content, P1, new Replacer() 
        {
			@Override
			public String getReplaceString(Matcher m) 
			{
				String group2 = m.group(2);
	            // 1. move LINE_BREAK in front of the start tag
	            if (group2.trim().startsWith(LINE_BREAK))
	            {
	                StringBuilder sb = new StringBuilder();
	                sb.append(MARK_LF_IDML);
	                sb.append("[");
	                sb.append(m.group(1));
	                sb.append("]");
	                sb.append(removeLineBreak(group2));
	                sb.append("[/");
	                sb.append(m.group(3));
	                sb.append("]");
	                
	                return sb.toString();
	            }
	            // 2. move LINE_BREAK after the end tag
	            else if (group2.trim().endsWith(LINE_BREAK))
	            {
	                StringBuilder sb = new StringBuilder();
	                sb.append("[");
	                sb.append(m.group(1));
	                sb.append("]");
	                sb.append(removeLineBreak(group2));
	                sb.append("[/");
	                sb.append(m.group(3));
	                sb.append("]");
	                sb.append(MARK_LF_IDML);
	                
	                return sb.toString();
	            }
	            // 3. LINE_BREAK is in the middle of the tag text
	            else if (group2.contains(LINE_BREAK))
	            {
	                int index = group2.indexOf(LINE_BREAK);
	                String before = group2.substring(0, index);
	                String after = group2.substring(index + LINE_BREAK.length());

	                StringBuilder sb = new StringBuilder();
	                sb.append("[");
	                sb.append(m.group(1));
	                sb.append("]");
	                sb.append(before);
	                sb.append("[/");
	                sb.append(m.group(3));
	                sb.append("]");
	                sb.append(MARK_LF_IDML);
	                sb.append("[");
	                sb.append(m.group(1));
	                sb.append("]");
	                sb.append(after);
	                sb.append("[/");
	                sb.append(m.group(3));
	                sb.append("]");
	                
	                return sb.toString();
	            }
	            
				return m.group();
			}
		});
    	
        return StringUtil.replace(content, LINE_BREAK, MARK_LF_IDML);
    }

    private String removeLineBreak(String content)
    {
        return StringUtil.replace(content, LINE_BREAK, "");
    }

    private boolean extractLineBreak()
    {
        InddFilter f = getMainFilter();
        return f == null ? true : f.getExtractLineBreak();
    }

    private String writeContentToAdobeInbox() throws AdobeAdapterException
    {
        try
        {
            String fileName = FileUtils.concatPath(m_saveDir,
                    getSafeBaseFileName());
            if (logger.isInfoEnabled())
            {
                logger.info("Converting: " + m_eventFlow.getDisplayName()
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
            logger.error("Failed to write adobe to inbox. ", e);
            String[] errorArgs =
            { m_eventFlow.getDisplayName() };
            throw new AdobeAdapterException("Import", errorArgs, e);
        }
    }

    private String writeContentToXmlBox() throws IOException
    {
        String saveFileName = FileUtils.concatPath(m_saveDir, getCategory().getValue("relSafeName"));
        File saveFile = new File(saveFileName);

        m_cxeMessage.getMessageData().copyTo(saveFile);

        // Indesign can import the xmp file like
        // <rdf:RDF ... >
        // ...
        // </rdf:RDF>
        // So, I do not wrap the rdf file which extracted
        // by back to
        // <?xpacket begin= ... ?>
        // <x:xmpmeta ... >
        // <rdf:RDF ... >
        // ...
        // If needed, remove the comment sign "//" below,
        // and toggle comment the sentence above
        // "m_cxeMessage.getMessageData().copyTo(saveFile);"

        // if (saveFileName.endsWith(XMP_FILE_SUFFIX))
        // {
        // String saveRdfFileName = FileUtils.getPrefix(saveFileName) +
        // "_RDF.xml";
        // File saveRdfFile = new File(saveRdfFileName);
        // saveRdfFile.createNewFile();
        // m_cxeMessage.getMessageData().copyTo(saveRdfFile);
        //
        // String xmpRdfData = FileUtils.read(saveRdfFile);
        // String oriXmpData = FileUtils.read(saveFile);
        //
        // FileUtils.write(saveFile, addXmpmetaTag(oriXmpData, xmpRdfData),
        // "UTF-8");
        // FileUtils.deleteSilently(saveRdfFileName);
        // }
        // else
        // {
        // m_cxeMessage.getMessageData().copyTo(saveFile);
        // }

        return saveFileName;
    }

    private static String statusInfo(File p_file)
    {
        BufferedReader br = null;
        String errorLine = null;
        try
        {
            br = new BufferedReader(new FileReader(p_file));
            errorLine = br.readLine();
            errorLine = errorLine.substring(6); // error=
            int error = Integer.parseInt(errorLine);

            return error == 0 ? null : errorLine;
        }
        catch (NumberFormatException nfe)
        {
            return errorLine;
        }
        catch (Exception e)
        {
            if (logger.isEnabledFor(Priority.WARN))
                logger.warn("Cannot read status info", e);
            return e.getMessage();
        }
        finally
        {
            FileUtils.closeSilently(br);
        }
    }

    private static AdobeAdapterException wrapAdobeExportException(Exception e,
            String arg)
    {
        return new AdobeAdapterException("Export", new String[]
        { arg }, e);
    }

    private static AdobeAdapterException wrapAdobeImportException(Exception e,
            String arg)
    {
        return new AdobeAdapterException("Import", new String[]
        { arg }, e);
    }

    private void writeDebugFile(String fileName, String content)
    {
        String debugFileDirectory = m_adobeProperties
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
                if (logger.isEnabledFor(Priority.WARN))
                {
                    logger.warn("Fail to write content to file: " + fileName, e);
                }
            }
        }
    }

    private List copyToTargetLocales(String[] fileName)
    {
        List fileList = new ArrayList();
        for (int i = 0; i < fileName.length; i++)
        {
            File expectedFile = new File(fileName[i]);
            if (!expectedFile.exists())
            {
                throw new RuntimeException("conversion failed: " + fileName);
            }
            fileList.add(expectedFile);
            doCopyToTargetLocales(expectedFile);
        }
        return fileList;
    }

    /**
     * Writes status file with converted file name to target locales folder.
     * 
     */
    private void writeStatusToTargetLocales(String p_extectedFile)
            throws IOException
    {
        String l10nProfileId = m_eventFlow.getBatchInfo().getL10NProfileId();
        Set<String> targetLocales = findTargetLocales(l10nProfileId);
        String splitChar = File.separator;
        if ("\\".equalsIgnoreCase(splitChar))
        {
            splitChar = "\\\\";
        }
        String[] srcDisplayName = m_eventFlow.getDisplayName().split(splitChar);
        for (String locale : targetLocales)
        {
            StringBuffer tarDir = new StringBuffer(AmbFileStoragePathUtils
                    .getPdfPreviewDir().getAbsolutePath());
            tarDir.append(File.separator).append(locale);
            for (int j = 1; j < srcDisplayName.length - 1; j++)
            {
                tarDir.append(File.separator);
                tarDir.append(srcDisplayName[j]);
            }
            File tarDirFile = new File(tarDir.toString());
            String p_expectedFileName = p_extectedFile.substring(p_extectedFile
                    .lastIndexOf(File.separator) + 1);
            String inddName = srcDisplayName[srcDisplayName.length - 1];
            String pdfName = inddName.substring(0, inddName.lastIndexOf("."))
                    + PDF_FILE_SUFFIX;
            tarDirFile.mkdirs();
            // write status file for recording the file name which was converted
            FileWriter commandFile = new FileWriter(tarDir.toString()
                    + File.separator + FileUtils.getPrefix(pdfName)
                    + STATUS_FILE_SUFFIX);
            commandFile.write("convertedFileName=" + p_expectedFileName
                    + "\r\n");
            commandFile.close();
        }
    }

    private void doCopyToTargetLocales(File expectedFile)
    {
        String[] targetLocales = this.m_eventFlow.getTargetLocale().split(",");
        for (int i = 0; i < targetLocales.length; i++)
        {
            String locale = (String) targetLocales[i];
            StringBuffer targetDir = new StringBuffer(m_convDir);
            targetDir.append(File.separator).append(locale);

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

        synchronized (s_exportBatches)
        {
            Integer oldPageCount = (Integer) s_exportBatches.get(p_filekey);
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

    private String readRdfData(String p_xmpFileData)
    {
        int index_1 = p_xmpFileData.indexOf("<rdf:RDF");
        String rdf = "</rdf:RDF>";
        int index_2 = p_xmpFileData.indexOf(rdf);

        return (p_xmpFileData.substring(index_1, index_2 + rdf.length()));
    }

    private String addXmpmetaTag(String p_xmpFileData, String p_xmpRdfFileData)
    {
        try
        {
            int index_1 = p_xmpFileData.indexOf("<rdf:RDF");
            String rdf = "</rdf:RDF>";
            int index_2 = p_xmpFileData.indexOf(rdf);

            String p = p_xmpFileData.substring(0, index_1);
            String s = p_xmpFileData.substring(index_2 + rdf.length());
            String rdfData = readRdfData(p_xmpRdfFileData);

            String id = "id=";
            int index_id = p.indexOf(id);
            // UTF-8 encoding the begin attribute
            byte[] bUTF8 = new byte[]
            { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };
            String sUTF8 = new String(bUTF8, "UTF-8");
            String beforeId = "<?xpacket begin=\"" + sUTF8 + "\" ";
            String afterId = p.substring(index_id);

            StringBuffer sb = new StringBuffer().append(beforeId)
                    .append(afterId).append(rdfData).append(s);

            return sb.toString();
        }
        catch (Exception e)
        {
            return p_xmpRdfFileData;
        }
    }

    private Set<String> findTargetLocales(String p_l10nProfileId)
    {
        Set<String> targetLocales = new HashSet<String>();

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
                targetLocales.add(locale);
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
}
