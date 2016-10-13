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
package com.globalsight.everest.jobhandler;


import com.globalsight.everest.foundation.SearchCriteriaParameters;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.everest.foundation.User;
import java.util.List;
import java.util.Date;

/**
* This class is a subclass of SearchCriteriaParameters which sets
* all the values required for building the query expression used
* for searching jobs.
*/
public class JobSearchParameters extends SearchCriteriaParameters
{
	private static final long serialVersionUID = 1L;
	public static final int JOB_NAME = 0;
    public static final int JOB_NAME_CONDITION = 1;
    public static final int JOB_ID = 2;
    public static final int JOB_ID_CONDITION = 3;
    public static final int STATE = 4;
    public static final int PROJECT_ID = 5;
    public static final int SOURCE_LOCALE = 6;
    public static final int TARGET_LOCALE = 7;
    public static final int PRIORITY = 8;
    public static final int CREATION_START = 9;
    public static final int CREATION_START_CONDITION = 10;
    public static final int CREATION_END = 11;
    public static final int CREATION_END_CONDITION = 12;
    public static final int EST_COMPLETION_START = 13;
    public static final int EST_COMPLETION_START_CONDITION = 14;
    public static final int EST_COMPLETION_END = 15;
    public static final int EST_COMPLETION_END_CONDITION = 16;
    public static final int USER = 17;
    public static final int CREATION_START_DATE = 18;
    public static final int CREATION_END_DATE = 19;
    
    public static final int EXPORT_DATE_START = 20;
    public static final int EXPORT_DATE_START_OPTIONS = 21;
    public static final int EXPORT_DATE_END = 22;
    public static final int EXPORT_DATE_END_OPTIONS = 23;
    public static final int JOB_GROUP_ID = 24;
    /**
     *  Default constructor.
     */
    public JobSearchParameters() 
    {
        super();
    }

    //
    // Helper Methods
    //
    /**
     * Set the job name to be searched.
     */
    public void setJobName(String p_name) 
    {
        addElement(JOB_NAME, p_name);
    }

    /**
     * Set the search condition for the job name (i.e.
     * begins with, contains, and etc.)
     */
    public void setJobNameCondition(String p_key)
    {
        addElement(JOB_NAME_CONDITION, p_key);
    }

    /**
     * Set the job id to be searched.
     */
    public void setJobId(String p_jobId)
    {
        addElement(JOB_ID, p_jobId);
    }
    
    public void setJobGroupId(String p_jobGroupId)
    {
        addElement(JOB_GROUP_ID, p_jobGroupId);
    }
    
	/**
	 *Set the job id to be searched.
	 *@param p_jobId ---List
	 */
    public void setJobId(List p_jobId){
    	addElement(JOB_ID, p_jobId);
    }
    /**
     * Set the search condition for the job id (i.e.
     * less than or greater than.)
     */
    public void setJobIdCondition(String p_jobIdKey)
    {
        addElement(JOB_ID_CONDITION, p_jobIdKey);
    }

    /**
     * Set the job state to be searched for.
     * This takes a List of String
     */
    public void setJobState(List p_states)
    {
        addElement(STATE, p_states);
    }

    /**
     * Set the project id used for the job search.
     * @param p_projectId -- project id
     */
    public void setProjectId(String p_projectId)
    {
        addElement(PROJECT_ID, p_projectId);
    }

    /**
     * Set the project ids used for the job search.
     * @param p_projectId -- List of Long
     */
    public void setProjectId(List p_projectId)
    {
        addElement(PROJECT_ID, p_projectId);
    }

    /**
     * Set the source locale to be searched.
     */
    public void setSourceLocale(GlobalSightLocale p_sourceLocale)
    {
        addElement(SOURCE_LOCALE, p_sourceLocale);
    }

    /**
     * Set the target locale to be searched.
     */
    public void setTargetLocale(GlobalSightLocale p_targetLocale)
    {
        addElement(TARGET_LOCALE, p_targetLocale);
    }

    /**
     * Set the job priority to be searched for.
     */
    public void setPriority(String p_priority)
    {
        addElement(PRIORITY, p_priority);
    }

    /**
     * Set the creation start count.
     */
    public void setCreationStart(Integer p_startNum)
    {
        addElement(CREATION_START, p_startNum);
    }

    /**
     * Sets the creation start date with an actual date.
     * 
     * @param p_startDate
     *               start date
     */
    public void setCreationStart(Date p_startDate)
    {
        addElement(CREATION_START_DATE,p_startDate);
    }


    /**
     * Set the condition for searching the creation start unit (i.e. hours, days,
     * weeks, months).
     */
    public void setCreationStartCondition(String p_startCondition)
    {
        addElement(CREATION_START_CONDITION, p_startCondition);
    }

    /**
     * Set the creation end count.
     */
    public void setCreationEnd(Integer p_endNum)
    {
        addElement(CREATION_END, p_endNum);
    }

    /**
     * Sets the creation start date end with an actual date.
     * 
     * @param p_endDate
     *               start date end
     */
    public void setCreationEnd(Date p_endDate)
    {
        addElement(CREATION_END_DATE,p_endDate);
    }


    /**
     * Set the condition for searching the creation end unit (i.e. now, hours, days,
     * weeks, months).
     */
    public void setCreationEndCondition(String p_endCondition)
    {
        addElement(CREATION_END_CONDITION, p_endCondition);
    }


    /**
     * Set the est completion start count.
     */
    public void setEstCompletionStart(Integer p_startNum)
    {
        addElement(EST_COMPLETION_START, p_startNum);
    }

    /**
     * Set the condition for searching the est completion start unit (i.e. hours, days,
     * weeks, months).
     */
    public void setEstCompletionStartCondition(String p_startCondition)
    {
        addElement(EST_COMPLETION_START_CONDITION, p_startCondition);
    }

    /**
     * Set the creation end count.
     */
    public void setEstCompletionEnd(Integer p_endNum)
    {
        addElement(EST_COMPLETION_END, p_endNum);
    }

    /**
     * Set the condition for searching the creation end unit (i.e. now, hours, days,
     * weeks, months).
     */
    public void setEstCompletionEndCondition(String p_endCondition)
    {
        addElement(EST_COMPLETION_END_CONDITION, p_endCondition);
    }

    /**
     * Set the User that this query is executed as. This
     * determines what Jobs are returned. Admin gets all,
     * PM gets his/her jobs, WFM gets his/her jobs. Translator
     * doesn't get any jobs.
     */
    public void setUser(User p_user) 
    {
        addElement(USER, p_user);
    }
    
    public void setExportDateStart(Integer p_start)
    {
        addElement(EXPORT_DATE_START, p_start);
    }
    
    public void setExportDateStartOptions(String p_option) {
        addElement(EXPORT_DATE_START_OPTIONS, p_option);
    }
    
    public void setExportDateEnd(Integer p_end) {
        addElement(EXPORT_DATE_END, p_end);
    }
    
    public void setExportDateEndOptions(String p_option) {
        addElement(EXPORT_DATE_END_OPTIONS, p_option);
    }
}

