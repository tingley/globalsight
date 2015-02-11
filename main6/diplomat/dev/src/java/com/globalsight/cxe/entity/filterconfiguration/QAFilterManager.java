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
import java.util.List;
import java.util.Locale;

import com.globalsight.everest.util.comparator.FilterComparator;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.SortUtil;

public class QAFilterManager
{
    public static List<Filter> getAllQAFilters(long companyId)
    {
        List<Filter> filters = new ArrayList<Filter>();
        String hql = "from QAFilter qf where qf.companyId=" + companyId;
        filters = (ArrayList<Filter>) HibernateUtil.search(hql);
        SortUtil.sort(filters, new FilterComparator(Locale.getDefault()));
        return filters;
    }

    public static QAFilter getQAFilterByName(long companyId, String filterName)
    {
        StringBuffer sql = new StringBuffer();
        sql.append("from QAFilter qf where qf.companyId =").append(companyId)
                .append(" and qf.filterName like '%").append(filterName)
                .append("%'");
        List filters = HibernateUtil.search(sql.toString());
        if (filters != null && filters.size() > 0)
        {
            return (QAFilter) filters.get(0);
        }
        else
        {
            return null;
        }
    }

    public static QAFilter getQAFilterById(long filterId)
    {
        StringBuffer sql = new StringBuffer();
        sql.append("from QAFilter qf where qf.id=").append(filterId);
        List filters = HibernateUtil.search(sql.toString());
        if (filters != null && filters.size() > 0)
        {
            return (QAFilter) filters.get(0);
        }
        else
        {
            return null;
        }
    }

    public static List<QARule> getQARules(QAFilter qaFilter) throws Exception
    {
        QAFilterParser p = new QAFilterParser(qaFilter);
        p.parserXml();

        return p.getQARules();
    }

    public static List<QARuleDefault> getQARulesDefault(QAFilter qaFilter)
            throws Exception
    {
        QAFilterParser p = new QAFilterParser(qaFilter);
        p.parserXml();

        return p.getQARulesDefault();
    }
}
