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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;

import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.jobcreation.JobAdditionEngine;
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
import com.globalsight.log.GlobalSightCategory;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.zip.ZipIt;

/**
 * This control flow helper exports the selected pages
 * and then sends the user back to the page that they
 * initiated Export from.
 * <P>
 * We will know the page they initiated Export from by
 * grabbing the ExportInitPage param from the
 * sessionMgr. This means that all pages that have
 * an Export button will have to set the ExportInitPage param.
 */
class JobExportControlFlowHelper
    implements ControlFlowHelper, WebAppConstants
{
    private static final GlobalSightCategory CATEGORY =
        (GlobalSightCategory) GlobalSightCategory.getLogger(
            JobExportControlFlowHelper.class);

    // local variables
    private HttpServletRequest m_request = null;
    private HttpServletResponse m_response = null;
    private Job m_job = null;
    private String m_exportType = null;
    private String m_stfExportType = null;

    /**
     * Constructor
     */
    public JobExportControlFlowHelper(HttpServletRequest p_request,
                                      HttpServletResponse p_response)
    {
        m_request = p_request;
        m_response = p_response;
    }


    //////////////////////////////////////////////////////////////////////
    //  Begin: ControlFlowHelper Implementation
    //////////////////////////////////////////////////////////////////////
    /**
     * Get the page, from the sessionMgr, where they initiated the Export from
     * and return it as the link to follow
     */
    public String determineLinkToFollow()
    throws EnvoyServletException
    {
        HttpSession session = m_request.getSession(false);
        SessionManager sessionMgr =
        (SessionManager)session.getAttribute(SESSION_MANAGER);
        String destinationPage = null;
        if (sessionMgr.getAttribute(
            JobManagementHandler.EXPORT_INIT_PARAM) == null)
        {

            destinationPage =  "progress";
        } else
        {
            destinationPage = 
            (String)sessionMgr.getAttribute(JobManagementHandler
                                            .EXPORT_INIT_PARAM);
        }

        String[] exportActions = m_request.getParameterValues("exportAction");
        if (exportActions != null && exportActions.length > 0 && exportActions[0].equals("export"))
        {
            // They clicked on the Export button
            // Export the data
            
            Date jobExportStartTime = new Date();
                        
            getExportData();
            
            // Setting Delay time for download after exporting 
            User user = (User) sessionMgr.getAttribute(USER);
            String userId = user.getUserId();
            String jobId = String.valueOf(m_job.getId());

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
                    String wfId = pages[temp].substring(pages[temp]
                            .indexOf("_wfId_") + 7, pages[temp].length());
                    delayTimeTableKey = userId + jobId + wfId;
                    delayTimeTable.put(delayTimeTableKey, jobExportStartTime);
                }
            }

            sessionMgr.setAttribute(WebAppConstants.DOWLOAD_DELAY_TIME_TABLE,
                    delayTimeTable);
            
            if ("true".equals(m_request.getParameter(JobManagementHandler.EXPORT_MULTIPLE_ACTIVITIES_PARAM)))
                m_request.setAttribute(WebAppConstants.TASK_STATE,new Integer(Task.STATE_ACCEPTED));
        } else
        {
            // They clicked on the Cancel button, so do nothing
        }

        CATEGORY.debug("Export initiated from page: " + destinationPage);

        return destinationPage;
    }
    //////////////////////////////////////////////////////////////////////
    //  End: ControlFlowHelper Implementation
    //////////////////////////////////////////////////////////////////////

    
    
    //////////////////////////////////////////////////////////////////////////////
    //////////////////////////  EXPORT OPERATION  ////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////

    private void getExportData()
        throws EnvoyServletException
    {
        try
        {
            if (m_request.getParameter(
                JobManagementHandler.EXPORT_FOR_UPDATE_PARAM) != null &&
                m_request.getParameter(
                    JobManagementHandler.EXPORT_FOR_UPDATE_PARAM)
                .equals("true"))
            {
                performExportForUpdate();
            } else
            {
                List wf_list = performExportPageLevel();
                //Make DTP Workflow instance
                JobAdditionEngine m_jobAdditionEngine = new JobAdditionEngine();
                m_jobAdditionEngine.addDtpWorkflowToJob(m_request.getSession().getId(), wf_list);
            }
        } catch (PageException pe)
        {
            throw new EnvoyServletException(pe);
        } catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    private void exportPage(Workflow p_workflow, List p_listOfPageIds, 
                            boolean p_isTargetPage, long p_exportBatchId)
        throws EnvoyServletException, PageException
    {
        long id = p_workflow.getId();
        String locale = p_workflow.getTargetLocale().toString();
        String codeSetParam = getConcatenatedRequestParameter(
            JobManagementHandler.EXPORT_WF_CODE_PARAM,id, locale);
        String exportLocation = getConcatenatedRequestParameter(
            JobManagementHandler.EXPORT_WF_LOCATION_PARAM, id, locale);
        String localeSubDir = getConcatenatedRequestParameter(
            JobManagementHandler.EXPORT_WF_LOCALE_SUBDIR_PARAM,id,locale);
        ExportParameters ep = new ExportParameters(
            p_workflow, codeSetParam, exportLocation, localeSubDir);

        WorkflowHandlerHelper.exportPage(ep, p_listOfPageIds, 
                                         p_isTargetPage, p_exportBatchId);
    }


    /* 
     * Export the primary target files as one batch.
     */
    private void exportPrimaryTargetFiles(List p_pageIds, 
                                          HttpSession p_session, 
                                          List p_workflowIds,
                                          HashMap p_map)
        throws EnvoyServletException, PageException
    {
        long exportBatchId = -1;
        // register for export notification - use last workflow to get the job
        try
        {
            if (p_pageIds.size() > 0)
            {
                exportBatchId = 
                    ExportEventObserverHelper.notifyBeginExportTargetBatch(
                        m_job,
                        PageHandler.getUser(p_session), p_pageIds,
                        p_workflowIds, null, m_exportType);                
            }
        }  
        catch(Exception e)
        { 
            throw new EnvoyServletException(e);
        }
        
        // Iterate through each workflow and send its checked
        // pages to be exported
        Iterator it = p_map.keySet().iterator();
        while (it.hasNext())
        {
            Workflow workflow = (Workflow)it.next();
            exportPage(workflow, 
                       getTargetPageIds((HashSet)p_map.get(workflow)), 
                       true, exportBatchId);
        }
    }


    /*
     * Export secondary target files as one batch.  Note that we cannot
     * export both priamry and secondary as one batch due to the page 
     * object type that needs to be known for ExportEventObserver remote
     * interface.  The object type is known based on the export type and
     * for STFs we use either INTERIM_SECONDARY or FINAL_SECONDARY.
     */
    private void exportSecondaryTargetFiles(List p_stfIds, 
                                            HttpSession p_session, 
                                            List p_stfWorkflowIds, 
                                            HashMap p_stfMap)
        throws EnvoyServletException
    {
        long exportBatchId = -1;
        try
        {
            if (p_stfIds.size() > 0)
            {
                exportBatchId = 
                    ExportEventObserverHelper.notifyBeginExportTargetBatch(
                        m_job,
                        PageHandler.getUser(p_session), p_stfIds,
                        p_stfWorkflowIds, null, m_stfExportType);
            }
        }  
        catch(Exception e)
        { 
            throw new EnvoyServletException(e);
        }

        Object[] keys = p_stfMap.keySet().toArray();
        for (int h = 0; h < keys.length; h++)
        {
            Workflow workflow = (Workflow)keys[h];
            exportStfs(workflow, getTargetPageIds(
                (HashSet)p_stfMap.get(workflow)), exportBatchId);
        }
    }

    // Export the Secondary Target Files
    private void exportStfs(Workflow p_workflow,
                            List p_listOfStfIds, 
                            long p_exportBatchId)
        throws EnvoyServletException
    {
        try
        {
            long id = p_workflow.getId();
            String locale = p_workflow.getTargetLocale().toString();
            String codeSetParam = getConcatenatedRequestParameter(
                JobManagementHandler.EXPORT_WF_CODE_PARAM,id, locale);
            String exportLocation = getConcatenatedRequestParameter(
                JobManagementHandler.EXPORT_WF_LOCATION_PARAM, id, locale);
            String localeSubDir = getConcatenatedRequestParameter(
                JobManagementHandler.EXPORT_WF_LOCALE_SUBDIR_PARAM,id,locale);
            ExportParameters ep = 
            new ExportParameters(p_workflow, codeSetParam, 
                                 exportLocation, localeSubDir,
                                 ExportConstants.EXPORT_STF);
        
            ServerProxy.getPageManager().exportSecondaryTargetFiles(
                ep, p_listOfStfIds, p_exportBatchId);
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
        CATEGORY.debug("List of target pageIds to export: " + list);
        return list;
    }

    private Workflow getWorkflowById(long p_wfId)
        throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getWorkflowManager()
                    .getWorkflowById(m_request.getSession().getId(), 
                                     p_wfId);
        }
        catch(Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }


    private void performExportForUpdate()
    throws Exception
    {
        long jobId = Long.parseLong(m_request.getParameter(
            JobManagementHandler.JOB_ID));
        Job job = WorkflowHandlerHelper.getJobById(jobId);
        /* set the job */
        m_job = job;

        // validate page states before exporting source pages
        PageStateValidator.validateStateOfPagesInJob(job);

        Iterator iter = job.getSourcePages().iterator();
        ArrayList idList = new ArrayList();
        while (iter.hasNext())
        {
            SourcePage sp = (SourcePage) iter.next();
            long pageId = sp.getId();
            idList.add(new Long(pageId));
        }

        String codeSetParam = 
        m_request.getParameter(JobManagementHandler.EXPORT_WF_CODE_PARAM +
                               "_" +
                               jobId);
        String exportLocation = 
        m_request.getParameter(JobManagementHandler
                               .EXPORT_WF_LOCATION_PARAM +
                               "_" +
                               jobId);
        String localeSubDir = 
        m_request.getParameter(JobManagementHandler
                               .EXPORT_WF_LOCALE_SUBDIR_PARAM +
                               "_" +
                               jobId);

        ExportParameters ep =  new ExportParameters(
            null, codeSetParam, exportLocation, localeSubDir);
        int minutesOfDelay = Integer.parseInt(
            m_request.getParameter(JobExportHandler.PARAM_DELAY));

	HttpSession session = m_request.getSession(false);
        User user = PageHandler.getUser(session);

        CATEGORY.info("Exporting source pages in " + minutesOfDelay + 
                      " minutes. Export initiated by " + user);
        DelayedExportRequest der = new DelayedExportRequest(
            ep, idList, false, minutesOfDelay, jobId, user);
        DelayedExporter.getInstance().delayExport(der, job);
    }


    private List performExportPageLevel() throws PageException,
            EnvoyServletException
    {
        // Get all the checked pages as an array of strings
        String pages[] = m_request.getParameterValues("page");
        HttpSession session = m_request.getSession(false);
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
        
        ArrayList<String> parentFolders = new ArrayList<String>();
        TargetPage[] targetPages = new TargetPage[pages.length];
        TargetPage tp = null;
        SourcePage sp = null;
        String externalId = "";
        String pathName = "";
        String tmpName = "";
        String exportSubDir = "";
        
        // Find the workflows associated with the pages checked
        for (int i = 0; i < pages.length; i++)
        {       
            // The value of the page param is "pageId_<pageId>_wfId_<p or s><wfId>
            // so we must extract it using substring()
            Long curPageId = new Long(pages[i].substring(
                pages[i].indexOf("pageId_") + 7, 
                pages[i].indexOf("_wfId")));
            
            String wfId = pages[i].substring(pages[i].indexOf("_wfId_") + 
                                             7, pages[i].length());
            
            if (pages[i].indexOf(
                "_wfId_"+JobExportHandler.PRIMARY_PREFIX) != -1)
            {
                preparePrimaryTargetInfo(pageIds, curPageId, 
                                         map, workflowIds);
            }
            else
            {
                prepareSecondaryTargetInfo(stfIds, curPageId, wfMap, 
                                           wfId, stfMap, stfWorkflowIds);
            }            
            
            /**
             * GBS-1785, XLZ Support Added by Vincent Yan, 2011/01/26 Get all
             * parent folders which are used to store the exported target files.
             * For now, it just process the xlf/xliff file format.
             */
            tp = WorkflowHandlerHelper.getTargetPage(curPageId.longValue());
            targetPages[i] = tp;
            externalId = tp.getSourcePage().getExternalPageId();
            tmpName = externalId.substring(externalId.lastIndexOf(".") + 1);
            if (tmpName != null
                    && (tmpName.equalsIgnoreCase("xlf") || tmpName
                            .equalsIgnoreCase("xliff")))
            {
                exportSubDir = tp.getExportSubDir();
                pathName = externalId.substring(0,
                        externalId.lastIndexOf(File.separator));
                if (!parentFolders.contains(pathName))
                {
                    parentFolders.add(pathName);
                }
            }
        }

        // before exporting, let's make sure no page is in UPDATING state
        PageStateValidator.validateStateOfPagesInWorkflows(map.keySet().toArray());

        List wfList = new ArrayList();       
        // first export primary target files (if any)
        exportPrimaryTargetFiles(pageIds, session, workflowIds, map);
        wfList.addAll(map.keySet());
        // now export secondary target files (if any)
        exportSecondaryTargetFiles(stfIds, session, stfWorkflowIds,stfMap);
        wfList.removeAll(stfMap.keySet());
        wfList.addAll(stfMap.keySet());
        
        // GBS-1785, added by Vincent Yan, 2011/01/26
        if (parentFolders.size() > 0)
        {
            File tmpTargetFile = null;
            String tmpFilename = "";
            String tmpSourceFilename = "";
            String baseDocDir = AmbFileStoragePathUtils.getCxeDocDirPath()
                    .concat(File.separator);
            String companyId = "", companyName = "";
            tp = targetPages[0];
            companyId = tp.getSourcePage().getCompanyId();
            companyName = CompanyWrapper.getCompanyNameById(companyId);
            if ("1".equals(CompanyWrapper.getCurrentCompanyId()))
            {
                baseDocDir += companyName + File.separator;
            }

            // To verify if all target files are exported
            for (int i = 0; i < targetPages.length; i++)
            {
                tp = targetPages[i];
                tmpSourceFilename = tp.getExternalPageId();
                tmpSourceFilename = tmpSourceFilename
                        .substring(tmpSourceFilename.indexOf(File.separator));
                tmpFilename = baseDocDir + targetPages[i].getExportSubDir()
                        + File.separator + tmpSourceFilename;
                tmpTargetFile = new File(tmpFilename);
                while (!tmpTargetFile.exists())
                {
                    try
                    {
                        Thread.sleep(500);
                    }
                    catch (InterruptedException e)
                    {
                        CATEGORY.error(e);
                    }
                }
            }

            // Process exported files and generate XLZ file
            processXLZFiles(baseDocDir, parentFolders, exportSubDir);
        }
        
        return wfList;
    }
    

    /*
     * Prepare the information used for exporting a list of
     * primary target files.  This method is called within a for
     * loop.
     */
    private void preparePrimaryTargetInfo(List p_pageIds, Long p_curPageId,
                                          HashMap p_map, List p_workflowIds)
        throws EnvoyServletException
    {
        p_pageIds.add(p_curPageId);
        TargetPage targetPage = 
            WorkflowHandlerHelper.getTargetPage(p_curPageId.longValue());
        Workflow workflow = targetPage.getWorkflowInstance();

        HashSet set = (HashSet)p_map.get(workflow);
        if (set == null)
        {
            set = new HashSet();
            p_map.put(workflow, set);
            p_workflowIds.add(workflow.getIdAsLong());
        }
        set.add(p_curPageId);

        // use the last workflow to determine the export type
        m_exportType = workflow.getState().equals(Workflow.LOCALIZED) ? 
            ExportBatchEvent.FINAL_PRIMARY : 
                ExportBatchEvent.INTERIM_PRIMARY;

        // only set job once.
        if (m_job == null)
        {
            m_job = workflow.getJob();
        }
    }

    /**
     * Prepare the information used for exporting a list of
     * secondary target files.  This method is called within a for
     * loop.
     */
    private void prepareSecondaryTargetInfo(List p_stfIds, Long p_curPageId,
                                            HashMap p_wfMap, String p_wfId, 
                                            HashMap p_stfMap, 
                                            List p_stfWorkflowIds)
        throws EnvoyServletException
    {
        p_stfIds.add(p_curPageId);
        Workflow workflow = (Workflow)p_wfMap.get(p_wfId);
        if (workflow == null)
        {
            workflow = getWorkflowById(Long.parseLong(p_wfId));
                
            p_wfMap.put(p_wfId, workflow);
        }
        
        HashSet set = (HashSet)p_stfMap.get(workflow);
        if (set == null)
        {
            set = new HashSet();
            p_stfMap.put(workflow, set);
            p_stfWorkflowIds.add(workflow.getIdAsLong());
        }
        set.add(p_curPageId);

        // use the last workflow to determine the export type
        m_stfExportType = workflow.getState().equals(Workflow.LOCALIZED) ? 
            ExportBatchEvent.FINAL_SECONDARY : 
                ExportBatchEvent.INTERIM_SECONDARY;

        // only set job once.
        if (m_job == null)
        {
            m_job = workflow.getJob();
        }
    }

    /**
    * Concatenates the strings to create the desired request parameter name.
    * If the value does not exist, then it is tried without the original suffix,
    * but with the locale instead. format is "prefix_suffix" or "prefix_locale"
    */
    private String getConcatenatedRequestParameter(String p_prefix,
                                                   long p_suffix,
                                                   String p_locale)
    {
        String newPrefix = p_prefix + "_";
        String value = m_request.getParameter(newPrefix + p_suffix);
        if (value == null)
        {
            value = m_request.getParameter(newPrefix + p_locale);
        }

        return value;
    }
    
    /**
     * Generate exported XLZ file if the source file is XLZ file format After
     * generating XLZ file, the folders which used to store exported xliff files
     * and other files in original XLZ source file will be removed.
     * 
     * @param p_folders
     * @param p_exportSubDir
     * @author Vincent Yan, 2011/01/26
     */
    private void processXLZFiles(String p_baseDocDir, ArrayList<String> p_folders,
            String p_exportSubDir)
    {
        String baseDocDir = p_baseDocDir;
        String filePath = "";
        String pathName = "";
        String sourcePath = "", targetPath = "";
        File fSourcePath, fTargetPath;
        File file = null;
        String tmp = "";
        File targetXLZFile = null;
        try
        {
            for (String fn : p_folders)
            {
                filePath = baseDocDir + File.separator + fn + ".xlz";
                file = new File(filePath);
                if (file.exists() && file.isFile())
                {
                    // Exist a xlz file with the special folder name
                    sourcePath = baseDocDir + File.separator + fn;
                    targetPath = baseDocDir + File.separator + p_exportSubDir
                            + File.separator
                            + fn.substring(fn.indexOf(File.separator) + 1);
                    fSourcePath = new File(sourcePath);
                    fTargetPath = new File(targetPath);
                    for (File f : fSourcePath.listFiles())
                    {
                        tmp = f.getName().toLowerCase();
                        if (!tmp.endsWith(".xlf") && !tmp.endsWith(".xliff"))
                        {
                            FileUtils.copyFileToDirectory(f, fTargetPath);
                        }
                    }

                    // Generate the output XLZ file
                    targetXLZFile = new File(targetPath + ".xlz");
                    ZipIt.addEntriesToZipFile(targetXLZFile,
                            fTargetPath.listFiles(), true, "");

                    // Remove the temporary folders
                    //FileUtils.deleteAllFilesSilently(targetPath);
                }
            }
        }
        catch (Exception e)
        {
            CATEGORY.error(e);
        }
    }
}
