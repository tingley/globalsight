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
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.ling.tm3.core.persistence.SQLUtil;
import com.globalsight.ling.tm3.core.persistence.StatementBuilder;
import com.globalsight.ling.tm3.integration.GSTuvData;
import com.globalsight.ling.tm3.integration.segmenttm.Tm3SegmentTmInfo;
import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * Access to TU and TUV persistence.
 * 
 * This is only for dedicated tables, not for shared tables.
 */
abstract class TuStorage<T extends TM3Data>
{
    private static Logger logger = Logger.getLogger(TuStorage.class);

    private Tm3SegmentTmInfo tm3SegmentTmInfo = new Tm3SegmentTmInfo();

    private StorageInfo<T> storage;

    TuStorage(StorageInfo<T> storage)
    {
        this.storage = storage;
    }

    public TM3Tu<T> createTu(TM3Locale sourceLocale, T sourceTuvData,
            Map<TM3Attribute, Object> attributes, TM3Event event,
            String creationUser, Date creationDate, String modifyUser,
			Date modifyDate, Date lastUsageDate, long jobId, String jobName,
			long previousHash, long nextHash, String sid)
    {
		TM3Tuv<T> sourceTuv = new TM3Tuv<T>(sourceLocale, sourceTuvData, event,
				creationUser, creationDate, modifyUser, modifyDate,
				lastUsageDate, jobId, jobName, previousHash, nextHash, sid);
        return new TM3Tu<T>(storage.getTm(), this, sourceTuv, attributes);
    }

    protected StorageInfo<T> getStorage()
    {
        return storage;
    }

    /**
     * Persist a newly created TU, or save updates to an existing one. This will
     * set the TM3Tu id field if it is not already set.
     * 
     * @param tu
     * @throws SQLException
     */
    public abstract void saveTu(Connection conn, TM3Tu<T> tu)
            throws SQLException;

    /**
     * Delete a TU, along with all its TUV, attributes, and history.
     * 
     * @param conn
     * @param tu
     * @throws SQLException
     */
    public void deleteTu(Connection conn, TM3Tu<T> tu) throws SQLException
    {
        try
        {
        	// tm3_attr_val_shared_xx
            SQLUtil.exec(conn, new StatementBuilder().append("DELETE FROM ")
                    .append(storage.getAttrValTableName()).append(" WHERE tuId = ?")
                    .addValue(tu.getId()));
            // tm3_tuv_ext_shared_xx
            SQLUtil.exec(conn, new StatementBuilder().append("DELETE FROM ")
                    .append(storage.getTuvExtTableName()).append(" WHERE tuId = ?")
                    .addValue(tu.getId()));
            // TODO: not care event table
            // tm3_tuv_shared_xx
            SQLUtil.exec(conn, new StatementBuilder().append("DELETE FROM ")
                    .append(storage.getTuvTableName()).append(" WHERE tuId = ?")
                    .addValue(tu.getId()));
            // tm3_tu_shared_xx
            SQLUtil.exec(conn, new StatementBuilder().append("DELETE FROM ")
                    .append(storage.getTuTableName()).append(" WHERE id = ?")
                    .addValue(tu.getId()));
        }
        catch (Exception e)
        {
            throw new SQLException(e);
        }
    }

    /**
     * Deletes one or more TUs, along with all their TUVs, attributes, and
     * history.
     * 
     * @param ids
     *            List of TU ids to delete
     * @throws SQLException
     */
    public void deleteTusById(List<Long> ids) throws SQLException
    {
        if (ids.size() == 0)
        {
            return;
        }
        Connection conn = null;
        try
        {
            conn = DbUtil.getConnection();
            conn.setAutoCommit(false);
            SQLUtil.exec(conn, new StatementBuilder().append("DELETE FROM ")
                    .append(storage.getTuTableName()).append(" WHERE id IN ")
                    .append(SQLUtil.longGroup(ids)));
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

    /**
     * Delete TUVs, along with their history.
     * 
     * @param conn
     * @param tuv
     * @throws IllegalArgumentException
     *             if you try to delete the source TUV for a TU.
     */
    public void deleteTuvs(Connection conn, List<TM3Tuv<T>> tuvs)
            throws SQLException
    {
        if (tuvs.size() == 0)
        {
            return;
        }
        try
        {
			StatementBuilder sb = new StatementBuilder("DELETE FROM ")
					.append(storage.getTuvExtTableName())
					.append(" WHERE tuvId IN (?").addValue(tuvs.get(0).getId());
			for (int i = 1; i < tuvs.size(); i++)
        	{
        		sb.append(",?").addValue(tuvs.get(i).getId());
        	}
            sb.append(")");
        	SQLUtil.exec(conn, sb);

        	// Events will cascade
            sb = new StatementBuilder("DELETE FROM ")
                    .append(storage.getTuvTableName())
                    .append(" WHERE id IN (?").addValue(tuvs.get(0).getId());
            for (int i = 1; i < tuvs.size(); i++)
            {
                sb.append(",?").addValue(tuvs.get(i).getId());
            }
            sb.append(")");
            SQLUtil.exec(conn, sb);
        }
        catch (Exception e)
        {
            throw new SQLException(e);
        }
    }

    public abstract void addTuvs(Connection conn, TM3Tu<T> tu,
            List<TM3Tuv<T>> tuvs) throws SQLException;

    List<FuzzyCandidate<T>> loadFuzzyCandidates(List<Long> tuvIds,
            TM3Locale keyLocale) throws SQLException
    {
        if (tuvIds.size() == 0)
        {
            return Collections.emptyList();
        }
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ").append("id, tuId, fingerprint, content")
                .append(" FROM ").append(getStorage().getTuvTableName())
                .append(" WHERE id IN").append(SQLUtil.longGroup(tuvIds));
        Connection conn = null;
        try
        {
            conn = DbUtil.getConnection();
            Statement s = conn.createStatement();
            ResultSet rs = SQLUtil.execQuery(s, sb.toString());

            List<FuzzyCandidate<T>> fuzzies = new ArrayList<FuzzyCandidate<T>>();
            while (rs.next())
            {
                long id = rs.getLong(1);
                long tuId = rs.getLong(2);
                long fingerprint = rs.getLong(3);
                String content = rs.getString(4);
                TM3DataFactory<T> factory = getStorage().getTm()
                        .getDataFactory();
                fuzzies.add(new FuzzyCandidate<T>(id, tuId, fingerprint,
                        factory.fromSerializedForm(keyLocale, content)));
            }
            s.close();
            return fuzzies;
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

    public abstract void updateTuvs(Connection conn, TM3Tu<T> tu,
            List<TM3Tuv<T>> tuvs, TM3Event event) throws SQLException;

    public void updateAttributes(Connection conn, TM3Tu<T> tu,
            Map<TM3Attribute, Object> inlineAttributes,
            Map<TM3Attribute, String> customAttributes) throws SQLException
    {
        // This is very simple. Just dump the old values and write the
        // new ones.
        deleteCustomAttribute(conn, tu.getId());
        saveCustomAttributes(conn, tu.getId(), customAttributes);

        updateInlineAttributes(conn, tu.getId(), inlineAttributes);
    }

    /**
     * Update inline attributes. This is the same across all table types.
     * 
     * @param conn
     * @param attributes
     * @throws SQLException
     */
    public void updateInlineAttributes(Connection conn, long tuId,
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
            // SID attribute is always stored into another table
            if (".sid".equalsIgnoreCase(e.getKey().getName()))
            {
//            	String sid = (String) e.getValue();
//                long tmId = getStorage().getId();
                continue;
            }

            if (!first)
            {
                sb.append(", ");
            }
            sb.append(e.getKey().getColumnName() + " = ? ").addValue(
                    e.getValue());
            first = false;
        }
        sb.append(" WHERE id = ? ").addValue(tuId);
        SQLUtil.exec(conn, sb);
    }

    /**
     * Save custom attributes. This is the same across all table types.
     * 
     * @param conn
     * @param attributes
     * @throws SQLException
     */
    abstract void saveCustomAttributes(Connection conn, long tuId,
            Map<TM3Attribute, String> attributes) throws SQLException;

    private void deleteCustomAttribute(Connection conn, long tuId)
            throws SQLException
    {
        try
        {
			SQLUtil.exec(
					conn,
					new StatementBuilder("DELETE FROM ")
							.append(storage.getAttrValTableName())
							.append(" WHERE tuId = ?").addValue(tuId));
        }
        catch (Exception e)
        {
            throw new SQLException(e);
        }
    }

    public TM3Tu<T> getTu(Long id, boolean locking) throws SQLException
    {
        List<TM3Tu<T>> list = getTu(Collections.singletonList(id), locking);
        if (list == null || list.size() == 0)
        {
            return null;
        }
        else
        {
            return list.get(0);
        }
    }

    public List<TM3Tu<T>> getTu(List<Long> ids, boolean locking)
            throws SQLException
    {
        Connection conn = null;
        try
        {
            conn = DbUtil.getConnection();
            return getTu(conn, ids, locking);
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

    public List<TM3Tu<T>> getTu(Connection conn, List<Long> tuIds,
            boolean locking) throws SQLException
    {
        if (tuIds.size() == 0)
        {
            return Collections.emptyList();
        }
        List<TuData<T>> tuDatas = getTuData(conn, tuIds, locking);

        // Now fetch TUVs
        loadTuvs(conn, tuIds, tuDatas, locking);

        // I could proxy attrs, but it could produce n+1 query problems.
        // For now, just load them immediately until we know it's a problem.
        loadAttrs(conn, tuIds, tuDatas, locking);

        List<TM3Tu<T>> tus = new ArrayList<TM3Tu<T>>();
        for (TuData<T> data : tuDatas)
        {
            if (data.srcTuv == null)
            {
                logger.warn("SRC TUV is NULL! TU id: " + data.id);

                if (data.tgtTuvs != null && data.tgtTuvs.size() > 0)
                {
                    for (TM3Tuv<T> tuv : data.tgtTuvs)
                    {
                        logger.warn("Target TUV id: " + tuv.getId());
                    }
                }

                continue;
            }
            @SuppressWarnings("unchecked")
            BaseTm<T> currTm = (BaseTm<T>) tm3SegmentTmInfo.getTM3Tm(data.tmId);
            TM3Tu<T> tu = new TM3Tu<T>(currTm, this, data.srcTuv, data.tgtTuvs,
                    data.attrs);
            tu.setId(data.id);
            tus.add(tu);
            data.srcTuv.setTu(tu);
            for (TM3Tuv<T> tuv : data.tgtTuvs)
            {
                tuv.setTu(tu);
            }
        }
        return tus;
    }

    public TM3Tu<T> getTuByTuvId(long tuvId) throws SQLException
    {
        long tuId = getTuIdByTuvId(tuvId);
        if (tuId == 0)
        {
            // tuv may already been deleted
            return null;
        }
        return getTu(tuId, false);
    }

    void loadLeverageMatches(List<FuzzyCandidate<T>> candidates,
            TM3LeverageResults<T> results) throws SQLException
    {
        // XXX This could optimize further by reusing content?
        // XXX Currently getTuData() orders by ID
        List<Long> tuIds = new ArrayList<Long>();
        for (FuzzyCandidate<T> candidate : candidates)
        {
            tuIds.add(candidate.getTuId());
        }
        List<TM3Tu<T>> tus = getTu(tuIds, false);
        Map<Long, TM3Tu<T>> tuMap = new HashMap<Long, TM3Tu<T>>();
        for (TM3Tu<T> tu : tus)
        {
            tuMap.put(tu.getId(), tu);
        }
        // Now use the existing sort order of the fuzzy candidates
        OUTER: for (FuzzyCandidate<T> candidate : candidates)
        {
            TM3Tu<T> tu = tuMap.get(candidate.getTuId());
            if (tu == null)
            {
                throw new TM3Exception("Failed to load TU "
                        + candidate.getTuId());
            }
            for (TM3Tuv<T> tuv : tu.getAllTuv())
            {
                if (tuv.getId().equals(candidate.getId()))
                {
                    results.addFuzzyMatch(tu, tuv, candidate.getScore());
                    continue OUTER;
                }
            }
        }
    }

    private List<TuData<T>> getTuData(Connection conn, List<Long> ids,
            boolean locking) throws SQLException
    {
        StatementBuilder sb = new StatementBuilder("SELECT ")
                .append("tmId, id, srcLocaleId");
        for (TM3Attribute attr : getStorage().getInlineAttributes())
        {
            sb.append(", ").append(attr.getColumnName());
        }
        sb.append(" FROM ").append(getStorage().getTuTableName())
                .append(" WHERE id IN").append(SQLUtil.longGroup(ids))
                .append(" ORDER BY id");

        try
        {
            PreparedStatement ps = sb.toPreparedStatement(conn);
            ResultSet rs = SQLUtil.execQuery(ps);

            List<TuData<T>> tuDatas = new ArrayList<TuData<T>>();
            while (rs.next())
            {
                TuData<T> tu = new TuData<T>();
                tu.tmId = rs.getLong(1);
                tu.id = rs.getLong(2);
                tu.srcLocaleId = rs.getLong(3);
                int pos = 4;
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
            ps.close();
            return tuDatas;
        }
        catch (Exception e)
        {
            throw new SQLException(e);
        }
    }

    /**
     * Load data from "tm3_tuv_shared_xx" and "tm3_tuv_ext_shared_xx" tables.
     */
    private void loadTuvs(Connection conn, List<Long> tuIds,
            List<TuData<T>> tuDatas, boolean locking) throws SQLException
    {
        StatementBuilder sb = new StatementBuilder("SELECT ")
                .append("tuId, id, localeId, fingerprint, content, firstEventId, lastEventId, creationUser, creationDate, modifyUser, modifyDate")
                .append(" FROM ").append(getStorage().getTuvTableName())
                .append(" WHERE tuId IN").append(SQLUtil.longGroup(tuIds))
                .append("ORDER BY tuId");

        try
        {
            PreparedStatement ps = sb.toPreparedStatement(conn);
            ResultSet rs = SQLUtil.execQuery(ps);
            // In order to avoid a hidden ResultSet in the data factory
            // invalidating our current one, we must defer looking up
            // the locales until after we've read all of the data.
            List<TuvData<T>> rawTuvs = new ArrayList<TuvData<T>>();
            while (rs.next())
            {
                rawTuvs.add(new TuvData<T>(rs.getLong(1), rs.getLong(2), rs
                        .getLong(3), rs.getLong(4), rs.getString(5), rs
                        .getLong(6), rs.getLong(7), rs.getString(8), rs
                        .getTimestamp(9), rs.getString(10), rs.getTimestamp(11)));
            }
            ps.close();

            // Load TUV extension data into TUV objects.
            HashMap<Long, TuvData<T>> rawTuvsMap = new HashMap<Long, TuvData<T>>();
            for (TuvData<T> tuvData : rawTuvs)
            {
            	rawTuvsMap.put(tuvData.id, tuvData);
            }
			sb = new StatementBuilder("SELECT ")
					.append("tuvId, tuId, tmId, lastUsageDate, jobId, jobName, previousHash, nextHash, sid ")
					.append("FROM ").append(getStorage().getTuvExtTableName())
					.append(" WHERE tuId IN").append(SQLUtil.longGroup(tuIds));
			ps = sb.toPreparedStatement(conn);
			rs = SQLUtil.execQuery(ps);
			TuvData<T> tuvData = null;
			while (rs.next())
			{
				tuvData = rawTuvsMap.get(rs.getLong("tuvId"));
				if (tuvData != null)
				{
					tuvData.setLastUsageDate(rs.getTimestamp("lastUsageDate"));
					tuvData.setJobId(rs.getLong("jobId"));
					tuvData.setJobName(rs.getString("jobName"));
					tuvData.setPreviousHash(rs.getLong("previousHash"));
					tuvData.setNextHash(rs.getLong("nextHash"));
					tuvData.setSid(rs.getString("sid"));
				}
			}
			ps.close();

			TM3Attribute sidAttr = null;
			for (TM3Attribute att : getStorage().getInlineAttributes())
			{
				if (att != null && att.getName().equals(".sid"))
				{
					sidAttr = att;
					break;
				}
			}

            Iterator<TuData<T>> tus = tuDatas.iterator();
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
                tuv.setStorage(getTuStorage(current.tmId));
                if (tuv.getLocale().getId() == current.srcLocaleId)
                {
                    current.srcTuv = tuv;
                }
                else
                {
                    current.tgtTuvs.add(tuv);
                }
                // Put srcTuv SID to in-line SID attribute.
				if (sidAttr != null && StringUtils.isNotEmpty(tuv.getSid()))
				{
                    current.attrs.put(sidAttr, tuv.getSid());
                }
            }
        }
        catch (Exception e)
        {
            throw new SQLException(e);
        }
    }

    private void loadAttrs(Connection conn, List<Long> tuIds,
            List<TuData<T>> data, boolean locking) throws SQLException
    {
        StatementBuilder sb = new StatementBuilder();
        sb.append("SELECT tuId, attrId, value FROM ")
                .append(getStorage().getAttrValTableName())
                .append(" WHERE tuid IN (?").addValue(tuIds.get(0));
        for (int i = 1; i < tuIds.size(); i++)
        {
            sb.append(", ?").addValue(tuIds.get(i));
        }
        sb.append(") ORDER BY tuId");

        try
        {
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
    }

    protected TM3Attribute getAttributeById(long id)
    {
        return (TM3Attribute) HibernateUtil.get(TM3Attribute.class, id, false);
    }

    protected TM3Tuv<T> createTuv(TuvData<T> rawData)
    {
        TM3Tuv<T> tuv = new TM3Tuv<T>();
        tuv.setId(rawData.id);
        TM3Locale locale = storage.getTm().getDataFactory()
                .getLocaleById(rawData.localeId);
        tuv.setLocale(locale);
        tuv.setFingerprint(rawData.fingerprint);
        tuv.setContent(storage.getTm().getDataFactory()
                .fromSerializedForm(locale, rawData.content));
        TM3Event firstEvent = null;
        if (rawData.firstEventId > 0)
        { // Check for null value
            firstEvent = (TM3Event) HibernateUtil.get(TM3Event.class,
                    rawData.firstEventId, false);
        }
        tuv.setFirstEvent(firstEvent);
        TM3Event latestEvent = null;
        if (rawData.lastEventId > 0)
        { // Check for null value
            latestEvent = (TM3Event) HibernateUtil.get(TM3Event.class,
                    rawData.lastEventId, false);
        }
        tuv.setLatestEvent(latestEvent);

        tuv.setCreationUser(rawData.creationUser);
        tuv.setCreationDate(rawData.creationDate);
        tuv.setModifyUser(rawData.modifyUser);
        tuv.setModifyDate(rawData.modifyDate);
        tuv.setLastUsageDate(rawData.lastUsageDate);
        tuv.setJobId(rawData.jobId);
        tuv.setJobName(rawData.jobName);
        tuv.setPreviousHash(rawData.previousHash);
        tuv.setNextHash(rawData.nextHash);
        tuv.setSid(rawData.sid);

        return tuv;
    }

    protected TuData<T> advanceToTu(TuData<T> first, Iterator<TuData<T>> rest,
            long id)
    {
        if (first != null && first.id == id)
        {
            return first;
        }
        while (rest.hasNext())
        {
            first = rest.next();
            if (first.id == id)
            {
                return first;
            }
        }
        return null;
    }

    static class TuData<T extends TM3Data>
    {
        long tmId;
        long id;
        long srcLocaleId;
        TM3Tuv<T> srcTuv;
        List<TM3Tuv<T>> tgtTuvs = new ArrayList<TM3Tuv<T>>();
        Map<TM3Attribute, Object> attrs = new HashMap<TM3Attribute, Object>();
    }

    static class TuvData<T extends TM3Data>
    {
        long id;
        long tuId;
        long localeId;
        long fingerprint;
        String content;
        long firstEventId;
        long lastEventId;
        String creationUser = null;
        Date creationDate = null;
        String modifyUser = null;
        Date modifyDate = null;
        Date lastUsageDate = null;
        long jobId = -1;
        String jobName = null;
        long previousHash = -1;
        long nextHash = -1;
        String sid = null;

        TuvData(long tuId, long id, long localeId, long fingerprint,
                String content, long firstEventId, long lastEventId,
                String creationUser, Date creationDate, String modifyUser,
                Date modifyDate)
        {
            this.id = id;
            this.tuId = tuId;
            this.localeId = localeId;
            this.fingerprint = fingerprint;
            this.content = content;
            this.firstEventId = firstEventId;
            this.lastEventId = lastEventId;
            this.creationUser = creationUser;
            this.creationDate = creationDate;
            this.modifyUser = modifyUser;
            this.modifyDate = modifyDate;
        }

        void setLastUsageDate(Date lastUsageDate) {
        	this.lastUsageDate = lastUsageDate;
        }

        void setJobId(long jobId) {
        	this.jobId = jobId;
        }

        void setJobName(String jobName) {
        	this.jobName = jobName;
        }

        void setPreviousHash(long previousHash) {
        	this.previousHash = previousHash;
        }

        void setNextHash(long nextHash) {
        	this.nextHash = nextHash;
        }

        void setSid(String sid) {
        	this.sid = sid;
        }
    }

    public List<TM3Tuv<T>> getExactMatchesForSave(Connection conn, T key,
            TM3Locale sourceLocale, Set<? extends TM3Locale> targetLocales,
            Map<TM3Attribute, Object> inlineAttributes,
            Map<TM3Attribute, String> customAttributes, boolean lookupTarget,
            boolean locking, long preHash, long nextHash) throws SQLException
    {
    	List<Long> tm3TmIds = new ArrayList<Long>();
        tm3TmIds.add(getStorage().getId());

   		// If SID is not empty/null ...
        String sid = getSidFromInlineAttributes(inlineAttributes);

        Set<Long> tuvIds = new HashSet<Long>();
        List<Long> tuIds = new ArrayList<Long>();
        StatementBuilder sb = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        // Fetch candidates by previous/next hash values prior.
		sb = getHashMatchedStatement(key, sourceLocale, tm3TmIds, preHash,
				nextHash, sid);
        ps = sb.toPreparedStatement(conn);
        rs = SQLUtil.execQuery(ps);
        while (rs.next())
        {
            tuvIds.add(rs.getLong(1));
            tuIds.add(rs.getLong(2));
        }
        ps.close();

        List<TM3Tu<T>> tus = getTu(conn, tuIds, locking);
        List<TM3Tuv<T>> tuvs = new ArrayList<TM3Tuv<T>>();
        for (TM3Tu<T> tu : tus)
        {
            for (TM3Tuv<T> tuv : tu.getAllTuv())
            {
                if (tuvIds.contains(tuv.getId()))
                {
                    tuvs.add(tuv);
                }
            }
        }

        return tuvs;
    }

	private String getSidFromInlineAttributes(
			Map<TM3Attribute, Object> inlineAttributes)
    {
        if (!inlineAttributes.isEmpty())
        {
            for (Map.Entry<TM3Attribute, Object> e : inlineAttributes.entrySet())
            {
				if (e.getKey().getAffectsIdentity()
						&& "sid".equalsIgnoreCase(e.getKey().getColumnName()))
                {
					return (String) e.getValue();
                }
            }
        }

        return null;
    }

	/**
	 * When save TU into TM, narrow the tuId/tuvId scope by previous/next hash
	 * values and SID for performance.
	 */
    private StatementBuilder getHashMatchedStatement(T key,
			TM3Locale srcLocale, List<Long> tm3TmIds, long preHash,
			long nextHash, String sid)
    {
		StatementBuilder sb = new StatementBuilder(
				"SELECT tuv.id, tuv.tuId FROM ")
				.append(getStorage().getTuvTableName()).append(" AS tuv, ")
				.append(getStorage().getTuvExtTableName()).append(" AS ext");
		sb.append(" WHERE tuv.id = ext.tuvId")
				.append(" AND tuv.tuId = ext.tuId")
				.append(" AND tuv.fingerprint = ?").addValue(key.getFingerprint())
				.append(" AND tuv.localeId = ?").addValue(srcLocale.getId());
		if (preHash != -1)
		{
			sb.append(" AND ext.previousHash = ?").addValue(preHash);
		}
		if (nextHash != -1)
		{
			sb.append(" AND ext.nextHash = ?").addValue(nextHash);
		}
		if (sid != null && sid.trim().length() > 0)
		{
			sb.append(" AND ext.sid = ?").addValue(sid);
		}
		sb.append(" AND tuv.tmId IN").append(SQLUtil.longGroup(tm3TmIds));
		sb.append(" LIMIT 20");

		return sb;
    }

    /**
     * For create job to leverage TM, it does not care previous/next hash.
     */
    public List<TM3Tuv<T>> getExactMatches(Connection conn, T key,
            TM3Locale sourceLocale, Set<? extends TM3Locale> targetLocales,
            Map<TM3Attribute, Object> inlineAttributes,
            Map<TM3Attribute, String> customAttributes, boolean lookupTarget,
            boolean locking, List<Long> tm3TmIds) throws SQLException
    {
        // avoid an awkward case in getExactMatchStatement
        if (targetLocales != null && targetLocales.isEmpty())
        {
            return Collections.emptyList();
        }

        //#1. Find all possible candidates, no max number limitation...
        StatementBuilder sb = getExactMatchStatement(key, sourceLocale,
                targetLocales, inlineAttributes, lookupTarget, tm3TmIds);
        if (customAttributes.size() > 0)
        {
            sb = getAttributeMatchWrapper(sb, customAttributes);
        }
        PreparedStatement ps = sb.toPreparedStatement(conn);
        ResultSet rs = SQLUtil.execQuery(ps);
        Set<Long> tuvIds = new HashSet<Long>();
        List<Long> tuIds = new ArrayList<Long>();
		HashMap<Long, Long> tuId2srcTuvId = new HashMap<Long, Long>();
        while (rs.next())
        {
            tuvIds.add(rs.getLong(1));
            tuIds.add(rs.getLong(2));
            tuId2srcTuvId.put(rs.getLong(2), rs.getLong(1));
        }
        ps.close();

        //#2. If there are too many, need reduce them for performance...
        boolean isCandidateFiltered = false;
        int max = 200;
        if (targetLocales.size() * 10 > max) {
        	max = targetLocales.size() * 10;
        }
        String sid = getSidFromInlineAttributes(inlineAttributes);
		if (tuvIds.size() > max)
		{
			isCandidateFiltered = true;
			if (logger.isDebugEnabled()) {
				logger.info("Candidated exact matches tuvIds number: "
						+ tuvIds.size());				
			}
			//#2.1 previous/next hash matched candidates must be returned ...
	        Set<Long> hashMatchedTuvIds = new HashSet<Long>();
	        List<Long> hashMatchedTuIds = new ArrayList<Long>();
			BaseTmTuv srcTuv = ((GSTuvData) key).getSrcTuv();
			long preHash = srcTuv.getPreviousHash();
			long nextHash = srcTuv.getNextHash();
			if (preHash != -1 && nextHash != -1)
			{
				sb = new StatementBuilder("SELECT tuv.id, tuv.tuId FROM ");
				sb.append(getStorage().getTuvTableName()).append(" AS tuv, ");
				sb.append(getStorage().getTuvExtTableName()).append(" AS ext");
				sb.append(" WHERE tuv.id = ext.tuvId");
				sb.append(" AND tuv.tuId = ext.tuId");
				sb.append(" AND ext.previousHash = ?").addValue(preHash);
				sb.append(" AND ext.nextHash = ?").addValue(nextHash);
				if (sid != null && sid.trim().length() > 0)
				{
					sb.append(" AND ext.sid = ?").addValue(sid);
				}
				sb.append(" AND tuv.tmId IN").append(SQLUtil.longGroup(tm3TmIds));
				sb.append(" AND tuv.id IN").append(SQLUtil.longGroup(new ArrayList<Long>(tuvIds)));

				ps = sb.toPreparedStatement(conn);
				rs = SQLUtil.execQuery(ps);
				while (rs.next())
				{
					hashMatchedTuvIds.add(rs.getLong(1));
					hashMatchedTuIds.add(rs.getLong(2));
				}
				ps.close();
			}

			//#2.2 Ensure returning at most 10 for every locale...
	        List<Long> targetLocaleIds = new ArrayList<Long>();
			if (targetLocales != null) {
				for (TM3Locale locale : targetLocales) {
					targetLocaleIds.add(locale.getId());
				}
			}
			sb = new StatementBuilder(
					"SELECT tuv.tuId, tuv.localeId, COUNT(id) FROM ")
					.append(getStorage().getTuvTableName()).append(" AS tuv, ")
					.append(getStorage().getTuvExtTableName()).append(" AS ext");
			sb.append(" WHERE tuv.id = ext.tuvId")
					.append(" AND tuv.tmId IN")
					.append(SQLUtil.longGroup(tm3TmIds))
					.append(" AND tuv.tuId IN").append(SQLUtil.longGroup(tuIds));
			if (targetLocaleIds.size() > 0) {
				sb.append(" AND tuv.localeId IN").append(
						SQLUtil.longGroup(targetLocaleIds));
			}
			sb.append(" GROUP BY tuv.tuId, tuv.localeId");

			tuvIds.clear();
			tuIds.clear();
			tuvIds.addAll(hashMatchedTuvIds);
			tuIds.addAll(hashMatchedTuIds);

			long tuId = -1;
			long localeId = -1;
	        HashMap<Long, Long> locale2tuvNum = new HashMap<Long, Long>();
			ps = sb.toPreparedStatement(conn);
	        rs = SQLUtil.execQuery(ps);
	        while (rs.next())
	        {
	        	tuId = rs.getLong(1);
	        	localeId = rs.getLong(2);
	        	Long tuvNum = locale2tuvNum.get(localeId);
	        	if (tuvNum == null) {
	        		tuvNum = new Long(0);
	        	}
	        	if (tuvNum < 10)
	        	{
	        		tuIds.add(tuId);
	        		tuvIds.add(tuId2srcTuvId.get(tuId));// add source tuvId!!!
	        		locale2tuvNum.put(localeId, tuvNum + rs.getLong(3));
	       		}
	        }
	        ps.close();
		}

   		// If SID is not empty/null, need filter the candidate TUs here.
        // To avoid slow query, we filter TUs via 2 queries.
		if (!isCandidateFiltered && tuvIds.size() > 0 && sid != null
				&& sid.length() > 0)
        {
			sb = new StatementBuilder("SELECT tuvId, tuId FROM ")
					.append(getStorage().getTuvExtTableName())
					.append(" WHERE tmId IN").append(SQLUtil.longGroup(tm3TmIds))
					.append(" AND sid = ?").addValue(sid)
					.append(" AND tuvId IN ")
					.append(SQLUtil.longGroup(new ArrayList<Long>(tuvIds)));
            ps = sb.toPreparedStatement(conn);
            rs = SQLUtil.execQuery(ps);
            tuvIds.clear();
            tuIds.clear();
            while (rs.next())
            {
                tuvIds.add(rs.getLong(1));
                tuIds.add(rs.getLong(2));
            }
            ps.close();
        }

        // XXX Could save a query by having the lookup fetch the TU row, or even
        // the TU row + TUV source row (and lazily load rest...)
        List<TM3Tu<T>> tus = getTu(conn, tuIds, locking);
        List<TM3Tuv<T>> tuvs = new ArrayList<TM3Tuv<T>>();
        for (TM3Tu<T> tu : tus)
        {
            for (TM3Tuv<T> tuv : tu.getAllTuv())
            {
                // XXX For safety we should probably be comparing keys here.
                // However, just testing string equality on the serialized
                // form can sometimes produce false negatives (for instance,
                // this is true in GS due to optional attributes on
                // the <segment> markup. TM3TuvData needs an extra method
                // to make this comparison. Since this only matters if
                // there is a hash collision, I'm not going to worry about
                // it for now.
                if (tuvIds.contains(tuv.getId()))
                {
                    tuvs.add(tuv);
                }
            }
        }

        return tuvs;
    }

    // Note for implementors: targetLocales may be null, but will not be empty
	private StatementBuilder getExactMatchStatement(T key, TM3Locale srcLocale,
			Set<? extends TM3Locale> targetLocales,
			Map<TM3Attribute, Object> inlineAttrs, boolean lookupTarget,
			List<Long> tm3TmIds)
    {
        StatementBuilder sb = new StatementBuilder(
                "SELECT tuv.id, tuv.tuId FROM ").append(getStorage()
                .getTuvTableName() + " AS tuv");
        if (!inlineAttrs.isEmpty() || !lookupTarget)
        {
            sb.append(" join " + getStorage().getTuTableName() + " as tu")
                    .append(" on tu.id = tuv.tuId");
        }
        sb.append(" WHERE fingerprint = ? AND localeId = ? ")
                .addValues(key.getFingerprint(), srcLocale.getId())
                .append("AND tuv.tmId IN").append(SQLUtil.longGroup(tm3TmIds));
        if (!lookupTarget)
        {
            sb.append("AND tu.srcLocaleId = ? ").addValue(srcLocale.getId());
        }
        if (!inlineAttrs.isEmpty())
        {
            for (Map.Entry<TM3Attribute, Object> e : inlineAttrs.entrySet())
            {
            	// As SID is in extension table now, ignore sid here
				if (e.getKey().getAffectsIdentity()
						&& !"sid".equalsIgnoreCase(e.getKey().getColumnName()))
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
                    .append("WHERE targetTuv.tuId = result.tuId AND ")
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
    protected abstract StatementBuilder getAttributeMatchWrapper(
            StatementBuilder inner, Map<TM3Attribute, String> attributes);

    protected TM3EventLog loadEvents(ResultSet rs) throws SQLException
    {
        List<TM3Event> events = new ArrayList<TM3Event>();
        while (rs.next())
        {
            TM3Event event = new TM3Event();
            event.setId(rs.getLong(1));
            event.setTimestamp(rs.getDate(2));
            event.setUsername(rs.getString(3));
            event.setType(rs.getInt(4));
            event.setArgument(rs.getString(5));
            events.add(event);
        }
        return new TM3EventLog(events);
    }

    public abstract Set<TM3Locale> getTuvLocales() throws SQLException;

    public abstract List<Object> getAllAttrValues(TM3Attribute attr)
            throws SQLException;

    protected Set<TM3Locale> loadLocales(Collection<Long> ids)
    {
        Set<TM3Locale> locales = new HashSet<TM3Locale>();
        for (Long id : ids)
        {
            locales.add(storage.getTm().getDataFactory().getLocaleById(id));
        }
        return locales;
    }

    //
    // Data Handle backends
    //

	public abstract long getTuCountByParamMap(Map<String, Object> paramMap)
			throws SQLException;
	
	public abstract long getAllTuCount() throws SQLException;

	public abstract long getTuCountByLocale(Long localeId) throws SQLException;

    public abstract long getTuvCount(Date start, Date end) throws SQLException;

    public abstract void deleteTus(Date start, Date end) throws SQLException;

    public abstract long getTuvCountByLocales(List<TM3Locale> localeList,
            Date start, Date end) throws SQLException;

    public abstract void deleteTuvsByLocale(TM3Locale locale)
            throws SQLException;

    public abstract long getTuvCountByAttributes(
            Map<TM3Attribute, Object> inlineAttrs,
            Map<TM3Attribute, String> customAttrs, Date start, Date end)
            throws SQLException;

	public abstract List<TM3Tu<T>> getTuPageByParamMap(long startId, int count,
			Map<String, Object> paramMap) throws SQLException;

    // Support for the "get tu by tuv" hack.
    protected abstract long getTuIdByTuvId(Long tuvId) throws SQLException;

    public boolean doesCustomAttribtueExist(Connection conn, long tuId,
            long attId) throws SQLException
    {
        try
        {
            BaseTm tm = storage == null ? null : storage.getTm();
            if (tm != null)
            {
                long tmId = tm.getId();
                StatementBuilder sb = new StatementBuilder();
                sb.append("select count(*) from ").append(
                        storage.getAttrValTableName());
                sb.append(" where tmId = ? and tuId = ? and attrId = ?");
                sb.addValues(tmId, tuId, attId);

                long count = SQLUtil.execCountQuery(conn, sb);

                return count > 0;
            }
            else
            {
                return false;
            }
        }
        catch (Exception e)
        {
            throw new SQLException(e);
        }
    }

    public void saveCustomAttribute(Connection conn, long tuId,
            TM3Attribute tm3a, String value) throws SQLException
    {
        try
        {
            BaseTm tm = storage == null ? null : storage.getTm();
            if (tm != null)
            {
                long tmId = tm.getId();
                StatementBuilder sb = new StatementBuilder();
                sb.append("INSERT INTO ")
                        .append(storage.getAttrValTableName())
                        .append(" (tmId, tuId, attrId, value) VALUES (?, ?, ?, ?)");
                sb.addValues(tmId, tuId, tm3a.getId(), value);

                SQLUtil.exec(conn, sb);
            }
        }
        catch (Exception e)
        {
            throw new SQLException(e);
        }
    }

    public void updateCustomAttribute(Connection conn, long tuId,
            TM3Attribute tm3a, String value) throws SQLException
    {
        try
        {
            BaseTm tm = storage == null ? null : storage.getTm();
            if (tm != null)
            {
                long tmId = tm.getId();
                StatementBuilder sb = new StatementBuilder();
                sb.append("UPDATE ")
                        .append(storage.getAttrValTableName())
                        .append(" set value = ? where tmId = ? and tuId = ? and attrId = ?");
                sb.addValues(value, tmId, tuId, tm3a.getId());

                SQLUtil.exec(conn, sb);
            }
        }
        catch (Exception e)
        {
            throw new SQLException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private TuStorage<T> getTuStorage(Long tm3TmId)
    {
        BaseTm<T> currentBaseTm = (BaseTm<T>) tm3SegmentTmInfo
                .getTM3Tm(tm3TmId);
        return currentBaseTm.getStorageInfo().getTuStorage();
    }
}
