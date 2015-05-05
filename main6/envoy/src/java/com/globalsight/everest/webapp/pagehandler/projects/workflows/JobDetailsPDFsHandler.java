/**
 *  Copyright 2013 Welocalize, Inc. 
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.everest.company.Company;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.foundation.Timestamp;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.ActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.reports.ReportHelper;
import com.globalsight.everest.webapp.pagehandler.edit.online.previewPDF.PreviewPDFHelper;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.StringUtil;
import com.globalsight.util.zip.ZipIt;

/**
 * The PageHandler for PDF Tab in JobDetails Page.
 */
public class JobDetailsPDFsHandler extends PageActionHandler implements JobDetailsConstants
{
    @ActionHandler(action = ACTION_VIEW_JOBDETAILS, formClass = "")
    public void viewJobDetails(HttpServletRequest p_request, 
            HttpServletResponse p_response, Object form) throws Exception
    {
        JobSummaryHelper jobSummaryHelper = new JobSummaryHelper();
        Job job = jobSummaryHelper.getJobByRequest(p_request);
        jobSummaryHelper.packJobSummaryInfoView(p_request, p_response, 
                p_request.getServletContext(), job);
        List<JobDetailsPDFsBO> JobDetailsPDFList = getPDFDisplayList(p_request, job);
        p_request.setAttribute("JobDetailsPDFList", JobDetailsPDFList);
    }
    
    @ActionHandler(action = ACTION_PDF_CREATE, formClass = "")
    public void createPDFs(HttpServletRequest p_request, 
            HttpServletResponse p_response, Object p_form) throws Exception
    {
        HttpSession session = p_request.getSession();
        String userId = (String) session.getAttribute(WebAppConstants.USER_NAME);
        String wfids = p_request.getParameter("wfids"); 
        Set<Long> workflowIdSet = getLongSet(wfids);
        new PreviewPDFHelper().createPDF(workflowIdSet, userId);
        pageReturn();
    }
    
    @ActionHandler(action = ACTION_PDF_CANCEL, formClass = "")
    public void cancel(HttpServletRequest p_request, 
            HttpServletResponse p_response, Object p_form) throws Exception
    {
        HttpSession session = p_request.getSession();
        String userId = (String) session.getAttribute(WebAppConstants.USER_NAME);
        String wfids = p_request.getParameter("wfids"); 
        Set<Long> workflowIdSet = getLongSet(wfids);
        new PreviewPDFHelper().cancelPDF(workflowIdSet, userId);
        pageReturn();
    }
    
    @ActionHandler(action = ACTION_PDF_DOWNLOAD, formClass = "")
    public void downloadPDFs(HttpServletRequest p_request, 
            HttpServletResponse p_response, Object p_form) throws Exception
    {
        HttpSession session = p_request.getSession();
        String userId = (String) session.getAttribute(WebAppConstants.USER_NAME);
        String wfids = p_request.getParameter("wfids"); 
        Set<Long> workflowIdSet = getLongSet(wfids);
        Set<File> pdfs = PreviewPDFHelper.getPreviewPdf(workflowIdSet, userId);
        String basicFileName = getBasicFileName(workflowIdSet);
        // Send PDF files to Browser
        if (pdfs.size() == 1)
        {
            ReportHelper.sendFiles(pdfs, basicFileName + ".pdf", p_response, false);
        }
        else
        {
            File zipFile = new File(basicFileName + ".zip");
            StringBuffer excludePathBuf = new StringBuffer();
            User user = ServerProxy.getUserManager().getUser(userId);            
            Company company = CompanyWrapper.getCompanyByName(user.getCompanyName());
            excludePathBuf.append(AmbFileStoragePathUtils.getPdfPreviewDir(company.getId()).getAbsolutePath());
            excludePathBuf.append(File.separator);
            excludePathBuf.append(userId).append(File.separator);
            String excludePath = excludePathBuf.toString();
            if (File.separator.equals("\\"))
            {
                excludePath = excludePath.replace("/", File.separator);
            }
            ZipIt.addEntriesToZipFile(zipFile, pdfs, excludePath.toString(), "");
            File[] files = { zipFile };
            ReportHelper.sendFiles(files, null, p_response, true);
        }
        pageReturn();
    }
    
    /**
     * Get the JSON Array of JobDetailsPDFsBO, by workflow IDS.
     */
    @ActionHandler(action = ACTION_VIEW_PDFBO, formClass = "")
    public void getPDFBO(HttpServletRequest p_request, 
            HttpServletResponse p_response, Object p_form) throws Exception
    {
        HttpSession session = p_request.getSession();
        String userId = (String) session.getAttribute(WebAppConstants.USER_NAME);
        User user = ServerProxy.getUserManager().getUser(userId);
        String wfids = p_request.getParameter("wfids"); 
        Set<Long> workflowIdSet = getLongSet(wfids);
        Set<JobDetailsPDFsBO> dataSet = new HashSet<JobDetailsPDFsBO>();
        for(Long workflowId : workflowIdSet)
        {
            Workflow wf = ServerProxy.getWorkflowManager().getWorkflowById(workflowId);
            JobDetailsPDFsBO data = new JobDetailsPDFsBO(wf);
            new PreviewPDFHelper().setJobDetailsPDFsBO(wf, data, user.getUserId());
            dataSet.add(data);
        }
        
        StringBuilder json = new StringBuilder();
        if (dataSet.size() > 0)
        {
            for (JobDetailsPDFsBO data : dataSet)
            {
                json.append(data.toJSON()).append(",");
            }
            json.delete(json.lastIndexOf(","), json.length());
            json.insert(0, "[");
            json.append("]");
        }

        p_response.getWriter().write(json.toString());
        pageReturn();
    }
    
    public void beforeAction(HttpServletRequest p_request, HttpServletResponse p_response) throws ServletException,
            IOException, EnvoyServletException
    {
        
    }

    public void afterAction(HttpServletRequest p_request, HttpServletResponse p_response) throws ServletException,
            IOException, EnvoyServletException
    {
        
    }
    
    private List<JobDetailsPDFsBO> getPDFDisplayList(
            HttpServletRequest p_request, Job p_job)
    {
        List<JobDetailsPDFsBO> result = new ArrayList<JobDetailsPDFsBO>();
        HttpSession session = p_request.getSession(false);
        ResourceBundle bundle = getBundle(session);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        PermissionSet perms = (PermissionSet) session
                .getAttribute(WebAppConstants.PERMISSIONS);
        User user = (User) sessionMgr.getAttribute(USER);
        Locale uiLocale = (Locale) session.getAttribute(UILOCALE);
        TimeZone timezone = (TimeZone) session.getAttribute(USER_TIME_ZONE);
        Timestamp ts = new Timestamp(Timestamp.DATE, timezone);
        ts.setLocale(uiLocale);
        List<Workflow> workflows = new ArrayList<Workflow>(p_job.getWorkflows());
        Collections.sort(workflows, new WorkflowComparator(Locale.getDefault()));
        int wordCount = 0;
        for (Workflow workflow : workflows)
        {
            if (workflow.getState().equals(Workflow.CANCELLED))
                continue;
            if (!perms.getPermissionFor(Permission.JOB_SCOPE_ALL)
                    && !perms.getPermissionFor(Permission.JOB_SCOPE_MYPROJECTS)
                    && (p_job.getProject().getProjectManagerId() != user
                            .getUserId())
                    && PageHandler.invalidForWorkflowOwner(user.getUserId(),
                            perms, workflow))
                continue;

            JobDetailsPDFsBO data = new JobDetailsPDFsBO(workflow);

            if (wordCount == 0)
            {
                for (TargetPage tp : workflow.getAllTargetPages())
                {
                	String externalPageId = tp.getExternalPageId();
    				String extension = externalPageId.substring(
    						externalPageId.lastIndexOf(".")).toLowerCase();
                	if (PreviewPDFHelper.extensionSet.contains(extension))
                	{
                		wordCount += tp.getWordCount().getTotalWordCount();
                	}
                }
            }

            data.setTargetLocaleDisplayName(workflow
                    .getTargetLocale().getDisplayName(uiLocale));
            data.setTotalWordCount(wordCount);
            new PreviewPDFHelper().setJobDetailsPDFsBO(workflow, data, user.getUserId());
            setStatusDisplayName(data, bundle);
            
            result.add(data);
        }

        return result;
    }
    
    // Set Status Display Name for page view.
    private void setStatusDisplayName(JobDetailsPDFsBO p_pdfBO, ResourceBundle p_bundle)
    {
        String statusDisplayName = p_bundle.getString(p_pdfBO.getStatus());
        p_pdfBO.setStatusDisplayName(statusDisplayName);
    }
    
    private Set<Long> getLongSet(String p_str)
    {
        Set<Long> result = new HashSet<Long>();
        if(p_str.startsWith("[") && p_str.endsWith("]"))
            p_str = p_str.substring(1, p_str.length() - 1);
        
        for(String str : p_str.split(","))
        {
            if(StringUtil.isNotEmpty(str))
            {
                result.add(Long.valueOf(str.trim()));
            }
        }
        return result;
    }

    // Get Download Prefix File Name.
    private String getBasicFileName(Set<Long> workflowIdSet)
    {
        StringBuffer fileName = new StringBuffer("GSPDF");
        try
        {
            if (workflowIdSet.size() >= 1)
            {
                Workflow wf = ServerProxy.getWorkflowManager().getWorkflowById(workflowIdSet.iterator().next());
                Job job = wf.getJob();
                fileName.append("-(").append(job.getJobId()).append(")")
                        .append("(").append(job.getJobName()).append(")");
                if (workflowIdSet.size() == 1)
                {
                    fileName.append("(").append(wf.getTargetLocale()).append(")");
                }
            }
        }
        catch (Exception e)
        {
        }
        
        return fileName.toString();
    }
}
