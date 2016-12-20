package com.globalsight.everest.webapp.pagehandler.administration.remoteServices.perplexity;

import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.persistence.PersistentObject;

/**
 * <code>PerplexityService</code> is a persistent object. Will be saved to
 * database.
 * <p>
 * For GBS-4495 perplexity score on MT.
 */
public class PerplexityService extends PersistentObject
{
    private static final long serialVersionUID = -5514591142058131313L;

    private String name;
    private String userName;
    private String password;
    private String url;
    private long companyId;
    private String description;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public long getCompanyId()
    {
        return companyId;
    }

    public void setCompanyId(long companyId)
    {
        this.companyId = companyId;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getGsCompany()
    {
        return CompanyWrapper.getCompanyNameById(getCompanyId());
    }
}
