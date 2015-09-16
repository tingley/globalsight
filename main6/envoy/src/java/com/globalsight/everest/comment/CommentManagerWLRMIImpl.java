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

package com.globalsight.everest.comment;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.globalsight.everest.foundation.WorkObject;
import com.globalsight.everest.util.system.RemoteServer;

public class CommentManagerWLRMIImpl extends RemoteServer implements
        CommentManagerWLRemote
{
    private CommentManager m_localInstance;

    public CommentManagerWLRMIImpl() throws RemoteException
    {
        super(CommentManager.SERVICE_NAME);
        m_localInstance = new CommentManagerLocal();
    }

    /**
     * Deletes the specified comment reference file.
     */
    public void deleteCommentReference(CommentFile p_file, String tmpDir)
            throws RemoteException, CommentException
    {
        m_localInstance.deleteCommentReference(p_file, tmpDir);
    }

    /**
     * @see CommentManager.getCommentReferences(String, String)
     */
    public ArrayList /* of CommentFile */getCommentReferences(
            String p_commentId, String p_access) throws RemoteException,
            CommentException
    {
        return m_localInstance.getCommentReferences(p_commentId, p_access);
    }

    /**
     * @see CommentManager.getCommentReferences(String, String, boolean)
     */
    public ArrayList /* of CommentFile */getCommentReferences(
            String p_commentId, String p_access, boolean p_saved)
            throws RemoteException, CommentException
    {
        return m_localInstance.getCommentReferences(p_commentId, p_access,
                p_saved);
    }

    /**
     * @see CommentManager.getCommentById(long)
     */
    public Comment getCommentById(long p_commentId) throws RemoteException,
            CommentException
    {
        return m_localInstance.getCommentById(p_commentId);
    }

    /**
     * @see CommentManager.saveComment(WorkObject, long, String, String)
     */
    public Comment saveComment(WorkObject p_wo, long p_id,
            String p_creatorUserId, String p_comment) throws RemoteException,
            CommentException
    {
        return m_localInstance.saveComment(p_wo, p_id, p_creatorUserId,
                p_comment);
    }

    /**
     * Save comment for a Task. It saves comment to database task_comment table.
     * 
     * @param p_comment
     *            the comment object to be saved.
     * @throws RemoteException
     *             , TaskException
     */
    public Comment saveComment(WorkObject p_wo, long p_id,
            String p_creatorUserId, String p_comment, Date p_date)
            throws RemoteException, CommentException
    {
        return m_localInstance.saveComment(p_wo, p_id, p_creatorUserId,
                p_comment, p_date);
    }

    /**
     * @see CommentManager.updateComment(long, int, String, Comment)
     */
    public Comment updateComment(long p_commentId, String p_modifierUserId,
            String p_commentString) throws RemoteException, CommentException
    {
        return m_localInstance.updateComment(p_commentId, p_modifierUserId,
                p_commentString);
    }

    /**
     * @see CommentManager.addIssue(int, long, String, String, String, String,
     *      String, String, String)
     */
    public Issue addIssue(int p_levelObjectType, long p_levelObjectId,
            String p_title, String p_priority, String p_status,
            String p_category, String p_creatorUserId, String p_comment,
            String p_logicalKey) throws RemoteException, CommentException
    {
        return m_localInstance.addIssue(p_levelObjectType, p_levelObjectId,
                p_title, p_priority, p_status, p_category, p_creatorUserId,
                p_comment, p_logicalKey);
    }

    /**
     * @see CommentManager.addIssue(int, long, String, String, String, String,
     *      String, String, String)
     */
    public Issue addIssue(int p_levelObjectType, long p_levelObjectId,
            String p_title, String p_priority, String p_status,
            String p_category, String p_creatorUserId, String p_comment,
            String p_logicalKey, boolean share, boolean overwrite)
            throws RemoteException, CommentException
    {
        return m_localInstance.addIssue(p_levelObjectType, p_levelObjectId,
                p_title, p_priority, p_status, p_category, p_creatorUserId,
                p_comment, p_logicalKey, share, overwrite);
    }

    /**
     * @see CommentManager.replyToIssue(long, String, String, String, String,
     *      String, String)
     */
    public Issue replyToIssue(long p_issueId, String p_title,
            String p_priority, String p_status, String p_category,
            String p_reportedBy, String p_comment) throws RemoteException,
            CommentException
    {
        return m_localInstance.replyToIssue(p_issueId, p_title, p_priority,
                p_status, p_category, p_reportedBy, p_comment);
    }

    public Issue replyToIssue(long p_issueId, String p_title,
            String p_priority, String p_status, String p_category,
            String p_reportedBy, String p_comment, boolean share,
            boolean overwrite) throws RemoteException, CommentException
    {
        return m_localInstance
                .replyToIssue(p_issueId, p_title, p_priority, p_status,
                        p_category, p_reportedBy, p_comment, share, overwrite);
    }

    /**
     * @see CommentManager.editIssue(long, String, String, String, String,
     *      String, String)
     */
    public Issue editIssue(long p_issueId, String p_title, String p_priority,
            String p_status, String p_category, String p_reportedBy,
            String p_comment) throws RemoteException, CommentException
    {
        return m_localInstance.editIssue(p_issueId, p_title, p_priority,
                p_status, p_category, p_reportedBy, p_comment);
    }

    public Issue editIssue(long p_issueId, String p_title, String p_priority,
            String p_status, String p_category, String p_reportedBy,
            String p_comment, boolean share, boolean overwrite)
            throws RemoteException, CommentException
    {
        return m_localInstance
                .editIssue(p_issueId, p_title, p_priority, p_status,
                        p_category, p_reportedBy, p_comment, share, overwrite);
    }

    /**
     * @see CommentManager.getTaskComments(long, List)
     */

    public ArrayList getTaskComments(long p_jobId, List p_targetLocales)
            throws RemoteException, CommentException
    {
        return m_localInstance.getTaskComments(p_jobId, p_targetLocales);
    }

    /**
     * @see CommentManager.getIssue(long)
     */
    public Issue getIssue(long p_issueId) throws RemoteException,
            CommentException
    {
        return m_localInstance.getIssue(p_issueId);
    }

    /**
     * @see CommentManager.getIssues(int, long);
     */
    public ArrayList<IssueImpl> getIssues(int p_levelObjectType,
            long p_targetPageId) throws RemoteException, CommentException
    {
        return m_localInstance.getIssues(p_levelObjectType, p_targetPageId);
    }

    /**
     * @see CommentManager.getIssueCount(int, String, List)
     */
    public int getIssueCount(int p_levelObjectType, Long p_targetPageId,
            List<String> p_statusList) throws RemoteException, CommentException
    {
        return m_localInstance.getIssueCount(p_levelObjectType, p_targetPageId,
                p_statusList);
    }

    /**
     * @see CommentManager.getIssueCount(int, String, List)
     */
    public int getIssueCount(int p_levelObjectType, List<Long> p_targetPageIds,
            List<String> p_statusList) throws RemoteException, CommentException
    {
        return m_localInstance.getIssueCount(p_levelObjectType, p_targetPageIds,
                p_statusList);
    }

    /**
     * @see CommentManager.getIssueCountPerTargetPage(int, String, List)
     */
	public HashMap<Long, Integer> getIssueCountPerTargetPage(
			int p_levelObjectType, List<Long> p_targetPageIds,
			List<String> p_statusList) throws RemoteException, CommentException
	{
		return m_localInstance.getIssueCountPerTargetPage(p_levelObjectType,
				p_targetPageIds, p_statusList);
	}

    /**
     * @see CommentManager.deleteIssues(int, List)
     */
    public void deleteIssues(int p_levelObjectType, List p_levelObjectIds)
            throws RemoteException, CommentException
    {
        m_localInstance.deleteIssues(p_levelObjectType, p_levelObjectIds);
    }

    @Override
    public void changeToGeneral(CommentFile p_file, String tmpDir)
            throws RemoteException, CommentException
    {
        m_localInstance.changeToGeneral(p_file, tmpDir);

    }

    @Override
    public void changeToRestrict(CommentFile p_file, String tmpDir)
            throws RemoteException, CommentException
    {
        m_localInstance.changeToRestrict(p_file, tmpDir);
    }

    @Override
    public ArrayList getCommentReferences(String id, String p_access,
            String companyId) throws RemoteException, CommentException
    {
        return m_localInstance.getCommentReferences(id, p_access, companyId);
    }
}
