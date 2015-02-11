/**
 * Copyright 2009 Welocalize, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package com.globalsight.webservices;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.workflow.WorkflowTask;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.util.StringUtil;

/**
 * Helper for Ambassador.java.
 * 
 * @author YorkJin
 * @version 8.5.0.1
 * @since 2013-06-19
 */
public class AmbassadorHelper
{
    private static final Logger logger = Logger.getLogger(AmbassadorHelper.class);
    private static final String PROCESS_DEFINITION_IDS_PLACEHOLDER = "\uE000"
            + "_processDefinition_Ids_" + "\uE000";

    private static String QUERY_TASK_ASSIGNEE_SQL1 =
            "SELECT ti.workflow_id, ti.task_id, ti.name AS task_name, node.name_ AS node_name, node.processdefinition_ AS processdefinition "
            + " FROM workflow wf, task_info ti, jbpm_node node "
            + " WHERE wf.iflow_instance_id = ti.workflow_id "
            + " AND ti.task_id = node.id_ "
            + "AND node.class_ = 'K' "
            + "AND wf.job_id = ? "
            + "AND ti.user_id IS NULL";

    private static String QUERY_TASK_ASSIGNEE_SQL2 =
            "SELECT jd.id_ AS delegation_id, jd.processdefinition_ AS processDefinition, jd.configuration_ AS configuration FROM jbpm_delegation jd "
            + " WHERE jd.processdefinition_ IN (" + PROCESS_DEFINITION_IDS_PLACEHOLDER + ") "
            + " AND jd.classname_ = 'com.globalsight.everest.workflow.WorkflowAssignment'";

    /**
     * Get available users (assignees) for not accepted tasks in specified job.
     * 
     * @param jobId
     * @return Map<Long, String> : taskId as key, assignees as value.
     */
    protected static Map<Long, String> getTaskAssigneesByJob(long jobId)
    {
        Map<Long, String> availablTaskAssigneeMap = new HashMap<Long, String>();

        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            //1. Get all available tasks basic info.
            List<TaskAssignee> availableTasks = new ArrayList<TaskAssignee>();
            Set<Long> processDefinitions = new HashSet<Long>();

            con = DbUtil.getConnection();
            ps = con.prepareStatement(QUERY_TASK_ASSIGNEE_SQL1);
            ps.setLong(1, jobId);
            rs = ps.executeQuery();
            if (rs == null)
            {
                return availablTaskAssigneeMap;                
            }
            while (rs.next())
            {
                TaskAssignee ta = new TaskAssignee();
                ta.setWorkflowId(rs.getLong(1));
                ta.setTaskId(rs.getLong(2));
                ta.setTaskName(rs.getString(3));
                ta.setNodeName(rs.getString(4));
                long processDefinition = rs.getLong(5);
                ta.setProcessDefinition(processDefinition);

                processDefinitions.add(processDefinition);
                availableTasks.add(ta);
            }
            if (availableTasks.size() == 0 || processDefinitions.size() == 0)
            {
                return availablTaskAssigneeMap;                
            }

            //2. Get all configurations from "jbpm_delegation" for current job.
            List<TaskConfiguration> tcs = new ArrayList<TaskConfiguration>();
            String sql = QUERY_TASK_ASSIGNEE_SQL2.replace(
                    PROCESS_DEFINITION_IDS_PLACEHOLDER,
                    getProcessDefinitions(processDefinitions));
            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs == null)
            {
                return availablTaskAssigneeMap;                
            }
            while (rs.next())
            {
                TaskConfiguration tc = new TaskConfiguration();
                tc.setDelegationId(rs.getLong(1));
                tc.setProcessDefinition(rs.getLong(2));
                tc.setConfiguration(rs.getString(3));
                tcs.add(tc);
            }

            // 3. Get a map:: "[processDefinition]_[nodeName]" as key, assignee
            // as value, e.x. "6600_node_1_Translation1_2"->"myUserName".
            // This key equals "node name" from "jbpm_node" table.
            Map<String, String> tcMap = new HashMap<String, String>();
            for (TaskConfiguration tc : tcs)
            {
                String configuration = tc.getConfiguration();
                String sequence = getValueForSpecifiedTag(configuration,
                        "sequence");
                String activity = getValueForSpecifiedTag(configuration,
                        "activity");
                String roleName = getValueForSpecifiedTag(configuration,
                        "role_name");
                // form the key
                String key = tc.getProcessDefinition() + "_node_" + sequence + "_" + activity;
                tcMap.put(key, roleName);
            }

            //4. result
            for (TaskAssignee ta : availableTasks)
            {
                String value = WorkflowTask.DEFAULT_ROLE_NAME;
                String key = ta.getProcessDefinition() + "_" + ta.getNodeName();
                String assignees = tcMap.get(key);
                if (!StringUtil.isEmpty(assignees))
                {
                    StringBuilder userNames = new StringBuilder();
                    String[] availableUsers = assignees.split(",");
                    for (int i = 0; i < availableUsers.length; i++)
                    {
                        String userName = UserUtil
                                .getUserNameById(availableUsers[i].trim());
                        userNames.append(userName);
                        if (i < availableUsers.length - 1)
                        {
                            userNames.append(",");
                        }
                    }
                    value = userNames.toString();
                }

                availablTaskAssigneeMap.put(ta.getTaskId(), value);
            }
        }
        catch (Exception e)
        {
            logger.error("Error when get available tasks assignees.", e);
        }
        finally
        {
            DbUtil.silentClose(rs);
            DbUtil.silentClose(ps);
            DbUtil.silentReturnConnection(con);
        }

        return availablTaskAssigneeMap;
    }

    /**
     * For "jbpm_delegation" table "configuration_" column "sequence",
     * "activity" and "role_name" tags.
     * 
     * @param p_str
     *            -- "configuration_" column content.
     * @param p_tagName
     *            -- "sequence", "activity", "role_name".
     * @return String
     */
    private static String getValueForSpecifiedTag(String p_str, String p_tagName)
    {
        if (StringUtil.isEmpty(p_str) || StringUtil.isEmpty(p_tagName))
        {
            return "";
        }

        String result = "";
        try
        {
            int index = p_str.indexOf("</" + p_tagName + ">");
            if (index > -1)
            {
                result = p_str.substring(0, index);
                index = result.lastIndexOf(">");
                result = result.substring(index + 1);
            }            
        }
        catch (Exception ignore)
        {
        }

        return result;
    }

    /**
     * Get a process definitions string like "1,2,3".
     */
    private static String getProcessDefinitions(Set<Long> processDefinitions)
    {
        StringBuilder processDefs = new StringBuilder();
        int count = 0;
        for (Long pd : processDefinitions)
        {
            processDefs.append(pd);
            count++;
            if (count < processDefinitions.size())
            {
                processDefs.append(",");
            }
        }

        return processDefs.toString();
    }

    /**
     * Help class for "getTaskAssigneesByJob(jobId)" method.
     */
    static class TaskAssignee
    {
        private long workflowId = -1;
        private long taskId = -1;
        private String taskName = null;
        private String nodeName = null;
        private long processDefinition = -1;

        public long getWorkflowId()
        {
            return workflowId;
        }

        public void setWorkflowId(long workflowId)
        {
            this.workflowId = workflowId;
        }

        public long getTaskId()
        {
            return taskId;
        }

        public void setTaskId(long taskId)
        {
            this.taskId = taskId;
        }

        public String getTaskName()
        {
            return taskName;
        }

        public void setTaskName(String taskName)
        {
            this.taskName = taskName;
        }

        public String getNodeName()
        {
            return nodeName;
        }

        public void setNodeName(String nodeName)
        {
            this.nodeName = nodeName;
        }

        public long getProcessDefinition()
        {
            return processDefinition;
        }

        public void setProcessDefinition(long processDefinition)
        {
            this.processDefinition = processDefinition;
        }
    }

    /**
     * Help class for "getTaskAssigneesByJob(jobId)" method.
     */
    static class TaskConfiguration
    {
        private long delegationId = -1;
        private long processDefinition = -1;
        private String configuration = null;

        public long getDelegationId()
        {
            return delegationId;
        }

        public void setDelegationId(long delegationId)
        {
            this.delegationId = delegationId;
        }

        public long getProcessDefinition()
        {
            return processDefinition;
        }

        public void setProcessDefinition(long processDefinition)
        {
            this.processDefinition = processDefinition;
        }

        public String getConfiguration()
        {
            return configuration;
        }

        public void setConfiguration(String configuration)
        {
            this.configuration = configuration;
        }
    }
}
