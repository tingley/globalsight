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
import java.util.Map;

import com.globalsight.persistence.hibernate.HibernateUtil;

public class QAFilter implements Filter
{
    private long id;
    private String filterName;
    private String filterDescription;
    private String configXml = "";
    private long companyId;

    public QAFilter()
    {
    }

    public QAFilter(String filterName, String filterDescription, long companyId)
    {
        this.filterName = filterName;
        this.filterDescription = filterDescription;
        this.companyId = companyId;
    }

    public QAFilter(long id, String filterName, String filterDescription,
            long companyId)
    {
        this(filterName, filterDescription, companyId);
        this.id = id;
    }

    public boolean checkExistsNew(String filterName, long companyId)
    {
        String hql = "from QAFilter qf where qf.filterName =:filterName and qf.companyId=:companyId";
        Map map = new HashMap();
        map.put("filterName", filterName);
        map.put("companyId", companyId);
        return HibernateUtil.search(hql, map).size() > 0;
    }

    public boolean checkExistsEdit(long filterId, String filterName,
            long companyId)
    {
        String hql = "from QAFilter qf where qf.id<>:filterId and qf.filterName =:filterName and qf.companyId=:companyId";
        Map map = new HashMap();
        map.put("filterId", filterId);
        map.put("filterName", filterName);
        map.put("companyId", companyId);
        return HibernateUtil.search(hql, map).size() > 0;
    }

    public String getFilterTableName()
    {
        return FilterConstants.QA_TABLENAME;
    }

    public ArrayList<Filter> getFilters(long companyId)
    {
        return (ArrayList<Filter>) QAFilterManager.getAllQAFilters(companyId);
    }

    public QAFilter getFilter(long companyId, String filterName)
    {
        return QAFilterManager.getQAFilterByName(companyId, filterName);
    }

    public String toJSON(long companyId)
    {
        QAFilterParser parser = new QAFilterParser(configXml);
        boolean isParsed = false;
        try
        {
            parser.parserXml();
            isParsed = true;
        }
        catch (Exception e)
        {
            CATEGORY.error("configXml : " + configXml, e);
            isParsed = false;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"filterTableName\":")
                .append("\"" + FilterConstants.QA_TABLENAME + "\"").append(",");
        sb.append("\"id\":").append(id).append(",");
        sb.append("\"companyId\":").append(companyId).append(",");
        sb.append("\"filterName\":").append("\"")
                .append(FilterHelper.escape(filterName)).append("\"")
                .append(",");
        sb.append("\"filterDescription\":").append("\"")
                .append(FilterHelper.escape(filterDescription)).append("\"")
                .append(",");
        sb.append("\"rules\":")
                .append("\"")
                .append(isParsed ? FilterHelper.escape(parser.getRulesJson())
                        : "[]").append("\"").append(",");
        sb.append("\"defaultRules\":")
                .append("\"")
                .append(isParsed ? FilterHelper.escape(parser
                        .getDefaultRulesJson()) : "[]").append("\"");
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

    public String getConfigXml()
    {
        return configXml;
    }

    public void setConfigXml(String configXml)
    {
        this.configXml = configXml;
    }
}
