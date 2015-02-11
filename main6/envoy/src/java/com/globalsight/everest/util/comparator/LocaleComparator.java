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
import com.globalsight.util.EnvoyDataComparator;
import com.globalsight.util.GlobalSightLocale;

/**
 * To serve for sorting locales based on specified display string. 
*/
public class LocaleComparator extends EnvoyDataComparator
{
    
    private Locale m_uiLocale = null;
    //////////////////////////////////////////////////////////////////////////////////
    //  Begin:  Constructor
    //////////////////////////////////////////////////////////////////////////////////
    /**
    * Construct a LocaleComparator for sorting purposes based on a particular locale.
    * @param sortCol - The parameter represents an index used for an attribute of
    * the object to be sorted (i.e. 0 for displayName, 1 for displayCountry, and etc.).    
    */
    public LocaleComparator(int p_sortCol, Locale p_uiLocale)
    {
        super(p_sortCol, p_uiLocale);
        m_uiLocale = p_uiLocale;
    }
    //////////////////////////////////////////////////////////////////////////////////
    //  End:  Constructor
    //////////////////////////////////////////////////////////////////////////////////


    public Object[] getComparableObjects(Object o1, Object o2, int sortColumn)
    {
        Object objects[] = new Object[2];
        // our objects are Task
        if(o1 instanceof GlobalSightLocale && o2 instanceof GlobalSightLocale)
        {
            objects = getValues(objects, o1, o2, sortColumn);
        }
        else
        {
            objects[0] = o1;
            objects[1] = o2;
        }

        return objects;
    }

    private Object[] getValues(Object[] p_objects, Object o1, Object o2, int p_sortColumn)
    {

        switch(p_sortColumn)
        {
        default:  // always display string
            p_objects[0] = ((GlobalSightLocale)o1).getDisplayName();
            p_objects[1] = ((GlobalSightLocale)o2).getDisplayName();
            break;
        case 1: // display string 
            p_objects[0] = ((GlobalSightLocale)o1).getDisplayName();
            p_objects[1] = ((GlobalSightLocale)o2).getDisplayName();
            break;

        case 2: // display string based on a locale
            p_objects[0] = ((GlobalSightLocale)o1).getDisplayName(m_uiLocale);
            p_objects[1] = ((GlobalSightLocale)o2).getDisplayName(m_uiLocale);
            break;        
        }

        return p_objects;
    }
}

