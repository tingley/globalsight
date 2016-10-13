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
package com.globalsight.ling.inprogresstm.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.everest.persistence.tuv.BigTableUtil;
import com.globalsight.ling.inprogresstm.leverage.LeveragedInProgressTu;
import com.globalsight.ling.inprogresstm.leverage.LeveragedInProgressTuv;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.ling.tm2.persistence.Sequence;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.edit.EditUtil;

/**
 * TmPersistence class is responsible for accessing persistence layer of
 * in-progress Tm.
 */

public class TmPersistence
{
    private static final Logger c_logger = Logger
            .getLogger(TmPersistence.class.getName());

    // table names
    private static final String SRC_T_TABLE = "ip_tm_src_t";
    private static final String SRC_L_TABLE = "ip_tm_src_l";
    private static final String TRG_T_TABLE = "ip_tm_trg_t";
    private static final String TRG_L_TABLE = "ip_tm_trg_l";

    private static final String TRG_TABLE_ALIAS = " TRG ";
    private static final String SRC_TABLE_ALIAS = " SRC ";

    // select column list
    private static final String SELECT_COLUMN_LIST = "SELECT SRC.ID, SRC.JOB_ID, SRC.TYPE, SRC.SEGMENT_STRING, SRC.SEGMENT_CLOB, "
            + "TRG.ID, TRG.SEGMENT_STRING, TRG.SEGMENT_CLOB, TRG.TU_ID ";

    // from table list
    private static final String FROM_T_TABLE_LIST = "FROM IP_TM_TRG_T TRG, IP_TM_SRC_T SRC ";

    private static final String FROM_L_TABLE_LIST = "FROM IP_TM_TRG_L TRG, IP_TM_SRC_L SRC ";

    // Sort clause
    private static final String ORDER_BY_SRC_ID = " ORDER BY SRC.ID";

    // Get single tu query
    private static final String SINGLE_TU_QUERY = SELECT_COLUMN_LIST
            + FROM_T_TABLE_LIST
            + "WHERE SRC.ID = TRG.SRC_ID AND TRG.LOCALE_ID = ? AND SRC.ID = ?";

    // Leverage localizable query
    private static final String LEVERAGE_LOCALIZABLE_QUERY = SELECT_COLUMN_LIST
            + FROM_L_TABLE_LIST
            + " WHERE SRC.ID = TRG.SRC_ID"
            + " AND TRG.LOCALE_ID = ?"
            + " AND SRC.LOCALE_ID = ?"
            + " AND SRC.EXACT_MATCH_KEY = ?";

    // Get translatable segment query
    private static final String T_SEGMENT_QUERY = SELECT_COLUMN_LIST
            + FROM_T_TABLE_LIST
            + "WHERE SRC.ID = TRG.SRC_ID AND TRG.LOCALE_ID = ? AND SRC.ID IN ";

    // Select for population
    private static final String SELECT_FOR_POPULATION_CONDITION_T = SELECT_COLUMN_LIST
            + FROM_T_TABLE_LIST
            + "WHERE SRC.ID = TRG.SRC_ID AND TRG.LOCALE_ID = ? AND SRC.TYPE = ? "
            + "AND SRC.LOCALE_ID = ? AND SRC.JOB_ID = ? AND SRC.EXACT_MATCH_KEY = ? "
            + ORDER_BY_SRC_ID;

    private static final String SELECT_FOR_POPULATION_CONDITION_L = SELECT_COLUMN_LIST
            + FROM_L_TABLE_LIST
            + "WHERE SRC.ID = TRG.SRC_ID AND TRG.LOCALE_ID = ? AND SRC.TYPE = ? "
            + "AND SRC.LOCALE_ID = ? AND SRC.JOB_ID = ? AND SRC.EXACT_MATCH_KEY = ? "
            + ORDER_BY_SRC_ID;

    // delete sql
    private static final String DELETE_FROM = "DELETE FROM ";
    private static final String DELETE_SRC = " WHERE job_id = ?";
    private static final String DELETE_TRG3 = " WHERE id = ?";

    // insert sql
    static private final String INSERT_INTO = "INSERT INTO ";

    static private final String INSERT_NON_CLOB_SRC = " (id, job_id, population_tm_id, locale_id, type, exact_match_key, "
            + "segment_string, segment_clob) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    static private final String INSERT_NON_CLOB_TRG = " (id, src_id, tu_id, locale_id, segment_string, segment_clob) "
            + "VALUES (?, ?, ?, ?, ?, ?)";

    // update sql
    private static final String UPDATE = "UPDATE ";
    private static final String UPDATE_SET_SEGMENT_STRING = " SET segment_string = ?, segment_clob = ? WHERE id = ?";

    private Connection m_connection;

    public TmPersistence(Connection p_connection) throws Exception
    {
        m_connection = p_connection;
    }

    /**
     * Leverage localizable matches from in-progress TM. Matches are retrieved
     * based on exact match key from specified job ids (synonym to tm id).
     * 
     * @param p_sourceLocale
     *            source locale
     * @param p_targetLocale
     *            target locale
     * @param p_exactMatchKey
     *            exact match key
     * @param p_jobId
     *            job id to leverage from
     * @param p_tmIds
     *            TM ids to leverage from. This parameter can be null.
     * @return Collection of LeveragedInProgressTu objects
     */
    public Collection leverageLocalizable(GlobalSightLocale p_sourceLocale,
            GlobalSightLocale p_targetLocale, long p_exactMatchKey,
            long p_jobId, Set<Long> p_tmIds) throws Exception
    {
        HashSet<Long> jobIds = new HashSet<Long>();
        jobIds.add(p_jobId);

        return leverageLocalizable(p_sourceLocale, p_targetLocale,
                p_exactMatchKey, jobIds, p_tmIds);
    }
    
    /**
     * Leverage localizable matches from in-progress TM. Matches are retrieved
     * based on exact match key from specified job ids (synonym to tm id).
     * 
     * @param p_sourceLocale
     *            source locale
     * @param p_targetLocale
     *            target locale
     * @param p_exactMatchKey
     *            exact match key
     * @param p_jobIds
     *            job IDs to leverage from
     * @param p_tmIds
     *            TM IDs to leverage from. This parameter can be null.
     * @return Collection of LeveragedInProgressTu objects
     */
    public Collection leverageLocalizable(GlobalSightLocale p_sourceLocale,
            GlobalSightLocale p_targetLocale, long p_exactMatchKey,
            Set<Long> p_jobIds, Set<Long> p_tmIds) throws Exception
    {
        // In general, jobIds can't be empty.If empty, return null regardless
        // what p_tmIds are.
        if (p_jobIds == null || p_jobIds.size() == 0) {
            return null;
        }
        
        String querySQL = getQuerySQL(p_jobIds, p_tmIds);

        Collection matches = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            ps = m_connection.prepareStatement(querySQL);

            ps.setLong(1, p_targetLocale.getId());
            ps.setLong(2, p_sourceLocale.getId());
            ps.setLong(3, p_exactMatchKey);
            if (p_tmIds != null && p_tmIds.size() > 0)
            {
                ps.setLong(4, p_targetLocale.getId());
                ps.setLong(5, p_sourceLocale.getId());
                ps.setLong(6, p_exactMatchKey);
            }

            rs = ps.executeQuery();
            matches = createLeveragedTus(rs, p_sourceLocale, p_targetLocale,
                    false);
        }
        finally
        {
            DbUtil.silentClose(rs);
            DbUtil.silentClose(ps);
        }

        return matches;
    }
    
    private String getQuerySQL(Set<Long> p_jobIds, Set<Long> p_tmIds)
    {
        StringBuffer query = new StringBuffer();
        
        query = query.append(LEVERAGE_LOCALIZABLE_QUERY);
        query = query.append(" AND SRC.JOB_ID IN ");
        query = query.append(DbUtil.createIntegerInClause(p_jobIds));

        if (p_tmIds != null && p_tmIds.size() > 0)
        {
            query = query.append(" UNION ");
            query = query.append(LEVERAGE_LOCALIZABLE_QUERY);
            query = query.append(" AND SRC.POPULATION_TM_ID IN ");
            query = query.append(DbUtil.createIntegerInClause(p_tmIds));
        }

        return query.toString();
    }

    /**
     * Retrieve translatable TUs by src ids.
     * 
     * @param p_sourceLocale
     *            source locale
     * @param p_targetLocale
     *            target locale
     * @param p_srcIds
     *            Set of src ids (Long)
     * @return Collection of LeveragedInProgressTu objects
     */
    public Collection getTranslatableSegment(GlobalSightLocale p_sourceLocale,
            GlobalSightLocale p_targetLocale, Set p_srcIds) throws Exception
    {
        String query = new StringBuffer(T_SEGMENT_QUERY).append(
                DbUtil.createIntegerInClause(p_srcIds)).append(ORDER_BY_SRC_ID)
                .toString();

        Collection matches = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            ps = m_connection.prepareStatement(query);
            ps.setLong(1, p_targetLocale.getId());
            rs = ps.executeQuery();
            matches = createLeveragedTus(rs, p_sourceLocale, p_targetLocale,
                    true);
        }
        finally
        {
            DbUtil.silentClose(rs);
            DbUtil.silentClose(ps);
        }

        return matches;
    }

    /**
     * Retrieve a translatable TU by a src id.
     * 
     * @param p_sourceLocale
     *            source locale
     * @param p_targetLocale
     *            target locale
     * @param p_srcId
     *            src id
     * @return LeveragedInProgressTu object
     */
    public LeveragedInProgressTu getTranslatableSegment(
            GlobalSightLocale p_sourceLocale, GlobalSightLocale p_targetLocale,
            long p_srcId) throws Exception
    {
        Collection matches = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            // m_getSingleTuStmt
            ps = m_connection.prepareStatement(SINGLE_TU_QUERY);
            ps.setLong(1, p_targetLocale.getId());
            ps.setLong(2, p_srcId);
            rs = ps.executeQuery();
            matches = createLeveragedTus(rs, p_sourceLocale, p_targetLocale,
                    true);
        }
        finally
        {
            DbUtil.silentClose(rs);
            DbUtil.silentClose(ps);
        }

        LeveragedInProgressTu tu = null;
        Iterator it = matches.iterator();
        if (it.hasNext())
        {
            tu = (LeveragedInProgressTu) it.next();
        }

        return tu;
    }

    /**
     * Retrieve TUs for population
     * 
     * @param p_sourceLocale
     *            source locale
     * @param p_targetLocale
     *            target locale
     * @param p_exactMatchKey
     *            exact match key of source segment
     * @param p_jobId
     *            job id
     * @param p_type
     *            type of the segment
     * @param p_isTranslatable
     *            true to retrieve translatable segments
     * @return Collection of LeveragedInProgressTu objects
     */
    public Collection getTusForPopulation(GlobalSightLocale p_sourceLocale,
            GlobalSightLocale p_targetLocale, long p_exactMatchKey,
            long p_jobId, String p_type, boolean p_isTranslatable)
            throws Exception
    {
        String query = null;
        if (p_isTranslatable)
        {
            query = SELECT_FOR_POPULATION_CONDITION_T;
        }
        else
        {
            query = SELECT_FOR_POPULATION_CONDITION_L;
        }

        Collection matches = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            ps = m_connection.prepareStatement(query);
            ps.setLong(1, p_targetLocale.getId());
            ps.setString(2, p_type);
            ps.setLong(3, p_sourceLocale.getId());
            ps.setLong(4, p_jobId);
            ps.setLong(5, p_exactMatchKey);
            rs = ps.executeQuery();
            matches = createLeveragedTus(rs, p_sourceLocale, p_targetLocale,
                    p_isTranslatable);
        }
        finally
        {
            DbUtil.silentClose(rs);
            DbUtil.silentClose(ps);

        }

        return matches;
    }

    /**
     * Insert source segment
     * 
     * @param p_sourceSegment
     *            source segment to be inserted. The exact match key must have
     *            been calculated.
     * @param p_jobId
     *            job id
     * @param p_populateTmId
     *            Segment TM id this job is to be saved
     * @return id of this segment
     */
    public long insertSrcSegment(BaseTmTuv p_sourceSegment, long p_jobId,
            long p_populateTmId) throws Exception
    {
        String tableName = (p_sourceSegment.isTranslatable() ? SRC_T_TABLE
                : SRC_L_TABLE);

        String insertSql = INSERT_INTO + tableName + INSERT_NON_CLOB_SRC;

        String seqName = (p_sourceSegment.isTranslatable() ? Sequence.SRC_T_SEQ
                : Sequence.SRC_L_SEQ);
        long id = Sequence.allocateIds(seqName, 1);

        PreparedStatement ps = null;
        try
        {
            ps = m_connection.prepareStatement(insertSql);
            ps.setLong(1, id);
            ps.setLong(2, p_jobId);
            ps.setLong(3, p_populateTmId);
            ps.setLong(4, p_sourceSegment.getLocale().getId());
            ps.setString(5, p_sourceSegment.getType());
            ps.setLong(6, p_sourceSegment.getExactMatchKey());
            String segment = p_sourceSegment.getSegment();
            ps.setString(7, null);
            ps.setString(8, null);
            if (segment != null)
            {
                if (EditUtil.getUTF8Len(segment) <= PersistentObject.CLOB_THRESHOLD)
                {
                	ps.setString(7, segment);
                }
                else
                {
                	ps.setString(8, segment);
                }
            }

            ps.executeUpdate();
        }
        finally
        {
            if (ps != null)
            {
                ps.close();
            }
        }

        return id;
    }

    /**
     * Insert target segment
     * 
     * @param p_targetSegment
     *            target segment to be inserted.
     * @param p_srcId
     *            src id
     * @param p_tuId
     *            translation_unit id
     * @return id of this segment
     */
    public long insertTrgSegment(BaseTmTuv p_targetSegment, long p_srcId,
            long p_tuId) throws Exception
    {
        String tableName = (p_targetSegment.isTranslatable() ? TRG_T_TABLE
                : TRG_L_TABLE);

        String insertSql = INSERT_INTO + tableName + INSERT_NON_CLOB_TRG;

        String seqName = (p_targetSegment.isTranslatable() ? Sequence.TRG_T_SEQ
                : Sequence.TRG_L_SEQ);
        long id = Sequence.allocateIds(seqName, 1);

        PreparedStatement ps = null;
        try
        {
            ps = m_connection.prepareStatement(insertSql);
            ps.setLong(1, id);
            ps.setLong(2, p_srcId);
            ps.setLong(3, p_tuId);
            ps.setLong(4, p_targetSegment.getLocale().getId());
            ps.setString(5, null);
            ps.setString(6, null);
            String segment = p_targetSegment.getSegment();
            if (segment != null)
            {
                if (EditUtil.getUTF8Len(segment) <= PersistentObject.CLOB_THRESHOLD)
                {
                	ps.setString(5, segment);
                }
                else
                {
                	ps.setString(6, segment);
                }
            }

            ps.executeUpdate();
        }
        finally
        {
            if (ps != null)
            {
                ps.close();
            }
        }

        return id;
    }

    /**
     * Update target segment text
     * 
     * @param p_id
     *            segment id
     * @param p_targetSegment
     *            target segment to be updated.
     */
    public void updateTrgSegment(long p_id, BaseTmTuv p_targetSegment)
            throws Exception
    {
        String tableName = (p_targetSegment.isTranslatable() ? TRG_T_TABLE
                : TRG_L_TABLE);

        String updateSql = UPDATE + tableName + UPDATE_SET_SEGMENT_STRING;

        PreparedStatement ps = null;
        try
        {
            ps = m_connection.prepareStatement(updateSql);
            String segment = p_targetSegment.getSegment();
            if (segment != null)
            {
                if (EditUtil.getUTF8Len(segment) <= PersistentObject.CLOB_THRESHOLD)
                {
                	ps.setString(1, segment);
                	ps.setString(2, null);
                }
                else
                {
                	ps.setString(1, null);
                	ps.setString(2, segment);
                }
            }
            ps.setLong(3, p_id);

            ps.executeUpdate();
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
     * Delete segments for a given job id from the in-progress TM.
     * 
     * @param p_jobId
     *            job id
     */
    public void deleteSegments(long p_jobId) throws Exception
    {
        // delete translatable
        deleteTargetSegments(p_jobId, TRG_T_TABLE, SRC_T_TABLE);
        deleteSourceSegments(p_jobId, SRC_T_TABLE);

        // delete localizable
        deleteTargetSegments(p_jobId, TRG_L_TABLE, SRC_L_TABLE);
        deleteSourceSegments(p_jobId, SRC_L_TABLE);
    }

    /**
     * Delete a target segment by id
     * 
     * @param p_id
     *            segment id
     * @param p_isTranslatable
     *            true if the segment is translatable
     */
    public void deleteTrgTuv(long p_id, boolean p_isTranslatable)
            throws Exception
    {
        String deleteSql = DELETE_FROM
                + (p_isTranslatable ? TRG_T_TABLE : TRG_L_TABLE) + DELETE_TRG3;

        PreparedStatement ps = null;
        try
        {
            ps = m_connection.prepareStatement(deleteSql);
            ps.setLong(1, p_id);
            ps.executeUpdate();
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
     * Get lock on necessary tables for saving segments.
     * 
     * @param p_translatable
     *            indicates the Tuvs are translatable or localizable
     */
    public void getLockForSave(boolean p_translatable) throws Exception
    {
        String srcTable = p_translatable ? SRC_T_TABLE : SRC_L_TABLE;
        String trgTable = p_translatable ? TRG_T_TABLE : TRG_L_TABLE;

        ArrayList<String> tableList = new ArrayList<String>();
        tableList.add(srcTable + SRC_TABLE_ALIAS);
        tableList.add(trgTable + TRG_TABLE_ALIAS);
        tableList.add(srcTable);
        tableList.add(trgTable);
        
        if (p_translatable)
        {
            tableList.add(IndexPersistence.INDEX_TABLE);
        }

        DbUtil.lockTables(m_connection, tableList);
    }

    /**
     * Get lock on necessary tables for deleting segments.
     * 
     */
    public void getLockForDeletion() throws Exception
    {
        ArrayList<String> tableList = new ArrayList<String>();
        tableList.add(SRC_T_TABLE);
        tableList.add(SRC_L_TABLE);
        tableList.add(TRG_T_TABLE);
        tableList.add(TRG_L_TABLE);
        tableList.add(IndexPersistence.INDEX_TABLE);

        DbUtil.lockTables(m_connection, tableList);
    }

    // /// private methods ////////

    // create Collection of LeveragedInProgressTu objects
    private Collection<LeveragedInProgressTu> createLeveragedTus(
            ResultSet p_rs, GlobalSightLocale p_sourceLocale,
            GlobalSightLocale p_targetLocale, boolean p_translatable)
            throws Exception
    {
        ArrayList<LeveragedInProgressTu> matches = new ArrayList<LeveragedInProgressTu>();
        long currentSrcId = 0;
        LeveragedInProgressTu currentTu = null;

        while (p_rs.next())
        {
            long jobId = p_rs.getLong(2); // src.job_id
            long tuId = p_rs.getLong(9); // trg.tu_id
            HashMap<Long, CreationModifyUserDateHolder> cmInfos = getCreationModifyInfo(jobId, tuId);

            long srcId = p_rs.getLong(1); // src.id
            if (srcId != currentSrcId)
            {
                currentSrcId = srcId;

                // create TU
                currentTu = createTu(srcId, p_rs, p_sourceLocale,
                        p_translatable);
                matches.add(currentTu);

                // create Src TUV
                LeveragedInProgressTuv srcTuv = createSrcTuv(srcId, p_rs,
                        p_sourceLocale,
                        cmInfos.get(p_sourceLocale.getIdAsLong()));
                currentTu.addTuv(srcTuv);
            }

            // create trg TUV
            LeveragedInProgressTuv trgTuv = createTrgTuv(p_rs, p_targetLocale,
                    cmInfos.get(p_targetLocale.getIdAsLong()));
            currentTu.addTuv(trgTuv);
        }

        return matches;
    }

    private LeveragedInProgressTu createTu(long p_id, ResultSet p_rs,
            GlobalSightLocale p_sourceLocale, boolean p_translatable)
            throws Exception
    {
        long jobId = p_rs.getLong(2); // src.job_id
        String type = p_rs.getString(3); // src.type

        LeveragedInProgressTu tu = new LeveragedInProgressTu(p_id, jobId, type,
                p_translatable, p_sourceLocale);

        return tu;
    }

    private LeveragedInProgressTuv createSrcTuv(long p_id, ResultSet p_rs,
            GlobalSightLocale p_sourceLocale,
            CreationModifyUserDateHolder cmInfo) throws Exception
    {
        String segment = getSrcTuvSegment(p_rs);
        LeveragedInProgressTuv ipTuv = new LeveragedInProgressTuv(p_id,
                segment, p_sourceLocale);
        if (cmInfo != null)
        {
            ipTuv.setCreationUser(cmInfo.getCreationUser());
            ipTuv.setCreationDate(cmInfo.getCreationDate());
            ipTuv.setModifyUser(cmInfo.getModifyUser());
            ipTuv.setModifyDate(cmInfo.getModifyDate());
        }
        return ipTuv;
    }

    private LeveragedInProgressTuv createTrgTuv(ResultSet p_rs,
            GlobalSightLocale p_targetLocale,
            CreationModifyUserDateHolder cmInfo) throws Exception
    {
        long id = p_rs.getLong(6); // trg.id
        long tuId = p_rs.getLong(9); // trg.tu_id
        String segment = getTrgTuvSegment(p_rs);
        LeveragedInProgressTuv ipTuv = new LeveragedInProgressTuv(id, segment,
                p_targetLocale);
        ipTuv.setJobDataTuId(tuId);
        if (cmInfo != null)
        {
            ipTuv.setCreationUser(cmInfo.getCreationUser());
            ipTuv.setCreationDate(cmInfo.getCreationDate());
            ipTuv.setModifyUser(cmInfo.getModifyUser());
            ipTuv.setModifyDate(cmInfo.getModifyDate());
        }

        return ipTuv;
    }

    private String getSrcTuvSegment(ResultSet p_rs) throws Exception
    {
        String segment = p_rs.getString(4); // src.segment_string
        if (segment == null)
        {
            segment = DbUtil.readClob(p_rs, 5); // src.segment_clob
        }
        return segment;
    }

    private String getTrgTuvSegment(ResultSet p_rs) throws Exception
    {
        String segment = p_rs.getString(7); // trg.segment_string
        if (segment == null)
        {
            segment = DbUtil.readClob(p_rs, 8); // trg.segment_clob
        }
        return segment;
    }

    private void deleteTargetSegments(long p_jobId, String p_targetTable,
            String p_sourceTable) throws Exception
    {
        StringBuilder deleteBuf = new StringBuilder();
        deleteBuf.append("delete _trgTable_ from _trgTable_, _srcTable_ ");
        deleteBuf.append("where _trgTable_.src_id = _srcTable_.id ");
        deleteBuf.append(" and _srcTable_.job_id = ? ");

		String deleteSql = deleteBuf.toString()
				.replaceAll("_srcTable_", p_sourceTable)
				.replaceAll("_trgTable_", p_targetTable);

        PreparedStatement ps = null;
        try
        {
            ps = m_connection.prepareStatement(deleteSql);
            ps.setLong(1, p_jobId);
            ps.executeUpdate();
        }
        finally
        {
            if (ps != null)
            {
                ps.close();
            }
        }
    }

    private void deleteSourceSegments(long p_jobId, String p_sourceTable)
            throws Exception
    {
        String deleteSql = DELETE_FROM + p_sourceTable + DELETE_SRC;

        PreparedStatement ps = null;
        try
        {
            ps = m_connection.prepareStatement(deleteSql);
            ps.setLong(1, p_jobId);
            ps.executeUpdate();
        }
        finally
        {
            if (ps != null)
            {
                ps.close();
            }
        }
    }

    private HashMap<Long, CreationModifyUserDateHolder> getCreationModifyInfo(
            long p_jobId, long tuId)
    {
        HashMap<Long, CreationModifyUserDateHolder> result = new HashMap<Long, CreationModifyUserDateHolder>();

        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            con = DbUtil.getConnection();
            String tuvTableName = BigTableUtil.getTuvTableJobDataInByJobId(p_jobId);
            String query = "SELECT tuv.locale_id, tuv.creation_user, tuv.creation_date, tuv.modify_user, tuv.last_modified FROM "
                    + tuvTableName + " tuv "
                    + "WHERE tu_id = ? "
                    + "AND state != 'OUT_OF_DATE'";
            ps = con.prepareStatement(query);
            ps.setLong(1, tuId);
            rs = ps.executeQuery();
            while (rs.next())
            {
                CreationModifyUserDateHolder holder = new CreationModifyUserDateHolder(
                        rs.getString(2), rs.getTimestamp(3), rs.getString(4),
                        rs.getTimestamp(5));
                result.put(rs.getLong(1), holder);
            }
        }
        catch (Exception e)
        {
            c_logger.warn(
                    "Error happens when fetch creation/modify user-date info for job "
                            + p_jobId + " and tuId " + tuId, e);
        }
        finally
        {
            DbUtil.silentClose(rs);
            DbUtil.silentClose(ps);
            DbUtil.silentReturnConnection(con);
        }

        return result;
    }

    private class CreationModifyUserDateHolder
    {
        String creationUser = null;
        String modifyUser = null;
        Timestamp creationDate = null;
        Timestamp modifyDate = null;

        CreationModifyUserDateHolder(String creationUser,
                Timestamp creationDate, String modifyUser, Timestamp modifyDate)
        {
            this.creationUser = creationUser;
            this.creationDate = creationDate;
            this.modifyUser = modifyUser;
            this.modifyDate = modifyDate;
        }

        String getCreationUser()
        {
            return this.creationUser;
        }

        Timestamp getCreationDate()
        {
            return this.creationDate;
        }

        String getModifyUser()
        {
            return this.modifyUser;
        }

        Timestamp getModifyDate()
        {
            return this.modifyDate;
        }
    }
}
