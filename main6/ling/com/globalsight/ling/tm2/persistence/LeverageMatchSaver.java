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
package com.globalsight.ling.tm2.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.ling.tm2.SegmentTmTu;
import com.globalsight.ling.tm2.SegmentTmTuv;
import com.globalsight.ling.tm2.leverage.LeverageDataCenter;
import com.globalsight.ling.tm2.leverage.LeverageMatches;
import com.globalsight.ling.tm2.leverage.LeverageOptions;
import com.globalsight.ling.tm2.leverage.LeveragedTu;
import com.globalsight.ling.tm2.leverage.LeveragedTuv;
import com.globalsight.ling.tm2.leverage.Leverager;
import com.globalsight.util.GlobalSightLocale;

/**
 * LeverageMatchSaver saves matched segments to leverage_match table.
 * 
 * @deprecated - should use
 *             "LeverageMatchLingManagerLocal.saveLeverageResults(...)" API.
 */
public class LeverageMatchSaver
{
    private static final Logger c_logger = Logger
            .getLogger(LeverageMatchSaver.class.getName());

    // insert statement
    private static final String INSERT_LEVERAGE_MATCH_NON_CLOB = "INSERT INTO leverage_match (source_page_id, "
            + "original_source_tuv_id, sub_id, matched_text_string, "
            + "target_locale_id, match_type, order_num, score_num, "
            + "matched_tuv_id, matched_table_type, project_tm_index, tm_id, tm_profile_id, matched_original_source) VALUES(?, ?, ?, ?, "
            + "?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    // private static final String INSERT_LEVERAGE_MATCH_CLOB
    // = "INSERT INTO leverage_match (source_page_id, "
    // + "original_source_tuv_id, sub_id, matched_text_clob, "
    // + "target_locale_id, match_type, order_num, score_num, "
    // + "matched_tuv_id, matched_table_type) VALUES(?, ?, ?, EMPTY_CLOB(), "
    // + "?, ?, ?, ?, ?, ?)";

    static private final String SELECT_CLOB = "SELECT matched_text_clob FROM leverage_match "
            + "WHERE original_source_tuv_id = ? AND sub_id = ? AND "
            + "target_locale_id = ? AND order_num = ? FOR UPDATE";

    static private final String SELECT_EXISTING_RECORD = "SELECT original_source_tuv_id FROM leverage_match "
            + "WHERE original_source_tuv_id = ? AND sub_id = ? AND "
            + "target_locale_id = ? AND order_num = ?";

    // constants that indicate which table the matches come from
    private static final int SEGMENT_TM_T = 1;
    private static final int SEGMENT_TM_L = 2;
    private static final int PAGE_TM_T = 3;
    private static final int PAGE_TM_L = 4;

    private Connection m_connection;

    // constructor
    public LeverageMatchSaver(Connection p_connection) throws Exception
    {
        m_connection = p_connection;
    }

    /**
     * Save matched segments to the database
     * 
     * @param p_sourcePage
     *            SourcePage object
     * @param p_leverageDataCenter
     *            Repository of matches of a page
     */
    @SuppressWarnings("unchecked")
    public void saveMatchesToDb(SourcePage p_sourcePage,
            LeverageDataCenter p_leverageDataCenter) throws Exception
    {
        String companyId = p_sourcePage != null ? String.valueOf(p_sourcePage
                .getCompanyId()) : CompanyWrapper.getCurrentCompanyId();
        Collection nonClobMatches = new ArrayList();
        // Collection clobMatches = new ArrayList();
        LeverageOptions leverageOptions = p_leverageDataCenter
                .getLeverageOptions();
        // walk through all LeverageMatches in p_leverageDataCenter
        Iterator itLeverageMatches = p_leverageDataCenter
                .leverageResultIterator();
        while (itLeverageMatches.hasNext())
        {
            LeverageMatches levMatches = (LeverageMatches) itLeverageMatches
                    .next();

            // walk through all target locales in the LeverageMatches
            Iterator itLocales = levMatches.targetLocaleIterator(companyId);
            while (itLocales.hasNext())
            {
                GlobalSightLocale targetLocale = (GlobalSightLocale) itLocales
                        .next();

                // walk through all matches in the locale
                Iterator itMatch = levMatches.matchIterator(targetLocale,
                        companyId);
                while (itMatch.hasNext())
                {
                    LeveragedTuv matchedTuv = (LeveragedTuv) itMatch.next();
                    long tmId = matchedTuv.getTu().getTmId();
                    long tmProfileId = leverageOptions.getTmProfileId();
                    int projectTmIndex = Leverager.getProjectTmIndex(
                            leverageOptions, tmId);
                    // classify the matches to clob and non-clob matches
                    String segNoTopTag = matchedTuv.getSegmentNoTopTag();
                    if (segNoTopTag != null && !segNoTopTag.equals(""))
                    {
                        MatchInfo matchInfo = new MatchInfo(
                                (SegmentTmTuv) levMatches.getOriginalTuv(),
                                targetLocale, matchedTuv, projectTmIndex, tmId,
                                tmProfileId, matchedTuv.getSourceTuv()
                                        .getSegment());
                        nonClobMatches.add(matchInfo);
                    }

                    /**
                     * The condition will always be false for MySql.
                     */
                    // if(matchedTuv.isClobSegment())
                    // {
                    // clobMatches.add(matchInfo);
                    // }
                    // else
                    // {
                    // nonClobMatches.add(matchInfo);
                    // }
                }
            }
        }

        // save matches to the database
        saveNonClobMatches(nonClobMatches, p_sourcePage);
        // saveClobMatches(clobMatches, p_sourcePage);
    }

    // save non clob segments to the database
    private void saveNonClobMatches(Collection p_matches,
            SourcePage p_sourcePage) throws Exception
    {
        PreparedStatement ps = null;
        PreparedStatement ps1 = null;
        try
        {
            ps = m_connection.prepareStatement(INSERT_LEVERAGE_MATCH_NON_CLOB);
            ps1 = m_connection.prepareStatement(SELECT_EXISTING_RECORD);

            int batchCount = 0;
            Iterator itMatch = p_matches.iterator();
            while (itMatch.hasNext())
            {
                MatchInfo matchInfo = (MatchInfo) itMatch.next();

                // For the LEVERAGE_MATCH_PK violated issue.
                // If the result is already recorded in the
                // LEVERAGE_MATCH table, then ignore it.
                if (existsInTable(matchInfo, ps1))
                {
                    continue;
                }
                ps.setLong(1, p_sourcePage.getId());
                ps.setLong(2, matchInfo.getOriginalTuvId());
                ps.setString(3, matchInfo.getSubId());
                ps.setString(4, matchInfo.getMatchedText());
                ps.setLong(5, matchInfo.getTargetLoaleId());
                ps.setString(6, matchInfo.getMatchType());
                ps.setLong(7, matchInfo.getOrderNum());
                ps.setFloat(8, matchInfo.getScore());
                ps.setLong(9, matchInfo.getMatchedTuvId());
                ps.setLong(10, matchInfo.getMatchTableType());
                ps.setInt(11, matchInfo.getTmIndex());
                ps.setLong(12, matchInfo.getTmId());
                ps.setLong(13, matchInfo.getTmProfileId());
                ps.setString(14, matchInfo.getMatchedOriginalSource());
                ps.addBatch();

                batchCount++;
                if (batchCount > DbUtil.BATCH_INSERT_UNIT)
                {
                    ps.executeBatch();
                    batchCount = 0;
                }
            }

            if (batchCount > 0)
            {
                ps.executeBatch();
            }
            m_connection.commit();
        }
        catch (Exception e)
        {
            m_connection.rollback();
            throw e;
        }
        finally
        {
            if (ps != null)
            {
                ps.close();
            }
            if (ps1 != null)
            {
                ps1.close();
            }
        }
    }

    private boolean existsInTable(MatchInfo p_matchInfo, PreparedStatement p_ps)
            throws Exception
    {
        ResultSet rs = null;
        try
        {
            p_ps.setLong(1, p_matchInfo.getOriginalTuvId());
            p_ps.setString(2, p_matchInfo.getSubId());
            p_ps.setLong(3, p_matchInfo.getTargetLoaleId());
            p_ps.setLong(4, p_matchInfo.getOrderNum());

            rs = p_ps.executeQuery();

            return rs.next();
        }
        finally
        {
            DbUtil.silentClose(rs);
        }
    }

    // save clob segments to the database
    // private void saveClobMatches(
    // Collection p_matches, SourcePage p_sourcePage)
    // throws Exception
    // {
    // PreparedStatement ps = null;
    // PreparedStatement ps1 = null;
    // try
    // {
    // ps = m_connection.prepareStatement(
    // INSERT_LEVERAGE_MATCH_CLOB);
    // ps1 = m_connection.prepareStatement(SELECT_EXISTING_RECORD);
    //
    // int batchCount = 0;
    // Iterator itMatch = p_matches.iterator();
    // while(itMatch.hasNext())
    // {
    // MatchInfo matchInfo = (MatchInfo)itMatch.next();
    //
    // //For the LEVERAGE_MATCH_PK violated issue.
    // //If the result is already recorded in the
    // //LEVERAGE_MATCH table, then ignore it.
    // if (existsInTable(matchInfo, ps1))
    // {
    // continue;
    // }
    // ps.setLong(1, p_sourcePage.getId());
    // ps.setLong(2, matchInfo.getOriginalTuvId());
    // ps.setString(3, matchInfo.getSubId());
    // ps.setLong(4, matchInfo.getTargetLoaleId());
    // ps.setString(5, matchInfo.getMatchType());
    // ps.setLong(6, matchInfo.getOrderNum());
    // ps.setLong(7, matchInfo.getScore());
    // ps.setLong(8, matchInfo.getMatchedTuvId());
    // ps.setLong(9, matchInfo.getMatchTableType());
    // ps.addBatch();
    //
    // batchCount++;
    // if(batchCount > DbUtil.BATCH_INSERT_UNIT)
    // {
    // ps.executeBatch();
    // batchCount = 0;
    // }
    // }
    //
    // if(batchCount > 0)
    // {
    // ps.executeBatch();
    // }
    //
    // writeClobSegments(p_matches);
    //
    // m_connection.commit();
    // }
    // catch(Exception e)
    // {
    // m_connection.rollback();
    // throw e;
    // }
    // finally
    // {
    // if(ps != null)
    // {
    // ps.close();
    // }
    // if(ps1 != null)
    // {
    // ps1.close();
    // }
    // }
    // }

    // write CLOB columns
    // private void writeClobSegments(Collection p_matches)
    // throws Exception
    // {
    // PreparedStatement ps = null;
    // ResultSet rs = null;
    // try
    // {
    // ps = m_connection.prepareStatement(SELECT_CLOB);
    // Iterator itMatch = p_matches.iterator();
    // while(itMatch.hasNext())
    // {
    // MatchInfo matchInfo = (MatchInfo)itMatch.next();
    // ps.setLong(1, matchInfo.getOriginalTuvId());
    // ps.setString(2, matchInfo.getSubId());
    // ps.setLong(3, matchInfo.getTargetLoaleId());
    // ps.setLong(4, matchInfo.getOrderNum());
    //
    // rs = ps.executeQuery();
    //
    // if (rs.next())
    // {
    // DbUtil.writeClob(rs, 1, matchInfo.getMatchedText());
    // }
    // DbUtil.silentClose(rs);
    // }
    // }
    // finally
    // {
    // DbUtil.silentClose(rs);
    // DbUtil.silentClose(ps);
    // }
    // }

    // Helper class to hold match information needed to insert row
    // into leverage_match table
    private class MatchInfo
    {
        private long m_originalTuvId;
        private String m_subId;
        private GlobalSightLocale m_targetLocale;
        private LeveragedTuv m_matchedTuv;
        private int tmIndex = -1;
        private long tmId = 0;
        private long tmProfileId = 0;
        private String matchedOriginalSource = null;

        MatchInfo(SegmentTmTuv p_originalSourceTuv,
                GlobalSightLocale p_targetLocale, LeveragedTuv p_matchedTuv,
                int tmIndex, long tmId, long tmProfileId,
                String matchedOriginalSource)
        {
            m_originalTuvId = p_originalSourceTuv.getId();
            m_subId = ((SegmentTmTu) p_originalSourceTuv.getTu()).getSubId();
            m_targetLocale = p_targetLocale;
            m_matchedTuv = p_matchedTuv;
            this.tmIndex = tmIndex;
            this.tmId = tmId;
            this.tmProfileId = tmProfileId;
            this.matchedOriginalSource = matchedOriginalSource;
        }

        String getMatchedOriginalSource()
        {
            return matchedOriginalSource;
        }

        public long getTmProfileId()
        {
            return tmProfileId;
        }

        public long getTmId()
        {
            return this.tmId;
        }

        int getTmIndex()
        {
            return this.tmIndex;
        }

        long getOriginalTuvId()
        {
            return m_originalTuvId;
        }

        String getSubId()
        {
            return m_subId;
        }

        String getMatchedText()
        {
            return m_matchedTuv.getSegment();
        }

        long getTargetLoaleId()
        {
            return m_targetLocale.getId();
        }

        String getMatchType()
        {
            return m_matchedTuv.getMatchState().getName();
        }

        int getOrderNum()
        {
            return m_matchedTuv.getOrder();
        }

        float getScore()
        {
            return m_matchedTuv.getScore();
        }

        long getMatchedTuvId()
        {
            return m_matchedTuv.getId();
        }

        int getMatchTableType()
        {
            int type;

            if (((LeveragedTu) m_matchedTuv.getTu()).getMatchTableType() == LeveragedTu.PAGE_TM)
            {
                if (m_matchedTuv.isTranslatable())
                {
                    type = PAGE_TM_T;
                }
                else
                {
                    type = PAGE_TM_L;
                }
            }
            else
            {
                if (m_matchedTuv.isTranslatable())
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

    }

}
