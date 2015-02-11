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

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.everest.company.MultiCompanySupportedThread;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.jobcreation.JobCreationMonitor;
import com.globalsight.everest.page.PageException;
import com.globalsight.everest.page.PageStateValidator;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.page.pageexport.DelayedExportRequest;
import com.globalsight.everest.page.pageexport.DelayedExporter;
import com.globalsight.everest.page.pageexport.ExportBatchEvent;
import com.globalsight.everest.page.pageexport.ExportConstants;
import com.globalsight.everest.page.pageexport.ExportEventObserverHelper;
import com.globalsight.everest.page.pageexport.ExportParameters;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.ControlFlowHelper;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.everest.workflowmanager.WorkflowExportingHelper;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.NumberUtil;
import com.globalsight.util.file.XliffFileUtil;

/**
 * This control flow helper exports the selected pages and then sends the user
 * back to the page that they initiated Export from.
 * <P>
 * We will know the page they initiated Export from by grabbing the
 * ExportInitPage param from the sessionMgr. This means that all pages that have
 * an Export button will have to set the ExportInitPage param.
 */
class JobExportControlFlowHelper implements ControlFlowHelper, WebAppConstants
{
    private static final Logger CATEGORY = Logger
            .getLogger(JobExportControlFlowHelper.class);

    // local variables
    private HttpServletRequest m_request = null;
    private HttpServletResponse m_response = null;
    private Job m_job = null;
    private String m_exportType = null;
    private String m_stfExportType = null;
    private HashMap<String, String> m_paras = new HashMap<String, String>();

    /**
     * Constructor
     */
    public JobExportControlFlowHelper(HttpServletRequest p_request,
            HttpServletResponse p_response)
    {
        m_request = p_request;
        m_response = p_response;
    }

    // ////////////////////////////////////////////////////////////////////
    // Begin: ControlFlowHelper Implementation
    // ////////////////////////////////////////////////////////////////////
    /**
     * Get the page, from the sessionMgr, where they initiated the Export from
     * and return it as the link to follow
     */
    public String determineLinkToFollow() throws EnvoyServletException
    {
        HttpSession session = m_request.getSession(false);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        String destinationPage = null;
        if (sessionMgr.getAttribute(JobManagementHandler.EXPORT_INIT_PARAM) == null)
        {

            destinationPage = "progress";
        }
        else
        {
            destinationPage = (String) sessionMgr
                    .getAttribute(JobManagementHandler.EXPORT_INIT_PARAM);
        }

        String[] exportActions = m_request.getParameterValues("exportAction");
        if (exportActions != null && exportActions.length > 0
                && exportActions[0].equals("export"))
        {
            String jobId = m_request.getParameter(JobManagementHandler.JOB_ID);
            Job job = WorkflowHandlerHelper.getJobById(Long.parseLong(jobId));
            m_job = job;

            Date jobExportStartTime = new Date();

            getExportData();

            // Setting Delay time for download after exporting
            User user = (User) sessionMgr.getAttribute(USER);
            String userId = user.getUserId();

            String delayTimeTableKey = null;
            Hashtable delayTimeTable = (Hashtable) sessionMgr
                    .getAttribute(WebAppConstants.DOWLOAD_DELAY_TIME_TABLE);
            if (delayTimeTable == null)
            {
                delayTimeTable = new Hashtable();
            }
            String pages[] = m_request.getParameterValues("page");
            if (pages != null)
            {
                for (int temp = 0; temp < pages.length; temp++)
                {
                    String wfId = pages[temp].substring(
                            pages[temp].indexOf("_wfId_") + 7,
                            pages[temp].length());
                    delayTimeTableKey = userId + jobId + wfId;
                    delayTimeTable.put(delayTimeTableKey, jobExportStartTime);
                }
            }

            sessionMgr.setAttribute(WebAppConstants.DOWLOAD_DELAY_TIME_TABLE,
                    delayTimeTable);

            if ("true"
                    .equals(m_request
                            .getParameter(JobManagementHandler.EXPORT_MULTIPLE_ACTIVITIES_PARAM)))
                m_request.setAttribute(WebAppConstants.TASK_STATE, new Integer(
                        Task.STATE_ACCEPTED));
        }
        else
        {
            // They clicked on the Cancel button, so do nothing
        }

        CATEGORY.debug("Export initiated from page: " + destinationPage);

        return destinationPage;
    }

    // ////////////////////////////////////////////////////////////////////
    // End: ControlFlowHelper Implementation
    // ////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////////////
    // //////////////////////// EXPORT OPERATION
    // ////////////////////////////////
    // ////////////////////////////////////////////////////////////////////////////
    private void getExportData() throws EnvoyServletException
    {
        try
        {
            if ("true"
                    .equals(m_request
                            .getParameter(JobManagementHandler.EXPORT_FOR_UPDATE_PARAM)))
            {
                performExportForUpdate();
            }
            else
            {
                // Get all the checked pages as an array of strings
                final String pages[] = m_request.getParameterValues("page");
                HttpSession session = m_request.getSession(false);
                final User user = PageHandler.getUser(session);

                Enumeration<String> names = m_request.getParameterNames();
                if (names != null && names.hasMoreElements())
                {
                    while (names.hasMoreElements())
                    {
                        String name = names.nextElement();
                        String value = m_request.getParameter(name);
                        m_paras.put(name, value);
                    }
                }

                Runnable runnable = new Runnable()
                {
                    public void run()
                    {
                        // run the export process in a new thread in order to
                        // return to job detail page quickly without waiting for
                        // the export process to be completed
                        performExportPageLevel(pages, user, m_paras);
                    }
                };
                Thread t = new MultiCompanySupportedThread(runnable);
                t.start();
            }
        }
        catch (PageException pe)
        {
            throw new EnvoyServletException(pe);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    private void exportPage(Workflow p_workflow, List p_listOfPageIds,
            boolean p_isTargetPage, long p_exportBatchId,
            HashMap<String, String> paras) throws EnvoyServletException,
            PageException
    {
        long id = p_workflow.getId();
        String locale = p_workflow.getTargetLocale().toString();
        String codeSetParam = getConcatenatedRequestParameter(
                JobManagementHandler.EXPORT_WF_CODE_PARAM, id, locale, paras);
        String exportLocation = getConcatenatedRequestParameter(
                JobManagementHandler.EXPORT_WF_LOCATION_PARAM, id, locale,
                paras);
        String localeSubDir = getConcatenatedRequestParameter(
                JobManagementHandler.EXPORT_WF_LOCALE_SUBDIR_PARAM, id, locale,
                paras);
        String bomType = getConcatenatedRequestParameter("bomType", id, locale,
                paras);
        String xlfSourceAsTarget = getConcatenatedRequestParameter(
                JobManagementHandler.EXPORT_WF_XLF_SRC_AS_TRG_PARAM, id,
                locale, paras);

        ExportParameters ep = new ExportParameters(p_workflow, codeSetParam,
                exportLocation, localeSubDir, NumberUtil.convertToInt(bomType));
        ep.setXlfSrcAsTrg(NumberUtil.convertToInt(xlfSourceAsTarget));

        WorkflowHandlerHelper.exportPage(ep, p_listOfPageIds, p_isTargetPage,
                p_exportBatchId);
    }

    /*
     * Export the primary target files as one batch.
     */
    private void exportPrimaryTargetFiles(List p_pageIds, User p_user,
            List p_workflowIds, HashMap p_map, HashMap<String, String> paras)
            throws EnvoyServletException, PageException
    {
        long exportBatchId = -1;
        // register for export notification - use last workflow to get the job
        try
        {
            if (p_pageIds.size() > 0)
            {
                exportBatchId = ExportEventObserverHelper
                        .notifyBeginExportTargetBatch(m_job, p_user, p_pageIds,
                                p_workflowIds, null, m_exportType);
            }
        }
        catch (Exception e)
        {
            CATEGORY.error(e.getMessage(), e);
            throw new EnvoyServletException(e);
        }

        // Iterate through each workflow and send its checked
        // pages to be exported
        Iterator it = p_map.keySet().iterator();
        while (it.hasNext())
        {
            Workflow workflow = (Workflow) it.next();
            // from GBS-2137, add "Exporting" state before export is done
            if (Workflow.LOCALIZED.equals(workflow.getState()))
            {
                JobCreationMonitor.updateWorkflowState(workflow,
                        Workflow.EXPORTING);
                JobCreationMonitor.updateJobStateToExporting(workflow.getJob());
            }

            exportPage(workflow,
                    getTargetPageIds((HashSet) p_map.get(workflow)), true,
                    exportBatchId, paras);
        }
    }

    /*
     * Export secondary target files as one batch. Note that we cannot export
     * both priamry and secondary as one batch due to the page object type that
     * needs to be known for ExportEventObserver remote interface. The object
     * type is known based on the export type and for STFs we use either
     * INTERIM_SECONDARY or FINAL_SECONDARY.
     */
    private void exportSecondaryTargetFiles(List p_stfIds, User p_user,
            List p_stfWorkflowIds, HashMap p_stfMap,
            HashMap<String, String> paras) throws EnvoyServletException
    {
        long exportBatchId = -1;
        try
        {
            if (p_stfIds.size() > 0)
            {
                exportBatchId = ExportEventObserverHelper
                        .notifyBeginExportTargetBatch(m_job, p_user, p_stfIds,
                                p_stfWorkflowIds, null, m_stfExportType);
            }
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }

        Object[] keys = p_stfMap.keySet().toArray();
        for (int h = 0; h < keys.length; h++)
        {
            Workflow workflow = (Workflow) keys[h];
            exportStfs(workflow,
                    getTargetPageIds((HashSet) p_stfMap.get(workflow)),
                    exportBatchId, paras);
        }
    }

    // Export the Secondary Target Files
    private void exportStfs(Workflow p_workflow, List p_listOfStfIds,
            long p_exportBatchId, HashMap<String, String> paras)
            throws EnvoyServletException
    {
        try
        {
            long id = p_workflow.getId();
            String locale = p_workflow.getTargetLocale().toString();
            String codeSetParam = getConcatenatedRequestParameter(
                    JobManagementHandler.EXPORT_WF_CODE_PARAM, id, locale,
                    paras);
            String exportLocation = getConcatenatedRequestParameter(
                    JobManagementHandler.EXPORT_WF_LOCATION_PARAM, id, locale,
                    paras);
            String localeSubDir = getConcatenatedRequestParameter(
                    JobManagementHandler.EXPORT_WF_LOCALE_SUBDIR_PARAM, id,
                    locale, paras);
            String bomType = getConcatenatedRequestParameter(
                    JobManagementHandler.EXPORT_WF_BOM_PARAM, id, locale, paras);
            String xlfSourceAsTarget = getConcatenatedRequestParameter(
                    JobManagementHandler.EXPORT_WF_XLF_SRC_AS_TRG_PARAM, id,
                    locale, paras);

            ExportParameters ep = new ExportParameters(p_workflow,
                    codeSetParam, exportLocation, localeSubDir,
                    NumberUtil.convertToInt(bomType),
                    ExportConstants.EXPORT_STF);
            ep.setXlfSrcAsTrg(NumberUtil.convertToInt(xlfSourceAsTarget));

            ServerProxy.getPageManager().exportSecondaryTargetFiles(ep,
                    p_listOfStfIds, p_exportBatchId);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    private List getTargetPageIds(HashSet p_setOfPageIds)
    {
        Iterator it = p_setOfPageIds.iterator();
        ArrayList list = new ArrayList();
        while (it.hasNext())
        {
            list.add(it.next());
        }

        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("List of target pageIds to export: " + list);            
        }

        return list;
    }

    private Workflow getWorkflowById(long p_wfId) throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getWorkflowManager().getWorkflowByIdRefresh(
                    p_wfId);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    private void performExportForUpdate() throws Exception
    {
        long jobId = m_job.getId();
        // validate page states before exporting source pages
        PageStateValidator.validateStateOfPagesInJob(m_job);

        Iterator iter = m_job.getSourcePages().iterator();
        ArrayList idList = new ArrayList();
        while (iter.hasNext())
        {
            SourcePage sp = (SourcePage) iter.next();
            long pageId = sp.getId();
            idList.add(new Long(pageId));
        }

        String codeSetParam = m_request
                .getParameter(JobManagementHandler.EXPORT_WF_CODE_PARAM + "_"
                        + jobId);
        String exportLocation = m_request
                .getParameter(JobManagementHandler.EXPORT_WF_LOCATION_PARAM
                        + "_" + jobId);
        String localeSubDir = m_request
                .getParameter(JobManagementHandler.EXPORT_WF_LOCALE_SUBDIR_PARAM
                        + "_" + jobId);
        String bomType = m_request
                .getParameter(JobManagementHandler.EXPORT_WF_BOM_PARAM + "_"
                        + jobId);
        String xlfSrcAsTrg = m_request
                .getParameter(JobManagementHandler.EXPORT_WF_XLF_SRC_AS_TRG_PARAM
                        + "_" + jobId);

        ExportParameters ep = new ExportParameters(null, codeSetParam,
                exportLocation, localeSubDir, NumberUtil.convertToInt(bomType));
        ep.setXlfSrcAsTrg(NumberUtil.convertToInt(xlfSrcAsTrg));

        int minutesOfDelay = Integer.parseInt(m_request
                .getParameter(JobExportHandler.PARAM_DELAY));
        HttpSession session = m_request.getSession(false);
        User user = PageHandler.getUser(session);
        CATEGORY.info("Exporting source pages in " + minutesOfDelay
                + " minutes. Export initiated by " + user);
        DelayedExportRequest der = new DelayedExportRequest(ep, idList, false,
                minutesOfDelay, jobId, user);

        DelayedExporter.getInstance().delayExport(der, m_job);
    }

    private void performExportPageLevel(String[] pages, User user,
            HashMap<String, String> paras) throws PageException,
            EnvoyServletException
    {
        Workflow workflow = null;
        // For Primary Files
        HashMap map = new HashMap();
        ArrayList pageIds = new ArrayList();
        ArrayList workflowIds = new ArrayList();

        // For Secondary Target Files
        HashMap stfMap = new HashMap();
        ArrayList stfIds = new ArrayList();
        ArrayList stfWorkflowIds = new ArrayList();
        HashMap wfMap = new HashMap();
        Set<Long> ignoreWorkflowIds = new HashSet<Long>();

        // Find the workflows associated with the pages checked
        for (int i = 0; i < pages.length; i++)
        {
            // The value of the page param is "pageId_<pageId>_wfId_<p or
            // s><wfId>
            // so we must extract it using substring()
            Long curPageId = new Long(pages[i].substring(
                    pages[i].indexOf("pageId_") + 7, pages[i].indexOf("_wfId")));

            String wfId = pages[i].substring(pages[i].indexOf("_wfId_") + 7,
                    pages[i].length());

            if (pages[i].indexOf("_wfId_" + JobExportHandler.PRIMARY_PREFIX) != -1)
            {
                preparePrimaryTargetInfo(pageIds, curPageId, map, workflowIds,
                        ignoreWorkflowIds);
            }
            else
            {
                prepareSecondaryTargetInfo(stfIds, curPageId, wfMap, wfId,
                        stfMap, stfWorkflowIds);
            }
        }

        for (Long id : ignoreWorkflowIds)
        {
            CATEGORY.info("Ignored re-exporting request from workflow(id=" + id
                    + ") since it is being exported.");
        }

        if (workflowIds.size() > 0)
        {
            WorkflowExportingHelper.setAsExporting(workflowIds);
        }
        if (stfWorkflowIds.size() > 0)
        {
            WorkflowExportingHelper.setAsExporting(stfWorkflowIds);
        }

        // before exporting, let's make sure no page is in UPDATING state
        PageStateValidator.validateStateOfPagesInWorkflows(map.keySet()
                .toArray());

        // first export primary target files (if any)
        exportPrimaryTargetFiles(pageIds, user, workflowIds, map, paras);
        // now export secondary target files (if any)
        exportSecondaryTargetFiles(stfIds, user, stfWorkflowIds, stfMap, paras);
    }

    private void deleteOldFiles(TargetPage targetPage)
    {
    	SourcePage page = targetPage.getSourcePage();
    	 
    	if (page != null)
        {
            String pageId = page.getExternalPageId().replace("\\", "/");
            pageId = pageId.substring(pageId.indexOf("/"));
            String locale = targetPage.getExportSubDir();
            String path = AmbFileStoragePathUtils.getCxeDocDir(page
                    .getCompanyId()) + locale + pageId;
            File f = new File(path);
            if (f.exists())
            {
                f.delete();
            }            
            
            if (path.endsWith(".xlf") || path.endsWith(".xliff"))
            {
//            	for passolo
            	int n = path.lastIndexOf(".lpu");
            	if (n > 0)
            	{
            		String pPath = AmbFileStoragePathUtils.getCxeDocDir(page
                            .getCompanyId()) + "/passolo" + pageId;
            		n = pPath.lastIndexOf(".lpu");
            		
            		pPath = pPath.substring(0,  n + 4);
                    f = new File(pPath);
                	
                	if (f.exists())
                    {
                        f.delete();
                    }
            	}
            	
            	n = path.lastIndexOf(XliffFileUtil.SEPARATE_FLAG);
            	if (n > 0)
            	{
            		path = path.substring(0, n);
                	f = new File(path);
                	
                	if (f.exists())
                    {
                        f.delete();
                    }
            	}
            	
            	path = path.substring(0, path.lastIndexOf("/")) + XliffFileUtil.XLZ_EXTENSION;
            	f = new File(path);
            	
            	if (f.exists())
                {
                    f.delete();
                }
            }
        }
    }
    
    private String getSourcePath(String page, String companyId)
    {
    	if (page == null)
    		return null;
    	
    	String sourcePath = XliffFileUtil.getSourceFile(page, companyId);
    	if (sourcePath != null)
    	{
    		return sourcePath;
    	}
    	
    	if (page.toLowerCase().endsWith(".indd"))
    	{
    		if (page.startsWith("("))
    		{
    			int n = page.indexOf(")");
    			page = page.substring(n + 1);
    		}
    		
    		return page.trim();
    	}
    	
    	return null;
    }
    
    /*
     * Prepare the information used for exporting a list of primary target
     * files. This method is called within a for loop.
     */
    private void preparePrimaryTargetInfo(List p_pageIds, Long p_curPageId,
            HashMap p_map, List p_workflowIds, Set<Long> ignoreWorkflowIds)
            throws EnvoyServletException
    {
        TargetPage targetPage = WorkflowHandlerHelper.getTargetPage(p_curPageId
                .longValue());
        Workflow workflow = targetPage.getWorkflowInstance();
        String companyId = "" + workflow.getCompanyId();

        if (WorkflowExportingHelper.isExporting(workflow.getId()))
        {
            ignoreWorkflowIds.add(workflow.getId());
            return;
        }

        if (p_pageIds.contains(p_curPageId))
        {
        	return;
        }
        
        HashSet set = (HashSet) p_map.get(workflow);
        if (set == null)
        {
            set = new HashSet();
            p_map.put(workflow, set);
            p_workflowIds.add(workflow.getIdAsLong());
        }
        
        deleteOldFiles(targetPage);
    	p_pageIds.add(p_curPageId);
        set.add(p_curPageId);
        
        String page = targetPage.getExternalPageId();
        if (page != null)
        {
        	String sourcePath = getSourcePath(page, companyId);
        	if (sourcePath != null)
        	{
        		for (TargetPage t : workflow.getTargetPages())
                {
            		if (t.getId() == p_curPageId)
            		{
            			continue;
            		}
            		
                	String tPage = t.getExternalPageId();
                	if (tPage != null)
                	{
                		String tsPage = getSourcePath(tPage, companyId);
                		if (sourcePath.equals(tsPage))
                		{
                			set.add(t.getId());
                			if (!p_pageIds.contains(t.getId()))
                			{
                				deleteOldFiles(t);
                    			p_pageIds.add(t.getId());
                			}
                		}
                	}
                }
        	}
        }

        // use the last workflow to determine the export type
        m_exportType = workflow.getState().equals(Workflow.LOCALIZED) ? ExportBatchEvent.FINAL_PRIMARY
                : ExportBatchEvent.INTERIM_PRIMARY;
    }

    /**
     * Prepare the information used for exporting a list of secondary target
     * files. This method is called within a for loop.
     */
    private void prepareSecondaryTargetInfo(List p_stfIds, Long p_curPageId,
            HashMap p_wfMap, String p_wfId, HashMap p_stfMap,
            List p_stfWorkflowIds) throws EnvoyServletException
    {
        p_stfIds.add(p_curPageId);
        Workflow workflow = (Workflow) p_wfMap.get(p_wfId);
        if (workflow == null)
        {
            workflow = getWorkflowById(Long.parseLong(p_wfId));

            p_wfMap.put(p_wfId, workflow);
        }

        HashSet set = (HashSet) p_stfMap.get(workflow);
        if (set == null)
        {
            set = new HashSet();
            p_stfMap.put(workflow, set);
            p_stfWorkflowIds.add(workflow.getIdAsLong());
        }
        set.add(p_curPageId);

        // use the last workflow to determine the export type
        m_stfExportType = workflow.getState().equals(Workflow.LOCALIZED) ? ExportBatchEvent.FINAL_SECONDARY
                : ExportBatchEvent.INTERIM_SECONDARY;
    }

    /**
     * Concatenates the strings to create the desired request parameter name. If
     * the value does not exist, then it is tried without the original suffix,
     * but with the locale instead. format is "prefix_suffix" or "prefix_locale"
     */
    private String getConcatenatedRequestParameter(String p_prefix,
            long p_suffix, String p_locale, HashMap<String, String> paras)
    {
        String newPrefix = p_prefix + "_";
        String value = paras.get(newPrefix + p_suffix);
        if (value == null)
        {
            value = paras.get(newPrefix + p_locale);
        }

        return value;
    }
}
