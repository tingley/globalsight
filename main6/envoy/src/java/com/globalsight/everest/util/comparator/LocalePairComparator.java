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
import com.globalsight.everest.foundation.LocalePair;

/**
 * This class can be used to compare LocalePair objects
 */
public class LocalePairComparator extends StringComparator
{
    // types of comparison
    public static final int BOTH = 0;
    public static final int SRC = 1;
    public static final int TARG = 2;
    public static final int ASC_COMPANY = 3;

    /**
     * Creates a LocalePairComparator with the given locale.
     */
    public LocalePairComparator(Locale p_locale)
    {
        super(p_locale);
    }

    /**
     * Performs a comparison of two LocalePair objects.
     */
    public int compare(java.lang.Object p_A, java.lang.Object p_B)
    {
        LocalePair a = (LocalePair) p_A;
        LocalePair b = (LocalePair) p_B;
        int rv;

        switch (m_type)
        {
            case BOTH:
                String aSource = a.getSource().getDisplayName(getLocale());
                String bSource = b.getSource().getDisplayName(getLocale());
                rv = this.compareStrings(aSource, bSource);

                // if the src locales are the same, do a secondary
                // comparison of the target locales
                if (rv == 0)
                {
                    String aTarget = a.getTarget().getDisplayName(getLocale());
                    String bTarget = b.getTarget().getDisplayName(getLocale());
                    rv = this.compareStrings(aTarget, bTarget);
                }
                break;
            default:
            case SRC:
                aSource = a.getSource().getDisplayName(getLocale());
                bSource = b.getSource().getDisplayName(getLocale());
                rv = this.compareStrings(aSource, bSource);
                break;
            case TARG:
                aSource = a.getTarget().getDisplayName(getLocale());
                bSource = b.getTarget().getDisplayName(getLocale());
                rv = this.compareStrings(aSource, bSource);
                break;
            case ASC_COMPANY:
                aSource = CompanyWrapper.getCompanyNameById(String.valueOf(a
                        .getCompanyId()));
                bSource = CompanyWrapper.getCompanyNameById(String.valueOf(b
                        .getCompanyId()));
                rv = this.compareStrings(aSource, bSource);
                break;
        }
        return rv;
    }
}
