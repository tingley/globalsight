package com.globalsight.connector.eloqua.form;

import java.util.ArrayList;
import java.util.List;

import com.globalsight.cxe.entity.eloqua.EloquaConnector;

public class EloquaConnectFilter
{
    private String nameFilter;
    private String companyFilter;
    private String companyNameFilter;
    private String urlFilter;
    private String descriptionFilter;
    
    public List<EloquaConnector> filter(List<EloquaConnector> conns)
    {
        List<EloquaConnector> result = new ArrayList<EloquaConnector>();
        
        for (EloquaConnector conn : conns)
        {
            if (!like(nameFilter, conn.getName()))
            {
                continue;
            }
            
            if (!like(companyFilter, conn.getCompany()))
            {
                continue;
            }
            
            if (!like(companyNameFilter, conn.getGsCompany()))
            {
                continue;
            }
            
            if (!like(urlFilter, conn.getUrl()))
            {
                continue;
            }
            
            if (!like(descriptionFilter, conn.getDescription()))
            {
                continue;
            }
            
            result.add(conn);
        }
        
        return result;
    }
    
    private boolean like(String filter, String value)
    {
        if (filter == null)
            return true;
        
        filter = filter.trim();
        if (filter.length() == 0)
            return true;
        
        if (value == null)
            return false;
        
        filter = filter.toLowerCase();
        value = value.toLowerCase();
        
        return value.indexOf(filter) > -1;
    }

    public String getNameFilter()
    {
        return nameFilter;
    }

    public void setNameFilter(String nameFilter)
    {
        this.nameFilter = nameFilter;
    }

    public String getCompanyFilter()
    {
        return companyFilter;
    }

    public void setCompanyFilter(String companyFilter)
    {
        this.companyFilter = companyFilter;
    }

    public String getCompanyNameFilter()
    {
        return companyNameFilter;
    }

    public void setCompanyNameFilter(String companyNameFilter)
    {
        this.companyNameFilter = companyNameFilter;
    }

    public String getUrlFilter()
    {
        return urlFilter;
    }

    public void setUrlFilter(String urlFilter)
    {
        this.urlFilter = urlFilter;
    }

    public String getDescriptionFilter()
    {
        return descriptionFilter;
    }

    public void setDescriptionFilter(String descriptionFilter)
    {
        this.descriptionFilter = descriptionFilter;
    }

}
