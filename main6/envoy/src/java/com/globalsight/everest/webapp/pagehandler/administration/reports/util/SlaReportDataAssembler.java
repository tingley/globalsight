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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.globalsight.calendar.BaseFluxCalendar;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobSearchParameters;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.JobSearchConstants;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.everest.workflow.WorkflowTaskInstance;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.scheduling.EventScheduler;
import com.globalsight.util.date.DateHelper;

public class SlaReportDataAssembler
{
    private static Logger s_logger = Logger
            .getLogger(SlaReportDataAssembler.class);

    private HttpServletRequest request = null;

    private XlsReportData reportData = null;
    
    public SlaReportDataAssembler(HttpServletRequest p_request)
    {
        this.request = p_request;
        this.reportData = new XlsReportData();
    }

    public XlsReportData getXlsReportData()
    {
        return this.reportData;
    }

    public void setJobIdList()
    {
        String[] jobIds = request.getParameterValues("jobId");

        for (int i = 0; i < jobIds.length; i++)
        {
            if ("*".equals(jobIds[i]))
            {
                reportData.wantsAllJobs = true;
                break;
            }
            else
            {
                reportData.jobIdList.add(jobIds[i]);
            }
        }
    }

    public void setProjectIdList(String userId) throws Exception
    {
        String[] projectIds = request.getParameterValues("projectId");

        for (int i = 0; i < projectIds.length; i++)
        {
            if ("*".equals(projectIds[i]))
            {
                reportData.wantsAllProjects = true;
                for (Project project : (ArrayList<Project>) ServerProxy
                        .getProjectHandler().getProjectsByUser(userId))
                {
                    reportData.projectIdList.add(project.getIdAsLong());
                }
                break;
            }
            else
            {
                reportData.projectIdList.add(new Long(projectIds[i]));
            }
        }
    }

    public void setStatusList()
    {
        String[] status = request.getParameterValues("status");

        if ((status == null) || "*".equals(status[0]))
        {
            // just do a query for all in progress jobs, localized, and exported
            reportData.statusList.add(Job.DISPATCHED);
            reportData.statusList.add(Job.LOCALIZED);
            reportData.statusList.add(Job.EXPORTED);
        }
        else
        {
            for (int i = 0; i < status.length; i++)
            {
                reportData.statusList.add(status[i]);
            }
        }
    }

    public void setTargetLangList()
    {
        String[] targetLangs = request.getParameterValues("targetLocalesList");

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

    public void setDateFormat()
    {
        String dateFormat = request.getParameter("dateFormat");
        reportData.dateFormat = new SimpleDateFormat(dateFormat);
    }

    public void setProjectData() throws Exception
    {
        HashMap projectMap = new HashMap();

        JobSearchParameters searchParams = getSearchParams();
        ArrayList queriedJobs = new ArrayList(ServerProxy.getJobHandler()
                .getJobs(searchParams));

        HashSet jobs = new HashSet(queriedJobs);

        String companyId = CompanyThreadLocal.getInstance().getValue();

        BaseFluxCalendar calendar = ServerProxy.getCalendarManager()
                .findDefaultCalendar(companyId);
        EventScheduler eventScheduler = ServerProxy.getEventScheduler();

        // first iterate through the Jobs and group by Project/workflow because
        // Dell doesn't want to see actual Jobs
        Iterator jobIter = jobs.iterator();
        while (jobIter.hasNext())
        {
            Job j = (Job) jobIter.next();

            if (!reportData.wantsAllJobs
                    && !reportData.jobIdList.contains(Long.toString(j.getId())))
            {
                continue;
            }

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

                long jobId = j.getId();
                L10nProfile l10nProfile = j.getL10nProfile();

                HashMap localeMap = (HashMap) projectMap.get(Long
                        .toString(jobId));
                if (localeMap == null)
                {
                    localeMap = new HashMap();
                    projectMap.put(Long.toString(jobId), localeMap);
                }

                ProjectWorkflowData data = (ProjectWorkflowData) localeMap
                        .get(targetLang);
                if (data == null)
                {
                    data = new ProjectWorkflowData();
                    localeMap.put(targetLang, data);

                    data.jobName = j.getJobName();

                    data.jobId = jobId;

                    data.targetLang = w.getTargetLocale().toString();

                    data.workflowName = l10nProfile.getWorkflowTemplateInfo(
                            w.getTargetLocale()).getName();

                    data.totalWordCount = w.getTotalWordCount();

                    data.creationDate = j.getCreateDate();

                    data.currentActivityName = getCurrentActivityName(w);

                    // For sla report issue
                    // There are lots of original workflows while applying the
                    // sla patch.
                    // And this workflows have not translation completed dates.
                    // So, it needs to calculate this dates.
                    w.updateTranslationCompletedDates();

                    data.estimatedTranslateCompletionDate = w
                            .getEstimatedTranslateCompletionDate();

                    if (data.estimatedTranslateCompletionDate != null)
                    {
                        // Get days and hours of leadtime
                        data.leadtime = getBusinessIntervals(data.creationDate,
                                data.estimatedTranslateCompletionDate,
                                calendar, eventScheduler,
                                calendar.getHoursPerDay());
                    }

                    data.actualTranslateCompletionDate = w
                            .getTranslationCompletedDate();

                    if (data.actualTranslateCompletionDate != null)
                    {
                        // Get days and hours of actualPerformance
                        data.actualPerformance = getBusinessIntervals(
                                data.creationDate,
                                data.actualTranslateCompletionDate, calendar,
                                eventScheduler, calendar.getHoursPerDay());
                    }
                }

                // now add or amend the data in the ProjectWorkflowData based on
                // this next workflow
                if (j.getCreateDate().before(data.creationDate))
                {
                    data.creationDate = j.getCreateDate();
                }

            }

        }

        reportData.projectMap = projectMap;
    }

    /*
     * ----------------------------------------------------------------------
     * Below are helper methods
     * ----------------------------------------------------------------------
     */

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

        sp.setJobState(reportData.statusList);

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

    private String getCurrentActivityName(Workflow w)
    {
        String result = null;

        Map activeTasks = null;
        try
        {
            activeTasks = ServerProxy.getWorkflowServer()
                    .getActiveTasksForWorkflow(w.getId());
        }
        catch (Exception e)
        {
            activeTasks = null;
            s_logger.error("Failed to get active tasks for workflow "
                    + w.getId() + " " + e.getMessage());
        }

        // for now we'll only have one active task
        Object[] tasks = (activeTasks == null) ? null : activeTasks.values()
                .toArray();

        if ((tasks == null) || (tasks.length <= 0))
        {
            String state = w.getState();

            if (state.equals(Workflow.LOCALIZED))
            {
                result = "Localized";
            }
            else if (state.equals(Workflow.EXPORTED))
            {
                result = "Exported";
            }
            else if (state.equals(Workflow.READY_TO_BE_DISPATCHED))
            {
                result = "Not Yet Dispatched";
            }
        }
        else
        {
            // assume just one active task for now
            Activity a = ((WorkflowTaskInstance) tasks[0]).getActivity();
            result = (a == null) ? null : a.getDisplayName();
        }

        if (result == null)
        {
            result = "Unknown";
        }

        return result;
    }

    private String getBusinessIntervals(Date startDate, Date endDate,
            BaseFluxCalendar calendar, EventScheduler eventScheduler,
            int workingHoursPerDay) throws Exception
    {
        long MILLIS_PER_MIN = DateHelper.milliseconds(0, 0, 1);
        long MILLIS_PER_HOUR = DateHelper.milliseconds(0, 1, 0);
        long MILLIS_PER_DAY = DateHelper.milliseconds(1, 0, 0);

        // Keep the result of business intervals.
        int intervalDays = -1;
        int intervalHours = -1;

        long durations = 0l;
        Date determineDate = null;

        int intervalDays_min = 0;
        int intervalDays_max = DateHelper.convertMillisecondsToDays(endDate
                .getTime() - startDate.getTime());
        int intervalDays_temp = 0;

        // Determine date at day-level.
        while (true)
        {
            intervalDays_temp = (intervalDays_max + intervalDays_min) / 2;
            durations = intervalDays_temp * MILLIS_PER_DAY;
            determineDate = eventScheduler.determineDate(startDate, calendar,
                    durations);

            if (intervalDays_max - intervalDays_min <= 1)
            {
                intervalDays = intervalDays_min;
                break;
            }

            if (endDate.compareTo(determineDate) > 0)
            {
                // endDate > determineDate
                intervalDays_min = intervalDays_temp;
            }
            else if (endDate.compareTo(determineDate) < 0)
            {
                // endDate < determineDate
                intervalDays_max = intervalDays_temp;
            }
            else
            {
                // endDate = determineDate
                intervalDays = intervalDays_temp;
                intervalHours = 0;
                break;
            }
        }

        // Determine date at hour-level.
        if (intervalHours == -1)
        {
            // Fix the date to working hours.
            startDate = eventScheduler.determineDate(startDate, calendar,
                    intervalDays * MILLIS_PER_DAY + MILLIS_PER_MIN);

            int intervalHours_max = workingHoursPerDay;
            int intervalHours_min = 0;
            int intervalHours_temp = 0;

            if (endDate.compareTo(startDate) <= 0)
            {
                // Outside working time.
                intervalHours = 0;
            }
            else if (endDate.compareTo(eventScheduler.determineDate(startDate,
                    calendar, workingHoursPerDay * MILLIS_PER_HOUR)) >= 0)
            {
                // Outside working time.
                intervalHours = workingHoursPerDay;
            }
            else
            {
                while (true)
                {
                    intervalHours_temp = (intervalHours_min + intervalHours_max) / 2;
                    determineDate = eventScheduler.determineDate(startDate,
                            calendar, intervalHours_temp * MILLIS_PER_HOUR);

                    if (endDate.compareTo(determineDate) > 0)
                    {
                        // endDate > determineDate
                        intervalHours_min = intervalHours_temp;
                    }
                    else if (endDate.compareTo(determineDate) < 0)
                    {
                        // endDate < determineDate
                        intervalHours_max = intervalHours_temp;
                    }
                    else
                    {
                        // endDate = determineDate
                        intervalHours = intervalHours_temp;
                        break;
                    }

                    if (intervalHours_max - intervalHours_min <= 1)
                    {
                        intervalHours = intervalHours_max;
                        break;
                    }
                } // end while (true)
            } // end if (endDate.compareTo(determineDate) > 0)
        } // end if (resultHours == null)

        if (intervalHours >= workingHoursPerDay)
        {
            intervalDays = intervalDays + (intervalHours / workingHoursPerDay);
            intervalHours = intervalHours % workingHoursPerDay;
        }

        return intervalDays + "d " + intervalHours + "h";
    }
}
