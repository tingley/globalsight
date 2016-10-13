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
package com.globalsight.cxe.entity.exportlocation;

import com.globalsight.everest.persistence.PersistentObject;

/** Implements an ExportLocation */
public class ExportLocationImpl extends PersistentObject implements
        ExportLocation
{
    private static final long serialVersionUID = -3036562705273046249L;

    /**
     * Default constructor for TOPLink
     */
    public ExportLocationImpl()
    {
        m_name = null;
        m_description = null;
        m_location = null;
        m_companyId = -1;
    }

    /**
     * Constructs an ExportLocationImpl with id, name, description, and location
     * 
     * @param p_name
     * @param p_description
     * @param p_ruleText
     */
    public ExportLocationImpl(String p_name, String p_description,
            String p_location, String p_companyId)
    {
        m_name = p_name;
        m_description = p_description;
        m_location = p_location;
        if (p_companyId != null)
        {
            m_companyId = Long.parseLong(p_companyId);
        }
    }

    public ExportLocationImpl(String p_name, String p_description,
            String p_location, long p_companyId)
    {
        m_name = p_name;
        m_description = p_description;
        m_location = p_location;
        m_companyId = p_companyId;
    }

    /**
     * Constructs an ExportLocationImpl from an ExportLocation
     * 
     * @param o
     */
    public ExportLocationImpl(ExportLocation o)
    {
        this(o.getName(), o.getDescription(), o.getLocation(), o.getCompanyId());
    }

    /**
     * Returns the name
     */
    public String getName()
    {
        return m_name;
    }

    /**
     * Returns the description
     */
    public String getDescription()
    {
        return m_description;
    }

    /**
     * Returns the location
     */
    public String getLocation()
    {
        return m_location;
    }

    /**
     ** Sets the name
     **/
    public void setName(String p_name)
    {
        m_name = p_name;
    }

    /**
     ** Sets the description
     **/
    public void setDescription(String p_description)
    {
        m_description = p_description;
    }

    /**
     ** Sets the location (directory)
     **/
    public void setLocation(String p_location)
    {
        m_location = p_location;
    }

    /**
     * @return Returns the m_companyId.
     */
    public long getCompanyId()
    {
        return m_companyId;
    }

    /**
     * @param id
     *            The m_companyId to set.
     */
    public void setCompanyId(long id)
    {
        m_companyId = id;
    }

    /** Returns a string representation of the object */
    public String toString()
    {
        return m_name;
    }

    // PRIVATE MEMBERS
    private String m_name;
    private String m_description;
    private String m_location;
    private long m_companyId;

}
