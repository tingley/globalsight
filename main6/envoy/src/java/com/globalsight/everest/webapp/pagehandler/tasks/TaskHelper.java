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
package com.globalsight.everest.webapp.pagehandler.tasks;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.naming.NamingException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.globalsight.everest.comment.Comment;
import com.globalsight.everest.costing.CostingException;
import com.globalsight.everest.edit.offline.AmbassadorDwUpConstants;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.foundation.WorkObject;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.projecthandler.ProjectImpl;
import com.globalsight.everest.qachecks.DITAQACheckerHelper;
import com.globalsight.everest.qachecks.QACheckerHelper;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.taskmanager.TaskException;
import com.globalsight.everest.taskmanager.TaskImpl;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.JobSearchConstants;
import com.globalsight.everest.workflow.WorkflowConstants;
import com.globalsight.everest.workflow.WorkflowTaskInstance;
import com.globalsight.ling.common.Text;
import com.globalsight.ling.common.URLDecoder;
import com.globalsight.ling.common.URLEncoder;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.terminology.util.SqlUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.ServerUtil;
import com.globalsight.util.edit.EditUtil;

/**
 * TaskHelper is used for communicating with remote objects and performing task
 * related actions.
 * 
 * TomyD -- Note that this class should only talk to one remote object (most
 * likely JobHandler)...TBD
 */
public class TaskHelper
{
    public static final String DETAIL_PAGE_1 = "1";
    public static final String DETAIL_PAGE_2 = "2";

    private static final Logger CATEGORY = Logger.getLogger(TaskHelper.class
            .getName());

    /**
     * To get tasks for a user by task state.
     */
    static public List getTasks(String p_userId, int p_taskState)
            throws EnvoyServletException
    {
        List tasks = null;

        try
        {
            tasks = ServerProxy.getTaskManager()
                    .getTasks(p_userId, p_taskState);
        }

        catch (GeneralException e)
        {
            CATEGORY.debug(e);
            throw new EnvoyServletException(e);
        }
        catch (RemoteException e)
        {
            CATEGORY.debug(e);
            throw new EnvoyServletException(GeneralException.EX_REMOTE, e);
        }

        return tasks;
    }

    /**
     * To get task for a user by giving the id of task.
     */
    public static Task getTask(long p_taskId) throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getTaskManager().getTask(p_taskId);
        }
        catch (TaskException te)
        {
            CATEGORY.debug(te);
            throw new EnvoyServletException(te.getMessageKey(),
                    te.getMessageArguments(), te, te.getPropertyFileName());
        }
        catch (GeneralException e)
        {
            CATEGORY.debug(e);
            throw new EnvoyServletException(e);
        }
        catch (RemoteException e)
        {
            CATEGORY.debug(e);
            throw new EnvoyServletException(GeneralException.EX_REMOTE, e);
        }
    }

    /**
     * To get task for a user by giving the id and state of task.
     */
    public static Task getTask(String p_userId, long p_taskId, int p_state)
            throws EnvoyServletException
    {
        Task task = null;

        try
        {
            task = ServerProxy.getTaskManager().getTask(p_userId, p_taskId,
                    p_state);
        }
        catch (TaskException te)
        {
            CATEGORY.debug(te);
            throw new EnvoyServletException(te.getMessageKey(),
                    te.getMessageArguments(), te, te.getPropertyFileName());
        }
        catch (GeneralException e)
        {
            CATEGORY.debug(e);
            throw new EnvoyServletException(e);
        }
        catch (RemoteException e)
        {
            CATEGORY.debug(e);
            throw new EnvoyServletException(GeneralException.EX_REMOTE, e);
        }

        return task;
    }

    public static boolean checkPageContainText(String tuTableName,
            String tuvTableName, String searchLocale, String searchText,
            long pageId, long localeId)
    {
        StringBuffer buferSql = new StringBuffer();
        buferSql.append("SELECT tuv.SEGMENT_STRING FROM ").append(tuvTableName)
                .append(" tuv,").append(tuTableName).append(" tu,");
        if (searchLocale != null
                && searchLocale.equalsIgnoreCase("sourceLocale"))
        {
            buferSql.append(" source_page_leverage_group splg");
        }
        else if (searchLocale != null
                && searchLocale.equalsIgnoreCase("targetLocale"))
        {
            buferSql.append(" target_page_leverage_group tplg");
        }
        buferSql.append(" WHERE tuv.TU_ID = tu.ID ");
        if (searchLocale != null
                && searchLocale.equalsIgnoreCase("sourceLocale"))
        {
            buferSql.append(" AND tu.LEVERAGE_GROUP_ID = splg.LG_ID");
            buferSql.append(" AND splg.SP_ID in (?) ");
        }
        else if (searchLocale != null
                && searchLocale.equalsIgnoreCase("targetLocale"))
        {
            buferSql.append(" AND tu.LEVERAGE_GROUP_ID = tplg.LG_ID ");
            buferSql.append(" AND tplg.TP_ID in (?) ");
        }
        buferSql.append(" AND tuv.LOCALE_ID IN (?)");
        buferSql.append(" AND ( LOWER(tuv.segment_string) LIKE LOWER(?) ESCAPE '&' || LOWER(tuv.segment_clob) LIKE LOWER(?) ESCAPE '&' )");
        buferSql.append("AND tuv.STATE != 'OUT_OF_DATE' " + "LIMIT 1");

        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rset = null;
        try
        {
            String checkBpt = "<bpt internal=\"yes\"";
            String checkEpt = "<ept";
            connection = SqlUtil.hireConnection();
            stmt = connection.prepareStatement(buferSql.toString());
            String queryPattern = makeWildcardQueryString(searchText, '&');
            stmt.setLong(1, pageId);
            stmt.setLong(2, localeId);
            stmt.setString(3, queryPattern);
            stmt.setString(4, queryPattern);
            rset = stmt.executeQuery();
            while (rset.next())
            {
                String segment = rset.getString(1);
                if (segment.contains(checkBpt) && segment.contains(checkEpt))
                {
                    if (checkBpt.contains(searchText)
                            || checkEpt.contains(searchText))
                    {
                        List<Integer> bptIndexs = findAllIndex(checkBpt,
                                segment, 0);
                        List<Integer> eptIndexs = findAllIndex(checkEpt,
                                segment, 0);
                        List<Integer> textIndexs = findAllIndex(searchText,
                                segment, 0);
                        if (bptIndexs.size() == textIndexs.size()
                                || eptIndexs.size() == textIndexs.size())
                        {
                            return false;
                        }
                        else if (bptIndexs.size() < textIndexs.size()
                                || eptIndexs.size() < textIndexs.size())
                        {
                            return true;
                        }

                    }
                    else if (!checkBpt.contains(searchText)
                            && !checkEpt.contains(searchText))
                    {
                        return true;
                    }
                }
                else
                {
                    return true;
                }
            }
        }
        catch (SQLException e)
        {
        }
        finally
        {
            DbUtil.silentClose(rset);
            DbUtil.silentClose(stmt);
            DbUtil.silentReturnConnection(connection);
        }

        return false;
    }

    private static List<Integer> findAllIndex(String searchText,
            String segment, int startPos)
    {
        int foundPos = -1; // -1 represents not found.
        List<Integer> foundIndexs = new ArrayList<Integer>();
        do
        {
            foundPos = segment.toLowerCase().indexOf(searchText.toLowerCase(),
                    startPos);
            if (foundPos > -1)
            {
                startPos = foundPos + 1;
                foundIndexs.add(foundPos);
            }
        } while (foundPos > -1 && startPos < segment.length());

        return foundIndexs;
    }

    static private String makeWildcardQueryString(String p_queryString,
            char p_escapeChar)
    {
        // KNOWN BUG
        /* Current implementatin does not support the escape function */

        String pattern = p_queryString;
        String escape = String.valueOf(p_escapeChar);
        String asterisk = "*";
        String percent = "%";
        String underScore = "_";

        // remove the first and the last '*' from the string
        if (pattern.startsWith(asterisk))
        {
            pattern = pattern.substring(1);
        }
        if (pattern.endsWith(asterisk)
                && pattern.charAt(pattern.length() - 2) != '\\')
        {
            pattern = pattern.substring(0, pattern.length() - 1);
        }

        // '&' -> '&&' (escape itself)
        pattern = Text.replaceString(pattern, escape, escape + escape);

        // '%' -> '&%' (escape wildcard char)
        pattern = Text.replaceString(pattern, percent, escape + percent);

        // '_' -> '&_' (escape wildcard char)
        pattern = Text.replaceString(pattern, underScore, escape + underScore);

        // '*' -> '%' (change wildcard) '\*' -> '*' (literal *)
        pattern = Text.replaceChar(pattern, '*', '%', '\\');

        // Add '%' to the beginning and the end of the string (because
        // the segment text is enclosed with <segment></segment> or
        // <localizable></localizable>)
        pattern = percent + pattern + percent;

        pattern = "<%>" + pattern + "</%>";

        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("search + replace pattern = " + pattern);
        }

        return pattern;
    }

    // Auto accept all active tasks of the Job.
    public static void autoAcceptTaskInJob(Job p_job)
    {
        List<TaskImpl> tasks = ServerProxy.getTaskManager().getActivieTasks(
                p_job.getId());
        for (TaskImpl task : tasks)
        {
            autoAcceptTask(task);
        }
    }

    /**
     * Auto accept the activity/task only when accepter is single and the
     * acceptor has the permission.
     * 
     * @param p_task
     *            The task which need to been accepted.
     */
    public static void autoAcceptTask(Task p_task)
    {
        ProjectImpl project = (ProjectImpl) p_task.getWorkflow().getJob()
                .getProject();

        boolean isDitaQaTask = DITAQACheckerHelper.isDitaQaActivity(p_task);
        boolean isQATask = QACheckerHelper.isQAActivity(p_task);
        if (project.getReviewOnlyAutoAccept() || project.getAutoAcceptPMTask()
                || (isDitaQaTask && project.getAutoAcceptDitaQaTask())
                || (isQATask && project.getAutoAcceptQATask())
                || project.getAutoAcceptTrans())
        {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(TaskThread.KEY_ACTION, TaskThread.ACTION_AUTOACCEPT);
            TaskThread t = new TaskThread((TaskImpl) p_task, map);
            t.start();
        }
    }

    /**
     * Auto accept next task. Added for GBS-2461 & 2462.
     * 
     * @param p_task
     *            curren task
     * @param p_nextActivityName
     *            next activity name
     */
    public static void autoAcceptNextTask(Task p_task, String p_nextActivityName)
    {
        try
        {
            WorkflowTaskInstance wti = ServerProxy.getWorkflowServer()
                    .nextNodeInstances(p_task, p_nextActivityName, null);
            if (wti != null)
            {
                Task task = ServerProxy.getTaskManager().getTask(
                        wti.getTaskId());
                autoAcceptTask(task);
            }
        }
        catch (Exception e)
        {
            CATEGORY.error("autoAcceptNextTask Error", e);
        }
    }

    // Gets task URL for Email message.
    public static String getTaskURL(Task p_task)
    {
        StringBuffer url = new StringBuffer();
        url.append(ServerUtil.getServerURL());
        url.append("ControlServlet?linkName=detail&pageName=TK1&taskAction=getTask");
        url.append("&taskId=").append(p_task.getId());
        return url.toString();
    }

    /**
     * To accept a Task by a user.
     * 
     * @throws NamingException
     */
    public static void acceptTask(String p_userId, Task p_task)
            throws EnvoyServletException, NamingException
    {
        try
        {
            ServerProxy.getTaskManager().acceptTask(p_userId, p_task, false);
        }
        catch (TaskException te)
        {
            CATEGORY.debug(te);
            throw new EnvoyServletException(te.getMessageKey(),
                    te.getMessageArguments(), te, te.getPropertyFileName());
        }
        catch (RemoteException e)
        {
            CATEGORY.debug(e);
            throw new EnvoyServletException(GeneralException.EX_REMOTE, e);
        }
    }

    /**
     * To reject a Task by a user.
     */
    public static void rejectTask(String p_userId, Task p_task,
            String p_rejectComment) throws EnvoyServletException
    {

        try
        {
            ServerProxy.getTaskManager().rejectTask(p_userId, p_task,
                    p_rejectComment);
        }
        catch (TaskException te)
        {
            CATEGORY.debug(te);
            throw new EnvoyServletException(te.getMessageKey(),
                    te.getMessageArguments(), te, te.getPropertyFileName());
        }
        catch (GeneralException e)
        {
            CATEGORY.debug(e);
            throw new EnvoyServletException(e);
        }
        catch (RemoteException e)
        {
            CATEGORY.debug(e);
            throw new EnvoyServletException(GeneralException.EX_REMOTE, e);
        }

    }

    /**
     * To complete a Task by a user.
     */
    static void completeTask(String p_userId, Task p_task,
            String p_destinationArrow) throws EnvoyServletException
    {
        try
        {
            ServerProxy.getTaskManager().completeTask(p_userId, p_task,
                    p_destinationArrow, null);
        }
        catch (TaskException te)
        {
            CATEGORY.debug(te);
            throw new EnvoyServletException(te.getMessageKey(),
                    te.getMessageArguments(), te, te.getPropertyFileName());
        }
        catch (GeneralException e)
        {
            CATEGORY.debug(e);
            throw new EnvoyServletException(e);
        }
        catch (RemoteException e)
        {
            CATEGORY.debug(e);
            throw new EnvoyServletException(GeneralException.EX_REMOTE, e);
        }
    }

    /**
     * Get a task comment for a particular task/job.
     * 
     * @param p_commentId
     *            The Comment id
     */
    public static Comment getComment(HttpSession p_session, long p_commentId)
            throws EnvoyServletException
    {
        WorkObject wo = (WorkObject) retrieveObject(p_session,
                WebAppConstants.WORK_OBJECT);
        Comment comment = null;
        if (wo != null)
        {
            if (wo instanceof Job)
            {
                Job job = (Job) wo;
                comment = job.getJobComment(p_commentId);
            }
            else
            {
                Task task = (Task) wo;
                comment = task.getTaskComment(p_commentId);
            }
        }

        // if still null then possibly from another task
        if (comment == null)
        {
            try
            {
                comment = ServerProxy.getCommentManager().getCommentById(
                        p_commentId);
            }
            catch (Exception e)
            {
                CATEGORY.debug(e);
                throw new EnvoyServletException(GeneralException.EX_REMOTE, e);
            }
        }
        return comment;
    }

    /**
     * Update a task comment for a particular task.
     */
    public static Comment updateComment(long p_commentId, String p_userId,
            String p_comment) throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getCommentManager().updateComment(p_commentId,
                    p_userId, p_comment);
        }
        catch (GeneralException e)
        {
            CATEGORY.debug(e);
            throw new EnvoyServletException(e);
        }
        catch (RemoteException e)
        {
            CATEGORY.debug(e);
            throw new EnvoyServletException(GeneralException.EX_REMOTE, e);
        }
    }

    /**
     * Save a task comment for a particular task.
     */
    public static Comment saveComment(WorkObject p_wo, long p_taskId,
            String p_userId, String p_comment) throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getCommentManager().saveComment(p_wo, p_taskId,
                    p_userId, p_comment);
        }
        catch (GeneralException e)
        {
            CATEGORY.debug(e);
            throw new EnvoyServletException(e);
        }
        catch (RemoteException e)
        {
            CATEGORY.debug(e);
            throw new EnvoyServletException(GeneralException.EX_REMOTE, e);
        }
    }

    /**
     * Save a task comment for a particular task.
     */
    public static Comment saveComment(WorkObject p_wo, long p_taskId,
            String p_userId, String p_comment, Date p_date)
            throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getCommentManager().saveComment(p_wo, p_taskId,
                    p_userId, p_comment, p_date);
        }
        catch (GeneralException e)
        {
            CATEGORY.debug(e);
            throw new EnvoyServletException(e);
        }
        catch (RemoteException e)
        {
            CATEGORY.debug(e);
            throw new EnvoyServletException(GeneralException.EX_REMOTE, e);
        }
    }

    /**
     * To set the actual amount-of-work.
     */
    static void setActualAmountOfWork(Task p_task, Integer p_unitOfWork,
            String p_value) throws EnvoyServletException
    {
        try
        {
            ServerProxy.getCostingEngine().setActualAmountOfWork(
                    p_task.getId(), p_unitOfWork.intValue(),
                    Float.parseFloat(p_value));
        }

        catch (CostingException te)
        {
            CATEGORY.debug(te);
            throw new EnvoyServletException(te.getMessageKey(),
                    te.getMessageArguments(), te, te.getPropertyFileName());
        }
        catch (GeneralException ge)
        {
            CATEGORY.debug(ge);
            throw new EnvoyServletException(ge);
        }
        catch (RemoteException e)
        {
            CATEGORY.debug(e);
            throw new EnvoyServletException(GeneralException.EX_REMOTE, e);
        }
    }

    /**
     * Modify the user account.
     */
    static void modifyUserAccount(HttpSession p_session)
            throws EnvoyServletException
    {
        SessionManager sessionMgr = (SessionManager) p_session
                .getAttribute(WebAppConstants.SESSION_MANAGER);

        User user = (User) sessionMgr.getAttribute("myAccountUser");

        try
        {
            ServerProxy.getUserManager().modifyUser(user, user, null, null,
                    null);
            sessionMgr.setAttribute(WebAppConstants.USER, user);
        }
        catch (GeneralException e)
        {
            CATEGORY.debug(e);
            throw new EnvoyServletException(e);
        }
        catch (RemoteException e)
        {
            CATEGORY.debug(e);
            throw new EnvoyServletException(GeneralException.EX_REMOTE, e);
        }

    }

    public static Vector getUILocales() throws EnvoyServletException
    {
        Vector locales = null;

        try
        {
            locales = ServerProxy.getLocaleManager().getSupportedLocalesForUi();
        }
        catch (GeneralException e)
        {
            CATEGORY.debug(e);
            throw new EnvoyServletException(e);
        }
        catch (RemoteException e)
        {
            CATEGORY.debug(e);
            throw new EnvoyServletException(GeneralException.EX_REMOTE, e);
        }

        return locales;
    }

    /**
     * Get the user id of the logged in person.
     * 
     * @return The user id of the logged in person.
     */
    static String getUserId(HttpSession p_session)
    {
        User user = (User) getStoredObject(p_session, WebAppConstants.USER);
        return user.getUserId();
    }

    /**
     * Get the user who logged in.
     * 
     * @return The user object.
     */
    public static User getUser(HttpSession p_session)
    {
        return (User) getStoredObject(p_session, WebAppConstants.USER);
    }

    /**
     * Store the object in the session manager.
     */
    static void updateTaskInSession(HttpSession p_httpSession, String p_userId,
            long p_taskId) throws EnvoyServletException
    {
        updateTaskInSession(p_httpSession, p_userId, p_taskId,
                WorkflowConstants.TASK_ALL_STATES);
    }

    /**
     * Store the object in the session manager.
     */
    public static void updateTaskInSession(HttpSession p_httpSession,
            String p_userId, long p_taskId, int p_state)
            throws EnvoyServletException
    {
        Task task = getTask(p_userId, p_taskId, p_state);
        storeObject(p_httpSession, WebAppConstants.WORK_OBJECT, task);
    }

    /**
     * Store the object in the session manager.
     */
    public static void storeObject(HttpSession p_httpSession, String p_key,
            Object p_object)
    {
        SessionManager sessionMgr = (SessionManager) p_httpSession
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        sessionMgr.setAttribute(p_key, p_object);
    }

    /**
     * Retrieve an object from the session manager.
     */
    public static Object retrieveObject(HttpSession p_httpSession, String p_key)
    {
        SessionManager sessionMgr = (SessionManager) p_httpSession
                .getAttribute(WebAppConstants.SESSION_MANAGER);

        return sessionMgr == null ? null : sessionMgr.getAttribute(p_key);
    }

    /**
     * Retrieve the task object from the session manager and merge it against
     * the current Hibernate session.
     */
    public static TaskImpl retrieveMergeObject(HttpSession p_httpSession,
            String key)
    {
        TaskImpl obj = (TaskImpl) retrieveObject(p_httpSession, key);
        if (obj == null)
            return null;
        Session hibSession = HibernateUtil.getSession();
        TaskImpl newObj = (TaskImpl) hibSession.merge(obj);
        copyTaskValue(obj, newObj);

        // Update the session - merge() returns a new instance
        storeObject(p_httpSession, key, newObj);
        return newObj;
    }

    /**
     * Copy the value from p_sTask to p_tTask. Due TaskImpl include some
     * parameters not Mapping to Hibernate.
     * 
     * @param p_sTask
     *            source Task
     * @param p_tTask
     *            target Task
     */
    public static void copyTaskValue(TaskImpl p_sTask, TaskImpl p_tTask)
    {
        if (p_sTask.getProjectManagerName() != null
                && p_tTask.getProjectManagerName() == null)
        {
            p_tTask.setProjectManagerName(p_sTask.getProjectManagerName());
        }

        if (p_sTask.getWorkflowTask() != null
                && p_tTask.getWorkflowTask() == null)
        {
            p_tTask.setWorkflowTask(p_sTask.getWorkflowTask());
        }
    }

    /**
     * Clears the delayTimeTable date for the job export.
     */
    public static void clearDelayTimeTable(HttpSession session, String userId,
            String taskId)
    {
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        Hashtable delayTimeTable = (Hashtable) sessionMgr
                .getAttribute(WebAppConstants.TASK_COMPLETE_DELAY_TIME_TABLE);
        if (delayTimeTable != null)
        {
            String delayTimeKey = userId + String.valueOf(taskId);
            Object startTimeObj = delayTimeTable.get(delayTimeKey);
            if (startTimeObj != null)
            {
                delayTimeTable.remove(delayTimeKey);
            }
        }
    }

    /**
     * see
     * {@link #removeMRUtask(HttpServletRequest, HttpSession, String, HttpServletResponse)}
     * .
     */
    public static void removeMRUTask(HttpServletRequest request,
            HttpSession session, Task task, HttpServletResponse response)
    {
        String displayLocale = task.getSourceLocale().toString() + "->"
                + task.getTargetLocale().toString();
        String thisTask = displayLocale + ":" + task.getJobName() + ":"
                + task.getId() + ":" + task.getState();

        removeMRUtask(request, session, thisTask, response);
    }

    /**
     * Removes a task from the most recently used list.
     */
    public static void removeMRUtask(HttpServletRequest request,
            HttpSession session, String thisTask, HttpServletResponse response)
    {
        if (thisTask == null)
        {
            return;
        }

        String cookieName = JobSearchConstants.MRU_TASKS_COOKIE
                + session.getAttribute(WebAppConstants.USER_NAME).hashCode();
        StringBuffer newCookie = new StringBuffer();
        Cookie[] cookies = (Cookie[]) request.getCookies();
        if (cookies != null)
        {
            // don't need to match on state of task, so strip it off
            int idx = thisTask.lastIndexOf(":");
            String matchOn = thisTask.substring(0, idx);

            for (int i = 0; i < cookies.length; i++)
            {
                Cookie cookie = (Cookie) cookies[i];
                if (cookie.getName().equals(cookieName))
                {
                    String mruTaskStr = cookie.getValue();
                    mruTaskStr = URLDecoder.decode(mruTaskStr);
                    StringTokenizer st = new StringTokenizer(mruTaskStr, "|");
                    while (st.hasMoreTokens())
                    {
                        String value = st.nextToken();
                        if (!value.startsWith(matchOn))
                        {
                            newCookie.append("|");
                            newCookie.append(value);
                        }
                    }
                    break;
                }
            }

            session.setAttribute(JobSearchConstants.MRU_TASKS,
                    newCookie.toString());

            String value = newCookie.toString();
            value = URLEncoder.encode(value);
            try
            {
                response.addCookie(new Cookie(cookieName, value));
            }
            catch (Exception e)
            {
                response.addCookie(new Cookie(cookieName, ""));
            }
        }
    }

    /**
     * Update the session with this most recently used activity. It will become
     * the first in the list and all the rest moved down. Also check that it
     * wasn't already in the list. Only allow 3 in the list.
     */
    public static void updateMRUtask(HttpServletRequest request,
            HttpSession session, Task task, HttpServletResponse response,
            int taskState)
    {
        String cookieName = JobSearchConstants.MRU_TASKS_COOKIE
                + session.getAttribute(WebAppConstants.USER_NAME).hashCode();
        String displayLocale = task.getSourceLocale().toString() + "->"
                + task.getTargetLocale().toString();
        String jobName = task.getJobName();
        String thisTask = displayLocale + ":" + jobName + ":" + task.getId()
                + ":" + taskState;
        StringBuffer newCookie = new StringBuffer(thisTask);
        int count = 1;
        Cookie[] cookies = (Cookie[]) request.getCookies();
        if (cookies != null)
        {
            // don't need to match on state of task, so strip it off
            int idx = thisTask.lastIndexOf(":");
            String matchOn = thisTask.substring(0, idx);
            for (int i = 0; i < cookies.length; i++)
            {
                Cookie cookie = (Cookie) cookies[i];
                if (cookie.getName().equals(cookieName))
                {
                    String mruTaskStr = cookie.getValue();
                    mruTaskStr = URLDecoder.decode(mruTaskStr);
                    StringTokenizer st = new StringTokenizer(mruTaskStr, "|");
                    while (st.hasMoreTokens() && count < 3)
                    {
                        String value = st.nextToken();

                        if (!value.startsWith(matchOn))
                        {
                            newCookie.append("|");
                            newCookie.append(value);
                            count++;
                        }
                    }
                    break;
                }
            }
        }

        String sessionValue = newCookie.toString();
        session.setAttribute(JobSearchConstants.MRU_TASKS, sessionValue);

        String value = newCookie.toString();
        value = URLEncoder.encode(value);

        try
        {
            response.addCookie(new Cookie(cookieName, value));
        }
        catch (Exception e)
        {
            CATEGORY.error("Failed to add cookie value: " + value, e);
            response.addCookie(new Cookie(cookieName, ""));
        }
    }

    /**
     * Save user account basic information into the session.
     */
    public static void saveBasicInformation(HttpSession p_session,
            HttpServletRequest p_request) throws EnvoyServletException
    {
        SessionManager sessionMgr = (SessionManager) p_session
                .getAttribute(WebAppConstants.SESSION_MANAGER);

        User user = (User) sessionMgr.getAttribute("myAccountUser");
        String password = EditUtil.utf8ToUnicode(p_request
                .getParameter(WebAppConstants.USER_PASSWORD));
        String password1 = EditUtil.utf8ToUnicode(p_request
                .getParameter(WebAppConstants.USER_PASSWORD_CONFIRM));
        String firstName = EditUtil.utf8ToUnicode(p_request
                .getParameter(WebAppConstants.USER_FIRST_NAME));
        String lastName = EditUtil.utf8ToUnicode(p_request
                .getParameter(WebAppConstants.USER_LAST_NAME));
        String title = EditUtil.utf8ToUnicode(p_request.getParameter("title"));
        String companyName = EditUtil.utf8ToUnicode(p_request
                .getParameter("companyName"));
        if ("false".equalsIgnoreCase(p_request.getParameter("company")))
        {
            companyName = EditUtil.utf8ToUnicode(p_request
                    .getParameter("companies"));
        }

        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setTitle(title);
        user.setCompanyName(companyName);

        if (password != null && password.equals(password1)
                && (!password.trim().equals("")))
        {
            user.setPassword(password);
            user.setPasswordSet(true);
        }

        sessionMgr.setAttribute("myAccountUser", user);
    }

    /**
     * Get a reference to the object stored in the http session.
     */
    static Object getStoredObject(HttpSession p_httpSession, String p_key)
    {
        SessionManager sessionMgr = (SessionManager) p_httpSession
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        return sessionMgr.getAttribute(p_key);
    }

    // perform parsing the string parameter as a signed integer.
    public static int getInt(String p_value, int p_defaultValue)
    {
        int intVal = p_defaultValue;

        try
        {
            intVal = Integer.parseInt(p_value);
        }
        catch (NumberFormatException e)
        {
        }

        return intVal;
    }

    /**
     * Get the long type from a String
     * 
     * @return a long
     */
    public static long getLong(String p_value)
    {
        long num = 0;

        try
        {
            num = p_value == null ? 0 : Long.parseLong(p_value);
        }
        catch (NumberFormatException e)
        {
        }

        return num;
    }

    /**
     * Justify whether the task can been completed. If the task is uploading, it
     * can't been completed.
     */
    public static boolean canCompleteTask(Task p_task)
    {
        if (p_task != null && p_task.getIsUploading() == 'Y')
            return false;

        return true;
    }

    // Update the task status in Database.
    public static Task updateTaskStatus(long p_taskId, String p_status)
    {
        Task task = getTask(p_taskId);
        return updateTaskStatus(task, p_status, false);
    }

    public static Task updateTaskStatus(long p_taskId, String p_status,
            boolean isUploaded)
    {
        Task task = getTask(p_taskId);
        return updateTaskStatus(task, p_status, isUploaded);
    }

    /**
     * Update the task status in Database.(GBS-3229/1939)
     * 
     * @param p_task
     *            Task
     * @param p_status
     *            Task status(UPLOAD_IN_PROGRESS/UPLOAD_DONE)
     */
    public static Task updateTaskStatus(Task p_task, String p_status,
            boolean isUploaded)
    {
        if (p_task == null)
            return null;

        if (AmbassadorDwUpConstants.UPLOAD_IN_PROGRESS.equals(p_status))
        {
            p_task.setIsUploading('Y');
            HibernateUtil.update(p_task);
        }
        else if (AmbassadorDwUpConstants.UPLOAD_DONE.equals(p_status))
        {
            p_task.setIsUploading('N');
            if (isUploaded)
            {
                p_task.setIsReportUploaded(1);
            }
            HibernateUtil.update(p_task);
        }

        return p_task;
    }

    public static void updateTaskStatus(List<Task> p_tasks, String p_status,
            boolean isReportUploaded)
    {
        if (p_tasks != null && p_tasks.size() > 0)
        {
            for (Task task : p_tasks)
            {
                // Update task status (Upload Done)
                updateTaskStatus(task, p_status, isReportUploaded);
            }
        }
    }

    public static synchronized boolean installTaskIsUploading()
    {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            conn = SqlUtil.hireConnection();
            ps = conn
                    .prepareStatement(" UPDATE task_info SET is_uploading = 'N' WHERE is_uploading = 'Y' ");
            ps.execute();
        }
        catch (SQLException e)
        {
            CATEGORY.error("delAllReportsData error.", e);
            return false;
        }
        finally
        {
            SqlUtil.silentClose(rs);
            SqlUtil.silentClose(ps);
            SqlUtil.fireConnection(conn);
        }

        return true;
    }

    public static void updateReviewInfo(long p_taskId, String qualityAssessment,
            String marketSuitabilty)
    {
        Task p_task = getTask(p_taskId);
        if (p_task != null)
        {
            p_task.setQualityAssessment(qualityAssessment);
            p_task.setMarketSuitability(marketSuitabilty);
            HibernateUtil.update(p_task);
        }
    }
}
