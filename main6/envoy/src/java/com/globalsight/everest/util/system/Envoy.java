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

import org.apache.log4j.Logger;

import com.globalsight.everest.util.server.RegistryLocator;
import com.globalsight.everest.util.server.ServerRegistry;
import com.globalsight.util.GeneralException;

// Core Java classes
import javax.naming.NamingException;
import java.rmi.Remote;
import java.rmi.RemoteException;


/**
 * This is the main program of the Envoy system.  It can be deployed
 * in an application server, or run from the command line.
 *
 * @version     1.0, (8/15/00)
 * @author      Marvin Lau, mlau@globalsight.com
 */
public class Envoy
{

    private static final Logger CATEGORY =
        Logger.getLogger(
            Envoy.class.getName());

    final static String c_startup = "startup";
    final static String c_shutdown = "shutdown";

    public Envoy()
    {
    }

    /**
     * Main method that is run by the JVM.  It takes in one optional
     * parameter that specifies if the system should be started up or
     * shutdown.  If the parameter isn't specified, it assumes
     * startup.
     */
    public static void main(String[] args)
        throws SystemStartupException,
               SystemShutdownException
    {
        String command;

        if (args.length <= 0)
        {
            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("No command was specified [startup/restart/shutdown] so \"startup\" is being assumed.");
            }

            command = c_startup;
        }
        else
        {
            command = args[0];
        }

        try
        {
            Envoy system = new Envoy();
            if (command.equals(c_startup))
            {
                system.startup();
            }
            else if (command.equals(c_shutdown))
            {
                system.shutdown();
            }
            else
            {
                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("Invalid parameter specified " +
                        command + ".  Only " + c_startup + " and " +
                        c_shutdown  + " are valid.");
                }
            }
        }
        catch(GeneralException ge)
        {
            CATEGORY.error("Envoy::main", ge);
        }
    }

    /**
     * Shutdown the system.
     *
     *  @throws SystemShutdownException is thrown if the system can't
     *  be shutdown properly.
     */
    void shutdown()
        throws SystemShutdownException
    {
        // Bind the listener so that it can listen to messages
        boolean failedToFindListener = false;
        ServerRegistry registry = null;

        try
        {
            registry = RegistryLocator.getRegistry();
            EnvoySystemListener envoy = (EnvoySystemListener)registry.lookup(
                EnvoySystemListener.SERVICE_NAME);

            envoy.shutdownSystem();
        }
        catch (RemoteException re)
        {
            CATEGORY.error("Envoy::shutdown", re);
            failedToFindListener = true;
        }
        catch (NamingException ne)
        {
            CATEGORY.error("Envoy::shutdown", ne);
            failedToFindListener = true;
        }
        catch (GeneralException ge)
        {
            CATEGORY.error("Envoy::shutdown", ge);
            failedToFindListener = true;
        }
        finally
        {
            if (failedToFindListener)
            {
                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("Failed to find a system listener.");
                    CATEGORY.debug("Shutdown failed....");
                }
            }
        }
    }

    /**
     * Starts the system up.
     *
     * @throws SystemStartupException when the system cannot be
     * successfully started.
     * @throws SystemShutdownException when the system cannot be
     * started for technical reasons and thusly not be shut down. Just
     * so you know it is not running.
     */
    void startup()
        throws SystemStartupException,
               SystemShutdownException
    {
        // Instantiate the system control unit
        EnvoySystemControl controlUnit = new EnvoySystemControl();

        // Invoke the control unit's startup method
        controlUnit.startup();

        // Create a listener and associate it with the control unit
        EnvoySystemListenerLocal localListener =
            new EnvoySystemListenerLocal(controlUnit);

        // Bind the listener so that it can listen to messages
        boolean failedToBindListener = false;
        ServerRegistry registry = null;

        try
        {
            EnvoySystemListenerWLRMIImpl remoteListener =
                new EnvoySystemListenerWLRMIImpl (localListener);

            registry = RegistryLocator.getRegistry();
            registry.bind(EnvoySystemListener.SERVICE_NAME,
                (Remote)remoteListener);
        }
        catch (RemoteException re)
        {
            CATEGORY.error("Envoy::startup", re);
            failedToBindListener = true;
        }
        catch (NamingException ne)
        {
            CATEGORY.error("Envoy::startup", ne);
            failedToBindListener = true;
        }
        catch (GeneralException ge)
        {
            CATEGORY.error("Envoy::startup", ge);
            failedToBindListener = true;
        }
        finally
        {
            if (failedToBindListener)
            {
                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("Failed to bind system listener to service");
                    CATEGORY.debug("Attempting to shutdown...");
                }

                controlUnit.shutdown();
            }
        }

        if (CATEGORY.isDebugEnabled()) {
            CATEGORY.debug("Server successfully started.");
        }
    }
}
