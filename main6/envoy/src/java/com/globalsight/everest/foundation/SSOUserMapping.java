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
package com.globalsight.everest.foundation;

import java.io.Serializable;

public class SSOUserMapping implements Serializable
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private long id = -1l;
    private long companyId = -1l;
    private String userId = null;
    private String ssoUserId = null;

    public SSOUserMapping()
    {
    }

    public String toString()
    {
        return companyId + " - " + userId + " - " + ssoUserId;
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public long getCompanyId()
    {
        return companyId;
    }

    public void setCompanyId(long companyId)
    {
        this.companyId = companyId;
    }

    public String getUserId()
    {
        return userId;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    public String getSsoUserId()
    {
        return ssoUserId;
    }

    public void setSsoUserId(String ssoUserId)
    {
        this.ssoUserId = ssoUserId;
    }

    /**
     * Compare if the objects are equal.
     */
    public boolean equals(Object o)
    {
        if (o == null || !(o instanceof SSOUserMapping))
            return false;

        SSOUserMapping other = (SSOUserMapping) o;
        return other.companyId == this.companyId
                && other.userId.equals(this.userId)
                && other.ssoUserId.equals(this.ssoUserId);
    }
}
