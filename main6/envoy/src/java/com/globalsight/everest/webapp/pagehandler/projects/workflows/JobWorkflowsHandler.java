/**
 * Copyright 2009 Welocalize, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */
package com.globalsight.everest.webapp.pagehandler.projects.workflows;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jbpm.taskmgmt.exe.TaskInstance;
import org.w3c.dom.Element;

import com.alibaba.fastjson.JSONObject;
import com.globalsight.config.UserParamNames;
import com.globalsight.cxe.adapter.msoffice.OfficeXmlHelper;
import com.globalsight.cxe.adapter.openoffice.OpenOfficeHelper;
import com.globalsight.cxe.engine.util.XmlUtils;
import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.cxe.entity.filterconfiguration.JsonUtil;
import com.globalsight.cxe.persistence.fileprofile.FileProfilePersistenceManager;
import com.globalsight.everest.company.Company;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.company.MultiCompanySupportedThread;
import com.globalsight.everest.edit.EditHelper;
import com.globalsight.everest.foundation.ContainerRole;
import com.globalsight.everest.foundation.Timestamp;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobHandler;
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.jobhandler.jobcreation.JobCreationMonitor;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.persistence.tuv.SegmentTuTuvCacheManager;
import com.globalsight.everest.persistence.tuv.SegmentTuvUtil;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.statistics.StatisticsService;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.company.Select;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.webapp.pagehandler.tasks.UpdateLeverageHelper;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.everest.workflow.ScorecardData;
import com.globalsight.everest.workflow.ScorecardScore;
import com.globalsight.everest.workflow.ScorecardScoreHelper;
import com.globalsight.everest.workflow.WorkflowInstance;
import com.globalsight.everest.workflow.WorkflowTaskInstance;
import com.globalsight.everest.workflowmanager.JobWorkflowDisplay;
import com.globalsight.everest.workflowmanager.TaskJbpmUtil;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.everest.workflowmanager.WorkflowAdditionSender;
import com.globalsight.everest.workflowmanager.WorkflowImpl;
import com.globalsight.everest.workflowmanager.WorkflowManagerLocal;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.Entry;
import com.globalsight.util.FileUtil;
import com.globalsight.util.FormUtil;
import com.globalsight.util.StringUtil;
import com.globalsight.util.modules.Modules;

public class JobWorkflowsHandler extends PageHandler implements UserParamNames
{
    private static final Logger CATEGORY = Logger
            .getLogger(JobWorkflowsHandler.class);
    protected static boolean s_isGxmlEditorEnabled = false;
    private static boolean s_isSpecialCustomer = false; // Dell specific!
    private static String s_downloadDelayTimeAfterExporting; // Dell specific!
    private static Map<Long, Integer> updateWordCountsPercentageMap = Collections
            .synchronizedMap(new HashMap<Long, Integer>());

    static
    {
        try
        {
            SystemConfiguration sc = SystemConfiguration.getInstance();
            s_isGxmlEditorEnabled = EditHelper.isGxmlEditorInstalled();
            s_isSpecialCustomer = sc
                    .getBooleanParameter(SystemConfiguration.IS_DELL);
            s_downloadDelayTimeAfterExporting = sc
                    .getStringParameter(SystemConfigParamNames.DOWNLOAD_JOB_DELAY_TIME);
        }
        catch (Throwable e)
        {
            CATEGORY.error("JobHandlerMain::invokeJobControlPage(): "
                    + "Problem getting costing parameter from database.", e);
        }
    }

    @Override
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            RemoteException, EnvoyServletException
    {
		HttpSession httpSession = p_request.getSession(false);
		SessionManager sessionMgr = (SessionManager) httpSession
				.getAttribute(SESSION_MANAGER);
        JobSummaryHelper jobSummaryHelper = new JobSummaryHelper();
        Job job = jobSummaryHelper.getJobByRequest(p_request);
        String scorecardFlag = "";
        // deal with ajax request.Start.
        if (p_request.getParameter("obtainTime") != null)
        {
            PrintWriter out = p_response.getWriter();
            p_response.setContentType("text/html");
            out.write(getLeftTime(p_request, job));
            out.close();
            return;
        }
        else if (p_request.getParameter("changePriority") != null)
        {
            long wfId = Long.parseLong(p_request.getParameter("wfId"));
            int priority = Integer.parseInt(p_request.getParameter("priority"
                    + wfId));
            WorkflowManagerLocal wfManager = new WorkflowManagerLocal();
            wfManager.updatePriority(wfId, priority);
            PrintWriter out = p_response.getWriter();
            p_response.setContentType("text/html");
            out.write("OK");
            out.close();
            return;
        }
        else if(p_request.getParameter("checkIsUploadingForExport") != null)
        {
        	String[] wfIds = p_request.getParameter("wfId").split(" ");
        	for(String wfIdStr: wfIds)
        	{
        		long wfId = Long.parseLong(wfIdStr);
        		Workflow workflow =  WorkflowHandlerHelper.getWorkflowById(wfId);
        		Hashtable<Long, Task> tasks = workflow.getTasks();
        		String result = "";
        		for(Long taskKey:  tasks.keySet())
        		{
        			if(tasks.get(taskKey).getIsUploading() == 'Y')
        			{
        				result = "uploading";
        				break;
        			}
        		}
        		
        		if(result.length() > 0)
        		{
        			PrintWriter out = p_response.getWriter();
        			p_response.setContentType("text/html");
        			out.write(result);
        			out.close();
        			break;
        		}
        	}
            return;
        }
        else if ("getUpdateWCPercentage".equals(p_request
                .getParameter("action")))
        {
            getUpdateWordCountsPercentage(p_response, job.getJobId());
            return;
        }
        else if ("jobPageCount".equals(p_request.getParameter("action")))
        {
            // Clicked on Save on the Edit Pages screen, do the processing, then
            // send them to JobDetails
            // to see the updated value
            int p_unitOfWork = 3;
            long p_jobId = Long.parseLong(p_request.getParameter("jobId"));
            int numPages = Integer
                    .parseInt(p_request.getParameter("pageCount"));
            PrintWriter out = p_response.getWriter();
            p_response.setContentType("text/html");
            try
            {
                JobHandler jh = ServerProxy.getJobHandler();
                jh.updatePageCount(job, numPages);
                ServerProxy.getCostingEngine().setEstimatedAmountOfWorkInJob(
                        p_jobId, p_unitOfWork, numPages);
                out.write("OK");
            }
            catch (Exception e)
            {
                out.write("Error");
                throw new EnvoyServletException(
                        EnvoyServletException.EX_GENERAL, e);
            }
            finally
            {
                out.close();
            }
            return;
        }
        else if(("updateScorecard".equals(p_request.getParameter("action"))))
        {
        	List<Select> categoryList = ScorecardScoreHelper.initSelectList(job.getCompanyId(),
        			PageHandler.getBundle(p_request.getSession()));
        	
        	
        	String userId = (String) p_request.getSession().
        					getAttribute(WebAppConstants.USER_NAME);
        	Long workflowId = Long.parseLong(p_request.getParameter("savedWorkflowId"));
        	
        	Session session = HibernateUtil.getSession();
            Transaction tx = session.beginTransaction();
        	try {
        		for(Workflow workflow: job.getWorkflows())
            	{
        			if(workflow.getId() != workflowId)
        				continue;
        			storeScores(workflowId, session);
            		for(Select select: categoryList)
            		{
            			ScorecardScore score = new ScorecardScore();
            			score.setScorecardCategory(select.getValue());
            			score.setScore(Integer.parseInt(
            					p_request.getParameter(workflow.getId()+"."+select.getValue())));
            			score.setWorkflowId(workflow.getId());
            			score.setJobId(job.getId());
            			score.setCompanyId(job.getCompanyId());
            			score.setModifyUserId(userId);
            			session.saveOrUpdate(score);
            		}
            		WorkflowImpl workflowImpl = (WorkflowImpl)workflow;
            		String comment = p_request.getParameter(workflow.getId()+".scoreComment");
            		if(!comment.equals(workflowImpl.getScorecardComment()))
            		{
            			workflowImpl.setScorecardComment(comment);
            			session.saveOrUpdate(workflowImpl);
            		}
            	}
        		tx.commit();
			} catch (Exception e) {
				tx.rollback();
				e.printStackTrace();
			}
        	
        	scorecardFlag = "scorecard";
        }
        
        if("scorecard".equals(p_request.getParameter("action")) || 
        		"scorecard".equals(scorecardFlag))
        {
        	List<Select> categoryList = ScorecardScoreHelper.initSelectList(job.getCompanyId(),
        			PageHandler.getBundle(p_request.getSession()));
        	
        	HashMap<String, ScorecardScore> scoreMap = new HashMap<String, ScorecardScore>();
        	setScore(job.getJobId(), scoreMap);

        	HashMap<String, String> tmpScoreMap = new HashMap<String, String>();
        	List<ScorecardData> scorecardDataList = new ArrayList<ScorecardData>();
        	HashMap<String, String> avgScoreMap = new HashMap<String, String>();
        	HashMap<String, Integer> avgScoreSum = new HashMap<String, Integer>();
        	HashMap<String, Integer> avgScoreNum = new HashMap<String, Integer>();
        	DecimalFormat numFormat = new DecimalFormat("#.00");
        	List<Workflow> workflows = new ArrayList<Workflow>(job.getWorkflows());
        	Collections.sort(workflows, new WorkflowComparator(
        			WorkflowComparator.TARG_LOCALE_SIMPLE, Locale.getDefault()));
        	for(Workflow workflow: workflows)
        	{
        		if(workflow.getScorecardShowType() == -1)
        			continue;
        		
        		ScorecardData scoreData = new ScorecardData();
        		scoreData.setWorkflowId(workflow.getId());
        		scoreData.setLocaleDisplayname(workflow.getTargetLocale().toString());
        		if(StringUtil.isEmpty(((WorkflowImpl)workflow).getScorecardComment()))
        		{
        			scoreData.setScoreComment("");
        		}
        		else 
        		{
        			scoreData.setScoreComment(((WorkflowImpl)workflow).getScorecardComment());
				}
        		
        		int localeSocreNum = 0;
        		int localeScoreSum = 0;
        		for(Select category: categoryList)
        		{
        			String mapKey = workflow.getId() + "." + category.getValue();
        			if(scoreMap.get(mapKey) == null)
        			{
        				tmpScoreMap.put(mapKey, "--");
        			}
        			else
        			{
        				localeSocreNum++;
        				localeScoreSum = localeScoreSum + scoreMap.get(mapKey).getScore();
        				if(avgScoreSum.get(category.getValue()) == null)
        				{
        					avgScoreSum.put(category.getValue(), scoreMap.get(mapKey).getScore());
        					avgScoreNum.put(category.getValue(), 1);
        				}
        				else
        				{
        					avgScoreSum.put(category.getValue(), 
        							avgScoreSum.get(category.getValue()) 
        							+ scoreMap.get(mapKey).getScore());
        					avgScoreNum.put(category.getValue(), 
        							avgScoreNum.get(category.getValue()) + 1);
        				}
        				tmpScoreMap.put(mapKey, scoreMap.get(mapKey).getScore() + "");
        			}
        		}
        		
        		if(localeSocreNum > 0)
        		{
        			Double avgScore = (double)localeScoreSum/localeSocreNum;
        			scoreData.setAvgScore(numFormat.format(avgScore));
        		}
        		else
        		{
        			scoreData.setAvgScore("--");
        		}
        		scorecardDataList.add(scoreData);
        	}
        	
        	int totalScoreSum = 0;
        	int totalScoreNum = 0;
        	for(Select category: categoryList)
        	{
        		if(avgScoreSum.get(category.getValue()) != null)
        		{
        			Double avgScore = (double)avgScoreSum.get(category.getValue())/
        								avgScoreNum.get(category.getValue());
        			totalScoreSum = totalScoreSum + avgScoreSum.get(category.getValue());
        			totalScoreNum = totalScoreNum + avgScoreNum.get(category.getValue());
        			avgScoreMap.put(category.getValue(), numFormat.format(avgScore));
        		}
        		else
        		{
        			avgScoreMap.put(category.getValue(), "--");
        		}
        	}
        	if(totalScoreSum > 0)
        	{
    			Double avgScore = (double)totalScoreSum/totalScoreNum;
        		avgScoreMap.put("avgScore", numFormat.format(avgScore));
        	}
        	else
        	{
        		avgScoreMap.put("avgScore", "--");
        	}
        	
        	sessionMgr.setAttribute("avgScoreMap", avgScoreMap);
        	sessionMgr.setAttribute("categoryList", categoryList);
        	sessionMgr.setAttribute("scorecardDataList", scorecardDataList);
        	sessionMgr.setAttribute("tmpScoreMap", tmpScoreMap);
		}
		else if ("checkDownloadQAReport".equals(p_request
				.getParameter("action")))
		{
			ServletOutputStream out = p_response.getOutputStream();
			String[] wfIds = p_request.getParameter("wfId").split(" ");
			String jobId = p_request.getParameter("jobId");
			long companyId = job.getCompanyId();
			boolean checkQA = checkQAReport(sessionMgr, companyId, jobId, wfIds);
			String download = "";
			if (checkQA)
			{
				download = "success";
			}
			else
			{
				download = "fail";
			}
			Map<String, Object> returnValue = new HashMap<String, Object>();
			returnValue.put("download", download);
			out.write((JsonUtil.toObjectJson(returnValue)).getBytes("UTF-8"));
			return;
		}
		else if ("downloadQAReport".equals(p_request.getParameter("action")))
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
        else if ("retrieveTranslatedText".equals(p_request
                .getParameter("action")))
        {
            String workflowId = p_request.getParameter("workflowId");
            Workflow workflow = ServerProxy.getWorkflowManager()
                    .getWorkflowById(Long.parseLong(workflowId));
            int percentage = 0;
            JSONObject jsonObject = null;
            Task task = (Task) workflow.getTasks().values().iterator().next();
            percentage = SegmentTuvUtil.getTranslatedPercentageForTask(task);
            jsonObject = new JSONObject();
            jsonObject.put("workflowId", Long.parseLong(workflowId));
            jsonObject.put("percent", percentage);

            p_response.getWriter().write(jsonObject.toJSONString());
            return;
		}
		else if ("updatePriority".equals(p_request.getParameter("action")))
		{
			ServletOutputStream out = p_response.getOutputStream();
			String jobId = p_request.getParameter("jobId");
			String selectOption = p_request.getParameter("selectOption");
			JobImpl jobImpl = HibernateUtil.get(JobImpl.class,
					Long.parseLong(jobId));
			jobImpl.setPriority(Integer.parseInt(selectOption));
			HibernateUtil.merge(job);

			Map<String, Object> returnValue = new HashMap<String, Object>();
			returnValue.put("newPriority", jobImpl.getPriority());
			out.write((JsonUtil.toObjectJson(returnValue)).getBytes("UTF-8"));
			return;
		}
        // deal with ajax request.End.

        boolean isOk = jobSummaryHelper.packJobSummaryInfoView(p_request,
                p_response, p_context, job);
        if (!isOk)
        {
            return;
        }
        parseRequestParameterDistribute(p_request, job);
        packJobWorkflowInfoView(p_request, job);
        packSessionMgrAttr(p_request);
        
        if (Job.CANCELLED.equals(job.getState()))
        {
            jobSummaryHelper.jobNotFound(p_request, p_response, p_context, job);
            return;
        }
        // Update the session with this most recently used job
        jobSummaryHelper.updateMRUJob(p_request, job, p_response);
        super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                p_context);
    }

    public boolean checkQAReport(SessionManager sessionMgr, long companyId,
			String jobId, String[] wfIds)
	{
		Company company = CompanyWrapper.getCompanyById(companyId);
		Set<File> exportFilesSet = new HashSet<File>();
		Set<String> localesSet = new HashSet<String>();
		Set<Long> jobIdSet = new HashSet<Long>();
		jobIdSet.add(Long.parseLong(jobId));
		try
		{
			if (company.getEnableQAChecks())
			{
				for (String wfIdStr : wfIds)
				{
					long wfId = Long.parseLong(wfIdStr);
					Workflow workflow = WorkflowHandlerHelper
							.getWorkflowById(wfId);
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
			CATEGORY.error(e);
		}
		return false;
	}
	
	
	
    private void getUpdateWordCountsPercentage(HttpServletResponse p_response,
            long p_jobId) throws IOException
    {
        ServletOutputStream out = p_response.getOutputStream();
        try
        {
            p_response.setContentType("text/plain");
            out = p_response.getOutputStream();
            StringBuffer sb = new StringBuffer();
            sb.append("{\"updateWCPercentage\":");
            sb.append(updateWordCountsPercentageMap.get(p_jobId)).append("}");
            out.write(sb.toString().getBytes("UTF-8"));
        }
        catch (Exception e)
        {
            CATEGORY.error(e.getMessage(), e);
        }
        finally
        {
            out.close();
        }
    }

    private void parseRequestParameterDistribute(HttpServletRequest p_request,
            Job job)
    {
        HttpSession session = p_request.getSession(false);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        String wfIdParam = p_request.getParameter(JobManagementHandler.WF_ID);
        String wfId = null;
        if (isRefresh(sessionMgr, wfIdParam, JobManagementHandler.WF_ID)
                && isSameAction(sessionMgr, p_request, wfIdParam))
        {
            return;
        }
        if (p_request.getParameter(JobManagementHandler.DISCARD_WF_PARAM) != null)
        {
            sessionMgr.setAttribute(JobManagementHandler.WF_ID, wfIdParam);
            sessionMgr.setAttribute(JobManagementHandler.WF_PREVIOUS_ACTION,
                    JobManagementHandler.DISCARD_WF_PARAM);
            // Discard the selected workflows
            StringTokenizer tokenizer = new StringTokenizer(wfIdParam);
            while (tokenizer.hasMoreTokens())
            {
                wfId = tokenizer.nextToken();
                String userId = ((User) sessionMgr
                        .getAttribute(WebAppConstants.USER)).getUserId();

                WorkflowHandlerHelper.cancelWF(userId, WorkflowHandlerHelper
                        .getWorkflowById(Long.parseLong(wfId)));
            }
            sessionMgr.setAttribute(JobManagementHandler.ADDED_WORKFLOWS, null);
        }
        else if (p_request.getParameter(JobManagementHandler.DISPATCH_WF_PARAM) != null)
        {
            sessionMgr.setAttribute(JobManagementHandler.WF_ID, wfIdParam);
            // Dispatch the selected workflows
            StringTokenizer tokenizer = new StringTokenizer(wfIdParam);
            while (tokenizer.hasMoreTokens())
            {
                wfId = tokenizer.nextToken();
                WorkflowHandlerHelper.dispatchWF(WorkflowHandlerHelper
                        .getWorkflowById(Long.parseLong(wfId)));
            }

            sessionMgr.setAttribute(JobManagementHandler.WF_PREVIOUS_ACTION,
                    JobManagementHandler.DISPATCH_WF_PARAM);
        }
        else if (p_request
                .getParameter(JobManagementHandler.DISPATCH_ALL_WF_PARAM) != null)
        {
            String readyWorkflowIds = p_request
                    .getParameter(JobManagementHandler.ALL_READY_WORKFLOW_IDS);
            if (readyWorkflowIds != null && readyWorkflowIds.length() > 0)
            {
                for (String id : readyWorkflowIds.split(","))
                {
                    WorkflowHandlerHelper.dispatchWF(WorkflowHandlerHelper
                            .getWorkflowById(Long.parseLong(id)));
                }
            }
        }
        else if (p_request
                .getParameter(JobManagementHandler.UPDATE_WORD_COUNTS) != null)
        {
            if (wfIdParam != null)
            {
                updateWordCounts(wfIdParam);
                p_request.setAttribute("isUpdatingWordCounts", "true");
                return;
            }
        }
        else if (p_request.getParameter(JobManagementHandler.ARCHIVE_WF_PARAM) != null)
        {
            sessionMgr.setAttribute(JobManagementHandler.WF_ID, wfIdParam);
            // Archive the selected workflows
            StringTokenizer tokenizer = new StringTokenizer(wfIdParam);
            while (tokenizer.hasMoreTokens())
            {
                wfId = tokenizer.nextToken();
                Workflow wf = WorkflowHandlerHelper.getWorkflowById(Long
                        .parseLong(wfId));
                WorkflowHandlerHelper.archiveWorkflow(wf);
            }

            sessionMgr.setAttribute(JobManagementHandler.WF_PREVIOUS_ACTION,
                    JobManagementHandler.ARCHIVE_WF_PARAM);
        }
        else if (p_request.getParameter(JobManagementHandler.ASSIGN_PARAM) != null)
        {
            if ("saveAssign".equalsIgnoreCase(p_request
                    .getParameter(JobManagementHandler.ASSIGN_PARAM)))
            {
                doSaveAssign(p_request, sessionMgr);
            }

            sessionMgr.setAttribute(JobManagementHandler.WF_ID, null);
        }
        else if (p_request.getParameter(JobManagementHandler.SKIP_PARAM) != null)
        {
            if (FormUtil.isNotDuplicateSubmisson(p_request,
                    FormUtil.Forms.SKIP_ACTIVITIES))
            {
                doSkip(p_request, sessionMgr);
            }
        }
        if (p_request.getParameter(JobManagementHandler.ADD_WF_PARAM) != null)
        {
            // Get a comma separated string of WorkflowTemplatInfo ids
            String buf = p_request
                    .getParameter(JobManagementHandler.ADD_WF_PARAM);
            if (isRefresh(sessionMgr, buf, JobManagementHandler.ADDED_WORKFLOWS))
            {
                return;
            }
            long jobId = job.getJobId();
            // first validate the state of the existing pages of the job
            WorkflowHandlerHelper.validateStateOfPagesByJobId(jobId);

            sessionMgr.setAttribute(JobManagementHandler.ADDED_WORKFLOWS, buf);
            // Convert to List
            String[] wfInfosArray = buf.split(",");
            ArrayList<Long> wfInfos = new ArrayList<Long>();
            for (int i = 0; i < wfInfosArray.length; i++)
            {
                wfInfos.add(Long.decode(wfInfosArray[i]));
            }
            try
            {
                WorkflowAdditionSender sender = new WorkflowAdditionSender(
                        wfInfos, jobId);
                sender.sendToAddWorkflows();
            }
            catch (Exception e)
            {
                throw new EnvoyServletException(e);
            }
            List<SourcePage> sps = new ArrayList<SourcePage>();
            try
            {
                sps.addAll(ServerProxy.getJobHandler().getJobById(jobId)
                        .getSourcePages());
            }
            catch (Exception e)
            {
            	CATEGORY.error(e);
            }

            List<String> targetLocales = new ArrayList<String>();
            for (Long workflowId : wfInfos)
            {
                try
                {
                    targetLocales.add(ServerProxy.getProjectHandler()
                            .getWorkflowTemplateInfoById(workflowId)
                            .getTargetLocale().toString());
                }
                catch (Exception e)
                {
                	CATEGORY.error(e);
                }
            }

            copyFilesToTargetDir(sps, targetLocales);
        }
        else
        {
            // Don't do anything if they are just viewing the table
            // and not performing an action on a workflow
            return;
        }
    }

    private void updateWordCounts(final String p_workflowIds)
    {
        Runnable runnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    List<Long> wfIds = new ArrayList<Long>();
                    wfIds = UpdateLeverageHelper.getWfIds(p_workflowIds);

                    long jobId = -1;
                    int wfNumber = wfIds.size();
                    if (wfNumber > 0)
                    {
                        Workflow wf = ServerProxy.getWorkflowManager()
                                .getWorkflowById(wfIds.get(0));
                        jobId = wf.getJob().getId();
                        // Initialize this job's percentage to 0.
                        updateWordCountsPercentageMap.put(jobId, 0);
                    }

                    int count = 0;
                    for (Iterator it = wfIds.iterator(); it.hasNext();)
                    {
                        Workflow wf = ServerProxy.getWorkflowManager()
                                .getWorkflowById((Long) it.next());

                        TranslationMemoryProfile tmProfile = wf.getJob()
                                .getL10nProfile().getTranslationMemoryProfile();
                        Vector<String> jobExcludeTuTypes = tmProfile
                                .getJobExcludeTuTypes();

                        StatisticsService.calculateTargetPagesWordCount(wf,
                                jobExcludeTuTypes);

                        List<Workflow> wfList = new ArrayList<Workflow>();
                        wfList.add(wf);
                        StatisticsService.calculateWorkflowStatistics(wfList,
                                jobExcludeTuTypes);

                        count++;
                        updateWordCountsPercentageMap.put(jobId,
                                Math.round(count * 100 / wfNumber));
                    }
                    // Add this to ensure the progressBar will go to end 100.
                    updateWordCountsPercentageMap.put(jobId, 100);
                }
                catch (Exception e)
                {
                    throw new EnvoyServletException(e);
                }
                finally
                {
                    SegmentTuTuvCacheManager.clearCache();
                }
            }
        };

        Thread t = new MultiCompanySupportedThread(runnable);
        t.setName("Update WordCounts for workflows: " + p_workflowIds);
        t.start();
    }

    private boolean isSameAction(SessionManager p_sessionMgr,
            HttpServletRequest p_request, String wfIdParam)
    {
        String preAction = (String) p_sessionMgr
                .getAttribute(JobManagementHandler.WF_PREVIOUS_ACTION);

        if (p_request.getParameter(JobManagementHandler.DISCARD_WF_PARAM) != null)
        {
            if (JobManagementHandler.DISCARD_WF_PARAM.equals(preAction))
            {
                return true;
            }
            else
            {
                return false;
            }
        }

        return true;
    }

    private void packSessionMgrAttr(HttpServletRequest p_request)
    {
        HttpSession session = p_request.getSession(false);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        // fix the null pointer exception when going back to job detail page
        // after some operation.
        sessionMgr.setAttribute("destinationPage",
                JobManagementHandler.DETAILS_BEAN);

        sessionMgr.setAttribute(JobManagementHandler.EXPORT_INIT_PARAM,
                JobManagementHandler.DETAILS_BEAN);
    }

    private String getLeftTime(HttpServletRequest p_request, Job job)
    {
        HttpSession session = p_request.getSession(false);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        User user = (User) sessionMgr.getAttribute(USER);
        // Set the download delay time for this company
        sessionMgr.setAttribute(SystemConfigParamNames.DOWNLOAD_JOB_DELAY_TIME,
                s_downloadDelayTimeAfterExporting);
        Hashtable delayTimeTable = (Hashtable) sessionMgr
                .getAttribute(WebAppConstants.DOWLOAD_DELAY_TIME_TABLE);
        String delayTimeTableKey = user.getUserName() + job.getJobId()
                + p_request.getParameter("wfId");
        long startTime;
        if (delayTimeTable != null)
        {
            Date date = (Date) delayTimeTable.get(delayTimeTableKey);
            if (date != null)
            {
                startTime = date.getTime();
            }
            else
            {
                startTime = 0l;
            }
        }
        else
        {
            startTime = 0l;
        }
        long currentTime = new Date().getTime();
        double usedTime = (currentTime - startTime) / 1000;
        String delayTimeStr = (String) sessionMgr
                .getAttribute(SystemConfigParamNames.DOWNLOAD_JOB_DELAY_TIME);
        double delayTime = Double.valueOf(delayTimeStr);
        double leftTime = delayTime - usedTime;
        return String.valueOf(delayTime) + "," + String.valueOf(leftTime);
    }

    private void packJobWorkflowInfoView(HttpServletRequest p_request, Job job)
    {
        List<JobWorkflowDisplay> jobWorkflowDisplayList = getJobWorkflowDisplayList(
                p_request, job);
        p_request
                .setAttribute("JobWorkflowDisplayList", jobWorkflowDisplayList);
        // for AddWF button submit condition
        p_request.setAttribute("jobHasPassoloFiles", job.hasPassoloFiles());
        // for Dispatch button
        p_request.setAttribute("jobHasSetCostCenter", job.hasSetCostCenter());
        p_request.setAttribute("isSuperAdmin", isSuperAdmin(p_request));
        p_request.setAttribute("isCustomerAccessGroupInstalled",
                Modules.isCustomerAccessGroupInstalled());
        p_request.setAttribute("isVendorManagementInstalled",
                Modules.isVendorManagementInstalled());
        p_request.setAttribute("reimportOption", getReimportOption());
        // control Estimated Review Start column access permission
        p_request
                .setAttribute("customerAccessGroupIsDell", s_isSpecialCustomer);
        // tell UI if current job has been archived.
        p_request.setAttribute("isJobMigrated", job.isMigrated() ? "true"
                : "false");
        p_request.setAttribute("project", job.getProject());
		try
		{
			Company company = ServerProxy.getJobHandler().getCompanyById(
					job.getCompanyId());
			p_request.setAttribute("company", company);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
    }

    private boolean isSuperAdmin(HttpServletRequest p_request)
    {
        HttpSession session = p_request.getSession(false);
        String userId = (String) session
                .getAttribute(WebAppConstants.USER_NAME);
        return UserUtil.isSuperAdmin(userId);
    }

    private int getReimportOption()
    {
        int reimportOption = 0;
        try
        {
            reimportOption = Integer.parseInt(ServerProxy
                    .getSystemParameterPersistenceManager()
                    .getSystemParameter(SystemConfigParamNames.REIMPORT_OPTION)
                    .getValue());
        }
        catch (Exception ge)
        {
            // assumes disabled.
        }
        return reimportOption;
    }

    private List<JobWorkflowDisplay> getJobWorkflowDisplayList(
            HttpServletRequest p_request, Job job)
    {
        HttpSession session = p_request.getSession(false);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        ResourceBundle bundle = getBundle(session);
        PermissionSet perms = (PermissionSet) session
                .getAttribute(WebAppConstants.PERMISSIONS);
        User user = (User) sessionMgr.getAttribute(USER);
        Locale uiLocale = (Locale) session.getAttribute(UILOCALE);
        TimeZone timezone = (TimeZone) session.getAttribute(USER_TIME_ZONE);
        Timestamp ts = new Timestamp(Timestamp.DATE, timezone);
        ts.setLocale(uiLocale);
        List<Workflow> workflows = new ArrayList<Workflow>(job.getWorkflows());
        Collections
                .sort(workflows, new WorkflowComparator(Locale.getDefault()));
        List<JobWorkflowDisplay> jobWorkflowDisplayList = new ArrayList<JobWorkflowDisplay>();
        for (Workflow workflow : workflows)
        {
            if (workflow.getState().equalsIgnoreCase(Workflow.CANCELLED))
                continue;
            if (!perms.getPermissionFor(Permission.JOB_SCOPE_ALL)
                    && !perms.getPermissionFor(Permission.JOB_SCOPE_MYPROJECTS)
                    && (job.getProject().getProjectManagerId() != user
                            .getUserId())
                    && PageHandler.invalidForWorkflowOwner(user.getUserId(),
                            perms, workflow))
                continue;

            JobWorkflowDisplay jobWorkflowDisplay = new JobWorkflowDisplay(
                    workflow);
            jobWorkflowDisplay.setIsUploading("No");
            Hashtable<Long, Task> tasks = workflow.getTasks();
            for(Long taskKey:  tasks.keySet())
            {
            	if(tasks.get(taskKey).getIsUploading() == 'Y')
            	{
            		jobWorkflowDisplay.setIsUploading("Yes");
            		break;
            	}
            }
            jobWorkflowDisplay.setTargetLocaleDisplayName(workflow
                    .getTargetLocale().getDisplayName(uiLocale));
            jobWorkflowDisplay.setTotalWordCount(getTotalWordCount(workflow));
            jobWorkflowDisplay.setStateBundleString(bundle.getString(workflow
                    .getState()));
            jobWorkflowDisplay.setIsWorkflowEditable(isWorkflowEditable(perms,
                    workflow));
            setJobWorkflowDisplayTaskDisplayName(workflow, jobWorkflowDisplay);
            setJobWorkflowDisplayEstimaedTimestamp(perms, uiLocale, ts,
                    workflow, jobWorkflowDisplay);

            jobWorkflowDisplayList.add(jobWorkflowDisplay);
        }

        return jobWorkflowDisplayList;
    }

    private void setJobWorkflowDisplayEstimaedTimestamp(PermissionSet perms,
            Locale uiLocale, Timestamp ts, Workflow workflow,
            JobWorkflowDisplay jobWorkflowDisplay)
    {
        String str;
        if (s_isSpecialCustomer
                && perms.getPermissionFor(Permission.JOB_WORKFLOWS_ESTREVIEWSTART))
        {
            Date estimatedStart = getFirstReviewStartDate(workflow.getId(),
                    workflow.getTasks(), workflow.getDispatchedDate());
            if (estimatedStart != null)
            {
                ts.setDate(estimatedStart);
                jobWorkflowDisplay.setEstimatedStartTimestamp(ts.toString());
            }
            else
            {
                jobWorkflowDisplay.setEstimatedStartTimestamp("--");
            }
        }

        if (workflow.getEstimatedTranslateCompletionDate() != null)
        {
            ts.setDate(workflow.getEstimatedTranslateCompletionDate());
            str = ts.toString() + " " + ts.getHour() + ":";
            if (ts.getMinute() < 10)
            {
                str += "0";
            }
            str += ts.getMinute() + " "
                    + ts.getTimeZone().getDisplayName(uiLocale);
            jobWorkflowDisplay
                    .setEstimatedTranslateCompletionDateTimestamp(str);
        }
        else
        {
            jobWorkflowDisplay
                    .setEstimatedTranslateCompletionDateTimestamp("--");
        }

        if (workflow.getEstimatedCompletionDate() != null)
        {
            ts.setDate(workflow.getEstimatedCompletionDate());
            str = ts.toString() + " " + ts.getHour() + ":";
            if (ts.getMinute() < 10)
            {
                str += "0";
            }
            str += ts.getMinute() + " "
                    + ts.getTimeZone().getDisplayName(uiLocale);
            jobWorkflowDisplay.setEstimatedCompletionDateTimestamp(str);
        }
        else
        {
            jobWorkflowDisplay.setEstimatedCompletionDateTimestamp("--");
        }
    }

    private int getTotalWordCount(Workflow workflow)
    {
        int wordCount = JobManagementHandler.getTotalWordCount(workflow,
                JobManagementHandler.TOTAL_WF_WORD_CNT);
        return wordCount;
    }

    private void setJobWorkflowDisplayTaskDisplayName(Workflow workflow,
            JobWorkflowDisplay jobWorkflowDisplay)
    {
        if (!Workflow.SKIPPING.equals(workflow.getState()))
        {
            TaskInstance task = WorkflowManagerLocal.getCurrentTask(workflow
                    .getId());
            if (task != null)
            {
                String str = TaskJbpmUtil.getTaskDisplayName(task.getName());
                jobWorkflowDisplay.setTaskDisplayName(str);
            }
        }
        else
        {
            jobWorkflowDisplay.setTaskDisplayName(" ");
        }
    }

    /*
     * Need to display the start date of the first review-only activity in a
     * workflow's default path (ONLY when a system-wide flag is on).
     */
    private Date getFirstReviewStartDate(long p_workflowId, Hashtable p_tasks,
            Date p_startDate) throws EnvoyServletException
    {
        try
        {
            Date result = null;

            // get all the task ids for the path in the workflow
            long[] taskIds = ServerProxy.getWorkflowServer()
                    .taskIdsInDefaultPath(p_workflowId);

            for (int i = 0; i < taskIds.length; i++)
            {
                Task t = (Task) p_tasks.get(new Long(taskIds[i]));

                if (t.isType(Task.TYPE_REVIEW)
                        || t.isType(Task.TYPE_REVIEW_EDITABLE))
                {
                    result = p_startDate;
                    break;
                }

                p_startDate = t.getEstimatedCompletionDate();
            }

            return result;
        }
        catch (Exception ex)
        {
            throw new EnvoyServletException(ex);
        }
    }

    /**
     * Determine whether the workflow is editable or not. Note that only a
     * workflow owner can modify a workflow. In our case, a Project Manager and
     * a Workflow Manager can modify a workflow that they are assiciated with.
     */
    private boolean isWorkflowEditable(PermissionSet p_perms, Workflow p_wf)
    {
        boolean canEditWorkflow = p_perms
                .getPermissionFor(Permission.PROJECTS_MANAGE)
                || p_perms
                        .getPermissionFor(Permission.PROJECTS_MANAGE_WORKFLOWS);
        return WorkflowHandlerHelper.isWorkflowModifiable(p_wf.getState())
                && canEditWorkflow;
    }

    /**
     * Get values from request and session. Assign the selected user to the
     * task.
     */
    private void doSaveAssign(HttpServletRequest p_request,
            SessionManager p_sessionMgr) throws EnvoyServletException
    {
        try
        {
            String srcLocale = (String) p_sessionMgr.getAttribute("srcLocale");
            String targLocale = (String) p_sessionMgr
                    .getAttribute("targLocale");
            Hashtable taskUserHash = (Hashtable) p_sessionMgr
                    .getAttribute("taskUserHash");
            String wfId = (String) p_sessionMgr.getAttribute("wfId");
            // If "F5" to refresh, wfId is null.
            if (wfId == null)
                return;

            Enumeration keys = taskUserHash.keys();
            HashMap roleMap = new HashMap();
            while (keys.hasMoreElements())
            {
                Task task = (Task) keys.nextElement();
                String taskId = String.valueOf(task.getId());
                Activity activity = ServerProxy.getJobHandler()
                        .getActivityByCompanyId(task.getTaskName(),
                                String.valueOf(task.getCompanyId()));
                ContainerRole containerRole = ServerProxy.getUserManager()
                        .getContainerRole(activity, srcLocale, targLocale);
                String userParam = p_request.getParameter("users" + taskId);
                /*
                 * userParam =
                 * userId1,user1Name:userId2,user2Name:userId3,user3Name: when
                 * the above parameters are split on ":", it gives users
                 * users[0] = userId1, user1Name users[1] = userId1, user1Name
                 * users[2] = userId1, user1Name It's required to split users on
                 * "," to get userInfo. So userInfos[0] = "userId1" and
                 * userInfos[1]=user1Name userInfos[0] = "userId2" and
                 * userInfos[1]=user2Name userInfos[0] = "userId3" and
                 * userInfos[1]=user3Name
                 */
                if(userParam != null && userParam != ""){
                	String[] users = userParam.split(":");
                	String[] userInfos = null;
                	Vector newAssignees = new Vector();
                	String[] roles = new String[users.length];
                	String displayRole = "";
                	for (int k = 0; k < users.length; k++)
                	{
                		userInfos = users[k].split(",");
                		roles[k] = containerRole.getName() + " " + userInfos[0];
                		if (k == users.length - 1)
                		{
                			displayRole += userInfos[1];
                		}
                		else
                		{
                			displayRole += userInfos[1] + ",";
                		}
                	}
                	newAssignees.addElement(new NewAssignee(roles, displayRole,
                			true));
                	roleMap.put(taskId, newAssignees);
                }
            }

            boolean shouldModifyWf = false;
            Long id = Long.valueOf(wfId);
            WorkflowInstance wi = ServerProxy.getWorkflowServer()
                    .getWorkflowInstanceById(id.longValue());

            Vector tasks = wi.getWorkflowInstanceTasks();

            int sz = tasks == null ? -1 : tasks.size();
            for (int j = 0; j < sz; j++)
            {
                WorkflowTaskInstance wti = (WorkflowTaskInstance) tasks.get(j);
                Vector newAssignees = (Vector) roleMap.get(String.valueOf(wti
                        .getTaskId()));

                if (newAssignees != null)
                {
                    for (int r = 0; r < newAssignees.size(); r++)
                    {
                        NewAssignee na = (NewAssignee) newAssignees.elementAt(r);
                        if (shouldModifyWf(wti, na))
                        {
                            shouldModifyWf = true;
                            wti.setRoleType(na.m_isUserRole);
                            wti.setRoles(na.m_roles);
                            wti.setDisplayRoleName(na.m_displayRoleName);
                        }
                    }
                }

            }

            // modify one workflow at a time and reset the flag
            if (shouldModifyWf)
            {
                shouldModifyWf = false;
                ServerProxy.getWorkflowManager().modifyWorkflow(null, wi, null,
                        null);
            }
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    private boolean shouldModifyWf(WorkflowTaskInstance wti, NewAssignee na)
    {
        boolean result = false;
        if (na != null && !areSameRoles(wti.getRoles(), na.m_roles))
        {
            result = true;

            // If current task is in progress, and its accepter equals specified
            // role, this can be ignored.
            String acceptUser = wti.getAcceptUser();
            String[] roles = na.m_roles;
            if (na.m_isUserRole && roles != null && roles.length == 1
                    && acceptUser != null && roles[0].endsWith(acceptUser))
            {
                result = false;
            }
        }

        return result;
    }

    // ////////////////////////////////////////////////////////////////////
    // Begin: Inner Class
    // ////////////////////////////////////////////////////////////////////
    class NewAssignee
    {
        String m_displayRoleName = null;

        String[] m_roles = null;

        boolean m_isUserRole = false;

        NewAssignee(String[] p_roles, String p_displayRoleName,
                boolean p_isUserRole)
        {
            m_displayRoleName = p_displayRoleName;
            m_roles = p_roles;
            m_isUserRole = p_isUserRole;
        }
    }

    /**
     * Determines whether the two array of roles contain the same set of role
     * names.
     */
    private boolean areSameRoles(String[] p_workflowRoles,
            String[] p_selectedRoles)
    {
        // First need to sort since Arrays.equals() requires
        // the parameters to be sorted
        Arrays.sort(p_workflowRoles);
        Arrays.sort(p_selectedRoles);
        return Arrays.equals(p_workflowRoles, p_selectedRoles);
    }

    /**
     * Does the skip job for the activities.
     * 
     * @param p_request
     * @param p_sessionMgr
     * @throws EnvoyServletException
     */
    private void doSkip(HttpServletRequest p_request,
            SessionManager p_sessionMgr) throws EnvoyServletException
    {

        final User user = (User) p_sessionMgr
                .getAttribute(WebAppConstants.USER);
        final List<Entry> list = getSkipParameter(p_request);

        if (list == null)
        {
            return;
        }
        boolean isDuplicateAction = updateWorkflowState(list);
        if (isDuplicateAction)
        {
            return;
        }
        Runnable runnable = new Runnable()
        {
            @Override
            public void run()
            {
                // run the skip process in a new thread in order to return to
                // job detail page quickly without waiting for the skip process
                // to be completed
                try
                {
                    ServerProxy.getWorkflowManager().setSkip(list,
                            user.getUserId());
                }
                catch (Exception e)
                {
                    CATEGORY.error("Skip activity error", e);
                    throw new EnvoyServletException(e);
                }
            }
        };
        Thread t = new MultiCompanySupportedThread(runnable);
        t.start();
    }

    /**
     * Updates the workflow state to "SKIPPING".
     */
    private boolean updateWorkflowState(List<Entry> list)
    {
        for (Entry<String, String> entry : list)
        {
            String workflowId = entry.getKey();
            Workflow workflow = (Workflow) HibernateUtil.get(
                    WorkflowImpl.class, Long.valueOf(workflowId));
            String state = workflow.getState();
            if (Workflow.SKIPPING.equals(state))
            {
                // the workflow has been doing skipping, so return this
                // duplicate action
                CATEGORY.info("Ignored duplicate skip action as workflow "
                        + workflowId + " is already being skipped.");
                return true;
            }
            JobCreationMonitor.updateWorkflowState(workflow, Workflow.SKIPPING);
        }
        return false;
    }

    private List<Entry> getSkipParameter(HttpServletRequest p_request)
    {
        String[] workflowIds = p_request.getParameterValues("workflowId");

        if (workflowIds == null)
        {
            return null;
        }

        List<Entry> list = new ArrayList<Entry>(workflowIds.length);

        String activity;
        Entry<String, String> entry;
        for (String workflowId : workflowIds)
        {
            activity = p_request.getParameter("activity" + workflowId);
            entry = new Entry<String, String>(workflowId, activity);
            entry.setHelp(p_request.getParameter("activity_" + workflowId));
            list.add(entry);
        }
        return list;
    }

    /**
     * Copy files from source converter folder to target converter folder
     * 
     * Fix for GBS-1815
     * 
     * @param sourcePages
     * @param targetLocales
     */
    private void copyFilesToTargetDir(List<SourcePage> sourcePages,
            List<String> targetLocales)
    {
        SystemConfiguration sc = SystemConfiguration.getInstance();
        String fileStorageDir = sc.getStringParameter(
                SystemConfigParamNames.FILE_STORAGE_DIR,
                CompanyWrapper.SUPER_COMPANY_ID);
        List<String> copiedFiles = new ArrayList<String>();
        for (SourcePage sp : sourcePages)
        {
            String eventFlowXml = sp.getRequest().getEventFlowXml();
            Element rootElement = XmlUtils.findRootElement(eventFlowXml);

            long fileProfileId = sp.getRequest().getFileProfileId();
            String formatType = getFormatType(fileProfileId);

            String sourceLocale = sp.getGlobalSightLocale().toString();

            List<String> copyFilesName = null;
            String baseConv = null;

            if ("OpenOffice document".equals(formatType))
            {
                // Open office
                copyFilesName = getAllCopiedFilesForOpenOffice(rootElement);
                baseConv = fileStorageDir + File.separator + "OpenOffice-Conv";
            }
            else if ("Office2010 document".equals(formatType) 
            		|| formatType.contains("Office 2010"))
            {
                // Office 2010
                copyFilesName = getAllCopiedFilesForOffice2010(rootElement);
                baseConv = fileStorageDir + File.separator + "OfficeXml-Conv";
            }
            else if ("Word2007".equals(formatType))
            {
                // Word2007
                copyFilesName = getAllCopiedFilesForMSOffice20032007(rootElement);
                String office2007Conv = sc.getStringParameter(
                        SystemConfigParamNames.MSOFFICE_CONV_DIR,
                        CompanyWrapper.SUPER_COMPANY_ID);
                baseConv = office2007Conv + File.separator + "word";
            }
            else if ("Excel2007".equals(formatType))
            {
                // Excel2007
                copyFilesName = getAllCopiedFilesForMSOffice20032007(rootElement);
                String office2007Conv = sc.getStringParameter(
                        SystemConfigParamNames.MSOFFICE_CONV_DIR,
                        CompanyWrapper.SUPER_COMPANY_ID);
                baseConv = office2007Conv + File.separator + "excel";
            }
            else if ("PowerPoint2007".equals(formatType))
            {
                // PowerPoint2007
                copyFilesName = getAllCopiedFilesForMSOffice20032007(rootElement);
                String office2007Conv = sc.getStringParameter(
                        SystemConfigParamNames.MSOFFICE_CONV_DIR,
                        CompanyWrapper.SUPER_COMPANY_ID);
                baseConv = office2007Conv + File.separator + "powerpoint";
            }
            else if ("Word2003".equals(formatType))
            {
                // Word2003
                copyFilesName = getAllCopiedFilesForMSOffice20032007(rootElement);
                String office2003Conv = sc.getStringParameter(
                        SystemConfigParamNames.MSOFFICE2003_CONV_DIR,
                        CompanyWrapper.SUPER_COMPANY_ID);
                baseConv = office2003Conv + File.separator + "word";
            }
            else if ("Excel2003".equals(formatType))
            {
                // Excel2003
                copyFilesName = getAllCopiedFilesForMSOffice20032007(rootElement);
                String office2003Conv = sc.getStringParameter(
                        SystemConfigParamNames.MSOFFICE2003_CONV_DIR,
                        CompanyWrapper.SUPER_COMPANY_ID);
                baseConv = office2003Conv + File.separator + "excel";
            }
            else if ("PowerPoint2003".equals(formatType))
            {
                // PowerPoint2003
                copyFilesName = getAllCopiedFilesForMSOffice20032007(rootElement);
                String office2003Conv = sc.getStringParameter(
                        SystemConfigParamNames.MSOFFICE2003_CONV_DIR,
                        CompanyWrapper.SUPER_COMPANY_ID);
                baseConv = office2003Conv + File.separator + "powerpoint";
            }
            else if ("InDesign Markup (IDML)".equals(formatType))
            {
                // IDML
                String safeBaseFileName = getSafeBaseFileName(rootElement,
                        "IdmlAdapter");
                String filesDir = safeBaseFileName + ".unzip";
                copyFilesName = new ArrayList<String>();
                copyFilesName.add(filesDir);

                baseConv = fileStorageDir + File.separator + "Idml-Conv";
            }
            else if ("INDD (CS5)".equals(formatType))
            {
                // INDD (CS5)
                copyFilesName = getAllCopiedFilesForIndd(rootElement);
                baseConv = sc.getStringParameter(
                        SystemConfigParamNames.ADOBE_CONV_DIR_CS5,
                        CompanyWrapper.SUPER_COMPANY_ID)
                        + File.separator + "indd";
            }
            else if ("INDD (CS4)".equals(formatType))
            {
                // INDD (CS4)
                copyFilesName = getAllCopiedFilesForIndd(rootElement);
                baseConv = sc.getStringParameter(
                        SystemConfigParamNames.ADOBE_CONV_DIR_CS4,
                        CompanyWrapper.SUPER_COMPANY_ID)
                        + File.separator + "indd";

            }
            else if ("INDD (CS3)".equals(formatType))
            {
                // INDD (CS3)
                copyFilesName = getAllCopiedFilesForIndd(rootElement);
                baseConv = sc.getStringParameter(
                        SystemConfigParamNames.ADOBE_CONV_DIR_CS3,
                        CompanyWrapper.SUPER_COMPANY_ID)
                        + File.separator + "indd";
            }
            else if ("INDD (CS5.5)".equals(formatType))
            {
                // INDD (CS5.5)
                copyFilesName = getAllCopiedFilesForIndd(rootElement);
                baseConv = sc.getStringParameter(
                        SystemConfigParamNames.ADOBE_CONV_DIR_CS5_5,
                        CompanyWrapper.SUPER_COMPANY_ID)
                        + File.separator + "indd";
            }
            else if ("INX (CS3)".equals(formatType))
            {
                // INX(CS3)
                copyFilesName = getAllCopiedFilesForIndd(rootElement);
                baseConv = sc.getStringParameter(
                        SystemConfigParamNames.ADOBE_CONV_DIR_CS3,
                        CompanyWrapper.SUPER_COMPANY_ID)
                        + File.separator + "inx";
            }
            else if ("Windows Portable Executable".equals(formatType))
            {
                String companyName = CompanyWrapper.getCompanyNameById(sp
                        .getCompanyId());

                copyFilesName = getAllCopiedFilesForWinPE(rootElement);
                baseConv = sc.getStringParameter(
                        SystemConfigParamNames.WINDOWS_PE_DIR,
                        CompanyWrapper.SUPER_COMPANY_ID)
                        + File.separator
                        + "winpe"
                        + File.separator
                        + companyName;
            }

            // Copy files to target folder
            if (baseConv != null)
            {
                for (String fileName : copyFilesName)
                {
                    String filePath = baseConv + File.separator + sourceLocale
                            + File.separator + fileName;
                    if (copiedFiles.contains(filePath))
                    {
                        continue;
                    }
                    else
                    {
                        copiedFiles.add(filePath);
                    }
                    File file = new File(filePath);
                    for (String targetLocale : targetLocales)
                    {
                        if (file.exists())
                        {
                            File target = new File(baseConv + File.separator
                                    + targetLocale + File.separator + fileName);
                            try
                            {
                                if (file.isDirectory())
                                {
                                    FileUtil.copyFolder(file, target);
                                }
                                else
                                {
                                    FileUtil.copyFile(file, target);
                                }
                            }
                            catch (IOException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Get format type
     * 
     * @param fileProfileId
     * @return
     */
    private String getFormatType(long fileProfileId)
    {
        FileProfilePersistenceManager fpPM;
        FileProfile fp;
        String formatType = null;
        try
        {
            fpPM = ServerProxy.getFileProfilePersistenceManager();
            fp = fpPM.getFileProfileById(fileProfileId, false);
            formatType = fpPM.getKnownFormatTypeById(fp.getKnownFormatTypeId(),
                    false).getName();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return formatType;
    }

    /**
     * Get safeBaseFileName to get base directory name
     * 
     * @param eventFlow
     * @param adapter
     * @return
     */
    private String getSafeBaseFileName(Element rootElement, String adapter)
    {
        String safeBaseFileName = "";
        List categoryElements = XmlUtils.getChildElements(rootElement,
                "category");
        for (Iterator itor = categoryElements.iterator(); itor.hasNext();)
        {
            Element categoryElement = (Element) itor.next();
            if (adapter.equals(categoryElement.getAttribute("name")))
            {
                List daElements = XmlUtils.getChildElements(categoryElement,
                        "da");
                for (Iterator it = daElements.iterator(); it.hasNext();)
                {
                    Element daElement = (Element) it.next();
                    String name = daElement.getAttribute("name");
                    if ("safeBaseFileName".equals(name))
                    {
                        safeBaseFileName = XmlUtils.getChildElementValue(
                                daElement, "dv");
                        break;
                    }
                }
            }
        }
        return safeBaseFileName;
    }

    /**
     * Get all needed files to copy
     * 
     * @param rootElement
     * @return
     */
    private List<String> getAllCopiedFilesForMSOffice20032007(
            Element rootElement)
    {
        List<String> list = new ArrayList<String>();
        String safeBaseFileName = getSafeBaseFileName(rootElement,
                "MicrosoftApplicationAdapter");
        String name = safeBaseFileName.substring(0,
                safeBaseFileName.lastIndexOf("."));
        String htmlFile = name + ".html";
        String filesDir = name + "_files";
        String filesDir1 = name + ".files";
        list.add(htmlFile);
        list.add(filesDir);
        list.add(filesDir1);
        return list;
    }

    /**
     * Get all needed files to copy
     * 
     * @param rootElement
     * @return
     */
    private List<String> getAllCopiedFilesForOpenOffice(Element rootElement)
    {
        List<String> list = new ArrayList<String>();
        String safeBaseFileName = getSafeBaseFileName(rootElement,
                "OpenOfficeAdapter");
        String fileDir;
        if (safeBaseFileName.endsWith("odp"))
        {
            fileDir = safeBaseFileName + "." + OpenOfficeHelper.OPENOFFICE_ODP;
        }
        else if (safeBaseFileName.endsWith("ods"))
        {
            fileDir = safeBaseFileName + "." + OpenOfficeHelper.OPENOFFICE_ODS;
        }
        else
        {
            fileDir = safeBaseFileName + "." + OpenOfficeHelper.OPENOFFICE_ODT;
        }
        list.add(safeBaseFileName);
        list.add(fileDir);
        return list;
    }

    /**
     * Get all needed files to copy
     * 
     * @param rootElement
     * @return
     */
    private List<String> getAllCopiedFilesForIndd(Element rootElement)
    {
        List<String> list = new ArrayList<String>();
        String safeBaseFileName = getSafeBaseFileName(rootElement,
                "AdobeAdapter");
        String fileName = safeBaseFileName.substring(0,
                safeBaseFileName.lastIndexOf("."));
        list.add(safeBaseFileName);
        list.add(fileName + ".pdf");
        list.add(fileName + ".xmp");
        return list;
    }

    /**
     * Get all needed files to copy
     * 
     * @param rootElement
     * @return
     */
    private List<String> getAllCopiedFilesForOffice2010(Element rootElement)
    {
        List<String> list = new ArrayList<String>();
        String safeBaseFileName = getSafeBaseFileName(rootElement,
                "OfficeXmlAdapter");

        String fileDir;
        if (safeBaseFileName.endsWith("docx"))
        {
            fileDir = safeBaseFileName + "." + OfficeXmlHelper.OFFICE_DOCX;
        }
        else if (safeBaseFileName.endsWith("pptx"))
        {
            fileDir = safeBaseFileName + "." + OfficeXmlHelper.OFFICE_PPTX;
        }
        else
        {
            fileDir = safeBaseFileName + "." + OfficeXmlHelper.OFFICE_XLSX;
        }
        list.add(safeBaseFileName);
        list.add(fileDir);
        return list;
    }

    /**
     * Get all needed files to copy
     * 
     * @param rootElement
     * @return
     */
    private List<String> getAllCopiedFilesForWinPE(Element rootElement)
    {
        List<String> list = new ArrayList<String>();
        String safeBaseFileName = getSafeBaseFileName(rootElement,
                "WindowsPEAdapter");

        list.add(safeBaseFileName);
        return list;
    }
    
    private HashMap<String, ScorecardScore> setScore(long jobId, HashMap<String, ScorecardScore> scoreMap)
    {
    	List<ScorecardScore> scoreList = ScorecardScoreHelper.getScoreByJobId(jobId);
    	for(ScorecardScore score:scoreList)
    	{
    		scoreMap.put(score.getWorkflowId() + "." + score.getScorecardCategory(), 
    				score);
    	}
    	return scoreMap;
    }
    
    private void storeScores(long workflowId, Session session)
    {
    	List<ScorecardScore> scoreList = ScorecardScoreHelper.getScoreByWrkflowId(workflowId);
    	for(ScorecardScore score:scoreList)
    	{
    		score.setIsActive(false);
    		session.update(score);
    	}
    }
}
