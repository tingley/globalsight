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

package com.globalsight.everest.tuv;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.persistence.tuv.SegmentTuTuvCacheManager;
import com.globalsight.everest.persistence.tuv.TuvQueryConstants;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.util.GlobalSightLocale;

/**
 * TuvJdbcQuery class is responsible for querying Tu/Tuv data from database.
 */

public class TuvJdbcQuery extends SegmentTuTuvCacheManager implements
        TuvQueryConstants
{

	// select column list
	private static final String SELECT_COLUMN_LIST = "SELECT "
	        + "tu.id tu_id, tu.order_num tu_order_num, tu.tm_id tm_id, "
			+ "tu.data_type data_type, tu.tu_type tu_type, "
			+ "tu.localize_type localize_type, "
			+ "tu.leverage_group_id leverage_group_id, tu.pid pid, tuv.id tuv_id, "
			+ "tuv.order_num tuv_order_num, tuv.locale_id locale_id, "
			+ "tuv.is_indexed is_indexed, tuv.segment_clob segment_clob, "
			+ "tuv.segment_string segment_string, tuv.word_count word_count, "
			+ "tuv.exact_match_key exact_match_key, tuv.state state, "
			+ "tuv.merge_state merge_state, tuv.last_modified last_modified, "
	        + "tuv.timestamp timestamp, tuv.modify_user, tuv.sid ";

	private static final String CONDITION_BY_SOURCE_PAGE_AND_LOCALE = "FROM "
	        + TU_TABLE_PLACEHOLDER + " tu, "
	        + TUV_TABLE_PLACEHOLDER + " tuv, "
			+ "source_page_leverage_group splg "
			+ "WHERE tuv.state != 'OUT_OF_DATE' AND tuv.tu_id = tu.id "
			+ "AND tu.leverage_group_id = splg.lg_id AND splg.sp_id = ? "
			+ "AND tuv.locale_id in ";

	private static final String ORDER_BY_TU_ORDER = " order by tu.order_num asc ";

	private Connection m_connection;

	public TuvJdbcQuery(Connection p_connection)
	{
		m_connection = p_connection;
	}

	/**
	 * Retrieve TU/TUVs by source page id and locales. TUs are sorted by
	 * order_num
	 * 
	 * @param p_sourcePage
	 *            source page
	 * @param p_targetLocales
	 *            List of target locales
	 * @return List of Tus
	 */
	public List getTusBySourcePageIdAndLocales(SourcePage p_sourcePage,
			Collection p_targetLocales) throws Exception
	{
		GlobalSightLocaleRepository localeRepository = new GlobalSightLocaleRepository();
		localeRepository.addLocale(p_sourcePage.getGlobalSightLocale());
		localeRepository.addLocales(p_targetLocales);

		String query = SELECT_COLUMN_LIST + CONDITION_BY_SOURCE_PAGE_AND_LOCALE
				+ DbUtil.createLocaleInClause(localeRepository.getAllLocales())
				+ ORDER_BY_TU_ORDER;

		String tuTableName = getTuTableName(p_sourcePage.getCompanyId());
		String tuvTableName = getTuvTableName(p_sourcePage.getCompanyId());
		query = query.replace(TU_TABLE_PLACEHOLDER, tuTableName);
		query = query.replace(TUV_TABLE_PLACEHOLDER, tuvTableName);

		ArrayList tus = null;

		PreparedStatement ps = null;
		ResultSet rs = null;
		try
		{
			ps = m_connection.prepareStatement(query);

			ps.setLong(1, p_sourcePage.getId());
			rs = ps.executeQuery();

			tus = createTus(rs, localeRepository);
		}
		finally
		{
		    releaseRsPsConnection(rs, ps, null);
		}

		return tus;
	}

	// /// private methods ////////

	// create List of Tu objects
	private ArrayList createTus(ResultSet p_rs,
			GlobalSightLocaleRepository p_localeRepository) throws Exception
	{
		ArrayList tus = new ArrayList();
		long currentTuId = 0;
		TuImplVo currentTu = null;

		while (p_rs.next())
		{
			long tuId = p_rs.getLong("tu_id");

			if (tuId != currentTuId)
			{
				currentTuId = tuId;

				// create TU
				currentTu = createTu(tuId, p_rs);
				tus.add(currentTu);
			}

			// create TUV
			TuvImplVo tuv = createTuv(p_rs, p_localeRepository);
			currentTu.addTuv(tuv);
		}

		return tus;
	}

	private TuImplVo createTu(long p_id, ResultSet p_rs) throws Exception
	{
		TuImplVo tu = new TuImplVo();
		tu.setId(p_id);
		tu.setOrder(p_rs.getLong("tu_order_num"));
		tu.setTmId(p_rs.getLong("tm_id"));
		tu.setDataType(p_rs.getString("data_type"));
		tu.setTuTypeName(p_rs.getString("tu_type"));
		tu.setLocalizableType(p_rs.getString("localize_type").charAt(0));
		tu.setLeverageGroupId(p_rs.getLong("leverage_group_id"));
		tu.setPid(p_rs.getLong("pid"));

		return tu;
	}

	private TuvImplVo createTuv(ResultSet p_rs,
			GlobalSightLocaleRepository p_localeRepository) throws Exception
	{
		TuvImplVo tuv = new TuvImplVo();
		tuv.setId(p_rs.getLong("tuv_id"));
		tuv.setOrder(p_rs.getLong("tuv_order_num"));
		tuv.setGxml(getTuvSegment(p_rs));
		tuv.setWordCount(p_rs.getInt("word_count"));
		tuv.setExactMatchKey(p_rs.getLong("exact_match_key"));
		tuv.setStateName(p_rs.getString("state"));
		tuv.setMergeState(p_rs.getString("merge_state"));
		tuv.setLastModified(p_rs.getDate("last_modified"));
		tuv.setLastModifiedUser(p_rs.getString("modify_user"));
		tuv.setTimestamp(p_rs.getTimestamp("timestamp"));
		tuv.setSid(p_rs.getString("sid"));
		
		long localeId = p_rs.getLong("locale_id");
		tuv.setGlobalSightLocale(p_localeRepository.getLocale(localeId));

		String isIndexed = p_rs.getString("is_indexed");
		if (isIndexed.equals("Y"))
		{
			tuv.makeIndexed();
		}

		return tuv;
	}

	private String getTuvSegment(ResultSet p_rs) throws Exception
	{
		String segment = p_rs.getString("segment_string");
		if (segment == null)
		{
			segment = DbUtil.readClob(p_rs, "segment_clob");
		}
		return segment;
	}

	private class GlobalSightLocaleRepository
	{
        HashMap<Long, GlobalSightLocale> m_locales = new HashMap<Long, GlobalSightLocale>();

		private void addLocale(GlobalSightLocale p_locale)
		{
			m_locales.put(p_locale.getIdAsLong(), p_locale);
		}

		private void addLocales(Collection p_locales)
		{
			for (Iterator it = p_locales.iterator(); it.hasNext();)
			{
				GlobalSightLocale locale = (GlobalSightLocale) it.next();
				m_locales.put(locale.getIdAsLong(), locale);
			}
		}

		private GlobalSightLocale getLocale(long p_localeId)
		{
			return (GlobalSightLocale) m_locales.get(new Long(p_localeId));
		}

		private Collection getAllLocales()
		{
			return m_locales.values();
		}
	}

}
