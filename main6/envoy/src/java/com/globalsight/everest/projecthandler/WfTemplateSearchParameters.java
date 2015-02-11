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

/**
* This class is a subclass of SearchCriteriaParameters which sets
* all the values required for building the query expression used
* for searching workflow templates.
*/
public class WfTemplateSearchParameters extends SearchCriteriaParameters
{
    private static final long serialVersionUID = 4818190354447447244L;

    public static final int WF_TEMPLATE_NAME = 0;
    public static final int WF_NAME_CONDITION = 1;
    public static final int PROJECT = 2;
    public static final int SOURCE_LOCALE = 3;
    public static final int TARGET_LOCALE = 4;    
    public static final int COMPANY_NAME = 5;    
    
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
     * Set the project id used for the workflow template search.
     */
    public void setProject(String p_projectId)
    {
        addElement(PROJECT, p_projectId);
    }

    /**
     * Set the source locale to be searched.
     */
    public void setSourceLocale(String p_sourceLocale)
    {
        addElement(SOURCE_LOCALE, p_sourceLocale);
    }

    /**
     * Set the target locale to be searched.
     */
    public void setTargetLocale(String p_targetLocale)
    {
        addElement(TARGET_LOCALE, p_targetLocale);
    }
    
    public void setCompanyName(String p_companyName)
    {
        addElement(COMPANY_NAME, p_companyName);
    }
}

