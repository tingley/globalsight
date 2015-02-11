/**
 *  Copyright 2009 Welocalize, Inc. 
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */
package com.globalsight.cxe.entity.filterconfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.globalsight.everest.util.comparator.FilterComparator;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.SortUtil;

public class MSOfficeExcelFilter implements Filter
{
    @SuppressWarnings("unchecked")
    public ArrayList<Filter> getFilters(long companyId)
    {
        ArrayList<Filter> filters = null;
        filters = new ArrayList<Filter>();
        String hql = "from MSOfficeExcelFilter jp where jp.companyId="
                + companyId;
        try
        {
            filters = (ArrayList<Filter>) HibernateUtil.search(hql);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        SortUtil.sort(filters, new FilterComparator(Locale.getDefault()));
        return filters;
    }

    private long id;
    private String filterName;
    private String filterDescription;
    private long contentPostFilterId = -2;
    private String contentPostFilterTableName = null;
    private long companyId;
    private boolean altTranslate = false;
    private boolean tabNamesTranslate = false;

    public long getId()
    {
        return id;
    }

    public String getFilterTableName()
    {
        return FilterConstants.MSOFFICEEXCEL_TABLENAME;
    }

    public String toJSON(long companyId)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"filterTableName\":")
                .append("\"" + FilterConstants.MSOFFICEEXCEL_TABLENAME + "\"")
                .append(",");
        sb.append("\"id\":").append(id).append(",");
        sb.append("\"filterName\":").append("\"")
                .append(FilterHelper.escape(filterName)).append("\"")
                .append(",");
        sb.append("\"filterDescription\":").append("\"")
                .append(FilterHelper.escape(filterDescription)).append("\"")
                .append(",");
        sb.append("\"companyId\":").append(companyId).append(",");
        sb.append("\"altTranslate\":").append(altTranslate).append(",");
        sb.append("\"tabNamesTranslate\":").append(tabNamesTranslate)
                .append(",");
        sb.append("\"contentPostFilterId\":").append(contentPostFilterId)
                .append(",");
        sb.append("\"contentPostFilterTableName\":").append("\"")
                .append(FilterHelper.escape(contentPostFilterTableName))
                .append("\",");
        sb.append("\"baseFilterId\":")
                .append("\"")
                .append(BaseFilterManager.getBaseFilterIdByMapping(id,
                        FilterConstants.MSOFFICEEXCEL_TABLENAME)).append("\"");
        sb.append("}");
        return sb.toString();
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String getFilterName()
    {
        return filterName;
    }

    public void setFilterName(String filterName)
    {
        this.filterName = filterName;
    }

    public String getFilterDescription()
    {
        return filterDescription;
    }

    public void setFilterDescription(String filterDescription)
    {
        this.filterDescription = filterDescription;
    }

    public long getCompanyId()
    {
        return companyId;
    }

    public void setCompanyId(long companyId)
    {
        this.companyId = companyId;
    }

    public boolean isAltTranslate()
    {
        return altTranslate;
    }

    public void setAltTranslate(boolean altTranslate)
    {
        this.altTranslate = altTranslate;
    }

    public boolean isTabNamesTranslate()
    {
        return tabNamesTranslate;
    }

    public void setTabNamesTranslate(boolean tabNamesTranslate)
    {
        this.tabNamesTranslate = tabNamesTranslate;
    }

    public void setContentPostFilterId(long contentPostFilterId)
    {
        this.contentPostFilterId = contentPostFilterId;
    }

    public long getContentPostFilterId()
    {
        return this.contentPostFilterId;
    }

    public void setContentPostFilterTableName(String contentPostFilterTableName)
    {
        this.contentPostFilterTableName = contentPostFilterTableName;
    }

    public String getContentPostFilterTableName()
    {
        return this.contentPostFilterTableName;
    }

    public boolean checkExistsNew(String filterName, long companyId)
    {
        String hql = "from MSOfficeExcelFilter jp "
                + "where jp.filterName =:filterName "
                + "and jp.companyId=:companyId";
        Map map = new HashMap();
        map.put("filterName", filterName);
        map.put("companyId", companyId);
        return HibernateUtil.search(hql, map).size() > 0;
    }
    
    public boolean checkExistsEdit(long filterId, String filterName, long companyId)
    {
        String hql = "from MSOfficeExcelFilter jp "
        		 + "where jp.id <>:filterId "
                + " and jp.filterName =:filterName "
                + "and jp.companyId=:companyId";
        Map map = new HashMap();
        map.put("filterId", filterId);
        map.put("filterName", filterName);
        map.put("companyId", companyId);
        return HibernateUtil.search(hql, map).size() > 0;
    }
}
