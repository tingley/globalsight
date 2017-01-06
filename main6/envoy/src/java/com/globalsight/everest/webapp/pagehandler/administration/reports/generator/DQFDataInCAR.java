package com.globalsight.everest.webapp.pagehandler.administration.reports.generator;

import com.globalsight.everest.workflow.ScorecardScore;

import java.util.List;

/**
 * DQF data object used in Comment Analysis Report(CAR)
 */
public class DQFDataInCAR
{
    private long jobId;
    private String jobName;
    private long workflowId;
    private String fluencyScore;
    private String adequacyScore;
    private String dqfComment;
    private String scorecardComment;
    private List<ScorecardScore> scorecards;

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

    public long getWorkflowId()
    {
        return workflowId;
    }

    public void setWorkflowId(long workflowId)
    {
        this.workflowId = workflowId;
    }

    public String getFluencyScore()
    {
        return fluencyScore;
    }

    public void setFluencyScore(String fluencyScore)
    {
        this.fluencyScore = fluencyScore;
    }

    public String getAdequacyScore()
    {
        return adequacyScore;
    }

    public void setAdequacyScore(String adequacyScore)
    {
        this.adequacyScore = adequacyScore;
    }

    public String getDqfComment()
    {
        return dqfComment;
    }

    public void setDqfComment(String dqfComment)
    {
        this.dqfComment = dqfComment;
    }

    public String getScorecardComment()
    {
        return scorecardComment;
    }

    public void setScorecardComment(String scorecardComment)
    {
        this.scorecardComment = scorecardComment;
    }

    public List<ScorecardScore> getScorecards()
    {
        return scorecards;
    }

    public void setScorecards(List<ScorecardScore> scorecards)
    {
        this.scorecards = scorecards;
    }
}
