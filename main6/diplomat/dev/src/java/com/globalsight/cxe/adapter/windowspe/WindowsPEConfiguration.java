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

package com.globalsight.cxe.adapter.windowspe;

// JDK
import java.util.Properties;

import org.apache.log4j.Logger;

import com.globalsight.cxe.adapter.msoffice.MsOfficeConfiguration;
import com.globalsight.everest.util.system.DynamicPropertiesSystemConfiguration;
import com.globalsight.everest.util.system.SystemConfiguration;

/**
 * The MsOfficeConfiguration is a singleton object used for providing
 * the properties of the MS Office Adapter.
 */

public class WindowsPEConfiguration
{
    static private final Logger CATEGORY =
        Logger.getLogger(WindowsPEConfiguration.class);

    static public final String MAX_TIME_TO_WAIT_IMPORT = "maxTimeToWaitForImport";
    static public final String MAX_TIME_TO_WAIT_EXPORT = "maxTimeToWaitForExport";
    static private final String PROPERTY_FILE =
        "/properties/WindowsPEAdapter.properties";

    static private WindowsPEConfiguration m_instance =
        new WindowsPEConfiguration();

    /**
     * Private Constructor.
     */
    private WindowsPEConfiguration()
    {
    }

    /**
     * Get the MsOfficeConfiguration object.
     * @return The MsOfficeConfiguration object.
     */
    static public WindowsPEConfiguration getInstance()
    {
        return m_instance;
    }


    /**
     * Get the MS Office Adapter's properties.
     * @return The Properties object based on the MS Office Adapter's
     * property file.
     */
    public synchronized Properties loadProperties()
    {
    	try
		{
			Properties props = ((DynamicPropertiesSystemConfiguration) SystemConfiguration
					.getInstance(PROPERTY_FILE)).getProperties();
			return props;
        }
        catch (Exception e)
        {
        	CATEGORY.error( "Problem reading properties from " + PROPERTY_FILE +
                                     ". Using default values.", e);
        	Properties p = new Properties();
        	p.setProperty(MAX_TIME_TO_WAIT_IMPORT, "720");
        	p.setProperty(MAX_TIME_TO_WAIT_EXPORT, "60");
        	return p;
        }
    }
}

