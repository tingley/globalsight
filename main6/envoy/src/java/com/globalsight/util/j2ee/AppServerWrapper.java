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

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.InitialContext;
import com.globalsight.everest.util.server.ServerRegistry;
import com.globalsight.everest.util.server.ServerRegistryImpl;
import com.globalsight.util.GeneralException;

/**
 * This class exists to wrap functionality that does
 * not exist in standard J2EE but may be offered by
 * application server specific code.
 * 
 * This class should be extened by implementation
 * specific classes that are free to use vendor
 * specific APIS
 */
public abstract class AppServerWrapper
{

    /**
     * Naming context
     */
    private Context m_context = null;

    /**
     * constructor for children
     */
    protected AppServerWrapper()
    {
        try {
            m_context = new InitialContext();
        }
        catch (NamingException ne)
        {
            throw new IllegalStateException("Could not load initial naming context: " + ne.getMessage());
        }
    }

    /**
     * Returns the name of the J2EE application server.
     * 
     * @return String
     */
    public abstract String getJ2EEServerName();

    /**
     * Get the JNDI lookup string for getting a UserTransaction object.
     * @return The application server dependent string for the lookup.
     */
    public abstract String getUserTransactionString();

    /**
     * Gets the AppServer Naming Context
     * 
     * @return Context
     */
    public Context getNamingContext()
    {
        return m_context;
    }

    /**
     * Sets the app server's naming context
     * 
     * @param p_context naming context
     */
    protected void setNamingContext(Context p_context)
    {
        m_context = p_context;
    }

    /**
     * Either undeploys Ambassador or shuts down the container.
     */
    public void shutdown()
    {
        throw new IllegalStateException("Not implemented.");
    }

    /**
    * Returns a ServerRegistry object appropriate for the appserver.
    * NOTE: The default is to return the one we used to use for WebLogic    
    * (ServerRegistryImpl)
    */
    public ServerRegistry getServerRegistry()
    throws GeneralException
    {
        return ServerRegistryImpl.getInstance();
    }
}

