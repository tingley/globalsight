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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.util.comparator.JobComparator;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobSearchParameters;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.workflowmanager.Workflow;

public class UpdateLeverageHandler extends PageHandler
{
    private static final Logger logger = 
        Logger
            .getLogger(UpdateLeverageHandler.class);

    /**
     * Invokes this PageHandler
     * 
     * @param p_thePageDescriptor
     *            the page descriptor
     * @param p_theRequest
     *            the original request sent from the browser
     * @param p_theResponse
     *            the original response object
     * @param p_context
     *            context the Servlet context
     * @throws NamingException 
     */
    @SuppressWarnings("unchecked")
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            EnvoyServletException
    {
        List<Job> availableJobs = new ArrayList();
        HttpSession session = p_request.getSession();
        String strTaskId = (String) p_request.getParameter(WebAppConstants.TASK_ID);
        Task task = ServerProxy.getTaskManager().getTask(Long.parseLong(strTaskId));
        
        try {
            long projectId = task.getWorkflow().getJob().getL10nProfile()
                    .getProjectId();

            // Available jobs should be 1) in the same project, 2) this job
            // should also have a workflow with the same target locale, 3) this
            // workflow must be "In Progress" or "Localized"(translation work is
            // in progress or finished, but not populate into storage TM yet).
            JobSearchParameters jobSearchParam = new JobSearchParameters();
            jobSearchParam.setProjectId(String.valueOf(projectId));// for 1)
            jobSearchParam.setSourceLocale(task.getSourceLocale());// for 2)
            jobSearchParam.setTargetLocale(task.getTargetLocale());// for 2)
            Collection jobs = ServerProxy.getJobHandler().getJobs(jobSearchParam);
            Iterator<Job> jobIter = jobs.iterator();
            while (jobIter.hasNext())
            {
                Job job = (Job) jobIter.next();
                Collection<Workflow> wfs = job.getWorkflows();
                for (Workflow wf : wfs)
                {
                    // for 3)
                    if (wf.getTargetLocale().equals(task.getTargetLocale()))
                    {
                        String wfState = wf.getState();
                        if (Workflow.DISPATCHED.equalsIgnoreCase(wfState)
                                || Workflow.LOCALIZED.equalsIgnoreCase(wfState)
                                || Workflow.EXPORT_FAILED
                                        .equalsIgnoreCase(wfState))
                        {
                            availableJobs.add(job);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Fail to get available jobs to update leverage from.", e);
            throw new EnvoyServletException(e);
        }

        Locale uiLocale = (Locale) session.getAttribute(WebAppConstants.UILOCALE);
        Collections.sort(availableJobs, new JobComparator(JobComparator.NAME, uiLocale));
        p_request.setAttribute("availableJobs", availableJobs);
        p_request.setAttribute(WebAppConstants.TASK_ID, strTaskId);
        
        super.invokePageHandler(p_pageDescriptor, p_request, p_response, p_context);
    }

}
