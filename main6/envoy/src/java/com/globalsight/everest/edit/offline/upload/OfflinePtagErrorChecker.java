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

package com.globalsight.everest.edit.offline.upload;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.globalsight.everest.comment.Issue;
import com.globalsight.everest.comment.IssueHistory;
import com.globalsight.everest.edit.CommentHelper;
import com.globalsight.everest.edit.offline.AmbassadorDwUpConstants;
import com.globalsight.everest.edit.offline.AmbassadorDwUpException;
import com.globalsight.everest.edit.offline.OEMProcessStatus;
import com.globalsight.everest.edit.offline.OfflineEditManagerLocal;
import com.globalsight.everest.edit.offline.page.OfflinePageData;
import com.globalsight.everest.edit.offline.page.OfflineSegmentData;
import com.globalsight.everest.edit.offline.page.PageData;
import com.globalsight.everest.edit.offline.page.UploadIssue;
import com.globalsight.everest.edit.offline.xliff.XLIFFStandardUtil;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.persistence.tuv.SegmentTuTuvCacheManager;
import com.globalsight.everest.persistence.tuv.SegmentTuvUtil;
import com.globalsight.everest.tuv.TuImpl;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.everest.tuv.TuvState;
import com.globalsight.everest.webapp.pagehandler.administration.reports.generator.Cancelable;
import com.globalsight.everest.webapp.pagehandler.edit.online.PreviewPageHandler;
import com.globalsight.everest.webapp.pagehandler.edit.online.previewPDF.PreviewPDFHelper;
import com.globalsight.ling.common.DiplomatBasicParserException;
import com.globalsight.ling.common.XmlEntities;
import com.globalsight.ling.docproc.DiplomatAPI;
import com.globalsight.ling.tw.PseudoConstants;
import com.globalsight.ling.tw.PseudoData;
import com.globalsight.ling.tw.PseudoErrorChecker;
import com.globalsight.ling.tw.PseudoParserException;
import com.globalsight.ling.tw.TagNode;
import com.globalsight.ling.tw.TmxPseudo;
import com.globalsight.util.StringUtil;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.edit.GxmlUtil;
import com.globalsight.util.edit.SegmentUtil2;
import com.globalsight.util.gxml.GxmlElement;

public class OfflinePtagErrorChecker implements Cancelable
{
    static private final Logger CATEGORY = Logger
            .getLogger(OfflinePtagErrorChecker.class);

    private PtagErrorPageWriter m_errWriter = null;
    private ResourceBundle m_messages = null;

    private int TRADOS_REGX_SOURCE_PAREN = 1;
    private int TRADOS_REGX_TARGET_PAREN = 2;

    // Compile Regex used to detect and auto-clean Trados segments
    // A trados segment has the following form:
    //
    // {0> sourceText <}nn{> targetText <0}
    //
    // Where:
    // {0> is the begin marker
    // <}nn{> is the middle marker and "nn" is some number
    // <0} is the end marker
    //
    // This basic aexpresion will match:
    // ^{0>(.*)<}\d+{>(.*)<0}$
    //
    // below we add an allowance for white space within the marker syntax
    // and use escapes where necessary.
    static private final String tradosSegStart = "\\w*\\{\\s*0\\s*>";
    static private final String tradosSegSource = "(.*)";
    static private final String tradosSegMid = "<\\s*\\}\\s*\\d+\\s*\\{\\s*>";
    static private final String tradosSegTarget = "(.*)";
    static private final String tradosSegEnd = "<\\s*0\\s*\\}\\w*";
    static private final Pattern pattern = Pattern.compile(tradosSegStart
            + tradosSegSource + tradosSegMid + tradosSegTarget + tradosSegEnd,
            Pattern.DOTALL);

    static private final Pattern RE_SEGMENT_START = Pattern.compile(
            tradosSegStart, Pattern.DOTALL);
    static private final Pattern RE_SEGMENT_MID = Pattern.compile(tradosSegMid,
            Pattern.DOTALL);
    static private final Pattern RE_SEGMENT_END = Pattern.compile(tradosSegEnd,
            Pattern.DOTALL);

    private OEMProcessStatus status = null;
    private boolean cancel = false;
    private String filename = null;

    private XmlEntities xmlEncoder = new XmlEntities();

    /**
     * Maximum size of a segment in UTF-8 chars. Together these values can be
     * used to enable a gxml length check for our db. A length of zero disables
     * the check.
     */
    static private final int m_maxLengthGxml = 0; // disabled
    static private final String m_gxmlEncoding = "UTF8";

    /**
     * Maximum size of native-content. Together these values can be used to
     * enable a native content length check for the clients db. A length of zero
     * disables the check.
     */
    static private final int m_maxLengthNativeContent = 0; // disabled
    static private final String m_nativeContentEncoding = "UTF8";
    static private final List<String> stateList = new ArrayList<String>();
    static{
        stateList.add("translated");
        stateList.add("signed-off");
    	stateList.add("final");
//        stateList.add("needs-adaptation");
//        stateList.add("needs-l10n");
//        stateList.add("needs-review-adaptation");
//        stateList.add("needs-review-l10n");
//        stateList.add("needs-review-translation");
//        stateList.add("needs-translation");
    }

    /**
     * Constructor.
     */
    public OfflinePtagErrorChecker(PtagErrorPageWriter p_errWriter)
            throws AmbassadorDwUpException
    {
        m_messages = ResourceBundle
                .getBundle(
                        "com.globalsight.everest.edit.offline.upload.OfflinePtagErrorChecker",
                        p_errWriter.getLocale());

        m_errWriter = p_errWriter;
    }

    /**
     * Offline report file error checker for Translation Edit Report.
     */
    @SuppressWarnings(
    { "unchecked", "rawtypes" })
    public String checkAndSave(Map p_idSegmentMap, boolean p_adjustWS,
            long p_localeId, User p_user, long p_jobId) throws Exception
    {
        List tuvs = new ArrayList<Tuv>();
        Set tuIds = p_idSegmentMap.keySet();
        Iterator tuIdIterator = tuIds.iterator();
        long tuId = -1;
        Long tuIdLong = null;
        String segment = null;
        boolean isChanged = false;
        boolean hasError = false; // flag for loop
        boolean hasErrorFlag = false; // Once become true, never become back.
        PseudoData pTagData = new PseudoData();
        pTagData.setMode(PseudoConstants.PSEUDO_COMPACT);
        PseudoErrorChecker errorChecker = new PseudoErrorChecker();
        errorChecker.setEscapeResult(true);
        errorChecker.setStyles(SegmentUtil2.getTAGS());
        List<List<String>> errorInternalList = new ArrayList<List<String>>();

        int n = UploadApi.LOAD_DATA;
        int m = UploadApi.CHECK_SAVE / 2;
        int size = tuIds.size();
        int i = 0;
        while (tuIdIterator.hasNext())
        {
            if (cancel)
            {
                for (Object ob : tuvs)
                {
                    Tuv t = (Tuv) ob;
                    SegmentTuTuvCacheManager.removeTuvFromCache(t.getId());
                }
                ;
                return null;
            }

            i++;
            updateProcess(n + m * i / size);

            tuIdLong = (Long) tuIdIterator.next();
            tuId = tuIdLong.longValue();
            Tuv trgTuv = SegmentTuvUtil.getTuvByTuIdLocaleId(tuId, p_localeId,
                    p_jobId);
            TuImpl tu = (TuImpl) trgTuv.getTu(p_jobId);
            Tuv srcTuv = tu.getSourceTuv();
            String dataType = trgTuv.getDataType(p_jobId);
            pTagData.setAddables(dataType);
            // special treatment for html
            if ("html".equalsIgnoreCase(dataType)
                    && !"text".equalsIgnoreCase(tu.getTuType()))
            {
                pTagData.setAddables(tu.getTuType());
            }

            HashMap<String, String> allSrcGxmls = null;
            HashMap<String, String> allTrgGxmls = null;
            HashMap<String, String> allTrgCompactTrans = null;

            String errMsg = null;
            segment = (String) p_idSegmentMap.get(tuIdLong);
            if (segment != null && segment != "")
            {
                isChanged = false;
                hasError = false;
                allTrgCompactTrans = splitSegFromTERWithSubs(segment, tuId,
                        trgTuv);
                allSrcGxmls = getAllGxmls(srcTuv);
                allTrgGxmls = getAllGxmls(trgTuv);

                // Keep compatible with old report, loose the check
                if (allSrcGxmls.size() != allTrgCompactTrans.size()
                        && allTrgCompactTrans.size() > 1)
                {
                    hasError = true;
                    hasErrorFlag = true;
                    m_errWriter.addSegmentErrorMsg(String.valueOf(tuId),
                            "Missing main or sub translations");
                    continue;
                }

                for (int j = 0; j < allTrgCompactTrans.size(); j++)
                {
                    String key = String.valueOf(j);
                    String compactTrans = allTrgCompactTrans.get(key);
                    String sourceGxml = allSrcGxmls.get(key);
                    String targetGxml = allTrgGxmls.get(key);
                    TmxPseudo.tmx2Pseudo(targetGxml, pTagData);
                    String targetString = pTagData.getPTagSourceString();
                    TmxPseudo.tmx2Pseudo(sourceGxml, pTagData);
                    String sourceString = pTagData.getPTagSourceString();
                    // Compare original target and translation from TER report.
                    if (!targetString.equals(compactTrans))
                    {
                        isChanged = true;
                        pTagData.setPTagTargetString(compactTrans);
                        pTagData.setDataType(dataType);
                        // Tag check (TER translation VS source)
                        if ((errMsg = errorChecker.check(pTagData, "",
                                m_maxLengthGxml, m_gxmlEncoding,
                                m_maxLengthNativeContent,
                                m_nativeContentEncoding)) != null)
                        {
                            hasError = true;
                            hasErrorFlag = true;
                            m_errWriter.addSegmentErrorMsg(
                                    String.valueOf(tuId), errMsg.trim());
                        }
                        else
                        {
                            if (errorChecker.geStrInternalErrMsg().length() > 0)
                            {
                                List<String> seg = new ArrayList<String>();
                                seg.add(tuIdLong.toString());
                                seg.add(errorChecker.geStrInternalErrMsg());
                                errorInternalList.add(seg);
                            }

                            if (j == 0)// master segment
                            {
                                String newGxml = TmxPseudo.pseudo2Tmx(pTagData);

                                if ("xlf".equalsIgnoreCase(dataType))
                                {
                                    Vector srcTagList = pTagData
                                            .getSrcCompleteTagList();
                                    if (srcTagList != null
                                            && srcTagList.size() > 0)
                                    {
                                        Hashtable map = pTagData
                                                .getPseudo2TmxMap();
                                        Enumeration srcEnumerator = srcTagList
                                                .elements();
                                        while (srcEnumerator.hasMoreElements())
                                        {
                                            TagNode srcItem = (TagNode) srcEnumerator
                                                    .nextElement();
                                            String tagName = PseudoConstants.PSEUDO_OPEN_TAG
                                                    + srcItem.getPTagName()
                                                    + PseudoConstants.PSEUDO_CLOSE_TAG;

                                            if (newGxml.contains(tagName))
                                            {
                                                String oriTag = (String) map
                                                        .get(srcItem
                                                                .getPTagName());
                                                newGxml = newGxml.replace(
                                                        tagName, oriTag);
                                            }
                                        }
                                    }
                                }

                                // Set changed master segment
                                trgTuv.setGxmlExcludeTopTagsIgnoreSubflows(
                                        newGxml, p_jobId);
                                trgTuv.setLastModifiedUser(p_user.getUserId());
                                trgTuv.setState(TuvState.LOCALIZED);
                            }
                            pTagData.reset();
                            pTagData.resetMTIdentifierList();
                        }
                    }
                }

                if (isChanged && !hasError && allTrgCompactTrans.size() > 1)
                {
                    allTrgCompactTrans.remove("0");
                    // Set sub segments
                    Set<Entry<String, String>> entries = allTrgCompactTrans
                            .entrySet();
                    for (Entry<String, String> entry : entries)
                    {
                        String value = xmlEncoder.encodeStringBasic(entry
                                .getValue());
                        entry.setValue(value);
                    }
                    trgTuv.setSubflowsGxml(allTrgCompactTrans);
                }

                if (isChanged && !hasError)
                {
                    tuvs.add(trgTuv);
                }
            }
        }

        if (errorInternalList.size() > 0)
        {
            if (status != null)
            {
                status.setIsContinue(null);
                CheckResult r = new CheckResult();
                r.setErrorInternalList(errorInternalList);
                r.setFileName(m_errWriter.getFileName());
                // r.setTaskId(p_uploadPage.getTaskId());
                // r.setWorkflowId(p_uploadPage.getWorkflowId());
                // r.setPageId(p_uploadPage.getPageId());
                status.setCheckResult(r);
                status.setCheckResultCopy(r);

                Calendar c = Calendar.getInstance();
                int next = c.get(Calendar.HOUR_OF_DAY);
                c.set(Calendar.HOUR_OF_DAY, next + 1);
                Date d = c.getTime();

                OfflineEditManagerLocal manager = new OfflineEditManagerLocal();
                manager.startConfirm();
                try
                {
                    while (status.getIsContinue() == null)
                    {
                        Date d2 = new Date();
                        if (d2.after(d))
                        {
                            String msg = "One hour has passed waiting for user confirmation";
                            CATEGORY.info(msg);
                            return msg;
                        }

                        try
                        {
                            Thread.sleep(1000);
                        }
                        catch (InterruptedException e)
                        {
                            CATEGORY.error(e);
                        }
                    }

                }
                finally
                {
                    manager.endConfirm();
                }
            }
        }

        if (status.getIsContinue() != null && !status.getIsContinue())
        {
            status.getCheckResultCopy().setFileName(null);
            return "<div class='headingError'><bold>"
                    + "Page is cancelled for missing following internal tags</bold>"
                    + status.getCheckResultCopy().getMessage(false) + "</div>";
        }

        if (tuvs.size() > 0)
        {
            SegmentTuvUtil.updateTuvs(tuvs, p_jobId);
        }

        updateProcess(n + m + 5);

        Iterator tuvIterator = tuvs.iterator();
        Tuv tuvToUpdate = null;
        ArrayList<Long> targetPageIds = new ArrayList<Long>();

        n = n + m + 5;
        m -= 10;
        size = tuvs.size();
        i = 0;
        while (tuvIterator.hasNext())
        {
            i++;
            updateProcess(n + m * i / size);

            tuvToUpdate = (Tuv) tuvIterator.next();
            if (tuvToUpdate instanceof TuvImpl)
            {
                TuvImpl tuvImpl = (TuvImpl) tuvToUpdate;
                Long targetPageId = tuvImpl.getTargetPage(p_jobId)
                        .getIdAsLong();
                if (!targetPageIds.contains(targetPageId))
                {
                    targetPageIds.add(targetPageId);
                }
            }
        }

        // Delete the old files for preview
        if (tuvs.size() > 0)
        {
            for (Long targetId : targetPageIds)
            {
                PreviewPDFHelper.deleteOldPdf(targetId, p_localeId);
                PreviewPageHandler.deleteOldPreviewFile(targetId, p_localeId);
            }
        }

        updateProcess(UploadApi.LOAD_DATA + UploadApi.CHECK_SAVE);

        if (hasErrorFlag)
        {
            return m_errWriter.buildReportErroPage().toString();
        }
        return null;
    }

    /**
     * Offline error checker.
     * 
     * Compares an offline page to an internal offline reference page. In doing
     * so this method compares the ptags in the uploaded target segments to the
     * ptags in the reference source segments.
     * 
     * If successful, each segment in the upload page is converted back to gxml.
     * 
     * @param p_referencePage
     *            - the base file containing the original English text.
     * @param p_uploadPage
     *            - the upload file containing the translated text.
     * @return On error, we return an error message. If there are no errors we
     *         return null.
     */
    @SuppressWarnings(
    { "rawtypes", "unchecked", "static-access" })
    public String check(ArrayList<PageData> p_referencePages,
            OfflinePageData p_uploadPage, boolean p_adjustWS)
    {
        PseudoData pTagData = null;
        TmxPseudo convertor = null;
        PseudoErrorChecker errorChecker = null;
        String errMsg = null;
        boolean hasErr = false;
        int uploadPagePtagDisplayMode;
        List<String> errorList = new ArrayList<String>();
        List<List<String>> errorInternalList = new ArrayList<List<String>>();

        // Create PTag resources
        pTagData = new PseudoData();
        pTagData.setXliff20File(p_uploadPage.isXliff20());

        pTagData.setLocale(m_errWriter.getLocale());

        convertor = new TmxPseudo();
        errorChecker = new PseudoErrorChecker();
        errorChecker.setEscapeResult(true);
        errorChecker.setStyles(SegmentUtil2.getTAGS());
        // Confirm/get the ptag display mode.
        if (p_uploadPage.getPlaceholderFormat().equals(
                AmbassadorDwUpConstants.TAG_TYPE_PTAGV))
        {
            uploadPagePtagDisplayMode = PseudoConstants.PSEUDO_VERBOSE;
        }
        else if (p_uploadPage.getPlaceholderFormat().equals(
                AmbassadorDwUpConstants.TAG_TYPE_PTAGC))
        {
            uploadPagePtagDisplayMode = PseudoConstants.PSEUDO_COMPACT;
        }
        else
        {
            // InvalidTagModeError
            String label = AmbassadorDwUpConstants.LABEL_CURRENT_FMT;

            String args[] =
            { label, AmbassadorDwUpConstants.TAG_TYPE_PTAGV,
                    AmbassadorDwUpConstants.TAG_TYPE_PTAGC };

            errMsg = MessageFormat.format(
                    m_messages.getString("InvalidTagModeError"), args);

            m_errWriter.addFileErrorMsg(errMsg);

            return m_errWriter.buildPage().toString();
        }

        // ==========================
        // compare pages, segment-by-segment, driven by the reference page
        // ==========================
        OfflineSegmentData refSeg = null;
        OfflineSegmentData uploadSeg = null;
        OfflinePageData p_referencePage = null;
        HashMap oldIssueMap = new HashMap();
        HashMap segmentsMap = new HashMap();
        String currentTuId = null;
        String xlfTargetState = null;
        for (int ii = 0; ii < p_referencePages.size(); ii++)
        {
            p_referencePage = p_referencePages.get(ii).getOfflinePageData();

            if (p_referencePage.getIssuesMap() != null)
            {
                oldIssueMap.putAll(p_referencePage.getIssuesMap());
            }

            if (p_referencePage.getSegmentMap() != null)
            {
                segmentsMap.putAll(p_referencePage.getSegmentMap());
            }

            for (ListIterator it = p_referencePage.getSegmentIterator(); it
                    .hasNext();)
            {
                refSeg = (OfflineSegmentData) it.next();
                currentTuId = refSeg.getDisplaySegmentID();
                uploadSeg = p_uploadPage.getSegmentByDisplayId(currentTuId);
                xlfTargetState = p_uploadPage.getXlfTargetState(currentTuId);
                pTagData.setDataType(refSeg.getDisplaySegmentFormat());

                if (uploadSeg == null)
                {
                    continue;
                }

                uploadSeg.setReferenceSegmentFound(true);
                String tempUploadTargetDisplayText = uploadSeg
                        .getDisplayTargetText();
                boolean isIncludeSeparateFlag = false;// define IN loop
                try
                {
                    // flag protected segments
                    if (UploadPageSaver.confirmUploadProtection(uploadSeg))
                    {
                        refSeg.setWriteAsProtectedSegment(true);
                    }
                    else
                    {
                        refSeg.setWriteAsProtectedSegment(false);

                        // detect uncleared Trados segments
                        Matcher matcher = pattern
                                .matcher(tempUploadTargetDisplayText);
                        if (matcher.matches())
                        {
                            tempUploadTargetDisplayText = matcher
                                    .group(TRADOS_REGX_TARGET_PAREN);
                            String src = matcher
                                    .group(TRADOS_REGX_SOURCE_PAREN);

                            isIncludeSeparateFlag = isIncludeSeparateFlag(src);
                            if (isIncludeSeparateFlag)
                            {
                                errMsg = m_messages
                                        .getString("TradosSegmentationError");
                                m_errWriter.addSegmentErrorMsg(uploadSeg,
                                        errMsg);
                                hasErr = true;
                            }
                        }
                        else
                        {
                            isIncludeSeparateFlag = isIncludeSeparateFlag(tempUploadTargetDisplayText);
                            if (isIncludeSeparateFlag)
                            {
                                String args[] =
                                { "<,},0,{,>,<,0,}" };
                                errMsg = MessageFormat.format(
                                        m_messages.getString("TagsMissError"),
                                        args);

                                m_errWriter.addSegmentErrorMsg(uploadSeg,
                                        errMsg);
                                hasErr = true;
                            }
                        }

                        // Configure the PTag data object - this must
                        // be done prior to using the object with the
                        // conversion methods. Set placeholder mode
                        // as determined by the upload header.
                        pTagData.setMode(uploadPagePtagDisplayMode);

                        // - set addables as determined by reference segment
                        String disSegFormat = refSeg.getDisplaySegmentFormat();
                        String segType = refSeg.getSegmentType();
                        // default
                        pTagData.setAddables(disSegFormat);
                        // special treatment for html
                        if ("html".equalsIgnoreCase(disSegFormat)
                                && !"text".equals(segType))
                        {
                            pTagData.setAddables(segType);
                        }

                        // Compare previous ptag target with new one.
                        // Note: implementation of split/merge
                        // requires subflows to always be re-joined
                        // with targets.

                        // for xliff upload, x tag id issue
                        String oriTarget = refSeg.getDisplayTargetText();
                        String oriSource = refSeg.getDisplaySourceText();
                        PseudoData tuvPtagData = null;
                        boolean isXliff = (refSeg.getDisplaySegmentFormat()
                                .toLowerCase().equals("xlf")
                                && filename != null && filename.toLowerCase()
                                .endsWith(".xlf"));
                        if (isXliff)
                        {
                            // save tuv tags
                            tuvPtagData = new PseudoData();
                            tuvPtagData.setLocale(m_errWriter.getLocale());
                            tuvPtagData.setDataType("xlf");
                            tuvPtagData.setAddables("xlf");
                            tuvPtagData.setIsXliffXlfFile(isXliff);
                            tuvPtagData.setMode(uploadPagePtagDisplayMode);
                            convertor.tmx2Pseudo(oriSource, tuvPtagData);

                            // convert xliff standard to tmx standard
                            oriSource = XLIFFStandardUtil
                                    .convertToTmx(oriSource);
                            oriTarget = XLIFFStandardUtil
                                    .convertToTmx(oriTarget);

                            // avoid entity difference impacting
                            tempUploadTargetDisplayText = xmlEncoder
                                    .decodeStringBasic(tempUploadTargetDisplayText);
                        }

                        pTagData.setIsXliffXlfFile(isXliff);
                        convertor.tmx2Pseudo(oriTarget, pTagData);
                        String refTarget = pTagData.getPTagSourceString(); // intentional

                        // For GBS-608. I think the method refinePseudoTag is
                        // wrong. we can't replace all tags just by order.

                        convertor.tmx2Pseudo(oriSource, pTagData);
                        String refSource = pTagData.getPTagSourceString();
                        // For RTF, OmegaT, TTX etc (except for XLF format).
                        if (!p_uploadPage.isXliff()
                                && !isIncludeSeparateFlag
                                && !refTarget
                                        .equals(tempUploadTargetDisplayText))
                        {
                            uploadSeg.setTargetHasBeenEdited(true);
                        }
                        // for XLF format
                        if (p_uploadPage.isXliff() && !isIncludeSeparateFlag)
                        {
                            if (refSource.equals(tempUploadTargetDisplayText))
                            {
                                if (xlfTargetState != null && 
                                        stateList.contains(xlfTargetState.toLowerCase()))
                                {
                                    uploadSeg.setTargetHasBeenEdited(true);
                                    uploadSeg.setStateTranslated(true);
                                }
                            }
                            else if (!refTarget
                                    .equals(tempUploadTargetDisplayText))
                            {
                                uploadSeg.setTargetHasBeenEdited(true);
                            }
                        }
                        // for TTX only
                        if (ttxEmptySegment(uploadSeg))
                        {
                            hasErr = true;
                            uploadSeg.setTargetHasBeenEdited(false);
                            errMsg = m_messages.getString("TargetMissError");
                            m_errWriter.addSegmentErrorMsg(uploadSeg, errMsg);
                        }

                        pTagData.setPTagTargetString(tempUploadTargetDisplayText);
                        if ((errMsg = errorChecker.check(pTagData, "",
                                m_maxLengthGxml, m_gxmlEncoding,
                                m_maxLengthNativeContent,
                                m_nativeContentEncoding)) != null)
                        {
                            hasErr = true;
                            uploadSeg.setTagCheckSuccesful(false);
                            m_errWriter.addSegmentErrorMsg(uploadSeg,
                                    errMsg.trim());
                            errorList.add("Segment "
                                    + uploadSeg.getDisplaySegmentID()
                                    + " error : " + errMsg);
                        }
                        else
                        {
                            if (errorChecker.geStrInternalErrMsg().length() > 0)
                            {
                                // show cannot find target segment here :
                                // GBS-3001 issue
                                if (ttxEmptySegment(uploadSeg))
                                {
                                    // ignore me
                                }
                                else
                                {
                                    List<String> segment = new ArrayList<String>();
                                    segment.add(uploadSeg.getDisplaySegmentID());
                                    segment.add(errorChecker
                                            .geStrInternalErrMsg());
                                    errorInternalList.add(segment);
                                }
                            }

                            // for xliff of xliff format upload
                            // <bpt id="1" ctype="">&lt;a
                            // href="http://en.wikipedia.org/wiki/Field_bus"
                            // title="<sub ctype="x-xhtml-a-title" datatype="xhtml" xid="5" />"&gt;</bpt>
                            if (tuvPtagData != null)
                            {
                                Hashtable oriTags = pTagData.getPseudo2TmxMap();
                                Hashtable tuvTags = tuvPtagData
                                        .getPseudo2TmxMap();
                                Hashtable tuvNativeTags = tuvPtagData
                                        .getPseudo2NativeMap();
                                Hashtable tuvNativeTags2 = new Hashtable();

                                if (tuvNativeTags.size() > 0)
                                {
                                    if (tuvNativeTags.size() == oriTags.size())
                                    {
                                        tuvNativeTags2 = tuvNativeTags;
                                    }
                                    else if (tuvNativeTags.size() > oriTags
                                            .size())
                                    {
                                        List<String> inlineKeys = new ArrayList<String>();

                                        for (Object obj : tuvNativeTags
                                                .entrySet())
                                        {
                                            Map.Entry eee = (Map.Entry) obj;
                                            String eeekey = (String) eee
                                                    .getKey();
                                            String eeevalue = (String) eee
                                                    .getValue();
                                            boolean isInline = false;
                                            String newValue = "";
                                            for (Object obj2 : tuvNativeTags
                                                    .entrySet())
                                            {
                                                Map.Entry eee2 = (Map.Entry) obj2;
                                                String eee2key = (String) eee2
                                                        .getKey();
                                                String eee2value = (String) eee2
                                                        .getValue();
                                                if (eee2key.equals(eeekey))
                                                {
                                                    continue;
                                                }

                                                if (eeevalue.contains(eee2key))
                                                {
                                                    newValue = eeevalue
                                                            .replace(eee2key,
                                                                    eee2value);
                                                    isInline = true;
                                                    inlineKeys.add(eee2key);
                                                    break;
                                                }
                                            }

                                            if (isInline)
                                            {
                                                tuvNativeTags2.put(eeekey,
                                                        newValue);
                                            }
                                        }

                                        for (Object obj : tuvNativeTags
                                                .entrySet())
                                        {
                                            Map.Entry eee = (Map.Entry) obj;
                                            String eeekey = (String) eee
                                                    .getKey();
                                            String eeevalue = (String) eee
                                                    .getValue();
                                            if (!inlineKeys.contains(eeekey)
                                                    && !tuvNativeTags2
                                                            .containsKey(eeekey))
                                            {
                                                tuvNativeTags2.put(eeekey,
                                                        eeevalue);
                                            }
                                        }
                                    }

                                    Iterator vit = oriTags.values().iterator();
                                    List<String> oriValues = new ArrayList<String>();
                                    while (vit.hasNext())
                                    {
                                        String stt = (String) vit.next();
                                        String newstt = XLIFFStandardUtil
                                                .convertToStandard(uploadSeg,
                                                        stt);
                                        oriValues.add(newstt);
                                    }

                                    vit = tuvNativeTags2.values().iterator();
                                    List<String> tuvValues = new ArrayList<String>();
                                    while (vit.hasNext())
                                    {
                                        String stt = (String) vit.next();
                                        String newstt = XLIFFStandardUtil
                                                .convertToStandard(uploadSeg,
                                                        stt);
                                        tuvValues.add(newstt);
                                    }

                                    if (tuvValues.containsAll(oriValues))
                                    {
                                        Hashtable tuvTagsInline = new Hashtable();
                                        for (Object key : tuvTags.keySet())
                                        {
                                            Object tagValue = tuvTags.get(key);
                                            Object tagNativeValue = tuvNativeTags2
                                                    .get("[" + key + "]");

                                            if (tagNativeValue == null)
                                            {
                                                tuvTagsInline.put("[" + key
                                                        + "]", tagValue);
                                            }
                                        }

                                        for (Object key : tuvTags.keySet())
                                        {
                                            String tagValue = (String) tuvTags
                                                    .get(key);
                                            Object tagNativeValue = tuvNativeTags2
                                                    .get("[" + key + "]");

                                            if (tagNativeValue == null)
                                            {
                                                continue;
                                            }

                                            String tagNativeValueStr = XLIFFStandardUtil
                                                    .convertToStandard(
                                                            uploadSeg,
                                                            (String) tagNativeValue);

                                            for (Object orikey : oriTags
                                                    .keySet())
                                            {
                                                Object oritagValue = oriTags
                                                        .get(orikey);
                                                String oritagValueStr = XLIFFStandardUtil
                                                        .convertToStandard(
                                                                uploadSeg,
                                                                (String) oritagValue);

                                                // put new value from source tuv
                                                if (oritagValueStr
                                                        .equals(tagNativeValueStr))
                                                {
                                                    for (Object obj : tuvTagsInline
                                                            .entrySet())
                                                    {
                                                        Map.Entry eee = (Map.Entry) obj;
                                                        String eeekey = (String) eee
                                                                .getKey();
                                                        String eeevalue = (String) eee
                                                                .getValue();
                                                        if (tagValue
                                                                .contains(eeekey))
                                                        {
                                                            tagValue = tagValue
                                                                    .replace(
                                                                            eeekey,
                                                                            eeevalue);
                                                        }
                                                    }

                                                    pTagData.m_hPseudo2TmxMap
                                                            .put(orikey,
                                                                    tagValue);
                                                    pTagData.getPseudo2NativeMap()
                                                            .put("[" + orikey
                                                                    + "]",
                                                                    tagNativeValue);
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // If successful, set the new GXML string.
                            // The convertor will encode special characters user
                            // may have entered.
                            String newGxml = convertor.pseudo2Tmx(pTagData);

                            if (tuvPtagData == null
                                    && refSeg.getDisplaySegmentFormat()
                                            .toLowerCase().equals("xlf"))
                            {
                                PseudoData mTagData = new PseudoData();
                                mTagData.setLocale(m_errWriter.getLocale());
                                mTagData.setDataType("xlf");
                                mTagData.setAddables("xlf");
                                mTagData.setMode(uploadPagePtagDisplayMode);

                                String orisrc = refSeg.getSourceTuv()
                                        .getGxmlExcludeTopTags();
                                convertor.tmx2Pseudo(orisrc, mTagData);

                                Vector srcTagList = mTagData
                                        .getSrcCompleteTagList();

                                if (srcTagList != null && srcTagList.size() > 0)
                                {
                                    Hashtable map = mTagData.getPseudo2TmxMap();
                                    Enumeration srcEnumerator = srcTagList
                                            .elements();
                                    while (srcEnumerator.hasMoreElements())
                                    {
                                        TagNode srcItem = (TagNode) srcEnumerator
                                                .nextElement();
                                        String tagName = PseudoConstants.PSEUDO_OPEN_TAG
                                                + srcItem.getPTagName()
                                                + PseudoConstants.PSEUDO_CLOSE_TAG;

                                        if (newGxml.contains(tagName))
                                        {
                                            String oriTag = (String) map
                                                    .get(srcItem.getPTagName());
                                            newGxml = newGxml.replace(tagName,
                                                    oriTag);
                                        }
                                    }
                                }
                            }

                            if (p_adjustWS)
                            {
                                newGxml = EditUtil.adjustWhitespace(newGxml,
                                        refSeg.getDisplaySourceText());
                            }

                            uploadSeg.setDisplayTargetText(newGxml);
                        }
                    }
                }
                catch (PseudoParserException ex)
                {
                    String args[] =
                    { ex.toString() };
                    errMsg = MessageFormat.format(
                            m_messages.getString("PtagParseError"), args);

                    m_errWriter.addSegmentErrorMsg(uploadSeg, errMsg);
                    return m_errWriter.buildPage().toString();
                }
                catch (DiplomatBasicParserException ex)
                {
                    String args[] =
                    { ex.toString() };
                    errMsg = MessageFormat.format(
                            m_messages.getString("GXMLParseError"), args);

                    m_errWriter.addSegmentErrorMsg(uploadSeg, errMsg);
                    return m_errWriter.buildPage().toString();
                }
                catch (UploadPageSaverException ex)
                {
                    m_errWriter.addSegmentErrorMsg(refSeg,
                            ex.getMessage(m_errWriter.getLocale()));
                    return m_errWriter.buildPage().toString();
                }
                catch (Exception ex)
                {
                    m_errWriter.addSystemErrorMsg(uploadSeg, ex.toString());
                    return m_errWriter.buildPage().toString();
                }

                pTagData.reset();
                pTagData.resetMTIdentifierList();
            }
        }

        // Detect SegmentAddError.
        StringBuffer extraIds = null;
        ListIterator uploadSegIterator = p_uploadPage.getSegmentIterator();
        while (uploadSegIterator.hasNext())
        {
            uploadSeg = (OfflineSegmentData) uploadSegIterator.next();

            if (!uploadSeg.isReferenceSegmentFound())
            {
                hasErr = true;

                if (extraIds == null)
                {
                    extraIds = new StringBuffer();
                }

                extraIds.append("\n" + uploadSeg.getDisplaySegmentID());
            }
        }

        if (extraIds != null)
        {
            String args[] =
            { extraIds.toString() };

            errMsg = MessageFormat.format(
                    m_messages.getString("SegmentAddError"), args);

            m_errWriter.addSegmentErrorMsg(refSeg, errMsg.trim());
        }

        // Finally check uploaded issues. For now, we separate them
        // into new issues and replies to existing issues.
        int iNew = 0, iReplies = 0, iDiscard = 0;
        HashMap issues = p_uploadPage.getUploadedIssuesMap();

        for (Iterator it = issues.values().iterator(); it.hasNext();)
        {
            UploadIssue issue = (UploadIssue) it.next();

            String issueKey = makeIssueKey(issue);

            if (isNewIssue(oldIssueMap, issue, issueKey))
            {
                iNew++;

                p_uploadPage.addUploadedNewIssue(issue);
            }
            else if (isModifiedIssue(oldIssueMap, issue, issueKey))
            {
                iDiscard++;

                Issue refIssue = getReferenceIssue(oldIssueMap, issueKey);
                p_uploadPage.addUploadedReplyIssue(refIssue, issue);
            }
            else
            {
                // this is an untouched issue, discard.
                iDiscard++;
            }
        }

        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("Uploaded issues=" + issues.size() + ", new=" + iNew
                    + " replies=" + iReplies + " discarded=" + iDiscard);
        }

        // Then we fill in the missing TUV ID.
        fixIssueTuvs(segmentsMap, p_uploadPage);

        // Complete the error checking process.
        if (hasErr)
        {
            CATEGORY.info("Error happens with the following segments when uploading them.");
            CATEGORY.info("File Name = " + m_errWriter.getFileName()
                    + " Task Id = " + p_uploadPage.getTaskId() + " Page ID = "
                    + p_uploadPage.getPageId() + " Workflow ID = "
                    + p_uploadPage.getWorkflowId());
            for (int i = 0, len = errorList.size(); i < len; i++)
            {
                CATEGORY.info((String) errorList.get(i));
            }
            return m_errWriter.buildPage().toString();
        }
        else if (errorInternalList.size() > 0)
        {
            if (status != null)
            {
                status.setIsContinue(null);
                CheckResult r = new CheckResult();
                r.setErrorInternalList(errorInternalList);
                r.setFileName(m_errWriter.getFileName());
                r.setTaskId(p_uploadPage.getTaskId());
                r.setWorkflowId(p_uploadPage.getWorkflowId());
                r.setPageId(p_uploadPage.getPageId());
                status.setCheckResult(r);
                status.setCheckResultCopy(r);

                Calendar c = Calendar.getInstance();
                int n = c.get(Calendar.HOUR_OF_DAY);
                c.set(Calendar.HOUR_OF_DAY, n + 1);
                Date d = c.getTime();

                OfflineEditManagerLocal manager = new OfflineEditManagerLocal();
                manager.startConfirm();
                try
                {
                    while (status.getIsContinue() == null)
                    {
                        Date d2 = new Date();
                        if (d2.after(d))
                        {
                            String msg = "One hour has passed waiting for user confirmation";
                            CATEGORY.info(msg);
                            return msg;
                        }

                        try
                        {
                            Thread.sleep(1000);
                        }
                        catch (InterruptedException e)
                        {
                            CATEGORY.error(e);
                        }
                    }
                }
                finally
                {
                    manager.endConfirm();
                }

            }
        }

        p_uploadPage
                .setPlaceholderFormat(AmbassadorDwUpConstants.TAG_TYPE_GXML);

        return null;
    }

    private boolean ttxEmptySegment(OfflineSegmentData uploadSeg)
    {
        if (filename != null && filename.toLowerCase().endsWith(".ttx"))
        {
            if (StringUtil.isEmpty(uploadSeg.getDisplayTargetText())
                    && StringUtil.isEmpty(uploadSeg.getDisplaySourceText()))
            {
                return true;
            }
        }
        return false;
    }

    private boolean isIncludeSeparateFlag(String tempUploadTargetDisplayText)
    {
        // The display text not match the format "{0>
        // sourceText
        // <}n{> targetText
        // <0}", and include "{0>", "<}n{>" or "<0}", we
        // think there are some targets are missing.

        boolean isInclude = false;
        List<Pattern> res = new ArrayList<Pattern>();
        res.add(RE_SEGMENT_START);
        res.add(RE_SEGMENT_MID);
        res.add(RE_SEGMENT_END);

        for (Pattern pattern : res)
        {
            isInclude = pattern.matcher(tempUploadTargetDisplayText).find();
            if (isInclude)
            {
                break;
            }
        }
        return isInclude;
    }

    /**
     * Refine the segment content. Because some tags was missing during the
     * inconsistence, it causes the generated pseudo tag inconsistence.
     * 
     * @param str
     *            The sentence need to be refined.
     * @param refStr
     *            The reference sentence.
     * @return The refined sentence.
     */
    private String refinePseudoTag(String str, String refStr)
    {
        String resultStr = str;
        String regEx = "\\[[^\\[]+\\]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        Matcher m2 = p.matcher(refStr);
        String result, result2;
        while (m.find())
        {
            result = str.substring(m.start(), m.end());
            if (m2.find())
            {
                result2 = refStr.substring(m2.start(), m2.end());
                resultStr = resultStr.replace(result, result2);
            }
        }
        return resultStr;
    }

    /**
     * Gets a key for issues stored in the reference page's IssueMap.
     */
    private String makeIssueKey(UploadIssue p_issue)
    {
        StringBuffer sb = new StringBuffer();

        sb.append(p_issue.getTuId());
        sb.append(CommentHelper.SEPARATOR);
        sb.append(p_issue.getSubId());

        return sb.toString();
    }

    /**
     * Checks if the uploaded issue is new.
     */
    private Issue getReferenceIssue(HashMap oldIssues, String p_key)
    {
        return (Issue) oldIssues.get(p_key);
    }

    /**
     * Checks if the uploaded issue is new.
     */
    private boolean isNewIssue(HashMap oldIssues, UploadIssue p_issue,
            String p_key)
    {
        Issue issue = getReferenceIssue(oldIssues, p_key);

        // User has created a new issue.
        if (issue == null)
        {
            return true;
        }

        return false;
    }

    /**
     * Checks if the uploaded issue is a reply to an existing issue, or merely a
     * discardable copy of an existing issue.
     */
    private boolean isModifiedIssue(HashMap oldIssues, UploadIssue p_issue,
            String p_key)
    {
        Issue issue = getReferenceIssue(oldIssues, p_key);

        if (issue == null)
        {
            // This is a new issue, not a modified one, caller should
            // have detected this by calling isNewIssue().
            return false;
        }

        String oldComment = ((IssueHistory) issue.getHistory().get(0))
                .getComment();

        if (issue.getTitle().equals(p_issue.getTitle())
                && issue.getStatus().equals(p_issue.getStatus())
                && issue.getPriority().equals(p_issue.getPriority())
                && oldComment.equals(p_issue.getComment()))
        {
            return false;
        }

        return true;
    }

    /**
     * Fix the UploadIssue objects by patching in the TUV ID based on the
     * available TU and SUB ID.
     */
    private void fixIssueTuvs(HashMap p_segmentsMap,
            OfflinePageData p_uploadPageData)
    {
        // Collect all uploaded issues in a nice data structure.
        ArrayList issues = new ArrayList();

        issues.addAll(p_uploadPageData.getUploadedNewIssues());
        issues.addAll(p_uploadPageData.getUploadedReplyIssuesMap().values());

        for (int i = 0, max = issues.size(); i < max; i++)
        {
            UploadIssue issue = (UploadIssue) issues.get(i);

            String key = issue.getDisplayId();

            /*
             * Works for para view but not list view. Aaargh. String key =
             * String.valueOf(issue.getTuId()); if (issue.getSubId() != 0) { key
             * = key + "_" + String.valueOf(issue.getSubId()); }
             */

            OfflineSegmentData resOsd = (OfflineSegmentData) p_segmentsMap
                    .get(key);

            if (CATEGORY.isDebugEnabled())
            {
                System.out
                        .println("fixIssueTuvs looking for tu "
                                + issue.getTuId()
                                + CommentHelper.SEPARATOR
                                + "?"
                                + CommentHelper.SEPARATOR
                                + issue.getSubId()
                                + " (display id="
                                + key
                                + ")"
                                + ", found display id "
                                + (resOsd != null ? resOsd
                                        .getDisplaySegmentID() : "???")
                                + " tuvid="
                                + (resOsd != null ? resOsd.getTrgTuvId()
                                        : new Long(-1)));
            }

            // resOsd better be an object!
            if (resOsd != null)
            {
                Long tuvId = resOsd.getTrgTuvId();

                issue.setTuvId(tuvId.longValue());
            }
        }
    }

    private void updateProcess(int n)
    {
        if (status == null)
            return;

        status.updateProcess(n);
    }

    private HashMap<String, String> getAllGxmls(Tuv tuv)
    {
        HashMap<String, String> allGxmls = new HashMap<String, String>();
        allGxmls.put("0", tuv.getGxmlExcludeTopTags());
        List subFlows = tuv.getSubflowsAsGxmlElements();
        if (subFlows != null && subFlows.size() > 0)
        {
            for (int j = 0; j < subFlows.size(); j++)
            {
                GxmlElement ele = (GxmlElement) subFlows.get(j);
                allGxmls.put(String.valueOf(j + 1), GxmlUtil.getInnerXml(ele));
            }
        }

        return allGxmls;
    }

    /**
     * For segment with subs, its content in COMPACT mode from TER report is
     * like: [g1]this is the master content[/g1]
     * 
     * #66524:1 Content for sub 1
     * 
     * #66524:2 Content for sub 2
     * 
     */
    private HashMap<String, String> splitSegFromTERWithSubs(String segment,
            long tuId, Tuv tuv)
    {
        if (segment == null || tuv == null)
            return null;

        HashMap<String, String> results = new HashMap<String, String>();

        int index = -1;
        String tmp = segment;
        List subFlows = tuv.getSubflowsAsGxmlElements();
        if (subFlows != null && subFlows.size() > 0)
        {
            for (int i = 0; i < subFlows.size(); i++)
            {
                String key = "#" + tuId + ":" + (i + 1);
                index = tmp.indexOf(key);
                if (index > -1)
                {
                    String str1 = tmp.substring(0, index);
                    str1 = str1 == null ? "" : str1.trim();
                    // We assume the main segment is always there.
                    if (results.get("0") == null)
                    {
                        results.put("0", str1);
                    }
                    else
                    {
                        results.put(String.valueOf(i), str1);
                    }
                    tmp = tmp.substring(index + key.length());
                }
            }
            if (segment.equals(tmp))
            {
                results.put("0", segment);
            }
            else
            {
                // the last sub segment
                results.put(String.valueOf(subFlows.size()), tmp.trim());
            }
        }
        else
        {
            results.put("0", segment);
        }

        return results;
    }

    public OEMProcessStatus getStatus()
    {
        return status;
    }

    public void setStatus(OEMProcessStatus status)
    {
        this.status = status;
    }

    @Override
    public void cancel()
    {
        cancel = true;
    }

    public void setFileName(String fileName)
    {
        this.filename = fileName;
    }

    private DiplomatAPI m_diplomat = null;

    private DiplomatAPI getDiplomatApi()
    {
        if (m_diplomat == null)
        {
            m_diplomat = new DiplomatAPI();
        }

        m_diplomat.reset();

        return m_diplomat;
    }
}
