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

package com.globalsight.everest.webapp.pagehandler.administration.users;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.globalsight.calendar.UserFluxCalendar;
import com.globalsight.everest.costing.Rate;
import com.globalsight.everest.foundation.Role;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.foundation.UserRole;
import com.globalsight.everest.securitymgr.FieldSecurity;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.usermgr.UserManager;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.util.GeneralException;

/**
 * Helper class used by pagehandlers to contain code used to create new users,
 * assign them to the appropriate groups, and generate all their user roles.
 */
public class CreateUserWrapper
{
    // used for logging errors
    private static final Logger c_logger = Logger
            .getLogger(CreateUserWrapper.class);

    protected UserManager m_userMgr = null;

    protected User m_user = null;

    protected User m_userPerformingAction = null;

    protected Vector m_roles = null;

    protected List m_projects = null;

    protected FieldSecurity m_fieldSecurity = null;

    protected UserFluxCalendar m_calendar = null;

    protected boolean promptIsActive = true;

    protected Hashtable m_sourceTargetMap = null;

    protected ArrayList m_defaultRoles = null;

    protected HashMap m_defaultRolesHash = null;

    protected String m_ssoUserId = null;

    /**
     * Default constructor.
     */
    public CreateUserWrapper(UserManager p_userMgr, User p_userRequestingCreate)
    {
        m_userMgr = p_userMgr;
        m_userPerformingAction = p_userRequestingCreate;

        // Get a new User object from the factory.
        m_user = m_userMgr.createUser();

        // Instantiate group and role collections.
        m_roles = new Vector();

        m_sourceTargetMap = new Hashtable();
    }

    /**
     * Generates a batch of user roles from a source/target locale pair, and a
     * Hashtable of activity-cost pairs.
     * 
     * @param p_sourceLocale
     *            <code>Locale.toString()</code> for the valid source locale.
     * @param p_targetLocale
     *            <code>Locale.toString()</code> for the valid target locale.
     * @param p_activityCostMap
     *            A Hashtable containing String names of activities as keys, and
     *            Float values of costs for those activities.
     */
    public void addUserRoles(String p_sourceLocale, String p_targetLocale,
            Hashtable p_activityCostMap) throws EnvoyServletException
    {
        Enumeration eKeys = p_activityCostMap.keys();
        while (eKeys.hasMoreElements())
        {
            try
            {
                // Pull activity name/cost pair out of the hashtable.
                Activity curKey = (Activity) eKeys.nextElement();
                Vector params = (Vector) p_activityCostMap.get(curKey);
                String activity = (String) params.elementAt(0);

                // Get a new UserRole from the factory, and fill out its fields
                // with the data passed.
                UserRole userRole = m_userMgr.createUserRole();
                ((Role) userRole).setActivity(curKey);
                ((Role) userRole).setSourceLocale(p_sourceLocale);
                ((Role) userRole).setTargetLocale(p_targetLocale);
                userRole.setUserName(m_user.getUserName());
                if (UserUtil.isJobCostingEnabled())
                {
                    long expense = Long.parseLong((String) params.elementAt(1));

                    Rate expenseRate;
                    expenseRate = (Rate) ServerProxy.getCostingEngine()
                            .getRate(expense);
                    if (expenseRate != null)
                    {
                        userRole.setRate((new Long(expenseRate.getId()))
                                .toString());
                    }
                    else
                    {
                        userRole.setRate("-1");
                    }
                    ((Role) userRole).addRate(expenseRate);
                }

                // Iterate through the role list, and if a role with this
                // activity/source/target exists, remove it.
                Enumeration eRoles = m_roles.elements();
                while (eRoles.hasMoreElements())
                {
                    UserRole curRole = (UserRole) eRoles.nextElement();

                    Activity curActivity = ((Role) curRole).getActivity();
                    String curSource = ((Role) curRole).getSourceLocale();
                    String curTarget = ((Role) curRole).getTargetLocale();

                    if (curActivity.equals(curKey)
                            && curSource.equalsIgnoreCase(p_sourceLocale)
                            && curTarget.equalsIgnoreCase(p_targetLocale))
                    {
                        m_roles.remove(curRole);
                    }
                }

                // Now add the new role to the Vector we're maintaining.
                m_roles.addElement(userRole);
            }
            catch (GeneralException ge)
            {
                throw new EnvoyServletException(ge);
            }
            catch (RemoteException re)
            {
                throw new EnvoyServletException(GeneralException.EX_REMOTE, re);
            }
        }

        Vector vTargets = (Vector) m_sourceTargetMap.get(p_sourceLocale + "="
                + getCurCompanyId());

        if (vTargets == null)
        {
            vTargets = new Vector();
            m_sourceTargetMap.put(p_sourceLocale + "=" + getCurCompanyId(),
                    vTargets);
        }

        if (!(vTargets.contains(p_targetLocale)))
        {
            vTargets.addElement(p_targetLocale);
        }
    }

    /**
     * Takes the Locale.toString() of a source locale; returns a Vector
     * containing the Locale.toStrings() of all the currently-used targets for
     * that source.
     */
    public Hashtable getSourceTargetMap()
    {
        return m_sourceTargetMap;
    }

    /**
     * Called to commit the contents of the wrapper back to LDAP, using the
     * UserManager we've held onto.
     */
    public void commitWrapper() throws EnvoyServletException
    {
        try
        {
            // Add the user object and its groups and roles
            m_userMgr.addUser(m_userPerformingAction, m_user, m_projects,
                    m_fieldSecurity, new ArrayList(m_roles));

            ServerProxy.getCalendarManager().createUserCalendar(m_calendar,
                    m_user.getUserId());
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Set the first address line field on the user object.
     */
    public void setAddress(String p_address)
    {
        m_user.setAddress(p_address);
    }

    /**
     * Set the default UI locale field on the user object.
     */
    public void setDefaultUILocale(String p_locale)
    {
        m_user.setDefaultUILocale(p_locale);
    }

    /**
     * Set the email field on the user object.
     */
    public void setEmail(String p_email)
    {
        m_user.setEmail(p_email);
        if (p_email == null || p_email.equals(""))
        {
            promptIsActive = false;
        }
    }

    /**
     * Set the cc email field on the user object.
     */
    public void setCCEmail(String p_ccEmail)
    {
        m_user.setCCEmail(p_ccEmail);
    }

    /**
     * Set the bcc email field on the user object.
     */
    public void setBCCEmail(String p_bccEmail)
    {
        m_user.setBCCEmail(p_bccEmail);
    }

    /**
     * Sets the first name field on the user object.
     */
    public void setFirstName(String p_firstName)
    {
        m_user.setFirstName(p_firstName);
    }

    /**
     * Sets the last name on the user object.
     */
    public void setLastName(String p_lastName)
    {
        m_user.setLastName(p_lastName);
    }

    /**
     * Set the password field on the user object.
     */
    public void setPassword(String p_password)
    {
        m_user.setPassword(p_password);
        m_user.setPasswordSet(true);
        if (p_password == null || p_password.equals(""))
        {
            promptIsActive = false;
        }
    }

    /**
     * Set the title field on the user object.
     */
    public void setTitle(String p_title)
    {
        m_user.setTitle(p_title);
    }
    
    public void setWssePassword(String p_pwd)
    {
        m_user.setWssePassword(p_pwd);
    }

    /**
     * Set the company field on the user object.
     */
    public void setCompanyName(String p_companyName)
    {
        m_user.setCompanyName(p_companyName);
    }
    
    /**
     * Sets the userId field on the User object.
     */
    public void setUserId(String p_userId)
    {
        m_user.setUserId(p_userId);
    }

    public void setUserName(String p_userName)
    {
        m_user.setUserName(p_userName);
    }

    /**
     * Sets the projects.
     */
    public void setProjects(List p_projects)
    {
        this.m_projects = p_projects;
    }

    /**
     * Set if user is added to all projects.
     */
    public void setIsInAllProjects(boolean p_isInAllProjects)
    {
        m_user.isInAllProjects(p_isInAllProjects);
    }

    /**
     * Set field level security.
     */
    public void setFieldSecurity(FieldSecurity p_fieldSecurity)
    {
        this.m_fieldSecurity = p_fieldSecurity;
    }

    /**
     * Set the user calendar.
     */
    public void setCalendar(UserFluxCalendar p_calendar)
    {
        m_calendar = p_calendar;
    }

    public void setSsoUserId(String p_ssoUserId)
    {
        m_ssoUserId = p_ssoUserId;
    }

    public String getSsoUserId()
    {
        return m_ssoUserId;
    }

    /**
     * Get the user id.
     */
    public String getUserId()
    {
        return m_user.getUserId();
    }

    public String getUserName()
    {
        return m_user.getUserName();
    }

    /**
     * Gets the first name field on the user object.
     */
    public String getFirstName()
    {
        return m_user.getFirstName();
    }

    /**
     * Gets the last name field on the user object.
     */
    public String getLastName()
    {
        return m_user.getLastName();
    }

    /**
     * Gets the password field on the user object.
     */
    public String getPassword()
    {
        return m_user.getPassword();
    }

    /**
     * Used for determining if the UI should ask the user if he wants to
     * continue with an inactive user. Otherwise, it might ask on every edit
     * user page. Instead ask only once.
     */
    public boolean promptIsActive()
    {
        return promptIsActive;
    }

    /**
     * Return the address field on the user object.
     */
    public String getAddress()
    {
        return m_user.getAddress();
    }

    /**
     * Return the default UI locale field on the user object.
     */
    public String getDefaultUILocale()
    {
        return m_user.getDefaultUILocale();
    }

    /**
     * Return the email field on the user object.
     */
    public String getEmail()
    {
        return m_user.getEmail();
    }

    /**
     * Return the cc email field on the user object.
     */
    public String getCCEmail()
    {
        return m_user.getCCEmail();
    }

    /**
     * Return the bcc email field on the user object.
     */
    public String getBCCEmail()
    {
        return m_user.getBCCEmail();
    }

    /**
     * Gets the title.
     */
    public String getTitle()
    {
        return m_user.getTitle();
    }
    
    public String getWssePassword()
    {
        return m_user.getWssePassword();
    }

    /**
     * Gets the company name.
     */
    public String getCompanyName()
    {
        return m_user.getCompanyName();
    }

    /**
     * Gets the projects.
     */
    public List getProjects()
    {
        return m_projects;
    }

    /**
     * Gets value that specifies if user should be added to all projects.
     */
    public boolean isInAllProjects()
    {
        return m_user.isInAllProjects();
    }

    /**
     * Returns the user that is being created.
     */
    public User getUser()
    {
        return m_user;
    }

    /**
     * Returns field level security.
     */
    public FieldSecurity getFieldSecurity()
    {
        return m_fieldSecurity;
    }

    /**
     * Gets all the roles that have the specified source and target locale.
     */
    public Vector getRoles(String p_sourceLocale, String p_targetLocale)
    {
        Vector retVal = new Vector();

        for (int i = 0; i < m_roles.size(); i++)
        {
            Role curRole = (Role) m_roles.get(i);
            if (p_sourceLocale.equalsIgnoreCase(curRole.getSourceLocale())
                    && p_targetLocale.equalsIgnoreCase(curRole
                            .getTargetLocale()))
            {
                retVal.addElement(curRole);
            }
        }

        return retVal;
    }

    private String curCompanyId;

    /**
     * @return Returns the curCompanyId.
     */
    public String getCurCompanyId()
    {
        return curCompanyId;
    }

    /**
     * @param curCompanyId
     *            The curCompanyId to set.
     */
    public void setCurCompanyId(String curCompanyId)
    {
        this.curCompanyId = curCompanyId;
    }
    
    public String getOfficePhoneNumber() 
	{
		return m_user.getOfficePhoneNumber();
	}

	public void setOfficePhoneNumber(String officePhoneNumber) 
	{
		m_user.setOfficePhoneNumber(officePhoneNumber);
	}

	public String getHomePhoneNumber() 
	{
		return m_user.getHomePhoneNumber();
	}

	public void setHomePhoneNumber(String homePhoneNumber) 
	{
		m_user.setHomePhoneNumber(homePhoneNumber);
	}

	public String getCellPhoneNumber() 
	{
		return m_user.getCellPhoneNumber();
	}

	public void setCellPhoneNumber(String cellPhoneNumber) 
	{
		m_user.setCellPhoneNumber(cellPhoneNumber);
	}

	public String getFaxPhoneNumber() 
	{
		return m_user.getFaxPhoneNumber();
	}

	public void setFaxPhoneNumber(String faxPhoneNumber) 
	{
		m_user.setFaxPhoneNumber(faxPhoneNumber);
	}
}
