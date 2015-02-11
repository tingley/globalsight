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

import java.rmi.RemoteException;

/**
 * This type defines a remote interface of the class that listens to
 * messages that are to be acted upon by the Envoy system controller.
 */
public class EnvoySystemListenerLocal
    implements EnvoySystemListener
{
    private SystemControl m_envoyController;

    EnvoySystemListenerLocal (SystemControl p_envoyController)
    {
        m_envoyController = p_envoyController;
    }

    /**
     * Request the Envoy system to shutdown.
     */
    public void shutdownSystem()
        throws RemoteException, SystemShutdownException
    {
        if (m_envoyController != null)
        {
            m_envoyController.shutdown();
        }
    }
}
