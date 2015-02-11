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

package com.globalsight.everest.util.system;

import java.util.Properties;

import com.globalsight.util.system.ConfigException;

/**
 * A SystemConfiguration object that holds information from property files only.
 * It will also check the last modified time of the property file and if it has
 * changed, re-read the set of properties from that file.
 */
public class DynamicPropertiesSystemConfiguration extends SystemConfiguration
{
    private Properties m_properties = new Properties();

    private String m_propertyFileName = null;

    DynamicPropertiesSystemConfiguration(String p_propertyFileName)
            throws ConfigException
    {
        super();
        m_propertyFileName = p_propertyFileName;
        loadProperties();
    }

    /**
     * Constructor for unit test only!
     */
    DynamicPropertiesSystemConfiguration()
    {
    }

    /**
     * Get the specified parameter and return it as a String.
     * 
     * @param p_paramName
     *            Name of the parameter to get value for.
     * @return Value of the specified parameter.
     * @exception com.globalsight.util.system.ConfigException
     */
    public String getStringParameter(String p_paramName) throws ConfigException
    {
        return (String) m_properties.get(p_paramName);
    }

    /**
     * For parameters from file system, no parameter has company id.
     */
    public String getStringParameter(String p_paramName, String p_companyId)
            throws ConfigException
    {
        return null;
    }

    /**
     * Loads the properties from the named file
     * 
     * @exception ConfigException
     */
    private synchronized void loadProperties() throws ConfigException
    {
        try
        {
            m_properties.load(getClass()
                    .getResourceAsStream(m_propertyFileName));
        }
        catch (Exception e)
        {
            throw new ConfigException(ConfigException.EX_PROPERTIES, e);
        }
    }

    /**
     * Return a string representation of the object.
     * 
     * @return a string representation of the object.
     */
    public String toString()
    {
        return getClass().getName() + " " + m_properties.toString();
    }

    /**
     * @return the properties in file For issue "Properties files separation by
     *         Company"
     * @throws ConfigException
     */
    public Properties getProperties() throws ConfigException
    {
        return m_properties;
    }
}
