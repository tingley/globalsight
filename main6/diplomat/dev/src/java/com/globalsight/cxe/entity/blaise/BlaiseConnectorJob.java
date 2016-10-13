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

import com.globalsight.everest.persistence.PersistentObject;

public class BlaiseConnectorJob extends PersistentObject
{
	private static final long serialVersionUID = -933803792285804414L;

	// Available values for "uploadXliffState" and "completeState".
	public static String FAIL = "fail";
	public static String SUCCEED = "succeed";

	private long blaiseConnectorId;
	private long blaiseEntryId;
    private long jobId;
    private String uploadXliffState;
    private String completeState;

    public long getBlaiseConnectorId()
	{
		return blaiseConnectorId;
	}

	public void setBlaiseConnectorId(long blaiseConnectorId)
	{
		this.blaiseConnectorId = blaiseConnectorId;
	}

	public long getBlaiseEntryId()
	{
		return blaiseEntryId;
	}

	public void setBlaiseEntryId(long blaiseEntryId)
	{
		this.blaiseEntryId = blaiseEntryId;
	}

	public void setJobId(long jobId)
	{
		this.jobId = jobId;
	}
	
	public long getJobId()
	{
		return jobId;
	}

    public String getUploadXliffState()
    {
        return uploadXliffState;
    }

    public void setUploadXliffState(String uploadXliffState)
    {
        this.uploadXliffState = uploadXliffState;
    }

    public String getCompleteState()
    {
        return completeState;
    }

    public void setCompleteState(String completeState)
    {
        this.completeState = completeState;
    }
}
