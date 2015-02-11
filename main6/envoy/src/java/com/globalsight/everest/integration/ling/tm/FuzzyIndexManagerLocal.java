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



package com.globalsight.everest.integration.ling.tm;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import com.globalsight.everest.persistence.PersistenceService;
import com.globalsight.ling.tm.LeverageProperties;
import com.globalsight.ling.tm.TuvLing;
import com.globalsight.ling.tm.fuzzy.FuzzyIndexManager;
import com.globalsight.ling.tm.fuzzy.FuzzyIndexManagerException;
import com.globalsight.log.GlobalSightCategory;
import com.globalsight.persistence.fuzzyindexing.InsertFuzzyIndexPersistenceCommand;
import com.globalsight.util.GlobalSightLocale;

public class FuzzyIndexManagerLocal implements FuzzyIndexManager
{
    private static final GlobalSightCategory CATEGORY = (GlobalSightCategory) GlobalSightCategory
            .getLogger(FuzzyIndexManagerLocal.class.getName());

    /**
     * FuzzyIndexManagerLocal constructor comment.
     */
    public FuzzyIndexManagerLocal()
    {
        super();
    }

    /**
     * Query the FUZZY_INDEX table and get all matches for each token in
     * p_tokens. Limit the search to the supplied TM id and locale.
     * 
     * @param p_tokens -
     *            A collection of Token objects (a hash table of tokens and
     *            their frequencies).
     * @param p_locale -
     *            The locale of the Tuv used to generate the token.
     * @param p_tmId -
     *            The id of the Tm we are searching.
     * @return List of FuzzyCandidates sorted by fuzzy score.
     */
    public Collection getFuzzyCandidates(HashMap p_tokens,
            GlobalSightLocale p_locale, List p_tmIds,
            LeverageProperties p_leverageProperties)
            throws FuzzyIndexManagerException, RemoteException
    {
//        Collection results = null;
//        TopLinkPersistence ps = null;
//        Collection tokenCrcs = null;
//        Iterator tokenCrcsIterator = null;
//        Vector args = new Vector(5);
//
//        // tokenCount
//        args.add(new Integer(p_tokens.size()));
//
//        // tokenCrcs
//        Vector crcs = new Vector();
//        args.add(crcs);
//        tokenCrcs = p_tokens.values();
//        tokenCrcsIterator = tokenCrcs.iterator();
//
//        while (tokenCrcsIterator.hasNext())
//        {
//            long crc = ((Token) tokenCrcsIterator.next()).getTokenCrc();
//            crcs.add(new Long(crc));
//        }
//
//        // tmIds
//        args.add(new Vector(p_tmIds));
//
//        // locale Id
//        args.add(p_locale.getIdAsLong());
//
//        // fuzzyThreshold
//        args.add(new Float(p_leverageProperties.getFuzzyThreshold() * 0.01));
//
//        try
//        {
//            ps = PersistenceService.getInstance();
//            results = ps.executeNamedQuery(
//                    TokenQueryNames.TOKENS_BY_FUZZY_MATCH, args, false);
//        }
//        catch (PersistenceException pe)
//        {
//            CATEGORY.error("Cannot retrieve tokens", pe);
//            throw new FuzzyIndexManagerException(pe);
//        }
//
//        // Convert our results to FuzzyCandidates
//        ArrayList fuzzyCandidates = new ArrayList();
//        ReportQueryResult row = null;
//
//        for (Iterator it = results.iterator(); it.hasNext();)
//        {
//            row = (ReportQueryResult) it.next();
//
//            long tuvId = ((Long) row.getByIndex(0)).longValue();
//            double score = ((BigDecimal) row.getByIndex(1)).doubleValue();
//            short roundedScore = (short) Math.round((score * 100.0));
//
//            fuzzyCandidates.add(new FuzzyCandidate(tuvId, roundedScore));
//        }
//
        return null;
    }

    // CvdL
    /**
     * Queries the FUZZY_INDEX table and retrieves all matches for each token in
     * p_tokens, thereby filtering the returned tuvs by segment type and
     * excluded item types. Limits the search to the supplied TM id and locale.
     * 
     * @param p_tokens -
     *            A collection of Token objects (a hash table of tokens and
     *            their frequencies).
     * @param p_locale -
     *            The locale of the Tuv used to generate the token.
     * @param p_tmIds -
     *            List of Tms we are searching.
     * @param p_locType -
     *            segment type (LOCALIZABLE or TRANSLATABLE)
     * @param p_leverageExcludeTypes -
     *            List of excluded item types that should not be fuzzy-matched
     * @return List of FuzzyCandidates sorted by fuzzy score.
     */
    public Collection getFuzzyCandidates(TuvLing p_originalSourceTuv,
            HashMap p_tokens, GlobalSightLocale p_locale, List p_tmIds,
            long p_locType, Collection p_leverageExcludeTypes,
            LeverageProperties p_leverageProperties)
            throws FuzzyIndexManagerException, RemoteException
    {
//         Collection results = null;
        // Vector args = new Vector(7);
        //
        // // tokenCount
        // args.add(new Integer(p_tokens.size()));
        //
        // // tokenCrcs
        // Collection tokens = p_tokens.values();
        // Vector crcs = new Vector(tokens.size());
        // args.add(crcs);
        //
        // for (Iterator it = tokens.iterator(); it.hasNext();)
        // {
        // long crc = ((Token) it.next()).getTokenCrc();
        // crcs.add(new Long(crc));
        // }
        //
        // args.add(new Vector(p_tmIds));
        // args.add(p_locale.getIdAsLong());
        // args.add(TuvLing.getLocTypeString(p_locType));
        // args.add(new Vector(p_leverageExcludeTypes));
        // args.add(p_originalSourceTuv.getIdAsLong());
        //
        // // fuzzyThreshold
        // args.add(new Float(p_leverageProperties.getFuzzyThreshold() * 0.01));
        //
        // try
        // {
        // TopLinkPersistence ps = PersistenceService.getInstance();
        // results = ps
        // .executeNamedQuery(
        // TokenQueryNames.FILTERED_TOKENS_BY_FUZZY_MATCH,
        // args, false);
        // }
        // catch (PersistenceException pe)
        // {
        // CATEGORY.error("Cannot retrieve tokens", pe);
        // throw new FuzzyIndexManagerException(pe);
        // }
        //
        // // Convert our results to FuzzyCandidates
        // HashSet fuzzyCandidates = new HashSet(results.size());
        // ReportQueryResult row = null;
        //
        // for (Iterator it = results.iterator(); it.hasNext();)
        // {
        //            row = (ReportQueryResult) it.next();
        //
        //            long tuvId = ((Long) row.getByIndex(0)).longValue();
        //            double score = ((BigDecimal) row.getByIndex(1)).doubleValue();
        //            short roundedScore = (short) Math.round((score * 100.0));
        //
        //            fuzzyCandidates.add(new FuzzyCandidate(tuvId, roundedScore));
        //        }

        return null;
    }

    /**
     * For each token in p_tokens add a row to the FUZZY_INDEX table with the
     * p_tuvId, token CRC, token count, locale and tm id.
     * 
     * @param p_tokens -
     *            A collection of Token objects.
     */
    public void updateFuzzyIndex(Vector p_tokens)
            throws FuzzyIndexManagerException, RemoteException
    {
        // Session client = null;
        // UnitOfWork uow = null;
        //
        // try
        // {
        // client = PersistenceService.getInstance().acquireClientSession();
        // uow = client.acquireUnitOfWork();
        // uow.registerAllObjects(p_tokens);
        // uow.commit();
        // }
        // catch (PersistenceException pe)
        // {
        // if (uow != null)
        // {
        // uow.rollbackTransaction();
        // }
        //
        // CATEGORY.error("Cannot update fuzzy index", pe);
        //
        // throw new FuzzyIndexManagerException(pe);
        //        }
        //        finally
        //        {
        //            if (client != null)
        //            {
        //                client.release();
        //            }
        //        }
    }

//    /**
//     * Index tuvs using a stored procedure. 
//     *
//     * @param p_paramList a list of parameters passed to the stored
//     * procedure. The order of parameters is as follows.
//     * <ul>
//     * <li>tuv_id</li>
//     * <li>locale_id</li>
//     * <li>tm_id</li>
//     * <li>token_count</li>
//     * <li>fisrt_token_crc</li>
//     * <li>...</li>
//     * <li>last_token_crc</li>
//     * <li>-1  // separater</li></ul>
//     */
//
//    public synchronized void callIndexingProcedure(Vector p_paramList)
//        throws FuzzyIndexManagerException,
//               RemoteException
//    {
//        try
//        {
//            OracleStoredProcCaller.insertFuzzyIndex(p_paramList);
//        }
//        catch (Exception e)
//        {
//            CATEGORY.error("Failed to call index stored procedure", e);
//            throw new FuzzyIndexManagerException(e);
//        }
//    }

    public void persistFuzzyIndexes(Vector p_paramList)
            throws FuzzyIndexManagerException, RemoteException
    {
        Connection connection = null;
        try
        {
            connection = PersistenceService.getInstance().getConnection();
            InsertFuzzyIndexPersistenceCommand ifipc = new InsertFuzzyIndexPersistenceCommand(
                    p_paramList);
            ifipc.persistObjects(connection);
            connection.commit();
        }
        catch (Exception e)
        {
            try
            {
                connection.rollback();
                throw new FuzzyIndexManagerException(e);
            }
            catch (Exception sqle)
            {
                throw new FuzzyIndexManagerException(e);
            }
        }
        finally
        {
            try
            {
                if (connection != null)
                {
                    PersistenceService.getInstance().returnConnection(
                            connection);

                }
            }
            catch (Exception e)
            {
                CATEGORY
                        .error("Unable to return connection to the connection pool");
            }
        }
    }
}
