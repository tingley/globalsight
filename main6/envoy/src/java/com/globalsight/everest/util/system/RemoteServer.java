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

import java.rmi.Remote;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.apache.log4j.Logger;

import com.globalsight.everest.util.server.RegistryLocator;

/**
 * This is a base class for remote server classes. Remote server objects are
 * ones that must be registered in the ServerRegistry (@see
 * com.globalsight.everest.util.server.ServerRegistry), started at system
 * startup, and destroyed at system shutdown (@see
 * com.globalsight.everest.util.system.ServerObject). Derived classes must set
 * m_serviceName in their constructors.
 */
public abstract class RemoteServer
/* extends UnicastRemoteObject */
implements ServerObject
{
    private static final Logger CATEGORY = Logger.getLogger(RemoteServer.class);

    // Name to bind in the ServerRegistry
    protected String m_serviceName = null;

    /**
     * Constructor - pass in the service name to use for binding
     */
    protected RemoteServer(String p_serviceName) throws RemoteException
    {
        super();

        m_serviceName = p_serviceName;
    }

    protected RemoteServer() throws RemoteException
    {
        super();

        m_serviceName = getServiceName(getClass());
    }

    /**
     * Determine an appropriate JNDI service name from a class.
     * 
     * @param p_class
     *            Class to determine an appropriate JNDI service name from.
     * @return an appropriate JNDI service name. Objects that are not bound in
     *         JDNI with the service name returned from this method will not be
     *         found.
     */
    public static String getServiceName(Class p_class)
    {
        String name = p_class.getName();
        return name.substring(name.lastIndexOf('.') + 1);
    }

    public String getServiceName()
    {
        return m_serviceName;
    }

    /**
     * Bind the remote server to the ServerRegistry.
     * 
     * @throws SystemStartupException
     *             when a NamingException or other Exception occurs.
     */
    public void init() throws SystemStartupException
    {
        try
        {
            RegistryLocator.getRegistry().bind(m_serviceName, (Remote) this);

            CATEGORY.info(m_serviceName
                    + " start up successful and bound to registry.");
        }
        catch (NamingException ne)
        {
            CATEGORY.error("Error binding " + m_serviceName
                    + " to the registry", ne);

            throw new SystemStartupException(
                    SystemStartupException.EX_SERVERCLASSNAMES, ne);
        }
        catch (Exception e)
        {
            CATEGORY.error("Error binding " + m_serviceName
                    + " to the registry", e);

            throw new SystemStartupException(
                    SystemStartupException.EX_FAILEDTOINITSERVER, e);
        }
    }

    /**
     * Unbind the remote server from the ServerRegistry.
     * 
     * @throws SystemShutdownException
     *             when a NamingException or other Exception occurs.
     */
    public void destroy() throws SystemShutdownException
    {
        try
        {
            RegistryLocator.getRegistry().unbind(m_serviceName);

            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug(m_serviceName + " unbind from registry.");
            }
        }
        catch (NamingException ne)
        {
            CATEGORY.error("unbind error", ne);
            throw new SystemShutdownException(
                    SystemStartupException.EX_SERVERCLASSNAMES, ne);
        }
        catch (Exception e)
        {
            CATEGORY.error("unbind error", e);
            throw new SystemShutdownException(
                    SystemStartupException.EX_SERVERCLASSNAMES, e);
        }
    }
}
