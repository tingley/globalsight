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
package com.globalsight.ling.tm3.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.ling.tm3.core.persistence.BatchStatementBuilder;
import com.globalsight.ling.tm3.core.persistence.SQLUtil;
import com.globalsight.ling.tm3.core.persistence.StatementBuilder;
import com.globalsight.util.StringUtil;

class SharedTuStorage<T extends TM3Data> extends TuStorage<T>
{
    private long tmId;

	private final SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd");

    SharedTuStorage(StorageInfo<T> storage)
    {
        super(storage);
        this.tmId = storage.getTm().getId();
    }

    @Override
    public void addTuvs(Connection conn, TM3Tu<T> tu, List<TM3Tuv<T>> tuvs)
            throws SQLException
    {
        if (tuvs.size() == 0)
        {
            return;
        }
        for (TM3Tuv<T> tuv : tuvs)
        {
            tuv.setId(getStorage().getTuvId(conn));
        }
        BatchStatementBuilder sb = new BatchStatementBuilder("INSERT INTO ")
                .append(getStorage().getTuvTableName())
                .append(" (id, tuId, tmId, localeId, content, fingerprint, firstEventId, lastEventId, creationUser, creationDate, modifyUser, modifyDate) ")
                .append("VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        for (TM3Tuv<T> tuv : tuvs)
        {
            sb.addBatch(tuv.getId(), tu.getId(), tmId, tuv.getLocale().getId(),
                    tuv.getSerializedForm(), tuv.getFingerprint(), tuv
                            .getFirstEvent().getId(), tuv.getLatestEvent()
                            .getId(), tuv.getCreationUser(), tuv
                            .getCreationDate(), tuv.getModifyUser(), tuv
                            .getModifyDate());
        }
        SQLUtil.execBatch(conn, sb);

        // Store TUV extension attributes
		BatchStatementBuilder sb2 = new BatchStatementBuilder("INSERT INTO ")
				.append(getStorage().getTuvExtTableName())
				.append(" (tuvId, tuId, tmId, lastUsageDate, jobId, jobName, previousHash, nextHash, sid) ")
				.append("VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
        for (TM3Tuv<T> tuv : tuvs)
        {
			sb2.addBatch(tuv.getId(), tu.getId(), tmId, tuv.getLastUsageDate(),
					tuv.getJobId(), tuv.getJobName(), tuv.getPreviousHash(),
					tuv.getNextHash(), tuv.getSid());
        }
        SQLUtil.execBatch(conn, sb2);
    }

    @Override
    protected StatementBuilder getAttributeMatchWrapper(StatementBuilder inner,
            Map<TM3Attribute, String> attributes)
    {
        StatementBuilder sb = new StatementBuilder(
                "SELECT dummy.id, dummy.tuId FROM (").append(inner)
                .append(") as dummy, ").append(getStorage().getTuTableName())
                .append(" as tu");
        int i = 0;
        for (Map.Entry<TM3Attribute, String> attr : attributes.entrySet())
        {
            if (attr.getKey().getAffectsIdentity())
            {
                String alias = "attr" + i++;
                sb.append(" INNER JOIN ")
                        .append(getStorage().getAttrValTableName())
                        .append(" AS ").append(alias).append(" ON tu.id = ")
                        .append(alias).append(".tuId AND ").append(alias)
                        .append(".attrId = ?").addValue(attr.getKey().getId())
                        .append(" AND ").append(alias).append(".value = ?")
                        .addValue(attr.getValue());
            }
        }
        sb.append(" WHERE dummy.tuId = tu.id");
        return sb;
    }

    /**
     * Save custom attributes. This is the same across all table types.
     * 
     * @param conn
     * @param attributes
     * @throws SQLException
     */
    @Override
    void saveCustomAttributes(Connection conn, long tuId,
            Map<TM3Attribute, String> attributes) throws SQLException
    {
        if (attributes.isEmpty())
        {
            return;
        }
        BatchStatementBuilder sb = new BatchStatementBuilder()
                .append("INSERT INTO ")
                .append(getStorage().getAttrValTableName())
                .append(" (tmId, tuId, attrId, value) VALUES (?, ?, ?, ?)");
        for (Map.Entry<TM3Attribute, String> e : attributes.entrySet())
        {
            sb.addBatch(tmId, tuId, e.getKey().getId(), e.getValue());
        }
        SQLUtil.execBatch(conn, sb);
    }
    @Override
    public void saveTu(Connection conn, TM3Tu<T> tu) throws SQLException
    {
        tu.setId(getStorage().getTuId(conn));
        Map<TM3Attribute, Object> inlineAttributes = BaseTm
                .getInlineAttributes(tu.getAttributes());
        Map<TM3Attribute, String> customAttributes = BaseTm
                .getCustomAttributes(tu.getAttributes());
        StatementBuilder sb = new StatementBuilder("INSERT INTO ").append(
                getStorage().getTuTableName())
                .append(" (id, tmId, srcLocaleId");
        for (Map.Entry<TM3Attribute, Object> e : inlineAttributes.entrySet())
        {
            sb.append(", ").append(e.getKey().getColumnName());
        }
        sb.append(") VALUES (?, ?, ?").addValues(tu.getId(), tmId,
                tu.getSourceTuv().getLocale().getId());
        for (Map.Entry<TM3Attribute, Object> e : inlineAttributes.entrySet())
        {
			// SID attribute is stored into tm3_tuv_ext_shared_xx table since 8.6.4.
			if (".sid".equalsIgnoreCase(e.getKey().getName()))
			{
				sb.append(", ?").addValue(null);
			}
			else
			{
				sb.append(", ?").addValue(e.getValue());
			}
        }
        sb.append(")");
        SQLUtil.exec(conn, sb);

        addTuvs(conn, tu, tu.getAllTuv());

        saveCustomAttributes(conn, tu.getId(), customAttributes);
    }

    @Override
    public void updateTuvs(Connection conn, TM3Tu<T> tu, List<TM3Tuv<T>> tuvs,
            TM3Event event) throws SQLException
    {
        if (tuvs.size() == 0)
        {
            return;
        }
        BatchStatementBuilder sb = new BatchStatementBuilder("UPDATE ")
                .append(getStorage().getTuvTableName())
                .append(" SET content = ?, fingerprint = ?, modifyUser = ?, modifyDate = ?")
                .append(" WHERE id = ?");
        for (TM3Tuv<T> tuv : tuvs)
        {
			sb.addBatch(tuv.getSerializedForm(), tuv.getFingerprint(),
					tuv.getModifyUser(), tuv.getModifyDate(), tuv.getId());
        }
        SQLUtil.execBatch(conn, sb);

        // Store TUV extension attributes
        StatementBuilder sb2 = new StatementBuilder();
        for (TM3Tuv<T> tuv : tuvs)
        {
			sb2 = new StatementBuilder("SELECT tuvId FROM ")
					.append(getStorage().getTuvExtTableName())
					.append(" WHERE tuvId = ?").addValues(tuv.getId());
        	long tuvExtCount = SQLUtil.execCountQuery(conn, sb2);
            if (tuvExtCount == 0)
            {
				sb2 = new StatementBuilder("INSERT INTO ")
						.append(getStorage().getTuvExtTableName())
						.append(" (tuvId, tuId, tmId, lastUsageDate, jobId, jobName, previousHash, nextHash, sid) ")
						.append("VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")
						.addValues(tuv.getId(), tu.getId(), tmId,
								tuv.getLastUsageDate(), tuv.getJobId(),
								tuv.getJobName(), tuv.getPreviousHash(),
								tuv.getNextHash(), tuv.getSid());
				SQLUtil.exec(conn, sb2);
            }
            else
            {
				sb2 = new StatementBuilder("UPDATE ")
						.append(getStorage().getTuvExtTableName())
						.append(" SET sid = ?, lastUsageDate = ?, previousHash = ?, nextHash = ?, jobId = ?, jobName = ? ")
						.addValues(tuv.getSid(), tuv.getLastUsageDate(), tuv.getPreviousHash(), tuv.getNextHash(), tuv.getJobId(), tuv.getJobName())
						.append(" WHERE tuvId = ?").addValue(tuv.getId());
                SQLUtil.exec(conn, sb2);
            }
        }
    }

	@Override
	public long getTuCountByParamMap(Map<String, Object> paramMap)
			throws SQLException
	{
		Connection conn = null;
		try
		{
			long count = 0;
			conn = DbUtil.getConnection();
			StatementBuilder sb = new StatementBuilder();

			String stringId = null;
			String isRegex = null;
			if (paramMap != null)
			{
				stringId = (String) paramMap.get("stringId");
				isRegex = (String) paramMap.get("isRegex");
			}
			getCountSqlByParamMap(sb, paramMap);

			if (StringUtil.isNotEmpty(stringId))
			{
				count = filterTuBySid(conn, sb, stringId, isRegex).size();
			}
			else
			{
				count = SQLUtil.execCountQuery(conn, sb);
			}

			return count;
		}
		catch (Exception e)
		{
			throw new SQLException(e);
		}
		finally
		{
			DbUtil.silentReturnConnection(conn);
		}
	}

    private void appendAttributeSql(Set<String> jobAttributeSet, StatementBuilder sb,
    		boolean forTuv)
    {
    	String key;
		String value;
		int count = 0;
    	for(String keyAndValue: jobAttributeSet)
    	{
    		count++;
    		key = keyAndValue.substring(0,keyAndValue.indexOf(":"));
			value = keyAndValue.substring(keyAndValue.indexOf(":") + 1).replaceAll("'", "''");
			if(forTuv)
			{
				sb.append(" and tuv.tuId in ( ");
			}
			else
			{
				sb.append(" and tu.id in ( ");
			}
			sb.append(" select DISTINCT attrVal.tuId FROM " + getStorage().getAttrValTableName() + " AS attrVal,  TM3_ATTR AS attr WHERE attrVal.value = '" + value + "' and attr.name = '" + key + "' )");
    	}
    }

    @Override
    public long getTuvCount(Date start, Date end) throws SQLException
    {
        Connection conn = null;
        try
        {
            conn = DbUtil.getConnection();
            StatementBuilder sb = new StatementBuilder();
            if (start != null && end != null)
            {
                sb.append("SELECT COUNT(tuv.id) FROM ")
                        .append(getStorage().getTuvTableName())
                        .append(" as tuv, ").append("TM3_EVENTS as event ")
                        .append("WHERE tuv.lastEventId = event.id ")
                        .append("AND tuv.tmId = ? ").addValue(tmId)
                        .append("AND event.time >= ? AND event.time <= ?")
                        .addValues(parseStartDate(start), parseEndDate(end));
            }
            else
            {
                sb.append("SELECT COUNT(id) FROM ")
                        .append(getStorage().getTuvTableName())
                        .append(" WHERE tmId = ?").addValue(tmId);
            }
            return SQLUtil.execCountQuery(conn, sb);
        }
        catch (Exception e)
        {
            throw new SQLException(e);
        }
        finally
        {
            DbUtil.silentReturnConnection(conn);
        }
    }

    @Override
    public void deleteTus(Date start, Date end) throws SQLException
    {
        Connection conn = null;
        try
        {
            conn = DbUtil.getConnection();
            conn.setAutoCommit(false);
            StatementBuilder sb = new StatementBuilder();
            if (start != null && end != null)
            {
                sb.append("DELETE tu.* from ")
                        .append(getStorage().getTuTableName())
                        .append(" as tu, ")
                        .append(getStorage().getTuvTableName())
                        .append(" as tuv, ")
                        .append("TM3_EVENTS as event ")
                        .append("WHERE tu.id = tuv.tuId AND tuv.lastEventId = event.id ")
                        .append("AND tu.tmId = ? ").addValue(tmId)
                        .append(" AND event.time >= ? AND event.time <= ?")
                        .addValues(parseStartDate(start), parseEndDate(end));
            }
            else
            {
                sb.append("DELETE FROM ").append(getStorage().getTuTableName())
                        .append(" WHERE tmId = ?").addValue(tmId);
            }
            SQLUtil.exec(conn, sb);
            conn.commit();
        }
        catch (Exception e)
        {
            throw new SQLException(e);
        }
        finally
        {
            DbUtil.silentReturnConnection(conn);
        }
    }

    

    @Override
    public long getTuvCountByLocales(List<TM3Locale> localeList, Date start, Date end)
            throws SQLException
    {
        Connection conn = null;
        try
        {
        	String localeIds = getLocaleIds(localeList);
            conn = DbUtil.getConnection();
            StatementBuilder sb = new StatementBuilder();
            if (start != null && end != null)
			{
				sb.append("SELECT COUNT(tuv.id) FROM ")
						.append(getStorage().getTuvTableName())
						.append(" as tuv, ").append("TM3_EVENTS as event ")
						.append("WHERE tuv.lastEventId = event.id ")
						.append("AND tuv.tmId = ? ").addValue(tmId)
						.append(" AND event.time >= ? AND event.time <= ? ")
						.addValues(parseStartDate(start), parseEndDate(end))
						.append(" AND localeId in (").append(localeIds)
						.append(")");
			}
            else
			{
				sb.append("SELECT COUNT(id) FROM ")
						.append(getStorage().getTuvTableName())
						.append(" WHERE tmId = ? ").addValues(tmId)
						.append(" AND localeId in (").append(localeIds)
						.append(")");
			}
            return SQLUtil.execCountQuery(conn, sb);
        }
        catch (Exception e)
        {
            throw new SQLException(e);
        }
        finally
        {
            DbUtil.silentReturnConnection(conn);
        }
    }

    @Override
    public void deleteTuvsByLocale(TM3Locale locale) throws SQLException
    {
        Connection conn = null;
        try
        {
            conn = DbUtil.getConnection();
            conn.setAutoCommit(true);

            StatementBuilder sb = new StatementBuilder();
			sb.append("SELECT tuv.id FROM ")
					.append(getStorage().getTuvTableName())
					.append(" tuv, (SELECT id FROM ")
					.append(getStorage().getTuTableName())
					.append(" WHERE tmid = ?").addValue(tmId)
					.append(" AND srcLocaleId != ?").addValue(locale.getId())
					.append(") AS tu").append(" WHERE tu.id = tuv.tuId")
					.append(" AND tuv.tmid = ?").addValue(tmId)
					.append(" AND tuv.localeId = ?").addValue(locale.getId());
            List<Long> tuvIds = SQLUtil.execIdsQuery(conn, sb);

            List<List<Long>> tuvIdsInbatch = SQLUtil.toBatchList(tuvIds, 1000);

			SQLUtil.execBatchDelete(conn, "DELETE FROM "
					+ getStorage().getTuvExtTableName() + " WHERE tuvId IN ",
					tuvIdsInbatch);

			SQLUtil.execBatchDelete(conn, "DELETE FROM "
					+ getStorage().getTuvTableName() + " WHERE ID IN ",
					tuvIdsInbatch);
        }
        catch (Exception e)
        {
            throw new SQLException(e);
        }
        finally
        {
            DbUtil.silentReturnConnection(conn);
        }
    }

   

	@Override
    public long getTuvCountByAttributes(Map<TM3Attribute, Object> inlineAttrs,
            Map<TM3Attribute, String> customAttrs, Date start, Date end)
            throws SQLException
    {
        Connection conn = null;
        try
        {
            conn = DbUtil.getConnection();
            StatementBuilder sb = new StatementBuilder();
            if (start != null && end != null)
            {
                sb.append("SELECT COUNT(DISTINCT tuv.id) FROM ")
                        .append(getStorage().getTuvTableName())
                        .append(" as tuv, ")
                        .append(getStorage().getTuTableName())
                        .append(" as tu, ").append("TM3_EVENTS as event ");
                getStorage().attributeJoinFilter(sb, "tuv.tuId", customAttrs);
                sb.append(
                        "WHERE tuv.tuid=tu.id AND tuv.lastEventId = event.id ")
                        .append("AND tuv.tmId = ? ").addValue(tmId)
                        .append(" AND event.time >= ? AND event.time <= ? ")
                        .addValues(parseStartDate(start), parseEndDate(end));
            }
            else
            {
                sb.append("SELECT COUNT(tuv.id) FROM ")
                        .append(getStorage().getTuvTableName())
                        .append(" as tuv, ")
                        .append(getStorage().getTuTableName())
                        .append(" as tu ");
                getStorage().attributeJoinFilter(sb, "tuv.tuId", customAttrs);
                sb.append(" WHERE tuv.tuid=tu.id AND tuv.tmId = ?").addValue(
                        tmId);
            }
            
            getInlineAttrsSql(sb,inlineAttrs);

            return SQLUtil.execCountQuery(conn, sb);
        }
        catch (Exception e)
        {
            throw new SQLException(e);
        }
        finally
        {
            DbUtil.silentReturnConnection(conn);
        }
    }

	@Override
	public List<TM3Tu<T>> getTuPageByParamMap(long startId, int count,
			Map<String, Object> paramMap) throws SQLException
	{
		Connection conn = null;
		try
		{
			List<Long> ids = new ArrayList<Long>();
			conn = DbUtil.getConnection();
			StatementBuilder sb = new StatementBuilder();
			String stringId = null;
			String isRegex = null;
			if (paramMap != null)
			{
				stringId = (String) paramMap.get("stringId");
				isRegex = (String) paramMap.get("isRegex");
			}
			getSqlByParamMap(sb, paramMap, startId, count);
			if (StringUtil.isNotEmpty(stringId))
			{
				ids = filterTuBySid(conn, sb, stringId, isRegex);
			}
			else
			{
				ids = getIdList(conn, sb);
			}

			return getTu(ids, false);
		}
		catch (Exception e)
		{
			throw new SQLException(e);
		}
		finally
		{
			DbUtil.silentReturnConnection(conn);
		}
	}

    @Override
    public Set<TM3Locale> getTuvLocales() throws SQLException
    {
        Connection conn = null;
        try
        {
            conn = DbUtil.getConnection();
            return loadLocales(SQLUtil.execIdsQuery(
                    conn,
                    new StatementBuilder()
                            .append("SELECT DISTINCT(localeId) FROM ")
                            .append(getStorage().getTuvTableName())
                            .append(" WHERE tmId = ?").addValue(tmId)));
        }
        catch (Exception e)
        {
            throw new SQLException(e);
        }
        finally
        {
            DbUtil.silentReturnConnection(conn);
        }
    }

    @Override
    public List<Object> getAllAttrValues(TM3Attribute attr) throws SQLException
    {
        Connection conn = null;
        try
        {
            conn = DbUtil.getConnection();
            if (attr.isInline())
            {
                return SQLUtil.execObjectsQuery(
                        conn,
                        new StatementBuilder().append("SELECT DISTINCT(")
                                .append(attr.getColumnName()).append(") FROM ")
                                .append(getStorage().getTuTableName())
                                .append(" WHERE tmId = ?").addValues(tmId)
                                .append(" AND ").append(attr.getColumnName())
                                .append(" IS NOT NULL"));
            }
            else
            {
                return SQLUtil.execObjectsQuery(
                        conn,
                        new StatementBuilder()
                                .append("SELECT DISTINCT(value) FROM ")
                                .append(getStorage().getAttrValTableName())
                                .append(" WHERE tmId = ? AND attrId = ?")
                                .addValues(tmId, attr.getId()));
            }
        }
        catch (Exception e)
        {
            throw new SQLException(e);
        }
        finally
        {
            DbUtil.silentReturnConnection(conn);
        }
    }

    @Override
    protected long getTuIdByTuvId(Long tuvId) throws SQLException
    {
        Connection conn = null;
        try
        {
            conn = DbUtil.getConnection();
            return SQLUtil.execCountQuery(conn, new StatementBuilder(
                    "SELECT tuId FROM ").append(getStorage().getTuvTableName())
                    .append(" WHERE tmId = ? AND id = ?")
                    .addValues(tmId, tuvId));
        }
        catch (Exception e)
        {
            throw new SQLException(e);
        }
        finally
        {
            DbUtil.silentReturnConnection(conn);
        }
    }

    private String parseStartDate(Date start)
    {
        String startDate = dateFormat.format(start);
        if (StringUtil.isNotEmpty(startDate))
            return startDate + " 00:00:00";
        else
            return null;
    }

    private String parseEndDate(Date end)
    {
        String endDate = dateFormat.format(end);
        if (StringUtil.isNotEmpty(endDate))
            return endDate + " 23:59:59";
        else
            return null;
    }

	private String getLocaleIds(List<TM3Locale> localeList)
	{
		String localeIds = "";
		if (localeList != null && localeList.size() > 0)
		{
			for (int i = 0; i < localeList.size(); i++)
			{
				localeIds += localeList.get(i).getId() + ",";
			}
		}
		if (StringUtil.isNotEmpty(localeIds) && localeIds.endsWith(","))
		{
			localeIds = localeIds.substring(0, localeIds.lastIndexOf(","));
		}
		return localeIds;
	}
	
	private void getInlineAttrsSql(StatementBuilder sb,
			Map<TM3Attribute, Object> inlineAttrs)
	{
		for (Map.Entry<TM3Attribute, Object> e : inlineAttrs.entrySet())
		{
			if (e.getKey().getColumnName().equalsIgnoreCase("project"))
			{
				String projectValue = getProjects((String) e.getValue());
				sb.append(" AND tu.").append(e.getKey().getColumnName())
						.append(" in (").append(projectValue).append(")");
			}
			else
			{
				sb.append(" AND tu.").append(e.getKey().getColumnName())
						.append(" = ?").addValues(e.getValue());
			}
		}
	}
	
	private String getProjects(String values)
	{
		String[] valueArr = values.split(",");
		String projectValue = "";
		for (String value : valueArr)
		{
			projectValue += "'" + value + "'" + ",";
		}
		return projectValue.substring(0, projectValue.lastIndexOf(","));
	}

	private void getCountSqlByParamMap(StatementBuilder sb,
			Map<String, Object> paramMap)
	{
		String stringId = null;
		if (paramMap != null)
		{
			stringId = (String) paramMap.get("stringId");
		}

		// If SID is not empty, query "tm3_tuv_ext_shared_xx" table...
		if (StringUtil.isNotEmpty(stringId))
		{
			sb.append("SELECT DISTINCT tu.id AS tuId, ext.sid AS sid FROM ");
		}
		else
		{
			sb.append("SELECT COUNT(DISTINCT tu.id)  FROM ");
		}
		sb.append(getStorage().getTuvTableName()).append(" as tuv ");
		
		getParameterSql(sb, paramMap,stringId);
	}
	
	private void getSqlByParamMap(StatementBuilder sb,
			Map<String, Object> paramMap, long startId, int count)
	{
		String stringId = null;
		if (paramMap != null)
		{
			stringId = (String) paramMap.get("stringId");
		}

		// If SID is not empty, query "tm3_tuv_ext_shared_xx" table...
		if (StringUtil.isNotEmpty(stringId))
		{
			sb.append("SELECT DISTINCT tu.id AS tuId, ext.sid AS sid FROM ");
		}
		else
		{
			sb.append("SELECT DISTINCT tu.id AS tuId FROM ");
		}
		
		sb.append(getStorage().getTuvTableName()).append(" as tuv ");
		
		getParameterSql(sb, paramMap,stringId);
		
		if (startId != -1 && count != -1)
		{
			sb.append(" AND tuv.tuId > ? ORDER BY tu.id ASC LIMIT ?")
					.addValues(startId, count);
		}
	}

	@SuppressWarnings("unchecked")
	private void getParameterSql(StatementBuilder sb,
			Map<String, Object> paramMap,String stringId)
	{
		Map<TM3Attribute, Object> inlineAttrs = null;
		Map<TM3Attribute, String> customAttrs = null;
		if (checkContainExtTable(paramMap))
		{
			sb.append(",").append(getStorage().getTuvExtTableName())
					.append(" AS ext ");
		}
		sb.append(",").append(getStorage().getTuTableName()).append(" AS tu");
		if (paramMap != null)
		{
			inlineAttrs = (Map<TM3Attribute, Object>) paramMap
					.get("inlineAttrs");
			customAttrs = (Map<TM3Attribute, String>) paramMap
					.get("customAttrs");
			if (inlineAttrs != null && !inlineAttrs.isEmpty())
			{
				getStorage().attributeJoinFilter(sb, "tu.id", customAttrs);
			}
		}
		sb.append(" WHERE tu.id = tuv.tuId ");
		sb.append(" AND  tu.tmId = ? ").addValue(tmId);
		
		if (paramMap != null)
		{
			Set<String> jobAttributeSet = (Set<String>) paramMap
					.get("jobAttributeSet");
			String createUser = (String) paramMap.get("createUser");
			String modifyUser = (String) paramMap.get("modifyUser");
			String modifyAfter = (String) paramMap.get("modifyAfter");
			String modifyBefore = (String) paramMap.get("modifyBefore");
			String createdAfter = (String) paramMap.get("createdAfter");
			String createdBefore = (String) paramMap.get("createdBefore");
			String tuIds = (String) paramMap.get("tuIds");
			String jobId = (String) paramMap.get("jobId");
			String lastUsageAfter = (String) paramMap.get("lastUsageAfter");
			String lastUsageBefore = (String) paramMap.get("lastUsageBefore");
			List localeIdList = (List) paramMap.get("language");
			String localeIds = null;
			if (localeIdList != null)
			{
				localeIds = getLocaleIds(localeIdList);
			}

			if (checkContainExtTable(paramMap))
			{
				sb.append(" AND tuv.id = ext.tuvId ");
				sb.append(" AND tu.id = ext.tuId ");
			}
			
			if (StringUtil.isNotEmpty(lastUsageAfter)
					&& StringUtil.isNotEmpty(lastUsageBefore))
			{
				sb.append(
						" AND ext.lastUsageDate >= ? AND ext.lastUsageDate <= ?")
						.addValues(parseStartDate(new Date(lastUsageAfter)),
								parseEndDate(new Date(lastUsageBefore)));
			}

			if (StringUtil.isNotEmpty(jobId))
			{
				sb.append(" AND ext.jobId in (").append(jobId).append(")");
			}

			if (StringUtil.isNotEmpty(stringId))
			{
				sb.append(" AND ext.sid IS NOT NULL");
			}

			if (StringUtil.isNotEmpty(createdAfter)
					&& StringUtil.isNotEmpty(createdBefore))
			{
				sb.append(
						" AND tuv.creationDate >= ? AND tuv.creationDate <= ?")
						.addValues(parseStartDate(new Date(createdAfter)),
								parseEndDate(new Date(createdBefore)));
			}
			if (StringUtil.isNotEmpty(modifyAfter)
					&& StringUtil.isNotEmpty(modifyBefore))
			{
				sb.append(" AND tuv.modifyDate >= ? AND tuv.modifyDate <= ?")
						.addValues(parseStartDate(new Date(modifyAfter)),
								parseEndDate(new Date(modifyBefore)));
			}

			if (StringUtil.isNotEmpty(createUser))
			{
				sb.append(" AND tuv.creationUser = ? ").addValues(createUser);
			}
			if (StringUtil.isNotEmpty(modifyUser))
			{
				sb.append(" AND tuv.modifyUser = ? ").addValues(modifyUser);
			}
			if (StringUtil.isNotEmpty(tuIds))
			{
				getTuIds(sb, tuIds);
			}

			if (jobAttributeSet != null && jobAttributeSet.size() > 0)
			{
				appendAttributeSql(jobAttributeSet, sb, true);
			}

			if (StringUtil.isNotEmpty(localeIds))
			{
				sb.append(" AND tuv.localeId in (").append(localeIds)
						.append(") ");
			}
			if (inlineAttrs != null && !inlineAttrs.isEmpty())
			{
				getInlineAttrsSql(sb, inlineAttrs);
			}
		}
	}

	private boolean checkContainExtTable(Map<String, Object> paramMap)
	{
		if (paramMap != null)
		{
			String stringId = (String) paramMap.get("stringId");
			String jobId = (String) paramMap.get("jobId");
			String lastUsageAfter = (String) paramMap.get("lastUsageAfter");
			String lastUsageBefore = (String) paramMap.get("lastUsageBefore");
			if (StringUtil.isNotEmpty(stringId)
					|| (StringUtil.isNotEmpty(lastUsageAfter) && StringUtil
							.isNotEmpty(lastUsageBefore))
					|| StringUtil.isNotEmpty(jobId))
			{
				return true;
			}
		}
		return false;
	}
	
	private void getTuIds(StatementBuilder sb, String tuIds)
	{
		String tuIdStr = "";
		String[] tuIdsArr = tuIds.split(",");
		int count = 0;
		sb.append(" AND (");
		for (String tuId : tuIdsArr)
		{
			if (tuId.contains("-"))
			{
				String[] tuIdArr = tuId.split("-");
				if (count == 0)
				{
					sb.append(" tuv.tuId >= ? ").addValue(tuIdArr[0])
							.append(" AND tuv.tuId <= ? ").addValue(tuIdArr[1]);
				}
				else
				{
					sb.append(" OR tuv.tuId >= ? ").addValue(tuIdArr[0])
							.append(" AND tuv.tuId <= ? ").addValue(tuIdArr[1]);
				}
				count++;
			}
			else
			{
				tuIdStr += tuId + ",";
			}
		}
		if (StringUtil.isNotEmpty(tuIdStr))
		{
			if (count == 0)
			{
				sb.append(" tuv.tuId in (")
						.append(tuIdStr.substring(0, tuIdStr.lastIndexOf(",")))
						.append(") ");
			}
			else
			{
				sb.append(" OR tuv.tuId in (")
						.append(tuIdStr.substring(0, tuIdStr.lastIndexOf(",")))
						.append(") ");
			}
		}
		sb.append(") ");
	}

	private List<Long> filterTuBySid(Connection conn, StatementBuilder sb,
			String stringId, String isRegex) throws SQLException
	{
		List<Long> tuIdList = new ArrayList<Long>();
		Map<Long, String> map = new HashMap<Long, String>();
		PreparedStatement ps = sb.toPreparedStatement(conn);
		ResultSet rs = ps.executeQuery();
		while (rs.next())
		{
			map.put(rs.getLong(1), rs.getString(2));
		}
		rs.close();
		ps.close();

		boolean regex = Boolean.parseBoolean(isRegex);
		Pattern pattern = Pattern.compile(stringId);
		Matcher matcher = null;
		Iterator it = map.keySet().iterator();
		while (it.hasNext())
		{
			long key = Long.valueOf(String.valueOf(it.next()));
			String sid = map.get(key);
			if (regex)
			{
				matcher = pattern.matcher(sid);
				if (matcher.matches())
				{
					tuIdList.add(key);
				}
			}
			else
			{
				if (stringId.equalsIgnoreCase(sid))
				{
					tuIdList.add(key);
				}
			}
		}

		return tuIdList;
	}

	private List<Long> getIdList(Connection conn, StatementBuilder sb)
			throws SQLException
	{
		List<Long> idList = new ArrayList<Long>();
		PreparedStatement ps = sb.toPreparedStatement(conn);
		ResultSet rs = ps.executeQuery();
		while (rs.next())
		{
			idList.add(rs.getLong(1));
		}
		ps.close();
		return idList;
	}

	@Override
	public long getAllTuCount() throws SQLException
	{
		Connection conn = null;
		try
		{
			conn = DbUtil.getConnection();
			StatementBuilder sb = new StatementBuilder(
					"SELECT COUNT(id) FROM ")
					.append(getStorage().getTuTableName())
					.append(" WHERE tmId = ? ").addValue(tmId);
			return SQLUtil.execCountQuery(conn, sb);
		}
		catch (Exception e)
		{
			throw new SQLException(e);
		}
		finally
		{
			DbUtil.silentReturnConnection(conn);
		}
	}

	@Override
	public long getTuCountByLocale(Long localeId) throws SQLException
	{
		Connection conn = null;
		try
		{
			conn = DbUtil.getConnection();
			// It is for performance to inner join TU table.
			StatementBuilder sb = new StatementBuilder(
					"SELECT COUNT(DISTINCT tu.id) FROM ")
					.append(getStorage().getTuTableName()).append(" as tu, ")
					.append(getStorage().getTuvTableName()).append(" as tuv ")
					.append(" WHERE tu.id = tuv.tuId ")
					.append(" AND tu.tmId = ?").addValue(tmId)
					.append(" AND tuv.localeId = ?").addValue(localeId);
			return SQLUtil.execCountQuery(conn, sb);
		}
		catch (Exception e)
		{
			throw new SQLException(e);
		}
		finally
		{
			DbUtil.silentReturnConnection(conn);
		}
	}
	
	
}
