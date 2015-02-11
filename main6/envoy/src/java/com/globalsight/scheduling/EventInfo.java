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
package com.globalsight.scheduling;

import java.util.HashMap;
import java.io.Serializable;

/**
 * EventInfo is a wrapper for the FluxEventInfo class.  This
 * class allows the general specification of user-defined attributes as
 * key-value pairs that are stored on a HashMap.  When an event is actually
 * persisted into the database, the HashMap is converted into a string
 * representation; this is converted back into a HashMap through the special
 * constructor that takes a FluxEventInfo as its argument.
 */
public class EventInfo implements Serializable
{
    // This HashMap contains all the event related info.  
    public HashMap flux_event_info;
    
    
    //
    // PACKAGE-SCOPE CONSTRUCTORS
    //
    /**
     * Create a default instance of the event info.
     */
    public EventInfo()
    {
        this(new HashMap());
    }

    /**
     * Create an instance of the event info from the given HashMap.
     */
    public EventInfo(HashMap p_map)
    {
        super();
        flux_event_info = p_map;        
    }

    
    //
    // PUBLIC METHODS
    //
    /**
     * Add the given key-value pair as an info entry on the EventInfo.
     * Null keys and values are not inserted into the table.
     *
     * @param p_key the searchable string to be treated as the key
     * @param p_value the string to be treated as the value
     */
    public void addEntry(Object p_key, Object p_value)
    {
        if (p_key != null && p_value != null)
        {
            flux_event_info.put(p_key, p_value);
        }
    }

    /**
     * Find the entry value that corresponds to the given key, or null
     * if none exists.
     *
     * @param p_key the key to search for in the info.
     *
     * @return the string that corresponds to the given key, or null.
     */
    public Object findEntryValue(Object p_key)
    {
        return flux_event_info.get(p_key);
    }

    /**
     * Return the underlying hashmap.
     *
     * @return the hashmap.
     */
    public HashMap getMap()
    {
        return flux_event_info;
    }


    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("EventInfo[ ");
        sb.append(flux_event_info);
        sb.append(" ]");
        return sb.toString();
    }
}
