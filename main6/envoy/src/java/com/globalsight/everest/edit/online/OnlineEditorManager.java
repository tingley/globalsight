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

package com.globalsight.everest.edit.online;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import org.json.JSONObject;

import com.globalsight.everest.integration.ling.tm2.MatchTypeStatistics;
import com.globalsight.everest.tuv.TuvState;
import com.globalsight.everest.webapp.pagehandler.edit.online.EditorState;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;

/**
 * OnlineEditorManager, is a server interface contains APIs to serve online
 * Editor UI layer data needs.
 */
public interface OnlineEditorManager extends UIConstants
{
    //
    // Constants
    //
    public static final String SERVICE_NAME = "OnlineEditorManager";

    //
    // Methods
    //
    public OnlineEditorManager newInstance() throws OnlineEditorException,
            RemoteException;

    /**
     * For source page editing: returns the GXML of the source page.
     */
    public String getSourcePageGxml(long p_srcPageId)
            throws OnlineEditorException, RemoteException;

    public ArrayList validateSourcePageGxml(String p_gxml)
            throws RemoteException;

    public ArrayList updateSourcePageGxml(long p_srcPageId, String p_gxml)
            throws OnlineEditorException, RemoteException;

    public String getGxmlPreview(String p_gxml, String p_locale)
            throws Exception, RemoteException;

    /**
     * To get HTML formated output for source page view.
     * 
     * @param p_srcPageId
     *            The id of the source page to view.
     * @param p_options
     *            Stores the options about how to view the page and what access
     *            rights the user has.
     * @param p_locale
     *            The locale of the page.
     * @p_dirtyTemplate Specifies if the template has changed and must be
     *                  reloaded.
     * 
     * @return the page view as HTML String
     * @exception OnlineEditorManagerException
     *                - Component related exception.
     * @exception RemoteException
     *                Network related exception.
     */
    public String getSourcePageView(long p_srcPageId,
            RenderingOptions p_options, GlobalSightLocale p_locale,
            boolean p_dirtyTemplate, PaginateInfo p_pi)
            throws OnlineEditorException, RemoteException;

    public String getSourcePageView(long p_srcPageId,
            RenderingOptions p_options, GlobalSightLocale p_locale,
            boolean p_dirtyTemplate, PaginateInfo p_pi, HashMap searchMap)
            throws OnlineEditorException, RemoteException;

    /**
     * To get HTML formated output for target page view.
     * 
     * @param p_targetPageId
     *            - The id of the target page to be viewed.
     * @param EditorState
     *            - editor state information.
     * @param p_excludedItemTypes
     *            - The item type excluded by Editor.
     * @p_dirtyTemplate Specifies if the template has changed and must be
     *                  reloaded.
     * 
     * @return page view as String
     * @exception OnlineEditorManagerException
     *                - Component related exception.
     * @exception RemoteException
     *                Network related exception.
     */
    public String getTargetPageView(long p_trgPageId, EditorState p_state,
            Vector p_excludedItemTypes, boolean p_dirtyTemplate)
            throws OnlineEditorException, RemoteException;

    public String getTargetPageView(long p_trgPageId, EditorState p_state,
            Vector p_excludedItemTypes, boolean p_dirtyTemplate, HashMap p_map)
            throws OnlineEditorException, RemoteException;

    /**
     * To get HTML formated output for segment editor. The output is wrapped up
     * in SegmentView object.
     * 
     * @param p_tuId
     *            - The tu ID of the segment being edited.
     * @param p_subId
     *            - The sub ID of the segment being edited.
     * @param p_sourceLocaleId
     *            - The source locale ID of the segment being edited.
     * @param p_targetLocaleId
     *            - The target locale ID of the segment being edited.
     * 
     * @return SegmentView object
     * @exception OnlineEditorManagerException
     *                - Component related exception.
     * @exception RemoteException
     *                Network related exception.
     */
    public SegmentView getSegmentView(long p_tuId, long p_tuvId,
            String p_subId, long p_targetPageId, long p_sourceLocaleId,
            long p_targetLocaleId, String[] p_tmNames, String p_termbase)
            throws OnlineEditorException, RemoteException;
    
    public SegmentView getSegmentView(long p_tuId, long p_tuvId,
            String p_subId, long p_trgPageId, long p_sourceLocaleId,
            long p_targetLocaleId, String[] p_tmNames, String p_termbase,
            boolean isTarget) throws OnlineEditorException, RemoteException;
    

    /**
     * Retrieves the PageInfo data object: page name, page format, word count,
     * total segment count.
     */
    public PageInfo getPageInfo(long p_srcPageId) throws OnlineEditorException,
            RemoteException;

    /**
     * Returns a list of TU ids (Long) for a source page.
     */
    public ArrayList<Long> getTuIdsInPage(Long p_srcPageId)
            throws OnlineEditorException, RemoteException;

    /**
     * To update the target segment content after being edited.
     * 
     * @param p_tuId
     *            - The tu ID of the segment being edited.
     * @param p_subId
     *            - The sub ID of the segment being edited.
     * @param p_newContent
     *            - The new content of the target segment.
     * 
     * @exception OnlineEditorManagerException
     *                - Component related exception.
     * @exception RemoteException
     *                Network related exception.
     */
    public void updateTUV(long p_tuvId, String p_subId, String p_newContent,
            long p_jobId) throws OnlineEditorException, RemoteException;

    public void updateTUV(long p_tuvId, String p_subId, String p_newContent,
            String userId, long p_jobId) throws OnlineEditorException,
            RemoteException;

    /**
     * Updates an existing ImageMap for the given target page, tuv and sub or
     * creates a new ImageMap if it doesn't exist.
     */
    public void createImageMap(Long p_trgPageId, long p_tuvId, long p_subId,
            String p_tempName, String p_realName) throws OnlineEditorException,
            RemoteException;

    /**
     * Returns a set of TU ids that are part of the target page, i.e. ones that
     * have not been deleted using GSA delete tags.
     */
    public HashSet getInterpretedTuIds(long p_srcPageId,
            GlobalSightLocale p_locale) throws OnlineEditorException,
            RemoteException;

    /**
     * Invalidates the online editor cache.
     */
    public void invalidateCache() throws OnlineEditorException, RemoteException;

    /**
     * Invalidates the online editor's cached templates (for snippets).
     */
    public void invalidateCachedTemplates() throws OnlineEditorException,
            RemoteException;

    public void splitSegments(long p_tuv1, long p_tuv2, String p_location,
            long p_jobId) throws OnlineEditorException, RemoteException;

    public void mergeSegments(long p_tuv1, long p_tuv2, long p_jobId)
            throws OnlineEditorException, RemoteException;

    /**
     * Returns all segment comments for the target page.
     */
    public CommentThreadView getCommentThreads(long p_trgPageId)
            throws OnlineEditorException, RemoteException;

    /**
     * Retrieves the segment comment for the specified segment if it exists.
     * p_commentId is used to identify one of multiple comments for the same
     * segment (which, alas, have the same logical ID in 6.5). If passed in as
     * -1, either the first existing comment is returned or a new CommentView to
     * create a new comment.
     */
    public CommentView getCommentView(long p_commentId, long p_trgPageId,
            long p_tuId, long p_tuvId, long p_subId)
            throws OnlineEditorException, RemoteException;

    public void createComment(long p_tuId, long p_tuvId, long p_subId,
            String p_title, String p_comment, String p_priority,
            String p_status, String p_category, String p_user, boolean sharem,
            boolean overwrite) throws OnlineEditorException, RemoteException;

    public void createComment(long p_tuId, long p_tuvId, long p_subId,
            String p_title, String p_comment, String p_priority,
            String p_status, String p_category, String p_user)
            throws OnlineEditorException, RemoteException;

    public void editComment(CommentView p_view, String p_title,
            String p_comment, String p_priority, String p_status,
            String p_category, String p_user, boolean sharem, boolean overwrite)
            throws OnlineEditorException, RemoteException;

    public void editComment(CommentView p_view, String p_title,
            String p_comment, String p_priority, String p_status,
            String p_category, String p_user) throws OnlineEditorException,
            RemoteException;

    public void addComment(CommentView p_view, String p_title,
            String p_comment, String p_priority, String p_status,
            String p_category, String p_user) throws OnlineEditorException,
            RemoteException;

    public void addComment(CommentView p_view, String p_title,
            String p_comment, String p_priority, String p_status,
            String p_category, String p_user, boolean sharem, boolean overwrite)
            throws OnlineEditorException, RemoteException;

    public void closeAllComment(ArrayList p_issueList, String p_user)
            throws OnlineEditorException, RemoteException;

    public ArrayList getPageLastModifyUserList(EditorState p_state)
            throws GeneralException, RemoteException;

    public ArrayList getPageSidList(EditorState p_state)
            throws GeneralException, RemoteException;

    public MatchTypeStatistics getMatchTypes(Long p_sourcePageId,
            Long p_targetLocaleId) throws GeneralException, RemoteException;

    /**
     * Set TM matches for "SegmentView" object.This method allows to reset TM
     * matches separately.
     */
    public SegmentView addSegmentMatches(SegmentView p_view,
            EditorState p_state, long p_tuId, long p_tuvId, long p_subId,
            long p_sourceLocaleId, long p_targetLocaleId, long p_jobId);

    public String getSourceJsonData(EditorState p_state, boolean isAssignee);

    public String getSourceJsonData(EditorState p_state, boolean isAssignee, boolean fromInCtxRv);
    
    public String getTargetJsonData(EditorState state, boolean isAssignee,
            HashMap<String, String> hm);

    public String getTargetJsonData(EditorState state, boolean isAssignee,
            HashMap<String, String> hm, boolean fromInCtxRv);
    
    public JSONObject getTargetJsonObject(EditorState state, boolean isAssignee,
            HashMap<String, String> hm, boolean fromInCtxRv);

	public void updateApprovedTuvCache(List<Long> approvedTuvIds, Date modifiedDate, String user);

	public void updateUnapprovedTuvCache(List<Long> unapprovedTuvIds,
			HashMap<Long, TuvState> originalStateMap, Date modifiedDate, String user);

	public void updateRevertTuvCache(List<Long> revertTuvIds,
			HashMap<Long, String> originalGxmlMap, Date modifiedDate, String user);
}
