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

import com.globalsight.everest.webapp.pagehandler.administration.remoteServices.perplexity.PerplexityService;

/**
 * This class can be used to compare Perplexity.
 */
public class PerplexityComparator extends StringComparator
{
    private static final long serialVersionUID = -7196747104191119958L;

    public static final int NAME = 0;
    public static final int ID = 1;
    public static final int USER_NAME = 2;
    public static final int URL = 4;
    public static final int DESC = 5;
    public static final int GS_COMPANY_NAME = 6;

    /**
     * Creates a PerplexityComparator with the given type and locale. If the
     * type is not a valid type, then the default comparison is done by
     * displayName
     */
    public PerplexityComparator(int p_type, Locale p_locale)
    {
        super(p_type, p_locale);
    }

    public PerplexityComparator(Locale p_locale)
    {
        super(p_locale);
    }

    /**
     * Performs a comparison of two PerplexityService objects.
     */
    public int compare(java.lang.Object p_A, java.lang.Object p_B)
    {
        PerplexityService a = (PerplexityService) p_A;
        PerplexityService b = (PerplexityService) p_B;

        String aValue;
        String bValue;
        int rv;

        switch (m_type)
        {
        default:
        case NAME:
            aValue = a.getName();
            bValue = b.getName();
            rv = this.compareStrings(aValue, bValue);
            break;
        case ID:
            rv = (int)(a.getId() - b.getId());
            break;
        case USER_NAME:
            aValue = a.getUserName();
            bValue = b.getUserName();
            rv = this.compareStrings(aValue, bValue);
            break;
        case URL:
            aValue = a.getUrl();
            bValue = b.getUrl();
            rv = this.compareStrings(aValue, bValue);
            break;
        case DESC:
            aValue = a.getDescription();
            bValue = b.getDescription();
            rv = this.compareStrings(aValue, bValue);
            break;   
        case GS_COMPANY_NAME:
            aValue = a.getGsCompany();
            bValue = b.getGsCompany();
            rv = this.compareStrings(aValue, bValue);
            break;    
            
        }
        return rv;
    }
}
