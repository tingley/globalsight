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
package com.globalsight.reports.handler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.workflow.WfTaskInfo;
import com.globalsight.everest.workflow.WorkflowConstants;
import com.globalsight.everest.workflow.WorkflowTaskInstance;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.reports.Constants;
import com.globalsight.reports.datawrap.TaskDurationReportDataWrap;
import com.globalsight.reports.util.ReportHandlerFactory;
import com.globalsight.reports.util.ReportsPackage;
import com.globalsight.util.SortUtil;

public class TaskDurationReportHandler extends BasicReportHandler
{
    private static final String MY_MESSAGES = BUNDLE_LOCATION
            + "taskDurationReport";
    private static final String NO_CONTENT_STRING = "&nbsp;";

    private static final int NUM_ITEMS_DISPLAYED = 20;

    private ResourceBundle m_bundle = null;
    private TaskDurationReportDataWrap reportDataWrap = null;
    private List<Project> projectList;

    public TaskDurationReportHandler()
    {
        // just a empty constructor.
    }

    /**
     * Initializes the report and sets all the required parameters.
     */
    public void init()
    {
        try
        {
            super.init(); // get the common parameters
            m_bundle = ResourceBundle.getBundle(MY_MESSAGES, theUiLocale);
            this.reportKey = Constants.TASK_DURATION_REPORT_KEY;
        }
        catch (Exception e)
        {
            ReportsPackage.logError(e);
        }
    }

    public void invokeHandler(HttpServletRequest req, HttpServletResponse res,
            ServletContext p_context) throws Exception
    {
        // just do this at the first time
        super.invokeHandler(req, res, p_context);

        String act = (String) req.getParameter(Constants.REPORT_ACT);
        s_category
                .debug("Perform taskDurationReportHandler.invokeHandler with action "
                        + act);

        if (Constants.REPORT_ACT_CREATE.equalsIgnoreCase(act))
        {
            cleanSession(theSession);
            createReport(req);
        }
        else if (Constants.REPORT_ACT_TURNPAGE.equalsIgnoreCase(act))
        {
            reportDataWrap = (TaskDurationReportDataWrap) getSessionAttribute(
                    theSession, this.reportKey + Constants.REPORT_DATA_WRAP);
        }

        setTableNavigation(req, theSession, NUM_ITEMS_DISPLAYED,
                Constants.TASK_DURATION_REPORT_CURRENT_PAGE_LIST,
                this.reportKey, reportDataWrap);
        dispatcherForward(
                ReportHandlerFactory.getTargetUrl(reportKey
                        + Constants.REPORT_ACT_CREATE), req, res, p_context);
    }

    /**
     * Creates the actual report and fills it with data and messages. Also
     * determines the grouping styles. <br>
     * 
     * @param HttpServletRequest
     */
    public void createReport(HttpServletRequest req)
    {
        try
        {
            // define a kind of data class like WorkflowStatusReportDataWrap to
            // store data.
            reportDataWrap = new TaskDurationReportDataWrap();
            bindMessages();
            bindData(req);
            setSessionAttribute(theSession, this.reportKey
                    + Constants.REPORT_DATA_WRAP, reportDataWrap);
        }
        catch (Exception e)
        {
            ReportsPackage.logError(e);
        }
    }

    public void bindMessages()
    {
        reportDataWrap.setReportTitle(ReportsPackage.getMessage(m_bundle,
                Constants.REPORT_TITLE));
        reportDataWrap.setDescription(ReportsPackage.getMessage(m_bundle,
                Constants.DESCRIPTION));
        setCommonMessages(reportDataWrap);
    }

    /**
     * Gets the data from the DB and binds it to tables <br>
     * 
     * @param HttpServletRequest
     * @throws Exception
     */
    public void bindData(HttpServletRequest req) throws Exception
    {
        executeQuery(req);
    }

    public void readResultSet(List<?> jobs) throws Exception
    {
        ArrayList<String> fieldnameList = new ArrayList<String>();

        fieldnameList.add("Job Name");
        fieldnameList.add("Source_Locale");
        fieldnameList.add("Target_Locale");
        fieldnameList.add("Activity Name");
        fieldnameList.add("Accepter");
        fieldnameList.add("Duration");

        ArrayList<ArrayList<String>> allRowsDataList = new ArrayList<ArrayList<String>>();

        int rowCount = 0;
        for (Object o : jobs)
        {
            rowCount++;

            Job job = (Job) o;

            for (Workflow workflow : job.getWorkflows())
            {
                Hashtable<?, ?> tasks = workflow.getTasks();

                for (Object oTask : tasks.values())
                {
                    Task task = (Task) oTask;

                    ArrayList<String> singleRowDataList = new ArrayList<String>();
                    singleRowDataList.add(job.getJobName());
                    singleRowDataList.add(job.getSourceLocale().toString());
                    singleRowDataList
                            .add(workflow.getTargetLocale().toString());
                    singleRowDataList.add(task.getTaskDisplayName());
                    singleRowDataList.add(UserUtil.getUserNameById(task
                            .getAcceptor()));
                    singleRowDataList.add(task.getDurationString());
                    allRowsDataList.add(singleRowDataList);
                }
            }
        }

        // sort by job name, source locale, activity name
        SortUtil.sort(allRowsDataList, new Comparator<ArrayList<String>>()
        {
            public int compare(ArrayList<String> row1, ArrayList<String> row2)
            {
                if ((row1.get(0)).compareTo(row2.get(0)) != 0)
                {
                    return (row1.get(0)).compareTo(row2.get(0));
                }
                if ((row1.get(1)).compareTo(row2.get(1)) != 0)
                {
                    return (row1.get(1)).compareTo(row2.get(1));
                }
                if ((row1.get(2)).compareTo(row2.get(2)) != 0)
                {
                    return (row1.get(2)).compareTo(row2.get(2));
                }
                return (row1.get(3)).compareTo(row2.get(3));
            }
        });

        String lastJobName = null;
        for (ArrayList<String> row : allRowsDataList)
        {
            if (row.get(0) == lastJobName)
            {
                row.set(0, NO_CONTENT_STRING);
            }
            else
            {
                lastJobName = row.get(0);
            }
        }

        reportDataWrap.setTableHeadList(fieldnameList);
        reportDataWrap.setDataList(allRowsDataList);
    }

    private List<Long> findDefaultTaskIds(long p_wfId) throws Exception
    {
        List<Long> defaultTaskIds = new ArrayList<Long>();

        List<?> defaultTasks = ServerProxy.getWorkflowServer()
                .timeDurationsInDefaultPath(null, p_wfId, -1);
        for (Object o : defaultTasks)
        {
            WfTaskInfo wftaskinfo = (WfTaskInfo) o;
            defaultTaskIds.add(wftaskinfo.getId());
        }

        return defaultTaskIds;
    }

    private void executeQuery(HttpServletRequest req) throws Exception
    {
        StringBuilder hql = new StringBuilder();
        hql.append(" select distinct j ")
                .append(" from JobImpl j, RequestImpl r, ")
                .append(" BasicL10nProfile l, WorkflowImpl w, TaskImpl t, ")
                .append(" GlobalSightLocale l1, GlobalSightLocale l2 ")
                .append(" where j.id = w.job.id ")
                .append(" and j.id = r.job.id ")
                .append(" and r.l10nProfile.id = l.id ")
                .append(" and w.id = t.workflow.id ")
                .append(" and w.targetLocale.id = l1.id ")
                .append(" and l.sourceLocale.id = l2.id ")
                .append(" and j.createDate >= :createDate ")
                .append(" and j.state in ('EXPORTED', 'LOCALIZED') ");

        Map<String, Object> params = new HashMap<String, Object>();

        // last 30 days to now
        Calendar now = Calendar.getInstance();
        now.set(Calendar.HOUR_OF_DAY, 0);
        now.set(Calendar.MINUTE, 0);
        now.set(Calendar.SECOND, 0);
        now.set(Calendar.MILLISECOND, 0);
        now.add(Calendar.DATE, -30);
        params.put("createDate", now.getTime());

        String currentId = CompanyThreadLocal.getInstance().getValue();
        String userId = (String) req.getSession(false).getAttribute(WebAppConstants.USER_NAME);
        if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
        {
            hql.append(" and j.companyId = :companyId ");
            params.put("companyId", Long.parseLong(currentId));
        }

        List<?> jobs = HibernateUtil.search(hql.toString(), params);
        projectList = ServerProxy.getProjectHandler().getProjectsByUser(userId);
        if (jobs != null && !jobs.isEmpty())
        {
            filterReportJobInfoByProject(jobs);
        }
        
        readResultSet(jobs);
    }

	private void filterReportJobInfoByProject(List<?> jobs) {
		Set<String> projectIds = getProjectIdSet();
		for (Iterator<?> it = jobs.iterator(); it.hasNext();) {
			Job info = (Job) it.next();
			if (!projectIds.contains(String.valueOf(info.getProjectId()))) {
				it.remove();
			}
		}
	}
	
    private Set<String> getProjectIdSet()
    {
        Set<String> projectIds = new HashSet<String>();
        if (projectList != null && projectList.size() > 0)
        {
            for (Project pro : projectList)
            {
                projectIds.add(String.valueOf(pro.getId()));
            }
        }
        return projectIds;
    }

	class InnerTaskComparator implements Comparator<WorkflowTaskInstance>
    {
        private List<Long> m_defaultTaskIds = null;
        private HashMap<Long, Object[]> m_completedActivities = null;

        public InnerTaskComparator(List<Long> p_defaultTaskIds,
                HashMap<Long, Object[]> p_completedActivities)
        {
            m_defaultTaskIds = p_defaultTaskIds;
            m_completedActivities = p_completedActivities;
        }

        public int compare(WorkflowTaskInstance a_wfTask,
                WorkflowTaskInstance b_wfTask)
        {
            Long a_taskId = new Long(a_wfTask.getTaskId());
            Long b_taskId = new Long(b_wfTask.getTaskId());
            // compare only activities
            boolean a_isActivity = (a_wfTask.getType() == WorkflowConstants.ACTIVITY);
            boolean b_isActivity = (b_wfTask.getType() == WorkflowConstants.ACTIVITY);
            if (a_isActivity && !b_isActivity)
            {
                return 1;
            }
            else if (!a_isActivity && b_isActivity)
            {
                return -1;
            }
            else if (!a_isActivity && !b_isActivity)
            {
                return a_taskId.compareTo(b_taskId);
            }

            // see if one is completed
            boolean a_isCompleted = m_completedActivities.containsKey(a_taskId);
            boolean b_isCompleted = m_completedActivities.containsKey(b_taskId);
            if (a_isCompleted && !b_isCompleted)
            {
                return -1;
            }
            else if (!a_isCompleted && b_isCompleted)
            {
                return 1;
            }

            // now see if one of them is in the default path or not
            boolean a_isInDefault = m_defaultTaskIds.contains(a_taskId);
            boolean b_isInDefault = m_defaultTaskIds.contains(b_taskId);
            if (a_isInDefault && !b_isInDefault)
            {
                return -1;
            }
            else if (!a_isInDefault && b_isInDefault)
            {
                return 1;
            }
            else if (a_isInDefault && b_isInDefault)
            {
                // they're both in default path, so find whichever comes first
                int a_pos = m_defaultTaskIds.indexOf(a_taskId);
                int b_pos = m_defaultTaskIds.indexOf(b_taskId);
                return a_pos - b_pos;
            }
            else
            {
                // neither is in default path, so just compare task IDs
                return a_taskId.compareTo(b_taskId);
            }
        }
    }

}
