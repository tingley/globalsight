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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.log4j.Logger;

import com.globalsight.everest.jobhandler.Job;
import com.globalsight.ling.tm.ExactMatchedSegments;
import com.globalsight.ling.tm.LeverageMatchType;
import com.globalsight.ling.tm.LingManagerException;
import com.globalsight.ling.tm2.BaseTmTu;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.ling.tm2.SegmentTmTu;
import com.globalsight.ling.tm2.TmUtil;
import com.globalsight.util.GlobalSightLocale;

/**
 * LeverageDataCenter is a central repository of segment data to be levereaged
 * and the leverage results. This class provides the functions listed below.
 * 
 * - repository of the original source segment and the leverage results of these
 * segments.
 * 
 * - make a list of unique original source segments to avoid leverage identical
 * segments from Page TM many times.
 * 
 * - break up the original source segments into a main segment and subflows.
 * Maintain the mapping of the original segments and the separated segments.
 * 
 * - make a list of unique separated segments to avoid leverage identical
 * segments from Segment TM many times.
 * 
 * - produce ExactMatchedSegments object from the leverage results.
 */

public class LeverageDataCenter
{
    private static Logger c_logger = Logger.getLogger(LeverageDataCenter.class
            .getName());

    // leverage options
    private LeverageOptions m_leverageOptions = null;

    private GlobalSightLocale m_sourceLocale = null;
    private Collection m_targetLocales = null;
    private Job m_job = null;
    private boolean m_dynLevStopSearch = false;
    private ArrayList<String> m_dynLevExactMatchs = null;

    // mapping of a unique original source segment and a list of
    // identical segments to it.
    // key: BaseTmTuv (a unique original source segment)
    // value: IdenticalSegments object which holds a list of identical
    // original source segments and their leverage results
    private Map<BaseTmTuv, IdenticalSegments> m_uniqueOriginalSegments = null;

    // mapping of a unique separated source segment and a list of
    // identical segments to it
    // key: SegmentTmTuv (a unique separated original source segment)
    // value: Initially, it is null. After Segment Tm leveraging,
    // LeverageMatches object is set if the segment finds matches. It
    // remains null otherwise.
    private Map<BaseTmTuv, LeverageMatches> m_uniqueSeparatedSegments = null;

    // constructor
    public LeverageDataCenter(GlobalSightLocale p_sourceLocale,
            Collection p_targetLocales, LeverageOptions p_leverageOptions)
    {
        m_sourceLocale = p_sourceLocale;
        m_targetLocales = p_targetLocales;
        m_leverageOptions = p_leverageOptions;
        m_uniqueOriginalSegments = new HashMap<BaseTmTuv, IdenticalSegments>();
        m_uniqueSeparatedSegments = null;
    }

    /**
     * Get source locale
     * 
     * @return source locale in GlobalSightLocale
     */
    public GlobalSightLocale getSourceLocale()
    {
        return m_sourceLocale;
    }

    /**
     * Get leverage options
     * 
     * @return LeverageOptions object
     */
    public LeverageOptions getLeverageOptions()
    {
        return m_leverageOptions;
    }

    /**
     * Get target locales
     * 
     * @return Collection of target locales (GlobalSightLocale)
     */
    public Collection getTargetLocales()
    {
        return m_targetLocales;
    }

    /**
     * Add an original source Tuv to this object.
     * 
     * @param p_tuv
     *            original source Tuv that is leveraged
     */
    public void addOriginalSourceTuv(BaseTmTuv p_tuv)
    {
        IdenticalSegments identicalSegments = m_uniqueOriginalSegments
                .get(p_tuv);

        if (identicalSegments == null)
        {
            identicalSegments = new IdenticalSegments();
            m_uniqueOriginalSegments.put(p_tuv, identicalSegments);
        }
        identicalSegments.add(p_tuv);
    }
    
    public void setDynLevStopSearch(boolean stopSearch)
    {
        m_dynLevStopSearch = stopSearch;
    }
    
    public void addDynLevExactMatch(String exactMatchKey)
    {
        if (!m_dynLevStopSearch)
        {
            return;
        }
        
        if (m_dynLevExactMatchs == null)
        {
            m_dynLevExactMatchs = new ArrayList<String>();
        }
        
        m_dynLevExactMatchs.add(exactMatchKey);
    }

    /**
     * Get a Set of whole (un-separated) BaseTmTuv objects that are leveraged in
     * Page TM or TUV table. This is a unique set of segments collected from all
     * segments that are leveraged. This method returns only segments that don't
     * have matches for all locales.
     * 
     * @return Set of unique original source segments (BaseTmTuv) that are
     *         leveraged in Page TM.
     */
    public Set<BaseTmTuv> getOriginalWholeSegments(long p_jobId)
    {
        Set<BaseTmTuv> segments = new HashSet<BaseTmTuv>();

        // walk through all unique original source segments
        for (Map.Entry<BaseTmTuv, IdenticalSegments> entry : m_uniqueOriginalSegments
                .entrySet())
        {
            // if the unique segment has an exact match for all
            // locales, skip the segment. We don't re-leverage again a
            // segment that already has an exact match.
            if (!entry.getValue().hasPageTmExactMatchForAllLocales(p_jobId))
            {
                segments.add(entry.getKey());
            }
        }

        return segments;
    }

    /**
     * Get a Set of BaseTmTuv objects that are leveraged in Segment TM. This is
     * a unique set of subflow separated segments collected from all segments
     * that are leveraged. If the Page TM leverage has taken place before the
     * Segment TM leveraging, the segments that have found their matches in the
     * Page TM are not included in the set returned from this method.
     * 
     * @return Set of unique original (subflow separated) source segments
     *         (BaseTmTuv) that are leveraged in Segment TM.
     */
    public Set<BaseTmTuv> getOriginalSeparatedSegments(long p_jobId)
            throws Exception
    {
        // if unique separated segments map is not there, build it.
        if (m_uniqueSeparatedSegments == null)
        {
            m_uniqueSeparatedSegments = new HashMap<BaseTmTuv, LeverageMatches>();

            // walk through all unique original source segments
            Iterator<IdenticalSegments> itIdenticalSegments = m_uniqueOriginalSegments
                    .values().iterator();
            while (itIdenticalSegments.hasNext())
            {
                IdenticalSegments identicalSegments = itIdenticalSegments
                        .next();

                // if the unique segment has a page tm exact match for
                // all locales, skip the segment. We don't leverage
                // again a segment that already has a page tm exact
                // match.
                if (!identicalSegments.hasPageTmExactMatchForAllLocales(p_jobId))
                {
                    // walk through all separated segments in a unique
                    // original segment
                    Iterator<LeverageMatches> itSeparatedSegment = identicalSegments
                            .separatedSegmentIterator();
                    while (itSeparatedSegment.hasNext())
                    {
                        LeverageMatches levMatch = itSeparatedSegment.next();
                        BaseTmTuv originalSeparatedTuv = levMatch
                                .getOriginalTuv();

                        m_uniqueSeparatedSegments.put(originalSeparatedTuv,
                                null);
                    }
                }
            }
        }

        Set<BaseTmTuv> segments = m_uniqueSeparatedSegments.keySet();
        if (m_dynLevStopSearch && m_dynLevExactMatchs != null)
        {
            int size = segments.size() - m_dynLevExactMatchs.size();
            Set<BaseTmTuv> result = new HashSet<BaseTmTuv>(size);

            for (BaseTmTuv baseTmTuv : segments)
            {
                String key = baseTmTuv.getId() + ""
                        + getSourceLocale().toString();

                if (!m_dynLevExactMatchs.contains(key))
                {
                    result.add(baseTmTuv);
                }
            }

            return result;
        }
        else
        {
            return segments;
        }
    }

    /**
     * Add match results from a TM that leverages whole segment
     * 
     * @param p_leverageResults
     *            LeverageMatchResults object
     */
    public void addLeverageResultsOfWholeSegment(
            LeverageMatchResults p_leverageResults) throws Exception
    {
        // Walk through the list of leverage result of the page
        Iterator<LeverageMatches> itLeverageResult = p_leverageResults
                .iterator();
        while (itLeverageResult.hasNext())
        {
            // get the original source segment of the leverage result
            LeverageMatches leverageMatches = itLeverageResult.next();
            BaseTmTuv originalTuv = leverageMatches.getOriginalTuv();

            // set the leverage result to an IdenticalSegments of the
            // original source segment
            IdenticalSegments identicalSegments = m_uniqueOriginalSegments
                    .get(originalTuv);
            identicalSegments.setLeverageResultsOfWholeSegment(leverageMatches);
        }
    }

    /**
     * Add leverage results from Segment TM leveraging to this object. This will
     * merge with existing results.
     * 
     * @param p_leverageResults
     *            LeverageMatchResults object
     */
    public void addLeverageResultsOfSegmentTmMatching(
            LeverageMatchResults p_leverageResults) throws Exception
    {
        if (m_uniqueSeparatedSegments == null)
        {
            m_uniqueSeparatedSegments = new HashMap<BaseTmTuv, LeverageMatches>();
        }
        
        // Walk through the list of leverage result of the page
        for (LeverageMatches leverageMatches : p_leverageResults)
        {
            // get the original (subflow separated) source segment of
            // the leverage result
            BaseTmTuv originalTuv = leverageMatches.getOriginalTuv();

            // set the leverage result
            LeverageMatches existing = m_uniqueSeparatedSegments
                    .get(originalTuv);
            if (existing == null)
            {
                m_uniqueSeparatedSegments.put(originalTuv, leverageMatches);
            }
            else
            {
                // Merge with existing results
                existing.merge(leverageMatches);
            }
        }
    }

    /**
     * Get ExactMatchedSegments from the leverage results held in this object.
     * Segments all of whose subflow separated segments have got an exact match
     * (including Page TM match) are re-composed to a complete segment (meaning
     * merging subflows into a main segment) and are included in the
     * ExactMatchedSegments object. The segments that have only fuzzy matches
     * are not included in it.
     * 
     * @return ExactMatchedSegments object
     */
    public ExactMatchedSegments getExactMatchedSegments(long p_jobId)
            throws Exception
    {
        ExactMatchedSegments exactMatchedSegments = new ExactMatchedSegments();

        // walk through all unique original source segments
        Iterator<IdenticalSegments> itOriginalSource = m_uniqueOriginalSegments
                .values().iterator();
        while (itOriginalSource.hasNext())
        {
            IdenticalSegments identicalSegments = itOriginalSource.next();

            // check if the original segment and all its subflows get
            // an exact match
            ExactLeverageMatch exactLevMatch = identicalSegments
                    .getCompleteExactMatch(p_jobId);
            if (exactLevMatch != null)
            {
                // walk through all target matches in the exact match
                Iterator itTargetLocale = exactLevMatch.targetLocaleIterator();
                while (itTargetLocale.hasNext())
                {
                    GlobalSightLocale targetLocale = (GlobalSightLocale) itTargetLocale
                            .next();
                    LeveragedTuv matchedTuv = exactLevMatch
                            .getMatch(targetLocale);

                    // walk through all original source segments in
                    // this unique segment (IdenticalSegments)
                    Iterator<BaseTmTuv> itOriginalSegment = identicalSegments
                            .originalSegmentIterator();
                    while (itOriginalSegment.hasNext())
                    {
                        BaseTmTuv originalTuv = itOriginalSegment.next();

                        // set the exact match segment in ExactMatchedSegments object
                        String matchType = getJobTuvState(matchedTuv.getMatchState());
                        int tmIndex = m_leverageOptions
                                .getTmIndexsToLeverageFrom()
                                .get(matchedTuv.getTu().getTmId()).intValue();
                        long tmId = matchedTuv.getTu().getTmId();
						exactMatchedSegments.putLeveragedSegment(targetLocale,
								originalTuv.getId(), matchedTuv.getSegment(),
								matchType, matchedTuv.getModifyDate(),
								matchedTuv.getLastUsageDate(),
								matchedTuv.getPreviousHash(),
								matchedTuv.getNextHash(), tmIndex,
								matchedTuv.getSid(), matchedTuv.getId(), tmId);
                    }
                }
            }
        }

        return exactMatchedSegments;
    }

    // get (old) state for job data.
    public String getJobTuvState(MatchState p_matchState)
    {
        String jobTuvState = null;

        if (p_matchState == null)
        {
            jobTuvState = LeverageMatchType.UNKNOWN_NAME;
        }
        else if (p_matchState.equals(MatchState.SEGMENT_TM_EXACT_MATCH))
        {
            jobTuvState = LeverageMatchType.EXACT_MATCH_NAME;
        }
        else if (p_matchState.equals(MatchState.PAGE_TM_EXACT_MATCH))
        {
            jobTuvState = LeverageMatchType.LEVERAGE_GROUP_EXACT_MATCH_NAME;
        }
        else if (p_matchState.equals(MatchState.UNVERIFIED_EXACT_MATCH))
        {
            jobTuvState = LeverageMatchType.UNVERIFIED_EXACT_MATCH_NAME;
        }

        return jobTuvState;
    }

    public static String getTuvState(String p_matchState)
    {
        String jobTuvState = null;

        if (p_matchState == null)
        {
            jobTuvState = LeverageMatchType.UNKNOWN_NAME;
        }
        else if (p_matchState.equals(MatchState.SEGMENT_TM_EXACT_MATCH
                .getName()))
        {
            jobTuvState = LeverageMatchType.EXACT_MATCH_NAME;
        }
        else if (p_matchState.equals(MatchState.PAGE_TM_EXACT_MATCH.getName()))
        {
            jobTuvState = LeverageMatchType.LEVERAGE_GROUP_EXACT_MATCH_NAME;
        }
        else if (p_matchState.equals(MatchState.UNVERIFIED_EXACT_MATCH
                .getName()))
        {
            jobTuvState = LeverageMatchType.UNVERIFIED_EXACT_MATCH_NAME;
        }

        return jobTuvState;
    }

    /**
     * Apply Page Tm leveraging options to each Page Tm match.
     */
    public void applyPageTmOptions() throws Exception
    {
        Iterator<IdenticalSegments> itIdenticalSegments = m_uniqueOriginalSegments
                .values().iterator();
        while (itIdenticalSegments.hasNext())
        {
            IdenticalSegments identicalSegments = itIdenticalSegments.next();
            identicalSegments.applyPageTmOptions();
        }
    }

    /**
     * Apply Segment Tm leveraging options to each Segment Tm match.
     */
    public void applySegmentTmOptions() throws Exception
    {
        if (m_uniqueSeparatedSegments != null)
        {
            Iterator<LeverageMatches> itLeverageMatches = m_uniqueSeparatedSegments
                    .values().iterator();
            while (itLeverageMatches.hasNext())
            {
                LeverageMatches levMatches = itLeverageMatches.next();

                // value of m_uniqueSeparatedSegments can be null
                if (levMatches != null)
                {
                    levMatches.setJob(m_job);
                    levMatches.applySegmentTmOptions();
                }
            }
        }
    }

    /**
     * Get an Iterator that iterates leverage match results in this object.
     * next() method of this Iterator returns LeverageMatches object.
     * 
     * @return Iterator of LeverageMatches object
     */
    public Iterator<LeverageMatches> leverageResultIterator() throws Exception
    {
        return new LeverageResultItr();
    }

    public Job getJob()
    {
        return m_job;
    }

    public void setJob(Job m_job)
    {
        this.m_job = m_job;
    }

    // leverage result iterator
    private class LeverageResultItr implements Iterator<LeverageMatches>
    {
        Iterator<IdenticalSegments> m_itUniqueSegment = null;
        Iterator<LeverageMatches> m_itSeparatedSegmentMatch = null;

        // constructor
        private LeverageResultItr() throws Exception
        {
            // iterator of unique segments
            m_itUniqueSegment = m_uniqueOriginalSegments.values().iterator();
            if (m_itUniqueSegment.hasNext())
            {
                // iterator of separated segment match in IdenticalSegments
                IdenticalSegments identicalSegments = m_itUniqueSegment.next();
                m_itSeparatedSegmentMatch = identicalSegments
                        .separatedSegmentMatchIterator();
            }
        }

        public boolean hasNext()
        {
            boolean result = false;
            if (m_itSeparatedSegmentMatch != null)
            {
                result = m_itSeparatedSegmentMatch.hasNext();
                if (result == false)
                {
                    if (m_itUniqueSegment.hasNext())
                    {
                        result = true;
                        // iterator of separated segment match in
                        // IdenticalSegments
                        IdenticalSegments identicalSegments = (IdenticalSegments) m_itUniqueSegment
                                .next();

                        // can't throw checked exceptions
                        try
                        {
                            m_itSeparatedSegmentMatch = identicalSegments
                                    .separatedSegmentMatchIterator();
                        }
                        catch (Exception e)
                        {
                            throw new RuntimeException(e.getMessage());
                        }
                    }
                }
            }
            return result;
        }

        public LeverageMatches next()
        {
            LeverageMatches o = null;
            if (hasNext())
            {
                o = m_itSeparatedSegmentMatch.next();
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

    // Inner class. This class holds identical segments of a unique
    // original segment.
    private class IdenticalSegments
    {
        // List of identical original source Tuvs. Two Tuvs are deemed to
        // be identical when they have identical segment text, the same
        // type and the same localize type. See BaseTmTuv#equals()
        private List<BaseTmTuv> m_originalSourceTuvs = new ArrayList<BaseTmTuv>();

        // Page TM matches
        private LeverageMatches m_pageTmLeverageMatches = null;

        // map of subflow id and its LeverageMatches object. One of an
        // original source Tuv this class holds is broken up and form this
        // member variable.
        // key: Subflow id in String (main segment's id is "0")
        // value: LeverageMatches object, which holds a separated Tuv
        // that corresponds to the subflow id. This object also holds
        // leverage match result of the separated segment.
        private Map<String, LeverageMatches> m_separatedSegments = null;

        // indicates that the values in m_separatedSegments have been
        // merged with the Segment TM match results
        private boolean m_separatedSegmentMatchMerged = false;

        /**
         * Add a Tuv
         * 
         * @param p_tuv
         *            original source Tuv to be leveraged
         */
        private void add(BaseTmTuv p_tuv)
        {
            m_originalSourceTuvs.add(p_tuv);
        }

        /**
         * Set leverage result of page tm matching for this unique original
         * source Tuv
         * 
         * @param p_leverageMatches
         *            leverage match result of page tm
         */
        private void setLeverageResultsOfWholeSegment(
                LeverageMatches p_leverageMatches) throws Exception
        {
            if (p_leverageMatches != null)
            {
                if (m_pageTmLeverageMatches != null)
                {
                    m_pageTmLeverageMatches.merge(p_leverageMatches);
                }
                else
                {
                    m_pageTmLeverageMatches = p_leverageMatches;
                }

                setPageTmMatchResultToSeparatedSegments();
            }
        }

        /**
         * Break up a page tm match result into main segments and subflows and
         * set them to m_separatedSegments.
         * 
         * @param p_leverageMatches
         *            Page tm leverage result
         */
        private void setPageTmMatchResultToSeparatedSegments() throws Exception
        {
            // reset the merge flag
            m_separatedSegmentMatchMerged = false;

            // if separated segments list are not setup, build it
            if (m_separatedSegments == null)
            {
                setupSeparatedSegments();
            }

            // break up the page tm match result and put them in
            // m_separatedSegments
            Iterator itPageTmMatchSeparated = m_pageTmLeverageMatches
                    .separateMatch().iterator();
            while (itPageTmMatchSeparated.hasNext())
            {
                LeverageMatches separatedLevMatch = (LeverageMatches) itPageTmMatchSeparated
                        .next();

                String subId = separatedLevMatch.getSubflowId();
                // sanity check
                if (!m_separatedSegments.containsKey(subId))
                {
                    throw new LingManagerException(
                            "IllegalSeparatedPageTmMatch", null, null);
                }

                m_separatedSegments.put(subId, separatedLevMatch);
            }
        }

        /**
         * Break up the original segment and put them in m_separatedSegments.
         */
        private void setupSeparatedSegments() throws Exception
        {
            // if separated segment list is not there yet, build it.
            if (m_separatedSegments == null)
            {
                m_separatedSegments = new HashMap<String, LeverageMatches>();

                // no original segment, no separated segment
                if (!m_originalSourceTuvs.isEmpty())
                {
                    // get the first original source segment
                    BaseTmTuv originalTuv = m_originalSourceTuvs.get(0);
                    BaseTmTu originalTu = originalTuv.getTu();
                    GlobalSightLocale sourceLocale = originalTuv.getLocale();

                    // break the segment up
                    Iterator<SegmentTmTu> itSeparatedTu = TmUtil
                            .createSegmentTmTus(originalTu, sourceLocale)
                            .iterator();

                    // walk through the separated segments
                    while (itSeparatedTu.hasNext())
                    {
                        BaseTmTuv separatedTuv = itSeparatedTu.next()
                                .getFirstTuv(sourceLocale);

                        String subId = ((SegmentTmTu) separatedTuv.getTu())
                                .getSubId();
                        LeverageMatches levMatch = new LeverageMatches(
                                separatedTuv, m_leverageOptions);
                        m_separatedSegments.put(subId, levMatch);
                    }
                }
            }
        }

        /**
         * Get LeverageMatch that has exact matches of the complete segment (not
         * separated segments) for all or part of target locales.
         * 
         * All the parts of the segment (the main segment and its subflows)
         * returned in LeverageMatch are not necessarily exact matches. Only the
         * main segment or only a subflow can be an exact. If the other parts
         * didn't get exact matches in the TM, source segments are substituted.
         * In other words, only one of parts of the segment gets an exact match,
         * LeverageMatch for the segment is created and the segment string will
         * be copied in the target segment in translation_unit_variant table.
         * 
         * @return LeverageMatch object
         */
        private ExactLeverageMatch getCompleteExactMatch(long p_jobId)
                throws Exception
        {
            ExactLeverageMatch exactLevMatch = null;
            Set<GlobalSightLocale> exactTargetForPageTm = null;
            if (m_pageTmLeverageMatches != null)
            {
                // get exact matches from Page TM matches
                exactLevMatch = m_pageTmLeverageMatches
                        .getExactLeverageMatch(p_jobId);
                exactTargetForPageTm = m_pageTmLeverageMatches
                        .getExactMatchLocales(p_jobId);
            }
            else
            {
                exactLevMatch = new ExactLeverageMatch();
                exactTargetForPageTm = new HashSet<GlobalSightLocale>();
            }

            if (m_separatedSegments != null)
            {
                // get all target locales that have at least one exact
                // match for any separated segments
                Set<GlobalSightLocale> exactTargetForSegmentTm = new HashSet<GlobalSightLocale>();
                for (LeverageMatches levMatches : m_separatedSegments.values())
                {
                    exactTargetForSegmentTm.addAll(levMatches
                            .getExactMatchLocales(p_jobId));
                }

                // remove locales that have Page TM exact match
                exactTargetForSegmentTm.removeAll(exactTargetForPageTm);

                Iterator<GlobalSightLocale> itTargetLocales = exactTargetForSegmentTm
                        .iterator();
                while (itTargetLocales.hasNext())
                {
                    GlobalSightLocale targetLocale = itTargetLocales.next();

                    // for each target locale, get exact match or source
                    // segment (if there is no match for the separated
                    // segment) for all separated segment and put them in
                    // a Map
                    LeveragedTuv levMatchMainSegment = null;
                    boolean isExactMatch = true;
                    Map<String, String> separatedSegmentMap = new HashMap<String, String>();
                    for (String subId : m_separatedSegments.keySet())
                    {
                        LeverageMatches levMatches = m_separatedSegments
                                .get(subId);
                        LeveragedTuv levMatchSub = levMatches
                                .getExactLeverageMatch(targetLocale, p_jobId);
                        if (subId == SegmentTmTu.ROOT)
                        {
                            levMatchMainSegment = levMatchSub;
                        }

                        if (levMatchSub == null)
                        {
                            isExactMatch = false;
                        }

                        String segment = null;
                        if (levMatchSub != null)
                        {
                            segment = levMatchSub.getSegment();
                        }
                        else
                        {
                            segment = levMatches.getOriginalTuv().getSegment();
                        }

                        separatedSegmentMap.put(subId, segment);
                    }

                    // get an original segment text (with formatting code
                    // and subflows in it)
                    String originalSegmentText = m_originalSourceTuvs.get(0)
                            .getSegment();

                    // get a composed segment text
                    String composedSegmentText = TmUtil.composeCompleteText(
                            originalSegmentText, separatedSegmentMap);

                    // If there isn't matched main segment, create one
                    // from the original main segment
                    if (levMatchMainSegment == null)
                    {
                        BaseTmTuv originalMainSegment = m_separatedSegments
                                .get(SegmentTmTu.ROOT).getOriginalTuv();
                        BaseTmTu originalMainTu = originalMainSegment.getTu();

                        LeveragedTu createdTu = new LeveragedPageTu(0, 0,
                                originalMainTu.getFormat(),
                                originalMainTu.getType(),
                                originalMainTu.isTranslatable(),
                                originalMainSegment.getLocale());

                        levMatchMainSegment = new LeveragedPageTuv(0, "",
                                targetLocale);

                        createdTu.addTuv(levMatchMainSegment);
                    }

                    // put the composed segment in it
                    levMatchMainSegment.setSegment(composedSegmentText);

                    // put it in a return object
                    if (isExactMatch)
                    {
                        exactLevMatch.add(levMatchMainSegment, targetLocale);
                    }
                }
            }

            return exactLevMatch;
        }

        /**
         * Apply Page Tm leveraging options to the Page Tm match.
         */
        public void applyPageTmOptions() throws Exception
        {
            if (m_pageTmLeverageMatches != null)
            {
                m_pageTmLeverageMatches.applyPageTmOptions();
            }
        }

        /**
         * Returns Iterator of a list of identical original source segments.
         * next() method of this Iterator returns BaseTmTuv object
         * 
         * @return Iterator
         */
        private Iterator<BaseTmTuv> originalSegmentIterator()
        {
            return m_originalSourceTuvs.iterator();
        }

        /**
         * Returns Iterator of a list of separated segments. next() method of
         * this Iterator returns LeverageMatches object
         * 
         * @return Iterator
         */
        private Iterator<LeverageMatches> separatedSegmentIterator()
                throws Exception
        {
            // if separated segment list is not there yet, build it.
            if (m_separatedSegments == null)
            {
                setupSeparatedSegments();
            }
            return m_separatedSegments.values().iterator();
        }

        /**
         * Test if Page TM leverage results have exact matches for all target
         * locales
         */
        private boolean hasPageTmExactMatchForAllLocales(long p_jobId)
        {
            boolean result = false;

            if (m_pageTmLeverageMatches != null)
            {
                // get all target locales of the exact matches
                Collection exactMatchedTargetLocales = m_pageTmLeverageMatches
                        .getExactMatchLocales(p_jobId);

                // test if the exact match target locales contains all
                // target locales for leveraging
                result = exactMatchedTargetLocales.containsAll(m_targetLocales);
            }

            return result;
        }

        /**
         * Merge Segment TM match result of each separated segment with the
         * separated Page TM match results.
         */
        private void mergeSeparatedSegmentMatchResult() throws Exception
        {
            // if separated segment list is not there yet, build it.
            if (m_separatedSegments == null)
            {
                setupSeparatedSegments();
            }

            if (m_uniqueSeparatedSegments != null)
            {
                Iterator<LeverageMatches> itSeparatedSegment = m_separatedSegments
                        .values().iterator();
                while (itSeparatedSegment.hasNext())
                {
                    LeverageMatches pageTmLevMatch = itSeparatedSegment.next();
                    BaseTmTuv separatedSourceTuv = pageTmLevMatch
                            .getOriginalTuv();
                    LeverageMatches segmentTmLevMatch = m_uniqueSeparatedSegments
                            .get(separatedSourceTuv);
                    pageTmLevMatch.merge(segmentTmLevMatch);
                }
            }

            // set the 'merged' flag to true
            m_separatedSegmentMatchMerged = true;
        }

        /**
         * Get an Iterator that iterates leverage match results of separated
         * segments in this object. The iterator iterates the result of each
         * separated segments of each original segments. next() method of this
         * Iterator returns appropriate LeverageMatches object.
         * 
         * @return Iterator of LeverageMatches object
         */
        private Iterator<LeverageMatches> separatedSegmentMatchIterator()
                throws Exception
        {
            // if separated segment list is not there yet, build it.
            if (m_separatedSegments == null)
            {
                setupSeparatedSegments();
            }

            // if match result is not merged yet, do so now
            if (!m_separatedSegmentMatchMerged)
            {
                mergeSeparatedSegmentMatchResult();
            }

            return new SeparatedSegmentMatchItr();
        }

        // iterator of separated segment tm matches
        private class SeparatedSegmentMatchItr implements
                Iterator<LeverageMatches>
        {
            Iterator<BaseTmTuv> m_itOriginalSegment = null;
            Iterator<LeverageMatches> m_itSeparatedSegment = null;
            BaseTmTuv m_originalSourceTuv = null;

            // constructor
            private SeparatedSegmentMatchItr()
            {
                // iterator of unique segments
                m_itOriginalSegment = m_originalSourceTuvs.iterator();
                if (m_itOriginalSegment.hasNext())
                {
                    m_originalSourceTuv = m_itOriginalSegment.next();

                    // iterator of separated segment
                    m_itSeparatedSegment = m_separatedSegments.values()
                            .iterator();
                }
            }

            public boolean hasNext()
            {
                boolean result = false;
                if (m_itSeparatedSegment != null)
                {
                    result = m_itSeparatedSegment.hasNext();
                    if (result == false)
                    {
                        if (m_itOriginalSegment.hasNext())
                        {
                            result = true;

                            m_originalSourceTuv = m_itOriginalSegment.next();

                            // iterate the separated segment again
                            m_itSeparatedSegment = m_separatedSegments.values()
                                    .iterator();
                        }
                    }
                }
                return result;
            }

            public LeverageMatches next()
            {
                LeverageMatches levMatch = null;
                if (hasNext())
                {
                    levMatch = m_itSeparatedSegment.next();
                    levMatch.setOriginalSourceTuvId(m_originalSourceTuv.getId());
                }
                else
                {
                    throw new NoSuchElementException();
                }
                return levMatch;
            }

            public void remove()
            {
                // it shouldn't be called.
            }

        }

    }

}
