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

class CheckForDuplicateForTargetProcCaller implements
        StoredProcCallerProxy
{
    private static final Logger c_logger = Logger
            .getLogger(CheckForDuplicateForTargetProcCaller.class.getName());

    private Connection m_connection;
    private String m_tableName;
    private long m_localeId;

    private Iterator m_itTuId;
    private Iterator m_itTuvId;
    private Iterator m_itExactMatchKey;

    /**
     * Constructor
     * 
     * @param p_connection
     *            Connection
     * @param p_tuvs
     *            Collection of source BaseTmTuv objects
     * @param p_locale
     *            Source locale
     * @param p_translatable
     *            segments are translatable or not
     */
    CheckForDuplicateForTargetProcCaller(Connection p_connection,
            Collection p_tuvs, GlobalSightLocale p_locale,
            boolean p_translatable) throws Exception
    {
        m_connection = p_connection;
        m_localeId = p_locale.getId();
        m_tableName = p_translatable ? SegmentTmPersistence.SEGMENT_TM_TUV_T
                : SegmentTmPersistence.SEGMENT_TM_TUV_L;
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
                        (ArrayList) m_itTuId.next(),
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
        Collection tuvIds = new ArrayList(DbUtil.MAX_ELEM);
        Collection tuIds = new ArrayList(DbUtil.MAX_ELEM);
        Collection exactMatchKeys = new ArrayList(DbUtil.MAX_ELEM);

        Collection tuvIdARRAYs = new ArrayList();
        Collection tuIdARRAYs = new ArrayList();
        Collection exactMatchKeyARRAYs = new ArrayList();

        int cnt = 0;
        Iterator itTuv = p_tuvs.iterator();
        while (itTuv.hasNext())
        {
            BaseTmTuv tuv = (BaseTmTuv) itTuv.next();
            tuvIds.add(new Long(tuv.getId()));
            tuIds.add(new Long(tuv.getTu().getId()));
            exactMatchKeys.add(new Long(tuv.getExactMatchKey()));
            cnt++;

            if (cnt >= DbUtil.MAX_ELEM)
            {
                tuvIdARRAYs.add(new ArrayList(tuvIds));
                tuIdARRAYs.add(new ArrayList(tuIds));
                exactMatchKeyARRAYs.add(new ArrayList(exactMatchKeys));
                tuvIds.clear();
                tuIds.clear();
                exactMatchKeys.clear();
                cnt = 0;
            }
        }

        if (cnt > 0)
        {
            tuvIdARRAYs.add(new ArrayList(tuvIds));
            tuIdARRAYs.add(new ArrayList(tuIds));
            exactMatchKeyARRAYs.add(new ArrayList(exactMatchKeys));
        }

        m_itTuvId = tuvIdARRAYs.iterator();
        m_itTuId = tuIdARRAYs.iterator();
        m_itExactMatchKey = exactMatchKeyARRAYs.iterator();
    }

    /**
     * Re-implement procedure "segment_tm_query.check_for_target_duplicate" by
     * Java code.
     * 
     * @param p_tuvIds
     * @param p_tuIds
     * @param p_exactMatchKeys
     * @return
     * @throws Exception
     */
    private ResultSet callProc(ArrayList p_tuvIds, ArrayList p_tuIds,
            ArrayList p_exactMatchKeys) throws Exception
    {
        Statement statement = null;
        PreparedStatement preparedStatement = null;
        PreparedStatement insertStatement = null;
        ResultSet values = null;
        ResultSet rs = null;

        statement = m_connection.createStatement();
        startProc(statement);

        if (p_tuIds == null || p_tuvIds == null || p_exactMatchKeys == null)
        {
            throw new PersistenceException(
                    "One or more of the input array is null.");
        }

        if (p_exactMatchKeys.size() != p_tuIds.size()
                || p_exactMatchKeys.size() != p_tuvIds.size())
        {
            throw new PersistenceException(
                    "Input arrays have different number of items.");
        }

        String selectSql = " SELECT tuv.id " + " FROM " + m_tableName + " tuv "
                + " WHERE tuv.tu_id = ? " + " AND tuv.exact_match_key = ? "
                + " AND tuv.locale_id = ? " + " AND tuv.id != ? ";
        
        preparedStatement = m_connection.prepareStatement(selectSql);
        insertStatement = m_connection.prepareStatement("INSERT INTO tmp_tu_id (org_id, found_id) values (?, ?)");

        for (int i = 0, length = p_tuvIds.size(); i < length; i++)
        {
            preparedStatement.setLong(1, ((Long) p_tuIds.get(i)).longValue());
            preparedStatement.setLong(2, ((Long) p_exactMatchKeys.get(i))
                    .longValue());
            preparedStatement.setLong(3, m_localeId);
            preparedStatement.setLong(4, ((Long) p_tuvIds.get(i)).longValue());

            values = preparedStatement.executeQuery();
            while (values.next()) {
            	insertStatement.setLong(1, ((Long) p_tuvIds.get(i)).longValue());
            	insertStatement.setLong(2, values.getLong(1));
            	
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

        String querySql = " SELECT tmp.org_id org_tuv_id, tuv.id tuv_id, segment_string, "
                + "        segment_clob, exact_match_key, locale_id, modify_date "
                + " FROM "
                + m_tableName
                + " tuv, tmp_tu_id tmp "
                + " WHERE tmp.found_id = tuv.id ";

        rs = statement.executeQuery(querySql);

        return rs;
    }

    private void startProc(Statement statement) throws SQLException
    {
        statement.execute("drop temporary table if exists tmp_ids");
        statement
                .execute("create temporary table tmp_ids (org_tuv_id int, org_sub_id VARCHAR(40), tu_id int)");
    }

}
