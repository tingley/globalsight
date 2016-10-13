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

import java.util.Comparator;
import java.util.Locale;
import com.globalsight.calendar.ReservedTime;
import com.globalsight.everest.foundation.Timestamp;
import com.globalsight.everest.webapp.pagehandler.administration.calendars.ReservedTimeState;

/**
* This class can be used to compare ReservedTime objects as well as
* ReservedTimeState objects (as a separate list of objects).
*/
public class ReservedTimeComparator extends StringComparator
{
    //types of ReservedTime comparison
    public static final int NAME = 0;
    public static final int TYPE = 1;
    public static final int TIME = 2;


    /**
    * Creates a ReservedTimeComparator with the given locale.
    */
    public ReservedTimeComparator(Locale p_locale)
    {
        super(p_locale);
    }
    
    /**
     * Creates a ReservedTimeComparator with the given type and locale.
     * If the type is not a valid type, then the default comparison
     * is done by the reserved time's subject.
     */
    public ReservedTimeComparator(int p_type, Locale p_locale)
    {
        super(p_type, p_locale);
    }

    /**
    * Performs a comparison of two ReservedTimeState objects.
    */
    public int compare(java.lang.Object p_A, java.lang.Object p_B)
    {
        ReservedTime a = null;
        ReservedTime b = null;

        if (p_A instanceof ReservedTimeState)
        {
            a = ((ReservedTimeState)p_A).getReservedTime();
            b = ((ReservedTimeState)p_B).getReservedTime();
        }
        else
        {
            a = (ReservedTime) p_A;
            b = (ReservedTime) p_B;
        }
        

        String aValue;
        String bValue;
        int rv = 0;

        switch (m_type)
        {
            case NAME:
                aValue = a.getSubject();
                bValue = b.getSubject();
                rv = this.compareStrings(aValue,bValue);
                break;
            case TYPE:
                aValue = a.getType();
                bValue = b.getType();
                rv = this.compareStrings(aValue,bValue);
                break;
            case TIME:
                Timestamp aTS = a.getStartTimestamp();
                Timestamp bTS = b.getStartTimestamp();
                if (aTS.isGreaterThan(bTS))
                    rv = 1;
                else if (aTS.isEqualTo(bTS))
                    rv = 0;
                else
                    rv = -1;
                break;
            default:
                aValue = a.getSubject();
                bValue = b.getSubject();
                rv = this.compareStrings(aValue,bValue);
                break;
        }
        return rv;
    }
}
