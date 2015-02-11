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

import com.globalsight.everest.foundation.User;

/**
 * This class can be used to compare User objects
 */
public class UserComparator extends StringComparator
{
    // types of User comparison
    public static final int DISPLAYNAME = 0;
    public static final int USERNAME = 1;
    public static final int EMAIL = 2;
    public static final int FIRSTNAME = 3;
    public static final int LASTNAME = 4;

    /**
     * Creates a UserComparator with the given locale.
     */
    public UserComparator(Locale p_locale)
    {
        super(p_locale);
    }

    /**
     * Creates a UserComparator with the given type and locale. If the type is
     * not a valid type, then the default comparison is done by displayName
     */
    public UserComparator(int p_type, Locale p_locale)
    {
        super(p_type, p_locale);
    }

    /**
     * Performs a comparison of two User objects.
     */
    public int compare(java.lang.Object p_A, java.lang.Object p_B)
    {
        User a = (User) p_A;
        User b = (User) p_B;

        String aValue;
        String bValue;
        int rv;

        switch (m_type)
        {
            case DISPLAYNAME:
                aValue = a.getDisplayName(getLocale());
                bValue = b.getDisplayName(getLocale());
                rv = this.compareStrings(aValue, bValue);
                break;
            case USERNAME:
                aValue = a.getUserName();
                bValue = b.getUserName();
                rv = this.compareStrings(aValue, bValue);
                break;
            case EMAIL:
                aValue = a.getEmail();
                bValue = b.getEmail();
                rv = this.compareStrings(aValue, bValue);
                break;
            case FIRSTNAME:
                aValue = a.getFirstName();
                bValue = b.getFirstName();
                rv = this.compareStrings(aValue, bValue);
                break;
            case LASTNAME:
                aValue = a.getLastName();
                bValue = b.getLastName();
                rv = this.compareStrings(aValue, bValue);
                break;
            default:
                aValue = a.getDisplayName(getLocale());
                bValue = b.getDisplayName(getLocale());
                rv = aValue.compareTo(bValue);
                break;
        }
        return rv;
    }
}
