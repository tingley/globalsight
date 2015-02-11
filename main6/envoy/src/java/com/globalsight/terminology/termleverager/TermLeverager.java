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

package com.globalsight.terminology.termleverager;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Vector;

import com.globalsight.everest.persistence.PersistenceService;
import com.globalsight.everest.persistence.StoredProcCaller;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.ling.tm.TuvLing;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.log.GlobalSightCategory;
import com.globalsight.terminology.Hitlist;
import com.globalsight.terminology.ITermbase;
import com.globalsight.terminology.ITermbaseManager;
import com.globalsight.terminology.TermbaseException;

/**
 * Workhorse for TermLeverageManager that performs the actual leveraging.
 * 
 * Features: - use as little memory as posisble by storing in-memory results in
 * compressed data structures (the previous, database-based implementation
 * created too many duplicate objects in memory until it ran out of it). - match
 * prioritization based on term status = approved and term usage = preferred is
 * not implemented. I don't think anybody will miss it.
 * 
 * To implement it right, the TB_TERMS table needs to be extended with a USAGE
 * column, Termbase.java needs to populate it, and we need a migration script.
 * 
 * (Fri Nov 19 22:32:14 2004 CvdL: deferred)
 * 
 * Although this code supports multiple termbases, the options object does not:
 * language names may exist in multiple termbases but with different locales.
 */
public class TermLeverager
{
    private static final GlobalSightCategory CATEGORY = (GlobalSightCategory) GlobalSightCategory
            .getLogger(TermLeverager.class);

    //
    // Static Private Members
    //

    private static final String s_STRING_TERMINATOR = "";

    // Comparators can be static because they implement functional
    // interfaces (that don't hold state themselves).
    static private final TuvLingComparator c_tuvComparator = new TuvLingComparator();

    // Sorts source matches by score.
    static private final Comparator s_sourceTermComparator = new SourceTermComparator();

    // Sorts target matches by locale and term status.
    static private final Comparator s_targetTermComparator = new TargetTermComparator();

    static private ITermbaseManager s_manager;

    //
    // Constructor
    //

    public TermLeverager()
    {
        if (s_manager == null)
        {
            try
            {
                s_manager = ServerProxy.getTermbaseManager();
            }
            catch (Exception ex)
            {
                CATEGORY.error("Termbase Manager is not available (yet?)", ex);

                throw new RuntimeException("Cannot access TermbaseManager", ex);
            }
        }
    }

    //
    // Public Methods
    //

    public TermLeverageResult leverageTerms(String p_segment,
            TermLeverageOptions p_options) throws TermLeveragerException,
            RemoteException
    {
        TuvLing tuv = new TuvImpl();
        tuv.setGxml(p_segment);

        ArrayList dummyList = new ArrayList();
        dummyList.add(tuv);

        return leverageTerms(dummyList, p_options);
    }

    public TermLeverageResult leverageTerms(ArrayList p_tuvs,
            TermLeverageOptions p_options) throws TermLeveragerException,
            RemoteException
    {
        long start = System.currentTimeMillis();

        TermLeverageResult result = new TermLeverageResult();

        Collections.sort(p_tuvs, c_tuvComparator);

        // For each Termbase in options, leverage terms.
        ArrayList termbases = p_options.getTermBases();

        for (int i = 0, max = termbases.size(); i < max; i++)
        {
            String tbname = (String) termbases.get(i);

            leverageTermsInTermbase(tbname, p_tuvs, p_options, result);
        }

        // Now we have source terms in memory.
        if (result.size() > 0)
        {
            // debugPrint(result, p_tuvs, p_options);

            // Weed out bad source candidates (optional).
            // applyTermRecognition(result, p_tuvs, p_options);

            // Sort source matches by best score first (regardless of TB)
            reorderSourceTerms(result, p_options);

            if (p_options.getLoadTargetTerms())
            {
                // Load target terms for source terms.
                loadTargetTerms(result, p_options);

                // Remove source terms that have no target (are not
                // leverageable).
                removeSourceWithoutTarget(result, p_options);

                // Sort target terms by target locale and so that per
                // locale the preferred ones show up on top.
                reorderTargetTerms(result, p_options);
            }

            debugPrint(result, p_tuvs, p_options);
        }

        if (CATEGORY.isDebugEnabled())
        {
            long stop = System.currentTimeMillis();

            CATEGORY.debug("Total time to leverage terminology: "
                    + (stop - start) / 1000.0 + "s.");
        }

        return result;
    }

    //
    // Private Methods
    //

    private void leverageTermsInTermbase(String p_tbname, ArrayList p_tuvs,
            TermLeverageOptions p_options, TermLeverageResult p_result)
            throws TermLeveragerException, RemoteException
    {
        long tbid = s_manager.getTermbaseId(p_tbname);
        ITermbase tb = null;

        try
        {
            // Connect to the termbase see if it's still there.
            tb = s_manager.connect(p_tbname, ITermbase.SYSTEM_USER, "");
        }
        catch (TermbaseException ex)
        {
            CATEGORY.debug("Can't leverage from termbase " + p_tbname + ": "
                    + ex.getMessage());

            return;
        }

        // Then find the indexes to leverage from and query them.
        ArrayList langs = p_options.getSourcePageLangNames();

        l_languages: for (int i = 0, maxi = langs.size(); i < maxi; i++)
        {
            String lang = (String) langs.get(i);

            // Do that for each TUV, ignoring subflows since release 4.2.
            for (int j = 0, maxj = p_tuvs.size(); j < maxj; j++)
            {
                TuvLing tuv = (TuvLing) p_tuvs.get(j);

                try
                {
                    Hitlist hits = tb.recognizeTerms(lang, tuv
                            .getTermMatchFormat(), 10);

                    addSourceHits(p_result, tuv, hits, tbid);
                }
                catch (TermbaseException ex)
                {
                    // language doesn't exist? index locked?
                    CATEGORY.error("Could not leverage from index `" + lang
                            + "', skipping.", ex);

                    continue l_languages;
                }
            }
        }
    }

    /**
     * Helper method to record source matches in result.
     */
    private void addSourceHits(TermLeverageResult p_result, TuvLing p_tuv,
            Hitlist p_hits, long p_tbid)
    {
        ArrayList hits = p_hits.getHits();

        for (int i = 0, max = hits.size(); i < max; i++)
        {
            Hitlist.Hit hit = (Hitlist.Hit) hits.get(i);

            p_result.addSourceHit(p_tuv, p_tbid, hit.getConceptId(), hit
                    .getTermId(), hit.getTerm().trim(), hit.getScore(), hit.getDescXML());
        }
    }

    /*
     * private HashMap invertLocale2LangNameMapping( TermLeverageOptions
     * p_options) { Locale loc; List langNames; Collection locales =
     * p_options.getAllTargetPageLocales(); HashMap result = new
     * HashMap(locales.size()); // target pages for (Iterator localeIt =
     * locales.iterator(); localeIt.hasNext();) { loc = (Locale)localeIt.next();
     * langNames = p_options.getTargetPageLangNames(loc);
     * 
     * for (Iterator langNameIt = langNames.iterator(); langNameIt.hasNext();) {
     * result.put(langNameIt.next(), loc); } } // source page loc =
     * p_options.getSourcePageLocale(); langNames =
     * p_options.getSourcePageLangNames(); for (Iterator langNameIt =
     * langNames.iterator(); langNameIt.hasNext();) {
     * result.put(langNameIt.next(), loc); }
     * 
     * return result; }
     */

    /**
     * Loads the target terms in all requested target locales.
     * 
     * This is done per termbase, trading multiple calls for easier parameter
     * passing and shortcutting if a termbase generated no matches.
     */
    private void loadTargetTerms(TermLeverageResult p_result,
            TermLeverageOptions p_options) throws TermLeveragerException,
            RemoteException
    {
        // Given the target languages to leverage from, retrieve the
        // list of locales as they are stored in the database (derived
        // from termbase definition).
        ArrayList targetLanguages = p_options.getAllTargetPageLangNames();
        long[] tbids = getTermbaseIds(p_options);

        Connection connection = null;
        ResultSet results = null;

        Vector numberParams = new Vector();
        Vector stringParams = new Vector();

        try
        {
            // Load target terms from each termbase.
            for (int i = 0, max = tbids.length; i < max; i++)
            {
                long tbid = tbids[i];

                // Termbase has a problem, skip.
                if (tbid == -1)
                {
                    continue;
                }

                numberParams.clear();
                stringParams.clear();

                boolean hasMatches = buildStoredProcedureParams(numberParams,
                        stringParams, tbid, p_result, targetLanguages);

                // If there were no matches from this termbase, skip.
                if (!hasMatches)
                {
                    continue;
                }

                if (connection == null)
                {
                    connection = PersistenceService.getInstance()
                            .getConnection();
                    connection.setAutoCommit(false);
                }
                results = StoredProcCaller.findTargetTerms(connection,
                        numberParams, stringParams);

                results.setFetchDirection(ResultSet.FETCH_FORWARD);
                while (results.next())
                {
                    long cid = results.getLong("cid");
                    long tid = results.getLong("tid");
                    String term = results.getString("term");
                    String langname = results.getString("lang_name");
                    // TODO: term penalties not implemented yet.
                    // String status = results.getString("status");
                    // String usage = results.getString("usage");

                    // Map lang_name to a termbase locale (language code only)
                    if (p_options.getLocale(langname) != null) {
                        String locale = p_options.getLocale(langname).getLanguage();

                        p_result.addTargetTerm(tbid, cid, tid, term, locale, "");
                    }
                }
            }
        }
        catch (Exception e)
        {
            CATEGORY.error("cannot load target terms", e);
            throw new TermLeveragerException(e);
        }
        finally
        {
            DbUtil.closeAll(results);

            if (connection != null)
            {
                try
                {
                    PersistenceService.getInstance().returnConnection(
                            connection);
                }
                catch (Throwable ignore)
                {
                }
            }
        }
    }

    private long[] getTermbaseIds(TermLeverageOptions p_options)
            throws RemoteException
    {
        ArrayList tbs = p_options.getTermBases();

        long[] result = new long[tbs.size()];

        for (int i = 0, max = tbs.size(); i < max; i++)
        {
            String tbname = (String) tbs.get(i);

            long tbid = s_manager.getTermbaseId(tbname);

            result[i] = tbid;
        }

        return result;
    }

    // number=cid,cid,... (no terminator or separator)
    // string=tbid,trglang,trglang,...,"" (terminator at end)
    // returns true if CIDs were found in this termbase.
    private boolean buildStoredProcedureParams(Vector p_numberParams,
            Vector p_stringParams, long p_tbid, TermLeverageResult p_result,
            ArrayList p_targetLanguages) throws TermLeveragerException
    {
        boolean result = false;

        // add term base to search
        p_stringParams.add(String.valueOf(p_tbid));

        // target languages
        for (int i = 0, max = p_targetLanguages.size(); i < max; i++)
        {
            p_stringParams.add((String) p_targetLanguages.get(i));
        }
        p_stringParams.add(s_STRING_TERMINATOR);

        // add all source matches in termbase tbid (their cids)
        for (Iterator it = p_result.getRecordIterator(); it.hasNext();)
        {
            ArrayList records = (ArrayList) it.next();

            for (int i = 0, max = records.size(); i < max; i++)
            {
                TermLeverageResult.MatchRecord record = (TermLeverageResult.MatchRecord) records
                        .get(i);

                if (record.getTermbaseId() == p_tbid)
                {
                    p_numberParams.add(new Long(record.getConceptId()));
                    result = true;
                }
            }
        }

        return result;
    }

    private void debugPrint(TermLeverageResult p_matches, ArrayList p_tuvs,
            TermLeverageOptions p_options)
    {
        if (!CATEGORY.isDebugEnabled())
        {
            return;
        }

        for (int i = 0, max = p_tuvs.size(); i < max; i++)
        {
            TuvLing tuv = (TuvLing) p_tuvs.get(i);
            Long id = tuv.getIdAsLong();

            if (p_matches.hasMatchForTuv(id))
            {
                ArrayList matches = p_matches.getMatchesForTuv(id);

                System.out.println("Hits from fuzzy index: tuvid " + id + ": "
                        + matches.size() + " matches");
                System.out.println("TUV '" + tuv.getTermMatchFormat() + "'");

                for (int j = 0, maxj = matches.size(); j < maxj; j++)
                {
                    TermLeverageResult.MatchRecord tlm = (TermLeverageResult.MatchRecord) matches
                            .get(j);

                    System.out.println(String.valueOf(j) + ": ["
                            + tlm.getTermbaseId() + "," + tlm.getConceptId()
                            + "," + tlm.getMatchedSourceTermId() + "] "
                            + tlm.getMatchedSourceTerm() + " ("
                            + tlm.getScore() + ")");

                    ArrayList targets = tlm.getSourceTerm().getTargetTerms();
                    if (targets == null)
                        continue;
                    for (int k = 0, maxk = targets.size(); k < maxk; k++)
                    {
                        TermLeverageResult.TargetTerm trg = (TermLeverageResult.TargetTerm) targets
                                .get(k);
                        System.out.println("\t" + trg.getMatchedTargetTerm()
                                + " (" + trg.getLocale() + ")");
                    }
                }
            }
        }
    }

    /**
     * Sort source matches best score first.
     */
    private void reorderSourceTerms(TermLeverageResult p_result,
            TermLeverageOptions p_options)
    {
        for (Iterator it = p_result.getRecordIterator(); it.hasNext();)
        {
            ArrayList records = (ArrayList) it.next();

            // Sort source terms by score.
            Collections.sort(records, s_sourceTermComparator);
        }
    }

    /**
     * Removes source terms that have absolutely no target term for the target
     * locales we're currently leveraging for.
     */
    private void removeSourceWithoutTarget(TermLeverageResult p_result,
            TermLeverageOptions p_options) throws TermLeveragerException
    {
        p_result.removeSourceWithoutTarget();
    }

    /**
     * Sorts target terms by target locale and so that per locale the preferred
     * ones show up on top.
     */
    private void reorderTargetTerms(TermLeverageResult p_result,
            TermLeverageOptions p_options)
    {
        for (Iterator it = p_result.getRecordIterator(); it.hasNext();)
        {
            ArrayList records = (ArrayList) it.next();

            for (int i = 0, max = records.size(); i < max; i++)
            {
                TermLeverageResult.MatchRecord record = (TermLeverageResult.MatchRecord) records
                        .get(i);

                ArrayList targets = record.getSourceTerm().getTargetTerms();

                // Sort target terms by target locale and term status
                Collections.sort(targets, s_targetTermComparator);
            }
        }
    }
}
