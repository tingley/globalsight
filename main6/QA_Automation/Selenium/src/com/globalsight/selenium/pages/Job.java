package com.globalsight.selenium.pages;

public class Job
{
    private String jobName = "";
    private String filePaths = "";
    private String fileProfiles = "";
    private String targetLocales = "";

    public String getJobName()
    {
        return jobName;
    }

    public void setJobName(String jobName)
    {
        this.jobName = jobName;
    }

    public String getFilePaths()
    {
        return filePaths;
    }

    public void setFilePaths(String filePaths)
    {
        this.filePaths = filePaths;
    }

    public String getFileProfiles()
    {
        return fileProfiles;
    }

    public void setFileProfiles(String fileProfiles)
    {
        this.fileProfiles = fileProfiles;
    }

    public String getTargetLocales()
    {
        return targetLocales;
    }

    public void setTargetLocales(String targetLocales)
    {
        this.targetLocales = targetLocales;
    }

}
