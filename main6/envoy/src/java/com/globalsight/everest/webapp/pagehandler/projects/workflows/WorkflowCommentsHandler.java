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

//javax
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.util.comparator.TaskComparator;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.workflow.WorkflowTaskInstance;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.util.GeneralException;
import com.globalsight.util.SortUtil;

/** The WorkflowCommentsHandler provides data to the workflowComments.jsp page*/
public class WorkflowCommentsHandler extends PageHandler
{
    public static final String WORKFLOW_TASKS_ATTRIBUTE = "wfA";
    public static final String TASKS_ATTRIBUTE = "tA";

    public static final String WORKFLOW_ID_PARAMETER = "wfId";
    public static final String VIEW_COMMENTS_BEAN = "workflowComments";

    /**
     * Invokes this EntryPageHandler object
     * <p>
     * @param p_ageDescriptor the description of the page to be produced.
     * @param p_request original request sent from the browser.
     * @param p_response original response object.
     * @param p_context the Servlet context.
     */
    public void invokePageHandler(WebPageDescriptor p_thePageDescriptor,
                                  HttpServletRequest p_request,
                                  HttpServletResponse p_response,
                                  ServletContext p_context)
    throws ServletException, IOException, EnvoyServletException
    {
        try {
            //get the ID of the Task whose comments we need to display
            long workflowId = Long.parseLong(p_request.getParameter(WORKFLOW_ID_PARAMETER));

            try
            {
                HttpSession session = p_request.getSession();
                SystemConfiguration sc = SystemConfiguration.getInstance();
                String sortOrder = sc.getStringParameter(SystemConfigParamNames.COMMENTS_SORTING);
                session.setAttribute(SystemConfigParamNames.COMMENTS_SORTING, sortOrder);
            }
            catch (GeneralException e)
            {
                System.out.println("Error getting system parameters for comment sorting");
                throw new EnvoyServletException(GeneralException.EX_GENERAL, e);
            }

            // Get all tasks as WorkflowTaskInstances in a Vector
            Vector allTasks = ServerProxy.getWorkflowServer().getTasksForWorkflow(
                workflowId);

            Workflow wf = WorkflowHandlerHelper.getWorkflowById(
                workflowId);
            Hashtable tasks = wf.getTasks();
            Vector sortedTasks = new Vector();

            List taskList = new ArrayList(tasks.values());

            int size = taskList.size();
            HttpSession session = p_request.getSession(false);
            Locale uiLocale = (Locale) session
                    .getAttribute(WebAppConstants.UILOCALE);
            SortUtil.sort(taskList, new TaskComparator(
                    TaskComparator.COMPLETE_DATE, uiLocale));

            //TreeMap sortedWorkflowTasks = new TreeMap();
            Vector activityNames = new Vector();

            for (int i=size-1 ; i>=0; i--)
            {
                Task task = (Task)taskList.get(i);
                for(int x=0; x<allTasks.size(); x++)
                {
                    WorkflowTaskInstance curTask = 
                        (WorkflowTaskInstance)allTasks.elementAt(x);
                    if( curTask.getTaskId() == task.getId())
                    {
                        //Integer seq = new Integer(curTask.getSequence());
                        //sortedWorkflowTasks.put(seq, curTask);
                        sortedTasks.addElement(task);
                        activityNames.addElement(curTask.getActivityName());
                    }
                }
            }
            p_request.setAttribute(WORKFLOW_TASKS_ATTRIBUTE, activityNames);

            p_request.setAttribute(TASKS_ATTRIBUTE, sortedTasks);

            //now call the JSP
            super.invokePageHandler(p_thePageDescriptor,
                                    p_request,
                                    p_response,
                                    p_context);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(GeneralException.EX_GENERAL, e);
        }
    }
}

