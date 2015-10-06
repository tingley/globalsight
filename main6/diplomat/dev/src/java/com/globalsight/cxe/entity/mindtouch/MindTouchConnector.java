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
package com.globalsight.cxe.entity.mindtouch;

import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.persistence.PersistentObject;

public class MindTouchConnector extends PersistentObject 
{
    private static final long serialVersionUID = 3955253539878774299L;

    private String name = null;
    private String description = null;
    private String url = null;
    private String username = null;
    private String password = null;
    private long companyId;
    private boolean isPostToSourceServer = false;

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

    public long getCompanyId()
    {
        return companyId;
    }

    public void setCompanyId(long companyId)
    {
        this.companyId = companyId;
    }
    
    public boolean getIsPostToSourceServer() 
    {
		return isPostToSourceServer;
	}

	public void setIsPostToSourceServer(boolean isPostToSourceServer) 
	{
		this.isPostToSourceServer = isPostToSourceServer;
	}

    // Utility
    public String getCompanyName()
    {
        return CompanyWrapper.getCompanyNameById(getCompanyId());
    }
}
