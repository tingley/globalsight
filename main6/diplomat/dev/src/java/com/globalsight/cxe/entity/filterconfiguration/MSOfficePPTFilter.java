package com.globalsight.cxe.entity.filterconfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.globalsight.everest.util.comparator.FilterComparator;
import com.globalsight.persistence.hibernate.HibernateUtil;

public class MSOfficePPTFilter implements Filter
{
    private long id;
    private String filterName;
    private String filterDescription;
    private long contentPostFilterId = -2;
    private String contentPostFilterTableName = null;
    private long companyId;
    private boolean altTranslate = false;

    @SuppressWarnings("unchecked")
    public ArrayList<Filter> getFilters(long companyId)
    {
        ArrayList<Filter> filters = null;
        filters = new ArrayList<Filter>();
        String hql = "from MSOfficePPTFilter jp where jp.companyId="
                + companyId;
        try
        {
            filters = (ArrayList<Filter>) HibernateUtil.search(hql);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        Collections.sort(filters, new FilterComparator(Locale.getDefault()));
        return filters;
    }

    public boolean checkExists(String filterName, long companyId)
    {
        String hql = "from MSOfficePPTFilter jp "
                + "where jp.filterName =:filterName "
                + "and jp.companyId=:companyId";
        Map map = new HashMap();
        map.put("filterName", filterName);
        map.put("companyId", companyId);
        return HibernateUtil.search(hql, map).size() > 0;
    }

    public String getFilterTableName()
    {
        return FilterConstants.MSOFFICEPPT_TABLENAME;
    }

    public String toJSON(long companyId)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"filterTableName\":")
                .append("\"" + FilterConstants.MSOFFICEPPT_TABLENAME + "\"")
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
        sb.append("\"contentPostFilterId\":").append(contentPostFilterId)
                .append(",");
        sb.append("\"contentPostFilterTableName\":").append("\"")
                .append(FilterHelper.escape(contentPostFilterTableName))
                .append("\",");
        sb.append("\"baseFilterId\":")
                .append("\"")
                .append(BaseFilterManager.getBaseFilterIdByMapping(id,
                        FilterConstants.MSOFFICEPPT_TABLENAME)).append("\"");
        sb.append("}");
        return sb.toString();
    }

    public long getId()
    {
        return id;
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

    public boolean isAltTranslate()
    {
        return altTranslate;
    }

    public void setAltTranslate(boolean altTranslate)
    {
        this.altTranslate = altTranslate;
    }
}
