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

package com.globalsight.everest.edit;

import java.util.Date;
import java.io.Serializable;

/**
 * <p>Data class holding information on page upload status for
 * synchronizing offline uploads with the online editor.</p>
 */
public class SynchronizationStatus
    implements Serializable
{
    static public final String UNKNOWN =
        SynchronizationManager.UNKNOWN;
    static public final String UPLOAD_STARTED =
        SynchronizationManager.UPLOAD_STARTED;
    static public final String UPLOAD_FINISHED =
        SynchronizationManager.UPLOAD_FINISHED;

    static public final String GXMLUPDATE_STARTED =
        SynchronizationManager.GXMLUPDATE_STARTED;
    static public final String GXMLUPDATE_FINISHED =
        SynchronizationManager.GXMLUPDATE_FINISHED;

    private Long m_pageId;
    private String m_status;
    private long m_timestamp;

    public SynchronizationStatus()
    {
    }

    public SynchronizationStatus(Long p_pageId, long p_timestamp,
        String p_status)
    {
        m_pageId = p_pageId;
        m_status = p_status;
        m_timestamp = p_timestamp;
    }

    public SynchronizationStatus(SynchronizationStatus p_other)
    {
        m_pageId = p_other.m_pageId;
        m_status = p_other.m_status;
        m_timestamp = p_other.m_timestamp;
    }

    public void setPageId(Long p_arg)
    {
        m_pageId = p_arg;
    }

    public Long getPageId()
    {
        return m_pageId;
    }

    public void setStatus(String p_arg)
    {
        m_status = p_arg;
    }

    public String getStatus()
    {
        return m_status;
    }

    public void setTimestamp(long p_arg)
    {
        m_timestamp = p_arg;
    }

    public long getTimestamp()
    {
        return m_timestamp;
    }

    public String toString()
    {
        StringBuffer result = new StringBuffer();

        result.append("SyncStatus page ");
        result.append(m_pageId);
        result.append(" ");
        result.append(m_status);
        result.append(" (");
        result.append(new Date(m_timestamp));
        result.append(")");

        return result.toString();
    }
}
