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
import java.net.MalformedURLException;
import javax.naming.NamingException;

import java.rmi.Remote;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

// Envoy classes
import com.globalsight.util.GeneralException;

/**
 * This interface defines the methods of a server registry that holds
 * server objects.  Server objects can be inserted into the registry.
 * Clients can use the registry to lookup the server objects.
 *
 * @version     1.0, (3/17/00 1:36:00 AM)
 * @author      Marvin Lau, mlau@globalsight.com
 */
public interface ServerRegistry
{
    /**
     * Bind the specified server object under the specified name.  If
     * the specified name is already bound, the new object will be
     * rebinded under the given name, overwriting the previous
     * binding.
     *
     * @param p_name The name to register the server under.
     * @param p_server The server object to register.
     */
    void bind(String p_name, Remote p_server)
        throws GeneralException, NamingException;

    /**
     * Lookup a server object by name.
     *
     * @return The server object.
     * @param p_name Name of the server object to lookup.
     */
    Object lookup(String p_name)
        throws GeneralException, NamingException;

    /**
     * Lookup a server object by name at the specified host.
     *
     * @return The server object.
     * @param p_name Name of the server object to lookup.
     * @param p_host Name of the host where the server object is.
     */
    //Object lookup(String p_name, String p_host)
    // throws GeneralException, NamingException;

    /**
     * Unbind the specified server object under the specified name.
     *
     * @param p_name The name of the server to unbind.
     */
    void unbind(String p_name)
        throws GeneralException, NamingException;
}
