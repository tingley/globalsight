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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.ControlFlowHelper;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.workflowmanager.Workflow;

public class EstimatedTranslateCompletionDateHandler extends PageHandler
{
    public static final String PAGE_NUM = "datePageNum";
    public static final String NUM_PER_PAGE_STR = "dateNumPerPage";
    public static final String LIST_SIZE = "dateSize";
    public static final String SORTING = "dateSorting";
    public static final String REVERSE_SORT = "dateReverseSort";
    public static final String LAST_PAGE_NUM = "dateLastPageNum";
    public static final String NUM_PAGES = "dateNumPages";
    public static final String LIST = "workflows";

    /**
     * Invokes this EntryPageHandler object
     * <p>
     * 
     * @param pageDescriptor
     *            the description of the page to be produced.
     * @param request
     *            original request sent from the browser.
     * @param response
     *            original response object.
     * @param context
     *            the Servlet context.
     */
    public void invokePageHandler(WebPageDescriptor pageDescriptor,
            HttpServletRequest request, HttpServletResponse response,
            ServletContext context) throws ServletException, IOException,
            RemoteException, EnvoyServletException
    {
        setup(request);

        super.invokePageHandler(pageDescriptor, request, response, context);
    }

    /**
     * Get data needed for the page.
     */
    public void setup(HttpServletRequest request) throws ServletException,
            IOException, RemoteException, EnvoyServletException
    {
        HttpSession session = request.getSession(false);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        String from = request.getParameter("from");
        if (from != null)
        {
            if ("jobInProgress".equals(from))
            {
                request.setAttribute("from", "jobInProgress");
            }
            else if ("jobReady".equals(from))
            {
                request.setAttribute("from", "jobReady");
            }
        }
        List<Job> jobs = getJobs(request, sessionMgr);
        request.setAttribute("Jobs", jobs);

        initWorkflowTable(request, session, sessionMgr,
                filterValidWorkflows(jobs, request, sessionMgr));
    }

    private void initWorkflowTable(HttpServletRequest request,
            HttpSession session, SessionManager sessionMgr, List wfs)
            throws EnvoyServletException
    {

        Locale uiLocale = (Locale) session
                .getAttribute(WebAppConstants.UILOCALE);

        setTableNavigation(request, session, wfs, new WorkflowComparator(
                uiLocale), wfs.size(), NUM_PER_PAGE_STR, NUM_PAGES, LIST,
                SORTING, REVERSE_SORT, PAGE_NUM, LAST_PAGE_NUM, LIST_SIZE);
    }

    /**
     * Filter valid workflows based on access group (a WFM can only see
     * workflows that they are responsible for).
     */
    private List filterValidWorkflows(List<Job> p_jobs,
            HttpServletRequest p_request, SessionManager p_sessionMgr)
    {
        HttpSession session = p_request.getSession(false);
        PermissionSet perms = (PermissionSet) session
                .getAttribute(WebAppConstants.PERMISSIONS);
        User user = (User) p_sessionMgr.getAttribute(USER);

        List wfmList = new ArrayList();
        
        for (Job job : p_jobs)
        {
            Collection p_workflows = job.getWorkflows();
            if (perms.getPermissionFor(Permission.PROJECTS_MANAGE_WORKFLOWS))
            {
                Iterator iter = p_workflows.iterator();
                while(iter.hasNext())
                {
                    Workflow curWF = (Workflow) iter.next();

                    if (curWF.getState().equals(Workflow.DISPATCHED)
                            || curWF.getState().equals(
                                    Workflow.READY_TO_BE_DISPATCHED))
                    {
                        wfmList.add(curWF);
                    }
                    else
                    {
                        continue;
                    }
                }
            }
        }
        
        return wfmList;
    }

    /**
     * Returns the id of the job that an action is being performed on.
     */
    private List<Job> getJobs(HttpServletRequest request, SessionManager sessionMgr)
    {
        List<Job> jobs = new ArrayList<Job>();

        String jobids = "";
        if (request.getParameter("checkedJobIds") == null)
        {
            String jobid = "";
            if (request.getParameter(JobManagementHandler.JOB_ID) != null)
            {
                jobid = request.getParameter(JobManagementHandler.JOB_ID);
                jobids = jobid;
                sessionMgr.setAttribute("jobIds", jobids);
            }

            jobids = (String) sessionMgr.getAttribute("jobIds");
        }
        else
        {
            jobids = request.getParameter("checkedJobIds");
            sessionMgr.setAttribute("jobIds", jobids);
        }

        if (jobids != null && jobids.trim().length() > 0)
        {
            StringTokenizer st = new StringTokenizer(jobids);
            while (st.hasMoreTokens())
            {
                String jid = st.nextToken();
                Job job = WorkflowHandlerHelper.getJobById(Long.parseLong(jid));
                jobs.add(job);
            }
        }

        return jobs;
    }

    /**
     * Overide getControlFlowHelper so we can do processing and redirect the
     * user correctly.
     * 
     * @return the name of the link to follow
     */
    public ControlFlowHelper getControlFlowHelper(HttpServletRequest p_request,
            HttpServletResponse p_response)
    {
        return new EstimatedTranslateCompletionDateControlFlowHelper(p_request,
                p_response);
    }
}
