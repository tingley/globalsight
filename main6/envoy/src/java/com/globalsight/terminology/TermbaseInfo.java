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
package com.globalsight.terminology;

/**
 * Wrapper of Termbase for the UI.
 */
public class TermbaseInfo implements java.io.Serializable
{
    private long m_tbId = -1;
    private String m_name = null;    
    private String m_description = null;
    private String m_companyId = null;

    /**
    * Default Constructor
    */
    public TermbaseInfo(long p_tbId, String p_name, String p_description, String p_companyId)
    {
        m_tbId = p_tbId;
        m_name = p_name;
        m_description = p_description;
        m_companyId = p_companyId;
    }

    /**
    * Get the Termbase name.
    */
    public String getName() 
    {
    	return m_name;
    }

    /**
    * Get the Termbase description.
    */
    public String getDescription() 
    {
        return m_description;
    }   

    /**
    * Get the Termbase id
    */
    public long getTermbaseId()
    {
        return m_tbId;
    }
    
    public String getCompanyId()
    {
        return m_companyId;
    }
}
