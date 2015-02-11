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

/*
 * Copyright (c) 2002 GlobalSight Corporation. All rights reserved.
 *
 * THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF
 * GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
 * IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
 * OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
 * AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.
 *
 * THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
 * SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
 * UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
 * BY LAW.
 */
import com.globalsight.everest.persistence.PersistentObject;

/** Implements an ExportLocation */
public class ExportLocationImpl extends PersistentObject implements ExportLocation
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
        m_companyId = null;
    }

    /**
     * Constructs an ExportLocationImpl with id, name, description, and location
     * 
     * @param p_name
     * @param p_description
     * @param p_ruleText
     */
    public ExportLocationImpl(String p_name, String p_description, String p_location, String p_companyId)
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
        this (o.getName(), o.getDescription(), o.getLocation(), o.getCompanyId());
    }


    /**
     * Returns the name
     */
    public String getName()
    {return m_name;}

    /**
     * Returns the description
     */
    public String getDescription()
    {return m_description;}

    /**
     * Returns the location
     */
    public String getLocation()
    {return m_location;}

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
    public String getCompanyId() {
        return m_companyId;
    }
    /**
     * @param id The m_companyId to set.
     */
    public void setCompanyId(String id) {
        m_companyId = id;
    }

    /** Returns a string representation of the object*/
    public String toString()
    {
        return m_name;
    }

    //PRIVATE MEMBERS
    private String m_name;
    private String m_description;
    private String m_location;
    private String m_companyId;
    
}

