package com.globalsight.cxe.entity.filterconfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.globalsight.everest.util.comparator.FilterComparator;
import com.globalsight.persistence.hibernate.HibernateUtil;

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
    
    
    public FMFilter(String filterName, String filterDescription, long companyId,
            boolean exposeFootNote, boolean exposeLeftMasterPage,
            boolean exposeRightMasterPage, boolean exposeOtherMasterPage)
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
        Collections.sort(filters, new FilterComparator(Locale.getDefault()));
        return filters;
    }

    public String toJSON(long companyId)
    {
        StringBuilder con = new StringBuilder();
        con.append("{");
        con.append("\"filterTableName\":").append(
                "\"" + getFilterTableName() + "\"").append(",");
        con.append("\"id\":").append(id).append(",");
        con.append("\"companyId\":").append(companyId).append(",");
        con.append("\"filterName\":").append("\"").append(
                FilterHelper.escape(filterName)).append("\"").append(",");
        con.append("\"filterDescription\":").append("\"").append(
                FilterHelper.escape(filterDescription)).append("\"")
                .append(",");
        //con.append("\"isExposeFootNote\":").append(exposeFootNote).append(",");
        con.append("\"isExposeLeftMasterPage\":").append(exposeLeftMasterPage).append(",");
        con.append("\"isExposeRightMasterPage\":").append(exposeRightMasterPage).append(",");
        con.append("\"isExposeOtherMasterPage\":").append(exposeOtherMasterPage).append(",");
        con.append("\"isTOCTranslate\":").append(isTableOfContentTranslate());
        con.append("}");
        return con.toString();
    }

    public boolean checkExists(String filterName, long companyId)
    {
        String hql = "select count(*) from FMFilter fm where fm.filterName =:filterName and fm.companyId=:companyId";
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("filterName", filterName);
        map.put("companyId", companyId);
        
        List<?> res = HibernateUtil.search(hql, map);
        int count = (Integer) res.get(0);
        if (count > 0) {
            return true;
        }
        else 
        {
            return false;
        }
    }

    public String getFilterTableName()
    {
        return FilterConstants.FM_TABLENAME;
    }

}
