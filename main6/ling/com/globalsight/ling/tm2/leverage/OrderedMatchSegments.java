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
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.log4j.Logger;

import com.globalsight.diplomat.util.XmlUtil;
import com.globalsight.ling.tm.LeveragingLocales;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.SortUtil;
import com.globalsight.util.StringUtil;
import com.globalsight.util.edit.GxmlUtil;

/**
 * OrderedMatchSegments stores leverage matches by target locales so we can
 * perform duplicates removals (yes, we still have duplicates, e.g. leveraging
 * from two Tms), apply options on multiple exact matches, etc.
 */

class OrderedMatchSegments
{
    private static Logger c_logger = Logger
            .getLogger(OrderedMatchSegments.class.getName());

    // repository of leverage matches.
    // key: target locale (GlobalSightLocale)
    // value: List of LeveragedTuv. LeveragedTuv's locale and the
    // locale of its key don't necessarily match. Because of the cross
    // locale leveraging, fr_CA target locale might have fr_FR
    // matches.
    private Map m_localeMatchMap = new HashMap();

    /**
     * Populate data from a given collection of LeveragedTus. While populating
     * data, it does: - align matches in cross locale targets - remove
     * duplicates - apply leverage options for multiple translations - assign
     * order
     * 
     * @param p_leveragedTus
     *            Collection of LeveragedTu to be populated in this object
     * @param p_leverageOptions
     *            LeverageOptions
     */
    void populate(Collection p_leveragedTus, LeverageOptions p_leverageOptions,
            long p_jobId)
    {
        LeveragingLocales leveragingLocales = p_leverageOptions
                .getLeveragingLocales();

        // build m_localeMatchMap
        buildLocaleMatchMap(leveragingLocales);

        // align matches to cross locale map
        alignMatchesForCrossLocales(leveragingLocales, p_leveragedTus);

        // remove duplicates
        removeDuplicates(p_leverageOptions, p_jobId);

        // apply leverage options for multiple translations
        applyMultiTransOption(p_leverageOptions, p_jobId);

        // assign order
        assignOrder(p_leverageOptions, p_jobId);

        if (c_logger.isDebugEnabled())
        {
            c_logger.debug("\nOrderedMatchSegments:\n" + toDebugString());            
        }
    }

    /**
     * Returns exact match of each target locales in ExactLeverageMatch object.
     * ExactLeverageMatch holds only one LeveragedTuv as an exact match for each
     * target locale. If there is no exact match for a locale, the locale won't
     * be added in ExactLeverageMatch.
     * 
     * @return ExactLeverageMatch object
     */
    ExactLeverageMatch getExactLeverageMatch()
    {
        ExactLeverageMatch exactLeverageMatch = new ExactLeverageMatch();
        Iterator itLocale = m_localeMatchMap.keySet().iterator();
        while (itLocale.hasNext())
        {
            GlobalSightLocale locale = (GlobalSightLocale) itLocale.next();
            List levTuvList = (List) m_localeMatchMap.get(locale);
            if (levTuvList.size() > 0)
            {
                LeveragedTuv levTuv = (LeveragedTuv) levTuvList.get(0);
                if (levTuv.getScore() == 100)
                {
                    exactLeverageMatch.add(levTuv, locale);
                }
            }
        }

        return exactLeverageMatch;
    }

    /**
     * Returns a Set of target locales that have an exact match.
     * 
     * @return Set of GlobalSightLocale objects which are target locales that
     *         have an exact match
     */
    Set getExactMatchLocales()
    {
        Set exactMatchLocales = new HashSet();
        Iterator itLocale = m_localeMatchMap.keySet().iterator();
        while (itLocale.hasNext())
        {
            GlobalSightLocale locale = (GlobalSightLocale) itLocale.next();
            List levTuvList = (List) m_localeMatchMap.get(locale);
            if (levTuvList.size() > 0)
            {
                LeveragedTuv levTuv = (LeveragedTuv) levTuvList.get(0);
                if (levTuv.getScore() == 100)
                {
                    exactMatchLocales.add(locale);
                }
            }
        }

        return exactMatchLocales;
    }

    /**
     * Returns an exact match as LeveragedTuv for a given locale.
     * 
     * @return exact match for a given locale. If there is no exact match for
     *         the locale, null is returned.
     */
    LeveragedTuv getExactLeverageMatch(GlobalSightLocale p_targetLocale)
    {
        LeveragedTuv levTuv = null;

        List levTuvList = (List) m_localeMatchMap.get(p_targetLocale);
        if (levTuvList.size() > 0)
        {
            levTuv = (LeveragedTuv) levTuvList.get(0);
            if (levTuv.getScore() != 100)
            {
                levTuv = null;
            }
        }

        return levTuv;
    }

    /**
     * Returns an Iterator that iterates all target locales. next() method of
     * this Iterator returns GlobalSightLocale object
     * 
     * @return Iterator
     */
    Iterator targetLocaleIterator()
    {
        return m_localeMatchMap.keySet().iterator();
    }

    /**
     * Returns an Iterator that iterates matches for a given target. The number
     * of iteration doesn't exceed the user specified number of returned
     * matches. next() method of this Iterator returns LeveragedTuv object.
     * 
     * @param p_targetLocale
     *            target locale
     * @param p_numReturned
     *            number of matches to be returned
     * @return Iterator
     */
    public Iterator matchIterator(GlobalSightLocale p_targetLocale,
            int p_numReturned, int p_threshold)
    {
        return new MatchIterator(p_targetLocale, p_numReturned, p_threshold);
    }

    // build m_localeMatchMap
    private void buildLocaleMatchMap(LeveragingLocales p_leveragingLocales)
    {
        Iterator itTargetLocales = p_leveragingLocales.getAllTargetLocales()
                .iterator();
        while (itTargetLocales.hasNext())
        {
            m_localeMatchMap.put(itTargetLocales.next(), new ArrayList());
        }
    }

    // align matches to cross locale map
    private void alignMatchesForCrossLocales(
            LeveragingLocales p_leveragingLocales, Collection p_leveragedTus)
    {
        // walk through each target locale
        Iterator itTargetLocale = m_localeMatchMap.keySet().iterator();
        while (itTargetLocale.hasNext())
        {
            GlobalSightLocale targetLocale = (GlobalSightLocale) itTargetLocale
                    .next();
            List leveragedTuvList = (List) m_localeMatchMap.get(targetLocale);

            // walk through each leveraging locale in a target locale
            Iterator itLeveragingLocale = p_leveragingLocales
                    .getLeveragingLocales(targetLocale).iterator();
            while (itLeveragingLocale.hasNext())
            {
                GlobalSightLocale leveragingLocale = (GlobalSightLocale) itLeveragingLocale
                        .next();

                // walk through each Tu in the Collection and put Tuvs
                // in a leveraging locale to m_localeMatchMap in place
                // of target locale
                Iterator itLeveragedTu = p_leveragedTus.iterator();
                while (itLeveragedTu.hasNext())
                {
                    LeveragedTu leverageTu = (LeveragedTu) itLeveragedTu.next();
                    Collection tuvList = leverageTu
                            .getTuvList(leveragingLocale);
                    if (tuvList != null)
                    {
                        // clone Tuvs and add them to the list
                        Iterator itTuvs = tuvList.iterator();
                        while (itTuvs.hasNext())
                        {
                            BaseTmTuv tuv = (BaseTmTuv) itTuvs.next();
                            leveragedTuvList.add(tuv.clone());
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Gets duplicate tuvs in leveragedTuvList.
     * 
     * @param leveragedTuvList
     * @return
     */
    private List<LeveragedTuv> getDuplicateTuv(
            List<LeveragedTuv> leveragedTuvList)
    {
        List<LeveragedTuv> removedTuv = new ArrayList<LeveragedTuv>();
        
        for (int i = 0; i < leveragedTuvList.size() - 1; i++)
        {
             LeveragedTuv tuv1 = (LeveragedTuv) leveragedTuvList.get(i);
             String sText1 = getSegmentText(tuv1.getSourceTuv().getSegment());
             String tText1 = getSegmentText(tuv1.getSegment());
             
             for (int j = i + 1; j < leveragedTuvList.size(); j++)
             {
                 LeveragedTuv tuv2 = (LeveragedTuv) leveragedTuvList.get(j);
                 
                 String sText2 = getSegmentText(tuv2.getSourceTuv().getSegment());
                 String tText2 = getSegmentText(tuv2.getSegment());
                 
				if (sText1.equals(sText2) && tText1.equals(tText2)
						&& tuv1.getPreviousHash() == tuv2.getPreviousHash()
						&& tuv1.getNextHash() == tuv1.getNextHash())
                 {
                     removedTuv.add(tuv2);
                 }
             }
        }
        
        return removedTuv;
    }
    
    /**
     * Can not use leveragedTuvList.removeAll(removedTuv). The equals method has
     * been rewrited.
     * 
     * @param leveragedTuvList
     * @param removedTuv
     */
    private void removeAll(List<LeveragedTuv> leveragedTuvList,
            List<LeveragedTuv> removedTuv)
    {
        for (LeveragedTuv aa : removedTuv)
        {
            for (int i = 0; i < leveragedTuvList.size(); i++)
            {
                LeveragedTuv aa2 = leveragedTuvList.get(i);
                if (aa2.getId() == aa.getId()
                        && aa2.getScore() == aa.getScore())
                {
                    leveragedTuvList.remove(i);
                    break;
                }
            }
        }
    }

    /**
     * Removes duplicates
     * @param p_leverageOptions
     * @param companyId
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void removeDuplicates(LeverageOptions p_leverageOptions,
            long p_jobId)
    {
        Iterator itLocale = m_localeMatchMap.keySet().iterator();
        while (itLocale.hasNext())
        {
            GlobalSightLocale targetLocale = (GlobalSightLocale) itLocale
                    .next();
            List<LeveragedTuv> leveragedTuvList = (List) m_localeMatchMap
                    .get(targetLocale);
            SortUtil.sort(leveragedTuvList, new AssignOrderComparator(
                    targetLocale, p_leverageOptions, p_jobId));

            List<LeveragedTuv> removedTuv = getDuplicateTuv(leveragedTuvList);
            removeAll(leveragedTuvList, removedTuv);
        }
    }

    /**
     * Get the actual plain text within tag '<segment...>...</segment>', and
     * unescape string to be compared with other segments
     * 
     * @param text
     *            Segment content
     * @return String Plain text with tags and being unescaped.
     */
    private String getSegmentText(String text)
    {
        if (StringUtil.isEmpty(text))
            return text;
        String segmentText = "";
        segmentText = GxmlUtil.stripRootTag(text);
        if (StringUtil.isNotEmpty(segmentText))
            segmentText = XmlUtil.unescapeString(segmentText);
        return segmentText;
    }

    // apply leverage options for multiple translations
    private void applyMultiTransOption(LeverageOptions p_leverageOptions,
            long p_jobId)
    {
        Iterator itLocale = m_localeMatchMap.keySet().iterator();
        while (itLocale.hasNext())
        {
            GlobalSightLocale targetLocale = (GlobalSightLocale) itLocale
                    .next();
            List leveragedTuvList = (List) m_localeMatchMap.get(targetLocale);

            // If the number of matches is only 1, no need to apply
            // the option.
            if (leveragedTuvList.size() < 2)
            {
                continue;
            }

            LeveragedTuv firstTuv = (LeveragedTuv) leveragedTuvList.get(0);
            LeveragedTuv secondTuv = (LeveragedTuv) leveragedTuvList.get(1);
            MatchState firstState = firstTuv.getMatchState();
            MatchState secondState = secondTuv.getMatchState();
            GlobalSightLocale firstLocale = firstTuv.getLocale();
            GlobalSightLocale secondLocale = secondTuv.getLocale();

            // Test if the first and second tuv match states are
            // PAGE_TM_EXACT_MATCH or SEGMENT_TM_EXACT_MATCH and both
            // of their locales are target locale or both are not. If
            // the above condition is satisfied, there are multiple
            // translation.
            if ((firstState.equals(MatchState.PAGE_TM_EXACT_MATCH) || firstState
                    .equals(MatchState.SEGMENT_TM_EXACT_MATCH))
                    && (secondState.equals(MatchState.PAGE_TM_EXACT_MATCH) || secondState
                            .equals(MatchState.SEGMENT_TM_EXACT_MATCH))
                    && (!(firstLocale.equals(targetLocale) ^ secondLocale
                            .equals(targetLocale))))
            {
            	if (LeverageUtil.getSidCompareRusult(firstTuv, p_jobId) == 1 
            			&& LeverageUtil.getSidCompareRusult(secondTuv, p_jobId) < 1)
            	{
            		continue;
            	}
                assignMultiTransStateAndScore(leveragedTuvList,
                        p_leverageOptions);
            }
        }
    }

    // assigns MULTIPLE_TRANSLATION state to multiple translation
    // Tuvs. The existence of multiple translation has been already
    // detected. The list of Tuvs has been sorted according to the
    // options the user specified. i.e. the latest modified Tuv first
    // if the user chose "latest" and so on.
    private void assignMultiTransStateAndScore(List p_leveragedTuvList,
            LeverageOptions p_leverageOptions)
    {
        LeveragedTuv firstTuv = (LeveragedTuv) p_leveragedTuvList.get(0);
        MatchState firstState = firstTuv.getMatchState();
        int penalty = 0;
        int mode = 0;
        firstTuv.setMatchState(MatchState.MULTIPLE_TRANSLATION);

        if (firstState.equals(MatchState.PAGE_TM_EXACT_MATCH))
        {
            penalty = p_leverageOptions.getMultiTransForReimportPenalty();

            if (!p_leverageOptions.isNoMultipleExactMatchesForReimport())
            {
                firstTuv.setScore(100 - penalty);
            }
        }
        else
        {
            penalty = p_leverageOptions.getMultiTransPenalty();
            mode = p_leverageOptions.getMultipleExactMatcheMode();

            if (mode == LeverageOptions.DEMOTE)
            {
                firstTuv.setScore(100 - penalty);
            }
        }

        for (int i = 1; i < p_leveragedTuvList.size(); i++)
        {
            LeveragedTuv tuv = (LeveragedTuv) p_leveragedTuvList.get(i);
            if (tuv.getScore() != 100)
            {
                break;
            }

            // Demote other 100% matches such as
            // TYPE_DIFFERENCE. Since multiple translations are better
            // matches, the other state of matches cannot be 100%.
            MatchState state = tuv.getMatchState();
            if ((!state.equals(MatchState.PAGE_TM_EXACT_MATCH))
                    && (!state.equals(MatchState.SEGMENT_TM_EXACT_MATCH)))
            {
                demoteExact(tuv, p_leverageOptions);
            }
            else
            {
                // For the multiple translation, only penalty the demote model.
                tuv.setMatchState(MatchState.MULTIPLE_TRANSLATION);
                if (mode == LeverageOptions.DEMOTE)
                {
                    tuv.setScore(100 - penalty);
                }
            }
        }
    }

    // Demote the score of a Tuv according to the options the user chose.
    private void demoteExact(LeveragedTuv p_tuv,
            LeverageOptions p_leverageOptions)
    {
        MatchState state = p_tuv.getMatchState();
        int penalty = 0;

        if (state.equals(MatchState.TYPE_DIFFERENT))
        {
            if (((LeveragedTu) p_tuv.getTu()).getMatchTableType() == LeveragedTu.PAGE_TM)
            {
                penalty = p_leverageOptions
                        .getTypeDifferencePenaltyForReimport();
            }
            else
            {
                penalty = p_leverageOptions.getTypeDifferencePenalty();
            }
        }
        else if (state.equals(MatchState.CASE_DIFFERENT))
        {
            penalty = p_leverageOptions.getCaseDifferencePenalty();
        }
        else if (state.equals(MatchState.WHITESPACE_DIFFERENT))
        {
            penalty = p_leverageOptions.getWhiteSpaceDifferencePenalty();
        }
        else if (state.equals(MatchState.CODE_DIFFERENT))
        {
            penalty = p_leverageOptions.getCodeDifferencePenalty();
        }

        p_tuv.setScore(100 - penalty);
    }

    // assign order
    private void assignOrder(LeverageOptions options, long p_jobId)
    {
        Iterator itLocale = m_localeMatchMap.keySet().iterator();
        while (itLocale.hasNext())
        {
            GlobalSightLocale targetLocale = (GlobalSightLocale) itLocale
                    .next();
            List leveragedTuvList = (List) m_localeMatchMap.get(targetLocale);

            SortUtil.sort(leveragedTuvList, new AssignOrderComparator(
                    targetLocale, options, p_jobId));

            for (int i = 0; i < leveragedTuvList.size(); i++)
            {
                LeveragedTuv leveragedTuv = (LeveragedTuv) leveragedTuvList
                        .get(i);
                leveragedTuv.setOrder(i + 1);
            }
        }
    }

    public String toDebugString()
    {
        return m_localeMatchMap.toString();
    }

    // Iterator that iterates matches for a given target locale. The
    // number of iteration is restricted by a number given to the
    // constructor. next() method of this Iterator returns
    // LeveragedTuv object.
    private class MatchIterator implements Iterator
    {
        private List m_matchList = null;
        private int m_currentId;
        private int m_maxNum;
        private int m_threshold;

        private MatchIterator(GlobalSightLocale p_targetLocale,
                int p_numReturned, int p_threshold)
        {
            m_matchList = (List) m_localeMatchMap.get(p_targetLocale);
            m_currentId = 0;
            m_maxNum = p_numReturned;
            m_threshold = p_threshold;
        }

        public boolean hasNext()
        {
            boolean hasNext = false;

            if (m_matchList != null
                    && m_matchList.size() > m_currentId
                    && m_currentId < m_maxNum
                    && m_threshold <= ((LeveragedTuv) m_matchList
                            .get(m_currentId)).getScore())
            {
                hasNext = true;
            }

            return hasNext;
        }

        public Object next()
        {
            Object o = null;
            if (hasNext())
            {
                o = m_matchList.get(m_currentId);
                m_currentId++;
            }
            else
            {
                throw new NoSuchElementException();
            }
            return o;
        }

        public void remove()
        {
            // it shouldn't be called.
        }
    }

    private abstract class LeveragedTuvComparator implements Comparator
    {
        GlobalSightLocale m_targetLocale;
        LeverageOptions options;
        long jobId = -1;

        LeveragedTuvComparator(GlobalSightLocale p_targetLocale,
                LeverageOptions options, long jobId)
        {
            m_targetLocale = p_targetLocale;
            this.options = options;
            this.jobId = jobId;
        }

        // This method compares the match states of the two Tuvs. It helps
        // sort better match state first
        int compareMatchState(LeveragedTuv p_tuv1, LeveragedTuv p_tuv2)
        {
            int stateVal1 = p_tuv1.getMatchState().getCompareKey();
            int stateVal2 = p_tuv2.getMatchState().getCompareKey();
            return stateVal1 - stateVal2;
        }

        // This method compares the target locale and the locales of
        // the given two Tuvs. It helps sort target locale Tuv
        // first. Cross locale leveraging brings non-target locale
        // Tuvs to the leverage results.
        int compareLocale(LeveragedTuv p_tuv1, LeveragedTuv p_tuv2)
        {
            GlobalSightLocale locale1 = p_tuv1.getLocale();
            GlobalSightLocale locale2 = p_tuv2.getLocale();

            int cmp1 = locale1.equals(m_targetLocale) ? 0 : 1;
            int cmp2 = locale2.equals(m_targetLocale) ? 0 : 1;

            return cmp1 - cmp2;
        }
        
        int compareTm(LeveragedTuv tuv1, LeveragedTuv tuv2)
        {
        	if (!options.isTmProcedence())
            {
        		return 0;
            }
        	
            long tmId1 = tuv1.getTu().getTmId();
            long tmId2 = tuv2.getTu().getTmId();
            int projectTmIndex1 = Leverager.getProjectTmIndex(options, tmId1);
            int projectTmIndex2 = Leverager.getProjectTmIndex(options, tmId2);
            
            return projectTmIndex1 - projectTmIndex2;
        }

        // // This method compares the last modified time of the two
        // // Tuvs. The order of the sort depends on the option the user
        // // chose.
        // int compareTime(LeveragedTuv p_tuv1, LeveragedTuv p_tuv2,
        // LeverageOptions p_leverageOptions)
        // {
        // Timestamp time1 = p_tuv1.getModifyDate();
        // Timestamp time2 = p_tuv2.getModifyDate();
        //
        // int result = time2.compareTo(time1);
        //
        // int mode;
        // if (p_leverageOptions.isLatestLeveragingForReimport())
        // {
        // mode = LeverageOptions.PICK_LATEST;
        // }
        // else
        // {
        // mode = p_leverageOptions.getMultipleExactMatcheMode();
        // }
        //
        // if (mode == LeverageOptions.PICK_OLDEST)
        // {
        // result = -result;
        // }
        //
        // return result;
        // }

    }

    private class AssignOrderComparator extends LeveragedTuvComparator
    {
        AssignOrderComparator(GlobalSightLocale p_targetLocale,
                LeverageOptions options, long jobId)
        {
            super(p_targetLocale, options, jobId);
        }

        public int compare(Object p_o1, Object p_o2)
        {
            LeveragedTuv tuv1 = (LeveragedTuv) p_o1;
            LeveragedTuv tuv2 = (LeveragedTuv) p_o2;

            float score1 = tuv1.getScore();
            float score2 = tuv2.getScore();

            long tmId1 = tuv1.getTu().getTmId();
            long tmId2 = tuv2.getTu().getTmId();
            int projectTmIndex1 = Leverager.getProjectTmIndex(options, tmId1);
            int projectTmIndex2 = Leverager.getProjectTmIndex(options, tmId2);
            int result = 0;
            result = (int) (score2 - score1);
            if (result != 0)
            {
                return result;
            }

            if (score1 == 100)
            {
                result = LeverageUtil.compareSid(tuv1, tuv2, jobId);
                if (result != 0)
                {
                    return result;
                }
            }

            if (tuv1.getPreviousHash() != -1 && tuv2.getPreviousHash() != -1)
            {
                result = (int) (tuv2.getPreviousHash() - tuv1.getPreviousHash());
                if (result != 0)
                {
                    return result;
                }
            }

            if (tuv1.getNextHash() != -1 && tuv2.getNextHash() != -1)
            {
                result = (int) (tuv2.getNextHash() - tuv1.getNextHash());
                if (result != 0)
                {
                    return result;
                }
            }

            result = projectTmIndex1 - projectTmIndex2;
            if (result != 0)
            {
            	return result;
            }

            // Ok, same score. Compare match state.
            result = compareMatchState(tuv1, tuv2);
            if (result != 0)
            {
                return result;
            }

            // Ok, same match state. Compare locale
            result = compareLocale(tuv1, tuv2);
            if (result != 0)
            {
                return result;
            }

            result = LeverageUtil.compareLastUsageDate(tuv1, tuv2, options);
            if (result != 0)
            {
                return result;
            }

            result = LeverageUtil.compareModifyTime(tuv1, tuv2, options);
            if (result != 0)
            {
                return result;
            }

            result = tuv1.getOrder() - tuv2.getOrder();

            return result;
        }
    }
}
