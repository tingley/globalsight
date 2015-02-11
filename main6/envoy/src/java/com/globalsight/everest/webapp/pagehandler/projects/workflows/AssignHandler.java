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

package com.globalsight.everest.webapp.pagehandler.projects.workflows;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.usermgr.UserInfo;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.workflow.WorkflowInstance;
import com.globalsight.everest.workflow.WorkflowProcessAdapter;
import com.globalsight.everest.workflow.WorkflowTaskInstance;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.util.GeneralException;

/**
 * Page handler for getting data for "Assign" users.
 */
public class AssignHandler extends PageHandler
{
    /**
     * Invokes this PageHandler
     * 
     * @param p_pageDescriptor
     *            the page desciptor
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
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);

        String wfId = p_request.getParameter(JobManagementHandler.WF_ID);
        sessionMgr.setAttribute(JobManagementHandler.MODIFY_WF_PARAM, wfId);
        List taskHash = new ArrayList();
        Hashtable taskUserHash = new Hashtable();
        Hashtable taskSelectedUserHash = new Hashtable();
        if (wfId != null)
        {
            try
            {
                WorkflowInstance workflowInstance = WorkflowProcessAdapter
                        .getProcessInstance(Long.parseLong(wfId));
                Workflow workflow = ServerProxy.getWorkflowManager()
                        .getWorkflowByIdRefresh(Long.parseLong(wfId));
                Hashtable tasks = workflow.getTasks();

                // get the NodeInstances of TYPE_ACTIVITY
                List<WorkflowTaskInstance> nodesInPath = workflowInstance
                        .getDefaultPathNode();

                for (WorkflowTaskInstance task : nodesInPath)
                {
                    Task taskInfo = (Task) tasks.get(task.getTaskId());

                    if (taskInfo.reassignable())
                    {
                        taskHash.add(taskInfo);
                    }
                }

                workflow.getId();

                // The display name of target locale for UI
                Locale uiLocale = (Locale) session.getAttribute(UILOCALE);
                sessionMgr.setAttribute("targDisplayName", workflow
                        .getTargetLocale().getDisplayName(uiLocale));

                // source and target locale used for getting container role
                sessionMgr.setAttribute("targLocale", workflow
                        .getTargetLocale().toString());

                sessionMgr.setAttribute("srcLocale", workflow.getJob()
                        .getSourceLocale().toString());

                sessionMgr.setAttribute("wfId", wfId);

                p_request.setAttribute("jobName", workflow.getJob()
                        .getJobName());
                p_request.setAttribute(WebAppConstants.JOB_ID, workflow.getJob().getId()+"");

                updateUsers(taskHash, taskUserHash, taskSelectedUserHash,
                        workflow);
            }
            catch (Exception e)
            {
                throw new EnvoyServletException(e);
            }
        }

        sessionMgr.setAttribute("tasks", taskHash);
        sessionMgr.setAttribute("taskUserHash", taskUserHash);
        sessionMgr.setAttribute("taskSelectedUserHash", taskSelectedUserHash);

        super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                p_context);
    }

    /**
     * Get the list of users for each Review-Only activity.
     */
    @SuppressWarnings("unchecked")
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
            WorkflowTaskInstance wfTask = p_wf.getIflowInstance()
                    .getWorkflowTaskById(taskId);
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
            Set projectUserIds = null;
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
}
