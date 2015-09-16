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

import java.io.File;
import java.io.FileFilter;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.globalsight.everest.foundation.WorkObject;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.jobhandler.JobPersistenceAccessor;
import com.globalsight.everest.persistence.comment.CommentQueryResultHandler;
import com.globalsight.everest.persistence.comment.CommentUnnamedQueries;
import com.globalsight.everest.persistence.comment.IssueUnnamedQueries;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.taskmanager.TaskImpl;
import com.globalsight.everest.taskmanager.TaskPersistenceAccessor;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.everest.workflowmanager.WorkflowImpl;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.terminology.util.SqlUtil;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.GlobalSightLocale;

/**
 * <p>
 * Implementation of comment reference file persistence server.
 * </p>
 * 
 * <p>
 * Files are stored under ~<file storage>/GlobalSight/CommentReference. Each job
 * is a sub-directory, there might be some files which would be visible to
 * Admin/PM/WFManagers only. They are called "Restricted" files and are
 * therefore kept in a separate directory named "Restricted". All other files
 * can be viewd by all kinds of users in the job called as "General" flies and
 * are therefore kept in a directory named "General"
 * </p>
 * 
 * <pre>
 * /CommentReference/CommentID/Restricted/cr1.txt
 * /CommentReference/CommentID/General/cr2.txt
 * </pre>
 */
public class CommentManagerLocal implements CommentManager
{
    static private final Logger CATEGORY = Logger
            .getLogger(CommentManagerLocal.class);

    //
    // Interface methods
    //

    /**
     * Deletes the specified comment reference file.
     */
    public void deleteCommentReference(CommentFile p_file, String tmpDir)
            throws RemoteException, CommentException
    {

        String[] access =
        { WebAppConstants.COMMENT_REFERENCE_GENERAL_ACCESS,
                WebAppConstants.COMMENT_REFERENCE_RESTRICTED_ACCESS };
        try
        {
            String filePath = getFilePath(p_file, tmpDir);

            for (int i = 0; i < access.length; i++)
            {
                String fileName = filePath + access[i] + "/"
                        + p_file.getFilename();
                File file = new File(fileName);

                if (file.exists())
                {
                    file.delete();
                }
            }
        }
        catch (Exception ex)
        {
            String[] arg =
            { p_file.getFilename() };

            throw new CommentException(
                    CommentException.MSG_FAILED_TO_DELETE_FILE, arg, ex);
        }
    }

    /**
     * Deletes the specified comment reference file.
     */
    public void changeToGeneral(CommentFile p_file, String tmpDir)
            throws RemoteException, CommentException
    {
        try
        {
            String filePath = getFilePath(p_file, tmpDir);
            String oldPath = filePath
                    + WebAppConstants.COMMENT_REFERENCE_RESTRICTED_ACCESS + "/"
                    + p_file.getFilename();
            String newPath = filePath
                    + WebAppConstants.COMMENT_REFERENCE_GENERAL_ACCESS + "/"
                    + p_file.getFilename();
            File file = new File(oldPath);
            File newFile = new File(newPath);
            if (file.exists())
            {
                int i = 2;
                while (newFile.exists())
                {
                    newPath = newPath + "(" + i + ")";
                    newFile = new File(newPath);
                }

                newFile.getParentFile().mkdirs();
                file.renameTo(newFile);
            }
        }
        catch (Exception ex)
        {
            CATEGORY.error(ex.getMessage(), ex);
            throw new CommentException(ex.getMessage());
        }
    }

    /**
     * Deletes the specified comment reference file.
     */
    public void changeToRestrict(CommentFile p_file, String tmpDir)
            throws RemoteException, CommentException
    {
        try
        {
            String filePath = getFilePath(p_file, tmpDir);
            String oldPath = filePath
                    + WebAppConstants.COMMENT_REFERENCE_GENERAL_ACCESS + "/"
                    + p_file.getFilename();
            String newPath = filePath
                    + WebAppConstants.COMMENT_REFERENCE_RESTRICTED_ACCESS + "/"
                    + p_file.getFilename();
            File file = new File(oldPath);
            File newFile = new File(newPath);
            if (file.exists())
            {
                int i = 2;
                while (newFile.exists())
                {
                    newPath = newPath + "(" + i + ")";
                    newFile = new File(newPath);
                }

                newFile.getParentFile().mkdirs();
                file.renameTo(newFile);
            }
        }
        catch (Exception ex)
        {
            CATEGORY.error(ex.getMessage(), ex);
            throw new CommentException(ex.getMessage());
        }
    }

    /**
     * @see CommentManager.getCommentRefrences(String, String)
     */
    public ArrayList getCommentReferences(String p_commentId, String p_access)
            throws RemoteException, CommentException
    {
        return getCommentReferences(p_commentId, p_access, false);
    }

    /**
     * @see CommentManager.getCommentRefrences(String, String)
     */
    public ArrayList getCommentReferences(String p_commentId, String p_access,
            String companyId) throws RemoteException, CommentException
    {
        return getCommentReferences(p_commentId, p_access, false, companyId);
    }

    /**
     * @see CommentManager.getCommentReferences(String, String, boolean)
     */
    public ArrayList /* of CommentFile */getCommentReferences(
            String p_commentId, String p_access, boolean p_saved,
            String companyId) throws RemoteException, CommentException
    {
        ArrayList result = readCommentReferenceDirectory(
                AmbFileStoragePathUtils.getCommentReferenceDir(companyId)
                        .toString(), p_commentId, p_access, p_saved);

        return result;
    }

    /**
     * @see CommentManager.getCommentReferences(String, String, boolean)
     */
    public ArrayList /* of CommentFile */getCommentReferences(
            String p_commentId, String p_access, boolean p_saved)
            throws RemoteException, CommentException
    {
        ArrayList result = readCommentReferenceDirectory(
                AmbFileStoragePathUtils.getCommentReferenceDir().toString(),
                p_commentId, p_access, p_saved);

        return result;
    }

    /**
     * @see CommentManager.getCommentById(long)
     */
    public Comment getCommentById(long p_commentId) throws RemoteException,
            CommentException
    {
        try
        {
            return (CommentImpl) HibernateUtil.get(CommentImpl.class,
                    p_commentId);
        }
        catch (Exception ex)
        {
            CATEGORY.error("Failed to find the comment with id " + p_commentId);
        }

        return null;
    }

    /**
     * @see CommentManager.save(WorkObject, long, String, String)
     */
    public Comment saveComment(WorkObject p_wo, long p_id,
            String p_creatorUserId, String p_comment) throws RemoteException,
            CommentException
    {
        return saveComment(p_wo, p_id, p_creatorUserId, p_comment, Calendar
                .getInstance().getTime());
    }

    /**
     * @see CommentManager.save(WorkObject, long, String, String, Date)
     */
    public Comment saveComment(WorkObject p_wo, long p_id,
            String p_creatorUserId, String p_comment, Date p_date)
            throws RemoteException, CommentException
    {
        // not supposed to save null comment
        if (p_comment == null)
        {
            return null;
        }

        if (p_wo == null)
        {
            // if the task was not found - need this for HP's integration API
            // in case they send invalid task ids
            CATEGORY.error("Failed to specify a workflow object to add the comment to.");
            throw new CommentException(
                    "CommmentException.FAILED_TO_SPECIFY_WORK_OBJECT", null,
                    null);
        }

        Comment comment = null;
        Session session = HibernateUtil.getSession();
        Transaction transaction = session.beginTransaction();

        try
        {
            comment = new CommentImpl(p_date, p_creatorUserId, p_comment, p_wo);
            session.save(comment);

            if (p_wo instanceof Job)
            {
                Job job = (Job) session.get(JobImpl.class,
                        new Long(p_wo.getId()));
                job.addJobComment(comment);
                session.update(job);
            }
            else if (p_wo instanceof Workflow)
            {
                Workflow workflow = (Workflow) session.get(WorkflowImpl.class,
                        new Long(p_wo.getId()));
                workflow.addWorkflowComment(comment);
                session.update(workflow);
            }
            else if (p_wo instanceof Task)
            {
                Task task = (TaskImpl) session.get(TaskImpl.class, new Long(
                        p_wo.getId()));
                task.addTaskComment(comment);
                session.update(task);
            }

            transaction.commit();
        }
        catch (Exception ex)
        {
            transaction.rollback();

            CATEGORY.error("saveComment() failed : " + ex.toString(), ex);

            String objectType = "Unknown";
            if (p_wo instanceof Job)
            {
                objectType = "Job";
            }
            else if (p_wo instanceof Workflow)
            {
                objectType = "Workflow";
            }
            else if (p_wo instanceof Task)
            {
                objectType = "Task";
            }

            String[] msgArgs =
            { objectType, String.valueOf(p_id) };

            throw new CommentException(
                    CommentException.MSG_FAILED_TO_SAVE_COMMENT, msgArgs, ex);
        }

        // session.close();

        return comment;
    }

    /**
     * @see CommentManager.updateComment(long, String, String)
     */
    public Comment updateComment(long p_commentId, String p_modifierUserId,
            String p_commentString) throws RemoteException, CommentException
    {
        try
        {
            Comment com = getCommentById(p_commentId);

            if (com != null)
            {
                WorkObject wo = com.getWorkObject();

                if (wo instanceof Job)
                {
                    Job job = (Job) wo;
                    // refresh the job and leave for editing
                    job = JobPersistenceAccessor.getJob(job.getId(), true);
                    int jcIndex = job.getJobComments().indexOf(com);
                    if (jcIndex > -1)
                    {
                        Comment jobComment = (Comment) job.getJobComments()
                                .get(jcIndex);
                        updateComment((CommentImpl) jobComment,
                                p_commentString, p_modifierUserId);
                        JobPersistenceAccessor.updateJobState(job);
                    }
                }
                else if (wo instanceof Task)
                {
                    Task task = (Task) wo;
                    task = TaskPersistenceAccessor.getTask(task.getId(), true);
                    int tcIndex = task.getTaskComments().indexOf(com);
                    if (tcIndex > -1)
                    {
                        Comment taskComment = (Comment) task.getTaskComments()
                                .get(tcIndex);
                        updateComment((CommentImpl) taskComment,
                                p_commentString, p_modifierUserId);
                        TaskPersistenceAccessor.updateTask(task);
                    }
                }
            }

            return com;
        }
        catch (Exception ex)
        {
            CATEGORY.error("Failed to update comment " + p_commentId);
            throw new CommentException(ex);
        }
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
        return addIssue(p_levelObjectType, p_levelObjectId, p_title,
                p_priority, p_status, p_category, p_creatorUserId, p_comment,
                p_logicalKey, false, false);
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
        Issue issue = null;

        try
        {
            // create the new comment and clone
            issue = new IssueImpl(p_levelObjectType, p_levelObjectId, p_title,
                    p_priority, p_status, p_category, p_creatorUserId,
                    p_comment, p_logicalKey);
            issue.setShare(share);
            issue.setOverwrite(overwrite);

            HibernateUtil.saveOrUpdate(issue);
        }
        catch (Exception ex)
        {
            CATEGORY.error("Failed to add a new issue.", ex);
            String[] args =
            { IssueImpl.getLevelTypeAsString(p_levelObjectType),
                    String.valueOf(p_levelObjectId), p_creatorUserId };
            throw new CommentException(
                    CommentException.MSG_FAILED_TO_ADD_ISSUE, args, ex);
        }

        return issue;
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
        return replyToIssue(p_issueId, p_title, p_priority, p_status,
                p_category, p_reportedBy, p_comment, false, false);
    }

    /**
     * @see CommentManager.replyToIssue(long, String, String, String, String,
     *      String, String)
     */
    public Issue replyToIssue(long p_issueId, String p_title,
            String p_priority, String p_status, String p_category,
            String p_reportedBy, String p_comment, boolean share,
            boolean overwrite) throws RemoteException, CommentException
    {
        IssueImpl issue = null;

        try
        {
            issue = (IssueImpl) HibernateUtil.get(IssueImpl.class, p_issueId);

            issue.setTitle(p_title);
            issue.setPriority(p_priority);
            issue.setStatus(p_status);
            issue.setCategory(p_category);
            issue.setOverwrite(overwrite);
            issue.setShare(share);

            if (p_reportedBy != null)
            {
                // even if the comment is empty that is fine it'll at least
                // store that a change was made by this user
                issue.addHistory(p_reportedBy, p_comment);
            }

            HibernateUtil.saveOrUpdate(issue);
        }
        catch (Exception ex)
        {
            CATEGORY.error("Failed to edit issue.", ex);
            String[] args =
            { String.valueOf(p_issueId), p_reportedBy };
            throw new CommentException(
                    CommentException.MSG_FAILED_TO_EDIT_ISSUE, args, ex);
        }

        return issue;
    }

    /**
     * @see CommentManager.editIssue(long, String, String, String, String,
     *      String, String)
     */
    public Issue editIssue(long p_issueId, String p_title, String p_priority,
            String p_status, String p_category, String p_reportedBy,
            String p_comment) throws RemoteException, CommentException
    {

        return editIssue(p_issueId, p_title, p_priority, p_status, p_category,
                p_reportedBy, p_comment, false, false);
    }

    public Issue editIssue(long p_issueId, String p_title, String p_priority,
            String p_status, String p_category, String p_reportedBy,
            String p_comment, boolean share, boolean overwrite)
            throws RemoteException, CommentException
    {
        IssueImpl issue = null;

        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        Connection conn = null;
        PreparedStatement stmt = null;

        try
        {
            issue = (IssueImpl) session.get(IssueImpl.class,
                    new Long(p_issueId));

            issue.setTitle(p_title);
            issue.setPriority(p_priority);
            issue.setStatus(p_status);
            issue.setCategory(p_category);
            issue.setOverwrite(overwrite);
            issue.setShare(share);

            if (p_reportedBy != null)
            {
                // edit the latest history if the user is the same
                // the latest history is the first in the list
                IssueHistoryImpl ih = (IssueHistoryImpl) issue.getHistory()
                        .get(0);

                if (ih.reportedBy().equals(p_reportedBy))
                {
                    // For the "changing comments not saved" issue, use jdbc to
                    // update ISSUE_HISTORY table.
                    conn = SqlUtil.hireConnection();
                    conn.setAutoCommit(false);

                    String sqlUpdate = "update ISSUE_HISTORY set DESCRIPTION= ? ,"
                            + "REPORTED_DATE = ? "
                            + " Where REPORTED_BY = ? and REPORTED_DATE = ?";
                    stmt = conn.prepareStatement(sqlUpdate);
                    Date date = ih.dateReportedAsDate();
                    Date currentDate = Calendar.getInstance().getTime();

                    ih.dateReported(Calendar.getInstance().getTime());
                    ih.setComment(p_comment);
                    session.saveOrUpdate(ih);

                    stmt.setString(1, p_comment);
                    stmt.setDate(2, new java.sql.Date(currentDate.getTime()));
                    stmt.setString(3, p_reportedBy);
                    stmt.setDate(4, new java.sql.Date(date.getTime()));

                    stmt.executeUpdate();
                    conn.commit();
                }
            }

            tx.commit();
        }
        catch (Exception ex)
        {
            CATEGORY.error("Failed to edit issue.", ex);
            String[] args =
            { String.valueOf(p_issueId), p_reportedBy };
            throw new CommentException(
                    CommentException.MSG_FAILED_TO_EDIT_ISSUE, args, ex);
        }
        finally
        {
        	DbUtil.silentClose(stmt);
            SqlUtil.fireConnection(conn);
            // session.close();
        }

        return issue;
    }

    /**
     * @see CommentManager.getTaskComments(long, List)
     */
    public ArrayList getTaskComments(long p_jobId, List p_targetLocales)
            throws RemoteException, CommentException
    {
        ArrayList tComments = new ArrayList();

        try
        {
            ArrayList localeIds = new ArrayList();
            if (p_targetLocales != null && p_targetLocales.size() > 0)
            {
                for (Iterator i = p_targetLocales.iterator(); i.hasNext();)
                {
                    GlobalSightLocale gsl = (GlobalSightLocale) i.next();
                    localeIds.add(gsl.getIdAsLong());
                }
            }

            // get the result
            String sql = CommentUnnamedQueries
                    .getTaskCommentsByJobIdAndLocales(p_jobId, localeIds);
            List result = HibernateUtil.searchWithSql(sql, null);

            tComments = (ArrayList) CommentQueryResultHandler
                    .handleResult(result);

            // add the attachments
            tComments = getCommentReferences(tComments);
        }
        catch (Exception ex)
        {
            CATEGORY.error("Failed to get comments for the tasks in job "
                    + p_jobId, ex);
            String args[] =
            { String.valueOf(p_jobId) };
            throw new CommentException(
                    CommentException.MSG_FAILED_TO_GET_TASK_COMMENTS, args, ex);
        }

        return tComments;
    }

    /**
     * @see CommentManager.getIssue(long)
     */
    public Issue getIssue(long p_issueId) throws RemoteException,
            CommentException
    {
        try
        {
            return (IssueImpl) HibernateUtil.get(IssueImpl.class, p_issueId);
        }
        catch (Exception ex)
        {
            String issueIdString = String.valueOf(p_issueId);
            CATEGORY.error("Failed to get the issue with id " + issueIdString,
                    ex);
            String msgArgs[] =
            { issueIdString };
            throw new CommentException(
                    CommentException.MSG_FAILED_TO_GET_ISSUE, msgArgs, ex);
        }
    }

    /**
     * @see CommentManager.getIssues(int, long);
     */
    @SuppressWarnings("unchecked")
    public ArrayList<IssueImpl> getIssues(int p_levelObjectType,
            long p_targetPageId) throws RemoteException, CommentException
    {
        try
        {
            String hql = "from IssueImpl i where i.levelObjectTypeAsString = ? "
                    + " and i.targetPageId = ? ";

            String levelType = IssueImpl
                    .getLevelTypeAsString(p_levelObjectType);
            levelType = levelType == null ? "" : levelType;

            return (ArrayList<IssueImpl>) HibernateUtil.search(hql, levelType,
                    p_targetPageId);
        }
        catch (Exception ex)
        {
            CATEGORY.error("Failed to get issues for object type "
                    + p_levelObjectType + " with targetPageId "
                    + p_targetPageId, ex);
            String msgArgs[] =
            { String.valueOf(p_levelObjectType), String.valueOf(p_targetPageId) };
            throw new CommentException(
                    CommentException.MSG_FAILED_TO_GET_ISSUES, msgArgs, ex);
        }
    }

    /**
     * @see CommentManager.getIssueCount(int, Long, List)
     */
	public int getIssueCount(int p_levelObjectType, Long p_targetPageId,
			List<String> p_statusList) throws RemoteException, CommentException
    {
    	List<Long> targetPageIds = new ArrayList<Long>();
    	targetPageIds.add(p_targetPageId);

    	return getIssueCount(p_levelObjectType, targetPageIds, p_statusList);
    }

	/**
     * @see CommentManager.getIssueCount(int, List, List)
     */
	public int getIssueCount(int p_levelObjectType, List<Long> p_targetPageIds,
			List<String> p_statusList) throws RemoteException, CommentException
    {
        int numOfIssues = 0;

        try
        {
            String sql = IssueUnnamedQueries
                    .getIssueCountByTypeStatusAndPageId(p_statusList,
                    		p_targetPageIds);
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("objectType",
                    IssueImpl.getLevelTypeAsString(p_levelObjectType));
            numOfIssues = HibernateUtil.countWithSql(sql, map);
        }
        catch (Exception ex)
        {
            CATEGORY.error(
                    "Failed to get the issue count for object type "
                            + p_levelObjectType + " with logical key "
                            + p_targetPageIds.toString(), ex);
            String msgArgs[] =
            { IssueImpl.getLevelTypeAsString(p_levelObjectType),
            		p_targetPageIds.toString(), p_statusList.toString() };
            throw new CommentException(
                    CommentException.MSG_FAILED_TO_GET_ISSUE_COUNT, msgArgs, ex);
        }

        return numOfIssues;
    }
	
    /**
     * @see CommentManager.getIssueCountPerTargetPage(int, String, List)
     */
	public HashMap<Long, Integer> getIssueCountPerTargetPage(
			int p_levelObjectType, List<Long> p_targetPageIds,
			List<String> p_statusList) throws RemoteException, CommentException
	{
		HashMap<Long, Integer> result = new HashMap<Long, Integer>();
		try
		{
			String sql = IssueUnnamedQueries
					.getIssueCountByTypeStatusPerPageId(p_statusList,
							p_targetPageIds);
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("objectType",
                    IssueImpl.getLevelTypeAsString(p_levelObjectType));
            List<?> list = HibernateUtil.searchWithSql(sql, map);
            if (list != null && list.size() > 0)
            {
				for (int i = 0; i < list.size(); i++)
            	{
            		Object[] contents = (Object[]) list.get(i);
            		Long tpId = Long.parseLong(contents[0].toString());
            		Integer count = Integer.parseInt(contents[1].toString());
            		result.put(tpId, count);
            	}
            }
		}
		catch (Exception ex)
		{
            CATEGORY.error(
                    "Failed to get the issue count per page for object type "
                            + p_levelObjectType + " with logical key "
                            + p_targetPageIds.toString(), ex);
            String msgArgs[] =
            { IssueImpl.getLevelTypeAsString(p_levelObjectType),
            		p_targetPageIds.toString(), p_statusList.toString() };
            throw new CommentException(
                    CommentException.MSG_FAILED_TO_GET_ISSUE_COUNT, msgArgs, ex);
		}

		return result;
	}

    /**
     * @see CommentManager.deleteIssues(int, List)
     */
    public void deleteIssues(int p_levelObjectType, List p_levelObjectIds)
            throws RemoteException, CommentException
    {
        CATEGORY.info("Deleting issues that are associated with "
                + IssueImpl.getLevelTypeAsString(p_levelObjectType)
                + " objects, with ids of " + p_levelObjectIds + ".");

        if (p_levelObjectIds == null || p_levelObjectIds.size() == 0)
        {
            return;
        }

        int numOfDeletedIssues = 0;

        try
        {
            Map map = new HashMap();
            map.put("objectType",
                    IssueImpl.getLevelTypeAsString(p_levelObjectType));
            // Get all the issues with the specified type and ids in
            // chunks of 500 (needs to be smaller than 1000 because of
            // the query used).
            for (int size = p_levelObjectIds.size(); size / 500 > 0;)
            {
                List chunkOfIds = p_levelObjectIds.subList(0, 499);
                String sql = IssueUnnamedQueries
                        .getIssuesByTypeAndObjectId(chunkOfIds);
                Collection issues = HibernateUtil.searchWithSql(sql, map,
                        IssueImpl.class);
                numOfDeletedIssues += issues.size();
                HibernateUtil.delete(issues);
                p_levelObjectIds.subList(0, 499).clear();
            }

            if (p_levelObjectIds != null && p_levelObjectIds.size() > 0)
            {
                String sql = IssueUnnamedQueries
                        .getIssuesByTypeAndObjectId(p_levelObjectIds);
                Collection is = HibernateUtil.searchWithSql(sql, map,
                        IssueImpl.class);
                numOfDeletedIssues += is.size();
                HibernateUtil.delete(is);
            }
        }
        catch (Exception ex)
        {
            CATEGORY.error("Failed to delete issues of type "
                    + IssueImpl.getLevelTypeAsString(p_levelObjectType)
                    + ". See previous log message for the list of affected objects.");
            String[] errorArgs =
            { IssueImpl.getLevelTypeAsString(p_levelObjectType),
                    "(see globalsight.log for the list of affected objects)" };
            throw new CommentException(
                    CommentException.MSG_FAILED_TO_DELETE_ISSUES, errorArgs, ex);
        }

        CATEGORY.info("Deleted " + numOfDeletedIssues + " issues.");
    }

    //
    // Private methods
    //

    /**
     * Takes in an array of TaskCommentInfo objects and returns them back with
     * their file attachments set. This retrieves them with the highest access
     * assumed for the files.
     */
    private ArrayList getCommentReferences(ArrayList p_comments)
            throws Exception
    {
        for (Iterator ci = p_comments.iterator(); ci.hasNext();)
        {
            TaskCommentInfo tci = (TaskCommentInfo) ci.next();

            // get all restricted and general attachments
            ArrayList attach = getCommentReferences(
                    String.valueOf(tci.getCommentId()),
                    WebAppConstants.COMMENT_REFERENCE_RESTRICTED_ACCESS, true);

            tci.setAttachments(attach);
        }

        return p_comments;
    }

    /**
     * Returns the absolute file name for the specified comment reference file.
     */
    private String getFilePath(CommentFile p_file, String tmpDir)
    {
        return new StringBuffer()
                .append(AmbFileStoragePathUtils.getCommentReferenceDirPath())
                .append(File.separator).append(tmpDir).append(File.separator)
                .toString();
    }

    /**
     * Reads a directory structure and converts files to CommentFile objects.
     * Category is currently ignored. If source and target locale are null, all
     * files are returned.
     */
    private ArrayList readCommentReferenceDirectory(String p_root,
            final String p_commentId, final String p_access, boolean p_saved)
    {
        ArrayList result = new ArrayList();

        String[] accessLevel =
        { WebAppConstants.COMMENT_REFERENCE_GENERAL_ACCESS,
                WebAppConstants.COMMENT_REFERENCE_RESTRICTED_ACCESS };
        int level = 0;
        if (p_access
                .equals(WebAppConstants.COMMENT_REFERENCE_RESTRICTED_ACCESS))
        {
            level = 2;
        }
        else
        {
            level = 1;
        }

        for (int x = 0; x < level; x++)
        {
            File path = new File(p_root + "/" + p_commentId + "/"
                    + accessLevel[x]);

            if (!path.exists() || !path.isDirectory())
            {
                continue;
            }

            // directory structure:
            // root/srclocale/trglocale/file

            // Read source locale directories. Filter by source locale if
            // that is requested.
            File[] files = path.listFiles(new FileFilter()
            {
                public boolean accept(File p_path)
                {
                    if (!p_path.isDirectory())
                    {
                        return true;
                    }

                    return false;
                }
            });

            for (int l = 0; l < files.length; ++l)
            {
                CommentFile g = new CommentFile();

                g.setFileSize(files[l].length());
                g.setLastModified(files[l].lastModified());
                g.setFilename(files[l].getName());
                g.setFileAccess(accessLevel[x]);
                g.setSaved(p_saved);
                g.setAbsolutePath(files[l].getAbsolutePath());

                result.add(g);
            }
        }

        return result;
    }

    private void updateComment(CommentImpl p_originalComment,
            String p_updatedComment, String p_modifierUserId) throws Exception
    {
        if (p_originalComment != null)
        {
            p_originalComment.setComment(p_updatedComment);
            p_originalComment.setModifierId(p_modifierUserId);
            p_originalComment.setModifiedDate(Calendar.getInstance().getTime());
        }
    }

    public HashMap getIssuesMapByTuv(Tuv tuv)
    {
        HashMap tempMap = new HashMap();

        try
        {
            String hql = "from IssueImpl a where a.levelObjectId = :tuvid";
            HashMap map = new HashMap();
            map.put("tuvid", tuv.getId());
            Collection issues = HibernateUtil.search(hql, map);
            Iterator ite = issues.iterator();

            if (ite.hasNext())
            {
                IssueImpl issue = (IssueImpl) ite.next();
                tempMap.put("IssueID", issue.getId());
                tempMap.put("LevelObjectId", issue.getLevelObjectId());
                tempMap.put("LevelObjectType",
                        issue.getLevelObjectTypeAsString());
                tempMap.put("CreateDate", issue.getCreateDate());
                tempMap.put("CreatorId", issue.getCreatorId());
                tempMap.put("Title", issue.getComment());
                tempMap.put("Priority", issue.getPriority());
                tempMap.put("Status", issue.getStatus());
                tempMap.put("LogicalKey", issue.getLogicalKey());
                tempMap.put("Category", issue.getCategory());

                Vector historyVec = new Vector();

                for (int i = 0; i < issue.getHistory().size(); i++)
                {
                    HashMap hv = new HashMap();
                    IssueHistoryImpl history = (IssueHistoryImpl) issue
                            .getHistory().get(i);
                    hv.put("HistoryID", history.getDbId());
                    hv.put("IssueID", history.getIssue().getId());
                    hv.put("Timestamp", history.getTimestamp());
                    hv.put("ReportedBy", history.getReportedBy());
                    hv.put("Comment", history.getComment());
                    historyVec.add(hv);
                }

                tempMap.put("localeId", Long.toString(tuv.getLocaleId()));
                tempMap.put("HistoryVec", historyVec);
            }
        }
        catch (Exception pe)
        {
            pe.printStackTrace();
        }

        return tempMap;
    }
}
