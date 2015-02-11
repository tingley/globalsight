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
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TimeZone;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.jbpm.taskmgmt.exe.TaskInstance;

import com.globalsight.everest.foundation.Timestamp;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.PrimaryFile;
import com.globalsight.everest.secondarytargetfile.SecondaryTargetFile;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.taskmanager.TaskInfo;
import com.globalsight.everest.util.comparator.TaskInstanceComparator;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.webapp.pagehandler.administration.workflow.WorkflowTemplateConstants;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.workflow.WorkflowConstants;
import com.globalsight.everest.workflow.WorkflowInstance;
import com.globalsight.everest.workflowmanager.TaskJbpmUtil;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.util.SortUtil;

/**
 * The WorkflowActivitiesHandler provides data to the workflowActivities.jsp
 * page which provides a read-only version of the graphical workflow and list a
 * history of the workflow activities (only completed and current active ones).
 */
public class WorkflowActivitiesHandler extends PageHandler
{

    private static final Logger CATEGORY = Logger
            .getLogger(WorkflowActivitiesHandler.class.getName());

    /**
     * Invokes this EntryPageHandler object
     * <p>
     * 
     * @param p_ageDescriptor
     *            the description of the page to be produced.
     * @param p_request
     *            original request sent from the browser.
     * @param p_response
     *            original response object.
     * @param p_context
     *            the Servlet context.
     */
    public void invokePageHandler(WebPageDescriptor p_thePageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            RemoteException, EnvoyServletException
    {
        String action = p_request.getParameter("action");
        try
        {
            HttpSession session = p_request.getSession(false);
            String wfIdParam = p_request
                    .getParameter(JobManagementHandler.WF_ID);
            long wfId = Long.parseLong(wfIdParam);

            SessionManager sessionMgr = (SessionManager) p_request.getSession(
                    false).getAttribute(SESSION_MANAGER);
            Locale uiLocale = (Locale) session.getAttribute(UILOCALE);
            TimeZone timeZone = (TimeZone) session.getAttribute(USER_TIME_ZONE);
            String jobIdParam = p_request
                    .getParameter(JobManagementHandler.JOB_ID);
            Job job = WorkflowHandlerHelper.getJobById(Long
                    .parseLong(jobIdParam));

            List<Workflow> workflows = new ArrayList<Workflow>(
                    job.getWorkflows());

            // Find the workflow whose activities you want to display
            Workflow curWf = null;
            for (int i = 0; i < workflows.size(); i++)
            {
                Workflow wf = (Workflow) workflows.get(i);

                if (wfId == wf.getId())
                {
                    curWf = wf;
                    break;
                }
            }
            WorkflowInstance wfi = WorkflowHandlerHelper
                    .getWorkflowInstance(wfId);
            if (action != null && "view".equals(action))
            {
                List taskInfos = (List) sessionMgr
                        .getAttribute(WorkflowConstants.TASK_INFOS);
                String taskIdParam = p_request.getParameter("taskId");
                long taskId = Long.parseLong(taskIdParam);

                TaskInfo taskInfo = ServerProxy.getWorkflowManager()
                        .getTaskInfoByTaskId(curWf, taskInfos, taskId, true);
                p_request.setAttribute(WorkflowConstants.TASK_INFO, taskInfo);
            }
            else
            {
                ArrayList taskInfos = (ArrayList) ServerProxy
                        .getWorkflowServer().timeDurationsInDefaultPath(wfId,
                                -1, null, wfi);
                sessionMgr
                        .setAttribute(WorkflowConstants.TASK_INFOS, taskInfos);
            }

            p_request.setAttribute("workflow", curWf);
            p_request.setAttribute(JobManagementHandler.WF_ID, wfIdParam);
            p_request.setAttribute(JobManagementHandler.JOB_ID, jobIdParam);

            Hashtable tasks = curWf.getTasks();
            // store info in session for the applet purposes
            storeInfoInSessionManager(p_request, wfi);

            p_request.setAttribute(JobManagementHandler.WF_NAME, curWf
                    .getTargetLocale().getDisplayName(uiLocale));

            p_request.setAttribute(
                    JobManagementHandler.WORKFLOW_ACTIVITIES_SCRIPTLET,
                    getActivitiesText(session, wfi, uiLocale, timeZone));

            p_request
                    .setAttribute(
                            JobManagementHandler.WORKFLOW_PRIMARY_UNEXTRACTED_TARGET_FILES,
                            curWf.getTargetPages(PrimaryFile.UNEXTRACTED_FILE));

            p_request.setAttribute(
                    JobManagementHandler.WORKFLOW_SECONDARY_TARGET_FILES,
                    new ArrayList<SecondaryTargetFile>(curWf
                            .getSecondaryTargetFiles()));

            // now call the JSP
            super.invokePageHandler(p_thePageDescriptor, p_request, p_response,
                    p_context);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Invokes this page handler for an applet request object.
     * 
     * @param p_isGet
     *            - Determines whether the request is a get or post.
     * @param thePageDescriptor
     *            the description of the page to be produced
     * @param theRequest
     *            the original request sent from the browser
     * @param theResponse
     *            the original response object
     * @param context
     *            the Servlet context
     * @return A vector of serializable objects to be passed to applet.
     */
    @SuppressWarnings("rawtypes")
    public Vector invokePageHandlerForApplet(boolean p_isDoGet,
            WebPageDescriptor p_thePageDescriptor,
            HttpServletRequest p_theRequest, HttpServletResponse p_theResponse,
            ServletContext p_context, HttpSession p_session)
            throws ServletException, IOException, EnvoyServletException
    {
        Vector retVal = null;
        if (p_isDoGet)
        {
            retVal = getDisplayData(p_session);
        }

        return retVal;
    }

    private String getActivitiesText(HttpSession p_session,
            WorkflowInstance wfi, Locale p_uiLocale, TimeZone p_timeZone)
            throws EnvoyServletException
    {
        ResourceBundle bundle = getBundle(p_session);
        StringBuilder sb = new StringBuilder();
        Map<TaskInstance, Boolean> taskMap = WorkflowHandlerHelper
                .getWorkflowHistory(wfi.getId());
        if (taskMap.isEmpty())
        {
            return emptyTableMessage(sb, bundle.getString("msg_wf_not_started"));
        }

        Timestamp ts = new Timestamp(p_timeZone);
        ts.setLocale(p_uiLocale);

        Set<TaskInstance> keys = taskMap.keySet();
        List<TaskInstance> tasks = new ArrayList<TaskInstance>(keys.size());
        tasks.addAll(keys);
        SortUtil.sort(tasks, new TaskInstanceComparator(p_uiLocale));
        for (TaskInstance task : tasks)
        {
            String style = "standardText";
            boolean taskCompleted = true;
            Date completedDate = task.getEnd();
            if (completedDate == null)
            {
                // this task has not been completed
                style = "greenText";
                taskCompleted = false;
            }
            ts.setDate(completedDate);

            sb.append("<TR CLASS=" + style + ">");

            // activity name
            sb.append("<TD STYLE=\"padding-right: 10px;\">");
            sb.append(TaskJbpmUtil.getTaskDisplayName(task.getName()));
            sb.append("</TD>\n");

            // roles
            sb.append("<TD STYLE=\"padding-right: 10px;\">");
            String[] roles = TaskJbpmUtil.getActivityRole(wfi, task);
            int numOfRoles = roles == null ? 0 : roles.length;
            if (numOfRoles > 0)
            {
                sb.append(roles[0]);
                for (int i = 1; i < numOfRoles; i++)
                {
                    sb.append("<BR>");
                    sb.append(roles[i]);
                }
            }
            sb.append("</TD>\n");

            // accepter
            sb.append("<TD STYLE=\"padding-right: 10px;\">");
            String acceptor = task.getActorId();
            sb.append(acceptor == null ? "--" : UserUtil
                    .getUserNameById(acceptor));
            sb.append("</TD>");

            // duration
            sb.append("<TD STYLE=\"padding-right: 10px;\">");
            sb.append(TaskJbpmUtil.getActualDuration(task));
            sb.append("</TD>\n");

            // completed date
            sb.append("<TD STYLE=\"padding-right: 10px;\">");
            sb.append(completedDate == null ? "--" : ts.toString());
            sb.append("</TD>\n");

            // status
            sb.append("<TD>");
            sb.append(taskMap.get(task) == true ? bundle
                    .getString("lb_skipped") : (taskCompleted ? bundle
                    .getString("lb_completed_report") : bundle
                    .getString("lb_inprogress")));
            sb.append("</TD>\n");

            sb.append("</TR>\n");
        }
        return sb.toString();
    }

    /**
     * store the info from the first page in the session manager
     * 
     * @param p_request
     * @param p_workflow
     * @exception EnvoyServletException
     */
    private void storeInfoInSessionManager(HttpServletRequest p_request,
            WorkflowInstance p_workflow) throws EnvoyServletException
    {
        SessionManager sessionMgr = getSessionManager(p_request);

        if (p_workflow != null)
        {
            sessionMgr.setAttribute(WorkflowTemplateConstants.WF_INSTANCE,
                    p_workflow);
        }
    }

    /**
     * Get all the info required to be displayed on the graphical workflow UI.
     * Note that only required info for a read-only UI is returned
     * 
     * @param p_request
     * @param p_session
     * @return
     * @exception EnvoyServletException
     */
    private Vector getDisplayData(HttpSession p_session)
            throws EnvoyServletException
    {
        SessionManager sm = (SessionManager) p_session
                .getAttribute(SESSION_MANAGER);
        Vector<Object> objs = new Vector<Object>();

        WorkflowInstance wfi = (WorkflowInstance) sm
                .getAttribute(WorkflowTemplateConstants.WF_INSTANCE);
        // all images and the related flags
        Hashtable<String, Object> imageHash = new Hashtable<String, Object>();
        imageHash.put("gpact", "/images/graphicalworkflow/gpact.gif");
        imageHash.put("gpexit", "/images/graphicalworkflow/gpexit.gif");
        imageHash.put("gpcond", "/images/graphicalworkflow/gpcond.gif");
        imageHash.put("gpand", "/images/graphicalworkflow/gpand.gif");
        imageHash.put("gparrow", "/images/graphicalworkflow/gparrow.gif");
        imageHash.put("pointer", "/images/graphicalworkflow/pointer.gif");
        imageHash.put("gpor", "/images/graphicalworkflow/gpor.gif");
        imageHash.put("gpsub", "/images/graphicalworkflow/gpsub.gif");
        imageHash.put("gpcancel", "/images/graphicalworkflow/gpcancel.gif");
        imageHash.put("gpsave", "/images/graphicalworkflow/gpsave.gif");
        imageHash.put("gpprint", "/images/graphicalworkflow/print.gif");
        imageHash.put("visible", Boolean.FALSE);
        imageHash.put("editMode", Boolean.FALSE);

        objs.addElement(imageHash); // 0
        objs.addElement(""); // 1 (condition node attribute)
        objs.addElement(new Hashtable(0)); // 2 (activity dialog info)
        objs.addElement(wfi); // 3
        objs.addElement(p_session.getAttribute(UILOCALE));// 4
        objs.addElement(Boolean.FALSE); // 5 (with costing)
        return objs;
    }

    // Get a reference to the session manager
    private SessionManager getSessionManager(HttpServletRequest p_request)
    {
        HttpSession session = p_request.getSession(false);

        return (SessionManager) session.getAttribute(SESSION_MANAGER);
    }

    // for a workflow that has not started yet, prepare the text to be displayed
    private String emptyTableMessage(StringBuilder p_sb, String p_message)
    {
        p_sb.append("<TR><TD>\n");
        p_sb.append("<SPAN CLASS=\"standardTextBold\">");
        p_sb.append(p_message);
        p_sb.append("</SPAN>\n");
        p_sb.append("</TD></TR>\n");
        return p_sb.toString();
    }
}
