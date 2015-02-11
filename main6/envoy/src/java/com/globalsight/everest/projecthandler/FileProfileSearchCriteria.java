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
package com.globalsight.everest.projecthandler;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.foundation.SearchCriteriaParameters;
import com.globalsight.persistence.hibernate.HibernateUtil;

public class FileProfileSearchCriteria
{
    private StringBuffer hql;

    protected boolean m_isCaseSensitive = true;

    private Map map = new HashMap();

    public FileProfileSearchCriteria()
    {
        super();
    }

    /**
     * Get the search expression based on the specified search criteria object.
     * 
     * @param p_searchCriteriaParams
     *            - The search criteria object.
     */
    protected Collection search(SearchCriteriaParameters p_searchCriteriaParams)
            throws Exception
    {
        hql = new StringBuffer("from FileProfileImpl f where f.isActive = 'Y'");

        m_isCaseSensitive = p_searchCriteriaParams.isCaseSensitive();
        Map criteria = p_searchCriteriaParams.getParameters();

        // get the keys of the map
        Object[] keys = criteria.keySet().toArray();
        int mapSize = keys.length;

        // loop throught the parameters to create the sql statement.
        for (int i = 0; i < mapSize; i++)
        {
            switch (((Integer) (keys[i])).intValue())
            {
            // fileprofile template name
                case FileProfileSearchParameters.FP_TEMPLATE_NAME:
                    fpTemplateName(keys[i], criteria);
                    break;

                // Localization Profiles id
                case FileProfileSearchParameters.LOCALIZATION_PROFILES_ID:
                    fpLocProfiles(keys[i], criteria);
                    break;

                // Source format
                case FileProfileSearchParameters.SOURCE_FILE_FORMAT:
                    sourceFormatExpression(keys[i], criteria);
                    break;

                default:
                    break;
            }
        }

        createCompanyExpression();

        return HibernateUtil.search(hql.toString(), map);
    }

    private void createCompanyExpression()
    {
        String currentId = CompanyThreadLocal.getInstance().getValue();
        if (currentId == null || currentId.trim().length() == 0
                || currentId.equals(CompanyWrapper.SUPER_COMPANY_ID))
        {
            return;
        }

        hql.append(" and f.companyId = " + Long.parseLong(currentId.trim()));
    }

    /*
     * Prepare the search expression based on the fileprofile template's name.
     */
    private void fpTemplateName(Object p_key, Map criteria)
    {
        String name = (String) criteria.get(p_key);
        if (name == null || name.trim().length() == 0)
        {
            return;
        }

        String condition = (String) criteria.get(new Integer(
                FileProfileSearchParameters.FP_NAME_CONDITION));

        name = dealWithCondition(name, condition, m_isCaseSensitive);

        if (m_isCaseSensitive)
        {
            hql.append(" and lower(f.name) like :name");
        }
        else
        {
            hql.append(" and f.name like :name");
        }

        map.put("name", name);
    }

    /**
     * Prepare the search expression based on the job's fileprofile format
     */
    private void sourceFormatExpression(Object p_key, Map criteria)
    {
        hql.append(" and f.knownFormatTypeId =:kId");
        map.put("kId", criteria.get(p_key));
    }

    /*
     * Prepare the search expression based on the Localization Profiles id.
     */
    private void fpLocProfiles(Object p_key, Map criteria)
    {
        hql.append(" and f.l10nProfileId =:lId");
        map.put("lId", criteria.get(p_key));
    }

    protected String dealWithCondition(String s, String condition,
            boolean isCaseSensitive)
    {
        if (!isCaseSensitive)
        {
            s = s.toLowerCase();
        }

        s = s.trim();

        if (SearchCriteriaParameters.BEGINS_WITH.equals(condition))
        {
            s += "%";
        }
        // select values greater than p_firstValue
        else if (SearchCriteriaParameters.ENDS_WITH.equals(condition))
        {
            s = "%" + s;
        }
        // select values between p_firstValue and p_secondValue
        else if (SearchCriteriaParameters.CONTAINS.equals(condition))
        {
            s = "%" + s + "%";

        }

        return s;
    }
}
