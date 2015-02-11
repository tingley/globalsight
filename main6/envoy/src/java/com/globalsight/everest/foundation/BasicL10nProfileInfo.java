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
package com.globalsight.everest.foundation;

public class BasicL10nProfileInfo implements java.io.Serializable
{
    protected long m_profileId = -1;
    protected String m_name = null;    
    protected String m_description = null;
    protected int m_count = -1;
    protected String m_companyId = null;
    
    /**
    * Default Constructor
    */
    public BasicL10nProfileInfo(long p_profileId, 
                                String p_name, 
                                String p_description,
                                String p_companyId)
    {
        m_profileId = p_profileId;
        m_name = p_name;
        m_description = p_description;
        m_companyId = p_companyId;
    }

    public void setWFTCount(int p_count)
    {
        m_count = p_count;
    }

    /**
    * Get the profile name.
    */
    public String getName() 
    {
    	return m_name;
    }

    /**
    * Get the profile description.
    */
    public String getDescription() 
    {
        return m_description;
    }   
    
    public String getCompanyId()
    {
        return m_companyId;
    }

    /**
    * Get the profile id
    */
    public long getProfileId()
    {
        return m_profileId;
    }

    public int getWorkflowTemplateCount()
    {
        return m_count;
    }
    /**
    * Returns a string representation of the object (based on the object name).
    */
    public String toString()
    { 
        return getName();
    }
}
