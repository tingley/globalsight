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
package com.globalsight.cxe.entity.dbconnection;

/*
 * Copyright (c) 2001 GlobalSight Corporation. All rights reserved.
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

/**
 * Represents a CXE DB Connection entity object.
 */
public class DBConnectionImpl extends PersistentObject implements DBConnection
{
    private static final long serialVersionUID = 8935213023572846318L;
    public boolean useActive = false;

    // CONSTRUCTORS
    /** Default constructor for TOPLink */
    public DBConnectionImpl()
    {
        m_name = null;
        m_description = null;
        m_driver = null;
        m_connection = null;
        m_username = null;
        m_password = null;
    }

    /***************************************************************************
     * Constructs an DBConnectionImpl from a DBConnection (no deep copy)
     * 
     * @param o
     *            Another DBConnection object *
     **************************************************************************/
    public DBConnectionImpl(DBConnection o)
    {
        m_name = o.getName();
        m_description = o.getDescription();
        m_driver = o.getDriver();
        m_connection = o.getConnection();
        m_username = o.getUserName();
        m_password = o.getPassword();
    }

    // PUBLIC METHODS

    /**
     * Return the name of the db connection
     * 
     * @return db connection name
     */
    public String getName()
    {
        return m_name;
    }

    /**
     * Return the description of the db connection
     * 
     * @return db connection description
     */
    public String getDescription()
    {
        return m_description;
    }

    /**
     * Return the driver of the db connection
     * 
     * @return db connection driver
     */
    public String getDriver()
    {
        return m_driver;
    }

    /**
     * Return the connection string of the db connection
     * 
     * @return db connection string
     */
    public String getConnection()
    {
        return m_connection;
    }

    /**
     * Return the user name of the db connection
     * 
     * @return db connection user name
     */
    public String getUserName()
    {
        return m_username;
    }

    /**
     * Return the password of the db connection
     * 
     * @return db connection password
     */
    public String getPassword()
    {
        return m_password;
    }

    /**
     * Set the name of the db connection
     */
    public void setName(String p_name)
    {
        m_name = p_name;
    }

    /**
     * Set the description of the db connection
     * 
     * @param p_description
     *            The description of the db connection
     */
    public void setDescription(String p_description)
    {
        m_description = p_description;
    }

    /**
     * Set the driver of the db connection
     * 
     * @param p_driver
     *            The driver of the db connection
     */
    public void setDriver(String p_driver)
    {
        m_driver = p_driver;
    }

    /**
     * Set the connection string of the db connection
     * 
     * @param p_connection
     *            The connection string of the db connection
     */
    public void setConnection(String p_connection)
    {
        m_connection = p_connection;
    }

    /**
     * Set the user name of the db connection
     * 
     * @param p_username
     *            The user name of the db connection
     */
    public void setUserName(String p_username)
    {
        m_username = p_username;
    }

    /**
     * Set the password of the db connection
     * 
     * @param p_password
     *            The password of the db connection
     */
    public void setPassword(String p_password)
    {
        m_password = p_password;
    }

    /** Returns a string representation of the object */
    public String toString()
    {
        return m_name;
    }

    // PRIVATE MEMBERS
    private String m_name;
    private String m_description;
    private String m_driver;
    private String m_connection;
    private String m_username;
    private String m_password;

    public String getUsername()
    {
        return m_username;
    }

    public void setUsername(String m_username)
    {
        this.m_username = m_username;
    }
}
