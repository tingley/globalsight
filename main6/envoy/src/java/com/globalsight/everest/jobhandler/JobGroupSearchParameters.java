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
public class JobGroupSearchParameters extends SearchCriteriaParameters
{
    /**
	 * TODO
	 */
	private static final long serialVersionUID = 1L;
	public static final int JOB_GROUP_NAME = 0;
    public static final int JOB_GROUP_ID = 1;
    public static final int JOB_GROUP_PROJECT_ID = 2;
    public static final int JOB_GROUP_SOURCE_LOCALE = 3;
    public static final int CREATION_START_DATE = 18;
    
    /**
     *  Default constructor.
     */
    public JobGroupSearchParameters() 
    {
        super();
    }

    //
    // Helper Methods
    //
    /**
     * Set the job name to be searched.
     */
    public void setJobGroupName(String p_name) 
    {
        addElement(JOB_GROUP_NAME, p_name);
    }

    /**
     * Set the job id to be searched.
     */
    public void setJobGroupId(String p_jobId)
    {
        addElement(JOB_GROUP_ID, p_jobId);
    }

    /**
     * Set the project id used for the job search.
     * @param p_projectId -- project id
     */
    public void setJobGroupProjectId(String p_projectId)
    {
        addElement(JOB_GROUP_PROJECT_ID, p_projectId);
    }

    /**
     * Set the source locale to be searched.
     */
    public void setJobGroupSourceLocale(GlobalSightLocale p_sourceLocale)
    {
        addElement(JOB_GROUP_SOURCE_LOCALE, p_sourceLocale);
    }
}

