package com.globalsight.entity;

import java.io.File;
import java.util.List;

import com.globalsight.util.UsefulTools;

public class User
{
    private String name = null;

    private String pwd = null;

    private Host host = null;

    private boolean useSSL = false;

    private String savepath = null;

    private String minutes = null;

    private boolean autoDownload = true;

    private String companyName = "";

    private User[] downloadUsers = null;

    public static String defaultSavePath = UsefulTools.getUserHome() + File.separator
            + "DesktopIcon-download";

    public static int defaultMs = 10;

    public static String defaultMinutes = "" + defaultMs;

    public User(String p_name, String p_pwd, Host p_host)
    {
        name = p_name;
        pwd = p_pwd;
        host = p_host;
        savepath = defaultSavePath;
        minutes = defaultMinutes;
    }

    public User(String p_username, String p_pwd, Host p_host, String p_savepath, String p_minutes,
            boolean p_autodown)
    {
        name = p_username;
        pwd = p_pwd;
        host = p_host;
        savepath = p_savepath;
        minutes = p_minutes;
        autoDownload = p_autodown;
    }

    public Host getHost()
    {
        return host;
    }

    public String getPassword()
    {
        return pwd;
    }

    public String getName()
    {
        return name;
    }

    public boolean isUseSSL()
    {
        return useSSL;
    }

    public void setUseSSL(boolean useSSL)
    {
        this.useSSL = useSSL;
    }

    public boolean equals(Object anObj)
    {
        if (anObj == this)
        {
            return true;
        }

        if (anObj instanceof User)
        {
            User u = (User) anObj;
            return (u.name.equals(this.name) && u.host.equals(this.host));
        }

        return false;
    }

    public void setMinutes(String minutes)
    {
        this.minutes = minutes;
    }

    public void setPassword(String pwd)
    {
        this.pwd = pwd;
    }

    public void setSavepath(String savepath)
    {
        this.savepath = savepath;
    }

    public String toString()
    {
        return name + " (" + host + (useSSL? " with https" : "") + ")";
    }

    public String getMinutes()
    {
        return minutes;
    }

    public String getSavepath()
    {
        return savepath;
    }

    public boolean isAutoDownload()
    {
        return autoDownload;
    }

    public void setAutoDownload(boolean autoDownload)
    {
        this.autoDownload = autoDownload;
    }

    public String getCompanyName()
    {
        return companyName;
    }

    public void setCompanyName(String companyName)
    {
        this.companyName = companyName;
    }

    /**
     * get downloadUsers, will filter by company name
     * 
     * @return
     */
    public User[] getDownloadUsers()
    {
        verifyCompanyName();
        return downloadUsers;
    }

    /**
     * set downloadUsers, will filter by company name
     * 
     * @param downloadUsers
     */
    public void setDownloadUsers(User[] downloadUsers)
    {
        this.downloadUsers = downloadUsers;
        verifyCompanyName();
    }

    public boolean isSuperUser()
    {
        return "welocalize".equalsIgnoreCase(this.getCompanyName());
    }

    private void verifyCompanyName()
    {
        if (isSuperUser())
        {
            return;
        }

        int count = 0;
        for (int i = 0; i < downloadUsers.length; i++)
        {
            User user = downloadUsers[i];
            if (user.getCompanyName().equals(this.companyName))
            {
                count++;
            }
        }

        if (count != downloadUsers.length)
        {
            User[] newArray = new User[count];
            count = 0;
            for (int i = 0; i < downloadUsers.length; i++)
            {
                User user = downloadUsers[i];
                if (user.getCompanyName().equals(this.companyName))
                {
                    newArray[count] = user;
                    count++;
                }
            }
        }
    }
}
