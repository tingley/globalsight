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

package com.globalsight.everest.webapp.pagehandler.edit.online3;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
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
import com.globalsight.everest.edit.online.OnlineEditorConstants;
import com.globalsight.everest.edit.online.PageInfo;
import com.globalsight.everest.edit.online.PaginateInfo;
import com.globalsight.everest.edit.online.RenderingOptions;
import com.globalsight.everest.edit.online.SegmentFilter;
import com.globalsight.everest.edit.online.SegmentView;
import com.globalsight.everest.edit.online.UIConstants;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.PageManager;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.persistence.tuv.SegmentTuTuvAttributeUtil;
import com.globalsight.everest.persistence.tuv.SegmentTuUtil;
import com.globalsight.everest.persistence.tuv.SegmentTuvUtil;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.tuv.TuImpl;
import com.globalsight.everest.tuv.TuTuvAttributeImpl;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.everest.tuv.TuvState;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.ActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.reports.ReportConstants;
import com.globalsight.everest.webapp.pagehandler.edit.online.EditorConstants;
import com.globalsight.everest.webapp.pagehandler.edit.online.EditorHelper;
import com.globalsight.everest.webapp.pagehandler.edit.online.EditorState;
import com.globalsight.everest.webapp.pagehandler.edit.online.PreviewPageHandler;
import com.globalsight.everest.webapp.pagehandler.edit.online.EditorState.PagePair;
import com.globalsight.everest.webapp.pagehandler.edit.online.previewPDF.PreviewPDFHelper;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler;
import com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper;
import com.globalsight.everest.webapp.pagehandler.terminology.management.FileUploadHelper;
import com.globalsight.everest.workflow.WorkflowConstants;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.ling.docproc.extractor.html.OfficeContentPostFilterHelper;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.FileUtil;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.StringUtil;
import com.globalsight.util.edit.EditUtil;

/**
 * <p>
 * online3.EditorPageHandler shows the new editor.
 * </p>
 */
public class EditorPageHandler extends PageActionHandler implements EditorConstants
{
	private static final Logger CATEGORY = Logger
            .getLogger(EditorPageHandler.class);
	private static int DEFAULT_VIEWMODE_IF_NO_PREVIEW = VIEWMODE_TEXT;

	public static boolean s_pmCanEditTargetPages = false;
	public static boolean s_pmCanEditSnippets = false;

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
            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("Error when get 'editalltargetpages.allowed' and 'editallsnippets.allowed' configurations");
            }
            // Do nothing if configuration is not available.
        }
    }

    @ActionHandler(action = "refresh", formClass = "")
    public void refresh(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
		HttpSession session = request.getSession();
        SessionManager sessionMgr = (SessionManager) session
        		.getAttribute(WebAppConstants.SESSION_MANAGER);
        EditorState state = (EditorState) sessionMgr
        		.getAttribute(WebAppConstants.EDITORSTATE);
		String value = request.getParameter("refresh");
        int i_direction = 0;
        if (!value.startsWith("0"))
            i_direction = Integer.parseInt(value);
        boolean fromActivity = false;
        String att = (String) sessionMgr
                .getAttribute(WebAppConstants.IS_FROM_ACTIVITY);
        if (att != null && att.equals("yes"))
        {
            fromActivity = true;
        }
        if (i_direction == -1) // previous file
        {
            previousPage(state, request.getSession(), fromActivity);
            while (!state.isFirstPage()
                    && (state.getTuIds() == null || state.getTuIds()
                            .size() == 0))
            {
                previousPage(state, request.getSession(), fromActivity);
            }
        }
        else if (i_direction == 1) // next file
        {
            nextPage(state, request.getSession(), fromActivity);
            while (!state.isLastPage()
                    && (state.getTuIds() == null || state.getTuIds()
                            .size() == 0))
            {
                nextPage(state, request.getSession(), fromActivity);
            }
        }
        else if (i_direction == -11) // previous page
        {
            int oldCurrentPageNum = state.getPaginateInfo()
                    .getCurrentPageNum();
            int newCurrentPageNum = oldCurrentPageNum - 1;
            if (newCurrentPageNum < 1)
            {
                state.getPaginateInfo().setCurrentPageNum(
                        state.getPaginateInfo().getTotalPageNum());
            }
            else
            {
                state.getPaginateInfo().setCurrentPageNum(
                        newCurrentPageNum);
            }
        }
        else if (i_direction == 11) // next page
        {
            int oldCurrentPageNum = state.getPaginateInfo()
                    .getCurrentPageNum();
            int newCurrentPageNum = oldCurrentPageNum + 1;
            if (newCurrentPageNum > state.getPaginateInfo()
                    .getTotalPageNum())
            {
                state.getPaginateInfo().setCurrentPageNum(1);
            }
            else
            {
                state.getPaginateInfo().setCurrentPageNum(
                        newCurrentPageNum);
            }
        }
        else if (value.startsWith("0")) // goto page
        {
        	i_direction = Integer.parseInt(value);
			if (value.equals("0"))
			{
				int oldCurrentPageNum = state.getPaginateInfo()
						.getCurrentPageNum();
				if (oldCurrentPageNum != i_direction)
				{
					i_direction = oldCurrentPageNum;
				}
			}
            state.getPaginateInfo().setCurrentPageNum(i_direction);
        }

        // When you modify "edit segment" page and check the "close comment"
        // checkbox and save it, in the comments page the stater need to
        // be refreshed. So here refresh the CommentThreadView in
        // the EditorState.
        CommentThreadView view = state.getCommentThreads();
        if (view == null)
        {
            view = EditorHelper.getCommentThreads(state);
            state.setCommentThreads(view);
        }
        if (view != null)
        {
            String sortedBy = view.getSortedBy();
            view = EditorHelper.getCommentThreads(state);
            view.sort(sortedBy);
            state.setCommentThreads(view);
        }

        String currentSrcPageId = state.getCurrentPage()
                .getSourcePageId().toString();
        sessionMgr.setAttribute(WebAppConstants.SOURCE_PAGE_ID,
                currentSrcPageId);
        
        if(SegmentFilter.isFilterSegment(state))
        {
            if (OnlineEditorConstants.SEGMENT_FILTER_ICE.equals(state
                    .getSegmentFilter()))
            {
                request.setAttribute("refreshSource", "true");
            }
        }
    }
	
	private void previousPage(EditorState p_state, HttpSession p_session,
            boolean p_fromActivity) throws EnvoyServletException
    {
        ArrayList<EditorState.PagePair> pages = p_state.getPages();
        pages = (ArrayList<PagePair>) getPagePairList(p_session, pages);
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

                    if (p_state.getUserIsPm())
                    {
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

                if (p_state.getUserIsPm())
                {
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
                }
            }
        }

    }
    private void nextPage(EditorState p_state, HttpSession p_session,
            boolean p_fromActivity) throws EnvoyServletException
    {
        ArrayList<EditorState.PagePair> pages = p_state.getPages();
        pages = (ArrayList<PagePair>) getPagePairList(p_session, pages);
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

                    if (p_state.getUserIsPm())
                    {
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

                if (p_state.getUserIsPm())
                {
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
                }
            }
        }

    }
    
    @ActionHandler(action = "segment", formClass = "")
    public void segment(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
    	HttpSession session = request.getSession();
        SessionManager sessionMgr = (SessionManager) session
        		.getAttribute(WebAppConstants.SESSION_MANAGER);
        EditorState state = (EditorState) sessionMgr
        		.getAttribute(WebAppConstants.EDITORSTATE);
        User user = TaskHelper.getUser(session);
        String userId = user.getUserId();
    	
    	long tuId = state.getTuId();
        long tuvId = state.getTuvId();
        long subId = state.getSubId();
        String value = request.getParameter("save");
        try
        {
            // Updated segment arrives in UTF-8, decode to Unicode
            value = EditUtil.utf8ToUnicode(value);
            SegmentView segmentView = (SegmentView) sessionMgr
                    .getAttribute(WebAppConstants.SEGMENTVIEW);
            EditorHelper.updateSegment(state, segmentView, tuId, tuvId,
                    subId, value, userId);

            // Delete the old pdf file for the Indd preview
            PreviewPDFHelper.deleteOldPdf(state.getTargetPageId()
                    .longValue(), state.getTargetLocale().getId());
            PreviewPageHandler.deleteOldPreviewFile(state
                    .getTargetPageId().longValue(), state
                    .getTargetLocale().getId());
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

        if (OnlineEditorConstants.SEGMENT_FILTER_ICE.equals(state
                .getSegmentFilter()))
        {
            request.setAttribute("refreshSource", "true");
        }

        long targetPageId = state.getTargetPageId().longValue();
        long sourceLocaleId = state.getSourceLocale().getId();
        long targetLocaleId = state.getTargetLocale().getId();
        SegmentView segmentView = EditorHelper.getSegmentView(state,
                tuId, tuvId, subId, targetPageId, sourceLocaleId,
                targetLocaleId);
        sessionMgr.setAttribute(WebAppConstants.SEGMENTVIEW, segmentView);
    }
    
    @ActionHandler(action = "comment", formClass = "")
    public void comment(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
    	HttpSession session = request.getSession();
        SessionManager sessionMgr = (SessionManager) session
        		.getAttribute(WebAppConstants.SESSION_MANAGER);
        EditorState state = (EditorState) sessionMgr
        		.getAttribute(WebAppConstants.EDITORSTATE);
    	CommentView commentView = (CommentView) sessionMgr
                .getAttribute(WebAppConstants.COMMENTVIEW);
    	User user = TaskHelper.getUser(session);
        executeCommentCommand(state, commentView, request, user);
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
            ArrayList currentIssues = p_state.getCommentThreads().getIssues();
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
                for (Map.Entry<Long, Tuv> entry : tuvs.entrySet())
                {
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
	
	@ActionHandler(action = "search", formClass = "")
    public void search(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
		HttpSession session = request.getSession();
        SessionManager sessionMgr = (SessionManager) session
        		.getAttribute(WebAppConstants.SESSION_MANAGER);
        EditorState state = (EditorState) sessionMgr
        		.getAttribute(WebAppConstants.EDITORSTATE);
        
        sessionMgr.setAttribute("userNameList", state
                .getEditorManager().getPageLastModifyUserList(state));

        sessionMgr.setAttribute("sidList", state.getEditorManager()
                .getPageSidList(state));
        sessionMgr.setAttribute("from", "online3");
    }
	
	@ActionHandler(action = "approve", formClass = "")
    public void approve(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
		HttpSession session = request.getSession();
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        EditorState state = (EditorState) sessionMgr
                .getAttribute(WebAppConstants.EDITORSTATE);
		long p_trgPageId = state.getTargetPageId().longValue();
		PageManager pageManager = ServerProxy.getPageManager();
		TargetPage targetPage = pageManager.getTargetPage(p_trgPageId);
		targetPage.setGlobalSightLocale(targetPage.getGlobalSightLocale());
		List<Tuv> tuvList= SegmentTuvUtil.getAllTargetTuvs(targetPage);
		
		Task theTask = (Task) TaskHelper.retrieveObject(session,
                WebAppConstants.WORK_OBJECT);
		long jobId = theTask.getJobId();
		
		List<TuvImpl> tuvImplList = new ArrayList<TuvImpl>();
		HashMap<String,Tuv> tuvMap = new HashMap<String,Tuv>(); 
		for(Tuv tuv : tuvList)
		{
			tuvMap.put(tuv.getTuId() + "_" + tuv.getId(), tuv);
		}
		String approveIds = request.getParameter("approveIds");
		Connection conn = DbUtil.getConnection();
		conn.setAutoCommit(false);
		List<Long> approvedTuvIds = new ArrayList<Long>();
		Date modifyDate = new Date();
		User user = TaskHelper.getUser(session);
        String userId = user.getUserId();
		if(StringUtil.isNotEmpty(approveIds))
		{
			approveIds = approveIds.substring(0, approveIds.length() - 1);
			String[] tempIds = approveIds.split(",");
			for(String tempId: tempIds)
			{
				String[] temp = tempId.split("_");
				String tuId = temp[0];
				String tuvId = temp[1];
				
				Tuv tuv = tuvMap.get(tuId + "_" + tuvId);
				
				TuvState tuvState = tuv.getState();
				if(tuvState.getValue() != TuvState.APPROVED.getValue())
				{
					tuv.setState(TuvState.APPROVED);
					tuv.setLastModified(modifyDate);
					tuv.setLastModifiedUser(userId);
					tuvImplList.add((TuvImpl) tuv);
					
					List<TuTuvAttributeImpl>  tuTuvAttributeImplList = new ArrayList<TuTuvAttributeImpl>();
					TuTuvAttributeImpl tuTuvAttributeImpl = new TuTuvAttributeImpl(
							tuv.getId(), TuTuvAttributeImpl.OBJECT_TYPE_TUV,TuTuvAttributeImpl.STATE);
					tuTuvAttributeImpl.setLongValue(tuvState.getValue());
					tuTuvAttributeImplList.add(tuTuvAttributeImpl);
					SegmentTuTuvAttributeUtil.saveTuTuvAttributes(conn, tuTuvAttributeImplList, jobId);
					
					approvedTuvIds.add(tuv.getId());
				}
			}
		}
		conn.close();
		
		SegmentTuvUtil.updateTuvs(tuvImplList, jobId);
		state.getEditorManager().updateApprovedTuvCache(approvedTuvIds, modifyDate, userId);
		
		ServletOutputStream out = response.getOutputStream();
		out.write("true".getBytes("UTF-8"));
		out.close();
    	pageReturn();
    }
	
	@ActionHandler(action = "unapprove", formClass = "")
    public void unapprove(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
		HttpSession session = request.getSession();
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        EditorState state = (EditorState) sessionMgr
                .getAttribute(WebAppConstants.EDITORSTATE);
		long p_trgPageId = state.getTargetPageId().longValue();
		PageManager pageManager = ServerProxy.getPageManager();
		TargetPage targetPage = pageManager.getTargetPage(p_trgPageId);
		targetPage.setGlobalSightLocale(targetPage.getGlobalSightLocale());
		List<Tuv> tuvList= SegmentTuvUtil.getAllTargetTuvs(targetPage);
		
		Task theTask = (Task) TaskHelper.retrieveObject(session,
                WebAppConstants.WORK_OBJECT);
		long jobId = theTask.getJobId();
		
		List<TuvImpl> tuvImplList = new ArrayList<TuvImpl>();
		HashMap<String,Tuv> tuvMap = new HashMap<String,Tuv>(); 
		for(Tuv tuv : tuvList)
		{
			tuvMap.put(tuv.getTuId() + "_" + tuv.getId(), tuv);
		}
		String unApproveIds = request.getParameter("unApproveIds");
		Connection conn = DbUtil.getConnection();
		conn.setAutoCommit(false);
		List<Long> unapprovedTuvIds = new ArrayList<Long>();
		HashMap<Long, TuvState> originalStateMap = new HashMap<Long, TuvState>();
		Date modifyDate = new Date();
		User user = TaskHelper.getUser(session);
        String userId = user.getUserId();
		if(StringUtil.isNotEmpty(unApproveIds))
		{
			unApproveIds = unApproveIds.substring(0, unApproveIds.length() - 1);
			String[] tempIds = unApproveIds.split(",");
			for(String tempId: tempIds)
			{
				String[] temp = tempId.split("_");
				String tuId = temp[0];
				String tuvId = temp[1];
				
				Tuv tuv = tuvMap.get(tuId + "_" + tuvId);
				TuvState tuvState = tuv.getState();
				if(tuvState.getValue() == TuvState.APPROVED.getValue())
				{
					List<Long> tuvIdList = new ArrayList<Long>();
					tuvIdList.add(tuv.getIdAsLong());
					List<TuTuvAttributeImpl>  tuTuvAttributeImplList
						= SegmentTuTuvAttributeUtil.getStateAttributesByTuvIds(tuvIdList, jobId);
					if(tuTuvAttributeImplList.size() > 0)
					{
						int stateInt = (int) tuTuvAttributeImplList.get(0).getLongValue();
						TuvState originalState = TuvState.valueOf(stateInt);
						tuv.setState(originalState);
						tuv.setLastModified(modifyDate);
						tuv.setLastModifiedUser(userId);
						tuvImplList.add((TuvImpl) tuv);
						
						originalStateMap.put(tuv.getId(), originalState);
						
						SegmentTuTuvAttributeUtil.deleteStateAttributes(
								conn, tuTuvAttributeImplList, jobId);
					}
					else
					{
						tuv.setState(TuvState.NOT_LOCALIZED);
						tuv.setLastModified(modifyDate);
						tuv.setLastModifiedUser(userId);
						tuvImplList.add((TuvImpl) tuv);
						
						originalStateMap.put(tuv.getId(), TuvState.NOT_LOCALIZED);
					}
					unapprovedTuvIds.add(tuv.getId());
				}
			}
		}
		conn.close();
		
		SegmentTuvUtil.updateTuvs(tuvImplList, jobId);
		state.getEditorManager().updateUnapprovedTuvCache(unapprovedTuvIds, originalStateMap, modifyDate, userId);
		
		ServletOutputStream out = response.getOutputStream();
		out.write("true".getBytes("UTF-8"));
		out.close();
    	pageReturn();
    }
	
	@ActionHandler(action = "revert", formClass = "")
    public void revert(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
		HttpSession session = request.getSession();
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        EditorState state = (EditorState) sessionMgr
                .getAttribute(WebAppConstants.EDITORSTATE);
		long p_trgPageId = state.getTargetPageId().longValue();
		PageManager pageManager = ServerProxy.getPageManager();
		TargetPage targetPage = pageManager.getTargetPage(p_trgPageId);
		targetPage.setGlobalSightLocale(targetPage.getGlobalSightLocale());
		List<Tuv> allTuvList = SegmentTuvUtil.getAllTargetTuvs(targetPage);
		List<TuvImpl> tuvList = SegmentTuvUtil.getTargetTuvs(targetPage);
		HashMap<Long, Tuv> originalTargetTuvMap = new HashMap<Long, Tuv>();
		Long targetLocaleId = targetPage.getGlobalSightLocale().getIdAsLong();
		setOriginalTargetTuvMap(tuvList, allTuvList, originalTargetTuvMap, targetLocaleId);
		
		Task theTask = (Task) TaskHelper.retrieveObject(session,
                WebAppConstants.WORK_OBJECT);
		long jobId = theTask.getJobId();
		
		List<TuvImpl> tuvImplList = new ArrayList<TuvImpl>();
		HashMap<String,Tuv> tuvMap = new HashMap<String,Tuv>(); 
		for(Tuv tuv : tuvList)
		{
			tuvMap.put(tuv.getTuId() + "_" + tuv.getId(), tuv);
		}
		String revertIds = request.getParameter("revertIds");
		List<Long> revertTuvIds = new ArrayList<Long>();
		HashMap<Long, String> originalGxmlMap = new HashMap<Long, String>();
		Connection conn = DbUtil.getConnection();
		conn.setAutoCommit(false);
		Date modifyDate = new Date();
		User user = TaskHelper.getUser(session);
        String userId = user.getUserId();
		if(StringUtil.isNotEmpty(revertIds))
		{
			revertIds = revertIds.substring(0, revertIds.length() - 1);
			String[] tempIds = revertIds.split(",");
			for(String tempId: tempIds)
			{
				String[] temp = tempId.split("_");
				String tuId = temp[0];
				String tuvId = temp[1];
				
				Tuv tuv = tuvMap.get(tuId + "_" + tuvId);
				if(originalTargetTuvMap.get(Long.parseLong(tuvId)) != null)
				{
					tuv.setGxml(originalTargetTuvMap.get(Long.parseLong(tuvId)).getGxml());
					tuv.setState(TuvState.LOCALIZED);
					tuv.setLastModified(modifyDate);
					tuv.setLastModifiedUser(userId);
					tuvImplList.add((TuvImpl) tuv);
					revertTuvIds.add(tuv.getId());
					originalGxmlMap.put(tuv.getId(), originalTargetTuvMap.get(Long.parseLong(tuvId)).getGxml());
				}
			}
		}
		conn.close();
		
		SegmentTuvUtil.updateTuvs(tuvImplList, jobId);
		state.getEditorManager().updateRevertTuvCache(revertTuvIds, originalGxmlMap, modifyDate, userId);
		
		ServletOutputStream out = response.getOutputStream();
		out.write("true".getBytes("UTF-8"));
		out.close();
    	pageReturn();
    }
	
	private void setOriginalTargetTuvMap(List<TuvImpl> tuvList, List<Tuv> allTargetTuvs, 
    		HashMap<Long, Tuv> setOriginalTargetTuvMap, long targetLocaleId)
    {
    	HashMap<Long, List<Tuv>> tempHashMap = new HashMap<Long, List<Tuv>>();
    	for(Tuv tuv: allTargetTuvs)
    	{
    		long tuId = tuv.getTuId();
    		if(tuv.getLocaleId() == targetLocaleId && tuv.getState().equals(TuvState.OUT_OF_DATE))
    		{
    			if(tempHashMap.get(tuId) == null)
    			{
    				List<Tuv> tempTuvList = new ArrayList<Tuv>();
    				tempTuvList.add(tuv);
    				tempHashMap.put(tuId, tempTuvList);
    			}
    			else
    			{
    				tempHashMap.get(tuId).add(tuv);
    			}
    		}
    	}
    	
    	for(Tuv tuv: tuvList)
    	{
    		long tuId = tuv.getTuId();
    		List<Tuv> tempTuvList = tempHashMap.get(tuId);
    		if(tempTuvList != null)
    		{
    			sortById(tempTuvList);
    			for(Tuv tempTuv: tempTuvList)
    			{
    				if(!tempTuv.getGxml().equals(tuv.getGxml()))
    				{
    					setOriginalTargetTuvMap.put(tuv.getId(), tempTuv);
    					break;
    				}
    			}
    		}
    	}
    }
	
	private static void sortById(List<Tuv> tempTuvList)
    {
    	if(tempTuvList.size() > 1)
    	{
    		Collections.sort(tempTuvList, new Comparator<Tuv>() 
    		{  
                public int compare(Tuv arg0, Tuv arg1) 
                {  
                    long id0 = arg0.getId();
                    long id1 = arg1.getId();  
                    if (id1 > id0) 
                    {  
                        return 1;  
                    } 
                    else if (id1 == id0) 
                    {  
                        return 0;  
                    }
                    else
                    {
                        return -1;  
                    }  
                }  
            });
    	}
    }
    
    @ActionHandler(action = "getData", formClass = "")
    public void getData(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
    	HttpSession session = request.getSession();
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);

        EditorState state = (EditorState) sessionMgr
                .getAttribute(WebAppConstants.EDITORSTATE);
        
        Boolean assigneeValue = (Boolean) TaskHelper.retrieveObject(session,
                WebAppConstants.IS_ASSIGNEE);
        boolean isAssignee = assigneeValue == null ? true : assigneeValue
                .booleanValue();

        String jsonStr = "";
        response.setContentType("text/html;charset=UTF-8");
        String value = "3";

        // comment button
        if ((value = request.getParameter(WebAppConstants.REVIEW_MODE)) != null)
        {
            if ("Show Comments".equals(value))
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

        // Show/Hide PTags
        if ((value = request.getParameter("pTagsAction")) != null)
        {
            if (value.equalsIgnoreCase("Show PTags"))
            {
                state.setNeedShowPTags(true);
            }
            else
            {
                state.setNeedShowPTags(false);
            }
        }

        jsonStr = state.getEditorManager().getTargetJsonData(state,
                isAssignee, getSearchParamsInMap(request));
        ServletOutputStream out = response.getOutputStream();
		out.write(jsonStr.getBytes("UTF-8"));
		out.close();
    	pageReturn();
    }
    
    private HashMap<String, String> getSearchParamsInMap(
            HttpServletRequest p_request)
    {
        HashMap<String, String> hm = new HashMap<String, String>();

        if (p_request.getParameter("searchByUser") != null)
        {
            String userId = p_request.getParameter("searchByUser");
            hm.put("userId", userId);
        }
        else if (p_request.getParameter("searchBySid") != null)
        {
            String sid = p_request.getParameter("searchBySid");
            hm.put("sid", sid);
        }

        HttpSession session = p_request.getSession();
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);

        if (sessionMgr.getAttribute(JobManagementHandler.PAGE_SEARCH_TEXT) != null
                && sessionMgr
                        .getAttribute(JobManagementHandler.PAGE_SEARCH_TEXT) != "")
        {
            String searchText = (String) sessionMgr
                    .getAttribute(JobManagementHandler.PAGE_SEARCH_TEXT);
            hm.put("searchText", searchText);
        }
        
        hm.put("needOriginalTarget", "true");
        
        return hm;
    }

    @Override
    public void beforeAction(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException,
            EnvoyServletException
    {
    	HttpSession session = request.getSession();
        SessionManager sessionMgr = (SessionManager) session
        		.getAttribute(WebAppConstants.SESSION_MANAGER);
        Locale uiLocale = (Locale) session
        		.getAttribute(WebAppConstants.UILOCALE);
        EditorState state = (EditorState) sessionMgr
        		.getAttribute(WebAppConstants.EDITORSTATE);
        User user = TaskHelper.getUser(session);
        Boolean assigneeValue = (Boolean) TaskHelper.retrieveObject(session,
                WebAppConstants.IS_ASSIGNEE);
        boolean isAssignee = assigneeValue == null ? true : assigneeValue
                .booleanValue();
        String pageSearchText = request
                .getParameter(JobManagementHandler.PAGE_SEARCH_TEXT);
        if (StringUtil.isNotEmpty(pageSearchText))
        {
            pageSearchText = URLDecoder.decode(pageSearchText, "UTF-8");
            sessionMgr.setAttribute(JobManagementHandler.PAGE_SEARCH_TEXT,
                    pageSearchText);
        }
        
        String jobId = request.getParameter(WebAppConstants.JOB_ID);
    	String taskId = request.getParameter(WebAppConstants.TASK_ID);
    	String srcPageId = request.getParameter(WebAppConstants.SOURCE_PAGE_ID);
        String trgPageId = request.getParameter(WebAppConstants.TARGET_PAGE_ID);
        
    	if (taskId != null && srcPageId != null && trgPageId != null)
        {
            sessionMgr.setAttribute(WebAppConstants.IS_FROM_ACTIVITY, "yes");
            // store jobId, target language and source page id for Lisa QA
            // report
            Task theTask = (Task) TaskHelper.retrieveObject(session,
                    WebAppConstants.WORK_OBJECT);
            sessionMgr.setAttribute(WebAppConstants.JOB_ID,
                    Long.toString(theTask.getJobId()));
            sessionMgr.setAttribute("taskStatus", theTask.getStateAsString());
            sessionMgr.setAttribute(ReportConstants.TARGETLOCALE_LIST,
                    String.valueOf(theTask.getTargetLocale().getId()));
            sessionMgr.setAttribute(WebAppConstants.SOURCE_PAGE_ID, srcPageId);

            state = new EditorState();

            EditorHelper.initEditorManager(state);
            EditorHelper.initEditorOptions(state, session);

            sessionMgr.setAttribute(WebAppConstants.EDITORSTATE, state);

            initializeFromActivity(state, session, user.getUserId(), taskId,
                    srcPageId, trgPageId, isAssignee, request, uiLocale);

            initState(state, session);
            
            if(theTask.getStateAsString().equals(Task.STATE_ACCEPTED_STR) && 
            		user.getUserId().equals(theTask.getAcceptor()))
            {
            	sessionMgr.setAttribute("approveAction", "true");
            }
            else
            {
            	sessionMgr.setAttribute("approveAction", "false");
            }
        }
    	else if (jobId != null && srcPageId != null)
        {
            isAssignee = false;
            TaskHelper.storeObject(session, IS_ASSIGNEE,new Boolean(isAssignee));

            state = new EditorState();
            EditorHelper.initEditorManager(state);
            EditorHelper.initEditorOptions(state, session);
            sessionMgr.setAttribute(WebAppConstants.EDITORSTATE, state);
            sessionMgr.setAttribute(WebAppConstants.JOB_ID,
                    Long.parseLong(jobId));
            sessionMgr.setAttribute(ReportConstants.TARGETLOCALE_LIST,
                    getTargetIDS(jobId, srcPageId));
            sessionMgr.setAttribute(WebAppConstants.SOURCE_PAGE_ID, srcPageId);

            initializeFromJob(state, request, jobId, srcPageId, trgPageId,
                    uiLocale, user);

            initState(state, session);
            
            sessionMgr.setAttribute("approveAction", "false");
        }
    	
    	HashMap<String, String> hm = getSearchParamsInMap(request);
        updateSourcePageView(state, request, isAssignee,
                isIE(request), hm);
        
        if (StringUtil.isNotEmpty(request.getParameter("trgViewLocale")))
        {
            state.setCommentThreads(null);

            state.setTargetViewLocale(EditorHelper.getLocale(request.getParameter("trgViewLocale")));
            state.setTargetPageHtml(null);

            sessionMgr.setAttribute("trgViewLocale",
                    EditorHelper.getLocale(request.getParameter("trgViewLocale")).getDisplayName());

            if (state.hasGsaTags())
            {
                state.clearSourcePageHtml();
                EditorHelper.invalidateCachedTemplates(state);
            }
        }
        
        CommentThreadView view = state.getCommentThreads();
        if (view == null)
        {
            view = EditorHelper.getCommentThreads(state);
            state.setCommentThreads(view);
        }
        if (view != null)
        {
            if (StringUtil.isNotEmpty(request.getParameter("sortComments")))
            {
                view.sort(request.getParameter("sortComments"));
            }
        }
        
        if (StringUtil.isNotEmpty(request.getParameter("segmentFilter")))
        {
            state.setSegmentFilter(request.getParameter("segmentFilter"));
        }
        request.setAttribute("segmentFilter", state.getSegmentFilter());
        
        if (StringUtil.isNotEmpty(request.getParameter("reviewMode")))
        {
            if ("true".equals(request.getParameter("reviewMode")))
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
    }
    
    private void initializeFromJob(EditorState p_state,
            HttpServletRequest p_request, String p_jobId, String p_srcPageId,
            String p_trgPageId, Locale p_uiLocale, User p_user)
            throws EnvoyServletException
    {
        p_state.setUserIsPm(true);
        HttpSession session = p_request.getSession();
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
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

            setCurrentPage(p_request.getSession(), p_state, p_srcPageId,
                    p_trgPageId);

            EditorState.PagePair currentPage = p_state.getCurrentPage();

            p_state.setTargetViewLocale(currentPage
                    .getTargetPageLocale(new Long(p_trgPageId)));
        }
        else
        {
            // No target page/locale requested, find a suitable one.

            setCurrentPage(p_request.getSession(), p_state, p_srcPageId);

            GlobalSightLocale viewLocale = p_state.getTargetViewLocale();
            Vector trgLocales = p_state.getJobTargetLocales();
            GlobalSightLocale local = (GlobalSightLocale) sessionMgr
                    .getAttribute("targetLocale");
            if (viewLocale == null || !trgLocales.contains(viewLocale))
            {
                if (trgLocales.contains(local))
                {
                    Iterator it = trgLocales.iterator();
                    while (it.hasNext())
                    {
                        GlobalSightLocale trgLocale = (GlobalSightLocale) it
                                .next();
                        if (local.getLocale().equals(trgLocale.getLocale()))
                        {

                            p_state.setTargetViewLocale((GlobalSightLocale) trgLocale);
                        }
                    }
                }
                else
                {
                    p_state.setTargetViewLocale((GlobalSightLocale) trgLocales
                            .elementAt(0));
                }
            }
        }

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

        p_state.setAllowEditSnippets(s_pmCanEditSnippets);

        p_state.setReviewMode();
    }
    
    private void setCurrentPage(HttpSession p_session, EditorState p_state,
            String p_srcPageId, String p_trgPageId)
    {
        ArrayList pages = p_state.getPages();
        pages = (ArrayList<PagePair>) getPagePairList(p_session, pages);
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
    
    private void setCurrentPage(HttpSession p_session, EditorState p_state,
            String p_srcPageId)
    {
        ArrayList pages = p_state.getPages();
        pages = (ArrayList<PagePair>) getPagePairList(p_session, pages);
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
    
    private String getTargetIDS(String p_jobId, String p_srcPageId)
            throws EnvoyServletException
    {
        StringBuffer result = new StringBuffer();
        try
        {
            Job job = ServerProxy.getJobHandler().getJobById(
                    Long.parseLong(p_jobId));
            Collection<Workflow> wfs = job.getWorkflows();
            for (Iterator<Workflow> it = wfs.iterator(); it.hasNext();)
            {
                Workflow wf = (Workflow) it.next();
                if (Workflow.CANCELLED.equals(wf.getState())
                        || Workflow.EXPORT_FAILED.equals(wf.getState())
                        || Workflow.IMPORT_FAILED.equals(wf.getState()))
                {
                    continue;
                }

                result.append(wf.getTargetLocale().getId()).append(",");
            }
        }
        catch (Exception e)
        {
            CATEGORY.error("Problem getting job from database ", e);
            throw new EnvoyServletException(e);
        }

        if (result.length() > 0 && result.toString().endsWith(","))
        {
            result.deleteCharAt(result.length() - 1);
        }

        return result.toString();
    }
    
    private void updateSourcePageView(EditorState p_state,
            HttpServletRequest p_request, boolean p_isTaskAssignee,
            boolean p_isIE, HashMap p_searchMap) throws EnvoyServletException
    {
        int viewMode = p_state.getLayout().getSourceViewMode();
        int uiMode = getUiMode(p_state, p_isTaskAssignee);

        String html;
        // Update sourcePageHtml whatever it is null or not as it maybe is
        // "batch navigation".
        p_state.setRenderingOptions(initRenderingOptions(
                p_request.getSession(), uiMode, viewMode,
                UIConstants.EDITMODE_DEFAULT));
        if (viewMode == UIConstants.VIEWMODE_LIST)
        {
            p_state.setSourcePageHtml(viewMode, "");
        }
        else
        {
            html = EditorHelper.getSourcePageView(p_state, false, p_searchMap);
            html = OfficeContentPostFilterHelper.fixHtmlForSkeleton(html);
            html = replaceImgForFirefox(html, p_isIE);
            p_state.setSourcePageHtml(viewMode, html);
        }
    }
    
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
    
    private RenderingOptions initRenderingOptions(HttpSession p_session,
            int p_uiMode, int p_viewMode, int p_editMode)
    {
        PermissionSet permSet = (PermissionSet) p_session
                .getAttribute(WebAppConstants.PERMISSIONS);
        return new RenderingOptions(p_uiMode, p_viewMode, p_editMode, permSet);
    }
    
    private int getUiMode(EditorState p_state, boolean p_isTaskAssignee)
    {
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

        return editorMode;
    }
    
    private boolean isIE(HttpServletRequest p_request)
    {
        return (p_request.getHeader("User-Agent").toLowerCase()
                .indexOf("msie")) != -1 ? true : false;
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

        setCurrentPageFromActivity(p_session,p_state, p_srcPageId);
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
    
    private void setCurrentPageFromActivity(HttpSession p_session,EditorState p_state,
            String p_srcPageId)
    {
        ArrayList pages = p_state.getPages();
        pages = (ArrayList<PagePair>) getPagePairList(p_session, pages);
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
    
    private List<EditorState.PagePair> getPagePairList(HttpSession p_session,
            List<EditorState.PagePair> pages)
    {
        SessionManager sessionMgr = (SessionManager) p_session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        List<Long> sourcePageIdList = (List<Long>) sessionMgr
                .getAttribute("sourcePageIdList");

        List<EditorState.PagePair> newPages = new ArrayList<EditorState.PagePair>();
        if (sourcePageIdList != null && sourcePageIdList.size() > 0)
        {
            for (int i = 0; i < pages.size(); i++)
            {
                EditorState.PagePair page = pages.get(i);
                if (sourcePageIdList.contains(page.getSourcePageId()))
                {
                    newPages.add(page);
                }
            }
        }
        else
        {
            newPages = pages;
        }
        return newPages;
    }

    @Override
    public void afterAction(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException,
            EnvoyServletException
    {
    	
    }
}
