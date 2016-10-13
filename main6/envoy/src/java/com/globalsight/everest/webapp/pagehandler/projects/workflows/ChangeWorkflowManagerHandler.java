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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.LocaleComparator;
import com.globalsight.everest.util.comparator.UserComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.javabean.NavigationBean;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.everest.workflowmanager.WorkflowOwner;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.SortUtil;

/**
 * ChangeWorkflowManagerHandler is the page handler used to reassign Workflow managers.
 */

public class ChangeWorkflowManagerHandler extends PageHandler 
{
    
    //
    // PUBLIC CONSTANTS
    //
    
    // http parameter names
    public static final String WORKFLOW_DATA = "workflow_data";
    public static final String MANAGER_DATA = "manager_data";
    public static final String JOB_NAME = "job_name";    
    public static final String WORKFLOW_MANAGER_ASSIGNMENTS = "wfl_mgrs";    
        
    // keys for the above data hashtables
    public static final String ALL_WORKFLOW_NAMES = "workflow_names";    
    public static final String ALL_WORKFLOW_IDS = "workflow_ids";    
    public static final String ALL_WORKFLOW_MGR_NAMES = "all_workflow_manager_names";
    public static final String ALL_WORKFLOW_MGR_IDS = "all_workflow_manager_ids";
    public static final String ALL_CURRENT_WORKFLOW_MGR_IDS = "all_current_workflow_manager_ids";
    
    //
    // PRIVATE STATIC VARIABLES
    //
    private static final Logger s_logger =
        Logger.
        getLogger(ChangeWorkflowManagerHandler.class.getName());

    private class _workflow_data
    {
        Long m_id = null;
        List m_curMgrs = new ArrayList();
        
        public _workflow_data(Long p_wflId, List p_curMgrs)
        {
          m_id = p_wflId; 
          m_curMgrs = p_curMgrs; 
        }
        
        Long getWorkflowId()  { return m_id; }
        List getManagers()  { return m_curMgrs; }
    }

    /**
     * Invokes this PageHandler
     * <p>
     * @param p_thePageDescriptor the page descriptor
     * @param p_theRequest the original request sent from the browser
     * @param p_theResponse the original response object
     * @param p_context context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_descriptor,
                                  HttpServletRequest p_request,
                                  HttpServletResponse p_response,
                                  ServletContext p_context)
    throws ServletException, IOException, EnvoyServletException
    {
        HttpSession session = p_request.getSession();
        SessionManager sessionMgr =
            (SessionManager)session.getAttribute(SESSION_MANAGER);
        
        performAppropriateOperation(p_request);
                
        dispatchJSP(p_descriptor, p_request, p_response, p_context);
    }

    /**
     * Invoke the correct JSP for this page
     */
    protected void dispatchJSP(WebPageDescriptor p_descriptor,
                               HttpServletRequest p_request,
                               HttpServletResponse p_response,
                               ServletContext p_context)
    throws ServletException, IOException, EnvoyServletException
    {
        HttpSession session = p_request.getSession();
        SessionManager sessionMgr =
            (SessionManager) session.getAttribute(SESSION_MANAGER);
        Locale uiLocale = (Locale)session.getAttribute(UILOCALE);
        User user = (User)sessionMgr.getAttribute(USER);
        String jobIdAsString = (String)p_request.getParameter(JobManagementHandler.JOB_ID);
        Job job = null;
        
        try
        {
            long jobIdAsLong = Long.parseLong(jobIdAsString);
            job = ServerProxy.getJobHandler().getJobById(jobIdAsLong);
        }
        catch (Exception e)
        {
            s_logger.error(e.getMessage(), e);
            throw new EnvoyServletException(e);
        }
 
        linkNavigationBeans(p_descriptor, p_request);          

        sessionMgr.setAttribute(WORKFLOW_DATA, getWorkflowData(p_request,user, uiLocale, job));
        sessionMgr.setAttribute(MANAGER_DATA, getWorkflowManagersByProject(uiLocale, job.getL10nProfile().getProject()));
        sessionMgr.setAttribute(JOB_NAME, job.getJobName());

        //Call parent invokePageHandler() to set link beans and invoke JSP
        super.invokePageHandler(p_descriptor, p_request,
                                p_response,p_context);
    }

    protected void performAppropriateOperation(HttpServletRequest p_request)
        throws EnvoyServletException
    {        
        String assignments[] = p_request.getParameterValues(WORKFLOW_MANAGER_ASSIGNMENTS);
        if (assignments != null  && assignments.length > 0) 
        {    
            reassignWorkflowOwners(assignments);

            // Here we try to stop a refresh from setting old assignments by removing them
            // but this has no affect on the browsers address bar.... so it does not work
            p_request.removeAttribute(WORKFLOW_MANAGER_ASSIGNMENTS);
        }
        else
        {
            // Don't do anything if they are just viewing the table 
            // and not performing an action on a job
            return;
        }
    }
      
    private void linkNavigationBeans(WebPageDescriptor p_descriptor,
                                     HttpServletRequest p_request)
    {
        Enumeration en = p_descriptor.getLinkNames();
        boolean adding = false;
        while (en.hasMoreElements())
        {
            String linkName = (String) en.nextElement();
            String pageName = p_descriptor.getPageName();
            NavigationBean bean = new NavigationBean(linkName, pageName);
            p_request.setAttribute(linkName, bean);
        }
    }

    // create hashtable of workflows names (value) and ids (key)
    private Hashtable getWorkflowData(HttpServletRequest p_request,User p_user, Locale p_uiLocale, Job p_job)
    {
        Hashtable workflowData = new Hashtable();
        Hashtable workflowMap = new Hashtable();
        ArrayList targetLocalesForDisplay_sorted = new ArrayList();
        ArrayList workflowIds_sorted = new ArrayList();
        ArrayList workflowCurMgrIds_sorted = new ArrayList();
        HttpSession session=p_request.getSession(false);
        PermissionSet perms = (PermissionSet) session.getAttribute(WebAppConstants.PERMISSIONS);

        // gather all workflow data
        Collection workflows = p_job.getWorkflows();
        Iterator it = workflows.iterator();                            
        while(it.hasNext())
        {
            Workflow wf = (Workflow) it.next();
            Long wfId = wf.getIdAsLong();
            GlobalSightLocale targetLocale = wf.getTargetLocale();
            String state = wf.getState();

            // only show the workflows that the user is qualified to access and
            // are in the DISPATCHED or READY_TO_BE_DISPATCHED state
            if ((!state.equals(Workflow.DISPATCHED) && 
                !state.equals(Workflow.READY_TO_BE_DISPATCHED)) || 
                invalidForWorkflowOwner(p_user.getUserId(),
                    perms, wf))
            {
                continue;
            } 
            
            List<String> wfOwners = wf.getWorkflowOwnerIds();
            //remove the PM from the list of workflow owners so that cannot be changed
            String thePM = p_job.getL10nProfile().getProject().getProjectManagerId();
            wfOwners.remove(thePM);
            workflowMap.put(targetLocale.getDisplayName(p_uiLocale), 
                            new _workflow_data(wfId, wfOwners));
        }      

        // sort target locale display names
        // - use this code for sorting based on getDisplayName(uiLocale) by passing 2                
        ArrayList targetLocaleNames = new ArrayList(workflowMap.keySet());
        SortUtil.sort(targetLocaleNames, new LocaleComparator(2, p_uiLocale));                

        // use sorted locale names to create sort-aligned data lists for ui
        for(int i=0; i< targetLocaleNames.size(); i++)
        {
            targetLocalesForDisplay_sorted.add(targetLocaleNames.get(i));
            _workflow_data wd = (_workflow_data)workflowMap.get(targetLocaleNames.get(i));
            workflowIds_sorted.add(wd.getWorkflowId());
            workflowCurMgrIds_sorted.add(wd.getManagers());
        }
        workflowData.put(ALL_WORKFLOW_NAMES, targetLocalesForDisplay_sorted);
        workflowData.put(ALL_WORKFLOW_IDS, workflowIds_sorted);
        workflowData.put(ALL_CURRENT_WORKFLOW_MGR_IDS, workflowCurMgrIds_sorted);
        return workflowData;     
    }
 
    // return a list of the workflow managers for this project. This excludes
    // the PM
    private Hashtable getWorkflowManagersByProject(Locale p_locale, Project p_project)
        throws EnvoyServletException
    {
        Hashtable user_map = new Hashtable();
        ArrayList userIds_sorted = new ArrayList();        
        List users = null;
        String thePM = p_project.getProjectManagerId();

        try
        {
            users = ServerProxy.getUserManager().getUsersByFilter(
                null, p_project);

            if (users != null)
            {
                UserComparator uc = new UserComparator(UserComparator.
                                                       DISPLAYNAME,p_locale);
                SortUtil.sort(users, uc);
                Iterator iter = users.iterator();
                while (iter.hasNext())
                {
                    User u = (User) iter.next();
                    PermissionSet perms = Permission.getPermissionManager()
                    .getPermissionSetForUser(u.getUserId());
                    if (thePM.equals(u.getUserId())==false &&
                        perms.getPermissionFor(Permission.PROJECTS_MANAGE_WORKFLOWS))
                    {
                        userIds_sorted.add(u.getUserId());
                    }
                    else
                    {
                        iter.remove();
                    }
                }

                user_map.put(ALL_WORKFLOW_MGR_NAMES, new ArrayList(users));
                user_map.put(ALL_WORKFLOW_MGR_IDS, userIds_sorted);
            }
        }
        catch(Exception e)
        {
            s_logger.error(e.getMessage(), e);            
            throw new EnvoyServletException(e);
        }
        
        return user_map;
    }

    
    //
    // PRIVATE SUPPORT METHODS
    //

    private void reassignWorkflowOwners(String[] p_assignments)
        throws EnvoyServletException
    {
        try
        {
            for(int i=0; i<p_assignments.length; i++)
            {
                StringTokenizer tokenizer = new StringTokenizer(p_assignments[i], " ");
                String workflowId = null;
                ArrayList wfOwners = new ArrayList();
                if(tokenizer.hasMoreTokens())
                {
                    workflowId = tokenizer.nextToken();
                    while(tokenizer.hasMoreTokens())
                    {
                        String userId = tokenizer.nextToken();
                        WorkflowOwner wo = new WorkflowOwner(userId, Permission.GROUP_WORKFLOW_MANAGER);
                        wfOwners.add(wo);
                    }
                    ServerProxy.getWorkflowManager().
                        reassignWorkflowOwners(Long.parseLong(workflowId), 
                                               wfOwners);
    
                }
            }
        }
        catch(Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }
}

