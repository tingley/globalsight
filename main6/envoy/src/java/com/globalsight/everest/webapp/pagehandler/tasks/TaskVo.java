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

import java.io.Serializable;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TimeZone;

import com.globalsight.everest.company.Company;
import com.globalsight.everest.foundation.Timestamp;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.StringUtil;

/**
 * TaskVo will be saved in session
 */
public class TaskVo implements Serializable
{
    private static final long serialVersionUID = 1L;

    private long taskId;
    private long jobId;
    private String jobName;
    private long workflowId;
    private long localeId;
    private long sourceLocaleId;
    private long targetLocaleId;
    private int wordCount;
    private Date estimatedCompletionDate;
    private String estimatedCompletionDateString;
    private Date acceptedDate;
    private Date completedDate;
    private Date estimatedAcceptanceDate;
    private String taskType;
    private int priority;
    private int state;
    private String sourceLocaleName;
    private String targetLocaleName;
    private boolean isOverdue;
    private String stateString;
    private String taskDateLabel;
    private String taskDateString;
    private long companyId;
    private String companyName;
    private String activityName;
    private String assignees;
    private String isUploading;
    
    public long getSourceLocaleId()
    {
        return sourceLocaleId;
    }

    public void setSourceLocaleId(long sourceLocaleId)
    {
        this.sourceLocaleId = sourceLocaleId;
    }

    public long getTargetLocaleId()
    {
        return targetLocaleId;
    }

    public void setTargetLocaleId(long targetLocaleId)
    {
        this.targetLocaleId = targetLocaleId;
    }

    public Date getAcceptedDate()
    {
        return acceptedDate;
    }

    public void setAcceptedDate(Date acceptedDate)
    {
        this.acceptedDate = acceptedDate;
    }

    public Date getCompletedDate()
    {
        return completedDate;
    }

    public void setCompletedDate(Date completedDate)
    {
        this.completedDate = completedDate;
    }

    public Date getEstimatedAcceptanceDate()
    {
        return estimatedAcceptanceDate;
    }

    public void setEstimatedAcceptanceDate(Date estimatedAcceptanceDate)
    {
        this.estimatedAcceptanceDate = estimatedAcceptanceDate;
    }

    public String getTaskType()
    {
        return taskType;
    }

    public void setTaskType(String taskType)
    {
        this.taskType = taskType;
    }

    public int getPriority()
    {
        return priority;
    }

    public void setPriority(int priority)
    {
        this.priority = priority;
    }

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

    public int getState()
    {
        return state;
    }

    public void setState(int state)
    {
        this.state = state;
    }

    public String getSourceLocaleName()
    {
        if (StringUtil.isNotEmpty(sourceLocaleName))
            return sourceLocaleName;

        if (sourceLocaleId > 0)
        {
            try
            {
                GlobalSightLocale locale = ServerProxy.getLocaleManager()
                        .getLocaleById(sourceLocaleId);
                sourceLocaleName = locale.getDisplayName();
            }
            catch (Exception e)
            {
                sourceLocaleName = "";
            }
        }

        return sourceLocaleName;
    }

    public void setSourceLocaleName(String sourceLocaleName)
    {
        this.sourceLocaleName = sourceLocaleName;
    }

    public String getTargetLocaleName()
    {
        if (StringUtil.isNotEmpty(targetLocaleName))
            return targetLocaleName;

        if (targetLocaleId > 0)
        {
            try
            {
                GlobalSightLocale locale = ServerProxy.getLocaleManager()
                        .getLocaleById(targetLocaleId);
                targetLocaleName = locale.getDisplayName();
            }
            catch (Exception e)
            {
                targetLocaleName = "";
            }
        }

        return targetLocaleName;
    }

    public void setTargetLocaleName(String targetLocaleName)
    {
        this.targetLocaleName = targetLocaleName;
    }

    public boolean isOverdue()
    {
        Date now = new Date();
        if ((state == Task.STATE_ACTIVE && estimatedAcceptanceDate != null && now
                .after(estimatedAcceptanceDate))
                || (state == Task.STATE_ACCEPTED
                        && estimatedCompletionDate != null && now
                            .after(estimatedCompletionDate)))
            isOverdue = true;
        else
            isOverdue = false;

        return isOverdue;
    }

    public void setOverdue(boolean isOverdue)
    {
        this.isOverdue = isOverdue;
    }

    public String getStateString()
    {
        return stateString;
    }

    public void setStateString(String stateString)
    {
        this.stateString = stateString;
    }

    public void setTaskDateString(Locale uiLocale, TimeZone timeZone, int searchState)
    {
        Timestamp ts = new Timestamp(timeZone);
        ts.setLocale(uiLocale);
        if(searchState == Task.STATE_ALL)
        {
        	ts.setDate(getEstimatedAcceptanceDate());
        }
        else
        {  	
        	switch (state)
        	{
	        	case Task.STATE_ACTIVE:
	        		ts.setDate(getEstimatedAcceptanceDate());
	        		taskDateLabel = "Accept By";
	        		break;
	        	case Task.STATE_COMPLETED:
	        		ts.setDate(getCompletedDate());
	        		taskDateLabel = "Due By";
	        		break;
	        	case Task.STATE_REJECTED:
	        		ts.setDate(getAcceptedDate());
	        		break;
	        	default:
	        		ts.setDate(getEstimatedCompletionDate());
	        		break;
        	}
        }
        taskDateString = ts.toString();
    }

    public void setTaskDateString(String taskDateString)
    {
        this.taskDateString = taskDateString;
    }

    public String getTaskDateLabel()
    {
        return taskDateLabel;
    }

    public void setTaskDateLabel(String taskDateLabel)
    {
        this.taskDateLabel = taskDateLabel;
    }

    public long getCompanyId()
    {
        return companyId;
    }

    public void setCompanyId(long companyId)
    {
        this.companyId = companyId;
        if (companyId > 0) {
            try
            {
                Company company = ServerProxy.getJobHandler().getCompanyById(companyId);
                this.companyName = company.getCompanyName();
            }
            catch (Exception e)
            {
                this.companyName = "";
            }
        }
    }

    public String getCompanyName()
    {
        return companyName;
    }

    public void setCompanyName(String companyName)
    {
        this.companyName = companyName;
    }

    public String getTaskDateString()
    {
        return taskDateString;
    }

    public String getEstimatedCompletionDateString()
    {
        return estimatedCompletionDateString;
    }

    public void setEstimatedCompletionDateString(
            Locale uiLocale, TimeZone timeZone)
    {
        if (estimatedCompletionDate != null) {
            Timestamp ts = new Timestamp(timeZone);
            ts.setLocale(uiLocale);
            ts.setDate(getEstimatedCompletionDate());
            
            this.estimatedCompletionDateString = ts.toString();
        }
    }

    public void setStateString(ResourceBundle bundle)
    {
        switch (state)
        {
            case Task.STATE_ACTIVE:
                stateString = bundle.getString("lb_available");
                break;
            case Task.STATE_ACCEPTED:
                stateString = bundle.getString("lb_inprogress");
                break;
            case Task.STATE_REJECTED:
                stateString = bundle.getString("lb_rejected");
                break;
            case Task.STATE_COMPLETED:
                stateString = bundle.getString("lb_finished");
                break;
            default:
                stateString = bundle.getString("lb_available");
                break;
        }
    }

    public String getActivityName()
    {
        return activityName;
    }

    public void setActivityName(String activityName)
    {
        this.activityName = activityName;
    }

    public void setEstimatedCompletionDateString(
            String estimatedCompletionDateString)
    {
        this.estimatedCompletionDateString = estimatedCompletionDateString;
    }

    public String getAssignees()
    {
        return assignees;
    }

    public void setAssignees(String assignees)
    {
        this.assignees = assignees;
    }

	public void setIsUploading(String isUploading) {
		this.isUploading = isUploading;
	}

	public String getIsUploading() {
		return isUploading;
	}
}
