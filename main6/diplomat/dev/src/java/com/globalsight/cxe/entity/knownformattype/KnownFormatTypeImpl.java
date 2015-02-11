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
package com.globalsight.cxe.entity.knownformattype;

import com.globalsight.everest.persistence.PersistentObject;

/**
 * Represents a CXE Known Format Type entity object.
 */
public class KnownFormatTypeImpl extends PersistentObject implements
        KnownFormatType
{
    private static final long serialVersionUID = -3332849379922835296L;

    // CONSTRUCTORS
    /** Default constructor for TOPLink */
    public KnownFormatTypeImpl()
    {
        m_name = null;
        m_description = null;
        m_format_type = null;
        m_pre_extract_event = null;
        m_pre_merge_event = null;
    }

    /***************************************************************************
     * Constructs an KnownFormatTypeImpl from a KnownFormatType (no deep copy)
     * 
     * @param o
     *            Another KnownFormatType object *
     **************************************************************************/
    public KnownFormatTypeImpl(KnownFormatType o)
    {
        m_name = o.getName();
        m_description = o.getDescription();
        m_format_type = o.getFormatType();
        m_pre_extract_event = o.getPreExtractEvent();
        m_pre_merge_event = o.getPreMergeEvent();
    }

    // PUBLIC METHODS

    /**
     * Return the name of the known format type
     * 
     * @return known format type name
     */
    public String getName()
    {
        return m_name;
    }

    /**
     * Return the description of the known format type
     * 
     * @return known format type description
     */
    public String getDescription()
    {
        return m_description;
    }

    /**
     * Return the format type of the known format type
     * 
     * @return known format type format type
     */
    public String getFormatType()
    {
        return m_format_type;
    }

    /**
     * Return the connection string of the known format type
     * 
     * @return known format type string
     */
    public String getPreExtractEvent()
    {
        return m_pre_extract_event;
    }

    /**
     * Return the user name of the known format type
     * 
     * @return known format type user name
     */
    public String getPreMergeEvent()
    {
        return m_pre_merge_event;
    }

    /**
     * Set the name of the known format type
     */
    public void setName(String p_name)
    {
        m_name = p_name;
    }

    /**
     * Set the description of the known format type
     * 
     * @param p_description
     *            The description of the known format type
     */
    public void setDescription(String p_description)
    {
        m_description = p_description;
    }

    /**
     * Set the driver of the known format type
     * 
     * @param p_format_type
     *            The driver of the known format type
     */
    public void setFormatType(String p_format_type)
    {
        m_format_type = p_format_type;
    }

    /**
     * Set the pre_extract_event of the known format type
     * 
     * @param p_pre_extract_event
     *            The pre_extract_event of the known format type
     */
    public void setPreExtractEvent(String p_pre_extract_event)
    {
        m_pre_extract_event = p_pre_extract_event;
    }

    /**
     * Set the pre_merge_event of the known format type
     * 
     * @param p_pre_merge_event
     *            The pre_merge_event of the known format type
     */
    public void setPreMergeEvent(String p_pre_merge_event)
    {
        m_pre_merge_event = p_pre_merge_event;
    }

    /** Returns a string representation of the object */
    public String toString()
    {
        return m_name;
    }

    /**
     * Return a string representation of the object for debugging purposes.
     * 
     * @return a string representation of the object for debugging purposes.
     */
    public String toDebugString()
    {
        return super.toString() + " m_name="
                + (m_name == null ? "null" : m_name) + " m_description="
                + (m_description == null ? "null" : m_description)
                + " m_format_type="
                + (m_format_type == null ? "null" : m_format_type)
                + " m_pre_extract_event="
                + (m_pre_extract_event == null ? "null" : m_pre_extract_event)
                + " m_pre_merge_event="
                + (m_pre_merge_event == null ? "null" : m_pre_merge_event);
    }

    // PRIVATE MEMBERS
    private String m_name;
    private String m_description;
    private String m_format_type;
    private String m_pre_extract_event;
    private String m_pre_merge_event;

    public String getFormat_type()
    {
        return m_format_type;
    }

    public void setFormat_type(String m_format_type)
    {
        this.m_format_type = m_format_type;
    }

    public String getPre_extract_event()
    {
        return m_pre_extract_event;
    }

    public void setPre_extract_event(String m_pre_extract_event)
    {
        this.m_pre_extract_event = m_pre_extract_event;
    }

    public String getPre_merge_event()
    {
        return m_pre_merge_event;
    }

    public void setPre_merge_event(String m_pre_merge_event)
    {
        this.m_pre_merge_event = m_pre_merge_event;
    }
}
