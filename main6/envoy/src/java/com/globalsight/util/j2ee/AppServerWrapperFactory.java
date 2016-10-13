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

package com.globalsight.util.j2ee;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.util.j2ee.jboss.JBossWrapper;

public class AppServerWrapperFactory
{
    //app server types
    public static final String JBOSS = "jboss";

    /**
     * the appserver wrapper
     */
    private static AppServerWrapper s_wrapper;
    static
    {
        try
        {
            SystemConfiguration config = SystemConfiguration.getInstance();
            String vendor = config.getStringParameter
                            (SystemConfigParamNames.APPSERVER_VENDOR);
            vendor = JBOSS;
            if (JBOSS.equals(vendor))
            {
                s_wrapper = new JBossWrapper();
            }
            else
            {
                throw new UnsupportedOperationException
                ("J2EE Appserver vendor '" + vendor + "' not supported.");
            }
        }
        catch (UnsupportedOperationException uoe)
        {
            throw uoe;
        }
        catch (Exception ce)
        {
            throw new UnsupportedOperationException
            ("Received exception when looking for property " +
             SystemConfigParamNames.APPSERVER_VENDOR + ": " + ce.getMessage());
        }
    }

    /**
     * Gets the J2EE Application Server wrapper
     * 
     * @return AppServerWrapper
     */
    public static AppServerWrapper getAppServerWrapper()
    {
        return s_wrapper;
    }
}

