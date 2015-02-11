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

package com.globalsight.everest.webapp.pagehandler.administration.customer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.foundation.ContainerRole;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.WorkflowHandlerHelper;
import com.globalsight.everest.webapp.tags.TableConstants;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.everest.workflow.WorkflowInstance;
import com.globalsight.everest.workflow.WorkflowTaskInstance;
import com.globalsight.everest.workflowmanager.Workflow;


/**
 * This is the handler for displaying all jobs.
 */
public class MyJobsHandler
    extends PageHandler
{
    public final static String MYJOB_LIST = "myjobs";
    public final static String MYJOB_KEY = "myjob";

    /**
     * Invokes this PageHandler
     *
     * @param p_pageDescriptor the page desciptor
     * @param p_request the original request sent from the browser
     * @param p_response the original response object
     * @param p_context context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
        HttpServletRequest p_request, HttpServletResponse p_response,
        ServletContext p_context)
        throws ServletException,
               IOException,
               EnvoyServletException
    {
        HttpSession session = p_request.getSession();
        SessionManager sessionMgr = 
            (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);

        String action = (String)p_request.getParameter("action");
        if ("saveAssign".equals(action))
        {
            doSaveAssign(p_request, sessionMgr);
        }
        List jobs = null;
        if (action != null)
        {
            if (action.equals("search"))
            {
                // clear existing list if doing new search
                sessionMgr.clear();
            }
            else
            {
                // Did not come from main menu - clear session except table information
                jobs = (List)sessionMgr.getAttribute("myJobs");
                clearSessionExceptTableInfo(session, MYJOB_KEY);
                sessionMgr.setAttribute("myJobs", jobs);
            }
        }
        try
        {
            if ((action != null && !action.equals("search")) ||
                p_request.getParameter(MYJOB_KEY + TableConstants.SORTING) != null)
            {
                jobs = (List)sessionMgr.getAttribute("myJobs");
            }
            else
            {
                String username = (String)session.getAttribute(
                    WebAppConstants.USER_NAME);

                Vector stateList = new Vector();
                stateList.add(Job.DISPATCHED);
                stateList.add(Job.LOCALIZED);
                stateList.add(Job.EXPORTED);
                String searchName = null;
                if ("search".equals(action))
                {
                    searchName = p_request.getParameter("nameField");
                }
                jobs = getJobs(new ArrayList(
                    WorkflowHandlerHelper.getJobsByWfManagerIdAndStateList(
                        username, stateList)),
                                 searchName);
                sessionMgr.setAttribute("myJobs", jobs);
            }
            Locale locale = (Locale)session.getAttribute(UILOCALE);
            dataForTable(p_request, session, MYJOB_LIST, MYJOB_KEY,
                          new MyJobComparator(locale), jobs);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
        super.invokePageHandler(p_pageDescriptor, p_request, 
                                p_response, p_context);
    }

    /**
     * Get list of MyJob for displaying in table
     */
    private ArrayList getJobs(List p_jobs, String searchName)
        throws EnvoyServletException
    {
        Hashtable myJobs = new Hashtable();
        if (p_jobs != null)
        {
            for (int i = 0; i < p_jobs.size(); i++)
            {
                Job job = (Job)p_jobs.get(i);
                if (searchName != null && !job.getJobName().startsWith(searchName))
                    continue;
                for (Workflow wf : job.getWorkflows())
                {
                    String wfState = wf.getState();
                    String key = job.getJobName() + wf.getTargetLocale().toString();
                    if (!wfState.equals(Workflow.DISPATCHED) &&
                        !wfState.equals(Workflow.LOCALIZED)  &&
                        !wfState.equals(Workflow.EXPORTED))
                    {
                        //ignore ready to be dispatched workflows as the
                        //customer user shouldn't see it
                        continue;
                    }


                    MyJob myJob = (MyJob)myJobs.get(key);
                    if (myJob == null)
                    {
                        myJob = new MyJob(job.getId(), job.getJobName(),
                                    job.getSourceLocale(), wf.getTargetLocale(),
                                    job.getWordCount(), job.getCreateDate(),
                                    wf.getPlannedCompletionDate());
                        myJobs.put(key, myJob);
                    } 
                    else
                    {
                        myJob.setJobId(job.getId());
                        myJob.setWordCount(job.getWordCount());
                        myJob.setTargetLocale(wf.getTargetLocale());
                        myJob.setCreateDate(job.getCreateDate());
                        myJob.setPlannedDate(wf.getPlannedCompletionDate());
                    }
                }
            }
        }
        Collection myJobList =  myJobs.values();
        return new ArrayList(myJobList); 
    }

    /**
     * Get values from request and session.  Assign the selected user to the task.
     */
    private void doSaveAssign(HttpServletRequest p_request, 
                              SessionManager p_sessionMgr)
        throws EnvoyServletException
    {
        try
        {
            String srcLocale = (String)p_sessionMgr.
                getAttribute("srcLocale");
            String targLocale = (String)p_sessionMgr.
                getAttribute("targLocale");
            Hashtable taskUserHash = (Hashtable)p_sessionMgr.
                getAttribute("taskUserHash");
            ArrayList wfIds = (ArrayList)p_sessionMgr.
                getAttribute("wfIds");

            Enumeration keys = taskUserHash.keys();
            HashMap roleMap = new HashMap();
            while (keys.hasMoreElements())
            {
                String taskname = (String)keys.nextElement();
                String companyId = CompanyThreadLocal.getInstance().getValue();
                Activity activity = ServerProxy.getJobHandler()
                    .getActivityByCompanyId(taskname, companyId);
                ContainerRole containerRole = ServerProxy.getUserManager().
                    getContainerRole(activity, srcLocale, targLocale);
                String userParam = p_request.getParameter(taskname);
                String[] userInfos = userParam.split(",");
                if ("-1".equals(userInfos[0]))
                {
                    // All Qualified Users
                    String[] roles = {containerRole.getName()};
                    roleMap.put(taskname, new NewAssignee(
                        roles, "All qualified users", false));
                }
                else
                {
                    // TBD -- Once the UI supports multiple user selection
                    // this code should be revisited.
                    String[] roles = {containerRole.getName()+" "+userInfos[0]};
                    roleMap.put(taskname, new NewAssignee(
                        roles, userInfos[1], true));
                }
            }

            int size = wfIds == null ? -1 : wfIds.size();
            for (int i = 0; i < size; i++)
            {
                boolean shouldModifyWf = false;
                Long id = (Long)wfIds.get(i);
                WorkflowInstance wi = ServerProxy.getWorkflowServer().
                    getWorkflowInstanceById(id.longValue());

                Vector tasks = wi.getWorkflowInstanceTasks();

                int sz = tasks == null ? -1 : tasks.size();
                for (int j = 0; j < sz ; j++)
                {
                    WorkflowTaskInstance wti = 
                        (WorkflowTaskInstance)tasks.get(j);

                    NewAssignee na = (NewAssignee)roleMap.get(
                        wti.getActivityName());
                    if (na != null && 
                        !areSameRoles(wti.getRoles(), na.m_roles))
                    {
                        shouldModifyWf = true;
                        wti.setRoleType(na.m_isUserRole);
                        wti.setRoles(na.m_roles);
                        wti.setDisplayRoleName(na.m_displayRoleName);
                    }

                }

                // modify one workflow at a time and reset the flag
                if (shouldModifyWf)
                {
                    shouldModifyWf = false;
                    ServerProxy.getWorkflowManager().modifyWorkflow(
                        null, wi, null, null);
                }
            }
        }
        catch(Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    private void dataForTable(HttpServletRequest p_request,
                              HttpSession p_session, String listname, String keyname,
                              MyJobComparator comparator,
                              List p_jobs)
        throws EnvoyServletException
    {
        try
        {
            setTableNavigation(p_request, p_session, p_jobs,
                comparator,
                20,
                listname,
                keyname);
        }
        catch (Exception e)
        {
            // Config exception (already has message key...)
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Determines whether the two array of roles contain the same
     * set of role names.
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

    //////////////////////////////////////////////////////////////////////
    //  Begin: Inner Class
    //////////////////////////////////////////////////////////////////////
    class NewAssignee
    {
        String m_displayRoleName = null;
        String[] m_roles = null;
        boolean m_isUserRole = false;
        NewAssignee(String[] p_roles, 
                    String p_displayRoleName, 
                    boolean p_isUserRole)
        {
            m_displayRoleName = p_displayRoleName;
            m_roles = p_roles;
            m_isUserRole = p_isUserRole;
        }
    }
}
