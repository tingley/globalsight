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

import java.util.Set;

import com.globalsight.everest.foundation.SearchCriteriaParameters;
import com.globalsight.everest.foundation.User;
import com.globalsight.util.GlobalSightLocale;

/**
* This class is a subclass of SearchCriteriaParameters which sets
* all the values required for building the query expression used
* for searching tasks
*/
public class TaskSearchParameters extends SearchCriteriaParameters
{
    private static final long serialVersionUID = 1043809021107771652L;
    
    public static final int JOB_NAME = 0;
    public static final int JOB_NAME_CONDITION = 1;
    public static final int JOB_ID = 2;
    public static final int JOB_ID_CONDITION = 3;
    public static final int STATE = 4;
    public static final int ACTIVITY_NAME=5;
    public static final int SOURCE_LOCALE = 6;
    public static final int TARGET_LOCALE = 7;
    public static final int PRIORITY = 8;
    public static final int ACCEPTANCE_START = 9;
    public static final int ACCEPTANCE_START_CONDITION = 10;
    public static final int ACCEPTANCE_END = 11;
    public static final int ACCEPTANCE_END_CONDITION = 12;
    public static final int EST_COMPLETION_START = 13;
    public static final int EST_COMPLETION_START_CONDITION = 14;
    public static final int EST_COMPLETION_END = 15;
    public static final int EST_COMPLETION_END_CONDITION = 16;
    public static final int USER = 17;
    public static final int SESSION_ID = 18;
    
    public static final int COMPANY_NAME = 19;
    public static final int ID = 20;
    
    public static final int ROW_START = 21;
    public static final int ROW_PER_PAGE = 22;
    public static final int SORT_COLUMN = 23;
    public static final int SORT_TYPE = 24;
    public static final int ACTIVITY_NAME_CONDITION = 25;
    public static final int ASSIGNEES_NAME = 27;
    
    /**
     *  Default constructor.
     */
    public TaskSearchParameters() 
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
     * Set the company name to be searched.
     */
    public void setCompanyName(String p_name) 
    {
        addElement(COMPANY_NAME, p_name);
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

    /**
     * Set the search condition for the job id (i.e.
     * less than or greater than.)
     */
    public void setJobIdCondition(String p_jobIdKey)
    {
        addElement(JOB_ID_CONDITION, p_jobIdKey);
    }

    /**
     * Set the activity name to be searched.
     */
    public void setActivityName(String p_name) 
    {
        addElement(ACTIVITY_NAME, p_name);
    }

    /**
     * Set the activity state to be searched for.
     */
    public void setActivityState(Integer p_state)
    {
        addElement(STATE, p_state);
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
     * Set the acceptance start amount
     */
    public void setAcceptanceStart(Integer p_startNum)
    {
        addElement(ACCEPTANCE_START, p_startNum);
    }

    /**
     * Set the condition for searching the creation start unit (i.e. hours, days,
     * weeks, months).
     */
    public void setAcceptanceStartCondition(String p_startCondition)
    {
        addElement(ACCEPTANCE_START_CONDITION, p_startCondition);
    }

    /**
     * Set the creation end count.
     */
    public void setAcceptanceEnd(Integer p_endNum)
    {
        addElement(ACCEPTANCE_END, p_endNum);
    }

    /**
     * Set the condition for searching the creation end unit (i.e. now, hours, days,
     * weeks, months).
     */
    public void setAcceptanceEndCondition(String p_endCondition)
    {
        addElement(ACCEPTANCE_END_CONDITION, p_endCondition);
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
     * Set the User that this query is executed as. 
     */
    public void setUser(User p_user) 
    {
        addElement(USER, p_user);
    }

    /**
     * Set the Iflow session Id used for queries
     */
    public void setSessionId(String  p_id) 
    {
        addElement(SESSION_ID, p_id);
    }
    
    /**
     * Set Ids used for queries
     */
    public void setIds(Set  p_ids) 
    {
        addElement(ID, p_ids);
    }
    
    public void setRowStart(int start) {
        addElement(ROW_START, start);
    }
    
    public void setRowPerPage(int perPage) {
        addElement(ROW_PER_PAGE, perPage);
    }
    
    public void setSortColumn(String column) {
        addElement(SORT_COLUMN, column);
    }
    
    public void setSortType(boolean isAscSort) {
        addElement(SORT_TYPE, isAscSort);
    }
    
    public void setActivityNameCondition(String p_endCondition) {
    	 addElement(ACTIVITY_NAME_CONDITION, p_endCondition);
    }
    
    public void setAssigneesName(String p_name) {
     	 addElement(ASSIGNEES_NAME, p_name);
    }
    
}

