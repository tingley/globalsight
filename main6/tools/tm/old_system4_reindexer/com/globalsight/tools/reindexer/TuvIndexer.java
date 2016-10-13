/*
Copyright (c) 2000-2001 GlobalSight Corporation. All rights reserved.

THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF
GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.

THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
BY LAW.
*/
package com.globalsight.tools.reindexer;

import com.globalsight.util.GlobalSightLocale;
import com.globalsight.ling.docproc.GlobalsightBreakIterator;
import com.globalsight.ling.docproc.GlobalsightRuleBasedBreakIterator;
import com.globalsight.ling.tm.fuzzy.FuzzyIndexManager;
import com.globalsight.everest.integration.ling.tm.FuzzyIndexManagerLocal;
import com.globalsight.ling.tm.fuzzy.FuzzyIndexer;

import java.util.Vector;
import java.util.List;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

/**
 * 
 */
public class TuvIndexer
{
    private GlobalSightLocale m_srcLocale = null;
    private GlobalsightBreakIterator m_breakIterator = null;
    private FuzzyIndexManager m_fuzzyIndexManager
        = new FuzzyIndexManagerLocal();
    private FuzzyIndexer m_fuzzyIndexer = new FuzzyIndexer();
    private boolean m_dropFuzzyIndex;
    private PreparedStatement m_deleteIndexStatement = null;
    private Connection m_connection = null;
    
    private static final String DROP_FUZZY_INDEX_INDEX
        = "DROP INDEX idx_fuzzy_loc_crc_tuv_tkcnt";
    private static final String DROP_FUZZY_INDEX_SEQUENCE
        = "DROP sequence seq_fuzzy_index";
    private static final String DROP_FUZZY_INDEX_TABLE
        = "drop table fuzzy_index";


    private static final String CREATE_FUZZY_INDEX_TABLE
        = "create table fuzzy_index("
        + "id integer constraint fuzzy_index_id_pk primary key, "
        + "tuv_id integer constraint fuzzy_index_tuv_id_nn not null "
        + "constraint fuzzy_index_tuv_id_fk references translation_unit_variant, "
        + "locale_id integer constraint fuzzy_index_locale_id_nn not null "
        + "constraint fuzzy_index_locale_id_fk references locale, "
        + "tm_id integer constraint fuzzy_index_tm_id_nn not null "
        + "constraint fuzzy_index_tm_id_fk references translation_memory, "
        + "token_crc integer constraint fuzzy_index_token_crc_nn not null, "
        + "token_count smallint)";
    private static final String CREATE_FUZZY_INDEX_SEQUENCE
        = "CREATE sequence seq_fuzzy_index start with 100";
    private static final String CREATE_FUZZY_INDEX_INDEX
        = "CREATE INDEX idx_fuzzy_loc_crc_tuv_tkcnt ON FUZZY_INDEX "
        + "(LOCALE_ID, TOKEN_CRC, TUV_ID, TOKEN_COUNT) "
        + "NOLOGGING  INITRANS 2  MAXTRANS 255  TABLESPACE indx "
        + "PCTFREE 10 STORAGE (INITIAL 1M  NEXT 1M  PCTINCREASE 0 "
        + "MINEXTENTS 1  MAXEXTENTS 1024)";
    
    private static final String DELETE_INDICES
        = "delete from fuzzy_index where tuv_id = ?";
    
    public TuvIndexer(Connection p_connection,
        GlobalSightLocale p_srcLocale, boolean p_dropFuzzyIndex)
        throws Exception
    {
        m_connection = p_connection;
        m_deleteIndexStatement = m_connection.prepareStatement(DELETE_INDICES);

        m_srcLocale = p_srcLocale;
        m_dropFuzzyIndex = p_dropFuzzyIndex;
        
        m_breakIterator = GlobalsightRuleBasedBreakIterator
            .getWordInstance(m_srcLocale.getLocale());

    }


    public void index(String p_segmentString, long p_tuvId, long p_tmId)
        throws Exception
    {
        // if fuzzy_index table is not dropped, indices for p_tuvId
        // must be deleted
        if(!m_dropFuzzyIndex)
        {
            m_deleteIndexStatement.setLong(1, p_tuvId);
            m_deleteIndexStatement.executeUpdate();
        }
        
        // generate a list of parameters for each tuv
        List list = m_fuzzyIndexer.makeParameterList(p_segmentString,
            p_tuvId, m_srcLocale, p_tmId, m_breakIterator);
        
        // save all the tokens
        m_fuzzyIndexManager.callIndexingProcedure(new Vector(list));
    }
    

    public void recreateFuzzyIndexTable()
        throws Exception
    {
        Statement stmt = m_connection.createStatement();
        stmt.executeUpdate(DROP_FUZZY_INDEX_INDEX);
        stmt.executeUpdate(DROP_FUZZY_INDEX_SEQUENCE);
        stmt.executeUpdate(DROP_FUZZY_INDEX_TABLE);
        stmt.executeUpdate(CREATE_FUZZY_INDEX_TABLE);
        stmt.executeUpdate(CREATE_FUZZY_INDEX_SEQUENCE);
        stmt.executeUpdate(CREATE_FUZZY_INDEX_INDEX);
        stmt.close();
    }
    
}
