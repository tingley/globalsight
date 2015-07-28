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
package com.globalsight.ling.tm3.integration;

import static com.globalsight.ling.tm3.integration.segmenttm.SegmentTmAttribute.FORMAT;
import static com.globalsight.ling.tm3.integration.segmenttm.SegmentTmAttribute.FROM_WORLDSERVER;
import static com.globalsight.ling.tm3.integration.segmenttm.SegmentTmAttribute.SID;
import static com.globalsight.ling.tm3.integration.segmenttm.SegmentTmAttribute.TRANSLATABLE;
import static com.globalsight.ling.tm3.integration.segmenttm.SegmentTmAttribute.TYPE;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.customAttribute.TMAttribute;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.projecthandler.ProjectTM;
import com.globalsight.everest.projecthandler.ProjectTMTBUsers;
import com.globalsight.everest.projecthandler.ProjectTmTuTProp;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.tm.management.Tm3ConvertProcess;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.ling.tm2.SegmentResultSet;
import com.globalsight.ling.tm2.SegmentTmTu;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.ling.tm3.core.BaseTm;
import com.globalsight.ling.tm3.core.DefaultManager;
import com.globalsight.ling.tm3.core.TM3Attribute;
import com.globalsight.ling.tm3.core.TM3Event;
import com.globalsight.ling.tm3.core.TM3Exception;
import com.globalsight.ling.tm3.core.TM3Manager;
import com.globalsight.ling.tm3.core.TM3SaveMode;
import com.globalsight.ling.tm3.core.TM3Saver;
import com.globalsight.ling.tm3.core.TM3Tm;
import com.globalsight.ling.tm3.core.TM3Tu;
import com.globalsight.ling.tm3.integration.segmenttm.EventType;
import com.globalsight.ling.tm3.integration.segmenttm.SegmentTmAttribute;
import com.globalsight.ling.tm3.integration.segmenttm.TM3Util;
import com.globalsight.ling.tm3.integration.segmenttm.Tm3SegmentTmInfo;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.progress.ProgressReporter;

public class Tm3Migrator
{
    private Logger logger = Logger.getLogger(Tm3Migrator.class);

    private long companyId;
    private ProjectTM oldTm;
    private ProjectTM convertingTm;
    private TM3Tm<GSTuvData> tm3tm;
    private boolean userInterrupt = false;
    private Tm3ConvertProcess convertProcess = Tm3ConvertProcess.getInstance();

    public Tm3Migrator(long companyId, ProjectTM oldTm)
    {
        this.companyId = companyId;
        this.oldTm = oldTm;
    }

    /**
     * Migrate the TM. Because big TMs may take a long time to migrate, this
     * code will use multiple transactions to write out the new TM data.
     * 
     * @param progress
     *            ProgressReporter for status updates
     * @return migrated TM, or NULL if the operation failed
     */
    public ProjectTM migrate(ProgressReporter progress)
    {
        Connection conn = null;
        try
        {
            // Now make sure we can find a name for the new TM
            String newTmName = getUniqueTmName(oldTm.getName());
            if (newTmName == null)
            {
                progress.setMessageKey("lb_tm_migrate_tm3_failed",
                        "Failed to migrate TM");
                return null;
            }

            conn = DbUtil.getConnection();
            TM3Manager manager = DefaultManager.create();
            SegmentResultSet segments = oldTm.getSegmentTmInfo()
                    .getAllSegments(oldTm, null, null, conn);

            // XXX It would be nice to be able to ensure that the shared storage
            // for this company actually existed first.
            tm3tm = manager.createMultilingualSharedTm(new GSDataFactory(),
                    SegmentTmAttribute.inlineAttributes(), companyId);

            // Create an event for this import
            // TODO: factor out segment save from TM3SegmentTmInfo
            EventMap events = new EventMap(tm3tm);
            TM3Attribute typeAttr = TM3Util.getAttr(tm3tm, TYPE);
            TM3Attribute formatAttr = TM3Util.getAttr(tm3tm, FORMAT);
            TM3Attribute sidAttr = TM3Util.getAttr(tm3tm, SID);
            TM3Attribute translatableAttr = TM3Util
                    .getAttr(tm3tm, TRANSLATABLE);
            TM3Attribute fromWsAttr = TM3Util.getAttr(tm3tm, FROM_WORLDSERVER);

            // In order to calculate completion percentage, we need to see how
            // big the old TM was
            long totalCount = oldTm.getSegmentTmInfo().getAllSegmentsCount(
                    oldTm, null, null);

            progress.setMessageKey("lb_tm_migrate_tm3_converting",
                    "Got tm, migrating to tm3 id " + tm3tm.getId());
            long oldCount = 0, newCount = 0;
            TM3Saver<GSTuvData> saver = tm3tm.createSaver();

            // Create the project TM that points to the this TM. Do it now so
            // we have the id for the lucene
            ProjectTM tm = new ProjectTM();
            tm.setName(newTmName);
            tm.setDomain(oldTm.getDomain());
            tm.setOrganization(oldTm.getOrganization());
            tm.setDescription(oldTm.getDescription());
            tm.setCreationUser(oldTm.getCreationUser());
            tm.setCreationDate(oldTm.getCreationDate());
            tm.setCompanyId(oldTm.getCompanyId());
            tm.setTm3Id(tm3tm.getId());
            tm.setIsRemoteTm(false);
            tm.setIndexTarget(oldTm.isIndexTarget());
            // add attributes from old tm
            List<TMAttribute> oldAtts = oldTm.getAllTMAttributes();
            Set<TMAttribute> attSet = new HashSet<TMAttribute>();
            if (oldAtts != null)
            {
                for (TMAttribute oldAtt : oldAtts)
                {
                    TMAttribute tmatt = new TMAttribute();
                    tmatt.setAttributename(oldAtt.getAttributename());
                    tmatt.setSettype(oldAtt.getSettype());
                    tmatt.setTm(tm);

                    attSet.add(tmatt);
                }
            }
            tm.setAttributes(attSet);

            HibernateUtil.save(tm);

            progress.setMessageKey("", "Created Project TM " + newTmName);

            // the lucene index uses this
            CompanyThreadLocal.getInstance().setIdValue(
                    Long.toString(companyId));

            while (true)
            {
                synchronized (this)
                {
                    if (userInterrupt || !segments.hasNext())
                    {
                        break;
                    }
                    SegmentTmTu oldTu = segments.next();
                    oldCount++;
                    if (oldTu == null)
                    {
                        continue;
                    }
                    BaseTmTuv oldSrcTuv = oldTu.getSourceTuv();
                    TM3Saver<GSTuvData>.Tu tu = saver
                            .tu(new GSTuvData(oldSrcTuv),
                                    oldTu.getSourceLocale(),
                                    events.get(oldSrcTuv),
                                    oldSrcTuv.getCreationUser(),
                                    oldSrcTuv.getCreationDate(),
                                    oldSrcTuv.getModifyUser(),
                                    oldSrcTuv.getModifyDate(),
                                    oldSrcTuv.getLastUsageDate(),
                                    oldSrcTuv.getJobId(),
                                    oldSrcTuv.getJobName(),
                                    oldSrcTuv.getPreviousHash(),
                                    oldSrcTuv.getNextHash(),
                                    oldSrcTuv.getSid());
                    tu.attr(fromWsAttr, oldTu.isFromWorldServer());
                    tu.attr(translatableAttr, oldTu.isTranslatable());
                    if (oldTu.getType() != null)
                    {
                        tu.attr(typeAttr, oldTu.getType());
                    }
                    if (oldTu.getFormat() != null)
                    {
                        tu.attr(formatAttr, oldTu.getFormat());
                    }
                    if (oldSrcTuv.getSid() != null)
                    {
                        tu.attr(sidAttr, oldSrcTuv.getSid());
                    }
                    for (BaseTmTuv tuv : oldTu.getTuvs())
                    {
                        if (tuv.equals(oldSrcTuv))
                        {
                            continue;
                        }
						tu.target(new GSTuvData(tuv), tuv.getLocale(),
								events.get(tuv), tuv.getCreationUser(),
								tuv.getCreationDate(), tuv.getModifyUser(),
								tuv.getModifyDate(), tuv.getLastUsageDate(),
								tuv.getJobId(), tuv.getJobName(),
								tuv.getPreviousHash(), tuv.getNextHash(),
								tuv.getSid());
                    }

                    // handle TU properties
                    Collection<ProjectTmTuTProp> props = oldTu.getProps();
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

                            if (tm3tm.doesAttributeExist(name))
                            {
                                tm3a = tm3tm.getAttributeByName(name);
                            }
                            else
                            {
                                tm3a = TM3Util.toTM3Attribute(prop);
                                tm3a = TM3Util.saveTM3Attribute(tm3a,
                                        (BaseTm) tm3tm);
                            }

                            tu.attr(tm3a, vv);
                        }
                    }

                    if (oldCount % 1000 == 0)
                    {
                        if (!userInterrupt)
                        {
                            List<TM3Tu<GSTuvData>> saved = saver.save(
                                    TM3SaveMode.MERGE, tm.isIndexTarget());
                            // try {
                            // tm3SegmentTmInfo.luceneIndexTus(tm.getId(),
                            // saved);
                            // } catch (Exception e) {
                            // throw new TM3Exception(e);
                            // }
                            newCount += saved.size();

                            saver = tm3tm.createSaver();

                            // Update the percentage
                            progress.setPercentage((int) ((100 * newCount) / totalCount));
                        }
                    }
                }
            }
            // HACK: Normally we should call segments.finish() here, but we
            // still own/need the session, so we'll clean it up ourselves.
            synchronized (this)
            {
                if (!userInterrupt)
                {
                    List<TM3Tu<GSTuvData>> saved = saver
                            .save(TM3SaveMode.MERGE, tm.isIndexTarget());
                    // try {
                    // tm3SegmentTmInfo.luceneIndexTus(tm.getId(), saved);
                    // } catch (Exception e) {
                    // throw new TM3Exception(e);
                    // }
                    newCount += saved.size();
                    progress.setPercentage(99);

                    progress.setMessageKey("", "Created TM3 " + newTmName
                            + " with " + newCount + " TUs");

                    // Now update any TM Profiles that are saving to the old TM
                    // so that they
                    // update to the migrated TM.
                    /**
                     * progress.setMessageKey("lb_tm_migrate_tm3_created",
                     * "Updating TM Profiles"); List<TranslationMemoryProfile>
                     * profiles =
                     * session.createCriteria(TranslationMemoryProfile.class)
                     * .add(Restrictions.eq("projectTmIdForSave",
                     * oldTm.getId())) .list(); for (TranslationMemoryProfile p
                     * : profiles) { p.setProjectTmIdForSave(tm.getId()); }
                     */
                    progress.setPercentage(100);
                    progress.setMessageKey("lb_done", "Done");
                    return tm;
                }
                else
                {
                    progress.setMessageKey("lb_tm_convert_cancel",
                            "User cancel the conversion");
                    return null;
                }
            }
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            return null;
        }
        finally
        {
            DbUtil.silentReturnConnection(conn);
        }
    }

    /**
     * Migrate the TM. Because big TMs may take a long time to migrate, this
     * code will use multiple transactions to write out the new TM data.
     * 
     * @param progress
     *            ProgressReporter for status updates
     * @return migrated TM, or NULL if the operation failed
     */
    public ProjectTM migrate()
    {
        Connection conn = null;
        try
        {
            conn = DbUtil.getConnection();
            // Change the status to "Converting" first !!!
            convertProcess.setStatus(WebAppConstants.TM_STATUS_CONVERTING);

            TM3Manager manager = DefaultManager.create();
            long tm3Id = -1l, lastTUId = 0;
            String newTM3Name = "";
            int basePercentage = 0;

            if (oldTm.getLastTUId() >= 0)
            {
                // Current TM2 has existed TM3 conversion
                try
                {
                    convertingTm = ServerProxy.getProjectHandler()
                            .getProjectTMById(oldTm.getConvertedTM3Id(), false);
                }
                catch (Exception e)
                {
                    logger.error("Can not get converted TM3 information.", e);
                    return null;
                }

                tm3Id = convertingTm.getTm3Id();
                basePercentage = convertingTm.getConvertRate();
                lastTUId = oldTm.getLastTUId();
                tm3tm = manager.getTm(new GSDataFactory(), tm3Id);

                convertProcess.setConvertedRate(basePercentage);
                convertProcess.setLastTUId(lastTUId);

                logger.info("Continue with the last conversion which was stopped or cancelled.");
            }
            else
            {
                // new TM3
                newTM3Name = getUniqueTmName(oldTm.getName());
                if (newTM3Name == null)
                {
                    updateTMConvertProcess(0, 0,
                            WebAppConstants.TM_STATUS_CONVERTED_FAIL);
                    return null;
                }
                logger.info("Converting " + oldTm.getName() + " to new TM3 "
                        + newTM3Name);

                tm3tm = manager.createMultilingualSharedTm(new GSDataFactory(),
                        SegmentTmAttribute.inlineAttributes(), companyId);

                // Create an event for this import
                convertingTm = new ProjectTM();
                convertingTm.setName(newTM3Name);
                convertingTm.setDomain(oldTm.getDomain());
                convertingTm.setOrganization(oldTm.getOrganization());
                convertingTm.setDescription(oldTm.getDescription());
                convertingTm.setCreationUser(oldTm.getCreationUser());
                convertingTm.setCreationDate(oldTm.getCreationDate());
                convertingTm.setCompanyId(oldTm.getCompanyId());
                convertingTm.setTm3Id(tm3tm.getId());
                convertingTm.setIndexTarget(oldTm.isIndexTarget());
                convertingTm.setIsRemoteTm(false);
                convertingTm.setConvertedTM3Id(oldTm.getId());
                convertingTm.setConvertRate(1);
                // add attributes from old tm
                List<TMAttribute> oldAtts = oldTm.getAllTMAttributes();
                Set<TMAttribute> attSet = new HashSet<TMAttribute>();
                if (oldAtts != null)
                {
                    for (TMAttribute oldAtt : oldAtts)
                    {
                        TMAttribute tmatt = new TMAttribute();
                        tmatt.setAttributename(oldAtt.getAttributename());
                        tmatt.setSettype(oldAtt.getSettype());
                        tmatt.setTm(convertingTm);

                        attSet.add(tmatt);
                    }
                }
                convertingTm.setAttributes(attSet);

                HibernateUtil.save(convertingTm);

                oldTm.setConvertedTM3Id(convertingTm.getId());
                oldTm.setConvertRate(1);
                // Ensure "convertedTm3Id" is updated in DB for restoring
                // cancelled or stopped conversion.
                HibernateUtil.getSession().merge(oldTm);

                basePercentage = 5;
                lastTUId = 0;
            }
            convertProcess.setTm3Id(convertingTm.getId());
            convertProcess.setTm3Name(convertingTm.getName());

            updateTMConvertProcess(basePercentage, lastTUId,
                    WebAppConstants.TM_STATUS_CONVERTING);

            EventMap events = new EventMap(tm3tm);
            TM3Attribute typeAttr = TM3Util.getAttr(tm3tm, TYPE);
            TM3Attribute formatAttr = TM3Util.getAttr(tm3tm, FORMAT);
            TM3Attribute sidAttr = TM3Util.getAttr(tm3tm, SID);
            TM3Attribute translatableAttr = TM3Util
                    .getAttr(tm3tm, TRANSLATABLE);
            TM3Attribute fromWsAttr = TM3Util.getAttr(tm3tm, FROM_WORLDSERVER);

            SegmentResultSet segments = oldTm.getSegmentTmInfo()
                    .getAllSegments(oldTm, lastTUId, conn);

            // In order to calculate completion percentage, we need to see how
            // big the old TM was
            long totalCount = oldTm.getSegmentTmInfo().getAllSegmentsCount(
                    oldTm, lastTUId);

            // If need to create lucene index, the percentage will be double
            totalCount = 2 * totalCount;

            long oldCount = 0;
            int percentage = 0, loop = 0;
            int round = (int) (totalCount / 1000);

            if (totalCount % 1000 != 0)
                round++;

            TM3Saver<GSTuvData> saver = tm3tm.createSaver();

            // Create the project TM that points to the this TM. Do it now so
            // we have the id for the lucene
            // just to borrow luceneIndexTus
            Tm3SegmentTmInfo tm3SegmentTmInfo = new Tm3SegmentTmInfo();

            // the lucene index uses this
            CompanyThreadLocal.getInstance().setIdValue(
                    Long.toString(companyId));

            while (true)
            {
                synchronized (this)
                {
                    if (userInterrupt || !segments.hasNext())
                    {
                        break;
                    }
                    SegmentTmTu oldTu = segments.next();
                    oldCount++;
                    if (oldTu == null)
                    {
                        continue;
                    }
                    lastTUId = oldTu.getId();
                    BaseTmTuv oldSrcTuv = oldTu.getSourceTuv();
                    TM3Saver<GSTuvData>.Tu tu = saver
                            .tu(new GSTuvData(oldSrcTuv),
                                    oldTu.getSourceLocale(),
                                    events.get(oldSrcTuv),
                                    oldSrcTuv.getCreationUser(),
                                    oldSrcTuv.getCreationDate(),
                                    oldSrcTuv.getModifyUser(),
                                    oldSrcTuv.getModifyDate(),
                                    oldSrcTuv.getLastUsageDate(),
                                    oldSrcTuv.getJobId(),
                                    oldSrcTuv.getJobName(),
                                    oldSrcTuv.getPreviousHash(),
                                    oldSrcTuv.getNextHash(),
                                    oldSrcTuv.getSid());
                    tu.attr(fromWsAttr, oldTu.isFromWorldServer());
                    tu.attr(translatableAttr, oldTu.isTranslatable());
                    if (oldTu.getType() != null)
                    {
                        tu.attr(typeAttr, oldTu.getType());
                    }
                    if (oldTu.getFormat() != null)
                    {
                        tu.attr(formatAttr, oldTu.getFormat());
                    }
                    if (oldSrcTuv.getSid() != null)
                    {
                        tu.attr(sidAttr, oldSrcTuv.getSid());
                    }
                    for (BaseTmTuv tuv : oldTu.getTuvs())
                    {
                        if (tuv.equals(oldSrcTuv))
                        {
                            continue;
                        }
						tu.target(new GSTuvData(tuv), tuv.getLocale(),
								events.get(tuv), tuv.getCreationUser(),
								tuv.getCreationDate(), tuv.getModifyUser(),
								tuv.getModifyDate(), tuv.getLastUsageDate(),
								tuv.getJobId(), tuv.getJobName(),
								tuv.getPreviousHash(), tuv.getNextHash(),
								tuv.getSid());
                    }
                    // handle TU properties
                    Collection<ProjectTmTuTProp> props = oldTu.getProps();
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

                            if (tm3tm.doesAttributeExist(name))
                            {
                                tm3a = tm3tm.getAttributeByName(name);
                            }
                            else
                            {
                                tm3a = TM3Util.toTM3Attribute(prop);
                                tm3a = TM3Util.saveTM3Attribute(tm3a,
                                        (BaseTm) tm3tm);
                            }

                            tu.attr(tm3a, vv);
                        }
                    }

                    if (oldCount % 1000 == 0)
                    {
                        if (!userInterrupt)
                        {
                            List<TM3Tu<GSTuvData>> saved = saver.save(
                                    TM3SaveMode.MERGE,
                                    convertingTm.isIndexTarget());
                            try
                            {
                                tm3SegmentTmInfo.luceneIndexTus(
                                        convertingTm.getId(), saved);
                                loop++;
                                percentage = calculatePercentage(
                                        basePercentage, loop, round);
                                updateTMConvertProcess(percentage, lastTUId,
                                        WebAppConstants.TM_STATUS_CONVERTING);
                            }
                            catch (Exception e)
                            {
                                throw new TM3Exception(e);
                            }

                            // Commit this batch and start a new transaction
                            loop++;
                            percentage = calculatePercentage(basePercentage,
                                    loop, round);
                            updateTMConvertProcess(percentage, lastTUId,
                                    WebAppConstants.TM_STATUS_CONVERTING);

                            saver = tm3tm.createSaver();
                        }
                    }
                }
            }
            // HACK: Normally we should call segments.finish() here, but we
            // still own/need the session, so we'll clean it up ourselves.
            synchronized (this)
            {
                if (!userInterrupt)
                {
                    List<TM3Tu<GSTuvData>> saved = saver.save(
                            TM3SaveMode.MERGE, convertingTm.isIndexTarget());
                    try
                    {
                        tm3SegmentTmInfo.luceneIndexTus(convertingTm.getId(),
                                saved);
                    }
                    catch (Exception e)
                    {
                        throw new TM3Exception(e);
                    }
                    updateTMConvertProcess(100, lastTUId,
                            WebAppConstants.TM_STATUS_DEFAULT);

                    addUsersToTmAccessControl();

                    logger.info("TM3 Conversion for " + newTM3Name + " is done");

                    return convertingTm;
                }
                else
                {
                    updateTMConvertProcess(percentage, lastTUId,
                            WebAppConstants.TM_STATUS_CONVERTED_CANCELLED);
                    return null;
                }
            }
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            return null;
        }
        finally
        {
            DbUtil.silentReturnConnection(conn);
        }
    }

    private void addUsersToTmAccessControl()
    {
        ProjectTMTBUsers tmUser = new ProjectTMTBUsers();
        ArrayList<User> addedUsers = new ArrayList<User>(tmUser.getAddedUsers(
                String.valueOf(oldTm.getId()), "TM"));
        String tmId = String.valueOf(convertingTm.getId());
        for (User user : addedUsers)
        {
            tmUser.addUsers(user.getUserId(), tmId, "TM");
        }
    }

    private int calculatePercentage(int basePercentage, int loop, int round)
    {
        int convertRate;
        double calculateRate;
        calculateRate = 100 - basePercentage;
        calculateRate = calculateRate * loop / round;
        convertRate = (int) calculateRate;
        return basePercentage + convertRate;
    }

    private void updateTMConvertProcess(int convertedRate, long lastTUId,
            String status)
    {
        oldTm = HibernateUtil.get(ProjectTM.class, oldTm.getId());
        if (convertedRate == 100)
        {
            oldTm.setConvertRate(0);
            oldTm.setLastTUId(-1);
        }
        else
        {
            oldTm.setLastTUId(lastTUId);
        }
        HibernateUtil.update(oldTm);

        convertingTm.setConvertRate(convertedRate);
        convertingTm.setStatus(status);
        HibernateUtil.update(convertingTm);

        convertProcess.setConvertedRate(convertedRate);
        convertProcess.setLastTUId(lastTUId);
        convertProcess.setStatus(status);
    }

    public TM3Tm<GSTuvData> getCurrrentTm3()
    {
        return this.tm3tm;
    }

    public String getUniqueTmName(String base)
    {
        for (int i = 1; i < 100; i++)
        {
            StringBuilder sb = new StringBuilder(base).append("_TM3");
            if (i > 1)
            {
                sb.append("_").append(i);
            }
            String candidate = sb.toString();
            if (!checkForTmByName(candidate))
            {
                return candidate;
            }
        }
        // Couldn't find one in 100 tries? Give up.
        return null;
    }

    private boolean checkForTmByName(String name)
    {
        String hql = "from ProjectTM ptm where ptm.name ='" + name + "'";
        return HibernateUtil.search(hql).size() > 0;
    }

    public void cancelConvert()
    {
        this.userInterrupt = true;
        this.convertProcess.setStatus("Cancelling");
        try
        {
            ProjectTM tm3Tm = ServerProxy.getProjectHandler().getProjectTMById(
                    convertingTm.getId(), false);
            tm3Tm.setStatus("Cancelling");
            HibernateUtil.update(tm3Tm);
        }
        catch (Exception e)
        {
            logger.error(
                    "Error when update project TM status to 'Cancelling'.", e);
        }
    }

    static class EventMap
    {
        private TM3Tm<GSTuvData> tm;
        private Map<LegacyEventKey, TM3Event> events = new HashMap<LegacyEventKey, TM3Event>();

        EventMap(TM3Tm<GSTuvData> tm)
        {
            this.tm = tm;
        }

        public TM3Event get(BaseTmTuv tuv)
        {
            String username = tuv.getModifyUser() != null ? tuv.getModifyUser()
                    : tuv.getCreationUser();
            Date date = tuv.getModifyDate() != null ? tuv.getModifyDate() : tuv
                    .getCreationDate();
            LegacyEventKey k = new LegacyEventKey(username, date);
            TM3Event e = events.get(k);
            if (e == null)
            {
                e = tm.addEvent(EventType.LEGACY_MIGRATE.getValue(), username,
                        null, date);
                events.put(k, e);
            }
            return e;
        }
    }

    static class LegacyEventKey
    {
        String username;
        Date date;

        LegacyEventKey(String username, Date date)
        {
            this.username = username;
            this.date = date;
        }

        @Override
        public boolean equals(Object o)
        {
            LegacyEventKey k = (LegacyEventKey) o;
            return username.equals(k.username) && date.equals(k.date);
        }

        @Override
        public int hashCode()
        {
            return 17 * username.hashCode() + date.hashCode();
        }
    }
}
