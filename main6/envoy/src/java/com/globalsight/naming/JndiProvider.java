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

package com.globalsight.naming;


import java.util.Properties;
import java.util.HashMap;
import javax.naming.Context;


/**
 * Encapsulates the Properties needed to get InitialContexts.
 * Different JNDI providers require different Property values.
 */

public final class JndiProvider
{ 
    private String m_contextFactory = null;
	private String m_providerProtocol = null;

	public static final String SUN_RMI_REGISTRY = "SUN_RMI_REGISTRY"; 
	public static final JndiProvider SUN_RMI_REGISTRY_PROVIDER = 
            new JndiProvider(
            "com.sun.jndi.rmi.registry.RegistryContextFactory", "rmi");

	private static final HashMap m_nameToProvider = new HashMap(3);
	static 
    {
	    m_nameToProvider.put(SUN_RMI_REGISTRY, 
                SUN_RMI_REGISTRY_PROVIDER);
	}
	
	public static JndiProvider getProvider(String providerName)
	{
		return (JndiProvider)m_nameToProvider.get(providerName);
	}
	
	/** 
	  * Constructor is private because only the pre-constructed, 
	  * public static final JndiProviders are to be used. 
	  */
	private JndiProvider(String contextFactory, 
            String providerProtocol) 
    {
	    m_contextFactory = contextFactory;
		m_providerProtocol = providerProtocol;
	}
    
	
	public Properties getInitialContextProperties(String host, 
            String port) 
    {
        	Properties jndiProperties = new Properties();
        	jndiProperties.put(Context.INITIAL_CONTEXT_FACTORY, 
                    m_contextFactory);
        	jndiProperties.put(Context.PROVIDER_URL, 
                    m_providerProtocol + "://" + host + ":" + port);
        	return(jndiProperties);
	}
}
