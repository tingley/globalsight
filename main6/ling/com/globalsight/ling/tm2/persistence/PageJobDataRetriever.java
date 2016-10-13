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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import com.globalsight.everest.persistence.tuv.BigTableUtil;
import com.globalsight.everest.persistence.tuv.TuvQueryConstants;
import com.globalsight.util.GlobalSightLocale;

/**
 * PageJobDataRetriever is responsible to retrieve page data from
 * translation_unit_variant and translation_unit tables.
 */

public class PageJobDataRetriever implements TuRetriever, TuvQueryConstants
{
    private Connection m_connection = null;
    private PreparedStatement m_statement = null;

    private long m_sourcePageId;
    private GlobalSightLocale m_sourceLocale;
    private long m_jobId;

    // If the list is changed, getter methods in
    // PageJobDataQueryResult must be changed to sync with this list.
    static private final String SELECT_LIST = "SELECT tu.id, tu.tm_id, tu.data_type, tu.tu_type, "
            + "tu.localize_type, tuv.id, tuv.segment_string, "
            + "tuv.segment_clob, tuv.locale_id, tuv.state, tuv.merge_state, "
            + "tuv.last_modified, tuv.exact_match_key, "
            + "tu.source_tm_name, tuv.modify_user, tuv.creation_date, "
            + "tuv.creation_user, tuv.updated_by_project, tuv.sid ";

    static private final String WHERE_FOR_POPULATION1 = "FROM "
            + TUV_TABLE_PLACEHOLDER + " tuv, " + TU_TABLE_PLACEHOLDER
            + " tu, source_page_leverage_group splg "
            + "WHERE tu.id = tuv.tu_id "
            + "AND splg.lg_id = tu.leverage_group_id " + "AND splg.sp_id = ? "
            + "AND tuv.state != 'OUT_OF_DATE' " + "AND tuv.locale_id in ";

    static private final String WHERE_FOR_POPULATION2 = " ORDER BY tu.order_num";

    static private final String WHERE_FOR_LEVERAGE = "FROM "
            + TUV_TABLE_PLACEHOLDER + " tuv, " + TU_TABLE_PLACEHOLDER
            + " tu, source_page_leverage_group splg "
            + "WHERE tu.id = tuv.tu_id "
            + "AND splg.lg_id = tu.leverage_group_id " + "AND splg.sp_id = ? "
            + "AND tuv.locale_id = ? " + "AND tuv.state != 'OUT_OF_DATE' "
            + "ORDER BY tu.id";

    public PageJobDataRetriever(Connection p_connection, long p_sourcePageId,
            GlobalSightLocale p_sourceLocale, long p_jobId) throws Exception
    {
        m_connection = p_connection;
        m_statement = null;
        m_sourcePageId = p_sourcePageId;
        m_sourceLocale = p_sourceLocale;
        m_jobId = p_jobId;
    }

    /**
     * Query job data segments for a given set of target locales + source
     * locale.
     * 
     * @param p_targetLocales
     *            Set of target locales (GlobalSightLocale)
     */
    public SegmentQueryResult query() throws Exception
    {
        throw new Exception("Don't use this method");
    }

    /**
     * Query job data segments for a given set of target locales + source
     * locale.
     * 
     * @param p_targetLocales
     *            Set of target locales (GlobalSightLocale)
     */
    public SegmentQueryResult queryForPopulation(
            Set<GlobalSightLocale> p_targetLocales) throws Exception
    {
        Collection<GlobalSightLocale> localeList = new ArrayList<GlobalSightLocale>(
                p_targetLocales);
        localeList.add(m_sourceLocale);
        String inClause = DbUtil.createLocaleInClause(localeList);

        String sql = SELECT_LIST + WHERE_FOR_POPULATION1 + inClause
                + WHERE_FOR_POPULATION2;
        sql = replaceTuTuvTablePlaceholder(sql);
        m_statement = m_connection.prepareStatement(sql);
        m_statement.setLong(1, m_sourcePageId);

        return new PageJobDataQueryResult(m_statement.executeQuery(), true);
    }

    public SegmentQueryResult queryForLeverage() throws Exception
    {
        String sql = SELECT_LIST + WHERE_FOR_LEVERAGE;
        sql = replaceTuTuvTablePlaceholder(sql);

        m_statement = m_connection.prepareStatement(sql);
        m_statement.setLong(1, m_sourcePageId);
        m_statement.setLong(2, m_sourceLocale.getId());
        return new PageJobDataQueryResult(m_statement.executeQuery());
    }

    public void close() throws Exception
    {
        if (m_statement != null)
        {
            m_statement.close();
        }
    }

    private String replaceTuTuvTablePlaceholder(String sql) throws Exception
    {
        String tuTableName = BigTableUtil.getTuTableJobDataInByJobId(m_jobId);
        String tuvTableName = BigTableUtil.getTuvTableJobDataInByJobId(m_jobId);
        sql = sql.replace(TU_TABLE_PLACEHOLDER, tuTableName);
        sql = sql.replace(TUV_TABLE_PLACEHOLDER, tuvTableName);

        return sql;
    }

}
