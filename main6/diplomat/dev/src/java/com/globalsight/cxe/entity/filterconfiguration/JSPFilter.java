package com.globalsight.cxe.entity.filterconfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.globalsight.everest.util.comparator.FilterComparator;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.SortUtil;

public class JSPFilter implements Filter
{
    private long id;
    private String filterName;
    private String filterDescription;
    private long companyId;
    private boolean addAdditionalHead = false;
    private boolean enableEscapeEntity = false;

    public JSPFilter()
    {
        super();
        // TODO Auto-generated constructor stub
    }

    public JSPFilter(String filterName, String filterDescription,
            long companyId, boolean addAdditionalHead,
            boolean enableEscapeEntity)
    {
        super();
        this.filterName = filterName;
        this.filterDescription = filterDescription;
        this.companyId = companyId;
        this.addAdditionalHead = addAdditionalHead;
        this.enableEscapeEntity = enableEscapeEntity;
    }

    public JSPFilter(long id, String filterName, String filterDescription,
            long companyId, boolean addAdditionalHead, boolean isEscapeEntity)
    {
        super();
        this.id = id;
        this.filterName = filterName;
        this.filterDescription = filterDescription;
        this.companyId = companyId;
        this.addAdditionalHead = addAdditionalHead;
        this.enableEscapeEntity = isEscapeEntity;
    }

    public long getCompanyId()
    {
        return companyId;
    }

    public void setCompanyId(long companyId)
    {
        this.companyId = companyId;
    }

    public String getFilterDescription()
    {
        return filterDescription;
    }

    public void setFilterDescription(String filterDescription)
    {
        this.filterDescription = filterDescription;
    }

    public String getFilterName()
    {
        return filterName;
    }

    public void setFilterName(String filterName)
    {
        this.filterName = filterName;
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public boolean getAddAdditionalHead()
    {
        return addAdditionalHead;
    }

    public void setAddAdditionalHead(boolean addAdditionalHead)
    {
        this.addAdditionalHead = addAdditionalHead;
    }

    public boolean getEnableEscapeEntity()
    {
        return enableEscapeEntity;
    }

    public void setEnableEscapeEntity(boolean enableEscapeEntity)
    {
        this.enableEscapeEntity = enableEscapeEntity;
    }

    public boolean checkExistsNew(String filterName, long companyId)
    {
        String hql = "from JSPFilter jsp where jsp.filterName =:filterName and jsp.companyId=:companyId";
        Map map = new HashMap();
        map.put("filterName", filterName);
        map.put("companyId", companyId);
        return HibernateUtil.search(hql, map).size() > 0;
    }

    public boolean checkExistsEdit(long filterId, String filterName,
            long companyId)
    {
        String hql = "from JSPFilter jsp where jsp.id<>:filterId and jsp.filterName =:filterName and jsp.companyId=:companyId";
        Map map = new HashMap();
        map.put("filterId", filterId);
        map.put("filterName", filterName);
        map.put("companyId", companyId);
        return HibernateUtil.search(hql, map).size() > 0;
    }

    public String getFilterTableName()
    {
        return FilterConstants.JSP_TABLENAME;
    }

    public ArrayList<Filter> getFilters(long companyId)
    {
        ArrayList<Filter> filters = null;
        filters = new ArrayList<Filter>();
        String hql = "from JSPFilter jsp where jsp.companyId=" + companyId;
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
                .append("\"" + getFilterTableName() + "\"").append(",");
        sb.append("\"id\":").append(id).append(",");
        sb.append("\"companyId\":").append(companyId).append(",");
        sb.append("\"filterName\":").append("\"")
                .append(FilterHelper.escape(filterName)).append("\"")
                .append(",");
        sb.append("\"filterDescription\":").append("\"")
                .append(FilterHelper.escape(filterDescription)).append("\"")
                .append(",");
        sb.append("\"isAdditionalHeadAdded\":").append(addAdditionalHead)
                .append(",");
        sb.append("\"isEscapeEntity\":").append(enableEscapeEntity).append(",");
        sb.append("\"baseFilterId\":").append("\"").append(baseFilterId)
                .append("\"");
        sb.append("}");
        return sb.toString();
    }

}
