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
package com.globalsight.connector.blaise.form;

public class CreateBlaiseJobForm
{
    private String jobName;
    private String comment;
    private String priority;
    private String fileMapFileProfile;
    private String attributeString;
    private String userName;
    private String blaiseConnectorId;
    private String combineByLangs;
    private String attachment;

    public String getJobName()
    {
        return jobName;
    }

    public void setJobName(String jobName)
    {
        this.jobName = jobName;
    }

    public String getComment()
    {
        return comment;
    }

    public void setComment(String comment)
    {
        this.comment = comment;
    }

    public String getPriority()
    {
        return priority;
    }

    public void setPriority(String priority)
    {
        this.priority = priority;
    }

    public String getFileMapFileProfile()
    {
        return fileMapFileProfile;
    }

    public void setFileMapFileProfile(String fileMapFileProfile)
    {
        this.fileMapFileProfile = fileMapFileProfile;
    }

    public String getAttributeString()
    {
        return attributeString;
    }

    public void setAttributeString(String attributeString)
    {
        this.attributeString = attributeString;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

	public String getBlaiseConnectorId()
	{
		return blaiseConnectorId;
	}

	public void setBlaiseConnectorId(String blaiseConnectorId)
	{
		this.blaiseConnectorId = blaiseConnectorId;
	}

    public String getCombineByLangs()
    {
        return combineByLangs;
    }

    public void setCombineByLangs(String combineByLangs)
    {
        this.combineByLangs = combineByLangs;
    }

    public String getAttachment()
    {
        return attachment;
    }

    public void setAttachment(String attachment)
    {
        this.attachment = attachment;
    }
}
