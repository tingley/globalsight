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

import com.globalsight.everest.page.Page;

/**
 * This class can be used to compare Page objects by their external page id
 * (page name).
 */
public class PageComparator extends CachingStringComparator
{
    // types of comparison
    public static final int EXTERNAL_PAGE_ID = 0;

    /**
     * Creates a PageComparator with the given type and locale. If the type is
     * not a valid type, then the default comparison is done by external page
     * id.
     */
    public PageComparator(int p_type, Locale p_locale)
    {
        super(p_type, p_locale);
    }

    /**
     * Performs a comparison of two TargetPage objects.
     */
    public int compare(Object p_A, Object p_B)
    {
        Page a = (Page) p_A;
        Page b = (Page) p_B;

        String aMainName;
        String aSubName;
        String bMainName;
        String bSubName;
        int rv;

        switch (m_type)
        {
            case EXTERNAL_PAGE_ID:
                aMainName = this.getMainFileName(a.getExternalPageId());
                aSubName = this.getSubFileName(a.getExternalPageId());
                bMainName = this.getMainFileName(b.getExternalPageId());
                bSubName = this.getSubFileName(b.getExternalPageId());

                rv = this.compareStrings(aMainName, bMainName);
                if (rv == 0)
                {
                	if (aSubName.matches("\\(sheet\\d+\\)") && bSubName.matches("\\(sheet\\d+\\)"))
                	{
                		String n1 = aSubName.substring(6, aSubName.length() - 1);
                		String n2 = bSubName.substring(6, bSubName.length() - 1);
                		return Integer.parseInt(n1) - Integer.parseInt(n2);
                	}
                    rv = this.compareStrings(aSubName, bSubName);
                }

                break;

            default:
                aMainName = getMainFileName(a.getExternalPageId());
                aSubName = getSubFileName(a.getExternalPageId());
                bMainName = getMainFileName(b.getExternalPageId());
                bSubName = getSubFileName(b.getExternalPageId());

                rv = this.compareStrings(aMainName, bMainName);
                if (rv == 0)
                {
                    rv = this.compareStrings(aSubName, bSubName);
                }

                break;
        }

        return rv;
    }
}
