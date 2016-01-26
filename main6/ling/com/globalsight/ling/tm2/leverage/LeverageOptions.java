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
import com.globalsight.util.SortUtil;

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

    private boolean fromTMSearchPage = false;

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
    
    public boolean saveWhollyInternalTextSegmentTm()
    {
        return m_tmProfile.isSaveWhollyInternalTextToProjectTM();
    }
    
    public boolean saveLocalizedInSegmentTM()
    {
    	return m_tmProfile.isSaveLocSegToProjectTM();
    }
    
    public boolean savesApprovedInSegmentTm()
    {
        return m_tmProfile.isSaveApprovedSegToProjectTM();
    }
    
    public boolean savesExactMatchInSegmentTm()
    {
        return m_tmProfile.isSaveExactMatchSegToProjectTM();
    }

    public long getSaveTmId()
    {
        return m_tmProfile.getProjectTmIdForSave();
    }

    public int getNumberOfMatchesReturned()
    {
        return (int) m_tmProfile.getNumberOfMatchesReturned();
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

    public Collection<Long> getTmIdsToLeverageFrom()
    {
        Vector<LeverageProjectTM> tms = m_tmProfile.getProjectTMsToLeverageFrom();
        SortUtil.sort(tms, new Comparator<LeverageProjectTM>()
        {
            @Override
            public int compare(LeverageProjectTM o1, LeverageProjectTM o2)
            {
                return o1.getProjectTmIndex() - o2.getProjectTmIndex();
            }
        });
        Collection<Long> tmIds = new ArrayList<Long>(tms.size());
        for (LeverageProjectTM tm : tms)
        {
            tmIds.add(tm.getProjectTmId());
        }

        return tmIds;
    }

    public Map<Long, Integer> getTmIndexsToLeverageFrom()
    {
        Map<Long, Integer> tmIndexs = new HashMap<Long, Integer>();
        Collection<LeverageProjectTM> tms = m_tmProfile
                .getProjectTMsToLeverageFrom();

        Iterator<LeverageProjectTM> itTms = tms.iterator();
        while (itTms.hasNext())
        {
            LeverageProjectTM tm = itTms.next();
            tmIndexs.put(tm.getProjectTmId(), tm.getProjectTmIndex());
        }
        return tmIndexs;
    }

    public Map<Integer, Long> getTmIdIndexMap()
    {
        Map<Integer, Long> tmIndexs = new HashMap<Integer, Long>();
        Collection<LeverageProjectTM> tms = m_tmProfile
                .getProjectTMsToLeverageFrom();

        Iterator<LeverageProjectTM> itTms = tms.iterator();
        while (itTms.hasNext())
        {
            LeverageProjectTM tm = itTms.next();
            tmIndexs.put(tm.getProjectTmIndex(), tm.getProjectTmId());
        }
        return tmIndexs;

    }

    public Vector<Long> getOrderTmIds()
    {
        Vector<Long> orderTmIds = null;
        Vector<LeverageProjectTM> tms = m_tmProfile
                .getProjectTMsToLeverageFrom();
        List<LeverageProjectTM> tmList = new ArrayList<LeverageProjectTM>(tms);
        SortUtil.sort(tmList, new TmComparator());
        orderTmIds = new Vector<Long>(tmList.size());
        for (int i = 0; i < tmList.size(); i++)
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
        return (int) m_tmProfile.getFuzzyMatchThreshold();
    }

    public boolean isNoMultipleExactMatchesForReimport()
    {
        return m_tmProfile.isMultipleMatchesForReimp();
    }

    public int getMultiTransForReimportPenalty()
    {
        return (int) m_tmProfile.getMultipleMatchesPenalty();
    }

    public boolean isTypeSensitiveLeveraging()
    {
        return m_tmProfile.isTypeSensitiveLeveraging();
    }

    public int getTypeDifferencePenalty()
    {
        return (int) m_tmProfile.getTypeDifferencePenalty();
    }

    public boolean isTypeSensitiveLeveragingForReimport()
    {
        return m_tmProfile.isTypeSensitiveLeveragingForReimp();
    }

    public int getTypeDifferencePenaltyForReimport()
    {
        return (int) m_tmProfile.getTypeDifferencePenaltyForReimp();
    }

    public boolean isCaseSensitiveLeveraging()
    {
        return m_tmProfile.isCaseSensitiveLeveraging();
    }

    public int getCaseDifferencePenalty()
    {
        return (int) m_tmProfile.getCaseDifferencePenalty();
    }

    public boolean isWhiteSpaceSensitiveLeveraging()
    {
        return m_tmProfile.isWhiteSpaceSensitiveLeveraging();
    }

    public int getWhiteSpaceDifferencePenalty()
    {
        return (int) m_tmProfile.getWhiteSpaceDifferencePenalty();
    }

    public boolean isCodeSensitiveLeveraging()
    {
        return m_tmProfile.isCodeSensitiveLeveraging();
    }

    public int getCodeDifferencePenalty()
    {
        return (int) m_tmProfile.getCodeDifferencePenalty();
    }

    public int getMultipleExactMatcheMode()
    {
    	return m_tmProfile.getMultipleExactMatcheMode();
    }

    public int getMultiTransPenalty()
    {
        return (int) m_tmProfile.getMultipleExactMatchesPenalty();
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
    
    public boolean dynamicLeveragesStopSearch()
    {
        return m_tmProfile.getDynLevStopSearch();
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

    public TranslationMemoryProfile getTmProfile()
    {
        return m_tmProfile;
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

    public List<ProjectTM> getLeverageTms() throws LingManagerException
    {
        if (m_projectTms == null)
        {
            m_projectTms = new ArrayList<ProjectTM>();
            try
            {
                ProjectHandler ph = ServerProxy.getProjectHandler();
                for (Long tmId : getTmIdsToLeverageFrom())
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

    public List<ProjectTM> getRemoteTms() throws LingManagerException
    {
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
        public int compare(LeverageProjectTM tm1, LeverageProjectTM tm2)
        {
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
        HashSet<Long> results = new HashSet<Long>();

        // Leverage from jobs that writes to population TM
        // For option "and from Jobs that write to the Storage TM".
        if (dynamicLeveragesFromPopulationTm())
        {
            long populationTmId = getSaveTmId();
            results.add(new Long(populationTmId));
        }

        // Leverage from jobs that writes to reference TM
        // For option "and from Jobs that write to selected Reference TM(s)".
        if (dynamicLeveragesFromReferenceTm())
        {
            Collection<Long> tmIds = getTmIdsToLeverageFrom();
            results.addAll(tmIds);
        }

        return results;
    }

    public boolean isFromTMSearchPage()
    {
        return fromTMSearchPage;
    }

    public void setFromTMSearchPage(boolean fromTMSearchPage)
    {
        this.fromTMSearchPage = fromTMSearchPage;
    }

    public boolean getUniqueFromMultipleTranslation()
    {
        return getTmProfile().isUniqueFromMultipleTranslation();
    }
}
