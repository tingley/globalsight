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
import java.util.List;

import org.apache.log4j.Logger;

import com.globalsight.everest.persistence.StoredProcCallerProxy;
import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.everest.persistence.StoredProcCaller;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.util.GlobalSightLocale;

/**
 * PageTmLeveragerStoredProcCaller calls stored procedures to leverage from Page
 * Tm.
 */

class PageTmLeveragerStoredProcCaller implements StoredProcCallerProxy
{
    private static final Logger c_logger = Logger
            .getLogger(PageTmLeveragerStoredProcCaller.class.getName());
    
    private static int number = 0;

    private Connection m_connection;
    private long m_tmId;
    private long m_sourceLocaleId;
    private long m_translatable;
    private Collection<Long> m_targetLocales;

    private Iterator<List<Long>> m_exactMatchKeyIt;
    private Iterator<List<Long>> m_orgTuvIdIt;
    
    private String tmpTableName;

    /**
     * Constructor
     * 
     * @param p_connection
     *            Connection
     * @param p_tmId
     *            Tm id of a Page Tm
     * @param p_sourceLocale
     *            source locale
     * @param p_targetLocales
     *            Collection of target locales (GlobalSightLocale)
     * @param p_segments
     *            Collection of original source BaseTmTuv objects
     * @param p_isTranslatable
     *            indicates if leveraging translatable
     */
    PageTmLeveragerStoredProcCaller(Connection p_connection, long p_tmId,
            GlobalSightLocale p_sourceLocale,
            Collection<GlobalSightLocale> p_targetLocales,
            Collection p_segments, boolean p_isTranslatable) throws Exception
    {
        m_connection = p_connection;
        m_tmId = p_tmId;
        m_sourceLocaleId = p_sourceLocale.getId();

        m_targetLocales = new ArrayList<Long>();
        for (GlobalSightLocale locale : p_targetLocales)
        {
            m_targetLocales.add(locale.getIdAsLong());
        }

        splitTuvParam(p_segments);
        m_translatable = p_isTranslatable ? 1 : 0;
        
        tmpTableName = "tmp_ids" + getNumber();
    }
    
    public static int getNumber() {
    	if (number > Integer.MAX_VALUE) {
    		number = 0;
    	}
    	
    	return number++;
    }

    public ResultSet getNextResult() throws PersistenceException
    {
        ResultSet resultSet = null;

        try
        {
            // number of iterations of m_exactMatchKeyIt and
            // m_orgTuvIdIt are always the same
            while (m_exactMatchKeyIt.hasNext())
            {
                resultSet = callProc(m_tmId, m_sourceLocaleId,
                        m_translatable,
                        m_targetLocales,
                        (ArrayList<Long>) m_exactMatchKeyIt.next(),
                        (ArrayList<Long>) m_orgTuvIdIt.next());

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
     * Split the list of exact match key and original source Tuv id due to JDBC
     * driver bug. Each list are converted to ARRAY object. Iterator of the list
     * of ARRAY are stored in the member variables.
     * 
     * @param p_sourceTuvs
     *            Collection of source BaseTmTuv objects
     */
    private void splitTuvParam(Collection p_sourceTuvs) throws Exception
    {
        Collection<Long> exactMatchKeys = new ArrayList<Long>(DbUtil.MAX_ELEM);
        Collection<Long> orgTuvIds = new ArrayList<Long>(DbUtil.MAX_ELEM);

        Collection<List<Long>> exactMatchKeyARRAYs = new ArrayList<List<Long>>();
        Collection<List<Long>> orgTuvIdARRAYs = new ArrayList<List<Long>>();

        int cnt = 0;
        Iterator it = p_sourceTuvs.iterator();
        while (it.hasNext())
        {
            BaseTmTuv tuv = (BaseTmTuv) it.next();
            exactMatchKeys.add(new Long(tuv.getExactMatchKey()));
            orgTuvIds.add(new Long(tuv.getId()));
            cnt++;

            if (cnt >= DbUtil.MAX_ELEM)
            {
                exactMatchKeyARRAYs.add(new ArrayList<Long>(exactMatchKeys));
                orgTuvIdARRAYs.add(new ArrayList<Long>(orgTuvIds));
                exactMatchKeys.clear();
                orgTuvIds.clear();
                cnt = 0;
            }
        }

        if (cnt > 0)
        {
            exactMatchKeyARRAYs.add(new ArrayList<Long>(exactMatchKeys));
            orgTuvIdARRAYs.add(new ArrayList<Long>(orgTuvIds));
        }

        m_exactMatchKeyIt = exactMatchKeyARRAYs.iterator();
        m_orgTuvIdIt = orgTuvIdARRAYs.iterator();
    }

    /**
     * Replace procedure "lev_match2.get_page_tm_matches" by Java codes.
     */
    private ResultSet callProc(long p_tmId, long p_sourceLocaleId,
            long p_translatable, Collection<Long> p_targetLocales,
            ArrayList<Long> p_exactMatchKeys, ArrayList<Long> p_orgTuvIds)
            throws Exception
    {
        Statement statement = null;
        PreparedStatement preparedStatement = null;
        PreparedStatement insertStatement = null;
        ResultSet values = null;
        ResultSet rs = null;

        String tuv_table_name;
        String tu_table_name;

        if (p_exactMatchKeys == null || p_orgTuvIds == null
                || p_targetLocales == null)
        {
            throw new PersistenceException(
                    "One or more of the input array is null.");
        }

        if (p_exactMatchKeys.size() != p_orgTuvIds.size())
        {
            throw new PersistenceException(
                    "Exact match key list and original tuv id list have different number of items.");
        }

        if (p_translatable == 1)
        {
            tu_table_name = "page_tm_tu_t";
            tuv_table_name = "page_tm_tuv_t";
        }
        else
        {
            tu_table_name = "page_tm_tu_l";
            tuv_table_name = "page_tm_tuv_l";
        }

        statement = m_connection.createStatement();
        startProc(statement);
        String selectSql = " SELECT tuv.tu_id " + " FROM " + tu_table_name + " tu, "
                + tuv_table_name + " tuv " + " WHERE tuv.exact_match_key = ? "
                + " AND tuv.locale_id = ? " + " AND tu.tm_id = ? "
                + " AND tu.id = tuv.tu_id ";
        preparedStatement = m_connection.prepareStatement(selectSql);
        insertStatement = m_connection.prepareStatement("INSERT INTO " + tmpTableName + " (org_tuv_id, tu_id) values (?, ?)");

        for (int i = 0, length = p_orgTuvIds.size(); i < length; i++)
        {
            preparedStatement.setLong(1, ((Long) p_exactMatchKeys.get(i))
                    .longValue());
            preparedStatement.setLong(2, p_sourceLocaleId);
            preparedStatement.setLong(3, p_tmId);

            values = preparedStatement.executeQuery();
            
            while (values.next()) {
            	insertStatement.setLong(1, ((Long) p_orgTuvIds.get(i)));
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

        List<Long> all_locales = new ArrayList<Long>(p_targetLocales);
        all_locales.add(new Long(p_sourceLocaleId));
        String query = " SELECT  tmp.org_tuv_id org_tuv_id, "
                + "         tmp.org_sub_id org_sub_id, "
                + "         tu.id tu_id, tu.tm_id tm_id, tu.format format, "
                + "         tu.type type, tuv.id tuv_id, tuv.segment_string segment_string, "
                + "         tuv.segment_clob segment_clob, tuv.creation_user creation_user, "
                + "         tuv.creation_date creation_date, "
                + "         tuv.exact_match_key exact_match_key, tuv.locale_id locale_id, "
                + "         tuv.modify_date modify_date, tuv.modify_user modify_user, 100 score " + " FROM "
                + tu_table_name + " tu, " + tuv_table_name + " tuv, "
                + tmpTableName + " tmp " + " WHERE tmp.tu_id = tu.id "
                + " AND tu.id = tuv.tu_id " + " AND tmp.tu_id = tuv.tu_id "
                + " AND tuv.locale_id IN " + " (:all_locales) "
                + " ORDER BY tmp.org_tuv_id, tu.id ";
        query = query.replaceAll(":all_locales", StoredProcCaller
                .convertCollectionToSql(all_locales, null));

        rs = statement.executeQuery(query);

        return rs;
    }

    private void startProc(Statement statement) throws SQLException
    {
        statement.execute("drop temporary table if exists " + tmpTableName);
        statement
                .execute("create temporary table " + tmpTableName + "  (org_tuv_id int, org_sub_id VARCHAR(40), tu_id int)");
    }

}
