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

public class FMFilter implements Filter
{

    private long id;
    private String filterName;
    private String filterDescription;
    private long companyId;
    private boolean exposeFootNote = false;
    private boolean exposeLeftMasterPage = false;
    private boolean exposeRightMasterPage = false;
    private boolean exposeOtherMasterPage = false;
    private boolean tableOfContentTranslate = false;

    /**
     * Construture
     */
    public FMFilter()
    {
        super();
    }

    public FMFilter(long id, String filterName, String filterDescription,
            long companyId, boolean exposeFootNote,
            boolean exposeLeftMasterPage, boolean exposeRightMasterPage,
            boolean exposeOtherMasterPage)
    {
        super();
        this.id = id;
        this.filterName = filterName;
        this.filterDescription = filterDescription;
        this.companyId = companyId;
        this.exposeFootNote = exposeFootNote;
        this.exposeLeftMasterPage = exposeLeftMasterPage;
        this.exposeRightMasterPage = exposeRightMasterPage;
        this.exposeOtherMasterPage = exposeOtherMasterPage;
    }

    public FMFilter(String filterName, String filterDescription,
            long companyId, boolean exposeFootNote,
            boolean exposeLeftMasterPage, boolean exposeRightMasterPage,
            boolean exposeOtherMasterPage)
    {
        super();
        this.filterName = filterName;
        this.filterDescription = filterDescription;
        this.companyId = companyId;
        this.exposeFootNote = exposeFootNote;
        this.exposeLeftMasterPage = exposeLeftMasterPage;
        this.exposeRightMasterPage = exposeRightMasterPage;
        this.exposeOtherMasterPage = exposeOtherMasterPage;
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

    public boolean isExposeFootNote()
    {
        return exposeFootNote;
    }

    public void setExposeFootNote(boolean exposeFootNote)
    {
        this.exposeFootNote = exposeFootNote;
    }

    public boolean isExposeLeftMasterPage()
    {
        return exposeLeftMasterPage;
    }

    public void setExposeLeftMasterPage(boolean exposeLeftMasterPage)
    {
        this.exposeLeftMasterPage = exposeLeftMasterPage;
    }

    public boolean isExposeRightMasterPage()
    {
        return exposeRightMasterPage;
    }

    public void setExposeRightMasterPage(boolean exposeRightMasterPage)
    {
        this.exposeRightMasterPage = exposeRightMasterPage;
    }

    public boolean isExposeOtherMasterPage()
    {
        return exposeOtherMasterPage;
    }

    public void setExposeOtherMasterPage(boolean exposeOtherMasterPage)
    {
        this.exposeOtherMasterPage = exposeOtherMasterPage;
    }

    public boolean isTableOfContentTranslate()
    {
        return tableOfContentTranslate;
    }

    public void setTableOfContentTranslate(boolean tableOfContentTranslate)
    {
        this.tableOfContentTranslate = tableOfContentTranslate;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public void setFilterName(String filterName)
    {
        this.filterName = filterName;
    }

    public String getFilterName()
    {
        return filterName;
    }

    public long getId()
    {
        return id;
    }

    public ArrayList<Filter> getFilters(long companyId)
    {
        ArrayList<Filter> filters = new ArrayList<Filter>();
        String hql = "from FMFilter fm where fm.companyId=" + companyId;
        filters = (ArrayList<Filter>) HibernateUtil.search(hql);
        SortUtil.sort(filters, new FilterComparator(Locale.getDefault()));
        return filters;
    }

    public String toJSON(long companyId)
    {
        StringBuilder con = new StringBuilder();
        con.append("{");
        con.append("\"filterTableName\":")
                .append("\"" + getFilterTableName() + "\"").append(",");
        con.append("\"id\":").append(id).append(",");
        con.append("\"companyId\":").append(companyId).append(",");
        con.append("\"filterName\":").append("\"")
                .append(FilterHelper.escape(filterName)).append("\"")
                .append(",");
        con.append("\"filterDescription\":").append("\"")
                .append(FilterHelper.escape(filterDescription)).append("\"")
                .append(",");
        // con.append("\"isExposeFootNote\":").append(exposeFootNote).append(",");
        con.append("\"isExposeLeftMasterPage\":").append(exposeLeftMasterPage)
                .append(",");
        con.append("\"isExposeRightMasterPage\":")
                .append(exposeRightMasterPage).append(",");
        con.append("\"isExposeOtherMasterPage\":")
                .append(exposeOtherMasterPage).append(",");
        con.append("\"isTOCTranslate\":").append(isTableOfContentTranslate());
        con.append("}");
        return con.toString();
    }

    public boolean checkExistsNew(String filterName, long companyId)
    {
        String hql = "from FMFilter fm where fm.filterName =:filterName and fm.companyId=:companyId";
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("filterName", filterName);
        map.put("companyId", companyId);
        return HibernateUtil.search(hql, map).size() > 0;
    }

    public boolean checkExistsEdit(long filterId, String filterName, long companyId)
    {
        String hql = "from FMFilter fm where fm.id<>:filterId and fm.filterName =:filterName and fm.companyId=:companyId";
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("filterId", filterId);
        map.put("filterName", filterName);
        map.put("companyId", companyId);
        return HibernateUtil.search(hql, map).size() > 0;
    }
    
    public String getFilterTableName()
    {
        return FilterConstants.FM_TABLENAME;
    }

}
