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
package com.globalsight.everest.workflowmanager;

import org.apache.log4j.Logger;

import com.globalsight.everest.persistence.PersistentObject;

public class WorkflowStatePosts extends PersistentObject
{
    private static Logger c_logger = Logger.getLogger(WorkflowStatePosts.class);
    private static final long serialVersionUID = 8507246584782247051L;

    private String m_description;

    private String m_listenerURL;
    private String m_secretKey;
    private int m_timeoutPeriod;
    private int m_retryNumber;
    private String m_notifyEmail;
    private long m_companyId;

    public WorkflowStatePosts()
    {

    }

    public WorkflowStatePosts(String description,
            String listenerURL, String secretKey, int timeoutPeriod,
            int retryNumber, String notifyEmail, long companyId)
    {
        m_description = description;
        m_listenerURL = listenerURL;
        m_secretKey = secretKey;
        m_timeoutPeriod = timeoutPeriod;
        m_retryNumber = retryNumber;
        m_notifyEmail = notifyEmail;
        m_companyId = companyId;
    }

    public String getDescription()
    {
        return m_description;
    }

    public void setDescription(String m_description)
    {
        this.m_description = m_description;
    }

    public String getListenerURL()
    {
        return m_listenerURL;
    }

    public void setListenerURL(String m_listenerURL)
    {
        this.m_listenerURL = m_listenerURL;
    }

    public String getSecretKey()
    {
        return m_secretKey;
    }

    public void setSecretKey(String m_secretKey)
    {
        this.m_secretKey = m_secretKey;
    }

    public int getTimeoutPeriod()
    {
        return m_timeoutPeriod;
    }

    public void setTimeoutPeriod(int m_timeoutPeriod)
    {
        this.m_timeoutPeriod = m_timeoutPeriod;
    }

    public int getRetryNumber()
    {
        return m_retryNumber;
    }

    public void setRetryNumber(int m_retryNumber)
    {
        this.m_retryNumber = m_retryNumber;
    }

    public String getNotifyEmail()
    {
        return m_notifyEmail;
    }

    public void setNotifyEmail(String m_notifyEmail)
    {
        this.m_notifyEmail = m_notifyEmail;
    }

    public long getCompanyId()
    {
        return m_companyId;
    }

    public void setCompanyId(long m_companyId)
    {
        this.m_companyId = m_companyId;
    }
}
