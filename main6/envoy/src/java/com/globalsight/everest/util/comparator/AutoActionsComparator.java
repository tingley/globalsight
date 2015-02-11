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
import com.globalsight.everest.autoactions.AutoAction;

/**
* This class can be used to compare Activity objects
*/
public class AutoActionsComparator extends StringComparator
{
    public static final int NAME = 0;
    public static final int MAILADDRESS = 1;
    public static final int DESCRIPTION = 2;
    
    public AutoActionsComparator(Locale pLocale) {
        super(pLocale);
    }

    public AutoActionsComparator(int pType, Locale pLocale) {
        super(pType, pLocale);
    }

    public int compare(java.lang.Object p_A, java.lang.Object p_B) 
    {
        AutoAction a = (AutoAction) p_A;
        AutoAction b = (AutoAction) p_B;
        
        String aValue;
        String bValue;
        int rv;
        
        switch (m_type)
        {
            case NAME:
                aValue = a.getName();
                bValue = b.getName();
                rv = this.compareStrings(aValue,bValue);
                break;
            case MAILADDRESS:
                aValue = a.getEmail();
                bValue = b.getEmail();
                rv = this.compareStrings(aValue,bValue);
                break;
            case DESCRIPTION:
                aValue = a.getDescription();
                bValue = b.getDescription();
                rv = this.compareStrings(aValue,bValue);
                break;
            default:
                aValue = a.getName();
                bValue = b.getName();
                rv = this.compareStrings(aValue,bValue);
                break;
            }

            return rv;
    }
}
