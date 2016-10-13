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

class CheckForExistenceProcCaller implements StoredProcCallerProxy
{
    private static final Logger c_logger = Logger
            .getLogger(CheckForExistenceProcCaller.class.getName());

    private Connection m_connection;
    private String m_tableName;
    private Iterator m_itTuvId;

    /**
     * Constructor
     * 
     * @param p_connection
     *            Connection
     * @param p_tuvs
     *            Collection of source SegmentTmTuv objects
     * @param p_translatable
     *            segments are translatable or not
     */
    CheckForExistenceProcCaller(Connection p_connection, Collection p_tuvs,
            boolean p_translatable) throws Exception
    {
        m_connection = p_connection;
        m_tableName = p_translatable ? SegmentTmPersistence.SEGMENT_TM_TUV_T
                : SegmentTmPersistence.SEGMENT_TM_TUV_L;
        splitTuvParam(p_tuvs);
    }

    public ResultSet getNextResult() throws PersistenceException
    {
        ResultSet resultSet = null;

        try
        {
            while (m_itTuvId.hasNext())
            {
                resultSet = callProc((ArrayList) m_itTuvId.next());
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
        Collection tuvIdARRAYs = new ArrayList();

        int cnt = 0;
        Iterator itTuv = p_tuvs.iterator();
        while (itTuv.hasNext())
        {
            BaseTmTuv tuv = (BaseTmTuv) itTuv.next();
            tuvIds.add(new Long(tuv.getId()));
            cnt++;

            if (cnt >= DbUtil.MAX_ELEM)
            {
                tuvIdARRAYs.add(new ArrayList(tuvIds));
                tuvIds.clear();
                cnt = 0;
            }
        }

        if (cnt > 0)
        {
            tuvIdARRAYs.add(new ArrayList(tuvIds));
        }

        m_itTuvId = tuvIdARRAYs.iterator();
    }

    private ResultSet callProc(ArrayList p_tuvIds) throws Exception
    {
        Statement statement = null;
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;

        statement = m_connection.createStatement();
        startProc(statement);

        if (p_tuvIds == null)
        {
            throw new PersistenceException(
                    "One or more of the input array is null.");
        }

        String insertSql = "insert into tmp_tu_id(org_id) VALUES(?)";
        preparedStatement = m_connection.prepareStatement(insertSql);

        for (int i = 0, length = p_tuvIds.size(); i < length; i++)
        {
            preparedStatement.setLong(1, ((Long) p_tuvIds.get(i)).longValue());
            preparedStatement.execute();
        }

        String querySql = " SELECT tmp.org_id " + " FROM tmp_tu_id tmp "
                + " WHERE NOT EXISTS " + "      (SELECT * FROM " + m_tableName
                + " tuv " + "       WHERE tuv.id = tmp.org_id) ";

        rs = statement.executeQuery(querySql);

        return rs;
    }

    private void startProc(Statement statement) throws SQLException
    {
        statement.execute("drop temporary table if exists tmp_tu_id");
        statement
                .execute("create temporary table tmp_tu_id (org_id bigint, found_id bigint)");
    }

}
