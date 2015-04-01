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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.globalsight.cxe.adapter.openoffice.StringIndex;
import com.globalsight.cxe.engine.util.FileCopier;
import com.globalsight.cxe.engine.util.FileUtils;
import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.cxe.entity.fileprofile.FileProfileImpl;
import com.globalsight.cxe.entity.filterconfiguration.FilterConstants;
import com.globalsight.cxe.entity.filterconfiguration.FilterHelper;
import com.globalsight.cxe.entity.filterconfiguration.InddFilter;
import com.globalsight.cxe.entity.filterconfiguration.MSOffice2010Filter;
import com.globalsight.cxe.entity.knownformattype.KnownFormatTypeImpl;
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
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.FileUtil;
import com.globalsight.util.StringUtil;

public class IdmlHelper
{
    private static final String CATEGORY_NAME = "IdmlAdapter";

    private static final String CONTENT = "content.xml";

    // GBS-2955
    public static final String MARK_LF_IDML = "<GS-IDML-LF/>";
    // GBS-3619
    public static final String MARK_LineBreak_IDML = "<GS-IDML-LineBreak/>";
    public static final String LINE_BREAK = FileUtil.unUnicode("\u2028");
    private static final String NONBREEAKING_SPACE = FileUtil
            .unUnicode("\u00A0");
    private static final String PARAGRAPH_START = "<ParagraphStyleRange";
    private static final String PARAGRAPH_END = "</ParagraphStyleRange>";

    private static final String DESIGNMAP = "designmap.xml";
    private static final String METADATA = "META-INF" + File.separator
            + "metadata.xml";
    
    // for idml filter, skip Tracking and Kerning
    private static String[] ignored_atts = { "Tracking", "KerningValue", "KerningMethod" };
    private static String tags_End = "</Content></CharacterStyleRange>";
    private static String tag_Start = "<CharacterStyleRange";
    private static String content_Start = "<Content>";
    
    private static String Visible_false = "Visible=\"false\"";

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

    private EventFlowXml m_eventFlow;

    private static Hashtable<String, Integer> s_exportBatches = new Hashtable<String, Integer>();
    private static Object s_exportBatchesLocker = new Object();

    private String displayName = null;

    private List<String> layers = null;
    private InddFilter filter = null;
    // GBS-3054
    private List<String> hyperlinkIds = new ArrayList<String>();

    public IdmlHelper(CxeMessage p_cxeMessage)
    {
        m_cxeMessage = p_cxeMessage;
        m_eventFlow = p_cxeMessage.getEventFlowObject();
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
                EventFlowXml newEventFlowXml = m_eventFlow.clone();
                String basename = FileUtils.getBaseName(filename);
                String dirname = getUnzipDir(basename);
                String xmlfilename = dirname + File.separator + CONTENT;
                modifyEventFlowXmlForImport(xmlfilename, i + 1,
                        messageData.length, newEventFlowXml);
                // 6 return proper CxeMesseges
                CxeMessageType type = getPostConversionEvent();
                CxeMessage cxeMessage = new CxeMessage(type);
                cxeMessage.setParameters(m_cxeMessage.getParameters());
                cxeMessage.setMessageData(messageData[i]);
                cxeMessage.setEventFlowObject(newEventFlowXml);

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
                String oofilename = getCategory().getValue("safeBaseFileName");
                String oofile = FileUtils.concatPath(m_saveDir, oofilename);
                modifyEventFlowXmlForExport();
                convert(oofile);
                MessageData fmd = readConvOutput(oofile);

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

            String filename = getCategory().getValue("safeBaseFileName");
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
        // GBS-3054
        content = repairHyperlinks(content);

        Pattern p = Pattern
                .compile("<story name=\"(.*?)\"[^>]*?>[\\r\\n]*([\\d\\D]*?)</story>");
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

    /**
     * Repairs the hyperlinks in designmap.xml. To keep the hyperlink name
     * consistent with the url translation.
     */
    private static String repairHyperlinks(String content)
    {
        String regexTemplate = "(<HyperlinkURLDestination [^>]*DestinationURL=\"([^\"]*)\"[^>]*DestinationUniqueKey=\"{0}\"[^>]*Name=\")[^\"]*(\"[^>]*/>)";

        List<String> hyperlinkIds = getHyperlinkIds(content);
        for (String id : hyperlinkIds)
        {
            String regex = MessageFormat.format(regexTemplate, id);
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(content);
            if (m.find())
            {
                // DestinationURL was extracted for translation
                String translatedUrl = m.group(2);
                String newHyperlink = m.group(1) + translatedUrl + m.group(3);

                content = content.replace(m.group(), newHyperlink);
            }
        }
        return content;
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
            int p_docPageNum, int p_docPageCount, EventFlowXml newEventFlowXml) throws Exception
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
        newC.addValue("hyperlinkIds", MSOffice2010Filter.toString(hyperlinkIds));
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

        File designmap = new File(dir + File.separator + DESIGNMAP);
        String content = FileUtil.readFile(designmap, "utf-8");
        StringBuffer buff = new StringBuffer(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        buff.append(FileUtil.lineSeparator);
        buff.append("<stories>");

        boolean addDesignMapXml = false;
        // extract Text Variables
        if (includeTextVariable(content))
        {
            addDesignMapXml = true;
        }

        if (isTranslateHyperlinks() && !addDesignMapXml)
        {
            // GBS-3054
            setHyperlinkIds(content);
            if (hyperlinkIds.size() > 0)
            {
                addDesignMapXml = true;
            }
        }

        if (addDesignMapXml)
        {
            String c = content.replaceFirst("<\\?xml ", "<xml ");
            c = c.replaceFirst("\"\\?>", "\"/>");

            buff.append(FileUtil.lineSeparator);
            buff.append("<story name=\"").append(DESIGNMAP).append("\">");
            buff.append(FileUtil.lineSeparator);
            buff.append(c);
            buff.append(FileUtil.lineSeparator);
            buff.append("</story>");
        }

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

                buff.append(FileUtil.lineSeparator);
                buff.append("<story name=\"").append(METADATA).append("\">");
                buff.append(FileUtil.lineSeparator);
                buff.append(c);
                buff.append(FileUtil.lineSeparator);
                buff.append("</story>");
            }
        }

        // 1 find MasterSpread & Spread by order
        List<String> spreadFiles = new ArrayList<String>();
        Pattern pMasterSpread = Pattern
                .compile("<idPkg:MasterSpread src=\"([^\"]*?/MasterSpread_([^\"]*?).xml)\"\\s*/>");
        Matcher mMasterSpread = pMasterSpread.matcher(content);
        while (mMasterSpread.find())
        {
            String masterSpread = mMasterSpread.group(1);
            spreadFiles.add(masterSpread);
        }

        Pattern pSpread = Pattern
                .compile("<idPkg:Spread src=\"([^\"]*?/Spread_([^\"]*?).xml)\"\\s*/>");
        Matcher mSpread = pSpread.matcher(content);
        while (mSpread.find())
        {
            String spread = mSpread.group(1);
            spreadFiles.add(spread);
        }

        // 2 find all TextFrame from Spread
        LinkedList<TextFrameObj> allTextFrameList = new LinkedList<TextFrameObj>();
        ArrayList<TextFrameObj> spreadTextFrameList = new ArrayList<TextFrameObj>();
        List<Double> pageXlist = new ArrayList<Double>();
        List<Integer> pageNumlist = new ArrayList<Integer>();
        List<String> overrideList = new ArrayList<String>();
        Pattern pPage = Pattern
                .compile("<Page[\\s]+[^>]*ItemTransform=\"([^\"]+)\"[^>]*>");
        Pattern pTextFrame = Pattern
                .compile("<TextFrame[\\s]+[^>]*ParentStory=\"([^\"]+)\"[^>]*ItemTransform=\"([^\"]+)\"[^>]*>");
        int pageDiff = 0;
        boolean isFirstPage = true;
        Map<String, Integer> storyPage = new HashMap<String, Integer>();
        

        for (String src : spreadFiles)
        {
            String path = dir + File.separator + src;
            File f = new File(path);
            String c = FileUtil.readFile(f, "utf-8").trim();
            pageXlist.clear();
            pageNumlist.clear();
            overrideList.clear();
            spreadTextFrameList.clear();
            boolean isMaster = c.endsWith("</idPkg:MasterSpread>");
            
            // get DOMVersion
            String domVersion = "8.0";
            StringIndex si = StringIndex.getValueBetween(c, 0, "DOMVersion=\"", "\"");
            if (si != null)
            {
                domVersion = si.value;
            }

            // get page locations
            Matcher mPage = pPage.matcher(c);
            while (mPage.find())
            {
                String itemTransform = mPage.group(1);
                String[] arrTransform = itemTransform.trim().split(" ");
                if (arrTransform.length == 6)
                {
                    String tempX = arrTransform[4].trim();
                    double dblX = Double.parseDouble(tempX);
                    pageXlist.add(dblX);
                }

                String pageTag = mPage.group();
                StringIndex siOverList = StringIndex.getValueBetween(pageTag, 0, "OverrideList=\"",
                        "\"");
                if (siOverList != null)
                {
                    String temp = siOverList.value;
                    String[] tempArray = temp.split(" ");
                    for (String tempS : tempArray)
                    {
                        overrideList.add(tempS);
                    }
                }

                // find page number
                if (!isMaster)
                {
                    int pageEndIndex = c.indexOf("</Page>", mPage.end());
                    String pageTagContent = c.substring(mPage.start(), pageEndIndex);
                    StringIndex pageNumSi = StringIndex.getValueBetween(pageTagContent, 0,
                            "<ListItem type=\"long\">", "</ListItem>");
                    if (pageNumSi != null)
                    {
                        int pageNum = Integer.parseInt(pageNumSi.value);

                        if (isFirstPage)
                        {
                            isFirstPage = false;
                            pageDiff = pageNum - 1;
                        }

                        int pageNumFromOne = pageNum - pageDiff;
                        pageNumlist.add(pageNumFromOne);
                    }
                }
            }

            if (pageXlist.size() == 0)
            {
                pageXlist.add(new Double(0));
            }

            // find TextFrame
            Matcher mTextFrame = pTextFrame.matcher(c);
            while (mTextFrame.find())
            {
                String textFrameInSpread = mTextFrame.group();
                String id = mTextFrame.group(1);
                String itemTransform = mTextFrame.group(2);
                boolean isHidden = false;

                TextFrameObj textFrame = TextFrameObj.createInstance(
                        domVersion, textFrameInSpread, id, itemTransform, c,
                        mTextFrame, overrideList);

                // ignore hidden layer
                if (!isTranslateHiddenLayer()
                        && textFrameInSpread.contains(Visible_false))
                {
                    isHidden = true;
                    textFrame.isHidden = true;
                }

                if (spreadTextFrameList.contains(textFrame))
                {
                    continue;
                }
                spreadTextFrameList.add(textFrame);

                // 2.2 find all TextFrame from story
                String storyFile = dir + File.separator + "Stories/Story_" + id
                        + ".xml";
                File sfile = new File(storyFile);
                if (sfile.exists())
                {
                    String storyContent = FileUtil.readFile(sfile, "utf-8");
                    Matcher storyTextFrame = pTextFrame.matcher(storyContent);

                    while (storyTextFrame.find())
                    {
                        String textFrameG = storyTextFrame.group();
                        String subid = storyTextFrame.group(1);
                        String subitemTransform = storyTextFrame.group(1);
                        TextFrameObj subtextFrame = TextFrameObj
                                .createInstance(domVersion, textFrameG, subid,
                                        subitemTransform, storyContent,
                                        storyTextFrame, overrideList);

                        // ignore hidden layer
                        if (isHidden
                                || (!isTranslateHiddenLayer() && textFrameG
                                        .contains(Visible_false)))
                        {
                            subtextFrame.isHidden = true;
                        }

                        if (!spreadTextFrameList.contains(subtextFrame))
                        {
                            spreadTextFrameList.add(subtextFrame);
                        }
                    }
                }
            }

            if (spreadTextFrameList.size() > 0)
            {
                Collections.sort(pageXlist);
                Collections.sort(pageNumlist);
                
                TextFrameComparator tfcom = new TextFrameComparator();
                tfcom.setPageX(pageXlist);
                tfcom.setPageNum(pageNumlist);
                Collections.sort(spreadTextFrameList, tfcom);
                
                if (pageNumlist.size() != 0)
                {
                    tfcom.setTextFramePageNum(spreadTextFrameList);
                }

                for (TextFrameObj textFrameObj : spreadTextFrameList)
                {
                    allTextFrameList.add(textFrameObj);
                }
            }
        }

        // find all story
        Pattern p = Pattern
                .compile("<idPkg:Story src=\"([^\"]*?/Story_([^\"]*?).xml)\"\\s*/>");
        Matcher m = p.matcher(content);
        List<String> storySrc = new ArrayList<String>();

        while (m.find())
        {
            String id = m.group(2);

            if (!isTranslate(id, dir))
            {
                continue;
            }

            String src = m.group(1);
            storySrc.add(src);
        }

        // sort them
        List<String> storySrcSorted = new ArrayList<String>();
        if (allTextFrameList == null || allTextFrameList.size() == 0)
        {
            // sort all stories by Document StoryList attributes
            Pattern pDocument = Pattern
                    .compile("<Document[\\s]+[^>]*StoryList=\"([^\"]*)\"[^>]*>");
            Matcher mDocument = pDocument.matcher(content);

            while (mDocument.find())
            {
                String ids = mDocument.group(1);
                String[] idList = ids.split("\\s");

                for (String storyId : idList)
                {
                    String story = "Stories/Story_" + storyId + ".xml";

                    if (storySrc.contains(story)
                            && !storySrcSorted.contains(story))
                    {
                        storySrcSorted.add(story);
                    }
                }
            }
        }
        else
        {
            for (TextFrameObj tfo : allTextFrameList)
            {
                if (tfo.isHidden)
                {
                    continue;
                }

                String story = "Stories/Story_" + tfo.parentStory + ".xml";
                if (storySrc.contains(story) && !storySrcSorted.contains(story))
                {
                    storySrcSorted.add(story);
                    storyPage.put(story, tfo.pageNum);
                }
            }

            if (allTextFrameList.size() < storySrc.size())
            {
                List<String> notIn = new ArrayList<String>();
                List<String> allTextFrameStories = new ArrayList<String>();

                for (TextFrameObj tfo : allTextFrameList)
                {
                    String story = "Stories/Story_" + tfo.parentStory + ".xml";
                    allTextFrameStories.add(story);
                }

                for (String story : storySrc)
                {
                    if (!allTextFrameStories.contains(story))
                    {
                        notIn.add(story);
                    }
                }

                for (String story : notIn)
                {
                    if (!storySrcSorted.contains(story))
                    {
                        storySrcSorted.add(story);
                    }
                }
            }

        }

        // add story content
        for (String src : storySrcSorted)
        {
            String path = dir + File.separator + src;
            File f = new File(path);
            if (!f.exists())
            {
                continue;
            }

            String c = FileUtil.readFile(f, "utf-8");
            c = c.replaceFirst("<\\?xml ", "<xml ");
            c = c.replaceFirst("\"\\?>", "\"/>");
            c = formatForImport(c);

            buff.append(FileUtil.lineSeparator);
            buff.append("<story name=\"").append(src).append("\"");
            if (storyPage.containsKey(src))
            {
                buff.append(" pageNum=\"").append(storyPage.get(src)).append("\"");
            }
            buff.append(">");
            buff.append(FileUtil.lineSeparator);
            buff.append(c);
            buff.append(FileUtil.lineSeparator);
            buff.append("</story>");
        }

        buff.append(FileUtil.lineSeparator);
        buff.append("</stories>");

        File f = new File(dir + CONTENT);
        FileUtil.writeFile(f, buff.toString(), "utf-8");

        return f;
    }

    /**
     * Sets hyperlink ids in designmap.xml content.
     */
    private void setHyperlinkIds(String content)
    {
        Pattern p = Pattern
                .compile("<Hyperlink [^>]*DestinationUniqueKey=\"(\\d+)\"[^>]*>[\\s\\S]*?</Hyperlink>");
        Matcher m = p.matcher(content);
        while (m.find())
        {
            hyperlinkIds.add(m.group(1));
        }
    }
    
    private boolean includeTextVariable(String content)
    {
        Pattern p = Pattern
                .compile("<TextVariable [^>]*>([\\s\\S]*?)</TextVariable>");
        Matcher m = p.matcher(content);
        while (m.find())
        {
            String ccc = m.group(1);
            if (ccc.contains("<Contents type=\"string\">"))
            {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Gets hyperlink ids in designmap.xml content.
     */
    private static List<String> getHyperlinkIds(String content)
    {
        List<String> hyperlinkIds = new ArrayList<String>();
        Pattern p = Pattern
                .compile("<Hyperlink [^>]*DestinationUniqueKey=\"(\\d+)\"[^>]*>[\\s\\S]*?</Hyperlink>");
        Matcher m = p.matcher(content);
        while (m.find())
        {
            hyperlinkIds.add(m.group(1));
        }
        return hyperlinkIds;
    }

    private String optimizeForOddChar(String s)
    {
        /*
         * GBS-3619 : After discussion with Andy, what we expected is: 1. When
         * Ignore Forced Line Breaks option is enabled, soft return is
         * maintained and treated as tag, and all text will be merged into one
         * segment(by this way, DTP need not add soft return back in target
         * file). 2. When Ignore Forced Line Breaks option is not enabled, the
         * text is allowed to segment on soft return as before.
         */
        if (!isExtractLineBreak())
        {
            s = convertLineBreakToTag(s);
        }
        else
        {
            s = removeLineBreak(s);
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
        s = s.replace(LINE_BREAK, MARK_LineBreak_IDML);
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
    
    private boolean isTranslateHiddenLayer()
    {
        return getInddFilter().getTranslateHiddenLayer();
    }
    
    private boolean isTranslateMasterLayer()
    {
        return getInddFilter().getTranslateMasterLayer();
    }

    private boolean isTranslateFileInfo()
    {
        return getInddFilter().getTranslateFileInfo();
    }

    private boolean isTranslateHyperlinks()
    {
        return getInddFilter().getTranslateHyperlinks();
    }

    private boolean isExtractLineBreak()
    {
        return getInddFilter().getExtractLineBreak();
    }

    private boolean isReplaceNonbreakingSpace()
    {
        return getInddFilter().isReplaceNonbreakingSpace();
    }
    
    private boolean isSkipTrackingKerning()
    {
        return getInddFilter().getSkipTrackingKerning();
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
        boolean isTranslateHiddenLayer = isTranslateHiddenLayer();
        boolean isTranslateMaster = isTranslateMasterLayer();

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
        String saveFileName = FileUtils.concatPath(m_saveDir, getCategory().getValue("relSafeName"));
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
            String temContent = StringUtil.replaceWithRE(content,
                    "<ParagraphStyleRange[^>]*/>", "");

            while (temContent.split(PARAGRAPH_START).length != temContent
                    .split(PARAGRAPH_END).length + 1)
            {
                n2 = temp.indexOf(PARAGRAPH_END, n2 + 1);
                if (n2 < 0)
                    break;

                content = temp.substring(index, n2 + parL);
                temContent = StringUtil.replaceWithRE(content,
                        "<ParagraphStyleRange[^>]*/>", "");
            }

            String content2 = trimSpace(content);
            
            content2 = mergeTag(content2);
            
            temp = temp.replace(index, index + content.length(), content2);

            index = getIndexOfParaStart(temp, index + content2.length());
        }

        String range = temp.toString();
        IdmlTagHelper h = new IdmlTagHelper();
        return h.mergeTags(range);
    }

    private String mergeTag(String s)
    {
        if (!isSkipTrackingKerning())
        {
            return s;
        }

        if (!s.contains(tags_End))
        {
            return s;
        }

        if (!s.contains(ignored_atts[0]) && !s.contains(ignored_atts[1]) && !s.contains(ignored_atts[2]))
        {
            return s;
        }

        StringBuilder output = new StringBuilder();
        String lastGroup1 = "";
        int start = 0;
        
        int find = s.indexOf(tags_End, start);
        while (find != -1)
        {
            int i_lastCha = s.lastIndexOf(tag_Start, find);
            int i_nextContent = s.indexOf(content_Start, i_lastCha);

            int i_nextContentEnd = i_nextContent + content_Start.length();
            int m_end = find + tags_End.length();
            
            String group1 = "";
            
            if (i_lastCha < start)
            {
                group1 = s.substring(start, find);
                
                if ("".equals(lastGroup1))
                {
                    output.append(s.substring(start, find));
                }
                // theset two tag is not near
                else
                {
                    output.append(tags_End);
                    output.append(s.substring(start, find));
                }
            }
            else
            {
                group1 = s.substring(i_lastCha, i_nextContentEnd);
                String group2 = s.substring(i_nextContentEnd, find);
                group1 = removeAtt(group1, ignored_atts);
                
                if ("".equals(lastGroup1))
                {
                    output.append(s.substring(start, i_lastCha));
                    output.append(group1);
                    output.append(group2);
                }
                // theset two tag is not near
                else if (i_lastCha > start)
                {
                    output.append(tags_End);
                    output.append(s.substring(start, i_lastCha));
                    output.append(group1);
                    output.append(group2);

                }
                else
                {
                    if (group1.equals(lastGroup1))
                    {
                        output.append(group2);
                    }
                    else
                    {
                        output.append(tags_End);
                        output.append(s.substring(start, i_lastCha));
                        output.append(group1);
                        output.append(group2);
                    }
                }
            }

            // Back up one character so we include the '<' we stopped on
            // in our next search
            start = m_end;
            lastGroup1 = group1;

            find = s.indexOf(tags_End, start);
        }
        // Write out the leftovers
        if (start != 0)
        {
            output.append(tags_End);
        }
        output.append(s.substring(start));

        return output.toString();

    }

    private static String removeAtt(String lastGroup1, String[] atts)
    {
        String result = lastGroup1;
        for (String att : atts)
        {
            String re = " " + att + "=\"[^\"]*?\"";
            
            result = StringUtil.replaceWithRE(result, re, "");
        }
        
        return result;
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
    
    /**
     * Removes space if it is not in content tag.
     * <p>
     * GBS-3168
     */
    private String trimSpace(String s)
    {
        StringBuilder output = new StringBuilder();
        Pattern p = Pattern.compile("<[^>]*?>([\\t\\n]+?)<");
        Matcher m = p.matcher(s);

        int start = 0;
        while (m.find(start))
        {
            String content = m.group();
            if (!content.startsWith("<Content>"))
            {
                // Copy everything from starting point to the start of the
                // captured whitespace group. Don't write out the trailing
                // '<' yet, as it will be picked up in the next search.
                output.append(s.substring(start, m.start(1)));
            }
            else
            {
                // Otherwise, copy everything except the trailing '<'
                output.append(s.substring(start, m.end() - 1));
            }
            // Back up one character so we include the '<' we stopped on
            // in our next search
            start = m.end() - 1;
        }
        // Write out the leftovers
        output.append(s.substring(start));

        return output.toString();
    }

    private static String trimSpaceBeforeTag(String s, String tagRegex)
    {
        StringBuilder output = new StringBuilder();
        Pattern p = Pattern.compile(tagRegex);
        Matcher m = p.matcher(s);

        int start = 0;
        while (m.find(start))
        {
            String content = m.group();

            // no string before this tag
            if (m.start() == 0)
            {
                output.append(s.substring(start, m.end()));
            }
            else
            {
                // check if there is \n\t before this tag
                char cc = s.charAt(m.start() - 1);
                if (cc == '\n')// || cc == '\t')
                {
                    // append content before \n\t
                    output.append(s.substring(start, m.start() - 1));
                    // append content after \n\t
                    // output.append(s.substring(m.start(), m.end()));
                    output.append(content);
                }
                else
                {
                    // append all content
                    output.append(s.substring(start, m.end()));
                }
            }

            start = m.end();
        }
        // Write out the leftovers
        if (start < s.length())
        {
            output.append(s.substring(start));
        }
        
        return output.toString();
    }

    private static String trimSpaceAfterTags(String s, String tagRegex)
    {
        StringBuilder output = new StringBuilder();
        Pattern p = Pattern.compile(tagRegex);
        Matcher m = p.matcher(s);

        int start = 0;
        while (m.find(start))
        {
            output.append(s.substring(start, m.end()));
            if (m.end() == s.length())
            {
                start = m.end();
            }
            else
            {
                // check if there is \n\t before this tag
                char cc = s.charAt(m.end());
                if (cc == '\n')// || cc == '\t')
                {
                    start = m.end() + 1;
                }
                else
                {
                    start = m.end();
                }
            }
        }
        // Write out the leftovers
        if (start < s.length())
        {
            output.append(s.substring(start));
        }

        return output.toString();
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