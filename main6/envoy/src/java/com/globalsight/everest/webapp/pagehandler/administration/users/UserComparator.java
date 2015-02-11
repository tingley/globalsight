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
package com.globalsight.everest.webapp.pagehandler.administration.users;

import java.util.Locale;
import java.util.ResourceBundle;

import com.globalsight.everest.foundation.User;
import com.globalsight.everest.util.comparator.StringComparator;

/**
 * This class can be used to compare User objects
 */
public class UserComparator extends StringComparator
{
    // types of User comparison
    public static final int USERNAME = 0;
    public static final int FIRSTNAME = 1;
    public static final int LASTNAME = 2;
    public static final int ASC_COMPANY = 3;
    public static final int EMAIL=4;
    public static final int PERMISSION=5;
    public static final int PROJECT=6;

    ResourceBundle m_bundle = null;

    /**
     * Creates a UserComparator with the given locale.
     */
    public UserComparator(Locale p_locale, ResourceBundle p_bundle)
    {
        super(p_locale);
        m_bundle = p_bundle;
    }

    /**
     * Performs a comparison of two User objects.
     */
    public int compare(java.lang.Object p_A, java.lang.Object p_B)
    {
        User a = (User) p_A;
        User b = (User) p_B;

        String aValue = "";
        String bValue = "";
        int rv;

        switch (m_type)
        {
            case USERNAME:
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
            case ASC_COMPANY:
                aValue = a.getCompanyName();
                bValue = b.getCompanyName();
                rv = this.compareStrings(aValue, bValue);
                break;
            case EMAIL:
                aValue = a.getEmail();
                bValue = b.getEmail();;
                rv = this.compareStrings(aValue,bValue);
                break;
            case PERMISSION:
                aValue = a.getPermissiongNames();
                bValue = b.getPermissiongNames();
                rv = this.compareStrings(aValue,bValue);
                break;
            case PROJECT:
                aValue = a.getProjectNames();
                bValue = b.getProjectNames();
                rv = this.compareStrings(aValue,bValue);
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
