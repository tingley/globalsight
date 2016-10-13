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
package com.globalsight.everest.util.server;

// Core Java classes
import com.globalsight.util.GeneralException;
import com.globalsight.util.j2ee.AppServerWrapperFactory;
import com.globalsight.util.j2ee.AppServerWrapper;


/**
 * This class is used to locate an instance of the Envoy Registry of
 * server objects.
 */
public class RegistryLocator
{
    private static AppServerWrapper s_appServerWrapper = AppServerWrapperFactory.getAppServerWrapper();

    RegistryLocator()
    {
    }

    /**
     * Locate an instance of the Envoy server registry.  If no server
     * registry exists, one will be created. This returns a ServerRegistry
     * appropriate for the appserver we're running in.
     *
     * @return A reference to the Envoy server registry.
     */
    public static ServerRegistry getRegistry()
        throws GeneralException
    {
        return s_appServerWrapper.getServerRegistry();
    }
}
