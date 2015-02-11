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
package com.globalsight.everest.taskmanager;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.foundation.SearchCriteriaParameters;
import com.globalsight.everest.workflow.WorkflowConstants;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GlobalSightLocale;

/**
 * TaskSearchCriteria is used to construct an activity search expression based
 * on user input.
 */
public class TaskSearchCriteria
{
    private StringBuffer hql = new StringBuffer();

    private boolean m_isCaseSensitive = false;

    private Map params = new HashMap();
    private Map params2 = new HashMap();

    // ////////////////////////////////////////////////////////////////////////////
    // Begin: Abstract Method Implementation
    // ////////////////////////////////////////////////////////////////////////////

    /**
     * Return the search result based on the specified search criteria object.
     * 
     * @param p_searchCriteriaParams
     *            - The search criteria object.
     */
    public List search(SearchCriteriaParameters p_searchCriteriaParams)
            throws Exception
    {
        hql.append("select distinct t from TaskImpl t ").append(
                "inner join t.workflow.job.requestSet r where 1=1 ");

        m_isCaseSensitive = p_searchCriteriaParams.isCaseSensitive();
        Map criteria = p_searchCriteriaParams.getParameters();

        // get the keys of the map
        Object[] keys = criteria.keySet().toArray();

        int mapSize = keys.length;

        // loop throught the parameters to create the sql statement.
        for (int i = 0; i < mapSize; i++)
        {
            switch (((Integer) (keys[i])).intValue())
            {
            // job name
                case TaskSearchParameters.JOB_NAME:
                    jobName(keys[i], criteria);
                    break;

                // job id
                case TaskSearchParameters.JOB_ID:
                    jobId(keys[i], criteria);
                    break;

                // job priority
                case TaskSearchParameters.PRIORITY:
                    jobPriority(keys[i], criteria);
                    break;

                // activity name
                case TaskSearchParameters.ACTIVITY_NAME:
                    activityName(keys[i], criteria);
                    break;

                // activity state
                case TaskSearchParameters.STATE:
                    activityState(keys[i], criteria);
                    break;

                // Source locale
                case TaskSearchParameters.SOURCE_LOCALE:
                    sourceLocaleExpression(keys[i], criteria);
                    break;

                // target locale
                case TaskSearchParameters.TARGET_LOCALE:
                    targetLocaleExpression(keys[i], criteria);
                    break;

                // activity acceptance start date
                case TaskSearchParameters.ACCEPTANCE_START:
                    acceptanceStartExpression(keys[i], criteria);
                    break;

                // activity acceptance end date
                case TaskSearchParameters.ACCEPTANCE_END:
                    acceptanceEndExpression(keys[i], criteria);
                    break;

                // activity est completion start date
                case TaskSearchParameters.EST_COMPLETION_START:
                    estCompletionStartExpression(keys[i], criteria);
                    break;

                // activity est completion end date
                case TaskSearchParameters.EST_COMPLETION_END:
                    estCompletionEndExpression(keys[i], criteria);
                    break;

                // company name
                case TaskSearchParameters.COMPANY_NAME:
                    companyName(keys[i], criteria);
                    break;

                // ID
                case TaskSearchParameters.ID:
                    id(keys[i], criteria);
                    break;

                default:
                    break;
            }
        }

        if (!criteria.keySet().contains(TaskSearchParameters.COMPANY_NAME))
        {
            if (!CompanyThreadLocal.getInstance().fromSuperCompany())
            {
                hql.append(" and t.companyId = :companyId");
                params.put("companyId", new Long(CompanyThreadLocal
                        .getInstance().getValue()));
            }
        }

        return HibernateUtil.search(hql.toString(), params, params2);
    }

    // ////////////////////////////////////////////////////////////////////////////
    // End: Abstract Method Implementation
    // ////////////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////
    // Begin: Private Methods
    // ////////////////////////////////////////////////////////////////////

    /*
     * Prepare the search expression based on the job's name.
     */
    private void jobName(Object p_key, Map criteria)
    {
        String jobName = (String) criteria.get(p_key);
        String condition = (String) criteria.get(new Integer(
                TaskSearchParameters.JOB_NAME_CONDITION));

        jobName = dealWithCondition(jobName, condition);

        hql.append(" and t.workflow.job.jobName like :jobName ");

        params.put("jobName", jobName);
    }

    /*
     * Prepare the search expression based on the job's id.
     */
    private void jobId(Object p_key, Map criteria)
    {
        String jobId = (String) criteria.get(p_key);
        String condition = (String) criteria.get(new Integer(
                TaskSearchParameters.JOB_ID_CONDITION));

        if (SearchCriteriaParameters.LESS_THAN.equals(condition))
        {
            hql.append(" and t.workflow.job.id < :jobId ");
        }
        else if (SearchCriteriaParameters.GREATER_THAN.equals(condition))
        {
            hql.append(" and t.workflow.job.id > :jobId ");
        }
        else
        {
            hql.append(" and t.workflow.job.id = :jobId ");
        }

        params.put("jobId", Long.valueOf(jobId));
    }

    /*
     * Prepare the search expression based on the job's priority.
     */
    private void jobPriority(Object p_key, Map criteria)
    {
        String priority = (String) criteria.get(p_key);

        if (priority.indexOf("*") >= 0)
        {
            priority = priority.replace('*', '%');
            hql.append(" and t.workflow.job.priority like :priority ");
        }
        else
        {
            hql.append(" and t.workflow.job.priority = :priority ");
        }

        params.put("priority", priority);
    }

    /*
     * Prepare the search expression based on the activity's name
     */
    private void activityName(Object p_key, Map criteria)
    {
        String name = (String) criteria.get(p_key);
        String companyName = (String) criteria
                .get(TaskSearchParameters.COMPANY_NAME);
        long companyId = CompanyWrapper.getCompanyByName(companyName).getId();

        if (name.indexOf("*") >= 0)
        {
            name = name.replace('*', '%');
            hql.append(" and t.name like :name ");
        }
        else
        {
            hql.append(" and t.name = :name ");
            name = name + "_" + companyId;
        }

        params.put("name", name);
    }

    /*
     * Prepare the search expression based on the activity's state
     */
    private void activityState(Object p_key, Map criteria)
    {
        Integer state = (Integer) criteria.get(p_key);
        String stateStr = TaskImpl.getStateAsString(state.intValue());

        if (state == WorkflowConstants.TASK_GSEDITION_IN_PROGESS)
        {
            // add the "dispatched_to_translation","in translation",
            // "translation completed" status into "in progress" search
            hql.append(" and t.stateStr in ('"
                    + TaskImpl.STATE_REDEAY_DISPATCH_GSEDTION_STR + "','"
                    + TaskImpl.STATE_DISPATCHED_TO_TRANSLATION_STR + "','"
                    + TaskImpl.STATE_IN_TRANSLATION_STR + "','"
                    + TaskImpl.STATE_TRANSLATION_COMPLETED_STR + "') ");
        }
        else
        {
            hql.append(" and t.stateStr = :state ");
            params.put("state", stateStr);
        }
    }

    /**
     * Prepare the search expression based on the job's source locale
     */
    private void sourceLocaleExpression(Object p_key, Map criteria)
    {
        GlobalSightLocale srcLocale = (GlobalSightLocale) criteria.get(p_key);
        hql.append(" and r.l10nProfile.sourceLocale = :srcLocale ");
        params.put("srcLocale", srcLocale.getIdAsLong());
    }

    /**
     * Prepare the search expression based on the job's workflowtemplate's
     * target locales
     */
    private void targetLocaleExpression(Object p_key, Map criteria)
    {
        GlobalSightLocale trgLocale = (GlobalSightLocale) criteria.get(p_key);
        hql.append(" and t.workflow.targetLocale = :trgLocale ");
        params.put("trgLocale", trgLocale.getIdAsLong());
    }

    /*
     * Prepare the search expression based on the job's creation start date
     */
    private void acceptanceStartExpression(Object p_key, Map criteria)
    {
        Integer amount = (Integer) criteria.get(p_key);

        String condition = (String) criteria.get(new Integer(
                TaskSearchParameters.ACCEPTANCE_START_CONDITION));

        Calendar now = Calendar.getInstance();
        int negativeAmount = (int) 0 - amount.intValue();

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

        hql.append(" and t.acceptedDate >= :acceptedDate ");
        params.put("acceptedDate", now.getTime());
    }

    /*
     * Prepare the search expression based on the job's creation date (end)
     */
    private void acceptanceEndExpression(Object p_key, Map criteria)
    {
        Integer amount = (Integer) criteria.get(p_key);
        String condition = (String) criteria.get(new Integer(
                TaskSearchParameters.ACCEPTANCE_END_CONDITION));

        Calendar now = Calendar.getInstance();
        if (amount != null || !condition.equals(SearchCriteriaParameters.NOW))
        {
            int negativeAmount = (int) 0 - amount.intValue();
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

        hql.append(" and t.acceptedDate <= :acceptedDate ");
        params.put("acceptedDate", now.getTime());
    }

    /*
     * Prepare the search expression based on the job's workflows estimated
     * completion date
     */
    private void estCompletionStartExpression(Object p_key, Map criteria)
    {
        Integer amount = (Integer) criteria.get(p_key);
        String condition = (String) criteria.get(new Integer(
                TaskSearchParameters.EST_COMPLETION_START_CONDITION));

        Calendar now = Calendar.getInstance();
        if (amount != null || !condition.equals(SearchCriteriaParameters.NOW))
        {

            int negativeAmount = (int) 0 - amount.intValue();
            int positiveAmount = amount.intValue();
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

        hql.append(" and t.estimatedCompletionDate >= :estimatedCompletionDate ");
        params.put("estimatedCompletionDate", now.getTime());
    }

    /*
     * Prepare the search expression based on the job's workflows' completion
     * end
     */
    private void estCompletionEndExpression(Object p_key, Map criteria)
    {
        Integer amount = (Integer) criteria.get(p_key);
        String condition = (String) criteria.get(new Integer(
                TaskSearchParameters.EST_COMPLETION_END_CONDITION));

        Calendar now = Calendar.getInstance();
        if (amount != null || !condition.equals(SearchCriteriaParameters.NOW))
        {
            int negativeAmount = (int) 0 - amount.intValue();
            int positiveAmount = amount.intValue();
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

        hql.append(" and t.estimatedCompletionDate <= :estimatedCompletionDate ");
        params.put("estimatedCompletionDate", now.getTime());
    }

    /*
     * Prepare the search expression based on the company's name
     */
    private void companyName(Object p_key, Map criteria)
    {
        String companyName = (String) criteria.get(p_key);
        long companyId = CompanyWrapper.getCompanyByName(companyName).getId();
        hql.append(" and t.companyId = :companyId");
        params.put("companyId", new Long(companyId));
    }

    /**
     * Prepare the search expression based on the task id.
     * 
     * @param p_key
     *            The key of the task ids.
     * @param criteria
     *            A map included all search criteria.
     */
    private void id(Object p_key, Map criteria)
    {
        Set ids = (Set) criteria.get(p_key);
        hql.append(" and t.id in (:ids)");
        params2.put("ids", ids);
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
}
