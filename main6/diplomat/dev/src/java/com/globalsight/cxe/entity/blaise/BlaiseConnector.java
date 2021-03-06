/**
 * Copyright 2009 Welocalize, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * <p>
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.globalsight.cxe.entity.blaise;

import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.persistence.PersistentObject;

public class BlaiseConnector extends PersistentObject
{
    private static final long serialVersionUID = -7876757344723844737L;

    private String name = null;
    private String description = null;
    private String url = null;
    private String username = null;
    private String password = null;
    private String clientCoreVersion = null;
    private int clientCoreRevision = 0;
    private String workflowId = "TRANSLATION";
    private long companyId;
    private boolean automatic = false;
    private String pullDays = "";
    private String userPullDays = "";
    private int pullHour = 7;
    private int userPullHour = 7;
    private int minProcedureWords = 600;
    private long defaultFileProfileId = -1L;
    private long jobAttributeGroupId = -1L;
    private boolean combined = true;
    private long lastMaxEntryId = -1L;
    private String loginUser = "";
    private int qaCount = 0;
    private int checkDuration = 0;
    private String userCalendar = "";

    public String getUserCalendar()
    {
        return userCalendar;
    }

    public void setUserCalendar(String userCalendar)
    {
        this.userCalendar = userCalendar;
    }

    public String getUserPullDays()
    {
        return userPullDays;
    }

    public void setUserPullDays(String userPullDays)
    {
        this.userPullDays = userPullDays;
    }

    public int getUserPullHour()
    {
        return userPullHour;
    }

    public void setUserPullHour(int userPullHour)
    {
        this.userPullHour = userPullHour;
    }

    public int getCheckDuration()
    {
        return checkDuration;
    }

    public void setCheckDuration(int checkDuration)
    {
        this.checkDuration = checkDuration;
    }

    public int getQaCount()
    {
        return qaCount;
    }

    public void setQaCount(int qaCount)
    {
        this.qaCount = qaCount;
    }

    public String getLoginUser()
    {
        return loginUser;
    }

    public void setLoginUser(String loginUser)
    {
        this.loginUser = loginUser;
    }

    public long getLastMaxEntryId()
    {
        return lastMaxEntryId;
    }

    public void setLastMaxEntryId(long lastMaxEntryId)
    {
        this.lastMaxEntryId = lastMaxEntryId;
    }

    public String getPullDays()
    {
        return pullDays;
    }

    public void setPullDays(String pullDays)
    {
        this.pullDays = pullDays;
    }

    public int getMinProcedureWords()
    {
        return minProcedureWords;
    }

    public void setMinProcedureWords(int minProcedureWords)
    {
        this.minProcedureWords = minProcedureWords;
    }

    public long getDefaultFileProfileId()
    {
        return defaultFileProfileId;
    }

    public void setDefaultFileProfileId(long defaultFileProfileId)
    {
        this.defaultFileProfileId = defaultFileProfileId;
    }

    public long getJobAttributeGroupId()
    {
        return jobAttributeGroupId;
    }

    public void setJobAttributeGroupId(long jobAttributeGroupId)
    {
        this.jobAttributeGroupId = jobAttributeGroupId;
    }

    public boolean isCombined()
    {
        return combined;
    }

    public void setCombined(boolean isCombined)
    {
        this.combined = isCombined;
    }

    public boolean isAutomatic()
    {
        return automatic;
    }

    public void setAutomatic(boolean isAutomatic)
    {
        this.automatic = isAutomatic;
    }

    public int getPullHour()
    {
        return pullHour;
    }

    public void setPullHour(int pullHour)
    {
        this.pullHour = pullHour;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getClientCoreVersion()
    {
        return clientCoreVersion;
    }

    public void setClientCoreVersion(String clientCoreVersion)
    {
        this.clientCoreVersion = clientCoreVersion;
    }

    public int getClientCoreRevision()
    {
        return clientCoreRevision;
    }

    public void setClientCoreRevision(int clientCoreRevision)
    {
        this.clientCoreRevision = clientCoreRevision;
    }

    public String getWorkflowId()
    {
        return workflowId;
    }

    public void setWorkflowId(String workflowId)
    {
        this.workflowId = workflowId;
    }

    public long getCompanyId()
    {
        return companyId;
    }

    public void setCompanyId(long companyId)
    {
        this.companyId = companyId;
    }

    public String getCompanyName()
    {
        return CompanyWrapper.getCompanyNameById(companyId);
    }
}
