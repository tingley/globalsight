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

package com.globalsight.everest.projecthandler.importer;

/**
 * This class wraps the data read from the file and prepares it
 * from creation of a reserved time object.
 */
public class Entry
{
    private String m_subject = null;
    private String m_type = null;
    private String m_username = null;
    private String m_startDate = null;
    private String m_startDateFormatType = null;
    private String m_endDate = null;
    private String m_endDateFormatType = null;


    /**
     * The default constructor.
     */
    public Entry()
    {
        super();
    }

    //////////////////////////////////////////////////////////////////////
    //  Begin: Public Methods
    //////////////////////////////////////////////////////////////////////
    /**
     * Get the event/activity type.
     * @return The event/activity type.
     */
    public String getActivityType()
    {
        return m_type;
    }

    /**
     * Get the end date.
     * @return The string representation of end date.
     */
    public String getEndDate()
    {
        return m_endDate;
    }

    /**
     * Get the format type for the end date.
     * @return The format type used for end date.
     */
    public String getEndDateFormatType()
    {
        return m_endDateFormatType;
    }

    /**
     * Get the start date.
     * @return The string representation of start date.
     */
    public String getStartDate()
    {
        return m_startDate;
    }

    /**
     * Get the format type for the start date.
     * @return The format type used for start date.
     */
    public String getStartDateFormatType()
    {
        return m_startDateFormatType;
    }

    /**
     * Get the subject of the event/activity.
     * @return The subject of the reserved time.
     */
    public String getSubject()
    {
        return m_subject;
    }

    /**
     * Get the username (calendar owner).
     * @return The username of the calendar owner.
     */
    public String getUsername()
    {
        return m_username;
    }

    /**
     * Determines whether this object is a valid entry.
     * @return True if ALL the attribtes are non-null.  Otherwise, returns false.
     */
    public boolean isEntryValid()
    {
        return m_type != null &&
            m_endDate != null &&
            m_endDateFormatType != null && 
            m_startDate != null &&
            m_startDateFormatType != null && 
            m_subject != null && 
            m_username != null;
    }

    /**
     * Set the event/activity type to be the specified value.
     * @param p_type - The event/activity type.
     */
    public void setActivityType(String p_type)
    {
        m_type = p_type;
    }

    /**
     * Set the end date to be the specified value.
     * @param p_endDate - The string representation of end date.
     */
    public void setEndDate(String p_endDate)
    {
        m_endDate = p_endDate;
    }

    /**
     * Set the format type for the end date to be the specified value.
     * @param p_endDateFormatType - The format type used for end date.
     */
    public void setEndDateFormatType(String p_endDateFormatType)
    {
        m_endDateFormatType = p_endDateFormatType;
    }

    /**
     * Set the start date to be the specified value.
     * @param p_startDate - The string representation of start date.
     */
    public void setStartDate(String p_startDate)
    {
        m_startDate = p_startDate;
    }

    /**
     * Set the format type for the start date to be the specified value.
     * @param p_startDateFormatType - The format type used for start date.
     */
    public void setStartDateFormatType(String p_startDateFormatType)
    {
        m_startDateFormatType = p_startDateFormatType;
    }

    /**
     * Set the subject of this entry to be the specified value.
     * @param p_subject - The subject of the entry.
     */
    public void setSubject(String p_subject)
    {
        m_subject = p_subject;
    }

    /**
     * Set the username (calendar owner) to be the specified value.
     * @param p_username - The username of the calendar owner.
     */
    public void setUsername(String p_username)
    {
        m_username = p_username;
    }
}
