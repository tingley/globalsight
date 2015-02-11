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

import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.everest.persistence.StoredProcCaller;
import com.globalsight.everest.persistence.StoredProcCallerProxy;
import com.globalsight.everest.tm.Tm;
import com.globalsight.ling.tm2.SegmentTmTu;
import com.globalsight.ling.tm2.SegmentTmTuv;
import com.globalsight.util.GlobalSightLocale;

/**
 * SegmentTmExactLeveragerStoredProcCaller calls stored procedures to leverage
 * exact matches (translatable or localizable) from Segment Tm. 
 */

class SegmentTmExactLeveragerStoredProcCaller implements
        StoredProcCallerProxy
{
	
	private static int number = 0; 
	
    private Connection m_connection;
    private long m_sourceLocaleId;
    private long m_translatable;
    private long lookupTarget;

    private Collection m_tmsToLeverageFrom;
    private Collection m_targetLocales;

    private Iterator m_exactMatchKeyIt;
    private Iterator m_orgTuvIdIt;
    private Iterator m_orgSubIdIt;
    
    private String tableName;

    /**
     * Constructor
     * 
     * @param p_connection
     *            Connection
     * @param p_tmsToLeverageFrom
     *            Collection of Tm ids (Long) to leverage from
     * @param p_sourceLocale
     *            source locale
     * @param p_targetLocales
     *            Collection of target locales (GlobalSightLocale)
     * @param p_isMultiLingLeveraging
     *            indicates if multi lingual leveraging is performed
     * @param p_segments
     *            Collection of original source SegmentTmTuv objects
     * @param p_isTranslatable
     *            indicates if leveraging translatable
     */
    SegmentTmExactLeveragerStoredProcCaller(Connection p_connection,
            Collection p_tmsToLeverageFrom, GlobalSightLocale p_sourceLocale,
            Collection p_targetLocales, boolean p_lookupTarget,
            Collection p_segments, boolean p_isTranslatable) throws Exception
    {
        m_connection = p_connection;
        m_tmsToLeverageFrom = p_tmsToLeverageFrom;
        m_sourceLocaleId = p_sourceLocale.getId();
        m_targetLocales = p_targetLocales;
        lookupTarget = p_lookupTarget ? 1 : 0;
        splitTuvParam(p_segments);
        m_translatable = p_isTranslatable ? 1 : 0;
        
        tableName = "tmp_ids" + getNumber();
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
                resultSet = callProc(m_tmsToLeverageFrom, m_sourceLocaleId,
                        m_translatable, lookupTarget,
                        m_targetLocales, (ArrayList) m_exactMatchKeyIt.next(),
                        (ArrayList) m_orgTuvIdIt.next(),
                        (ArrayList) m_orgSubIdIt.next());

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
     * Split the list of exact match key, original source Tuv id and original
     * source sub id due to JDBC driver bug. Each list are converted to ARRAY
     * object. Iterator of the list of ARRAY are stored in the member variables.
     * 
     * @param p_sourceTuvs
     *            Collection of source SegmentTmTuv objects
     */
    private void splitTuvParam(Collection p_sourceTuvs) throws Exception
    {
        Collection exactMatchKeys = new ArrayList(DbUtil.MAX_ELEM);
        Collection orgTuvIds = new ArrayList(DbUtil.MAX_ELEM);
        Collection orgSubIds = new ArrayList(DbUtil.MAX_ELEM);

        Collection exactMatchKeyARRAYs = new ArrayList();
        Collection orgTuvIdARRAYs = new ArrayList();
        Collection orgSubIdARRAYs = new ArrayList();

        int cnt = 0;
        Iterator it = p_sourceTuvs.iterator();
        while (it.hasNext())
        {
            SegmentTmTuv tuv = (SegmentTmTuv) it.next();
            exactMatchKeys.add(new Long(tuv.getExactMatchKey()));
            orgTuvIds.add(new Long(tuv.getId()));
            orgSubIds.add(((SegmentTmTu) tuv.getTu()).getSubId());
            cnt++;

            if (cnt >= DbUtil.MAX_ELEM)
            {
                exactMatchKeyARRAYs.add(new ArrayList(exactMatchKeys));
                orgTuvIdARRAYs.add(new ArrayList(orgTuvIds));
                orgSubIdARRAYs.add(new ArrayList(orgSubIds));
                exactMatchKeys.clear();
                orgTuvIds.clear();
                orgSubIds.clear();
                cnt = 0;
            }
        }

        if (cnt > 0)
        {
            exactMatchKeyARRAYs.add(new ArrayList(exactMatchKeys));
            orgTuvIdARRAYs.add(new ArrayList(orgTuvIds));
            orgSubIdARRAYs.add(new ArrayList(orgSubIds));
        }

        m_exactMatchKeyIt = exactMatchKeyARRAYs.iterator();
        m_orgTuvIdIt = orgTuvIdARRAYs.iterator();
        m_orgSubIdIt = orgSubIdARRAYs.iterator();
    }

    /**
     * Replace procedure "lev_match2.get_st_exact_matches" by Java codes.
     */
    private ResultSet callProc(Collection p_tmsToLeverageFrom,
            long p_sourceLocaleId, long p_translatable,
            long p_multiLingLeveraging, Collection p_targetLocales,
            ArrayList p_exactMatchKeys, ArrayList p_orgTuvIds,
            ArrayList p_orgSubIds) throws Exception
    {

        Statement statement = null;
        PreparedStatement preparedStatement = null;
        PreparedStatement insertStatement = null;
        ResultSet values = null;
        ResultSet rs = null;

        String tuv_table_name;
        String tu_table_name;
        String multi_ling_opt;
        List all_locales;

        if (p_exactMatchKeys == null || p_orgTuvIds == null
                || p_targetLocales == null || p_tmsToLeverageFrom == null
                || p_orgSubIds == null)
        {
            throw new PersistenceException(
                    "One or more of the input array is null.");
        }

        if (p_exactMatchKeys.size() != p_orgTuvIds.size()
                || p_exactMatchKeys.size() != p_orgSubIds.size())
        {
            throw new PersistenceException("Exact match key list and original "
                    + "tuv id list have different number of items.");
        }

        if (p_translatable == 1)
        {
            tu_table_name = "project_tm_tu_t";
            tuv_table_name = "project_tm_tuv_t";
        }
        else
        {
            tu_table_name = "project_tm_tu_l";
            tuv_table_name = "project_tm_tuv_l";
        }

        if (p_multiLingLeveraging == 0)
        {
            multi_ling_opt = "AND tu.source_locale_id = " + p_sourceLocaleId
                    + ' ';
        }
        else
        {
            multi_ling_opt = "";
        }

        statement = m_connection.createStatement();
        startProc(statement);

        String selectIdSql = " SELECT tuv.tu_id " + " FROM " + tu_table_name
				+ " tu, " + tuv_table_name + " tuv "
				+ " WHERE tuv.exact_match_key = ? " + " AND tuv.locale_id = ? "
				+ " AND tu.tm_id IN " + " (:tm_id_list) " + multi_ling_opt
				+ " AND tu.id = tuv.tu_id ";

        ArrayList tmIds = new ArrayList();
        for (Object ob : p_tmsToLeverageFrom)
        {
            if (ob instanceof Tm)
            {
                Tm tm = (Tm) ob;
                tmIds.add(tm.getId());
            }
            else
            {
                tmIds.add(ob);
            }
        }
		String preparedSql = selectIdSql.replaceAll(":tm_id_list",
				StoredProcCaller.convertCollectionToSql(tmIds,
						null));
		preparedStatement = m_connection.prepareStatement(preparedSql);

		insertStatement = m_connection.prepareStatement(" INSERT INTO "
				+ tableName
				+ "(org_tuv_id, org_sub_id, tu_id) values (?, ?, ?) ");

		for (int i = 0, length = p_orgTuvIds.size(); i < length; i++) {
			preparedStatement.setLong(1, ((Long) p_exactMatchKeys.get(i))
					.longValue());
			preparedStatement.setLong(2, p_sourceLocaleId);

			values = preparedStatement.executeQuery();

			while (values.next()) {
				insertStatement.setLong(1, ((Long) p_orgTuvIds.get(i))
						.longValue());
				insertStatement.setString(2, (String) p_orgSubIds.get(i));
				insertStatement.setLong(3, values.getLong(1));

				insertStatement.execute();
			}
		}

		if (values != null) {
			values.close();
        }
        
        if (preparedStatement != null) {
            preparedStatement.close();
        }
        
        if (insertStatement != null) {
            insertStatement.close();
        }

        all_locales = new ArrayList();
        Iterator iterator = p_targetLocales.iterator();
        while (iterator.hasNext())
        {
            GlobalSightLocale locale = (GlobalSightLocale) iterator.next();
            all_locales.add(locale.getIdAsLong());
        }
        all_locales.add(new Long(p_sourceLocaleId));
        String query = " SELECT tmp.org_tuv_id org_tuv_id, "
                + "tmp.org_sub_id org_sub_id, "
                + "tu.id tu_id, tu.tm_id tm_id, tu.format format, "
                + "tu.type type, tuv.id tuv_id, tuv.segment_string segment_string, "
                + "tuv.segment_clob segment_clob, "
                + "tuv.creation_date creation_date, tuv.creation_user creation_user, "
                + "tuv.modify_date modify_date, tuv.modify_user modify_user, "
                + "tuv.exact_match_key exact_match_key, tuv.locale_id locale_id, "
                + "100 score, tuv.sid "
                + "FROM "
                + tu_table_name + " tu, "
                + tuv_table_name + " tuv, "
                + tableName + " tmp "
                + "WHERE tmp.tu_id = tu.id "
                + "AND tu.id = tuv.tu_id "
                + "AND tmp.tu_id = tuv.tu_id "
                + "AND tuv.locale_id IN " + "(:all_locales) "
                + "ORDER BY tmp.org_tuv_id, tmp.org_sub_id, tu.id ";

        query = query.replaceAll(":all_locales", StoredProcCaller
                .convertCollectionToSql(all_locales, null));

        rs = statement.executeQuery(query);

        return rs;
    }

    private void startProc(Statement statement) throws SQLException
    {
        dropTempTable(statement);
        statement.execute("create temporary table " + tableName
                + "(org_tuv_id bigint, org_sub_id VARCHAR(40), tu_id bigint)");
    }

    private void dropTempTable(Statement statement) throws SQLException
    {
        statement.execute("drop temporary table if exists " + tableName);
    }

}
