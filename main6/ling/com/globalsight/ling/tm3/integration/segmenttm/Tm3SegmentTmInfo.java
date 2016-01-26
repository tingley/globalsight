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
package com.globalsight.ling.tm3.integration.segmenttm;

import static com.globalsight.ling.tm3.integration.segmenttm.SegmentTmAttribute.FORMAT;
import static com.globalsight.ling.tm3.integration.segmenttm.SegmentTmAttribute.FROM_WORLDSERVER;
import static com.globalsight.ling.tm3.integration.segmenttm.SegmentTmAttribute.SID;
import static com.globalsight.ling.tm3.integration.segmenttm.SegmentTmAttribute.TRANSLATABLE;
import static com.globalsight.ling.tm3.integration.segmenttm.SegmentTmAttribute.TYPE;
import static com.globalsight.ling.tm3.integration.segmenttm.SegmentTmAttribute.UPDATED_BY_PROJECT;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.customAttribute.TMAttributeManager;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.projecthandler.ProjectTM;
import com.globalsight.everest.projecthandler.ProjectTmTuTProp;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.tm.StatisticsInfo;
import com.globalsight.everest.tm.Tm;
import com.globalsight.everest.util.comparator.ComparatorByTmOrder;
import com.globalsight.ling.tm.LingManagerException;
import com.globalsight.ling.tm.TuvBasicInfo;
import com.globalsight.ling.tm2.BaseTmTu;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.ling.tm2.SegmentResultSet;
import com.globalsight.ling.tm2.SegmentTmInfo;
import com.globalsight.ling.tm2.SegmentTmTu;
import com.globalsight.ling.tm2.SegmentTmTuv;
import com.globalsight.ling.tm2.TmCoreManager;
import com.globalsight.ling.tm2.TmUtil;
import com.globalsight.ling.tm2.corpusinterface.TuvMappingHolder;
import com.globalsight.ling.tm2.indexer.Reindexer;
import com.globalsight.ling.tm2.leverage.LeverageDataCenter;
import com.globalsight.ling.tm2.leverage.LeverageMatchResults;
import com.globalsight.ling.tm2.leverage.LeverageMatches;
import com.globalsight.ling.tm2.leverage.LeverageOptions;
import com.globalsight.ling.tm2.leverage.LeveragedTu;
import com.globalsight.ling.tm2.lucene.LuceneIndexWriter;
import com.globalsight.ling.tm2.lucene.LuceneSearcher;
import com.globalsight.ling.tm2.lucene.LuceneUtil;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.ling.tm2.population.UniqueSegmentRepositoryForCorpus;
import com.globalsight.ling.tm2.segmenttm.TMidTUid;
import com.globalsight.ling.tm3.core.BaseTm;
import com.globalsight.ling.tm3.core.DefaultManager;
import com.globalsight.ling.tm3.core.TM3Attribute;
import com.globalsight.ling.tm3.core.TM3AttributeValueType;
import com.globalsight.ling.tm3.core.TM3Attributes;
import com.globalsight.ling.tm3.core.TM3Event;
import com.globalsight.ling.tm3.core.TM3Exception;
import com.globalsight.ling.tm3.core.TM3Handle;
import com.globalsight.ling.tm3.core.TM3ImportHelper;
import com.globalsight.ling.tm3.core.TM3Locale;
import com.globalsight.ling.tm3.core.TM3Manager;
import com.globalsight.ling.tm3.core.TM3SaveMode;
import com.globalsight.ling.tm3.core.TM3Saver;
import com.globalsight.ling.tm3.core.TM3Tm;
import com.globalsight.ling.tm3.core.TM3Tu;
import com.globalsight.ling.tm3.core.TM3Tuv;
import com.globalsight.ling.tm3.integration.GSDataFactory;
import com.globalsight.ling.tm3.integration.GSTuvData;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.SortUtil;
import com.globalsight.util.StringUtil;
import com.globalsight.util.progress.InterruptMonitor;
import com.globalsight.util.progress.ProgressReporter;

/**
 * Segment TM adapter for TM3. This code is somewhat ugly.
 * 
 * IMPORTANT NOTE: TM3 currently mixes Hibernate and raw JDBC calls against
 * connection(). (I would like to switch this to use Hibernate's SQLQuery or
 * Work interfaces, but GlobalSight needs a Hibenate version upgrade before this
 * is possible.)
 * 
 * However, TM3 does not currently manage closing its own connection. As a
 * result, two things are critical: 1) TM3 callers must close the detached
 * connection() 2) To allow for #1, the result of session.connection() MUST be
 * stable.
 * 
 * Basically, this means that sessions need to be opened on injected Connection
 * objects, or some other means needs to be used to prevent Jboss/Hibernate from
 * spinning off a new Connection each time connection() is called on a fresh
 * Session.
 * 
 */
public class Tm3SegmentTmInfo implements SegmentTmInfo
{

    private static final Logger LOGGER = Logger
            .getLogger(Tm3SegmentTmInfo.class);

    private TM3Manager manager = DefaultManager.create();
    
    private boolean lock = false;

    public static List<TM3Tu<GSTuvData>> tusRemove = new ArrayList<TM3Tu<GSTuvData>>();

    protected TM3Tm<GSTuvData> getTM3Tm(Tm tm)
    {
        TM3Tm<GSTuvData> tm3tm = manager.getTm(new GSDataFactory(),
                tm.getTm3Id());
        if (tm3tm == null)
        {
            throw new IllegalArgumentException("Non-existent tm3 tm: "
                    + tm.getTm3Id());
        }
        return tm3tm;
    }

    public TM3Tm<GSTuvData> getTM3Tm(Long tm3TmId)
    {
        TM3Tm<GSTuvData> tm3tm = manager.getTm(new GSDataFactory(), tm3TmId);
        if (tm3tm == null)
        {
            throw new IllegalArgumentException("Non-existent tm3 tm: "
                    + tm3TmId);
        }
        return tm3tm;
    }

    /**
     * Delete TUs. Since the SegmentTmTu are not tightly bound to the underlying
     * TM3 data, we just get a handle to the TUs and delete them in bulk.
     */
    @Override
    public void deleteSegmentTmTus(Tm pTm, Collection<SegmentTmTu> pTus)
            throws Exception
    {
        try
        {
            TM3Tm<GSTuvData> tm3tm = manager.getTm(new GSDataFactory(),
                    pTm.getTm3Id());
            if (tm3tm == null)
            {
                throw new IllegalArgumentException("Non-existent tm3 tm: "
                        + pTm.getTm3Id());
            }

            List<Long> ids = new ArrayList<Long>();
            for (SegmentTmTu tu : pTus)
            {
                ids.add(tu.getId());
            }
            LOGGER.debug("deleteSegmentTmTus: " + describeTm(pTm, tm3tm)
                    + "ids " + ids);

            // Delete the segments by ID
            if (lock)
            {
                tm3tm.getDataById(ids).purge();
            }
            else
            {
                tm3tm.getDataById(ids).purgeWithoutLock();
            }

            // update the Lucene index
            luceneRemoveBaseTus(pTm.getId(), pTus);
        }
        catch (TM3Exception e)
        {
            throw new LingManagerException(e);
        }
    }

    /**
     * Delete TUVs.
     */
    @Override
    public void deleteSegmentTmTuvs(Tm pTm, Collection<SegmentTmTuv> pTuvs)
            throws Exception
    {
        try
        {
            GSDataFactory factory = new GSDataFactory();
            TM3Tm<GSTuvData> tm3tm = manager.getTm(factory, pTm.getTm3Id());
            if (tm3tm == null)
            {
                throw new IllegalArgumentException("Non-existent tm3 tm: "
                        + pTm.getTm3Id());
            }

            /**
             * This is a naive implementation, because this is not currently
             * performance critical. It finds the parent TU IDs, fetches those
             * TU, then individually removes each of the TUVs and updates the
             * owning TUs.
             */
            Map<Long, TM3Tu<GSTuvData>> map = buildTuMap(tm3tm, pTuvs);
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("deleteSegmentTmTuvs: " + describeTm(pTm, tm3tm)
                        + "tuIds " + map.keySet());
            }

            // Do all the deletions
            List<TM3Tu<GSTuvData>> removeTus = new ArrayList<TM3Tu<GSTuvData>>();
            for (SegmentTmTuv stuv : pTuvs)
			{
				long tuId = stuv.getTu().getId();
				TM3Tu<GSTuvData> tu = map.get(tuId);
				if (tu == null)
				{
					LOGGER.warn("Can't delete tuv from non-existent tu " + tuId
							+ ": " + stuv);
					continue;
				}
				tu.removeTargetTuv(
						stuv.getLocale(),
						factory.fromSerializedForm(stuv.getLocale(),
								stuv.getSegment()));
				if (tu.getTargetTuvs().size() == 0)
				{
					removeTus.add(tu);
				}
			}
            // Now update the affected TUs in the DB.
            // XXX This requires an event, even though TM3 doesn't currently
            // record it for TUV deletion
            TM3Event event = tm3tm.addEvent(EventType.TUV_DELETE.getValue(),
                    "system", null);
            List<TM3Tu<GSTuvData>> newTus = new ArrayList<TM3Tu<GSTuvData>>();
            for (TM3Tu<GSTuvData> tu : map.values())
			{
				if (removeTus.size() > 0 && removeTus.contains(tu))
				{
					continue;
				}
				TM3Tu<GSTuvData> newTu = tm3tm.modifyTu(tu, event,
						pTm.isIndexTarget());
				newTus.add(newTu);
			}

            // update the Lucene index
            // target locale lists have changed, so deindex old tus, using the
            // BaseTmTus that haven't been mangled ...
			if (newTus.size() > 0)
			{
				Set<BaseTmTu> tusForRemove = new HashSet<BaseTmTu>();
				for (BaseTmTuv tuv : pTuvs)
				{
					tusForRemove.add(tuv.getTu());
				}
				luceneRemoveBaseTus(pTm.getId(), tusForRemove);
				// ... and index new tus
				luceneIndexTus(pTm.getId(), newTus, pTm.isIndexTarget());
			}
            
			if (removeTus.size() > 0)
			{
				Iterator<TM3Tu<GSTuvData>> tuIt = removeTus.iterator();

				List<SegmentTmTu> removeList = new ArrayList<SegmentTmTu>();
				while (tuIt.hasNext())
				{
					TM3Tu<GSTuvData> tm3tu = tuIt.next();

					if (tm3tu.getTm().getId().equals(pTm.getTm3Id()))
					{
						TM3Attribute typeAttr = TM3Util.getAttr(tm3tm, TYPE);
						TM3Attribute formatAttr = TM3Util
								.getAttr(tm3tm, FORMAT);
						TM3Attribute sidAttr = TM3Util.getAttr(tm3tm, SID);
						TM3Attribute translatableAttr = TM3Util.getAttr(tm3tm,
								TRANSLATABLE);
						TM3Attribute fromWsAttr = TM3Util.getAttr(tm3tm,
								FROM_WORLDSERVER);
						TM3Attribute projectAttr = TM3Util.getAttr(tm3tm,
								UPDATED_BY_PROJECT);

						SegmentTmTu segmentTmTu = TM3Util.toSegmentTmTu(tm3tu,
								pTm.getId(), formatAttr, typeAttr, sidAttr,
								fromWsAttr, translatableAttr, projectAttr);
						removeList.add(segmentTmTu);
					}
					deleteSegmentTmTus(pTm, removeList);
				}
			}
            
        }
        catch (TM3Exception e)
        {
            throw new LingManagerException(e);
        }
    }

    @Override
    public void updateSegmentTmTuvs(Tm pTm, Collection<SegmentTmTuv> pTuvs)
            throws LingManagerException
    {
        try
        {
            GSDataFactory factory = new GSDataFactory();
            TM3Tm<GSTuvData> tm3tm = manager.getTm(factory, pTm.getTm3Id());
            if (tm3tm == null)
            {
                throw new IllegalArgumentException("Non-existent tm3 tm: "
                        + pTm.getTm3Id());
            }

            /**
             * This is a naive implementation, because this is not currently
             * performance critical. It finds the parent TU IDs, fetches those
             * TU, then individually removes each of the TUVs and updates the
             * owning TUs.
             */
            Map<Long, TM3Tu<GSTuvData>> map = buildTuMap(tm3tm, pTuvs);
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("deleteSegmentTmTuvs: " + describeTm(pTm, tm3tm)
                        + "tuIds " + map.keySet());
            }

            String modifyUser = null;
            TM3Tm<GSTuvData> tm = getTM3Tm(pTm);
            for (SegmentTmTuv tuv : pTuvs)
            {
                modifyUser = tuv.getModifyUser();
                if (modifyUser == null) {
                    modifyUser = "system";
                }
                SegmentTmTu stu = (SegmentTmTu) tuv.getTu();
                long tuId = stu.getId();
                TM3Tu<GSTuvData> tu = map.get(tuv.getTu().getId());
                if (tu == null)
                {
                    LOGGER.warn("Can't update tuv from non-existent tu " + tuId
                            + ": " + tuv);
                    continue;
                }
                // SID changed from TM Search Edit Entry page
                TM3Attribute sidAttr = TM3Util.getAttr(tm, SID);
//                if (StringUtil.isNotEmpty(stu.getSID()))
//                {
                    tu.setAttribute(sidAttr, stu.getSID());
//                }
                // Now find the relevant TUV
                for (TM3Tuv<GSTuvData> tm3tuv : tu.getAllTuv())
                {
                    if (tm3tuv.getId().equals(tuv.getId()))
                    {
                        // Update the segment content
                        tm3tuv.setContent(factory.fromSerializedForm(
                                tuv.getLocale(), tuv.getSegment()));
                        tm3tuv.setModifyDate(tuv.getModifyDate());
                        tm3tuv.setModifyUser(modifyUser);
                    }
                }
            }

            TM3Event event = tm3tm.addEvent(EventType.TUV_MODIFY.getValue(),
                    modifyUser, null);
            List<TM3Tu<GSTuvData>> newTus = new ArrayList<TM3Tu<GSTuvData>>();
            for (TM3Tu<GSTuvData> tu : map.values())
            {
                TM3Tu<GSTuvData> newTu = tm3tm.modifyTu(tu, event,
                        pTm.isIndexTarget());
                newTus.add(newTu);
            }

            // update the Lucene index
            // ids may have changed, so deindex old tus ...
            Set<BaseTmTu> tusForRemove = new HashSet<BaseTmTu>();
            for (BaseTmTuv tuv : pTuvs)
            {
                tusForRemove.add(tuv.getTu());
            }
            luceneRemoveBaseTus(pTm.getId(), tusForRemove);
            // ... and index new tus
            luceneIndexTus(pTm.getId(), newTus);
        }
        catch (Exception e)
        {
            throw new LingManagerException(e);
        }
    }

    private Map<Long, TM3Tu<GSTuvData>> buildTuMap(TM3Tm<GSTuvData> tm3tm,
            Collection<? extends BaseTmTuv> tuvs)
    {
        List<Long> tuIds = new ArrayList<Long>();
        for (BaseTmTuv tuv : tuvs)
        {
            tuIds.add(tuv.getTu().getId());
        }

        TM3Handle<GSTuvData> h = tm3tm.getDataById(tuIds);
        Map<Long, TM3Tu<GSTuvData>> map = new HashMap<Long, TM3Tu<GSTuvData>>();
        for (TM3Tu<GSTuvData> tu : h)
        {
            map.put(tu.getId(), tu);
        }
        return map;
    }

    @Override
    public List<SegmentTmTu> getSegmentsById(Tm tm, List<Long> tuIds)
            throws LingManagerException
    {
        try
        {
            TM3Tm<GSTuvData> tm3tm = getTM3Tm(tm);
            Tm3SegmentResultSet result = new Tm3SegmentResultSet(tm, tm3tm,
                    tm3tm.getDataById(tuIds).iterator());
            List<SegmentTmTu> resultList = new ArrayList<SegmentTmTu>();
            while (result.hasNext())
            {
                resultList.add(result.next());
            }
            return resultList;
        }
        catch (TM3Exception e)
        {
            throw new LingManagerException(e);
        }

    }

    @Override
    public SegmentResultSet getAllSegments(Tm tm, long startTUId,
            Connection conn) throws LingManagerException
    {
        return getAllSegments(tm, null, null, null);
    }

    @Override
    public SegmentResultSet getAllSegments(Tm tm, String createdBefore,
            String createdAfter, Connection conn) throws LingManagerException
    {
        try
        {
            TM3Tm<GSTuvData> tm3tm = getTM3Tm(tm);
            LOGGER.info("getAllSgments: " + describeTm(tm, tm3tm)
                    + " createdBefore " + createdBefore + " createdAfter "
                    + createdAfter);

            return new Tm3SegmentResultSet(tm, tm3tm, tm3tm.getAllData(
                    parseDate(createdAfter), parseDate(createdBefore))
                    .iterator());
        }
        catch (TM3Exception e)
        {
            throw new LingManagerException(e);
        }
    }

	@Override
	public SegmentResultSet getAllSegmentsByParamMap(Tm tm,
			Map<String, Object> paramMap, Connection conn)
			throws LingManagerException
	{
		try
		{
			TM3Tm<GSTuvData> tm3tm = getTM3Tm(tm);
			LOGGER.info("getAllSgments: " + describeTm(tm, tm3tm));
			if (!paramMap.isEmpty())
			{
				String projectName = (String) paramMap.get("projectName");
				if (StringUtil.isNotEmpty(projectName))
				{
					TM3Attribute projectAttr = TM3Util.getAttr(tm3tm,
							UPDATED_BY_PROJECT);
					Map<TM3Attribute, Object> attrs = TM3Attributes.one(
							projectAttr, projectName);
					paramMap.remove("projectName");
					paramMap.put("projectAttr", attrs);
				}
			}
			return new Tm3SegmentResultSet(tm, tm3tm, tm3tm
					.getAllDataByParamMap(paramMap).iterator());
		}
		catch (TM3Exception e)
		{
			throw new LingManagerException(e);
		}
	}

    @Override
    public int getSegmentCountForReindex(Tm tm)
    {
        return getAllSegmentsCount(tm, null, null);
    }

    @Override
    public int getAllSegmentsCount(Tm tm, String createdBefore,
            String createdAfter) throws LingManagerException
    {
        try
        {
            TM3Tm<GSTuvData> tm3tm = getTM3Tm(tm);
            LOGGER.info("getAllSgments: " + describeTm(tm, tm3tm)
                    + " createdBefore " + createdBefore + " createdAfter "
                    + createdAfter);

            return Long.valueOf(
                    tm3tm.getAllData(parseDate(createdAfter),
                            parseDate(createdBefore)).getCount()).intValue();
        }
        catch (TM3Exception e)
        {
            throw new LingManagerException(e);
        }
    }

	@Override
	public int getAllSegmentsCountByParamMap(Tm tm, Map<String, Object> paramMap)
			throws LingManagerException
	{
		try
		{
			TM3Tm<GSTuvData> tm3tm = getTM3Tm(tm);
			LOGGER.info("getAllSgments: " + describeTm(tm, tm3tm)
					+ " paramMap " + paramMap);

			if (!paramMap.isEmpty())
			{
				String projectName = (String) paramMap.get("projectName");
				if (StringUtil.isNotEmpty(projectName))
				{
					TM3Attribute projectAttr = TM3Util.getAttr(tm3tm,
							UPDATED_BY_PROJECT);
					Map<TM3Attribute, Object> attrs = TM3Attributes.one(
							projectAttr, projectName);
					paramMap.remove("projectName");
					paramMap.put("projectAttr", attrs);
				}
			}
			
			return Long.valueOf(
					tm3tm.getAllDataByParamMap(paramMap).getCount())
					.intValue();
		}
		catch (TM3Exception e)
		{
			throw new LingManagerException(e);
		}
	}

    @Override
    public String getSidByTuvId(Tm tm, long tuvId)
    {
        try
        {
            String sid = null;
            TM3Tm<GSTuvData> tm3tm = getTM3Tm(tm);
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("getSidByTuvId(" + tuvId + "): "
                        + describeTm(tm, tm3tm));                
            }
            TM3Tuv<GSTuvData> tuv = tm3tm.getTuv(tuvId);
            if (tuv != null)
            {
                sid = (String) tuv.getTu().getAttribute(
                        TM3Util.getAttr(tm3tm, SID));
            }
            return sid;
        }
        catch (TM3Exception e)
        {
            throw new LingManagerException(e);
        }
    }

    @Override
    public String getSourceTextByTuvId(Tm tm, long tuvId, long srcLocaleId)
    {
        try
        {
            String sourceText = null;
            TM3Tm<GSTuvData> tm3tm = getTM3Tm(tm);
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("getSourceTextByTuvId(" + tuvId + ", locale="
                        + srcLocaleId + "): " + describeTm(tm, tm3tm));                
            }
            GlobalSightLocale srcLocale = (GlobalSightLocale) HibernateUtil
                    .get(GlobalSightLocale.class, srcLocaleId);
            TM3Tuv<GSTuvData> tuv = tm3tm.getTuv(tuvId);

            if (tuv != null)
            {
                if (srcLocale.equals(tuv.getLocale()))
                {
                    sourceText = tuv.getContent().getData();
                }
                else
                {
                    for (TM3Tuv<GSTuvData> otherTuv : tuv.getTu().getAllTuv())
                    {
                        if (otherTuv.getLocale().equals(srcLocale))
                        {
                            sourceText = otherTuv.getContent().getData();
                        }
                    }
                }
            }

            return sourceText;
        }
        catch (TM3Exception e)
        {
            throw new LingManagerException(e);
        }
    }

    @Override
    public StatisticsInfo getStatistics(Tm pTm, Locale pUILocale,
            boolean pIncludeProjects) throws LingManagerException
    {
        try
        {
            TM3Tm<GSTuvData> tm3tm = getTM3Tm(pTm);
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("getStatistics: " + describeTm(pTm, tm3tm)
                        + " includeProjects=" + pIncludeProjects);             
            }

            StatisticsInfo stats = new StatisticsInfo();
            TM3Handle<GSTuvData> handle = tm3tm.getAllData(null, null);
            int tuCount = (int) handle.getAllTuCount();

            // We could query this directly, but for now it's cheaper to
            // add it up from the locale data
            int tuvCount = 0;
            List localeList = null;
            GlobalSightLocale locale = null;
            for (TM3Locale l : tm3tm.getTuvLocales())
            {
               locale = (GlobalSightLocale) l;
               localeList = new ArrayList();
               localeList.add(locale);
                int localeTuCount = (int) handle.getTuCountByLocale(locale.getId());
                int localeTuvCount = (int) handle.getTuvCountByLocale(localeList);
                stats.addLanguageInfo(locale.getId(), locale.getLocale(),
                        locale.getLocale().getDisplayName(pUILocale),
                        localeTuCount, localeTuvCount);
                tuvCount += localeTuvCount;
            }
            stats.setTm(pTm.getName());
            stats.setTUs(tuCount);
            stats.setTUVs(tuvCount);

            if (pIncludeProjects)
            {
                TM3Attribute projectAttr = TM3Util.getAttr(tm3tm,
                        UPDATED_BY_PROJECT);
                List<Object> projectValues = tm3tm
                        .getAllAttributeValues(projectAttr);
                for (Object pv : projectValues)
                {
                    // it does not need to show the count info in page
                    stats.addUpdateProjectInfo((String) pv, 0, 0);
                }
            }
            return stats;
        }
        catch (TM3Exception e)
        {
            throw new LingManagerException(e);
        }
    }
    
	@Override
	public StatisticsInfo getTmExportInformation(Tm pTm, Locale pUILocale)
			throws LingManagerException
	{
		try
		{
			TM3Tm<GSTuvData> tm3tm = getTM3Tm(pTm);
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug("getTmExportInformation: "
						+ describeTm(pTm, tm3tm));
			}

			StatisticsInfo stats = new StatisticsInfo();
			TM3Handle<GSTuvData> handle = tm3tm.getAllData(null, null);
			int tuCount = (int) handle.getAllTuCount();

			// We could query this directly, but for now it's cheaper to
			// add it up from the locale data
			GlobalSightLocale locale = null;
			for (TM3Locale l : tm3tm.getTuvLocales())
			{
				locale = (GlobalSightLocale) l;
				stats.addLanguageInfo(locale.getId(), locale.getLocale(),
						locale.getLocale().getDisplayName(pUILocale), 0, 0);
			}
			stats.setTm(pTm.getName());
			stats.setTUs(tuCount);
			stats.setTUVs(0);

			TM3Attribute projectAttr = TM3Util.getAttr(tm3tm,
					UPDATED_BY_PROJECT);
			List<Object> projectValues = tm3tm
					.getAllAttributeValues(projectAttr);
			for (Object pv : projectValues)
			{
				// it does not need to show the count info in page
				stats.addUpdateProjectInfo((String) pv, 0, 0);
			}
			return stats;
		}
		catch (TM3Exception e)
		{
			throw new LingManagerException(e);
		}
	}
    
    /**
     * Delete all TM data.
     */
    @Override
    public boolean removeTmData(Tm pTm, ProgressReporter pReporter,
            InterruptMonitor pMonitor) throws LingManagerException
    {
        try
        {
            String tmName = pTm.getName();
            TM3Tm<GSTuvData> tm3tm = getTM3Tm(pTm);
            LOGGER.info("Removing data for " + describeTm(pTm, tm3tm));
            // pReporter.setMessageKey("lb_tm_remove_removing_tm",
            // "Removing TM data ...");
            // pReporter.setPercentage(35);
            manager.removeTm(tm3tm);
            // Remove the dependency on the deleted TM
            pTm.setTm3Id(null);
            // pReporter.setPercentage(90);
            LuceneIndexWriter.removeTm(pTm.getId());
            pReporter.setMessageKey("", tmName
                    + " has been successfully removed.");
            // pReporter.setPercentage(100);
            return true;
        }
        catch (Exception e)
        {
            throw new LingManagerException(e);
        }
    }

	@Override
    public boolean removeTmData(Tm pTm, GlobalSightLocale pLocale,
            ProgressReporter pReporter, InterruptMonitor pMonitor)
            throws LingManagerException
    {
        try
        {
            String tmName = pTm.getName();
            TM3Tm<GSTuvData> tm3tm = getTM3Tm(pTm);
            LOGGER.info("Removing data for " + describeTm(pTm, tm3tm)
                    + " locale " + pLocale);
            // pReporter.setMessageKey("lb_tm_remove_removing_tm",
            // "Removing TM data ...");
            // pReporter.setPercentage(35);
            List localeList = new ArrayList();
            localeList.add(pLocale);
			TM3Handle<GSTuvData> handle = tm3tm.getDataByLocales(localeList,
					null, null);
            //XXX: this is bad for a big TM, should be batched
            List<TM3Tu<GSTuvData>> tus = new ArrayList<TM3Tu<GSTuvData>>();
            for (TM3Tu<GSTuvData> tu : handle)
            {
                tus.add(tu);
            }
            // pReporter.setPercentage(50);

            // Remove lucene index data for specified locale.
            luceneRemoveTM3Tus(pTm.getId(), tus, pLocale);

			List<TM3Tu<GSTuvData>> removeTus = new ArrayList<TM3Tu<GSTuvData>>();
			// Remove specified tuvs then recreate lucene index data.
			for (TM3Tu<GSTuvData> tu : tus)
			{
				tu.removeTargetTuvByLocale(pLocale);
				if (tu.getAllTuv().size() == 1)
				{
					removeTus.add(tu);
				}
			}
			luceneIndexTus(pTm.getId(), tus);
			// pReporter.setPercentage(90);

			// Remove data from DB for specified locale.
			tm3tm.removeDataByLocale(pLocale);

			// Removing TM by language caused single TUV left
			if (removeTus.size() > 0)
			{
				Iterator<TM3Tu<GSTuvData>> tuIt = removeTus.iterator();

				List<SegmentTmTu> removeList = new ArrayList<SegmentTmTu>();
				while (tuIt.hasNext())
				{
					TM3Tu<GSTuvData> tm3tu = tuIt.next();

					if (tm3tu.getTm().getId().equals(pTm.getTm3Id()))
					{
						TM3Attribute typeAttr = TM3Util.getAttr(tm3tm, TYPE);
						TM3Attribute formatAttr = TM3Util
								.getAttr(tm3tm, FORMAT);
						TM3Attribute sidAttr = TM3Util.getAttr(tm3tm, SID);
						TM3Attribute translatableAttr = TM3Util.getAttr(tm3tm,
								TRANSLATABLE);
						TM3Attribute fromWsAttr = TM3Util.getAttr(tm3tm,
								FROM_WORLDSERVER);
						TM3Attribute projectAttr = TM3Util.getAttr(tm3tm,
								UPDATED_BY_PROJECT);

						SegmentTmTu segmentTmTu = TM3Util.toSegmentTmTu(tm3tu,
								pTm.getId(), formatAttr, typeAttr, sidAttr,
								fromWsAttr, translatableAttr, projectAttr);
						removeList.add(segmentTmTu);
					}
				}
				deleteSegmentTmTus(pTm, removeList);
			}

            pReporter.setMessageKey("",
                    tmName + " - " + pLocale.getDisplayName()
                            + " has been successfully removed.");
            // pReporter.setPercentage(100);
            return true;
        }
        catch (Exception e)
        {
            throw new LingManagerException(e);
        }
    }

    @Override
    public LeverageMatchResults leverage(List<Tm> projectTms,
            LeverageDataCenter pLDC, long p_jobId) throws Exception
    {
        try
        {
            LeverageOptions leverageOptions = pLDC.getLeverageOptions();

            // The LeverageMatches objects are keyed by source segments. Rather
            // than do each TM for each segment, do all the segments in a given
            // TM in a row and keep track of the running results.
            Map<BaseTmTuv, LeverageMatches> progress = new HashMap<BaseTmTuv, LeverageMatches>();

            // If tm3 TMs belong to same company, they use the same
            // "tm3_tu_shared_[companyID]", "tm3_tuv_shared_[companyID]" and
            // "tm3_attr_val_shared_[companyID]". Leverage segment across
            // multiple TMs at a time can improve performance.
            if (isInSameCompany(projectTms))
            {
                Set<BaseTmTuv> oriSegments = pLDC
                        .getOriginalSeparatedSegments(p_jobId);
                if (projectTms.size() == 1)
                {
                    LOGGER.info("Leveraging against TM3 TM: "
                            + projectTms.get(0).getName() + " for "
                            + oriSegments.size() + " segments.");
                }
                else
                {
                    LOGGER.info("Leveraging against all " + projectTms.size()
                            + " TM3 TMs for " + oriSegments.size() + " segments.");
                }
                TM3Tm<GSTuvData> firstValidTm = null;
                for (Tm projectTm : projectTms)
                {
                    TM3Tm<GSTuvData> tm = getTM3Tm(projectTm);
                    if (tm != null)
                    {
                        firstValidTm = tm;
                        break;
                    }
                }

                Tm3LeveragerAcrossMutipleTms leverager = new Tm3LeveragerAcrossMutipleTms(
                        projectTms, firstValidTm, pLDC.getSourceLocale(),
                        leverageOptions, progress);
                int segCounter = 0;
                for (BaseTmTuv srcTuv : oriSegments)
                {
                    segCounter++;
                    if (segCounter % 100 == 0)
                    {
                        LOGGER.info("Leveraged segments : " + segCounter);
                    }
                    if (srcTuv.isTranslatable())
                    {
                        leverager.leverageSegment(srcTuv, TM3Attributes.one(
                                TM3Util.getAttr(firstValidTm, TRANSLATABLE),
                                true));
                    }
                    else if (leverageOptions.isLeveragingLocalizables())
                    {
                        leverager.leverageSegment(srcTuv, TM3Attributes.one(
                                TM3Util.getAttr(firstValidTm, TRANSLATABLE),
                                false));
                    }
                }
                LOGGER.info("End to leverage against all TM3 reference TMs.");
            }
            else
            {
                // Leverage each TM separately and build a composite list.
                for (Tm projectTm : projectTms)
                {
                    TM3Tm<GSTuvData> tm = getTM3Tm(projectTm);
                    if (tm == null)
                    {
                        LOGGER.warn("TM " + projectTm.getId()
                                + " is not a TM3 TM, will not be leveraged");
                        continue;
                    }
                    LOGGER.info("Leveraging against "
                            + describeTm(projectTm, tm));
                    Tm3Leverager leverager = new Tm3Leverager(projectTm, tm,
                            pLDC.getSourceLocale(), leverageOptions, progress);
                    for (BaseTmTuv srcTuv : pLDC
                            .getOriginalSeparatedSegments(p_jobId))
                    {
                        if (srcTuv.isTranslatable())
                        {
                            leverager.leverageSegment(srcTuv, TM3Attributes
                                    .one(TM3Util.getAttr(tm, TRANSLATABLE),
                                            true));
                        }
                        else if (leverageOptions.isLeveragingLocalizables())
                        {
                            leverager.leverageSegment(srcTuv, TM3Attributes
                                    .one(TM3Util.getAttr(tm, TRANSLATABLE),
                                            false));
                        }
                    }
                }
            }

            // Now make sure that the results are all sorted in the right order
            return new LeverageMatchResults(sortResults(progress.values(),
                    leverageOptions));
        }
        catch (TM3Exception e)
        {
            throw new LingManagerException(e);
        }
    }

    @Override
    public LeverageMatches leverageSegment(BaseTmTuv pSourceTuv,
            LeverageOptions pLeverageOptions, List<Tm> pTms) throws Exception
    {
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("leverageSegment(" + pSourceTuv.getSegment() + ")");
        }

        try
        {
            // This is a trivial (one-pair) map for leverageSegment().
            Map<BaseTmTuv, LeverageMatches> progress = new HashMap<BaseTmTuv, LeverageMatches>(
                    1);

            // Leverage each TM separately and build a composite list.
            for (Tm projectTm : pTms)
            {
                TM3Tm<GSTuvData> tm = getTM3Tm(projectTm);
                if (tm == null)
                {
                    LOGGER.warn("TM " + projectTm.getId()
                            + " is not a TM3 TM, will not be leveraged");
                    continue;
                }
                Tm3Leverager leverager = new Tm3Leverager(projectTm, tm,
                        pSourceTuv.getLocale(), pLeverageOptions, progress);
                leverager.leverageSegment(pSourceTuv, TM3Attributes.one(
                        TM3Util.getAttr(tm, TRANSLATABLE), true));
            }
            LeverageMatches lm = progress.get(pSourceTuv);
            if (lm != null)
            {
                sortResults(Collections.singleton(lm), pLeverageOptions);
            }
            return lm;
        }
        catch (TM3Exception e)
        {
            LOGGER.warn("Error leveraging segment " + pSourceTuv, e);
            throw new LingManagerException(e);
        }
        catch (Exception e)
        {
            LOGGER.warn("Error leveraging segment " + pSourceTuv, e);
            throw e;
        }
    }

    /**
     * Save segments to TM3, and then generate the slightly odd mapping
     * structure that GlobalSight demands.
     * 
     * Currently, this routine will generate a separate event every time it is
     * called. This is bad behavior for TM import.
     */
    @Override
    public TuvMappingHolder saveToSegmentTm(
            Collection<? extends BaseTmTu> pSegmentsToSave,
            GlobalSightLocale pSourceLocale, Tm pTm,
            Set<GlobalSightLocale> pTargetLocales, int pMode,
            boolean pFromTmImport)
    {
    	int BATCH_SIZE = 1000;
        LOGGER.info("saveToSegmentTm: " + pSegmentsToSave.size()
                + " segs, srcLocale=" + pSourceLocale + ", pMode=" + pMode
                + ", fromTmImport=" + pFromTmImport);
        long start = System.currentTimeMillis();

        if (pSegmentsToSave.size() == 0)
        {
            return new TuvMappingHolder();
        }

        Connection connection = null;
        try
        {
            connection = DbUtil.getConnection();
            connection.setAutoCommit(false);

            TM3Tm<GSTuvData> tm = getTM3Tm(pTm);
            tm.setConnection(connection);

            UniqueSegmentRepositoryForCorpus usr = getUniqueRepository(
                    pSourceLocale, pSegmentsToSave);
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(pSegmentsToSave.size()
						+ " TUs from TMX are merged into " + usr.getAllTus().size());
			}

            // Use the first TU's filename/username info for the even, on the
            // assumption that they are all the same.
            BaseTmTu firstTu = pSegmentsToSave.iterator().next();
            BaseTmTuv firstTuv = firstTu.getFirstTuv(pSourceLocale);
            EventProducer sourceEvents = null;
            EventProducer targetEvents = null;
            if (pFromTmImport)
            {
                sourceEvents = new MultiUserEventProducer(tm,
                        EventType.TM_IMPORT, firstTu.getSourceTmName());
                targetEvents = sourceEvents;
            }
            else
            {
                // Completed translations being saved or from TM Search. Events
                // here are more complex because we have a source creator and
                // one or more target creators.
                TM3Event sourceTuvEvent = addSaveEvent(tm,
                        firstTuv.getCreationUser(),
                        firstTuv.getUpdatedProject());
                sourceEvents = new SingleEventProducer(sourceTuvEvent);
                targetEvents = new MultiUserEventProducer(tm, sourceTuvEvent);
            }

			// pTargetLocales is just the set of all locales for which there is
			// a target segment. We can pull that directly from the segments
			// themselves, so it's ignored.
            TM3Attribute translatableAttr = TM3Util.getAttr(tm, TRANSLATABLE);
            TM3Attribute typeAttr = TM3Util.getAttr(tm, TYPE);
            TM3Attribute formatAttr = TM3Util.getAttr(tm, FORMAT);
            TM3Attribute sidAttr = TM3Util.getAttr(tm, SID);
            TM3Attribute fromWsAttr = TM3Util.getAttr(tm, FROM_WORLDSERVER);
            TM3Attribute projectAttr = TM3Util.getAttr(tm, UPDATED_BY_PROJECT);

            TuvMappingHolder holder = new TuvMappingHolder();
            ProjectTM projectTM = (pTm instanceof ProjectTM) ? (ProjectTM) pTm
                    : null;
            Map<String, String> attValues = TMAttributeManager
                    .getTUAttributesForPopulator(projectTM, m_job);

            List<ArrayList<BaseTmTu>> batches = new ArrayList<ArrayList<BaseTmTu>>();
            ArrayList<BaseTmTu> batch = new ArrayList<BaseTmTu>();
            for (BaseTmTu base : usr.getAllTus())
            {
            	batch.add(base);
            	if (batch.size() == BATCH_SIZE)
            	{
            		batches.add(batch);
            		batch = new ArrayList<BaseTmTu>();
            	}
            }
            if (batch.size() > 0)
            {
            	batches.add(batch);
            }

            List<TM3Tu<GSTuvData>> savedTus = new ArrayList<TM3Tu<GSTuvData>>();
			boolean indexTarget = (projectTM == null ? false : projectTM
					.isIndexTarget());
            for (ArrayList<BaseTmTu> bat : batches)
            {
                TM3Saver<GSTuvData> saver = tm.createSaver();
                for (BaseTmTu base : bat)
                {
                    SegmentTmTu srcTu = (SegmentTmTu) base;

                    // NB SegmentTmTu's idea of source tuv is the first tuv with
                    // the source locale
                    BaseTmTuv srcTuv = srcTu.getSourceTuv();
					TM3Saver<GSTuvData>.Tu tuToSave = saver.tu(new GSTuvData(
							srcTuv), pSourceLocale, sourceEvents
							.getEventForTuv(srcTuv), srcTuv.getCreationUser(),
							srcTuv.getCreationDate(), srcTuv.getModifyUser(),
							srcTuv.getModifyDate(), srcTuv.getLastUsageDate(),
							srcTuv.getJobId(), srcTuv.getJobName(),
							srcTuv.getPreviousHash(), srcTuv.getNextHash(),
							srcTuv.getSid());
                    for (BaseTmTuv tuv : srcTu.getTuvs())
                    {
                        if (tuv.equals(srcTuv))
                        {
                            continue;
                        }
						tuToSave.target(
								new GSTuvData(tuv),
								tuv.getLocale(),
								targetEvents.getEventForTuv(tuv),
								decideTargetTuvCreationUser(srcTuv, tuv, pFromTmImport),
								tuv.getCreationDate(),
								tuv.getModifyUser(), tuv.getModifyDate(),
								tuv.getLastUsageDate(), tuv.getJobId(),
								tuv.getJobName(), tuv.getPreviousHash(),
								tuv.getNextHash(), tuv.getSid());
                    }

                    // handle TU properties
                    Collection<ProjectTmTuTProp> props = srcTu.getProps();
                    if (props != null)
                    {
                        for (ProjectTmTuTProp prop : props)
                        {
                            String vv = prop.getPropValue();

                            if (vv == null)
                            {
                                continue;
                            }

                            String name = TM3Util.getNameForTM3(prop);
                            TM3Attribute tm3a = null;

                            if (tm.doesAttributeExist(name))
                            {
                                tm3a = tm.getAttributeByName(name);
                            }
                            else
                            {
                                tm3a = TM3Util.toTM3Attribute(prop);
                                tm3a = TM3Util.saveTM3Attribute(tm3a, (BaseTm) tm);
                            }

                            tuToSave.attr(tm3a, vv);
                        }
                    }

                    if (attValues != null)
                    {
                        for (Map.Entry<String, String> attV : attValues.entrySet())
                        {
                            String vv = attV.getValue();

                            if (vv == null)
                            {
                                continue;
                            }

                            String name = attV.getKey();
                            TM3Attribute tm3a = null;

                            if (tm.doesAttributeExist(name))
                            {
                                tm3a = tm.getAttributeByName(name);
                            }
                            else
                            {
                                tm3a = new TM3Attribute(name,
                                        new TM3AttributeValueType.CustomType(),
                                        null, false);
                                tm3a = TM3Util.saveTM3Attribute(tm3a, (BaseTm) tm);
                            }

                            tuToSave.attr(tm3a, vv);
                        }
                    }

                    tuToSave.attr(translatableAttr, srcTu.isTranslatable());
                    tuToSave.attr(fromWsAttr, srcTu.isFromWorldServer());
                    if (srcTu.getType() != null)
                    {
                        tuToSave.attr(typeAttr, srcTu.getType());
                    }
                    if (srcTu.getFormat() != null)
                    {
                        tuToSave.attr(formatAttr, srcTu.getFormat());
                    }
                    if (srcTuv.getSid() != null)
                    {
                        tuToSave.attr(sidAttr, srcTuv.getSid());
                    }
                    else if (srcTu.getSID() != null)
                    {
                        tuToSave.attr(sidAttr, srcTu.getSID());
                    }

                    if (srcTuv.getUpdatedProject() != null)
                    {
                        tuToSave.attr(projectAttr, srcTuv.getUpdatedProject());
                    }
                }
				savedTus.addAll(saver.save(convertSaveMode(pMode), indexTarget));
                synchronized (this)
                {
                    connection.commit();
                }
            }

            // build the Lucene index.
            // XXX if tm.save overwrite some old data, we should re-index it
            luceneIndexTus(pTm.getId(), savedTus, indexTarget);
            if (tusRemove.size() != 0)
            {
                luceneRemoveTM3Tus(pTm.getId(), tusRemove, null);
                tusRemove.clear();
            }

            batches = null;
            savedTus = null;
            return holder;
        }
        catch (Exception e)
        {
            try
            {
                connection.rollback();
            }
            catch (Exception e2)
            {
            }
            throw new LingManagerException(e);
        }
        finally
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("tm3save: " + pSegmentsToSave.size() + " took "
                        + (System.currentTimeMillis() - start) + "ms");
            }
            DbUtil.silentReturnConnection(connection);
        }
    }

    /**
     * When populate TM, the target TUV's creationUser is just the job creator,
     * not the real creation user. Let's take the modify user as creation user
     * if source TUV and target TUV have the same creation user(GBS-1804).
     * 
     * Note that this implementation is not accurate, but mostly.
     */
    private String decideTargetTuvCreationUser(BaseTmTuv srcTuv,
            BaseTmTuv trgTuv, boolean isFromTmImport)
    {
        String trgTuvCreationUser = trgTuv.getCreationUser();
        if (!isFromTmImport && trgTuvCreationUser != null
                && trgTuvCreationUser.equals(srcTuv.getCreationUser())
                && trgTuv.getModifyUser() != null)
        {
            trgTuvCreationUser = trgTuv.getModifyUser();
        }

        return trgTuvCreationUser;
    }

    @Override
    public List<TMidTUid> tmConcordanceQuery(List<Tm> tms, String query,
            GlobalSightLocale sourceLocale, GlobalSightLocale targetLocale,
            Connection conn) throws LingManagerException
    {
        try
        {
            List<Long> tmIds = new ArrayList<Long>();
            for (Tm tm : tms)
            {
                tmIds.add(tm.getId());
            }
            return LuceneSearcher.search(tmIds, query, sourceLocale,
                    targetLocale);
        }
        catch (Exception e)
        {
            throw new LingManagerException(e);
        }
    }

    @Override
    public String getCreatingUserByTuvId(Tm tm, long tuvId)
    {
        try
        {
            TM3Tm<GSTuvData> tm3tm = getTM3Tm(tm);
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("getCreatingUserByTuvId(" + tuvId + "): "
                        + describeTm(tm, tm3tm));                
            }
            TM3Tuv<GSTuvData> tuv = tm3tm.getTuv(tuvId);
            if (tuv != null)
            {
                return tuv.getCreationUser();
            }

            return null;
        }
        catch (TM3Exception e)
        {
            throw new LingManagerException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<GlobalSightLocale> getLocalesForTm(Tm tm)
            throws LingManagerException
    {
        try
        {
            TM3Tm<GSTuvData> tm3tm = getTM3Tm(tm);
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("getLocalesForTm: " + describeTm(tm, tm3tm));                
            }
            Set s = tm3tm.getTuvLocales();
            return (Set<GlobalSightLocale>) s;
        }
        catch (TM3Exception e)
        {
            throw new LingManagerException(e);
        }
    }

    @Override
    public Date getModifyDateByTuvId(Tm tm, long tuvId)
    {
        try
        {
            TM3Tm<GSTuvData> tm3tm = getTM3Tm(tm);
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("getModifyDateByTuvId(" + tuvId + "): "
                        + describeTm(tm, tm3tm));                
            }
            TM3Tuv<GSTuvData> tuv = tm3tm.getTuv(tuvId);
            if (tuv != null)
            {
                return tuv.getModifyDate();
            }

            return null;
        }
        catch (TM3Exception e)
        {
            throw new LingManagerException(e);
        }
    }

    /**
     * TM3 reindexing doesn't currently do anything.
     */
    @Override
    public boolean reindexTm(Tm ptm, Reindexer reindexer, boolean indexTarget)
    {
        long ptmId = ptm.getId();
        boolean isFirst = true;
        TM3Tm<GSTuvData> tm3Tm = getTM3Tm(ptm);
        Tm3SegmentResultSet segments = (Tm3SegmentResultSet) getAllSegments(ptm,
                null, null, null);

        // index per 1000 TUs
        int count = 0;
        int max = 200;
        Collection<TM3Tu<GSTuvData>> tus = new ArrayList<TM3Tu<GSTuvData>>(max);
        while (segments.hasNext())
        {
            if (count == max)
            {
                try
                {
                    luceneIndexTus(ptmId, tus, indexTarget, isFirst);
                    isFirst = false;

                    // Recreate fuzzy index data in "tm3_index_shared_xx_xx" table.
                    // Source index is always created, so only for target here.
                    recreateFuzzyIndex(tm3Tm, tus, indexTarget);

                    LOGGER.info("Finished to reindex TUs : " + max);
                    reindexer.incrementCounter(max);
                }
                catch (Exception e)
                {
                    LOGGER.error(
                            "Fail to create lucene index for " + tus.size()
                                    + "tus in TM " + ptmId, e);
                }
                count = 0;
                tus.clear();
            }

            TM3Tu<GSTuvData> tu = segments.nextTm3tu();
            tus.add(tu);
            count++;
        }

        if (tus.size() > 0)
        {
            try
            {
                luceneIndexTus(ptmId, tus, indexTarget, isFirst);
                isFirst = false;
                recreateFuzzyIndex(tm3Tm, tus, indexTarget);

                LOGGER.info("Finished to reindex TUs : " + tus.size());
                reindexer.incrementCounter(tus.size());
            }
            catch (Exception e)
            {
                LOGGER.error("Fail to create lucene index for " + tus.size()
                        + "tus in TM " + ptmId, e);
            }
        }

        return true;
    }

    private void recreateFuzzyIndex(TM3Tm<GSTuvData> tm3Tm,
            Collection<TM3Tu<GSTuvData>> tus, boolean indexTarget)
    {
        Map<TM3Locale, List<TM3Tuv<GSTuvData>>> sourceTuvs = getLocaleSourceTuvMap(tus);
        for (Map.Entry<TM3Locale, List<TM3Tuv<GSTuvData>>> e : sourceTuvs
                .entrySet())
        {
            List<TM3Tuv<GSTuvData>> srcTuvs = e.getValue();
            tm3Tm.recreateFuzzyIndex(srcTuvs);
        }

        if (indexTarget)
        {
            Map<TM3Locale, List<TM3Tuv<GSTuvData>>> targetTuvs = getLocaleTargetTuvMap(tus);
            for (Map.Entry<TM3Locale, List<TM3Tuv<GSTuvData>>> e : targetTuvs
                    .entrySet())
            {
                List<TM3Tuv<GSTuvData>> trgTuvs = e.getValue();
                tm3Tm.recreateFuzzyIndex(trgTuvs);
            }
        }
    }

    private TM3SaveMode convertSaveMode(int mode)
    {
        switch (mode)
        {
            case TmCoreManager.SYNC_DISCARD:
                return TM3SaveMode.DISCARD;
            case TmCoreManager.SYNC_MERGE:
                return TM3SaveMode.MERGE;
            case TmCoreManager.SYNC_OVERWRITE:
                return TM3SaveMode.OVERWRITE;
        }
        return TM3SaveMode.MERGE;
    }

    // This code comes from the old implementation -- it is designed to
    // construct
    // the gnarly mapping of source data to TM segments.
    private UniqueSegmentRepositoryForCorpus getUniqueRepository(
            GlobalSightLocale pSourceLocale,
            Collection<? extends BaseTmTu> pSegmentsToSave)
            throws LingManagerException
    {
        try
        {
            UniqueSegmentRepositoryForCorpus jobDataToSave = new UniqueSegmentRepositoryForCorpus(
                    pSourceLocale);
            for (BaseTmTu tu : pSegmentsToSave)
            {
                // separate subflows out of the main text and save them as
                // independent segments
                Collection<SegmentTmTu> segmentTus = TmUtil.createSegmentTmTus(
                        tu, pSourceLocale);
                jobDataToSave.addTus(segmentTus);
            }
            return jobDataToSave;
        }
        catch (Exception e)
        {
            throw new LingManagerException(e);
        }
    }

    /**
     * Sort the TU contents of each LeverageMatches according to the TM
     * priority.
     * 
     * @return the collection it was passed
     */
    private Collection<LeverageMatches> sortResults(
            Collection<LeverageMatches> lms, LeverageOptions options)
    {
        Comparator<LeveragedTu> cmp = new ComparatorByTmOrder(options);
        for (LeverageMatches lm : lms)
        {
            List<LeveragedTu> tus = lm.getLeveragedTus();
            SortUtil.sort(tus, cmp);
            lm.setLeveragedTus(tus);
        }
        return lms;
    }

    private String describeTm(Tm tm, TM3Tm<GSTuvData> tm3tm)
    {
        return "TM " + tm.getId() + " (tm3tm " + tm3tm.getId() + ")";
    }

    private Date parseDate(String s) throws LingManagerException
    {
        return StringUtil.isEmpty(s) ? null : new Date(s);
    }

    /**
     * Add a TM Import event to this TM's history.
     * 
     * @param tm
     *            TM3Tm
     * @param userName
     *            Name of the user performing the import
     * @param importFileName
     *            Name of the TMX file that is being imported
     * @return persisted TM3Event
     */
    TM3Event addImportEvent(TM3Tm<GSTuvData> tm, String userName,
            String importFileName)
    {
        return tm.addEvent(EventType.TM_IMPORT.getValue(), userName,
                importFileName);
    }

    /**
     * Add a TM Import event to this TM's history.
     * 
     * @param tm
     *            TM3Tm
     * @param userName
     *            Name of the user performing the import
     * @param projectName
     *            Name of the project that this job is part of
     * @return persisted TM3Event
     */
    TM3Event addSaveEvent(TM3Tm<GSTuvData> tm, String userName,
            String projectName)
    {
        return tm.addEvent(EventType.SEGMENT_SAVE.getValue(), userName,
                projectName);
    }

    // Whether index target is determined by project TM option "index target"
    // default.
    public void luceneIndexTus(long ptmId, Collection<TM3Tu<GSTuvData>> tus)
            throws Exception
    {
        boolean indexTarget = false;
        try
        {
            indexTarget = ServerProxy.getProjectHandler()
                    .getProjectTMById(ptmId, false).isIndexTarget();
        }
        catch (Exception e)
        {
        }

        luceneIndexTus(ptmId, tus, indexTarget);
    }

    public <T> void luceneIndexTus(long ptmId, Collection<TM3Tu<GSTuvData>> tus,
            boolean p_indexTarget) throws Exception
    {
        luceneIndexTus(ptmId, tus, p_indexTarget, false);
    }
    
    public <T> void luceneIndexTus(long ptmId, Collection<TM3Tu<GSTuvData>> tus,
            boolean p_indexTarget, boolean p_isFirst) throws Exception
    {
        boolean isFirst = p_isFirst;
        
        Map<TM3Locale, List<TM3Tuv<GSTuvData>>> sourceTuvs = getLocaleSourceTuvMap(tus);
        for (Map.Entry<TM3Locale, List<TM3Tuv<GSTuvData>>> e : sourceTuvs
                .entrySet())
        {
            GlobalSightLocale gsl = (GlobalSightLocale) e.getKey();
            LuceneIndexWriter indexWriter = new LuceneIndexWriter(ptmId, gsl, isFirst);
            isFirst = false;
            try
            {
                indexWriter.index(e.getValue(), true, true);
            }
            finally
            {
                indexWriter.close();
            }
        }
        if (p_indexTarget)
        {
            Map<TM3Locale, List<TM3Tuv<GSTuvData>>> targetTuvs = getLocaleTargetTuvMap(tus);
            for (Map.Entry<TM3Locale, List<TM3Tuv<GSTuvData>>> e : targetTuvs
                    .entrySet())
            {
                LuceneIndexWriter indexWriter = new LuceneIndexWriter(ptmId,
                        (GlobalSightLocale) e.getKey());
                try
                {
                    indexWriter.index(e.getValue(), false, true);
                }
                finally
                {
                    indexWriter.close();
                }
            }
        }
    }

    private Map<TM3Locale, List<TM3Tuv<GSTuvData>>> getLocaleSourceTuvMap(
            Collection<TM3Tu<GSTuvData>> tus)
    {
        Map<TM3Locale, List<TM3Tuv<GSTuvData>>> sourceTuvs = new HashMap<TM3Locale, List<TM3Tuv<GSTuvData>>>();
        for (TM3Tu<GSTuvData> tu : tus)
        {
            TM3Tuv<GSTuvData> srcTuv = tu.getSourceTuv();
            if (!sourceTuvs.containsKey(srcTuv.getLocale()))
            {
                sourceTuvs.put(srcTuv.getLocale(),
                        new ArrayList<TM3Tuv<GSTuvData>>());
            }
            sourceTuvs.get(srcTuv.getLocale()).add(srcTuv);
        }

        return sourceTuvs;
    }

    private Map<TM3Locale, List<TM3Tuv<GSTuvData>>> getLocaleTargetTuvMap(
            Collection<TM3Tu<GSTuvData>> tus)
    {
        Map<TM3Locale, List<TM3Tuv<GSTuvData>>> targetTuvs = new HashMap<TM3Locale, List<TM3Tuv<GSTuvData>>>();
        for (TM3Tu<GSTuvData> tu : tus)
        {
            for (TM3Tuv<GSTuvData> tuv : (List<TM3Tuv<GSTuvData>>) tu
                    .getTargetTuvs())
            {
                if (!targetTuvs.containsKey(tuv.getLocale()))
                {
                    targetTuvs.put(tuv.getLocale(),
                            new ArrayList<TM3Tuv<GSTuvData>>());
                }
                targetTuvs.get(tuv.getLocale()).add(tuv);
            }
        }
        return targetTuvs;
    }

    /**
     * Remove lucene index data from lucene files on hard disk, system will
     * recreate lucene index data later.
     * 
     * @param tmId
     * @param tus
     * @throws Exception
     */
    private void luceneRemoveTM3Tus(long tmId,
            Collection<TM3Tu<GSTuvData>> tus, GlobalSightLocale specifiedLocale)
            throws Exception
    {
        Map<TM3Locale, List<TM3Tuv<GSTuvData>>> tuvs = new HashMap<TM3Locale, List<TM3Tuv<GSTuvData>>>();
        for (TM3Tu<GSTuvData> tu : tus)
        {
            for (TM3Tuv<GSTuvData> tuv : tu.getAllTuv())
            {
                if (!tuvs.containsKey(tuv.getLocale()))
                {
                    tuvs.put(tuv.getLocale(),
                            new ArrayList<TM3Tuv<GSTuvData>>());
                }
                tuvs.get(tuv.getLocale()).add(tuv);
            }
        }
        for (Map.Entry<TM3Locale, List<TM3Tuv<GSTuvData>>> e : tuvs.entrySet())
        {
            GlobalSightLocale currentLocale = (GlobalSightLocale) e.getKey();
            if (specifiedLocale == null
                    || specifiedLocale.getId() == currentLocale.getId())
            {
                luceneRemoveByLocale(tmId, currentLocale, e.getValue());
            }
        }
    }

    private void luceneRemoveBaseTus(long tmId,
            Collection<? extends BaseTmTu> tus) throws Exception
    {
        Map<GlobalSightLocale, List<BaseTmTuv>> tuvs = new HashMap<GlobalSightLocale, List<BaseTmTuv>>();
        for (BaseTmTu tu : tus)
        {
            for (BaseTmTuv tuv : tu.getTuvs())
            {
                if (!tuvs.containsKey(tuv.getLocale()))
                {
                    tuvs.put(tuv.getLocale(), new ArrayList<BaseTmTuv>());
                }
                tuvs.get(tuv.getLocale()).add(tuv);
            }
        }
        for (Map.Entry<GlobalSightLocale, List<BaseTmTuv>> e : tuvs.entrySet())
        {
            luceneRemoveByLocale(tmId, (GlobalSightLocale) e.getKey(),
                    e.getValue());
        }
    }

    private void luceneRemoveByLocale(long tmId, GlobalSightLocale locale,
            Collection tuvs) throws Exception
    {
        // Check if the lucene index file exists on HD.If not, do not need
        // execute the removing at all.
        File indexDir = LuceneUtil.getGoldTmIndexDirectory(tmId, locale, false);
        if (indexDir != null)
        {
            LuceneIndexWriter indexWriter = new LuceneIndexWriter(tmId, locale);
            try
            {
                indexWriter.remove(tuvs);
            }
            finally
            {
                indexWriter.close();
            }
        }
    }

    interface EventProducer
    {
        TM3Event getEventForTuv(BaseTmTuv tuv);
    }

    class SingleEventProducer implements EventProducer
    {
        private TM3Event event;

        SingleEventProducer(TM3Event event)
        {
            this.event = event;
        }

        @Override
        public TM3Event getEventForTuv(BaseTmTuv tuv)
        {
            return event;
        }
    }

    class MultiUserEventProducer implements EventProducer
    {
        private Map<String, TM3Event> map = new HashMap<String, TM3Event>();
        private TM3Tm<GSTuvData> tm;
        private int eventType;
        private String attr;

        MultiUserEventProducer(TM3Tm<GSTuvData> tm, EventType eventType,
                String attr)
        {
            this.tm = tm;
            this.eventType = eventType.getValue();
            this.attr = attr;
        }

        // For segment save events only
        MultiUserEventProducer(TM3Tm<GSTuvData> tm, TM3Event sourceEvent)
        {
            this.tm = tm;
            this.attr = sourceEvent.getArgument();
            this.eventType = EventType.SEGMENT_SAVE.getValue();
            map.put(sourceEvent.getUsername(), sourceEvent);
        }

        @Override
        public TM3Event getEventForTuv(BaseTmTuv tuv)
        {

            String user = tuv.getModifyUser();
            if (user == null)
            {
                user = tuv.getCreationUser();
            }
            if (map.containsKey(user))
            {
                return map.get(user);
            }
            TM3Event event = TM3ImportHelper.createTM3Event(tm, user, eventType, attr, new Date());
            map.put(user, event);

            return event;
        }
    }

    @Override
    public TuvBasicInfo getTuvBasicInfoByTuvId(Tm tm, long tuvId)
    {
        try
        {
            TM3Tm<GSTuvData> tm3tm = getTM3Tm(tm);
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("getModifyDateByTuvId(" + tuvId + "): "
                        + describeTm(tm, tm3tm));                
            }
            TM3Tuv<GSTuvData> tuv = tm3tm.getTuv(tuvId);

            TuvBasicInfo tuvBasicInfo = null;
            if (tuv != null)
            {
                String sid = (String) tuv.getTu().getAttribute(
                        TM3Util.getAttr(tm3tm, SID));
                String exactMatchedKey = String.valueOf(tuv.getFingerprint());
                String project = (String) tuv.getTu().getAttribute(
                        TM3Util.getAttr(tm3tm, UPDATED_BY_PROJECT));
                tuvBasicInfo = new TuvBasicInfo(tuv.getContent().getData(),
                        null, exactMatchedKey, tuv.getContent().getLocale(),
                        tuv.getCreationDate(), tuv.getCreationUser(),
                        tuv.getModifyDate(), tuv.getModifyUser(), project, sid);
            }

            return tuvBasicInfo;
        }
        catch (TM3Exception e)
        {
            throw new LingManagerException(e);
        }
    }

    private Job m_job;

    @Override
    public void setJob(Job job)
    {
        m_job = job;
    }

    @Override
    public Job getJob()
    {
        return m_job;
    }

    @Override
    public int getAllSegmentsCount(Tm tm, long startTUId)
            throws LingManagerException
    {
        return getAllSegmentsCount(tm, null, null);
    }

    private boolean isInSameCompany(List<Tm> projectTms)
    {
        boolean result = true;

        long companyId = -1;
        for (Tm pTm : projectTms)
        {
            if (companyId == -1)
            {
                companyId = pTm.getCompanyId();
            }
            else if (companyId != pTm.getCompanyId())
            {
                result = false;
            }
        }

        return result;
    }
    
    private String getLocaleCodeStr(List<String> localeCodeList)
	{
		StringBuffer buffer = new StringBuffer();
		for (String localeCode : localeCodeList)
		{
			buffer.append(localeCode).append(",");
		}
		String str = null;
		if (buffer.toString().endsWith(","))
		{
			str = buffer.toString().substring(0,
					buffer.toString().lastIndexOf(","));
		}
		return str;
	}

	private List getLocaleList(List<String> localeCodeList)
	{
		List localeList = new ArrayList();
		GlobalSightLocale locale = null;
		for (int i = 0; i < localeCodeList.size(); i++)
		{
			locale = GSDataFactory.localeFromCode(localeCodeList.get(i));
			if (locale != null)
			{
				localeList.add(locale);
			}
		}
		return localeList;
	}
	
    @Override
    public void setLock(boolean lock)
    {
        this.lock = lock;
    }

    @Override
    public boolean getLock()
    {
        return this.lock;
    }
}
