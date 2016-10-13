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

package com.globalsight.everest.workflow;

// globalsight
import java.util.Arrays;
import java.util.Date;

import com.globalsight.calendar.UserFluxCalendar;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.TaskInfo;
import com.globalsight.everest.usermgr.UserManager;

/**
 * Class that filters the assignees for a particular activity/node. It may
 * filter them by the roles and the project, or by a particular preference.
 */
public class AssigneeFilter
{
    // --------------package methods-----------------------------
    // package methods only - to just be used by the Workflow package
    // in determining what assignees are valid to be used for
    // a particular role(s)

    /**
     * Return a comma delimted list of all the assignees
     */
    static String assigneeListAsString(String[] p_userIds)
    {
        StringBuilder users = new StringBuilder();
        if (p_userIds != null && p_userIds.length > 0)
        {
            users.append(p_userIds[0]);
            for (int i = 1; i < p_userIds.length; i++)
            {
                users.append(",");
                users.append(p_userIds[i]);
            }
        }
        return users.toString();
    }

    /**
     * Return the list of assignees as a comma-delimited list. The assignees are
     * filtered by the specified roles and project. It is the intersection of
     * the users in the roles and in the project.
     * 
     * @return String A comma-delimisted list of the user ids.
     */
    static String getAssigneeListAsString(String[] p_roleNames, long p_projectId)
            throws WorkflowException
    {
        String[] users = getAssigneeList(p_roleNames, p_projectId);
        return assigneeListAsString(users);
    }

    /**
     * Get a list of assignees (user ids) as an array of strings. This list is
     * provided based on the node's role preference.
     * 
     * @param p_pi
     *            - The process instance which the node belongs to.
     * @param p_node
     *            - The node which needs to have the assignees set.
     * @param p_userIds
     *            - The user ids based on the node's role(s).
     * @param p_baseDate
     *            - The based date of the action (start or advance task)
     * @param p_rolePreference
     *            - The preference of the node's role. These are specified in
     *            Worklflow Constants (i.e. FASTEST_ROLE_PREFERENCE).
     * @param p_taskInfo
     *            - A TaskInfo object containing estimated date info of a task.
     */
    static String[] getAssigneeListByRolePreference(
            WorkflowTaskInstance p_node, String[] p_userIds, Date p_baseDate,
            String p_rolePreference, TaskInfo p_taskInfo) throws Exception
    {
        if (p_node.getRoleType() || "" == p_rolePreference)
        {
            return p_userIds;
        }
        else
        {
            String assignees = getUserIdsByRolePreference(p_node, p_userIds,
                    p_baseDate, p_rolePreference, p_taskInfo);

            return assignees == null ? null : assignees.split(",");
        }
    }

    // ////////////////////////////////////////////////////////////////////
    // Private Methods
    // ////////////////////////////////////////////////////////////////////

    /*
     * Return the list of assignees as a comma-delimited list. This is a
     * filtered version of the role assignees (only the available ones are
     * included - users who can finish the task before or on the estimated
     * date).
     */
    private static String getAvailableResources(Date p_baseDate,
            Date p_estimatedCompletionDate, long p_duration, String[] p_userIds)
            throws Exception
    {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < p_userIds.length; i++)
        {
            UserFluxCalendar cal = ServerProxy.getCalendarManager()
                    .findUserCalendarByOwner(p_userIds[i]);
            Date dt = ServerProxy.getEventScheduler().determineDate(p_baseDate,
                    cal, p_duration);
            if (dt.compareTo(p_estimatedCompletionDate) > 0)
            {
                continue;
            }

            if (sb.length() > 0)
            {
                sb.append(",");
            }
            sb.append(p_userIds[i]);
        }
        return sb.length() > 0 ? sb.toString() : null;
    }

    /*
     * Return the list of assignees as a comma-delimited list. This is a
     * filtered version of the role assignees (only fastest ones are included).
     */
    private static String getFastestResources(Date p_baseDate, long p_duration,
            String[] p_userIds) throws Exception
    {
        Date bestDate = null;
        StringBuilder sb = null;

        // loop thru users and compute estimated completion date for each
        for (int i = 0; i < p_userIds.length; i++)
        {
            UserFluxCalendar cal = ServerProxy.getCalendarManager()
                    .findUserCalendarByOwner(p_userIds[i]);
            Date dt = ServerProxy.getEventScheduler().determineDate(p_baseDate,
                    cal, p_duration);

            if (bestDate == null || dt.before(bestDate))
            {
                bestDate = dt;
                sb = new StringBuilder();
                sb.append(p_userIds[i]);
            }
            else if (dt.equals(bestDate))
            {
                // add the user to list
                sb.append(",");
                sb.append(p_userIds[i]);
            }
        }

        return sb.toString();
    }

    /**
     * Get a list of assigneess (user ids) separated by comma. This list is
     * provided based on the node's role preference.
     */
    private static String getUserIdsByRolePreference(
            WorkflowTaskInstance p_node, String[] p_userIds, Date p_baseDate,
            String p_rolePreference, TaskInfo p_taskInfo) throws Exception
    {
        String assignees = null;
        if (p_rolePreference != null)
        {

            long totalDuration = p_node.getAcceptTime()
                    + p_node.getCompletedTime();

            if (WorkflowConstants.FASTEST_ROLE_PREFERENCE
                    .equals(p_rolePreference))
            {
                assignees = getFastestResources(p_baseDate, totalDuration,
                        p_userIds);
            }
            else if (WorkflowConstants.AVAILABLE_ROLE_PREFERENCE
                    .equals(p_rolePreference))
            {
                assignees = getAvailableResources(p_baseDate,
                        p_taskInfo.getCompleteByDate(), totalDuration,
                        p_userIds);
            }
        }
        return assignees;
    }

    /**
     * Gets the list of assignees that are common within the role(s) and
     * projecs. All returned users must be part of the specified role(s) AND in
     * the specified project.
     * 
     * @return An array of all the user ids that match the filter (roles and
     *         project).
     */
    public static String[] getAssigneeList(String[] p_roleNames,
            long p_projectId) throws WorkflowException
    {
        String[] finalUserIds = null;
        try
        {
            Project p = ServerProxy.getProjectHandler().getProjectById(
                    p_projectId);

            // retrieves all the users that are assigned to the
            // specific role(s) (container or user role)
            // and filtered by projects
            UserManager um = ServerProxy.getUserManager();

            if (p_roleNames == null || p_roleNames.length == 0)
            {
                finalUserIds = um.getUserIdsByFilter(null, p);
            }
            else if (p_roleNames.length == 1)
            {
                finalUserIds = um.getUserIdsByFilter(p_roleNames[0], p);
            }
            else
            {
                finalUserIds = um.getUserIdsFromRoles(p_roleNames, p);
            }

        }
        catch (Exception e)
        {
            String[] args =
            { Arrays.asList(p_roleNames).toString() };
            throw new WorkflowException(
                    WorkflowException.MSG_FAILED_TO_GET_ASSIGNEES_FOR_ROLE,
                    args, e);
        }
        return finalUserIds;
    }
}
