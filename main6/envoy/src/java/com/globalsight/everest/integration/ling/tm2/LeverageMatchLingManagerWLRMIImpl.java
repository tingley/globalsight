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
package com.globalsight.everest.integration.ling.tm2;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;

import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.util.system.RemoteServer;
import com.globalsight.ling.tm.LeverageMatchLingManager;
import com.globalsight.ling.tm.LingManagerException;
import com.globalsight.log.GlobalSightCategory;

public class LeverageMatchLingManagerWLRMIImpl
    extends RemoteServer
    implements LeverageMatchLingManagerWLRemote
{
    static private final GlobalSightCategory CATEGORY =
        (GlobalSightCategory)GlobalSightCategory.getLogger(
            LeverageMatchLingManagerWLRMIImpl.class);

    private LeverageMatchLingManager m_localInstance = null;

    public LeverageMatchLingManagerWLRMIImpl()
        throws RemoteException
    {
        super(SERVICE_NAME);
        m_localInstance = new LeverageMatchLingManagerLocal();
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

    public void saveLeveragedMatches(Collection p_leverageMatchList)
        throws RemoteException, LingManagerException
    {
        m_localInstance.saveLeveragedMatches(p_leverageMatchList);
    }

    public HashMap getExactMatches(Long p_spLgId,
        Long p_targetLocaleId)
        throws RemoteException, LingManagerException
    {
        return m_localInstance.getExactMatches(p_spLgId, p_targetLocaleId);
    }

    public HashMap getFuzzyMatches(Long p_spLgId, Long p_targetLocaleId)
        throws RemoteException, LingManagerException
    {
        return m_localInstance.getFuzzyMatches(p_spLgId, p_targetLocaleId);
    }

    public SortedSet getTuvMatches(Long p_sourceTuvId, Long p_targetLocaleId,
        String p_subId, boolean isTmProcedence, long...tmIds)
        throws RemoteException, LingManagerException
    {
        return m_localInstance.getTuvMatches(p_sourceTuvId, p_targetLocaleId,
            p_subId, isTmProcedence,  tmIds);
    }

    /**
     * @see com.globalsight.ling.tm.LeverageMatchLingManager#getMatchTypes(java.util.Collection,
     *      long, int)
     */
    public MatchTypeStatistics getMatchTypesForStatistics(
        Long p_sourcePageId, Long p_targetLocaleId, int p_levMatchThreshold)
        throws RemoteException, LingManagerException
    {
        return m_localInstance.getMatchTypesForStatistics(
            p_sourcePageId, p_targetLocaleId, p_levMatchThreshold);
    }

    public boolean isMatchCopied(int p_lingManagerMatchType)
        throws RemoteException, LingManagerException
    {
        return m_localInstance.isMatchCopied(p_lingManagerMatchType);
    }

    public Map getExactMatchesForDownLoadTmx(Long pageId, Long idAsLong) 
    {
        return m_localInstance.getExactMatchesForDownLoadTmx(pageId, idAsLong);
    }


    public void updateProjectTmIndex(long tmId, int projectTmIndex, long tmProfileId) {
        m_localInstance.updateProjectTmIndex(tmId, projectTmIndex, tmProfileId);
    }


    public HashMap getExactMatchesWithSetInside(Long pageId, Long localeId, int model, TranslationMemoryProfile tmProfile)
    {
        return m_localInstance.getExactMatchesWithSetInside(pageId, localeId, model, tmProfile);
    }
    
    public boolean isIncludeMtMatches()
    {
        return m_localInstance.isIncludeMtMatches();
    }
    
    public void setIncludeMtMatches(boolean isIncludeMtMatches)
    {
        m_localInstance.setIncludeMtMatches(isIncludeMtMatches);
    }

}
