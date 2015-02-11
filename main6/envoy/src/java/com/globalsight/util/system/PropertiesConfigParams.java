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
package com.globalsight.util.system;

import org.apache.log4j.Logger;

import com.globalsight.util.GeneralException;
import com.globalsight.util.PropertiesFactory;

// Core Java classes
import java.io.IOException;
import java.util.Properties;

/**
 * This class implements the ConfigParams superclass using
 * Java properties files.
 *
 * @version     1.0, (7/12/00 11:48:28 AM)
 * @author      Marvin Lau, mlau@globalsight.com
 */
class PropertiesConfigParams
    extends ConfigParams
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            PropertiesConfigParams.class.getName());

    Properties m_paramStore; // Where parameters are stored.
    private String m_propertiesFile;

    /**
     * Default PropertiesConfigParams constructor.  This is not public
     * because clients must use the getInstance() method in the base
     * class to get an instance of this class.
     *
     * @param p_propertyFile Name of the properties file to use.
     */
    PropertiesConfigParams(String p_propertiesFile)
        throws GeneralException
    {
        super();

        m_propertiesFile = p_propertiesFile;

        try
        {
            m_paramStore = (new PropertiesFactory()).getProperties(
                p_propertiesFile);
        }
        catch (GeneralException ge)
        {
            CATEGORY.error(p_propertiesFile, ge);
            throw (ge);
        }
    }

    /**
     * Get the specified parameter and return it as an int.
     *
     * @param p_paramName Name of the parameter to get value for.
     * @return Value of the specified parameter.
     * @exception com.globalsight.util.system.ConfigException
     */
    public int getIntParameter(String p_paramName)
        throws ConfigException
    {
        int value;
        String strParam = getStringParameter(p_paramName);

        try
        {
            value = Integer.parseInt(strParam);
        }
        catch (NumberFormatException nfe)
        {
            throw new ConfigException(ConfigException.EX_BADPARAM, nfe);
        }
        return value;
    }

    /**
     * Get the specified parameter and return it as a String.
     *
     * @param p_paramName Name of the parameter to get value for.
     * @return Value of the specified parameter.
     * @exception com.globalsight.util.system.ConfigException
     */
    public String getStringParameter(String p_paramName)
        throws ConfigException
    {
        String param = m_paramStore.getProperty(p_paramName);

        if (param == null)
        {
            throw new ConfigException(ConfigException.EX_PARAMNOTFOUND);
        }

        return param;
    }

    /**
     * Return a string representation of the object.
     * @return a string representation of the object.
     */
    public String toString()
    {
        return getClass().getName()
            + " "
            + "m_propertiesFile=" + m_propertiesFile
            + " "
            + m_paramStore.toString()
            ;
    }
}
