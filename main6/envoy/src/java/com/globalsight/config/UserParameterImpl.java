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
package com.globalsight.config;

import com.globalsight.everest.persistence.PersistentObject;

/**
 * User Parameter Class Implementation
 */
public class UserParameterImpl extends PersistentObject implements
        UserParameter
{
    private static final long serialVersionUID = 4818949427043940320L;

    public static final String M_USERID = "m_userId";
    public static final String M_NAME = "m_name";
    public static final String M_VALUE = "m_value";

    //
    // PRIVATE MEMBER VARIABLES
    //
    private String m_userId;
    private String m_name;
    private String m_value;

    /**
     * Default constructor
     */
    public UserParameterImpl()
    {
        this(null, null, null);
    }

    /**
     * Constructor that supplies all attributes for user parameter object.
     * 
     * @param p_userId
     *            user id
     * @param p_name
     *            system parameter name
     * @param p_value
     *            system parameter value
     */
    public UserParameterImpl(String p_userId, String p_name, String p_value)
    {
        super();

        m_userId = p_userId;
        m_name = p_name;
        m_value = p_value;
    }

    public String getUserId()
    {
        return m_userId;
    }

    public String getName()
    {
        return m_name;
    }

    public String getValue()
    {
        return m_value;
    }

    public boolean getBooleanValue()
    {
        if (m_value.equals("0")) { return false; }

        return true;
    }

    public int getIntValue()
    {
        return Integer.parseInt(m_value);
    }

    public void setValue(String p_value)
    {
        m_value = p_value;
    }

    /**
     * Convert int to String and set system parameter value
     * 
     * @param p_intValue
     *            int system parameter value
     */
    public void setValue(int p_intValue)
    {
        m_value = String.valueOf(p_intValue);
    }

    /**
     * Convert boolean to String and set system parameter value - true is set as
     * "1" and false is set as "0"
     * 
     * @param p_booleanValue
     *            int system parameter value
     */
    public void setValue(boolean p_booleanValue)
    {
        if (p_booleanValue)
        {
            m_value = "1";
        }
        else
        {
            m_value = "0";
        }
    }

    public String toString()
    {
        return super.toString() + " m_userId="
                + (m_userId == null ? "null" : m_userId) + " m_name="
                + (m_name == null ? "null" : m_name) + " m_value="
                + (m_value == null ? "null" : m_value);
    }

    public void setName(String m_name)
    {
        this.m_name = m_name;
    }

    public void setUserId(String id)
    {
        m_userId = id;
    }
}
