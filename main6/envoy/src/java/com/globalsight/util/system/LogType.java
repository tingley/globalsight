package com.globalsight.util.system;

public enum LogType
{
    JOB("JOB"), FILEPROFILE("File Profile"), TMProfile("Translation Memory Profile");

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
