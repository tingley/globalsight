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
package com.globalsight.ling.tm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.SortUtil;

/**
 * TM hits (leveraged matches) for an original source tuv.
 * 
 * Please add documentation of how the matches are re-ordered and how their
 * state is set. I think a condition is that there can only be one (LG-) exact
 * match per locale, and all other matches must be demoted to fuzzy. Please
 * explain why this is.
 */
class LeverageMatchesPerTuv
{
    private static final Logger CATEGORY = Logger
            .getLogger(LeverageMatchesPerTuv.class);

    // tag aligner
    private SegmentTagsAligner m_tagAligner = new SegmentTagsAligner();

    // key: GlobalSightLocale
    // value: LeverageMatches
    private Map m_lmLocaleMap = new HashMap();

    /**
     * Add a CandidateMatch. All CandidateMatches to add must have the same
     * original source tuv id.
     */
    public void add(CandidateMatch p_match)
    {
        GlobalSightLocale locale = p_match.getTargetGlobalSightLocale();

        LeverageMatches matches = (LeverageMatches) m_lmLocaleMap.get(locale);

        if (matches == null)
        {
            matches = new LeverageMatches(locale, new ArrayList());

            m_lmLocaleMap.put(locale, matches);
        }

        matches.add(p_match);
    }

    /**
     * Get LeverageMatches by locale.
     * 
     * @param p_locale
     *            GlobalSightLocale
     * @return LeverageMatches. Can be null.
     */
    public LeverageMatches get(GlobalSightLocale p_locale)
    {
        return (LeverageMatches) m_lmLocaleMap.get(p_locale);
    }

    /**
     * Get unmodifiable Set of target locales stored in the object.
     * 
     * @return Unmodifiable Set of target locales. Null won't be returned.
     */
    public Set getLocales()
    {
        return Collections.unmodifiableSet(m_lmLocaleMap.keySet());
    }

    /**
     * Get unmodifiable Collection of all LeverageMatches stored in the object.
     * 
     * @return Unmodifiable Collection of all LeverageMatches. Null won't be
     *         returned.
     */
    public Collection getLeverageMatches()
    {
        return Collections.unmodifiableCollection(m_lmLocaleMap.values());
    }

    /**
     * Remove a mapping specified by a locale.
     * 
     * @param p_locale
     *            GlobalSightLocale
     */
    public void remove(GlobalSightLocale p_locale)
    {
        m_lmLocaleMap.remove(p_locale);
    }

    /**
     * Remove mappings specified by a list of locales.
     * 
     * @param p_locales
     *            a list of GlobalSightLocales
     */
    public void remove(Collection p_locales)
    {
        Iterator it = p_locales.iterator();

        while (it.hasNext())
        {
            m_lmLocaleMap.remove((GlobalSightLocale) it.next());
        }
    }

    /**
     * Merge two LeverageMatchesPerTuv objects.
     * 
     * @param p_other
     *            LeverageMatchesPerTuv
     */
    public void merge(LeverageMatchesPerTuv p_other)
    {
        Iterator localeIt = p_other.getLocales().iterator();

        while (localeIt.hasNext())
        {
            GlobalSightLocale localeInOther = (GlobalSightLocale) localeIt
                    .next();

            LeverageMatches lmSelf = get(localeInOther);

            if (lmSelf == null)
            {
                m_lmLocaleMap.put(localeInOther, p_other.get(localeInOther));
            }
            else
            {
                lmSelf.combine(p_other.get(localeInOther));
            }
        }
    }

    /**
     * Clear all contents
     */
    public void clear()
    {
        m_lmLocaleMap.clear();
    }

    /**
     * Test if there are any matches.
     */
    public boolean hasMatches()
    {
        return !m_lmLocaleMap.isEmpty();
    }

    /**
     * Regroup matches from "grouped by leveraging locales" to
     * "grouped by target locales".
     * 
     * @param p_leveragingLocales
     *            LeveragingLocales object
     */
    public void regroupLeveragingLocales(LeveragingLocales p_leveragingLocales)
    {
        Map newMap = new HashMap(m_lmLocaleMap.size());

        Iterator targetLocaleIt = p_leveragingLocales.getAllTargetLocales()
                .iterator();

        // iterate through target locales
        while (targetLocaleIt.hasNext())
        {
            GlobalSightLocale targetLocale = (GlobalSightLocale) targetLocaleIt
                    .next();

            LeverageMatches targetLm = get(targetLocale);

            // there may not be a match for the target locale
            if (targetLm == null)
            {
                targetLm = new LeverageMatches(targetLocale, new ArrayList());
            }

            // create a new LeverageMatches for the target
            LeverageMatches newTargetLm = new LeverageMatches(targetLocale,
                    targetLm.getLeverageMatches());

            Iterator leveragingLocaleIt = p_leveragingLocales
                    .getLeveragingLocales(targetLocale).iterator();

            // iterate through the leveraging locales
            while (leveragingLocaleIt.hasNext())
            {
                GlobalSightLocale leveragingLocale = (GlobalSightLocale) leveragingLocaleIt
                        .next();

                if (!leveragingLocale.equals(targetLocale))
                {
                    LeverageMatches leveragingLm = get(leveragingLocale);

                    if (leveragingLm != null)
                    {
                        newTargetLm.combine(leveragingLm);
                    }
                }
            }

            // put a combined matches to a new map
            if (newTargetLm.getLeverageMatches().size() > 0)
            {
                newMap.put(targetLocale, newTargetLm);
            }
        }

        // replace Locale/LeverageMatches map with the new one
        m_lmLocaleMap = newMap;
    }

    /**
     * Remove LGEM matches from the object, record the LGEM match in p_hitResult
     * and return the collection of found LGEMs.
     * 
     * @return collection of LeverageMatches
     */
    public Collection removeLgemMatches(LgemHitsResult p_hitResult)
    {
        Collection result = new ArrayList();
        Iterator lmIt = m_lmLocaleMap.values().iterator();

        while (lmIt.hasNext())
        {
            LeverageMatches lm = (LeverageMatches) lmIt.next();
            List candidateMatches = lm.getLeverageMatches();

            if (candidateMatches.size() >= 1)
            {
                CandidateMatch cm = (CandidateMatch) candidateMatches.get(0);

                if (cm.getMatchType() == LeverageMatchType.LEVERAGE_GROUP_EXACT_MATCH)
                {
                    result.add(lm);

                    p_hitResult.foundMatch(new Long(cm.getOriginalSourceId()),
                            lm.getGlobalSightLocale());

                    // Discard all other demoted exact and fuzzy matches.
                    // NOTE: see clearFuzziesIfLatestExact().
                    // NOTE: BasePostProcessor.getCandidateMatchList()
                    // expects the list to be removed.
                    lmIt.remove();
                }
            }
            else if (candidateMatches.size() == 0)
            {
                lmIt.remove();
            }
        }

        return result;
    }

    /**
     * Removes the first exact match from the object, records the match in
     * p_hitResult and returns the collection of found exact matches.
     * 
     * @return collection of LeverageMatches
     */
    public Collection removeLatestExactMatches(LgemHitsResult p_hitResult)
    {
        Collection result = new ArrayList();
        Iterator lmIt = m_lmLocaleMap.values().iterator();

        while (lmIt.hasNext())
        {
            LeverageMatches lm = (LeverageMatches) lmIt.next();
            List candidateMatches = lm.getLeverageMatches();

            if (candidateMatches.size() >= 1)
            {
                CandidateMatch cm = (CandidateMatch) candidateMatches.get(0);

                if (cm.getMatchType() == LeverageMatchType.EXACT_MATCH)
                {
                    result.add(lm);

                    p_hitResult.foundMatch(new Long(cm.getOriginalSourceId()),
                            lm.getGlobalSightLocale());

                    // Discard all other demoted exact and fuzzy matches.
                    // NOTE: see clearFuzziesIfLatestExact()
                    // NOTE: BasePostProcessor.getCandidateMatchList()
                    // expects the list to be removed.
                    lmIt.remove();
                }
            }
            else if (candidateMatches.size() == 0)
            {
                lmIt.remove();
            }
        }

        return result;
    }

    /**
     * Change 100% fuzzy match to exact match
     */
    public void change100PercentFuzzyToExact(BaseTmTuv p_originalSourceTuv)
            throws LingManagerException
    {
        Iterator lmIt = m_lmLocaleMap.values().iterator();
        while (lmIt.hasNext())
        {
            LeverageMatches lm = (LeverageMatches) lmIt.next();
            Iterator cmIt = lm.getLeverageMatches().iterator();
            while (cmIt.hasNext())
            {
                CandidateMatch candidateMatch = (CandidateMatch) cmIt.next();

                if (candidateMatch.getScoreNum() >= 100)
                {
                    if (candidateMatch.sameMatchedSource(p_originalSourceTuv
                            .getExactMatchFormat()))
                    {
                        candidateMatch
                                .setMatchType(LeverageMatchType.EXACT_MATCH);
                    }
                }
            }
        }
    }

    /**
     * Initial exact matches are based on CRC match only - we could have false
     * hits; remove them here.
     * 
     * @param p_sourceTuv
     *            Original source TuvLing
     */
    public void removeFalseHits(BaseTmTuv p_sourceTuv)
            throws LingManagerException
    {
        Iterator lmIt = m_lmLocaleMap.values().iterator();

        while (lmIt.hasNext())
        {
            Iterator cmIt = ((LeverageMatches) lmIt.next())
                    .getLeverageMatches().iterator();

            while (cmIt.hasNext())
            {
                CandidateMatch match = (CandidateMatch) cmIt.next();

                // byte compare on native format (e.g., HTML, XML, RTF etc..)
                if (!p_sourceTuv.getExactMatchFormat().equals(
                        match.getSourceExactMatchFormat()))
                {
                    // remove current element - it was a false hit
                    cmIt.remove();
                }
            }
        }
    }

    /**
     * If we have true duplicates (same source AND target) then remove them
     * here.
     * 
     * Does re-sort the matches by source/target string, match type and target
     * locale.
     */
    public void removeDuplicates() throws LingManagerException
    {
        Iterator lmIt = m_lmLocaleMap.values().iterator();
        while (lmIt.hasNext())
        {
            LeverageMatches lm = (LeverageMatches) lmIt.next();
            List candidateMatches = lm.getLeverageMatches();

            Comparator comparator = new CandidateMatchComparator(lm.getLocale());

            SortUtil.sort(candidateMatches, comparator);

            ListIterator cmIt = candidateMatches.listIterator();
            while (cmIt.hasNext())
            {
                CandidateMatch currentMatch = (CandidateMatch) cmIt.next();

                while (cmIt.hasNext())
                {
                    CandidateMatch nextMatch = (CandidateMatch) cmIt.next();

                    if (currentMatch.equals(nextMatch))
                    {
                        cmIt.remove();
                    }
                    else
                    {
                        cmIt.previous();
                        break;
                    }
                }
            }
        }
    }

    /**
     * If we have true duplicates (same source AND target) then remove the older
     * dup here and keep the latest alive.
     * 
     * Does re-sort the matches by source_target string, match type, target
     * locale and timestamp.
     */
    public void removeOlderDuplicates() throws LingManagerException
    {
        Iterator lmIt = m_lmLocaleMap.values().iterator();
        while (lmIt.hasNext())
        {
            LeverageMatches lm = (LeverageMatches) lmIt.next();
            List candidateMatches = lm.getLeverageMatches();

            Comparator comparator = new CandidateMatchSegmentTimeComparator(
                    lm.getLocale());

            SortUtil.sort(candidateMatches, comparator);

            ListIterator cmIt = candidateMatches.listIterator();
            while (cmIt.hasNext())
            {
                CandidateMatch currentMatch = (CandidateMatch) cmIt.next();

                while (cmIt.hasNext())
                {
                    CandidateMatch nextMatch = (CandidateMatch) cmIt.next();

                    if (currentMatch.equals(nextMatch))
                    {
                        cmIt.remove();
                    }
                    else
                    {
                        cmIt.previous();
                        break;
                    }
                }
            }
        }
    }

    /**
     * Demote matches if there are multiple exact matches. The score of demoted
     * fuzzy match is 99%. The algorithm to demote exact matches are as follows.
     * 
     * a) When there is only one LGEM match, that's the only match we get. We
     * don't try to look for more matches.
     * 
     * b) If there are no or more than one LGEM matches:
     * 
     * i) If there is only one "target locale" (versus "leveraging locale")
     * exact match, it stays as an exact match and the rest of exacts are
     * demoted to fuzzy.
     * 
     * ii) If there are no or more than one "target locale" matches, all of
     * exacts are demoted to fuzzy.
     */
    public void demoteExactMatches() throws LingManagerException
    {
        Iterator lmIt = m_lmLocaleMap.values().iterator();
        while (lmIt.hasNext())
        {
            LeverageMatches lm = (LeverageMatches) lmIt.next();
            List candidateMatches = lm.getLeverageMatches();

            Comparator comparator = new CandidateMatchSourceComparator(
                    lm.getLocale());

            SortUtil.sort(candidateMatches, comparator);

            demoteDuplicateExacts(candidateMatches, lm.getLocale());
        }
    }

    /**
     * Demotes matches if there are multiple exact matches, but leaves the first
     * exact match intact.
     * 
     * Does re-sort the matches by type, target locale and timestamp (latest
     * first).
     */
    public void demoteAllButLatestExactMatches() throws LingManagerException
    {
        for (Iterator it = m_lmLocaleMap.values().iterator(); it.hasNext();)
        {
            LeverageMatches lm = (LeverageMatches) it.next();
            List matches = lm.getLeverageMatches();

            if (matches.size() <= 1)
            {
                // too small amount of matches to sort or demote
                continue;
            }

            Comparator comparator = new CandidateMatchTypeTimeComparator(
                    lm.getLocale());

            SortUtil.sort(matches, comparator);

            Iterator it1 = matches.iterator();
            // whatever the match type of the first match is, just
            // demote everything else (no-op for non-exact matches)
            it1.next();
            demoteAllExacts(it1);
        }
    }

    /**
     * Demotes exact matches if the tags in source and target are different, or
     * a target TUV is not complete. Also normalizes fuzzy match scores to
     * max(score, 99).
     * 
     * Does not re-sort the matches.
     */
    public void assignLeverageMatchTypes() throws LingManagerException
    {
        Iterator lmIt = m_lmLocaleMap.values().iterator();
        while (lmIt.hasNext())
        {
            LeverageMatches lm = (LeverageMatches) lmIt.next();
            List candidateMatches = lm.getLeverageMatches();

            assignLeverageMatchTypes(candidateMatches);
        }
    }

    /**
     * Clears fuzzy matches if an exact match exists.
     * 
     * Does re-sort the matches by match type and target locale.
     */
    public void clearFuzziesIfExact() throws LingManagerException
    {
        Iterator lmIt = m_lmLocaleMap.values().iterator();
        while (lmIt.hasNext())
        {
            LeverageMatches lm = (LeverageMatches) lmIt.next();
            List candidateMatches = lm.getLeverageMatches();

            if (candidateMatches.size() <= 1)
            {
                continue;
            }

            Comparator comparator = new CandidateMatchPriorityComparator(
                    lm.getLocale());

            SortUtil.sort(candidateMatches, comparator);

            CandidateMatch cm = (CandidateMatch) candidateMatches.get(0);
            if (cm.isExactMatch(false)) // DEMOTED is not included
            {
                candidateMatches.clear();
                candidateMatches.add(cm);
            }
        }
    }

    /**
     * Clears fuzzy matches if a latest exact match exists. Keeps the requested
     * number of matches alive. If no exact match exists at all, all matches are
     * kept.
     * 
     * Does re-sort the matches by match type and timestamp.
     */
    public void clearFuzziesIfLatestExact(int p_keepAlive)
            throws LingManagerException
    {
        Iterator lmIt = m_lmLocaleMap.values().iterator();
        while (lmIt.hasNext())
        {
            LeverageMatches lm = (LeverageMatches) lmIt.next();
            ArrayList candidateMatches = lm.getLeverageMatches();

            if (candidateMatches.size() <= 1)
            {
                continue;
            }

            Comparator comparator = new CandidateMatchTypeTimeComparator(
                    lm.getLocale());

            SortUtil.sort(candidateMatches, comparator);

            // If there is an exact match, remove the other fuzzy
            // matches but keep the requested number alive.
            CandidateMatch cm = (CandidateMatch) candidateMatches.get(0);
            if (cm.isExactMatch(false)) // DEMOTED is not included
            {
                if (p_keepAlive < 1)
                    p_keepAlive = 1;

                for (int i = candidateMatches.size(); i > p_keepAlive; --i)
                {
                    candidateMatches.remove(i - 1);
                }
            }
        }
    }

    /**
     * Assigns order number to the matches.
     * 
     * Does re-sort the matches. There is no guarantee that a prior process has
     * sorted matches.
     */
    public void assignOrderNum() throws LingManagerException
    {
        for (Iterator it = m_lmLocaleMap.values().iterator(); it.hasNext();)
        {
            LeverageMatches lm = (LeverageMatches) it.next();
            List candidateMatches = lm.getLeverageMatches();

            if (candidateMatches.size() > 1)
            {
                Comparator comparator = new CandidateMatchPriorityComparator(
                        lm.getLocale());
                SortUtil.sort(candidateMatches, comparator);
            }

            short order = 1;
            for (Iterator it1 = candidateMatches.iterator(); it1.hasNext();)
            {
                CandidateMatch cm = (CandidateMatch) it1.next();
                cm.setOrderNum(order++);
            }
        }
    }

    /**
     * Returns a Collection of CandidateMatches of exact match
     */
    public Collection getExactMatches() throws LingManagerException
    {
        Collection exactMatches = new ArrayList(m_lmLocaleMap.size());

        for (Iterator lmIt = m_lmLocaleMap.values().iterator(); lmIt.hasNext();)
        {
            LeverageMatches lm = (LeverageMatches) lmIt.next();
            List candidateMatches = lm.getLeverageMatches();

            for (Iterator cmIt = candidateMatches.iterator(); cmIt.hasNext();)
            {
                CandidateMatch cm = (CandidateMatch) cmIt.next();
                if (cm.isExactMatch(false)) // DEMOTED is not included
                {
                    // When the match is found in one of leveraging
                    // locale instead of the target locale, create a
                    // clone of the CandidateMatch and set the target
                    // locale
                    if (!cm.getTargetGlobalSightLocale().equals(
                            lm.getGlobalSightLocale()))
                    {
                        cm = (CandidateMatch) cm.clone();
                        cm.setTargetLocale(lm.getGlobalSightLocale());
                    }
                    exactMatches.add(cm);

                    // it it guaranteed that there is at most only one
                    // exact match per TUV per locale
                    break;
                }
            }
        }
        return exactMatches;
    }

    /**
     * Demote matches to 99% fuzzy when the matched target state is not
     * complete. This can be used as a separate step in the leveraging
     * algorithm.
     */
    public void demoteIncompleteMatches() throws LingManagerException
    {
        Iterator lmIt = m_lmLocaleMap.values().iterator();
        while (lmIt.hasNext())
        {
            LeverageMatches lm = (LeverageMatches) lmIt.next();
            Iterator cmIt = lm.getLeverageMatches().iterator();
            while (cmIt.hasNext())
            {
                CandidateMatch cm = (CandidateMatch) cmIt.next();

                if (cm.isExactMatch(false) && !cm.isCompleted())
                {
                    cm.demoteToFuzzy();
                }
            }
        }
    }

    /**
     * Demotes exact matches if the tags in source and target are different, or
     * a target TUV is not complete. Also normalizes fuzzy match scores to
     * max(score, 99).
     * 
     * Does not re-sort the matches.
     */
    private void assignLeverageMatchTypes(List p_candidateMatches)
            throws LingManagerException
    {
        Iterator cmIt = p_candidateMatches.iterator();
        while (cmIt.hasNext())
        {
            CandidateMatch candidate = (CandidateMatch) cmIt.next();
            int type = candidate.getMatchType();

            if (type == LeverageMatchType.EXACT_MATCH
                    || type == LeverageMatchType.LEVERAGE_GROUP_EXACT_MATCH)
            {
                String source = candidate.getGxmlSource();
                String target = candidate.getGxmlTarget();

                // Demote if tags are different in source and target
                if (!areGxmlTagsTheSame(source, target))
                {
                    candidate.demoteToFuzzy();
                }

                // Demote if target TUV's state is not COMPLETE
                if (!candidate.isCompleted())
                {
                    candidate.demoteToFuzzy();
                }
            }
            else if (candidate.getMatchType() == LeverageMatchType.FUZZY_MATCH)
            {
                // must be a text only match
                if (candidate.getScoreNum() >= 100)
                {
                    candidate.setScoreNum((short) 99);
                }
            }
        }
    }

    /**
     * Compare the GXML tags (disregarding addable tags, attribute order and i &
     * x attributes) between source and target. Return true if tags are the
     * same, otherwise false.
     */
    private boolean areGxmlTagsTheSame(String p_source, String p_target)
            throws LingManagerException
    {
        String result = null;

        result = m_tagAligner.alignTags(p_source, p_target);

        if (result == null || m_tagAligner.isDifferingX())
        {
            return false;
        }

        return true;
    }

    private void demoteDuplicateExacts(List p_candidateMatches,
            Locale p_targetLocale)
    {
        Iterator it = p_candidateMatches.iterator();
        CandidateMatch cm1 = null;
        CandidateMatch cm2 = null;

        if (it.hasNext())
        {
            cm1 = (CandidateMatch) it.next();
        }
        if (it.hasNext())
        {
            cm2 = (CandidateMatch) it.next();
        }
        if (cm1 == null || cm2 == null)
        {
            // too small amount of matches to demote
            return;
        }

        // no need to demote if the second match is not exact
        if (!cm2.isExactMatch(false))
        {
            return;
        }

        // refresh iterator for demotion
        it = p_candidateMatches.iterator();

        if (cm1.isLeverageGroupExactMatch())
        {
            if (cm2.isLeverageGroupExactMatch())
            {
                // both matches are LGEM: demote all exacts
                demoteAllExacts(it);
            }
            else
            {
                // don't demote the first LGEM
                it.next();
                demoteAllExacts(it);
            }
        }
        else if (cm1.isExactMatch(false))
        {
            // the second match must be exact. (see above)
            if (cm1.getTargetLocale().equals(p_targetLocale))
            {
                if (cm2.getTargetLocale().equals(p_targetLocale))
                {
                    // both matches are in target locale:
                    // demote all exacts
                    demoteAllExacts(it);
                }
                else
                {
                    // the first is in target locale and the second is
                    // not: don't demote the first EXACT
                    it.next();
                    demoteAllExacts(it);
                }
            }
            else
            {
                // both matches are in leveraging locales:
                // demote all exacts
                demoteAllExacts(it);
            }
        }
    }

    private void demoteAllExacts(Iterator p_it)
    {
        while (p_it.hasNext())
        {
            CandidateMatch match = (CandidateMatch) p_it.next();

            if (match.isExactMatch(false))
            {
                match.demoteToFuzzy();
            }
            else
            {
                // the rest in the list are not exact as it's sorted so
                break;
            }
        }
    }

    public String toString()
    {
        StringBuffer result = new StringBuffer();

        Iterator lmIt = m_lmLocaleMap.values().iterator();

        result.append("Matches for TUV...\n");

        while (lmIt.hasNext())
        {
            LeverageMatches lm = (LeverageMatches) lmIt.next();
            Iterator cmIt = lm.getLeverageMatches().iterator();

            result.append("  Matches for locale " + lm.getLocale() + "\n");

            int i = 0;
            while (cmIt.hasNext())
            {
                CandidateMatch cm = (CandidateMatch) cmIt.next();
                ++i;

                result.append("    " + i + ": " + cm.getMatchTypeString()
                        + ": " + cm.getTimestamp() + ": " + cm.getGxmlTarget()
                        + "\n");
            }
        }

        return result.toString();
    }
}
