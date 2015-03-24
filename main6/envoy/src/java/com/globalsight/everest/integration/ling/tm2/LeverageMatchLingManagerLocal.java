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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.everest.persistence.tuv.BigTableUtil;
import com.globalsight.everest.persistence.tuv.SegmentTuUtil;
import com.globalsight.everest.persistence.tuv.SegmentTuvUtil;
import com.globalsight.everest.persistence.tuv.TuvQueryConstants;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.tuv.TuImpl;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.everest.util.comparator.StringComparator;
import com.globalsight.everest.webapp.pagehandler.tasks.UpdateLeverageHelper;
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
import com.globalsight.ling.tm2.leverage.MatchState;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.SortUtil;
import com.globalsight.util.StringUtil;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.gxml.GxmlElement;
import com.globalsight.util.gxml.GxmlNames;

/**
 * Implementation of com.globalsight.ling.tm.LeverageMatchLingManager.
 */
public class LeverageMatchLingManagerLocal implements LeverageMatchLingManager
{
    static private final Logger c_logger = Logger
            .getLogger(LeverageMatchLingManagerLocal.class);

    private static Set<String> differentCases = null;

    private static final String LM_TABLE_NAME_PLACE_HOLDER = TuvQueryConstants.LM_TABLE_PLACEHOLDER;

    private static final String SELECT_COLUMNS = "SELECT "
            + " SOURCE_PAGE_ID, ORIGINAL_SOURCE_TUV_ID, SUB_ID, MATCHED_TEXT_STRING, "
            + " TARGET_LOCALE_ID, MATCH_TYPE, ORDER_NUM, SCORE_NUM, MATCHED_TUV_ID, "
            + " MATCHED_TABLE_TYPE, PROJECT_TM_INDEX, TM_ID, TM_PROFILE_ID, MT_NAME, "
            + " MATCHED_ORIGINAL_SOURCE, JOB_DATA_TU_ID, SID, CREATION_USER, CREATION_DATE, "
            + " MODIFY_USER, MODIFY_DATE FROM ";

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
     * Get all order_num existed in DB by sourceTuvId, targetLocaleId and subId.
     */
    private static final String GET_ALL_ORDER_NUMS =
            "SELECT order_num FROM " + LM_TABLE_NAME_PLACE_HOLDER
            + " WHERE ORIGINAL_SOURCE_TUV_ID = ? "
            + " AND TARGET_LOCALE_ID = ? "
            + " AND SUB_ID = ? "
            + " AND ORDER_NUM > 0 "
            + " AND ORDER_NUM < " + TmCoreManager.LM_ORDER_NUM_START_REMOTE_TM;

    private static final String BEST_MATCH_SCORE = "select max(score_num) from "
            + LM_TABLE_NAME_PLACE_HOLDER
            + " WHERE original_source_tuv_id = ? "
            + " AND target_locale_id = ? " + " AND sub_id = ? ";

    private static final String INSERT_INTO_LEVERAGE_MATCH_SQL = "INSERT INTO "
            + LM_TABLE_NAME_PLACE_HOLDER
            + " (SOURCE_PAGE_ID, ORIGINAL_SOURCE_TUV_ID, SUB_ID, MATCHED_TEXT_STRING, TARGET_LOCALE_ID, "
            + "  MATCH_TYPE, ORDER_NUM, SCORE_NUM, MATCHED_TUV_ID, MATCHED_TABLE_TYPE, "
            + "  PROJECT_TM_INDEX, TM_ID, TM_PROFILE_ID, MT_NAME, MATCHED_ORIGINAL_SOURCE, "
            + "  JOB_DATA_TU_ID, SID, CREATION_USER, CREATION_DATE, MODIFY_USER, MODIFY_DATE, MATCHED_TEXT_CLOB) "
            + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

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
            String lmTableName = BigTableUtil
                    .getLMTableJobDataInBySourcePageId(p_sourcePageId);
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
            long jobId, long... tmIds) throws LingManagerException
    {
        TreeSet<LeverageMatch> set = null;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try
        {
            conn = DbUtil.getConnection();
            String lmTableName = BigTableUtil.getLMTableJobDataInByJobId(jobId);
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
        long jobId = sp.getJobId();
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
                tuv = SegmentTuvUtil.getTuvById(srcTuvHandling.longValue(), jobId);
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
            SortUtil.sort(subidList);

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
                                matchType, null, rootLm.getProjectTmIndex(),
                                null, rootLm.getMatchedTuvId(),
                                rootLm.getTmId());
                        ls.setOrgSid(rootLm.getOrgSid(jobId));
                        Date d = rootLm.getModifyDate() == null ? new Date() : rootLm.getModifyDate();
                        ls.setModifyDate(new Timestamp(d.getTime()));
                        ls.setSid(rootLm.getSid());
                        ArrayList<LeverageSegment> l = (ArrayList<LeverageSegment>) result
                                .get(new Long(rootLm.getOriginalSourceTuvId()));
                        if (l != null && l.size() != 0)
                        {
                            l.add(ls);
                            SortUtil.sort(l, new ComparatorByModifyDate(model,
                                    tmProfile, jobId));
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

            LeverageSegment ls = new LeverageSegment(gxml,
                    LeverageDataCenter.getTuvState(lm.getMatchType()), null,
                    lm.getProjectTmIndex(), null, lm.getMatchedTuvId(),
                    lm.getTmId());
            Date d = lm.getModifyDate() == null ? new Date() : lm.getModifyDate();
            ls.setModifyDate(new Timestamp(d.getTime()));
            ls.setSid(lm.getSid());
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

            String lmTableName = BigTableUtil
                    .getLMTableJobDataInBySourcePageId(p_sourcePageId);
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
        long jobId = -1l;

        try
        {
            jobId = BigTableUtil.getSourcePageById(p_sourcePageId).getJobId();
            connection = DbUtil.getConnection();

            String lmTableName = BigTableUtil.getLMTableJobDataInByJobId(jobId);
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
            // touch to load into cache for performance.
            SegmentTuUtil.getTusBySourcePageId(p_sourcePageId);
            SegmentTuvUtil.getSourceTuvs(ServerProxy.getPageManager()
                    .getSourcePage(p_sourcePageId));
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
                        TuImpl tu = (TuImpl) SegmentTuvUtil.getTuvById(
                                originalSourceTuvId, jobId).getTu(jobId);
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
                        if ((LeverageUtil.compareSid(lm, cloneMatch, jobId) > 0 && cloneMatch
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
        Collection<LeverageMatch> leverageMatches2 = leveragematchesMap
                .values();
        Set<String> keys = new HashSet<String>();
        for (LeverageMatch match : leverageMatches2)
        {
            String key = MatchTypeStatistics.makeKey(
                    match.getOriginalSourceTuvId(), match.getSubId());
            if (!keys.contains(key))
            {
                result.addMatchType(match);
                keys.add(key);
            }
        }

        return result;
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
			String p_subId, Long p_targetLocaleId, Long p_orderNum, long p_jobId)
			throws LingManagerException
    {
        Connection connection = null;
        PreparedStatement ps = null;

        try
        {
            connection = DbUtil.getConnection();
            connection.setAutoCommit(false);

            // Delete leverage match attributes first.
			LeverageMatchAttributeUtil.deleteLMAttributes(
					p_OriginalSourceTuvId, p_subId, p_targetLocaleId,
					p_orderNum, p_jobId);

            // Delete
            String sql = getDeleteSql(p_OriginalSourceTuvId, p_subId,
                    p_targetLocaleId, p_orderNum, p_jobId);

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
            Long p_targetLocaleId, Long p_orderNum, long p_jobId)
            throws Exception
    {
        StringBuilder sql = new StringBuilder();

        String lmTableName = BigTableUtil.getLMTableJobDataInByJobId(p_jobId);
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

            String lmTableName = BigTableUtil
                    .getLMTableJobDataInBySourcePageId(p_sourcePageId);
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
        long jobId = p_sourcePage.getJobId();
        Set<LeverageMatch> nonClobMatches = new HashSet<LeverageMatch>();
        // Collection clobMatches = new ArrayList();
        LeverageOptions leverageOptions = p_leverageDataCenter
                .getLeverageOptions();
        // walk through all LeverageMatches in p_leverageDataCenter
        Iterator<LeverageMatches> itLeverageMatches = null;
        try
        {
            itLeverageMatches = p_leverageDataCenter.leverageResultIterator();
        }
        catch (Exception e)
        {
            throw new LingManagerException(e);
        }

        Map<Long, String> srcTuvId2SidMap = getTuvId2SidMap(p_sourcePage);

        while (itLeverageMatches != null && itLeverageMatches.hasNext())
        {
            LeverageMatches levMatches = (LeverageMatches) itLeverageMatches
                    .next();

            // walk through all target locales in the LeverageMatches
            Iterator itLocales = levMatches.targetLocaleIterator(jobId);
            while (itLocales.hasNext())
            {
                try
                {
                    GlobalSightLocale targetLocale = (GlobalSightLocale) itLocales
                            .next();

                    // walk through all matches in the locale
                    Collection<LeverageMatch> subNonClobMatches = getNonClobMatches(
                            p_connection, levMatches, targetLocale,
                            leverageOptions, p_sourcePage, null,
                            srcTuvId2SidMap);
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
            saveLeveragedMatches(nonClobMatches, p_connection, jobId);
        }
    }

    public void saveLeverageResults(Connection p_connection,
            long p_sourcePageId,
            Map<Long, LeverageMatches> p_leverageMatchesMap,
            GlobalSightLocale p_targetLocale, LeverageOptions p_leverageOptions)
            throws LingManagerException
    {
        Set<LeverageMatch> nonClobMatches = new HashSet<LeverageMatch>();

        SourcePage srcPage = null;
        Map<Long, String> srcTuvId2SidMap = null;
        long jobId = -1;
        try
        {
            srcPage = ServerProxy.getPageManager().getSourcePage(p_sourcePageId);
            srcTuvId2SidMap = getTuvId2SidMap(srcPage);
            jobId = srcPage.getJobId();
        }
        catch (Exception e)
        {
            c_logger.error("Can't get source page by sourcePageId "
                    + p_sourcePageId);
        }

        for (Map.Entry<Long, LeverageMatches> entry : p_leverageMatchesMap
                .entrySet())
        {
            Long originalSourceTuvId = (Long) entry.getKey();
            LeverageMatches levMatches = (LeverageMatches) entry.getValue();

            try
            {
                Collection<LeverageMatch> matches = getNonClobMatches(
                        p_connection, levMatches, p_targetLocale,
                        p_leverageOptions, srcPage, originalSourceTuvId,
                        srcTuvId2SidMap);
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
            saveLeveragedMatches(nonClobMatches, p_connection, jobId);
        }
    }

    public void saveLeveragedMatches(
            Collection<LeverageMatch> p_leverageMatchList, long p_jobId)
            throws LingManagerException
    {
        Connection conn = null;
        try
        {
            conn = DbUtil.getConnection();

            saveLeveragedMatches(p_leverageMatchList, conn, p_jobId);
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
            Connection p_connection, long p_jobId) throws LingManagerException
    {
        Map<Long, List<LeverageMatch>> groups = groupLeverageMatchesByCompany(p_leverageMatchList);

        PreparedStatement ps = null;
        int max = 500;
        int count = 0;
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
                List<LeverageMatch> lms = (List<LeverageMatch>) entry.getValue();

                // Check to avoid possible duplicate primary key error for LM table.
                checkOrderNumUnique(p_connection, lms, p_jobId);
                
                String lmTableName = BigTableUtil
                        .getLMTableJobDataInByJobId(p_jobId);
                String insertSql = INSERT_INTO_LEVERAGE_MATCH_SQL.replace(
                        LM_TABLE_NAME_PLACE_HOLDER, lmTableName);

                List<LeverageMatchAttribute> sidAttrs = new ArrayList<LeverageMatchAttribute>();
                ps = p_connection.prepareStatement(insertSql);
                for (LeverageMatch lm : lms)
                {
                    ps.setLong(1, lm.getSourcePageId());
                    ps.setLong(2, lm.getOriginalSourceTuvId());
                    ps.setString(3, lm.getSubId());
                    if (EditUtil.getUTF8Len(lm.getMatchedText()) > PersistentObject.CLOB_THRESHOLD)
					{
						ps.setString(4, null);
						ps.setString(22, lm.getMatchedText());
					}
					else
					{
						ps.setString(4, lm.getMatchedText());
						ps.setString(22, null);
					}
                    ps.setLong(5, lm.getTargetLocaleId());

                    ps.setString(6, lm.getMatchType());
                    ps.setShort(7, lm.getOrderNum());

                    ps.setFloat(8, lm.getScoreNum());
                    ps.setLong(9, lm.getMatchedTuvId());
                    ps.setLong(10, lm.getMatchedTableType());

                    ps.setInt(11, lm.getProjectTmIndex());
                    ps.setLong(12, lm.getTmId());
                    ps.setLong(13, lm.getTmProfileId());
                    ps.setString(14, lm.getMtName());
                    ps.setString(15, lm.getMatchedOriginalSource());

                    ps.setLong(16, lm.getJobDataTuId());
					// SID is always saved into "leverage_match_attr_xx" table
					// since 8.6.1.
                    ps.setString(17, null);
                    if (StringUtil.isNotEmpty(lm.getSid()))
                    {
						LeverageMatchAttribute sidAttr = new LeverageMatchAttribute(
								lm.getSourcePageId(),
								lm.getOriginalSourceTuvId(), lm.getSubId(),
								lm.getOrderNum(), lm.getTargetLocaleId());
                    	sidAttr.setName(LeverageMatchAttribute.SID);
                    	sidAttr.setTextValue(lm.getSid());
                    	sidAttrs.add(sidAttr);
                    }
                    ps.setString(18, lm.getCreationUser());
                    // creation date
                    Date creationDate = lm.getCreationDate() == null ? new Date() : lm.getCreationDate();
                    ps.setTimestamp(19, new java.sql.Timestamp(creationDate.getTime()));
                    ps.setString(20, lm.getModifyUser());
                    // modify date
                    Date modifyDate = lm.getModifyDate() == null ? new Date() : lm.getModifyDate();
                    ps.setTimestamp(21,  new java.sql.Timestamp(modifyDate.getTime()));

                    ps.addBatch();
                    count ++;
                    if(count == max)
                    {
                    	ps.executeBatch();
                        p_connection.commit();
                        ps.clearBatch();
                        count = 0;
                    }
                }
                
                if(count  > 0)
                {
                	ps.executeBatch();
                    p_connection.commit();
                }

				LeverageMatchAttributeUtil.saveLeverageMatchAttributes(
						p_connection, sidAttrs, p_jobId);
            }
        }
        catch (Exception ex)
        {
            c_logger.error("Error when save leverage matches.", ex);
            throw new LingManagerException(ex);
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
    @SuppressWarnings({ "rawtypes"})
    private Collection<LeverageMatch> getNonClobMatches(
            Connection p_connection, LeverageMatches p_levMatches,
            GlobalSightLocale p_targetLocale,
            LeverageOptions p_leverageOptions, SourcePage p_sourcePage,
            Long p_originalSourceTuvId, Map<Long, String> p_srcTuvId2SidMap)
            throws Exception
    {
        Collection<LeverageMatch> allLeverageMatches = new ArrayList<LeverageMatch>();

        long jobId = p_sourcePage.getJobId();
        Iterator itMatch = p_levMatches.matchIterator(p_targetLocale, jobId);
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
                // 7.order_num (We don't ensure the order_num unique here, the
                // check happens when save into DB)
                lm.setOrderNum((short) matchedTuv.getOrder());
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

                // 15,16,17,18,19
                lm.setSid(matchedTuv.getSid());
                lm.setCreationUser(matchedTuv.getCreationUser());
                lm.setCreationDate(matchedTuv.getCreationDate());
                lm.setModifyUser(matchedTuv.getModifyUser());
                lm.setModifyDate(matchedTuv.getModifyDate());

                allLeverageMatches.add(lm);
            }
        }

        Collection<LeverageMatch> results = new ArrayList<LeverageMatch>();
        // If "Get Unique from Multiple Exact Matches" is checked in TM profile...
        if (p_leverageOptions.getUniqueFromMultipleTranslation())
        {
            int mode = UpdateLeverageHelper.getMode(p_leverageOptions
                    .getTmProfile());
            boolean isTmProcedence = p_leverageOptions.getTmProfile()
                    .isTmProcendence();
            filterMatchesForMultipleTranslations(results, allLeverageMatches,
                    p_srcTuvId2SidMap, isTmProcedence, mode);
        }
        else
        {
            results = allLeverageMatches;
        }

        return results;
    }

    /**
     * If "Get Unique from Multiple Exact Matches" is checked in TM profile,
     * only returns one exact match for every segment.
     * 
     * @param results
     * @param allLeverageMatches
     * @param mode
     */
    private void filterMatchesForMultipleTranslations(
            Collection<LeverageMatch> results,
            Collection<LeverageMatch> allLeverageMatches, 
            Map<Long, String> p_srcTuvId2SidMap, boolean isTmProcedence,
            int mode)
    {
        // SourceTuvId_subId : List<LeverageMatch>
        Map<String, List<LeverageMatch>> map = new HashMap<String, List<LeverageMatch>>();
        for (LeverageMatch lm : allLeverageMatches)
        {
            // All fuzzy matches will be stored into DB.
            if (lm.getScoreNum() < 100)
            {
                results.add(lm);
            }
            else
            {
                // Put all 100% matches into map first.
                String key = lm.getOriginalSourceTuvId() + "_" + lm.getSubId();
                List<LeverageMatch> listForOne = map.get(key);
                if (listForOne == null)
                {
                    listForOne = new ArrayList<LeverageMatch>();
                    map.put(key, listForOne);
                }
                listForOne.add(lm);
            }
        }

        // Handle all 100% matches.
        Collection<List<LeverageMatch>> lmGroups = map.values();
        for (List<LeverageMatch> listForOne : lmGroups)
        {
            if (listForOne.size() <= 1)
            {
                results.addAll(listForOne);
            }
            else
            {
                // Divide "XX_DIFFERENT" matches into "backup". 
                List<LeverageMatch> backup = new ArrayList<LeverageMatch>();
                Set<String> differentCases = getDifferentCases();
                for (Iterator<LeverageMatch> it = listForOne.iterator(); it.hasNext();)
                {
                    LeverageMatch lm = it.next();
                    if (differentCases.contains(lm.getMatchType()))
                    {
                        backup.add(lm);
                        it.remove();
                    }
                }

                // if no other matches, keep all "different" matches.
                if (listForOne.size() == 0)
                {
                    results.addAll(backup);
                }
                // if only one, this is wanted.
                else if (listForOne.size() == 1)
                {
                    results.add(listForOne.get(0));
                }
                // if multiple, sort and get first.
                else
                {
                    // Sort by reference TM sequence, latest/oldest, order-num,
                    // then pick up the FIRST.
                    SortUtil.sort(listForOne, new LeverageMatchComparator(
                            isTmProcedence, mode));

                    // Check to see if there is SID matched leverage match.
                    LeverageMatch sidMatchedLM = null;
                    String tuvSID = p_srcTuvId2SidMap.get(
                            listForOne.get(0).getOriginalSourceTuvId());
                    if (tuvSID != null)
                    {
                        sidMatchedLM = getSidMatchedLeverageMatchData(tuvSID,
                                listForOne);
                    }

                    if (sidMatchedLM != null)
                    {
                        sidMatchedLM
                                .setMatchType(MatchState.SEGMENT_TM_EXACT_MATCH
                                        .getName());
                        results.add(sidMatchedLM);
                    }
                    else
                    {
                        LeverageMatch firstLM = listForOne.get(0);
                        firstLM.setMatchType(MatchState.SEGMENT_TM_EXACT_MATCH
                                .getName());
                        results.add(firstLM);                        
                    }
                }
            }
        }
    }

    private Set<String> getDifferentCases()
    {
        if (differentCases == null)
        {
            differentCases = new HashSet<String>();
            differentCases.add(MatchState.CODE_DIFFERENT.getName());
            differentCases.add(MatchState.WHITESPACE_DIFFERENT.getName());
            differentCases.add(MatchState.CASE_DIFFERENT.getName());
            differentCases.add(MatchState.TYPE_DIFFERENT.getName());
        }

        return differentCases;
    }
    
    // Sort by reference TM sequence, latest/oldest, order-num, then pick up the
    // first (GBS-3073)
    private class LeverageMatchComparator extends StringComparator
    {
        private static final long serialVersionUID = -729929686418029804L;
        private boolean isTmProcedence = false;
        private int mode = LeverageOptions.PICK_LATEST;

        public LeverageMatchComparator(boolean p_isTmProcedence, int p_mode)
        {
            super(Locale.ENGLISH);
            this.isTmProcedence = p_isTmProcedence;
            this.mode = p_mode;
        }

        public int compare(Object p_A, Object p_B)
        {
            LeverageMatch a = (LeverageMatch) p_A;
            LeverageMatch b = (LeverageMatch) p_B;

            int result = 0;

            // "SEGMENT_TM_EXACT_MATCH" matches have top priority.
            String matchType1 = a.getMatchType();
            String matchType2 = b.getMatchType();
            if (matchType1 == null
                    || !MatchState.SEGMENT_TM_EXACT_MATCH.getName().equals(
                            matchType1))
            {
                matchType1 = "";
            }
            if (matchType2 == null
                    || !MatchState.SEGMENT_TM_EXACT_MATCH.getName().equals(
                            matchType2))
            {
                matchType2 = "";
            }
            result = super.compareStrings(matchType1, matchType2);
            if (result != 0)
            {
                return -result;
            }

            // Compare project TM index
            if (isTmProcedence)
            {
                result = a.getProjectTmIndex() - b.getProjectTmIndex();
                if (result != 0)
                {
                    return result;
                }                
            }

            // Compare modified date
            Date aDate = a.getModifyDate();
            Date bDate = b.getModifyDate();
            if (aDate != null && bDate != null)
            {
                result = aDate.compareTo(bDate);
                if (mode == LeverageOptions.PICK_LATEST)
                {
                    result = -result;
                }
            }
            if (result != 0)
            {
                return result;
            }

            // Compare order NUM
            result = a.getOrderNum() - b.getOrderNum();

            return result;
        }
    }

    private Map<Long, String> getTuvId2SidMap(SourcePage p_sourcePage)
    {
        Map<Long, String> srcTuvId2SidMap = new HashMap<Long, String>();
        try
        {
            ArrayList<Tuv> tuvs = SegmentTuvUtil.getSourceTuvs(p_sourcePage,
                    false);
            for (Tuv srcTuv : tuvs)
            {
                if (srcTuv.getSid() != null)
                {
                    srcTuvId2SidMap.put(srcTuv.getIdAsLong(), srcTuv.getSid());
                }
            }            
        }
        catch (Exception e)
        {
            
        }
        return srcTuvId2SidMap;
    }

    /**
     * Return the first leverage match which SID is matched with TUV SID.
     * 
     * @param p_tuvSID -- Segment SID from TUV.
     * @param lms
     * @return LeverageMatch object.
     */
    private LeverageMatch getSidMatchedLeverageMatchData(String p_tuvSID,
            List<LeverageMatch> lms)
    {
        if (p_tuvSID == null)
        {
            return null;
        }

        LeverageMatch result = null;
        for (LeverageMatch lm : lms)
        {
            if (p_tuvSID.equals(lm.getSid()))
            {
                result = lm;
                break;
            }
        }

        return result;
    }

    /**
     * Check to ensure "order_num" unique for
     * "sourceTuvId, targetLocaleId, subId, orderNum" primary key in leverage
     * match table.
     */
    private void checkOrderNumUnique(Connection p_connection,
            List<LeverageMatch> lms, long p_jobId)
    {
        HashMap<String, Set<Integer>> cachedOrderNums = new HashMap<String, Set<Integer>>();

        long srcTuvId, trgLocaleId;
        String subId = null;
        for (LeverageMatch lm : lms)
        {
            srcTuvId = lm.getOriginalSourceTuvId();
            trgLocaleId = lm.getTargetLocaleId();
            subId = lm.getSubId();

            Set<Integer> existedOrderNums = getExistedOrderNums(p_connection,
                    srcTuvId, trgLocaleId, subId, p_jobId);
            String key = srcTuvId + "-" + trgLocaleId + "-" + subId;
            Set<Integer> orderNums = cachedOrderNums.get(key);
            if (orderNums != null)
            {
                orderNums.addAll(existedOrderNums);
            }
            else
            {
                cachedOrderNums.put(key, existedOrderNums);
            }

            orderNums = cachedOrderNums.get(key);
            int orderNum = determineOrderNum(orderNums, lm.getOrderNum());
            orderNums.add(orderNum);
            lm.setOrderNum((short) orderNum);
        }
    }

    private int determineOrderNum(Set<Integer> existedOrderNums,
            int suggestedOrderNum)
    {
        if (existedOrderNums == null
                || !existedOrderNums.contains(suggestedOrderNum))
            return suggestedOrderNum;

        int orderNum = 1;
        for (int i = 1; i <= 100; i++)
        {
            if (!existedOrderNums.contains(i))
            {
                orderNum = i;
                break;
            }
        }

        return orderNum;
    }

    private Set<Integer> getExistedOrderNums(Connection p_connection,
            long p_originalSourceTuvId, long p_targetLocaleId, String p_subId,
            long p_jobId)
    {
        Set<Integer> orderNums = new HashSet<Integer>();

        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            String lmTableName = BigTableUtil
                    .getLMTableJobDataInByJobId(p_jobId);
            String sql = GET_ALL_ORDER_NUMS.replace(LM_TABLE_NAME_PLACE_HOLDER,
                    lmTableName);
            ps = p_connection.prepareStatement(sql);

            ps.setLong(1, p_originalSourceTuvId);
            ps.setLong(2, p_targetLocaleId);
            ps.setString(3, p_subId);

            rs = ps.executeQuery();
            while (rs.next())
            {
                orderNums.add(rs.getInt(1));
            }
        }
        catch (Exception e)
        {
            c_logger.error("Failed to getOrderNums by originalSourceTuvId "
                    + p_originalSourceTuvId + ", subId : " + p_subId
                    + ", targetLocaleId : " + p_targetLocaleId, e);
        }
        finally
        {
            DbUtil.silentClose(rs);
            DbUtil.silentClose(ps);
        }

        return orderNums;
    }

    public float getBestMatchScore(Connection p_connection,
            long p_originalSourceTuvId, long p_targetLocaleId, String p_subId,
            long p_jobId)
    {
        // default;
        float bestMatchScore = 0;

        PreparedStatement ps = null;
        ResultSet rs = null;

        try
        {
            String lmTableName = BigTableUtil
                    .getLMTableJobDataInByJobId(p_jobId);
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

    class ComparatorByModifyDate extends StringComparator
    {
        private static final long serialVersionUID = 9141714929755854387L;
        private int model;
        private TranslationMemoryProfile tmProfile;
        private long jobId = -1;

        public ComparatorByModifyDate(int model,
                TranslationMemoryProfile tmProfile, long jobId)
        {
            super(Locale.ENGLISH);
            this.model = model;
            this.tmProfile = tmProfile;
            this.jobId = jobId;
        }

        public int compare(Object arg0, Object arg1)
        {
            LeverageSegment ls1 = (LeverageSegment) arg0;
            LeverageSegment ls2 = (LeverageSegment) arg1;

            // "EXACT_MATCH" matches have top priority.
            String matchType1 = ls1.getMatchType();
            String matchType2 = ls2.getMatchType();
            if (matchType1 == null || !"EXACT_MATCH".equals(matchType1))
            {
                matchType1 = "";
            }
            if (matchType2 == null || !"EXACT_MATCH".equals(matchType2))
            {
                matchType2 = "";
            }
            int result = super.compareStrings(matchType1, matchType2);
            if (result != 0)
            {
                return -result;
            }

            result = LeverageUtil.compareSid(ls1, ls2, jobId);
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
     * @throws Exception 
     */
    private List<LeverageMatch> convertToLeverageMatches(ResultSet rs)
            throws Exception
    {
        List<LeverageMatch> leverageMatches = new ArrayList<LeverageMatch>();
        long sourcePageId = -1;
        long trgLocaleId = -1;
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
            lm.setSid(rs.getString(17));
            lm.setCreationUser(rs.getString(18));
            try
            {
                lm.setCreationDate(new Date(rs.getTimestamp(19).getTime()));
            }
            catch (Exception e)
            {
                lm.setCreationDate(null);
            }
            lm.setModifyUser(rs.getString(20));
            try
            {
                lm.setModifyDate(new Date(rs.getTimestamp(21).getTime()));
            }
            catch (Exception e)
            {
                lm.setModifyDate(null);
            }

            leverageMatches.add(lm);

			// we assume all leverage matches in the ResultSet is for same
			// source page and target locale.
            if (sourcePageId == -1)
            {
            	sourcePageId = lm.getSourcePageId();
            }
            if (trgLocaleId == -1)
            {
            	trgLocaleId = lm.getTargetLocaleId();
            }
        }

		List<LeverageMatchAttribute> attrs = LeverageMatchAttributeUtil
				.getLeverageMatchAttrbutes(sourcePageId, trgLocaleId);
		HashMap<String, LeverageMatchAttribute> sidAttrMap = new HashMap<String, LeverageMatchAttribute>(); 
		for (LeverageMatchAttribute attr : attrs)
		{
			if (LeverageMatchAttribute.SID.equalsIgnoreCase(attr.getName()))
			{
				sidAttrMap.put(attr.getUniqueKey(), attr);
			}
		}
		for (LeverageMatch lm : leverageMatches)
		{
			String key = lm.getOriginalSourceTuvId() + "_" + lm.getSubId()
					+ "_" + lm.getOrderNum() + "_" + lm.getTargetLocaleId();
			if (sidAttrMap.get(key) != null)
			{
				lm.setSid(sidAttrMap.get(key).getTextValue());
			}
		}

        return leverageMatches;
    }
}
