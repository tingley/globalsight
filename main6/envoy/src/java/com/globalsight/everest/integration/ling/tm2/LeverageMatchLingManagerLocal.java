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
import java.util.Vector;

import org.apache.log4j.Logger;

import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.tuv.TuImpl;
import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.ling.docproc.extractor.xliff.Extractor;
import com.globalsight.ling.inprogresstm.leverage.LeveragedInProgressTuv;
import com.globalsight.ling.tm.LeverageMatchLingManager;
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
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GlobalSightLocale;

/**
 * Implementation of com.globalsight.ling.tm.LeverageMatchLingManager.
 */
public class LeverageMatchLingManagerLocal implements LeverageMatchLingManager
{
    static private final Logger c_logger = Logger
            .getLogger(LeverageMatchLingManagerLocal.class);

    private static final String FUZZY_LEVERAGE_MATCH_HQL = "from LeverageMatch this "
            + " where this.sourcePageId = :SOURCE_PAGE_ID"
            + "  and this.targetLocale = :TARGET_LOCALE_ID"
            + "  and this.scoreNum < 100 "
            + "  and this.matchType != 'STATISTICS_MATCH'"
            + "  and this.matchType != 'NOT_A_MATCH'";

    private static final String ALL_LEVERAGE_MATCHES_FOR_TUV_HQL = "FROM LeverageMatch lm "
            + "WHERE lm.originalSourceTuvId = :TUV_ID"
            + "  AND lm.targetLocale = :TARGET_LOCALE_ID "
            + "  AND lm.subId = :SUB_ID AND lm.matchType != 'STATISTICS_MATCH'"
            + "  AND lm.matchType != 'NOT_A_MATCH'"
            + "  AND lm.tmId in (tmId_list) "
            + "ORDER BY lm.scoreNum desc, lm.projectTmIndex asc, lm.matchedText asc";

    private static final String ALL_LEVERAGE_MATCHES_FOR_TM_PROCEDENCE = "FROM LeverageMatch lm "
        + "WHERE lm.originalSourceTuvId = :TUV_ID"
        + "  AND lm.targetLocale = :TARGET_LOCALE_ID "
        + "  AND lm.subId = :SUB_ID AND lm.matchType != 'STATISTICS_MATCH'"
        + "  AND lm.matchType != 'NOT_A_MATCH'"
        + "  AND lm.tmId in (tmId_list) "
        + "ORDER BY lm.projectTmIndex asc, lm.scoreNum desc, lm.matchedText asc";

    /**
     * Used to get all exact matches despite of where they come from.
     */
    private static final String EXACT_LEVERAGE_MATCH_HQL = "FROM LeverageMatch lm "
            + " WHERE lm.sourcePageId = :SOURCE_PAGE_ID"
            + " AND lm.targetLocale = :TARGET_LOCALE_ID"
            + " AND lm.scoreNum = 100";
//            + " AND lm.orderNum < " + TmCoreManager.LM_ORDER_NUM_START_TDA;

    private static final String BEST_LEVERAGE_MATCH_HQL = "from LeverageMatch lm "
        + "  WHERE lm.sourcePageId = :SOURCE_PAGE_ID"
        + "  and lm.targetLocale = :TARGET_LOCALE_ID"
        + "  order by lm.originalSourceTuvId, lm.orderNum";
    
    private static final String MAX_ORDER_NUM = 
        "select max(order_num) from leverage_match "
        + "where order_num > 0 "
        + "and order_num < " + TmCoreManager.LM_ORDER_NUM_START_REMOTE_TM + " "
        + "and original_source_tuv_id = ? "
        + "and target_locale_id = ? "
        + "and sub_id = ? ";

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

    public HashMap getFuzzyMatches(Long p_spLgId, Long p_targetLocaleId)
            throws LingManagerException
    {
        Collection leverageMatches = null;

        Vector args = new Vector(2);
        args.add(p_spLgId);
        args.add(p_targetLocaleId);

        try
        {
            // Get a collection of fuzzy matches
            HashMap map = new HashMap();
            map.put("SOURCE_PAGE_ID", p_spLgId);
            map.put("TARGET_LOCALE_ID", p_targetLocaleId);
            leverageMatches = HibernateUtil.search(FUZZY_LEVERAGE_MATCH_HQL,
                    map);

        }
        catch (Exception ex)
        {
            c_logger.error("database error", ex);
            throw new LingManagerException(ex);
        }

        return getLeverageMatchMap(leverageMatches);
    }

    /**
     * Get static leverage matches from DB store for specified source tuvID,
     * target locale ID, subID etc. The matches are stored when creating job.
     */
    public SortedSet getTuvMatches(Long p_sourceTuvId, Long p_targetLocaleId,
            String p_subId, boolean isTmProcedence, long... tmIds)
            throws LingManagerException
    {
        TreeSet set = null;

        Vector args = new Vector(3);
        args.add(p_sourceTuvId);
        args.add(p_targetLocaleId);
        args.add(p_subId);
        StringBuilder tmIdWhereCondition = new StringBuilder();
        String allLeverageMatchesForTuvHql = ALL_LEVERAGE_MATCHES_FOR_TUV_HQL;
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
            allLeverageMatchesForTuvHql = ALL_LEVERAGE_MATCHES_FOR_TUV_HQL
                    .replaceAll("tmId_list", tmIdWhereCondition.toString());
            if (isTmProcedence)
            {
                allLeverageMatchesForTuvHql = ALL_LEVERAGE_MATCHES_FOR_TM_PROCEDENCE
                        .replaceAll("tmId_list", tmIdWhereCondition.toString());
            }

        }
        else
        {
            allLeverageMatchesForTuvHql = ALL_LEVERAGE_MATCHES_FOR_TUV_HQL
                    .replace("AND lm.tmId in (tmId_list) ", "");
            if (isTmProcedence)
            {
                allLeverageMatchesForTuvHql = ALL_LEVERAGE_MATCHES_FOR_TM_PROCEDENCE
                        .replace("AND lm.tmId in (tmId_list) ", "");
            }
        }
        try
        {
            HashMap map = new HashMap();
            map.put("TUV_ID", p_sourceTuvId);
            map.put("TARGET_LOCALE_ID", p_targetLocaleId);
            map.put("SUB_ID", p_subId);
            List list = HibernateUtil.search(allLeverageMatchesForTuvHql, map);
            set = new TreeSet(list);
        }
        catch (Exception ex)
        {
            c_logger.error("database error", ex);
            throw new LingManagerException(ex);
        }

        return set;
    }

    public HashMap getExactMatchesWithSetInside(Long p_sourcePageId,
            Long p_targetLocaleId, int model, TranslationMemoryProfile tmProfile)
    {
        Collection matches = null;
        HashMap result = new HashMap();

        try
        {
            HashMap map = new HashMap();
            map.put("SOURCE_PAGE_ID", p_sourcePageId);
            map.put("TARGET_LOCALE_ID", p_targetLocaleId);
            matches = HibernateUtil.search(EXACT_LEVERAGE_MATCH_HQL, map);
        }
        catch (Exception ex)
        {
            c_logger.error("database error", ex);
            throw new LingManagerException(ex);
        }
        
        // return empty map if no match 
        if (matches == null || matches.size() == 0)
        {
            return result;
        }
        
        // create a new list for all LeverageMatch
        ArrayList<LeverageMatch> allLms = new ArrayList<LeverageMatch>();
        for (Iterator it = matches.iterator(); it.hasNext();)
        {
            LeverageMatch lm = (LeverageMatch) it.next();
            allLms.add(lm);
        }
        matches.clear();

        // get root match with sub match
        ArrayList<LeverageMatch> lmWithSameSrcTuv = new ArrayList<LeverageMatch>();
        ArrayList<LeverageMatch> lmOrderBySub = new ArrayList<LeverageMatch>();
        ArrayList<LeverageMatch> lmOrderBySubLast = new ArrayList<LeverageMatch>();
        int rootSubId = Integer.parseInt(SegmentTmTu.ROOT);
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

            TuvImpl tuv = HibernateUtil.get(TuvImpl.class, srcTuvHandling.longValue());
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

            // must have root sub
            if (!subidList.contains(rootSubId))
            {
                allLms.removeAll(lmWithSameSrcTuv);
                lmWithSameSrcTuv.clear();
                continue;
            }

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
                    Map segmentMap = new HashMap();

                    for (LeverageMatch lm : lmOrderBySub)
                    {
                        segmentMap.put(lm.getSubId(), lm.getMatchedText());
                        lmOrderBySubLast.add(lm);
                    }

                    try
                    {
                        String segmengString = tuv.getSegmentString();
                        segmengString = segmengString != null ? segmengString : tuv
                                .getSegmentClob();
                        matchedGxml = TmUtil.composeCompleteText(segmengString, segmentMap);
                    }
                    catch (Exception e)
                    {
                        /*
                         * Change error info into warning info,
                         * only main segment and subflows are both
                         * exact match can we get a normal matchedGxml.
                         * If either of them fails, we throw a warning.
                         */
                        c_logger.warn(e);
                    }

                    if (matchedGxml != null && rootLm != null)
                    {
                        LeverageSegment ls = new LeverageSegment(matchedGxml, LeverageDataCenter
                                .getTuvState(rootLm.getMatchType()), rootLm.getModifyDate(), rootLm
                                .getProjectTmIndex(), rootLm.getMatchedSid(), rootLm
                                .getMatchedTuvId());
                        ls.setOrgSid(rootLm.getOrgSid());
                        ArrayList l = (ArrayList) result.get(new Long(rootLm
                                .getOriginalSourceTuvId()));
                        if (l != null && l.size() != 0)
                        {
                            l.add(ls);
                            Collections.sort(l, new ComparatorByModifyDate(model, tmProfile));
                            result.put(srcTuvHandling, l);
                        }
                        else
                        {
                            l = new ArrayList();
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

    public HashMap getExactMatches(Long p_sourcePageId, Long p_targetLocaleId)
            throws LingManagerException
    {
        Collection matches = null;

        try
        {
            HashMap map = new HashMap();
            map.put("SOURCE_PAGE_ID", p_sourcePageId);
            map.put("TARGET_LOCALE_ID", p_targetLocaleId);
            matches = HibernateUtil.search(EXACT_LEVERAGE_MATCH_HQL, map);
        }
        catch (Exception ex)
        {
            c_logger.error("database error", ex);
            throw new LingManagerException(ex);
        }

        HashMap result = new HashMap();
        for (Iterator it = matches.iterator(); it.hasNext();)
        {
            LeverageMatch lm = (LeverageMatch) it.next();

            String gxml = lm.getMatchedText();

            // LeverageSegment ls = new LeverageSegment(gxml, lm.getMatchType(),
            // lm.getModifyDate(), lm.getProjectTmIndex());
            LeverageSegment ls = new LeverageSegment(gxml, LeverageDataCenter
                    .getTuvState(lm.getMatchType()), lm.getModifyDate(), lm
                    .getProjectTmIndex(), lm.getMatchedSid(), lm
                    .getMatchedTuvId());
            result.put(new Long(lm.getOriginalSourceTuvId()), ls);
        }

        return result;
    }

    /**
     * Retrieves a mapping of tuv id + sub id to match type in a given leveraged
     * target page.
     */
    public MatchTypeStatistics getMatchTypesForStatistics(Long p_sourcePageId,
            Long p_targetLocaleId, int p_levMatchThreshold)
            throws LingManagerException
    {
    	String sql = BEST_LEVERAGE_MATCH_HQL;

        Collection leverageMatches = null;
        HashMap leveragematchesMap = new HashMap();
        try
        {
            HashMap map = new HashMap();
            map.put("SOURCE_PAGE_ID", p_sourcePageId);
            map.put("TARGET_LOCALE_ID", p_targetLocaleId);

            leverageMatches = HibernateUtil.search(sql, map);
            // remove lower score_num record
            for (Iterator it = leverageMatches.iterator(); it.hasNext();)
            {
                LeverageMatch match = (LeverageMatch) it.next();
                LeverageMatch cloneMatch = new LeverageMatch(match);
                long originalSourceTuvId = cloneMatch.getOriginalSourceTuvId();
                
                // For WS XLF file,MT translation should NOT impact
                // word-count,displayed color.
                if (!isIncludeMtMatches)
                {
                    try
                    {
                        TuImpl tu = (TuImpl) ServerProxy.getTuvManager()
                                .getTuvForSegmentEditor(originalSourceTuvId).getTu();
                        String translationType = tu.getXliffTranslationType();
                        float tm_score = Float.parseFloat(tu.getIwsScore());
                        if (Extractor.IWS_TRANSLATION_MANUAL.equalsIgnoreCase(translationType)) {
                            cloneMatch.setScoreNum(100);
                        } else if (Leverager.MT_PRIORITY == cloneMatch.getProjectTmIndex()) {
                            cloneMatch.setScoreNum(tm_score);
                        }
                    }
                    catch (Exception e)
                    {
                    }
                }

                float scoreNum = cloneMatch.getScoreNum();
                String subId = cloneMatch.getSubId();
                String idKey = MatchTypeStatistics.makeKey(originalSourceTuvId, subId);
                LeverageMatch lm = (LeverageMatch) leveragematchesMap.get(idKey);
                if (lm != null)
                {
                    if (scoreNum == 100)
                    {
                        if ((LeverageUtil.compareSid(lm, cloneMatch) > 0 
                                && cloneMatch.getOrderNum() != -1)
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
            c_logger.error("database error", ex);
            throw new LingManagerException(ex);
        }

        MatchTypeStatistics result = new MatchTypeStatistics(
                p_levMatchThreshold);

        // set the match type with the found leverage matches
        Collection leverageMatches2 = leveragematchesMap.values();
        List<String> list = new ArrayList<String>();
        for (Iterator it = leverageMatches2.iterator(); it.hasNext();)
        {
            LeverageMatch match = (LeverageMatch) it.next();
            String key = MatchTypeStatistics.makeKey(match
                    .getOriginalSourceTuvId(), match.getSubId());
            if (!list.contains(key))
            {
                result.addMatchType(match);
                result.addMatchTypeForCosting(match);
                list.add(key);
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
            String p_subId, Long p_targetLocaleId, Long p_orderNum)
            throws LingManagerException
    {
        String hql = getDeleteHql(p_OriginalSourceTuvId, p_subId,
                p_targetLocaleId, p_orderNum);

        HashMap map = getParamMap(p_OriginalSourceTuvId, p_subId,
                p_targetLocaleId, p_orderNum);
        
        try
        {
            Collection results = HibernateUtil.search(hql.toString(), map);
            if (results != null && results.size() > 0) {
                HibernateUtil.delete(results);
            }
        }
        catch (Exception ex)
        {
            c_logger.error(
                    "Failed to delete leverage matches for originalSourceTuvID : "
                            + p_OriginalSourceTuvId, ex);
            throw new LingManagerException(ex);
        }
    }
    
    private String getDeleteHql(Long p_OriginalSourceTuvId, String p_subId,
            Long p_targetLocaleId, Long p_orderNum)
    {
        StringBuffer hql = new StringBuffer();
        
        hql.append(" from LeverageMatch lm ");
        // "p_OriginalSourceTuvId" can't be null
        hql.append(" WHERE lm.originalSourceTuvId = :TUV_ID");
        
        if (p_subId != null) {
            hql.append(" AND lm.subId = :SUB_ID");
        }
        if (p_targetLocaleId != null) {
            hql.append(" AND lm.targetLocale = :TARGET_LOCALE_ID");
        }
        if (p_orderNum != null) {
            hql.append(" AND lm.orderNum = :ORDER_NUM");
        }
        
        return hql.toString();
    }
    
    private HashMap<String, Object> getParamMap(Long p_OriginalSourceTuvId,
            String p_subId, Long p_targetLocaleId, Long p_orderNum)
    {
        HashMap<String, Object> map = new HashMap();
        map.put("TUV_ID", p_OriginalSourceTuvId);
        
        if (p_subId != null) {
            map.put("SUB_ID", p_subId);
        }
        if (p_targetLocaleId != null) {
            map.put("TARGET_LOCALE_ID", p_targetLocaleId);            
        }
        if (p_orderNum != null) {
            map.put("ORDER_NUM", p_orderNum);
        }

        return map;
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

    // Get a hash map from a collection of LeverageMatch
    private HashMap getLeverageMatchMap(Collection p_leverageMatches)
    {
        // Put all the LeverageMatch in HashMap grouping by original
        // Tuv id
        HashMap result = new HashMap();

        for (Iterator it = p_leverageMatches.iterator(); it.hasNext();)
        {
            LeverageMatch match = (LeverageMatch) it.next();

            Long key = new Long(match.getOriginalSourceTuvId());
            Set set = (Set) result.get(key);

            if (set == null)
            {
                set = new TreeSet();
                result.put(key, set);
            }

            // TreeSet sorts the elements
            set.add(match);
        }
        return result;
    }

    public Map getExactMatchesForDownLoadTmx(Long p_spLgId,
            Long p_targetLocaleId)
    {
        Collection leverageMatches = null;

        try
        {
            HashMap map = new HashMap();
            map.put("SOURCE_PAGE_ID", p_spLgId);
            map.put("TARGET_LOCALE_ID", p_targetLocaleId);
            leverageMatches = HibernateUtil.search(EXACT_LEVERAGE_MATCH_HQL,
                    map);
        }
        catch (Exception ex)
        {
            c_logger.error("database error", ex);
            throw new LingManagerException(ex);
        }

        return getLeverageMatchMap(leverageMatches);
    }

    @SuppressWarnings("unchecked")
    public void updateProjectTmIndex(long tmId, int projectTmIndex,
            long tmProfileId)
    {
        Connection connection = null;
        PreparedStatement ps = null;
        try
        {
            connection = DbUtil.getConnection();
            String sql = "update leverage_match set project_tm_index = ? where tm_id = ? and tm_profile_id=?";
            ps = connection.prepareStatement(sql);
            ps.setInt(1, projectTmIndex);
            ps.setLong(2, tmId);
            ps.setLong(3, tmProfileId);
            ps.executeUpdate();
        }
        catch (Exception ex)
        {
            c_logger.error("database error", ex);
            throw new LingManagerException(ex);
        }
        finally
        {
            if (ps != null)
            {
                try
                {
                    ps.close();
                }
                catch (SQLException e)
                {
                    c_logger.error("Can not close the preparedStatement");
                }
            }
            if (connection != null)
            {
                try
                {
                    DbUtil.returnConnection(connection);
                }
                catch (Exception e)
                {
                    c_logger.error("Can not close the connection");
                }
            }
        }
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
        PreparedStatement stmt = null;
        try {
            stmt = p_connection.prepareStatement(MAX_ORDER_NUM);
        } catch (SQLException e) {
            c_logger.error("Failed to get preparedStatement", e);
        }

        Set<LeverageMatch> nonClobMatches = new HashSet();
        // Collection clobMatches = new ArrayList();
        LeverageOptions leverageOptions = 
            p_leverageDataCenter.getLeverageOptions();
        // walk through all LeverageMatches in p_leverageDataCenter
        Iterator itLeverageMatches;
        try {
            itLeverageMatches = p_leverageDataCenter.leverageResultIterator();
        } catch (Exception e) {
            throw new LingManagerException(e);
        }
        
        while (itLeverageMatches != null && itLeverageMatches.hasNext())
        {
            LeverageMatches levMatches = 
                (LeverageMatches) itLeverageMatches.next();

            // walk through all target locales in the LeverageMatches
            Iterator itLocales = levMatches.targetLocaleIterator();
            while (itLocales.hasNext())
            {
                try {
                    GlobalSightLocale targetLocale = 
                        (GlobalSightLocale) itLocales.next();

                    // walk through all matches in the locale
                    Collection<LeverageMatch> subNonClobMatches = getNonClobMatches(
                            stmt, levMatches, targetLocale, leverageOptions,
                            p_sourcePage.getId(), null);
                    nonClobMatches.addAll(subNonClobMatches);
                } catch (Exception e) {
                    c_logger.error("Failed to getNonClobMatches *.", e);
                    throw new LingManagerException(e);
                }
            }
        }
        
        try {
            if (stmt != null) {
                stmt.close();
            }
        } catch (SQLException sqlEx) {
            c_logger.error("Failed to close preparedStatement", sqlEx);
        }

        // save matches to the database
        if (nonClobMatches.size() > 0) {
            saveLeveragedMatches(nonClobMatches);
        }
    }
    
    public void saveLeverageResults(Connection p_connection,
            long p_sourcePageId, Map<Long, LeverageMatches> p_leverageMatchesMap,
            GlobalSightLocale p_targetLocale, LeverageOptions p_leverageOptions)
            throws LingManagerException
    {
        Set<LeverageMatch> nonClobMatches = new HashSet();

        PreparedStatement stmt = null;
        try {
            stmt = p_connection.prepareStatement(MAX_ORDER_NUM);
        } catch (SQLException e) {
            c_logger.error("Failed to get preparedStatement", e);
        }

        Iterator iter = p_leverageMatchesMap.entrySet().iterator();
        while (iter.hasNext())
        {
            Map.Entry entry = (Map.Entry) iter.next();
            Long originalSourceTuvId = (Long) entry.getKey();
            LeverageMatches levMatches = (LeverageMatches) entry.getValue();

            try {
                Collection<LeverageMatch> matches = getNonClobMatches(stmt,
                        levMatches, p_targetLocale, p_leverageOptions,
                        p_sourcePageId, originalSourceTuvId);
                nonClobMatches.addAll(matches);
            } catch (Exception e) {
                c_logger.error("Failed to getNonClobMatches **.", e);
                throw new LingManagerException(e);
            }
        }
        
        try {
            if (stmt != null) {
                stmt.close();
            }
        } catch (SQLException sqlEx) {
            c_logger.error("Failed to close preparedStatement", sqlEx);
        }

        // save matches to the database
        if (nonClobMatches.size() > 0) {
            saveLeveragedMatches(nonClobMatches);
        }
    }
    
    public void saveLeveragedMatches(Collection p_leverageMatchList)
            throws LingManagerException
    {
        try
        {
            HibernateUtil.save(p_leverageMatchList);
        }
        catch (Exception ex)
        {
            c_logger.error("Failed to save leverage matches into DB", ex);
            throw new LingManagerException(ex);
        }
    }

    /**
     * Transfer matches in "LeverageMatches" into "LeverageMatch" object.The
     * data in "LeverageMatches" may be "LeveragedSegmentTuv"(from gold TM) or
     * be "LevereageInProgressTuv"(from in progress TM).
     * 
     */
    private Collection<LeverageMatch> getNonClobMatches(
            PreparedStatement p_stmt, LeverageMatches p_levMatches,
            GlobalSightLocale p_targetLocale,
            LeverageOptions p_leverageOptions, long p_sourcePageId,
            Long p_originalSourceTuvId) throws Exception
    {
        Collection<LeverageMatch> results = new ArrayList();

        Iterator itMatch = p_levMatches.matchIterator(p_targetLocale);
        while (itMatch.hasNext())
        {
            LeveragedTuv matchedTuv = (LeveragedTuv) itMatch.next();
            boolean isLeveragedSegmentTuv = 
                (matchedTuv instanceof LeveragedSegmentTuv);
            boolean isLeveragedInProgressTuv = 
                (matchedTuv instanceof LeveragedInProgressTuv);
            String segNoTopTag = matchedTuv.getSegmentNoTopTag();
            // For now,I can't see other TUV types which are from "LeverageMatches".
            if (segNoTopTag != null && !segNoTopTag.equals("")
                    && (isLeveragedSegmentTuv || isLeveragedInProgressTuv))
            {
                SegmentTmTuv originalSourceTuv = 
                    (SegmentTmTuv) p_levMatches.getOriginalTuv();
                
                LeverageMatch lm = new LeverageMatch();
                // 1.source_page_id
                lm.setSourcePageId(p_sourcePageId);
                // 2.original_source_tuv_id
                if (p_originalSourceTuvId != null && p_originalSourceTuvId > 0) {
                    lm.setOriginalSourceTuv(p_originalSourceTuvId);
                } else {
                    lm.setOriginalSourceTuv(originalSourceTuv.getId());                    
                }
                // 3.sub_id
                SegmentTmTu originalTu = 
                    (SegmentTmTu) originalSourceTuv.getTu();
                lm.setSubId(originalTu.getSubId());
                // 4.matched_text_string
                lm.setMatchedText(matchedTuv.getSegment());
                // 5.target_locale_id
                lm.setTargetLocale(p_targetLocale);
                // 6.match_type
                lm.setMatchType(matchedTuv.getMatchState().getName());
                // 7.order_num
                int maxOrderNum = getMaxOrderNum(p_stmt, originalSourceTuv
                        .getId(), p_targetLocale.getId(), originalTu.getSubId());
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
                    // ID of "ip_tm_trg_X",not the real matchedTuvId from project
                    // TM, so keep it -1.
                    lm.setMatchedTuvId(-1);
                    // If match is from in-progress TM,the tmId is "jobId",not
                    // the real project TM ID.
                    lm.setTmId(tmId);
                    // For now,leverage_match data is from project TM or
                    // in-progress TM,so it is safe to set "-7" here.
                    lm.setProjectTmIndex(Leverager.IN_PROGRESS_TM_PRIORITY);
                    
                    long jobDataTuId = 
                        ((LeveragedInProgressTuv) matchedTuv).getJobDataTuId();
                    lm.setJobDataTuId(jobDataTuId);
                }
                // 13.tm_profile_id
                long tmProfileId = p_leverageOptions.getTmProfileId();
                lm.setTmProfileId(tmProfileId);
                // 14.matched_original_source
                String matchedOriginalSource = 
                    matchedTuv.getSourceTuv().getSegment();
                lm.setMatchedOriginalSource(matchedOriginalSource);

                results.add(lm);
            }
        }
        
        return results;
    }
    
    /**
     * Get max order_num per specified TUV.
     * 
     * @return -- the max order NUM.
     */
    private int getMaxOrderNum(PreparedStatement p_stmt,
            long p_originalSourceTuvId, long p_targetLocaleId, String p_subId)
            throws Exception
    {
        // default;
        int maxOrderNum = 0;

        ResultSet rs = null;
        try
        {
            p_stmt.setLong(1, p_originalSourceTuvId);
            p_stmt.setLong(2, p_targetLocaleId);
            p_stmt.setString(3, p_subId);
            
            rs = p_stmt.executeQuery();

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
        }

        return maxOrderNum;
    }
    
    public static int getMatchTableType(LeveragedTuv p_matchedTuv)
    {
        // Default
        int type = SEGMENT_TM_T;
        int matchedTableType = 
            ((LeveragedTu) p_matchedTuv.getTu()).getMatchTableType();
        
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
    
    class ComparatorByModifyDate implements Comparator
    {
        private int model;
        private TranslationMemoryProfile tmProfile;

        public ComparatorByModifyDate(int model,
                TranslationMemoryProfile tmProfile)
        {
            this.model = model;
            this.tmProfile = tmProfile;
        }

        public int compare(Object arg0, Object arg1)
        {
            LeverageSegment ls1 = (LeverageSegment) arg0;
            LeverageSegment ls2 = (LeverageSegment) arg1;

            int result = LeverageUtil.compareSid(ls1, ls2);
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

}
