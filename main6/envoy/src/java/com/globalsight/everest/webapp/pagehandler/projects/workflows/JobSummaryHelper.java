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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TimeZone;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.globalsight.connector.blaise.util.BlaiseManager;
import com.globalsight.cxe.adapter.msoffice.OfficeXmlHelper;
import com.globalsight.cxe.entity.blaise.BlaiseConnectorJob;
import com.globalsight.cxe.util.EventFlowXmlParser;
import com.globalsight.everest.comment.CommentManager;
import com.globalsight.everest.comment.Issue;
import com.globalsight.everest.company.Company;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.CookieUtil;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.edit.online.previewPDF.PreviewPDFHelper;
import com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.StringUtil;
import com.globalsight.util.date.DateHelper;

public class JobSummaryHelper
{
    private static final Logger CATEGORY = Logger.getLogger(JobSummaryHelper.class);
    protected static boolean s_isCostingEnabled = false;
    protected static boolean s_isRevenueEnabled = false;
    
    static
    {
        try
        {
            SystemConfiguration sc = SystemConfiguration.getInstance();
            s_isCostingEnabled = sc.getBooleanParameter(SystemConfigParamNames.COSTING_ENABLED);
            s_isRevenueEnabled = sc.getBooleanParameter(SystemConfigParamNames.REVENUE_ENABLED);
        }
        catch (Throwable e)
        {
            CATEGORY.error("JobSummaryHelper::invokeJobControlPage(): " + "Problem getting costing parameter from database.", e);
        }
    }
    
    public boolean packJobSummaryInfoView(HttpServletRequest p_request,
            HttpServletResponse p_response, ServletContext p_context, Job job)
            throws EnvoyServletException, ServletException, IOException
    {
        if (job == null)
        {
            jobNotFound(p_request, p_response, p_context, job);
            return false;
        }
		
        // check user permission for this job
        HttpSession session = p_request.getSession(false);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(PageHandler.SESSION_MANAGER);
        User user = (User) sessionMgr.getAttribute(PageHandler.USER);
        
        // 1. user can not be null
        if (user == null)
        {
            jobNotFound(p_request, p_response, p_context, job);
            return false;
        }
        // 2. user and job should belong to same company
        String userComName = user.getCompanyName();
        if (!CompanyWrapper.isSuperCompanyName(userComName))
        {
            Company userC = CompanyWrapper.getCompanyByName(userComName);
            if (job.getCompanyId() != userC.getId())
            {
                job = null;
                jobNotFound(p_request, p_response, p_context, job);
                return false;
            }
        }
        
        packJobSummaryInfoView(p_request, job);
        
        return true;
    }
    
    private void packJobSummaryInfoView(HttpServletRequest p_request, Job job)
            throws EnvoyServletException
    {
		HttpSession session = p_request.getSession(false);
		List<Integer> priorityList = new ArrayList<Integer>();
		priorityList.add(1);
		priorityList.add(2);
		priorityList.add(3);
		priorityList.add(4);
		priorityList.add(5);
	
		SessionManager sessionMgr = (SessionManager) session
				.getAttribute(WebAppConstants.SESSION_MANAGER);
		Locale uiLocale = (Locale) session
				.getAttribute(WebAppConstants.UILOCALE);

		p_request.setAttribute("priorityList", priorityList);
		p_request.setAttribute("Job", job);
		p_request.setAttribute("jobId", job.getJobId());
		p_request.setAttribute("dateCreated",
				getDateCreated(session, uiLocale, job));
		p_request.setAttribute("jobInitiator", getInitiatorUserName(job));
		p_request.setAttribute("srcLocale", job.getSourceLocale()
				.getDisplayName(uiLocale));
		p_request.setAttribute("openSegmentCommentsCount",
				getOpenSegmentCommentsCount(job, session));
		p_request.setAttribute("closedSegmentCommentsCount",
				getClosedSegmentCommentsCount(job, session));
		p_request.setAttribute("isFinshedJob", isFinishedJob(job));
		p_request.setAttribute("isCostingEnabled", s_isCostingEnabled);
		p_request.setAttribute("jobCostsTabPermission",
				getJobCostsTabPermission(session));
		p_request.setAttribute("isPreviewPDF",
				PreviewPDFHelper.isEnablePreviewPDF(job));

		// Number of languages
        p_request.setAttribute("numberLanguages", job.getWorkflows().size());
        // total source wordcount
        List<Workflow> workflows = new ArrayList<Workflow>(job.getWorkflows());
        int totalWord = 0;
        if(workflows.size() > 0)
        {
            totalWord = workflows.get(0).getTotalWordCount();
        }
        p_request.setAttribute("sourceWordCount", totalWord);
        // Set extra info for PPTX and DOCX files (GBS-3373) 
        setPptxDocxInfo(job, p_request);

        if (job.isBlaiseJob())
        {
            List<BlaiseConnectorJob> blaiseJobEntries = BlaiseManager
                    .getBlaiseConnectorJobByJobId(job.getId());
            p_request.setAttribute("blaiseUploadXliffState",
                    decideBlaiseUploadState(blaiseJobEntries));
            p_request.setAttribute("blaiseCompleteState",
                    decideBlaiseCompleteState(blaiseJobEntries));
        }

        // Edit Source page word counts and edit costs page needed
		sessionMgr.setAttribute(JobManagementHandler.JOB_NAME_SCRIPTLET,
				job.getJobName());
	}

    private void setPptxDocxInfo(Job job, HttpServletRequest p_request)
    {
        String soureLocale = job.getSourceLocale().toString();

        int numPagesDocx = 0;
        int numPagesPptx = 0;
        int countDocx = 0;
        int countPptx = 0;

        Set<String> handledSafeBaseFiles = new HashSet<String>();
        String externalPageId = null;
        String eventFlowXml = null;
        List<SourcePage> sourcePages = (List<SourcePage>) job.getSourcePages();
        for (SourcePage sourcePage : sourcePages)
		{
			// m_externalPageId
			externalPageId = sourcePage.getExternalPageId();
			if (externalPageId.toLowerCase().endsWith("docx")
					|| externalPageId.toLowerCase().endsWith("pptx"))
			{
				eventFlowXml = sourcePage.getRequest().getEventFlowXml();
				String safeBaseFilename = getOffice2010SafeBaseFileName(eventFlowXml);
				if (StringUtil.isNotEmpty(safeBaseFilename)
						&& !handledSafeBaseFiles.contains(safeBaseFilename))
				{
					if (externalPageId.toLowerCase().endsWith("docx"))
					{
						numPagesDocx += getPageCount(safeBaseFilename,
								soureLocale, "docx");
						countDocx++;
					}
					else if (externalPageId.toLowerCase().endsWith("pptx"))
					{
						numPagesPptx += getPageCount(safeBaseFilename,
								soureLocale, "pptx");
						countPptx++;
					}
				}

				if (StringUtil.isNotEmpty(safeBaseFilename))
				{
					handledSafeBaseFiles.add(safeBaseFilename);
				}
			}
		}

        p_request.setAttribute("numPagesDocx", numPagesDocx);
        p_request.setAttribute("numPagesPptx", numPagesPptx);
        p_request.setAttribute("countDocx", countDocx);
        p_request.setAttribute("countPptx", countPptx);
    }

    public String getOffice2010SafeBaseFileName(String eventFlowXml)
    {
        EventFlowXmlParser evenFlowXmlParser = new EventFlowXmlParser();
        String safeBaseFileName = null;
        try
        {
			if (StringUtil.isNotEmpty(eventFlowXml))
			{
				evenFlowXmlParser.parse(eventFlowXml);
				Element category = evenFlowXmlParser
						.getCategory("OfficeXmlAdapter");
				if (category != null)
				{
					String[] safeBaseFileNames = evenFlowXmlParser
							.getCategoryDaValue(category, "safeBaseFileName");
					if (safeBaseFileNames != null
							&& safeBaseFileNames.length == 1)
					{
						safeBaseFileName = safeBaseFileNames[0];
					}
				}
			}
        }
        catch (Exception ignore)
        {

        }

        return safeBaseFileName;
    }

    /**
     * Docx and pptx files to get a number of pages
     * @param map
     * @param job
     * */
    public int getPageCount(String safeBaseFilename, String soureLocale,
            String fileType)
    {
        File appXmlFile = getOffice2010AppXmlFile(safeBaseFilename, soureLocale,fileType);

        String appXmlFileContent = readOffice2010AppXmlFile(appXmlFile);

        return getInfoFromAppXml(appXmlFileContent, fileType);
    }

	/**
	 * Get the "app.xml" file object.
	 * @param safeBaseFileName
	 * @param soureLocale
	 * @return File
	 */
    private File getOffice2010AppXmlFile(String safeBaseFileName,
            String soureLocale,String fileType)
    {
        StringBuffer soureLocalePath = new StringBuffer();
        // Get the storage dir for company base on the parameter p_companyId
        soureLocalePath
                .append(AmbFileStoragePathUtils.getFileStorageDirPath(1))
                .append(File.separator).append("OfficeXml-Conv")
                .append(File.separator).append(soureLocale)
                .append(File.separator);

        StringBuffer appXmlPath = new StringBuffer();
        StringBuilder path = new StringBuilder();
        path.append(soureLocalePath.toString());
        path.append(safeBaseFileName);
        path.append(".");
        if ("docx".equals(fileType))
        {
           path.append(OfficeXmlHelper.OFFICE_DOCX);
        }
        else if ("pptx".equals(fileType))
        {
           path.append(OfficeXmlHelper.OFFICE_PPTX);
        }
        File file = new File(path.toString());
        if (file.exists() && file.isDirectory())
        {
            appXmlPath.append(path);
        }
        else
        {
            return null;
        }

        appXmlPath.append(File.separator).append("docProps")
                .append(File.separator).append("app.xml");
        File appXmlFile = new File(appXmlPath.toString());
        if (appXmlFile.exists() && appXmlFile.isFile())
            return appXmlFile;
        else
            return null;
    }

    private String readOffice2010AppXmlFile(File appXmlFile)
    {
		if (appXmlFile == null || !appXmlFile.exists()
				|| appXmlFile.isDirectory())
			return null;

        StringBuffer appXml = new StringBuffer();
        BufferedReader reader = null;
        try
        {
            reader = new BufferedReader(new FileReader(appXmlFile));
			if (reader != null)
			{
				String tempString = null;
				while ((tempString = reader.readLine()) != null)
				{
					appXml.append(tempString);
				}
			}
            reader.close();
        }
        catch (Exception e)
        {
            CATEGORY.error(e);
        }

        return appXml.toString();
    }

    private int getInfoFromAppXml(String appXml, String fileType)
    {
        try
		{
			if (StringUtil.isEmpty(appXml))
				return 0;
			StringReader sr = new StringReader(appXml);
			InputSource is = new InputSource(sr);
			DOMParser parser = new DOMParser();
			String numStr = null;
			parser.setFeature("http://xml.org/sax/features/validation", false);
			parser.parse(is);
			Element rootElement = parser.getDocument().getDocumentElement();
			if (fileType.equals("pptx"))
			{
				NodeList slidesNodeList = rootElement
						.getElementsByTagName("Slides");
				if (slidesNodeList != null && slidesNodeList.getLength() > 0)
				{
					numStr = slidesNodeList.item(0).getFirstChild()
							.getNodeValue();
				}
			}
			else if (fileType.equals("docx"))
			{
				NodeList pagesNodeList = rootElement
						.getElementsByTagName("Pages");
				if (pagesNodeList != null && pagesNodeList.getLength() > 0)
				{
					numStr = pagesNodeList.item(0).getFirstChild()
							.getNodeValue();
				}
			}
			if (StringUtil.isNotEmpty(numStr))
			{
				return Integer.parseInt(numStr);
			}
		}
        catch (Exception e)
        {
            CATEGORY.error(e);
        }

        return 0;
    }
	
    public Job getJobByRequest(HttpServletRequest p_request)
    {
        HttpSession session = p_request.getSession(false);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        Job job = WorkflowHandlerHelper.getJobById(getJobId(p_request,
                sessionMgr));

        // Comment Page will retrieve it,but I don't know it's reason since now.
        TaskHelper.storeObject(session, WebAppConstants.WORK_OBJECT, job);
        return job;
    }
    
    private String getDateCreated(HttpSession session, Locale uiLocale, Job job)
    {
        TimeZone timeZone = (TimeZone) session.getAttribute(WebAppConstants.USER_TIME_ZONE);
        String dateCreated = DateHelper.getFormattedDateAndTime(job.getCreateDate(),uiLocale, timeZone);
        return dateCreated;
    }

    private String getInitiatorUserName(Job job)
    {
        User initiator = job.getCreateUser();
        if (initiator == null)
        {
            initiator = job.getProject().getProjectManager();
        }
        return initiator.getUserName();
    }
    
    public void jobNotFound(HttpServletRequest p_request,
            HttpServletResponse p_response, ServletContext p_context, Job job)
            throws ServletException, IOException, EnvoyServletException
    {
        HttpSession session = p_request.getSession(false);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(PageHandler.SESSION_MANAGER);
        ResourceBundle bundle = PageHandler.getBundle(session);

        String jobname = null;
        if (job == null)
        {
            jobname = Long.toString(getJobId(p_request,
                    sessionMgr));
        }
        else
        {
            jobname = job.getJobName();
            // remove from MRU list
            String cookieName = JobSearchConstants.MRU_JOBS_COOKIE
                    + session.getAttribute(WebAppConstants.USER_NAME).hashCode();

            CookieUtil.removeMRU(p_request, p_response, session,
                    job.getId() + ":" + job.getJobName(), cookieName, JobSearchConstants.MRU_JOBS);
        }
        p_request.setAttribute("badresults", bundle.getString("lb_job") + " "
                + jobname + " " + bundle.getString("msg_cannot_be_found"));
        // forward to the jsp page.
        sessionMgr.setMyjobsAttribute("badresults", bundle.getString("lb_job") + " "
                + jobname + " " + bundle.getString("msg_cannot_be_found"));
        String targetState = (String)sessionMgr.getMyjobsAttribute("lastState");
        if(Job.PENDING.equals(targetState))
        	p_response.sendRedirect("/globalsight/ControlServlet?activityName=jobsPending&searchType=stateOnly");
        else if(Job.READY_TO_BE_DISPATCHED.equals(targetState))
        	p_response.sendRedirect("/globalsight/ControlServlet?activityName=jobsReady&searchType=stateOnly");
        else if(Job.DISPATCHED.equals(targetState))
        	p_response.sendRedirect("/globalsight/ControlServlet?activityName=jobsInProgress&searchType=stateOnly");
        else if(Job.LOCALIZED.equals(targetState))
        	p_response.sendRedirect("/globalsight/ControlServlet?activityName=jobsLocalized&searchType=stateOnly");
        else if(Job.EXPORTED.equals(targetState))
        	p_response.sendRedirect("/globalsight/ControlServlet?activityName=jobsExported&searchType=stateOnly");
        else if(Job.ARCHIVED.equals(targetState))
        	p_response.sendRedirect("/globalsight/ControlServlet?activityName=jobsArchived&searchType=stateOnly");
        else
        	p_response.sendRedirect("/globalsight/ControlServlet?activityName=jobsAll&searchType=stateOnly");
    }
    
    
    /**
     * Returns the id of the job that an action is being performed on.
     */
    protected long getJobId(HttpServletRequest p_request,
            SessionManager p_sessionMgr)
    {
        long jobId = 0;
        // Get the jobId from the sessionMgr if it's not in the request.
        // This means you are coming to the Job Details screen from
        // somewhere other than the My Jobs screens.
        if (p_request.getParameterValues(WebAppConstants.JOB_ID) == null)
        {
            try
            {
                Object oJobId = p_sessionMgr.getAttribute(WebAppConstants.JOB_ID);
                // The type of oJobId may be Long or String.
                if (oJobId instanceof Long)
                {
                    jobId = ((Long) oJobId).longValue();
                }
                else
                {
                    jobId = Long.parseLong((String) oJobId);
                }
            }
            catch (Exception e)
            {
                CATEGORY.error("Failed to get job id from session manager ", e);
            }
        }
        else
        {
            String[] jobIdInfo = p_request.getParameterValues(JobManagementHandler.JOB_ID);
            String jobIdString = jobIdInfo[0];
            jobId = Long.parseLong(jobIdString);
            p_sessionMgr.setAttribute(JobManagementHandler.JOB_ID, new Long(jobId));
        }
        return jobId;
    }
    
    private int getOpenSegmentCommentsCount(Job job,HttpSession session){
        int openSegmentCount = 0;
        List<String> oStates = new ArrayList<String>();
        oStates.add(Issue.STATUS_OPEN);
        oStates.add(Issue.STATUS_QUERY);
        oStates.add(Issue.STATUS_REJECTED);
        openSegmentCount = getSegmentCommentsCount(job, session, oStates);
        return openSegmentCount;
    }
    
    private int getClosedSegmentCommentsCount(Job job,HttpSession session){
        int closedSegmentCount = 0;
        List<String> cStates = new ArrayList<String>();
        cStates.add(Issue.STATUS_CLOSED);
        closedSegmentCount = getSegmentCommentsCount(job, session, cStates);
        return closedSegmentCount;
    }
    
    private int getSegmentCommentsCount(Job job, HttpSession session,
            List<String> states) throws EnvoyServletException
    {
        Set<Long> targetPageIds = getTargetPageIdsByJob(job.getId());
        if (targetPageIds.size() == 0)
        {
            return 0;
        }

        try
        {
            CommentManager manager = ServerProxy.getCommentManager();
            return manager.getIssueCount(Issue.TYPE_SEGMENT,
                    new ArrayList<Long>(targetPageIds), states);
        }
        catch (Exception ex)
        {
            throw new EnvoyServletException(ex);
        }
    }

    /**
     * Get all target page IDs for all workflows in current job.
     */
    private Set<Long> getTargetPageIdsByJob(long jobId)
    {
        Set<Long> targetPageIds = new HashSet<Long>();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            String sql = "SELECT DISTINCT tp.ID FROM target_page tp, workflow wf "
                        + " WHERE wf.IFLOW_INSTANCE_ID = tp.WORKFLOW_IFLOW_INSTANCE_ID "
                        + " AND wf.STATE != 'CANCELLED' "
                        + " AND tp.STATE != 'IMPORT_FAIL' "
                        + " AND wf.JOB_ID = ?";
            con = DbUtil.getConnection();
            ps = con.prepareStatement(sql);
            ps.setLong(1, jobId);
            rs = ps.executeQuery();
            while (rs != null && rs.next())
            {
                long tpId = rs.getLong(1);
                targetPageIds.add(tpId);
            }
        }
        catch (Exception e)
        {
            
        }
        finally
        {
            DbUtil.silentClose(rs);
            DbUtil.silentClose(ps);
            DbUtil.silentReturnConnection(con);
        }

        return targetPageIds;
    }

    private boolean isFinishedJob(Job job)
    {
        if (Job.EXPORTED.equals(job.getState()) || 
           Job.EXPORT_FAIL.equals(job.getState()) ||
           Job.LOCALIZED.equals(job.getState()) || 
           Job.ARCHIVED.equals(job.getState()))
        {
            return true;
        }
        else 
        {
            return false;
        }
    }
    
    private boolean getJobCostsTabPermission(HttpSession session){
        PermissionSet perms = (PermissionSet) session.getAttribute(WebAppConstants.PERMISSIONS);
        boolean jobCostingView = perms.getPermissionFor(Permission.JOB_COSTING_VIEW);
        boolean jobQuoteSend = perms.getPermissionFor(Permission.JOB_QUOTE_SEND);
        boolean jobQuotePonumberView = perms.getPermissionFor(Permission.JOB_QUOTE_PONUMBER_VIEW);
        boolean jobQuoteApprove = perms.getPermissionFor(Permission.JOB_QUOTE_APPROVE);
        boolean jobQuoteStatusView = perms.getPermissionFor(Permission.JOB_QUOTE_STATUS_VIEW);
        
        return jobCostingView || jobQuoteSend || jobQuotePonumberView || jobQuoteApprove || jobQuoteStatusView;
    }

    /**
     * 1. One fail, all fail;
     * 2. One null, all null;
     * 3. All succeed, then succeed;
     */
    private String decideBlaiseUploadState(List<BlaiseConnectorJob> blaiseJobEntries)
    {
        if (blaiseJobEntries != null && blaiseJobEntries.size() > 0)
        {
            if (hasUploadFail(blaiseJobEntries))
            {
                return BlaiseConnectorJob.FAIL;
            }
            else if (hasUploadNullState(blaiseJobEntries))
            {
                return null;
            }
            else
            {
                return BlaiseConnectorJob.SUCCEED;
            }
        }

        return null;
    }

    // One fail, all fail
    private boolean hasUploadFail(List<BlaiseConnectorJob> blaiseJobEntries)
    {
        for (BlaiseConnectorJob bcj : blaiseJobEntries)
        {
            if (BlaiseConnectorJob.FAIL.equalsIgnoreCase(bcj.getUploadXliffState()))
            {
                return true;
            }
        }
        return false;
    }

    // One null, all null
    private boolean hasUploadNullState(List<BlaiseConnectorJob> blaiseJobEntries)
    {
        for (BlaiseConnectorJob bcj : blaiseJobEntries)
        {
            if (bcj.getUploadXliffState() == null)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * 1. One fail, all fail;
     * 2. One null, all null;
     * 3. All succeed, then succeed;
     */
    private String decideBlaiseCompleteState(List<BlaiseConnectorJob> blaiseJobEntries)
    {
        if (blaiseJobEntries != null && blaiseJobEntries.size() > 0)
        {
            if (hasCompleteFail(blaiseJobEntries))
            {
                return BlaiseConnectorJob.FAIL;
            }
            else if (hasCompleteNullState(blaiseJobEntries))
            {
                return null;
            }
            else
            {
                return BlaiseConnectorJob.SUCCEED;
            }
        }

        return null;
    }

    // One fail, all fail
    private boolean hasCompleteFail(List<BlaiseConnectorJob> blaiseJobEntries)
    {
        for (BlaiseConnectorJob bcj : blaiseJobEntries)
        {
            if (BlaiseConnectorJob.FAIL.equalsIgnoreCase(bcj.getCompleteState()))
            {
                return true;
            }
        }
        return false;
    }

    // One null, all null
    private boolean hasCompleteNullState(List<BlaiseConnectorJob> blaiseJobEntries)
    {
        for (BlaiseConnectorJob bcj : blaiseJobEntries)
        {
            if (bcj.getCompleteState() == null)
            {
                return true;
            }
        }
        return false;
    }
}
