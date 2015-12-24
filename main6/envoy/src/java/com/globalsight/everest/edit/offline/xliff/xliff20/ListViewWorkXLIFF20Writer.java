/**
 * Copyright 2009 Welocalize, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * 
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 */
package com.globalsight.everest.edit.offline.xliff.xliff20;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;

import com.globalsight.everest.edit.offline.AmbassadorDwUpConstants;
import com.globalsight.everest.edit.offline.AmbassadorDwUpException;
import com.globalsight.everest.edit.offline.XliffConstants;
import com.globalsight.everest.edit.offline.download.DownloadParams;
import com.globalsight.everest.edit.offline.page.OfflinePageData;
import com.globalsight.everest.edit.offline.page.OfflineSegmentData;
import com.globalsight.everest.edit.offline.xliff.xliff20.document.Data;
import com.globalsight.everest.edit.offline.xliff.xliff20.document.File;
import com.globalsight.everest.edit.offline.xliff.xliff20.document.Note;
import com.globalsight.everest.edit.offline.xliff.xliff20.document.Notes;
import com.globalsight.everest.edit.offline.xliff.xliff20.document.OriginalData;
import com.globalsight.everest.edit.offline.xliff.xliff20.document.Segment;
import com.globalsight.everest.edit.offline.xliff.xliff20.document.Source;
import com.globalsight.everest.edit.offline.xliff.xliff20.document.StateType;
import com.globalsight.everest.edit.offline.xliff.xliff20.document.Target;
import com.globalsight.everest.edit.offline.xliff.xliff20.document.Unit;
import com.globalsight.everest.edit.offline.xliff.xliff20.document.Xliff;
import com.globalsight.everest.edit.offline.xliff.xliff20.document.YesNo;
import com.globalsight.everest.edit.offline.xliff.xliff20.match.Match;
import com.globalsight.everest.edit.offline.xliff.xliff20.match.Matches;
import com.globalsight.everest.integration.ling.tm2.LeverageMatch;
import com.globalsight.everest.page.TemplatePart;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.tda.TdaHelper;
import com.globalsight.everest.tuv.TuImpl;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.ling.common.DiplomatBasicParserException;
import com.globalsight.ling.common.Text;
import com.globalsight.ling.docproc.IFormatNames;
import com.globalsight.ling.docproc.extractor.xliff.Extractor;
import com.globalsight.ling.docproc.extractor.xliff.XliffAlt;
import com.globalsight.ling.tm2.leverage.Leverager;
import com.globalsight.ling.tw.internal.InternalTextUtil;
import com.globalsight.ling.tw.internal.XliffInternalTag;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.terminology.termleverager.TermLeverageMatchResult;
import com.globalsight.util.FileUtil;
import com.globalsight.util.StringUtil;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.edit.GxmlUtil;
import com.globalsight.util.edit.SegmentUtil;

/**
 * This class is used to write a xliff 2.0 file for offline download.
 *
 */
public class ListViewWorkXLIFF20Writer implements XliffConstants
{
    static private final Logger logger = Logger
            .getLogger(ListViewWorkXLIFF20Writer.class);
    public static String XLIFF_ENCODING = FileUtil.UTF16LE;
    private OutputStreamWriter outputStream;
    private DownloadParams downloadParams;
    private OfflinePageData page;
    private Xliff xliff = new Xliff();
    private File file = new File();

    protected static String strEOL = "\r\n";
    public static final char NORMALIZED_LINEBREAK = '\n';
    private int altFlag = -100;

    /**
     * Writes the xliff 2.0 file to the outputString.
     * 
     * You can get more information about xliff 2.0 specification from
     * http://docs.oasis-open.org/xliff/xliff-core/v2.0/xliff-core-v2.0.html
     * 
     * @param downloadParams
     * @param page
     * @param outputStream
     * @param uiLocale
     * @throws IOException
     * @throws AmbassadorDwUpException
     */
    public void write(DownloadParams downloadParams, OfflinePageData page,
            OutputStream outputStream, Locale uiLocale) throws IOException,
            AmbassadorDwUpException
    {
        this.outputStream = new OutputStreamWriter(outputStream, XLIFF_ENCODING);
        this.downloadParams = downloadParams;
        this.page = page;
        FileUtil.writeBom(outputStream, XLIFF_ENCODING);
        writeXLF();

        JAXBContext context;
        try
        {
            context = JAXBContext.newInstance(Xliff.class);
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);
            m.setProperty(Marshaller.JAXB_ENCODING, XLIFF_ENCODING);

            StringWriter sw = new StringWriter();
            sw.getBuffer().toString();
            m.marshal(xliff, sw);

            String content = formatXliff(sw.getBuffer().toString());
            this.outputStream.write(content);
        }
        catch (JAXBException e)
        {
            logger.error(e);
        }

        this.outputStream.flush();
    }

    /**
     * The generated xliff file is not formatted. So we need to format it.
     * 
     * Notes that we cannot set the Marshaller.JAXB_FORMATTED_OUTPUT to true. If
     * we do that some space and line break will add to source and target
     * content.
     * 
     * @param content
     * @return
     */
    public static String formatXliff(String content)
    {
        List<String> notBreakTag = new ArrayList<String>();
        notBreakTag.add("/source");
        notBreakTag.add("/target");
        notBreakTag.add("/note");
        notBreakTag.add("/data");

        int i1 = content.indexOf("<", 2);
        int i2 = content.indexOf(">", i1);
        String xliff = content.substring(0, i2 + 1);

        // Change the short name of match to mtc.
        // This is a bug exist in JAXB 1.0 that we can not set the short name of
        // the name space.
        String regex = "xmlns:([^=]*)=\"urn:oasis:names:tc:xliff:matches:2.0\"";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(xliff);
        
        if (m.find())
        {
            String tag = m.group(1);
            content = content.replace("<" + tag + ":", "<mtc:");
            content = content.replace("</" + tag + ":", "</mtc:");
            
            xliff = xliff.replace(m.group(),
                    "xmlns:mtc=\"urn:oasis:names:tc:xliff:matches:2.0\"");
            xliff = xliff.replace("<xliff ", "\r\n<xliff ");
        }
        
        regex = "xmlns:([^=]*)=\"urn:oasis:names:tc:xliff:metadata:2.0\"";
        p = Pattern.compile(regex);
        m = p.matcher(xliff);
        if (m.find())
        {
            xliff = xliff.replace(m.group(),
                    "xmlns:mda=\"urn:oasis:names:tc:xliff:metadata:2.0\"");
        }

        content = content.substring(i2 + 1);
        StringBuffer sb = new StringBuffer(xliff);

        // update \n to \r\n in first note
        int i3 = content.indexOf("<unit");
        if (i3 > 0)
        {
            String note = content.substring(0, i3);
            note = StringUtil.replace(note, "\n", "\r\n");
            content = note + content.substring(i3);
        }

        boolean notFormat = false;
        for (int i = 0; i < content.length(); i++)
        {
            char c = content.charAt(i);
            if (notFormat == true)
            {
                if (c == '<')
                {

                    int j = content.indexOf(">", i + 1);
                    if (j > 0)
                    {
                        String s = content.substring(i + 1, j);
                        if ("/source".equals(s) || "/target".equals(s))
                        {
                            notFormat = false;
                        }
                    }
                }
            }
            else
            {
                if (c == '<')
                {
                    if (content.charAt(i + 1) != '/')
                    {
                        sb.append(strEOL);
                    }

                    int j = content.indexOf(">", i + 1);
                    if (j > 0)
                    {
                        String s = content.substring(i + 1, j);
                        if ("source".equals(s) || "target".equals(s))
                        {
                            notFormat = true;
                        }

                        if (content.charAt(i + 1) == '/'
                                && !notBreakTag.contains(s))
                        {
                            sb.append(strEOL);
                        }
                    }
                }
            }
            sb.append(c);
        }

        return sb.toString();
    }

    /**
     * Changes "_" to "-". For example, change "en_US" to "en-US".
     * 
     * @param locale
     * @return
     */
    protected String changeLocaleToXlfFormat(String locale)
    {
        if (locale == null)
            return locale;

        return locale.replace("_", "-");
    }

    /**
     * Generates the annotation.
     * 
     * @return
     * @throws Exception
     */
    private String getAnnotation() throws Exception
    {
        StringBuffer sb = new StringBuffer(strEOL);

        sb.append("# GlobalSight Download File").append(strEOL);

        // Activity Type. Should be removed
        sb.append("# Activity Type: ").append(downloadParams.getActivityType())
                .append(strEOL);

        // User name. Should be removed.
        String user = null;
        if (downloadParams.getUser() != null)
        {
            user = UserUtil.getUserNameById(downloadParams.getUser()
                    .getUserId());
        }
        else
        {
            if (downloadParams.getAutoActionNodeEmail() != null)
            {
                user = "";

                for (int x = 0; x < downloadParams.getAutoActionNodeEmail()
                        .size(); x++)
                {
                    String email = downloadParams.getAutoActionNodeEmail()
                            .get(x).toString();
                    email = email.replace("<", " - ").replace(">", "");
                    user += email;
                    if (x < downloadParams.getAutoActionNodeEmail().size() - 1)
                    {
                        user += ",";
                    }
                }
            }
        }
        if (user != null)
        {
            sb.append("# User name: ").append(user).append(strEOL);
        }

        // Accept time. Should be removed.
        String acceptTaskTime = getAcceptTaskTime(getTasks(downloadParams));
        sb.append("# Accept time: ").append(acceptTaskTime).append(strEOL);

        sb.append("# Encoding: ").append(XLIFF_ENCODING).append(strEOL);
        sb.append("# Document Format: ").append(page.getDocumentFormat())
                .append(strEOL);
        sb.append("# Placeholder Format: ").append(page.getPlaceholderFormat())
                .append(strEOL);
        sb.append("# Source Locale: ")
                .append(changeLocaleToXlfFormat(page.getSourceLocaleName()))
                .append(strEOL);
        sb.append("# Target Locale: ")
                .append(changeLocaleToXlfFormat(page.getTargetLocaleName()))
                .append(strEOL);
        sb.append("# Page ID: ").append(page.getPageId()).append(strEOL);
        sb.append("# Workflow ID: ").append(page.getWorkflowId())
                .append(strEOL);

        sb.append("# Task ID: ");
        if (page.getTaskIds() != null)
        {
            StringBuffer taskIds = new StringBuffer();
            for (Long taskId : page.getTaskIds())
            {
                taskIds.append(taskId).append(",");
            }
            sb.append(taskIds.substring(0, taskIds.length() - 1));
        }
        else
        {
            sb.append(page.getTaskId());
        }
        sb.append(strEOL);

        // Optional
        sb.append("# Exact Match word count: ")
                .append(page.getExactMatchWordCountAsString()).append(strEOL);
        sb.append("# Fuzzy Match word count: ")
                .append(page.getFuzzyMatchWordCountAsString()).append(strEOL);
        sb.append("# No Match word count: ")
                .append(page.getNoMatchWordCountAsString()).append(strEOL);

        sb.append("# Populate 100% Target Segments: ")
                .append(page.isPopulate100() ? "YES" : "NO").append(strEOL);

        // Server Instance ID
        if (page.getServerInstanceID() != null)
        {
            sb.append("# GlobalSight Instance ID: ")
                    .append(page.getServerInstanceID()).append(strEOL);
        }

        Workflow wf = ServerProxy.getWorkflowManager().getWorkflowById(
                Long.parseLong(page.getWorkflowId()));
        sb.append("# In-Context Match word count: ")
                .append(wf.getInContextMatchWordCount()).append(strEOL);

        sb.append("# Edit all: ").append(page.getDisplayTMEditType())
                .append(strEOL);

        if (downloadParams.getJob() != null)
        {
            TranslationMemoryProfile tmprofile = downloadParams.getJob()
                    .getL10nProfile().getTranslationMemoryProfile();
            sb.append("# GlobalSight TM Profile id: ")
                    .append(tmprofile.getId()).append(strEOL);
            sb.append("# GlobalSight TM Profile name: ")
                    .append(tmprofile.getName()).append(strEOL);
            sb.append("# GlobalSight Termbase: ")
                    .append(downloadParams.getJob().getL10nProfile()
                            .getProject().getTermbaseName()).append(strEOL);
        }

        if (downloadParams.getFileFormatId() != -1)
        {
            String format = downloadParams.getFileFormatId() == AmbassadorDwUpConstants.DOWNLOAD_FILE_FORMAT_OMEGAT ? "OmegaT"
                    : "Xliff";
            sb.append("# GlobalSight Offline Toolkit Format: ").append(format)
                    .append(strEOL);
        }

        return sb.toString();
    }

    private String getAcceptTaskTime(List<Task> tasks)
    {
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        StringBuffer acceptTime = new StringBuffer();
        for (Task task : tasks)
        {
            acceptTime.append(
                    dateFormat.format(task.getEstimatedAcceptanceDate()))
                    .append(",");
        }
        return acceptTime.substring(0, acceptTime.length() - 1);
    }

    private List<Task> getTasks(DownloadParams p_downloadParams)
    {
        List<Task> tasks = new ArrayList<Task>();
        List<Long> taskIds = p_downloadParams.getAllTaskIds();
        if (taskIds != null)
        {
            for (Long taskId : taskIds)
            {
                tasks.add(TaskHelper.getTask(taskId));
            }
        }
        else
        {
            long taskId = Long.parseLong(p_downloadParams.getTaskID());
            tasks.add(TaskHelper.getTask(taskId));
        }
        return tasks;
    }

    /**
     * Gets the skeleton from database.
     * 
     * @param tuIdList
     * @param skeletonMap
     */
    @SuppressWarnings("unchecked")
    private void getSkeleton(List<Long> tuIdList,
            HashMap<Long, String> skeletonMap)
    {
        StringBuffer sqlBuffer = new StringBuffer();
        sqlBuffer.append(" FROM TemplatePart as tp WHERE "
                + " tp.pageTemplate.typeValue = 'EXP' and tp.tuId in ( ");
        for (Long tuId : tuIdList)
        {
            sqlBuffer.append(tuId).append(",");
        }
        String hql = sqlBuffer.substring(0, sqlBuffer.length() - 1) + ")";
        List<TemplatePart> templatePartList = (List<TemplatePart>) HibernateUtil
                .search(hql);
        for (TemplatePart tp : templatePartList)
        {
            String richSkeletonString = tp.getSkeletonString();
            if (StringUtil.isNotEmpty(richSkeletonString)
                    && richSkeletonString.contains("<skeleton>")
                    && richSkeletonString.contains("</skeleton>"))
            {
                String skeleton = richSkeletonString.substring(
                        richSkeletonString.indexOf("<skeleton>") + 10,
                        richSkeletonString.indexOf("</skeleton>"));
                skeletonMap.put(tp.getTuId(), skeleton);
            }
        }
    }

    /**
     * Add the xliff file body. It includes all segments and tm matches.
     * 
     * @throws AmbassadorDwUpException
     */
    @SuppressWarnings("rawtypes")
    protected void addBody() throws AmbassadorDwUpException
    {
        boolean isTmx = true;
        int TMEditType = downloadParams.getTMEditType();
        if (downloadParams.getResInsOption() == AmbassadorDwUpConstants.MAKE_RES_ATNS
                || downloadParams.getResInsOption() == AmbassadorDwUpConstants.MAKE_RES_TMX_BOTH)
        {
            isTmx = false;
        }

        OfflineSegmentData osd = null;

        // Loads skeleton from databas.
        HashMap<Long, String> skeletonMap = new HashMap<Long, String>();
        if (downloadParams.isIncludeXmlNodeContextInformation())
        {
            List<Long> tuIdList = new ArrayList<Long>();
            Task task = TaskHelper.getTask(Long.parseLong(page.getTaskId()));
            long jobId = task.getJobId();
            for (ListIterator it = page.getSegmentIterator(); it.hasNext();)
            {
                osd = (OfflineSegmentData) it.next();
                tuIdList.add(osd.getTargetTuv().getTu(jobId).getId());
            }
            getSkeleton(tuIdList, skeletonMap);
        }

        // Adds segments.
        for (ListIterator it = page.getSegmentIterator(); it.hasNext();)
        {
            osd = (OfflineSegmentData) it.next();

            try
            {
                writeTranslationUnit(osd, isTmx, TMEditType, skeletonMap);
            }
            catch (Exception ex)
            {
                throw new AmbassadorDwUpException(ex);
            }
        }
    }

    /**
     * The sub id is like 38305:[l2]:1. The [ and ] is not allowed.
     * 
     * @param id
     * @return
     */
    private String getXliffId(String id)
    {
        id = StringUtil.replace(id, ":[", ":-");
        id = StringUtil.replace(id, "]:", "-:");
        return id;
    }

    /**
     * Adds translation unit to xliff files.
     * 
     * @param osd
     * @param isTmx
     * @param TMEditType
     * @param skeletonMap
     * @throws Exception
     */
    private void writeTranslationUnit(OfflineSegmentData osd, boolean isTmx,
            int TMEditType, HashMap<Long, String> skeletonMap) throws Exception
    {
        String srcSegment;
        String trgSegment;
        String dataType = osd.getDisplaySegmentFormat();

        // Special treatment for HTML.
        if (("html").equalsIgnoreCase(dataType))
        {
            if (!("text").equals(dataType))
            {
                dataType = osd.getSegmentType();
            }
        }

        InternalTextUtil util = new InternalTextUtil(new XliffInternalTag());

        srcSegment = osd.getDisplaySourceTextWithNewLinebreaks(String
                .valueOf(NORMALIZED_LINEBREAK));

        Task task = TaskHelper.getTask(Long.parseLong(page.getTaskId()));
        long jobId = task.getJobId();
        try
        {
            srcSegment = util.preProcessInternalText(srcSegment).getSegment();

            if (("xlf").equalsIgnoreCase(dataType))
            {
                String sourceLocal = ServerProxy.getLocaleManager()
                        .getLocaleById(osd.getSourceTuv().getLocaleId())
                        .getLocaleCode();
                srcSegment = SegmentUtil.restoreSegment(osd.getSourceTuv()
                        .getGxml(), sourceLocal);
            }
        }
        catch (DiplomatBasicParserException e)
        {
            logger.error(e.getMessage(), e);
        }

        trgSegment = osd.getDisplayTargetTextWithNewLineBreaks(String
                .valueOf(NORMALIZED_LINEBREAK));
        try
        {
            trgSegment = util.preProcessInternalText(trgSegment).getSegment();

            if (("xlf").equalsIgnoreCase(dataType))
            {
                // Added for "Populate 100% Target Segments" option, GBS-2796
                if (downloadParams.isPopulate100())
                {
                    String targetLocal = ServerProxy.getLocaleManager()
                            .getLocaleById(osd.getTargetTuv().getLocaleId())
                            .getLocaleCode();
                    trgSegment = SegmentUtil.restoreSegment(osd.getTargetTuv()
                            .getGxml(), targetLocal);
                }
                else
                {
                    trgSegment = srcSegment;
                }
            }
        }
        catch (DiplomatBasicParserException e)
        {
            logger.error(e.getMessage(), e);
        }

        Unit unit = new Unit();
        file.getUnitOrGroup().add(unit);

        // Handle edit type
        YesNo translate = YesNo.YES;
        if (TMEditType != AmbassadorDwUpConstants.TM_EDIT_TYPE_BOTH)
        {
            if (TMEditType == AmbassadorDwUpConstants.TM_EDIT_TYPE_100
                    && isInContextMatch(osd))
                translate = YesNo.NO;
            else if (TMEditType == AmbassadorDwUpConstants.TM_EDIT_TYPE_ICE
                    && isExactMatch(osd))
                translate = YesNo.NO;
			else if (TMEditType == AmbassadorDwUpConstants.TM_EDIT_TYPE_DENY
					&& (isExactMatch(osd) || isInContextMatch(osd))
					&& osd.isWriteAsProtectedSegment())
                translate = YesNo.NO;
            else
                translate = YesNo.YES;
        }
        unit.setTranslate(translate);
        unit.setId("u" + getXliffId(osd.getDisplaySegmentID()));

        OriginalData od = new OriginalData();

        Segment seg = new Segment();
        unit.getSegmentOrIgnorable().add(seg);
        seg.setState(getState(osd));
        if (osd.getDisplaySegmentID() != null)
        {
            seg.setId(getXliffId(osd.getDisplaySegmentID()));
        }

        // Add SID
        String sid = osd.getSourceTuv().getSid();
        if (sid != null && sid.length() > 0)
        {
            Notes notes = new Notes();
            Note note = new Note();
            note.setContent(sid);
            unit.setNotes(notes);
            notes.getNote().add(note);
            note.setCategory("SID");
        }

        if (isInContextMatch(osd))
        {
            Notes notes = unit.getNotes();
            if (notes == null)
            {
                notes = new Notes();
                unit.setNotes(notes);
            }

            Note note = new Note();
            note.setContent(osd.getDisplayMatchType());
            note.setCategory("MatchType");
            notes.getNote().add(note);
        }

        // Adds source.
        if (srcSegment != null)
        {
            Source s = new Source();
            seg.setSource(s);

            Tmx2Xliff20Handler handler = new Tmx2Xliff20Handler();
            handler.setSource(true);
            Tmx2Xliff20 tmxXliff = new Tmx2Xliff20();
            tmxXliff.setHandler(handler);
            tmxXliff.parse(srcSegment);

            s.getContent().addAll(handler.getResult());

            List<Data> data = tmxXliff.getDatas();
            od.getData().addAll(data);
        }

        // Adds target.
        if (trgSegment != null)
        {
            Target t = new Target();
            seg.setTarget(t);

            Tmx2Xliff20Handler handler = new Tmx2Xliff20Handler();
            handler.setSource(false);
            Tmx2Xliff20 tmxXliff = new Tmx2Xliff20();
            tmxXliff.setHandler(handler);
            tmxXliff.parse(trgSegment);

            t.getContent().addAll(tmxXliff.getResult());
            List<Data> data = tmxXliff.getDatas();
            od.getData().addAll(data);
        }

        if (od.getData().size() > 0)
        {
            unit.setOriginalData(od);
        }

        // Adds skeleton
        if (downloadParams.isIncludeXmlNodeContextInformation())
        {
            long tuId = osd.getTargetTuv().getTu(jobId).getId();

            if (StringUtil.isNotEmpty(skeletonMap.get(tuId)))
            {
                Notes notes = unit.getNotes();
                if (notes == null)
                {
                    notes = new Notes();
                    unit.setNotes(notes);
                }

                Note note = new Note();
                note.setContent(EditUtil.decodeXmlEntities(skeletonMap
                        .get(tuId)));
                notes.getNote().add(note);
            }
        }

        // Adds tm matches and terminology.
        if (!isTmx)
        {
            writeTmMatch(osd, jobId, unit);
            writeTerminology(osd, unit);
        }
    }

    /**
     * The state is always "new" in below cases: 1. For xliff 2.0, if
     * "Populate 100% Target Segments" is not checked; 2. For OmegaT, it has no
     * "Populate 100% Target Segments" option on UI, always use
     * "isPopulate100=false" setting.
     */
    private StateType getState(OfflineSegmentData data)
    {
        if (downloadParams.isPopulate100())
        {
            if (isInContextMatch(data) || isExactMatch(data))
            {
                return StateType.FINAL;
            }
            else if (isPenaltiedExtarctMatch(data))
            {
                return StateType.REVIEWED;
            }
            else if (isFuzzyMatch(data))
            {
                return StateType.TRANSLATED;
            }
        }

        return StateType.INITIAL;
    }

    /**
     * Is incontext match or not.
     * 
     * @param data
     * @return
     */
    private boolean isInContextMatch(OfflineSegmentData data)
    {
        String matchType = data.getDisplayMatchType();
        if (matchType != null && matchType.startsWith("Context Exact Match"))
        {
            return true;
        }

        return false;
    }

    /**
     * Is extract match or not.
     * 
     * @param data
     * @return
     */
    private boolean isExactMatch(OfflineSegmentData data)
    {
        return "DO NOT TRANSLATE OR MODIFY (Locked).".equals(data
                .getDisplayMatchType());
    }

    /**
     * Is penaltied extract match or not.
     * 
     * @param data
     * @return
     */
    private boolean isPenaltiedExtarctMatch(OfflineSegmentData data)
    {
        return "Exact Match.".equals(data.getDisplayMatchType());
    }

    /**
     * Is fuzzy match or not.
     * 
     * @param data
     * @return
     */
    private boolean isFuzzyMatch(OfflineSegmentData data)
    {
        return data.getMatchTypeId() == AmbassadorDwUpConstants.MATCH_TYPE_FUZZY;
    }

    /**
     * Adds tm matches to xliff file.
     * 
     * @param osd
     * @param jobId
     * @param unit
     * @throws IOException
     */
    @SuppressWarnings(
    { "unchecked", "rawtypes" })
    private void writeTmMatch(OfflineSegmentData osd, long jobId, Unit unit)
            throws IOException
    {
        List<LeverageMatch> list = osd.getOriginalFuzzyLeverageMatchList();
        List<LeverageMatch> list2 = new ArrayList<LeverageMatch>();

        if (list != null)
        {
            list2.addAll(list);
        }

        Tuv tuv = ServerProxy.getTuvManager().getTuvForSegmentEditor(
                osd.getTrgTuvId(), jobId);
        Tuv sourceTuv = osd.getSourceTuv();
        Set xliffAltSet = tuv.getXliffAlt(true);
        if (xliffAltSet != null && xliffAltSet.size() > 0)
        {
            Iterator it = xliffAltSet.iterator();

            while (it.hasNext())
            {
                XliffAlt alt = (XliffAlt) it.next();
                LeverageMatch lm = new LeverageMatch();
                if (sourceTuv != null)
                {
                    lm.setOriginalSourceTuvId(sourceTuv.getId());
                }
                String str = EditUtil.decodeXmlEntities(alt.getSourceSegment());
                float score = (float) TdaHelper
                        .PecentToDouble(alt.getQuality());

                lm.setMatchedOriginalSource(str);
                lm.setMatchedText(EditUtil.decodeXmlEntities(alt.getSegment()));
                lm.setScoreNum(score);
                lm.setProjectTmIndex(altFlag);
                list2.add(lm);
            }
        }

        if (list2 != null)
        {
            LeverageMatch.orderMatchResult(list2);

            Matches ms = new Matches();

            for (int i = 0; i < list2.size(); i++)
            {
                LeverageMatch leverageMatch = list2.get(i);
                Match m = getMatch(leverageMatch, osd, jobId);
                if (m != null)
                {
                    ms.getMatch().add(m);
                }
            }

            if (ms.getMatch().size() > 0)
            {
                unit.setMatches(ms);
            }
        }
    }

    /**
     * If the osd is null, this method is called by the PageTemplate, use for
     * writing leverage match results into exported xliff file
     */
    public Match getMatch(LeverageMatch leverageMatch, OfflineSegmentData osd,
            long p_jobId)
    {
        String altTrans = new String();

        // altFlag to flag the leverageMatch form alt-trans
        int altFlag = -100;

        /**
         * Retrieve the source segment from TM if the leverage match is from
         * local TM. Default is string in source TUV.
         */
        long matchedTmTuvId = leverageMatch.getMatchedTuvId();

        try
        {
            Tuv sourceTuv = ServerProxy.getTuvManager().getTuvForSegmentEditor(
                    leverageMatch.getOriginalSourceTuvId(), p_jobId);
            String targetLocal = new String();
            String sourceLocal = new String();
            boolean isFromXliff = false;

            if (leverageMatch.getProjectTmIndex() != altFlag)
            {
                sourceLocal = ServerProxy.getLocaleManager()
                        .getLocaleById(sourceTuv.getLocaleId()).getLocaleCode();
                targetLocal = leverageMatch.getTargetLocale().getLocaleCode();

                if (sourceTuv != null
                        && sourceTuv.getTu(p_jobId).getDataType()
                                .startsWith(IFormatNames.FORMAT_XLIFF))
                {
                    isFromXliff = true;
                }
            }

            String sourceStr = leverageMatch.getMatchedOriginalSource();
            String targetStr = leverageMatch.getLeveragedTargetString();

            if (leverageMatch.getProjectTmIndex() == Leverager.TDA_TM_PRIORITY)
            {
                // not do anything
            }
            else if (leverageMatch.getProjectTmIndex() == Leverager.REMOTE_TM_PRIORITY)
            {
                sourceStr = GxmlUtil.stripRootTag(sourceStr);
            }
            else if (leverageMatch.getProjectTmIndex() == Leverager.MT_PRIORITY)
            {
                if (isFromXliff)
                {
                    sourceStr = SegmentUtil.restoreSegment(sourceStr,
                            sourceLocal);
                    targetStr = SegmentUtil.restoreSegment(targetStr,
                            targetLocal);
                }
                else
                {
                    sourceStr = GxmlUtil.stripRootTag(sourceStr);
                }
            }
            else if (leverageMatch.getProjectTmIndex() == altFlag)
            {
                // for offline download, write out the alt-trans
                sourceStr = GxmlUtil.stripRootTag(sourceStr);
            }
            else if (leverageMatch.getProjectTmIndex() == Leverager.XLIFF_PRIORITY)
            {
                TuImpl tu = (TuImpl) sourceTuv.getTu(p_jobId);
                String xliffTarget = tu.getXliffTargetGxml().getTextValue();

                if (xliffTarget != null && Text.isBlank(xliffTarget))
                {
                    // is populate from alt-trans
                    sourceStr = GxmlUtil.stripRootTag(leverageMatch
                            .getMatchedOriginalSource());
                    sourceStr = EditUtil.decodeXmlEntities(sourceStr);
                    targetStr = EditUtil.decodeXmlEntities(targetStr);
                }
                else
                {
                    // is populate by the xliff target, need to return to the
                    // original
                    sourceStr = SegmentUtil.restoreSegment(sourceStr,
                            sourceLocal);
                    targetStr = SegmentUtil.restoreSegment(targetStr,
                            targetLocal);
                }

            }
            else
            {
                if (matchedTmTuvId == -1)
                {
                    if (osd == null)
                    {
                        sourceStr = SegmentUtil.restoreSegment(sourceStr,
                                sourceLocal);
                        targetStr = SegmentUtil.restoreSegment(targetStr,
                                targetLocal);
                    }
                    else
                    {
                        sourceStr = osd
                                .getDisplaySourceTextWithNewLinebreaks(String
                                        .valueOf(NORMALIZED_LINEBREAK));
                    }
                }
                else
                {
                    String srcTmTuvString = leverageMatch
                            .getMatchedOriginalSource();
                    // TmUtil.getSourceTextForTuv( leverageMatch.getTmId(),
                    // matchedTmTuvId, sourceTuv.getLocaleId());
                    sourceStr = GxmlUtil.stripRootTag(srcTmTuvString);
                }
            }

            // Adds source
            sourceStr = processInternalText(sourceStr);

            Match m = new Match();
            m.setRef("#" + getXliffId(osd.getDisplaySegmentID()));

            Source s = new Source();
            m.setSource(s);
            String score = StringUtil.formatPercent(
                    leverageMatch.getScoreNum(), 2);
            m.setMatchQuality(score);
            Tmx2Xliff20Handler handler = new Tmx2Xliff20Handler();
            handler.setSource(true);
            Tmx2Xliff20 tmxXliff = new Tmx2Xliff20();
            tmxXliff.setHandler(handler);
            tmxXliff.parse(sourceStr);
            s.getContent().addAll(handler.getResult());
            List<Data> data = tmxXliff.getDatas();

            OriginalData originalData = new OriginalData();
            originalData.getData().addAll(data);

            // Adds target
            Target t = new Target();
            m.setTarget(t);
            handler = new Tmx2Xliff20Handler();
            handler.setSource(false);
            tmxXliff = new Tmx2Xliff20();
            tmxXliff.setHandler(handler);
            tmxXliff.parse(targetStr);
            t.getContent().addAll(tmxXliff.getResult());
            List<Data> data2 = tmxXliff.getDatas();
            originalData.getData().addAll(data2);

            // Adds data
            if (originalData.getData().size() > 0)
            {
                m.setOriginalData(originalData);
            }

            // Sets origin
            String tmOrigin = getTmOrigin(leverageMatch, osd, sourceTuv,
                    p_jobId);
            if (tmOrigin != null && tmOrigin.length() > 0)
            {
                m.setOrigin(tmOrigin);
            }

            return m;
        }
        catch (Exception he)
        {
        }

        return null;
    }

    /**
     * Gets tm origin for the leverage match.
     * 
     * @param leverageMatch
     * @param osd
     * @param sourceTuv
     * @param p_jobId
     * @return
     */
    private String getTmOrigin(LeverageMatch leverageMatch,
            OfflineSegmentData osd, Tuv sourceTuv, long p_jobId)
    {
        String tmOrigin = "TM";

        // If target TUV is never changed, adjust the "origin".
        int projectTmIndex = leverageMatch.getProjectTmIndex();
        if (projectTmIndex < -1)
        {
            if (projectTmIndex == Leverager.MT_PRIORITY)
            {
                tmOrigin = leverageMatch.getMtName();

                if ("".equals(tmOrigin) || tmOrigin == null)
                {
                    tmOrigin = "MT";
                }
            }
            else if (projectTmIndex == Leverager.XLIFF_PRIORITY)
            {
                TuImpl tu = (TuImpl) sourceTuv.getTu(p_jobId);
                if (tu != null && tu.isXliffTranslationMT() && osd == null)
                {
                    tmOrigin = Extractor.IWS_TRANSLATION_MT;
                }
                else
                {
                    tmOrigin = "XLF";
                }
            }
            else if (projectTmIndex == Leverager.REMOTE_TM_PRIORITY)
            {
                tmOrigin = "REMOTE_TM";
            }
            else if (projectTmIndex == Leverager.TDA_TM_PRIORITY)
            {
                tmOrigin = "TDA";
            }
            else if (projectTmIndex == Leverager.PO_TM_PRIORITY)
            {
                tmOrigin = "PO";
            }
            else if (projectTmIndex == altFlag)
            {
                tmOrigin = "XLF Source";
            }
            else if (projectTmIndex == Leverager.IN_PROGRESS_TM_PRIORITY)
            {
                tmOrigin = "In Progress Translation";
            }
        }

        return tmOrigin;
    }

    /**
     * Processes internal text.
     * 
     * @param segment
     * @return
     */
    private String processInternalText(String segment)
    {
        InternalTextUtil util = new InternalTextUtil(new XliffInternalTag());
        try
        {
            return util.preProcessInternalText(segment).getSegment();
        }
        catch (DiplomatBasicParserException e)
        {
            logger.error(e.getMessage(), e);
        }

        return segment;
    }

    /**
     * Adds terminology to xliff file.
     * 
     * @param osd
     * @param unit
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    private void writeTerminology(OfflineSegmentData osd, Unit unit)
            throws IOException
    {
        List<TermLeverageMatchResult> matchs = osd.getTermLeverageMatchList();
        if (matchs == null)
            return;

        Matches ms = unit.getMatches();
        if (ms == null)
        {
            ms = new Matches();
        }

        for (TermLeverageMatchResult result : matchs)
        {
            String src = result.getSourceTerm();
            String tag = result.getFirstTargetTerm();

            while (tag != null)
            {
                Match m = new Match();
                m.setOrigin("terminology");
                m.setRef("#" + getXliffId(osd.getDisplaySegmentID()));

                Source source = new Source();
                source.getContent().add(src);
                m.setSource(source);

                Target target = new Target();
                target.getContent().add(tag);
                m.setTarget(target);

                ms.getMatch().add(m);
                tag = result.getNextTargetTerm();
            }
        }

        if (ms.getMatch().size() > 0)
        {
            unit.setMatches(ms);
        }
    }

    /**
     * Adds header to xliff file.
     * 
     * @throws Exception
     */
    private void addHeader() throws Exception
    {
        xliff.setVersion("2.0");

        String sLocale = page.getSourceLocaleName();
        if (sLocale != null)
        {
            sLocale = changeLocaleToXlfFormat(sLocale);
            xliff.setSrcLang(sLocale);
        }

        String tLocale = page.getTargetLocaleName();
        if (tLocale != null)
        {
            tLocale = changeLocaleToXlfFormat(tLocale);
            xliff.setTrgLang(tLocale);
        }

        xliff.getFile().add(file);

        file.setId("f1");
        file.setOriginal(page.getPageName());

        Notes notes = new Notes();
        Note note = new Note();
        note.setContent(getAnnotation());

        file.setNotes(notes);
        notes.getNote().add(note);
    }

    /**
     * Generates xliff 2.0 files according to xliff 2.0 specification.
     */
    public void writeXLF()
    {
        try
        {
            addHeader();
            addBody();
        }
        catch (Exception e)
        {
            logger.error(e);
            throw new AmbassadorDwUpException(e);
        }
    }
}
