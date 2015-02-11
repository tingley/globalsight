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

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.globalsight.everest.tm.Tm;
import com.globalsight.everest.util.comparator.ComparatorByTmOrder;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.ling.tm2.SegmentTmTuv;
import com.globalsight.ling.tm2.TmUtil;
import com.globalsight.ling.tm2.persistence.SegmentTmMatchRetrieveProcCaller;
import com.globalsight.ling.tm2.persistence.SegmentTmPersistence;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.SortUtil;

/**
 * PageTmLeverager is responsible for leveraging Page Tm segments
 */

public class SegmentTmLeverager
{
    private static final Logger c_logger = Logger
            .getLogger(SegmentTmLeverager.class);

    private static final Object LOCK = new Object();

    /**
     * leverage a page from Segment Tm
     * 
     * @param p_leverageDataCenter
     *            LeverageDataCenter that contains segments that are leveraged.
     * @return LeverageMatchResults that contains leverage results
     */
    public LeverageMatchResults leverage(Connection p_connection,
            List<Tm> p_tms, LeverageDataCenter p_leverageDataCenter,
            long p_jobId) throws Exception
    {
        LeverageMatchResults levMatchResults = new LeverageMatchResults();
        GlobalSightLocale sourceLocale = p_leverageDataCenter.getSourceLocale();

        SegmentTmPersistence stPersistence = new SegmentTmPersistence(
                p_connection);

        // classify original source segments to translatable
        // and localizable
        Collection trSegments = new ArrayList();
        Collection loSegments = new ArrayList();
        Iterator itOriginalSegment = p_leverageDataCenter
                .getOriginalSeparatedSegments(p_jobId).iterator();
        while (itOriginalSegment.hasNext())
        {
            BaseTmTuv originalSegment = (BaseTmTuv) itOriginalSegment.next();
            if (originalSegment.isTranslatable())
            {
                trSegments.add(originalSegment);
            }
            else
            {
                loSegments.add(originalSegment);
            }
        }

        LeverageOptions leverageOptions = p_leverageDataCenter
                .getLeverageOptions();

        // leverage translatable
        //
        // 1. At first, exact match only leveraging is performed
        levMatchResults = leverageExactTranslatable(stPersistence,
                sourceLocale, trSegments, leverageOptions, p_tms);

        // Original Notes:
        // For Lexmark TMs leverage issue
        // Add a blank space while the segment is not end with blank space and
        // this segment can't be the last segment in a translatable. ???
        //
        // New Notes by York on 2009-10-29:
        // The purpose of below method is to keep only ONE space if the segment
        // has/have trailing space(s).
        // If no trailing space(s),do nothing.
        // Ignore this method for GBS-747.
        // addBlankSpace(levMatchResults);

        // 2. then do fuzzy matching unless "only exact match" is set
        if (!leverageOptions.leverageOnlyExactMatches())
        {
            // get the original segments that didn't find exact matches
            Collection forFuzzy = getNoMatchSegments(trSegments,
                    levMatchResults, leverageOptions);

            // fuzzy match leveraging
            LeverageMatchResults levFuzzyMatchResults = leverageFuzzyTranslatable(
                    p_connection, sourceLocale, forFuzzy, leverageOptions,
                    p_tms);
            levMatchResults.merge(levFuzzyMatchResults);
        }

        // leverage localizable if the user choose to do so
        if (leverageOptions.isLeveragingLocalizables())
        {
            LeverageMatchResults loLevMatchResults = leverageLocalizable(
                    stPersistence, sourceLocale, loSegments, leverageOptions,
                    p_tms);
            levMatchResults.merge(loLevMatchResults);
        }

        return levMatchResults;
    }

    /**
     * Leverage a segment
     * 
     * @param p_sourceTuv
     *            source segment
     * @param p_leverageOptions
     *            leverage options
     * @return LeverageMatches object
     */
    public LeverageMatches leverageSegment(Connection p_connection,
            BaseTmTuv p_sourceTuv, LeverageOptions p_leverageOptions,
            List<Tm> p_tms) throws Exception
    {
        List<BaseTmTuv> segments = new ArrayList<BaseTmTuv>();
        segments.add(p_sourceTuv);

        LeverageMatchResults levResults = null;
        if (p_sourceTuv.isTranslatable())
        {
            levResults = leverageFuzzyTranslatable(p_connection,
                    p_sourceTuv.getLocale(), segments, p_leverageOptions, p_tms);
        }
        else
        {
            SegmentTmPersistence stPersistence = new SegmentTmPersistence(
                    p_connection);
            levResults = leverageLocalizable(stPersistence,
                    p_sourceTuv.getLocale(), segments, p_leverageOptions, p_tms);
        }

        // LeverageMatchResults should holds at most one LeverageMatches.
        LeverageMatches levMatches = null;
        Iterator it = levResults.iterator();
        if (it.hasNext())
        {
            levMatches = (LeverageMatches) it.next();
        }

        if (levMatches == null)
        {
            levMatches = new LeverageMatches(p_sourceTuv, p_leverageOptions);
        }

        return levMatches;
    }

    /**
     * Leverage translatable segments from Segment Tm (exact match only)
     * 
     * @param p_stPersistence
     *            SegmentTmPersistence
     * @param p_sourceLocale
     *            source locale
     * @param p_trSegments
     *            Collection of original segments (BaseTmTuv)
     * @param p_leverageOptions
     *            leverage options
     * @return LeverageMatchResults object
     */
    private LeverageMatchResults leverageExactTranslatable(
            SegmentTmPersistence p_stPersistence,
            GlobalSightLocale p_sourceLocale, Collection p_trSegments,
            LeverageOptions p_leverageOptions, List<Tm> p_tms) throws Exception
    {
        return leverageExactSegments(p_stPersistence, p_sourceLocale,
                p_trSegments, true, p_leverageOptions, p_tms);
    }

    /**
     * Leverage translatable segments from Segment Tm (fuzzy leveraging)
     * 
     * @param p_connection
     *            Connection
     * @param p_sourceLocale
     *            source locale
     * @param p_trSegments
     *            Collection of original segments (BaseTmTuv)
     * @param p_leverageOptions
     *            leverage options
     * @return LeverageMatchResults object
     */
    private LeverageMatchResults leverageFuzzyTranslatable(
            Connection p_connection, GlobalSightLocale p_sourceLocale,
            Collection p_trSegments, LeverageOptions p_leverageOptions,
            List<Tm> p_tms) throws Exception
    {
        return leverageFuzzySegments(p_connection, p_sourceLocale,
                p_trSegments, p_leverageOptions, p_tms);
    }

    /**
     * Leverage localizable segments from Segment Tm
     * 
     * @param p_stPersistence
     *            SegmentTmPersistence
     * @param p_sourceLocale
     *            source locale
     * @param p_loSegments
     *            Collection of original segments (BaseTmTuv)
     * @param p_leverageOptions
     *            leverage options
     * @return LeverageMatchResults object
     */
    private LeverageMatchResults leverageLocalizable(
            SegmentTmPersistence p_stPersistence,
            GlobalSightLocale p_sourceLocale, Collection p_loSegments,
            LeverageOptions p_leverageOptions, List<Tm> p_tms) throws Exception
    {
        return leverageExactSegments(p_stPersistence, p_sourceLocale,
                p_loSegments, false, p_leverageOptions, p_tms);
    }

    /**
     * Leverage exact matches (translatable or localizable) from Segment Tm
     * 
     * @param p_stPersistence
     *            SegmentTmPersistence
     * @param p_sourceLocale
     *            source locale
     * @param p_segments
     *            Collection of original segments (BaseTmTuv)
     * @param p_isTranslatable
     *            indicates if leverage translatable segments
     * @param p_leverageOptions
     *            leverage options
     * @return LeverageMatchResults object
     */
    @SuppressWarnings("unchecked")
    private LeverageMatchResults leverageExactSegments(
            SegmentTmPersistence p_stPersistence,
            GlobalSightLocale p_sourceLocale, Collection p_segments,
            boolean p_isTranslatable, LeverageOptions p_leverageOptions,
            List<Tm> p_tms) throws Exception
    {
        LeverageMatchResults levResults = new LeverageMatchResults();

        c_logger.debug("Leverage localizable begin");

        synchronized (LOCK)
        {
            Iterator itLevMatches = p_stPersistence.leverageExact(
                    p_sourceLocale, p_segments, p_isTranslatable,
                    p_leverageOptions, p_tms);

            c_logger.debug("Leverage localizable end");
            c_logger.debug("Leverage match retrieve begin");

            while (itLevMatches.hasNext())
            {
                LeverageMatches levMatches = (LeverageMatches) itLevMatches
                        .next();
                List list = levMatches.getLeveragedTus();
                SortUtil.sort(list, new ComparatorByTmOrder(p_leverageOptions));
                levMatches.setLeveragedTus(list);
                levResults.add(levMatches);
            }
        }

        c_logger.debug("Leverage match retrieve end");

        return levResults;
    }

    /**
     * Leverage fuzzy matches for translatable segments from Segment Tm
     * 
     * @param p_connection
     *            Connection
     * @param p_sourceLocale
     *            source locale
     * @param p_segments
     *            Collection of original segments (SegmentTmTuv)
     * @param p_leverageOptions
     *            leverage options
     * @return LeverageMatchResults object
     */
    private LeverageMatchResults leverageFuzzySegments(Connection p_connection,
            GlobalSightLocale p_sourceLocale, Collection p_segments,
            LeverageOptions p_leverageOptions, List<Tm> p_tms) throws Exception
    {
        c_logger.debug("Leverage fuzzy begin");
        c_logger.debug("Creating token list");

        List segmentTokensList = new ArrayList(p_segments.size());

        // fix for GBS-2448, user could search target locale in TM Search Page,
        // if not from TM Search Page, keep old logic(by isMultiLingLeveraging
        // of FileProfile)
        boolean lookupTarget;
        if (p_leverageOptions.isFromTMSearchPage())
        {
            lookupTarget = true;
        }
        else
        {
            lookupTarget = p_leverageOptions.isMultiLingLeveraging();
        }

        FuzzyMatcher fuzzyMatcher = new FuzzyMatcher(p_sourceLocale, p_tms,
                lookupTarget);

        // tokenize segments and store them in segmentTokensList
        Iterator itSegment = p_segments.iterator();
        while (itSegment.hasNext())
        {
            SegmentForFuzzyMatching segmentForFuzzyMatching = new SegmentForFuzzyMatching(
                    (SegmentTmTuv) itSegment.next());

            segmentTokensList.add(segmentForFuzzyMatching);
        }

        List tokenMatchList = new ArrayList();
        try
        {
            c_logger.debug("match score compute begin");

            // compute match scores for each segment
            Iterator itSegmentToken = segmentTokensList.iterator();
            while (itSegmentToken.hasNext())
            {
                SegmentForFuzzyMatching segmentForFuzzyMatching = (SegmentForFuzzyMatching) itSegmentToken
                        .next();

                Collection tokenMatches = fuzzyMatcher.findMatches(
                        segmentForFuzzyMatching,
                        p_leverageOptions.getMatchThreshold(),
                        p_leverageOptions.getNumberOfMatchesReturned());

                // System.out.println("Score of matches:");
                // Iterator it = tokenMatches.iterator();
                // while(it.hasNext())
                // {
                // TokenMatch tokenMatch = (TokenMatch)it.next();
                // System.out.println(" " + tokenMatch.getScore());
                // }

                tokenMatchList.addAll(tokenMatches);
            }
        }
        finally
        {
            // clean up
            fuzzyMatcher.close();
        }

        c_logger.debug("match score compute end");

        // make a list of target locales + source locale
        Collection locales = new ArrayList(p_leverageOptions
                .getLeveragingLocales().getAllLeveragingLocales());
        locales.add(p_sourceLocale);

        SegmentTmMatchRetrieveProcCaller caller = new SegmentTmMatchRetrieveProcCaller(
                p_connection, locales, tokenMatchList);

        SegmentIdMap segmentIdMap = TmUtil.buildSegmentIdMap(p_segments);

        c_logger.debug("matched segments retrieve call");

        Iterator itLevMatches = new LeverageIterator(caller.getNextResult(),
                segmentIdMap, p_sourceLocale, true, LeveragedTu.SEGMENT_TM,
                p_leverageOptions);

        c_logger.debug("Leverage match retrieve begin");

        LeverageMatchResults levResults = new LeverageMatchResults();
        while (itLevMatches.hasNext())
        {
            LeverageMatches levMatches = (LeverageMatches) itLevMatches.next();
            levResults.add(levMatches);
        }
        c_logger.debug("Leverage match retrieve end");
        c_logger.debug("Leverage fuzzy end");

        return levResults;
    }

    // Select segments from p_segments that are not listed in
    // p_levMatchResults and return a new Collection that contains them
    private Collection getNoMatchSegments(Collection p_segments,
            LeverageMatchResults p_levMatchResults,
            LeverageOptions leverageOptions)
    {
        ArrayList newList = new ArrayList(p_segments);
        Set<GlobalSightLocale> trgLocales = leverageOptions
                .getLeveragingLocales().getAllTargetLocales();

        Iterator it = p_levMatchResults.iterator();
        while (it.hasNext())
        {
            boolean hasExactForAllTargetLocales = false;
            LeverageMatches levMatches = (LeverageMatches) it.next();
            for (LeveragedTu levTu : levMatches.getLeveragedTus())
            {
                Set<GlobalSightLocale> localesIHave = levTu.getAllTuvLocales();
                if (localesIHave.containsAll(trgLocales))
                {
                    hasExactForAllTargetLocales = true;
                    break;
                }
            }

            if (hasExactForAllTargetLocales)
            {
                newList.remove(levMatches.getOriginalTuv());
            }
        }

        return newList;
    }

    // For Lexmark TMs leverage issue
    private void addBlankSpace(LeverageMatchResults p_levMatchResults)
    {
        Iterator it = p_levMatchResults.iterator();
        while (it.hasNext())
        {
            LeverageMatches lm = (LeverageMatches) it.next();
            if (!isSourceTuvEndsWithBlankSpace(lm.getOriginalTuv()))
                continue;

            Iterator it2 = lm.getLeveragedTus().iterator();
            while (it2.hasNext())
            {
                _addBlankSpaceForTu((LeveragedSegmentTu) it2.next());
            }
        }
    }

    private boolean isSourceTuvEndsWithBlankSpace(BaseTmTuv sourceTuv)
    {
        String segment = sourceTuv.getSegment().trim();

        if (segment.endsWith(" </segment>"))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    private void _addBlankSpaceForTu(LeveragedSegmentTu tu)
    {
        GlobalSightLocale sourceLocale = tu.getSourceLocale();
        Iterator tuvLocalesIt = tu.getAllTuvLocales().iterator();
        while (tuvLocalesIt.hasNext())
        {
            GlobalSightLocale locale = (GlobalSightLocale) tuvLocalesIt.next();
            if (sourceLocale.equals(locale))
                continue;

            Iterator tuvsIt = tu.getTuvList(locale).iterator();
            while (tuvsIt.hasNext())
            {
                LeveragedSegmentTuv tuv = (LeveragedSegmentTuv) tuvsIt.next();
                String segment = tuv.getSegment().trim();

                if (segment.startsWith("<segment>")
                        && segment.endsWith("</segment>"))
                {
                    segment = segment.substring(0, segment.length()
                            - "</segment>".length());
                    segment = segment.trim() + " </segment>";
                }

                tuv.setSegment(segment);
            }
        }
    }
}
