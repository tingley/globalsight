package com.globalsight.everest.webapp.pagehandler.administration.remoteServices.perplexity;

import java.util.ArrayList;
import java.util.List;

/**
 * <code>PerplexityServiceFilter</code> is used to filter
 * <code>PerplexityService</code>
 * <p>
 * For GBS-4495 perplexity score on MT.
 */
public class PerplexityServiceFilter
{
    private String nameFilter;
    private String urlFilter;
    private String descriptionFilter;

    public List<PerplexityService> filter(List<PerplexityService> conns)
    {
        List<PerplexityService> result = new ArrayList<PerplexityService>();

        for (PerplexityService conn : conns)
        {
            if (!like(nameFilter, conn.getName()))
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
