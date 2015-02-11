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

import com.globalsight.terminology.java.TbLanguage;

public class TbLanguageComparator extends StringComparator
{
    private static final long serialVersionUID = -6758549281229546070L;
    // types of comparison
    public static final int NAME = 0;
    public static final int LOCALE = 1;

    /**
     * Creates a TbLanguageComparator with the given type and locale. If the
     * type is not a valid type, then the default comparison is done by
     * displayName
     */
    public TbLanguageComparator(int p_type, Locale p_locale)
    {
        super(p_type, p_locale);
    }

    public TbLanguageComparator(Locale p_locale)
    {
        super(p_locale);
    }

    /**
     * Performs a comparison of two TbLanguage objects.
     */
    public int compare(java.lang.Object p_A, java.lang.Object p_B)
    {
        TbLanguage a = (TbLanguage) p_A;
        TbLanguage b = (TbLanguage) p_B;

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
            case LOCALE:
                aValue = a.getLocal();
                bValue = b.getLocal();
                rv = this.compareStrings(aValue, bValue);
                break;
        }
        return rv;
    }
}
