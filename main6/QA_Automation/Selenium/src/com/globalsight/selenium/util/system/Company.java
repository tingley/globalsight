package com.globalsight.selenium.util.system;

import java.util.Hashtable;

public class Company
{
    private String name = "";
    private String adminUser = "";
    private String adminPassword = "";
    private String pmUser = "";
    private String pmPassword = "";
    private String anyoneUser = "";
    private String anyonePassword = "";
    private String reviewerUser = "";
    private String reviewerPassword = "";
    private String defaultLocale = "";
    private Hashtable<String, String> targetLocales = new Hashtable<String, String>();
    private Hashtable<String, String> localePairs = new Hashtable<String, String>();

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getAdminUser()
    {
        return adminUser;
    }

    public void setAdminUser(String adminUser)
    {
        this.adminUser = adminUser;
    }

    public String getAdminPassword()
    {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword)
    {
        this.adminPassword = adminPassword;
    }

    public String getPmUser()
    {
        return pmUser;
    }

    public void setPmUser(String pmUser)
    {
        this.pmUser = pmUser;
    }

    public String getPmPassword()
    {
        return pmPassword;
    }

    public void setPmPassword(String pmPassword)
    {
        this.pmPassword = pmPassword;
    }

    public String getAnyoneUser()
    {
        return anyoneUser;
    }

    public void setAnyoneUser(String anyoneUser)
    {
        this.anyoneUser = anyoneUser;
    }

    public String getAnyonePassword()
    {
        return anyonePassword;
    }

    public void setAnyonePassword(String anyonePassword)
    {
        this.anyonePassword = anyonePassword;
    }

    public String getReviewerUser()
    {
        return reviewerUser;
    }

    public void setReviewerUser(String reviewerUser)
    {
        this.reviewerUser = reviewerUser;
    }

    public String getReviewerPassword()
    {
        return reviewerPassword;
    }

    public void setReviewerPassword(String reviewerPassword)
    {
        this.reviewerPassword = reviewerPassword;
    }

    public String getDefaultLocale()
    {
        return defaultLocale;
    }

    public void setDefaultLocale(String defaultLocale)
    {
        this.defaultLocale = defaultLocale;
    }

    public Hashtable<String, String> getTargetLocales()
    {
        return targetLocales;
    }

    public void setTargetLocales(Hashtable<String, String> targetLocales)
    {
        this.targetLocales = targetLocales;
    }

    public Hashtable<String, String> getLocalePairs()
    {
        return localePairs;
    }

    public void setLocalePairs(Hashtable<String, String> localePairs)
    {
        this.localePairs = localePairs;
    }

}
