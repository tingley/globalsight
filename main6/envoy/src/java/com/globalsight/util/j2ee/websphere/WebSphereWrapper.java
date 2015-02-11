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

package com.globalsight.util.j2ee.websphere;
import org.apache.log4j.Logger;
import com.globalsight.util.j2ee.AppServerWrapper;
import com.globalsight.util.j2ee.AppServerWrapperFactory;
import javax.naming.NamingException;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import java.io.File;
import com.globalsight.util.ProcessRunner;
import com.globalsight.everest.util.server.ServerRegistry;
import com.globalsight.util.GeneralException;

/**
 * Implements the wrapper for WebSphere
 */
public class WebSphereWrapper extends AppServerWrapper
{
    //logging
    private static final Logger s_logger =
        Logger.getLogger(
            WebSphereWrapper.class.getName());

    public static final String USER_TRANSACTION = "jta/usertransaction";
    
    /**
     * Constructor
     */
    public WebSphereWrapper()
    {
        super();
        System.out.println("Using IBM WebSphere as the J2EE Application Server");
    }

    /**
     * Returns the name of the J2EE application server.
     * 
     * @return String
     */
    public String getJ2EEServerName()
    {
        return AppServerWrapperFactory.WEBSPHERE;
    }

    /**
     * Get the JNDI lookup string for getting a UserTransaction object.
     * @return The WebLogic application server string for the lookup.
     */
    public String getUserTransactionString()
    {
        return USER_TRANSACTION;
    }

    /**
     * This will shut down weblogic. It will check System properties
     * to see whether Ambassador was deployed as a service or from
     * the command line.
     */
    public void shutdown()
    {
        throw new IllegalStateException("WebSphereWrapper.shutdown() not implemented yet.");
    }

    /**
    * Returns a ServerRegistry object appropriate for the appserver.
    * NOTE: The default is to return the one we used to use for WebLogic    
    * (ServerRegistryImpl)
    */
    public ServerRegistry getServerRegistry()
    throws GeneralException
    {
        return WebSphereServerRegistry.getInstance();
    }
}

