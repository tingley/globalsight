package com.globalsight.connector.git.form;

import java.util.ArrayList;
import java.util.List;

import com.globalsight.cxe.entity.gitconnector.GitConnector;

public class GitConnectorFilter
{
    private String nameFilter;
    private String urlFilter;
    private String usernameFilter;
    private String companyNameFilter;
    private String descriptionFilter;
    private String branchFilter;
    private String emailFilter;

    public List<GitConnector> filter(List<GitConnector> conns)
    {
        List<GitConnector> result = new ArrayList<GitConnector>();
        
        for (GitConnector conn : conns)
        {
            if (!like(nameFilter, conn.getName()))
            {
                continue;
            }

            if (!like(urlFilter, conn.getUrl()))
            {
                continue;
            }

            if (!like(usernameFilter, conn.getUsername()))
            {
                continue;
            }

            if (!like(companyNameFilter, conn.getCompanyName()))
            {
                continue;
            }
            
            if (!like(descriptionFilter, conn.getDescription()))
            {
                continue;
            }
            
            if (!like(branchFilter, conn.getBranch()))
            {
                continue;
            }
            
            if (!like(emailFilter, conn.getEmail()))
            {
                continue;
            }
            
            result.add(conn);
        }
        
        return result;
    }

    private boolean like(String filterValue, String candidateValue)
    {
        if (filterValue == null)
            return true;

        filterValue = filterValue.trim();
        if (filterValue.length() == 0)
            return true;

        if (candidateValue == null)
            return false;

        filterValue = filterValue.toLowerCase();
        candidateValue = candidateValue.toLowerCase();

        return candidateValue.indexOf(filterValue) > -1;
    }

    public String getNameFilter()
    {
        return nameFilter;
    }

    public void setNameFilter(String nameFilter)
    {
        this.nameFilter = nameFilter;
    }

    public String getDescriptionFilter()
    {
        return descriptionFilter;
    }

    public void setDescriptionFilter(String descriptionFilter)
    {
        this.descriptionFilter = descriptionFilter;
    }

    public String getUrlFilter()
    {
        return urlFilter;
    }

    public void setUrlFilter(String urlFilter)
    {
        this.urlFilter = urlFilter;
    }

    public String getUsernameFilter()
    {
        return usernameFilter;
    }

    public void setUsernameFilter(String usernameFilter)
    {
        this.usernameFilter = usernameFilter;
    }

    public String getCompanyNameFilter()
    {
        return companyNameFilter;
    }

    public void setCompanyNameFilter(String companyNameFilter)
    {
        this.companyNameFilter = companyNameFilter;
    }

	public void setBranchFilter(String branchFilter) {
		this.branchFilter = branchFilter;
	}

	public String getBranchFilter() {
		return branchFilter;
	}

	public String getEmailFilter() {
		return emailFilter;
	}

	public void setEmailFilter(String emailFilter) {
		this.emailFilter = emailFilter;
	}

    
}
