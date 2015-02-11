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

package com.globalsight.everest.integration.ling.tm2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.persistence.tuv.SegmentTuTuvCacheManager;
import com.globalsight.everest.persistence.tuv.SegmentTuvUtil;
import com.globalsight.everest.persistence.tuv.TuvQueryConstants;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.tuv.TuImpl;
import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.ling.docproc.extractor.xliff.Extractor;
import com.globalsight.ling.inprogresstm.leverage.LeveragedInProgressTuv;
import com.globalsight.ling.tm.LeverageMatchLingManager;
import com.globalsight.ling.tm.LeverageMatchType;
import com.globalsight.ling.tm.LeverageSegment;
import com.globalsight.ling.tm.LingManagerException;
import com.globalsight.ling.tm2.SegmentTmTu;
import com.globalsight.ling.tm2.SegmentTmTuv;
import com.globalsight.ling.tm2.TmCoreManager;
import com.globalsight.ling.tm2.TmUtil;
import com.globalsight.ling.tm2.leverage.LeverageDataCenter;
import com.globalsight.ling.tm2.leverage.LeverageMatches;
import com.globalsight.ling.tm2.leverage.LeverageOptions;
import com.globalsight.ling.tm2.leverage.LeverageUtil;
import com.globalsight.ling.tm2.leverage.LeveragedSegmentTuv;
import com.globalsight.ling.tm2.leverage.LeveragedTu;
import com.globalsight.ling.tm2.leverage.LeveragedTuv;
import com.globalsight.ling.tm2.leverage.Leverager;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.gxml.GxmlElement;
import com.globalsight.util.gxml.GxmlNames;

/**
 * Implementation of com.globalsight.ling.tm.LeverageMatchLingManager.
 */
public class LeverageMatchLingManagerLocal implements LeverageMatchLingManager
{
    static private final Logger c_logger = Logger
            .getLogger(LeverageMatchLingManagerLocal.class);

    private static final String LM_TABLE_NAME_PLACE_HOLDER = TuvQueryConstants.LM_TABLE_PLACEHOLDER;

    private static final String SELECT_COLUMNS = "SELECT "
            + " SOURCE_PAGE_ID, ORIGINAL_SOURCE_TUV_ID, SUB_ID, MATCHED_TEXT_STRING, "
            + " TARGET_LOCALE_ID, MATCH_TYPE, ORDER_NUM, SCORE_NUM, MATCHED_TUV_ID, "
            + " MATCHED_TABLE_TYPE, PROJECT_TM_INDEX, TM_ID, TM_PROFILE_ID, MT_NAME, "
            + " MATCHED_ORIGINAL_SOURCE, JOB_DATA_TU_ID FROM ";

    /**
     * Used to get all exact matches despite of where they come from.
     */
    private static final String EXACT_LEVERAGE_MATCH_SQL = SELECT_COLUMNS
            + LM_TABLE_NAME_PLACE_HOLDER + " WHERE SOURCE_PAGE_ID = ?"
            + " AND TARGET_LOCALE_ID = ?" + " AND SCORE_NUM = 100";

    private static final String FUZZY_LEVERAGE_MATCH_SQL = SELECT_COLUMNS
            + LM_TABLE_NAME_PLACE_HOLDER + "  where SOURCE_PAGE_ID = ? "
            + "  and TARGET_LOCALE_ID = ? " + "  and SCORE_NUM < 100 "
            + "  and MATCH_TYPE != 'STATISTICS_MATCH'"
            + "  and MATCH_TYPE != 'NOT_A_MATCH'";

    private static final String ALL_LEVERAGE_MATCHES_FOR_TUV_SQL = SELECT_COLUMNS
            + LM_TABLE_NAME_PLACE_HOLDER
            + " WHERE ORIGINAL_SOURCE_TUV_ID = ? "
            + " AND TARGET_LOCALE_ID = ? "
            + " AND SUB_ID = ? "
            + " AND MATCH_TYPE != 'STATISTICS_MATCH' "
            + " AND MATCH_TYPE != 'NOT_A_MATCH' "
            + " AND TM_ID in (tmId_list) ";

    private static final String BEST_LEVERAGE_MATCH_SQL = SELECT_COLUMNS
            + LM_TABLE_NAME_PLACE_HOLDER + " WHERE SOURCE_PAGE_ID = ? "
            + " AND TARGET_LOCALE_ID = ? "
            + " ORDER BY ORIGINAL_SOURCE_TUV_ID, ORDER_NUM ";

    private static final String LEVERAGE_MATCH_FOR_OFFLINE_SQL = SELECT_COLUMNS
            + LM_TABLE_NAME_PLACE_HOLDER + " WHERE SOURCE_PAGE_ID = ? "
            + " AND TARGET_LOCALE_ID = ? "
            + " AND MATCH_TYPE != 'STATISTICS_MATCH'"
            + " AND MATCH_TYPE != 'NOT_A_MATCH'"
            + " ORDER BY ORIGINAL_SOURCE_TUV_ID, ORDER_NUM ";

    /**
     * Get max order_num by tuvId, targetLocaleId and subId.
     */
    private static final String MAX_ORDER_NUM = "select max(order_num) from "
            + LM_TABLE_NAME_PLACE_HOLDER + " WHERE ORDER_NUM > 0 "
            + " AND ORDER_NUM < " + TmCoreManager.LM_ORDER_NUM_START_REMOTE_TM
            + " AND ORIGINAL_SOURCE_TUV_ID = ? " + " AND TARGET_LOCALE_ID = ? "
            + " AND SUB_ID = ? ";

    private static final String BEST_MATCH_SCORE = "select max(score_num) from "
            + LM_TABLE_NAME_PLACE_HOLDER
            + " WHERE original_source_tuv_id = ? "
            + " AND target_locale_id = ? " + " AND sub_id = ? ";

    private static final String INSERT_INTO_LEVERAGE_MATCH_SQL = "INSERT INTO "
            + LM_TABLE_NAME_PLACE_HOLDER
            + " (SOURCE_PAGE_ID, ORIGINAL_SOURCE_TUV_ID, SUB_ID, MATCHED_TEXT_STRING, TARGET_LOCALE_ID, "
            + "  MATCH_TYPE, ORDER_NUM, SCORE_NUM, MATCHED_TUV_ID, MATCHED_TABLE_TYPE, "
            + "  PROJECT_TM_INDEX, TM_ID, TM_PROFILE_ID, MT_NAME, MATCHED_ORIGINAL_SOURCE, "
            + "  JOB_DATA_TU_ID) "
            + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String DELETE_LM_BY_SOURCE_PAGE_ID =
            "delete from " + LM_TABLE_NAME_PLACE_HOLDER + " WHERE source_page_id = ? ";

    /**
     * Indicate whether MT matches should be returned in the query results.
     * Default is true. For word-count statistics,if for WorldServer xliff file,
     * this should be false.
     * 
     * This requirement is from Don: MT translations for WS XLF should not
     * impact word-count statistics.
     */
    private boolean isIncludeMtMatches = true;

    public boolean isIncludeMtMatches()
    {
        return isIncludeMtMatches;
    }

    public void setIncludeMtMatches(boolean isIncludeMtMatches)
    {
        this.isIncludeMtMatches = isIncludeMtMatches;
    }

    public LeverageMatchLingManagerLocal()
    {
        super();
    }

    public HashMap<Long, Set<LeverageMatch>> getFuzzyMatches(
            Long p_sourcePageId, Long p_targetLocaleId)
            throws LingManagerException
    {
        List<LeverageMatch> leverageMatches = new ArrayList<LeverageMatch>();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try
        {
            String lmTableName = SegmentTuTuvCacheManager
                    .getLMTableNameJobDataIn(p_sourcePageId);
            String sql = FUZZY_LEVERAGE_MATCH_SQL.replace(
                    LM_TABLE_NAME_PLACE_HOLDER, lmTableName);

            connection = DbUtil.getConnection();
            ps = connection.prepareStatement(sql);
            ps.setLong(1, p_sourcePageId);
            ps.setLong(2, p_targetLocaleId);

            rs = ps.executeQuery();

            leverageMatches = convertToLeverageMatches(rs);
        }
        catch (Exception ex)
        {
            c_logger.error("getFuzzyMatches() error", ex);
            throw new LingManagerException(ex);
        }
        finally
        {
            DbUtil.silentClose(rs);
            DbUtil.silentClose(ps);
            DbUtil.silentReturnConnection(connection);
        }

        return getLeverageMatchMap(leverageMatches);
    }

    /**
     * Get static leverage matches from DB store for specified source tuvID,
     * target locale ID, subID etc. The matches are stored when creating job.
     */
    public SortedSet<LeverageMatch> getTuvMatches(Long p_sourceTuvId,
            Long p_targetLocaleId, String p_subId, boolean isTmProcedence,
            long companyId, boolean isJobDataMigrated, long... tmIds)
            throws LingManagerException
    {
        TreeSet<LeverageMatch> set = null;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try
        {
            conn = DbUtil.getConnection();
            String lmTableName = SegmentTuTuvCacheManager
                    .getLMTableNameJobDataIn(companyId, isJobDataMigrated);
            String sql = ALL_LEVERAGE_MATCHES_FOR_TUV_SQL.replace(
                    LM_TABLE_NAME_PLACE_HOLDER, lmTableName);
            if (isTmProcedence)
            {
                sql += " ORDER BY ORIGINAL_SOURCE_TUV_ID asc, SCORE_NUM desc, MATCHED_TEXT_STRING asc";
            }
            else
            {
                sql += " ORDER BY SCORE_NUM desc, ORIGINAL_SOURCE_TUV_ID asc, MATCHED_TEXT_STRING asc";
            }

            StringBuilder tmIdWhereCondition = new StringBuilder();
            if (tmIds != null && tmIds.length != 0)
            {
                for (int i = 0; i < tmIds.length; i++)
                {
                    tmIdWhereCondition.append(tmIds[i]);
                    if (i < tmIds.length - 1)
                    {
                        tmIdWhereCondition.append(", ");
                    }
                }
                sql = sql
                        .replaceAll("tmId_list", tmIdWhereCondition.toString());
            }
            else
            {
                sql = sql.replace("AND TM_ID in (tmId_list) ", "");
            }

            ps = conn.prepareStatement(sql);
            ps.setLong(1, p_sourceTuvId);
            ps.setLong(2, p_targetLocaleId);
            ps.setString(3, p_subId);

            rs = ps.executeQuery();
            List<LeverageMatch> list = new ArrayList<LeverageMatch>();
            list = convertToLeverageMatches(rs);
            set = new TreeSet<LeverageMatch>(list);
        }
        catch (Exception ex)
        {
            c_logger.error("getTuvMatches() error", ex);
            throw new LingManagerException(ex);
        }
        finally
        {
            DbUtil.silentClose(rs);
            DbUtil.silentClose(ps);
            DbUtil.silentReturnConnection(conn);
        }

        return set;
    }

    @SuppressWarnings("unchecked")
    public HashMap<Long, ArrayList<LeverageSegment>> getExactMatchesWithSetInside(
            Long p_sourcePageId, Long p_targetLocaleId, int model,
            TranslationMemoryProfile tmProfile)
    {
        SourcePage sp = null;
        try
        {
            sp = ServerProxy.getPageManager().getSourcePage(p_sourcePageId); 
        }
        catch (Exception e)
        {
            c_logger.error(e.getMessage(), e);
        }
        long companyId = sp.getCompanyId();
        HashMap<Long, ArrayList<LeverageSegment>> result = new HashMap<Long, ArrayList<LeverageSegment>>();

        Collection<LeverageMatch> matches = getExactLeverageMatches(
                p_sourcePageId, p_targetLocaleId);
        // return empty map if no match
        if (matches == null || matches.size() == 0)
        {
            return result;
        }
        
        // create a new list for all LeverageMatch
        ArrayList<LeverageMatch> allLms = new ArrayList<LeverageMatch>();
        allLms.addAll(matches);
        matches.clear();

        // get root match with sub match
        ArrayList<LeverageMatch> lmWithSameSrcTuv = new ArrayList<LeverageMatch>();
        ArrayList<LeverageMatch> lmOrderBySub = new ArrayList<LeverageMatch>();
        ArrayList<LeverageMatch> lmOrderBySubLast = new ArrayList<LeverageMatch>();
        // int rootSubId = Integer.parseInt(SegmentTmTu.ROOT);
        int handled = 0;
        int size = allLms.size();

        // handle LeverageMatch by OriginalSourceTuvId
        while (handled < size)
        {
            // pick up one src tuv to handle
            Long srcTuvHandling = -1l;
            for (LeverageMatch lm : allLms)
            {
                Long oriSrcTuvId = new Long(lm.getOriginalSourceTuvId());
                if (srcTuvHandling == -1l)
                {
                    srcTuvHandling = oriSrcTuvId;
                }

                if (oriSrcTuvId.equals(srcTuvHandling))
                {
                    lmWithSameSrcTuv.add(lm);
                    ++handled;
                }
            }

            TuvImpl tuv = null;
            try
            {
                tuv = SegmentTuvUtil.getTuvById(srcTuvHandling.longValue(),
                        companyId);
            }
            catch (Exception e)
            {
                c_logger.error(e.getMessage(), e);
            }

            if (tuv == null)
            {
                continue;
            }

            // get sub id list for this match
            ArrayList<Integer> subidList = new ArrayList<Integer>();
            for (int i = 0; i < lmWithSameSrcTuv.size(); i++)
            {
                LeverageMatch lm = lmWithSameSrcTuv.get(i);
                String subId = lm.getSubId();
                int subid = Integer.parseInt(subId);

                if (!subidList.contains(subid))
                    subidList.add(subid);
            }
            Collections.sort(subidList);

            // must have root sub (Not required,ignore this to partial applying)
            // if (!subidList.contains(rootSubId))
            // {
            // allLms.removeAll(lmWithSameSrcTuv);
            // lmWithSameSrcTuv.clear();
            // continue;
            // }

            // pick up one LeverageMatch list order by sub id to match
            while (!lmWithSameSrcTuv.isEmpty())
            {
                // pick up one LeverageMatch list order by sub id, like 0 1 2 3
                for (int i = 0; i < subidList.size(); i++)
                {
                    Integer subid = subidList.get(i);
                    LeverageMatch lmWithSameSub = null;
                    for (LeverageMatch lm : lmWithSameSrcTuv)
                    {
                        String lmSubId = lm.getSubId();

                        if ((subid + "").equals(lmSubId))
                        {
                            lmWithSameSub = lm;
                            break;
                        }
                    }

                    // break if can not found current sub id
                    if (lmWithSameSub == null && lmOrderBySubLast.isEmpty())
                    {
                        allLms.removeAll(lmWithSameSrcTuv);
                        lmWithSameSrcTuv.clear();
                        break;
                    }

                    if (lmWithSameSub == null)
                    {
                        lmOrderBySub.add(lmOrderBySubLast.get(i));
                    }
                    else
                    {
                        lmOrderBySub.add(lmWithSameSub);
                        lmWithSameSrcTuv.remove(lmWithSameSub);
                        allLms.remove(lmWithSameSub);
                    }
                }

                lmOrderBySubLast.clear();

                // add match
                if (lmOrderBySub.size() == subidList.size())
                {
                    String matchedGxml = null;
                    LeverageMatch rootLm = lmOrderBySub.get(0);
                    Map<String, String> segmentMap = new HashMap<String, String>();
                    boolean hasExactMatchesForAllSubIds = true;

                    for (LeverageMatch lm : lmOrderBySub)
                    {
                        segmentMap.put(lm.getSubId(), lm.getMatchedText());
                        lmOrderBySubLast.add(lm);
                    }

                    try
                    {
                        String segmengString = tuv.getSegmentString();
                        segmengString = segmengString != null ? segmengString
                                : tuv.getSegmentClob();
                        matchedGxml = TmUtil.composeCompleteText(segmengString,
                                segmentMap);

                        List<GxmlElement> subflows = tuv
                                .getSubflowsAsGxmlElements();
                        if (subflows != null && subflows.size() > 0)
                        {
                            for (GxmlElement sub : subflows)
                            {
                                String subId = sub
                                        .getAttribute(GxmlNames.SUB_ID);
                                if (segmentMap.get(subId) == null)
                                {
                                    hasExactMatchesForAllSubIds = false;
                                    break;
                                }
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        /*
                         * Change error info into warning info, only main
                         * segment and subflows are both exact match can we get
                         * a normal matchedGxml. If either of them fails, we
                         * throw a warning.
                         */
                        c_logger.warn(e);
                    }

                    if (matchedGxml != null && rootLm != null)
                    {
                        String matchType = null;
                        if (hasExactMatchesForAllSubIds)
                        {
                            matchType = LeverageDataCenter.getTuvState(rootLm
                                    .getMatchType());
                        }
                        else
                        {
                            matchType = LeverageMatchType.UNKNOWN_NAME;
                        }
                        LeverageSegment ls = new LeverageSegment(matchedGxml,
                                matchType, rootLm.getModifyDate(),
                                rootLm.getProjectTmIndex(),
                                rootLm.getMatchedSid(),
                                rootLm.getMatchedTuvId());
                        ls.setOrgSid(rootLm.getOrgSid(companyId));
                        ArrayList<LeverageSegment> l = (ArrayList<LeverageSegment>) result
                                .get(new Long(rootLm.getOriginalSourceTuvId()));
                        if (l != null && l.size() != 0)
                        {
                            l.add(ls);
                            Collections.sort(l, new ComparatorByModifyDate(
                                    model, tmProfile, companyId));
                            result.put(srcTuvHandling, l);
                        }
                        else
                        {
                            l = new ArrayList<LeverageSegment>();
                            l.add(ls);
                            result.put(srcTuvHandling, l);
                        }
                    }
                }

                lmOrderBySub.clear();
            }

            // remove handled LM
            allLms.removeAll(lmWithSameSrcTuv);
            lmWithSameSrcTuv.clear();
            lmOrderBySub.clear();
        }

        return result;
    }

    public HashMap<Long, LeverageSegment> getExactMatches(Long p_sourcePageId,
            Long p_targetLocaleId) throws LingManagerException
    {
        Collection<LeverageMatch> matches = getExactLeverageMatches(
                p_sourcePageId, p_targetLocaleId);

        HashMap<Long, LeverageSegment> result = new HashMap<Long, LeverageSegment>();
        for (LeverageMatch lm : matches)
        {
            String gxml = lm.getMatchedText();

            // LeverageSegment ls = new LeverageSegment(gxml, lm.getMatchType(),
            // lm.getModifyDate(), lm.getProjectTmIndex());
            LeverageSegment ls = new LeverageSegment(gxml,
                    LeverageDataCenter.getTuvState(lm.getMatchType()),
                    lm.getModifyDate(), lm.getProjectTmIndex(),
                    lm.getMatchedSid(), lm.getMatchedTuvId());
            result.put(new Long(lm.getOriginalSourceTuvId()), ls);
        }

        return result;
    }

    public Map<Long, Set<LeverageMatch>> getExactMatchesForDownLoadTmx(
            Long p_sourcePageId, Long p_targetLocaleId)
    {
        List<LeverageMatch> leverageMatches = getExactLeverageMatches(
                p_sourcePageId, p_targetLocaleId);

        return getLeverageMatchMap(leverageMatches);
    }

    private List<LeverageMatch> getExactLeverageMatches(Long p_sourcePageId,
            Long p_targetLocaleId) throws LingManagerException
    {
        List<LeverageMatch> leverageMatches = new ArrayList<LeverageMatch>();

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try
        {
            connection = DbUtil.getConnection();

            String lmTableName = SegmentTuTuvCacheManager
                    .getLMTableNameJobDataIn(p_sourcePageId);
            String sql = EXACT_LEVERAGE_MATCH_SQL.replace(
                    LM_TABLE_NAME_PLACE_HOLDER, lmTableName);

            ps = connection.prepareStatement(sql);
            ps.setLong(1, p_sourcePageId);
            ps.setLong(2, p_targetLocaleId);
            rs = ps.executeQuery();
            leverageMatches = convertToLeverageMatches(rs);
        }
        catch (Exception ex)
        {
            c_logger.error("getExactLeverageMatches() error", ex);
            throw new LingManagerException(ex);
        }
        finally
        {
            DbUtil.silentClose(rs);
            DbUtil.silentClose(ps);
            DbUtil.silentReturnConnection(connection);
        }

        return leverageMatches;
    }

    /**
     * Retrieves a mapping of tuv id + sub id to match type in a given leveraged
     * target page.
     */
    public MatchTypeStatistics getMatchTypesForStatistics(Long p_sourcePageId,
            Long p_targetLocaleId, int p_levMatchThreshold)
            throws LingManagerException
    {
        List<LeverageMatch> leverageMatches = new ArrayList<LeverageMatch>();

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        long companyId = -1l;

        try
        {
            companyId = SegmentTuTuvCacheManager
                    .getCompanyIdBySourcePageId(p_sourcePageId);
            connection = DbUtil.getConnection();

            String lmTableName = SegmentTuTuvCacheManager
                    .getLMTableNameJobDataIn(p_sourcePageId);
            String sql = BEST_LEVERAGE_MATCH_SQL.replace(
                    LM_TABLE_NAME_PLACE_HOLDER, lmTableName);

            ps = connection.prepareStatement(sql);
            ps.setLong(1, p_sourcePageId);
            ps.setLong(2, p_targetLocaleId);
            rs = ps.executeQuery();
            leverageMatches = convertToLeverageMatches(rs);
        }
        catch (Exception ex)
        {
            c_logger.error("getMatchTypesForStatistics() error", ex);
            throw new LingManagerException(ex);
        }
        finally
        {
            DbUtil.silentClose(rs);
            DbUtil.silentClose(ps);
            DbUtil.silentReturnConnection(connection);
        }

        HashMap<String, LeverageMatch> leveragematchesMap = new HashMap<String, LeverageMatch>();
        try
        {
            // remove lower score_num record
            for (LeverageMatch match : leverageMatches)
            {
                LeverageMatch cloneMatch = new LeverageMatch(match);
                long originalSourceTuvId = cloneMatch.getOriginalSourceTuvId();

                // For WS XLF file,MT translation should NOT impact
                // word-count,displayed color.
                if (!isIncludeMtMatches)
                {
                    try
                    {
                        TuImpl tu = (TuImpl) ServerProxy
                                .getTuvManager()
                                .getTuvForSegmentEditor(originalSourceTuvId,
                                        companyId).getTu(companyId);
                        String translationType = tu.getXliffTranslationType();
                        float tm_score = Float.parseFloat(tu.getIwsScore());
                        if (Extractor.IWS_TRANSLATION_MANUAL
                                .equalsIgnoreCase(translationType))
                        {
                            cloneMatch.setScoreNum(100);
                        }
                        else if (Leverager.MT_PRIORITY == cloneMatch
                                .getProjectTmIndex())
                        {
                            cloneMatch.setScoreNum(tm_score);
                        }
                    }
                    catch (Exception e)
                    {
                    }
                }

                float scoreNum = cloneMatch.getScoreNum();
                String subId = cloneMatch.getSubId();
                String idKey = MatchTypeStatistics.makeKey(originalSourceTuvId,
                        subId);
                LeverageMatch lm = (LeverageMatch) leveragematchesMap
                        .get(idKey);
                if (lm != null)
                {
                    if (scoreNum == 100)
                    {
                        if ((LeverageUtil.compareSid(lm, cloneMatch, companyId) > 0 && cloneMatch
                                .getOrderNum() != -1)
                                || lm.getScoreNum() < scoreNum)
                        {
                            leveragematchesMap.remove(idKey);
                            leveragematchesMap.put(idKey, cloneMatch);
                        }
                    }
                    else if (lm.getScoreNum() < scoreNum)
                    {
                        leveragematchesMap.remove(idKey);
                        leveragematchesMap.put(idKey, cloneMatch);
                    }
                }
                else
                {
                    leveragematchesMap.put(idKey, cloneMatch);
                }
            }
        }
        catch (Exception ex)
        {
            c_logger.error("getMatchTypesForStatistics() error", ex);
            throw new LingManagerException(ex);
        }

        MatchTypeStatistics result = new MatchTypeStatistics(
                p_levMatchThreshold);

        // set the match type with the found leverage matches
        Collection<LeverageMatch> leverageMatches2 = leveragematchesMap.values();
        List<String> list = new ArrayList<String>();
        for (LeverageMatch match : leverageMatches2)
        {
            String key = MatchTypeStatistics.makeKey(
                    match.getOriginalSourceTuvId(), match.getSubId());
            if (!list.contains(key))
            {
                result.addMatchType(match);
                list.add(key);
            }
        }

        return result;
    }

    /**
     * Delete leverage matches for specified source page.
     */
    public void deleteLeverageMatches(Long p_sourcePageId)
            throws LingManagerException
    {
        Connection connection = null;
        PreparedStatement ps = null;
        try
        {
            connection = DbUtil.getConnection();
            connection.setAutoCommit(false);

            String lmTableName = SegmentTuTuvCacheManager
                    .getLMTableNameJobDataIn(p_sourcePageId);
            String sql = DELETE_LM_BY_SOURCE_PAGE_ID.replace(
                    LM_TABLE_NAME_PLACE_HOLDER, lmTableName);

            ps = connection.prepareStatement(sql.toString());
            ps.executeUpdate();
            connection.commit();
        }
        catch (Exception ex)
        {
            c_logger.error(
                    "Failed to delete leverage matches by sourcePageId : "
                            + p_sourcePageId, ex);
            throw new LingManagerException(ex);
        }
        finally
        {
            DbUtil.silentClose(ps);
            DbUtil.silentReturnConnection(connection);
        }
    }

    /**
     * Delete leverage matches for specified TUV.
     * 
     * @param p_OriginalSourceTuvId
     *            -- Can not be null
     * @param p_subId
     *            -- Can be null
     * @param p_targetLocaleId
     *            -- Can be null
     * @param p_orderNum
     *            -- Can be null
     */
    public void deleteLeverageMatches(Long p_OriginalSourceTuvId,
            String p_subId, Long p_targetLocaleId, Long p_orderNum,
            Long p_companyId, boolean p_isJobDataMigrated)
            throws LingManagerException
    {
        Connection connection = null;
        PreparedStatement ps = null;

        try
        {
            connection = DbUtil.getConnection();
            connection.setAutoCommit(false);

            // Delete
            String sql = getDeleteSql(p_OriginalSourceTuvId, p_subId,
                    p_targetLocaleId, p_orderNum, p_companyId,
                    p_isJobDataMigrated);

            ps = connection.prepareStatement(sql);
            ps.executeUpdate();

            connection.commit();
        }
        catch (Exception ex)
        {
            c_logger.error(
                    "Failed to delete leverage matches by originalSourceTuvID : "
                            + p_OriginalSourceTuvId, ex);
            throw new LingManagerException(ex);
        }
        finally
        {
            DbUtil.silentClose(ps);
            DbUtil.silentReturnConnection(connection);
        }
    }

    private String getDeleteSql(Long p_OriginalSourceTuvId, String p_subId,
            Long p_targetLocaleId, Long p_orderNum, Long p_companyId,
            boolean p_isJobDataMigrated)
    {
        StringBuilder sql = new StringBuilder();

        String lmTableName = SegmentTuTuvCacheManager.getLMTableNameJobDataIn(
                p_companyId, p_isJobDataMigrated);
        sql.append("DELETE FROM ").append(lmTableName).append(" ");
        // "p_OriginalSourceTuvId" can't be null
        sql.append(" WHERE ORIGINAL_SOURCE_TUV_ID = ").append(
                p_OriginalSourceTuvId);

        if (p_subId != null)
        {
            sql.append(" AND SUB_ID = ").append(p_subId);
        }
        if (p_targetLocaleId != null)
        {
            sql.append(" AND TARGET_LOCALE_ID = ").append(p_targetLocaleId);
        }
        if (p_orderNum != null)
        {
            sql.append(" AND ORDER_NUM = ").append(p_orderNum);
        }

        return sql.toString();
    }

    /**
     * Returns true if a specified state indicates that the match is copied into
     * a target segment.
     * 
     * @param p_lingManagerMatchType
     *            match state defined in this class
     */
    public boolean isMatchCopied(int p_lingManagerMatchType)
    {
        boolean result = false;

        if (p_lingManagerMatchType == EXACT
                || p_lingManagerMatchType == UNVERIFIED)
        {
            result = true;
        }

        return result;
    }

    /**
     * Get a hash map from a collection of LeverageMatch (sourceTuvId as key).
     */
    private HashMap<Long, Set<LeverageMatch>> getLeverageMatchMap(
            Collection<LeverageMatch> p_leverageMatches)
    {
        // Put all the LeverageMatch in HashMap grouping by original Tuv id
        HashMap<Long, Set<LeverageMatch>> result = new HashMap<Long, Set<LeverageMatch>>();

        for (LeverageMatch match : p_leverageMatches)
        {
            Long key = new Long(match.getOriginalSourceTuvId());
            Set<LeverageMatch> set = (TreeSet<LeverageMatch>) result.get(key);

            if (set == null)
            {
                set = new TreeSet<LeverageMatch>();
                result.put(key, set);
            }

            // TreeSet sorts the elements
            set.add(match);
        }
        return result;
    }

    /**
     * Gets fuzzy match map merged with extract match map.
     */
    public List<LeverageMatch> getLeverageMatchesForOfflineDownLoad(
            Long p_sourcePageId, Long p_targetLocaleId)
    {
        List<LeverageMatch> leverageMatches = new ArrayList<LeverageMatch>();

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try
        {
            connection = DbUtil.getConnection();

            String lmTableName = SegmentTuTuvCacheManager
                    .getLMTableNameJobDataIn(p_sourcePageId);
            String sql = LEVERAGE_MATCH_FOR_OFFLINE_SQL.replace(
                    LM_TABLE_NAME_PLACE_HOLDER, lmTableName);

            ps = connection.prepareStatement(sql);
            ps.setLong(1, p_sourcePageId);
            ps.setLong(2, p_targetLocaleId);
            rs = ps.executeQuery();

            leverageMatches = convertToLeverageMatches(rs);
        }
        catch (Exception ex)
        {
            c_logger.error("getLeverageMatchesForOfflineDownLoad() error", ex);
            throw new LingManagerException(ex);
        }
        finally
        {
            DbUtil.silentClose(rs);
            DbUtil.silentClose(ps);
            DbUtil.silentReturnConnection(connection);
        }

        return leverageMatches;
    }

    /**
     * Save matched segments to the database
     * 
     * @param p_connection
     *            DB connection
     * @param p_sourcePage
     *            SourcePage object
     * @param p_leverageDataCenter
     *            Repository of matches of a page
     */
    public void saveLeverageResults(Connection p_connection,
            SourcePage p_sourcePage, LeverageDataCenter p_leverageDataCenter)
            throws LingManagerException
    {
        Set<LeverageMatch> nonClobMatches = new HashSet<LeverageMatch>();
        // Collection clobMatches = new ArrayList();
        LeverageOptions leverageOptions = p_leverageDataCenter
                .getLeverageOptions();
        // walk through all LeverageMatches in p_leverageDataCenter
        Iterator<LeverageMatches>  itLeverageMatches = null;
        try
        {
            itLeverageMatches = p_leverageDataCenter.leverageResultIterator();
        }
        catch (Exception e)
        {
            throw new LingManagerException(e);
        }

        while (itLeverageMatches != null && itLeverageMatches.hasNext())
        {
            LeverageMatches levMatches = (LeverageMatches) itLeverageMatches
                    .next();

            // walk through all target locales in the LeverageMatches
            Iterator itLocales = levMatches.targetLocaleIterator(String
                    .valueOf(p_sourcePage.getCompanyId()));
            while (itLocales.hasNext())
            {
                try
                {
                    GlobalSightLocale targetLocale = (GlobalSightLocale) itLocales
                            .next();

                    // walk through all matches in the locale
                    Collection<LeverageMatch> subNonClobMatches = getNonClobMatches(
                            p_connection, levMatches, targetLocale,
                            leverageOptions, p_sourcePage, null);
                    nonClobMatches.addAll(subNonClobMatches);
                }
                catch (Exception e)
                {
                    c_logger.error("Failed to getNonClobMatches *.", e);
                    throw new LingManagerException(e);
                }
            }
        }

        // save matches to the database
        if (nonClobMatches.size() > 0)
        {
            saveLeveragedMatches(nonClobMatches, p_connection);
        }
    }

    public void saveLeverageResults(Connection p_connection,
            long p_sourcePageId,
            Map<Long, LeverageMatches> p_leverageMatchesMap,
            GlobalSightLocale p_targetLocale, LeverageOptions p_leverageOptions)
            throws LingManagerException
    {
        Set<LeverageMatch> nonClobMatches = new HashSet<LeverageMatch>();

        SourcePage sp = null;
        try
        {
            sp = ServerProxy.getPageManager().getSourcePage(p_sourcePageId);
        }
        catch (Exception e)
        {
            c_logger.error("Can't get source page by sourcePageId "
                    + p_sourcePageId);
        }

        for (Map.Entry<Long, LeverageMatches> entry : p_leverageMatchesMap.entrySet())
        {
            Long originalSourceTuvId = (Long) entry.getKey();
            LeverageMatches levMatches = (LeverageMatches) entry.getValue();

            try
            {
                Collection<LeverageMatch> matches = getNonClobMatches(
                        p_connection, levMatches, p_targetLocale,
                        p_leverageOptions, sp, originalSourceTuvId);
                nonClobMatches.addAll(matches);
            }
            catch (Exception e)
            {
                c_logger.error("Failed to getNonClobMatches **.", e);
                throw new LingManagerException(e);
            }
        }

        // save matches to the database
        if (nonClobMatches.size() > 0)
        {
            saveLeveragedMatches(nonClobMatches, p_connection);
        }
    }

    public void saveLeveragedMatches(
            Collection<LeverageMatch> p_leverageMatchList)
            throws LingManagerException
    {
        Connection conn = null;
        try
        {
            conn = DbUtil.getConnection();

            saveLeveragedMatches(p_leverageMatchList, conn);
        }
        catch (Exception e)
        {
            c_logger.error("Error when saving leverage matches.", e);
            throw new LingManagerException(e);
        }
        finally
        {
            DbUtil.silentReturnConnection(conn);
        }
    }

    public void saveLeveragedMatches(
            Collection<LeverageMatch> p_leverageMatchList,
            Connection p_connection) throws LingManagerException
    {
        Map<Long, List<LeverageMatch>> groups = groupLeverageMatchesByCompany(p_leverageMatchList);

        PreparedStatement ps = null;
        try
        {
            Set<Map.Entry<Long, List<LeverageMatch>>> entries = groups
                    .entrySet();
            for (Iterator<Map.Entry<Long, List<LeverageMatch>>> it = entries
                    .iterator(); it.hasNext();)
            {
                p_connection.setAutoCommit(false);

                Map.Entry<Long, List<LeverageMatch>> entry =
                        (Map.Entry<Long, List<LeverageMatch>>) it.next();
                long companyId = (Long) entry.getKey();
                List<LeverageMatch> lms = (List<LeverageMatch>) entry
                        .getValue();

                String lmTableName = SegmentTuTuvCacheManager
                        .getLeverageMatchWorkingTableName(companyId);
                String insertSql = INSERT_INTO_LEVERAGE_MATCH_SQL.replace(
                        LM_TABLE_NAME_PLACE_HOLDER, lmTableName);

                ps = p_connection.prepareStatement(insertSql);
                for (LeverageMatch lm : lms)
                {
                    ps.setLong(1, lm.getSourcePageId());
                    ps.setLong(2, lm.getOriginalSourceTuvId());
                    ps.setString(3, lm.getSubId());
                    ps.setString(4, lm.getMatchedText());
                    ps.setLong(5, lm.getTargetLocaleId());

                    ps.setString(6, lm.getMatchType());
                    int maxOrderNum = getMaxOrderNum(p_connection,
                            lm.getOriginalSourceTuvId(),
                            lm.getTargetLocaleId(), lm.getSubId(), companyId);
                    if (lm.getOrderNum() <= maxOrderNum)
                    {
                        ps.setShort(7, (short) (maxOrderNum + 1));
                    }
                    else
                    {
                        ps.setShort(7, lm.getOrderNum());
                    }
                    ps.setFloat(8, lm.getScoreNum());
                    ps.setLong(9, lm.getMatchedTuvId());
                    ps.setLong(10, lm.getMatchedTableType());

                    ps.setInt(11, lm.getProjectTmIndex());
                    ps.setLong(12, lm.getTmId());
                    ps.setLong(13, lm.getTmProfileId());
                    ps.setString(14, lm.getMtName());
                    ps.setString(15, lm.getMatchedOriginalSource());

                    ps.setLong(16, lm.getJobDataTuId());

                    ps.addBatch();
                }

                ps.executeBatch();
                p_connection.commit();
            }
        }
        catch (SQLException sqlEx)
        {
            c_logger.error("Error when save leverage matches.", sqlEx);
            throw new LingManagerException(sqlEx);
        }
        finally
        {
            DbUtil.silentClose(ps);
        }
    }

    /**
     * All leverage matches may be from different companies, group them first.
     * 
     * @param p_leverageMatchList
     * 
     */
    private Map<Long, List<LeverageMatch>> groupLeverageMatchesByCompany(
            Collection<LeverageMatch> p_leverageMatchList)
    {
        Map<Long, List<LeverageMatch>> result = new HashMap<Long, List<LeverageMatch>>();
        for (LeverageMatch lm : p_leverageMatchList)
        {
            long companyId = getCompanyIdBySourcePageId(lm.getSourcePageId());
            List<LeverageMatch> lms = result.get(companyId);
            if (lms == null)
            {
                lms = new ArrayList<LeverageMatch>();
                lms.add(lm);
                result.put(companyId, lms);
            }
            else
            {
                lms.add(lm);
            }
        }

        return result;
    }

   private long getCompanyIdBySourcePageId(long p_sourcePageId)
    {
        long result = -1;

        try
        {
            result = ServerProxy.getPageManager().getSourcePage(p_sourcePageId)
                    .getCompanyId();
        }
        catch (Exception e)
        {
            c_logger.error("Failed to get companyId by sourcePageId "
                    + p_sourcePageId);
            throw new LingManagerException(e);
        }

        return result;
    }

    /**
     * Transfer matches in "LeverageMatches" into "LeverageMatch" object.The
     * data in "LeverageMatches" may be "LeveragedSegmentTuv"(from gold TM) or
     * be "LevereageInProgressTuv"(from in progress TM).
     * 
     */
    @SuppressWarnings("rawtypes")
    private Collection<LeverageMatch> getNonClobMatches(
            Connection p_connection, LeverageMatches p_levMatches,
            GlobalSightLocale p_targetLocale,
            LeverageOptions p_leverageOptions, SourcePage p_sourcePage,
            Long p_originalSourceTuvId) throws Exception
    {
        Collection<LeverageMatch> results = new ArrayList<LeverageMatch>();

        Iterator itMatch = p_levMatches.matchIterator(p_targetLocale,
                String.valueOf(p_sourcePage.getCompanyId()));
        while (itMatch.hasNext())
        {
            LeveragedTuv matchedTuv = (LeveragedTuv) itMatch.next();
            boolean isLeveragedSegmentTuv = (matchedTuv instanceof LeveragedSegmentTuv);
            boolean isLeveragedInProgressTuv = (matchedTuv instanceof LeveragedInProgressTuv);
            String segNoTopTag = matchedTuv.getSegmentNoTopTag();
            // For now,I can't see other TUV types which are from
            // "LeverageMatches".
            if (segNoTopTag != null && !segNoTopTag.equals("")
                    && (isLeveragedSegmentTuv || isLeveragedInProgressTuv))
            {
                SegmentTmTuv originalSourceTuv = (SegmentTmTuv) p_levMatches
                        .getOriginalTuv();

                LeverageMatch lm = new LeverageMatch();
                // 1.source_page_id
                lm.setSourcePageId(p_sourcePage.getIdAsLong());
                // 2.original_source_tuv_id
                if (p_originalSourceTuvId != null && p_originalSourceTuvId > 0)
                {
                    lm.setOriginalSourceTuvId(p_originalSourceTuvId);
                }
                else
                {
                    lm.setOriginalSourceTuvId(originalSourceTuv.getId());
                }
                // 3.sub_id
                SegmentTmTu originalTu = (SegmentTmTu) originalSourceTuv
                        .getTu();
                lm.setSubId(originalTu.getSubId());
                // 4.matched_text_string
                lm.setMatchedText(matchedTuv.getSegment());
                // 5.target_locale_id
                lm.setTargetLocale(p_targetLocale);
                // 6.match_type
                lm.setMatchType(matchedTuv.getMatchState().getName());
                // 7.order_num
                int maxOrderNum = getMaxOrderNum(p_connection,
                        originalSourceTuv.getId(), p_targetLocale.getId(),
                        originalTu.getSubId(), p_sourcePage.getCompanyId());
                int orderNum = maxOrderNum + matchedTuv.getOrder();
                lm.setOrderNum((short) orderNum);
                // 8.score_num
                lm.setScoreNum(matchedTuv.getScore());
                // 9.matched_table_type
                int matchedTableType = getMatchTableType(matchedTuv);
                lm.setMatchedTableType(matchedTableType);

                long tmId = matchedTuv.getTu().getTmId();
                if (isLeveragedSegmentTuv)
                {
                    // 10.matched_tuv_id
                    lm.setMatchedTuvId(matchedTuv.getId());
                    // 11.tm_id
                    lm.setTmId(tmId);
                    // 12.project_tm_index
                    int projectTmIndex = Leverager.getProjectTmIndex(
                            p_leverageOptions, tmId);
                    lm.setProjectTmIndex(projectTmIndex);
                    lm.setJobDataTuId(-1);
                }
                else if (isLeveragedInProgressTuv)
                {
                    // If match is from in-progress TM,the matchedTuvId is
                    // ID of "ip_tm_trg_X",not the real matchedTuvId from
                    // project
                    // TM, so keep it -1.
                    lm.setMatchedTuvId(-1);
                    // If match is from in-progress TM,the tmId is "jobId",not
                    // the real project TM ID.
                    lm.setTmId(tmId);
                    // For now,leverage_match data is from project TM or
                    // in-progress TM,so it is safe to set "-7" here.
                    lm.setProjectTmIndex(Leverager.IN_PROGRESS_TM_PRIORITY);

                    long jobDataTuId = ((LeveragedInProgressTuv) matchedTuv)
                            .getJobDataTuId();
                    lm.setJobDataTuId(jobDataTuId);
                }
                // 13.tm_profile_id
                long tmProfileId = p_leverageOptions.getTmProfileId();
                lm.setTmProfileId(tmProfileId);
                // 14.matched_original_source
                String matchedOriginalSource = matchedTuv.getSourceTuv()
                        .getSegment();
                lm.setMatchedOriginalSource(matchedOriginalSource);

                results.add(lm);
            }
        }

        return results;
    }

    /**
     * Get max order_num per specified TUV. Note that this does not work for
     * archived jobs.
     * 
     * @return -- the max order NUM.
     */
    private int getMaxOrderNum(Connection p_connection,
            long p_originalSourceTuvId, long p_targetLocaleId, String p_subId,
            long p_companyId)
    {
        // default;
        int maxOrderNum = 0;

        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            String lmTableName = SegmentTuTuvCacheManager
                    .getLeverageMatchWorkingTableName(p_companyId);
            String sql = MAX_ORDER_NUM.replace(LM_TABLE_NAME_PLACE_HOLDER,
                    lmTableName);
            ps = p_connection.prepareStatement(sql);

            ps.setLong(1, p_originalSourceTuvId);
            ps.setLong(2, p_targetLocaleId);
            ps.setString(3, p_subId);

            rs = ps.executeQuery();
            while (rs.next())
            {
                maxOrderNum = rs.getInt(1);
            }
        }
        catch (Exception e)
        {
            c_logger.error("Failed to get max order num originalSourceTuvId "
                    + p_originalSourceTuvId + ", subId : " + p_subId
                    + ", targetLocaleId : " + p_targetLocaleId, e);
        }
        finally
        {
            DbUtil.silentClose(rs);
            DbUtil.silentClose(ps);
        }

        return maxOrderNum;
    }

    public float getBestMatchScore(Connection p_connection,
            long p_originalSourceTuvId, long p_targetLocaleId, String p_subId,
            long p_companyId, boolean p_isJobDataMigrated)
    {
        // default;
        float bestMatchScore = 0;

        PreparedStatement ps = null;
        ResultSet rs = null;

        try
        {
            String lmTableName = SegmentTuTuvCacheManager
                    .getLMTableNameJobDataIn(p_companyId, p_isJobDataMigrated);
            ps = p_connection.prepareStatement(BEST_MATCH_SCORE.replace(
                    LM_TABLE_NAME_PLACE_HOLDER, lmTableName));

            ps.setLong(1, p_originalSourceTuvId);
            ps.setLong(2, p_targetLocaleId);
            ps.setString(3, p_subId);

            rs = ps.executeQuery();
            if (rs.next())
            {
                bestMatchScore = rs.getFloat(1);
            }
        }
        catch (Exception e)
        {
            c_logger.error(
                    "Failed to get best match score for originalSourceTuvId "
                            + p_originalSourceTuvId + ", subId : " + p_subId
                            + ", targetLocaleId : " + p_targetLocaleId, e);
            throw new LingManagerException(e);
        }
        finally
        {
            DbUtil.silentClose(rs);
            DbUtil.silentClose(ps);
        }

        return bestMatchScore;
    }

    public static int getMatchTableType(LeveragedTuv p_matchedTuv)
    {
        // Default
        int type = SEGMENT_TM_T;
        int matchedTableType = ((LeveragedTu) p_matchedTuv.getTu())
                .getMatchTableType();

        if (matchedTableType == LeveragedTu.PAGE_TM)
        {
            if (p_matchedTuv.isTranslatable())
            {
                type = PAGE_TM_T;
            }
            else
            {
                type = PAGE_TM_L;
            }
        }
        else if (matchedTableType == LeveragedTu.IN_PROGRESS_TM)
        {
            if (p_matchedTuv.isTranslatable())
            {
                type = IN_PROGRESS_TM_T;
            }
            else
            {
                type = IN_PROGRESS_TM_L;
            }
        }
        else
        {
            if (p_matchedTuv.isTranslatable())
            {
                type = SEGMENT_TM_T;
            }
            else
            {
                type = SEGMENT_TM_L;
            }
        }

        return type;
    }

    @SuppressWarnings("rawtypes")
    class ComparatorByModifyDate implements Comparator
    {
        private int model;
        private TranslationMemoryProfile tmProfile;
        private long companyId = -1;

        public ComparatorByModifyDate(int model,
                TranslationMemoryProfile tmProfile, long companyId)
        {
            this.model = model;
            this.tmProfile = tmProfile;
            this.companyId = companyId;
        }

        public int compare(Object arg0, Object arg1)
        {
            LeverageSegment ls1 = (LeverageSegment) arg0;
            LeverageSegment ls2 = (LeverageSegment) arg1;

            int result = LeverageUtil.compareSid(ls1, ls2, companyId);
            if (result != 0)
            {
                return result;
            }

            if (tmProfile.isTmProcendence())
            {
                result = ls1.getTmIndex() - ls2.getTmIndex();
                if (result != 0)
                {
                    return result;
                }
            }

            if (ls1.getModifyDate() != null && ls2.getModifyDate() != null)
            {
                result = ls1.getModifyDate().compareTo(ls2.getModifyDate());
                if (model == LeverageOptions.PICK_LATEST)
                {
                    result = -result;
                }
            }

            return result;
        }

    }

    /**
     * Convert the data in ResultSet into LeverageMatch list.
     * 
     * @param rs
     * @return
     * @throws SQLException
     */
    private List<LeverageMatch> convertToLeverageMatches(ResultSet rs)
            throws SQLException
    {
        List<LeverageMatch> leverageMatches = new ArrayList<LeverageMatch>();

        while (rs.next())
        {
            LeverageMatch lm = new LeverageMatch();
            lm.setSourcePageId(rs.getLong(1));
            lm.setOriginalSourceTuvId(rs.getLong(2));
            lm.setSubId(rs.getString(3));
            lm.setMatchedText(rs.getString(4));
            lm.setTargetLocaleId(rs.getLong(5));
            lm.setMatchType(rs.getString(6));
            lm.setOrderNum(rs.getShort(7));
            lm.setScoreNum(rs.getFloat(8));
            lm.setMatchedTuvId(rs.getLong(9));
            lm.setMatchedTableType(rs.getLong(10));
            lm.setProjectTmIndex(rs.getInt(11));
            lm.setTmId(rs.getLong(12));
            lm.setTmProfileId(rs.getLong(13));
            lm.setMtName(rs.getString(14));
            lm.setMatchedOriginalSource(rs.getString(15));
            lm.setJobDataTuId(rs.getLong(16));

            leverageMatches.add(lm);
        }

        return leverageMatches;
    }
}
