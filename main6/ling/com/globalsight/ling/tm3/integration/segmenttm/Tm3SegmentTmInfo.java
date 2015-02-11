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

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.customAttribute.TMAttributeManager;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.projecthandler.ProjectTM;
import com.globalsight.everest.projecthandler.ProjectTmTuTProp;
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

    public static List<TM3Tu<GSTuvData>> tusRemove = new ArrayList<TM3Tu<GSTuvData>>();
    private TM3Manager manager = DefaultManager.create();
    private boolean indexTarget;
    {
        try
        {
            // Fix for GBS-2448
            // indexTarget =
            // SystemConfiguration.getInstance().getBooleanParameter("leverager.targetIndexing");
            indexTarget = true;

        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    protected TM3Tm<GSTuvData> getTM3Tm(Tm tm)
    {
        TM3Tm<GSTuvData> tm3tm = manager.getTm(new GSDataFactory(),
                tm.getTm3Id());
        if (tm3tm == null)
        {
            throw new IllegalArgumentException("Non-existent tm3 tm: "
                    + tm.getTm3Id());
        }
        tm3tm.setIndexTarget(indexTarget);
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
            LOGGER.info("deleteSegmentTmTus: " + describeTm(pTm, tm3tm)
                    + "ids " + ids);

            // Delete the segments by ID
            tm3tm.getDataById(ids).purge();

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
            if (LOGGER.isInfoEnabled())
            {
                LOGGER.info("deleteSegmentTmTuvs: " + describeTm(pTm, tm3tm)
                        + "tuIds " + map.keySet());
            }

            // Do all the deletions
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
            }
            // Now update the affected TUs in the DB.
            // XXX This requires an event, even though TM3 doesn't currently
            // record it for TUV deletion
            TM3Event event = tm3tm.addEvent(EventType.TUV_DELETE.getValue(),
                    "system", null);
            List<TM3Tu<GSTuvData>> newTus = new ArrayList<TM3Tu<GSTuvData>>();
            for (TM3Tu<GSTuvData> tu : map.values())
            {
                TM3Tu<GSTuvData> newTu = tm3tm.modifyTu(tu, event);
                newTus.add(newTu);
            }

            // update the Lucene index
            // target locale lists have changed, so deindex old tus, using the
            // BaseTmTus that haven't been mangled ...
            Set<BaseTmTu> tusForRemove = new HashSet<BaseTmTu>();
            for (BaseTmTuv tuv : pTuvs)
            {
                tusForRemove.add(tuv.getTu());
            }
            luceneRemoveBaseTus(pTm.getId(), tusForRemove);
            // ... and index new tus
            luceneIndexTus(pTm.getId(), newTus);
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
            if (LOGGER.isInfoEnabled())
            {
                LOGGER.info("deleteSegmentTmTuvs: " + describeTm(pTm, tm3tm)
                        + "tuIds " + map.keySet());
            }

            String modifyUser = null;
            TM3Tm<GSTuvData> tm = getTM3Tm(pTm);
            for (SegmentTmTuv tuv : pTuvs)
            {
                modifyUser = tuv.getModifyUser();
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
                tu.setAttribute(sidAttr, stu.getSID());
                // Now find the relevant TUV
                for (TM3Tuv<GSTuvData> tm3tuv : tu.getAllTuv())
                {
                    if (tm3tuv.getId().equals(tuv.getId()))
                    {
                        // Update the segment content
                        tm3tuv.setContent(factory.fromSerializedForm(
                                tuv.getLocale(), tuv.getSegment()));
                    }

                }
            }

            // TODO: would be nice to capture user name
            if (modifyUser == null)
            {
                modifyUser = "system";
            }
            TM3Event event = tm3tm.addEvent(EventType.TUV_MODIFY.getValue(),
                    modifyUser, null);
            List<TM3Tu<GSTuvData>> newTus = new ArrayList<TM3Tu<GSTuvData>>();
            for (TM3Tu<GSTuvData> tu : map.values())
            {
                TM3Tu<GSTuvData> newTu = tm3tm.modifyTu(tu, event);
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
    public SegmentResultSet getAllSegments(Tm tm, String createdBefore,
            String createdAfter) throws LingManagerException
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
    public SegmentResultSet getSegmentsByLocale(Tm tm, String localeCode,
            String createdBefore, String createdAfter)
            throws LingManagerException
    {
        try
        {
            TM3Tm<GSTuvData> tm3tm = getTM3Tm(tm);
            LOGGER.info("getSegmentsByLocale(" + localeCode + "): "
                    + describeTm(tm, tm3tm) + " createdBefore " + createdBefore
                    + " createdAfter " + createdAfter);
            GlobalSightLocale locale = GSDataFactory.localeFromCode(localeCode);
            return new Tm3SegmentResultSet(tm, tm3tm, tm3tm.getDataByLocale(
                    locale, parseDate(createdAfter), parseDate(createdBefore))
                    .iterator());
        }
        catch (TM3Exception e)
        {
            throw new LingManagerException(e);
        }
    }

    @Override
    public int getSegmentsCountByLocale(Tm tm, String localeCode,
            String createdBefore, String createdAfter)
            throws LingManagerException
    {
        try
        {
            TM3Tm<GSTuvData> tm3tm = getTM3Tm(tm);
            LOGGER.info("getSegmentsByLocale(" + localeCode + "): "
                    + describeTm(tm, tm3tm) + " createdBefore " + createdBefore
                    + " createdAfter " + createdAfter);
            GlobalSightLocale locale = GSDataFactory.localeFromCode(localeCode);
            return Long.valueOf(
                    tm3tm.getDataByLocale(locale, parseDate(createdAfter),
                            parseDate(createdBefore)).getCount()).intValue();
        }
        catch (TM3Exception e)
        {
            throw new LingManagerException(e);
        }
    }

    @Override
    public SegmentResultSet getSegmentsByProjectName(Tm tm, String projectName,
            String createdBefore, String createdAfter)
            throws LingManagerException
    {
        try
        {
            TM3Tm<GSTuvData> tm3tm = getTM3Tm(tm);
            LOGGER.info("getSegmentsByProjectName(" + projectName + "): "
                    + describeTm(tm, tm3tm) + " createdBefore " + createdBefore
                    + " createdAfter " + createdAfter);
            TM3Attribute projectAttr = TM3Util.getAttr(tm3tm,
                    UPDATED_BY_PROJECT);
            return new Tm3SegmentResultSet(tm, tm3tm, tm3tm
                    .getDataByAttributes(
                            TM3Attributes.one(projectAttr, projectName),
                            parseDate(createdAfter), parseDate(createdBefore))
                    .iterator());
        }
        catch (TM3Exception e)
        {
            throw new LingManagerException(e);
        }
    }

    @Override
    public int getSegmentsCountByProjectName(Tm tm, String projectName,
            String createdBefore, String createdAfter)
            throws LingManagerException
    {
        try
        {
            TM3Tm<GSTuvData> tm3tm = getTM3Tm(tm);
            LOGGER.debug("getSegmentsCountByProjectName(" + projectName + "): "
                    + describeTm(tm, tm3tm) + " createdBefore " + createdBefore
                    + " createdAfter " + createdAfter);
            TM3Attribute projectAttr = TM3Util.getAttr(tm3tm,
                    UPDATED_BY_PROJECT);
            return Long.valueOf(
                    tm3tm.getDataByAttributes(
                            TM3Attributes.one(projectAttr, projectName),
                            parseDate(createdAfter), parseDate(createdBefore))
                            .getCount()).intValue();
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
            LOGGER.debug("getSidByTuvId(" + tuvId + "): "
                    + describeTm(tm, tm3tm));
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
            LOGGER.debug("getSourceTextByTuvId(" + tuvId + ", locale="
                    + srcLocaleId + "): " + describeTm(tm, tm3tm));
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
            LOGGER.debug("getStatistics: " + describeTm(pTm, tm3tm)
                    + " includeProjects=" + pIncludeProjects);

            StatisticsInfo stats = new StatisticsInfo();
            int tuCount = (int) tm3tm.getAllData(null, null).getCount();
            // We could query this directly, but for now it's cheaper to
            // add it up from the locale data
            int tuvCount = 0;
            for (TM3Locale l : tm3tm.getTuvLocales())
            {
                GlobalSightLocale locale = (GlobalSightLocale) l;
                TM3Handle<GSTuvData> handle = tm3tm.getDataByLocale(l, null,
                        null);
                int localeTuCount = (int) handle.getCount();
                int localeTuvCount = (int) handle.getTuvCount();
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
                    TM3Handle<GSTuvData> handle = tm3tm.getDataByAttributes(
                            TM3Attributes.one(projectAttr, pv), null, null);
                    // stats.addUpdateProjectInfo((String) pv,
                    // (int) handle.getCount(), (int) handle.getTuvCount());
                    // To set tu and tuv count as 0 is because that in
                    // statistics
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
            TM3Handle<GSTuvData> handle = tm3tm.getDataByLocale(pLocale, null,
                    null);
            // this is bad for a big TM, should be batched
            List<TM3Tu<GSTuvData>> tus = new ArrayList<TM3Tu<GSTuvData>>();
            for (TM3Tu<GSTuvData> tu : handle)
            {
                tus.add(tu);
                // we're not going to sync back this TU object, just modifying
                // it for luceneIndexTus
                tu.removeTargetTuvByLocale(pLocale);
            }
            // pReporter.setPercentage(50);
            luceneRemoveTM3Tus(pTm.getId(), tus);
            luceneIndexTus(pTm.getId(), tus);
            // pReporter.setPercentage(90);
            tm3tm.removeDataByLocale(pLocale);

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
    public LeverageMatchResults leverage(List<Tm> pTms,
            LeverageDataCenter pLDC, String companyId) throws Exception
    {
        try
        {
            LeverageOptions leverageOptions = pLDC.getLeverageOptions();

            // The LeverageMatches objects are keyed by source segments. Rather
            // than
            // do each TM for each segment, do all the segments in a given TM in
            // a row
            // and keep track of the running results.
            Map<BaseTmTuv, LeverageMatches> progress = new HashMap<BaseTmTuv, LeverageMatches>();

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
                LOGGER.info("Leveraging against " + describeTm(projectTm, tm));
                Tm3Leverager leverager = new Tm3Leverager(projectTm, tm,
                        pLDC.getSourceLocale(), leverageOptions, progress);
                for (BaseTmTuv srcTuv : pLDC
                        .getOriginalSeparatedSegments(companyId))
                {
                    if (srcTuv.isTranslatable())
                    {
                        leverager.leverageSegment(srcTuv, TM3Attributes.one(
                                TM3Util.getAttr(tm, TRANSLATABLE), true));
                    }
                    else if (leverageOptions.isLeveragingLocalizables())
                    {
                        leverager.leverageSegment(srcTuv, TM3Attributes.one(
                                TM3Util.getAttr(tm, TRANSLATABLE), false));
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
        if (LOGGER.isInfoEnabled())
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
            tm.setFirstImporting(pTm.isFirstImporting());

            UniqueSegmentRepositoryForCorpus usr = getUniqueRepository(
                    pSourceLocale, pSegmentsToSave);

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
            // a
            // target segment. We can pull that directly from the segments
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

            TM3Saver<GSTuvData> saver = tm.createSaver();
            for (BaseTmTu base : usr.getAllTus())
            {
                SegmentTmTu srcTu = (SegmentTmTu) base;

                // NB SegmentTmTu's idea of source tuv is the first tuv with
                // the source locale
                BaseTmTuv srcTuv = srcTu.getSourceTuv();
                TM3Saver<GSTuvData>.Tu tuToSave = saver.tu(
                        new GSTuvData(srcTuv), pSourceLocale,
                        sourceEvents.getEventForTuv(srcTuv));
                for (BaseTmTuv tuv : srcTu.getTuvs())
                {
                    if (tuv.equals(srcTuv))
                    {
                        continue;
                    }
                    tuToSave.target(new GSTuvData(tuv), tuv.getLocale(),
                            targetEvents.getEventForTuv(tuv));
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
//                if (savedTus.size() > 0)
//                {
//                    TM3Tu<GSTuvData> saved = savedTus.get(0);
//                    for (BaseTmTuv stuv : usr.getIdenticalSegment(srcTuv))
//                    {
//                        // Note that this uses the PROJECT TM ID, not the tm3 tm
//                        // id.
//                        holder.addMapping(pSourceLocale, stuv.getId(), stuv
//                                .getTu().getId(), pTm.getId(), saved.getId(),
//                                saved.getSourceTuv().getId());
//                    }
//                    newTus.add(saved);
//                }
            }
            saver.setFromTmImport(pFromTmImport);
            List<TM3Tu<GSTuvData>> savedTus = saver
                    .save(convertSaveMode(pMode));

            // build the Lucene index.
            // XXX if tm.save overwrote some old data, we should re-index it
            luceneIndexTus(pTm.getId(), savedTus);
            if (tusRemove.size() != 0)
            {
                luceneRemoveTM3Tus(pTm.getId(), tusRemove);
                tusRemove.clear();
            }
            
            synchronized (this)
            {
                connection.commit();
            }

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
            LOGGER.debug("tm3save: " + pSegmentsToSave.size() + " took "
                    + (System.currentTimeMillis() - start) + "ms");
            DbUtil.silentReturnConnection(connection);
        }
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
            LOGGER.debug("getCreatingUserByTuvId(" + tuvId + "): "
                    + describeTm(tm, tm3tm));
            TM3Tuv<GSTuvData> tuv = tm3tm.getTuv(tuvId);
            if (tuv != null)
            {
                TM3Event event = tuv.getFirstEvent();
                return (event != null) ? event.getUsername() : null;
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
            LOGGER.debug("getLocalesForTm: " + describeTm(tm, tm3tm));
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
            LOGGER.debug("getModifyDateByTuvId(" + tuvId + "): "
                    + describeTm(tm, tm3tm));
            TM3Tuv<GSTuvData> tuv = tm3tm.getTuv(tuvId);
            if (tuv != null)
            {
                TM3Event event = tuv.getLatestEvent();
                return (event != null) ? event.getTimestamp() : null;
            }
            return null;
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

    /**
     * TM3 reindexing doesn't currently do anything.
     */
    @Override
    public boolean reindexTm(Tm tm, Reindexer reindexer)
    {
        reindexer.incrementCounter(getSegmentCountForReindex(tm));
        return true;
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
            Collections.sort(tus, cmp);
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

    // Lucene helpers
    // HACK: this one is public for Tm3Migrator
    public void luceneIndexTus(long tmId, Collection<TM3Tu<GSTuvData>> tus)
            throws Exception
    {
        Map<TM3Locale, List<TM3Tuv<GSTuvData>>> sourceTuvs = new HashMap<TM3Locale, List<TM3Tuv<GSTuvData>>>(), targetTuvs = new HashMap<TM3Locale, List<TM3Tuv<GSTuvData>>>();
        for (TM3Tu<GSTuvData> tu : tus)
        {
            TM3Tuv<GSTuvData> srcTuv = tu.getSourceTuv();
            if (!sourceTuvs.containsKey(srcTuv.getLocale()))
            {
                sourceTuvs.put(srcTuv.getLocale(),
                        new ArrayList<TM3Tuv<GSTuvData>>());
            }
            sourceTuvs.get(srcTuv.getLocale()).add(srcTuv);
            if (indexTarget)
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
        }
        for (Map.Entry<TM3Locale, List<TM3Tuv<GSTuvData>>> e : sourceTuvs
                .entrySet())
        {
            LuceneIndexWriter indexWriter = new LuceneIndexWriter(tmId,
                    (GlobalSightLocale) e.getKey());
            try
            {
                indexWriter.index(e.getValue(), true, true);
            }
            finally
            {
                indexWriter.close();
            }
        }
        for (Map.Entry<TM3Locale, List<TM3Tuv<GSTuvData>>> e : targetTuvs
                .entrySet())
        {
            LuceneIndexWriter indexWriter = new LuceneIndexWriter(tmId,
                    (GlobalSightLocale) e.getKey());
            try
            {
                if (indexTarget)
                {
                    indexWriter.index(e.getValue(), false, true);
                }
            }
            finally
            {
                indexWriter.close();
            }
        }
    }

    private void luceneRemoveTM3Tus(long tmId, Collection<TM3Tu<GSTuvData>> tus)
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
            LuceneIndexWriter indexWriter = new LuceneIndexWriter(tmId,
                    (GlobalSightLocale) e.getKey());
            try
            {
                indexWriter.remove(e.getValue());
            }
            finally
            {
                indexWriter.close();
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
            LuceneIndexWriter indexWriter = new LuceneIndexWriter(tmId,
                    e.getKey());
            try
            {
                indexWriter.remove(e.getValue());
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
            TM3Event event = TM3ImportHelper.createTM3Event(tm, user, eventType, attr);
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
            LOGGER.debug("getModifyDateByTuvId(" + tuvId + "): "
                    + describeTm(tm, tm3tm));
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
                        tuv.getFirstEvent().getTimestamp(), tuv.getFirstEvent()
                                .getUsername(), tuv.getLatestEvent()
                                .getTimestamp(), tuv.getLatestEvent()
                                .getUsername(), project, sid);
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
    public SegmentResultSet getAllSegments(Tm tm, long startTUId)
            throws LingManagerException
    {
        return getAllSegments(tm, null, null);
    }

    @Override
    public int getAllSegmentsCount(Tm tm, long startTUId)
            throws LingManagerException
    {
        return getAllSegmentsCount(tm, null, null);
    }
}
