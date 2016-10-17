/**
 *  Copyright 2009-2016 Welocalize, Inc. 
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
 */
package com.globalsight.everest.workflow;

/**
 * DQF data which is used to be shown in job detail page
 * 
 * @author VincentYan
 * @since 8.7.2
 */
public class DQFData
{
    private long workflowId = -1l;
    private String targetLocale = "";
    private String fluency = "";
    private String adequacy = "";
    private String comment = "";
    
    public long getWorkflowId()
    {
        return workflowId;
    }
    public void setWorkflowId(long workflowId)
    {
        this.workflowId = workflowId;
    }
    public String getTargetLocale()
    {
        return targetLocale;
    }
    public void setTargetLocale(String targetLocale)
    {
        this.targetLocale = targetLocale;
    }
    public String getFluency()
    {
        return fluency;
    }
    public void setFluency(String fluency)
    {
        this.fluency = fluency;
    }
    public String getAdequacy()
    {
        return adequacy;
    }
    public void setAdequacy(String adequacy)
    {
        this.adequacy = adequacy;
    }
    public String getComment()
    {
        return comment;
    }
    public void setComment(String comment)
    {
        this.comment = comment;
    }
}
