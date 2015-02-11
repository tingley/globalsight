package com.globalsight.connector.eloqua.form;

import java.util.ArrayList;
import java.util.List;

import com.globalsight.connector.eloqua.models.EloquaObject;

public class EloquaFileFilter
{
    private String nameFilter;
    private String createdAtFilter;
    private String createdByFilter;
    private String statusFilter;
    private Integer type;
    
    public List filter(List obs)
    {
        List<EloquaObject>  conns = obs;
        List<EloquaObject> result = new ArrayList<EloquaObject>();
        
        for (EloquaObject conn : conns)
        {
            if (!like(nameFilter, conn.getName()))
            {
                continue;
            }
            
            if (!like(createdAtFilter, conn.getCreatedAt()))
            {
                continue;
            }
            
            if (!like(createdByFilter, conn.getCreateBy()))
            {
                continue;
            }
            
            if (!like(statusFilter, conn.getStatus()))
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

    public String getCreatedAtFilter()
    {
        return createdAtFilter;
    }

    public void setCreatedAtFilter(String createdAtFilter)
    {
        this.createdAtFilter = createdAtFilter;
    }

    public String getCreatedByFilter()
    {
        return createdByFilter;
    }

    public void setCreatedByFilter(String createdByFilter)
    {
        this.createdByFilter = createdByFilter;
    }

    public String getStatusFilter()
    {
        return statusFilter;
    }

    public void setStatusFilter(String statusFilter)
    {
        this.statusFilter = statusFilter;
    }

    public Integer getType()
    {
        return type;
    }

    public void setType(Integer type)
    {
        this.type = type;
    }
}
