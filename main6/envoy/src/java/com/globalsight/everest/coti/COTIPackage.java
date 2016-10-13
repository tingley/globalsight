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
 * Envoy database and is used for defining a COTI package
 * 
 */
public class COTIPackage extends PersistentObject
{
    private String fileName;
    private String cotiProjectName;
    private String cotiProjectTimestamp;
    private long companyId;
    private Date creationDate;

    public String getFileName()
    {
        return fileName;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    public String getCotiProjectName()
    {
        return cotiProjectName;
    }

    public void setCotiProjectName(String cotiProjectName)
    {
        this.cotiProjectName = cotiProjectName;
    }

    public String getCotiProjectTimestamp()
    {
        return cotiProjectTimestamp;
    }

    public void setCotiProjectTimestamp(String cotiProjectTimestamp)
    {
        this.cotiProjectTimestamp = cotiProjectTimestamp;
    }

    public long getCompanyId()
    {
        return companyId;
    }

    public void setCompanyId(long companyId)
    {
        this.companyId = companyId;
    }

    public Date getCreationDate()
    {
        return creationDate;
    }

    public void setCreationDate(Date creationDate)
    {
        this.creationDate = creationDate;
    }
}
