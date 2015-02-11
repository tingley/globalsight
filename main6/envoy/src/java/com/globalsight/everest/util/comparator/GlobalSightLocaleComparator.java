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

import java.util.Comparator;
import java.util.Locale;
import com.globalsight.util.GlobalSightLocale;

/**
 * This class can be used to compare GlobalSightLocale objects
 */
public class GlobalSightLocaleComparator
    extends StringComparator
{
    //types of GlobalSightLocale comparison
    public static final int DISPLAYNAME = 0;
    public static final int ISO_CODE = 1;

    /**
     * Creates a GlobalSightLocaleComparator with the given type and locale.
     * If the type is not a valid type, then the default comparison
     * is done by displayName
     */
    public GlobalSightLocaleComparator(int p_type, Locale p_locale)
    {
        super(p_type, p_locale);
    }

    public GlobalSightLocaleComparator(Locale p_locale)
    {
        super(p_locale);
    }

    /**
     * Performs a comparison of two GlobalSightLocale objects.
     */
    public int compare(java.lang.Object p_A, java.lang.Object p_B)
    {
        GlobalSightLocale a = (GlobalSightLocale) p_A;
        GlobalSightLocale b = (GlobalSightLocale) p_B;

        String aValue;
        String bValue;
        int rv;

        switch (m_type)
        {
        case DISPLAYNAME:
            aValue = a.getDisplayName();
            bValue = b.getDisplayName();
            rv = this.compareStrings(aValue, bValue);
            break;
        case ISO_CODE:
            aValue = a.toString();
            bValue = b.toString();
            rv = this.compareStrings(aValue, bValue);
            break;
        default:
            aValue = a.getDisplayName();
            bValue = b.getDisplayName();
            rv = this.compareStrings(aValue, bValue);
            break;
        }

        return rv;
    }
}
