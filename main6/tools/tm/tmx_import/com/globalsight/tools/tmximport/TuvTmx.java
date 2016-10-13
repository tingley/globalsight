/*
 * Copyright (c) 2000 GlobalSight Corporation. All rights reserved.
 *
 * THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF
 * GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
 * IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
 * OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
 * AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.
 *
 * THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
 * SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
 * UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
 * BY LAW.
 */

package com.globalsight.tools.tmximport;

import com.globalsight.everest.tuv.Tu;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.everest.tuv.TuvState;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.ling.util.GlobalSightCrc;
import com.globalsight.ling.common.Text;
import com.globalsight.util.edit.EditUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;

import java.io.StringReader;
import java.io.Writer;

import oracle.sql.CLOB;

/**
 * This class is responsible for creating Tuv
 */
public class TuvTmx
{
    private static final int EOF = -1;
//    private static final int CLOB_THRESHOLD = 4000;
    private static final String SEQUENCE_QUERY
        = "SELECT count FROM sequence WHERE name = 'TUV_SEQ'";

    private static final String TUV_INSERT_NON_CLOB =
        "INSERT INTO translation_unit_variant(id, order_num, locale_id,"
        + " tu_id, is_indexed, segment_string, word_count, exact_match_key,"
        + " state, timestamp, last_modified)"
        + " VALUES(?, 0, ?, ?, 'N', ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
//    private static final String TUV_INSERT_NON_CLOB =
//        "INSERT INTO translation_unit_variant(id, order_num, locale_id,"
//        + " tu_id, is_indexed, segment_string, word_count, exact_match_key,"
//        + " state, timestamp, last_modified)"
//        + " VALUES(?, 0, ?, ?, 'N', ?, ?, ?, ?, SYSDATE, SYSDATE)";

//    private static final String TUV_INSERT_WITH_CLOB =
//        "INSERT INTO translation_unit_variant(id, order_num, locale_id,"
//        + " tu_id, is_indexed, segment_clob, word_count, exact_match_key,"
//        + " state, timestamp, last_modified)"
//        + " VALUES(?, 0, ?, ?, 'N', empty_clob(), ?, ?,"
//        + " ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
//    private static final String TUV_INSERT_WITH_CLOB =
//        "INSERT INTO translation_unit_variant(id, order_num, locale_id,"
//        + " tu_id, is_indexed, segment_clob, word_count, exact_match_key,"
//        + " state, timestamp, last_modified)"
//        + " VALUES(?, 0, ?, ?, 'N', empty_clob(), ?, ?,"
//        + " ?, SYSDATE, SYSDATE)";

    private static final String TUV_SELECT_CLOB =
        "SELECT segment_clob FROM translation_unit_variant WHERE"
        + " id = ? FOR UPDATE";

    private PreparedStatement m_sequenceStatement = null;
    private PreparedStatement m_tuvInsertNonClob = null;
    private PreparedStatement m_tuvInsertWithClob = null;
    private PreparedStatement m_tuvSelectClob = null;
    
    public TuvTmx(Connection p_connection)
        throws Exception
    {
        m_sequenceStatement = p_connection.prepareStatement(SEQUENCE_QUERY,
            ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
        m_tuvInsertNonClob
            = p_connection.prepareStatement(TUV_INSERT_NON_CLOB);
//        m_tuvInsertWithClob
//            = p_connection.prepareStatement(TUV_INSERT_WITH_CLOB);
        m_tuvSelectClob
            = p_connection.prepareStatement(TUV_SELECT_CLOB);
    }

    /**
     * Create a Tuv
     *
     * @param p_locale GlobalSightLocale 
     * @param p_tu Tu that associate with this Tuv
     * @return Tuv object
     */
    public TuvImpl create(GlobalSightLocale p_locale, Tu p_tu)
        throws Exception
    {
        TuvImpl tuv = new TuvImpl(p_locale, p_tu);
        setTuvId(tuv);
        return tuv;
    }


    /**
     * Set segment string.
     *
     * @param p_tuv tuv object
     * @param p_text text to set
     */
    public void setSegment(TuvImpl p_tuv, String p_text)
        throws Exception
    {
        // normalize whitespaces if the data type is HTML
        if(p_tuv.getTu().getDataType().equals("html"))
        {
            p_text = Text.normalizeWhiteSpaces(p_text);
        }
        p_tuv.setGxml(p_text);
        p_tuv.setState(TuvState.ALIGNMENT_LOCALIZED);

        // generate exact match key
        long crc = GlobalSightCrc.calculate(p_tuv.getExactMatchFormat());
        p_tuv.setExactMatchKey(crc);
    }
    

        
    public void save(TuvImpl p_tuv)
        throws Exception
    {
//        if(EditUtil.getUTF8Len(p_tuv.getGxml()) > CLOB_THRESHOLD)
//        {
//            // segment is in CLOB
//            m_tuvInsertWithClob.setLong(1, p_tuv.getId());
//            m_tuvInsertWithClob.setLong(2, p_tuv.getLocaleId());
//            m_tuvInsertWithClob.setLong(3, p_tuv.getTu().getId());
//            m_tuvInsertWithClob.setInt(4, p_tuv.getWordCount());
//            m_tuvInsertWithClob.setLong(5, p_tuv.getExactMatchKey());
//            m_tuvInsertWithClob.setString(6, p_tuv.getState().getName());
//            m_tuvInsertWithClob.executeUpdate();
//            // write segment to CLOB
//            writeClob(p_tuv);
//        }
//        else
//        {
            m_tuvInsertNonClob.setLong(1, p_tuv.getId());
            m_tuvInsertNonClob.setLong(2, p_tuv.getLocaleId());
            m_tuvInsertNonClob.setLong(3, p_tuv.getTu().getId());
            m_tuvInsertNonClob.setString(4, p_tuv.getGxml());
            m_tuvInsertNonClob.setInt(5, p_tuv.getWordCount());
            m_tuvInsertNonClob.setLong(6, p_tuv.getExactMatchKey());
            m_tuvInsertNonClob.setString(7, p_tuv.getState().getName());
            m_tuvInsertNonClob.executeUpdate();
//        }
            
    }


    private void setTuvId(TuvImpl p_tuv)
        throws Exception
    {
        // increase TUV_SEQ by one
        ResultSet rs = m_sequenceStatement.executeQuery();
        rs.next();
        long id = rs.getLong("count");
        rs.updateLong("count", ++id);
        rs.updateRow();
        rs.close();
        p_tuv.setId(id);
    }


//    private void writeClob(TuvImpl p_tuv)
//        throws Exception
//    {
//        CLOB oclob;
//        Writer writer = null;
//        ResultSet rs = null;
//        
//        try
//        {
//            m_tuvSelectClob.setLong(1, p_tuv.getId());
//            rs = m_tuvSelectClob.executeQuery();
//            rs.next();
//            oclob = (CLOB)rs.getClob(1);
//
//            writer = oclob.getCharacterOutputStream();
//            StringReader sr = new StringReader(p_tuv.getGxml());
//            char[] buffer = new char[oclob.getChunkSize()];
//            int charsRead = 0;
//            while ((charsRead = sr.read(buffer)) != EOF)
//            {
//                writer.write(buffer, 0, charsRead);
//            }
//
//            writer.flush();
//        }
//        finally
//        {
//            writer.close();
//            rs.close();
//        }
//    }
}
