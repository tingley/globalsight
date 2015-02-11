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
package com.globalsight.ling.tm;

import java.util.Date;

import com.globalsight.util.GlobalSightLocale;

/**
 * This class is used for saving basic information for TUV(both tm2 and tm3)
 * 
 * @author leon
 * 
 */
public class TuvBasicInfo
{
    private String segmentString;
    private String segmentClob;
    private String exactMatchKey;
    private GlobalSightLocale locale;
    private Date creationDate;
    private String creationUser;
    private Date modifyDate;
    private String modifyUser;
    private String updatedByProject;
    private String sid;

    public TuvBasicInfo(String segmentString, String segmentClob,
            String exactMatchKey, GlobalSightLocale locale, Date creationDate,
            String creationUser, Date modifyDate, String modifyUser,
            String updatedByProject, String sid)
    {
        this.segmentString = segmentString;
        this.segmentClob = segmentClob;
        this.exactMatchKey = exactMatchKey;
        this.locale = locale;
        this.creationDate = creationDate;
        this.creationUser = creationUser;
        this.modifyDate = modifyDate;
        this.modifyUser = modifyUser;
        this.updatedByProject = updatedByProject;
        this.sid = sid;
    }

    public String getSegmentString()
    {
        return segmentString;
    }

    public void setSegmentString(String segmentString)
    {
        this.segmentString = segmentString;
    }

    public String getSegmentClob()
    {
        return segmentClob;
    }

    public void setSegmentClob(String segmentClob)
    {
        this.segmentClob = segmentClob;
    }

    public String getExactMatchKey()
    {
        return exactMatchKey;
    }

    public void setExactMatchKey(String exactMatchKey)
    {
        this.exactMatchKey = exactMatchKey;
    }

    public GlobalSightLocale getLocale()
    {
        return locale;
    }

    public void setLocale(GlobalSightLocale locale)
    {
        this.locale = locale;
    }

    public Date getCreationDate()
    {
        return creationDate;
    }

    public void setCreationDate(Date creationDate)
    {
        this.creationDate = creationDate;
    }

    public String getCreationUser()
    {
        return creationUser;
    }

    public void setCreationUser(String creationUser)
    {
        this.creationUser = creationUser;
    }

    public Date getModifyDate()
    {
        return modifyDate;
    }

    public void setModifyDate(Date modifyDate)
    {
        this.modifyDate = modifyDate;
    }

    public String getModifyUser()
    {
        return modifyUser;
    }

    public void setModifyUser(String modifyUser)
    {
        this.modifyUser = modifyUser;
    }

    public String getUpdatedByProject()
    {
        return updatedByProject;
    }

    public void setUpdatedByProject(String updatedByProject)
    {
        this.updatedByProject = updatedByProject;
    }

    public String getSid()
    {
        return sid;
    }

    public void setSid(String sid)
    {
        this.sid = sid;
    }
}
