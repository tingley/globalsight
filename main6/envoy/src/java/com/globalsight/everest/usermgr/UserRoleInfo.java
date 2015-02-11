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

package com.globalsight.everest.usermgr;

//globalsight
import com.globalsight.everest.foundation.User;

//java
import java.util.Vector;

/**
 * The UserRoleInfo class contains info needed for editing user roles
 * in the UI.
 */
public class UserRoleInfo
    implements java.io.Serializable
{
    private String m_sourceDisplayName = null;
    private String m_targetDisplayName = null;
    private String m_source = null;
    private String m_target = null;
    
    private String m_companyId = null;

    /**
     * Default Constructor
     */
    public UserRoleInfo()
    {}

    /**
     * Constructor to set initial values.
     * @param p_sourceDisplayName - The display name for the source locale
     * @param p_targetDisplayName - The display name for the target locale
     * @param p_source - The source locale
     * @param p_target - The target locale
     */
    public UserRoleInfo(String p_sourceDisplayName, String p_targetDisplayName, String p_source, String p_target)
    {
        m_sourceDisplayName = p_sourceDisplayName;
        m_targetDisplayName = p_targetDisplayName;
        m_source = p_source;
        m_target = p_target;
    }

    public String getSourceDisplayName()
    {
        return m_sourceDisplayName;
    }

    public void setSourceDisplayName(String p_sourceDisplayName)
    {
        m_sourceDisplayName = p_sourceDisplayName;
    }

    public String getTargetDisplayName()
    {
        return m_targetDisplayName;
    }

    public void setTargetDisplayName(String p_targetDisplayName)
    {
        m_targetDisplayName = p_targetDisplayName;
    }

    public String getSource()
    {
        return m_source;
    }

    public void setSource(String p_source)
    {
        m_source = p_source;
    }

    public String getTarget()
    {
        return m_target;
    }

    public void setTarget(String p_target)
    {
        m_target = p_target;
    }

    /**
     * @return Returns the companyId.
     */
    public String getCompanyId() {
        return m_companyId;
    }
    /**
     * @param companyId The companyId to set.
     */
    public void setCompanyId(String companyId) {
        this.m_companyId = companyId;
    }
}
