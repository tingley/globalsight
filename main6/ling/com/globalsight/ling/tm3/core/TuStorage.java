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

import org.hibernate.Session;

import com.globalsight.ling.tm3.core.persistence.BatchStatementBuilder;
import com.globalsight.ling.tm3.core.persistence.SQLUtil;
import com.globalsight.ling.tm3.core.persistence.StatementBuilder;

/**
 * Access to TU and TUV persistence.
 * 
 * This is only for dedicated tables, not for shared tables.
 */
abstract class TuStorage<T extends TM3Data> {
    private StorageInfo<T> storage;
    
    TuStorage(StorageInfo<T> storage) {
        this.storage = storage;
    }
    
    public TM3Tu<T> createTu(TM3Locale sourceLocale, 
                          T sourceTuvData, Map<TM3Attribute, Object> attributes,
                          TM3Event event) {
        TM3Tuv<T> sourceTuv = new TM3Tuv<T>(sourceLocale, 
                                            sourceTuvData, event);
        return new TM3Tu<T>(storage.getTm(), this, sourceTuv, attributes);
    }

    protected StorageInfo<T> getStorage() {
        return storage;
    }
    
    protected Connection getConnection() {
        return storage.getSession().connection();
    }

    /**
     * Persist a newly created TU, or save updates to an existing one.
     * This will set the TM3Tu id field if it is not already set.
     * @param tu
     * @throws SQLException 
     */
    public abstract void saveTu(Connection conn, TM3Tu<T> tu)
                        throws SQLException;
    
    /**
     * Delete a TU, along with all its TUV, attributes, and history.
     * @param conn
     * @param tu
     * @throws SQLException
     */
    public void deleteTu(TM3Tu<T> tu) throws SQLException {
        // TUV, Events, and AttrVals should both cascade.
        SQLUtil.exec(getConnection(), new StatementBuilder()
            .append("DELETE FROM ")
            .append(storage.getTuTableName())
            .append(" WHERE id = ?").addValue(tu.getId()));
    }
    
    /**
     * Deletes one or more TUs, along with all their TUVs, attributes,
     * and history.
     * @param ids List of TU ids to delete
     * @throws SQLException
     */
    public void deleteTusById(List<Long> ids) throws SQLException {
        if (ids.size() == 0) {
            return;
        }
        SQLUtil.exec(getConnection(), new StatementBuilder()
            .append("DELETE FROM ")
            .append(storage.getTuTableName())
            .append(" WHERE id IN ")
            .append(SQLUtil.longGroup(ids)));
    }

    /**
     * Delete TUVs, along with their history.   
     * @param conn
     * @param tuv
     * @throws IllegalArgumentException if you try to delete the source TUV for
     *         a TU.
     */
    public void deleteTuvs(List<TM3Tuv<T>> tuvs) throws SQLException { 
        if (tuvs.size() == 0) {
            return;
        }
        for (TM3Tuv<T> tuv : tuvs) {
            if (tuv.equals(tuv.getTu().getSourceTuv())) {
                throw new IllegalArgumentException(
                        "Can't delete source TUV without deleting TU");
            }
        }
        // Events will cascade
        StatementBuilder sb = new StatementBuilder("DELETE FROM ")
            .append(storage.getTuvTableName())
            .append(" WHERE id IN (?").addValue(tuvs.get(0).getId());
        for (int i = 1; i < tuvs.size(); i++) {
            sb.append(",?").addValue(tuvs.get(i).getId());
        }
        sb.append(")");
        SQLUtil.exec(getConnection(), sb);
    }
       
    public abstract void addTuvs(TM3Tu<T> tu, List<TM3Tuv<T>> tuvs) 
                            throws SQLException;
    
    List<FuzzyCandidate<T>> loadFuzzyCandidates(List<Long> tuvIds,
            TM3Locale keyLocale) throws SQLException {
        if (tuvIds.size() == 0) {
            return Collections.emptyList();
        }
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ")
          .append("id, tuId, fingerprint, content")
          .append(" FROM ")
          .append(getStorage().getTuvTableName())
          .append(" WHERE id IN")
          .append(SQLUtil.longGroup(tuvIds));
        Statement s = getConnection().createStatement();
        ResultSet rs = SQLUtil.execQuery(s, sb.toString()); 

        List<FuzzyCandidate<T>> fuzzies = new ArrayList<FuzzyCandidate<T>>();
        while (rs.next()) {
            long id = rs.getLong(1);
            long tuId = rs.getLong(2);
            long fingerprint = rs.getLong(3);
            String content = rs.getString(4);
            TM3DataFactory<T> factory = getStorage().getTm().getDataFactory();
            fuzzies.add(new FuzzyCandidate<T>(id, tuId, fingerprint,
                    factory.fromSerializedForm(keyLocale, content)));
        }
        s.close();
        return fuzzies;
    }    

    public abstract void updateTuvs(TM3Tu<T> tu, List<TM3Tuv<T>> tuvs,
                           TM3Event event) throws SQLException;
        
    public void updateAttributes(TM3Tu<T> tu,
            Map<TM3Attribute, Object> inlineAttributes,
            Map<TM3Attribute, String> customAttributes) throws SQLException {
        // This is very simple.  Just dump the old values and write the 
        // new ones.
        deleteCustomAttributes(tu.getId());
        saveInlineAttributes(tu.getId(), inlineAttributes);
        saveCustomAttributes(tu.getId(), customAttributes);
    }
    
    /**
     * Save inline attributes.  This is the same across all table types.
     * @param conn
     * @param attributes
     * @throws SQLException
     */
    abstract void saveInlineAttributes(long tuId, 
            Map<TM3Attribute, Object> attributes) throws SQLException;

    /**
     * Save custom attributes.  This is the same across all table types.
     * @param conn
     * @param attributes
     * @throws SQLException
     */
    abstract void saveCustomAttributes(long tuId, 
            Map<TM3Attribute, String> attributes) throws SQLException;

    void deleteCustomAttributes(long tuId) throws SQLException {
        SQLUtil.exec(getConnection(), new StatementBuilder("DELETE FROM ")
                .append(storage.getAttrValTableName())
                .append(" WHERE tuId = ?").addValue(tuId));
    }

    public TM3Tu<T> getTu(Long id, boolean locking) throws SQLException {
        return getTu(Collections.singletonList(id), locking).get(0);
    }
    
    public List<TM3Tu<T>> getTu(List<Long> ids, boolean locking)
            throws SQLException {
        if (ids.size() == 0) {
            return Collections.emptyList();
        }
        List<TuData<T>> tuDatas = getTuData(ids, locking);
        
        // Now fetch TUVs
        loadTuvs(ids, tuDatas, locking);

        // I could proxy attrs, but it could produce n+1 query problems.
        // For now, just load them immediately until we know it's a problem.
        loadAttrs(ids, tuDatas, locking);

        List<TM3Tu<T>> tus = new ArrayList<TM3Tu<T>>();
        for (TuData<T> data : tuDatas) {
            TM3Tu<T> tu = new TM3Tu<T>(storage.getTm(), this, 
                        data.srcTuv, data.tgtTuvs, data.attrs);
            tu.setId(data.id);
            tus.add(tu);
            data.srcTuv.setTu(tu);
            for (TM3Tuv<T> tuv : data.tgtTuvs) {
                tuv.setTu(tu);
            }
        }
        return tus;
    }
    
    public TM3Tu<T> getTuByTuvId(long tuvId) throws SQLException { 
       long tuId = getTuIdByTuvId(tuvId);
       if (tuId == 0) {
           throw new IllegalArgumentException("No such tuv " + tuvId + " for tm3 id "  +
                   getStorage().getTm().getId());
       }
       return getTu(tuId, false);
    }
    
    void loadLeverageMatches(List<FuzzyCandidate<T>> candidates,
            TM3LeverageResults<T> results) throws SQLException {
        // XXX This could optimize further by reusing content?
        // XXX Currently getTuData() orders by ID
        List<Long> tuIds = new ArrayList<Long>();
        for (FuzzyCandidate<T> candidate : candidates) {
            tuIds.add(candidate.getTuId());
        }
        List<TM3Tu<T>> tus = getTu(tuIds, false);
        Map<Long, TM3Tu<T>> tuMap = new HashMap<Long, TM3Tu<T>>();
        for (TM3Tu<T> tu : tus) {
            tuMap.put(tu.getId(), tu);
        }
        // Now use the existing sort order of the fuzzy candidates
OUTER:  for (FuzzyCandidate<T> candidate : candidates) {
            TM3Tu<T> tu = tuMap.get(candidate.getTuId());
            if (tu == null) {
                throw new TM3Exception("Failed to load TU " + candidate.getTuId());
            }
            for (TM3Tuv<T> tuv : tu.getAllTuv()) {
                if (tuv.getId().equals(candidate.getId())) {
                    results.addFuzzyMatch(tu, tuv, candidate.getScore());
                    continue OUTER;
                }
            }
        }
    }

    protected abstract List<TuData<T>> getTuData(List<Long> ids,
            boolean locking) throws SQLException;
    
    protected abstract void loadTuvs(List<Long> tuIds, List<TuData<T>> data, boolean locking) 
                    throws SQLException;
    
    protected abstract void loadAttrs(List<Long> tuIds, List<TuData<T>> data, boolean locking)
                    throws SQLException;

    protected TM3Attribute getAttributeById(Session session, long id) {
        return (TM3Attribute)session.get(TM3Attribute.class, id);
    }    
  
    protected TM3Tuv<T> createTuv(TuvData<T> rawData) {
        TM3Tuv<T> tuv = new TM3Tuv<T>();
        tuv.setId(rawData.id);
        TM3Locale locale = storage.getTm().getDataFactory()
                .getLocaleById(storage.getTm().getSession(), rawData.localeId);
        tuv.setLocale(locale);
        tuv.setFingerprint(rawData.fingerprint);
        tuv.setContent(storage.getTm().getDataFactory()
                .fromSerializedForm(locale, rawData.content));
        TM3Event firstEvent = null;
        if (rawData.firstEventId > 0) { // Check for null value
            firstEvent = (TM3Event)getStorage().getSession()
                .get(TM3Event.class, rawData.firstEventId);
        }
        tuv.setFirstEvent(firstEvent);
        TM3Event latestEvent = null;
        if (rawData.lastEventId > 0) { // Check for null value
            latestEvent = (TM3Event)getStorage().getSession()
                .get(TM3Event.class, rawData.lastEventId);
        }
        tuv.setLatestEvent(latestEvent);
        return tuv;
    }
    
    protected TuData<T> advanceToTu(TuData<T> first, Iterator<TuData<T>> rest, 
                                    long id) {
        if (first != null && first.id == id) {
            return first;
        }
        while (rest.hasNext()) {
            first = rest.next();
            if (first.id == id) {
                return first;
            }
        }
        return null;
    }
    
    static class TuData<T extends TM3Data> {
        long id;
        long srcLocaleId;
        TM3Tuv<T> srcTuv;
        List<TM3Tuv<T>> tgtTuvs = new ArrayList<TM3Tuv<T>>();
        Map<TM3Attribute, Object> attrs = new HashMap<TM3Attribute, Object>(); 
    }

    static class TuvData<T extends TM3Data> {
        long id;
        long tuId;
        long localeId;
        long fingerprint;
        String content;
        long firstEventId;
        long lastEventId;
        TuvData(long tuId, long id, long localeId, long fingerprint, 
                String content, long firstEventId, long lastEventId) { 
            this.id = id;
            this.tuId = tuId;
            this.localeId = localeId;
            this.fingerprint = fingerprint;
            this.content = content;
            this.firstEventId = firstEventId;
            this.lastEventId = lastEventId;
        }
    }
        
    public List<TM3Tuv<T>> getExactMatches(Connection conn, T key, 
           TM3Locale sourceLocale, Set<? extends TM3Locale> targetLocales,
           Map<TM3Attribute, Object> inlineAttributes,
           Map<TM3Attribute, String> customAttributes,
           boolean lookupTarget, boolean locking) throws SQLException {
        // avoid an awkward case in getExactMatchStatement
        if (targetLocales != null && targetLocales.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        StatementBuilder sb = getExactMatchStatement(key, sourceLocale, 
                            targetLocales, inlineAttributes, lookupTarget);
        if (locking) {
            sb.append(" FOR UPDATE");
        }
        if (customAttributes.size() > 0) {
            sb = getAttributeMatchWrapper(sb, customAttributes);
            if (locking) {
                sb.append(" FOR UPDATE");
            }
        }
        PreparedStatement ps = sb.toPreparedStatement(conn);
        ResultSet rs = SQLUtil.execQuery(ps);
        Set<Long> tuvIds = new HashSet<Long>();
        List<Long> tuIds = new ArrayList<Long>();
        while (rs.next()) {
            tuvIds.add(rs.getLong(1));
            tuIds.add(rs.getLong(2));
        }
        ps.close();

        // XXX Could save a query by having the lookup fetch the TU row, or even
        // the TU row + TUV source row (and lazily load rest...)
        List<TM3Tu<T>> tus = getTu(tuIds, locking);
        List<TM3Tuv<T>> tuvs = new ArrayList<TM3Tuv<T>>();
        for (TM3Tu<T> tu : tus) {
            for (TM3Tuv<T> tuv : tu.getAllTuv()) {
                // XXX For safety we should probably be comparing keys here.
                // However, just testing string equality on the serialized
                // form can sometimes produce false negatives (for instance,
                // this is true in GS due to optional attributes on 
                // the <segment> markup.  TM3TuvData needs an extra method
                // to make this comparison.  Since this only matters if
                // there is a hash collision, I'm not going to worry about
                // it for now.
                if (tuvIds.contains(tuv.getId())) {
                    tuvs.add(tuv);
                }
            }
        }

        return tuvs;
    }
    
    // Note for implementors: targetLocales may be null, but will not be empty
    protected abstract StatementBuilder getExactMatchStatement(T key, 
                            TM3Locale sourceLocale,
                            Set<? extends TM3Locale> targetLocales,
                            Map<TM3Attribute, Object> inlineAttrs,
                            boolean lookupTarget);

    /**
     * Generate SQL to apply attribute matching as a filter to another
     * result (which is nested as a table, to avoid MySQL's troubles with
     * subquery performance).
     */
    protected abstract StatementBuilder getAttributeMatchWrapper(
                StatementBuilder inner, Map<TM3Attribute, String> attributes);
    
    protected TM3EventLog loadEvents(ResultSet rs) throws SQLException {
        List<TM3Event> events = new ArrayList<TM3Event>();
        while (rs.next()) {
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

    public abstract List<Object> getAllAttrValues(TM3Attribute attr) throws SQLException;
    
    protected Set<TM3Locale> loadLocales(Collection<Long> ids) {
        Set<TM3Locale> locales = new HashSet<TM3Locale>();
        for (Long id : ids) {
            locales.add(storage.getTm().getDataFactory()
                    .getLocaleById(storage.getSession(), id));
        }
        return locales;
    }
    
    // 
    // Data Handle backends
    //
    
    public abstract long getTuCount(Date start, Date end) 
                        throws SQLException;
    
    public abstract long getTuvCount(Date start, Date end) 
                        throws SQLException;

    public abstract void deleteTus(Date start, Date end) 
                        throws SQLException;
    
    public abstract long getTuCountByLocale(TM3Locale locale,
                        Date start, Date end) throws SQLException;
    
    public abstract long getTuvCountByLocale(TM3Locale locale,
                        Date start, Date end) throws SQLException;

    public abstract void deleteTuvsByLocale(TM3Locale locale)
                        throws SQLException;

    public abstract long getTuCountByAttributes(
            Map<TM3Attribute, Object> inlineAttrs,
            Map<TM3Attribute, String> customAttrs,
            Date start, Date end) throws SQLException;

    public abstract long getTuvCountByAttributes(
                Map<TM3Attribute, Object> inlineAttrs,
                Map<TM3Attribute, String> customAttrs,
                Date start, Date end) throws SQLException;
    
    // Paging interface for data handles
    public abstract List<TM3Tu<T>> getTuPage(long startId, int count, 
                Date start, Date end) throws SQLException;
    
    public abstract List<TM3Tu<T>> getTuPageByLocale(long startId, int count, 
            TM3Locale locale, Date start, Date end) throws SQLException;

    public abstract List<TM3Tu<T>> getTuPageByAttributes(long startId, int count, 
            Map<TM3Attribute, Object> inlineAttrs,
            Map<TM3Attribute, String> customAttrs,
            Date start, Date end) throws SQLException;
    
    // Support for the "get tu by tuv" hack.
    protected abstract long getTuIdByTuvId(Long tuvId) throws SQLException;

}
