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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import com.globalsight.connector.eloqua.models.EloquaObject;
import com.globalsight.connector.eloqua.models.LandingPage;

/**
 * This class can be used to compare XmlRuleFile objects
 */
public class EloquaObjectComparator extends StringComparator
{
    private static final long serialVersionUID = -7196747104191119958L;

    // types of comparison
    public static final int NAME = 0;
    public static final int ID = 1;
    public static final int AT = 2;
    public static final int BY = 3;
    public static final int STATUS = 4;
    
    /**
     * Creates a XmlRuleFileComparator with the given type and locale. If the
     * type is not a valid type, then the default comparison is done by
     * displayName
     */
    public EloquaObjectComparator(int p_type, Locale p_locale)
    {
        super(p_type, p_locale);
    }

    public EloquaObjectComparator(Locale p_locale)
    {
        super(p_locale);
    }

    /**
     * Performs a comparison of two EloquaConnector objects.
     */
    public int compare(java.lang.Object p_A, java.lang.Object p_B)
    {
        EloquaObject a = (EloquaObject) p_A;
        EloquaObject b = (EloquaObject) p_B;

        String aValue;
        String bValue;
        int rv;

        switch (m_type)
        {
        default:
        case ID:
            rv = Integer.parseInt(a.getId()) - Integer.parseInt(b.getId());
            break;
        case NAME:
            aValue = a.getName();
            bValue = b.getName();
            rv = this.compareStrings(aValue, bValue);
            break;
        case AT :
            aValue = a.getString("createdAt");
            bValue = b.getString("createdAt");
            if (aValue == null)
            {
                rv = 1;
                break;
            }
            
            if (bValue == null)
            {
                rv = -1;
                break;
            }
            Long l1 = Long.parseLong(aValue);
            Long l2 = Long.parseLong(bValue);
            rv = l1.compareTo(l2);
            break;
        case BY :
            aValue = a.getCreateBy();
            bValue = b.getCreateBy();
            rv = this.compareStrings(aValue, bValue);
            break;
        case STATUS :
            aValue = a.getStatus();
            bValue = b.getStatus();
            rv = this.compareStrings(aValue, bValue);
            break;
        }
        return rv;
    }
}
