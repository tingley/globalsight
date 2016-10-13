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

import com.globalsight.cxe.entity.filterconfiguration.Filter;

public class FilterComparator extends CachingStringComparator
{
    /**
     * 
     */
    private static final long serialVersionUID = 2395005441072507079L;
    // types of comparison
    public static final int NAME = 0;
    public static final int TABLENAME = 1;

    /**
     * Creates a FilterConfigurationComparator with the given type and locale.
     * If the type is not a valid type, then the default comparison is done by
     * displayName
     */
    public FilterComparator(int p_type, Locale p_locale)
    {
        super(p_type, p_locale);
    }

    public FilterComparator(Locale p_locale)
    {
        super(p_locale);
    }

    /**
     * Performs a comparison of two FilterConfiguration objects.
     */
    public int compare(java.lang.Object p_A, java.lang.Object p_B)
    {
        Filter a = (Filter) p_A;
        Filter b = (Filter) p_B;

        String aValue;
        String bValue;
        int rv;

        switch (m_type)
        {
            default:
            case NAME:
                aValue = a.getFilterName();
                bValue = b.getFilterName();
                rv = this.compareStrings(aValue, bValue);
                break;
        }
        return rv;
    }
}
