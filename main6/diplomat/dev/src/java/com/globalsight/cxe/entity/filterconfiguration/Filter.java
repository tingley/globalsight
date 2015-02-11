package com.globalsight.cxe.entity.filterconfiguration;

import java.util.ArrayList;

import com.globalsight.log.GlobalSightCategory;

public interface Filter
{
    public static final GlobalSightCategory CATEGORY = (GlobalSightCategory) GlobalSightCategory
            .getLogger(Filter.class);

    ArrayList<Filter> getFilters(long companyId);

    String toJSON(long companyId);

    boolean checkExists(String filterName, long companyId);

    String getFilterTableName();

    String getFilterName();

    long getId();
}
