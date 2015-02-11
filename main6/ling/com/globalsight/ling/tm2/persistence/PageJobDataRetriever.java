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
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.util.Set;
import java.util.Collection;
import java.util.ArrayList;

import com.globalsight.util.GlobalSightLocale;
import com.globalsight.ling.tm2.BaseTmTu;

/**
 * PageJobDataRetriever is responsible to retrieve page data from
 * translation_unit_variant and translation_unit tables.
 */

public class PageJobDataRetriever
    implements TuRetriever
{
    private Connection m_connection = null;
    private PreparedStatement m_statement = null;

    private long m_sourcePageId;
    private GlobalSightLocale m_sourceLocale;
    
    // If the list is changed, getter methods in
    // PageJobDataQueryResult must be changed to sync with this list.
    static private final String SELECT_LIST
        = "SELECT tu.id, tu.tm_id, tu.data_type, tu.tu_type, "
        + "tu.localize_type, tuv.id, tuv.segment_string, "
        + "tuv.segment_clob, tuv.locale_id, tuv.state, tuv.merge_state, "
        + "tuv.last_modified, tuv.exact_match_key, " 
        + "tu.source_tm_name, tuv.modify_user, tuv.creation_date, " 
        + "tuv.creation_user, tuv.updated_by_project, tuv.sid ";

    static private final String WHERE_FOR_POPULATION1
        = "FROM translation_unit_variant tuv, translation_unit tu, "
        + "source_page_leverage_group splg "
        + "WHERE splg.sp_id = ? AND splg.lg_id = tu.leverage_group_id "
        + "AND tu.id = tuv.tu_id AND tuv.state != 'OUT_OF_DATE' "
        + "AND tuv.locale_id in ";

    static private final String WHERE_FOR_POPULATION2
        = " ORDER BY tu.order_num";

    static private final String WHERE_FOR_LEVERAGE
        = "FROM translation_unit_variant tuv, translation_unit tu, "
        + "source_page_leverage_group splg "
        + "WHERE splg.sp_id = ? AND splg.lg_id = tu.leverage_group_id "
        + "AND tu.id = tuv.tu_id AND tuv.locale_id = ? "
        + "AND tuv.state != 'OUT_OF_DATE' "
        + "ORDER BY tu.id";


    public PageJobDataRetriever(Connection p_connection,
        long p_sourcePageId, GlobalSightLocale p_sourceLocale)
        throws Exception
    {
        m_connection = p_connection;
        m_statement = null;
        m_sourcePageId = p_sourcePageId;
        m_sourceLocale = p_sourceLocale;
    }


    /**
     * Query job data segments for a given set of target locales +
     * source locale.
     *
     * @param p_targetLocales Set of target locales (GlobalSightLocale)
     */
    public SegmentQueryResult query()
        throws Exception
    {
        throw new Exception("Don't use this method");
    }

    /**
     * Query job data segments for a given set of target locales +
     * source locale.
     *
     * @param p_targetLocales Set of target locales (GlobalSightLocale)
     */
    public SegmentQueryResult queryForPopulation(Set p_targetLocales)
        throws Exception
    {
        Collection localeList = new ArrayList(p_targetLocales);
        localeList.add(m_sourceLocale);
        String inClause = DbUtil.createLocaleInClause(localeList);
        
        m_statement = m_connection.prepareStatement(
            SELECT_LIST + WHERE_FOR_POPULATION1 + inClause
            + WHERE_FOR_POPULATION2);
        m_statement.setLong(1, m_sourcePageId);
        return new PageJobDataQueryResult(m_statement.executeQuery());
    }


    public SegmentQueryResult queryForLeverage()
        throws Exception
    {
        m_statement = m_connection.prepareStatement(
            SELECT_LIST + WHERE_FOR_LEVERAGE);
        m_statement.setLong(1, m_sourcePageId);
        m_statement.setLong(2, m_sourceLocale.getId());
        return new PageJobDataQueryResult(m_statement.executeQuery());
    }


    public void close()
        throws Exception
    {
        if(m_statement != null)
        {
            m_statement.close();
        }
    }


    
}
