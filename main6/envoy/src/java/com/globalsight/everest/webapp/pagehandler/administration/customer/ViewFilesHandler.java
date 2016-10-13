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

import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.webapp.tags.TableConstants;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.rmi.RemoteException;


/**
 * This is the handler for displaying all files for a locale in a job.
 */
public class ViewFilesHandler
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
        ArrayList pages = null;

        try
        {
            // Is this a sort? or a next/previous?
            if (p_request.getParameter(SourceFile.FILE_KEY + TableConstants.SORTING) != null)
            {
                pages = getDataFromSession(sessionMgr);
            }
            else
            {
                pages = getDataFromRequest(p_request, sessionMgr);
            }

            Locale locale = (Locale)session.getAttribute(UILOCALE);
            dataForTable(p_request, session, SourceFile.FILE_LIST, SourceFile.FILE_KEY,
                         new SourceFileComparator(locale), pages);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
        super.invokePageHandler(p_pageDescriptor, p_request, 
                                p_response, p_context);
    }

    private ArrayList getDataFromSession(SessionManager p_sessionMgr)
    {
        return (ArrayList)p_sessionMgr.getAttribute("pages");
    }

    private ArrayList getDataFromRequest(HttpServletRequest p_request,
                                     SessionManager p_sessionMgr)
        throws EnvoyServletException
    {
        String completed = getBundle(p_request.getSession()).getString("lb_completed");
        // Value is jobname, locale language, locale country, jobid, jobid...
        String value = (String)p_request.getParameter("value");
        StringTokenizer st = new StringTokenizer(value, ",");
        String jobName = st.nextToken();
        String targLocale = st.nextToken();
        String srcLocale = st.nextToken();  // not needed for this page but is for others
        p_sessionMgr.setAttribute("jobName", jobName);
        p_sessionMgr.setAttribute("targLocale", targLocale);
        ArrayList pages = new ArrayList();
        try
        {
            while (st.hasMoreTokens())
            {
                Job job = ServerProxy.getJobHandler().getJobById(Long.parseLong(st.nextToken()));
                // Get the workflows and search for the workflow with this target locale
                String status = null;
                for (Workflow wf : job.getWorkflows())
                {
                    if (wf.getTargetLocale().toString().equals(targLocale))
                    {
                        Hashtable taskHash = wf.getTasks();
                        Collection tasks = taskHash.values();
                        status = getStatus(tasks, completed);
                    }
                }
                Collection srcPages = job.getSourcePages();
                for (Iterator iter=srcPages.iterator(); iter.hasNext(); )
                {
                    SourcePage sp = (SourcePage)iter.next();
                    SourceFile sf = new SourceFile(sp.getExternalPageId(), status);
                    pages.add(sf);
                }
            }
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
        p_sessionMgr.setAttribute("pages", pages);
        return pages;
    }

    /**
     * Return the status of the tasks.  If there is an active one, return that.
     * If no active, then return the accepted one.  If all completed, return COMPLETED.
     * If only awaiting acceptance, return that one.
     */
    private String getStatus(Collection tasks, String completed)
    {
        for (Iterator iter=tasks.iterator(); iter.hasNext(); )
        {
            Task task = (Task)iter.next();
            if (task.getState() == Task.STATE_ACTIVE)
            {
                return task.getTaskName();
            }
        }
        for (Iterator iter=tasks.iterator(); iter.hasNext(); )
        {
            Task task = (Task)iter.next();
            if (task.getState() == Task.STATE_ACCEPTED)
            {
                return task.getTaskName();
            }
        }
        for (Iterator iter=tasks.iterator(); iter.hasNext(); )
        {
            Task task = (Task)iter.next();
            if (task.getState() == Task.STATE_COMPLETED)
            {
                return completed;
            }
        }
        return "";
    }

    /**
     * Get list of files for displaying in table
     */
    private void dataForTable(HttpServletRequest p_request,
                              HttpSession p_session, String listname, String keyname,
                              SourceFileComparator comparator,
                              List p_files)
        throws EnvoyServletException
    {
        try
        {
            setTableNavigation(p_request, p_session, p_files,
                comparator,
                20,
                listname,
                keyname);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }
}
