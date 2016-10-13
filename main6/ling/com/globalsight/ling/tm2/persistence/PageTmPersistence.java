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
import java.util.Iterator;
import java.util.List;

import com.globalsight.ling.tm2.TmUtil;
import com.globalsight.ling.tm2.leverage.LeverageIterator;
import com.globalsight.ling.tm2.leverage.LeverageOptions;
import com.globalsight.ling.tm2.leverage.LeveragedTu;
import com.globalsight.ling.tm2.leverage.SegmentIdMap;
import com.globalsight.util.GlobalSightLocale;

/**
 * PageTmPersistence class is responsible for accessing persistence layer of
 * Page Tm.
 */

public class PageTmPersistence
{
    private Connection m_connection;

    static private final String GET_PAGE_TM_ID = "SELECT id FROM page_tm WHERE page_name = ? "
            + "AND source_locale_id = ?";

    static private final String CREATE_PAGE_TM = "INSERT INTO page_tm VALUES (?, ?, ?)";

    static public final String PAGE_TM = "page_tm";

    static public final String PAGE_TM_TU_T = "page_tm_tu_t";
    static public final String PAGE_TM_TUV_T = "page_tm_tuv_t";

    static public final String PAGE_TM_TU_L = "page_tm_tu_l";
    static public final String PAGE_TM_TUV_L = "page_tm_tuv_l";

    static public final String TU_T_ALIAS = " tu_t ";
    static public final String TU_L_ALIAS = " tu_l ";
    static public final String TUV_T_ALIAS = " tuv_t ";
    static public final String TUV_L_ALIAS = " tuv_l ";

    public PageTmPersistence(Connection p_connection) throws Exception
    {
        m_connection = p_connection;
    }

    /**
     * Locks necessary tables for Page Tm population. It locks page_tm,
     * page_tm_tu_(t,l) and page_tm_tuv_(t, l) tables.
     */
    public void lockPageTmTables() throws Exception
    {
        List<String> tableList = new ArrayList<String>();
        tableList.add(PAGE_TM);
        tableList.add(PAGE_TM_TU_T);
        tableList.add(PAGE_TM_TUV_T);
        tableList.add(PAGE_TM_TU_L);
        tableList.add(PAGE_TM_TUV_L);
        tableList.add(PAGE_TM_TU_T + TU_T_ALIAS);
        tableList.add(PAGE_TM_TU_L + TU_L_ALIAS);
        tableList.add(PAGE_TM_TUV_T + TUV_T_ALIAS);
        tableList.add(PAGE_TM_TUV_L + TUV_L_ALIAS);

        DbUtil.lockTables(m_connection, tableList);
    }

    /**
     * Get Page Tm id
     * 
     * @param p_pageName
     *            page name (external page id)
     * @param p_locale
     *            source locale
     * @return tm id
     */
    public long getPageTmId(String p_pageName, GlobalSightLocale p_locale)
            throws Exception
    {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        long tmId = 0;
        try
        {
            stmt = m_connection.prepareStatement(GET_PAGE_TM_ID);
            stmt.setString(1, p_pageName);
            stmt.setLong(2, p_locale.getId());
            rs = stmt.executeQuery();
            if (rs.next())
                tmId = rs.getLong(1);
        }
        finally
        {
            DbUtil.silentClose(rs);
            DbUtil.silentClose(stmt);
        }
        return tmId;
    }

    /**
     * Create a page repository (Page Tm)
     * 
     * @param p_pageName
     *            page name (external page id)
     * @param p_locale
     *            source locale
     * @return Tm id of a new Page TM
     */
    public long createPageTm(String p_pageName, GlobalSightLocale p_locale)
            throws Exception
    {
        if (getPageTmId(p_pageName, p_locale) != 0)
        {
            throw new Exception("Duplicate entry " + p_pageName + "-"
                    + p_locale.getId() + " for key 'PAGE_NAME'");
        }

        long tmId = Sequence.allocateIds(Sequence.PAGE_TM_SEQ, 1);

        PreparedStatement stmt = m_connection.prepareStatement(CREATE_PAGE_TM);
        stmt.setLong(1, tmId);
        stmt.setString(2, p_pageName);
        stmt.setLong(3, p_locale.getId());
        stmt.executeUpdate();
        stmt.close();

        return tmId;
    }

    /**
     * Delete specified target segments from a specified Page Tm
     * 
     * @param p_tmId
     *            Page Tm id
     * @param p_targetLocales
     *            Collection of GlobalSightLocale objects
     */
    public void deleteTargetSegments(long p_tmId,
            Collection<GlobalSightLocale> p_targetLocales) throws Exception
    {
        String inLocaleList = DbUtil.createLocaleInClause(p_targetLocales);

        // delete translatable segments
        String sql = createDeleteSql(inLocaleList, true);

        PreparedStatement stmt = m_connection.prepareStatement(sql);
        stmt.setLong(1, p_tmId);
        stmt.executeUpdate();
        stmt.close();

        // delete localizable segments
        sql = createDeleteSql(inLocaleList, false);

        stmt = m_connection.prepareStatement(sql);
        stmt.setLong(1, p_tmId);
        stmt.executeUpdate();
        stmt.close();
    }

    /**
     * Leverage segments from Page Tm. This method calls a stored procedure in
     * the database to get leverage matches and returns an Iterator that creates
     * LeverageMatches object while it's iterated by reading the query results.
     * 
     * @param p_tmId
     *            Tm id from which segments are leveraged
     * @param p_sourceLocale
     *            source locale
     * @param p_segments
     *            Collection of original segments (BaseTmTuv)
     * @param p_isTranslatable
     *            indicates if leverage translatable segments
     * @return Iterator that iterates the query results. next() method of this
     *         Iterator returns LeverageMatches object.
     */
    public Iterator leverage(long p_tmId, GlobalSightLocale p_sourceLocale,
            Collection p_segments, boolean p_isTranslatable,
            LeverageOptions p_leverageOptions) throws Exception
    {
        PageTmLeveragerStoredProcCaller caller = new PageTmLeveragerStoredProcCaller(
                m_connection, p_tmId, p_sourceLocale, p_leverageOptions
                        .getLeveragingLocales().getAllLeveragingLocales(),
                p_segments, p_isTranslatable);

        SegmentIdMap segmentIdMap = TmUtil.buildSegmentIdMap(p_segments);

        return new LeverageIterator(caller.getNextResult(), segmentIdMap,
                p_sourceLocale, p_isTranslatable, LeveragedTu.PAGE_TM,
                p_leverageOptions);
    }

    // create a DELETE FROM sql string
    private String createDeleteSql(String p_localeList, boolean p_translatable)
    {
        String tu = p_translatable ? PAGE_TM_TU_T : PAGE_TM_TU_L;
        String tuv = p_translatable ? PAGE_TM_TUV_T : PAGE_TM_TUV_L;

        StringBuffer sf = new StringBuffer();
        sf.append("delete _tuv_ from _tuv_, _tu_ ");
        sf.append("where _tuv_.tu_id = _tu_.id ");
        sf.append("and _tu_.tm_id = ? ");
        sf.append("and _tuv_.locale_id in ");
        sf.append(p_localeList);

        String sql = sf.toString();
        sql = sql.replaceAll("_tuv_", tuv);
        sql = sql.replaceAll("_tu_", tu);

        return sql;
    }

}
