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
package com.globalsight.ling.tm2.leverage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import com.globalsight.everest.projecthandler.LeverageProjectTM;
import com.globalsight.everest.projecthandler.ProjectHandler;
import com.globalsight.everest.projecthandler.ProjectTM;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.ling.tm.LeveragingLocales;
import com.globalsight.ling.tm.LingManagerException;

/**
 * LeverageOptions holds data about user selected Tm options .
 */

public class LeverageOptions
{
    public static final int PICK_LATEST = 1;
    public static final int PICK_OLDEST = 2;
    public static final int DEMOTE = 3;
    
    private TranslationMemoryProfile m_tmProfile = null;
    private LeveragingLocales m_leveragingLocales = null;
    private boolean m_latestReimport = false;
    
    private List<ProjectTM> m_projectTms;
    private List<ProjectTM> m_remoteTms;

    // constructor
    public LeverageOptions(TranslationMemoryProfile p_tmProfile,
        LeveragingLocales p_leveragingLocales)
    {
        m_tmProfile = p_tmProfile;
        m_leveragingLocales = p_leveragingLocales;
    }
    
    public boolean savesUntranslatedInPageTm()
    {
        return m_tmProfile.isSaveUnLocSegToPageTM();
    }
    
    public boolean savesUntranslatedInSegmentTm()
    {
        return m_tmProfile.isSaveUnLocSegToProjectTM();
    }

    public long getSaveTmId()
    {
        return m_tmProfile.getProjectTmIdForSave();
    }
    
    public int getNumberOfMatchesReturned()
    {
        return (int)m_tmProfile.getNumberOfMatchesReturned();
    }
    
    public LeveragingLocales getLeveragingLocales()
    {
        return m_leveragingLocales;
    }

    public boolean isExcluded(String p_type)
    {
        return m_tmProfile.getJobExcludeTuTypes().contains(p_type);
    }

    public boolean isLatestLeveragingForReimport()
    {
        return m_tmProfile.isLatestMatchForReimport();
    }
    
    public boolean isLeveragingLocalizables()
    {
        return m_tmProfile.isLeverageLocalizable();
    }
    
    public boolean leverageOnlyExactMatches()
    {
        return m_tmProfile.getIsExactMatchLeveraging();
    }
    
    // TODO: rename this getTmIdsToLeverageFrom()
    @SuppressWarnings("unchecked")
	public Collection<Long> getTmsToLeverageFrom()
    {
        Collection tms = m_tmProfile.getProjectTMsToLeverageFrom();
        Collection<Long> tmIds = new ArrayList<Long>(tms.size());
        Iterator itTms = tms.iterator();
        while(itTms.hasNext())
        {
            LeverageProjectTM tm = (LeverageProjectTM)itTms.next();
            tmIds.add(new Long(tm.getProjectTmId()));
        }
        
        return tmIds;
    }
    
    @SuppressWarnings("unchecked")
	public Map<Long, Integer> getTmIndexsToLeverageFrom()
    {
    	Map<Long, Integer> tmIndexs = new HashMap<Long, Integer>();
    	Collection<LeverageProjectTM> tms = m_tmProfile.getProjectTMsToLeverageFrom();
    	
    	Iterator<LeverageProjectTM> itTms = tms.iterator();
    	while(itTms.hasNext())
    	{
    		LeverageProjectTM tm = itTms.next();
    		tmIndexs.put(tm.getProjectTmId(), tm.getProjectTmIndex());
    	}
    	return tmIndexs;
    }
    
    public Map<Integer, Long> getTmIdIndexMap()
    {
    	Map<Integer, Long> tmIndexs = new HashMap<Integer, Long>();
    	Collection<LeverageProjectTM> tms = m_tmProfile.getProjectTMsToLeverageFrom();
    	
    	Iterator<LeverageProjectTM> itTms = tms.iterator();
    	while(itTms.hasNext())
    	{
    		LeverageProjectTM tm = itTms.next();
    		tmIndexs.put(tm.getProjectTmIndex(), tm.getProjectTmId());
    	}
    	return tmIndexs;

    }
    
    public Vector<Long> getOrderTmIds()
    {
    	Vector<Long> orderTmIds = null;
    	Vector<LeverageProjectTM> tms = m_tmProfile.getProjectTMsToLeverageFrom();
    	List<LeverageProjectTM> tmList = new ArrayList<LeverageProjectTM>(tms);
    	Collections.sort(tmList, new TmComparator());
    	orderTmIds = new Vector<Long>(tmList.size());
    	for(int i = 0; i < tmList.size(); i++)
    	{
    		LeverageProjectTM tm = (LeverageProjectTM) tmList.get(i);
    		orderTmIds.add(tm.getProjectTmId());
    	}
    	return orderTmIds;
    }
    
    public boolean isMultiLingLeveraging()
    {
        return m_tmProfile.isMultiLingualLeveraging();
    }
    
    public int getMatchThreshold()
    {
        return (int)m_tmProfile.getFuzzyMatchThreshold();
    }

    public boolean isNoMultipleExactMatchesForReimport()
    {
        return m_tmProfile.isMultipleMatchesForReimp();
    }

    public int getMultiTransForReimportPenalty()
    {
        return (int)m_tmProfile.getMultipleMatchesPenalty();
    }
    
    public boolean isTypeSensitiveLeveraging()
    {
        return m_tmProfile.isTypeSensitiveLeveraging();
    }
    
    public int getTypeDifferencePenalty()
    {
        return (int)m_tmProfile.getTypeDifferencePenalty();
    }
    
    public boolean isTypeSensitiveLeveragingForReimport()
    {
        return m_tmProfile.isTypeSensitiveLeveragingForReimp();
    }
    
    public int getTypeDifferencePenaltyForReimport()
    {
        return (int)m_tmProfile.getTypeDifferencePenaltyForReimp();
    }
    
    public boolean isCaseSensitiveLeveraging()
    {
        return m_tmProfile.isCaseSensitiveLeveraging();
    }
    
    public int getCaseDifferencePenalty()
    {
        return (int)m_tmProfile.getCaseDifferencePenalty();
    }
    
    public boolean isWhiteSpaceSensitiveLeveraging()
    {
        return m_tmProfile.isWhiteSpaceSensitiveLeveraging();
    }
    
    public int getWhiteSpaceDifferencePenalty()
    {
        return (int)m_tmProfile.getWhiteSpaceDifferencePenalty();
    }
    
    public boolean isCodeSensitiveLeveraging()
    {
        return m_tmProfile.isCodeSensitiveLeveraging();
    }
    
    public int getCodeDifferencePenalty()
    {
        return (int)m_tmProfile.getCodeDifferencePenalty();
    }
    
    public int getMultipleExactMatcheMode()
    {
        int mode = 0;
        String modeString = m_tmProfile.getMultipleExactMatches();
        if(modeString.equals(TranslationMemoryProfile.LATEST_EXACT_MATCH))
        {
            mode = PICK_LATEST;
        }
        else if(modeString.equals(
            TranslationMemoryProfile.OLDEST_EXACT_MATCH))
        {
            mode = PICK_OLDEST;
        }
        else if(modeString.equals(
            TranslationMemoryProfile.DEMOTED_EXACT_MATCH))
        {
            mode = DEMOTE;
        }
        
        return mode;
    }
    
    public int getMultiTransPenalty()
    {
        return (int)m_tmProfile.getMultipleExactMatchesPenalty();
    }

    
    public boolean isLatestReimport()
    {
        return m_latestReimport;
    }
    

    public void setLatestReimport(boolean p_latestReimport)
    {
        m_latestReimport = p_latestReimport;
    }
    

    public boolean dynamicLeveragesFromInProgressTm()
    {
        return m_tmProfile.getDynLevFromInProgressTm();
    }
    

    public boolean dynamicLeveragesFromGoldTm()
    {
        return m_tmProfile.getDynLevFromGoldTm();
    }
    

    public boolean dynamicLeveragesFromPopulationTm()
    {
        return m_tmProfile.getDynLevFromPopulationTm();
    }

    public boolean dynamicLeveragesFromReferenceTm()
    {
        return m_tmProfile.getDynLevFromReferenceTm();
    }
    
    public boolean isTmProcedence()
    {
    	return m_tmProfile.isTmProcendence();
    }
    
    public boolean isMatchPercentage()
    {
    	return m_tmProfile.isMatchPercentage();
    }
    
    public long getTmProfileId()
    {
    	return m_tmProfile.getId();
    }
    
    public boolean isRefTm()
    {
        return m_tmProfile.getSelectRefTm();
    }
    
    public long getRefTmPenalty()
    {
        return m_tmProfile.getRefTmPenalty();
    }
    
    public String getRefTMsToLeverageFrom()
    {
        return m_tmProfile.getRefTMsToLeverageFrom();
    }
    
    // TODO: change callers of current getTmsToLeverageFrom() over to use
    // this where possible
    public List<ProjectTM> getLeverageTms() 
            throws LingManagerException {
        if (m_projectTms == null) 
        {
            m_projectTms = new ArrayList<ProjectTM>();
            try 
            {
                ProjectHandler ph = ServerProxy.getProjectHandler();
                for (Long tmId : getTmsToLeverageFrom()) 
                {
                    m_projectTms.add(ph.getProjectTMById(tmId, false));
                }
            }
            catch (Exception e) 
            {
                throw new LingManagerException(e);
            }
        }
        return m_projectTms;
    }
    
    public List<ProjectTM> getRemoteTms() throws LingManagerException {
        if (m_remoteTms == null)
        {
            m_remoteTms = new ArrayList<ProjectTM>();
            for (ProjectTM tm : getLeverageTms())
            {
                if (tm.getIsRemoteTm())
                {
                    m_remoteTms.add(tm);
                }
            }
        }
        return m_remoteTms;
    }
    
    class TmComparator implements Comparator<LeverageProjectTM>
    {
		public int compare(LeverageProjectTM tm1, LeverageProjectTM tm2) {
			return tm1.getProjectTmIndex() - tm2.getProjectTmIndex();
		}
    }
    
    public boolean isAutoRepair()
    {
        return m_tmProfile.isAutoRepair();
    }
    
    /**
     * Get TM IDs to leverage from according to
     * "and from Jobs that write to the Storage TM" and
     * "and from Jobs that write to selected Reference TM(s)" options in TM
     * profile.
     */
    public Set<Long> getTmIdsForLevInProgressTmPurpose()
    {
        HashSet<Long> results = new HashSet();

        // Leverage from jobs that writes to population TM
        // For option "and from Jobs that write to the Storage TM".
        if(dynamicLeveragesFromPopulationTm())
        {
            long populationTmId = getSaveTmId();
            results.add(new Long(populationTmId));
        }
        
        // Leverage from jobs that writes to reference TM
        // For option "and from Jobs that write to selected Reference TM(s)".
        if(dynamicLeveragesFromReferenceTm())
        {
            Collection<Long> tmIds = getTmsToLeverageFrom();
            results.addAll(tmIds);
        }

        return results;
    }
}
