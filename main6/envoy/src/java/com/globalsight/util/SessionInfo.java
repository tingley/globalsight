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

package com.globalsight.util;

import java.util.Date;

import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;

/**
 * <p>
 * SessionInfo collects user-related information to make it accessible to lower
 * terminology layers.
 * </p>
 */
public class SessionInfo
{
    //
    // Private Member Variables
    //
    private String m_userName = null; // actually user id
    private String m_userRole = null;
    private Date m_timestamp = new Date();

    private String sourceLan = "";
    private String targetLan = "";

    //
    // Constructors
    //
    public SessionInfo(String p_user, String p_role)
    {
        m_userName = p_user;
        m_userRole = p_role;
    }

    public String getUserName()
    {
        return m_userName;
    }

    public String getUserDisplayName()
    {
        return UserUtil.getUserNameById(m_userName);
    }

    public String getUserRole()
    {
        return m_userRole;
    }

    /**
     * Sets the timestamp to NOW.
     */
    public void setTimestamp()
    {
        m_timestamp.setTime(System.currentTimeMillis());
    }

    /**
     * Retrieves the current timestamp value.
     */
    public Date getTimestamp()
    {
        return m_timestamp;
    }

    public String getSourceLan()
    {
        return sourceLan;
    }

    public void setSourceLan(String sourceLan)
    {
        this.sourceLan = sourceLan;
    }

    public String getTargetLan()
    {
        return targetLan;
    }

    public void setTargetLan(String targetLan)
    {
        this.targetLan = targetLan;
    }
}
