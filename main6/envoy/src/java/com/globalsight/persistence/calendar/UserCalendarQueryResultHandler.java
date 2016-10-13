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

import com.globalsight.calendar.UserFluxCalendar;

/**
 * UserCalendarQueryResultHandler provides functionality to convert a Collection
 * of ReportQueryResults into a Collection containing a single Hashtable of
 * user calendar ids & owner's usernames.  If the query for getting the time
 * zone id for a user is invoked, it'll return a collection with a single
 * time zone id.
 */
public class UserCalendarQueryResultHandler
{
    /**
     * Convert the given collection of ReportQueryResults into a Collection
     * containing either a time zone id for a given user calendar or a map of
     * user calendar ids as keys and owner usernames as values.
     *
     * @param p_collection the collection of ReportQueryResults that is
     * created when the original query is executed
     *
     * @return a collection of either a time zone id or a map with calendar
     * ids as keys and usernames as values.
     */
    public static Collection handleResult(Collection p_collection)
    {
        Collection c = new ArrayList();
        Object[] values = p_collection.toArray();
        if (values.length == 0)
        {
            return c;
        }

        UserFluxCalendar calendar = (UserFluxCalendar)values[0];
        String timeZoneId = calendar.getTimeZoneId();
        if (timeZoneId == null)
        {         
            return handleResult(values, c);
        }
        else
        {
            c.add(timeZoneId);
            return c;
        }
    }

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
    public static Collection handleResult(Object[] p_values, 
                                          Collection p_returnList)
    {
        HashMap hm = new HashMap();
        for (int i = 0; i < p_values.length; i++)
        {
            UserFluxCalendar calendar = (UserFluxCalendar)p_values[i];
            hm.put(calendar.getIdAsLong(), calendar.getOwnerUserId());
        }
        if (!hm.isEmpty())
        {
            p_returnList.add(hm);
        }
        
        return p_returnList;
    }
}
