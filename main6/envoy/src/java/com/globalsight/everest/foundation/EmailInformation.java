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

import java.util.Locale;
import java.util.TimeZone;

import com.globalsight.util.resourcebundle.LocaleWrapper;

/**
 * The EmailInformation contains the following information:
 * 1. User's email address (displayed as Joe Smith <joe@domain.com>).
 * 2. User's default UI locale (used for message)
 * 3. cc email address and bcc email address.
 */
public class EmailInformation
    implements java.io.Serializable
{
    private String m_userId = null;
    private String m_userFullName = null;
    private String m_userEmail = null;
    private String m_ccEmail = null;
    private String m_bccEmail = null;
    private String m_userDefaultLocaleString = null;
    private Locale m_userDefaultLocale = null;
    // this value is from the user's calendar
    private TimeZone m_userTimeZone = null;
    
    //this for automatic action, the email will send to the address which 
    //is assigned in automatic action.
    private String autoActionEmailAddress = null;
    private boolean isAutoAction = false;
    private String companyName = null;
    
    /**
     * Constructor used after a successful login.
     * @param p_userId - The user's id.
     * @param p_userFullName - The user's full name.
     * @param p_userEmail - The user's email address.
     * @param p_uiLocale - The user's default email locale.
     * @param p_timeZone - The user's time zone.
     * @param p_isNotificationEnabled - Determines whether the notification
     *                                  should be enabled for the user.
     */
    public EmailInformation(String p_userId,
                            String p_userFullName,
                            String p_userEmail, 
                            String p_uiLocale,
                            TimeZone p_timeZone)
    {
        StringBuffer sb = new StringBuffer();
        sb.append(p_userFullName == null ? "" : p_userFullName);
        sb.append("<");
        sb.append(p_userEmail);
        sb.append(">");

        m_userId = p_userId;
        m_userFullName = p_userFullName;
        m_userEmail = sb.toString();
        m_userDefaultLocaleString = p_uiLocale;
        m_userTimeZone = p_timeZone;        
    }

    
    /**
     * Get the user email.
     * @return The user email address.
     */
    public String getEmailAddress()
    {
        return m_userEmail;
    }
    
    /**
     * Get cc email address.
     */
    public String getCCEmailAddress()
    {
    	return m_ccEmail;
    }
    
    /**
     * Set cc email address.
     */
    public void setCCEmailAddress(String p_ccEmail)
    {
    	m_ccEmail = p_ccEmail;
    }
    
    /**
     * Get bcc email address.
     */
    public String getBCCEmailAddress()
    {
    	return m_bccEmail;
    }
    
    /**
     * Set bcc email address.
     */
    public void setBCCEmailAddress(String p_bccEmail)
    {
    	m_bccEmail = p_bccEmail;
    }
    /**
     * Get user's email locale.
     * @return The user's email used for email's subject and body.
     */
    public String getEmailLocaleAsString()
    {
        return m_userDefaultLocaleString;
    }

    /**
     * Get user's email locale as Locale object.
     */
    public Locale getEmailLocale()
    {
        if (m_userDefaultLocale == null)
        {
            m_userDefaultLocale = LocaleWrapper.getLocale(
                m_userDefaultLocaleString);
        }

        return m_userDefaultLocale;
    }

    /**
     * Get the user id.
     */
    public String getUserId()
    {
        return m_userId;
    }

    /**
     * Get the user's full name.
     */
    public String getUserFullName()
    {
        return m_userFullName;
    }

    /**
     * Get the user's time zone (based on the user's calendar).
     * @return The user's time zone.
     */
    public TimeZone getUserTimeZone()
    {
        return m_userTimeZone;
    }

    public String toString()
    {
        return getEmailAddress();
    }
    
    public boolean isAutoAction()
    {
        return isAutoAction;
    }

    public void setIsAutoAction(boolean flag)
    {
        isAutoAction = flag;
    }
    
    public String getAutoActionEmailAddress()
    {
        return autoActionEmailAddress;
    }

    public void setAutoActionEmailAddress(String email)
    {
        autoActionEmailAddress = email;
    }
    
    public String getCompanyName()
    {
        return companyName;
    }

    public void setCompanyName(String companyName)
    {
        this.companyName = companyName;
    }
}
