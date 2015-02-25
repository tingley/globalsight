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

package com.globalsight.cxe.adapter.passolo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.globalsight.cxe.engine.util.FileUtils;
import com.globalsight.cxe.message.CxeMessage;
import com.globalsight.cxe.message.CxeMessageType;
import com.globalsight.cxe.message.FileMessageData;
import com.globalsight.cxe.message.MessageData;
import com.globalsight.cxe.message.MessageDataFactory;
import com.globalsight.cxe.util.fileImport.eventFlow.Category;
import com.globalsight.cxe.util.fileImport.eventFlow.EventFlowXml;
import com.globalsight.cxe.util.fileImport.eventFlow.ExportBatchInfo;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.ExportUtil;
import com.globalsight.util.FileUtil;
import com.globalsight.util.Replacer;
import com.globalsight.util.StringUtil;
import com.globalsight.util.file.FileWaiter;

public class PassoloHelper
{
    private static final String CATEGORY_NAME = "PassoloAdapter";

    private static final Logger logger = Logger.getLogger(PassoloHelper.class);

    // The content specific conversion directory
    private String m_convDir = null;

    // The locale specific save directory under the conversion dir.
    private String m_saveDir = null;

    private boolean m_isImport = true;

    private long m_currentTimeMillis = 0;

    private CxeMessage m_cxeMessage;

    private EventFlowXml m_eventFlow;

    private static Object s_exportBatchesLocker = new Object();

    private static Map<String, List<String>> INIT_FILES = new HashMap<String, List<String>>();

    private String displayName = null;

    private Properties m_passoloProperties = null;
    private PassoloConfiguration passoloConfig = PassoloConfiguration
            .getInstance();

    private static final String TU = "      <trans-unit id=\"{0}\">"
            + FileUtil.lineSeparator + "        <source>{1}</source>"
            + FileUtil.lineSeparator
            + "        <target xml:lang=\"{2}\">{3}</target>"
            + FileUtil.lineSeparator + "      </trans-unit>"
            + FileUtil.lineSeparator;

    private static Vector<String> PROCESS_FILES = new Vector<String>();
    private static Pattern P1 = Pattern.compile("([^\\\\])\\\\[\\d](.{2,2})");
    private static Pattern P2 = Pattern.compile("&#x([^;]{1,3});");

    public PassoloHelper(CxeMessage p_cxeMessage)
    {
        m_cxeMessage = p_cxeMessage;
        m_eventFlow = p_cxeMessage.getEventFlowObject();
        m_passoloProperties = passoloConfig.loadProperties();
    }

    /**
     * Just for junit test
     * 
     * @deprecated
     */
    public PassoloHelper()
    {

    }

    /**
     * Perform conversion
     * 
     * @return conversion result
     * @throws PassoloAdapterException
     */
    public CxeMessage[] performConversion() throws PassoloAdapterException
    {
        m_isImport = true;
        String filename = null;
        try
        {
            // 1 set basic parameters such as file type and conversion dir.
            setBasicParams();
            // 2 write file to conv dir
            filename = writeContentToConvInbox();
            // 3 ask converter to handle
            convert(filename);
            // 4 wait for Adobe Converter to convert
            Map<String, MessageData> messageData = readXliffOutput(filename);

            CxeMessage[] result = new CxeMessage[messageData.size()];

            int i = 0;
            for (String key : messageData.keySet())
            {
                EventFlowXml newEventFlowXml = m_eventFlow.clone();
                modifyEventFlowXmlForImport(filename, key, i + 1,
                        messageData.size(), newEventFlowXml);
                // 6 return proper CxeMesseges
                CxeMessageType type = getPostConversionEvent();
                CxeMessage cxeMessage = new CxeMessage(type);
                cxeMessage.setParameters(m_cxeMessage.getParameters());
                cxeMessage.setMessageData(messageData.get(key));

                cxeMessage.setEventFlowObject(newEventFlowXml);

                result[i] = cxeMessage;

                i++;
            }

            return result;
        }
        catch (PassoloAdapterException e)
        {
            logger.error(e.getMessage(), e);
            throw e;
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw wrapPassoloImportException(e, e.getMessage());
        }
    }

    private void copyLpuFile(String fileName) throws IOException
    {
        String docHome = SystemConfiguration.getInstance().getStringParameter(
                SystemConfigParamNames.CXE_DOCS_DIR);
        String displayName = m_eventFlow.getDisplayName();
        String temp = StringUtil.replace(displayName.toLowerCase(), "\\", "/");

        String sourceFile = fileName;
        if (temp.lastIndexOf((".lpu/")) > 0)
        {
            sourceFile = displayName.substring(0,
                    temp.lastIndexOf((".lpu/")) + 4);
        }

        File source = new File(docHome, sourceFile);
        File target = new File(m_convDir + "/" + "export" + "/" + fileName);

        if (!source.exists())
        {
            throw new IllegalArgumentException("File " + source.getPath()
                    + " is not exist");
        }

        if (!target.exists())
        {
            FileUtil.copyFile(source, target);
        }
    }

    private void initLpu(String fileName, String batchId) throws IOException
    {
        synchronized (INIT_FILES)
        {
            List<String> files = INIT_FILES.get(batchId);

            if (files == null)
            {
                files = new ArrayList<String>();
                INIT_FILES.put(batchId, files);
            }

            if (!files.contains(fileName))
            {
                copyLpuFile(fileName);
                files.add(fileName);
            }
        }
    }

    public CxeMessage[] performConversionBack() throws PassoloAdapterException
    {
        ExportBatchInfo info = m_eventFlow.getExportBatchInfo();
        String exportBatchId = info.getExportBatchId();

        m_isImport = false;
        try
        {
            setBasicParams();
            String fileName = getCategory().getValue("safeBaseFileName");
            initLpu(fileName, exportBatchId);
            String saveFileName = writeContent();
            HashMap params = m_cxeMessage.getParameters();

            String name = (String) params.get("TargetFileName");
            if (name != null)
            {
                String tempFileName = StringUtil.replace(saveFileName, "\\", "/");
                int index = tempFileName.indexOf(fileName);
                String suffex = tempFileName.substring(index);
                int index2 = suffex.indexOf("/", 1);
                name = name.substring(0,
                        name.length() - suffex.substring(index2).length());
                params.put("TargetFileName", name);
            }

            String baseHref = m_eventFlow.getBatchInfo().getBaseHref();
            String displayName = m_eventFlow.getDisplayName();
            String key = PassoloUtil.getKey(baseHref, displayName,
                    Long.parseLong(exportBatchId));

            String eBatchId = (String) params.get("ExportBatchId");
            String tFileName = (String) params.get("TargetFileName");
            if (ExportUtil.isLastFile(eBatchId, tFileName, "all"))
            {
                String oofilename = fileName;
                String oofile = FileUtils.concatPath(m_saveDir, oofilename);

                modifyEventFlowXmlForExport(oofile);

                decodingAllXliffForPassolo(oofile);
                boolean hasXLiffs = writeMergedContent(oofile);
                if (hasXLiffs)
                {
                    convertBack(oofile);
                }

                MessageData fmd = readConvOutput(oofile);

                CxeMessage outputMsg = new CxeMessage(
                        CxeMessageType.getCxeMessageType(m_eventFlow
                                .getPostMergeEvent()));
                outputMsg.setMessageData(fmd);
                outputMsg.setParameters(params);
                outputMsg.setEventFlowObject(m_eventFlow.clone());

                INIT_FILES.get(exportBatchId).remove(fileName);
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
            logger.error(e.getMessage(), e);
            throw wrapPassoloExportException(e, e.getMessage());
        }
    }

    private void decodingAllXliffForPassolo(String safeBaseFileName)
            throws Exception
    {
        List<File> fs = FileUtil.getAllFiles(new File(safeBaseFileName
                + ".xliffs"));
        for (File f : fs)
        {
            String content = FileUtil.readFile(f, "utf-8");
            content = decodingForPassolo(content);
            FileUtil.writeFile(f, content, "utf-8");
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

        try
        {
            // First create the command file.
            StringBuffer commandFileNameBuffer = new StringBuffer(
                    safeBaseFileName);
            commandFileNameBuffer.append(".ex_command");

            commandFileName = commandFileNameBuffer.toString();
            writeCommandFile(commandFileName);

            // Now wait for status file.
            StringBuffer statusFileName = new StringBuffer(safeBaseFileName);
            statusFileName.append(".status");
            String maxTTW = m_passoloProperties
                    .getProperty(PassoloConfiguration.MAX_TIME_TO_WAIT_EXPORT);
            long maxTimeToWait = (long) (Long.parseLong(maxTTW)) * 60 * 1000;
            FileWaiter waiter = new FileWaiter(2000L, maxTimeToWait,
                    statusFileName.toString());
            waiter.waitForFile();

            // Conversion is done, but check the status to see if there is an
            // error.
            File statusFile = new File(statusFileName.toString());
            BufferedReader reader = new BufferedReader(new FileReader(
                    statusFile));
            String line = reader.readLine();
            String msg = reader.readLine();
            reader.close();
            statusFile.delete();
            int errorCode = Integer.parseInt(line);
            if (errorCode > 0)
            {
                logger.error(msg);
                throw new Exception(msg);
            }
        }
        finally
        {
            if (commandFileName != null)
            {
                try
                {
                    File f = new File(commandFileName);
                    f.delete();
                }
                catch (Exception e)
                {
                }
            }

            PROCESS_FILES.remove(safeBaseFileName);
        }
    }

    private void setBasicParams() throws PassoloAdapterException
    {
        try
        {
            setConversionDir();
            setSaveDirectory();
            m_currentTimeMillis = System.currentTimeMillis();
        }
        catch (Exception e)
        {
            logger.error("Unable to set basic parameters. ", e);
            throw new PassoloAdapterException("Unexpected", null, e);
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
        saveDir.append(m_isImport ? "import" : "export");
        File saveDirF = new File(saveDir.toString());
        saveDirF.mkdirs();

        m_saveDir = saveDir.toString();
    }

    /**
     * Actually writes out the command file.
     */
    private void writeCommandFile(String p_commandFileName) throws Exception
    {
        String locales = m_eventFlow.getTargetLocale();
        String orgLocales = locales;
        locales = StringUtil.replace(locales, "_", "-");
        locales = StringUtil.replace(locales, ",", "|");

        String[] ls = orgLocales.split(",");
        for (String l : ls)
        {
            List<String> mLs = PassoloUtil.getMappingLocalesG2P(l);
            if (mLs != null)
            {
                for (String ml : mLs)
                {
                    locales += "|";
                    locales += ml;
                }
            }
        }

        FileWriter commandFile = new FileWriter(p_commandFileName);
        commandFile.write(locales);
        commandFile.close();
    }

    /**
     * Converts idml to xml for importing or converts xml to idml for exporting.
     * 
     * @param filepath
     * @throws Exception
     */
    private void convert(String filepath) throws Exception
    {
        String commandFileName = null;

        try
        {
            StringBuffer commandFileNameBuffer = new StringBuffer(filepath);
            commandFileNameBuffer.append(".im_command");

            commandFileName = commandFileNameBuffer.toString();
            writeCommandFile(commandFileName);

            // now wait for status file
            StringBuffer statusFileName = new StringBuffer(filepath);
            statusFileName.append(".status");
            String maxTTW = m_passoloProperties
                    .getProperty(PassoloConfiguration.MAX_TIME_TO_WAIT_IMPORT);
            long maxTimeToWait = Long.parseLong(maxTTW) * 60 * 1000;
            FileWaiter waiter = new FileWaiter(10000L, maxTimeToWait,
                    statusFileName.toString());

            waiter.waitForFile();

            // conversion is done, but check the status to see if
            // there is an error
            File statusFile = new File(statusFileName.toString());
            BufferedReader reader = new BufferedReader(new FileReader(
                    statusFile));

            String line = reader.readLine();
            String msg = reader.readLine();

            // String errorCodeString = line.substring(6); //Error:1
            reader.close();
            statusFile.delete();

            int errorCode = Integer.parseInt(line);
            if (errorCode > 0)
            {
                logger.error(msg);
                throw new Exception(msg);
            }

            List<File> fs = FileUtil
                    .getAllFiles(new File(filepath + ".xliffs"));
            for (File f : fs)
            {
                String content = FileUtil.readFile(f, "utf-8");
                content = encodingForPassolo(content);
                FileUtil.writeFile(f, content, "utf-8");
            }
        }
        finally
        {
            if (commandFileName != null)
            {
                try
                {
                    File f = new File(commandFileName);
                    f.delete();
                }
                catch (Exception e)
                {
                }
            }
        }
    }

    private String decodingForPassolo(String content)
    {
        content = StringUtil.replaceWithRE(content, P1, new Replacer() 
        {
			@Override
			public String getReplaceString(Matcher m) 
			{
				String s = m.group(1);
	            String n = m.group(2);

	            while (n.startsWith("0"))
	            {
	                n = n.substring(1);
	            }
	            
				return s + "&#x" + n + ";";
			}
		});

        return content;
    }

    private String encodingForPassolo(String content)
    {
        content = StringUtil.replaceWithRE(content, P2, new Replacer() 
        {
			@Override
			public String getReplaceString(Matcher m) 
			{
				String n = m.group(1);
	            for (int i = 0; i < 4 - n.length(); i++)
	            {
	                n = "0" + n;
	            }
	            
				return "\\" + n;
			}
		});
        return content;
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
        return CxeMessageType
                .getCxeMessageType(CxeMessageType.XML_IMPORTED_EVENT);
    }

    public String getPostMergeEvent()
    {
        return CxeMessageType.getCxeMessageType(
                CxeMessageType.PASSOLO_LOCALIZED_EVENT).getName();
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

    protected void modifyEventFlowXmlForImport(String fileName,
            String xliffPath, int p_docPageNum, int p_docPageCount, EventFlowXml newEventFlowXml)
            throws Exception
    {
        File f = new File(fileName);
        String name = f.getName();
        
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
        newC.addValue("relSafeName", name + ".xliffs" + xliffPath);
        newEventFlowXml.getCategory().add(newC);

        // Then modify eventFlow
        newEventFlowXml.setPostMergeEvent(getPostMergeEvent());
        // newEventFlowXml.setSourceFormatType("xml");

        newEventFlowXml.getBatchInfo().setDocPageCount(p_docPageCount);
        newEventFlowXml.getBatchInfo().setDocPageNumber(p_docPageNum);

        if (displayName == null)
        {
            displayName = newEventFlowXml.getDisplayName();
        }

        newEventFlowXml.getBatchInfo().setDisplayName(displayName + xliffPath);
    }

    protected MessageData readConvOutput(String fileName)
            throws PassoloAdapterException
    {
        try
        {
            FileMessageData fmd = MessageDataFactory.createFileMessageData();
            fmd.copyFrom(new File(fileName));
            return fmd;
        }
        catch (Exception e)
        {
            logger.error("Read passolo file failed", e);
            throw wrapPassoloExportException(e, e.getMessage());
        }
    }

    private boolean writeMergedContent(String p_filepath)
    {
        String bak = p_filepath + ".xliffs.bak";
        File bakRoot = new File(bak);
        if (bakRoot.exists())
        {
            FileUtil.deleteFile(bakRoot);
        }

        File target = new File(p_filepath + ".xliffs");
        try
        {
            FileUtil.copyFolder(target, bakRoot);
        }
        catch (IOException e)
        {
            logger.error(e.getMessage(), e);
        }

        List<File> fs = FileUtil.getAllFiles(target);
        for (File f : fs)
        {
            String path = StringUtil.replace(f.getAbsolutePath(), "\\", "/");

            File source = new File(path);
            if (source.exists())
            {
                String content = getMergedContent(source);

                if (content != null && content.length() > 0)
                {
                    try
                    {
                        FileUtil.writeFile(f, content, "utf-8");
                    }
                    catch (IOException e)
                    {
                        logger.error(e.getMessage(), e);
                    }
                }
                else
                {
                    FileUtil.deleteFile(f);
                }
            }
        }

        fs = FileUtil.getAllFiles(target);
        return fs.size() > 0;
    }

    protected Map<String, MessageData> readXliffOutput(String p_filepath)
            throws PassoloAdapterException
    {
        Map<String, MessageData> map = new HashMap<String, MessageData>();
        try
        {
            String root = p_filepath + ".xliffs";
            List<File> fs = FileUtil.getAllFiles(new File(root));

            if (fs.size() == 0)
            {
                String msg = "The target languages in LPU have no corresponding target locales in the file profile selected in GlobalSight.";
                throw wrapPassoloImportException(new IllegalArgumentException(
                        msg), msg);
            }

            for (int i = 0; i < fs.size(); i++)
            {
                FileMessageData d = MessageDataFactory
                        .createFileMessageData("xliff");
                d.copyFrom(fs.get(i));

                File f = fs.get(i);
                String path = f.getAbsolutePath();
                path = StringUtil.replace(path, root, "");

                map.put(path, d);
            }

            return map;
        }
        catch (Exception e)
        {
            logger.error("Failed to read xml output:", e);
            throw wrapPassoloImportException(e, e.getMessage());
        }
    }

    private String writeContentToConvInbox() throws PassoloAdapterException
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

            // fileName = fileName.replace("\\", "/");

            FileMessageData fmd = (FileMessageData) m_cxeMessage
                    .getMessageData();
            fmd.copyTo(new File(fileName));

            return fileName;
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            String[] errorArgs =
            { m_eventFlow.getDisplayName() };
            throw new PassoloAdapterException("Import", errorArgs, e);
        }
    }

    private String writeContent() throws IOException
    {
        String saveFileName = FileUtils.concatPath(m_saveDir, getCategory().getValue("relSafeName"));
        File saveFile = new File(saveFileName);
        File parent = saveFile.getParentFile();
        if (!parent.exists())
        {
            parent.mkdirs();
        }

        m_cxeMessage.getMessageData().copyTo(saveFile);

        return saveFileName;
    }

    private static PassoloAdapterException wrapPassoloExportException(
            Exception e, String arg)
    {
        return new PassoloAdapterException("Export", new String[]
        { arg }, e);
    }

    private static PassoloAdapterException wrapPassoloImportException(
            Exception e, String arg)
    {
        return new PassoloAdapterException("Import", new String[]
        { arg }, e);
    }

    private static boolean isExportFileComplete(String p_filekey)
    {
        // Default is to write out the file.
        boolean result = true;
        int curPageCnt = -1;

        synchronized (s_exportBatchesLocker)
        {
            Integer oldPageCount = PassoloUtil.EXPORTING_PAGES.get(p_filekey);
            if (oldPageCount == null)
            {
                throw new IllegalArgumentException("Can not find the file "
                        + p_filekey);
            }
            else
            {
                curPageCnt = oldPageCount.intValue() - 1;
                if (curPageCnt == 0)
                {
                    // The batch is complete, remove the value from the
                    // hashtable.
                    result = true;
                    PassoloUtil.EXPORTING_PAGES.remove(p_filekey);
                }
                else
                {
                    result = false;
                    PassoloUtil.EXPORTING_PAGES.put(p_filekey, new Integer(
                            curPageCnt));
                }
            }
        }

        return result;
    }

    public static String getConversionDir() throws Exception
    {
        String winfiles = SystemConfiguration.getInstance().getStringParameter(
                SystemConfigParamNames.PASSOLO_CONV_DIR);

        return winfiles + File.separator + "passolo";
    }

    private List<PassoloTu> getAllTus(String content)
    {
        List<PassoloTu> tus = new ArrayList<PassoloTu>();

        Pattern p = Pattern
                .compile("<trans-unit[\\s\\S]*? id=\"([^\"]*)\"[^<]*?<source[^>]*?>([^<]*?)</source>[^<]*(<target[^>]*>)([^<]*?)</target>");
        Matcher m = p.matcher(content);
        while (m.find())
        {
            String id = m.group(1);
            String source = m.group(2);
            String targetTag = m.group(3);
            String target = m.group(4);

            PassoloTu tu = new PassoloTu();
            tu.setId(id);
            tu.setSource(source);
            tu.setTarget(target);

            if (!(targetTag.indexOf("removed='true'") > 0))
            {
                tus.add(tu);
            }
        }

        return tus;
    }

    private String getMergedContent(File source)
    {
        if (source.exists())
        {
            String content2 = null;
            try
            {
                String content1 = FileUtil.readFile(source, "utf-8");

                StringBuffer content = new StringBuffer();
                content.append("");

                Pattern p = Pattern.compile("[\\s\\S]*<body>");
                Matcher m = p.matcher(content1);

                if (m.find())
                {
                    content.append(m.group()).append(FileUtil.lineSeparator);
                }

                String targetLocale = "";
                Pattern p2 = Pattern.compile("xml:lang=\"([^\"]*)\"");
                Matcher m2 = p2.matcher(content1);

                if (m2.find())
                {
                    targetLocale = m2.group(1);
                }

                List<PassoloTu> tus = getAllTus(content1);
                if (tus.size() == 0)
                    return "";

                for (PassoloTu tu : tus)
                {
                    content.append(MessageFormat.format(TU, tu.getId(),
                            tu.getSource(), targetLocale, tu.getTarget()));
                }

                Pattern p3 = Pattern.compile("</body>[\\s\\S]*");
                Matcher m3 = p3.matcher(content1);

                if (m3.find())
                {
                    content.append("    " + m3.group());
                }

                return content.toString();
            }
            catch (Exception e)
            {
                logger.error(e.getMessage(), e);
                return content2;
            }
        }

        return "";
    }
}