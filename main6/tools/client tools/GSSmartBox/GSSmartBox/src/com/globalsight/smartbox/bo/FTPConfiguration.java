package com.globalsight.smartbox.bo;

/**
 * FTP configuration
 * 
 * @author leon
 * 
 */
public class FTPConfiguration
{
    private boolean useFTP;
    private String ftpHost;
    private String ftpUsername;
    private String ftpPassword;
    private int ftpPort;
    private String ftpInbox;
    private String ftpOutbox;
    private String ftpFailedbox;

    public FTPConfiguration(boolean useFTP, String ftpHost, int ftpPort,
            String ftpUsername, String ftpPassword, String ftpInbox,
            String ftpOutbox, String ftpFailedbox)
    {
        this.useFTP = useFTP;
        this.ftpHost = ftpHost;
        this.ftpUsername = ftpUsername;
        this.ftpPassword = ftpPassword;
        this.ftpInbox = ftpInbox;
        this.ftpOutbox = ftpOutbox;
        this.ftpFailedbox = ftpFailedbox;
        this.setFtpPort(ftpPort);
    }

    public boolean getUseFTP()
    {
        return useFTP;
    }

    public void setUseFTP(boolean useFTP)
    {
        this.useFTP = useFTP;
    }

    public String getFtpHost()
    {
        return ftpHost;
    }

    public void setFtpHost(String ftpHost)
    {
        this.ftpHost = ftpHost;
    }

    public String getFtpUsername()
    {
        return ftpUsername;
    }

    public void setFtpUsername(String ftpUsername)
    {
        this.ftpUsername = ftpUsername;
    }

    public String getFtpPassword()
    {
        return ftpPassword;
    }

    public void setFtpPassword(String ftpPassword)
    {
        this.ftpPassword = ftpPassword;
    }

    public String getFtpInbox()
    {
        return ftpInbox;
    }

    public void setFtpInbox(String ftpInbox)
    {
        this.ftpInbox = ftpInbox;
    }

    public String getFtpOutbox()
    {
        return ftpOutbox;
    }

    public void setFtpOutbox(String ftpOutbox)
    {
        this.ftpOutbox = ftpOutbox;
    }

    public String getFtpFailedbox()
    {
        return ftpFailedbox;
    }

    public void setFtpFailedbox(String ftpFailedbox)
    {
        this.ftpFailedbox = ftpFailedbox;
    }

    public int getFtpPort()
    {
        return ftpPort;
    }

    public void setFtpPort(int ftpPort)
    {
        if (ftpPort > 0)
        {
            this.ftpPort = ftpPort;
        }
    }
}
