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

    // company description
    private String m_companyDescription;
    // enable IP filter when using Desktop Icon (company level)
    private boolean m_enableIPFilter = true;
    private boolean m_enableTMAccessControl = false;
    private boolean m_enableTBAccessControl = false;
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
     * @param p_companyName -
     *            The company name.
     * @param p_companyDescription -
     *            The description of the company.
     */
    public Company(String p_companyName, String p_companyDescription)
    {
        super();
        setName(p_companyName.length() > 30 ? p_companyName.substring(0, 30)
                : p_companyName);
        m_companyDescription = p_companyDescription;
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
     * @deprecated Use getDescription().
     */
    public String getCompanyDescription()
    {
        return getDescription();
    }

    /**
     * Get the company description.
     * 
     * @return The company description.
     */
    public String getDescription()
    {
        return m_companyDescription;
    }

    /**
     * Update the company's description;
     */
    public void setDescription(String p_newDescription)
    {
        m_companyDescription = p_newDescription;
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
        if (p_company instanceof Company) { return (getId() == ((Company) p_company)
                .getId()); }
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
        buff.append(m_companyDescription != null ? m_companyDescription
                : "null");
        return buff.toString();
    }

    public void setCompanyDescription(String description)
    {
        m_companyDescription = description;
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
    protected int getTmVersionVal() {
        return m_tmVersion;   
    }
    
    protected void setTmVersionVal(int tmVersion) {
        this.m_tmVersion = tmVersion;
    }
    
    /**
     * Public getter/setter to use from calling code.
     */
    public TmVersion getTmVersion() {
        return TmVersion.fromValue(m_tmVersion);
    }
    
    public void setTmVersion(TmVersion version) {
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
}
