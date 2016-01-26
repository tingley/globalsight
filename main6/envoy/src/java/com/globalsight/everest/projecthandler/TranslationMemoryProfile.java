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

package com.globalsight.everest.projecthandler;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import com.globalsight.cxe.entity.customAttribute.TMPAttribute;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.foundation.TDATM;
import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.ling.tm2.leverage.LeverageOptions;
import com.globalsight.util.SortUtil;
import com.globalsight.util.StringUtil;

public class TranslationMemoryProfile extends PersistentObject
{

    private static final long serialVersionUID = -3548514967241450885L;

    // separator used between TU types to exclude - for parsing the string.
    public static final String EXCLUDE_TU_TYPE_DELIMITER = "|";

    public static final String LATEST_EXACT_MATCH = "LATEST";
    public static final String OLDEST_EXACT_MATCH = "OLDEST";
    public static final String DEMOTED_EXACT_MATCH = "DEMOTED";

    // SID only
    public static final int ICE_PROMOTION_SID_ONLY = 1;
    // SID and hash matches
    public static final int ICE_PROMOTION_SID_HASH_MATCHES = 2;
    // SID, hash and bracketed matches
    public static final int ICE_PROMOTION_ALL = 3;

    // How to deal with TM matches which TU attributes do not match settings.
    public static final String CHOICE_DISREGARD = "disregard";
    public static final String CHOICE_PENALIZE = "penalize";

    // 1 back pointer to L10nProfile
    private L10nProfile m_l10nProfile = null;

    // 2 one to many mapping from TM Profile to List of Project TMs to Leverage
    // From
    private Vector<LeverageProjectTM> m_projectTMsToLeverageFrom = new Vector<LeverageProjectTM>();

    // Use this field for modification only.
    private Vector<LeverageProjectTM> m_newProjectTMsToLeverageFrom = new Vector<LeverageProjectTM>();

    // 3
    private String m_name = null;

    // 4
    private String m_description = null;

    // 5
    private long m_projectTmIdForSave = 0;

    // 6
    private boolean m_isSaveUnLocSegToProjectTM = true;

    // 6
    private boolean m_saveWhollyInternalTextToProjectTM = false;

    // 7
    private boolean m_isSaveUnLocSegToPageTM = true;

    // 8
    protected Vector<String> m_jobExcludeTuTypes = new Vector<String>();

    // 9
    private boolean m_isLeverageLocalizable = true;

    // 10
    private boolean m_isTypeSensitiveLeveraging = true;

    // 11
    private long m_typeDifferencePenalty = -1;

    // 12
    private boolean m_isCaseSensitiveLeveraging = true;

    // 13
    private long m_caseDifferencePenalty = -1;

    // 14
    private boolean m_isWhiteSpaceSensitiveLeveraging = true;

    // 15
    private long m_whiteSpaceDifferencePenalty = -1;

    // 16
    private boolean m_isCodeSensitiveLeveraging = true;

    // 17
    private long m_codeDifferencePenalty = -1;

    // 18
    private boolean m_isMultiLingualLeveraging = true;

    // 19
    private String m_multipleExactMatches;

    // 20
    private long m_multipleExactMatchesPenalty = -1;

    // 21
    private long m_fuzzyMatchThreshold;

    // 22
    private long m_numberOfMatchesReturned;

    // 23
    private boolean m_isLatestMatchForReimport = false;

    // 24
    private boolean m_isTypeSensitiveLeveragingForReimp = false;

    // 25
    private long m_typeDifferencePenaltyForReimp = -1;

    // 26
    private boolean m_isMultipleMatchesForReimp = false;

    // 27
    private long m_multipleMatchesPenalty = -1;

    // 28
    private boolean m_isExactMatchLeveraging = false;

    private boolean m_isContextMatchLeveraging = true;
    private int icePromotionRules = ICE_PROMOTION_ALL;

    private boolean m_dynLevFromGoldTm = false;

    private boolean m_dynLevFromInProgressTm = true;
    
    private boolean m_dynLevStopSearch = false;

    private boolean m_dynLevFromPopulationTm = false;

    private boolean m_dynLevFromReferenceTm = false;

    private boolean m_selectRefTm = false;
    private long m_refTmPenalty = -1;
    private String m_refTMsToLeverageFrom;

    private boolean isMatchPercentage = true;

    private boolean isTmProcendence = false;

    private boolean autoRepair = true;
    
    private TDATM tdatm;

    private boolean uniqueFromMultipleTranslation = false;

    private long m_companyId = -1;
    
    private boolean m_isSaveApprovedSegToProjectTM = true;
    
    private boolean m_isSaveLocSegToProjectTM = true;
    
    private boolean m_isSaveExactMatchSegToProjectTM = true;

    private Set<TMPAttribute> attributes;
    private String choiceIfAttNotMatch = null;
    private int tuAttNotMatchPenalty = 0;

    public TranslationMemoryProfile()
    {
    }

    public void setName(String p_name)
    {
        m_name = p_name;
    }

    public void setDescription(String p_description)
    {
        m_description = p_description;
    }

    public void setProjectTmIdForSave(long p_projectTmIdForSave)
    {
        m_projectTmIdForSave = p_projectTmIdForSave;
    }

    public void setLeverageLocalizable(boolean p_isLeverageLocalizable)
    {
        m_isLeverageLocalizable = p_isLeverageLocalizable;
    }

    public void setSaveUnLocSegToProjectTM(boolean p_isSaveUnLocSegToProjectTM)
    {
        m_isSaveUnLocSegToProjectTM = p_isSaveUnLocSegToProjectTM;
    }
    
    public void setSaveWhollyInternalTextToProjectTM(boolean p_saveWhollyInternalTextToProjectTM)
    {
    	m_saveWhollyInternalTextToProjectTM = p_saveWhollyInternalTextToProjectTM;
    }
    
    public void setSaveApprovedSegToProjectTM(boolean p_isSaveApprovedSegToProjectTM)
    {
		this.m_isSaveApprovedSegToProjectTM = p_isSaveApprovedSegToProjectTM;
	}  

    public void setSaveExactMatchSegToProjectTM(boolean p_isSaveExactMatchSegToProjectTM)
    {
		this.m_isSaveExactMatchSegToProjectTM = p_isSaveExactMatchSegToProjectTM;
	}

    public void setSaveUnLocSegToPageTM(boolean p_isSaveUnLocSegToPageTM)
    {
        m_isSaveUnLocSegToPageTM = p_isSaveUnLocSegToPageTM;
    }

    public void setExcludeTuTypes(String p_tuTypes)
    {
        if (m_jobExcludeTuTypes.size() > 0)
        {
            m_jobExcludeTuTypes.clear();
        }

        if (p_tuTypes != null)
        {
            // parse through string
            StringTokenizer st = new StringTokenizer(p_tuTypes,
                    EXCLUDE_TU_TYPE_DELIMITER);

            while (st.hasMoreTokens())
            {
                String tuType = st.nextToken();
                m_jobExcludeTuTypes.add(tuType.trim());
            }
        }
    }

    public void setTuTypes(String tuTypes)
    {
        setExcludeTuTypes(tuTypes);
    }

    public String getTuTypes()
    {
        return getJobExcludeTuTypesAsString();
    }

    public void setProjectTMToLeverageFrom(LeverageProjectTM p_leverageProjectTM)
    {
        m_projectTMsToLeverageFrom.add(p_leverageProjectTM);
    }

    public void setAllLeverageProjectTMs(
            Vector<LeverageProjectTM> p_projectTMsToLeverageFrom)
    {
        m_projectTMsToLeverageFrom = p_projectTMsToLeverageFrom;
    }

    public void setNewProjectTMs(Vector<LeverageProjectTM> p_newProjectTMs)
    {
        m_newProjectTMsToLeverageFrom = p_newProjectTMs;
    }

    public Vector<LeverageProjectTM> getNewProjectTMs()
    {
        return m_newProjectTMsToLeverageFrom;
    }

    public void setIsTypeSensitiveLeveraging(boolean p_isTypeSensitiveLeveraging)
    {
        m_isTypeSensitiveLeveraging = p_isTypeSensitiveLeveraging;
    }

    public void setTypeDifferencePenalty(long p_typeDifferencePenalty)
    {
        m_typeDifferencePenalty = p_typeDifferencePenalty;
    }

    public void setIsCaseSensitiveLeveraging(boolean p_isCaseSensitiveLeveraging)
    {
        m_isCaseSensitiveLeveraging = p_isCaseSensitiveLeveraging;
    }

    public void setCaseDifferencePenalty(long p_caseDifferencePenalty)
    {
        m_caseDifferencePenalty = p_caseDifferencePenalty;
    }

    public void setIsWhiteSpaceSensitiveLeveraging(
            boolean p_isWhiteSpaceSensitiveLeveraging)
    {
        m_isWhiteSpaceSensitiveLeveraging = p_isWhiteSpaceSensitiveLeveraging;
    }

    public void setWhiteSpaceDifferencePenalty(
            long p_whiteSpaceDifferencePenalty)
    {
        m_whiteSpaceDifferencePenalty = p_whiteSpaceDifferencePenalty;
    }

    public void setIsCodeSensitiveLeveraging(boolean p_isCodeSensitiveLeveraging)
    {
        m_isCodeSensitiveLeveraging = p_isCodeSensitiveLeveraging;
    }

    public void setCodeDifferencePenalty(long p_codeDifferencePenalty)
    {
        m_codeDifferencePenalty = p_codeDifferencePenalty;
    }

    public void setIsMultiLingualLeveraging(boolean p_isMultiLingualLeveraging)
    {
        m_isMultiLingualLeveraging = p_isMultiLingualLeveraging;
    }

    public void setMultipleExactMatches(String p_multipleExactMatches)
    {
        m_multipleExactMatches = p_multipleExactMatches;
    }

    public void setMultipleExactMatchPenalty(long p_multipleExactMatchesPenalty)
    {
        m_multipleExactMatchesPenalty = p_multipleExactMatchesPenalty;
    }

    public void setFuzzyMatchThreshold(long p_fuzzyMatchThreshold)
    {
        m_fuzzyMatchThreshold = p_fuzzyMatchThreshold;
    }

    public void setNumberOfMatchesReturned(long p_numberOfMatchesReturned)
    {
        m_numberOfMatchesReturned = p_numberOfMatchesReturned;
    }

    public void setIsLatestMatchForReimport(boolean p_isLatestMatchForReimport)
    {
        m_isLatestMatchForReimport = p_isLatestMatchForReimport;
    }

    public void setIsTypeSensitiveLeveragingForReimp(
            boolean p_isTypeSensitiveLeveragingForReimp)
    {
        m_isTypeSensitiveLeveragingForReimp = p_isTypeSensitiveLeveragingForReimp;
    }

    public void setTypeDifferencePenaltyForReimp(
            long p_typeDifferencePenaltyForReimp)
    {
        m_typeDifferencePenaltyForReimp = p_typeDifferencePenaltyForReimp;
    }

    public void setIsMultipleMatchesForReimp(boolean p_isMultipleMatchesForReimp)
    {
        m_isMultipleMatchesForReimp = p_isMultipleMatchesForReimp;
    }

    public void setMultipleMatchesPenalty(long p_multipleMatchesPenalty)
    {
        m_multipleMatchesPenalty = p_multipleMatchesPenalty;
    }

    public void setL10nProfile(L10nProfile p_l10nProfile)
    {
        m_l10nProfile = p_l10nProfile;
    }

    public String getName()
    {
        return m_name;
    }

    public String getDescription()
    {
        return m_description;
    }

    public long getProjectTmIdForSave()
    {
        return m_projectTmIdForSave;
    }

    public boolean isSaveUnLocSegToPageTM()
    {
        return m_isSaveUnLocSegToPageTM;
    }

    public boolean isSaveApprovedSegToProjectTM() {
    	return m_isSaveApprovedSegToProjectTM;
    }
    
    public boolean isSaveExactMatchSegToProjectTM() {
    	return m_isSaveExactMatchSegToProjectTM;
    }
    
    public boolean isSaveUnLocSegToProjectTM()
    {
        return m_isSaveUnLocSegToProjectTM;
    }
    
    public boolean isSaveWhollyInternalTextToProjectTM()
    {
        return m_saveWhollyInternalTextToProjectTM;
    }

    public Vector<String> getJobExcludeTuTypes()
    {
        return m_jobExcludeTuTypes;
    }

    /**
     * Used by TOPLink to populate the database with a string - pipe delimited
     */
    public String getJobExcludeTuTypesAsString()
    {
        StringBuffer result = new StringBuffer();

        for (Enumeration<String> enumeration = m_jobExcludeTuTypes.elements(); enumeration
                .hasMoreElements();)
        {
            result.append((String) enumeration.nextElement());
            result.append(EXCLUDE_TU_TYPE_DELIMITER);
        }

        return result.toString();
    }

    public boolean isLeverageLocalizable()
    {
        return m_isLeverageLocalizable;
    }

    public Vector<LeverageProjectTM> getProjectTMsToLeverageFrom()
    {
        return m_projectTMsToLeverageFrom;
    }

    public boolean isTypeSensitiveLeveraging()
    {
        return m_isTypeSensitiveLeveraging;
    }

    public long getTypeDifferencePenalty()
    {
        return m_typeDifferencePenalty;
    }

    public boolean isCaseSensitiveLeveraging()
    {
        return m_isCaseSensitiveLeveraging;
    }

    public long getCaseDifferencePenalty()
    {
        return m_caseDifferencePenalty;
    }

    public boolean isWhiteSpaceSensitiveLeveraging()
    {
        return m_isWhiteSpaceSensitiveLeveraging;
    }

    public long getWhiteSpaceDifferencePenalty()
    {
        return m_whiteSpaceDifferencePenalty;
    }

    public boolean isCodeSensitiveLeveraging()
    {
        return m_isCodeSensitiveLeveraging;
    }

    public long getCodeDifferencePenalty()
    {
        return m_codeDifferencePenalty;
    }

    public boolean isMultiLingualLeveraging()
    {
        return m_isMultiLingualLeveraging;
    }

    public String getMultipleExactMatches()
    {
        return m_multipleExactMatches;
    }

    public long getMultipleExactMatchesPenalty()
    {
        return m_multipleExactMatchesPenalty;
    }

    public long getFuzzyMatchThreshold()
    {
        return m_fuzzyMatchThreshold;
    }

    public long getNumberOfMatchesReturned()
    {
        return m_numberOfMatchesReturned;
    }

    public boolean isLatestMatchForReimport()
    {
        return m_isLatestMatchForReimport;
    }

    public boolean isTypeSensitiveLeveragingForReimp()
    {
        return m_isTypeSensitiveLeveragingForReimp;
    }

    public long getTypeDifferencePenaltyForReimp()
    {
        return m_typeDifferencePenaltyForReimp;
    }

    public boolean isMultipleMatchesForReimp()
    {
        return m_isMultipleMatchesForReimp;
    }

    public long getMultipleMatchesPenalty()
    {
        return m_multipleMatchesPenalty;
    }

    public L10nProfile getL10nProfile()
    {
        return m_l10nProfile;
    }

    public boolean getDynLevFromInProgressTm()
    {
        return m_dynLevFromInProgressTm;
    }

    public void setDynLevFromInProgressTm(boolean levFromInProgressTm)
    {
        m_dynLevFromInProgressTm = levFromInProgressTm;
    }
    
    public boolean getDynLevStopSearch()
    {
        return m_dynLevStopSearch;
    }

    public void setDynLevStopSearch(boolean dynLevStopSearch)
    {
        m_dynLevStopSearch = dynLevStopSearch;
    }

    public boolean getDynLevFromGoldTm()
    {
        return m_dynLevFromGoldTm;
    }

    public void setDynLevFromGoldTm(boolean levFromGoldTm)
    {
        m_dynLevFromGoldTm = levFromGoldTm;
    }

    public boolean getDynLevFromPopulationTm()
    {
        return m_dynLevFromPopulationTm;
    }

    public void setDynLevFromPopulationTm(boolean levFromPopulationTm)
    {
        m_dynLevFromPopulationTm = levFromPopulationTm;
    }

    public boolean getDynLevFromReferenceTm()
    {
        return m_dynLevFromReferenceTm;
    }

    public void setDynLevFromReferenceTm(boolean levFromReferenceTm)
    {
        m_dynLevFromReferenceTm = levFromReferenceTm;
    }
    
    // Below method for hibernate
    public void setJobExcludeTuTypesStr(String jobExcludeTuTypes)
    {
        setExcludeTuTypes(jobExcludeTuTypes);
    }

    public String getJobExcludeTuTypesStr()
    {
        return getJobExcludeTuTypesAsString();
    }

    public boolean getIsExactMatchLeveraging()
    {
        return m_isExactMatchLeveraging;
    }

    public void setIsExactMatchLeveraging(boolean exactMatchLeveraging)
    {
        m_isExactMatchLeveraging = exactMatchLeveraging;
    }

    public boolean getIsTypeSensitiveLeveraging()
    {
        return m_isTypeSensitiveLeveraging;
    }

    public boolean getIsCaseSensitiveLeveraging()
    {
        return m_isCaseSensitiveLeveraging;
    }

    public boolean getIsWhiteSpaceSensitiveLeveraging()
    {
        return m_isWhiteSpaceSensitiveLeveraging;
    }

    public boolean getIsCodeSensitiveLeveraging()
    {
        return m_isCodeSensitiveLeveraging;
    }

    public boolean getIsMultiLingualLeveraging()
    {
        return m_isMultiLingualLeveraging;
    }

    public void setMultipleExactMatchesPenalty(long exactMatchesPenalty)
    {
        m_multipleExactMatchesPenalty = exactMatchesPenalty;
    }

    public boolean getIsLatestMatchForReimport()
    {
        return m_isLatestMatchForReimport;
    }

    public boolean getIsTypeSensitiveLeveragingForReimp()
    {
        return m_isTypeSensitiveLeveragingForReimp;
    }

    public boolean getIsMultipleMatchesForReimp()
    {
        return m_isMultipleMatchesForReimp;
    }

    public Set<LeverageProjectTM> getProjectTMsToLeverageFromSet()
    {
        HashSet<LeverageProjectTM> set = new HashSet<LeverageProjectTM>();
        if (m_projectTMsToLeverageFrom != null)
        {
            set = new HashSet<LeverageProjectTM>(m_projectTMsToLeverageFrom);
        }
        return set;
    }

    public void setProjectTMsToLeverageFromSet(
            Set<LeverageProjectTM> projectTMsToLeverageFromSet)
    {
        setAllLeverageProjectTMs(new Vector<LeverageProjectTM>(
                projectTMsToLeverageFromSet));
    }

    public boolean getIsContextMatchLeveraging()
    {
        return m_isContextMatchLeveraging;
    }

    public void setIsContextMatchLeveraging(boolean contextMatchLeveraging)
    {
        m_isContextMatchLeveraging = contextMatchLeveraging;
    }

    public int getIcePromotionRules()
    {
		return this.icePromotionRules;
	}

	public void setIcePromotionRules(int icePromotionRules)
	{
		this.icePromotionRules = icePromotionRules;
	}

	public boolean isMatchPercentage()
    {
        return isMatchPercentage;
    }

    public void setMatchPercentage(boolean isMatchPercentage)
    {
        this.isMatchPercentage = isMatchPercentage;
    }

    public boolean isTmProcendence()
    {
        return isTmProcendence;
    }

    public void setTmProcendence(boolean isTmProcendence)
    {
        this.isTmProcendence = isTmProcendence;
    }

    public long getRefTmPenalty()
    {
        return m_refTmPenalty;
    }

    public void setRefTmPenalty(long tmPenalty)
    {
        m_refTmPenalty = tmPenalty;
    }

    // added 2

    public String getRefTMsToLeverageFrom()
    {
        return m_refTMsToLeverageFrom;
    }

    public void setRefTMsToLeverageFrom(String msRefToLeverageFrom)
    {
        m_refTMsToLeverageFrom = msRefToLeverageFrom;
    }

    public boolean getSelectRefTm()
    {
        return m_selectRefTm;
    }

    public void setSelectRefTm(boolean refTm)
    {
        m_selectRefTm = refTm;
    }


    public boolean tmOrderChanged()
    {
        if (m_newProjectTMsToLeverageFrom == null
                || m_projectTMsToLeverageFrom == null)
        {
            return false;
        }

        if (m_newProjectTMsToLeverageFrom.size() != m_projectTMsToLeverageFrom
                .size())
        {
            return true;
        }

        for (LeverageProjectTM newTm : (Vector<LeverageProjectTM>) m_newProjectTMsToLeverageFrom)
        {
            boolean found = false;
            for (LeverageProjectTM oldTm : (Vector<LeverageProjectTM>) m_projectTMsToLeverageFrom)
            {
                if (newTm.equals(oldTm))
                {
                    found = true;
                    break;
                }
            }

            if (!found)
            {
                return true;
            }
        }

        return false;
    }

    public boolean isAutoRepair()
    {
        return autoRepair;
    }

    public void setAutoRepair(boolean autoRepair)
    {
        this.autoRepair = autoRepair;
    }

    public int getMultipleExactMatcheMode()
    {
        int mode = 0;
        String modeString = this.getMultipleExactMatches();
        if (modeString.equals(TranslationMemoryProfile.LATEST_EXACT_MATCH))
        {
            mode = LeverageOptions.PICK_LATEST;
        }
        else if (modeString.equals(TranslationMemoryProfile.OLDEST_EXACT_MATCH))
        {
            mode = LeverageOptions.PICK_OLDEST;
        }
        else if (modeString
                .equals(TranslationMemoryProfile.DEMOTED_EXACT_MATCH))
        {
            mode = LeverageOptions.DEMOTE;
        }

        return mode;
    }

    public TDATM getTdatm()
    {
        return this.tdatm;
    }

    public void setTdatm(TDATM P_TDATM)
    {
        this.tdatm = P_TDATM;
    }
    
    public List<TMPAttribute> getAllTMPAttributes()
    {
        List<TMPAttribute> atts = new ArrayList<TMPAttribute>();
        Set<TMPAttribute> tmAtts = getAttributes();
        if (tmAtts != null)
        {
            atts.addAll(tmAtts);
        }

        return atts;
    }
    
    public List<String> getAllTMPAttributenames()
    {
        List<String> atts = new ArrayList<String>();
        Set<TMPAttribute> tmAtts = getAttributes();
        if (tmAtts != null)
        {
            Iterator<TMPAttribute> it = tmAtts.iterator();
            while(it.hasNext())
            {
                TMPAttribute tma = it.next();
                atts.add(tma.getAttributeName());
            }

            SortUtil.sort(atts);
        }

        return atts;
    }
    
    public Set<TMPAttribute> getAttributes()
    {
        return attributes;
    }

    public void setAttributes(Set<TMPAttribute> attributes)
    {
        this.attributes = attributes;
    }

    public long getCompanyId()
    {
        return m_companyId;
    }

    public void setCompanyId(long p_companyId)
    {
        m_companyId = p_companyId;
    }

    /**
	 * Get all reference TMs' names in order to display on TM profile main list
	 * UI for "Reference TM(s)" column.
	 */
    public String getProjectTMNamesToLeverageFrom()
    {
		if (this.m_projectTMsToLeverageFrom == null
				|| this.m_projectTMsToLeverageFrom.size() == 0) {
			return "";
		}

		// Get TreeMap<projectIndex, tmName> sorted by projectIndex.
		String tmName = null;
		TreeMap<Integer, String> lmIdTmNameMap = new TreeMap<Integer, String>();
		for (Iterator<LeverageProjectTM> it = this.m_projectTMsToLeverageFrom
				.iterator(); it.hasNext();)
		{
			LeverageProjectTM levTm = it.next();
			try
			{
				tmName = ServerProxy.getProjectHandler()
						.getProjectTMById(levTm.getProjectTmId(), false).getName();
				if (!StringUtil.isEmpty(tmName))
				{
					lmIdTmNameMap.put(levTm.getProjectTmIndex(), tmName);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		// Per TM name one line
		StringBuilder result = new StringBuilder();
		for(Integer projectIndex : lmIdTmNameMap.keySet())
		{
			tmName = lmIdTmNameMap.get(projectIndex);
	        if (result.length() > 0) {
				result.append("<br/>").append(tmName);
			} else {
				result.append(tmName);
			}
		}

		return result.toString();
    }

    public boolean isUniqueFromMultipleTranslation()
    {
        return uniqueFromMultipleTranslation;
    }

    public void setUniqueFromMultipleTranslation(
            boolean uniqueFromMultipleTranslation)
    {
        this.uniqueFromMultipleTranslation = uniqueFromMultipleTranslation;
    }

    public boolean isSaveLocSegToProjectTM()
    {
        return m_isSaveLocSegToProjectTM;
    }
    
    public void setSaveLocSegToProjectTM(boolean p_isSaveLocSegToProjectTM)
    {
        m_isSaveLocSegToProjectTM = p_isSaveLocSegToProjectTM;
    }

	public String getChoiceIfAttNotMatch() {
		return choiceIfAttNotMatch;
	}

	public void setChoiceIfAttNotMatch(String choiceIfAttNotMatch) {
		this.choiceIfAttNotMatch = choiceIfAttNotMatch;
	}

	public int getTuAttNotMatchPenalty() {
		return tuAttNotMatchPenalty;
	}

	public void setTuAttNotMatchPenalty(int tuAttNotMatchPenalty) {
		this.tuAttNotMatchPenalty = tuAttNotMatchPenalty;
	}
}
