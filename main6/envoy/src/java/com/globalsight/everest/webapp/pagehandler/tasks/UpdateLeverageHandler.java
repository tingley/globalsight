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

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.globalsight.everest.company.MultiCompanySupportedThread;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.integration.ling.LingServerProxy;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.jobhandler.JobSearchParameters;
import com.globalsight.everest.page.PrimaryFile;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.taskmanager.TaskManager;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.util.comparator.JobComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.ActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageActionHandler;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.everest.workflowmanager.WorkflowManager;
import com.globalsight.ling.tm.LeverageMatchLingManager;
import com.globalsight.ling.tm.LeverageSegment;
import com.globalsight.ling.tm.LeveragingLocales;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.ling.tm2.TmUtil;
import com.globalsight.ling.tm2.leverage.LeverageDataCenter;
import com.globalsight.ling.tm2.leverage.LeverageMatches;
import com.globalsight.ling.tm2.leverage.LeverageOptions;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.SortUtil;

public class UpdateLeverageHandler extends PageActionHandler
{
    private static final Logger logger = Logger
            .getLogger(UpdateLeverageHandler.class);

    private static LeverageMatchLingManager leverageMatchLingManager = LingServerProxy
            .getLeverageMatchLingManager();
    private TaskManager taskManager = ServerProxy.getTaskManager();
    private WorkflowManager workflowManager = ServerProxy.getWorkflowManager();

    /**
     * Map<Long, Long>: jobID:percentage
     */
    private static Map<Long, Integer> percentageMap = new HashMap<Long, Integer>();

    /**
     * Get available jobs for task when click "update leverage" button on task
     * details page.
     * 
     * @param p_request
     * @param p_response
     * @param form
     * @throws Exception
     */
    @ActionHandler(action = "getAvailableJobsForTask", formClass = "")
    public void getAvailableJobsForTask(HttpServletRequest p_request,
            HttpServletResponse p_response, Object form) throws Exception
    {
        List<Job> availableJobs = new ArrayList<Job>();
        HttpSession session = p_request.getSession(false);
        String strTaskId = (String) p_request
                .getParameter(WebAppConstants.TASK_ID);

        try
        {
            Task task = taskManager.getTask(Long.parseLong(strTaskId));

            // Available jobs should be 1) in the same project, 2) this job
            // should also have a workflow with the same target locale, 3) this
            // workflow must be "In Progress" or "Localized"(translation work is
            // in progress or finished, but not populate into storage TM yet).
            JobSearchParameters jobSearchParam = new JobSearchParameters();
            long projectId = task.getWorkflow().getJob().getProjectId();
            jobSearchParam.setProjectId(String.valueOf(projectId));// for 1)
            jobSearchParam.setSourceLocale(task.getSourceLocale());// for 2)
            jobSearchParam.setTargetLocale(task.getTargetLocale());// for 2)
            Collection<JobImpl> jobs = ServerProxy.getJobHandler().getJobs(
                    jobSearchParam);

            for (Iterator<JobImpl> jobIter = jobs.iterator(); jobIter.hasNext();)
            {
                Job job = (Job) jobIter.next();
                Collection<Workflow> wfs = job.getWorkflows();
                for (Workflow wf : wfs)
                {
                    // for 3)
                    if (wf.getTargetLocale().equals(task.getTargetLocale()))
                    {
                        String wfState = wf.getState();
                        if (Workflow.DISPATCHED.equalsIgnoreCase(wfState)
                                || Workflow.LOCALIZED.equalsIgnoreCase(wfState)
                                || Workflow.EXPORT_FAILED
                                        .equalsIgnoreCase(wfState))
                        {
                            availableJobs.add(job);
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new EnvoyServletException(e);
        }

        Locale uiLocale = (Locale) session
                .getAttribute(WebAppConstants.UILOCALE);
        SortUtil.sort(availableJobs, new JobComparator(JobComparator.NAME,
                uiLocale));
        p_request.setAttribute("availableJobs", availableJobs);
        p_request.setAttribute(WebAppConstants.TASK_ID, strTaskId);
    }

    @ActionHandler(action = "checkHaveNonReadyWFSelected", formClass = "")
    public void checkHaveNonReadyWFSelected(HttpServletRequest p_request,
            HttpServletResponse p_response, Object form) throws Exception
    {
        HttpSession session = p_request.getSession(false);
        Locale uiLocale = (Locale) session
                .getAttribute(WebAppConstants.UILOCALE);

        String selectedWfs = (String) p_request
                .getParameter("selectedWorkFlows");
        List<Long> wfIds = UpdateLeverageHelper.getWfIds(selectedWfs);
        StringBuffer readyWfIds = new StringBuffer();
        StringBuffer nonReadyWfs = new StringBuffer();
        int count = 0;
        for (Iterator<Long> it = wfIds.iterator(); it.hasNext();)
        {
            Long wfId = it.next();
            Workflow wf = workflowManager.getWorkflowById(wfId);
            if (Workflow.READY_TO_BE_DISPATCHED.equals(wf.getState()))
            {
                readyWfIds.append(" ").append(wfId);
            }
            else
            {
                count++;
                String wfDisplayName = wf.getTargetLocale().getDisplayName(
                        uiLocale);
                if (nonReadyWfs.length() == 0)
                {
                    nonReadyWfs.append(count).append(". ")
                            .append(wfDisplayName);
                }
                else
                {
                    nonReadyWfs.append("_returnPH_").append(count).append(". ")
                            .append(wfDisplayName);
                }
            }
        }

        // Write back
        String strReadyWfIds = (readyWfIds.toString().trim().length() > 0 ? readyWfIds
                .toString().trim() : "");
        String strNonReadyWfs = (nonReadyWfs.length() > 0 ? nonReadyWfs
                .toString() : "");
        ServletOutputStream out = p_response.getOutputStream();
        try
        {
            p_response.setContentType("text/plain");
            out = p_response.getOutputStream();
            StringBuffer sb = new StringBuffer();
            sb.append("{\"readyWfIds\":\"");
            sb.append(strReadyWfIds).append("\", \"nonReadyWfs\":\"")
                    .append(strNonReadyWfs).append("\"}");
            out.write(sb.toString().getBytes("UTF-8"));
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
        }
        finally
        {
            out.close();
            pageReturn();
        }
    }

    /**
     * Get available jobs for specified workflows when click "update leverage"
     * button on job details page.
     * 
     * @param p_request
     * @param p_response
     * @param form
     * @throws Exception
     */
    @ActionHandler(action = "getAvailableJobsForWfs", formClass = "")
    public void getAvailableJobsForWfs(HttpServletRequest p_request,
            HttpServletResponse p_response, Object form) throws Exception
    {
        List<Job> availableJobs = new ArrayList<Job>();
        HttpSession session = p_request.getSession(false);
        String strWfIds = (String) p_request.getParameter("wfId");

        try
        {
            List<Long> wfIds = UpdateLeverageHelper.getWfIds(strWfIds);
            Workflow tmpWf = workflowManager.getWorkflowById(wfIds.get(0));
            GlobalSightLocale sourceLocale = tmpWf.getJob().getSourceLocale();
            long projectId = tmpWf.getJob().getProjectId();

            // Available jobs should be 1) in the same project, 2) this job
            // should also have a workflow with the same target locale, 3) this
            // workflow must be "Ready", "In Progress" or
            // "Localized"(translation work is in progress or finished, but not
            // populate into storage TM yet).
            JobSearchParameters jobSearchParam = new JobSearchParameters();
            jobSearchParam.setProjectId(String.valueOf(projectId));// for 1)
            jobSearchParam.setSourceLocale(sourceLocale);// for 2)
            Collection<JobImpl> jobs = ServerProxy.getJobHandler().getJobs(
                    jobSearchParam);
            for (Iterator<JobImpl> it = jobs.iterator(); it.hasNext();)
            {
                if (Job.CANCELLED.equals(it.next().getState()))
                {
                    it.remove();
                }
            }
            availableJobs.addAll(jobs);
        }
        catch (Exception e)
        {
            logger.error("Fail to get available jobs to update leverage from.",
                    e);
            throw new EnvoyServletException(e);
        }

        Locale uiLocale = (Locale) session
                .getAttribute(WebAppConstants.UILOCALE);
        SortUtil.sort(availableJobs, new JobComparator(JobComparator.NAME,
                uiLocale));
        p_request.setAttribute("availableJobs", availableJobs);
        p_request.setAttribute("wfIds", strWfIds);
    }

    /**
     * Update from specified jobs or/and re-leverage reference TMs.
     */
    @ActionHandler(action = "updateLeverage", formClass = "")
    public void updateLeverage(HttpServletRequest p_request,
            HttpServletResponse p_response, Object form) throws Exception
    {
        // Initial parameters and values
        String reApplyRefTmsCheckBox = p_request
                .getParameter("reApplyReferenceTmsName");
        String updateFromJobCheckBox = p_request
                .getParameter("updateFromJobCheckBoxName");
        String selectedJobIds = p_request.getParameter("selectJobs");
        String ipTmPenalty = p_request.getParameter("inProgressTmPenaltyName");
        int intIpTmPenalty = 0;
        if ("on".equals(updateFromJobCheckBox))
        {
            try
            {
                intIpTmPenalty = Integer.parseInt(ipTmPenalty);
            }
            catch (Exception e)
            {
                logger.error("Invalid In Progress TM penalty", e);
                throw new EnvoyServletException(e);
            }
        }
        String currentUserId = p_request.getParameter("userId");

        String fromWhere = null;
        String taskId = p_request.getParameter(WebAppConstants.TASK_ID);
        if (taskId != null && !"".equals(taskId.trim()))
        {
            taskManager.getTask(Long.valueOf(taskId));
            fromWhere = "fromTaskDetail";
        }
        p_request.setAttribute(WebAppConstants.TASK_ID, taskId);

        String strWfIds = p_request.getParameter("wfIds");
        p_request.setAttribute("wfIds", strWfIds);
        List<Long> allSelectedWfIds = UpdateLeverageHelper.getWfIds(strWfIds);
        if (allSelectedWfIds.size() > 0)
        {
            fromWhere = "fromJobDetail";
        }

        if ("fromTaskDetail".equals(fromWhere))
        {
            doUpdateLeverageForTask(Long.valueOf(taskId),
                    reApplyRefTmsCheckBox, updateFromJobCheckBox,
                    selectedJobIds, intIpTmPenalty, currentUserId);
        }
        else if ("fromJobDetail".equals(fromWhere))
        {
            doUpdateLeverageForWfs(allSelectedWfIds, reApplyRefTmsCheckBox,
                    updateFromJobCheckBox, selectedJobIds, intIpTmPenalty,
                    currentUserId);
        }
    }

    @ActionHandler(action = "getPercentage", formClass = "")
    public void getPercentage(HttpServletRequest p_request,
            HttpServletResponse p_response, Object form) throws Exception
    {
        String jobId = p_request.getParameter("jobId");
        int percentage = percentageMap.get(Long.parseLong(jobId));
        String fakeParam = p_request.getParameter("fresh");
        if (logger.isDebugEnabled())
        {
            logger.debug("FakeParam is " + fakeParam + "; Current jobId is "
                    + jobId + "; Percentage is " + percentage);            
        }

        ServletOutputStream out = p_response.getOutputStream();
        try
        {
            p_response.setContentType("text/plain");
            out = p_response.getOutputStream();
            StringBuffer sb = new StringBuffer();
            sb.append("{\"updateLeveragePercentage\":");
            sb.append(percentage).append("}");
            out.write(sb.toString().getBytes("UTF-8"));
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
        }
        finally
        {
            out.close();
            pageReturn();
        }
    }

    @SuppressWarnings("unchecked")
    private void doUpdateLeverageForTask(final Long p_taskId,
            final String p_reApplyRefTms, final String p_updateFromJobs,
            final String p_selectedJobIds, final int p_ipTmPenalty,
            final String p_currentUserId)
    {
        Runnable runnable = new Runnable()
        {
            public void run()
            {
                try
                {
                    Task task = taskManager.getTask(p_taskId);
                    long jobId = task.getJobId();
                    Workflow workflow = task.getWorkflow();
                    List<TargetPage> targetPages = workflow
                            .getTargetPages(PrimaryFile.EXTRACTED_FILE);
                    percentageMap.put(jobId, 0);
                    int count = 0;
                    int total = targetPages.size() * 2;
                    for (TargetPage tp : targetPages)
                    {
                        // 1.Re-leverage reference TMs
                        if ("on".equals(p_reApplyRefTms))
                        {
                            reApplyRefTms(task.getWorkflow(), tp,
                                    p_currentUserId);
                        }
                        count++;
                        percentageMap.put(jobId,
                                Math.round(count * 100 / total));
                        // 2. Update from jobs
                        if ("on".equals(p_updateFromJobs))
                        {
                            String[] selectedJobs = StringUtils.split(
                                    p_selectedJobIds, " ");
                            updateFromJobs(task.getWorkflow(), selectedJobs,
                                    p_ipTmPenalty, tp, p_currentUserId);
                        }
                        count++;
                        percentageMap.put(jobId,
                                Math.round(count * 100 / total));
                    }
                    percentageMap.put(jobId, 100);
                }
                catch (Exception ex)
                {
                    logger.error(ex.getMessage(), ex);
                }
            }
        };

        try
        {
            // Set the percentage to 0 to ensure previous operation won't impact
            // this time.
            Task task = taskManager.getTask(p_taskId);
            long jobId = task.getJobId();
            percentageMap.put(jobId, 0);
        }
        catch (Exception ex)
        {
            logger.warn("Failed to set the update leverage percentage to 0");
            ex.printStackTrace();
        }

        Thread t = new MultiCompanySupportedThread(runnable);
        t.setName("Update Leverage " + String.valueOf(p_currentUserId));
        t.start();
    }

    @SuppressWarnings("unchecked")
    private void doUpdateLeverageForWfs(final List<Long> p_workflowIds,
            final String p_reApplyRefTms, final String p_updateFromJobs,
            final String p_selectedJobIds, final int p_ipTmPenalty,
            final String p_currentUserId) throws Exception
    {
        Runnable runnable = new Runnable()
        {
            public void run()
            {
                try
                {
                    Job job = workflowManager.getWorkflowById(
                            p_workflowIds.get(0)).getJob();
                    int sourcePageNumber = job.getSourcePages(
                            PrimaryFile.EXTRACTED_FILE).size();
                    long jobId = job.getId();
                    percentageMap.put(jobId, 0);
                    // Only ready workflows should be updated leverage from job
                    // details UI.
                    List<Long> validWfIds = UpdateLeverageHelper
                            .filterWorkflowsByState(p_workflowIds,
                                    Workflow.READY_TO_BE_DISPATCHED);
                    if (validWfIds != null && validWfIds.size() > 0)
                    {
                        int count = 0;
                        int total = validWfIds.size() * sourcePageNumber * 2;
                        for (Iterator<Long> it = validWfIds.iterator(); it.hasNext();)
                        {
                            Long wfId = it.next();
                            Workflow wf = workflowManager.getWorkflowById(wfId);
                            List<TargetPage> targetPages = wf
                                    .getTargetPages(PrimaryFile.EXTRACTED_FILE);
                            for (TargetPage tp : targetPages)
                            {
                                // 1.Re-leverage reference TMs
                                if ("on".equals(p_reApplyRefTms))
                                {
                                    reApplyRefTms(wf, tp, p_currentUserId);
                                }
                                count++;
                                percentageMap.put(jobId,
                                        Math.round(count * 100 / total));
                                // 2. Update from jobs
                                if ("on".equals(p_updateFromJobs))
                                {
                                    String[] selectedJobs = StringUtils.split(
                                            p_selectedJobIds, " ");
                                    selectedJobs = filterJobs(selectedJobs, wf);
                                    if (selectedJobs.length > 0)
                                    {
                                        updateFromJobs(wf, selectedJobs,
                                                p_ipTmPenalty, tp,
                                                p_currentUserId);
                                    }
                                }
                                count++;
                                percentageMap.put(jobId,
                                        Math.round(count * 100 / total));
                            }
                        }
                    }
                    // Anyway, set this to ensure the process bar will end.
                    percentageMap.put(jobId, 100);
                    logger.info("========== Update Leverage finished ==========");
                }
                catch (Exception ex)
                {
                    logger.error(ex.getMessage(), ex);
                }
            }
        };

        // Set the percentage to 0 to ensure previous operation won't impact
        // this time.
        Job job = workflowManager.getWorkflowById(p_workflowIds.get(0))
                .getJob();
        percentageMap.put(job.getId(), 0);
        Thread t = new MultiCompanySupportedThread(runnable);
        t.setName("Update Leverage " + String.valueOf(p_currentUserId));
        t.start();
    }

    private void reApplyRefTms(Workflow p_wf, TargetPage p_targetPage,
            String p_userId) throws Exception
    {
        GlobalSightLocale sourceLocale = p_wf.getJob().getSourceLocale();
        GlobalSightLocale targetLocale = p_wf.getTargetLocale();

        SourcePage sp = p_targetPage.getSourcePage();
        long sourcePageId = sp.getId();
        long jobId = sp.getJobId();

        // 1. Untranslated source segments
        Collection<Tuv> untranslatedSrcTuvs = UpdateLeverageHelper
                .getUntranslatedTuvs(p_targetPage, sourceLocale.getId());

        // 2. Convert all untranslated source TUVs to "BaseTmTuv".
        List<BaseTmTuv> sourceTuvs = new ArrayList<BaseTmTuv>();
        for (Tuv srcTuv : untranslatedSrcTuvs)
        {
            // TODO: Need care sub segments?
            BaseTmTuv btt = TmUtil.createTmSegment(srcTuv, "0", jobId);
            sourceTuvs.add(btt);
        }

        // 3. Re-leverage reference TMs
        LeverageDataCenter levDataCenter = null;
        if (sourceTuvs.size() > 0)
        {
			levDataCenter = UpdateLeverageHelper.reApplyReferenceTMs(p_wf,
					sourceTuvs);
        }

        // 4. Re-save leverage matches
        if (levDataCenter != null)
        {
        	List<Long> originalSourceTuvIds = new ArrayList<Long>();
			Iterator<LeverageMatches> itLeverageMatches = levDataCenter
					.leverageResultIterator();
            while (itLeverageMatches.hasNext())
            {
				LeverageMatches levMatches = itLeverageMatches.next();
                originalSourceTuvIds.add(levMatches.getOriginalTuv().getId());
            }

			leverageMatchLingManager.deleteLeverageMatches(
					originalSourceTuvIds, targetLocale,
					LeverageMatchLingManager.DEL_LEV_MATCHES_GOLD_TM_ONLY,
					jobId);

            Connection conn = null;
            try
            {
                conn = DbUtil.getConnection();
				leverageMatchLingManager.saveLeverageResults(conn, sp,
						levDataCenter);
            }
            catch (Exception e)
            {
                throw new EnvoyServletException(e);
            }
            finally
            {
                DbUtil.silentReturnConnection(conn);
            }
        }

        // 5. Populate into target TUVs
        if (untranslatedSrcTuvs != null && untranslatedSrcTuvs.size() > 0)
        {
			TranslationMemoryProfile tmProfile = p_wf.getJob().getL10nProfile()
					.getTranslationMemoryProfile();
            int mode = UpdateLeverageHelper.getMode(tmProfile);
			Map<Long, ArrayList<LeverageSegment>> exactMap = leverageMatchLingManager
					.getExactMatchesWithSetInside(sourcePageId,
							targetLocale.getIdAsLong(), mode, tmProfile);
            if (exactMap != null && exactMap.size() > 0)
            {
                try
                {
                    UpdateLeverageHelper.populateExactMatchesToTargetTuvs(
                            exactMap, untranslatedSrcTuvs, targetLocale,
                            p_userId, sourcePageId, jobId);
                }
                catch (Exception e)
                {
                    throw new EnvoyServletException(e);
                }
            }
        }
    }

    private void updateFromJobs(Workflow p_workflow, String[] p_selectedJobIds,
            int p_ipTmPenalty, TargetPage p_targetPage, String p_userId)
            throws Exception
    {
        GlobalSightLocale sourceLocale = p_workflow.getJob().getSourceLocale();
        GlobalSightLocale targetLocale = p_workflow.getTargetLocale();

        SourcePage sp = p_targetPage.getSourcePage();
        long sourcePageId = sp.getId();
        long jobId = sp.getJobId();

        // 1. Untranslated source segments
        Collection<Tuv> untranslatedSrcTuvs = UpdateLeverageHelper
                .getUntranslatedTuvs(p_targetPage, sourceLocale.getId());

        // 2. Convert all untranslated source TUVs to "BaseTmTuv".
        List<BaseTmTuv> sourceTuvs = new ArrayList<BaseTmTuv>();
        for (Tuv srcTuv : untranslatedSrcTuvs)
        {
            // TODO: Need care sub segments?
            BaseTmTuv btt = TmUtil.createTmSegment(srcTuv, "0", jobId);
            sourceTuvs.add(btt);
        }

        // 3. Update from jobs
        Map<Long, LeverageMatches> ipMatches = new HashMap<Long, LeverageMatches>();
        TranslationMemoryProfile tmProfile = UpdateLeverageHelper
                .getTMProfile(p_workflow);
        // 3.1 Get in progress translations from jobs
        if (sourceTuvs.size() > 0)
        {
            ipMatches = UpdateLeverageHelper.getInProgressTranslationFromJobs(
                    p_workflow, sourceTuvs, p_selectedJobIds);
        }

        if (ipMatches.size() > 0)
        {
            // Apply In Progress TM penalty
            List<Long> originalSourceTuvIds = new ArrayList<Long>();
            for (LeverageMatches levMatches : ipMatches.values())
            {
                UpdateLeverageHelper.applyInProgressTmPenalty(levMatches,
                        tmProfile, targetLocale, p_ipTmPenalty);
                originalSourceTuvIds.add(levMatches.getOriginalTuv().getId());
            }

            // Delete original in-progress matches
			leverageMatchLingManager.deleteLeverageMatches(
					originalSourceTuvIds, targetLocale,
					LeverageMatchLingManager.DEL_LEV_MATCHES_IP_TM_ONLY, jobId);

            // Save new matches into "leverage_match"
            L10nProfile lp = p_workflow.getJob().getL10nProfile();
            LeveragingLocales leveragingLocales = lp.getLeveragingLocales();
            LeverageOptions leverageOptions = new LeverageOptions(tmProfile,
                    leveragingLocales);
            Connection conn = null;
            try
            {
                conn = DbUtil.getConnection();
                leverageMatchLingManager.saveLeverageResults(conn,
                        sourcePageId, ipMatches, targetLocale,
                        leverageOptions);
            }
            catch (Exception e)
            {
                throw new EnvoyServletException(e);
            }
            finally
            {
                DbUtil.silentReturnConnection(conn);
            }

            // Populate into target TUVs
            int mode = UpdateLeverageHelper.getMode(tmProfile);
            Map<Long, ArrayList<LeverageSegment>> exactMap = leverageMatchLingManager
                    .getExactMatchesWithSetInside(sourcePageId,
                            targetLocale.getIdAsLong(), mode, tmProfile);
            if (exactMap != null && exactMap.size() > 0)
            {
                try
                {
                    UpdateLeverageHelper.populateExactMatchesToTargetTuvs(
                            exactMap, untranslatedSrcTuvs, targetLocale,
                            p_userId, sourcePageId, jobId);
                }
                catch (Exception e)
                {
                    throw new EnvoyServletException(e);
                }
            }
        }
    }

    /**
     * Available job must has a workflow in "READY_TO_BE_DISPATCHED",
     * "DISPATCHED", "LOCALIZED" or "EXPORT_FAILED" state, and this workflow
     * must has same source and target locales with the specified workflow.
     * 
     * @param p_selectedJobIds
     * @param p_workflow
     * @return
     * @throws Exception
     */
    private String[] filterJobs(String[] p_selectedJobIds, Workflow p_workflow)
            throws Exception
    {
        List<Long> jobIds = new ArrayList<Long>();

        GlobalSightLocale sourceLocale = p_workflow.getJob().getSourceLocale();
        GlobalSightLocale targetLocale = p_workflow.getTargetLocale();
        for (int i = 0; i < p_selectedJobIds.length; i++)
        {
            long jobId = Long.parseLong(p_selectedJobIds[i]);
            Job job = ServerProxy.getJobHandler().getJobById(jobId);
            for (Workflow wf : job.getWorkflows())
            {
                if (wf.getTargetLocale().equals(targetLocale)
                        && wf.getJob().getSourceLocale().equals(sourceLocale))
                {
                    String wfState = wf.getState();
                    if (Workflow.READY_TO_BE_DISPATCHED
                            .equalsIgnoreCase(wfState)
                            || Workflow.DISPATCHED.equalsIgnoreCase(wfState)
                            || Workflow.LOCALIZED.equalsIgnoreCase(wfState)
                            || Workflow.EXPORT_FAILED.equalsIgnoreCase(wfState))
                    {
                        jobIds.add(jobId);
                        break;
                    }
                }
            }
        }
        String[] result = new String[jobIds.size()];
        for (int j = 0; j < jobIds.size(); j++)
        {
            result[j] = String.valueOf(jobIds.get(j));
        }

        return result;
    }

    @Override
    public void beforeAction(HttpServletRequest request,
            HttpServletResponse response)
    {

    }

    @Override
    public void afterAction(HttpServletRequest request,
            HttpServletResponse response)
    {

    }
}
