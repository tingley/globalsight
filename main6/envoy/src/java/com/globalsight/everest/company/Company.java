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
package com.globalsight.everest.company;

// GlobalSight
import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.everest.webapp.pagehandler.administration.company.CompanyConstants;
import com.globalsight.ling.tm2.TmVersion;

/**
 * This class only represent a wrapper object for the company names defined in
 * Envoy database and is used for defining a workflow template (template node
 * names).
 * 
 */
public class Company extends PersistentObject
{
    private static final long serialVersionUID = 3382538647678827006L;

    public static final String STATE_DELETING = "DELETING";
    // company description
    private String m_description;
    // enable IP filter when using Desktop Icon (company level)
    private boolean m_enableIPFilter = true;
    private boolean m_enableTMAccessControl = false;
    private boolean m_enableTBAccessControl = false;
    private boolean m_enableQAChecks = false;
    // for sso
    private String m_ssoIdpUrl;
    private String m_ssoLoginUrl;
    private String m_ssoLogoutUrl;
    private String m_ssoWSEndpoint;
    private boolean m_enableSSOLogin = false;
    private String sessionTime;

    // Company Email Address
    private String m_email;

    // Segment TM version - 2 for TM2, 3 for TM3
    private int m_tmVersion = TmVersion.TM2.getValue();

    public boolean useActive = true;

    private String m_state;

    private int bigDataStoreLevel = CompanyConstants.BIG_DATA_STORE_LEVEL_COMPNAY;
    private int m_migrateProcessing = 0;

    private boolean m_enableDitaChecks = false;
    private boolean m_enableWorkflowStatePosts = false;

    // ////////////////////////////////////////////////////////////////////////////////
    // Begin: Constructor
    // ////////////////////////////////////////////////////////////////////////////////
    /**
     * Default Company constructor.
     */
    public Company()
    {
        super();
    }

    // tbd - remove
    public Company(String p_companyName)
    {
        super();
        setName(p_companyName.length() > 30 ? p_companyName.substring(0, 30)
                : p_companyName);
    }

    /**
     * Company constructor used for creating a new company.
     * 
     * @param p_companyName
     *            - The company name.
     * @param p_companyDescription
     *            - The description of the company.
     */
    public Company(String p_companyName, String p_companyDescription)
    {
        super();
        setName(p_companyName.length() > 30 ? p_companyName.substring(0, 30)
                : p_companyName);
        m_description = p_companyDescription;
    }

    // ////////////////////////////////////////////////////////////////////////////////
    // End: Constructor
    // ////////////////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////////////////
    // Begin: Local Methods
    // ////////////////////////////////////////////////////////////////////////////////
    /**
     * Get the company name.
     * 
     * @return The company name.
     */
    public String getCompanyName()
    {
        return getName();
    }

    /**
     * Get the company description.
     * 
     * @return The company description.
     */
    public String getDescription()
    {
        return m_description;
    }

    /**
     * Update the company's description;
     */
    public void setDescription(String p_newDescription)
    {
        m_description = p_newDescription;
    }

    public boolean getEnableIPFilter()
    {
        return m_enableIPFilter;
    }

    public void setEnableIPFilter(boolean p_enableIPFilter)
    {
        this.m_enableIPFilter = p_enableIPFilter;
    }

    public boolean getEnableTMAccessControl()
    {
        return m_enableTMAccessControl;
    }

    public void setEnableTMAccessControl(boolean p_EnableTMTBAccessControl)
    {
        m_enableTMAccessControl = p_EnableTMTBAccessControl;
    }

    public boolean getEnableTBAccessControl()
    {
        return m_enableTBAccessControl;
    }

    public void setEnableTBAccessControl(boolean p_EnableTMTBAccessControl)
    {
        m_enableTBAccessControl = p_EnableTMTBAccessControl;
    }

    public boolean getEnableQAChecks()
    {
        return m_enableQAChecks;
    }

    public void setEnableQAChecks(boolean p_enableQAChecks)
    {
        m_enableQAChecks = p_enableQAChecks;
    }

    // ////////////////////////////////////////////////////////////////////////////////
    // End: Local Methods
    // ////////////////////////////////////////////////////////////////////////////////
    /**
     * Returns a string representation of the object (based on the object name).
     */
    public String toString()
    {
        return getName();
    }

    /**
     * Returns 'true' if the ids are the same - this denotes the same company
     * object. If they aren't the same then it returns 'false'.
     */
    public boolean equals(Object p_company)
    {
        if (p_company instanceof Company)
        {
            return (getId() == ((Company) p_company).getId());
        }
        return false;
    }

    /**
     * Return a string representation of the object appropriate for debugging.
     * 
     * @return a string representation of the object appropriate for debugging.
     */
    public String toDebugString()
    {
        StringBuffer buff = new StringBuffer();
        buff.append(super.toString());
        buff.append(", m_companyDescription=");
        buff.append(m_description != null ? m_description : "null");
        return buff.toString();
    }

    public String getSsoIdpUrl()
    {
        return m_ssoIdpUrl;
    }

    public void setSsoIdpUrl(String ssoIdpUrl)
    {
        this.m_ssoIdpUrl = ssoIdpUrl;
    }

    public String getSsoLoginUrl()
    {
        return m_ssoLoginUrl;
    }

    public void setSsoLoginUrl(String ssoLoginUrl)
    {
        this.m_ssoLoginUrl = ssoLoginUrl;
    }

    public String getSsoLogoutUrl()
    {
        return m_ssoLogoutUrl;
    }

    public void setSsoLogoutUrl(String ssoLogoutUrl)
    {
        this.m_ssoLogoutUrl = ssoLogoutUrl;
    }

    public String getSessionTime()
    {
        return sessionTime;
    }

    public void setSessionTime(String sessionTime)
    {
        this.sessionTime = sessionTime;
    }

    public String getSsoWSEndpoint()
    {
        return m_ssoWSEndpoint;
    }

    public void setSsoWSEndpoint(String ssoWSEndpoint)
    {
        this.m_ssoWSEndpoint = ssoWSEndpoint;
    }

    public boolean getEnableSSOLogin()
    {
        return m_enableSSOLogin;
    }

    public void setEnableSSOLogin(boolean enableSSOLogin)
    {
        this.m_enableSSOLogin = enableSSOLogin;
    }

    /**
     * Internal getter/setter for use by Hibernate.
     */
    protected int getTmVersionVal()
    {
        return m_tmVersion;
    }

    protected void setTmVersionVal(int tmVersion)
    {
        this.m_tmVersion = tmVersion;
    }

    /**
     * Public getter/setter to use from calling code.
     */
    public TmVersion getTmVersion()
    {
        return TmVersion.fromValue(m_tmVersion);
    }

    public void setTmVersion(TmVersion version)
    {
        this.m_tmVersion = version.getValue();
    }

    public String getEmail()
    {
        return m_email;
    }

    public void setEmail(String email)
    {
        this.m_email = email;
    }

    public void setState(String p_state)
    {
        if (p_state != null)
        {
            p_state = p_state.toUpperCase();
        }

        m_state = p_state;
    }

    public String getState()
    {
        return m_state;
    }

    public int getBigDataStoreLevel()
    {
        return bigDataStoreLevel;
    }

    public void setBigDataStoreLevel(int p_bigDataStoreLevel)
    {
        this.bigDataStoreLevel = p_bigDataStoreLevel;
    }

    public int getMigrateProcessing()
    {
        return m_migrateProcessing;
    }

    public void setMigrateProcessing(int p_migrateProcessing)
    {
        this.m_migrateProcessing = p_migrateProcessing;
    }

    public boolean getEnableDitaChecks()
    {
        return m_enableDitaChecks;
    }

    public void setEnableDitaChecks(boolean p_enableDitaChecks)
    {
        this.m_enableDitaChecks = p_enableDitaChecks;
    }
    
    public boolean getEnableWorkflowStatePosts()
    {
        return m_enableWorkflowStatePosts;
    }
    
    public void setEnableWorkflowStatePosts(boolean p_enableWorkflowStatePosts)
    {
        this.m_enableWorkflowStatePosts = p_enableWorkflowStatePosts;
    }
}
