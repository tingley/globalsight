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

package com.globalsight.everest.webapp.pagehandler.edit.online2;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

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
import com.globalsight.everest.edit.online.UIConstants;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.persistence.tuv.BigTableUtil;
import com.globalsight.everest.persistence.tuv.SegmentTuUtil;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.tuv.TuImpl;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.edit.online.EditorConstants;
import com.globalsight.everest.webapp.pagehandler.edit.online.EditorHelper;
import com.globalsight.everest.webapp.pagehandler.edit.online.EditorState;
import com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper;
import com.globalsight.everest.webapp.pagehandler.terminology.management.FileUploadHelper;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.workflow.WorkflowConstants;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.FileUtil;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.edit.EditUtil;

/**
 * <p>
 * online2.EditorPageHandler shows the new preview-mode editor.
 * </p>
 */
public class EditorPageHandler extends PageHandler implements EditorConstants
{
    private static final Logger CATEGORY = Logger
            .getLogger(EditorPageHandler.class);

    private static int DEFAULT_VIEWMODE_IF_NO_PREVIEW = VIEWMODE_TEXT;

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

        String srcPageId = (String) p_request
                .getParameter(WebAppConstants.SOURCE_PAGE_ID);
        String trgPageId = (String) p_request
                .getParameter(WebAppConstants.TARGET_PAGE_ID);
        String jobId = (String) p_request.getParameter(WebAppConstants.JOB_ID);
        String taskId = (String) p_request
                .getParameter(WebAppConstants.TASK_ID);

        // Get user object for the person who has logged in.
        User user = TaskHelper.getUser(session);

        TranslationMemoryProfile tmProfile = null;

        // Decide from which screen we've been called.

        // From Activity Details (Translator opening pages rw or ro)
        if (taskId != null && srcPageId != null && trgPageId != null)
        {
            sessionMgr.setAttribute(WebAppConstants.IS_FROM_ACTIVITY, "yes");
            state = new EditorState();

            tmProfile = TaskHelper.getTask(Integer.parseInt(taskId))
                    .getWorkflow().getJob().getL10nProfile()
                    .getTranslationMemoryProfile();

            EditorHelper.initEditorManager(state);
            EditorHelper.initEditorOptions(state, session);

            sessionMgr.setAttribute(WebAppConstants.EDITORSTATE, state);

            Boolean assigneeValue = (Boolean) TaskHelper.retrieveObject(
                    session, WebAppConstants.IS_ASSIGNEE);
            boolean isAssignee = assigneeValue == null ? true : assigneeValue
                    .booleanValue();

            initializeFromActivity(state, session, user.getUserId(), taskId,
                    srcPageId, trgPageId, isAssignee, p_request, uiLocale);

            initState(state, session);
        }
        // From Job Details (Admin or PM opening pages read-only)
        else if (jobId != null && srcPageId != null)
        {
            state = new EditorState();

            EditorHelper.initEditorManager(state);
            EditorHelper.initEditorOptions(state, session);

            sessionMgr.setAttribute(WebAppConstants.EDITORSTATE, state);
            initializeFromJob(state, p_request, jobId, srcPageId, uiLocale,
                    user);

            initState(state, session);
        }

        state.setTmProfile(tmProfile);

        if ((p_request.getParameter(WebAppConstants.REVIEW_MODE)) != null)
        {
            state.setTargetPageHtml(null);

            if ("true".equals(p_request.getParameter(WebAppConstants.REVIEW_MODE)))
            {
                state.setReviewMode();
            }
            else if (state.getUserIsPm())
            {
                state.setViewerMode();
            }
            else
            {
                state.setEditorMode();
            }
        }
        
        String trgViewLocale = (String) p_request
                .getParameter(WebAppConstants.TARGETVIEW_LOCALE);

        if (trgViewLocale != null)
        {
            state.setTargetViewLocale(EditorHelper.getLocale(trgViewLocale));
        }
        
        CommentView commentView = (CommentView) sessionMgr
                .getAttribute(WebAppConstants.COMMENTVIEW);
        if ((p_request.getParameter("cmtAction")) != null)
        {
            executeCommentCommand(state, commentView, p_request, user);
        }

        dispatchJSP(p_pageDescriptor, p_request, p_response, p_context, state,
                sessionMgr);

        super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                p_context);
    }

    //
    // Private Methods
    //

    /**
     * Executes all actions sent in from the UI and updates the EditorState to
     * have correct data for the JSPs.
     */
    private void dispatchJSP(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context, EditorState p_state,
            SessionManager p_sessionMgr) throws ServletException, IOException,
            EnvoyServletException
    {
        if (p_request.getParameter("segmentFilter") != null)
        {
            p_state.setSegmentFilter(p_request.getParameter("segmentFilter"));
        }
        p_request.setAttribute("segmentFilter", p_state.getSegmentFilter());
        
        if ((p_request.getParameter("search")) != null)
        {
            p_sessionMgr.setAttribute("userNameList", p_state
                    .getEditorManager().getPageLastModifyUserList(p_state));

            p_sessionMgr.setAttribute("sidList", p_state.getEditorManager()
                    .getPageSidList(p_state));
            p_sessionMgr.setAttribute("from", "online2");
        }

        String action = (String) p_request.getParameter("action");

        if (action != null)
        {
            String tuv1 = (String) p_request.getParameter("tuv1");
            String tuv2 = (String) p_request.getParameter("tuv2");
            String location = (String) p_request.getParameter("location");
            long spId = p_state.getCurrentPage().getSourcePageId();
            long jobId = BigTableUtil.getJobBySourcePageId(spId).getId();
            if (action.equals("split"))
            {
                splitSegments(p_state, tuv1, tuv2, location, jobId);
            }
            else if (action.equals("merge"))
            {
                mergeSegments(p_state, tuv1, tuv2, jobId);
            }

            p_request.setAttribute("currenttuv", tuv1);
        }

        action = (String) p_request.getParameter("refresh");

        if (action != null)
        {
            int i_direction = 0;
            if (!action.startsWith("0"))
                i_direction = Integer.parseInt(action);

            boolean fromActivity = false;
            String att = (String) p_sessionMgr
                    .getAttribute(WebAppConstants.IS_FROM_ACTIVITY);
            if (att != null && att.equals("yes"))
            {
                fromActivity = true;
            }

            if (i_direction == -1)// previous file
            {
                previousPage(p_state, p_request.getSession(), fromActivity);
                while (!p_state.isFirstPage()
                        && (p_state.getTuIds() == null || p_state.getTuIds()
                                .size() == 0))
                {
                    previousPage(p_state, p_request.getSession(), fromActivity);
                }
            }
            else if (i_direction == 1)// next file
            {
                nextPage(p_state, p_request.getSession(), fromActivity);
                while (!p_state.isLastPage()
                        && (p_state.getTuIds() == null || p_state.getTuIds()
                                .size() == 0))
                {
                    nextPage(p_state, p_request.getSession(), fromActivity);
                }
            }
            else if (i_direction == -11)// previous page
            {
                int oldCurrentPageNum = p_state.getPaginateInfo()
                        .getCurrentPageNum();
                p_state.getPaginateInfo().setCurrentPageNum(
                        oldCurrentPageNum - 1);
            }
            else if (i_direction == 11) // next page
            {
                int oldCurrentPageNum = p_state.getPaginateInfo()
                        .getCurrentPageNum();
                p_state.getPaginateInfo().setCurrentPageNum(
                        oldCurrentPageNum + 1);
            }
            else if (action.startsWith("0")) // goto page
            {
                i_direction = Integer.parseInt(action);
                p_state.getPaginateInfo().setCurrentPageNum(i_direction);
            }
        }
        // Check for any offline uploads that affect this page.
        EditorHelper.checkSynchronizationStatus(p_state);

        if (p_request.getParameter("searchByUser") != null)
        {
            String userId = p_request.getParameter("searchByUser");
            HashMap<String, String> hm = new HashMap<String, String>();
            hm.put("userId", userId);
            updateTargetPageView(p_state, p_request.getSession(), hm);
        }
        else if (p_request.getParameter("searchBySid") != null)
        {
            String sid = p_request.getParameter("searchBySid");
            HashMap<String, String> hm = new HashMap<String, String>();
            hm.put("sid", sid);
            updateTargetPageView(p_state, p_request.getSession(), hm);
        }
        else
        {
            updateTargetPageView(p_state, p_request.getSession(), null);
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
            }
        }

    }

    private void updateTargetPageView(EditorState p_state,
            HttpSession p_session, HashMap p_searchMap)
            throws EnvoyServletException
    {
        String html;

        // Sat Oct 26 00:11:27 2002 CvdL: I think we need separate
        // rendering options for source & target. The options can be
        // different and cannot be shared. See updateSourcePageView().
        // Also, allocating a new RenderingOptions object is too much.
        p_state.setRenderingOptions(initRenderingOptions(p_session,
                UIConstants.UIMODE_PREVIEW_EDITOR,
                UIConstants.VIEWMODE_PREVIEW, UIConstants.EDITMODE_DEFAULT));

        html = EditorHelper.getTargetPageView(p_state, false, p_searchMap);
        p_state.setTargetPageHtml(html);
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
            // snippet editor permission check
            if (p_state.getCurrentPage().hasGsaTags()
                    && perms.getPermissionFor(Permission.SNIPPET_EDIT))
                b_canEditSnippets = true;
        }

        p_state.setAllowEditSnippets(b_canEditSnippets);

        // Indicate that main editor is in 'editor' mode.
        p_state.setEditorMode();
    }

    /**
     * Initializes editor state from a job, i.e. when the editor is opened by an
     * Admin or PM.
     */
    private void initializeFromJob(EditorState p_state,
            HttpServletRequest p_request, String p_jobId, String p_srcPageId,
            Locale p_uiLocale, User p_user) throws EnvoyServletException
    {
        PermissionSet perms = (PermissionSet) p_request.getSession()
                .getAttribute(WebAppConstants.PERMISSIONS);

        p_state.setUserIsPm(true);

        // Reset all options because the state may be inherited from a
        // previous page.
        EditorHelper.initEditorOptions(p_state, p_request.getSession());

        // Initializes pages, target locales, excluded items, and termbases
        EditorHelper.initializeFromJob(p_state, p_jobId, p_srcPageId,
                p_uiLocale, p_user.getUserId(), perms);

        Vector trgLocales = p_state.getJobTargetLocales();

        // If none is set or the set locale doesn't exist in the list
        // of target locales in the job (fix for def_5545), determine
        // the default locale to display in target window.
        GlobalSightLocale viewLocale = p_state.getTargetViewLocale();
        if (viewLocale == null || !trgLocales.contains(viewLocale))
        {
            p_state.setTargetViewLocale((GlobalSightLocale) trgLocales
                    .elementAt(0));
        }

        setCurrentPage(p_state, p_srcPageId);

        // When coming from job page, target page is read only.
        p_state.setReadOnly(true);

        // Snippets can not be edited.
        p_state.setAllowEditSnippets(false);

        // Indicate that main editor is in 'viewer' mode.
        p_state.setViewerMode();
    }

    private void initState(EditorState p_state, HttpSession p_session)
            throws EnvoyServletException
    {
        p_state.setSourceLocale(p_state.getSourceLocale());

        ArrayList<Long> tuIds = EditorHelper.getTuIdsInPage(p_state,
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
        if (!EditUtil.hasPreviewMode(p_state.getPageFormat()))
        {
            EditorState.Layout layout = p_state.getLayout();

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
        EditorState.PagePair pair;

        long l_srcPageId = Long.parseLong(p_srcPageId);
        int i_offset = 0;
        Iterator it = pages.iterator();
        while (it.hasNext())
        {
            pair = (EditorState.PagePair) it.next();
            ++i_offset;

            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("Pagepair= " + pair.toString() + " p_srcPageId="
                        + p_srcPageId);
            }

            if (pair.m_sourcePageId.longValue() == l_srcPageId)
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
        EditorState.PagePair pair;

        Long srcPageId = new Long(p_srcPageId);
        Long trgPageId = new Long(p_trgPageId);
        int i_index = 0;

        Iterator it = pages.iterator();
        while (it.hasNext())
        {
            pair = (EditorState.PagePair) it.next();
            ++i_index;

            // See if this page pair object is for this source page.
            // If so, see if this page pair object contains the
            // specified target page id.

            if (pair.getSourcePageId() == srcPageId
                    && pair.getTargetPageLocale(trgPageId) != null)
            {
                p_state.setCurrentPage(pair);
                break;
            }
        }

        p_state.setIsFirstPage(i_index == 1);
        p_state.setIsLastPage(pages.size() == i_index);
    }

    public void splitSegments(EditorState p_state, String p_tuv1,
            String p_tuv2, String p_location, long p_jobId)
    {
        try
        {
            EditorHelper.splitSegments(p_state, p_tuv1, p_tuv2, p_location,
                    p_jobId);
        }
        catch (Exception ex)
        {
            CATEGORY.error("error when splitting segment " + p_tuv1, ex);

            // Maybe forward exception to editor.
        }
    }

    public void mergeSegments(EditorState p_state, String p_tuv1,
            String p_tuv2, long p_jobId)
    {
        try
        {
            EditorHelper.mergeSegments(p_state, p_tuv1, p_tuv2, p_jobId);
        }
        catch (Exception ex)
        {
            CATEGORY.error("error when merging segments " + p_tuv1 + " + "
                    + p_tuv2, ex);

            // Maybe forward exception to editor.
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
            SourcePage sp = null;
            try
            {
                sp = ServerProxy.getPageManager().getSourcePage(
                        p_state.getSourcePageId());
            }
            catch (Exception e)
            {
                CATEGORY.error("Problem getting source page", e);
                throw new EnvoyServletException(e);
            }
            long jobId = sp.getJobId();
            shareImg(Long.parseLong(tuId), Long.parseLong(tuvId), overwrite,
                    jobId);

            TuImpl tu = null;
            try
            {
                tu = SegmentTuUtil.getTuById(Long.parseLong(tuId), jobId);
            }
            catch (Exception e)
            {
                throw new EnvoyServletException(e);
            }

            if (tu == null)
            {
                CATEGORY.error("Can not find tu with id: " + tuId);
            }
            else
            {
                @SuppressWarnings("unchecked")
                Map<Long, Tuv> tuvs = tu.getTuvAsSet(true, jobId);
                for (Iterator iter = tuvs.entrySet().iterator(); iter.hasNext();)
                {
                    Map.Entry entry = (Map.Entry) iter.next();
                    Long localeId = (Long) entry.getKey();
                    TuvImpl tuv = (TuvImpl) entry.getValue();

                    if (tuv.getId() == Long.parseLong(tuvId))
                    {
                        continue;
                    }
                    TargetPage tp = sp.getTargetPageByLocaleId(localeId);
                    if (tp == null)
                    {
                        continue;
                    }

                    String hql = "from IssueImpl i where "
                            + "i.levelObjectTypeAsString = :type "
                            + "and i.levelObjectId = :oId";
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("type", "S");
                    map.put("oId", tuv.getId());

                    IssueImpl issue = (IssueImpl) HibernateUtil.getFirst(hql,
                            map);
                    if (issue == null)
                    {
                        String key = CommentHelper.makeLogicalKey(tp.getId(),
                                tu.getId(), tuv.getId(), 0);
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
    
    private void shareImg(long tuId, long tuvId, boolean overwrite, long p_jobId)
    {
        File img = getTuvCommentImg(tuvId);

        if (img != null)
        {
            String termImgPath = FileUploadHelper.DOCROOT + "terminologyImg/";
            String name = img.getName();
            String type = name.substring(name.indexOf("."));

            TuImpl tu = null;
            try
            {
                tu = SegmentTuUtil.getTuById(tuId, p_jobId);
            }
            catch (Exception e)
            {
                CATEGORY.error(e.getMessage(), e);
            }
            for (Object obj : tu.getTuvs(true, p_jobId))
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
                        CATEGORY.error(e.getMessage(), e);
                    }
                }
            }
        }
    }
    
    public static File getTuvCommentImg(long tuvId)
    {
        String termImgPath = FileUploadHelper.DOCROOT + "terminologyImg";
        File parentFilePath = new File(termImgPath.toString());
        File[] files = parentFilePath.listFiles();

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
}
