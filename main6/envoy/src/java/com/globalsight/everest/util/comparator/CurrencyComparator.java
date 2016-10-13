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
package com.globalsight.everest.util.comparator;

import java.util.Locale;

import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.costing.Currency;

/**
 * This class can be used to compare Currenct objects
 */
public class CurrencyComparator extends StringComparator
{
    // types of comparison
    public static final int NAME = 0;
    public static final int CONVERSION = 1;
    public static final int ASC_COMPANY = 2;

    /**
     * Creates a CurrencyComparator with the given type and locale. If the type
     * is not a valid type, then the default comparison is done by displayName
     */
    public CurrencyComparator(int p_type, Locale p_locale)
    {
        super(p_type, p_locale);
    }

    public CurrencyComparator(Locale p_locale)
    {
        super(p_locale);
    }

    /**
     * Performs a comparison of two Currency objects.
     */
    public int compare(java.lang.Object p_A, java.lang.Object p_B)
    {
        Currency a = (Currency) p_A;
        Currency b = (Currency) p_B;

        String aValue;
        String bValue;
        int rv;

        switch (m_type)
        {
            default:
            case NAME:
                aValue = a.getDisplayName();
                bValue = b.getDisplayName();
                rv = this.compareStrings(aValue, bValue);
                break;
            case CONVERSION:
                float aFloat = a.getConversionFactor();
                float bFloat = b.getConversionFactor();
                if (aFloat > bFloat)
                    rv = 1;
                else if (aFloat == bFloat)
                    rv = 0;
                else
                    rv = -1;
                break;
            case ASC_COMPANY:
                aValue = CompanyWrapper.getCompanyNameById(String.valueOf(a
                        .getCompanyId()));
                bValue = CompanyWrapper.getCompanyNameById(String.valueOf(b
                        .getCompanyId()));
                rv = this.compareStrings(aValue, bValue);
                break;
        }
        return rv;
    }
}
