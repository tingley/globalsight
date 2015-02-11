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

import com.globalsight.everest.persistence.StoredProcCallerProxy;
import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.util.GlobalSightLocale;

class CheckForDuplicateForSourceProcCaller implements
        StoredProcCallerProxy
{
    private static final Logger c_logger = Logger
            .getLogger(CheckForDuplicateForSourceProcCaller.class.getName());

    private Connection m_connection;
    private long m_tmId;
    private long m_translatable;
    private long m_localeId;

    private Iterator m_itTuvId;
    private Iterator m_itExactMatchKey;

    /**
     * Constructor
     * 
     * @param p_connection
     *            Connection
     * @param p_tuvs
     *            Collection of source BaseTmTuv objects
     * @param p_tmId
     *            Tm id to check for the duplicate
     * @param p_locale
     *            Source locale
     * @param p_translatable
     *            segments are translatable or not
     */
    CheckForDuplicateForSourceProcCaller(Connection p_connection,
            Collection p_tuvs, long p_tmId, GlobalSightLocale p_locale,
            boolean p_translatable) throws Exception
    {
        m_connection = p_connection;
        m_tmId = p_tmId;
        m_localeId = p_locale.getId();
        m_translatable = p_translatable ? 1 : 0;
        splitTuvParam(p_tuvs);
    }

    public ResultSet getNextResult() throws PersistenceException
    {
        ResultSet resultSet = null;

        try
        {
            // number of iterations of m_itExactMatchKey and m_itTuvId are
            // always the same
            while (m_itTuvId.hasNext())
            {
                resultSet = callProc((ArrayList) m_itTuvId.next(),
                        (ArrayList) m_itExactMatchKey.next());
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
     * Split the list of tuv id due to JDBC driver bug. Each list are converted
     * to ARRAY object. Iterator of the list of ARRAY are stored in the member
     * variables.
     * 
     * @param p_tuvs
     *            Collection of BaseTmTuv objects
     */
    private void splitTuvParam(Collection p_tuvs) throws Exception
    {
        Collection exactMatchKeys = new ArrayList(DbUtil.MAX_ELEM);
        Collection tuvIds = new ArrayList(DbUtil.MAX_ELEM);

        Collection exactMatchKeyARRAYs = new ArrayList();
        Collection tuvIdARRAYs = new ArrayList();

        int cnt = 0;
        Iterator itTuv = p_tuvs.iterator();
        while (itTuv.hasNext())
        {
            BaseTmTuv tuv = (BaseTmTuv) itTuv.next();
            tuvIds.add(new Long(tuv.getId()));
            exactMatchKeys.add(new Long(tuv.getExactMatchKey()));
            cnt++;

            if (cnt >= DbUtil.MAX_ELEM)
            {
                tuvIdARRAYs.add(new ArrayList(tuvIds));
                exactMatchKeyARRAYs.add(new ArrayList(exactMatchKeys));
                tuvIds.clear();
                exactMatchKeys.clear();
                cnt = 0;
            }
        }

        if (cnt > 0)
        {
            tuvIdARRAYs.add(new ArrayList(tuvIds));
            exactMatchKeyARRAYs.add(new ArrayList(exactMatchKeys));
        }

        m_itTuvId = tuvIdARRAYs.iterator();
        m_itExactMatchKey = exactMatchKeyARRAYs.iterator();
    }

    /**
     * Re-implement procedure "segment_tm_query.check_for_source_duplicate" by
     * Java code.
     * 
     * @param p_tuvIds
     * @param p_exactMatchKeys
     * @return
     * @throws Exception
     */
    private ResultSet callProc(ArrayList p_tuvIds, ArrayList p_exactMatchKeys)
            throws Exception
    {
        Statement statement = null;
        PreparedStatement preparedStatement = null;
        PreparedStatement insertStatement = null;
        ResultSet values = null;
        ResultSet rs = null;

        String tuv_table_name;
        String tu_table_name;

        if (p_tuvIds == null || p_exactMatchKeys == null)
        {
            throw new PersistenceException(
                    "One or more of the input array is null.");
        }

        if (p_tuvIds.size() != p_exactMatchKeys.size())
        {
            throw new PersistenceException(
                    "Input arrays have different number of items.");
        }

        statement = m_connection.createStatement();
        startProc(statement);

        if (m_translatable == 1)
        {
            tu_table_name = "project_tm_tu_t";
            tuv_table_name = "project_tm_tuv_t";
        }
        else
        {
            tu_table_name = "project_tm_tu_l";
            tuv_table_name = "project_tm_tuv_l";
        }

        String selectSql = " SELECT tuv1.tu_id, tuv2.tu_id " + " FROM "
                + tuv_table_name + " tuv1, " + tuv_table_name + " tuv2, "
                + tu_table_name + " tu1, " + tu_table_name + " tu2 "
                + " WHERE tuv1.id = ? " + "       AND tu1.id = tuv1.tu_id "
                + "       AND tuv2.exact_match_key = ? "
                + "       AND tuv2.locale_id = ? "
                + "       AND tu2.id = tuv2.tu_id "
                + "       AND tu2.source_locale_id = ? "
                + "       AND tuv2.id != ? " + "       AND tu2.tm_id = ? "
                + "       AND tu1.type = tu2.type ";

        preparedStatement = m_connection.prepareStatement(selectSql);
        insertStatement = m_connection.prepareStatement("INSERT INTO tmp_tu_id (org_id, found_id) values (?, ?)");

        for (int i = 0, length = p_tuvIds.size(); i < length; i++)
        {
            preparedStatement.setLong(1, ((Long) p_tuvIds.get(i)).longValue());
            preparedStatement.setLong(2, ((Long) p_exactMatchKeys.get(i))
                    .longValue());
            preparedStatement.setLong(3, m_localeId);
            preparedStatement.setLong(4, m_localeId);
            preparedStatement.setLong(5, ((Long) p_tuvIds.get(i)).longValue());
            preparedStatement.setLong(6, m_tmId);

            values = preparedStatement.executeQuery();
            
            while (values.next()) {
            	insertStatement.setLong(1, values.getLong(1));
            	insertStatement.setLong(2, values.getLong(2));
            	
            	insertStatement.execute();
            }
        }
        
        if (values != null) {
        	values.close();
        }
        
        if (insertStatement != null) {
        	insertStatement.close();
        }
        
        if (preparedStatement != null) {
        	preparedStatement.close();
        }

        String querySql = " SELECT tmp.org_id org_id, tu.id tu_id, format,"
                + "        type, tuv.id tuv_id, "
                + "        segment_string, segment_clob, exact_match_key, locale_id, "
                + "        modify_date " + " FROM " + tu_table_name + " tu, "
                + tuv_table_name + " tuv, " + "       tmp_tu_id tmp "
                + " WHERE tu.id = tuv.tu_id "
                + "       AND (tu.id = tmp.org_id "
                + "       OR tu.id = tmp.found_id) "
                + " ORDER BY tmp.org_id, tu.id ";

        rs = statement.executeQuery(querySql);
        return rs;
    }

    private void startProc(Statement statement) throws SQLException
    {
        statement.execute("drop temporary table if exists tmp_tu_id");
        statement.execute("create temporary table tmp_tu_id "
                + "(org_id int, found_id int)");
    }

}
