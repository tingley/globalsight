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

import java.util.Enumeration;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.globalsight.everest.webapp.pagehandler.administration.reports.ReportDBUtil;
import com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper;
import com.globalsight.util.system.ConfigException;

/**
 * This is an abstract implementation of the SystemControl interface. This class
 * provides default implementations of the startup and shutdown methods. The
 * startup method employs the template method pattern, which uses the abstract
 * method getServerClasses method to get a list of server object classes that
 * needs to be started and initialized.
 */
public abstract class SystemControlTemplate implements SystemControl
{
    private static final Logger CATEGORY = Logger
            .getLogger(SystemControlTemplate.class.getName());

    private static Boolean s_areAllServerClassesLoaded = Boolean.FALSE;

    static final int SYSTEM_NOTSTARTED = 0;
    static final int SYSTEM_STARTING = 1;
    static final int SYSTEM_STARTED = 2;
    static final int SYSTEM_STOPPING = 3;
    static final int SYSTEM_STOPPED = 4;

    static final int COMMAND_STARTUP = 1;
    static final int COMMAND_SHUTDOWN = 2;

    Vector m_serverInstances; // All the started instances
    int m_systemState;

    /**
     * Default constructor.
     */
    protected SystemControlTemplate()
    {
        m_serverInstances = null;
    }

    //
    // Begin methods for the SystemControl interface.
    //

    /**
     * Returns true if all the RMI server classes have been loaded.
     */
    public static boolean areAllServerClassesLoaded()
    {
        return s_areAllServerClassesLoaded.booleanValue();
    }

    /**
     * This method is called to start the system that is controlled by this
     * object.
     */
    public void startup() throws SystemStartupException
    {
        try
        {
            processCommand(COMMAND_STARTUP);
            synchronized (s_areAllServerClassesLoaded)
            {
                s_areAllServerClassesLoaded = Boolean.TRUE;
            }
        }
        catch (Exception e)
        {
            CATEGORY.error(e.getMessage(), e);

            throw new SystemStartupException(
                    SystemStartupException.EX_SERVERCLASSNAMES, e);
        }
    }

    /**
     * This method is called to shutdown the system that is controlled by this
     * object.
     */
    public void shutdown() throws SystemShutdownException
    {
        try
        {
            synchronized (s_areAllServerClassesLoaded)
            {
                s_areAllServerClassesLoaded = Boolean.FALSE;
            }
            processCommand(COMMAND_SHUTDOWN);
        }
        catch (Exception e)
        {
            // This should not happen, so just log it and continue
            // ** loggin code not implemented **
            CATEGORY.error("SystemControlTemplate::shutdown", e);

            throw new SystemShutdownException(
                    SystemStartupException.EX_SERVERCLASSNAMES, e);
        }
    }

    //
    // End methods for the SystemControl interface.
    //

    //
    // Beginn other methods
    //

    /**
     * Get a list of the names of the server object classes. This method is to
     * be implemented by the subclasses.
     */
    protected abstract String[] getServerClasses() throws ConfigException;

    /**
     * Get a list of the created server objects.
     * 
     * @return A list of the created server objects.
     */
    protected Enumeration getServerInstances()
    {
        Enumeration servers = null;

        if (m_serverInstances != null)
        {
            servers = m_serverInstances.elements();
        }

        return servers;
    }

    /**
     * Process system startup, shutdown and other commands from interface
     * methods. All these command are processed here so that only one command is
     * being processed at a time.
     */
    synchronized void processCommand(int command)
            throws SystemStartupException, SystemShutdownException
    {
        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("processCommand " + Integer.toString(command));
        }

        switch (command)
        {
            case COMMAND_STARTUP:
                if (m_systemState == SYSTEM_NOTSTARTED)
                {
                    m_systemState = SYSTEM_STARTING;
                    m_serverInstances = new Vector();

                    String[] serverClasses = null;
                    // Instantiate the server object instances
                    try
                    {
                        serverClasses = getServerClasses();
                    }
                    catch (Exception e)
                    {
                        CATEGORY.error("getServerClasses", e);
                        throw new SystemStartupException(
                                SystemStartupException.EX_SERVERCLASSNAMES, e);
                    }

                    int cnt = serverClasses.length;
                    boolean cleanupAfterException = false;
                    Class server = null;
                    ServerObject serverInstance;

                    try
                    {
                        for (int i = 0; i < cnt; i++)
                        {
                            // Instantiate the server class
                            server = Class.forName(serverClasses[i]);
                            serverInstance = (ServerObject) server
                                    .newInstance();
                            serverInstance.init();
                            m_serverInstances.add(serverInstance);
                        }

                        // Clean "REPORTS_DATA" table for unexpected restart. 
                        ReportDBUtil.delAllReportsData();
                        // set isUploading = 'N' for unexpected restart.
                        TaskHelper.installTaskIsUploading();
                    }
                    catch (ClassNotFoundException cnfe)
                    {
                        cleanupAfterException = true;

                        CATEGORY.error(
                                "server="
                                        + (server != null ? server.toString()
                                                : "null"), cnfe);

                        throw new SystemStartupException(
                                SystemStartupException.EX_FAILEDTOCREATESERVER,
                                cnfe);
                    }
                    catch (IllegalAccessException iae)
                    {
                        cleanupAfterException = true;
                        CATEGORY.error(
                                "server="
                                        + (server != null ? server.toString()
                                                : "null"), iae);

                        throw new SystemStartupException(
                                SystemStartupException.EX_FAILEDTOCREATESERVER,
                                iae);
                    }
                    catch (InstantiationException ie)
                    {
                        cleanupAfterException = true;
                        CATEGORY.error(
                                "server="
                                        + (server != null ? server.toString()
                                                : "null"), ie);

                        throw new SystemStartupException(
                                SystemStartupException.EX_FAILEDTOCREATESERVER,
                                ie);
                    }
                    catch (Exception t)
                    {
                        cleanupAfterException = true;
                        CATEGORY.error(
                                "server="
                                        + (server != null ? server.toString()
                                                : "null"), t);

                        throw new SystemStartupException(
                                SystemStartupException.EX_FAILEDTOCREATESERVER,
                                t);
                    }
                    finally
                    {
                        if (cleanupAfterException)
                        {
                            destroyStartedServerObjects();
                            m_systemState = SYSTEM_STOPPED;
                        }
                    }

                    m_systemState = SYSTEM_STARTED;
                }
                break;

            case COMMAND_SHUTDOWN:
                if (m_systemState == SYSTEM_STARTED)
                {
                    destroyStartedServerObjects();
                    m_systemState = SYSTEM_STOPPED;
                }
                break;

            default:
                // Just ignore invalid commands.
                break;
        }
    }

    /**
     * Destroy all started server objects.
     */
    synchronized void destroyStartedServerObjects()
    {
        // Destroy the started classes
        Enumeration servers = m_serverInstances.elements();
        boolean allServersDestroyed;

        if (servers != null)
        {
            allServersDestroyed = true;

            while (servers.hasMoreElements())
            {
                try
                {
                    ((ServerObject) servers.nextElement()).destroy();
                }
                catch (SystemShutdownException sue)
                {
                    // log the exception and continue
                    // ** logging code is not implemented yet **
                    CATEGORY.error("SystemControlTemplate::"
                            + "destroyStartedServerObjects", sue);

                    allServersDestroyed = false;
                }
            }

            if (allServersDestroyed)
            {
                m_serverInstances.removeAllElements();
            }
        }
    }
}
