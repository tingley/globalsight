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

import org.apache.log4j.Logger;

import com.globalsight.everest.foundation.User;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.GlobalSightLocaleComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.javabean.NavigationBean;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.everest.workflowmanager.WorkflowOwner;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Iterator;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TreeSet;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class SearchJobsHandler
    extends PageHandler
{
    private static Logger s_logger =
        Logger.getLogger(
            SearchJobsHandler.class);

    /**
     * Invokes this EntryPageHandler object
     * <p>
     * @param p_ageDescriptor the description of the page to be produced.
     * @param p_request original request sent from the browser.
     * @param p_response original response object.
     * @param p_context the Servlet context.
     */
    public void invokePageHandler(WebPageDescriptor pageDescriptor,
        HttpServletRequest request, HttpServletResponse response,
        ServletContext context)
        throws ServletException, IOException, RemoteException,
               EnvoyServletException
    {
        String action = (String)request.getParameter("action");

        if ("search".equals(action))
        {
            firstSearch(request);
        }

        super.invokePageHandler(pageDescriptor, request, response, context);
    }

    private void firstSearch(HttpServletRequest request)
        throws ServletException, IOException, RemoteException,
               EnvoyServletException
    {
        HttpSession session = request.getSession(false);
        SessionManager sessionMgr = (SessionManager)session.getAttribute(
            WebAppConstants.SESSION_MANAGER);
        User loginUser = (User)sessionMgr.getAttribute(WebAppConstants.USER);

        // get space separated id's
        String jobIdsString = (String)request.getParameter("search");

        // Get all target locales for all jobs
        Locale uiLocale = (Locale)session.getAttribute(
            WebAppConstants.UILOCALE);

        String[] jobIds = jobIdsString.split(" ");
        ArrayList jobs = new ArrayList();
        String[] languageSet = request.getParameterValues("languageSet");
        boolean fromMyJobs = (languageSet == null) ? true : false;
        TreeSet targetLocales = new TreeSet(
            new GlobalSightLocaleComparator(uiLocale));

        for (int i = 0; i < jobIds.length; i++)
        {
            String id = (String)jobIds[i];

            if (fromMyJobs == true)
            {
                Job job = WorkflowHandlerHelper.getJobById(Long.parseLong(id));
                Collection<Workflow> workflows = job.getWorkflows();
                boolean canAccess = true;
                boolean canAccessAtLeastOne = true;

                for (Workflow wf : workflows)
                {
                    canAccess = userCanAccessWorkflow(loginUser, wf, request);

                    if (canAccess)
                    {
                        canAccessAtLeastOne = true;
                        targetLocales.add(wf.getTargetLocale());
                    }
                }

                if (canAccessAtLeastOne)
                {
                    jobs.add(id);
                }
            }
            else
            {
                jobs.add(id);
            }
        }


        if (fromMyJobs == false)
        {
            // fill in target locales for my activities

            // GSDEF00012724 says that the target locales offered in
            // the activity content search/replace screen is the
            // superset of *all* target locales of all activities
            // displayed, even if the selected activity is only in one
            // locale (you should be PM or LP with multiple locales).
            //
            // While it is possible to suppress showing *all* target
            // locales, since the user can still select *multiple*
            // activities for which target locales must be shown on a
            // single screen, those target locales can be a superset.
            //
            // Example:
            //  act1         de (in job with only DE as target locale)
            //  act2         fr (in job with only FR as target locale)
            //
            // Search replace screen must show: de + fr
            //
            // This case is benign since search/replace will find only
            // segments in existing locales.
            //
            // It becomes a problem however in the case where a job
            // contains activities in locales that the user is allowed
            // to see but that he has not accepted:
            //
            // Example:
            //  act1         de (where DE is accepted by the current user
            //                   and FR is accepted by somebody else)
            //  act2         fr (in job with only FR as target locale)
            //
            // Search replace screen must show: de + fr.
            // --> Current user can change somebody else's locale.

            try
            {
                // TODO: fix GSDEF00012724 by implementing this:
                // - get the job
                // - iterate the workflows
                //   - if workflow is accepted by current user:
                //     - add target locale
                //
                // Then remove the following "for" statement:

                // The languageSet passed in is the superset of all
                // languages of all tasks displayed on the screen.
                for (int i = 0; i < languageSet.length; i++)
                {
                    GlobalSightLocale locale = ServerProxy.getLocaleManager().
                        getLocaleByString(languageSet[i]);
                    targetLocales.add(locale);
                }
            }
            catch (Exception e)
            {
                throw new EnvoyServletException(e);
            }
        }

        sessionMgr.setAttribute("jobIds", jobs);
        sessionMgr.setAttribute("targetLocales", new ArrayList(targetLocales));
    }

    private boolean userCanAccessWorkflow(User p_user, Workflow p_workflow,
        HttpServletRequest p_request)
    {
        PermissionSet perms=(PermissionSet)p_request.getSession(false).getAttribute(WebAppConstants.PERMISSIONS);
        boolean result = false;
        if (perms.getPermissionFor(Permission.JOB_SCOPE_ALL))
        {
            //admins can access everything
            result = true;
        }
        else
        {
            //others can access only if they can manage the workflow
            List workflowOwners = p_workflow.getWorkflowOwners();

            for (Iterator it = workflowOwners.iterator(); it.hasNext(); )
            {
                WorkflowOwner owner = (WorkflowOwner)it.next();
                if (p_user.getUserId().equals(owner.getOwnerId()))
                {
                    result = true;
                    break;
                }
            }
        }

        return result;
    }
}
