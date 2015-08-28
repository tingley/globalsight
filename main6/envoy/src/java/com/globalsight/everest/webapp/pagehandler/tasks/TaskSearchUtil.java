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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.foundation.SearchCriteriaParameters;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.taskmanager.TaskException;
import com.globalsight.everest.taskmanager.TaskImpl;
import com.globalsight.everest.taskmanager.TaskSearchParameters;
import com.globalsight.everest.workflow.WorkflowConstants;
import com.globalsight.everest.workflow.WorkflowTaskInstance;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.StringUtil;

/**
 * TaskSearchUtil is used to improve performance of searching activity.
 */
public class TaskSearchUtil
{
    private static Logger logger = Logger.getLogger(TaskSearchUtil.class);
    private static String IS_REJECT_SQL = null;
    static
    {
        StringBuffer sb = new StringBuffer();

        sb.append("SELECT COUNT(*) FROM JBPM_TASKINSTANCE ti ");
        sb.append("INNER JOIN JBPM_TASK jt ON jt.ID_ = ti.TASK_ ");
        sb.append("INNER JOIN JBPM_TASKACTORPOOL tap ");
        sb.append("ON ti.ID_ = tap.TASKINSTANCE_ ");
        sb.append("INNER JOIN JBPM_POOLEDACTOR pa ");
        sb.append("ON tap.POOLEDACTOR_ = pa.ID_ ");
        sb.append("INNER JOIN JBPM_GS_VARIABLE vi ");
        sb.append("ON ti.ID_ = vi.TASKINSTANCE_ID ");
        sb.append("WHERE jt.TASKNODE_ = :id ");
        sb.append("AND vi.NAME = 'isRejected' ");
        sb.append("AND vi.VALUE != pa.actorid_ ");
        sb.append("AND vi.CATEGORY = 'reject' ");
        sb.append("AND pa.ACTORID_ != :actorId ");

        IS_REJECT_SQL = sb.toString();
    }

    private static String IS_REJECT_REASSIGN_SQL = null;
    static
    {
        StringBuffer sb = new StringBuffer();
        sb.append("select count(*) from JBPM_TASKINSTANCE ti ");
        sb.append("join JBPM_TASKACTORPOOL tap ");
        sb.append("on ti.ID_ = tap.TASKINSTANCE_ ");
        sb.append("INNER JOIN JBPM_TASK jt ON jt.ID_ = ti.TASK_ ");
        sb.append("inner join JBPM_POOLEDACTOR pa ");
        sb.append("on tap.POOLEDACTOR_ = pa.ID_ ");
        sb.append("inner join JBPM_GS_VARIABLE vi ");
        sb.append("on ti.ID_ = vi.TASKINSTANCE_ID ");
        sb.append("where jt.TASKNODE_ = :id ");
        sb.append("and pa.ACTORID_ = :actorId ");
        sb.append("and vi.NAME = 'isRejected' ");
        sb.append("and vi.CATEGORY = 'reject' ");

        IS_REJECT_REASSIGN_SQL = sb.toString();
    }

    private static String SELECT = null;
    static
    {
        StringBuffer sb = new StringBuffer();

        sb.append("select distinct t.TASK_ID, j.ID, j.NAME, ");
        sb.append("w.TOTAL_WORD_COUNT, t.ESTIMATED_COMPLETION_DATE, ");
        sb.append("w.IFLOW_INSTANCE_ID, w.TARGET_LOCALE_ID, ");
        sb.append("w.PRIORITY, t.ACCEPTED_DATE, t.COMPLETED_DATE, ");
        sb.append("t.ESTIMATED_ACCEPTANCE_DATE, j.SOURCE_LOCALE_ID, t.TASK_TYPE, t.COMPANY_ID ");
        sb.append("from JBPM_TASKINSTANCE ti ");
        sb.append("inner join JBPM_TASK jt on jt.ID_ = ti.TASK_ ");
        sb.append("inner join TASK_INFO t on t.TASK_ID = jt.TASKNODE_ ");
        sb.append("inner join WORKFLOW w on t.WORKFLOW_ID = w.IFLOW_INSTANCE_ID ");
        sb.append("inner join JOB j on j.ID = w.JOB_ID ");
        sb.append("inner join JBPM_TASKACTORPOOL tap on ti.ID_ = tap.TASKINSTANCE_ ");
        sb.append("inner join JBPM_POOLEDACTOR pa on tap.POOLEDACTOR_ = pa.ID_ ");
        sb.append("left join JBPM_GS_VARIABLE vi on ti.ID_ = vi.TASKINSTANCE_ID ");
        sb.append("and vi.NAME = 'isRejected' and vi.CATEGORY = 'reject' ");
        sb.append("where w.state not in ('CANCELLED','ARCHIVED') and ");
        sb.append("j.state not in ('ADDING_FILES','DELETING_FILES', 'BATCH_RESERVED') and (");

        SELECT = sb.toString();
    }

    /**
     * Sets all assignees for the specified task.
     * 
     * @param t
     *            the specified task that need to set all assignees
     */
    public static void setAllAssignees(TaskImpl t)
    {
        StringBuffer sb2 = new StringBuffer();
        sb2.append("select distinct(ti.actorid_) from JBPM_TASKINSTANCE ti ");
        sb2.append("inner join JBPM_TASK jt ON jt.ID_ = ti.TASK_ ");
        sb2.append("where jt.TASKNODE_ = :id and ti.actorid_ is not null ");

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", t.getId());
        List result = HibernateUtil.searchWithSql(sb2.toString(), params);
        if (result.size() == 0 || result.get(0) == null)
        {
            StringBuffer sb = new StringBuffer();

            sb.append("select distinct(p.actorid_) from jbpm_pooledactor p ");
            sb.append("inner join jbpm_taskactorpool a on a.pooledactor_ = p.id_ ");
            sb.append("inner join JBPM_TASKINSTANCE ti on a.TASKINSTANCE_ = ti.id_ ");
            sb.append("inner join JBPM_TASK jt ON jt.ID_ = ti.TASK_ ");
            sb.append("where jt.TASKNODE_ = :id ");

            String sql = sb.toString();
            params = new HashMap<String, Object>();
            params.put("id", t.getId());
            result = HibernateUtil.searchWithSql(sql, params);
        }

        WorkflowTaskInstance w = new WorkflowTaskInstance("", 0);
        w.getAllAssignees().addAll(result);
        t.setWorkflowTask(w);
    }

    /**
     * Checks the user is project manager or not.
     * 
     * @param p_user
     *            The user to check
     * @return true if the user i project manager or false if the user is not
     *         project manager.
     */
    public static boolean isProjectManager(User p_user)
    {
        boolean isProjectManager = false;
        try
        {
            // get the projects whose manager is this user.
            Collection projects = ServerProxy.getProjectHandler()
                    .getProjectsByProjectManager(p_user);
            if (projects != null && !projects.isEmpty())
            {
                isProjectManager = true;
            }
        }
        catch (Exception e)
        {
            throw new TaskException(e);
        }
        return isProjectManager;
    }

    private static String dealState(int state, boolean isPm)
    {
        StringBuffer sql = new StringBuffer();
        if (state != WorkflowConstants.TASK_DECLINED
                && state != WorkflowConstants.TASK_ALL_STATES)
        {
            if (isPm)
            {
                sql.append("( pa.ACTORID_ = :actorId or ti.DESCRIPTION_ = :actorId ) ");
            }
            else
            {
                sql.append("( pa.ACTORID_ = :actorId ) ");
            }
        }

        switch (state)
        {
            case WorkflowConstants.TASK_ALL_STATES:
                if (isPm)
                {
                    sql.append("( pa.ACTORID_ = :actorId or ti.DESCRIPTION_ = :actorId ");
                }
                else
                {
                    sql.append("( pa.ACTORID_ = :actorId ");
                }

                sql.append("or vi.VALUE = :actorId ) ");

                if (isPm)
                {
                    sql.append("and (ti.ACTORID_ = :actorId or ti.ACTORID_ is null or ti.DESCRIPTION_ = :actorId) ");
                }
                else
                {
                    sql.append("and (ti.ACTORID_ = :actorId or ti.ACTORID_ is null) ");
                }

                break;
            case WorkflowConstants.TASK_ACTIVE:
                sql.append("and ti.START_ is null ");
                if (isPm)
                {
                    sql.append("and (ti.id_ not in (select distinct ti.id_ ");
                    sql.append("from JBPM_TASKINSTANCE ti inner join JBPM_TASKACTORPOOL tap ");
                    sql.append("on ti.ID_ = tap.TASKINSTANCE_  inner join JBPM_POOLEDACTOR pa ");
                    sql.append("on tap.POOLEDACTOR_ = pa.ID_  inner join JBPM_GS_VARIABLE vi ");
                    sql.append("on ti.ID_ = vi.TASKINSTANCE_ID  and vi.NAME = 'isRejected' ");
                    sql.append("and vi.VALUE != pa.actorid_  and vi.CATEGORY = 'reject'");
                    sql.append("and pa.ACTORID_ != :actorId ))");
                }
                break;

            case WorkflowConstants.TASK_ACCEPTED:
                if (isPm)
                {
                    sql.append(" and (ti.ACTORID_ = :actorId or ti.DESCRIPTION_ = :actorId) ");
                }
                else
                {
                    sql.append(" and ti.ACTORID_ = :actorId ");
                }
                sql.append("and ti.START_ is not null and ti.END_ is null ");
                break;
            case WorkflowConstants.TASK_GSEDITION_IN_PROGESS:
                if (isPm)
                {
                    sql.append(" and (ti.ACTORID_ = :actorId or ti.DESCRIPTION_ = :actorId) ");
                }
                else
                {
                    sql.append(" and ti.ACTORID_ = :actorId ");
                }
                sql.append(" and ti.ACTORID_ = :actorId ");
                sql.append(" and t.STATE in ('"
                        + TaskImpl.STATE_REDEAY_DISPATCH_GSEDTION_STR + "','"
                        + TaskImpl.STATE_DISPATCHED_TO_TRANSLATION_STR + "','"
                        + TaskImpl.STATE_IN_TRANSLATION_STR + "','"
                        + TaskImpl.STATE_TRANSLATION_COMPLETED_STR + "') ");
                break;
            case WorkflowConstants.TASK_DECLINED:
                sql.append(" vi.NAME = 'isRejected' ");
                sql.append("and vi.CATEGORY = 'reject' ");
                if (!isPm)
                {
                    sql.append("and vi.VALUE = :actorId and ti.ID_ not in (select ti.ID_ from JBPM_TASKINSTANCE ti join JBPM_TASKACTORPOOL tap on ti.ID_ = tap.TASKINSTANCE_ inner join JBPM_POOLEDACTOR pa on tap.POOLEDACTOR_ = pa.ID_ inner join JBPM_GS_VARIABLE vi on ti.ID_ = vi.TASKINSTANCE_ID and vi.NAME = 'isRejected' and vi.VALUE = :actorId and vi.CATEGORY = 'reject' where pa.ACTORID_ = :actorId )");
                }
                else
                {
                    sql.append("and vi.VALUE != pa.actorid_ and pa.ACTORID_ != :actorId ");
                }
                break;
            case WorkflowConstants.TASK_COMPLETED:
                sql.append("and ti.END_ is not null ");
                break;
        }

        sql.append(")");
        return sql.toString();
    }

    private static String getSearchSql(User user, TaskSearchParameters sp,
            Map<String, Object> params)
    {
        Map parameters = sp.getParameters();

        boolean isPm = isProjectManager(user);
        StringBuffer sql = new StringBuffer(SELECT + "(");
        int state = (Integer) parameters.get(TaskSearchParameters.STATE);
        sql.append(dealState(state, isPm));
        params.put("actorId", user.getUserId());

        String companyName = (String) parameters
                .get(TaskSearchParameters.COMPANY_NAME);
        if (companyName != null)
        {
            long companyId = CompanyWrapper.getCompanyByName(companyName)
                    .getId();
            sql.append(" and t.COMPANY_ID = :companyId");
            params.put("companyId", new Long(companyId));
        }
        else
        {
            if (!CompanyThreadLocal.getInstance().fromSuperCompany())
            {
                sql.append(" and t.COMPANY_ID = :companyId");
                params.put("companyId", new Long(CompanyThreadLocal
                        .getInstance().getValue()));
            }
        }

        GlobalSightLocale srcLocale = (GlobalSightLocale) parameters
                .get(TaskSearchParameters.SOURCE_LOCALE);
        if (srcLocale != null)
        {
            int n = sql.indexOf("inner join JBPM_TASKACTORPOOL");
            sql.insert(
                    n,
                    "inner join REQUEST r on r.JOB_ID = j.id inner join L10N_PROFILE l on r.L10N_PROFILE_ID = l.id ");
            sql.append(" and l.SOURCE_LOCALE_ID = :lId ");
            params.put("lId", srcLocale.getIdAsLong());
        }

        GlobalSightLocale trgLocale = (GlobalSightLocale) parameters
                .get(TaskSearchParameters.TARGET_LOCALE);
        if (trgLocale != null)
        {
            sql.append(" and w.TARGET_LOCALE_ID = :trgLocale ");
            params.put("trgLocale", trgLocale.getIdAsLong());
        }

        Integer accAmount = (Integer) parameters
                .get(TaskSearchParameters.ACCEPTANCE_START);
        if (accAmount != null)
        {
            String condition = (String) parameters.get(new Integer(
                    TaskSearchParameters.ACCEPTANCE_START_CONDITION));

            Calendar now = Calendar.getInstance();
            int negativeAmount = (int) 0 - accAmount.intValue();
            int positiveAmount = accAmount.intValue();
            if (condition.equals(SearchCriteriaParameters.MONTHS_AGO))
            {
                now.add(Calendar.MONTH, negativeAmount);
            }
            else if (condition.equals(SearchCriteriaParameters.WEEKS_AGO))
            {
                now.add(Calendar.WEEK_OF_YEAR, negativeAmount);
            }
            else if (condition.equals(SearchCriteriaParameters.DAYS_AGO))
            {
                now.add(Calendar.DATE, negativeAmount);
            }
            else if (condition
                    .equals(SearchCriteriaParameters.MONTHS_FROM_NOW))
            {
                now.add(Calendar.MONTH, positiveAmount);
            }
            else if (condition
                    .equals(SearchCriteriaParameters.WEEKS_FROM_NOW))
            {
                now.add(Calendar.WEEK_OF_YEAR, positiveAmount);
            }
            else if (condition
                    .equals(SearchCriteriaParameters.DAYS_FROM_NOW))
            {
                now.add(Calendar.DATE, positiveAmount);
            }
            else if (condition
                    .equals(SearchCriteriaParameters.HOURS_FROM_NOW))
            {
                now.add(Calendar.HOUR_OF_DAY, positiveAmount);
            }
            else
            {
                // assume SearchCriteriaParameters.HOURS_AGO
                now.add(Calendar.HOUR_OF_DAY, negativeAmount);
            }

            sql.append(" and t.ACCEPTED_DATE >= :acceptedStartDate ");
            params.put("acceptedStartDate", now.getTime());
        }

        Integer endAmount = (Integer) parameters
                .get(TaskSearchParameters.ACCEPTANCE_END);
        if (endAmount != null)
        {
            String condition = (String) parameters.get(new Integer(
                    TaskSearchParameters.ACCEPTANCE_END_CONDITION));

            Calendar now = Calendar.getInstance();
            int negativeAmount = (int) 0 - endAmount.intValue();
            int positiveAmount = endAmount.intValue();
            if (condition.equals(SearchCriteriaParameters.MONTHS_AGO))
            {
                now.add(Calendar.MONTH, negativeAmount);
            }
            else if (condition.equals(SearchCriteriaParameters.WEEKS_AGO))
            {
                now.add(Calendar.WEEK_OF_YEAR, negativeAmount);
            }
            else if (condition.equals(SearchCriteriaParameters.DAYS_AGO))
            {
                now.add(Calendar.DATE, negativeAmount);
            }
            else if (condition
                    .equals(SearchCriteriaParameters.MONTHS_FROM_NOW))
            {
                now.add(Calendar.MONTH, positiveAmount);
            }
            else if (condition
                    .equals(SearchCriteriaParameters.WEEKS_FROM_NOW))
            {
                now.add(Calendar.WEEK_OF_YEAR, positiveAmount);
            }
            else if (condition
                    .equals(SearchCriteriaParameters.DAYS_FROM_NOW))
            {
                now.add(Calendar.DATE, positiveAmount);
            }
            else if (condition
                    .equals(SearchCriteriaParameters.HOURS_FROM_NOW))
            {
                now.add(Calendar.HOUR_OF_DAY, positiveAmount);
            }
            else
            {
                // assume SearchCriteriaParameters.HOURS_AGO
                now.add(Calendar.HOUR_OF_DAY, negativeAmount);
            }

            sql.append(" and t.ACCEPTED_DATE <= :acceptedEndDate ");
            params.put("acceptedEndDate", now.getTime());
        }

        Integer estAmount = (Integer) parameters
                .get(TaskSearchParameters.EST_COMPLETION_START);
        if (estAmount != null)
        {
            String condition = (String) parameters.get(new Integer(
                    TaskSearchParameters.EST_COMPLETION_START_CONDITION));

            Calendar now = Calendar.getInstance();
            if (!condition.equals(SearchCriteriaParameters.NOW))
            {

                int negativeAmount = (int) 0 - estAmount.intValue();
                int positiveAmount = estAmount.intValue();
                if (condition.equals(SearchCriteriaParameters.MONTHS_AGO))
                {
                    now.add(Calendar.MONTH, negativeAmount);
                }
                else if (condition.equals(SearchCriteriaParameters.WEEKS_AGO))
                {
                    now.add(Calendar.WEEK_OF_YEAR, negativeAmount);
                }
                else if (condition.equals(SearchCriteriaParameters.DAYS_AGO))
                {
                    now.add(Calendar.DATE, negativeAmount);
                }
                else if (condition
                        .equals(SearchCriteriaParameters.MONTHS_FROM_NOW))
                {
                    now.add(Calendar.MONTH, positiveAmount);
                }
                else if (condition
                        .equals(SearchCriteriaParameters.WEEKS_FROM_NOW))
                {
                    now.add(Calendar.WEEK_OF_YEAR, positiveAmount);
                }
                else if (condition
                        .equals(SearchCriteriaParameters.DAYS_FROM_NOW))
                {
                    now.add(Calendar.DATE, positiveAmount);
                }
                else if (condition
                        .equals(SearchCriteriaParameters.HOURS_FROM_NOW))
                {
                    now.add(Calendar.HOUR_OF_DAY, positiveAmount);
                }
                else
                {
                    // assume SearchCriteriaParameters.HOURS_AGO
                    now.add(Calendar.HOUR_OF_DAY, negativeAmount);
                }
            }

            sql.append(" and t.ESTIMATED_COMPLETION_DATE >= :estimatedCompletionStartDate ");
            params.put("estimatedCompletionStartDate", now.getTime());
        }

        Integer estEndAmount = (Integer) parameters
                .get(TaskSearchParameters.EST_COMPLETION_END);
        if (estEndAmount != null)
        {
            String condition = (String) parameters.get(new Integer(
                    TaskSearchParameters.EST_COMPLETION_END_CONDITION));

            Calendar now = Calendar.getInstance();
            if (!condition.equals(SearchCriteriaParameters.NOW))
            {
                int negativeAmount = (int) 0 - estEndAmount.intValue();
                int positiveAmount = estEndAmount.intValue();
                if (condition.equals(SearchCriteriaParameters.MONTHS_AGO))
                {
                    now.add(Calendar.MONTH, negativeAmount);
                }
                else if (condition.equals(SearchCriteriaParameters.WEEKS_AGO))
                {
                    now.add(Calendar.WEEK_OF_YEAR, negativeAmount);
                }
                else if (condition.equals(SearchCriteriaParameters.DAYS_AGO))
                {
                    now.add(Calendar.DATE, negativeAmount);
                }
                else if (condition
                        .equals(SearchCriteriaParameters.MONTHS_FROM_NOW))
                {
                    now.add(Calendar.MONTH, positiveAmount);
                }
                else if (condition
                        .equals(SearchCriteriaParameters.WEEKS_FROM_NOW))
                {
                    now.add(Calendar.WEEK_OF_YEAR, positiveAmount);
                }
                else if (condition
                        .equals(SearchCriteriaParameters.DAYS_FROM_NOW))
                {
                    now.add(Calendar.DATE, positiveAmount);
                }
                else if (condition
                        .equals(SearchCriteriaParameters.HOURS_FROM_NOW))
                {
                    now.add(Calendar.HOUR_OF_DAY, positiveAmount);
                }
                else
                {
                    // assume SearchCriteriaParameters.HOURS_AGO
                    now.add(Calendar.HOUR_OF_DAY, negativeAmount);
                }
            }

            sql.append(" and t.ESTIMATED_COMPLETION_DATE <= :estimatedCompletionEndDate ");
            params.put("estimatedCompletionEndDate", now.getTime());
        }

        String jobName = (String) sp.getParameters().get(
                TaskSearchParameters.JOB_NAME);
        if (jobName != null)
        {
            String condition = (String) parameters.get(new Integer(
                    TaskSearchParameters.JOB_NAME_CONDITION));

            jobName = dealWithCondition(jobName, condition,
                    sp.isCaseSensitive());

            sql.append(" and j.NAME like :jobName ");
            params.put("jobName", jobName);
        }

        String jobId = (String) parameters.get(TaskSearchParameters.JOB_ID);
        if (jobId != null)
        {
            String condition = (String) parameters.get(new Integer(
                    TaskSearchParameters.JOB_ID_CONDITION));

            if (SearchCriteriaParameters.LESS_THAN.equals(condition))
            {
                sql.append(" and j.id < :jobId ");
            }
            else if (SearchCriteriaParameters.GREATER_THAN.equals(condition))
            {
                sql.append(" and j.id > :jobId ");
            }
            else
            {
                sql.append(" and j.id = :jobId ");
            }

            params.put("jobId", Long.valueOf(jobId));
        }

        String priority = (String) parameters
                .get(TaskSearchParameters.PRIORITY);
        if (priority != null)
        {
            if (priority.indexOf("*") >= 0)
            {
                priority = priority.replace('*', '%');
                sql.append(" and j.priority like :priority ");
            }
            else
            {
                sql.append(" and j.priority = :priority ");
            }

            params.put("priority", priority);
        }

        String name = (String) parameters
                .get(TaskSearchParameters.ACTIVITY_NAME);
        if (name != null)
        {
    		name = "%" + name + "%_%";
    		sql.append(" and t.name like :name ");
        	
            params.put("name", name);
        }
        
        String assigneesName = (String) parameters
        		.get(TaskSearchParameters.ASSIGNEES_NAME);
        if(assigneesName != null)
        {
        	assigneesName = "%" + assigneesName + "%";
        	sql.append(" and (ti.actorid_ like :assigneesName or (pa.actorid_ like :assigneesName and t.state = :avaiable)) ");
        	params.put("avaiable", TaskImpl.STATE_ACTIVE_STR);
        	params.put("assigneesName", assigneesName);
        }

        sql.append(") ");
        
        String sortColumn = (String) parameters.get(TaskSearchParameters.SORT_COLUMN);
        if (StringUtil.isNotEmpty(sortColumn)) {
            sql.append("ORDER BY ").append(getSortColumn(sortColumn));
            Boolean isAscSort = (Boolean) parameters.get(TaskSearchParameters.SORT_TYPE);
            if (isAscSort == null || isAscSort)
                sql.append(" ASC");
            else 
                sql.append(" DESC");
        }
        /**
        Integer rowStart = (Integer) parameters.get(TaskSearchParameters.ROW_START);
        if (rowStart != null) {
            Integer rowPerPage = (Integer) parameters.get(TaskSearchParameters.ROW_PER_PAGE);
            if (rowPerPage == null)
                rowPerPage = 20;
            
            sql.append(" LIMIT " + rowStart.intValue() + "," + rowPerPage.intValue());
        }

        logger.info("Executed SQL == " + sql.toString());
        */
        return sql.toString();
    }
    
    private static String getSortColumn(String column) {
        if ("jobId".equals(column))
            return "j.ID";
        else if ("jobName".equals(column))
            return "j.NAME";
        else if ("activityName".equals(column))
            return "t.NAME";
        else if ("wordCount".equals(column))
            return "w.TOTAL_WORD_COUNT";
        else if ("acceptedDate".equals(column))
            return "t.ACCEPTED_DATE";
        else if ("completedDate".equals(column))
            return "t.COMPLETED_DATE";
        else if ("ecdDate".equals(column))
            return "t.ESTIMATED_COMPLETION_DATE";
        else if ("ecaDate".equals(column))
            return "t.ESTIMATED_ACCEPTANCE_DATE";
        else if ("priority".equals(column))
            return "w.PRIORITY";
        else if ("sourceLocale".equals(column))
            return "j.SOURCE_LOCALE_ID";
        else if ("targetLocale".equals(column))
            return "w.TARGET_LOCALE_ID";
        else if ("company".equals(column))
            return "t.COMPANY_ID";
        else if ("sourceWordCount".equals(column))
            return "w.TOTAL_WORD_COUNT";
        else if ("priority".equals(column))
            return "w.PRIORITY";
        else if ("sourceLocale".equals(column))
            return "j.SOURCE_LOCALE_ID";
        else if ("targetLocale".equals(column))
            return "w.TARGET_LOCALE_ID";
        
        return "";
    }

    private static String dealWithCondition(String s, String condition,
            boolean isCaseSensitive)
    {
        if (!isCaseSensitive)
        {
            s = s.toLowerCase();
        }

        s = s.trim();

        if (SearchCriteriaParameters.BEGINS_WITH.equals(condition))
        {
            s += "%";
        }
        // select values greater than p_firstValue
        else if (SearchCriteriaParameters.ENDS_WITH.equals(condition))
        {
            s = "%" + s;
        }
        // select values between p_firstValue and p_secondValue
        else if (SearchCriteriaParameters.CONTAINS.equals(condition))
        {
            s = "%" + s + "%";

        }

        return s;
    }

    /**
     * Search tasks with search parameters.
     * 
     * @param user
     *            The user who search the tasks.
     * @param sp
     *            includes all search parameters
     * @return a list, includes all tasks that searched out.
     */
    public static List<TaskVo> search(User user, TaskSearchParameters sp)
    {
        Map<String, Object> params = new HashMap<String, Object>();
        String sql = getSearchSql(user, sp, params);
        //logger.info("Search SQL == " + sql);

        List result = HibernateUtil.searchWithSql(sql, params);
        String taskIdStr = "";
        int state = (Integer) sp.getParameters()
                .get(TaskSearchParameters.STATE);
        List<TaskVo> taskVoList = new ArrayList<TaskVo>();
        for (int i = 0; i < result.size(); i++)
        {
            Object[] contents = (Object[]) result.get(i);
            TaskVo taskVo = new TaskVo();

            // for issue : After click on the download button in available
            // activity list, the activity will not go to the In Progress list
            long taskId = Long.parseLong(contents[0].toString());
            taskIdStr += taskId +",";
            if (state == WorkflowConstants.TASK_ACTIVE)
            {
                // force to close session in order to get the latest task from
                // database
                HibernateUtil.closeSession();
                TaskImpl task = HibernateUtil.get(TaskImpl.class, taskId);
                if (TaskImpl.getStateAsInt(task.getStateAsString()) != 3)
                {
                    continue;
                }
            }
            else if (state == WorkflowConstants.TASK_ACCEPTED)
            {
                // force to close session in order to get the latest task from
                // database
                HibernateUtil.closeSession();
                TaskImpl task = HibernateUtil.get(TaskImpl.class, taskId);
                if (!Task.STATE_ACCEPTED_STR.equals(task.getStateAsString()))
                {
                    continue;
                }
            }

            taskVo.setTaskId(taskId);
            taskVo.setJobId(Long.parseLong(contents[1].toString()));
            taskVo.setJobName(contents[2].toString());
            taskVo.setWordCount(Integer.parseInt(contents[3].toString()));
            Timestamp time = null; 
            time = (Timestamp) contents[4];
            if (time != null)
                taskVo.setEstimatedCompletionDate(new Date(time.getTime()));

            taskVo.setWorkflowId(Long.parseLong(contents[5].toString()));
            taskVo.setTargetLocaleId(Long.parseLong(contents[6].toString()));
            taskVo.setPriority(Integer.parseInt(contents[7].toString()));
            time = (Timestamp) contents[8];
            if (time != null)
                taskVo.setAcceptedDate(new Date(time.getTime()));
            time = (Timestamp) contents[9];
            if (time != null)
                taskVo.setCompletedDate(new Date(time.getTime()));
            time = (Timestamp) contents[10];
            if (time != null)
                taskVo.setEstimatedAcceptanceDate(new Date(time.getTime()));
            taskVo.setSourceLocaleId(Long.parseLong(contents[11].toString()));
            taskVo.setTaskType(contents[12].toString());
            taskVo.setCompanyId(Long.parseLong(contents[13].toString()));
            taskVoList.add(taskVo);
        }

		Map<Long, Timestamp> workflowMap = new HashMap<Long, Timestamp>();
		if (taskIdStr.endsWith(","))
		{
			StringBuffer workflowSql = new StringBuffer();
			workflowSql
					.append("SELECT DISTINCT t.TASK_ID,w.ESTIMATED_COMPLETION_DATE ");
			workflowSql
					.append(" FROM task_info t ,workflow w WHERE t.WORKFLOW_ID = w.IFLOW_INSTANCE_ID AND w.IS_ESTI_CMPLTN_DATE_OVERRIDED = 'Y' ");
			workflowSql
					.append(" AND t.TASK_ID IN (")
					.append(taskIdStr.subSequence(0, taskIdStr.lastIndexOf(",")))
					.append(")");
			List workflowResult = HibernateUtil.searchWithSql(
					workflowSql.toString(), null);
			for (int i = 0; i < workflowResult.size(); i++)
			{
				Object[] contents = (Object[]) workflowResult.get(i);
				workflowMap.put(Long.parseLong(contents[0].toString()),
						(Timestamp) contents[1]);
			}
		}
		
		Set<Long> keySet = workflowMap.keySet();
		for (int i = 0; i < taskVoList.size(); i++)
		{
			if (keySet.contains(taskVoList.get(i).getTaskId()))
			{
				Timestamp value = workflowMap
						.get(taskVoList.get(i).getTaskId());
				taskVoList.get(i).setEstimatedAcceptanceDate(
						new Date(value.getTime()));
				taskVoList.get(i).setEstimatedCompletionDate(
						new Date(value.getTime()));
			}
		}
		
		return taskVoList;
    }

    /**
     * Check the user has rejected the task or not.
     * 
     * @param t
     *            The task to check
     * @param actorId
     *            The user to check
     * @return true if the user has rejected the task or false if the user is
     *         not rejected the task
     */
    public static boolean isReject(TaskImpl t, String actorId)
    {
        String sql = IS_REJECT_SQL;
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", t.getId());
        params.put("actorId", actorId);

        List<?> result = HibernateUtil.searchWithSql(sql, params);
        Number num = (Number) result.get(0);
        return num.longValue() > 0;
    }

    public static boolean isRejectedForReassign(TaskImpl t, String actorId)
    {
        String sql = IS_REJECT_REASSIGN_SQL;
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", t.getId());
        params.put("actorId", actorId);

        List<Number> result = (List<Number>) HibernateUtil.searchWithSql(sql,
                params);
        Number num = (Number) result.get(0);
        return num.longValue() > 0;
    }

    public static void setState(TaskImpl t, String actorId)
    {
        int state = WorkflowConstants.TASK_DECLINED;
        if (isReject(t, actorId) && !isRejectedForReassign(t, actorId))
        {
            state = WorkflowConstants.TASK_DECLINED;
        }
        else
        {
            String sql = "SELECT start_, end_ FROM JBPM_TASKINSTANCE ti "
                    + "INNER JOIN JBPM_TASK jt ON jt.ID_ = ti.TASK_ "
                    + "WHERE jt.TASKNODE_ = :id order by ti.ID_ desc limit 1";
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("id", t.getId());

            List result = HibernateUtil.searchWithSql(sql, params);
            if (result.size() > 0)
            {
                Object[] contents = (Object[]) result.get(0);
                if (contents[0] == null)
                {
                    state = WorkflowConstants.TASK_ACTIVE;
                }
                else if (contents[0] != null && contents[1] == null)
                {
                    state = WorkflowConstants.TASK_ACCEPTED;
                }
                else if (contents[1] != null)
                {
                    state = WorkflowConstants.TASK_COMPLETED;
                }
            }
        }

        t.setState(state);
    }
}
