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

import java.io.Serializable;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.everest.usermgr.UserInfo;

/**
 * UserImpl implemented the User interface. Holds the user information (except
 * for roles).
 */
public class UserImpl extends PersistentObject implements User, Serializable
{
    private static final long serialVersionUID = -3038450248168515060L;

    private String m_userId = null;

    private int m_state = State.CREATED;

    private String m_userName = null;

    private String m_firstName = null;

    private String m_lastName = null;

    private String m_title = null;

    private String m_companyName = null;

    private String m_password = null;

    private String m_email = null;

    private String m_ccEmail = null;

    private String m_bccEmail = null;

    private String m_address = null;
    
    private String wssePassword = null;

//    private Hashtable m_phoneNumbers = new Hashtable();
    private String officePhoneNumber;
    private String homePhoneNumber;
    private String cellPhoneNumber;
    private String faxPhoneNumber;

    private String m_defaultLocale = null;

    private boolean isPasswordSet = false;

    // default is GlobalSight
    private int m_type = UserType.GLOBALSIGHT;

    // specifies whether the user is part of all current AND
    // future projects
    private boolean m_isInAllProjects = false;
    
    private Set<ContainerRoleImpl> containerRoles = new HashSet<ContainerRoleImpl>();

    public String getProjectNames()
    {
        return projectNames;
    }

    public void setProjectNames(String projectNames)
    {
        this.projectNames = projectNames == null ? "" : projectNames;
    }

    public String getPermissiongNames()
    {
        return permissiongNames;
    }

    public void setPermissiongNames(String permissiongNames)
    {
        this.permissiongNames = permissiongNames == null ? ""
                : permissiongNames;
    }

    private String projectNames = "";
    private String permissiongNames = "";

    public UserImpl()
    {
    }

    /** Gets the user's name for display in a locale sensitive manner. */
    public String getDisplayName(Locale p_locale)
    {
        // Just assume all locales need "firstname lastname" for now.
        // The logic to print names differently for Chinese can be
        // added here later.
        return getFullName();
    }

    public String getUserId()
    {
        return m_userId;
    }

    public void setUserId(String p_userId)
    {
        m_userId = p_userId;
    }

    public int getState()
    {
        return m_state;
    }

    public boolean isActive()
    {
        if (m_state == User.State.ACTIVE)
            return true;
        else
            return false;
    }

    public void setState(int p_userState)
    {
        // tbd - check if state change is valid
        m_state = p_userState;
    }

    public String getUserName()
    {
        return m_userName;
    }

    public void setUserName(String p_userName)
    {
        m_userName = p_userName;
    }

    public String getFirstName()
    {
        return m_firstName;
    }

    public void setFirstName(String p_firstName)
    {
        m_firstName = p_firstName;
    }

    public String getLastName()
    {
        return m_lastName;
    }

    public void setLastName(String p_lastName)
    {
        m_lastName = p_lastName;
    }

    public String getPassword()
    {
        return m_password;
    }

    public void setPassword(String p_password)
    {
        m_password = p_password;
    }

    public String getWssePassword()
    {
        return wssePassword;
    }

    public void setWssePassword(String wssePassword)
    {
        this.wssePassword = wssePassword;
    }

    public String getEmail()
    {
        return m_email;
    }

    public void setEmail(String p_email)
    {
        m_email = p_email;
    }

    public String getCCEmail()
    {
        return m_ccEmail;
    }

    public void setCCEmail(String p_ccEmail)
    {
        m_ccEmail = p_ccEmail;
    }

    public String getBCCEmail()
    {
        return m_bccEmail;
    }

    public void setBCCEmail(String p_bccEmail)
    {
        m_bccEmail = p_bccEmail;
    }
    
    public String getCcEmail()
    {
        return m_ccEmail;
    }

    public void setCcEmail(String p_ccEmail)
    {
        m_ccEmail = p_ccEmail;
    }

    public String getBccEmail()
    {
        return m_bccEmail;
    }

    public void setBccEmail(String p_bccEmail)
    {
        m_bccEmail = p_bccEmail;
    }

    public String getAddress()
    {
        return m_address;
    }

    public void setAddress(String p_address)
    {
        m_address = p_address;
    }

    public String getDefaultUILocale()
    {
        return (m_defaultLocale == null || m_defaultLocale.length() == 0) ? "en_US"
                : m_defaultLocale;
    }

    public void setDefaultUILocale(String p_defaultLocale)
    {
        m_defaultLocale = p_defaultLocale;
    }
    
    //used for hibernate
    public String getDefaultLocale()
    {
        return m_defaultLocale;
    }

    //used for hibernate
    public void setDefaultLocale(String p_defaultLocale)
    {
        m_defaultLocale = p_defaultLocale;
    }

    public String getTitle()
    {
        return m_title;
    }

    public void setTitle(String p_title)
    {
        m_title = p_title;
    }

    public String getCompanyName()
    {
        return m_companyName;
    }

    public void setCompanyName(String p_companyName)
    {
        m_companyName = p_companyName;
    }

    /**
     * Validates the User object: the UserId can not be empty.
     */
    public boolean isUserValid()
    {
        return m_userId != null && (!"".equals(m_userId.trim()));
    }

    /**
     * @see User.isInAllProjects(boolean)
     */
    public void isInAllProjects(boolean p_inAllProjects)
    {
        m_isInAllProjects = p_inAllProjects;
    }

    /**
     * @see User.isInAllProjects()
     */
    public boolean isInAllProjects()
    {
        return m_isInAllProjects;
    }

    /**
     * Return the type of user this is.
     */
    public int getType()
    {
        return m_type;
    }

    /**
     * Set the type of user this is. Should only be used when reading from
     * storage (LDAP) and shouldn't be changed back and forth.
     */
    public void setType(int p_type)
    {
        m_type = p_type;
    }

    /**
     * Override toString method for displaying a user object.
     * 
     * @return The string representation of a User object.
     */
    public String toString()
    {
        return getUserName();
    }

    /**
     * This method is used only for debugging.
     */
    public String toDebugString()
    {
        return super.toString() + " m_userId="
                + (m_userId != null ? m_userId : "null") + " m_isActive="
                + (isActive() == true ? "true" : "false") + " m_firstName="
                + (m_firstName != null ? m_firstName : "null") + " m_lastName="
                + (m_lastName != null ? m_lastName : "null") + " m_password="
                + (m_password != null ? m_password : "null") + " m_email="
                + (m_email != null ? m_email : "null") + " m_address="
                + (m_address != null ? m_address : "null") + " m_defaultLocale="
                + (m_defaultLocale != null ? m_defaultLocale : "null")
                + " m_isInAllProjects="
                + (m_isInAllProjects ? "true" : "false");
    }

    // Since a "user name" is not being set through UI (or even behind
    // the scene), we'll create it as LDAP does it.
    private String getFullName()
    {
        StringBuffer sb = new StringBuffer();

        if (m_firstName != null)
        {
            sb.append(m_firstName);
            sb.append(" ");
        }

        if (m_lastName != null)
        {
            sb.append(m_lastName);
        }

        return sb.toString();
    }

    public boolean isPasswordSet()
    {
        return isPasswordSet;
    }

    public void setPasswordSet(boolean passwordSet)
    {
        this.isPasswordSet = passwordSet;
    }

    public String getSpecialNameForEmail()
    {
        StringBuffer sb = new StringBuffer();

        if (m_userName != null)
        {
            sb.append(m_userName);
            sb.append(" ");
        }

        sb.append("(");

        if (m_firstName != null)
        {
            sb.append(m_firstName);
            sb.append(" ");
        }

        if (m_lastName != null)
        {
            sb.append(m_lastName);
        }

        sb.append(")");

        return sb.toString();
    }

	public String getOfficePhoneNumber() 
	{
		return officePhoneNumber;
	}

	public void setOfficePhoneNumber(String officePhoneNumber) 
	{
		this.officePhoneNumber = officePhoneNumber;
	}

	public String getHomePhoneNumber() {
		return homePhoneNumber;
	}

	public void setHomePhoneNumber(String homePhoneNumber) 
	{
		this.homePhoneNumber = homePhoneNumber;
	}

	public String getCellPhoneNumber() 
	{
		return cellPhoneNumber;
	}

	public void setCellPhoneNumber(String cellPhoneNumber) 
	{
		this.cellPhoneNumber = cellPhoneNumber;
	}

	public String getFaxPhoneNumber() 
	{
		return faxPhoneNumber;
	}

	public void setFaxPhoneNumber(String faxPhoneNumber) 
	{
		this.faxPhoneNumber = faxPhoneNumber;
	}

	public Set<ContainerRoleImpl> getContainerRoles() 
	{
		return containerRoles;
	}

	public void setContainerRoles(Set<ContainerRoleImpl> containerRoles) 
	{
		this.containerRoles = containerRoles;
	}
	
	public UserInfo toUserInfo()
	{
		UserInfo ui = new UserInfo();
		ui.setUserId(getUserId());
		ui.setUserName(getUserName());
		ui.setTitle(getTitle());
		ui.setFirstName(getFirstName());
		ui.setLastName(getLastName());
		ui.setEmailAddress(getEmail());
		ui.setCCEmailAddress(getCcEmail());
		ui.setBCCEmailAddress(getBccEmail());
		ui.isInAllProjects(isInAllProjects());

		return ui;
	}
}
