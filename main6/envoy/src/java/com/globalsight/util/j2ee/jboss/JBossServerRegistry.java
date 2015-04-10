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

package com.globalsight.util.j2ee.jboss;

import java.rmi.Remote;
import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import com.globalsight.everest.util.server.ServerRegistry;
import com.globalsight.util.GeneralException;
import com.globalsight.util.j2ee.AppServerWrapper;
import com.globalsight.util.j2ee.AppServerWrapperFactory;

/**
 * This is an implementation of the ServerRegistry. It helps clients register
 * and lookup servers/services when using Jboss. Note: This does not really
 * support RMI, it simply caches local objects. The RMI interfaces will not work
 * in Jboss right now.
 */
public class JBossServerRegistry implements ServerRegistry
{
	// One and only instance of this registry
	private static ServerRegistry s_serverRegistry = null;

	private static AppServerWrapper s_appServerWrapper = AppServerWrapperFactory
			.getAppServerWrapper();

	private static final Logger s_logger = Logger
			.getLogger(JBossServerRegistry.class);

	private Map m_registry = new HashMap();

	/**
     * Construct a WebSphereServerRegistry
     */
	JBossServerRegistry() throws GeneralException
	{
		super();
		if(s_logger.isDebugEnabled()){			
			s_logger.debug("Creating new JBoss registry.");
		}
	}

	/**
     * Get the reference to an instance of this class. This will be used as the
     * Envoy server registry when using Jboss.
     * 
     * @return The reference to an instance of this class.
     * @exception GeneralException -
     *                General exception thrown by the system.
     */
	static ServerRegistry getInstance() throws GeneralException
	{
		if (s_serverRegistry == null)
		{
			s_serverRegistry = new JBossServerRegistry();
		}

		return s_serverRegistry;
	}

	/**
     * Bind the specified server object under the specified name. If the
     * specified name is already bound, the new object will be rebinded under
     * the given name, overwriting the previous binding.
     * 
     * @param p_name
     *            The name to register the server under.
     * @param p_server
     *            The server object to register.
     * @exception NamingException -
     *                if a naming exception is encountered.
     * @exception GeneralException -
     *                General exception thrown by the system.
     */
	public void bind(String p_name, Remote p_server) throws GeneralException,
			NamingException
	{
		
		s_logger.info("JBoss registry binding " + p_name);
		m_registry.put(p_name, p_server);
	}

	/**
     * Lookup a server object by name.
     * 
     * @return The server object.
     * @param p_name
     *            Name of the server object to lookup.
     * @exception NamingException -
     *                if a naming exception is encountered.
     * @exception GeneralException -
     *                General exception thrown by the system.
     */
	public Object lookup(String p_name) throws NamingException,
			GeneralException
	{
		return m_registry.get(p_name);
	}

	/**
     * Unbind the specified server object under the specified name.
     * 
     * @param p_name
     *            The name of the server to unbind.
     * @exception NamingException -
     *                if a naming exception is encountered.
     * @exception GeneralException -
     *                General exception thrown by the system.
     */
	public void unbind(String p_name) throws GeneralException, NamingException
	{
		m_registry.remove(p_name);
	}

	/**
     * Get the initial context for performing naming operations.
     * 
     * @return The initial context.
     * @exception NamingException -
     *                if a naming exception is encountered.
     * @exception GeneralException -
     *                General exception thrown by the system.
     */
	private Context getInitialContext() throws NamingException,
			GeneralException
	{
		return s_appServerWrapper.getNamingContext();
	}
}
