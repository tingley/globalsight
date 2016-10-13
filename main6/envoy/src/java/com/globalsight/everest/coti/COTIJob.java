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
package com.globalsight.everest.coti;

// GlobalSight
import com.globalsight.everest.coti.util.COTIConstants;

/**
 * This class only represent a wrapper object for the company names defined in
 * Envoy database and is used for defining a COTI job
 * 
 */
public class COTIJob
{
    // private long id;
    private String jobId;
    private String cotiProjectId;
    private String cotiProjectName;
    private String globalsightJobId;
    private String globalsightJobStatus;
    private String status;
    private String sourceLang;
    private String targetLang;
    private String companyId;
    private String creationDate;
    private String cotiPackageId;
    
    private String textType;
    
    public String getJobId()
    {
        return jobId;
    }

    public void setJobId(String jobId)
    {
        this.jobId = jobId;
    }

    public String getGlobalsightJobStatus()
    {
        return globalsightJobStatus;
    }

    public void setGlobalsightJobStatus(String globalsightJobStatus)
    {
        this.globalsightJobStatus = globalsightJobStatus;
    }

    public String getCompanyId()
    {
        return companyId;
    }

    public void setCompanyId(String companyId)
    {
        this.companyId = companyId;
    }

    public String getCreationDate()
    {
        return creationDate;
    }

    public void setCreationDate(String creationDate)
    {
        this.creationDate = creationDate;
    }

    public String getCotiProjectId()
    {
        return cotiProjectId;
    }

    public void setCotiProjectId(String cotiProjectId)
    {
        this.cotiProjectId = cotiProjectId;
    }

    public String getCotiProjectName()
    {
        return cotiProjectName;
    }

    public void setCotiProjectName(String cotiProjectName)
    {
        this.cotiProjectName = cotiProjectName;
    }

    public String getGlobalsightJobId()
    {
        return globalsightJobId;
    }

    /**
     * set this value after create GlobalSight job
     * 
     * @param globalsightJobId
     */
    public void setGlobalsightJobId(String globalsightJobId)
    {
        this.globalsightJobId = globalsightJobId;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getSourceLang()
    {
        return sourceLang;
    }

    public void setSourceLang(String sourceLang)
    {
        this.sourceLang = sourceLang;
    }

    public String getTargetLang()
    {
        return targetLang;
    }

    public void setTargetLang(String targetLang)
    {
        this.targetLang = targetLang;
    }
    
    public String getTextType()
    {
        return textType;
    }

    public void setTextType(String textType)
    {
        this.textType = textType;
    }

    public String getCotiPackageId()
    {
        return cotiPackageId;
    }

    public void setCotiPackageId(String cotiPackageId)
    {
        this.cotiPackageId = cotiPackageId;
    }

    public static boolean isWarningText(String state)
    {
        return state.equals(COTIConstants.project_status_unknown)
                || state.equals(COTIConstants.project_status_cancelled)
                || state.equals(COTIConstants.project_status_closed)
                || state.equals(COTIConstants.project_status_rejected);
    }
}
