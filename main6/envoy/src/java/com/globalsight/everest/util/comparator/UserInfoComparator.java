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

import com.globalsight.everest.usermgr.UserInfo;

/**
 * This class can be used to compare UserInfo objects
 */
public class UserInfoComparator extends StringComparator
{
    // types of User comparison
    public static final int USERID = 1;
    public static final int FIRSTNAME = 2;
    public static final int LASTNAME = 3;
    public static final int FULL_NAME = 4;

    /**
     * Creates a UserInfoComparator with the given locale.
     */
    public UserInfoComparator(Locale p_locale)
    {
        super(p_locale);
    }

    /**
     * Creates a UserInfoComparator with the given type and locale. If the type
     * is not a valid type, then the default comparison is done by displayName
     */
    public UserInfoComparator(int p_type, Locale p_locale)
    {
        super(p_type, p_locale);
    }

    /**
     * Performs a comparison of two UserInfo objects.
     */
    public int compare(java.lang.Object p_A, java.lang.Object p_B)
    {
        UserInfo a = (UserInfo) p_A;
        UserInfo b = (UserInfo) p_B;

        String aValue;
        String bValue;
        int rv;

        switch (m_type)
        {
            case USERID:
                aValue = a.getUserName();
                bValue = b.getUserName();
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
            case FULL_NAME:
                aValue = a.getFullName();
                bValue = b.getFullName();
                rv = this.compareStrings(aValue, bValue);
                break;
            default:
                aValue = a.getUserName();
                bValue = b.getUserName();
                rv = this.compareStrings(aValue, bValue);
                break;
        }
        return rv;
    }
}
