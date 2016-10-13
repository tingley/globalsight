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

import com.globalsight.terminology.Termbase;
import java.util.Comparator;
import java.util.Locale;

/**
 * This class can be used to compare Termbase objects
 */
public class TermbaseComparator
    extends StringComparator
{
    //types of comparison
    static public final int NAME = 0;

    /**
     * Creates a TermbaseComparator with the given type and locale.
     * If the type is not a valid type, then the default comparison
     * is done by displayName
     */
    public TermbaseComparator(int p_type, Locale p_locale)
    {
        super(p_type, p_locale);
    }

    /**
     * Performs a comparison of two Termbase objects.
     */
    public int compare(Object p_A, Object p_B)
    {
        Termbase a = (Termbase)p_A;
        Termbase b = (Termbase)p_B;

        String aValue;
        String bValue;
        int rv;

        switch (m_type)
        {
        case NAME:
            aValue = a.getName();
            bValue = b.getName();
            rv = this.compareStrings(aValue, bValue);
            break;
        default:
            aValue = a.getName();
            bValue = b.getName();
            rv = this.compareStrings(aValue, bValue);
            break;
        }

        return rv;
    }
}
