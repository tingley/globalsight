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

    // Utility
    public String getCompanyName()
    {
        return CompanyWrapper.getCompanyNameById(companyId);
    }
}
