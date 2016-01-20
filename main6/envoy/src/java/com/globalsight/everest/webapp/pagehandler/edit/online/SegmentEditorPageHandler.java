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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.everest.comment.Issue;
import com.globalsight.everest.comment.IssueHistory;
import com.globalsight.everest.edit.online.CommentView;
import com.globalsight.everest.edit.online.PaginateInfo;
import com.globalsight.everest.edit.online.SegmentView;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.projecthandler.MachineTranslationProfile;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.mtprofile.MTProfileHandlerHelper;
import com.globalsight.everest.webapp.pagehandler.edit.online.previewPDF.PreviewPDFHelper;
import com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.machineTranslation.MTHelper2;
import com.globalsight.util.edit.EditUtil;

/**
 * <p>
 * SegmentEditorPageHandler is responsible for:
 * </p>
 * <ol>
 * <li>Displaying the segment editor screen.</li>
 * </ol>
 */

public class SegmentEditorPageHandler extends PageHandler implements
        EditorConstants
{
    private static final Logger CATEGORY = Logger
            .getLogger(SegmentEditorPageHandler.class);

    public SegmentEditorPageHandler()
    {
        super();
    }

    /**
     * Invokes this PageHandler
     * 
     * @param p_pageDescriptor
     *            the page descriptor
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

        // "state" is initialized when open pop-up editor
        EditorState state = (EditorState) sessionMgr
                .getAttribute(WebAppConstants.EDITORSTATE);

        SegmentView segmentView = (SegmentView) sessionMgr
                .getAttribute(WebAppConstants.SEGMENTVIEW);

        User user = TaskHelper.getUser(session);
        String userId = user.getUserId();

        // set parameters into state.
        setParameters(p_request, state);

        // Only "se_main.jsp" will run "save" and "refresh" codes.
        String value = null;
        // save segment
        if ((value = p_request.getParameter("save")) != null)
        {
            saveSegment(value, state, segmentView, userId);
            segmentView = null;//after save, force to reload segment view.
        }

        // next & previous segment
        if ((value = p_request.getParameter("refresh")) != null)
        {
            // Close all comments for current TUV.
            String isClosedComment = p_request.getParameter("isClosedComment");
            if (isClosedComment != null
                    && "true".equals(isClosedComment.trim()))
            {
                closeAllCommentForCurrentSegment(state, userId);
            }

            int i_direction = Integer.parseInt(value);
            if (i_direction == -1) // go backward
            {
                EditorHelper.previousSegment(state);
                // If current TU is on previous page,need refresh pop-up editor.
                setUpdatePopUpEditorPrevious(state);
            }
            else if (i_direction == 1) // go forward
            {
                EditorHelper.nextSegment(state);
                // If current TU is on next page, need refresh pop-up editor.
                setUpdatePopUpEditorNext(state);
            }

            // fulfill SegmentView
            long tuId = state.getTuId();
            long tuvId = state.getTuvId();
            long subId = state.getSubId();
            long targetPageId = state.getTargetPageId().longValue();
            long sourceLocaleId = state.getSourceLocale().getId();
            long targetLocaleId = state.getTargetLocale().getId();
            
            String fromPage = p_request.getParameter("fromPage");
            if ("source".equals(fromPage))
            {
                TargetPage targetPage = ServerProxy.getPageManager().getTargetPage(targetPageId);
                long jobId = targetPage.getSourcePage().getJobId();
                
                Tuv sourceTuv = ServerProxy.getTuvManager().getTuvForSegmentEditor(tuvId, jobId);
                Tuv targetTuv = sourceTuv.getTu(jobId).getTuv(targetLocaleId, jobId);
                
                tuvId = targetTuv.getId();
                state.setTuvId(tuvId);
            }

            segmentView = EditorHelper.getSegmentView(state, tuId, tuvId,
                    subId, targetPageId, sourceLocaleId, targetLocaleId);
            EditorHelper.setEditorType(state, segmentView);
            // Set this segmentView in session for UI usage.
            sessionMgr.setAttribute(WebAppConstants.SEGMENTVIEW, segmentView);

            setShowInEditorAttribute(sessionMgr, state);
        }

        EditorHelper.checkSynchronizationStatus(state);

        super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                p_context);
    }
    
    /**
     * Close all comments for current TUV.
     * 
     * @param p_state
     * @param p_targetPageId
     * @param p_userName
     */
    private void closeAllCommentForCurrentSegment(EditorState p_state,
            String p_userName)
    {
        if (p_state == null)
            return;

        long tuId = p_state.getTuId();
        long tuvId = p_state.getTuvId();
        long subId = p_state.getSubId();
        long targetPageId = p_state.getTargetPageId().longValue();

        try
        {
            CommentView commentView = p_state.getEditorManager()
                    .getCommentView(-1, targetPageId, tuId, tuvId, subId);
            Issue issue = commentView.getComment();
            if (issue != null)
            {
                String title = issue.getTitle();
                IssueHistory history = (IssueHistory) issue.getHistory().get(0);
                String comment = history.getComment();
                String priority = issue.getPriority();
                String status = "closed";
                String category = issue.getCategory();
                EditorHelper.editComment(p_state, commentView, title, comment,
                        priority, status, category, p_userName);
            }
        }
        catch (Exception e)
        {
            CATEGORY.error("Failed to close comments for tuv : " + tuvId + "; "
                    + e.getMessage());
        }

    }

    /**
     * If current TU is on previous page, need refresh pop-up editor.
     * 
     * @param p_state
     */
    private void setUpdatePopUpEditorPrevious(EditorState p_state)
    {
        // set this option to null first
        p_state.setNeedUpdatePopUpEditor(null);

        long currentTuId = p_state.getTuId();
        ArrayList<Long> tuIds = p_state.getTuIds();

        PaginateInfo pi = p_state.getPaginateInfo();
        int segmentNumPerPage = pi.getSegmentNumPerPage();
        int currentPageNum = pi.getCurrentPageNum();
        int beginIndex = (currentPageNum - 1) * segmentNumPerPage;

        // Put tuIDs of current page to list.
        Set<Long> tuIdsOfCurBatch = new HashSet<Long>();
        int tmpCount = 0;
        for (int i = beginIndex, max = tuIds.size(); tmpCount < segmentNumPerPage
                && i < max; i++)
        {
            tmpCount++;
            tuIdsOfCurBatch.add(tuIds.get(i));
        }

        // If the new TU is not on current page, need refresh pop-up editor.
        if (!tuIdsOfCurBatch.contains(new Long(currentTuId)))
        {
            p_state.setNeedUpdatePopUpEditor("previous");
        }
    }

    /**
     * If current TU is on next page, need refresh pop-up editor.
     * 
     * @param p_state
     */
    private void setUpdatePopUpEditorNext(EditorState p_state)
    {
        // set this option to null first
        p_state.setNeedUpdatePopUpEditor(null);

        long currentTuId = p_state.getTuId();
        ArrayList<Long> tuIds = p_state.getTuIds();

        PaginateInfo pi = p_state.getPaginateInfo();
        int segmentNumPerPage = pi.getSegmentNumPerPage();
        int currentPageNum = pi.getCurrentPageNum();
        int beginIndex = (currentPageNum - 1) * segmentNumPerPage;

        // Put tuIDs of current page to list.
        Set<Long> tuIdsOfCurBatch = new HashSet<Long>();
        int tmpCount = 0;
        for (int i = beginIndex, max = tuIds.size(); tmpCount < segmentNumPerPage
                && i < max; i++)
        {
            tmpCount++;
            tuIdsOfCurBatch.add(tuIds.get(i));
        }

        // If the new TU is not on current page, need refresh pop-up editor.
        if (!tuIdsOfCurBatch.contains(new Long(currentTuId)))
        {
            p_state.setNeedUpdatePopUpEditor("next");
        }
    }

    /**
     * Set parameters into EditorState such as "tuId", "tuvId", "subId",
     * "ptags".
     * 
     * @param p_request
     * @param p_state
     */
    private void setParameters(HttpServletRequest p_request, EditorState p_state)
    {
        String value = null;
        if ((value = p_request.getParameter("tuId")) != null)
        {
            p_state.setTuId(Long.parseLong(value));
        }
        if ((value = p_request.getParameter("tuvId")) != null)
        {
            p_state.setTuvId(Long.parseLong(value));
        }
        if ((value = p_request.getParameter("subId")) != null)
        {
            p_state.setSubId(Long.parseLong(value));
        }
        if ((value = p_request.getParameter("ptags")) != null)
        {
            p_state.setPTagFormat(value);
        }
    }
    
    /**
     * Execute "save" if:
     * <p>
     * 1. "Save" is clicked;
     * </p>
     * <p>
     * 2. Target is changed when click "next segment" or "previous segment",and
     * auto save is set to yes.
     * </p>
     * <p>
     * 3. Target is changed when click "Close" and auto save is set to yes.
     * </p>
     * 
     * @param p_valueToBeSaved
     * @param p_state
     * @param p_segmentView
     * @param userId
     */
    private void saveSegment(String p_valueToBeSaved, EditorState p_state,
            SegmentView p_segmentView, String p_userId)
    {
        try
        {
            SegmentView segmentView = p_segmentView;

            // Updated segment arrives in UTF-8, decode to Unicode
            String value = EditUtil.utf8ToUnicode(p_valueToBeSaved);

            long tuId = p_state.getTuId();
            long tuvId = p_state.getTuvId();
            long subId = p_state.getSubId();
            long targetPageId = p_state.getTargetPageId().longValue();
            long sourceLocaleId = p_state.getSourceLocale().getId();
            long targetLocaleId = p_state.getTargetLocale().getId();

            // For paragraph editor which does not preload the
            // segment view but saves through this page handler.
            if (segmentView == null)
            {
                segmentView = EditorHelper.getSegmentView(p_state, tuId, tuvId,
                        subId, targetPageId, sourceLocaleId, targetLocaleId);
            }

            EditorHelper.updateSegment(p_state, segmentView, tuId, tuvId,
                    subId, value, p_userId);

            // Delete the old PDF file for the INDD preview
            PreviewPDFHelper.deleteOldPdf(targetPageId, targetLocaleId);
            PreviewPageHandler.deleteOldPreviewFile(targetPageId,
                    targetLocaleId);
        }
        catch (EnvoyServletException e)
        {
            // This should, of course, never fail. If it fails,
            // we just redisplay the current state.
            CATEGORY.error("SE ignoring update exception", e);
        }
        catch (Exception e)
        {
            CATEGORY.error("SE ignoring update exception", e);
        }
    }

    /**
     * Set "SHOW_IN_EDITOR" attribute which is used for "MT matches" section in
     * segment editor.
     * 
     * @param sessionMgr
     * @param p_state
     */
    private void setShowInEditorAttribute(SessionManager sessionMgr,
            EditorState p_state)
    {
        MachineTranslationProfile machineTranslationProfile =
                MTProfileHandlerHelper.getMtProfileBySourcePageId(
                        p_state.getSourcePageId(), p_state.getTargetLocale());
        if (machineTranslationProfile != null)
        {
        	sessionMgr.setAttribute("showMachineTranslation", String.valueOf(true));
            sessionMgr.setAttribute(MTHelper2.SHOW_IN_EDITOR,
                    String.valueOf(machineTranslationProfile.isShowInEditor()));
        }
    }

}
