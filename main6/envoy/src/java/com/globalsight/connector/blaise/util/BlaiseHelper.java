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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;

import org.apache.log4j.Logger;

import com.cognitran.blaise.translation.api.ClientFactory;
import com.cognitran.blaise.translation.api.TranslationAgencyClient;
import com.cognitran.client.IncompatibleVersionException;
import com.cognitran.core.model.util.Collections;
import com.cognitran.translation.client.TranslationPageCommand;
import com.cognitran.translation.client.workflow.TranslationInboxEntry;
import com.cognitran.workflow.client.InboxEntry;
import com.globalsight.connector.blaise.vo.TranslationInboxEntryVo;
import com.globalsight.cxe.entity.blaise.BlaiseConnector;
import com.globalsight.cxe.entity.blaise.BlaiseConnectorJob;
import com.globalsight.everest.util.comparator.BlaiseInboxEntryComparator;
import com.globalsight.ling.common.URLEncoder;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.FileUtil;
import com.globalsight.util.StringUtil;

public class BlaiseHelper
{
    static private final Logger logger = Logger.getLogger(BlaiseHelper.class);

    private BlaiseConnector blc = null;
    private TranslationAgencyClient client = null;

    private static final String DASH = " - ";
    private static List<String> specialChars = new ArrayList<String>();

    private static final String HARLEY = "Harley";

    public static final List<java.util.Locale> blaiseSupportedLocales = new ArrayList<java.util.Locale>();
    public static final HashMap<String, com.cognitran.core.model.i18n.Locale> blaiseSupportedLocalesMap = new HashMap<String, com.cognitran.core.model.i18n.Locale>();

    public static final SortedMap<String, String> relatedObjectClassName2Type = new TreeMap<String, String>();
    public static final SortedMap<String, String> type2RelatedObjectClassName = new TreeMap<String, String>();

    static
    {
        java.util.Locale javaLocale = null;
        com.cognitran.core.model.i18n.Locale[] blaiseLocales = com.cognitran.core.model.i18n.Locale.values();
        for (com.cognitran.core.model.i18n.Locale blaiseLocale : blaiseLocales)
        {
            javaLocale = blaiseLocale.toLocale();
            blaiseSupportedLocales.add(javaLocale);
            String key = javaLocale.getLanguage() + "_" + javaLocale.getCountry();
            blaiseSupportedLocalesMap.put(key.toLowerCase(), blaiseLocale);
        }

        relatedObjectClassName2Type.put("com.cognitran.publication.model.media.Graphic", "Graphic");
        relatedObjectClassName2Type.put(
                "com.cognitran.publication.model.standalone.StandalonePublication", "Standalone");
        relatedObjectClassName2Type.put("com.cognitran.publication.model.composite.Procedure",
                "Procedure");
        relatedObjectClassName2Type.put(
                "com.cognitran.publication.model.controlled.ControlledContent",
                "Controlled content");
        relatedObjectClassName2Type.put(
                "com.cognitran.translation.model.TranslatableObjectsDocument",
                "Translatable object");

        type2RelatedObjectClassName.put("Graphic", "com.cognitran.publication.model.media.Graphic");
        type2RelatedObjectClassName.put("Standalone",
                "com.cognitran.publication.model.standalone.StandalonePublication");
        type2RelatedObjectClassName.put("Procedure",
                "com.cognitran.publication.model.composite.Procedure");
        type2RelatedObjectClassName.put("Controlled content",
                "com.cognitran.publication.model.controlled.ControlledContent");
        type2RelatedObjectClassName.put("Translatable object",
                "com.cognitran.translation.model.TranslatableObjectsDocument");
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
    	catch(UnknownHostException e)
    	{
    		logger.warn("Incorrect URL: " + blc.getUrl());
    		return "Incorrect URL!";
    	}
    	catch (SecurityException e)
    	{
    		logger.warn("Incorrect username or password: " + blc.getUsername() + "/" + blc.getPassword());
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
            for (InboxEntry entry : inboxEntries)
            {
                try
                {
                    TranslationInboxEntryVo vo = new TranslationInboxEntryVo(
                            (TranslationInboxEntry) entry);
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
            logger.error("Error when invoke listInbox(TranslationPageCommand command): ", e);
        }

        return results;
    }

    public List<TranslationInboxEntryVo> listInboxByIds(Set<Long> ids, TranslationPageCommand command)
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
                    TranslationInboxEntryVo vo = new TranslationInboxEntryVo(
                            (TranslationInboxEntry) entry);
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
                    TranslationInboxEntryVo vo = new TranslationInboxEntryVo(
                            (TranslationInboxEntry) entry);
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
	 * 
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
     * Download XLIFF file. Note that current entry must be claimed already.
     * 
     * @param entry -- TranslationInboxEntryVo
     * @param storeFile -- storeFile
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
		    if (!isEntryExisted(entryId))
		    {
		        return;
		    }

		    TranslationAgencyClient client = getTranslationClient();
            if (client == null)
            {
                logger.error("TranslationAgencyClient is null, entry cannot be uploaded: " + entryId);
                return;
            }

            // A simple replace to "cheat" Blaise API
            String content = FileUtil.readFile(file, "UTF-8");
            content = StringUtil.replace(content, "<target state=\"new\"", "<target state=\"translated\"");
            content = StringUtil.replace(content, "<target state=\"needs-review-translation\"", "<target state=\"translated\"");
            content = StringUtil.replace(content, "<target state=\"needs-i10n\"", "<target state=\"translated\"");
            FileUtil.writeFile(file, content, "UTF-8");

            InputStream is = new FileInputStream(file);
			client.uploadXliff(entryId, is);
            logger.info("Blaise file is uploaded successfully for entryId: " + entryId);

            updateUploadXliffSuccess(jobId, entryId);
		}
		catch (Exception e)
		{
		    updateUploadXliffFail(jobId, entryId);
			logger.error("Error when uploadXliff for job:" + jobId, e);
		}
	}

	private void updateUploadXliffSuccess(long jobId, long blaiseEntryId)
	{
        BlaiseConnectorJob bcj = BlaiseManager.getBlaiseConnectorJobByJobIdEntryId(jobId,
                blaiseEntryId);
        if (bcj != null)
        {
            bcj.setUploadXliffState(BlaiseConnectorJob.SUCCEED);
            HibernateUtil.saveOrUpdate(bcj);
        }
	}

    private void updateUploadXliffFail(long jobId, long blaiseEntryId)
    {
        BlaiseConnectorJob bcj = BlaiseManager.getBlaiseConnectorJobByJobIdEntryId(jobId,
                blaiseEntryId);
        if (bcj != null)
        {
            bcj.setUploadXliffState(BlaiseConnectorJob.FAIL);
            HibernateUtil.saveOrUpdate(bcj);
        }
    }

    public void complete(long id, long jobId)
    {
		try
		{
		    if (!isEntryExisted(id))
		    {
		        return;
		    }

		    TranslationAgencyClient client = getTranslationClient();
		    if (client == null)
		    {
		        logger.error("TranslationAgencyClient is null, entry cannot be completed: " + id);
		        return;
		    }

		    client.complete(id);
            logger.info("Blaise entry is completed successfully: " + id);

            updateCompleteSuccess(jobId, id);
		}
		catch (Exception e)
		{
		    updateCompleteFail(jobId, id);
			logger.error("Failed to complete entry for job: " + jobId, e);
		}
    }

	private void updateCompleteSuccess(long jobId, long blaiseEntryId)
	{
        BlaiseConnectorJob bcj = BlaiseManager.getBlaiseConnectorJobByJobIdEntryId(jobId,
                blaiseEntryId);
        if (bcj != null)
        {
            bcj.setCompleteState(BlaiseConnectorJob.SUCCEED);
            HibernateUtil.saveOrUpdate(bcj);
        }
	}

	private void updateCompleteFail(long jobId, long blaiseEntryId)
	{
        BlaiseConnectorJob bcj = BlaiseManager.getBlaiseConnectorJobByJobIdEntryId(jobId,
                blaiseEntryId);
        if (bcj != null)
        {
            bcj.setCompleteState(BlaiseConnectorJob.FAIL);
            HibernateUtil.saveOrUpdate(bcj);
        }
	}

	public boolean isEntryExisted(long id)
    {
        Set<Long> ids = new HashSet<Long>();
        ids.add(id);

        TranslationPageCommand command = initTranslationPageCommand(1, 100, null, null, null, null,
                null, 0, false);
        List<TranslationInboxEntryVo> entries = listInboxByIds(ids, command);
        if (entries == null || entries.size() == 0)
        {
            logger.warn("Entry " + id + " is not existed already, cannot operate on it.");
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
     * 
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
        catch(UnknownHostException e)
        {
            logger.error("Incorrect URL: " + blc.getUrl(), e);
        }
        catch (SecurityException e)
        {
            logger.error("Incorrect username or password: " + blc.getUsername() + "/"
                    + blc.getPassword(), e);
        }
        catch (IncompatibleVersionException e)
        {
            logger.error("Incorrect client core version: " + blc.getClientCoreVersion(), e);
        }
        catch (Exception e)
        {
            logger.error("Fail to get Blaise translation client: ", e);
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

        String localeInfo = fixLocale(entry.getEntry().getTargetLocale().getLocaleCode());
		fileName.append("Blaise ID ").append(entry.getRelatedObjectId())
		        .append(DASH).append(entry.getSourceRevision())
				.append(DASH).append(des)
				.append(DASH).append(localeInfo)
				.append(".xlf").toString();
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
     * @return String -- job name
     * 
     */
	public static String getHarlyJobName(TranslationInboxEntryVo entry, String falconTargetValue)
	{
        String targetLocale = entry.getEntry().getTargetLocale().getLocaleCode();
        targetLocale = BlaiseHelper.fixLocale(targetLocale);
        if (falconTargetValue != null && falconTargetValue.length() > 55)
        {
            falconTargetValue = falconTargetValue.substring(0, 55);
        }
        String jobName = HARLEY + "_" + falconTargetValue + "_" + targetLocale;

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
        List<Long> relatedObjectIds = new ArrayList<Long>();
        for (TranslationInboxEntryVo entry : entries)
        {
            long blaiseId = entry.getRelatedObjectId();
            if (relatedObjectIds.contains(blaiseId))
                continue;

            relatedObjectIds.add(blaiseId);
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
        jobName.append(DASH);
        jobName.append(fixLocale(entries.get(0).getEntry().getSourceLocale().getLocaleCode()));
        jobName.append(DASH);
        jobName.append(fixLocale(entries.get(0).getEntry().getTargetLocale().getLocaleCode()));

        return handleSpecialChars(jobName.toString());
    }

	private static String handleSpecialChars(String fileName)
	{
		initSpecialChars();

		for (String specialChar : specialChars)
		{
			fileName = fileName.replace(specialChar, " ");
		}

		// Replace all continuous space to single space
		while (fileName.indexOf("  ") > -1)
		{
			fileName = fileName.replace("  ", " ");
		}
		return fileName;
	}

	private synchronized static void initSpecialChars()
	{
		if (specialChars.size() == 0)
		{
			specialChars.add("\\");
			specialChars.add("/");
			specialChars.add(":");
			specialChars.add("*");
			specialChars.add("?");
			specialChars.add("\"");
			specialChars.add("'");
			specialChars.add("<");
			specialChars.add(">");
			specialChars.add("|");
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
}
