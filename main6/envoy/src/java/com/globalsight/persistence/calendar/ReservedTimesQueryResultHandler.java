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
package com.globalsight.persistence.calendar;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import com.globalsight.calendar.ReservedTime;
import com.globalsight.calendar.UserFluxCalendar;

/**
 * ReservedTimesQueryResultHandler provides functionality to convert a Collection
 * of ReservedTime objects into a Collection containing a single Hashtable of
 * user calendars & a list of their reserved times.  The reserved times are
 * grouped based on their user calendar.
 */
public class ReservedTimesQueryResultHandler
{
    /**
     * Convert the given collection of ReportQueryResults into a Collection
     * containing a single Hashtable of UserFluxCalendar ids & owner usernames.
     *
     * @param p_collection the collection of ReportQueryResults that is created
     * when the original query is executed
     *
     * @return a collection containing a single Hashtable of UserFluxCalendar
     * key (id) -> value (owner username) pairs
     */
    public static Collection handleResult(Collection p_collection)
    {
        HashMap hm = new HashMap();
        Iterator it = p_collection.iterator();
        while (it.hasNext())
        {
            ReservedTime result = (ReservedTime)it.next();

            UserFluxCalendar cal = result.getUserFluxCalendar();
            ArrayList content = (ArrayList)hm.get(cal);

            if (content == null)
            {
                ArrayList l = new ArrayList();
                l.add(result);
                hm.put(cal, l);
            }
            else
            {
                content.add(result);
            }            
        }
        Collection c = new ArrayList(1);
        if (!hm.isEmpty())
        {
            c.add(hm);
        }
        
        return c;
    }
}
