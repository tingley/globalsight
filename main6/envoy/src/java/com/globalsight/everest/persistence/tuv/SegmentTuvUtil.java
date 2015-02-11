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

package com.globalsight.everest.persistence.tuv;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.globalsight.everest.comment.IssueEditionRelation;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.everest.tuv.TuvState;
import com.globalsight.ling.docproc.extractor.xliff.XliffAlt;
import com.globalsight.ling.tm2.TmUtil;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.ling.util.GlobalSightCrc;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.edit.GxmlUtil;

/**
 * Helper for "translation_unit_variant_[companyId]" storage.
 * 
 * @author york.jin
 * @since 2012-03-22
 * @version 8.2.3
 */
public class SegmentTuvUtil extends SegmentTuTuvCacheManager implements
        TuvQueryConstants
{
    static private final Logger logger = Logger.getLogger(SegmentTuvUtil.class);

    private static final String SELECT_COLUMNS = "SELECT "
            + "tuv.id, tuv.order_num, tuv.locale_id, tuv.tu_id, tuv.is_indexed, "
            + "tuv.segment_clob, tuv.segment_string, tuv.word_count, tuv.exact_match_key, tuv.state, "
            + "tuv.merge_state, tuv.timestamp, tuv.last_modified, tuv.modify_user, tuv.creation_date, "
            + "tuv.creation_user, tuv.updated_by_project, tuv.sid, tuv.src_comment FROM ";

    private static final String GET_TUV_BY_TUV_ID_SQL = SELECT_COLUMNS
            + TUV_TABLE_PLACEHOLDER + " tuv " + "WHERE tuv.id = ? ";

    private static final String GET_TUVS_BY_TU_ID_SQL = SELECT_COLUMNS
            + TUV_TABLE_PLACEHOLDER + " tuv "
            + "WHERE tuv.state != 'OUT_OF_DATE' " + "AND tuv.tu_id = ? ";

    private static final String GET_TUV_BY_TU_ID_LOCALE_ID_SQL = SELECT_COLUMNS
            + TUV_TABLE_PLACEHOLDER + " tuv "
            + "WHERE tuv.state != 'OUT_OF_DATE' " + "AND tuv.locale_id = ? "
            + "AND tuv.tu_id = ? ";

    private static final String GET_SOURCE_TUVS_SQL = SELECT_COLUMNS
            + TUV_TABLE_PLACEHOLDER + " tuv, " + TU_TABLE_PLACEHOLDER + " tu, "
            + "source_page_leverage_group splg " + "WHERE tuv.tu_id = tu.id "
            + "AND tu.leverage_group_id = splg.lg_id "
            + "AND tuv.locale_id = ? " + "AND splg.sp_id = ? "
            + "ORDER BY tuv.order_num asc";

    // This SQL gets the same result as GET_TARGET_TUVS_SQL
    private static final String GET_EXPORT_TUVS_SQL = SELECT_COLUMNS
            + TUV_TABLE_PLACEHOLDER + " tuv, " + TU_TABLE_PLACEHOLDER + " tu, "
            + "source_page_leverage_group splg " + "WHERE tuv.tu_id = tu.id "
            + "AND tu.leverage_group_id = splg.lg_id "
            + "AND tuv.state != 'OUT_OF_DATE' " + "AND tuv.locale_id = ? "
            + "AND splg.sp_id = ? " + "ORDER BY tuv.order_num asc";

    private static final String GET_TARGET_TUVS_SQL = SELECT_COLUMNS
            + TUV_TABLE_PLACEHOLDER + " tuv, " + TU_TABLE_PLACEHOLDER + " tu, "
            + "target_page_leverage_group tplg " + "WHERE tuv.tu_id = tu.id "
            + "AND tu.leverage_group_id = tplg.lg_id "
            + "AND tuv.state != 'OUT_OF_DATE' " + "AND tuv.locale_id = ? "
            + "AND tplg.tp_id = ? " + "ORDER BY tuv.order_num asc ";

    private static final String LOAD_XLIFF_ALT_BY_SPID_LOCALE_SQL = "SELECT alt.* FROM xliff_alt alt, "
            + TUV_TABLE_PLACEHOLDER
            + " tuv, "
            + TU_TABLE_PLACEHOLDER
            + " tu, "
            + "source_page_leverage_group splg "
            + "WHERE alt.tuv_id = tuv.id "
            + "AND tuv.tu_id = tu.id "
            + "AND tu.leverage_group_id = splg.lg_id "
            + "AND tuv.locale_id = ? "
            + "AND splg.sp_id = ? "
            + "ORDER BY alt.id, alt.tuv_id ";

    private static final String LOAD_XLIFF_ALT_BY_TPID_LOCALE_SQL = "SELECT alt.* FROM xliff_alt alt, "
            + TUV_TABLE_PLACEHOLDER
            + " tuv, "
            + TU_TABLE_PLACEHOLDER
            + " tu, "
            + "target_page_leverage_group tplg "
            + "WHERE alt.tuv_id = tuv.id "
            + "AND tuv.tu_id = tu.id "
            + "AND tu.leverage_group_id = tplg.lg_id "
            + "AND tuv.locale_id = ? "
            + "AND tplg.tp_id = ? "
            + "ORDER BY alt.id, alt.tuv_id ";

    private static final String SAVE_TUVS_SQL = "INSERT INTO "
            + TUV_TABLE_PLACEHOLDER
            + " ("
            + "id, order_num, locale_id, tu_id, is_indexed, "
            + "segment_clob, segment_string, word_count, exact_match_key, state, "
            + "merge_state, timestamp, last_modified, modify_user, creation_date, "
            + "creation_user, updated_by_project, sid, src_comment) "
            + "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    /**
     * Save TUVs into DB.
     * 
     * @param conn
     * @param p_tuvs
     *            -- TuvImpl objects
     * @param companyId
     * @throws Exception
     */
    public static void saveTuvs(Connection conn, Collection<Tuv> p_tuvs,
            String companyId) throws Exception
    {
        PreparedStatement ps = null;

        try
        {
            SegmentTuTuvIndexUtil.updateTuvSequence(conn);

            String sql = SAVE_TUVS_SQL.replace(TUV_TABLE_PLACEHOLDER,
                    getTuvTableName(companyId));
            ps = conn.prepareStatement(sql);

            Set<Long> tuIds = new HashSet<Long>();
            for (Iterator it = p_tuvs.iterator(); it.hasNext();)
            {
                TuvImpl tuv = (TuvImpl) it.next();
                tuIds.add(tuv.getTuId());

                ps.setLong(1, tuv.getId());
                ps.setLong(2, tuv.getOrder());
                ps.setLong(3, tuv.getLocaleId());
                ps.setLong(4, tuv.getTu(companyId).getId());
                ps.setString(5, tuv.getIsIndexed() ? "Y" : "N");

                ps.setString(6, tuv.getSegmentClob());
                ps.setString(7, tuv.getSegmentString());
                ps.setInt(8, tuv.getWordCount());
                ps.setLong(9, tuv.getExactMatchKey());
                ps.setString(10, tuv.getState().getName());

                ps.setString(11, tuv.getMergeState());
                ps.setDate(12, new java.sql.Date(tuv.getTimestamp().getTime()));
                ps.setDate(13, new java.sql.Date(tuv.getLastModified()
                        .getTime()));
                ps.setString(14, tuv.getLastModifiedUser());
                ps.setDate(15,
                        new java.sql.Date(tuv.getCreatedDate().getTime()));

                ps.setString(16, tuv.getCreatedUser());
                ps.setString(17, tuv.getUpdatedProject());
                ps.setString(18, tuv.getSid());
                ps.setString(19, tuv.getSrcComment());

                ps.addBatch();
            }

            ps.executeBatch();

            // Cache the TUVs for large pages when create jobs.
            if (tuIds.size() > 800)
            {
                for (Iterator it = p_tuvs.iterator(); it.hasNext();)
                {
                    TuvImpl tuv = (TuvImpl) it.next();
                    setTuvIntoCache(tuv);
                }
            }
        }
        catch (Exception e)
        {
            logger.error("Error when save TUVs " + e.getMessage(), e);
            for (Iterator it = p_tuvs.iterator(); it.hasNext();)
            {
                TuvImpl tuv = (TuvImpl) it.next();
                removeTuvFromCache(tuv.getId());
            }
            throw e;
        }
        finally
        {
            DbUtil.silentClose(ps);
        }
    }

    /**
     * Query TUV by TuvId.
     * 
     * @param p_tuvId
     * @return TuvImpl
     * @throws Exception
     */
    public static TuvImpl getTuvById(long p_tuvId, String companyId)
            throws Exception
    {
        TuvImpl tuv = getTuvFromCache(p_tuvId);
        if (tuv != null)
        {
            return tuv;
        }

        Session session = TmUtil.getStableSession();
        try
        {
            tuv = getTuvById(session.connection(), p_tuvId, companyId);
        }
        catch (Exception e)
        {
            throw e;
        }
        finally
        {
            TmUtil.closeStableSession(session);
        }

        return tuv;
    }

    /**
     * Query TUV by TuvId.
     * 
     * @param connection
     * @param tuvId
     * @return TuvImpl
     * @throws SQLException
     */
    public static TuvImpl getTuvById(Connection connection, long p_tuvId,
            String companyId) throws Exception
    {
        TuvImpl tuv = getTuvFromCache(p_tuvId);
        if (tuv != null)
        {
            return tuv;
        }

        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            String sql = GET_TUV_BY_TUV_ID_SQL.replace(TUV_TABLE_PLACEHOLDER,
                    getTuvTableName(companyId));

            ps = connection.prepareStatement(sql);
            ps.setLong(1, p_tuvId);
            rs = ps.executeQuery();

            List<TuvImpl> tuvs = convertResultSetToTuv(rs, true);
            if (tuvs != null && tuvs.size() > 0)
            {
                tuv = tuvs.get(0);
            }
        }
        catch (Exception e)
        {
            logger.error("Error when get TUV by tuvID " + p_tuvId, e);
            throw e;
        }
        finally
        {
            releaseRsPsConnection(rs, ps, null);
        }

        return tuv;
    }

    /**
     * Query TUVs by specified tuIds and localeId.
     * 
     * @param p_tuIds
     * @param p_localeId
     * @return TuvImpl list
     * @throws Exception
     */
    public static List<TuvImpl> getTuvsByTuIdsLocaleId(long[] p_tuIds,
            long p_localeId, String companyId) throws Exception
    {
        List<TuvImpl> result = new ArrayList<TuvImpl>();

        Session session = TmUtil.getStableSession();
        try
        {
            for (int i = 0; i < p_tuIds.length; i++)
            {
                result.add(getTuvByTuIdLocaleId(session.connection(),
                        p_tuIds[i], p_localeId, companyId));
            }
        }
        catch (Exception e)
        {
            throw e;
        }
        finally
        {
            TmUtil.closeStableSession(session);
        }

        return result;
    }

    /**
     * Query TUV by tuId and localeId.
     * 
     * @param p_tuId
     * @param p_localeId
     * @return TuvImpl
     * @throws Exception
     */
    public static TuvImpl getTuvByTuIdLocaleId(long p_tuId, long p_localeId,
            String companyId) throws Exception
    {
        TuvImpl tuv = null;

        Session session = TmUtil.getStableSession();
        try
        {
            tuv = getTuvByTuIdLocaleId(session.connection(), p_tuId,
                    p_localeId, companyId);
        }
        catch (Exception e)
        {
            throw e;
        }
        finally
        {
            TmUtil.closeStableSession(session);
        }

        return tuv;
    }

    public static TuvImpl getTuvByTuIdLocaleId(long p_tuId, long p_localeId,
            long companyId) throws Exception
    {
        TuvImpl tuv = null;

        Session session = TmUtil.getStableSession();
        try
        {
            tuv = getTuvByTuIdLocaleId(session.connection(), p_tuId,
                    p_localeId, companyId);
        }
        catch (Exception e)
        {
            throw e;
        }
        finally
        {
            TmUtil.closeStableSession(session);
        }

        return tuv;
    }

    /**
     * Query TUV by tuId and localeId.
     * 
     * @param p_connection
     * @param p_tuId
     * @param p_localeId
     * @return TuvImpl
     * @throws Exception
     */
    public static TuvImpl getTuvByTuIdLocaleId(Connection p_connection,
            long p_tuId, long p_localeId, String companyId) throws Exception
    {
        return getTuvByTuIdLocaleId(p_connection, p_tuId, p_localeId,
                Long.parseLong(companyId));
    }

    public static TuvImpl getTuvByTuIdLocaleId(Connection p_connection,
            long p_tuId, long p_localeId, long companyId) throws Exception
    {
        TuvImpl tuv = null;

        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            String sql = GET_TUV_BY_TU_ID_LOCALE_ID_SQL.replace(
                    TUV_TABLE_PLACEHOLDER, getTuvTableName(companyId));

            ps = p_connection.prepareStatement(sql);
            ps.setLong(1, p_localeId);
            ps.setLong(2, p_tuId);
            rs = ps.executeQuery();

            List<TuvImpl> result = convertResultSetToTuv(rs, true);
            if (result != null && result.size() > 0)
            {
                tuv = result.get(0);
            }
        }
        catch (Exception e)
        {
            logger.error("Error when get TUV by TuId " + p_tuId
                    + " and localeId " + p_localeId, e);
            throw e;
        }
        finally
        {
            releaseRsPsConnection(rs, ps, null);
        }

        return tuv;
    }

    /**
     * Query translation unit variant by TuId.
     * 
     * @param tuId
     * @return TUV list for specified TU_ID.
     * @throws Exception
     */
    public static List<TuvImpl> getTuvsByTuId(long p_tuId, String companyId)
            throws Exception
    {
        List<TuvImpl> result = new ArrayList<TuvImpl>();

        Session session = TmUtil.getStableSession();
        try
        {
            result = getTuvsByTuId(session.connection(), p_tuId, companyId);
        }
        finally
        {
            TmUtil.closeStableSession(session);
        }

        return result;
    }

    /**
     * Query translation unit variant by TuId.
     * 
     * @param connection
     * @param tuId
     * @return TUV list for specified TU_ID.
     * @throws Exception
     */
    public static List<TuvImpl> getTuvsByTuId(Connection connection,
            long p_tuId, String companyId) throws Exception
    {
        List<TuvImpl> result = new ArrayList<TuvImpl>();

        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            String sql = GET_TUVS_BY_TU_ID_SQL.replace(TUV_TABLE_PLACEHOLDER,
                    getTuvTableName(companyId));

            ps = connection.prepareStatement(sql);
            ps.setLong(1, p_tuId);
            rs = ps.executeQuery();

            result = convertResultSetToTuv(rs, true);
        }
        catch (Exception e)
        {
            logger.error("Error when get TUVs by tuID " + p_tuId, e);
            throw e;
        }
        finally
        {
            releaseRsPsConnection(rs, ps, null);
        }

        return result;
    }

    /**
     * Query source TUVs in current source page. Note:In this method, need load
     * extra data for TUVs such as xliff_alt etc.
     * 
     * @param p_sourcePage
     *            -- SourcePage
     * @return ArrayList<Tuv>
     * @throws Exception
     */
    public static ArrayList<Tuv> getSourceTuvs(SourcePage p_sourcePage)
            throws Exception
    {
        boolean loadExtraInfo = true;
        return getSourceTuvs(p_sourcePage, loadExtraInfo);
    }

    /**
     * Query source TUVs in current source page.
     * 
     * @param p_sourcePage
     *            -- SourcePage
     * @param p_loadXlfAlts
     *            -- if this is true,maybe reduce performance.
     * @return ArrayList<Tuv>
     * @throws Exception
     */
    public static ArrayList<Tuv> getSourceTuvs(SourcePage p_sourcePage,
            boolean p_needLoadXlfAlts) throws Exception
    {
        ArrayList<Tuv> result = new ArrayList<Tuv>();

        Session session = TmUtil.getStableSession();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            String companyId = p_sourcePage.getCompanyId();
            String sql = GET_SOURCE_TUVS_SQL.replace(TUV_TABLE_PLACEHOLDER,
                    getTuvTableName(companyId));
            sql = sql.replace(TU_TABLE_PLACEHOLDER, getTuTableName(companyId));

            ps = session.connection().prepareStatement(sql);

            ps.setLong(1, p_sourcePage.getLocaleId());
            ps.setLong(2, p_sourcePage.getId());
            rs = ps.executeQuery();

            result.addAll(convertResultSetToTuv(rs, false));

            // Load xliff_alt data in page level to improve performance.
            if (p_needLoadXlfAlts)
            {
                loadXliffAlts(companyId, result, p_sourcePage.getLocaleId(),
                        p_sourcePage.getId());
            }
        }
        catch (Exception e)
        {
            logger.error("Error when getSourceTuvs() for source page "
                    + p_sourcePage.getId(), e);
            throw e;
        }
        finally
        {
            TmUtil.closeStableSession(session);
        }

        return result;
    }

    /**
     * Query Tuvs for specified source page and loale. The locale can be source
     * locale or any target locale.
     * 
     * @param p_companyId
     * @param p_localeId
     *            -- targetLocaleId
     * @param p_sourcePageId
     * @return TuvImpl list
     * @throws Exception
     */
    public static List<Tuv> getExportTuvs(long p_companyId, long p_localeId,
            long p_sourcePageId) throws Exception
    {
        List<Tuv> result = new ArrayList<Tuv>();

        Session session = TmUtil.getStableSession();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try
        {
            String sql = GET_EXPORT_TUVS_SQL.replace(TUV_TABLE_PLACEHOLDER,
                    getTuvTableName(p_companyId)).replace(TU_TABLE_PLACEHOLDER,
                    getTuTableName(p_companyId));

            ps = session.connection().prepareStatement(sql);
            ps.setLong(1, p_localeId);
            ps.setLong(2, p_sourcePageId);
            rs = ps.executeQuery();

            result.addAll(convertResultSetToTuv(rs, false));

            // Load xliff_alt data in page level to improve performance.
            loadXliffAlts(String.valueOf(p_companyId), result, p_localeId,
                    p_sourcePageId);
        }
        catch (Exception e)
        {
            logger.error("Error when getExportTuvs for sourcePageId "
                    + p_sourcePageId + " and localeId " + p_localeId, e);
            throw e;
        }
        finally
        {
            TmUtil.closeStableSession(session);
        }

        return result;
    }

    /**
     * Query target TUVs in current target page.
     * 
     * @param p_companyId
     * @param p_targetPageId
     * @param p_targetLocaleId
     * @return Tuv list
     * @throws Exception
     */
    public static List<TuvImpl> getTargetTuvs(TargetPage p_targetPage)
            throws Exception
    {
        List<TuvImpl> result = new ArrayList<TuvImpl>();

        Session session = TmUtil.getStableSession();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            long companyId = Long.parseLong(p_targetPage.getSourcePage()
                    .getCompanyId());
            String sql = GET_TARGET_TUVS_SQL.replace(TUV_TABLE_PLACEHOLDER,
                    getTuvTableName(companyId)).replace(TU_TABLE_PLACEHOLDER,
                    getTuTableName(companyId));

            ps = session.connection().prepareStatement(sql);
            ps.setLong(1, p_targetPage.getLocaleId());
            ps.setLong(2, p_targetPage.getId());
            rs = ps.executeQuery();

            result = convertResultSetToTuv(rs, false);

            // Load xliff_alt data in page level to improve performance.
            loadXliffAlts2(String.valueOf(companyId),
                    new ArrayList<Tuv>(result), p_targetPage.getLocaleId(),
                    p_targetPage.getId());
        }
        catch (Exception e)
        {
            logger.error("Error when getTargetTuvs for target page "
                    + p_targetPage.getId(), e);
            throw e;
        }
        finally
        {
            TmUtil.closeStableSession(session);
        }

        return result;
    }

    /**
     * Load XliffAlt for TUVs of current source page.
     * 
     * @param p_companyId
     * @param p_tuvs
     *            -- source or target Tuvs.
     * @param p_localeId
     *            -- source or target page localeId.
     * @param p_pageId
     *            -- source page ID
     */
    private static void loadXliffAlts(String p_companyId, List<Tuv> p_tuvs,
            long p_localeId, long p_sourcePageId)
    {
        try
        {
            String sql = LOAD_XLIFF_ALT_BY_SPID_LOCALE_SQL.replace(
                    TUV_TABLE_PLACEHOLDER, getTuvTableName(p_companyId));
            sql = sql
                    .replace(TU_TABLE_PLACEHOLDER, getTuTableName(p_companyId));

            List<XliffAlt> xlfAlts = HibernateUtil.searchWithSql(
                    XliffAlt.class, sql, p_localeId, p_sourcePageId);

            Map<Long, Set<XliffAlt>> xlfAltMap = getXliffAltMap(xlfAlts);

            if (xlfAltMap != null && xlfAltMap.size() > 0)
            {
                putXliffAltIntoTuv(xlfAltMap, p_tuvs);
            }

            for (Tuv tuv : p_tuvs)
            {
                recordWhichTuvExtraDataAlreadyLoaded(tuv.getIdAsLong(),
                        XLIFF_ALT);
            }
        }
        catch (Exception e)
        {
            logger.error("Error when loadXliffAlts() for sourcePageId "
                    + p_sourcePageId + " localeId " + p_localeId, e);
        }
    }

    /**
     * Invoked by getTargetTuvs(...) method.
     * 
     * @param companyId
     * @param p_tuvs
     * @param p_localeId
     * @param p_pageId
     */
    private static void loadXliffAlts2(String companyId, ArrayList<Tuv> p_tuvs,
            long p_localeId, long p_pageId)
    {
        try
        {
            String sql = LOAD_XLIFF_ALT_BY_TPID_LOCALE_SQL.replace(
                    TUV_TABLE_PLACEHOLDER, getTuvTableName(companyId));
            sql = sql.replace(TU_TABLE_PLACEHOLDER, getTuTableName(companyId));

            List<XliffAlt> xlfAlts = HibernateUtil.searchWithSql(
                    XliffAlt.class, sql, p_localeId, p_pageId);

            Map<Long, Set<XliffAlt>> xlfAltMap = getXliffAltMap(xlfAlts);

            if (xlfAltMap != null && xlfAltMap.size() > 0)
            {
                putXliffAltIntoTuv(xlfAltMap, p_tuvs);
            }

            for (Tuv tuv : p_tuvs)
            {
                recordWhichTuvExtraDataAlreadyLoaded(tuv.getIdAsLong(),
                        XLIFF_ALT);
            }
        }
        catch (Exception e)
        {
            logger.error("Error when loadXliffAlts2() for pageId " + p_pageId
                    + " localeId " + p_localeId, e);
        }
    }

    /**
     * Convert ResultSet into XliffAlt objects, and put them into map.
     * 
     * @param alt
     *            -- List<XliffAlt>
     * @return -- Map<Long, Set<XliffAlt>> (tuvId : Set<XliffAlt>)
     */
    private static Map<Long, Set<XliffAlt>> getXliffAltMap(List<XliffAlt> alts)
    {
        // tuvId : Set<XliffAlt>
        Map<Long, Set<XliffAlt>> xlfAltMap = new HashMap<Long, Set<XliffAlt>>();

        for (XliffAlt alt : alts)
        {
            Set<XliffAlt> myAlts = xlfAltMap.get(alt.getTuvId());
            if (myAlts == null)
            {
                myAlts = new HashSet<XliffAlt>();
                myAlts.add(alt);
                xlfAltMap.put(alt.getTuvId(), myAlts);
            }
            else
            {
                myAlts.add(alt);
                xlfAltMap.put(alt.getTuvId(), myAlts);
            }
        }

        return xlfAltMap;
    }

    /**
     * Put XliffAlts into its own TUV.
     * 
     * @param p_xlfAltMap
     * @param p_tuvs
     */
    private static void putXliffAltIntoTuv(
            Map<Long, Set<XliffAlt>> p_xlfAltMap, List<Tuv> p_tuvs)
    {
        if (p_xlfAltMap != null && p_xlfAltMap.size() > 0)
        {
            for (Tuv tuv : p_tuvs)
            {
                Set<XliffAlt> myAlts = p_xlfAltMap.get(tuv.getIdAsLong());
                if (myAlts != null && myAlts.size() > 0)
                {
                    tuv.setXliffAlt(myAlts);
                }
            }
        }
    }

    /**
     * Update TuvImpl
     * 
     * @param p_tuv
     *            TuvImpl
     * @throws Exception
     */
    public static void updateTuv(TuvImpl p_tuv, String companyId)
            throws Exception
    {
    	List<TuvImpl> tuvs = new ArrayList<TuvImpl>();
    	tuvs.add(p_tuv);
    	updateTuvs(tuvs, Long.parseLong(companyId));
    }

    public static void updateTuvs(List<TuvImpl> p_tuvs, long companyId)
            throws Exception
    {
        Session session = TmUtil.getStableSession();

        try
        {
            StringBuilder sql = new StringBuilder();

            sql.append("update ").append(getTuvTableName(companyId))
                    .append(" set ");
            sql.append("order_num = ?, ");// 1
            sql.append("locale_id = ?, ");// 2
            sql.append("tu_id = ?, ");// 3
            sql.append("is_indexed = ?, ");// 4
            sql.append("segment_clob = ?, ");// 5
            sql.append("segment_string = ?,");// 6
            sql.append("word_count = ?,");// 7
            sql.append("exact_match_key = ?,");// 8
            sql.append("state = ?, ");// 9
            sql.append("merge_state = ?, ");// 10
            sql.append("timestamp = ?, ");// 11
            sql.append("last_modified = ?, ");// 12
            sql.append("modify_user = ?, ");// 13
            sql.append("creation_date = ?, ");// 14
            sql.append("creation_user = ?, ");// 15
            sql.append("updated_by_project = ?, ");// 16
            sql.append("sid = ?, ");// 17
            sql.append("src_comment = ? where id = ?");// 18 19

            PreparedStatement tuvUpdateStmt = session.connection()
                    .prepareStatement(sql.toString());

            // addBatch counters
            int batchUpdate = 0;

            for (TuvImpl tuv : p_tuvs)
            {
                tuv.setExactMatchKey(GlobalSightCrc.calculate(tuv
                        .getExactMatchFormat()));
                tuv.setState(TuvState.LOCALIZED);
                tuv.setLastModified(new Date());

                tuvUpdateStmt.setLong(1, tuv.getOrder());
                tuvUpdateStmt.setLong(2, tuv.getLocaleId());
                tuvUpdateStmt.setLong(3, tuv.getTuId());
                tuvUpdateStmt.setString(4, tuv.getIsIndexed() ? "Y" : "N");
                tuvUpdateStmt.setString(5, tuv.getSegmentClob());
                tuvUpdateStmt.setString(6, tuv.getSegmentString());
                tuvUpdateStmt.setInt(7, tuv.getWordCount());
                tuvUpdateStmt.setLong(8, tuv.getExactMatchKey());
                tuvUpdateStmt.setString(9, tuv.getState().getName());
                tuvUpdateStmt.setString(10, tuv.getMergeState());
                tuvUpdateStmt.setTimestamp(11, tuv.getTimestamp());
                tuvUpdateStmt.setTimestamp(12, new Timestamp(tuv
                        .getLastModified().getTime()));
                tuvUpdateStmt.setString(13, tuv.getLastModifiedUser());
                tuvUpdateStmt.setTimestamp(14, new Timestamp(tuv
                        .getCreatedDate().getTime()));
                tuvUpdateStmt.setString(15, tuv.getCreatedUser());
                tuvUpdateStmt.setString(16, tuv.getUpdatedProject());
                tuvUpdateStmt.setString(17, tuv.getSid());
                tuvUpdateStmt.setString(18, tuv.getSrcComment());
                tuvUpdateStmt.setLong(19, tuv.getId());

                tuvUpdateStmt.addBatch();

                batchUpdate++;

                if (batchUpdate > DbUtil.BATCH_INSERT_UNIT)
                {
                    tuvUpdateStmt.executeBatch();
                    batchUpdate = 0;
                }
            }

            // execute the rest of the added batch
            if (batchUpdate > 0)
            {
                tuvUpdateStmt.executeBatch();
            }

            tuvUpdateStmt.close();

        }
        catch (Exception e)
        {
            throw e;
        }
        finally
        {
            TmUtil.closeStableSession(session);
        }
    }

    /**
     * Update TuvImpl
     * 
     * @param p_connection
     * @param p_tuv
     *            TuvImpl
     * @throws Exception
     */
    public static void updateTuv(Connection p_connection, TuvImpl p_tuv,
            String companyId) throws Exception
    {
        if (p_tuv == null)
            return;

        StringBuilder sql = new StringBuilder();
        TuvImpl changedTuv = p_tuv;

        sql.append("update ").append(getTuvTableName(companyId))
                .append(" set ");
        if (changedTuv.getOrder() > 0)
        {
            sql.append("order_num = ").append(changedTuv.getOrder())
                    .append(", ");
        }
        if (changedTuv.getLocaleId() > 0)
        {
            sql.append("locale_id = ").append(changedTuv.getLocaleId())
                    .append(", ");
        }
        if (changedTuv.getTuId() > 0)
        {
            sql.append("tu_id = ").append(changedTuv.getTuId()).append(", ");
        }
        sql.append("is_indexed = ")
                .append(changedTuv.getIsIndexed() ? "'Y'" : "'N'").append(", ");
        
        int index = 1;
        int clobIndex = -1;
        int segmentIndex = -1;
        
        if (isValidString(changedTuv.getSegmentClob()))
        {
        	clobIndex = index;
        	index ++;
            sql.append("segment_clob = ?, ");
        }
        
        if (isValidString(changedTuv.getSegmentString()))
        {
        	segmentIndex = index;
        	index++;
            sql.append("segment_string = ?, ");
        }
        if (changedTuv.getWordCount() >= 0)
        {
            sql.append("word_count = ").append(changedTuv.getWordCount())
                    .append(", ");
        }
        if (changedTuv.getExactMatchKey() > 0)
        {
            sql.append("exact_match_key = ")
                    .append(changedTuv.getExactMatchKey()).append(", ");
        }
        if (isValidString(changedTuv.getState().getName()))
        {
            sql.append("state = '").append(changedTuv.getState().getName())
                    .append("', ");
        }
        if (isValidString(changedTuv.getMergeState()))
        {
            sql.append("merge_state = '").append(changedTuv.getMergeState())
                    .append("', ");
        }
        if (changedTuv.getTimestamp() != null)
        {
            sql.append("timestamp = '").append(changedTuv.getTimestamp())
                    .append("', ");
        }
        if (changedTuv.getLastModified() != null)
        {
            sql.append("last_modified = '")
                    .append(new Timestamp(changedTuv.getLastModified()
                            .getTime())).append("', ");
        }
        if (isValidString(changedTuv.getLastModifiedUser()))
        {
            sql.append("modify_user = '")
                    .append(changedTuv.getLastModifiedUser()).append("', ");
        }
        if (changedTuv.getCreatedDate() != null)
        {
            sql.append("creation_date = '")
                    .append(new Timestamp(changedTuv.getCreatedDate().getTime()))
                    .append("', ");
        }
        if (isValidString(changedTuv.getCreatedUser()))
        {
            sql.append("creation_user = '").append(changedTuv.getCreatedUser())
                    .append("', ");
        }
        if (isValidString(changedTuv.getUpdatedProject()))
        {
            sql.append("updated_by_project = '")
                    .append(changedTuv.getUpdatedProject()).append("', ");
        }
        if (isValidString(changedTuv.getSid()))
        {
            sql.append("sid = '").append(changedTuv.getSid()).append("', ");
        }
        if (isValidString(changedTuv.getSrcComment()))
        {
            sql.append("src_comment = '").append(changedTuv.getSrcComment())
                    .append("', ");
        }

        String finalSql = null;
        boolean isAnythingChanged = false;
        if (sql.toString().trim().endsWith(","))
        {
            isAnythingChanged = true;
            finalSql = sql.toString();
            finalSql = finalSql.substring(0, finalSql.lastIndexOf(","));
            finalSql = finalSql + " where id = " + p_tuv.getId();
        }

        if (isAnythingChanged)
        {
            PreparedStatement ps = null;
            try
            {
                ps = p_connection.prepareStatement(finalSql);
                
                if (clobIndex > 0)
                {
                	ps.setString(clobIndex, changedTuv.getSegmentClob());
                }
                
                if (segmentIndex > 0)
                {
                	ps.setString(segmentIndex, changedTuv.getSegmentString());
                }
                
                ps.setString(1, changedTuv.getSegmentString());
                ps.executeUpdate();
                setTuvIntoCache(p_tuv);
            }
            catch (Exception e)
            {
                logger.error("Error when update Tuv " + p_tuv.getId(), e);
                throw e;
            }
            finally
            {
                releaseRsPsConnection(null, ps, null);
            }
        }
    }

    /**
     * Replace the single quote "'" to "\\" to avoid sql syntax error.
     */
    private static String revertApos(String gxml)
    {
        if (gxml == null || gxml.trim().length() == 0)
        {
            return gxml;
        }

        String innerText = GxmlUtil.stripRootTag(gxml);
        innerText = innerText.replace("'", "\\'");
        return GxmlUtil.resetInnerText(gxml, innerText);
    }

    /**
     * Convert the ResultSet objects into TuvImpl objects.
     * 
     * @param rs
     *            -- ResultSet which concludes TuvImpl data.
     * @param loadExtraInfo
     *            -- Note that if true, there may be performance problem.
     * @return
     * @throws Exception
     */
    private static ArrayList<TuvImpl> convertResultSetToTuv(ResultSet rs,
            boolean loadExtraInfo) throws Exception
    {
        ArrayList<TuvImpl> result = new ArrayList<TuvImpl>();
        if (rs == null)
        {
            return result;
        }

        while (rs.next())
        {
            TuvImpl tuv = new TuvImpl();

            Long tuvId = new Long(rs.getLong(1));
            if (getTuvFromCache(tuvId) != null)
            {
                result.add(getTuvFromCache(tuvId));
                continue;
            }

            tuv.setId(rs.getLong(1));
            tuv.setOrder(rs.getLong(2));
            tuv.setLocaleId(rs.getLong(3));
            tuv.setTuId(rs.getLong(4));
            tuv.setIsIndexed("Y".equalsIgnoreCase(rs.getString(5)) ? true
                    : false);

            tuv.setSegmentClob(rs.getString(6));
            tuv.setSegmentString(rs.getString(7));
            tuv.setWordCount(rs.getInt(8));
            tuv.setExactMatchKey(rs.getLong(9));
            tuv.setState(rs.getString(10));

            tuv.setMergeState(rs.getString(11));
            tuv.setTimestamp(rs.getTimestamp(12));
            tuv.setLastModified(new java.util.Date(rs.getDate(13).getTime()));
            tuv.setLastModifiedUser(rs.getString(14));
            tuv.setCreatedDate(new java.util.Date(rs.getDate(15).getTime()));

            tuv.setCreatedUser(rs.getString(16));
            tuv.setUpdatedProject(rs.getString(17));
            tuv.setSid(rs.getString(18));
            tuv.setSrcComment(rs.getString(19));

            if (loadExtraInfo)
            {
                // Load XliffAlt data if current TUV has.
                // loadXliffAlt(tuv);
                // Load IssueEditionRelation for GS edition
                // loadIssueEditionRelation(tuv);
            }

            result.add(tuv);
            setTuvIntoCache(tuv);
        }

        return result;
    }

    /**
     * Load Xliff Alt-trans data for current TuvImpl if it is from XLF.
     * 
     * @param tuv
     *            -- TuvImpl object
     */
    @SuppressWarnings("unchecked")
    public static void loadXliffAlt(TuvImpl tuv)
    {
        boolean isXliffAltLoaded = isTuvExtraDataLoaded(tuv.getIdAsLong(),
                XLIFF_ALT);
        if (!isXliffAltLoaded)
        {
            Set<XliffAlt> xliff_alt = new HashSet<XliffAlt>();
            String hql = "from XliffAlt xa where xa.tuvId = " + tuv.getId();
            List<XliffAlt> xliffAlts = (List<XliffAlt>) HibernateUtil
                    .search(hql);
            if (xliffAlts != null && xliffAlts.size() > 0)
            {
                for (XliffAlt alt : xliffAlts)
                {
                    alt.setTuv(tuv);
                }
                xliff_alt.addAll(xliffAlts);
                tuv.setXliffAlt(xliff_alt);
            }

            recordWhichTuvExtraDataAlreadyLoaded(tuv.getIdAsLong(), XLIFF_ALT);
        }
    }

    /**
     * Load IssueEditionRelation data for current TuvImpl if it is GS edition
     * TUV.
     * 
     * @param tuv
     *            -- TuvImpl object
     */
    @SuppressWarnings("unchecked")
    public static void loadIssueEditionRelation(TuvImpl tuv)
    {
        boolean isIERLoaded = isTuvExtraDataLoaded(tuv.getIdAsLong(),
                ISSUE_EDITION_RELATION);
        if (!isIERLoaded)
        {
            Set<IssueEditionRelation> issueERs = new HashSet<IssueEditionRelation>();
            String hql = "from IssueEditionRelation ier where ier.tuvId = "
                    + tuv.getId();
            List<IssueEditionRelation> relations = (List<IssueEditionRelation>) HibernateUtil
                    .search(hql);
            if (relations != null && relations.size() > 0)
            {
                for (IssueEditionRelation relation : relations)
                {
                    relation.setTuv(tuv);
                }

                issueERs.addAll(relations);
                tuv.setIssueEditionRelation(issueERs);
            }

            recordWhichTuvExtraDataAlreadyLoaded(tuv.getIdAsLong(),
                    ISSUE_EDITION_RELATION);
        }
    }
}
