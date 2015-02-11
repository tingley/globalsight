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

package com.globalsight.everest.webapp.pagehandler.tasks;

import java.util.Date;

/**
 * TaskVo will be saved in session
 */
public class TaskVo
{
    private long taskId;
    private long jobId;
    private long workflowId;
    private long localeId;
    private String jobName;
    private int wordCount;
    private Date estimatedCompletionDate;
    public long getJobId()
    {
        return jobId;
    }
    public void setJobId(long jobId)
    {
        this.jobId = jobId;
    }
    public String getJobName()
    {
        return jobName;
    }
    public void setJobName(String jobName)
    {
        this.jobName = jobName;
    }
    public int getWordCount()
    {
        return wordCount;
    }
    public void setWordCount(int wordCount)
    {
        this.wordCount = wordCount;
    }
    public Date getEstimatedCompletionDate()
    {
        return estimatedCompletionDate;
    }
    public void setEstimatedCompletionDate(Date estimatedCompletionDate)
    {
        this.estimatedCompletionDate = estimatedCompletionDate;
    }
    public long getTaskId()
    {
        return taskId;
    }
    public void setTaskId(long taskId)
    {
        this.taskId = taskId;
    }
    public long getWorkflowId()
    {
        return workflowId;
    }
    public void setWorkflowId(long workflowId)
    {
        this.workflowId = workflowId;
    }
    public long getLocaleId()
    {
        return localeId;
    }
    public void setLocaleId(long localeId)
    {
        this.localeId = localeId;
    }
    
}
