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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.everest.comment.Issue;
import com.globalsight.everest.edit.online.CommentView;
import com.globalsight.everest.edit.online.PaginateInfo;
import com.globalsight.everest.edit.online.SegmentView;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.projecthandler.MachineTranslationProfile;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.mtprofile.MTProfileHandlerHelper;
import com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
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
    @SuppressWarnings("unchecked")
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            EnvoyServletException
    {
        HttpSession session = p_request.getSession();

        User user = TaskHelper.getUser(session);
        String userId = user.getUserId();

        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        // "state" is initialized when open pop-up editor
        EditorState state = (EditorState) sessionMgr
                .getAttribute(WebAppConstants.EDITORSTATE);

        long targetPageId = state.getTargetPageId().longValue();
        long sourceLocaleId = state.getSourceLocale().getId();
        long targetLocaleId = state.getTargetLocale().getId();
        MachineTranslationProfile machineTranslationProfile = MTProfileHandlerHelper
                .getMtProfileBySourcePageId(state.getSourcePageId(),
                        state.getTargetLocale());
        String value;
        if ((value = p_request.getParameter("tuId")) != null)
        {
            state.setTuId(Long.parseLong(value));
        }
        if ((value = p_request.getParameter("tuvId")) != null)
        {
            state.setTuvId(Long.parseLong(value));
        }
        if ((value = p_request.getParameter("subId")) != null)
        {
            state.setSubId(Long.parseLong(value));
        }
        if ((value = p_request.getParameter("ptags")) != null)
        {
            state.setPTagFormat(value);
        }
        // for close all comments issue
        String isClosedComment = "";
        if ((value = p_request.getParameter("isClosedComment")) != null)
        {
            isClosedComment = value;
        }

        long tuId = state.getTuId();
        long tuvId = state.getTuvId();
        long subId = state.getSubId();
        String originalKey = tuId + "_" + tuvId + "_" + subId;
        String newKey = null;

        ConcurrentHashMap<String, SegmentView> segmentViewMap =
                (ConcurrentHashMap<String, SegmentView>) sessionMgr
                        .getAttribute(WebAppConstants.SEGMENT_VIEW_MAP);
        if (segmentViewMap == null)
        {
            segmentViewMap = new ConcurrentHashMap<String, SegmentView>();
        }
        CopyOnWriteArrayList<SegmentKey> tuTuvSubIDList =
                (CopyOnWriteArrayList<SegmentKey>) sessionMgr
                        .getAttribute(WebAppConstants.PAGE_TU_TUV_SUBID_SET);

        SegmentView segmentView = (SegmentView) sessionMgr
                .getAttribute(WebAppConstants.SEGMENTVIEW);

        /**
         * Execute "save" if: 1. "Save" is clicked; 2. Target is changed when
         * click "next segment" or "previous segment",and auto save is set to
         * yes. 3. Target is changed when click "Close" and auto save is set to
         * yes.
         */
        if ((value = p_request.getParameter("save")) != null)
        {
            try
            {
                // Updated segment arrives in UTF-8, decode to Unicode
                value = EditUtil.utf8ToUnicode(value);

                // For paragraph editor which does not preload the
                // segment view but saves through this page handler.
                if (segmentView == null)
                {
                    segmentView = EditorHelper.getSegmentView(state, tuId,
                            tuvId, subId, targetPageId, sourceLocaleId,
                            targetLocaleId, false);
                }

                EditorHelper.updateSegment(state, segmentView, tuId, tuvId,
                        subId, value, userId);

                // Delete the old pdf file for the Indd preview
                PreviewPDFPageHandler
                        .deleteOldPdf(targetPageId, targetLocaleId);
                PreviewPageHandler.deleteOldPreviewFile(targetPageId,
                        targetLocaleId);

                // As target is changed,remove this from cache to ensure it is
                // obtained again from DB when cache data.
                segmentViewMap.remove(originalKey);
                sessionMgr.setAttribute(WebAppConstants.SEGMENT_VIEW_MAP,
                        segmentViewMap);
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

        // next & previous segment
        if ((value = p_request.getParameter("refresh")) != null)
        {
            // Close all comments for current TUV.
            if (isClosedComment.trim().equals("true"))
            {
                closeAllCommentForCurrentSegment(state, userId);
            }

            int i_direction = Integer.parseInt(value);
            if (i_direction == -1) // go backward
            {
                // Update previous tuID, tuvID, subID from cache to save time.
                boolean isSucceed = setPreviousSegment(state, tuTuvSubIDList);
                if (!isSucceed)
                {
                    EditorHelper.previousSegment(state);

                    // Update the key set in session for performance reason.
                    tuId = state.getTuId();
                    tuvId = state.getTuvId();
                    subId = state.getSubId();
                    newKey = tuId + "_" + tuvId + "_" + subId;
                    if (!newKey.equals(originalKey))
                    {
                        SegmentKey originalSegKey = CacheSegmentViewDataThread
                                .findSegmentKey(tuTuvSubIDList, originalKey);
                        SegmentKey preSegKey = CacheSegmentViewDataThread
                                .findSegmentKey(tuTuvSubIDList, newKey);
                        if (originalSegKey == null)
                        {
                            originalSegKey = new SegmentKey(originalKey);
                        }
                        if (preSegKey == null)
                        {
                            preSegKey = new SegmentKey(newKey);
                        }
                        originalSegKey.setPreviousKey(newKey);
                        preSegKey.setNextKey(originalKey);

                        CacheSegmentViewDataThread.updateSessionWithSegmentKey(
                                sessionMgr, tuTuvSubIDList, originalSegKey);
                        CacheSegmentViewDataThread.updateSessionWithSegmentKey(
                                sessionMgr, tuTuvSubIDList, preSegKey);
                    }
                }

                // If current TU is on previous page, need refresh pop-up
                // editor.
                setUpdatePopUpEditorPrevious(state);
            }
            else if (i_direction == 1) // go forward
            {
                // Update next tuID, tuvID, subID from cache to save time.
                boolean isSucceed = setNextSegment(state, tuTuvSubIDList);
                if (!isSucceed)
                {
                    EditorHelper.nextSegment(state);

                    // Update the key set in session for performance reason.
                    tuId = state.getTuId();
                    tuvId = state.getTuvId();
                    subId = state.getSubId();
                    newKey = tuId + "_" + tuvId + "_" + subId;
                    if (!newKey.equals(originalKey))
                    {
                        SegmentKey originalSegKey = CacheSegmentViewDataThread
                                .findSegmentKey(tuTuvSubIDList, originalKey);
                        SegmentKey nextSegKey = CacheSegmentViewDataThread
                                .findSegmentKey(tuTuvSubIDList, newKey);
                        if (originalSegKey == null)
                        {
                            originalSegKey = new SegmentKey(originalKey);
                        }
                        if (nextSegKey == null)
                        {
                            nextSegKey = new SegmentKey(newKey);
                        }
                        originalSegKey.setNextKey(newKey);
                        nextSegKey.setPreviousKey(originalKey);

                        CacheSegmentViewDataThread.updateSessionWithSegmentKey(
                                sessionMgr, tuTuvSubIDList, originalSegKey);
                        CacheSegmentViewDataThread.updateSessionWithSegmentKey(
                                sessionMgr, tuTuvSubIDList, nextSegKey);
                    }
                }

                // If current TU is on next page, need refresh pop-up editor.
                setUpdatePopUpEditorNext(state);
            }

            // This option is "true" for both "Save" and "Refresh".
            boolean b_releverage = false;
            if ((value = p_request.getParameter("releverage")) != null)
            {
                b_releverage = value.equals("true");
            }

            // Try to get segmentView from cache,otherwise get it from DB.
            // If "direction" is not -1 and 1,"newKey" is still null.
            tuId = state.getTuId();
            tuvId = state.getTuvId();
            subId = state.getSubId();
            newKey = tuId + "_" + tuvId + "_" + subId;
            segmentView = (SegmentView) segmentViewMap.get(newKey);
            if (segmentView == null)
            {
                segmentView = EditorHelper.getSegmentView(state, tuId, tuvId,
                        subId, targetPageId, sourceLocaleId, targetLocaleId,
                        b_releverage);

                // Put this segmentView into SEGMENT_VIEW_MAP in session.
                segmentViewMap.put(newKey, segmentView);
                sessionMgr.setAttribute(WebAppConstants.SEGMENT_VIEW_MAP,
                        segmentViewMap);
            }
            else
            {
                TargetPage tp = ServerProxy.getPageManager().getTargetPage(
                        targetPageId);
                // If segmentView has existed in cache,reset its TM matches to
                // include page_tm data.
                segmentView = state.getEditorManager().addSegmentMatches(
                        segmentView, state, tuId, tuvId, subId, sourceLocaleId,
                        targetLocaleId, b_releverage,
                        tp.getSourcePage().getCompanyId());

                // Put this segmentView into SEGMENT_VIEW_MAP in session.
                segmentViewMap.put(newKey, segmentView);
                sessionMgr.setAttribute(WebAppConstants.SEGMENT_VIEW_MAP,
                        segmentViewMap);
            }

            EditorHelper.setEditorType(state, segmentView);
            // Set this segmentView in session for UI usage.
            sessionMgr.setAttribute(WebAppConstants.SEGMENTVIEW, segmentView);
            if (machineTranslationProfile != null)
            {
                // Set "SHOW_IN_EDITOR" value.
                sessionMgr.setAttribute("SHOW_IN_EDITOR", String
                        .valueOf(machineTranslationProfile.isShowInEditor()));
            }

            // Cache "next" and "previous" segmentView asynchronously.
            EditorState cloneState = EditorState.cloneState(state);
            CacheSegmentViewDataThread t = new CacheSegmentViewDataThread(
                    sessionMgr, cloneState, targetPageId, sourceLocaleId,
                    targetLocaleId, b_releverage);
            t.start();
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
                String comment = issue.getComment();
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
     * Get previous tuID, tuvID, subID from cached key list to update the state
     * object.
     * 
     * @param p_state
     * @param p_tuTuvSubIDSet
     * 
     * @return boolean which indicates if the state is updated successfully.
     */
    private boolean setPreviousSegment(EditorState p_state,
            CopyOnWriteArrayList<SegmentKey> p_tuTuvSubIDList)
    {
        if (p_state == null || p_tuTuvSubIDList == null
                || p_tuTuvSubIDList.size() < 2)
        {
            return false;
        }

        boolean succeed = false;
        try
        {
            long tuID = p_state.getTuId();
            long tuvID = p_state.getTuvId();
            long subID = p_state.getSubId();
            String key = tuID + "_" + tuvID + "_" + subID;

            SegmentKey currentSegKey = CacheSegmentViewDataThread
                    .findSegmentKey(p_tuTuvSubIDList, key);
            if (currentSegKey != null)
            {
                String preKey = currentSegKey.getPreviousKey();
                if (preKey != null)
                {
                    CacheSegmentViewDataThread
                            .resetTuTuvSubIDs(p_state, preKey);
                    succeed = true;
                }
            }
        }
        catch (Exception ex)
        {
            succeed = false;
        }

        return succeed;
    }

    /**
     * Get next tuID, tuvID, subID from cached key list to update the state
     * object.
     * 
     * @param p_state
     * @param p_tuTuvSubIDList
     * 
     * @return boolean which indicates if the state is updated successfully.
     */
    private boolean setNextSegment(EditorState p_state,
            CopyOnWriteArrayList<SegmentKey> p_tuTuvSubIDList)
    {
        if (p_state == null || p_tuTuvSubIDList == null
                || p_tuTuvSubIDList.size() < 2)
        {
            return false;
        }

        boolean succeed = false;
        try
        {
            long tuID = p_state.getTuId();
            long tuvID = p_state.getTuvId();
            long subID = p_state.getSubId();
            String key = tuID + "_" + tuvID + "_" + subID;

            SegmentKey currentSegKey = CacheSegmentViewDataThread
                    .findSegmentKey(p_tuTuvSubIDList, key);
            if (currentSegKey != null)
            {
                String nextKey = currentSegKey.getNextKey();
                if (nextKey != null)
                {
                    CacheSegmentViewDataThread.resetTuTuvSubIDs(p_state,
                            nextKey);
                    succeed = true;
                }
            }
        }
        catch (Exception ex)
        {
            succeed = false;
        }

        return succeed;
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
        ArrayList tus = p_state.getTuIds();

        PaginateInfo pi = p_state.getPaginateInfo();
        int segmentNumPerPage = pi.getSegmentNumPerPage();
        int currentPageNum = pi.getCurrentPageNum();
        int beginIndex = (currentPageNum - 1) * segmentNumPerPage;

        // Put tuIDs of current page to list.
        ArrayList tusForCurrentBatch = new ArrayList();
        int tmpCount = 0;
        for (int i = beginIndex, max = tus.size(); tmpCount < segmentNumPerPage
                && i < max; i++)
        {
            tmpCount++;
            tusForCurrentBatch.add(tus.get(i));
        }

        // If the new TU is not on current page, need refresh pop-up editor.
        if (tusForCurrentBatch != null
                && !tusForCurrentBatch.contains(new Long(currentTuId)))
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
        ArrayList tus = p_state.getTuIds();

        PaginateInfo pi = p_state.getPaginateInfo();
        int segmentNumPerPage = pi.getSegmentNumPerPage();
        int currentPageNum = pi.getCurrentPageNum();
        int beginIndex = (currentPageNum - 1) * segmentNumPerPage;

        // Put tuIDs of current page to list.
        ArrayList tusForCurrentBatch = new ArrayList();
        int tmpCount = 0;
        for (int i = beginIndex, max = tus.size(); tmpCount < segmentNumPerPage
                && i < max; i++)
        {
            tmpCount++;
            tusForCurrentBatch.add(tus.get(i));
        }

        // If the new TU is not on current page, need refresh pop-up editor.
        if (tusForCurrentBatch != null
                && !tusForCurrentBatch.contains(new Long(currentTuId)))
        {
            p_state.setNeedUpdatePopUpEditor("next");
        }
    }

}
