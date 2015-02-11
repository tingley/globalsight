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

/**
 * Event is an abstract class that represents a generic event.  Concrete
 * subclasses include ScheduledEvent and UnscheduledEvent, which provide
 * behavior that coincides with the current state of the event.
 */
public abstract class Event
{
    //
    // PRIVATE MEMBER VARIABLES
    //
    private EventInfo m_info;
    
    //
    // PUBLIC CONSTRUCTORS
    //
    /**
     * Create an instance of the event.
     */
    public Event()
    {
        m_info = new EventInfo();        
    }

    //
    // PACKAGE-SCOPE METHODS
    //
    /**
     * Set the event info for this event.
     *
     * @param p_eventInfo the new value to use
     */
    void setEventInfo(EventInfo p_eventInfo)
    {
        m_info = p_eventInfo;
    }

    //
    // PUBLIC METHODS
    //
    /**
     * Add the given key-value pair to the event's info object.
     *
     * @param p_key the key of the pair
     * @param p_value the value of the pair
     */
    public void addInfoEntry(Object p_key, Object p_value)
    {
        m_info.addEntry(p_key, p_value);
    }

    /**
     * Find the info entry value that corresponds to the given key, or null
     * if none exists.
     *
     * @param p_key the key to search for in the info.
     *
     * @return the string that corresponds to the given key, or null.
     */
    public Object findInfoEntryValue(Object p_key)
    {
        return m_info.findEntryValue(p_key);
    }

    /**
     * Return the event info for this event.
     *
     * @return the event info
     */
    public EventInfo getEventInfo()
    {
        return m_info;
    }
}
