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
package com.globalsight.everest.webapp.pagehandler.administration.reports.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.globalsight.everest.costing.Cost;
import com.globalsight.everest.costing.CostByWordCount;
import com.globalsight.everest.costing.Currency;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobSearchParameters;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.JobSearchConstants;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.everest.workflowmanager.Workflow;

public class ReviewerVendorPoReportDataAssembler
{
    private static Logger s_logger = Logger
            .getLogger(ReviewerVendorPoReportDataAssembler.class);

    private HttpServletRequest request = null;

    private XlsReportData reportData = null;

    public ReviewerVendorPoReportDataAssembler(HttpServletRequest p_request)
    {
        this.request = p_request;
        this.reportData = new XlsReportData();
    }

    public XlsReportData getXlsReportData()
    {
        return this.reportData;
    }

    public void setActivityNameList()
    {
        String[] activityNameList = request.getParameterValues("activityName");
        for (int i = 0; i < activityNameList.length; i++)
        {
            if ("*".equals(activityNameList[i]))
            {
                reportData.activityNameList.clear();
                reportData.allActivities = true;
                Collection atc = null;
                try
                {
                    atc = ServerProxy.getJobHandler().getAllActivities();
                }
                catch (Exception e)
                {
                    s_logger.error("Failed to get all activities!", e);
                }
                if (atc != null)
                {
                    Iterator it = atc.iterator();
                    while (it.hasNext())
                    {
                        Activity at = (Activity) it.next();
                        if (at.getActivityType() == Activity.TYPE_REVIEW
                                || at.getActivityType() == Activity.TYPE_REVIEW_EDITABLE)
                        {
                            reportData.activityNameList.add(at
                                    .getActivityName());
                        }
                    }
                }
                break;
            }
            else
            {
                reportData.activityNameList.add(activityNameList[i]);
            }
        }
    }

    public void setJobStateList()
    {
        String[] jobStates = request.getParameterValues("status");

        for (int i = 0; i < jobStates.length; i++)
        {
            if ("*".equals(jobStates[i]))
            {
                reportData.allJobStatus = true;
                break;
            }
            else
            {
                reportData.jobStatusList.add(jobStates[i]);
            }
        }
    }

    public void setProjectIdList(String userId)
    {
        String[] projectIds = request.getParameterValues("projectId");

        for (int i = 0; i < projectIds.length; i++)
        {
            if ("*".equals(projectIds[i]))
            {
                reportData.wantsAllProjects = true;
                try
                {
                    List<Project> projectList = (ArrayList<Project>) ServerProxy
                            .getProjectHandler().getProjectsByUser(userId);
                    for (Project project : projectList)
                    {
                        reportData.projectIdList.add(String.valueOf(project
                                .getIdAsLong()));
                    }
                    break;
                }
                catch (Exception e)
                {
                }
                break;
            }
            else
            {
                reportData.projectIdList.add(new Long(projectIds[i]));
            }
        }
    }

    public void setTargetLangList()
    {
        String[] targetLangs = request.getParameterValues("targetLang");

        for (int i = 0; i < targetLangs.length; i++)
        {
            if ("*".equals(targetLangs[i]))
            {
                reportData.wantsAllTargetLangs = true;
                break;
            }
            else
            {
                reportData.targetLangList.add(targetLangs[i]);
            }
        }
    }

    /**
     * Returns a list of jobs that are in a different project but should be
     * treated as if they're in this project.
     */
    public void setJobsInWrongProject()
    {
        try
        {
            setWrongJobsHelper();
        }
        catch (Exception e)
        {
            s_logger.error(
                    "Failed to add jobs which are in the 'wrong' project.", e);
        }
    }

    /**
     * Adds data for the exported and in-progress jobs. Archived jobs are
     * ignored for now.
     */
    public void setProjectData(boolean p_recalculateFinishedWorkflow)
            throws Exception
    {
        HashMap projectMap = new HashMap();
        Currency pivotCurrency = CurrencyThreadLocal.getCurrency();

        JobSearchParameters searchParams = getSearchParams();
        ArrayList queriedJobs = new ArrayList(ServerProxy.getJobHandler()
                .getJobs(searchParams));
        ArrayList wrongJobs = getWrongJobs();

        // now create a Set for all the jobs
        HashSet jobs = new HashSet();
        jobs.addAll(queriedJobs);
        jobs.addAll(wrongJobs);
        jobs.removeAll(reportData.ignoreJobs);

        // first iterate through the Jobs and group by Project/workflow because
        // Dell doesn't want to see actual Jobs
        Iterator jobIter = jobs.iterator();
        reportData.headers = getHeaders(jobIter);
        jobIter = jobs.iterator();

        while (jobIter.hasNext())
        {
            Job j = (Job) jobIter.next();
            if (!reportData.allJobStatus)
            {
                if (!reportData.jobStatusList.contains(j.getState()))
                {
                    continue;
                }
            }
            boolean isInContextMatch = PageHandler.isInContextMatch(j);
            // only handle jobs in these states
            Iterator wfIter = j.getWorkflows().iterator();
            while (wfIter.hasNext())
            {
                Workflow w = (Workflow) wfIter.next();

                // skip certain workflows
                if (Workflow.PENDING.equals(w.getState())
                        || Workflow.IMPORT_FAILED.equals(w.getState())
                        || Workflow.CANCELLED.equals(w.getState())
                        || Workflow.BATCHRESERVED.equals(w.getState())
                        || Workflow.READY_TO_BE_DISPATCHED.equals(w.getState()))
                {
                    continue;
                }

                // skip workflows without special target lang
                String targetLang = Long.toString(w.getTargetLocale().getId());
                if (!(reportData.wantsAllTargetLangs || reportData.targetLangList
                        .contains(targetLang)))
                {
                    continue;
                }

                // skip workflows without "REPORT_ACTIVITY" activity
                Map<String, Task> dellReviewActivities = new HashMap<String, Task>();
                Iterator tasksIterator = w.getTasks().values().iterator();
                while (tasksIterator.hasNext())
                {
                    Task task = (Task) tasksIterator.next();
                    if (reportData.activityNameList
                            .contains(task.getTaskName()))
                    {
                        if (dellReviewActivities.get(task.getTaskName()) == null)
                        {
                            dellReviewActivities.put(task.getTaskName(), task);
                        }
                    }
                }
                Iterator it = dellReviewActivities.entrySet().iterator();
                while (it.hasNext())
                {
                    Entry entry = (java.util.Map.Entry) it.next();
                    Task dellReviewActivity = (Task) entry.getValue();

                    long jobId = j.getId();
                    HashMap localeAvticityMap = (HashMap) projectMap.get(Long
                            .toString(jobId));
                    if (localeAvticityMap == null)
                    {
                        localeAvticityMap = new HashMap();
                        projectMap.put(Long.toString(jobId), localeAvticityMap);
                    }

                    ProjectWorkflowData data = (ProjectWorkflowData) localeAvticityMap
                            .get(targetLang + dellReviewActivity.getTaskName());
                    if (data == null)
                    {
                        data = new ProjectWorkflowData();
                        localeAvticityMap.put(
                                targetLang + dellReviewActivity.getTaskName(),
                                data);
                        data.jobName = j.getJobName();
                        data.jobId = jobId;
                        data.projectDesc = getProjectDesc(j);
                        data.targetLang = w.getTargetLocale().toString();
                        data.creationDate = j.getCreateDate();
                        data.dellReviewActivityState = dellReviewActivity
                                .getState();
                        data.acceptedReviewerDate = dellReviewActivity
                                .getAcceptedDate();
                        data.currentActivityName = dellReviewActivity
                                .getTaskDisplayName();
                    }

                    // now add or amend the data in the ProjectWorkflowData
                    // based on
                    // this next workflow
                    if (j.getCreateDate().before(data.creationDate))
                    {
                        data.creationDate = j.getCreateDate();
                    }

                    data.repetitionWordCount += w.getRepetitionWordCount();
                    data.dellInternalRepsWordCount += w.getRepetitionWordCount();
                    data.tradosRepsWordCount = data.dellInternalRepsWordCount;
                    data.lowFuzzyMatchWordCount += w
                            .getThresholdLowFuzzyWordCount();
                    data.medFuzzyMatchWordCount += w
                            .getThresholdMedFuzzyWordCount();
                    data.medHiFuzzyMatchWordCount += w
                            .getThresholdMedHiFuzzyWordCount();
                    data.hiFuzzyMatchWordCount += w
                            .getThresholdHiFuzzyWordCount();

                    // the Dell fuzzyMatchWordCount is the sum of the top 3
                    // categories
                    data.dellFuzzyMatchWordCount = data.medFuzzyMatchWordCount
                            + data.medHiFuzzyMatchWordCount
                            + data.hiFuzzyMatchWordCount;
                    data.trados95to99WordCount = data.hiFuzzyMatchWordCount;
                    data.trados85to94WordCount = data.medHiFuzzyMatchWordCount;
                    data.trados75to84WordCount = data.medFuzzyMatchWordCount;
                    data.trados50to74WordCount = data.lowFuzzyMatchWordCount;

                    // add the lowest fuzzies and sublev match to nomatch
                    data.noMatchWordCount += w.getNoMatchWordCount()
                            + data.lowFuzzyMatchWordCount;
                    data.dellNewWordsWordCount = w.getNoMatchWordCount()
                            + w.getLowFuzzyMatchWordCount();
                    data.tradosNoMatchWordCount = data.dellNewWordsWordCount;

                    data.segmentTmWordCount += (isInContextMatch) ? w
                            .getSegmentTmWordCount()
                            : w.getTotalExactMatchWordCount();
                    data.inContextMatchWordCount += (isInContextMatch) ? w
                            .getInContextMatchWordCount() : w
                            .getNoUseInContextMatchWordCount();
                    data.dellExactMatchWordCount = data.segmentTmWordCount;
                    data.dellInContextMatchWordCount = data.inContextMatchWordCount;
                    data.trados100WordCount = data.dellExactMatchWordCount;
                    data.tradosInContextWordCount = data.inContextMatchWordCount;
                    data.tradosContextWordCount = data.contextMatchWordCount;

                    // data.contextMatchWordCount +=
                    // w.getContextMatchWordCount();
                    data.totalWordCount += w.getTotalWordCount();

                    data.dellTotalWordCount = w.getTotalWordCount();

                    data.tradosTotalWordCount = data.trados100WordCount
                            + data.tradosInContextWordCount
                            + data.tradosContextWordCount
                            + data.trados95to99WordCount
                            + data.trados85to94WordCount
                            + data.trados75to84WordCount
                            + data.trados50to74WordCount
                            + data.tradosRepsWordCount
                            + data.tradosNoMatchWordCount;

                    // Counts the total cost for dell activities in the same
                    // workflow.
                    // It's possible that existing mutiple activities with name
                    // "REPORT_ACTIVITY".
                    CostByWordCount dellReivewCostByWordCount = ServerProxy
                            .getCostingEngine()
                            .calculateCost(dellReviewActivity, pivotCurrency,
                                    p_recalculateFinishedWorkflow, Cost.REVENUE)
                            .getCostByWordCount();

                    if (dellReivewCostByWordCount != null)
                    {
                        // repetition costs for activity "REPORT_ACTIVITY"
                        data.repetitionWordCountCostForDellReview = add(
                                data.repetitionWordCountCostForDellReview,
                                dellReivewCostByWordCount.getRepetitionCost());

                        data.dellInternalRepsWordCountCostForDellReview = data.repetitionWordCountCostForDellReview;
                        data.tradosRepsWordCountCostForDellReview = data.repetitionWordCountCostForDellReview;

                        // exact match costs for activity "REPORT_ACTIVITY"
                        data.contextMatchWordCountCostForDellReview = add(
                                data.contextMatchWordCountCostForDellReview, 0);

                        data.inContextMatchWordCountCostForDellReview = add(
                                data.inContextMatchWordCountCostForDellReview,
                                (isInContextMatch) ? dellReivewCostByWordCount
                                        .getInContextMatchCost()
                                        : dellReivewCostByWordCount
                                                .getNoUseInContextMatchCost());

                        data.segmentTmWordCountCostForDellReview = add(
                                data.segmentTmWordCountCostForDellReview,
                                (isInContextMatch) ? dellReivewCostByWordCount.getSegmentTmMatchCost() : dellReivewCostByWordCount.getNoUseExactMatchCost());

                        data.dellExactMatchWordCountCostForDellReview = data.segmentTmWordCountCostForDellReview;
                        data.trados100WordCountCostForDellReview = data.segmentTmWordCountCostForDellReview;

                        data.dellInContextMatchWordCountCostForDellReview = data.inContextMatchWordCountCostForDellReview;
                        data.dellContextMatchWordCountCostForDellReview = data.contextMatchWordCountCostForDellReview;
                        data.tradosInContextWordCountCostForDellReview = data.inContextMatchWordCountCostForDellReview;
                        data.tradosContextWordCountCostForDellReview = data.tradosContextWordCountCostForDellReview;
                        // fuzzy match costs for activity "REPORT_ACTIVITY"
                        data.lowFuzzyMatchWordCountCostForDellReview = add(
                                data.lowFuzzyMatchWordCountCostForDellReview,
                                dellReivewCostByWordCount
                                        .getLowFuzzyMatchCost());

                        data.medFuzzyMatchWordCountCostForDellReview = add(
                                data.medFuzzyMatchWordCountCostForDellReview,
                                dellReivewCostByWordCount
                                        .getMedFuzzyMatchCost());

                        data.medHiFuzzyMatchWordCountCostForDellReview = add(
                                data.medHiFuzzyMatchWordCountCostForDellReview,
                                dellReivewCostByWordCount
                                        .getMedHiFuzzyMatchCost());

                        data.hiFuzzyMatchWordCountCostForDellReview = add(
                                data.hiFuzzyMatchWordCountCostForDellReview,
                                dellReivewCostByWordCount.getHiFuzzyMatchCost());

                        // Dell fuzzy match cost is the sum of the top three
                        // fuzzy
                        // match categories
                        data.dellFuzzyMatchWordCountCostForDellReview = data.medFuzzyMatchWordCountCostForDellReview
                                .add(data.medHiFuzzyMatchWordCountCostForDellReview)
                                .add(data.hiFuzzyMatchWordCountCostForDellReview);

                        // Trados breakdown for fuzzy for activity
                        // "REPORT_ACTIVITY"
                        data.trados95to99WordCountCostForDellReview = data.hiFuzzyMatchWordCountCostForDellReview;

                        data.trados75to94WordCountCostForDellReview = data.medFuzzyMatchWordCountCostForDellReview
                                .add(data.medHiFuzzyMatchWordCountCostForDellReview);

                        data.trados1to74WordCountCostForDellReview = data.lowFuzzyMatchWordCountCostForDellReview;

                        // new words, no match costs for activity
                        // "REPORT_ACTIVITY"
                        data.noMatchWordCountCostForDellReview = add(
                                data.noMatchWordCountCostForDellReview,
                                dellReivewCostByWordCount.getNoMatchCost());

                        data.tradosNoMatchWordCountCostForDellReview = data.noMatchWordCountCostForDellReview;
                        data.dellNewWordsWordCountCostForDellReview = data.noMatchWordCountCostForDellReview
                                .add(data.lowFuzzyMatchWordCountCostForDellReview);

                        // Cost totals for activity "REPORT_ACTIVITY"
                        data.dellTotalWordCountCostForDellReview = data.dellInternalRepsWordCountCostForDellReview
                                .add(data.dellExactMatchWordCountCostForDellReview)
                                .add(data.dellInContextMatchWordCountCostForDellReview)
                                .add(data.dellContextMatchWordCountCostForDellReview)
                                .add(data.dellFuzzyMatchWordCountCostForDellReview)
                                .add(data.dellNewWordsWordCountCostForDellReview);

                        data.tradosTotalWordCountCostForDellReview = data.trados100WordCountCostForDellReview
                                .add(data.tradosInContextWordCountCostForDellReview)
                                .add(data.tradosContextWordCountCostForDellReview)
                                .add(data.trados95to99WordCountCostForDellReview)
                                .add(data.trados75to94WordCountCostForDellReview)
                                .add(data.trados1to74WordCountCostForDellReview)
                                .add(data.tradosRepsWordCountCostForDellReview)
                                .add(data.tradosNoMatchWordCountCostForDellReview);
                    }

                    Cost wfCost = ServerProxy.getCostingEngine().calculateCost(
                            w, pivotCurrency, p_recalculateFinishedWorkflow,
                            Cost.REVENUE, p_recalculateFinishedWorkflow);

                    CostByWordCount costByWordCount = wfCost
                            .getCostByWordCount();
                    if (costByWordCount != null)
                    {
                        // repetition costs
                        data.repetitionWordCountCost = add(
                                data.repetitionWordCountCost,
                                costByWordCount.getRepetitionCost());

                        data.dellInternalRepsWordCountCost = data.repetitionWordCountCost;
                        data.tradosRepsWordCountCost = data.repetitionWordCountCost;

                        // exact match costs
                        data.contextMatchWordCountCost = add(
                                data.contextMatchWordCountCost, 0);

                        data.inContextMatchWordCountCost = add(
                                data.inContextMatchWordCountCost,
                                (isInContextMatch) ? costByWordCount
                                        .getInContextMatchCost()
                                        : costByWordCount
                                                .getNoUseInContextMatchCost());

                        data.segmentTmWordCountCost = add(
                                data.segmentTmWordCountCost,
                                (isInContextMatch) ? costByWordCount.getSegmentTmMatchCost() : costByWordCount.getNoUseExactMatchCost());

                        data.dellExactMatchWordCountCost = data.segmentTmWordCountCost;
                        data.trados100WordCountCost = data.segmentTmWordCountCost;

                        data.dellInContextMatchWordCountCost = data.inContextMatchWordCountCost;
                        data.dellContextMatchWordCountCost = data.contextMatchWordCountCost;
                        data.tradosInContextWordCountCost = data.inContextMatchWordCountCost;
                        data.tradosContextWordCountCost = data.contextMatchWordCountCost;

                        // fuzzy match costs
                        data.lowFuzzyMatchWordCountCost = add(
                                data.lowFuzzyMatchWordCountCost,
                                costByWordCount.getLowFuzzyMatchCost());

                        data.medFuzzyMatchWordCountCost = add(
                                data.medFuzzyMatchWordCountCost,
                                costByWordCount.getMedFuzzyMatchCost());

                        data.medHiFuzzyMatchWordCountCost = add(
                                data.medHiFuzzyMatchWordCountCost,
                                costByWordCount.getMedHiFuzzyMatchCost());

                        data.hiFuzzyMatchWordCountCost = add(
                                data.hiFuzzyMatchWordCountCost,
                                costByWordCount.getHiFuzzyMatchCost());

                        // Dell fuzzy match cost is the sum of the top three
                        // fuzzy
                        // match categories
                        data.dellFuzzyMatchWordCountCost = data.medFuzzyMatchWordCountCost
                                .add(data.medHiFuzzyMatchWordCountCost).add(
                                        data.hiFuzzyMatchWordCountCost);

                        // Trados breakdown for fuzzy
                        data.trados95to99WordCountCost = data.hiFuzzyMatchWordCountCost;

                        data.trados85to94WordCountCost = data.medHiFuzzyMatchWordCountCost;

                        data.trados75to84WordCountCost = data.medFuzzyMatchWordCountCost;

                        data.trados50to74WordCountCost = data.lowFuzzyMatchWordCountCost;

                        // new words, no match costs
                        data.noMatchWordCountCost = add(
                                data.noMatchWordCountCost,
                                costByWordCount.getNoMatchCost());

                        data.tradosNoMatchWordCountCost = data.noMatchWordCountCost;

                        data.dellNewWordsWordCountCost = data.noMatchWordCountCost
                                .add(data.lowFuzzyMatchWordCountCost);

                        // totals
                        data.dellTotalWordCountCost = data.dellInternalRepsWordCountCost
                                .add(data.dellExactMatchWordCountCost)
                                .add(data.dellInContextMatchWordCountCost)
                                .add(data.dellContextMatchWordCountCost)
                                .add(data.dellFuzzyMatchWordCountCost)
                                .add(data.dellNewWordsWordCountCost);

                        data.totalWordCountCost = data.dellTotalWordCountCost;

                        data.tradosTotalWordCountCost = data.trados100WordCountCost
                                .add(data.tradosInContextWordCountCost)
                                .add(data.tradosContextWordCountCost)
                                .add(data.trados95to99WordCountCost)
                                .add(data.trados85to94WordCountCost)
                                .add(data.trados75to84WordCountCost)
                                .add(data.trados50to74WordCountCost)
                                .add(data.tradosRepsWordCountCost)
                                .add(data.tradosNoMatchWordCountCost);

                        // transltion total
                        data.dellTotalWordCountCostForTranslation = data.dellTotalWordCountCost
                                .subtract(data.dellTotalWordCountCostForDellReview);
                        data.tradosTotalWordCountCostForTranslation = data.tradosTotalWordCountCost
                                .subtract(data.tradosTotalWordCountCostForDellReview);
                    }

                }

                // now recalculate the job level cost if the workflow was
                // recalculated
                if (p_recalculateFinishedWorkflow)
                {
                    ServerProxy.getCostingEngine().reCostJob(j, pivotCurrency,
                            Cost.REVENUE);
                }

            }
        }

        reportData.projectMap = projectMap;
        reportData.jobIdList = new ArrayList(projectMap.keySet());
    }

    /*
     * ----------------------------------------------------------------------
     * Below are helper methods
     * ----------------------------------------------------------------------
     */

    /**
     * Opens up the file "jobsInWrongDivision.txt" and returns only the jobs
     * that should be mapped to the requested projects
     */
    private void setWrongJobsHelper() throws Exception
    {
        SystemConfiguration sc = SystemConfiguration.getInstance();
        StringBuffer mapFile = new StringBuffer(
                sc.getStringParameter(SystemConfigParamNames.GLOBALSIGHT_HOME_DIRECTORY));

        mapFile.append(File.separator);
        mapFile.append("jobsInWrongDivision.txt");

        File f = new File(mapFile.toString());
        if (!f.exists())
        {
            s_logger.warn("jobsInWrongDivision.txt file not found.");
            return;
        }

        BufferedReader reader = new BufferedReader(new FileReader(f));
        String line = null;

        while ((line = reader.readLine()) != null)
        {
            if (line.startsWith("#") || line.length() == 0)
                continue;

            String jobname = null;
            String projectname = null;

            try
            {
                // the format is jobname = projectname
                String[] tokens = line.split("=");
                jobname = tokens[0].trim();
                projectname = tokens[1].trim();

                Project p = ServerProxy.getProjectHandler().getProjectByName(
                        projectname);
                Long newProjectId = p.getIdAsLong();

                // ISSUE: If two jobs have the same name, it goes wrong.
                JobSearchParameters sp = new JobSearchParameters();
                sp.setJobName(jobname);
                ArrayList jobs = new ArrayList(ServerProxy.getJobHandler()
                        .getJobs(sp));
                Job j = (Job) jobs.get(0);

                if (!(reportData.wantsAllProjects || reportData.projectIdList
                        .contains(newProjectId)))
                {
                    reportData.ignoreJobs.add(j);
                    continue;
                }

                // but if the wrong job is actually currently in a project we
                // are reporting on, then skip it as well
                // if the project id list contains the old project but not the
                // new project
                Long oldProjectId = j.getL10nProfile().getProject()
                        .getIdAsLong();
                if (reportData.projectIdList.contains(oldProjectId)
                        && !reportData.projectIdList.contains(newProjectId))
                {
                    reportData.ignoreJobs.add(j);
                    continue;
                }

                reportData.wrongJobs.add(j);
                reportData.wrongJobNames.add(j.getJobName());
                reportData.wrongJobMap.put(new Long(j.getId()), p);
            }
            catch (Exception e)
            {
                s_logger.warn("Ignoring mapping line for "
                        + jobname
                        + " => "
                        + projectname
                        + ". Either the job doesn't exist, the project doesn't exist, or both.");
            }
        }

        reader.close();
    }

    /**
     * Returns search params used to find the jobs based on state
     * (READY,EXPORTED,LOCALIZED,DISPATCHED), and creation date during the range
     * 
     * @return JobSearchParams
     */
    private JobSearchParameters getSearchParams()
    {
        JobSearchParameters sp = new JobSearchParameters();
        
        sp.setProjectId(reportData.projectIdList);

        // job state EXPORTED,DISPATCHED,LOCALIZED
        ArrayList<String> list = new ArrayList<String>();
        list.add(Job.READY_TO_BE_DISPATCHED);
        list.add(Job.DISPATCHED);
        list.add(Job.LOCALIZED);
        list.add(Job.EXPORTED);
        list.add(Job.EXPORT_FAIL);
        list.add(Job.ARCHIVED);
        sp.setJobState(list);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
        try {
        	String paramCreateDateStartCount = request
        			.getParameter(JobSearchConstants.CREATION_START);
        	if (paramCreateDateStartCount != null && paramCreateDateStartCount != "")
        	{
        		sp.setCreationStart(simpleDateFormat.parse(paramCreateDateStartCount));
        	}
        	
        	String paramCreateDateEndCount = request
                    .getParameter(JobSearchConstants.CREATION_END);
           if (paramCreateDateEndCount != null && paramCreateDateEndCount != "")
            {
        	   Date date = simpleDateFormat.parse(paramCreateDateEndCount);
        	   long endLong = date.getTime()+(24*60*60*1000-1);
                sp.setCreationEnd(new Date(endLong));
            }
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        

        return sp;
    }

    // returns jobs in the specified criteria date range
    private ArrayList getWrongJobs()
    {
        ArrayList wrongJobs = new ArrayList();
        Iterator iter = reportData.wrongJobs.iterator();
        Calendar cal = Calendar.getInstance();
        while (iter.hasNext())
        {
            Job j = (Job) iter.next();
            cal.setTime(j.getCreateDate());

            // check the job's time against the search criteria
            wrongJobs.add(j);
        }
        return wrongJobs;
    }

    // Get project description
    private String getProjectDesc(Job p_job)
    {
        Project p = (Project) reportData.wrongJobMap
                .get(new Long(p_job.getId()));
        if (p == null)
            p = p_job.getL10nProfile().getProject();
        String d = p.getDescription();
        String desc = null;
        if (d == null || d.length() == 0)
            desc = p.getName();
        else
            desc = p.getName() + ": " + d;
        return desc;
    }

    /**
     * Adds the given float to the BigDecimal after scaling it to 3(SCALE)
     * decimal points of precision and rounding half up. If you don't do this,
     * then the float 0.255 will become 0.254999995231628 Returns a new
     * BigDecimal which is the sum of a and p_f
     */
    private BigDecimal add(BigDecimal p_a, float p_f)
    {
        String floatString = Float.toString(p_f);
        BigDecimal bd = new BigDecimal(floatString);
        BigDecimal sbd = bd.setScale(ProjectWorkflowData.SCALE,
                BigDecimal.ROUND_HALF_UP);
        return p_a.add(sbd);
    }

    private String[] getHeaders(Iterator iter)
    {
        String[] headers = new String[1];
        while (iter.hasNext())
        {
            Job job = (Job) iter.next();
            // TranslationMemoryProfile tmProfile =
            // job.getL10nProfile().getTranslationMemoryProfile();
            if (PageHandler.isInContextMatch(job))
            {
                // hava tm profile contains in context match
                headers[0] = "In Context Match";
            }
        }
        return headers;
    }
}
