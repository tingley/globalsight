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

import com.globalsight.util.GeneralException;
import com.globalsight.ling.tm.Leverager;
import com.globalsight.ling.tm.LeveragerLocal;
import com.globalsight.ling.tm.LeverageProperties;
import com.globalsight.ling.tm.LeveragingLocales;
import com.globalsight.ling.tm.ExactMatchedSegments;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.ling.tm.LingManagerException;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.util.system.RemoteServer;
import com.globalsight.ling.tm.TargetLocaleLgIdsMapper;
import com.globalsight.ling.tm2.leverage.LeverageDataCenter;

public class LeveragerWLRMIImpl
    extends RemoteServer
    implements LeveragerWLRemote
{
     private static final Logger CATEGORY =
        Logger.getLogger(
        LeveragerWLRMIImpl.class.getName());
     
    private Leverager m_localInstance = null;

    /**
     * LeveragerWLRMIImpl constructor comment.
     */
    public LeveragerWLRMIImpl()
        throws RemoteException
    {
        super(SERVICE_NAME);
        m_localInstance = new LeveragerLocal();
    }

    /**
     * Get the reference to the local implementation of the server.
     *
     * @return The reference to the local implementation of the server.
     */
    public Object getLocalReference()
    {
        return m_localInstance;
    }
    
//     public ExactMatchedSegments leverage(
//         SourcePage p_sourcePage,
//         List p_tmIdsToSearch,
//         GlobalSightLocale p_sourceLocale,
//         LeveragingLocales p_leveragingLocales,        
//         LeverageProperties p_leverageProperties,
//         Collection p_excludeTypes,
//         Collection p_leverageExcludeTypes)
//         throws RemoteException, LingManagerException
//     {
//         return m_localInstance.leverage(
//             p_sourcePage,
//             p_tmIdsToSearch,
//             p_sourceLocale,
//             p_leveragingLocales,
//             p_leverageProperties,
//             p_excludeTypes,
//             p_leverageExcludeTypes);
//     }
    
//     public ExactMatchedSegments leverageForReimport(
//         SourcePage p_sourcePage,
//         TargetLocaleLgIdsMapper p_localeLgIdsMapper,
//         List p_tmIdsToSearch,
//         GlobalSightLocale p_sourceLocale,
//         LeveragingLocales p_leveragingLocales,       
//         LeverageProperties p_leverageProperties,
//         Collection p_excludeTypes,
//         Collection p_leverageExcludeTypes)
//         throws RemoteException, LingManagerException
//     {
//         return m_localInstance.leverageForReimport(
//             p_sourcePage,
//             p_localeLgIdsMapper,
//             p_tmIdsToSearch,
//             p_sourceLocale,
//             p_leveragingLocales,
//             p_leverageProperties,
//             p_excludeTypes,
//             p_leverageExcludeTypes);
//     }


    public void leverageForReimport(
        SourcePage p_sourcePage,
        TargetLocaleLgIdsMapper p_localeLgIdsMapper,
        GlobalSightLocale p_sourceLocale,
        LeverageDataCenter p_leverageDataCenter)
        throws RemoteException, LingManagerException
    {
        m_localInstance.leverageForReimport(p_sourcePage,
            p_localeLgIdsMapper, p_sourceLocale, p_leverageDataCenter);
    }
}
