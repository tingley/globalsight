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

package com.globalsight.cxe.adapter.idml;

import java.util.Properties;

import org.apache.log4j.Logger;


/**
 * The AdobeConfiguration is a singleton object used for providing
 * the properties of the Adobe Adapter.
 */

public class IdmlConfiguration
{
    static private final Logger CATEGORY =
        Logger.getLogger(
                IdmlConfiguration.class);
    
    static public final String MAX_TIME_TO_WAIT = "maxTimeToWait";
    static public final long TIMES = 1000L;
    static public final long MINUTE = TIMES * 60;
    static public final long DEFAULT_MAX_WAIT_TIME = MINUTE * 60;
    static public final long SLEEP_TIME = TIMES * 2;

    static private final String[] PROPERTY_FILES = 
    	{"/properties/Logger.properties", 
        "/properties/IdmlAdapter.properties"};

    static private IdmlConfiguration m_instance = new IdmlConfiguration();

    private Properties m_properties = null;

    /**
     * Gets the IdmlConfiguration object.
     * 
     * @return The IdmlConfiguration object.
     */
    static public IdmlConfiguration getInstance()
    {
        return m_instance;
    }

    /**
     * Gets the Logger properties.
     * 
     * @return The Properties object the Logger property file.
     */
    public Properties loadProperties()
    {
        try
		{
            m_properties = new Properties();
            m_properties.load(IdmlConfiguration.class
					.getResourceAsStream(PROPERTY_FILES[0]));
		}
		catch (Throwable ex)
		{
			CATEGORY.error(
					"Failed to load Logger properties.", ex);
		}
        
        return m_properties;
    }
}

