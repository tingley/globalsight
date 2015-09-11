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
package com.globalsight.everest.taskmanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jbpm.taskmgmt.exe.PooledActor;
import org.jbpm.taskmgmt.exe.TaskInstance;

import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.diplomat.util.database.ConnectionPoolException;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.webapp.pagehandler.tasks.TaskSearchUtil;
import com.globalsight.everest.webapp.pagehandler.tasks.TaskVo;
import com.globalsight.everest.workflow.WorkflowJbpmUtil;
import com.globalsight.everest.workflow.WorkflowTaskInstance;
import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * This class is used for updating the TASK_INTERIM table by inserting and
 * deleting records for the activity dashboard quick view.
 * 
 */
public class TaskInterimPersistenceAccessor
{
    private static final Logger CATEGORY = Logger
            .getLogger(TaskInterimPersistenceAccessor.class.getName());

    private static final String SQL_INSERT_ACTIVITY = "insert into TASK_INTERIM values(null,?,?,?,?)";

    private static final String SQL_UPDATE_ACTIVITY = "update TASK_INTERIM set STATE='ACCEPTED' where TASK_ID=? and USER_ID=?";

    private static final String SQL_DELETE_AVAILABLE_ACTIVITY = "delete from TASK_INTERIM where TASK_ID=? and USER_ID not in (?,?) and STATE='ACTIVE'";

    private static final String SQL_DELETE_ACCEPTED_ACTIVITY = "delete from TASK_INTERIM where TASK_ID=? and USER_ID in (?,?) and STATE='ACCEPTED'";

    private static final String SQL_DELETE_DIRTY_ACTIVITY = "delete from TASK_INTERIM where USER_ID=?";

    private static final String SQL_DELETE_TRASHED_ACTIVITY = "delete from TASK_INTERIM where TASK_ID=?";

    private static final String SQL_QUERY_TRIGGERED = "select ID from TASK_INTERIM where STATE='TRIGGERED' and USER_ID=?";

    private static final String SQL_QUERY_ACTIVITY_COUNT = "select count(*) from TASK_INTERIM where STATE=? and USER_ID=?";

    private static final String SQL_QUERY_ACTIVITY = "select ID from TASK_INTERIM where TASK_ID=?";

    // 'TRIGGERED' state indicates this user's tasks have been moved to
    // TASK_INTERIM table and next time when they login, the dashboard data is
    // fetched from TASK_INTERIM table.
    private static final String SQL_INSERT_TRIGGERED = "insert into TASK_INTERIM values(null,null,null,'TRIGGERED',?)";

    /**
     * Updates the activity to 'ACCEPTED' state for the assignee who has
     * accepted the activity (and the pm) and deletes the activities with
     * 'ACTIVE' state from other users.
     * 
     * @param taskInstance
     *            The task instance {@link TaskInstance}.
     */
    public static void acceptInterimActivity(TaskInstance taskInstance)
    {
        Connection cnn = null;
        PreparedStatement ps = null;
        long taskId = taskInstance.getTask().getTaskNode().getId();
        String actor = taskInstance.getActorId();
        String pm = taskInstance.getDescription();
        try
        {
            cnn = ConnectionPool.getConnection();
            cnn.setAutoCommit(false);
            ps = cnn.prepareStatement(SQL_UPDATE_ACTIVITY);
            ps.setLong(1, taskId);
            ps.setString(2, actor);
            ps.executeUpdate();
            if (!actor.equals(pm))
            {
                ps.setString(2, pm);
                ps.executeUpdate();
            }
            // delete the activities with 'ACTIVE' state from other users
            // (excluding the pm)
            deleteAvailableActivity(cnn, taskId, actor, pm);
            cnn.commit();
        }
        catch (Exception e)
        {
            CATEGORY.error("Failed to update activity with TASK_ID " + taskId,
                    e);
        }
        finally
        {
            ConnectionPool.silentClose(ps);
            ConnectionPool.silentReturnConnection(cnn);
        }
    }

    /**
     * Cancels the active activities from the workflow(s) being canceled.
     * 
     * @param taskList
     *            The list of active activities.
     */
    public static void cancelInterimActivities(List<?> taskList)
    {
        Connection connection = null;
        try
        {
            connection = ConnectionPool.getConnection();
            for (int i = 0; i < taskList.size(); i++)
            {
                Object[] tasks = (Object[]) taskList.get(i);
                for (Object task : tasks)
                {
                    try
                    {
                        skipInterimActivity(
                                ((WorkflowTaskInstance) task).getTaskId(),
                                connection);
                    }
                    catch (Exception e)
                    {
                        CATEGORY.error(
                                "Error found in cancelInterimActivities.", e);
                    }
                }
            }
        }
        catch (ConnectionPoolException e)
        {
            CATEGORY.error(
                    "Fail to get connection in cancelInterimActivities.", e);
        }
        finally
        {
            if (connection != null)
                ConnectionPool.silentReturnConnection(connection);
        }

    }

    /**
     * Deletes the activity records belonging to the given user.
     * 
     * @param userId
     *            The user that is being removed.
     */
    public static void deleteInterimUser(String userId)
    {
        try
        {
            deleteDirtyActivities(null, userId);
        }
        catch (Exception e)
        {
            // do nothing
        }
    }

    /**
     * Adds activity for the assignees (and the pm) to TASK_INTERIM table with
     * 'ACTIVE' state.
     * 
     * @param taskInstance
     *            The task instance {@link TaskInstance}.
     * 
     */
    public static void dispatchInterimActivity(TaskInstance taskInstance)
    {
        String activityName = WorkflowJbpmUtil.getActivityName(taskInstance
                .getName());
        Connection cnn = null;
        PreparedStatement ps = null;
        long taskId = taskInstance.getTask().getTaskNode().getId();
        Set<?> pooledActors = taskInstance.getPooledActors();
        Set<?> actors = PooledActor.extractActorIds(pooledActors);
        String pm = taskInstance.getDescription();
        try
        {
            cnn = ConnectionPool.getConnection();
            cnn.setAutoCommit(false);
            ps = cnn.prepareStatement(SQL_INSERT_ACTIVITY);
            ps.setLong(1, taskId);
            ps.setString(2, activityName);
            ps.setString(3, Task.STATE_ACTIVE_STR);
            for (Object assignee : actors)
            {
                ps.setString(4, (String) assignee);
                ps.executeUpdate();
                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("Added activity " + activityName
                            + " to user " + assignee + " with TASK_ID "
                            + taskId + " to TASK_INTERIM table.");
                }
            }
            if (!actors.contains(pm))
            {
                // add the activity to pm if he is not in the assignee list
                ps.setString(4, pm);
                ps.executeUpdate();
                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("Added activity " + activityName + " to pm "
                            + pm + " with TASK_ID " + taskId
                            + " to TASK_INTERIM table.");
                }
            }
            cnn.commit();
        }
        catch (Exception e)
        {
            CATEGORY.error("Failed to add activity " + activityName
                    + " with TASK_ID " + taskId + " to TASK_INTERIM table.", e);
        }
        finally
        {
            ConnectionPool.silentClose(ps);
            ConnectionPool.silentReturnConnection(cnn);
        }
    }

    /**
     * Deletes the activity with 'ACCEPETED' state from the translator (and the
     * pm) when this activity is completed.
     * 
     * @param taskInstance
     *            The task instance {@link TaskInstance}.
     */
    public static void endInterimActivity(TaskInstance taskInstance)
    {
        Connection cnn = null;
        PreparedStatement ps = null;
        long taskId = taskInstance.getTask().getTaskNode().getId();
        String actor = taskInstance.getActorId();
        String pm = taskInstance.getDescription();
        try
        {
            cnn = ConnectionPool.getConnection();
            cnn.setAutoCommit(false);
            ps = cnn.prepareStatement(SQL_DELETE_ACCEPTED_ACTIVITY);
            ps.setLong(1, taskId);
            ps.setString(2, actor);
            ps.setString(3, pm);
            ps.executeUpdate();
            cnn.commit();
        }
        catch (Exception e)
        {
            CATEGORY.error("Failed to delete activity with TASK_ID " + taskId,
                    e);
        }
        finally
        {
            ConnectionPool.silentClose(ps);
            ConnectionPool.silentReturnConnection(cnn);
        }
    }

    /**
     * Gets the task count for activity dashboard for the given user.
     * 
     * @param userId
     *            The user who is logging in.
     * 
     * @return a Map<String, Long> of status(Available|In Progress) and task
     *         count key value pair.
     * 
     */
    public static Map<String, Long> getTasksForDashboard(String userId)
    {
        Connection cnn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Map<String, Long> map = new HashMap<String, Long>();
        try
        {
            cnn = ConnectionPool.getConnection();
            ps = cnn.prepareStatement(SQL_QUERY_ACTIVITY_COUNT);
            // search available activity count.
            ps.setString(1, Task.STATE_ACTIVE_STR);
            ps.setString(2, userId);
            rs = ps.executeQuery();
            if (rs.next())
            {
                map.put(Task.STATE_ACTIVE_STR, new Long(rs.getLong(1)));
            }
            // search in progress activity count.
            ps.setString(1, Task.STATE_ACCEPTED_STR);
            rs = ps.executeQuery();
            if (rs.next())
            {
                map.put(Task.STATE_ACCEPTED_STR, new Long(rs.getLong(1)));
            }
        }
        catch (Exception e)
        {
            CATEGORY.error("Failed to get tasks for user " + userId, e);
        }
        finally
        {
            ConnectionPool.silentClose(rs);
            ConnectionPool.silentClose(ps);
            ConnectionPool.silentReturnConnection(cnn);
        }

        return map;
    }

    /**
     * Moves the user's existing available and in progress tasks to TASK_INTERIM
     * table. Used for data migration when users login GS first time after this
     * change is applied.
     * 
     * @param cnn
     *            The database connection.
     * @param userId
     *            The user who is logging in the first time.
     */
    public static void initializeUserTasks(String userId)
    {
        TaskSearchParameters tsp = new TaskSearchParameters();
        List<TaskVo> availableTasks = null;
        List<TaskVo> inprogressTasks = null;
        User u = null;
        try
        {
            u = ServerProxy.getUserManager().getUser(userId);
            tsp.setUser(u);
        }
        catch (Exception e)
        {
            CATEGORY.error("Failed to get user " + userId + " from LDAP.", e);
        }
        // search available tasks.
        tsp.setActivityState(new Integer(Task.STATE_ACTIVE));
        availableTasks = TaskSearchUtil.search(u, tsp);
        tsp.setActivityState(new Integer(Task.STATE_ACCEPTED));
        inprogressTasks = TaskSearchUtil.search(u, tsp);

        // transfer data into TASK_INTERIM table.
        Connection cnn = null;
        try
        {
            cnn = ConnectionPool.getConnection();
            boolean autoCommit = cnn.getAutoCommit();
            cnn.setAutoCommit(false);
            deleteDirtyActivities(cnn, userId);
            transferActivities(cnn, availableTasks, inprogressTasks, userId);
            setTriggeredMark(cnn, userId);
            cnn.commit();
            cnn.setAutoCommit(autoCommit);
        }
        catch (Exception e)
        {
            try
            {
                if (cnn != null)
                {
                    cnn.rollback();
                }
            }
            catch (SQLException e1)
            {
                CATEGORY.error("Could not roll back ", e1);
            }
        }
        finally
        {
            ConnectionPool.silentReturnConnection(cnn);
        }
    }

    /**
     * Judges if this user has been triggered with the interim table.
     * 
     * @param userId
     *            The user.
     * 
     * @return true or false.
     */
    public static boolean isTriggered(String userId)
    {
        Connection cnn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            cnn = ConnectionPool.getConnection();
            ps = cnn.prepareStatement(SQL_QUERY_TRIGGERED);
            ps.setString(1, userId);
            rs = ps.executeQuery();
            if (!rs.next())
            {
                // indicates the interim table has not been used for this user.
                return false;
            }
        }
        catch (Exception e)
        {
            CATEGORY.error("Failed to query 'TRIGGERED' state for user "
                    + userId, e);
        }
        finally
        {
            ConnectionPool.silentClose(rs);
            ConnectionPool.silentClose(ps);
            ConnectionPool.silentReturnConnection(cnn);
        }
        return true;
    }

    /**
     * Checks and reassigns the activity to new assignees (and the pm).
     * 
     * @param taskInstance
     *            The task instance {@link TaskInstance}.
     */
    public static void reassignInterimActivity(TaskInstance taskInstance)
    {
        Connection cnn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        long taskId = taskInstance.getTask().getTaskNode().getId();
        try
        {
            cnn = ConnectionPool.getConnection();
            ps = cnn.prepareStatement(SQL_QUERY_ACTIVITY);
            ps.setLong(1, taskId);
            rs = ps.executeQuery();
            if (!rs.next())
            {
                // indicates this activity has not been dispatched
                // ignore the actions below
                return;
            }
        }
        catch (Exception e)
        {
            CATEGORY.error("Failed to query the activity with TASK_ID "
                    + taskId, e);
        }
        finally
        {
            ConnectionPool.silentClose(rs);
            ConnectionPool.silentClose(ps);
            ConnectionPool.silentReturnConnection(cnn);
        }
        // same action as reject if this activity has been dispatched or
        // accepted
        rejectInterimActivity(taskInstance);
    }

    /**
     * Refreshes the activities for the given users.
     * 
     * @param userIds
     *            A group of users that need to be updated.
     */
    public static void refreshActivities(String[] userIds)
    {
        for (String userId : userIds)
        {
            initializeUserTasks(userId);
        }
    }

    /**
     * Deletes the activities rejected by the user and adds them to the other
     * assignees or the pm.
     * 
     * @param taskInstance
     *            The task instance {@link TaskInstance}.
     */
    public static void rejectInterimActivity(TaskInstance taskInstance)
    {
        Connection connection = null;
        try
        {
            connection = ConnectionPool.getConnection();

            // skip the activities by the task id first
            long taskId = taskInstance.getTask().getTaskNode().getId();
            skipInterimActivity(taskId, connection);
            // dispatch the activity to the new assignees in the task instance
            dispatchInterimActivity(taskInstance);
        }
        catch (Exception e)
        {
            CATEGORY.error("Error found in rejectInterimActivity.", e);
        }
        finally
        {
            ConnectionPool.silentReturnConnection(connection);
        }
    }

    /**
     * Deletes the activity available from the users when it is skipped.
     * 
     * @param taskId
     *            The task id, corresponding to TASK_ID column in TASK_INFO
     *            table.
     */
    public static void skipInterimActivity(long taskId, Connection connection)
    {
        PreparedStatement ps = null;
        boolean returnInternalConnection = false;
        try
        {
            if (connection == null)
            {
                connection = ConnectionPool.getConnection();
                returnInternalConnection = true;
            }
            connection.setAutoCommit(false);
            ps = connection.prepareStatement(SQL_DELETE_TRASHED_ACTIVITY);
            ps.setLong(1, taskId);
            ps.executeUpdate();
            connection.commit();
        }
        catch (Exception e)
        {
            CATEGORY.error("Failed to delete activity with TASK_ID " + taskId,
                    e);
        }
        finally
        {
            ConnectionPool.silentClose(ps);
            if (returnInternalConnection)
            {
                ConnectionPool.silentReturnConnection(connection);
            }
        }
    }

    /**
     * Deletes the activities with 'ACTIVE' state from other users excluding the
     * given translator.
     * 
     * @param cnn
     *            The database connection.
     * @param taskId
     *            The task id, corresponding to TASK_ID column in TASK_INFO
     *            table.
     * @param actor
     *            The user who accepts the activity.
     * @param pm
     *            The project manager.
     */
    private static void deleteAvailableActivity(Connection cnn, long taskId,
            String actor, String pm)
    {
        PreparedStatement ps = null;
        try
        {
            ps = cnn.prepareStatement(SQL_DELETE_AVAILABLE_ACTIVITY);
            ps.setLong(1, taskId);
            ps.setString(2, actor);
            ps.setString(3, pm);
            ps.executeUpdate();
        }
        catch (Exception e)
        {
            CATEGORY.error(
                    "Failed to delete activities with TASK_ID " + taskId, e);
        }
        finally
        {
            ConnectionPool.silentClose(ps);
        }
    }

    /**
     * Deletes the dirty activities that were inserted before setting
     * 'TRIGGERED'.
     * 
     * @param cnn
     *            The database connection.
     * @param userId
     *            The user who is logging in the first time.
     */
    private static void deleteDirtyActivities(Connection cnn, String userId)
            throws Exception
    {
        Connection c = cnn;
        PreparedStatement ps = null;
        try
        {
            if (c == null)
            {
                c = ConnectionPool.getConnection();
            }
            ps = c.prepareStatement(SQL_DELETE_DIRTY_ACTIVITY);
            ps.setString(1, userId);
            ps.executeUpdate();
        }
        catch (Exception e)
        {
            CATEGORY.error("Failed to delete the dirty activities from user "
                    + userId, e);
            throw e;
        }
        finally
        {
            ConnectionPool.silentClose(ps);
            if (cnn == null)
            {
                // this is request from deleteInterimUser() which needs to close
                // connection right away
                ConnectionPool.silentReturnConnection(c);
            }
        }
    }

    /**
     * Sets 'TRIGGERED' state for the given user.
     * 
     * @param cnn
     *            The database connection.
     * @param userId
     *            The user who is logging in the first time.
     * 
     * @throws Exception
     */
    private static void setTriggeredMark(Connection cnn, String userId)
            throws Exception
    {
        PreparedStatement ps = null;
        try
        {
            ps = cnn.prepareStatement(SQL_INSERT_TRIGGERED);
            ps.setString(1, userId);
            ps.executeUpdate();
        }
        catch (Exception e)
        {
            CATEGORY.error("Failed to set the 'TRIGGERED' mark for user "
                    + userId, e);
            throw e;
        }
        finally
        {
            ConnectionPool.silentClose(ps);
        }
    }

    /**
     * Transfers user's available and in progress activities to TASK_INTERIM
     * table.
     * 
     * @param cnn
     *            The database connection.
     * @param availableTasks
     *            The available activities.
     * @param inprogressTasks
     *            The in progress activities.
     * @param userId
     *            The user.
     * 
     * @throws Exception
     */
    private static void transferActivities(Connection cnn,
            List<TaskVo> availableTasks, List<TaskVo> inprogressTasks, String userId)
            throws Exception
    {
        PreparedStatement ps = null;
        try
        {
            ps = cnn.prepareStatement(SQL_INSERT_ACTIVITY);
            for (int i = 0; i < availableTasks.size(); i++)
            {
            	TaskVo task = availableTasks.get(i);
            	TaskImpl t = HibernateUtil.get(TaskImpl.class, task.getTaskId());
            	
                ps.setLong(1, t.getId());
                ps.setString(2, t.getTaskName());
                ps.setString(3, Task.STATE_ACTIVE_STR);
                ps.setString(4, userId);
                ps.executeUpdate();
            }
            
            for (int i = 0; i < inprogressTasks.size(); i++)
            {
            	TaskVo task = inprogressTasks.get(i);
            	TaskImpl t = HibernateUtil.get(TaskImpl.class, task.getTaskId());
            	
                ps.setLong(1, t.getId());
                ps.setString(2, t.getTaskName());
                ps.setString(3, Task.STATE_ACCEPTED_STR);
                ps.setString(4, userId);
                ps.executeUpdate();
            }
        }
        catch (Exception e)
        {
            CATEGORY.error("Failed to transfer activities for user " + userId
                    + " to TASK_INTERIM table.", e);
            throw e;
        }
        finally
        {
            ConnectionPool.silentClose(ps);
        }
    }
}
