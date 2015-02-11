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
package com.globalsight.tools.normalizetuv;

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
    private PreparedStatement m_deleteIndexStatement = null;
    
    private static final String DELETE_INDICES
        = "delete from fuzzy_index where tuv_id = ?";
    
    public TuvIndexer(Connection p_connection, GlobalSightLocale p_srcLocale)
        throws Exception
    {
        m_deleteIndexStatement = p_connection.prepareStatement(DELETE_INDICES);

        m_srcLocale = p_srcLocale;
        
        m_breakIterator = GlobalsightRuleBasedBreakIterator
            .getWordInstance(m_srcLocale.getLocale());
    }


    public void index(String p_segmentString, long p_tuvId, long p_tmId)
        throws Exception
    {
        m_deleteIndexStatement.setLong(1, p_tuvId);
        m_deleteIndexStatement.executeUpdate();
        
        // generate a list of parameters for each tuv
        List list = m_fuzzyIndexer.makeParameterList(p_segmentString,
            p_tuvId, m_srcLocale, p_tmId, m_breakIterator);
        
        // save all the tokens
        m_fuzzyIndexManager.callIndexingProcedure(new Vector(list));
    }
    

}
