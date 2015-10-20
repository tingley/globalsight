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
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.util.system.RemoteServer;
import com.globalsight.ling.tm.LeverageMatchLingManager;
import com.globalsight.ling.tm.LeverageSegment;
import com.globalsight.ling.tm.LingManagerException;
import com.globalsight.ling.tm2.leverage.LeverageDataCenter;
import com.globalsight.ling.tm2.leverage.LeverageMatches;
import com.globalsight.ling.tm2.leverage.LeverageOptions;
import com.globalsight.util.GlobalSightLocale;

public class LeverageMatchLingManagerWLRMIImpl extends RemoteServer implements
        LeverageMatchLingManagerWLRemote
{
    private LeverageMatchLingManager m_localInstance = null;

    public LeverageMatchLingManagerWLRMIImpl() throws RemoteException
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

	@Override
    public void deleteLeverageMatches(Long p_OriginalSourceTuvId,
            String p_subId, Long p_targetLocaleId, Long p_orderNum, long p_jobId)
            throws LingManagerException
    {
        m_localInstance.deleteLeverageMatches(p_OriginalSourceTuvId, p_subId,
                p_targetLocaleId, p_orderNum, p_jobId);
    }

	@Override
	public void deleteLeverageMatches(List<Long> p_originalSourceTuvIds,
			GlobalSightLocale p_targetLocale, int p_deleteFlag, long p_jobId)
	{
		m_localInstance.deleteLeverageMatches(p_originalSourceTuvIds,
				p_targetLocale, p_deleteFlag, p_jobId);
	}

	public HashMap<Long, LeverageSegment> getExactMatches(Long p_spLgId,
            Long p_targetLocaleId) throws RemoteException, LingManagerException
    {
        return m_localInstance.getExactMatches(p_spLgId, p_targetLocaleId);
    }

    public HashMap<Long, Set<LeverageMatch>> getFuzzyMatches(
            Long p_sourePageId, Long p_targetLocaleId) throws RemoteException,
            LingManagerException
    {
        return m_localInstance.getFuzzyMatches(p_sourePageId, p_targetLocaleId);
    }

    public SortedSet<LeverageMatch> getTuvMatches(Long p_sourceTuvId,
            Long p_targetLocaleId, String p_subId, boolean isTmProcedence,
            long p_jobId, long... tmIds) throws RemoteException,
            LingManagerException
    {
        return m_localInstance.getTuvMatches(p_sourceTuvId, p_targetLocaleId,
                p_subId, isTmProcedence, p_jobId, tmIds);
    }

    /**
     * @see com.globalsight.ling.tm.LeverageMatchLingManager#getMatchTypes(java.util.Collection,
     *      long, int)
     */
    public MatchTypeStatistics getMatchTypesForStatistics(Long p_sourcePageId,
            Long p_targetLocaleId, int p_levMatchThreshold)
            throws RemoteException, LingManagerException
    {
        return m_localInstance.getMatchTypesForStatistics(p_sourcePageId,
                p_targetLocaleId, p_levMatchThreshold);
    }

    public boolean isMatchCopied(int p_lingManagerMatchType)
            throws RemoteException, LingManagerException
    {
        return m_localInstance.isMatchCopied(p_lingManagerMatchType);
    }

    public Map<Long, Set<LeverageMatch>> getExactMatchesForDownLoadTmx(
            Long pageId, Long idAsLong)
    {
        return m_localInstance.getExactMatchesForDownLoadTmx(pageId, idAsLong);
    }

    public List<LeverageMatch> getLeverageMatchesForOfflineDownLoad(
            Long pageId, Long idAsLong)
    {
        return m_localInstance.getLeverageMatchesForOfflineDownLoad(pageId,
                idAsLong);
    }

    public HashMap<Long, ArrayList<LeverageSegment>> getExactMatchesWithSetInside(
            Long pageId, Long localeId, int model,
            TranslationMemoryProfile tmProfile)
    {
        return m_localInstance.getExactMatchesWithSetInside(pageId, localeId,
                model, tmProfile);
    }

    public boolean isIncludeMtMatches()
    {
        return m_localInstance.isIncludeMtMatches();
    }

    public void setIncludeMtMatches(boolean isIncludeMtMatches)
    {
        m_localInstance.setIncludeMtMatches(isIncludeMtMatches);
    }

    public void saveLeverageResults(Connection p_connection,
            SourcePage p_sourcePage, LeverageDataCenter p_leverageDataCenter)
            throws LingManagerException
    {
        m_localInstance.saveLeverageResults(p_connection, p_sourcePage,
                p_leverageDataCenter);
    }

    public void saveLeverageResults(Connection p_connection,
            long p_sourcePageId,
            Map<Long, LeverageMatches> p_leverageMatchesMap,
            GlobalSightLocale p_targetLocale, LeverageOptions p_leverageOptions)
            throws LingManagerException
    {
        m_localInstance.saveLeverageResults(p_connection, p_sourcePageId,
                p_leverageMatchesMap, p_targetLocale, p_leverageOptions);
    }

    public void saveLeveragedMatches(
            Collection<LeverageMatch> p_leverageMatchList, long p_jobId)
            throws RemoteException, LingManagerException
    {
        m_localInstance.saveLeveragedMatches(p_leverageMatchList, p_jobId);
    }

    public void saveLeveragedMatches(
            Collection<LeverageMatch> p_leverageMatchList,
            Connection p_connection, long p_jobId) throws LingManagerException
    {
        m_localInstance.saveLeveragedMatches(p_leverageMatchList, p_connection,
                p_jobId);
    }

    /**
     * Get best match score for specified TUV.
     */
    public float getBestMatchScore(Connection p_connection,
            long p_originalSourceTuvId, long p_targetLocaleId, String p_subId,
            long p_jobId)
    {
        return m_localInstance.getBestMatchScore(p_connection,
                p_originalSourceTuvId, p_targetLocaleId, p_subId, p_jobId);
    }

	public List<LeverageMatch> getExactLeverageMatches(Long p_sourcePageId,
			Long p_targetLocaleId)
	{
		return m_localInstance.getExactLeverageMatches(p_sourcePageId,
				p_targetLocaleId);
	}
}