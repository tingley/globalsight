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
package com.globalsight.everest.webapp;
import com.globalsight.everest.util.system.SystemConfiguration;

/**
 * Used to initialize run time values in WebAppConstants
 */
class WebAppConstantsHelper
{
    public static final String PROPERTY_NAME_HTTP_PORT = "nonSSLPort";
    public static final String PROPERTY_NAME_HTTPS_PORT = "proxy.server.port";    

    ///public static String HOST;
    public static int HTTP_PORT;
//    public static int HTTPS_PORT;

    static
    {
        try
        {
            SystemConfiguration prop = SystemConfiguration.getInstance();

            HTTP_PORT = Integer.parseInt(
                prop.getStringParameter(PROPERTY_NAME_HTTP_PORT));
//            HTTPS_PORT = Integer.parseInt(
//                prop.getStringParameter(PROPERTY_NAME_HTTPS_PORT));
        }
        catch (Exception e)
        {
            throw new IllegalStateException("Problem setting HTTP_PORT and HTTPS_PORT."
                                            + e.getMessage());
        }
    }
}

