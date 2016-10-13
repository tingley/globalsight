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

import com.globalsight.util.GlobalSightLocale;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.Iterator;

/**
 * PageTmRetriever class is responsible for retrieving data from Page TM
 */

public class PageTmRetriever
    implements TuRetriever
{
    private Connection m_connection = null;
    private PreparedStatement m_statementTr = null;
    private PreparedStatement m_statementLo = null;

    private long m_tmId;
    private GlobalSightLocale m_sourceLocale;

    static private final String SELECT_SEGMENT_T = "SELECT tu_t.id, tu_t.format,"
            + " tu_t.type, tuv_t.segment_string, tuv_t.segment_clob, "
            + "tuv_t.exact_match_key "
            + "FROM  page_tm_tuv_t tuv_t, page_tm_tu_t tu_t WHERE tu_t.tm_id = ? "
            + "AND tu_t.id = tuv_t.tu_id AND tuv_t.locale_id = ? "
            + "ORDER BY tu_t.id";

    static private final String SELECT_SEGMENT_L = "SELECT tu_l.id, tu_l.format,"
            + " tu_l.type, tuv_l.segment_string, tuv_l.segment_clob, "
            + "tuv_l.exact_match_key "
            + "FROM  page_tm_tuv_l tuv_l, page_tm_tu_l tu_l WHERE tu_l.tm_id = ? "
            + "AND tu_l.id = tuv_l.tu_id AND tuv_l.locale_id = ? "
            + "ORDER BY tu_l.id";
    
    /**
     * Constructor
     *
     * @param p_tmId Page Tm id to get data from 
     * @param p_sourceLocale source locale (GlobalSightLocale)
     */
    public PageTmRetriever(
        Connection p_connection, long p_tmId, GlobalSightLocale p_sourceLocale)
        throws Exception
    {
        m_connection = p_connection;
        m_statementTr = null;
        m_statementLo = null;
        m_tmId = p_tmId;
        m_sourceLocale = p_sourceLocale;
    }
    

    /**
     * Get source segments in a specified Page Tm
     */
    public SegmentQueryResult query()
        throws Exception
    {
        // get translatable segments
        String sql = createSelectSql(true);
        m_statementTr = m_connection.prepareStatement(sql);
        m_statementTr.setLong(1, m_tmId);
        m_statementTr.setLong(2, m_sourceLocale.getId());
        ResultSet rsTr = m_statementTr.executeQuery();

        // get localizable segments
        sql = createSelectSql(false);
        PreparedStatement m_statementLo = m_connection.prepareStatement(sql);
        m_statementLo.setLong(1, m_tmId);
        m_statementLo.setLong(2, m_sourceLocale.getId());
        ResultSet rsLo = m_statementLo.executeQuery();

        return new PageTmQueryResult(rsTr, rsLo, m_sourceLocale);
    }
    
        
    public void close()
        throws Exception
    {
        if(m_statementTr != null)
        {
            m_statementTr.close();
        }
        
        if(m_statementLo != null)
        {
            m_statementLo.close();
        }
    }

    // create a SELECT sql string
    private String createSelectSql(boolean p_translatable)
    {
        return p_translatable? SELECT_SEGMENT_T : SELECT_SEGMENT_L;
    }

}
