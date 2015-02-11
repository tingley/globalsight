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
package com.globalsight.everest.tm.searchreplace;

import java.io.Serializable;

public class JobInfo implements Serializable
{
    private static final long serialVersionUID = 4397183201348982743L;

    private long m_jobId;
    private String m_jobName;
    private TargetLocaleInfo m_targetLocaleInfo;
    private TargetPageInfo m_targetPageInfo;
    private TuvInfo m_tuvInfo;

    public JobInfo()
    {
    }

    public void setJobId(long p_jobId)
    {
        m_jobId = p_jobId;
    }

    public void setJobName(String p_jobName)
    {
        m_jobName = p_jobName;
    }

    public void setTargetLocaleInfo(TargetLocaleInfo p_targetLocaleInfo)
    {
        m_targetLocaleInfo = p_targetLocaleInfo;
    }

    public TargetLocaleInfo getTargetLocaleInfo()
    {
        return m_targetLocaleInfo;
    }

    public void setTargetPageInfo(TargetPageInfo p_targetPageInfo)
    {
        m_targetPageInfo = p_targetPageInfo;
    }

    public TargetPageInfo getTargetPageInfo()
    {
        return m_targetPageInfo;
    }

    public void setTuvInfo(TuvInfo p_tuvInfo)
    {
        m_tuvInfo = p_tuvInfo;
    }

    public TuvInfo getTuvInfo()
    {
        return m_tuvInfo;
    }

    public long getJobId()
    {
        return m_jobId;
    }
    
    public String getJobName()
    {
        return m_jobName;
    }

}
