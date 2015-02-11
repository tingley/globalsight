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

import com.globalsight.everest.persistence.PersistentObject;

/**
 * FluxEventMap contains event related info used for updating the status of
 * a job when necessary (i.e. when a user accepts an activity, the event for
 * sending deadline email should be removed).
 */

public class FluxEventMap extends PersistentObject
{
    private static final long serialVersionUID = 3865171265777345999L;

    // PRIVATE MEMBER VARIABLES
    private String m_eventId = null;
    private int m_eventType = -1;
    private long m_domainObjectId = -1;
    private int m_domainObjectType = -1;

    // ////////////////////////////////////////////////////////////////////
    // Begin: Constructor
    // ////////////////////////////////////////////////////////////////////
    /**
     * Create an initialized FluxEventMap.
     */
    public FluxEventMap()
    {
        super();
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Constructor
    // ////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////
    // Begin: Public Methods
    // ////////////////////////////////////////////////////////////////////
    /**
     * Get the id of the event.
     * 
     * @return the event id.
     */
    public String getEventId()
    {
        return m_eventId;
    }

    /**
     * Get the type of the event.
     * 
     * @return a valid event type (based on the types in SchedulerConstants).
     */
    public int getEventType()
    {
        return m_eventType;
    }

    /**
     * Get the id of the domain object.
     * 
     * @return the domain object id.
     */
    public long getDomainObjectId()
    {
        return m_domainObjectId;
    }

    /**
     * Get the domain object type as an integer. The value is based on the
     * object's class type (<domainObject>.class).
     * 
     * @return the type of a particular domain object.
     */
    public int getDomainObjectType()
    {
        return m_domainObjectType;
    }

    /**
     * OVERRIDE: Return a string representation of the receiver.
     * 
     * @return a description of the time expression.
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append(getClass().getName());
        sb.append("[Event id=\"");
        sb.append(getEventId());
        sb.append("\", Event type=\"");
        sb.append(getEventType());
        sb.append("\", domain object id=\"");
        sb.append(getDomainObjectId());
        sb.append("\", domain object type=\"");
        sb.append(getDomainObjectType());
        sb.append("\"]");
        return sb.toString();
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Public Methods
    // ////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////
    // Begin: Package-scope Methods
    // ////////////////////////////////////////////////////////////////////
    /**
     * Set the value of the event id based on scheduled job id.
     * 
     * @param p_eventId
     *            the id of the event.
     */
    void setEventId(String p_eventId)
    {
        m_eventId = p_eventId;

    }

    /**
     * Set the value of the event type to a valid type. SchedulerConstants
     * contains a list of valid event types.
     * 
     * @param p_eventType
     *            the type of the event.
     */
    void setEventType(int p_eventType)
    {
        m_eventType = p_eventType;
    }

    /**
     * Set the value of the domain object id.
     * 
     * @param p_domainObjectId
     *            The id of the domain object.
     */
    void setDomainObjectId(long p_domainObjectId)
    {
        m_domainObjectId = p_domainObjectId;
    }

    /**
     * Set the value of the domain object type. This value is based on the
     * <domain object>.class. The valid values are inserted into database and
     * loaded into a HashMap where the key is the class and the returned value
     * is the domain object type.
     * 
     * @param p_domainObjectType
     *            the type of the domain object represented as an integer.
     */
    void setDomainObjectType(int p_domainObjectType)
    {
        m_domainObjectType = p_domainObjectType;
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Package-scope Methods
    // ////////////////////////////////////////////////////////////////////
}
