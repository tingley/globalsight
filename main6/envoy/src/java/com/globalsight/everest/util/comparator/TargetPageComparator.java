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

import com.globalsight.everest.page.TargetPage;

/**
 * This class can be used to compare TargetPage objects
 */
public class TargetPageComparator extends StringComparator
{
    private static final long serialVersionUID = -1695760098179606483L;
    // types of comparison
    public static final int ID = 1;
    public static final int EXTERNALPAGEID = 2;

    public TargetPageComparator(int p_type, Locale p_locale)
    {
        super(p_type, p_locale);
    }

    public TargetPageComparator(Locale p_locale)
    {
        super(p_locale);
    }

    /**
     * Performs a comparison of two TargetPage objects.
     */
    public int compare(java.lang.Object p_A, java.lang.Object p_B)
    {
        TargetPage a = (TargetPage) p_A;
        TargetPage b = (TargetPage) p_B;

        int rv = 0;

        switch (m_type)
        {
            case ID:
                rv = new Long(a.getId()).compareTo(new Long(b.getId()));
                break;
            case EXTERNALPAGEID:
                String aValue = a.getExternalPageId();
                String bValue = b.getExternalPageId();
                rv = this.compareStrings(aValue, bValue);
                break;
        }
        return rv;
    }
}
