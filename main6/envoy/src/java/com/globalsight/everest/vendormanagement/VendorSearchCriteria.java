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
package com.globalsight.everest.vendormanagement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.globalsight.everest.costing.Rate;
import com.globalsight.everest.foundation.SearchCriteriaParameters;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GlobalSightLocale;

/**
 * VendorSearchCriteria is used to search vendor based on user input.
 */
public class VendorSearchCriteria
{
    private StringBuffer hql = new StringBuffer();
    private boolean m_isCaseSensitive = false;
    private Map params = new HashMap();

    /**
     * Get the search expression based on the specified search criteria object.
     * 
     * @param p_searchCriteriaParams -
     *            The search criteria object.
     */
    public List search(SearchCriteriaParameters p_searchCriteriaParams)
            throws Exception
    {
        hql.append("from Vendor v left join fetch v.roles r where 1=1 ");

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
            // vendor name
            case VendorSearchParameters.VENDOR_NAME:
                vendorName(keys[i], criteria);
                break;

            // company name
            case VendorSearchParameters.COMPANY_NAME:
                companyName(keys[i], criteria);
                break;

            // Source locale (vendor.vendorRole.localePair.source_locale)
            case VendorSearchParameters.SOURCE_LOCALE:
                sourceLocaleExpression(keys[i], criteria);
                break;

            // target locale
            case VendorSearchParameters.TARGET_LOCALE:
                targetLocaleExpression(keys[i], criteria);
                break;

            // rate
            case VendorSearchParameters.RATE_VALUE:
                rateExpression(keys[i], criteria);
                break;

            // Activity type
            case VendorSearchParameters.ACTIVITY_ID:
                activityTypeExpression(keys[i], criteria);
                break;

            // custom page keywords
            case VendorSearchParameters.CUSTOM_PAGE_KEYWORD:
                customPageExpression(keys[i], criteria);
                break;

            default:
                break;
            }
        }

        return HibernateUtil.search(hql.toString(), params);
    }

    /**
     * Prepare the search expression based on the vendor role's activity type.
     */
    private void activityTypeExpression(Object p_key, Map criteria)
    {
        Long activityId = (Long) criteria.get(p_key);
        hql.append(" and r.activity = :activityId ");
        params.put("activityId", activityId);
    }

    /**
     * Prepare the search expression based on the compnay name.
     */
    private void companyName(Object p_key, Map criteria)
    {
        String companyName = (String) criteria.get(p_key);
        String condition = (String) criteria.get(new Integer(
                VendorSearchParameters.COMPANY_NAME_CONDITION));
        hql.append(" and v.companyName like :companyName ");
        params.put("companyName", dealWithCondition(companyName, condition));
    }

    /**
     * Prepare the search expression based on the custom page keywords.
     */
    private void customPageExpression(Object p_key, Map criteria)
    {
        String keyword = (String) criteria.get(p_key);

        hql.append(" and 1=1 ");

        String[] keywords = keyword.split(",");
        for (int i = 0; i < keywords.length; i++)
        {
            hql.append(" or v.customFieldsAsSet.value like :value" + i);
            params.put("value" + i, keywords[i]);
        }
    }

    /**
     * Prepare the search expression based on the vendor role's rate. The search
     * is done based on the value of a particular rate type.
     */
    private void rateExpression(Object p_key, Map criteria)
    {
        Float rateValue = (Float) criteria.get(p_key);
        Integer rateType = (Integer) criteria.get(new Integer(
                VendorSearchParameters.RATE_TYPE));
        String operation = (String) criteria.get(new Integer(
                VendorSearchParameters.RATE_CONDITION));

        String rateColumn = Rate.UnitOfWork.WORD_COUNT.equals(rateType) ? "noMatchRate"
                : "unitRate";

        String queryString = "and r.rate." + rateColumn;

        // select values less than p_secondValue
        if (SearchCriteriaParameters.LESS_THAN.equals(operation))
        {
            hql.append(queryString + " <= :rateValue ");
        }
        // select values greater than p_firstValue
        else if (SearchCriteriaParameters.GREATER_THAN.equals(operation))
        {
            hql.append(queryString + " >= :rateValue ");
        }
        else
        {
            hql.append(queryString + " = :rateValue ");
        }

        hql.append(" and r.rate.type = :rateType ");

        params.put("rateValue", rateValue);
        params.put("rateType", rateType);
    }

    /**
     * Prepare the search expression based on the vendor role's source locale.
     */
    private void sourceLocaleExpression(Object p_key, Map criteria)
    {
        GlobalSightLocale srcLocale = (GlobalSightLocale) criteria.get(p_key);
        hql.append(" and r.localePair.source = :srcLocaleId ");
        params.put("srcLocaleId", srcLocale.getIdAsLong());
    }

    /**
     * Prepare the search expression based on the vendor role's target locale.
     */
    private void targetLocaleExpression(Object p_key, Map criteria)
    {
        GlobalSightLocale trgLocale = (GlobalSightLocale) criteria.get(p_key);
        hql.append(" and r.localePair.target = :targetLocaleId ");
        params.put("targetLocaleId", trgLocale.getIdAsLong());
    }

    /*
     * Prepare the search expression based on the vendor's first name, last
     * name, or username.
     */
    private void vendorName(Object p_key, Map criteria)
    {
        String name = (String) criteria.get(p_key);
        String fieldType = (String) criteria.get(new Integer(
                VendorSearchParameters.VENDOR_NAME_TYPE));
        String condition = (String) criteria.get(new Integer(
                VendorSearchParameters.VENDOR_NAME_CONDITION));

        hql.append(" and v.");
        if (VendorSearchParameters.VENDOR_FIRST_NAME.equals(fieldType))
        {
            hql.append("firstName");
        }
        else if (VendorSearchParameters.VENDOR_LAST_NAME.equals(fieldType))
        {
            hql.append("lastName");
        }
        else
        {
            hql.append("userId");
        }

        hql.append(" like :name ");
        params.put("name", dealWithCondition(name, condition));
    }

    private String dealWithCondition(String s, String condition)
    {
        if (!m_isCaseSensitive)
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
