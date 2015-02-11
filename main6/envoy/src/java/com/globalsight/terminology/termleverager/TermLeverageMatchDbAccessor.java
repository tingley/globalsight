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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.globalsight.everest.persistence.tuv.BigTableUtil;
import com.globalsight.everest.persistence.tuv.TuvQueryConstants;
import com.globalsight.terminology.util.SqlUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.database.PreparedStatementBatch;

/**
 * The TermLeverageMatchDbAccessor accesses the data from the
 * TERM_LEVERAGE_MATCH table.
 */
class TermLeverageMatchDbAccessor
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            TermLeverageMatchDbAccessor.class);

    static private final String INSERT_SQL =
        "insert into TERM_LEVERAGE_MATCH values (?, ?, ?, ?, ?, ?, ?)";

    private static final int BATCH_SIZE = 5000;

    // Note: the select queries are used by the online editor. They
    // may run slow on large termbases since only IDs are stored and
    // the terms are retrieved through a join with TB_TERM.
    // We may consider persisting the terms as well.
    // Also, term IDs are not stable in 6.3/7.0, so the match may
    // actually refer to a different term in a different language.

    // IMPORTANT:
    // Whenever returned column list is changed,
    // SELECT_TUV_MATCH_COLUMN_MAP must also be changed.
    static private final String SELECT_TUV_MATCH_SQL
        = "SELECT tlm.source_term_id, tlm.target_term_id, tlm.score, "
        + "src_term.cid, src_term.term, tgt_term.cid, tgt_term.term, src_term.xml "
        + "FROM term_leverage_match tlm, tb_term src_term, tb_term tgt_term "
        + "WHERE tlm.source_tuv_id = ? AND tlm.target_page_locale = ? "
        + "AND tlm.source_term_id = src_term.tid "
        + "AND tlm.target_term_id = tgt_term.tid "
        + "ORDER BY tlm.order_num";

    // IMPORTANT:
    // Whenever returned column list is changed,
    // SELECT_PAGE_MATCH_COLUMN_MAP must also be changed.
    static private final String SELECT_PAGE_MATCH_SQL
        = "SELECT tlm.source_term_id, tlm.target_term_id, tlm.score, "
        + "src_term.cid, src_term.term, tgt_term.cid, tgt_term.term, "
        + "tlm.source_tuv_id, src_term.xml "
        + "FROM term_leverage_match tlm, tb_term src_term, tb_term tgt_term, "
        + "source_page_leverage_group splg, "
        + TuvQueryConstants.TU_TABLE_PLACEHOLDER + " tu, "
        + TuvQueryConstants.TUV_TABLE_PLACEHOLDER + " tuv "
        + "WHERE splg.sp_id = ? AND splg.lg_id = tu.leverage_group_id "
        + "AND tu.id = tuv.tu_id AND tuv.locale_id = ? "
        + "AND tuv.id = tlm.source_tuv_id AND tlm.target_page_locale = ? "
        + "AND tlm.source_term_id = src_term.tid "
        + "AND tlm.target_term_id = tgt_term.tid "
        + "ORDER BY tlm.source_tuv_id, tlm.order_num";

    static private final HashMap SELECT_TUV_MATCH_COLUMN_MAP =
        setSelectTuvMatchColumnMap();

    static private final HashMap SELECT_PAGE_MATCH_COLUMN_MAP =
        setSelectPageMatchColumnMap();

    // This method supplies a map from column names to indices in
    // SELECT_TUV_MATCH_SQL expression. The indices are used to
    // retrieve column values through java.sql.ResultSet#getXXX()
    // method. It is important to maintain the consistency between
    // this method and the above SQL expression.
    static private HashMap setSelectTuvMatchColumnMap()
    {
        HashMap map = new HashMap();

        map.put("tlm.source_term_id", new Integer(1));
        map.put("tlm.target_term_id", new Integer(2));
        map.put("tlm.score", new Integer(3));
        map.put("src_term.cid", new Integer(4));
        map.put("src_term.term", new Integer(5));
        map.put("tgt_term.cid", new Integer(6));
        map.put("tgt_term.term", new Integer(7));
        map.put("src_term.xml", new Integer(8));

        return map;
    }

    // This method supplies a map from column names to indices in
    // SELECT_PAGE_MATCH_SQL expression. The indices are used to
    // retrieve column values through java.sql.ResultSet#getXXX()
    // method. It is important to maintain the consistency between
    // this method and the above SQL expression.
    static private HashMap setSelectPageMatchColumnMap()
    {
        HashMap map = new HashMap();

        map.put("tlm.source_term_id", new Integer(1));
        map.put("tlm.target_term_id", new Integer(2));
        map.put("tlm.score", new Integer(3));
        map.put("src_term.cid", new Integer(4));
        map.put("src_term.term", new Integer(5));
        map.put("tgt_term.cid", new Integer(6));
        map.put("tgt_term.term", new Integer(7));
        map.put("tlm.source_tuv_id", new Integer(8));
        map.put("src_term.xml", new Integer(9));

        return map;
    }


    /**
     * Inner class of TermLeverageMatchDbAccessor. This class is used
     * to return a ResultSet along with a Statement so that after
     * using the ResultSet and Statement, the connection can be closed
     * and returned to application server.
     *
     * CvdL: I'm not sure this is correct.
     */
    static class SelectResult
    {
        private ResultSet m_resultSet = null;
        private Statement m_statement = null;
        private HashMap m_columnListMap = null;

        SelectResult(ResultSet p_resultSet, Statement p_statement,
            HashMap p_columnListMap)
        {
            m_resultSet = p_resultSet;
            m_statement = p_statement;
            m_columnListMap = p_columnListMap;
        }

        boolean next()
            throws SQLException
        {
            return m_resultSet.next();
        }

        long getLong(String p_columnName)
            throws SQLException
        {
            int ix = ((Integer)m_columnListMap.get(p_columnName)).intValue();
            return m_resultSet.getLong(ix);
        }

        int getInt(String p_columnName)
            throws SQLException
        {
            int ix = ((Integer)m_columnListMap.get(p_columnName)).intValue();
            return m_resultSet.getInt(ix);
        }

        String getString(String p_columnName)
            throws SQLException
        {
            int ix = ((Integer)m_columnListMap.get(p_columnName)).intValue();
            return m_resultSet.getString(ix);
        }

        void close()
        {
            try
            {
                if (m_resultSet != null)
                {
                    m_resultSet.close();
                }

                if (m_statement != null)
                {
                    m_statement.close();
                }
            }
            catch (SQLException ignore)
            { /* ignore */ }
        }
    }


    /**
     * Save leveraged terms into TERM_LEVERAGE_MATCH table.
     *
     * The target locale got matched based on the language part only,
     * so save only that part of the locale.
     */
    static void saveLeveragedTerms(TermLeverageResult p_matches,
        TermLeverageOptions p_options)
        throws GeneralException
    {
        Connection conn = null;
        boolean autoCommit = true;
        PreparedStatementBatch batches = null;

        try
        {
            conn = SqlUtil.hireConnection();
            autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            batches = new PreparedStatementBatch(
                PreparedStatementBatch.DEFAULT_BATCH_SIZE,
                conn, INSERT_SQL, false);

            // Expand the compact in-memory representation of source
            // and target matches here.
            for (Iterator it = p_matches.getRecordIterator(); it.hasNext(); )
            {
                // Write out the priority in increasing order but spread it
                // across all target locales: [en]0,1[fr]2,3[en]4,5[fr]6,7.
                int priority = 0;

                ArrayList records = (ArrayList)it.next();
                for (int ii = 0, maxi = records.size(); ii < maxi; ii++)
                {
                    TermLeverageResult.MatchRecord match =
                        (TermLeverageResult.MatchRecord)records.get(ii);

                    ArrayList targets = match.getSourceTerm().getTargetTerms();
                    for (int jj = 0, maxj = targets.size(); jj < maxj; jj++)
                    {
                        TermLeverageResult.TargetTerm target =
                            (TermLeverageResult.TargetTerm)targets.get(jj);

                        PreparedStatement stmt = batches.getNextPreparedStatement();

                        // Terms are not stored here, only their IDs.
                        // Since TIDs are not stable, when the stored
                        // matches are retrieved they may be out of sync
                        // with the termbase. The retrieval code deals
                        // with this, and the user can always (real soon
                        // now, in 6.3 or 7.0) request dynamic leveraging.

                        stmt.setLong(1, match.getSourceTuvId());
                        stmt.setLong(2, match.getTermbaseId());
                        stmt.setLong(3, match.getMatchedSourceTermId());
                        stmt.setLong(4, target.getMatchedTargetTermId());
                        stmt.setString(5, Util.fixLocale(target.getLocale()));
                        stmt.setInt(6, priority++);
                        stmt.setInt(7, match.getScore());

                        stmt.addBatch();
                    }
                }
            }

            batches.executeBatches();

            conn.commit();
        }
        catch (Exception e)
        {
            try
            {
                if (conn != null)
                {
                    conn.rollback();
                }
            }
            catch (Throwable ignore) {}

            CATEGORY.error("Insertion to TERM_LEVERAGE_MATCH failed", e);
            throw new GeneralException(e);
        }
        finally
        {
            if (batches != null)
            {
                batches.closeAll();
                batches = null;
            }

            try
            {
                // restore auto commit mode
                if (conn != null)
                {
                    conn.setAutoCommit(autoCommit);
                }
            }
            catch (Throwable ignore) {}

            // return the connection
            SqlUtil.fireConnection(conn);
        }
    }


    /**
     * Retrieve term leverage matches for a given TUV and target locale.
     *
     * Since termbases store data for languages only, only retrieve it
     * based on the language part of the locale.
     *
     * @param p_sourceTuvId source TUV id for which the leveraged
     * terms are returned
     * @param p_targetPageLocale target page locale
     * @param p_connection -- a DB connection to use. It is not
     * returned to the connection pool or closed.  That is the
     * responsibility of the caller.
     * @throws GeneralException when an error occurs in database.
     */
    static SelectResult getTermMatchesForSegment(long p_sourceTuvId,
        String p_targetPageLocale, Connection p_connection)
        throws GeneralException
    {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            stmt = p_connection.prepareStatement(SELECT_TUV_MATCH_SQL);
            stmt.setLong(1, p_sourceTuvId);
            stmt.setString(2, Util.fixLocale(p_targetPageLocale));
            rs = stmt.executeQuery();
        }
        catch (Exception e)
        {
            try
            {
                if (rs != null)   { rs.close(); }
                if (stmt != null) { stmt.close(); }
            }
            catch (Throwable ignore) {}

            CATEGORY.error("Term leverage retrieval failed!", e);
            throw new GeneralException(e);
        }

        return new SelectResult(rs, stmt, SELECT_TUV_MATCH_COLUMN_MAP);
    }


    /**
     * Retrieve term leverage matches for a given page and target locale.
     *
     * @param p_sourcePageId source page id
     * @param p_sourcePageLocale source page locale
     * @param p_targetPageLocale target page locale
     * @param p_connection -- a DB connection to use. It is not
     * returned to the connection pool or closed.
     * @throws GeneralException when an error occurs in database.
     */
    static SelectResult getTermMatchesForPage(long p_sourcePageId,
        long p_sourcePageLocaleId, String p_targetPageLocale,
        Connection p_conn)
        throws GeneralException
    {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            String tuTableName = BigTableUtil
                    .getTuTableJobDataInBySourcePageId(p_sourcePageId);
            String tuvTableName = BigTableUtil
                    .getTuvTableJobDataInBySourcePageId(p_sourcePageId);
            String sql = SELECT_PAGE_MATCH_SQL.replace(
                    TuvQueryConstants.TU_TABLE_PLACEHOLDER, tuTableName)
                    .replace(TuvQueryConstants.TUV_TABLE_PLACEHOLDER,
                            tuvTableName);
            stmt = p_conn.prepareStatement(sql);
            stmt.setLong(1, p_sourcePageId);
            stmt.setLong(2, p_sourcePageLocaleId);
            stmt.setString(3, Util.fixLocale(p_targetPageLocale));
            rs = stmt.executeQuery();
        }
        catch (Exception e)
        {
            try
            {
                if (rs != null)   { rs.close(); }
                if (stmt != null) { stmt.close(); }
            }
            catch (Throwable ignore) {}

            CATEGORY.error("Term leverage retrieval failed!", e);
            throw new GeneralException(e);
        }

        return new SelectResult(rs, stmt, SELECT_PAGE_MATCH_COLUMN_MAP);
    }
}

