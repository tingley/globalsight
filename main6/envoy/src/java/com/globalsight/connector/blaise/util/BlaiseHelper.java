/**
 * Copyright 2009 Welocalize, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * <p>
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.globalsight.connector.blaise.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPInputStream;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.cognitran.blaise.translation.api.ClientFactory;
import com.cognitran.blaise.translation.api.TranslationAgencyClient;
import com.cognitran.client.IncompatibleVersionException;
import com.cognitran.core.model.util.Collections;
import com.cognitran.translation.client.PublicationTypeUsageDetails;
import com.cognitran.translation.client.TranslationPageCommand;
import com.cognitran.translation.client.TranslationStatisticsDetails;
import com.cognitran.translation.client.workflow.TranslationInboxEntry;
import com.cognitran.workflow.client.InboxEntry;
import com.globalsight.connector.blaise.BlaiseConstants;
import com.globalsight.connector.blaise.CreateBlaiseJobThread;
import com.globalsight.connector.blaise.form.BlaiseConnectorAttribute;
import com.globalsight.connector.blaise.form.CreateBlaiseJobForm;
import com.globalsight.connector.blaise.vo.TranslationInboxEntryVo;
import com.globalsight.cxe.entity.blaise.BlaiseConnector;
import com.globalsight.cxe.entity.blaise.BlaiseConnectorJob;
import com.globalsight.cxe.entity.blaise.BlaiseConnectorJobException;
import com.globalsight.cxe.entity.customAttribute.Attribute;
import com.globalsight.cxe.entity.customAttribute.AttributeClone;
import com.globalsight.cxe.entity.customAttribute.Condition;
import com.globalsight.cxe.entity.customAttribute.DateCondition;
import com.globalsight.cxe.entity.customAttribute.FloatCondition;
import com.globalsight.cxe.entity.customAttribute.IntCondition;
import com.globalsight.cxe.entity.customAttribute.JobAttribute;
import com.globalsight.cxe.entity.customAttribute.ListCondition;
import com.globalsight.cxe.entity.customAttribute.SelectOption;
import com.globalsight.cxe.entity.customAttribute.TextCondition;
import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.everest.company.MultiCompanySupportedThread;
import com.globalsight.everest.foundation.BasicL10nProfile;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.util.comparator.BlaiseInboxEntryComparator;
import com.globalsight.ling.common.URLEncoder;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.FileUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.StringUtil;

public class BlaiseHelper
{
    static private final Logger logger = Logger.getLogger(BlaiseHelper.class);
    // GBS-4748, blaiseConnectorLogger
    static private final Logger blaiseConnectorLogger = Logger
            .getLogger(BlaiseHelper.class.getName() + ".entry");

    private BlaiseConnector blc = null;
    private TranslationAgencyClient client = null;
    private BlaiseEntryCollect entryCollect = null;

    private static List<String> specialChars = new ArrayList<String>();

    public static final List<java.util.Locale> blaiseSupportedLocales = new ArrayList<java.util.Locale>();
    public static final HashMap<String, com.cognitran.core.model.i18n.Locale> blaiseSupportedLocalesMap = new HashMap<String, com.cognitran.core.model.i18n.Locale>();

    public static final SortedMap<String, String> relatedObjectClassName2Type = new TreeMap<String, String>();
    public static final SortedMap<String, String> type2RelatedObjectClassName = new TreeMap<String, String>();

    static
    {
        java.util.Locale javaLocale = null;
        com.cognitran.core.model.i18n.Locale[] blaiseLocales = com.cognitran.core.model.i18n.Locale
                .values();
        for (com.cognitran.core.model.i18n.Locale blaiseLocale : blaiseLocales)
        {
            javaLocale = blaiseLocale.toLocale();
            blaiseSupportedLocales.add(javaLocale);
            String key = javaLocale.getLanguage() + "_" + javaLocale.getCountry();
            blaiseSupportedLocalesMap.put(key.toLowerCase(), blaiseLocale);
        }

        relatedObjectClassName2Type.put(BlaiseConstants.BLAISE_TYPE_GRAPHIC,
                BlaiseConstants.GS_TYPE_GRAPHIC);
        relatedObjectClassName2Type.put(BlaiseConstants.BLAISE_TYPE_STANDALONE,
                BlaiseConstants.GS_TYPE_STANDALONE);
        relatedObjectClassName2Type.put(BlaiseConstants.BLAISE_TYPE_PROCEDURE,
                BlaiseConstants.GS_TYPE_PROCEDURE);
        relatedObjectClassName2Type.put(BlaiseConstants.BLAISE_TYPE_CONTROLLED_CONTENT,
                BlaiseConstants.GS_TYPE_CONTROLLED_CONTENT);
        relatedObjectClassName2Type.put(BlaiseConstants.BLAISE_TYPE_TRANSLATABLE_OBJECT,
                BlaiseConstants.GS_TYPE_TRANSLATABLE_OBJECT);

        type2RelatedObjectClassName.put(BlaiseConstants.GS_TYPE_GRAPHIC,
                BlaiseConstants.BLAISE_TYPE_GRAPHIC);
        type2RelatedObjectClassName.put(BlaiseConstants.GS_TYPE_STANDALONE,
                BlaiseConstants.BLAISE_TYPE_STANDALONE);
        type2RelatedObjectClassName.put(BlaiseConstants.GS_TYPE_PROCEDURE,
                BlaiseConstants.BLAISE_TYPE_PROCEDURE);
        type2RelatedObjectClassName.put(BlaiseConstants.GS_TYPE_CONTROLLED_CONTENT,
                BlaiseConstants.BLAISE_TYPE_CONTROLLED_CONTENT);
        type2RelatedObjectClassName.put(BlaiseConstants.GS_TYPE_TRANSLATABLE_OBJECT,
                BlaiseConstants.BLAISE_TYPE_TRANSLATABLE_OBJECT);
    }

    public BlaiseHelper(BlaiseConnector blc)
    {
        this.blc = blc;
    }

    public String doTest()
    {
        try
        {
            client = initTranslationClient();
        }
        catch (UnknownHostException e)
        {
            logger.warn("Incorrect URL: " + blc.getUrl());
            return "Incorrect URL!";
        }
        catch (SecurityException e)
        {
            logger.warn("Incorrect username or password: " + blc.getUsername() + "/"
                    + blc.getPassword());
            return "Incorrect username or password!";
        }
        catch (IncompatibleVersionException e)
        {
            logger.warn("Incorrect client core version: " + blc.getClientCoreVersion());
            return e.getMessage().replace("'", "").replace("\"", "");
        }
        catch (Exception e)
        {
            String msg = e.getMessage();
            logger.warn("Blaise connector test failure: " + e.getMessage());
            if (msg.indexOf("Unexpected response status") > -1 || msg.indexOf("503") > -1)
            {
                msg = msg.replace("'", "").replace("\"", "");
                return "Connection failed: " + msg;
            }
            return "Connection failed!";
        }

        return "";
    }

    public int getInboxEntryCount(TranslationPageCommand command)
    {
        int number = 0;
        try
        {
            TranslationAgencyClient client = getTranslationClient();
            if (client == null)
            {
                logger.error("TranslationAgencyClient is null.");
                return 0;
            }

            number = client.getInboxEntryCount(command);
        }
        catch (Exception e)
        {
            logger.error("Error when getInboxEntryCount(TranslationPageCommand command):", e);
        }

        return number;
    }

    public List<TranslationInboxEntryVo> listInbox(TranslationPageCommand command)
    {
        List<TranslationInboxEntryVo> results = new ArrayList<TranslationInboxEntryVo>();
        try
        {
            TranslationAgencyClient client = getTranslationClient();
            if (client == null)
            {
                logger.error("TranslationAgencyClient is null, cannot list inbox.");
                return results;
            }

            List<InboxEntry> inboxEntries = client.listInbox(command);
            results = convertToGS(inboxEntries, client);
        }
        catch (Exception e)
        {
            logger.error("Error when invoke listInbox(TranslationPageCommand command): ", e);
        }

        return results;
    }

    /**
     * Converts a set of Blaise inbox entries to GlobalSight
     * TranslationInboxEntryVo objects
     */
    private List<TranslationInboxEntryVo> convertToGS(List<InboxEntry> entries,
            TranslationAgencyClient client) throws Exception
    {
        if (entries == null || entries.size() == 0 || client == null)
            return new ArrayList<TranslationInboxEntryVo>();

        List<TranslationInboxEntryVo> vos = new ArrayList<>(entries.size());
        Set<Long> idSets = new HashSet<>();
        for (InboxEntry entry : entries)
            idSets.add(entry.getId());
        long runTime = System.currentTimeMillis();
        Map<InboxEntry, PublicationTypeUsageDetails> usages = client
                .mapPublicationTypeUsages(idSets);
        logger.debug(
                "Get all usages by id set used " + (System.currentTimeMillis() - runTime) + " ms");
        runTime = System.currentTimeMillis();
        Map<InboxEntry, TranslationStatisticsDetails> counts = client
                .mapTranslationStatistics(idSets);
        logger.debug("Get all word count by id set used " + (System.currentTimeMillis() - runTime)
                + " ms");

        boolean hasUsages = usages != null && usages.size() > 0;
        boolean hasCounts = counts != null && counts.size() > 0;
        PublicationTypeUsageDetails usage;
        TranslationStatisticsDetails detailCount;

        runTime = System.currentTimeMillis();
        for (InboxEntry entry : entries)
        {
            TranslationInboxEntryVo vo = new TranslationInboxEntryVo((TranslationInboxEntry) entry);
            if (hasUsages)
            {
                usage = usages.get(entry);
                if (usage != null)
                    vo.setUsages(usage.getTypes());
            }
            if (hasCounts)
            {
                detailCount = counts.get(entry);
                if (detailCount != null)
                    vo.setWordCount(detailCount.getNewSentencesWords());
            }
            vos.add(vo);
        }
        logger.debug(
                "Changed objects to GS used " + (System.currentTimeMillis() - runTime) + " ms");

        return vos;
    }

    /**
     * Converts Blaise translation inbox entry to GlobalSight
     * TranslationInboxEntryVo object
     */
    private TranslationInboxEntryVo convert(TranslationInboxEntry entry,
            TranslationAgencyClient client) throws Exception
    {
        long runTime = System.currentTimeMillis();
        TranslationInboxEntryVo vo = new TranslationInboxEntryVo(entry);
        // logger.info("Convert TranslationInboxEntry directly used " +
        // (System.currentTimeMillis
        // () - runTime) + " ms");
        Set<Long> idSet = new HashSet<>(1);
        idSet.add(entry.getId());
        List<String> entryUsages = new ArrayList<>();
        // Fetch usages of entry
        runTime = System.currentTimeMillis();
        Map<InboxEntry, PublicationTypeUsageDetails> usages = client
                .mapPublicationTypeUsages(idSet);
        if (usages != null)
        {
            for (Map.Entry<InboxEntry, PublicationTypeUsageDetails> item : usages.entrySet())
            {
                if (entry.getId() == item.getKey().getId() && item.getValue() != null)
                {
                    entryUsages = item.getValue().getTypes();
                }
            }
        }
        vo.setUsages(entryUsages);
        // logger.info("Fetch usages of entry used " +
        // (System.currentTimeMillis() - runTime) + "
        // ms");

        runTime = System.currentTimeMillis();
        Map<InboxEntry, TranslationStatisticsDetails> counts = client
                .mapTranslationStatistics(idSet);
        int wordCount = 0;
        if (counts != null)
        {
            for (Map.Entry<InboxEntry, TranslationStatisticsDetails> detailsEntry : counts
                    .entrySet())
            {
                if (entry.getId() == detailsEntry.getKey().getId()
                        && detailsEntry.getValue() != null)
                    wordCount = detailsEntry.getValue().getNewSentencesWords();
            }
        }
        vo.setWordCount(wordCount);

        return vo;
    }

    /**
     * Groups the inbox entries
     */
    public void groupInboxEntries()
    {
        long lastMaxEntryId = blc.getLastMaxEntryId();
        long maxInboxEntryId = getMaxInboxEntryId();
        if (maxInboxEntryId <= lastMaxEntryId)
            return;
        try
        {
            long fileProfileId = blc.getDefaultFileProfileId();
            FileProfile fp = ServerProxy.getFileProfilePersistenceManager()
                    .getFileProfileById(fileProfileId, false);
            long lpId = fp.getL10nProfileId();
            L10nProfile lp = ServerProxy.getProjectHandler().getL10nProfile(lpId);
            Locale sourceLocale = lp.getSourceLocale().getLocale();
            GlobalSightLocale[] targetLocales = lp.getTargetLocales();
            ArrayList<String> targetLocaleList = new ArrayList<>();
            if (targetLocales != null)
            {
                for (GlobalSightLocale locale : targetLocales)
                    targetLocaleList.add(fixLocale(locale.getLocaleCode()));
            }
            else
                return;
            String userId = blc.getLoginUser();
            long companyId = blc.getCompanyId();

            List<TranslationInboxEntryVo> entries = null;
            List<TranslationInboxEntryVo> totalEntries = new ArrayList<>();
            ArrayList<TranslationInboxEntryVo> hduEntries = null;
            ArrayList<TranslationInboxEntryVo> inSheetEntries = null;
            ArrayList<TranslationInboxEntryVo> otherEntries = null;
            TranslationPageCommand command = new TranslationPageCommand();
            int count = 100;
            if (blc.getQaCount() > 0)
                count = blc.getQaCount();
            else
                count = getInboxEntryCount(command);
            int fetchCount = 0;
            List<Long> existedEntryIds = getEntryIdsInGS(blc.getId());
            long tmpEntryId = -1L;
            int pageIndex = 0;
            String tmp;
            logger.info("Start fetching entries for company " + companyId);
            while (fetchCount < count)
            {
                command = initTranslationPageCommand(pageIndex, 100, null, sourceLocale.toString(),
                        null, null, null, 0, false);
                command.sortById();
                command.setSortDesc(false);
                entries = listInbox(command);
                if (entries == null || entries.isEmpty())
                {
                    break;
                }
                for (TranslationInboxEntryVo vo : entries)
                {
                    tmpEntryId = vo.getEntry().getId();
                    tmp = vo.getEntry().getTargetLocale().getLocaleCode();
                    if (existedEntryIds.contains(tmpEntryId) || !targetLocaleList.contains(tmp))
                        continue;
                    totalEntries.add(vo);
                    fetchCount++;
                    if (fetchCount >= count)
                        break;
                }
                pageIndex++;
                logger.info("Current page index " + pageIndex + ", fetched count " + fetchCount
                        + ", configured count " + count);
            }

            if (totalEntries != null && totalEntries.size() > 0)
            {
                hduEntries = new ArrayList<>();
                inSheetEntries = new ArrayList<>();
                otherEntries = new ArrayList<>();
                Set<Long> entryIds = new HashSet<>();
                for (TranslationInboxEntryVo vo : totalEntries)
                {
                    if (vo.isUsageOfIsSheet() && isInSheetType(vo))
                        inSheetEntries.add(vo);
                    else if (vo.isUsageOfHDU() && isHDUType(vo))
                        hduEntries.add(vo);
                    else
                        otherEntries.add(vo);
                    entryIds.add(vo.getEntry().getId());
                    try
                    {
                        claim(vo.getEntry().getId());
                    }
                    catch (Exception e)
                    {
                        logger.error("Encountered an error when claiming entry "
                                + vo.getEntry().getId());
                    }
                }
                logger.info("Done fetching entries for company " + companyId);

                BasicL10nProfile l10Profile = HibernateUtil.get(BasicL10nProfile.class,
                        fp.getL10nProfileId());
                ExecutorService pool = Executors.newFixedThreadPool(5);
                CreateBlaiseJobForm blaiseForm = new CreateBlaiseJobForm();
                blaiseForm.setPriority("3");
                blaiseForm.setBlaiseConnectorId(String.valueOf(blc.getId()));
                blaiseForm.setUserName(userId);
                int size = 0;
                ArrayList<FileProfile> fps = null;
                String companyIdString = String.valueOf(companyId);
                if (inSheetEntries != null && inSheetEntries.size() > 0)
                {
                    createJob(blaiseForm, "I", companyIdString, userId, fp, inSheetEntries,
                            blc.isCombined());
                }
                if (otherEntries != null && otherEntries.size() > 0)
                {
                    createJob(blaiseForm, "A", companyIdString, userId, fp, otherEntries,
                            blc.isCombined());
                }
                if (hduEntries != null && hduEntries.size() > 0)
                {
                    createJob(blaiseForm, "H", companyIdString, userId, fp, hduEntries,
                            blc.isCombined());
                }
            }
        }
        catch (Exception e)
        {
            logger.error("Encountered an Error when fetching entries", e);
        }
    }

    private void createJob(CreateBlaiseJobForm blaiseJobForm, String type, String companyId,
            String userId, FileProfile fp, ArrayList<TranslationInboxEntryVo> entries,
            boolean isCombinedByLang) throws RemoteException
    {
        BasicL10nProfile l10Profile = HibernateUtil.get(BasicL10nProfile.class,
                fp.getL10nProfileId());
        User user = ServerProxy.getUserManager().getUser(userId);

        ExecutorService pool = Executors.newFixedThreadPool(5);
        int size = 0;
        ArrayList<FileProfile> fps = null;
        if (entries != null && entries.size() > 0)
        {
            String attributeString = getJobAttributeString(blc.getId(), type);

            if (isCombinedByLang)
            {
                blaiseJobForm.setCombineByLangs("on");
                HashMap<String, ArrayList<TranslationInboxEntryVo>> localeGroup = groupEntriesByLang(
                        entries);
                Iterator<String> keys = localeGroup.keySet().iterator();
                String targetLocale;
                ArrayList<TranslationInboxEntryVo> localeEntries;
                while (keys.hasNext())
                {
                    targetLocale = keys.next();
                    localeEntries = localeGroup.get(targetLocale);
                    size = localeEntries.size();
                    fps = new ArrayList<>(size);
                    for (int i = 0; i < size; i++)
                        fps.add(fp);
                    List<JobAttribute> jobAttributes = getJobAttributes(attributeString,
                            l10Profile);
                    CreateBlaiseJobThread runnable = new CreateBlaiseJobThread(user,
                            String.valueOf(companyId), blc, blaiseJobForm, localeEntries, fps, null,
                            null, JobImpl.createUuid(), jobAttributes, targetLocale);
                    Thread t = new MultiCompanySupportedThread(runnable);
                    pool.execute(t);
                }
            }
            else
            {
                fps = new ArrayList<>(1);
                fps.add(fp);
                blaiseJobForm.setCombineByLangs("");
                String companyIdString = String.valueOf(companyId);
                List<TranslationInboxEntryVo> jobEntries = null;
                for (TranslationInboxEntryVo entry : entries)
                {
                    jobEntries = new ArrayList<>(1);
                    jobEntries.add(entry);
                    List<JobAttribute> jobAttributes = getJobAttributes(attributeString,
                            l10Profile);
                    CreateBlaiseJobThread runnable = new CreateBlaiseJobThread(user,
                            companyIdString, blc, blaiseJobForm, jobEntries, fps, null, null,
                            JobImpl.createUuid(), jobAttributes, entry.getTargetLocaleAsString());
                    Thread t = new MultiCompanySupportedThread(runnable);
                    pool.execute(t);
                }
            }
        }
    }

    private HashMap<String, ArrayList<TranslationInboxEntryVo>> groupEntriesByLang(
            ArrayList<TranslationInboxEntryVo> entries)
    {
        HashMap<String, ArrayList<TranslationInboxEntryVo>> result = new HashMap<>();
        if (entries != null && entries.size() > 0)
        {
            ArrayList<TranslationInboxEntryVo> entryList = new ArrayList<>();
            String tmp;
            for (TranslationInboxEntryVo entry : entries)
            {
                tmp = entry.getTargetLocaleAsString();
                entryList = result.get(tmp);
                if (entryList == null)
                    entryList = new ArrayList<>();
                entryList.add(entry);
                result.put(tmp, entryList);
            }
        }
        return result;
    }

    private String getJobAttributeString(long blcId, String blaiseJobType)
    {
        String result = "";
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try
        {
            conn = DbUtil.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("select attribute_id, attribute_value, attribute_type from "
                    + "connector_blaise_attributes where connector_id=" + blcId + " and "
                    + "blaise_job_type='" + blaiseJobType + "'");
            StringBuilder data = new StringBuilder();
            long attrId;
            String attrValue;
            String attrType;
            while (rs.next())
            {
                attrId = rs.getLong(1);
                attrValue = rs.getString(2);
                attrType = rs.getString(3);
                if ("choiceList".equalsIgnoreCase(attrType))
                {
                    Attribute attribute = HibernateUtil.get(Attribute.class, attrId);
                    ListCondition lc = (ListCondition) attribute.getCondition();
                    List<SelectOption> options = lc.getSortedAllOptions();
                    long tmpAttrValue = Long.parseLong(attrValue);
                    for (SelectOption so : options)
                    {
                        if (so.getId() == tmpAttrValue)
                        {
                            attrValue = so.getValue();
                            break;
                        }
                    }
                }
                data.append("5,.,").append(attrId).append(",.,").append(attrValue).append(";.;");
            }
            result = data.toString();
            if (result.length() > 0)
                result = result.substring(0, result.length() - 3);
        }
        catch (Exception e)
        {
            logger.error("Error found when getJobAttributeString.", e);
        }
        finally
        {
            DbUtil.silentClose(rs);
            DbUtil.silentClose(stmt);
            DbUtil.silentReturnConnection(conn);
        }
        return result;
    }

    public boolean isHDUType(TranslationInboxEntryVo vo)
    {
        if (vo == null || !vo.isUsageOfHDU())
            return false;
        String type = vo.getType();
        if (type.equals(BlaiseConstants.GS_TYPE_TRANSLATABLE_OBJECT)
                || type.equals(BlaiseConstants.GS_TYPE_CONTROLLED_CONTENT)
                || type.equals(BlaiseConstants.GS_TYPE_GRAPHIC)
                || type.equals(BlaiseConstants.GS_TYPE_PROCEDURE))
            return true;
        return false;
    }

    public boolean isInSheetType(TranslationInboxEntryVo vo)
    {
        if (vo == null || !vo.isUsageOfIsSheet())
            return false;
        String type = vo.getType();
        if (type.equals(BlaiseConstants.GS_TYPE_STANDALONE)
                || type.equals(BlaiseConstants.GS_TYPE_GRAPHIC))
            return true;
        return false;
    }

    public BlaiseEntryCollect getEntryCollect()
    {
        if (entryCollect == null)
            entryCollect = new BlaiseEntryCollect();
        return entryCollect;
    }

    /**
     * Gets maximum inbox entry id
     */
    public long getMaxInboxEntryId()
    {
        TranslationPageCommand command = new TranslationPageCommand(0, 1);
        command.sortById();
        command.setSortDesc(true);

        TranslationAgencyClient client = getTranslationClient();
        if (client == null)
        {
            logger.error("TranslationAgencyClient is null, cannot list inbox entry");
            return -1L;
        }
        try
        {
            List<InboxEntry> entries = client.listInbox(command);
            if (entries == null || entries.size() == 0)
                return -1L;
            TranslationInboxEntry entry = (TranslationInboxEntry) entries.get(0);

            return entry.getId();
        }
        catch (Exception e)
        {
            logger.error("Error when invoke getMaxInboxEntryId method", e);
            return -1L;
        }
    }

    public List<TranslationInboxEntryVo> listInboxByIds(Set<Long> ids,
            TranslationPageCommand command)
    {
        List<TranslationInboxEntryVo> results = new ArrayList<TranslationInboxEntryVo>();

        TranslationAgencyClient client = getTranslationClient();
        if (client == null)
        {
            logger.error("TranslationAgencyClient is null, cannot list inbox by Ids: "
                    + listToString(ids));
            return results;
        }

        try
        {
            List<InboxEntry> inboxEntries = client.listInbox(ids, command);
            for (InboxEntry entry : inboxEntries)
            {
                try
                {
                    TranslationInboxEntryVo vo = convert((TranslationInboxEntry) entry, client);
                    results.add(vo);
                }
                catch (Exception ignore)
                {
                    // ignore this entry if it has no required target locale
                }
            }
        }
        catch (Exception e)
        {
            logger.error("Error when invoke listInboxByIds(ids):" + listToString(ids), e);
        }

        return results;
    }

    public int getReferenceCount(long parentId, TranslationPageCommand command)
    {
        int number = 0;
        try
        {
            TranslationAgencyClient client = getTranslationClient();
            if (client == null)
            {
                logger.error("TranslationAgencyClient is null.");
                return 0;
            }

            number = client.getReferenceCount(parentId, command);
        }
        catch (Exception e)
        {
            logger.error("Error when getReferenceCount(parentId, TranslationPageCommand):", e);
        }

        return number;
    }

    public List<TranslationInboxEntryVo> listReferences(long parentId,
            TranslationPageCommand command)
    {
        List<TranslationInboxEntryVo> results = new ArrayList<TranslationInboxEntryVo>();
        try
        {
            TranslationAgencyClient client = getTranslationClient();
            if (client == null)
            {
                logger.error("TranslationAgencyClient is null.");
                return results;
            }

            List<InboxEntry> inboxEntries = client.listReferences(parentId, command);
            for (InboxEntry entry : inboxEntries)
            {
                try
                {
                    TranslationInboxEntryVo vo = convert((TranslationInboxEntry) entry, client);
                    results.add(vo);
                }
                catch (Exception ignore)
                {
                    // ignore this entry if it has no required target locale
                }
            }
        }
        catch (Exception e)
        {
            logger.error("Error when invoke listReferences(parentId, TranslationPageCommand): ", e);
        }

        return results;
    }

    /**
     * Claim Blaise inbox entries. If it has been claimed, Blaise API will throw
     * exception/warning.
     * <p>
     * As ONLY the first identifier is returned, we claim entry one by one.
     *
     * @param ids
     */
    public void claim(long id)
    {
        try
        {
            TranslationAgencyClient client = getTranslationClient();
            if (client == null)
            {
                logger.error("TranslationAgencyClient is null, entry cannot be claimed: " + id);
                return;
            }

            Set<Long> ids = new HashSet<Long>();
            ids.add(id);
            client.claim(ids);
        }
        catch (Exception e)
        {
            if (e.getMessage().toLowerCase().contains("task already claimed"))
            {
                logger.warn("Warning when claim entry(" + id + "): " + e.getMessage());
            }
            else
            {
                logger.error("Error when claim entry: " + id, e);
            }
        }
    }

    /**
     * Claim Blaise inbox entries. If it has been claimed, Blaise API will throw
     * exception/warning.
     * <p>
     * As ONLY the first identifier is returned, we claim entry one by one.
     *
     * @param ids
     */
    public void claim(Set<Long> ids)
    {
        try
        {
            TranslationAgencyClient client = getTranslationClient();
            if (client == null)
            {
                logger.error("TranslationAgencyClient is null, entry cannot be claimed: " + ids);
                return;
            }
            if (ids == null || ids.size() == 0)
                return;

            client.claim(new HashSet<Long>(ids));
        }
        catch (Exception e)
        {
            if (e.getMessage().toLowerCase().contains("task already claimed"))
            {
                logger.warn("Warning when claim entry(" + ids + "): " + e.getMessage());
            }
            else
            {
                logger.error("Error when claim entry: " + ids, e);
            }
        }
    }

    /**
     * Download XLIFF file. Note that current entry must be claimed already.
     *
     * @param entry
     *            -- TranslationInboxEntryVo
     * @param storeFile
     *            -- storeFile
     */
    public void downloadXliff(TranslationInboxEntryVo entry, File storeFile)
    {
        GZIPInputStream is = null;
        OutputStream os = null;
        try
        {
            TranslationAgencyClient client = getTranslationClient();
            if (client == null)
            {
                logger.error("TranslationAgencyClient is null, cannot download xliff for entry: "
                        + entry.getId());
                return;
            }

            is = (GZIPInputStream) client.downloadXliff(entry.getId());
            os = new FileOutputStream(storeFile);

            int count;
            byte data[] = new byte[1024];
            while ((count = is.read(data, 0, 1024)) != -1)
            {
                os.write(data, 0, count);
            }

            is.close();
            os.close();
        }
        catch (Exception e)
        {
            logger.error("Fail to downloadXliff for entry: " + entry.getId(), e);
        }
    }

    public void uploadXliff(long entryId, File file, long jobId)
    {
        try
        {
            if (!entryExists(entryId, jobId))
            {
                logger.info("Blaise entry: " + entryId + ", job " + jobId
                        + " is already closed. Set upload state to '"
                        + BlaiseConnectorJob.SUCCEED_CLOSED + "'");
                blaiseConnectorLogger.info("Blaise entry: " + entryId + ", job " + jobId
                        + " is already closed. Set upload state to '"
                        + BlaiseConnectorJob.SUCCEED_CLOSED + "'");
                BlaiseConnectorJob bcj = BlaiseManager.getBlaiseConnectorJobByJobIdEntryId(jobId,
                        entryId);
                if (bcj != null)
                {
                    String state = bcj.getUploadXliffState();
                    if (!BlaiseConnectorJob.SUCCEED.equals(state)
                            && !BlaiseConnectorJob.SUCCEED_CLOSED.equals(state))
                    {
                        updateUploadState(jobId, entryId, BlaiseConnectorJob.SUCCEED_CLOSED);
                    }
                }
                return;
            }

            TranslationAgencyClient client = getTranslationClient();
            if (client == null)
            {
                logger.error("TranslationAgencyClient is null, entry cannot be uploaded. Entry "
                        + entryId + ", job " + jobId + ", file " + file.getName());
                blaiseConnectorLogger
                        .error("TranslationAgencyClient is null, entry cannot be uploaded. Entry "
                                + entryId + ", job " + jobId + ", file " + file.getName());
                return;
            }

            // A simple replace to "cheat" Blaise API
            String content = FileUtil.readFile(file, BlaiseConstants.ENCODING);
            content = StringUtil.replace(content, "<target state=\"new\"",
                    "<target state=\"translated\"");
            content = StringUtil.replace(content, "<target state=\"needs-review-translation\"",
                    "<target state=\"translated\"");
            content = StringUtil.replace(content, "<target state=\"needs-i10n\"",
                    "<target state=\"translated\"");
            FileUtil.writeFile(file, content, BlaiseConstants.ENCODING);

            InputStream is = new FileInputStream(file);
            logger.info("Called uploadXliff() for entry " + entryId + ", job " + jobId);
            blaiseConnectorLogger
                    .info("Called uploadXliff() for entry " + entryId + ", job " + jobId);
            client.uploadXliff(entryId, is);
            logger.info("Blaise file is uploaded successfully for entry " + entryId + ", job "
                    + jobId + ", file " + file.getName());
            blaiseConnectorLogger.info("Blaise file is uploaded successfully for entry " + entryId
                    + ", job " + jobId + ", file " + file.getName());

            updateUploadXliffSuccess(jobId, entryId);
        }
        catch (Exception e)
        {
            logger.error("Encountered an error when uploading entry " + entryId + ", job " + jobId
                    + ", file " + file.getName(), e);
            blaiseConnectorLogger.error("Encountered an error when uploading entry " + entryId
                    + ", job " + jobId + ", file " + file.getName(), e);
            String[] args =
            { String.valueOf(entryId), String.valueOf(jobId), file.getName() };
            BlaiseConnectorJobException uploadExp = new BlaiseConnectorJobException(
                    BlaiseConnectorJobException.EXCEPTION_UPLOAD, args, e);
            setUploadException(jobId, entryId, uploadExp);
        }
    }

    private void setUploadException(long jobId, long blaiseEntryId,
            GeneralException p_uploadException)
    {
        BlaiseConnectorJob bcj = BlaiseManager.getBlaiseConnectorJobByJobIdEntryId(jobId,
                blaiseEntryId);
        if (bcj != null)
        {
            bcj.setUploadException(p_uploadException);
            bcj.setUploadXliffState(BlaiseConnectorJob.FAIL);
            HibernateUtil.saveOrUpdate(bcj);
        }
    }

    private void updateUploadState(long jobId, long blaiseEntryId, String state)
    {
        BlaiseConnectorJob bcj = BlaiseManager.getBlaiseConnectorJobByJobIdEntryId(jobId,
                blaiseEntryId);
        if (bcj != null)
        {
            bcj.setUploadXliffState(state);
            if (state != null && state.startsWith(BlaiseConnectorJob.SUCCEED))
            {
                // clear exception if setting state to "succeed"
                bcj.setUploadException(null);
            }
            HibernateUtil.saveOrUpdate(bcj);
        }
    }

    private void updateUploadXliffSuccess(long jobId, long blaiseEntryId)
    {
        updateUploadState(jobId, blaiseEntryId, BlaiseConnectorJob.SUCCEED);
    }

    public void complete(long entryId, long jobId)
    {
        try
        {
            if (!entryExists(entryId, jobId))
            {
                logger.info("Blaise entry: " + entryId + ", job " + jobId
                        + " is already closed. Set complete state to '"
                        + BlaiseConnectorJob.SUCCEED_CLOSED + "'");
                blaiseConnectorLogger.info("Blaise entry: " + entryId + ", job " + jobId
                        + " is already closed. Set complete state to '"
                        + BlaiseConnectorJob.SUCCEED_CLOSED + "'");
                BlaiseConnectorJob bcj = BlaiseManager.getBlaiseConnectorJobByJobIdEntryId(jobId,
                        entryId);
                if (bcj != null)
                {
                    String state = bcj.getCompleteState();
                    if (!BlaiseConnectorJob.SUCCEED.equals(state)
                            && !BlaiseConnectorJob.SUCCEED_CLOSED.equals(state))
                    {
                        updateCompleteState(jobId, entryId, BlaiseConnectorJob.SUCCEED_CLOSED);
                    }
                }
                return;
            }

            TranslationAgencyClient client = getTranslationClient();
            if (client == null)
            {
                logger.error("TranslationAgencyClient is null, entry cannot be completed. Entry "
                        + entryId + ", job " + jobId);
                blaiseConnectorLogger
                        .error("TranslationAgencyClient is null, entry cannot be completed. Entry "
                                + entryId + ", job " + jobId);
                return;
            }

            logger.info("Called complete() for entry " + entryId + ", job " + jobId);
            blaiseConnectorLogger.info("Called complete() for entry " + entryId + ", job " + jobId);
            client.complete(entryId);
            logger.info(
                    "Blaise entry is completed successfully. Entry " + entryId + ", job " + jobId);
            blaiseConnectorLogger.info(
                    "Blaise entry is completed successfully. Entry " + entryId + ", job " + jobId);

            updateCompleteSuccess(jobId, entryId);
        }
        catch (Exception e)
        {
            logger.error("Encountered an error when completing entry " + entryId + ", job " + jobId,
                    e);
            blaiseConnectorLogger.error(
                    "Encountered an error when completing entry " + entryId + ", job " + jobId, e);
            String[] args =
            { String.valueOf(entryId), String.valueOf(jobId) };
            BlaiseConnectorJobException completeExp = new BlaiseConnectorJobException(
                    BlaiseConnectorJobException.EXCEPTION_COMPLETE, args, e);
            setCompleteException(jobId, entryId, completeExp);
        }
    }

    private void setCompleteException(long jobId, long blaiseEntryId,
            GeneralException p_completeException)
    {
        BlaiseConnectorJob bcj = BlaiseManager.getBlaiseConnectorJobByJobIdEntryId(jobId,
                blaiseEntryId);
        if (bcj != null)
        {
            bcj.setCompleteException(p_completeException);
            bcj.setCompleteState(BlaiseConnectorJob.FAIL);
            HibernateUtil.saveOrUpdate(bcj);
        }
    }

    private void updateCompleteState(long jobId, long blaiseEntryId, String state)
    {
        BlaiseConnectorJob bcj = BlaiseManager.getBlaiseConnectorJobByJobIdEntryId(jobId,
                blaiseEntryId);
        if (bcj != null)
        {
            bcj.setCompleteState(state);
            if (state != null && state.startsWith(BlaiseConnectorJob.SUCCEED))
            {
                // clear exception if setting state to "succeed"
                bcj.setCompleteException(null);
            }
            HibernateUtil.saveOrUpdate(bcj);
        }
    }

    private void updateCompleteSuccess(long jobId, long blaiseEntryId)
    {
        updateCompleteState(jobId, blaiseEntryId, BlaiseConnectorJob.SUCCEED);
    }

    private boolean entryExists(long entryId, long jobId)
    {
        Set<Long> ids = new HashSet<Long>();
        ids.add(entryId);

        TranslationPageCommand command = initTranslationPageCommand(1, 100, null, null, null, null,
                null, 0, false);
        List<TranslationInboxEntryVo> entries = listInboxByIds(ids, command);
        if (entries == null || entries.size() == 0)
        {
            return false;
        }

        return true;
    }

    /**
     * Initialize a TranslationPageCommand object for query from Blaise server.
     *
     * @param pageIndex
     * @param pageSize
     * @param relatedObjectIdFilter
     * @param sourceLocaleFilter
     * @param targetLocaleFilter
     * @param typeFilter
     * @param desFilter
     * @param sortBy
     * @param sortDesc
     * @return TranslationPageCommand
     */
    public static TranslationPageCommand initTranslationPageCommand(int pageIndex, int pageSize,
            String relatedObjectIdFilter, String sourceLocaleFilter, String targetLocaleFilter,
            String typeFilter, String desFilter, int sortBy, boolean sortDesc)
    {
        TranslationPageCommand command = new TranslationPageCommand();

        command.setPageIndex(pageIndex);
        command.setPageSize(pageSize);

        setFilters(command, relatedObjectIdFilter, sourceLocaleFilter, targetLocaleFilter,
                typeFilter, desFilter);

        setSortBy(command, sortBy);

        if (sortDesc)
        {
            command.setSortDesc(true);
        }

        return command;
    }

    // Set filters
    private static void setFilters(TranslationPageCommand command, String relatedObjectIdFilter,
            String sourceLocaleFilter, String targetLocaleFilter, String typeFilter,
            String desFilter)
    {
        // Blaise ID
        if (StringUtil.isNotEmpty(relatedObjectIdFilter))
        {
            try
            {
                long relatedObjId = Long.parseLong(relatedObjectIdFilter.trim());
                command.setRelatedObjectIdFilter(relatedObjId);
            }
            catch (NumberFormatException ignore)
            {

            }
        }

        // Source Locale
        if (sourceLocaleFilter != null)
        {
            com.cognitran.core.model.i18n.Locale locale = blaiseSupportedLocalesMap
                    .get(sourceLocaleFilter.trim().toLowerCase());
            if (locale != null)
            {
                command.setSourceLocaleFilter(locale);
            }
        }

        // Target Locale
        if (targetLocaleFilter != null)
        {
            com.cognitran.core.model.i18n.Locale locale = blaiseSupportedLocalesMap
                    .get(targetLocaleFilter.trim().toLowerCase());
            if (locale != null)
            {
                command.setTargetLocaleFilter(locale);
            }
        }

        // Type
        if (StringUtil.isNotEmpty(typeFilter))
        {
            command.setObjectClassnameFilter(type2RelatedObjectClassName.get(typeFilter));
        }

        // Description
        if (StringUtil.isNotEmpty(desFilter))
        {
            desFilter = "*" + desFilter.trim() + "*";
            desFilter = URLEncoder.encode(desFilter);
            command.setDescriptionFilter(desFilter);
        }
    }

    private static void setSortBy(TranslationPageCommand command, int sortBy)
    {
        // sort by related object id as default
        command.sortByRelatedObjectId();

        if (sortBy == BlaiseInboxEntryComparator.SOURCE_LOCALE)
        {
            command.sortBySourceLocale();
        }
        else if (sortBy == BlaiseInboxEntryComparator.TARGET_LOCALE)
        {
            command.sortByTargetLocale();
        }
        else if (sortBy == BlaiseInboxEntryComparator.DESCRIPTION)
        {
            command.sortByDescription();
        }
        else if (sortBy == BlaiseInboxEntryComparator.WORKFLOW_START_DATE)
        {
            command.sortByCreationDate();
        }
        else if (sortBy == BlaiseInboxEntryComparator.DUE_DATE)
        {
            command.sortByDueDate();
        }
        else if (sortBy == BlaiseInboxEntryComparator.ENTRY_ID)
        {
            command.sortById();
        }
    }

    private TranslationAgencyClient getTranslationClient()
    {
        if (client != null)
        {
            return client;
        }

        try
        {
            client = initTranslationClient();
        }
        catch (UnknownHostException e)
        {
            logger.error("Incorrect URL: " + blc.getUrl(), e);
            blaiseConnectorLogger.error("Incorrect URL: " + blc.getUrl(), e);
        }
        catch (SecurityException e)
        {
            logger.error("Incorrect username or password: " + blc.getUsername() + "/"
                    + blc.getPassword(), e);
            blaiseConnectorLogger.error("Incorrect username or password: " + blc.getUsername() + "/"
                    + blc.getPassword(), e);
        }
        catch (IncompatibleVersionException e)
        {
            logger.error("Incorrect client core version: " + blc.getClientCoreVersion(), e);
            blaiseConnectorLogger
                    .error("Incorrect client core version: " + blc.getClientCoreVersion(), e);
        }
        catch (Exception e)
        {
            logger.error("Fail to get Blaise translation client: ", e);
            blaiseConnectorLogger.error("Fail to get Blaise translation client: ", e);
        }

        return client;
    }

    private TranslationAgencyClient initTranslationClient() throws SecurityException,
            IncompatibleVersionException, MalformedURLException, IOException, URISyntaxException
    {
        TranslationAgencyClient client = null;
        if (blc.getClientCoreVersion() != null)
        {
            ClientFactory.setClientCoreVersion(blc.getClientCoreVersion());
        }
        ClientFactory.setClientCoreRevision(blc.getClientCoreRevision());

        if (blc.getWorkflowId() == null)
        {
            client = ClientFactory.createClient(new URL(blc.getUrl()), blc.getUsername(),
                    blc.getPassword());
        }
        else
        {
            client = ClientFactory.createClient(new URL(blc.getUrl()), blc.getUsername(),
                    blc.getPassword(), blc.getWorkflowId());
        }

        return client;
    }

    public void destroyTranslationClient()
    {
        client = null;
    }

    /**
     * Blaise inbox entry file name is like
     * "Blaise inbox entry - Alerts - 40768 - 2 - es_MX.xlf".
     *
     * @param entry
     *            - TranslationInboxEntry boject
     * @return the file name which is used to create job in GlobalSight.
     */
    public static String getEntryFileName(TranslationInboxEntryVo entry)
    {
        StringBuilder fileName = new StringBuilder();
        String des = entry.getDescription();
        if (des == null || des.trim().length() == 0)
        {
            des = "No Description";
        }
        else
        {
            des = des.trim();
            if (des.length() > 55)
            {
                des = des.substring(0, 55);
            }
        }

        String localeInfo = entry.getTargetLocaleAsString();
        fileName.append(BlaiseConstants.FILENAME_PREFIX).append(entry.getRelatedObjectId())
                .append(BlaiseConstants.DASH).append(entry.getSourceRevision())
                .append(BlaiseConstants.DASH).append(des).append(BlaiseConstants.DASH)
                .append(localeInfo).append(BlaiseConstants.FILENAME_EXTENSION).toString();
        String fileNameStr = fileName.toString();

        return handleSpecialChars(fileNameStr);
    }

    /**
     * A typical Harley job name is
     * "Harley_[Falcon Target Value]_[target locale]_[plus a random number]".
     *
     * @param entry
     *            -- TranslationInboxEntryVo
     * @param falconTargetValue
     *            -- value of job attribute "Falcon Target Value".
     * @param targetLocale
     *            -- targetLocale from GS.
     * @return String -- job name
     */
    public static String getHarlyJobName(TranslationInboxEntryVo entry, String falconTargetValue)
    {
        String targetLocale = entry.getTargetLocaleAsString();
        if (falconTargetValue != null && falconTargetValue.length() > 55)
        {
            falconTargetValue = falconTargetValue.substring(0, 55);
        }
        String jobName = BlaiseConstants.HARLEY + "_" + falconTargetValue + "_" + targetLocale;

        return handleSpecialChars(jobName);
    }

    public static String getHarlyJobName(List<TranslationInboxEntryVo> entries,
            String falconTargetValue)
    {
        if (falconTargetValue != null && falconTargetValue.length() > 55)
        {
            falconTargetValue = falconTargetValue.substring(0, 55);
        }
        String jobName = BlaiseConstants.HARLEY + "_" + falconTargetValue + "_"
                + entries.get(0).getTargetLocaleAsString();

        return handleSpecialChars(jobName);
    }

    public static String getEntryJobName(TranslationInboxEntryVo entry)
    {
        String fileName = getEntryFileName(entry);
        fileName = fileName.substring(0, fileName.lastIndexOf("."));
        return fileName + "_XLF";
    }

    public static String getEntriesJobName(List<TranslationInboxEntryVo> entries)
    {
        ArrayList<String> targetLocales = new ArrayList<>();
        List<Long> relatedObjectIds = new ArrayList<Long>();
        String tmp;
        for (TranslationInboxEntryVo entry : entries)
        {
            long blaiseId = entry.getRelatedObjectId();
            if (relatedObjectIds.contains(blaiseId))
                continue;

            relatedObjectIds.add(blaiseId);
            tmp = entry.getTargetLocaleAsString();
            if (!targetLocales.contains(tmp))
                targetLocales.add(tmp);
        }
        Collections.sort(relatedObjectIds);
        StringBuilder ids = new StringBuilder();
        for (Long id : relatedObjectIds)
        {
            if (ids.length() < 50)
            {
                ids.append(id).append(" ");
            }
        }

        StringBuilder jobName = new StringBuilder();
        jobName.append("Blaise IDs (");
        jobName.append(ids.toString().trim()).append(")");
        jobName.append(BlaiseConstants.DASH);
        jobName.append(entries.get(0).getSourceLocaleAsString());
        if (targetLocales.size() == 1)
        {
            jobName.append(BlaiseConstants.DASH);
            jobName.append(entries.get(0).getTargetLocaleAsString());
        }

        return handleSpecialChars(jobName.toString());
    }

    private static String handleSpecialChars(String str)
    {
        initSpecialChars();

        for (String specialChar : specialChars)
        {
            str = str.replace(specialChar, " ");
        }

        // Replace all continuous space to single space
        while (str.indexOf("  ") > -1)
        {
            str = str.replace("  ", " ");
        }

        if (str != null)
            str = str.trim();

        return str;
    }

    private synchronized static void initSpecialChars()
    {
        if (specialChars.size() == 0)
        {
            specialChars.add("\\");
            specialChars.add("/");
            specialChars.add(":");
            specialChars.add(";");
            specialChars.add("*");
            specialChars.add("?");
            specialChars.add("\"");
            specialChars.add("'");
            specialChars.add("<");
            specialChars.add(">");
            specialChars.add("|");
            specialChars.add("%");
            specialChars.add("&");
        }
    }

    public static String fixLocale(String localeString)
    {
        if (localeString.startsWith("iw"))
        {
            localeString = "he" + localeString.substring(2);
        }
        else if (localeString.startsWith("ji"))
        {
            localeString = "yi" + localeString.substring(2);
        }
        else if (localeString.startsWith("in"))
        {
            localeString = "id" + localeString.substring(2);
        }

        return localeString;
    }

    /**
     * Change list to string comma separated.
     *
     * @return String like "string1,string2,string3".
     */
    public static String listToString(Collection<Long> objects)
    {
        StringBuilder buffer = new StringBuilder();
        int counter = 0;
        for (Long number : objects)
        {
            if (counter > 0)
            {
                buffer.append(",");
            }
            counter++;
            buffer.append(number);
        }

        return buffer.toString();
    }

    public static String getTypeByRelatedObjectClassName(String relatedObjectClassName)
    {
        if (relatedObjectClassName == null || relatedObjectClassName.trim().length() == 0)
            return null;

        String type = relatedObjectClassName2Type.get(relatedObjectClassName);
        // Just in case
        if (StringUtil.isEmpty(type))
            type = "Unkown";

        return type;
    }

    /**
     * Get the value of job attribute "Falcon Target Value" if it has.
     */
    public static String findFalconTargetValue(List<JobAttribute> jobAttribtues)
    {
        String falconTargetValue = null;
        if (jobAttribtues != null && jobAttribtues.size() != 0)
        {
            String displayName = null;
            String internalName = null;
            String type = null;
            for (JobAttribute attr : jobAttribtues)
            {
                displayName = attr.getAttribute().getDisplayName();
                internalName = attr.getAttribute().getName();
                if (BlaiseConstants.FALCON_TARGET_VALUE.equalsIgnoreCase(displayName)
                        || BlaiseConstants.FALCON_TARGET_VALUE.equalsIgnoreCase(internalName))
                {
                    type = attr.getAttribute().getType();
                    // If "Falcon Target Value" attribute is "text" type...
                    if (AttributeClone.TYPE_TEXT.equalsIgnoreCase(type))
                    {
                        falconTargetValue = (String) attr.getValue();
                        break;
                    }
                    // If "Falcon Target Value" attribute is "choicelist"
                    // type...
                    else if (AttributeClone.TYPE_CHOICE_LIST.equalsIgnoreCase(type))
                    {
                        @SuppressWarnings("unchecked")
                        List<String> choiceValue = (List<String>) attr.getValue();
                        if (choiceValue != null && choiceValue.size() > 0)
                        {
                            // pick up the first only
                            falconTargetValue = choiceValue.get(0);
                        }
                        break;
                    }
                }
            }
        }

        if (falconTargetValue != null && falconTargetValue.trim().length() == 0)
            falconTargetValue = null;

        return falconTargetValue;
    }

    public List<BlaiseConnectorAttribute> getConnectorAttributes(long blcId)
    {
        List<BlaiseConnectorAttribute> data = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try
        {
            conn = DbUtil.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(
                    "select * from connector_blaise_attributes where " + "connector_id=" + blcId);
            BlaiseConnectorAttribute attr = null;
            while (rs.next())
            {
                attr = new BlaiseConnectorAttribute();
                attr.setId(rs.getLong("id"));
                attr.setBlaiseConnectorId(blcId);
                attr.setAttributeId(rs.getLong("attribute_id"));
                attr.setAttributeValue(rs.getString("attribute_value"));
                attr.setAttributeType(rs.getString("attribute_type"));
                attr.setBlaiseJobType(rs.getString("blaise_job_type"));
                data.add(attr);
            }
        }
        catch (Exception e)
        {
            logger.error("Error found when getting Blaise connector attributes.", e);
        }
        finally
        {
            DbUtil.silentClose(rs);
            DbUtil.silentClose(stmt);
            DbUtil.silentReturnConnection(conn);
        }
        return data;
    }

    public static void saveConnectorAttributes(List<BlaiseConnectorAttribute> attributes)
    {
        if (attributes == null || attributes.size() == 0)
            return;
        Connection conn = null;
        Statement stmt = null;
        PreparedStatement pstmt = null;
        try
        {
            conn = DbUtil.getConnection();
            conn.setAutoCommit(false);
            stmt = conn.createStatement();
            stmt.execute("delete from connector_blaise_attributes where connector_id="
                    + attributes.get(0).getBlaiseConnectorId());

            pstmt = conn.prepareStatement("INSERT INTO "
                    + "connector_blaise_attributes (Connector_ID,Attribute_ID,Attribute_Value,"
                    + "Attribute_Type,Blaise_job_type) values (?, ?, ?, ?, ?)");
            for (BlaiseConnectorAttribute attribute : attributes)
            {
                pstmt.setLong(1, attribute.getBlaiseConnectorId());
                pstmt.setLong(2, attribute.getAttributeId());
                pstmt.setString(3, attribute.getAttributeValue());
                pstmt.setString(4, attribute.getAttributeType());
                pstmt.setString(5, attribute.getBlaiseJobType());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            conn.commit();
        }
        catch (Exception e)
        {
            try
            {
                conn.rollback();
            }
            catch (SQLException e1)
            {
            }
            logger.error("Error found in saveConnectorAttributes.", e);
        }
        finally
        {
            DbUtil.silentClose(pstmt);
            DbUtil.silentClose(stmt);
            DbUtil.silentReturnConnection(conn);
        }
    }

    private List<JobAttribute> getJobAttributes(String attributeString, BasicL10nProfile l10Profile)
    {
        List<JobAttribute> jobAttributeList = new ArrayList<JobAttribute>();

        if (l10Profile.getProject().getAttributeSet() == null)
        {
            return null;
        }

        if (StringUtils.isNotEmpty(attributeString))
        {
            String[] attributes = attributeString.split(";.;");
            for (String ele : attributes)
            {
                try
                {
                    String attributeId = ele.substring(ele.indexOf(",.,") + 3,
                            ele.lastIndexOf(",.,"));
                    String attributeValue = ele.substring(ele.lastIndexOf(",.,") + 3);

                    Attribute attribute = HibernateUtil.get(Attribute.class,
                            Long.parseLong(attributeId));
                    JobAttribute jobAttribute = new JobAttribute();
                    jobAttribute.setAttribute(attribute.getCloneAttribute());
                    if (attribute != null && StringUtils.isNotEmpty(attributeValue))
                    {
                        Condition condition = attribute.getCondition();
                        if (condition instanceof TextCondition)
                        {
                            jobAttribute.setStringValue(attributeValue);
                        }
                        else if (condition instanceof IntCondition)
                        {
                            jobAttribute.setIntegerValue(Integer.parseInt(attributeValue));
                        }
                        else if (condition instanceof FloatCondition)
                        {
                            jobAttribute.setFloatValue(Float.parseFloat(attributeValue));
                        }
                        else if (condition instanceof DateCondition)
                        {
                            SimpleDateFormat sdf = new SimpleDateFormat(DateCondition.FORMAT);
                            jobAttribute.setDateValue(sdf.parse(attributeValue));
                        }
                        else if (condition instanceof ListCondition)
                        {
                            String[] options = attributeValue.split("@@@");
                            List<String> optionValues = Arrays.asList(options);
                            jobAttribute.setValue(optionValues, false);
                        }
                    }
                    jobAttributeList.add(jobAttribute);
                }
                catch (Exception e)
                {
                    logger.error("Failed to get job attributes", e);
                }
            }
        }
        else
        {
            List<Attribute> attsList = l10Profile.getProject().getAttributeSet()
                    .getAttributeAsList();
            for (Attribute att : attsList)
            {
                JobAttribute jobAttribute = new JobAttribute();
                jobAttribute.setAttribute(att.getCloneAttribute());
                jobAttributeList.add(jobAttribute);
            }
        }

        return jobAttributeList;
    }

    public List<Long> getEntryIdsInGS(long blcId)
    {
        List<Long> ids = new ArrayList<>();
        Connection conn = null;
        try
        {
            conn = DbUtil.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                    "select blaise_entry_id from connector_blaise_job where blaise_connector_id="
                            + blcId);
            while (rs.next())
            {
                ids.add(rs.getLong(1));
            }
        }
        catch (Exception e)
        {
            logger.error("Error found when getEntryIdsInGS.", e);
        }
        finally
        {
            if (conn != null)
            {
                try
                {
                    DbUtil.returnConnection(conn);
                }
                catch (Exception e)
                {
                }
            }

        }
        return ids;
    }
}
