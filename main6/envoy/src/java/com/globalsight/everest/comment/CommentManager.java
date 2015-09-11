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

/**
 * Manages comments and their attachments (reference file information), and also
 * issues.
 */
public interface CommentManager
{
    /**
     * Service name for RMI registration.
     */
    public static final String SERVICE_NAME = "CommentReferenceManager";

    /**
     * Get the comment specified by the id. If the comment doesn't exist it'll
     * return null.
     */
    public Comment getCommentById(long p_commentId) throws RemoteException,
            CommentException;

    /**
     * Save the comment attached to a WorkObject (job or task).
     * 
     * NOTE that the comment should not be more than 4000 characters. Otherwise
     * it should be added as a comment reference/attachment.
     * 
     * @param p_wo
     *            The work object the comment is attached to.
     * @param p_id
     *            The id of the work object.
     * @param p_creatorUserName
     *            The name of the user creating the comment.
     * @param p_comment
     *            The comment string
     * 
     * @return The newly created and saved Comment.
     * @throws RemoteException
     *             , TaskException
     */
    public Comment saveComment(WorkObject p_wo, long p_id,
            String p_creatorUserId, String p_comment) throws RemoteException,
            CommentException;

    /**
     * Save the comment attached to a WorkObject (job or task). NOTE that the
     * comment should not be more than 4000 characters. Otherwise it should be
     * added as a comment reference/attachment.
     * 
     * @param p_wo
     *            The work object the comment is attached to.
     * @param p_id
     *            The id of the work object.
     * @param p_creatorUserName
     *            The name of the user creating the comment.
     * @param p_comment
     *            The comment string param p_date The date the comment is
     *            created on.
     * 
     * @return The newly created and saved Comment.
     * @throws RemoteException
     *             , TaskException
     */
    public Comment saveComment(WorkObject p_wo, long p_id,
            String p_creatorUserId, String p_comment, Date p_date)
            throws RemoteException, CommentException;

    /**
     * Update an already existing comment. NOTE that the comment should not be
     * more than 4000 characters.
     * 
     * @param p_commentId
     *            The id of the comment to update.
     * @param p_modifierUserName
     *            The username of the comment modifier.
     * @param p_newCommentText
     *            The new comment string for the update.
     * 
     * @return The newly updated comment.
     * 
     * @throws RemoteException
     *             , CommentException
     */
    public Comment updateComment(long p_commentId, String p_modifierUserId,
            String p_newCommentText) throws RemoteException, CommentException;

    /**
     * Deletes the specified comment reference file.
     */
    public void deleteCommentReference(CommentFile p_file, String tmpDir)
            throws RemoteException, CommentException;

    public void changeToGeneral(CommentFile p_file, String tmpDir)
            throws RemoteException, CommentException;

    public void changeToRestrict(CommentFile p_file, String tmpDir)
            throws RemoteException, CommentException;

    /**
     * Retrieves a list of comment reference file descriptors matching the given
     * source/target locale (and category).
     */
    public ArrayList /* of CommentFile */getCommentReferences(
            String p_commentId, String p_access) throws RemoteException,
            CommentException;

    public ArrayList /* of CommentFile */getCommentReferences(
            String p_commentId, String p_access, boolean saved)
            throws RemoteException, CommentException;

    public ArrayList getCommentReferences(String p_commentId, String p_access,
            String companyId) throws RemoteException, CommentException;

    /**
     * Gets all the task comments that are associated with the specific job and
     * target locales. If no locales are specified then it gets all. These
     * comments are the ones that were added at the task/activity level (not the
     * job level or segment level).
     * 
     * @param p_jobId
     *            The id of the job that the tasks are asssociated with whose
     *            comments are being retrieved.
     * @param p_targetLocales
     *            The list of GlobalSightLocale objects to get comments from. If
     *            NULL get for all target locales. Assumes the target locales
     *            specified are valid ones for the job.
     * 
     * @return Returns a list of TaskCommentInfo objects that hold the
     *         information about the task comments that are associated with the
     *         job in the locales specified.
     */
    public ArrayList getTaskComments(long p_jobId, List p_targetLocales)
            throws RemoteException, CommentException;

    // -------------------------ISSUES--------------------------------------

    /**
     * Add a new issue.
     * 
     * @param p_objectLevelType
     *            The type of object the issue is attached to. It is called
     *            "Level" in the requirements document.
     * @see com.globalsight.everest.comment.Issue for the list of valid types.
     * @param p_levelObjectId
     *            The unique id of the object that the issue is associated with.
     * @param p_title
     *            The title of the issue.
     * @param p_priority
     *            The priority of the issue.
     * @see Issue.java for the valid priorities.
     * @param p_status
     *            The status the issue is in.
     * @see Issue.java for the valid status.
     * @param p_category
     *            The category of the issue.
     * @see Issue.java for the valid category.
     * @param p_creatorUserId
     *            The id of the user that created the issue.
     * @param p_comment
     *            The comment text.
     * @param p_logicalKey
     *            The logical key stores a hierarchy of keys that show the
     *            hierarchy for the issue on the This parameter should be the
     *            whole key. It is stored and used later for querying and
     *            knowing to what hierarchy of objects the issue belongs to. For
     *            segments the logical key is <target page id>_<tu id>_<tuv
     *            id>_<sub id>
     * 
     * @return Returns the newly created and added Issue.
     */
    public Issue addIssue(int p_levelObjectType, long p_levelObjectId,
            String p_title, String p_priority, String p_status,
            String p_category, String p_creatorUserId, String p_comment,
            String p_logicalKey) throws RemoteException, CommentException;

    public Issue addIssue(int p_levelObjectType, long p_levelObjectId,
            String p_title, String p_priority, String p_status,
            String p_category, String p_creatorUserId, String p_comment,
            String p_logicalKey, boolean share, boolean overwrite)
            throws RemoteException, CommentException;

    /**
     * Reply and add a comment to an existing issue. The status, title and
     * priority can also be edited if this is the user that is assigned to the
     * current activity.
     * 
     * @param p_issueId
     *            The id of the issue that is being replied to.
     * @param p_title
     *            The title of the issue. This can be the same as before or
     *            specify a different title for the issue.
     * @param p_priority
     *            The priority of the issue. This can be the same as before or
     *            specify a different priority for the issue.
     * @see Issue.java for the valid priorities.
     * @param p_status
     *            The status the issue is in. This can be the same status as
     *            before or specify a different status the issue is now in.
     * @see Issue.java for the valid status.
     * @param p_category
     *            The category of the issue.
     * @see Issue.java for the valid category.
     * @param p_reportedBy
     *            The id of the user that replied to the issue and added this
     *            comment.
     * @param p_comment
     *            The comment text to be added. This should not be NULL or empty
     *            since it is a reply to a previous comment.
     */
    public Issue replyToIssue(long p_issueId, String p_title,
            String p_priority, String p_status, String p_category,
            String p_reportedBy, String p_comment) throws RemoteException,
            CommentException;

    public Issue replyToIssue(long p_issueId, String p_title,
            String p_priority, String p_status, String p_category,
            String p_reportedBy, String p_comment, boolean share,
            boolean overwrite) throws RemoteException, CommentException;

    /**
     * Edit the status, priority, title and/or the last comment(IssueHistory)
     * made to the issue. In order to edit the last comment the p_reportedBy
     * user id must be the same as the reported by user in the latest
     * IssueHistory.
     * 
     * @param p_issueId
     *            The id of the issue that is being edited.
     * @param p_title
     *            The title of the issue. This can be the same as before or
     *            specify a different title.
     * @param p_priority
     *            The priority of the issue. This can be the same as before or
     *            specify a different priority for the issue.
     * @see Issue.java for the valid priorities.
     * @param p_status
     *            The status the issue is in. This can be the same status as
     *            before or specify a different status the issue is now in.
     * @see Issue.java for the valid status.
     * @param p_category
     *            The category of the issue.
     * @see Issue.java for the valid category.
     * @param p_reportedBy
     *            The id of the user that replied to the issue and added this
     *            comment.
     * @param p_comment
     *            The comment text to be added. It can be NULL or empty if the
     *            text from the latest IssueHistory should be removed.
     */
    public Issue editIssue(long p_issueId, String p_title, String p_priority,
            String p_status, String p_category, String p_reportedBy,
            String p_comment) throws RemoteException, CommentException;

    public Issue editIssue(long p_issueId, String p_title, String p_priority,
            String p_status, String p_category, String p_reportedBy,
            String p_comment, boolean share, boolean overwrite)
            throws RemoteException, CommentException;

    /**
     * Get the issue with the particular id.
     * 
     * @param p_issueId
     *            The unique identifier for the issue.
     * 
     * @return An issue or null if it doesn't exist.
     */
    public Issue getIssue(long p_issueId) throws RemoteException,
            CommentException;

    /**
     * Gets all the issues that are associated with the specific target object
     * type and contain the specific logical key.
     * 
     * @param p_levelObjectType
     *            The type of object the issues should be associated with
     * @see Issue for valid types (prefix TYPE_)
     * @param p_targetPageId
     * 
     * @return Returns a list of Issue objects.
     */
    public ArrayList<IssueImpl> getIssues(int p_levelObjectType,
            long p_targetPageId) throws RemoteException, CommentException;

    /**
     * Returns the number of issues associated with the type, matching the
     * specified logical key or logical key prefix and with the statuses
     * specified. If the status list is empty or NULL then search across all
     * status.
     * 
     * @param p_levelObjectType
     *            The type of object the issues should be associated with.
     * @see Issue for valid types, types start with the prefix TYPE_
     * @param p_targetPageId
     * @param p_statusList
     *            A List of String objects that contain the status of issues to
     *            search for. If empty or NULL then search for all. Valid states
     *            can be found in Issue with the prefix STATUS_*
     * 
     * @return The number of issues there are that fulfill the criteria.
     */
	public int getIssueCount(int p_levelObjectType, Long p_targetPageId,
			List<String> p_statusList) throws RemoteException, CommentException;

    /**
	 * Returns the number of issues associated with the type, matching the
	 * specified target page IDs and with the statuses specified. If the status
	 * list is empty or NULL then search across all status.
	 * 
	 * @param p_levelObjectType
	 *            The type of object the issues should be associated with.
	 * @see Issue for valid types, types start with the prefix TYPE_
	 * @param p_targetPageIds
	 *            The target page IDs List to count issues from.
	 * @param p_statusList
	 *            A List of String objects that contain the status of issues to
	 *            search for. If empty or NULL then search for all. Valid states
	 *            can be found in Issue with the prefix STATUS_*
	 * 
	 * @return The number of issues there are that fulfill the criteria.
	 */
    public int getIssueCount(int p_levelObjectType, List<Long> p_targetPageIds,
            List<String> p_statusList) throws RemoteException, CommentException;

	/**
	 * Returns the number of issues associated with the type, target page IDs
	 * and statuses per page in a map.
	 * 
	 * @param p_levelObjectType
	 * @param p_targetPageIds
	 * @param p_statusList
	 * @return HashMap<Long, Integer>:target page ID : count.
	 * @throws RemoteException
	 * @throws CommentException
	 */
	public HashMap<Long, Integer> getIssueCountPerTargetPage(
			int p_levelObjectType, List<Long> p_targetPageIds,
			List<String> p_statusList) throws RemoteException, CommentException;
    
    /**
     * Delete the issues that are associated with the specified object type and
     * the ids. It will delete one or more issues of the same type.
     * 
     * @param p_levelObjectType
     *            The type of object the issues to be deleted are associated
     *            with.
     * @see Issue for valid types. types start with the prefix TYPE_.
     * @param p_levelObjectIds
     *            A list of Longs that contains the ids of the objects that the
     *            issues are associated with.
     */
    public void deleteIssues(int p_levelObjectType, List p_levelObjectIds)
            throws RemoteException, CommentException;

}
