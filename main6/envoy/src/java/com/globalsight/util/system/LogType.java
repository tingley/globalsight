package com.globalsight.util.system;

public enum LogType
{
    JOB("JOB"), WORKFLOW("WORKFLOW"), TASK("TASK");

    private String logType = "";

    LogType(String type)
    {
        logType = type;
    }

    public String getType()
    {
        return logType;
    }

    public void setType(String type)
    {
        logType = type;
    }
}
