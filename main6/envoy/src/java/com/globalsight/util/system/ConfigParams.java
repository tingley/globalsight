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

import com.globalsight.util.GeneralException;

/**
 * This abstract class models a collection of configuration
 * parameters.  It supports a set of methods to read the parameters.
 * It has a factory method that returns an instance of the required
 * concrete subclass.
 *
 * @version     1.0, (7/11/00 6:32:27 PM)
 * @author      Marvin Lau, mlau@globalsight.com
 */

public abstract class ConfigParams
{
    /**
     * Specifies a ConfigParams type that is implemented with Properties.
     */
    public static final int TYPE_PROPERTIES = 1;

    public ConfigParams()
    {
        super();
    }

    /**
     * Get an instance of a concrete subclass of this class.
     *
     * @return com.globalsight.util.system.ConfigParams
     * @param p_type Type of the subclass.
     * @param p_resourceName Name of the resource that holds the
     * parameters.
     * @exception com.globalsight.util.system.ConfigException
     * Configuration resource exception.
     * @exception com.globalsight.util.GeneralException General
     * exception.
     */
    public static ConfigParams getInstance(int p_type, String p_resourceName)
        throws ConfigException, GeneralException
    {
        ConfigParams params = null;

        switch (p_type)
        {
        case TYPE_PROPERTIES :
            params = new PropertiesConfigParams(p_resourceName);
            break;
        default :
            throw new ConfigException(ConfigException.EX_INVALIDTYPE);
        }

        return params;
    }

    /**
     * Get the specified parameter and return it as an int.
     *
     * @param p_paramName Name of the parameter to get value for.
     * @return Value of the specified parameter.
     * @exception com.globalsight.util.system.ConfigException
     */
    public abstract int getIntParameter(String p_paramName)
        throws ConfigException;

    /**
     * Get the specified parameter and return it as a String.
     *
     * @param p_paramName Name of the parameter to get value for.
     * @return Value of the specified parameter.
     * @exception com.globalsight.util.system.ConfigException
     */
    public abstract String getStringParameter(String p_paramName)
        throws ConfigException;
}
