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

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.persistence.hibernate.HibernateUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class GlobalExclusionFilterHelper
{
    /**
     * Gets all enabled global exclusion Filters with specified company id.
     * @param companyId
     */
    public static  ArrayList<GlobalExclusionFilterSid> getAllEnabledGlobalExclusionFilters(long companyId)
    {
        String hql = "from GlobalExclusionFilter f where f.companyId=" + companyId;
        ArrayList<GlobalExclusionFilter> filters = (ArrayList<GlobalExclusionFilter>) HibernateUtil.search(hql);
        ArrayList<GlobalExclusionFilterSid> enabledFilters = new ArrayList<>();
        for (GlobalExclusionFilter f : filters)
        {
            String json = f.getConfigXml();
            JSONArray items = JSONArray.fromObject(json);
            for (int i = 0; i < items.size(); i++)
            {
                JSONObject ob = (JSONObject) items.get(i);
                if (ob.getBoolean("enable"))
                {
                    GlobalExclusionFilterSid gob = new GlobalExclusionFilterSid(ob);
                    enabledFilters.add(gob);
                }
            }
        }
        
        return enabledFilters;
    }
    
    /**
     * Gets all enabled global exclusion Filters with specified company id.
     * @param companyId
     */
    public static  ArrayList<GlobalExclusionFilterSid> getAllEnabledGlobalExclusionFilters()
    {
        String currentCompanyId = CompanyThreadLocal.getInstance()
                .getValue();
        long companyId = Long.parseLong(currentCompanyId);
        return getAllEnabledGlobalExclusionFilters(companyId);
    }
}
