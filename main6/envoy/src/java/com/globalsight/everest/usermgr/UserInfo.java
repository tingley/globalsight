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

/**
 * 
 * The UserInfo class is a more light-weight object than the User class. It
 * contains basic information about a user to be used by the UI, reporting or
 * API.
 */
public class UserInfo implements java.io.Serializable
{
    // basic user information
    private String m_userId = null;
    private String m_userName = null;
    private String m_title = null;
    private String m_firstName = null;
    private String m_lastName = null;
    private int m_state = User.State.CREATED;
    private String m_emailAddress = null;
    private String m_ccEmailAddress = null;
    private String m_bccEmailAddress = null;
    // default is FALSE
    private boolean m_isInAllProjects = false;
    private String defaultUILocale = null;

    /**
     * Default Constructor
     */
    public UserInfo()
    {
    }

    /**
     * Constructor to set initial values.
     * 
     * @param p_userId
     *            - The user's id.
     * @param p_firstName
     *            - The user's first name.
     * @param p_lastName
     *            - The user's last name.
     */
    public UserInfo(String p_userId, String p_firstName, String p_lastName,
            boolean p_isInAllProjects)
    {
        this(p_userId, null, p_firstName, p_lastName, p_isInAllProjects);
    }

    /**
     * Constructor to set initial values.
     * 
     * @param p_userId
     * @param p_title
     * @param p_firstName
     * @param p_lastName
     */
    public UserInfo(String p_userId, String p_title, String p_firstName,
            String p_lastName, boolean p_isInAllProjects)
    {
        m_userId = p_userId;
        m_title = p_title;
        m_firstName = p_firstName;
        m_lastName = p_lastName;
        m_isInAllProjects = p_isInAllProjects;
    }

    /**
     * Constructor to set initial values.
     */
    public UserInfo(String p_userId, String p_title, String p_firstName,
            String p_lastName, boolean p_isInAllProjects, int p_state)
    {
        m_userId = p_userId;
        m_title = p_title;
        m_firstName = p_lastName;
        m_lastName = p_lastName;
        m_isInAllProjects = p_isInAllProjects;
        m_state = p_state;
    }

    /**
     * Create a user info from the information in the User object.
     */
    public UserInfo(User p_user)
    {
        m_userId = p_user.getUserId();
        m_userName = p_user.getUserName();
        m_title = p_user.getTitle();
        m_firstName = p_user.getFirstName();
        m_lastName = p_user.getLastName();
        m_emailAddress = p_user.getEmail();
        m_ccEmailAddress = p_user.getCCEmail();
        m_bccEmailAddress = p_user.getBCCEmail();
        m_isInAllProjects = p_user.isInAllProjects();
        m_state = p_user.getState();
        defaultUILocale = p_user.getDefaultUILocale();
    }

    public String getUserId()
    {
        return m_userId;
    }

    public void setUserId(String p_userId)
    {
        m_userId = p_userId;
    }

    public String getUserName()
    {
        return m_userName;
    }

    public void setUserName(String p_userName)
    {
        m_userName = p_userName;
    }

    public String getTitle()
    {
        return m_title;
    }

    public void setTitle(String p_title)
    {
        m_title = p_title;
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

    /**
     * Get the user's full name
     */
    public String getFullName()
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

    public String getEmailAddress()
    {
        return m_emailAddress;
    }

    public void setEmailAddress(String p_email)
    {
        m_emailAddress = p_email;
    }

    public String getCCEmailAddress()
    {
        return m_ccEmailAddress;
    }

    public void setCCEmailAddress(String p_ccEmail)
    {
        m_ccEmailAddress = p_ccEmail;
    }

    public String getBCCEmailAdress()
    {
        return m_bccEmailAddress;
    }

    public void setBCCEmailAddress(String p_bccEmail)
    {
        m_bccEmailAddress = p_bccEmail;
    }

    /**
     * Sets the value to specify if the user should be added to all projects
     * (current and future) or not. 'true' means they should, 'false' means they
     * shouldn'
     * 
     * @param p_inAllProjects
     *            'true' the user should be part of all current and future
     *            projects. 'false' the user shouldn't be part of all future
     *            projects.
     */
    public void isInAllProjects(boolean p_inAllProjects)
    {
        m_isInAllProjects = p_inAllProjects;
    }

    /**
     * Get the value to whether the user is part of all projects and should be
     * added to all new project that is created.
     * 
     * @return 'true' - the user is part of all current and future projects.
     *         'false - the user is not to be added to all future projects.
     */
    public boolean isInAllProjects()
    {
        return m_isInAllProjects;
    }

    public void setState(int p_state)
    {
        m_state = p_state;
    }

    public int getState()
    {
        return m_state;
    }

    public String toString()
    {
        return getFullName();
    }

    public String getDefaultUILocale()
    {
        return defaultUILocale;
    }

    public void setDefaultUILocale(String p_local)
    {
        defaultUILocale = p_local;
    }
}
