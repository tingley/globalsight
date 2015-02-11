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



package com.globalsight.everest.integration.ling.tm;

import java.util.List;
import java.util.Collection;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import com.globalsight.util.GlobalSightLocale;
import com.globalsight.ling.tm.IndexerLocal;
import com.globalsight.ling.tm.TuvLing;
import com.globalsight.ling.tm.LingManagerException;
import com.globalsight.ling.tm.fuzzy.FuzzyIndexManagerException;
import com.globalsight.everest.util.system.RemoteServer;
import com.globalsight.everest.util.system.SystemStartupException;

public class IndexerWLRMIImpl
    extends RemoteServer
    implements IndexerWLRemote
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            IndexerWLRMIImpl.class.getName());

    private IndexerLocal m_indexerLocal = null;

    public IndexerWLRMIImpl()
        throws RemoteException
    {
        super(SERVICE_NAME);
        m_indexerLocal = new IndexerLocal();
    }

    /**
     * Get the reference to the local implementation of the server.
     *
     * @return The reference to the local implementation of the server.
     */
    public Object getLocalReference()
    {
        return m_indexerLocal;
    }

    /**
     * Index an entire page for a single locale.
     *
     * @param p_leverageGroupIds - The group id (page) or group ids
     * (db records) that we need to index.
     * @param p_locale - locale of the Tuv(s) we need to index.
     * @param p_tmId - Id of the TM we add the indexed content to.
     * @param p_services - used to get reference to the TuvLingManager
     * and TmFuzzyIndexManager.
    */
    /*
    public void index(List p_leverageGroupIds, GlobalSightLocale p_locale,
        long p_tmId, Collection p_excludeTypes)
        throws RemoteException,
               FuzzyIndexManagerException,
               LingManagerException
    {
        m_indexerLocal.index(p_leverageGroupIds, p_locale, p_tmId, p_excludeTypes);
    }
    */
    /**
     * Index a single Tuv.
     *
     * @param p_tuv - the TUV we wish to index.
     */
    public void index(TuvLing p_tuv) 
        throws RemoteException, FuzzyIndexManagerException, LingManagerException 
    {
        m_indexerLocal.index(p_tuv);
    }    


    public void index(List p_tuvs)
        throws RemoteException,
               FuzzyIndexManagerException,
               LingManagerException
    {        
        m_indexerLocal.index(p_tuvs);
    }    
    
}
