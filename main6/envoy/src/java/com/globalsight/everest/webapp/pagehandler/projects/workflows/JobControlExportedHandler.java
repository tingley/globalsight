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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.filterconfiguration.JsonUtil;
import com.globalsight.everest.company.Company;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.pageexport.ExportBatchEvent;
import com.globalsight.everest.page.pageexport.ExportParameters;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.javabean.NavigationBean;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.webapp.pagehandler.ControlFlowHelper;
import com.globalsight.everest.webapp.pagehandler.administration.customer.download.DownloadFileHandler;
import com.globalsight.everest.webapp.pagehandler.projects.jobvo.JobVoExportSearcher;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.everest.workflowmanager.WorkflowManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.rmi.RemoteException;

public class JobControlExportedHandler
    extends JobManagementHandler
{
    private static final String BASE_BEAN = "exported";
    private static NavigationBean m_exportErrorBean = null;
    private static final Logger s_logger = Logger
    		.getLogger(JobControlExportedHandler.class);
    
    /**
     * Invokes this EntryPageHandler object
     * <p>
     * @param p_ageDescriptor the description of the page to be produced.
     * @param p_request original request sent from the browser.
     * @param p_response original response object.
     * @param p_context the Servlet context.
     */
    public void myInvokePageHandler(WebPageDescriptor p_thePageDescriptor,
                                  HttpServletRequest p_request,
                                  HttpServletResponse p_response,
                                  ServletContext p_context)
    throws ServletException, IOException, RemoteException, EnvoyServletException
    {
        HttpSession session = p_request.getSession(false);
        SessionManager sessionMgr = (SessionManager)session.getAttribute(SESSION_MANAGER);
        
        //"Job Details" >> "View Error" >> "Re-Export" (GBS-908)
        //After re-export,UI will turn back to "Exported" search results list.
        try
        {
        	Company company = ServerProxy.getJobHandler().getCompanyById(
					CompanyWrapper.getCurrentCompanyIdAsLong());
			p_request.setAttribute("company", company);
            String isFromExportErrorPage = p_request.getParameter("fromExportErrorPage");
            if (isFromExportErrorPage != null && "true".equals(isFromExportErrorPage))
            {
            	User user = (User) sessionMgr.getAttribute(WebAppConstants.USER);
            	WorkflowManager wfManager = ServerProxy.getWorkflowManager();
            	//Get all the checked pages as an array of strings
            	//For "View Error",only one workflow can be selected.
                String pages[] = p_request.getParameterValues("page");
                if (pages != null && pages.length > 0)
                {
                    ArrayList pageIds = new ArrayList();//target pages
                    Workflow wf = null;
                    Job job = null;
                    ArrayList wfIds = new ArrayList();
                    
                    // Find the workflows associated with the pages checked
                    for (int i = 0; i < pages.length; i++)
                    {
                        // The value of the page param is "pageId_<pageId>_wfId_<wfId>
                        Long curPageId = new Long(pages[i].substring(
                        		pages[i].indexOf("pageId_") + 7, pages[i].indexOf("_wfId")));
                        pageIds.add(curPageId);
                        //set values for wf and job only once
                        if (wf == null) {
                            String wfId = pages[i].substring(pages[i].indexOf("_wfId_") + 6, pages[i].length());
                            wf = wfManager.getWorkflowById(new Long(wfId));
                            wfIds.add(new Long(wfId));
                            job = wf.getJob();
                        }
                    }
                    ExportParameters ep = new ExportParameters(wf);
                    boolean isTargetPage = true;
                    Long taskId = null;
                    
                    long exportBatchId = ServerProxy.getExportEventObserver()
                    		.notifyBeginExportTargetBatch(job, user, pageIds, wfIds,
                    				taskId, ExportBatchEvent.INTERIM_PRIMARY);
                    ServerProxy.getPageManager().exportPage(ep, pageIds, isTargetPage, exportBatchId);        	
                }            	
            }
        } 
        catch (Exception e) 
        {
        	//In fact,no exception will be caught here.
        	s_logger.error("Fail to re-export selected pages.", e);
        }
        
    	boolean stateMarch = false;
		if(Job.EXPORTED.equals((String)sessionMgr.getMyjobsAttribute("lastState")))
				stateMarch = true;
		setJobSearchFilters(sessionMgr, p_request, stateMarch);

        HashMap beanMap = invokeJobControlPage(p_thePageDescriptor, p_request, BASE_BEAN);
        p_request.setAttribute("searchType", p_request.getParameter("searchType"));
        // error bean.
        m_exportErrorBean = new NavigationBean(ERROR_BEAN, p_thePageDescriptor.getPageName());
        p_request.setAttribute("action", p_request.getParameter("action"));

        if (p_request.getParameter("action") != null
				&& "checkDownloadQAReport".equals(p_request
						.getParameter("action")))
		{
			ServletOutputStream out = p_response.getOutputStream();
			String jobIds = p_request.getParameter("jobIds");
			boolean checkQA = checkQAReport(sessionMgr, jobIds);
			String download = "";
			if (checkQA)
			{
				download = "success";
			}
			else
			{
				download = "fail";
			}
			Map<String, Object> returnValue = new HashMap();
			returnValue.put("download", download);
			out.write((JsonUtil.toObjectJson(returnValue)).getBytes("UTF-8"));
			return;
		}
		else if (p_request.getParameter("action") != null
				&& "downloadQAReport".equals(p_request.getParameter("action")))
		{
			Set<Long> jobIdSet = (Set<Long>) sessionMgr
					.getAttribute("jobIdSet");
			Set<File> exportFilesSet = (Set<File>) sessionMgr
					.getAttribute("exportFilesSet");
			Set<String> localesSet = (Set<String>) sessionMgr
					.getAttribute("localesSet");
			long companyId = (Long) sessionMgr.getAttribute("companyId");
			WorkflowHandlerHelper.zippedFolder(p_request, p_response,
					companyId, jobIdSet, exportFilesSet, localesSet);
			sessionMgr.removeElement("jobIdSet");
			sessionMgr.removeElement("exportFilesSet");
			sessionMgr.removeElement("localesSet");
			return;
		}
        
        performAppropriateOperation(p_request);
        Vector jobStates = new Vector();
        jobStates.add(Job.EXPORTED);
        jobStates.add(Job.EXPORT_FAIL);
        p_request.setAttribute(JobManagementHandler.EXPORT_URL_PARAM, ((NavigationBean)beanMap.get(EXPORT_BEAN)).getPageURL());
        p_request.setAttribute(JobManagementHandler.JOB_ID, JobManagementHandler.JOB_ID);
        
        sessionMgr.setMyjobsAttribute("lastState", Job.EXPORTED);
        JobVoExportSearcher searcher = new JobVoExportSearcher();
        searcher.setJobVos(p_request);
        
        p_request.setAttribute(JOB_LIST_START_PARAM, 
                               p_request.getParameter(JOB_LIST_START_PARAM));
        p_request.setAttribute(PAGING_SCRIPTLET, 
                               getPagingText(p_request, 
                                             ((NavigationBean)beanMap.get(BASE_BEAN)).getPageURL(),
                                             jobStates));

        // Set the EXPORT_INIT_PARAM in the sessionMgr so we can bring
        // the user back here after they Export
        sessionMgr.setAttribute(JobManagementHandler.EXPORT_INIT_PARAM, 
                BASE_BEAN);
        sessionMgr.setAttribute("destinationPage", "exported");
        //clear the session for download job from joblist page
        sessionMgr.setAttribute(DownloadFileHandler.DOWNLOAD_JOB_LOCALES, null);
        sessionMgr.setAttribute(DownloadFileHandler.DESKTOP_FOLDER, null);       
        setJobProjectsLocales(sessionMgr, session);

        // turn on cache.  do both.  "pragma" for the older browsers.
        p_response.setHeader("Pragma", "yes-cache"); //HTTP 1.0
        p_response.setHeader("Cache-Control", "yes-cache"); //HTTP 1.1
        p_response.addHeader("Cache-Control", "yes-store"); // tell proxy not to cache
        // forward to the jsp page.
        RequestDispatcher dispatcher =
            p_context.getRequestDispatcher(p_thePageDescriptor.getJspURL());
        dispatcher.forward(p_request, p_response);
    }

	public boolean checkQAReport(SessionManager sessionMgr, String jobIds)
	{
		Set<File> exportFilesSet = new HashSet<File>();
		Set<String> localesSet = new HashSet<String>();
		Set<Long> jobIdSet = new HashSet<Long>();
		if (StringUtils.isNotBlank(jobIds))
		{
			String[] jobIdArr = jobIds.split(" ");
			for (String id : jobIdArr)
			{
				jobIdSet.add(Long.parseLong(id));
			}
		}
		Set<Workflow> workflowSet = new HashSet<Workflow>();
		try
		{
			long companyId = -1;
			for (Long jobId : jobIdSet)
			{
				Job job = ServerProxy.getJobHandler().getJobById(jobId);
				if (companyId == -1)
				{
					companyId = job.getCompanyId();
				}
				workflowSet.addAll(job.getWorkflows());
			}
			Company company = CompanyWrapper.getCompanyById(companyId);
			if (company.getEnableQAChecks())
			{
				for (Workflow workflow : workflowSet)
				{
					localesSet.add(workflow.getTargetLocale().getLocaleCode());
					String filePath = WorkflowHandlerHelper
							.getExportFilePath(workflow);
					if (filePath != null)
					{
						exportFilesSet.add(new File(filePath));
					}
				}
			}
			if (exportFilesSet != null && exportFilesSet.size() > 0)
			{
				sessionMgr.setAttribute("jobIdSet", jobIdSet);
				sessionMgr.setAttribute("exportFilesSet", exportFilesSet);
				sessionMgr.setAttribute("localesSet", localesSet);
				sessionMgr.setAttribute("companyId", companyId);
				return true;
			}
		}
		catch (Exception e)
		{
			s_logger.error(e);
		}
		return false;
	}
	
    /**
     * Overide getControlFlowHelper so we can do processing
     * and redirect the user correctly.
     *
     * @return the name of the link to follow
     */
    public ControlFlowHelper getControlFlowHelper(
        HttpServletRequest p_request, HttpServletResponse p_response)
    {
        return new JobSearchControlFlowHelper(p_request, p_response);
    }

    //////////////////////////////////////////////////////////////////////////////
    /////////////////////////// JOB CONTROL OPERATION ////////////////////////////
    //////////////////////////////////////////////////////////////////////////////

    protected void performAppropriateOperation(HttpServletRequest p_request)
    throws EnvoyServletException
    {
    	HttpSession session = p_request.getSession(false);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);

        String param = null;
        if ((param = p_request.getParameter(ARCHIVE_JOB_PARAM)) != null)
        {
            String jobId = null;
            StringTokenizer tokenizer = new StringTokenizer(param);
            while(tokenizer.hasMoreTokens())
            {
                jobId = tokenizer.nextToken();
                Job job = WorkflowHandlerHelper.getJobById(Long.parseLong(jobId));
                WorkflowHandlerHelper.archiveJob(job);
            }
        }
        else if (PLANNED_COMP_DATE.equals(p_request.getParameter("action")))
        {
            WorkflowHandlerHelper.
                updatePlannedCompletionDates(p_request);
        }
        else if ((param = p_request.getParameter(DISCARD_JOB_PARAM)) != null)
        {
            if (isRefresh(sessionMgr, param, DISCARD_JOB_PARAM))
            {
                return;
            }
            sessionMgr.setAttribute(DISCARD_JOB_PARAM, param);
            String jobId;
            StringTokenizer tokenizer = new StringTokenizer(param);
            while (tokenizer.hasMoreTokens())
            {
                jobId = tokenizer.nextToken();
                Job job = WorkflowHandlerHelper.getJobById(Long
                        .parseLong(jobId));
                String userId = ((User) sessionMgr
                        .getAttribute(WebAppConstants.USER)).getUserId();

                WorkflowHandlerHelper.cancelJob(userId, job, null);
            }
        }
        else  
        {
            // Don't do anything if they are just viewing the table 
            // and not performing an action on a job
            return;
        }
    }

}
