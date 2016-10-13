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

import com.globalsight.everest.usermgr.UserInfo;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.rmi.RemoteException;


/**
 * Page handler for getting data for "Assign" users.
 */
public class AssignHandler
    extends PageHandler
{
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

        String value = (String)p_request.getParameter("value");
        StringTokenizer st = new StringTokenizer(value, ",");
        String jobName = st.nextToken();
        String targLocale = st.nextToken();
        String srcLocale = st.nextToken();
        p_request.setAttribute("jobName", jobName);
        sessionMgr.setAttribute("targLocale", targLocale);
        sessionMgr.setAttribute("srcLocale", srcLocale);

        ArrayList wfIds = new ArrayList();
        Hashtable taskUserHash = new Hashtable();
        try
        {
            while (st.hasMoreTokens())
            {
                Job job = ServerProxy.getJobHandler().getJobById(Long.parseLong(st.nextToken()));
                // Get the workflows and search for the workflow with this target locale
                for (Workflow wf : job.getWorkflows())
                {
                    if (!wf.getState().equals(Workflow.CANCELLED) && 
                        wf.getTargetLocale().toString().equals(targLocale))
                    {
                        wfIds.add(new Long(wf.getId()));
                        Hashtable taskHash = wf.getTasks();
                        updateUsers(taskHash, taskUserHash);
                    }
                }
            }
            sessionMgr.setAttribute("taskUserHash", taskUserHash);
            sessionMgr.setAttribute("wfIds", wfIds);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
        super.invokePageHandler(p_pageDescriptor, p_request, 
                                p_response, p_context);
    }

    /**
     * Get the list of users for each Review-Only activity.
     */
    private void updateUsers(Hashtable p_taskHash, Hashtable p_taskUserHash)
        throws GeneralException, RemoteException
    {
        Collection tasks = p_taskHash.values();
        Hashtable userHash = new Hashtable();
        for (Iterator iter=tasks.iterator(); iter.hasNext(); )
        {
            Task task = (Task)iter.next();

            // Check if task is Review-only
            if (!task.isType(Activity.TYPE_REVIEW)) continue;
            
            // Check if task is already in list
            if (p_taskUserHash.get(task.getTaskName()) != null) continue;

            List userInfos =  ServerProxy.getUserManager().getUserInfos(
                                task.getTaskName(),
                                task.getSourceLocale().toString(),
                                task.getTargetLocale().toString());
            if (userInfos == null) continue;
            
            for (Iterator iter2=userInfos.iterator(); iter2.hasNext(); )
            {
                UserInfo userInfo = (UserInfo)iter2.next();
                userHash.put(userInfo.getUserId(), userInfo);
            }
            p_taskUserHash.put(task.getTaskName(), userHash);
        }
    }
}
