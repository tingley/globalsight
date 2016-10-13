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

/**
 * WfTemplateSearchCriteria is used to construct a workflow template search
 * expression based on user input.
 */

public class WfTemplateSearchCriteria
{
    private StringBuffer hql;

    protected boolean m_isCaseSensitive = true;

    private Map map = new HashMap();

    // ////////////////////////////////////////////////////////////////////////////
    // Begin: Abstract Method Implementation
    // ////////////////////////////////////////////////////////////////////////////
    /**
     * Get the search expression based on the specified search criteria object.
     * 
     * @param p_searchCriteriaParams
     *            - The search criteria object.
     * @throws Exception
     */
    public Collection search(SearchCriteriaParameters p_searchCriteriaParams)
            throws Exception
    {
        hql = new StringBuffer(
                "from WorkflowTemplateInfo w where w.isActive = 'Y' ");
        // queryOnlyActiveWorkflows();

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
            // workflow template name
                case WfTemplateSearchParameters.WF_TEMPLATE_NAME:
                    wfTemplateName(keys[i], criteria);
                    break;

                // project name
                case WfTemplateSearchParameters.PROJECT:
                    wfTemplateProject(keys[i], criteria);
                    break;

                // Company Name
                case WfTemplateSearchParameters.COMPANY_NAME:
                    companyNameExpression(keys[i], criteria);
                    break;

                default:
                    break;
            }
        }

        createCompanyExpression();

        return HibernateUtil.search(hql.toString(), map);
    }

    // ////////////////////////////////////////////////////////////////////////////
    // End: Abstract Method Implementation
    // ////////////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////
    // Begin: Private Methods
    // ////////////////////////////////////////////////////////////////////

    private void createCompanyExpression()
    {
        String currentId = CompanyThreadLocal.getInstance().getValue();
        if (currentId == null || currentId.trim().length() == 0
                || currentId.equals(CompanyWrapper.SUPER_COMPANY_ID))
        {
            return;
        }

        hql.append(" and w.companyId = " + Long.parseLong(currentId.trim()));
    }

    private void companyNameExpression(Object p_key, Map criteria)
    {
        String companyName = (String) criteria.get(p_key);
        if (companyName == null || companyName.trim().length() == 0)
        {
            return;
        }
        companyName = "%" + companyName + "%";
        hql.append(" and w.companyId in (select c.id from Company c where c.name like :cId)");
        map.put("cId", companyName);
    }

    /*
     * Prepare the search expression based on the workflow template's name.
     */
    private void wfTemplateName(Object p_key, Map criteria)
    {
        String name = (String) criteria.get(p_key);
        if (name == null || name.trim().length() == 0)
        {
            return;
        }

        name = "%" + name + "%";
        if (m_isCaseSensitive)
        {
            hql.append(" and lower(w.name) like :name");
        }
        else
        {
            hql.append(" and w.name like :name");
        }

        map.put("name", name);
    }

    /*
     * Prepare the search expression based on the project id
     */
    private void wfTemplateProject(Object p_key, Map criteria)
    {
        String projectName = (String) criteria.get(p_key);
        if (projectName == null || projectName.trim().length() == 0)
        {
            return;
        }

        projectName = "%" + projectName + "%";
        hql.append(" and w.project.name like :pro");
        map.put("pro", projectName);
    }

}
