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
package com.globalsight.everest.projecthandler;


import com.globalsight.everest.foundation.SearchCriteriaParameters;
import com.globalsight.util.GlobalSightLocale;

/**
* This class is a subclass of SearchCriteriaParameters which sets
* all the values required for building the query expression used
* for searching workflow templates.
*/
public class WfTemplateSearchParameters extends SearchCriteriaParameters
{
    public static final int WF_TEMPLATE_NAME = 0;
    public static final int WF_NAME_CONDITION = 1;
    public static final int PROJECT_ID = 2;
    public static final int PROJECT_MANAGER_ID = 3;
    public static final int PM_ID_CONDITION = 4;
    public static final int SOURCE_LOCALE = 5;
    public static final int TARGET_LOCALE = 6;    
    
    /**
     *  Default constructor.
     */
    public WfTemplateSearchParameters() 
    {
        super();
    }

    //
    // Helper Methods
    //
    /**
     * Set the workflow template name to be searched.
     */
    public void setWorkflowName(String p_name) 
    {
        addElement(WF_TEMPLATE_NAME, p_name);
    }

    /**
     * Set the search condition for the workflow template name (i.e.
     * begins with, contains, and etc.)
     */
    public void setWorkflowNameCondition(String p_key)
    {
        addElement(WF_NAME_CONDITION, p_key);
    }

    /**
     * Set the workflow template's project manager username to be searched.
     */
    public void setPmUsername(String p_pmUsername) 
    {
        addElement(PROJECT_MANAGER_ID, p_pmUsername);
    }

    /**
     * Set the search condition for the workflow template's project 
     * manager username (i.e. begins with, contains, and etc.)
     */
    public void setPmUsernameCondition(String p_key)
    {
        addElement(PM_ID_CONDITION, p_key);
    }
    
    /**
     * Set the project id used for the workflow template search.
     */
    public void setProjectId(String p_projectId)
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
}

