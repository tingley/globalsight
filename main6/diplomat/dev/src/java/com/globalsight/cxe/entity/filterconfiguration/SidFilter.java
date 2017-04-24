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

import org.apache.log4j.Logger;

import com.globalsight.everest.util.comparator.FilterComparator;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.SortUtil;

public class SidFilter implements Filter
{
    static private final Logger logger = Logger
            .getLogger(SidFilter.class);
    
    private long id;
    private String filterName;
    private String filterDescription;
    private String configXml = "";
    private int type = 0;
    private long companyId;

    @SuppressWarnings("unchecked")
    public ArrayList<Filter> getFilters(long companyId)
    {
        ArrayList<Filter> filters = null;
        filters = new ArrayList<Filter>();
        String hql = "from SidFilter f where f.companyId=" + companyId;
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

    public boolean checkExistsNew(String filterName, long companyId)
    {
        String hql = "from SidFilter f "
                + "where f.filterName =:filterName "
                + "and f.companyId =:companyId";
        Map map = new HashMap();
        map.put("filterName", filterName);
        map.put("companyId", companyId);
        return HibernateUtil.search(hql, map).size() > 0;
    }

    public boolean checkExistsEdit(long filterId, String filterName,
            long companyId)
    {
        String hql = "from SidFilter f " + "where f.id<>:filterId "
                + "and f.filterName =:filterName "
                + "and f.companyId =:companyId";
        Map map = new HashMap();
        map.put("filterId", filterId);
        map.put("filterName", filterName);
        map.put("companyId", companyId);
        return HibernateUtil.search(hql, map).size() > 0;
    }

    public String getFilterTableName()
    {
        return FilterConstants.SID_TABLENAME;
    }

    public String toJSON(long companyId)
    {
        logger.debug("Sid filter config Xml:" + this.getConfigXml());
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"filterTableName\":")
                .append("\"" + getFilterTableName() + "\"").append(",");
        sb.append("\"id\":").append(id).append(",");
        sb.append("\"filterName\":").append("\"")
                .append(FilterHelper.escape(filterName)).append("\"")
                .append(",");
        sb.append("\"filterDescription\":").append("\"")
                .append(FilterHelper.escape(filterDescription)).append("\"")
                .append(",");
        sb.append("\"filterType\":").append("\"").append(type).append("\"").append(",");
        sb.append("\"companyId\":").append(companyId).append(",");
        sb.append("\"jsonText\":")
                .append("\"")
                .append(FilterHelper.escape(configXml)).append("\",");

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
        if (filterDescription == null)
            return "";
        
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

    public String getConfigXml()
    {
        if (configXml == null)
            configXml = "";
        
        return configXml;
    }

    public void setConfigXml(String configXml)
    {
        this.configXml = configXml;
    }
    
    public int getType()
    {
        return type;
    }

    /**
     * 1 : xml
     * 2 : txt
     * 3 : java properties
     * 4 : json
     * @param type
     */
    public void setType(int type)
    {
        this.type = type;
    }
}
