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

package com.globalsight.everest.webapp.pagehandler.administration.comment;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.everest.comment.Comment;
import com.globalsight.everest.comment.CommentFile;
import com.globalsight.everest.comment.CommentFilesDownLoad;
import com.globalsight.everest.comment.CommentManager;
import com.globalsight.everest.comment.Issue;
import com.globalsight.everest.comment.TaskCommentInfo;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.foundation.WorkObject;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.util.comparator.CommentComparator;
import com.globalsight.everest.util.comparator.JavaLocaleComparator;
import com.globalsight.everest.util.comparator.StringComparator;
import com.globalsight.everest.util.comparator.TaskCommentInfoComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.JobSummaryHelper;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.WorkflowHandlerHelper;
import com.globalsight.everest.webapp.pagehandler.tasks.TaskDetailHelper;
import com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.SortUtil;
import com.globalsight.util.edit.EditUtil;

/**
 * <p>
 * Comment Reference CommentMainHandler is responsible for:
 * </p>
 * <ol>
 * <li>Displaying the list of available comment reference files.</li>
 * <li>Sorting the list of comment reference files.</li>
 * <li>Deleting (and updating) existing Comment Reference files.</li>
 * </ol>
 * 
 * For uploading comment reference files, see UploadHandler.
 * 
 * 
 */
public class CommentMainHandler extends PageHandler implements CommentConstants
{
    private static final Logger CATEGORY = Logger
            .getLogger(CommentMainHandler.class.getName());

    //
    // Private Members
    //
    private CommentState m_state = null;

    //
    // Constructor
    //
    public CommentMainHandler()
    {
    }

    //
    // Interface Methods: PageHandler
    //

    /**
     * Invokes this PageHandler
     * 
     * @param p_pageDescriptor
     *            the page desciptor
     * @param p_request
     *            the original request sent from the browser
     * @param p_response
     *            the original response object
     * @param p_context
     *            context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            EnvoyServletException
    {
        CommentFilesDownLoad commentFilesDownload = new CommentFilesDownLoad();
        String action = p_request.getParameter(ACTION_PARAMETER);
        String[] jobComments = p_request
                .getParameterValues(JOB_COMMENT_CHECKBOX);
        String[] activityComments = p_request
                .getParameterValues(ACTIVITY_COMMENT_CHECKBOX);
        String selectedLocale = p_request.getParameter("localeValue");
        activityComments = commentFilesDownload.removeUnrelatedIds(
                activityComments, selectedLocale);
        String[] commentIds = commentFilesDownload.mergeCommentIds(jobComments,
                activityComments);
        
        HttpSession httpSession = p_request.getSession();
        String taskId = p_request.getParameter(TASK_ID);
        if(taskId != null && !taskId.equals(""))
        {
        	TaskDetailHelper taskDetailHelper = new TaskDetailHelper();
        	taskDetailHelper.prepareTaskData(p_request, p_response, httpSession, taskId);
        	p_request.setAttribute(TASK_ID, taskId);
        }  

        if (DOWNLOAD_COMMENT_FILES.equals(action))
        {
            try
            {
                commentFilesDownload.downloadCommentFiles(commentIds,
                        p_request, p_response);
            }
            catch (Exception e)
            {
                CATEGORY.error(e.getMessage(), e);
            }
        }
        else
        {
            handleRequest(p_pageDescriptor, p_request, p_response, p_context);

            super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                    p_context);
        }

    }

    public void handleRequest(WebPageDescriptor descriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws EnvoyServletException,
            ServletException, IOException
    {
        HttpSession session = p_request.getSession();
        PermissionSet perms = (PermissionSet) session
                .getAttribute(WebAppConstants.PERMISSIONS);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        if (p_request.getParameter("toJob") != null)
        {
            long contextMenuJobId = Long.valueOf(p_request
                    .getParameter("jobId"));
            Job contextMenuJob = WorkflowHandlerHelper
                    .getJobById(contextMenuJobId);
            TaskHelper.storeObject(session, WebAppConstants.WORK_OBJECT,
                    contextMenuJob);
        }
        String commentIdPara = (String)p_request.getParameter("commentId");
        if(commentIdPara != null && commentIdPara != ""){
        	Comment comment = TaskHelper.getComment(session, Long.parseLong(commentIdPara));
        	sessionMgr.setAttribute("comment", comment);
        }
        User user = (User) sessionMgr.getAttribute(WebAppConstants.USER);
        String userId = user.getUserId();
        String wId = "";
        WorkObject wo = (WorkObject) TaskHelper.retrieveObject(session,
                WebAppConstants.WORK_OBJECT);

        if (wo == null)
        {
            // Get taskId parameter
            String taskIdParam = p_request.getParameter(TASK_ID);
            long taskId = TaskHelper.getLong(taskIdParam);
            // get task state (determines from which tab, the task details is
            // requested)
            String taskStateParam = p_request.getParameter(TASK_STATE);
            int taskState = TaskHelper.getInt(taskStateParam, -10);// -10 as
            // default
            try
            {
                // Get task
                wo = TaskHelper.getTask(user.getUserId(), taskId, taskState);
            }
            catch (Exception e)
            {
                CATEGORY.info(e);

                ResourceBundle bundle = getBundle(session);
                String stateLabel = "";
                switch (taskState)
                {
                    case Task.STATE_ACCEPTED:
                        stateLabel = bundle.getString("lb_accepted");
                        break;
                    case Task.STATE_COMPLETED:
                        stateLabel = bundle.getString("lb_finished");
                        break;
                    case Task.STATE_REJECTED:
                        stateLabel = bundle.getString("lb_rejected");
                        break;
                    case Task.STATE_ACTIVE:
                        stateLabel = bundle.getString("lb_available");
                        break;
                }
                Object[] args =
                { p_request.getParameter("jobname"), stateLabel };
                p_request.setAttribute("badresults", MessageFormat.format(
                        bundle.getString("msg_bad_task"), args));
                // remove the task from the most recently used list
                String menuName = (String) p_request.getParameter("cookie");
                TaskHelper.removeMRUtask(p_request, session, menuName,
                        p_response);
                // forward to the jsp page.
                RequestDispatcher dispatcher = p_context
                        .getRequestDispatcher("/envoy/tasks/taskSearch.jsp");

                dispatcher.forward(p_request, p_response);
                return;
            }
        }
        if (wo == null)
        {
            CATEGORY.info("Can not found work object");
            EnvoyServletException e = new EnvoyServletException(
                    "WorkObjectNotFound", null, null);
            CATEGORY.error(e.getMessage(), e);
            throw e;
        }

        String companyId = null;

        if (wo != null)
        {
            if (wo instanceof Task)
            {
                Task task = (Task) wo;
                wId = (new Long(task.getId())).toString();
                sessionMgr.setAttribute("jobName", task.getJobName());

                // added for JobDetails Page Rewirte
                JobSummaryHelper jobSummaryHelper = new JobSummaryHelper();
                Job job = WorkflowHandlerHelper.getJobById(task.getJobId());
                boolean isOk = jobSummaryHelper.packJobSummaryInfoView(p_request,
                        p_response, p_context, job);
                if (!isOk)
                {
                    return;
                }

                companyId = String.valueOf(((Task) wo).getCompanyId());
            }
            else if (wo instanceof Job)
            {
                Job job = (Job) wo;
                wId = (new Long(job.getId())).toString();
                sessionMgr.setAttribute("jobName", job.getJobName());
                sessionMgr.setAttribute("jobId", job.getJobId());
                // added for JobDetails Page Rewirte
                JobSummaryHelper jobSummaryHelper = new JobSummaryHelper();
                // prevent hibernate lazily initialize Job Object
                job = WorkflowHandlerHelper.getJobById(job.getJobId());
                boolean isOk = jobSummaryHelper.packJobSummaryInfoView(p_request,
                        p_response, p_context, job);
                if (!isOk)
                {
                    return;
                }

                companyId = String.valueOf(((Job) wo).getCompanyId());
            }
        }

        CompanyThreadLocal.getInstance().setIdValue(companyId);

        String tmpDir = WebAppConstants.COMMENT_REFERENCE_TEMP_DIR + wId
                + userId;
        String action = p_request.getParameter(TASK_ACTION);
        String commentId = "";

        // If from 'add comment' dialog
        if (action != null && action.equals(TASK_ACTION_SAVECOMMENT))
        {
            saveCommentReferences(p_request, session, user, tmpDir);
            session.setAttribute(
                    WebAppConstants.COMMENT_REFERENCE_TASK_COMMENT, null);
            sessionMgr.setAttribute(
                    WebAppConstants.COMMENT_REFERENCE_TASK_COMMENT, null);
        }
        if (action != null && action.equals(COMMENT_REFERENCE_ACTION_CANCEL))
        {
            // String path = CommentUpload.UPLOAD_BASE_DIRECTORY
            // + CommentUpload.UPLOAD_DIRECTORY
            // + tmpDir + "/" ;
            String path = AmbFileStoragePathUtils.getCommentReferenceDirPath()
                    + File.separator + tmpDir + File.separator;
            String[] dirs =
            { GENERAL, RESTRICTED };
            for (int h = 0; h < dirs.length; h++)
            {
                String dir = "";
                dir += path + "/" + dirs[h];
                File commentFile = new File(dir);
                File[] srcFiles = commentFile.listFiles();
                if (srcFiles != null)
                {
                    for (int i = 0; i < srcFiles.length; ++i)
                    {
                        srcFiles[i].delete();
                    }
                }
                commentFile.delete();
            }
            File commentFile = new File(path);
            commentFile.delete();
            session.setAttribute(
                    WebAppConstants.COMMENT_REFERENCE_TASK_COMMENT, null);
            sessionMgr.setAttribute(
                    WebAppConstants.COMMENT_REFERENCE_TASK_COMMENT, null);
        }
        sessionMgr.setAttribute("comment", null);
        sessionMgr.setAttribute("taskComment", null);
        sessionMgr.setAttribute("deletedReferences", null);
        sessionMgr.setAttribute("commentReferences", null);
        if (m_state == null)
        {
            m_state = new CommentState();
        }

        String access = "";
        if (perms.getPermissionFor(Permission.COMMENT_ACCESS_RESTRICTED))
        {
            access = RESTRICTED;
        }
        else
        {
            access = GENERAL;
        }

        // Now reload the list
        refreshCommentReferences(commentId, access);

        if (wo != null)
        {
            Locale locale = (Locale) session.getAttribute(UILOCALE);
            try
            {
                if (wo instanceof Task)
                {
                    // get the comments from all the tasks
                    Task task = (Task) wo;
                    // refresh the task from the cache
                    task = ServerProxy.getTaskManager().getTask(task.getId());
                    ArrayList comments = new ArrayList();
                    Workflow wf = task.getWorkflow();
                    Job job = wf.getJob();
                    GlobalSightLocale glocale = task.getTargetLocale();
                    // Method 1, get all comments and use filter
                    Iterator workflows = job.getWorkflows().iterator();
                    while (workflows.hasNext())
                    {
                        Workflow t_wf = (Workflow) workflows.next();
                        Hashtable tasks = t_wf.getTasks();
                        for (Iterator i = tasks.values().iterator(); i
                                .hasNext();)
                        {
                            Task t = (Task) i.next();
                            comments.addAll(t.getTaskComments());
                        }
                    }

                    // comments filter
                    for (int i = 0; i < comments.size();)
                    {
                        Comment comment = (Comment) comments.get(i);
                        WorkObject t_wo = comment.getWorkObject();
                        if (t_wo instanceof Task)
                        {
                            Task t = (Task) t_wo;
                            if (!t.getTargetLocale().equals(glocale))
                            {
                                comments.remove(i);
                                continue;
                            }
                        }
                        i++;
                    }
                    // method 1 end

                    // method 2, Only need locale filter, but have a bug without
                    // fixed
                    // List glocales = new ArrayList();
                    // glocales.add(0,glocale);
                    // comments =
                    // ServerProxy.getCommentManager().getTaskComments(job.getId(),glocales);
                    // method 2 end
                    dataForTable(p_request, session, TASK_COMMENT_LIST,
                            TASK_COMMENT_KEY, new CommentComparator(locale),
                            comments);

                    // Also get job comments (new in release 6.5)
                    dataForTable(p_request, session, JOB_COMMENT_LIST,
                            JOB_COMMENT_KEY, new CommentComparator(locale),
                            job.getJobComments());

                    // Get Segment comment summary info.
                    ArrayList<LocaleCommentsSummary> segments = getSegmentSummaryForTask(
                            task, p_request, sessionMgr, perms);
                    dataForTable(p_request, session, SEGMENT_COMMENT_LIST,
                            SEGMENT_COMMENT_KEY, new LocaleCommentsComparator(
                                    locale), segments);

                    // Set list of target locales for selection box of segment
                    // comments
                    setTargetLocalesForSegments(segments, sessionMgr, locale);
                }
                else if (wo instanceof Job)
                {
                    Job job = (Job) wo;
                    // refresh the job from the cache
                    job = ServerProxy.getJobHandler().getJobById(job.getId());

                    dataForTable(p_request, session, JOB_COMMENT_LIST,
                            JOB_COMMENT_KEY, new CommentComparator(locale),
                            job.getJobComments());
                    // Get list of target locales
                    List<GlobalSightLocale> targLocales = getValidTargetLocales(
                            job, p_request, perms);

                    // Get all activity comments for Job
                    ArrayList taskComments = (ArrayList) getTaskCommentsForJob(
                            job, targLocales, p_request);

                    dataForTable(p_request, session, TASK_COMMENT_LIST,
                            TASK_COMMENT_KEY, new TaskCommentInfoComparator(
                                    locale), taskComments);

                    // Set list of target locales for selection box of task
                    // comments
                    setTargetLocales(taskComments, sessionMgr, locale);

                    // Get Segment comment summary info.
                    ArrayList<LocaleCommentsSummary> segments = getSegmentSummaryForJob(
                            job, p_request, sessionMgr, perms);

                    dataForTable(p_request, session, SEGMENT_COMMENT_LIST,
                            SEGMENT_COMMENT_KEY, new LocaleCommentsComparator(
                                    locale), segments);

                    // Set list of target locales for selection box of segment
                    // comments
                    setTargetLocalesForSegments(segments, sessionMgr, locale);
                }
            }
            catch (Exception e)
            {
                throw new EnvoyServletException(e);
            }
        }
    }

    /**
     * Calls the remote server to fetch all task Comments for a job
     */
    private List getTaskCommentsForJob(Job p_job, List p_localesList,
            HttpServletRequest p_request) throws EnvoyServletException
    {
        String localeName = p_request.getParameter(TASK_COMMENT_KEY + "Filter");
        if (localeName != null)
        {
            int idx = localeName.indexOf(',');
            String language = localeName.substring(0, idx);
            String country = localeName.substring(idx + 1);
            Locale locale = new Locale(language, country);
            p_request.setAttribute("selectedLocale", locale.getDisplayName());
        }
        ArrayList taskComments = null;
        try
        {
            taskComments = ServerProxy.getCommentManager().getTaskComments(
                    p_job.getId(), p_localesList);
        }
        catch (GeneralException ex)
        {
            throw new EnvoyServletException(ex);
        }
        catch (RemoteException ex)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_REMOTE, ex);
        }
        return taskComments;
    }

    /**
     * Get the total # open comments count for each target page. Set it in the
     * request.
     */
    private ArrayList<LocaleCommentsSummary> getSegmentSummaryForJob(Job job,
            HttpServletRequest request, SessionManager sessionMgr,
            PermissionSet perms) throws EnvoyServletException
    {
        CommentManager manager = null;
        try
        {
            manager = ServerProxy.getCommentManager();
        }
        catch (GeneralException ex)
        {
            throw new EnvoyServletException(ex);
        }

        // Set the selected locale in the request
        String localeName = request
                .getParameter(SEGMENT_COMMENT_KEY + "Filter");
        if (localeName != null)
        {
            int idx = localeName.indexOf(',');
            String language = localeName.substring(0, idx);
            String country = localeName.substring(idx + 1);
            Locale locale = new Locale(language, country);
            request.setAttribute("segmentSelectedLocale",
                    locale.getDisplayName());
        }

        // get just the number of issues in OPEN state
        // query is also considered a subset of the OPEN state
        List<String> statesOpen = new ArrayList<String>();
        statesOpen.add(Issue.STATUS_OPEN);
        statesOpen.add(Issue.STATUS_QUERY);
        statesOpen.add(Issue.STATUS_REJECTED);

        List<String> statesClosed = new ArrayList<String>();
        statesClosed.add(Issue.STATUS_CLOSED);

        ArrayList<LocaleCommentsSummary> summary = new ArrayList<LocaleCommentsSummary>();
        for (Workflow wf : job.getWorkflows())
        {
            if (wf.getState().equals(Workflow.CANCELLED))
                continue;

            GlobalSightLocale targLocale = wf.getTargetLocale();
            List<TargetPage> pages = wf.getTargetPages();
            List<Long> tpIds = new ArrayList<Long>();
            for (int j = 0; j < pages.size(); j++)
            {
                TargetPage tPage = (TargetPage) pages.get(j);
                tpIds.add(tPage.getIdAsLong());
            }

            HashMap<Long, Integer> openCounts = null;
            HashMap<Long, Integer> closedCounts = null;
            try
            {
                openCounts = manager.getIssueCountPerTargetPage(
                        Issue.TYPE_SEGMENT, tpIds, statesOpen);
                closedCounts = manager.getIssueCountPerTargetPage(
                        Issue.TYPE_SEGMENT, tpIds, statesClosed);
            }
            catch (Exception e)
            {
                throw new EnvoyServletException(e);
            }

            if (openCounts.size() == 0 && closedCounts.size() == 0)
            {
                continue;
            }

            ArrayList<PageCommentsSummary> pageSummaries = new ArrayList<PageCommentsSummary>();
            for (int k = 0; k < pages.size(); k++)
            {
                TargetPage tPage = (TargetPage) pages.get(k);
                int countOpen = (openCounts.get(tPage.getIdAsLong()) == null ? 0
                        : openCounts.get(tPage.getIdAsLong()));
                int countClosed = (closedCounts.get(tPage.getIdAsLong()) == null ? 0
                        : closedCounts.get(tPage.getIdAsLong()));
                if ((countOpen + countClosed) > 0)
                {
                    PageCommentsSummary ps = new PageCommentsSummary(tPage);
                    ps.setOpenCommentsCount(countOpen);
                    ps.setClosedCommentsCount(countClosed);
                    pageSummaries.add(ps);
                }
            }

            LocaleCommentsSummary ls = new LocaleCommentsSummary(targLocale);
            ls.setPageCommentsSummary(pageSummaries);
            summary.add(ls);
        }

        return summary;
    }

    /**
     * Get the total # open comments count for each target page. Set it in the
     * request.
     */
    private ArrayList<LocaleCommentsSummary> getSegmentSummaryForTask(
            Task p_task, HttpServletRequest request, SessionManager sessionMgr,
            PermissionSet perms) throws EnvoyServletException
    {
        CommentManager manager = null;
        try
        {
            manager = ServerProxy.getCommentManager();
        }
        catch (GeneralException ex)
        {
            throw new EnvoyServletException(ex);
        }

        // Set the selected locale in the request
        String localeName = request
                .getParameter(SEGMENT_COMMENT_KEY + "Filter");
        if (localeName != null)
        {
            int idx = localeName.indexOf(',');
            String language = localeName.substring(0, idx);
            String country = localeName.substring(idx + 1);
            Locale locale = new Locale(language, country);
            request.setAttribute("segmentSelectedLocale",
                    locale.getDisplayName());
        }

        ArrayList<LocaleCommentsSummary> summary = new ArrayList<LocaleCommentsSummary>();
        Workflow wf = (Workflow) p_task.getWorkflow();
        if (Workflow.CANCELLED.equals(wf.getState()))
        {
            return summary;
        }
        else
        {
            // get just the number of issues in OPEN state
            // query is also considered a subset of the OPEN state
            List<String> statesOpen = new ArrayList<String>();
            statesOpen.add(Issue.STATUS_OPEN);
            statesOpen.add(Issue.STATUS_QUERY);
            statesOpen.add(Issue.STATUS_REJECTED);

            List<String> statesClosed = new ArrayList<String>();
            statesClosed.add(Issue.STATUS_CLOSED);

            GlobalSightLocale targLocale = wf.getTargetLocale();
            List<TargetPage> pages = wf.getTargetPages();
            List<Long> tpIds = new ArrayList<Long>();
            for (int j = 0; j < pages.size(); j++)
            {
                TargetPage tPage = (TargetPage) pages.get(j);
                tpIds.add(tPage.getIdAsLong());
            }

            HashMap<Long, Integer> openCounts = null;
            HashMap<Long, Integer> closedCounts = null;
            try
            {
                openCounts = manager.getIssueCountPerTargetPage(
                        Issue.TYPE_SEGMENT, tpIds, statesOpen);
                closedCounts = manager.getIssueCountPerTargetPage(
                        Issue.TYPE_SEGMENT, tpIds, statesClosed);
            }
            catch (Exception e)
            {
                throw new EnvoyServletException(e);
            }

            if (openCounts.size() == 0 && closedCounts.size() == 0)
            {
                return summary;
            }

            ArrayList<PageCommentsSummary> pageSummaries = new ArrayList<PageCommentsSummary>();
            for (int k = 0; k < pages.size(); k++)
            {
                TargetPage tPage = (TargetPage) pages.get(k);
                int countOpen = (openCounts.get(tPage.getIdAsLong()) == null ? 0
                        : openCounts.get(tPage.getIdAsLong()));
                int countClosed = (closedCounts.get(tPage.getIdAsLong()) == null ? 0
                        : closedCounts.get(tPage.getIdAsLong()));
                if ((countOpen + countClosed) > 0)
                {
                    PageCommentsSummary ps = new PageCommentsSummary(tPage);
                    ps.setOpenCommentsCount(countOpen);
                    ps.setClosedCommentsCount(countClosed);
                    pageSummaries.add(ps);
                }
            }

            LocaleCommentsSummary ls = new LocaleCommentsSummary(targLocale);
            ls.setPageCommentsSummary(pageSummaries);
            summary.add(ls);
        }

        return summary;
    }

    private List<GlobalSightLocale> getValidTargetLocales(Job job,
            HttpServletRequest request, PermissionSet perms)
    {
        HttpSession session = request.getSession();
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        User user = (User) sessionMgr.getAttribute(USER);
        List<GlobalSightLocale> validLocales = new ArrayList<GlobalSightLocale>();
        for (Workflow wf : job.getWorkflows())
        {
            if (wf.getState().equals(Workflow.CANCELLED)
                    || invalidForWorkflowOwner(user.getUserId(), perms, wf))
            {
                boolean canSeeAllJobs = perms
                        .getPermissionFor(Permission.JOB_SCOPE_ALL);
                if (canSeeAllJobs == false)
                    continue;
            }
            // if (wf.getState().equals(Workflow.CANCELLED))
            // {
            // continue;
            // }
            validLocales.add(wf.getTargetLocale());
        }
        return validLocales;
    }

    /**
     * The ui needs to populate a selection box of target locales. Loop through,
     * adding them to a hash table to get the list. Then sort and put in the
     * session.
     */
    private void setTargetLocales(ArrayList taskComments,
            SessionManager sessionMgr, Locale locale)
    {
        Hashtable locales = new Hashtable();
        for (int i = 0; i < taskComments.size(); i++)
        {
            TaskCommentInfo info = (TaskCommentInfo) taskComments.get(i);
            locales.put(info.getTargetLocale().getDisplayName(),
                    info.getTargetLocale());
        }
        ArrayList localesList = new ArrayList(locales.values());
        JavaLocaleComparator comp = new JavaLocaleComparator(locale);
        comp.setType(JavaLocaleComparator.DISPLAYNAME);
        SortUtil.sort(localesList, comp);
        sessionMgr.setAttribute("targetLocales", localesList);
    }

    /**
     * The ui needs to populate a selection box of target locales. Loop through,
     * adding them to a hash table to get the list. Then sort and put in the
     * session.
     */
    private void setTargetLocalesForSegments(ArrayList segmentComments,
            SessionManager sessionMgr, Locale locale)
    {
        Hashtable locales = new Hashtable();
        for (int i = 0; i < segmentComments.size(); i++)
        {
            LocaleCommentsSummary ls = (LocaleCommentsSummary) segmentComments
                    .get(i);
            locales.put(ls.getTargetLocale().getLocale().getDisplayName(), ls
                    .getTargetLocale().getLocale());
        }
        ArrayList localesList = new ArrayList(locales.values());
        JavaLocaleComparator comp = new JavaLocaleComparator(locale);
        comp.setType(JavaLocaleComparator.DISPLAYNAME);
        SortUtil.sort(localesList, comp);
        sessionMgr.setAttribute("targetLocalesForSegments", localesList);
    }

    /**
     * Calls the remote server to refresh Comment Reference data
     */
    private void refreshCommentReferences(String p_commentId, String p_access)
            throws EnvoyServletException
    {
        ArrayList commentReferences = null;

        try
        {
            commentReferences = ServerProxy.getCommentManager()
                    .getCommentReferences(p_commentId, p_access);
        }
        catch (GeneralException ex)
        {
            throw new EnvoyServletException(ex);
        }
        catch (RemoteException ex)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_REMOTE, ex);
        }

        m_state.setCommentReferences(commentReferences);

    }

    private Comment addTaskComment(HttpServletRequest p_request,
            HttpSession p_session, User p_user, boolean isNewComment)
            throws ServletException, IOException, EnvoyServletException
    {
        HttpSession session = p_request.getSession();
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);

        String commentInput = (String) p_request.getParameter("taskComment");
        if (commentInput == null)
        {
            commentInput = (String) sessionMgr.getAttribute("taskComment");
        }
        String commentText = null;
        Comment comment = null;

        // commentInput is in UTF-8 encoding, convert to unicode
        if (commentInput != null)
        {
            commentText = EditUtil.utf8ToUnicode(commentInput);
        }
        if (commentText != null)
        {
            WorkObject wo = (WorkObject) TaskHelper.retrieveObject(p_session,
                    WORK_OBJECT);
            if (wo != null)
            {
                Comment commentObj = (Comment) sessionMgr
                        .getAttribute("comment");
                if (wo instanceof Task)
                {
                    Task task = (Task) wo;
                    if (isNewComment)
                    {
                        // GBS-1012: Added for create job comment from
                        // Task/Activity
                        String saveCommStatus = (String) p_request
                                .getParameter(SAVE_COMMENT_STATUS);
                        if (SAVE_COMMENT_STATUS_JT.equals(saveCommStatus))
                        {
                            Job tempJob = null;
                            try
                            {
                                tempJob = ServerProxy.getJobHandler()
                                        .getJobById(task.getJobId());
                                comment = TaskHelper.saveComment(tempJob,
                                        tempJob.getId(), p_user.getUserId(),
                                        commentText);
                            }
                            catch (Exception e)
                            {
                            }
                        }// GBS-1012:end
                        else
                        {
                            comment = TaskHelper.saveComment(wo, task.getId(),
                                    p_user.getUserId(), commentText);
                        }
                    }
                    else
                    {
                        comment = TaskHelper.updateComment(commentObj.getId(),
                                p_user.getUserId(), commentText);
                    }
                }
                else if (wo instanceof Job)
                {
                    Job job = (Job) wo;
                    if (isNewComment)
                    {
                        // new comment
                        comment = TaskHelper.saveComment(wo, job.getId(),
                                p_user.getUserId(), commentText);
                    }
                    else
                    {
                        // update comment
                        comment = TaskHelper.updateComment(commentObj.getId(),
                                p_user.getUserId(), commentText);
                    }

                    // refresh the job work object since the comment has been
                    // added or updated
                    try
                    {
                        wo = ServerProxy.getJobHandler()
                                .getJobById(job.getId());
                        TaskHelper.storeObject(p_session, WORK_OBJECT, wo);
                    }
                    catch (Exception e)
                    {
                        // ignore if couldn't retrieve or store since
                        // it already exists in the session
                    }
                }
            }
        }
        return comment;
    }

    private void saveCommentReferences(HttpServletRequest p_request,
            HttpSession p_session, User p_user, String tmpDir)
            throws ServletException, IOException, EnvoyServletException
    {
        SessionManager sessionMgr = (SessionManager) p_session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        boolean newComment = true;
        Comment commentObj = (Comment) sessionMgr.getAttribute("comment");
        if (commentObj != null)
            newComment = false;
        String commentId = new Long(addTaskComment(p_request, p_session,
                p_user, newComment).getId()).toString();
        // String fullTmpDir = CommentUpload.UPLOAD_BASE_DIRECTORY +
        // CommentUpload.UPLOAD_DIRECTORY + tmpDir;
        // File tmpPath = new File(fullTmpDir);
        // String dir = CommentUpload.UPLOAD_BASE_DIRECTORY +
        // CommentUpload.UPLOAD_DIRECTORY + commentId;
        // File finalPath = new File(dir);
        File tmpPath = new File(
                AmbFileStoragePathUtils.getCommentReferenceDir(), tmpDir);
        File finalPath = new File(
                AmbFileStoragePathUtils.getCommentReferenceDir(), commentId);
        if (newComment)
        {
            tmpPath.renameTo(finalPath);
        }
        else
        {
            if (tmpPath.exists())
            {
                // User added more attached files to existing comment
                rename(tmpPath.toString(), GENERAL, commentId);
                rename(tmpPath.toString(), RESTRICTED, commentId);
                tmpPath.delete();
            }
            ArrayList list = (ArrayList) sessionMgr
                    .getAttribute("deletedReferences");
            if (list != null)
            {
                // Need to loop through deletedReferences to remove references
                for (int i = 0; i < list.size(); i++)
                {
                    CommentFile cf = (CommentFile) list.get(i);
                    try
                    {
                        ServerProxy.getCommentManager().deleteCommentReference(
                                cf, commentId);
                    }
                    catch (GeneralException ex)
                    {
                        throw new EnvoyServletException(ex);
                    }
                }
            }
        }
    }

    /**
     * Move files from tmp to final. The comment already existed, so can't just
     * rename the directory.
     */
    private void rename(String p_tmpDir, String p_subDir, String commentId)
            throws ServletException, IOException, EnvoyServletException
    {
        File dir = new File(p_tmpDir, p_subDir);
        if (dir.exists())
        {
            // String finalDir = CommentUpload.UPLOAD_BASE_DIRECTORY +
            // CommentUpload.UPLOAD_DIRECTORY + commentId +
            // "/" + p_subDir;
            // File finalPath = new File(finalDir);
            File finalPath = new File(
                    AmbFileStoragePathUtils.getCommentReferenceDir(), commentId
                            + File.separator + p_subDir);
            if (!finalPath.exists())
                finalPath.mkdirs();
            String[] list = dir.list();
            for (int i = 0; i < list.length; i++)
            {
                File f = new File(p_tmpDir + "/" + p_subDir + "/" + list[i]);
                // File f2 = new File(finalDir + "/" + list[i]);
                File f2 = new File(finalPath, list[i]);
                f.renameTo(f2);
            }
            dir.delete();
        }
    }

    /**
     * Get list of comments for displaying in table
     */
    private void dataForTable(HttpServletRequest p_request,
            HttpSession p_session, String listname, String keyname,
            StringComparator comparator, List p_comments)
            throws EnvoyServletException
    {

        try
        {
            setTableNavigation(p_request, p_session, p_comments, comparator,
                    9999, listname, keyname);
        }
        catch (Exception e)
        {
            // Config exception (already has message key...)
            throw new EnvoyServletException(e);
        }
    }
}
