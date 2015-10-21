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

import com.globalsight.everest.persistence.PersistentObject;

public class MindTouchConnectorTargetServer extends PersistentObject 
{
	private static final long serialVersionUID = -6949621562709924834L;
	
	private String targetLocale = null;
    private String url = null;
    private String username = null;
    private String password = null;
    private long sourceServerId = 0;
    private long companyId;
    
    public String getTargetLocale() 
    {
		return targetLocale;
	}

	public void setTargetLocale(String targetLocale)
	{
		this.targetLocale = targetLocale;
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

	public long getSourceServerId() 
	{
		return sourceServerId;
	}

	public void setSourceServerId(long sourceServerId) 
	{
		this.sourceServerId = sourceServerId;
	}

	public long getCompanyId()
    {
        return companyId;
    }

    public void setCompanyId(long companyId)
    {
        this.companyId = companyId;
    }
}
