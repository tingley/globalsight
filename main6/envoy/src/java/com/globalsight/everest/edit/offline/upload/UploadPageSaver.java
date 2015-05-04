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

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.log4j.Logger;

import com.globalsight.everest.comment.CommentManager;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.edit.CommentHelper;
import com.globalsight.everest.edit.DisplayMatchTypeKeys;
import com.globalsight.everest.edit.SynchronizationManager;
import com.globalsight.everest.edit.SynchronizationStatus;
import com.globalsight.everest.edit.offline.AmbassadorDwUpConstants;
import com.globalsight.everest.edit.offline.OfflineEditHelper;
import com.globalsight.everest.edit.offline.download.DownLoadApi;
import com.globalsight.everest.edit.offline.page.OfflinePageData;
import com.globalsight.everest.edit.offline.page.OfflinePageDataGenerator;
import com.globalsight.everest.edit.offline.page.OfflineSegmentData;
import com.globalsight.everest.edit.offline.page.PageData;
import com.globalsight.everest.edit.offline.page.SubflowMergeInfo;
import com.globalsight.everest.edit.offline.page.SubsOfParent;
import com.globalsight.everest.edit.offline.page.UploadIssue;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.integration.ling.LingServerProxy;
import com.globalsight.everest.localemgr.LocaleManager;
import com.globalsight.everest.page.ExtractedSourceFile;
import com.globalsight.everest.page.PageManager;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.tuv.PageSegments;
import com.globalsight.everest.tuv.SegmentPair;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tuv.TuvImplVo;
import com.globalsight.everest.tuv.TuvManager;
import com.globalsight.everest.tuv.TuvManagerLocal;
import com.globalsight.everest.tuv.TuvState;
import com.globalsight.everest.util.jms.JmsHelper;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.pagehandler.edit.online.AutoPropagateThread;
import com.globalsight.everest.webapp.pagehandler.edit.online.PreviewPageHandler;
import com.globalsight.everest.webapp.pagehandler.edit.online.previewPDF.PreviewPDFHelper;
import com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper;
import com.globalsight.ling.common.DiplomatBasicParserException;
import com.globalsight.ling.inprogresstm.InProgressTmManager;
import com.globalsight.ling.tw.PseudoData;
import com.globalsight.ling.tw.TmxPseudo;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;

/**
 * This class is responsible for generating the reference data needed for error
 * checking and for saving the final page data to the Db. The OfflinePageData
 * object which is passed in and out of this class is the in-memory equivalent
 * of the offline file.
 */
public class UploadPageSaver implements AmbassadorDwUpConstants
{
    static private final Logger s_category = Logger
            .getLogger(UploadPageSaver.class);

    static private final SynchronizationManager s_synchManager = getSynchronizationManager();

    static public final int SCORE_UNKNOWN = -1;

    // The Hashtable Keys used for JMS
    static public final String USER = "user";
    static public final String FILE_NAME = "fileName";
    static public final String MODIFIED_TUVS = "tuvs";
    static public final String NEW_COMMENTS = "newComments";
    static public final String MODIFIED_COMMENTS = "modifiedComments";
    static public final String UPLOAD_PAGE_ID = "uploadPageId";
    static public final String IS_LAST_PAGE = "isLastPAGE";
    static public final String UPLOAD_PAGE_SOURCE_LOCALE = "uploadPageSourceLocale";
    static public final String UPLOAD_PAGE_TARGET_LOCALE = "uploadPageTargetLocale";
    static public final String UPLOAD_PAGE_USER_LOCALE = "uploadPageUserLocale";
    static public final String IS_UPLOADING_TASKS="isUploadingTasks";

    static private final String UPLOAD_SUCCESSFUL_SUBJECT = "uploadSuccessfulSubject";
    static private final String UPLOAD_SUCCESSFUL_MESSAGE = "uploadSuccessfulMessage";

    static private Set m_allMatchTypesSet = null;
    static private Set m_protectedMatchTypesSet = null;

    private PageData m_ref_PageData = null;
    private String m_pageIdAsString = null;
    private TargetPage m_targetPage = null;
    private GlobalSightLocale m_sourceLocale = null;
    private GlobalSightLocale m_targetLocale = null;
    private GlobalSightLocale m_userLocale = null;
    private int m_placeholderFormatId = -1;
    private Collection m_excludedItemTypes = null;
    private boolean m_addDeleteEnabled = false;
    private int m_uploadFileFormat = -1;
    private OfflinePageData m_uploadPage = null;

    static
    {
        try
        {
            // NOTE: since the keys that appear in the offline TEXT file format
            // currently cannot be translated without breaking the
            // parser, we will have to stick to English only VALUES for
            // those keys as well.
            ResourceBundle m_matchTypeLabels = ResourceBundle.getBundle(
                    "com.globalsight.resources.messages.EditorMatchTypeLabels",
                    Locale.US);

            // LOAD match type string for comparison.
            // The hash set is used to validate uploaded match type strings.
            Enumeration enumKeys = m_matchTypeLabels.getKeys();
            String key = null;

            m_allMatchTypesSet = new HashSet();

            while (enumKeys.hasMoreElements())
            {
                key = (String) enumKeys.nextElement();
                m_allMatchTypesSet.add(m_matchTypeLabels.getString(key)
                        .toLowerCase());
            }

            // Load only the protected match types.
            // This hash is used to determine protected segments.
            m_protectedMatchTypesSet = new HashSet();

            m_protectedMatchTypesSet
                    .add(m_matchTypeLabels.getString(
                            DisplayMatchTypeKeys.MSG_OL_CUR_TRG_EXCLUDED)
                            .toLowerCase());

            m_protectedMatchTypesSet.add(m_matchTypeLabels.getString(
                    DisplayMatchTypeKeys.MSG_OL_CUR_TRG_SUB_EXCLUDED)
                    .toLowerCase());

            m_protectedMatchTypesSet.add(m_matchTypeLabels.getString(
                    DisplayMatchTypeKeys.MSG_OL_EXACT_LOCKED).toLowerCase());

            m_protectedMatchTypesSet
                    .add(m_matchTypeLabels.getString(
                            DisplayMatchTypeKeys.MSG_OL_EXACT_SUB_LOCKED)
                            .toLowerCase());
        }
        catch (Exception ex)
        {
            s_category.error(ex.getMessage(), ex);

            // throws unchecked exception
            throw new RuntimeException(ex.toString());
        }
    }

    /**
     * Constructor.
     */
    public UploadPageSaver() throws UploadPageSaverException
    {
        try
        {
            // Check if Add Delete is enabled
            m_addDeleteEnabled = SystemConfiguration.getInstance()
                    .getBooleanParameter(
                            SystemConfigParamNames.ADD_DELETE_ENABLED);
        }
        catch (Exception ex)
        {
            UploadPageSaverException ex1 = new UploadPageSaverException(ex);

            s_category.error(ex1.getMessage(), ex1);

            throw ex1;
        }
    }

    /**
     * Initializes the Saver and gets the reference OfflinePageData needed for
     * error checking. The last reference OfflinePageData requested is also used
     * internally by other methods of this class.
     * 
     * @param p_uploadPage
     *            the page for which you need the reference data
     * @param p_excludedItemTypes
     *            the excluded item types list
     * @param p_uploadFileFormat
     *            the type of upload file
     * @return PageData that will be used as a reference for error checking
     * @exception UploadPageSaverException
     */
    public PageData initializeAndGetReferencePage(OfflinePageData p_uploadPage,
            Collection p_excludedItemTypes, int p_uploadFileFormat, String p_tempFileName)
            throws UploadPageSaverException
    {
        String pageId = p_uploadPage.getPageId();

        // check parameters
        if (pageId == null || pageId.length() == 0)
        {
            UploadPageSaverException ex = new UploadPageSaverException(
                    UploadPageSaverException.MSG_FAILED_ARGS_GET_REFPAGE, null,
                    null);
            s_category.error(ex.getMessage(), ex);
            throw ex;
        }

        m_pageIdAsString = pageId;
        m_placeholderFormatId = p_uploadPage.getPlaceholderFormatId();
        setLocales(p_uploadPage.getSourceLocaleName(),
                p_uploadPage.getTargetLocaleName());

        m_targetPage = getTargetPage(Long.parseLong(pageId),
                m_targetLocale.getId());

        // GXML editor synchronization - if source page is
        // being updated, can't download target page.

        // Todo: need to make this check earlier so the error message
        // is more accurate than the generic message that speaks about
        // incorrect file formats.

        SynchronizationStatus status = null;
        try
        {
            status = s_synchManager.getStatus(m_targetPage.getIdAsLong());
        }
        catch (Throwable ignore)
        {
            // ignore remote exceptions and other stuff
        }

        if (status != null)
        {
            String args[] =
            { String.valueOf(m_targetPage.getId()) };

            if (status.getStatus().equals(
                    SynchronizationStatus.GXMLUPDATE_STARTED))
            {
                UploadPageSaverException ex = new UploadPageSaverException(
                        UploadPageSaverException.MSG_PAGE_IS_BEING_UPDATED,
                        args, null);
                s_category.info(ex);
                throw ex;
            }
            else if (status.getStatus().equals(
                    SynchronizationStatus.UPLOAD_STARTED)
                    && s_synchManager.checkTempFileName(p_tempFileName))
            {
                UploadPageSaverException ex = new UploadPageSaverException(
                        UploadPageSaverException.MSG_PAGE_IS_BEING_UPDATED,
                        args, null);
                s_category.info(ex);
                throw ex;
            }
        }

        // Init the actual page data from the database.
        m_ref_PageData = getRefferencePageData(p_uploadPage,
                p_excludedItemTypes, p_uploadFileFormat);

        return m_ref_PageData;
    }

    /**
     * Initializes the Saver and gets the reference OfflinePageData needed for
     * error checking. The last reference OfflinePageData requested is also used
     * internally by other methods of this class.
     * 
     * @param p_uploadPage
     *            the page for which you need the reference data
     * @param p_excludedItemTypes
     *            the excluded item types list
     * @param p_uploadFileFormat
     *            the type of upload file
     * @return PageData that will be used as a reference for error checking
     * @exception UploadPageSaverException
     */
    public ArrayList<PageData> initializeAndGetReferencePages(
            OfflinePageData p_uploadPage, Collection p_excludedItemTypes,
            int p_uploadFileFormat) throws UploadPageSaverException
    {
        ArrayList<PageData> pageDatas = new ArrayList<PageData>();
        PageData pageData = null;

        String pageId = p_uploadPage.getPageId();
        String[] pageIds = pageId.split(",");

        setLocales(p_uploadPage.getSourceLocaleName(),
                p_uploadPage.getTargetLocaleName());
        m_placeholderFormatId = p_uploadPage.getPlaceholderFormatId();

        for (int i = 0; i < pageIds.length; i++)
        {
            pageId = pageIds[i];

            // check parameters
            if (pageId == null || pageId.length() == 0)
            {
                UploadPageSaverException ex = new UploadPageSaverException(
                        UploadPageSaverException.MSG_FAILED_ARGS_GET_REFPAGE,
                        null, null);
                s_category.error(ex.getMessage(), ex);
                throw ex;
            }

            m_pageIdAsString = pageId;
            m_targetPage = getTargetPage(Long.parseLong(pageId),
                    m_targetLocale.getId());

            // GXML editor synchronization - if source page is
            // being updated, can't download target page.

            // Todo: need to make this check earlier so the error message
            // is more accurate than the generic message that speaks about
            // incorrect file formats.

            SynchronizationStatus status = null;
            try
            {
                status = s_synchManager.getStatus(m_targetPage.getIdAsLong());
            }
            catch (Throwable ignore)
            {
                // ignore remote exceptions and other stuff
            }

            if (status != null)
            {
                String args[] =
                { String.valueOf(m_targetPage.getId()) };

                if (status.getStatus().equals(
                        SynchronizationStatus.GXMLUPDATE_STARTED))
                {
                    UploadPageSaverException ex = new UploadPageSaverException(
                            UploadPageSaverException.MSG_PAGE_IS_BEING_UPDATED,
                            args, null);
                    s_category.info(ex);
                    throw ex;
                }
                else if (status.getStatus().equals(
                        SynchronizationStatus.UPLOAD_STARTED))
                {
                    UploadPageSaverException ex = new UploadPageSaverException(
                            UploadPageSaverException.MSG_PAGE_IS_BEING_UPDATED,
                            args, null);
                    s_category.info(ex);
                    throw ex;
                }
            }

            // Init the actual page data from the database.
            pageData = getRefferencePageData(pageId, p_uploadPage,
                    p_excludedItemTypes, p_uploadFileFormat);

            if (pageData != null)
                pageDatas.add(pageData);
        }

        return pageDatas;
    }

    /**
     * Determines what the download status-of-protection was for a given offline
     * segment by doing a case-insensitive comparison of the offline matchType
     * string. Any type of protected segments will be ignored during upload.
     * 
     * Note: we could remove this dependency on the upload file by saving the
     * download protection state for each segment in the db. But this may not be
     * desirable because we allow multiple downloads (same user different
     * machine or pso trying to debug upload for a user) and we also allow the
     * user to choose to un-protect exact matches on the download screen (when
     * this feature is enabled for them in the l10n profile). The end result of
     * all these choices is currently recorded in the given instance of the
     * offline file by using the Matchtype string.
     * 
     * @param p_uploadSegment
     *            the uploaded segment in question.
     * @return true if protected, false if not.
     */
    static public boolean confirmUploadProtection(
            OfflineSegmentData p_uploadSegment) throws UploadPageSaverException
    {
        String uploadMatchType = p_uploadSegment.getDisplayMatchType()
                .toLowerCase();

        // We do not show the match type at all when the segment is
        // not an exact or fuzzy match. So an empty string means NOT
        // protected.
        if (uploadMatchType == null || uploadMatchType.length() == 0)
        {
            return false;
        }

        // must a valid matchType string (any type)
        if (!m_allMatchTypesSet.contains(uploadMatchType))
        {
            UploadPageSaverException ex = new UploadPageSaverException(
                    UploadPageSaverException.MSG_INVALID_UPLOAD_MATCH_TYPE,
                    null, null);
            s_category.error(ex.getMessage(), ex);
            throw ex;
        }

        // Determine if it is a protected match type.
        return m_protectedMatchTypesSet.contains(uploadMatchType);
    }

    /**
     * Saves unprotected offline segments to the appropriate target Tuvs. The
     * caller is expected to have submitted this page to the Offline Error
     * Checker before passing it to this method.
     * 
     * Since all relevant subflows must be merged into a given Tuv at the same
     * time, this method builds two hash maps. One is the Tuvs to be saved and
     * the other is the subflows to be saved. After both maps are populated, we
     * merge the subflows to the Tuvs to be saved. Working this way, we are able
     * to collect subflows in any order that they may appear in the upload file
     * and eventually join them with the correct Tuv.
     * 
     * @param p_uploadPage
     *            the offline page you wish to save.
     * @param p_jmsDestinationQueue
     *            The JMS queue used for saving tvus and indexing in the
     *            background.
     * @param p_user
     *            - The user to be notified upon success or failure of the
     *            process.
     * @param p_fileName
     *            - The file name used for email notification.
     * @exception GeneralException
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void savePageToDb(OfflinePageData p_uploadPage,
            ArrayList<PageData> p_referencePages, String p_jmsDestinationQueue,
            User p_user, String p_fileName, List<Task> p_isUploadingTasks)
            throws GeneralException, DiplomatBasicParserException
    {
        m_uploadPage = p_uploadPage;

        boolean isRepeatedSegments = false;
        if (p_fileName != null
                && p_fileName.contains(DownLoadApi.REPEATED_SEGMENTS_KEY))
        {
            isRepeatedSegments = true;
        }
        
        long jobId = -1;
        OfflineSegmentData refSegment = null;
        HashMap subsToBeSavedMap = new HashMap();
        PageData refPageData = null;
        for (int i = 0; i < p_referencePages.size(); i++)
        {
            refPageData = p_referencePages.get(i);
            jobId = refPageData.getPageSegments().getSourcePage().getJobId();
            HashMap ref_OPDSegmentMap = refPageData.getOfflinePageData()
                    .getSegmentMap();

            // NOTE: all upload errors have been handled at this point,
            // and all changed/unchanged segments have been flagged in the
            // p_uploadPage. Here we collect only the changed segments.
            ListIterator it = p_uploadPage.getSegmentIterator();
            while (it.hasNext())
            {
                OfflineSegmentData uploadSegment = (OfflineSegmentData) it.next();
                refSegment = (OfflineSegmentData) ref_OPDSegmentMap
                        .get(uploadSegment.getDisplaySegmentID());

                if (refSegment == null)
                {
                    continue;
                }

                // Only modified segments are saved in PageSegments.
                // Modified segments have been marked in the ptag error checker.
                if (uploadSegment.hasTargetBeenEdited()
                        && uploadSegment.isTagCheckSuccesful())
                {
                    // Add - tuvs to be saved:

                    // Lookup the target Tuv in the SubsToBeSaved collection
                    // first.
                    // If it is not there, we add it to the collection.
                    // We MUST do this no matter if it is a parent or
                    // subflow because the sub may have been edited while
                    // the parent was not.
                    if (uploadSegment.isSubflowSegment())
                    {
                        SubsOfParent subs;
                        if ((subs = (SubsOfParent) subsToBeSavedMap
                                .get(uploadSegment.getTuIdAsLong())) == null)
                        {
                            subs = new SubsOfParent(
                                    uploadSegment.getTuIdAsLong());
                            subsToBeSavedMap.put(uploadSegment.getTuIdAsLong(),
                                    subs);
                        }

                        // add the subflow to TuvParentSubs
                        subs.setSubflow(uploadSegment.getSubflowId(),
                                uploadSegment.getDisplayTargetText());
                    }
                    else
                    // Copy Parent (to the PageSegments that will be saved)
                    {
                        // Note: MUST use setGxmlExcludeTopTagsIgnoreSubflows()
                        SegmentPair segmentPair = refPageData.getPageSegments()
                                .getSegmentPairByTuId(
                                        uploadSegment.getTuIdAsLong()
                                                .longValue(), m_targetLocale);
                        // set the text
                        segmentPair.getTargetTuv()
                                .setGxmlExcludeTopTagsIgnoreSubflows(
                                        uploadSegment.getDisplayTargetText(),
                                        jobId);
                        if (uploadSegment.isStateTranslated()) {
                            segmentPair.getTargetTuv().setState(TuvState.APPROVED);
                        } else {
                        	segmentPair.getTargetTuv().setState(TuvState.LOCALIZED);
                        }
                        // set the modified flag
                        segmentPair.setModified();
                    }
                }
                else if (isRepeatedSegments
                        && uploadSegment.isTagCheckSuccesful())
                {
                    if (!uploadSegment.isSubflowSegment()
                            && !confirmUploadProtection(uploadSegment))
                    {
                        // Note: MUST use setGxmlExcludeTopTagsIgnoreSubflows()
                        SegmentPair segmentPair = refPageData.getPageSegments()
                                .getSegmentPairByTuId(
                                        uploadSegment.getTuIdAsLong().longValue(),
                                        m_targetLocale);

                        String srcGxml = segmentPair.getSourceTuv()
                                .getGxmlExcludeTopTags();
                        String tgtGxml = segmentPair.getTargetTuv()
                                .getGxmlExcludeTopTags();
                        String newTgtGxml = uploadSegment.getDisplayTargetText();
                        PseudoData srcPD = new PseudoData();
                        srcPD.setIgnoreNativeId(true);
                        PseudoData tgtPD = new PseudoData();
                        tgtPD.setIgnoreNativeId(true);
                        PseudoData newTgtPD = new PseudoData();
                        newTgtPD.setIgnoreNativeId(true);
                        String srcPtag = TmxPseudo.tmx2Pseudo(srcGxml, srcPD)
                                .getPTagSourceString();
                        String tgtPtag = TmxPseudo.tmx2Pseudo(tgtGxml, tgtPD)
                                .getPTagSourceString();
                        String newTgtPtag = TmxPseudo.tmx2Pseudo(newTgtGxml,
                                newTgtPD).getPTagSourceString();
                        
                        String matchType = uploadSegment.getDisplayMatchType();
                        if (matchType != null
                                && matchType.contains("Context Exact Match"))
                        {
                            newTgtPtag = newTgtPtag.trim();
                        }

                        if (!newTgtPtag.equals(tgtPtag) &&
                        		!newTgtPtag.equals(srcPtag))
                        {
                            // set the text same as target segment
                            segmentPair.getTargetTuv()
                                    .setGxmlExcludeTopTagsIgnoreSubflows(
                                            newTgtGxml, jobId);

                            // set the modified flag
                            segmentPair.setModified();
                        }

                        if (uploadSegment.isStateTranslated())
                        {
                            segmentPair.getTargetTuv().setState(TuvState.APPROVED);
                            segmentPair.setModified();
                        }
                        else if (uploadSegment.hasTargetBeenEdited())
                        {
                        	segmentPair.getTargetTuv().setState(TuvState.LOCALIZED);
                        	segmentPair.setModified();
                        }
                    }
                }
                it.remove();
            }
            setSubsOnTargets(subsToBeSavedMap, p_referencePages);

            // Save
            // Send a message to a JMS Queue.
            // NOTE: We send a message even if there are no modified Tuvs to
            // save or index. This is so an upload confirmation e-mail
            // will still be sent - and in the same order that files
            // are upload.
            List<TuvImplVo> modifiedTuvs = getModifiedTuvs(refPageData
                    .getPageSegments());
            List newComments = p_uploadPage.getUploadedNewIssues();
            Map replyComments = p_uploadPage.getUploadedReplyIssuesMap();

            // filter comments here for consolidate RTF
            newComments = filterNewComment(newComments, ref_OPDSegmentMap);
            replyComments = filterReplayComment(replyComments,
                    ref_OPDSegmentMap);
            boolean isLastOne = (i == p_referencePages.size() - 1);
            long trgPageId = refPageData.getPageSegments().getSourcePage()
                    .getTargetPageByLocaleId(m_targetLocale.getId()).getId();
            saveNoJms(modifiedTuvs, newComments, replyComments, p_user, p_fileName,
                    p_jmsDestinationQueue, isLastOne, trgPageId, p_isUploadingTasks);
        }
    }

    private Map filterReplayComment(Map replyComments, HashMap ref_OPDSegmentMap)
    {
        Map result = new HashMap();

        if (replyComments == null || replyComments.size() == 0)
        {
            return result;
        }

        Iterator issueKeys = replyComments.keySet().iterator();

        while (issueKeys.hasNext())
        {
            Object issueKey = issueKeys.next();
            UploadIssue issue = (UploadIssue) replyComments.get(issueKey);
            String key = issue.getDisplayId();

            if (ref_OPDSegmentMap.containsKey(key))
            {
                result.put(issueKey, issue);
            }
        }

        return result;
    }

    private List filterNewComment(List newComments, HashMap ref_OPDSegmentMap)
    {
        List result = new ArrayList();

        if (newComments == null || newComments.size() == 0)
        {
            return result;
        }

        for (int i = 0, max = newComments.size(); i < max; i++)
        {
            UploadIssue issue = (UploadIssue) newComments.get(i);

            String key = issue.getDisplayId();

            if (ref_OPDSegmentMap.containsKey(key))
            {
                result.add(issue);
            }
        }

        return result;
    }

    /**
     * Returns the primary file if it is extracted or NULL if it isn't
     * extracted.
     */
    private ExtractedSourceFile getExtractedSourceFile(SourcePage p_page)
    {
        ExtractedSourceFile result = null;

        if (p_page.getPrimaryFileType() == ExtractedSourceFile.EXTRACTED_FILE)
        {
            result = (ExtractedSourceFile) p_page.getPrimaryFile();
        }

        return result;
    }

    // Request reference data:
    // NOTE: Due to split/merge we now get the ref page using the same
    // download page generator. This represents a major shift in how
    // the previous coded worked. We do not have all original
    // download parameters. We only have the ones recorded in the
    // upload file header.
    private PageData getRefferencePageData(OfflinePageData p_uploadPage,
            Collection p_excludedTypeNames, int p_uploadFileFormat)
            throws UploadPageSaverException
    {
        try
        {
            GlobalSightLocale srcLoc = ServerProxy.getLocaleManager()
                    .getLocaleByString(p_uploadPage.getSourceLocaleName());
            GlobalSightLocale trgLoc = ServerProxy.getLocaleManager()
                    .getLocaleByString(p_uploadPage.getTargetLocaleName());

            UploadParams uploadParams = new UploadParams();
            uploadParams.setSourceLocale(srcLoc);
            uploadParams.setTargetLocale(trgLoc);
            uploadParams.setTagDisplayFormatID(p_uploadPage
                    .getPlaceholderFormatId());
            uploadParams.setPageId(p_uploadPage.getPageId());
            uploadParams.setTargetPageId(m_targetPage.getIdAsLong());
            // We do not record the page name in the header during
            // download (need to consider multi-lingual names).
            // /*p_uploadPage.getPageName()*/
            uploadParams.setPageName("Unknown upload page name");
            uploadParams.setCanUseUrl(false);
            uploadParams.setMergeOverrideDirectives(p_uploadPage
                    .getSegmentMergeMap());
            uploadParams.setMergeEnabled(p_uploadPage
                    .isLoadedFromMergeEnabledClient());
            uploadParams.setExcludedTypeNames(p_excludedTypeNames);
            uploadParams.setFileFormatId(p_uploadFileFormat);

            OfflinePageDataGenerator generator = new OfflinePageDataGenerator();
            return generator.getUploadReferencePageData(uploadParams);
        }
        catch (Exception ex)
        {
            UploadPageSaverException ex1 = new UploadPageSaverException(
                    UploadPageSaverException.MSG_FAILED_ARGS_GET_REFPAGE, null,
                    ex);
            throw ex1;
        }
    }

    // Request reference data:
    // NOTE: Due to split/merge we now get the ref page using the same
    // download page generator. This represents a major shift in how
    // the previous coded worked. We do not have all original
    // download parameters. We only have the ones recorded in the
    // upload file header.
    private PageData getRefferencePageData(String pageId,
            OfflinePageData p_uploadPage, Collection p_excludedTypeNames,
            int p_uploadFileFormat) throws UploadPageSaverException
    {
        try
        {
            GlobalSightLocale srcLoc = ServerProxy.getLocaleManager()
                    .getLocaleByString(p_uploadPage.getSourceLocaleName());
            GlobalSightLocale trgLoc = ServerProxy.getLocaleManager()
                    .getLocaleByString(p_uploadPage.getTargetLocaleName());

            UploadParams uploadParams = new UploadParams();
            uploadParams.setSourceLocale(srcLoc);
            uploadParams.setTargetLocale(trgLoc);
            uploadParams.setTagDisplayFormatID(p_uploadPage
                    .getPlaceholderFormatId());
            uploadParams.setPageId(pageId);
            uploadParams.setTargetPageId(m_targetPage.getIdAsLong());
            // We do not record the page name in the header during
            // download (need to consider multi-lingual names).
            // /*p_uploadPage.getPageName()*/
            uploadParams.setPageName("Unknown upload page name");
            uploadParams.setCanUseUrl(false);
            uploadParams.setMergeOverrideDirectives(p_uploadPage
                    .getSegmentMergeMap());
            uploadParams.setMergeEnabled(p_uploadPage
                    .isLoadedFromMergeEnabledClient());
            uploadParams.setExcludedTypeNames(p_excludedTypeNames);
            uploadParams.setFileFormatId(p_uploadFileFormat);

            OfflinePageDataGenerator generator = new OfflinePageDataGenerator();
            return generator.getUploadReferencePageData(uploadParams);
        }
        catch (Exception ex)
        {
            UploadPageSaverException ex1 = new UploadPageSaverException(
                    UploadPageSaverException.MSG_FAILED_ARGS_GET_REFPAGE, null,
                    ex);
            throw ex1;
        }
    }

    private void setSubsOnTargets(Map p_subsToBeSavedMap,
            ArrayList<PageData> p_referencePages) throws GeneralException
    {
        Map subflowOffsetMap = m_uploadPage.buildSubflowOffsetMap();
        Collection allSubsOfParent = p_subsToBeSavedMap.values();

        for (Iterator it = allSubsOfParent.iterator(); it.hasNext();)
        {
            SubsOfParent subs = (SubsOfParent) it.next();
            SubflowMergeInfo subInfo = (SubflowMergeInfo) subflowOffsetMap
                    .get(subs.m_parentTuvId);

            if (subInfo != null)
            {
                subs.renumber(subInfo);
            }

            // get the trgTuv and add the subflows
            if (subs.m_subMap != null && subs.m_subMap.size() != 0)
            {
                try
                {
                    PageData pageData = m_ref_PageData;
                    SegmentPair segmentPair = null;

                    if (pageData != null)
                    {
                        segmentPair = m_ref_PageData.getPageSegments()
                                .getSegmentPairByTuId(
                                        subs.m_parentTuvId.longValue(),
                                        m_targetLocale);
                    }
                    if (pageData == null && p_referencePages != null)
                    {
                        for (int i = 0; i < p_referencePages.size(); i++)
                        {
                            pageData = p_referencePages.get(i);
                            segmentPair = pageData.getPageSegments()
                                    .getSegmentPairByTuId(
                                            subs.m_parentTuvId.longValue(),
                                            m_targetLocale);

                            if (segmentPair != null)
                            {
                                break;
                            }
                        }
                    }

                    segmentPair.getTargetTuv().setSubflowsGxml(subs.m_subMap);
                    segmentPair.setModified();
                }
                catch (Exception ex)
                {
                    // Not critical enough to abort and unfortunately,
                    // we currently do not have a warning mechanism
                    s_category
                            .error("Unable to set Subflow(s) on parent segment. "
                                    + "The parent segment will be saved without the updated subflow(s).",
                                    ex);
                }
            }
        }
    }
    
    private void saveTuvNoJms(List<TuvImplVo> p_modifiedTuvs, List p_newComments,
            Map p_replyComments, User p_user, String p_fileName,
            String p_jmsDestinationQueue, boolean p_isLastOne, long p_trgPageId,
            List<Task> p_isUploadingTasks) throws GeneralException
    {
        if (p_modifiedTuvs == null || p_modifiedTuvs.size() == 0)
        {
            // If no modified TUVs, ensure the uploading status is set back to
            // "N" before return.
            if (p_isLastOne) {
                TaskHelper.updateTaskStatus(p_isUploadingTasks, UPLOAD_DONE,
                        false);
            }

            return;
        }

        SynchronizationManager syncMgr = getSynchronizationManager();
        try
        {
            syncMgr.uploadStarted(p_trgPageId);
        }
        catch (RemoteException e)
        {
        }
        
       
        try
        {
            PageManager pageMgr = ServerProxy.getPageManager();
            SourcePage sp = pageMgr.getTargetPage(p_trgPageId)
                    .getSourcePage();
            long jobId = sp.getJobId();
            
            TuvManagerLocal tml = new TuvManagerLocal();
            tml.saveTuvsFromOfflineNoJms(p_modifiedTuvs, jobId);
        }
        catch (RemoteException e)
        {
            s_category.error(e);
        }
        finally
        {
            try
            {
                syncMgr.uploadFinished(p_trgPageId);
            }
            catch (Throwable ex)
            {
            }

        }
    }
    

    // "modifiedTuvs" are from same page/job.
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void save(List<TuvImplVo> p_modifiedTuvs, List p_newComments,
            Map p_replyComments, User p_user, String p_fileName,
            String p_jmsDestinationQueue, boolean p_isLastOne, long p_trgPageId,
            List<Task> p_isUploadingTasks) throws GeneralException
    {
        if (p_modifiedTuvs == null || p_modifiedTuvs.size() == 0)
        {
            // If no modified TUVs, ensure the uploading status is set back to
            // "N" before return.
            if (p_isLastOne) {
                TaskHelper.updateTaskStatus(p_isUploadingTasks, UPLOAD_DONE,
                        false);
            }

            return;
        }

        try
        {
            HashMap map = new HashMap();

            CompanyWrapper.saveCurrentCompanyIdInMap(map, s_category);
            map.put(UPLOAD_PAGE_ID, p_trgPageId);
            map.put(UPLOAD_PAGE_SOURCE_LOCALE, m_sourceLocale);
            map.put(UPLOAD_PAGE_TARGET_LOCALE, m_targetLocale);
            map.put(UPLOAD_PAGE_USER_LOCALE, setUserLocale(p_user));
            map.put(MODIFIED_TUVS, p_modifiedTuvs);
            map.put(NEW_COMMENTS, p_newComments);
            map.put(MODIFIED_COMMENTS, p_replyComments);
            map.put(USER, p_user);
            map.put(FILE_NAME, p_fileName);
            map.put(IS_LAST_PAGE, new Boolean(p_isLastOne));
            map.put(IS_UPLOADING_TASKS, p_isUploadingTasks);

            // Send all data through the JMS queue to PageSaverMDB.
            JmsHelper.sendMessageToQueue(map, p_jmsDestinationQueue);
        }
        catch (Exception ex)
        {
            // s_category.error("UploadPageSaver" + ex.getMessage(), ex);
            throw new UploadPageSaverException(
                    UploadPageSaverException.MSG_FAILED_TO_POST_JMS_UPLOAD_SAVE,
                    null, ex);
        }
    }

    /**
     * Save modified target TUVs into in progress TM when offline upload.
     * 
     * @param p_modifiedTuvs
     * @param p_sourceLocale
     * @param p_targetPageId
     */
    private void saveTuvsIntoInProgressTm(List<TuvImplVo> p_modifiedTuvs,
            GlobalSightLocale p_sourceLocale, Long p_targetPageId)
    {
        if (p_modifiedTuvs == null || p_modifiedTuvs.size() == 0)
            return;

        try
        {
            Tuv sourceTuv = null;
            Tuv targetTuv = null;

            PageManager pageMgr = ServerProxy.getPageManager();
            TuvManager tuvMgr = ServerProxy.getTuvManager();
            InProgressTmManager ipTmMgr = LingServerProxy
                    .getInProgressTmManager();

            SourcePage sp = pageMgr.getTargetPage(p_targetPageId)
                    .getSourcePage();
            long sourcePageId = sp.getId();
            long jobId = sp.getJobId();
            for (Iterator it = p_modifiedTuvs.iterator(); it.hasNext();)
            {
                targetTuv = (Tuv) it.next();
                sourceTuv = tuvMgr.getTuvForSegmentEditor(targetTuv
                        .getTu(jobId).getId(), p_sourceLocale.getId(), jobId);
                // only saveTuvsIntoInProgressTm if target segment != source
                // segment
                String srcGxml = sourceTuv.getGxmlExcludeTopTags();
                String tgtGxml = targetTuv.getGxmlExcludeTopTags();
                if (srcGxml != null && tgtGxml != null
                        && !srcGxml.equals(tgtGxml))
                {
                    ipTmMgr.save(sourceTuv, targetTuv, "0", sourcePageId);
                }
            }
        }
        catch (Exception e)
        {
            s_category.error("Cannot save TUVs to In Progress TM", e);
            throw new UploadPageSaverException(e);
        }
    }

    private void saveNewComments(List p_comments, String p_user,
            Long p_targetPageId)
    {
        // p_comments is just a flat ArrayList of UploadIssue objects.

        for (int i = 0, max = p_comments.size(); i < max; i++)
        {
            UploadIssue issue = (UploadIssue) p_comments.get(i);

            long tuId = issue.getTuId();
            long tuvId = issue.getTuvId();
            long subId = issue.getSubId();

            if (tuvId == -1)
            {
                continue;
            }

            String logicalKey = CommentHelper.makeLogicalKey(
                    p_targetPageId.longValue(), tuId, tuvId, subId);

            if (s_category.isDebugEnabled())
            {
                s_category.debug("Creating new comment for " + logicalKey);
            }

            try
            {
                CommentManager mgr = ServerProxy.getCommentManager();

                mgr.addIssue(issue.getLevelObjectType(), issue.getTuvId(),
                        issue.getTitle(), issue.getPriority(),
                        issue.getStatus(), issue.getCategory(), p_user,
                        issue.getComment(), logicalKey);
            }
            catch (Exception ex)
            {
                // Don't fail the upload because of comments.
                s_category.error("Error creating new comment for " + logicalKey
                        + " (continuing anyway)", ex);
            }
        }
    }

    private void saveModifiedComments(Map p_comments, String p_user)
    {
        // p_comments is a HashMap with original Issue objects as
        // keys, and UploadIssue objects as values.

        Collection oldIssues = p_comments.keySet();

        for (Iterator it = oldIssues.iterator(); it.hasNext();)
        {
            Long oldIssueId = (Long) it.next();
            UploadIssue issue = (UploadIssue) p_comments.get(oldIssueId);

            if (s_category.isDebugEnabled())
            {
                s_category.debug("Replying to comment id=" + oldIssueId);
            }

            try
            {
                CommentManager mgr = ServerProxy.getCommentManager();
                mgr.replyToIssue(oldIssueId.longValue(), issue.getTitle(),
                        issue.getPriority(), issue.getStatus(),
                        issue.getCategory(), p_user, issue.getComment());
            }
            catch (Exception ex)
            {
                // Don't fail the upload because of comments.
                s_category.error("Error replying to comment " + oldIssueId
                        + " (continuing anyway)", ex);
            }
        }
    }
    
 // "modifiedTuvs" are from same page/job.
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void saveNoJms(List<TuvImplVo> p_modifiedTuvs, List p_newComments,
            Map p_replyComments, User p_user, String p_fileName,
            String p_jmsDestinationQueue, boolean p_isLastOne, long p_trgPageId,
            List<Task> p_isUploadingTasks) throws GeneralException
    {
        if (p_modifiedTuvs == null || p_modifiedTuvs.size() == 0)
        {
            // If no modified TUVs, ensure the uploading status is set back to
            // "N" before return.
            if (p_isLastOne) {
                TaskHelper.updateTaskStatus(p_isUploadingTasks, UPLOAD_DONE,
                        false);
            }

            return;
        }

        String companyIdStr = null;
        try
        {
            TargetPage tp = ServerProxy.getPageManager().getTargetPage(
                    p_trgPageId);
            companyIdStr = String.valueOf(tp.getWorkflowInstance()
                    .getCompanyId());
        }
        catch (Exception e)
        {
            s_category.error("Get company id Error:" + e);
        }

        // check if auto propagate
        String specTus = "";
        boolean isRepeatedSegments = false;
        if (p_fileName != null
                && p_fileName.contains(DownLoadApi.REPEATED_SEGMENTS_KEY))
        {
            isRepeatedSegments = true;
        }

        // save target tuvs
        Iterator tuvIter = p_modifiedTuvs.iterator();
        while (tuvIter.hasNext())
        {
            Tuv tuv = (Tuv) tuvIter.next();
            tuv.setLastModifiedUser(p_user.getUserId());

            if (isRepeatedSegments)
            {
                specTus = specTus + tuv.getTuId() + ",";
            }
        }
        
        saveTuvNoJms(p_modifiedTuvs, p_newComments, p_replyComments, p_user,
                p_fileName, p_jmsDestinationQueue, p_isLastOne, p_trgPageId,
                p_isUploadingTasks);

        specTus = ""; // do not do Auto-Propagate from don's email
        if (specTus != null && specTus.length() > 0)
        {
            // AutoPropagateThread.java
            AutoPropagateThread apThread = new AutoPropagateThread();
            apThread.setPickup("latest");
            apThread.setSpecTus(specTus);
            apThread.setTargetPageId("" + p_trgPageId);
            apThread.setTuScope("specifiedTus");
            apThread.setTuvScope("all");
            apThread.setUser(p_user);
            apThread.run();
        }

        // save modified target TUVs into in progress TM
        saveTuvsIntoInProgressTm(p_modifiedTuvs, m_sourceLocale, p_trgPageId);

        // save comments
        saveNewComments(p_newComments, p_user.getUserId(), p_trgPageId);
        saveModifiedComments(p_replyComments, p_user.getUserId());

        // Delete the old files for preview
        PreviewPDFHelper.deleteOldPdf(p_trgPageId, m_targetLocale.getId());
        PreviewPageHandler.deleteOldPreviewFile(p_trgPageId,
                m_targetLocale.getId());
        // After a successful save, notify user.
        // (Including when no modified Tuvs or comments are uploaded.)
        GlobalSightLocale userLocale = setUserLocale(p_user);
        String localePair = OfflineEditHelper.localePair(m_sourceLocale,
                m_targetLocale, userLocale);

        if (isRepeatedSegments)
        {
            if (p_isLastOne)
            {
                OfflineEditHelper.notifyUser(p_user, p_fileName, localePair,
                        OfflineEditHelper.UPLOAD_SUCCESSFUL_SUBJECT,
                        OfflineEditHelper.UPLOAD_SUCCESSFUL_MESSAGE,
                        companyIdStr);
            }
        }
        else
        {
            OfflineEditHelper.notifyUser(p_user, p_fileName, localePair,
                    OfflineEditHelper.UPLOAD_SUCCESSFUL_SUBJECT,
                    OfflineEditHelper.UPLOAD_SUCCESSFUL_MESSAGE, companyIdStr);
        }
        
        if (p_isLastOne && p_isUploadingTasks != null && p_isUploadingTasks.size() > 0)
        {
            for (Task isUploadingTask : p_isUploadingTasks)
            {
                // Update task status (Upload Done)
                TaskHelper.updateTaskStatus(isUploadingTask, AmbassadorDwUpConstants.UPLOAD_DONE, false);
                s_category
                        .info("Offline file uploading is done for task(taskID:"
                                + isUploadingTask.getId() + "):"
                                + isUploadingTask.getTaskName());
            }
        }
    }
    
    /**
     * Gets all the required data to build an Offline page from the DB.
     * 
     * @exception UploadPageSaverException
     */
    private void setLocales(String p_sourceLocale, String p_targetLocale)
            throws UploadPageSaverException
    {
        LocaleManager mgr = null;

        try
        {
            mgr = ServerProxy.getLocaleManager();
            m_sourceLocale = mgr.getLocaleByString(p_sourceLocale);
            m_targetLocale = mgr.getLocaleByString(p_targetLocale);
        }
        catch (Exception ex)
        {
            s_category.error(ex.getMessage(), ex);
            throw new UploadPageSaverException(ex);
        }
    }

    private List<TuvImplVo> getModifiedTuvs(PageSegments p_segments)
            throws GeneralException
    {
        return p_segments.getModifiedTuvs(m_targetLocale);
    }

    private GlobalSightLocale setUserLocale(User p_user)
            throws GeneralException
    {
        // get locale objects for e-mail notification
        try
        {
            m_userLocale = ServerProxy.getLocaleManager().getLocaleByString(
                    p_user.getDefaultUILocale());

            return m_userLocale;
        }
        catch (Exception ex)
        {
            s_category.error(ex.getMessage(), ex);
            throw new UploadPageSaverException(ex);
        }
    }

    /**
     * Wraps the code for getting the source page from the DB and handling any
     * exceptions.
     * 
     * @param p_sourcePageId
     *            the id of the source page you are requesting
     * @return the actual source page
     * @exception UploadPageSaverException
     */
    private SourcePage getSourcePage(long p_sourcePageId)
            throws UploadPageSaverException
    {
        PageManager mgr = null;

        try
        {
            mgr = ServerProxy.getPageManager();
            return mgr.getSourcePage(p_sourcePageId);
        }
        catch (Exception ex)
        {
            s_category.error(ex.getMessage(), ex);
            throw new UploadPageSaverException(ex);
        }
    }

    private TargetPage getTargetPage(long p_sourcePageId, long p_targetLocaleId)
            throws UploadPageSaverException
    {
        PageManager mgr = null;

        try
        {
            mgr = ServerProxy.getPageManager();
            return mgr.getTargetPage(p_sourcePageId, p_targetLocaleId);
        }
        catch (Exception ex)
        {
            s_category.error(ex.getMessage(), ex);
            throw new UploadPageSaverException(ex);
        }
    }

    static private SynchronizationManager getSynchronizationManager()
    {
        try
        {
            return ServerProxy.getSynchronizationManager();
        }
        catch (Exception ex)
        {
            s_category.error("Internal error: "
                    + "cannot receive offline/online synchronization messages",
                    ex);
        }

        return null;
    }
}
