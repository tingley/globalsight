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

package com.globalsight.migration.system4;

import com.globalsight.everest.tuv.Tu;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.everest.tuv.TuvState;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.everest.persistence.PersistenceService;
import com.globalsight.ling.util.GlobalSightCrc;
import com.globalsight.ling.common.Text;

import com.globalsight.migration.system3.System3Segment;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;


/**
 * This class is responsible for creating and deleting Tuv
 */
public class MigrationTuv
{
    private static final String TRANSLATABLE_START = "<segment>";
    private static final String TRANSLATABLE_END   = "</segment>";
    private static final String LOCALIZABLE_START  = "<localizable>";
    private static final String LOCALIZABLE_END    = "</localizable>";
    

    private static final String SEQUENCE_QUERY
        = "SELECT count FROM sequence WHERE name = 'TUV_SEQ'";

    // We know there is no segment that exceeds 4000 characters in System3 data
    private static final String TUV_INSERT
        = "INSERT INTO translation_unit_variant VALUES (?, 0, ?, ?, 'N', NULL, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
//    private static final String TUV_INSERT
//    = "INSERT INTO translation_unit_variant VALUES (?, 0, ?, ?, 'N', NULL, ?, ?, ?, ?, SYSDATE)";
    
    private PreparedStatement m_sequenceStatement = null;
    private PreparedStatement m_tuvStatement = null;
    
    public MigrationTuv(Connection p_connection)
        throws Exception
    {
        m_sequenceStatement = p_connection.prepareStatement(SEQUENCE_QUERY,
            ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
        m_tuvStatement = p_connection.prepareStatement(TUV_INSERT);
    }

    /**
     * Create a Tuv
     * @param p_text text
     * @param p_isTranslatable true if the segment is translatable
     * @param p_locale GlobalSightLocale 
     * @param p_tu Tu that associate with this Tuv
     * @return Tuv object
     */
    public Tuv create(String p_text, boolean p_isTranslatable,
        GlobalSightLocale p_locale, Tu p_tu)
        throws Exception
    {
        TuvImpl tuv = new TuvImpl(p_locale, p_tu);
        // normalize whitespaces if the data type is HTML
        if(p_tu.getDataType().equals("html"))
        {
            p_text = Text.normalizeWhiteSpaces(p_text);
        }
        tuv.setGxml(getGxml(p_text, p_isTranslatable));
        tuv.setState(TuvState.ALIGNMENT_LOCALIZED);

        // generate exact match key
        long crc = GlobalSightCrc.calculate(tuv.getExactMatchFormat());
        tuv.setExactMatchKey(crc);

        storeTuvToDatabase(tuv);

        // Toplink code
//          PersistenceService persistence = PersistenceService.getInstance();
//          persistence.insertObject(tuv);
        return tuv;

    }



    // wrap the text with XML tags
    private static String getGxml(String p_text, boolean p_isTranslatable)
    {
        String start
            = p_isTranslatable ? TRANSLATABLE_START : LOCALIZABLE_START;
        String end
            = p_isTranslatable ? TRANSLATABLE_END : LOCALIZABLE_END;
        return start + p_text + end;
    }



    private void storeTuvToDatabase(TuvImpl p_tuv)
        throws Exception
    {
        // increase TU_SEQ by one
        ResultSet rs = m_sequenceStatement.executeQuery();
        rs.next();
        long id = rs.getLong("count");
        rs.updateLong("count", ++id);
        rs.updateRow();
        rs.close();
        
        m_tuvStatement.setLong(1, id);
        m_tuvStatement.setLong(2, p_tuv.getLocaleId());
        m_tuvStatement.setLong(3, p_tuv.getTu().getId());
        m_tuvStatement.setString(4, p_tuv.getGxml());
        m_tuvStatement.setInt(5, p_tuv.getWordCount());
        m_tuvStatement.setLong(6, p_tuv.getExactMatchKey());
        m_tuvStatement.setString(7, p_tuv.getState().getName());
        
        m_tuvStatement.executeUpdate();
        p_tuv.setId(id);
    }
    


}
