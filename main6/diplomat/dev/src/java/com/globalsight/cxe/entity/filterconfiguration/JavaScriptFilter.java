package com.globalsight.cxe.entity.filterconfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.globalsight.everest.util.comparator.FilterComparator;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.SortUtil;

public class JavaScriptFilter implements Filter
{
    private long id;
    private String filterName;
    private String filterDescription;
    private String jsFunctionText;
    private long companyId;
    private boolean enableUnicodeEscape = false;

    public boolean checkExistsNew(String filterName, long companyId)
    {
        String hql = "from JavaScriptFilter js where js.filterName =:filterName and js.companyId=:companyId";
        Map map = new HashMap();
        map.put("filterName", filterName);
        map.put("companyId", companyId);
        return HibernateUtil.search(hql, map).size() > 0;
    }

    public boolean checkExistsEdit(long filterId, String filterName,
            long companyId)
    {
        String hql = "from JavaScriptFilter js where js.id<>:filterId and js.filterName =:filterName and js.companyId=:companyId";
        Map map = new HashMap();
        map.put("filterId", filterId);
        map.put("filterName", filterName);
        map.put("companyId", companyId);
        return HibernateUtil.search(hql, map).size() > 0;
    }

    public ArrayList<Filter> getFilters(long companyId)
    {
        ArrayList<Filter> filters = null;
        filters = new ArrayList<Filter>();
        String hql = "from JavaScriptFilter js where js.companyId=" + companyId;
        filters = (ArrayList<Filter>) HibernateUtil.search(hql);
        SortUtil.sort(filters, new FilterComparator(Locale.getDefault()));
        return filters;
    }

    public String toJSON(long companyId)
    {
        long baseFilterId = BaseFilterManager.getBaseFilterIdByMapping(id,
                getFilterTableName());
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"filterTableName\":")
                .append("\"" + FilterConstants.JAVASCRIPT_TABLENAME + "\"")
                .append(",");
        sb.append("\"id\":").append(id).append(",");
        sb.append("\"companyId\":").append(companyId).append(",");
        sb.append("\"filterName\":").append("\"")
                .append(FilterHelper.escape(filterName)).append("\"")
                .append(",");
        sb.append("\"filterDescription\":").append("\"")
                .append(FilterHelper.escape(filterDescription)).append("\"")
                .append(",");
        sb.append("\"jsFunctionText\":").append("\"")
                .append(FilterHelper.escape(jsFunctionText)).append("\"")
                .append(",");
        sb.append("\"enableUnicodeEscape\":").append(enableUnicodeEscape)
                .append(",");
        sb.append("\"baseFilterId\":").append("\"").append(baseFilterId)
                .append("\"");
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

    public String getJsFunctionText()
    {
        return jsFunctionText;
    }

    public void setJsFunctionText(String jsFunctionText)
    {
        this.jsFunctionText = jsFunctionText;
    }

    public long getCompanyId()
    {
        return companyId;
    }

    public void setCompanyId(long companyId)
    {
        this.companyId = companyId;
    }

    public String getFilterTableName()
    {
        return FilterConstants.JAVASCRIPT_TABLENAME;
    }

    public boolean getEnableUnicodeEscape()
    {
        return enableUnicodeEscape;
    }

    public void setEnableUnicodeEscape(boolean enableUnicodeEscape)
    {
        this.enableUnicodeEscape = enableUnicodeEscape;
    }

}
