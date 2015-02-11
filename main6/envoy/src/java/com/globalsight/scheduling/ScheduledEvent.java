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
 * ScheduledEvent extends Event by providing additional functionality and
 * features pertinent to an event that has been scheduled.  This is most
 * important during event handling.
 */
public class ScheduledEvent 
    extends Event
{
    //
    // PRIVATE MEMBER VARIABLES
    //
    private String m_id;
    private String m_state;

    //
    // PUBLIC CONSTRUCTORS
    //
    /**
     * Construct a ScheduledEvent.
     */
    public ScheduledEvent()
    {
        super();
    }

    //
    // PACKAGE-SCOPE METHODS
    //
    /**
     * Set the value of the event's id.
     *
     * @param p_id the new id value.
     */
    void setId(String p_id)
    {
        m_id = p_id;
    }

    /**
     * Set the value of the event's state.
     *
     * @param p_state the new state value.
     */
    void setState(String p_state)
    {
        m_state = p_state;
    }

    //
    // PUBLIC METHODS
    //
    /**
     * Return the id of the scheduled event.
     *
     * @return the id.
     */
    public String getId()
    {
        return m_id;
    }

    /**
     * Return the state of the scheduled event.
     *
     * @return the state.
     */
    public String getState()
    {
        return m_state;
    }
}
