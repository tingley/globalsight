package com.globalsight.connector.eloqua.form;

public class CreateEloquaForm
{
    private String jobName;
    private String comment;
    private String priority;
    private String fileMapFileProfile;
    private String attributeString;
    private String userName;

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
}
