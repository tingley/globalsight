package com.globalsight.smartbox.bo;

/**
 * SMB configuration
 * 
 * @author leon
 * 
 */
public class SMBConfiguration
{
    private boolean useSMB;
    private String SMBInbox;
    private String SMBOutbox;
    private String SMBFailedbox;

    public SMBConfiguration(boolean useSMB, String SMBInbox, String SMBOutbox,
            String SMBFailedbox)
    {
        this.useSMB = useSMB;
        this.SMBInbox = SMBInbox;
        this.SMBOutbox = SMBOutbox;
        this.SMBFailedbox = SMBFailedbox;
    }

    public boolean getUseSMB()
    {
        return useSMB;
    }

    public void setUseSMB(boolean useSMB)
    {
        this.useSMB = useSMB;
    }

    public String getSMBInbox()
    {
        return SMBInbox;
    }

    public void setSMBInbox(String SMBInbox)
    {
        this.SMBInbox = SMBInbox;
    }

    public String getSMBOutbox()
    {
        return SMBOutbox;
    }

    public void setSMBOutbox(String SMBOutbox)
    {
        this.SMBOutbox = SMBOutbox;
    }

    public String getSMBFailedbox()
    {
        return SMBFailedbox;
    }

    public void setSMBFailedbox(String SMBFailedbox)
    {
        this.SMBFailedbox = SMBFailedbox;
    }
}
