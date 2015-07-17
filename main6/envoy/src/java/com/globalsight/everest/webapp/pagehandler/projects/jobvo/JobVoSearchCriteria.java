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
package com.globalsight.everest.webapp.pagehandler.projects.jobvo;

import java.util.ArrayList;
import java.util.Calendar;
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
import com.globalsight.everest.jobhandler.JobSearchParameters;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.webapp.pagehandler.administration.projects.ProjectHandlerHelper;
import com.globalsight.everest.workflowmanager.WorkflowImpl;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GlobalSightLocale;

/**
 * JobSearchCriteria is used to construct a job search expression based on user
 * input.
 */
public class JobVoSearchCriteria
{
    private static final Logger s_logger = Logger
            .getLogger(JobVoSearchCriteria.class);

    private StringBuffer sql = new StringBuffer();
    private boolean m_isCaseSensitive = false;
    private Map<String, Object> params = new HashMap<String, Object>();

    /**
     * Return the search result based on the specified search criteria object.
     * 
     * @param p_searchCriteriaParams
     *            - The search criteria object.
     */
    public List<?> search(SearchCriteriaParameters p_searchCriteriaParams)
    {
    	sql.append(JobVoSearcher.SELECT_PART);
    	sql.append("  FROM ");
    	sql.append("  JOB j ");
    	sql.append("  left outer join REQUEST r on j.ID = r.JOB_ID ");
    	sql.append("  left outer join L10N_PROFILE l on j.L10N_PROFILE_ID = l.ID ");
    	sql.append("  left outer join PROJECT p on l.PROJECT_ID = p.PROJECT_SEQ ");
    	sql.append("  left outer join WORKFLOW w on j.ID=w.JOB_ID ");
    	sql.append("  left outer join WORKFLOW_OWNER wo on w.IFLOW_INSTANCE_ID=wo.WORKFLOW_ID ");
    	sql.append("  WHERE 1=1 ");    	

        m_isCaseSensitive = p_searchCriteriaParams.isCaseSensitive();
        Map<?, ?> criteria = p_searchCriteriaParams.getParameters();

        // get the keys of the map
        Object[] keys = criteria.keySet().toArray();

        int mapSize = keys.length;

        // loop throught the parameters to create the sql statement.
        for (int i = 0; i < mapSize; i++)
        {
            switch (((Integer) (keys[i])).intValue())
            {
            // job name
                case JobSearchParameters.JOB_NAME:
                    jobName(keys[i], criteria);
                    break;

                // job id
                case JobSearchParameters.JOB_ID:
                    jobId(keys[i], criteria);
                    break;

                    // job id
                case JobSearchParameters.JOB_GROUP_ID:
                    jobGroupId(keys[i], criteria);
                    break;
                    
                // job state
                case JobSearchParameters.STATE:
                    jobState(keys[i], criteria);
                    break;

                // priority
                case JobSearchParameters.PRIORITY:
                    jobPriority(keys[i], criteria);
                    break;

                // project id
                case JobSearchParameters.PROJECT_ID:
                    jobProject(keys[i], criteria);
                    break;

                // Source locale
                case JobSearchParameters.SOURCE_LOCALE:
                    sourceLocaleExpression(keys[i], criteria);
                    break;

                // target locale
                case JobSearchParameters.TARGET_LOCALE:
                    targetLocaleExpression(keys[i], criteria);
                    break;

                // Job creation start date (x ago)
                case JobSearchParameters.CREATION_START:
                    creationStartExpression(keys[i], criteria);
                    break;

                // Job creation start date (date)
                case JobSearchParameters.CREATION_START_DATE:
                    creationStartDateExpression(keys[i], criteria);
                    break;

                // Job creation end date (x ago)
                case JobSearchParameters.CREATION_END:
                    creationEndExpression(keys[i], criteria);
                    break;

                // Job creation end date (date)
                case JobSearchParameters.CREATION_END_DATE:
                    creationEndDateExpression(keys[i], criteria);
                    break;

                // Job est completion start date
                case JobSearchParameters.EST_COMPLETION_START:
                    estCompletionStartExpression(keys[i], criteria);
                    break;

                // Job creation end date
                case JobSearchParameters.EST_COMPLETION_END:
                    estCompletionEndExpression(keys[i], criteria);
                    break;

                // User
                case JobSearchParameters.USER:
                    userExpression(keys[i], criteria);
                    break;

                // Job export start date
                case JobSearchParameters.EXPORT_DATE_START:
                    exportStartExpression(keys[i], criteria);
                    break;

                // Job export end date
                case JobSearchParameters.EXPORT_DATE_END:
                    exportEndExpression(keys[i], criteria);
                    break;

                default:
                    break;
            }
        }

        createCompanyExpression();
        HibernateUtil.closeSession();
        return HibernateUtil.searchWithSql(sql.toString(), params);
    }

    private void exportEndExpression(Object object, Map<?, ?> criteria)
    {
        Integer amount = (Integer) criteria.get(object);
        String condition = (String) criteria.get(new Integer(
                JobSearchParameters.EXPORT_DATE_END_OPTIONS));

        Calendar now = Calendar.getInstance();
        if (amount != null || !condition.equals(SearchCriteriaParameters.NOW))
        {
            int negativeAmount = (int) 0 - amount.intValue();
            int positiveAmount = amount.intValue();
            if (condition.equals(SearchCriteriaParameters.HOURS_AGO) == false
                    && condition
                            .equals(SearchCriteriaParameters.HOURS_FROM_NOW) == false)
            {
                // if it's not hours ago, then start counting back from
                // tonight just before midnight
                now.set(Calendar.HOUR_OF_DAY, 23);
                now.set(Calendar.MINUTE, 59);
                now.set(Calendar.SECOND, 59);
                now.set(Calendar.MILLISECOND, 999);
            }

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
            else if (condition.equals(SearchCriteriaParameters.MONTHS_FROM_NOW))
            {
                now.add(Calendar.MONTH, positiveAmount);
            }
            else if (condition.equals(SearchCriteriaParameters.WEEKS_FROM_NOW))
            {
                now.add(Calendar.WEEK_OF_YEAR, positiveAmount);
            }
            else if (condition.equals(SearchCriteriaParameters.DAYS_FROM_NOW))
            {
                now.add(Calendar.DATE, positiveAmount);
            }
            else if (condition.equals(SearchCriteriaParameters.HOURS_FROM_NOW))
            {
                now.add(Calendar.HOUR_OF_DAY, positiveAmount);
            }
            else
            {
                // assume SearchCriteriaParameters.HOURS_AGO
                now.add(Calendar.HOUR_OF_DAY, negativeAmount);
            }
        }

        sql.append(" and j.TIMESTAMP <= :exportEndDate ");
        params.put("exportEndDate", now.getTime());
    }

    private void exportStartExpression(Object object, Map<?, ?> criteria)
    {
        Integer amount = (Integer) criteria.get(object);
        String condition = (String) criteria.get(new Integer(
                JobSearchParameters.EXPORT_DATE_START_OPTIONS));

        Calendar now = Calendar.getInstance();
        if (amount != null || !condition.equals(SearchCriteriaParameters.NOW))
        {
            int negativeAmount = (int) 0 - amount.intValue();
            int positiveAmount = amount.intValue();

            if (condition.equals(SearchCriteriaParameters.HOURS_AGO) == false
                    && condition
                            .equals(SearchCriteriaParameters.HOURS_FROM_NOW) == false)
            {
                // if it's not hours ago, then start counting back from
                // this morning just past midnight
                now.set(Calendar.HOUR_OF_DAY, 0);
                now.set(Calendar.MINUTE, 0);
                now.set(Calendar.SECOND, 0);
                now.set(Calendar.MILLISECOND, 0);
            }

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
            else if (condition.equals(SearchCriteriaParameters.MONTHS_FROM_NOW))
            {
                now.add(Calendar.MONTH, positiveAmount);
            }
            else if (condition.equals(SearchCriteriaParameters.WEEKS_FROM_NOW))
            {
                now.add(Calendar.WEEK_OF_YEAR, positiveAmount);
            }
            else if (condition.equals(SearchCriteriaParameters.DAYS_FROM_NOW))
            {
                now.add(Calendar.DATE, positiveAmount);
            }
            else if (condition.equals(SearchCriteriaParameters.HOURS_FROM_NOW))
            {
                now.add(Calendar.HOUR_OF_DAY, positiveAmount);
            }
            else
            {
                // assume SearchCriteriaParameters.HOURS_AGO
                now.add(Calendar.HOUR_OF_DAY, negativeAmount);
            }
        }

        sql.append(" and j.TIMESTAMP >= :exportDate ");
        params.put("exportDate", now.getTime());
    }

    private void createCompanyExpression()
    {
        String currentId = CompanyThreadLocal.getInstance().getValue();
        if (currentId == null || currentId.trim().length() == 0
                || currentId.equals(CompanyWrapper.SUPER_COMPANY_ID))
        {
            return;
        }

        sql.append(" and j.COMPANY_ID = " + Long.parseLong(currentId.trim()));
    }

    /*
     * Prepare the search expression based on the job's name.
     */
    private void jobName(Object p_key, Map<?, ?> criteria)
    {
        String jobName = (String) criteria.get(p_key);

        String condition = (String) criteria.get(new Integer(
                JobSearchParameters.JOB_NAME_CONDITION));

        jobName = dealWithCondition(jobName, condition);
        sql.append(" and j.NAME like :jobName ");
        params.put("jobName", jobName);
    }

    /*
     * Prepare the search expression based on the job's id.
     */
    private void jobId(Object p_key, Map<?, ?> criteria)
    {
        String jobId = (String) criteria.get(p_key);
        String condition = (String) criteria.get(new Integer(
                JobSearchParameters.JOB_ID_CONDITION));

        if (SearchCriteriaParameters.LESS_THAN.equals(condition))
        {
            sql.append(" and j.id < :jobId ");
            params.put("jobId", Long.valueOf(jobId));
        }
        else if (SearchCriteriaParameters.GREATER_THAN.equals(condition))
        {
            sql.append(" and j.id > :jobId ");
            params.put("jobId", Long.valueOf(jobId));
        }
        else
        {
			String[] jobIds = jobId.split(",");
			boolean isFirst = true;
			for (String jobIdEq : jobIds)
			{
			    jobIdEq = jobIdEq.trim();
				if (jobIdEq.contains("-"))
				{
				    String[] jobIdBet = jobIdEq.split("-");
				    long jobIdFrom = Long.valueOf(jobIdBet[0].trim());
				    long jobIdTo = Long.valueOf(jobIdBet[1].trim());

				    if (jobIdFrom > jobIdTo)
				    {
				        jobIdFrom = Long.valueOf(jobIdBet[1]);
				        jobIdTo = Long.valueOf(jobIdBet[0]);
				    }
                    if (isFirst)
                    {
                        sql.append(" and (j.id >= " + jobIdFrom
                                + " and j.id <= " + jobIdTo);
                    }
                    else
                    {
                        sql.append(" or j.id >= " + jobIdFrom
                                + " and j.id <= " + jobIdTo);
                    }
					
				}
				else
				{
				    if (isFirst)
                    {
                        sql.append(" and (j.id = " + jobIdEq);
                    }
                    else
                    {
                        sql.append(" or j.id = " + jobIdEq);
                    }
				}
				isFirst = false;
			}
			sql.append(") ");
		}
    }
    
    /*
     * Prepare the search expression based on the job group id.
     */
    private void jobGroupId(Object p_key, Map<?, ?> criteria)
	{
		String jobGroupId = (String) criteria.get(p_key);

		sql.append(" and j.GROUP_ID = :jobGroupId ");

		params.put("jobGroupId", Long.valueOf(jobGroupId));
	}

    /*
     * Prepare the search expression based on the job's priority.
     */
    private void jobPriority(Object p_key, Map<?, ?> criteria)
    {
        String priority = (String) criteria.get(p_key);

        if (priority.indexOf("*") >= 0)
        {
            priority = priority.replace('*', '%');
            sql.append(" and j.priority like :priority ");
        }
        else
        {
            sql.append(" and j.priority = :priority ");
        }

        params.put("priority", Integer.parseInt(priority));
    }

    /*
     * Prepare the search expression based on the job's state.
     */
    private void jobState(Object p_key, Map<?, ?> criteria)
    {
        List<?> states = (List<?>) criteria.get(p_key);

        String statesStr = convertList(states);

        sql.append(" and j.state in (" + statesStr + ") ");
    }

    /*
     * Prepare the search expression based on the project id
     */
    @SuppressWarnings("unchecked")
    private void jobProject(Object p_key, Map<?, ?> criteria)
    {
        // first check the project Id sent in is a List of Ids
        Object keyCriteria = (Object) criteria.get(p_key);
        List<Long> projectIds = null;
        if (keyCriteria instanceof String)
        {
            s_logger.debug("jobProject() handling single project id");
            Long projectId = Long.valueOf((String) keyCriteria);
            projectIds = new ArrayList<Long>();
            projectIds.add(projectId);
        }
        else
        {
            s_logger.debug("jobProject() handling list of project ids");
            projectIds = (List<Long>) keyCriteria;
        }

        sql.append(" and l.PROJECT_ID in (")
                .append(convertList(projectIds)).append(") ");
    }

    /**
     * Prepare the search expression based on the job's source locale
     */
    private void sourceLocaleExpression(Object p_key, Map<?, ?> criteria)
    {
        GlobalSightLocale srcLocale = (GlobalSightLocale) criteria.get(p_key);
        sql.append(" and l.SOURCE_LOCALE_ID = :srcLocale ");
        params.put("srcLocale", srcLocale.getIdAsLong());
    }

    /**
     * Prepare the search expression based on the job's workflowtemplate's
     * target locales
     */
    private void targetLocaleExpression(Object p_key, Map<?, ?> criteria)
    {
        GlobalSightLocale trgLocale = (GlobalSightLocale) criteria.get(p_key);
        sql.append(" and w.TARGET_LOCALE_ID = :trgLocale ");
        sql.append(" and w.STATE <> :cancelled ");
        params.put("trgLocale", trgLocale.getIdAsLong());
        params.put("cancelled", WorkflowImpl.CANCELLED);
    }

    /*
     * Prepare the search expression based on the job's creation start date
     * based on X days/months/years ago.
     */
    private void creationStartExpression(Object p_key, Map<?, ?> criteria)
    {
        Integer amount = (Integer) criteria.get(p_key);
        String condition = (String) criteria.get(new Integer(
                JobSearchParameters.CREATION_START_CONDITION));

        Calendar now = Calendar.getInstance();
        int negativeAmount = (int) 0 - amount.intValue();
        if (!condition.equals(SearchCriteriaParameters.HOURS_AGO))
        {
            // if it's not hours ago, then start counting back from
            // this morning just past midnight
            now.set(Calendar.HOUR_OF_DAY, 0);
            now.set(Calendar.MINUTE, 0);
            now.set(Calendar.SECOND, 0);
            now.set(Calendar.MILLISECOND, 0);
        }

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
        else
        {
            // assume SearchCriteriaParameters.HOURS_AGO
            now.add(Calendar.HOUR_OF_DAY, negativeAmount);
        }

        sql.append(" and j.CREATE_DATE >= :createExpressionStartDate ");
        params.put("createExpressionStartDate", now.getTime());
    }

    /*
     * Prepare the search expression based on the job's creation start date as
     * an actual date.
     */
    private void creationStartDateExpression(Object p_key, Map<?, ?> criteria)
    {
        Date createDate = (Date) criteria.get(p_key);
        sql.append(" and j.CREATE_DATE >= :createStartDate ");
        params.put("createStartDate", createDate);
    }

    /*
     * Prepare the search expression based on the job's creation start date end
     * as an actual date.
     */
    private void creationEndDateExpression(Object p_key, Map<?, ?> criteria)
    {
        Date startDateEnd = (Date) criteria.get(p_key);
        sql.append(" and j.CREATE_DATE <= :createEndDate ");
        params.put("createEndDate", startDateEnd);
    }

    /*
     * Prepare the search expression based on the job's creation date (end)
     */
    private void creationEndExpression(Object p_key, Map<?, ?> criteria)
    {
        Integer amount = (Integer) criteria.get(p_key);
        String condition = (String) criteria.get(new Integer(
                JobSearchParameters.CREATION_END_CONDITION));

        Calendar now = Calendar.getInstance();
        if (amount != null || !condition.equals(SearchCriteriaParameters.NOW))
        {
            int negativeAmount = (int) 0 - amount.intValue();
            if (!condition.equals(SearchCriteriaParameters.HOURS_AGO))
            {
                // if it's not hours ago, then start counting back from
                // tonight just before midnight
                now.set(Calendar.HOUR_OF_DAY, 23);
                now.set(Calendar.MINUTE, 59);
                now.set(Calendar.SECOND, 59);
                now.set(Calendar.MILLISECOND, 999);
            }

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
            else
            {
                // assume SearchCriteriaParameters.HOURS_AGO
                now.add(Calendar.HOUR_OF_DAY, negativeAmount);
            }
        }

        sql.append(" and j.CREATE_DATE <= :createExpressionEndDate ");
        params.put("createExpressionEndDate", now.getTime());
    }

    /*
     * Prepare the search expression based on the job's workflows estimated
     * completion date
     */
    private void estCompletionStartExpression(Object p_key, Map<?, ?> criteria)
    {
        Integer amount = (Integer) criteria.get(p_key);
        String condition = (String) criteria.get(new Integer(
                JobSearchParameters.EST_COMPLETION_START_CONDITION));

        Calendar now = Calendar.getInstance();
        if (amount != null || !condition.equals(SearchCriteriaParameters.NOW))
        {
            int negativeAmount = (int) 0 - amount.intValue();
            int positiveAmount = amount.intValue();

            if (condition.equals(SearchCriteriaParameters.HOURS_AGO) == false
                    && condition
                            .equals(SearchCriteriaParameters.HOURS_FROM_NOW) == false)
            {
                // if it's not hours ago, then start counting back from
                // this morning just past midnight
                now.set(Calendar.HOUR_OF_DAY, 0);
                now.set(Calendar.MINUTE, 0);
                now.set(Calendar.SECOND, 0);
                now.set(Calendar.MILLISECOND, 0);
            }

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
            else if (condition.equals(SearchCriteriaParameters.MONTHS_FROM_NOW))
            {
                now.add(Calendar.MONTH, positiveAmount);
            }
            else if (condition.equals(SearchCriteriaParameters.WEEKS_FROM_NOW))
            {
                now.add(Calendar.WEEK_OF_YEAR, positiveAmount);
            }
            else if (condition.equals(SearchCriteriaParameters.DAYS_FROM_NOW))
            {
                now.add(Calendar.DATE, positiveAmount);
            }
            else if (condition.equals(SearchCriteriaParameters.HOURS_FROM_NOW))
            {
                now.add(Calendar.HOUR_OF_DAY, positiveAmount);
            }
            else
            {
                // assume SearchCriteriaParameters.HOURS_AGO
                now.add(Calendar.HOUR_OF_DAY, negativeAmount);
            }
        }

        sql.append(" and w.ESTIMATED_COMPLETION_DATE >= :estimatedCompletionStartDate ");
        params.put("estimatedCompletionStartDate", now.getTime());
    }

    /*
     * Prepare the search expression based on the job's workflows' completion
     * end
     */
    private void estCompletionEndExpression(Object p_key, Map<?, ?> criteria)
    {
        Integer amount = (Integer) criteria.get(p_key);
        String condition = (String) criteria.get(new Integer(
                JobSearchParameters.EST_COMPLETION_END_CONDITION));

        Calendar now = Calendar.getInstance();
        if (amount != null || !condition.equals(SearchCriteriaParameters.NOW))
        {
            int negativeAmount = (int) 0 - amount.intValue();
            int positiveAmount = amount.intValue();
            if (condition.equals(SearchCriteriaParameters.HOURS_AGO) == false
                    && condition
                            .equals(SearchCriteriaParameters.HOURS_FROM_NOW) == false)
            {
                // if it's not hours ago, then start counting back from
                // tonight just before midnight
                now.set(Calendar.HOUR_OF_DAY, 23);
                now.set(Calendar.MINUTE, 59);
                now.set(Calendar.SECOND, 59);
                now.set(Calendar.MILLISECOND, 999);
            }

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
            else if (condition.equals(SearchCriteriaParameters.MONTHS_FROM_NOW))
            {
                now.add(Calendar.MONTH, positiveAmount);
            }
            else if (condition.equals(SearchCriteriaParameters.WEEKS_FROM_NOW))
            {
                now.add(Calendar.WEEK_OF_YEAR, positiveAmount);
            }
            else if (condition.equals(SearchCriteriaParameters.DAYS_FROM_NOW))
            {
                now.add(Calendar.DATE, positiveAmount);
            }
            else if (condition.equals(SearchCriteriaParameters.HOURS_FROM_NOW))
            {
                now.add(Calendar.HOUR_OF_DAY, positiveAmount);
            }
            else
            {
                // assume SearchCriteriaParameters.HOURS_AGO
                now.add(Calendar.HOUR_OF_DAY, negativeAmount);
            }
        }

        sql.append(" and w.ESTIMATED_COMPLETION_DATE <= :estimatedCompletionEndDate ");
        params.put("estimatedCompletionEndDate", now.getTime());
    }

    /*
     * Prepare the search expression based on the user's id.
     */
    private void userExpression(Object p_key, Map<?, ?> criteria)
    {
        User user = (User) criteria.get(p_key);
        String userid = user.getUserId();
        PermissionSet perms = new PermissionSet();
        try
        {
            perms = Permission.getPermissionManager().getPermissionSetForUser(
                    userid);
        }
        catch (Exception e)
        {
            s_logger.error("Failed to get permissions for user " + userid, e);
        }

        if (perms.getPermissionFor(Permission.JOB_SCOPE_ALL))
        {
            return;
        }
        else
        {
            if (perms.getPermissionFor(Permission.PROJECTS_MANAGE))
            {
                sql.append(" and ( p.MANAGER_USER_ID = :userId ");
                params.put("userId", userid);
                if (perms
                        .getPermissionFor(Permission.PROJECTS_MANAGE_WORKFLOWS))
                {
                    sql.append(" or wo.OWNER_ID = :ownerId ");
                    params.put("ownerId", userid);
                }
                if (perms.getPermissionFor(Permission.JOB_SCOPE_MYPROJECTS))
                {
                    List allProjectsIds = getMyProjects(userid);
                    String projectsIdsStr = convertList(allProjectsIds);
                    sql.append(" or l.PROJECT_ID in ("
                            + projectsIdsStr + ") ");
                }
                sql.append(")");
            }
            else
            {
                if (perms
                        .getPermissionFor(Permission.PROJECTS_MANAGE_WORKFLOWS))
                {
                    sql.append(" and ( wo.OWNER_ID = :ownerId ");
                    params.put("ownerId", userid);
                    if (perms.getPermissionFor(Permission.JOB_SCOPE_MYPROJECTS))
                    {
                        List allProjectsIds = getMyProjects(userid);
                        String projectsIdsStr = convertList(allProjectsIds);
                        sql.append(" or l.PROJECT_ID in ("
                                + projectsIdsStr + ") ");
                    }
                    sql.append(")");
                }
                else
                {
                    if (perms.getPermissionFor(Permission.JOB_SCOPE_MYPROJECTS))
                    {
                        List allProjectsIds = getMyProjects(userid);
                        String projectsIdsStr = convertList(allProjectsIds);
                        sql.append(" and ( l.PROJECT_ID in ("
                                + projectsIdsStr + ") ");
                        sql.append(")");
                    }
                    else
                    {
                        sql.append("and 1=2");
                    }
                }
            }
        }
    }

    public List getMyProjects(String userid)
    {
        List<Long> condition = new ArrayList<Long>();
        List allProjects = (List) ProjectHandlerHelper.getProjectByUser(userid);
        Iterator itAllProjects = allProjects.iterator();
        while (itAllProjects.hasNext())
        {
            Project project = (Project) itAllProjects.next();
            Set userIds = (Set) project.getUserIds();
            if (userIds.contains(userid))
            {
                condition.add(project.getId());
            }
        }
        return condition;
    }

    private void pmExpression(User p_user)
    {
        sql.append(" and r.l10nProfile.project.managerUserId = :userId ");
        params.put("userId", p_user.getUserId());
    }

    private void wfmExpression(User p_user)
    {
        sql.append(" and wfo.ownerId = :ownerId ");
        params.put("ownerId", p_user.getUserId());
    }

    private String dealWithCondition(String s, String condition)
    {
        if (!m_isCaseSensitive)
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

    private String convertList(List<?> list)
    {
        String result = "null";
        if (list == null || list.size() == 0)
        {
            return result;
        }
        for (int i = 0; i < list.size(); i++)
        {
            Object obj = list.get(i);
            if (obj instanceof String)
            {
                result += ",'" + list.get(i) + "'";
            }
            else
            {
                result += "," + list.get(i) + "";
            }
        }
        return result;
    }
}
