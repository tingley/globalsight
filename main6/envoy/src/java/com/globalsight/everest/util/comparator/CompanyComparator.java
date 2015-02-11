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

import com.globalsight.everest.company.Company;
import java.util.Locale;

/**
* This class can be used to compare Company objects
*/
public class CompanyComparator extends StringComparator 
{
    //types of comparison
    public static final int NAME = 0;
    public static final int DESC = 1;

    /**
     * Creates a ActivityComparator with the given type and locale.
     * If the type is not a valid type, then the default comparison
     * is done by displayName
     */
    public CompanyComparator(Locale p_locale) 
    {
        super(p_locale);
    }

    public CompanyComparator(int p_type, Locale p_locale)
    {
        super(p_type, p_locale);
    }

    /**
     * Performs a comparison of two Tm objects.
     */
    public int compare(java.lang.Object p_A, java.lang.Object p_B) 
    {
        Company a = (Company) p_A;
        Company b = (Company) p_B;
        
        String aValue;
        String bValue;
        int rv;
        
        switch (m_type)
        {
            case NAME:
                aValue = a.getCompanyName();
                bValue = b.getCompanyName();
                rv = this.compareStrings(aValue,bValue);
                break;
            case DESC:
                aValue = a.getDescription();
                bValue = b.getDescription();
                rv = this.compareStrings(aValue,bValue);
                break;
            default:
                aValue = a.getCompanyName();
                bValue = b.getCompanyName();
                rv = this.compareStrings(aValue,bValue);
                break;
            }
            return rv;
	}
}
