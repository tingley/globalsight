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

import com.globalsight.everest.foundation.SearchCriteriaParameters;
import com.globalsight.util.GlobalSightLocale;

/**
* This class is a subclass of SearchCriteriaParameters which sets
* all the values required for building the query expression used
* for searching vendors.
*/
public class VendorSearchParameters extends SearchCriteriaParameters
{
    private static final long serialVersionUID = 4222127153944827375L;
    
    public static final int VENDOR_NAME = 0;
    public static final int VENDOR_NAME_TYPE = 1;
    public static final int VENDOR_NAME_CONDITION = 2;
    public static final int COMPANY_NAME = 3;
    public static final int COMPANY_NAME_CONDITION = 4;
    public static final int SOURCE_LOCALE = 5;
    public static final int TARGET_LOCALE = 6;
    public static final int RATE_TYPE = 7;
    public static final int RATE_CONDITION = 8;
    public static final int RATE_VALUE = 9;
    public static final int ACTIVITY_ID = 10;
    public static final int CUSTOM_PAGE_KEYWORD = 11;

    public static final String USER_ID = "UID";
    public static final String VENDOR_FIRST_NAME = "VFN";
    public static final String VENDOR_LAST_NAME = "VLN";
    /**
     *  Default constructor.
     */
    public VendorSearchParameters() 
    {
        super();
    }

    //
    // Helper Methods
    //
    /**
     * Set the vendor name to be searched.
     */
    public void setVendorName(String p_name) 
    {
        addElement(VENDOR_NAME, p_name);
    }

    /**
     * Set the vendor name type for searching (i.e.
     * first name, last name, or custom vendor id).
     */
    public void setVendorNameType(String p_nameType) 
    {
        addElement(VENDOR_NAME_TYPE, p_nameType);
    }

    /**
     * Set the search condition for the vendor name (i.e.
     * begins with, contains, and etc.)
     */
    public void setVendorKey(String p_key)
    {
        addElement(VENDOR_NAME_CONDITION, p_key);
    }

    /**
     * Set the company name to be searched.
     */
    public void setCompanyName(String p_companyName)
    {
        addElement(COMPANY_NAME, p_companyName);
    }

    /**
     * Set the search condition for the company name (i.e.
     * begins with, contains, and etc.)
     */
    public void setCompanyNameKey(String p_companyNameKey)
    {
        addElement(COMPANY_NAME_CONDITION, p_companyNameKey);
    }

    /**
     * Set the source locale to be searched.
     */
    public void setSourceLocale(GlobalSightLocale p_sourceLocale)
    {
        addElement(SOURCE_LOCALE, p_sourceLocale);
    }

    /**
     * Set the target locale to be searched.
     */
    public void setTargetLocale(GlobalSightLocale p_targetLocale)
    {
        addElement(TARGET_LOCALE, p_targetLocale);
    }

    /**
     * Set the rate type used for searching by rate.
     */
    public void setRateType(Integer p_rateType)
    {
        addElement(RATE_TYPE, p_rateType);
    }

    /**
     * Set the condition for searching the rate (i.e. greater
     * than, less than, or equals to).
     */
    public void setRateCondition(String p_rateCondition)
    {
        addElement(RATE_CONDITION, p_rateCondition);
    }

    /**
     * Set the rate id to be searched.
     */
    public void setRateValue(float p_rateValue)
    {
        addElement(RATE_VALUE, new Float(p_rateValue));
    }

    public void setActivityId(long p_activityId)
    {
        addElement(ACTIVITY_ID, new Long(p_activityId));
    }

    /**
     * Set the custom page keywords to be searched.  The 
     * value is a comma delimited string.
     */
    public void setCustomPageKeyword(String p_keyword)
    {
        addElement(CUSTOM_PAGE_KEYWORD, p_keyword);
    }
}

