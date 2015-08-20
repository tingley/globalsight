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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.globalsight.everest.tm.Tm;
import com.globalsight.ling.tm2.BaseTmTu;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.ling.tm2.GlobalSightLocalePool;
import com.globalsight.ling.tm2.SegmentTmTu;
import com.globalsight.ling.tm2.SegmentTmTuv;
import com.globalsight.ling.tm2.TmUtil;
import com.globalsight.ling.tm2.indexer.TmSegmentIndexer;
import com.globalsight.ling.tm2.leverage.LeverageIterator;
import com.globalsight.ling.tm2.leverage.LeverageOptions;
import com.globalsight.ling.tm2.leverage.LeveragedTu;
import com.globalsight.ling.tm2.leverage.SegmentIdMap;
import com.globalsight.ling.tm2.lucene.LuceneIndexWriter;
import com.globalsight.ling.tm2.lucene.LuceneUtil;
import com.globalsight.util.GlobalSightLocale;

/**
 * SegmentTmPersistence class is responsible for accessing persistence layer of
 * Segment Tm.
 */

public class SegmentTmPersistence
{
    private static final Logger c_logger = Logger
            .getLogger(SegmentTmPersistence.class);

    private Connection m_connection;

    static public final String SEGMENT_TM_TU_T = "project_tm_tu_t";
    static public final String SEGMENT_TM_TU_T_PROP = "project_tm_tu_t_prop";
    static public final String SEGMENT_TM_TUV_T = "project_tm_tuv_t";
    static public final String CORPUS_MAP = "corpus_map";

    static public final String SEGMENT_TM_TU_L = "project_tm_tu_l";
    static public final String SEGMENT_TM_TUV_L = "project_tm_tuv_l";
    
    static public final String TU_T_ALIAS = " tu_t ";
    static public final String TU_L_ALIAS = " tu_l ";
    static public final String TUV_T_ALIAS = " tuv_t ";
    static public final String TUV_L_ALIAS = " tuv_l ";
    
    static public final String TU_ALIAS = " tu ";
    static public final String TU_ALIAS_1 = " tu1 ";
    static public final String TU_ALIAS_2 = " tu2 ";
    static public final String TUV_ALIAS = " tuv ";
    static public final String TUV_ALIAS_1 = " tuv1 ";
    static public final String TUV_ALIAS_2 = " tuv2 ";

    // delete sql
    private static final String DELETE_FROM = "DELETE FROM ";
    private static final String DELETE_WHERE_ID = " WHERE id = ?";

    // update sql
    private static final String UPDATE = "UPDATE ";
    private static final String UPDATE_SET_TU_ID = " SET tu_id = ? WHERE id = ?";
    private static final String UPDATE_SET_MODIFY_DATE = " SET modify_date = CURRENT_TIMESTAMP, modify_user = ? WHERE id = ?";
    private static final String UPDATE_SET_SEGMENT_STRING = " SET segment_string = ?, segment_clob = null, "
            + "exact_match_key = ?, modify_date = CURRENT_TIMESTAMP, modify_user = ?, sid = ? WHERE id = ?";

    private static final String SELECT_TUVS_BY_TU_ID = "SELECT id, locale_id, segment_string, segment_clob FROM ";
    private static final String SELECT_TUVS_BY_TU_ID_WHERE = " WHERE tu_id = ?";

    public SegmentTmPersistence(Connection p_connection) throws Exception
    {
        m_connection = p_connection;
    }

    /**
     * Leverage exact matches (translatable or localizable) from Segment Tm.
     * This method calls a stored procedure in the database to get leverage
     * matches and returns an Iterator that creates LeverageMatches object while
     * it's iterated by reading the query results.
     * 
     * @param p_sourceLocale
     *            source locale
     * @param p_segments
     *            Collection of original segments (BaseTmTuv)
     * @param p_isTranslatable
     *            indicates if leverage translatable segments
     * @param p_leverageOptions
     *            leverage options
     * @return Iterator that iterates the query results. next() method of this
     *         Iterator returns LeverageMatches object.
     */
    public Iterator leverageExact(GlobalSightLocale p_sourceLocale,
            Collection p_segments, boolean p_isTranslatable,
            LeverageOptions p_leverageOptions, List<Tm> p_tms) throws Exception
    {
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

        SegmentTmExactLeveragerStoredProcCaller caller = new SegmentTmExactLeveragerStoredProcCaller(
                m_connection, p_tms, p_sourceLocale, p_leverageOptions
                        .getLeveragingLocales().getAllLeveragingLocales(),
                lookupTarget, p_segments, p_isTranslatable);

        SegmentIdMap segmentIdMap = TmUtil.buildSegmentIdMap(p_segments);
        return new LeverageIterator(caller.getNextResult(), segmentIdMap,
                p_sourceLocale, p_isTranslatable, LeveragedTu.SEGMENT_TM,
                p_leverageOptions);
    }

    /**
     * Get lock on necessary tables for Segment Tm Tuv update. It locks Segment
     * Tm Tuv and Segment Tm Tu tables
     * 
     * @param p_translatable
     *            indicates the Tuvs are translatable or localizable
     */
    public void getLockForSegmentUpdate(boolean p_translatable)
            throws Exception
    {
        String tuTable = p_translatable ? SEGMENT_TM_TU_T : SEGMENT_TM_TU_L;
        String tuvTable = p_translatable ? SEGMENT_TM_TUV_T : SEGMENT_TM_TUV_L;

        List<String> tableList = new ArrayList<String>();
        tableList.add(tuTable);
        tableList.add(tuvTable);
        
        tableList.add(tuTable + TU_ALIAS);
        tableList.add(tuTable + TU_ALIAS_1);
        tableList.add(tuTable + TU_ALIAS_2);
        
        tableList.add(tuvTable + TUV_ALIAS);
        tableList.add(tuvTable + TUV_ALIAS_1);
        tableList.add(tuvTable + TUV_ALIAS_2);
        
        tableList.add("corpus_map");

        DbUtil.lockTables(m_connection, tableList);
    }
    
    /**
     * Locks necessary tables for Segment Tm population. It locks
     * segment_tm_tu_(t,l) and segdment_tm_tuv_(t, l) tables.
     */
    public void lockSegmentTmTables() throws Exception
    {
        List<String> tableList = new ArrayList<String>();
        tableList.add(SEGMENT_TM_TU_T);
        tableList.add(SEGMENT_TM_TUV_T);
        tableList.add(SEGMENT_TM_TU_L);
        tableList.add(SEGMENT_TM_TUV_L);
        tableList.add(SEGMENT_TM_TU_T + TU_T_ALIAS);
        tableList.add(SEGMENT_TM_TU_L + TU_L_ALIAS);
        tableList.add(SEGMENT_TM_TUV_T + TUV_T_ALIAS);
        tableList.add(SEGMENT_TM_TUV_L + TUV_L_ALIAS);
        
        tableList.add(SEGMENT_TM_TU_T_PROP);
        
        tableList.add(CORPUS_MAP);
        DbUtil.lockTables(m_connection, tableList);
    }

    /**
     * Remove Tuvs from Segment Tm Tuv table. This method also removes Tuvs'
     * indexes.
     * 
     * @param p_tuvs
     *            Collection of Tuvs (BaseTmTuv) to be removed.
     * @param p_translatable
     *            indicates whether Tuvs are translatable or localizable
     * @param p_locale
     *            locale of the Tuvs
     * @param p_tmId
     *            TM id
     * @param p_sourceLocale
     *            indicates whether the Tuvs are source locale
     */
    public void removeTuvs(Collection p_tuvs, boolean p_translatable,
            GlobalSightLocale p_locale, long p_tmId) throws Exception
    {
        // remove indexes
        if (p_translatable
                && (LuceneUtil.getGoldTmIndexDirectory(p_tmId, p_locale, false) != null))
        {
            LuceneIndexWriter indexWriter = new LuceneIndexWriter(p_tmId,
                    p_locale);
            try
            {
                indexWriter.remove(p_tuvs);
            }
            finally
            {
                indexWriter.close();
            }
        }

        // remove Tuvs
        String tuvTable = p_translatable ? SEGMENT_TM_TUV_T : SEGMENT_TM_TUV_L;
        String deleteSql = DELETE_FROM + tuvTable + DELETE_WHERE_ID;
        String deleteCorpusSql = "delete from corpus_map where project_tuv_id = ?";
        PreparedStatement ps = null;
        PreparedStatement deleteCorpusPs = null;
        try
        {
            ps = m_connection.prepareStatement(deleteSql);
            deleteCorpusPs = m_connection.prepareStatement(deleteCorpusSql);
            Iterator itTuv = p_tuvs.iterator();
            int count = 0;
            while (itTuv.hasNext())
            {
                count++;
                BaseTmTuv tuv = (BaseTmTuv) itTuv.next();

                ps.setLong(1, tuv.getId());
                ps.addBatch();

                deleteCorpusPs.setLong(1, tuv.getId());
                deleteCorpusPs.addBatch();
                
                if (count > DbUtil.BATCH_INSERT_UNIT)
                {
                	deleteCorpusPs.executeBatch();
                    ps.executeBatch();
                    count = 0;
                }
            }
            if (count > 0)
            {
            	deleteCorpusPs.executeBatch();

                ps.executeBatch();

            }
        }
        finally
        {
            if (ps != null)
            {
                ps.close();
            }
            if(deleteCorpusPs != null)
            {
            	deleteCorpusPs.close();
            }
        }
    }

	/**
     * Remove Tus from Segment Tm Tu table.
     * 
     * @param p_tus
     *            Collection of Tus (BaseTmTu) to be removed.
     * @param p_translatable
     *            indicates whether Tuvs are translatable or localizable
     */
    public void removeTus(Collection p_tus, boolean p_translatable)
            throws Exception
    {
        String tuTable = p_translatable ? SEGMENT_TM_TU_T : SEGMENT_TM_TU_L;
        String deleteSql = DELETE_FROM + tuTable + DELETE_WHERE_ID;
        String deleteCorpusSql = "delete from corpus_map where project_tu_id = ?";
        PreparedStatement deleteCorpusPS = null;
        PreparedStatement ps = null;
        try
        {
            ps = m_connection.prepareStatement(deleteSql);
            deleteCorpusPS = m_connection.prepareStatement(deleteCorpusSql);
            
            Iterator itTu = p_tus.iterator();
            int count = 0;
            while (itTu.hasNext())
            {
                count++;
                BaseTmTu tu = (BaseTmTu) itTu.next();

                ps.setLong(1, tu.getId());
                ps.addBatch();

                deleteCorpusPS.setLong(1, tu.getId());
                deleteCorpusPS.addBatch();
                if (count > DbUtil.BATCH_INSERT_UNIT)
                {
                	deleteCorpusPS.executeBatch();
                    ps.executeBatch();
                    count = 0; 
                }
            }
            if (count > 0)
            {
            	deleteCorpusPS.executeBatch();
                ps.executeBatch();
            }
        }
        finally
        {
            if (ps != null)
            {
                ps.close();
            }
        }
    }

    /**
     * Change Tuvs' tu_id column and index table's tu_id column.
     * 
     * @param p_tuvs
     *            Collection of BaseTmTuv. The value of tu_id column in the
     *            database for these Tuvs will be changed to the id of Tu these
     *            Tuvs have reference to.
     * @param p_translatable
     *            indicates whether Tuvs are translatable or localizable
     * @param p_locale
     *            locale of the Tuvs
     * @param p_tmId
     *            TM id
     * @param p_sourceLocale
     *            indicates whether the Tuvs are source locale
     */
    public void changeTuId(List p_tuvs, boolean p_translatable,
            GlobalSightLocale p_locale, long p_tmId, boolean p_sourceLocale)
            throws Exception
    {
        // change tu_id in index table
        if (p_translatable
                && (p_sourceLocale || TmSegmentIndexer.indexesTargetSegments(p_tmId)))
        {
            LuceneIndexWriter indexWriter = new LuceneIndexWriter(p_tmId,
                    p_locale);
            try
            {
                indexWriter.remove(p_tuvs);
                indexWriter.index(p_tuvs, p_sourceLocale, false);
            }
            finally
            {
                indexWriter.close();
            }
        }

        String tuvTable = p_translatable ? SEGMENT_TM_TUV_T : SEGMENT_TM_TUV_L;
        String updateSql = UPDATE + tuvTable + UPDATE_SET_TU_ID;

        PreparedStatement ps = null;
        try
        {
            ps = m_connection.prepareStatement(updateSql);
            Iterator itTuv = p_tuvs.iterator();
            int count = 0;
            while (itTuv.hasNext())
            {
                count++;
                BaseTmTuv tuv = (BaseTmTuv) itTuv.next();

                ps.setLong(1, tuv.getTu().getId());
                ps.setLong(2, tuv.getId());
                ps.addBatch();

                if (count > DbUtil.BATCH_INSERT_UNIT)
                {
                    ps.executeBatch();
                    count = 0;
                }
            }
            if (count > 0)
            {
                ps.executeBatch();
            }
        }
        finally
        {
            if (ps != null)
            {
                ps.close();
            }
        }
    }

    /**
     * Update modify_date column in Segment Tuv table to the current time.
     * 
     * @param p_tuvs
     *            Collection of BaseTmTuv. The new value of the modify date is
     *            set to these Tuvs.
     * @param p_translatable
     *            indicates whether Tuvs are translatable or localizable
     */
    public void updateLastModifiedTimeTuvs(Collection p_tuvs,
            boolean p_translatable) throws Exception
    {
        String tuvTable = p_translatable ? SEGMENT_TM_TUV_T : SEGMENT_TM_TUV_L;
        String updateSql = UPDATE + tuvTable + UPDATE_SET_MODIFY_DATE;

        PreparedStatement ps = null;
        try
        {
            ps = m_connection.prepareStatement(updateSql);
            Iterator itTuv = p_tuvs.iterator();
            int count = 0;
            while (itTuv.hasNext())
            {
                count++;
                BaseTmTuv tuv = (BaseTmTuv) itTuv.next();
                // Update modify user.
                ps.setString(1, tuv.getModifyUser());
                ps.setLong(2, tuv.getId());
                ps.addBatch();

                if (count > DbUtil.BATCH_INSERT_UNIT)
                {
                    ps.executeBatch();
                    count = 0;
                }
            }
            if (count > 0)
            {
                ps.executeBatch();
            }
        }
        finally
        {
            if (ps != null)
            {
                ps.close();
            }
        }
    }

    /**
     * Update Tuvs' segment text and reindex the Tuvs
     * 
     * @param p_tuvs
     *            Collection of Tuvs (BaseTmTuv) of which text are updated.
     * @param p_translatable
     *            indicates whether Tuvs are translatable or localizable
     * @param p_locale
     *            locale of the Tuvs
     * @param p_tmId
     *            Tm id of the Tuvs
     * @param p_sourceLocale
     *            indicates whether the Tuvs are source locale
     */
    public void updateTuvs(List p_tuvs, boolean p_translatable,
            GlobalSightLocale p_locale, long p_tmId, boolean p_sourceLocale)
            throws Exception
    {
        if (p_translatable
                && (p_sourceLocale || TmSegmentIndexer.indexesTargetSegments(p_tmId)))
        {
            // reindexes segments
            LuceneIndexWriter indexWriter = new LuceneIndexWriter(p_tmId,
                    p_locale);
            try
            {
                indexWriter.remove(p_tuvs);
                indexWriter.index(p_tuvs, p_sourceLocale, false);
            }
            finally
            {
                indexWriter.close();
            }
        }

        Collection nonClobSegments = new ArrayList();
        // Collection clobSegments = new ArrayList();

        Iterator itTuv = p_tuvs.iterator();
        while (itTuv.hasNext())
        {
            BaseTmTuv tuv = (BaseTmTuv) itTuv.next();

            /**
             * The condition will always be false for MySql.
             */
            nonClobSegments.add(tuv);
        }

        if (nonClobSegments.size() > 0)
        {
            updateNonClobTuvs(nonClobSegments, p_translatable);
        }
    }

    /**
     * Check if the given Tuvs still exists in a Segment Tuv table. This method
     * returns a collection of BaseTmTuv that don't exist any more
     * 
     * @param p_tuvs
     *            Collection of Tus (BaseTmTu) to be checked.
     * @param p_translatable
     *            indicates whether Tuvs are translatable or localizable
     * @return Collection of BaseTmTuv that don't exist any more
     */
    public Collection checkForTuvExistence(Collection p_tuvs,
            boolean p_translatable) throws Exception
    {
        // the stored procedure returns all non existing tuv ids
        ResultSet rs = null;
        Collection nonExistingTuvIds = new ArrayList();
        try
        {
            CheckForExistenceProcCaller caller = new CheckForExistenceProcCaller(
                    m_connection, p_tuvs, p_translatable);
            rs = caller.getNextResult();
            while (rs != null && rs.next())
                nonExistingTuvIds.add(new Long(rs.getLong(1)));
        }
        finally
        {
            DbUtil.closeAll(rs);
        }
        return getTuvsById(p_tuvs, nonExistingTuvIds);
    }

    /**
     * Check for the existence of duplicate source segment in the same Project
     * Tm. If such segment exists, get the two duplicate Tus along with all
     * their target segments.
     * 
     * @param p_tuvs
     *            Collection of source BaseTmTuv for the duplicate check
     * @param p_tmId
     *            Tm id to check for the duplicate
     * @param p_locale
     *            Source locale
     * @param p_translatable
     *            indicates whether Tuvs are translatable or localizable
     * @return Map of duplicate Tus. Key: BaseTmTu one of Tuvs in p_tuvs belongs
     *         to Value: BaseTmTu that has a duplicate source segment with the
     *         Tu in Key
     */
    public Map checkForTuvDuplicatesForSource(Collection p_tuvs, long p_tmId,
            GlobalSightLocale p_locale, boolean p_translatable)
            throws Exception
    {
        CheckForDuplicateForSourceProcCaller caller = new CheckForDuplicateForSourceProcCaller(
                m_connection, p_tuvs, p_tmId, p_locale, p_translatable);
        ResultSet rs = caller.getNextResult();
        try
        {
            return createDuplicateTuPairs(rs, p_tuvs, p_locale, p_tmId,
                    p_translatable);
        }
        finally
        {
            DbUtil.closeAll(rs);
        }
    }

    /**
     * Check for the existence of duplicate segment in the same Tu. If such
     * segment exists, get the two duplicate Tuvs.
     * 
     * @param p_tuvs
     *            Collection of BaseTmTuv for checking the duplicate
     * @param p_tmId
     *            Tm id to check for the duplicate
     * @param p_locale
     *            locale of the Tuvs
     * @param p_translatable
     *            indicates whether Tuvs are translatable or localizable
     * @return Map of duplicate Tuvs. Key: One of the Tuvs (BaseTmTuv) in p_tuvs
     *         that has a duplicate in the same Tu Value: The detected duplicate
     *         of the segment in Key (BaseTmTuv)
     */
    public Map checkForTuvDuplicatesForTarget(Collection p_tuvs, long p_tmId,
            GlobalSightLocale p_locale, boolean p_translatable)
            throws Exception
    {
        CheckForDuplicateForTargetProcCaller caller = new CheckForDuplicateForTargetProcCaller(
                m_connection, p_tuvs, p_locale, p_translatable);
        ResultSet rs = caller.getNextResult();

        try
        {
            return createDuplicateTuvPairs(rs, p_tuvs, p_locale, p_tmId,
                    p_translatable);
        }
        finally
        {
            DbUtil.closeAll(rs);
        }
    }

    /**
     * Retrieve TUVs that belong to specified TUs.
     * 
     * @param p_tus
     *            Collection of BaseTmTu.
     * @param p_translatable
     *            indicates whether Tus are translatable or localizable
     * @return Collection of TUVs
     */
    public Collection retrieveTuvsByTuId(Collection p_tus,
            boolean p_translatable) throws Exception
    {
        String tuvTable = p_translatable ? SEGMENT_TM_TUV_T : SEGMENT_TM_TUV_L;
        String selectSql = SELECT_TUVS_BY_TU_ID + tuvTable
                + SELECT_TUVS_BY_TU_ID_WHERE;

        Collection tuvs = new ArrayList();

        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            ps = m_connection.prepareStatement(selectSql);
            Iterator itTu = p_tus.iterator();
            while (itTu.hasNext())
            {
                BaseTmTu tu = (BaseTmTu) itTu.next();
                ps.setLong(1, tu.getId());
                rs = ps.executeQuery();
                while (rs.next())
                {
                    GlobalSightLocale locale = getGlobalSightLocale(rs);
                    String segmentText = getTuvSegment(rs);
                    SegmentTmTuv tuv = new SegmentTmTuv(rs.getLong("id"),
                            segmentText, locale);
                    tuv.setTu(tu);
                    tuvs.add(tuv);
                }
                DbUtil.silentClose(rs);
            }
        }
        finally
        {
            DbUtil.silentClose(rs);
            DbUtil.silentClose(ps);
        }

        return tuvs;
    }

    // update segment_string in Segment Tuv table
    private void updateNonClobTuvs(Collection p_nonClobTuvs,
            boolean p_translatable) throws Exception
    {
        String tuvTable = p_translatable ? SEGMENT_TM_TUV_T : SEGMENT_TM_TUV_L;
        String updateSql = UPDATE + tuvTable + UPDATE_SET_SEGMENT_STRING;

        PreparedStatement ps = null;
        try
        {
            ps = m_connection.prepareStatement(updateSql);
            Iterator itTuv = p_nonClobTuvs.iterator();
            int count = 0;
            String sid = null;
            while (itTuv.hasNext())
            {
                count++;
                BaseTmTuv tuv = (BaseTmTuv) itTuv.next();

                ps.setString(1, tuv.getSegment());
                ps.setLong(2, tuv.getExactMatchKey());
                ps.setString(3, tuv.getModifyUser());
                sid = tuv.getSid();
                if (sid != null && sid.length() > 254)
                {
                	sid = sid.substring(0, 254);
                }
                ps.setString(4, sid);
                ps.setLong(5, tuv.getId());
                ps.addBatch();

                if (count > DbUtil.BATCH_INSERT_UNIT)
                {
                    ps.executeBatch();
                    count = 0;
                }
            }
            if (count > 0)
            {
                ps.executeBatch();
            }
        }
        finally
        {
            if (ps != null)
            {
                ps.close();
            }
        }
    }


    /**
     * Pick Tuvs from a given list of Tuvs that match by a given list of Tuv
     * ids.
     * 
     * @param p_tuvs
     *            Collection of BaseTmTuv
     * @param p_tuvIds
     *            Collection of Tuv Ids (Long)
     */
    private Collection getTuvsById(Collection p_tuvs, Collection p_tuvIds)
    {
        Collection returnTuvs = new ArrayList();

        for (Iterator itTuvId = p_tuvIds.iterator(); itTuvId.hasNext();)
        {
            long id = ((Long) itTuvId.next()).longValue();

            for (Iterator itTuv = p_tuvs.iterator(); itTuv.hasNext();)
            {
                BaseTmTuv tuv = (BaseTmTuv) itTuv.next();
                if (tuv.getId() == id)
                {
                    returnTuvs.add(tuv);
                    break;
                }
            }
        }

        return returnTuvs;
    }

    private Map createDuplicateTuvPairs(ResultSet p_rs, Collection p_tuvs,
            GlobalSightLocale p_locale, long p_tmId, boolean p_translatable)
            throws Exception
    {
        Map tuvPairs = new HashMap();

        while (p_rs != null && p_rs.next())
        {
            BaseTmTuv orgTuv = findOriginalTuv(p_rs.getLong("org_tuv_id"),
                    p_tuvs);

            BaseTmTuv dupTuv = new SegmentTmTuv(p_rs.getLong("tuv_id"),
                    getTuvSegment(p_rs), getGlobalSightLocale(p_rs));
            dupTuv.setExactMatchKey(p_rs.getLong("exact_match_key"));
            dupTuv.setModifyDate(p_rs.getTimestamp("modify_date"));

            // Tuv always need its Tu
            BaseTmTu tu = (BaseTmTu) orgTuv.getTu().clone();
            tu.addTuv(dupTuv);

            // check if the two Tuvs are really duplicate
            if (orgTuv.equals(dupTuv))
            {
                // add modify user
                dupTuv.setModifyUser(orgTuv.getModifyUser());
                tuvPairs.put(orgTuv, dupTuv);
            }
        }

        return tuvPairs;
    }

    private Map createDuplicateTuPairs(ResultSet p_rs, Collection p_tuvs,
            GlobalSightLocale p_locale, long p_tmId, boolean p_translatable)
            throws Exception
    {
        Map tuPairs = new HashMap();

        SegmentTmTu keyTu = null;
        SegmentTmTu valueTu = null;
        long prevOrgId = 0;
        long prevTuId = 0;

        while (p_rs != null && p_rs.next())
        {
            long orgId = p_rs.getLong("org_id");
            if (prevOrgId != orgId)
            {
                // check if the two Tus are really duplicate
                if (prevOrgId != 0
                        && keyTu.getSourceTuv().equals(valueTu.getSourceTuv()))
                {
                    tuPairs.put(keyTu, valueTu);
                }

                prevOrgId = orgId;
            }

            long tuId = p_rs.getLong("tu_id");
            if (prevTuId != tuId)
            {
                SegmentTmTu tu = new SegmentTmTu(tuId, p_tmId, p_rs
                        .getString("format"), p_rs.getString("type"),
                        p_translatable, p_locale);

                if (tuId == orgId)
                {
                    keyTu = tu;
                }
                else
                {
                    valueTu = tu;
                }

                prevTuId = tuId;
            }

            SegmentTmTuv tuv = new SegmentTmTuv(p_rs.getLong("tuv_id"),
                    getTuvSegment(p_rs), getGlobalSightLocale(p_rs));
            tuv.setExactMatchKey(p_rs.getLong("exact_match_key"));
            tuv.setModifyDate(p_rs.getTimestamp("modify_date"));

            BaseTmTuv orgSrc = findOriginalTuv(tuv.getId(), p_tuvs);
            if (orgSrc != null)
            {
                tuv.setSegment(orgSrc.getSegment());
                tuv.setExactMatchKey(orgSrc.getExactMatchKey());
            }

            if (tuId == orgId)
            {
                keyTu.addTuv(tuv);
            }
            else
            {
                valueTu.addTuv(tuv);
            }
        }

        // add the last Tu pair
        if (keyTu != null && valueTu != null
                && keyTu.getSourceTuv().equals(valueTu.getSourceTuv()))
        {
            tuPairs.put(keyTu, valueTu);
        }

        return tuPairs;
    }

    private String getTuvSegment(ResultSet p_rs) throws Exception
    {
        String segment = p_rs.getString("segment_string");
        if (segment == null)
        {
            segment = DbUtil.readClob(p_rs, "segment_clob");
        }
        return segment;
    }

    private GlobalSightLocale getGlobalSightLocale(ResultSet p_rs)
            throws Exception
    {
        long locale_id = p_rs.getLong("locale_id");
        return GlobalSightLocalePool.getLocaleById(locale_id);
    }

    private BaseTmTuv findOriginalTuv(long p_id, Collection p_tuvs)
    {
        BaseTmTuv found = null;

        Iterator itTuv = p_tuvs.iterator();
        while (itTuv.hasNext())
        {
            BaseTmTuv tuv = (BaseTmTuv) itTuv.next();
            if (tuv.getId() == p_id)
            {
                found = tuv;
                break;
            }
        }

        return found;
    }

}
