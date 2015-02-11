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



package com.globalsight.everest.integration.ling;

import com.globalsight.everest.integration.ling.tm2.LeverageMatchLingManagerWLRemote;
import com.globalsight.ling.tm.LeverageMatchLingManager;

import com.globalsight.ling.tm2.TmCoreManagerWLRemote;
import com.globalsight.ling.tm2.TmCoreManager;
import com.globalsight.ling.tm.Leverager;
import com.globalsight.everest.integration.ling.tm.LeveragerWLRemote;
import com.globalsight.ling.inprogresstm.InProgressTmManagerWLRemote;



import com.globalsight.everest.util.server.RegistryLocator;
import com.globalsight.everest.util.server.ServerRegistry;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GeneralExceptionConstants;

import javax.naming.NamingException;


public class LingServerProxy
{
    //
    // Static Member Variables
    //

    // lazy instantiation variables.
    static private TmCoreManager m_tmCoreManager = null;
    static private Leverager m_leverager = null;
    static private LeverageMatchLingManager m_leverageMatchLingManager = null;
    private static InProgressTmManagerWLRemote m_inProgressTmManager = null;

    //
    // Constructor
    //
    private LingServerProxy(){}

    //
    // Public Methods
    //

    /**
     * Get an instance of Leverager type.
     * @return The Leverager object.
     */
    static public TmCoreManager getTmCoreManager()
        throws GeneralException
    {
        if (m_tmCoreManager == null)
        {
            try
            {
                ServerRegistry serverRegistry = RegistryLocator.getRegistry();
                m_tmCoreManager = (TmCoreManager)serverRegistry.lookup(
                    TmCoreManagerWLRemote.SERVICE_NAME);
            }
            catch (NamingException ne)
            {
                error(TmCoreManagerWLRemote.SERVICE_NAME, ne);
            }
        }

        return m_tmCoreManager;
    }

    /**
     * Get an instance of Leverager type.
     * @return The Leverager object.
     */
    public static Leverager getLeverager()
        throws GeneralException
    {
        if (m_leverager == null)
        {
            try
            {
                ServerRegistry serverRegistry = RegistryLocator.getRegistry();
                m_leverager = (Leverager)serverRegistry.lookup(
                    LeveragerWLRemote.SERVICE_NAME);
            }
            catch (NamingException ne)
            {
                error(LeveragerWLRemote.SERVICE_NAME, ne);
            }
        }

        return m_leverager;
    }


    /**
     * Get an instance of LeverageMatchLingManager type.
     * @return The LeverageMatchLingManager object.
     */
    static public LeverageMatchLingManager getLeverageMatchLingManager()
        throws GeneralException
    {
        if (m_leverageMatchLingManager == null)
        {
            try
            {
                ServerRegistry serverRegistry = RegistryLocator.getRegistry();
                m_leverageMatchLingManager =
                    (LeverageMatchLingManager)serverRegistry.lookup(
                        LeverageMatchLingManagerWLRemote.SERVICE_NAME);
            }
            catch (NamingException ne)
            {
                error(LeverageMatchLingManagerWLRemote.SERVICE_NAME, ne);
            }
        }

        return m_leverageMatchLingManager;
    }


    public static InProgressTmManagerWLRemote getInProgressTmManager()
        throws GeneralException
    {
        if (m_inProgressTmManager == null)
        {
            try
            {
                ServerRegistry serverRegistry = RegistryLocator.getRegistry();
                m_inProgressTmManager =
                    (InProgressTmManagerWLRemote)serverRegistry.lookup(
                        InProgressTmManagerWLRemote.SERVICE_NAME);
            }
            catch (NamingException ne)
            {
                error(InProgressTmManagerWLRemote.SERVICE_NAME, ne);
            }
        }

        return m_inProgressTmManager;
    }


    static private void error(String p_name, Exception p_exception)
        throws GeneralException
    {
        throw new GeneralException(
            GeneralExceptionConstants.COMP_SYSUTIL,
            GeneralExceptionConstants.EX_NAMING,
            "remote server " + p_name + " not found", p_exception);
    }
}
