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

package com.globalsight.persistence.pageupdate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.globalsight.everest.page.TemplatePart;
import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.everest.tuv.TuImplVo;
import com.globalsight.everest.tuv.TuvImplVo;
import com.globalsight.persistence.PersistenceCommand;
import com.globalsight.util.database.PreparedStatementBatch;

/**
 * Performs an in-place update of existing source page data: modified TUs and
 * TUVs are updated, unmodified but reordered TUs and TUVs are ordered into
 * their new place, and template parts are re-created.
 * 
 */
public class UpdateGxmlPersistenceCommand extends PersistenceCommand
{
    private static Logger s_logger = Logger
            .getLogger(UpdateGxmlPersistenceCommand.class);

    private static final String s_INSERT_TP_NONCLOB = "insert into template_part(id, template_id, order_num, skeleton_string, tu_id) values(null,?,?,?,?)";

    // private static final String s_INSERT_TP_CLOB =
    // "insert into template_part(id, template_id, order_num, skeleton_clob, tu_id) values(?,?,?,empty_clob(),?)";
    private static final String s_INSERT_TP_CLOB = "insert into template_part(id, template_id, order_num, skeleton_clob, tu_id) values(null,?,?,?,?)";

    // private static final String s_SELECT_TP_CLOB =
    // "select * from template_part where id = ? for update ";

    private static final String s_INSERT_TU = "insert into translation_unit values(null,?,?,?,?,?,?,?)";

    private static final String s_UPDATE_TU = "update translation_unit set order_num=?, pid=? where id=?";

    private static final String s_UPDATE_TUV = "update translation_unit_variant set order_num=? where id=?";

    private static final String s_INSERT_TUV_NONCLOB = "insert into Translation_Unit_Variant "
            + "(id, order_num, locale_id, tu_id, is_indexed, segment_string, "
            + " word_count, exact_match_key, state, merge_state, timestamp, "
            + " last_modified) values(null,?,?,?,?,?,?,?,?,?,?,?)";

    // private static final String s_INSERT_TUV_CLOB =
    // "insert into Translation_Unit_Variant " +
    // "(id, order_num, locale_id, tu_id, is_indexed, word_count, " +
    // " exact_match_key, state, merge_state, timestamp, last_modified, " +
    // " segment_clob) values(?,?,?,?,?,?,?,?,?,?,?,empty_clob())";

    private static final String s_INSERT_TUV_CLOB = "insert into Translation_Unit_Variant "
            + "(id, order_num, locale_id, tu_id, is_indexed, word_count, "
            + " exact_match_key, state, merge_state, timestamp, last_modified, "
            + " segment_clob) values(null,?,?,?,?,?,?,?,?,?,?,?)";

    // private static final String s_SELECT_TUV_CLOB =
    // "select * from translation_unit_variant where id = ? for update";

    private static final String s_UPDATE_TUV_NONCLOB = "update translation_unit_variant set segment_string=? where id=?";

    // private static final String s_UPDATE_TUV_CLOB =
    // "update translation_unit_variant set segment_clob=empty_clob() where id=?";
    private static final String s_UPDATE_TUV_CLOB = "update translation_unit_variant set segment_clob=? where id=?";

    //
    // Members
    //

    private ArrayList m_nonClobTemplateParts;
    private ArrayList m_clobTemplateParts;
    private ArrayList m_modifiedTus;
    private ArrayList m_nonClobTuvs;
    private ArrayList m_clobTuvs;
    private ArrayList m_nonClobLocTuvs;
    private ArrayList m_clobLocTuvs;
    private ArrayList m_allTus;
    private ArrayList m_allTuvs;

    private PreparedStatementBatch m_ps_tp1Batch = null;
    private PreparedStatement m_ps_tp2;
    // private PreparedStatement m_ps_tp3;

    private PreparedStatementBatch m_ps_tu1Batch = null;
    private PreparedStatementBatch m_ps_tuv1Batch = null;
    private PreparedStatementBatch m_ps_tuv2Batch = null;
    // private PreparedStatement m_ps_tuv3 = null;

    private PreparedStatementBatch m_ps_tuv3Batch = null;
    private PreparedStatementBatch m_ps_tuv4Batch = null;

    private PreparedStatementBatch m_ps_tuBatch = null;
    private PreparedStatementBatch m_ps_tuvBatch = null;

    private long m_jobId = -1;

    //
    // Constructor
    //

    /**
     * Unmodified TUs get updated (pid+order); Modified TUs get inserted. TUVs
     * and template parts all get inserted.
     * 
     * All IDs (primary keys) must be preassigned.
     */
    public UpdateGxmlPersistenceCommand(ArrayList p_nonClobTemplateParts,
            ArrayList p_clobTemplateParts, ArrayList p_modifiedTus,
            ArrayList p_nonClobTuvs, ArrayList p_clobTuvs,
            ArrayList p_nonClobLocTuvs, ArrayList p_clobLocTuvs,
            ArrayList p_allTus, ArrayList p_allTuvs)
    {
        m_nonClobTemplateParts = p_nonClobTemplateParts;
        m_clobTemplateParts = p_clobTemplateParts;
        m_modifiedTus = p_modifiedTus;
        m_nonClobTuvs = p_nonClobTuvs;
        m_clobTuvs = p_clobTuvs;
        m_nonClobLocTuvs = p_nonClobLocTuvs;
        m_clobLocTuvs = p_clobLocTuvs;
        m_allTus = p_allTus;
        m_allTuvs = p_allTuvs;
    }

    public void setJobId(long jobId)
    {
        m_jobId = jobId;
    }

    //
    // Methods
    //

    public void persistObjects(Connection p_connection)
            throws PersistenceException
    {
        try
        {
            createPreparedStatement(p_connection);
            setData();
            batchStatements();
        }
        catch (Exception sqle)
        {
            throw new PersistenceException(sqle);
        }
        finally
        {
            close(m_ps_tp1Batch);
            close(m_ps_tp2);
            // close(m_ps_tp3);

            close(m_ps_tu1Batch);
            close(m_ps_tuv1Batch);
            close(m_ps_tuv2Batch);
            // close(m_ps_tuv3);
            close(m_ps_tuv3Batch);
            close(m_ps_tuv4Batch);

            close(m_ps_tuBatch);
            close(m_ps_tuvBatch);
        }
    }

    public void createPreparedStatement(Connection p_connection)
            throws Exception
    {
        if (m_nonClobTemplateParts.size() > 0)
        {
            m_ps_tp1Batch = new PreparedStatementBatch(
                    PreparedStatementBatch.DEFAULT_BATCH_SIZE, p_connection,
                    s_INSERT_TP_NONCLOB, true);
        }

        if (m_clobTemplateParts.size() > 0)
        {
            m_ps_tp2 = p_connection.prepareStatement(s_INSERT_TP_CLOB);
            // m_ps_tp3 = p_connection.prepareStatement(s_SELECT_TP_CLOB);
        }

        if (m_modifiedTus.size() > 0)
        {
            m_ps_tu1Batch = new PreparedStatementBatch(
                    PreparedStatementBatch.DEFAULT_BATCH_SIZE, p_connection,
                    s_INSERT_TU, true);
        }

        if (m_nonClobTuvs.size() > 0)
        {
            m_ps_tuv1Batch = new PreparedStatementBatch(
                    PreparedStatementBatch.DEFAULT_BATCH_SIZE, p_connection,
                    s_INSERT_TUV_NONCLOB, true);
        }

        if (m_clobTuvs.size() > 0)
        {
            m_ps_tuv2Batch = new PreparedStatementBatch(
                    PreparedStatementBatch.DEFAULT_BATCH_SIZE, p_connection,
                    s_INSERT_TUV_CLOB, true);

            // m_ps_tuv3 = p_connection.prepareStatement(s_SELECT_TUV_CLOB);
        }

        if (m_nonClobLocTuvs.size() > 0)
        {
            m_ps_tuv3Batch = new PreparedStatementBatch(
                    PreparedStatementBatch.DEFAULT_BATCH_SIZE, p_connection,
                    s_UPDATE_TUV_NONCLOB, true);
        }

        if (m_clobLocTuvs.size() > 0)
        {
            m_ps_tuv4Batch = new PreparedStatementBatch(
                    PreparedStatementBatch.DEFAULT_BATCH_SIZE, p_connection,
                    s_UPDATE_TUV_CLOB, true);
        }

        if (m_allTus.size() > 0)
        {
            m_ps_tuBatch = new PreparedStatementBatch(
                    PreparedStatementBatch.DEFAULT_BATCH_SIZE, p_connection,
                    s_UPDATE_TU, true);
        }

        if (m_allTuvs.size() > 0)
        {
            m_ps_tuvBatch = new PreparedStatementBatch(
                    PreparedStatementBatch.DEFAULT_BATCH_SIZE, p_connection,
                    s_UPDATE_TUV, true);
        }
    }

    public void setData() throws Exception
    {
        Timestamp now = new Timestamp(System.currentTimeMillis());

        if (m_nonClobTemplateParts.size() > 0)
        {
            // get the first prepared statement for non-clob parts
            for (int i = 0, max = m_nonClobTemplateParts.size(); i < max; i++)
            {
                TemplatePart tp = (TemplatePart) m_nonClobTemplateParts.get(i);

                // insert into template_part(id, template_id, order_num,
                // skeleton_string, tu_id) values(?,?,?,?,?)
                PreparedStatement ps = m_ps_tp1Batch.getNextPreparedStatement();

                // ps.setLong(1, tp.getId());
                ps.setLong(1, tp.getTemplateId());
                ps.setInt(2, tp.getOrder());
                ps.setString(3, tp.getSkeleton());
                if (tp.getTuId() == TemplatePart.INVALID_TUID)
                {
                    ps.setNull(4, Types.NUMERIC);
                }
                else
                {
                    ps.setLong(4, tp.getTuId());
                }

                ps.addBatch();

                if (s_logger.isDebugEnabled())
                {
                    String s = s_INSERT_TP_NONCLOB;
                    // s = s.replaceFirst("\\?", String.valueOf(tp.getId()));
                    s = s.replaceFirst("\\?",
                            String.valueOf(tp.getTemplateId()));
                    s = s.replaceFirst("\\?", String.valueOf(tp.getOrder()));
                    s = s.replaceFirst("\\?", "'" + tp.getSkeleton() + "'");
                    s = s.replaceFirst("\\?", String.valueOf(tp.getTuId()));
                    System.err.println(s);
                }
            }
        }

        if (m_clobTemplateParts.size() > 0)
        {
            for (int i = 0, max = m_clobTemplateParts.size(); i < max; i++)
            {
                TemplatePart tp = (TemplatePart) m_clobTemplateParts.get(i);

                // insert into template_part(id, template_id, order_num,
                // skeleton_clob, tu_id) values(?,?,?,empty_clob(),?)
                // m_ps_tp2.setLong(1, tp.getId());
                m_ps_tp2.setLong(1, tp.getTemplateId());
                m_ps_tp2.setInt(2, tp.getOrder());
                m_ps_tp2.setString(3, tp.getSkeletonClob());
                if (tp.getTuId() == TemplatePart.INVALID_TUID)
                {
                    m_ps_tp2.setNull(4, Types.NUMERIC);
                }
                else
                {
                    m_ps_tp2.setLong(4, tp.getTuId());
                }

                m_ps_tp2.addBatch();

                if (s_logger.isDebugEnabled())
                {
                    String s = s_INSERT_TP_CLOB;
                    // s = s.replaceFirst("\\?", String.valueOf(tp.getId()));
                    s = s.replaceFirst("\\?",
                            String.valueOf(tp.getTemplateId()));
                    s = s.replaceFirst("\\?", String.valueOf(tp.getOrder()));
                    s = s.replaceFirst("\\?", String.valueOf(tp.getTuId()));
                    s = s + " - CLOB=" + tp.getSkeleton();
                    System.err.println(s);
                }
            }
        }

        if (m_modifiedTus.size() > 0)
        {
            for (int i = 0, max = m_modifiedTus.size(); i < max; i++)
            {
                TuImplVo tu = (TuImplVo) m_modifiedTus.get(i);

                // insert into translation_unit values(?,?,?,?,?,?,?,?)
                PreparedStatement ps = m_ps_tu1Batch.getNextPreparedStatement();

                // ps.setLong(1, tu.getId());
                ps.setLong(1, tu.getOrder());
                ps.setLong(2, tu.getTmId());
                ps.setString(3, tu.getDataType());
                ps.setString(4, tu.getTuType());
                ps.setString(5, String.valueOf(tu.getLocalizableType()));
                ps.setLong(6, tu.getLeverageGroupId());
                ps.setLong(7, tu.getPid());

                ps.addBatch();

                if (s_logger.isDebugEnabled())
                {
                    String s = s_INSERT_TU;
                    // s = s.replaceFirst("\\?", String.valueOf(tu.getId()));
                    s = s.replaceFirst("\\?",
                            "ordernum=" + String.valueOf(tu.getOrder()));
                    s = s.replaceFirst("\\?",
                            "tmid=" + String.valueOf(tu.getTmId()));
                    s = s.replaceFirst("\\?", String.valueOf(tu.getDataType()));
                    s = s.replaceFirst("\\?", String.valueOf(tu.getTuType()));
                    s = s.replaceFirst("\\?",
                            String.valueOf(tu.getLocalizableType()));
                    s = s.replaceFirst("\\?",
                            "lgid=" + String.valueOf(tu.getLeverageGroupId()));
                    s = s.replaceFirst("\\?",
                            "pid=" + String.valueOf(tu.getPid()));
                    System.err.println(s);
                }
            }
        }

        if (m_nonClobTuvs.size() > 0)
        {
            for (int i = 0, max = m_nonClobTuvs.size(); i < max; i++)
            {
                TuvImplVo tuv = (TuvImplVo) m_nonClobTuvs.get(i);

                // insert into Translation_Unit_Variant ....
                PreparedStatement ps = m_ps_tuv1Batch
                        .getNextPreparedStatement();

                // ps.setLong (1, tuv.getId());
                ps.setLong(1, tuv.getOrder());
                ps.setLong(2, tuv.getLocaleId());
                ps.setLong(3, tuv.getTu(m_jobId).getId());
                ps.setString(4, "N");
                ps.setString(5, tuv.getGxml());
                ps.setLong(6, tuv.getWordCount());
                ps.setLong(7, tuv.getExactMatchKey());
                ps.setString(8, tuv.getStateName());
                ps.setString(9, tuv.getMergeState());
                ps.setTimestamp(10, now);
                ps.setTimestamp(11, now);

                ps.addBatch();

                if (s_logger.isDebugEnabled())
                {
                    String s = s_INSERT_TUV_NONCLOB;
                    // s = s.replaceFirst("\\?", String.valueOf(tuv.getId()));
                    s = s.replaceFirst("\\?",
                            "order=" + String.valueOf(tuv.getOrder()));
                    s = s.replaceFirst("\\?",
                            "locale=" + String.valueOf(tuv.getLocaleId()));
                    s = s.replaceFirst(
                            "\\?",
                            "tuid="
                                    + String.valueOf(tuv.getTu(m_jobId)
                                            .getId()));
                    s = s.replaceFirst("\\?", "N");
                    s = s.replaceFirst("\\?", "'" + tuv.getGxml() + "'");
                    s = s.replaceFirst("\\?",
                            "wordcount=" + String.valueOf(tuv.getWordCount()));
                    s = s.replaceFirst("\\?",
                            "crc=" + String.valueOf(tuv.getExactMatchKey()));
                    s = s.replaceFirst("\\?", tuv.getStateName());
                    s = s.replaceFirst("\\?", tuv.getMergeState());
                    System.err.println(s);
                }
            }
        }

        if (m_clobTuvs.size() > 0)
        {
            for (int i = 0, max = m_clobTuvs.size(); i < max; i++)
            {
                TuvImplVo tuv = (TuvImplVo) m_clobTuvs.get(i);

                // insert into Translation_Unit_Variant ....
                PreparedStatement ps = m_ps_tuv2Batch
                        .getNextPreparedStatement();

                // ps.setLong (1, tuv.getId());
                ps.setLong(1, tuv.getOrder());
                ps.setLong(2, tuv.getLocaleId());
                ps.setLong(3, tuv.getTu(m_jobId).getId());
                ps.setString(4, "N");
                ps.setLong(5, tuv.getWordCount());
                ps.setLong(6, tuv.getExactMatchKey());
                ps.setString(7, tuv.getStateName());
                ps.setString(8, tuv.getMergeState());
                ps.setTimestamp(9, now);
                ps.setTimestamp(10, now);
                ps.setString(11, tuv.getGxml());

                ps.addBatch();

                if (s_logger.isDebugEnabled())
                {
                    String s = s_INSERT_TUV_CLOB;
                    // s = s.replaceFirst("\\?", String.valueOf(tuv.getId()));
                    s = s.replaceFirst("\\?", String.valueOf(tuv.getOrder()));
                    s = s.replaceFirst("\\?", String.valueOf(tuv.getLocaleId()));
                    s = s.replaceFirst("\\?",
                            String.valueOf(tuv.getTu(m_jobId).getId()));
                    s = s.replaceFirst("\\?", "N");
                    s = s.replaceFirst("\\?",
                            String.valueOf(tuv.getWordCount()));
                    s = s.replaceFirst("\\?",
                            String.valueOf(tuv.getExactMatchKey()));
                    s = s.replaceFirst("\\?", tuv.getStateName());
                    s = s.replaceFirst("\\?", tuv.getMergeState());
                    s = s + " - CLOB=" + tuv.getGxml();

                    System.err.println(s);
                }
            }
        }

        if (m_nonClobLocTuvs.size() > 0)
        {
            for (int i = 0, max = m_nonClobLocTuvs.size(); i < max; i++)
            {
                TuvImplVo tuv = (TuvImplVo) m_nonClobLocTuvs.get(i);

                // update tuv set segment_string=? where id=?
                PreparedStatement ps = m_ps_tuv3Batch
                        .getNextPreparedStatement();

                ps.setString(1, tuv.getGxml());
                ps.setLong(2, tuv.getId());

                ps.addBatch();

                if (s_logger.isDebugEnabled())
                {
                    String s = s_UPDATE_TUV_NONCLOB;
                    s = s.replaceFirst("\\?", tuv.getGxml());
                    s = s.replaceFirst("\\?", String.valueOf(tuv.getId()));
                    System.err.println(s);
                }
            }
        }

        if (m_clobLocTuvs.size() > 0)
        {
            for (int i = 0, max = m_clobLocTuvs.size(); i < max; i++)
            {
                TuvImplVo tuv = (TuvImplVo) m_clobLocTuvs.get(i);

                // update tuv set segment_clob=empty_clob() where id=?
                PreparedStatement ps = m_ps_tuv4Batch
                        .getNextPreparedStatement();

                ps.setString(1, tuv.getGxml());
                ps.setLong(2, tuv.getId());
                ps.addBatch();

                if (s_logger.isDebugEnabled())
                {
                    String s = s_INSERT_TUV_CLOB;
                    s = s.replaceFirst("\\?", String.valueOf(tuv.getId()));
                    s = s + " - CLOB=" + tuv.getGxml();

                    System.err.println(s);
                }
            }
        }

        if (m_allTus.size() > 0)
        {
            for (int i = 0, max = m_allTus.size(); i < max; i++)
            {
                TuImplVo tu = (TuImplVo) m_allTus.get(i);

                // update translation_unit set order_num=?, pid=? where id=?
                PreparedStatement ps = m_ps_tuBatch.getNextPreparedStatement();

                ps.setLong(1, tu.getOrder());
                ps.setLong(2, tu.getPid());
                ps.setLong(3, tu.getId());

                ps.addBatch();

                if (s_logger.isDebugEnabled())
                {
                    String s = s_UPDATE_TU;
                    s = s.replaceFirst("\\?", String.valueOf(tu.getOrder()));
                    s = s.replaceFirst("\\?", String.valueOf(tu.getPid()));
                    s = s.replaceFirst("\\?", String.valueOf(tu.getId()));
                    System.err.println(s);
                }
            }
        }

        if (m_allTuvs.size() > 0)
        {
            for (int i = 0, max = m_allTuvs.size(); i < max; i++)
            {
                TuvImplVo tuv = (TuvImplVo) m_allTuvs.get(i);

                // update translation_unit_variant set order_num=?, where id=?
                PreparedStatement ps = m_ps_tuvBatch.getNextPreparedStatement();

                ps.setLong(1, tuv.getOrder());
                ps.setLong(2, tuv.getId());

                ps.addBatch();

                if (s_logger.isDebugEnabled())
                {
                    String s = s_UPDATE_TUV;
                    s = s.replaceFirst("\\?", String.valueOf(tuv.getOrder()));
                    s = s.replaceFirst("\\?", String.valueOf(tuv.getId()));
                    System.err.println(s);
                }
            }
        }
    }

    public void batchStatements() throws Exception
    {
        if (m_modifiedTus.size() > 0)
        {
            m_ps_tu1Batch.executeBatches();
        }

        if (m_nonClobTuvs.size() > 0)
        {
            m_ps_tuv1Batch.executeBatches();
        }

        if (m_clobTuvs.size() > 0)
        {
            m_ps_tuv2Batch.executeBatches();

            // Clob clob = null;
            // CLOB oclob;
            // Writer writer = null;
            // ResultSet rs = null;
            //
            // try
            // {
            // for (int i = 0, max = m_clobTuvs.size(); i < max; i++)
            // {
            // TuvImplVo tuv = (TuvImplVo)m_clobTuvs.get(i);
            //
            // m_ps_tuv3.setLong(1, tuv.getId());
            // rs = m_ps_tuv3.executeQuery();
            //
            // if (rs.next())
            // {
            // clob = rs.getClob(6);
            // oclob = (CLOB)clob;
            //
            // writer = oclob.getCharacterOutputStream();
            // StringReader sr = new StringReader(tuv.getGxml());
            // char[] buffer = new char[oclob.getChunkSize()];
            // int charsRead = 0;
            // while ((charsRead = sr.read(buffer)) != -1)
            // {
            // writer.write(buffer, 0, charsRead);
            // }
            // close(writer);
            // writer = null;
            // }
            // close(rs);
            // rs = null;
            // }
            // }
            // finally
            // {
            // close(writer);
            // close(rs);
            // }
        }

        if (m_nonClobLocTuvs.size() > 0)
        {
            m_ps_tuv3Batch.executeBatches();
        }

        if (m_clobLocTuvs.size() > 0)
        {
            m_ps_tuv4Batch.executeBatches();

            // Clob clob = null;
            // CLOB oclob;
            // Writer writer = null;
            // ResultSet rs = null;
            //
            // try
            // {
            // for (int i = 0, max = m_clobLocTuvs.size(); i < max; i++)
            // {
            // TuvImplVo tuv = (TuvImplVo)m_clobLocTuvs.get(i);
            //
            // m_ps_tuv3.setLong(1, tuv.getId());
            // rs = m_ps_tuv3.executeQuery();
            //
            // if (rs.next())
            // {
            // clob = rs.getClob(6);
            // oclob = (CLOB)clob;
            //
            // writer = oclob.getCharacterOutputStream();
            // StringReader sr = new StringReader(tuv.getGxml());
            // char[] buffer = new char[oclob.getChunkSize()];
            // int charsRead = 0;
            // while ((charsRead = sr.read(buffer)) != -1)
            // {
            // writer.write(buffer, 0, charsRead);
            // }
            // close(writer);
            // writer = null;
            // }
            // close(rs);
            // rs = null;
            // }
            // }
            // finally
            // {
            // close(writer);
            // close(rs);
            // }
        }

        // After all new objects have been inserted, update TU/TUV orders

        if (m_allTus.size() > 0)
        {
            m_ps_tuBatch.executeBatches();
        }

        if (m_allTuvs.size() > 0)
        {
            m_ps_tuvBatch.executeBatches();
        }

        // Template Parts depend on the TUs being written first.

        if (m_nonClobTemplateParts.size() > 0)
        {
            m_ps_tp1Batch.executeBatches();
        }

        if (m_clobTemplateParts.size() > 0)
        {
            m_ps_tp2.executeBatch();

            // Clob clob = null;
            // CLOB oclob;
            // Writer writer = null;
            // ResultSet rs = null;
            //
            // try
            // {
            // for (int i = 0, max = m_clobTemplateParts.size(); i < max; i++)
            // {
            // TemplatePart tp = (TemplatePart)m_clobTemplateParts.get(i);
            //
            // m_ps_tp3.setLong(1, tp.getId());
            // rs = m_ps_tp3.executeQuery();
            // if (rs.next())
            // {
            // clob = rs.getClob(4);
            //
            // oclob = (CLOB)clob;
            // writer = oclob.getCharacterOutputStream();
            // StringReader sr = new StringReader(tp.getSkeletonClob());
            // char[] buffer = new char[oclob.getChunkSize()];
            // int charsRead = 0;
            // while ((charsRead = sr.read(buffer)) != -1)
            // {
            // writer.write(buffer, 0, charsRead);
            // }
            // close(writer);
            // writer = null;
            // }
            // close(rs);
            // rs = null;
            // }
            // }
            // finally
            // {
            // close(writer);
            // close(rs);
            // }
        }
    }
}
