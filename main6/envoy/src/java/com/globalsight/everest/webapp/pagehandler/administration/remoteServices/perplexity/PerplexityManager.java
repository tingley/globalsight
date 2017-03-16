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
package com.globalsight.everest.webapp.pagehandler.administration.remoteServices.perplexity;

import java.util.HashMap;
import java.util.List;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * <code>PerplexityManager</code> can help us with using Perplexity.
 * <p>
 * For GBS-4495 perplexity score on MT.
 */
public class PerplexityManager
{
    @SuppressWarnings("unchecked")
    public static List<PerplexityService> getAllPerplexity()
    {
        String hql = "from PerplexityService a where a.companyId = 1";
        HashMap<String, Long> map = new HashMap<String, Long>();

        String currentId = CompanyThreadLocal.getInstance().getValue();
        if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
        {
            hql += " or a.companyId = :companyId";
            map.put("companyId", Long.parseLong(currentId));
        }

        return (List<PerplexityService>) HibernateUtil.search(hql, map);
    }

    public static PerplexityService getByName(String name)
    {
        String hql = "from PerplexityService a where a.name = :name";
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("name", name);

        return (PerplexityService) HibernateUtil.getFirst(hql, map);
    }

    public static boolean isExistName(String name, String id)
    {
        PerplexityService perplexityService = getByName(name);
        return perplexityService != null
                && (id == null || Long.parseLong(id) != perplexityService.getId());
    }

    public static List<PerplexityService> getAllPerplexityByCompanyId(long companyId)
    {
        String hql = "from PerplexityService ps where ps.companyId = :companyId";
        HashMap<String, Long> map = new HashMap<String, Long>();

        map.put("companyId", companyId);

        return (List<PerplexityService>) HibernateUtil.search(hql, map);
    }
}
