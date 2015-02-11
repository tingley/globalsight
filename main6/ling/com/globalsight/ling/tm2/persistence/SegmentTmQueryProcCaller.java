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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.everest.persistence.StoredProcCaller;
import com.globalsight.everest.persistence.StoredProcCallerProxy;
import com.globalsight.ling.tm2.BaseTmTuv;

class SegmentTmQueryProcCaller implements StoredProcCallerProxy
{
    private static final Logger c_logger = Logger
            .getLogger(SegmentTmQueryProcCaller.class.getName());

    private static int number = 0;

    private Connection m_connection;
    private long m_tmId;
    private long m_sourceLocaleId;
    private long m_translatable;

    private Iterator m_exactMatchKeyIt;
    private Iterator m_typeIt;

    private String tempTableName;

    private static final String SELECT_ID_SQL_T = "SELECT distinct tuv_t.tu_id "
            + "from project_tm_tu_t tu_t, "
            + "project_tm_tuv_t tuv_t "
            + "WHERE tu_t.id = tuv_t.tu_id "
            + "AND tu_t.tm_id = :tm_id "
            + "AND tu_t.SOURCE_LOCALE_ID = :source_locale_id "
            + "AND tuv_t.locale_id = :source_locale_id "
            + "AND tu_t.type in (:type) "
            + "AND tuv_t.exact_match_key in (:exact_match_key)";

    private static final String SELECT_ID_SQL_L = "SELECT distinct tuv_l.tu_id "
            + "from project_tm_tu_l tu_l, "
            + "project_tm_tuv_l tuv_l "
            + "WHERE tu_l.id = tuv_l.tu_id "
            + "AND tu_l.tm_id = :tm_id "
            + "AND tu_l.SOURCE_LOCALE_ID = :source_locale_id "
            + "AND tuv_l.locale_id = :source_locale_id "
            + "AND tu_l.type in (:type) "
            + "AND tuv_l.exact_match_key in (:exact_match_key)";

    private static final String QUERY_SQL_T = "SELECT  tu_t.id tu_id, format, "
            + "type, tuv_t.id tuv_id, segment_string, segment_clob, "
            + "exact_match_key, locale_id, modify_date, sid "
            + "FROM project_tm_tu_t tu_t, project_tm_tuv_t tuv_t, "
            + ":tempTableName tmp WHERE tmp.found_id = tu_t.id "
            + "AND tu_t.id = tuv_t.tu_id AND tmp.found_id = tuv_t.tu_id "
            + "ORDER BY tu_t.id";

    private static final String QUERY_SQL_L = " SELECT tu_l.id tu_id, format, "
            + "type, tuv_l.id tuv_id, segment_string, segment_clob, "
            + "exact_match_key, locale_id, modify_date "
            + "FROM project_tm_tu_l tu_l, project_tm_tuv_l tuv_l, "
            + ":tempTableName tmp WHERE tmp.found_id = tu_l.id "
            + "AND tu_l.id = tuv_l.tu_id AND tmp.found_id = tuv_l.tu_id "
            + "ORDER BY tu_l.id";

    /**
     * Constructor
     * 
     * @param p_connection
     *            Connection
     * @param p_tmId
     *            Tm id
     * @param p_sourceLocaleId
     *            source locale id
     * @param p_translatable
     *            segments are translatable or not
     * @param p_sourceTuvs
     *            Collection of source SegmentTmTuv objects
     */
    SegmentTmQueryProcCaller(Connection p_connection, long p_tmId,
            long p_sourceLocaleId, boolean p_translatable,
            Collection p_sourceTuvs) throws Exception
    {
        m_connection = p_connection;
        m_tmId = p_tmId;
        m_sourceLocaleId = p_sourceLocaleId;
        m_translatable = p_translatable ? 1 : 0;

        splitTuvParam(p_sourceTuvs);

        tempTableName = "tmp_tu_id" + getNumber();
    }

    public static int getNumber()
    {
        if (number > Integer.MAX_VALUE)
        {
            number = 0;
        }

        return number++;
    }

    public ResultSet getNextResult() throws PersistenceException
    {
        ResultSet resultSet = null;

        try
        {
            // number of iterations of m_exactMatchKeyIt and m_typeIt are
            // always the same
            while (m_exactMatchKeyIt.hasNext())
            {
                resultSet = callProc(m_connection, m_tmId, m_sourceLocaleId,
                        m_translatable, (ArrayList) m_exactMatchKeyIt.next(),
                        (ArrayList) m_typeIt.next());

                if (resultSet != null)
                {
                    break;
                }
            }
        }
        catch (PersistenceException pe)
        {
            throw pe;
        }
        catch (Exception e)
        {
            throw new PersistenceException(e);
        }

        return resultSet;
    }

    /**
     * Split the list of exact match key and type due to JDBC driver bug. Each
     * list are converted to ARRAY object. Iterator of the list of ARRAY are
     * stored in the member variables.
     * 
     * @param p_sourceTuvs
     *            Collection of source SegmentTmTuv objects
     */
    private void splitTuvParam(Collection p_sourceTuvs) throws Exception
    {
        Collection exactMatchKeys = new ArrayList(DbUtil.MAX_ELEM);
        Collection types = new ArrayList(DbUtil.MAX_ELEM);

        Collection exactMatchKeyARRAYs = new ArrayList();
        Collection typeARRAYs = new ArrayList();

        int cnt = 0;
        Iterator it = p_sourceTuvs.iterator();
        while (it.hasNext())
        {
            BaseTmTuv tuv = (BaseTmTuv) it.next();
            exactMatchKeys.add(new Long(tuv.getExactMatchKey()));
            types.add(tuv.getTu().getType());
            cnt++;

            if (cnt >= DbUtil.MAX_ELEM)
            {
                exactMatchKeyARRAYs.add(new ArrayList(exactMatchKeys));
                typeARRAYs.add(new ArrayList(types));
                exactMatchKeys.clear();
                types.clear();
                cnt = 0;
            }
        }

        if (cnt > 0)
        {
            exactMatchKeyARRAYs.add(new ArrayList(exactMatchKeys));
            typeARRAYs.add(new ArrayList(types));
        }

        m_exactMatchKeyIt = exactMatchKeyARRAYs.iterator();
        m_typeIt = typeARRAYs.iterator();
    }

    /**
     * Re-implement procedure "segment_tm_query.get_exact_segments" by Java
     * code.
     */
    private ResultSet callProc(Connection p_connection, long p_tmId,
            long p_sourceLocaleId, long p_translatable,
            ArrayList p_exactMatchKeys, ArrayList p_types) throws Exception
    {
        Statement statement = null;
        ResultSet rs = null;

        String tuv_table_name;
        String tu_table_name;

        if (p_exactMatchKeys == null || p_types == null)
        {
            throw new PersistenceException(
                    "One or more of the input array is null.");
        }

        if (p_exactMatchKeys.size() != p_types.size())
        {
            throw new PersistenceException(
                    "Exact match key list and type list have different number of items.");
        }

        statement = m_connection.createStatement();
        startProc(statement);

        String selectIdSql = null;
        String querySql = null;
        if (p_translatable == 1)
        {
            selectIdSql = SELECT_ID_SQL_T;
            querySql = QUERY_SQL_T;
        }
        else
        {
            selectIdSql = SELECT_ID_SQL_L;
            querySql = QUERY_SQL_L;
        }

        selectIdSql = selectIdSql.replaceAll(":type",
                StoredProcCaller.convertCollectionToSql(p_types, null));
        selectIdSql = selectIdSql.replaceAll(":tm_id", String.valueOf(p_tmId));
        selectIdSql = selectIdSql
                .replaceAll(":exact_match_key", StoredProcCaller
                        .convertCollectionToSql(p_exactMatchKeys, null));
        selectIdSql = selectIdSql.replaceAll(":source_locale_id",
                String.valueOf(p_sourceLocaleId));

        ResultSet values = statement.executeQuery(selectIdSql);
        PreparedStatement insertStatement = m_connection
                .prepareStatement("INSERT INTO " + tempTableName
                        + " (found_id) values (?)");

        while (values.next())
        {
            insertStatement.setLong(1, values.getLong(1));
            insertStatement.execute();
        }

        if (values != null)
        {
            values.close();
        }

        if (insertStatement != null)
        {
            insertStatement.close();
        }

        querySql = querySql.replaceAll(":tempTableName", tempTableName);

        rs = statement.executeQuery(querySql);

        return rs;
    }

    private void startProc(Statement statement) throws SQLException
    {
        statement.execute("drop temporary table if exists " + tempTableName);
        statement.execute("create temporary table " + tempTableName
                + " (org_id bigint, found_id bigint)");
    }

}
