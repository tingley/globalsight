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

import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.util.GeneralException;

/**
 * Pagehandler for word count pages.
 */
public class WordCountHandler extends PageHandler
{
    public static String WF_LIST = "wfs";
    public static String WF_KEY = "wf";
    public static String LMT = "LevMatchThreshold";
    public static String JOB_NAME = "JOB_NAME";

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
//        String tpSorting = p_request.getParameter(WF_KEY + TableConstants.SORTING);

        HttpSession session = p_request.getSession(false);
        SessionManager sessionMgr = (SessionManager)
            session.getAttribute(WebAppConstants.SESSION_MANAGER);

        setCommonRequestAttributes(p_request);

        if ("one".equals(action))
        {
            oneWorkflow(p_request, session, sessionMgr);
        }
        else if ("list".equals(action))
        {
            multipleWfs(p_request, session, sessionMgr);
        }
        else
        {
            // sorting or paging
            refresh(p_request, session, sessionMgr);
        }
        super.invokePageHandler(p_pageDescriptor, p_request, p_response, p_context);
    }

    /*
     * Get wordcounts for a particular Workflow.
     */
    private void oneWorkflow(HttpServletRequest p_request,
                             HttpSession p_session, 
                             SessionManager p_sessionMgr)
    throws EnvoyServletException
    {
        Workflow wf = null;
        try
        {
            String wfid = p_request.getParameter("wfId");
            wf = ServerProxy.getWorkflowManager().
                getWorkflowByIdRefresh(Long.parseLong(wfid));
            Job job = wf.getJob();
            List<Workflow> wfs = new ArrayList<Workflow>();
            wfs.add(wf);
            boolean isUseInContext = job.getL10nProfile().getTranslationMemoryProfile().getIsContextMatchLeveraging();
            boolean exactMatchOnly = job.getL10nProfile().getTranslationMemoryProfile().getIsExactMatchLeveraging();
            p_sessionMgr.setAttribute(WebAppConstants.IS_USE_IN_CONTEXT, isUseInContext);
            p_sessionMgr.setAttribute(WebAppConstants.LEVERAGE_EXACT_ONLY, exactMatchOnly);
            p_sessionMgr.setAttribute(WebAppConstants.IS_IN_CONTEXT_MATCH, PageHandler.isInContextMatch(job));
            p_request.setAttribute(WebAppConstants.JOB_ID,job.getId()+"");

            prepareWorkflowList(p_request, p_session, p_sessionMgr, wfs,
                                job.getJobName(), String.valueOf(
                                    job.getLeverageMatchThreshold()));
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
     * Get a list of wordcounts for a list of workflows.
     */
    private void multipleWfs(HttpServletRequest p_request,
                             HttpSession p_session, 
                             SessionManager p_sessionMgr)
    throws EnvoyServletException
    {
        Long jobId = new Long((String) p_request.getParameter(
            JobManagementHandler.JOB_ID));
        p_request.setAttribute(JobManagementHandler.JOB_ID, jobId+"");
        Job job = null;
        boolean isUseInContext = false;
        boolean exactMatchOnly = false;
        boolean isInContextMatch = false;
        try
        {
            job = ServerProxy.getJobHandler().getJobById(
                jobId.longValue());
            isUseInContext = job.getL10nProfile().getTranslationMemoryProfile().getIsContextMatchLeveraging();
            exactMatchOnly = job.getL10nProfile().getTranslationMemoryProfile().getIsExactMatchLeveraging();
            isInContextMatch = isInContextMatch(job);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }

        p_sessionMgr.setAttribute(WebAppConstants.IS_USE_IN_CONTEXT, isUseInContext);
        p_sessionMgr.setAttribute(WebAppConstants.LEVERAGE_EXACT_ONLY, exactMatchOnly);
        p_sessionMgr.setAttribute(WebAppConstants.IS_IN_CONTEXT_MATCH, isInContextMatch);
		String wfids = (String) p_request
				.getParameter(JobManagementHandler.WF_ID);

        Hashtable hash = new Hashtable();
        StringTokenizer st = new StringTokenizer(wfids, " ");
        while (st.hasMoreTokens())
        {
            hash.put(st.nextToken(), "1");
        }
        
        List sublist = new ArrayList();
        // pull out ones the user is interested in
        for (Workflow wf : job.getWorkflows())
        {
            if (hash.get(String.valueOf(wf.getId())) != null)
            {
                sublist.add(wf);
            }
        }
        
        prepareWorkflowList(p_request, p_session, p_sessionMgr, sublist, 
                            job.getJobName(), String.valueOf(
                                job.getLeverageMatchThreshold()));
    }

    /*
     * Prepare the info for the word count of the workflow(s) 
     */
    private void prepareWorkflowList(HttpServletRequest p_request, 
                                     HttpSession p_session, 
                                     SessionManager p_sessionMgr,
                                     List p_workflows,
                                     String p_jobName,
                                     String p_levMatchThreshold)
        throws EnvoyServletException
    {
        p_sessionMgr.setAttribute(JOB_NAME, p_jobName);
        p_sessionMgr.setAttribute(LMT, p_levMatchThreshold);
        p_sessionMgr.setAttribute("sublist", p_workflows);

        Locale uiLocale = (Locale)p_session.getAttribute(
            WebAppConstants.UILOCALE);

        setTableNavigation(p_request, p_session, p_workflows,
                          new WorkflowComparator(uiLocale),
                          10,
                          WF_LIST, WF_KEY);
    }

    // refresh the list when sorting by a column is invoked.
    private void refresh(HttpServletRequest p_request,
                         HttpSession p_session,
                         SessionManager p_sessionMgr)
    throws EnvoyServletException
    {
        Locale uiLocale = (Locale)p_session.getAttribute(
                                    WebAppConstants.UILOCALE);

        ArrayList sublist = (ArrayList) p_sessionMgr.getAttribute("sublist");
        setTableNavigation(p_request, p_session, sublist,
                          new WorkflowComparator(uiLocale),
                          10,
                          WF_LIST, WF_KEY);

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

