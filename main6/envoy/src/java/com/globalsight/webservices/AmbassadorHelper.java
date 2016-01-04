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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jbpm.JbpmContext;
import org.json.JSONObject;

import com.globalsight.everest.company.Company;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.edit.offline.OEMProcessStatus;
import com.globalsight.everest.edit.offline.OfflineEditManager;
import com.globalsight.everest.edit.offline.download.DownloadParams;
import com.globalsight.everest.foundation.ContainerRole;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.foundation.LocalePair;
import com.globalsight.everest.foundation.Role;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.foundation.UserRole;
import com.globalsight.everest.jobhandler.JobHandlerWLRemote;
import com.globalsight.everest.localemgr.LocaleManagerException;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionGroup;
import com.globalsight.everest.permission.PermissionManager;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.projecthandler.ProjectHandlerWLRemote;
import com.globalsight.everest.projecthandler.ProjectImpl;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.usermgr.UserInfo;
import com.globalsight.everest.usermgr.UserManagerWLRemote;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.administration.permission.PermissionHelper;
import com.globalsight.everest.webapp.pagehandler.administration.reports.generator.PostReviewQAReportGenerator;
import com.globalsight.everest.webapp.pagehandler.administration.reports.generator.ReportGenerator;
import com.globalsight.everest.webapp.pagehandler.administration.reports.generator.ReviewersCommentsReportGenerator;
import com.globalsight.everest.webapp.pagehandler.administration.reports.generator.ReviewersCommentsSimpleReportGenerator;
import com.globalsight.everest.webapp.pagehandler.administration.reports.generator.TranslationVerificationReportGenerator;
import com.globalsight.everest.webapp.pagehandler.administration.reports.generator.TranslationsEditReportGenerator;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.webapp.pagehandler.offline.OfflineConstants;
import com.globalsight.everest.webapp.pagehandler.offline.download.SendDownloadFileHelper;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.everest.workflow.ConditionNodeTargetInfo;
import com.globalsight.everest.workflow.WorkflowConfiguration;
import com.globalsight.everest.workflow.WorkflowInstance;
import com.globalsight.everest.workflow.WorkflowProcessAdapter;
import com.globalsight.everest.workflow.WorkflowTask;
import com.globalsight.everest.workflow.WorkflowTaskInstance;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.Assert;
import com.globalsight.util.ExcelUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.RegexUtil;
import com.globalsight.util.StringUtil;
import com.globalsight.util.XmlParser;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * Helper for Ambassador.java.
 * 
 * @author YorkJin
 * @version 8.5.0.1
 * @since 2013-06-19
 */
public class AmbassadorHelper extends JsonTypeWebService
{
    private static final Logger logger = Logger.getLogger(AmbassadorHelper.class);

    public static final String GET_WORK_OFFLINE_FILES = "getWorkOfflineFiles";
    public static final String UPLOAD_WORK_OFFLINE_FILES = "uploadWorkOfflineFiles";
    public static final String IMPORT_WORK_OFFLINE_FILES = "importWorkOfflineFiles";

    //Error constants used in createUser()/modifyUser()
    private final int UNKNOWN_ERROR = -1;
    private final int SUCCESS = 0;
    private final int INVALID_ACCESS_TOKEN = 1;
    private final int INVALID_USER_ID = 2;
    private final int USER_EXISTS = 4;
    private final int USER_NOT_EXIST = 5;
    private final int NOT_IN_SAME_COMPANY = 6;
    private final int INVALID_PASSWORD = 7;
    private final int INVALID_FIRST_NAME = 8;
    private final int INVALID_LAST_NAME = 9;
    private final int INVALID_EMAIL_ADDRESS = 10;
    private final int INVALID_PERMISSION_GROUPS = 11;
    private final int INVALID_PROJECTS = 12;
    private final int INVALID_ROLES = 13;
    private final int INVALD_PERMISSION = 14;
    private String SEPARATOR = "_";
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


	private static String QUERY_TASK_ISSKIP_SQL = "SELECT instance.id_ FROM jbpm_task jt, jbpm_taskinstance instance"
			+ " WHERE jt.id_ = instance.task_ " + " AND jt.tasknode_ = ";

	private static String QUERY_PROCESSDEFINITION_SQL = "SELECT DISTINCT node.processdefinition_ FROM workflow wf, task_info ti, jbpm_node node "
			+ "WHERE wf.job_id = ? AND wf.iflow_instance_id = ti.workflow_id AND ti.task_id = node.id_ ";

	private static String QUERY_JBPM_NODE_SQL = "SELECT node.ID_,node.CLASS_,node.NAME_,node.PROCESSDEFINITION_  "
			+ "FROM jbpm_node node WHERE node.processdefinition_ IN ";

	private static String QUERY_JBPM_TRANSITION_SQL = "SELECT tran.NAME_,tran.FROM_,tran.TO_,tran.PROCESSDEFINITION_ " 
			+ "FROM jbpm_transition tran WHERE tran.processdefinition_ IN ";

	private static String QUERY_WORKFLOW_NAME_SQL = "SELECT DISTINCT wf.IFLOW_INSTANCE_ID, def.name_ AS workflowName "
			+ "FROM workflow wf, task_info ti, jbpm_node node, jbpm_processdefinition def WHERE wf.iflow_instance_id = ti.workflow_id AND ti.task_id = node.id_ AND node.PROCESSDEFINITION_ = def.id_ AND wf.job_id = ?";
	
    protected static boolean isSkippedTask(long taskId)
    {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        JbpmContext ctx = WorkflowConfiguration.getInstance().getJbpmContext();
        boolean isSkipped = false;
        
        try
        {
        	con = DbUtil.getConnection();
            ps = con.prepareStatement(QUERY_TASK_ISSKIP_SQL +  taskId + " order by instance.id_ desc");
            rs = ps.executeQuery(); 
            while (rs.next())
            {
            	long tiId = rs.getLong(1);
				String hql = "from JbpmVariable j where j.name='skip' and j.taskInstance.id = :tiId";
				Session dbSession = ctx.getSession();
				Query query = dbSession.createQuery(hql);
				query.setParameter("tiId", tiId);
				List skipped = query.list();
				if (skipped != null && skipped.size() > 0)
				{
					isSkipped = true;
				}
				break;
            }
        }
        catch(Exception e)
        {
            logger.error("Error when determine skippedTasks.", e);
        }
        finally
        {
            DbUtil.silentClose(rs);
            DbUtil.silentClose(ps);
            DbUtil.silentReturnConnection(con);
            ctx.close();
        }

        return isSkipped;
    }
    
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

	protected static List<Long> getProcessdefintion(long jobId)
	{
		List<Long> returnList = new ArrayList<Long>();
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try
		{
			con = DbUtil.getConnection();
			ps = con.prepareStatement(QUERY_PROCESSDEFINITION_SQL);
			ps.setLong(1, jobId);
			rs = ps.executeQuery();

			if (rs == null)
			{
				return returnList;
			}
			while (rs.next())
			{
				returnList.add(rs.getLong(1));
			}
		}
		catch (Exception e)
		{
			logger.error("Error when get processdefintion.", e);
		}
		finally
		{
			DbUtil.silentClose(rs);
			DbUtil.silentClose(ps);
			DbUtil.silentReturnConnection(con);
		}
		return returnList;
	}
	
	protected static Map<Long,TaskJbpmNode> getTaskJbpmNode(
			List<Long> processdefinitionList)
	{
		Map<Long, TaskJbpmNode> jbpmNodeMap = new HashMap<Long, TaskJbpmNode>();
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try
		{
			String sql = QUERY_JBPM_NODE_SQL
					+ toInClause(processdefinitionList);
			con = DbUtil.getConnection();
			ps = con.prepareStatement(sql);
			rs = ps.executeQuery();

			if (rs == null)
			{
				return null;
			}
			while (rs.next())
			{
				TaskJbpmNode node = new TaskJbpmNode();
				node.setNodeId(rs.getLong(1));
				node.setNodeClass(rs.getString(2));
				node.setNodeName(rs.getString(3));
				node.setProcessDefinition(rs.getLong(4));
				jbpmNodeMap.put(node.getNodeId(), node);
			}
		}
		catch (Exception e)
		{
			logger.error("Error when get processdefintion.", e);
		}
		finally
		{
			DbUtil.silentClose(rs);
			DbUtil.silentClose(ps);
			DbUtil.silentReturnConnection(con);
		}

		return jbpmNodeMap;
	}
	
	protected static Map<Long,List<TaskJbpmTransition>> getTaskJbpmTransition(
			List<Long> processdefinitionList)
	{
		List<TaskJbpmTransition> jbpmTranList = new ArrayList<TaskJbpmTransition>();
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try
		{
			String sql = QUERY_JBPM_TRANSITION_SQL
					+ toInClause(processdefinitionList);
			con = DbUtil.getConnection();
			ps = con.prepareStatement(sql);
			rs = ps.executeQuery();

			if (rs == null)
			{
				return null;
			}
			while (rs.next())
			{
				TaskJbpmTransition tran = new TaskJbpmTransition();
				tran.setName(rs.getString(1));
				tran.setFrom(rs.getLong(2));
				tran.setTo(rs.getLong(3));
				tran.setProcessDefinition(rs.getLong(4));
				jbpmTranList.add(tran);
			}
		}
		catch (Exception e)
		{
			logger.error("Error when get processdefintion.", e);
		}
		finally
		{
			DbUtil.silentClose(rs);
			DbUtil.silentClose(ps);
			DbUtil.silentReturnConnection(con);
		}

		Map<Long, List<TaskJbpmTransition>> map = new HashMap<Long, List<TaskJbpmTransition>>();
		for (int i = 0; i < jbpmTranList.size(); i++)
		{
			List<TaskJbpmTransition> tranList = new ArrayList<TaskJbpmTransition>();
			long from = jbpmTranList.get(i).getFrom();
			if (map.containsKey(from))
			{
				tranList = map.get(from);
				tranList.add(jbpmTranList.get(i));
				map.put(from, tranList);
			}
			else
			{
				tranList.add(jbpmTranList.get(i));
				map.put(from, tranList);
			}
		}
		return map;
	}
	
	protected static List<ConditionNodeTargetInfo> getConditionNodeTargetInfo(
			long toParam, Map<Long, TaskJbpmNode> nodeMap,
			Map<Long, List<TaskJbpmTransition>> tranMap,
			List<ConditionNodeTargetInfo> conList)
	{
		List<TaskJbpmTransition> toList = new ArrayList<TaskJbpmTransition>();
		// get 'from' and 'to' value
		if (tranMap.containsKey(toParam))
		{
			toList = tranMap.get(toParam);
		}

		for (int i = 0; i < toList.size(); i++)
		{
			String arrowName = toList.get(i).getName();
			long to = toList.get(i).getTo();
			// According 'to' judge this node
			if (nodeMap.containsKey(to))
			{
				TaskJbpmNode node = nodeMap.get(to);
				String nodeClass = node.getNodeClass();
				String name = node.getNodeName();
				// Node
				if (nodeClass.equals("K"))
				{
					name = subString(name);
					conList.add(new ConditionNodeTargetInfo(arrowName, name));
				}
				// Condition Node
				else if (nodeClass.equals("D"))
				{
					conList = getConditionNodeTargetInfo(to, nodeMap, tranMap,
							conList);
				}
				// Exit
				else if (nodeClass.equals("E"))
				{
					name = "Exit";
					conList.add(new ConditionNodeTargetInfo(arrowName, name));
				}
				// Start
				else if (nodeClass.equals("R"))
				{
				}
			}
		}
		return conList;
	}
	
	protected static Map<Long, String> getWorkflowName(long jobId)
	{
		Map<Long, String> map = new HashMap<Long, String>();

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try
		{
			con = DbUtil.getConnection();
			ps = con.prepareStatement(QUERY_WORKFLOW_NAME_SQL);
			ps.setLong(1, jobId);
			rs = ps.executeQuery();

			while (rs.next())
			{
				map.put(rs.getLong(1), rs.getString(2));
			}
		}
		catch (Exception e)
		{
			logger.error("Error when get processdefintion.", e);
		}
		finally
		{
			DbUtil.silentClose(rs);
			DbUtil.silentClose(ps);
			DbUtil.silentReturnConnection(con);
		}
		return map;
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
   
	static class TaskJbpmNode
	{
		private long nodeId = -1;
		private String nodeClass = null;
		private String nodeName = null;
		private long processDefinition = -1;

		public long getNodeId()
		{
			return nodeId;
		}

		public void setNodeId(long nodeId)
		{
			this.nodeId = nodeId;
		}

		public String getNodeClass()
		{
			return nodeClass;
		}

		public void setNodeClass(String nodeClass)
		{
			this.nodeClass = nodeClass;
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
	
	static class TaskJbpmTransition{
		private long id = -1;
		private String name = null;
		private long processDefinition = -1;
		private long from = -1;
		private long to = -1;
		private long fromIndex = -1;
		public long getId()
		{
			return id;
		}
		public void setId(long id)
		{
			this.id = id;
		}
		public String getName()
		{
			return name;
		}
		public void setName(String name)
		{
			this.name = name;
		}
		public long getProcessDefinition()
		{
			return processDefinition;
		}
		public void setProcessDefinition(long processDefinition)
		{
			this.processDefinition = processDefinition;
		}
		public long getFrom()
		{
			return from;
		}
		public void setFrom(long from)
		{
			this.from = from;
		}
		public long getTo()
		{
			return to;
		}
		public void setTo(long to)
		{
			this.to = to;
		}
		public long getFromIndex()
		{
			return fromIndex;
		}
		public void setFromIndex(long fromIndex)
		{
			this.fromIndex = fromIndex;
		}
	}

	/**
     * Create new user
     * 
     * @param p_accessToken
     *            String Access token. REQUIRED.
     * @param p_userId
     *            String User ID. REQUIRED.
     *            Example: 'qaadmin'
     * @param p_password
     *            String Password. It requires 8 characters at least. REQUIRED.
     * @param p_firstName
     *            String First name. It can have 100 characters at most. REQUIRED.
     * @param p_lastName
     *            String Last name. It can have 100 characters at most. REQUIRED.
     * @param p_email
     *            String Email address. REQUIRED.
     *            If the email address is not vaild then the user's status will be set up as inactive
     * @param p_permissionGrps
     *            String[] Permission groups which the new user belongs to.
     *            The element in the array is the name of permission group.
     *            Example: [{"Administrator"}, {"ProjectManager"}]
     * @param p_status
     *            String Status of user. This parameter is not using now, it should be null.
     * @param p_roles
     *            Roles String information of user. It uses a string with XML format to mark all roles information of user. REQUIRED.
     *            Example:
     *              <?xml version=\"1.0\"?>
     *                <roles>
     *                  <role>
     *                    <sourceLocale>en_US</sourceLocale>
     *                    <targetLocale>de_DE</targetLocale>
     *                    <activities>
     *                      <activity>
     *                        <name>Dtp1</name>
     *                      </activity>
     *                      <activity>
     *                        <name>Dtp2</name>
     *                      </activity>
     *                    </activities>
     *                  </role>
     *                </roles>
     * @param p_isInAllProject
     *            boolean If the user need to be included in all project. REQUIRED.
     * @param p_projectIds
     *            String[] ID of projects which user should be included in. If p_isInAllProject is true, this will not take effect.
     *            Example: [{"1"}, {"3"}]
     * @return int Return code 
     *        0 -- Success 
     *        1 -- Invalid access token 
     *        2 -- Invalid user id 
     *        3 -- Cannot create super user
     *        4 -- User exists
     *        5 -- User does NOT exist
     *        6 -- User is NOT in the same company with logged user
     *        7 -- Invalid user password 
     *        8 -- Invalid first name 
     *        9 -- Invalid last name 
     *       10 -- Invalid email address 
     *       11 -- Invalid permission groups 
     *       12 -- Invalid project information 
     *       13 -- Invalid role information 
     *       14-- Current login user does not have enough permission
     *       -1 -- Unknown exception
     * @throws WebServiceException
     */
    int createUser(String p_accessToken, String p_userId,
            String p_password, String p_firstName, String p_lastName,
            String p_email, String[] p_permissionGrps, String p_status,
            String p_roles, boolean p_isInAllProject, String[] p_projectIds)
            throws WebServiceException
    {
        checkAccess(p_accessToken, "createUser");
        WebServicesLog.Start activityStart = null;
        try
        {
            User loggedUser = getUser(getUsernameFromSession(p_accessToken));
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", loggedUser.getUserName());
            activityArgs.put("userId", p_userId);
            activityArgs.put("password", p_password);
            activityArgs.put("firstName", p_firstName);
            activityArgs.put("lastName", p_lastName);
            activityArgs.put("email", p_email);
            activityArgs.put("permissionGrps", stringArr2Str(p_permissionGrps));
            activityArgs.put("roles", p_roles);
            activityArgs.put("isInAllProject", p_isInAllProject);
            activityArgs.put("projectIds", stringArr2Str(p_projectIds));
			activityStart = WebServicesLog.start(AmbassadorHelper.class,
					"createUser", activityArgs);

			String returnStr = checkPermissionReturnStr(p_accessToken,
					Permission.USERS_NEW);
			if (StringUtil.isNotEmpty(returnStr))
				return INVALD_PERMISSION;

	        int checkResult = validateUserInfo(p_accessToken, p_userId, p_password,
	                p_firstName, p_lastName, p_email, p_permissionGrps,
	                p_isInAllProject, p_projectIds, true);
	        if (checkResult > 0)
	            return checkResult;

			// Get current user as requesting user
            UserManagerWLRemote userManager = ServerProxy.getUserManager();
            Company company = ServerProxy.getJobHandler().getCompany(
                    loggedUser.getCompanyName());
            long companyId = company.getId();
            String companyIdString = String.valueOf(companyId);

            // Set up basic user information
            User user = userManager.createUser();
            //Because UserUtil.newUserId(...) will insert the relationship between
            //userid and username into user_id_user_name table directly, Then
            //for creating new user, it should be generated later after role checking
            //user.setUserId(UserUtil.newUserId(p_userId));
            user.setUserName(p_userId.trim());
            user.setFirstName(p_firstName.trim());
            user.setLastName(p_lastName.trim());
            user.setEmail(p_email.trim());
            user.setPassword(p_password.trim());
            user.setCompanyName(loggedUser.getCompanyName());
            user.isInAllProjects(p_isInAllProject);

            // Set up project information
            ArrayList<Long> projectIds = new ArrayList<Long>();
            ProjectHandlerWLRemote projectManager = ServerProxy
                    .getProjectHandler();
            if (p_isInAllProject)
            {
                // user is in all projects
                List<Project> projects = (List<Project>)projectManager.getAllProjects();
                if (projects == null || projects.size() == 0)
                    return INVALID_PROJECTS;
                for (Project project : projects)
                    projectIds.add(project.getIdAsLong());
            }
            else
            {
                // user is in some special projects
                for (String projectId : p_projectIds)
                    projectIds.add(Long.parseLong(projectId));
            }

            List<UserRole> roles = parseRoles(user, p_roles);
            if (roles == null)
                return INVALID_ROLES;
            
            user.setUserId(UserUtil.newUserId(p_userId.trim()));
            for (UserRole ur : roles)
                ur.setUser(user.getUserId());
            
            // Check the argument of permssion groups
            // Get all permission groups in special company
            ArrayList<PermissionGroup> permissionGroups = new ArrayList<PermissionGroup>();
            PermissionManager permissionManager = Permission.getPermissionManager();
            List<PermissionGroup> companyPermissionGroups = (List<PermissionGroup>) permissionManager
                    .getAllPermissionGroupsByCompanyId(companyIdString);
            
            //Get permission group map
            HashMap<String, PermissionGroup> permissionGroupsMap = new HashMap<String, PermissionGroup>();
            for (PermissionGroup pg : companyPermissionGroups)
                permissionGroupsMap.put(pg.getName(), pg);
            
            for (String pgName : p_permissionGrps)
                permissionGroups.add(permissionGroupsMap.get(pgName));
            
            // Add user
            userManager.addUser(loggedUser, user, projectIds, null, roles);
            user = userManager.getUser(user.getUserId());
            if (user != null) {
                // Set up user's permission groups
                ArrayList<String> users = new ArrayList<String>(1);
                users.add(user.getUserId());
                for (PermissionGroup pg : permissionGroups) 
                    permissionManager.mapUsersToPermissionGroup(users, pg);
            }
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            return UNKNOWN_ERROR;
        }
        finally
        {
            if (activityStart != null)
            {
                activityStart.end();
            }
        }

        return SUCCESS;
    }
    
    private int validateUserInfo(String accessToken, String userId,
            String password, String firstName, String lastName, String email,
            String[] permissionGroups, boolean isInAllProjects,
            String[] projectIds, boolean isToCreateUser)
    {
        // Basic check of parameters
        if (StringUtil.isEmpty(accessToken))
            return INVALID_ACCESS_TOKEN;
        if (StringUtil.isEmpty(userId) || !RegexUtil.validUserId(userId))
            return INVALID_USER_ID;
        if (isToCreateUser) {
            //Create new user
            if (StringUtil.isEmpty(password) || password.length() < 8)
                return INVALID_PASSWORD;
            if (StringUtil.isEmpty(firstName) || firstName.length() > 100)
                return INVALID_FIRST_NAME;
            if (StringUtil.isEmpty(lastName) || lastName.length() > 100)
                return INVALID_LAST_NAME;
            if (StringUtil.isEmpty(email) || !RegexUtil.validEmail(email))
                return INVALID_EMAIL_ADDRESS;
            if (permissionGroups == null || permissionGroups.length == 0)
                return INVALID_PERMISSION_GROUPS;
            if (projectIds == null || projectIds.length == 0)
                return INVALID_PROJECTS;
        } else {
            //Modify user
            if (StringUtil.isNotEmpty(password) && password.length() < 8)
                return INVALID_PASSWORD;
            if (StringUtil.isNotEmpty(firstName) && firstName.length() > 100)
                return INVALID_FIRST_NAME;
            if (StringUtil.isNotEmpty(lastName) && lastName.length() > 100)
                return INVALID_LAST_NAME;
            if (StringUtil.isNotEmpty(email) && !RegexUtil.validEmail(email))
                return INVALID_EMAIL_ADDRESS;
        }
        
        userId = userId.trim();

        // Check access token and get logged user and his company info
        String loggedUserName = getUsernameFromSession(accessToken);
        User loggedUser = null;
        try
        {
            loggedUser = getUser(loggedUserName);
            if (StringUtil.isEmpty(loggedUserName) || loggedUser == null)
                return INVALID_ACCESS_TOKEN;
        }
        catch (WebServiceException e)
        {
            return INVALID_ACCESS_TOKEN;
        }
        Company companyOfLoggedUser = CompanyWrapper
                .getCompanyByName(loggedUser.getCompanyName());
        long companyIdOfLoggedUser = companyOfLoggedUser.getId();
        String companyIdStringOfLoggedUser = String
                .valueOf(companyIdOfLoggedUser);

        // Check user id
        User user = null;
        try
        {
            UserManagerWLRemote userManager = ServerProxy.getUserManager();
            if (isToCreateUser)
            {
                Vector<User> usersInCompany = userManager
                        .getUsersFromCompany(companyIdStringOfLoggedUser);
                for (User iUser : usersInCompany)
                {
                    if (iUser.getUserName().equalsIgnoreCase(userId))
                        return USER_EXISTS;
                }
            }
            else
            {
                user = userManager.getUser(userId);
                if (user == null)
                    return USER_NOT_EXIST;
                if (!user.getCompanyName().equals(loggedUser.getCompanyName()))
                    return NOT_IN_SAME_COMPANY;
            }
        }
        catch (Exception e)
        {
            return INVALID_USER_ID;
        }

        // Check permission groups
        if (permissionGroups != null && permissionGroups.length > 0) {
            PermissionManager permissionManager = null;
            ArrayList<String> permissionGroupList = new ArrayList<String>();
            try
            {
                for (String perm : permissionGroups)
                {
                    if (StringUtil.isEmpty(perm))
                        return INVALID_PERMISSION_GROUPS;
                    permissionGroupList.add(perm.trim());
                }
    
                permissionManager = Permission.getPermissionManager();
                ArrayList<PermissionGroup> companyPermissionGroups = (ArrayList<PermissionGroup>) permissionManager
                        .getAllPermissionGroupsByCompanyId(companyIdStringOfLoggedUser);
                ArrayList<String> validPermissionGroupsList = new ArrayList<String>();
                for (PermissionGroup pg : companyPermissionGroups)
                    validPermissionGroupsList.add(pg.getName());
    
                for (String pg : permissionGroupList)
                {
                    if (!validPermissionGroupsList.contains(pg))
                        return INVALID_PERMISSION_GROUPS;
                }
            }
            catch (Exception e)
            {
                return INVALID_PERMISSION_GROUPS;
            }
        }

        // Check project Ids
        if (!isInAllProjects)
        {
            if (projectIds != null && projectIds.length > 0) {
                try
                {
                    Project project = null;
                    ProjectHandlerWLRemote projectManager = ServerProxy
                            .getProjectHandler();
                    long projectId = -1L;
    
                    for (String pid : projectIds)
					{
						if (StringUtil.isEmpty(pid))
							return INVALID_PROJECTS;
						projectId = Long.parseLong(pid.trim());
						project = projectManager.getProjectById(projectId);
						if (project == null
								|| (companyIdOfLoggedUser != 1 && project
										.getCompanyId() != companyIdOfLoggedUser))
							return INVALID_PROJECTS;
					}
                }
                catch (Exception e)
                {
                    return INVALID_PROJECTS;
                }
            }
        }

        return SUCCESS;
    }

    /**
     * Modify user
     * 
     * @param p_accessToken
     *            String Access token. REQUIRED.
     * @param p_userId
     *            String User ID.  REQUIRED.
     *            Example: 'qaadmin'
     * @param p_password
     *            String Password. It requires 8 characters at least.
     * @param p_firstName
     *            String First name. It can have 100 characters at most.
     * @param p_lastName
     *            String Last name. It can have 100 characters at most.
     * @param p_email
     *            String Email address.
     *            If the email address is not valid, the user's status will be set up as inactive.
     * @param p_permissionGrps
     *            String[] Permission groups which the new user belongs to.
     *            The element in the array is the name of permission group.
     *            Example: [{"Administrator"}, {"ProjectManager"}]
     * @param p_status
     *            String Status of user. This parameter is not using now, it should be null.
     * @param p_roles
     *            Roles String information of user. It uses a string with XML format to mark all roles information of user.
     *            Example:
     *              <?xml version=\"1.0\"?>
     *                <roles>
     *                  <role>
     *                    <sourceLocale>en_US</sourceLocale>
     *                    <targetLocale>de_DE</targetLocale>
     *                    <activities>
     *                      <activity>
     *                        <name>Dtp1</name>
     *                      </activity>
     *                      <activity>
     *                        <name>Dtp2</name>
     *                      </activity>
     *                    </activities>
     *                  </role>
     *                </roles>
     * @param p_isInAllProject
     *            boolean If the user need to be included in all project. REQUIRED.
     * @param p_projectIds
     *            String[] ID of projects which user should be included in. If p_isInAllProject is true, this will not take effect.
     *            Example: [{"1"}, {"3"}]
     * @return int Return code 
     *        0 -- Success 
     *        1 -- Invalid access token 
     *        2 -- Invalid user id 
     *        3 -- Cannot create super user
     *        4 -- User exists
     *        5 -- User does NOT exist
     *        6 -- User is NOT in the same company with logged user
     *        7 -- Invalid user password 
     *        8 -- Invalid first name 
     *        9 -- Invalid last name 
     *       10 -- Invalid email address 
     *       11 -- Invalid permission groups 
     *       12 -- Invalid project information 
     *       13 -- Invalid role information 
     *       14-- Current login user does not have enough permission
     *       -1 -- Unknown exception
     * @throws WebServiceException
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    int modifyUser(String p_accessToken, String p_userId, String p_password,
            String p_firstName, String p_lastName, String p_email,
            String[] p_permissionGrps, String p_status, String p_roles,
            boolean p_isInAllProject, String[] p_projectIds)
            throws WebServiceException
    {
        checkAccess(p_accessToken, "modifyUser");
        WebServicesLog.Start activityStart = null;
        try
        {
            // Get current user as requesting user
            User loggedUser = getUser(getUsernameFromSession(p_accessToken));

            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", loggedUser.getUserName());
            activityArgs.put("userId", p_userId);
            activityArgs.put("password", p_password);
            activityArgs.put("firstName", p_firstName);
            activityArgs.put("lastName", p_lastName);
            activityArgs.put("email", p_email);
            activityArgs.put("permissionGrps", stringArr2Str(p_permissionGrps));
            activityArgs.put("roles", p_roles);
            activityArgs.put("isInAllProject", p_isInAllProject);
            activityArgs.put("projectIds", stringArr2Str(p_projectIds));
			activityStart = WebServicesLog.start(AmbassadorHelper.class,
					"modifyUser", activityArgs);

			String returnStr = checkPermissionReturnStr(p_accessToken,
    				Permission.USERS_EDIT);
    		if (StringUtil.isNotEmpty(returnStr))
    			return INVALD_PERMISSION;
            
            int checkResult = validateUserInfo(p_accessToken, p_userId, p_password,
                    p_firstName, p_lastName, p_email, p_permissionGrps,
                    p_isInAllProject, p_projectIds, false);
            if (checkResult > 0)
                return checkResult;

            UserManagerWLRemote userManager = ServerProxy.getUserManager();
            PermissionManager permissionManager = Permission
                    .getPermissionManager();
            Company company = ServerProxy.getJobHandler().getCompany(
                    loggedUser.getCompanyName());
            long companyId = company.getId();

            // Set up basic user information
            User user = userManager.getUser(p_userId);
            if (StringUtil.isNotEmpty(p_firstName))
                user.setFirstName(p_firstName.trim());
            if (StringUtil.isNotEmpty(p_lastName))
                user.setLastName(p_lastName.trim());
            if (StringUtil.isNotEmpty(p_email))
                user.setEmail(p_email.trim());
            if (StringUtil.isNotEmpty(p_password))
                user.setPassword(p_password.trim());
            user.isInAllProjects(p_isInAllProject);

            // Set up project information
            ArrayList projectIds = null;
            ProjectHandlerWLRemote projectManager = ServerProxy
                    .getProjectHandler();
            if (p_isInAllProject)
            {
                // user is in all projects
                List<Project> projects = (List<Project>) projectManager.getAllProjects();
                if (projects != null && projects.size() > 0) {
                    projectIds = new ArrayList();
                    for (Project project : projects)
                        projectIds.add(project.getIdAsLong());
                }
            }
            else
            {
                if (p_projectIds != null && p_projectIds.length > 0) {
                    Project project = null;
                    projectIds = new ArrayList();
                    for (String pid : p_projectIds) {
                        project = projectManager.getProjectById(Long.parseLong(pid.trim()));
                        projectIds.add(project.getIdAsLong());
                    }
                }
            }

            List<UserRole> roles = null;
            if (StringUtil.isNotEmpty(p_roles))
            {
                roles = parseRoles(user, p_roles);
                if (roles == null || roles.size() == 0)
                	return INVALID_ROLES;
                else
                {
                    for (UserRole ur : roles)
                        ur.setUser(user.getUserId());
                }
            }

            // Check the argument of permission groups
            // Get all permission groups in special company
            ArrayList updatePermissionGroups = null;
            if (p_permissionGrps != null && p_permissionGrps.length > 0) {
                updatePermissionGroups = new ArrayList();
                
                List<PermissionGroup> companyPermissionGroups = (List<PermissionGroup>) permissionManager
                        .getAllPermissionGroupsByCompanyId(String.valueOf(companyId));
                
                //Get permission group map
                HashMap<String, PermissionGroup> permissionGroupsMap = new HashMap<String, PermissionGroup>();
                for (PermissionGroup pg : companyPermissionGroups)
                    permissionGroupsMap.put(pg.getName(), pg);
                
                for (String pgName : p_permissionGrps)
                    updatePermissionGroups.add(permissionGroupsMap.get(pgName));
            }

            // Modify user
            userManager.modifyUser(loggedUser, user, projectIds, null, roles);

            // Set up user's permission groups
            updatePermissionGroups(p_userId, updatePermissionGroups);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            return UNKNOWN_ERROR;
        }
        finally
        {
            if (activityStart != null)
            {
                activityStart.end();
            }
        }

        return SUCCESS;
    }

    /**
     * Parse roles information from XML format string.
     * The XML format string is like below:
     * <?xml version=\"1.0\"?>
     * <roles>
     *   <role>
     *     <sourceLocale>en_US</sourceLocale>
     *     <targetLocale>de_DE</targetLocale>
     *     <activities>
     *       <activity>
     *         <name>Dtp1</name>
     *       </activity>
     *       <activity>
     *         <name>Dtp2</name>
     *       </activity>
     *     </activities>
     *   </role>
     * </roles>
     * 
     * @param p_user -- User
     * @param p_xml -- Roles information
     * @return List<UserRole>
     */
    @SuppressWarnings({ "unused", "rawtypes" })
    private List<UserRole> parseRoles(User p_user, String p_xml)
    {
        if (StringUtil.isEmpty(p_xml))
            return null;

        ArrayList<UserRole> roles = new ArrayList<UserRole>();
        try
        {
            XmlParser parser = new XmlParser();
            Document doc = parser.parseXml(p_xml);
            Element root = doc.getRootElement();
            List rolesList = root.elements();
            if (rolesList == null || rolesList.size() == 0)
                return null;

            String sourceLocale, targetLocale, activityId, activityName, activityDisplayName, activityUserType, activityType;
            Activity activity = null;
            UserRole role = null;
            LocalePair localePair = null;

            UserManagerWLRemote userManager = ServerProxy.getUserManager();
            JobHandlerWLRemote jobManager = ServerProxy.getJobHandler();
            Company loggedCompany = CompanyWrapper.getCompanyByName(p_user
                    .getCompanyName());

            for (Iterator iter = rolesList.iterator(); iter.hasNext();)
			{
				Element roleElement = (Element) iter.next();
				sourceLocale = roleElement.element("sourceLocale").getText();
				targetLocale = roleElement.element("targetLocale").getText();
				String localeCompanyName = null;
				Company localeCompany = null;
				Element node = (Element) roleElement
						.selectSingleNode("companyName");
				if (CompanyWrapper.SUPER_COMPANY_ID.equals(String
						.valueOf(loggedCompany.getId())))
				{
					if (node == null)
						return null;
					localeCompanyName = roleElement.element("companyName")
							.getText();
					localeCompany = CompanyWrapper
							.getCompanyByName(localeCompanyName.trim());
					if (localeCompany == null)
						return null;
				}
				else
				{
					if (node != null)
						return null;
				}

				if (localeCompany == null)
					localePair = getLocalePairBySourceTargetAndCompanyStrings(
							sourceLocale, targetLocale, loggedCompany.getId());
				else localePair = getLocalePairBySourceTargetAndCompanyStrings(
						sourceLocale, targetLocale, localeCompany.getId());
				
				if (localePair == null)
					return null;

				List activitiesList = roleElement.elements("activities");
				if (activitiesList == null || activitiesList.size() == 0)
					return null;

				for (Iterator iter1 = activitiesList.iterator(); iter1
						.hasNext();)
				{
					Element activitiesElement = (Element) iter1.next();

					List activityList = activitiesElement.elements();
					for (Iterator iter2 = activityList.iterator(); iter2
							.hasNext();)
					{
						Element activityElement = (Element) iter2.next();
						activityName = activityElement.element("name")
								.getText();
						if (localeCompany != null)
						{
							activity = jobManager.getActivityByCompanyId(
									activityName + "_" + localeCompany.getId(),
									String.valueOf(localeCompany.getId()));
						}
						else
						{
							activity = jobManager.getActivityByCompanyId(
									activityName + "_" + loggedCompany.getId(),
									String.valueOf(loggedCompany.getId()));
						}

						if (activity == null)
							return null;

						role = userManager.createUserRole();
						((Role) role).setActivity(activity);
						((Role) role).setSourceLocale(sourceLocale);
						((Role) role).setTargetLocale(targetLocale);
						// role.setUser(p_user.getUserId());
						roles.add(role);
					}
				}
			}
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            return null;
        }
        return roles;
    }
    
    
    private LocalePair getLocalePairBySourceTargetAndCompanyStrings(
            String p_sourceLocaleString, String p_targetLocaleString,
            long companyId) throws LocaleManagerException, RemoteException
    {
        if (p_sourceLocaleString == null || p_sourceLocaleString.length() <= 1
                || p_targetLocaleString == null
                || p_targetLocaleString.length() <= 1)
        {
            return null;
        }
        LocalePair localePair = null;
        // source
        StringTokenizer srcTokenizer = new StringTokenizer(
                p_sourceLocaleString, SEPARATOR);

        // should at least be 2 (ll_cc) or at most 3 (ll_cc_vv)
        String language = srcTokenizer.nextToken();
        String country = "";

        if (p_sourceLocaleString.length() > 3)
        {
            country = srcTokenizer.nextToken();
        }

        HashMap map = new HashMap();
        map.put("sLanguage", language);
        map.put("sCountry", country);
        // target
        StringTokenizer trgTokenizer = new StringTokenizer(
                p_targetLocaleString, SEPARATOR);
        language = trgTokenizer.nextToken();
        country = "";
        if (p_targetLocaleString.length() > 3)
        {
            country = trgTokenizer.nextToken();
        }

        map.put("tLanguage", language);
        map.put("tCountry", country);

        try
		{
			StringBuffer hql = new StringBuffer();
			hql.append("from LocalePair lp where lp.isActive = 'Y'")
					.append(" and lp.source.language = :sLanguage ")
					.append(" and lp.source.country = :sCountry ")
					.append(" and lp.target.language = :tLanguage ")
					.append(" and lp.target.country = :tCountry");

			hql.append(" and lp.companyId = :companyId");
			map.put("companyId", companyId);

			List localePairs = HibernateUtil.search(hql.toString(), map);
			if (localePairs != null && localePairs.size() > 0)
			{
				localePair = (LocalePair) localePairs.get(0);
			}
		}
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return localePair;
    }

    /**
     * Update user's permission groups
     * 
     * @param p_userId
     *            User ID
     * @param p_permissionGrps
     *            Permission groups
     * @throws EnvoyServletException
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void updatePermissionGroups(String p_userId, List p_permissionGrps)
            throws EnvoyServletException
    {
        ArrayList changed = (ArrayList) p_permissionGrps;
        if (changed == null)
            return;
        ArrayList existing = (ArrayList) PermissionHelper
                .getAllPermissionGroupsForUser(p_userId);
        if (existing == null && changed.size() == 0)
            return;

        ArrayList list = new ArrayList(1);
        list.add(p_userId);
        try
        {
            PermissionManager manager = Permission.getPermissionManager();
            if (existing == null)
            {
                // just adding new perm groups
                for (int i = 0; i < changed.size(); i++)
                {
                    PermissionGroup pg = (PermissionGroup) changed.get(i);
                    manager.mapUsersToPermissionGroup(list, pg);
                }
            }
            else
            {
                // need to determine what to add and what to remove.
                // Loop thru old list and see if perm is in new list. If not,
                // remove it.
                for (int i = 0; i < existing.size(); i++)
                {
                    PermissionGroup pg = (PermissionGroup) existing.get(i);
                    boolean found = false;
                    for (int j = 0; j < changed.size(); j++)
                    {
                        PermissionGroup cpg = (PermissionGroup) changed.get(j);
                        if (pg.getId() == cpg.getId())
                        {
                            found = true;
                            break;
                        }
                    }
                    if (!found)
                        manager.unMapUsersFromPermissionGroup(list, pg);
                }

                // Loop thru new list and see if perm is in old list. If not,
                // add it.
                for (int i = 0; i < changed.size(); i++)
                {
                    boolean found = false;
                    PermissionGroup pg = (PermissionGroup) changed.get(i);
                    for (int j = 0; j < existing.size(); j++)
                    {
                        PermissionGroup cpg = (PermissionGroup) existing.get(j);
                        if (pg.getId() == cpg.getId())
                        {
                            found = true;
                            break;
                        }
                    }
                    if (!found)
                        manager.mapUsersToPermissionGroup(list, pg);
                }
            }
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Reassign task to other translators
     *  
     * @param p_accessToken
     *            String Access token
     * @param p_workflowId
     *            String ID of task
     *            Example: "10"
     * @param p_users
     *            String[] Users' information who will be reassigned to. The element in the array is [{userid}].
     *            Example: ["qaadmin", "qauser"]
     * @return 
     *            Return null if the reassignment executes successfully.
     *            Otherwise it will throw exception or return error message
     * @throws WebServiceException
     */
    @SuppressWarnings("rawtypes")
    String taskReassign(String p_accessToken, String p_taskId,
            String[] p_users) throws WebServiceException
    {
		checkAccess(p_accessToken, "jobsReassign");
		String returnStr = checkPermissionReturnStr(p_accessToken,
				Permission.JOB_WORKFLOWS_REASSIGN);
		if (StringUtil.isNotEmpty(returnStr))
			return returnStr;
		
        try
        {
            Assert.assertNotEmpty(p_accessToken, "Access token");
            Assert.assertNotEmpty(p_taskId, "Task Id");
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new WebServiceException(e.getMessage());
        }

        if (p_users == null || p_users.length == 0)
        {
            throw new WebServiceException("Users is null");
        }
        

        ArrayList<String> reassignedUsers = new ArrayList<String>();
        StringBuffer users = new StringBuffer();
        for (String userId : p_users)
        {
            if (StringUtil.isEmpty(userId))
                continue;
            userId = userId.trim();
            users.append(userId + ",");
            reassignedUsers.add(userId);
        }

        WebServicesLog.Start activityStart = null;
        try
        {
            String userName = getUsernameFromSession(p_accessToken);
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", userName);
            activityArgs.put("taskId", p_taskId);
            activityArgs.put("users", users.toString());
            activityStart = WebServicesLog
                    .start(Ambassador.class,
                            "jobsReassign(p_accessToken, p_workflowId,p_targetLocale,p_workflowId,p_users)",
                            activityArgs);

            User loggedUser = getUser(userName);

            Task taskInfo = null;
            try
            {
                taskInfo = ServerProxy.getTaskManager().getTask(
                        Integer.parseInt(p_taskId));
            }
            catch (Exception e)
            {
                logger.error("Error found in get task info", e);
                return "Incorrect task ID";
            }

            if (taskInfo == null)
                return "Cannot find task";

            Company company = CompanyWrapper.getCompanyByName(loggedUser
                    .getCompanyName());
            long companyId = company.getId();
            if (companyId !=1 && companyId != taskInfo.getCompanyId())
                return "Cannot re-assign task which is in different company";

            String taskAcceptor = taskInfo.getAcceptor();
            // If current task acceptor is the user to be reassigned, then
            // return
            if (!StringUtil.isEmpty(taskAcceptor))
            {
                if (reassignedUsers.size() == 1
                        && reassignedUsers.contains(taskAcceptor))
                    return "Current task acceptor is the same with user being reassigned";
            }

            String errorMessage = "";
            String sourceLocale = taskInfo.getSourceLocale().toString();
            String targetLocale = taskInfo.getTargetLocale().toString();
            List<Task> tasks = new ArrayList<Task>();
            tasks.add(taskInfo);
            Workflow wf = taskInfo.getWorkflow();
            long wfId = wf.getId();
            String workflowState = wf.getState();
            if (!Workflow.READY_TO_BE_DISPATCHED.equals(workflowState)
                    && !Workflow.DISPATCHED.equals(workflowState))
            {
                return "Workflow which contains the task is not in 'Ready' or 'In Progress' state.";
            }

            wf = ServerProxy.getWorkflowManager().getWorkflowById(wfId);
            Hashtable taskUserHash = new Hashtable();
            Hashtable taskSelectedUserHash = new Hashtable();

            updateUsers(tasks, taskUserHash, taskSelectedUserHash, wf);

            Enumeration keys = taskUserHash.keys();
            HashMap<Long, Vector<NewAssignee>> roleMap = new HashMap<Long, Vector<NewAssignee>>();
            long taskId = -1;
            String displayRole = "";
            ContainerRole containerRole = null;
            Activity activity = null;
            String[] roles = null;
            Vector<NewAssignee> newAssignees = null;
            Task task = null;
            User user = null;
            Hashtable<String, UserInfo> taskUsers = null;

            while (keys.hasMoreElements())
            {
                task = (Task) keys.nextElement();
                String taskState = task.getStateAsString();
                if (!Task.STATE_ACTIVE_STR.equals(taskState)
                        && !Task.STATE_DEACTIVE_STR.equals(taskState)
                        && !Task.STATE_ACCEPTED_STR.equals(taskState))
                {
                    errorMessage += " task state is not in Active, Deactive or Accepted status.";
                    continue;
                }
                taskUsers = (Hashtable<String, UserInfo>) taskUserHash
                        .get(task);
                taskId = task.getId();
                activity = ServerProxy.getJobHandler()
                        .getActivityByCompanyId(task.getTaskName(),
                                String.valueOf(task.getCompanyId()));
                containerRole = ServerProxy.getUserManager().getContainerRole(
                        activity, sourceLocale, targetLocale);
                ArrayList<User> vaildUsers = new ArrayList<User>();
                for (String userId : reassignedUsers)
                {
                    user = ServerProxy.getUserManager().getUser(userId);
                    if (!taskUsers.containsKey(userId))
                    {
                        errorMessage += " " + userId
                                + " has not corresponding role.";
                        continue;
                    }
                    vaildUsers.add(user);
                }
                if (vaildUsers.size() == 0)
                    continue;

                newAssignees = new Vector<NewAssignee>();
                roles = new String[vaildUsers.size()];
                int i = 0;
                for (User userInfo : vaildUsers)
                {
                    roles[i] = containerRole.getName() + " "
                            + userInfo.getUserId();
                    if (i == vaildUsers.size() - 1)
                    {
                        displayRole += userInfo.getUserName();
                    }
                    else
                    {
                        displayRole += userInfo.getUserName() + ",";
                    }
                    i++;
                }
                newAssignees.addElement(new NewAssignee(roles, displayRole,
                        true));
                roleMap.put(taskId, newAssignees);
            }

            boolean shouldModifyWf = false;
            WorkflowInstance wi = ServerProxy.getWorkflowServer()
                    .getWorkflowInstanceById(wf.getId());

            Vector<WorkflowTaskInstance> wfiTasks = wi
                    .getWorkflowInstanceTasks();
            for (WorkflowTaskInstance wti : wfiTasks)
            {
                newAssignees = roleMap.get(wti.getTaskId());
                if (newAssignees != null)
                {
                    for (int r = 0; r < newAssignees.size(); r++)
                    {
                        NewAssignee na = (NewAssignee) newAssignees
                                .elementAt(r);
                        if (na != null
                                && !areSameRoles(wti.getRoles(), na.m_roles))
                        {
                            shouldModifyWf = true;
                            wti.setRoleType(na.m_isUserRole);
                            wti.setRoles(na.m_roles);
                            wti.setDisplayRoleName(na.m_displayRoleName);
                        }
                    }
                }
            }

            // modify one workflow at a time and reset the flag
            if (shouldModifyWf)
            {
                shouldModifyWf = false;
                ServerProxy.getWorkflowManager().modifyWorkflow(null, wi, null,
                        null);
            }

            if (!StringUtil.isEmpty(errorMessage))
                return errorMessage.substring(1);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
        finally
        {
            if (activityStart != null)
            {
                activityStart.end();
            }
        }

        return null;
    }

    /**
     * Get the list of users for each Review-Only activity.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void updateUsers(List p_tasks, Hashtable p_taskUserHash,
            Hashtable p_taskSelectedUserHash, Workflow p_wf)
            throws GeneralException, RemoteException
    {
        Project proj = p_wf.getJob().getL10nProfile().getProject();
        for (Iterator iter = p_tasks.iterator(); iter.hasNext();)
        {
            Hashtable userHash = new Hashtable();
            Hashtable selectedUserHash = new Hashtable();
            Task task = (Task) iter.next();

            List selectedUsers = null;
            long taskId = task.getId();
            WorkflowInstance wi = ServerProxy.getWorkflowServer().getWorkflowInstanceById(p_wf.getId());
            //WorkflowTaskInstance wfTask = p_wf.getIflowInstance()
            //        .getWorkflowTaskById(taskId);
            WorkflowTaskInstance wfTask = wi.getWorkflowTaskById(taskId);
            String[] roles = wfTask.getRoles();
            String[] userIds = ServerProxy.getUserManager()
                    .getUserIdsFromRoles(roles, proj);
            if ((userIds != null) && (userIds.length > 0))
            {
                selectedUsers = ServerProxy.getUserManager().getUserInfos(
                        userIds);
            }

            // get all users for this task and locale pair.
            List userInfos = ServerProxy.getUserManager().getUserInfos(
                    task.getTaskName(), task.getSourceLocale().toString(),
                    task.getTargetLocale().toString());
            Set<String> projectUserIds = null;
            if (proj != null)
            {
                projectUserIds = proj.getUserIds();
            }

            if (userInfos == null)
                continue;

            for (Iterator iter2 = userInfos.iterator(); iter2.hasNext();)
            {
                UserInfo userInfo = (UserInfo) iter2.next();
                // filter user by project
                if (projectUserIds != null)
                {
                    String userId = userInfo.getUserId();
                    // if the specified user is contained in the project
                    // then add to the Hash.
                    if (projectUserIds.contains(userId))
                    {
                        userHash.put(userInfo.getUserId(), userInfo);
                    }
                }
            }
            p_taskUserHash.put(task, userHash);
            if (selectedUsers == null)
                continue;

            for (Iterator iter3 = selectedUsers.iterator(); iter3.hasNext();)
            {
                UserInfo ta = (UserInfo) iter3.next();
                selectedUserHash.put(ta.getUserId(), ta);
            }
            p_taskSelectedUserHash.put(task, selectedUserHash);
        }
    }

    /**
     * Determines whether the two array of roles contain the same set of role
     * names.
     */
    private boolean areSameRoles(String[] p_workflowRoles,
            String[] p_selectedRoles)
    {
        // First need to sort since Arrays.equals() requires
        // the parameters to be sorted
        Arrays.sort(p_workflowRoles);
        Arrays.sort(p_selectedRoles);
        return Arrays.equals(p_workflowRoles, p_selectedRoles);
    }

    /**
     * Get tasks in specified workflow
     * 
     * @param p_wfId
     *            workflow ID
     * @return ArrayList Collection of tasks which is in the specified workflow
     * @throws WebServiceException
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private ArrayList<Task> getTasksInWorkflow(String p_wfId)
            throws WebServiceException
    {
        ArrayList<Task> tasks = new ArrayList<Task>();
        long wfId = 0l;

        // Validate workflow ID
        if (p_wfId == null || p_wfId.trim().length() == 0)
            return tasks;
        try
        {
            wfId = Long.parseLong(p_wfId);
        }
        catch (NumberFormatException nfe)
        {
            throw new WebServiceException("Wrong workflow ID");
        }

        try
        {
            WorkflowInstance workflowInstance = WorkflowProcessAdapter
                    .getProcessInstance(wfId);
            Workflow workflow = ServerProxy.getWorkflowManager()
                    .getWorkflowByIdRefresh(wfId);
            Hashtable tasksInWF = workflow.getTasks();

            // get the NodeInstances of TYPE_ACTIVITY
            List<WorkflowTaskInstance> nodesInPath = workflowInstance
                    .getDefaultPathNode();

            for (WorkflowTaskInstance task : nodesInPath)
            {
                Task taskInfo = (Task) tasksInWF.get(task.getTaskId());

                if (taskInfo.reassignable())
                {
                    tasks.add(taskInfo);
                }
            }
            return tasks;
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new WebServiceException(e.getMessage());
        }
    }

    private class NewAssignee
    {
        String m_displayRoleName = null;

        String[] m_roles = null;

        boolean m_isUserRole = false;

        NewAssignee(String[] p_roles, String p_displayRoleName,
                boolean p_isUserRole)
        {
            m_displayRoleName = p_displayRoleName;
            m_roles = p_roles;
            m_isUserRole = p_isUserRole;
        }
    }

    @Override
    protected void checkIfInstalled() throws WebServiceException
    {
        // do nothing
    }

    private static String subString(String name)
    {
    	if(name == null)
    		return null;
    	
    	String[] nameStr = name.split("_");
    	StringBuffer buffer = new StringBuffer();
    	for(int i=2;i<nameStr.length;i++){
    		buffer.append(nameStr[i]);
    		buffer.append("_");
    	}
    	String returnStr = null;
    	if(buffer.toString().endsWith("_")){
    		returnStr = buffer.toString().substring(0, buffer.toString().lastIndexOf("_"));
    	}
    	return returnStr;
    }

    private static String toInClause(List<?> list)
    {
        StringBuilder in = new StringBuilder();
        if (list.size() == 0)
            return "(0)";
        
        in.append("(");
        for (Object o : list)
        {
            if (o instanceof List)
            {
                if (((List) o).size() == 0)
                    continue;
                
                for (Object id : (List<?>) o)
                {
                    if (id instanceof String)
                    {
                        in.append("'");
                        in.append(((String) id).replace("\'", "\\\'"));
                        in.append("'");
                    }
                    else
                    {
                        in.append(id);
                    }
                    in.append(",");
                }
            }
            else if (o instanceof String)
            {
                in.append("'");
                in.append(((String) o).replace("\'", "\\\'"));
                in.append("'");
                in.append(",");
            }
            else
            {
                in.append(o);
                in.append(",");
            }
        }
        in.deleteCharAt(in.length() - 1);
        in.append(")");

        return in.toString();
    }

    /**
     * Offline download to get reviewers comments report, translations edit
     * report or offline translation kit.
     * 
     * @param p_accessToken
     *            -- login user's token
     * @param p_taskId
     *            -- task ID to offline download file for.
     * @param p_workOfflineFileType
     *            -- 1 : Reviewer Comments Report or Translations Edit Report (this follows UI settings)
     *            -- 2 : Offline Translation Kit
     *            -- 3 : Translation Edit Report
     *            -- 4 : Reviewer Comments Report
     *            -- 5 : Reviewer Comments Report (Simplified)
     *            -- 6 : Post Review QA Report
     *            -- 7 : Translation Verification Report
     *            -- 14 : Reviewer Comments Report with Compact Tags
	 *			   -- 15 : Reviewer Comments Report (Simplified) with Compact Tags
     * @throws WebServiceException
     */
    protected String getWorkOfflineFiles(String p_accessToken, Long p_taskId,
            int p_workOfflineFileType, boolean p_isJson)
            throws WebServiceException
    {
		// Check work offline file type
		if (p_workOfflineFileType != 1 && p_workOfflineFileType != 2
				&& p_workOfflineFileType != 3 && p_workOfflineFileType != 4
				&& p_workOfflineFileType != 5 && p_workOfflineFileType != 6
				&& p_workOfflineFileType != 7)
		{
			if (p_isJson && p_workOfflineFileType != 14
					&& p_workOfflineFileType != 15)
			{
				return makeErrorMessage(
						p_isJson,
						GET_WORK_OFFLINE_FILES,
						"Invalid workOfflineFileType "
								+ p_workOfflineFileType
								+ ", it should be limited in 1, 2, 3, 4, 5, 6, 7, 14 or 15.");
			}
			
			if (!p_isJson)
			{
				return makeErrorMessage(
						p_isJson,
						GET_WORK_OFFLINE_FILES,
						"Invalid workOfflineFileType "
								+ p_workOfflineFileType
								+ ", it should be limited in 1, 2, 3, 4, 5, 6 or 7.");
			}
		}

        Task task = null;
        try
        {
            task = ServerProxy.getTaskManager().getTask(p_taskId);
        }
        catch (Exception e)
        {
            logger.warn("Can not get task info by taskId " + p_taskId);
        }

        if (task == null)
        {
            return makeErrorMessage(p_isJson, GET_WORK_OFFLINE_FILES,
                    "Can not find task by taskId " + p_taskId);
        }

        if (task.getState() != Task.STATE_ACCEPTED)
        {
            return makeErrorMessage(p_isJson, GET_WORK_OFFLINE_FILES,
                    "This task should be in ACCEPTED state.");
        }
        
		User loggedUser = getUser(getUsernameFromSession(p_accessToken));
		String userId = loggedUser.getUserId();
		ProjectImpl project = getProjectByTask(task);
		if (!userId.equals(task.getAcceptor())
				&& !userId.equals(project.getProjectManagerId()))
		{
			return makeErrorJson(
					GET_WORK_OFFLINE_FILES,
					"This task belongs to user "
							+ UserUtil.getUserNameById(task.getAcceptor())
							+ ", current logged user has no previlege to handle it.");
		}

        String returning = "";
        String companyName = CompanyWrapper.getCompanyNameById(task.getCompanyId());
        WebServicesLog.Start activityStart = null;
        try
        {
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", loggedUser.getUserName());
            activityArgs.put("taskId", p_taskId);
            activityArgs.put("workOfflineFileType", p_workOfflineFileType);
            activityStart = WebServicesLog.start(Ambassador4Falcon.class,
                            "getWorkOfflineFiles", activityArgs);

            String fileUrl = null;
            ReportGenerator generator = null;
            boolean isIncludeCompactTags = (project == null ? false
                    : project.isReviewReportIncludeCompactTags());
            // Follow UI settings to decide which report to generate.
            if (p_workOfflineFileType == 1)
            {
                if (task.getType() == Activity.TYPE_REVIEW)
                {
                    PermissionSet perms = Permission.getPermissionManager()
                            .getPermissionSetForUser(task.getAcceptor());
                    if (!perms.getPermissionFor(Permission.REPORTS_LANGUAGE_SIGN_OFF)
                            && perms.getPermissionFor(Permission.REPORTS_LANGUAGE_SIGN_OFF_SIMPLE))
                    {
                        generator = new ReviewersCommentsSimpleReportGenerator(
                                companyName);
                        ((ReviewersCommentsSimpleReportGenerator) generator)
                                .setIncludeCompactTags(isIncludeCompactTags);
                        ((ReviewersCommentsSimpleReportGenerator) generator)
                                .setUserId(userId);
                    }
                    else
                    {
                        generator = new ReviewersCommentsReportGenerator(
                                companyName);
                        ((ReviewersCommentsReportGenerator) generator)
                                .setIncludeCompactTags(isIncludeCompactTags);
                        ((ReviewersCommentsReportGenerator) generator)
                                .setUserId(userId);
                    }
                }
                else
                {
                    generator = new TranslationsEditReportGenerator(companyName);
                    ((TranslationsEditReportGenerator) generator).setUserId(userId);
                }
            }
            // translation kit
            else if (p_workOfflineFileType == 2)
            {
                OfflineEditManager oem = ServerProxy.getOfflineEditManager();
                DownloadParams params = oem.getDownloadParamsByUser(userId, task);
                File file = oem.getDownloadOfflineFiles(userId, task.getId(), params);
                StringBuffer root = new StringBuffer();
                root.append(AmbassadorUtil.getCapLoginOrPublicUrl()).append("/DownloadOfflineKit");
                if (task.getCompanyId() != 1)
                {
                    root.append("/").append(companyName); 
                }
                root.append("/GlobalSight/CustomerDownload/").append(file.getName());
                fileUrl = root.toString();
            }
            // TER (does not care current task type)
            else if (p_workOfflineFileType == 3)
            {
                generator = new TranslationsEditReportGenerator(companyName);
                ((TranslationsEditReportGenerator) generator).setUserId(userId);
            }
            // RCR (does not care current task type)
            else if (p_workOfflineFileType == 4)
            {
                generator = new ReviewersCommentsReportGenerator(companyName);
                ((ReviewersCommentsReportGenerator) generator)
                        .setIncludeCompactTags(isIncludeCompactTags);
                ((ReviewersCommentsReportGenerator) generator)
                        .setUserId(userId);
            }
            // RCR Simplified (does not care current task type)
            else if (p_workOfflineFileType == 5)
            {
                generator = new ReviewersCommentsSimpleReportGenerator(
                        companyName);
                ((ReviewersCommentsSimpleReportGenerator) generator)
                        .setIncludeCompactTags(isIncludeCompactTags);
                ((ReviewersCommentsSimpleReportGenerator) generator)
                        .setUserId(userId);
            }
            //PRR (does not care current task type)
            else if (p_workOfflineFileType == 6)
            {
                generator = new PostReviewQAReportGenerator(companyName);
                ((PostReviewQAReportGenerator) generator).setUserId(userId);
			}
            //TVR (does not care current task type)
            else if (p_workOfflineFileType == 7)
            {
                generator = new TranslationVerificationReportGenerator(companyName);
                ((TranslationVerificationReportGenerator)generator).setUserId(userId);
            }
			// Reviewer Comments Report with Compact Tags
			else if (p_workOfflineFileType == 14)
			{
				generator = new ReviewersCommentsReportGenerator(companyName);
				((ReviewersCommentsReportGenerator) generator)
						.setIncludeCompactTags(true);
				((ReviewersCommentsReportGenerator) generator)
						.setUserId(userId);
			}
			// Reviewer Comments Report (Simplified) with Compact Tags
			else if (p_workOfflineFileType == 15)
			{
				generator = new ReviewersCommentsSimpleReportGenerator(
						companyName);
				((ReviewersCommentsSimpleReportGenerator) generator)
						.setIncludeCompactTags(true);
				((ReviewersCommentsSimpleReportGenerator) generator)
						.setUserId(userId);
			}

			if (p_workOfflineFileType == 1 || p_workOfflineFileType == 3
					|| p_workOfflineFileType == 4 || p_workOfflineFileType == 5
					|| p_workOfflineFileType == 6 || p_workOfflineFileType == 7
					|| p_workOfflineFileType == 14
					|| p_workOfflineFileType == 15)
			{
                List<Long> jobIds = new ArrayList<Long>();
                jobIds.add(task.getJobId());
                List<GlobalSightLocale> trgLocales = new ArrayList<GlobalSightLocale>();
                trgLocales.add(task.getTargetLocale());
                File[] files = generator.generateReports(jobIds, trgLocales);
                if (files.length > 0)
                {
                    File file = files[0];
                    String superFSDir = AmbFileStoragePathUtils
                            .getFileStorageDirPath(1).replace("\\", "/");
                    String fullPathName = file.getAbsolutePath().replace("\\", "/");
                    String path = fullPathName.substring(fullPathName
                            .indexOf(superFSDir) + superFSDir.length());
                    path = path.substring(path.indexOf("/Reports/")
                            + "/Reports/".length());
                    String root = AmbassadorUtil.getCapLoginOrPublicUrl() + "/DownloadReports";
                    fileUrl = root + "/" + path;
                }
            }

            if (p_isJson)
            {
                JSONObject jsonObj = new JSONObject();
                jsonObj.put("path", fileUrl);
                jsonObj.put("taskId", task.getId());
                jsonObj.put("targetLocale", task.getTargetLocale().toString());
                jsonObj.put("acceptorUserId", task.getAcceptor());
                returning = jsonObj.toString();
            }
            else
            {
                returning = fileUrl;                
            }
        }
        catch (Exception e)
        {
            logger.error(e);
            String message = "Error when generate translation kit or report.";
            return makeErrorMessage(p_isJson, GET_WORK_OFFLINE_FILES, message);
        }
        finally
        {
            if (activityStart != null)
            {
                activityStart.end();
            }
        }

        return returning;
    }

    /**
     *  Offline download to get reviewers comments report, translations edit
     * report or offline translation kit.
     * 
     * @param p_accessToken
     *            -- login user's token
     * @param p_taskId
     *            -- task ID to offline download file for.
     * @param p_workOfflineFileType
     *            -- 1 : Reviewer Comments Report or Translations Edit Report (this follows UI settings)
     *            -- 2 : Offline Translation Kit
     *            -- 3 : Translation Edit Report
     *            -- 4 : Reviewer Comments Report
     *            -- 5 : Reviewer Comments Report (Simplified)
     *            -- 6 : Post Review QA Report
     *            -- 7 : Translation Verification Report
     *            -- 8  : Biligual Trados RTF
	 *			   -- 9  : Trados 7 TTX
	 *			   -- 10  : OmegaT
	 *			   -- 11 : XLiff 1.2
	 *			   -- 12 : Xliff 2.0
	 *			   -- 13 : RTF List view
	 *			   -- 14 : Reviewer Comments Report with Compact Tags
	 *			   -- 15 : Reviewer Comments Report (Simplified) with Compact Tags
     *@param p_workofflineFileTypeOption
     *			   --1  : consolidate/split = split per file, include repeated segments = no (Default)
     *			   --2  : consolidate/split = consolidate (overrides preserve folder structure setting),include repeated segments = no
     *			   --3  : consolidate/split = split per wordcount of 2000, include repeated segments = no
     *			   --4  : consolidate/split = split per file, include repeated segments = yes
     *			   --5  : consolidate/split = consolidate (overrides preserve folder structure setting),include repeated segments = yes
     *			   --6  : consolidate/split = split per wordcount of 2000, include repeated segments = yes
     * @return -- JSON string. -- If fail, it is like
     *         '{"getWorkOfflineFiles":"Corresponding message is here."}'; -- If
     *         succeed, report returning is like
     *         '{"taskId":3715,"targetLocale":"zh_CN","acceptorUserId":"yorkadmin","path":"http://10.10.215.21:8080/globalsight/DownloadReports/yorkadmin/TranslationsEditReport/20140219/ReviewersCommentsReport-(jobname_492637643)(337)-en_US_zh_CN-20140218
     *         162543.xlsx"}'. -- offline translation kit returning is like
     *         '{"taskId":3715,"targetLocale":"zh_CN","acceptorUserId":"yorkadmin","path":"http://10.10.215.21:8080/globalsight/DownloadOfflineKit/[CompanyName]/GlobalSight/CustomerDownload/[jobName_zh_CN.zip]"}'
     *         .
     * @throws WebServiceException
     */
	protected String getWorkOfflineFiles(String p_accessToken, Long p_taskId,
			int p_workOfflineFileType, String p_workofflineFileTypeOption,
			boolean p_isJson) throws WebServiceException
	{
		// Check work offline file type
		if (p_workOfflineFileType != 1 && p_workOfflineFileType != 2
				&& p_workOfflineFileType != 3 && p_workOfflineFileType != 4
				&& p_workOfflineFileType != 5 && p_workOfflineFileType != 6
				&& p_workOfflineFileType != 7 && p_workOfflineFileType != 8
				&& p_workOfflineFileType != 9 && p_workOfflineFileType != 10
				&& p_workOfflineFileType != 11 && p_workOfflineFileType != 12
				&& p_workOfflineFileType != 13 && p_workOfflineFileType != 14 
				&& p_workOfflineFileType != 15)
		{
			return makeErrorMessage(
					p_isJson,
					GET_WORK_OFFLINE_FILES,
					"Invalid workOfflineFileType "
							+ p_workOfflineFileType
							+ ", it should be limited in 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11,12,13,14 or 15.");
		}

		int workofflineFileTypeOption = -1;
		// Check work offline file type option
		if (StringUtil.isEmpty(p_workofflineFileTypeOption))
		{
			workofflineFileTypeOption = 1;
		}
		else
		{
			try
			{
				Assert.assertIsInteger(p_workofflineFileTypeOption);
			}
			catch (Exception e)
			{
				return makeErrorMessage(
						p_isJson,
						GET_WORK_OFFLINE_FILES,
						"Invalid workofflineFileTypeOption format : "
								+ p_workofflineFileTypeOption
								+ ", it should be limited in 1, 2, 3, 4, 5, 6 or empty");
			}

			workofflineFileTypeOption = Integer
					.parseInt(p_workofflineFileTypeOption);
			if (workofflineFileTypeOption != 1
					&& workofflineFileTypeOption != 2
					&& workofflineFileTypeOption != 3
					&& workofflineFileTypeOption != 4
					&& workofflineFileTypeOption != 5
					&& workofflineFileTypeOption != 6)
			{
				return makeErrorMessage(
						p_isJson,
						GET_WORK_OFFLINE_FILES,
						"Invalid workofflineFileTypeOption : "
								+ p_workofflineFileTypeOption
								+ ", it should be limited in 1, 2, 3, 4, 5, 6 or empty.");
			}
		}

		if (p_workOfflineFileType == 1 || p_workOfflineFileType == 2
				|| p_workOfflineFileType == 3 || p_workOfflineFileType == 4
				|| p_workOfflineFileType == 5 || p_workOfflineFileType == 6
				|| p_workOfflineFileType == 7
				|| p_workOfflineFileType == 14 || p_workOfflineFileType == 15)
		{
			return getWorkOfflineFiles(p_accessToken, p_taskId,
					p_workOfflineFileType, p_isJson);
		}

		Task task = null;
		try
		{
			task = ServerProxy.getTaskManager().getTask(p_taskId);
		}
		catch (Exception e)
		{
			logger.warn("Can not get task info by taskId " + p_taskId);
		}

		if (task == null)
		{
			return makeErrorMessage(p_isJson, GET_WORK_OFFLINE_FILES,
					"Can not find task by taskId " + p_taskId);
		}

		if (task.getState() != Task.STATE_ACCEPTED)
		{
			return makeErrorMessage(p_isJson, GET_WORK_OFFLINE_FILES,
					"This task should be in ACCEPTED state.");
		}

		try
		{
			Activity act = ServerProxy.getJobHandler().getActivity(
					task.getTaskName());
			if (act != null && act.getType() == Activity.TYPE_REVIEW)
			{
				if (p_workOfflineFileType == 8 || p_workOfflineFileType == 9
						|| p_workOfflineFileType == 10
						|| p_workOfflineFileType == 11
						|| p_workOfflineFileType == 12
						|| p_workOfflineFileType == 13)
				{
					return makeErrorMessage(
							p_isJson,
							GET_WORK_OFFLINE_FILES,
							"The task type is review status,can't download when workOfflineFileType are 8,9,10,11,12,13.");
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		User loggedUser = getUser(getUsernameFromSession(p_accessToken));
		String userId = loggedUser.getUserId();
		ProjectImpl project = getProjectByTask(task);
		if (!userId.equals(task.getAcceptor())
				&& !userId.equals(project.getProjectManagerId()))
		{
			return makeErrorJson(
					GET_WORK_OFFLINE_FILES,
					"This task belongs to user "
							+ UserUtil.getUserNameById(task.getAcceptor())
							+ ", current logged user has no previlege to handle it.");
		}

		String returning = "";
		String companyName = CompanyWrapper.getCompanyNameById(task
				.getCompanyId());
		WebServicesLog.Start activityStart = null;
		try
		{
			Map<Object, Object> activityArgs = new HashMap<Object, Object>();
			activityArgs.put("loggedUserName", loggedUser.getUserName());
			activityArgs.put("taskId", p_taskId);
			activityArgs.put("workOfflineFileType", p_workOfflineFileType);
			activityStart = WebServicesLog.start(Ambassador4Falcon.class,
					"getWorkOfflineFiles", activityArgs);

			OfflineEditManager oem = ServerProxy.getOfflineEditManager();
			String fileType = null;
			// 8:Biligual Trados RTF.
			if (p_workOfflineFileType == 8)
			{
				fileType = OfflineConstants.FORMAT_RTF_TRADOS_OPTIMIZED;
			}
			// 9:Trados 7 TTX
			else if (p_workOfflineFileType == 9)
			{
				fileType = OfflineConstants.FORMAT_TTX_VALUE;
			}
			// 10:OmegaT
			else if (p_workOfflineFileType == 10)
			{
				fileType = OfflineConstants.FORMAT_OMEGAT_VALUE;
			}
			// 11:XLiff 1.2
			else if (p_workOfflineFileType == 11)
			{
				fileType = OfflineConstants.FORMAT_XLF_NAME_12;
			}
			// 12:Xliff 2.0
			else if (p_workOfflineFileType == 12)
			{
				fileType = OfflineConstants.FORMAT_XLF_VALUE_20;
			}
			// 13:RTF List view
			else if (p_workOfflineFileType == 13)
			{
				fileType = OfflineConstants.FORMAT_RTF;
			}
			DownloadParams params = getDownloadParams(userId, task, fileType,
					workofflineFileTypeOption);
			File file = oem.getDownloadOfflineFiles(userId, task.getId(),
					params);
			StringBuffer root = new StringBuffer();
			root.append(AmbassadorUtil.getCapLoginOrPublicUrl()).append(
					"/DownloadOfflineKit");
			if (task.getCompanyId() != 1)
			{
				root.append("/").append(companyName);
			}
			root.append("/GlobalSight/CustomerDownload/")
					.append(file.getName());
			String fileUrl = root.toString();

			if (p_isJson)
			{
				JSONObject jsonObj = new JSONObject();
				jsonObj.put("path", fileUrl);
				jsonObj.put("taskId", task.getId());
				jsonObj.put("targetLocale", task.getTargetLocale().toString());
				jsonObj.put("acceptorUserId", task.getAcceptor());
				returning = jsonObj.toString();
			}
			else
			{
				returning = fileUrl;
			}
		}
		catch (Exception e)
		{
			logger.error(e);
			String message = "Error when generate translation kit or report.";
			return makeErrorMessage(p_isJson, GET_WORK_OFFLINE_FILES, message);
		}
		finally
		{
			if (activityStart != null)
			{
				activityStart.end();
			}
		}

		return returning;
	}
	
	private DownloadParams getDownloadParams(String userId, Task task,
			String fileType, int workofflineFileTypeOption)
	{
		SendDownloadFileHelper helper = new SendDownloadFileHelper();
		List<Long> pageIdList = new ArrayList<Long>();
		List<String> pageNameList = new ArrayList<String>();
		List<Boolean> canUseUrlList = new ArrayList<Boolean>();
		helper.getAllPageIdList(task, pageIdList, pageNameList);
		if (pageIdList != null && pageIdList.size() <= 0)
		{
			pageIdList = null;
			pageNameList = null;
		}

		if (pageIdList != null)
		{
			for (int i = 0; i < pageIdList.size(); i++)
			{
				canUseUrlList.add(Boolean.FALSE);
			}
		}
		List primarySourceFiles = helper.getAllPSFList(task);
		List stfList = helper.getAllSTFList(task);
		List supportFileList = helper.getAllSupportFileList(task);
		int editorId = helper.getEditorId(null);
		int platformId = helper.getPlatformId(null, editorId);
		User user = UserUtil.getUserById(userId);
		String uiLocale = user.getDefaultUILocale();
		L10nProfile l10nProfile = task.getWorkflow().getJob().getL10nProfile();
		Vector excludeTypes = l10nProfile.getTranslationMemoryProfile()
				.getJobExcludeTuTypes();

		// Placeholder Format(Deafult--compact)
		int ptagFormat = helper.getPtagFormat(OfflineConstants.PTAG_COMPACT);
		// Allow Edit Locked Segments(Deafult--Allow Edit of ICE and 100% matches)
		int TMEditType = helper.getEditAllState("1", l10nProfile);
		// TM Options(Deafult--TMX File - 1.4b)
		int resInsMode = helper
				.getResourceInsertionMode(OfflineConstants.RES_INS_TMX_14B);
		// Format
		int fileFormat = helper.getFileFormat(fileType);

		DownloadParams downloadParams = new DownloadParams(task.getJobName(),
				null, "", Long.toString(task.getWorkflow().getId()),
				Long.toString(task.getId()), pageIdList, pageNameList,
				canUseUrlList, primarySourceFiles, stfList, editorId,
				platformId, null, ptagFormat, uiLocale, task.getSourceLocale(),
				task.getTargetLocale(), true, fileFormat, excludeTypes,
				TMEditType, supportFileList, resInsMode, user);

		// 7:Biligual Trados RTF.
		if (fileType.equals(OfflineConstants.FORMAT_RTF_TRADOS_OPTIMIZED))
		{
			downloadParams.setPopulate100(true);
			// Penalized Reference TM Options
			downloadParams.setPenalizedReferenceTmPre(true);
			downloadParams.setPenalizedReferenceTmPer(false);
		}
		// 8:Trados 7 TTX
		else if (fileType.equals(OfflineConstants.FORMAT_TTX_VALUE))
		{
			downloadParams.setPopulate100(true);
			// Penalized Reference TM Options
			downloadParams.setPenalizedReferenceTmPre(true);
			downloadParams.setPenalizedReferenceTmPer(false);
		}
		// 9:OmegaT
		else if (fileType.equals(OfflineConstants.FORMAT_OMEGAT_VALUE))
		{
			downloadParams.setPopulate100(false);
			// Penalized Reference TM Options
			downloadParams.setPenalizedReferenceTmPre(false);
			downloadParams.setPenalizedReferenceTmPer(false);
		}
		// 10:XLiff 1.2
		else if (fileType.equals(OfflineConstants.FORMAT_XLF_NAME_12))
		{
			downloadParams.setPopulate100(true);
			// Penalized Reference TM Options
			downloadParams.setPenalizedReferenceTmPre(true);
			downloadParams.setPenalizedReferenceTmPer(false);
		}
		// 11:Xliff 2.0
		else if (fileType.equals(OfflineConstants.FORMAT_XLF_VALUE_20))
		{
			downloadParams.setPopulate100(true);
			// Penalized Reference TM Options
			downloadParams.setPenalizedReferenceTmPre(true);
			downloadParams.setPenalizedReferenceTmPer(false);
		}
		// 12:RTF List view
		else if (fileType.equals(OfflineConstants.FORMAT_RTF))
		{
			downloadParams.setPopulate100(true);
			// Penalized Reference TM Options
			downloadParams.setPenalizedReferenceTmPre(true);
			downloadParams.setPenalizedReferenceTmPer(false);
		}
		Activity act = new Activity();
		try
		{
			act = ServerProxy.getJobHandler().getActivity(task.getTaskName());
		}
		catch (Exception e)
		{
		}
	
		downloadParams.setActivityType(act.getDisplayName());
		downloadParams.setJob(task.getWorkflow().getJob());
		// Terminology
		downloadParams.setTermFormat(OfflineConstants.TERM_TBX);
		downloadParams.setDisplayExactMatch(null);
		downloadParams.setConsolidateTermFiles(false);
		downloadParams.setConsolidateTmxFiles(false);
		// Include XML Node Context Information 9 10 11
		downloadParams.setIncludeXmlNodeContextInformation(false);
		// populate fuzzy target segments = no
		downloadParams.setPopulateFuzzy(false);

		// 1 : consolidate/split = split per file, include repeated segments =
		// no (Default)
		if (workofflineFileTypeOption == 1)
		{
			// Consolidate/Split Type
			downloadParams.setConsolidateFileType("notConsolidate");
			// Include Repeated Segments as Separate File
			downloadParams.setIncludeRepetitions(false);
			// Preserve Source Folder Structure
			downloadParams.setPreserveSourceFolder(true);
		}
		// 2 : consolidate/split = consolidate (overrides preserve folder
		// structure setting),include repeated segments = no
		else if (workofflineFileTypeOption == 2)
		{
			// Consolidate/Split Type
			downloadParams.setConsolidateFileType("consolidate");
			// Include Repeated Segments as Separate File
			downloadParams.setIncludeRepetitions(false);
			// Preserve Source Folder Structure
			downloadParams.setPreserveSourceFolder(false);
		}
		// 3 : consolidate/split = split per wordcount of 2000, include repeated
		// segments = no
		else if (workofflineFileTypeOption == 3)
		{
			// Consolidate/Split Type
			downloadParams.setConsolidateFileType("consolidateByWordCount");
			// Include Repeated Segments as Separate File
			downloadParams.setIncludeRepetitions(false);
			downloadParams.setWordCountForDownload(2000);
			// Preserve Source Folder Structure
			downloadParams.setPreserveSourceFolder(true);
		}
		// 4 : consolidate/split = split per file, include repeated segments =
		// yes
		else if (workofflineFileTypeOption == 4)
		{
			// Consolidate/Split Type
			downloadParams.setConsolidateFileType("notConsolidate");
			// Preserve Source Folder Structure
			downloadParams.setPreserveSourceFolder(true);
			// Include Repeated Segments as Separate File
			if (!fileType.equals(OfflineConstants.FORMAT_TTX_VALUE))
			{
				downloadParams.setIncludeRepetitions(true);
			}
			else
			{
				downloadParams.setIncludeRepetitions(false);
			}
		}
		// 5 : consolidate/split = consolidate (overrides preserve folder
		// structure setting),include repeated segments = yes
		else if (workofflineFileTypeOption == 5)
		{
			// Consolidate/Split Type
			downloadParams.setConsolidateFileType("consolidate");
			// Preserve Source Folder Structure
			downloadParams.setPreserveSourceFolder(false);
			// Include Repeated Segments as Separate File
			if (!fileType.equals(OfflineConstants.FORMAT_TTX_VALUE))
			{
				downloadParams.setIncludeRepetitions(true);
			}
			else
			{
				downloadParams.setIncludeRepetitions(false);
			}
		}
		// 6 : consolidate/split = split per wordcount of 2000, include repeated
		// segments = yes
		else if (workofflineFileTypeOption == 6)
		{
			// Consolidate/Split Type
			downloadParams.setConsolidateFileType("consolidateByWordCount");
			// Preserve Source Folder Structure
			downloadParams.setPreserveSourceFolder(true);
			downloadParams.setWordCountForDownload(2000);
			// Include Repeated Segments as Separate File
			if (!fileType.equals(OfflineConstants.FORMAT_TTX_VALUE))
			{
				downloadParams.setIncludeRepetitions(true);
			}
			else
			{
				downloadParams.setIncludeRepetitions(false);
			}
		}

		// NOT include fully leveraged file(s)
		downloadParams.setExcludeFullyLeveragedFiles(false);
		// Creation ID to "MT!"
		downloadParams.setChangeCreationIdForMTSegments(true);

		return downloadParams;
	}
	
    String uploadWorkOfflineFiles(String p_accessToken, Long p_taskId,
            int p_workOfflineFileType, String p_fileName, byte[] p_bytes,
            boolean p_isJson) throws WebServiceException
    {
        if (StringUtil.isEmpty(p_fileName))
        {
            return makeErrorMessage(p_isJson, UPLOAD_WORK_OFFLINE_FILES,
                    "Empty file name");
        }

        // Check work offline file type
        if (p_workOfflineFileType != 1 && p_workOfflineFileType != 2)
        {
            return makeErrorMessage(
                    p_isJson,
                    UPLOAD_WORK_OFFLINE_FILES,
                    "Invalid workOfflineFileType "
                            + p_workOfflineFileType
                            + ", it should be limited in 1(for Reviewer Comments Report, Translations Edit Report, Post-Review QA Report or Translation Verification Report) "
                            + "or 2(for Offline Translation Kit).");
        }

        Task task = null;
        try
        {
            task = ServerProxy.getTaskManager().getTask(p_taskId);
        }
        catch (Exception e)
        {
            logger.warn("Can not get task info by taskId " + p_taskId);
        }
        if (task == null)
        {
            return makeErrorMessage(p_isJson, UPLOAD_WORK_OFFLINE_FILES,
                    "Can not find task by taskId " + p_taskId);
        }

        if (task.getState() != Task.STATE_ACCEPTED)
        {
            return makeErrorMessage(p_isJson, UPLOAD_WORK_OFFLINE_FILES,
                    "This task is not in ACCEPTED state, not allowed to upload translation kit or report.");
        }

        if (p_workOfflineFileType == 1 && !p_fileName.endsWith(".xls")
                && !p_fileName.endsWith(".xlsx"))
        {
            return makeErrorMessage(p_isJson, UPLOAD_WORK_OFFLINE_FILES,
                    "You are trying to upload a report file (excel file), but the file name extension is neither 'xls' nor 'xlsx'.");
        }

        User loggedUser = getUser(getUsernameFromSession(p_accessToken));
        try
        {
            long userCompanyId = ServerProxy.getJobHandler()
                    .getCompany(loggedUser.getCompanyName()).getId();
            ProjectImpl project = getProjectByTask(task);
            if (userCompanyId != 1 && userCompanyId != project.getCompanyId())
            {
                return makeErrorMessage(p_isJson, UPLOAD_WORK_OFFLINE_FILES,
                        "Current logged user has no previlege to upload file as it is neither in super company nor in company of specified task.");
            }
        }
        catch (Exception e)
        {
        }

        WebServicesLog.Start activityStart = null;
        FileOutputStream fos = null;
        String returning = "";
        try
        {
            String identifyKey = AmbassadorUtil.getRandomFeed();

            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", loggedUser.getUserName());
            activityArgs.put("taskId", p_taskId);
            activityArgs.put("workOfflineFileType", p_workOfflineFileType);
            activityArgs.put("fileName", p_fileName);
            activityStart = WebServicesLog.start(Ambassador4Falcon.class,
                            "uploadWorkOfflineFiles", activityArgs);

            File tmpSaveFile = null;
            String fsDirPath = AmbFileStoragePathUtils
                    .getFileStorageDirPath(task.getCompanyId());
            StringBuffer parentPath = new StringBuffer();
            parentPath.append(fsDirPath).append("/GlobalSight/tmp/")
                    .append(identifyKey).append("_taskID").append(p_taskId);
            tmpSaveFile = new File(parentPath.toString(), p_fileName);
            tmpSaveFile.getParentFile().mkdirs();
            fos = new FileOutputStream(tmpSaveFile, true);
            fos.write(p_bytes);

            if (p_isJson) {
                JSONObject obj = new JSONObject();
                obj.put("identifyKey", identifyKey);
                obj.put(UPLOAD_WORK_OFFLINE_FILES, "");
                returning = obj.toString();
            } else {
                returning = identifyKey;
            }
        }
        catch (Exception e)
        {
            logger.error(e);
            String message = "Error when save uploaded file to specified directory.";
            return makeErrorMessage(p_isJson, UPLOAD_WORK_OFFLINE_FILES, message);
        }
        finally
        {
            try
            {
                if (fos != null)
                    fos.close();
            }
            catch (IOException e)
            {
            }
            if (activityStart != null)
            {
                activityStart.end();
            }
        }

        return returning;
    }

    String importWorkOfflineFiles(String p_accessToken, Long p_taskId,
            String p_identifyKey, int p_workOfflineFileType, boolean p_isJson)
            throws WebServiceException
    {
        String repName = null;
        if (StringUtil.isEmpty(p_identifyKey))
        {
            return makeErrorMessage(p_isJson, IMPORT_WORK_OFFLINE_FILES, "Empty parameter identifyKey");
        }

        // Check work offline file type
        if (p_workOfflineFileType != 1 && p_workOfflineFileType != 2)
        {
            return makeErrorMessage(p_isJson, IMPORT_WORK_OFFLINE_FILES,
                    "Invalid workOfflineFileType "
                            + p_workOfflineFileType
                            + ", it should be limited in 1(for Reviewer Comments Report, Translations Edit Report, Post-Review QA Report or Translation Verification Report) or 2(for Offline Translation Kit).");
        }

        Task task = null;
        try
        {
            task = ServerProxy.getTaskManager().getTask(p_taskId);
        }
        catch (Exception e)
        {
            logger.warn("Can not get task info by taskId " + p_taskId);
        }
        if (task == null)
        {
            return makeErrorMessage(p_isJson, IMPORT_WORK_OFFLINE_FILES,
                    "Can not find task by taskId " + p_taskId);
        }
        if (task.getState() != Task.STATE_ACCEPTED)
        {
            return makeErrorMessage(p_isJson, IMPORT_WORK_OFFLINE_FILES,
                    "This task is not in ACCEPTED state, not allowed to upload translation kit or report.");
        }

        User loggedUser = getUser(getUsernameFromSession(p_accessToken));
        try
        {
            long userCompanyId = ServerProxy.getJobHandler()
                    .getCompany(loggedUser.getCompanyName()).getId();
            ProjectImpl project = getProjectByTask(task);
            if (userCompanyId != 1 && userCompanyId != project.getCompanyId())
            {
                return makeErrorMessage(p_isJson, UPLOAD_WORK_OFFLINE_FILES,
                        "Current logged user has no previlege to upload file as it is neither in super company nor in company of specified task.");
            }
        }
        catch (Exception e)
        {
        }

        File tmpSaveFile = null;
        String fsDirPath = AmbFileStoragePathUtils.getFileStorageDirPath(task
                .getCompanyId());
        StringBuffer parentPath = new StringBuffer();
        parentPath.append(fsDirPath).append("/GlobalSight/tmp/")
                .append(p_identifyKey).append("_taskID").append(p_taskId);
        File saveDir = new File(parentPath.toString());
        if (saveDir.exists() && saveDir.isDirectory())
        {
            File[] subFiles = saveDir.listFiles();
            for (int i = 0; i < subFiles.length; i++)
            {
                File subFile = subFiles[i];
                if (subFile.exists() && subFile.isFile())
                {
                    tmpSaveFile = subFile;
                    break;
                }
            }
        }
        if (tmpSaveFile == null)
        {
            return makeErrorMessage(p_isJson, IMPORT_WORK_OFFLINE_FILES,
                    "Can not find the uploaded file for taskId " + p_taskId
                            + " and identifyKey " + p_identifyKey);
        }
        if (p_workOfflineFileType == 1)
        {
            try
            {
                FileInputStream fis = new FileInputStream(tmpSaveFile);
                Workbook wb = ExcelUtil.getWorkbook(tmpSaveFile.getAbsolutePath(), fis);
                Sheet sheet = ExcelUtil.getDefaultSheet(wb);
                repName = sheet.getRow(0).getCell(0).toString();
                if (!"Translation Edit Report".equalsIgnoreCase(repName)
                        && !"Reviewers Comments Report"
                                .equalsIgnoreCase(repName)
                        && !"Reviewers Comments Report (Simplified)"
                                .equalsIgnoreCase(repName)
                        && !"Post-Review QA Report".equalsIgnoreCase(repName)
                        && !"Translation Verification Report".equalsIgnoreCase(repName))
                {
                    return makeErrorMessage(p_isJson, UPLOAD_WORK_OFFLINE_FILES,
                            "The file is none of Translation Edit Report, Reviewers Comments Report, Post-Review QA Report or Translation Verification Report file.");
                }
            }
            catch (Exception e)
            {
                return makeErrorMessage(p_isJson, UPLOAD_WORK_OFFLINE_FILES,
                        "The file is none of Translation Edit Report, Reviewers Comments Report, Post-Review QA Report or Translation Verification Report file.");
            }
        }

        WebServicesLog.Start activityStart = null;
        try
        {
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("loggedUserName", loggedUser.getUserName());
            activityArgs.put("taskId", p_taskId);
            activityArgs.put("workOfflineFileType", p_workOfflineFileType);
            activityStart = WebServicesLog.start(Ambassador4Falcon.class,
                    "importWorkOfflineFiles", activityArgs);

            OfflineEditManager OEM = ServerProxy.getOfflineEditManager();
            OEM.attachListener(new OEMProcessStatus());

            if (p_workOfflineFileType == 1)
            {
                String reportName = WebAppConstants.TRANSLATION_EDIT; 
                if ("Reviewers Comments Report".equalsIgnoreCase(repName)
                        || "Reviewers Comments Report (Simplified)"
                                .equalsIgnoreCase(repName))
                {
                    reportName = WebAppConstants.LANGUAGE_SIGN_OFF;
                }
                else if ("Post-Review QA Report".equalsIgnoreCase(repName))
                {
                    reportName = WebAppConstants.POST_REVIEW_QA;
                }
                else if ("Translation Verification Report"
                        .equalsIgnoreCase(repName))
                {
                    reportName = WebAppConstants.TRANSLATION_VERIFICATION;
                }
                // Process uploading in same thread, not use separate thread so
                // that error message can be returned to invoker.
                String errMsg = OEM.runProcessUploadReportPage(tmpSaveFile,
                        loggedUser, task, tmpSaveFile.getName(), reportName);
                if (StringUtil.isNotEmpty(errMsg))
                {
                    errMsg = errMsg.replaceAll("</?\\w+>", "").replace("&nbsp;", "");
                    // remove all html tags from error message.
                    return makeErrorMessage(p_isJson,
                            IMPORT_WORK_OFFLINE_FILES, errMsg);
                }
            }
            else if (p_workOfflineFileType == 2)
            {
                String errMsg = OEM.runProcessUploadPage(tmpSaveFile,
                        loggedUser, task, tmpSaveFile.getName());
                if (StringUtil.isNotEmpty(errMsg))
                {
                    return makeErrorMessage(p_isJson, IMPORT_WORK_OFFLINE_FILES,
                            errMsg.replaceAll("</?\\w+>", ""));                    
                }
            }
        }
        catch (Exception e)
        {
            logger.error(e);
            return makeErrorMessage(p_isJson, IMPORT_WORK_OFFLINE_FILES,
                    "Error when import offline kit or report with exception message "
                            + e.getMessage());
        }
        finally
        {
            if (activityStart != null)
            {
                activityStart.end();
            }
        }

        return "";
    }

    private ProjectImpl getProjectByTask(Task task)
    {
        ProjectImpl project = null;
        try
        {
            project = (ProjectImpl) ServerProxy.getProjectHandler()
                    .getProjectByNameAndCompanyId(task.getProjectName(),
                            task.getCompanyId());
        }
        catch (Exception e)
        {
            logger.warn("Error when get project by task" + e.getMessage());
        }
        return project;
    }

    private String makeErrorMessage(boolean p_isJson, String p_method,
            String p_message)
    {
        if (p_isJson)
        {
            return makeErrorJson(p_method, p_message);
        }
        else
        {
            return makeErrorXml(p_method, p_message);
        }
    }

	private String stringArr2Str(String[] arr)
	{
		StringBuffer sb = new StringBuffer();
		if (arr == null || arr.length == 0) return "";

		for (int i = 0; i < arr.length; i++)
		{
			if (i < arr.length - 1)
			{
				sb.append(arr[i]).append(",");
			}
			else
			{
				sb.append(arr[i]);
			}
		}
		return sb.toString();
	}
}
