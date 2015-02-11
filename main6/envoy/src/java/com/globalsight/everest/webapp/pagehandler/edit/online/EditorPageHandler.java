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

package com.globalsight.everest.webapp.pagehandler.edit.online;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.config.UserParamNames;
import com.globalsight.everest.comment.Issue;
import com.globalsight.everest.comment.IssueImpl;
import com.globalsight.everest.edit.CommentHelper;
import com.globalsight.everest.edit.online.CommentThreadView;
import com.globalsight.everest.edit.online.CommentView;
import com.globalsight.everest.edit.online.PageInfo;
import com.globalsight.everest.edit.online.PaginateInfo;
import com.globalsight.everest.edit.online.RenderingOptions;
import com.globalsight.everest.edit.online.SegmentView;
import com.globalsight.everest.edit.online.UIConstants;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.tuv.TuImpl;
import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper;
import com.globalsight.everest.webapp.pagehandler.terminology.management.FileUploadHelper;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.workflow.WorkflowConstants;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.ling.docproc.extractor.html.OfficeContentPostFilterHelper;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.FileUtil;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.edit.EditUtil;

/**
 * <p>
 * EditorPageHandler is responsible for:
 * </p>
 * <ol>
 * <li>Displaying the editor screen with both source and target page.</li>
 * <li>Showing the Segment Editor.</li>
 * </ol>
 */
public class EditorPageHandler extends PageHandler implements EditorConstants
{
    private static final Logger CATEGORY = Logger
            .getLogger(EditorPageHandler.class);

    private static int DEFAULT_VIEWMODE_IF_NO_PREVIEW = VIEWMODE_TEXT;
    
    /**
     * Determines whether PMs can edit all target pages.
     * 
     * WARNING: Care must be taken outside of the system - via phone or email -
     * to prevent multiple people from editing the same target page at the same
     * time.
     */
    static public boolean s_pmCanEditTargetPages = false;
    static public boolean s_pmCanEditSnippets = false;

    static
    {
        try
        {
            SystemConfiguration sc = SystemConfiguration.getInstance();

            s_pmCanEditTargetPages = sc
                    .getBooleanParameter("editalltargetpages.allowed");
            s_pmCanEditSnippets = sc
                    .getBooleanParameter("editallsnippets.allowed");
        }
        catch (Throwable e)
        {
            // Do nothing if configuration is not available.
        }
    }

    //
    // Constructor
    //
    public EditorPageHandler()
    {
        super();
    }

    //
    // Interface Methods: PageHandler
    //

    /**
     * Prepares the EditorState object that all invocations of this PageHandler
     * require. (Almost) All Main Editor pages (me_xx.jsp) go through this
     * handler.
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
        HttpSession session = p_request.getSession();
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);

        Locale uiLocale = (Locale) session
                .getAttribute(WebAppConstants.UILOCALE);
        EditorState state = (EditorState) sessionMgr
                .getAttribute(WebAppConstants.EDITORSTATE);

        String srcPageId = p_request
                .getParameter(WebAppConstants.SOURCE_PAGE_ID);
        String trgPageId = p_request
                .getParameter(WebAppConstants.TARGET_PAGE_ID);
        String jobId = p_request.getParameter(WebAppConstants.JOB_ID);
        String taskId = p_request.getParameter(WebAppConstants.TASK_ID);

        // Get user object for the person who has logged in.
        User user = TaskHelper.getUser(session);

        Boolean assigneeValue = (Boolean) TaskHelper.retrieveObject(session,
                WebAppConstants.IS_ASSIGNEE);
        boolean isAssignee = assigneeValue == null ? true : assigneeValue
                .booleanValue();

        // Decide from which screen we've been called.

        // From Activity Details (Translator opening pages read-write or
        // read-only)
        if (taskId != null && srcPageId != null && trgPageId != null)
        {
            // Flag to cache the segment data for the first 3 editable segments.
            // If from activity details page, this will be initialized to "yes";
            // When cache data is executed, this will be removed from session.
            sessionMgr.setAttribute(WebAppConstants.NEED_CACHE_SEGMENT_DATA,
                    "yes");

            sessionMgr.setAttribute(WebAppConstants.IS_FROM_ACTIVITY, "yes");
            // store jobId, target language and source page id for Lisa QA
            // report
            Task theTask = (Task) TaskHelper.retrieveObject(session,
                    WebAppConstants.WORK_OBJECT);
            sessionMgr.setAttribute(WebAppConstants.JOB_ID,
                    Long.toString(theTask.getJobId()));
            sessionMgr.setAttribute(WebAppConstants.TARGETVIEW_LOCALE, theTask
                    .getTargetLocale().getDisplayName());
            sessionMgr.setAttribute(WebAppConstants.SOURCE_PAGE_ID, srcPageId);

            state = new EditorState();

            EditorHelper.initEditorManager(state);
            EditorHelper.initEditorOptions(state, session);

            sessionMgr.setAttribute(WebAppConstants.EDITORSTATE, state);

            initializeFromActivity(state, session, user.getUserId(), taskId,
                    srcPageId, trgPageId, isAssignee, p_request, uiLocale);

            initState(state, session);

            // Clear cache data anyway before open pop-up editor,the cache data
            // is probably for another page.
            // When from Job Details (Admin or PM opening pages read-only),need
            // not do this.
            removeParameterFromSession(sessionMgr,
                    WebAppConstants.PAGE_TU_TUV_SUBID_SET);
            removeParameterFromSession(sessionMgr,
                    WebAppConstants.SEGMENT_VIEW_MAP);
        }
        // From Job Details (Admin or PM opening pages read-only)
        else if (jobId != null && srcPageId != null)
        {
            // being assignee is not important when accessing editor from job
            // details.
            isAssignee = false;
            TaskHelper.storeObject(session, IS_ASSIGNEE,
                    new Boolean(isAssignee));
            state = new EditorState();

            EditorHelper.initEditorManager(state);
            EditorHelper.initEditorOptions(state, session);

            sessionMgr.setAttribute(WebAppConstants.EDITORSTATE, state);
            // store jobId, target language and source page id for Lisa QA
            // report
            sessionMgr.setAttribute(WebAppConstants.JOB_ID, jobId);
            sessionMgr.setAttribute(WebAppConstants.TARGETVIEW_LOCALE,
                    getTargetLang(jobId, srcPageId));
            sessionMgr.setAttribute(WebAppConstants.SOURCE_PAGE_ID, srcPageId);

            initializeFromJob(state, p_request, jobId, srcPageId, trgPageId,
                    uiLocale, user);

            initState(state, session);
        }

        dispatchJSP(p_pageDescriptor, p_request, p_response, p_context,
                sessionMgr, state, user, isAssignee);

        super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                p_context);
    }

    /**
     * Get the target language based on job id and source page id
     * 
     * @throws EnvoyServletException
     */
    private String getTargetLang(String p_jobId, String p_srcPageId)
            throws EnvoyServletException
    {
        try
        {
            Job job = ServerProxy.getJobHandler().getJobById(
                    Long.parseLong(p_jobId));
            Collection wfs = job.getWorkflows();
            for (Iterator it = wfs.iterator(); it.hasNext();)
            {
                Workflow wf = (Workflow) it.next();
                if (Workflow.PENDING.equals(wf.getState())
                        || Workflow.CANCELLED.equals(wf.getState())
                        || Workflow.EXPORT_FAILED.equals(wf.getState())
                        || Workflow.IMPORT_FAILED.equals(wf.getState()))
                {
                    continue;
                }
                Collection targetPages = wf.getTargetPages();
                for (Iterator itr = targetPages.iterator(); itr.hasNext();)
                {
                    TargetPage tp = (TargetPage) itr.next();
                    if (p_srcPageId.equals(Long.toString(tp.getSourcePage()
                            .getId())))
                    {
                        return wf.getTargetLocale().getDisplayName();
                    }
                }
            }
        }
        catch (Exception e)
        {
            CATEGORY.error("Problem getting job from database ", e);
            throw new EnvoyServletException(e);
        }
        return "";
    }

    /**
     * Executes all actions sent in from the UI and updates the EditorState to
     * have correct data for the JSPs.
     */
    private void dispatchJSP(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context, SessionManager p_sessionMgr,
            EditorState p_state, User p_user, boolean p_isTaskAssignee)
            throws ServletException, IOException, EnvoyServletException
    {
        SegmentView segmentView = (SegmentView) p_sessionMgr
                .getAttribute(WebAppConstants.SEGMENTVIEW);

        CommentView commentView = (CommentView) p_sessionMgr
                .getAttribute(WebAppConstants.COMMENTVIEW);

        HttpSession session = p_request.getSession();
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);

        User user = TaskHelper.getUser(session);
        String userName = user.getUserId();
        p_state.setUserName(userName);

        boolean bUpdateSource = false;
        boolean bUpdateTarget = false;

        EditorState.Layout layout = p_state.getLayout();

        String value;
        if ((value = p_request.getParameter("srcViewMode")) != null)
        {
            layout.setSourceViewMode(Integer.parseInt(value));
            bUpdateSource = true;
        }
        if ((value = p_request.getParameter("trgViewMode")) != null)
        {
            layout.setTargetViewMode(Integer.parseInt(value));
            bUpdateTarget = true;
        }
        if ((value = p_request.getParameter("singlePage")) != null)
        {
            layout.setSinglePage(Integer.parseInt(value));
        }
        if ((value = p_request.getParameter("singlePageSource")) != null)
        {
            layout.setSinglePageIsSource(Integer.parseInt(value));
        }
        if ((value = p_request.getParameter("horizontal")) != null)
        {
            layout.setHorizontal(Integer.parseInt(value));
        }
        if ((value = p_request.getParameter("editAll")) != null)
        {
            if (p_state.canEditAll())
            {
                p_state.setEditAllState(Integer.parseInt(value));
                bUpdateTarget = true;

                // As "Lock" or "Unlock" may change the previous-next segment
                // order,remove them from session.
                removeParameterFromSession(sessionMgr,
                        WebAppConstants.PAGE_TU_TUV_SUBID_SET);
                // SegmentViewMap will not be removed here.
            }
        }
        if ((value = p_request.getParameter(WebAppConstants.REVIEW_MODE)) != null)
        {
            p_state.setTargetPageHtml(null);

            if ("true".equals(value))
            {
                p_state.setReviewMode();
            }
            else if (p_state.getUserIsPm())
            {
                p_state.setViewerMode();
            }
            else
            {
                p_state.setEditorMode();
            }
        }

        if ((value = p_request.getParameter("cmtAction")) != null)
        {
            executeCommentCommand(p_state, commentView, p_request, p_user);
        }

        // Sat Mar 19 00:43:53 2005 CvdL
        // PM switched target locale in main editor. If there are no
        // GS tags, only the me_target frame is reloaded. If the page
        // has GS tags, the content frame gets reloaded (me_pane2 or
        // me_split) and the source page view + cache is invalidated.
        if ((value = p_request.getParameter(WebAppConstants.TARGETVIEW_LOCALE)) != null)
        {
            // Clear comments from previous target locales.
            p_state.setCommentThreads(null);

            p_state.setTargetViewLocale(EditorHelper.getLocale(value));
            p_state.setTargetPageHtml(null);

            p_sessionMgr.setAttribute(WebAppConstants.TARGETVIEW_LOCALE,
                    EditorHelper.getLocale(value).getDisplayName());

            if (p_state.hasGsaTags())
            {
                p_state.clearSourcePageHtml();
                EditorHelper.invalidateCachedTemplates(p_state);
            }
        }

        if ((value = p_request.getParameter("save")) != null)
        {
            long tuId = p_state.getTuId();
            long tuvId = p_state.getTuvId();
            long subId = p_state.getSubId();

            try
            {
                // Updated segment arrives in UTF-8, decode to Unicode
                value = EditUtil.utf8ToUnicode(value);

                EditorHelper.updateSegment(p_state, segmentView, tuId, tuvId,
                        subId, value, userName);

                // Delete the old pdf file for the Indd preview
                PreviewPDFPageHandler.deleteOldPdf(p_state.getTargetPageId()
                        .longValue(), p_state.getTargetLocale().getId());
                PreviewPageHandler.deleteOldPreviewFile(p_state
                        .getTargetPageId().longValue(), p_state
                        .getTargetLocale().getId());

                // As target is changed,remove this from cache to ensure it is
                // obtained again from DB when cache data.
                ConcurrentHashMap segmentViewMap = (ConcurrentHashMap) sessionMgr
                        .getAttribute(WebAppConstants.SEGMENT_VIEW_MAP);
                if (segmentViewMap != null && segmentViewMap.size() > 0)
                {
                    String key = tuId + "_" + tuvId + "_" + subId;
                    segmentViewMap.remove(key);
                    sessionMgr.setAttribute(WebAppConstants.SEGMENT_VIEW_MAP,
                            segmentViewMap);
                }
            }
            catch (EnvoyServletException e)
            {
                // This should, of course, never fail. If it fails,
                // we just redisplay the current state.
                CATEGORY.error("ME ignoring update exception ", e);
            }
            catch (Exception e)
            {
                CATEGORY.error("ME ignoring update exception ", e);
            }

            bUpdateTarget = true;
        }

        // Sat Jun 07 00:56:22 2003 CvdL: remember the segment
        // last viewed in the Segment Editor so the Main Editor
        // can highlight it when it loads.
        // Wed May 11 23:48:48 2005 CvdL: reuse for opening editor
        // with a specific segment highlighted.
        setCurrentEditorSegment(p_state, p_request);

        // next & previous page
        if ((value = p_request.getParameter("refresh")) != null)
        {
            int i_direction = Integer.parseInt(value);
            boolean fromActivity = false;
            String att = (String) p_sessionMgr
                    .getAttribute(WebAppConstants.IS_FROM_ACTIVITY);
            if (att != null && att.equals("yes"))
            {
                fromActivity = true;
            }
            if (i_direction == -1) // previous file
            {
                previousPage(p_state, p_request.getSession(), fromActivity);
                while (!p_state.isFirstPage()
                        && (p_state.getTuIds() == null || p_state.getTuIds()
                                .size() == 0))
                {
                    previousPage(p_state, p_request.getSession(), fromActivity);
                }
            }
            else if (i_direction == 1) // next file
            {
                nextPage(p_state, p_request.getSession(), fromActivity);
                while (!p_state.isLastPage()
                        && (p_state.getTuIds() == null || p_state.getTuIds()
                                .size() == 0))
                {
                    nextPage(p_state, p_request.getSession(), fromActivity);
                }
            }
            else if (i_direction == -11) // previous page
            {
                bUpdateSource = true;// in this case,source needs to be updated
                bUpdateTarget = true;

                int oldCurrentPageNum = p_state.getPaginateInfo()
                        .getCurrentPageNum();
                int newCurrentPageNum = oldCurrentPageNum - 1;
                if (newCurrentPageNum < 1)
                {
                    p_state.getPaginateInfo().setCurrentPageNum(
                            p_state.getPaginateInfo().getTotalPageNum());
                }
                else
                {
                    p_state.getPaginateInfo().setCurrentPageNum(
                            newCurrentPageNum);
                }
            }
            else if (i_direction == 11) // next page
            {
                bUpdateSource = true;// in this case,source needs to be updated
                bUpdateTarget = true;

                int oldCurrentPageNum = p_state.getPaginateInfo()
                        .getCurrentPageNum();
                int newCurrentPageNum = oldCurrentPageNum + 1;
                if (newCurrentPageNum > p_state.getPaginateInfo()
                        .getTotalPageNum())
                {
                    p_state.getPaginateInfo().setCurrentPageNum(1);
                }
                else
                {
                    p_state.getPaginateInfo().setCurrentPageNum(
                            newCurrentPageNum);
                }
            }
            else
            {
                // redisplay current page - set flag to update target view
                bUpdateTarget = true;
            }

            // When you modify "edit segment" page and check the "close comment"
            // checkbox and save it, in the comments page the stater need to
            // be refreshed. So here refresh the CommentThreadView in
            // the EditorState.
            CommentThreadView view = p_state.getCommentThreads();

            if (view != null)
            {
                String sortedBy = view.getSortedBy();
                view = EditorHelper.getCommentThreads(p_state);
                view.sort(sortedBy);
                p_state.setCommentThreads(view);
            }

            String currentSrcPageId = p_state.getCurrentPage()
                    .getSourcePageId().toString();
            p_sessionMgr.setAttribute(WebAppConstants.SOURCE_PAGE_ID,
                    currentSrcPageId);
        }

        if ((value = p_request.getParameter("search")) != null)
        {
            p_sessionMgr.setAttribute("userNameList", p_state
                    .getEditorManager().getPageLastModifyUserList(p_state));

            p_sessionMgr.setAttribute("sidList", p_state.getEditorManager()
                    .getPageSidList(p_state));
            p_sessionMgr.setAttribute("from", "online");
        }

        // The user may have chosen preview mode as default, if that
        // is not available switch to TEXT.
        if (!EditUtil.hasPreviewMode(p_state.getPageFormat()))
        {
            if (layout.getSourceViewMode() == VIEWMODE_PREVIEW)
            {
                layout.setSourceViewMode(DEFAULT_VIEWMODE_IF_NO_PREVIEW);
            }

            if (layout.getTargetViewMode() == VIEWMODE_PREVIEW)
            {
                layout.setTargetViewMode(DEFAULT_VIEWMODE_IF_NO_PREVIEW);
            }
        }

        // Check for any offline uploads that affect this page.
        EditorHelper.checkSynchronizationStatus(p_state);

        // Thu Jan 09 23:58:56 2003 CvdL: Source views are computed on
        // demand, also when the editor is first opened. Check if
        // me_source or me_target is getting called and then update.
        boolean isIE = (p_request.getHeader("User-Agent").toLowerCase()
                .indexOf("msie")) != -1 ? true : false;
        if (bUpdateSource || needSourcePageView(p_pageDescriptor, p_state))
        {
            if (p_request.getParameter("searchByUser") != null)
            {
                String userId = p_request.getParameter("searchByUser");
                HashMap<String, String> hm = new HashMap<String, String>();
                hm.put("userId", userId);
                updateSourcePageView(p_state, p_request, p_isTaskAssignee,
                        isIE, hm);
            }
            else if (p_request.getParameter("searchBySid") != null)
            {
                String sid = p_request.getParameter("searchBySid");
                HashMap<String, String> hm = new HashMap<String, String>();
                hm.put("sid", sid);
                updateSourcePageView(p_state, p_request, p_isTaskAssignee,
                        isIE, hm);
            }
            else
            {
                updateSourcePageView(p_state, p_request, p_isTaskAssignee,
                        isIE, null);
            }
        }

        if (bUpdateTarget || needTargetPageView(p_pageDescriptor, p_state))
        {
            if (p_request.getParameter("searchByUser") != null)
            {
                String userId = p_request.getParameter("searchByUser");
                HashMap<String, String> hm = new HashMap<String, String>();
                hm.put("userId", userId);

                updateTargetPageView(p_state, p_request.getSession(),
                        p_isTaskAssignee, isIE, hm);
            }
            else if (p_request.getParameter("searchBySid") != null)
            {
                String sid = p_request.getParameter("searchBySid");
                HashMap<String, String> hm = new HashMap<String, String>();
                hm.put("sid", sid);

                updateTargetPageView(p_state, p_request.getSession(),
                        p_isTaskAssignee, isIE, hm);
            }
            else
            {
                updateTargetPageView(p_state, p_request.getSession(),
                        p_isTaskAssignee, isIE, null);
            }
        }

        // comment pane needs comment data
        if (needComments(p_pageDescriptor))
        {
            CommentThreadView view = p_state.getCommentThreads();

            if (view == null)
            {
                view = EditorHelper.getCommentThreads(p_state);
                p_state.setCommentThreads(view);
            }

            if (view != null)
            {
                if ((value = p_request.getParameter("sortComments")) != null)
                {
                    view.sort(value);
                }
            }
        }

        // Cache the segmentView data for the first 3 segments on current page.
        String needCacheSegmentData = (String) sessionMgr
                .getAttribute(WebAppConstants.NEED_CACHE_SEGMENT_DATA);
        String targetPageHtml = p_state.getTargetPageHtml();
        // When open pop-up editor,only cache data once to avoid performance
        // issue.
        if ("yes".equalsIgnoreCase(needCacheSegmentData)
                && targetPageHtml != null)
        {
            // Remove this attribute to avoid re-cache the same data.
            sessionMgr.removeElement(WebAppConstants.NEED_CACHE_SEGMENT_DATA);

            EditorState clonedState = EditorState.cloneState(p_state);
            // Find the second segment to put into p_state
            findSecondEditableSegment(clonedState, targetPageHtml);

            CacheSegmentViewDataThread t = new CacheSegmentViewDataThread(
                    sessionMgr, clonedState, clonedState.getTargetPageId(),
                    clonedState.getSourceLocale().getId(), clonedState
                            .getTargetLocale().getId(), true);
            t.start();
        }

    }

    /**
     * Find the second editable segment key(tuId_tuvId_subId) to put into
     * EditorState. When pup-up editor is opened,user commonly click the first
     * editable segment to translate, so cache the first three segment data for
     * performance enhancement.
     * 
     * @param p_clonedState
     * @param p_targetPageHtml
     */
    private void findSecondEditableSegment(EditorState p_clonedState,
            String p_targetPageHtml)
    {
        if (p_clonedState == null || p_targetPageHtml == null)
        {
            return;
        }
        int index = p_targetPageHtml.indexOf("javascript:SE(");
        if (index > -1)
        {
            p_targetPageHtml = p_targetPageHtml.substring(index + 14);
            index = p_targetPageHtml.indexOf("javascript:SE(");
            if (index > -1)
            {
                p_targetPageHtml = p_targetPageHtml.substring(index + 14);
            }
            String key = p_targetPageHtml.substring(0,
                    p_targetPageHtml.indexOf(")"));
            String[] keys = key.split(",");
            if (keys.length == 3)
            {
                p_clonedState.setTuId(Long.parseLong(keys[0]));
                p_clonedState.setTuvId(Long.parseLong(keys[1]));
                p_clonedState.setSubId(Long.parseLong(keys[2]));
            }
        }
    }

    private void previousPage(EditorState p_state, HttpSession p_session,
            boolean p_fromActivity) throws EnvoyServletException
    {
        ArrayList pages = p_state.getPages();
        int i_index = pages.indexOf(p_state.getCurrentPage());

        if (p_fromActivity)
        {
            boolean foundNonempty = false;
            boolean allEmptyBefore = true;
            while (i_index > 0)
            {
                --i_index;
                EditorState.PagePair pp = (EditorState.PagePair) pages
                        .get(i_index);

                if (!foundNonempty)
                {
                    p_state.setCurrentPage(pp);
                    p_state.setIsFirstPage(i_index == 0);
                    p_state.setIsLastPage(false);

                    initState(p_state, p_session);

                    if (p_state.getUserIsPm() && s_pmCanEditTargetPages)
                    {
                        if (EditorHelper.pmCanEditCurrentPage(p_state))
                        {
                            p_state.setReadOnly(false);
                            p_state.setAllowEditAll(true);
                            p_state.setEditAllState(EDIT_ALL);
                        }
                        else
                        {
                            p_state.setReadOnly(true);
                        }
                    }
                    foundNonempty = true;
                    continue;
                }

                if (foundNonempty && allEmptyBefore)
                {
                    allEmptyBefore = false;
                    break;
                }

            }
            if (foundNonempty && allEmptyBefore)
            {
                p_state.setIsFirstPage(true);
            }
        }
        else
        {
            if (i_index > 0)
            {
                --i_index;

                p_state.setCurrentPage((EditorState.PagePair) pages
                        .get(i_index));

                p_state.setIsFirstPage(i_index == 0);
                p_state.setIsLastPage(false);

                initState(p_state, p_session);

                if (p_state.getUserIsPm() && s_pmCanEditTargetPages)
                {
                    if (EditorHelper.pmCanEditCurrentPage(p_state))
                    {
                        p_state.setReadOnly(false);
                        p_state.setAllowEditAll(true);
                        p_state.setEditAllState(EDIT_ALL);
                    }
                    else
                    {
                        p_state.setReadOnly(true);
                    }
                }
            }
        }

    }

    private void nextPage(EditorState p_state, HttpSession p_session,
            boolean p_fromActivity) throws EnvoyServletException
    {
        ArrayList pages = p_state.getPages();
        int i_index = pages.indexOf(p_state.getCurrentPage());

        if (p_fromActivity)
        {
            boolean foundNonempty = false;
            boolean allEmptyAfter = true;

            while (i_index >= 0 && i_index < (pages.size() - 1))
            {
                ++i_index;

                EditorState.PagePair pp = (EditorState.PagePair) pages
                        .get(i_index);

                if (!foundNonempty)
                {
                    p_state.setCurrentPage(pp);
                    p_state.setIsFirstPage(false);
                    p_state.setIsLastPage(i_index == (pages.size() - 1));

                    initState(p_state, p_session);

                    if (p_state.getUserIsPm() && s_pmCanEditTargetPages)
                    {
                        if (EditorHelper.pmCanEditCurrentPage(p_state))
                        {
                            p_state.setReadOnly(false);
                            p_state.setAllowEditAll(true);
                            p_state.setEditAllState(EDIT_ALL);
                        }
                        else
                        {
                            p_state.setReadOnly(true);
                        }
                    }
                    foundNonempty = true;
                    continue;
                }

                if (foundNonempty && allEmptyAfter)
                {
                    allEmptyAfter = false;
                    break;
                }

            }
            if (foundNonempty && allEmptyAfter)
            {
                p_state.setIsLastPage(true);
            }
        }

        else
        {
            if (i_index >= 0 && i_index < (pages.size() - 1))
            {
                ++i_index;

                p_state.setCurrentPage((EditorState.PagePair) pages
                        .get(i_index));

                p_state.setIsFirstPage(false);
                p_state.setIsLastPage(i_index == (pages.size() - 1));

                initState(p_state, p_session);

                if (p_state.getUserIsPm() && s_pmCanEditTargetPages)
                {
                    if (EditorHelper.pmCanEditCurrentPage(p_state))
                    {
                        p_state.setReadOnly(false);
                        p_state.setAllowEditAll(true);
                        p_state.setEditAllState(EDIT_ALL);
                    }
                    else
                    {
                        p_state.setReadOnly(true);
                    }
                }
            }
        }

    }

    private void updateSourcePageView(EditorState p_state,
            HttpServletRequest p_request, boolean p_isTaskAssignee,
            boolean p_isIE, HashMap p_searchMap) throws EnvoyServletException
    {
        int viewMode = p_state.getLayout().getSourceViewMode();
        int editorMode = 0;
        if (p_state.isReviewMode())
        {
            editorMode = (p_state.isReadOnly() && p_isTaskAssignee) ? UIConstants.UIMODE_REVIEW_READ_ONLY
                    : UIConstants.UIMODE_REVIEW;
        }
        else
        {
            editorMode = UIConstants.UIMODE_EDITOR;
        }

        String html;

        // Sat Oct 26 00:11:27 2002 CvdL: I think we need separate
        // rendering options for source & target. The options can be
        // different and cannot be shared. See updateTargetPageView().
        // Also, allocating a new RenderingOptions object is too much.

        // Update sourcePageHtml whatever it is null or not as it maybe is
        // "batch navigation".
        p_state.setRenderingOptions(initRenderingOptions(
                p_request.getSession(), editorMode, viewMode,
                UIConstants.EDITMODE_DEFAULT));

        if (p_searchMap == null)
        {
            html = EditorHelper.getSourcePageView(p_state, false);
        }
        else
        {
            html = EditorHelper.getSourcePageView(p_state, false, p_searchMap);
        }
        html = OfficeContentPostFilterHelper.fixHtmlForSkeleton(html);
        html = replaceImgForFirefox(html, p_isIE);
        p_state.setSourcePageHtml(viewMode, html);

    }

    private void updateTargetPageView(EditorState p_state,
            HttpSession p_session, boolean p_isTaskAssignee, boolean p_isIE,
            HashMap p_searchMap) throws EnvoyServletException
    {
        int viewMode = p_state.getLayout().getTargetViewMode();
        int editorMode = 0;
        if (p_state.isReviewMode())
        {
            editorMode = (p_state.isReadOnly() && p_isTaskAssignee) ? UIConstants.UIMODE_REVIEW_READ_ONLY
                    : UIConstants.UIMODE_REVIEW;
        }
        else
        {
            editorMode = UIConstants.UIMODE_EDITOR;
        }
        String html;

        // Sat Oct 26 00:11:27 2002 CvdL: I think we need separate
        // rendering options for source & target. The options can be
        // different and cannot be shared. See updateSourcePageView().
        // Also, allocating a new RenderingOptions object is too much.
        p_state.setRenderingOptions(initRenderingOptions(p_session, editorMode,
                viewMode, UIConstants.EDITMODE_DEFAULT));

        if (p_searchMap != null)
        {
            html = EditorHelper.getTargetPageView(p_state, false, p_searchMap);
        }
        else
        {
            html = EditorHelper.getTargetPageView(p_state, false);
        }
        html = OfficeContentPostFilterHelper.fixHtmlForSkeleton(html);
        html = replaceImgForFirefox(html, p_isIE);
        p_state.setTargetPageHtml(html);
    }

    /**
     * VML only works in IE, so replace image tag
     * 
     * @param p_html
     *            original html string
     * @param p_isIE
     *            is IE
     * @return For example: Original html: <v:shape alt="original.aspx"
     *         style='width:487.5pt;'><v:imagedata src="image001.jpg"
     *         o:title="original"/></v:shape> Return html: <img
     *         alt="original.aspx" style='width:487.5pt;' src="image001.jpg"
     *         o:title="original"/>
     */
    public String replaceImgForFirefox(String p_html, boolean p_isIE)
    {
        if (p_isIE)
        {
            return p_html;
        }
        else
        {
            String result = p_html;
            String shapeTag1 = "<v:shape ";
            String shapeTag2 = "</v:shape>";
            String imgTag = "<v:imagedata ";

            if (p_html.contains(shapeTag1) && p_html.contains(shapeTag2)
                    && p_html.contains(imgTag))
            {
                int shapePos1 = p_html.indexOf(shapeTag1);
                int shapePos2 = p_html.indexOf(shapeTag2) + shapeTag2.length();
                int imgPos = p_html.indexOf(imgTag, shapePos1);
                if ((shapePos1 < imgPos) && (imgPos < shapePos2))
                {
                    String str = p_html.substring(shapePos1, shapePos2);
                    int imgPos2 = str.indexOf(imgTag);
                    if (imgPos2 == str.lastIndexOf(imgTag))
                    {
                        String attr1 = str.substring(shapeTag1.length(),
                                str.indexOf(">"));
                        String attr2 = str.substring(imgPos2 + imgTag.length(),
                                str.indexOf("/>", imgPos2));
                        attr1 = attr1.replace("style='width:487.5pt;",
                                "style='width:375pt;");//
                        String repStr = "<img " + attr1 + " " + attr2 + " />";
                        result = p_html.replace(str, repStr);
                    }
                }
            }
            return result;
        }
    }

    /**
     * Creates a new RenderingOptions object based on the UI and view modes and
     * the current session.
     * 
     * Note: there should be two RenderingObjects kept in EditorState to keep
     * track of the different modes the editor frames can be in.
     */
    private RenderingOptions initRenderingOptions(HttpSession p_session,
            int p_uiMode, int p_viewMode, int p_editMode)
    {
        SessionManager mgr = (SessionManager) p_session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        PermissionSet permSet = (PermissionSet) p_session
                .getAttribute(WebAppConstants.PERMISSIONS);
        return new RenderingOptions(p_uiMode, p_viewMode, p_editMode, permSet);
    }

    //
    // Initialization logic for jobs and activities
    //

    /**
     * Initializes editor state from an activity, i.e. when the editor is opened
     * by a localization participant.
     */
    private void initializeFromActivity(EditorState p_state,
            HttpSession p_session, String p_userId, String p_taskId,
            String p_srcPageId, String p_trgPageId, boolean p_isAssignee,
            HttpServletRequest p_request, Locale p_uiLocale)
            throws EnvoyServletException
    {
        PermissionSet perms = (PermissionSet) p_session
                .getAttribute(WebAppConstants.PERMISSIONS);
        p_state.setUserIsPm(false);

        // Reset all options because the state may be inherited from a
        // previous page.
        EditorHelper.initEditorOptions(p_state, p_session);

        // Initializes pages, target locales, excluded items,
        // termbases and editAll state.
        EditorHelper.initializeFromActivity(p_state, p_session, p_userId,
                p_taskId, p_request, p_uiLocale);

        setCurrentPageFromActivity(p_state, p_srcPageId);
        EditorState.PagePair currentPage = p_state.getCurrentPage();

        p_state.setTargetViewLocale(currentPage.getTargetPageLocale(new Long(
                p_trgPageId)));

        // Target page is viewed read-only when the activity/task has
        // not been accepted yet.
        String stateAsString = (String) TaskHelper.retrieveObject(p_session,
                TASK_STATE);

        int taskState = stateAsString == null ? WorkflowConstants.TASK_ALL_STATES
                : Integer.parseInt(stateAsString);

        // We need to set the state to read-only if the user is not
        // the assignee. This happens when a PM is viewing an
        // activity that belong's to his/her job.

        // In other words, if this task is assigned to the user but he
        // hasn't accepted it, it is read-only.
        boolean b_readOnly = true;
        if (p_isAssignee)
        {
            b_readOnly = EditorHelper.getTaskIsReadOnly(p_userId, p_taskId,
                    taskState);
        }

        p_state.setReadOnly(b_readOnly);

        // If the page is read-only, don't allow to unlock segments.
        if (b_readOnly)
        {
            p_state.setAllowEditAll(false);
        }

        // Set editAll state based on whether we can edit all or not.
        if (p_state.canEditAll())
        {
            p_state.setEditAllState(p_state.getOptions().getAutoUnlock() == true ? EDIT_ALL
                    : EDIT_DEFAULT);
        }
        else
        {
            p_state.setEditAllState(EDIT_DEFAULT);
        }

        boolean b_canEditSnippets = false;
        if (!b_readOnly)
        {
            // snippet editor permission check: everybody but
            if (p_state.getCurrentPage().hasGsaTags()
                    && perms.getPermissionFor(Permission.SNIPPET_EDIT))
                b_canEditSnippets = true;
        }

        p_state.setAllowEditSnippets(b_canEditSnippets);

        // Indicate that main editor is in 'editor' mode -- see
        // dispatchJsp for switching to review mode.
        // Comments are turned ON by default in popup editor for a review
        // activity
        if (p_state.getIsReviewActivity())
        {
            p_state.setReviewMode();
        }
        else
        {
            p_state.setEditorMode();
        }
    }

    /**
     * Initializes editor state from a job, i.e. when the editor is opened by an
     * Admin or PM.
     */
    private void initializeFromJob(EditorState p_state,
            HttpServletRequest p_request, String p_jobId, String p_srcPageId,
            String p_trgPageId, Locale p_uiLocale, User p_user)
            throws EnvoyServletException
    {
        p_state.setUserIsPm(true);

        PermissionSet perms = (PermissionSet) p_request.getSession()
                .getAttribute(WebAppConstants.PERMISSIONS);
        // Reset all options because the state may be inherited from a
        // previous page.
        EditorHelper.initEditorOptions(p_state, p_request.getSession());

        // Initializes pages, target locales, excluded items, and termbases
        EditorHelper.initializeFromJob(p_state, p_jobId, p_srcPageId,
                p_uiLocale, p_user.getUserId(), perms);

        if (p_trgPageId != null && p_trgPageId.length() > 0)
        {
            // If the PM requests a specific target page...

            setCurrentPage(p_state, p_srcPageId, p_trgPageId);

            EditorState.PagePair currentPage = p_state.getCurrentPage();

            p_state.setTargetViewLocale(currentPage
                    .getTargetPageLocale(new Long(p_trgPageId)));
        }
        else
        {
            // No target page/locale requested, find a suitable one.

            setCurrentPage(p_state, p_srcPageId);

            // If no locale is set or the set locale doesn't exist in the
            // list of target locales in the job (fix for def_5545),
            // determine the default locale to display in target window.
            GlobalSightLocale viewLocale = p_state.getTargetViewLocale();
            Vector trgLocales = p_state.getJobTargetLocales();
            if (viewLocale == null || !trgLocales.contains(viewLocale))
            {
                p_state.setTargetViewLocale((GlobalSightLocale) trgLocales
                        .elementAt(0));
            }
        }

        // When coming from job page, target page is read only.
        // Fri Feb 20 20:18:44 2004 CvdL: Patch for HP: PMs can edit
        // all target pages any time at their own risk.
        if (s_pmCanEditTargetPages
                && EditorHelper.pmCanEditCurrentPage(p_state))
        {
            p_state.setReadOnly(false);
            p_state.setAllowEditAll(true);
            p_state.setEditAllState(EDIT_ALL);
        }
        else
        {
            p_state.setReadOnly(true);
        }

        // Mon Jan 31 18:56:04 2005 CvdL: PM can edit snippets too (12665)
        p_state.setAllowEditSnippets(s_pmCanEditSnippets);

        // Indicate that main editor is in 'viewer' mode -- see
        // dispatchJsp for switching to review mode.
        // Comments are turned ON by default in popup editor from Job detail
        // page
        p_state.setReviewMode();
    }

    private void initState(EditorState p_state, HttpSession p_session)
            throws EnvoyServletException
    {
        p_state.setSourceLocale(p_state.getSourceLocale());

        ArrayList tuIds = EditorHelper.getTuIdsInPage(p_state,
                p_state.getSourcePageId());
        p_state.setTuIds(tuIds);

        PageInfo info = EditorHelper.getPageInfo(p_state,
                p_state.getSourcePageId());

        // Wed Mar 05 20:18:29 2003 CvdL: use pageinfo record in
        // me_(target|source)Menu to determine when preview mode is
        // available.
        p_state.setPageInfo(info);

        p_state.setPageFormat(info.getPageFormat());
        // discard PageInfo object for now - could add fields to this?

        // If we were in preview mode and the current page doesn't
        // have a preview mode, change the mode to TEXT.
        SessionManager sessionMgr = (SessionManager) p_session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        sessionMgr.removeElement("src_view_pdf");
        sessionMgr.removeElement("tgt_view_pdf");
        EditorState.Layout layout = p_state.getLayout();
        if (EditUtil.hasPDFPreviewMode(p_state))
        {
            if (layout.getSourceViewMode() == VIEWMODE_PREVIEW)
            {
                sessionMgr.setAttribute("src_view_pdf", Boolean.TRUE);
            }

            if (layout.getTargetViewMode() == VIEWMODE_PREVIEW)
            {
                sessionMgr.setAttribute("tgt_view_pdf", Boolean.FALSE);
            }
        }
        if (!EditUtil.hasPreviewMode(p_state.getPageFormat()))
        {
            if (layout.getSourceViewMode() == VIEWMODE_PREVIEW)
            {
                layout.setSourceViewMode(DEFAULT_VIEWMODE_IF_NO_PREVIEW);
            }

            if (layout.getTargetViewMode() == VIEWMODE_PREVIEW)
            {
                layout.setTargetViewMode(DEFAULT_VIEWMODE_IF_NO_PREVIEW);
            }
        }

        // Sat Feb 19 02:06:30 2005 CvdL Loading a new page, clear
        // offline upload status. This also clears the OEML's chached
        // target tuvs, which is ok when loading a new (or the
        // next/previous) page.
        EditorHelper.clearSynchronizationStatus(p_state);

        // Clear comments from previous pages.
        p_state.setCommentThreads(null);

        // Fri Jan 10 00:30:46 2003 CvdL: this used to precompute all
        // views. We don't do this anymore to ensure better
        // parallelism and on-demand computation (i.e. if user looks
        // only at target view we don't need source view).

        p_state.clearSourcePageHtml();
        p_state.setTargetPageHtml(null);

        String str_segmentNumPerPage = PageHandler.getUserParameter(p_session,
                UserParamNames.EDITOR_SEGMENTS_MAX_NUM).getValue();
        int int_segmentNumPerPage = Integer.parseInt(str_segmentNumPerPage);
        PaginateInfo pi = new PaginateInfo(tuIds.size(), int_segmentNumPerPage,
                1);
        p_state.setPaginateInfo(pi);
    }

    /**
     * Scans the pagelist for the first pair having the right source page id and
     * set the pair to be shown first.
     */
    private void setCurrentPage(EditorState p_state, String p_srcPageId)
    {
        ArrayList pages = p_state.getPages();
        Long srcPageId = new Long(p_srcPageId);
        int i_offset = 0;

        for (int i = 0, max = pages.size(); i < max; i++)
        {
            EditorState.PagePair pair = (EditorState.PagePair) pages.get(i);
            ++i_offset;

            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("Pagepair= " + pair.toString() + " p_srcPageId="
                        + p_srcPageId);
            }

            if (pair.getSourcePageId().equals(srcPageId))
            {
                p_state.setCurrentPage(pair);
                break;
            }
        }

        p_state.setIsFirstPage(i_offset == 1);
        p_state.setIsLastPage(pages.size() == i_offset);
    }

    /**
     * Scans the pagelist for the first pair having the right source page id and
     * set the pair to be shown first.
     */
    private void setCurrentPageFromActivity(EditorState p_state,
            String p_srcPageId)
    {
        ArrayList pages = p_state.getPages();
        Long srcPageId = new Long(p_srcPageId);
        int i_offset = 0;
        int offset = 0;
        boolean foundPage = false;
        boolean allEmptyBefore = true;
        boolean allEmptyAfter = true;

        for (int i = 0, max = pages.size(); i < max; i++)
        {
            EditorState.PagePair pair = (EditorState.PagePair) pages.get(i);
            ++i_offset;

            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("Pagepair= " + pair.toString() + " p_srcPageId="
                        + p_srcPageId);
            }

            if (!foundPage && pair.getSourcePageId().equals(srcPageId))
            {
                p_state.setCurrentPage(pair);
                foundPage = true;
                offset = i_offset;
                continue;
            }

            if (foundPage && allEmptyAfter)
            {
                allEmptyAfter = false;
                break;
            }
            else if (!foundPage && allEmptyBefore)
            {
                allEmptyBefore = false;
            }
        }

        p_state.setIsFirstPage(offset == 1);
        p_state.setIsLastPage(pages.size() == offset);

        if (allEmptyBefore)
        {
            p_state.setIsFirstPage(true);
        }

        if (allEmptyAfter)
        {
            p_state.setIsLastPage(true);
        }

    }

    /**
     * Scans the pagelist for the pair having the right source and target page
     * id and set the pair to be shown first.
     */
    private void setCurrentPage(EditorState p_state, String p_srcPageId,
            String p_trgPageId)
    {
        ArrayList pages = p_state.getPages();
        Long srcPageId = new Long(p_srcPageId);
        Long trgPageId = new Long(p_trgPageId);
        int i_index = 0;

        for (int i = 0, max = pages.size(); i < max; i++)
        {
            EditorState.PagePair pair = (EditorState.PagePair) pages.get(i);
            ++i_index;

            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("Pagepair= " + pair.toString() + " p_srcPageId="
                        + p_srcPageId + " p_trgPageId=" + p_trgPageId);
            }

            // See if this page pair object is for this source page.
            // If so, see if this page pair object contains the
            // specified target page id.

            if (pair.getSourcePageId().equals(srcPageId)
                    && pair.getTargetPageLocale(trgPageId) != null)
            {
                p_state.setCurrentPage(pair);
                break;
            }
        }

        p_state.setIsFirstPage(i_index == 1);
        p_state.setIsLastPage(pages.size() == i_index);
    }

    /**
     * For better parallelism during frame loading: compute the source page view
     * only when the me_source page (ED5) is actually shown.
     */
    private boolean needSourcePageView(WebPageDescriptor p_pageDescriptor,
            EditorState p_state)
    {
        String pageName = p_pageDescriptor.getPageName();

        if (pageName.equals("ED5"))
        {
            // return true despite of "SourcePageHtml" is null or not.
            return true;
        }

        return false;
    }

    /**
     * For better parallelism during frame loading: compute the target page view
     * only when the me_target page (ED8) is actually shown.
     */
    private boolean needTargetPageView(WebPageDescriptor p_pageDescriptor,
            EditorState p_state)
    {
        String pageName = p_pageDescriptor.getPageName();

        if (pageName.equals("ED8"))
        {
            // return true despite of "TargetPageHtml" is null or not.
            return true;
        }

        return false;
    }

    private boolean needComments(WebPageDescriptor p_pageDescriptor)
    {
        return p_pageDescriptor.getPageName().equals("ED15");
    }

    /**
     * Remembers the last segment being edited when the editor is closed so the
     * target page view can show it when it's getting reloaded.
     */
    private void setCurrentEditorSegment(EditorState p_state,
            HttpServletRequest p_request)
    {
        String tuId = p_request.getParameter("curTuId");
        String tuvId = p_request.getParameter("curTuvId");
        String subId = p_request.getParameter("curSubId");

        if (tuId != null)
        {
            p_state.setTuId(Long.parseLong(tuId));
            p_state.setTuvId(Long.parseLong(tuvId));
            p_state.setSubId(Long.parseLong(subId));
        }
    }

    public static File getTuvCommentImg(long tuvId)
    {
        String termImgPath = FileUploadHelper.DOCROOT + "terminologyImg";
        File parentFilePath = new File(termImgPath.toString());
        File[] files = parentFilePath.listFiles();
        File img = null;

        if (files != null && files.length > 0)
        {
            for (int j = 0; j < files.length; j++)
            {
                File file = files[j];
                String fileName = file.getName();

                if (fileName.lastIndexOf(".") > 0)
                {
                    String tempName = fileName.substring(0,
                            fileName.lastIndexOf("."));
                    String nowImgName = "tuv_" + Long.toString(tuvId);

                    if (tempName.equals(nowImgName))
                    {
                        return file;
                    }
                }
            }
        }

        return null;
    }

    private void shareImg(long tuId, long tuvId, boolean overwrite)
    {
        File img = getTuvCommentImg(tuvId);

        if (img != null)
        {
            String termImgPath = FileUploadHelper.DOCROOT + "terminologyImg/";
            String name = img.getName();
            String type = name.substring(name.indexOf("."));

            TuImpl tu = HibernateUtil.get(TuImpl.class, tuId);
            for (Object obj : tu.getTuvs())
            {
                TuvImpl tuv = (TuvImpl) obj;
                if (tuvId != tuv.getId())
                {
                    if (!overwrite)
                    {
                        File img2 = getTuvCommentImg(tuv.getId());
                        if (img2 != null)
                        {
                            continue;
                        }
                    }

                    File trg = new File(termImgPath + "tuv_"
                            + Long.toString(tuv.getId()) + type);
                    try
                    {
                        FileUtil.copyFile(img, trg);
                    }
                    catch (IOException e)
                    {
                        CATEGORY.error(e);
                    }
                }
            }
        }
    }

    private void executeCommentCommand(EditorState p_state, CommentView p_view,
            HttpServletRequest p_request, User p_user)
            throws EnvoyServletException
    {
        String action = p_request.getParameter("cmtAction");
        String title = p_request.getParameter("cmtTitle");
        String comment = p_request.getParameter("cmtComment");
        String priority = p_request.getParameter("cmtPriority");
        String status = p_request.getParameter("cmtStatus");
        String category = p_request.getParameter("cmtCategory");

        title = EditUtil.utf8ToUnicode(title);
        comment = EditUtil.utf8ToUnicode(comment);

        String tuId = p_request.getParameter("tuId");
        String tuvId = p_request.getParameter("tuvId");
        String subId = p_request.getParameter("subId");
        String shareComment = p_request.getParameter("cmtShare");
        String overwriteComment = p_request.getParameter("cmtOverwrite");

        boolean update = false;
        boolean share = "true".equalsIgnoreCase(shareComment);
        boolean overwrite = "true".equalsIgnoreCase(overwriteComment);

        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("Comment " + action + " id " + tuId + "_" + tuvId
                    + "_" + subId + " status=" + status);
        }

        if (action.equals("create"))
        {
            EditorHelper.createComment(p_state, p_view, title, comment,
                    priority, status, category, p_user.getUserId(), share,
                    overwrite);

            // Recompute target page view (new icon)
            p_state.setTargetPageHtml(null);
            update = true;
        }
        else if (action.equals("edit"))
        {
            EditorHelper.editComment(p_state, p_view, title, comment, priority,
                    status, category, p_user.getUserId(), share, overwrite);
            update = true;
        }
        else if (action.equals("add"))
        {
            EditorHelper.addComment(p_state, p_view, title, comment, priority,
                    status, category, p_user.getUserId(), share, overwrite);
            update = true;
        }
        else if (action.equals("closeAllComments"))
        {
            ArrayList currentIssues = (ArrayList) (p_request.getSession()
                    .getAttribute("currentIssues"));
            EditorHelper.closeAllComment(p_state, currentIssues,
                    p_user.getUserId());
        }

        if (share && update)
        {
            shareImg(Long.parseLong(tuId), Long.parseLong(tuvId), overwrite);

            TuImpl tu = HibernateUtil.get(TuImpl.class, Long.parseLong(tuId));
            if (tu == null)
            {
                CATEGORY.error("Can not find tu with id: " + tuId);
            }
            else
            {
                String sql = " select tp.* from TRANSLATION_UNIT_VARIANT tuv, "
                        + "TRANSLATION_UNIT tu,TARGET_PAGE_LEVERAGE_GROUP tplg, "
                        + "WORKFLOW w, TARGET_PAGE tp where tuv.id = ? "
                        + "and tuv.TU_ID = tu.id "
                        + "and tu.LEVERAGE_GROUP_ID = tplg.LG_ID "
                        + "and tplg.TP_ID = tp.id "
                        + "and tp.WORKFLOW_IFLOW_INSTANCE_ID = w.IFLOW_INSTANCE_ID "
                        + "and tuv.STATE != 'OUT_OF_DATE' "
                        + "and w.TARGET_LOCALE_ID = tuv.LOCALE_ID";

                Map<Long, TuvImpl> tuvs = tu.getTuvAsSet();
                List<Long> localeIds = new ArrayList<Long>();
                localeIds.addAll(tuvs.keySet());

                for (Long id : localeIds)
                {
                    TuvImpl tuv = tuvs.get(id);

                    if (tuv.getId() == Long.parseLong(tuvId))
                    {
                        continue;
                    }

                    List<TargetPage> tPages = HibernateUtil.searchWithSql(
                            TargetPage.class, sql, tuv.getId());
                    if (tPages.size() == 0)
                    {
                        continue;
                    }

                    String hql = "from IssueImpl i where "
                            + "i.levelObjectTypeAsString = :type "
                            + "&& i.levelObjectId = :oId";
                    Map map = new HashMap();
                    map.put("type", "S");
                    map.put("oId", tuv.getId());

                    IssueImpl issue = (IssueImpl) HibernateUtil.getFirst(hql,
                            map);
                    if (issue == null)
                    {
                        String key = CommentHelper.makeLogicalKey(tPages.get(0)
                                .getId(), tu.getId(), tuv.getId(), 0);
                        issue = new IssueImpl(Issue.TYPE_SEGMENT, tuv.getId(),
                                title, priority, status, category,
                                p_user.getUserId(), comment, key);
                        issue.setShare(share);
                        issue.setOverwrite(overwrite);

                        HibernateUtil.saveOrUpdate(issue);
                    }
                    else if (overwrite)
                    {
                        issue.setTitle(title);
                        issue.setPriority(priority);
                        issue.setStatus(status);
                        issue.setCategory(category);
                        issue.addHistory(p_user.getUserId(), comment);
                        issue.setShare(share);
                        issue.setOverwrite(overwrite);

                        HibernateUtil.saveOrUpdate(issue);
                    }
                }
            }
        }

        // Refresh and re-sort comments - if they were shown.
        CommentThreadView view = p_state.getCommentThreads();
        if (view != null)
        {
            String sortedBy = view.getSortedBy();
            view = EditorHelper.getCommentThreads(p_state);
            view.sort(sortedBy);
            p_state.setCommentThreads(view);
        }

        p_request.setAttribute("cmtRefreshOtherPane", Boolean.TRUE);
    }
}
