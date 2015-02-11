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
package com.globalsight.diplomat.util.database;

/**
 * Represents a connection profile, containing specific information about
 * how to connect to a particular database using a particular driver, connect
 * string, user name, and password.
 */
public class ConnectionProfile
{
    // 
    // PRIVATE MEMBER VARIABLES
    //
    private long m_id;
    private String m_name;
    private String m_driver;
    private String m_connStr;
    private String m_userName;
    private String m_pw;

    //
    // PUBLIC CONSTRUCTOR
    //
    public ConnectionProfile ()
    {
        m_id = 0;
        m_name = "";
        m_driver = "";
        m_connStr = "";
        m_userName = "";
        m_pw = "";
    }

    //
    // PUBLIC ACCESSORS
    //
    /**
     * Return the current value of the id field.
     *
     * @return the current value.
     */
    public long getId()
    {
        return m_id;
    }

    /**
     * Set the value of the id field.
     *
     * @param the new value to use.
     */
    public void setId (long p_id)
    {
        m_id = p_id;
    }

    /**
     * Return the current value of the profile name field.
     *
     * @return the current value.
     */
    public String getProfileName()
    {
        return m_name;
    }

    /**
     * Set the value of the profile name field.
     *
     * @param p_str the new value to use.
     */
    public void setProfileName (String p_str)
    {
        m_name = p_str;
    }

    /**
     * Return the current value of the driver field.
     *
     * @return the current value.
     */
    public String getDriver()
    {
        return m_driver;
    }

    /**
     * Set the value of the driver field.
     *
     * @param p_str the new value to use.
     */
    public void setDriver (String p_str)
    {
        m_driver = p_str;
    }

    /**
     * Return the current value of the connection string field.
     *
     * @return the current value.
     */
    public String getConnectionString()
    {
        return m_connStr;
    }

    /**
     * Set the value of the connection string field.
     *
     * @param p_str the new value to use.
     */
    public void setConnectionString (String p_str)
    {
        m_connStr = p_str;
    }

    /**
     * Return the current value of the user name field.
     *
     * @return the current value.
     */
    public String getUserName()
    {
        return m_userName;
    }

    /**
     * Set the value of the user name field.
     *
     * @param p_str the new value to use.
     */
    public void setUserName(String p_str)
    {
        m_userName = p_str;
    }

    /**
     * Return the current value of the password field.
     *
     * @return the current value.
     */
    public String getPassword()
    {
        return m_pw;
    }

    /**
     * Set the value of the password field.
     *
     * @param the new value to use.
     */
    public void setPassword (String p_str)
    {
        m_pw = p_str;
    }

    //
    // PUBLIC METHODS
    //
    /**
     * Return a string representation of the connection profile.
     *
     * @return a description of the receiver.
     */
    public String toString()
    {
        return ("ConnectionProfile (id=" + getId() + 
            ", name=" + getProfileName() + ")");
    }
}
