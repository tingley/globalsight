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
package com.globalsight.everest.tm.searchreplace;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.globalsight.everest.persistence.tuv.BigTableUtil;
import com.globalsight.everest.persistence.tuv.TuvQueryConstants;
import com.globalsight.everest.workflow.WorkflowJbpmUtil;
import com.globalsight.ling.common.Text;

public class ActivityPageDataQuery
{
	static private Logger c_category = Logger
			.getLogger(ActivityPageDataQuery.class);

	private Connection m_connection;

	public ActivityPageDataQuery(Connection p_connection)
	{
		m_connection = p_connection;
	}

    public List<TaskInfo> query(String p_searchString,
            Collection<String> p_targetLocales, long p_jobId,
            boolean p_caseSensitiveSearch) throws Exception
	{
		List<TaskInfo> result = new ArrayList<TaskInfo>();
		PreparedStatement stmt = null;
		ResultSet rset = null;

		String orderingClause = " order by jnode.name_, concat(l1.iso_lang_code,'_',l1.iso_country_code), sp.external_page_id";

		try
		{
			String inWFTClauseHolder = addWFTLocaleClause(p_targetLocales);
			String inJobClauseHolder = " and j.id = " + p_jobId;
			String inTUVClauseHolder = addTUVLocaleClause(p_targetLocales);

            String tuTableName = BigTableUtil
                    .getTuTableJobDataInByJobId(p_jobId);
            String tuvTableName = BigTableUtil
                    .getTuvTableJobDataInByJobId(p_jobId);
			if (p_caseSensitiveSearch)
			{
                String sql1 = SqlHolder.SEARCH_CASE_SENSITIVE.replaceAll(
                        TuvQueryConstants.TU_TABLE_PLACEHOLDER, tuTableName);
                sql1 = sql1.replaceAll(TuvQueryConstants.TUV_TABLE_PLACEHOLDER,
                        tuvTableName);
				StringBuffer sb = new StringBuffer(sql1);
				sb.append(inWFTClauseHolder);
				sb.append(inJobClauseHolder);
				sb.append(inTUVClauseHolder);
				sb.append(orderingClause);

				if (c_category.isDebugEnabled())
				{
					c_category.debug("activity search query = " + sb);
				}

				stmt = m_connection.prepareStatement(sb.toString());
			}
			else
			{
                String sql2 = SqlHolder.SEARCH_CASE_INSENSITIVE.replaceAll(
                        TuvQueryConstants.TU_TABLE_PLACEHOLDER, tuTableName);
                sql2 = sql2.replaceAll(TuvQueryConstants.TUV_TABLE_PLACEHOLDER,
                        tuvTableName);
				StringBuffer sb = new StringBuffer(sql2);
				sb.append(inWFTClauseHolder);
				sb.append(inJobClauseHolder);
				sb.append(inTUVClauseHolder);
				sb.append(orderingClause);

				if (c_category.isDebugEnabled())
				{
					c_category.debug("activity search query = " + sb);
				}

				stmt = m_connection.prepareStatement(sb.toString());
			}

			String queryPattern = makeWildcardQueryString(p_searchString, '&');
			stmt.setString(1, queryPattern);
			rset = stmt.executeQuery();

			while (rset.next())
			{
				long taskId = rset.getLong(1);
				String jobName = rset.getString(2);
				String taskName = WorkflowJbpmUtil.getTaskName(rset
						.getString(3));

				// information about the job first
				TaskInfo taskInfo = new TaskInfo();
				taskInfo.setTaskId(taskId);
				taskInfo.setTaskName(taskName);
				taskInfo.setJobName(jobName);

				// information about the target locale next
				TargetLocaleInfo targetLocaleInfo = new TargetLocaleInfo();
				targetLocaleInfo.setId(rset.getLong(4));
				String targetLocaleName = rset.getString(5);
				targetLocaleInfo.setName(targetLocaleName);

				taskInfo.setTargetLocaleInfo(targetLocaleInfo);

				// information about the target page next
				TargetPageInfo targetPageInfo = new TargetPageInfo();
				targetPageInfo.setId(rset.getLong(6));
				String targetPageName = rset.getString(7);
				targetPageInfo.setName(targetPageName);

				taskInfo.setTargetPageInfo(targetPageInfo);

				// information about the Tuv Info next
				TuvInfo tuvInfo = new TuvInfo();
				tuvInfo.setDataType(rset.getString(8));
				tuvInfo.setId(rset.getLong(9));
				tuvInfo.setLocaleId(rset.getLong(10));
				tuvInfo.setSegment(rset.getString(11));
				tuvInfo.setJobId((rset.getLong(12)));

				taskInfo.setTuvInfo(tuvInfo);

				// add the Job info to the collection of jobInfos

				result.add(taskInfo);
			}
		}
		catch (Exception ex)
		{
			c_category.error("activity search error", ex);

			throw ex;
		}
		finally
		{
			try
			{
				if (rset != null)
				{
					rset.close();
				}
				if (stmt != null)
				{
					stmt.close();
				}
			}
			catch (Exception ex)
			{
				c_category.warn("Unable to close statement and result set", ex);
			}
		}

		return result;
	}

	private String addWFTLocaleClause(Collection<String> p_listOfLocales)
	{
		StringBuffer sb = new StringBuffer();
		sb.append(" and wf.target_locale_id IN (");

		int i = 0;
		Iterator<String> it = p_listOfLocales.iterator();
		while (it.hasNext())
		{
			String targetLocale = (String) it.next();

			sb.append("'");
			sb.append(targetLocale);
			sb.append("'");

			if (i < p_listOfLocales.size() - 1)
			{
				sb.append(", ");
			}

			i++;
		}

		sb.append(")");

		return sb.toString();
	}

	private String addTUVLocaleClause(Collection<String> p_listOfLocales)
	{
		StringBuffer sb = new StringBuffer();
		sb.append(" and tuv.locale_id IN (");

		int i = 0;
		Iterator<String> it = p_listOfLocales.iterator();
		while (it.hasNext())
		{
			String targetLocale = (String) it.next();

			sb.append("'");
			sb.append(targetLocale);
			sb.append("'");

			if (i < p_listOfLocales.size() - 1)
			{
				sb.append(", ");
			}

			i++;
		}

		sb.append(")");

		return sb.toString();
	}

	/**
     * '*' is the only allowed wildcard char the user can enter. '*' is escaped
     * using '\' to make '*' literal.
     * 
     * p_escapeChar is an escape character used in LIKE predicate to escape '%'
     * and '_' wildcards.
     */
	static private String makeWildcardQueryString(String p_queryString,
			char p_escapeChar)
	{
		//KNOWN BUG
		/* Current implementatin does not support the escape function */
		
		String pattern = p_queryString;
		String escape = String.valueOf(p_escapeChar);
		String asterisk = "*";
		String percent = "%";
		String underScore = "_";

		// remove the first and the last '*' from the string
		if (pattern.startsWith(asterisk))
		{
			pattern = pattern.substring(1);
		}
		if (pattern.endsWith(asterisk)
				&& pattern.charAt(pattern.length() - 2) != '\\')
		{
			pattern = pattern.substring(0, pattern.length() - 1);
		}

		// '&' -> '&&' (escape itself)
		pattern = Text.replaceString(pattern, escape, escape + escape);

		// '%' -> '&%' (escape wildcard char)
		pattern = Text.replaceString(pattern, percent, escape + percent);

		// '_' -> '&_' (escape wildcard char)
		pattern = Text.replaceString(pattern, underScore, escape + underScore);

		// '*' -> '%' (change wildcard) '\*' -> '*' (literal *)
		pattern = Text.replaceChar(pattern, '*', '%', '\\');

		// Add '%' to the beginning and the end of the string (because
		// the segment text is enclosed with <segment></segment> or
		// <localizable></localizable>)
		pattern = percent + pattern + percent;

		pattern = "<%>" + pattern + "</%>";

		if (c_category.isDebugEnabled())
		{
			c_category.debug("search + replace pattern = " + pattern);
		}

		return pattern;
	}

	static class SqlHolder
	{

		static StringBuilder sb = new StringBuilder();

		static
		{

			sb.append(" SELECT ");
			sb.append(" ti.task_id, ");
			sb.append(" j.name, ");
			sb.append(" jnode.name_,");
			sb.append(" wf.target_locale_id,");
			sb.append(" concat(l1.iso_lang_code,'_',l1.iso_country_code),");
			sb.append(" tp.id,");
			sb.append(" sp.external_page_id,");
			sb.append(" tu.data_type,");
			sb.append(" tuv.id,");
			sb.append(" tuv.locale_id,");
			sb.append(" tuv.segment_string,");
			sb.append(" j.id ");
			sb.append(" FROM task_info ti, ");
			sb.append(" job j,  ");
			sb.append(" workflow wf,  ");
			sb.append(" locale l1, ");
			sb.append(" source_page sp, ");
			sb.append(" target_page tp, ");
			sb.append(" target_page_leverage_group tplg,  ");
			sb.append(TuvQueryConstants.TU_TABLE_PLACEHOLDER).append(" tu,  ");
			sb.append(TuvQueryConstants.TUV_TABLE_PLACEHOLDER).append(" tuv, ");
			sb.append(" jbpm_taskinstance jti,");
			sb.append(" jbpm_task jtask,");
			sb.append(" jbpm_node jnode");
			sb.append(" WHERE ti.workflow_id = wf.iflow_instance_id  ");
			sb.append(" and wf.job_id = j.id  ");
			sb.append(" and ti.task_id = jnode.id_  ");
			sb.append(" and jnode.id_ = jtask.tasknode_");
			sb.append(" and jtask.id_ = jti.task_ ");
			sb.append(" and jti.start_ is not null");
			sb.append(" and jti.end_ is null ");
			sb.append(" and wf.target_locale_id = l1.id   ");
			sb.append(" and wf.iflow_instance_id = tp.workflow_iflow_instance_id ");
			sb.append(" and tp.source_page_id = sp.id  ");
			sb.append(" and tp.id = tplg.tp_id ");
			sb.append(" and tu.leverage_group_id = tplg.lg_id  ");
			sb.append(" and tuv.tu_id = tu.id ");
			sb.append(" and tuv.locale_id = wf.target_locale_id ");
		}

		//KNOWN BUG
		/*
         * The current implementation does not support the case sensitive
         * function because the setting in mysql is case insensitive
         */

		static String SEARCH_CASE_SENSITIVE = sb.toString()
				+ " AND tuv.segment_string like ? ";

		static String SEARCH_CASE_INSENSITIVE = sb.toString()
				+ " AND LOWER(tuv.segment_string) like LOWER(?) ";

	}
}
