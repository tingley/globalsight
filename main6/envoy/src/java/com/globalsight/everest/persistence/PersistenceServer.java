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

package com.globalsight.everest.persistence;

import org.apache.log4j.Logger;

import com.globalsight.everest.util.system.ServerObject;
import com.globalsight.everest.util.system.SystemShutdownException;
import com.globalsight.everest.util.system.SystemStartupException;
import com.globalsight.everest.util.system.SystemUtilException;

/**
 * Provides the ability to start and stop the PersistenceService.
 */
public class PersistenceServer implements ServerObject
{

    private static final Logger CATEGORY = Logger
            .getLogger(PersistenceServer.class.getName());

    PersistenceService m_persistenceService = null;

    public PersistenceServer()
    {
        super();
    }

    /**
     * Stops the PersistenceService.
     * 
     * @throws SystemShutdownException
     *             if there is a problem stopping the PersistenceService.
     */
    public void destroy() throws SystemShutdownException
    {
        if (m_persistenceService != null)
        {
            m_persistenceService = null;

            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("PersistenceServer destroyed.");
            }
        }
    }

    /**
     * Initializes the PersistenceService.
     * 
     * @throws SystemStartupException
     *             if there is a problem starting the PersistenceService.
     */
    public void init() throws SystemStartupException
    {
        try
        {
            if (m_persistenceService == null)
            {
                m_persistenceService = PersistenceService.getInstance();
            }

            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("PersistenceServer initialized.");
            }

        }
        catch (Exception pe)
        {
            CATEGORY.error("PersistenceServer::init", pe);

            throw new SystemStartupException(
                    SystemUtilException.EX_FAILEDTOINITSERVER, pe);
        }
    }
}
