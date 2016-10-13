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

import java.util.Date;

import com.globalsight.everest.persistence.PersistentObject;

/**
 * This class only represent a wrapper object for the company names defined in
 * Envoy database and is used for defining a COTI project
 * 
 */
public class COTIProject extends PersistentObject
{
    private String dirName;
    private long packageId;
    private String cotiProjectId;
    private String cotiProjectName;
    private long globalsightJobId;
    private String status;
    private String sourceLang;
    private String targetLang;

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

    public long getGlobalsightJobId()
    {
        return globalsightJobId;
    }

    /**
     * set this value after create GlobalSight job
     * @param globalsightJobId
     */
    public void setGlobalsightJobId(long globalsightJobId)
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

    public String getDirName()
    {
        return dirName;
    }

    public void setDirName(String dirName)
    {
        this.dirName = dirName;
    }

    public long getPackageId()
    {
        return packageId;
    }

    public void setPackageId(long packageId)
    {
        this.packageId = packageId;
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
}
