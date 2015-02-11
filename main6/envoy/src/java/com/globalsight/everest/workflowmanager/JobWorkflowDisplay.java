package com.globalsight.everest.workflowmanager;

public class JobWorkflowDisplay
{
    private Workflow workflow;
    private boolean isWorkflowEditable;
    private String targetLocaleDisplayName;
    private int totalWordCount;
    private String stateBundleString;
    private String taskDisplayName;
    private String estimatedStartTimestamp;
    private String estimatedTranslateCompletionDateTimestamp;
    private String estimatedCompletionDateTimestamp;
    private String isUploading;
    
    public JobWorkflowDisplay(Workflow workflow){
        this.workflow = workflow;
    }
    
    public Workflow getWorkflow()
    {
        return workflow;
    }
    
    public boolean getIsWorkflowEditable()
    {
        return isWorkflowEditable;
    }

    public void setIsWorkflowEditable(boolean isWorkflowEditable)
    {
        this.isWorkflowEditable = isWorkflowEditable;
    }

    public String getTargetLocaleDisplayName()
    {
        return targetLocaleDisplayName;
    }

    public void setTargetLocaleDisplayName(String targetLocaleDisplayName)
    {
        this.targetLocaleDisplayName = targetLocaleDisplayName;
    }
    
    public int getTotalWordCount()
    {
        return totalWordCount;
    }

    public void setTotalWordCount(int totalWordCount)
    {
        this.totalWordCount = totalWordCount;
    }

    public String getStateBundleString()
    {
        return stateBundleString;
    }

    public void setStateBundleString(String stateBundleString)
    {
        this.stateBundleString = stateBundleString;
    }
    
    public String getTaskDisplayName()
    {
        return taskDisplayName;
    }

    public void setTaskDisplayName(String taskDisplayName)
    {
        this.taskDisplayName = taskDisplayName;
    }

    public String getEstimatedStartTimestamp()
    {
        return estimatedStartTimestamp;
    }
    
    public void setEstimatedStartTimestamp(String estimatedStartTimestamp)
    {
        this.estimatedStartTimestamp = estimatedStartTimestamp;
    }
    
    public String getEstimatedTranslateCompletionDateTimestamp()
    {
        return estimatedTranslateCompletionDateTimestamp;
    }
    
    public void setEstimatedTranslateCompletionDateTimestamp(
            String estimatedTranslateCompletionDateTimestamp)
    {
        this.estimatedTranslateCompletionDateTimestamp = estimatedTranslateCompletionDateTimestamp;
    }
    
    public String getEstimatedCompletionDateTimestamp()
    {
        return estimatedCompletionDateTimestamp;
    }
    
    public void setEstimatedCompletionDateTimestamp(
            String estimatedCompletionDateTimestamp)
    {
        this.estimatedCompletionDateTimestamp = estimatedCompletionDateTimestamp;
    }

	public void setIsUploading(String isUploading) {
		this.isUploading = isUploading;
	}

	public String getIsUploading() {
		return isUploading;
	}
}
