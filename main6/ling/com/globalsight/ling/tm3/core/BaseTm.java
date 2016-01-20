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

import static com.globalsight.ling.tm3.integration.segmenttm.SegmentTmAttribute.SID;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
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
import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.ling.tm3.integration.GSTuvData;
import com.globalsight.ling.tm3.integration.segmenttm.TM3Util;
import com.globalsight.ling.tm3.integration.segmenttm.Tm3SegmentTmInfo;
import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * Base TM implementation.
 */
public abstract class BaseTm<T extends TM3Data> implements TM3Tm<T>
{
    private static Logger LOGGER = Logger.getLogger(BaseTm.class);

    private long id;
    private String tuTableName;
    private String tuvTableName;
    private String tuvExtTableName;
    private String indexTableName;
    private String attrValTableName;
    private Set<TM3Attribute> attributes = new HashSet<TM3Attribute>();

    // Injected
    private TM3Manager manager;
    private TM3DataFactory<T> factory;
    private Connection connection = null;
    
    private boolean locked = false;

    // Transient
    private StorageInfo<T> storage;

    BaseTm(TM3DataFactory<T> factory)
    {
        this.factory = factory;
    }

    BaseTm()
    {
    }

    protected abstract StorageInfo<T> createStorageInfo();

    @Override
    public Long getId()
    {
        return id;
    }

    @SuppressWarnings("unused")
    private void setId(Long id)
    {
        this.id = id;
    }

    public abstract TM3TmType getType();

    StorageInfo<T> getStorageInfo()
    {
        if (storage == null)
        {
            storage = createStorageInfo();
        }
        return storage;
    }

    public String getTuTableName()
    {
        return tuTableName;
    }

    public void setTuTableName(String tuTableName)
    {
        this.tuTableName = tuTableName;
    }

    public String getTuvTableName()
    {
        return tuvTableName;
    }

    public void setTuvTableName(String tuvTableName)
    {
        this.tuvTableName = tuvTableName;
    }

    public String getTuvExtTableName()
    {
        return tuvExtTableName;
    }

    public void setTuvExtTableName(String tuvExtTableName)
    {
        this.tuvExtTableName = tuvExtTableName;
    }

    public String getFuzzyIndexTableName()
    {
        return indexTableName;
    }

    public void setFuzzyIndexTableName(String indexTableName)
    {
        this.indexTableName = indexTableName;
    }

    public String getAttrValTableName()
    {
        return attrValTableName;
    }

    public void setAttrValTableName(String name)
    {
        this.attrValTableName = name;
    }

    TM3Manager getManager()
    {
        return manager;
    }

    void setManager(TM3Manager manager)
    {
        this.manager = manager;
    }

    public Session getSession()
    {
        return HibernateUtil.getSession();
    }

    public TM3DataFactory<T> getDataFactory()
    {
        return factory;
    }

    void setDataFactory(TM3DataFactory<T> factory)
    {
        this.factory = factory;
    }

    @SuppressWarnings("unchecked")
    @Override
    public TM3EventLog getEventLog() throws TM3Exception
    {
        try
        {
            return new TM3EventLog(getSession().createCriteria(TM3Event.class)
                    .add(Restrictions.eq("tm", this))
                    .addOrder(Order.desc("timestamp"))
                    .addOrder(Order.desc("id")).list());
        }
        catch (HibernateException e)
        {
            throw new TM3Exception(e);
        }
    }

    @Override
    public Set<TM3Attribute> getAttributes()
    {
        return attributes;
    }

    @SuppressWarnings("unused")
    private void setAttributes(Set<TM3Attribute> attributes)
    {
        this.attributes = attributes;
    }

    @Override
    public TM3Attribute addAttribute(String name) throws TM3Exception
    {
        try
        {
            lockForWrite();
            TM3Attribute attr = getAttributeByName(name);
            if (attr != null)
            {
                return attr;
            }
            attr = new TM3Attribute(this, name);
            addAttribute(attr);
            return attr;
        }
        catch (HibernateException e)
        {
            throw new TM3Exception(e);
        }
    }

    public void addAttribute(TM3Attribute attr) throws HibernateException
    {
        try
        {
            HibernateUtil.save(attr);
        }
        catch (Exception e)
        {
            throw new HibernateException(e);
        }
        attributes.add(attr);
    }

    @Override
    public void removeAttribute(TM3Attribute attribute)
    {
        if (attribute == null)
        {
            throw new IllegalArgumentException("null attribute value");
        }
        if (attribute.isInline())
        {
            throw new IllegalArgumentException("can't remove inline attribute");
        }
        attributes.remove(attribute);
    }

    @Override
    public TM3LeverageResults<T> findMatches(T matchKey,
            TM3Locale sourceLocale, Set<? extends TM3Locale> targetLocales,
            Map<TM3Attribute, Object> attributes, TM3MatchType matchType,
            boolean lookupTarget) throws TM3Exception
    {
        return findMatches(matchKey, sourceLocale, targetLocales, attributes,
                matchType, lookupTarget, Integer.MAX_VALUE, 0);
    }

    @Override
    public TM3LeverageResults<T> findMatches(T matchKey,
            TM3Locale sourceLocale, Set<? extends TM3Locale> targetLocales,
            Map<TM3Attribute, Object> attributes, TM3MatchType matchType,
            boolean lookupTarget, int maxResults, int threshold)
            throws TM3Exception
    {
        List<Long> tm3TmIds = new ArrayList<Long>();
        tm3TmIds.add(getStorageInfo().getId());

        return findMatches(matchKey, sourceLocale, targetLocales, attributes,
                matchType, lookupTarget, maxResults, threshold, tm3TmIds);
    }

    @Override
    public TM3LeverageResults<T> findMatches(T matchKey,
            TM3Locale sourceLocale, Set<? extends TM3Locale> targetLocales,
            Map<TM3Attribute, Object> attributes, TM3MatchType matchType,
            boolean lookupTarget, int maxResults, int threshold,
            List<Long> tm3TmIds) throws TM3Exception
    {
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("findMatches: key=" + matchKey + ", srcLoc="
                    + sourceLocale + ", type=" + matchType + ", max="
                    + maxResults + ", threhold=" + threshold);
        }
        if (attributes == null)
        {
            attributes = TM3Attributes.NONE;
        }
        TM3LeverageResults<T> results = new TM3LeverageResults<T>(matchKey,
                attributes);
        Map<TM3Attribute, Object> inlineAttributes = getInlineAttributes(attributes);
        Map<TM3Attribute, String> customAttributes = getCustomAttributes(attributes);
        int count = 0;
        Connection conn = null;
        try
        {
            conn = DbUtil.getConnection();
            switch (matchType)
            {
                case EXACT:
                    getExactMatches(conn, results, matchKey, sourceLocale,
                            targetLocales, inlineAttributes, customAttributes,
                            maxResults, lookupTarget, tm3TmIds);
                    break;
                case ALL:
                    count = getExactMatches(conn, results, matchKey,
                            sourceLocale, targetLocales, inlineAttributes,
                            customAttributes, maxResults, lookupTarget,
                            tm3TmIds);
            		int max = determineMaxResults(maxResults, tm3TmIds.size(),
            				targetLocales.size());
                    if (count < max)
                    {
                        getFuzzyMatches(conn, results, matchKey, sourceLocale,
                                targetLocales, inlineAttributes,
                                customAttributes, maxResults, threshold,
                                lookupTarget, tm3TmIds);
                    }
                    break;
                case FALLBACK:
                    count = getExactMatches(conn, results, matchKey,
                            sourceLocale, targetLocales, inlineAttributes,
                            customAttributes, maxResults, lookupTarget,
                            tm3TmIds);
                    if (count == 0)
                    {
                        getFuzzyMatches(conn, results, matchKey, sourceLocale,
                                targetLocales, inlineAttributes,
                                customAttributes, maxResults, threshold,
                                lookupTarget, tm3TmIds);
                    }
                    break;
            }
        }
        catch (Exception e)
        {
            throw new TM3Exception(e);
        }
        finally
        {
            DbUtil.silentReturnConnection(conn);
        }
        return results;
    }

    private int getExactMatches(Connection conn,
            TM3LeverageResults<T> results, T matchKey, TM3Locale sourceLocale,
            Set<? extends TM3Locale> targetLocales,
            Map<TM3Attribute, Object> inlineAttributes,
            Map<TM3Attribute, String> customAttributes, int maxResults,
            boolean lookupTarget, List<Long> tm3TmIds) throws SQLException
    {
        int count = 0;
        long start = System.currentTimeMillis();
        List<TM3Tuv<T>> exactTuv = getStorageInfo().getTuStorage()
                .getExactMatches(conn, matchKey, sourceLocale, targetLocales,
                        inlineAttributes, customAttributes, lookupTarget,
                        false, tm3TmIds);
        for (TM3Tuv<T> exactMatch : exactTuv)
        {
            count++;
            results.addExactMatch(exactMatch.getTu(), exactMatch);
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Exact match: TU " + exactMatch.getTu().getId()
                        + " TUV " + exactMatch.getId() + "; " + exactMatch);
            }
        }
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Exact match lookup found " + count + " results in "
                    + (System.currentTimeMillis() - start) + "ms");
        }
        return count;
    }

    private void getFuzzyMatches(Connection conn,
            TM3LeverageResults<T> results, T matchKey, TM3Locale sourceLocale,
            Set<? extends TM3Locale> targetLocales,
            Map<TM3Attribute, Object> inlineAttributes,
            Map<TM3Attribute, String> customAttributes, int maxResults,
            int threshold, boolean lookupTarget, List<Long> tm3TmIds)
            throws SQLException
    {
        long start = System.currentTimeMillis();
        if (maxResults < 0)
        {
            throw new IllegalArgumentException("Invalid maxResults: "
                    + maxResults);
        }
        List<FuzzyCandidate<T>> candidates = new ArrayList<FuzzyCandidate<T>>();
        for (Long tm3TmId : tm3TmIds)
        {
            candidates.addAll(getStorageInfo().getFuzzyIndex().lookup(matchKey,
                    sourceLocale, targetLocales, inlineAttributes,
                    customAttributes, maxResults, lookupTarget, tm3TmId));
        }

        SortedSet<FuzzyCandidate<T>> sorted = new TreeSet<FuzzyCandidate<T>>(
                FuzzyCandidate.COMPARATOR);

        // Score and sort all the results, then select best maxResults
        for (FuzzyCandidate<T> candidate : candidates)
        {
            float score = getDataFactory().getFuzzyMatchScorer().score(
                    matchKey, candidate.getContent(), sourceLocale);
            // Fix any errant scoring
            if (score < 0)
                score = 0;
            if (score > 1)
                score = 1;

            int normalizedScore = (int) (score * 100);
            if (normalizedScore >= threshold)
            {
                candidate.setScore(normalizedScore);
                sorted.add(candidate);
            }
        }

        // Now take the n highest, load them completely, and return them.
        // There are some complications here. We want to avoid returning
        // the same match as both exact and fuzzy. But we also want to
        // make sure that by filtering duplicates we end up with
        // too few results. A better implementation might take the
        // exact match tuv ids and integrate them into the fuzzy query
        // as an exclude.
        candidates.clear();
        Set<Long> exactTuvIds = new HashSet<Long>();
        for (TM3LeverageMatch<T> match : results.getMatches())
        {
            exactTuvIds.add(match.getTuv().getId());
        }

		int max = determineMaxResults(maxResults, tm3TmIds.size(),
				targetLocales.size());
		int fuzzyMax = max - results.getMatches().size();
        Iterator<FuzzyCandidate<T>> it = sorted.iterator();
        while (candidates.size() < fuzzyMax && it.hasNext())
        {
            FuzzyCandidate<T> c = it.next();
            if (!exactTuvIds.contains(c.getId()))
            {
                candidates.add(c);
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("Fuzzy Match " + c);
                }
            }
        }

        getStorageInfo().getTuStorage()
                .loadLeverageMatches(candidates, results);
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Fuzzy match lookup found "
                    + results.getMatches().size() + " results in "
                    + (System.currentTimeMillis() - start) + "ms");
        }
    }

	private int determineMaxResults(int maxResults, int tmSize,
			int targetLocaleSize)
    {
        if (maxResults < Integer.MAX_VALUE)
        {
        	int maxHits = maxResults;
            int max1 = maxHits * (tmSize <= 3 ? 3 : tmSize);
            int max2 = maxHits * targetLocaleSize;
            return Math.max(max1, max2);
        }
        else
        {
        	// maxResults = Integer.MAX_VALUE, want all
            return maxResults;        	
        }
    }

	@Override
    public TM3Saver<T> createSaver()
    {
        return new BaseSaver<T>(this);
    }

    /**
     * Save one or more TUs containing arbitary quantities of data.
     * 
     * @param saver
     * @param mode
     * @return TUs corresponding to the save requests made. Depending on data
     *         and save mode, these may not actually be new TUs (or even new
     *         TUVs).
     * @throws TM3Exception
     */
    List<TM3Tu<T>> save(TM3Saver<T> saver, TM3SaveMode mode, boolean indexTarget)
            throws TM3Exception
    {
        List<TM3Tu<T>> saved = new ArrayList<TM3Tu<T>>();
        TuStorage<T> tuStorage = getStorageInfo().getTuStorage();
        Connection conn = null;
        try
        {
            if (connection == null)
                connection = DbUtil.getConnection();
            conn = connection;
            // Lock the TM to avoid racing
            // lockForWrite();
            for (TM3Saver<T>.Tu tuData : saver.tus)
            {
                Map<TM3Attribute, Object> inlineAttributes = getInlineAttributes(tuData.attrs);
                Map<TM3Attribute, String> customAttributes = getCustomAttributes(tuData.attrs);
                // Always check duplicate in DB
				TM3Tu<T> tu = findTuForSave(conn, tuData.srcTuv,
						inlineAttributes, customAttributes);
                if (tu == null)
                {
                    tu = tuStorage.createTu(tuData.srcTuv.locale,
                            tuData.srcTuv.content, tuData.attrs,
                            tuData.srcTuv.event,
                            tuData.srcTuv.getCreationUser(),
                            tuData.srcTuv.getCreationDate(),
                            tuData.srcTuv.getModifyUser(),
                            tuData.srcTuv.getModifyDate(),
                            tuData.srcTuv.getLastUsageDate(),
                            tuData.srcTuv.getJobId(),
                            tuData.srcTuv.getJobName(),
                            tuData.srcTuv.getPreviousHash(),
                            tuData.srcTuv.getNextHash(),
                            tuData.srcTuv.getSid());
                    for (TM3Saver<T>.Tuv tuvData : tuData.targets)
                    {
                        tu.addTargetTuv(tuvData.locale, tuvData.content,
                                tuvData.event, tuvData.getCreationUser(),
                                tuvData.getCreationDate(),
                                tuvData.getModifyUser(),
                                tuvData.getModifyDate(),
                                tuvData.getLastUsageDate(),
                                tuvData.getJobId(),
                                tuvData.getJobName(),
                                tuvData.getPreviousHash(),
                                tuvData.getNextHash(),
                                tuvData.getSid());
                    }
                    tuStorage.saveTu(conn, tu);
                    getStorageInfo().getFuzzyIndex().index(conn,
                            tu.getSourceTuv());
                    if (indexTarget)
                    {
                        for (TM3Tuv<T> tuv : tu.getTargetTuvs())
                        {
                            getStorageInfo().getFuzzyIndex().index(conn, tuv);
                        }
                    }
                }
                else
                {
                    switch (mode)
                    {
                        case DISCARD:
                            break;
                        case OVERWRITE:
                            // Delete old TU, then create a new TU
                            tuStorage.deleteTu(conn, tu);
                            Tm3SegmentTmInfo.tusRemove
                                    .add((TM3Tu<GSTuvData>) tu);
                            TM3Tu<T> newTu = tuStorage.createTu(
                                    tuData.srcTuv.locale,
                                    tuData.srcTuv.content, tuData.attrs,
                                    tuData.srcTuv.event,
                                    tuData.srcTuv.getCreationUser(),
                                    tuData.srcTuv.getCreationDate(),
                                    tuData.srcTuv.getModifyUser(),
                                    tuData.srcTuv.getModifyDate(),
                                    tuData.srcTuv.getLastUsageDate(),
                                    tuData.srcTuv.getJobId(),
                                    tuData.srcTuv.getJobName(),
                                    tuData.srcTuv.getPreviousHash(),
                                    tuData.srcTuv.getNextHash(),
                                    tuData.srcTuv.getSid());
                            Set<TM3Locale> overWritedLocales = new HashSet<TM3Locale>();
                            for (TM3Saver<T>.Tuv tuvData : tuData.targets)
                            {
                            	overWritedLocales.add(tuvData.locale);
                                newTu.addTargetTuv(tuvData.locale,
                                        tuvData.content, tuvData.event,
                                        tuvData.getCreationUser(),
                                        tuvData.getCreationDate(),
                                        tuvData.getModifyUser(),
                                        tuvData.getModifyDate(),
                                        tuvData.getLastUsageDate(),
                                        tuvData.getJobId(),
                                        tuvData.getJobName(),
                                        tuvData.getPreviousHash(),
                                        tuvData.getNextHash(),
                                        tuvData.getSid());
                            }
                            for (TM3Tuv<T> oldTuv : tu.getTargetTuvs())
                            {
                            	if(!overWritedLocales.contains(oldTuv.getLocale()))
                            	{
                            		newTu.addTargetTuv(oldTuv.getLocale(),
                            				oldTuv.getContent(),
                            				oldTuv.getLatestEvent(),
                            				oldTuv.getCreationUser(),
                            				oldTuv.getCreationDate(),
                            				oldTuv.getModifyUser(),
                            				oldTuv.getModifyDate(),
                            				oldTuv.getLastUsageDate(),
                            				oldTuv.getJobId(),
                            				oldTuv.getJobName(),
                            				oldTuv.getPreviousHash(),
                            				oldTuv.getNextHash(),
                            				oldTuv.getSid());
                            	}
                            }
                            tuStorage.saveTu(conn, newTu);
                            getStorageInfo().getFuzzyIndex().index(conn,
                                    newTu.getSourceTuv());
                            if (indexTarget)
                            {
                                for (TM3Tuv<T> tuv : newTu.getTargetTuvs())
                                {
                                    getStorageInfo().getFuzzyIndex().index(
                                            conn, tuv);
                                }
                            }
                            saved.add(newTu);
                            break;
                        case MERGE:
                            // check if create new tu
                            boolean createTu = false;
                            if (!customAttributes.isEmpty())
                            {
                                for (Map.Entry<TM3Attribute, String> e : customAttributes
                                        .entrySet())
                                {
                                    TM3Attribute tm3a = e.getKey();
                                    String v = e.getValue();

                                    if (tu.getAttributes() != null
                                            && !tu.getAttributes().isEmpty())
                                    {
                                        for (Map.Entry<TM3Attribute, Object> existe : tu
                                                .getAttributes().entrySet())
                                        {
                                            TM3Attribute existstm3a = existe.getKey();
                                            Object existsv = existe.getValue();
                                            
                                            if (existstm3a.getId() == tm3a.getId() && !v.equals(existsv))
                                            {
                                                createTu = true;
                                                break;
                                            }
                                        }
                                    }

                                    if (createTu)
                                    {
                                        break;
                                    }
                                }
                            }
                            
                            //create new tu if the customAttributes are not same
                            TM3Tu<T> newtu = null;
                            if (createTu)
                            {
                                newtu = tuStorage.createTu(
                                        tuData.srcTuv.locale,
                                        tuData.srcTuv.content, tuData.attrs,
                                        tuData.srcTuv.event,
                                        tuData.srcTuv.getCreationUser(),
                                        tuData.srcTuv.getCreationDate(),
                                        tuData.srcTuv.getModifyUser(),
                                        tuData.srcTuv.getModifyDate(),
                                        tuData.srcTuv.getLastUsageDate(),
                                        tuData.srcTuv.getJobId(),
                                        tuData.srcTuv.getJobName(),
                                        tuData.srcTuv.getPreviousHash(),
                                        tuData.srcTuv.getNextHash(),
                                        tuData.srcTuv.getSid());
                                for (TM3Saver<T>.Tuv tuvData : tuData.targets)
                                {
                                    newtu.addTargetTuv(tuvData.locale,
                                            tuvData.content, tuvData.event,
                                            tuvData.getCreationUser(),
                                            tuvData.getCreationDate(),
                                            tuvData.getModifyUser(),
                                            tuvData.getModifyDate(),
                                            tuvData.getLastUsageDate(),
                                            tuvData.getJobId(),
                                            tuvData.getJobName(),
                                            tuvData.getPreviousHash(),
                                            tuvData.getNextHash(),
                                            tuvData.getSid());
                                }
                                tuStorage.saveTu(conn, newtu);
                                getStorageInfo().getFuzzyIndex().index(conn,
                                        newtu.getSourceTuv());
                                if (indexTarget)
                                {
                                    for (TM3Tuv<T> tuv : newtu.getTargetTuvs())
                                    {
                                        getStorageInfo().getFuzzyIndex().index(
                                                conn, tuv);
                                    }
                                }
                                saved.add(newtu);
                            }
                            else
                            {
                                newtu = tu;
                                List<TM3Tuv<T>> addedTuvs = new ArrayList<TM3Tuv<T>>();
                                List<TM3Tuv<T>> updatedTuvs = new ArrayList<TM3Tuv<T>>();
                                for (TM3Saver<T>.Tuv tuvData : tuData.targets)
                                {
                                    TM3Tuv<T> newTuv = tu.addTargetTuv(
                                            tuvData.locale, tuvData.content,
                                            tuvData.event,
                                            tuvData.getCreationUser(),
                                            tuvData.getCreationDate(),
                                            tuvData.getModifyUser(),
                                            tuvData.getModifyDate(),
                                            tuvData.getLastUsageDate(),
                                            tuvData.getJobId(),
                                            tuvData.getJobName(),
                                            tuvData.getPreviousHash(),
                                            tuvData.getNextHash(),
                                            tuvData.getSid());
                                    // Current TU has same TUV already(with same locale, content and hash values).
                                    if (newTuv == null)
                                    {
                                    	findUpdatedTuvs(updatedTuvs, tu, tuvData);
                                    }
                                    else
                                    {
                                    	addedTuvs.add(newTuv);
                                    }
                                }
                                tuStorage.updateTuvs(conn, newtu, updatedTuvs, null);
                                tuStorage.addTuvs(conn, tu, addedTuvs);
                                if (indexTarget)
                                {
                                    for (TM3Tuv<T> tuv : addedTuvs)
                                    {
                                        getStorageInfo().getFuzzyIndex().index(
                                                conn, tuv);
                                    }
                                }
                            }
                            
                            // When merge, update exists attributes or add new
                            // attributes
                            if (!customAttributes.isEmpty())
                            {
                                long tuId = newtu.getId();
                                for (Map.Entry<TM3Attribute, String> e : customAttributes
                                        .entrySet())
                                {
                                    TM3Attribute tm3a = e.getKey();
                                    boolean exists = tuStorage
                                            .doesCustomAttribtueExist(conn,
                                                    tuId, tm3a.getId());

                                    if (exists)
                                    {
                                        tuStorage.updateCustomAttribute(conn,
                                                tuId, tm3a, e.getValue());
                                    }
                                    else
                                    {
                                        tuStorage.saveCustomAttribute(conn,
                                                tuId, tm3a, e.getValue());
                                    }
                                }
                            }
                            break;
                    }
                }
                saved.add(tu);
            }
        }
        catch (Exception e)
        {
            throw new TM3Exception(e);
        }
        return saved;
    }

    /**
     * Decide TUVs that should be updated.
     */
	private void findUpdatedTuvs(List<TM3Tuv<T>> updatedTuvs, TM3Tu<T> tu,
			TM3Saver<T>.Tuv tuvData)
    {
    	for (TM3Tuv<T> tuv : tu.getTargetTuvs())
    	{
			if (tu.isIdenticalTuv(tuv, tuvData.locale, tuvData.content,
					tuvData.previousHash, tuvData.nextHash))
    		{
    			if (tuvData.lastUsageDate != null)
    			{
    				// update lastUsageDate only
					if (tuv.getLastUsageDate() == null
							|| tuvData.lastUsageDate.getTime() > tuv
									.getLastUsageDate().getTime())
    				{
            			tuv.setLastUsageDate(tuvData.lastUsageDate);
    				}
    			}
    			if (tuvData.previousHash != -1)
    			{
    				tuv.setPreviousHash(tuvData.previousHash);
    			}
    			if (tuvData.nextHash != -1)
    			{
    				tuv.setNextHash(tuvData.nextHash);
    			}
    			if (tuvData.jobId != -1)
    			{
    				tuv.setJobId(tuvData.jobId);
    			}
    			if (tuvData.jobName != null)
    			{
    				tuv.setJobName(tuvData.jobName);
    			}
    			if (tuvData.sid != null)
    			{
    				tuv.setSid(tuvData.sid);
    			}

    			updatedTuvs.add(tuv);
    		}
    	}
    }

    /**
     * The exact match lookup guarantees that everything it returns has all of
     * the specified attribute values, but it doesn't ensure that the segment
     * doesn't have OTHER attributes as well. (That is, it only bounds in one
     * direction.) So an extra filtering step is required to check the upper
     * bound. The easiest way to do this is just compared the attribute counts.
     */
	private TM3Tu<T> findTuForSave(Connection conn, TM3Saver<T>.Tuv sourceTuv,
			Map<TM3Attribute, Object> inlineAttributes,
			Map<TM3Attribute, String> customAttributes) throws SQLException
    {
		List<TM3Tuv<T>> tuvs = getStorageInfo().getTuStorage()
				.getExactMatchesForSave(conn, sourceTuv.content,
						sourceTuv.locale, null, inlineAttributes,
						customAttributes, false, true,
						sourceTuv.getPreviousHash(), sourceTuv.getNextHash());
        List<TM3Tuv<T>> filtered = new ArrayList<TM3Tuv<T>>();
        int desiredAttrCount = requiredCount(inlineAttributes.keySet())
                + requiredCount(customAttributes.keySet());
        for (TM3Tuv<T> tuv : tuvs)
        {
            if (requiredCount(tuv.getTu().getAttributes().keySet()) == desiredAttrCount)
            {
                filtered.add(tuv);
            }
        }

        // for custom attributes
        List<TM3Tuv<T>> results = new ArrayList<TM3Tuv<T>>();
        List<TM3Tuv<T>> resultsSameAtt = new ArrayList<TM3Tuv<T>>();
        if (customAttributes == null || customAttributes.size() == 0)
        {
            for (TM3Tuv<T> tuv : filtered)
            {
                TM3Tu<T> tu = tuv.getTu();
                Map<TM3Attribute, Object> atts = tu.getAttributes();

                if (atts == null || atts.size() == 0)
                {
                    results.add(tuv);
                }
                else
                {
                    boolean addme = true;
                    for (Map.Entry<TM3Attribute, Object> att : atts.entrySet())
                    {
                        if (!att.getKey().getName().startsWith("."))
                        {
                            addme = false;
                            break;
                        }
                    }
                    
                    if (addme)
                    {
                        results.add(tuv);
                    }
                }
            }
            
            if (results.size() == 0)
            {
                results = filtered;
            }
        }
        else if (filtered.size() != 0)
        {
            for (TM3Tuv<T> tuv : filtered)
            {
                TM3Tu<T> tu = tuv.getTu();
                Map<TM3Attribute, Object> atts = tu.getAttributes();

                if (atts == null || atts.size() == 0)
                {
                    results.add(tuv);
                }
                else
                {
                    if (atts.entrySet()
                            .containsAll(customAttributes.entrySet()))
                    {
                        resultsSameAtt.add(tuv);
                    }
                    else if (atts.keySet().containsAll(
                            customAttributes.keySet()))
                    {
                        continue;
                    }
                    else
                    {
                        boolean addme = true;
                        boolean addtoSame = false;
                        for (Map.Entry<TM3Attribute, String> ccatt : customAttributes
                                .entrySet())
                        {
                            if (atts.containsKey(ccatt.getKey()))
                            {
                                if (!atts.get(ccatt.getKey()).equals(
                                        ccatt.getValue()))
                                {
                                    addme = false;
                                    break;
                                }
                                else
                                {
                                    addtoSame = true;
                                }
                            }
                        }

                        if (addtoSame && addme)
                        {
                            resultsSameAtt.add(tuv);
                        }
                        else if (addme)
                        {
                            results.add(tuv);
                        }
                    }
                }
            }
            
            if (results.size() == 0)
            {
                results = filtered;
            }
            if (resultsSameAtt.size() != 0)
            {
                results = resultsSameAtt;
            }
        }


        return results.size() == 0 ? null : results.get(0).getTu();
    }

    private int requiredCount(Set<TM3Attribute> attrs)
    {
        int required = 0;
        for (TM3Attribute attr : attrs)
        {
            if (attr.getAffectsIdentity())
            {
                required++;
            }
        }
        return required;
    }

    @Override
    public TM3Attribute getAttributeByName(String name)
    {
        for (TM3Attribute attr : getAttributes())
        {
            if (attr.getName().equals(name))
            {
                return attr;
            }
        }
        return null;
    }

    public boolean doesAttributeExist(String name)
    {
        for (TM3Attribute attr : getAttributes())
        {
            if (attr.getName().equals(name))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public TM3Tu<T> modifyTu(TM3Tu<T> tu, TM3Event event, boolean indexTarget)
            throws TM3Exception
    {
        // The strategy for now is to blindly replace the existing data.
        // This could be simpler if we just deleted the old rows, but then we
        // would lose the segment history.
        if (tu.getId() == null)
        {
            throw new IllegalArgumentException("Can't update a transient TU");
        }
        if (event == null)
        {
            throw new IllegalArgumentException("Null event value");
        }
        // to be safe...
        for (TM3Tuv<T> tuv : tu.getAllTuv())
        {
        	TM3Attribute sidAttr = TM3Util.getAttr((TM3Tm<GSTuvData>) this, SID);
        	Object sid = tu.getAttribute(sidAttr);
        	if (sid != null)
        	{
            	tuv.setSid((String) sid);
        	}
        }
        TuStorage<T> storage = getStorageInfo().getTuStorage();
        Connection conn = null;
        try
        {
            // Lock the TM to avoid racing
            lockForWrite();
            conn = DbUtil.getConnection();

            List<TM3Tuv<T>> deleted = new ArrayList<TM3Tuv<T>>();
            List<TM3Tuv<T>> added = new ArrayList<TM3Tuv<T>>();
            List<TM3Tuv<T>> modified = new ArrayList<TM3Tuv<T>>();

            TM3Tu<T> tuInDb = storage.getTu(tu.getId(), true);
            // For GBS-2442,
            // 1. If edit source, source TUV need to update
            // 2. If the SID or target changed, the modify date and modify user
            // for target TUV should be updated.

            // Now check the attributes
            boolean sidNeedUpdate = false;
            if (!tuInDb.getAttributes().equals(tu.getAttributes()))
            {
                storage.updateAttributes(conn, tu,
                        getInlineAttributes(tu.getAttributes()),
                        getCustomAttributes(tu.getAttributes()));
                sidNeedUpdate = true;
            }
            TM3Tuv<T> srcTuv = tu.getSourceTuv();
			if (!tuInDb.getSourceTuv().getContent().equals(srcTuv.getContent())
					|| (srcTuv.getSid() != null && !srcTuv.getSid().equals(
							tuInDb.getSourceTuv().getSid())))
            {
                modified.add(srcTuv);
            }
            // Now check target TUVs
            Map<Long, TM3Tuv<T>> oldMap = buildIdMap(tuInDb.getTargetTuvs());
            Map<Long, TM3Tuv<T>> newMap = buildIdMap(tu.getTargetTuvs());
            // First, check for deleted or added TUVs
            for (TM3Tuv<T> tuv : oldMap.values())
            {
                if (!newMap.containsKey(tuv.getId()))
                {
                    deleted.add(tuv);
                }
            }
            // Now check for added... modified at the same time?
            for (TM3Tuv<T> tuv : newMap.values())
            {
                TM3Tuv<T> oldTuv = oldMap.get(tuv.getId());
                if (oldTuv == null)
                {
                    added.add(tuv);
                }
                else if (!oldTuv.getSerializedForm().equals(
                        tuv.getSerializedForm()) || sidNeedUpdate)
                {
                    modified.add(tuv);
                }
            }
            storage.deleteTuvs(conn, deleted);
            storage.addTuvs(conn, tu, added);
            storage.updateTuvs(conn, tu, modified, event);
            // delete old fingerprints from updated tuv even without
            // indexTarget, because it might have been indexed in the past
			for (TM3Tuv<T> tuv : modified)
			{
				getStorageInfo().getFuzzyIndex().deleteFingerprints(conn, tuv);
            }

            for (TM3Tuv<T> tuv : added)
            {
            	if (tuv.getId() == srcTuv.getId() || indexTarget)
            	{
            		getStorageInfo().getFuzzyIndex().index(conn, tuv);
            	}
            }

            for (TM3Tuv<T> tuv : modified)
            {
            	if (tuv.getId() == srcTuv.getId() || indexTarget)
            	{
            		getStorageInfo().getFuzzyIndex().index(conn, tuv);
            	}
            }

            return tu;
        }
        catch (Exception e)
        {
            throw new TM3Exception(e);
        }
        finally
        {
            DbUtil.silentReturnConnection(conn);
        }
    }

    Map<Long, TM3Tuv<T>> buildIdMap(List<TM3Tuv<T>> tuvs)
    {
        Map<Long, TM3Tuv<T>> map = new HashMap<Long, TM3Tuv<T>>();
        for (TM3Tuv<T> tuv : tuvs)
        {
            map.put(tuv.getId(), tuv);
        }
        return map;
    }

    @Override
    public TM3Event addEvent(int type, String username, String arg)
    {
        return addEvent(type, username, arg, new Date());
    }

    public TM3Event addEvent(int type, String username, String arg, Date date)
    {
        TM3Event event = new TM3Event(this, type, username, arg, date);
        lockForWrite();
        try
        {
            HibernateUtil.save(event);
        }
        catch (Exception e)
        {
            LOGGER.error("Error saving TM3Event", e);
        }
        return event;
    }

    @Override
    public TM3Event getEvent(long id) throws TM3Exception
    {
        try
        {
            return (TM3Event) getSession().createCriteria(TM3Event.class)
                    .add(Restrictions.eq("id", id))
                    .add(Restrictions.eq("tm", this)).uniqueResult();
        }
        catch (HibernateException e)
        {
            throw new TM3Exception(e);
        }
    }

    @Override
    public TM3Tu<T> getTu(long id) throws TM3Exception
    {
        TuStorage<T> storage = getStorageInfo().getTuStorage();
        try
        {
            return storage.getTu(id, false);
        }
        catch (SQLException e)
        {
            throw new TM3Exception(e);
        }
    }

    @Override
    public List<TM3Tu<T>> getTu(List<Long> ids) throws TM3Exception
    {
        TuStorage<T> storage = getStorageInfo().getTuStorage();
        try
        {
            return storage.getTu(ids, false);
        }
        catch (SQLException e)
        {
            throw new TM3Exception(e);
        }
    }

    @Override
    public TM3Tuv<T> getTuv(long tuvId) throws TM3Exception
    {
        TuStorage<T> storage = getStorageInfo().getTuStorage();
        try
        {
            TM3Tu<T> tu = storage.getTuByTuvId(tuvId);
            if (tu != null)
            {
                for (TM3Tuv<T> tuv : tu.getAllTuv())
                {
                    if (tuv.getId().equals(tuvId))
                    {
                        return tuv;
                    }
                }
            }
            return null;
        }
        catch (SQLException e)
        {
            throw new TM3Exception(e);
        }
    }

    @Override
    public TM3Handle<T> getAllData(Date start, Date end)
    {
        checkDateRange(start, end);
        return new AllTusDataHandle<T>(this, start, end);
    }

	@Override
	public TM3Handle<T> getAllDataByParamMap(Map<String, Object> paramMap)
	{
		if (!paramMap.isEmpty())
		{
			Map<TM3Attribute, Object> attrs = (Map<TM3Attribute, Object>) paramMap
					.get("projectAttr");
			if (attrs != null)
			{
				Map<TM3Attribute, Object> inlineAttrs = getInlineAttributes(attrs);
				Map<TM3Attribute, String> customAttrs = getCustomAttributes(attrs);
				paramMap.remove("projectAttr");
				paramMap.put("inlineAttrs", inlineAttrs);
				paramMap.put("customAttrs", customAttrs);
			}
		}
		return new AllTusDataHandle<T>(this, paramMap);
	}

    @Override
    public TM3Handle<T> getDataByLocales(List<TM3Locale> localeList, Date start, Date end)
    {
        checkDateRange(start, end);
        return new LocaleDataHandle<T>(this, localeList, start, end);
    }

    @Override
    public TM3Handle<T> getDataById(List<Long> tuIds)
    {
        return new ByIdDataHandle<T>(this, tuIds);
    }

    @Override
    public TM3Handle<T> getDataByAttributes(Map<TM3Attribute, Object> attrs,
            Date start, Date end)
    {
        checkDateRange(start, end);
        return new AttributeDataHandle<T>(this, getInlineAttributes(attrs),
                getCustomAttributes(attrs), start, end);
    }

    @Override
    public void removeDataByLocale(TM3Locale locale)
    {
        try
        {
            getStorageInfo().getTuStorage().deleteTuvsByLocale(locale);
        }
        catch (SQLException e)
        {
            throw new TM3Exception(e);
        }
    }

    @Override
    public Set<TM3Locale> getTuvLocales() throws TM3Exception
    {
        try
        {
            return getStorageInfo().getTuStorage().getTuvLocales();
        }
        catch (SQLException e)
        {
            throw new TM3Exception(e);
        }
    }

    @Override
    public List<Object> getAllAttributeValues(TM3Attribute attr)
            throws TM3Exception
    {
        try
        {
            return getStorageInfo().getTuStorage().getAllAttrValues(attr);
        }
        catch (SQLException e)
        {
            throw new TM3Exception(e);
        }
    }

    private void checkDateRange(Date start, Date end)
    {
        if (start == null && end == null)
        {
            return;
        }
        if (start != null && end != null)
        {
            if (end.before(start))
            {
                throw new IllegalArgumentException("Invalid date range: end "
                        + end + " comes before start " + start);
            }
            return;
        }
        throw new IllegalArgumentException("Date range not fully specified");
    }

    void lockForWrite() throws TM3Exception
    {
        if (!locked)
        {
            getSession().buildLockRequest(LockOptions.UPGRADE).lock(this);
            //getSession().lock(this, LockMode.UPGRADE);
            
            locked = true;
        }
    }

    // TODO: on save, check that all required attrs are present
    // TODO: make an unchecked version that doesn't call checkValue
    public static Map<TM3Attribute, Object> getInlineAttributes(
            Map<TM3Attribute, Object> attrs)
    {
        Map<TM3Attribute, Object> r = new HashMap<TM3Attribute, Object>();
        for (Map.Entry<TM3Attribute, Object> e : attrs.entrySet())
        {
            TM3Attribute attr = e.getKey();
            if (attr.isInline())
            {
                attr.getValueType().checkValue(e.getValue(), attr.getName());
                r.put(attr, e.getValue());
            }
        }
        return r;
    }

    public static Map<TM3Attribute, String> getCustomAttributes(
            Map<TM3Attribute, Object> attrs)
    {
        Map<TM3Attribute, String> r = new HashMap<TM3Attribute, String>();
        for (Map.Entry<TM3Attribute, Object> e : attrs.entrySet())
        {
            TM3Attribute attr = e.getKey();
            if (attr.isCustom())
            {
                attr.getValueType().checkValue(e.getValue(), attr.getName());
                r.put(attr, (String) e.getValue());
            }
        }
        return r;
    }

    public void setConnection(Connection connection)
    {
        this.connection = connection;
    }

    public Connection getConnection()
    {
        return this.connection;
    }

    public void recreateFuzzyIndex(List<TM3Tuv<T>> tuvs)
    {
        Connection conn = null;
        try
        {
            conn = DbUtil.getConnection();
            conn.setAutoCommit(false);
            FuzzyIndex<T> fuzzyIndex = getStorageInfo().getFuzzyIndex();

            // delete in batches first.
            int counter = 0;
            List<TM3Tuv<T>> batches = new ArrayList<TM3Tuv<T>>();
            for (TM3Tuv<T> tuv : tuvs)
            {
                batches.add(tuv);
                counter++;
                if (counter == 100)
                {
                    fuzzyIndex.deleteFingerprints(conn, batches);
                    batches.clear();
                    counter = 0;
                }
            }
            if (batches.size() > 0)
            {
                fuzzyIndex.deleteFingerprints(conn, batches);
                batches.clear();
                counter = 0;
            }

            // create fuzzy fingerprints one by one.
            fuzzyIndex.index(conn, tuvs);

            synchronized (this)
            {
                conn.commit();
            }
        }
        catch (Exception e)
        {
            try
            {
                conn.rollback();
            }
            catch (Exception e2)
            {
            }

            throw new TM3Exception(e);
        }
        finally
        {
            DbUtil.silentReturnConnection(conn);
        }
    }
}
