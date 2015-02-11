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
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.ling.tm3.core.persistence.BatchStatementBuilder;
import com.globalsight.ling.tm3.core.persistence.SQLUtil;
import com.globalsight.ling.tm3.core.persistence.StatementBuilder;

class DedicatedTuStorage<T extends TM3Data> extends TuStorage<T>
{

    DedicatedTuStorage(StorageInfo<T> storage)
    {
        super(storage);
    }

    /**
     * Persist a newly created TU, or save updates to an existing one. This will
     * set the TM3Tu id field if it is not already set.
     * 
     * @param tu
     * @throws SQLException
     */
    @Override
    public void saveTu(Connection conn, TM3Tu<T> tu) throws SQLException
    {
        tu.setId(getStorage().getTuId(conn));
        Map<TM3Attribute, Object> inlineAttributes = BaseTm
                .getInlineAttributes(tu.getAttributes());
        Map<TM3Attribute, String> customAttributes = BaseTm
                .getCustomAttributes(tu.getAttributes());
        StatementBuilder sb = new StatementBuilder("INSERT INTO ").append(
                getStorage().getTuTableName()).append(" (id, srcLocaleId");
        for (Map.Entry<TM3Attribute, Object> e : inlineAttributes.entrySet())
        {
            sb.append(", ").append(e.getKey().getColumnName());
        }
        sb.append(") VALUES (?, ?").addValues(tu.getId(),
                tu.getSourceTuv().getLocale().getId());
        for (Map.Entry<TM3Attribute, Object> e : inlineAttributes.entrySet())
        {
            sb.append(", ?").addValue(e.getValue());
        }
        sb.append(")");
        SQLUtil.exec(conn, sb);
        addTuvs(conn, tu, tu.getAllTuv());
        saveCustomAttributes(conn, tu.getId(), customAttributes);
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
                .append(" (id, tuId, localeId, content, fingerprint, firstEventId, lastEventId) ")
                .append("VALUES (?, ?, ?, ?, ?, ?, ?)");
        for (TM3Tuv<T> tuv : tuvs)
        {
            sb.addBatch(tuv.getId(), tu.getId(), tuv.getLocale().getId(), tuv
                    .getSerializedForm(), tuv.getFingerprint(), tuv
                    .getFirstEvent().getId(), tuv.getLatestEvent().getId());
        }
        SQLUtil.execBatch(conn, sb);
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
                .append(" SET content = ?, fingerprint = ?, lastEventId = ? WHERE id = ?");
        for (TM3Tuv<T> tuv : tuvs)
        {
            sb.addBatch(tuv.getSerializedForm(), tuv.getFingerprint(),
                    event.getId(), tuv.getId());
        }
        SQLUtil.execBatch(conn, sb);
    }

    /**
     * Save inline attributes. This is the same across all table types.
     * 
     * @param conn
     * @param attributes
     * @throws SQLException
     */
    @Override
    void saveInlineAttributes(Connection conn, long tuId,
            Map<TM3Attribute, Object> attributes) throws SQLException
    {
        if (attributes.isEmpty())
        {
            return;
        }
        StatementBuilder sb = new StatementBuilder("UPDATE ").append(
                getStorage().getTuTableName()).append(" SET ");
        boolean first = true;
        for (Map.Entry<TM3Attribute, Object> e : attributes.entrySet())
        {
            if (!first)
            {
                sb.append(", ");
                first = true;
            }
            ;
            sb.append(e.getKey().getColumnName() + " = ?").addValue(
                    e.getValue());
        }
        sb.append(" WHERE id = ?").addValue(tuId);
        SQLUtil.exec(conn, sb);
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
                .append(" (tuId, attrId, value) VALUES (?, ?, ?)");
        for (Map.Entry<TM3Attribute, String> e : attributes.entrySet())
        {
            sb.addBatch(tuId, e.getKey().getId(), e.getValue());
        }
        SQLUtil.execBatch(conn, sb);
    }

    @Override
    protected List<TuData<T>> getTuData(List<Long> ids, boolean locking)
            throws SQLException
    {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ").append("id, srcLocaleId");
        for (TM3Attribute attr : getStorage().getInlineAttributes())
        {
            sb.append(", ").append(attr.getColumnName());
        }
        sb.append(" FROM ").append(getStorage().getTuTableName())
                .append(" WHERE id IN").append(SQLUtil.longGroup(ids))
                .append("ORDER BY id");
        if (locking)
        {
            sb.append(" FOR UPDATE");
        }

        Connection conn = null;
        try
        {
            conn = DbUtil.getConnection();
            Statement s = conn.createStatement();
            ResultSet rs = SQLUtil.execQuery(s, sb.toString());

            List<TuData<T>> tuDatas = new ArrayList<TuData<T>>();
            while (rs.next())
            {
                TuData<T> tu = new TuData<T>();
                tu.id = rs.getLong(1);
                tu.srcLocaleId = rs.getLong(2);
                int pos = 3;
                for (TM3Attribute attr : getStorage().getInlineAttributes())
                {
                    Object val = rs.getObject(pos++);
                    if (val != null)
                    {
                        tu.attrs.put(attr, val);
                    }
                }
                tuDatas.add(tu);
            }
            s.close();
            return tuDatas;
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
                    .append(" WHERE id = ?").addValue(tuvId));
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
    protected void loadTuvs(List<Long> tuIds, List<TuData<T>> data,
            boolean locking) throws SQLException
    {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ")
                .append("tuId, id, localeId, fingerprint, content, firstEventId, lastEventId")
                .append(" FROM ").append(getStorage().getTuvTableName())
                .append(" WHERE tuId IN").append(SQLUtil.longGroup(tuIds))
                .append("ORDER BY tuId");
        if (locking)
        {
            sb.append(" FOR UPDATE");
        }
        Connection conn = null;
        try
        {
            conn = DbUtil.getConnection();
            Statement s = conn.createStatement();
            ResultSet rs = SQLUtil.execQuery(s, sb.toString());

            // In order to avoid a hidden ResultSet in the data factory
            // invalidating our current one, we must defer looking up
            // the locales until after we've read all of the data.
            List<TuvData<T>> rawTuvs = new ArrayList<TuvData<T>>();
            while (rs.next())
            {
                rawTuvs.add(new TuvData<T>(rs.getLong(1), rs.getLong(2), rs
                        .getLong(3), rs.getLong(4), rs.getString(5), rs
                        .getLong(6), rs.getLong(7)));
            }
            s.close();

            Iterator<TuData<T>> tus = data.iterator();
            TuData<T> current = null;
            for (TuvData<T> rawTuv : rawTuvs)
            {
                current = advanceToTu(current, tus, rawTuv.tuId);
                if (current == null)
                {
                    throw new IllegalStateException("Couldn't find tuId for "
                            + rawTuv.tuId);
                }
                // "tuId, id, localeId, fingerprint, content";
                TM3Tuv<T> tuv = createTuv(rawTuv);
                tuv.setStorage(this);
                if (tuv.getLocale().getId() == current.srcLocaleId)
                {
                    current.srcTuv = tuv;
                }
                else
                {
                    current.tgtTuvs.add(tuv);
                }
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
    protected void loadAttrs(List<Long> tuIds, List<TuData<T>> data,
            boolean locking) throws SQLException
    {
        StatementBuilder sb = new StatementBuilder();
        sb.append("SELECT tuId, attrId, value FROM ")
                .append(getStorage().getAttrValTableName())
                .append(" WHERE tuid IN (?").addValue(tuIds.get(0));
        for (int i = 1; i < tuIds.size(); i++)
        {
            sb.append(",?").addValue(tuIds.get(i));
        }
        sb.append(") ORDER BY tuId");
        if (locking)
        {
            sb.append(" FOR UPDATE");
        }

        Connection conn = null;
        try
        {
            conn = DbUtil.getConnection();
            PreparedStatement ps = sb.toPreparedStatement(conn);
            ResultSet rs = SQLUtil.execQuery(ps);

            Iterator<TuData<T>> tus = data.iterator();
            TuData<T> current = null;
            while (rs.next())
            {
                long tuId = rs.getLong(1);
                current = advanceToTu(current, tus, tuId);
                if (current == null)
                {
                    throw new IllegalStateException("Couldn't find tuId for "
                            + tuId);
                }
                current.attrs.put(getAttributeById(rs.getLong(2)),
                        rs.getString(3));
            }
            ps.close();
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
    protected StatementBuilder getExactMatchStatement(T key,
            TM3Locale srcLocale, Set<? extends TM3Locale> targetLocales,
            Map<TM3Attribute, Object> inlineAttrs, boolean lookupTarget)
    {
        StatementBuilder sb = new StatementBuilder()
                .append("SELECT tuv.id, tuv.tuId FROM ")
                .append(getStorage().getTuvTableName()).append(" as tuv");
        if (!inlineAttrs.isEmpty() || !lookupTarget)
        {
            sb.append(" join " + getStorage().getTuTableName() + " as tu")
                    .append(" on tu.id = tuv.tuId");
        }
        sb.append(" WHERE tuv.fingerprint = ? AND tuv.localeId = ? ")
                .addValues(key.getFingerprint(), srcLocale.getId());
        if (!lookupTarget)
        {
            sb.append("AND tu.srcLocaleId = ? ").addValue(srcLocale.getId());
        }
        if (!inlineAttrs.isEmpty())
        {
            for (Map.Entry<TM3Attribute, Object> e : inlineAttrs.entrySet())
            {
                if (e.getKey().getAffectsIdentity())
                {
                    sb.append(" AND tu." + e.getKey().getColumnName() + " = ?");
                    sb.addValue(e.getValue());
                }
            }
        }
        if (targetLocales != null)
        {
            // an exists subselect seems simpler, but mysql bug 46947 causes
            // exists subselects to take locks even in repeatable read
            List<Long> targetLocaleIds = new ArrayList<Long>();
            for (TM3Locale locale : targetLocales)
            {
                targetLocaleIds.add(locale.getId());
            }
            sb = new StatementBuilder()
                    .append("SELECT DISTINCT result.* FROM (").append(sb)
                    .append(") AS result, ")
                    .append(getStorage().getTuvTableName() + " AS targetTuv ")
                    .append("WHERE ")
                    .append("targetTuv.tuId = result.tuId AND ")
                    .append("targetTuv.localeId IN")
                    .append(SQLUtil.longGroup(targetLocaleIds));
        }
        return sb;
    }

    /**
     * Generate SQL to apply attribute matching as a filter to another result
     * (which is nested as a table, to avoid MySQL's troubles with subquery
     * performance).
     */
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
                        .append(".attrId = ? AND ").append(alias)
                        .append(".value = ?")
                        .addValues(attr.getKey().getId(), attr.getValue());
            }
        }
        sb.append(" WHERE dummy.tuId = tu.id");
        return sb;
    }

    @Override
    public long getTuCount(Date start, Date end) throws SQLException
    {
        Connection conn = null;
        try
        {
            conn = DbUtil.getConnection();
            StatementBuilder sb = new StatementBuilder();
            if (start != null && end != null)
            {
                sb.append("SELECT COUNT(DISTINCT tu.id) FROM ")
                        .append(getStorage().getTuTableName())
                        .append(" as tu, ")
                        .append(getStorage().getTuvTableName())
                        .append(" as tuv, ")
                        .append("TM3_EVENTS as event ")
                        .append("WHERE tu.id = tuv.tuId AND tuv.lastEventId = event.id ")
                        .append("AND event.time >= ? AND event.time <= ?")
                        .addValues(start, end);
            }
            else
            {
                sb.append("SELECT COUNT(id) FROM ").append(
                        getStorage().getTuTableName());
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
                        .append("AND event.time >= ? AND event.time <= ?")
                        .addValues(start, end);
            }
            else
            {
                sb.append("SELECT COUNT(id) FROM ").append(
                        getStorage().getTuvTableName());
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
                        .append("AND event.time >= ? AND event.time <= ?")
                        .addValues(start, end);
            }
            else
            {
                sb.append("DELETE FROM ").append(getStorage().getTuTableName());
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
    public long getTuCountByLocale(TM3Locale locale, Date start, Date end)
            throws SQLException
    {
        Connection conn = null;
        try
        {
            conn = DbUtil.getConnection();
            StatementBuilder sb = new StatementBuilder();
            if (start != null && end != null)
            {
                sb.append("SELECT COUNT(DISTINCT tu.id) FROM ")
                        .append(getStorage().getTuTableName())
                        .append(" as tu, ")
                        .append(getStorage().getTuvTableName())
                        .append(" as tuv, ")
                        .append("TM3_EVENTS as event ")
                        .append("WHERE tu.id = tuv.tuId AND tuv.lastEventId = event.id ")
                        .append("AND event.time >= ? AND event.time <= ? ")
                        .addValues(start, end).append("AND localeId = ?")
                        .addValue(locale.getId());
            }
            else
            {
                sb.append("SELECT COUNT(DISTINCT tuId) FROM ")
                        .append(getStorage().getTuvTableName())
                        .append(" WHERE localeId = ?").addValue(locale.getId());
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
    public long getTuvCountByLocale(TM3Locale locale, Date start, Date end)
            throws SQLException
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
                        .append("AND event.time >= ? AND event.time <= ? ")
                        .addValues(start, end).append("AND tuv.localeId = ?")
                        .addValue(locale.getId());
            }
            else
            {
                sb.append("SELECT COUNT(id) FROM ")
                        .append(getStorage().getTuvTableName())
                        .append(" WHERE localeId = ?").addValue(locale.getId());
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
            conn.setAutoCommit(false);
            StatementBuilder sb = new StatementBuilder().append("DELETE FROM ")
                    .append(getStorage().getTuvTableName())
                    .append(" WHERE localeId = ?").addValue(locale.getId())
                    .append(" AND (select srcLocaleId from ")
                    .append(getStorage().getTuTableName())
                    .append(" WHERE id = tuId) != localeId");
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

    //
    // AttributeDataHandle
    //
    @Override
    public long getTuCountByAttributes(Map<TM3Attribute, Object> inlineAttrs,
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
                sb.append("SELECT COUNT(DISTINCT tu.id) FROM ")
                        .append(getStorage().getTuTableName())
                        .append(" as tu ")
                        .append(getStorage().getTuvTableName())
                        .append(" as tuv, ").append("TM3_EVENTS as event ");
                getStorage().attributeJoinFilter(sb, "tu.id", customAttrs);
                sb.append(
                        " WHERE tu.id = tuv.tuId AND tuv.lastEventId = event.id ")
                        .append("AND event.time >= ? AND event.time <= ? ")
                        .addValues(start, end);
            }
            else
            {
                sb.append("SELECT COUNT(DISTINCT tu.id) FROM ")
                        .append(getStorage().getTuTableName())
                        .append(" as tu ");
                getStorage().attributeJoinFilter(sb, "tu.id", customAttrs);
                sb.append(" WHERE 1");
            }
            for (Map.Entry<TM3Attribute, Object> e : inlineAttrs.entrySet())
            {
                sb.append(" AND tu.").append(e.getKey().getColumnName())
                        .append(" = ?").addValues(e.getValue());
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
                sb.append(" WHERE tuv.lastEventId = event.id ")
                        .append("AND event.time >= ? AND event.time <= ? ")
                        .addValues(start, end);
            }
            else
            {
                sb.append("SELECT COUNT(tuv.id) FROM ")
                        .append(getStorage().getTuvTableName())
                        .append(" as tuv, ")
                        .append(getStorage().getTuTableName())
                        .append(" as tu ");
                getStorage().attributeJoinFilter(sb, "tuv.tuId", customAttrs);
                sb.append(" WHERE 1");
            }
            for (Map.Entry<TM3Attribute, Object> e : inlineAttrs.entrySet())
            {
                sb.append(" AND tu.").append(e.getKey().getColumnName())
                        .append(" = ?").addValues(e.getValue());
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

    //
    // Paging
    //

    @Override
    public List<TM3Tu<T>> getTuPage(long startId, int count, Date start,
            Date end) throws SQLException
    {
        Connection conn = null;
        try
        {
            conn = DbUtil.getConnection();
            StatementBuilder sb = new StatementBuilder();
            if (start != null && end != null)
            {
                sb.append("SELECT DISTINCT tuId FROM ")
                        .append(getStorage().getTuvTableName())
                        .append(" as tuv, ").append("TM3_EVENTS as event ")
                        .append("WHERE tuv.lastEventId = event.id ")
                        .append("AND event.time >= ? AND event.time <= ? ")
                        .addValues(start, end)
                        .append("AND tuId > ? ORDER BY tuId ASC LIMIT ?")
                        .addValues(startId, count);
            }
            else
            {
                sb.append("SELECT id FROM ")
                        .append(getStorage().getTuTableName())
                        .append(" WHERE id > ? ORDER BY id ASC LIMIT ?")
                        .addValues(startId, count);
            }
            return getTu(SQLUtil.execIdsQuery(conn, sb), false);
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
    public List<TM3Tu<T>> getTuPageByLocale(long startId, int count,
            TM3Locale locale, Date start, Date end) throws SQLException
    {
        Connection conn = null;
        try
        {
            conn = DbUtil.getConnection();
            StatementBuilder sb = new StatementBuilder();
            if (start != null && end != null)
            {
                sb.append("SELECT DISTINCT tuId FROM ")
                        .append(getStorage().getTuvTableName())
                        .append(" as tuv, ").append("TM3_EVENTS as event ")
                        .append("WHERE tuv.lastEventId = event.id ")
                        .append("AND tuv.localeId = ?")
                        .addValue(locale.getId())
                        .append(" AND event.time >= ? AND event.time <= ? ")
                        .addValues(start, end)
                        .append("AND tuId > ? ORDER BY tuId ASC LIMIT ?")
                        .addValues(startId, count);
            }
            else
            {
                sb.append("SELECT DISTINCT tuId FROM ")
                        .append(getStorage().getTuvTableName())
                        .append(" WHERE localeId = ? AND tuId > ? ORDER BY tuId ASC LIMIT ?")
                        .addValues(locale.getId(), startId, count);
            }
            return getTu(SQLUtil.execIdsQuery(conn, sb), false);
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
    public List<TM3Tu<T>> getTuPageByAttributes(long startId, int count,
            Map<TM3Attribute, Object> inlineAttrs,
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
                sb.append("SELECT DISTINCT tuv.tuId FROM ")
                        .append(getStorage().getTuvTableName())
                        .append(" as tuv, ")
                        .append(getStorage().getTuTableName())
                        .append(" as tu, ").append("TM3_EVENTS as event ");
                getStorage().attributeJoinFilter(sb, "tuv.tuId", customAttrs);
                sb.append(" WHERE tuv.lastEventId = event.id ")
                        .append("AND event.time >= ? AND event.time <= ? ")
                        .addValues(start, end).append("AND tuv.tuId > ?")
                        .addValues(startId);
            }
            else
            {
                sb.append("SELECT DISTINCT tu.id FROM ")
                        .append(getStorage().getTuTableName())
                        .append(" as tu ");
                getStorage().attributeJoinFilter(sb, "tu.id", customAttrs);
                sb.append(" WHERE tu.id > ?").addValues(startId);
            }
            for (Map.Entry<TM3Attribute, Object> e : inlineAttrs.entrySet())
            {
                sb.append(" AND tu.").append(e.getKey().getColumnName())
                        .append(" = ?").addValues(e.getValue());
            }
            if (start != null && end != null)
            {
                sb.append(" ORDER BY tuv.tuId ASC LIMIT ?").addValues(count);
            }
            else
            {
                sb.append(" ORDER BY tu.id ASC LIMIT ?").addValues(count);
            }
            return getTu(SQLUtil.execIdsQuery(conn, sb), false);
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
                    new StatementBuilder().append(
                            "SELECT DISTINCT(localeId) FROM ").append(
                            getStorage().getTuvTableName())));
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
                                .append(" WHERE ").append(attr.getColumnName())
                                .append(" IS NOT NULL"));
            }
            else
            {
                return SQLUtil.execObjectsQuery(
                        conn,
                        new StatementBuilder()
                                .append("SELECT DISTINCT(value) FROM ")
                                .append(getStorage().getAttrValTableName())
                                .append(" WHERE attrId = ?")
                                .addValue(attr.getId()));
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
}
