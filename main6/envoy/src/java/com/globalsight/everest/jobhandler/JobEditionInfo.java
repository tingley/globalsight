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
package com.globalsight.everest.jobhandler;

import com.globalsight.everest.persistence.PersistentObject;

public class JobEditionInfo extends PersistentObject
{
	private static final long serialVersionUID = -5375715533597529121L;

	private String jobId = null;
	private String originalTaskId = null;
	private String url = null;
	private String userName = null;
	private String password = null;
	private String sending_back_status = "begin";
	
	public JobEditionInfo()
	{
	}
	
	public JobEditionInfo(String p_jobId, String p_originalTaskId, 
                          String p_url, String p_userName, String p_password)
	{
		this.jobId = p_jobId;
		this.originalTaskId = p_originalTaskId;
		this.url = p_url;
		this.userName = p_userName;
		this.password = p_password;
	}
	
	public String getJobId() 
	{
		return jobId;
	}
	
	public void setJobId(String jobId) 
	{
		this.jobId = jobId;
	}
	
	public String getOriginalTaskId() 
	{
		return originalTaskId;
	}
	
	public void setOriginalTaskId(String originalTaskId) 
	{
		this.originalTaskId = originalTaskId;
	}
	
	public String getUrl() 
	{
		return url;
	}
	
	public void setUrl(String url) 
	{
		this.url = url;
	}
	
	public String getUserName() 
	{
		return userName;
	}
	
	public void setUserName(String userName) 
	{
		this.userName = userName;
	}
	
	public String getPassword() 
	{
		return password;
	}
	
	public void setPassword(String password) 
	{
		this.password = password;
	}

    public String getSendingBackStatus() 
    {
        return sending_back_status;
    }
    
    public void setSendingBackStatus(String status) 
    {
        this.sending_back_status = status;
    }
}
