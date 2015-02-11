package com.globalsight.cxe.entity.filterconfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.globalsight.persistence.hibernate.HibernateUtil;

public class InddFilter implements Filter
{
    private long id;
    private String filterName;
    private String filterDescription;
    private long companyId;
    private boolean translateHiddenLayer = false;
    private boolean translateMasterLayer = true;
    private boolean translateFileInfo = false;

    public boolean checkExists(String filterName, long companyId)
    {
        String hql = "from InddFilter infl where infl.filterName =:filterName and infl.companyId=:companyId";
        Map map = new HashMap();
        map.put("filterName", filterName);
        map.put("companyId", companyId);
        return HibernateUtil.search(hql, map).size() > 0;
    }

    public ArrayList<Filter> getFilters(long companyId)
    {
        ArrayList<Filter> filters = null;
        filters = new ArrayList<Filter>();
        String hql = "from InddFilter infl where infl.companyId=" + companyId;
        filters = (ArrayList<Filter>) HibernateUtil.search(hql);
        return filters;
    }

    public String toJSON(long companyId)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"filterTableName\":").append(
                "\"" + FilterConstants.INDD_TABLENAME + "\"").append(",");
        sb.append("\"id\":").append(id).append(",");
        sb.append("\"companyId\":").append(companyId).append(",");
        sb.append("\"filterName\":").append("\"").append(
                FilterHelper.escape(filterName)).append("\"").append(",");
        sb.append("\"filterDescription\":").append("\"").append(
                FilterHelper.escape(filterDescription)).append("\"")
                .append(",");
        sb.append("\"translateHiddenLayer\":").append(translateHiddenLayer).append(",");
        sb.append("\"translateMasterLayer\":").append(translateMasterLayer).append(",");
        sb.append("\"translateFileInfo\":").append(translateFileInfo);
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

    public String getFilterTableName()
    {
        return FilterConstants.INDD_TABLENAME;
    }

    public boolean getTranslateHiddenLayer()
    {
        return translateHiddenLayer;
    }

    public void setTranslateHiddenLayer(boolean translateHiddenLayer)
    {
        this.translateHiddenLayer = translateHiddenLayer;
    }

    public boolean getTranslateMasterLayer()
    {
        return translateMasterLayer;
    }

    public void setTranslateMasterLayer(boolean translateMasterLayer)
    {
        this.translateMasterLayer = translateMasterLayer;
    }

    public boolean getTranslateFileInfo()
    {
        return translateFileInfo;
    }

    public void setTranslateFileInfo(boolean translateFileInfo)
    {
        this.translateFileInfo = translateFileInfo;
    }
}
