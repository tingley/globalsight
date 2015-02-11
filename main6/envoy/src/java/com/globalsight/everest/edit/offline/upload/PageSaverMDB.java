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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.apache.log4j.Logger;

import com.globalsight.cxe.adaptermdb.EventTopicMap;
import com.globalsight.everest.comment.CommentManager;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.edit.CommentHelper;
import com.globalsight.everest.edit.SynchronizationManager;
import com.globalsight.everest.edit.offline.AmbassadorDwUpConstants;
import com.globalsight.everest.edit.offline.OfflineEditHelper;
import com.globalsight.everest.edit.offline.download.DownLoadApi;
import com.globalsight.everest.edit.offline.page.UploadIssue;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.integration.ling.LingServerProxy;
import com.globalsight.everest.page.PageManager;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tuv.TuvImplVo;
import com.globalsight.everest.tuv.TuvManager;
import com.globalsight.everest.util.jms.GenericQueueMDB;
import com.globalsight.everest.util.jms.JmsHelper;
import com.globalsight.everest.webapp.pagehandler.edit.online.AutoPropagateThread;
import com.globalsight.everest.webapp.pagehandler.edit.online.PreviewPageHandler;
import com.globalsight.everest.webapp.pagehandler.edit.online.previewPDF.PreviewPDFHelper;
import com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper;
import com.globalsight.ling.inprogresstm.InProgressTmManager;
import com.globalsight.log.ActivityLog;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GlobalSightLocale;

/**
 * PageSaverMDB is a message driven bean responsible for saving modified tuvs
 * (target tuvs) and then indexing the source tuvs during upload.
 */
@MessageDriven(messageListenerInterface = MessageListener.class, activationConfig =
{
        @ActivationConfigProperty(propertyName = "destination", propertyValue = EventTopicMap.QUEUE_PREFIX_JBOSS
                + JmsHelper.JMS_UPLOAD_QUEUE),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = JmsHelper.JMS_TYPE_QUEUE) })
@TransactionManagement(value = TransactionManagementType.BEAN)
public class PageSaverMDB extends GenericQueueMDB
{
    private static final long serialVersionUID = -4276547556612098118L;

    static private final Logger s_category = Logger
            .getLogger(PageSaverMDB.class);

    /**
     * Default constructor.
     */
    public PageSaverMDB()
    {
        super(s_category);
    }

    /**
     * Start the upload process as a separate thread. This method is not a
     * public API and is ONLY invoked by it's consumer for saving target tuvs
     * durning an upload.
     * 
     * @param p_message
     *            - The message to be passed. In this case, it's an object
     *            message that contains a HashMap containing: 1. A collection of
     *            modified tuvs 2. A map of modified comments (organized by tuv
     *            id) 3. The source locale object. 4. The user object 5. The
     *            file name. 6. The target page id (as string).
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void onMessage(Message p_message)
    {
        User user = null;
        String fileName = null;
        Long targetPageId = null;
        GlobalSightLocale sourceLocale = null;
        GlobalSightLocale targetLocale = null;
        GlobalSightLocale userLocale = null;
        String compandIdStr = null;
        List<Task> isUploadingTasks = null;

        SynchronizationManager syncMgr = getSynchronizationManager();

        ActivityLog.Start activityStart = null;
        try
        {
            if (s_category.isDebugEnabled())
            {
                s_category.debug("Received message for offline upload.");
            }

            if (p_message.getJMSRedelivered())
            {
                s_category.warn("Ignoring duplicate upload message.");
                return;
            }

            // get the hashtable that contains the export info
            HashMap map = (HashMap) ((ObjectMessage) p_message).getObject();

            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put(UploadPageSaver.UPLOAD_PAGE_SOURCE_LOCALE,
                    map.get(UploadPageSaver.UPLOAD_PAGE_SOURCE_LOCALE));
            activityArgs.put(UploadPageSaver.UPLOAD_PAGE_TARGET_LOCALE,
                    map.get(UploadPageSaver.UPLOAD_PAGE_TARGET_LOCALE));
            activityArgs.put(UploadPageSaver.FILE_NAME,
                    map.get(UploadPageSaver.FILE_NAME));
            activityArgs.put(UploadPageSaver.USER,
                    map.get(UploadPageSaver.USER));
            activityArgs.put(UploadPageSaver.UPLOAD_PAGE_ID,
                    map.get(UploadPageSaver.UPLOAD_PAGE_ID));
            activityStart = ActivityLog.start(PageSaverMDB.class, "run",
                    activityArgs);

            compandIdStr = (String) map.get(CompanyWrapper.CURRENT_COMPANY_ID);
            CompanyThreadLocal.getInstance().setIdValue(compandIdStr);

            sourceLocale = (GlobalSightLocale) map
                    .get(UploadPageSaver.UPLOAD_PAGE_SOURCE_LOCALE);
            targetLocale = (GlobalSightLocale) map
                    .get(UploadPageSaver.UPLOAD_PAGE_TARGET_LOCALE);
            userLocale = (GlobalSightLocale) map
                    .get(UploadPageSaver.UPLOAD_PAGE_USER_LOCALE);
            @SuppressWarnings("unchecked")
            List<TuvImplVo> modifiedTuvs = (List<TuvImplVo>) map
                    .get(UploadPageSaver.MODIFIED_TUVS);
            List newComments = (List) map.get(UploadPageSaver.NEW_COMMENTS);
            Map modifiedComments = (Map) map
                    .get(UploadPageSaver.MODIFIED_COMMENTS);
            user = (User) map.get(UploadPageSaver.USER);
            fileName = (String) map.get(UploadPageSaver.FILE_NAME);
            targetPageId = (Long) map.get(UploadPageSaver.UPLOAD_PAGE_ID);
            Boolean isLast = (Boolean) map.get(UploadPageSaver.IS_LAST_PAGE);
            isUploadingTasks = (List<Task>)map.get(UploadPageSaver.IS_UPLOADING_TASKS);

            // Notify editor of page uploaded having started.
            try
            {
                syncMgr.uploadStarted(targetPageId);
            }
            catch (Throwable ex)
            {
            }

            savePageToDb(modifiedTuvs, newComments, modifiedComments,
                    sourceLocale, targetLocale, userLocale, user, fileName,
                    targetPageId, isLast.booleanValue(), isUploadingTasks);
        }
        catch (Exception ex)
        {
            s_category.error("PageSaverListener :: Failed to save segments: "
                    + ex.getMessage(), ex);

            String localePair = OfflineEditHelper.localePair(sourceLocale,
                    targetLocale, userLocale);

            OfflineEditHelper.notifyUser(user, fileName, localePair,
                    OfflineEditHelper.UPLOAD_FAIL_SUBJECT,
                    OfflineEditHelper.UPLOAD_FAIL_MESSAGE, compandIdStr);
        }
        finally
        {
            // Notify editor of page upload having finished.
            try
            {
                syncMgr.uploadFinished(targetPageId);
            }
            catch (Throwable ex)
            {
            }

            HibernateUtil.closeSession();
            if (activityStart != null)
            {
                activityStart.end();
            }
        }
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
     * @exception UploadPageSaverException
     */
    private void savePageToDb(List<TuvImplVo> modifiedTuvs, List newComments,
            Map modifiedComments, GlobalSightLocale sourceLocale,
            GlobalSightLocale targetLocale, GlobalSightLocale userLocale,
            User user, String p_fileName, Long p_targetPageId, boolean isLast,
            List<Task> p_isUploadingTasks)throws UploadPageSaverException
    {
        String companyIdStr = null;
        long jobId = -1;
        try
        {
            TargetPage tp = ServerProxy.getPageManager().getTargetPage(
                    p_targetPageId);
            jobId = tp.getSourcePage().getJobId();
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
        Iterator tuvIter = modifiedTuvs.iterator();
        while (tuvIter.hasNext())
        {
            Tuv tuv = (Tuv) tuvIter.next();
            tuv.setLastModifiedUser(user.getUserId());

            if (isRepeatedSegments)
            {
                specTus = specTus + tuv.getTuId() + ",";
            }
        }
        saveTuvs(modifiedTuvs, jobId);

        specTus = ""; // do not do Auto-Propagate from don's email
        if (specTus != null && specTus.length() > 0)
        {
            // AutoPropagateThread.java
            AutoPropagateThread apThread = new AutoPropagateThread();
            apThread.setPickup("latest");
            apThread.setSpecTus(specTus);
            apThread.setTargetPageId("" + p_targetPageId);
            apThread.setTuScope("specifiedTus");
            apThread.setTuvScope("all");
            apThread.setUser(user);

            apThread.run();
        }

        // save modified target TUVs into in progress TM
        saveTuvsIntoInProgressTm(modifiedTuvs, sourceLocale, p_targetPageId);

        // save comments
        saveNewComments(newComments, user.getUserId(), p_targetPageId);
        saveModifiedComments(modifiedComments, user.getUserId());

        // Delete the old files for preview
        PreviewPDFHelper.deleteOldPdf(p_targetPageId, targetLocale.getId());
        PreviewPageHandler.deleteOldPreviewFile(p_targetPageId,
                targetLocale.getId());
        // After a successful save, notify user.
        // (Including when no modified Tuvs or comments are uploaded.)
        String localePair = OfflineEditHelper.localePair(sourceLocale,
                targetLocale, userLocale);

        if (isRepeatedSegments)
        {
            if (isLast)
            {
                OfflineEditHelper.notifyUser(user, p_fileName, localePair,
                        OfflineEditHelper.UPLOAD_SUCCESSFUL_SUBJECT,
                        OfflineEditHelper.UPLOAD_SUCCESSFUL_MESSAGE,
                        companyIdStr);
            }
        }
        else
        {
            OfflineEditHelper.notifyUser(user, p_fileName, localePair,
                    OfflineEditHelper.UPLOAD_SUCCESSFUL_SUBJECT,
                    OfflineEditHelper.UPLOAD_SUCCESSFUL_MESSAGE, companyIdStr);
        }
        
        if (isLast && p_isUploadingTasks != null && p_isUploadingTasks.size() > 0)
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
     * Update TUVs in DB with specified TUV contents.
     * 
     * @param p_tuvsToBeSaved
     * @throws UploadPageSaverException
     */
    private void saveTuvs(List<TuvImplVo> p_tuvsToBeSaved, long p_jobId)
            throws UploadPageSaverException
    {
        try
        {
            TuvManager mgr = ServerProxy.getTuvManager();
            mgr.saveTuvsFromOffline(p_tuvsToBeSaved, p_jobId);
        }
        catch (Exception ex)
        {
            s_category.error("Cannot save TUVs", ex);
            throw new UploadPageSaverException(ex);
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
                CommentManager mgr = getCommentManager();

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
                CommentManager mgr = getCommentManager();

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

    private SynchronizationManager getSynchronizationManager()
    {
        try
        {
            return ServerProxy.getSynchronizationManager();
        }
        catch (Exception ex)
        {
            s_category.error("Internal error: cannot send offline/online "
                    + "synchronization messages", ex);
        }

        return null;
    }

    private CommentManager getCommentManager()
    {
        try
        {
            return ServerProxy.getCommentManager();
        }
        catch (Exception ex)
        {
            s_category
                    .error("Internal error: cannot access CommentManager", ex);
        }

        return null;
    }
}
