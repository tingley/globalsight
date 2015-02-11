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

package com.globalsight.cxe.adapter.idml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.globalsight.cxe.engine.eventflow.Category;
import com.globalsight.cxe.engine.eventflow.DiplomatAttribute;
import com.globalsight.cxe.engine.eventflow.EventFlow;
import com.globalsight.cxe.engine.util.FileCopier;
import com.globalsight.cxe.engine.util.FileUtils;
import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.cxe.entity.fileprofile.FileProfileImpl;
import com.globalsight.cxe.entity.filterconfiguration.FilterConstants;
import com.globalsight.cxe.entity.filterconfiguration.FilterHelper;
import com.globalsight.cxe.entity.filterconfiguration.InddFilter;
import com.globalsight.cxe.entity.knownformattype.KnownFormatTypeImpl;
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
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.FileUtil;

public class IdmlHelper
{
    private static final String CATEGORY_NAME = "IdmlAdapter";

    private static final String CONTENT = "content.xml";

    // GBS-2955
    public static final String MARK_LF_IDML = "<GS-IDML-LF/>";
    public static final String LINE_BREAK = FileUtil.unUnicode("\u2028");
    private static final String NONBREEAKING_SPACE = FileUtil
            .unUnicode("\u00A0");
    private static final String PARAGRAPH_START = "<ParagraphStyleRange";
    private static final String PARAGRAPH_END = "</ParagraphStyleRange>";

    private static final String METADATA = "META-INF" + File.separator
            + "metadata.xml";

    private static final Logger logger = Logger.getLogger(IdmlHelper.class);

    // "INDD", "INX", "AI" -- goes in the command file
    private String m_conversionType = null;

    // The content specific conversion directory
    private String m_convDir = null;

    // The locale specific save directory under the conversion dir.
    private String m_saveDir = null;

    private boolean m_isImport = true;

    private long m_currentTimeMillis = 0;

    private static SystemConfiguration m_sc = SystemConfiguration.getInstance();

    private CxeMessage m_cxeMessage;

    private EventFlow m_eventFlow;

    private static Hashtable<String, Integer> s_exportBatches = new Hashtable<String, Integer>();
    private static Object s_exportBatchesLocker = new Object();

    private String displayName = null;

    private List<String> layers = null;
    private InddFilter filter = null;

    public IdmlHelper(CxeMessage p_cxeMessage)
    {
        m_cxeMessage = p_cxeMessage;
        m_eventFlow = new EventFlow(p_cxeMessage.getEventFlowXml());
    }

    /**
     * Just for junit test
     * 
     * @deprecated
     */
    public IdmlHelper()
    {

    }

    /**
     * Perform conversion
     * 
     * @return conversion result
     * @throws IdmlAdapterException
     */
    public CxeMessage[] performConversion() throws IdmlAdapterException
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
            MessageData[] messageData = readXmlOutput(filename);

            CxeMessage[] result = new CxeMessage[messageData.length];

            for (int i = 0; i < result.length; i++)
            {
                // 5 modify eventflowxml
                String basename = FileUtils.getBaseName(filename);
                String dirname = getUnzipDir(basename);
                String xmlfilename = dirname + File.separator + CONTENT;
                modifyEventFlowXmlForImport(xmlfilename, i + 1,
                        messageData.length);
                // 6 return proper CxeMesseges
                CxeMessageType type = getPostConversionEvent();
                CxeMessage cxeMessage = new CxeMessage(type);
                cxeMessage.setParameters(m_cxeMessage.getParameters());
                cxeMessage.setMessageData(messageData[i]);

                String eventFlowXml = m_eventFlow.serializeToXml();
                cxeMessage.setEventFlowXml(eventFlowXml);

                result[i] = cxeMessage;
            }

            return result;
        }
        catch (IdmlAdapterException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw wrapAdobeImportException(e, m_eventFlow.getDisplayName());
        }
    }

    public CxeMessage[] performConversionBack() throws IdmlAdapterException
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

            String eBatchId = (String) params.get("ExportBatchId");
            String tFileName = (String) params.get("TargetFileName");
            if (ExportUtil.isLastFile(eBatchId, tFileName, targetLocale))
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
                outputMsg.setEventFlowXml(eventFlowXml);

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
                outputMsg.setEventFlowXml(m_eventFlow.serializeToXml());
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

    private void setBasicParams() throws IdmlAdapterException
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
            throw new IdmlAdapterException("Unexpected", null, e);
        }
    }

    /**
     * Sets the type
     */
    private void setType()
    {
        m_conversionType = "idml";
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
     * Converts idml to xml for importing or converts xml to idml for exporting.
     * 
     * @param filepath
     * @throws Exception
     */
    private void convert(String filepath) throws Exception
    {
        String dirName = getUnzipDir(filepath);
        IdmlConverter converter = new IdmlConverter();
        if (m_isImport)
        {
            converter.convertIdmlToXml(filepath, dirName);
        }
        else
        {
            split(dirName);

            String filename = getCategory().getDiplomatAttribute(
                    "safeBaseFileName").getValue();
            converter.convertXmlToIdml(filename, dirName);
        }
    }

    public static void split(String dir) throws Exception
    {
        File f = new File(dir, CONTENT);
        File backupFile = new File(dir + CONTENT);
        if (backupFile.exists())
        {
            backupFile.delete();
        }

        String content = FileUtil.readFile(f, "utf-8");

        Pattern p = Pattern
                .compile("<story name=\"(.*?)\">[\\r\\n]*([\\d\\D]*?)</story>");
        Matcher m = p.matcher(content);
        while (m.find())
        {
            String path = m.group(1);
            String fContent = m.group(2);

            fContent = fContent.replaceFirst("<xml ", "<?xml ");
            fContent = fContent.replaceFirst("/>", "\\?>");

            File newFile = new File(dir, path);
            FileUtil.writeFile(newFile, fContent, "utf-8");
        }

        f.renameTo(backupFile);

        f = new File(dir, CONTENT);
        if (f.exists())
        {
            f.delete();
        }
    }

    private String getUnzipDir(String p_filepath)
    {
        return p_filepath + ".unzip";
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
                CxeMessageType.IDML_LOCALIZED_EVENT).getName();
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
            int p_docPageNum, int p_docPageCount) throws Exception
    {
        // First get original Category
        Category oriC = getCategory();
        if (oriC != null)
        {
            Category newC = new Category(CATEGORY_NAME, new DiplomatAttribute[]
            { oriC.getDiplomatAttribute("postMergeEvent"),
                    oriC.getDiplomatAttribute("formatType"),
                    oriC.getDiplomatAttribute("safeBaseFileName"),
                    oriC.getDiplomatAttribute("originalFileSize"),
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
                    new DiplomatAttribute("relSafeName", p_xmlFilename) });
            m_eventFlow.addCategory(newC);
        }
        // Then modify eventFlow
        m_eventFlow.setPostMergeEvent(getPostMergeEvent());
        // m_eventFlow.setSourceFormatType("xml");

        m_eventFlow.setDocPageCount(p_docPageCount);
        m_eventFlow.setDocPageNumber(p_docPageNum);

        if (displayName == null)
        {
            displayName = m_eventFlow.getDisplayName();
        }
    }

    protected MessageData readConvOutput(String fileName)
            throws IdmlAdapterException
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
            logger.error("Read adobe file failed", e);
            throw wrapAdobeExportException(e, e.getMessage());
        }
    }

    /**
     * Integrates all xml files to one file.
     * 
     * @param filepath
     * @throws Exception
     */
    private File integrate(String filepath) throws Exception
    {
        String dir = getUnzipDir(filepath);

        File designmap = new File(dir + "/designmap.xml");
        String content = FileUtil.readFile(designmap, "utf-8");
        StringBuffer s = new StringBuffer(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        s.append(FileUtil.lineSeparator);
        s.append("<stories>");

        if (isTranslateFileInfo())
        {
            String path = dir + File.separator + METADATA;
            File f = new File(path);
            if (f.exists())
            {
                String c = FileUtil.readFile(f, "utf-8");
                c = c.replaceFirst("<\\?xml ", "<xml ");
                c = c.replaceFirst("\"\\?>", "\"/>");
                c = formatForImport(c);

                s.append(FileUtil.lineSeparator);
                s.append("<story name=\"").append(METADATA).append("\">");
                s.append(FileUtil.lineSeparator);
                s.append(c);
                s.append(FileUtil.lineSeparator);
                s.append("</story>");
            }
        }

        Pattern p = Pattern
                .compile("<idPkg:Story src=\"([^\"]*?/Story_([^\"]*?).xml)\"\\s*/>");
        Matcher m = p.matcher(content);

        while (m.find())
        {
            String id = m.group(2);

            if (!isTranslate(id, dir))
            {
                continue;
            }

            String path = dir + File.separator + m.group(1);
            File f = new File(path);

            String c = FileUtil.readFile(f, "utf-8");

            c = c.replaceFirst("<\\?xml ", "<xml ");
            c = c.replaceFirst("\"\\?>", "\"/>");
            c = formatForImport(c);

            s.append(FileUtil.lineSeparator);
            s.append("<story name=\"").append(m.group(1)).append("\">");
            s.append(FileUtil.lineSeparator);
            s.append(c);
            s.append(FileUtil.lineSeparator);
            s.append("</story>");
        }

        s.append(FileUtil.lineSeparator);
        s.append("</stories>");

        File f = new File(dir + CONTENT);
        FileUtil.writeFile(f, s.toString(), "utf-8");

        return f;
    }

    private String optimizeForOddChar(String s)
    {
        if (!isExtractLineBreak())
        {
            s = removeLineBreak(s);
        }
        else
        {
            s = convertLineBreakToTag(s);
        }

        if (isReplaceNonbreakingSpace())
        {
            s = replaceSpace(s);
        }

        return s;
    }

    private String convertLineBreakToTag(String s)
    {
        s = s.replace(LINE_BREAK, MARK_LF_IDML);
        return s;
    }

    private String removeLineBreak(String s)
    {
        s = s.replace(LINE_BREAK, "");
        return s;
    }

    private String replaceSpace(String s)
    {
        return s.replace(NONBREEAKING_SPACE, " ");
    }

    private InddFilter getInddFilter()
    {
        if (filter == null)
        {
            filter = getMainFilter();
        }

        if (filter == null)
        {
            filter = new InddFilter();
        }

        return filter;
    }

    private boolean isTranslateFileInfo()
    {
        return getInddFilter().getTranslateFileInfo();
    }

    private boolean isExtractLineBreak()
    {
        return getInddFilter().getExtractLineBreak();
    }

    private boolean isReplaceNonbreakingSpace()
    {

        return getInddFilter().isReplaceNonbreakingSpace();
    }

    /**
     * Is the layer with the id should be translated.
     * 
     * @param id
     * @param root
     * @return
     * @throws Exception
     */
    private boolean isTranslate(String id, String dir) throws Exception
    {
        if (filter == null)
        {
            filter = getMainFilter();
        }

        if (filter == null)
        {
            filter = new InddFilter();
        }

        boolean isTranslateHiddenLayer = filter.getTranslateHiddenLayer();
        boolean isTranslateMaster = filter.getTranslateMasterLayer();

        if (!isTranslateHiddenLayer && isHidden(id, dir))
        {
            return false;
        }

        if (!isTranslateMaster && isMasterSpread(id, dir))
        {
            return false;
        }

        return true;
    }

    /**
     * Gets all hidden layers id.
     * 
     * @param root
     * @return
     * @throws Exception
     */
    private List<String> getHiddenLayers(String root) throws Exception
    {
        String content = FileUtil.readFile(new File(root, "designmap.xml"),
                "utf-8");

        List<String> layers = new ArrayList<String>();
        Pattern p = Pattern
                .compile("<Layer Self=\"([^\"]*)\" Name=\"[^\"]*\" Visible=\"false\"");
        Matcher m = p.matcher(content);
        while (m.find())
        {
            layers.add(m.group(1));
        }

        return layers;
    }

    /**
     * Is the story with the id in hidden layer.
     * 
     * @param id
     * @param dir
     * @return
     * @throws Exception
     */
    private boolean isHidden(String id, String dir) throws Exception
    {
        return isInHiddenLayer(id, dir) || isInHiddenLayerGroup(id, dir);
    }

    /**
     * Is the layer with the id is in a hidden layer.
     * 
     * @param id
     * @param dir
     * @return
     * @throws Exception
     */
    private boolean isInHiddenLayer(String id, String dir) throws Exception
    {
        String s = "ParentStory=\"" + id + "\"";

        List<String> roots = new ArrayList<String>();
        roots.add(dir + "/MasterSpreads");
        roots.add(dir + "/Spreads");

        for (String root : roots)
        {
            List<File> fs = FileUtil.getAllFiles(new File(root));
            for (File f : fs)
            {
                String content = FileUtil.readFile(f, "utf-8");

                if (content.contains(s))
                {
                    Pattern p = Pattern.compile(s
                            + "[^>]*?ItemLayer=\"([^\"]*?)\"");
                    Matcher m = p.matcher(content);
                    if (m.find())
                    {
                        String layerId = m.group(1);

                        if (layers == null)
                        {
                            layers = getHiddenLayers(dir);
                        }

                        return layers.contains(layerId);
                    }

                    return false;
                }
            }
        }

        return false;
    }

    /**
     * Is the story with the id in a hidden layer group.
     * 
     * @param id
     * @param dir
     * @return
     * @throws Exception
     */
    private boolean isInHiddenLayerGroup(String id, String dir)
            throws Exception
    {
        String s = "ParentStory=\"" + id + "\"";

        String regex = "<Group[^>]*?ItemLayer=\"([^\"]*?)\"[^>]*?>([\\d\\D]*?)</Group>";

        List<String> roots = new ArrayList<String>();
        roots.add(dir + "/MasterSpreads");
        roots.add(dir + "/Spreads");

        for (String root : roots)
        {
            List<File> fs = FileUtil.getAllFiles(new File(root));
            for (File f : fs)
            {
                String content = FileUtil.readFile(f, "utf-8");

                if (content.contains(s))
                {
                    Pattern p = Pattern.compile(regex);
                    Matcher m = p.matcher(content);
                    while (m.find())
                    {
                        String layerId = m.group(1);
                        String layerContent = m.group(2);
                        if (layerContent.contains(s))
                        {
                            if (layers == null)
                            {
                                layers = getHiddenLayers(dir);
                            }

                            return layers.contains(layerId);
                        }
                    }

                    return false;
                }
            }
        }

        return false;
    }

    /**
     * Is the layer with the id is in a master spread.
     * 
     * @param id
     * @param dir
     * @return
     * @throws Exception
     */
    private boolean isMasterSpread(String id, String dir) throws Exception
    {
        String s = "ParentStory=\"" + id + "\"";

        String path = dir + "/MasterSpreads";
        List<File> fs = FileUtil.getAllFiles(new File(path));
        for (File f : fs)
        {
            String content = FileUtil.readFile(f, "utf-8");
            if (content.contains(s))
            {
                return true;
            }
        }
        return false;
    }

    protected MessageData[] readXmlOutput(String p_filepath)
            throws IdmlAdapterException
    {
        try
        {
            File content = integrate(p_filepath);
            FileMessageData d = MessageDataFactory.createFileMessageData("xml");
            d.copyFrom(content);

            MessageData[] md = new MessageData[]
            { d };

            String dir = getUnzipDir(p_filepath);
            File dirFile = new File(dir);
            copyToTargetLocales(FileUtil.getAllFiles(dirFile));

            return md;
        }
        catch (Exception e)
        {
            logger.error("Failed to read xml output:", e);
            throw wrapAdobeImportException(e, e.getMessage());
        }
    }

    private String writeContentToConvInbox() throws IdmlAdapterException
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
            throw new IdmlAdapterException("Import", errorArgs, e);
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

    private static IdmlAdapterException wrapAdobeExportException(Exception e,
            String arg)
    {
        return new IdmlAdapterException("Export", new String[]
        { arg }, e);
    }

    private static IdmlAdapterException wrapAdobeImportException(Exception e,
            String arg)
    {
        return new IdmlAdapterException("Import", new String[]
        { arg }, e);
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
            StringBuffer targetDir = new StringBuffer(expectedFile.getParent());
            int srcIndex = targetDir.lastIndexOf(srcLocale);
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

    public static String getConversionDir() throws Exception
    {
        StringBuffer convDir = new StringBuffer();
        convDir.append(m_sc.getStringParameter(
                SystemConfigParamNames.FILE_STORAGE_DIR,
                CompanyWrapper.SUPER_COMPANY_ID));
        convDir.append(File.separator);
        convDir.append("Idml-Conv");

        return convDir.toString();
    }

    public static boolean isIdmlFileProfile(long fileProfileId)
    {
        FileProfileImpl f = HibernateUtil.get(FileProfileImpl.class,
                fileProfileId, false);
        if (f != null)
        {
            long id = f.getKnownFormatTypeId();
            KnownFormatTypeImpl type = HibernateUtil.get(
                    KnownFormatTypeImpl.class, id);
            if (type != null)
            {
                return "IDML_IMPORTED_EVENT".equalsIgnoreCase(type
                        .getPre_extract_event());
            }
        }

        return false;
    }

    /**
     * Removes space if it is not in content tag.
     * 
     * @param s
     * @return
     */
    private static String trimSpace(String s)
    {
        StringBuffer temp = new StringBuffer(s);

        Pattern p = Pattern.compile("<[^>]*?>[\\t\\n]+?<");
        Matcher m = p.matcher(s);
        while (m.find())
        {
            String content = m.group();
            if (content.startsWith("<Content>"))
            {
                continue;
            }

            int index = -1;
            while (true)
            {
                index = temp.indexOf(content, index + 1);
                if (index < 0)
                {
                    break;
                }

                for (int i = index + content.length() - 2; i >= 0; i--)
                {
                    if ("\n\t".indexOf(temp.charAt(i)) > -1)
                    {
                        temp.deleteCharAt(i);
                    }
                    else
                    {
                        break;
                    }
                }
            }

            m = p.matcher(temp);
        }

        return temp.toString();
    }

    private int getIndexOfParaStart(StringBuffer s, int index)
    {
        int n = s.indexOf(PARAGRAPH_START, index + 1);
        if (n < 0)
            return n;

        int n2 = s.indexOf(">", n);

        if (s.charAt(n2 - 1) == '/')
            return getIndexOfParaStart(s, n2);

        return n;
    }

    public String formatForImport(String s)
    {
        s = optimizeForOddChar(s);
        StringBuffer temp = new StringBuffer(s);

        int parL = PARAGRAPH_END.length();
        int index = getIndexOfParaStart(temp, -1);

        while (index > 0)
        {
            int n2 = temp.indexOf(PARAGRAPH_END, index);

            if (n2 < 0)
            {
                break;
            }

            String content = temp.substring(index, n2 + parL);
            String temContent = content.replaceAll(
                    "<ParagraphStyleRange[^>]*/>", "");

            while (temContent.split(PARAGRAPH_START).length != temContent
                    .split(PARAGRAPH_END).length + 1)
            {
                n2 = temp.indexOf(PARAGRAPH_END, n2 + 1);
                if (n2 < 0)
                    break;

                content = temp.substring(index, n2 + parL);
                temContent = content.replaceAll("<ParagraphStyleRange[^>]*/>",
                        "");
            }

            String content2 = trimSpace(content);
            temp = temp.replace(index, index + content.length(), content2);

            index = getIndexOfParaStart(temp, index + content2.length());
        }

        String range = temp.toString();
        IdmlTagHelper h = new IdmlTagHelper();
        return h.mergeTags(range);
    }

    public static String formatForOfflineDownload(String s)
    {
        s = removeSpaceBeforeTag(s, "[^>]*?&lt;Content&gt;[^>]*?");
        s = removeSpaceAfterTags(s, "[^>]*?&lt;/Content&gt;[^>]*?");
        s = removeSpaceBeforeTag(s,
                "[^>]*?&lt;CharacterStyleRange.*?&gt;[^>]*?");
        s = removeSpaceAfterTags(s,
                "[^>]*?&lt;CharacterStyleRange.*?&gt;[^>]*?");
        s = removeSpaceBeforeTag(s, "[^>]*?&lt;/CharacterStyleRange&gt;[^>]*?");
        s = removeSpaceAfterTags(s, "[^>]*?&lt;/CharacterStyleRange&gt;[^>]*?");
        return s;
    }

    private static String trimSpaceBeforeTag(String s, String tagRegex)
    {
        StringBuffer temp = new StringBuffer(s);

        Pattern p = Pattern.compile(tagRegex);
        Matcher m = p.matcher(s);
        while (m.find())
        {
            String content = m.group();
            int index = -1;
            while (true)
            {
                index = temp.indexOf(content, index + 1);
                if (index < 0)
                {
                    break;
                }

                for (int i = index - 1; i >= 0; i--)
                {
                    if ("\n\t".indexOf(temp.charAt(i)) > -1)
                    {
                        temp.deleteCharAt(i);
                    }
                    else
                    {
                        break;
                    }
                }
            }
        }

        return temp.toString();
    }

    private static String trimSpaceAfterTags(String s, String tagRegex)
    {
        StringBuffer temp = new StringBuffer(s);

        Pattern p = Pattern.compile(tagRegex);
        Matcher m = p.matcher(s);
        while (m.find())
        {
            String content = m.group();
            int index = -1;
            while (true)
            {
                index = temp.indexOf(content, index + 1);
                if (index < 0)
                {
                    break;
                }

                int i = index + content.length();
                while (i < temp.length())
                {
                    if ("\n\t".indexOf(temp.charAt(i)) > -1)
                    {
                        temp.deleteCharAt(i);
                    }
                    else
                    {
                        break;
                    }
                }
            }
        }

        return temp.toString();
    }

    private static String removeSpaceBeforeTag(String s, String tagRegex)
    {
        String realRegex = "<[^>]*?>" + tagRegex + "</[^>]*>";
        return trimSpaceBeforeTag(s, realRegex);
    }

    private static String removeSpaceAfterTags(String s, String tagRegex)
    {
        String realRegex = "<[^>]*?>" + tagRegex + "</[^>]*>";
        return trimSpaceAfterTags(s, realRegex);
    }

    private InddFilter getMainFilter()
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

}