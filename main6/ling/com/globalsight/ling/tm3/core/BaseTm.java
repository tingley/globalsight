package com.globalsight.ling.tm3.core;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

/**
 * Base TM implementation.
 */
abstract class BaseTm<T extends TM3Data> implements TM3Tm<T> {
    private static Logger LOGGER = Logger.getLogger(BaseTm.class);
    
    private Long id;
    private String tuTableName;
    private String tuvTableName;
    private String indexTableName;
    private String attrValTableName;
    private Set<TM3Attribute> attributes = new HashSet<TM3Attribute>();

    // Injected
    private TM3Manager manager;
    private Session session;
    private TM3DataFactory<T> factory;
    private boolean indexTarget = false;
    
    // Transient
    private StorageInfo<T> storage;

    BaseTm(TM3DataFactory<T> factory) {
        this.factory = factory;
    }
    
    BaseTm() { }

    protected abstract StorageInfo<T> createStorageInfo();
    
    @Override
    public Long getId() {
        return id;
    }
    
    @SuppressWarnings("unused")
    private void setId(Long id) {
        this.id = id;
    }

    public abstract TM3TmType getType();

    StorageInfo<T> getStorageInfo() {
        if (storage == null) {
            storage = createStorageInfo();
        }
        return storage;
    }
    
    String getTuTableName() {
        return tuTableName;
    }

    void setTuTableName(String tuTableName) {
        this.tuTableName = tuTableName;
    }

    String getTuvTableName() {
        return tuvTableName;
    }

    void setTuvTableName(String tuvTableName) {
        this.tuvTableName = tuvTableName;
    }

    String getFuzzyIndexTableName() {
        return indexTableName;
    }

    void setFuzzyIndexTableName(String indexTableName) {
        this.indexTableName = indexTableName;
    }
    
    String getAttrValTableName() {
        return attrValTableName;
    }
    
    void setAttrValTableName(String name) {
        this.attrValTableName = name;
    }
        
    TM3Manager getManager() {
        return manager;
    }
  
    void setManager(TM3Manager manager) {
        this.manager = manager;
    }
    
    public Session getSession() {
        return session;
    }
    
    void setSession(Session session) {
        this.session = session;
    }
    
    public TM3DataFactory<T> getDataFactory() {
        return factory;
    }
    
    void setDataFactory(TM3DataFactory<T> factory) {
        this.factory = factory;
    }
    
    @Override
    public void setIndexTarget(boolean indexTarget) {
        this.indexTarget = indexTarget;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public TM3EventLog getEventLog() throws TM3Exception {
        try {
            return new TM3EventLog(session.createCriteria(TM3Event.class)
                    .add(Restrictions.eq("tm", this))
                    .addOrder(Order.desc("timestamp"))
                    .addOrder(Order.desc("id"))
                    .list());
        }
        catch (HibernateException e) {
            throw new TM3Exception(e);
        }
    }

    @Override
    public Set<TM3Attribute> getAttributes() {
        return attributes;
    }
    
    @SuppressWarnings("unused")
    private void setAttributes(Set<TM3Attribute> attributes) {
        this.attributes = attributes;
    }
    
    @Override
    public TM3Attribute addAttribute(String name) throws TM3Exception {
        try {
            lockForWrite();
            TM3Attribute attr = getAttributeByName(name);
            if (attr != null) {
                return attr;
            }
            attr = new TM3Attribute(this, name);
            addAttribute(attr);
            return attr;
        }
        catch (HibernateException e) {
            throw new TM3Exception(e);
        }
    }

    void addAttribute(TM3Attribute attr) throws HibernateException {
        getSession().persist(attr);
        attributes.add(attr);
    }
    
    @Override
    public void removeAttribute(TM3Attribute attribute) {
        if (attribute == null) {
            throw new IllegalArgumentException("null attribute value");
        }
        if (attribute.isInline()) {
            throw new IllegalArgumentException("can't remove inline attribute");
        }
        attributes.remove(attribute);
    }

    @Override
    public TM3LeverageResults<T> findMatches(T matchKey, 
            TM3Locale sourceLocale, Set<? extends TM3Locale> targetLocales,
            Map<TM3Attribute, Object> attributes, TM3MatchType matchType, boolean lookupTarget) 
            throws TM3Exception {
        return findMatches(matchKey, sourceLocale, targetLocales, 
                           attributes, matchType, lookupTarget, 
                           Integer.MAX_VALUE, 0);
    }
    
    @Override
    public TM3LeverageResults<T> findMatches(T matchKey, 
                TM3Locale sourceLocale, Set<? extends TM3Locale> targetLocales,
                Map<TM3Attribute, Object> attributes, TM3MatchType matchType, 
                boolean lookupTarget, int maxResults, int threshold)
                throws TM3Exception {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("findMatches: key=" + matchKey + ", srcLoc=" + 
                    sourceLocale + ", type=" + matchType + ", max=" + 
                    maxResults + ", threhold=" + threshold);
        }
        if (attributes == null) {
            attributes = TM3Attributes.NONE;
        }        
        TM3LeverageResults<T> results = 
            new TM3LeverageResults<T>(matchKey, attributes);
        Connection conn = getSession().connection();
        Map<TM3Attribute, Object> inlineAttributes =
            getInlineAttributes(attributes);
        Map<TM3Attribute, String> customAttributes =
            getCustomAttributes(attributes);
        int count = 0;
        try {
            switch (matchType) {
            case EXACT:
                getExactMatches(conn, results, matchKey, sourceLocale, targetLocales, inlineAttributes, customAttributes, maxResults, lookupTarget);
                break;
            case ALL:
                count = getExactMatches(conn, results, matchKey, sourceLocale, targetLocales, inlineAttributes, customAttributes, maxResults, lookupTarget);
                if (count < maxResults) {
                    getFuzzyMatches(conn, results, matchKey, sourceLocale, 
                                targetLocales, inlineAttributes, customAttributes, maxResults, threshold, lookupTarget);
                }
                break;
            case FALLBACK:
                count = getExactMatches(conn, results, matchKey, sourceLocale, targetLocales, inlineAttributes, customAttributes, maxResults, lookupTarget);
                if (count == 0) {
                    getFuzzyMatches(conn, results, matchKey, sourceLocale, 
                                        targetLocales, inlineAttributes, customAttributes, maxResults, threshold, lookupTarget);
                }
                break;
            }
        } catch (SQLException e) {
            throw new TM3Exception(e);
        }
        return results;
    }

    protected int getExactMatches(Connection conn,
            TM3LeverageResults<T> results,
            T matchKey, TM3Locale sourceLocale, 
            Set<? extends TM3Locale> targetLocales,
            Map<TM3Attribute, Object> inlineAttributes,
            Map<TM3Attribute, String> customAttributes,
            int maxResults,
            boolean lookupTarget) throws SQLException {
        
        int count = 0;
        long start = System.currentTimeMillis();
        List<TM3Tuv<T>> exactTuv = getStorageInfo().getTuStorage()
            .getExactMatches(conn, matchKey, sourceLocale, 
                         targetLocales, inlineAttributes, customAttributes, lookupTarget, false);
        for (TM3Tuv<T> exactMatch : exactTuv) {
            if (count++ >= maxResults) {
                break;
            }
            results.addExactMatch(exactMatch.getTu(), exactMatch);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Exact match: TU " + exactMatch.getTu().getId() + 
                        " TUV " + exactMatch.getId() + "; " + exactMatch);
            }
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Exact match lookup found " + count + 
                " results in " + (System.currentTimeMillis() - start) + "ms");
        }
        return count;
    }
    
    protected void getFuzzyMatches(Connection conn,
            TM3LeverageResults<T> results, T matchKey,
            TM3Locale sourceLocale, Set<? extends TM3Locale> targetLocales,
            Map<TM3Attribute, Object> inlineAttributes,
            Map<TM3Attribute, String> customAttributes,
            int maxResults, int threshold,
            boolean lookupTarget) throws SQLException {

        long start = System.currentTimeMillis();
        if (maxResults < 0) {
            throw new IllegalArgumentException("Invalid threshold: " + 
                                               threshold);
        }
        
        List<FuzzyCandidate<T>> candidates = getStorageInfo().getFuzzyIndex()
                .lookup(matchKey, sourceLocale, targetLocales,
                        inlineAttributes, customAttributes, maxResults, lookupTarget);

        SortedSet<FuzzyCandidate<T>> sorted = 
            new TreeSet<FuzzyCandidate<T>>(FuzzyCandidate.COMPARATOR);

        // Score and sort all the results, then select best maxResults
        for (FuzzyCandidate<T> candidate : candidates) {
            float score = getDataFactory().getFuzzyMatchScorer()
                    .score(matchKey, candidate.getContent(), sourceLocale); 
            // Fix any errant scoring
            if (score < 0) score = 0;
            if (score > 1) score = 1;
            
            int normalizedScore = (int)(score * 100);
            if (normalizedScore >= threshold) {
                candidate.setScore(normalizedScore);
                sorted.add(candidate);
            }
        }
        
        // Now take the n highest, load them completely, and return them.
        // There are some complications here.  We want to avoid returning 
        // the same match as both exact and fuzzy.  But we also want to 
        // make sure that by filtering duplicates we end up with 
        // too few results.  A better implementation might take the
        // exact match tuv ids and integrate them into the fuzzy query
        // as an exclude.
        candidates.clear();
        Set<Long> exactTuvIds = new HashSet<Long>();
        for (TM3LeverageMatch<T> match : results.getMatches()) {
            exactTuvIds.add(match.getTuv().getId());
        }
        int max = maxResults - results.getMatches().size();
        Iterator<FuzzyCandidate<T>> it = sorted.iterator();
        while (candidates.size() < max && it.hasNext()) {
            FuzzyCandidate<T> c = it.next();
            if (!exactTuvIds.contains(c.getId())) {
                candidates.add(c);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Fuzzy Match " + c);
                }
            }
        }
        
        getStorageInfo().getTuStorage().loadLeverageMatches(candidates, results);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Fuzzy match lookup found " + 
                results.getMatches().size() + " results in " + 
                (System.currentTimeMillis() - start) + "ms");
        }
    }
   
    @Override
    public TM3Saver<T> createSaver() {
        return new BaseSaver<T>(this);
    }
    
    @Override
    public TM3Tu<T> save(TM3Locale srcLocale, T source, 
            Map<TM3Attribute, Object> attributes,
            TM3Locale tgtLocale, T target, 
            TM3SaveMode mode, TM3Event event) throws TM3Exception {
        if (event == null) {
            throw new IllegalArgumentException("Null event value");
        }
        TM3Saver<T> saver = createSaver();
        saver.tu(source, srcLocale, event)
             .attrs(attributes)
             .target(target, tgtLocale, event);
        return saver.save(mode).get(0);             
    }
        
    @Override
    public TM3Tu<T> save(TM3Locale srcLocale, T source,
            Map<TM3Attribute, Object> attributes, Map<TM3Locale, T> targets, 
            TM3SaveMode mode, TM3Event event) throws TM3Exception {
        if (event == null) {
            throw new IllegalArgumentException("Null event value");
        }
        TM3Saver<T> saver = createSaver();
        saver.tu(source, srcLocale, event)
             .attrs(attributes)
             .targets(targets, event);
        return saver.save(mode).get(0);
    }
        
    /**
     * Save one or more TUs containing arbitary quantities of data.
     * @param saver
     * @param mode
     * @return TUs corresponding to the save requests made.  Depending on data and
     *          save mode, these may not actually be new TUs (or even new TUVs).
     * @throws TM3Exception
     */
    List<TM3Tu<T>> save(TM3Saver<T> saver, TM3SaveMode mode) throws TM3Exception {
        List<TM3Tu<T>> saved = new ArrayList<TM3Tu<T>>();
        TuStorage<T> tuStorage = getStorageInfo().getTuStorage();
        Connection conn = getSession().connection();
        try {
            // Lock the TM to avoid racing
            lockForWrite();
            for (TM3Saver<T>.Tu tuData : saver.tus) {
                Map<TM3Attribute, Object> inlineAttributes =
                    getInlineAttributes(tuData.attrs);
                Map<TM3Attribute, String> customAttributes =
                    getCustomAttributes(tuData.attrs);
                TM3Tu<T> tu = findTuForSave(conn, tuData.srcTuv.content, 
                        tuData.srcTuv.locale, inlineAttributes, customAttributes);
                if (tu == null) {
                    tu = tuStorage.createTu(tuData.srcTuv.locale, 
                            tuData.srcTuv.content, tuData.attrs,
                            tuData.srcTuv.event); // Includes source tuv
                    for (TM3Saver<T>.Tuv tuvData : tuData.targets) {
                        tu.addTargetTuv(tuvData.locale, tuvData.content, tuvData.event);
                    }
                    tuStorage.saveTu(conn, tu);
                    getStorageInfo().getFuzzyIndex().index(tu.getSourceTuv());
                    if (indexTarget) {
                        for (TM3Tuv<T> tuv : tu.getTargetTuvs()) {
                            getStorageInfo().getFuzzyIndex().index(tuv);
                        }
                    }
                }
                else {
                    List<TM3Tuv<T>> addedTuv = new ArrayList<TM3Tuv<T>>();
                    List<TM3Tuv<T>> deletedTuv = new ArrayList<TM3Tuv<T>>();
                    switch (mode) {
                    case DISCARD:
                        break;
                    case OVERWRITE:
                        // collect up the new locales, remove everything
                        // currently in those locales, then add the new tuvs
                        Set<TM3Locale> targetLocales = new HashSet<TM3Locale>();
                        for (TM3Saver<T>.Tuv tuv : tuData.targets) {
                            targetLocales.add(tuv.locale);
                        }
                        for (TM3Locale locale : targetLocales) {
                            deletedTuv.addAll(tu.getLocaleTuvs(locale));
                            tu.removeTargetTuvByLocale(locale);
                        }
                        for (TM3Saver<T>.Tuv tuvData : tuData.targets) {
                            // they could have given use the same data twice
                            TM3Tuv<T> newTuv = tu.addTargetTuv(tuvData.locale,
                                    tuvData.content, tuvData.event);
                            if (newTuv != null) {
                                addedTuv.add(newTuv);
                            }
                        }
                        break;
                    case MERGE:
                        for (TM3Saver<T>.Tuv tuvData : tuData.targets) {
                            TM3Tuv<T> newTuv = tu.addTargetTuv(tuvData.locale,
                                    tuvData.content, tuvData.event);
                            if (newTuv != null) {
                                addedTuv.add(newTuv);
                            }
                        }
                        break;
                    }
                    tuStorage.addTuvs(tu, addedTuv);
                    tuStorage.deleteTuvs(deletedTuv);
                    if (indexTarget) {
                        for (TM3Tuv<T> tuv : addedTuv) {
                            getStorageInfo().getFuzzyIndex().index(tuv);
                        }
                    }
                }
                saved.add(tu);
            }
        }
        catch (HibernateException e) {
            throw new TM3Exception(e);
        }
        catch (SQLException e) {
            throw new TM3Exception(e);
        }
        return saved;
    }
    
    /**
     * The exact match lookup guarantees that everything it returns
     * has all of the specified attribute values, but it doesn't ensure
     * that the segment doesn't have OTHER attributes as well.  
     * (That is, it only bounds in one direction.)  So an extra
     * filtering step is required to check the upper bound.  The easiest
     * way to do this is just compared the attribute counts.
     */
    private TM3Tu<T> findTuForSave(Connection conn, T source,
            TM3Locale srcLocale,
            Map<TM3Attribute, Object> inlineAttributes,
            Map<TM3Attribute, String> customAttributes)
            throws SQLException {
        List<TM3Tuv<T>> tuvs = getStorageInfo().getTuStorage()
                .getExactMatches(conn, source, srcLocale, null,
                                 inlineAttributes, customAttributes, false, true);
        List<TM3Tuv<T>> filtered = new ArrayList<TM3Tuv<T>>();
        int desiredAttrCount = requiredCount(inlineAttributes.keySet()) +
                               requiredCount(customAttributes.keySet());
        for (TM3Tuv<T> tuv : tuvs) {
            if (requiredCount(tuv.getTu().getAttributes().keySet()) 
                                        == desiredAttrCount) {
                filtered.add(tuv);
            }
        }
        return filtered.size() == 0 ? null : filtered.get(0).getTu();
    }

    private int requiredCount(Set<TM3Attribute> attrs) {
        int required = 0;
        for (TM3Attribute attr : attrs) {
            if (attr.getAffectsIdentity()) {
                required++;
            }
        }
        return required;
    }

    @Override
    public TM3Attribute getAttributeByName(String name) {
        for (TM3Attribute attr : getAttributes()) {
            if (attr.getName().equals(name)) {
                return attr;
            }
        }
        return null;
    }

    @Override
    public TM3Tu<T> modifyTu(TM3Tu<T> tu, TM3Event event) throws TM3Exception {
        // The strategy for now is to blindly replace the existing data.
        // This could be simpler if we just deleted the old rows, but then we 
        // would lose the segment history.
        if (tu.getId() == null) {
            throw new IllegalArgumentException("Can't update a transient TU");
        }
        if (event == null) {
            throw new IllegalArgumentException("Null event value");
        }
        
        TuStorage<T> storage = getStorageInfo().getTuStorage();
        try {
            // Lock the TM to avoid racing
            lockForWrite();
            
            TM3Tu<T> copy = storage.getTu(tu.getId(), true);
            
            // Check for source updates.  If there is one, we delete the
            // existing TU and recreate it, in order to both re-persist
            // all the proper data and also handle merging with another TU.
            // XXX this causes a glitch in the search and replace UI: if you
            // change the source, it shows a blank result because it can't find
            // the old tu id
            TM3Tuv<T> srcTuv = tu.getSourceTuv();
            if (!copy.getSourceTuv().getContent().equals(srcTuv.getContent())) {
                // XXX If I ever lazily load parts of TU data, this will break
                // in possibly nasty ways.  I would need to do something like
                // tu.ensureLoaded() first.
                storage.deleteTu(tu);
                TM3Saver<T> saver = createSaver();
                TM3Saver<T>.Tu newTu = 
                    saver.tu(srcTuv.getContent(), srcTuv.getLocale(), event)
                         .attrs(tu.getAttributes());
                for (TM3Tuv<T> tuv : tu.getTargetTuvs()) {
                    newTu.target(tuv.getContent(), tuv.getLocale(), event);
                }
                List<TM3Tu<T>> saved = save(saver, TM3SaveMode.MERGE);
                return saved.get(0);
            }
            
            // Now check target TUVs
            Map<Long, TM3Tuv<T>> oldMap = buildIdMap(copy.getTargetTuvs());
            Map<Long, TM3Tuv<T>> newMap = buildIdMap(tu.getTargetTuvs());
            // First, check for deleted or added TUVs
            List<TM3Tuv<T>> deleted = new ArrayList<TM3Tuv<T>>();
            for (TM3Tuv<T> tuv : oldMap.values()) {
                if (!newMap.containsKey(tuv.getId())) {
                    deleted.add(tuv);
                }
            }
            // Now check for added... modified at the same time?
            List<TM3Tuv<T>> added = new ArrayList<TM3Tuv<T>>();
            List<TM3Tuv<T>> modified = new ArrayList<TM3Tuv<T>>();
            for (TM3Tuv<T> tuv : newMap.values()) {
                TM3Tuv<T> oldTuv = oldMap.get(tuv.getId());
                if (oldTuv == null) {
                    added.add(tuv);
                }
                else if (!oldTuv.getSerializedForm()
                                .equals(tuv.getSerializedForm())) {
                    modified.add(tuv);
                }
            }
            storage.deleteTuvs(deleted);
            storage.addTuvs(tu, added);
            storage.updateTuvs(tu, modified, event);
            // delete old fingerprints from updated tuv even without
            // indexTarget, because it might have been indexed in the past
            for (TM3Tuv<T> tuv : modified) {
                getStorageInfo().getFuzzyIndex().deleteFingerprints(tuv);
            }
            if (indexTarget) {
                for (TM3Tuv<T> tuv : added) {
                    getStorageInfo().getFuzzyIndex().index(tuv);
                }
                for (TM3Tuv<T> tuv : modified) {
                    getStorageInfo().getFuzzyIndex().index(tuv);
                }
            }
            
            // Now check the attributes
            if (!copy.getAttributes().equals(tu.getAttributes())) {
                storage.updateAttributes(tu,
                    getInlineAttributes(tu.getAttributes()),
                    getCustomAttributes(tu.getAttributes()));
            }
            
            return tu;
        }
        catch (HibernateException e) {
            throw new TM3Exception(e);
        }
        catch (SQLException e) {
            throw new TM3Exception(e);
        }
    }
   
    Map<Long, TM3Tuv<T>> buildIdMap(List<TM3Tuv<T>> tuvs) {
        Map<Long, TM3Tuv<T>> map = new HashMap<Long, TM3Tuv<T>>();
        for (TM3Tuv<T> tuv : tuvs) {
            map.put(tuv.getId(), tuv);
        }
        return map;
    }
    
    @Override
    public TM3Event addEvent(int type, String username, String arg) {
        return addEvent(type, username, arg, new Date());
    }
    
    public TM3Event addEvent(int type, String username, String arg, Date date) {
        TM3Event event = new TM3Event(this, type, username, arg, date);
        lockForWrite();
        session.persist(event);
        session.flush();
        return event;
    }


    @Override
    public TM3Event getEvent(long id) throws TM3Exception {
        try {
            return (TM3Event)session.createCriteria(TM3Event.class)
                .add(Restrictions.eq("id", id))
                .add(Restrictions.eq("tm", this))
                .uniqueResult();
        }
        catch (HibernateException e) {
            throw new TM3Exception(e);
        }
    }
    
    @Override
    public TM3Tu<T> getTu(long id) throws TM3Exception {
        TuStorage<T> storage = getStorageInfo().getTuStorage();
        try {
            return storage.getTu(id, false);
        }
        catch (SQLException e) {
            throw new TM3Exception(e);
        }
    }

    @Override
    public TM3Tuv<T> getTuv(long tuvId) throws TM3Exception {
        TuStorage<T> storage = getStorageInfo().getTuStorage();
        try {
            TM3Tu<T> tu = storage.getTuByTuvId(tuvId);
            for (TM3Tuv<T> tuv : tu.getAllTuv()) {
                if (tuv.getId().equals(tuvId)) {
                    return tuv;
                }
            }
            return null;
        }
        catch (SQLException e) {
            throw new TM3Exception(e);
        }
    }
    
    @Override
    public TM3Handle<T> getAllData(Date start, Date end) {
        checkDateRange(start, end);
        return new AllTusDataHandle<T>(this, start, end);
    }
    
    @Override
    public TM3Handle<T> getDataByLocale(TM3Locale locale, Date start, Date end) {
        checkDateRange(start, end);
        return new LocaleDataHandle<T>(this, locale, start, end);
    }
    

    @Override
    public TM3Handle<T> getDataById(List<Long> tuIds) {
        return new ByIdDataHandle<T>(this, tuIds);
    }
    
    @Override
    public TM3Handle<T> getDataByAttributes(Map<TM3Attribute, Object> attrs,
            Date start, Date end) {
        checkDateRange(start, end);
        return new AttributeDataHandle<T>(this,
                getInlineAttributes(attrs),
                getCustomAttributes(attrs),
                start, end);
    }
    
    @Override
    public void removeDataByLocale(TM3Locale locale) {
        try {
            getStorageInfo().getTuStorage().deleteTuvsByLocale(locale);
        }
        catch (SQLException e) {
            throw new TM3Exception(e);
        }
    }
   
    @Override
    public Set<TM3Locale> getTuvLocales() throws TM3Exception {
        try {
            return getStorageInfo().getTuStorage().getTuvLocales();
        } 
        catch (SQLException e) {
            throw new TM3Exception(e);
        }
    }
    
    @Override
    public List<Object> getAllAttributeValues(TM3Attribute attr) 
                throws TM3Exception {
        try {
            return getStorageInfo().getTuStorage().getAllAttrValues(attr);
        }
        catch (SQLException e) {
            throw new TM3Exception(e);
        }
    }
    
    
    private void checkDateRange(Date start, Date end) {
        if (start == null && end == null) {
            return;
        }
        if (start != null && end != null) {
            if (end.before(start)) {
                throw new IllegalArgumentException("Invalid date range: end " +
                        end + " comes before start " + start);
            }
            return;
        }
        throw new IllegalArgumentException("Date range not fully specified");
    }

    void lockForWrite() throws TM3Exception {
        session.lock(this, LockMode.UPGRADE);
    }

    // TODO: on save, check that all required attrs are present
    // TODO: make an unchecked version that doesn't call checkValue
    public static Map<TM3Attribute, Object> getInlineAttributes(
            Map<TM3Attribute, Object> attrs) {
        Map<TM3Attribute, Object> r = new HashMap<TM3Attribute, Object>();
        for (Map.Entry<TM3Attribute, Object> e: attrs.entrySet()) {
            TM3Attribute attr = e.getKey();
            if (attr.isInline()) {
                attr.getValueType().checkValue(e.getValue(), attr.getName());
                r.put(attr, e.getValue());
            }
        }
        return r;
    }
    
    public static Map<TM3Attribute, String> getCustomAttributes(
            Map<TM3Attribute, Object> attrs) {
        Map<TM3Attribute, String> r = new HashMap<TM3Attribute, String>();
        for (Map.Entry<TM3Attribute, Object> e: attrs.entrySet()) {
            TM3Attribute attr = e.getKey();
            if (attr.isCustom()) {
                attr.getValueType().checkValue(e.getValue(), attr.getName());
                r.put(attr, (String) e.getValue());
            }
        }
        return r;
    }
}
