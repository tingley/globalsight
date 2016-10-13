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
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.util.comparator.FilterComparator;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.SortUtil;

/**
 * The manager for BaseFilter (Internal Text, and others)
 * 
 */
public class BaseFilterManager
{
    private static final Logger CATEGORY = Logger
            .getLogger(BaseFilterManager.class);

    public static List<BaseFilter> getAllBaseFilters()
    {
        String hql = "from BaseFilter x";

        HashMap map = null;
        String currentId = CompanyThreadLocal.getInstance().getValue();
        if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
        {
            hql += " where x.companyId = :companyId";
            map = new HashMap();
            map.put("companyId", Long.parseLong(currentId));
        }

        hql += " order by x.name";

        ArrayList<BaseFilter> filters = new ArrayList<BaseFilter>();
        filters = (ArrayList<BaseFilter>) HibernateUtil.search(hql, map);
        SortUtil.sort(filters, new FilterComparator(Locale.getDefault()));
        return filters;
    }

    public static String getAllBaseFiltersJson()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("\"baseFilters\":");
        sb.append("[");
        Iterator<BaseFilter> bfs = BaseFilterManager.getAllBaseFilters()
                .iterator();
        boolean deleteComm = false;
        while (bfs.hasNext())
        {
            deleteComm = true;
            BaseFilter bf = bfs.next();
            sb.append("{");
            sb.append("\"baseFilterId\":").append(bf.getId()).append(",");
            sb.append("\"baseFilterName\":").append("\"")
                    .append(FilterHelper.escape(bf.getFilterName()))
                    .append("\"");
            sb.append("}");

            sb.append(",");
        }
        if (deleteComm)
        {
            sb = sb.deleteCharAt(sb.length() - 1);
        }
        sb.append("]");
        return sb.toString();
    }

    public static List<Filter> getAllBaseFilters(long companyId)
    {
        ArrayList<Filter> filters = new ArrayList<Filter>();
        String hql = "from BaseFilter bf where bf.companyId=" + companyId;
        filters = (ArrayList<Filter>) HibernateUtil.search(hql);
        SortUtil.sort(filters, new FilterComparator(Locale.getDefault()));
        return filters;
    }

    public static BaseFilter getBaseFilterByName(long companyId,
            String filterName)
    {
        StringBuffer sql = new StringBuffer();
        sql.append("from BaseFilter bf where bf.companyId =").append(companyId)
                .append(" and bf.filterName like '%").append(filterName)
                .append("%'");
        List filters = HibernateUtil.search(sql.toString());
        if (filters != null && filters.size() > 0)
        {
            return (BaseFilter) filters.get(0);
        }
        else
        {
            return null;
        }
    }

    public static BaseFilter getBaseFilterById(long filterId)
    {
        StringBuffer sql = new StringBuffer();
        sql.append("from BaseFilter bf where bf.id=").append(filterId);
        List filters = HibernateUtil.search(sql.toString());
        if (filters != null && filters.size() > 0)
        {
            return (BaseFilter) filters.get(0);
        }
        else
        {
            return null;
        }
    }

    public static BaseFilterMapping saveBaseFilterMapping(long baseFilterId,
            long filterId, String filterTableName)
    {
        BaseFilterMapping bfm = getBaseFilterMapping(filterId, filterTableName);

        if (baseFilterId > 0)
        {
            if (bfm == null)
            {
                bfm = new BaseFilterMapping();
                bfm.setFilterId(filterId);
                bfm.setFilterTableName(filterTableName);
                bfm.setBaseFilterId(baseFilterId);
                HibernateUtil.saveOrUpdate(bfm);
            }
            else
            {
                bfm.setBaseFilterId(baseFilterId);
                HibernateUtil.update(bfm);
            }
        }

        return bfm;
    }

    public static void deleteBaseFilterMapping(long filterId,
            String filterTableName)
    {
        BaseFilterMapping bfm = getBaseFilterMapping(filterId, filterTableName);

        try
        {
            while (bfm != null)
            {
                HibernateUtil.delete(bfm);
                bfm = getBaseFilterMapping(filterId, filterTableName);
            }
        }
        catch (Exception e)
        {
            CATEGORY.error("Error when deleteBaseFilterMapping", e);
        }
    }

    public static BaseFilterMapping getBaseFilterMapping(long filterId,
            String filterTableName)
    {
        StringBuffer sql = new StringBuffer();
        sql.append("from BaseFilterMapping bfm where bfm.filterId =")
                .append(filterId).append(" and bfm.filterTableName = '")
                .append(filterTableName).append("'");
        List filters = HibernateUtil.search(sql.toString());
        if (filters != null && filters.size() > 0)
        {
            return (BaseFilterMapping) filters.get(0);
        }
        else
        {
            return null;
        }
    }

    public static List<BaseFilterMapping> getBaseFilterMapping(long baseFilterId)
    {
        StringBuffer sql = new StringBuffer();
        sql.append("from BaseFilterMapping bfm where bfm.baseFilterId =")
                .append(baseFilterId);
        List filters = HibernateUtil.search(sql.toString());
        if (filters != null && filters.size() > 0)
        {
            return (List<BaseFilterMapping>) filters;
        }
        else
        {
            return new ArrayList<BaseFilterMapping>();
        }
    }

    public static BaseFilter getBaseFilterByMapping(long filterId,
            String filterTableName)
    {
        BaseFilterMapping bfm = getBaseFilterMapping(filterId, filterTableName);
        if (bfm != null)
        {
            return getBaseFilterById(bfm.getBaseFilterId());
        }
        else
        {
            return null;
        }
    }

    public static long getBaseFilterIdByMapping(long filterId,
            String filterTableName)
    {
        BaseFilter bf = getBaseFilterByMapping(filterId, filterTableName);

        if (bf != null)
        {
            return bf.getId();
        }
        else
        {
            return -2;
        }
    }

    public static List<InternalText> getInternalTexts(BaseFilter bf)
            throws Exception
    {
        BaseFilterParser p = new BaseFilterParser(bf);
        p.parserXml();
        return p.getInternalTexts();
    }
    
    public static List<Escaping> getEscapings(BaseFilter bf)
            throws Exception
    {
        BaseFilterParser p = new BaseFilterParser(bf);
        p.parserXml();
        return p.getEscapings();
    }
}
