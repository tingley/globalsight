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
package com.globalsight.everest.edit.offline.xliff;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Set;

import org.apache.log4j.Logger;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

import com.globalsight.diplomat.util.XmlUtil;
import com.globalsight.everest.comment.Comment;
import com.globalsight.everest.edit.offline.AmbassadorDwUpConstants;
import com.globalsight.everest.edit.offline.AmbassadorDwUpException;
import com.globalsight.everest.edit.offline.XliffConstants;
import com.globalsight.everest.edit.offline.download.DownloadParams;
import com.globalsight.everest.edit.offline.page.OfflinePageData;
import com.globalsight.everest.edit.offline.page.OfflineSegmentData;
import com.globalsight.everest.integration.ling.tm2.LeverageMatch;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.TemplatePart;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.taskmanager.TaskImpl;
import com.globalsight.everest.tda.TdaHelper;
import com.globalsight.everest.tuv.TuImpl;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.ling.common.DiplomatBasicParserException;
import com.globalsight.ling.common.RegExException;
import com.globalsight.ling.common.Text;
import com.globalsight.ling.docproc.IFormatNames;
import com.globalsight.ling.docproc.extractor.xliff.Extractor;
import com.globalsight.ling.docproc.extractor.xliff.XliffAlt;
import com.globalsight.ling.tm2.leverage.Leverager;
import com.globalsight.ling.tw.internal.InternalTextUtil;
import com.globalsight.ling.tw.internal.XliffInternalTag;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.terminology.termleverager.TermLeverageMatchResult;
import com.globalsight.util.StringUtil;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.edit.GxmlUtil;
import com.globalsight.util.edit.SegmentUtil;

public class ListViewWorkXLIFFWriter extends XLIFFWriterUnicode
{
    static private final Logger logger = Logger
            .getLogger(ListViewWorkXLIFFWriter.class);

    static public final String WORK_DOC_TITLE = "GlobalSight Extracted List-View Export";
    static private final String TERMINOLOGY_PATTERN = "<alt-trans origin=\"terminology\">\r\n"
            + "<source>{0}</source>\r\n"
            + "<target>{1}</target>\r\n"
            + "</alt-trans>\r\n";
    static private final String TM_PATTERN = "<alt-trans match-quality=\"{0}\" origin=\"TM\">\r\n"
            + "<source>{1}</source>\r\n"
            + "<target>{2}</target>\r\n"
            + "</alt-trans>\r\n";
    // for worldserver export
    static private final String TM_PATTERN1 = "<alt-trans match-quality=\"{0}\" origin=\"TM\">"
            + "<source>{1}</source>" + "<target>{2}</target>" + "</alt-trans>";

    static private final String PHASE = "<phase-group>\r\n"
            + "<phase phase-name=\"{0}\" process-name=\"{1}\"/>\r\n"
            + "</phase-group>\r\n";

    private static SAXReader reader = null;

    public ListViewWorkXLIFFWriter()
    {
    }

    protected void writeXlfDocBody(boolean isTmx, int TMEditType,
            DownloadParams p_downloadParams) throws AmbassadorDwUpException
    {
        OfflineSegmentData osd = null;

        HashMap<Long, String> skeletonMap = new HashMap<Long, String>();
        if (p_downloadParams.isIncludeXmlNodeContextInformation())
        {
            List<Long> tuIdList = new ArrayList<Long>();
            Task task = TaskHelper.getTask(Long.parseLong(m_page.getTaskId()));
            long jobId = task.getJobId();
            for (ListIterator it = m_page.getSegmentIterator(); it.hasNext();)
            {
                osd = (OfflineSegmentData) it.next();
                tuIdList.add(osd.getTargetTuv().getTu(jobId).getId());
            }
            getSkeleton(tuIdList, skeletonMap);
        }

        for (ListIterator it = m_page.getSegmentIterator(); it.hasNext();)
        {
            osd = (OfflineSegmentData) it.next();

            try
            {
                writeTranslationUnit(osd, m_page, isTmx, TMEditType,
                        p_downloadParams, skeletonMap);
            }
            catch (Exception ex)
            {
                throw new AmbassadorDwUpException(ex);
            }
        }
    }

    private void getSkeleton(List<Long> p_tuIdList,
            HashMap<Long, String> p_skeletonMap)
    {
        StringBuffer sqlBuffer = new StringBuffer();
        sqlBuffer.append(" FROM TemplatePart as tp WHERE "
                + " tp.pageTemplate.typeValue = 'EXP' and tp.tuId in ( ");
        for (Long tuId : p_tuIdList)
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
                p_skeletonMap.put(tp.getTuId(), skeleton);
            }
        }
    }

    protected void writeXlfDocHeader(DownloadParams p_downloadParams)
            throws IOException, AmbassadorDwUpException
    {
        writeDocumentHeader(p_downloadParams);
    }

    private void writeAltTranslationUnit(OfflineSegmentData p_osd, long p_jobId)
            throws IOException
    {
        writeTmMatch(p_osd, p_jobId);
        writeTerminology(p_osd);
    }

    private String repairForAltTrans(String segment, SAXReader reader)
    {
        try
        {
            reader.read(new StringReader("<target>" + segment + "</target>"));
        }
        catch (DocumentException e)
        {
            return EditUtil.encodeXmlEntities(segment);
        }

        return segment;
    }

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

    /*
     * If the osd is null, this method is called by the PageTemplate, use for
     * writing leverage match results into exported xliff file
     */
    public String getAltByMatch(LeverageMatch leverageMatch,
            OfflineSegmentData osd, SAXReader reader, long p_jobId)
    {
        String altTrans = new String();

        // altFlag to flag the leverageMatch form alt-trans
        int altFlag = -100;
        String score = StringUtil.formatPercent(leverageMatch.getScoreNum(), 2);

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

            sourceStr = processInternalText(sourceStr);
            targetStr = processInternalText(targetStr);

            if (osd == null)
            {
                altTrans = MessageFormat.format(TM_PATTERN1, score,
                        repairForAltTrans(sourceStr, reader),
                        repairForAltTrans(targetStr, reader));
            }
            else
            {
                altTrans = MessageFormat.format(TM_PATTERN, score,
                        repairForAltTrans(sourceStr, reader),
                        repairForAltTrans(targetStr, reader));
            }

            // If target TUV is never changed, adjust the "origin".
            int projectTmIndex = leverageMatch.getProjectTmIndex();
            if (projectTmIndex < -1)
            {
                if (projectTmIndex == Leverager.MT_PRIORITY)
                {
                    String origin = leverageMatch.getMtName();

                    if ("".equals(origin) || origin == null)
                    {
                        origin = "MT";
                    }

                    altTrans = altTrans.replace("origin=\"TM\"", "origin=\""
                            + origin + "\"");
                }
                else if (projectTmIndex == Leverager.XLIFF_PRIORITY)
                {
                    TuImpl tu = (TuImpl) sourceTuv.getTu(p_jobId);
                    if (tu != null && tu.isXliffTranslationMT() && osd == null)
                    {
                        String temp = Extractor.IWS_TRANSLATION_MT;
                        altTrans = altTrans.replace("origin=\"TM\"",
                                "origin=\"" + temp + "\"");
                    }
                    else
                    {
                        altTrans = altTrans.replace("origin=\"TM\"",
                                "origin=\"XLF\"");
                    }
                }
                else if (projectTmIndex == Leverager.REMOTE_TM_PRIORITY)
                {
                    altTrans = altTrans.replace("origin=\"TM\"",
                            "origin=\"REMOTE_TM\"");
                }
                else if (projectTmIndex == Leverager.TDA_TM_PRIORITY)
                {
                    altTrans = altTrans.replace("origin=\"TM\"",
                            "origin=\"TDA\"");
                }
                else if (projectTmIndex == Leverager.PO_TM_PRIORITY)
                {
                    altTrans = altTrans.replace("origin=\"TM\"",
                            "origin=\"PO\"");
                }
                else if (projectTmIndex == altFlag)
                {
                    altTrans = altTrans.replace("origin=\"TM\"",
                            "origin=\"XLF Source\"");
                }
                else if (projectTmIndex == Leverager.IN_PROGRESS_TM_PRIORITY)
                {
                    altTrans = altTrans.replace("origin=\"TM\"",
                            "origin=\"In Progress Translation\"");
                }
            }
        }
        catch (Exception he)
        {
        }

        return altTrans;
    }

    private void writeTmMatch(OfflineSegmentData p_osd, long p_jobId)
            throws IOException
    {
        List<LeverageMatch> list = p_osd.getOriginalFuzzyLeverageMatchList();
        List<LeverageMatch> list2 = new ArrayList<LeverageMatch>();

        if (list != null)
        {
            list2.addAll(list);
        }

        Tuv tuv = ServerProxy.getTuvManager().getTuvForSegmentEditor(
                p_osd.getTrgTuvId(), p_jobId);
        Tuv sourceTuv = p_osd.getSourceTuv();

        Set xliffAltSet = tuv.getXliffAlt(true);
        int altFlag = -100;

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

            for (int i = 0; i < list2.size(); i++)
            {
                LeverageMatch leverageMatch = list2.get(i);
                m_outputStream.write(getAltByMatch(leverageMatch, p_osd,
                        getSAXReader(), p_jobId));
            }
        }
    }

    private void writeTerminology(OfflineSegmentData osd) throws IOException
    {
        List<TermLeverageMatchResult> matchs = osd.getTermLeverageMatchList();
        if (matchs == null)
            return;

        for (TermLeverageMatchResult result : matchs)
        {
            String src = result.getSourceTerm();
            String tag = result.getFirstTargetTerm();
            while (tag != null)
            {
                String altTrans = MessageFormat.format(TERMINOLOGY_PATTERN,
                        repairForAltTrans(src, getSAXReader()),
                        repairForAltTrans(tag, getSAXReader()));
                m_outputStream.write(altTrans);
                tag = result.getNextTargetTerm();
            }
        }
    }

    private boolean isExactMatch(OfflineSegmentData data)
    {
        return "DO NOT TRANSLATE OR MODIFY (Locked).".equals(data
                .getDisplayMatchType());
    }

    private boolean isPenaltiedExtarctMatch(OfflineSegmentData data)
    {
        return "Exact Match.".equals(data.getDisplayMatchType());
    }

    private boolean isInContextMatch(OfflineSegmentData data)
    {
        String matchType = data.getDisplayMatchType();
        if (matchType != null && matchType.startsWith("Context Exact Match"))
        {
            return true;
        }

        return false;
    }

    private boolean isFuzzyMatch(OfflineSegmentData data)
    {
        return data.getMatchTypeId() == AmbassadorDwUpConstants.MATCH_TYPE_FUZZY;
    }

    /**
     * The state is always "new" in below cases: 1. For xliff 1.2, if
     * "Populate 100% Target Segments" is not checked; 2. For OmegaT, it has no
     * "Populate 100% Target Segments" option on UI, always use
     * "isPopulate100=false" setting.
     */
    private String getState(OfflineSegmentData data,
            DownloadParams p_downloadParams)
    {
        String state = "new";
        if (p_downloadParams.isPopulate100())
        {
            if (isInContextMatch(data) || isExactMatch(data))
            {
                state = "final";
            }
            else if (isPenaltiedExtarctMatch(data))
            {
                state = "translated";
            }
            else if (isFuzzyMatch(data))
            {
                state = "needs-review-translation";
            }
        }

        return state;
    }

    private void writeTranslationUnit(OfflineSegmentData p_osd,
            OfflinePageData m_page, boolean isTmx, int TMEditType,
            DownloadParams p_downloadParams, HashMap<Long, String> p_skeletonMap)
            throws IOException, RegExException
    {
        String srcSegment;
        String trgSegment;
        String dataType = p_osd.getDisplaySegmentFormat();

        // Special treatment for HTML.
        if (("html").equalsIgnoreCase(dataType))
        {
            if (!("text").equals(dataType))
            {
                dataType = p_osd.getSegmentType();
            }
        }

        InternalTextUtil util = new InternalTextUtil(new XliffInternalTag());

        srcSegment = p_osd.getDisplaySourceTextWithNewLinebreaks(String
                .valueOf(NORMALIZED_LINEBREAK));

        Task task = TaskHelper.getTask(Long.parseLong(m_page.getTaskId()));
        long jobId = task.getJobId();
        try
        {
            srcSegment = util.preProcessInternalText(srcSegment).getSegment();

            if (("xlf").equalsIgnoreCase(dataType))
            {
                String sourceLocal = ServerProxy.getLocaleManager()
                        .getLocaleById(p_osd.getSourceTuv().getLocaleId())
                        .getLocaleCode();
                srcSegment = SegmentUtil.restoreSegment(p_osd.getSourceTuv()
                        .getGxml(), sourceLocal);
            }
        }
        catch (DiplomatBasicParserException e)
        {
            logger.error(e.getMessage(), e);
        }

        trgSegment = p_osd.getDisplayTargetTextWithNewLineBreaks(String
                .valueOf(NORMALIZED_LINEBREAK));
        try
        {
            trgSegment = util.preProcessInternalText(trgSegment).getSegment();

            if (("xlf").equalsIgnoreCase(dataType))
            {
                // Added for "Populate 100% Target Segments" option, GBS-2796
                if (p_downloadParams.isPopulate100())
                {
                    String targetLocal = ServerProxy.getLocaleManager()
                            .getLocaleById(p_osd.getTargetTuv().getLocaleId())
                            .getLocaleCode();
                    trgSegment = SegmentUtil.restoreSegment(p_osd
                            .getTargetTuv().getGxml(), targetLocal);
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

        // write ID, match type, SID
        String sid = p_osd.getSourceTuv().getSid();
        if (p_osd.getDisplaySegmentID() != null)
        {
            m_outputStream.write("<trans-unit id=");
            m_outputStream.write(str2DoubleQuotation(String.valueOf(p_osd
                    .getDisplaySegmentID())));
            m_outputStream.write(" translate=");

            // Handle edit type
            if (TMEditType != AmbassadorDwUpConstants.TM_EDIT_TYPE_BOTH)
            {
                if (TMEditType == AmbassadorDwUpConstants.TM_EDIT_TYPE_100
                        && isInContextMatch(p_osd))
                    m_outputStream.write(str2DoubleQuotation("no"));
                else if (TMEditType == AmbassadorDwUpConstants.TM_EDIT_TYPE_ICE
                        && isExactMatch(p_osd))
                    m_outputStream.write(str2DoubleQuotation("no"));
                else if (TMEditType == AmbassadorDwUpConstants.TM_EDIT_TYPE_DENY
                        && (isExactMatch(p_osd) || isInContextMatch(p_osd)))
                    m_outputStream.write(str2DoubleQuotation("no"));
                else
                    m_outputStream.write(str2DoubleQuotation("yes"));
            }
            else
                m_outputStream.write(str2DoubleQuotation("yes"));

            // SID
            if (sid != null && sid.length() > 0)
            {
                m_outputStream.write(" resname=\"SID\"");
            }

            if (p_downloadParams.isIncludeXmlNodeContextInformation())
            {
                long tuId = p_osd.getTargetTuv().getTu(jobId).getId();

                if (StringUtil.isNotEmpty(p_skeletonMap.get(tuId)))
                {
                    m_outputStream.write(" extradata=\"");
                    m_outputStream.write(p_skeletonMap.get(tuId));
                    m_outputStream.write("\"");
                }
            }

            m_outputStream.write(">");
            m_outputStream.write(m_strEOL);
        }

        // write Source
        if (srcSegment != null)
        {
            String s = XLIFFStandardUtil.convertToStandard(p_osd, srcSegment);
            m_outputStream.write("<source>");
            m_outputStream.write(s);
            m_outputStream.write("</source>");
            m_outputStream.write(m_strEOL);
        }

        // write Target, set state in target node
        if (trgSegment != null)
        {
            String s = XLIFFStandardUtil.convertToStandard(p_osd, trgSegment);
            m_outputStream.write("<target");
            m_outputStream.write(" state=");
            m_outputStream.write(str2DoubleQuotation(getState(p_osd,
                    p_downloadParams)));
            m_outputStream.write(">");
            m_outputStream.write(s);
            m_outputStream.write("</target>");
            m_outputStream.write(m_strEOL);
        }

        if (sid != null && sid.length() > 0)
        {
            m_outputStream.write("<note>");
            m_outputStream.write(sid);
            m_outputStream.write("</note>");
            m_outputStream.write(m_strEOL);
        }

        if (isInContextMatch(p_osd))
        {
            m_outputStream.write("<note>");
            m_outputStream.write("Match Type: " + p_osd.getDisplayMatchType());
            m_outputStream.write("</note>");
            m_outputStream.write(m_strEOL);
        }

        if (!isTmx)
        {
            // if (p_osd.getOriginalFuzzyLeverageMatchList() != null)
            // {
            writeAltTranslationUnit(p_osd, jobId);
            // }
        }

        m_outputStream.write("</trans-unit>");
        m_outputStream.write(m_strEOL);

    }

    /**
     * Writes the document header - encoding, formats, languages etc.
     */
    private void writeDocumentHeader(DownloadParams p_downloadParams)
            throws IOException, AmbassadorDwUpException
    {
        String fileName = str2DoubleQuotation(XmlUtil.escapeString(m_page
                .getPageName()));
        m_outputStream.write("<file ");

        if (m_page.getPageName() != null)
        {
            m_outputStream.write("original=" + fileName);
            m_outputStream.write(m_space);
        }

        String sLocale = m_page.getSourceLocaleName();
        if (sLocale != null)
        {
            sLocale = changeLocaleToXlfFormat(sLocale);
            m_outputStream.write("source-language="
                    + str2DoubleQuotation(sLocale));
            m_outputStream.write(m_space);
        }

        String tLocale = m_page.getTargetLocaleName();
        if (tLocale != null)
        {
            tLocale = changeLocaleToXlfFormat(tLocale);
            m_outputStream.write("target-language="
                    + str2DoubleQuotation(tLocale));
            m_outputStream.write(m_space);
        }

        // m_outputStream.write("tool="
        // + str2DoubleQuotation("Transware Ambassador"));
        // m_outputStream.write(m_space);

        if (m_page.getDocumentFormat() != null)
        {
            String docFormat = m_page.getDocumentFormat();

            if (docFormat.equals("multi-format"))
            {
                docFormat = "x-" + docFormat;
            }

            m_outputStream.write("datatype=" + str2DoubleQuotation(docFormat));
        }

        m_outputStream.write(">");
        m_outputStream.write(m_strEOL);
        writeAnnotation(p_downloadParams);
        m_outputStream.write("<body>");
        m_outputStream.write(m_strEOL);

    }

    private void writePhase(DownloadParams p_downloadParams) throws IOException
    {
        List<Task> tasks = getTasks(p_downloadParams);

        for (Task task : tasks)
        {
            String name = task.getTaskDisplayName();
            String typeName = "Translation";

            try
            {
                Activity act = ServerProxy.getJobHandler().getActivity(
                        task.getTaskName());

                int type = act.getActivityType();

                if (Activity.TYPE_REVIEW == type)
                {
                    typeName = "Review";
                }
                else if (TaskImpl.TYPE_REVIEW_EDITABLE == type)
                {
                    typeName = "Review Editable";
                }
            }
            catch (Exception e)
            {
                logger.error(e.getMessage(), e);
                throw new AmbassadorDwUpException(e);
            }

            String phase = MessageFormat.format(PHASE, name, typeName);
            m_outputStream.write(phase);
        }
    }

    private void writeAnnotation(DownloadParams p_downloadParams)
            throws IOException, AmbassadorDwUpException
    {
        String acceptTaskTime = getAcceptTaskTime(getTasks(p_downloadParams));

        m_outputStream.write("<header>");
        m_outputStream.write(m_strEOL);

        writePhase(p_downloadParams);

        m_outputStream.write("<note>");
        m_outputStream.write(m_strEOL);

        m_outputStream.write(XliffConstants.HASH_MARK);
        m_outputStream.write("GlobalSight Download File");
        m_outputStream.write(m_strEOL);

        m_outputStream.write(XliffConstants.HASH_MARK);
        m_outputStream.write("Activity Type:");
        m_outputStream.write(p_downloadParams.getActivityType());
        m_outputStream.write(m_strEOL);

        if (p_downloadParams.getUser() != null)
        {
            m_outputStream.write(XliffConstants.HASH_MARK);
            m_outputStream.write("User name:");
            m_outputStream.write(UserUtil.getUserNameById(p_downloadParams
                    .getUser().getUserId()));
            m_outputStream.write(m_strEOL);
        }
        else
        {
            if (p_downloadParams.getAutoActionNodeEmail() != null)
            {
                m_outputStream.write(XliffConstants.HASH_MARK);
                m_outputStream.write("User name:");

                for (int x = 0; x < p_downloadParams.getAutoActionNodeEmail()
                        .size(); x++)
                {
                    String email = p_downloadParams.getAutoActionNodeEmail()
                            .get(x).toString();
                    email = email.replace("<", " - ").replace(">", "");
                    m_outputStream.write(email);

                    if (x < p_downloadParams.getAutoActionNodeEmail().size() - 1)
                    {
                        m_outputStream.write(",");
                    }
                }

                m_outputStream.write(m_strEOL);
            }
        }

        m_outputStream.write(XliffConstants.HASH_MARK);
        m_outputStream.write("Accept time:");
        m_outputStream.write(acceptTaskTime);
        m_outputStream.write(m_strEOL);

        m_outputStream.write(XliffConstants.HASH_MARK);
        m_outputStream.write("Encoding:");
        // m_outputStream.write(m_page.getEncoding());
        m_outputStream.write(XLIFF_ENCODING);
        m_outputStream.write(m_strEOL);

        m_outputStream.write(XliffConstants.HASH_MARK);
        m_outputStream.write("Document Format:");
        m_outputStream.write(m_page.getDocumentFormat());
        m_outputStream.write(m_strEOL);

        m_outputStream.write(XliffConstants.HASH_MARK);
        m_outputStream.write("Placeholder Format:");
        m_outputStream.write(m_page.getPlaceholderFormat());
        m_outputStream.write(m_strEOL);

        m_outputStream.write(XliffConstants.HASH_MARK);
        m_outputStream.write("Source Locale:");
        m_outputStream.write(changeLocaleToXlfFormat(m_page
                .getSourceLocaleName()));
        m_outputStream.write(m_strEOL);

        m_outputStream.write(XliffConstants.HASH_MARK);
        m_outputStream.write("Target Locale:");
        m_outputStream.write(changeLocaleToXlfFormat(m_page
                .getTargetLocaleName()));
        m_outputStream.write(m_strEOL);

        m_outputStream.write(XliffConstants.HASH_MARK);
        m_outputStream.write("Page ID:");
        m_outputStream.write(m_page.getPageId());
        m_outputStream.write(m_strEOL);

        m_outputStream.write(XliffConstants.HASH_MARK);
        m_outputStream.write("Workflow ID:");
        m_outputStream.write(m_page.getWorkflowId());
        m_outputStream.write(m_strEOL);

        m_outputStream.write(XliffConstants.HASH_MARK);
        m_outputStream.write("Task ID:");
        if (m_page.getTaskIds() != null)
        {
            StringBuffer taskIds = new StringBuffer();
            for (Long taskId : m_page.getTaskIds())
            {
                taskIds.append(taskId).append(",");
            }
            m_outputStream.write(taskIds.substring(0, taskIds.length() - 1));
        }
        else
        {
            m_outputStream.write(m_page.getTaskId());
        }
        m_outputStream.write(m_strEOL);

        m_outputStream.write(XliffConstants.HASH_MARK);
        m_outputStream.write("Exact Match word count:");
        m_outputStream.write(m_page.getExactMatchWordCountAsString());
        m_outputStream.write(m_strEOL);

        m_outputStream.write(XliffConstants.HASH_MARK);
        m_outputStream.write("Fuzzy Match word count:");
        m_outputStream.write(m_page.getFuzzyMatchWordCountAsString());
        m_outputStream.write(m_strEOL);

        m_outputStream.write(XliffConstants.HASH_MARK);
        m_outputStream.write("No Match word count:");
        m_outputStream.write(m_page.getNoMatchWordCountAsString());
        m_outputStream.write(m_strEOL);

        m_outputStream
                .write(AmbassadorDwUpConstants.HEADER_POPULATE_100_SEGMENTS
                        + " " + (m_page.isPopulate100() ? "YES" : "NO"));
        m_outputStream.write(m_strEOL);

        // Server Instance ID
        if (m_page.getServerInstanceID() != null)
        {
            m_outputStream.write(XliffConstants.HASH_MARK);
            m_outputStream
                    .write(AmbassadorDwUpConstants.LABEL_SERVER_INSTANCEID
                            + ":");
            m_outputStream.write(m_page.getServerInstanceID());
            m_outputStream.write(m_strEOL);
        }

        Workflow wf = ServerProxy.getWorkflowManager().getWorkflowById(
                Long.parseLong(m_page.getWorkflowId()));
        m_outputStream.write(XliffConstants.HASH_MARK);
        m_outputStream.write("In-Context Match word count:");
        m_outputStream.write(wf.getInContextMatchWordCount() + "");
        m_outputStream.write(m_strEOL);

        m_outputStream.write(XliffConstants.HASH_MARK);
        m_outputStream.write("Edit all:");
        m_outputStream.write(m_page.getDisplayTMEditType());
        m_outputStream.write(m_strEOL);

        if (p_downloadParams.getJob() != null)
        {
            m_outputStream.write(XliffConstants.HASH_MARK);
            m_outputStream.write("GlobalSight TM Profile id:");
            TranslationMemoryProfile tmprofile = p_downloadParams.getJob()
                    .getL10nProfile().getTranslationMemoryProfile();
            m_outputStream.write(tmprofile.getId() + "");
            m_outputStream.write(m_strEOL);

            m_outputStream.write(XliffConstants.HASH_MARK);
            m_outputStream.write("GlobalSight TM Profile name:");
            m_outputStream.write(tmprofile.getName());
            m_outputStream.write(m_strEOL);

            m_outputStream.write(XliffConstants.HASH_MARK);
            m_outputStream.write("GlobalSight Termbase:");
            m_outputStream.write(p_downloadParams.getJob().getL10nProfile()
                    .getProject().getTermbaseName());
            m_outputStream.write(m_strEOL);
        }

        if (p_downloadParams.getFileFormatId() != -1)
        {
            m_outputStream.write(XliffConstants.HASH_MARK);
            m_outputStream.write(XliffConstants.GS_TOOLKIT_FORMAT);
            m_outputStream
                    .write(p_downloadParams.getFileFormatId() == AmbassadorDwUpConstants.DOWNLOAD_FILE_FORMAT_OMEGAT ? "OmegaT"
                            : "Xliff");
            m_outputStream.write(m_strEOL);
        }

        m_outputStream.write("</note>");
        m_outputStream.write(m_strEOL);

        // writeUserComments(p_downloadParams);

        m_outputStream.write("</header>");
        m_outputStream.write(m_strEOL);

    }

    @SuppressWarnings("unused")
    private void writeUserComments(DownloadParams p_downloadParams)
            throws IOException, AmbassadorDwUpException
    {
        List commentLists = getComments(getTasks(p_downloadParams));
        String comments = "";
        if (commentLists == null)
        {
            comments = "No user comments";
        }
        else
        {
            for (int i = 0; i < commentLists.size(); i++)
            {
                commentLists.get(i);
            }

        }

        m_outputStream.write("<note>");
        m_outputStream.write(m_strEOL);
        m_outputStream.write(comments);
        m_outputStream.write(m_strEOL);
        m_outputStream.write("</note>");
        m_outputStream.write(m_strEOL);

    }

    @SuppressWarnings("unchecked")
    private List getComments(List<Task> p_tasks)
    {
        List<Comment> comments = new ArrayList<Comment>();
        for (Task task : p_tasks)
        {
            Workflow wf = task.getWorkflow();
            Job job = wf.getJob();
            Iterator workflows = job.getWorkflows().iterator();
            while (workflows.hasNext())
            {
                Workflow t_wf = (Workflow) workflows.next();
                Hashtable tasks = t_wf.getTasks();
                for (Iterator i = tasks.values().iterator(); i.hasNext();)
                {
                    Task t = (Task) i.next();
                    comments.addAll(t.getTaskComments());
                }
            }
        }
        return comments;
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

    // parse string to "string"
    private String str2DoubleQuotation(String str)
    {
        String result = null;
        result = new StringBuffer().append("\"").append(str).append("\"")
                .toString();
        return result;
    }

    public void write(OfflinePageData p_page, OutputStream p_outputStream,
            Locale p_uiLocale) throws IOException, AmbassadorDwUpException
    {
        // TODO Auto-generated method stub

    }

    private static SAXReader getSAXReader()
    {
        if (reader == null)
        {
            reader = new SAXReader();
        }
        return reader;
    }
}
