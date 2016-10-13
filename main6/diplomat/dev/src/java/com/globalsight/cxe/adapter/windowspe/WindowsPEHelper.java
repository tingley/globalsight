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

package com.globalsight.cxe.adapter.windowspe;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

import com.globalsight.cxe.adapter.adobe.AdobeAdapterException;
import com.globalsight.cxe.adapter.adobe.AdobeConfiguration;
import com.globalsight.cxe.engine.util.FileCopier;
import com.globalsight.cxe.engine.util.FileUtils;
import com.globalsight.cxe.message.CxeMessage;
import com.globalsight.cxe.message.CxeMessageType;
import com.globalsight.cxe.message.FileMessageData;
import com.globalsight.cxe.message.MessageData;
import com.globalsight.cxe.message.MessageDataFactory;
import com.globalsight.cxe.util.fileImport.eventFlow.Category;
import com.globalsight.cxe.util.fileImport.eventFlow.EventFlowXml;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.util.FileUtil;
import com.globalsight.util.file.FileWaiter;

public class WindowsPEHelper
{
    private static final String CATEGORY_NAME = "WindowsPEAdapter";

    private static final Logger logger = Logger.getLogger(WindowsPEHelper.class);

    // The content specific conversion directory
    private String m_convDir = null;

    // The locale specific save directory under the conversion dir.
    private String m_saveDir = null;

    private boolean m_isImport = true;

    private long m_currentTimeMillis = 0;

    private static final String STATUS_FILE_SUFFIX = ".status";

    private CxeMessage m_cxeMessage;

    private EventFlowXml m_eventFlow;

    public static final int WINDOWSPE_EXE = 0;

    public static final int WINDOWSPE_DLL = 1;

    private static final String XML_FILE_SUFFIX = ".xml";

    private int m_type = WINDOWSPE_EXE;

    // "EXE", "DLL" -- goes in the command file
    private String m_conversionType = null;

    private String displayName = null;

    private Properties m_properties = null;

    private static Vector<String> PROCESS_FILES = new Vector<String>();
    private WindowsPEConfiguration m_config = WindowsPEConfiguration.getInstance();

    public WindowsPEHelper(CxeMessage p_cxeMessage)
    {
        m_cxeMessage = p_cxeMessage;
        m_eventFlow = p_cxeMessage.getEventFlowObject();
        m_properties = m_config.loadProperties();
    }

    /**
     * Just for junit test
     * 
     * @deprecated
     */
    public WindowsPEHelper()
    {

    }

    /**
     * Perform conversion
     * 
     * @return conversion result
     * @throws WindowsPEAdapterException
     */
    public CxeMessage[] performConversion() throws WindowsPEAdapterException
    {
        m_isImport = true;
        String filename = null;
        try
        {
            // 1 set basic parameters such as file type and conversion dir.
            setBasicParams();
            // 2 write file to conv dir
            filename = writeContentToConvInbox();
            // 3 write command file
            writeCommandFile(filename);
            // 4 wait for Converter to convert
            MessageData[] messageData = readXmlOutput(filename);

            CxeMessage[] result = new CxeMessage[messageData.length];

            int i = 0;
            for (MessageData mdata : messageData)
            {
                EventFlowXml newEventFlowXml = m_eventFlow.clone();
                String suffix = XML_FILE_SUFFIX;
                modifyEventFlowXmlForImport(FileUtils.getPrefix(FileUtils.getBaseName(filename))
                        + suffix, i + 1, result.length, newEventFlowXml);
                // 6 return proper CxeMesseges
                CxeMessageType type = getPostConversionEvent();
                CxeMessage cxeMessage = new CxeMessage(type);
                cxeMessage.setParameters(m_cxeMessage.getParameters());
                cxeMessage.setMessageData(mdata);

                cxeMessage.setEventFlowObject(newEventFlowXml);

                result[i] = cxeMessage;

                i++;
            }

            return result;
        }
        catch (WindowsPEAdapterException e)
        {
            logger.error(e.getMessage(), e);
            throw e;
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw wrapImportException(e, e.getMessage());
        }
    }

    public CxeMessage[] performConversionBack() throws WindowsPEAdapterException
    {
        m_isImport = false;
        try
        {
            setBasicParams();
            writeContent();
            HashMap params = m_cxeMessage.getParameters();

            String oofilename = getCategory().getValue("safeBaseFileName");
            String oofile = FileUtils.concatPath(m_saveDir, oofilename);

            modifyEventFlowXmlForExport(oofile);

            convertBack(oofile);

            MessageData fmd = readConvOutput(oofile);

            CxeMessage outputMsg = new CxeMessage(CxeMessageType.getCxeMessageType(m_eventFlow
                    .getPostMergeEvent()));
            outputMsg.setMessageData(fmd);
            outputMsg.setParameters(params);

            outputMsg.setEventFlowObject(m_eventFlow.clone());

            return new CxeMessage[] { outputMsg };
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw wrapExportException(e, e.getMessage());
        }
    }

    /**
     * Writes out the command file to invoke the appropriate MS Office
     * converter, and waits until the conversion has completed.
     */
    private void convertBack(String safeBaseFileName) throws Exception
    {
        while (PROCESS_FILES.contains(safeBaseFileName))
        {
            Thread.sleep(4000);
        }

        PROCESS_FILES.add(safeBaseFileName);
        String commandFileName = null;
        String statusFilePath = null;
        try
        {
            // First create the command file.
            String prefixFilePath = FileUtils.getPrefix(safeBaseFileName);
            StringBuffer commandFileNameBuffer = new StringBuffer(prefixFilePath);
            commandFileNameBuffer.append(".ex_command");
            commandFileName = commandFileNameBuffer.toString();
            writeCommandFile(commandFileName);

            // Now wait for status file.
            StringBuffer statusFileName = new StringBuffer(prefixFilePath);
            statusFileName.append(".status");
            long maxTimeToWait = getMaxTimeToWaitExport();
            statusFilePath = statusFileName.toString();
            FileWaiter waiter = new FileWaiter(2000L, maxTimeToWait, statusFilePath);
            waiter.waitForFile();

            // Conversion is done, but check the status to see if there is an
            // error.
            File statusFile = new File(statusFilePath);
            String status = statusInfo(statusFile);
            if (status != null)
            {
                throw new Exception(status);
            }
        }
        finally
        {
            FileUtils.deleteSilently(commandFileName);
            FileUtils.deleteSilently(statusFilePath);

            PROCESS_FILES.remove(safeBaseFileName);
        }
    }

    private long getMaxTimeToWaitExport()
    {
        String maxTTW = m_properties.getProperty(WindowsPEConfiguration.MAX_TIME_TO_WAIT_EXPORT);
        long maxTimeToWait = (long) (Long.parseLong(maxTTW)) * 60 * 1000;
        return maxTimeToWait;
    }

    private long getMaxTimeToWaitImport()
    {
        String maxTTW = m_properties.getProperty(WindowsPEConfiguration.MAX_TIME_TO_WAIT_IMPORT);
        long maxTimeToWait = (long) (Long.parseLong(maxTTW)) * 60 * 1000;
        return maxTimeToWait;
    }

    private void setBasicParams() throws WindowsPEAdapterException
    {
        try
        {
            setType();
            setConversionDir();
            setSaveDirectory();
            m_currentTimeMillis = System.currentTimeMillis();
        }
        catch (Exception e)
        {
            logger.error("Unable to set basic parameters. ", e);
            throw new WindowsPEAdapterException("Unexpected", null, e);
        }
    }

    private void setType()
    {
        String name = m_eventFlow.getDisplayName().toLowerCase();

        if (name.endsWith(".exe"))
        {
            m_type = WINDOWSPE_EXE;
            m_conversionType = "exe";
        }
        else if (name.endsWith(".dll"))
        {
            m_type = WINDOWSPE_DLL;
            m_conversionType = "dll";
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

    private void writeCommandFile(String filepath) throws Exception
    {
        String commandFileName = null;

        String prefixFilePath = FileUtils.getPrefix(filepath);
        StringBuffer commandFileNameBuffer = new StringBuffer(prefixFilePath);
        commandFileNameBuffer.append(".im_command");
        commandFileName = commandFileNameBuffer.toString();

        String convertFrom = "ConvertFrom=";
        String convertTo = "ConvertTo=";

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

        FileUtil.writeFileAtomically(new File(commandFileName), text.toString(), "US-ASCII");
    }

    private String getBaseFileName()
    {
        String dName = m_eventFlow.getDisplayName();
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
        return CxeMessageType.getCxeMessageType(CxeMessageType.WINPE_LOCALIZED_EVENT).getName();
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

    private void modifyEventFlowXmlForExport(String name)
    {
        m_eventFlow.setPostMergeEvent(getCategory().getPostMergeEvent());
    }

    protected void modifyEventFlowXmlForImport(String p_xmlFilename, int p_docPageNum,
            int p_docPageCount, EventFlowXml newEventFlowXml) throws Exception
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
        newC.addValue("relSafeName", p_xmlFilename);
        
        newEventFlowXml.getCategory().add(newC);
        
        // Then modify eventFlow
        newEventFlowXml.setPostMergeEvent(getPostMergeEvent());
        newEventFlowXml.getSource().setFormatType("xml");

        newEventFlowXml.getBatchInfo().setDocPageCount(p_docPageCount);
        newEventFlowXml.getBatchInfo().setDocPageNumber(p_docPageNum);
    }

    protected MessageData readConvOutput(String fileName) throws WindowsPEAdapterException
    {
        try
        {
            FileMessageData fmd = MessageDataFactory.createFileMessageData();
            fmd.copyFrom(new File(fileName));
            return fmd;
        }
        catch (Exception e)
        {
            logger.error("Read windows pe file failed", e);
            throw wrapExportException(e, e.getMessage());
        }
    }

    protected MessageData[] readXmlOutput(String fileName) throws AdobeAdapterException
    {
        String statusFileName = FileUtils.getPrefix(fileName) + STATUS_FILE_SUFFIX;
        File statusFile = new File(statusFileName);
        String status = null;
        try
        {
            FileWaiter fileWaiter = new FileWaiter(AdobeConfiguration.SLEEP_TIME,
                    getMaxTimeToWaitImport(), statusFileName);
            fileWaiter.waitForFile();

            status = statusInfo(statusFile);
            if (status != null)
            {
                throw new Exception(status);
            }
            else
            {
                String xmlFileName = FileUtils.getPrefix(fileName) + XML_FILE_SUFFIX;
                String targetFileName = FileUtils.getPrefix(fileName) + "." + m_conversionType;
                FileMessageData fmd = (FileMessageData) MessageDataFactory.createFileMessageData();
                fmd.copyFrom(new File(xmlFileName));

                copyToTargetLocales(new String[] { targetFileName });

                return new MessageData[] { fmd };
            }
        }
        catch (Exception e)
        {
            logger.error("Failed to read xml output:", e);
            throw wrapImportException(e, status);
        }
        finally
        {
            FileUtils.deleteSilently(statusFileName);
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

    private String writeContentToConvInbox() throws WindowsPEAdapterException
    {
        try
        {
            String fileName = FileUtils.concatPath(m_saveDir, getSafeBaseFileName());
            if (logger.isInfoEnabled())
            {
                logger.info("Converting: " + m_eventFlow.getDisplayName() + ", size: "
                        + m_cxeMessage.getMessageData().getSize() + ", tmp file: " + fileName);
            }

            // fileName = fileName.replace("\\", "/");

            FileMessageData fmd = (FileMessageData) m_cxeMessage.getMessageData();
            fmd.copyTo(new File(fileName));

            return fileName;
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            String[] errorArgs = { m_eventFlow.getDisplayName() };
            throw new WindowsPEAdapterException("Import", errorArgs, e);
        }
    }

    private String writeContent() throws IOException
    {
        String saveFileName = FileUtils.concatPath(m_saveDir,
                getCategory().getValue("relSafeName"));
        File saveFile = new File(saveFileName);
        File parent = saveFile.getParentFile();
        if (!parent.exists())
        {
            parent.mkdirs();
        }

        m_cxeMessage.getMessageData().copyTo(saveFile);

        return saveFileName;
    }

    private static WindowsPEAdapterException wrapExportException(Exception e, String arg)
    {
        return new WindowsPEAdapterException("Export", new String[] { arg }, e);
    }

    private static WindowsPEAdapterException wrapImportException(Exception e, String arg)
    {
        return new WindowsPEAdapterException("Import", new String[] { arg }, e);
    }

    public static String getConversionDir() throws Exception
    {
        String companyName = CompanyWrapper.getCurrentCompanyName();
        String winfiles = SystemConfiguration.getInstance().getStringParameter(
                SystemConfigParamNames.WINDOWS_PE_DIR);

        StringBuffer sb = new StringBuffer(winfiles);
        sb.append(File.separator);
        sb.append("winpe");
        sb.append(File.separator);
        sb.append(companyName);
        return sb.toString();
    }
}