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
package com.globalsight.everest.webapp.pagehandler.administration.reports;

import java.util.ArrayList;
import java.util.List;

/**
 * Job Info for Report
 * 
 * @author Leon
 * 
 */
public class ReportJobInfo
{
    private String jobId = "";
    private String jobName = "";
    private String jobState = "";

    private String locProfile = "";
    private String projectId = "";

    private List<String> targetLocales = new ArrayList<String>();
    
    public ReportJobInfo(){}

    public ReportJobInfo(String jobId, String jobName, String jobState,
            String locProfile,
            String projectId, List<String> targetLocales)
    {
        this.jobId = jobId;
        this.jobName = jobName;
        this.jobState = jobState;
        this.locProfile = locProfile;
        this.projectId = projectId;
        this.targetLocales = targetLocales;
    }

    public String getJobId()
    {
        return jobId;
    }

    public void setJobId(String jobId)
    {
        this.jobId = jobId;
    }

    public String getJobName()
    {
        return jobName;
    }

    public void setJobName(String jobName)
    {
        this.jobName = jobName;
    }

    public String getJobState()
    {
        return jobState;
    }

    public void setJobState(String jobStatus)
    {
        this.jobState = jobStatus;
    }

    public String getLocProfile()
    {
        return locProfile;
    }

    public void setLocProfile(String locProfile)
    {
        this.locProfile = locProfile;
    }

    public String getProjectId()
    {
        return projectId;
    }

    public void setProjectId(String projectId)
    {
        this.projectId = projectId;
    }

    public List<String> getTargetLocales()
    {
        return targetLocales;
    }
    
    /**
     * Get the target locales list as String.
     * @return ID1,ID2,ID3
     */
    public String getTargetLocalesStr()
    {
        StringBuffer result = new StringBuffer();
        for(String str : targetLocales)
        {
            result.append(str).append(",");
        }
        
        return result.substring(0, result.length()-1);
    }

    public void setTargetLocales(List<String> targetLocales)
    {
        this.targetLocales = targetLocales;
    }

    public void addTargetLocale(String p_targetLocale)
    {
        if(!targetLocales.contains(p_targetLocale))
        {
            targetLocales.add(p_targetLocale);
        }
    }
}
