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
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Hashtable;
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
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.util.comparator.TaskComparator;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler;
import com.globalsight.everest.webapp.tags.TableConstants;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.GeneralException;

/**
 * Pagehandler for word count pages.
 */
public class WordCountHandler extends PageHandler
{
    public static String TASK_LIST = "tasks";
    public static String TASK_KEY = "task";
    public static String TP_LIST = "targetPages";
    public static String TP_KEY = "targetPage";
    public static String LMT = "LevMatchThreshold";

    private static final Logger s_logger =
        Logger.getLogger(
            WordCountHandler.class);

    /**
     * Invokes this PageHandler
     *
     * @param pageDescriptor the page desciptor
     * @param request the original request sent from the browser
     * @param response the original response object
     * @param context context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
        HttpServletRequest p_request, HttpServletResponse p_response,
        ServletContext p_context)
        throws ServletException, IOException, EnvoyServletException
    {
        String action = p_request.getParameter("action");
        String tpSorting = p_request.getParameter(TP_KEY + TableConstants.SORTING);

        HttpSession session = p_request.getSession(false);
        SessionManager sessionMgr = (SessionManager)
            session.getAttribute(WebAppConstants.SESSION_MANAGER);
        
        setCommonRequestAttributes(p_request);

        if ("one".equals(action))
        {
            oneTask(p_request, session, sessionMgr);
        }
        else if ("tpList".equals(action) || tpSorting != null)
        {
            targetPageList(p_request);
        }
        else
        {
            multipleTasks(p_request, session, sessionMgr, action);
        }
        super.invokePageHandler(p_pageDescriptor, p_request, p_response, p_context);
    }

    /*
     * Get wordcounts for a particular task.
     */
    private void oneTask(HttpServletRequest p_request,
                         HttpSession p_session,
                         SessionManager p_sessionMgr)
    throws EnvoyServletException
    {
        Task task = null;
        try
        {
            String taskid = p_request.getParameter("taskid");
            task = ServerProxy.getTaskManager().getTask(Long.parseLong(taskid));
            //p_request.setAttribute("task", task);
            List tasks = new ArrayList();
            tasks.add(task);
            Job job = task.getWorkflow().getJob();
            p_sessionMgr.setAttribute(LMT, job.getLeverageMatchThreshold());

            boolean isUseInContext = job.getL10nProfile().getTranslationMemoryProfile().getIsContextMatchLeveraging();
            boolean exactMatchOnly = job.getL10nProfile().getTranslationMemoryProfile().getIsExactMatchLeveraging();
            p_request.setAttribute(WebAppConstants.IS_USE_IN_CONTEXT, new Boolean(isUseInContext));
            p_request.setAttribute(WebAppConstants.LEVERAGE_EXACT_ONLY, new Boolean(exactMatchOnly));
            p_request.setAttribute(WebAppConstants.IS_IN_CONTEXT_MATCH, isInContextMatch(job));

            prepareTaskList(p_request, p_session, p_sessionMgr, tasks);
        }
        catch (RemoteException re)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, re);
        }
        catch (GeneralException ge)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, ge);
        }
    }


    /*
     * Get a list of wordcounts for a list of tasks.
     */
    private void multipleTasks(HttpServletRequest p_request, 
                               HttpSession session,
                               SessionManager sessionMgr,
                               String p_action)
    throws EnvoyServletException
    {
        sessionMgr.getAttribute(WebAppConstants.TASK_LIST);
        /*Locale uiLocale = (Locale)session.getAttribute(
                                    WebAppConstants.UILOCALE);*/


        ArrayList list = (ArrayList) sessionMgr.getAttribute(WebAppConstants.TASK_LIST);

        String wfids = (String) p_request.getParameter(JobManagementHandler.WF_ID);

        // This is used for navigating back
        String sorting = p_request.getParameter("taskdoSort");
        if (sorting != null)
        {
            // check if wfids in session
            wfids = (String) sessionMgr.getAttribute(JobManagementHandler.WF_ID);
        }
        else if (wfids != null)
        {
            // User only wants to see word counts for a certain tasks.  Create new list
            // based on the selected.

            // set wfids in session
            sessionMgr.setAttribute(JobManagementHandler.WF_ID, wfids);
        }
        else
        {
            // remove wfid's from session because it's a new list
            sessionMgr.removeElement(JobManagementHandler.WF_ID);
            if ("wclist".equals(p_action))
            {
                // remove wordcount info
                clearSessionOfTableInfo(session, TASK_KEY);
            }
        }

        ArrayList sublist = null;
        if (wfids != null)
        {
            Hashtable hash = new Hashtable();
            StringTokenizer st = new StringTokenizer(wfids, ",");
            while (st.hasMoreTokens())
            {
                hash.put(st.nextToken(), "1");
            }
            
            sublist = new ArrayList();
            // pull out ones the user is interested in
            for (int i = 0 ; i < list.size(); i++)
            {
                Task task = (Task) list.get(i);
                if (hash.get(String.valueOf(task.getWorkflow().getId())) != null)
                {
                    sublist.add(task);
                    Job job = task.getWorkflow().getJob();
                    sessionMgr.setAttribute(LMT,
                                job.getLeverageMatchThreshold());

                    boolean isUseInContext = job.getL10nProfile().getTranslationMemoryProfile().getIsContextMatchLeveraging();
                    boolean exactMatchOnly = job.getL10nProfile().getTranslationMemoryProfile().getIsExactMatchLeveraging();
                    p_request.setAttribute(WebAppConstants.IS_USE_IN_CONTEXT, new Boolean(isUseInContext));
                    p_request.setAttribute(WebAppConstants.LEVERAGE_EXACT_ONLY, new Boolean(exactMatchOnly));
                    p_request.setAttribute(WebAppConstants.IS_IN_CONTEXT_MATCH, isInContextMatch(job));
                }
            }
        } 
        else
        {
            // just keep full list
            sublist = list;
        }

        /*setTableNavigation(p_request, session, sublist,
                          new TaskComparator(uiLocale),
                          10,
                          TASK_LIST, TASK_KEY);*/
        prepareTaskList(p_request, session, sessionMgr, sublist);


    }

    private void targetPageList(HttpServletRequest p_request)
    throws EnvoyServletException
    {
        HttpSession session = p_request.getSession(false);
        SessionManager sessionMgr = (SessionManager)
            session.getAttribute(WebAppConstants.SESSION_MANAGER);
        sessionMgr.getAttribute(WebAppConstants.TASK_LIST);
        Locale uiLocale = (Locale)session.getAttribute(
                                    WebAppConstants.UILOCALE);
        User user = TaskHelper.getUser(session);

        String taskIdParam = p_request.getParameter(TASK_ID);
        long taskId = TaskHelper.getLong(taskIdParam);
        String taskStateParam = p_request.getParameter(TASK_STATE);
        int taskState = TaskHelper.getInt(taskStateParam, -10);
        
        Task task = TaskHelper.getTask(user.getUserId(), taskId,taskState);

//        Task task = (Task)TaskHelper.retrieveObject(
//            session, WebAppConstants.WORK_OBJECT);
        Job job = task.getWorkflow().getJob();
        sessionMgr.setAttribute(LMT, job.getLeverageMatchThreshold());
        
        boolean isUseInContext = job.getL10nProfile().getTranslationMemoryProfile().getIsContextMatchLeveraging();
        boolean exactMatchOnly = job.getL10nProfile().getTranslationMemoryProfile().getIsExactMatchLeveraging();
        boolean isInContextMatch = isInContextMatch(job);
        p_request.setAttribute(WebAppConstants.IS_USE_IN_CONTEXT, new Boolean(isUseInContext));
        p_request.setAttribute(WebAppConstants.LEVERAGE_EXACT_ONLY, new Boolean(exactMatchOnly));
        p_request.setAttribute(WebAppConstants.IS_IN_CONTEXT_MATCH, isInContextMatch);

        ArrayList targetPgs = new ArrayList(task.getTargetPages());
        p_request.setAttribute(TASK_ID, taskIdParam);
        p_request.setAttribute(TASK_STATE, taskStateParam);
        setTableNavigation(p_request, session, targetPgs,
                          new TPWordCountComparator(uiLocale),
                          10,
                          TP_LIST, TP_KEY);
    }

    /*
     * Prepare the info for the word count of the workflow(s) 
     */
    private void prepareTaskList(HttpServletRequest p_request, 
                                 HttpSession p_session, 
                                 SessionManager p_sessionMgr,
                                 List p_tasks)
        throws EnvoyServletException
    {
        Locale uiLocale = (Locale)p_session.getAttribute(
            WebAppConstants.UILOCALE);

        setTableNavigation(p_request, p_session, p_tasks,
                          new TaskComparator(uiLocale),
                          10,
                          TASK_LIST, TASK_KEY);
    }

    private void setCommonRequestAttributes(HttpServletRequest p_request)
    {
        boolean isSpecialCustomer = false; // Dell specific!
        try
        {
           SystemConfiguration sc = SystemConfiguration.getInstance();
           isSpecialCustomer = sc.getBooleanParameter(sc.IS_DELL);
        }
        catch (Exception e)
        {
            s_logger.error("Problem getting system-wide parameter. ", e);
        }                                     

        p_request.setAttribute(SystemConfigParamNames.IS_DELL,
            new Boolean(isSpecialCustomer));
    }
}
