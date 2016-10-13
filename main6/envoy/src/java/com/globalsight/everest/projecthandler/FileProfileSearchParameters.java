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

public class FileProfileSearchParameters extends SearchCriteriaParameters
{
	
    public static final int FP_TEMPLATE_NAME = 0;
    public static final int FP_NAME_CONDITION = 1;
    public static final int LOCALIZATION_PROFILES_ID = 2;
    public static final int SOURCE_FILE_FORMAT = 3;
    public static final int SOURCE_FILE_ENCODING = 4; 
    
    /**
     *  Default constructor.
     */
    public FileProfileSearchParameters() 
    {
        super();
    }
    
    /**
     * Set the fileprofile template name to be searched.
     */
    public void setFileProfileName(String p_name) 
    {
        addElement(FP_TEMPLATE_NAME, p_name);
    }
    /**
     * Set the search condition for the fileprofile template name (i.e.
     * begins with, contains, and etc.)
     */
    public void setFileProfileCondition(String p_key)
    {
        addElement(FP_NAME_CONDITION, p_key);
    }
    /**
     * Set the source format to be searched.
     */
    public void setSourceFileFormat(Long p_sourceFormatId)
    {
        addElement(SOURCE_FILE_FORMAT, p_sourceFormatId);
    }
    /**
     * Set the localization id to be searched.
     */
    public void setLocProfilesId(Long p_locprojectId)
    {
    	addElement(LOCALIZATION_PROFILES_ID, p_locprojectId );
    }
}
