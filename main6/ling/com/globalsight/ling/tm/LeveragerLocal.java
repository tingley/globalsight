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

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.globalsight.diplomat.util.database.DbAccessException;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.everest.persistence.PersistenceService;
import com.globalsight.everest.persistence.StoredProcCaller;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.ling.tm2.leverage.LeverageDataCenter;
import com.globalsight.ling.tm2.leverage.LeverageMatchResults;
import com.globalsight.ling.tm2.leverage.LeverageMatches;
import com.globalsight.ling.tm2.leverage.LeverageOptions;
import com.globalsight.ling.tm2.leverage.LeveragedPageTu;
import com.globalsight.ling.tm2.leverage.LeveragedPageTuv;
import com.globalsight.ling.tm2.leverage.LeveragedTu;
import com.globalsight.ling.tm2.leverage.LeveragedTuv;
import com.globalsight.ling.tm2.leverage.MatchState;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.util.GlobalSightLocale;

/**
 * This class is the implementation of the Leverager Interface.
 * 
 * @see Leverager
 */
public class LeveragerLocal implements Leverager
{
    private static final Logger CATEGORY = Logger
            .getLogger(LeveragerLocal.class);

    // Cache of locale id and GlobalSightLocale map
    private static Hashtable s_localeCache = new Hashtable();

    public void leverageForReimport(SourcePage p_sourcePage,
            TargetLocaleLgIdsMapper p_localeLgIdsMapper,
            GlobalSightLocale p_sourceLocale,
            LeverageDataCenter p_leverageDataCenter) throws RemoteException,
            LingManagerException
    {
        try
        {
            Map tuvMap = getTuvMap(p_leverageDataCenter, p_sourcePage);

            Collection levMatchesList = null;
            if (tuvMap.size() > 0)
            {
                levMatchesList = leverageLgemForReimport(p_sourcePage,
                        p_localeLgIdsMapper, p_sourceLocale,
                        p_leverageDataCenter, tuvMap);
            }

            if (levMatchesList != null)
            {
                LeverageMatchResults levMatchResults = new LeverageMatchResults();

                for (Iterator it = levMatchesList.iterator(); it.hasNext();)
                {
                    LeverageMatches levMatches = (LeverageMatches) it.next();
                    levMatchResults.add(levMatches);
                }

                p_leverageDataCenter
                        .addLeverageResultsOfWholeSegment(levMatchResults);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new LingManagerException(e);
        }
    }

    /**
     * Leverages segments from the previous leverage group Returns a Collection
     * of LeverageMatches
     */
    private Collection leverageLgemForReimport(SourcePage p_sourcePage,
            TargetLocaleLgIdsMapper p_localeLgIdsMapper,
            GlobalSightLocale p_sourceLocale,
            LeverageDataCenter p_leverageDataCenter, Map p_tuvMap)
            throws LingManagerException
    {
        long time_PERFORMANCE = 0;
        Collection matchResults = new ArrayList();
        Connection connection = null;
        ResultSet resultSet = null;
        try
        {
            connection = PersistenceService.getInstance()
                    .getConnectionForImport();

            StoredProcedureParams params = new StoredProcedureParams();

            Iterator it = p_localeLgIdsMapper.getAllLocaleLgIdsPairs().iterator();
            long jobId = p_sourcePage.getJobId();
            // get LGEM matches per locale group that has the same
            // leverage group ids
            while (it.hasNext())
            {
                TargetLocaleLgIdsMapper.LocaleLgIdsPair lgIdsByLocale = (TargetLocaleLgIdsMapper.LocaleLgIdsPair) it
                        .next();

                params.setParamsForLgem(p_tuvMap.values(), p_sourceLocale,
                        lgIdsByLocale.getLocales(), lgIdsByLocale.getLgIds());

                time_PERFORMANCE = System.currentTimeMillis();

                // get LGEM hits
                resultSet = findLevGroupExactMatches(connection, params, jobId);
                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("Performance:: findLevGroupExactMatches for "
                            + p_sourcePage.getExternalPageId() + " time = "
                            + (System.currentTimeMillis() - time_PERFORMANCE));                    
                }

                LgemPostProcessor lgemProcessor = new LgemPostProcessor(
                        p_tuvMap, p_sourcePage.getId(), p_tuvMap.keySet(),
                        lgIdsByLocale.getLocales());

                time_PERFORMANCE = System.currentTimeMillis();

                matchResults.addAll(postProcessAndSave(resultSet,
                        lgemProcessor,
                        p_leverageDataCenter.getLeverageOptions()));

                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("Performance:: lgem postProcessAndSave for "
                            + p_sourcePage.getExternalPageId() + " time = "
                            + (System.currentTimeMillis() - time_PERFORMANCE));                    
                }
            }
        }
        catch (Exception e)
        {
            throw new LingManagerException(e);
        }
        finally
        {
            DbUtil.silentClose(resultSet);
            returnConnection(connection);
        }

        return matchResults;
    }

    private ResultSet findLevGroupExactMatches(Connection p_connection,
            StoredProcedureParams p_params, long p_jobId)
            throws LingManagerException
    {
        ResultSet results = null;

        try
        {
            results = StoredProcCaller.findReimportMatches(p_connection,
                    p_params.getNumberForReimport(),
                    p_params.getStringParams(), p_jobId);
        }
        catch (PersistenceException ex)
        {
            CATEGORY.error("error with stored procedure "
                    + StoredProcCaller.LGEM_SP, ex);
            throw new LingManagerException(ex);
        }

        return results;
    }

    private Collection postProcessAndSave(ResultSet p_resultSet,
            BasePostProcessor p_postProcessor, LeverageOptions p_leverageOptions)
            throws LingManagerException
    {
        // we didn't find any matches
        if (p_resultSet == null)
        {
            return new ArrayList();
        }

        Collection matchResults = new ArrayList();
        try
        {
            long prevId = -1;
            while (p_resultSet.next())
            {
                CandidateMatch cm = populateCandidateMatch(
                        p_postProcessor.getMatchType(), p_resultSet);

                if (prevId != cm.getOriginalSourceId())
                {
                    Collection cmList = p_postProcessor.postProcess(prevId);
                    if (cmList != null && cmList.size() > 0)
                    {
                        LeverageMatches levMatches = convertCandidateMatchesToLeverageMatches(
                                cmList, p_postProcessor.getOriginalTuv(prevId),
                                p_leverageOptions);
                        matchResults.add(levMatches);
                    }

                    prevId = cm.getOriginalSourceId();
                }

                p_postProcessor.add(cm);
            }

            Collection cmList = p_postProcessor.postProcess(prevId);
            if (cmList != null && cmList.size() > 0)
            {
                LeverageMatches levMatches = convertCandidateMatchesToLeverageMatches(
                        cmList, p_postProcessor.getOriginalTuv(prevId),
                        p_leverageOptions);
                matchResults.add(levMatches);
            }

            // p_postProcessor.saveHits();
        }
        catch (SQLException ex)
        {
            CATEGORY.error("error traversing result set: ", ex);
            throw new LingManagerException(ex);
        }
        catch (DbAccessException ex)
        {
            CATEGORY.error("error getting CLOB: ", ex);
            throw new LingManagerException(ex);
        }
        return matchResults;
    }

    private CandidateMatch populateCandidateMatch(int p_matchType,
            ResultSet p_resultSet) throws LingManagerException, SQLException,
            DbAccessException
    {
        CandidateMatch cm = new CandidateMatch();

        // ids
        cm.setOriginalSourceId(p_resultSet.getLong("orig_src_id"));
        cm.setMatchedTuId(p_resultSet.getLong("tu_id"));
        cm.setMatchedSourceId(p_resultSet.getLong("match_src_id"));
        cm.setMatchedTargetId(p_resultSet.getLong("match_tgt_id"));

        // fuzzy score - default is 100
        short fuzzyScore = (short) (p_resultSet.getDouble("fuzzy_score") * 100.0);

        if (!p_resultSet.wasNull())
        {
            cm.setScoreNum(fuzzyScore);
        }

        // matched source segment
        String src = p_resultSet.getString("src_segment_string");
        if (src == null || src.length() <= 0)
        {
            // must be a CLOB
            cm.setGxmlSource(p_resultSet.getString("src_segment_clob"));
        }
        else
        {
            cm.setGxmlSource(src);
        }

        // matched target segment
        String trg = p_resultSet.getString("segment_string");
        if (trg == null || trg.length() <= 0)
        {
            // must be a CLOB
            cm.setGxmlTarget(p_resultSet.getString("segment_clob"));
        }
        else
        {
            cm.setGxmlTarget(trg);
        }

        // matched target locale
        GlobalSightLocale loc = getLocaleById(p_resultSet.getLong("locale_id"));

        cm.setTargetLocale(loc);

        // matched target segment state
        cm.setState(p_resultSet.getString("state"));

        // match type: EXACT, FUZZY or LGEM
        cm.setMatchType(p_matchType);

        // set order num to 1 tentatively
        cm.setOrderNum((short) 1);

        // set latest modificaton timestamp of target tuv
        cm.setTimestamp(p_resultSet.getTimestamp("timestamp"));

        cm.setFormat(p_resultSet.getString("format"));
        cm.setType(p_resultSet.getString("type"));

        String localize_type = p_resultSet.getString("localize_type");
        cm.setTranslatable(localize_type.equals("T"));

        return cm;
    }

    private GlobalSightLocale getLocaleById(long p_localeId)
            throws LingManagerException
    {
        Long localeId = new Long(p_localeId);
        GlobalSightLocale loc = (GlobalSightLocale) s_localeCache.get(localeId);

        if (loc == null)
        {
            try
            {
                // query LocaleManager
                loc = ServerProxy.getLocaleManager().getLocaleById(p_localeId);
            }
            catch (Exception ex)
            {
                CATEGORY.error("error in LocaleManager: ", ex);
                throw new LingManagerException(ex);
            }

            s_localeCache.put(localeId, loc);
        }

        return loc;
    }

    /**
     * Retrieves original source TUVs from LeverageDataCenter object
     * 
     * @param p_leverageDataCenter
     *            LeverageDataCenter object. This contains source segments of a
     *            page already.
     * @param p_sourcePage
     *            source page
     * @return map of tuv id and BaseTmTuv Key: tuv id (Long) Value: BaseTmTuv
     *         object
     */
    private Map getTuvMap(LeverageDataCenter p_leverageDataCenter,
            SourcePage p_sourcePage) throws Exception
    {
        Map tuvMap = new HashMap();
        Set sourceSegments = p_leverageDataCenter
                .getOriginalWholeSegments(p_sourcePage.getJobId());

        for (Iterator it = sourceSegments.iterator(); it.hasNext();)
        {
            BaseTmTuv tm2Tuv = (BaseTmTuv) it.next();
            tuvMap.put(new Long(tm2Tuv.getId()), tm2Tuv);
        }

        return tuvMap;
    }

    private LeverageMatches convertCandidateMatchesToLeverageMatches(
            Collection p_candidateMatchList, BaseTmTuv p_originalSegment,
            LeverageOptions p_leverageOptions)
    {
        LeverageMatches leverageMatches = new LeverageMatches(
                p_originalSegment, p_leverageOptions);

        HashMap tuIdTuMap = new HashMap();
        for (Iterator it = p_candidateMatchList.iterator(); it.hasNext();)
        {
            CandidateMatch cm = (CandidateMatch) it.next();

            Long matchedTuId = new Long(cm.getMatchedTuId());
            LeveragedTu tu = (LeveragedTu) tuIdTuMap.get(matchedTuId);

            if (tu == null)
            {
                tu = getTuWithSourceFromCandidateMatch(cm,
                        p_originalSegment.getLocale());
                tuIdTuMap.put(matchedTuId, tu);
            }

            LeveragedTuv targetTuv = getTargetTuvFromCandidateMatch(cm);
            tu.addTuv(targetTuv);
        }

        for (Iterator it = tuIdTuMap.values().iterator(); it.hasNext();)
        {
            leverageMatches.add((LeveragedTu) it.next());
        }

        return leverageMatches;
    }

    private LeveragedTu getTuWithSourceFromCandidateMatch(CandidateMatch p_cm,
            GlobalSightLocale p_sourceLocale)
    {
        LeveragedPageTu tu = new LeveragedPageTu(p_cm.getMatchedTuId(), 0 /*
                                                                           * tm
                                                                           * id
                                                                           */,
                p_cm.getFormat(), p_cm.getType(), p_cm.isTranslatable(),
                p_sourceLocale);

        tu.setMatchState(MatchState.UNVERIFIED_EXACT_MATCH);
        tu.setScore(100);
        tu.setMatchTableType(LeveragedTu.PAGE_JOB_TABLE);

        LeveragedPageTuv sourceTuv = new LeveragedPageTuv(
                p_cm.getMatchedSourceId(), p_cm.getGxmlSource(), p_sourceLocale);

        tu.addTuv(sourceTuv);

        return tu;
    }

    private LeveragedTuv getTargetTuvFromCandidateMatch(CandidateMatch p_cm)
    {
        LeveragedPageTuv tuv = new LeveragedPageTuv(p_cm.getMatchedTargetId(),
                p_cm.getGxmlTarget(), p_cm.getTargetGlobalSightLocale());

        return tuv;
    }

    static private void returnConnection(Connection p_connection)
    {
        if (p_connection != null)
        {
            try
            {
                PersistenceService.getInstance().returnConnection(p_connection);
            }
            catch (Throwable e)
            {
                CATEGORY.error("Error returning a connection to the pool");
            }
        }
    }
}
